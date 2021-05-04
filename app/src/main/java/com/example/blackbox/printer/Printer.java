package com.example.blackbox.printer;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.FiscalPrinter;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.fragments.OrderFragment;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tiziano on 9/8/17.
 */

public class Printer {

    int typeDisplay=1;
    private static String PREF_NAME = "prefs";
    private Context context;
    private ArrayList<CashButtonLayout> productsList;
    private Collection<CashButtonLayout> pList;
    private Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiersList;
    private OrderFragment of;
    public static final int FISCAL_ZERO = 0;
    public static final int SIMPLE_REPORT = 1;

    public Printer( Context context){

    }

    public Printer( ){

    }

    public String printBill(String deviceName,
                            String billId,
                            String orderNumber,
                            String IP,
                            ArrayList<CashButtonLayout> products,
                            Map<CashButtonLayout,
                                    ArrayList<CashButtonListLayout>> modifiers,
                            float paid, float cost, float credit, float creditL,
                            int paymentType) {
        String success = "";
        FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
        //if(StaticValue.printerName.equals("custom")){
        if(myFiscal.getModel().equals("Custom")){
            if (StaticValue.fiscalPrinter) {
                PrinterCustomF custom = PrinterCustomF.getInstance();
                custom.startPrinteCosutmF();
                custom.setPaymentType(paymentType);
                custom.setBillId(billId);
                custom.setProducts(products);
                custom.setModifiers(modifiers);
                custom.setPaid(paid);
                custom.setCost(cost);
                custom.setCredit(credit);
                custom.setCreditL(creditL);
                custom.printBill();
            }
        }
        else if(myFiscal.getModel().equals("Epson")){
            if (StaticValue.fiscalPrinter) {
                PrinterEpson epson = new PrinterEpson();
                epson.setUrl(StaticValue.orderIP);
                epson.setBillId(billId);
                epson.setProducts(products);
                epson.setModifiers(modifiers);
                epson.setPaid(paid);
                epson.setCost(cost);
                epson.setCredit(credit);
                epson.setCreditL(creditL);

                epson.printFiscalBill();
                epson.setPrinterThread();
            }
        }
        else if(myFiscal.getModel().equals("RCH")){
            if(StaticValue.fiscalPrinter){
                PrinterRCH rch = new PrinterRCH();
                rch.setUrl(StaticValue.orderIP);
                rch.setBillId(billId);
                rch.setProducts(products);
                rch.setModifiers(modifiers);
                rch.setPaid(paid);
                rch.setCost(cost);
                rch.setCredit(credit);
                rch.setCreditL(creditL);

                rch.printFiscalBill();
                rch.setPrinterThread();
            }
        }
        else{
            if (StaticValue.fiscalPrinter) {
                //if(StaticValue.ditronApi) {
                if(myFiscal.isUseApi()){
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.codifica64();
                    ditron.setPaymentType(paymentType);
                    ditron.setBillId(billId);
                    ditron.setProducts(products);
                    ditron.setModifiers(modifiers);
                    ditron.setPaid(paid);
                    ditron.setCost(cost);
                    ditron.setCredit(credit);
                    ditron.setCreditL(creditL);
                    ditron.setMetodoStampa(1);
                    ditron.nowPrinting = true;
                    ditron.setPrinterThread();
                }else{
                    PrinterDitronF pd = new PrinterDitronF(context);
                    success = pd.printBill(IP, billId, products, modifiers, paid, cost, credit, creditL, paymentType);
                }
            }

        }

        return success;
    }

    public String printBillDiscount(
            Float discount ,
            String deviceName,
            String billId,
            String orderNumber,
            String IP,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            float paid,
            float cost,
            float credit,
            float creditL,
            int paymentType) {

        String success = "";
        FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
        if(myFiscal.getModel().equals("Custom")){
            //if(StaticValue.printerName.equals("custom")){
            if (StaticValue.fiscalPrinter) {
                PrinterCustomF custom = PrinterCustomF.getInstance();
                custom.startPrinteCosutmF();
                custom.setBillId(billId);
                custom.setProducts(products);
                custom.setModifiers(modifiers);
                custom.setPaid(paid);
                custom.setCost(cost);
                custom.setCredit(credit);
                custom.setCreditL(creditL);
                custom.setDiscountTotal(discount);
                custom.printBillDiscount();
            }
        }
        else if(myFiscal.getModel().equals("Epson")){
            if (StaticValue.fiscalPrinter) {
                PrinterEpson epson = new PrinterEpson();
                epson.setUrl(StaticValue.orderIP);
                epson.setBillId(billId);
                epson.setProducts(products);
                epson.setModifiers(modifiers);
                epson.setPaid(paid);
                epson.setCost(cost);
                epson.setCredit(credit);
                epson.setCreditL(creditL);
                epson.setTotalDiscount(discount);

                epson.printFiscalBill();
                epson.setPrinterThread();
            }
        }
        else if(myFiscal.getModel().equals("RCH")){
            if(StaticValue.fiscalPrinter){
                PrinterRCH rch = new PrinterRCH();
                rch.setUrl(StaticValue.orderIP);
                rch.setBillId(billId);
                rch.setProducts(products);
                rch.setModifiers(modifiers);
                rch.setPaid(paid);
                rch.setCost(cost);
                rch.setCredit(credit);
                rch.setCreditL(creditL);
                rch.setTotalDiscount(discount);

                rch.printFiscalBill();
                rch.setPrinterThread();
            }
        }
        else {
            if (StaticValue.fiscalPrinter) {
                //if(StaticValue.ditronApi) {
                if(myFiscal.isUseApi()){
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.codifica64();
                    ditron.setPaymentType(paymentType);
                    ditron.setBillId(billId);
                    ditron.setProducts(products);
                    ditron.setModifiers(modifiers);
                    ditron.setPaid(paid);
                    ditron.setCost(cost);
                    ditron.setCredit(credit);
                    ditron.setCreditL(creditL);
                    ditron.setMetodoStampa(2);
                    ditron.setDiscountTotal(discount);
                    ditron.nowPrinting = true;
                    ditron.setPrinterThread();
                }
                else{
                    PrinterDitronF pd = new PrinterDitronF(context);
                    success = pd.printBillDiscount(discount, IP, billId, products, modifiers, paid, cost, credit, creditL, paymentType);
                }
            }

        }
        return success;

    }

    protected ArrayList<CashButtonLayout> newProducts = new ArrayList<>();
    protected Map<CashButtonLayout, ArrayList<CashButtonListLayout>> newmodifiers= new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

    public String printNonFiscal(
            Float discount ,
            String deviceName,
            String orderNumber,
            String IP,
            final ArrayList<CashButtonLayout> products,
            final Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            float paid,
            float cost,
            float credit,
            int paymentType,
            ArrayList<Customer> customers,
            int tableNumber,
            String roomName) {
        String success = "";
        if(StaticValue.portablePrinter){
            PrinterHPRT phprt = new PrinterHPRT(context);
            success = phprt.printBillNonFiscalPortable(
                    products, modifiers, customers,paid, cost,discount,0.0f,0.0f, roomName, tableNumber, orderNumber);

        }
        else {
            FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
            //if(StaticValue.printerName.equals("custom")){
            if(myFiscal.getModel().equals("Custom")){
                if (StaticValue.fiscalPrinter) {
                    PrinterCustomF custom = PrinterCustomF.getInstance();
                    custom.stopPrinterCostumF();
                    custom.startPrinteCosutmF();
                    custom.setProducts(products);
                    custom.setModifiers(modifiers);
                    custom.setPaid(paid);
                    custom.setCost(cost);
                    custom.setCredit(credit);
                    custom.setDiscountTotal(discount);
                    custom.setOrderNumber(orderNumber);
                    custom.setCustomers(customers);
                    custom.setTableNumber(tableNumber);
                    custom.setRoomName(roomName);
                    custom.printNonFiscal();

                }
            }
            else if(myFiscal.getModel().equals("Epson")){
                if (StaticValue.fiscalPrinter) {
                    PrinterEpson epson = new PrinterEpson();
                    epson.setUrl(StaticValue.orderIP);
                    epson.setProducts(products);
                    epson.setModifiers(modifiers);
                    epson.setPaid(paid);
                    epson.setCost(cost);
                    epson.setCredit(credit);
                    epson.setTotalDiscount(discount);
                    epson.setOrderNumber(orderNumber);
                    epson.setCustomers(customers);
                    epson.setTableNumber(tableNumber);
                    epson.setRoomName(roomName);

                    epson.printNonFiscalBill();
                    epson.setPrinterThread();
                }
            }
            else if(myFiscal.getModel().equals("RCH")){
                if(StaticValue.fiscalPrinter){
                    PrinterRCH rch = new PrinterRCH();
                    rch.setUrl(StaticValue.orderIP);
                    rch.setProducts(products);
                    rch.setModifiers(modifiers);
                    rch.setPaid(paid);
                    rch.setCost(cost);
                    rch.setCredit(credit);
                    rch.setTotalDiscount(discount);
                    rch.setOrderNumber(orderNumber);
                    rch.setCustomers(customers);
                    rch.setTableNumber(tableNumber);
                    rch.setRoomName(roomName);

                    rch.printNonFiscalBill();
                    rch.setPrinterThread();
                }
            }
            else {
                if (StaticValue.fiscalPrinter) {
                    //if(StaticValue.ditronApi) {
                    if(myFiscal.isUseApi()){
                        PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                        ditron.codifica64();
                        ditron.setPaymentType(paymentType);
                        ditron.setProducts(products);
                        ditron.setModifiers(modifiers);
                        ditron.setPaid(paid);
                        ditron.setCost(cost);
                        ditron.setCredit(credit);
                        ditron.setMetodoStampa(4);
                        ditron.setTableNumber(tableNumber);
                        ditron.setRoomName(roomName);
                        ditron.setNumberOrderSplit(orderNumber);
                        ditron.setOrderNumber(orderNumber);
                        ditron.nowPrinting = true;
                        ditron.setPrinterThread();
                    }else{
                        PrinterDitronF pd = new PrinterDitronF(context);
                        success = pd.printNonFiscal(IP, products, modifiers, discount, customers, tableNumber, Integer.parseInt(orderNumber) - 1, roomName);

                    }
                }

            }
        }
        return success;
    }

    public String printFiscalBillPartialList(
            String IP,
            ArrayList<LeftPayment> left,
            Float discount ,
            String deviceName,
            String orderNumber,
            String IP2,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            float paid,
            float cost,
            float credit,
            int paymentType,
            ArrayList<Customer> customers,
            int tableNumber,
            String roomName
    ) {
        String success = "";
        FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
        //if(StaticValue.printerName.equals("custom")){
        if(myFiscal.getModel().equals("Custom")){
            if (StaticValue.fiscalPrinter) {
                PrinterCustomF custom = PrinterCustomF.getInstance();
                custom.startPrinteCosutmF();
                custom.setLeftPayment(left);
                custom.setPaymentType(paymentType);
                custom.setProducts(products);
                custom.setModifiers(modifiers);
                custom.setPaid(paid);
                custom.setCost(cost);
                custom.setCredit(credit);
                custom.setTableNumber(tableNumber);
                custom.setRoomName(roomName);
                custom.setNumberOrderSplit(orderNumber);
                custom.setOrderNumber(orderNumber);
                custom.printFiscalBillPartial();
            }
        }
        else if(myFiscal.getModel().equals("Epson")){
            if (StaticValue.fiscalPrinter) {
                PrinterEpson epson = new PrinterEpson();
                epson.setUrl(StaticValue.orderIP);
                epson.setLeftPayment(left);
                epson.setProducts(products);
                epson.setModifiers(modifiers);
                epson.setPaid(paid);
                epson.setCost(cost);
                epson.setCredit(credit);
                epson.setTotalDiscount(discount);
                epson.setOrderNumber(orderNumber);
                epson.setCustomers(customers);
                epson.setTableNumber(tableNumber);
                epson.setRoomName(roomName);
                epson.setNumberOrderSplit(orderNumber);

                epson.printFiscalWithNonFiscal();
            }
        }
        else if(myFiscal.getModel().equals("RCH")){
            if(StaticValue.fiscalPrinter){
                PrinterRCH rch = new PrinterRCH();
                rch.setUrl(StaticValue.orderIP);
                rch.setLeftPayment(left);
                rch.setProducts(products);
                rch.setModifiers(modifiers);
                rch.setPaid(paid);
                rch.setCost(cost);
                rch.setCredit(credit);
                rch.setTotalDiscount(discount);
                rch.setOrderNumber(orderNumber);
                rch.setCustomers(customers);
                rch.setTableNumber(tableNumber);
                rch.setRoomName(roomName);
                rch.setNumberOrderSplit(orderNumber);

                rch.printFiscalWithNonFiscal();
            }
        }
        else {
            if (StaticValue.fiscalPrinter) {
                //if(StaticValue.ditronApi) {
                if(myFiscal.isUseApi()){
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.codifica64();
                    ditron.setLeftPayment(left);
                    ditron.setMetodoStampa(15);
                    ditron.nowPrinting = true;
                    ditron.setPaymentType(paymentType);
                    ditron.setProducts(products);
                    ditron.setModifiers(modifiers);
                    ditron.setPaid(paid);
                    ditron.setCost(cost);
                    ditron.setCredit(credit);
                    ditron.setTableNumber(tableNumber);
                    ditron.setRoomName(roomName);
                    ditron.setNumberOrderSplit(orderNumber);
                    ditron.setOrderNumber(orderNumber);
                    ditron.nowPrinting = true;
                    ditron.setPrinterThread();
                }else{
                    PrinterDitronF pd = new PrinterDitronF(context);
                    pd.printFiscalBillPartialList(
                            left,
                            paymentType,
                            products,
                            modifiers);
                }
            }

        }
        return success;
    }

    public String printFiscalBillPartial(
            String IP,
            float paid,
            float cost,
            String description,
            String numberOrderSplit,
            int paymentType,
            int quantity
    ) {
        String success = "";
        FiscalPrinter myFiscal = StaticValue.fiscalPrinterClass;
        //if(StaticValue.printerName.equals("custom")){
        if(myFiscal.getModel().equals("Custom")){
            if (StaticValue.fiscalPrinter) {
                PrinterCustomF custom = PrinterCustomF.getInstance();
                custom.setPaid(paid);
                custom.setCost(cost);
                custom.setDescription(description);
                custom.setNumberOrderSplit(numberOrderSplit);
                custom.setPaymentType(paymentType);
                custom.setQuantity(quantity);
                custom.printFiscalBillPartial();
            }
        }
        else if(myFiscal.getModel().equals("Epson")){
            if (StaticValue.fiscalPrinter) {
                PrinterEpson epson = new PrinterEpson();
                epson.setUrl(StaticValue.orderIP);
                epson.setPaid(paid);
                epson.setCost(cost);
                epson.setDescription(description);
                epson.setNumberOrderSplit(numberOrderSplit);
                epson.setPaymentType(paymentType);
                epson.setQuantity(quantity);

                epson.printFiscalBill();
                epson.setPrinterThread();
            }
        }
        else if(myFiscal.getModel().equals("RCH")){
            if(StaticValue.fiscalPrinter){
                PrinterRCH rch = new PrinterRCH();
                rch.setUrl(StaticValue.orderIP);
                rch.setPaid(paid);
                rch.setCost(cost);
                rch.setDescription(description);
                rch.setNumberOrderSplit(numberOrderSplit);
                rch.setPaymentType(paymentType);
                rch.setQuantity(quantity);

                rch.printFiscalBill();
                rch.setPrinterThread();
            }
        }
        else {
            if (StaticValue.fiscalPrinter) {
                if(myFiscal.isUseApi()){
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.codifica64();
                    ditron.setPaymentType(paymentType);
                    ditron.setPaid(paid);
                    ditron.setCost(cost);
                    ditron.setMetodoStampa(3);
                    ditron.setDescription(description);
                    ditron.setNumberOrderSplit(numberOrderSplit);
                    ditron.setQuantity(quantity);
                    ditron.nowPrinting = true;
                    ditron.setPrinterThread();
                }else{
                    PrinterDitronF pd = new PrinterDitronF(context);
                    success = pd.printFiscalBillPartial(IP, paid, cost, description, numberOrderSplit, paymentType, quantity);
                }
            }

        }
        return success;
    }

    public String printFiscalBillWithNonFiscal(String billId, String IP,float  paid, float cost,float credit,String description, String orderNumber,ArrayList<CashButtonLayout> products, Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers, int paymentType){
        String success = "";

        if(StaticValue.fiscalPrinter) {
            PrinterDitronF pd = new PrinterDitronF(context);
            success = pd.printFiscalBillWithNonFiscal(
                    billId,
                    IP,
                    paid,
                    cost,
                    credit,
                    description,
                    orderNumber,
                    products,
                    modifiers,
                    paymentType);
        }

        if(StaticValue.anotherFiscalPrinter) {

        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printOrder(
            Context c,
            int indexList,
            String deviceName,
            String billId,
            String IP,
            String orderNumber,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            ArrayList<Customer> customers,
            String roomName,
            int tableNumber
            //,String username
    ){
        String success ="";
        if(StaticValue.kitchenPrinter) {
            KitchenPrinter kitchen= KitchenPrinter.getInstance().getInstance();
            kitchen.setContext(c);
            kitchen.setMetodoStampa(8);
            kitchen.setIndexList(indexList);
            kitchen.setDeviceName(deviceName);
            kitchen.setOrderNumber(orderNumber);
            kitchen.setProducts(products);
           // kitchen.setUsername(username);
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            for (int i = 0; i < products.size(); i++) {
                ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                mList = modifiers.get(products.get(i));
                if (mList != null) {
                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());

                }
                newModifiers.put(products.get(i), mList);
            }

            kitchen.setModifiers(newModifiers);
            kitchen.setCustomers(customers);
            kitchen.setRoomName(roomName);
            kitchen.setTableNumber(tableNumber);

            kitchen.setPrinterThread();
            kitchen.nowPrinting = true;

           /* PrinterHPRT phprt = new PrinterHPRT(context);
            success = phprt.printBillOrder(indexList, deviceName, orderNumber, IP, products, modifiers, customers, roomName, tableNumber);
         */   //String success = "00";
        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printOrderCorrection(
            String changedPosition,
            String deviceName,
            String billId,
            String IP,
            String orderNumber,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            CashButtonLayout oldProducts,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> oldModifiers,
            Customer customer
            //,String username
    ){
        String success ="";
        if(StaticValue.kitchenPrinter) {
            KitchenPrinter kitchen= KitchenPrinter.getInstance().getInstance();
            kitchen.setContext(context);
            kitchen.setMetodoStampa(9);
            kitchen.setChangedPosition(changedPosition);
            kitchen.setDeviceName(deviceName);
            kitchen.setOrderNumber(orderNumber);
            kitchen.setProducts(products);
        //    kitchen.setUsername(username);
            /*HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            for (int i = 0; i < products.size(); i++) {
                ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                mList = modifiers.get(products.get(i));
                if (mList != null) {
                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());

                }
                newModifiers.put(products.get(i), mList);
            }*/
            kitchen.setModifiers(modifiers);
            kitchen.setCustomer(customer);
            kitchen.setOldProducts(oldProducts);
            kitchen.setOldModifiers(oldModifiers);
            kitchen.nowPrinting = true;
            kitchen.setPrinterThread();


           /* PrinterHPRT phprt = new PrinterHPRT(context);
            success = phprt.printBillOrderCorrection(changedPosition, deviceName, orderNumber, IP, products, modifiers, oldProducts, oldModifiers, customer);
     */
        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printOrderCorrectionInc(
            String changedPosition,
            String deviceName,
            String billId,
            String IP,
            String orderNumber,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            CashButtonLayout oldProducts,
            int quantity,
            Customer customer
            //, String username
    ){
        String success ="";
        if(StaticValue.kitchenPrinter) {
            KitchenPrinter kitchen= KitchenPrinter.getInstance().getInstance();
            kitchen.setContext(context);
            kitchen.setMetodoStampa(10);
            kitchen.setChangedPosition(changedPosition);
            kitchen.setDeviceName(deviceName);
            kitchen.setOrderNumber(orderNumber);
     //       kitchen.setUsername(username);
         /*   HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
            mList = modifiers.get(oldProducts);
            if (mList != null) {
                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
            }
            newModifiers.put(oldProducts, mList);*/

            kitchen.setModifiers(modifiers);
            kitchen.setCustomer(customer);
            kitchen.setOldProducts(oldProducts);
            kitchen.setQuantity(quantity);
            kitchen.nowPrinting = true;
            kitchen.setPrinterThread();


      /*      PrinterHPRT phprt = new PrinterHPRT(context);
            success = phprt.printBillOrderCorrectionInc(changedPosition, deviceName, orderNumber, IP, modifiers, oldProducts, quantity, customer);
    */
        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String printOrderDelete(
            String changedPosition,
            String deviceName,
            String billId,
            String IP,
            String orderNumber,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            CashButtonLayout oldProducts,
            int quantity,
            Customer customer
            //,            String username
            ){
        String success ="";
        if(StaticValue.kitchenPrinter) {
            KitchenPrinter kitchen= KitchenPrinter.getInstance().getInstance();
            kitchen.setContext(context);
            kitchen.setMetodoStampa(11);
            kitchen.setChangedPosition(changedPosition);
            kitchen.setDeviceName(deviceName);
            kitchen.setOrderNumber(orderNumber);
     //       kitchen.setUsername(username);
          /*  HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
            mList = modifiers.get(oldProducts);
            if (mList != null) {
                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());
            }
            newModifiers.put(oldProducts, mList);
*/
            kitchen.setModifiers(modifiers);
            kitchen.setCustomer(customer);
            kitchen.setOldProducts(oldProducts);
            kitchen.setQuantity(quantity);
            kitchen.nowPrinting = true;
            kitchen.setPrinterThread();



   /*         PrinterHPRT phprt = new PrinterHPRT(context);
            success = phprt.printBillOrderDelete(changedPosition, deviceName, orderNumber, IP, modifiers, oldProducts, quantity, customer);
   */
        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String reprintOrder(
            int indexList,
            String deviceName,
            String billId,
            String IP,
            String orderNumber,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            ArrayList<Customer> customers,
            String roomName,
            int tableNumber
            //,            String username
    ){
        String success ="";
        if(StaticValue.kitchenPrinter) {
            KitchenPrinter kitchen= KitchenPrinter.getInstance().getInstance();
            kitchen.setContext(context);
            kitchen.setMetodoStampa(12);
            kitchen.setIndexList(indexList);
            kitchen.setDeviceName(deviceName);
            kitchen.setOrderNumber(orderNumber);
            kitchen.setProducts(products);
    //        kitchen.setUsername(username);
            /*HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            for (int i = 0; i < products.size(); i++) {
                ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                mList = modifiers.get(products.get(i));
                if (mList != null) {
                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2.getPosition());
                    mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2.getGroupPosition());

                }
                newModifiers.put(products.get(i), mList);
            }*/
            kitchen.setModifiers(modifiers);
            kitchen.setCustomers(customers);
            kitchen.setRoomName(roomName);
            kitchen.setTableNumber(tableNumber);
            kitchen.nowPrinting = true;
            kitchen.setPrinterThread();



           /* PrinterHPRT phprt = new PrinterHPRT(context);
            success = phprt.reprintBillOrder(indexList, deviceName, orderNumber, IP, products, modifiers, customers, roomName, tableNumber);
     */
        }
        return success;
    }

    public String printClosing(String IP, int type) {
        String success ="";
        if(StaticValue.printerName.equals("custom")){
            if (StaticValue.fiscalPrinter) {
                PrinterCustomF custom = PrinterCustomF.getInstance();
                custom.startPrinteCosutmF();
                if(type==0) {
                    custom.printMemoriaFiscale();
                }else{
                    custom.printLetturaGiornaliera();
                }
            }
        }
        else if(StaticValue.printerName.equals("Epson")){
            if (StaticValue.fiscalPrinter) {
                PrinterEpson epson = new PrinterEpson();
                epson.setUrl(StaticValue.orderIP);

                if(type == 0)
                    epson.printZReport();
                else
                    epson.printXReport();
                epson.setPrinterThread();
            }
        }
        else if(StaticValue.printerName.equals("RCH")){
            if(StaticValue.fiscalPrinter){
                PrinterRCH rch = new PrinterRCH();
                rch.setUrl(StaticValue.orderIP);
                Log.d("SIAMO QUI", "print" + (type==0?"Z":"X") + "report");

                if(type == 0)
                    rch.printZReport();
                else
                    rch.printXReport();
                rch.setPrinterThread();
            }
        }
        else {
            if (StaticValue.fiscalPrinter) {
                if(StaticValue.ditronApi) {
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.codifica64();
                    ditron.setReport(type);
                    ditron.setMetodoStampa(13);
                    ditron.nowPrinting = true;
                    ditron.setPrinterThread();
                }else{
                    PrinterDitronF pd = new PrinterDitronF(context);
                    pd.printReport(IP, type);
                }
            }

        }

        return success;
    }

    public String openCashDrawer() {
        String success = "";
        Log.i("PRINTER NAME", StaticValue.printerName);
        if(StaticValue.printerName.endsWith("custom")){
            PrinterCustomF custom = PrinterCustomF.getInstance();
            custom.apriCassetto();
        }
        else if(StaticValue.printerName.equals("Epson")){
            if (StaticValue.fiscalPrinter) {
                PrinterEpson epson = new PrinterEpson();
                epson.setUrl(StaticValue.orderIP);

                epson.openCashDrawer();
                epson.setPrinterThread();
            }
        }
        else if(StaticValue.printerName.equals("RCH")){
            if(StaticValue.fiscalPrinter){
                PrinterRCH rch = new PrinterRCH();
                rch.setUrl(StaticValue.orderIP);

                rch.openCashDrawer();
                rch.setPrinterThread();
            }
        }
        else{
            if(StaticValue.ditronApi){
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.codifica64();
                ditron.setMetodoStampa(14);
                ditron.nowPrinting = true;
                ditron.setPrinterThread();
            }else{
                PrinterDitronF pd = new PrinterDitronF(context);
                pd.openCashDrawer();
            }
        }
        return success;
    }

    public String printNonFiscalInvoice(
            ClientInfo client,
            float total,
            float paid,
            int paymentType,
            int numeroFattura,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            int tableNumber,
            String orderNumber,
            String roomName,
            float discount


    ) {
        String success = "";
        Log.i("SIAMO ENTRATI", "NEL METODO");
        if(StaticValue.printerName.endsWith("custom")){
            PrinterCustomF custom = PrinterCustomF.getInstance();
            custom.startPrinteCosutmF();
            custom .setClientInfo(client);
            custom .setCost(total);
            custom .setPaid(paid);
            custom .setPaymentType(paymentType);
            custom .setNumeroFattura(numeroFattura);
            custom .setProducts(products);
            custom .setModifiers(modifiers);
            custom .setTableNumber(tableNumber);
            custom .setNumberOrderSplit(String.valueOf(orderNumber));
            custom .setRoomName(roomName);
            custom .setDiscountTotal(discount);
            custom.printInvoice();
        }
        else if(StaticValue.printerName.equals("epson")){
            if (StaticValue.fiscalPrinter) {
                for(int i=0; i<2; i++){
                    PrinterEpson epson = new PrinterEpson();
                    epson.setUrl(StaticValue.orderIP);

                    epson.setClientInfo(client);
                    epson.setCost(total);
                    epson.setPaid(paid);
                    epson.setPaymentType(paymentType);
                    epson.setNumeroFattura(numeroFattura);
                    epson.setProducts(products);
                    epson.setModifiers(modifiers);
                    epson.setOrderNumber(orderNumber);
                    epson.setTableNumber(tableNumber);
                    epson.setNumberOrderSplit(String.valueOf(orderNumber));
                    epson.setRoomName(roomName);
                    epson.setTotalDiscount(discount);

                    epson.printInvoice(i);
                    epson.setPrinterThread();
                }
            }
        }
        else if(StaticValue.printerName.equals("RCH")){
            Log.i("SIAMO QUI", "RCH");
            if(StaticValue.fiscalPrinter){
                Log.i("SIAMO QUI", "RCH, fiscalPrinter");
                for(int i=0; i<2; i++){
                    PrinterRCH rch = new PrinterRCH();
                    rch.setUrl(StaticValue.orderIP);

                    rch.setClientInfo(client);
                    rch.setCost(total);
                    rch.setPaid(paid);
                    rch.setPaymentType(paymentType);
                    rch.setNumeroFattura(numeroFattura);
                    rch.setProducts(products);
                    rch.setModifiers(modifiers);
                    rch.setOrderNumber(orderNumber);
                    rch.setTableNumber(tableNumber);
                    rch.setNumberOrderSplit(String.valueOf(orderNumber));
                    rch.setRoomName(roomName);
                    rch.setTotalDiscount(discount);

                    rch.printInvoice(i);
                    rch.setPrinterThread();
                }
            }
        }
        else{
            if(StaticValue.ditronApi){
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.codifica64();
                ditron.setMetodoStampa(16);
                ditron.setClientInfo(client);
                ditron.setCost(total);
                ditron.setPaid(paid);
                ditron.setPaymentType(paymentType);
                ditron.setNumeroFattura(numeroFattura);
                ditron.setProducts(products);
                ditron.setModifiers(modifiers);
                ditron.setTableNumber(tableNumber);
                ditron.setNumberOrderSplit(String.valueOf(orderNumber));
                ditron.setRoomName(roomName);
                ditron.setDiscountTotal(discount);
                ditron.nowPrinting = true;
                ditron.setPrinterThread();
            }else{
                PrinterDitronF pd = new PrinterDitronF(context);
                pd.printInvoice(numeroFattura, client, products, modifiers,
                        discount, tableNumber,
                        Integer.parseInt(orderNumber),
                        roomName);
            }
        }
        return success;

    }


}
