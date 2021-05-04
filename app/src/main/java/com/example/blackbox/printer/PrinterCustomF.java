package com.example.blackbox.printer;

import android.util.Log;

import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tiziano on 1/14/19.
 */

public class PrinterCustomF {

    private String deviceName;
    private String billId;
    private String orderNumber;
    private String IP;
    private ArrayList<CashButtonLayout> products;
    private Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers;
    private float paid;
    private float cost;
    private float credit;
    private float creditL;
    private int paymentType;
    private float discountTotal;
    private ArrayList<Customer> customers;
    private int tableNumber;
    private String roomName;
    private String description;
    private String numberOrderSplit;
    private int quantity;
    private ArrayList<LeftPayment> left ;
    private ClientInfo clientInfo;
    private int numeroFattura;

    private static final String TAG = "PrinterCosutmF.java";

    private Socket socket;
    private String IPSERVER = StaticValue.IP;
    //per cutom 9100
    private int PORTASERVER = StaticValue.port;
    private SocketAddress address;
    private PrintWriter outW;
    private OutputStreamWriter outputStw;

    int somma;
    private String fmtcnt;
    private String fmtsum;
    int contout;
    int ind;

    private String comandoK;

    private String bufcom;
    private BufferedInputStream buffer;
    private BufferedReader bufferIn;

    char STX = '\u0002';
    int NRGK;
    private String DATI;
    char ETX = '\u0003';
    int TimeOut;

    private String COMANDI;
    private String RKC;

    private int attesa = 100;

    private static final PrinterCustomF instance = new PrinterCustomF();
    private Thread serverThread;

    private PrinterCustomF(){}

    public static PrinterCustomF getInstance() {
        return instance;
    }

    public void startPrinteCosutmF(){
        if(socket!=null){
            try {
                bufcom="";
                if(bufferIn!=null)
                    bufferIn.close();
                if(buffer!=null)
                    buffer.close();
                if(outW!=null)
                    outW.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
        address = new InetSocketAddress(IPSERVER, PORTASERVER);
        socket = new Socket();
        try {
            socket.connect(address, 6000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Impostacontout();
        NRGK = 0;
       /* if(serverThread==null){
            serverThread = new Thread(new ServerThread());
            serverThread.start();
        }*/

    }

    public boolean checkPrinterConnection(){
        address = new InetSocketAddress(IPSERVER, PORTASERVER);
        socket = new Socket();
        try {
            socket.connect(address, 6000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(socket.isConnected()) return true;
        else return false;

    }

    public void stopPrinterCostumF(){
        if(socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
    }

    public boolean nowPrinting = false;

    public class ServerThread extends Thread{
        public final ReentrantLock lockhead = new ReentrantLock();
        private PrintWriter outGo;
        private BufferedInputStream buff;
        private BufferedReader buffIn;
        public void run() {
            while(true) {
                if(myVal.size()>0 && nowPrinting && buff==null){
                       // Log.i("MY VAL", myVal);
                    synchronized (lockhead) {

                            executeKube(myVal.get(0));

                    }

                }
            }
        }

        public String executeKube(String val) {
            nowPrinting = false;
                    try {
                        Log.i("ENTRO", "ENTRO IN INVIAKUBE");


                   /*  this.TimeOut = 15;
                     this.NRGK++;
                     this.DATI = val;
                     //SENDCOM(this.DATI, this.COMANDI, this.RKC);
                     CONVERT(DATI, COMANDI);
                     Log.i(TAG, this.comandoK);*/
                        outGo = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), false);
                        outGo.print(val);
                        buffIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        outGo.flush();
                        buff= new BufferedInputStream(socket.getInputStream());
                        byte[] messageByte = new byte[1000];
                        boolean end = false;
                        String dataString = "";


                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        while (!end) {
                            for (int b = 0; ((b = buff.read()) >= 0); ) {
                                Log.i("carattere", b + " " + (char) b);
                                dataString += (char) b;
                                if (b == 3 || b == 21) {
                                    end = true;

                                    break;
                                } else if (b == 6) {

                                }
                            }
                        }

                        buff = null;
                        myVal.remove(0);
                        if(myVal.size()>0)
                        nowPrinting = true;

                        Log.i("DIO CANE: ", "" + dataString);
                        Log.i("FINITO STAPARE ARTICOLO", "FINITO STAMPARE ARTICOLO ");
                        //Log.i(TAG, "---------------" + receiveDataFromServer());
                    } catch (IOException e) {
                        Log.i("ERROR", "ERROR FROM SERVER");
                        e.printStackTrace();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }

            return "";
        }


    }

    public String toRemove ="";

    public ArrayList<String> myVal = new ArrayList<>();

    public void executePrint(){
        nowPrinting = true;
    }

    public synchronized void InviaKube1(String val) {

        this.TimeOut = 15;
        this.NRGK++;
        this.DATI = val;
        //SENDCOM(this.DATI, this.COMANDI, this.RKC);
        CONVERT(DATI, COMANDI);
        Log.i(TAG, this.comandoK);
        myVal.add(comandoK);


    }

    //public final static ReentrantLock lockhead = new ReentrantLock();
    public void InviaKube(String val) {
        synchronized (locking) {

            Log.i("ENTRO", "ENTRO IN INVIAKUBE");
            this.TimeOut = 15;
            this.NRGK++;
            this.DATI = val;
            SENDCOM(this.DATI, this.COMANDI, this.RKC);
        }

    }

    public void SENDCOM(String dato, String comandoz, String RKC) {
        try {
            CONVERT(dato, comandoz);
            sendComand(this.comandoK);
            Log.i(TAG, this.comandoK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String locking = "locking";


    public void sendComand(String comando) {

            String str = comando;
            try {
                this.outW = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream())), true);
                this.outW.print(str);
                this.bufferIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                this.outW.flush();
                buffer= new BufferedInputStream(socket.getInputStream());
                byte[] messageByte = new byte[1000];
                boolean end = false;
                String dataString = "";


                DataInputStream in = new DataInputStream(socket.getInputStream());
                int i = 0;
                while (!end) {
                    for (int b = 0; ((b = buffer.read()) >= 0); ) {
                        Log.i("carattere", b + " " + (char) b);
                        dataString += (char) b;
                        if (b == 3 || b == 21) {
                            end = true;

                            break;
                        }else{
                           // Thread.sleep(50);
                        } /*else if (b == 6) {

                        }*/
                    }
                }
                Log.i(TAG, "---------------" + this.bufferIn);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (Exception e3) {
                e3.printStackTrace();
            }

    }

    public static String Right(String str, int n) {
        if (n <= 0) {
            return "";
        }
        if (n > str.length()) {
            return str;
        }
        int iLen = str.length();
        try {
            return str.substring(iLen, iLen - n);
        } catch (Exception e) {
            return str;
        }
    }

    public static String Left(String str, int n) {
        if (n <= 0) {
            return "";
        }
        return n <= str.length() ? str.substring(0, n) : str;
    }

    public void CONVERT(String dato, String comandoz) {
        this.somma = 0;
        this.fmtcnt = Right(String.valueOf(this.contout), 2);
        if (Left(this.fmtcnt, 1) == " ") {
            this.fmtcnt = "0" + Right(String.valueOf(this.contout), 1);
        } else if (this.fmtcnt.length() == 1) {
            this.fmtcnt = "0" + Right(String.valueOf(this.contout), 1);
        }
        this.bufcom = this.fmtcnt + "0" + dato;
        this.ind = 0;
        while (this.ind < this.bufcom.length()) {
            this.somma += this.bufcom.charAt(this.ind);
            this.ind++;
        }
        this.fmtsum = Right(String.valueOf(this.somma % 100), 2);
        if (Left(this.fmtsum, 1) == " ") {
            this.fmtsum = "0" + Right(this.fmtsum, 1);

        }
        Log.i("FMTSUM", fmtsum+ " " + fmtsum.length());
        if(this.fmtsum.length()<=1)
            this.fmtsum = "0" + Right(this.fmtsum, 1);
        this.comandoK = this.STX + this.bufcom + this.fmtsum + this.ETX;
        Log.i("COMANDO K", ""+comandoK);
        this.contout++;
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", new Object[]{s});
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", new Object[]{s});
    }

    public static String padLeftZero(String s, int n) {
        return String.format("%0" + n + "d", new Object[]{Integer.valueOf(Integer.parseInt(s))});
    }

    public String receiveDataFromServer() throws IOException {
        String message = "";
        this.buffer = new BufferedInputStream(this.socket.getInputStream());
        return "";


        /* int j=0;
        while (((long) j) < 1000) {
            try {
                Log.i("SERVER", "DORMI");
                Thread.sleep((long) this.attesa);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            try {
                if (this.buffer.available() <= 0) {
                    j++;
                    Log.i("SERVER", "INCREMENTA J");
                }else{
                    Log.i("SERVER", "NON INCREMENTARE J");
                    break;

                }

            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }*/

  /*     int num8 = this.buffer.read();
        if (num8 == 6 || num8 == 21) {
            Log.i("FROM SERVER", "NAK");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            message = new StringBuilder(String.valueOf(message)).append(" NAK").toString();
        }else{
            Log.i("FROM SERVER", "ACK");
            message = new StringBuilder(String.valueOf(message)).append(" ACK").toString();
            int i=0;
            while (((long) i) < 1000) {
                if (this.buffer.available() > 0) {
                    message = new StringBuilder(String.valueOf(message)).append(Character.toString(Character.toChars(this.buffer.available())[0])).toString();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }
        print = true;

        return message;*/
    }

    public String receiveDataFromServer1() throws IOException {
        Boolean flag1 = Boolean.valueOf(false);
        String message = "";
        try {
            this.buffer = new BufferedInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int size = 0;
        Calendar dataOra = Calendar.getInstance();
        Calendar dataArrivo = Calendar.getInstance();
        dataArrivo.setTimeInMillis(dataOra.getTimeInMillis() - 1000);
        long Arrivo = dataArrivo.getTimeInMillis();
        int i = 0;
        while (((long) i) < 10) {
            try {
                Thread.sleep((long) this.attesa);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            try {
                if (this.buffer.available() <= 0) {
                    i++;
                }
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
        if (this.buffer.available() > 0) {
            size = this.buffer.available();
        }
        if (size > 0) {
            try {
                int num8 = this.buffer.read();
                if (num8 == 6 || num8 == 21) {
                    message = new StringBuilder(String.valueOf(message)).append(" NAK").toString();
                    dataOra = Calendar.getInstance();
                    dataArrivo = Calendar.getInstance();
                    dataArrivo.setTimeInMillis(dataOra.getTimeInMillis() - 1000);
                    Arrivo = dataArrivo.getTimeInMillis();
                    i = 0;
                    while (((long) i) < 10) {
                        try {
                            if (this.buffer.available() > 0) {
                                message = new StringBuilder(String.valueOf(message)).append(Character.toString(Character.toChars(this.buffer.available())[0])).toString();
                            }
                        } catch (IOException e32) {
                            e32.printStackTrace();
                            try {
                                Thread.sleep((long) this.attesa);
                            } catch (InterruptedException e22) {
                                e22.printStackTrace();
                            }
                            i++;
                        }
                    }
                } else {
                    message = new StringBuilder(String.valueOf(message)).append(" ACK").toString();
                    flag1 = Boolean.valueOf(true);
                    dataOra = Calendar.getInstance();
                    dataArrivo = Calendar.getInstance();
                    dataArrivo.setTimeInMillis(dataOra.getTimeInMillis() - 1000);
                    Arrivo = dataArrivo.getTimeInMillis();
                    i = 0;
                    while (((long) i) < 10) {
                        i++;
                        if (this.buffer.available() > 0) {
                            message = new StringBuilder(String.valueOf(message)).append(Character.toString(Character.toChars(this.buffer.available())[0])).toString();
                        }
                    }
                }
            } catch (IOException e322) {
                e322.printStackTrace();
            }
        }
        return message;
    }


    public void Impostacontout() {
        this.contout = 0;
        }

public void printScontrinoCustom() {
        Log.i("Print", "Scontrino");
        int i;
        Impostacontout();
        this.NRGK = 0;
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        float totAbbuono = 0.0f;
        float valSconto = 0.0f;
        float totale = 0.0f;
        float totaleNoIva = 0.0f;
        boolean floudNoIva = false;

        //InviaKube("70081");
        //InviaKube("70015");
        InviaKube("3012132------------------------------------");
        InviaKube("3012132TAVOLO 7");
        InviaKube("3012132------------------------------------");
        InviaKube("3012132------------------------------------");
        InviaKube("3012132ORDINE 15");
        InviaKube("3012132------------------------------------");

        // InviaKube("31011" + "10"+ "22" + "Articolo " + "1000");
        InviaKube("3002122" +"2X5.00");

        InviaKube("31011" + "22"+ "22" + padRight("Articolo", 22) + padLeftZero(String.valueOf((int) 1000), 8));
        InviaKube("3001322" + padRight("Sconto  %", 22) + padLeftZero(String.valueOf((int) 500), 9));
        InviaKube("31011" + "22"+ "22" + padRight("Articolo", 22) + padLeftZero(String.valueOf((int) 1000), 8));
        InviaKube("31011" + "22"+ "22" + padRight("Articolo", 22) + padLeftZero(String.valueOf((int) 1000), 8));
        InviaKube("31011" + "22"+ "22" + padRight("Articolo", 22) + padLeftZero(String.valueOf((int) 1000), 8));
        InviaKube("31011" + "22"+ "22" + padRight("Articolo", 22) + padLeftZero(String.valueOf((int) 1000), 8));
        InviaKube("31011" + "22"+ "22" + padRight("Articolo", 22) + padLeftZero(String.valueOf((int) 1000), 8));
        InviaKube("31011" + "22"+ "22" + padRight("Articolo", 22) + padLeftZero(String.valueOf((int) 1000), 8));
        //CARTA CEDITO
        InviaKube("3006");
        //CHIUSURA SCONTRINO
        InviaKube("3011");
        InviaKube("3013");
        Log.i("Print", "Fine Scontrin");

        }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public ArrayList<CashButtonLayout> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<CashButtonLayout> products) {
        this.products = products;
    }

    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getModifiers() {
        return modifiers;
    }

    public void setModifiers(Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers) {
        this.modifiers = modifiers;
    }

    public float getPaid() {
        return paid;
    }

    public void setPaid(float paid) {
        this.paid = paid;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public float getCreditL() {
        return creditL;
    }

    public void setCreditL(float creditL) {
        this.creditL = creditL;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public void setDiscountTotal(float discountTotal) {
        this.discountTotal = discountTotal;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNumberOrderSplit() {
        return numberOrderSplit;
    }

    public void setNumberOrderSplit(String numberOrderSplit) {
        this.numberOrderSplit = numberOrderSplit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void endPrint(){

    }

    public String printBill() {
        //if(ping(IP)){
        //contout = 0;
        Impostacontout();
        this.NRGK = 0;
        float omaggio;
        float discount = 0.0f;
            long costo = 0;
            int i = 0;
            for (CashButtonLayout cashButton : products) {
                if (cashButton.getQuantityInt() > 1) {
                    InviaKube("3002122" + String.valueOf(cashButton.getQuantity()) + "X" + String.valueOf(cashButton.getPriceFloat()));

                }
                InviaKube("31011" + "0" + cashButton.getVat() + "22" + padRight(cashButton.getTitle(), 22) + padLeftZero(String.valueOf((int) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt()))), 8));


                if (cashButton.getHomage() != 0)
                    discount += cashButton.getPriceFloat() * cashButton.getQuantityInt();
                if (cashButton.getDiscount() > 0)
                    discount += cashButton.getDiscount();
                ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                if (mList != null) {
                    for (CashButtonListLayout m : mList) {
                        if(m.getPriceFloat()>0.0f) {
                            if (cashButton.getQuantityInt() > 1) {
                                InviaKube("3002122" + String.valueOf(m.getQuantity()) + "X" + String.valueOf(m.getPriceFloat()));

                            }
                            String title = padRight(m.getTitle(), 22);
                            int valueInt = (int) ((m.getPriceFloat() * 100) * m.getQuantityInt());
                            String value = padLeftZero(
                                    String.valueOf(valueInt), 8);
                            InviaKube("31011" + "0" + m.getVat() + "22" + title + value);

                            if (cashButton.getHomage() != 0)
                                discount += m.getPriceFloat() * m.getQuantityInt();
                        }

                    }
                }

            }


            /**
             * 1=sconto
             * 2=maggiorazione
             * 3=sconto 1%
             * 4 = maggiorazione 1%
             * dfp.printRecItemAdjustment(1, cashButton.getTitle(), (long) (discount*100), 1);
             */
            if(credit>0.0f){
                long cr = Math.round( (credit) * 100);
                InviaKube("3001322" + padRight("Credito Consumato", 22) + padLeftZero(String.valueOf(cr), 9));
            }

            long pagato = Math.round( (paid) * 100);
            float creditValue = credit;
            InviaKube("3002215");

            if (pagato > 0.0f) {
                    InviaKube("3003");

                if(discount>0){
                        InviaKube("3001322" + padRight("Sconto  %", 22) + padLeftZero(String.valueOf((int) discount*100), 9));

                }

                if(paymentType==1) {
                    try {
                            InviaKube("300410CONTANTI  " + padLeftZero("000000000" + pagato, 9));

                    } catch (Exception e) {

                    }
                }else if(paymentType==2){
                       InviaKube("300510BUONI  " + padLeftZero("000000000" + ((int) (100.0f * cost)), 9));

                }else{
                        InviaKube("3006");

                }
            }
        InviaKube("3011");
        if(creditL>0){
            InviaKube("3012132--------------------------------");
            InviaKube("3012132" + "CREDITO  " + String.valueOf(creditL) + " EURO #"+ String.valueOf(billId));
            InviaKube("3012132--------------------------------");
        }

        InviaKube("3013");
       // executePrint();

            return "";

    }

    public String printBillDiscount(

    ) {
        contout = 0;
        float discount = 0.0f;
        for (CashButtonLayout cashButton : products) {
            //omaggio = 0.0f;
            //discount = 0.0f;
            if (cashButton.getQuantityInt() > 1)
                InviaKube("3002122" + String.valueOf(cashButton.getQuantity()) + "X" + String.valueOf(cashButton.getPriceFloat()));
            InviaKube("31011" +  "0"+cashButton.getVat() + "22" + padRight(cashButton.getTitle(), 22) + padLeftZero(String.valueOf((int)(cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), 8));
            if (cashButton.getHomage() != 0)
                discount += cashButton.getPriceFloat() * cashButton.getQuantityInt();
            if (cashButton.getDiscount() > 0)
                discount += cashButton.getDiscount();
            ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
            if (mList != null) {
                for (CashButtonListLayout m : mList) {
                        /*if (m.getPriceFloat() == 0.0f) continue;
                        else {*/
                    if(m.getPriceFloat()>0.0f) {
                        Log.i("Modifer", m.getTitle());
                        if (cashButton.getQuantityInt() > 1)
                            InviaKube("3002122" + String.valueOf(m.getQuantity()) + "X" + String.valueOf(m.getPriceFloat()));
                        String title = padRight(m.getTitle(), 22);
                        int valueInt = (int) ((m.getPriceFloat() * 100) * m.getQuantityInt());
                        Log.i("VALUEiNT", "" + valueInt);
                        String value = padLeftZero(
                                String.valueOf(valueInt), 8);
                        Log.i("value", value);
                        InviaKube("31011" + "0" + m.getVat() + "22" + title + value);
                        if (cashButton.getHomage() != 0)
                            discount += m.getPriceFloat() * m.getQuantityInt();
                        //}
                    }
                }
            }

        }


            /**
             * 1=sconto
             * 2=maggiorazione
             * 3=sconto 1%
             * 4 = maggiorazione 1%
             * dfp.printRecItemAdjustment(1, cashButton.getTitle(), (long) (discount*100), 1);
             */
        long pagato = Math.round((paid) * 100);
        if(discountTotal+discount>0.0f) {
            int value = (int) ((discount+discountTotal)*100);
            String left = padLeftZero(String.valueOf(value),8);
            InviaKube("3001322" + padRight("Sconto  ", 22) +left);
        }
        InviaKube("3002215");
        if (pagato > 0.0f) {
            InviaKube("3003");

            if(paymentType==1) {
                try {
                    InviaKube("300410CONTANTI  " + padLeftZero("000000000" + pagato, 9));
                } catch (Exception e) {
                }
            }else {
                InviaKube("3006");
            }
        }
        InviaKube("3011");
        InviaKube("3013");
        return "";

    }

    public String printNonFiscal(

    ) {
        contout = 0;
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        ArrayList<Customer> addedCustomer = new ArrayList<Customer>();
        float costo = 0.0f;
        InviaKube("70081");
        InviaKube("40011");
        InviaKube("4002");
        if(tableNumber!=-1 && tableNumber!=-11) {
            InviaKube("4003132--------------------------------");
            InviaKube("4003132 STANZA " + padRight(roomName, 32) + "          ");
            InviaKube("4003132--------------------------------");
            InviaKube("4003132 TAVOLO " + padRight(String.valueOf(tableNumber), 32) + "          ");
            InviaKube("4003132--------------------------------");
        }else{
            InviaKube("4003132--------------------------------");
            InviaKube("4003132 NUMERO ORDINE " + padRight(orderNumber, 32) + "          ");
            InviaKube("4003132--------------------------------");
        }
        if(products.size()>0) {
            for (CashButtonLayout cashButton : products) {
                if (cashButton.getClientPosition() != 0) {
                    if(customers.size()>0) {
                        if(customers.contains(cashButton.getClientPosition() - 1)) {
                            Customer customer = customers.get(cashButton.getClientPosition() - 1);
                            if (!addedCustomer.contains(customer)) {
                                InviaKube("4003132 " + padRight(customer.getDescription(), 32) + "          ");
                                addedCustomer.add(customer);
                            }
                        }
                    }
                }

                if (cashButton.getHomage() == 0) {
                    if (cashButton.getQuantityInt() > 1)
                        InviaKube("4003132" + String.valueOf(cashButton.getQuantity()) + "X" + String.valueOf(cashButton.getPriceFloat()));
                    costo += (cashButton.getPriceFloat()) * cashButton.getQuantityInt();
                    String price = df_1.format(((cashButton.getPriceFloat()) * cashButton.getQuantityInt()));
                    InviaKube("4003132" + padRight(cashButton.getTitle(), 32 - price.length()) + price);
                    ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                    if (mList != null) {
                        for (CashButtonListLayout m : mList) {
                            if (m.getPriceFloat() > 0.0f) {
                                if (m.getQuantityInt() > 1)
                                    InviaKube("4003132" + String.valueOf(m.getQuantity()) + "X" + String.valueOf(m.getPriceFloat()));
                                String priceM = df_1.format(((m.getPriceFloat()) * m.getQuantityInt()));
                                InviaKube("4003132" + padRight(m.getTitle(), 32 - priceM.length()) + priceM);
                                costo += (m.getPriceFloat()) * m.getQuantityInt();
                            }
                        }
                    }
                } else {
                    //omaggio
                    InviaKube("4003132" + padRight(cashButton.getTitle(), 20) + padLeftZero("OMAGGIO", 8));
                }
            }
        }
        String totale = df_1.format(costo);
        InviaKube("4003132--------------------------------");
        InviaKube("4003132" + padRight("TOTALE", 32-totale.length()) + totale);
        InviaKube("4003132--------------------------------");
        InviaKube("4004");
        return "";

    }

    public String printFiscalBillPartial1() {
        Log.i("LOG", "DENTRO printFiscalBillPartial");
        //if (ping(IP)) {
        InviaKube("31011" + "02" + "22" + padRight(description, 22) + padLeftZero(String.valueOf( (int) (cost* 100) ), 8));
        InviaKube("3002215");
        long pagato = Math.round((paid) * 100);
        if (pagato > 0.0f) {
            InviaKube("3003");
            if(paymentType==1) {
                try {
                    InviaKube("300410CONTANTI  " + padLeftZero("000000000" + pagato, 9));
                } catch (Exception e) {
                }
            }else if(paymentType==2){
                InviaKube("300510BUONI  " + padLeftZero("000000000" + ((int) (100.0f * cost)), 9));

            }else{
                InviaKube("3006");
            }
        }
        InviaKube("3011");
        InviaKube("3013");

       return "";
    }

    public String printFiscalBillPartial() {
        contout = 0;
        Log.i("LOG", "DENTRO printFiscalBillPartial");
        for(LeftPayment myLeft : left) {
            if(myLeft.getPaid()<myLeft.getCost())
                InviaKube("31011" + "01" + "22" + padRight("PER AMOUNT", 22) + padLeftZero(String.valueOf( (int) (myLeft.getPaid()* 100) ), 8));
            else
                InviaKube("31011" + "01" + "22" + padRight("PER AMOUNT", 22) + padLeftZero(String.valueOf( (int) (myLeft.getCost()* 100) ), 8));
            InviaKube("3002215");
            InviaKube("3003");
            String riga = "";
            if (myLeft.getPaymentType() == 1) {
                InviaKube("300410CONTANTI  " + padLeftZero("000000000" +((int) myLeft.getPaid()*100), 9));

            } else if (myLeft.getPaymentType() == 2) {
                InviaKube("300510BUONI  " + padLeftZero("000000000" + ((int) (100.0f * myLeft.getPaid())), 9));

            } else {
                InviaKube("3006");
            }
            InviaKube("3011");
            InviaKube("3013");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        printNonFiscal();

        return "";
    }

    public void apriCassetto() {
        Impostacontout();
        this.NRGK = 0;
        InviaKube("70081");
    }

    public void printLetturaGiornaliera() {
        Impostacontout();
        this.NRGK = 0;
        InviaKube("2003");
    }

    public void printMemoriaFiscale() {
        Impostacontout();
        this.NRGK = 0;
        InviaKube("2002");
    }




    public ArrayList<LeftPayment> getLeftPayment() {
        return left;
    }

    public void setLeftPayment(ArrayList<LeftPayment> left) {
        this.left= left;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public int getNumeroFattura() {
        return numeroFattura;
    }

    public void setNumeroFattura(int numeroFattura) {
        this.numeroFattura = numeroFattura;
    }

    public void printInvoice() {
        contout = 0;
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        for(int i =0; i<2; i++) {
            ArrayList<Customer> addedCustomer = new ArrayList<Customer>();
            float costo = 0.0f;
            InviaKube("70081");
            InviaKube("40011");
            InviaKube("4002");

            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

            InviaKube("4003132--------------------------------");
            InviaKube("4003132 Fattura Di Cortesia: " + + year + "/" + numeroFattura + "/" +StaticValue.shopName);
            InviaKube("4003132 Data: " + day + "/" + month + "/" + year );


            //Stampa Fattura
            if (clientInfo.getCodice_fiscale().equals("")) {
                InviaKube("4003132 " +clientInfo.getCompany_vat_number());
                InviaKube("4003132 " +clientInfo.getCompany_name());
                InviaKube("4003132 " +clientInfo.getCompany_address());
                InviaKube("4003132 " +clientInfo.getCompany_postal_code());
                InviaKube("4003132 " +clientInfo.getCompany_city());
                InviaKube("4003132 " +clientInfo.getCompany_country());

            } else {
                InviaKube("4003132 " +clientInfo.getCompany_vat_number());
                InviaKube("4003132 " +clientInfo.getCodice_fiscale());
                InviaKube("4003132 " + clientInfo.getName() + " " + clientInfo.getSurname());
                InviaKube("4003132 " +clientInfo.getCompany_address());
                InviaKube("4003132 " +clientInfo.getCompany_postal_code());
                InviaKube("4003132 " +clientInfo.getCompany_city());
                InviaKube("4003132 " +clientInfo.getCompany_country());
            }


            if(tableNumber!=-1 && tableNumber!=-11) {
                InviaKube("4003132--------------------------------");
                InviaKube("4003132 STANZA " + padRight(roomName, 32) + "          ");
                InviaKube("4003132--------------------------------");
                InviaKube("4003132 TAVOLO " + padRight(String.valueOf(tableNumber), 32) + "          ");
                InviaKube("4003132--------------------------------");
            }else{
                InviaKube("4003132--------------------------------");
                InviaKube("4003132 NUMERO ORDINE " + padRight(orderNumber, 32) + "          ");
                InviaKube("4003132--------------------------------");
            }

            for (CashButtonLayout cashButton : products) {
                if (cashButton.getClientPosition() != 0) {
                    Customer customer = customers.get(cashButton.getClientPosition() - 1);
                    if (!addedCustomer.contains(customer)) {
                        InviaKube("4003132 " + padRight(customer.getDescription(), 32) + "          ");
                        addedCustomer.add(customer);
                    }
                }

                if (cashButton.getHomage() == 0) {
                    if (cashButton.getQuantityInt() > 1)
                        InviaKube("4003132" + String.valueOf(cashButton.getQuantity()) + "X" + String.valueOf(cashButton.getPriceFloat()));
                    costo += (cashButton.getPriceFloat() ) * cashButton.getQuantityInt();
                    String price = df_1.format(((cashButton.getPriceFloat() ) * cashButton.getQuantityInt()));
                    InviaKube("4003132" + padRight(cashButton.getTitle(), 32-price.length()) + price);
                    ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                    if (mList != null) {
                        for (CashButtonListLayout m : mList) {
                            if(m.getPriceFloat()>0.0f) {
                            if (m.getQuantityInt() > 1)
                                InviaKube("4003132" + String.valueOf(m.getQuantity()) + "X" + String.valueOf(m.getPriceFloat()));
                            String priceM = df_1.format(((m.getPriceFloat() ) * m.getQuantityInt()));
                            InviaKube("4003132" + padRight(m.getTitle(), 32-priceM.length()) + priceM);
                            costo += (m.getPriceFloat() ) * m.getQuantityInt();
                            }
                        }
                    }
                } else {
                    //omaggio
                    InviaKube("4003132" + padRight(cashButton.getTitle(), 20) + padLeftZero("OMAGGIO", 8));
                }
            }
            String totale = df_1.format(costo);
            InviaKube("4003132--------------------------------");
            InviaKube("4003132" + padRight("TOTALE", 32-totale.length()) + totale);
            InviaKube("4003132--------------------------------");
            if (paymentType == 1) {
                InviaKube("4003132--------------------------------");
                InviaKube("4003132" + padRight("CONTANTI" ,32) );
                InviaKube("4003132--------------------------------");


            } else{
                InviaKube("4003132--------------------------------");
                InviaKube("4003132" + padRight("CARTA DI CREDITO" ,32) );
                InviaKube("4003132--------------------------------");
            }
            if(i==1){
                InviaKube("4003132 COPIA PER IL CLIENTE");
            }
            InviaKube("4004");
        }

    }


}
