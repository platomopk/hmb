package com.devlabspakistan.hmb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class trip extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 3000, FASTEST_INTERVAL = 3000; // = 3 seconds
    private Location location;
    LocationManager locationManager ;
    Handler handler;
    Runnable runnable;
    int delay = 10000;
    AlertDialog  alertDialog =null;
    LatLng madnimasjid = new LatLng(33.636028,73.068154);
    Marker lastmarker;
    boolean firsttime = true;
    LinearLayout overlay;
    TextView distance;
    ArrayList<Polyline> polylines;
    ArrayList<LatLng> latLngArrayList;
    ArrayList<Polyline> polylines_new;
    Polyline testpoly;
    String _cid ="",_id="",homelocation="", type="", fullname="";
    LatLng _homelocation = null ;
    boolean nearbyflag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        changeheadertext("Trip");

        handler = new Handler();
        overlay = findViewById(R.id.overlay);
        distance = findViewById(R.id.distance);
        polylines = new ArrayList<>();
        polylines_new = new ArrayList<>();
        latLngArrayList = new ArrayList<>();

        _cid = getIntent().getExtras().getString("_cid");
        _id = getIntent().getExtras().getString("_id");
        type = getIntent().getExtras().getString("type");
        fullname = getIntent().getExtras().getString("fullname");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // we build google api client
            googleApiClient = new GoogleApiClient.Builder(this).
                    addApi(LocationServices.API).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).build();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }else{
            showGPSDisabledAlertToUser();
            return;
        }

        onint();

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(runnable);
                if(location!=null){
                    Log.e("trip",""+ location.getLatitude()+","+location.getLongitude());
                    createmarker(new LatLng(location.getLatitude(),location.getLongitude()));
                    updatedriverloc(location.getLatitude()+","+location.getLongitude());


                    if(_homelocation !=null){
                        Log.e("run", _homelocation.latitude+","+_homelocation.longitude );

                        float[] results = new float[1];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                _homelocation.latitude, _homelocation.longitude, results);
                        distance.setText(new DecimalFormat("##.###").format(results[0]/1000) + " KM");


                        if(results[0]<300 && nearbyflag == false){
                            nearby();
                        }


                    }
                }
                handler.postDelayed(this,delay);
            }
        };
        handler.post(runnable);





        findViewById(R.id.endtrip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(trip.this);
                alertDialogBuilder.setMessage("Please mark attendance of the child.")
                        .setCancelable(false)
                        .setPositiveButton("Present",
                                new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int id){
                                        present();

                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("attendance","present");
                                        try {
                                            ad.dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        setResult(Activity.RESULT_OK,returnIntent);
                                        finish();



                                    }
                                });
                alertDialogBuilder.setNegativeButton("Absent",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                dialog.cancel();
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("attendance","absent");
                                try {
                                    ad.dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();

                            }
                        });
                alertDialogBuilder.setNeutralButton("Cancel",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                dialog.cancel();
                                Intent returnIntent = new Intent();
                                try {
                                    ad.dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                setResult(Activity.RESULT_CANCELED,returnIntent);
                                finish();
                            }
                        });

                ad = alertDialogBuilder.create();
                ad.show();
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }
    @Override
    protected void onResume() {
        if(handler!=null){
            handler.post(runnable);
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        if(handler!=null){
            handler.removeCallbacks(runnable);
        }
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        if(mMap!=null){
            mMap.clear();
        }

        if(ad != null){
            ad.dismiss();
        }

        if(handler!=null){
            handler.removeCallbacks(runnable);
        }


        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }

        super.onDestroy();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Permissions ok, we get last location
        try {
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        startLocationUpdates();
    }
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        if(location!=null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16));
            //mMap.addMarker(new MarkerOptions().position(madnimasjid));
        }


        overlay.setVisibility(View.VISIBLE);
        distance.setText("0.0 KM");

        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.blue));
//            mMap.setTrafficEnabled(true);
//            mMap.getUiSettings().setAllGesturesEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            //Log.e(TAG, location.getLatitude() + " " + location.getLongitude() );
            this.location = location;
            createmarker(new LatLng(this.location.getLatitude(),this.location.getLongitude()));

            if(_homelocation != null){
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        _homelocation.latitude, _homelocation.longitude, results);
                distance.setText(new DecimalFormat("##.###").format(results[0]/1000) + " KM");


                if(results[0]<300 && nearbyflag == false){
                    nearby();
                }


            }
        }else{
            try {
                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("Are you sure you want to go back? The application will quit if you do so.");
        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                moveTaskToBack(true);
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog = adb.create();
        alertDialog.show();
    }
    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private class current_location extends AsyncTask<Void,Void,Void> {

        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd= new ProgressDialog(trip.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.setMessage("Getting Location ..\nPlease Wait ..");
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while(location == null){
                try {
                    location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (pd.isShowing()){
                pd.dismiss();
            }
        }
    }
    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                dialog.cancel();
                                finish();
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    void createmarker(final LatLng latLng){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if(lastmarker!=null){
                    lastmarker.remove();
                }
                if(testpoly != null){
                    testpoly.remove();
                }

                latLngArrayList.add(latLng);

                if(latLngArrayList.size() >= 2){
                    testpoly = mMap.addPolyline(new PolylineOptions().addAll(latLngArrayList).width(10).color(getResources().getColor(R.color.colorAccent)));
                }

                int height = 58;
                int width = 58;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.bus);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                LatLng islamabadGPO = latLng;
                lastmarker = mMap.addMarker(new MarkerOptions().position(islamabadGPO).
                        title(""+latLng).
                        draggable(false).
                        icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(islamabadGPO));
                if(firsttime){
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng,16)));
                    firsttime = false;
                }
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));

            }
        });
    }
    void changeheadertext(String text){
        ((TextView)findViewById(R.id.headertext)).setText(text);
        ((ImageView)findViewById(R.id.headermenu)).setImageResource(R.drawable.back);
        ((ImageView)findViewById(R.id.headermenu)).setVisibility(View.VISIBLE);
        ((ImageView)findViewById(R.id.headermenu)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    AlertDialog ad =null;

    public void onint() {
        final ProgressDialog pd = new ProgressDialog(trip.this);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

         JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("_id",getIntent().getExtras().getString("_id"));
            jsonObject.put("drivername",getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("name",null));
            jsonObject.put("content", getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("name",null) + " is coming to pickup "+fullname+". Please get your child ready.");

        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/parentnotification", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){

                                homelocation = jsonObject.getJSONObject("data").getString("homelocation");


                                String [] arr = homelocation.split(",");

                                _homelocation = new LatLng(Double.parseDouble(arr[0]),Double.parseDouble(arr[1]));

                                mMap.addMarker(new MarkerOptions().position(_homelocation));

                                Toast.makeText(trip.this, homelocation, Toast.LENGTH_SHORT).show();

                            }else{
                                Toast.makeText(trip.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                Log.e("login",jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplication(), "Registered Successfully. Login now.", Toast.LENGTH_LONG).show();
                        //finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                Log.e("Message from server Err", volleyError.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(trip.this).add(jsonObjectRequest);

        pd.show();
    }
    public void nearby() {
        final ProgressDialog pd = new ProgressDialog(trip.this);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("_id",getIntent().getExtras().getString("_id"));
            jsonObject.put("drivername",getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("name",null));
            jsonObject.put("content", getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("name",null) + " is 1 minute away. Please come out with "+fullname);

        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/parentnotification", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){

                                nearbyflag = true;
                                Toast.makeText(trip.this, "Nearby reminder sent!", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(trip.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                Log.e("login",jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplication(), "Registered Successfully. Login now.", Toast.LENGTH_LONG).show();
                        //finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                Log.e("Message from server Err", volleyError.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(trip.this).add(jsonObjectRequest);

        pd.show();
    }
    public void present(){
        final ProgressDialog pd = new ProgressDialog(trip.this);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("childid",getIntent().getExtras().getString("_cid"));
            jsonObject.put("fullname",fullname);
            jsonObject.put("driverid",getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null));
        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/drivermarkpresent", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            pd.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if(jsonObject.getBoolean("success") == true){

                                Toast.makeText(trip.this, "Present Marked Successful!", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(trip.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                Log.e("login",jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplication(), "Registered Successfully. Login now.", Toast.LENGTH_LONG).show();
                        //finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                Log.e("Message from server Err", volleyError.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(trip.this).add(jsonObjectRequest);

        pd.show();
    }
    public void updatedriverloc(String latlng){
        final ProgressDialog pd = new ProgressDialog(trip.this);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("driverid",getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null));
            jsonObject.put("latlng",latlng);
        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/updatedriverloc", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            pd.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if(jsonObject.getBoolean("success") == true){

                                Toast.makeText(trip.this, "Driver loc updated", Toast.LENGTH_LONG).show();



                            }else{
                                Toast.makeText(trip.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                Log.e("login",jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplication(), "Registered Successfully. Login now.", Toast.LENGTH_LONG).show();
                        //finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                Log.e("Message from server Err", volleyError.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(trip.this).add(jsonObjectRequest);

        try {
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
