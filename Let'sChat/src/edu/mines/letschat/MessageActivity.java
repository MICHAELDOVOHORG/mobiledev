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
import org.json.JSONArray;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public class MessageActivity extends Activity {
	private String recipientID;
	TextCapitalizeResultReceiver capitalizeResultReceiver;
	static final String TAG = "GCM Demo";
	ListView conversationList;
	ArrayList<String> messages = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		// Show the Up button in the action bar.
		setupActionBar();
		
		recipientID = getIntent().getStringExtra(MainActivity.EXTRA_DEVICE_ID);
		TextView tv = (TextView) findViewById(R.id.conversationWith);
		tv.setText(recipientID);
//		Toast.makeText(this, recipientID, Toast.LENGTH_LONG).show();
		conversationList = (ListView) findViewById(R.id.conversationList);
		conversationList.setAdapter( new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, messages));
	}
	
	@Override
    protected void onResume() {
    	registerReceiver();
        super.onResume();
    }
	
	@Override
    protected void onPause() {
  		Log.i(TAG,"onPause()");
  		/* we should unregister BroadcastReceiver here*/
  		unregisterReceiver(capitalizeResultReceiver);
  		super.onPause();
  	}
	
	public void sendMessage(View v) {
		EditText et = (EditText) findViewById(R.id.typingArea);
		String message = et.getText().toString();
		new SendNotification(recipientID, message).execute();
	}
	
	private void registerReceiver() {
		/*create filter for exact intent what we want from other intent*/
		IntentFilter intentFilter =new IntentFilter(TextCapitalizeResultReceiver.ACTION_TEXT_CAPITALIZED);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		/* create new broadcast receiver*/
		capitalizeResultReceiver=new TextCapitalizeResultReceiver();
		/* registering our Broadcast receiver to listen action*/
		registerReceiver(capitalizeResultReceiver, intentFilter);
	}
	
	public class TextCapitalizeResultReceiver extends BroadcastReceiver {
      	/**
    		 * action string for our broadcast receiver to get notified
    		 */
    		public final static String ACTION_TEXT_CAPITALIZED= "com.android.guide.exampleintentservice.intent.action.ACTION_TEXT_CAPITALIZED";
    		@Override
    		public void onReceive(Context context, Intent intent) {
    			String resultText =intent.getStringExtra(GcmIntentService.OUTPUT_TEXT);
//    			Toast.makeText(MessageActivity.this, resultText, Toast.LENGTH_LONG).show();
    			messages.add(resultText);
    			ArrayAdapter ad = (ArrayAdapter) conversationList.getAdapter();
    			ad.notifyDataSetChanged();
    		}
    };

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class SendNotification extends AsyncTask<String, String, String> {

    	private static final String FUNCTION = "sendNotification";
    	private static final String API = "http://justacomputerscientist.com/mobile/api.php";
    	private String recipientID;
    	private String message;
    	
    	public SendNotification(String recipientID, String message) {
    		this.recipientID = recipientID;
    		this.message = message;
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
    		params.add(new BasicNameValuePair("message", message));
//    		params.add(new BasicNameValuePair("sender", regid));
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