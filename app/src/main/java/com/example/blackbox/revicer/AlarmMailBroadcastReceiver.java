package com.example.blackbox.revicer;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.example.blackbox.mail.LogCatMailSender;

import java.util.Calendar;


/**
 * This class will handle an automatic Wake Lock acquisition process,
 * in order to automatically send a mail  with the logcat
 */
public class AlarmMailBroadcastReceiver extends BroadcastReceiver
{

    public static void setAlarm(Context context)
    {
        AlarmManager  am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent        i  = new Intent(context, AlarmMailBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        // how much time must pass between
        // each wake lock
        // long interval = 1000 /*Milliseconds*/ * 60 /*Seconds*/ * 360 /*Minutes*/;

        // use a calendar to indicate the exact time of waking up
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        // if, when the alarm is started, is not the right moment to send the mail,
        // wait for the next day
        if (!now.before(cal))
            { cal.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) + 1); }

        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }


    public static void cancelAlarm(Context context)
    {
        Intent        intent       = new Intent(context, AlarmMailBroadcastReceiver.class);
        PendingIntent sender       = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager  alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }


    /**
     * On the receive moment, start the wake lock and send the mail
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // acquire the wake lock
        PowerManager          pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKE_LOG_TAG:X");
        wl.acquire(10_000);

        // The actual code to be executed during the wake
        //LogCatMailSender.sendMail();
        LogCatMailSender.sendMail(true);

        // finally release the lock
        wl.release();
    }
}