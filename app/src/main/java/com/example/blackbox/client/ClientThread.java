package com.example.blackbox.client;

import android.content.Context;
import android.util.Log;

import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.PrinterInfo;
import com.example.blackbox.model.SubdivisionItem;
import com.example.blackbox.printer.Printer;

import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tiziano on 12/11/18.
 */

public class ClientThread {

    private Thread printer;
    private String response = "";
    private ArrayList<String> jsonString = new ArrayList<String>();
    PrinterInfo printerInfo = new PrinterInfo();

    public ArrayList<CashButtonLayout> products = new ArrayList<CashButtonLayout>();
    public void setProducts(ArrayList<CashButtonLayout> p){ products = p;}
    public CashButtonLayout oldProduct = new CashButtonLayout();
    public void setOldProducts(CashButtonLayout p){ oldProduct = p;}
    protected Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
    public void setModifiers(Map<CashButtonLayout, ArrayList<CashButtonListLayout>> m){ modifiers = m;}
    protected Map<CashButtonLayout, ArrayList<CashButtonListLayout>> oldModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
    public void setOldModifiers(Map<CashButtonLayout, ArrayList<CashButtonListLayout>> m){ oldModifiers = m;}
    public Double paid = 0.0;
    public void setPaid(double p) {paid = p;}
    public Double cost = 0.0;
    public void setCost(double p) {cost = p;}
    public Double credit = 0.0;
    public void setCredit(double p) {credit = p;}
    public Double creditL = 0.0;
    public void setCreditL(double p) {creditL = p;}
    public int paymentType = 0;
    public void setPaymentType(int p) {paymentType= p;}
    public String deviceName = "";
    public void setDeviceName(String s){deviceName = s;}
    public String IP = "";
    public void setIP(String s){IP = s;}
    public String orderNumberBill = "-1";
    public void setOrderNumberBill(String s){orderNumberBill=s;}
    public String billId = "";
    public void setBillId(String s){billId = s;}
    public int printType = -1;
    public void setPrintType(int t){printType = t;}
    public Double discount = 0.0;
    public void setTotalDiscount(double d) {discount = d;}
    public String description = "";
    public void setDescription(String s) {description = s;}
    public String orderNumber = "";
    public void setOrderNumber(String s){orderNumber = s;}
    public int quantity = 0;
    public void setQuantity(int i){quantity = i;}
    public String item = "";
    public void setItem(String s) {item = s;}
    public SubdivisionItem items = new SubdivisionItem();
    public void setItems(SubdivisionItem i){items = i;}
    public String changedPosition = "";
    public void setChangedPosition(String s){changedPosition=s;}
    public int indexList = 0;
    public void setIndexList(int i){indexList =i;}
    public ArrayList<Customer> customers = new ArrayList<Customer>();
    public void setCustomers(ArrayList<Customer> c){customers = c;}
    public Customer customer = new Customer();
    public void setCustomer(Customer c){customer = c;}
    public String roomName = "";
    public void setRoomName(String s){roomName = s;}
    public int tableNumber = -1;
    public void setTableNumber(int i){tableNumber=i;}
    public int report = -1;
    public void setReport(int i){report = i;}
    public ArrayList<LeftPayment> left;
    public void setLeftPayment(ArrayList<LeftPayment> l){left = l;}
    public ClientInfo clientInfo;
    public void setClientInfo(ClientInfo c) {clientInfo =c;}
    public int numeroFattura;
    public void setNumeroFattura(int i){numeroFattura =i;}

    public Context context;
    public void setContext(Context c){
        context = c;
    }


    public void addJsonString(String newjson){
        jsonString.add(newjson);
    }

    private static final ClientThread instance = new ClientThread();

    private ClientThread(){}

    public static ClientThread getInstance() {
        return instance;
    }

    public void setClientThread(){
        if(printer==null){
            printer = new Thread(new PrintThread());
            printer.start();
        }
    }

    public final static ReentrantLock lockhead = new ReentrantLock();

    private boolean isRunning = true;

    private String lock = "myLock";

    private boolean runBaby = false;
    public void setRunBaby(boolean b){runBaby = b;}

    private class PrintThread extends Thread{
        public void run() {
            while(true) {
                if(runBaby && isRunning){
                    synchronized (lockhead){
                        isRunning = false;
                        ClientDelegateMethod(IP);
                    }
                }
            }
        }
    }

    public TaskDelegate delegate;

    public interface TaskDelegate {
        //define you method headers to override
        void onTaskEndWithResult(String success);
        void onTaskFinishGettingData(String result);
    }


    public void ClientDelegateMethod(String myip){
       // if (delegate != null) {
      //  if(json!=null) {
      //      synchronized (json) {
                serverMethod(myip);
      //      }
      //  }



      //  }
    }

    public double parseToDouble(Double s){
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return Double.parseDouble(formatter.format(s).replace(",", "."));
    }




    public void serverMethod(String myip) {
           int newOrderNumber = Integer.parseInt(orderNumberBill) + 1;
        String returnStatus = "";
        synchronized (myip) {
            switch (printType) {
                case 1:
                    Log.i("STAMP0", "1");
                    Printer pd = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd.printBill(deviceName, billId, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), creditL.floatValue(), paymentType);
                    Log.i("FINE", "1");
                    break;
                case 2:
                    Printer pd1 = new Printer();
                    returnStatus += printerInfo.getCode() + "-";
                    returnStatus += pd1.printBillDiscount(discount.floatValue(), deviceName, billId, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), creditL.floatValue(), paymentType);
                    break;
                case 3: {
                    Log.i("STAMPO", "SCONTRINO");
                    Printer pd3 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd3.printFiscalBillPartial(IP, paid.floatValue(), cost.floatValue(), description, String.valueOf(newOrderNumber), paymentType, quantity);
                    Log.i("FINITO ", "STAMPO");
                    break;
                }
                case 15: {
                    Log.i("STAMPO", "SCONTRINO PARZIALE");
                    Printer pd3 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd3.printFiscalBillPartialList(IP, left, discount.floatValue(), deviceName, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), paymentType, customers, tableNumber, roomName);
                    Log.i("FINITO ", "STAMPO");
                    break;
                }
                case 4: {
                    //PRINT NON FISCAL
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd4.printNonFiscal(discount.floatValue(), deviceName, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), paymentType, customers, tableNumber, roomName);
                    break;
                }
                case 5: {
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd4.printNonFiscal(discount.floatValue(), deviceName, String.valueOf(newOrderNumber), IP, products, modifiers, paid.floatValue(), cost.floatValue(), credit.floatValue(), paymentType, null, tableNumber, "");
                    break;
                }
                case 6: {
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd4.printFiscalBillWithNonFiscal(billId, IP, paid.floatValue(), cost.floatValue(), credit.floatValue(), description, String.valueOf(newOrderNumber), products, modifiers, paymentType);
                    break;
                }
                case 8: {
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd4.printOrder(context ,indexList, deviceName, billId, IP, String.valueOf(newOrderNumber), products, modifiers, customers, roomName, tableNumber);
                    break;
                }
                case 9: {
                    Printer pd5 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd5.printOrderCorrection(changedPosition, deviceName, billId, IP, String.valueOf(newOrderNumber), products, modifiers, oldProduct, oldModifiers, customer);
                    break;
                }
                case 10: {
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd4.printOrderCorrectionInc(changedPosition, deviceName, billId, IP, String.valueOf(newOrderNumber), modifiers, oldProduct, quantity, customer);
                    break;
                }
                case 11: {
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd4.printOrderDelete(changedPosition, deviceName, billId, IP, String.valueOf(newOrderNumber), modifiers, oldProduct, quantity, customer);
                    break;
                }
                case 12: {
                    Log.i("STAMP0", "12");
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    returnStatus += pd4.reprintOrder(indexList, deviceName, billId, IP, String.valueOf(newOrderNumber), products, modifiers, customers, roomName, tableNumber);
                    Log.i("FINE", "12");
                    break;
                }
                case 13: {
                    Printer pd4 = new Printer();
                    returnStatus += printerInfo.getId() + "-";
                    returnStatus += billId + "-";
                    pd4.printClosing(IP, report);
                    break;
                }

                case 14: {
                    Printer pd4 = new Printer();
                    pd4.openCashDrawer();
                    break;
                }
                case 16: {
                    Printer pd4 = new Printer();
                    pd4.printNonFiscalInvoice(
                            clientInfo,
                            cost.floatValue(),
                            paid.floatValue(),
                            paymentType,
                            numeroFattura,
                            products,
                            modifiers,
                            tableNumber,
                            orderNumber,
                            roomName,
                            discount.floatValue()
                           );
                    break;
                }
                default:
                    break;
            }

            setRunBaby(false);
            //jsonString.remove(0);
            isRunning = true;
        }

    }

    protected void onPostExecute() {
        if (delegate != null) {
            //return success or fail to activity
            delegate.onTaskEndWithResult(response);
        }
    }

}
