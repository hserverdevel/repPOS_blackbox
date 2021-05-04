package com.example.blackbox.revicer;

/**
 * Created by tiziano on 9/18/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.blackbox.activities.PinpadActivity;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TimerManager;

public class PinpadBroadcastReciver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        TimerManager.stopPinpadAlert();
        StaticValue.printerOn = false;

        String username = intent.getStringExtra("username");
        int isAdmin = intent.getIntExtra("isAdmin", -1);

        Intent i= new Intent(context, PinpadActivity.class);
        i.putExtra("username", username);
        i.putExtra("isAdmin", isAdmin);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        System.exit(0);
    }
}


