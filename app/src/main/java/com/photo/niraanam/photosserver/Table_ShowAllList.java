package com.photo.niraanam.photosserver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Table_ShowAllList extends AppCompatActivity {

    ListView TableListView;
    ProgressBar progressBar;
    String HttpUrl;
    int fixlist=0;
    int add = 20;
    List<String> IdList = new ArrayList<>();

    Table_ListAdapterClass adapter;

    Button more;

    String READALL_LINK, WhatThePlant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_showalllist);

        //*** Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //more= (Button) findViewById(R.id.btnmore);


        /*more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //TableListView.smoothScrollToPosition(adapter.getCount());



                add = add+20;
                HttpUrl = "http://150.107.31.104/sql_photo_android/all_sql_table.php?datanumber="+add;
                //fixlist = fixlist+add;

                new GetHttpResponse(Table_ShowAllList.this).execute();

                //TableListView.smoothScrollToPosition(fixlist);

                //Toast.makeText(getApplicationContext(), "TableListView.smoothScrollToPosition : "+ fixlist, Toast.LENGTH_LONG).show();

            }
        });*/

        Intent i = getIntent();
        WhatThePlant = i.getStringExtra("Plant");

        if(WhatThePlant.equals("Rice")){
            READALL_LINK = Config.RICE_READALL;
        }else if(WhatThePlant.equals("Maize")){
            READALL_LINK = Config.MAIZE_READALL;
        }else if(WhatThePlant.equals("Cassava")){
            READALL_LINK = Config.CASSAVA_READALL;
        }else if(WhatThePlant.equals("Sugarcane")){
            READALL_LINK = Config.SUGARCANE_READALL;
        }else if(WhatThePlant.equals("Other")){
            READALL_LINK = Config.OTHER_READALL;
        }else{
            Toast.makeText(getApplicationContext(),"!!! No Plant Selection !!!", Toast.LENGTH_LONG).show();
            finish();
        }

        TableListView = (ListView)findViewById(R.id.listView_table);

        progressBar = (ProgressBar)findViewById(R.id.progressBar_table);

        isInternetOn();

        //Adding ListView Item click Listener.
        TableListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // TODO Auto-generated method stub

                Intent intent = new Intent(Table_ShowAllList.this,Table_ShowSingle.class);

                // Sending ListView clicked value using intent.
                intent.putExtra("ListViewValue", IdList.get(position).toString());
                intent.putExtra("Plant",WhatThePlant);

                startActivity(intent);

                //Toast.makeText(getApplicationContext(), "User Clicked : "+ IdList.get(position).toString(), Toast.LENGTH_LONG).show();

            }
        });


    }

    public final  boolean isInternetOn() {

        if(isInternetAvailable()== true){

            //HttpUrl = "http://150.107.31.104/photo_android/read.php";
            HttpUrl = READALL_LINK;

            new GetHttpResponse(Table_ShowAllList.this).execute();


        }else{

            Toast.makeText(getApplicationContext(), " No Internet Connection!!! ", Toast.LENGTH_LONG).show();

            progressBar.setVisibility(View.GONE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Table_ShowAllList.this);
            alertDialogBuilder.setTitle("Warning!!!");
            alertDialogBuilder.setMessage("You have no Internet connection, Please check for retrieving data from Database Server");

            alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    Intent intent = new Intent(Table_ShowAllList.this, Table_ShowAllList.class);
                    startActivity(intent);
                    finish();

                }
            });
            // set negative button: No message
            alertDialogBuilder.setNegativeButton("Exit the app", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    Table_ShowAllList.this.finish();

                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            // show alert
            alertDialog.show();

        }
        return false;
    }

    public boolean isInternetAvailable() {
        try {
            Process p1 = Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }


    // JSON parse class started from here.
    private class GetHttpResponse extends AsyncTask<Void, Void, Void>
    {
        public Context context;

        String JSonResult;

        List<Table_Photo> numList;

        public GetHttpResponse(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            // Passing HTTP URL to HttpServicesClass Class.
            HttpServicesClass httpServicesClass = new HttpServicesClass(HttpUrl);
            try
            {
                httpServicesClass.ExecutePostRequest();

                if(httpServicesClass.getResponseCode() == 200)
                {
                    JSonResult = httpServicesClass.getResponse();

                    if(JSonResult != null)
                    {
                        JSONArray jsonArray = null;

                        try {
                            jsonArray = new JSONArray(JSonResult);

                            JSONObject jsonObject;

                            Table_Photo photo;

                            numList = new ArrayList<Table_Photo>();

                            for(int i=0; i<jsonArray.length(); i++)
                            {
                                photo = new Table_Photo();

                                jsonObject = jsonArray.getJSONObject(i);

                                // Adding Soil Id TO IdList Array.
                                IdList.add(jsonObject.getString("img").toString());

                                //Adding Num_mark point.

                                photo.last_modified = jsonObject.getString("img").toString();

                                photo.image_name = jsonObject.getString("img").toString();

                                numList.add(photo);

                            }
                        }
                        catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    Toast.makeText(context, httpServicesClass.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)

        {
            progressBar.setVisibility(View.GONE);

            TableListView.setVisibility(View.VISIBLE);

            adapter = new Table_ListAdapterClass(numList, context);

            TableListView.setAdapter(adapter);

            //fixlist = adapter.getCount();

            //TableListView.smoothScrollToPosition(fixlist);

            //Toast.makeText(getApplicationContext(), "Rows in List : "+ fixlist, Toast.LENGTH_LONG).show();

        }
    }
}
