package com.devlabspakistan.hmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Executors;

public class registerdriver extends AppCompatActivity  {

    ImageView image;
    Button register;
    ImageButton selectImg,galleryImg;
    EditText fullname,cnic,address,phone,email,password,carregistration;
    Spinner gender,capacity,city,schoolname;
    ArrayAdapter<CharSequence> genderAdapter,capacityAdapter,cityadapter;
    ArrayAdapter<String> schoolnameadapter;
    String encoded_string, image_name;
    String mCurrentPhotoPath;
    Bitmap bitmap;
    File file;
    Uri file_uri;
    TextView schoolslist;
    HashSet<String> setofschools;
    static final String TAG="registerdriver";

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_SELECT_PHOTO = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerdriver);
        changeheadertext("Register Driver");

        fullname = (EditText) findViewById(R.id.fullname);
        carregistration = (EditText) findViewById(R.id.carregid);
        address = (EditText) findViewById(R.id.address);
        gender = (Spinner) findViewById(R.id.gender);
        cnic = (EditText) findViewById(R.id.cnic);
        capacity = (Spinner) findViewById(R.id.capacity);
        city = (Spinner) findViewById(R.id.city);
        schoolname = (Spinner) findViewById(R.id.schoolname);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        schoolslist = (TextView) findViewById(R.id.listofschools);
        schoolslist.setText("");

        image = (ImageView) findViewById(R.id.image);

        register = (Button) findViewById(R.id.register);
        selectImg = (ImageButton) findViewById(R.id.selectImg);
        galleryImg = (ImageButton) findViewById(R.id.galleryImg);

        selectImg.requestFocus();

        genderAdapter = ArrayAdapter.createFromResource(this, R.array.genderArray, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(genderAdapter);

        capacityAdapter = ArrayAdapter.createFromResource(this, R.array.capacityArray, android.R.layout.simple_spinner_item);
        capacityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capacity.setAdapter(capacityAdapter);

        cityadapter = ArrayAdapter.createFromResource(this, R.array.cityArray, android.R.layout.simple_spinner_item);
        cityadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city.setAdapter(cityadapter);

        encoded_string="";image_name="";

        setofschools = new HashSet<>();

        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });



        galleryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_SELECT_PHOTO);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (encoded_string.length() > 0) {
                    if (fullname.getText().toString().trim().equals("") || cnic.getText().toString().trim().equals("") || address.getText().toString().trim().equals("") || phone.getText().toString().trim().equals("") || email.getText().toString().trim().equals("") || password.getText().toString().trim().equals("") ) {
                        Toast.makeText(registerdriver.this, "There are some empty fields here", Toast.LENGTH_SHORT).show();
                    } else {
                        try{
                            registerdriver();
                        }catch (Exception e){
                            Log.e(TAG, e.getMessage() );
                        }
                    }
                } else {
                    Toast.makeText(registerdriver.this, "Please Select Profile Image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getschools();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        schoolname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem().toString() != "") {
                    if (setofschools.contains(adapterView.getSelectedItem().toString())) {
                        setofschools.remove(adapterView.getSelectedItem().toString());
                    } else {
                        setofschools.add(adapterView.getSelectedItem().toString());
                    }
                    schoolslist.setText(setofschools.toString());
                    schoolname.setSelection(0);

                    Log.e(TAG, "onItemSelected: "+setofschools.toArray(new String[setofschools.size()]) );
                }


//
//                if(adapterView.getSelectedItem().toString() == ""){
//
//                }else{
//                    if(schoolslist.getText().toString().trim() == ""){
//                        setofschools.add(adapterView.getSelectedItem().toString());
//
//                        schoolslist.setText(adapterView.getSelectedItem().toString().trim());
//                    }else {
//
//                        if (schoolslist.getText().toString().contains(adapterView.getSelectedItem().toString())) {
//                            schoolslist.setText(schoolslist.getText().toString().replaceAll("," + adapterView.getSelectedItem().toString(), ""));
//                            schoolslist.setText(schoolslist.getText().toString().replaceAll(adapterView.getSelectedItem().toString(), ""));
//                        } else {
//                            if(schoolslist.getText().toString().trim() == ""){
//                                schoolslist.setText(adapterView.getSelectedItem().toString().trim());
//                            }else{
//                                schoolslist.setText(schoolslist.getText().toString().trim() + "," + adapterView.getSelectedItem().toString().trim());
//                            }
//
//                        }
//
//                    }
//                    schoolname.setSelection(0);
//
//                    String [] arr = schoolslist.getText().toString().trim().split(",");
//
//
//
//                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();

//        if (googleApiClient != null) {
//            googleApiClient.connect();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
//        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
//            googleApiClient.disconnect();
//        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                //Picasso.with(register.this).load(Uri.fromFile(new File(mCurrentPhotoPath))).fit().centerCrop().into(image);
                new encode_image().execute();
                Log.e(TAG,"Before " + new File(mCurrentPhotoPath).length()+" B" );
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() );
            }
        }
        if (requestCode==REQUEST_SELECT_PHOTO && resultCode==RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            file= new File(picturePath);
            image_name=file.getName();
            file_uri = Uri.fromFile(file);
            mCurrentPhotoPath = file.getAbsolutePath();

            new encode_image().execute();
        }
    }

    private void registerdriver() {
        final ProgressDialog pd = new ProgressDialog(registerdriver.this);
        pd.setMessage("Registering");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();

        if(setofschools.size()==0){
            Toast.makeText(this, "Please select atleast one school", Toast.LENGTH_SHORT).show();
            return;
        }

        String []  arr = new String[setofschools.size()];

        int i = 0;
        for (String s: setofschools)
            arr[i++] = s;

        try {
            jsonObject.put("picture",encoded_string.trim());
            jsonObject.put("picturename",image_name.trim().replaceAll(" ","%20"));
            jsonObject.put("fullname",fullname.getText().toString().trim());
            jsonObject.put("gender",gender.getSelectedItem().toString());
            jsonObject.put("cnic",cnic.getText().toString().trim());
            jsonObject.put("phone",phone.getText().toString().trim());
            jsonObject.put("address",address.getText().toString().trim());
            jsonObject.put("email",email.getText().toString().trim());
            jsonObject.put("password",password.getText().toString().trim());
            jsonObject.put("usertoken", new helper().getfirebasetoken());
            jsonObject.put("driverschoolname", schoolname.getSelectedItem().toString());
            jsonObject.put("carregistration", carregistration.getText().toString().trim());
            jsonObject.put("seatingcapacity", capacity.getSelectedItem().toString().trim());
            jsonObject.put("schools", TextUtils.join(",",setofschools));

        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage() );
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/registerdriver", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){
                                Toast.makeText(registerdriver.this, "Driver Registered. You can now log in.", Toast.LENGTH_LONG).show();

                                finish();
                            }else{
                                Toast.makeText(registerdriver.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(this).add(jsonObjectRequest);

        pd.show();
    }





    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagefile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = imagefile.getAbsolutePath();
        image_name =imageFileName+".jpg";

        return imagefile;
    }
    private class encode_image extends AsyncTask<Void,Void,Void> {

        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd= new ProgressDialog(registerdriver.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.setMessage("Saving Image ..\nPlease Wait ..");
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                try {
                    BitmapFactory.decodeStream(new FileInputStream(new File(mCurrentPhotoPath)), null, o);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // The new size we want to scale to
                final int REQUIRED_SIZE=350;

                // Find the correct scale value. It should be the power of 2.
                int scale = 2;
                while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                        o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                    scale *= 2;
                }

                // Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                try {
                    Bitmap m = BitmapFactory.decodeStream(new FileInputStream(new File(mCurrentPhotoPath)), null, o2);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    m.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    byte[] array = stream.toByteArray();
                    encoded_string = Base64.encodeToString(array,Base64.DEFAULT);
                    Log.e(TAG,"After " + BitmapCompat.getAllocationByteCount(m)+" B" );

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(encoded_string);
                    outputStreamWriter.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (pd.isShowing()){
                pd.dismiss();
            }
            Picasso.with(registerdriver.this).load(Uri.fromFile(new File(mCurrentPhotoPath))).fit().centerCrop().into(image);
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
    private void getschools() {
        final ProgressDialog pd = new ProgressDialog(registerdriver.this);
        pd.setMessage("Please wait getting schools .. ");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("city",city.getSelectedItem().toString());
        } catch (JSONException e) {
            Log.e("getschoolregchild", e.getLocalizedMessage() );
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/schools/getschool", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){

                                Log.e(TAG, jsonObject.getJSONArray("data").toString());

                                ArrayList<String> schools = new ArrayList<>();
                                schools.add("");
                                JSONArray jarr = jsonObject.getJSONArray("data");

                                for (int i=0; i<jarr.length();i++){
                                    JSONObject jobj = jarr.getJSONObject(i);
                                    schools.add(jobj.getString("name"));
                                }

                                schoolnameadapter = new ArrayAdapter<String>(registerdriver.this,android.R.layout.simple_spinner_dropdown_item,schools);
                                schoolnameadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                schoolname.setAdapter(schoolnameadapter);


                            }else{

                                schoolname.setAdapter(null);

                                Toast.makeText(registerdriver.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(this).add(jsonObjectRequest);

        pd.show();
    }

}
