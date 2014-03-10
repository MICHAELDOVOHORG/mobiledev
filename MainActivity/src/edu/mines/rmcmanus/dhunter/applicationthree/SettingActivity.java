/**
 * Description: This activity is used to set up settings for the application. 
 * The user can log out of the app or change their email address if they want.
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

public class SettingActivity extends Activity {

	public EditText changeEmail;
	public Button button;
	public TextView label;
	public String email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");
		changeEmail = (EditText) findViewById(R.id.changeEdit);
		button = (Button) findViewById(R.id.submitbuttonsettings);
		label = (TextView) findViewById(R.id.changeEmailLabel);
		if (ParseUser.getCurrentUser().isAuthenticated()) {
			//Fills in the text box with the user's email address if they are signed in
			changeEmail.setText(ParseUser.getCurrentUser().getString("email"));
		} else {
			//Doesn't allow the user to update their email if they are a guest
			changeEmail.setVisibility(View.INVISIBLE);
			button.setVisibility(View.INVISIBLE);
			label.setText(getString(R.string.changeEmailGuest));
		}
	}

	/**
	 * This function is called when the user presses the submit button on the activity.
	 * This function checks to make sure that the user entered a valid email, and that
	 * there are currently no other users with that email address.  If the above is true,
	 * then the user's email is updated.
	 * 
	 * @param v The button that was pressed
	 */
	public void changeEmail(View v) {
		email = changeEmail.getText().toString();
		//Queries for all of the users where the email is equal to the users requested email
		ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
		query.whereEqualTo("email", email);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					if (objects.size() == 0) {
						//If there are not results then get the current user's data
						ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
						// Retrieve the object by id
						query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseObject>() {
							public void done(ParseObject user, ParseException e) {
								if (e == null) {
									//Updates the current user's email and username since they are the same
									user.put("username", email);
									user.put("email", email);
									user.saveInBackground(new SaveCallback() {

										@Override
										public void done(ParseException e) {
											if (e == null) {
												Toast.makeText(getBaseContext(), getString(R.string.userUpdated), Toast.LENGTH_SHORT).show();
											} else {
												Log.d("update", "Error: " + e.getMessage() + " " + e.getCode());
												//Checks if the email is an invalid format or if it is already taken.
												if (e.getCode() == ParseException.INVALID_EMAIL_ADDRESS) {
													Toast.makeText(getBaseContext(), getString(R.string.invaidemail), Toast.LENGTH_SHORT).show();
												}
												if (e.getCode() == ParseException.EMAIL_TAKEN || e.getCode() == ParseException.USERNAME_TAKEN) {
													Toast.makeText(getBaseContext(), getString(R.string.duplicateemail), Toast.LENGTH_SHORT).show();
												}
											}
										}
									});
								}
							}
						});
					}
					else {
						//Error for if the user's requested email already exists.
						Toast.makeText(getBaseContext(), getString(R.string.duplicateemail), Toast.LENGTH_SHORT).show();
					}
				}
				else {
					Log.d("update", "Error: " + e.getMessage());
				}
			}
		});
	}
	
	/**
	 * The function is called when the user presses the logout button on the activity.
	 * This signs out the current user and returns the user back to the MainActivity
	 * 
	 * @param v
	 */
	public void logout(View v) {
		if (ParseUser.getCurrentUser() != null) {
			ParseUser.logOut();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.setting, menu);
	//		return true;
	//	}

}
