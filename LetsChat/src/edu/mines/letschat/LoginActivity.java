package edu.mines.letschat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	public static final String EXTRA_LOGIN = "edu.mines.letschat.EXTRA_LOGIN";
	public static final String EXTRA_PASSWORD = "edu.mines.letschat.EXTRA_PASSWORD";
	private SharedPreferences sharedPref;
	String loginResult, signupResult;
	String username, password, confirmpassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		//If the user is logged in, then don't show the login screen. Go straight to the schedule page
		if (sharedPref.getBoolean("loggedIn", false)) {
			Intent intent = new Intent(this, MainActivity.class);
			String username = sharedPref.getString("username", "");
			String password = sharedPref.getString("password", "");
			intent.putExtra(EXTRA_LOGIN, username);
			intent.putExtra(EXTRA_PASSWORD, password);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sharedPref.getBoolean("loggedIn", false)) {
			Intent intent = new Intent(this, MainActivity.class);
			String username = sharedPref.getString("username", "");
			String password = sharedPref.getString("password", "");
			intent.putExtra(EXTRA_LOGIN, username);
			intent.putExtra(EXTRA_PASSWORD, password);
			startActivity(intent);
			finish();
		}
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.login, menu);
//		return true;
//	}

	public void login(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View inflateView = inflater.inflate(R.layout.dialog_signin, null);
		builder.setView(inflateView);
		// Add action buttons
		final EditText usernameEdit = (EditText) inflateView.findViewById(R.id.username);
		final EditText passwordEdit = (EditText) inflateView.findViewById(R.id.password);
		builder.setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				username = usernameEdit.getText().toString();
				password = passwordEdit.getText().toString();
				new CheckLogin(username, password, new OnTaskCompleted() {

					@Override
					public void onTaskCompleted() {
						Log.v("login result", loginResult);
						if (loginResult.equals("Success\n")) {
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putBoolean("loggedIn", true);
							editor.putString("username", username);
							editor.putString("password", password);
							editor.commit();
							Intent intent = new Intent(LoginActivity.this, MainActivity.class);
							intent.putExtra(EXTRA_LOGIN, username);
							intent.putExtra(EXTRA_PASSWORD, password);
							startActivity(intent);
							finish();
						} else {
							Toast.makeText(LoginActivity.this, "Sorry incorrect username/password", Toast.LENGTH_LONG).show();
						}
					}
					
				}).execute();
				//					result = cl.get();
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});      
		Dialog dialog = builder.create();
		dialog.show();
	}

	public void register(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View inflateView = inflater.inflate(R.layout.dialog_signup, null);
		builder.setView(inflateView);
		// Add action buttons
		final EditText usernameEdit = (EditText) inflateView.findViewById(R.id.username);
		final EditText passwordEdit = (EditText) inflateView.findViewById(R.id.password);
		final EditText confirmpasswordEdit = (EditText) inflateView.findViewById(R.id.confirmpassword);
		builder.setPositiveButton(R.string.signup, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				username = usernameEdit.getText().toString();
				password = passwordEdit.getText().toString();
				confirmpassword = confirmpasswordEdit.getText().toString();
				if (username.isEmpty()) {
					Toast.makeText(LoginActivity.this, "Username cannot be empty", Toast.LENGTH_LONG).show();
				}
				else if (password.isEmpty()) {
					Toast.makeText(LoginActivity.this, "Password cannot be empty", Toast.LENGTH_LONG).show();
				}
				else if (!password.equals(confirmpassword)) {
					Toast.makeText(LoginActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
				} else {
//					String result;
						new CheckUser(username, new OnTaskCompleted(){

							@Override
							public void onTaskCompleted() {
								if (signupResult.equals("Success\n")) {
									SharedPreferences.Editor editor = sharedPref.edit();
									editor.putBoolean("loggedIn", true);
									editor.putString("username", username);
									editor.putString("password", password);
									editor.commit();
									Intent intent = new Intent(LoginActivity.this, MainActivity.class);
									intent.putExtra(EXTRA_LOGIN, username);
									intent.putExtra(EXTRA_PASSWORD, password);
									startActivity(intent);
									finish();
								} else {
									Toast.makeText(LoginActivity.this, "Sorry that username already exists", Toast.LENGTH_LONG).show();
								}
							}
							
						}).execute();
//						result = cu.get();
				}
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});      
		Dialog dialog = builder.create();
		dialog.show();
	}

	public class CheckUser extends AsyncTask<String, String, String> {

		private static final String FUNCTION = "checkIfUserExists";
		private static final String API = "http://justacomputerscientist.com/mobile/api.php";
		private String username;
		ProgressDialog dialog;
		OnTaskCompleted listener;
		//		private OnTaskCompleted listener;
		//
		//		public GetUsers(OnTaskCompleted listener) {
		//			this.listener = listener;
		//		}

		public CheckUser(String username, OnTaskCompleted listener) {
			this.username = username;
			this.listener = listener;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setCancelable(false);
			dialog.setMessage("Siging up please wait...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(API);
			HttpResponse response;
//			String result = "";

			List<NameValuePair> params = new ArrayList<NameValuePair>(1);
			params.add(new BasicNameValuePair("func", FUNCTION));
			params.add(new BasicNameValuePair("username", username));
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(params));
				response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream instream = entity.getContent();
					signupResult = convert(instream);
					instream.close();
					Log.v("RESULT", signupResult);
					if (signupResult.equals("Success\n")) {
						return "Success";
					} else {
						return "Fail";
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return signupResult;
		}

		@Override
		protected void onPostExecute(String msg) {
			dialog.dismiss();
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

	public class CheckLogin extends AsyncTask<String, String, String> {

		private static final String FUNCTION = "checkLogin";
		private static final String API = "http://justacomputerscientist.com/mobile/api.php";
		private String username, password;
		ProgressDialog dialog;
		private OnTaskCompleted listener;
		//
		//		public GetUsers(OnTaskCompleted listener) {
		//			this.listener = listener;
		//		}

		public CheckLogin(String username, String password, OnTaskCompleted listener) {
			this.username = username;
			this.password = password;
			this.listener = listener;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setCancelable(false);
			dialog.setMessage("Siging in please wait...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(API);
			HttpResponse response;
//			String result = "";

			List<NameValuePair> params = new ArrayList<NameValuePair>(1);
			params.add(new BasicNameValuePair("func", FUNCTION));
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("password", password));
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(params));
				response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream instream = entity.getContent();
					loginResult = convert(instream);
					instream.close();
					Log.v("RESULT", loginResult);
					if (loginResult.equals("Success\n")) {
						return "Success";
					} else {
						return "Fail";
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return loginResult;
		}

		@Override
		protected void onPostExecute(String msg) {
			dialog.dismiss();
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
