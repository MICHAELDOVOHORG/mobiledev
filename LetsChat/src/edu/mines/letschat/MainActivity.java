/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mines.letschat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main UI for the demo app.
 */
public class MainActivity extends Activity {

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_USER_NAME = "edu.mines.letschat.EXTRA_USER_NAME";
	public static final String EXTRA_DEVICE_ID = "edu.mines.letschat.EXTRA_DEVICE_ID";
	public static final String EXTRA_SENDER_ID = "edu.mines.letschat.EXTRA_SENDER_ID";
	public static final String EXTRA_NOTIFICATION_RETRIEVE = "edu.mines.letschat.EXTRA_NOTIFICATION_RETRIEVE";
	public String username;
	private ArrayList<String> users = new ArrayList<String>();
	public HashMap<String, String> map;
	static final String TAG = "GCM Demo";

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;

	String regid;

	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Log.v(TAG, "In on new intent");
		Bundle extras = intent.getExtras();
		if(extras != null){
			String string = extras.getString(EXTRA_NOTIFICATION_RETRIEVE);
			if (string!= null) {
				Intent tent = new Intent(MainActivity.this, MessageActivity.class);
				tent.putExtra(MainActivity.EXTRA_DEVICE_ID, string);
				tent.putExtra(MainActivity.EXTRA_SENDER_ID, regid);
				for (Entry<String, String> entry : map.entrySet()) {
					if (string.equals(entry.getValue())) {
						String username = entry.getKey();
						Log.v(TAG, username);
						tent.putExtra(MainActivity.EXTRA_USER_NAME, username);
					}
				}
				startActivity(tent); 
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = getApplicationContext();
		username = getIntent().getStringExtra(LoginActivity.EXTRA_LOGIN);

		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			} else {
				Log.v(TAG, regid);
				new GetUsers(new OnTaskCompleted() {
					@Override
					public void onTaskCompleted() {
						onNewIntent(getIntent());
					}
				}).execute();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Check device for Play Services APK.
		checkPlayServices();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new RegisterInBackgroundTask(gcm, context, regid, username, new OnTaskCompleted() {
			@Override
			public void onTaskCompleted() {
				new GetUsers(new OnTaskCompleted() {
					@Override
					public void onTaskCompleted() {
						onNewIntent(getIntent());
					}
				}).execute();
			}
		}).execute();
	}

	//    // Send an upstream message.
	//    public void onClick(final View view) {
	//
	//        if (view == findViewById(R.id.send)) {
	//            new AsyncTask<Void, Void, String>() {
	//                @Override
	//                protected String doInBackground(Void... params) {
	//                    String msg = "";
	//                    try {
	//                        Bundle data = new Bundle();
	//                        data.putString("my_message", "Hello World");
	//                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
	//                        String id = Integer.toString(msgId.incrementAndGet());
	//                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
	//                        msg = "Sent message";
	//                    } catch (IOException ex) {
	//                        msg = "Error :" + ex.getMessage();
	//                    }
	//                    return msg;
	//                }
	//
	//                @Override
	//                protected void onPostExecute(String msg) {
	//                    mDisplay.append(msg + "\n");
	//                }
	//            }.execute(null, null, null);
	//        } else if (view == findViewById(R.id.clear)) {
	//            mDisplay.setText("");
	//        }
	//    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class GetUsers extends AsyncTask<String, String, String> {

		private static final String FUNCTION = "getAllUsers";
		private static final String API = "http://justacomputerscientist.com/mobile/api.php";
		private OnTaskCompleted listener;

		public GetUsers(OnTaskCompleted listener) {
			this.listener = listener;
		}

		@Override
		protected String doInBackground(String... arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(API);
			HttpResponse response;
			String result = "";
			JSONArray jsonarray;

			List<NameValuePair> params = new ArrayList<NameValuePair>(1);
			params.add(new BasicNameValuePair("func", FUNCTION));
			params.add(new BasicNameValuePair("deviceKey", regid));
			map = new HashMap<String, String>();
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(params));
				response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream instream = entity.getContent();
					result = convert(instream);
					instream.close();
					jsonarray = new JSONArray(result);

					// looping through All Names
					for (int i = 0; i < jsonarray.length(); i++) {
						JSONObject obj = jsonarray.getJSONObject(i);

						// Storing each json item in variable
						String key = obj.getString("key");
						String username = obj.getString("username");
						map.put(username, key);
						users.add(username);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected void onPostExecute (String file_url) {
			ListView lv = (ListView) findViewById(R.id.list);
			lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, users));
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Intent intent = new Intent(MainActivity.this, MessageActivity.class);
					intent.putExtra(MainActivity.EXTRA_DEVICE_ID, map.get(users.get(arg2)));
					intent.putExtra(MainActivity.EXTRA_SENDER_ID, regid);
					intent.putExtra(MainActivity.EXTRA_USER_NAME, users.get(arg2));
					startActivity(intent);
				}
			});
			listener.onTaskCompleted();
		}

		public String convert(InputStream is) {
			/*
			 * To convert the InputStream to String we use the BufferedReader.readLine()
			 * method. We iterate until the BufferedReader return null which means
			 * there's no more data to read. Each line will appended to a StringBuilder
			 * and returned as String.
			 */
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();

			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return sb.toString();
		}
	}
}
