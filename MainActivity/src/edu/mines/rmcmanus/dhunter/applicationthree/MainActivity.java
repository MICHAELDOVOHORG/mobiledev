// This application was tested on a Nexus 4 and Nexus 7
// This application was tested on API 18 max and API 11 min
// Our application was designed to run in landscape mode only
// Our app has to have connection to the Internet to work properly. We tried to account for if the connection times out though
/**
 * Description: This class is used to either sign up for our application, or to login to our 
 * application using the Parse API.
 *
 * Point Distribution: We agree on a 50/50 point distribution
 *
 * Documentation Statement: We here by solemnly swear that we did not cheat.  We used StackOverflow
 * for small examples of how code segments should be implemented (ie. ArrayAdapters for arrays in xml) 
 * and the code from class on how to implement fragments, adapted for our needs. (Author Randy Bower).
 * Fragments were generated using Eclipse built in Activity generator.
 * 
 * This site: http://vikaskanani.wordpress.com/2011/07/27/android-focusable-edittext-inside-listview/
 * was used to figure out the custom list view with edit text boxes on the add course screen
 * 
 * This site: http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 * was used for an expandable list adapter that is used in the CourseDetailFragment
 * 
 * This site: https://parse.com/docs/android_guide was used for help with using the Parse API.
 * 
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends FragmentActivity {

	Intent intent;
	EditText first, last, email, password;
	EditText loginUsername, loginPassword, confirmPassword;
	boolean proceed = false;
	public final static String EXTRA_GUEST = "edu.mines.rmcmanus.dhunter.app3.GUEST";
	public boolean isGuest = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		first = (EditText) findViewById(R.id.firstname);
		last = (EditText) findViewById(R.id.lastname);
		email = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.passwordSignUp);
		confirmPassword = (EditText) findViewById(R.id.confirmPasswordSignUp);

		loginUsername = (EditText) findViewById(R.id.username);
		loginPassword = (EditText) findViewById(R.id.password);

		// Syncs our app up with the Parse API using an application ID and client id
		// Initialize our app to access specific folders on Parse.com
		// Application ID: NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu
		// Client ID: dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");
		ParseAnalytics.trackAppOpened(getIntent());

		// Check to see if a user is currently logged in
		// If true, moves directly to the semester activity
		ParseUser currentUser = ParseUser.getCurrentUser();
		Log.d("Intent", ""+getIntent().getBooleanExtra(SemesterActivity.EXTRA_LOGIN, false));
		if (currentUser != null && !getIntent().getBooleanExtra(SemesterActivity.EXTRA_LOGIN, false)) {
//			if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
//				isGuest = true;
//			}
			startSemesterIntent();
		}
//		ParseUser.enableAutomaticUser();
//		ParseUser.getCurrentUser().saveInBackground();
	}

	/**
	 * This function is called when the user presses the login button.  This function checks
	 * that the user put in valid login information that is in the Parse database.  If the
	 * login is successful then the next activity starts, otherwise an error is shown.
	 * 
	 * @param v The login button that is pressed
	 */
	public void login(View v) {
		String username = loginUsername.getText().toString();
		String password = loginPassword.getText().toString();
		Log.d("Parse Error", username);
		Log.d("Parse Error", password);

		//A parse API call to check if the user exists in the database and try to log them in
		ParseUser.logInInBackground(username, password, new LogInCallback() {
			public void done(ParseUser user, ParseException e) {
				if (user != null) {
					startSemesterIntent();
				} else {
					Log.d("Parse Error", e.toString());
					Log.d("Parse Error", "" + e.getCode());
					if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
						TextView invalid = (TextView) findViewById(R.id.errorlabel);
						invalid.setTextColor(Color.RED);
						invalid.setText(getString(R.string.invalidlogin));
					}
				}
			}
		});
	}

	/**
	 * This function gets called when the user presses the Guest button.  The function
	 * generates an automatic user name so that if the user decides they want to sign up
	 * for the application at a later time, the information is easily transferable.
	 * 
	 * @param v The Guest button that was pressed
	 */
	public void guest(View v) {
		// Creates an automatic user in Parse if not logged in. Once logged in, data is saved
		// to that new or existing user
		Toast.makeText(this, getString(R.string.featureNotImplemented), Toast.LENGTH_SHORT).show();
//		showWarningDialog();
	}

	/**
	 * This function is called when the user presses the sign-up button on the main screen.
	 * The function checks to see that there isn't already a user with the same email
	 * address in the database, and if there is then the user is added.  The function also
	 * checks to make sure that all the necessary edit boxes are filled out.
	 * 
	 * @param v The Signup button that was pressed
	 */
	public void signup(View v) {
		String firstName = first.getText().toString();
		String lastName = last.getText().toString();
		String userPassword = password.getText().toString();
		String confirm= confirmPassword.getText().toString();
		String userEmail = email.getText().toString();

		if (firstName.equals("") || lastName.equals("") || userPassword.equals("") || 
				confirm.equals("") || userEmail.equals("")) {
			TextView invalid = (TextView) findViewById(R.id.errorlabel);
			invalid.setTextColor(Color.RED);
			invalid.setText(getString(R.string.allrequiredfields));
			return;
		}

		if (userPassword.equals(confirm)) {
			//Creates a new Parse user if the above criteria is met
			ParseUser newUser = new ParseUser();

			newUser.setUsername(userEmail);
			newUser.put("first", firstName);
			newUser.put("last", lastName);
			newUser.setPassword(userPassword);
			newUser.setEmail(userEmail);

			//Calls a Parse API function to signup the user in the database
			newUser.signUpInBackground(new SignUpCallback() {
				public void done(ParseException e) {
					if (e == null) {
						//Log.d("Parse Message", "Let's start an intent!");
						startSemesterIntent();
					} else {
						Log.d("Parse Error", e.toString());
						Log.d("Parse Error", ""+e.getCode());
						if (e.getCode() == ParseException.INVALID_EMAIL_ADDRESS) {
							TextView invalid = (TextView) findViewById(R.id.errorlabel);
							invalid.setTextColor(Color.RED);
							invalid.setText(getString(R.string.invaidemail));
						}
						if (e.getCode() == ParseException.EMAIL_TAKEN || e.getCode() == ParseException.USERNAME_TAKEN) {
							TextView invalid = (TextView) findViewById(R.id.errorlabel);
							invalid.setTextColor(Color.RED);
							invalid.setText(getString(R.string.duplicateemail));
						}
					}
				}
			});
		} else {
			TextView invalid = (TextView) findViewById(R.id.errorlabel);
			invalid.setTextColor(Color.RED);
			invalid.setText(getString(R.string.nonmatchingpasswords));
		}

	}

	/**
	 * This function is called when the user clicks on the Guest button.  This warning
	 * is shown to tell the user that there information won't be saved if they delete
	 * the application 
	 */
//	private void showWarningDialog() {
//		final Warning warningDialog = new Warning(this);
//		warningDialog.show();
//		warningDialog.setOnDismissListener(new OnDismissListener() {
//
//			@Override
//			public void onDismiss(DialogInterface dialog) {
//				if (warningDialog.proceed) {
//					isGuest = true;
//					startSemesterIntent();
//				}
//			}
//
//		});
//	}

	/**
	 * This function is called when the user has finished either logging in or signing up
	 * successfully.  This function starts the next intent.
	 */
	public void startSemesterIntent() {
		intent = new Intent(this, SemesterActivity.class);
		intent.putExtra(EXTRA_GUEST, isGuest);
		startActivity(intent);
		finish();
	}

}
