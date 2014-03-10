/**
 * Description: This class is used to show a screen about our application. For this submission
 * we just have a picture that tells the purpose of the application and who made it.
 * This screen isn't very fancy but we didn't know what to put in it's place
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import com.parse.Parse;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		Parse.initialize(this, "NDFUVyb6g2ml4QQ87ArU7EqacAhNgCrHRELmxGRu", "dnUQXDcnhygT40PyGzPUP2Jw2Ch5zR11Gwq6xJZM");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

}
