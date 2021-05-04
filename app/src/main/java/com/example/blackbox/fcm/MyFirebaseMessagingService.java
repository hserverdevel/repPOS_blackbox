package com.example.blackbox.fcm;

/**
 * Created by tiziano on 2/12/19.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.blackbox.R;
import com.example.blackbox.activities.SplashScreen;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;



public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID ="admin_channel";
    private NotificationManager notificationManager;




    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i("TAG", "HO RICEVUTO UN UPDATE");
        Intent notificationIntent = new Intent(this, SplashScreen.class);

/* if(SplashScreen.isAppRunning){
            //Some action
        }else{
            //Show notification as usual
        }*/


        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0
/* Request code */
, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        //You should use an actual ID instead
        int notificationId = new Random().nextInt(60000);


        Bitmap bitmap = getBitmapfromUrl(remoteMessage.getData().get("image-url"));

        Intent likeIntent = new Intent(this,NotificationService.class);
        likeIntent.putExtra(NOTIFICATION_ID_EXTRA,notificationId);
        likeIntent.putExtra(IMAGE_URL_EXTRA,remoteMessage.getData().get("image-url"));
        PendingIntent likePendingIntent = PendingIntent.getService(this,
                notificationId+1,likeIntent,PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels();
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.mipmap.burger)
                        .setContentTitle(remoteMessage.getData().get("title"))
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .setSummaryText(remoteMessage.getData().get("body"))
                                .bigPicture(bitmap))
/*Notification with Image*/

                        .setContentText(remoteMessage.getData().get("body"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)

/*.addAction(R.drawable.ic_favorite_true,
                                getString(R.string.notification_add_to_cart_button),likePendingIntent)*/

                        .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, notificationBuilder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public Bitmap getBitmapfromUrl(String imageUrl) {
        Drawable drawable = ContextCompat.getDrawable(this,R.drawable.conductor);

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        Notification.Builder mBuilder =
                new Notification.Builder(getApplicationContext())
                        .setSmallIcon(Icon.createWithBitmap(bitmap))
                        .setLargeIcon(bitmap)
                        .setContentTitle("hahah")
                        .setContentText("Tap to stop")
                        .setOngoing(true);
        return bitmap;

        /*try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }*/
    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i("QUA TOKEN", s);
        //getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }



    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
