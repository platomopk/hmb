package com.devlabspakistan.hmb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
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
import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class childs extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<JSONObject> childList;
    LinearLayoutManager layoutManager;
    static Activity activity;
    String params="";
    static AlertDialog ad = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_childs);
        activity=this;
        childList = new ArrayList<JSONObject>();
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        changeheadertext("My Childs");

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(childs.this,registerchild.class));
            }
        });
    }



    @Override
    protected void onResume() {
//        new listchilds().execute(params);
        getallchilds();
        super.onResume();
    }
    public static class MyViewHolderRes extends RecyclerView.ViewHolder {

        TextView id,name,created,cnic,gender;
        ImageView image,verified,delete;
        LinearLayout mainLinear;

        public MyViewHolderRes(View itemView) {
            super(itemView);

            id = (TextView) itemView.findViewById(R.id.childID);
            name = (TextView) itemView.findViewById(R.id.childname);
            created = (TextView) itemView.findViewById(R.id.childcreated);
            cnic = (TextView) itemView.findViewById(R.id.childcnic);
            gender = (TextView) itemView.findViewById(R.id.childgender);
            image = (ImageView) itemView.findViewById(R.id.childimage);
            verified = (ImageView) itemView.findViewById(R.id.childverified);
            delete = (ImageView) itemView.findViewById(R.id.childdelete);
            mainLinear = (LinearLayout) itemView.findViewById(R.id.notificationMainLayout);
        }
    }
    public static class MyListAdapter extends RecyclerView.Adapter<MyViewHolderRes> {

        ArrayList<JSONObject> childList;

        public MyListAdapter(ArrayList<JSONObject> childList) {
            this.childList = childList;
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
            View view = inflater.inflate(R.layout.custom_child_layout, parent, false);
            MyViewHolderRes holder = new MyViewHolderRes(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolderRes holder, final int position) {
            try {
                if (childList.get(position).getString("verified").equalsIgnoreCase("no")){
                    holder.verified.setImageResource(R.drawable.tick);
                }else{
                    holder.verified.setImageResource(R.drawable.dtick);
                }
                holder.id.setText(childList.get(position).getString("id"));
                holder.name.setText(childList.get(position).getString("fullname"));
//                holder.created.setText(childList.get(position).getString("created").substring(0,childList.get(position).getString("created").indexOf("T")));
                holder.created.setText(childList.get(position).getString("schoolname"));
                holder.cnic.setText(childList.get(position).getString("cnic"));
                holder.gender.setText(childList.get(position).getString("gender"));
                Picasso.with(holder.id.getContext()).load(childList.get(position).getString("picture")).placeholder(R.drawable.loader).fit().centerCrop().into(holder.image);
                holder.mainLinear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(view.getContext(),child_profile.class);
                        i.putExtra("object",childList.get(position).toString());
                        view.getContext().startActivity(i);
                    }
                });
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletechilddialog(holder.id.getText().toString(),position);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void deletechilddialog(final String profileid, final int position){
            AlertDialog.Builder adb = new AlertDialog.Builder(activity);
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete this profile? \n\nProfile ID:\n"+profileid);
            adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ad.dismiss();
                    final ProgressDialog pd = new ProgressDialog(activity);
                    pd.setMessage("Please wait");
                    pd.setIndeterminate(true);
                    pd.setCancelable(false);

                    JSONObject jsonObject = new JSONObject();
                    String createdby = activity.getApplicationContext().getSharedPreferences("shared_hmb", MODE_PRIVATE).getString("email",null);
                    try {
                        jsonObject.put("childid",profileid);
                        jsonObject.put("createdby",createdby);
                    } catch (JSONException e) {
                        Log.e("childs", e.getLocalizedMessage() );
                    }

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/child/delete", jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    pd.dismiss();
                                    try {
                                        if(jsonObject.getBoolean("success") == true){
                                            childList.remove(position);
                                            notifyDataSetChanged();
                                            Toast.makeText(activity, "Deleted!", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(activity, "Error: "+jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
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
                    Volley.newRequestQueue(activity).add(jsonObjectRequest);

                    pd.show();
                }
            });
            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ad.dismiss();
                }
            });
            ad= adb.create();
            ad.show();
        }
    }


    public class listchilds extends AsyncTask<String,Void,Boolean> {
        ProgressDialog pd;
        JSONObject Jobj;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(childs.this);
            pd.setMessage("Wait ..");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                childList.clear();
//                HttpHandler handler = new HttpHandler();
//                String jsonString = handler.makeServiceCall(new helper().baseIP+"getallchilds/"+strings[0]);
//
//                JSONArray jArr = new JSONArray(jsonString);
//
//                if (jArr.length()>0){
//
//                    for (int i = 0; i < jArr.length(); i++) {
//                        Jobj = jArr.getJSONObject(i);
//                        childList.add(Jobj);
//                    }
//
//                }else {
//                    return  false;
//                }

                JSONObject obj = new JSONObject();
                obj.put("id","9846575124581");
                obj.put("fullname","Syed Sarmad Shah");
                obj.put("gender","Male");
                obj.put("cnic","12345-123456789-9");
                obj.put("picture","https://static.timesofisrael.com/www/uploads/2014/04/800px-Ayesha_eating_and_smiling-e1396640745621.jpg");
                obj.put("verified","yes");
                obj.put("created","January 20, 2018");
                childList.add(obj);

                JSONObject obj1 = new JSONObject();
                obj1.put("id","984657512458");
                obj1.put("fullname","Qazi Mohsin Ijaz");
                obj1.put("gender","Male");
                obj1.put("cnic","12345-123456789-9");
                obj1.put("picture","https://4.bp.blogspot.com/-aznXkMR2dTY/Tt70vbFkHjI/AAAAAAAAGz8/h4_z8b5BIyk/s1600/pakistani-baby-1.jpg");
                obj1.put("verified","yes");
                obj1.put("created","January 20, 2018");
                childList.add(obj1);


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
                    Toast.makeText(childs.this, "No childs registered yet", Toast.LENGTH_LONG).show();
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

    private void getallchilds() {
        final ProgressDialog pd = new ProgressDialog(childs.this);
        pd.setMessage("Please wait");
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
                                childList.clear();

                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                JSONObject toplevel = jsonArray.getJSONObject(0);
                                JSONArray childsarr = toplevel.getJSONArray("childs");

                                for(int i = 0; i < childsarr.length() ; i++){
                                    JSONObject obj = childsarr.getJSONObject(i);
                                    obj.put("id",obj.getString("_id"));
                                    obj.put("fullname",obj.getString("fullname"));
                                    obj.put("gender",obj.getString("gender"));
                                    obj.put("cnic",obj.getString("cnic"));
                                    obj.put("picture",obj.getString("picture"));
                                    obj.put("verified","yes");
//                                    obj.put("created",obj.getString("created"));
                                    obj.put("schoolname",obj.getString("schoolname"));
                                    childList.add(obj);
                                }

                                if (childsarr.length()>0){
                                    recyclerView.setVisibility(View.VISIBLE);
                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setLayoutManager(layoutManager);
                                    recyclerView.setAdapter(new MyListAdapter(childList));
                                }else {
                                    Toast.makeText(childs.this, "No childs registered yet", Toast.LENGTH_LONG).show();
                                    recyclerView.setVisibility(View.GONE);
                                }




//                                Toast.makeText(childs.this, "Successfully registered.", Toast.LENGTH_SHORT).show();
//                                Log.e("childs", jsonObject.toString() );
                            }else{
                                Toast.makeText(childs.this, "Error: "+jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
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
    public void deletechild(String childid) {
        final ProgressDialog pd = new ProgressDialog(activity);
        pd.setMessage("Please wait");
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        JSONObject jsonObject = new JSONObject();
        String createdby = activity.getApplicationContext().getSharedPreferences("shared_hmb", MODE_PRIVATE).getString("email",null);
        try {
            jsonObject.put("childid",childid);
            jsonObject.put("createdby",createdby);
        } catch (JSONException e) {
            Log.e("childs", e.getLocalizedMessage() );
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, new helper().baseIP+"/users/child/delete", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pd.dismiss();
                        try {
                            if(jsonObject.getBoolean("success") == true){
                                getallchilds();
                            }else{
                                Toast.makeText(childs.this, "Error: "+jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
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
