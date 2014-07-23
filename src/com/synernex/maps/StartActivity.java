package com.synernex.maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class StartActivity extends Activity {

	private String sPhoneNumber, sBootStart, sTracking;
	private TextView tvTrackNum;
	private ToggleButton tbTrackOnOff;
	private CheckBox cbStartBoot;
	private SharedPreferences myPrefs;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get Phone Number
		TelephonyManager tMgr = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		sPhoneNumber = tMgr.getLine1Number();

		tvTrackNum = (TextView) findViewById(R.id.tvTrackingMsg);

		tbTrackOnOff = (ToggleButton) findViewById(R.id.tbTrackOnOff);
		cbStartBoot = (CheckBox) findViewById(R.id.cbStartBoot);

		tvTrackNum.setText(sPhoneNumber);

		// On Boot Initialize
		myPrefs = getSharedPreferences("trackingPref", Context.MODE_PRIVATE);
		sBootStart = myPrefs.getString("BOOT", "");
		sTracking = myPrefs.getString("TRACKING", "");

		//Log.d("MAPS", "1TRACK: " + sTracking + "BOOT: " + sBootStart);
		if (sBootStart.equals("") || sBootStart.equals("YES")) {
			sBootStart = "YES";

			cbStartBoot.setChecked(true);
			//Log.d("MAPS", "2TRACK: " + sTracking + "BOOT: " + sBootStart);
		} else {
			sBootStart = "NO";
			cbStartBoot.setChecked(false);
			//Log.d("MAPS", "3TRACK: " + sTracking + "BOOT: " + sBootStart);
		}
		//tbTrackOnOff.requestLayout();

		editor = myPrefs.edit();
		editor.putString("BOOT", sBootStart);
		editor.commit();
		//Log.d("MAPS", "4TRACK: " + sTracking + "BOOT: " + sBootStart);
		if (sTracking.equals("") || sTracking.equals("NO")) {
			sTracking = "NO";

			tbTrackOnOff.setChecked(false);
			//Log.d("MAPS", "5TRACK: " + sTracking + "BOOT: " + sBootStart);
		} else {
			sTracking = "YES";
			tbTrackOnOff.setChecked(true);
			//Log.d("MAPS", "6TRACK: " + sTracking + "BOOT: " + sBootStart);
		}
		//cbStartBoot.requestLayout();

		editor = myPrefs.edit();
		editor.putString("TRACKING", sTracking);
		editor.commit();

		startService(new Intent(this, LocationService.class));

	}

	// On button clicks
	public void process(View view) {

		final Intent intentChangeCode = new Intent(this,
				ChangeCodeActivity.class);

		if (view.getId() == R.id.btChangeCode) {
			startActivity(intentChangeCode);

		}

	}

	public void activateTracking(View view) {
		if (!tbTrackOnOff.isChecked()) {
			sTracking = "NO";
			stopService(new Intent(this, LocationService.class));

		} else {
			sTracking = "YES";
			startService(new Intent(this, LocationService.class));

		}
		editor = myPrefs.edit();
		editor.putString("TRACKING", sTracking);
		editor.commit();
	}

	public void bootStart(View view) {

		if (sBootStart == "YES") {
			cbStartBoot.setChecked(false);
			sBootStart = "NO";
		} else {
			cbStartBoot.setChecked(true);
			sBootStart = "YES";
		}
		editor = myPrefs.edit();
		editor.putString("BOOT", sBootStart);
		editor.commit();

	}

}
