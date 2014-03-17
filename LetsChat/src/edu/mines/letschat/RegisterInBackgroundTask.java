package edu.mines.letschat;

import java.io.IOException;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

public class RegisterInBackgroundTask extends AsyncTask<Void, Void, String>{
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	static final String TAG = "GCM Demo";
	GoogleCloudMessaging gcm;
	String SENDER_ID = "1065664420508";
	Context context;
	String regid, username;
	OnTaskCompleted listener;
	String password;
	ProgressDialog dialog;

	public RegisterInBackgroundTask(GoogleCloudMessaging gcm, Context context, String regid, String username, String password, OnTaskCompleted listener) {
		this.gcm = gcm;
		this.context = context;
		this.regid = regid;
		this.username = username;
		this.listener = listener;
		this.password = password;
	}
	
	@Override
    protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setCancelable(false);
		dialog.setMessage("Siging up please wait...");
		dialog.show();
	}
	
	@Override
	protected String doInBackground(Void... params) {
		String msg = "";
		try {
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(context);
			}
			
			final SharedPreferences prefs = getGcmPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("password", password);
			editor.putString("username", username);
			editor.commit();
			
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
//			return regid;
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
//		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		dialog.dismiss();
		listener.onTaskCompleted();
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
	 * messages to your app. Not needed for this demo since the device sends upstream messages
	 * to a server that echoes back the message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		new RegisterUser("registerUser", regid, username, password, context).execute();
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
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
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

}
