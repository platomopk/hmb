package com.devlabspakistan.hmb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class notifications extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<JSONObject> notificationList;
    LinearLayoutManager layoutManager;
    SharedPreferences sharedPref;
    static Activity activity;
    String params="";
    ArrayList<JSONObject> childList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        activity=this;
        notificationList = new ArrayList<JSONObject>();
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        changeheadertext("My Notifications");

        childList = new ArrayList<JSONObject>();
    }



    @Override
    protected void onResume() {
//        new listnotifications().execute(params);
        getallnotifications();
        super.onResume();
    }
    public static class MyViewHolderRes extends RecyclerView.ViewHolder {

        TextView id,title,created,content;
        LinearLayout mainLinear;

        public MyViewHolderRes(View itemView) {
            super(itemView);

            id = (TextView) itemView.findViewById(R.id.notificationid);
            created = (TextView) itemView.findViewById(R.id.notificationcreated);
            title = (TextView) itemView.findViewById(R.id.notificationtitle);
            content = (TextView) itemView.findViewById(R.id.notificationcontent);
            mainLinear = (LinearLayout) itemView.findViewById(R.id.mainLinear);
        }
    }
    public static class MyListAdapter extends RecyclerView.Adapter<MyViewHolderRes> {

        ArrayList<JSONObject> notificationList;

        public MyListAdapter(ArrayList<JSONObject> notificationList) {
            this.notificationList = notificationList;
        }

        @Override
        public int getItemCount() {
            if (notificationList==null){
                return 0;
            }else {
                return notificationList.size();
            }
        }

        @Override
        public MyViewHolderRes onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.custom_notification_layout, parent, false);
            MyViewHolderRes holder = new MyViewHolderRes(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolderRes holder, final int position) {
            try {
                holder.id.setText(notificationList.get(position).getString("_id"));

                holder.created.setText(notificationList.get(position).getString("created"));
                holder.title.setText(notificationList.get(position).getString("content"));
//                holder.content.setText(notificationList.get(position).getString("content"));

                holder.mainLinear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public class listnotifications extends AsyncTask<String,Void,Boolean> {
        ProgressDialog pd;
        JSONObject Jobj;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(notifications.this);
            pd.setMessage("Wait ..");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                notificationList.clear();
                HttpHandler handler = new HttpHandler();
                String jsonString = handler.makeServiceCall(new helper().baseIP+"/users/getnotifications/"+getApplication().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null));

                JSONArray jArr = new JSONArray(jsonString);

                if (jArr.length()>0){

                    for (int i = 0; i < jArr.length(); i++) {
                        Jobj = jArr.getJSONObject(i);
                        notificationList.add(Jobj);
                    }

                }else {
                    return  false;
                }

                JSONObject obj = new JSONObject();
                obj.put("id","984657512458");
                obj.put("title","Notification 1");
                obj.put("content","This is the first notification of our application");
                obj.put("created","January 20, 2018");
                notificationList.add(obj);

                JSONObject obj1 = new JSONObject();
                obj1.put("id","984657512458");
                obj1.put("title","Notification 2");
                obj1.put("content","This is the second notification of our application");
                obj1.put("created","January 21, 2018");
                notificationList.add(obj1);


            } catch (Exception e) {
                Log.e("notificationslist", "doInBackground: "+e.getMessage());
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
                    recyclerView.setAdapter(new MyListAdapter(notificationList));
                }else {
                    Toast.makeText(notifications.this, "No notifications yet", Toast.LENGTH_LONG).show();
                    recyclerView.setVisibility(View.GONE);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void changeheadertext(String text){
        ((TextView)findViewById(R.id.headertext)).setText(text);
    }


    private void getallnotifications() {
        final ProgressDialog pd = new ProgressDialog(notifications.this);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        String userid = getApplicationContext().getSharedPreferences("shared_hmb",MODE_PRIVATE).getString("id",null);

        try {
            jsonObject.put("userid",userid.trim());
        } catch (JSONException e) {
            Log.e("childs", e.getLocalizedMessage() );
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/getnotifications", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){
                                childList.clear();

                                JSONArray jsonArray = jsonObject.getJSONArray("data");

                                for(int i=0;i<jsonArray.length();i++){
                                    childList.add(jsonArray.getJSONObject(i));
                                }

//                                JSONObject toplevel = jsonArray.getJSONObject(0);
//                                JSONArray childsarr = toplevel.getJSONArray("childs");
//
//                                for(int i = 0; i < childsarr.length() ; i++){
//                                    JSONObject obj = childsarr.getJSONObject(i);
//                                    obj.put("id",obj.getString("_id"));
//                                    obj.put("fullname",obj.getString("fullname"));
//                                    obj.put("gender",obj.getString("gender"));
//                                    obj.put("cnic",obj.getString("cnic"));
//                                    obj.put("picture",obj.getString("picture"));
//                                    obj.put("verified","yes");
////                                    obj.put("created",obj.getString("created"));
//                                    obj.put("schoolname",obj.getString("schoolname"));
//                                    childList.add(obj);
//                                }
//
                                if (childList.size()>0){
                                    recyclerView.setVisibility(View.VISIBLE);
                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setLayoutManager(layoutManager);
                                    recyclerView.setAdapter(new MyListAdapter(childList));
                                }else {
                                    Toast.makeText(notifications.this, "No notifications registered yet", Toast.LENGTH_LONG).show();
                                    recyclerView.setVisibility(View.GONE);
                                }




//                                Toast.makeText(childs.this, "Successfully registered.", Toast.LENGTH_SHORT).show();
//                                Log.e("childs", jsonObject.toString() );
                            }else{
                                Toast.makeText(notifications.this, "Error: "+jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
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
}
