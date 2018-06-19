package com.example.niraanam.photosserver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;*/

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class Table_ShowSingle extends AppCompatActivity /*implements  OnMapReadyCallback*/ {

    HttpParse httpParse = new HttpParse();
    ProgressDialog pDialog;

    // Http Url For Filter Student Data from Id Sent from previous activity.
    String HttpURL;

    String finalResult;
    HashMap<String, String> hashMap = new HashMap<>();
    String ParseResult;
    HashMap<String, String> ResultHash = new HashMap<>();
    String FinalJSonObject;

    TextView LatVal, LonVal, AziVal, ImgOrVal, ImgPVal, ImgCVal;

    String Value1Holder, Value2Holder, Value3Holder, Value4Holder, Value5Holder, Value6Holder;

    String TempItem;
    ProgressDialog progressDialog2;
    Button theClose, btnRotate;
    ImageView img;

    String READTHE_LINK, WhatThePlant;
    int ImageRotate = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_showsingle);

        //*** Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        AziVal = (TextView) findViewById(R.id.txtAzimuth);
        LatVal = (TextView) findViewById(R.id.txtLatitude);
        LonVal = (TextView) findViewById(R.id.txtLongitude);
        ImgOrVal = (TextView) findViewById(R.id.txtOrganize);
        ImgPVal = (TextView) findViewById(R.id.txtImagePath);
        ImgCVal = (TextView) findViewById(R.id.txtImageComment);

        btnRotate = (Button) findViewById(R.id.btnImageRotate);
        btnRotate.setVisibility(View.GONE);
        img = (ImageView) findViewById(R.id.imageView);

        Intent i = getIntent();
        WhatThePlant = i.getStringExtra("Plant");

        if (WhatThePlant.equals("Rice")) {
            READTHE_LINK = Config.RICE_READONE;
        } else if (WhatThePlant.equals("Maize")) {
            READTHE_LINK = Config.MAIZE_READONE;
        } else if (WhatThePlant.equals("Cassava")) {
            READTHE_LINK = Config.CASSAVA_READONE;
        } else if (WhatThePlant.equals("Sugarcane")) {
            READTHE_LINK = Config.SUGARCANE_READONE;
        } else if (WhatThePlant.equals("Other")){
            READTHE_LINK = Config.OTHER_READONE;
        } else {
            Toast.makeText(getApplicationContext(),"!!! No Plant Selection !!!", Toast.LENGTH_LONG).show();
            finish();
        }

        HttpURL = READTHE_LINK;


        //Receiving the ListView Clicked item value send by previous activity.
        TempItem = getIntent().getStringExtra("ListViewValue");

        //AziVal.setText(TempItem);

        //Calling method to filter Student Record and open selected record.
        HttpWebCall(TempItem);


        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                img.setRotation(ImageRotate);
                ImageRotate = ImageRotate + 90;

            }
        });

    }


    //Method to show current record Current Selected Record
    public void HttpWebCall(final String PreviousListViewClickedItem) {

        class HttpWebCallFunction extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pDialog = ProgressDialog.show(Table_ShowSingle.this, "Loading Data", null, true, true);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);

                pDialog.dismiss();

                //Storing Complete JSon Object into String Variable.
                FinalJSonObject = httpResponseMsg;

                //Parsing the Stored JSOn String to GetHttpResponse Method.
                new GetHttpResponse(Table_ShowSingle.this).execute();

            }

            @Override
            protected String doInBackground(String... params) {

                ResultHash.put("theID", params[0]);

                ParseResult = httpParse.postRequest(ResultHash, HttpURL);

                return ParseResult;
            }
        }

        HttpWebCallFunction httpWebCallFunction = new HttpWebCallFunction();

        httpWebCallFunction.execute(PreviousListViewClickedItem);
    }


    // Parsing Complete JSON Object.
    private class GetHttpResponse extends AsyncTask<Void, Void, Void> {
        public Context context;

        public GetHttpResponse(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                if (FinalJSonObject != null) {
                    JSONArray jsonArray = null;

                    try {
                        jsonArray = new JSONArray(FinalJSonObject);

                        JSONObject jsonObject;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);

                            // Storing Student Name, Phone Number, Class into Variables.
                            Value1Holder = jsonObject.getString("path").toString();
                            Value2Holder = jsonObject.getString("comment").toString();
                            Value3Holder = jsonObject.getString("copyright").toString();
                            Value4Holder = jsonObject.getString("azimuth").toString();
                            Value5Holder = jsonObject.getString("latitude").toString();
                            Value6Holder = jsonObject.getString("longitude").toString();


                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            String FullName = String.valueOf(Value1Holder);

            StringBuffer sb = new StringBuffer();

            if (FullName.contains(" ")) {
                String[] separated = FullName.split(" ");


                int length = separated.length;
                for (int i = 0; i < length; i++) {
                    if (i == length - 1) {
                        sb.append(separated[i]);
                    } else {
                        sb.append(separated[i] + "%20");
                    }

                }
                String thelink = sb.toString();

                new DownloadImageFromInternet((ImageView) findViewById(R.id.imageView))
                        .execute(thelink);
            } else {
                new DownloadImageFromInternet((ImageView) findViewById(R.id.imageView))
                        .execute(FullName);
            }

            /*SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(Table_ShowSingle.this);*/

            // Setting Student Name, Phone Number, Class into TextView after done all process .
            AziVal.setText(Value4Holder);
            LatVal.setText(Value5Holder);
            LonVal.setText(Value6Holder);
            ImgOrVal.setText(Value3Holder);
            ImgPVal.setText(Value1Holder);
            ImgCVal.setText(Value2Holder);

        }

        private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
            ImageView imageView;

            public DownloadImageFromInternet(ImageView imageView) {
                this.imageView = imageView;
                pDialog = ProgressDialog.show(Table_ShowSingle.this, "Loading Photo...", null, true, true);
                //Toast.makeText(getApplicationContext(), "Please wait, Loading Photo...", Toast.LENGTH_LONG).show();
            }

            protected Bitmap doInBackground(String... urls) {
                String imageURL = urls[0];
                Bitmap bimage = null;
                try {
                    InputStream in = new URL(imageURL).openStream();
                    bimage = BitmapFactory.decodeStream(in);

                } catch (Exception e) {

                }
                return bimage;
            }

            protected void onPostExecute(Bitmap result) {
                pDialog.dismiss();
                imageView.setImageBitmap(result);

                btnRotate.setVisibility(View.VISIBLE);
            }

        }
    }

   /* @Override
    public void onMapReady(GoogleMap map) {


        boolean isDouble = isDouble(Value1Holder,Value2Holder);
        if (isDouble) {

            double thelat = Double.parseDouble(Value1Holder);
            double thelon = Double.parseDouble(Value2Holder);

            map.addMarker(new MarkerOptions().position(new LatLng(thelat, thelon)).title(Value5Holder));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(thelat,thelon), 12.0f));

        } else {

            map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Failure Location from Database"));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), 12.0f));
            Toast.makeText(getApplicationContext(),"Failure Location from Databas", Toast.LENGTH_LONG).show();

        }

    }

    public boolean isDouble(String x,String y) {
        boolean isValidDouble = false;
        try
        {
            Double.parseDouble(x);
            Double.parseDouble(y);

            // x y is a valid double

            isValidDouble = true;
        }
        catch (NumberFormatException ex)
        {
            // x y is not an double
        }

        return isValidDouble;
    }*/
}
