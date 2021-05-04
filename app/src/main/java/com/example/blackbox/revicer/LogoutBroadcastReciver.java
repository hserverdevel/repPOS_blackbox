package com.example.blackbox.revicer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.blackbox.activities.Login;

/**
 * Created by tiziano on 9/19/17.
 */

public class LogoutBroadcastReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String actionName = intent.getAction();
        Toast.makeText(context, "Logout", Toast.LENGTH_LONG).show();

         Intent i= new Intent(context, Login.class);
        //i.setClassName("com.example.pos.activities", "PinpadActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
