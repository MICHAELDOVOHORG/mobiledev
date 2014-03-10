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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public class MessageActivity extends Activity {
	private String recipientID;
	private String senderID;
	private String userName;
	TextCapitalizeResultReceiver capitalizeResultReceiver;
	static final String TAG = "GCM Demo";
	ListView conversationList;
	AwesomeAdapter awesome;
	ArrayList<Message> messages = new ArrayList<Message>();
	List<Conversation>  listOfConverstations;
	int toDelete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		// Show the Up button in the action bar.
		setupActionBar();
		
		recipientID = getIntent().getStringExtra(MainActivity.EXTRA_DEVICE_ID);
		senderID = getIntent().getStringExtra(MainActivity.EXTRA_SENDER_ID);
		userName = getIntent().getStringExtra(MainActivity.EXTRA_USER_NAME);
		TextView tv = (TextView) findViewById(R.id.conversationWith);
		tv.setText("Conversation with " + userName);
		conversationList = (ListView) findViewById(R.id.conversationList);
		awesome = new AwesomeAdapter(this, messages);
		conversationList.setAdapter(awesome);
		conversationList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
				toDelete = arg2;
                builder.setTitle("Action:");
                builder.setItems(new String[] {"Delete"}, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
        				messages.remove(toDelete);
        				awesome.notifyDataSetChanged();
        				listOfConverstations.get(toDelete).delete();

//                        new AlertDialog.Builder(MessageActivity.this)
//                        .setTitle(getString(R.string.delete))
//                        .setMessage(getString(R.string.remove))
//                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) { 
////                                Intent intent = new Intent(CartDetailsActivity.this, HomeScreen.class);
////                                startActivity(intent);
//                            }
//                         })
//                         .show();
        				AwesomeAdapter.animate = false;
                    }

                });

                AlertDialog alert = builder.create();
                alert.show();
				return false;
			}
		});
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		Button button = (Button) findViewById(R.id.sendMessage);
		button.setEnabled(false);
		EditText et = (EditText) findViewById(R.id.typingArea);
		et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0) {
					Button button = (Button) findViewById(R.id.sendMessage);
					button.setEnabled(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				Button button = (Button) findViewById(R.id.sendMessage);
				button.setEnabled(false);
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}
	
	public void populateConversationList() {
		messages.clear();
		listOfConverstations = Conversation.findWithQuery(Conversation.class, "SELECT * FROM CONVERSATION WHERE (recipient_id = \"" + recipientID + "\" AND sender_id = \"" + senderID + "\")" +
				" OR (recipient_id = \"" + senderID + "\" AND sender_id = \"" + recipientID + "\");");
		
		Log.v(TAG, listOfConverstations.size() + "");
		for (Conversation c : listOfConverstations) {
			messages.add(new Message(c.message, c.sent));
		}
		AwesomeAdapter ad = (AwesomeAdapter) conversationList.getAdapter();
    	ad.notifyDataSetChanged();
    	conversationList.setSelection(messages.size() - 1);
	}
	
	@Override
    protected void onResume() {
    	registerReceiver();
    	populateConversationList();
    	GcmIntentService.messages.clear();
    	GcmIntentService.notificationCounter = 0;
    	NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.cancel(GcmIntentService.NOTIFICATION_ID);
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
		AwesomeAdapter.animate = true;
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
//    			String resultText =intent.getStringExtra(GcmIntentService.OUTPUT_TEXT);
    			AwesomeAdapter.animate = true;
    			populateConversationList();
    			GcmIntentService.messages.clear();
    	    	GcmIntentService.notificationCounter = 0;
    	    	NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    	    	notificationManager.cancel(GcmIntentService.NOTIFICATION_ID);
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
    		
    		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
    		params.add(new BasicNameValuePair("func", FUNCTION));
    		params.add(new BasicNameValuePair("recipient", recipientID));
    		params.add(new BasicNameValuePair("message", message));
    		params.add(new BasicNameValuePair("sender", senderID));
    		try {
    			httpPost.setEntity(new UrlEncodedFormEntity(params));
    			response = httpClient.execute(httpPost);

    			HttpEntity entity = response.getEntity();

    			if (entity != null) {
    				InputStream instream = entity.getContent();
    				result = convert(instream);
    				instream.close();
    				Log.v("RESULT", result);
    				Conversation convo = new Conversation(MessageActivity.this, senderID, recipientID, message, true);
        			convo.save();
        			runOnUiThread(new Runnable() {
						public void run() {
							messages.add(new Message(message, true));
		        			AwesomeAdapter adapter = (AwesomeAdapter) conversationList.getAdapter();
		        			adapter.notifyDataSetChanged();
		        			conversationList.setSelection(messages.size() - 1);
		        			EditText et = (EditText) findViewById(R.id.typingArea);
		        			et.setText("");
						}
					});
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
//    		progressBar.dismiss();
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
