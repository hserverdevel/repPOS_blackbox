package com.example.blackbox.client;

/**
 * Created by tiziano on 1/2/18.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.example.blackbox.model.StaticValue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ClientDelegate extends AsyncTask<String, Void, String> {

    private String dstAddress;
    private int dstPort;
    private String response = "";
    private Socket socket;
    private String sendData;
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();
    public final static Object lockObject = new Object();
    public final static ReentrantLock lock = new ReentrantLock();



    public String getMulticastChannel(int port){
        String returnip = "";
        MulticastSocket multicastSocket = null;
        try {
            multicastSocket = new MulticastSocket(7234);
            multicastSocket.setLoopbackMode(true);
            InetAddress group = InetAddress.getByName(StaticValue.multicastGroup);
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[65553];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length,group,7234);

            while (true) {
                multicastSocket.receive(dp);
                String s = new String(dp.getData(),0,dp.getLength());
                Log.i("PROVA", "VEDIAMO SE RIESCI A FERMARMI " +s);
                dstAddress = s;
                multicastSocket.leaveGroup(group);
                return dstAddress;

            }
         } catch (IOException e) {
            e.printStackTrace();
            return returnip;
        }

    }

    public TaskDelegate delegate;

    public interface TaskDelegate {
        //define you method headers to override
        void onTaskEndWithResult(String success);
        void onTaskFinishGettingData(String result);
    }

    @Override
    protected String doInBackground(String... strings) {

         if (delegate != null) {


                    socket = null;
                    DataInputStream dataInputStream = null;
                    DataOutputStream dataOutputStream = null;
                    sendData = strings[0];



                 Log.i("PROVA", dstAddress + ":" + dstPort);
                 try {


                     socket = new Socket(dstAddress, dstPort);

                     dataOutputStream = new DataOutputStream(socket.getOutputStream());

                     dataInputStream = new DataInputStream(socket.getInputStream());

                     //writing data to server
                     byte[] data = sendData.getBytes("UTF-8");
                     dataOutputStream.writeInt(data.length);
                     dataOutputStream.write(data);

                     Log.i("JOSN ", sendData.toString());

                 } catch (UnknownHostException e) {
                     // TODO Auto-generated catch block
                     response = "UnknownHostException: " + e.toString();
                     e.printStackTrace();

                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     response = "IOException: " + e.toString();
                     e.printStackTrace();

                 } finally {
                     if (socket != null) {
                         try {
                             Log.i("CLIENT DELEGATE", "CLOSING THE SERVER");
                             if (dataInputStream != null) {
                                 try {
                                     // Read data
                                     int length = dataInputStream.readInt();
                                     byte[] data = new byte[length];
                                     dataInputStream.readFully(data);
                                     String str = new String(data, "UTF-8");
                                     response = str;
                                 } catch (IOException e) {
                                     response = "IOException: " + e.toString();
                                     e.printStackTrace();
                                 }
                             }
                             if (!socket.isClosed()) socket.close();

                         } catch (IOException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                     }


                 }




         }
         return null;
    }

    @Override
    protected void onPostExecute(String result) {
       if (delegate != null) {
            //return success or fail to activity
            super.onPostExecute(result);
            delegate.onTaskEndWithResult(response);
        }
    }

    //here is the task protocol to can delegate on other object


    public ClientDelegate(int port) {
        dstAddress =  getMulticastChannel(port);
        dstPort = port;

    }





}
