package com.synernex.maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ChangeCodeActivity extends Activity {

	private String sTrackCode;
	private EditText etTrackCode;
	private SharedPreferences myPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.change_code);

		// Enter last tracked code or phone number
		myPrefs = getSharedPreferences("trackingPref", Context.MODE_PRIVATE);
		sTrackCode = myPrefs.getString("CODE", "");
		etTrackCode = (EditText) findViewById(R.id.etTrackCode);
		etTrackCode.setText(sTrackCode);

		// Get Phone number for tracking code
		TelephonyManager tMgr = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		String mPhoneNumber = tMgr.getLine1Number();

		if (sTrackCode == "") {
			sTrackCode = mPhoneNumber;
		}

		
		//EditText editText= (EditText) findViewById(R.id.editText);
		etTrackCode.setOnEditorActionListener(new OnEditorActionListener() { 

		                @Override
		                public boolean onEditorAction(TextView v, int actionId,
		                        KeyEvent event) {
		                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
		                            || (actionId == EditorInfo.IME_ACTION_DONE)) {
		                        
		                    	//Do your action
		                    	trackCode(etTrackCode);
		                    }
		                    return false;
		                }
		            });
		
	}

	public void trackCode(View view) {

		String sTrackCode = etTrackCode.getText().toString();

		if (sTrackCode.length() > 0) {

			SharedPreferences.Editor editor = myPrefs.edit();
			editor.putString("CODE", sTrackCode);
			editor.commit();

			final Intent intentView = new Intent(this, ViewActivity.class);

			startActivity(intentView);

		} else {
			// no code
		}

	}

}
