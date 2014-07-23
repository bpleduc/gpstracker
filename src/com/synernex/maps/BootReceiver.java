package com.synernex.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

	private SharedPreferences myPrefs;
	private SharedPreferences.Editor editor;
	private String sBootStart, sTracking;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		// Check if we run or not
		myPrefs = context.getSharedPreferences(
				"trackingPref", Context.MODE_PRIVATE);
		sBootStart = myPrefs.getString("BOOT", "");

		if (sBootStart.equals("") || sBootStart.equals("YES")) {
			// Yes we are running
			sTracking = "YES";
			// Modify Tracking to reflect on button
			editor = myPrefs.edit();
			editor.putString("TRACKING", sTracking);
			editor.commit();

			Intent service = new Intent(context, LocationService.class);
			context.startService(service);
			
		} else {
			// User has configured not to run
			sTracking = "NO";
			// Modify Tracking to reflect on button
			editor = myPrefs.edit();
			editor.putString("TRACKING", sTracking);
			editor.commit();
		}
		
		

	}
}
