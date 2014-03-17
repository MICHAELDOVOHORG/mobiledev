package edu.mines.letschat;

import java.io.IOException;
import java.io.InputStream;
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

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

class RegisterUser extends Task {
	String regid, username, password;
	ProgressDialog dialog;
	Context context;

	public RegisterUser(String function, String regid, String username, String password) {
		super(function);
		this.regid = regid;
		this.username = username;
		this.password = password;
	}
	
	public RegisterUser(String function, String regid, String username, String password, Context context) {
		super(function);
		this.regid = regid;
		this.username = username;
		this.password = password;
		this.context = context;
	}
	
	@Override
    protected void onPreExecute() {
		if (context != null) {
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Siging up please wait...");
			dialog.show();
		}
	}

	@Override
	protected String doInBackground(String... arg0) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(super.getApi());
		HttpResponse response;
		String result = "";

		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("func", super.getFunction()));
		params.add(new BasicNameValuePair("deviceKey", regid));
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				result = convert(instream);
				instream.close();
				Log.v("RESULT", result);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	@Override
	protected void onPostExecute(String msg) {
//		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		if (context != null) {
			dialog.dismiss();
		}
	}
}
