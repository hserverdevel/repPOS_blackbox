package com.example.blackbox.fcm;

/**
 * Created by tiziano on 2/12/19.
 */


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by L on 10/05/2017.
 * Copyright (c) 2017 Centroida. All rights reserved.
 */

public class NotificationService extends Service {

    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyFirebaseMessagingService.getToken(this);
        //Saving action implementation

        return null;
    }
}
