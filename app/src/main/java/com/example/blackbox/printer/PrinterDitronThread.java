package com.example.blackbox.printer;

import android.util.Base64;
import android.util.Log;

import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.FiscalPrinter;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tiziano on 1/15/19.
 */

public class PrinterDitronThread{
    public static final int FISCAL_ZERO = 0;
    public static final int SIMPLE_REPORT = 1;
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
    public int report = -1;
    public ClientInfo clientInfo;
    public void setClientInfo(ClientInfo c) {clientInfo =c;}
    public int numeroFattura;
    public void setNumeroFattura(int i){numeroFattura =i;}


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
    public ArrayList<LeftPayment> left;
    public void setLeftPayment(ArrayList<LeftPayment> l){left = l;}
    public void setReport(int i){report = i;}


    private static final String TAG = "PrinterDitronThread.java";
    private String COMANDI;
    private String IPSERVER = "";
    private int PORTASERVER = 0;
    private SocketAddress address;
    private String auth64;
    private BufferedWriter bufferW;
    private BufferedReader bufferIn;
    private PrintWriter outW;
    private OutputStreamWriter outputStw;
    private String pwd="admin";
    private Socket socket;
    private String user="admin";
    private int contout;
    private Thread serverThread;

    private int metodoStampa;
    public boolean nowPrinting = false;

    public void setPrinterThread(){
        FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
        IPSERVER = myFiscal.getAddress();
        PORTASERVER = myFiscal.getPort();
        if(serverThread==null){
            serverThread = new Thread(new ServerThread());
            serverThread.start();
        }
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", new Object[]{s});
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", new Object[]{s});
    }

    private static final PrinterDitronThread instance = new PrinterDitronThread();

    public PrinterDitronThread(){}

    public static PrinterDitronThread getInstance() {
        return instance;
    }

    public void closeAll(){
        if(socket!=null) {
            try {
                socket.close();
                socket = null;
                Log.i(TAG, "socket close");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(outputStw!=null) {
                    this.outputStw.close();
                    outputStw = null;
                }
                Log.i(TAG, "output close");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(bufferW!=null) {
                    bufferW.close();
                    bufferW = null;
                }
                Log.i(TAG, "bufferw close");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(outW!=null) {
                this.outW.close();
                outW = null;
            }
            Log.i(TAG, "outw close");
            try {
                if(bufferIn!=null) {
                    this.bufferIn.close();
                    bufferIn = null;
                }
                Log.i(TAG, "bufferin close");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public final static ReentrantLock lockhead = new ReentrantLock();

    private boolean isRunning = true;


    private class ServerThread extends Thread{
        public void run() {
            while(true) {
                if(isRunning && nowPrinting){
                    synchronized (lockhead){
                        isRunning = false;
                        executePrinter("sync");
                    }
                }
            }
        }
    }

   /* @Override
    protected String doInBackground(String... strings) {
        closeAll();
        this.address = new InetSocketAddress(this.IPSERVER, this.PORTASERVER);
        this.socket = new Socket();
        try {
            this.socket.connect(this.address, 6000);
            Log.i(TAG, "socket connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.outputStw = new OutputStreamWriter(this.socket.getOutputStream());
            Log.i(TAG, "output connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.bufferW = new BufferedWriter(this.outputStw);
        this.outW = new PrintWriter(this.bufferW, true);
        try {
            this.bufferIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            Log.i(TAG, "bufferin connected");
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (metodoStampa){
            case 1 :
                printBill();
                break;
            default :
                break;
        }



        //printScontrino();
        this.outW.flush();

        return null;
    }*/

    public boolean checkPrinterDitron(){
        FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
        IPSERVER = myFiscal.getAddress();
        PORTASERVER = myFiscal.getPort();
        this.address = new InetSocketAddress(this.IPSERVER, this.PORTASERVER);
        this.socket = new Socket();
        if(socket.isConnected()) return true;
        else return false;
    }


    public void startSocket(){
        FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
        IPSERVER = myFiscal.getAddress();
        PORTASERVER = myFiscal.getPort();
        this.address = new InetSocketAddress(this.IPSERVER, this.PORTASERVER);
        this.socket = new Socket();
        try {
            this.socket.connect(this.address, 6000);
            Log.i(TAG, "socket connected");
            this.outputStw = new OutputStreamWriter(this.socket.getOutputStream());
            Log.i(TAG, "output connected");
            this.bufferW = new BufferedWriter(this.outputStw);
            this.outW = new PrintWriter(this.bufferW, true);

            this.bufferIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            Log.i(TAG, "bufferin connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    protected String executePrinter(String runBaby) {
        // synchronized (socket) {

            /*this.address = new InetSocketAddress(this.IPSERVER, this.PORTASERVER);
            this.socket = new Socket();
            try {
                this.socket.connect(this.address, 6000);
                Log.i(TAG, "socket connected");
                this.outputStw = new OutputStreamWriter(this.socket.getOutputStream());
                Log.i(TAG, "output connected");
                this.bufferW = new BufferedWriter(this.outputStw);
                this.outW = new PrintWriter(this.bufferW, true);

                this.bufferIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                Log.i(TAG, "bufferin connected");
            } catch (IOException e) {
                    e.printStackTrace();
            }*/

        switch (metodoStampa) {
            case 1:
                if(outW!=null)
                    printBill();
                break;
            case 2:
                if(outW!=null)
                    printBillDiscount();
                break;
            case 3 :
                if(outW!=null)
                    printFiscalBillPartial();
                break;
            case 15 :
                if(outW!=null)
                    printFiscalBillPartialList();
                break;
            case 4 :
                if(outW!=null)
                    printNonFiscal();
                break;
            case 13 :
                if(outW!=null)
                    printReport();
                break;
            case 14 :
                if(outW!=null)
                    apriCassetto();
                break;
            case 16 :
                if(outW!=null)
                    printInvoice();
                break;
            default:
                break;
        }


        //printScontrino();
        if(outW!=null)
            this.outW.flush();
        isRunning = true;
        nowPrinting = false;



        return null;
    }

    private void printInvoice() {
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        float totAbbuono = 0.0f;
        float valSconto = 0.0f;
        float totale = 0.0f;
        for(int i =0; i<2; i++) {
            this.outW.print("AUTH " + this.auth64 + "\r\n");
            this.outW.print("BATCHSTART\r\n");
            this.outW.print("CLEAR\r\n");
            this.outW.print("CHIAVE REG\r\n");
            this.outW.print("INP TERM=TSAPCA\r\n");
            this.outW.print("NOFIS APRI, SOLOSCO\r\n");

            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            this.outW.print("NOFIS RIGA='Fattura Di Cortesia: " + year + "/" + numeroFattura + "/" +StaticValue.shopName+"',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='Data: " + day + "/" + month + "/" + year + "',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");


            //Stampa Fattura
            if (clientInfo.getCodice_fiscale().equals("")) {
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_vat_number() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_name() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_address() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_postal_code() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_city() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_country() + "',OPZ=0 ,SOLOSCO\r\n");


            } else {
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_vat_number() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCodice_fiscale() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getName() + " " + clientInfo.getSurname() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_address() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_postal_code() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_city() + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='" + clientInfo.getCompany_country() + "',OPZ=0 ,SOLOSCO\r\n");


            }


            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
            if (tableNumber != -1 && tableNumber != -11) {
                this.outW.print("NOFIS RIGA='STANZA" + padLeft(roomName, 16) + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='TAVOLO" + padLeft(String.valueOf(tableNumber), 16) + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
            } else {
                this.outW.print("NOFIS RIGA='NUMERO ORDINE" + padLeft(orderNumber, 16) + "',OPZ=0 ,SOLOSCO\r\n");
                this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");

            }
            for (CashButtonLayout cashButton : products) {
                if (cashButton.getQuantityInt() > 1)
                    this.outW.print("NOFIS RIGA='" + cashButton.getQuantity() + "X" + cashButton.getPriceFloat() + "',OPZ=0 ,SOLOSCO\r\n");
                else
                    this.outW.print("NOFIS RIGA='" + padRight(df_2.format((double) cashButton.getQuantityInt()), 8)
                            + padRight(cashButton.getTitle(), 20)
                            + padLeft(df_1.format((double) cashButton.getPriceFloat()), 8)
                            + "',OPZ=0, SOLOSCO\r\n");
                ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                if (mList != null) {
                    for (CashButtonListLayout m : mList) {
                        if (m.getQuantityInt() > 1)
                            this.outW.print("NOFIS RIGA='" + m.getQuantity() + "X" + m.getPriceFloat() + "',OPZ=0 ,SOLOSCO\r\n");
                        else
                            this.outW.print("NOFIS RIGA='" + padRight(df_2.format((double) m.getQuantityInt()), 8)
                                    + padRight(m.getTitle(), 20)
                                    + padLeft(df_1.format((double) m.getPriceFloat()), 8)
                                    + "',OPZ=0, SOLOSCO\r\n");
                    }
                }

            }
            this.outW.print("NOFIS RIGA='------------------------------------',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='" + padRight("TOTALE", 20) + padLeft(df_1.format((double) cost), 16) + "',OPZ=0, SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='------------------------------------',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS CHIUDI\r\n");
            this.outW.print("INP TERM=TSAPCA\r\n");
            this.outW.print("BATCHEND\r\n");
        }

    }

    public void executeAction(){
        this.outW.flush();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean codifica64() {
        try {
            this.auth64 = Base64.encodeToString((this.user + ":" + this.pwd).getBytes(AsyncHttpResponseHandler.DEFAULT_CHARSET), 0);
            return true;
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return false;
        }
    }
    public void Impostacontout() {
        this.contout = 0;
    }

    public void printBill() {
        Impostacontout();
        int i;
        float prezzo;
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df = new DecimalFormat("##.00");
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        this.outW.print("AUTH " + this.auth64 + "\r\n");
        this.outW.print("BATCHSTART\r\n");
        this.outW.print("CLEAR\r\n");
        this.outW.print("CHIAVE REG\r\n");
        this.outW.print("INP TERM=TSAPCA\r\n");
        // this.outW.print("VEND REP=" + "1"+ ",PREZZO=" + ((int) 1000) + ",QTY='" + 1+ "',DES='" + "articolo" + "'\r\n");
       /* //this.outW.print("PERCA ALI=" + ((Scontrino) this.scontrino.get(i)).getValScMg() + "\r\n");
        String riga = "";
        riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
        this.outW.print(new StringBuilder(String.valueOf(riga)).append("\r\n").toString());*/

        for (CashButtonLayout cashButton : products) {
            int myVat = cashButton.getVat()+1;
            if (cashButton.getQuantityInt() > 1)
                this.outW.print("VEND REP=" + myVat + ",PREZZO=" + ((int) (cashButton.getPriceFloat()*100)) + ",QTY='" + cashButton.getQuantity()+ "',DES='" + cashButton.getTitle()+ "'\r\n");
            else
                this.outW.print("VEND REP=" + myVat + ",PREZZO=" + ((int) (cashButton.getPriceFloat()*100))  + ",DES='" + cashButton.getTitle() + "'\r\n");
            ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
            if (mList != null) {
                for (CashButtonListLayout m : mList) {
                    int myVat1 = cashButton.getVat()+1;
                    if (m.getQuantityInt() > 1)
                        this.outW.print("VEND REP=" +myVat1 + ",PREZZO=" + ((int) (m.getPriceFloat()*100)) + ",QTY='" + m.getQuantity()+ "',DES='" + m.getTitle()+ "'\r\n");
                    else
                        this.outW.print("VEND REP=" + myVat1+ ",PREZZO=" + ((int) (m.getPriceFloat()*100))  + ",DES='" + m.getTitle() + "'\r\n");

                }
            }

        }
        //this.outW.print("PERCA ALI=" + ((Scontrino) this.scontrino.get(i)).getValScMg() + "\r\n");
        String riga = "";
        if(paymentType==1){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=1").toString();
                long pagato = Math.round((paid) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=1").toString();
                long pagato = Math.round((paid) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();

            }
        }else if(paymentType==2){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=4").toString();
                long pagato = Math.round((cost-credit) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=4").toString();
                long pagato = Math.round((cost) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }
        }else if(paymentType==4){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
                long pagato = Math.round((paid) * 100);
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
                long pagato = Math.round((paid) * 100);
            }
        }
        this.outW.print("ALLEG ON\r\n");
        this.outW.print(new StringBuilder(String.valueOf(riga)).append("\r\n").toString());
        if(creditL>0){
            this.outW.print("ALLEG RIGA='CREDITO " + creditL + " €  "+ billId + "   ',OPZ=1\r\n");

        }
        this.outW.print("ALLEG FINE\r\n");

        this.outW.print("BATCHEND\r\n");
        // executeAction();
    }

    public void setMetodoStampa(int metodoStampa) {
        this.metodoStampa = metodoStampa;
    }


    public String printBillDiscount() {

        Impostacontout();
        int i;
        float prezzo;
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df = new DecimalFormat("##.00");
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        this.outW.print("AUTH " + this.auth64 + "\r\n");
        this.outW.print("BATCHSTART\r\n");
        this.outW.print("CLEAR\r\n");
        this.outW.print("CHIAVE REG\r\n");
        this.outW.print("INP TERM=TSAPCA\r\n");
        // this.outW.print("VEND REP=" + "1"+ ",PREZZO=" + ((int) 1000) + ",QTY='" + 1+ "',DES='" + "articolo" + "'\r\n");
       /* //this.outW.print("PERCA ALI=" + ((Scontrino) this.scontrino.get(i)).getValScMg() + "\r\n");
        String riga = "";
        riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
        this.outW.print(new StringBuilder(String.valueOf(riga)).append("\r\n").toString());*/

        for (CashButtonLayout cashButton : products) {
            int myVat = cashButton.getVat()+1;
            if (cashButton.getQuantityInt() > 1)
                this.outW.print("VEND REP=" + myVat + ",PREZZO=" + ((int) (cashButton.getPriceFloat()*100)) + ",QTY='" + cashButton.getQuantity()+ "',DES='" + cashButton.getTitle()+ "'\r\n");
            else
                this.outW.print("VEND REP=" + myVat + ",PREZZO=" + ((int) (cashButton.getPriceFloat()*100))  + ",DES='" + cashButton.getTitle() + "'\r\n");
            ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
            if (mList != null) {
                for (CashButtonListLayout m : mList) {
                    int myVat1 = cashButton.getVat()+1;
                    if (m.getQuantityInt() > 1)
                        this.outW.print("VEND REP=" + myVat1+ ",PREZZO=" + ((int) (m.getPriceFloat()*100)) + ",QTY='" + m.getQuantity()+ "',DES='" + m.getTitle()+ "'\r\n");
                    else
                        this.outW.print("VEND REP=" + myVat1 + ",PREZZO=" + ((int) (m.getPriceFloat()*100))  + ",DES='" + m.getTitle() + "'\r\n");

                }
            }

        }
        if(discountTotal!=0.0f)
            this.outW.print("SCONTO VAL=" + ((int) ((discountTotal * 100) )) + ",SUBTOT\r\n");
        //this.outW.print("PERCA ALI=" + ((Scontrino) this.scontrino.get(i)).getValScMg() + "\r\n");
        String riga = "";
        if(paymentType==1){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=1").toString();
                long pagato = Math.round((paid) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=1").toString();
                long pagato = Math.round((paid) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();

            }
        }else if(paymentType==2){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=4").toString();
                long pagato = Math.round((cost-credit) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=4").toString();
                long pagato = Math.round((cost) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }
        }else if(paymentType==4){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
                long pagato = Math.round((paid) * 100);
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
                long pagato = Math.round((paid) * 100);
            }
        }
        this.outW.print("ALLEG ON\r\n");
        this.outW.print(new StringBuilder(String.valueOf(riga)).append("\r\n").toString());
        if(creditL>0){
            this.outW.print("ALLEG RIGA='CREDITO " + creditL + " €  "+ billId + "   ',OPZ=1\r\n");

        }
        this.outW.print("ALLEG FINE\r\n");

        this.outW.print("BATCHEND\r\n");
        return "";
    }

    public String printNonFiscal() {
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        float totAbbuono = 0.0f;
        float valSconto = 0.0f;
        float totale = 0.0f;
        this.outW.print("AUTH " + this.auth64 + "\r\n");
        this.outW.print("BATCHSTART\r\n");
        this.outW.print("CLEAR\r\n");
        this.outW.print("CHIAVE REG\r\n");
        this.outW.print("INP TERM=TSAPCA\r\n");
        this.outW.print("NOFIS APRI, SOLOSCO\r\n");
        this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        if(tableNumber!=-1 && tableNumber!=-11) {
            this.outW.print("NOFIS RIGA='STANZA"+ padLeft(roomName, 16)+ "',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='TAVOLO"+ padLeft(String.valueOf(tableNumber), 16)+ "',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        }else{
            this.outW.print("NOFIS RIGA='NUMERO ORDINE"+ padLeft(orderNumber, 16)+ "',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");

        }
        for (CashButtonLayout cashButton : products) {
            if (cashButton.getQuantityInt() > 1)
                this.outW.print("NOFIS RIGA='"+cashButton.getQuantity()+"X"+cashButton.getPriceFloat()+"',OPZ=0 ,SOLOSCO\r\n");
            else
                this.outW.print("NOFIS RIGA='" + padRight(df_2.format((double) cashButton.getQuantityInt()), 8)
                        + padRight(cashButton.getTitle(), 20)
                        + padLeft(df_1.format((double) cashButton.getPriceFloat()), 8)
                        + "',OPZ=0, SOLOSCO\r\n");
            ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
            if (mList != null) {
                for (CashButtonListLayout m : mList) {
                    if (m.getQuantityInt() > 1)
                        this.outW.print("NOFIS RIGA='"+m.getQuantity()+"X"+m.getPriceFloat()+"',OPZ=0 ,SOLOSCO\r\n");
                    else
                        this.outW.print("NOFIS RIGA='" + padRight(df_2.format((double) m.getQuantityInt()), 8)
                                + padRight(m.getTitle(), 20)
                                + padLeft(df_1.format((double) m.getPriceFloat()), 8)
                                + "',OPZ=0, SOLOSCO\r\n");
                }
            }

        }
        this.outW.print("NOFIS RIGA='------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        this.outW.print("NOFIS RIGA='" + padRight("TOTALE", 20) + padLeft(df_1.format((double) cost), 16) + "',OPZ=0, SOLOSCO\r\n");
        this.outW.print("NOFIS RIGA='------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        this.outW.print("NOFIS CHIUDI\r\n");
        this.outW.print("INP TERM=TSAPCA\r\n");
        this.outW.print("BATCHEND\r\n");

        return "";
    }

    public String printFiscalBillPartial() {
        Impostacontout();
        int i;
        float prezzo;
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df = new DecimalFormat("##.00");
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        this.outW.print("AUTH " + this.auth64 + "\r\n");
        this.outW.print("BATCHSTART\r\n");
        this.outW.print("CLEAR\r\n");
        this.outW.print("CHIAVE REG\r\n");
        this.outW.print("INP TERM=TSAPCA\r\n");
        this.outW.print("VEND REP=" + "1" + ",PREZZO=" + ((int) (cost*100)) + ",QTY='" + 1+ "',DES='" +description+ "'\r\n");
        String riga = "";
        if(paymentType==1){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=1").toString();
                long pagato = Math.round((paid) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=1").toString();
                long pagato = Math.round((paid) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();

            }
        }else if(paymentType==2){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=4").toString();
                long pagato = Math.round((cost-credit) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=4").toString();
                long pagato = Math.round((cost) * 100);
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) pagato).toString();
            }
        }else if(paymentType==4){
            if(credit!=0.0) {
                this.outW.print("SCONTO VAL=" + ((int) (credit*100)) + ",SUBTOT\r\n");
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
                long pagato = Math.round((paid) * 100);
            }else{
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
                long pagato = Math.round((paid) * 100);
            }
        }
        this.outW.print(new StringBuilder(String.valueOf(riga)).append("\r\n").toString());
        this.outW.print("BATCHEND\r\n");
        return "";

    }

    public String printFiscalBillPartialList() {
        Impostacontout();
        int i;
        float prezzo;
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df = new DecimalFormat("##.00");
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        this.outW.print("AUTH " + this.auth64 + "\r\n");
        for(LeftPayment myLeft : left) {

            this.outW.print("BATCHSTART\r\n");
            this.outW.print("CLEAR\r\n");
            this.outW.print("CHIAVE REG\r\n");
            this.outW.print("INP TERM=TSAPCA\r\n");
            if(myLeft.getPaid()<myLeft.getCost())
                this.outW.print("VEND REP=" + "1" + ",PREZZO=" + ((int) (myLeft.getPaid()* 100)) + ",QTY='" + 1 + "',DES='PER AMOUNT'\r\n");
            else
                this.outW.print("VEND REP=" + "1" + ",PREZZO=" + ((int) (myLeft.getCost()* 100)) + ",QTY='" + 1 + "',DES='PER AMOUNT'\r\n");

            String riga = "";
            if (myLeft.getPaymentType() == 1) {
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=1").toString();
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) (myLeft.getPaid()*100)).toString();


            } else if (myLeft.getPaymentType() == 2) {
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=4").toString();
                riga = new StringBuilder(String.valueOf(riga)).append(",IMP=").append((int) (myLeft.getPaid()*100)).toString();

            } else if (myLeft.getPaymentType() == 4) {
                riga = new StringBuilder(String.valueOf(riga)).append("CHIUS T=5").toString();
            }
            this.outW.print(new StringBuilder(String.valueOf(riga)).append("\r\n").toString());
            this.outW.print("BATCHEND\r\n");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.outW.print("BATCHSTART\r\n");
        this.outW.print("CLEAR\r\n");
        this.outW.print("CHIAVE REG\r\n");
        this.outW.print("INP TERM=TSAPCA\r\n");
        this.outW.print("NOFIS APRI, SOLOSCO\r\n");
        this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        if(tableNumber!=-1 && tableNumber!=-11) {
            this.outW.print("NOFIS RIGA='STANZA"+ padLeft(roomName, 16)+ "',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='TAVOLO"+ padLeft(String.valueOf(tableNumber), 16)+ "',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        }else{
            this.outW.print("NOFIS RIGA='NUMERO ORDINE"+ padLeft(orderNumber, 16)+ "',OPZ=0 ,SOLOSCO\r\n");
            this.outW.print("NOFIS RIGA='----------------------------------------',OPZ=0 ,SOLOSCO\r\n");

        }
        for (CashButtonLayout cashButton : products) {
            if (cashButton.getQuantityInt() > 1)
                this.outW.print("NOFIS RIGA='"+cashButton.getQuantity()+"X"+cashButton.getPriceFloat()+"',OPZ=0 ,SOLOSCO\r\n");
            else
                this.outW.print("NOFIS RIGA='" + padRight(df_2.format((double) cashButton.getQuantityInt()), 8)
                        + padRight(cashButton.getTitle(), 20)
                        + padLeft(df_1.format((double) cashButton.getPriceFloat()), 8)
                        + "',OPZ=0, SOLOSCO\r\n");
            ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
            if (mList != null) {
                for (CashButtonListLayout m : mList) {
                    if (m.getQuantityInt() > 1)
                        this.outW.print("NOFIS RIGA='"+m.getQuantity()+"X"+m.getPriceFloat()+"',OPZ=0 ,SOLOSCO\r\n");
                    else
                        this.outW.print("NOFIS RIGA='" + padRight(df_2.format((double) m.getQuantityInt()), 8)
                                + padRight(m.getTitle(), 20)
                                + padLeft(df_1.format((double) m.getPriceFloat()), 8)
                                + "',OPZ=0, SOLOSCO\r\n");
                }
            }

        }
        this.outW.print("NOFIS RIGA='------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        this.outW.print("NOFIS RIGA='" + padRight("TOTALE", 20) + padLeft(df_1.format((double) cost), 16) + "',OPZ=0, SOLOSCO\r\n");
        this.outW.print("NOFIS RIGA='------------------------------------',OPZ=0 ,SOLOSCO\r\n");
        this.outW.print("NOFIS CHIUDI\r\n");
        this.outW.print("INP TERM=TSAPCA\r\n");
        this.outW.print("BATCHEND\r\n");
        return "";

    }

    public void printReport(){

        switch (report) {
            case FISCAL_ZERO:
                printChiusuraGiornaliera();
                break;
            case SIMPLE_REPORT:
                printGiornale();
                break;
        }

    }

    public void apriCassetto() {
        if(outW!=null) {
            this.outW.print("AUTH " + this.auth64 + "\r\n");
            this.outW.print("BATCHSTART\r\n");
            this.outW.print("CLEAR\r\n");
            this.outW.print("INP TERM=TSAPCA\r\n");
            this.outW.print("BATCHEND\r\n");
        }

    }

    public void printGiornale() {
        this.outW.print("AUTH " + this.auth64 + "\r\n");
        this.outW.print("BATCHSTART\r\n");
        this.outW.print("CLEAR\r\n");
        this.outW.print("CHIAVE LETT\r\n");
        this.outW.print("INP NUM=1,TERM=TSSCEL\r\n");
        this.outW.print("INP TERM=TSCONF\r\n");
        this.outW.print("CHIAVE REG\r\n");
        this.outW.print("BATCHEND\r\n");

    }

    public void printChiusuraGiornaliera() {
        this.outW.print("AUTH " + this.auth64 + "\r\n");
        this.outW.print("BATCHSTART\r\n");
        this.outW.print("CLEAR\r\n");
        this.outW.print("CHIAVE AZZ\r\n");
        this.outW.print("INP TERM=TSCONF\r\n");
        this.outW.print("INP TERM=TSCONF\r\n");
        this.outW.print("CHIAVE REG\r\n");
        this.outW.print("BATCHEND\r\n");

    }

}
