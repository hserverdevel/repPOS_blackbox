package com.example.blackbox.activities;

/**
 * Created by tiziano on 11/27/18.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomEditText;
import com.utils.db.DatabaseAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class LogcatActivity extends FragmentActivity
{
    boolean showOnlyApp = true;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logcat);
        ((TextView) findViewById(R.id.textView1)).setMovementMethod(new ScrollingMovementMethod());

        try
        {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder log  = new StringBuilder();
            String        line = "";

            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line);
                log.append("\n");
            }

            TextView tv = (TextView) findViewById(R.id.textView1);
            tv.setText(log.toString());
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }



        DatabaseAdapter dbA  = new DatabaseAdapter(getApplicationContext());
        int             fatt = dbA.selectNumeroFattura();
        ((CustomEditText) findViewById(R.id.fourthButton)).setText(String.valueOf(fatt));


        findViewById(R.id.firstButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
                scrollView.fullScroll(View.FOCUS_UP);

            }
        });


        findViewById(R.id.secondButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
                scrollView.fullScroll(View.FOCUS_DOWN);


            }
        });


        findViewById(R.id.thirdButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TextView tv = (TextView) findViewById(R.id.textView1);
                try
                {
                    File root = new File(Environment.getExternalStorageDirectory(), "Download");
                    if (!root.exists())
                    {
                        root.mkdirs();
                    }
                    File       gpxfile = new File(root, "Logcat.txt");
                    FileWriter writer  = new FileWriter(gpxfile);
                    writer.append(tv.getText().toString());
                    writer.flush();
                    writer.close();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }


                try
                {
                    File   Root         = Environment.getExternalStorageDirectory();
                    File   filelocation = new File(Root.getAbsolutePath() + "/Download/", "Logcat.txt");
                    Uri    path         = Uri.fromFile(filelocation);

                    Intent emailIntent  = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("vnd.android.cursor.dir/email");
                    String to[] = {"tfiori@burgheria.it"};

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Logcat");

                    startActivity(Intent.createChooser(emailIntent, "Send email..."));



                    //startActivity(intent);
                }
                catch (Exception e)
                {
                    System.out.println("is exception raises during sending mail" + e);
                }




               /* Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:" + "tfiori@burgheria.it"+
                        "?subject="+ Uri.encode("Logcat")+
                        "&body="+ Uri.encode(tv.getText().toString());
                emailIntent.setData(Uri.parse(mailto));

                try{
                    startActivity(emailIntent);

                }
                catch(ActivityNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "No application found for sending emails", Toast.LENGTH_LONG).show();
                }
*/

            }
        });


        findViewById(R.id.fifthButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String mycost = ((CustomEditText) findViewById(R.id.fourthButton)).getText().toString();
                if (!mycost.equals(""))
                {
                    int newInvoiceNumber = Integer.valueOf(mycost);

                    dbA.updateNumeroFatture(newInvoiceNumber);
                    ((CustomEditText) findViewById(R.id.fourthButton)).setText("");
                    int fatt = dbA.selectNumeroFattura();
                }


            }
        });

        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LogcatActivity.this, PinpadActivity.class);
                startActivity(intent);

            }
        });


        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TextView tv = (TextView) findViewById(R.id.textView1);
                tv.setText("");
                ((TextView) findViewById(R.id.textView1)).setMovementMethod(new ScrollingMovementMethod());

                if (showOnlyApp)
                {
                    try
                    {
                        Process process = Runtime.getRuntime().exec("logcat -d");
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));

                        StringBuilder log  = new StringBuilder();
                        String        line = "";
                        while ((line = bufferedReader.readLine()) != null)
                        {
                            log.append(line);
                            log.append("\n");

                        }
                        tv.setText(log.toString());
                    }

                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    showOnlyApp = false;
                }
                else
                {
                    try
                    {
                        Process process = Runtime.getRuntime().exec("logcat -d");
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));

                        StringBuilder log  = new StringBuilder();
                        String        line = "";
                        while ((line = bufferedReader.readLine()) != null)
                        {
                            if (line.contains("POS LOGCAT"))
                            {
                                log.append(line);
                                log.append("\n");
                            }

                        }
                        tv = (TextView) findViewById(R.id.textView1);
                        tv.setText(log.toString());


                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    showOnlyApp = true;
                }

            }
        });
    }
}
