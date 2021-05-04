package com.example.blackbox.server;

import android.content.Context;
import android.util.Log;

import com.example.blackbox.printer.PrinterHPRT;

import java.util.ArrayList;

/**
 * Created by tiziano on 6/8/18.
 */

public class ErrorManager {
    Context context;

    private boolean isShow = false;
    private ArrayList<Integer> billIdList = new ArrayList<Integer>();

    public ErrorManager(Context context) {
        this.context = context;
       /* Thread errorManagerThread= new Thread(new ErrorManagerThread());
        errorManagerThread.start();*/
    }

    private class ErrorManagerThread extends Thread {
        PrinterHPRT pd = new PrinterHPRT(context);
        public void run() {
            try {
                //if it doesn't sleep 3 sec it can't connect
                while(true) {
                    //30 min
                    sleep(1800000);
                    Log.i("ERR", "sono nel check");

                    if(pd!=null) {
                        int myCheck = pd.checkNoPaper("10.10.10.204");
                        Log.i("ERR", String.valueOf(myCheck));
                    }
                }

            } catch (InterruptedException e) {
                    e.printStackTrace();
            }


        }
    }

    public void openErrorActivity(){
        if(isShow){

        }else{

        }

    }
}
