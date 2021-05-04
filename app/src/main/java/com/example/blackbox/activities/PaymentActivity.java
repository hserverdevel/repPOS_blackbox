package com.example.blackbox.activities;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.adapter.ClientsPopupAdapter;
import com.example.blackbox.adapter.OrderListAdapter;
import com.example.blackbox.adapter.SubdivisionAdapter;
import com.example.blackbox.client.ClientThread;
import com.example.blackbox.fragments.CalculatorFragment;
import com.example.blackbox.fragments.OptionsFragment;
import com.example.blackbox.fragments.OrderFragment;
import com.example.blackbox.fragments.OrderSubdivisionFragment;
import com.example.blackbox.fragments.PaymentActivityCommunicator;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.ButtonPermission;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Client;
import com.example.blackbox.model.ClientInCompany;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Company;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.Fidelity;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.example.blackbox.model.SubdivisionItemJson;
import com.example.blackbox.model.TemporaryOrder;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.model.TotalBill;
import com.example.blackbox.model.TwoString;
import com.example.blackbox.printer.PrinterDitronThread;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.example.blackbox.xml.XmlParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.example.blackbox.adapter.OrderListAdapter.ADD_DISCOUNT;
import static com.example.blackbox.adapter.OrderListAdapter.DEFAULT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.ELEMENT_ITEM_SPLIT;
import static com.example.blackbox.adapter.OrderListAdapter.HOMAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.INSERT_CREDIT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.INSERT_FIDELITY_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.ITEM_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.MODIFY_DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.NUMBER_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PARTIAL_TOTAL_DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_PARTIAL_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_TICKET_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_TOTAL_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERCENTAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERSON_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.TOTAL_DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.TOTAL_MODIFY_DISCOUNT_MODE;
import static com.example.blackbox.fragments.CalculatorFragment.roundDecimal;


public class PaymentActivity extends FragmentActivity
        implements PaymentActivityCommunicator , ClientThread.TaskDelegate, HttpHandler.AsyncResponse  {

    public static final String TAG = "<PaymentActivity>";

    public static String deviceName = "A";

    private DatabaseAdapter dbA;

    private PaymentActivity me;

    public static final int CALCULATOR_ACTIVATION = 1;
    public static final int CALCULATOR_NOTIFY_COST = 2;
    public static final int CALCULATOR_OFF = 3;
    public static final int CALCULATOR_INSERT_NUMBER = 4;
    public static final int CALCULATOR_INSERT_PERCENTAGE = 5;
    public static final int ADD_SUBDIVISION_ELEMENT = 6;
    public static final int DISCOUNT_MODE_OFF = 7;
    public static final int CALCULATOR_INSERT_PARTIAL= 103;
    public static final int CALCULATOR_ACTIVATION_TICKET = 104;
    public static final int CALCULATOR_ACTIVATION_FOR_CREDIT = 105;
    public static final int CALCULATOR_ACTIVATION_FIDELITY = 106;


    private CalculatorFragment calculatorFragment;
    private OptionsFragment optionsFragment;
    private OrderFragment orderFragment;
    private OrderSubdivisionFragment orderSubdivisionFragment;

    PaymentActivity forClient = null;

    private ClientsPopupAdapter clientsAdapter;

    private Intent intentPay;
    private Intent intentPasscode;

    private ButtonPermission buttonPer;

    private CustomButton perc_button;
    private CustomButton perperson_button;
    private CustomButton peritem_button;
    private CustomButton pernumber_button;
    public boolean greenButton;
    public boolean optionbutton = true;


    public boolean anyButtonPressed = false;
    private boolean percentageSplit = false;
    private boolean numberSplit = false;
    private boolean billSplitToZero = false;
    public Boolean splitItemSet = false;
    public Boolean isHomage = false;
    private boolean discountSet = false;
    public boolean isCalculatorOn = false;
    public boolean invoiceBill = false;

    // this is used only for the buy credit specific mode
    private int clientForCredit;


    private int selectedClient;
    private int tableNumber = -1;
    private int orderNumber = -1;

    private int fidelityClientId;

    public int mode;

    public static final int PAY_TOTAL_BILL = 101;
    public static final int PAY_PARTIAL_BILL = 102;
    private int pay_mode = PAY_TOTAL_BILL;

    public int paymentType = -1;

    private String passcode;
    private String email = "";
    private String IP = "";

    protected ArrayList<CashButtonLayout> products = new ArrayList<CashButtonLayout>();
    protected Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers;
    public ArrayList<Integer> creditId = new ArrayList<Integer>();


    private final int red = Color.parseColor("#cd0046");
    private final int black = Color.parseColor("#DD000000");
    private final int lightBlue = Color.parseColor("#05a8c0");

    public float density;
    public float dpHeight;
    public float dpWidth;


    // ----- Class GET/SET ------

    public void setOptionButton(Boolean b) {
        optionbutton = b;
    }

    public void setNumberSplit()
    {
        numberSplit = false;
    }

    public boolean getDiscountSet() { return discountSet; }
    public void setDiscountSet(boolean b) { discountSet = b; }

    public void setProductsList(ArrayList<CashButtonLayout> p ) { products = p; }
    public void setModifiersList(Map<CashButtonLayout,ArrayList<CashButtonListLayout>> m) { modifiers = m; }

    public int getOrderNumber() { return orderNumber; }
    public int getTableNumber() { return tableNumber; }

    public int getPaymentType() { return paymentType; }
    public void setPaymentType(int paymentType) { this.paymentType = paymentType; }

    public float getCredit()
    {
        float c = 0.0f;
        if (creditId != null)
            { for (int id : creditId) { c += dbA.getBillCreditPrice(id); } }
        return c;

    }
    public boolean setCreditId(Integer i){
        if (creditId.contains(i))
            { return false; }

        creditId.add(i);
        return true;
    }


    public int getMode(){
        return mode;
    }

    public void setMode(int i){
        mode = i;
    }

    public void setPay_mode(int pay_mode) {
        this.pay_mode = pay_mode;
    }

    public void setPercentageSplit(boolean percentageSplit) {
        this.percentageSplit = percentageSplit;
    }

    public void setNumberSplit(boolean numberSplit) {
        this.numberSplit = numberSplit;
    }

    @Override
    public void setProducts(ArrayList<CashButtonLayout> prod)
    {
        // create a shallow copy of the products
        // to avoid a ConcurrentModificationException,
        // when iterating and modifying the products object
        ArrayList<CashButtonLayout> productsCopy = (ArrayList<CashButtonLayout>) products.clone();

        // add the products present in the input param `prod` to the `products` global var
        // if a product is already present, increase it's quantity by 1
        for (CashButtonLayout p : prod)
        {
            if (products.size() == 0)
                { products.add(p); }

            else
            {
                for (CashButtonLayout product : productsCopy)
                {
                    // check if the current product is the same as p
                    // if so, increase the product quantity by 1
                    if (p.getTitle().equals(product.getTitle()) && p.getProductId() == product.getProductId())
                    {
                        int prodIndex = productsCopy.indexOf(product);
                        products.get(prodIndex).setQuantity(products.get(prodIndex).getQuantityInt() + 1);
                    }

                    else
                        { products.add(p); }
                }
            }
        }
    }


    @Override
    public void setModifiers(Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers)
    {
        this.modifiers = modifiers;
    }




    // ------ MAIN --------


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        me = (PaymentActivity)this;
        dbA = new DatabaseAdapter(this);

        // this fragment handle the calculator,
        // used as a input for various elements
        // (amount of cash, fidelity id, discount ... )
        calculatorFragment = new CalculatorFragment();

        // this fragment hosts the option of the payment,
        // thus how the bill will be paid (cash, card, credits ...)
        // and other options (discount, email, print bill ...)
        optionsFragment = new OptionsFragment();

        // this fragment hosts the item present in the current bill
        orderFragment = new OrderFragment();

        // this fragment handle the way to split the payment
        // (by item, by client ...) and represent the sub-bills
        orderSubdivisionFragment = new OrderSubdivisionFragment();

        perc_button      = (CustomButton)findViewById(R.id.percentage_button);
        perperson_button = (CustomButton)findViewById(R.id.perperson_button);
        peritem_button   = (CustomButton)findViewById(R.id.peritem_button);
        pernumber_button = (CustomButton)findViewById(R.id.pernumber_button);
        buttonPer = new ButtonPermission();
        greenButton = false;
        setupButtons();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.calc, calculatorFragment, "calc");
        transaction.add(R.id.payment_options, optionsFragment, "options");
        transaction.add(R.id.order, orderFragment, "order");
        transaction.add(R.id.order_subdivision, orderSubdivisionFragment, "orderSubdivision");
        transaction.commit();

        IP = StaticValue.IP;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;

        intentPay = this.getIntent();
        Log.i(TAG, "[onCreate] Processing billId: " + intentPay.getIntExtra("billId",-1));

        orderNumber = intentPay.getIntExtra("orderNumber", -1);
        tableNumber = intentPay.getIntExtra("tableNumber", -1);
        selectedClient = intentPay.getIntExtra("customer", -1);

        forClient = this;

        email = intentPay.getStringExtra("email");
        boolean moreThanOneMail = intentPay.getBooleanExtra("more", false);
        if (email != null)
            optionsFragment.setEmail(email);
        if (moreThanOneMail)
            optionsFragment.setMoreThanOneMail(true);

        TimerManager.setContext(getApplicationContext());
        intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);
        intentPasscode.putExtra("isAdmin", intentPay.getIntExtra("isAdmin", -1));
        intentPasscode.putExtra("username", intentPay.getStringExtra("username"));

        TimerManager.setIntentPinpad(intentPasscode);
        TimerManager.startPinpadAlert(2);
    }


    /**
     * Handle the output of the HttpHandler class, called from `callHttpHandler`
     * @param output: the output of the http response to the blackbox
     **/
    @Override
    public void processFinish(String output)
    {
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        // Each possible response is handled with a swtich case
        JSONObject jsonObject = new JSONObject();
        // the response route
        String route = "";
        // a bool indicating if the connection was succesful
        boolean success = false;

        try {
            jsonObject = new JSONObject(output);
            route = jsonObject.getString("route");
            success = jsonObject.getBoolean("success");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success)
        {
            boolean check = false;

            try
            {
                jsonObject = new JSONObject(output);

                switch (route)
                {
                    case "getBillDataPayment":
                        check = jsonObject.getBoolean("check");

                        if (jsonObject.getBoolean("check"))
                        {
                            JSONObject tbArray = new JSONObject(output).getJSONObject("totalBill");

                            JSONArray productArray = new JSONObject(output).getJSONArray("products");
                            JSONArray modifierArray = new JSONObject(output).getJSONArray("modifiers");
                            JSONArray customerArray = new JSONObject(output).getJSONArray("customers");

                            int homage = jsonObject.getInt("homage");
                            double discount = jsonObject.getDouble("discount");
                            TotalBill totals = TotalBill.fromJson(tbArray);
                            ArrayList<CashButtonLayout> products = CashButtonLayout.fromJsonArray(productArray);
                            ArrayList<CashButtonListLayout> modifiers = CashButtonListLayout.fromJsonArray(modifierArray);
                            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                            for (CashButtonLayout product : products) {
                                map.put(product, product.getCashList());
                            }

                            ArrayList<Customer> customers = Customer.fromJsonArray(customerArray);

                            orderFragment.setBillDataFromServer(totals, customers, products, map);
                            orderSubdivisionFragment.getSubdivisionAdapter().setSubdivisionItemFromServer(totals, discount);
                            orderSubdivisionFragment.getSubdivisionAdapter().notifyDataSetChanged();
                            orderFragment.setPostRunnable(homage, (float) discount, true);

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "getBillSplits":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            int billId = jsonObject.getInt("billId");
                            JSONArray splits = new JSONObject(output).getJSONArray("billSplits");
                            ArrayList<SubdivisionItemJson> bill_s = SubdivisionItemJson.fromJsonArray(splits);
                            ArrayList<SubdivisionItem> bill_splits = new ArrayList<>();
                            for (SubdivisionItemJson b : bill_s) {
                                HashMap<CashButtonLayout, Integer> map = new HashMap<CashButtonLayout, Integer>();
                                ArrayList<CashButtonLayout> cbls = b.getItems();
                                ArrayList<TwoString> tss = b.getItems_map();
                                for (TwoString ts : tss) {
                                    for (CashButtonLayout cbl : cbls) {
                                        if (Integer.valueOf(ts.getFirstString()) == cbl.getID()) {
                                            map.put(cbl, Integer.valueOf(ts.getSecondString()));
                                            //break;
                                        }
                                    }
                                }
                                SubdivisionItem subItem = new SubdivisionItem();
                                subItem.setDiscount(b.getDiscount());
                                subItem.setHomage(b.isHomage());
                                subItem.setIsShow(b.getIsShow());
                                subItem.setItems(b.getItems());
                                subItem.setItems_map(map);
                                subItem.setMode(b.getMode());
                                subItem.setNumber_subdivision(b.getNumber_subdivision());
                                subItem.setOwed_money(b.getOwed_money());
                                subItem.setPaid(b.isPaid());
                                subItem.setPaymentType(b.getPaymentType());
                                subItem.setPercentage(b.getPercentage());
                                subItem.setFidelity(b.getFidelity());

                                bill_splits.add(subItem);

                            }


                            if (bill_splits.size() > 0) {
                                orderSubdivisionFragment.getSubdivisionAdapter().loadPaidBillSplits(bill_splits);
                                orderFragment.getOrderListAdapter().setSplit(true);
                                orderFragment.getOrderListAdapter().loadBillSplits(bill_splits);

                                if (!checkIfOtherSplitBillArePaid())
                                    orderFragment.showRemaingTotal();
                            }
                            orderSubdivisionFragment.getSubdivisionAdapter().showFirstPaid();
                            orderFragment.getOrderListAdapter().notifyDataSetChanged();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "savePaidBill":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            // close payment activity
                            Intent intent = new Intent(PaymentActivity.this, Operative.class);
                            int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                            String username = intentPay.getStringExtra("username");
                            int isAdmin = intentPay.getIntExtra("isAdmin", -1);

                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.setAction("billPaid");
                            intent.putExtra("billId", -1);
                            intent.putExtra("orderNumber", -1);
                            startActivity(intent);
                            finish();
                            try {
                                PaymentActivity.this.finish();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "savePaidBillItem":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            // close payment activity

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "checkIfBillIsPaid":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            // close payment activity
                            Boolean paid = jsonObject.getBoolean("paid");
                            if (paid) {
                                int payment = jsonObject.getInt("payment");
                                orderSubdivisionFragment.getSubdivisionAdapter().setTotalItemPayment(payment);
                                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                if (item.isPaid()) {

                                    if (item.getOwed_money() > 0.0f) {
                                        orderSubdivisionFragment.getSubdivisionAdapter().showItem(1);

                                    } else {
                                        orderSubdivisionFragment.getSubdivisionAdapter().showItem(2);
                                    }
                                    orderSubdivisionFragment.getSubdivisionAdapter().notifyDataSetChanged();
                                    orderFragment.paidOrder();

                                }
                            }
                            if (orderFragment.getOrderListAdapter().getCustomers().size() > 0)
                                perc_button.performClick();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "checkIfBillIsPaidOnly":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            // close payment activity
                            Boolean paid = jsonObject.getBoolean("paid");
                            if (paid) {
                                hideButtonPerNumber();
                                optionsFragment.deactivatePayments();
                                buttonOpacitySetting1();
                                greenButton = true;
                                orderFragment.paidOrder();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "saveDiscountTotal":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                            if (item == null)
                                item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                            float other = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCostForTotal();
                            float remain = item.getOwed_money() - item.getDiscount() - getDiscountForItemSelected(item)/*getDiscountForItem()*/ - getHomageForItem()/*-other*/;
                            remain = remain - calculatorFragment.getActualCredit();
                            float cost1 = remain;
                            if (item.getMode() == -1 && !orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherPersonOrItem()) {
                                String txt1 = String.format("%.2f", roundDecimal(cost1 - other, 2));
                                calculatorFragment.setCost(txt1);
                            } else {
                                String txt1 = String.format("%.2f", roundDecimal(cost1, 2));
                                calculatorFragment.setCost(txt1);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "saveDiscountForElement":
                        check = jsonObject.getBoolean("check");
                        if (check) {

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "resetDiscountForElement":
                        check = jsonObject.getBoolean("check");
                        if (check) {

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "insertClient":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONObject jObject = new JSONObject(output).getJSONObject("client");
                            JSONObject jObject1 = new JSONObject(output).getJSONObject("cic");
                            Client client = Client.fromJson(jObject);
                            ClientInCompany cic = ClientInCompany.fromJson(jObject1);
                            dbA.insertClientFromServer(client);

                            dbA.insertCiCFromServer(cic);
                            if (!clientsAdapter.searchMode)
                                //clientsAdapter.updateDataSet();
                                name_et.setText("");
                            surname_et.setText("");
                            email_et.setText("");
                            clientsAdapter.reloadCLients();
                        } else
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        break;

                    case "insertClientWithCompanyPa":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONObject jObject = new JSONObject(output).getJSONObject("client");
                            JSONObject jObject1 = new JSONObject(output).getJSONObject("cic");
                            JSONObject jObject2 = new JSONObject(output).getJSONObject("company");
                            Client client = Client.fromJson(jObject);
                            ClientInCompany cic = ClientInCompany.fromJson(jObject1);
                            Company company = Company.fromJson(jObject2);
                            dbA.insertClientFromServer(client);
                            dbA.insertCiCFromServer(cic);
                            dbA.insertCompanyFromServer(company);


                            if (!clientsAdapter.searchMode)
                                //clientsAdapter.updateDataSet();
                                //reset dataFields
                                name_et.setText("");
                            surname_et.setText("");
                            email_et.setText("");
                            company_name_et.setText("");
                            address_et.setText("");
                            vat_number_et.setText("");
                            postal_code_et.setText("");
                            city_et.setText("");
                            country_et.setText("");
                            //codice_fiscale_et.setText("");
                            provincia_et.setText("");
                            codice_destinatario_et.setText("");
                            pec_et.setText("");
                            addCompanyInfo = (CustomButton) findViewById(R.id.add_company_info_button);
                            if (addCompanyInfo.isActivated())
                                addCompanyInfo.performClick();

                            address_et_p.setText("");
                            vat_number_et_p.setText("");
                            postal_code_et_p.setText("");
                            city_et_p.setText("");
                            country_et_p.setText("");
                            codice_fiscale_et_p.setText("");
                            provincia_et_p.setText("");
                            codice_destinatario_et_p.setText("");
                            pec_et_p.setText("");
                            addPersonalInfo = (CustomButton) findViewById(R.id.add_personal_info_button);
                            if (addPersonalInfo.isActivated())
                                addPersonalInfo.performClick();
                            clientsAdapter.reloadCLients();

                        }
                        break;

                    case "getCredit":
                        // TODO
                        // at this point the blackbox should be called, to get the credit of the current bill
                        // but, since this feature is still not present, there is just a placeholder now
                        ((CustomTextView) findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.credit_amount);
                        ((CustomTextView) findViewById(R.id.numberInsertion_tv)).setText(String.valueOf(9999));
                        // avaibleCredits = 9999;
                        /* --- the real code, to be implemented ---
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            double value = jsonObject.getDouble("credit");
                            int billId = jsonObject.getInt("billId");

                            if (value > 0)
                            {
                                if (setCreditId(billId))
                                {
                                    ((CustomTextView) findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.credit_amount);
                                    ((CustomTextView) findViewById(R.id.numberInsertion_tv)).setText(String.valueOf(value));
                                } else {
                                    calculatorFragment.okPressedNTimes = 0;
                                }
                            }

                            else
                            {
                                calculatorFragment.okPressedNTimes = 0;
                                calculatorFragment.amount = new StringBuilder();
                                ((CustomTextView) findViewById(R.id.numberInsertion_tv)).setText(R.string.no_credit);
                            }
                        }

                        else
                            { Toast.makeText(this.getApplicationContext(), getString(R.string.route_check_false, route, jsonObject.getString("reason")), Toast.LENGTH_LONG).show(); }
                         */
                        break;

                    case "printPaidBillInvoice":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            int billId = jsonObject.getInt("billId");
                            int paymentType = jsonObject.getInt("paymentType");
                            int clientId = jsonObject.getInt("clientId");
                            savePaidBillInvoice(paymentType, clientId);

                        }
                        break;

                    case "savePaidBillInvoice":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            // close payment activity
                            Intent intent = new Intent(PaymentActivity.this, Operative.class);
                            int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                            String username = intentPay.getStringExtra("username");
                            int isAdmin = intentPay.getIntExtra("isAdmin", -1);

                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.setAction("billPaid");
                            intent.putExtra("billId", -1);
                            intent.putExtra("orderNumber", -1);
                            startActivity(intent);
                            finish();
                            try {
                                PaymentActivity.this.finish();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "printItemBill":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            // close payment activity
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "printBill":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            // close payment activity
                            int customerId = jsonObject.getInt("customerId");
                            if (customerId != -1) {
                                double saldoPunti = jsonObject.getDouble("saldoPunti");
                                double earned = jsonObject.getDouble("earned");
                                double used = jsonObject.getDouble("used");
                                ClientInfo c = dbA.fetchSingleClient(customerId);
                                Fidelity f = dbA.fetchFidelityById(c.getFidelity_id());
                                dbA.updateFidelityPoint(saldoPunti, f.getEarned() + earned, f.getUsed() + used, f.getId());

                            }

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "addFidelityCredit":
                    case "subtractFidelityCredit":
                        if (jsonObject.getBoolean("check"))
                        {
                                dbA.updateFidelityPoint(
                                        jsonObject.getDouble("value"),
                                        jsonObject.getDouble("earned"),
                                        jsonObject.getDouble("used"),
                                        jsonObject.getInt("id")
                                );
                        }
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), "no route", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else
        {
            Toast.makeText(this,
                    getString(R.string.error_blackbox_comm, StaticValue.blackboxInfo.getName(), StaticValue.blackboxInfo.getAddress()),
                    Toast.LENGTH_LONG).show();
        }
    }


    /** Call the httpHandler at the specific route, with selected parameters */
    public void callHttpHandler(String route,  List<NameValuePair> params ){
        HttpHandler httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    /**
     * Set the onClick functions for the
     * percentage button
     * per person button
     * per item button
     * per number button
     * That are used to split the bill in different ways.
     * Each of the onClick will result in a shift in the `mode` of the payment
     * */
    public void setupButtons()
    {
        perc_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ((!greenButton && optionbutton) &&
                    (!(peritem_button.isActivated() || perperson_button.isActivated() || pernumber_button.isActivated())) &&
                    (!numberSplit && !billSplitToZero))
                {
                    if (orderFragment.getOrderListAdapter().getCustomerSize() > 0)
                    {
                        mode = PERSON_MODE;
                        orderFragment.activateSelectionMode(OrderListAdapter.PERSON_MODE);
                        orderSubdivisionFragment.setMode(OrderListAdapter.PERSON_MODE);

                        for (Customer c : orderFragment.getOrderListAdapter().getCustomers())
                        {
                            orderFragment.splitClientsBill();
                            orderSubdivisionFragment.showSubdivisionItem(1);
                        }

                        orderSubdivisionFragment.getSubdivisionAdapter().resetIsShow();
                        orderSubdivisionFragment.getSubdivisionAdapter().showFirstItemAvaiable()/*.setItemShow(1)*/;

                        hideSplitButton();
                        showAllBlueButtonExSplit();
                    }

                    else
                        { Toast.makeText(me, R.string.no_customer_on_this_bill, Toast.LENGTH_SHORT).show(); }
                }
            }
        });

        // NOTE
        // on 12/04/2020 it was noted that the perperson mode and the peritem mode,
        // have basically the same function, thus (since the perperson has some bug),
        // the two will be fused in one, but the two buttons will remain the same.
        perperson_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!greenButton && optionbutton)
                {
                    if (!isHomage)
                    {
                        if (!(perc_button.isActivated() || peritem_button.isActivated() || pernumber_button.isActivated()) && !percentageSplit && !numberSplit && !billSplitToZero)
                        {
                            if (!v.isActivated())
                            {
                                mode = ITEM_MODE;
                                orderFragment.activateSelectionMode(OrderListAdapter.ITEM_MODE);
                                orderSubdivisionFragment.setMode(OrderListAdapter.ITEM_MODE);
                                hideOtherButtons(v);
                                v.setActivated(!v.isActivated());
                                anyButtonPressed = v.isActivated();
                                setKillOkButtonForSubdivision();
                                optionsFragment.deactivatePayments();
                                setOpacityForSplitButton();
                            }

                            else
                            {
                                orderFragment.removeAllFromItem();
                                mode = DEFAULT_MODE;
                                orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                                buttonOpacitySetting();
                                setNormalKillOkButton();
                                optionsFragment.activatePayments();
                                resetOpacityForSlplittButton();
                            }
                        }
                    }

                    else
                    { Toast.makeText(me, R.string.sorry_you_already_set_an_homage, Toast.LENGTH_SHORT).show(); }
                }
            }

            /* OLD Per person mode
            @Override
            public void onClick(View v)
            {
                if(!greenButton && optionbutton && !isHomage)
                {
                    if (!isHomage)
                    {
                        if (!(perc_button.isActivated() || peritem_button.isActivated() || pernumber_button.isActivated()) && !percentageSplit && !numberSplit && !billSplitToZero)
                        {
                            if (!v.isActivated())
                            {
                                mode = PERSON_MODE;
                                orderFragment.activateSelectionMode(OrderListAdapter.PERSON_MODE);
                                orderSubdivisionFragment.setMode(OrderListAdapter.PERSON_MODE);
                                hideOtherButtons(v);
                                v.setActivated(!v.isActivated());
                                anyButtonPressed = v.isActivated();
                                setKillOkButtonForSubdivision();
                                optionsFragment.deactivatePayments();
                                setOpacityForSplitButton();
                            }

                            else
                            {
                                orderFragment.removeAllFromItemCustomer();
                                mode = DEFAULT_MODE;
                                orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                                buttonOpacitySetting();
                                setNormalKillOkButton();
                                optionsFragment.activatePayments();
                                resetOpacityForSlplittButton();
                            }
                        }
                    }

                    else
                        { Toast.makeText(me, R.string.sorry_you_already_set_an_homage, Toast.LENGTH_SHORT).show(); }

                }
            }
             */
        });

        peritem_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!greenButton && optionbutton)
                {
                    if (!isHomage)
                    {
                        if (!(perc_button.isActivated() || perperson_button.isActivated() || pernumber_button.isActivated()) && !percentageSplit && !numberSplit && !billSplitToZero)
                        {
                            if (!v.isActivated())
                            {
                                mode = ITEM_MODE;
                                orderFragment.activateSelectionMode(OrderListAdapter.ITEM_MODE);
                                orderSubdivisionFragment.setMode(OrderListAdapter.ITEM_MODE);
                                hideOtherButtons(v);
                                v.setActivated(!v.isActivated());
                                anyButtonPressed = v.isActivated();
                                setKillOkButtonForSubdivision();
                                optionsFragment.deactivatePayments();
                                setOpacityForSplitButton();
                            }

                            else
                            {
                                orderFragment.removeAllFromItem();
                                mode = DEFAULT_MODE;
                                orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                                buttonOpacitySetting();
                                setNormalKillOkButton();
                                optionsFragment.activatePayments();
                                resetOpacityForSlplittButton();
                            }
                        }
                    }

                    else
                        { Toast.makeText(me, R.string.sorry_you_already_set_an_homage, Toast.LENGTH_SHORT).show(); }
                }
            }
        });

        pernumber_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!greenButton && optionbutton)
                {
                    if (!(peritem_button.isActivated() || perperson_button.isActivated() || perc_button.isActivated()) && !numberSplit && !billSplitToZero)
                    {
                        if (!v.isActivated())
                        {
                            mode = NUMBER_MODE;
                            orderFragment.activateSelectionMode(OrderListAdapter.NUMBER_MODE);
                            orderSubdivisionFragment.setMode(OrderListAdapter.NUMBER_MODE);
                            activateFunction(CALCULATOR_INSERT_NUMBER, null, 0.0f);
                            hideOtherButtons(v);
                            v.setActivated(!v.isActivated());
                            anyButtonPressed = v.isActivated();
                            optionsFragment.deactivatePayments();
                            setOpacityForSplitButton();
                        }

                        else
                        {
                            mode = DEFAULT_MODE;
                            orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);

                            if (calculatorFragment.isActive())
                                { calculatorFragment.turnOnOffCalculator(); }

                            buttonOpacitySetting();
                            optionsFragment.activatePayments();
                            resetOpacityForSlplittButton();
                        }
                    }
                }
            }
        });

        setNormalKillOkButton();
    }


    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        int billId = intentPay.getIntExtra("billId",-1);
        fidelityClientId = intentPay.getIntExtra("fidelityClientId", -1);

        if (fidelityClientId != -1)
        {
            // since this is a special payment, no subdivision can be done
            // thus deactivate this feature
            perc_button.setEnabled(false);
            perperson_button.setEnabled(false);
            peritem_button.setEnabled(false);
            pernumber_button.setEnabled(false);

            orderSubdivisionFragment.setMenuVisibility(false);
            // TODO orderSubdivisionFragment.setMode();

            // set the button permission to buying fidelity,
            // thus remove the discount, email or other options,
            // and also disable the fidelity button (buying fidelity with fidelity should not be allowed)
            optionsFragment.setButtonPermissionBuyFidelity();
            optionsFragment.getAdapter().loadButtonsBuyFidelity();


            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));

            callHttpHandler("/getBillDataPayment", params);
            return;
        }


        if (StaticValue.blackbox)
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            callHttpHandler("/checkIfBillIsPaidOnly", params);
        }

        else
        {
            if (dbA.checkIfBillIsPaid(billId))
            {
                hideButtonPerNumber();
                optionsFragment.deactivatePayments();
                buttonOpacitySetting1();
                greenButton = true;
                orderFragment.paidOrder();
            }
        }

        if (selectedClient != -1)
        {
            ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
            invoiceBill = true;
            activateLayoutForInvoiceOnly();
        }

        if (!orderSubdivisionFragment.getSubdivisionAdapter().getItemsSize())
        {
            perc_button.performClick();
            orderSubdivisionFragment.getSubdivisionAdapter().showFirstItemAvaiable();
            orderSubdivisionFragment.getSubdivisionAdapter().notifyDataSetChanged();
        }
    }




    // ----- OK/KILL BUTTONS ------
    public void setKillOkButtonForSubdivision()
    {
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMode() == ITEM_MODE )
                {
                    orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                    orderSubdivisionFragment.setMode(OrderListAdapter.DEFAULT_MODE);
                    resetOpacityForOptionsButton();
                    resetOpacityForPayementButtons();
                    resetOpacityForSlplittButton();
                    resetOtherButtons();
                    setNormalKillOkButton();
                    optionsFragment.activatePayments();
                }

                else if(getMode() == PERSON_MODE)
                {
                    orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                    orderSubdivisionFragment.setMode(OrderListAdapter.DEFAULT_MODE);
                    resetOpacityForOptionsButton();
                    resetOpacityForPayementButtons();
                    resetOpacityForSlplittButton();
                    resetOtherButtons();
                    setNormalKillOkButton();
                    optionsFragment.activatePayments();
                }

                else
                {
                    orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                    orderSubdivisionFragment.setMode(OrderListAdapter.DEFAULT_MODE);
                    resetOpacityForOptionsButton();
                    resetOpacityForPayementButtons();
                    resetOpacityForSlplittButton();
                    resetOtherButtons();
                    setNormalKillOkButton();
                    optionsFragment.activatePayments();
                }
            }
        });
    }

    public void setKillForSplitOnCalculator()
    {
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();

        if (item == null)
        { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

        switch (item.getMode())
        {
            case PERCENTAGE_MODE :
            case NUMBER_MODE:
                break;

            case PERSON_MODE :
            case ITEM_MODE:
                activatePaymentButtonsOnly();
                hideSplitButton();
                showAllBlueButtonExSplit();
                break;

            default :
                break;
        }
    }

    public void setNormalKillOkButton()
    {
        resetOptionsButton();
        resetOpacityForSlplittButton();
        resetOpacityForPayementButtons();

        SubdivisionAdapter adapter = orderSubdivisionFragment.getSubdivisionAdapter();
        if (adapter != null)
        {
            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
            if (item == null)
            { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

            switch (item.getMode())
            {
                case PERCENTAGE_MODE:
                case NUMBER_MODE:
                    break;

                case PERSON_MODE:
                case ITEM_MODE:
                    if(!item.isHomage())
                        activatePaymentButtonsOnly();
                    hideSplitButton();
                    showAllBlueButtonExSplit();
                    break;

                default:
                    activatePaymentButtonsOnly();
                    resetOpacityForOptionsButton();
                    resetOpacityForSlplittButton();
                    if (orderSubdivisionFragment.getSubdivisionAdapter().returnIfPerNumberPresent())
                    {
                        //there is a split number
                        hidenBlueButtonExPrintAndEmail();
                    }
                    break;
            }
        }

        else
            { resetOpacityForOptionsButton(); }

        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // if this is a special case, in which the user is buying credits
                // delete all and go back
                if ( clientForCredit != -1 )
                {
                    Intent intent = new Intent(PaymentActivity.this, Operative.class);
                    String username = intentPay.getStringExtra("username");
                    int isAdmin = intentPay.getIntExtra("isAdmin", -1);
                    int tableNumber = intentPay.getIntExtra("tableNumber", -1);

                    // completely remove the bill for buying credits,
                    // since this bill can't be repeated
                    dbA.deleteBillData(intentPay.getIntExtra("billId", -1), getApplicationContext());

                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.setAction("normal");
                    intent.putExtra("billId", -1);
                    intent.putExtra("orderNumber", (orderNumber - 1));
                    intent.putExtra("tableNumber", tableNumber);
                    startActivity(intent);

                    finish();
                    try
                    {
                        PaymentActivity.this.finish();
                    } catch (Throwable throwable)
                    {
                        throwable.printStackTrace();
                    }
                }
                else if (discountSet)
                {
                    mode = DEFAULT_MODE;
                    orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                    discountSet = false;
                    showSplitButton();
                    showBlueButtonExDiscount();
                    optionsFragment.setButtonPermission();
                    resetOptionsButton();
                    showPaymentButton();
                }

                else if(splitItemSet)
                {
                    mode = DEFAULT_MODE;
                    orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
                    splitItemSet = false;
                    showSplitButton();
                    showAllBlueButton();
                    optionsFragment.setButtonPermission();
                    resetOptionsButton();
                    showPaymentButton();
                }

                else
                {
                    SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                    if (item == null)
                    { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

                    if (item.isHomage())
                    {
                        if (item.getMode() == -1)
                        { orderFragment.setTotalHomageMethod(item); }
                        else
                        {
                            int position = getSelectedItemPosition(item);
                            orderFragment.setTotalHomageMethodForItem(item, position);
                            switch (item.getMode()) {
                                case PERCENTAGE_MODE :
                                    break;
                                case PERSON_MODE :
                                    activatePaymentButtonsOnly();
                                    hideSplitButton();
                                    showAllBlueButtonExSplit();
                                    break;
                                case ITEM_MODE:
                                    activatePaymentButtonsOnly();
                                    hideSplitButton();
                                    showAllBlueButtonExSplit();
                                    break;
                                case NUMBER_MODE:
                                    break;
                                default :
                                    break;
                            }
                        }
                    }

                    else {
                        if (invoiceBill)
                        {
                            invoiceBill = false;
                            findViewById(R.id.invoice_button).setActivated(false);
                            findViewById(R.id.invoice_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white_press);
                            SubdivisionItem item1= orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                            if (item1 == null)
                            { item1 = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }
                            switch (item1.getMode())
                            {
                                case PERCENTAGE_MODE:
                                case NUMBER_MODE:
                                    break;

                                case PERSON_MODE:
                                case ITEM_MODE:
                                    activatePaymentButtonsOnly();
                                    hideSplitButton();
                                    showAllBlueButtonExSplit();
                                    break;

                                default:
                                    orderSubdivisionFragment.getSubdivisionAdapter().performClickOnTotal();
                                    showAllBlueButton();
                                    break;
                            }
                        }

                        else {
                            Intent intent = new Intent(PaymentActivity.this, Operative.class);
                            int billId = intentPay.getIntExtra("billId", -1);
                            int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                            String username = intentPay.getStringExtra("username");
                            int isAdmin = intentPay.getIntExtra("isAdmin", -1);
                            int tableNumber = intentPay.getIntExtra("tableNumber", -1);

                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.setAction("normal");
                            intent.putExtra("billId", billId);
                            intent.putExtra("orderNumber", (orderNumber - 1));
                            intent.putExtra("tableNumber", tableNumber);
                            startActivity(intent);

                            finish();
                            try
                            {
                                PaymentActivity.this.finish();
                            } catch (Throwable throwable)
                            {
                                throwable.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() <= 0)
                {
                    if (isHomage)
                    {
                        //con omaggio
                        if (orderFragment.getTotalCost() == 0.0f)
                        {
                            //omaggio tutto il bill
                            Intent intent = new Intent(PaymentActivity.this, Operative.class);
                            int billId = intentPay.getIntExtra("billId", -1);
                            int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                            String username = intentPay.getStringExtra("username");
                            int isAdmin = intentPay.getIntExtra("isAdmin", -1);

                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.putExtra("billId", billId);
                            intent.putExtra("orderNumber", (orderNumber - 1));
                            startActivity(intent);
                            finish();
                        }
                    }
                }

                else if(!splitItemSet)
                {
                    //
                    SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                    if (item == null)
                    {
                        item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                        if (item.isHomage())
                            { printBill(0.0f, 0.0f, 0.0f, 1);  }

                        else
                        {
                            int billId = intentPay.getIntExtra("billId", -1);
                            int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                            String username = intentPay.getStringExtra("username");
                            int isAdmin = intentPay.getIntExtra("isAdmin", -1);
                            int tableNumber = intentPay.getIntExtra("tableNumber", -1);

                            Intent intent = new Intent(PaymentActivity.this, Operative.class);
                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.setAction("normal");
                            intent.putExtra("billId", billId);
                            intent.putExtra("orderNumber", (orderNumber - 1));
                            intent.putExtra("tableNumber", tableNumber);
                            startActivity(intent);
                            finish();
                            try
                            {
                                PaymentActivity.this.finish();
                            }
                            catch (Throwable throwable)
                            {
                                throwable.printStackTrace();
                            }
                        }
                    }

                    else
                    {
                        if (item.isHomage())
                        {
                            if(StaticValue.blackbox)
                                { printBill(0.0f, 0.0f, 0.0f, 1); }

                            else
                            {
                                Gson gson = new Gson();
                                JSONObject combined = new JSONObject();
                                Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

                                ArrayList<CashButtonLayout> products = item.getItems();
                                ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
                                for (CashButtonLayout cashButton : products) {
                                    if (!cashButton.isSelected()) {
                                        myProducts.add(cashButton);
                                        int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                                        myProducts.get(myProducts.size() - 1).setQuantity(qty);
                                        ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                                        myModifiers.put(myProducts.get(myProducts.size() - 1), cashButton.getCashList());
                                    }
                                }

                                float itemsDiscount = 0.0f;
                                for (int i = 0; i < myProducts.size(); i++) {
                                    myProducts.get(i).setPosition(i);
                                    if (myProducts.get(i).getHomage() != 1)
                                        itemsDiscount += myProducts.get(i).getDiscount();
                                }

                                int billId = intentPay.getIntExtra("billId", -1);
                                int orderNumber = intentPay.getIntExtra("orderNumber", 1);

                                if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                    ditron.closeAll();
                                    ditron.startSocket();
                                }
                                ClientThread myThread = ClientThread.getInstance();
                                myThread.setProducts(myProducts);
                                myThread.setModifiers(myModifiers);
                                myThread.setPrintType(2);
                                myThread.setBillId(String.valueOf(billId));
                                myThread.setDeviceName(deviceName);
                                myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                myThread.setPaid(0.0f);
                                myThread.setCost(0.0f);
                                myThread.setCredit(0.0f);
                                myThread.setCreditL(0.0f);
                                myThread.setPaymentType(1);
                                myThread.setTotalDiscount(item.getOwed_money());
                                myThread.delegate = forClient;
                                myThread.setClientThread();
                                myThread.setRunBaby(true);
                                //  myThread.addJsonString(combined.toString());
                            }

                            if(item.getMode()==-1) {
                                pay_mode = PAY_TOTAL_BILL;
                                savePaidBill(1);
                            }else{
                                savePaidSubdivisionBill(item, 1);
                                orderSubdivisionFragment.getSubdivisionAdapter().setItemPaid(item);
                            }
                            orderSubdivisionFragment.getSubdivisionAdapter().showFirstItemAvaiable();

                            setNormalKillOkButton();
                        }

                        else
                        {
                            Intent intent = new Intent(PaymentActivity.this, Operative.class);
                            int billId = intentPay.getIntExtra("billId", -1);
                            int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                            String username = intentPay.getStringExtra("username");
                            int isAdmin = intentPay.getIntExtra("isAdmin", -1);
                            int tableNumber = intentPay.getIntExtra("tableNumber", -1);

                            // FIXME
                            // blunt solution
                            if (clientForCredit != -1)
                            {
                                intent.putExtra("username", username);
                                intent.putExtra("isAdmin", isAdmin);
                                intent.putExtra("billId", -1);
                                intent.putExtra("orderNumber", orderNumber+1);
                                intent.putExtra("tableNumber", tableNumber);
                                startActivity(intent);
                                finish();
                            }

                            else
                            {
                                intent.putExtra("username", username);
                                intent.putExtra("isAdmin", isAdmin);
                                intent.setAction("normal");
                                intent.putExtra("billId", billId);
                                intent.putExtra("orderNumber", (orderNumber - 1));
                                intent.putExtra("tableNumber", tableNumber);
                            }

                            startActivity(intent);
                            finish();
                            try {
                                PaymentActivity.this.finish();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    }

                }
            }
        });
    }





    // ----- ORDER SUBDIVISON FRAGMENT  -----

    // handle the subdivision setup and get/set methods
    // which allow to controls the Items (the groups in which the bill is divided)
    // like one item per client, or one item per groups of products present in the bill

    //get discount for this bill
    public float getDiscount()
    {
        int billId = intentPay.getIntExtra("billId", -1);
        return dbA.getBillDiscountPrice(billId);
    }

    //get discount for a subdivision item
    public float getDiscountForItem()
    {
        return orderSubdivisionFragment.getSubdivisionAdapter().returnDiscountForItem(products);
    }

    //get discount for specific subdivision item
    public float getDiscountForItemSelected(SubdivisionItem item)
    {
        float discount= 0.0f;
        ArrayList<CashButtonLayout> products = item.getItems();
        for (int i=0; i< products.size(); i++)
        {
            if (!products.get(i).isSelected())
                { discount += products.get(i).getDiscount(); }
        }
        return discount;
    }

    //set total bill items -> put the list of products inside total item
    public void setTotalBillItems(ArrayList<CashButtonLayout> products)
    {
        orderSubdivisionFragment.getSubdivisionAdapter().setTotalBillItemsForHomage(products);
    }

    //set products for specific subdivision item
    public void setItemProduct(ArrayList<CashButtonLayout> products)
    {
        orderSubdivisionFragment.getSubdivisionAdapter().setItemProduct(products);
    }

    //get homage for this item
    public float getHomageForItem()
    {
        float homage= 0.0f;
        for(int i=0; i< products.size(); i++){
            if(!products.get(i).isSelected()) {
                if (products.get(i).getHomage() == 1) {
                    homage += products.get(i).getPriceFloat();
                    ArrayList<CashButtonListLayout> mods = modifiers.get(products.get(i));
                    if(mods!=null) {
                        for (int j = 0; j < mods.size(); j++) {
                            homage += mods.get(j).getPriceFloat() * mods.get(j).getQuantityInt();
                        }
                    }}
            }
        }
        return homage;
    }

    //check if there are subdivision item person or item
    public boolean getIfSplitBillsAreItem()
    {
        return orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherAreItemSelectAndPaid2();
    }

    //easy....
    public boolean checkIfTotalBillHasDiscount()
    {
        return orderSubdivisionFragment.getSubdivisionAdapter().checkIfTotalHasDiscount();
    }

    //return the selected item
    public SubdivisionItem getSelectedItem()
    {
        return orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
    }

    //show the subdivision item
    public void showSubdivisionItem()
    {
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        if (item == null)
            { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

        if (item.getMode() == -1)
            { orderSubdivisionFragment.getSubdivisionAdapter().performClickOnTotal(); }
        else
        {
            int position = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItemPosition(item);
            orderSubdivisionFragment.getSubdivisionAdapter().showItem(position);
        }
    }

    //return the position of the selected subdivision item
    public int getSelectedItemPosition(SubdivisionItem item)
    {
        return orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItemPosition(item);
    }

    //return total subdivision item
    public SubdivisionItem getTotalItem()
    {
        return orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
    }

    //set total item as homage
    public void setTotalHomageForItem(int position, boolean activate)
    {
        orderSubdivisionFragment.getSubdivisionAdapter().setTotalHomageForItem(position, activate);
    }

    //check if total item is homage
    public boolean checkIfTotalHomage()
    {
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();

        if (item == null)
            { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

        return item.isHomage();
    }

    public Fidelity checkIfBillHasCustomer()
    {
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();

        if(item == null)
            { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

        Fidelity fid = new Fidelity();
        if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE || item.getMode() == -1)
        {
            if (item.getItems().get(0).getClientPosition() > 0)
            {
                Customer c = orderFragment.getOrderListAdapter().getCustomer(item.getItems().get(0).getClientPosition());
                ClientInfo client = dbA.fetchSingleClient(c.getCustomerId());
                Fidelity fidelity = dbA.fetchFidelityById(client.getFidelity_id());
                if (fidelity.getActive() == 1)
                    { fid = fidelity; }
            }
        }
        return fid;
    }

    public boolean checkSelectedItem()
    {
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        if (item != null)
        { return item.getMode() != NUMBER_MODE && item.getMode() != PERCENTAGE_MODE;  }
        else
        { return true; }
    }

    public float getRemainingCostSubdivisionItem()
    {
        return  orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost();
    }

    public float getRemainingCostForItemAndPerson()
    {
        return  orderSubdivisionFragment.getSubdivisionAdapter().returnSubdivisionItemsPrice();
    }

    public void setTotalSubdivisionPaid(float c)
    {
        orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(c);
    }

    public void setItemPaid(SubdivisionItem item){
        orderSubdivisionFragment.getSubdivisionAdapter().setItemPaid(item);
    }


    public float getRemainingForTotal()
    {
        return  orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCostForTotal();
    }

    public int getSubdivisionAdapterSize()
    {
        return orderSubdivisionFragment.getSubdivisionAdapter().getItemCount();
    }




    // ----- CALCULATOR FRAGMENT ------

    //get credit value from calculator fragment
    public float getActualCreditValue()
    {
        return calculatorFragment.getActualCredit();
    }

    public void setTempPositionDiscount(int i)
    {
        calculatorFragment.setTmpPositionDiscount(i);
    }

    public int getTempPositionDiscount()
    {
        return calculatorFragment.getTmpPositionDiscount();
    }

    public boolean getPercentageAmount()
    {
        return calculatorFragment.getPercentageAmount();
    }





    // ----- HIDE/SHOW BUTTONS ------ //

    public void hidePaymentButton()
    {
        optionsFragment.deactivatePayments();
    }

    public void showPaymentButton()
    {
        optionsFragment.activatePayments();
    }

    public void hidePersonAndItemDivision()
    {
        perc_button.setAlpha(1);
        perc_button.setActivated(false);
        pernumber_button.setAlpha(1);
        pernumber_button.setActivated(false);
        perperson_button.setAlpha(0.15f);
        perperson_button.setActivated(false);
        peritem_button.setAlpha(0.15f);
        peritem_button.setActivated(false);
        buttonPer.setPeritem(false);
        buttonPer.setPerperson(false);
        buttonPer.setPeramount(true);
        buttonPer.setPernumber(true);
        optionbutton = true;
    }

    public void hidenBlueButtonExDiscount()
    {
        findViewById(R.id.invoice_button).setAlpha(0.15f);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.print_button).setAlpha(0.15f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(0.15f);
        optionsFragment.setOnlyDiscount(true);
    }

    public void hidenBlueButtonExPrint()
    {
        findViewById(R.id.invoice_button).setAlpha(0.15f);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.print_button).setAlpha(1.0f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(0.15f);
        findViewById(R.id.discount_button).setAlpha(0.15f);
        optionsFragment.setButtonPermissionForPrintOnly();
    }

    public void hidenBlueButtonExPrintAndEmail()
    {
        findViewById(R.id.invoice_button).setAlpha(0.15f);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.print_button).setAlpha(1.0f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(1.0f);
        findViewById(R.id.discount_button).setAlpha(0.15f);
        optionsFragment.setButtonPermissionForNumber();
    }

    public void hidenBlueButtonForTotalHomage()
    {
        findViewById(R.id.invoice_button).setAlpha(0.15f);
        findViewById(R.id.discount_button).setAlpha(1f);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.print_button).setAlpha(1f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(1f);
    }

    public void hidenBlueButtonExHomage()
    {
        findViewById(R.id.invoice_button).setAlpha(0.15f);
        findViewById(R.id.discount_button).setAlpha(0.15f);
        findViewById(R.id.print_button).setAlpha(0.15f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(0.15f);
        //optionsFragment.setOnlyDiscount(true);
    }

    public void showAllBlueButton()
    {
        findViewById(R.id.invoice_button).setAlpha(1);
        findViewById(R.id.homage_button).setAlpha(1);
        findViewById(R.id.print_button).setAlpha(1);
        findViewById(R.id.round_button).setAlpha(1);
        findViewById(R.id.email_it_button).setAlpha(1);
        findViewById(R.id.discount_button).setAlpha(1);
        findViewById(R.id.discount_button).setActivated(false);
        findViewById(R.id.discount_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white_press);
        optionsFragment.setButtonPermission();
    }

    public void showAllBlueButtonExSplit()
    {
        findViewById(R.id.invoice_button).setAlpha(1);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.print_button).setAlpha(1);
        findViewById(R.id.round_button).setAlpha(1);
        findViewById(R.id.email_it_button).setAlpha(1);
        findViewById(R.id.discount_button).setAlpha(1);
        findViewById(R.id.discount_button).setActivated(false);
        findViewById(R.id.discount_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white_press);

        optionsFragment.setButtonPermissionExSplit();
    }

    public void showBlueButtonExDiscount()
    {
        findViewById(R.id.invoice_button).setAlpha(1f);
        findViewById(R.id.homage_button).setAlpha(1f);
        findViewById(R.id.print_button).setAlpha(1f);
        findViewById(R.id.round_button).setAlpha(1f);
        findViewById(R.id.email_it_button).setAlpha(1f);

        optionsFragment.setOnlyDiscount(false);
    }

    public void hidenOnlyBlueButton()
    {
        findViewById(R.id.invoice_button).setAlpha(0.15f);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.discount_button).setAlpha(0.15f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(0.15f);
    }

    public void hidenOnlyBlueButton2()
    {
        findViewById(R.id.invoice_button).setAlpha(0.15f);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(0.15f);
    }

    public void hideBlueButton()
    {
        buttonOpacitySetting1();
        hidenOnlyBlueButton2();
    }

    public void hideButtonPerNumber()
    {
        buttonOpacitySetting1();
        setOpacityForPayementButtons();
        hidenOnlyBlueButton();

        greenButton = true;
        optionsFragment.setOnlyPrint(true);
    }

    public void resetButtonPerNumberBlue()
    {
        findViewById(R.id.invoice_button).setAlpha(1.0f);
        findViewById(R.id.homage_button).setAlpha(1.0f);
        findViewById(R.id.discount_button).setAlpha(1.0f);
        findViewById(R.id.round_button).setAlpha(1.0f);
        findViewById(R.id.email_it_button).setAlpha(1.0f);
        optionsFragment.setOnlyPrint(false);
        setButtonPermission();
        setOptionButton(true);
    }

    public void resetButtonPerNumberGreen()
    {
        resetOpacityForPayementButtons();

        greenButton = false;
    }

    public void activatePaymentButtonsOnly()
    {
        greenButton = false;
        optionsFragment.getAdapter().setActive(true);
        optionsFragment.activateOnlyPayment();
    }

    public void activatePaymentButtonsSpec()
    {
        if(orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()==1)
            { optionsFragment.getAdapter().loadButtons(0); }

        else
        {
            if (!orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherPersonOrItem())
                { optionsFragment.getAdapter().loadButtons(0); }
            else
                { optionsFragment.getAdapter().loadButtonsPaymentOnly(); }
        }
    }

    public void hideOtherButtons(View v)
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.button_container);
        for(int i = 0; i < relativeLayout.getChildCount(); i++) {
            View child = relativeLayout.getChildAt(i);
            if(child!=v){
                child.setAlpha(0.15f);
            }
        }
    }

    public void resetOtherButtons()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.button_container);
        for(int i = 0; i < relativeLayout.getChildCount(); i++)
        {
            View child = relativeLayout.getChildAt(i);
            child.setAlpha(1.0f);

            if(child.isActivated())
            { child.setActivated(!child.isActivated()); }

        }
        setOptionButton(true);
    }

    public void setAllButtonPermission()
    {
        buttonPer.setPeritem(true);
        buttonPer.setPerperson(true);
    }

    public void setButtonPermission()
    {
        optionsFragment.setButtonPermission();
    }


    public void setButtonPermissionExSplit()
    {
        optionsFragment.setButtonPermissionExSplit();
    }


    public void buttonOpacitySetting1()
    {
        if (perc_button.isActivated())
        { perc_button.setActivated(!perc_button.isActivated()); }
        if (pernumber_button.isActivated())
        { pernumber_button.setActivated(!pernumber_button.isActivated()); }
        if (perperson_button.isActivated())
        { perperson_button.setActivated(!perperson_button.isActivated()); }
        if (peritem_button.isActivated())
        { peritem_button.setActivated(!peritem_button.isActivated()); }
        perc_button.setAlpha(0.15f);
        pernumber_button.setAlpha(0.15f);
        perperson_button.setAlpha(0.15f);
        peritem_button.setAlpha(0.15f);
        setOptionButton(false);
    }

    public void buttonOpacitySettingForPerNumber()
    {
        perc_button = (CustomButton)findViewById(R.id.percentage_button);
        perperson_button = (CustomButton)findViewById(R.id.perperson_button);
        peritem_button = (CustomButton)findViewById(R.id.peritem_button);
        pernumber_button = (CustomButton)findViewById(R.id.pernumber_button);
        perc_button.setAlpha(0.15f);

        pernumber_button.setAlpha(0.15f);
        perperson_button.setAlpha(0.15f);
        peritem_button.setAlpha(0.15f);
        optionsFragment.setButtonPermissionForPrintOnly();
    }

    public void hideSplitButton()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.button_container);
        relativeLayout.setAlpha(0.15f);
        View upperLine = findViewById(R.id.bottom_hline1_button_container);
        View bottomLine = findViewById(R.id.bottom_hline2_button_container);
        upperLine.setAlpha(0.15f);
        bottomLine.setAlpha(0.15f);
        setOptionButton(false);

    }


    public void showSplitButton()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.button_container);
        relativeLayout.setAlpha(1f);
        View upperLine = findViewById(R.id.bottom_hline1_button_container);
        View bottomLine = findViewById(R.id.bottom_hline2_button_container);
        upperLine.setAlpha(1f);
        bottomLine.setAlpha(1f);
        setOptionButton(true);
    }


    public void buttonOpacitySetting()
    {
        Map<CustomButton, Boolean> buttons = new HashMap<CustomButton, Boolean>() {{
            put(perc_button, buttonPer.getPeramount());
            put(pernumber_button, buttonPer.getPernumber());
            put(perperson_button, buttonPer.getPerperson());
            put(peritem_button, buttonPer.getPeritem());
        }};

        for (Map.Entry<CustomButton, Boolean> entry : buttons.entrySet())
        {
            if (!entry.getValue())
            { entry.getKey().setAlpha(0.15f); }
            else
            {
                entry.getKey().setAlpha(1.0f);
                if (entry.getKey().isActivated())
                { entry.getKey().setActivated(false); }
            }
        }
    }

    public void hidePartial()
    {
        orderFragment.hideRemaingTotal();
    }


    public void hidePartialWithDiscount()
    {
        orderFragment.hideRemainingWithDiscount();
    }


    public void performClickOnItem()
    {
        mode = DEFAULT_MODE;
        orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
        buttonOpacitySetting();
        setNormalKillOkButton();
        optionsFragment.activatePayments();
        resetOpacityForSlplittButton();
        setOpacityExSplit();
    }


    public boolean checkIfButtonActivated()
    {
        return peritem_button.isActivated() || perperson_button.isActivated() || pernumber_button.isActivated() || perperson_button.isActivated();
    }


    public void resetOptionsButton()
    {
        View tryView = findViewById(R.id.homage_button);

        if (tryView != null)
        {
            findViewById(R.id.homage_button).setBackgroundColor(lightBlue);
            findViewById(R.id.discount_button).setBackgroundColor(lightBlue);
        }
    }





    // --------------------------------------------
    public void closeBill(boolean close)
    {
        orderFragment.getOrderListAdapter().setBillClose(close);
    }

    public void openCashDrawer()
    {
        if (StaticValue.blackbox)
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            callHttpHandler("/openCashDrawer", params);
        }

        else
        {
            if (StaticValue.printerName.equals("ditron"))
            {
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.closeAll();
                ditron.startSocket();
            }
            ClientThread myThread = ClientThread.getInstance();
            myThread.setPrintType(14);
            myThread.setDeviceName(StaticValue.shopName);
            myThread.setOrderNumberBill("1");
            myThread.setBillId("1");
            myThread.delegate = forClient;
            myThread.setClientThread();
            myThread.setRunBaby(true);
        }
    }

    //expand group in order fragment
    public void expandGroup(){
        orderFragment.expandGroups();
    }


    public void activateLayoutForInvoiceOnly()
    {
        activatePaymentButtonsOnly();
        hideSplitButton();

        findViewById(R.id.invoice_button).setAlpha(1.0f);
        findViewById(R.id.invoice_button).setActivated(true);
        findViewById(R.id.invoice_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.print_button).setAlpha(0.15f);
        findViewById(R.id.round_button).setAlpha(0.15f);
        findViewById(R.id.email_it_button).setAlpha(0.15f);
        findViewById(R.id.discount_button).setAlpha(0.15f);

        optionsFragment.setButtonPermissionForInvoice();
    }

    public boolean returnTotalIsPaid() {
        int billId = intentPay.getIntExtra("billId",-1);
        return dbA.checkIfBillIsPaid(billId);
    }

    public float returnTotalBillDiscount() {
        int billId = intentPay.getIntExtra("billId",-1);
        return dbA.getBillDiscountPrice(billId);
    }


    public void modifyDiscountValue(View v, int groupPosition, int click)
    {
        calculatorFragment.setTmpDiscount(orderFragment.getOrderListAdapter().getElementDiscount(groupPosition));
        calculatorFragment.setTmpPositionDiscount(groupPosition);

        hidenBlueButtonExDiscount();
        hidePaymentButton();
        hideSplitButton();

        discountSet = true;
        mode = MODIFY_DISCOUNT_MODE;

        orderFragment.activateSelectionMode(OrderListAdapter.MODIFY_DISCOUNT_MODE);
        orderSubdivisionFragment.setMode(OrderListAdapter.MODIFY_DISCOUNT_MODE);
        orderFragment.setTotalCostDiscount();

        findViewById(R.id.homage_button).setBackgroundColor(lightBlue);
        findViewById(R.id.discount_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white);

        orderFragment.setMode(16);

        doSomething(v, groupPosition, click);
    }


    public void doSomething(View v, int groupPosition, int click)
    {
        orderFragment.doSomething(v, groupPosition, click);
    }

    public void setTotalHomage(){
        orderFragment.setTotalCostHomage();
    }


    @Override
    public void activateFunction(int function_id, Object dataBundle, Float any_float_value)
    {
        switch (function_id)
        {
            case CALCULATOR_ACTIVATION: // 1
                // 1: calculator on/off
                calculatorFragment.setPayementShortcut();
                calculatorFragment.turnOnOffCalculator();
                setOpacityForSplitButton();
                buttonOpacitySetting1();
                break;

            case CALCULATOR_NOTIFY_COST: // 2
                calculatorFragment.setCost((String)dataBundle);
                break;

            case CALCULATOR_OFF: // 3
                optionsFragment.activatePayments();
                break;

            case CALCULATOR_INSERT_NUMBER: // 4
                calculatorFragment.setMode(OrderListAdapter.NUMBER_MODE);
                calculatorFragment.turnOnOffCalculator();
                break;

            case CALCULATOR_INSERT_PERCENTAGE: // 5
                calculatorFragment.setPercentageShortCut();
                calculatorFragment.setMode(PERCENTAGE_MODE);
                calculatorFragment.turnOnOffCalculator();
                break;

            case CALCULATOR_INSERT_PARTIAL: // 103
                calculatorFragment.setPayementShortcut();
                calculatorFragment.setMode(PAY_PARTIAL_MODE);
                calculatorFragment.turnOnOffCalculator();
                break;

            case ADD_SUBDIVISION_ELEMENT: // 6
                orderSubdivisionFragment.addElement(dataBundle, any_float_value);
                switch (mode)
                {
                    case PERCENTAGE_MODE:
                        setNormalKillOkButton();
                        orderFragment.percentageSplitForAmount(((Float)dataBundle));
                        perc_button.performClick();
                        percentageSplit = true;
                        setButtonPermission();
                        buttonOpacitySetting1();
                        setOpacityExSplit();
                        break;

                    case PERSON_MODE:
                        performClickOnItem();
                        float remaining1 = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost();
                        orderFragment.setItemCost(((Float)any_float_value), remaining1);
                        setButtonPermission();
                        buttonOpacitySetting1();
                        activatePaymentButtonsSpec();
                        break;

                    case ITEM_MODE:
                        performClickOnItem();
                        float remaining = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost();
                        orderFragment.setItemCost(((Float)any_float_value), remaining);
                        setButtonPermissionExSplit();
                        buttonOpacitySetting1();
                        activatePaymentButtonsSpec();
                        break;

                    case NUMBER_MODE:
                        pernumber_button.performClick();
                        orderFragment.setNumberSplit(true);
                        numberSplit = true; //il fatto che venga fatto dopo performClick non è casuale!!
                        findViewById(R.id.invoice_button).setAlpha(0.15f);
                        findViewById(R.id.homage_button).setAlpha(0.15f);
                        findViewById(R.id.discount_button).setAlpha(0.15f);
                        findViewById(R.id.round_button).setAlpha(0.15f);
                        findViewById(R.id.email_it_button).setAlpha(0.15f);
                        calculatorFragment.turnOffCalculator();
                        isCalculatorOn = false;
                        calculatorFragment.setCost(String.valueOf(any_float_value));
                        break;
                }
                orderFragment.getOrderListAdapter().isSplit = true;
                break;

            case HOMAGE_MODE :  // ?
                //not needed anymore
                String txt2 = String.format("%.2f", roundDecimal(any_float_value, 2));
                calculatorFragment.setCost(txt2);
                isHomage = true;
                printBill(any_float_value, any_float_value, 0, 1);
                savePaidBill(1);
                break;

            case DISCOUNT_MODE : // 7
                float cost = orderFragment.getElementPrice();
                String txt = String.format("%.2f", roundDecimal(cost, 2));
                calculatorFragment.setCost(txt);
                calculatorFragment.setMode(DISCOUNT_MODE);
                calculatorFragment.setDiscountMode(false);
                calculatorFragment.setIsActiveToFalse();
                calculatorFragment.turnOnOffCalculator();
                float elementCostDiscount = orderFragment.getElementDiscount(Math.round(any_float_value));
                if(elementCostDiscount !=0.0f){
                    calculatorFragment.setDiscountValue(elementCostDiscount, (CashButtonLayout) dataBundle);
                }
                break;

            case MODIFY_DISCOUNT_MODE :
                float elementCost = orderFragment.getElementDiscount(Math.round(any_float_value));
                calculatorFragment.setMode(MODIFY_DISCOUNT_MODE);
                calculatorFragment.setDiscountMode(false);
                calculatorFragment.setIsActiveToFalse();
                calculatorFragment.turnOnOffCalculator();
                calculatorFragment.setDiscountValue(elementCost, (CashButtonLayout) dataBundle);
                break;

            case ADD_DISCOUNT:
                break;

            case DISCOUNT_MODE_OFF:
                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                if(item == null)
                    { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }
                float other  =orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCostForTotal();
                float remain = item.getOwed_money()-item.getDiscount()-getDiscountForItemSelected(item)/*getDiscountForItem()*/-getHomageForItem()/*-other*/;
                remain = remain-calculatorFragment.getActualCredit();
                float cost1 = remain;
                if(item.getMode()==-1 && !orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherPersonOrItem())
                {
                    String txt1 = String.format("%.2f", roundDecimal(cost1 - other, 2));
                    calculatorFragment.setCost(txt1);
                }
                else
                {
                    String txt1 = String.format("%.2f", roundDecimal(cost1 , 2));
                    calculatorFragment.setCost(txt1);
                }
                calculatorFragment.setPayementShortcut();
                break;

            case TOTAL_DISCOUNT_MODE:
                float totalCost = orderFragment.getTotalCost();
                String totalTxt= String.format("%.2f", roundDecimal(totalCost, 2));
                calculatorFragment.setCost(totalTxt);
                calculatorFragment.setMode(DISCOUNT_MODE);
                //set euro as firt discount
                calculatorFragment.setDiscountMode(false);
                calculatorFragment.setIsActive(false);
                calculatorFragment.turnOnOffCalculator();
                break;

            case TOTAL_MODIFY_DISCOUNT_MODE:
                float totalCostm = orderFragment.getTotalCost();
                String totalTxtm= String.format("%.2f", roundDecimal(totalCostm, 2));
                calculatorFragment.setCost(totalTxtm);
                calculatorFragment.setMode(MODIFY_DISCOUNT_MODE);
                //set euro as firt discount
                calculatorFragment.setDiscountMode(false);
                calculatorFragment.turnOnOffCalculator();
                break;

            case PARTIAL_TOTAL_DISCOUNT_MODE:
                float partialCost = orderFragment.returnRemaningTotal();
                String partialTxt= String.format("%.2f", roundDecimal(partialCost, 2));
                calculatorFragment.setCost(partialTxt);
                calculatorFragment.setMode(DISCOUNT_MODE);
                calculatorFragment.setDiscountMode(true);
                calculatorFragment.turnOnOffCalculator();
                break;

            case CALCULATOR_ACTIVATION_TICKET: // 104
                calculatorFragment.setMode(PAY_TICKET_MODE);
                calculatorFragment.setTicketShortCut();
                calculatorFragment.turnOnOffCalculator();
                break;

            case CALCULATOR_ACTIVATION_FOR_CREDIT: // 105
                calculatorFragment.setMode(INSERT_CREDIT_MODE);
                calculatorFragment.setNOShortCut();
                calculatorFragment.turnOnOffCalculator();
                break;

            case ELEMENT_ITEM_SPLIT:
                optionsFragment.setButtonPermissionForSplitElementItem();
                calculatorFragment.setMode(ELEMENT_ITEM_SPLIT);
                calculatorFragment.turnOnOffCalculator();
                isCalculatorOn = false;
                break;

            case CALCULATOR_ACTIVATION_FIDELITY: // 106
                String totalCredit = String.format("%.2f", roundDecimal(Math.round(any_float_value), 2));
                calculatorFragment.setCost(totalCredit);
                calculatorFragment.setMode(INSERT_FIDELITY_MODE);
                calculatorFragment.turnOnOffCalculator();
                break;
        }
    }



    public void printInvoice(float paid, float cost, float credit, int paymentType)
    {
        if(StaticValue.blackbox)
        {
            int billId = intentPay.getIntExtra("billId", -1);

            ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);

            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();

            if (item == null)
                item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();

            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

            ArrayList<Customer> myCustomers = new ArrayList<>();
            Customer customer = new Customer();
            customer.setActive(true);
            customer.setDescription("Fatture per " + clientInfo.getCompany_name());
            customer.setCustomerId(clientInfo.getClient_id());
            customer.setPosition(1);
            customer.setDelete(false);
            myCustomers.add(customer);

            ArrayList<CashButtonLayout> products = item.getItems();
            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
            for (CashButtonLayout cashButton : products)
            {
                if (!cashButton.isSelected())
                {
                    myProducts.add(cashButton);
                    int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                    myProducts.get(myProducts.size() - 1).setQuantity(qty);
                    ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                    myModifiers.put(myProducts.get(myProducts.size() - 1), cashButton.getCashList());
                }
            }

            float itemsDiscount = 0.0f;
            for (int i = 0; i < myProducts.size(); i++) {
                myProducts.get(i).setPosition(i);
                if (myProducts.get(i).getHomage() != 1)
                    itemsDiscount += myProducts.get(i).getDiscount();
            }

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            Gson gson = new Gson();
            String selectedC= gson.toJson(clientInfo);
            String prods= gson.toJson(myProducts);
            String mods= gson.toJson(myModifiers);
            String costum= gson.toJson(myCustomers);

            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
            params.add(new BasicNameValuePair("clientInfo",selectedC));
            params.add(new BasicNameValuePair("products", prods));
            params.add(new BasicNameValuePair("modifiers", mods));
            params.add(new BasicNameValuePair("customer", costum));
            params.add(new BasicNameValuePair("printType", String.valueOf(16)));
            //params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
            params.add(new BasicNameValuePair("paid", String.valueOf(paid)));
            params.add(new BasicNameValuePair("cost", String.valueOf(cost)));
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            params.add(new BasicNameValuePair("androidId", android_id));
            //params.add(new BasicNameValuePair("tableNumber", String.valueOf(tableNumber)));
            //params.add(new BasicNameValuePair("roomName", String.valueOf(orderFragment.getRoomName())));

            callHttpHandler("/printPaidBillInvoice", params);
        }

        else {
            ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
            int numeroFattura = dbA.selectNumeroFattura();
            numeroFattura++;
            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
            if (item == null)
                item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

            ArrayList<Customer> myCustomers = new ArrayList<>();
            Customer customer = new Customer();
            customer.setActive(true);
            customer.setDescription("Fatture per " + clientInfo.getCompany_name());
            customer.setCustomerId(clientInfo.getClient_id());
            customer.setPosition(1);
            customer.setDelete(false);
            myCustomers.add(customer);
            ArrayList<CashButtonLayout> products = item.getItems();
            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
            for (CashButtonLayout cashButton : products) {
                if (!cashButton.isSelected()) {
                    myProducts.add(cashButton);
                    int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                    myProducts.get(myProducts.size() - 1).setQuantity(qty);
                    ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                    myModifiers.put(myProducts.get(myProducts.size() - 1), cashButton.getCashList());
                }
            }

            float itemsDiscount = 0.0f;
            for (int i = 0; i < myProducts.size(); i++) {
                myProducts.get(i).setPosition(i);
                if (myProducts.get(i).getHomage() != 1)
                    itemsDiscount += myProducts.get(i).getDiscount();
            }
            XmlParser xml = new XmlParser();
            xml.createXml(clientInfo, myProducts, myModifiers, numeroFattura, paymentType);


            if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.closeAll();
                ditron.startSocket();
            }

            ClientThread myThread = ClientThread.getInstance();
            myThread.delegate = forClient;
            myThread.setProducts(myProducts);
            myThread.setModifiers(myModifiers);
            myThread.setPrintType(16);
            myThread.setOrderNumber(String.valueOf(orderNumber));
            myThread.setPaid(paid);
            myThread.setCost(cost);
            myThread.setPaymentType(paymentType);
            myThread.setTotalDiscount(0.0f);
            myThread.setTableNumber(tableNumber);
            myThread.setRoomName(orderFragment.getRoomName());
            myThread.setNumeroFattura(numeroFattura);
            myThread.setClientInfo(clientInfo);

            myThread.setClientThread();
            myThread.setRunBaby(true);

            dbA.updateNumeroFatture(numeroFattura);
            savePaidBillInvoice(paymentType, clientInfo.getClient_id());
        }
    }

    public double getFidelityValue() {
        double fidelity = 0.0;
        if(products.get(0).getClientPosition() > 0){
            for (int i = 0; i < products.size(); i++) {
                fidelity = fidelity + 1;
            }
        }
        return fidelity;
    }

    public int getFidelityCustomer() {
        int customerId = -1;
        if(products.get(0).getClientPosition()>0){
            Customer c = orderFragment.getOrderListAdapter().getCustomer(products.get(0).getClientPosition());
            customerId = c.getCustomerId();
        }
        return customerId;

    }



    // -------- PRINTER ------ //

    @Override
    public void printBill(float paid, float cost, float credit, int paymentType)
    {
        if (invoiceBill)
            { printInvoice(paid, cost, credit, paymentType); }

        else
        {
            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
            for (CashButtonLayout cashButton : products)
            {
                if (!cashButton.isSelected())
                {
                    myProducts.add(cashButton);
                    int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                    myProducts.get(myProducts.size() - 1).setQuantity(qty);

                    ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                    mList = modifiers.get(cashButton);
                    if (mList != null) {
                        Collections.sort(modifiers.get(cashButton));
                    }
                }
            }

            float creditToGive = getCreditValueAgain();

            int billId = intentPay.getIntExtra("billId", -1);
            int orderNumber = intentPay.getIntExtra("orderNumber", 1);

            if (StaticValue.blackbox)
            {
                Map<String,ArrayList<CashButtonListLayout>> test = new HashMap<String,ArrayList<CashButtonListLayout>>();
                for(int i =0; i < products.size(); i++){
                    test.put(String.valueOf(products.get(i).getPosition()), modifiers.get(products.get(i)));
                }

                int customerId = -1;
                if(products.get(0).getClientPosition()>0){
                    Customer c = orderFragment.getOrderListAdapter().getCustomer(products.get(0).getClientPosition());
                    customerId = c.getCustomerId();
                }

                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();

                Gson gson = new Gson();
                String prods = gson.toJson(myProducts);
                String mods = gson.toJson(test);
                String creditArray = gson.toJson(creditId);

                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("products", prods));
                params.add(new BasicNameValuePair("modifiers", mods));
                params.add(new BasicNameValuePair("printType", String.valueOf(1)));
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
                params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
                params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
                params.add(new BasicNameValuePair("paid", String.valueOf(paid)));
                params.add(new BasicNameValuePair("cost", String.valueOf(orderFragment.getOrderListAdapter().getTotal_cost())));
                params.add(new BasicNameValuePair("creditArray", creditArray));
                params.add(new BasicNameValuePair("creditL", String.valueOf(credit)));
                params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                params.add(new BasicNameValuePair("fidelity", String.valueOf(item.getFidelity())));

                callHttpHandler("/printBill", params);

                deleteCredit();
                savePaidBill(paymentType);
            }

            else {

                Float totalDiscount = dbA.getBillDiscountPrice(billId);
                if (totalDiscount == 0.0f) {

                        if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                            PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                            ditron.closeAll();
                            ditron.startSocket();
                        }

                        ClientThread myThread = ClientThread.getInstance();
                        myThread.delegate = forClient;
                        myThread.setProducts(products);
                        myThread.setModifiers(modifiers);
                        myThread.setPrintType(1);
                        myThread.setBillId(String.valueOf(billId));
                        myThread.setDeviceName(deviceName);
                        myThread.setOrderNumber(String.valueOf(orderNumber));
                        myThread.setPaid(paid);
                        myThread.setCost(orderFragment.getOrderListAdapter().getTotal_cost());
                        myThread.setCredit(creditToGive);
                        myThread.setCreditL(credit);
                        myThread.setPaymentType(paymentType);

                        myThread.setClientThread();
                        myThread.setRunBaby(true);
                        // myThread.addJsonString(combined.toString());


                    deleteCredit();
                    savePaidBill(paymentType);
                    if (credit > 0) {
                        saveBillCredit(credit);
                    }


                } else {
                    Double costo = dbA.getBillPrice(billId);
                    if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                        PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                        ditron.closeAll();
                        ditron.startSocket();
                    }
                    ClientThread myThread = ClientThread.getInstance();
                    myThread.delegate = forClient;
                    myThread.setProducts(products);
                    myThread.setModifiers(modifiers);
                    myThread.setPrintType(2);
                    myThread.setBillId(String.valueOf(billId));
                    myThread.setDeviceName(deviceName);
                    myThread.setOrderNumber(String.valueOf(orderNumber));
                    myThread.setPaid(paid);
                    myThread.setCost(costo.floatValue());
                    myThread.setCredit(creditToGive);
                    myThread.setCreditL(credit);
                    myThread.setPaymentType(paymentType);
                    myThread.setTotalDiscount(totalDiscount);
                    myThread.setClientThread();
                    myThread.setRunBaby(true);
                    //myThread.addJsonString(combined.toString());
                    deleteCredit();
                    savePaidBill(paymentType);
                    if (credit > 0) {
                        saveBillCredit(credit);
                    }
                }
            }
        }

    }

    public void printItemBill(SubdivisionItem item, float cost, float paid, int paymentType)
    {
       // if(!StaticValue.blackbox) {
            if (invoiceBill) {
                printInvoice(paid, cost, 0.0f, paymentType);

                invoiceBill = false;
                findViewById(R.id.invoice_button).setActivated(false);
                findViewById(R.id.invoice_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white_press);
            } else {
                ArrayList<CashButtonLayout> products = item.getItems();
                ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();


                float itemsDiscount = 0.0f;
                for (int i = 0; i < myProducts.size(); i++) {
                    myProducts.get(i).setPosition(i);
                    if (myProducts.get(i).getHomage() != 1)
                        itemsDiscount += myProducts.get(i).getDiscount();
                }
                if (paymentType == 2) paid = item.getOwed_money();

                int billId = intentPay.getIntExtra("billId", -1);
                int orderNumber = intentPay.getIntExtra("orderNumber", 1);

                Customer myCustomer = new Customer();

                if(StaticValue.blackbox)
                {

                    // test if this is a fidelity credit buying
                    if (fidelityClientId != -1)
                    {
                        // extract bluntly the amount of credits
                        String creditAmount = products.get(0).getTitle().replaceAll(".+\\(([0-9]+)\\)", "$1");

                        //add the fidelity credit to the given client
                        ArrayList<NameValuePair> creditParams = new ArrayList<>();
                        creditParams.add(new BasicNameValuePair("clientId", String.valueOf(fidelityClientId)));
                        creditParams.add(new BasicNameValuePair("amount", creditAmount));

                        callHttpHandler("/addFidelityCredit", creditParams);
                    }

                    Map<String,ArrayList<CashButtonListLayout>> test =
                            new HashMap<String,ArrayList<CashButtonListLayout>>();
                    for(int i =0; i<products.size(); i++){
                        test.put(String.valueOf(products.get(i).getPosition()), modifiers.get(products.get(i)));
                    }

                    Map<Integer, ArrayList<CashButtonListLayout>> myModifiers = new HashMap<Integer, ArrayList<CashButtonListLayout>>();


                    for (CashButtonLayout cashButton : products) {
                        if (!cashButton.isSelected()) {
                            myProducts.add(cashButton);
                            ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                            for (CashButtonListLayout m : mList) {
                                int qtyM = orderFragment.getOrderListAdapter().returnQuantityForModifier(cashButton, m);
                                m.setQuantity(qtyM);
                            }
                            int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                            myProducts.get(myProducts.size() - 1).setQuantity(qty);

                            //myModifiers.put(myProducts.get(myProducts.size() - 1), mList);
                            myModifiers.put(myProducts.get(myProducts.size() - 1).getPosition(), mList);
                        }
                    }
                    if(myProducts.get(0).getClientPosition()>0){
                        myCustomer = orderFragment.getOrderListAdapter().getCustomer(myProducts.get(0).getClientPosition());
                    }

                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    Gson gson = new Gson();
                    String prods = gson.toJson(myProducts);
                    String mods = gson.toJson(myModifiers);
                    String creditArray = gson.toJson(creditId);
                    params.add(new BasicNameValuePair("products", prods));
                    params.add(new BasicNameValuePair("modifiers", mods));
                    params.add(new BasicNameValuePair("printType", String.valueOf(2)));
                    params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                    params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
                    params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
                    params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
                    params.add(new BasicNameValuePair("paid", String.valueOf(paid)));
                    params.add(new BasicNameValuePair("cost", String.valueOf(item.getOwed_money())));
                    params.add(new BasicNameValuePair("creditArray", creditArray));
                    params.add(new BasicNameValuePair("creditL", String.valueOf(0.0f)));
                    params.add(new BasicNameValuePair("totalDiscount", String.valueOf(item.getDiscount() + itemsDiscount)));
                    params.add(new BasicNameValuePair("fidelity", String.valueOf(item.getFidelity())));
                    params.add(new BasicNameValuePair("customerId", String.valueOf(myCustomer.getCustomerId())));

                    callHttpHandler("/printItemBill", params);

                    deleteCredit();
                }else {

                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();


                    for (CashButtonLayout cashButton : products) {
                        if (!cashButton.isSelected()) {
                            myProducts.add(cashButton);
                            ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                            for (CashButtonListLayout m : mList) {
                                int qtyM = orderFragment.getOrderListAdapter().returnQuantityForModifier(cashButton, m);
                                m.setQuantity(qtyM);
                            }
                            int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                            myProducts.get(myProducts.size() - 1).setQuantity(qty);

                            //myModifiers.put(myProducts.get(myProducts.size() - 1), mList);
                            myModifiers.put(myProducts.get(myProducts.size() - 1), mList);
                        }
                    }

                    ClientThread myThread = ClientThread.getInstance();
                    myThread.delegate = forClient;
                    myThread.setProducts(myProducts);
                    myThread.setModifiers(myModifiers);
                    myThread.setPrintType(2);
                    myThread.setBillId(String.valueOf(billId));
                    myThread.setDeviceName(deviceName);
                    myThread.setOrderNumber(String.valueOf(orderNumber));
                    myThread.setPaid(paid);
                    myThread.setCost(item.getOwed_money());
                    myThread.setCredit(0.0f);
                    myThread.setCreditL(0.0f);
                    myThread.setPaymentType(paymentType);
                    myThread.setTotalDiscount(item.getDiscount() + itemsDiscount);
                    myThread.setClientThread();
                    myThread.setRunBaby(true);
                }


            }
       // }

    }

    
    public int getBillId(){
        return intentPay.getIntExtra("billId", -1);
    }

    public void saveBillCredit(float credit)
    {
        int bId = intentPay.getIntExtra("billId", -1);
        if(bId!=-1)
            { dbA.insertBillCredit(bId, credit); }
    }

    public boolean savePaidBillInvoice(int paymentType, int clientId)
    {
        int billId = intentPay.getIntExtra("billId", -1);
        SubdivisionItem item = orderFragment.getOrderListAdapter().getSubdivisionItem();
        calculatorFragment.resetActualCredit();

        if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() > 0)
            { orderSubdivisionFragment.getSubdivisionAdapter().setOpenSplitPaid(); }

        // If pay mode is set to PAY_TOTAL_BILL or if the bill splits are all paid, it
        // updates the total bill db value setting it to paid on the current timestamp.
        // If not, it saves the paid bill split data for the next time.
        boolean f;
        Float delta = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost();
        if ( delta <= 0 || pay_mode == PAY_TOTAL_BILL)
        {
            //if( orderFragment.returnRemaningTotal()==0.0f || pay_mode == PAY_TOTAL_BILL){
            if(StaticValue.blackbox)
            {
                f = true;
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
                params.add(new BasicNameValuePair("clientId", String.valueOf(clientId)));
                callHttpHandler("/savePaidBillInvoice", params);
            }

            else
            {
                f = dbA.savePaidBillInvoice(null, billId, paymentType, clientId);
                // close payment activity
                Intent intent = new Intent(PaymentActivity.this, Operative.class);
                int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                String username = intentPay.getStringExtra("username");
                int isAdmin = intentPay.getIntExtra("isAdmin", -1);

                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.setAction("billPaid");
                intent.putExtra("billId", -1);
                intent.putExtra("orderNumber", (orderNumber));
                startActivity(intent);
                finish();

                try {
                    PaymentActivity.this.finish();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

        else
        {
            if (StaticValue.blackbox)
            {
                f = true;
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
                params.add(new BasicNameValuePair("clientId", String.valueOf(clientId)));
                Gson gson = new Gson();
                SubdivisionItemJson myJson = new SubdivisionItemJson(item);
                String myItem = gson.toJson(myJson);
                params.add(new BasicNameValuePair("item", myItem));
                callHttpHandler("/savePaidBillItemInvoice", params);
            }

            else
            {
                f = dbA.savePaidBillInvoice(item, billId, paymentType, clientId);
                if (item != null)
                    { item.setPaid(true); }
            }
        }
        return f;
    }

    public void savePaidSubdivisionBill(SubdivisionItem item, int paymentType){
        int billId = intentPay.getIntExtra("billId", -1);
        dbA.savePaidBill(item,billId, paymentType);
    }

    public boolean savePaidBill(int paymentType){
        int billId = intentPay.getIntExtra("billId", -1);
        SubdivisionItem item = orderFragment.getOrderListAdapter().getSubdivisionItem();
        calculatorFragment.resetActualCredit();
        if(orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()>0)
            orderSubdivisionFragment.getSubdivisionAdapter().setOpenSplitPaid();
        /**
         * If pay mode is set to PAY_TOTAL_BILL or if the bill splits are all paid, it
         * updates the total bill db value setting it to paid on the current timestamp.
         * If not, it saves the paid bill split data for the next time.
         */
        boolean f;
        Float delta = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost();

        if( roundDecimal(delta, 2) <= 0 || pay_mode == PAY_TOTAL_BILL){
            if(StaticValue.blackbox){
                f = true;
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
                callHttpHandler("/savePaidBill", params);
            }else {
                f = dbA.savePaidBill(null, billId, paymentType);
                // close payment activity
                Intent intent = new Intent(PaymentActivity.this, Operative.class);
                int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                String username = intentPay.getStringExtra("username");
                int isAdmin = intentPay.getIntExtra("isAdmin", -1);

                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.setAction("billPaid");
                intent.putExtra("billId", -1);
                intent.putExtra("orderNumber", -1);
                startActivity(intent);
                finish();
                try {
                    PaymentActivity.this.finish();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        else{
            if(StaticValue.blackbox){
                f = true;
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
                Gson gson = new Gson();
                SubdivisionItemJson myJson = new SubdivisionItemJson(item);
                String myItem = gson.toJson(myJson);
                params.add(new BasicNameValuePair("item", myItem));
                callHttpHandler("/savePaidBillItem", params);
            }else {
                f = dbA.savePaidBill(item, billId, paymentType);
                if (item != null)
                    item.setPaid(true);
            }


        }
        return f;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ClientInfo client;
        switch(requestCode){ // see startActivityForResult comment for codes meanings.
            case 1001:
                if(resultCode == RESULT_OK){
                    client = TemporaryOrder.getClient();
                    /**
                     * Checking if client selected is actually connected to a company
                     */
                    if(client!=null){
                        if(client.getCompany_id() != -1){

                            //pd.printInvoice(client, IP);
                        }
                        else Toast.makeText(this, R.string.client_has_no_company, Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(this, R.string.null_client, Toast.LENGTH_SHORT).show();
                }
                else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, R.string.invoice_aborted, Toast.LENGTH_SHORT).show();
                }
                break;
            case 1002:
                if(resultCode == RESULT_OK){
                    client = TemporaryOrder.getClient();
                    /**
                     *  Checking if client has emai
                     */
                    if(client!=null){
                        if(client.getEmail().equals(""))
                            Toast.makeText(this,R.string.client_has_no_email, Toast.LENGTH_SHORT).show();
                        else{
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            String mailto = "mailto:" + client.getEmail()+
                                    "?subject="+ Uri.encode("Scontrino Burgheria")+
                                    "&body="+ Uri.encode("corpo del messaggio contenente lo scontrino o fattura");
                            emailIntent.setData(Uri.parse(mailto));

                            try{
                                startActivity(emailIntent);
                                finish();
                            }
                            catch(ActivityNotFoundException e){
                                e.printStackTrace();
                                Toast.makeText(this, R.string.no_application_found_for_sending_emails, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    else Toast.makeText(this, R.string.null_client, Toast.LENGTH_SHORT).show();
                }
                else if(resultCode == RESULT_CANCELED){

                }
                break;
            default:
                break;
        }
    }


    /**
     * PINPAD for authorization from options fragment
     * code rapprsesen wich operation i'm trying to invoke
     * 1=homage
     * 2=discount
     */

    public void openPinpad(int code){
            calculatorFragment.turnOffCalculator();
            LayoutInflater layoutInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.activity_pinpad, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            popupView.post(new Runnable() {
                @Override
                public void run() {
                    passcode = "";
                    setUpPinpadPopup(popupView, popupWindow, code);

                }
            });

            popupWindow.setFocusable(true);
            popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
       // }
    }

    private void setUpPinpadPopup(View popupView, PopupWindow popupWindow, int code) {
        setupDigits(popupView, code, popupWindow);
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                optionsFragment.setButtonPermission();
                resetDigitPinpads(popupView);
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setupDigits(View popupView, int code, PopupWindow popupWindow){
        RelativeLayout digitContainer = (RelativeLayout) popupView.findViewById(R.id.digits_container);
        View v;
        for(int i = 0; i < digitContainer.getChildCount(); i++){
            v = digitContainer.getChildAt(i);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    char digit = (((CustomButton)v).getText().charAt(0));
                    setDigitsPinpad(digit, popupView, code, popupWindow);
                }
            });
        }

    }

    public void resetDigitPinpads(View popupView){
        popupView.findViewById(R.id.first_d).setBackgroundColor(black);
        popupView.findViewById(R.id.second_d).setBackgroundColor(black);
        popupView.findViewById(R.id.third_d).setBackgroundColor(black);
        popupView.findViewById(R.id.fourth_d).setBackgroundColor(black);
        popupView.findViewById(R.id.fifth_d).setBackgroundColor(black);
        popupView.findViewById(R.id.sixth_d).setBackgroundColor(black);

    }

    private void setDigitsPinpad(char digit, View popupView, int code, PopupWindow popupWindow){

        int stringSize = passcode.length();
        if(stringSize<6){
            passcode += digit;
            switch(stringSize){
                case 0 : {
                    popupView.findViewById(R.id.first_d).setBackgroundColor(red);
                    break;
                }
                case 1 : {
                    popupView.findViewById(R.id.second_d).setBackgroundColor(red);
                    break;
                }
                case 2 :{
                    popupView.findViewById(R.id.third_d).setBackgroundColor(red);
                    break;
                }
                case 3 :{
                    popupView.findViewById(R.id.fourth_d).setBackgroundColor(red);
                    break;
                }
                case 4 :{
                    popupView.findViewById(R.id.fifth_d).setBackgroundColor(red);
                    break;
                }
                case 5 :{
                    popupView.findViewById(R.id.sixth_d).setBackgroundColor(red);
                    if(code==1) {

                        if (dbA.checkIfPasscodeExists(passcode)) {
                            hidenBlueButtonExHomage();
                            hidePaymentButton();
                            hideSplitButton();
                            popupWindow.dismiss();
                            discountSet = false;

                            mode = HOMAGE_MODE;
                            orderFragment.activateSelectionMode(OrderListAdapter.HOMAGE_MODE);
                            orderSubdivisionFragment.setMode(OrderListAdapter.HOMAGE_MODE);

                            int lightBlue = Color.parseColor("#05a8c0");
                            findViewById(R.id.discount_button).setBackgroundColor(lightBlue);
                            findViewById(R.id.homage_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white);

                        } else {
                            Toast.makeText(me, R.string.please_insert_your_supervisor_passcode, Toast.LENGTH_SHORT).show();
                            resetDigitPinpads(popupView);
                            passcode = new String();
                        }
                    }else{
                        if (dbA.checkIfPasscodeExists(passcode)) {
                            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                            if(item == null)
                                item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                            if(item.isHomage()){
                                hidenBlueButtonExDiscount();
                                hidePaymentButton();
                                hideSplitButton();
                                popupWindow.dismiss();
                                //homageSet = true;
                                orderFragment.setTotalCostHomage1();
                            }else {

                                hidenBlueButtonExDiscount();
                                hidePaymentButton();
                                hideSplitButton();
                                popupWindow.dismiss();

                                discountSet = true;
                                mode = DISCOUNT_MODE;
                                orderFragment.activateSelectionMode(OrderListAdapter.DISCOUNT_MODE);
                                orderSubdivisionFragment.setMode(OrderListAdapter.DISCOUNT_MODE);
                                orderFragment.setTotalCostDiscount();

                                int lightBlue = Color.parseColor("#05a8c0");
                                findViewById(R.id.homage_button).setBackgroundColor(lightBlue);
                            }
                            findViewById(R.id.discount_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white);

                        } else {
                            Toast.makeText(me, R.string.please_insert_your_supervisor_passcode, Toast.LENGTH_SHORT).show();
                            resetDigitPinpads(popupView);
                            passcode = new String();
                        }
                    }

                    break;
                }
                default : {
                    break;
                }
            }
        }
    }

    private void setupDigitsModifyDiscount(View popupView, int code, PopupWindow popupWindow, View view, int groupPosition, int click){
        RelativeLayout digitContainer = (RelativeLayout) popupView.findViewById(R.id.digits_container);
        View v;
        for(int i = 0; i < digitContainer.getChildCount(); i++){
            v = digitContainer.getChildAt(i);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    char digit = (((CustomButton)v).getText().charAt(0));
                    setDigitsPinpadModifyDiscount(digit, popupView, code, popupWindow,  view, groupPosition, click);


                }
            });
        }

    }

    private void setDigitsPinpadModifyDiscount(char digit, View popupView, int code, PopupWindow popupWindow, View v, int groupPosition, int click){

        int stringSize = passcode.length();
        if(stringSize<6){
            passcode += digit;
            switch(stringSize){
                case 0 : {
                    popupView.findViewById(R.id.first_d).setBackgroundColor(red);
                    break;
                }
                case 1 : {
                    popupView.findViewById(R.id.second_d).setBackgroundColor(red);
                    break;
                }
                case 2 :{
                    popupView.findViewById(R.id.third_d).setBackgroundColor(red);
                    break;
                }
                case 3 :{
                    popupView.findViewById(R.id.fourth_d).setBackgroundColor(red);
                    break;
                }
                case 4 :{
                    popupView.findViewById(R.id.fifth_d).setBackgroundColor(red);
                    break;
                }
                case 5 :{
                    popupView.findViewById(R.id.sixth_d).setBackgroundColor(red);
                        if (dbA.checkIfPasscodeExists(passcode)) {
                            hidenBlueButtonExDiscount();
                            hidePaymentButton();
                            hideSplitButton();
                            popupWindow.dismiss();

                            discountSet = true;
                            mode = MODIFY_DISCOUNT_MODE;
                            orderFragment.activateSelectionMode(OrderListAdapter.MODIFY_DISCOUNT_MODE);
                            orderSubdivisionFragment.setMode(OrderListAdapter.MODIFY_DISCOUNT_MODE);
                            orderFragment.setTotalCostDiscount();

                            int lightBlue = Color.parseColor("#05a8c0");
                            findViewById(R.id.homage_button).setBackgroundColor(lightBlue);

                            findViewById(R.id.discount_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white);

                            orderFragment.setMode(16);
                            doSomething(v, groupPosition, click);

                        } else {
                            Toast.makeText(me, R.string.please_insert_your_supervisor_passcode, Toast.LENGTH_SHORT).show();
                            resetDigitPinpads(popupView);
                            passcode = new String();
                        }


                    break;
                }
                default : {
                    break;
                }
            }
        }
    }

    public void exitHomageMode() {
        discountSet = false;
        mode = DEFAULT_MODE;
        resetOptionsButton();
        setNormalKillOkButton();
        optionsFragment.setButtonPermission();
        activatePaymentButtons();
        setOptionButton(true);
    }

    public void exitHomageModeForTotal() {
        discountSet = false;
        mode = DEFAULT_MODE;
        resetOptionsButton();
        hidenBlueButtonForTotalHomage();
        setNormalKillOkButton();
        hidenBlueButtonForTotalHomage();
        hideSplitButton();
        optionsFragment.setButtonPermissionForTotalHmage();
        setOptionButton(false);
    }

    public void setCalculatorCost(String cost){
        calculatorFragment.setCost(cost);
    }

    /**
     * DISCOUNT PART
     */
    public void closeSetDiscount(){
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        showPaymentButton();
        showSplitButton();
        showBlueButtonExDiscount();
        optionsFragment.setButtonPermission();
        orderFragment.setMode(DEFAULT_MODE);
        calculatorFragment.setPayementShortcut();
        activatePaymentButtons();
        setNormalKillOkButton();
        mode = DEFAULT_MODE;
        if(item!=null){
            if(item.getMode()==-1){
                if(orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()==1)
                orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
            }
        }else {
          orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
        }
        discountSet = false;
        orderFragment.discountSet = false;
        calculatorFragment.setIsActive(false);
        calculatorFragment.setTmpPositionDiscount(-1);
        orderFragment.getOrderListAdapter().notifyDataSetChanged();
        dbA.showData("bill_total_extra");
        dbA.showData("product_bill");
    }

    public void setDiscountAmount(float discountAmount, float discountValue, boolean reset, int groupPosition, boolean fidelity){
        if(reset ){
            if(discountValue==0.0f) {
                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();

                if(item!=null){
                    if(item.getMode()==-1){
                        float oldDiscount = dbA.getProductBillDiscount(getBillId(), groupPosition);
                        orderFragment.resetPartialTotalDiscountAmount(oldDiscount, discountValue);
                        if (groupPosition != -1) {
                            orderFragment.getOrderListAdapter().resetDiscountElement(groupPosition, discountValue);
                            if(StaticValue.blackbox){
                                int myBillId = getBillId();
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                                params.add(new BasicNameValuePair("position", String.valueOf(groupPosition)));
                                callHttpHandler("/resetDiscountForElement", params);
                            }else {
                                int productBillId = dbA.getBillProduct(getBillId(), groupPosition);
                                dbA.updateProductBillDiscount(discountValue, productBillId);
                            }
                        }
                        if (orderFragment.getOrderListAdapter().isSplit()) {
                            orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getOrderListAdapter().getPartial_cost());
                        } else {
                            orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getTotalCost());
                        }

                        calculatorFragment.turnOnOffCalculator();

                        float remain = orderFragment.returnRemaningTotal();
                        String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
                        calculatorFragment.setCost(txt);

                        orderSubdivisionFragment.getSubdivisionAdapter().resetDiscount(item, discountAmount);
                        orderSubdivisionFragment.getSubdivisionAdapter().notifyChange();

                    }else{

                        orderFragment.setItemDiscountAmount(item ,discountAmount, discountValue, reset, groupPosition, fidelity);
                        orderFragment.setDiscountElementFromFragment(groupPosition,discountValue, reset);
                        if (groupPosition != -1) {
                            if(StaticValue.blackbox){
                                int myBillId = getBillId();
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                                params.add(new BasicNameValuePair("position", String.valueOf(groupPosition)));
                                callHttpHandler("/resetDiscountForElement", params);
                            }else {
                                int positionToUpdate = orderFragment.getOrderListAdapter().returnOriginalPosition(groupPosition);
                                int productBillId = dbA.getBillProduct(getBillId(), positionToUpdate);
                                dbA.updateProductBillDiscount(discountValue, productBillId);
                            }

                        }
                        calculatorFragment.turnOnOffCalculator();

                        float itemsDiscount = 0.0f;
                        ArrayList<CashButtonLayout> products = item.getItems();
                        for (int i = 0; i < products.size(); i++) {
                            if (!products.get(i).isSelected() && products.get(i).getPosition()!=groupPosition)
                                itemsDiscount += products.get(i).getDiscount();
                        }
                        float remain = item.getOwed_money()-item.getDiscount()-itemsDiscount;
                        String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
                        calculatorFragment.setCost(txt);

                        orderSubdivisionFragment.getSubdivisionAdapter().notifyChange();

                    }
                }else{
                    float oldDiscount = dbA.getProductBillDiscount(getBillId(), groupPosition);
                    orderFragment.resetPartialTotalDiscountAmount(oldDiscount, discountValue);
                    if (groupPosition != -1) {
                        orderFragment.getOrderListAdapter().resetDiscountElement(groupPosition, discountValue);
                        if(StaticValue.blackbox){
                            int myBillId = getBillId();
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                            params.add(new BasicNameValuePair("position", String.valueOf(groupPosition)));
                            callHttpHandler("/resetDiscountForElement", params);
                        }else {
                            int productBillId = dbA.getBillProduct(getBillId(), groupPosition);
                            dbA.updateProductBillDiscount(discountValue, productBillId);
                        }
                    }
                    if (orderFragment.getOrderListAdapter().isSplit()) {
                        orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getOrderListAdapter().getPartial_cost());
                    } else {
                        float a = orderFragment.getTotalCost();
                        orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getTotalCost());
                    }

                    calculatorFragment.turnOnOffCalculator();

                    float remain = orderFragment.returnRemaningTotal();
                    String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
                    calculatorFragment.setCost(txt);
                }


            }else{
                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                if(item!=null){
                    if(item.getMode()==-1){
                        //HO FATTO LO SPLIT MA SETTO IL DISCOUNT SUL TOTAL

                        if(groupPosition==-1) {
                            orderFragment.setNewPartialTotalDiscountAmount(discountAmount, discountValue);
                            setDiscountOnTotalBill(discountAmount, discountValue, groupPosition, reset);
                        }else{
                            float other  =orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCostForTotal();

                            float remain = item.getOwed_money()-item.getDiscount()-getDiscountForItemSelected(item)/*getDiscountForItem()*/-getHomageForItem()/*-other*/;

                            remain = remain-calculatorFragment.getActualCredit();
                            orderFragment.setRemainingPercentageCost(remain);

                            orderFragment.setModifyPartialTotalDiscountAmount(discountAmount, discountValue);

                            setDiscountOnTotalBill( discountAmount, discountValue, groupPosition, reset);
                            if (groupPosition != -1) {
                                if(StaticValue.blackbox){
                                    int myBillId = getBillId();
                                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                    params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                                    params.add(new BasicNameValuePair("position", String.valueOf(groupPosition)));
                                    params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount + discountValue)));
                                    callHttpHandler("/saveDiscountForElement", params);
                                }else {
                                    int productBillId = dbA.getBillProduct(getBillId(), groupPosition);
                                    dbA.updateProductBillDiscount(discountAmount + discountValue, productBillId);
                                }

                            }
                        }
                    }else{
                        //HO FATTO LO SPLIT
                        orderFragment.setDiscountElementFromFragment(groupPosition,discountValue, reset);
                        if (groupPosition != -1) {
                            if(StaticValue.blackbox){
                                int positionToUpdate = orderFragment.getOrderListAdapter().returnOriginalPosition(groupPosition);
                                int myBillId = getBillId();
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                                params.add(new BasicNameValuePair("position", String.valueOf(positionToUpdate)));
                                params.add(new BasicNameValuePair("discount", String.valueOf(discountValue)));
                                callHttpHandler("/saveDiscountForElement", params);
                            }else {
                                int positionToUpdate = orderFragment.getOrderListAdapter().returnOriginalPosition(groupPosition);
                                int productBillId = dbA.getBillProduct(getBillId(), positionToUpdate);
                                dbA.updateProductBillDiscount(discountValue, productBillId);
                            }
                        }
                        orderFragment.setItemDiscountAmount(item ,discountAmount, discountValue, reset, groupPosition, fidelity);
                        calculatorFragment.turnOnOffCalculator();
                        float itemsDiscount = 0.0f;
                        ArrayList<CashButtonLayout> products = item.getItems();
                        for (int i = 0; i < products.size(); i++) {
                            if(!products.get(i).isSelected())
                                itemsDiscount += products.get(i).getDiscount();
                        }
                        float remain = item.getOwed_money()-item.getDiscount()-itemsDiscount;
                        String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
                        calculatorFragment.setCost(txt);

                        orderSubdivisionFragment.getSubdivisionAdapter().notifyChange();
                    }
                }else{
                    orderFragment.setModifyPartialTotalDiscountAmount(discountAmount, discountValue);
                    setDiscountOnTotalBill( discountAmount, discountValue, groupPosition, reset);
                    if (groupPosition != -1) {
                        if(StaticValue.blackbox){
                            int myBillId = getBillId();
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                            params.add(new BasicNameValuePair("position", String.valueOf(groupPosition)));
                            params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount + discountValue)));
                            callHttpHandler("/saveDiscountForElement", params);
                        }else {
                            int productBillId = dbA.getBillProduct(getBillId(), groupPosition);
                            dbA.updateProductBillDiscount(discountAmount + discountValue, productBillId);
                        }
                    }
                }
            }
        }else {
            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
            if(item!=null){
                if(item.getMode()==-1){
                    //HO FATTO LO SPLIT MA SETTO IL DISCOUNT SUL TOTAL
                    if(orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()>=2){
                        orderFragment.setDiscountElementFromFragment(groupPosition,discountValue, reset);
                        if (groupPosition != -1) {
                            if(StaticValue.blackbox) {
                                int myBillId = getBillId();
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                                params.add(new BasicNameValuePair("position", String.valueOf(groupPosition)));
                                params.add(new BasicNameValuePair("discount", String.valueOf(discountValue)));
                                callHttpHandler("/saveDiscountForElement", params);

                            }else {
                                int productBillId = dbA.getBillProduct(getBillId(), groupPosition);
                                dbA.updateProductBillDiscount(discountValue, productBillId);
                            }
                        }
                        orderFragment.setItemDiscountAmount(item ,discountAmount, discountValue, reset, groupPosition, fidelity);
                        calculatorFragment.turnOnOffCalculator();

                        float itemsDiscount = 0.0f;
                        ArrayList<CashButtonLayout> products = item.getItems();
                        for (int i = 0; i < products.size(); i++) {
                            if (!products.get(i).isSelected() && products.get(i).getPosition()!=groupPosition)
                                itemsDiscount += products.get(i).getDiscount();
                        }
                        float rem= getRemainingForTotal();
                        String txt = String.format("%.2f", item.getOwed_money()-item.getDiscount()-rem-discountAmount-itemsDiscount);

                        calculatorFragment.setCost(txt);

                        orderSubdivisionFragment.getSubdivisionAdapter().notifyChange();
                    }else {
                        orderFragment.setPartialTotalDiscountAmount(discountAmount, discountValue);
                        setDiscountOnTotalBill(discountAmount, discountValue, groupPosition, reset);
                    }
                }else{
                    //HO FATTO LO SPLIT
                    orderFragment.setDiscountElementFromFragment(groupPosition,discountValue, reset);
                    if (groupPosition != -1) {
                        if(StaticValue.blackbox){
                            int positionToUpdate = orderFragment.getOrderListAdapter().returnOriginalPosition(groupPosition);
                            int myBillId = getBillId();
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                            params.add(new BasicNameValuePair("position", String.valueOf(positionToUpdate)));
                            params.add(new BasicNameValuePair("discount", String.valueOf(discountValue)));
                            callHttpHandler("/saveDiscountForElement", params);
                        }else {
                            int positionToUpdate = orderSubdivisionFragment.getSubdivisionAdapter().returnOriginalPosition(groupPosition);
                            int productBillId = dbA.getBillProduct(getBillId(), positionToUpdate);
                            dbA.updateProductBillDiscount(discountValue, productBillId);
                        }
                    }
                    orderFragment.setItemDiscountAmount(item ,discountAmount, discountValue, reset, groupPosition, fidelity);
                    calculatorFragment.turnOnOffCalculator();
                    float itemsDiscount = 0.0f;
                    ArrayList<CashButtonLayout> products = item.getItems();
                    for (int i = 0; i < products.size(); i++) {
                        if (!products.get(i).isSelected() && products.get(i).getPosition()!=groupPosition)
                            itemsDiscount += products.get(i).getDiscount();
                    }
                    float remain = item.getOwed_money()-item.getDiscount()-discountValue-itemsDiscount;
                    String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
                    calculatorFragment.setCost(txt);

                    orderSubdivisionFragment.getSubdivisionAdapter().notifyChange();
                }
            }else{
                //NON HO FATTO LO SPLIT APPLICO AL TOTALE
                if(String.valueOf(discountValue).equals("GRATI"))
                    orderFragment.setHomageMethod(groupPosition);
                else {
                    orderFragment.setPartialTotalDiscountAmount(discountAmount, discountValue);
                    setDiscountOnTotalBill(discountAmount, discountValue, groupPosition, reset);
                }
                hideSplitButton();
            }
        }
    }

    public void setDiscountOnTotalBill(float discountAmount, float discountValue, int groupPosition, boolean reset){
        orderFragment.setDiscountElementFromFragment(groupPosition,discountValue, reset);
        if (groupPosition != -1) {
            if(StaticValue.blackbox){
                int positionToUpdate = orderFragment.getOrderListAdapter().returnOriginalPosition(groupPosition);
                int myBillId = getBillId();
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("billId", String.valueOf(myBillId)));
                params.add(new BasicNameValuePair("position", String.valueOf(positionToUpdate)));
                params.add(new BasicNameValuePair("discount", String.valueOf(discountValue)));
                callHttpHandler("/saveDiscountForElement", params);
            }else {
                int positionToUpdate = orderFragment.getOrderListAdapter().returnOriginalPosition(groupPosition);
                int productBillId = dbA.getBillProduct(getBillId(), positionToUpdate);
                float oldDiscount = dbA.getBillProductDiscount(getBillId(), positionToUpdate);
                dbA.updateProductBillDiscount(discountValue + oldDiscount, productBillId);
            }
        }
        float tc1 = orderFragment.getOrderListAdapter().getPartial_cost();
        float tc = orderFragment.getTotalCost();
        if (orderFragment.getOrderListAdapter().isSplit()) {
            orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getOrderListAdapter().getPartial_cost());
        } else {
            orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getTotalCost());
        }
        calculatorFragment.turnOnOffCalculator();
        float remain = orderFragment.getOrderListAdapter().getLeftCost();
        String txt = String.format("%.2f", roundDecimal((remain), 2));//;.replaceAll(",", ".");
        calculatorFragment.setCost(txt);
    }

    public void setTotalDiscountAmount(float discountAmount, float discountValue, boolean reset, boolean fidelity){
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        if(item!=null) {
            if (item.getMode() == -1) {
                if(orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()>=2){
                    orderFragment.setItemDiscountAmount(item ,discountAmount, discountValue, reset, -1, fidelity);
                    calculatorFragment.turnOnOffCalculator();

                    float otherRemain = 0.0f;
                    otherRemain = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCostForTotal();
                    float itemsDiscount = 0.0f;
                    float itemsHomage = 0.0f;
                    ArrayList<CashButtonLayout> products = item.getItems();
                    for (int i = 0; i < products.size(); i++) {
                        if (!products.get(i).isSelected()) {
                            itemsDiscount += products.get(i).getDiscount();
                            if(products.get(i).getHomage()==1) itemsHomage += products.get(i).getPriceFloat();
                        }
                    }

                    float remain = item.getOwed_money()-item.getDiscount()-otherRemain-itemsDiscount-itemsHomage;
                    String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
                    calculatorFragment.setCost(txt);

                    orderSubdivisionFragment.getSubdivisionAdapter().notifyChange();

                    RelativeLayout container = (RelativeLayout) findViewById(R.id.second_bottom_container);
                    RelativeLayout discount = (RelativeLayout) findViewById(R.id.euro_icon_discount);
                    RelativeLayout nodiscount = (RelativeLayout) findViewById(R.id.euro_icon_no_discount);
                    if(discountValue==0.0f){
                        container.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                        discount.setVisibility(View.GONE);
                        nodiscount.setVisibility(View.VISIBLE);
                    }else {
                        if(fidelity){
                            container.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.fidelity));
                            discount.setVisibility(View.VISIBLE);
                            nodiscount.setVisibility(View.GONE);
                        }else {
                            container.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eletric_blue));
                            discount.setVisibility(View.VISIBLE);
                            nodiscount.setVisibility(View.GONE);
                        }
                    }
                }else {
                    orderFragment.setTotalDiscountAmount(discountAmount, discountValue, reset, fidelity);
                    orderSubdivisionFragment.getSubdivisionAdapter().setItemDiscount(discountAmount);
                    orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getTotalCost());
                    calculatorFragment.turnOnOffCalculator();
                    mode = 0;
                    orderFragment.setMode(DEFAULT_MODE);
                    setNormalKillOkButton();
                }
            }else if(item.getMode() == 2 || item.getMode() == 3 || item.getMode() == 4){
                orderFragment.setItemDiscountAmount(item ,discountAmount, discountValue, reset, -1, fidelity);
                if(reset && discountValue==0.0f){
                    orderSubdivisionFragment.getSubdivisionAdapter().resetDiscount(item, item.getDiscount() );
                    item.setDiscount(0.0f);
                }
                float itemsDiscount = 0.0f;
                ArrayList<CashButtonLayout> products = item.getItems();
                for (int i = 0; i < products.size(); i++) {
                    if (!products.get(i).isSelected())
                        itemsDiscount += products.get(i).getDiscount();
                }
                calculatorFragment.turnOnOffCalculator();

                float remain = item.getOwed_money()-item.getDiscount()-itemsDiscount;
                String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
                calculatorFragment.setCost(txt);

                orderSubdivisionFragment.getSubdivisionAdapter().notifyChange();

                RelativeLayout container = (RelativeLayout) findViewById(R.id.second_bottom_container);
                RelativeLayout discount = (RelativeLayout) findViewById(R.id.euro_icon_discount);
                RelativeLayout nodiscount = (RelativeLayout) findViewById(R.id.euro_icon_no_discount);
                if(discountValue==0.0f){
                    container.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                    discount.setVisibility(View.GONE);
                    nodiscount.setVisibility(View.VISIBLE);
                }else {
                    if(fidelity){
                        container.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.fidelity));
                        discount.setVisibility(View.VISIBLE);
                        nodiscount.setVisibility(View.GONE);
                    }else {
                        container.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eletric_blue));
                        discount.setVisibility(View.VISIBLE);
                        nodiscount.setVisibility(View.GONE);
                    }
                }
            }
        }else{
            orderFragment.setTotalDiscountAmount(discountAmount, discountValue, reset, fidelity);

            orderSubdivisionFragment.getSubdivisionAdapter().setItemDiscount(discountAmount);
            calculatorFragment.turnOnOffCalculator();
            SubdivisionItem item2 = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
            float itemsDiscount = 0.0f;
            ArrayList<CashButtonLayout> produc = item2.getItems();
            for (int i = 0; i < produc.size(); i++) {
                if (!produc.get(i).isSelected())
                    itemsDiscount += produc.get(i).getDiscount();
            }
            float homage = 0.0f;
            for (int i = 0; i < products.size(); i++) {
                if (!products.get(i).isSelected())
                    if (products.get(i).getHomage()==1) {
                        homage += products.get(i).getPriceFloat();
                        ArrayList<CashButtonListLayout> mods = modifiers.get(products.get(i));
                        for(int j=0; j< mods.size(); j++){
                            homage += mods.get(j).getPriceFloat()*mods.get(j).getQuantityInt();
                        }
                    }
            }

            float remain = 0.0f;
            if(!reset)
                remain = item2.getOwed_money()-item2.getDiscount()-itemsDiscount-homage;
            else remain = item2.getOwed_money()-item2.getDiscount()-itemsDiscount-homage;
            String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
            calculatorFragment.setCost(txt);
            mode = 0;
            orderFragment.setMode(DEFAULT_MODE);
            setNormalKillOkButton();

        }
    }

    public void setPartialTotalDiscountAmount(float discountAmount, float discountValue){
        orderFragment.setPartialTotalDiscountAmount(discountAmount, discountValue);
        float remain = orderFragment.returnRemaningTotal();
        remain = remain-calculatorFragment.getActualCredit();
        calculatorFragment.turnOnOffCalculator();
        mode = 0;
        orderFragment.setMode(DEFAULT_MODE);
        setNormalKillOkButton();

        String txt = String.format("%.2f", roundDecimal((remain), 2));//.replaceAll(",", ".");
        calculatorFragment.setCost(txt);
        calculatorFragment.resetActualCredit();
    }


    public void setUpCalculatorShortcut(){
        calculatorFragment.setPayementShortcut();
    }

    /**
     * popup for process card from PaymentsOptionAdapter
     */
    public void openProcessCardPopupForItem(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.two_button_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams)popupView.findViewById(R.id.popup_container).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);*/

                setUpOkForConfirmPopupForItem(popupView, popupWindow);

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }

    /**
     * popup for acceppting payement with card, only kill and ok
     *
     * @param popupView
     * @param popupWindow
     */
    public void setUpOkForConfirmPopupForItem(View popupView, PopupWindow popupWindow){
        CustomTextView popupText = (CustomTextView) popupView.findViewById(R.id.popup_text);
        popupText.setText(R.string.process_the_credit_card_);

        CustomButton notAccepted = (CustomButton) popupView.findViewById(R.id.firstButton);
        notAccepted .setText(R.string.not_accepted);
        notAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();

            }
        });
        CustomButton accepted = (CustomButton) popupView.findViewById(R.id.secondButton);
        accepted.setText(R.string.accepted);
        accepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                        float credit1 = getCreditValueAgain();
                        SubdivisionItem totalItem = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                        SubdivisionItem item  = orderFragment.getOrderListAdapter().getSubdivisionItem();
                        if(item==null)
                            item =  orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                        String mode = "";
                        int billId1 = intentPay.getIntExtra("billId", -1);
                        if(item.getMode()==NUMBER_MODE) {
                            mode = "Per number";
                            if(item.getOwed_money()*item.getNumber_subdivision() == totalItem.getOwed_money()){
                                int billNumber = intentPay.getIntExtra("orderNumber", -1);

                                if(StaticValue.blackbox){
                                    Map<String,ArrayList<CashButtonListLayout>> test =
                                            new HashMap<String,ArrayList<CashButtonListLayout>>();
                                    for(int i =0; i<products.size(); i++){
                                        test.put(String.valueOf(products.get(i).getPosition()), modifiers.get(products.get(i)));
                                    }

                                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                    Gson gson = new Gson();
                                    String prods = gson.toJson(products);
                                    String mods = gson.toJson(test);
                                    params.add(new BasicNameValuePair("products", prods));
                                    params.add(new BasicNameValuePair("modifiers", mods));
                                    params.add(new BasicNameValuePair("printType", String.valueOf(1)));
                                    params.add(new BasicNameValuePair("billId", String.valueOf(billId1)));
                                    params.add(new BasicNameValuePair("paymentType", String.valueOf(4)));
                                    params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
                                    params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
                                    params.add(new BasicNameValuePair("paid", String.valueOf(orderFragment.getOrderListAdapter().getTotal_cost() - credit1)));
                                    params.add(new BasicNameValuePair("cost", String.valueOf(orderFragment.getOrderListAdapter().getTotal_cost())));
                                    params.add(new BasicNameValuePair("credit", String.valueOf(0.0)));
                                    params.add(new BasicNameValuePair("creditL", String.valueOf(0.0)));

                                    callHttpHandler("/printBill", params);
                                }else {

                                    if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                                        PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                        ditron.closeAll();
                                        ditron.startSocket();
                                    }
                                    ClientThread myThread = ClientThread.getInstance();
                                    myThread.setProducts(products);
                                    myThread.setModifiers(modifiers);
                                    myThread.setPrintType(1);
                                    myThread.setBillId(String.valueOf(billId1));
                                    myThread.setDeviceName(deviceName);
                                    myThread.setOrderNumber(String.valueOf(billNumber));
                                    myThread.setCost(orderFragment.getOrderListAdapter().getTotal_cost());
                                    myThread.setPaid(orderFragment.getOrderListAdapter().getTotal_cost() - credit1);
                                    myThread.setPaymentType(4);
                                    myThread.setCredit(0.0f);
                                    myThread.setCreditL(0.0f);
                                    myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                    myThread.delegate = forClient;
                                    myThread.setClientThread();
                                    myThread.setRunBaby(true);
                                }
                                orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(item.getOwed_money()*item.getNumber_subdivision());

                                savePaidBill(4);
                                orderSubdivisionFragment.showSubdivisionItem(1);
                            }else{
                                Gson gson = new Gson();
                                JSONObject combined = new JSONObject();

                                int billNumber = intentPay.getIntExtra("orderNumber", -1);

                                if(StaticValue.blackbox){
                                    Map<String,ArrayList<CashButtonListLayout>> test =
                                            new HashMap<String,ArrayList<CashButtonListLayout>>();
                                    for(int i =0; i<products.size(); i++){
                                        test.put(String.valueOf(products.get(i).getPosition()), modifiers.get(products.get(i)));
                                    }

                                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                    params.add(new BasicNameValuePair("printType", String.valueOf(3)));
                                    params.add(new BasicNameValuePair("billId", String.valueOf(billId1)));
                                    params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
                                    params.add(new BasicNameValuePair("orderNumber", String.valueOf(billNumber)));
                                    params.add(new BasicNameValuePair("paid", String.valueOf(orderFragment.getOrderListAdapter().getTotal_cost() - credit1)));
                                    params.add(new BasicNameValuePair("cost", String.valueOf(orderFragment.getOrderListAdapter().getTotal_cost())));
                                    params.add(new BasicNameValuePair("paymentType", String.valueOf(4)));
                                    params.add(new BasicNameValuePair("description", mode));
                                    params.add(new BasicNameValuePair("quantity", String.valueOf(item.getNumber_subdivision())));

                                    callHttpHandler("/printBillPartial", params);
                                }else {

                                    if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                                        PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                        ditron.closeAll();
                                        ditron.startSocket();
                                    }
                                    ClientThread myThread = ClientThread.getInstance();
                                    myThread.setPrintType(3);
                                    myThread.setBillId(String.valueOf(billId1));
                                    myThread.setDeviceName(deviceName);
                                    myThread.setOrderNumberBill(String.valueOf(billNumber));
                                    myThread.setCost(orderFragment.getOrderListAdapter().getTotal_cost());
                                    myThread.setPaid(orderFragment.getOrderListAdapter().getTotal_cost() - credit1);
                                    myThread.setPaymentType(4);
                                    myThread.setDescription(mode);
                                    myThread.setQuantity(item.getNumber_subdivision());
                                    myThread.delegate = forClient;
                                    myThread.setClientThread();
                                    myThread.setRunBaby(true);
                                }

                                orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(item.getOwed_money()*item.getNumber_subdivision());
                                if (orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost() <= 0) {

                                    Collections.sort(products);
                                    combined.put("products", gson.toJson(products));
                                    Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization()
                                                    .setPrettyPrinting().create();
                                    combined.put("modifiers", gson1.toJson(modifiers));
                                    combined.put("billId", billId1);
                                    combined.put("printType", 4);
                                    combined.put("IP", IP);
                                    combined.put("deviceName", deviceName);
                                    combined.put("orderNumber", billNumber);
                                    combined.put("roomName", orderFragment.getRoomName());

                                    //myThread.addJsonString(combined.toString());

                                }
                                savePaidBill(4);
                                orderSubdivisionFragment.showSubdivisionItem(1);
                            }

                        }else {
                            if (item.getMode() == PERCENTAGE_MODE) {
                                mode = "Per Amount";

                                JSONObject combined = new JSONObject();

                                int billNumber = intentPay.getIntExtra("orderNumber", -1);

                                if(StaticValue.printerName.equals("ditron") && StaticValue.ditronApi){
                                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                    ditron.closeAll();
                                    ditron.startSocket();
                                }
                                ClientThread myThread = ClientThread.getInstance();
                                myThread.setPrintType(3);
                                myThread.setBillId(String.valueOf(billId1));
                                myThread.setDeviceName(deviceName);
                                myThread.setOrderNumberBill(String.valueOf(billNumber));
                                myThread.setCost(item.getOwed_money());
                                myThread.setPaid(item.getOwed_money()-credit1);
                                myThread.setPaymentType(4);
                                myThread.setDescription(mode);
                                myThread.setQuantity(item.getNumber_subdivision());
                                myThread.delegate = forClient;
                                myThread.setClientThread();
                                myThread.setRunBaby(true);


                            } else if (item.getMode() == 2 || item.getMode() == 3 || item.getMode() == -1) {
                                printItemBill(item, item.getOwed_money(), item.getOwed_money(), 4);

                            }
                            orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(item.getOwed_money());
                            orderSubdivisionFragment.getSubdivisionAdapter().setItemPaid(item);

                            savePaidBill(4);
                            SubdivisionItem totalitem = orderSubdivisionFragment.getSubdivisionAdapter()
                                    .getTotalItem();
                            if (totalitem.getOwed_money() == 0.0f) {
                                orderSubdivisionFragment.getSubdivisionAdapter()
                                        .showFirstItemAvaiable();

                            } else {
                                orderSubdivisionFragment.getSubdivisionAdapter()
                                        .showItemOriginal();
                            }
                            orderFragment.setRemainingPercentageCost(calculatorFragment.getCost());
                            calculatorFragment.setPressedTime(0);
                            setNormalKillOkButton();
                            calculatorFragment.setMode(4);
                            calculatorFragment.turnOnOffCalculator();
                            // reset payment options buttons to original state.
                            optionsFragment.getAdapter().loadButtons(0);
                            //da cambiare controllando gli altri split bill
                            resetOpacityForSplit();
                        }
                    popupWindow.dismiss();
                    optionsFragment.getAdapter().setActive(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.processing_receipt_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * popup for process card from PaymentsOptionAdapter
     */
    public void openProcessCardPopup(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.two_button_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {

                setUpOkForConfirmPopup(popupView, popupWindow);

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }

    /**
     * popup for acceppting payement with card, only kill and ok
     *
     * @param popupView
     * @param popupWindow
     */
    public void setUpOkForConfirmPopup(View popupView, PopupWindow popupWindow){
        CustomTextView popupText = (CustomTextView) popupView.findViewById(R.id.popup_text);
        popupText.setText(R.string.process_the_credit_card_);

        CustomButton notAccepted = (CustomButton) popupView.findViewById(R.id.firstButton);
        notAccepted .setText(R.string.not_accepted);
        notAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();

            }
        });
        CustomButton accepted = (CustomButton) popupView.findViewById(R.id.secondButton);
        accepted.setText(R.string.accepted);
        accepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                           try {
                    switch (pay_mode) {
                        case PAY_TOTAL_BILL:
                            view.setActivated(!view.isActivated());
                            int billId = intentPay.getIntExtra("billId", -1);
                            Float totalDiscount = dbA.getBillDiscountPrice(billId);
                            float credit = getCreditValueAgain();
                            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
                            float homage = 0.0f;
                            for (CashButtonLayout cashButton : products) {
                                if (cashButton.getHomage() != 0)
                                    homage += cashButton.getPriceFloat();
                                myProducts.add(cashButton);
                                int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                                myProducts.get(myProducts.size() - 1).setQuantity(qty);

                                ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                                mList = modifiers.get(cashButton);
                                if (mList != null) {
                                    Collections.sort(modifiers.get(cashButton));
                                }
                            }


                            if (invoiceBill) {
                                printInvoice(orderFragment.getOrderListAdapter().getTotal_cost(), orderFragment.getOrderListAdapter().getTotal_cost(), 0.0f, 1);

                                invoiceBill = false;
                                findViewById(R.id.invoice_button).setActivated(false);
                                findViewById(R.id.invoice_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white_press);

                                deleteCredit();
                                popupWindow.dismiss();
                            } else {

                                if (returnSubdivisionSize() == 1) {

                                        float creditToGive = getCreditValueAgain();

                                        int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                                    if(StaticValue.blackbox){
                                        Map<String,ArrayList<CashButtonListLayout>> test =
                                                new HashMap<String,ArrayList<CashButtonListLayout>>();
                                        for(int i =0; i<products.size(); i++){
                                            test.put(String.valueOf(products.get(i).getPosition()), modifiers.get(products.get(i)));
                                        }

                                        int customerId = -1;
                                        if(products.get(0).getClientPosition()>0){
                                            Customer c = orderFragment.getOrderListAdapter().getCustomer(products.get(0).getClientPosition());
                                            customerId = c.getCustomerId();
                                        }


                                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                        Gson gson = new Gson();
                                        String prods = gson.toJson(myProducts);
                                        String mods = gson.toJson(test);
                                        String creditArray = gson.toJson(creditId);
                                        params.add(new BasicNameValuePair("products", prods));
                                        params.add(new BasicNameValuePair("modifiers", mods));
                                        params.add(new BasicNameValuePair("printType", String.valueOf(1)));
                                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                                        params.add(new BasicNameValuePair("paymentType", String.valueOf(4)));
                                        params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
                                        params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
                                        params.add(new BasicNameValuePair("paid", String.valueOf(orderFragment.getOrderListAdapter().getTotal_cost() - credit)));
                                        params.add(new BasicNameValuePair("cost", String.valueOf(orderFragment.getOrderListAdapter().getTotal_cost())));
                                        params.add(new BasicNameValuePair("creditArray", creditArray));
                                        params.add(new BasicNameValuePair("creditL", String.valueOf(creditToGive)));
                                        params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                                        callHttpHandler("/printBill", params);
                                    }else {

                                        if (totalDiscount == 0.0f) {

                                            if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                                                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                                ditron.closeAll();
                                                ditron.startSocket();
                                            }
                                            ClientThread myThread = ClientThread.getInstance();
                                            myThread.setProducts(myProducts);
                                            myThread.setModifiers(modifiers);
                                            myThread.setPrintType(1);
                                            myThread.setBillId(String.valueOf(billId));
                                            myThread.setDeviceName(deviceName);
                                            myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                            myThread.setCost(orderFragment.getOrderListAdapter().getTotal_cost());
                                            myThread.setPaid(orderFragment.getOrderListAdapter().getTotal_cost() - credit);
                                            myThread.setCreditL(creditToGive);
                                            myThread.setCredit(0.0f);
                                            myThread.setPaymentType(4);
                                            myThread.delegate = forClient;
                                            myThread.setClientThread();
                                            myThread.setRunBaby(true);

                                        } else {
                                            Double costo = dbA.getBillPrice(billId);
                                            float paid = (float) (costo - totalDiscount - homage);

                                            if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                                                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                                ditron.closeAll();
                                                ditron.startSocket();
                                            }
                                            ClientThread myThread = ClientThread.getInstance();
                                            myThread.setProducts(myProducts);
                                            myThread.setModifiers(modifiers);
                                            myThread.setPrintType(2);
                                            myThread.setBillId(String.valueOf(billId));
                                            myThread.setDeviceName(deviceName);
                                            myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                            myThread.setCost(paid);
                                            myThread.setPaid(paid);
                                            myThread.setCreditL(creditToGive);
                                            myThread.setCredit(0.0f);
                                            myThread.setPaymentType(4);
                                            myThread.setTotalDiscount(totalDiscount);
                                            myThread.delegate = forClient;
                                            myThread.setClientThread();
                                            myThread.setRunBaby(true);


                                        }
                                    }

                                } else {
                                    calculatorFragment.acceptRemainingPartialCard();
                                }
                                savePaidBill(4);
                                deleteCredit();
                                popupWindow.dismiss();
                            }
                            break;
                        case PAY_PARTIAL_BILL:

                            if (orderFragment.getOrderListAdapter().getSubdivisionItem() == null) {
                                calculatorFragment.acceptPartialCard();
                                optionsFragment.getAdapter().setIsCar(false);
                            }else{
                                float credit1 = getCreditValueAgain();
                                SubdivisionItem totalItem = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                SubdivisionItem item  = orderFragment.getOrderListAdapter().getSubdivisionItem();
                                String mode = "";
                                int billId1 = intentPay.getIntExtra("billId", -1);
                                if(item.getMode()==NUMBER_MODE) {
                                    mode = "Per number";
                                    if(item.getOwed_money()*item.getNumber_subdivision() == totalItem.getOwed_money()){
                                        Gson gson = new Gson();
                                        JSONObject combined = new JSONObject();
                                        int billNumber = intentPay.getIntExtra("orderNumber", -1);
                                        if(StaticValue.printerName.equals("ditron") && StaticValue.ditronApi){
                                            PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                            ditron.closeAll();
                                            ditron.startSocket();
                                        }
                                        ClientThread myThread = ClientThread.getInstance();
                                        myThread.setProducts(products);
                                        myThread.setModifiers(modifiers);
                                        myThread.setPrintType(1);
                                        myThread.setBillId(String.valueOf(billId1));
                                        myThread.setDeviceName(deviceName);
                                        myThread.setOrderNumberBill(String.valueOf(billNumber));
                                        myThread.setCost(orderFragment.getOrderListAdapter().getTotal_cost());
                                        myThread.setPaid(orderFragment.getOrderListAdapter().getTotal_cost()-credit1);
                                        myThread.setPaymentType(4);
                                        myThread.setCredit(0.0f);
                                        myThread.delegate = forClient;
                                        myThread.setClientThread();
                                        myThread.setRunBaby(true);
                                        //myThread.addJsonString(combined.toString());


                                        orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(item.getOwed_money()*item.getNumber_subdivision());
                                        savePaidBill(4);
                                        orderSubdivisionFragment.showSubdivisionItem(1);
                                    }else{
                                        Gson gson = new Gson();
                                        JSONObject combined = new JSONObject();
                                        int billNumber = intentPay.getIntExtra("orderNumber", -1);
                                        if(StaticValue.printerName.equals("ditron") && StaticValue.ditronApi){
                                            PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                            ditron.closeAll();
                                            ditron.startSocket();
                                        }
                                        ClientThread myThread = ClientThread.getInstance();
                                        myThread.setPrintType(3);
                                        myThread.setBillId(String.valueOf(billId1));
                                        myThread.setDeviceName(deviceName);
                                        myThread.setOrderNumberBill(String.valueOf(billNumber));
                                        myThread.setCost(orderFragment.getOrderListAdapter().getTotal_cost());
                                        myThread.setPaid(orderFragment.getOrderListAdapter().getTotal_cost()-credit1);
                                        myThread.setPaymentType(4);
                                        myThread.setDescription(mode);
                                        myThread.setQuantity(item.getNumber_subdivision());
                                        myThread.delegate = forClient;
                                        myThread.setClientThread();
                                        myThread.setRunBaby(true);
                                        //myThread.addJsonString(combined.toString());


                                        orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(item.getOwed_money()*item.getNumber_subdivision());
                                        if (orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost() <= 0) {

                                            if(StaticValue.printerName.equals("ditron") && StaticValue.ditronApi){
                                                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                                ditron.closeAll();
                                                ditron.startSocket();
                                            }
                                            myThread.setProducts(products);
                                            myThread.setModifiers(modifiers);
                                            myThread.setPrintType(4);
                                            myThread.setBillId(String.valueOf(billId1));
                                            myThread.setDeviceName(deviceName);
                                            myThread.setOrderNumberBill(String.valueOf(billNumber));
                                            myThread.setRoomName(orderFragment.getRoomName());
                                            myThread.setClientThread();
                                            myThread.setRunBaby(true);
                                           // myThread.addJsonString(combined.toString());

                                        }
                                        savePaidBill(4);
                                        orderSubdivisionFragment.showSubdivisionItem(1);
                                    }

                                }else {
                                    if (item.getMode() == PERCENTAGE_MODE) {
                                        mode = "Per Amount";
                                        Gson gson = new Gson();
                                        JSONObject combined = new JSONObject();
                                       /* ClientDelegate myClient = new ClientDelegate(8080);
                                        myClient.delegate = forClient;*/
                                        int billNumber = intentPay.getIntExtra("orderNumber", -1);

                                        if(StaticValue.printerName.equals("ditron") && StaticValue.ditronApi){
                                            PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                            ditron.closeAll();
                                            ditron.startSocket();
                                        }
                                        ClientThread myThread = ClientThread.getInstance();
                                        myThread.setPrintType(3);
                                        myThread.setBillId(String.valueOf(billId1));
                                        myThread.setDeviceName(deviceName);
                                        myThread.setOrderNumberBill(String.valueOf(billNumber));
                                        myThread.setCost(item.getOwed_money());
                                        myThread.setPaid(item.getOwed_money()-credit1);
                                        myThread.setPaymentType(4);
                                        myThread.setDescription(mode);
                                        myThread.setQuantity(item.getNumber_subdivision());
                                        myThread.delegate = forClient;
                                        myThread.setClientThread();
                                        myThread.setRunBaby(true);
                                        //myThread.addJsonString(combined.toString());

                                    } else if (item.getMode() == 2 || item.getMode() == 3) {
                                        ArrayList<CashButtonLayout> a1 = orderFragment.getOrderListAdapter().showSplitBillToPrintProducts(item.getItems_map());
                                        for(int i=0; i< a1.size(); i++){
                                            a1.get(i).setPosition(i);
                                        }
                                        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> b1 = orderFragment.getOrderListAdapter().getCurrentSplitMap(a1);
                                        Gson gson = new Gson();
                                        JSONObject combined = new JSONObject();
                                       /* ClientDelegate myClient = new ClientDelegate(8080);
                                        myClient.delegate = forClient;*/
                                        int billNumber = intentPay.getIntExtra("orderNumber", -1);

                                        if(StaticValue.printerName.equals("ditron") && StaticValue.ditronApi){
                                            PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                            ditron.closeAll();
                                            ditron.startSocket();
                                        }
                                        ClientThread myThread = ClientThread.getInstance();
                                        myThread.setProducts(a1);
                                        myThread.setModifiers(b1);
                                        myThread.setPrintType(1);
                                        myThread.setBillId(String.valueOf(billId1));
                                        myThread.setDeviceName(deviceName);
                                        myThread.setOrderNumberBill(String.valueOf(billNumber));
                                        myThread.setCost(item.getOwed_money());
                                        myThread.setPaid(item.getOwed_money());
                                        myThread.setPaymentType(4);
                                        myThread.setCredit(0.0f);
                                        myThread.delegate = forClient;
                                        myThread.setClientThread();
                                        myThread.setRunBaby(true);
                                        // myThread.addJsonString(combined.toString());

                                    }
                                    orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(item.getOwed_money());
                                    orderSubdivisionFragment.getSubdivisionAdapter().setItemPaid(item);
                                    if (orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost() <= 0) {
                                        int billNumber = intentPay.getIntExtra("orderNumber", -1);
                                        Gson gson = new Gson();
                                        JSONObject combined = new JSONObject();
                                        Collections.sort(products);

                                        if(StaticValue.printerName.equals("ditron") && StaticValue.ditronApi){
                                            PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                            ditron.closeAll();
                                            ditron.startSocket();
                                        }
                                        ClientThread myThread = ClientThread.getInstance();
                                        myThread.setProducts(products);
                                        myThread.setModifiers(modifiers);
                                        myThread.setPrintType(4);
                                        myThread.setBillId(String.valueOf(billId1));
                                        myThread.setDeviceName(deviceName);
                                        myThread.setOrderNumberBill(String.valueOf(billNumber));
                                        myThread.setRoomName(orderFragment.getRoomName());
                                        myThread.delegate = forClient;
                                        myThread.setClientThread();
                                        myThread.setRunBaby(true);

                                    }
                                    savePaidBill(4);
                                    if(orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem().getOwed_money()>0.0f){
                                        orderSubdivisionFragment.showSubdivisionItem(1);
                                    }else{
                                        int position = orderSubdivisionFragment.getSubdivisionAdapter().getFirstItemAvaiablePosition();
                                        orderSubdivisionFragment.showSubdivisionItem(position);
                                    }
                                }
                            }
                            popupWindow.dismiss();
                            break;
                    }
                    optionsFragment.getAdapter().setActive(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.processing_receipt_error, Toast.LENGTH_LONG).show();
                }

            }

        });

    }

    public void openProcessLeftCreditCardPopup(SubdivisionItem item){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.two_button_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams)popupView.findViewById(R.id.popup_container).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);*/

                setUpButtonForLeftPopup(popupView, popupWindow, item);

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }

    public void setUpButtonForLeftPopup(View popupView, PopupWindow popupWindow, SubdivisionItem item){
        CustomTextView popupText = (CustomTextView) popupView.findViewById(R.id.popup_text);
        popupText.setText(R.string.card_accepted);

        CustomButton notAccepted = (CustomButton) popupView.findViewById(R.id.firstButton);
        notAccepted.setText(R.string.not_accepted);
        notAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                //torno indietro di un left payment

            }
        });
        CustomButton accepted = (CustomButton) popupView.findViewById(R.id.secondButton);
        accepted.setText(R.string.accepted);
        accepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                printLeftPayment();
                calculatorFragment.endLeftPayment();

            }


        });

    }

    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> setSplitillToPrint(ArrayList<CashButtonLayout> items){
        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map1 = orderFragment.getOrderListAdapter().getGroupsBackup();

        for (CashButtonLayout prod: items
                ) {
            CashButtonLayout p = null;
            for (CashButtonLayout p1: orderFragment.getOrderListAdapter().getGroupsProductBackup()
                    ) {
                if(prod.getID() == p1.getID()){
                    p = p1;
                    break;
                }
            }
            map.put(prod, map1.get(p));
        }

        return map;
    }


    /**set opacity for otpion button percentage/item/person/number
     *
     */

    public boolean getGreenButton(){
        return greenButton;
    }

    /**
     * opacity forr green payment button
     */
    public void setOpacityForPayementButtons(){
        RelativeLayout payementButtons= (RelativeLayout) findViewById(R.id.payment_options_container);
        View upperLine = findViewById(R.id.top_line1);
        CustomTextView title = (CustomTextView) findViewById(R.id.options_title);
        View bottomLine = findViewById(R.id.top_line2);
        if(payementButtons!=null) {
            payementButtons.setAlpha(0.15f);
            upperLine.setAlpha(0.15f);
            title.setAlpha(0.15f);
            bottomLine.setAlpha(0.15f);
        }
    }

    /**
     * reset opacity for green button
     */
    public void resetOpacityForPayementButtons(){
        RelativeLayout payementButtons= (RelativeLayout) findViewById(R.id.payment_options_container);
        View upperLine = findViewById(R.id.top_line1);
        CustomTextView title = (CustomTextView) findViewById(R.id.options_title);
        View bottomLine = findViewById(R.id.top_line2);
        if(payementButtons!=null) {
            payementButtons.setAlpha(1f);
            upperLine.setAlpha(1f);
            title.setAlpha(1f);
            bottomLine.setAlpha(1f);
        }

    }

    public void activatePaymentButtons(){
        optionsFragment.activatePayments();
    }

    public void reactivatePaymentButtons(){
        optionsFragment.reactivatePayments();
    }

    public void resetOpacityForOptionsButton(){
        RelativeLayout buttonsContainer = (RelativeLayout) findViewById(R.id.button_container);
        if(buttonsContainer!=null) {
            buttonsContainer.setAlpha(1f);
        }
    }

    public void resetOpacityForSplit(){
        RelativeLayout payementOptions = (RelativeLayout) findViewById(R.id.button_container);

        View upperLine = findViewById(R.id.bottom_hline1_button_container);
        View bottomLine = findViewById(R.id.bottom_hline2_button_container);

        if(payementOptions!=null) {
            for (int i = 0; i < payementOptions.getChildCount(); i++) {
                View child = payementOptions.getChildAt(i);
                child.setEnabled(true);
                child.setAlpha(1f);
                child.setActivated(false);

            }

            payementOptions.setAlpha(1f);
            upperLine.setAlpha(1f);
            bottomLine.setAlpha(1f);
        }
        greenButton = false;
        optionbutton = true;
    }

    /**
     * opacity for options button, homage etc....
     */
    public void resetOpacityForSlplittButton(){
        RelativeLayout payementOptions = (RelativeLayout) findViewById(R.id.options_button_container_aa);

        View upperLine = findViewById(R.id.bottom_hline1_button_container);
        View bottomLine = findViewById(R.id.bottom_hline2_button_container);

        if(payementOptions!=null) {
            for (int i = 0; i < payementOptions.getChildCount(); i++) {
                View child = payementOptions.getChildAt(i);
                child.setEnabled(true);
                child.setAlpha(1f);

            }
            View vline1 = findViewById(R.id.vline1);
            View vline2 = findViewById(R.id.vline2);
            View vline3 = findViewById(R.id.vline3);

            vline1.setAlpha(1f);
            vline2.setAlpha(1f);
            vline3.setAlpha(1f);

            payementOptions.setAlpha(1f);
            upperLine.setAlpha(1f);
            bottomLine.setAlpha(1f);
        }
        greenButton = false;
    }

    public void setOpacityForSplitButton(){

        RelativeLayout payementOptions = (RelativeLayout) findViewById(R.id.options_button_container_aa);

        payementOptions.setEnabled(false);
        for (int i = 0; i < payementOptions.getChildCount(); i++) {
            View child = payementOptions.getChildAt(i);
            child.setEnabled(false);

        }
        View upperLine = findViewById(R.id.bottom_hline1_button_container);
        View bottomLine = findViewById(R.id.bottom_hline2_button_container);
        View vline1 = findViewById(R.id.vline1);
        View vline2 = findViewById(R.id.vline2);
        View vline3 = findViewById(R.id.vline3);

        payementOptions.setAlpha(0.15f);
        upperLine.setAlpha(0.15f);
        bottomLine.setAlpha(0.15f);
        vline1.setAlpha(0.15f);
        vline2.setAlpha(0.15f);
        vline3.setAlpha(0.15f);

    }

    public void setOpacityExSplit(){

        findViewById(R.id.invoice_button).setAlpha(1.0f);
        findViewById(R.id.homage_button).setAlpha(0.15f);
        findViewById(R.id.discount_button).setAlpha(1.0f);
        findViewById(R.id.round_button).setAlpha(1.0f);
        findViewById(R.id.email_it_button).setAlpha(1.0f);
        optionsFragment.setButtonPermissionExSplit();

        setOptionButton(true);
    }

    public boolean getIsCardFromActivity(){
        return optionsFragment.getAdapter().getIsCar();
    }

    public float getCreditValue(int billId)
    {
        return dbA.getBillCreditPrice(billId);
    }

    public float getCreditValueAgain(){
        Float value = 0.0f;
        if(creditId!=null){
            for(int id : creditId) {
                value  = value+dbA.getBillCreditPrice(id);
            }
        }
        return value;
    }

    public void deleteCredit(){
        if(creditId!=null){
            for(int id : creditId) {
                dbA.deleteCredit(id);

            }
            creditId = new ArrayList<Integer>();
        }
    }

    public int returnSubdivisionSize(){
        return orderSubdivisionFragment.getSubdivisionAdapter().getItemCount();
    }


    /**
     * ROUND PART
     */
    public void setRoundDiscount(){
        int size = returnSubdivisionSize();
        if(size>1) {
            //get selected item
            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
            float other  =orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCostForTotal();
            //float remain = orderFragment.returnRemaningTotal();
            float remain = 0.0f;
            if(item.getMode()!=-1)remain = item.getOwed_money()-item.getDiscount()-getDiscountForItemSelected(item)/*getDiscountForItem()*//*-getHomageForItem()*//*-other*/;
            else remain = item.getOwed_money()-item.getDiscount()-getDiscountForItemSelected(item)/*getDiscountForItem()*/-getHomageForItem()/*-other*/;
            remain = remain-calculatorFragment.getActualCredit();

            if (remain > 0.0) {
                float perc = remain % 1;
                if (perc == 0.0f) {
                    orderFragment.setItemDiscountAmount(item, 1, 1, false, -1, false);
                    if(item==null)orderFragment.setTotalDiscountAmount(1, 1, false, false);
                    else if(item.getMode()==-1) orderFragment.setTotalDiscountAmount(1, 1, false, false);
                    else  if(/*item.getMode()==-1 ||*/ item.getMode()==PERSON_MODE ||  item.getMode()==ITEM_MODE || item.getMode()==PERCENTAGE_MODE){
                        //SHOW DISCOUNT
                        orderFragment.showDiscountContainer();
                    }
                    if(item.getMode()==-1) {
                        String txt = String.format("%.2f", roundDecimal((remain - other - 1), 2));//.replaceAll(",", ".");
                        calculatorFragment.setCost(txt);
                    }else{
                        String txt = String.format("%.2f", roundDecimal((remain - 1), 2));//.replaceAll(",", ".");
                        calculatorFragment.setCost(txt);
                    }
                    calculatorFragment.resetActualCredit();
                    mode = 0;
                    orderFragment.setMode(DEFAULT_MODE);
                    setNormalKillOkButton();
                }else{
                    orderFragment.setItemDiscountAmount(item, perc, perc, false, -1, false);
                    if(item==null)  orderFragment.setTotalDiscountAmount(perc, perc, false, false);
                    else if(item.getMode()==PERCENTAGE_MODE)   orderFragment.setTotalDiscountAmount(perc, perc, false, false);
                    else  if(item.getMode()==-1||  item.getMode()==PERSON_MODE ||  item.getMode()==ITEM_MODE){
                        //SHOW DISCOUNT
                        orderFragment.showDiscountContainer();
                    }

                    String txt = String.format("%.2f", roundDecimal((remain - perc), 2));//.replaceAll(",", ".");
                    calculatorFragment.setCost(txt);
                    calculatorFragment.resetActualCredit();
                    mode = 0;
                    orderFragment.setMode(DEFAULT_MODE);
                    setNormalKillOkButton();
                }
            }
        }else {
            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
            if(item ==null)
                item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
            float other  =orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCostForTotal();
            //float remain = orderFragment.returnRemaningTotal();

            float remain = item.getOwed_money()-item.getDiscount()-getDiscountForItemSelected(item)/*getDiscountForItem()*/-getHomageForItem()/*-other*/;

            //float remain = orderFragment.returnRemaningTotal();
            remain = remain-calculatorFragment.getActualCredit();
            orderFragment.setRemainingPercentageCost(remain);
            if (remain > 0.0) {
                float perc = remain % 1;
                if (perc == 0.0f) {
                    //numero intero

                    orderFragment.setTotalDiscountAmount(1, 1, false, false);
                    orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getTotalCost());
                    orderSubdivisionFragment.getSubdivisionAdapter().setItemDiscount(1);
                    //  calculatorFragment.turnOnOffCalculator();
                    mode = 0;
                    String txt = String.format("%.2f", roundDecimal((remain - 1), 2));//.replaceAll(",", ".");
                    calculatorFragment.setCost(txt);
                    orderFragment.setMode(DEFAULT_MODE);
                    setNormalKillOkButton();
                } else {
                    //numero in virgola mobile
                    orderFragment.setTotalDiscountAmount(perc, perc, false, false);
                    //orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(remain-perc);
                    orderSubdivisionFragment.getSubdivisionAdapter().setItemDiscount(perc);
                    //orderFragment.setTotalCost(remain-);

                   // orderSubdivisionFragment.getSubdivisionAdapter().setTotalBill(orderFragment.getTotalCost());
                    String txt = String.format("%.2f", roundDecimal((remain - perc), 2));//.replaceAll(",", ".");
                    calculatorFragment.setCost(txt);
                    //  calculatorFragment.turnOnOffCalculator();
                    mode = 0;
                    orderFragment.setMode(DEFAULT_MODE);
                    setNormalKillOkButton();
                }
            }
        }


    }


    /**
     * NON FISCAL PRINTING PART
     */

    public void printLeftPayment(){
        int billId = intentPay.getIntExtra("billId", -1);
        SubdivisionItem item = calculatorFragment.getLeftPayment().get(0).getItem();
        if(StaticValue.blackbox){
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            Gson gson = new Gson();
            SubdivisionItemJson myJson = new SubdivisionItemJson(item);
            String myItem = gson.toJson(myJson);
            String lefts = gson.toJson( calculatorFragment.getLeftPayment());
            params.add(new BasicNameValuePair("item", myItem));
            params.add(new BasicNameValuePair("left", lefts));
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("paymentType", String.valueOf( calculatorFragment.getLeftPayment().get(0).getPaymentType())));
            callHttpHandler("/savePaidBillForLeftPayment", params);

        }else {
            dbA.savePaidBillForLeftPayment(item, billId, calculatorFragment.getLeftPayment().get(0).getPaymentType());
        }
        int i = -1;
        synchronized (this) {
            Float totalDiscount = item.getDiscount();
            Double costo = Double.valueOf(item.getOwed_money());
            int orderNumber1 = intentPay.getIntExtra("orderNumber", 1);

            ArrayList<Customer> customers = new ArrayList<Customer>();

            int tableNumber = orderFragment.getTableNumber();


            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myModifier = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            for (CashButtonLayout cashButton : item.getItems()) {
                myProducts.add(cashButton);

                ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                mList = cashButton.getCashList();
                if (mList != null) {
                    if (mList.size() != 0) {
                        myModifier.put(myProducts.get(myProducts.size() - 1), mList);
                    } else {
                        // myProducts.get(myProducts.size()-1).setCashList(null);
                    }
                }
                if (cashButton.getClientPosition() > 0) {
                    Customer customer = orderFragment.getOrderListAdapter().getCustomer(cashButton.getClientPosition());
                    if (!customers.contains(customer)) customers.add(customer);
                }
            }
            Collections.sort(myProducts);


            if(StaticValue.blackbox){
                Map<String,ArrayList<CashButtonListLayout>> test =
                        new HashMap<String,ArrayList<CashButtonListLayout>>();
                for(int i1 =0; i1<myProducts.size(); i1++){
                    test.put(String.valueOf(myProducts.get(i1).getPosition()), myProducts.get(i1).getCashList());
                }


                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                Gson gson = new Gson();
                String prods = gson.toJson(myProducts);
                String mods = gson.toJson(test);
                String leftPayment = gson.toJson(calculatorFragment.getLeftPayment());
                params.add(new BasicNameValuePair("products", prods));
                params.add(new BasicNameValuePair("modifiers", mods));
                params.add(new BasicNameValuePair("left", leftPayment));
                params.add(new BasicNameValuePair("printType", String.valueOf(15)));
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("paymentType", String.valueOf(1)));
                params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
                params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
                params.add(new BasicNameValuePair("paid", String.valueOf(costo)));
                params.add(new BasicNameValuePair("cost", String.valueOf(costo)));
                params.add(new BasicNameValuePair("totalDiscount", String.valueOf(0.0f)));
                params.add(new BasicNameValuePair("tableNumber", String.valueOf(tableNumber)));
                params.add(new BasicNameValuePair("roomName", String.valueOf(orderFragment.getRoomName())));

                callHttpHandler("/printLeftPayment", params);
            }else {



                if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.closeAll();
                    ditron.startSocket();
                }
                ClientThread myThread = ClientThread.getInstance();
                myThread.setPrintType(15);
                myThread.setLeftPayment(calculatorFragment.getLeftPayment());
                myThread.setBillId(String.valueOf(billId));
                myThread.setDeviceName(deviceName);



                myThread.setProducts(myProducts);
                myThread.setModifiers(myModifier);
                myThread.setBillId(String.valueOf(billId));
                myThread.setDeviceName(deviceName);
                myThread.setOrderNumberBill(String.valueOf(orderNumber));
                myThread.setCost(costo.floatValue());
                myThread.setPaid(costo.floatValue());
                myThread.setCredit(0.0f);
                myThread.setPaymentType(1);
                myThread.setTotalDiscount(totalDiscount);
                myThread.setTableNumber(tableNumber);
                myThread.setRoomName(orderFragment.getRoomName());
                myThread.delegate = forClient;
                myThread.setClientThread();

                myThread.setRunBaby(true);
            }
                if (!StaticValue.blackbox) {
                    for (LeftPayment left : calculatorFragment.getLeftPayment()) {
                        i++;
                        dbA.insertIntoItemSpecBill(left.getPaid(), left.getPaymentType(), billId);
                    }
                }
                int pType = calculatorFragment.getLeftPayment().get(0).getPaymentType();

                if (item.getMode() == -1) {
                    //sto pagando tutto lo split bill
                    Intent intent = new Intent(PaymentActivity.this, Operative.class);
                    int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                    String username = intentPay.getStringExtra("username");
                    int isAdmin = intentPay.getIntExtra("isAdmin", -1);

                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.setAction("billPaid");
                    intent.putExtra("billId", -1);
                    intent.putExtra("orderNumber", (orderNumber));
                    startActivity(intent);
                    finish();
                    try {
                        PaymentActivity.this.finish();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    //qua devo aggiungere come comportarsi
                    calculatorFragment.setLeftPayment(new ArrayList<LeftPayment>());
                    item.setPaid(true);
                    orderSubdivisionFragment.getSubdivisionAdapter().performClickOnTotal();

                    float delta = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost();
                    if (delta <= 0) {
                        savePaidBill(pType);
                    }
                }
            }



    }



    @Override
    public void printFiscalPartial(float paid, float cost, String description, int billIdToSplit, int paymentType){

        int billNumber = intentPay.getIntExtra("orderNumber", -1);

        int billId = intentPay.getIntExtra("billId", -1);

        if(StaticValue.blackbox){

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("printType", String.valueOf(3)));
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
            params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
            params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
            params.add(new BasicNameValuePair("paid", String.valueOf(paid)));
            params.add(new BasicNameValuePair("cost", String.valueOf(cost)));
            params.add(new BasicNameValuePair("description", "Per Amount"));
            params.add(new BasicNameValuePair("1", String.valueOf(1)));

            callHttpHandler("/printFiscalPartial", params);

            deleteCredit();
        }else {
            ClientThread myThread = ClientThread.getInstance();
            myThread.setPrintType(3);
            myThread.setBillId(String.valueOf(billId));
            myThread.setCost(cost);
            myThread.setPaid(paid);
            myThread.setPaymentType(paymentType);
            myThread.setDescription("Per Amount");
            myThread.setOrderNumberBill(String.valueOf(billIdToSplit));
            myThread.setQuantity(1);
            myThread.setDeviceName(deviceName);
            myThread.delegate = forClient;
            myThread.setClientThread();
            myThread.setRunBaby(true);
        }



    }

    public void printNonFiscal(){
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        if(item==null)
            item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
        if(item.isPaid()){
            openPopupToReprint(item);
        }else {

            if (item.getMode() == -1) {
                printTotalBillNonFiscal();
            } else if (item.getMode() == PERCENTAGE_MODE) {

            } else if (item.getMode() == PERSON_MODE || item.getMode() == ITEM_MODE) {
                printItemBillNonFiscal(item);
            } else if (item.getMode() == NUMBER_MODE) {
                printItemBillNonFiscalForNumber(item);
            }
        }
        orderFragment.getOrderListAdapter().notifyDataSetChanged();
    }

    public void openPopupToReprint(SubdivisionItem item){

        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.two_button_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams)popupView.findViewById(R.id.popup_container).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);
*/
                setUpReprintPopup(popupView, popupWindow, item);

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }

    public void setUpReprintPopup(View popupView, PopupWindow popupWindow, SubdivisionItem item) {
        popupView.findViewById(R.id.footer).setVisibility(View.VISIBLE);

        CustomTextView popupText = (CustomTextView) popupView.findViewById(R.id.popup_text);
        popupText.setText(R.string.which_kind_of_print_do_you_want_to_reprint);

        CustomButton notAccepted = (CustomButton) popupView.findViewById(R.id.firstButton);
        notAccepted .setText(R.string.non_fiscal);
        notAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getMode() == -1) {
                    printTotalBillNonFiscal();
                } else if (item.getMode() == PERCENTAGE_MODE) {

                } else if (item.getMode() == PERSON_MODE || item.getMode() == ITEM_MODE) {
                    printItemBillNonFiscal(item);
                } else if (item.getMode() == NUMBER_MODE) {
                    printItemBillNonFiscalForNumber(item);
                }
                popupWindow.dismiss();

            }
        });

        CustomButton accepted = (CustomButton) popupView.findViewById(R.id.secondButton);
        accepted.setText(R.string.fiscal);
        accepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item.getMode()!=1) {
                    printItemBill(item, item.getOwed_money(), item.getOwed_money(), item.getPaymentType());
                    popupWindow.dismiss();
                }else{
                    Toast.makeText(me, R.string.this_is_a_number_subdivision, Toast.LENGTH_SHORT).show();
                }


            }

        });

        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();

            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();

            }
        });

    }

    public void printItemBillNonFiscalForNumber(SubdivisionItem item){
        int billId = intentPay.getIntExtra("billId", -1);

        int orderNumber = intentPay.getIntExtra("orderNumber", 1);
        ArrayList<Customer> customers = new ArrayList<Customer>();
        int tableNumber = orderFragment.getTableNumber();
        ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
        Map<CashButtonLayout,ArrayList<CashButtonListLayout>> myModifier = new HashMap<CashButtonLayout,ArrayList<CashButtonListLayout>>();

        CashButtonLayout button = new CashButtonLayout();
        button.setTitle("per number");
        button.setPrice(item.getOwed_money());
        button.setQuantity(item.getNumber_subdivision());
        button.setProductId(-30);
        button.setIsDelete(false);
        button.setModifyModifier(false);
        button.setPosition(0);
        button.setID(-30);
        button.setClientPosition(0);
        button.setVat(StaticValue.staticVat);
        button.setNewDiscount(0.0f);
        button.setHomage(0);
        myProducts.add(button);


        if(StaticValue.blackbox){
            Map<String,ArrayList<CashButtonListLayout>> test =
                    new HashMap<String,ArrayList<CashButtonListLayout>>();
            for(int i =0; i<myProducts.size(); i++){
                test.put(String.valueOf(myProducts.get(i).getPosition()),myProducts.get(i).getCashList());
            }

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            Gson gson = new Gson();
            String prods = gson.toJson(myProducts);
            String mods = gson.toJson(test);
            params.add(new BasicNameValuePair("products", prods));
            params.add(new BasicNameValuePair("modifiers", mods));
            params.add(new BasicNameValuePair("printType", String.valueOf(4)));
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("paymentType", String.valueOf(1)));
            params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
            params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
            params.add(new BasicNameValuePair("paid", String.valueOf(item.getOwed_money() * item.getNumber_subdivision())));
            params.add(new BasicNameValuePair("cost", String.valueOf(item.getOwed_money() * item.getNumber_subdivision())));
            params.add(new BasicNameValuePair("totalDiscount", String.valueOf(0.0f)));
            params.add(new BasicNameValuePair("tableNumber", String.valueOf(tableNumber)));
            params.add(new BasicNameValuePair("roomName", String.valueOf(orderFragment.getRoomName())));

            callHttpHandler("/printItemBillNonFiscal", params);

            deleteCredit();
        }else {

            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(myProducts);
            myThread.setModifiers(myModifier);
            myThread.setPrintType(4);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCost(item.getOwed_money() * item.getNumber_subdivision());
            myThread.setPaid(item.getOwed_money() * item.getNumber_subdivision());
            myThread.setCredit(0.0f);
            myThread.setPaymentType(1);
            myThread.setTotalDiscount(0.0f);
            myThread.setTableNumber(tableNumber);
            myThread.setRoomName(orderFragment.getRoomName());
            myThread.delegate = forClient;
            myThread.setClientThread();
            myThread.setRunBaby(true);
            //myThread.addJsonString(combined.toString());
        }
    }

    public void printItemBillNonFiscal(SubdivisionItem item){
        int billId = intentPay.getIntExtra("billId", -1);

        Float totalDiscount = item.getDiscount();
        Double costo = Double.valueOf(item.getOwed_money());
        int orderNumber = intentPay.getIntExtra("orderNumber", 1);

        int tableNumber = orderFragment.getTableNumber();


        Map<CashButtonLayout,ArrayList<CashButtonListLayout>> myModifier = new HashMap<CashButtonLayout,ArrayList<CashButtonListLayout>>();
        Map<Integer,ArrayList<CashButtonListLayout>> myModifier2 = new HashMap<Integer,ArrayList<CashButtonListLayout>>();
        ArrayList<CashButtonLayout> products = item.getItems();
        ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
        for (CashButtonLayout cashButton : products) {
            if (!cashButton.isSelected()) {
                CashButtonLayout newbutton = new CashButtonLayout();
                newbutton.setTitle(cashButton.getTitle());
                newbutton.setPrice(cashButton.getPriceFloat());
                newbutton.setQuantity(cashButton.getQuantityInt());
                newbutton.setProductId(cashButton.getID());
                newbutton.setIsDelete(false);
                newbutton.setModifyModifier(false);
                newbutton.setPosition(cashButton.getPosition());
                newbutton.setID(cashButton.getID());
                newbutton.setClientPosition(cashButton.getClientPosition());
                newbutton.setVat(cashButton.getVat());
                myProducts.add(newbutton);
                //myProducts.add(cashButton);
                ArrayList<CashButtonListLayout> newList = new ArrayList<>();
                ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                for(CashButtonListLayout m : mList) {
                    int qtyM = orderFragment.getOrderListAdapter().returnQuantityForModifier(cashButton, m);
                    //m.setQuantity(qtyM);
                    CashButtonListLayout list = new CashButtonListLayout();
                    list.setTitle(m.getTitle());
                    list.setPrice(m.getPriceFloat());
                    list.setQuantity(qtyM);
                    list.setModifierId(m.getID());
                    list.setID(m.getID());
                    newList.add(list);

                   /* int qtyM = orderFragment.getOrderListAdapter().returnQuantityForModifier(cashButton, m);
                    m.setQuantity(qtyM);*/
                }
                int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                myProducts.get(myProducts.size() - 1).setQuantity(qty);
                myModifier2.put(myProducts.get(myProducts.size() - 1).getPosition(), newList);
                myModifier.put(myProducts.get(myProducts.size() - 1), newList);

            }
        }

        Collections.sort(myProducts);

        if(StaticValue.blackbox){
            Map<String,ArrayList<CashButtonListLayout>> test =
                    new HashMap<String,ArrayList<CashButtonListLayout>>();
            for(int i =0; i<myProducts.size(); i++){
                test.put(String.valueOf(myProducts.get(i).getPosition()),myProducts.get(i).getCashList());
            }

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            Gson gson = new Gson();
            String prods = gson.toJson(myProducts);
            String mods = gson.toJson(myModifier2);
            String creditArray = gson.toJson(creditId);
            params.add(new BasicNameValuePair("products", prods));
            params.add(new BasicNameValuePair("modifiers", mods));
            params.add(new BasicNameValuePair("printType", String.valueOf(4)));
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("paymentType", String.valueOf(1)));
            params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
            params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
            params.add(new BasicNameValuePair("paid", String.valueOf(costo)));
            params.add(new BasicNameValuePair("cost", String.valueOf(costo)));
            params.add(new BasicNameValuePair("totalDiscount", String.valueOf(totalDiscount)));
            params.add(new BasicNameValuePair("tableNumber", String.valueOf(tableNumber)));
            params.add(new BasicNameValuePair("roomName", String.valueOf(orderFragment.getRoomName())));

            callHttpHandler("/printItemBillNonFiscal", params);

            deleteCredit();
        }else {

            if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.closeAll();
                ditron.startSocket();
            }
            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(myProducts);
            myThread.setModifiers(myModifier);
            myThread.setPrintType(4);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCost(costo.floatValue());
            myThread.setPaid(costo.floatValue());
            myThread.setCredit(0.0f);
            myThread.setPaymentType(1);
            myThread.setTotalDiscount(totalDiscount);
            myThread.setTableNumber(tableNumber);
            myThread.setRoomName(orderFragment.getRoomName());
            myThread.delegate = forClient;
            myThread.setClientThread();
            myThread.setRunBaby(true);
        }

    }

    public void printTotalBillNonFiscal(){
        int billId = intentPay.getIntExtra("billId", -1);

        Float totalDiscount = dbA.getBillDiscountPrice(billId);
        Double costo = dbA.getBillPrice(billId);
        int orderNumber = intentPay.getIntExtra("orderNumber", 1);

        ArrayList<Customer> customers = new ArrayList<Customer>();

        int tableNumber = orderFragment.getTableNumber();

        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();

        Map<CashButtonLayout,ArrayList<CashButtonListLayout>> myModifier = new HashMap<CashButtonLayout,ArrayList<CashButtonListLayout>>();


        float mycost = 0.0f;
        ArrayList<CashButtonLayout> products= new ArrayList<>();
        products.addAll(item.getItems());


        ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>();
        for (CashButtonLayout cashButton : products) {
            if (!cashButton.isSelected()) {
                CashButtonLayout newbutton = new CashButtonLayout();
                newbutton.setTitle(cashButton.getTitle());
                newbutton.setPrice(cashButton.getPriceFloat());
                newbutton.setQuantity(cashButton.getQuantityInt());
                newbutton.setProductId(cashButton.getID());
                newbutton.setIsDelete(false);
                newbutton.setModifyModifier(false);
                newbutton.setPosition(cashButton.getPosition());
                newbutton.setID(cashButton.getID());
                newbutton.setClientPosition(cashButton.getClientPosition());
                newbutton.setVat(cashButton.getVat());
                myProducts.add(newbutton);
                ArrayList<CashButtonListLayout> newList = new ArrayList<>();
                ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                for(CashButtonListLayout m : mList) {
                    int qtyM = orderFragment.getOrderListAdapter().returnQuantityForModifier(cashButton, m);
                    //m.setQuantity(qtyM);
                        CashButtonListLayout list = new CashButtonListLayout();
                        list.setTitle(m.getTitle());
                        list.setPrice(m.getPriceFloat());
                        list.setQuantity(qtyM);
                        list.setModifierId(m.getID());
                        list.setID(m.getID());
                        newList.add(list);


                    mycost += m.getQuantityInt()*m.getPriceFloat();
                }
                int qty = orderFragment.getOrderListAdapter().returnQuantity(cashButton);
                myProducts.get(myProducts.size() - 1).setQuantity(qty);
                mycost += myProducts.get(myProducts.size() - 1).getQuantityInt()*myProducts.get(myProducts.size() - 1).getPriceFloat();
                myModifier.put(myProducts.get(myProducts.size() - 1), newList);
                //myModifiers.put(myProducts.get(myProducts.size() - 1), cashButton.getCashList());
            }
        }

        if(StaticValue.blackbox) {
            Map<String, ArrayList<CashButtonListLayout>> test =
                    new HashMap<String, ArrayList<CashButtonListLayout>>();
            for (int i = 0; i <myProducts.size(); i++) {
                test.put(String.valueOf(myProducts.get(i).getPosition()), myModifier.get(myProducts.get(i)));
            }

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            Gson gson = new Gson();
            String prods = gson.toJson(myProducts);
            String mods = gson.toJson(test);
            params.add(new BasicNameValuePair("products", prods));
            params.add(new BasicNameValuePair("modifiers", mods));
            params.add(new BasicNameValuePair("printType", String.valueOf(4)));
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("paymentType", String.valueOf(1)));
            params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
            params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
            params.add(new BasicNameValuePair("cost", String.valueOf(mycost)));
            params.add(new BasicNameValuePair("paid", String.valueOf(mycost)));
            params.add(new BasicNameValuePair("tableNumber", String.valueOf(tableNumber)));
            params.add(new BasicNameValuePair("roomName", String.valueOf(orderFragment.getRoomName())));
            params.add(new BasicNameValuePair("totalDiscount", String.valueOf(totalDiscount)));

            callHttpHandler("/printItemBillNonFiscal", params);
        }else{
            if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.closeAll();
                ditron.startSocket();
            }
            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(myProducts);
            myThread.setModifiers(myModifier);
            myThread.setPrintType(4);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCost(mycost);
            myThread.setPaid(mycost);
            myThread.setCredit(0.0f);
            myThread.setPaymentType(1);
            myThread.setTotalDiscount(totalDiscount);
            myThread.setTableNumber(tableNumber);
            myThread.setRoomName(orderFragment.getRoomName());
            myThread.delegate = forClient;
            myThread.setClientThread();
            myThread.setRunBaby(true);
        }

    }

    public void printFiscalBillWithNonFiscal(float credit ,float paid, float cost, String description, int billIdToSplit, int paymentType){
        int billId = intentPay.getIntExtra("billId", -1);
        int billNumber = intentPay.getIntExtra("orderNumber", -1);

        int orderNumber = intentPay.getIntExtra("orderNumber", 1);

        if(StaticValue.blackbox){
            Map<String,ArrayList<CashButtonListLayout>> test =
                    new HashMap<String,ArrayList<CashButtonListLayout>>();
            for(int i =0; i<products.size(); i++){
                test.put(String.valueOf(products.get(i).getPosition()), modifiers.get(products.get(i)));
            }

            SubdivisionItem item = orderFragment.getOrderListAdapter().getSubdivisionItem();
            if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() > 0)
                orderSubdivisionFragment.getSubdivisionAdapter().setOpenSplitPaid();

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            Gson gson = new Gson();
            String prods = gson.toJson(products);
            String mods = gson.toJson(test);
            String itemString = gson.toJson(item);
            String creditArray = gson.toJson(creditId);
            params.add(new BasicNameValuePair("products", prods));
            params.add(new BasicNameValuePair("modifiers", mods));
            params.add(new BasicNameValuePair("printType", String.valueOf(6)));
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("paymentType", String.valueOf(paymentType)));
            params.add(new BasicNameValuePair("deviceName", String.valueOf(deviceName)));
            params.add(new BasicNameValuePair("orderNumber", String.valueOf(orderNumber)));
            params.add(new BasicNameValuePair("paid", String.valueOf(paid)));
            params.add(new BasicNameValuePair("cost", String.valueOf(cost)));
            params.add(new BasicNameValuePair("creditArray", creditArray));
            params.add(new BasicNameValuePair("creditL", String.valueOf(credit)));
            params.add(new BasicNameValuePair("description", String.valueOf(description)));
            params.add(new BasicNameValuePair("item", itemString));


            callHttpHandler("/printBillWithNonFiscal", params);

            deleteCredit();
            Intent intent = new Intent(PaymentActivity.this, Operative.class);
            String username = intentPay.getStringExtra("username");
            int isAdmin = intentPay.getIntExtra("isAdmin", -1);

            intent.putExtra("username", username);
            intent.putExtra("isAdmin", isAdmin);
            intent.setAction("billPaid");
            intent.putExtra("billId", -1);
            intent.putExtra("orderNumber", (orderNumber));
            startActivity(intent);
            finish();
            try {
                PaymentActivity.this.finish();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }else {


            if (StaticValue.printerName.equals("ditron") && StaticValue.ditronApi) {
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.closeAll();
                ditron.startSocket();
            }
            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(products);
            myThread.setModifiers(modifiers);
            myThread.setPrintType(6);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCost(paid);
            myThread.setPaid(cost);
            myThread.setCredit(credit);
            myThread.setCreditL(credit);
            myThread.setPaymentType(paymentType);
            myThread.setDescription(description);
            myThread.delegate = forClient;
            myThread.setClientThread();
            myThread.setRunBaby(true);

            deleteCredit();
            SubdivisionItem item = orderFragment.getOrderListAdapter().getSubdivisionItem();
            if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() > 0)
                orderSubdivisionFragment.getSubdivisionAdapter().setOpenSplitPaid();
            /**
             * If pay mode is set to PAY_TOTAL_BILL or if the bill splits are all paid, it
             * updates the total bill db value setting it to paid on the current timestamp.
             * If not, it saves the paid bill split data for the next time.
             */
            dbA.savePaidBill(item, billId, 0);
            dbA.savePaidBill(null, billId, 0);
            if (credit > 0) {
                saveBillCredit(credit);
            }
            // close payment activity
            Intent intent = new Intent(PaymentActivity.this, Operative.class);
            String username = intentPay.getStringExtra("username");
            int isAdmin = intentPay.getIntExtra("isAdmin", -1);

            intent.putExtra("username", username);
            intent.putExtra("isAdmin", isAdmin);
            intent.setAction("billPaid");
            intent.putExtra("billId", -1);
            intent.putExtra("orderNumber", (orderNumber));
            startActivity(intent);
            finish();
            try {
                PaymentActivity.this.finish();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public void setIsActivePayementsOptions(Boolean b){
        optionsFragment.getAdapter().setActive(b);
    }

    public boolean checkIfOtherSplitBillArePaid(){
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().returnSplitOpen();
        if(item!=null) {
            if (item.getMode() == -1) {
                return orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherArePaid();
            } else {
                return true;
            }
        }else{return true;}
    }

    public boolean checkIfOtherSplitBillAreItemOrPerson(){
        return orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherPersonOrItem();
    }

    public boolean checkIfThereIsOneAmount(){
        return orderSubdivisionFragment.getSubdivisionAdapter().checkIfThereIsOneAmount();
    }


    public Customer getCustomer(int customerPosition){
        return orderFragment.getOrderListAdapter().getCustomer(customerPosition);
    }

    public SubdivisionItem returnSplitPosition(){
        SubdivisionItem a = orderSubdivisionFragment.getSubdivisionAdapter().returnSplitOpen();
        return a;
    }

    public void setSubdivisionCostLeft(float c, boolean add){
        orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionCostForHomage(c, add);

    }

    /**
     * part for partial payment on split bill for item and person
     */

    public void openPopupForPaymentLeft(ArrayList<LeftPayment> leftPayments) {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.payment_left_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
              /*  @SuppressLint("WrongViewCast")
                RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.pf_popup_container).getLayoutParams();
                int top1 = (int) (dpHeight - 262) / 2 - rlp1.height / 2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.pf_popup_container).setLayoutParams(rlp1);*/
                setUpLeftPaymentPopup(popupView, popupWindow,leftPayments);

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }


    public void setUpLeftPaymentPopup(View popupView, PopupWindow popupWindow, ArrayList<LeftPayment> leftPayments){

        CustomButton cashButton = (CustomButton) popupView.findViewById(R.id.pf_cash);
        cashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                setPaymentType(1);
                calculatorFragment.activatePaymentCalculator();
                hidePaymentButton();
                calculatorFragment.setCost(String.valueOf(leftPayments.get(leftPayments.size()-1).getCost()-leftPayments.get(leftPayments.size()-1).getPaid()));
                calculatorFragment.resetChange();
            }
        });

        CustomButton creditCard = (CustomButton) popupView.findViewById(R.id.pf_creditCard);
        creditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPaymentType(4);
                hidePaymentButton();
                calculatorFragment.activatePaymentCalculator();
                calculatorFragment.setCost(String.valueOf(leftPayments.get(leftPayments.size()-1).getCost()-leftPayments.get(leftPayments.size()-1).getPaid()));
                calculatorFragment.resetChange();
                popupWindow.dismiss();
            }
        });

        CustomButton bankCard = (CustomButton) popupView.findViewById(R.id.pf_bankCard);
        bankCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPaymentType(4);
                calculatorFragment.activatePaymentCalculator();
                calculatorFragment.setCost(String.valueOf(leftPayments.get(leftPayments.size()-1).getCost()-leftPayments.get(leftPayments.size()-1).getPaid()));
                calculatorFragment.resetChange();
                popupWindow.dismiss();
            }
        });


        ImageButton kill = (ImageButton) popupView.findViewById(R.id.kill);
        kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                calculatorFragment.resetChange();
                ArrayList<LeftPayment> leftPayments = calculatorFragment.getLeftPayment();
                if(leftPayments.size()==1) {
                    calculatorFragment.setMode(PAY_TOTAL_MODE);
                    leftPayments.clear();
                    calculatorFragment.setLeftPayment(new ArrayList<>());
                }else{
                    calculatorFragment.setLastLeftPayment();
                }

            }
        });

        ImageButton ok = (ImageButton) popupView.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void resetPaymentLeft(){
        ArrayList<LeftPayment> leftPayments = calculatorFragment.getLeftPayment();
        if(leftPayments.size()>0) {
            calculatorFragment.setLastLeftPayment();
        }
    }



    @Override
    public void onTaskEndWithResult(String success) {
        Log.i("HOS TAMPATO TUTTO", "STAMPATO TUTTO" +success);

    }

    @Override
    public void onTaskFinishGettingData(String result) {

    }




    /**
    * CLIENT POPUP NEW PART
    * not in a fragment, already too many fragment inside this activity
    *
    */

    /**
     * open client popup to select client for invoice or email
     */
    public void openClientPopup() {

        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        if(item==null) item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
        switch (item.getMode()) {
            case -1 :
                openRealClientPopup();

               /*
                if(item.getItems().get(0).getClientPosition()>0) {
                    Customer customerTotal = orderFragment.getOrderListAdapter().getCustomer(item.getItems().get(0).getClientPosition());
                    if (customerTotal.getCustomerId() != -1) {
                        selectedClient = customerTotal.getCustomerId();
                        //ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
                        invoiceBill = true;
                        activateLayoutForInvoiceOnly();
                    } else
                        openRealClientPopup();
                }else
                    openRealClientPopup();*/
                break;
            case PERCENTAGE_MODE :
                openRealClientPopup();
                break;
            case PERSON_MODE :
                openRealClientPopup();
               /* if(item.getItems().get(0).getClientPosition()>0) {
                    Customer customerInfo = orderFragment.getOrderListAdapter().getCustomer(item.getItems().get(0).getClientPosition());
                    if(customerInfo.getCustomerId()!=-1) {
                        selectedClient = customerInfo.getCustomerId();
                        //ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
                        invoiceBill = true;
                    }else
                        activateLayoutForInvoiceOnly();
                }else
                    openRealClientPopup();*/

                break;
            case ITEM_MODE:
                openRealClientPopup();
                /*if(item.getItems().get(0).getClientPosition()>0) {
                    Customer customerInfo = orderFragment.getOrderListAdapter().getCustomer(item.getItems().get(0).getClientPosition());
                    if(customerInfo.getCustomerId()!=-1) {
                        selectedClient = customerInfo.getCustomerId();
                        //ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
                        invoiceBill = true;
                    }else
                        activateLayoutForInvoiceOnly();
                }else
                    openRealClientPopup();*/

                break;
            case NUMBER_MODE:
                openRealClientPopup();
                break;
            default :
                openRealClientPopup();
                break;


        }


    }

    public void openRealClientPopup(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.activity_clients, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                setUpClientPopup(popupView, popupWindow);
            }
        });
        //to use to prevent graphic to move up
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    private CustomEditText name_et;
    private CustomEditText surname_et;
    private CustomEditText email_et;
    private CustomEditText company_name_et;
    private CustomEditText vat_number_et;
    private CustomEditText address_et;
    private CustomEditText postal_code_et;
    private CustomEditText country_et;
    private CustomEditText city_et;
    //private CustomEditText codice_fiscale_et;
    private CustomEditText provincia_et;
    private CustomButton addCompanyInfo;
    private CustomButton addPersonalInfo;
    private CustomButton addNewClient;
    private boolean newClient = false;
    private CustomButton searchClients;
    private CustomButton setDiscount;
    private CustomEditText search_et;
    private Animation shake;
    private CustomEditText codice_destinatario_et;
    private CustomEditText pec_et;

    private CustomEditText vat_number_et_p;
    private CustomEditText address_et_p;
    private CustomEditText postal_code_et_p;
    private CustomEditText country_et_p;
    private CustomEditText city_et_p;
    private CustomEditText codice_fiscale_et_p;
    private CustomEditText provincia_et_p;
    private CustomEditText codice_destinatario_et_p;
    private CustomEditText pec_et_p;

    private ClientInfo sClient = null;

    private void setUpClientPopup(View popupView, PopupWindow popupWindow) {
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        newClient = false;
        name_et = (CustomEditText)popupView.findViewById(R.id.name_et);
        surname_et = (CustomEditText)popupView.findViewById(R.id.surname_et);
        email_et = (CustomEditText)popupView.findViewById(R.id.email_et);
        company_name_et = (CustomEditText)popupView.findViewById(R.id.company_name_et);
        vat_number_et = (CustomEditText)popupView.findViewById(R.id.vat_number_et);
        address_et = (CustomEditText)popupView.findViewById(R.id.address_et);
        postal_code_et = (CustomEditText)popupView.findViewById(R.id.postal_code_et);
        country_et = (CustomEditText)popupView.findViewById(R.id.country_et);
        city_et = (CustomEditText)popupView.findViewById(R.id.city_et);
        //codice_fiscale_et = (CustomEditText)popupView.findViewById(R.id.codice_fiscale_et);
        provincia_et = (CustomEditText)popupView.findViewById(R.id.provincia_et);
        addCompanyInfo = (CustomButton)popupView.findViewById(R.id.add_company_info_button);
        addPersonalInfo = (CustomButton)popupView.findViewById(R.id.add_personal_info_button);
        addNewClient = (CustomButton)popupView.findViewById(R.id.add_new_client_button);
        searchClients = (CustomButton)popupView.findViewById(R.id.search_client_button);
        setDiscount = (CustomButton)popupView.findViewById(R.id.discount_button);
        search_et = (CustomEditText)popupView.findViewById(R.id.search_et);
        codice_destinatario_et = (CustomEditText)popupView.findViewById(R.id.codice_destinatario_et);
        pec_et = (CustomEditText)popupView.findViewById(R.id.pec_et);

        vat_number_et_p = (CustomEditText)popupView.findViewById(R.id.vat_number_et_p);
        address_et_p = (CustomEditText)popupView.findViewById(R.id.address_et_p);
        postal_code_et_p = (CustomEditText)popupView.findViewById(R.id.postal_code_et_p);
        country_et_p = (CustomEditText)popupView.findViewById(R.id.country_et_p);
        city_et_p = (CustomEditText)popupView.findViewById(R.id.city_et_p);
        codice_fiscale_et_p = (CustomEditText)popupView.findViewById(R.id.codice_fiscale_et_p);
        provincia_et_p = (CustomEditText)popupView.findViewById(R.id.provincia_et_p);
        codice_destinatario_et_p = (CustomEditText)popupView.findViewById(R.id.codice_destinatario_et_p);
        pec_et_p = (CustomEditText)popupView.findViewById(R.id.pec_et_p);

        vat_number_et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });
        vat_number_et_p.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });

        InputFilter[] input = new InputFilter[2];

        input[0] = new InputFilter.LengthFilter(16) ;
        input[1] = new InputFilter.AllCaps() ;
        codice_fiscale_et_p.setFilters(input);

        InputFilter[] input1 = new InputFilter[2];
        input1[0] = new InputFilter.LengthFilter(2) ;
        input1[1] = new InputFilter.AllCaps() ;
        provincia_et.setFilters(input1);
        provincia_et_p.setFilters(input1);

        RecyclerView clients_list_rv = (RecyclerView) popupView.findViewById(R.id.clients_rv);
        clients_list_rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        clientsAdapter = new ClientsPopupAdapter(dbA, this, mode, getBillId());
        clients_list_rv.setAdapter(clientsAdapter);
        clients_list_rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                ClientInfo c = (ClientInfo) view.getTag();
                //parent.getChildAdapterPosition(view) == 3
                outRect.set(0, 0, 0, 1);
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                //super.onDraw(c, parent, state);
                Drawable divider = PaymentActivity.this.getDrawable(R.drawable.divider_line_horizontal1dp);
                final int size = divider.getIntrinsicHeight();
                int left = parent.getLeft() + parent.getPaddingLeft();
                int right = parent.getRight() - parent.getPaddingRight();
                int top = 0;
                int bottom = 0;
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = parent.getChildAt(i);
                    if (i != childCount) {
                        top = child.getBottom();
                        bottom = top + 1;
                        divider.setBounds(left, top, right, bottom);
                        divider.draw(c);
                    }
                }
            }
        });


        searchClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    searchClients.setText(R.string.end_search);
                    v.setActivated(!v.isActivated());
                    CustomEditText search_et = (CustomEditText)popupView.findViewById(R.id.search_et);
                    search_et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    search_et.setSingleLine();
                    if(v.isActivated()){

                        clientsAdapter.setSearchMode(true);
                        search_et.setVisibility(View.VISIBLE);
                        popupView.findViewById(R.id.title_tv).setVisibility(View.INVISIBLE);
                        popupView.findViewById(R.id.hline_rv_top).setVisibility(View.INVISIBLE);
                        search_et.requestFocus();
                        ((InputMethodManager)getSystemService(PaymentActivity.this.INPUT_METHOD_SERVICE))
                                .showSoftInput(search_et, InputMethodManager.SHOW_IMPLICIT);

                        search_et.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            private Timer timer = new Timer();
                            private final long DELAY = 500; // milliseconds
                            @Override
                            public void afterTextChanged(final Editable s) {
                                timer.cancel();
                                timer = new Timer();
                                timer.schedule(
                                        new TimerTask() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable(){

                                                    @Override
                                                    public void run(){
                                                        clientsAdapter.searchClients(s.toString());
                                                    }
                                                });
                                            }
                                        },
                                        DELAY
                                );

                            }
                        });

                        search_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
                                    hideKeyboard(v);
                                }
                            }
                        });

                    }
                    else {
                        searchClients.setText(R.string.search_clients);
                        clientsAdapter.setSearchMode(false);

                        popupView.findViewById(R.id.title_tv).setVisibility(View.VISIBLE);
                        popupView.findViewById(R.id.hline_rv_top).setVisibility(View.VISIBLE);
                        ((CustomEditText)popupView.findViewById(R.id.search_et)).setText("");
                        popupView.findViewById(R.id.search_et).setVisibility(View.GONE);
                    }

            }
        });


        // add/show company info button click
        addCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.setActivated(!v.isActivated());
                if(v.isActivated()){
                    popupView.findViewById(R.id.company_info_scroll).setVisibility(View.VISIBLE);
                    popupView.findViewById(R.id.company_info_container).setVisibility(View.VISIBLE);

                    popupView.findViewById(R.id.personal_info_scroll).setVisibility(View.GONE);
                    popupView.findViewById(R.id.personal_info_container).setVisibility(View.GONE);
                    addPersonalInfo.setActivated(false);

                    //popupView.findViewById(R.id.company_info_container).setVisibility(View.VISIBLE);
                }
                else{
                    popupView.findViewById(R.id.company_info_container).setVisibility(View.GONE);
                    popupView.findViewById(R.id.company_info_scroll).setVisibility(View.GONE);

                    popupView.findViewById(R.id.personal_info_scroll).setVisibility(View.GONE);
                    popupView.findViewById(R.id.personal_info_container).setVisibility(View.GONE);
                    addPersonalInfo.setActivated(false);
                    //popupView.findViewById(R.id.company_info_container).setVisibility(View.GONE);
                }
             }

        });

        addPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.setActivated(!v.isActivated());
                if(v.isActivated()){
                    popupView.findViewById(R.id.company_info_scroll).setVisibility(View.GONE);
                    popupView.findViewById(R.id.company_info_container).setVisibility(View.GONE);

                    popupView.findViewById(R.id.personal_info_scroll).setVisibility(View.VISIBLE);
                    popupView.findViewById(R.id.personal_info_container).setVisibility(View.VISIBLE);

                    addCompanyInfo.setActivated(false);
                    //popupView.findViewById(R.id.company_info_container).setVisibility(View.VISIBLE);
                }
                else{
                    popupView.findViewById(R.id.company_info_container).setVisibility(View.GONE);
                    popupView.findViewById(R.id.company_info_scroll).setVisibility(View.GONE);

                    popupView.findViewById(R.id.personal_info_scroll).setVisibility(View.GONE);
                    popupView.findViewById(R.id.personal_info_container).setVisibility(View.GONE);

                    addCompanyInfo.setActivated(false);
                    //popupView.findViewById(R.id.company_info_container).setVisibility(View.GONE);
                }
            }

        });

        addNewClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String name = ((CustomEditText)popupView.findViewById(R.id.name_et)).getText().toString().replaceAll("'","\'");
                String surname = ((CustomEditText)popupView.findViewById(R.id.surname_et)).getText().toString().replaceAll("'","\'");
                String email = ((CustomEditText)popupView.findViewById(R.id.email_et)).getText().toString().replaceAll("'","\'");
                /**
                 * if the addCompanyInfo button is active then  there's the need to satisfy two more conditions(company name and vat_number)
                 * in order to add(or update) a client.
                 */
                if(addCompanyInfo.isActivated()){
                    String company_name = ((CustomEditText)popupView.findViewById(R.id.company_name_et)).getText().toString().replaceAll("'","\'");
                    String vat_number = ((CustomEditText)popupView.findViewById(R.id.vat_number_et)).getText().toString().replaceAll("'","\'");
                    String address = ((CustomEditText)popupView.findViewById(R.id.address_et)).getText().toString().replaceAll("'","\'");
                    String postal_code = ((CustomEditText)popupView.findViewById(R.id.postal_code_et)).getText().toString().replaceAll("'","\'");
                    String country = ((CustomEditText)popupView.findViewById(R.id.country_et)).getText().toString().replaceAll("'","\'");
                    String city = ((CustomEditText)popupView.findViewById(R.id.city_et)).getText().toString().replaceAll("'","\'");
                    String codicefiscale = "";
                    String provincia= ((CustomEditText)popupView.findViewById(R.id.provincia_et)).getText().toString().replaceAll("'","\'");
                    String codiceDestinatario= ((CustomEditText)popupView.findViewById(R.id.codice_destinatario_et)).getText().toString().replaceAll("'","\'");
                    String pec= ((CustomEditText)popupView.findViewById(R.id.pec_et)).getText().toString().replaceAll("'","\'");

                    /**
                     * The following set of If statements checks whether the minimum conditions to create a new user are
                     * satisfied.
                     */
                    if(!company_name.equals("") &&
                            !vat_number.equals("")&&
                            !address.equals("") &&
                            !postal_code.equals("") &&
                            !country.equals("") &&
                            !city.equals("") &&
                            !provincia.equals("")  &&
                            ( !codiceDestinatario.equals("") ||  !pec.equals(""))
                    ) {
                        if (!company_name.equals("")) {
                            if (!vat_number.equals("") && vat_number.length() == 11) {
                                if((!codiceDestinatario.equals("") && codiceDestinatario.length()==7) || (!pec.equals("") && pec.length()>1)) {
                            if(!name.equals("")||!email.equals("")||!surname.equals("")){
                                if(!email.equals("")){
                                    if(!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email)){
                                   /*     findViewById(R.id.email_et).startAnimation(shake);
                                        //Toast.makeText(getBaseContext(),"Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                               */     }
                                }
                                int company_id;
                                /**
                                 *  If Modify Mode is not on, then the "addNewClient" button will actually add a new client,
                                 *  otherwise it will update the selected client.
                                 */
                                if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email)) {
                                    View myEmail = findViewById(R.id.email_et);
                                    if(myEmail!=null)
                                        myEmail.startAnimation(shake);
                                    ////Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                }else {
                                    if (StaticValue.blackbox) {
                                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                        params.add(new BasicNameValuePair("name", name));
                                        params.add(new BasicNameValuePair("surname", surname));
                                        params.add(new BasicNameValuePair("email", email));
                                        params.add(new BasicNameValuePair("companyName", String.valueOf(company_name)));
                                        params.add(new BasicNameValuePair("address", String.valueOf(address)));
                                        params.add(new BasicNameValuePair("vat_number", String.valueOf(vat_number)));
                                        params.add(new BasicNameValuePair("postal_code", String.valueOf(postal_code)));
                                        params.add(new BasicNameValuePair("city", String.valueOf(city)));
                                        params.add(new BasicNameValuePair("country", String.valueOf(country)));
                                        params.add(new BasicNameValuePair("codicefiscale", String.valueOf(codicefiscale)));
                                        params.add(new BasicNameValuePair("provincia", String.valueOf(provincia)));
                                        params.add(new BasicNameValuePair("codiceDestinatario", String.valueOf(codiceDestinatario)));
                                        params.add(new BasicNameValuePair("pec", String.valueOf(pec)));


                                        callHttpHandler("/insertClientWithCompanyPA", params);

                                    } else {
                                        int client_id = dbA.insertClient(name, surname, email);
                                        if (client_id != -1) {
                                            company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                            dbA.insertClientInCompany(client_id, company_id);
                                            if (!clientsAdapter.searchMode)
                                                // clientsAdapter.updateDataSet();

                                                //reset dataFields
                                                name_et.setText("");
                                            surname_et.setText("");
                                            email_et.setText("");
                                            company_name_et.setText("");
                                            address_et.setText("");
                                            vat_number_et.setText("");
                                            postal_code_et.setText("");
                                            city_et.setText("");
                                            country_et.setText("");

                                            provincia_et.setText("");
                                            codice_destinatario_et.setText("");
                                            pec_et.setText("");
                                            addCompanyInfo.performClick();
                                        }

                                    }
                                }
                                }else {
                                    if (email.equals("") )
                                        popupView.findViewById(R.id.email_et).startAnimation(shake);
                                    if (name.equals("") )
                                        popupView.findViewById(R.id.name_et).startAnimation(shake);
                                    if (surname.equals(""))
                                        popupView.findViewById(R.id.surname_et).startAnimation(shake);
                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                }

                            }else{
                                if((codiceDestinatario.equals("") || codiceDestinatario.length()!=7)){
                                    popupView.findViewById(R.id.codice_destinatario_et).startAnimation(shake);
                                }else {
                                    popupView.findViewById(R.id.pec_et).startAnimation(shake);
                                }
                            }
                                } else {
                                    popupView.findViewById(R.id.vat_number_et).startAnimation(shake);
                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                popupView.findViewById(R.id.company_name_et).startAnimation(shake);
                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                        }
                }else if(addPersonalInfo.isActivated()) {
                        String company_name = "";
                        String vat_number = ((CustomEditText)popupView.findViewById(R.id.vat_number_et_p)).getText().toString().replaceAll("'","\'");
                        String address = (((CustomEditText)popupView.findViewById(R.id.address_et_p)).getText().toString()).replaceAll("'","\'");
                        address = address.replaceAll("'", "''");
                        String postal_code = ((CustomEditText)popupView.findViewById(R.id.postal_code_et_p)).getText().toString().replace("'","\'");
                        String country = ((CustomEditText)popupView.findViewById(R.id.country_et_p)).getText().toString().replaceAll("'","\'");
                        country = country.replaceAll("'", "''");
                        String city = ((CustomEditText)popupView.findViewById(R.id.city_et_p)).getText().toString().replaceAll("'","\'");
                        city = city.replaceAll("'", "''");
                        String codicefiscale = ((CustomEditText)popupView.findViewById(R.id.codice_fiscale_et_p)).getText().toString().replaceAll("'","\'");
                        String provincia = ((CustomEditText)popupView.findViewById(R.id.provincia_et_p)).getText().toString().replaceAll("'","\'");
                        String codiceDestinatario= ((CustomEditText)popupView.findViewById(R.id.codice_destinatario_et_p)).getText().toString().replaceAll("'","\'");
                        String pec = ((CustomEditText)popupView.findViewById(R.id.pec_et_p)).getText().toString().replaceAll("'","\'");
                        /**
                         * The following set of If statements checks whether the minimum conditions to create a new user are
                         * satisfied.
                         */

                        if(!codicefiscale.equals("") &&
                                !vat_number.equals("")&&
                                !address.equals("") &&
                                !postal_code.equals("") &&
                                !country.equals("") &&
                                !city.equals("") &&
                                !provincia.equals("")  &&
                                ( !codiceDestinatario.equals("") ||  !pec.equals(""))
                        ) {
                            if (!codicefiscale.equals("") && codicefiscale.length()==16) {
                                if (!vat_number.equals("") && vat_number.length() == 11) {
                                    if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec.equals("") && pec.length() > 1)) {
                                        if (!name.equals("") || !email.equals("") || !surname.equals("")) {
                                            if (!email.equals("")) {
                                                if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email)) {
                                   /*     findViewById(R.id.email_et).startAnimation(shake);
                                        //Toast.makeText(getBaseContext(),"Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                               */
                                                }
                                            }
                                            int company_id;
                                            /**
                                             *  If Modify Mode is not on, then the "addNewClient" button will actually add a new client,
                                             *  otherwise it will update the selected client.
                                             */
                                            if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email)) {
                                                popupView.findViewById(R.id.email_et).startAnimation(shake);
                                                ////Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                            } else {
                                                if(StaticValue.blackbox){
                                                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                                    params.add(new BasicNameValuePair("name", name));
                                                    params.add(new BasicNameValuePair("surname", surname));
                                                    params.add(new BasicNameValuePair("email", email));
                                                    params.add(new BasicNameValuePair("companyName", String.valueOf(company_name)));
                                                    params.add(new BasicNameValuePair("address", String.valueOf(address)));
                                                    params.add(new BasicNameValuePair("vat_number", String.valueOf(vat_number)));
                                                    params.add(new BasicNameValuePair("postal_code", String.valueOf(postal_code)));
                                                    params.add(new BasicNameValuePair("city", String.valueOf(city)));
                                                    params.add(new BasicNameValuePair("country", String.valueOf(country)));
                                                    params.add(new BasicNameValuePair("codicefiscale", String.valueOf(codicefiscale)));
                                                    params.add(new BasicNameValuePair("provincia", String.valueOf(provincia)));
                                                    params.add(new BasicNameValuePair("codiceDestinatario", String.valueOf(codiceDestinatario)));
                                                    params.add(new BasicNameValuePair("pec", String.valueOf(pec)));



                                                    callHttpHandler("/insertClientWithCompanyPA",params );

                                                }else {
                                                    int client_id = dbA.insertClient(name, surname, email);
                                                    if (client_id != -1) {
                                                        int company = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                        dbA.insertClientInCompany(client_id, company);
                                                        if (!clientsAdapter.searchMode)
                                                            //clientsAdapter.updateDataSet();
                                                            ////Toast.makeText(ClientsActivity.this, "Client and company added", //Toast.LENGTH_SHORT).show();

                                                            //reset dataFields
                                                            name_et.setText("");
                                                        surname_et.setText("");
                                                        email_et.setText("");
                                                        //company_name_et.setText("");
                                                        address_et_p.setText("");
                                                        vat_number_et_p.setText("");
                                                        postal_code_et_p.setText("");
                                                        city_et_p.setText("");
                                                        country_et_p.setText("");
                                                        codice_fiscale_et_p.setText("");
                                                        provincia_et_p.setText("");
                                                        codice_destinatario_et_p.setText("");
                                                        pec_et_p.setText("");
                                                        addPersonalInfo.performClick();
                                                    }
                                                }

                                            }

                                        } else {
                                            if (email.equals("") )
                                                popupView.findViewById(R.id.email_et).startAnimation(shake);
                                            if (name.equals("") )
                                                popupView.findViewById(R.id.name_et).startAnimation(shake);
                                            if (surname.equals(""))
                                                popupView.findViewById(R.id.surname_et).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if((codiceDestinatario.equals("") || codiceDestinatario.length()!=7)){
                                            popupView.findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                        }else {
                                            popupView.findViewById(R.id.pec_et_p).startAnimation(shake);
                                        }
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    popupView.findViewById(R.id.vat_number_et_p).startAnimation(shake);
                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                popupView.findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                            ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                        }
                    }
                else{
                    if(!name.equals("")||!email.equals("")||!surname.equals("")){
                        if(!email.equals("")) {
                            if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email)) {
                               /* findViewById(R.id.email_et).startAnimation(shake);
                                //Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();*/
                            }
                        }
                        /**
                         * If Modify Mode is not on, then the "addNewClient" button will actually add a new client,
                         *  otherwise it will update the selected client.
                         */
                        if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email)) {
                            findViewById(R.id.email_et).startAnimation(shake);
                            ////Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                        }else {
                            if(!StaticValue.blackbox) {
                                int client_id = dbA.insertClient(name, surname, email);
                                dbA.showData("client");
                                if (client_id == -1) {
                                    popupView.findViewById(R.id.email_et).startAnimation(shake);
                                    Toast.makeText(getApplicationContext(), R.string.client_already_present, Toast.LENGTH_SHORT).show();
                                } else {

                                    dbA.insertClientInCompany(client_id, -1);
                                    name_et.setText("");
                                    surname_et.setText("");
                                    email_et.setText("");
                                    clientsAdapter.reloadCLients();
                                }
                            }else{
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("name", name));
                                params.add(new BasicNameValuePair("surname", surname));
                                params.add(new BasicNameValuePair("email", email));
                                  params.add(new BasicNameValuePair("companyId", String.valueOf(-1)));


                                callHttpHandler("/insertClientPA",params );
                            }
                        }

                    }
                    else {
                        popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                        ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                    }
                }
                clientsAdapter.reloadCLients();
            }

        });

        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(newClient){
                    newClient = false;
                }else {
                    if (sClient != null) {
                        deselectClient();
                        sClient = null;
                    } else {
                        popupWindow.dismiss();
                    }
                }
            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (newClient) {
                    if (addCompanyInfo.isActivated()) {
                        String name = ((CustomEditText) popupView.findViewById(R.id.name_et)).getText().toString().replaceAll("'", "\'");
                        String surname = ((CustomEditText) popupView.findViewById(R.id.surname_et)).getText().toString().replaceAll("'", "\'");
                        String email = ((CustomEditText) popupView.findViewById(R.id.email_et)).getText().toString().replaceAll("'", "\'");
                        String company_name = ((CustomEditText) popupView.findViewById(R.id.company_name_et)).getText().toString().replaceAll("'", "\'");
                        String vat_number = ((CustomEditText) popupView.findViewById(R.id.vat_number_et)).getText().toString().replaceAll("'", "\'");
                        String address = ((CustomEditText) popupView.findViewById(R.id.address_et)).getText().toString().replaceAll("'", "\'");
                        String postal_code = ((CustomEditText) popupView.findViewById(R.id.postal_code_et)).getText().toString().replaceAll("'", "\'");
                        String country = ((CustomEditText) popupView.findViewById(R.id.country_et)).getText().toString().replaceAll("'", "\'");
                        String city = ((CustomEditText) popupView.findViewById(R.id.city_et)).getText().toString().replaceAll("'", "\'");
                        String codicefiscale = "";
                        String provincia = ((CustomEditText) popupView.findViewById(R.id.provincia_et)).getText().toString().replaceAll("'", "\'");
                        String codiceDestinatario = ((CustomEditText) popupView.findViewById(R.id.codice_destinatario_et)).getText().toString().replaceAll("'", "\'");
                        String pec = ((CustomEditText) popupView.findViewById(R.id.pec_et)).getText().toString().replaceAll("'", "\'");


                        if (!company_name.equals("") &&
                                !vat_number.equals("") &&
                                !address.equals("") &&
                                !postal_code.equals("") &&
                                !country.equals("") &&
                                !city.equals("") &&
                                !provincia.equals("") &&
                                (!codiceDestinatario.equals("") || !pec.equals(""))
                        ) {
                            if (!company_name.equals("")) {
                                if (!vat_number.equals("") && vat_number.length() == 11) {
                                    if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec.equals("") && pec.length() > 1)) {
                                        if (!name.equals("") || !email.equals("") || !surname.equals("")) {
                                            if (!email.equals("")) {
                                                if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email)) {
                                                    Toast.makeText(getBaseContext(), R.string.not_a_valid_email, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            int company_id;
                                            sClient.setName(name);
                                            sClient.setSurname(surname);
                                            sClient.setEmail(email);
                                            company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                            sClient.setCompany_id(company_id);
                                            sClient.setCompany_name(company_name);
                                            sClient.setCompany_address(address);
                                            sClient.setCompany_vat_number(vat_number);
                                            sClient.setCompany_postal_code(postal_code);
                                            sClient.setCompany_city(city);
                                            sClient.setCompany_country(country);
                                            sClient.setCodice_fiscale(codicefiscale);
                                            sClient.setCodice_fiscale(provincia);
                                            sClient.setCodice_destinatario(codiceDestinatario);
                                            sClient.setPec(pec);
                                            dbA.updateClientData(sClient);

                                            Toast.makeText(me, R.string.client_updated, Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (email.equals(""))
                                                popupView.findViewById(R.id.email_et).startAnimation(shake);
                                            if (name.equals(""))
                                                popupView.findViewById(R.id.name_et).startAnimation(shake);
                                            if (surname.equals(""))
                                                popupView.findViewById(R.id.surname_et).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7)) {
                                            popupView.findViewById(R.id.codice_destinatario_et).startAnimation(shake);
                                        } else {
                                            popupView.findViewById(R.id.pec_et).startAnimation(shake);
                                        }
                                    }
                                } else {
                                    popupView.findViewById(R.id.vat_number_et).startAnimation(shake);
                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                popupView.findViewById(R.id.company_name_et).startAnimation(shake);
                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                        }
                    } else if (addPersonalInfo.isActivated()) {
                        String name = ((CustomEditText) popupView.findViewById(R.id.name_et)).getText().toString().replaceAll("'", "\'");
                        String surname = ((CustomEditText) popupView.findViewById(R.id.surname_et)).getText().toString().replaceAll("'", "\'");
                        String email = ((CustomEditText) popupView.findViewById(R.id.email_et)).getText().toString().replaceAll("'", "\'");
                        String company_name = "";
                        String vat_number = ((CustomEditText) popupView.findViewById(R.id.vat_number_et_p)).getText().toString().replaceAll("'", "\'");
                        String address = ((CustomEditText) popupView.findViewById(R.id.address_et_p)).getText().toString().replaceAll("'", "\'");
                        String postal_code = ((CustomEditText) popupView.findViewById(R.id.postal_code_et_p)).getText().toString().replaceAll("'", "\'");
                        String country = ((CustomEditText) popupView.findViewById(R.id.country_et_p)).getText().toString().replaceAll("'", "\'");
                        String city = ((CustomEditText) popupView.findViewById(R.id.city_et_p)).getText().toString().replaceAll("'", "\'");
                        String codicefiscale = ((CustomEditText) popupView.findViewById(R.id.codice_fiscale_et_p)).getText().toString().replaceAll("'", "\'");
                        String provincia = ((CustomEditText) popupView.findViewById(R.id.provincia_et_p)).getText().toString().replaceAll("'", "\'");
                        String codiceDestinatario = ((CustomEditText) popupView.findViewById(R.id.codice_destinatario_et_p)).getText().toString().replaceAll("'", "\'");
                        String pec = ((CustomEditText) popupView.findViewById(R.id.pec_et_p)).getText().toString().replaceAll("'", "\'");


                        if (!codicefiscale.equals("") &&
                                !vat_number.equals("") &&
                                !address.equals("") &&
                                !postal_code.equals("") &&
                                !country.equals("") &&
                                !city.equals("") &&
                                !provincia.equals("") &&
                                (!codiceDestinatario.equals("") || !pec.equals(""))
                        ) {
                            if (!codicefiscale.equals("") && codicefiscale.length() == 16) {
                                if (!vat_number.equals("") && vat_number.length() == 11) {
                                    if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec.equals("") && pec.length() > 1)) {
                                        if (!name.equals("") || !email.equals("") || !surname.equals("")) {
                                            if (!email.equals("")) {
                                                if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email)) {
                                                    Toast.makeText(getBaseContext(), R.string.not_a_valid_email, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            int company_id;
                                            sClient.setName(name);
                                            sClient.setSurname(surname);
                                            sClient.setEmail(email);
                                            company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                            sClient.setCompany_id(company_id);
                                            sClient.setCompany_name(company_name);
                                            sClient.setCompany_address(address);
                                            sClient.setCompany_vat_number(vat_number);
                                            sClient.setCompany_postal_code(postal_code);
                                            sClient.setCompany_city(city);
                                            sClient.setCompany_country(country);
                                            sClient.setCodice_fiscale(codicefiscale);
                                            sClient.setCodice_fiscale(provincia);
                                            sClient.setCodice_destinatario(codiceDestinatario);
                                            sClient.setPec(pec);
                                            dbA.updateClientData(sClient);

                                            Toast.makeText(me, R.string.client_updated, Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (email.equals(""))
                                                popupView.findViewById(R.id.email_et).startAnimation(shake);
                                            if (name.equals(""))
                                                popupView.findViewById(R.id.name_et).startAnimation(shake);
                                            if (surname.equals(""))
                                                popupView.findViewById(R.id.surname_et).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7)) {
                                            popupView.findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                        } else {
                                            popupView.findViewById(R.id.pec_et_p).startAnimation(shake);
                                        }
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    popupView.findViewById(R.id.vat_number_et_p).startAnimation(shake);
                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                popupView.findViewById(R.id.codice_fiscale_et_p).startAnimation(shake);
                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                            ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (sClient != null) {
                        if (sClient.isHasCompany()) {
                            //
                            if (addCompanyInfo.isActivated()) {
                                String name = ((CustomEditText) popupView.findViewById(R.id.name_et)).getText().toString().replaceAll("'", "\'");
                                String surname = ((CustomEditText) popupView.findViewById(R.id.surname_et)).getText().toString().replaceAll("'", "\'");
                                String email = ((CustomEditText) popupView.findViewById(R.id.email_et)).getText().toString().replaceAll("'", "\'");
                                String company_name = ((CustomEditText) popupView.findViewById(R.id.company_name_et)).getText().toString().replaceAll("'", "\'");
                                String vat_number = ((CustomEditText) popupView.findViewById(R.id.vat_number_et)).getText().toString().replaceAll("'", "\'");
                                String address = ((CustomEditText) popupView.findViewById(R.id.address_et)).getText().toString().replaceAll("'", "\'");
                                String postal_code = ((CustomEditText) popupView.findViewById(R.id.postal_code_et)).getText().toString().replaceAll("'", "\'");
                                String country = ((CustomEditText) popupView.findViewById(R.id.country_et)).getText().toString().replaceAll("'", "\'");
                                String city = ((CustomEditText) popupView.findViewById(R.id.city_et)).getText().toString().replaceAll("'", "\'");
                                String codicefiscale = "";
                                String provincia = ((CustomEditText) popupView.findViewById(R.id.provincia_et)).getText().toString().replaceAll("'", "\'");
                                String codiceDestinatario = ((CustomEditText) popupView.findViewById(R.id.codice_destinatario_et)).getText().toString().replaceAll("'", "\'");
                                String pec = ((CustomEditText) popupView.findViewById(R.id.pec_et)).getText().toString().replaceAll("'", "\'");

                                if (!company_name.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codiceDestinatario.equals("") || !pec.equals(""))
                                ) {
                                    if (!company_name.equals("")) {
                                        if (!vat_number.equals("") && vat_number.length() == 11) {
                                            if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec.equals("") && pec.length() > 1)) {
                                                if (!name.equals("") || !email.equals("") || !surname.equals("")) {
                                        /*if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email)) {
                                            Toast.makeText(getBaseContext(), "Not a Valid E-Mail", Toast.LENGTH_SHORT).show();
                                        } else {*/
                                                    selectedClient = sClient.getClient_id();
                                                    ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
                                                    invoiceBill = true;
                                                    activateLayoutForInvoiceOnly();
                                                    int company_id;
                                                    sClient.setName(name);
                                                    sClient.setSurname(surname);
                                                    sClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    sClient.setCompany_id(company_id);
                                                    sClient.setCompany_name(company_name);
                                                    sClient.setCompany_address(address);
                                                    sClient.setCompany_vat_number(vat_number);
                                                    sClient.setCompany_postal_code(postal_code);
                                                    sClient.setCompany_city(city);
                                                    sClient.setCompany_country(country);
                                                    sClient.setCodice_fiscale(codicefiscale);
                                                    sClient.setProvincia(provincia);
                                                    sClient.setCodice_destinatario(codiceDestinatario);
                                                    sClient.setPec(pec);
                                                    dbA.updateClientData(sClient);

                                                    invoiceBill = true;
                                                    popupWindow.dismiss();
                                                    //}
                                                } else {
                                                    if (email.equals(""))
                                                        popupView.findViewById(R.id.email_et).startAnimation(shake);
                                                    if (name.equals(""))
                                                        popupView.findViewById(R.id.name_et).startAnimation(shake);
                                                    if (surname.equals(""))
                                                        popupView.findViewById(R.id.surname_et).startAnimation(shake);
                                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                                    ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7)) {
                                                    popupView.findViewById(R.id.codice_destinatario_et).startAnimation(shake);
                                                } else {
                                                    popupView.findViewById(R.id.pec_et).startAnimation(shake);
                                                }
                                            }
                                        } else {
                                            popupView.findViewById(R.id.vat_number_et).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        popupView.findViewById(R.id.company_name_et).startAnimation(shake);
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                                }
                            } else if (addPersonalInfo.isActivated()) {
                                String name = ((CustomEditText) popupView.findViewById(R.id.name_et)).getText().toString().replaceAll("'", "\'");
                                String surname = ((CustomEditText) popupView.findViewById(R.id.surname_et)).getText().toString().replaceAll("'", "\'");
                                String email = ((CustomEditText) popupView.findViewById(R.id.email_et)).getText().toString().replaceAll("'", "\'");
                                String company_name = "";
                                String vat_number = ((CustomEditText) popupView.findViewById(R.id.vat_number_et_p)).getText().toString().replaceAll("'", "\'");
                                String address = ((CustomEditText) popupView.findViewById(R.id.address_et_p)).getText().toString().replaceAll("'", "\'");
                                String postal_code = ((CustomEditText) popupView.findViewById(R.id.postal_code_et_p)).getText().toString().replaceAll("'", "\'");
                                String country = ((CustomEditText) popupView.findViewById(R.id.country_et_p)).getText().toString().replaceAll("'", "\'");
                                String city = ((CustomEditText) popupView.findViewById(R.id.city_et_p)).getText().toString().replaceAll("'", "\'");
                                String codicefiscale = ((CustomEditText) popupView.findViewById(R.id.codice_fiscale_et_p)).getText().toString().replaceAll("'", "\'");
                                String provincia = ((CustomEditText) popupView.findViewById(R.id.provincia_et_p)).getText().toString().replaceAll("'", "\'");
                                String codiceDestinatario = ((CustomEditText) popupView.findViewById(R.id.codice_destinatario_et_p)).getText().toString().replaceAll("'", "\'");
                                String pec = ((CustomEditText) popupView.findViewById(R.id.pec_et_p)).getText().toString().replaceAll("'", "\'");

                                if (!codicefiscale.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codiceDestinatario.equals("") || !pec.equals(""))
                                ) {
                                    if (!codicefiscale.equals("") && codicefiscale.length() == 16) {
                                        if (!vat_number.equals("") && vat_number.length() == 11) {
                                            if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec.equals("") && pec.length() > 1)) {
                                                if (!name.equals("") || !email.equals("") || !surname.equals("")) {
                                        /*if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email)) {
                                            Toast.makeText(getBaseContext(), "Not a Valid E-Mail", Toast.LENGTH_SHORT).show();
                                        } else {*/
                                                    selectedClient = sClient.getClient_id();
                                                    ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
                                                    invoiceBill = true;
                                                    activateLayoutForInvoiceOnly();
                                                    int company_id;
                                                    sClient.setName(name);
                                                    sClient.setSurname(surname);
                                                    sClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    sClient.setCompany_id(company_id);
                                                    sClient.setCompany_name(company_name);
                                                    sClient.setCompany_address(address);
                                                    sClient.setCompany_vat_number(vat_number);
                                                    sClient.setCompany_postal_code(postal_code);
                                                    sClient.setCompany_city(city);
                                                    sClient.setCompany_country(country);
                                                    sClient.setCodice_fiscale(codicefiscale);
                                                    sClient.setProvincia(provincia);
                                                    sClient.setCodice_destinatario(codiceDestinatario);
                                                    sClient.setPec(pec);
                                                    dbA.updateClientData(sClient);

                                                    invoiceBill = true;
                                                    popupWindow.dismiss();
                                                    //}
                                                } else {
                                                    if (email.equals(""))
                                                        popupView.findViewById(R.id.email_et).startAnimation(shake);
                                                    if (name.equals(""))
                                                        popupView.findViewById(R.id.name_et).startAnimation(shake);
                                                    if (surname.equals(""))
                                                        popupView.findViewById(R.id.surname_et).startAnimation(shake);
                                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                                    ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7)) {
                                                    popupView.findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                                } else {
                                                    popupView.findViewById(R.id.pec_et_p).startAnimation(shake);
                                                }
                                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                                ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            popupView.findViewById(R.id.vat_number_et_p).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        popupView.findViewById(R.id.codice_fiscale_et_p).startAnimation(shake);
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            if (addCompanyInfo.isActivated()) {
                                String company_name = ((CustomEditText) popupView.findViewById(R.id.company_name_et)).getText().toString().replaceAll("'", "\'");
                                String vat_number = ((CustomEditText) popupView.findViewById(R.id.vat_number_et)).getText().toString().replaceAll("'", "\'");
                                String address = ((CustomEditText) popupView.findViewById(R.id.address_et)).getText().toString().replaceAll("'", "\'");
                                String postal_code = ((CustomEditText) popupView.findViewById(R.id.postal_code_et)).getText().toString().replaceAll("'", "\'");
                                String country = ((CustomEditText) popupView.findViewById(R.id.country_et)).getText().toString().replaceAll("'", "\'");
                                String city = ((CustomEditText) popupView.findViewById(R.id.city_et)).getText().toString().replaceAll("'", "\'");
                                String codicefiscale = "";
                                String provincia = ((CustomEditText) popupView.findViewById(R.id.provincia_et)).getText().toString().replaceAll("'", "\'");
                                String codiceDestinatario = ((CustomEditText) popupView.findViewById(R.id.codice_destinatario_et)).getText().toString().replaceAll("'", "\'");
                                String pec = ((CustomEditText) popupView.findViewById(R.id.pec_et)).getText().toString().replaceAll("'", "\'");

                                /**
                                 * The following set of If statements checks whether the minimum conditions to create a new user are
                                 * satisfied.
                                 */
                                if (!company_name.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codiceDestinatario.equals("") || !pec.equals(""))
                                ) {
                                    if (!company_name.equals("")) {
                                        if (!vat_number.equals("") && vat_number.length() == 11) {
                                            if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec.equals("") && pec.length() > 1)) {


                                                int company_id;
                                                /**
                                                 *  If Modify Mode is not on, then the "addNewClient" button will actually add a new client,
                                                 *  otherwise it will update the selected client.
                                                 */

                                                if (sClient.getClient_id() != -1) {
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    dbA.insertClientInCompany(sClient.getClient_id(), company_id);
                                                    if (!clientsAdapter.searchMode)
                                                        // clientsAdapter.updateDataSet();

                                                        //reset dataFields
                                                        name_et.setText("");
                                                    surname_et.setText("");
                                                    email_et.setText("");
                                                    company_name_et.setText("");
                                                    address_et.setText("");
                                                    vat_number_et.setText("");
                                                    postal_code_et.setText("");
                                                    city_et.setText("");
                                                    country_et.setText("");

                                                    provincia_et.setText("");
                                                    codice_destinatario_et.setText("");
                                                    pec_et.setText("");
                                                    addCompanyInfo.performClick();

                                                    selectedClient = sClient.getClient_id();
                                                    ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
                                                    invoiceBill = true;
                                                    activateLayoutForInvoiceOnly();
                                                   /* sClient.setName(sc);
                                                    sClient.setSurname(surname);*/
                                                    sClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    sClient.setCompany_id(company_id);
                                                    sClient.setCompany_name(company_name);
                                                    sClient.setCompany_address(address);
                                                    sClient.setCompany_vat_number(vat_number);
                                                    sClient.setCompany_postal_code(postal_code);
                                                    sClient.setCompany_city(city);
                                                    sClient.setCompany_country(country);
                                                    sClient.setCodice_fiscale(codicefiscale);
                                                    sClient.setProvincia(provincia);
                                                    sClient.setCodice_destinatario(codiceDestinatario);
                                                    sClient.setPec(pec);
                                                    dbA.updateClientData(sClient);

                                                    invoiceBill = true;
                                                    popupWindow.dismiss();

                                                }


                                            } else {
                                                if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7)) {
                                                    popupView.findViewById(R.id.codice_destinatario_et).startAnimation(shake);
                                                } else {
                                                    popupView.findViewById(R.id.pec_et).startAnimation(shake);
                                                }
                                            }
                                        } else {
                                            popupView.findViewById(R.id.vat_number_et).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        popupView.findViewById(R.id.company_name_et).startAnimation(shake);
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                                }
                            } else if (addPersonalInfo.isActivated()) {
                                String company_name = "";
                                String vat_number = ((CustomEditText) popupView.findViewById(R.id.vat_number_et_p)).getText().toString().replaceAll("'", "\'");
                                String address = ((CustomEditText) popupView.findViewById(R.id.address_et_p)).getText().toString().replaceAll("'", "\'");
                                String postal_code = ((CustomEditText) popupView.findViewById(R.id.postal_code_et_p)).getText().toString().replaceAll("'", "\'");
                                String country = ((CustomEditText) popupView.findViewById(R.id.country_et_p)).getText().toString().replaceAll("'", "\'");
                                String city = ((CustomEditText) popupView.findViewById(R.id.city_et_p)).getText().toString().replaceAll("'", "\'");
                                String codicefiscale = ((CustomEditText) popupView.findViewById(R.id.codice_fiscale_et_p)).getText().toString().replaceAll("'", "\'");
                                String provincia = ((CustomEditText) popupView.findViewById(R.id.provincia_et_p)).getText().toString().replaceAll("'", "\'");
                                String codiceDestinatario = ((CustomEditText) popupView.findViewById(R.id.codice_destinatario_et_p)).getText().toString().replaceAll("'", "\'");
                                String pec = ((CustomEditText) popupView.findViewById(R.id.pec_et_p)).getText().toString().replaceAll("'", "\'");

                                /**
                                 * The following set of If statements checks whether the minimum conditions to create a new user are
                                 * satisfied.
                                 */
                                if (!codicefiscale.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codiceDestinatario.equals("") || !pec.equals(""))
                                ) {
                                    if (!codicefiscale.equals("") && codicefiscale.length() == 16) {
                                        if (!vat_number.equals("") && vat_number.length() == 11) {
                                            if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec.equals("") && pec.length() > 1)) {


                                                int company_id;
                                                /**
                                                 *  If Modify Mode is not on, then the "addNewClient" button will actually add a new client,
                                                 *  otherwise it will update the selected client.
                                                 */

                                                if (sClient.getClient_id() != -1) {
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    dbA.insertClientInCompany(sClient.getClient_id(), company_id);
                                                    if (!clientsAdapter.searchMode)
                                                        // clientsAdapter.updateDataSet();
                                                        ////Toast.makeText(ClientsActivity.this, "Client and company added", //Toast.LENGTH_SHORT).show();

                                                        //reset dataFields
                                                        name_et.setText("");
                                                    surname_et.setText("");
                                                    email_et.setText("");
                                                    //company_name_et_p.setText("");
                                                    address_et_p.setText("");
                                                    vat_number_et_p.setText("");
                                                    postal_code_et_p.setText("");
                                                    city_et_p.setText("");
                                                    country_et_p.setText("");
                                                    codice_fiscale_et_p.setText("");
                                                    provincia_et_p.setText("");
                                                    codice_destinatario_et_p.setText("");
                                                    pec_et_p.setText("");
                                                    addPersonalInfo.performClick();

                                                    selectedClient = sClient.getClient_id();
                                                    ClientInfo clientInfo = dbA.fetchSingleClientForPayment(selectedClient);
                                                    invoiceBill = true;
                                                    activateLayoutForInvoiceOnly();
                                                   /* sClient.setName(sc);
                                                    sClient.setSurname(surname);*/
                                                    sClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    sClient.setCompany_id(company_id);
                                                    sClient.setCompany_name(company_name);
                                                    sClient.setCompany_address(address);
                                                    sClient.setCompany_vat_number(vat_number);
                                                    sClient.setCompany_postal_code(postal_code);
                                                    sClient.setCompany_city(city);
                                                    sClient.setCompany_country(country);
                                                    sClient.setCodice_fiscale(codicefiscale);
                                                    sClient.setProvincia(provincia);
                                                    sClient.setCodice_destinatario(codiceDestinatario);
                                                    sClient.setPec(pec);
                                                    dbA.updateClientData(sClient);

                                                    invoiceBill = true;
                                                    popupWindow.dismiss();

                                                }


                                            } else {
                                                if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7)) {
                                                    popupView.findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                                } else {
                                                    popupView.findViewById(R.id.pec_et_p).startAnimation(shake);
                                                }
                                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                                ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            popupView.findViewById(R.id.vat_number_et_p).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        popupView.findViewById(R.id.codice_fiscale_et_p).startAnimation(shake);
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    popupView.findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(me, R.string.please_select_a_registered_client, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }
            }
        });
    }

    /**
     * @param client - selected client
     * Triggered when client is clicked.
     */
    public void setSelectedClient(ClientInfo client){
        //this is used to remove focus, still a working on
        search_et.setFocusableInTouchMode(false);
        search_et.setFocusable(false);
        search_et.setFocusableInTouchMode(true);
        search_et.setFocusable(true);


        sClient = client;
        name_et.setText(client.getName());
        surname_et.setText(client.getSurname());
        email_et.setText(client.getEmail());

        if(addCompanyInfo.isActivated()){
            addCompanyInfo.performClick();
            company_name_et.setText("");
            address_et.setText("");
            vat_number_et.setText("");
            postal_code_et.setText("");
            city_et.setText("");
            country_et.setText("");
            //  codice_fiscale_et.setText("");
            provincia_et.setText("");
            codice_destinatario_et.setText("");
            pec_et.setText("");
        }
        if(addPersonalInfo.isActivated()){
            addPersonalInfo.performClick();
            address_et_p.setText("");
            vat_number_et_p.setText("");
            postal_code_et_p.setText("");
            city_et_p.setText("");
            country_et_p.setText("");
            codice_fiscale_et_p.setText("");
            provincia_et_p.setText("");
            codice_destinatario_et_p.setText("");
            pec_et_p.setText("");
        }

        if(client.getCompany_id() > 0){
            if(!client.getCompany_name().equals("") && client.getCodice_fiscale().equals("")) {
                if (!addCompanyInfo.isActivated()) addCompanyInfo.performClick();
                company_name_et.setText(client.getCompany_name());
                address_et.setText(client.getCompany_address());
                vat_number_et.setText(client.getCompany_vat_number());
                postal_code_et.setText(client.getCompany_postal_code());
                city_et.setText(client.getCompany_city());
                country_et.setText(client.getCompany_country());
                //codice_fiscale_et.setText(client.getCodice_fiscale());
                provincia_et.setText(client.getProvincia());
                codice_destinatario_et.setText(client.getCodice_destinatario());
                pec_et.setText(client.getPec());
            }else{
                if(!addPersonalInfo.isActivated())addPersonalInfo.performClick();
                address_et_p.setText(client.getCompany_address());
                vat_number_et_p.setText(client.getCompany_vat_number());
                postal_code_et_p.setText(client.getCompany_postal_code());
                city_et_p.setText(client.getCompany_city());
                country_et_p.setText(client.getCompany_country());
                codice_fiscale_et_p.setText(client.getCodice_fiscale());
                provincia_et_p.setText(client.getProvincia());
                codice_destinatario_et_p.setText(client.getCodice_destinatario());
                pec_et_p.setText(client.getPec());
            }
        }


    }

    public void deselectClient(){
        name_et.setText("");
        surname_et.setText("");
        email_et.setText("");
        if(addCompanyInfo.isActivated()){
            addCompanyInfo.performClick();
            company_name_et.setText("");
            address_et.setText("");
            vat_number_et.setText("");
            postal_code_et.setText("");
            city_et.setText("");
            country_et.setText("");
            //  codice_fiscale_et.setText("");
            provincia_et.setText("");
            codice_destinatario_et.setText("");
            pec_et.setText("");
        }
        if(addPersonalInfo.isActivated()){
            addPersonalInfo.performClick();
            address_et_p.setText("");
            vat_number_et_p.setText("");
            postal_code_et_p.setText("");
            city_et_p.setText("");
            country_et_p.setText("");
            codice_fiscale_et_p.setText("");
            provincia_et_p.setText("");
            codice_destinatario_et_p.setText("");
            pec_et_p.setText("");
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * SPLIT ELEMENT ITEM PART
     *
     */

    public void setSplitElementItem(){
        hidenBlueButtonExHomage();
        hidePaymentButton();
        hideSplitButton();
        splitItemSet = true;
        mode = HOMAGE_MODE;
        orderFragment.activateSelectionMode(OrderListAdapter.HOMAGE_MODE);
        orderSubdivisionFragment.setMode(OrderListAdapter.HOMAGE_MODE);

        int lightBlue = Color.parseColor("#05a8c0");
        findViewById(R.id.discount_button).setBackgroundColor(lightBlue);
        findViewById(R.id.homage_button).setBackgroundResource(R.drawable.button_lightblue_3dp_white);

    }

    public void setElementItemSplitValue(int quantity, int groupPosition){
        splitItemSet = false;
        calculatorFragment.turnOnOffCalculator();
        orderFragment.getOrderListAdapter().separateElementItem(quantity, groupPosition);
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        showPaymentButton();
        showSplitButton();
        showBlueButtonExDiscount();
        optionsFragment.setButtonPermission();
        orderFragment.setMode(DEFAULT_MODE);
        calculatorFragment.setPayementShortcut();
        activatePaymentButtons();
        setNormalKillOkButton();
        mode = DEFAULT_MODE;
        if(item!=null){
            if(item.getMode()==-1){
                if(orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()==1)
                    orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
            }
        }else {
            orderFragment.activateSelectionMode(OrderListAdapter.DEFAULT_MODE);
        }
        discountSet = false;
        orderFragment.discountSet = false;
        calculatorFragment.setIsActive(false);
        calculatorFragment.setTmpPositionDiscount(-1);

    }

    /**
     * ask if you want to remove popup from element
     */
    public void openModifyPopup(int groupPosition, View elementView){
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        if(item==null) item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
        if(!item.isPaid()) {
            LayoutInflater layoutInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.modify_discount_popup, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            popupView.post(new Runnable() {
                @Override
                public void run() {
                  /*  @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                            (RelativeLayout.LayoutParams) popupView.findViewById(R.id.popup_container).getLayoutParams();
                    int top1 = (int) (dpHeight - 52) / 2 - rlp1.height / 2;
                    rlp1.topMargin = top1;
                    popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);*/

                    setUpPopupToModifyDiscount(popupView, popupWindow, groupPosition, elementView);

                }
            });

            popupWindow.setFocusable(true);
            popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
        }

    }

    public void setUpPopupToModifyDiscount(View popupView, PopupWindow popupWindow, int groupPosition, View elementView){
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });
        //modify
        popupView.findViewById(R.id.firstButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                setMode(16);
                modifyDiscountValue(elementView, groupPosition, 1);
                setTempPositionDiscount(groupPosition);
                orderFragment.getOrderListAdapter().notifyDataSetChanged();
            }
        });
        //delete
        popupView.findViewById(R.id.secondButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CashButtonLayout product = orderFragment.getOrderListAdapter().getElement(groupPosition);
                if(product.getHomage()==0) {
                    float myDiscount = orderFragment.getOrderListAdapter().getElementDiscount(groupPosition);
                    setDiscountAmount(orderFragment.getOrderListAdapter().getElementDiscount(groupPosition), 0.0f, true, groupPosition, false);
                    calculatorFragment.turnOffCalculator();
                    setKillForSplitOnCalculator();
                    popupWindow.dismiss();
                }else{
                    orderFragment.setHomageMethod(groupPosition);
                    mode = DEFAULT_MODE;
                    popupWindow.dismiss();
                }
            }
        });
    }

    /**
     * ask if you want to remove popup from element
     */
    public void openModifyPopupForTotal(){
        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
        if(item==null) item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
        if(!item.isPaid()) {

            LayoutInflater layoutInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.modify_discount_popup, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            popupView.post(new Runnable() {
                @Override
                public void run() {
                   /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                            (RelativeLayout.LayoutParams) popupView.findViewById(R.id.popup_container).getLayoutParams();
                    int top1 = (int) (dpHeight - 52) / 2 - rlp1.height / 2;
                    rlp1.topMargin = top1;
                    popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);*/

                    setUpPopupToModifyDiscountForTotal(popupView, popupWindow);

                }
            });

            popupWindow.setFocusable(true);
            popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
        }

    }

    public void setUpPopupToModifyDiscountForTotal(View popupView, PopupWindow popupWindow){
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });
        //modify
        popupView.findViewById(R.id.firstButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                orderFragment.setPartialTotalDiscount(false);
                orderFragment.setTotalDiscount(true);
                activateFunction(TOTAL_MODIFY_DISCOUNT_MODE, "", orderFragment.getTotalCost());

            }
        });
        //delete
        popupView.findViewById(R.id.secondButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 popupWindow.dismiss();
                setTotalDiscountAmount(0.0f, 0.0f, true, false);
                calculatorFragment.turnOnOffCalculator();
                setKillForSplitOnCalculator();
            }
        });
    }


    /**
     * ask if you want to remove popup from element
     */
    public void openRemoveAllDiscount(){

        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.two_button_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
              /*  @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams)popupView.findViewById(R.id.popup_container).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);
*/
                setUpOkForConfirmPopupForItem(popupView, popupWindow);
                setUpRemoveAllDiscount(popupView, popupWindow);

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }

    public void setUpRemoveAllDiscount(View popupView, PopupWindow popupWindow) {
        CustomTextView popupText = (CustomTextView) popupView.findViewById(R.id.popup_text);
        popupText.setText(R.string.do_you_want_to_remove_all_discount);

        CustomButton notAccepted = (CustomButton) popupView.findViewById(R.id.firstButton);
        notAccepted .setText(R.string.no);
        notAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();

            }
        });
        CustomButton accepted = (CustomButton) popupView.findViewById(R.id.secondButton);
        accepted.setText(R.string.yes);
        accepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                if(item ==null)
                    item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                if(item.getMode()==-1 || item.getMode()==2 || item.getMode()==3) {
                    ArrayList<CashButtonLayout> myProducts = item.getItems();
                    for (int groupPosition = 0; groupPosition < myProducts.size(); groupPosition++) {
                        CashButtonLayout product = orderFragment.getOrderListAdapter().getElement(groupPosition);
                        if (product.getHomage() == 0) {
                            float myDiscount = orderFragment.getOrderListAdapter().getElementDiscount(groupPosition);
                            setDiscountAmount(orderFragment.getOrderListAdapter().getElementDiscount(groupPosition), 0.0f, true, groupPosition, false);


                        } else {
                            orderFragment.setHomageMethod(groupPosition);

                        }
                    }
                }
                setTotalDiscountAmount(0.0f, 0.0f, true, false);
                calculatorFragment.turnOffCalculator();
                popupWindow.dismiss();

            }

        });

    }

    public void openPayOtherPopup(){

        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.one_button_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams)popupView.findViewById(R.id.popup_container).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);*/

                CustomTextView popupText = (CustomTextView) popupView.findViewById(R.id.popup_text);
                popupText.setText(R.string.please_pay_other_split_bill_first);

                CustomButton notAccepted = (CustomButton) popupView.findViewById(R.id.okButton);
                notAccepted.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        popupWindow.dismiss();

                    }
                });

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }


    public void resetPinpadTimer(int type){
        TimerManager.stopPinpadAlert();
        TimerManager.setContext(getApplicationContext());
        intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);
        Intent intent = getIntent();
        intentPasscode.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
        intentPasscode.putExtra("username", intent.getStringExtra("username"));

        TimerManager.setIntentPinpad(intentPasscode);
        TimerManager.startPinpadAlert(type);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            resetPinpadTimer(2);
            View v = getCurrentFocus();
            if ( v instanceof CustomEditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }

        }
        return super.dispatchTouchEvent( event );
    }


}



    /* OLD STUFF


    --- CALLED BY ONCREATE
    private void setOrderSubdivisionFragment(boolean inOrOut){
        if (inOrOut)
        {
            orderSubdivisionFragment = new OrderSubdivisionFragment();
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.order_subdivision, orderSubdivisionFragment, "orderSubdivision");
            transaction.commit();
        }

        else
        {
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(getSupportFragmentManager().findFragmentByTag("orderSubdivision"));
            transaction.commit();
        }
    }
    */

