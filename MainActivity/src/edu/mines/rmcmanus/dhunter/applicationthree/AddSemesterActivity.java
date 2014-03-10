/**
 * Description: This activity is used to add a new Semester.  This activity
 * gets the id of the user from the Parse API.  The activity checks to see that all the
 * proper fields were filled out and updates the database.
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
public class AddSemesterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_semester);
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");
	}
	
	/**
	 * This function is called when the submit button is pressed on the Activity.
	 * This function checks to make sure that all the proper fields were filled out,
	 * and if they were then a string is either pulled based on the radio button selected,
	 * or the other field if the user picked that.
	 * 
	 * @param v The button that was pressed on the activity
	 */
	public void addSemester(View v) {
		EditText otherField = (EditText) findViewById(R.id.othersemester);
		EditText yearField = (EditText) findViewById(R.id.semesteryear);
		//If the user didn't put anything in for the other field it defaults to the radio buttons
		if (otherField.getText().toString().equals("")) {
			//Checks to see if the year has text
			if (yearField.getText().toString().equals("")) {
				Toast.makeText(this, getString(R.string.yearError), Toast.LENGTH_SHORT).show();
			} else {
				//Gets the value from the radio button and adds it to the proper column in the database
				RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup1);
				int selected = rg.getCheckedRadioButtonId();
				RadioButton selectedButton = (RadioButton) findViewById(selected);
				int radioId = rg.indexOfChild(selectedButton);
				RadioButton rb = (RadioButton) rg.getChildAt(radioId);
				String semesterType = rb.getText().toString();
				ParseObject semester = new ParseObject("Semester");
				semester.put("semester_type", semesterType);
				semester.put("semester_year", yearField.getText().toString());
				semester.put("user", ParseUser.getCurrentUser());
				semester.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						//Finishes the activity once the data is saved
						finish();
					}
				});
			}
		}
		else {
			if (yearField.getText().toString().equals("")) {
				Toast.makeText(this, getString(R.string.yearError), Toast.LENGTH_SHORT).show();
			} else {
				ParseObject semester = new ParseObject("Semester");
				semester.put("semester_type", otherField.getText().toString());
				semester.put("semester_year", yearField.getText().toString());
				semester.put("user", ParseUser.getCurrentUser());
				semester.saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException e) {
						//Finishes the activity once the data is saved
						finish();
					}
				});
			}
		}
	}
}
