package com.devlabspakistan.hmb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class driverchilds extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 3000, FASTEST_INTERVAL = 3000; // = 3 seconds
    private Location location;
    LocationManager locationManager ;
    Handler handler;
    Runnable runnable;
    int delay = 10000;
    RecyclerView recyclerView;
    ArrayList<JSONObject> childList;
    LinearLayoutManager layoutManager;
    static Activity activity;
    String params="";
    static AlertDialog ad = null;
    String driverid = "";
    static String type = "";
    static Context maincontext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverchilds);
        activity=this;
        handler = new Handler();
        childList = new ArrayList<JSONObject>();
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        type = getIntent().getExtras().getString("type");

        maincontext = this;

        driverid = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null);


        changeheadertext("Children");


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // we build google api client
            googleApiClient = new GoogleApiClient.Builder(this).
                    addApi(LocationServices.API).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).build();
        }else{
            showGPSDisabledAlertToUser();
            return;
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(runnable);
                if(location!=null){
                    Log.e("trip",""+ location.getLatitude()+","+location.getLongitude());
                    updatedriverloc(location.getLatitude()+","+location.getLongitude());
                }
                handler.postDelayed(this,delay);
            }
        };
        handler.post(runnable);
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
        new listchilds().execute();
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
    public void onLocationChanged(Location location) {
        if (location != null) {
            //Log.e(TAG, location.getLatitude() + " " + location.getLongitude() );
            this.location = location;

//            Toast.makeText(this, this.location.getLatitude() +","+this.location.getLongitude(), Toast.LENGTH_SHORT).show();
        }else{
            try {
                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
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
    public static class MyViewHolderRes extends RecyclerView.ViewHolder {

        TextView id,name,address;
        ImageView image,mapimage;
        FrameLayout map;
        LinearLayout mainLinear;

        public MyViewHolderRes(View itemView) {
            super(itemView);

            id = (TextView) itemView.findViewById(R.id.childID);
            name = (TextView) itemView.findViewById(R.id.childname);
            address = (TextView) itemView.findViewById(R.id.childaddress);
            image = (ImageView) itemView.findViewById(R.id.childimage);
            mapimage = (ImageView) itemView.findViewById(R.id.childverified);
            map = (FrameLayout) itemView.findViewById(R.id.map);
            mainLinear = (LinearLayout) itemView.findViewById(R.id.notificationMainLayout);
        }
    }
    public static class MyListAdapter extends RecyclerView.Adapter<MyViewHolderRes> {

        ArrayList<JSONObject> childList;


        public MyListAdapter(ArrayList<JSONObject> childList) {
            this.childList = childList;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (childList==null){
                return 0;
            }else {
                return childList.size();
            }
        }

        @Override
        public MyViewHolderRes onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.custom_driverchild_layout, parent, false);
            MyViewHolderRes holder = new MyViewHolderRes(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolderRes holder, final int position) {
            try {
//                if(childList.get(position).getString("processed").equalsIgnoreCase("true")){
//                    holder.map.setEnabled(false);
//                    holder.map.setBackgroundColor(activity.getResources().getColor(R.color.colorAccentLight));
//                }else{
//                    holder.map.setEnabled(true);
//                }
                switch (type){
                    case "drophome":
                        holder.mapimage.setImageResource(R.drawable.attendance);
                        break;
                    case "dropschool":
                        holder.mapimage.setImageResource(R.drawable.attendance);
                        break;
                    case "pickupschool":
                        holder.mapimage.setImageResource(R.drawable.attendance);
                        break;
                }

                holder.id.setText(childList.get(position).getString("_cid"));
                holder.name.setText(childList.get(position).getString("fullname"));
                holder.address.setText(childList.get(position).getString("schoolname") + ", " +childList.get(position).getString("schoolcity"));
                Picasso.with(holder.id.getContext()).load(childList.get(position).getString("picture")).placeholder(R.drawable.loader).fit().centerCrop().into(holder.image);

                holder.map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        switch (type){
                            case "drophome":
                                try {
                                    new driverchilds().drophome(childList.get(position).getString("_cid"),childList.get(position).getString("fullname"), view.getContext());

                                    activity.startActivity(new Intent(activity,driverchilds.class).putExtra("type",type));
                                    activity.finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "dropschool":
                                try {
                                    new driverchilds().dropschool(childList.get(position).getString("_cid"),childList.get(position).getString("fullname"), view.getContext());

                                    activity.startActivity(new Intent(activity,driverchilds.class).putExtra("type",type));
                                    activity.finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "pickuphome":
                                try {
                                    Intent i = new Intent(activity, trip.class);
                                    i.putExtra("_cid", childList.get(position).getString("_cid"));
                                    i.putExtra("_id", childList.get(position).getString("_id"));
                                    i.putExtra("fullname",childList.get(position).getString("fullname"));
                                    i.putExtra("type","pickuphome");
                                    activity.startActivityForResult(i, 69);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "pickupschool":

                                try {
                                    new driverchilds().pickupschool(childList.get(position).getString("_cid"),childList.get(position).getString("fullname"), view.getContext());

                                    activity.startActivity(new Intent(activity,driverchilds.class).putExtra("type",type));
                                    activity.finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


//                                try {
//                                    Intent i = new Intent(activity, trip.class);
//                                    i.putExtra("_cid", childList.get(position).getString("_cid"));
//                                    i.putExtra("_id", childList.get(position).getString("_id"));
//                                    i.putExtra("type","pickupschool");
//                                    activity.startActivityForResult(i, 69);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                                break;
                            default:
                                Toast.makeText(view.getContext(), "False", Toast.LENGTH_SHORT).show();
                                break;
                        }


                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    public class listchilds extends AsyncTask<String,Void,Boolean> {
        ProgressDialog pd;
        JSONObject Jobj;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(driverchilds.this);
            pd.setMessage("Wait ..");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                childList.clear();



                HttpHandler handler = new HttpHandler();
                String jsonString = "";

                jsonString = handler.makeServiceCall(new helper().baseIP+"/users/driver/childs/"+driverid+"/"+type);



                JSONObject jobj = new JSONObject(jsonString);

                if(jobj.getBoolean("success") == true){
                    JSONArray jsonArray = jobj.getJSONArray("data");
                    if(jsonArray.length()>0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Jobj = jsonArray.getJSONObject(i);
                            childList.add(Jobj);
                        }
                    }else{
                        Toast.makeText(driverchilds.this, "No Childs Registered Yet", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    return false;
                }


            } catch (Exception e) {
                Log.e("childlist", "doInBackground: "+e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
            try {

                if (result){
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(new MyListAdapter(childList));
                }else {
                    Toast.makeText(getApplicationContext(), "No childs registered yet", Toast.LENGTH_LONG).show();
                    recyclerView.setVisibility(View.GONE);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 69) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("attendance");
                Toast.makeText(activity, "The child was "+ result, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(activity, "The action was cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void clickchild(final JSONObject jobj, final Context context) {
        final ProgressDialog pd = new ProgressDialog(activity);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = jobj;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/parentnotification", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){



                                String homelocation = jsonObject.getJSONObject("data").getString("homelocation");


                                try {
                                    Intent i = new Intent(activity, trip.class);
                                    i.putExtra("_cid", jobj.getString("_cid"));
                                    i.putExtra("_id", jobj.getString("_id"));
                                    i.putExtra("homelocation", homelocation);
                                    activity.startActivityForResult(i, 69);
                                    Toast.makeText(activity, "Seomthign", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                //Toast.makeText(context, homelocation, Toast.LENGTH_SHORT).show();



                            }else{
                                Toast.makeText(activity, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(activity).add(jsonObjectRequest);

        pd.show();
    }

    public void drophome(String id, String fullname, final Context context){
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("childid",id);
            jsonObject.put("fullname",fullname);
            jsonObject.put("driverid",context.getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null));
        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/driverdrophome", jsonObject,
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

                                Toast.makeText(context, "Home Drop Successful!", Toast.LENGTH_LONG).show();



                            }else{
                                Toast.makeText(context, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(context).add(jsonObjectRequest);

        try {
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dropschool(String id,String fullname, final Context context){
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("childid",id);
            jsonObject.put("fullname",fullname);
            jsonObject.put("driverid",context.getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null));
        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/driverdropschool", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            try {
                                pd.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if(jsonObject.getBoolean("success") == true){

                                Toast.makeText(context, "School Drop Successful!", Toast.LENGTH_LONG).show();



                            }else{
                                Toast.makeText(context, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(context).add(jsonObjectRequest);

        try {
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pickupschool(String id, String fullname, final Context context){
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("childid",id);
            jsonObject.put("fullname",fullname);
            jsonObject.put("driverid",context.getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null));
        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/driverpickupschool", jsonObject,
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

                                Toast.makeText(context, "School Pickup Successful!", Toast.LENGTH_LONG).show();



                            }else{
                                Toast.makeText(context, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(context).add(jsonObjectRequest);

        try {
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updatedriverloc(String latlng){
        final ProgressDialog pd = new ProgressDialog(driverchilds.this);
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

                                Toast.makeText(driverchilds.this, "Driver loc updated", Toast.LENGTH_LONG).show();



                            }else{
                                Toast.makeText(driverchilds.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(driverchilds.this).add(jsonObjectRequest);

        try {
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
