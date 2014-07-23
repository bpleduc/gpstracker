package com.synernex.maps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class ViewActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	
	// Logging
	protected static final String TAG = "MAPS";

	LocationClient mLocationClient;
	private TextView tvAddressLabel;

	private String sTrackCode;

	private URI sServerURL;
	private GoogleMap map;

	private JSONObject jObject = null;
	private JSONArray jArray = null;
	private LatLng position, oldPosition = null;
	
	private SimpleDateFormat sourceFormat, destFormat;
	String sTimeResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.track);

		tvAddressLabel = (TextView) findViewById(R.id.tvAddressLabel);

		SharedPreferences myPrefs = getSharedPreferences("trackingPref",
				Context.MODE_PRIVATE);

		sTrackCode = myPrefs.getString("CODE", "");

		if (BuildConfig.DEBUG) Log.i(TAG, "Code: " + sTrackCode);
		
		tvAddressLabel.setText(sTrackCode);

		// Create the LocationRequest object
		mLocationClient = new LocationClient(this, this, this);
		// create map object
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();

	}

	@Override
	protected void onStop() {
		// Disconnect the client.
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		//Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

		displayCurrentLocation();
	}

	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Display the error code on failure
		Toast.makeText(this,
				"Connection Failure : " + connectionResult.getErrorCode(),
				Toast.LENGTH_SHORT).show();
	}

	public void displayCurrentLocation() {
		// Demo using my phone and hard set date; This needs to change to input
		// fields

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateandTime = sdf.format(new Date());
		
		String url = "http://www.benleduc.com/gps/track.php?code=" + sTrackCode
				+ "&date="+currentDateandTime;
		
		if (BuildConfig.DEBUG) Log.i(TAG, url);

		sServerURL = URI.create(url);

		// Do RESTful and display on Map when done
		new trackREST(sServerURL).execute();

		// Get the current location's latitude & longitude
		Location currentLocation = mLocationClient.getLastLocation();

		String sLatLon = Double.toString(currentLocation.getLatitude()) + ","
				+ Double.toString(currentLocation.getLongitude());

		// Show user on Map NOT tracking codes
		LatLng position = new LatLng(currentLocation.getLatitude(),
				currentLocation.getLongitude());

		map.addMarker(new MarkerOptions().title("YOU")
				.snippet("Your Current Location").position(position));
	}

	public class trackREST extends AsyncTask<Void, Void, String> {
		private URI sServerURL;

		public trackREST(URI s) {
			sServerURL = s;
		}

		protected String getASCIIContentFromEntity(HttpEntity entity)
				throws IllegalStateException, IOException {
			InputStream in = entity.getContent();

			StringBuffer out = new StringBuffer();
			int n = 1;
			while (n > 0) {
				byte[] b = new byte[4096];
				n = in.read(b);

				if (n > 0)
					out.append(new String(b, 0, n));
			}

			return out.toString();
		}

		@Override
		protected String doInBackground(Void... params) {
			// Log.d(TAG, "doInBackground");
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(sServerURL);

			String text = null;
			try {				
				HttpResponse response = httpClient.execute(httpGet,
						localContext);

				HttpEntity entity = response.getEntity();
				text = getASCIIContentFromEntity(entity);

			} catch (Exception e) {

				return e.getLocalizedMessage();

			}

			return text;
		}

		protected void onPostExecute(String results) {
			LatLng currLatLng;
			LatLng prevLatLng;

			try {
				jObject = new JSONObject(results);
			} catch (JSONException e1) {

				e1.printStackTrace();
			}
			try {
				jArray = jObject.getJSONArray("markers");
			} catch (JSONException e) {

				e.printStackTrace();
			}
			for (int i = 0; i < jArray.length(); i++) {
				try {
					JSONObject oneObject = jArray.getJSONObject(i);

					// Pulling items from the array
					String sLat = oneObject.getString("latitude");
					String sLon = oneObject.getString("longitude");

					
					//Date convert for Server diff to User Time Zone
					sourceFormat = new SimpleDateFormat("HH:mm:ss"); 
					//Time Zone of Server
			        sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));

			        Date parsed = null;
					try {
						parsed = sourceFormat.parse(oneObject.getString("time"));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 

			        TimeZone tz = TimeZone.getDefault(); 
			        destFormat = new SimpleDateFormat("HH:mm:ss"); 
			        destFormat.setTimeZone(tz);  

			        sTimeResult = destFormat.format(parsed);

			       
			        //if (BuildConfig.DEBUG) Log.i(TAG, "Time: " + sTimeResult);
					
					// Show on Map
					position = new LatLng(Double.parseDouble(sLat),
							Double.parseDouble(sLon));
					map.addMarker(new MarkerOptions().title(sTrackCode+":"+(i+1))
							.snippet(oneObject.getString("date")+" - "+sTimeResult)
							.position(position));

					if (oldPosition != null) {
						Polyline line = map.addPolyline(new PolylineOptions()
								.add(oldPosition, position).width(5)
								.color(Color.RED));
					}

					oldPosition = position;

				} catch (JSONException e) {
					// Oops
				}
			}

			if (jArray.length() > 0) {

			} else {

				position = new LatLng(mLocationClient.getLastLocation()
						.getLatitude(), mLocationClient.getLastLocation()
						.getLongitude());
			}
			
			TextView tvNumberPositions = (TextView) findViewById(R.id.tvNumberPositions);
			tvNumberPositions.setText(""+jArray.length() );
		
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(position,
					11);
			map.animateCamera(update);

		}

	}

}