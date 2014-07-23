package com.synernex.maps;

import java.net.URI;

import com.google.android.gms.location.LocationClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class LocationReceiver extends BroadcastReceiver {

	private String sLatLon;
	private String sPhoneNumber;
	private URI sServerURL;

	@Override
	public void onReceive(Context context, Intent intent) {

		// Get Phone number for tracking code
		TelephonyManager tMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		sPhoneNumber = tMgr.getLine1Number();

		Location location = (Location) intent.getExtras().get(
				LocationClient.KEY_LOCATION_CHANGED);

		sLatLon = Double.toString(location.getLatitude()) + ","
				+ Double.toString(location.getLongitude());

		sServerURL = URI
				.create("http://www.benleduc.com/gps/reportPos.php?code="
						+ sPhoneNumber + "&latlon=" + sLatLon);
		
		new LongRunningGetIO(sServerURL).execute();
	}

}
