package com.example.blackbox.printer;

import android.util.Log;

import com.example.blackbox.R;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by Fabrizio on 25/03/2019.
 */

public class PrinterEpson{
    private String deviceName;
    private String billId;
    private String orderNumber;
    private String IP;
    private float paid;
    private float cost;
    private float credit;
    private float creditL;
    private int paymentType;
    private float totalDiscount;
    private ArrayList<CashButtonLayout> products;
    private Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers;
    private ArrayList<Customer> customers;
    private ArrayList<Integer> customersAdded;
    private String list = "";
    private int sale_number = 0;
    private int tableNumber;
    private String roomName;
    private static int max_item_desc_len = 32;
    private static int TOTAL_ROW_LENGTH = 50;
    private String req = "";
    private int quantity;
    private String numberOrderSplit;
    private String description;

    //EPSON PARAMETERS
    private static final String TAG = "PRINTER_EPSON";
    public ClientInfo clientInfo;
    public void setClientInfo(ClientInfo c) {clientInfo =c;}
    public int numeroFattura;
    public void setNumeroFattura(int i){numeroFattura =i;}
    private String url;

    public String getNumberOrderSplit(){return numberOrderSplit;}

    public void setNumberOrderSplit(String o){this.numberOrderSplit = o;}

    public String getDescription(){return description;}

    public void setDescription(String o){this.description = o;}

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

    public void setTotalDiscount(float d){this.totalDiscount = d;}

    public float getTotalDiscount(){return totalDiscount;}

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
    public void setQuantity(int q){this.quantity = q;}
    public int getQuantity(){return this.quantity;}
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
    public ArrayList<LeftPayment> left;
    public void setLeftPayment(ArrayList<LeftPayment> l){left = l;}
    public void setUrl(String url){
        this.url = "http://" + url + "/cgi-bin/fpmate.cgi?devid=local_printer&timeout=1000";
    }
    public String getUrl(){
        return this.url;
    }

    //must set req before calling this method
    public void setPrinterThread(){
        HttpPost httppost = new HttpPost(url);

        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used.
        int timeoutConnection = 3000;
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);

        Log.d("SCONTRINO", req);
        try {
            StringEntity entity = new StringEntity(req, HTTP.UTF_8);
            entity.setContentType("text/xml charset=utf-8");
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            // Receive response document
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(response.getEntity().getContent());
            Element el = (Element) doc.getElementsByTagName("response").item(0);
            Log.i("STATO STAMPANTE", "Success: " + el.getAttribute("success") + "\n"
                    + "Code: " + el.getAttribute("code") + "\n" + "Status: " + el.getAttribute("status"));
        }
        catch (Exception e) {
            Log.d("ERRORE PRINTER", e.getLocalizedMessage());
        }
    }

    public void printFiscalBill(){
        for(CashButtonLayout c : products){
            addSellItem(c);
        }

        // Create print document(String)
        req = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"" + R.array.company_head_printer + "\">" +
                "<s:Body>" +
                // data
                "<printerFiscalReceipt>" +
                "<beginFiscalReceipt operator=\"10\" />" +
                // list
                list +
                // end list
                ((totalDiscount != 0)? "<printRecItemAdjustment operator=\"10\" adjustmentType=\"0\" description=\"SCONTO\" amount=\"" + String.valueOf(totalDiscount) +
                        "\" department=\"1\" justification=\"1\" />": "") +
                "<printRecTotal operator=\"10\" description=\"" +
                switchPayment(paymentType) + "\" payment=\"" + (paid) + "\" paymentType=\""
                + paymentType + "\" index=\"0\" justification=\"1\" />" +
                "<printRecMessage  operator=\"10\" messageType=\"3\" index=\"1\" font=\"4\" message=\"Grazie e Arrivederci\" />" +
                "<endFiscalReceipt operator=\"10\" />" +
                "</printerFiscalReceipt>" +
                // end data
                "</s:Body>" +
                "</s:Envelope>";
    }

    public void printNonFiscalBill(){
        if(customers != null && customers.size() > 0)
            customersAdded = new ArrayList<>();
        for(CashButtonLayout c : products){
            if(customersAdded != null){
                Customer cost;
                if(c.getClientPosition() == 0)
                    cost = customers.get(c.getClientPosition());
                else
                    cost = customers.get(c.getClientPosition()-1);
                if(!customersAdded.contains(cost.getCustomerId()))
                    addCustomerItem(cost);
            }
            addNonFiscalSellItem(c);
        }
        DecimalFormat df = new DecimalFormat("#0.00");

        // Create print document(String)
        req = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"" + R.array.company_head_printer + "\">" +
                "<s:Body>" +
                // data
                "<printerNonFiscal>" +
                "<beginNonFiscal operator=\"\" />" +
                ((tableNumber != -1 && tableNumber != -11)? "<printNormal operator=\"\" data=\"                 STANZA " + roomName + "\" font=\"2\" />"
                        + "<printNormal operator=\"\" data=\"                 TAVOLO " + String.valueOf(tableNumber) + "\" font=\"2\" />" :
                        "<printNormal operator=\"\" data=\"                NUMERO ORDINE " + orderNumber + "\" font=\"2\" />") +
                "<printNormal operator=\"\" data=\"\" />" +
                // list
                list +
                // end list
                "<printNormal operator=\"\" data=\"TOTALE" + padLeft(df.format(cost-totalDiscount).replaceAll("\\.", "\\,"), 40)
                + "\" font=\"3\" />" +
                "<endNonFiscal operator=\"\" />" +
                "</printerNonFiscal>" +
                // end data
                "</s:Body>" +
                "</s:Envelope>";
    }

    public void printInvoice(int i){
        if(customers != null && customers.size() > 0)
            customersAdded = new ArrayList<>();
        for(CashButtonLayout c : products){
            if(customersAdded != null){
                Customer cost;
                if(c.getClientPosition() == 0)
                    cost = customers.get(c.getClientPosition());
                else
                    cost = customers.get(c.getClientPosition()-1);
                if(!customersAdded.contains(cost.getCustomerId()))
                    addCustomerItem(cost);
            }
            addNonFiscalSellItem(c);
        }
        DecimalFormat df = new DecimalFormat("#0.00");

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        // Create print document(String)
        req = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"" + R.array.company_head_printer + "\">" +
                "<s:Body>" +
                // data
                "<printerNonFiscal>" +
                "<beginNonFiscal operator=\"\" />" +
                //intestazione fattura
                "<printNormal operator=\"\" data=\"Fattura Di Cortesia: " + String.valueOf(year)
                + "/" + String.valueOf(numeroFattura) + "/" + StaticValue.shopName + "\" font=\"2\" />" +
                "<printNormal operator=\"\" data=\"Data: " + String.valueOf(year) + "/" + String.valueOf(month) + "/"
                + String.valueOf(day) + "\" font=\"2\" />" +
                //dettagli cliente
                ((clientInfo.getCodice_fiscale().equals(""))?
                        "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_vat_number() + "\" font=\"1\" />" +
                        "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_name() + "\" font=\"1\" />" +
                        "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_address() + "\" font=\"1\" />" +
                        "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_postal_code() + "\" font=\"1\" />" +
                        "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_city() + "\" font=\"1\" />" +
                        "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_country() + "\" font=\"1\" />"
                        :
                        "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_vat_number() + "\" font=\"1\" />" +
                                "<printNormal operator=\"\" data=\"" + clientInfo.getCodice_fiscale() + "\" font=\"1\" />" +
                                "<printNormal operator=\"\" data=\"" + clientInfo.getName() + " " + clientInfo.getSurname() + "\" font=\"1\" />" +
                                "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_address() + "\" font=\"1\" />" +
                                "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_postal_code() + "\" font=\"1\" />" +
                                "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_city() + "\" font=\"1\" />" +
                                "<printNormal operator=\"\" data=\"" + clientInfo.getCompany_country() + "\" font=\"1\" />") +
                //dettagli ordine
                ((tableNumber != -1 && tableNumber != -11)?
                        "<printNormal operator=\"\" data=\"STANZA"
                        + padLeft(roomName, (TOTAL_ROW_LENGTH - roomName.length() - 8)) + "\" font=\"2\" />"
                        + "<printNormal operator=\"\" data=\"TAVOLO" + padLeft(String.valueOf(tableNumber),
                        TOTAL_ROW_LENGTH - String.valueOf(tableNumber).length() - 8) + "\" font=\"2\" />"
                        :
                        "<printNormal operator=\"\" data=\"NUMERO ORDINE" + padLeft(orderNumber, TOTAL_ROW_LENGTH-orderNumber.length()-15) + "\" font=\"2\" />") +
                "<printNormal operator=\"\" data=\"\" />" +
                // list
                list +
                // end list
                "<printNormal operator=\"\" data=\"TOTALE" + padLeft(df.format(cost-totalDiscount).replaceAll("\\.", "\\,"), 40)
                + "\" font=\"3\" />" +
                ((i == 1)?
                        "<printNormal operator=\"\" data=\"\" font=\"1\" />" +
                        "<printNormal operator=\"\" data=\"SCONTRINO DI CORTESIA PER CLIENTE\" font=\"2\" />"
                : "") +
                "<endNonFiscal operator=\"\" />" +
                "</printerNonFiscal>" +
                // end data
                "</s:Body>" +
                "</s:Envelope>";
    }

    public void printFiscalWithNonFiscal(){
        //fiscal part
        for(LeftPayment myLeft : left){
            float tot;
            if(myLeft.getPaid() < myLeft.getCost())
                tot = myLeft.getPaid();
            else
                tot = myLeft.getCost();
            // Create print document(String)
            req = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                    "<s:Envelope xmlns:s=\"" + R.array.company_head_printer + "\">" +
                    "<s:Body>" +
                    // data
                    "<printerFiscalReceipt>" +
                    "<beginFiscalReceipt operator=\"10\" />" +
                    // list
                    "<printRecItem operator=\"10\" description=\"PER AMOUNT\" quantity=\"" + 1 + "\" unitPrice=\"" + tot +
                                    "\" department=\"1\" justification=\"1\" />" +
                    // end list
                    "<printRecTotal operator=\"10\" description=\"" +
                    switchPayment(paymentType) + "\" payment=\"" + tot + "\" paymentType=\""
                    + paymentType + "\" index=\"0\" justification=\"1\" />" +
                    "<printRecMessage  operator=\"10\" messageType=\"3\" index=\"1\" font=\"4\" message=\"Grazie e Arrivederci\" />" +
                    "<endFiscalReceipt operator=\"10\" />" +
                    "</printerFiscalReceipt>" +
                    // end data
                    "</s:Body>" +
                    "</s:Envelope>";

            setPrinterThread();
        }
        //non fiscal part
        printNonFiscalBill();
        setPrinterThread();
    }

    private void addSellItem(CashButtonLayout item) {
        if (item.getTitle().length() < max_item_desc_len) {
            //item.padRight(max_item_desc_len)
        }
        list = list + "<printRecItem operator=\"10\" description=\"" + item.getTitle() + "\" quantity=\"" + item.getQuantity() + "\" unitPrice=\"" + item.getPrice() +
                "\" department=\"1\" justification=\"1\" />";
        if(modifiers != null && modifiers.get(item) != null && modifiers.get(item).size() != 0){
            ArrayList<CashButtonListLayout> mods = modifiers.get(item);
            for(CashButtonListLayout cbll : mods){
                if(Float.parseFloat(cbll.getPrice()) > 0.0f){
                    list = list + "<printRecItem operator=\"10\" description=\"" + cbll.getTitle() + "\" quantity=\""
                            + cbll.getQuantity() + "\" unitPrice=\"" + cbll.getPrice() + "\" department=\"1\" justification=\"1\" />";
                    sale_number++;
                }
            }
        }
        sale_number = sale_number + 1;
    }

    private void addNonFiscalSellItem(CashButtonLayout item) {
        if (item.getTitle().length() < max_item_desc_len) {
            //item.padRight(max_item_desc_len)
        }
        DecimalFormat df = new DecimalFormat("#0.00");
        int dflength = df.format(item.getPriceFloat()*item.getQuantityInt()).replaceAll("\\.", "\\,").length();
        if(item.getPriceFloat()*item.getQuantityInt() >= 10.0f && item.getPriceFloat()*item.getQuantityInt() < 100.0f)
            dflength--;
        else if(item.getPriceFloat()*item.getQuantityInt() >= 100.0f && item.getPriceFloat()*item.getQuantityInt() < 1000.0f)
            dflength = dflength - 2;
        else if(item.getPriceFloat()*item.getQuantityInt() >= 1000.0f)
            dflength = dflength - 3;
        //one item
        if(item.getQuantityInt() == 1)
            list = list + "<printNormal operator=\"\" data=\"" + item.getTitle()
                    + padLeft(df.format(item.getPriceFloat()).replaceAll("\\.", "\\,"),
                    (TOTAL_ROW_LENGTH-item.getTitle().length()-dflength)) + "\" font=\"1\" />";
        //more than one
        else
            list = list + "<printNormal operator=\"\" data=\"" + padLeft(item.getQuantity() + " X "
                    + df.format(item.getPriceFloat()).replaceAll("\\.", "\\,"), 15) + "\" />" +
                    "<printNormal operator=\"\" data=\"" + item.getTitle()
                    + padLeft(df.format(item.getPriceFloat()*item.getQuantityInt()).replaceAll("\\.", "\\,"),
                    (TOTAL_ROW_LENGTH-item.getTitle().length()-dflength)) + "\" font=\"1\" />";
        //discount
        if(item.getDiscount() != null && item.getDiscount() > 0.0f){
            list = list + "<printNormal operator=\"\" data=\"Sconto"
                    + padLeft("-" + df.format(item.getDiscount()).replaceAll("\\.", "\\,"), TOTAL_ROW_LENGTH - 5) + "\" font=\"1\" />";
        }
        if(modifiers != null && modifiers.get(item) != null && modifiers.get(item).size() != 0){
            ArrayList<CashButtonListLayout> mods = modifiers.get(item);
            for(CashButtonListLayout cbll : mods){
                if(Float.parseFloat(cbll.getPrice()) > 0.0f){
                    int df1length = df.format(cbll.getPriceFloat()).replaceAll("\\.", "\\,").length();
                    if(cbll.getPriceFloat()*cbll.getQuantityInt() >= 10.0f && cbll.getPriceFloat()*cbll.getQuantityInt() < 100.0f)
                        df1length--;
                    else if(cbll.getPriceFloat()*cbll.getQuantityInt() >= 100.0f && cbll.getPriceFloat()*cbll.getQuantityInt() < 1000.0f)
                        df1length = df1length - 2;
                    else if(cbll.getPriceFloat()*cbll.getQuantityInt() >= 1000.0f)
                        df1length = df1length - 3;
                    if(cbll.getQuantityInt() == 1)
                        list = list + "<printNormal operator=\"\" data=\"" + cbll.getTitle()
                                + padLeft(df.format(cbll.getPriceFloat()).replaceAll("\\.", "\\,"),
                                (TOTAL_ROW_LENGTH-cbll.getTitle().length()-df1length)) + "\" font=\"1\" />";
                    else
                        list = list + "<printNormal operator=\"\" data=\"" + padLeft(cbll.getQuantity() + " X "
                                + df.format(cbll.getPriceFloat()).replaceAll("\\.", "\\,"), 15) + "\" />"
                                + "<printNormal operator=\"\" data=\"" + cbll.getTitle()
                                + padLeft(df.format(cbll.getPriceFloat()*cbll.getQuantityInt()).replaceAll("\\.", "\\,"),
                                (TOTAL_ROW_LENGTH-cbll.getTitle().length()-df1length)) + "\" font=\"1\" />";
                    sale_number++;
                }
            }
        }
        sale_number = sale_number + 1;
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", new Object[]{s});
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", new Object[]{s});
    }

    private void addCustomerItem(Customer c){
        if (c.getDescription().length() < max_item_desc_len) {
            //item.padRight(max_item_desc_len)
        }
        list = list + "<printNormal operator=\"\" data=\"" + c.getDescription().toUpperCase() + "\" font=\"1\" />";

        sale_number = sale_number + 1;

        customersAdded.add(c.getCustomerId());
    }


    public void openCashDrawer(){
        req = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<s:Body>"
                + "<printerCommand>"
                + "<displayText operator=\"\" data=\"\" />"
                + "<openDrawer\tOpe=\"1\" />"
                + "</printerCommand>"
                + "</s:Body>"
                + "</s:Envelope>";
    }

    //si può fare solo se la stampante è fiscalizzata
    public void printZReport(){
        req = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<s:Body>"
                + "<printerFiscalReport>"
                + "<printZReport operator=\"1\" timeout=\"\"/>"
                + "</printerFiscalReport>"
                + "</s:Body>"
                + "</s:Envelope>";
    }

    public void printXReport(){
        req = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<s:Body>"
                + "<printerFiscalReport>"
                + "<printXReport operator=\"1\"/>"
                + "</printerFiscalReport>"
                + "</s:Body>"
                + "</s:Envelope>";
    }

    public String switchPayment(int paymentType){
        switch(paymentType){
            case 0:
                return "CONTANTE";
            case 1:
                return "CARTA";
            case 2:
                return "CARTA";
            case 4:
                return "TICKET";
            case 5:
                return "CREDITO";
            default:
                return "";
        }
    }

    private static final PrinterEpson instance = new PrinterEpson();

    public PrinterEpson(){
    }

    public static PrinterEpson getInstance() {
        return instance;
    }
}
