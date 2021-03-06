package com.example.guideme;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PlacesAround extends Activity implements LocationListener{

	public final static String SLAT = "com.example.guideme.SLAT";
	public final static String SLON = "com.example.guideme.SLON";
	public final static String ELAT = "com.example.guideme.ELAT";
	public final static String ELON = "com.example.guideme.ELON";
	public final static String SADD = "com.example.guideme.SADD";
	public final static String EADD = "com.example.guideme.EADD";

	private LocationManager locManager;
	private String provider;
	public Geocoder geocoder;

	private TextView editMessageName;
	private TextView editMessageDesc;
	private TextView editMessageAdd;
	private TextView editMessageDist;
	private TextView nameVal;
	private TextView descriptionVal;
	private TextView addressVal;
	private TextView distanceVal;

	private double startLatitude;
	private double startLongitude;
	private double endLatitude;
	private double endLongitude;
	private String endAddress;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_places_around);

		editMessageName = (TextView) findViewById(R.id.edit_message1);
		editMessageName.setKeyListener(null);
		editMessageName.setEnabled(false);
		editMessageDesc = (TextView) findViewById(R.id.edit_message2);
		editMessageDesc.setKeyListener(null);
		editMessageDesc.setEnabled(false);
		editMessageAdd = (TextView) findViewById(R.id.edit_message3);
		editMessageAdd.setKeyListener(null);
		editMessageAdd.setEnabled(false);
		editMessageDist = (TextView) findViewById(R.id.edit_message4);
		editMessageDist.setKeyListener(null);
		editMessageDist.setEnabled(false);

		Intent intent = getIntent();
		String lat = intent.getStringExtra(LocationActivity.LAT);
		String lon = intent.getStringExtra(LocationActivity.LON);

		startLatitude = Double.parseDouble(lat);
		startLongitude = Double.parseDouble(lon);

		Location startPoint = new Location("startPoint");
		startPoint.setLatitude(startLatitude);
		startPoint.setLongitude(startLongitude);
		Location endPoint = new Location("endPoint");

		findPlaces(startLatitude, startLongitude, startPoint, endPoint);

		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locManager.getBestProvider(criteria, false);
		Location loc = locManager.getLastKnownLocation(provider);
		geocoder = new Geocoder(this,Locale.ENGLISH);

		if(loc == null)
		{
			Toast.makeText(this, "No Provider available: " + provider, Toast.LENGTH_LONG).show();
		} 
	}

	public void findPlacesGoogleAPI(double latitude, double longitude, double radius, String types) throws Exception
	{
		//Places API Key: AIzaSyD4gr49W-CkkTAsahoL8styFOvQAors4Es
		//Test URL: https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=32.7332917,-97.1152303&radius=500&sensor=false&key=AIzaSyD4gr49W-CkkTAsahoL8styFOvQAors4Es


	}

	public void findPlaces(double startLat, double startLon, Location startPoint, Location endPoint)
	{
		String result = null;
		InputStream is = null;
		StringBuilder sb = null;
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		int id = 0;
		String name = null;
		String description = null;
		String address = null;
		String zipCode = null;	
		String[] closestPlace = new String[7];
		Double distanceBenchmark = 1609.34; //Value in meters equivalent to 1 mile

		nameVal = (TextView) findViewById(R.id.nameval);
		descriptionVal = (TextView) findViewById(R.id.descriptionval);
		addressVal = (TextView) findViewById(R.id.addressval);
		distanceVal = (TextView) findViewById(R.id.distanceval);

		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://omega.uta.edu/~cxs5518/GuideMe/Home.php");
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e)
		{
			Log.e("log_tag", "Error in http connection" + e.toString());
		}

		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line="0";

			while ((line = reader.readLine()) != null) 
			{
				sb.append(line + "\n");
			}

			is.close();
			result = sb.toString();

		}
		catch(Exception e)
		{
			Log.e("log_tag", "Error converting result "+ e.toString());
		}

		try
		{
			JSONArray jArray = new JSONArray(result);
			JSONObject jsonData = null;

			for(int i=0;i<jArray.length();i++)
			{
				jsonData = jArray.getJSONObject(i);
				id = jsonData.getInt("Id");
				name = jsonData.getString("Name");
				description = jsonData.getString("Description");
				address = jsonData.getString("Address");
				zipCode = jsonData.getString("ZipCode");
				endLatitude = Double.parseDouble(jsonData.getString("Latitude"));
				endLongitude = Double.parseDouble(jsonData.getString("Longitude"));

				endPoint.setLatitude(endLatitude);
				endPoint.setLongitude(endLongitude);

				double distanceInMeter = startPoint.distanceTo(endPoint);
				if(distanceInMeter <= distanceBenchmark)
				{
					distanceBenchmark = distanceInMeter;
					closestPlace[0] = Integer.toString(id);
					closestPlace[1] = name;
					closestPlace[2] = description;
					closestPlace[3] = address;
					closestPlace[4] = zipCode;
					closestPlace[5] = String.valueOf(endLatitude);
					closestPlace[6] = String.valueOf(endLongitude);
				}
			}

			if(closestPlace[1] != null)
			{
				nameVal.setText(closestPlace[1]);
				descriptionVal.setText(closestPlace[2]);
				addressVal.setText(closestPlace[3]);
				distanceVal.setText(clipValue((distanceBenchmark/1609.34),2) + " miles");

				endLatitude = Double.parseDouble(closestPlace[5]);
				endLongitude = Double.parseDouble(closestPlace[6]);
				endAddress = closestPlace[3];
			}
			else
			{
				Toast.makeText(this, "No places found within 1 mile.", Toast.LENGTH_LONG).show();
				endLatitude = 0.0;
				endLongitude = 0.0;
			}				
		}

		catch(JSONException e1)
		{
			Toast.makeText(getBaseContext(), "No Data Found", Toast.LENGTH_LONG).show();
		}
		catch (ParseException e1)
		{
			e1.printStackTrace();
		}
	}

	public static double clipValue(double value, int places) 
	{
		if (places < 0) throw new IllegalArgumentException();
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_places_around, menu);
		return true;
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		double lat = (double) (location.getLatitude());
		double lon = (double) (location.getLongitude());       
		startLatitude = clipValue(lat,7);
		startLongitude = clipValue(lon,7);  

		Location startPoint = new Location("startPoint");
		startPoint.setLatitude(startLatitude);
		startPoint.setLongitude(startLongitude);
		Location endPoint = new Location("endPoint");

		findPlaces(startLatitude, startLongitude, startPoint, endPoint);
	}

	@Override
	protected void onResume(){
		super.onResume();
		locManager.requestLocationUpdates(provider, 400, 1, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		locManager.removeUpdates(this);
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	public String mapURL(String startLat,String startLong, String endLat, String endLong)
	{
		StringBuilder url = new StringBuilder();

		url.append("http://maps.google.com/maps?");
		url.append("&saddr=");
		url.append(startLat);
		url.append(",");
		url.append(startLong);
		url.append("&daddr=");
		url.append(endLat);
		url.append(",");
		url.append(endLong);

		return url.toString();    	
	}

	public void RouteMap(View view) 
	{

		if((endLatitude == 0.0) || (endLongitude == 0.0))
		{
			Toast.makeText(this, "No places found within 1 mile.", Toast.LENGTH_LONG).show();
		}
		else
		{
			String url = mapURL(String.valueOf(startLatitude), String.valueOf(startLongitude),  String.valueOf(endLatitude),  String.valueOf(endLongitude));
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");			
			startActivity(intent);
		}    	
	}

	public void InviteSomeone(View view)
	{
		String name = nameVal.getText().toString();
		String shareSubject = "It would be great if you could join me! - Sent using GuideMe!";
		String shareBody = "I am planning to go to " + name + " located at " + endAddress + ".\n\nIt would be great if you could join me!" ;

		if((endLatitude == 0.0) || (endLongitude == 0.0))
		{
			Toast.makeText(this, "No place to share.", Toast.LENGTH_LONG).show();
		}
		else
		{
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, "GuideMe! Invite someone via"));
		}
	}
}



