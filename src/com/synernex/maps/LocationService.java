package com.synernex.maps;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;

	private PendingIntent locationPendingIntent;
	// Flag that indicates if a request is underway.
	private boolean mInProgress;

	private Boolean servicesAvailable = false;

	public class LocalBinder extends Binder {
		public LocationService getServerInstance() {
			return LocationService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mInProgress = false;

		mLocationRequest = LocationRequest.create();
		mLocationRequest
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);

		servicesAvailable = servicesConnected();

		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */

		mLocationClient = new LocationClient(this, this, this);

	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		if (!servicesAvailable || mLocationClient.isConnected() || mInProgress)
			return START_STICKY;

		setUpLocationClientIfNeeded();
		if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()
				&& !mInProgress) {

			mInProgress = true;
			mLocationClient.connect();
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Turn off the request flag
		mInProgress = false;
		if (servicesAvailable && mLocationClient != null) {

			// Destroy the current location client
			mLocationClient.removeLocationUpdates(this.locationPendingIntent);
			mLocationClient = null;
		}
		// Display the connection status
		Toast.makeText(this, "TRACKING STOPPED", Toast.LENGTH_LONG).show();
		super.onDestroy();
	}

	/*
	 * Create a new location client, using the enclosing class to handle
	 * callbacks.
	 */
	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null)
			mLocationClient = new LocationClient(this, this, this);
	}

	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			return true;
		} else {

			return false;
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {

		// Request location updates using static settings
		Intent intent = new Intent(this, LocationReceiver.class);

		locationPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 14872, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		mLocationClient.requestLocationUpdates(mLocationRequest,
				locationPendingIntent);

		// Toast.makeText(this, "Connected ", Toast.LENGTH_LONG).show();
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Turn off the request flag
		mInProgress = false;
		// Destroy the current location client
		mLocationClient = null;
		// Display the connection status
		// Toast.makeText(this, DateFormat.getDateTimeInstance().format(new
		// Date()) + ": Disconnected. Please re-connect.",
		// Toast.LENGTH_SHORT).show();

	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mInProgress = false;

		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {

			// If no resolution is available, display an error dialog
		} else {

		}
	}

	// Define the callback method that receives location updates
	@Override
	public void onLocationChanged(Location location) {
		// Toast.makeText(this, "Location Changed", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Toast.makeText(this, "Provider Disabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Toast.makeText(this, "Provider Enabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Toast.makeText(this, "Status Changed", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
