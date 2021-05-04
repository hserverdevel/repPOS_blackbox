package com.example.blackbox.printer;

import android.util.Log;

import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by Fabrizio on 26/03/2019.
 */

public class PrinterRCH {
    private String deviceName;
    private String billId;
    private String orderNumber;
    private String IP;
    private float paid;
    private float cost;
    private float credit;
    private float creditL;
    private int paymentType;
    private ArrayList<CashButtonLayout> products;
    private Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers;
    private ArrayList<Customer> customers;
    private ArrayList<Integer> customersAdded;
    private String list = "";
    private int sale_number = 0;
    private int tableNumber;
    private String roomName;
    private static int max_item_desc_len = 32;
    private static int TOTAL_ROW_LENGTH = 52;
    private String req = "";
    private float totalDiscount;
    private int quantity;
    private String numberOrderSplit;
    private String description;
    public ClientInfo clientInfo;
    public void setClientInfo(ClientInfo c) {clientInfo =c;}
    public int numeroFattura;
    public void setNumeroFattura(int i){numeroFattura =i;}

    //RCH PARAMETERS
    private static final String TAG = "PRINTER_RCH";
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

    public void setQuantity(int q){this.quantity = q;}

    public int getQuantity(){return quantity;}

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
        this.url = "http://" + url + "/service.cgi";
        Log.i("URL", this.url);
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

        try {
            StringEntity entity = new StringEntity(req, HTTP.UTF_8);
            entity.setContentType("string/xml;UTF-8");
            httppost.setEntity(entity);
            EntityUtils.consumeQuietly(entity);

            String response = httpclient.execute(httppost).getEntity().toString();
            XmlRCHResponseParser parser = new XmlRCHResponseParser();
            parser.parseXml(response);
            Log.i("STATO STAMPANTE", parser.toString());
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
        req = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Service>" + "<cmd>=K</cmd>" + "<cmd>=C1</cmd>" + "<cmd>=C86</cmd>" +
                list +
                ((totalDiscount > 0)?"<cmd>=V/$" + String.valueOf(totalDiscount*100.0f) + "</cmd>":"") +
                "<cmd>=" + switchPayment() + "</cmd>" +
                "<cmd>=c</cmd>" +
                "</Service>";
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
            addSellItemNonFiscal(c);
        }

        DecimalFormat df = new DecimalFormat("#0.00");
        // Create print document(String)
        req = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Service>" + "<cmd>=K</cmd>" + "<cmd>=C1</cmd>" + "<cmd>=o</cmd>" +
                ((tableNumber != -11 && tableNumber != -1)?
                        "<cmd>=\"/(------------------------------------------------</cmd>" + "<cmd>=\"/(                    TAVOLO "
                                + String.valueOf(tableNumber) + "           </cmd>"
                                + "<cmd>=\"/(------------------------------------------------</cmd>"
                        : "<cmd>=\"/(------------------------------------------------</cmd>" + "<cmd>=\"/(                 NUMERO ORDINE "
                        + orderNumber + "    </cmd>"
                        + "<cmd>=\"/(------------------------------------------------</cmd>") +
                list +
                "<cmd>=\"/(------------------------------------------------</cmd>" + "<cmd>=\"/(" + padRight("T O T A L E", 32) + "        " +
                padLeft(df.format((cost)).replaceAll("\\.", "\\,"), 8) + "</cmd>"
                + "<cmd>=\"/(------------------------------------------------</cmd>" +
                "<cmd>=o</cmd>" +
                "</Service>";
        Log.d("SCONTRINO", req);
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
            addSellItemNonFiscal(c);
        }
        DecimalFormat df = new DecimalFormat("#0.00");

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        // Create print document(String)
        req = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Service>" + "<cmd>=K</cmd>" + "<cmd>=C1</cmd>" + "<cmd>=o</cmd>" +
                //intestazione fattura
                "<cmd>=\"/(------------------------------------------------</cmd>" + "<cmd>=\"/(Fattura di Cortesia: "
                + String.valueOf(year) + "/" + String.valueOf(numeroFattura) + "/" + StaticValue.shopName + "</cmd>" +
                "<cmd>=\"/(" + String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day) + "</cmd>" +
                //dettagli cliente
                ((clientInfo.getCodice_fiscale().equals(""))?
                        "<cmd>=\"/(" + clientInfo.getCompany_vat_number() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_name() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_address() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_postal_code() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_city() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_country() + "</cmd>"
                        :
                        "<cmd>=\"/(" + clientInfo.getCompany_vat_number() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCodice_fiscale() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getName() + " " + clientInfo.getSurname() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_address() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_postal_code() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_city() + "</cmd>" +
                                "<cmd>=\"/(" + clientInfo.getCompany_country() + "</cmd>") +
                "<cmd>=\"/(------------------------------------------------</cmd>" +
                //dettagli ordine
                ((tableNumber != -11 && tableNumber != -1)?
                        "<cmd>=\"/(------------------------------------------------</cmd>" + "<cmd>=\"/(                    TAVOLO "
                                + String.valueOf(tableNumber) + "           </cmd>"
                                + "<cmd>=\"/(------------------------------------------------</cmd>"
                        : "<cmd>=\"/(------------------------------------------------</cmd>" + "<cmd>=\"/(                 NUMERO ORDINE "
                        + orderNumber + "    </cmd>"
                        + "<cmd>=\"/(------------------------------------------------</cmd>") +
                list +
                // end list
                "<cmd>=\"/(------------------------------------------------</cmd>" + "<cmd>=\"/(" + padRight("T O T A L E", 32) + "        " +
                padLeft(df.format((cost)).replaceAll("\\.", "\\,"), 8) + "</cmd>"
                + "<cmd>=\"/(------------------------------------------------</cmd>" +
                ((i == 1)?
                        "<cmd>=\"/(SCONTRINO DI CORTESIA PER CLIENTE)</cmd>"
                        : "<cmd>=\"/()</cmd>") +
                "<cmd>=o</cmd>" +
                "</Service>";
    }

    public void printFiscalWithNonFiscal(){
        //fiscal part
        for(LeftPayment myLeft : left){

            // Create print document(String)
            req = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Service>" + "<cmd>=K</cmd>" + "<cmd>=C1</cmd>" + "<cmd>=C86</cmd>" +
                    (myLeft.getPaid() < myLeft.getCost()?
                            "<cmd>=R" + 1 + "/$" + String.valueOf((myLeft.getPaid()*100)) + "/*" + 1 + "/(PER AMOUNT)</cmd>"
                    :
                            "<cmd>=R" + 1 + "/$" + String.valueOf((myLeft.getCost()*100)) + "/*" + 1 + "/(PER AMOUNT)</cmd>") +
                    ((totalDiscount > 0)?"<cmd>=V/$" + String.valueOf(totalDiscount*100.0f) + "</cmd>":"") +
                    "<cmd>=" + switchPayment() + "</cmd>" +
                    "<cmd>=c</cmd>" +
                    "</Service>";

            setPrinterThread();
        }
        //non fiscal part
        printNonFiscalBill();
        setPrinterThread();
    }

    private void addSellItem(CashButtonLayout item) {
        if(item.getDiscount() == null || item.getDiscount() == 0.0f)
            list = list + "<cmd>=R" + 1 + "/$" + String.valueOf(item.getPriceFloat()*100.0f)
                    + "/*" + item.getQuantity() + "/(" + item.getTitle() + ")</cmd>";
        else
            list = list + "<cmd>=R" + 1 + "/$" + String.valueOf(item.getPriceFloat()*100.0f) + "/*" + item.getQuantity() + "/(" + item.getTitle() + ")</cmd>";
        if(modifiers != null && modifiers.get(item).size() != 0){
            ArrayList<CashButtonListLayout> mods = modifiers.get(item);
            for(CashButtonListLayout cbll : mods){
                if(Float.parseFloat(cbll.getPrice()) > 0.0f){
                    list = list + "<cmd>=R" + 1 + "/$" + String.valueOf(cbll.getPriceFloat()*100.0f) + "/*" + cbll.getQuantity()
                            + "/(" + cbll.getTitle() + ")</cmd>";
                    sale_number++;
                }
            }
        }
        sale_number = sale_number + 1;
    }

    private void addSellItemNonFiscal(CashButtonLayout item){
        DecimalFormat df = new DecimalFormat("#0.00");
        int dflength = df.format(item.getPriceFloat()*item.getQuantityInt()).replaceAll("\\.", "\\,").length();
        if(item.getPriceFloat()*item.getQuantityInt() >= 10.0f && item.getPriceFloat()*item.getQuantityInt() < 100.0f)
            dflength--;
        else if(item.getPriceFloat()*item.getQuantityInt() >= 100.0f && item.getPriceFloat()*item.getQuantityInt() < 1000.0f)
            dflength = dflength - 2;
        else if(item.getPriceFloat()*item.getQuantityInt() >= 1000.0f)
            dflength = dflength - 3;
        if(item.getQuantityInt() == 1)
            list = list + "<cmd>=\"/(" + item.getTitle() + padLeft(df.format(item.getPriceFloat()).replaceAll("\\.", "\\,"),
                    (TOTAL_ROW_LENGTH-item.getTitle().length()-dflength)) + ")</cmd>";
        else
            list = list + "<cmd>=\"/(              " + item.getQuantity() + " X " + df.format(item.getPriceFloat()).replaceAll("\\.", "\\,") + ")</cmd>"
                    + "<cmd>=\"/(" + item.getTitle() + padLeft(df.format(item.getPriceFloat()*item.getQuantityInt()).replaceAll("\\.", "\\,"),
                    (TOTAL_ROW_LENGTH-item.getTitle().length()-dflength)) + ")</cmd>";
        if(modifiers.get(item).size() != 0){
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
                    if(cbll.getQuantityInt() == 1){
                        list = list + "<cmd>=\"/(" + cbll.getTitle() + padLeft(df.format(cbll.getPriceFloat()).replaceAll("\\.", "\\,"),
                                TOTAL_ROW_LENGTH-cbll.getTitle().length()-df1length) + ")</cmd>";
                    }
                    else{
                        list = list + "<cmd>=\"/(             " + cbll.getQuantity() + " X " +
                                df.format(cbll.getPriceFloat()).replaceAll("\\.", "\\,") + ")</cmd>"
                                + "<cmd>=\"/(" + cbll.getTitle() + padLeft(
                                        df.format(cbll.getPriceFloat()*cbll.getQuantityInt()).replaceAll("\\.", "\\,"),
                                TOTAL_ROW_LENGTH-cbll.getTitle().length()-df1length) + ")</cmd>";
                    }
                    sale_number++;
                }
            }
        }
        sale_number = sale_number + 1;
    }

    private void addCustomerItem(Customer c){
        if (c.getDescription().length() < max_item_desc_len) {
            //item.padRight(max_item_desc_len)
        }
        list = list + "<cmd>=\"/(" + c.getDescription().toUpperCase() + ")</cmd>";

        sale_number = sale_number + 1;

        customersAdded.add(c.getCustomerId());
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", new Object[]{s});
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", new Object[]{s});
    }

    public void openCashDrawer(){
        req = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Service>"
        + "<cmd>=C86</cmd>"
        +"</Service>";
    }

    public void printZReport(){
        req = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Service>"
                + "<cmd>=K</cmd>" + "<cmd>=C3/$1</cmd>" + "<cmd>=C10</cmd>" + "<cmd>=C86</cmd>"
                + "</Service>";
    }

    public void printXReport(){
        req = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Service>"
                + "<cmd>=K</cmd>" + "<cmd>=C2/$1</cmd>" + "<cmd>=C10</cmd>" + "<cmd>=C86</cmd>"
                + "</Service>";
    }

    public String switchPayment(){
        switch(paymentType){
            case 1:
                return "T1";
            case 2:
                return "T4";
            case 3:
                return "T4";
            case 4:
                return "T1";
            case 5:
                return "T1";
            case 6:
                return "T1";
            default:
                return "T1";
        }
    }

    public class XmlRCHResponseParser {
        ArrayList<XmlRCHResponse> parsedData = new ArrayList();

        void vDebug(String debugString) {
            Log.v("DomParsing", new StringBuilder(String.valueOf(debugString)).append("\n").toString());
        }

        void eDebug(String debugString) {
            Log.e("DomParsing", new StringBuilder(String.valueOf(debugString)).append("\n").toString());
        }

        public ArrayList<XmlRCHResponse> getParsedData() {
            return this.parsedData;
        }

        public void parseXml(String xml) {
            Log.i("SIAMO SOLO QUI", "Livello -1");
            try {
                Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
                NodeList notes = root.getChildNodes();
                Log.i("QUI CI SIAMO", "Livello 0");
                for (int i = 0; i < notes.getLength(); i++) {
                    Log.i("QUI CI SIAMO", "Livello 1");
                    XmlRCHResponse newResponse;
                    NodeList noteDetails;
                    int j;
                    Node c1;
                    Element detail;
                    String nodeName;
                    String nodeValue;
                    Node c = notes.item(i);
                    if (c.getNodeName().equals("Enq")) {
                        Log.i("QUI CI SIAMO", "Livello 2");
                        newResponse = new XmlRCHResponse();
                        noteDetails = c.getChildNodes();
                        for (j = 0; j < noteDetails.getLength(); j++) {
                            Log.i("QUI CI SIAMO", "Livello 3");
                            c1 = noteDetails.item(j);
                            if (c1.getNodeType() == (short) 1) {
                                detail = (Element) c1;
                                nodeName = detail.getNodeName();
                                nodeValue = detail.getFirstChild().getNodeValue();
                                vDebug("______Dettaglio:" + nodeName);
                                vDebug("______Contenuto Dettaglio:" + nodeValue);

                                if (nodeName.equals("value")) {
                                    newResponse.setPrinterError(nodeValue);
                                }
                                if (nodeName.equals("paperEnd")) {
                                    newResponse.setPaperEnd(nodeValue);
                                }
                                if (nodeName.equals("coverOpen")) {
                                    newResponse.setCoverOpen(nodeValue);
                                }
                                if (nodeName.equals("lastCmd")) {
                                    newResponse.setLastCmd(nodeValue);
                                }
                                if (nodeName.equals("busy")) {
                                    newResponse.setBusy(nodeValue);
                                }
                                if (nodeName.equals("busy")) {
                                    newResponse.setBusy(nodeValue);
                                }
                                if(nodeName.equals("TEXT HTTP/1.1 200 OK")){
                                    newResponse.setBusy(nodeValue);
                                }
                            }
                        }
                        this.parsedData.add(newResponse);
                    }
                    else{
                        Log.i("SIAMO QUI", "cazzo");
                    }
                    if (c.getNodeType() == (short) 1) {
                        newResponse = new XmlRCHResponse();
                        noteDetails = c.getChildNodes();
                        for (j = 0; j < noteDetails.getLength(); j++) {
                            c1 = noteDetails.item(j);
                            if (c1.getNodeType() == (short) 1) {
                                detail = (Element) c1;
                                nodeName = detail.getNodeName();
                                nodeValue = detail.getFirstChild().getNodeValue();
                                vDebug("______Dettaglio:" + nodeName);
                                vDebug("______Contenuto Dettaglio:" + nodeValue);
                                if (nodeName.equals("errorCode")) {
                                    newResponse.setErrorCode(nodeValue);
                                }
                                if (nodeName.equals("printerError")) {
                                    newResponse.setPrinterError(nodeValue);
                                }
                                if (nodeName.equals("paperEnd")) {
                                    newResponse.setPaperEnd(nodeValue);
                                }
                                if (nodeName.equals("coverOpen")) {
                                    newResponse.setCoverOpen(nodeValue);
                                }
                                if (nodeName.equals("lastCmd")) {
                                    newResponse.setLastCmd(nodeValue);
                                }
                                if (nodeName.equals("busy")) {
                                    newResponse.setBusy(nodeValue);
                                }
                            }
                        }
                        this.parsedData.add(newResponse);
                    }
                }
            } catch (SAXException e) {
                eDebug(e.toString());
            } catch (IOException e2) {
                eDebug(e2.toString());
            } catch (ParserConfigurationException e3) {
                eDebug(e3.toString());
            } catch (FactoryConfigurationError e4) {
                eDebug(e4.toString());
            }
        }
    }

    public class XmlRCHResponse {
        private String busy;
        private String coverOpen;
        private String errorCode;
        private String lastCmd;
        private String paperEnd;
        private String printerError;

        public String getErrorCode() {
            return this.errorCode;
        }

        public String getPrinterError() {
            return this.printerError;
        }

        public String getPaperEnd() {
            return this.paperEnd;
        }

        public String getCoverOpen() {
            return this.coverOpen;
        }

        public String getLastCmd() {
            return this.lastCmd;
        }

        public String getBusy() {
            return this.busy;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public void setPrinterError(String printerError) {
            this.printerError = printerError;
        }

        public void setPaperEnd(String paperEnd) {
            this.paperEnd = paperEnd;
        }

        public void setCoverOpen(String coverOpen) {
            this.coverOpen = coverOpen;
        }

        public void setLastCmd(String lastCmd) {
            this.lastCmd = lastCmd;
        }

        public void setBusy(String busy) {
            this.busy = busy;
        }

        public String toString() {
            return "Response [errorCode=" + this.errorCode + ", printerError=" + this.printerError + ", paperEnd=" + this.paperEnd + ", coverOpen=" + this.coverOpen + ",lastCmd=" + this.lastCmd + ",busy=" + this.busy + "]";
        }
    }

}
