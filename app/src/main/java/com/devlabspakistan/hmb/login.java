package com.devlabspakistan.hmb;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class login extends AppCompatActivity {

    AlertDialog alertDialog;
    EditText email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        String account = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("account",null);
        if(account==null){

        }else if(account.equals("parent")){
            startActivity(new Intent(login.this,home.class));
            finish();
        }else if(account.equals("driver")){
            startActivity(new Intent(login.this,driverhome.class));
            finish();
        }

        (findViewById(R.id.loginbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performlogin();
            }
        });

        (findViewById(R.id.registerparentsbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(),register.class));
            }
        });

        (findViewById(R.id.registerdriversbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(),registerdriver.class));
            }
        });

        (findViewById(R.id.forgotbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recoverdialog();
            }
        });

    }




    void recoverdialog(){
        LayoutInflater layoutInflater = getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.custom_recover_dialog,null);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(view);
        adb.setCancelable(false);
        adb.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(login.this, ((EditText) view.findViewById(R.id.recoveremail)).getText().toString(), Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
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

    private void performlogin() {
        final ProgressDialog pd = new ProgressDialog(login.this);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("email",email.getText().toString().trim());
            jsonObject.put("password",password.getText().toString().trim());
            jsonObject.put("usertoken", new helper().getfirebasetoken());

        } catch (JSONException e) {
            Log.e("login", e.getLocalizedMessage() );
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/authenticate", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){

                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("account",jsonObject.getJSONObject("data").getString("account"));
                                editor.putString("name",jsonObject.getJSONObject("data").getString("name"));
                                editor.putString("id",jsonObject.getJSONObject("data").getString("id"));
                                editor.putString("email",jsonObject.getJSONObject("data").getString("email"));
                                editor.putString("picture",jsonObject.getJSONObject("data").getString("picture"));
                                editor.commit();

                                Log.e("login",jsonObject.getJSONObject("data").getString("account") );
                                Log.e("login",jsonObject.getJSONObject("data").getString("name") );
                                Log.e("login",jsonObject.getJSONObject("data").getString("id") );
                                Log.e("login",jsonObject.getJSONObject("data").getString("email") );
                                Log.e("login",jsonObject.getJSONObject("data").getString("picture") );

                                String account = jsonObject.getJSONObject("data").getString("account");
                                 if(account.trim().equals("parent")){
                                     startActivity(new Intent(login.this,home.class));
                                     finish();
                                 }else{
                                     startActivity(new Intent(login.this,driverhome.class));
                                     finish();
                                 }



                            }else{
                                Toast.makeText(login.this, "There was an error: "+ jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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
