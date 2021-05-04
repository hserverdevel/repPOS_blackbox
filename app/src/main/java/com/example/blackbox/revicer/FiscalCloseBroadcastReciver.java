package com.example.blackbox.revicer;

/**
 * Created by tiziano on 9/18/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class FiscalCloseBroadcastReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("FAI CHIUSURA", "FAI CHIUSURA");
        Toast.makeText(context, "fai chiusura", Toast.LENGTH_LONG).show();
     /*   DatabaseAdapter dbA = new DatabaseAdapter(context);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 16);
        calendar.set(Calendar.SECOND, 00);
        calendar.add(Calendar.DATE, -1);
        long last  = calendar.getTimeInMillis();

        long lastSession = dbA.getLastClosing();
        long now = System.currentTimeMillis();

        if(last>= lastSession && lastSession <= now) {
            Log.i("NON", "FACCIO CHIUSURA");
        }else{
            Intent i= new Intent(context, FiscalCloseActivity.class);
            context.startActivity(i);

        }*/


    }
}


