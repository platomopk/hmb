package com.devlabspakistan.hmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Provider;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class home extends AppCompatActivity implements OnMapReadyCallback {

    ImageView menu;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    View headerview;
    private GoogleMap mMap;
    Spinner mapchildren;
    ArrayAdapter<CharSequence> childrenAdapter;
    String _picture,_name,_email;
    JSONArray childsarr;
    String lastknown="";
    Handler handler;
    Runnable runnable;
    int delay = 10000;
    boolean noerror = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        changeheadertext("Track");

        handler = new Handler();

        menu = findViewById(R.id.headermenu);
        mapchildren = findViewById(R.id.mapchildren);
        drawerLayout = findViewById(R.id.drawerlayout);
        navigationView = findViewById(R.id.nv);
        headerview = navigationView.getHeaderView(0);

        menu.setImageResource(R.drawable.menu);
        menu.setVisibility(View.VISIBLE);

//        childrenAdapter = ArrayAdapter.createFromResource(this, R.array.genderArray, android.R.layout.simple_spinner_item);
//        childrenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mapchildren.setAdapter(childrenAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.notifications:
                        startActivity(new Intent(home.this,notifications.class));
                        break;
                    case R.id.createchild:
                        startActivity(new Intent(home.this,registerchild.class));
                        break;
                    case R.id.childs:
                        startActivity(new Intent(home.this,childs.class));
                        break;
                    case R.id.profile:
                        startActivity(new Intent(home.this,parent_profile.class));
                        break;
                    case R.id.logout:
                        getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).edit().clear().commit();
                        startActivity(new Intent(home.this,login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                        break;
                    default:
                        return true;
                }
                return false;
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                if(noerror == true){
                    handler.removeCallbacks(runnable);
                    getdriverloc(selectedchildid);
                    Toast.makeText(home.this, "Runnable Called/ Location Refreshed!", Toast.LENGTH_SHORT).show();
                }
                handler.postDelayed(this,delay);

            }
        };
        handler.post(runnable);

        findViewById(R.id.selectchild).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mMap.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int i =0; i<childsarr.length();i++)
                {
                    try {
                        JSONObject obj = childsarr.getJSONObject(i);

                        if(obj.getString("fullname") == mapchildren.getSelectedItem().toString().trim()){
                            Log.e(obj.getString("fullname"), obj.getString("_id"));
                            selectedchildid = obj.getString("_id");
                            getdriverloc(selectedchildid);
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    String selectedchildid ="";


    void setupnavheader(String pic,String name,String email){
        Picasso.with(this).load(pic).transform(new CropCircleTransformation()).into((ImageView)headerview.findViewById(R.id.nav_header_picture));
        ((TextView)headerview.findViewById(R.id.nav_header_fullname)).setText(name);
        ((TextView)headerview.findViewById(R.id.nav_header_email)).setText(email);
    }

    void changeheadertext(String text){
        ((TextView)findViewById(R.id.headertext)).setText(text);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
            drawerLayout.closeDrawer(Gravity.LEFT);
        }else{
            super.onBackPressed();
        }
    }
    @Override
    protected void onPause() {
        if(handler!=null){
            handler.removeCallbacks(runnable);
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        getallchilds();
        _picture = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("picture",null);
        _name = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("name",null);
        _email = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("email",null);
        setupnavheader(_picture,_name,_email);
        if(handler!=null){
            handler.post(runnable);
        }
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.blue));
//            mMap.setTrafficEnabled(true);
//            mMap.getUiSettings().setAllGesturesEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        createmarker(new LatLng(33.686373, 73.032789));
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(33.686373, 73.032789),15)));

    }

    void createmarker(final LatLng latLng)
    {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mMap.clear();
                int height = 58;
                int width = 58;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.bus);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                LatLng islamabadGPO = latLng;
                mMap.addMarker(new MarkerOptions().position(islamabadGPO).
                        title("SchoolBus").
                        draggable(false).
                        icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(islamabadGPO));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng,15)));
            }
        });
    }

    @Override
    protected void onDestroy() {
        mMap.clear();
        if(handler!=null){
            Toast.makeText(home.this, "Runnable Ended! destroyed", Toast.LENGTH_SHORT).show();
            handler.removeCallbacks(runnable);
        }
        super.onDestroy();
    }

    private void getallchilds() {
        final ProgressDialog pd = new ProgressDialog(home.this);
        pd.setMessage("Please wait ..");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        String createdby = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("email",null);

        try {
            jsonObject.put("createdby",createdby.trim());
        } catch (JSONException e) {
            Log.e("childs", e.getLocalizedMessage() );
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/childs", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){
                                String[] arr ;

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                JSONObject toplevel = jsonArray.getJSONObject(0);
                                childsarr = toplevel.getJSONArray("childs");

                                arr = new String[childsarr.length()];

                                for(int i = 0; i < childsarr.length() ; i++){
                                    JSONObject obj = childsarr.getJSONObject(i);
                                    arr[i] = obj.getString("fullname");
                                }

                                childrenAdapter = new ArrayAdapter(home.this,android.R.layout.simple_spinner_item,arr);
                                childrenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mapchildren.setAdapter(childrenAdapter);




//                                Toast.makeText(childs.this, "Successfully registered.", Toast.LENGTH_SHORT).show();
//                                Log.e("childs", jsonObject.toString() );
                            }else{
                                Toast.makeText(home.this, "Error: "+jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        Volley.newRequestQueue(this).add(jsonObjectRequest);

        pd.show();
    }


    public void getdriverloc(String childid){
        final ProgressDialog pd = new ProgressDialog(home.this);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("childid",childid);
        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/getdriverloc", jsonObject,
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

                                lastknown = jsonObject.getJSONObject("data").getString("lastknown");
                                String [] arr = lastknown.split(",");
                                createmarker(new LatLng(Double.parseDouble(arr[0]),Double.parseDouble(arr[1])));

                                noerror = true;

                            }else{
                                Toast.makeText(home.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                                Log.e("login",jsonObject.getString("error"));

                                noerror = false;

//                                if(handler!=null){
//                                    Toast.makeText(home.this, "Runnable Ended!", Toast.LENGTH_SHORT).show();
//                                    handler.removeCallbacks(runnable);
//                                }
                            }
                        } catch (JSONException e) {
//                            if(handler!=null){
//                                Toast.makeText(home.this, "Runnable Ended! with jsonexception", Toast.LENGTH_SHORT).show();
//                                handler.removeCallbacks(runnable);
//                            }
                            noerror = false;
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplication(), "Registered Successfully. Login now.", Toast.LENGTH_LONG).show();
                        //finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                try {
                    pd.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("Message from server Err", volleyError.toString());
                noerror = false;
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(home.this).add(jsonObjectRequest);

        try {
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
