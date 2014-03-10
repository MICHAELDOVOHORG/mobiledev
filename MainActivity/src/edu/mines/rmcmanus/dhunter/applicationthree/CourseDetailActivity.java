/**
 * Description: This activity is used to show all of the saved assignments relating
 * to the current users selected course. 
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import com.parse.Parse;
import com.parse.ParseUser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity representing a single Course detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link CourseListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link CourseDetailFragment}.
 */
public class CourseDetailActivity extends FragmentActivity {

	public final static String EXTRA_COURSE_ID = "edu.mines.rmcmanus.dhunter.applicationthree.COURSEADDID";
	public final static String EXTRA_SEMESTER_ID = "edu.mines.rmcmanus.dhunter.applicationthree.RESUMESEMESTERID";
	public String courseID, semesterID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_detail);
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			//			arguments.putString(CourseDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(CourseDetailFragment.ARG_ITEM_ID));
			arguments.putString(CourseListActivity.EXTRA_COURSE_ID, getIntent().getStringExtra(CourseListActivity.EXTRA_COURSE_ID));
			arguments.putString(CourseListActivity.EXTRA_SEMESTER_ID, getIntent().getStringExtra(CourseListActivity.EXTRA_SEMESTER_ID));
			courseID = getIntent().getStringExtra(CourseListActivity.EXTRA_COURSE_ID);
			semesterID = getIntent().getStringExtra(CourseListActivity.EXTRA_SEMESTER_ID);
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = sharedPref.edit();
	
			//puts the home and away team names into shared preferences
			editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
			editor.putString(getString(R.string.courseIDSharedPreference), courseID);
			editor.commit();
			CourseDetailFragment fragment = new CourseDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction().add(R.id.course_detail_container, fragment).commit();
		}

		courseID = getIntent().getStringExtra(CourseListActivity.EXTRA_COURSE_ID);
		semesterID = getIntent().getStringExtra(CourseListActivity.EXTRA_SEMESTER_ID);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the home and away team names into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.putString(getString(R.string.courseIDSharedPreference), courseID);
		editor.commit();

	}

		@Override
		public void onPause() {
			super.onPause();
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = sharedPref.edit();
	
			//puts the semester and course ids into shared preferences
			editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
			editor.putString(getString(R.string.courseIDSharedPreference), courseID);
			editor.commit();
		}

		@Override
		public void onStop() {
			super.onStop();
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = sharedPref.edit();
	
			//puts the semester and course ids into shared preferences
			editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
			editor.putString(getString(R.string.courseIDSharedPreference), courseID);
			editor.commit();
		}

		@Override
		protected void onResume() {
			super.onResume();
	
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	
			//The values are read back in from shared preferences and stored into their correct variable
			semesterID = sharedPrefs.getString(getString(R.string.semesterIDSharedPreference), "0");
			courseID = sharedPrefs.getString(getString(R.string.courseIDSharedPreference), "1");
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.assignment, menu);
		return true;
	}

	//This function does an action depending on what was selected in the context menu 
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
			//Resets the semesterID if the user uses the UP navigation
			Intent back = new Intent(this, CourseListActivity.class);
			back.putExtra(EXTRA_SEMESTER_ID, semesterID);
			NavUtils.navigateUpTo(this, back);
			return true;
		case R.id.addAssignmentContext:
			Intent intent = new Intent(this, AddAssignmentActivity.class);
			intent.putExtra(EXTRA_COURSE_ID, courseID);
			intent.putExtra(EXTRA_SEMESTER_ID, semesterID);
			startActivity(intent);
			return true;
		case R.id.setting:
			Intent setting = new Intent(this, SettingActivity.class);
			startActivity(setting);
			return true;
		case R.id.about:
			Intent about = new Intent(this, AboutActivity.class);
			startActivity(about);
			return true;
		case R.id.help:
			Intent help = new Intent(this, HelpActivity.class);
			startActivity(help);
			return true;
		case R.id.action_user:
			ParseUser.logOut();
			Intent login = new Intent(this, MainActivity.class);
			startActivity(login);
			finish();				
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
