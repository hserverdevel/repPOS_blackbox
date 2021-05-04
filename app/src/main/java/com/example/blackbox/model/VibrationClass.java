package com.example.blackbox.model;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by tiziano on 10/24/17.
 */

public class VibrationClass {
    private static boolean vibrationPermission = false;

    public static void vibeOn(Context context){
        if(vibrationPermission) {
            Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(200);
        }
    }

}
