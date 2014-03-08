package edu.mines.letschat;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
	public static final String EXTRA_LOGIN = "edu.mines.letschat.EXTRA_LOGIN";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void register(View v) {
		EditText et = (EditText) findViewById(R.id.login);
		String username = et.getText().toString();
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(EXTRA_LOGIN, username);
		startActivity(intent);
	}

}
