package com.example.niraanam.photosserver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.niraanam.photosserver.AndroidMultiPartEntity.ProgressListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import android.media.ExifInterface;

public class UploadActivity extends AppCompatActivity {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage,txtAzimuth,txtNameImg;
    private EditText edtComment;
    private ImageView imgPreview;
    private VideoView vidPreview;
    private Button btnUpload,btnBack;
    long totalSize = 0;

    private String CheckPhotoLatlon = null;

    private String  Azimuth,Lat,Lon,GetEditText= null;
    ExifInterface exif;

    String UPLOAD_LINK, WhatThePlant;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnBack = (Button) findViewById(R.id.btnBack);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);

        edtComment = (EditText) findViewById(R.id.edtComment);

        txtAzimuth = (TextView) findViewById(R.id.txtAzimuth);
        txtNameImg = (TextView) findViewById(R.id.txtXY);

        // Changing action bar background color
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getResources().getString(R.color.action_bar))));

        // Receiving the data from previous activity
        Intent i = getIntent();

        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");
        Azimuth = i.getStringExtra("AzimuthValue");
        Lat = i.getStringExtra("LatValue");
        Lon = i.getStringExtra("LonValue");
        WhatThePlant = i.getStringExtra("Plant");

        if(WhatThePlant.equals("Rice")){
            UPLOAD_LINK = Config.RICE_UPLOAD;
        }else if(WhatThePlant.equals("Maize")){
            UPLOAD_LINK = Config.MAIZE_UPLOAD;
        }else if(WhatThePlant.equals("Cassava")){
            UPLOAD_LINK = Config.CASSAVA_UPLOAD;
        }else if(WhatThePlant.equals("Sugarcane")){
            UPLOAD_LINK = Config.SUGARCANE_UPLOAD;
        }else if(WhatThePlant.equals("Other")){
            UPLOAD_LINK = Config.OTHER_UPLOAD;
        }else{
            Toast.makeText(getApplicationContext(),"!!! No Plant Selection !!!", Toast.LENGTH_LONG).show();
            finish();
        }

        String[] separated = Lat.split(":");
        String[] separated_decimal = separated[2].split("\\.");
        //char[] chars = separated[2].toCharArray();

        //String tmp_decimal = String.valueOf(chars[0]+chars[1]);

        String tmp_lat = separated[0]+"/1,"+separated[1]+"/1,"+separated_decimal[0]+"/1";
        Lat = tmp_lat;

        String[] separated2 = Lon.split(":");
        String[] separated_decimal2 = separated2[2].split("\\.");

        String tmp_lon = separated2[0]+"/1,"+separated2[1]+"/1,"+separated_decimal2[0]+"/1";
        Lon = tmp_lon;

        txtAzimuth.setText("Azimuth: "+Azimuth+"\nLatitude: "+tmp_lat+"\nLongitude: "+Lon);

        // boolean flag to identify the media type, image or video
        boolean isImage = i.getBooleanExtra("isImage", true);

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        //Exif Reader
        try {
            exif = new ExifInterface(filePath);

            //CheckPhotoLatlon = String.valueOf(getExifTag(exif,ExifInterface.TAG_GPS_LATITUDE)+getExifTag(exif,ExifInterface.TAG_GPS_LONGITUDE));


        } catch (IOException e) {
            e.printStackTrace();
        }

        try{

            //exif.setAttribute(ExifInterface.TAG_USER_COMMENT,Azimuth);
            String theazimuth = "Azimuth: "+Azimuth;
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION,theazimuth);
            exif.setAttribute(ExifInterface.TAG_COPYRIGHT,"GISTDA (Public Organization)");

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,Lat.toString());
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,"N");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,Lon.toString());
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,"E");

            exif.saveAttributes();

            Toast.makeText(getApplicationContext(),
                    "X: "+Lat+"\n"+"Y: "+Lon, Toast.LENGTH_LONG).show();


        }catch (IOException e){
            e.printStackTrace();
        }


       /* if(TextUtils.isEmpty(CheckPhotoLatlon) || CheckPhotoLatlon==""){

            File fdelete = new File(filePath);
            if (fdelete.exists()) {
                if(fdelete.delete()) {
                }else{
                }
            }

            Toast.makeText(getApplicationContext(),
                    "ภาพถ่ายไม่มีค่าพิกัด ;(", Toast.LENGTH_LONG).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
            builder.setMessage("Photo is No GPS Value : Maybe Lost GPS signal or Don't turn on loacation tags for Camera Device."+"\n\n Press \"OK\"").setTitle("Warning!!! No GPS Value")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // delete photo after checking Photo was no GPS value
                            *//*File fdelete = new File(filePath);
                            if (fdelete.exists()) {
                                if(fdelete.delete()) {
                                }else{
                                }
                            }*//*

                            finish();

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "ภาพถ่ายมีค่าพิกัด ^-^", Toast.LENGTH_SHORT).show();

            Toast.makeText(getApplicationContext(),
                    "X : "+String.valueOf(getExifTag(exif,ExifInterface.TAG_GPS_LATITUDE))+"\n"+"Y : "+String.valueOf(getExifTag(exif,ExifInterface.TAG_GPS_LONGITUDE)), Toast.LENGTH_SHORT).show();


            //exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION, Azimuth);
            //exif.readExif(exifVar.getAbsolutePath());
            //exif.setTagValue(ExifInterface.TAG_USER_COMMENT, mString);
            try{

                //exif.setAttribute(ExifInterface.TAG_USER_COMMENT,Azimuth);
                String theazimuth = "Azimuth: "+Azimuth;
                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION,theazimuth);
                exif.setAttribute(ExifInterface.TAG_COPYRIGHT,"GISTDA (Public Organization)");

                Toast.makeText(getApplicationContext(),
                        "X: "+sendLatitude+"\n"+"Y: "+sendLongitude, Toast.LENGTH_LONG).show();

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,sendLatitude.toString());
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,"N");
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,sendLongitude.toString());
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,"E");

                exif.saveAttributes();


            }catch (IOException e){
                e.printStackTrace();
            }

        }*/

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server and save comment
                GetEditText = edtComment.getText().toString();

                try{

                    exif.setAttribute(ExifInterface.TAG_USER_COMMENT,GetEditText);
                    exif.saveAttributes();


                }catch (IOException e){
                    e.printStackTrace();
                }
                isInternetOn();


            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Back and Save Comment
                GetEditText = edtComment.getText().toString();

                try{

                    exif.setAttribute(ExifInterface.TAG_USER_COMMENT,GetEditText);
                    exif.saveAttributes();


                }catch (IOException e){
                    e.printStackTrace();
                }


                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {

        GetEditText = edtComment.getText().toString();

        try{

            exif.setAttribute(ExifInterface.TAG_USER_COMMENT,GetEditText);
            exif.saveAttributes();


        }catch (IOException e){
            e.printStackTrace();
        }

        finish();

    }

    private String getExifTag(ExifInterface exif,String tag){
        String attribute = exif.getAttribute(tag);

        return (null != attribute ? attribute : "");
    }


    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            vidPreview.setVisibility(View.GONE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
            vidPreview.setVisibility(View.VISIBLE);
            vidPreview.setVideoPath(filePath);
            // start playing
            vidPreview.start();
        }
    }

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(UPLOAD_LINK);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("website",
                        new StringBody("http://150.107.31.104"));
                entity.addPart("email", new StringBody("ppknam@gmail.com"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }

    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {

        // Delete photo after send to server
        /*File fdelete = new File(filePath);
        if (fdelete.exists()) {
            if(fdelete.delete()) {
            }else{
            }
        }*/

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Delete photo after send to server
                        *//*File fdelete = new File(filePath);
                        if (fdelete.exists()) {
                            if(fdelete.delete()) {
                            }else{
                            }
                        }*//*
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/

        Toast.makeText(getApplicationContext(),
                "Photo Succesfully Uploaded", Toast.LENGTH_SHORT).show();
        finish();
    }

    public final  boolean isInternetOn() {

        if(isInternetAvailable()== true){

            new UploadFileToServer().execute();


        }else{

            Toast.makeText(getApplicationContext(), " No Internet Connection!!! ", Toast.LENGTH_LONG).show();

        }
        return false;
    }

    public boolean isInternetAvailable() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}
