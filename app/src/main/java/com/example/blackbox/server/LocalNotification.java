package com.example.blackbox.server;

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
import com.example.blackbox.activities.Operative;
import com.example.blackbox.fcm.NotificationService;
import com.example.blackbox.model.StaticValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

/**
 * Created by tiziano on 2/14/19.
 */

public class LocalNotification {

    Thread socketServerThread;
    static final int socketServerPORT = 8080;
    Socket socket;
    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream = null;
    private ServerSocket serverSocket;
    private Context context;
    String message = "";

    public String myIp ="";

    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID ="admin_channel";
    private NotificationManager notificationManager;


    private static final LocalNotification instance = new LocalNotification();

    private LocalNotification() {
    }

    public static LocalNotification getInstance() {
        return instance;
    }

    public void setLocalNotification(Context context) {
        this.context = context;
        try {
            if(serverSocket==null)
                serverSocket = new ServerSocket();
            Log.i("server socket", "run");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(socketServerThread==null) {
            socketServerThread = new Thread(new SocketServerThread());
            runThread();
            socketServerThread.start();
            Log.i("server socket thread", "run");
        }

    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        myIp = ip;
        return ip;
    }

    private void runThread() {

        new Thread() {
            public void run() {
                MulticastSocket socket = null;
                InetAddress group = null;
                try {
                    socket = new MulticastSocket(7234);
                    group = InetAddress.getByName(StaticValue.multicastGroup);
                    socket.joinGroup(group);
                    DatagramPacket packet;
                    while (true) {

                        byte[] messaggio= new byte[1024];
                        messaggio = getIpAddress().getBytes();
                        DatagramPacket dp = new DatagramPacket(messaggio, messaggio.length,group,7234);
                        socket.send(dp);
                        //Log.d("SERVER", "RUNNING");


                    }
                } catch (IOException e) {
                    System.out.println(e.toString());
                } finally {
                    if (socket != null) {
                        try {
                            if (group != null) {
                                socket.leaveGroup(group);
                            }
                            socket.close();
                        } catch (IOException e) {

                        }
                    }
                }
            }


        }.start();
    }

    private class SocketServerThread extends Thread {

        int count = 0;
        String response = "";

        public void run() {

            /**
             * THIS IS USED TO DISCOVER IP DEVICE OVER LAN
             * BUT THE PRINTER DOESN'T HAVE A FUCKING NAME, ONLY IP FOR EVERITHING,
             * SO I CAN'T KNOW WICH FUCKIN' DEVICE IS,
             * SO IT IS IMPOSSIBLE TO REGISTER IT, AND ALSO THIS DESN'T WORK ON WIFI
             * BASICALLY THIS SUCKS!!!!
             */
            Log.d("SERVER", "RUNNING");

            try {
                // create ServerSocket using specified port

                //serverSocket = new ServerSocket();
               /* PrinterCustomF custom = PrinterCustomF.getInstance();
                custom.startPrinteCosutmF();*/
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(socketServerPORT));

                while (serverSocket != null) {

                    // block the call until connection is created and return
                    // Socket object
                    //Socket socket = serverSocket.accept();
                        socket = serverSocket.accept();
                        dataInputStream = new DataInputStream(socket.getInputStream());

                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        count++;
                        message += "#" + count + " from "
                                + socket.getInetAddress() + ":"
                                + socket.getPort() + "\n";

                        int length = dataInputStream.readInt();
                        byte[] data = new byte[length];
                        dataInputStream.readFully(data);
                        String str = new String(data, "UTF-8");
                        response = str;
                        String messageFromClient = str;
                        try {
                            JSONObject obj = new JSONObject(str);
                            myNotification(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    Log.i("SERVER STRING", str);

                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void myNotification(JSONObject remoteMessage) {
        Log.i("TAG", "HO RICEVUTO UN UPDATE");
        Intent notificationIntent = new Intent(context, Operative.class);

        /* if(SplashScreen.isAppRunning){
            //Some action
        }else{
            //Show notification as usual
        }*/


        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0
        /* Request code */
                , notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        //You should use an actual ID instead
        int notificationId = new Random().nextInt(60000);


        Bitmap bitmap = getBitmapfromUrl("");

        Intent likeIntent = new Intent(context,NotificationService.class);
        likeIntent.putExtra(NOTIFICATION_ID_EXTRA,notificationId);
        //likeIntent.putExtra(IMAGE_URL_EXTRA,remoteMessage.getData().get("image-url"));
        PendingIntent likePendingIntent = PendingIntent.getService(context,
                notificationId+1,likeIntent,PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels();
        }

        try {
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, ADMIN_CHANNEL_ID)
                            .setLargeIcon(bitmap)
                            .setSmallIcon(R.mipmap.burger)
                            .setContentTitle(remoteMessage.getString("title"))
                            .setStyle(new NotificationCompat.BigPictureStyle()
                                    .setSummaryText(remoteMessage.getString("body"))
                                    .bigPicture(bitmap))
/*Notification with Image*/

                            .setContentText(remoteMessage.getString("body"))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)

/*.addAction(R.drawable.ic_favorite_true,
                                getString(R.string.notification_add_to_cart_button),likePendingIntent)*/

                            .setContentIntent(pendingIntent);

            notificationManager.notify(notificationId, notificationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public Bitmap getBitmapfromUrl(String imageUrl) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.conductor);

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        Notification.Builder mBuilder =
                new Notification.Builder(context)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = context.getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = context.getString(R.string.notifications_admin_channel_description);

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
