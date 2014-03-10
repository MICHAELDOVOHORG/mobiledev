/**
 * Description: This activity is used to add a new Course to a semester.  This activity
 * gets the id of the semester from the Semester activity. The activity checks to see that all the
 * proper fields were filled out and updates the database.  This class also implements a custom
 * adapter that was mentioned in the Main Activity.  This adapter was changed to fit our needs.
 * This adapter was needed so that the user can add as many category types as they need.
 * 
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AddCourseActivity extends Activity {

	public String semesterID;
	private ListView myList;
	private MyAdapter myAdapter;
	public LinearLayout pbl;
	public boolean save = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_course);
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");
		pbl = (LinearLayout) findViewById(R.id.progressView);
		pbl.setVisibility(View.INVISIBLE);
		semesterID = getIntent().getStringExtra(CourseListActivity.EXTRA_SEMESTER_ID);
		myList = (ListView) findViewById(R.id.MyList);
		myList.setItemsCanFocus(true);
		myAdapter = new MyAdapter();
		myList.setAdapter(myAdapter);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the semester and course ids into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.commit();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the semester and course ids into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.commit();
	}

	@Override
	public void onStop() {
		super.onStop();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the semester and course ids into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		//The values are read back in from shared preferences and stored into their correct variable
		semesterID = sharedPrefs.getString(getString(R.string.semesterIDSharedPreference), "0");	    
	}

	/**
	 * This function is called when the user presses "Add More" on the activity.
	 * This calls a function of the custom adapter which adds another view to the
	 * list view of edit text fields.
	 * 
	 * @param v The button that was clicked on the activity
	 */
	public void addAnotherView(View v) {
		myAdapter.addItem();
	}

	/**
	 * This function is called when the user presses the submit button on the activity.
	 * It checks to make sure that the user put in a name for the course, and put in at
	 * least one category for the assignments. They don't have to enter a location or a
	 * time.
	 * 
	 * @param v The view clicked on the activity
	 */
	public void addCourse(View v) {
		pbl.setVisibility(View.VISIBLE);
		Button submit = (Button) findViewById(R.id.addCourseSubmitButton);
		Button more = (Button) findViewById(R.id.addMoreCategoriesButton);
		submit.setClickable(false);
		more.setClickable(false);
		EditText courseName = (EditText) findViewById(R.id.addCourseName);
		String location;
		String time;
		if (courseName.getText().toString().equals("")) {
			Toast.makeText(this, getString(R.string.courseNameError), Toast.LENGTH_SHORT).show();
		}
		else {
			location = ((EditText) findViewById(R.id.courseTime)).getText().toString();
			time = ((EditText) findViewById(R.id.courseLocation)).getText().toString();
			int countOfEmpty = 0;
			//Iterates through the elements in the adapter to see that they filled out at least one
			//of the boxes
			for (int i = 0; i < myAdapter.getCount(); ++i) {
				if (!myAdapter.getText(i).equals("")) {
					countOfEmpty++;
					break;
				}
			}
			if (countOfEmpty == 0) {
				Toast.makeText(this, getString(R.string.courseTypeError), Toast.LENGTH_SHORT).show();
			}
			else {

				//Inserts into the Course column based on the information the user entered
				final ParseObject course = new ParseObject("Course");
				course.put("name", courseName.getText().toString());
				course.put("semester", semesterID);
				course.put("time", time);
				course.put("location", location);
				course.put("user", ParseUser.getCurrentUser());
				JSONObject myObject = new JSONObject();
				JSONArray categoryList = new JSONArray();
				for (int i = 0; i < myAdapter.getCount(); ++i) {
					//Only grabs the information that isn't empty
					if (!myAdapter.getText(i).equals("")) {
						categoryList.put(myAdapter.getText(i));
					}
				}
				try {
					myObject.put("category_list", categoryList);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				course.put("course_array", myObject);
				course.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
//						pbl.setVisibility(View.INVISIBLE);
						finish();
					}
				});
			}
		}
	}

	public void checkIfDoneSaving() {
		if (save) {
			finish();
		}
	}

	/**
	 * This is a custom adapter that was found online and adapted. This adapter uses edit
	 * text boxes for a list view instead of the normal text view.  This allows the code
	 * to not be so rigid. 
	 * 
	 * @author http://vikaskanani.wordpress.com/2011/07/27/android-focusable-edittext-inside-listview/
	 * Adapted by: David Hunter
	 */
	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		public ArrayList<ListItem> myItems = new ArrayList<ListItem>();

		public MyAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int i = 0; i < 5; i++) {
				ListItem listItem = new ListItem();
				listItem.caption = "";
				myItems.add(listItem);
			}
			notifyDataSetChanged();
		}

		/**
		 * This function was added by me.  I added this so that when the user presses
		 * the button to add more views this would be added to the adapter.
		 * 
		 */
		public void addItem() {
			ListItem listItem = new ListItem();
			listItem.caption = "";
			myItems.add(listItem);
			notifyDataSetChanged();
		}

		public int getCount() {
			return myItems.size();
		}

		/**
		 * I also added this function so that I could grab the text for a view at a position.
		 * I did this so I could iterate through the size of the adapter and grab each box in
		 * the adapter to get the text.
		 * 
		 * @param position The position of the view in the list
		 * @return The text contained at that position
		 */
		public String getText(int position) {
			return myItems.get(position).caption;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.item, null);
				holder.caption = (EditText) convertView
						.findViewById(R.id.ItemCaption);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//Fill EditText with the value you have in data source
			holder.caption.setText(myItems.get(position).caption);
			holder.caption.setId(position);

			//we need to update adapter once we finish with editing
			holder.caption.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus){
						final int position = v.getId();
						final EditText Caption = (EditText) v;
						myItems.get(position).caption = Caption.getText().toString();
					}
				}
			});

			return convertView;
		}
	}

	class ViewHolder {
		EditText caption;
	}

	class ListItem {
		String caption;
	}

}
