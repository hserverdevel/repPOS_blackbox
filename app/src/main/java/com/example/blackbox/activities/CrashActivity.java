package com.example.blackbox.activities;

/**
 * Created by tiziano on 3/15/18.
 * THIS IS CREATED ONLY TO LAUNCH THE CRASH REPO SYSTEM
 * IMPLEMENTED IN ACRA
 * RIGH NOW IITS CONFIGURATION IS THE SIMPLEST ONE,
 * IN FUTURE WILL BE CONNECTED TO SERVER SIDE WHERE CRASH ERROR MESSAGES WILL BE SAVED IN DB
 */

import android.app.Application;
import android.content.Intent;

import com.example.blackbox.R;
import com.example.blackbox.model.StaticValue;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        mailTo = "tfiori@burgheria.it",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)
public class CrashActivity extends Application {

    public String nameOfDev = StaticValue.shopName;




    @Override
    public void onCreate()
    {
        super.onCreate();


        //ACRA.init(this);

    }
}
