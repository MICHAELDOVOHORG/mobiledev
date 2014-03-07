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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
    public static final String EXTRA_DEVICE_ID = "edu.mines.letschat.EXTRA_DEVICE_ID";
    private ArrayList<String> users = new ArrayList<String>();

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "1065664420508";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demo";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mDisplay = (TextView) findViewById(R.id.display);

        context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
            	Log.v(TAG, regid);
            }
            new RegisterUser().execute();
            new GetUsers().execute();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
//        Book book = new Book(context, "Title here", "2nd edition");
//        book.save();
    }
    
    public void sugar(View v) {
    	List<Book> b = Book.find(Book.class, "title = ?", "Title here");
    	TextView tv = (TextView) findViewById(R.id.sugar);
    	tv.setText(b.get(0).title);
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
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
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
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    // Send an upstream message.
    public void onClick(final View view) {

        if (view == findViewById(R.id.send)) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    try {
                        Bundle data = new Bundle();
                        data.putString("my_message", "Hello World");
                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
      // Your implementation here.
    }
    
    class RegisterUser extends AsyncTask<String, String, String> {

    	private static final String FUNCTION = "registerUser";
    	private static final String API = "http://justacomputerscientist.com/mobile/api.php";
    	
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
    		try {
    			httpPost.setEntity(new UrlEncodedFormEntity(params));
    			response = httpClient.execute(httpPost);
    			HttpEntity entity = response.getEntity();

    			if (entity != null) {
    				InputStream instream = entity.getContent();
    				result = convert(instream);
    				instream.close();
    				Log.v("RESULT", "In Register" + result);
//    				jsonarray = new JSONArray(result);
//
//    				// looping through All Names
//    				for (int i = 0; i < jsonarray.length(); i++) {
//    					JSONObject obj = jsonarray.getJSONObject(i);
//
//    					// Storing each json item in variable
//    					int id = obj.getInt("id");
//    					String date = obj.getString("date");
//    					String startTime = obj.getString("start_time");
//    					String endTime = obj.getString("end_time");
//    					String title = obj.getString("title");
////    					Log.v("TITLE", title);
//    					int eventTypeId;
//    					if  (obj.isNull("event_type_id")) {
//    						eventTypeId = 0;
//    					}
//    					else {
//    						eventTypeId = obj.getInt("event_type_id");
//    					}
//    				}
    			}
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}

    		return result;
    	}
    	
    	@Override
    	protected void onPostExecute (String file_url) {
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
    
    class GetUsers extends AsyncTask<String, String, String> {

    	private static final String FUNCTION = "getAllUsers";
    	private static final String API = "http://justacomputerscientist.com/mobile/api.php";
    	
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
    		try {
    			httpPost.setEntity(new UrlEncodedFormEntity(params));
    			response = httpClient.execute(httpPost);
    			HttpEntity entity = response.getEntity();

    			if (entity != null) {
    				InputStream instream = entity.getContent();
    				result = convert(instream);
    				instream.close();
    				Log.v("RESULT", "Get users" + result);
    				jsonarray = new JSONArray(result);

    				// looping through All Names
    				for (int i = 0; i < jsonarray.length(); i++) {
    					JSONObject obj = jsonarray.getJSONObject(i);

    					// Storing each json item in variable
    					int id = obj.getInt("id");
    					String key = obj.getString("key");
    					Log.v("KEY", key);
    					users.add(key);
    				}
    			}
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    		return result;
    	}
    	
    	@Override
    	protected void onPostExecute (String file_url) {
//    		String[] lv_arr = {};
//    		lv_arr = (String[]) users.toArray();
    		ListView lv = (ListView) findViewById(R.id.list);
    		lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, users));
    		lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					String recipientID = ((TextView) arg1).getText().toString();
//					new SendNotification(recipientID).execute(); 
					Intent intent = new Intent(MainActivity.this, MessageActivity.class);
					intent.putExtra(MainActivity.EXTRA_DEVICE_ID, users.get(arg2));
					startActivity(intent);
				}
			});
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
    
    class SendNotification extends AsyncTask<String, String, String> {

    	private static final String FUNCTION = "sendNotification";
    	private static final String API = "http://justacomputerscientist.com/mobile/api.php";
    	private String recipientID;
    	
    	public SendNotification(String recipientID) {
    		this.recipientID = recipientID;
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
    		params.add(new BasicNameValuePair("recipient", recipientID));
    		params.add(new BasicNameValuePair("sender", regid));
    		try {
    			httpPost.setEntity(new UrlEncodedFormEntity(params));
    			response = httpClient.execute(httpPost);
    			HttpEntity entity = response.getEntity();

    			if (entity != null) {
    				InputStream instream = entity.getContent();
    				result = convert(instream);
    				instream.close();
    				Log.v("RESULT", result);
//    				jsonarray = new JSONArray(result);
//
//    				// looping through All Names
//    				for (int i = 0; i < jsonarray.length(); i++) {
//    					JSONObject obj = jsonarray.getJSONObject(i);
//
//    					// Storing each json item in variable
//    					int id = obj.getInt("id");
//    					String date = obj.getString("date");
//    					String startTime = obj.getString("start_time");
//    					String endTime = obj.getString("end_time");
//    					String title = obj.getString("title");
////    					Log.v("TITLE", title);
//    					int eventTypeId;
//    					if  (obj.isNull("event_type_id")) {
//    						eventTypeId = 0;
//    					}
//    					else {
//    						eventTypeId = obj.getInt("event_type_id");
//    					}
//    				}
    			}
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}

    		return result;
    	}
    	
    	@Override
    	protected void onPostExecute (String file_url) {
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