package com.example.blackbox.activities;

/**
 * Created by tiziano on 11/12/18.
 */


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.blackbox.R;
import com.example.blackbox.model.TwoString;
import com.example.blackbox.server.HttpHandler;
import com.example.blackbox.server.Server;
import com.utils.db.DatabaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;

public class SplashScreen extends Activity implements HttpHandler.AsyncResponse{

    String now_playing, earned;
    SplashScreen mySplash;
    private static int SPLASH_TIME_OUT = 1000;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView textView;
    private Handler handler = new Handler();
    private String status = "";
    private String username = "";
    private int userType;
    private DatabaseAdapter dbA;
    private HttpHandler httpHandler;

    public void importDatabase(String databaseName) {
       try {
            dbA = new DatabaseAdapter(this);
            dbA.getDbHelper().importDatabase(Environment.getExternalStorageDirectory() + File.separator
                    + "Download" + File.separator + databaseName+".sql", getApplicationContext().getDatabasePath("mydatabase.db").getPath());

            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            ActivityCompat.finishAfterTransition(SplashScreen.this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        Intent intent = getIntent();
        status = intent.getStringExtra("status");
        username = intent.getStringExtra("username");
        if(username==null) username= "";
        userType = intent.getIntExtra("isAdmin", -1);

        /**
         * when year is new set numero fattura to 1
         */
        //DatabaseAdapter dbA = new DatabaseAdapter(this);
        /*long lastClose = dbA.getLastClosing();
        Calendar c = Calendar.getInstance();
        int nowYear = c.get(Calendar.YEAR);
        c.setTimeInMillis(lastClose);
        int mYear = c.get(Calendar.YEAR);
        if(nowYear>mYear){
           dbA.updateNumeroFatture(0);
        }
*/
       Server server =  Server.getInstance();
        server.setUsername(username);
        server.setServer(getApplicationContext());


        if(status==null){
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mySplash = this;
            //new DownloadFileFromURL().execute("http://10.10.10.105:9000/getAppDB");
            new Thread(new Runnable() {
                public void run() {


                    while (progressStatus < 100) {
                        progressStatus += 1;
                        // Update the progress bar and display the
                        //current value in the text view
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(progressStatus);
                                //   textView.setText(progressStatus+"/"+progressBar.getMax());
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        else if(status.equals("pinpad")){
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mySplash = this;
            new Thread(new Runnable() {
                public void run() {

                    while (progressStatus < 100) {
                        progressStatus += 1;
                        // Update the progress bar and display the
                        //current value in the text view

                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(progressStatus);
                                //   textView.setText(progressStatus+"/"+progressBar.getMax());
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }else {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mySplash = this;
            //Server server = new Server(mySplash);
            new Thread(new Runnable() {
                public void run() {

                 /*   Server server =  Server.getInstance();
                    server.killServer();
                    server.setServer(getApplicationContext());*/
                    while (progressStatus < 100) {
                        progressStatus += 1;
                        // Update the progress bar and display the
                        //current value in the text view
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(progressStatus);
                                //   textView.setText(progressStatus+"/"+progressBar.getMax());
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }


    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }



    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(status==null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashScreen.this, Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                    //TwoString registration = dbA.selectCodeDuration();
                    //if(registration.getSecondString().equals("-1")){
                        //not yet registered, just for the first run
                        /*Intent i = new Intent(SplashScreen.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();*/
                    //}else{
                        //
                        /**
                         * TODO make new registrationc check for devices
                         */

                        /*
                        int duration =dbA.getCodeDuration(registration.getFirstString());

                        Log.i("MY DATE" , registration.getSecondString());
                        Log.i("MY DATE" , ""+duration);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        Calendar cal = Calendar.getInstance();

                        try {
                            cal.setTime(dateFormat.parse(registration.getSecondString()));// all done
                            Log.i("MY MONTH", ""+cal.get(Calendar.MONTH));

                            Calendar cal2 = Calendar.getInstance();
                            cal2.add(Calendar.MONTH, duration);

                            Calendar cal3 = Calendar.getInstance();
                            cal3.add(Calendar.MONTH, duration);
                            cal3.add(Calendar.DATE, -15);

                            Log.i("DATE", ""+cal.getTime());



                            if(cal.compareTo(cal3)>0) {
                                Intent i = new Intent(SplashScreen.this, ActivityLicense.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }else{
                                Intent i = new Intent(SplashScreen.this, Login.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }*/

                        /*Intent i = new Intent(SplashScreen.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();*/

                    //}




                }
            }, SPLASH_TIME_OUT);
        }else
        if(status.equals("pinpad")){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashScreen.this, Operative.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("isAdmin", userType);
                    i.putExtra("username", username);
                    startActivity(i);
                    finish();

                }
            }, SPLASH_TIME_OUT);
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    TwoString registration = dbA.selectCodeDuration();
                    if(registration.getSecondString().equals("-1")){
                        //not yet registered, just for the first run
                        Intent i = new Intent(SplashScreen.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }else{
                        //
                        /**
                         * TODO make new registrationc check for devices
                         */
                        /*int duration =dbA.getCodeDuration(registration.getFirstString());

                        Log.i("MY DATE" , registration.getSecondString());
                        Log.i("MY DATE" , ""+duration);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        Calendar cal = Calendar.getInstance();

                        try {
                            cal.setTime(dateFormat.parse(registration.getSecondString()));// all done
                            Log.i("MY MONTH", ""+cal.get(Calendar.MONTH));

                            Calendar cal2 = Calendar.getInstance();
                            cal.add(Calendar.MONTH, duration);

                            Calendar cal3 = Calendar.getInstance();
                            //cal3.add(Calendar.MONTH, duration);
                            cal3.add(Calendar.DATE, -15);

                            Log.i("DATE", ""+cal.getTime());



                            if(cal.compareTo(cal3)>0) {
                                Intent i = new Intent(SplashScreen.this, ActivityLicense.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }else{
                                Intent i = new Intent(SplashScreen.this, Login.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }



                        } catch (ParseException e) {
                            e.printStackTrace();
                        }*/

                        Intent i = new Intent(SplashScreen.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();

                    }

                }
            }, SPLASH_TIME_OUT);
        }
    }


    public void callHttpHandler(String route,  List<NameValuePair> params ){
        httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }

    @Override
    public void processFinish(String output) {
        Log.i("SERVER CONNECTION " ,"OK" + output);
        JSONObject jsonObject = null;
        Boolean check = false;
        try {
            jsonObject = new JSONObject(output);
            String route = jsonObject.getString("route");
            Log.i("SERVER RESPONSE FROM" ,route);
            switch (route) {

                default :
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                OutputStream output = new FileOutputStream(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                        + "/save.sql");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //pDialog.setProgress(Integer.parseInt(progress[0]));
            progressStatus +=Integer.parseInt(progress[0]);
            progressBar.setProgress(progressStatus);
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
           importDatabase("save");

            Intent i = new Intent(SplashScreen.this, Login.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();

        }




    }
}

