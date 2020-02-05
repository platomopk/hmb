package com.devlabspakistan.hmb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class driverhome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverhome);
        changeheadertext("Home");
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

        ((ImageView)findViewById(R.id.headerlogout)).setVisibility(View.VISIBLE);
        ((ImageView)findViewById(R.id.headerlogout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        ((ImageView)findViewById(R.id.headerprofile)).setImageResource(R.drawable.profile);
        ((ImageView)findViewById(R.id.headerprofile)).setVisibility(View.VISIBLE);
        ((ImageView)findViewById(R.id.headerprofile)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile();
            }
        });

        findViewById(R.id.pickuphome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(driverhome.this,driverchilds.class).putExtra("type","pickuphome"));
            }
        });

        findViewById(R.id.pickupschool).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(driverhome.this,driverchilds.class).putExtra("type","pickupschool"));
            }
        });

        findViewById(R.id.dropoffhome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(driverhome.this,driverchilds.class).putExtra("type","drophome"));
            }
        });

        findViewById(R.id.dropoffschool).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(driverhome.this,driverchilds.class).putExtra("type","dropschool"));
            }
        });
    }

    void logout(){
        getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).edit().clear().commit();
        startActivity(new Intent(driverhome.this,login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
    void profile(){
        startActivity(new Intent(this,driver_profile.class));
    }
}
