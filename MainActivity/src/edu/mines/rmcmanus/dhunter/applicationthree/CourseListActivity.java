/**
 * Description: This activity is used to show all of the saved courses relating
 * to the current users selected semester.
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import java.util.List;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity representing a list of Courses. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link CourseDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CourseListFragment} and the item details (if present) is a
 * {@link CourseDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link CourseListFragment.Callbacks} interface to listen for item selections.
 */
public class CourseListActivity extends FragmentActivity implements
CourseListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	public final static String EXTRA_SEMESTER_ID = "edu.mines.rmcmanus.dhunter.applicationthree.SEMESTERID";
	public final static String EXTRA_COURSE_ID = "edu.mines.rmcmanus.dhunter.applicationthree.COURSEID";
	public String semesterID;
	public String courseID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_list);
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");

		semesterID = getIntent().getStringExtra(SemesterActivity.EXTRA_SEMESTER_ID);

		if (findViewById(R.id.course_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((CourseListFragment) getSupportFragmentManager().findFragmentById(
					R.id.course_list)).setActivateOnItemClick(true);
		}

		if (!mTwoPane) {
			this.getWindow().setBackgroundDrawableResource(R.drawable.backtoschool);
		}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (mTwoPane)
			getMenuInflater().inflate(R.menu.two_pane_assignment, menu);
		else
			getMenuInflater().inflate(R.menu.course, menu);
		return true;
	}

	//Does an action based on the item selected in the Context menu
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
		case R.id.addAssignmentContext:
			if (courseID == null || courseID.equals("-1")) {
				Toast.makeText(this, getString(R.string.nullCourseError), Toast.LENGTH_SHORT).show();
			} else {
				Intent intent = new Intent(this, AddAssignmentActivity.class);
				intent.putExtra(EXTRA_COURSE_ID, courseID);
				intent.putExtra(EXTRA_SEMESTER_ID, semesterID);
				startActivity(intent);
			}
			return true;
		case R.id.addCourseContext:
			Intent addCourse = new Intent(this, AddCourseActivity.class);
			addCourse.putExtra(EXTRA_SEMESTER_ID, semesterID);
			startActivity(addCourse);
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

	/**
	 * Callback method from {@link CourseListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	/**
	 * This function was modified so that it can take the name of the course that was selected,
	 * and the semester id.  The boolean for hasCourses is to make sure that the user doesn't try
	 * to select the item from the list that is there for visual appeal.
	 * 
	 */
	@Override
	public void onItemSelected(String courseName, boolean hasCourses, final String semesterID) {
		this.semesterID = semesterID;
		if (!hasCourses) {
			if (courseName.equals(getString(R.string.allAssignments))) {
				courseID = "-1";
				if (mTwoPane) {
					// In two-pane mode, show the detail view in this activity by
					// adding or replacing the detail fragment using a
					// fragment transaction.
					Bundle arguments = new Bundle();
					//							arguments.putString(CourseDetailFragment.ARG_ITEM_ID, id);
					arguments.putString(EXTRA_COURSE_ID, courseID);
					arguments.putString(EXTRA_SEMESTER_ID, semesterID);
					CourseDetailFragment fragment = new CourseDetailFragment();
					fragment.setArguments(arguments);
					getSupportFragmentManager().beginTransaction()
					.replace(R.id.course_detail_container, fragment).commit();

				} else {
					// In single-pane mode, simply start the detail activity
					// for the selected item ID.
					startTheIntent();
				}
			}

			else {
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Course");
				query.whereEqualTo("semester", semesterID);
				query.whereEqualTo("user", ParseUser.getCurrentUser());
				query.whereEqualTo("name", courseName);
				query.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> courseList, ParseException e) {
						if (e == null) {
							Log.d("score", "Retrieved " + courseList.size() + " courses");
							for (int i = 0; i < courseList.size(); ++i) {
								courseID = courseList.get(i).getObjectId();
							}
							if (mTwoPane) {
								// In two-pane mode, show the detail view in this activity by
								// adding or replacing the detail fragment using a
								// fragment transaction.
								Bundle arguments = new Bundle();
								//							arguments.putString(CourseDetailFragment.ARG_ITEM_ID, id);
								arguments.putString(EXTRA_COURSE_ID, courseID);
								arguments.putString(EXTRA_SEMESTER_ID, semesterID);
								CourseDetailFragment fragment = new CourseDetailFragment();
								fragment.setArguments(arguments);
								getSupportFragmentManager().beginTransaction()
								.replace(R.id.course_detail_container, fragment).commit();

							} else {
								// In single-pane mode, simply start the detail activity
								// for the selected item ID.
								startTheIntent();
							}
						} else {
							Log.d("score", "Error: " + e.getMessage());
						}
					}
				});	
			}
		}
	}

	public void startTheIntent() {
		Intent detailIntent = new Intent(this, CourseDetailActivity.class);
		detailIntent.putExtra(EXTRA_COURSE_ID, courseID);
		detailIntent.putExtra(EXTRA_SEMESTER_ID, semesterID);
		//		detailIntent.putExtra(CourseDetailFragment.ARG_ITEM_ID, id);
		startActivity(detailIntent);
	}
}
