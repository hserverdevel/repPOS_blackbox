package com.example.blackbox.printer;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.StaticValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import HPRTAndroidSDK.HPRTPrinterHelper;

/**
 * Created by tiziano on 2/5/19.
 */

public class KitchenPrinter  extends Application {

    private Context context;
    private int indexList;
    private String deviceName;
    private String billId;
    private String IP;
    private String orderNumber;
    private ArrayList<CashButtonLayout> products;
    private Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers;
    private ArrayList<Customer> customers;
    private String roomName;
    private int tableNumber;
    private String changedPosition;
    private CashButtonLayout oldProducts;
    private Map<CashButtonLayout,ArrayList<CashButtonListLayout>> oldModifiers;
    private Customer customer;
    private int quantity;


    public final static ReentrantLock lockhead = new ReentrantLock();
    private boolean isRunning = true;
    private Thread serverThread;
    private int metodoStampa;

    public boolean nowPrinting = false;
    private Context c;

    public int getIndexList() {
        return indexList;
    }

    public void setIndexList(int indexList) {
        this.indexList = indexList;
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

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getChangedPosition() {
        return changedPosition;
    }

    public void setChangedPosition(String changedPosition) {
        this.changedPosition = changedPosition;
    }

    public CashButtonLayout getOldProducts() {
        return oldProducts;
    }

    public void setOldProducts(CashButtonLayout oldProducts) {
        this.oldProducts = oldProducts;
    }

    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getOldModifiers() {
        return oldModifiers;
    }

    public void setOldModifiers(Map<CashButtonLayout, ArrayList<CashButtonListLayout>> oldModifiers) {
        this.oldModifiers = oldModifiers;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * PARTE SERVER THREAD
     */


    @Override
    public void onCreate() {
        super.onCreate();
        c = this;
    }

    private static final KitchenPrinter instance = new KitchenPrinter();

    public KitchenPrinter(){}

    public static KitchenPrinter getInstance() {
        return instance;
    }

    public void setPrinterThread()
    {
        if(serverThread==null)
        {
            serverThread = new Thread(new ServerThread());
            serverThread.start();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setMetodoStampa(int metodoStampa) {
        this.metodoStampa = metodoStampa;
    }

    private class ServerThread extends Thread{
        @RequiresApi(api = Build.VERSION_CODES.N)
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected String executePrinter(String runBaby) {
        switch (metodoStampa) {
            case 8:
                printBillOrder();
                break;
            case 9:
                printBillOrderCorrection();
                break;
            case 10 :
                printBillOrderCorrectionInc();
                break;
            case 11 :
                printBillOrderDelete();
                break;
            case 12 :
                reprintBillOrder();
                break;
            default:
                break;
        }
        isRunning = true;
        nowPrinting = false;
        return null;
    }

    /**
     * METODI DI STAMPA
     */


    public String printBillOrderDelete(

    ) {
            ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
            CashButtonLayout cashButton = oldProducts;
            if(oldProducts.getPrinterId()!=-1) {
                HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(c, "TP805");
                try {
                    //KitchenPrinter kitchenPrinter = StaticValue.kitchenPrinterClass;
                    ArrayList<com.example.blackbox.model.KitchenPrinter> kPrinters = StaticValue.kitchenPrinterClass;
                    if(kPrinters.size()>0) {
                        for (com.example.blackbox.model.KitchenPrinter kitchenPrinter : kPrinters) {
                            //KitchenPrinter kitchenPrinter = new KitchenPrinter();
                            if (oldProducts.getPrinterId() == kitchenPrinter.getId()) {
                                hrphelper.PortOpen("WiFi," + kitchenPrinter.getAddress() + "," + String.valueOf(kitchenPrinter.getPort()));
                                //hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                                HPRTPrinterHelper.SetCharacterSet((byte) 0);
                                HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                                hrphelper.PrintText(" ANNULLO ORDINE " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                                hrphelper.PrintAndLineFeed();
                                if (!customer.getDescription().equals("")) {
                                    hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                                    hrphelper.PrintAndLineFeed();
                                }
                                if (oldProducts.getQuantityInt() > 1) {
                                    for (int i = 1; i <= oldProducts.getQuantityInt(); i++) {

                                        hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 32);
                                        hrphelper.PrintAndLineFeed();
                                        if (modifiers.size() > 0) {
                                            for (CashButtonListLayout m : modifiers.get(oldProducts)) {
                                                hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);

                                            }
                                        }
                                    }
                                } else {
                                    hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 32);
                                    hrphelper.PrintAndLineFeed();
                                    if (modifiers.size() > 0) {
                                        for (CashButtonListLayout m : modifiers.get(oldProducts)) {
                                            hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);

                                        }
                                    }

                                }
                                hrphelper.PrintAndLineFeed();
                                hrphelper.PrintAndLineFeed();
                                hrphelper.CutPaper(1, 1600);
                            }
                        }
                    }
                    return "02-00";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "02-02";
                }
            }
            return  "02-00";

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printBillOrderCorrectionInc() {
        String returnValue = "";
            ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
            CashButtonLayout cashButton = oldProducts;

        ArrayList<com.example.blackbox.model.KitchenPrinter> kPrinters = StaticValue.kitchenPrinterClass;
        if(kPrinters.size()>0) {
            for (com.example.blackbox.model.KitchenPrinter kitchenPrinter : kPrinters) {
                //KitchenPrinter kitchenPrinter = new KitchenPrinter();
                if (oldProducts.getPrinterId() == kitchenPrinter.getId()) {
                    //if(oldProducts.getPrinterId()!=-1) {
                    HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
                    if (cashButton.getQuantityInt() > quantity) {
                        try {
                            //KitchenPrinter kitchenPrinter = StaticValue.kitchenPrinterClass;
                            //KitchenPrinter kitchenPrinter = new KitchenPrinter();
                            hrphelper.PortOpen("WiFi," + kitchenPrinter.getAddress() + "," + String.valueOf(kitchenPrinter.getPort()));
                            //hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                            int toStart = 1;
                            if (cashButton.getQuantityInt() - quantity == 1) toStart = quantity + 1;
                            else toStart = quantity + 1;
                            for (int i = 1; i <= quantity; i++) {
                                HPRTPrinterHelper.SetCharacterSet((byte) 0);
                                HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                                hrphelper.PrintText(" NUMERO ORDINE : " + deviceName.toUpperCase() + " " + orderNumber, 1, 48, 0);
                                HPRTPrinterHelper.PrintAndLineFeed();
                                if (!customer.getDescription().equals("")) {
                                    hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                                    hrphelper.PrintAndLineFeed();
                                }
                                hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 32);
                                hrphelper.PrintAndLineFeed();
                                ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                // mList1 = modifiers.get(cashButton);
                                if (modifiers.get(cashButton) != null) {
                                    for (CashButtonListLayout m : modifiers.get(cashButton)) {
                                        mList.add(m);
                                    }
                                }
                                if (mList != null) {
                                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                    for (CashButtonListLayout m : mList) {
                                        if (m.getModifierId() != -15) {
                                            if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                                hrphelper.PrintText((m.getQuantityInt() / cashButton.getQuantityInt()) + " x 1", 1, 0, 0);
                                            }
                                            hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                        } else {
                                            notes.add(m);
                                        }
                                    }
                                }
                                Date date = new Date();

                                hrphelper.PrintAndLineFeed();
                                hrphelper.PrintAndLineFeed();
                                if (notes.size() > 0) {
                                    for (int j = 0; j < notes.size(); j++) {
                                        hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 16);
                                    }
                                    hrphelper.PrintAndLineFeed();
                                    hrphelper.PrintAndLineFeed();
                                }
                                hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                                hrphelper.PrintAndLineFeed();
                                hrphelper.PrintAndLineFeed();
                                hrphelper.CutPaper(1, 1600);
                                returnValue = "03-00";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            returnValue = "03-02";
                        }
                    } else {
                        try {
                            //KitchenPrinter kitchenPrinter = StaticValue.kitchenPrinterClass;
                            //KitchenPrinter kitchenPrinter = new KitchenPrinter();
                            hrphelper.PortOpen("WiFi," + kitchenPrinter.getAddress() + "," + String.valueOf(kitchenPrinter.getPort()));
                            //hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                            for (int i = quantity; i > oldProducts.getQuantityInt(); i--) {
                                hrphelper.PrintText(" ANNULLO ORDINE " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                                HPRTPrinterHelper.PrintAndLineFeed();
                                if (!customer.getDescription().equals("")) {
                                    hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                                    hrphelper.PrintAndLineFeed();
                                }
                                hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                                hrphelper.PrintAndLineFeed();
                                ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                if (modifiers.get(cashButton) != null) {
                                    for (CashButtonListLayout m : modifiers.get(cashButton)) {
                                        mList.add(m);
                                    }
                                }
                                if (mList != null) {
                                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                    for (CashButtonListLayout m : mList) {
                                        if (m.getModifierId() != -15) {
                                            if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                                hrphelper.PrintText((m.getQuantityInt() / cashButton.getQuantityInt()) + " x 1", 1, 0, 0);
                                            }
                                            hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                        } else {
                                            notes.add(m);
                                        }
                                    }
                                }
                                Date date = new Date();
                                hrphelper.PrintAndLineFeed();
                                hrphelper.PrintAndLineFeed();
                                if (notes.size() > 0) {
                                    for (int j = 0; j < notes.size(); j++) {
                                        hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 16);
                                    }
                                    hrphelper.PrintAndLineFeed();
                                    hrphelper.PrintAndLineFeed();
                                }
                                hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                                hrphelper.PrintAndLineFeed();
                                hrphelper.PrintAndLineFeed();
                                hrphelper.CutPaper(1, 1600);
                                returnValue = "03-00";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            returnValue = "03-02";
                        }
                    }
                }
            }
        }

        return returnValue;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printBillOrderCorrection() {
        ArrayList<com.example.blackbox.model.KitchenPrinter> kPrinters = StaticValue.kitchenPrinterClass;
        if(kPrinters.size()>0) {
            for (com.example.blackbox.model.KitchenPrinter kitchenPrinter : kPrinters) {
                //KitchenPrinter kitchenPrinter = new KitchenPrinter();
                if (oldProducts.getPrinterId() == kitchenPrinter.getId()) {

                    //if(oldProducts.getPrinterId()!=-1) {
                    HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
                    try {
                        hrphelper.PortOpen("WiFi," + kitchenPrinter.getAddress() + "," + String.valueOf(kitchenPrinter.getPort()));
                        hrphelper.PrintText(" ANNULLO ORDINE " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                        hrphelper.PrintAndLineFeed();
                        if (!customer.getDescription().equals("")) {
                            hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                            hrphelper.PrintAndLineFeed();
                        }
                        if (oldProducts.getQuantityInt() > 1) {
                            for (int i = 1; i <= oldProducts.getQuantityInt(); i++) {
                                hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 32);
                                hrphelper.PrintAndLineFeed();
                                if (oldModifiers.get(oldProducts) != null) {
                                    for (CashButtonListLayout m : oldModifiers.get(oldProducts)) {
                                        hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                    }
                                }
                            }
                        } else {
                            hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 32);
                            hrphelper.PrintAndLineFeed();
                            if (oldModifiers.containsKey(oldProducts)) {
                                if (oldModifiers.get(oldProducts) != null) {
                                    for (CashButtonListLayout m : oldModifiers.get(oldProducts)) {
                                        hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                    }
                                }
                            }
                        }
                        hrphelper.PrintAndLineFeed();
                        hrphelper.PrintAndLineFeed();

                        Date date = new Date();
                        hrphelper.PrintAndLineFeed();
                        hrphelper.PrintAndLineFeed();

                        hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                        hrphelper.CutPaper(1, 1600);
                        hrphelper.PortClose();
                        return "01-00";
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "01-02";
                    }
                }
            }
        }
            return  "01-00";

    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printBillOrder(

    ) {
        Log.i("PRINT ORDER BILL", "0");
        if (products.size() > 0) {
            Log.i("PRINT ORDER BILL", "1");
            //  HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP80A");
            HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(c, "TP805");
            try {
                Log.i("PRINT ORDER BILL", "2");
                ArrayList<com.example.blackbox.model.KitchenPrinter> kPrinters = StaticValue.kitchenPrinterClass;

                if(kPrinters.size()>0)
                {
                    for (com.example.blackbox.model.KitchenPrinter kitchenPrinter : kPrinters)
                    {
                        //KitchenPrinter kitchenPrinter = StaticValue.kitchenPrinterClass;

                        int connection = hrphelper.PortOpen("WiFi," + kitchenPrinter.getAddress() + "," + String.valueOf(kitchenPrinter.getPort()));

                        //hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                        Log.i("CONNECTION", "" + connection);
                        ArrayList<CashButtonLayout> pList = products;
                        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiersList = modifiers;
                        if (kitchenPrinter.isSingleOrder()==1) {
                            for (CashButtonLayout cashButton : pList) {
                                if (cashButton.getPrinterId() == kitchenPrinter.getId()) {

                                    ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
                                    if (cashButton.getQuantityInt() == 1) {
                                        // ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                        //mList = modifiersList.get(cashButton);
                                        ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                        if (modifiersList.get(cashButton) != null) {
                                            for (CashButtonListLayout m : modifiersList.get(cashButton)) {
                                                mList.add(m);
                                            }
                                        }
                                        HPRTPrinterHelper.SetCharacterSet((byte) 0);
                                        HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                                        hrphelper.PrintText(" NUMERO ORDINE : " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                                        if (tableNumber > 0) {
                                            hrphelper.PrintText(roomName + " " + tableNumber, 1, 48, 0);
                                        }
                                        if (customers.size() > 0) {
                                            hrphelper.PrintText(" NOME : " + customers.get(cashButton.getClientPosition() - 1).getDescription(), 1, 12, 0);
                                        }
                                        HPRTPrinterHelper.PrintAndLineFeed();
                                        if (cashButton.getQuantityInt() > 1) {
                                            hrphelper.PrintText(cashButton.getQuantity() + " x 1", 0, 0, 32);
                                        }
                                        hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 32);
                                        hrphelper.PrintAndLineFeed();
                                        if (mList != null) {
                                            //mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                            //mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                            for (CashButtonListLayout m : mList) {
                                                if (m.getModifierId() != -15) {
                                                    if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                                        hrphelper.SetJustification(0);
                                                        hrphelper.PrintText(m.getQuantityInt() + " x 1", 1, 0, 0);

                                                    }
                                                    hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                                } else {
                                                    notes.add(m);
                                                }
                                            }
                                        }
                                        Date date = new Date();
                                        hrphelper.PrintAndLineFeed();
                                        hrphelper.PrintAndLineFeed();
                                        if (notes.size() > 0) {
                                            for (int i = 0; i < notes.size(); i++) {
                                                hrphelper.PrintText(notes.get(i).getNote().toUpperCase(), 1, 0, 16);
                                            }
                                            hrphelper.PrintAndLineFeed();
                                            hrphelper.PrintAndLineFeed();
                                        }
                                        hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                                        hrphelper.CutPaper(1, 1600);
                                    } else {
                                        for (int i = 1; i <= cashButton.getQuantityInt(); i++) {
                                            HPRTPrinterHelper.SetCharacterSet((byte) 0);
                                            HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                                            hrphelper.PrintText(" NUMERO ORDINE : " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                                            if (tableNumber > 0) {
                                                hrphelper.PrintText(roomName + " " + tableNumber, 1, 48, 0);
                                            }
                                            if (customers.size() > 0) {
                                                hrphelper.PrintText(" NOME : " + customers.get(cashButton.getClientPosition() - 1).getDescription(), 1, 12, 0);
                                            }
                                            HPRTPrinterHelper.PrintAndLineFeed();
                                            hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 32);
                                            hrphelper.PrintAndLineFeed();
                                            ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                            if (modifiersList.get(cashButton) != null) {
                                                for (CashButtonListLayout m : modifiersList.get(cashButton)) {
                                                    mList.add(m);
                                                }
                                            }
                                            //ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                            if (mList != null) {
                                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                                for (CashButtonListLayout m : mList) {
                                                    if (m.getModifierId() != -15) {
                                                        if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                                            hrphelper.PrintText((m.getQuantityInt() / cashButton.getQuantityInt()) + " x 1", 1, 0, 0);
                                                        }
                                                        hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                                    } else {
                                                        notes.add(m);
                                                    }
                                                }
                                            }

                                            Date date = new Date();
                                            hrphelper.PrintAndLineFeed();
                                            hrphelper.PrintAndLineFeed();
                                            if (notes.size() > 0) {
                                                for (int j = 0; j < notes.size(); j++) {
                                                    hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 16);
                                                }
                                                hrphelper.PrintAndLineFeed();
                                                hrphelper.PrintAndLineFeed();
                                            }
                                            hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                                            hrphelper.CutPaper(1, 1600);
                                        }

                                    }
                                }
                            }
                        } else {

                            boolean printSomenthing = true;
                            for (CashButtonLayout cashButton : pList) {
                                if (cashButton.getPrinterId() == kitchenPrinter.getId()) {
                                    if(printSomenthing) {
                                        HPRTPrinterHelper.SetCharacterSet((byte) 0);
                                        HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                                        hrphelper.PrintText(" NUMERO ORDINE : " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                                        if (tableNumber > 0) {
                                            hrphelper.PrintText(roomName + " " + tableNumber, 1, 48, 0);
                                        }

                                        HPRTPrinterHelper.PrintAndLineFeed();
                                        HPRTPrinterHelper.PrintAndLineFeed();
                                        HPRTPrinterHelper.PrintAndLineFeed();
                                        printSomenthing= false;
                                    }

                                    ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                    if (modifiersList.get(cashButton) != null) {
                                        for (CashButtonListLayout m : modifiersList.get(cashButton)) {
                                            mList.add(m);
                                        }
                                    }
                                    if (cashButton.getQuantityInt() > 1) {
                                        hrphelper.PrintText(cashButton.getQuantity() + " x 1", 1, 0, 32);
                                    }
                                    hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 32);
                                    hrphelper.PrintAndLineFeed();
                                    ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
                                    if (mList != null) {
                                        for (CashButtonListLayout m : mList) {
                                            if (m.getModifierId() != -15) {
                                                if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                                    hrphelper.SetJustification(0);
                                                    hrphelper.PrintText(m.getQuantityInt() + " x 1", 1, 0, 16);

                                                }
                                                hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                            } else {
                                                notes.add(m);
                                            }
                                        }
                                    }
                                    hrphelper.PrintAndLineFeed();
                                    hrphelper.PrintAndLineFeed();
                                    if (notes.size() > 0) {
                                        for (int j = 0; j < notes.size(); j++) {
                                            hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 16);
                                        }
                                        hrphelper.PrintAndLineFeed();
                                        hrphelper.PrintAndLineFeed();
                                    }


                                }
                            }
                            if(!printSomenthing)
                                hrphelper.CutPaper(1, 1600);
                        }

                        hrphelper.PortClose();
                    }
                }
                return "04-00";

                } catch (Exception e) {
                e.printStackTrace();
                return "04-02";
            }
        } else {
            return "04-00";
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String reprintBillOrder(

    ) {
            ArrayList<CashButtonLayout> salse = new ArrayList<CashButtonLayout>();
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> salsemod = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            if (products.size() > 0) {
                HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
                try {
                    //KitchenPrinter kitchenPrinter = StaticValue.kitchenPrinterClass;
                    ArrayList<com.example.blackbox.model.KitchenPrinter> kPrinters = StaticValue.kitchenPrinterClass;
                    if(kPrinters.size()>0) {
                        for (com.example.blackbox.model.KitchenPrinter kitchenPrinter : kPrinters) {

                            Log.i("REPRINT ORDER", "WiFi," + kitchenPrinter.getAddress() + "," + String.valueOf(kitchenPrinter.getPort()));
                            hrphelper.PortOpen("WiFi," + kitchenPrinter.getAddress() + "," + String.valueOf(kitchenPrinter.getPort()));
                            //hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                            ArrayList<CashButtonLayout> pList = products;
                            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiersList = modifiers;
                            boolean printSomething = true;
                            for (CashButtonLayout cashButton : pList) {
                                if (cashButton.getPrinterId() == kitchenPrinter.getId()) {
                                    if(printSomething) {
                                        HPRTPrinterHelper.SetCharacterSet((byte) 0);
                                        HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                                        hrphelper.PrintText(" RISTAMPA ORDINE", 1, 48, 0);
                                        hrphelper.PrintText(" NUMERO ORDINE : " + (Integer.valueOf(orderNumber) - 1), 1, 48, 0);
                                        if (tableNumber > 0) {
                                            hrphelper.PrintText(roomName + " " + tableNumber, 1, 48, 0);
                                        }
                                        printSomething= false;
                                    }


                                    ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
                                    if (cashButton.getQuantityInt() == 1) {
                            /*ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                            mList = modifiersList.get(cashButton);*/
                                        ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                        if (modifiersList.get(cashButton) != null) {
                                            for (CashButtonListLayout m : modifiersList.get(cashButton)) {
                                                mList.add(m);
                                            }
                                        }
                                        if (customers.size() > 0) {
                                            hrphelper.PrintText(" NOME : " + customers.get(cashButton.getClientPosition() - 1).getDescription(), 1, 12, 0);
                                        }
                                        HPRTPrinterHelper.PrintAndLineFeed();
                                        hrphelper.SetJustification(0);
                                        if (cashButton.getQuantityInt() > 1) {
                                            hrphelper.PrintText(cashButton.getQuantity() + " x 1", 0, 0, 32);
                                        }
                                        hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 32);
                                        hrphelper.PrintAndLineFeed();
                                        if (mList != null) {
                                            mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                            mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                            for (CashButtonListLayout m : mList) {
                                                if (m.getModifierId() != -15) {
                                                    if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                                        hrphelper.PrintText(m.getQuantityInt() + " x 1", 1, 0, 16);
                                                    }
                                                    hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                                } else {
                                                    notes.add(m);
                                                }
                                            }
                                        }
                                        Date date = new Date();
                                        hrphelper.PrintAndLineFeed();
                                        hrphelper.PrintAndLineFeed();
                                        if (notes.size() > 0) {
                                            for (int i = 0; i < notes.size(); i++) {
                                                hrphelper.PrintText(notes.get(i).getNote().toUpperCase(), 1, 0, 16);
                                            }
                                            hrphelper.PrintAndLineFeed();
                                            hrphelper.PrintAndLineFeed();
                                        }
                                        hrphelper.PrintText(String.valueOf(date), 1, 0, 0);
                                        hrphelper.PrintAndLineFeed();
                                    } else {
                                        for (int i = 1; i <= cashButton.getQuantityInt(); i++) {
                                            if (customers.size() > 0) {
                                                hrphelper.PrintText(" NOME : " + customers.get(cashButton.getClientPosition() - 1).getDescription(), 1, 12, 0);
                                            }
                                            HPRTPrinterHelper.PrintAndLineFeed();
                                            hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 32);
                                            hrphelper.PrintAndLineFeed();
                                            // ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                            ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                            if (modifiersList.get(cashButton) != null) {
                                                for (CashButtonListLayout m : modifiersList.get(cashButton)) {
                                                    mList.add(m);
                                                }
                                            }
                                            if (mList != null) {
                                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                                for (CashButtonListLayout m : mList) {
                                                    if (m.getModifierId() != -15) {
                                                        if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                                            hrphelper.PrintText((m.getQuantityInt() / cashButton.getQuantityInt()) + " x 1", 1, 0, 0);
                                                        }
                                                        hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 16);
                                                    } else {
                                                        notes.add(m);
                                                    }
                                                }
                                            }
                                            Date date = new Date();
                                            hrphelper.PrintAndLineFeed();
                                            hrphelper.PrintAndLineFeed();
                                            if (notes.size() > 0) {
                                                for (int j = 0; j < notes.size(); j++) {
                                                    hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 16);
                                                }
                                                hrphelper.PrintAndLineFeed();
                                                hrphelper.PrintAndLineFeed();
                                            }
                                            hrphelper.PrintText(String.valueOf(date), 1, 0, 0);
                                            hrphelper.PrintAndLineFeed();
                                        }

                                    }

                                }
                            }
                            if(!printSomething)
                            hrphelper.CutPaper(1, 1600);
                            hrphelper.PortClose();
                        }
                    }
                    return "05-00";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "05-02";
                }
            } else {
                return "05-00";
            }

    }


}
