/**
 * Description: This activity is used to show all of the saved courses relating
 * to the current users selected semester. The functionality for this submission has not 
 * yet been implemented. The list is filled with dummy data.  This is the left fragment on
 * a tablet
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


/**
 * A list fragment representing a list of Courses. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link CourseDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class CourseListFragment extends ListFragment {

	public String[] courseArray;
	//	public ArrayList<Semester> courseArrayList;
	public ArrayList<Course> courseArrayList;
	public List<ParseObject> coursesList;
	public ListView courseListView;
	public String semesterID, courseID;
	public boolean noCourses = false;
	public DeleteWarning deleteWarning;
	public int deleteIndex;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String courseName, boolean hasCourses, String semesterID);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String courseName, boolean hasCourses, String semesterID) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CourseListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//		Toast.makeText(getActivity(), getActivity().getIntent().getStringExtra(SemesterActivity.EXTRA_SEMESTER_ID), Toast.LENGTH_SHORT).show();
		//		String[] courses = new String[] {"Course 1", "Course 2", "Course 3"};
		//		setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, android.R.id.text1, courses));
		semesterID = getActivity().getIntent().getStringExtra(SemesterActivity.EXTRA_SEMESTER_ID);
		getList();
		deleteWarning = new DeleteWarning(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the home and away team names into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.commit();
	}

	@Override
	public void onStop() {
		super.onStop();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the home and away team names into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		//The values are read back in from shared preferences and stored into their correct variable
		semesterID = sharedPrefs.getString(getString(R.string.semesterIDSharedPreference), "0");	    
		getList();
	}

	/**
	 * This function is called on create and on resume.  This function queries the database
	 * for all the courses related to the selected semester and the current user's id.
	 * Then an adapter is set based on all of the information that is returned.
	 * 
	 */
	public void getList() {
		noCourses = false;
		if (semesterID == null) {
			semesterID = getActivity().getIntent().getStringExtra(CourseDetailActivity.EXTRA_SEMESTER_ID);
		}
		//Queries the database for the courses
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Course");
		query.whereEqualTo("semester", semesterID);
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> courseList, ParseException e) {
				if (e == null) {
					Log.d("score", "Retrieved " + courseList.size() + " courses");
					courseArray = new String[courseList.size() + 1];
					courseArrayList = new ArrayList<Course>();
					coursesList = courseList;
					String courseName, objectID;
					//If there were no results returned, then a nice message is added to the list
					if (isAdded()) {
						if (courseList.size() == 0) {
							courseArray = new String[1];
							courseArray[0] = getString(R.string.noCourse);
							noCourses = true;
						}
						if (courseList.size() > 0) {
							courseArray[0] = getString(R.string.allAssignments);
							courseArrayList.add(new Course("", ""));
							for (int i = 1; i < courseList.size() + 1; ++i) {
								courseName = courseList.get(i - 1).getString("name");
								objectID = courseList.get(i - 1).getObjectId();
								courseArrayList.add(new Course(courseName, objectID));
								courseArray[i] = courseName;
							}
						}
						//					getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
						ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_list2, courseArray);
						setListAdapter(arrayAdapter);
						//					getListView().setSelector(android.R.color.black);
						if (courseList.size() > 0) {
							//This function is called when a user long presses an item.  It shows a warning asking if the user
							//really wants to delete the data, and if they do then the data is deleted from the database
							getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

								@Override
								public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
									if (position != 0) {
										courseID = courseArrayList.get(position).objectID;
										Log.d("Object ID", courseID);
										deleteIndex = position;
										deleteWarning.show();
										deleteWarning.setOnDismissListener(new OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (deleteWarning.proceed) {
													coursesList.get(deleteIndex - 1).deleteInBackground(new DeleteCallback() {

														@Override
														public void done(ParseException e) {
															if (e == null) {
																ParseQuery<ParseObject> query = ParseQuery.getQuery("Assignments");
																query.whereEqualTo("course", courseID);
																query.whereEqualTo("user", ParseUser.getCurrentUser());
																query.findInBackground(new FindCallback<ParseObject>() {

																	@Override
																	public void done(
																			List<ParseObject> objects,
																			ParseException e) {
																		for (int i = 0; i < objects.size(); ++i) {
																			objects.get(i).deleteInBackground();
																		}

																	}

																});
																getList();
															}
														}
													});			
												}
											}

										});
									}
									//							ParseObject.createWithoutData("Course", courseID).deleteInBackground(new DeleteCallback() {
									//								
									//								@Override
									//								public void done(ParseException e) {
									//									if (e == null) {
									//										getList();
									//									}
									//								}
									//							});
									return true;
								}
							});
						}
					}
				} else {
					Log.d("score", "Error: " + e.getMessage());
					Log.d("score", e.getCode() + "");
					if (e.getCode() == ParseException.CONNECTION_FAILED) {
						Toast.makeText(getActivity().getBaseContext(), getString(R.string.connectivityError), Toast.LENGTH_LONG).show();
					}
				}
			}
		});	
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		String courseName = listView.getItemAtPosition(position).toString();
		mCallbacks.onItemSelected(courseName, noCourses, semesterID);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		//		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
		getListView().setSelector(android.R.color.black);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
