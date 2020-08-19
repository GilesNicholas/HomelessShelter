package com.example.nicholas.myapplication.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.nicholas.myapplication.AppConfig;
import com.example.nicholas.myapplication.R;
import com.example.nicholas.myapplication.Shelter;
import com.example.nicholas.myapplication.fragments.FormFragment;
import com.example.nicholas.myapplication.fragments.ShelterFragment;
import com.example.nicholas.myapplication.util.AppController;
import com.example.nicholas.myapplication.util.SQLiteHandler;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by h on 31/10/15.
 */
public class MainActivity extends FragmentActivity{
    private final String TAG = this.getClass().getSimpleName();
    GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public Marker mMarker;
    private Spinner gender, age, numOfPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the resource of the activity
        setContentView(com.example.nicholas.myapplication.R.layout.activity_main);

        //Force the application to use portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        gender = (Spinner) findViewById(com.example.nicholas.myapplication.R.id.gender);
        ArrayAdapter<CharSequence> genderadapter = ArrayAdapter.createFromResource(this,
                com.example.nicholas.myapplication.R.array.gender_array, android.R.layout.simple_spinner_item);
        genderadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(genderadapter);

        age = (Spinner) findViewById(com.example.nicholas.myapplication.R.id.age);
        ArrayAdapter<CharSequence> ageadapter = ArrayAdapter.createFromResource(this,
                com.example.nicholas.myapplication.R.array.age_array, android.R.layout.simple_spinner_item);
        ageadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age.setAdapter(ageadapter);

        numOfPeople= (Spinner) findViewById(com.example.nicholas.myapplication.R.id.num_of_people);
        ArrayAdapter<CharSequence> numofPeopAdapter = ArrayAdapter.createFromResource(this,
                com.example.nicholas.myapplication.R.array.num_of_people, android.R.layout.simple_spinner_item);
        numofPeopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numOfPeople.setAdapter(numofPeopAdapter);
        Button c = (Button) findViewById(com.example.nicholas.myapplication.R.id.clear);
        c.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mMap.clear();
                getTextView().setText("");
                LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Location loc = locManager.getLastKnownLocation(locManager.getAllProviders().get(0));
                LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
                CameraUpdate camUpd = CameraUpdateFactory.newLatLngZoom(pos, 19);
                mMap.animateCamera(camUpd);
            }
        });

        Button b = (Button) findViewById(com.example.nicholas.myapplication.R.id.submit);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gender_send = "";
                gender_send = gender.getSelectedItem().toString();
                String age_send = "";
                age_send = age.getSelectedItem().toString();
                int age_group;
                if(age_send.equals("Under 18")) {
                    age_group = 1;
                } else if (age_send.equals("19 to 30")) {
                    age_group = 2;
                } else if (age_send.equals("31 to 45")) {
                    age_group = 3;
                } else if (age_send.equals("46 to 60")) {
                    age_group = 4;
                } else {
                    age_group = 5;
                }
                Integer num_of_people_send;

                if(numOfPeople.getSelectedItem().toString().equals("6+")) {
                    num_of_people_send = 6;
                } else {
                    num_of_people_send = Integer.parseInt(numOfPeople.getSelectedItem().toString());
                }

                LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Location loc = locManager.getLastKnownLocation(locManager.getAllProviders().get(0));

                sendLocation(loc.getLongitude(), loc.getLatitude(), gender_send, age_group, num_of_people_send);

            }
        });
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void sendLocation(final double longitude, final double latitude, final String gender, final Integer age_group, final int numOfPeople){
        final SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            List<Shelter> shelters = new ArrayList<Shelter>();

                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i <jsonArray.length(); i++){
                                JSONArray innerarr = jsonArray.getJSONArray(i);
                                String name = innerarr.getString(0);
                                double longitude = innerarr.getDouble(1);
                                double latitude = innerarr.getDouble(2);
                                double distance = innerarr.getDouble(3);
                                if (distance==0.0) {
                                    db.addShelter(name, new LatLng(latitude, longitude));
                                    Shelter shelter = new Shelter(name,longitude,latitude,6000);
                                    shelters.add(i, shelter);
                                } else {
                                    db.addShelter(name, new LatLng(latitude, longitude));
                                    Shelter shelter = new Shelter(name,longitude,latitude,distance);
                                    shelters.add(i, shelter);
                                }

                            }
                                showShelters(shelters);
                                Toast.makeText(getApplicationContext(), "New Shelters Added!", Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Shelter Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getShelters");
                params.put("lng", String.valueOf(longitude));
                params.put("lat", String.valueOf(latitude));
                params.put("gender", gender);
                params.put("age", ""+ age_group);
                params.put("numOfPeople", String.valueOf(numOfPeople));
                return params;
            }
        };
        // Adding request to request queue. Use tag_string_req to cancel request
        AppController.getInstance().addToRequestQueue(strReq, "getShelterRequest");
    }


    private void showShelters(List<Shelter> shelters){
/*
        // Create new fragment and transaction
        Fragment newFragment = new ShelterFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(android.R.id.content, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
*/
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location loc = locManager.getLastKnownLocation(locManager.getAllProviders().get(0));
        LatLng origin = new LatLng(loc.getLatitude(),loc.getLongitude());

        Collections.sort(shelters);

        LatLng closest_pos = shelters.get(0).getLatLng();
        String closest_name = shelters.get(0).getName();
        Double distance = shelters.get(0).getDistance();
        MarkerOptions closest_marker = new MarkerOptions()
                .position(closest_pos)
                .title("This is the closest shelter")
                .snippet(closest_name + " Distance: " + distance)
                .visible(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMarker = mMap.addMarker(closest_marker);
        mMarker.showInfoWindow();
        CameraUpdate camUpd = CameraUpdateFactory.newLatLngZoom(closest_pos, 19);
        mMap.animateCamera(camUpd);

        String url = getDirectionsUrl(origin, closest_pos);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);


        if(shelters.size()>0) {
            for(int i=1;i<shelters.size();i++) {
                LatLng pos = shelters.get(i).getLatLng();
                String name = shelters.get(i).getName();
                Double dist = shelters.get(i).getDistance();
                MarkerOptions marker = new MarkerOptions()
                        .position(pos)
                        .title(name + " Distance: " + dist)
                        .visible(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                mMarker = mMap.addMarker(marker);
            }
        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Set the travel mode
        String travel_mode = "walking";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&mode="+travel_mode;

        return url;
    }

    public TextView getTextView(){
        return (TextView) findViewById(R.id.textview);
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject, getTextView());
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(com.example.nicholas.myapplication.R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location loc = locManager.getLastKnownLocation(locManager.getAllProviders().get(0));
        LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
        CameraUpdate camUpd = CameraUpdateFactory.newLatLngZoom(pos, 19);
        mMap.animateCamera(camUpd);

        Log.d("POSITION", pos + " ");
/*
        MarkerOptions marker = new MarkerOptions()
                .position(pos)
                .title("Drag me to a homeless person")
                .draggable(true)
                .visible(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMarker = mMap.addMarker(marker);
        mMarker.showInfoWindow();
*/
    }

}
