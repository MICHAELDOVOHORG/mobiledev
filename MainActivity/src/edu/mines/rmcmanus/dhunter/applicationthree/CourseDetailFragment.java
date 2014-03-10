/**
 * Description: This activity is used to show all of the saved assignments relating
 * to the current users selected course. 
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A fragment representing a single Course detail screen. This fragment is
 * either contained in a {@link CourseListActivity} in two-pane mode (on
 * tablets) or a {@link CourseDetailActivity} on hand sets.
 */
public class CourseDetailFragment extends Fragment {

	public ExpandableListAdapter listAdapter;
	public ExpandableListView expListView;
	public List<String> listDataHeader;
	public HashMap<String, List<String>> listDataChild;
	public ArrayList<String> assignmentObjects;
	public String courseID;
	public String semesterID;
	public ArrayList<String> assignmentList;
	public List<ParseObject> assignmentsList;
	public DeleteWarning deleteWarning;
	public int deleteIndex;
	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CourseDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		courseID = getArguments().getString(CourseListActivity.EXTRA_COURSE_ID);
		semesterID = getArguments().getString(CourseListActivity.EXTRA_SEMESTER_ID);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the semester and course ids into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.putString(getString(R.string.courseIDSharedPreference), courseID);
		editor.commit();
		deleteWarning = new DeleteWarning(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_course_detail, container, false);

		expListView = (ExpandableListView) rootView.findViewById(R.id.assignmentsListView);
		getAssignments();

		return rootView;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the home and away team names into shared preferences
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.putString(getString(R.string.courseIDSharedPreference), courseID);
		editor.commit();
	}

	@Override
	public void onStop() {
		super.onStop();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();

		//puts the home and away team names into shared preferences
		editor.putString(getString(R.string.courseIDSharedPreference), courseID);
		editor.putString(getString(R.string.semesterIDSharedPreference), semesterID);
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		//The values are read back in from shared preferences and stored into their correct variable
		semesterID = sharedPrefs.getString(getString(R.string.semesterIDSharedPreference), "0");
		courseID = sharedPrefs.getString(getString(R.string.courseIDSharedPreference), "0");	    
		getAssignments();
	}

	/**
	 * This function is called on create and on resume.  It is used to query the database
	 * based on the course that was selected in the CourseListFragment or activity.  Then
	 * the expandable list view is updated based on the details of the assignment.
	 * 
	 */
	public void getAssignments() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Assignments");
		if (!courseID.equals("-1")) {
			query.whereEqualTo("course", courseID);
		}
		query.whereEqualTo("semester", semesterID);
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.orderByAscending("dateDue");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> assignmentList, ParseException e) {
				if (e == null) {
					Log.d("score", "Retrieved " + assignmentList.size() + " assignments");
					assignmentsList = assignmentList;
					listDataChild = new HashMap<String, List<String>>();
					listDataHeader = new ArrayList<String>();
					List<String> assignmentContents;
					assignmentObjects = new ArrayList<String>();
					//Loops through the list of results and pulls each of the fields for the assignments and sets
					//it in the expandable list view
					for (int i = 0; i < assignmentList.size(); ++i) {
						assignmentContents = new ArrayList<String>();
						listDataHeader.add(assignmentList.get(i).getString("assignmentName"));
						if (isAdded()) {
							assignmentContents.add(getString(R.string.appendDueDate) + " " + assignmentList.get(i).getString("dateDue"));
							assignmentContents.add(getString(R.string.appendPointValue) + " " + assignmentList.get(i).getString("pointValue"));
							assignmentContents.add(getString(R.string.appendPriority) + " " + assignmentList.get(i).getString("assignmentPriority"));
							assignmentContents.add(getString(R.string.appendType) + " " + assignmentList.get(i).getString("assignmentType"));
							
							assignmentObjects.add(assignmentList.get(i).getObjectId());
							listDataChild.put(listDataHeader.get(i), assignmentContents);
						}
					}

					listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
					expListView.setAdapter(listAdapter);
					expListView.setOnItemLongClickListener(new OnItemLongClickListener() {

						//This function is called to delete an item from the list view.  A warning is first shown
						//to ask the user if they really want to delete the item.  If they want to delete it then
						//it is removed from the database and the list is reloaded
						@Override
						public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
							Log.d("Object ID", courseID);
							deleteIndex = position;
							deleteWarning.show();
							deleteWarning.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (deleteWarning.proceed) {
										assignmentsList.get(deleteIndex).deleteInBackground(new DeleteCallback() {
											
											@Override
											public void done(ParseException e) {
												if (e == null) {
													getAssignments();
												}
											}
										});
									}
								}
							});
							return true;
						}
					});
				} else {
					Log.d("score", "Error: " + e.getMessage());
					if (e.getCode() == ParseException.CONNECTION_FAILED) {
						Toast.makeText(getActivity().getBaseContext(), getString(R.string.connectivityError), Toast.LENGTH_LONG).show();
					}
				}
			}
		});	
	}
}
