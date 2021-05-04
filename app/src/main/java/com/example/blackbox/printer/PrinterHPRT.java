package com.example.blackbox.printer;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.blackbox.R;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.StaticValue;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import HPRTAndroidSDK.HPRTPrinterHelper;

/**
 * Created by tiziano on 9/8/17.
 */

public class PrinterHPRT {

    int typeDisplay=1;
    private Context context;
    private Collection<CashButtonLayout> pList;
    private Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiersList;
    public static final int FISCAL_ZERO = 0;
    public static final int SIMPLE_REPORT = 1;

    public PrinterHPRT(Context context){
        this.context = context;
    }

    public String printBillOrderDelete(
            String changedPosition,
            String deviceName,
            String orderNumber,
            String IP,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            CashButtonLayout oldProducts,
            int quantity,
            Customer customer
    ) {
        if(ping(StaticValue.orderIP)) {
            ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
            CashButtonLayout cashButton = oldProducts;
            HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
            try {
                hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
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

                        hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 16);
                        hrphelper.PrintAndLineFeed();
                        if (modifiers.size() > 0) {
                            for (CashButtonListLayout m : modifiers.get(oldProducts)) {
                                hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);

                            }
                        }
                    }
                } else {
                    hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 16);
                    hrphelper.PrintAndLineFeed();
                    if (modifiers.size() > 0) {
                        for (CashButtonListLayout m : modifiers.get(oldProducts)) {
                            hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);

                        }
                    }

                }
                hrphelper.PrintAndLineFeed();
                hrphelper.PrintAndLineFeed();
                hrphelper.CutPaper(1, 1600);
                return "02-00";
            } catch (Exception e) {
                e.printStackTrace();
                return "02-02";
            }
        }else{
            return "02-01";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printBillOrderCorrectionInc(
            String changedPosition,
            String deviceName,
            String orderNumber,
            String IP,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            CashButtonLayout oldProducts,
            int quantity,
            Customer customer
    ) {
        String returnValue = "";
        if(ping(StaticValue.orderIP)) {
            ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
            CashButtonLayout cashButton = oldProducts;
            HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
            if (cashButton.getQuantityInt() > quantity) {
                try {
                    hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                    int toStart = 1;
                    if (cashButton.getQuantityInt() - quantity == 1) toStart = quantity + 1;
                    else toStart = quantity + 1;
                    for (int i = toStart; i <= cashButton.getQuantityInt(); i++) {
                        HPRTPrinterHelper.SetCharacterSet((byte) 0);
                        HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                        hrphelper.PrintText(" NUMERO ORDINE : " + deviceName.toUpperCase() + " " + orderNumber, 1, 48, 0);
                        HPRTPrinterHelper.PrintAndLineFeed();
                        if (!customer.getDescription().equals("")) {
                            hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                            hrphelper.PrintAndLineFeed();
                        }
                        hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                        hrphelper.PrintAndLineFeed();
                        ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                       // mList1 = modifiers.get(cashButton);
                        if(modifiers.get(cashButton)!=null) {
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
                                    hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 0);
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
                    hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                    for (int i = quantity; i > oldProducts.getQuantityInt(); i--) {
                        hrphelper.PrintText(" ANNULLO ORDINE " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                        HPRTPrinterHelper.PrintAndLineFeed();
                        if (!customer.getDescription().equals("")) {
                            hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                            hrphelper.PrintAndLineFeed();
                        }
                        hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                        hrphelper.PrintAndLineFeed();
                        ArrayList<CashButtonListLayout> mList =new ArrayList<CashButtonListLayout>();
                        if(modifiers.get(cashButton)!=null) {
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
                                    hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 0);
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
        }else{
            returnValue = "03-01";
        }
        return returnValue;
    }

    /**
     *
     * @param changedPosition
     * @param deviceName
     * @param orderNumber
     * @param IP
     * @param products
     * @param modifiers
     * @param oldProducts
     * @param oldModifiers
     * @param customer
     * @return printMethod 01 - errorCode (00 01 02 ...)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printBillOrderCorrection(
            String changedPosition,
            String deviceName,
            String orderNumber,
            String IP,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            CashButtonLayout oldProducts,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> oldModifiers,
            Customer customer

            ) {
        if(ping(StaticValue.orderIP)) {
            ArrayList<CashButtonLayout> salse = new ArrayList<CashButtonLayout>();
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> salsemod = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
            try {
                hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                hrphelper.PrintText(" ANNULLO ORDINE " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                hrphelper.PrintAndLineFeed();
                if (!customer.getDescription().equals("")) {
                    hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                    hrphelper.PrintAndLineFeed();
                }
                if (oldProducts.getQuantityInt() > 1) {
                    for (int i = 1; i <= oldProducts.getQuantityInt(); i++) {
                        hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 16);
                        hrphelper.PrintAndLineFeed();
                        if(oldModifiers.get(oldProducts)!=null) {
                            for (CashButtonListLayout m : oldModifiers.get(oldProducts)) {
                                hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
                            }
                        }
                    }
                } else {
                    hrphelper.PrintText(oldProducts.getTitle().toUpperCase(), 1, 0, 16);
                    hrphelper.PrintAndLineFeed();
                    if(oldModifiers.containsKey(oldProducts)) {
                        if(oldModifiers.get(oldProducts)!=null) {
                            for (CashButtonListLayout m : oldModifiers.get(oldProducts)) {
                                hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
                            }
                        }
                    }
                }
                hrphelper.PrintAndLineFeed();
                hrphelper.PrintAndLineFeed();
                /*pList = products;
                modifiersList = modifiers;
                for (CashButtonLayout cashButton : products) {
                    ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
                    if (cashButton.getQuantityInt() == 1) {
                        ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                        mList = modifiersList.get(cashButton);
                        HPRTPrinterHelper.SetCharacterSet((byte) 0);
                        HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                        hrphelper.PrintText(" NUMERO ORDINE : " + deviceName.toUpperCase() + orderNumber, 1, 48, 0);
                        HPRTPrinterHelper.PrintAndLineFeed();
                        if (!customer.getDescription().equals("")) {
                            hrphelper.PrintText(" NOME " + customer.getDescription(), 1, 12, 0);
                            hrphelper.PrintAndLineFeed();
                        }

                        if (cashButton.getQuantityInt() > 1) {
                            hrphelper.PrintText(cashButton.getQuantity() + " x 1", 0, 0, 16);

                        }
                        hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                        hrphelper.PrintAndLineFeed();

                        if (mList != null) {
                            for (CashButtonListLayout m : mList) {
                                if (m.getModifierId() != -15) {
                                    if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                        hrphelper.SetJustification(0);
                                        hrphelper.PrintText(m.getQuantityInt() + " x 1", 1, 0, 0);

                                    }
                                    hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                hrphelper.PrintText(notes.get(i).getNote().toUpperCase(), 1, 0, 0);
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
                            HPRTPrinterHelper.PrintAndLineFeed();
                            if ((deviceName.equals("A") && cashButton.getProductId() >= 47 && cashButton.getProductId() != 75) ||
                                    (deviceName.equals("B") && cashButton.getProductId() >= 15 && cashButton.getProductId() != 45)
                                    || (deviceName.equals("C") && cashButton.getProductId() >= 13)) {

                                salse.add(cashButton);
                                salsemod.put(cashButton, modifiers.get(cashButton));

                            }
                            hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                            hrphelper.PrintAndLineFeed();
                            ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                            if (mList != null) {
                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                for (CashButtonListLayout m : mList) {
                                    if (m.getModifierId() != -15) {
                                        if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                            hrphelper.PrintText((m.getQuantityInt() / cashButton.getQuantityInt()) + " x 1", 1, 0, 0);

                                        }
                                        hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                    hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 0);
                                }
                                hrphelper.PrintAndLineFeed();
                                hrphelper.PrintAndLineFeed();
                            }
                            hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                            hrphelper.CutPaper(1, 1600);
                        }

                    }

                }*/
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
        }else{
            return "01-01";
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printBillOrder(
            int indexList,
            String deviceName,
            String orderNumber,
            String IP,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            ArrayList<Customer> customers,
            String roomName,
            int tableNumber
    ) {
        Log.i("PRINT ORDER BILL", "0");
       // if(ping(StaticValue.orderIP)) {
            ArrayList<CashButtonLayout> salse = new ArrayList<CashButtonLayout>();
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> salsemod = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            if (products.size() > 0) {
                Log.i("PRINT ORDER BILL", "1");
              //  HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP80A");
                HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
                try {
                    Log.i("PRINT ORDER BILL", "2");
                    int connection = hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                    Log.i("CONNECTION", ""+connection);
                    pList = products;
                    modifiersList = modifiers;
                    for (CashButtonLayout cashButton : pList) {
                        ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
                        if (cashButton.getQuantityInt() == 1) {
                           // ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                            //mList = modifiersList.get(cashButton);
                            ArrayList<CashButtonListLayout> mList =new ArrayList<CashButtonListLayout>();
                            if(modifiersList.get(cashButton)!=null) {
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
                                hrphelper.PrintText(cashButton.getQuantity() + " x 1", 0, 0, 16);
                            }
                            hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                            hrphelper.PrintAndLineFeed();
                            if (mList != null) {
                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                for (CashButtonListLayout m : mList) {
                                    if (m.getModifierId() != -15) {
                                        if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                            hrphelper.SetJustification(0);
                                            hrphelper.PrintText(m.getQuantityInt() + " x 1", 1, 0, 0);

                                        }
                                        hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                    hrphelper.PrintText(notes.get(i).getNote().toUpperCase(), 1, 0, 0);
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
                                hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                                hrphelper.PrintAndLineFeed();
                                ArrayList<CashButtonListLayout> mList =new ArrayList<CashButtonListLayout>();
                                if(modifiersList.get(cashButton)!=null) {
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
                                            hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                        hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 0);
                                    }
                                    hrphelper.PrintAndLineFeed();
                                    hrphelper.PrintAndLineFeed();
                                }
                                hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                                hrphelper.CutPaper(1, 1600);
                            }

                        }

                    }
                    hrphelper.PortClose();
                    return "04-00";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "04-02";
                }
            } else {
                return "04-00";
            }
       /* }else{
            return "04-01";
        }*/
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String reprintBillOrder(
            int indexList,
            String deviceName,
            String orderNumber,
            String IP,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            ArrayList<Customer> customers,
            String roomName,
            int tableNumber
    ) {
        if(ping(StaticValue.orderIP)) {
            ArrayList<CashButtonLayout> salse = new ArrayList<CashButtonLayout>();
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> salsemod = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            if (products.size() > 0) {
                HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
                try {
                    hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                    pList = products;
                    modifiersList = modifiers;
                    HPRTPrinterHelper.SetCharacterSet((byte) 0);
                    HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                    hrphelper.PrintText(" RISTAMPA ORDINE", 1, 48, 0);
                    hrphelper.PrintText(" NUMERO ORDINE : " + (Integer.valueOf(orderNumber)-1), 1, 48, 0);
                    if (tableNumber > 0) {
                        hrphelper.PrintText(roomName + " " + tableNumber, 1, 48, 0);
                    }
                    for (CashButtonLayout cashButton : pList) {
                        ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
                        if (cashButton.getQuantityInt() == 1) {
                            /*ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                            mList = modifiersList.get(cashButton);*/
                            ArrayList<CashButtonListLayout> mList =new ArrayList<CashButtonListLayout>();
                            if(modifiersList.get(cashButton)!=null) {
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
                                hrphelper.PrintText(cashButton.getQuantity() + " x 1", 0, 0, 16);
                            }
                            hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                            hrphelper.PrintAndLineFeed();
                            if (mList != null) {
                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
                                for (CashButtonListLayout m : mList) {
                                    if (m.getModifierId() != -15) {
                                        if (m.getQuantityInt() / cashButton.getQuantityInt() > 1) {
                                            hrphelper.PrintText(m.getQuantityInt() + " x 1", 1, 0, 0);
                                        }
                                        hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                    hrphelper.PrintText(notes.get(i).getNote().toUpperCase(), 1, 0, 0);
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
                                hrphelper.PrintText(cashButton.getTitle().toUpperCase(), 1, 0, 16);
                                hrphelper.PrintAndLineFeed();
                               // ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                ArrayList<CashButtonListLayout> mList =new ArrayList<CashButtonListLayout>();
                                if(modifiersList.get(cashButton)!=null) {
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
                                            hrphelper.PrintText(m.getTitle().toUpperCase(), 1, 0, 0);
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
                                        hrphelper.PrintText(notes.get(j).getNote().toUpperCase(), 1, 0, 0);
                                    }
                                    hrphelper.PrintAndLineFeed();
                                    hrphelper.PrintAndLineFeed();
                                }
                                hrphelper.PrintText(String.valueOf(date), 1, 0, 0);
                                hrphelper.PrintAndLineFeed();
                            }

                        }

                    }
                    hrphelper.CutPaper(1, 1600);
                    hrphelper.PortClose();
                    return "05-00";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "05-02";
                }
            } else {
                return "05-00";
            }
        }else{
            return "05-01";
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

    public int checkNoPaper(String IP){
        HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
        if(ping(StaticValue.orderIP)){
            try {
                hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                byte[] statusData = new byte[1];

                int status = hrphelper.GetTransmitStatus(1, statusData);
                hrphelper.PortClose();

                return status;
                // hrphelper.PortOpen("WiFi,10.10.10.204,9100");
            } catch (Exception e) {
                e.printStackTrace();
                return -2;
            }
        }else{
            return -3;
        }
    }


    /**
     * THIS PART IS TO PRINT BILL EVEN IF NOT FISCAL
     */

    public String printBill(
            String IP,
            String billId,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            float paid,
            float cost,
            float credit,
            float creditL,
            int paymentType

    ) {


      //  HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "MTP58B");
        HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "P80B");
        //HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
        try {
            Log.i("PRINT", "ENTRO IN PRINT");
            int connection =hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
            //int connection = hrphelper.PortOpen("WiFi,192.168.1.204,9100");
            Log.i("CONNECTION", "C : " + connection);


            //hrphelper.WriteData(data1);
            HPRTPrinterHelper.SetCharacterSet((byte) 0);
            HPRTPrinterHelper.LanguageEncode="iso8859-1";
            HPRTPrinterHelper.SetJustification(1);
            String[] ReceiptLines = context.getResources().getStringArray(R.array.company_head_printer);
            for(int i=0;i<ReceiptLines.length;i++)
                hrphelper.PrintText(ReceiptLines[i], 1,0,0);
            HPRTPrinterHelper.PrintAndLineFeed();
            HPRTPrinterHelper.PrintAndLineFeed();

            long costo = 0;
            hrphelper.SetJustification(2);
            hrphelper.PrintText("EURO\n", 2,0,8);



            pList = products;
            modifiersList = modifiers;
            for (CashButtonLayout cashButton : pList) {




                hrphelper.SetJustification(0);
                if(cashButton.getQuantityInt()>1){
                    hrphelper.PrintText(cashButton.getQuantity() + " x 1" , 0,0,16);

                }
                hrphelper.PrintText(cashButton.getTitle(), 0,0,16);

                hrphelper.SetJustification(2);
                if(cashButton.getHomage()!=0) {
                    hrphelper.PrintText(String.format("%.2f", 0.0f), 2, 0, 8);
                }else {
                    hrphelper.PrintText(String.format("%.2f", cashButton.getQuantityInt() * cashButton.getPriceFloat()), 2, 0, 8);
                    costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());

                }
                hrphelper.SetJustification(0);
                ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                if (mList != null) {
                    for (CashButtonListLayout m : mList) {
                        if(m.getQuantityInt()>1){
                            hrphelper.SetJustification(0);
                            hrphelper.PrintText(m.getQuantityInt() + " x 1", 0,0,0);

                        }
                        if(cashButton.getHomage()!=0) {
                            hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                            hrphelper.SetJustification(2);
                            hrphelper.PrintText(String.format("%.2f",0.0f, 2, 0, 0));
                        }else{
                            costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                            hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                            hrphelper.SetJustification(2);
                            hrphelper.PrintText(String.format("%.2f", m.getPriceFloat() * m.getQuantityInt()), 2, 0, 0);
                        }

                    }
                }

            }


            hrphelper.SetJustification(0);
            hrphelper.PrintText("TOTALE ", 0,0,16);
            hrphelper.SetJustification(2);
            hrphelper.PrintText(String.format("%.2f", cost), 2,0,16);
            hrphelper.SetJustification(0);
            hrphelper.PrintText("PAGATO", 0,0,0);
            hrphelper.SetJustification(2);
            hrphelper.PrintText(String.format("%.2f", paid), 2,0,0);
            if(paid-cost>0){
                hrphelper.SetJustification(0);
                hrphelper.PrintText("RESTO ", 0,0,0);
                hrphelper.SetJustification(2);
                hrphelper.PrintText(String.format("%.2f", paid-cost), 2,0,0);
            }


            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            hrphelper.SetJustification(0);
            hrphelper.PrintText(String.valueOf(date), 0,0,0);

            hrphelper.SetJustification(0);


            HPRTPrinterHelper.PrintAndLineFeed();

            hrphelper.SetJustification(0);

            //questo manda a riga sotto a distanza 10
            //HPRTPrinterHelper.PrintAndFeed(10);

            hrphelper.CutPaper(1, 1600);
            //hrphelper.BeepBuzzer((byte)1,(byte)10,(byte)10);


            hrphelper.OpenCashdrawer(0);
            hrphelper.PortClose();






            return "06-00";
        } catch (Exception e) {
            e.printStackTrace();
            return "06-02";
        }





    }

    public String stringToPrint(String title, String price){
        String toPrint = "";
        int myLength = 30 - (title.length()+price.length());
        StringBuilder str1 = new StringBuilder();
        str1.append(title);
        for(int j=1;j<=myLength;j++) {
                str1.append(" ");
        }
        str1.append(price);

        return str1.toString();
    }


    public String printBillNonFiscalPortable(
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            ArrayList<Customer> customers,
            float paid,
            float cost,
            float discount,
            float credit,
            float creditL,
            String roomName,
            int tableNumber,
            String orderNumber

    ) {


        //  HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "MTP58B");
        //HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "P80B");
        HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
        try {
            Log.i("PRINT", "ENTRO IN PRINT");
            int connection =hrphelper.PortOpen("WiFi,"+StaticValue.portableIp+",9100");
            //int connection = hrphelper.PortOpen("WiFi,192.168.1.204,9100");
            Log.i("CONNECTION", "C : " + connection);


            //hrphelper.WriteData(data1);
            HPRTPrinterHelper.SetCharacterSet((byte) 0);
            HPRTPrinterHelper.LanguageEncode="iso8859-1";
            HPRTPrinterHelper.SetJustification(1);
           /* String[] ReceiptLines = context.getResources().getStringArray(R.array.company_head_printer);
            for(int i=0;i<ReceiptLines.length;i++)
                hrphelper.PrintText(ReceiptLines[i], 1,0,0);
            HPRTPrinterHelper.PrintAndLineFeed();
            HPRTPrinterHelper.PrintAndLineFeed();*/
            if(tableNumber!=-1 && tableNumber!=-11) {
                hrphelper.PrintText("------------------------------" , 1,0,8);
                hrphelper.PrintText("STANZA " + roomName , 1,0,8);
                hrphelper.PrintText("------------------------------" , 1,0,8);
                hrphelper.PrintText("TAVOLO # " + String.valueOf(tableNumber), 1,0,8);
                hrphelper.PrintText("------------------------------" , 1,0,8);

            }else{
                hrphelper.PrintText("------------------------------" , 1,0,8);

                hrphelper.PrintText("ORDINE # " + String.valueOf(orderNumber), 1,0,8);
                hrphelper.PrintText("------------------------------" , 1,0,8);

            }



            long costo = 0;
            hrphelper.SetJustification(2);
            hrphelper.PrintText("EURO\n", 2,0,8);


            pList = products;
            modifiersList = modifiers;
            for (CashButtonLayout cashButton : pList) {




                hrphelper.SetJustification(0);
                if(cashButton.getQuantityInt()>1){
                    hrphelper.PrintText(cashButton.getQuantity() + " x 1" , 0,0,8);

                }

                //hrphelper.PrintText(cashButton.getTitle(), 0,0,8);

                hrphelper.SetJustification(2);
                if(cashButton.getHomage()!=0) {
                    hrphelper.PrintText(stringToPrint(cashButton.getTitle(), "0.00"), 0,0,8);
                    //hrphelper.PrintText(String.format("%.2f", 0.0f), 2, 0, 8);
                }else {
                    hrphelper.PrintText(stringToPrint(cashButton.getTitle(), String.format("%.2f", cashButton.getQuantityInt() * cashButton.getPriceFloat())), 0,0,8);

                    //hrphelper.PrintText(String.format("%.2f", cashButton.getQuantityInt() * cashButton.getPriceFloat()), 2, 0, 8);
                    costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());

                }
                hrphelper.SetJustification(0);
                ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                if (mList != null) {
                    for (CashButtonListLayout m : mList) {
                        if (m.getPriceFloat() > 0.0f) {
                            if (m.getQuantityInt() > 1) {
                                hrphelper.SetJustification(0);
                                hrphelper.PrintText(m.getQuantityInt() + " x 1", 0, 0, 0);

                            }
                            if (cashButton.getHomage() != 0) {
                                hrphelper.PrintText(stringToPrint(m.getTitle(), "0.00"), 0,0,8);
                               /* hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                                hrphelper.SetJustification(2);
                                hrphelper.PrintText(String.format("%.2f", 0.0f, 2, 0, 0));*/
                            } else {
                                costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                hrphelper.PrintText(stringToPrint(m.getTitle(), String.format("%.2f", m.getPriceFloat() * m.getQuantityInt())), 0,0,8);
                               /* hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                                hrphelper.SetJustification(2);
                                hrphelper.PrintText(String.format("%.2f", m.getPriceFloat() * m.getQuantityInt()), 2, 0, 0);*/
                            }

                        }
                    }
                }

            }


           /* hrphelper.SetJustification(0);
            hrphelper.PrintText("TOTALE ", 0,0,8);
            hrphelper.SetJustification(2);
            hrphelper.PrintText(String.format("%.2f", cost), 2,0,8);
            hrphelper.SetJustification(0);*/
            if(discount>0.0f){
               HPRTPrinterHelper.PrintAndLineFeed();
               hrphelper.PrintText(stringToPrint("SCONTO", String.format("%.2f", discount)), 0,0,8);
            }

            HPRTPrinterHelper.PrintAndLineFeed();
            hrphelper.PrintText(stringToPrint("TOTALE", String.format("%.2f", cost-discount)), 0,0,8);



            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
           // dateFormat.format(date);

            HPRTPrinterHelper.PrintAndLineFeed();
            hrphelper.PrintText(String.valueOf(dateFormat.format(date)), 1,0,0);

            hrphelper.SetJustification(0);


            HPRTPrinterHelper.PrintAndLineFeed();

            hrphelper.SetJustification(0);

            //questo manda a riga sotto a distanza 10
            //HPRTPrinterHelper.PrintAndFeed(10);

            hrphelper.CutPaper(1, 1600);
            //hrphelper.BeepBuzzer((byte)1,(byte)10,(byte)10);



            hrphelper.PortClose();






            return "06-00";
        } catch (Exception e) {
            e.printStackTrace();
            return "06-02";
        }





    }


    public String printBillDiscount(
            Float  discountTotal,
            String IP,
            String billId,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            float paid,
            float cost,
            float credit,
            float creditL,
            int paymentType
    ) {
        HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
        try {
            Log.i("PRINT", "ENTRO IN PRINT");
            int connection =hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
            //int connection = hrphelper.PortOpen("WiFi,192.168.1.204,9100");
            Log.i("CONNECTION", "C : " + connection);


            //hrphelper.WriteData(data1);
            HPRTPrinterHelper.SetCharacterSet((byte) 0);
            HPRTPrinterHelper.LanguageEncode="iso8859-1";
            HPRTPrinterHelper.SetJustification(1);
            String[] ReceiptLines = context.getResources().getStringArray(R.array.company_head_printer);
            for(int i=0;i<ReceiptLines.length;i++)
                hrphelper.PrintText(ReceiptLines[i], 1,0,0);
            HPRTPrinterHelper.PrintAndLineFeed();
            HPRTPrinterHelper.PrintAndLineFeed();

            long costo = 0;
            hrphelper.SetJustification(2);
            hrphelper.PrintText("EURO\n", 2,0,8);



            pList = products;
            modifiersList = modifiers;
            for (CashButtonLayout cashButton : pList) {




                hrphelper.SetJustification(0);
                if(cashButton.getQuantityInt()>1){
                    hrphelper.PrintText(cashButton.getQuantity() + " x 1" , 0,0,16);

                }
                hrphelper.PrintText(cashButton.getTitle(), 0,0,16);

                hrphelper.SetJustification(2);
                if(cashButton.getHomage()!=0) {
                    hrphelper.PrintText(String.format("%.2f", 0.0f), 2, 0, 8);
                }else {
                    hrphelper.PrintText(String.format("%.2f", cashButton.getQuantityInt() * cashButton.getPriceFloat()), 2, 0, 8);
                    costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());

                }
                hrphelper.SetJustification(0);
                ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                if (mList != null) {
                    for (CashButtonListLayout m : mList) {
                        if(m.getQuantityInt()>1){
                            hrphelper.SetJustification(0);
                            hrphelper.PrintText(m.getQuantityInt() + " x 1", 0,0,0);

                        }
                        if(cashButton.getHomage()!=0) {
                            hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                            hrphelper.SetJustification(2);
                            hrphelper.PrintText(String.format("%.2f",0.0f, 2, 0, 0));
                        }else{
                            costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                            hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                            hrphelper.SetJustification(2);
                            hrphelper.PrintText(String.format("%.2f", m.getPriceFloat() * m.getQuantityInt()), 2, 0, 0);
                        }

                    }
                }

            }
            if(discountTotal>0.0f) {
                hrphelper.SetJustification(0);
                hrphelper.PrintText("SCONTO ", 0,0,16);
                hrphelper.SetJustification(2);
                hrphelper.PrintText("-"+String.format("%.2f", discountTotal), 2,0,16);
            }

            hrphelper.SetJustification(0);
            hrphelper.PrintText("TOTALE ", 0,0,16);
            hrphelper.SetJustification(2);
            hrphelper.PrintText(String.format("%.2f", cost), 2,0,16);
            hrphelper.SetJustification(0);
            hrphelper.PrintText("PAGATO", 0,0,0);
            hrphelper.SetJustification(2);
            hrphelper.PrintText(String.format("%.2f", paid), 2,0,0);
            if(paid-cost>0){
                hrphelper.SetJustification(0);
                hrphelper.PrintText("RESTO ", 0,0,0);
                hrphelper.SetJustification(2);
                hrphelper.PrintText(String.format("%.2f", paid-cost), 2,0,0);
            }


            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            hrphelper.SetJustification(0);
            hrphelper.PrintText(String.valueOf(date), 0,0,0);

            hrphelper.SetJustification(0);


            HPRTPrinterHelper.PrintAndLineFeed();

            hrphelper.SetJustification(0);

            //questo manda a riga sotto a distanza 10
            //HPRTPrinterHelper.PrintAndFeed(10);

            hrphelper.CutPaper(1, 1600);
            //hrphelper.BeepBuzzer((byte)1,(byte)10,(byte)10);


            hrphelper.OpenCashdrawer(0);
            hrphelper.PortClose();





            return "07-00";
        } catch (Exception e) {
            e.printStackTrace();
            return "07-02";
        }



    }

    public String printNonFiscal(
            String IP,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            float discount,
            ArrayList<Customer> customers,
            int tableNumber,
            int orderNumber,
            String roomName
    ) {

        if(ping(StaticValue.orderIP)) {
            ArrayList<CashButtonLayout> salse = new ArrayList<CashButtonLayout>();
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> salsemod = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            if (products.size() > 0) {
                HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
                try {
                    hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                    pList = products;
                    modifiersList = modifiers;
                    for (CashButtonLayout cashButton : pList) {
                        ArrayList<CashButtonListLayout> notes = new ArrayList<CashButtonListLayout>();
                            ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                            mList = modifiersList.get(cashButton);
                            HPRTPrinterHelper.SetCharacterSet((byte) 0);
                            HPRTPrinterHelper.LanguageEncode = "iso8859-1";
                            if (tableNumber > 0) {
                                hrphelper.PrintText(roomName + " " + tableNumber, 1, 48, 0);
                            }
                            if (customers.size() > 0) {
                                hrphelper.PrintText(" NOME : " + customers.get(cashButton.getClientPosition() - 1).getDescription(), 1, 12, 0);
                            }
                            HPRTPrinterHelper.PrintAndLineFeed();
                        hrphelper.SetJustification(0);
                        if(cashButton.getQuantityInt()>1){
                            hrphelper.PrintText(cashButton.getQuantity() + " x 1" , 0,0,16);

                        }
                        hrphelper.PrintText(cashButton.getTitle(), 0,0,16);

                        hrphelper.SetJustification(2);
                        if(cashButton.getHomage()!=0) {
                            hrphelper.PrintText(String.format("%.2f", 0.0f), 2, 0, 8);
                        }else {
                            hrphelper.PrintText(String.format("%.2f", cashButton.getQuantityInt() * cashButton.getPriceFloat()), 2, 0, 8);
                          //  costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());

                        }
                        hrphelper.SetJustification(0);
                        mList = modifiersList.get(cashButton);
                        if (mList != null) {
                            for (CashButtonListLayout m : mList) {
                                if(m.getQuantityInt()>1){
                                    hrphelper.SetJustification(0);
                                    hrphelper.PrintText(m.getQuantityInt() + " x 1", 0,0,0);

                                }
                                if(cashButton.getHomage()!=0) {
                                    hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                                    hrphelper.SetJustification(2);
                                    hrphelper.PrintText(String.format("%.2f",0.0f, 2, 0, 0));
                                }else{
                                 //   costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                    hrphelper.PrintText(m.getTitle(), 0, 0, 0);
                                    hrphelper.SetJustification(2);
                                    hrphelper.PrintText(String.format("%.2f", m.getPriceFloat() * m.getQuantityInt()), 2, 0, 0);
                                }

                            }
                        }
                            Date date = new Date();
                            hrphelper.PrintAndLineFeed();
                            hrphelper.PrintAndLineFeed();
                            if (notes.size() > 0) {
                                for (int i = 0; i < notes.size(); i++) {
                                    hrphelper.PrintText(notes.get(i).getNote().toUpperCase(), 1, 0, 0);
                                }
                                hrphelper.PrintAndLineFeed();
                                hrphelper.PrintAndLineFeed();
                            }
                            hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                            hrphelper.CutPaper(1, 1600);


                    }
                    hrphelper.PortClose();
                    return "04-00";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "04-02";
                }
            } else {
                return "04-00";
            }
        }else{
            return "04-01";
        }

    }

    public String printFiscalBillPartial(
            String IP,
            float paid,
            float cost,
            String description,
            String numberOrderSplit,
            int paymentType,
            int quantity) {
            HPRTPrinterHelper hrphelper = new HPRTPrinterHelper(context, "TP805");
            try {
                    Log.i("PRINT", "ENTRO IN PRINT");
                    int connection =hrphelper.PortOpen("WiFi,"+StaticValue.orderIP+",9100");
                    //int connection = hrphelper.PortOpen("WiFi,192.168.1.204,9100");
                    Log.i("CONNECTION", "C : " + connection);


                    //hrphelper.WriteData(data1);
                    HPRTPrinterHelper.SetCharacterSet((byte) 0);
                    HPRTPrinterHelper.LanguageEncode="iso8859-1";
                    HPRTPrinterHelper.SetJustification(1);
                    String[] ReceiptLines = context.getResources().getStringArray(R.array.company_head_printer);
                    for(int i=0;i<ReceiptLines.length;i++)
                        hrphelper.PrintText(ReceiptLines[i], 1,0,0);
                    HPRTPrinterHelper.PrintAndLineFeed();
                    HPRTPrinterHelper.PrintAndLineFeed();

                    long costo = 0;
                    hrphelper.SetJustification(2);
                    hrphelper.PrintText("EURO\n", 2,0,8);

                    hrphelper.PrintText(description, 0,0,16);
                    HPRTPrinterHelper.PrintAndLineFeed();
                    HPRTPrinterHelper.PrintAndLineFeed();

                hrphelper.PrintText(String.format("%.2f", cost), 2, 0, 8);
                HPRTPrinterHelper.PrintAndLineFeed();
                HPRTPrinterHelper.PrintAndLineFeed();


                hrphelper.SetJustification(0);
                    hrphelper.PrintText("TOTALE ", 0,0,16);
                    hrphelper.SetJustification(2);
                    hrphelper.PrintText(String.format("%.2f", cost), 2,0,16);
                    hrphelper.SetJustification(0);
                    hrphelper.PrintText("PAGATO", 0,0,0);
                    hrphelper.SetJustification(2);
                    hrphelper.PrintText(String.format("%.2f", paid), 2,0,0);
                    if(paid-cost>0){
                        hrphelper.SetJustification(0);
                        hrphelper.PrintText("RESTO ", 0,0,0);
                        hrphelper.SetJustification(2);
                        hrphelper.PrintText(String.format("%.2f", paid-cost), 2,0,0);
                    }

                Date date = new Date();
                hrphelper.PrintAndLineFeed();
                hrphelper.PrintAndLineFeed();
                hrphelper.PrintText(String.valueOf(date), 0, 0, 0);
                hrphelper.CutPaper(1, 1600);

                hrphelper.PortClose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return "04-00";

    }

}
