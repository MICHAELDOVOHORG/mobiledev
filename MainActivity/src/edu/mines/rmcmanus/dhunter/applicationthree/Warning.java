/**
 * Description:  This class defines a custom dialog that will be shown when a user
 * presses the guest button on the main screen.  This dialog has a listener
 * for if the user presses the yes or no button.  If the yes button is pressed then
 * proceed with the intent, otherwise dismiss the dialog.
 * 
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class Warning extends Dialog implements
android.view.View.OnClickListener {

	public Activity c;
	public Dialog d;
	public Button yes, no;
	public boolean proceed = false;
	
	public Warning() {
		super(null);
	}

	public Warning(Activity a) {
		super(a);
		this.c = a;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.guest_warning);
		yes = (Button) findViewById(R.id.yeswarning);
		yes.setOnClickListener(this);
		no = (Button) findViewById(R.id.nowarning);
		no.setOnClickListener(this);

	}

	/**
	 * The is the listener for the button on the dialog box.  If the button is pressed
	 * then it will set a flag that the user wants to proceed.  
	 * The dialog will then be dismissed.
	 * 
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.yeswarning:
			proceed = true;
			break;
		default:
			break;
		}
		dismiss();
	}
}
