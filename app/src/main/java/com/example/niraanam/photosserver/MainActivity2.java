package com.example.niraanam.photosserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.erikagtierrez.multiple_media_picker.Gallery;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity2 extends AppCompatActivity {

    static final int OPEN_MEDIA_PICKER = 1; // The request code

    int checkSelected;
    ArrayList<String> selectionResult = new ArrayList<String>();
    TextView txtv;
    StringBuilder builder;

    ListView listView;
    ArrayList<Spacecraft> spacecrafts = new ArrayList<>();

    ArrayList<String> listsendtoserver = new ArrayList<String>();
    Button listtoserv;

    int serverResponseCode = 0;
    ProgressDialog dialog = null;

    String upLoadServerUri = null;
    int ii;

    String UPLOAD_LINK, WhatThePlant;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                Intent intent= new Intent(MainActivity2.this, Gallery.class);
                // Set the title
                intent.putExtra("title","Select media");
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode",2);
                intent.putExtra("maxSelection",30);
                startActivityForResult(intent,OPEN_MEDIA_PICKER);
            }
        });


        Intent i = getIntent();
        WhatThePlant = i.getStringExtra("Plant");

        if(WhatThePlant.equals("Rice")){
            UPLOAD_LINK = Config.RICE_GALLERY;
        }else if(WhatThePlant.equals("Maize")){
            UPLOAD_LINK = Config.MAIZE_GALLERY;
        }else if(WhatThePlant.equals("Cassava")){
            UPLOAD_LINK = Config.CASSAVA_GALLERY;
        }else if(WhatThePlant.equals("Sugarcane")){
            UPLOAD_LINK = Config.SUGARCANE_GALLERY;
        }else if(WhatThePlant.equals("Other")){
            UPLOAD_LINK = Config.OTHER_GALLERY;
        }else{
            Toast.makeText(getApplicationContext(),"!!! No Plant Selection !!!", Toast.LENGTH_LONG).show();
            finish();
        }



        builder = new StringBuilder();
        listView= (ListView) findViewById(R.id.listview);

        sendlisttoServ();
    }

    public void sendlisttoServ() {

        listtoserv = (Button) findViewById(R.id.sendlisttoServ);

        /************* Php script path ****************/
        upLoadServerUri = UPLOAD_LINK;

        listtoserv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(listsendtoserver.size()==0){

                    Toast.makeText(MainActivity2.this, "Nothing Selected", Toast.LENGTH_SHORT).show();

                    /*Toast.makeText(MainActivity2.this,"selectionResult :"+selectionResult.size(),Toast.LENGTH_LONG).show();*/
                }else{

                    dialog = ProgressDialog.show(MainActivity2.this, "", "Uploading file(s)....please wait ^^"+"\n"+"หน้าต่างจะปิด หลังจากการอัพโหลดเสร็จสิ้น", true);
                    new Thread(new Task()).start();

                }


            }

        });

    }
    class Task implements Runnable {
        @Override
        public void run() {


            for (ii = 0; ii < listsendtoserver.size(); ii++) {

                uploadFile(listsendtoserver.get(ii));
            }
            dialog.dismiss();

            /*Toast.makeText(MainActivity2.this, "Finished Processing",
                    Toast.LENGTH_LONG).show();*/

            finish();
            //showToast();
        }


    }

    /*public void showToast(){
        Toast.makeText(getApplicationContext(), "Finished Processing", Toast.LENGTH_SHORT).show();
    }*/
    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);


        try {

            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileName + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();


            if(serverResponseCode == 200){

                runOnUiThread(new Runnable() {
                    public void run() {

                            /*Toast.makeText(MainActivity.this, ii+1+" >> File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();*/
                    }
                });
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {

            dialog.dismiss();
            ex.printStackTrace();

              /*  runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });*/

        } catch (Exception e) {

            dialog.dismiss();
            e.printStackTrace();

                /*runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });*/
        }

        return serverResponseCode;

    }

    public void openGallery(View view) {
        Intent intent= new Intent(this, Gallery.class);
        // Set the title
        intent.putExtra("title","Select media");
        // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
        intent.putExtra("mode",2);
        intent.putExtra("maxSelection",30);
        startActivityForResult(intent,OPEN_MEDIA_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == OPEN_MEDIA_PICKER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK && data != null) {
                selectionResult = data.getStringArrayListExtra("result");
                checkSelected = selectionResult.size();

                for(int i=0;i<checkSelected;i++){
                    listsendtoserver.add(selectionResult.get(i));
                    //builder.append(selectionResult.get(i) + "\n");
                }
                // txtv.setText(builder.toString());
                //Toast.makeText(getApplicationContext(), checkSelected+"\n"+selectionResult.toString(), Toast.LENGTH_LONG).show();

                listView.setAdapter(new CustomAdapter(MainActivity2.this,getData()));
            }
        }
    }

    private ArrayList<Spacecraft> getData() {

        Spacecraft s;
        for (int j = 0; j < checkSelected; j++) {

            s = new Spacecraft();
            s.setName(selectionResult.get(j));
            //String tmp = selectionResult.get(j);
            s.setUri(Uri.fromFile(new File(selectionResult.get(j))));

            spacecrafts.add(s);
        }
        return spacecrafts;
    }

}
