package com.example.blackbox.model;

import android.content.Context;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;

import com.example.blackbox.activities.SplashScreen1;
import com.utils.db.DatabaseAdapter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiziano on 9/18/18.
 * PLEASE:
 * add here other static configuration that we need across our project
 */

public class StaticValue
{

    private final static String TAG = "<StaticValue>";


    public static FiscalPrinter             fiscalPrinterClass;
    public static ArrayList<KitchenPrinter> kitchenPrinterClass;
    public static DeviceInfo                deviceInfo;
    // is a blackbox present?
    public static boolean                   blackbox     = false;
    public static BlackboxInfo              blackboxInfo = new BlackboxInfo();
    // is a fiscal printer present?
    public static boolean                   printerOn    = false;
    // this device hardware Id
    public static String                    androidId;

    //change this every time government change vat (In Italy something like every two years......)
    public static int[]   vats                 = {4, 10, 15, 22};
    public static int     staticVat            = 1;
    public static String  printerName          = "RCH";
    public static boolean ditronApi            = true;
    public static String  IP                   = "10.10.10.213";
    public static int     port                 = 1471;
    public static String  orderIP              = "10.10.10.210";
    public static boolean master               = true;
    public static String  myTag                = "";
    public static boolean anotherFiscalPrinter = false;
    public static int     claim                = 100;
    public static String  portableIp           = "10.10.10.211";
    public static String  multicastGroup       = "239.1.1.211";

    //per custom 9100
    public static int             normalPinpad    = 300000;
    public static int             paymentPinpad   = 900000;
    public static boolean         fiscalPrinter   = true;
    public static boolean         kitchenPrinter  = false;
    public static boolean         portablePrinter = false;
    public static String          shopName        = "D";
    public static boolean         showProducts    = false;
    public static boolean         showFavourites  = false;
    public static boolean         valuesSet       = false;
    public        Context         context;
    public        DatabaseAdapter dbA;

    public StaticValue(Context context, BlackboxInfo bb)
    {
        this.context = context;
        this.dbA     = new DatabaseAdapter(context);

        androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        fiscalPrinterClass  = dbA.selectFiscalPrinter();
        kitchenPrinterClass = dbA.selectAllKitchenPrinter();

        deviceInfo   = dbA.selectDeviceInfo();
        blackboxInfo = bb;
        blackbox     = true;

        // test if the fiscal printer is responsive
        // A thread is used, due to the android limitations that network operations
        // must be carried outside the main process

        printerOn = true;

        /*
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                SocketAddress printerAddr = new InetSocketAddress(fiscalPrinterClass.getAddress(), 9100);
                Socket printerSocket = new Socket();

                try
                {
                    printerSocket.connect(printerAddr, 6000);
                    printerOn = true;
                    Log.i(TAG, "[new] socket connection to fiscal printer successful");
                }

                catch (Exception e)
                {
                    Log.w(TAG, "[new] error on socket connection to fiscal printer. Exception " + e.toString());
                    e.printStackTrace();
                    printerOn = false;
                }

                finally
                {
                    try { printerSocket.close(); }
                    catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        thread.start();

         */
    }

    public static void setShowProducts()
    {
        showProducts   = true;
        showFavourites = false;
    }

    public static void setShowFavourites()
    {
        showProducts   = false;
        showFavourites = true;
    }

    public static void setShowMainMenu()
    {
        showProducts   = false;
        showFavourites = false;
    }

    public static boolean getValuesSet()
    {
        return valuesSet;
    }

    public static void setValuesSet(boolean value)
    {
        valuesSet = value;
    }

}
