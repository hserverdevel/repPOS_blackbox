package com.example.blackbox.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.blackbox.R;
import com.example.blackbox.client.ClientDelegate;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.server.Server;
import com.utils.db.DatabaseAdapter;

/**
 * Created by tiziano on 6/11/18.
 */

public class FiscalCloseActivity extends FragmentActivity implements ClientDelegate.TaskDelegate {

    private Context context;
    private DatabaseAdapter dbA;
    private FiscalCloseActivity  forClient;
    private  String IP = StaticValue.IP;
    private CustomButton hidden;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiscal_close);
      //  getSupportActionBar().hide();
        this.context = this;
        dbA = new DatabaseAdapter(this);
        //if(!StaticValue.printerOn) {

        Server server =  Server.getInstance();
        server.killAll();
        server.setUsername("UTENTE");
        server.fiscalCloseServer(getApplicationContext());
        //dbA.updateClosingTime();

        //}
        TimerManager.stopPinpadAlert();



        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // your code here

                        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                        startActivity(intent);
                        finish();
                    }
                },
                30000
        );







    }



    @Override
    public void onTaskEndWithResult(String success) {
        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void onTaskFinishGettingData(String result) {

    }
}
