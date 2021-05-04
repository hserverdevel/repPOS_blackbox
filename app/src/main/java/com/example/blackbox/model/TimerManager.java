package com.example.blackbox.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by tiziano on 9/19/17.
 */

public class TimerManager {

    private static long sessionTimeStart;
    private static PendingIntent pendingIntent;
    private long lastInteractionTime;
    private static AlarmManager alarmManagerPinpad;
    public static Intent intentPinpad;
    public static Intent intentLogout;
    public static Intent intentCloseFiscal;
    public static Context context;
    private static AlarmManager alarmManagerLogout;
    private static AlarmManager alarmManagerFiscalClose;


    public static void setSessionTimeStart(long time){
        sessionTimeStart = time;
    }
    public static long getSessionTimeStart(){
        return sessionTimeStart ;
    }

    public static void setIntentPinpad(Intent i ) {
        intentPinpad = i;
    }
    public static void setIntentLogout(Intent i ) {
        intentLogout = i;
    }

    public static void setAlarmManagerFiscalClose(Intent i) {
        intentCloseFiscal = i;
    }

    public static void setContext(Context c) {
        context = c;
    }


    public static void startPinpadAlert(int type) {
        pendingIntent = PendingIntent.getBroadcast(context, 0, intentPinpad, 0);
        alarmManagerPinpad = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        //5 min in millisecond
        //one is for normal activity, 2 is for payment so that last longer
        if(type==1) {
            alarmManagerPinpad.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (StaticValue.normalPinpad), pendingIntent);
        }else if(type==2){
            alarmManagerPinpad.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (StaticValue.paymentPinpad), pendingIntent);
        }else{
            alarmManagerPinpad.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (StaticValue.normalPinpad), pendingIntent);
        }
    }

    public static void stopPinpadAlert() {
        if(context!=null && intentPinpad!=null) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intentPinpad, 0);
            //15 min in millisecond
            alarmManagerPinpad.cancel(pendingIntent);
        }
    }

    public static void startLogoutAlert() {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intentLogout, 0);
            alarmManagerLogout = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            //15 min in millisecond
            alarmManagerLogout.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + (1500000), pendingIntent);
    }

    public static void stopLogoutAlert() {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intentLogout, 0);
        //15 min in millisecond
        alarmManagerLogout.cancel(pendingIntent);
    }

    public static void stopFiscalAlert() {
        if(context!=null) {
            if(intentCloseFiscal!=null) {
                pendingIntent = PendingIntent.getBroadcast(context, 0, intentCloseFiscal, 0);
                //15 min in millisecond
                alarmManagerFiscalClose.cancel(pendingIntent);
            }
        }
    }

    public static void startFiscalCloseAlert(){
       // PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentCloseFiscal, 0);

       /* // Set the alarm to start at 21:32 PM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = calendar.get(Calendar.HOUR_OF_DAY); //Get the hour from the calendar
        //recupero ultima chiusura se l'ora è minore di 00 e maggiore di 3 aumento il giorno di uno
        if(hour >= 00 && hour <=1){

        }else
            calendar.add(Calendar.DATE, 1  );
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 00);

        alarmManagerFiscalClose = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        alarmManagerFiscalClose.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                 pendingIntent);*/
    }

    /**
    public static void startAlert(Context context) {
        int timeInSec = 2;

        Intent intent = new Intent(context, MyBroadcastReceiver.class);
        intent.setAction("printFiscal");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, intent, 0);
        getAlarmManagerFiscalClose = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        getAlarmManagerFiscalClose.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + (2000000), pendingIntent);
    }
     */

    /**
     * check if session is open for today
     * @return
     * 0 if its first time
     * 1 if session is open
     * 2 if session must be close;
     */
    /*public static int checkSessionOpen(){
        int check  = 0;
        DatabaseAdapter dbA = new DatabaseAdapter(context);

        //retrive old start session time
        long startSessionTime  = dbA.getSessionTime(System.currentTimeMillis());
        if(startSessionTime==-1){
            //if equals -1 is probably the first time i'm using app
        }else{
            //start session time is an actual time
            Calendar now = Calendar.getInstance();
            Calendar oldSessionTime = Calendar.getInstance();
            oldSessionTime.setTimeInMillis(startSessionTime);
            setSessionTimeStart(startSessionTime);
            //check if start session time day is the same as today
            if(oldSessionTime.get(Calendar.DATE)==now.get(Calendar.DATE)){
                //yes, still working in same session
                check = 1;
            }else{
                //no, you have to close the old session and print resoconto fiscale o come cazzo si chiama
                check = 2;
            }
        }
        return check;
    }*/



}
