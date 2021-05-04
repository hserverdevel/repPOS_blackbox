package com.example.blackbox.printer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ditron.ziplib.DitronCashDrawerService;
import com.ditron.ziplib.DitronFiscalPrinterService;
import com.ditron.ziplib.ditron.helper.DitronEcrDefine;
import com.ditron.ziplib.jpos.JposException;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.fragments.CalculatorFragment;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Created by tiziano on 9/8/17.
 */

public class PrinterDitronF {

    int typeDisplay=1;
    private Context context;
    private Collection<CashButtonLayout> pList;
    private Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiersList;
    public static final int FISCAL_ZERO = 0;
    public static final int SIMPLE_REPORT = 1;

    public double parseToDouble(float s){
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return Double.parseDouble(formatter.format(s).replace(",", "."));
    }

    public PrinterDitronF(Context context){
        this.context = context;

    }
    //just used to initialize printer on start
    public PrinterDitronF(){}

    /**
     * CHECK IF PRINTER IS REACHABLE
     * @param IP
     * @return true/false
     */
    public Boolean ping(String IP){
        try{
            InetAddress address = InetAddress.getByName(IP);
            boolean reachable = address.isReachable(100);
            return reachable;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RETURN STRING WITH DISCOUNT VALUE
     * @param discount
     * @return string
     */
    public String printRowForDiscount(float discount){
        String sconto = "SCONTO";
        String txt = String.format("%.2f", CalculatorFragment.roundDecimal(discount, 2)).replace(".", ",");
        int digitNumber = txt.length();
        for(int i=0; i<=(31-digitNumber); i++){
            sconto += new String(" ");
        }
        sconto+= "-"+txt;
        return sconto;
    }

    /**
     * OPEN CASH DRAWER
     * TODO NOT WORKING, ASK DITRON
     */
    public void openCashDrawer(){
        DitronFiscalPrinterService dfp;
        // Change only ip address and port
        dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);

        DitronCashDrawerService dcd;

        dcd = new DitronCashDrawerService();

        try{

            dfp.open(); // for all command before send a request JavaPOS documentation
            dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
            dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation


            //Cash Drawer
            dcd.open();
            dcd.claim(StaticValue.claim);
            dcd.setDeviceEnabled(true);
            Log.i("APRO CASSETTO", "APRO CASSETTO");

            //Open Drawer
            dcd.openDrawer();
            dcd.setDeviceEnabled(false);
            dcd.release();
            dcd.close();

            dfp.close();


        }catch (JposException e){
            e.printStackTrace();

        }
    }

    /**
     * userd to initialized ditron printer or it doesn't work on first print
     * probably because the latency of ditron's firmware is too high to open the comunication socket
     * return boolean accomplished
     * @return treu/false
     */
    public boolean printWelcome(String username) throws JposException {
          //  try {
                boolean toreturn = false;
                DitronFiscalPrinterService dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
                if(!dfp.getClaimed()) {
                    dfp.open();
                    dfp.claim(0); // for all command before send a request JavaPOS documentation

                    dfp.setDeviceEnabled(true);
                    dfp.beginNonFiscal();
                    if(username.equals(""))
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "BENVENUTO UTENTE  " );
                    else
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "BENVENUTO " +username );

                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "IN UNA MAGICA  " );
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "ESPERIENZA  " );
                    dfp.endNonFiscal();
                    dfp.close(); // for all command after send a request JavaPOS documentation
                    toreturn = true;
                }
                return toreturn;
            /*} catch (JposException e) {
                e.printStackTrace();

                return false;

            }*/


    }

    public boolean initialize() throws JposException {
        //  try {
        boolean toreturn = false;
        DitronFiscalPrinterService dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
        if(!dfp.getClaimed()) {
            dfp.open();
            dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation

            dfp.setDeviceEnabled(true);

            dfp.close(); // for all command after send a request JavaPOS documentation
            toreturn = true;
        }
        return toreturn;



    }

    /**
     * PRINT FISCAL PRINT NORMAL
     * @param IP
     * @param billId
     * @param products
     * @param modifiers
     * @param paid
     * @param cost
     * @param credit
     * @param creditL
     * @param paymentType
     * @return 01-errorcode
     */
    public String printBill(String IP,
                          String billId,
                          ArrayList<CashButtonLayout> products,
                          Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
                          float paid,
                          float cost,
                          float credit,
                          float creditL,
                          int paymentType
    ) {
        //if(ping(IP)){
            pList = products;
            modifiersList = modifiers;

            DitronFiscalPrinterService dfp;
            dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
            try {
                dfp.open();
                // for all command before send a request JavaPOS documentation
                dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
                dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation

                dfp.beginFiscalReceipt(false);

                long costo = 0;
                int i=0;
                for (CashButtonLayout cashButton : pList) {
                    i++;
                    Float discount = cashButton.getDiscount();
                    if(cashButton.getHomage()!=1) {
                        if (discount != null) {
                            if (discount >= (cashButton.getPriceFloat() * (cashButton.getQuantityInt()))) {
                                dfp.printRecItem(cashButton.getTitle(), 0, (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                                discount = discount - (cashButton.getPriceFloat() * (cashButton.getQuantityInt()));
                                ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                if (mList != null) {
                                    for (CashButtonListLayout m : mList) {
                                        if (m.getPriceFloat() == 0) continue;
                                        else {
                                            i++;
                                            if (discount >= (m.getPriceFloat() * (m.getQuantityInt()))) {
                                                dfp.printRecItem(m.getTitle(), 0, (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                                discount = discount - (m.getPriceFloat() * (m.getQuantityInt()));

                                            } else {
                                                if (discount > 0) {
                                                    costo = costo + (long) ((m.getPriceFloat() * 100) * (m.getQuantityInt()) - discount * 100);
                                                    dfp.printRecItem(m.getTitle(), (long) ((m.getPriceFloat() * 100) * (m.getQuantityInt()) - discount * 100), 1000, m.getVat(), 0, "");
                                                    discount = 0.0f;
                                                } else {
                                                    costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                                    dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");

                                                }
                                            }
                                        }
                                    }
                                }
                                if (cashButton.getDiscount() != 0.0f) {
                                    dfp.printRecMessage(printRowForDiscount(cashButton.getDiscount()));
                                }
                            } else {
                                dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt()) - discount * 100), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                                costo = costo + (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt()) - discount * 100);
                                ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                if (mList != null) {
                                    for (CashButtonListLayout m : mList) {
                                        if (m.getPriceFloat() == 0) continue;
                                        else {
                                            i++;
                                            costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                            dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                        }
                                    }
                                }
                                if (cashButton.getDiscount() != 0.0f) {
                                    dfp.printRecMessage(printRowForDiscount(cashButton.getDiscount()));
                                }
                            }
                        } else {
                            i++;
                            dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                            costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());
                            ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                            if (mList != null) {
                                for (CashButtonListLayout m : mList) {
                                    if (m.getPriceFloat() == 0) {
                                        //costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                        //dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, 1, 0, "");
                                    }
                                    //continue;
                                    else {
                                        i++;
                                        costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                        dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                    }
                                }
                            }
                        }
                    }else{
                        i++;
                        dfp.printRecMessage("OMAGGIO");
                        dfp.printRecItem(cashButton.getTitle(), (long) ((0.0f* 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                        //costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());
                        ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                        if (mList != null) {
                            for (CashButtonListLayout m : mList) {
                                if (m.getPriceFloat() == 0) {
                                    //costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                    //dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, 1, 0, "");
                                }
                                //continue;
                                else {
                                    i++;
                                    //costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                    dfp.printRecItem(m.getTitle(), (long) (0.0f* 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                }
                            }
                        }
                    }
                    if(i>40) {
                        i=0;
                        Log.i("PRINTR", "PAUSE");
                        //Thread.sleep(1000);
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
                float creditValue= credit;
                if(creditValue!=0.0) {
                    if((creditValue*100<=costo)) {
                        dfp.printRecSubtotalAdjustment(1, "", (long) (creditValue * 100));
                        dfp.printRecTotal((long) (costo - (creditValue * 100)), pagato, String.valueOf(paymentType)); // "1"-> Cash
                    }else{
                        dfp.printRecSubtotalAdjustment(1, "", (long) (costo ));
                        dfp.printRecTotal((long) 0, pagato, String.valueOf(paymentType)); // "1"-> Cash
                    }
                }else{
                        dfp.printRecTotal(costo, pagato, String.valueOf(paymentType)); // "1"-> Cash


                }
                if(creditL>0){
                    dfp.printRecMessage("CREDITO " + creditL + " EURO #"+ billId);
                }

                dfp.endFiscalReceipt(false);
                dfp.close(); // for all command after send a request JavaPOS documentation
                //openCashDrawer();
                return "01-00";

            } catch (JposException e) {
                //error trying to open communication with printer
                e.printStackTrace();
                return "01-02";
            } /*catch (InterruptedException e) {
                e.printStackTrace();
                return "01-02";
            }*/

       /* }else{
            //printer isn't online, plug that f****g cable!!!!
            return "01-01";
        }*/
    }


    /**
     * PRINT BILL WITH DISCOUNT
     * @param discountTotal
     * @param IP
     * @param billId
     * @param products
     * @param modifiers
     * @param paid
     * @param cost
     * @param credit
     * @param creditL
     * @param paymentType
     * @return
     */
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

       // if(ping(IP)){
            pList = products;
            modifiersList = modifiers;
            DitronFiscalPrinterService dfp;
            dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
            try {

                dfp.open();
                // for all command before send a request JavaPOS documentation
                dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
                dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation
                dfp.beginFiscalReceipt(false);

                long costo = 0;

                for (CashButtonLayout cashButton : pList) {
                    Float discount = cashButton.getDiscount();
                    if(cashButton.getHomage()!=1) {
                        dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                        costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());
                        ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                        if (mList != null) {
                            for (CashButtonListLayout m : mList) {
                                if (m.getPriceFloat() == 0) continue;
                                else {
                                    costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                    dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                }
                            }
                        }
                    }else{
                        dfp.printRecMessage("OMAGGIO");
                        dfp.printRecItem(cashButton.getTitle(), (long) ((0.0f * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                        discountTotal = discountTotal-cashButton.getDiscount();
                        //costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());
                        ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                        if (mList != null) {
                            for (CashButtonListLayout m : mList) {
                                if (m.getPriceFloat() == 0) continue;
                                else {
                                    //costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                    dfp.printRecItem(m.getTitle(), (long) (0.0f* 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                }
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
                if(discountTotal>0.0f) {
                    dfp.printRecSubtotal(costo);
                    dfp.printRecSubtotalAdjustment(1, "", (long) (discountTotal * 100));
                }
                float creditValue= credit;
                if(creditValue!=0.0) {
                    if((creditValue*100)<=(costo-(discountTotal * 100))) {
                        dfp.printRecSubtotalAdjustment(1, "", (long) (creditValue * 100));
                        dfp.printRecTotal((long) (costo - (discountTotal * 100) - (creditValue * 100)), pagato, String.valueOf(paymentType)); // "1"-> Cash
                    }else{
                        //credit is maggiore
                        dfp.printRecSubtotalAdjustment(1, "", (long) (costo - (discountTotal * 100) ));
                        dfp.printRecTotal((long) 0, pagato, String.valueOf(paymentType)); // "1"-> Cash

                    }
                }else{
                    long costoQua = (costo - (long) (discountTotal * 100));
                    if(pagato<costoQua) pagato = costoQua;
                    dfp.printRecTotal( (costo-(long)(discountTotal*100)), (pagato), String.valueOf(paymentType)); // "1"-> Cash
                }
                if(creditL>0){
                    dfp.printRecMessage("CREDITO " + creditL + " EURO #"+ billId);
                }

                dfp.endFiscalReceipt(false);
                dfp.close(); // for all command after send a request JavaPOS documentation
                //               ((PaymentActivity)context).savePaidBill(0);
                return "02-00";
            } catch (JposException e) {
                Log.d("ERRORE STAMPA", " " + e.getErrorCode());
                e.printStackTrace();
               /* try {
                    dfp.close(); // for all command after send a request JavaPOS documentation
                    if(e.getMessage().equals("claim-Claimed-byother")){
                    }

                } catch (JposException e1) {
                    e1.printStackTrace();
                }*/
                return "02-02";
            }

       /* }else{
            //printer not Found
            return "02-01";
        }*/

    }

    /**
     * PRINT NON FISCAL BILL, THIS IS THE EASIEST METHOD
     * @param IP
     * @param products
     * @param modifiers
     * @return
     */
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
        ArrayList<Customer> addedCustomer = new ArrayList<Customer>();
        //if(ping(IP)) {
            DitronFiscalPrinterService dfp;
            dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);

            try {
                dfp.open();
                // for all command before send a request JavaPOS documentation
                dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
                dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation
                dfp.beginNonFiscal();
                float costo = 0.0f;
                if(tableNumber!=-1 && tableNumber!=-11) {
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "STANZA  " + roomName);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TAVOLO  " + tableNumber);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
                }else{
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "NUMERO ORDINE  " + orderNumber);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
                }

                String printEuro = "                                    EURO";
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printEuro);
                for (CashButtonLayout cashButton : products) {
                    if(cashButton.getClientPosition()!=0){
                        Customer customer = customers.get(cashButton.getClientPosition()-1);
                        if(!addedCustomer.contains(customer)){
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, " " +customer.getDescription());
                            addedCustomer.add(customer);

                        }
                    }

                    if(cashButton.getHomage()==0) {
                        if (cashButton.getQuantityInt() > 1) {
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getQuantity() + " x " + String.format("%.2f", cashButton.getPriceFloat()));
                        }

                        int size = cashButton.getTitle().length();
                        int sizeCost = String.format("%.2f", cashButton.getPriceFloat()*cashButton.getQuantityInt()).length();
                        StringBuffer toPrint = new StringBuffer();
                        if (size + sizeCost <= 39) {
                            toPrint.append(cashButton.getTitle().toUpperCase());
                            for (int i = 0; i <= (39 - (size + sizeCost)); i++) {
                                toPrint.append(" ");
                            }
                            toPrint.append(String.format("%.2f", cashButton.getPriceFloat()*cashButton.getQuantityInt()));
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint.toString());
                        } else {

                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getTitle() + "x" + cashButton.getQuantityInt() + " " + cashButton.getPriceFloat() * cashButton.getQuantityInt());
                        }

                        //dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, 1, 0, "");
                        costo = costo + (cashButton.getPriceFloat() * cashButton.getQuantityInt());
                        ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                        if (mList != null) {
                            for (CashButtonListLayout m : mList) {
                                if (m.getPriceFloat() == 0) continue;
                                else {
                                    costo = costo + (m.getPriceFloat() * m.getQuantityInt());
                                    int size1 = m.getTitle().length();
                                    int size1Cost = String.format("%.2f", m.getPriceFloat()).length();
                                    if (m.getQuantityInt() > 1) {
                                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getQuantity() + " x " + String.format("%.2f", m.getPriceFloat()));
                                    }
                                    StringBuffer toPrint1 = new StringBuffer();
                                    if (size1 + size1Cost <= 39) {
                                        toPrint1.append(m.getTitle().toUpperCase());
                                        for (int i = 0; i <= (39 - (size1 + size1Cost)); i++) {
                                            toPrint1.append(" ");
                                        }
                                        toPrint1.append(String.format("%.2f", m.getPriceFloat()));
                                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint1.toString());

                                    } else {

                                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getTitle() + "x" + m.getQuantityInt() + " " + m.getPriceFloat() * m.getQuantityInt());
                                    }
                                }
                            }
                        }
                        if(cashButton.getDiscount()>0.0f){
                            if (String.valueOf(cashButton.getDiscount()).length() <= 32) {
                                String printSconto = "SCONTO";
                                for (int i = 0; i < 32 - String.valueOf(cashButton.getDiscount()).length(); i++) {
                                    printSconto += " ";
                                }
                                printSconto += "-";
                                printSconto += String.format("%.2f", cashButton.getDiscount());
                                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printSconto);
                            }else {
                                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "SCONTO " + "-" + cashButton.getDiscount());
                                costo -= cashButton.getDiscount();
                            }

                        }
                    }else{
                        //PRODOTTO OMAGGIO
                        int size = cashButton.getTitle().length();
                        int sizeCost = "OMAGGIO".length();
                        StringBuffer toPrint = new StringBuffer();
                        if (size + sizeCost <= 39) {
                            toPrint.append(cashButton.getTitle().toUpperCase());
                            for (int i = 0; i <= (39 - (size + sizeCost)); i++) {
                                toPrint.append(" ");
                            }
                            toPrint.append("OMAGGIO");
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint.toString());
                        } else {

                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getTitle() + "x" + cashButton.getQuantityInt() + " OMAGGIO");
                        }

                        //dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, 1, 0, "");
                        ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                        if (mList != null) {
                            for (CashButtonListLayout m : mList) {
                                if (m.getPriceFloat() == 0) continue;
                                else {
                                    int size1 = m.getTitle().length();
                                    int size1Cost = String.format("%.2f", m.getPriceFloat()).length();
                                    if (m.getQuantityInt() > 1) {
                                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getQuantity() + " x omaggio" );
                                    }
                                    StringBuffer toPrint1 = new StringBuffer();
                                    if (size1 + size1Cost <= 39) {
                                        toPrint1.append(m.getTitle().toUpperCase());
                                        for (int i = 0; i <= (39 - (size1 + size1Cost)); i++) {
                                            toPrint1.append(" ");
                                        }
                                        toPrint1.append(" OMAGGIO");
                                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint1.toString());

                                    } else {

                                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getTitle() + "x" + m.getQuantityInt() + " OMAGGIO");
                                    }
                                }
                            }
                        }
                    }
                }
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
                if(discount==0.0f) {
                    if (String.valueOf(costo).length() <= 32) {
                        String printTot = "TOTALE ";
                        for (int i = 0; i < 32 - String.valueOf(costo).length(); i++) {
                            printTot += " ";
                        }
                        printTot += String.format("%.2f", costo);
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printTot);
                    } else {
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TOTALE " + costo);
                    }
                }else{
                    if (String.valueOf(costo).length() <= 32) {
                        String printSub = "SUBTOTALE ";
                        for (int i = 0; i < 29 - String.valueOf(costo).length(); i++) {
                            printSub += " ";
                        }
                        printSub += String.format("%.2f", costo);
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printSub);
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");

                        String printSconto= "SCONTO";
                        for (int i = 0; i < 32 - String.valueOf(discount).length(); i++) {
                            printSconto += " ";
                        }
                        printSconto += "-";
                        printSconto += String.format("%.2f", discount);
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printSconto);

                        String printTot = "TOTALE ";
                        for (int i = 0; i < 32 - String.valueOf(costo).length(); i++) {
                            printTot += " ";
                        }
                        printTot += String.format("%.2f", costo-discount);
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printTot);
                    } else {
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "SUBTOTALE " + costo);
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "SCONTO " + String.valueOf(discount));
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TOTALE " + String.valueOf(costo-discount));
                    }
                }

                dfp.endNonFiscal();
                dfp.close(); // for all command after send a request JavaPOS documentation
                return "03-00";
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                return "03-02";
            }
       /* }else{
            return "03-01";
        }*/
    }

    /**
     * PRINT FISCAL SPLIT BILL
     * @param IP
     * @param paid
     * @param cost
     * @param description
     * @param numberOrderSplit
     * @param paymentType
     * @param quantity
     * @return
     */
    public String printFiscalBillPartial(String IP, float paid, float cost, String description, String numberOrderSplit, int paymentType, int quantity) {
        Log.i("LOG", "DENTRO printFiscalBillPartial");
        //if (ping(IP)) {
                DitronFiscalPrinterService dfp;
                dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
                try {
                    dfp.open();
                    // for all command before send a request JavaPOS documentation
                    dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
                    dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation
                    dfp.beginFiscalReceipt(false);
                    long A = (long) (cost * 100);
                    dfp.printRecItem(description, (long) (cost * 100), quantity * 1000, StaticValue.staticVat, 0, "");
                    dfp.printRecTotal((long) (cost * 100), (long) (paid * 100), String.valueOf(paymentType)); // "1"-> Cash
                    dfp.printRecMessage("orderNumber : " + numberOrderSplit);
                    /**
                     * 1=sconto
                     * 2=maggiorazione
                     * 3=sconto 1%
                     * 4 = maggiorazione 1%
                     * dfp.printRecItemAdjustment(1, cashButton.getTitle(), (long) (discount*100), 1);
                     */
                    dfp.endFiscalReceipt(false);
                    dfp.close(); // for all command after send a request JavaPOS documentation
                    Log.i("LOG", "RETURN 1");
                    return "04-00";
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("LOG", "RETURN 2");
                    return "04-02";
                }
            /*} else {
            Log.i("LOG", "RETURN 3");
                return "04-01";
            }*/

    }

    /**
     *  PRINT FISCAL SPLIT BILL AND THE
     * FISCAL RESUME
     * DO WE HAVE TO SPLIT THIS????
     * @param billId
     * @param IP
     * @param paid
     * @param cost
     * @param credit
     * @param description
     * @param numberOrderSplit
     * @param products
     * @param modifiers
     * @param paymentType
     * @return
     */
    public String printFiscalBillWithNonFiscal(
            String billId,
            String IP,
            float paid,
            float cost,
            float credit,
            String description,
            String numberOrderSplit,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            int paymentType
    ) {
     //   if(ping(IP)) {
            DitronFiscalPrinterService dfp;
            dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
            try {
                dfp.open();
                // for all command before send a request JavaPOS documentation
                dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
                dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation
                dfp.beginFiscalReceipt(false);
                dfp.printRecItem(description, (long) parseToDouble(cost*100), 1* 1000, StaticValue.staticVat, 0, "");
                dfp.printRecTotal((long) parseToDouble(cost*100), (long) parseToDouble(paid*100), String.valueOf(paymentType)); // "1"-> Cash
                if(credit>0){
                   dfp.printRecMessage("CREDITO " + credit + " EURO #"+ billId);
                }
                dfp.printRecMessage("Order Number : " + numberOrderSplit);
                dfp.endFiscalReceipt(false);
                dfp.beginNonFiscal();
                float costo = 0.0f;
                String printEuro = "                                    EURO";
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printEuro);
                for (CashButtonLayout cashButton : products) {
                    if(cashButton.getQuantityInt()>1){
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,cashButton.getQuantity()+ " x " +String.format("%.2f", cashButton.getPriceFloat()));
                    }
                    int size = cashButton.getTitle().length();
                    int sizeCost = String.format("%.2f", cashButton.getPriceFloat()).length();
                    StringBuffer toPrint = new StringBuffer();
                    if(size+sizeCost<=39){
                        toPrint.append(cashButton.getTitle().toUpperCase());
                        for(int i=0; i<=(39-(size+sizeCost)); i++){
                            toPrint.append(" ");
                        }
                        toPrint.append(String.format("%.2f", cashButton.getPriceFloat()));
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,toPrint.toString());
                    }else {

                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getTitle() + "x" + cashButton.getQuantityInt() + " " + cashButton.getPriceFloat() * cashButton.getQuantityInt());
                    }
                    costo = costo +  (cashButton.getPriceFloat()  * cashButton.getQuantityInt());
                    ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                    if (mList != null) {
                        for (CashButtonListLayout m : mList) {
                                costo = costo +  (m.getPriceFloat() * m.getQuantityInt());
                                int size1 = m.getTitle().length();
                                int size1Cost = String.format("%.2f", m.getPriceFloat()).length();
                                if(m.getQuantityInt()>1){
                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,m.getQuantity()+ " x " +String.format("%.2f", m.getPriceFloat()));
                                }
                                StringBuffer toPrint1 = new StringBuffer();
                                if(size1+size1Cost<=39){
                                    toPrint1.append(m.getTitle().toUpperCase());
                                    for(int i=0; i<=(39-(size1+size1Cost)); i++){
                                        toPrint1.append(" ");
                                    }
                                    toPrint1.append(String.format("%.2f", m.getPriceFloat()));
                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,toPrint1.toString());
                                }else {

                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,m.getTitle()+"x"+m.getQuantityInt()+" " + m.getPriceFloat() * m.getQuantityInt());
                                }
                        }
                    }
                }
                if(String.valueOf(costo).length()<=32){
                    String printTot = "TOTALE ";
                    for(int i=0; i<32-String.valueOf(costo).length(); i++){
                        printTot+= " ";
                    }
                    printTot+=String.format("%.2f", costo);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printTot);
                }else {
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TOTALE " + costo);
                }
                dfp.endNonFiscal();
                dfp.close(); // for all command after send a request JavaPOS documentation
                return "06-00";
            } catch (Exception e) {
                e.printStackTrace();
                return "06-02";
            }
       /* }else{
            return "06-01";
        }*/
    }


    public String printBill(String IP, SubdivisionItem item, float paid, float cost, float credit) {
      //  if(ping(IP)) {
            DitronFiscalPrinterService dfp;
            dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
            try {
                if (item.getItems().size() > 0) {
                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = ((PaymentActivity) context).setSplitillToPrint(item.getItems());
                    modifiersList = map;
                    dfp.open();
                    // for all command before send a request JavaPOS documentation
                    dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
                    dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation
                    dfp.beginFiscalReceipt(false);
                    long costo = 0;
                    for (CashButtonLayout cashButton : item.getItems()) {
                        if(cashButton.getHomage()!=1) {
                            Float discount = cashButton.getDiscount();
                            if (discount != null) {
                                if (discount >= (cashButton.getPriceFloat() * (cashButton.getQuantityInt()))) {
                                    dfp.printRecItem(cashButton.getTitle(), 0, (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                                    //costo = costo + (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt() - cashButton.getHomage())-discount*100);
                                    discount = discount - (cashButton.getPriceFloat() * (cashButton.getQuantityInt()));
                                    ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                    if (mList != null) {
                                        for (CashButtonListLayout m : mList) {
                                            if (m.getPriceFloat() == 0) continue;
                                            else {
                                                if (discount >= (m.getPriceFloat() * (m.getQuantityInt()))) {
                                                    //costo = costo + (long) ((m.getPriceFloat() * 100) * (m.getQuantityInt() - cashButton.getHomage())-discount);
                                                    dfp.printRecItem(m.getTitle(), 0, (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                                    discount = discount - (m.getPriceFloat() * (m.getQuantityInt()));

                                                } else {
                                                    if (discount > 0) {
                                                        costo = costo + (long) ((m.getPriceFloat() * 100) * (m.getQuantityInt()) - discount * 100);
                                                        //quantity is set to zero or cash throw erro becaus calcolate element cost by cost/quantity.....try 2/3......
                                                        dfp.printRecItem(m.getTitle(), (long) ((m.getPriceFloat() * 100) * (m.getQuantityInt()) - discount * 100), 1000, m.getVat(), 0, "");
                                                        discount = 0.0f;
                                                    } else {
                                                        costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                                        dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");

                                                    }
                                                }
                                            }
                                        }
                                    }
                                    dfp.printRecMessage(printRowForDiscount(cashButton.getDiscount()));
                                } else {
                                    dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt()) - discount * 100), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                                    costo = costo + (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt()) - discount * 100);
                                    ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                    if (mList != null) {
                                        for (CashButtonListLayout m : mList) {
                                            if (m.getPriceFloat() == 0) continue;
                                            else {
                                                costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                                dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                            }
                                        }
                                    }
                                    dfp.printRecMessage(printRowForDiscount(cashButton.getDiscount()));
                                }
                            } else {
                                dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                                costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());
                                ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                                if (mList != null) {
                                    for (CashButtonListLayout m : mList) {
                                        if (m.getPriceFloat() == 0) continue;
                                        else {
                                            costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                            dfp.printRecItem(m.getTitle(), (long) (m.getPriceFloat() * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                        }
                                    }
                                }
                            }
                        }else{
                            dfp.printRecMessage("OMAGGIO");
                            dfp.printRecItem(cashButton.getTitle(), (long) ((0.0f * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, cashButton.getVat(), 0, "");
                            //costo = costo + (long) (cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt());
                            ArrayList<CashButtonListLayout> mList = modifiersList.get(cashButton);
                            if (mList != null) {
                                for (CashButtonListLayout m : mList) {
                                    if (m.getPriceFloat() == 0) continue;
                                    else {
                                        //costo = costo + (long) (m.getPriceFloat() * 100) * (m.getQuantityInt());
                                        dfp.printRecItem(m.getTitle(), (long) (0.0f * 100) * (m.getQuantityInt()), (m.getQuantityInt()) * 1000, m.getVat(), 0, "");
                                    }
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
                    dfp.printRecTotal(costo, pagato, "1"); // "1"-> Cash
                    dfp.endFiscalReceipt(false);
                    dfp.close(); // for all command after send a request JavaPOS documentation
                    return "06-00";
                } else {
                    //se non ho item da stampare non faccio nulla e ritorno 00
                    return "06-00";
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "06-02";
            }
     /*   }else{
            //connect cable
            return "06-01";
        }*/
    }

    public void reprintLastBill(String IP){
        int[] data = new int[1];
        String[] res = new String[1];

        try{

            DitronFiscalPrinterService dfp;
            // Change only ip address and port
            dfp = new DitronFiscalPrinterService(1, true, false, StaticValue.IP, StaticValue.port);
            dfp.open();
            dfp.claim(StaticValue.claim);
            dfp.setDeviceEnabled(true);


            try {
                //dfp.beginNonFiscal();
                dfp.getData(DitronFiscalPrinterService.FPTR_GD_RECEIPT_NUMBER, data, res);
                dfp.printDuplicateReceipt();

            }catch (JposException ultScontr) {

                Toast.makeText(context, res[0], Toast.LENGTH_SHORT).show();
            }


            dfp.close();

        }catch (JposException e){
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void printReport(String IP, int type){

        try {
            DitronFiscalPrinterService dfp = new DitronFiscalPrinterService(1, true, false, StaticValue.IP, StaticValue.port);
            dfp.open();
            dfp.claim(StaticValue.claim);
            dfp.setDeviceEnabled(true);
            //dfp.printZReport();
            switch (type) {
                case FISCAL_ZERO:
                    dfp.printZReport();
                    break;
                case SIMPLE_REPORT:
                    dfp.printXReport();
                    break;
            }
           dfp.close();
        } catch (JposException e) {
            e.printStackTrace();
        }
    }

    public void printInvoice1(ClientInfo client, String IP){
        DitronFiscalPrinterService dfp;
        dfp= new DitronFiscalPrinterService(1, true, false, StaticValue.IP, StaticValue.port);


        try {
            dfp.open();
            dfp.claim(StaticValue.claim);
            dfp.setDeviceEnabled(true);

            /*// todo: set properly
            Object[] invoice_identification = {"2", "A"};
            dfp.directIO(233,new int[]{}, invoice_identification);*/

            Object[] invoice_identification = {"anno", "numero", "nomeLocale"};
            dfp.directIO(232,new int[]{}, invoice_identification);

            if(client.getCodice_fiscale().equals("")) {

                Object[] invoice_header = {client.getCompany_name(),
                        client.getCompany_vat_number(),
                        client.getCompany_address() + ", " + client.getCompany_city(),
                        client.getCompany_country()};

                dfp.directIO(232, new int[]{}, invoice_header);
            }else{
                Object[] invoice_header = {client.getName()+" " + client.getSurname(),
                        client.getCodice_fiscale(),
                        client.getCompany_vat_number(),
                        client.getCompany_address() + ", " + client.getCompany_city(),
                        client.getCompany_country()};

                dfp.directIO(232, new int[]{}, invoice_header);
            }




            dfp.close();


        } catch (JposException e) {
            e.printStackTrace();
        }


    }

    public void printInvoice(
            int numeroFattura,
            ClientInfo clientInfo,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            float discount,

            int tableNumber,
            int orderNumber,
            String roomName
        ) {
        DitronFiscalPrinterService dfp;
        dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
        ArrayList<Customer> addedCustomer = new ArrayList<Customer>();
        float costo = 0.0f;
        try {
            dfp.open();
            // for all command before send a request JavaPOS documentation
            dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
            dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation
            dfp.beginNonFiscal();
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "Fattura Di Cortesia: " + year + "/" + numeroFattura + "/" +StaticValue.shopName);
            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "Data: " + day + "/" + month + "/" + year);
            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "----------------------------------------");
            if (clientInfo.getCodice_fiscale().equals("")) {
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_vat_number());
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_name());
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_address()  );
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_postal_code() );
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_city() );
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_country());

            } else {
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_vat_number());
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCodice_fiscale());
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getName() + " " + clientInfo.getSurname() );
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_address()  );
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_postal_code() );
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_city() );
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, clientInfo.getCompany_country());

            }

            if(tableNumber!=-1 && tableNumber!=-11) {
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "STANZA  " + roomName);
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TAVOLO  " + tableNumber);
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
            }else{
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "NUMERO ORDINE  " + orderNumber);
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
            }

            String printEuro = "                                    EURO";
            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printEuro);
            for (CashButtonLayout cashButton : products) {
                /*if(cashButton.getClientPosition()!=0){
                    Customer customer = customers.get(cashButton.getClientPosition()-1);
                    if(!addedCustomer.contains(customer)){
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, " " +customer.getDescription());
                        addedCustomer.add(customer);

                    }
                }
*/
                if(cashButton.getHomage()==0) {
                    if (cashButton.getQuantityInt() > 1) {
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getQuantity() + " x " + String.format("%.2f", cashButton.getPriceFloat()));
                    }

                    int size = cashButton.getTitle().length();
                    int sizeCost = String.format("%.2f", cashButton.getPriceFloat()*cashButton.getQuantityInt()).length();
                    StringBuffer toPrint = new StringBuffer();
                    if (size + sizeCost <= 39) {
                        toPrint.append(cashButton.getTitle().toUpperCase());
                        for (int i = 0; i <= (39 - (size + sizeCost)); i++) {
                            toPrint.append(" ");
                        }
                        toPrint.append(String.format("%.2f", cashButton.getPriceFloat()*cashButton.getQuantityInt()));
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint.toString());
                    } else {

                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getTitle() + "x" + cashButton.getQuantityInt() + " " + cashButton.getPriceFloat() * cashButton.getQuantityInt());
                    }

                    //dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, 1, 0, "");
                    costo = costo + (cashButton.getPriceFloat() * cashButton.getQuantityInt());
                    ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                    if (mList != null) {
                        for (CashButtonListLayout m : mList) {
                            if (m.getPriceFloat() == 0) continue;
                            else {
                                costo = costo + (m.getPriceFloat() * m.getQuantityInt());
                                int size1 = m.getTitle().length();
                                int size1Cost = String.format("%.2f", m.getPriceFloat()).length();
                                if (m.getQuantityInt() > 1) {
                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getQuantity() + " x " + String.format("%.2f", m.getPriceFloat()));
                                }
                                StringBuffer toPrint1 = new StringBuffer();
                                if (size1 + size1Cost <= 39) {
                                    toPrint1.append(m.getTitle().toUpperCase());
                                    for (int i = 0; i <= (39 - (size1 + size1Cost)); i++) {
                                        toPrint1.append(" ");
                                    }
                                    toPrint1.append(String.format("%.2f", m.getPriceFloat()));
                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint1.toString());

                                } else {

                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getTitle() + "x" + m.getQuantityInt() + " " + m.getPriceFloat() * m.getQuantityInt());
                                }
                            }
                        }
                    }
                    if(cashButton.getDiscount()>0.0f){
                        if (String.valueOf(cashButton.getDiscount()).length() <= 32) {
                            String printSconto = "SCONTO";
                            for (int i = 0; i < 32 - String.valueOf(cashButton.getDiscount()).length(); i++) {
                                printSconto += " ";
                            }
                            printSconto += "-";
                            printSconto += String.format("%.2f", cashButton.getDiscount());
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printSconto);
                        }else {
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "SCONTO " + "-" + cashButton.getDiscount());
                            costo -= cashButton.getDiscount();
                        }

                    }
                }else{
                    //PRODOTTO OMAGGIO
                    int size = cashButton.getTitle().length();
                    int sizeCost = "OMAGGIO".length();
                    StringBuffer toPrint = new StringBuffer();
                    if (size + sizeCost <= 39) {
                        toPrint.append(cashButton.getTitle().toUpperCase());
                        for (int i = 0; i <= (39 - (size + sizeCost)); i++) {
                            toPrint.append(" ");
                        }
                        toPrint.append("OMAGGIO");
                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint.toString());
                    } else {

                        dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getTitle() + "x" + cashButton.getQuantityInt() + " OMAGGIO");
                    }

                    //dfp.printRecItem(cashButton.getTitle(), (long) ((cashButton.getPriceFloat() * 100) * (cashButton.getQuantityInt())), (cashButton.getQuantityInt()) * 1000, 1, 0, "");
                    ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                    if (mList != null) {
                        for (CashButtonListLayout m : mList) {
                            if (m.getPriceFloat() == 0) continue;
                            else {
                                int size1 = m.getTitle().length();
                                int size1Cost = String.format("%.2f", m.getPriceFloat()).length();
                                if (m.getQuantityInt() > 1) {
                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getQuantity() + " x omaggio" );
                                }
                                StringBuffer toPrint1 = new StringBuffer();
                                if (size1 + size1Cost <= 39) {
                                    toPrint1.append(m.getTitle().toUpperCase());
                                    for (int i = 0; i <= (39 - (size1 + size1Cost)); i++) {
                                        toPrint1.append(" ");
                                    }
                                    toPrint1.append(" OMAGGIO");
                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, toPrint1.toString());

                                } else {

                                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, m.getTitle() + "x" + m.getQuantityInt() + " OMAGGIO");
                                }
                            }
                        }
                    }
                }
            }
            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
            if(discount==0.0f) {
                if (String.valueOf(costo).length() <= 32) {
                    String printTot = "TOTALE ";
                    for (int i = 0; i < 32 - String.valueOf(costo).length(); i++) {
                        printTot += " ";
                    }
                    printTot += String.format("%.2f", costo);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printTot);
                } else {
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TOTALE " + costo);
                }
            }else{
                if (String.valueOf(costo).length() <= 32) {
                    String printSub = "SUBTOTALE ";
                    for (int i = 0; i < 29 - String.valueOf(costo).length(); i++) {
                        printSub += " ";
                    }
                    printSub += String.format("%.2f", costo);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printSub);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");

                    String printSconto= "SCONTO";
                    for (int i = 0; i < 32 - String.valueOf(discount).length(); i++) {
                        printSconto += " ";
                    }
                    printSconto += "-";
                    printSconto += String.format("%.2f", discount);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printSconto);

                    String printTot = "TOTALE ";
                    for (int i = 0; i < 32 - String.valueOf(costo).length(); i++) {
                        printTot += " ";
                    }
                    printTot += String.format("%.2f", costo-discount);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printTot);
                } else {
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "SUBTOTALE " + costo);
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "");
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "SCONTO " + String.valueOf(discount));
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TOTALE " + String.valueOf(costo-discount));
                }
            }


            dfp.endNonFiscal();
            dfp.close();


        } catch (JposException e) {
            e.printStackTrace();
        }

    }

    public boolean checkPaper(String IP){
        DitronFiscalPrinterService dfp = null;
        dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
        try {
            boolean result = dfp.getCapJrnEmptySensor();
            return result;
        } catch (JposException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String printFiscalBillPartialList(
            ArrayList<LeftPayment> left,
            int paymentType,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers
            ) {
        DecimalFormat df_1 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_1.setMaximumFractionDigits(2);
        df_1.setMinimumFractionDigits(2);
        DecimalFormat df = new DecimalFormat("##.00");
        DecimalFormat df_2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ITALIAN);
        df_2.setMaximumFractionDigits(0);
        df_2.setMinimumFractionDigits(0);
        DitronFiscalPrinterService dfp;
        dfp = new DitronFiscalPrinterService(typeDisplay, true, false, StaticValue.IP, StaticValue.port);
        try {
            dfp.open();
            // for all command before send a request JavaPOS documentation
            dfp.claim(StaticValue.claim); // for all command before send a request JavaPOS documentation
            dfp.setDeviceEnabled(true); // for all command before send a rquest JavaPOS documentation
            for(LeftPayment myLeft : left) {

                    dfp.beginFiscalReceipt(false);
                    if(myLeft.getPaid()<myLeft.getCost())
                        dfp.printRecItem("PER AMOUNT", (long) parseToDouble(myLeft.getPaid()*100), 1* 1000, StaticValue.staticVat, 0, "");
                    else
                        dfp.printRecItem("PER AMOUNT", (long) parseToDouble(myLeft.getCost()*100), 1* 1000, StaticValue.staticVat, 0, "");

                    dfp.printRecTotal((long) parseToDouble(myLeft.getPaid()*100), (long) parseToDouble(myLeft.getPaid()*100), String.valueOf(paymentType)); // "1"-> Cash

                    dfp.endFiscalReceipt(false);
            }
            dfp.beginNonFiscal();
            float costo = 0.0f;
            String printEuro = "                                    EURO";
            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printEuro);
            for (CashButtonLayout cashButton : products) {
                if(cashButton.getQuantityInt()>1){
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,cashButton.getQuantity()+ " x " +String.format("%.2f", cashButton.getPriceFloat()));
                }
                int size = cashButton.getTitle().length();
                int sizeCost = String.format("%.2f", cashButton.getPriceFloat()).length();
                StringBuffer toPrint = new StringBuffer();
                if(size+sizeCost<=39){
                    toPrint.append(cashButton.getTitle().toUpperCase());
                    for(int i=0; i<=(39-(size+sizeCost)); i++){
                        toPrint.append(" ");
                    }
                    toPrint.append(String.format("%.2f", cashButton.getPriceFloat()));
                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,toPrint.toString());
                }else {

                    dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, cashButton.getTitle() + "x" + cashButton.getQuantityInt() + " " + cashButton.getPriceFloat() * cashButton.getQuantityInt());
                }
                costo = costo +  (cashButton.getPriceFloat()  * cashButton.getQuantityInt());
                ArrayList<CashButtonListLayout> mList = modifiers.get(cashButton);
                if (mList != null) {
                    for (CashButtonListLayout m : mList) {
                        costo = costo +  (m.getPriceFloat() * m.getQuantityInt());
                        int size1 = m.getTitle().length();
                        int size1Cost = String.format("%.2f", m.getPriceFloat()).length();
                        if(m.getQuantityInt()>1){
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,m.getQuantity()+ " x " +String.format("%.2f", m.getPriceFloat()));
                        }
                        StringBuffer toPrint1 = new StringBuffer();
                        if(size1+size1Cost<=39){
                            toPrint1.append(m.getTitle().toUpperCase());
                            for(int i=0; i<=(39-(size1+size1Cost)); i++){
                                toPrint1.append(" ");
                            }
                            toPrint1.append(String.format("%.2f", m.getPriceFloat()));
                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,toPrint1.toString());
                        }else {

                            dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT,m.getTitle()+"x"+m.getQuantityInt()+" " + m.getPriceFloat() * m.getQuantityInt());
                        }
                    }
                }
            }
            if(String.valueOf(costo).length()<=32){
                String printTot = "TOTALE ";
                for(int i=0; i<32-String.valueOf(costo).length(); i++){
                    printTot+= " ";
                }
                printTot+=String.format("%.2f", costo);
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, printTot);
            }else {
                dfp.printNormal(DitronEcrDefine.FPTR_S_RECEIPT, "TOTALE " + costo);
            }
            dfp.endNonFiscal();
            dfp.close(); // fo
        }catch (JposException e) {
            e.printStackTrace();

        }

        return "";

    }



}
