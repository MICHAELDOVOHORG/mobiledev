/**
 * Description: This activity is used to show all of the saved semesters relating
 * to the current user. 
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

public class SemesterActivity extends Activity {
	public Intent intent;
	public boolean isGuest;
	public LinearLayout pbl;
	public String[] semesterArray;
	public List<ParseObject> semestersList;
	public ArrayList<Semester> semesterArrayList;
	public ListView semesterListView;
	public String semesterID;
	public final static String EXTRA_SEMESTER_ID = "edu.mines.rmcmanus.dhunter.applicationthree.SEMESTERID";
	public final static String EXTRA_LOGIN = "edu.mines.rmcmanus.dhunter.applicationthree.LOGIN";
	public DeleteWarning deleteWarning;
	public int deleteIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_semester);
		// Show the Up button in the action bar.
		setupActionBar();
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");

		//Gets weather the user signed in as a guest or not
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(MainActivity.EXTRA_GUEST)) {
				isGuest = extras.getBoolean(MainActivity.EXTRA_GUEST, false);
			}
		}

		intent = new Intent(this, CourseListActivity.class);
		getList();
		deleteWarning = new DeleteWarning(this);
	}

	/**
	 * This function starts the next activity based on which semester was clicked on in 
	 * the list
	 */
	public void pickCourse() {
		intent.putExtra(EXTRA_SEMESTER_ID, semesterID);
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		getList();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();
		
		//puts the home and away team names into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.commit();
	}

	/**
	 * This function is called on create and on resume.  This function queries the database
	 * for all of the semesters that are related to the current user. The information is sorted
	 * based on the year of the semester in ascending order.
	 *
	 */
	public void getList() {
		//shows the spinning wheel when the data is loading
		pbl = (LinearLayout) findViewById(R.id.progressView);
		pbl.setVisibility(View.VISIBLE);
		//Queries the semester table based on the user id
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Semester");
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		//Sorts by the year and then the name.
		query.orderByAscending("semester_year");
		query.addAscendingOrder("semester_type");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> semesterList, ParseException e) {
				if (e == null) {
					Log.d("score", "Retrieved " + semesterList.size() + " scores");
					semestersList = semesterList;
					semesterArray = new String[semesterList.size()];
					semesterArrayList = new ArrayList<Semester>();
					String semesterType;
					String semesterYear;
					String objectId;
					//If there are no results returned then a nice message is added to the list
					if (semesterList.size() == 0) {
						semesterArray = new String[1];
						semesterArray[0] = getString(R.string.noSemester);
					}
					//Populates the list adapter based on the information that is queried
					for (int i = 0; i < semesterList.size(); ++i) {
						semesterType = semesterList.get(i).getString("semester_type");
						semesterYear = semesterList.get(i).getString("semester_year");
						objectId = semesterList.get(i).getObjectId();
						semesterArrayList.add(new Semester(semesterType, semesterYear, objectId));
						semesterArray[i] = semesterType + " " + semesterYear;
					}
					ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.custom_list, semesterArray);
					semesterListView = (ListView) findViewById(R.id.semesterlist);
					semesterListView.setAdapter(arrayAdapter);
					if (semesterList.size() != 0) {
						semesterListView.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								semesterID = semesterArrayList.get(position).objectId;
								pickCourse();
							}
						});
						semesterListView.setLongClickable(true);
						semesterListView.setOnItemLongClickListener(new OnItemLongClickListener() {
							//This function is called when a user long presses an item.  It asks the user if they
							//really want to delete the data. If they do it is deleted from the database.
							@Override
							public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
								semesterID = semesterArrayList.get(position).objectId;
								deleteIndex = position;
								deleteWarning.show();
								deleteWarning.setOnDismissListener(new OnDismissListener() {
									
									@Override
									public void onDismiss(DialogInterface dialog) {
										if (deleteWarning.proceed) {
											semestersList.get(deleteIndex).deleteInBackground(new DeleteCallback() {
												@Override
												public void done(ParseException e) {
													if (e == null) {
														getList();
													}
												}
											});
										}	
									}
								});
								return true;
							}
						});
					}
					//Gets rid of the spinning wheel because all the data has been loaded
					pbl.setVisibility(View.INVISIBLE);
				} else {
					Log.d("score", "Error: " + e.getMessage());
					//Shows an error if there was no internet connection
					if (e.getCode() == ParseException.CONNECTION_FAILED) {
						Toast.makeText(getBaseContext(), getString(R.string.connectivityError), Toast.LENGTH_LONG).show();
					}
				}
			}
		});	
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//If the user is a guest don't show the logout button
		if (!isGuest) {
			getMenuInflater().inflate(R.menu.semester, menu);
		} else {
			getMenuInflater().inflate(R.menu.semester_nosignout, menu);
		}
		return true;
	}

	//Does actions based on which item was clicked in the context menu
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
			//NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.addSemesterContext:
			Intent intent = new Intent(this, AddSemesterActivity.class);
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
			if (!isGuest) {
				ParseUser.logOut();
				Intent logout = new Intent(this, MainActivity.class);
				startActivity(logout);
				finish();
			}
			else {
				Intent login = new Intent(this, MainActivity.class);
				login.putExtra(EXTRA_LOGIN, true);
				Log.d("Guest Login", "Logging in");
				startActivity(login);
				finish();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
