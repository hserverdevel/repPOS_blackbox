package com.example.blackbox.mail;


import android.util.Log;

import com.example.blackbox.model.StaticValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;


/**
 * A simple class that will call the GMailSender class (which was copied from StackOverflow),
 * to send a mail to given development account, with the latest LOG details
 *
 * NOTE
 * the log is destroyed after an email, (if deleteOldData is set to true)
 * in order to avoid writing on the same log over and over
 * */
public class LogCatMailSender
{
    private static final String TAG = "<LogCatMailSender>";


    public static void sendMail(boolean deleteOldData)
    {
        // create a new thread, since in Android is not possible to
        // execute network operations on the main thread
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "Thread start to send Mail");

                try
                {
                    // get the ouput of the cmd `logcat`,
                    // and write it to a log object
                    Process        process        = Runtime.getRuntime().exec("logcat -d");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder  log            = new StringBuilder();
                    String         line           = "";

                    while ((line = bufferedReader.readLine()) != null)
                    {
                        log.append(line);
                        log.append("\n");
                    }

                    // create the subject of the mail
                    String subject = "";
                    if (deleteOldData)
                        subject = String.format("#AUTO# %s - [%s]", StaticValue.androidId, new Date().toString());
                    else
                        subject = String.format("%s - [%s]", StaticValue.androidId, new Date().toString());

                    // the receiver of the mail, in this case is the same as the sender,
                    // since this emails are just ot store a log
                    String to = "hserver.devel@gmail.com";

                    // send the email
                    // TODO remove plain text password
                    GMailSender sender = new GMailSender("hserver.devel@gmail.com", "safecalling911");
                    sender.sendMail(subject, log.toString(), to, to);

                    if (deleteOldData)
                    {
                        Log.i(TAG, "Cleaning old LOGCAT");

                        Runtime.getRuntime().exec("logcat -b all -c");
                    }
                }

                catch (Exception e)
                {
                    Log.e(TAG, "Error");
                    e.printStackTrace();
                }

            }
        });

        thread.start();
    }
}
