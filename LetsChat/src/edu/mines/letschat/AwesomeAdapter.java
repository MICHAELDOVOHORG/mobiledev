package edu.mines.letschat;

import java.io.File;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class AwesomeAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<Message> mMessages;
	public static boolean animate = false;

	public AwesomeAdapter(Context context, ArrayList<Message> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
	}
	
	public void remove (int position) {
		mMessages.remove(position);
	}

	@Override
	public int getCount() {
		return mMessages.size();
	}

	@Override
	public Object getItem(int position) {		
		return mMessages.get(position);
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Message message = (Message) this.getItem(position);

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		holder.message.setText(message.message);

		LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
		//check if it is a status message then remove background, and change text color.
		//		if(message.isStatusMessage())
		//		{
		//			holder.message.setBackgroundDrawable(null);
		//			lp.gravity = Gravity.LEFT;
		//			holder.message.setTextColor(R.color.textFieldColor);
		//		}
		//		else
		//		{		
		//			//Check whether message is mine to show green background and align to right
		if(message.isMine())
		{
//			new GetImage(holder).execute();
			if (message.hasPicture()) {
				SpannableString ss = new SpannableString("abc");
//	            Drawable d = mContext.getResources().getDrawable(R.drawable.test);
				File file = new File(message.picture);
				if (file.exists()) {
					Drawable d = Drawable.createFromPath(message.picture);
		            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
		            Drawable dr = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
		            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight()); 
		            ImageSpan span = new ImageSpan(dr, ImageSpan.ALIGN_BASELINE); 
		            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
		            if (message.message.length() != 0) {
		            	holder.message.append("\n");
		            }
		            holder.message.append("\n");
		            holder.message.append(ss);
				}
			}
//			ImageSpan imageSpan = new ImageSpan(mContext.getResources().getDrawable(R.drawable.test)); //Find your drawable.
//	        SpannableString spannableString = new SpannableString(holder.message.getText()); //Set text of SpannableString from TextView
//	        spannableString.setSpan(imageSpan, 0, 0, 0);
//	        holder.message.setText(spannableString);
//	        Drawable dr = mContext.getResources().getDrawable(R.drawable.test);
//			Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
			// Scale it to 50 x 50
//			Drawable d = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
//			d.setBounds(100, 100, 200, 200);
//			holder.message.setCompoundDrawables(d, null, null, null);
//			holder.message.setCompoundDrawablePadding(100);
//			holder.message.setCompoundDrawablesWithIntrinsicBounds(d, 0, 0, 0);
			holder.message.setBackgroundResource(R.drawable.speech_bubble_green);
			lp.gravity = Gravity.RIGHT;
		}
		//If not mine then it is from sender to show orange background and align to left
		else
		{
			if (message.hasPicture()) {
				SpannableString ss = new SpannableString("abc");
//	            Drawable d = mContext.getResources().getDrawable(R.drawable.test);
//				Log.v("file path", msg)
				File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Talkie Talk/" + message.picture);
				if (file.exists()) {
					Drawable d = Drawable.createFromPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "Talkie Talk/" + message.picture);
		            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
		            Drawable dr = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
		            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight()); 
		            ImageSpan span = new ImageSpan(dr, ImageSpan.ALIGN_BASELINE); 
		            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
		            if (message.message.length() != 0) {
		            	holder.message.append("\n");
		            }
		            holder.message.append("\n");
		            holder.message.append(ss);
				}
			}
			holder.message.setBackgroundResource(R.drawable.speech_bubble_orange);
			lp.gravity = Gravity.LEFT;
		}
		holder.message.setLayoutParams(lp);
		holder.message.setTextColor(R.color.textColor);	
		//		}
//		if (animate) {
//			if (position == mMessages.size() - 1 && mMessages.get(mMessages.size() - 1).isMine()) {
//				Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
//				convertView.startAnimation(animation);
//			}
//			
//			if (position == mMessages.size() - 1 && !mMessages.get(mMessages.size() - 1).isMine()) {
//				Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
//				convertView.startAnimation(animation);
//			}
//		}
		return convertView;
	}
	
//	class GetImage extends AsyncTask<String, String, String> {
//		
//		ViewHolder holder;
//		
//		public GetImage(ViewHolder holder) {
//			this.holder = holder;
//		}
//
//		@Override
//		protected String doInBackground(String... arg0) {
//			try {
//		        /* Open a new URL and get the InputStream to load data from it. */
//		        URL aURL = new URL("http://justacomputerscientist.com/mobile/turkey.jpg");
//		        URLConnection conn = aURL.openConnection();
//		        conn.connect();
//		        InputStream is = conn.getInputStream();
//		        /* Buffered is always good for a performance plus. */
//		        BufferedInputStream bis = new BufferedInputStream(is);
//		        /* Decode url-data to a bitmap. */
//		        Bitmap bm = BitmapFactory.decodeStream(bis);
//		        bis.close();
//		        is.close();
//
//		        Drawable d =new BitmapDrawable(bm);
////		        d.setId("1");
//		        Activity activity = (Activity) mContext;
//		        activity.runOnUiThread(new Runnable() {
//		            @Override
//		            public void run() {
//		            	SpannableString ss = new SpannableString("abc"); 
////		              Drawable d = mContext.getResources().getDrawable(R.drawable.test);
//		              Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
//		              Drawable dr = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
//		              dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight()); 
//		              ImageSpan span = new ImageSpan(dr, ImageSpan.ALIGN_BASELINE); 
//		              ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
////		              holder.message.append("\n");
////		              holder.message.append("\n");
////		              holder.message.append(ss);
//		              holder.message.setText("\n\n" + ss);
////		              holder.message.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null);// wherever u want the image relative to textview
//		           }
//		       });
//		        
//		        } catch (IOException e) {
//		        	Log.e("DEBUGTAG", "Remote Image Exception", e);
//		        } 
//			return null;
//		}
//		
//	}
	
	private static class ViewHolder
	{
		TextView message;
	}

	@Override
	public long getItemId(int position) {
		//Unimplemented, because we aren't using Sqlite.
		return position;
	}

}
