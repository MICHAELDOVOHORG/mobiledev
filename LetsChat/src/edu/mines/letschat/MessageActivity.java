package edu.mines.letschat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import edu.mines.letschat.CustomMultiPartEntity.ProgressListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.MediaStore;

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
	View deleteView;
	int toDelete;
	int selectedRow;
	Animation animation;
	Animation animationLeft;
	boolean hasPicture = false;
	String filePath;
	String uploadName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
//		AwesomeAdapter.animate = false;

		recipientID = getIntent().getStringExtra(MainActivity.EXTRA_DEVICE_ID);
		senderID = getIntent().getStringExtra(MainActivity.EXTRA_SENDER_ID);
		userName = getIntent().getStringExtra(MainActivity.EXTRA_USER_NAME);
//		TextView tv = (TextView) findViewById(R.id.conversationWith);
//		tv.setText("Conversation with " + userName);
		conversationList = (ListView) findViewById(R.id.conversationList);
		conversationList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		conversationList.setStackFromBottom(true);
		awesome = new AwesomeAdapter(this, messages);
		conversationList.setAdapter(awesome);
		animationLeft = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_left);
		animation = AnimationUtils.loadAnimation(this,
				android.R.anim.slide_out_right);
		animationLeft.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				awesome.remove(toDelete);
				awesome.notifyDataSetChanged();
				listOfConverstations.get(toDelete).delete();
			}
		});

		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				awesome.remove(toDelete);
				awesome.notifyDataSetChanged();
				listOfConverstations.get(toDelete).delete();
			}
		});

		//	    conversationList.setOnItemClickListener(new OnItemClickListener() {
		//	        @Override
		//	        public void onItemClick(AdapterView<?> parent, View view,
		//	                int position, long id) {
		//	            Toast.makeText(getApplicationContext(),
		//	                    " " + messages.get(position).message, Toast.LENGTH_LONG).show();
		//	            view.startAnimation(animation);
		//	            selectedRow = position;
		//
		//	        }
		//
		//	    });
		conversationList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (messages.get(arg2).hasPicture()) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
//					intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					if (messages.get(arg2).isMine()) {
						intent.setDataAndType(Uri.parse("file:///" + messages.get(arg2).picture),"image/*");
					} else {
						intent.setDataAndType(Uri.parse("file:///" + Environment.getExternalStorageDirectory().getPath() + File.separator + "Talkie Talk/" + messages.get(arg2).picture),"image/*");
					}
					startActivity(intent);
				}
			}
		});
		conversationList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
				toDelete = arg2;
				builder.setTitle("Action:");
				deleteView = arg1;
				builder.setItems(new String[] {"Delete"}, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {
						if (messages.get(toDelete).isMine()) {
							deleteView.startAnimation(animationLeft);
						} else {
							deleteView.startAnimation(animation);
						}

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
//						AwesomeAdapter.animate = false;
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
		
//		et.setOnEditorActionListener(new OnEditorActionListener() {
//		    @Override
//		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//		        boolean handled = false;
//		        if (actionId == EditorInfo.IME_ACTION_SEND) {
//		        	Button button = (Button) findViewById(R.id.sendMessage);
//		    		button.setEnabled(false);
//		    		EditText et = (EditText) findViewById(R.id.typingArea);
//		    		String message = et.getText().toString();
////		    		AwesomeAdapter.animate = true;
//		    		messages.add(new Message(message, true));
//		    		AwesomeAdapter adapter = (AwesomeAdapter) conversationList.getAdapter();
//		    		adapter.notifyDataSetChanged();
//		    		et.setText("");
//		    		new SendNotification("sendNotification", recipientID, message).execute();
//		            handled = true;
//		        }
//		        return handled;
//		    }
//		});

		// Show the Up button in the action bar.
		setupActionBar();
	}

	public void populateConversationList() {
		messages.clear();
		listOfConverstations = Conversation.findWithQuery(Conversation.class, "SELECT * FROM CONVERSATION WHERE (recipient_id = \"" + recipientID + "\" AND sender_id = \"" + senderID + "\")" +
				" OR (recipient_id = \"" + senderID + "\" AND sender_id = \"" + recipientID + "\");");
//		listOfConverstations = Conversation.findWithQuery(Conversation.class, "SELECT * FROM CONVERSATION WHERE picture = \"21131957.png\";");

		Log.v(TAG, listOfConverstations.size() + "");
		for (Conversation c : listOfConverstations) {
			if (c.picture == null) {
				messages.add(new Message(c.message, c.sent, ""));
			} else {
				messages.add(new Message(c.message, c.sent, c.picture));
			}
		}
		AwesomeAdapter ad = (AwesomeAdapter) conversationList.getAdapter();
		ad.notifyDataSetChanged();
	}

	@Override
	protected void onResume() {
		registerReceiver();
		populateConversationList();
		GcmIntentService.messages.clear();
		GcmIntentService.notificationCounter = 0;
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(GcmIntentService.NOTIFICATION_ID);
//		AwesomeAdapter.animate = false;
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
		Button button = (Button) findViewById(R.id.sendMessage);
		button.setEnabled(false);
		EditText et = (EditText) findViewById(R.id.typingArea);
		String message = et.getText().toString();
//		AwesomeAdapter.animate = true;
		if (hasPicture) {
			Calendar c = Calendar.getInstance(); 
			int hour = c.get(Calendar.HOUR);
			int minute = c.get(Calendar.MINUTE);
			int second = c.get(Calendar.SECOND);
			int milli = c.get(Calendar.MILLISECOND);
			uploadName = "" + hour + minute + second + milli + ".png";
		}
		messages.add(new Message(message, true, uploadName));
		AwesomeAdapter adapter = (AwesomeAdapter) conversationList.getAdapter();
		adapter.notifyDataSetChanged();
		et.setText("");
		et.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		new SendNotification("sendNotification", recipientID, message).execute();
//		AwesomeAdapter.animate = false;
	}
	
	public void addPicture(View v) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, 100);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

		switch(requestCode) { 
		case 100:
			if(resultCode == RESULT_OK){
				Uri selectedImage = imageReturnedIntent.getData();

				String[] filePathColumn = {MediaStore.Images.Media.DATA};
				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				filePath = cursor.getString(columnIndex);
				Log.v("log","filePath is : " + filePath); 
				EditText text = (EditText)findViewById(R.id.typingArea);
				Button b = (Button) findViewById(R.id.sendMessage);
				b.setEnabled(true);
				InputStream inputStream;
				try {
					inputStream = getContentResolver().openInputStream(selectedImage);
					Drawable d = Drawable.createFromStream(inputStream, selectedImage.toString());
		            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
		            Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
		            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
					text.setCompoundDrawablesWithIntrinsicBounds(null, null, dr, null);
					hasPicture = true;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
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
//			AwesomeAdapter.animate = true;
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
			getActionBar().setTitle(userName);
		}
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.message, menu);
//		return true;
//	}

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

	class SendNotification extends Task {

		private String recipientID;
		private String message;
		ProgressDialog pd;
		long totalSize;

		public SendNotification(String function, String recipientID, String message) {
			super(function);
			this.recipientID = recipientID;
			this.message = message;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (hasPicture) {
				pd = new ProgressDialog(MessageActivity.this);
				pd.setCancelable(false);
				pd.setMessage("Sending file...");
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pd.show();
			}
		}

		@Override
		protected String doInBackground(String... arg0) {
			if (hasPicture) {
				handlePicture();
			}
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(super.getApi());
			HttpResponse response;
			String result = "";

			List<NameValuePair> params = new ArrayList<NameValuePair>(1);
			params.add(new BasicNameValuePair("func", super.getFunction()));
			params.add(new BasicNameValuePair("recipient", recipientID));
			params.add(new BasicNameValuePair("message", message));
			if (hasPicture) {
				params.add(new BasicNameValuePair("picture", uploadName));
			} else {
				params.add(new BasicNameValuePair("picture", ""));
			}
			SharedPreferences pref = getGcmPreferences(MessageActivity.this);
			String registrationId = pref.getString(MainActivity.PROPERTY_REG_ID, "empty");
			Log.v("REGID", registrationId);
			params.add(new BasicNameValuePair("sender", registrationId));
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(params));
				response = httpClient.execute(httpPost);

				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream instream = entity.getContent();
					result = convert(instream);
					instream.close();
					Log.v("RESULT", result);
					if (!hasPicture) {
						filePath = "";
					}
					Conversation convo = new Conversation(MessageActivity.this, senderID, recipientID, message, true, filePath);
					convo.save();
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
		
		@SuppressWarnings("deprecation")
		public void handlePicture() {
//			String textFile = Environment.getExternalStorageDirectory() + "/sample.txt";
			Log.v("log", "textFile: " + filePath);

			// the URL where the file will be posted
			String postReceiverUrl = "http://justacomputerscientist.com/mobile/handle_upload.php";
			Log.v("log", "postURL: " + postReceiverUrl);

			// new HttpClient
			HttpClient httpClient = new DefaultHttpClient();

			// post header
			HttpPost httpPost = new HttpPost(postReceiverUrl);

			File file = new File(filePath);
			
			FileBody fileBody = new FileBody(file, ContentType.MULTIPART_FORM_DATA, uploadName);

			CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(new ProgressListener()
			{
				@Override
				public void transferred(final long num)
				{
					pd.setMax((int) totalSize);
//					publishProgress((int) num);
					pd.setProgress((int) num);
				}
			});
			multipartContent.addPart("uploadedfile", fileBody);
			totalSize = multipartContent.getContentLength();
			httpPost.setEntity(multipartContent);
			

			// execute HTTP post request
			HttpResponse response;
			try {
				response = httpClient.execute(httpPost);
				HttpEntity resEntity = response.getEntity();

				if (resEntity != null) {

					String responseStr = EntityUtils.toString(resEntity).trim();
					Log.v("log", "Response: " +  responseStr);
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute (String file_url) {
			//    		progressBar.dismiss();
			if (hasPicture) {
				pd.dismiss();
			}
			hasPicture = false;
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
