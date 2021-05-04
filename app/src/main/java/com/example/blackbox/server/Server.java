package com.example.blackbox.server;


import android.content.Context;
import android.util.Log;

import com.ditron.ziplib.jpos.JposException;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.FiscalPrinter;
import com.example.blackbox.model.PrinterInfo;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.example.blackbox.printer.Printer;
import com.example.blackbox.printer.PrinterCustomF;
import com.example.blackbox.printer.PrinterDitronF;
import com.example.blackbox.printer.PrinterDitronThread;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.utils.db.DatabaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tiziano on 1/2/18.
 */

public class Server {
    private static final Server instance = new Server();
    private Thread checkPrinter;
    private Thread checkPrinterStatus;
    private Thread refreshPrinterStatus;
    public boolean lockPrinter = false;
    private int waitingTime = 60000;
    private int suspendTime = 900000;
    public boolean delegatCheck = true;
    public final static Object lockObject = new Object();
    public final static ReentrantLock lock = new ReentrantLock();



    private Server() {
    }

    public static Server getInstance() {
        return instance;
    }

    //ServerSocket serverSocket;
    String username = "";
    public void setUsername(String s) { username = s;}
    public String getUserName(){return username;}
    String message = "";
    Thread socketServerThread;
    static final int socketServerPORT = 8080;
    Context context;
    Socket socket;
    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream = null;
    PrinterInfo printerInfo = new PrinterInfo();
    private ServerSocket serverSocket;
    boolean firstTime = true;

    boolean running = true;
    boolean check = true;
    int checkCounter = 0;
    DatabaseAdapter dbA;


    PrinterDitronF pd;


    public void setServer(Context context) {
        this.context = context;
        dbA = new DatabaseAdapter(context);
       /*this.context = context;
        try {
            if(serverSocket==null)
            serverSocket = new ServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(socketServerThread==null) {
            socketServerThread = new Thread(new SocketServerThread());
            runThread();
            socketServerThread.start();
        }

        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", "pos_network");
        wifiConfig.preSharedKey = String.format("\"%s\"", "pos_network");



      *//*  if(refreshPrinterStatus==null) {
            Log.i("START", "REFRESH ONCREATE");
            refreshPrinterStatus = new Thread(new RefreshPrinterStatusThread());
            refreshPrinterStatus.start();
            running = true;
        }*/

        if(checkPrinter==null) {
            Log.i("START", "REFRESH ONCREATE");
            checkPrinter = new Thread(new CheckIp());
            checkPrinter.start();
            check = true;
            checkCounter =0;

        }
        if(pd==null){
            pd = new PrinterDitronF();
        }




        Log.i("SETTO CHEC", "TRUE");
        check = true;
        checkCounter =0;
        running = true;
        //runCheckIp();

       /* DatabaseAdapter dbA = new DatabaseAdapter(context);
        printerInfo = dbA.getFiscalPrinter();*/

    }

    public void fiscalCloseServer(Context context) {
        this.context = context;
        try {
            if(serverSocket==null)
                serverSocket = new ServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(socketServerThread==null) {
            socketServerThread = new Thread(new SocketServerThread());
            runThread();
            socketServerThread.start();
        }

        //PrinterDitronF pd = new PrinterDitronF();
        //pd.printReport(StaticValue.IP, 0);

    }

    public void killAll() {
       // if(refreshPrinterStatus!=null){
          /*  Log.i("KILL All", ""+refreshPrinterStatus.getId());
            refreshPrinterStatus.interrupt();
            refreshPrinterStatus=null;*/
            running = false;

       // }
       // if(checkPrinter!=null) {
            check = false;
       // }

        if(socketServerThread!=null) {
            socketServerThread.interrupt();
            socketServerThread = null;
        }
        if(serverSocket!=null){
            try {
                serverSocket.close();
                serverSocket=null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }

       /* if(dataInputStream!=null){
            try {
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(dataOutputStream!=null){
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
*/


    }



    public void killServer() {
        if(refreshPrinterStatus!=null){
            Log.i("KILL REFRESH", ""+refreshPrinterStatus.getId());
            running = false;

        }
        if(checkPrinter!=null) {
            check = false;
        }


    }


    public void onDestroyThread() {

         if(socketServerThread!=null) {
             socketServerThread.interrupt();
             socketServerThread = null;
         }
         if(serverSocket!=null){
             try {
                 serverSocket.close();
                 serverSocket=null;
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         if(socket!=null){
             try {
                 socket.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             socket = null;
         }



    }

    public Boolean ping(String IP){
        try{
            InetAddress address = InetAddress.getByName(IP);
            boolean reachable = address.isReachable(10000);
            return reachable;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
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
                       // Log.d("SERVER", "RUNNING");


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

    public boolean runCheckIp(){
        Log.i("PRINTER CHECK", "LOOP CHECK");
        lockPrinter = true;
        boolean myCheck = false;
        PrinterDitronF pd = new PrinterDitronF();
        try {
            myCheck = pd.initialize();
        } catch (JposException e) {
            e.printStackTrace();
        }finally{
            StaticValue.printerOn = myCheck;
            lockPrinter = false;

        }
        Log.i("PRINTER STATUS", ""+myCheck);
        return myCheck;

    }

    private class CheckIp extends Thread{


        public void run() {

            while(true) {
               if (check) {
                   FiscalPrinter fiscal = dbA.selectFiscalPrinter();
                   if(fiscal.getId()>0) {
                       if (fiscal.getModel().equals("Ditron")) {
                           checkCounter++;
                           if (checkCounter > 10) {
                               try {
                                   Thread.sleep(5000);
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                                   check = false;
                               }
                           }
                           if (ping("192.168.0.1")) {

                           } else {


                           }
                           Log.i("DENTRO CHECK", "" + check);
                           //if (ping(StaticValue.IP)) {
                               lockPrinter = true;
                               //  PrinterDitronF pd = new PrinterDitronF();
                               boolean myCheck = false;

                               if (fiscal.isUseApi()) {
                                   Log.i("CHECK", "check ditron senza api");
                                   PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                   ditron.closeAll();
                                   myCheck = ditron.checkPrinterDitron();
                                   ditron.closeAll();
                               } else {
                                   Log.i("CHECK", "check ditron con api");
                                   try {
                                        if (pd == null) pd = new PrinterDitronF();
                                            myCheck = pd.initialize();
                                   } catch (JposException e) {
                                        e.printStackTrace();
                                        lockPrinter = false;
                                        StaticValue.printerOn = false;
                                   }
                               }


                               lockPrinter = false;

                               if (myCheck) {

                                   StaticValue.printerOn = true;
                                   Log.i("CHECK", "" + check);

                                   //interrupt();
                               } else {
                                   StaticValue.printerOn = false;


                               }
                               Log.i("PRINTER ON", ""+ StaticValue.printerOn  );

                           /*} else {
                               StaticValue.printerOn = false;


                           }*/
                           check = false;


                       } else if (fiscal.getModel().equals("Custom")) {
                           PrinterCustomF custom = PrinterCustomF.getInstance();
                           StaticValue.printerOn = custom.checkPrinterConnection();
                           custom.stopPrinterCostumF();
                           Log.i("CUSTOM PRINTER", "" + String.valueOf(StaticValue.printerOn).toUpperCase());
                           check = false;
                       }
                   }else{
                       check = false;
                   }
               }
            }
        }
    }

   /*private class RefreshPrinterStatusThread extends Thread{
        public void run() {
            while(true) {
                if(running) {
                    //if(check) {
                        try {
                            refreshPrinterStatus.sleep(suspendTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            running = false;
                        }
                    //}
                    Thread t = currentThread();
                    Log.i("ID THREAD REFRESH", "" + t.getId());

                  //  PrinterDitronF pd = new PrinterDitronF();
                    if (ping(StaticValue.IP)) {
                        //if it doesn't sleep 3 sec it can't connect
                        Log.i("PRINTER REFRESH", "CHECKING");
                        lockPrinter = true;
                        boolean myCheck = false;
                        try {
                            myCheck = pd.initialize();
                        } catch (JposException e) {
                            e.printStackTrace();
                            lockPrinter = false;
                            StaticValue.printerOn = false;
*//*                            Thread.currentThread().interrupt();
                            try {
                                refreshPrinterStatus.sleep(10000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                                Thread.currentThread().interrupt();
                            }*//*
                            //restartServer();
                        }
                        lockPrinter = false;
                        Log.i("PRINTER REFRESH", "ARRIVO QUA");
                        if (myCheck) {
                            Log.i("PRINTER REFRESH", "SETTO TRUE");
                            Log.i("ID THREAD REFRESH", "" + t.getId());
                            StaticValue.printerOn = true;
                        } else {
                            Log.i("PRINTER REFRESH", "SETTO FALSE");
                            Log.i("ID THREAD REFRESH", "" + t.getId());
                            StaticValue.printerOn = false;
                            //restartServer();
                        }

                    } else {
                        StaticValue.printerOn = false;
                    }
                   *//* try {
                        refreshPrinterStatus.sleep(suspendTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        running = false;
                        //refreshPrinterStatus.interrupt();
                       *//**//* if(refreshPrinterStatus!=null) {
                            refreshPrinterStatus.interrupt();
                            refreshPrinterStatus = null;

                        }
                        Thread.currentThread().interrupt();*//**//*
                    } finally {
                        Log.i("PRINTER FINALLY", "EXECUTE");
                      *//**//*  if(!StaticValue.printerOn) {
                                Log.i("PRINTER FINALLY", "RESTART");
                            Thread.currentThread().interrupt();
                            restartServer();

                        }*//**//*
                    }*//*
                }

            }
        }
    }*/

    public double parseToDouble(Double s){
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return Double.parseDouble(formatter.format(s).replace(",", "."));
    }




    private class SocketServerThread extends Thread{

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


            try {
                // create ServerSocket using specified port

                    //serverSocket = new ServerSocket();
               /* PrinterCustomF custom = PrinterCustomF.getInstance();
                custom.startPrinteCosutmF();*/
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(socketServerPORT));

                    while (serverSocket != null) {
                       // synchronized (lockObject) {
                            //lock.lock();
                            lockPrinter = true;
                            delegatCheck = false;



                            // block the call until connection is created and return
                            // Socket object
                            //Socket socket = serverSocket.accept();
                            try {
                                socket = serverSocket.accept();
                                //lock = new ReentrantLock();


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
                                Log.i("SERVER STRING", str);
                                //String messageFromClient = dataInputStream.readUTF();


                                ArrayList<CashButtonLayout> products = new ArrayList<CashButtonLayout>();
                                CashButtonLayout oldProduct = new CashButtonLayout();
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> oldModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                Double paid = 0.0;
                                Double cost = 0.0;
                                Double credit = 0.0;
                                Double creditL = 0.0;
                                int paymentType = 0;
                                String deviceName = "";
                                String IP = "";
                                String orderNumberBill = "-1";
                                String billId = "";
                                int printType = -1;
                                Double discount = 0.0;
                                String description = "";
                                String orderNumber = "";
                                int quantity = 0;
                                String item = "";
                                SubdivisionItem items = new SubdivisionItem();
                                String changedPosition = "";
                                int indexList = 0;
                                ArrayList<Customer> customers = new ArrayList<Customer>();
                                Customer customer = new Customer();

                                String roomName = "";
                                int tableNumber = -1;
                                int report = -1;

                                try {
                                    JSONObject jsonObject = new JSONObject(messageFromClient);
                                    printType = jsonObject.getInt("printType");
                                    IP = jsonObject.getString("IP");
                                    deviceName = jsonObject.getString("deviceName");
                                    orderNumberBill = jsonObject.getString("orderNumber");
                                    billId = jsonObject.getString("billId");

                                    switch (printType) {

                                        case 1:
                                            //print bill
                                            paymentType = jsonObject.getInt("paymentType");
                                            String prod = jsonObject.getString("products");
                                            String mod = jsonObject.getString("modifiers");

                                            Gson gson = new Gson();
                                            Type type = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                            products = gson.fromJson(prod, type);

                                            Type type1 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modi = gson.fromJson(mod, type1);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modi.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    newModifiers.put(products.get(m.getKey().getPosition()), m.getValue());
                                                }
                                            }

                                            modifiers = newModifiers;
                                            paid = parseToDouble(jsonObject.getDouble("paid"));
                                            cost = parseToDouble(jsonObject.getDouble("cost"));
                                            credit = parseToDouble(jsonObject.getDouble("credit"));
                                            creditL = parseToDouble(jsonObject.getDouble("creditL"));

                                            break;
                                        case 2:
                                            //print bill discount
                                            String prod2 = jsonObject.getString("products");
                                            String mod2 = jsonObject.getString("modifiers");

                                            Gson gson2 = new Gson();
                                            Type type2 = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                            products = gson2.fromJson(prod2, type2);

                                            Type type12 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();
                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers2 = gson2.fromJson(mod2, type12);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modi2 = gson2.fromJson(mod2, type12);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers2 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modi2.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    newModifiers2.put(products.get(m.getKey().getPosition()), m.getValue());
                                                }
                                            }

                                            modifiers = newModifiers2;

                                            paid = parseToDouble(jsonObject.getDouble("paid"));
                                            cost = parseToDouble(jsonObject.getDouble("cost"));
                                            credit = parseToDouble(jsonObject.getDouble("credit"));
                                            creditL = parseToDouble(jsonObject.getDouble("creditL"));
                                            paymentType = jsonObject.getInt("paymentType");
                                            discount = parseToDouble(jsonObject.getDouble("totalDiscount"));
                                            //pd.printBillDiscount(Float.valueOf(totalDiscount), IP, products2, modifiers2, paid2, cost2, credit2);
                                            break;
                                        case 3:
                                            //print bill fiscal partial
                                            paid = parseToDouble(jsonObject.getDouble("paid"));
                                            cost = parseToDouble(jsonObject.getDouble("cost"));

                                            paymentType = jsonObject.getInt("paymentType");

                                            description = jsonObject.getString("description");
                                            orderNumber = jsonObject.getString("orderNumber");
                                            quantity = jsonObject.getInt("quantity");

                                            break;
                                        case 4:
                                            //print non fiscal
                                            String prod4 = jsonObject.getString("products");
                                            String mod4 = jsonObject.getString("modifiers");
                                            String customer4 = jsonObject.getString("customers");

                                            Gson gson4 = new Gson();
                                            Type type4 = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                            products = gson4.fromJson(prod4, type4);

                                            Type type14 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();
                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers4 = gson4.fromJson(mod4, type14);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers4 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modifiers4.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    newModifiers4.put(products.get(m.getKey().getPosition()), m.getValue());
                                                }
                                            }
                                            // Gson gsonC4 = new Gson();
                                            Type typeC4 = new TypeToken<ArrayList<Customer>>() {
                                            }.getType();
                                            customers = gson4.fromJson(customer4, typeC4);

                                            modifiers = newModifiers4;
                                            paid = parseToDouble(jsonObject.getDouble("paid"));
                                            cost = parseToDouble(jsonObject.getDouble("cost"));
                                            credit = parseToDouble(jsonObject.getDouble("credit"));
                                            paymentType = jsonObject.getInt("paymentType");
                                            discount = parseToDouble(jsonObject.getDouble("totalDiscount"));
                                            tableNumber = jsonObject.getInt("tableNumber");
                                            roomName = jsonObject.getString("roomName");
                                            //pd.printNonFiscal(IP, products, modifiers);
                                            break;
                                        case 5:
                                            //pay subdivision item
                                            Gson gson5 = new Gson();
                                            item = jsonObject.getString("item");
                                            Type typeItem = new TypeToken<SubdivisionItem>() {
                                            }.getType();
                                            items = gson5.fromJson(item, typeItem);
                                            paid = parseToDouble(jsonObject.getDouble("paid"));
                                            cost = parseToDouble(jsonObject.getDouble("cost"));
                                            credit = parseToDouble(jsonObject.getDouble("credit"));
                                            //pd.printBill(IP, items,paid5, cost5, credit5);

                                            break;
                                        case 6:
                                            String prod6 = jsonObject.getString("products");
                                            String mod6 = jsonObject.getString("modifiers");

                                            Gson gson6 = new Gson();
                                            Type type6 = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                            products = gson6.fromJson(prod6, type6);

                                            Type type16 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();
                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers6 = gson6.fromJson(mod6, type16);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers6 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modifiers6.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    newModifiers6.put(products.get(m.getKey().getPosition()), m.getValue());
                                                }
                                            }

                                            modifiers = newModifiers6;
                                            paid = parseToDouble(jsonObject.getDouble("paid"));
                                            cost = parseToDouble(jsonObject.getDouble("cost"));
                                            credit = parseToDouble(jsonObject.getDouble("credit"));
                                            creditL = parseToDouble(jsonObject.getDouble("creditL"));
                                            paymentType = jsonObject.getInt("paymentType");
                                            description = jsonObject.getString("description");
                                            orderNumber = jsonObject.getString("orderNumber");

                                            //pd.printFiscalBillWithNonFiscal(IP, paid6, cost6,credit6, description6, orderNumber6, products6, modifiers6, paymentType6);

                                            break;
                                        case 7:
                                            String prod7 = jsonObject.getString("products");
                                            String mod7 = jsonObject.getString("modifiers");

                                            Gson gson7 = new Gson();
                                            Type type7 = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                            ArrayList<CashButtonLayout> products7 = gson7.fromJson(prod7, type7);

                                            Type type17 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();
                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers7 = gson7.fromJson(mod7, type17);

                                            Long paid7 = jsonObject.getLong("paid");
                                            Long cost7 = jsonObject.getLong("cost");
                                            int paymentType7 = jsonObject.getInt("paymentType");


                                        case 8:
                                            //print order
                                            String prod8 = jsonObject.getString("products");
                                            String mod8 = jsonObject.getString("modifiers");

                                            Gson gson8 = new Gson();
                                            Type type8 = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                            products = gson8.fromJson(prod8, type8);

                                            String prod8Customer = jsonObject.getString("customerList");

                                            Type type8Customer = new TypeToken<ArrayList<Customer>>() {
                                            }.getType();
                                            customers = gson8.fromJson(prod8Customer, type8Customer);

                                            Type type18 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modi8 = gson8.fromJson(mod8, type18);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers8 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modi8.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    newModifiers8.put(products.get(m.getKey().getPosition()), m.getValue());
                                                }
                                            }
                                            modifiers = newModifiers8;
                                            orderNumberBill = jsonObject.getString("orderNumber");
                                            indexList = jsonObject.getInt("indexList");
                                            roomName = jsonObject.getString("roomName");
                                            tableNumber = jsonObject.getInt("tableNumber");

                                            break;
                                        case 9:
                                            //print order
                                            String prod9 = jsonObject.getString("products");
                                            String oldProducts = jsonObject.getString("oldProducts");
                                            String mod9 = jsonObject.getString("modifiers");
                                            String oldModi = jsonObject.getString("oldModifiers");
                                            String customer9 = jsonObject.getString("customer");

                                            Gson gson9 = new Gson();
                                            Type type9 = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                            products = gson9.fromJson(prod9, type9);

                                            Type typeOld = new TypeToken<CashButtonLayout>() {
                                            }.getType();
                                            oldProduct = gson9.fromJson(oldProducts, typeOld);

                                            Type typeCustomer9 = new TypeToken<Customer>() {
                                            }.getType();
                                            customer = gson9.fromJson(customer9, typeCustomer9);

                                            Type type19 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();
                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modi9 = gson9.fromJson(mod9, type19);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers9 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modi9.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    newModifiers9.put(products.get(m.getKey().getPosition()), m.getValue());
                                                }
                                            }
                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> oldModi9 = gson9.fromJson(oldModi, type19);
                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : oldModi9.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    oldModi9.put(oldProduct, m.getValue());
                                                }
                                            }

                                            modifiers = newModifiers9;
                                            oldModifiers = oldModi9;
                                            orderNumberBill = jsonObject.getString("orderNumber");
                                            changedPosition = jsonObject.getString("changedPosition");
                                            break;
                                        case 10:
                                            //print increment quantity for order
                                            String prod10 = jsonObject.getString("product");
                                            String mod10 = jsonObject.getString("modifiers");
                                            String customer10 = jsonObject.getString("customer");
                                            Gson gson10 = new Gson();

                                            Type typeOld10 = new TypeToken<CashButtonLayout>() {
                                            }.getType();
                                            oldProduct = gson10.fromJson(prod10, typeOld10);

                                            Type typeCustomer10 = new TypeToken<Customer>() {
                                            }.getType();
                                            customer = gson10.fromJson(customer10, typeCustomer10);

                                            Type type110 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modi10 = gson10.fromJson(mod10, type110);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers10 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modi10.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {
                                                    newModifiers10.put(oldProduct, m.getValue());
                                                }
                                            }
                                            modifiers = newModifiers10;
                                            orderNumberBill = jsonObject.getString("orderNumber");
                                            changedPosition = jsonObject.getString("changedPosition");
                                            quantity = jsonObject.getInt("quantity");
                                            break;
                                        case 11:
                                            //delete product
                                            String prod11 = jsonObject.getString("product");
                                            String mod11 = jsonObject.getString("modifiers");
                                            String customer11 = jsonObject.getString("customer");

                                            Gson gson11 = new Gson();

                                            Type typeOld11 = new TypeToken<CashButtonLayout>() {
                                            }.getType();
                                            oldProduct = gson11.fromJson(prod11, typeOld11);

                                            Type typeCustomer11 = new TypeToken<Customer>() {
                                            }.getType();
                                            customer = gson11.fromJson(customer11, typeCustomer11);

                                            Type type111 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modi11 = gson11.fromJson(mod11, type111);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers11 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modi11.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {

                                                    newModifiers11.put(oldProduct, m.getValue());
                                                }

                                            }
                                            modifiers = newModifiers11;

                                            orderNumberBill = jsonObject.getString("orderNumber");
                                            changedPosition = jsonObject.getString("changedPosition");
                                            break;

                                        case 12:
                                            //reprint order
                                            String prod12 = jsonObject.getString("products");
                                            String mod12 = jsonObject.getString("modifiers");

                                            Gson gson12 = new Gson();
                                            Type type1212 = new TypeToken<ArrayList<CashButtonLayout>>() {
                                            }.getType();
                                /*ArrayList<CashButtonLayout> products = gson.fromJson(prod, type) ;*/
                                            products = gson12.fromJson(prod12, type1212);

                                            String prod12Customer = jsonObject.getString("customerList");

                                            Type type12Customer = new TypeToken<ArrayList<Customer>>() {
                                            }.getType();
                                            customers = gson12.fromJson(prod12Customer, type12Customer);

                                            Type type12122 = new TypeToken<HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>>() {
                                            }.getType();

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modi12 = gson12.fromJson(mod12, type12122);

                                            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers12 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                            for (Map.Entry<CashButtonLayout, ArrayList<CashButtonListLayout>> m : modi12.entrySet()) {
                                                if (m.getValue() != null && m.getValue().size() > 0) {

                                                    newModifiers12.put(products.get(m.getKey().getPosition()), m.getValue());
                                                }
                                            }
                                            modifiers = newModifiers12;
                                            orderNumberBill = jsonObject.getString("orderNumber");
                                            indexList = jsonObject.getInt("indexList");
                                            roomName = jsonObject.getString("roomName");
                                            tableNumber = jsonObject.getInt("tableNumber");

                                            break;
                                        case 13:
                                            report = jsonObject.getInt("report");
                                            break;
                                        case 14:
                                            //open cash drawer
                                            break;
                                        default:
                                            break;
                                    }

                                } catch (JSONException e) {
                                    //try del json object
                                    e.printStackTrace();
                                }


                                switch (printType) {
                                    case 1:
                                        SocketServerReplyThread socketServerReplyThread =
                                                new SocketServerReplyThread(socket, printType, deviceName, orderNumberBill, billId, IP, products, modifiers, paid, cost, credit, creditL, paymentType);
                                        socketServerReplyThread.run();

                                        break;
                                    case 2:
                                        SocketServerReplyThread socketServerReplyThread1 =
                                                new SocketServerReplyThread(socket, printType, deviceName, orderNumberBill, billId, IP, products, modifiers, paid, cost, credit, creditL, paymentType, discount, null, tableNumber, "");
                                        socketServerReplyThread1.run();
                                        break;
                                    case 3:
                                        SocketServerReplyThread socketServerReplyThread3 =
                                                new SocketServerReplyThread(socket, printType, deviceName, orderNumberBill, billId, IP, paid, cost, paymentType, description, quantity);
                                        socketServerReplyThread3.run();
                                        break;
                                    case 4:

                                        SocketServerReplyThread socketServerReplyThread4 =
                                                new SocketServerReplyThread(socket, printType, deviceName, orderNumberBill, billId, IP, products, modifiers, paid, cost, credit, creditL, paymentType, discount, customers, tableNumber, roomName);
                                        socketServerReplyThread4.run();

                                        break;

                                    case 5:
                                        SocketServerReplyThread socketServerReplyThread5 =
                                                new SocketServerReplyThread(socket, printType, deviceName, orderNumberBill, IP, paid, cost, paymentType, items);
                                        socketServerReplyThread5.run();

                                        break;
                                    case 6:
                                        SocketServerReplyThread socketServerReplyThread6 =
                                                new SocketServerReplyThread(socket, printType, deviceName, orderNumberBill, billId, IP, description, products, modifiers, paid, cost, credit, creditL, paymentType);
                                        socketServerReplyThread6.run();

                                        break;

                                    case 8:
                                        SocketServerReplyThread socketServerReplyThread8 =
                                                new SocketServerReplyThread(socket, indexList, printType, deviceName, orderNumberBill, billId, IP, products, modifiers, customers, roomName, tableNumber);
                                        socketServerReplyThread8.run();

                                        break;

                                    case 9:
                                        SocketServerReplyThread socketServerReplyThread9 =
                                                new SocketServerReplyThread(socket, changedPosition, printType, deviceName, orderNumberBill, billId, IP, products, modifiers, oldProduct, oldModifiers, customer);
                                        socketServerReplyThread9.run();

                                        break;

                                    case 10:
                                        SocketServerReplyThread socketServerReplyThread10 =
                                                new SocketServerReplyThread(socket, changedPosition, printType, deviceName, orderNumberBill, billId, IP, modifiers, oldProduct, quantity, customer);
                                        socketServerReplyThread10.run();
                                        break;
                                    case 11:
                                        SocketServerReplyThread socketServerReplyThread11 =
                                                new SocketServerReplyThread(socket, changedPosition, printType, deviceName, orderNumberBill, billId, IP, modifiers, oldProduct, quantity, customer);
                                        socketServerReplyThread11.run();

                                        break;
                                    case 12:
                                        SocketServerReplyThread socketServerReplyThread12 =
                                                new SocketServerReplyThread("reprint", socket, indexList, printType, deviceName, orderNumberBill, billId, IP, products, modifiers, customers, roomName, tableNumber);
                                        socketServerReplyThread12.run();

                                        break;
                                    case 13:
                                        SocketServerReplyThread socketServerReplyThread13 =
                                                new SocketServerReplyThread(socket, printType, IP, report);
                                        socketServerReplyThread13.run();

                                        break;
                                    case 14:
                                        SocketServerReplyThread socketServerReplyThread14 =
                                                new SocketServerReplyThread(socket);
                                        socketServerReplyThread14.run();

                                        break;


                                    default:
                                        break;
                                }

                    /*SocketServerReplyThread socketServerReplyThread =
                            new SocketServerReplyThread(socket, "192.168.1.201");
                    socketServerReplyThread.run();*/

                            } catch (SocketException e) {
                                e.printStackTrace();
                                killServer();

                            }
                        }
                   // }


                    //}
                    //QUA FINISCE IL SOKET


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean checkIfOn= false;

    private class SocketServerReplyThread extends Thread {
        OutputStream outputStream;
        String deviceName;
        String orderNumber;
        private Socket hostThreadSocket;
        int cnt;
        String response = "";
        String IP ="";
        String billId ="";
        ArrayList<CashButtonLayout> products = new ArrayList<CashButtonLayout>();
        CashButtonLayout oldProducts = new CashButtonLayout();
        HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> oldModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        Double paid;
        Double cost;
        Double credit=0.0;
        Double creditL;
        int paymentType;
        int printType;
        Double discount = 0.0;
        String description;
        int quantity;
        SubdivisionItem items = new SubdivisionItem();
        String changedPosition;
        int indexList;
        ArrayList<Customer> customers = new ArrayList<Customer>();
        Customer customer = new Customer();
        String roomName;
        int tableNumber = -1;
        int report;

        //13
        SocketServerReplyThread( Socket socket,int printType,
                                 String ip,
                                 int report) {
            hostThreadSocket = socket;
            this.printType = printType;
            IP = ip;
            this.orderNumber ="1";
            this.report = report;
        }

        SocketServerReplyThread(Socket socket,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                ArrayList<CashButtonLayout> products,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers,
                                Double paid,
                                Double cost,
                                Double credit,
                                Double creditL,
                                int paymentType
        ) {
            hostThreadSocket = socket;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            this.billId = billId;
            IP = ip;
            this.products = products;
            this.modifiers = modifiers;
            this.cost = cost;
            this.paid = paid;
            this.credit = credit;
            this.creditL = creditL;
            this.paymentType = paymentType;
        }

        //4 e 2
        SocketServerReplyThread(Socket socket,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                ArrayList<CashButtonLayout> products,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers,
                                Double paid,
                                Double cost,
                                Double credit,
                                Double creditL,
                                int paymentType,
                                Double discout,
                                ArrayList<Customer> customers,
                                int tableNumber,
                                String roomName
        ) {
            hostThreadSocket = socket;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            IP = ip;
            this.billId = billId;
            this.products = products;
            this.modifiers = modifiers;
            this.cost = cost;
            this.paid = paid;
            this.credit = credit;
            this.creditL = creditL;
            this.paymentType = paymentType;
            this.discount = discout;
            this.customers = customers;
            this.tableNumber = tableNumber;
            this.roomName = roomName;
        }


        SocketServerReplyThread(Socket socket,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                Double paid,
                                Double cost,
                                int paymentType,

                                String description,
                                int quantity
        ) {
            hostThreadSocket = socket;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            this.billId = billId;
            IP = ip;
            this.cost = cost;
            this.paid = paid;
            this.paymentType = paymentType;
            this.description = description;
            this.quantity = quantity;
        }

        SocketServerReplyThread(Socket socket,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String ip,
                                Double paid,
                                Double cost,
                                int paymentType,
                                SubdivisionItem items

        ) {
            hostThreadSocket = socket;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            IP = ip;
            this.cost = cost;
            this.paid = paid;
            this.paymentType = paymentType;
            this.items = items;

        }

        SocketServerReplyThread(Socket socket,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                String description,
                                ArrayList<CashButtonLayout> products,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers,
                                Double paid,
                                Double cost,
                                Double credit,
                                Double creditL,
                                int paymentType
        ) {
            hostThreadSocket = socket;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            this.billId = billId;
            IP = ip;
            this.description = description;
            this.products = products;
            this.modifiers = modifiers;
            this.cost = cost;
            this.paid = paid;
            this.credit = credit;
            this.creditL = creditL;
            this.paymentType = paymentType;
        }

        //8
        SocketServerReplyThread(Socket socket,
                                int indexList,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                ArrayList<CashButtonLayout> products,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers,
                                ArrayList<Customer> customers,
                                String roomName,
                                int tableNumber



        ) {
            hostThreadSocket = socket;
            this.indexList = indexList;
            this.deviceName = deviceName;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            this.billId = billId;
            IP = ip;
            this.products = products;
            this.modifiers = modifiers;
            this.customers = customers;
            this.roomName = roomName;
            this.tableNumber = tableNumber;
        }

        //9
        SocketServerReplyThread(Socket socket,
                                String changedPosition,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                ArrayList<CashButtonLayout> products,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers,
                                CashButtonLayout oldProducts,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> oldModifiers,
                                Customer customer


        ) {
            hostThreadSocket = socket;
            this.changedPosition = changedPosition;
            this.deviceName = deviceName;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            IP = ip;
            this.products = products;
            this.modifiers = modifiers;
            this.oldProducts = oldProducts;
            this.oldModifiers = oldModifiers;
            this.customer = customer;
        }

        //10
        SocketServerReplyThread(Socket socket,
                                String changedPosition,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers,
                                CashButtonLayout oldProducts,
                                int quantity,
                                Customer customer


        ) {
            hostThreadSocket = socket;
            this.changedPosition = changedPosition;
            this.deviceName = deviceName;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            IP = ip;
            this.modifiers = modifiers;
            this.oldProducts = oldProducts;
            this.quantity = quantity;
            this.customer = customer;
        }

        //12
        SocketServerReplyThread(String reprint,
                                Socket socket,
                                int indexList,
                                int printType,
                                String deviceName,
                                String orderNumber,
                                String billId,
                                String ip,
                                ArrayList<CashButtonLayout> products,
                                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers,
                                ArrayList<Customer> customers,
                                String roomName,
                                int tableNumber



        ) {
            hostThreadSocket = socket;
            this.indexList = indexList;
            this.deviceName = deviceName;
            this.printType = printType;
            this.deviceName = deviceName;
            this.orderNumber = orderNumber;
            this.billId = billId;
            IP = ip;
            this.products = products;
            this.modifiers = modifiers;
            this.customers = customers;
            this.roomName = roomName;
            this.tableNumber = tableNumber;
        }

        //14
        SocketServerReplyThread(Socket socket


        ) {
            hostThreadSocket = socket;
            orderNumber ="1";
            printType =14;
        }





        //pd.printFiscalBillWithNonFiscal(IP, paid6, cost6,credit6, description6, orderNumber6, products6, modifiers6, paymentType6);


        @Override
        public void run() {
            Log.i("SERVER PRINT", "STAMPO");
                delegatCheck = true;
                int newOrderNumber = Integer.parseInt(orderNumber) + 1;
                String returnStatus = "";

                switch (printType) {
                    case 1: {
                        //NORMAL PRINT

                        Printer pd = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd.printBill(deviceName, billId, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), creditL.floatValue(), paymentType);
                        break;
                    }
                    case 2: {
                        //PRINT WITH DISCOUNT
                        Printer pd1 = new Printer(context);
                        returnStatus += printerInfo.getCode() + "-";
                        returnStatus += pd1.printBillDiscount(discount.floatValue(), deviceName, billId, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), creditL.floatValue(), paymentType);
                        break;
                    }
                    case 3: {
                        Log.i("STAMPO", "SCONTRINO");
                        Printer pd3 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd3.printFiscalBillPartial(IP, paid.floatValue(), cost.floatValue(), description, String.valueOf(newOrderNumber), paymentType, quantity);
                        Log.i("FINITO ", "STAMPO");
                        break;
                    }
                    case 4: {
                        //PRINT NON FISCAL
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd4.printNonFiscal(discount.floatValue(), deviceName, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), paymentType, customers, tableNumber, roomName);
                        break;
                    }
                    case 5: {
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd4.printNonFiscal(discount.floatValue(), deviceName, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), paymentType, null, tableNumber, "");
                        break;
                    }
                    case 6: {
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd4.printFiscalBillWithNonFiscal(billId, IP, paid.floatValue(), cost.floatValue(), credit.floatValue(), description, String.valueOf(newOrderNumber), products, modifiers, paymentType);
                        break;
                    }
                    case 8: {
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd4.printOrder(null, indexList, deviceName, billId, IP, String.valueOf(newOrderNumber), products, modifiers, customers, roomName, tableNumber);
                        break;
                    }
                    case 9: {
                        Printer pd5 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd5.printOrderCorrection(changedPosition, deviceName, billId, IP, String.valueOf(newOrderNumber), products, modifiers, oldProducts, oldModifiers, customer);
                        break;
                    }
                    case 10: {
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd4.printOrderCorrectionInc(changedPosition, deviceName, billId, IP, String.valueOf(newOrderNumber), modifiers, oldProducts, quantity, customer);
                        break;
                    }
                    case 11: {
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd4.printOrderDelete(changedPosition, deviceName, billId, IP, String.valueOf(newOrderNumber), modifiers, oldProducts, quantity, customer);
                        break;
                    }
                    case 12: {
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        returnStatus += pd4.reprintOrder(indexList, deviceName, billId, IP, String.valueOf(newOrderNumber), products, modifiers, customers, roomName, tableNumber);
                        break;
                    }
                    case 13: {
                        Printer pd4 = new Printer(context);
                        returnStatus += printerInfo.getId() + "-";
                        returnStatus += billId + "-";
                        pd4.printClosing(IP, report);
                        break;
                    }

                    case 14: {
                        Printer pd4 = new Printer(context);
                        pd4.openCashDrawer();
                        break;
                    }
                    default:
                        break;
                }

                try {
                    Log.i("SERVER OUTPUT", "fuori");
                    outputStream = hostThreadSocket.getOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                    byte[] data = returnStatus.getBytes("UTF-8");
                    dataOutputStream.writeInt(data.length);
                    dataOutputStream.write(data);



                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        return ip;
    }

}
