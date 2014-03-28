/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.mines.letschat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.http.util.ByteArrayBuffer;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static int NOTIFICATION_ID;
    private NotificationManager mNotificationManager;
    public static ArrayList<String> messages = new ArrayList<String>();
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";
    public static final String OUTPUT_TEXT = "bababa";
    public static int notificationCounter = 1;

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), "0");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString(), "0");
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
//                for (int i = 0; i < 5; i++) {
//                    Log.i(TAG, "Working... " + (i + 1)
//                            + "/5 @ " + SystemClock.elapsedRealtime());
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                    }
//                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                
                String message = (String) extras.get("message");
                messages.add(message);
                
                String picture = (String) extras.get("picture");
                
                String senderID = (String) extras.get("sender");
                String recipientID = (String) extras.get("recipient");
                sendNotification(message, senderID);
                Log.v(TAG, "Received: " + message);
                
                Log.i(TAG, "Received: " + extras.toString());
                Intent resultBroadCastIntent =new Intent();
        		/*set action here*/
        		resultBroadCastIntent.setAction(edu.mines.letschat.MessageActivity.TextCapitalizeResultReceiver.ACTION_TEXT_CAPITALIZED);
        		/*set intent category as default*/
        		resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
         
        		/*add data to intent*/
        		resultBroadCastIntent.putExtra(OUTPUT_TEXT, message);
//        		Log.v(TAG, "Sender id " + senderID + " recieve id " + recipientID);
        		Log.v(TAG, picture);
        		Conversation convo = new Conversation(getApplicationContext(), senderID, recipientID, message, false, picture);
    			convo.save();
    			
    			if (!picture.isEmpty()) {
    				new DownloadImage(picture).execute();
    			}
    			
        		/*send broadcast */
        		sendBroadcast(resultBroadCastIntent);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReviever.completeWakefulIntent(intent);
    }
    
    protected PendingIntent getDeleteIntent()
    {
    	Intent resultBroadCastIntent = new Intent();
    	resultBroadCastIntent.setAction("deletion");
    	resultBroadCastIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
    	sendBroadcast(resultBroadCastIntent);
    	return PendingIntent.getBroadcast(getBaseContext(), 0, resultBroadCastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
   }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String senderID) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_NOTIFICATION_RETRIEVE, senderID);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        notificationCounter++;
        
        if (messages.size() > 1) {
        	msg = msg + "...";
        }
        
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setVibrate(new long[] { 0, 500, 250, 500, 250 })
        .setLights(Color.BLUE, 200, 200)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle("Let's Chat Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setNumber(notificationCounter)
        .setTicker(msg)
        .setContentText(msg);
        
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        // Sets a title for the Inbox style big view
        inboxStyle.setBigContentTitle("Unread messages (" + notificationCounter + "):");
        // Moves events into the big view
        ArrayList<String> temp = new ArrayList<String>();
        for (int i = 0; i < 5; ++i) {
        	if (i == messages.size()) {
        		break;
        	}
        	temp.add(messages.get(i));
        }
        Collections.reverse(temp);
        messages = temp;
        for (String s: messages) {
            inboxStyle.addLine(s);
        }
        
        mBuilder.setStyle(inboxStyle);
        
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//        AwesomeAdapter.animate = true;
    }
    
class DownloadImage extends AsyncTask<String, String, String> {
		
//		ProgressDialog pd;
		String picture;
	
		public DownloadImage(String picture) {
			this.picture = picture;
		}
		
		@Override
		protected void onPreExecute() {
//			pd = new ProgressDialog(MainActivity.this);
//			pd.setCancelable(false);
//			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//			pd.show();
		}
        
		@Override
		protected String doInBackground(String... params) {
			
			try {
		        File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Talkie Talk");
		        if(dir.exists() == false){
		             dir.mkdir();  
		        }

		        URL url = new URL("http://justacomputerscientist.com/mobile/images/" + picture);
		        File file = new File(dir,picture);

		        long startTime = System.currentTimeMillis();
		        Log.d("DownloadManager" , "download url:" +url);
		        Log.d("DownloadManager" , "download file name:" + picture);

		        URLConnection uconn = url.openConnection();
//		        pd.setMax(uconn.getContentLength());
//		        uconn.setReadTimeout(URLCoTIMEOUT_CONNECTION);
//		        uconn.setConnectTimeout(TIMEOUT_SOCKET);

		        InputStream is = uconn.getInputStream();
		        BufferedInputStream bufferinstream = new BufferedInputStream(is);

		        ByteArrayBuffer baf = new ByteArrayBuffer(5000);
		        int current = 0;
		        while((current = bufferinstream.read()) != -1){
		            baf.append((byte) current);
//		            Log.v("Download progress", "" + baf.length());
////		            pd.setProgress(pd.getMax() + current);
//		            pd.setProgress(baf.length());
		        }

		        FileOutputStream fos = new FileOutputStream( file);
		        fos.write(baf.toByteArray());
		        fos.flush();
		        fos.close();
		        Log.d("DownloadManager" , "download ready in" + ((System.currentTimeMillis() - startTime)/1000) + "sec");
		        Log.d("DownloadManager" , "download ready in" + dir.getPath());
		        int dotindex = picture.lastIndexOf('.');
		        if(dotindex>=0){
//		            fileName = "turkey.jpg".substring(0,dotindex);
		        }

		    }
		    catch(IOException e) {
		        Log.d("DownloadManager" , "Error:" + e);
		        e.printStackTrace();
		    }
			return null;
		}       
		
		@Override
		protected void onPostExecute(String result)
		{
//			pd.dismiss();
		}
    }
}
