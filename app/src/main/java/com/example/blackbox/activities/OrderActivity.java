package com.example.blackbox.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.adapter.CashAdapterForOrder;
import com.example.blackbox.adapter.TableOrderAdapter;
import com.example.blackbox.adapter.TakeAwayAdapter;
import com.example.blackbox.client.ClientThread;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.model.TotalBill;
import com.example.blackbox.model.TwoString;
import com.example.blackbox.printer.PrinterDitronThread;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.google.gson.Gson;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.example.blackbox.model.TimerManager.context;


/**
 * Created by tiziano on 8/29/17.
 */

public class OrderActivity extends AppCompatActivity implements TakeAwayAdapter.AdapterCallback, ClientThread.TaskDelegate, HttpHandler.AsyncResponse
{

    private static final String TAG = "<OrderActivity>";

    private float density;

    public OrderActivity()
    {
    }

    public static OrderActivity newInstance()
    {
        return new OrderActivity();
    }


    private RecyclerView take_away_recycler;
    private TakeAwayAdapter take_away_adapter;
    private TableOrderAdapter table_order_adapter;
    private RecyclerView table_order_recycler;
    private DatabaseAdapter dbA;
    private Intent intent;

    private int userType;
    private int userId;
    private boolean flag = false;   //false: current orders, true: recent orders
    private String username;
    private int isAdmin;

    private Intent intentPasscode;

    private OrderActivity orderActivity = null;

    private HttpHandler httpHandler;
    private View myPopupView;
    private PopupWindow myPopupWindow;

    @Override
    public void processFinish(String output)
    {
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        JSONObject jsonObject = null;
        Boolean check = false;
        try
        {
            jsonObject = new JSONObject(output);
            String route = jsonObject.getString("route");

            switch (route)
            {

                case "getTakeAwayOrders":
                    check = jsonObject.getBoolean("check");
                    if (check)
                    {
                        //if (!jsonObject.getBoolean("updated"))
                        //{
                        JSONArray tbArray = new JSONObject(output).getJSONArray("totalBills");
                        ArrayList<TotalBill> totals = TotalBill.fromJsonArray(tbArray);
                        take_away_adapter.setTotalBillLists(totals);

                        //dbA.updateChecksumForTable("bill_total", jsonObject.getString("billTotalChecksum"));
                        //}
                    }

                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                             .show();
                    }
                    break;

                case "getTableOrders":
                    check = jsonObject.getBoolean("check");
                    if (check)
                    {
                        JSONArray tbArray = new JSONObject(output).getJSONArray("totalBills");
                        JSONArray tableArray = new JSONObject(output).getJSONArray("tableNumber");

                        ArrayList<TotalBill> totals = TotalBill.fromJsonArray(tbArray);

                        ArrayList<TwoString> tableNumbers = TwoString.fromJsonArray(tableArray);
                        table_order_adapter.setTableNumbers(tableNumbers);
                        table_order_adapter.setTotalBillLists(totals);
                        table_order_adapter.notifyDataSetChanged();

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                             .show();
                    }
                    break;


                case "getBillData":
                    check = jsonObject.getBoolean("check");
                    if (check)
                    {
                        JSONObject tbArray = new JSONObject(output).getJSONObject("totalBill");

                        JSONArray productArray = new JSONObject(output).getJSONArray("products");
                        JSONArray modifierArray = new JSONObject(output).getJSONArray("modifiers");
                        JSONArray customerArray = new JSONObject(output).getJSONArray("customers");

                        int tableNum = jsonObject.getInt("tableNumber");

                        TotalBill totals = TotalBill.fromJson(tbArray);
                        ArrayList<CashButtonLayout> products = CashButtonLayout.fromJsonArray(productArray);
                        ArrayList<CashButtonListLayout> modifiers = CashButtonListLayout.fromJsonArray(modifierArray);
                        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

                        for (CashButtonLayout product : products)
                        {
                            map.put(product, product.getCashList());
                        }

                        ArrayList<Customer> customers = Customer.fromJsonArray(customerArray);
                        setPopupCashListFromServer(totals, products, map, customers, tableNum);
                    }

                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                             .show();
                    }
                    break;

                case "getAllOrders":
                    check = jsonObject.getBoolean("check");
                    if (check)
                    {
                        int myFlag = jsonObject.getInt("flag");
                        JSONArray taArray = new JSONObject(output).getJSONArray("takeAway");
                        JSONArray tbArray = new JSONObject(output).getJSONArray("tableList");
                        JSONArray tableArray = new JSONObject(output).getJSONArray("tableNumber");
                        ArrayList<TotalBill> tables = TotalBill.fromJsonArray(tbArray);
                        ArrayList<TotalBill> takeAways = TotalBill.fromJsonArray(taArray);
                        ArrayList<TwoString> tableNumbers = TwoString.fromJsonArray(tableArray);
                        if (myFlag == 1)
                        {

                            //setta gli ordini sul take away
                            take_away_adapter.setPaidBillsListFromServer(takeAways);
                            take_away_adapter.notifyDataSetChanged();

                            //setta gli ordini sui tables
                            table_order_adapter.setPaidTablesFromServer(tables, tableNumbers);
                            table_order_adapter.notifyDataSetChanged();

                            CustomButton currentOrdersButton = (CustomButton) findViewById(R.id.recent_orders);
                            currentOrdersButton.setText(R.string.current_orders);
                            flag = true;
                        }
                        else
                        {

                            //setta gli ordini sui take away
                            take_away_adapter.setBillsListFromServer(takeAways);
                            take_away_adapter.notifyDataSetChanged();

                            //setta gli ordini sui tables
                            table_order_adapter.setCurrentTablesFromServer(tables, tableNumbers);
                            table_order_adapter.notifyDataSetChanged();

                            CustomButton recentOrdersButton = (CustomButton) findViewById(R.id.recent_orders);
                            recentOrdersButton.setText(R.string.recent_orders);
                            flag = false;
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                             .show();
                    }
                    break;

                case "payBillTypeReturnTAList":
                    check = jsonObject.getBoolean("check");
                    if (check)
                    {
                        JSONArray tbArray = new JSONObject(output).getJSONArray("totalBills");
                        ArrayList<TotalBill> totals = TotalBill.fromJsonArray(tbArray);

                        take_away_adapter.setTotalBillLists(totals);
                        take_away_adapter.myPopupWindow.dismiss();

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                             .show();
                    }
                    break;

                case "payBillTypeReturnTableList":
                    check = jsonObject.getBoolean("check");
                    if (check)
                    {
                        JSONArray tbArray = new JSONObject(output).getJSONArray("totalBills");
                        JSONArray tableArray = new JSONObject(output).getJSONArray("tableNumber");

                        ArrayList<TotalBill> totals = TotalBill.fromJsonArray(tbArray);

                        ArrayList<TwoString> tableNumbers = TwoString.fromJsonArray(tableArray);
                        table_order_adapter.setTableNumbers(tableNumbers);
                        table_order_adapter.setTotalBillLists(totals);
                        table_order_adapter.notifyDataSetChanged();
                        table_order_adapter.myPopupWindow.dismiss();

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                             .show();
                    }
                    break;

                case "reprintOrder":
                    // this request will always return check: true
                    // thus nothing to do
                    break;

                default:
                    Toast.makeText(getApplicationContext(), "Unkown route: " + route, Toast.LENGTH_SHORT)
                         .show();
                    break;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


    public void callHttpHandler(String route, RequestParam params)
    {
        httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }


    public void setPopupCashListFromServer(TotalBill totalBill, ArrayList<CashButtonLayout> products, Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map, ArrayList<Customer> customers, int tableNum)
    {
        CustomTextView numberBillView = (CustomTextView) myPopupView.findViewById(R.id.cash_order_number);
        numberBillView.setText("#" + (totalBill.getBillNumber() + 1));

        // set the table button number, if a table is present
        if (tableNum != -1)
        {
            myPopupView.findViewById(R.id.cash_table_not_set).setVisibility(View.GONE);
            myPopupView.findViewById(R.id.cash_table).setVisibility(View.VISIBLE);
            myPopupView.findViewById(R.id.cash_table_number).setVisibility(View.VISIBLE);
            ( (CustomTextView) myPopupView.findViewById(R.id.cash_table_number)).setText(String.valueOf(tableNum));
        }

        else
        {
            myPopupView.findViewById(R.id.cash_table_not_set).setVisibility(View.VISIBLE);
            myPopupView.findViewById(R.id.cash_table).setVisibility(View.GONE);
            myPopupView.findViewById(R.id.cash_table_number).setVisibility(View.GONE);

            ( (CustomTextView) myPopupView.findViewById(R.id.cash_table_not_set)).setText("TAKE-AWAY");
            ( (CustomTextView) myPopupView.findViewById(R.id.cash_table_not_set)).setBackgroundColor(getResources().getColor(R.color.red));
        }

        ExpandableListView expListView = (ExpandableListView) myPopupView.findViewById(R.id.cash_recyclerView);
        //set total for bill if billId exist, else set 0.0f
        double total = totalBill.getTotal();
        if (total == 0)
        {
            DecimalFormat twoDForm = new DecimalFormat("#.00");
            ((CustomTextView) myPopupView.findViewById(R.id.cash_euro_total)).setText("0,00");
        }
        else
        {
            DecimalFormat twoDForm = new DecimalFormat("#.00");
            //viewTotal.setText(twoDForm.format(total).replace(",", "."));
            ((CustomTextView) myPopupView.findViewById(R.id.cash_euro_total)).setText(twoDForm.format(total)
                                                                                              .replace(".", ","));
        }
        //set cash adapter, is the parte where you see your product and modifier on the right (your bill pratically)
        CashAdapterForOrder listAdapter = new CashAdapterForOrder(orderActivity, products, map, dbA, totalBill
                .getId(), customers, false);
        //set customer if present
        if (customers.size() > 0)
        {
            listAdapter.setCustomerList(customers);
            listAdapter.setFirstClient();
        }
        // setting list adapter
        expListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        //expand allgroups
        for (int i = 0; i < products.size(); i++)
        {
            expListView.expandGroup(i);
        }
        //listDataChild = map;
        listAdapter.notifyDataSetChanged();

        myPopupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                myPopupWindow.dismiss();
            }
        });

        myPopupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String username = intent.getStringExtra("username");
                int isAdmin = intent.getIntExtra("isAdmin", -1);

                Intent intent = new Intent(orderActivity, Operative.class);
                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.setAction("setTable");
                intent.putExtra("orderNumber", totalBill.getBillNumber());
                intent.putExtra("billId", totalBill.getId());
                intent.putExtra("userId", userId);
                intent.putExtra("userType", userType);
                intent.putExtra("tableNumber", tableNum);

                startActivity(intent);
                myPopupWindow.dismiss();
                finish();
            }
        });

        myPopupView.findViewById(R.id.go_to_payment).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String username = intent.getStringExtra("username");
                int isAdmin = intent.getIntExtra("isAdmin", -1);

                Intent intent = new Intent(orderActivity, PaymentActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.setAction("setTable");
                intent.putExtra("orderNumber", totalBill.getBillNumber() + 1);
                intent.putExtra("billId", totalBill.getId());
                intent.putExtra("userId", userId);
                intent.putExtra("userType", userType);

                int tableNumber = intent.getIntExtra("tableNumber", -1);

                intent.putExtra("tableNumber", tableNumber);

                startActivity(intent);
                myPopupWindow.dismiss();
                finish();
            }
        });

        myPopupView.findViewById(R.id.print_order_1).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (StaticValue.blackbox)
                {
                    Map<String, ArrayList<CashButtonListLayout>> test = new HashMap<String, ArrayList<CashButtonListLayout>>();
                    for (int i = 0; i < products.size(); i++)
                    {
                        test.put(String.valueOf(products.get(i)
                                                        .getPosition()), map.get(products.get(i)));
                    }

                    RequestParam params = new RequestParam();
                    Gson gson = new Gson();
                    String prods = gson.toJson(products);
                    String mods = gson.toJson(test);
                    params.add("products", prods);
                    params.add("modifiers", mods);
                    params.add("printType", String.valueOf(4));
                    params.add("billId", String.valueOf(totalBill.getId()));
                    params.add("paymentType", String.valueOf(1));
                    params.add("deviceName", String.valueOf("device"));
                    params.add("orderNumber", String.valueOf(totalBill.getBillNumber()));
                    params.add("cost", String.valueOf(totalBill.getTotal()));
                    params.add("paid", String.valueOf(totalBill.getTotal()));
                    params.add("tableNumber", String.valueOf(-1));
                    params.add("roomName", String.valueOf(""));
                    params.add("totalDiscount", String.valueOf(0.0));

                    callHttpHandler("/printItemBillNonFiscal", params);
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
                    myThread.setProducts(products);
                    myThread.setModifiers(map);
                    myThread.setPrintType(4);
                    myThread.setBillId(String.valueOf(totalBill.getId()));
                    myThread.setDeviceName("device");
                    myThread.setOrderNumberBill(String.valueOf(totalBill.getBillNumber()));
                    myThread.setCost(totalBill.getTotal());
                    myThread.setPaid(totalBill.getTotal());
                    myThread.setCredit(0.0f);
                    myThread.setPaymentType(1);
                    myThread.setTotalDiscount(0.0f);
                    myThread.setTableNumber(-1);
                    myThread.setRoomName("");
                    myThread.setClientThread();
                    myThread.setRunBaby(true);
                }


            }
        });

        myPopupView.findViewById(R.id.reprint_order).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                if (StaticValue.blackbox)
                {
                    Map<String, ArrayList<CashButtonListLayout>> test = new HashMap<String, ArrayList<CashButtonListLayout>>();
                    for (int i = 0; i < products.size(); i++)
                    {
                        test.put(String.valueOf(products.get(i)
                                                        .getPosition()), map.get(products.get(i)));
                    }

                    RequestParam params = new RequestParam();
                    Gson gson = new Gson();
                    String prods = gson.toJson(products);
                    String mods = gson.toJson(test);
                    String costum = gson.toJson(customers);
                    params.add("products", prods);
                    params.add("modifiers", mods);
                    params.add("customers", costum);
                    params.add("printType", String.valueOf(12));
                    params.add("billId", String.valueOf(totalBill.getId()));
                    params.add("paymentType", String.valueOf(1));
                    params.add("deviceName", String.valueOf("device"));
                    params.add("orderNumber", String.valueOf(totalBill.getBillNumber()));
                    params.add("cost", String.valueOf(totalBill.getTotal()));
                    params.add("paid", String.valueOf(totalBill.getTotal()));
                    params.add("tableNumber", String.valueOf(-1));
                    params.add("roomName", String.valueOf(""));
                    params.add("indexList", String.valueOf(0));

                    callHttpHandler("/reprintOrder", params);
                }
                
                else
                {
                    ClientThread myThread = ClientThread.getInstance();
                    myThread.setProducts(products);
                    myThread.setModifiers(map);
                    myThread.setPrintType(12);
                    myThread.setIP(StaticValue.IP);
                    myThread.setBillId(String.valueOf(totalBill.getId()));
                    myThread.setDeviceName("device");
                    myThread.setOrderNumberBill(String.valueOf(totalBill.getBillNumber()));
                    myThread.setCost(totalBill.getTotal());
                    myThread.setPaid(totalBill.getTotal());
                    myThread.setCustomers(customers);
                    myThread.setIndexList(0);
                    myThread.setTableNumber(-1);
                    myThread.setRoomName("");
                    myThread.setClientThread();
                    myThread.setRunBaby(true);
                }

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Hides app title **/
        getSupportActionBar().hide();
        setContentView(R.layout.activity_orders);
        dbA = new DatabaseAdapter(this);

        // dbA.showData("product_bill");
        // dbA.showData("product_unspec_bill");
        intent = this.getIntent();
        orderActivity = this;

        userId = intent.getIntExtra("userId", -1);
        userType = intent.getIntExtra("userType", -1);
        username = intent.getStringExtra("username");
        isAdmin = intent.getIntExtra("isAdmin", -1);


        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = getResources().getDisplayMetrics().density;

        take_away_recycler = (RecyclerView) findViewById(R.id.take_away_recycler);
        take_away_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        take_away_recycler.setHasFixedSize(true);

        take_away_adapter = new TakeAwayAdapter(this, dbA);

        take_away_recycler.setAdapter(take_away_adapter);

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.divider_black_for_order_v));

        take_away_recycler.addItemDecoration(divider);

        table_order_recycler = (RecyclerView) findViewById(R.id.table_order_recycler);
        table_order_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        table_order_recycler.setHasFixedSize(true);

        table_order_adapter = new TableOrderAdapter(this, dbA);

        table_order_recycler.setAdapter(table_order_adapter);

        table_order_recycler.addItemDecoration(divider);

        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent newIntent = new Intent(getApplicationContext(), Operative.class);
                int billId = intent.getIntExtra("billId", -1);
                int orderNumber;
                if (billId == -1)
                {
                    orderNumber = -1;
                }
                else
                {
                    orderNumber = intent.getIntExtra("orderNumber", -1);
                }
                String username = intent.getStringExtra("username");
                int isAdmin = intent.getIntExtra("isAdmin", -1);
                int tableNumber = intent.getIntExtra("tableNumber", -1);

                newIntent.putExtra("username", username);
                newIntent.putExtra("isAdmin", isAdmin);
                newIntent.setAction("setTable");
                newIntent.putExtra("billId", billId);
                newIntent.putExtra("orderNumber", orderNumber);
                newIntent.putExtra("tableNumber", tableNumber);
                newIntent.putExtra("userId", userId);
                newIntent.putExtra("userType", userType);
                startActivity(newIntent);
                finish();
            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent newIntent = new Intent(getApplicationContext(), Operative.class);
                int billId = intent.getIntExtra("billId", -1);
                int orderNumber = intent.getIntExtra("orderNumber", -1);
                String username = intent.getStringExtra("username");
                int isAdmin = intent.getIntExtra("isAdmin", -1);
                int tableNumber = intent.getIntExtra("tableNumber", -1);

                newIntent.putExtra("username", username);
                newIntent.putExtra("isAdmin", isAdmin);
                newIntent.setAction("setTable");
                newIntent.putExtra("billId", billId);
                //newIntent.putExtra("orderNumber", orderNumber);
                newIntent.putExtra("orderNumber", -1);
                newIntent.putExtra("tableNumber", tableNumber);
                newIntent.putExtra("userId", userId);
                newIntent.putExtra("userType", userType);
                startActivity(newIntent);
                finish();
            }
        });

        /**
         * Gestione del bottone per i RECENT ORDERS e i CURRENT ORDERS
         */
        findViewById(R.id.recent_orders).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (StaticValue.blackbox)
                {
                    if (flag)
                    {
                        RequestParam params = new RequestParam();
                        params.add("paid", String.valueOf(0));
                        callHttpHandler("/getAllOrders", params);
                    }
                    else
                    {
                        RequestParam params = new RequestParam();
                        params.add("paid", String.valueOf(1));
                        callHttpHandler("/getAllOrders", params);
                    }
                }
                else
                {
                    flag = recentOrdersOrNot(take_away_adapter, table_order_adapter, flag);
                }
            }
        });

        findViewById(R.id.search_order).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });

        resetPinpadTimer(1);

    }

    public void resetPinpadTimer(int type)
    {
        TimerManager.stopPinpadAlert();
        TimerManager.setContext(getApplicationContext());
        intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);

        intentPasscode.putExtra("isAdmin", isAdmin);
        intentPasscode.putExtra("username", username);

        TimerManager.setIntentPinpad(intentPasscode);
        TimerManager.startPinpadAlert(type);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            resetPinpadTimer(1);
            View v = getCurrentFocus();
            if (v instanceof CustomEditText)
            {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }

        }
        return super.dispatchTouchEvent(event);
    }


    public void showPaymentFromOrder(int billId, int orderNumber)
    {
        //make open new popup
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_specific_order, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable()
        {
            @Override
            public void run()
            {

                // TOOD
                // remove balckbox comm for something so simple as getting a single bill data
                if (StaticValue.blackbox)
                {
                    myPopupView = popupView;
                    myPopupWindow = popupWindow;
                    RequestParam params = new RequestParam();
                    params.add("billId", String.valueOf(billId));
                    callHttpHandler("/getBillData", params);
                }

                else
                {
                    CustomTextView numberBillView = (CustomTextView) popupView.findViewById(R.id.cash_order_number);
                    numberBillView.setText("#" + orderNumber);

                    ExpandableListView expListView = (ExpandableListView) popupView.findViewById(R.id.cash_recyclerView);
                    ArrayList<Customer> listDataCustomer = new ArrayList<Customer>();
                    ArrayList<CashButtonLayout> listDataHeader = new ArrayList<CashButtonLayout>();
                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> listDataChild = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();


                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = dbA.getBillData(billId, context);
                    //set total for bill if billId exist, else set 0.0f
                    double total = dbA.getOnlyBillPrice(billId);
                    if (total == 0)
                    {
                        DecimalFormat twoDForm = new DecimalFormat("#.00");
                        ((CustomTextView) popupView.findViewById(R.id.cash_euro_total)).setText("0,00");
                    }
                    else
                    {
                        DecimalFormat twoDForm = new DecimalFormat("#.00");
                        //viewTotal.setText(twoDForm.format(total).replace(",", "."));
                        ((CustomTextView) popupView.findViewById(R.id.cash_euro_total)).setText(twoDForm
                                .format(total)
                                .replace(".", ","));
                    }
                    listDataChild = map;
                    listDataHeader = new ArrayList<>(map.keySet());
                    //set cash adapter, is the parte where you see your product and modifier on the right (your bill pratically)
                    CashAdapterForOrder listAdapter = new CashAdapterForOrder(orderActivity, listDataHeader, listDataChild, dbA, billId, listDataCustomer, false);
                    //set customer if present
                    listDataCustomer = dbA.getCustomerData(billId);
                    if (listDataCustomer.size() > 0)
                    {
                        listAdapter.setCustomerList(listDataCustomer);
                        listAdapter.setFirstClient();
                    }
                    // setting list adapter
                    expListView.setAdapter(listAdapter);
                    listAdapter.notifyDataSetChanged();

                    //expand allgroups
                    for (int i = 0; i < listDataHeader.size(); i++)
                    {
                        expListView.expandGroup(i);
                    }
                    //listDataChild = map;
                    final ArrayList<Customer> listCustomer = listDataCustomer;
                    final ArrayList<CashButtonLayout> listHeader = listDataHeader;
                    final Map<CashButtonLayout, ArrayList<CashButtonListLayout>> listChild = listDataChild;
                    final double totalFinal = total;
                    listAdapter.notifyDataSetChanged();

                    popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            popupWindow.dismiss();
                        }
                    });

                    popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {


                            String username = intent.getStringExtra("username");
                            int isAdmin = intent.getIntExtra("isAdmin", -1);


                            Intent intent = new Intent(orderActivity, Operative.class);
                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.setAction("setTable");
                            intent.putExtra("orderNumber", orderNumber - 1);
                            intent.putExtra("billId", billId);
                            intent.putExtra("userId", userId);
                            intent.putExtra("userType", userType);

                            int tableNumber = intent.getIntExtra("tableNumber", -1);

                            intent.putExtra("tableNumber", tableNumber);

                            startActivity(intent);
                            popupWindow.dismiss();
                            finish();
                        }
                    });

                    popupView.findViewById(R.id.go_to_payment)
                             .setOnClickListener(new View.OnClickListener()
                             {
                                 @Override
                                 public void onClick(View v)
                                 {


                                     String username = intent.getStringExtra("username");
                                     int isAdmin = intent.getIntExtra("isAdmin", -1);


                                     Intent intent = new Intent(orderActivity, PaymentActivity.class);
                                     intent.putExtra("username", username);
                                     intent.putExtra("isAdmin", isAdmin);
                                     intent.setAction("setTable");
                                     intent.putExtra("orderNumber", orderNumber);
                                     intent.putExtra("billId", billId);
                                     intent.putExtra("userId", userId);
                                     intent.putExtra("userType", userType);

                                     int tableNumber = intent.getIntExtra("tableNumber", -1);

                                     intent.putExtra("tableNumber", tableNumber);

                                     startActivity(intent);
                                     popupWindow.dismiss();
                                     finish();
                                 }
                             });

                    popupView.findViewById(R.id.print_order_1)
                             .setOnClickListener(new View.OnClickListener()
                             {
                                 @Override
                                 public void onClick(View v)
                                 {


                                     if (StaticValue.blackbox)
                                     {
                                         Map<String, ArrayList<CashButtonListLayout>> test = new HashMap<String, ArrayList<CashButtonListLayout>>();
                                         for (int i = 0; i < listHeader.size(); i++)
                                         {
                                             test.put(String.valueOf(listHeader.get(i)
                                                                               .getPosition()), map.get(listHeader
                                                     .get(i)));
                                         }

                                         RequestParam params = new RequestParam();
                                         Gson gson = new Gson();
                                         String prods = gson.toJson(listHeader);
                                         String mods = gson.toJson(test);
                                         params.add("products", prods);
                                         params.add("modifiers", mods);
                                         params.add("printType", String.valueOf(4));
                                         params.add("billId", String.valueOf(totalFinal));
                                         params.add("paymentType", String.valueOf(1));
                                         params.add("deviceName", String.valueOf("device"));
                                         params.add("orderNumber", String.valueOf(orderNumber));
                                         params.add("cost", String.valueOf(totalFinal));
                                         params.add("paid", String.valueOf(totalFinal));
                                         params.add("tableNumber", String.valueOf(-1));
                                         params.add("roomName", String.valueOf(""));
                                         params.add("totalDiscount", String.valueOf(0.0));

                                         callHttpHandler("/printItemBillNonFiscal", params);


                                     }
                                     else
                                     {
                                         int roomId = dbA.getRoomId(billId);
                                         Room room = dbA.fetchRoomById(roomId);
                                         if (StaticValue.printerName.equals("ditron"))
                                         {
                                             PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                             ditron.closeAll();
                                             ditron.startSocket();
                                         }
                                         ClientThread myThread = ClientThread.getInstance();
                                         myThread.setProducts(listHeader);
                                         myThread.setModifiers(listChild);
                                         myThread.setPrintType(4);
                                         myThread.setBillId(String.valueOf(billId));
                                         myThread.setDeviceName("device");
                                         myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                         myThread.setCost(totalFinal);
                                         myThread.setPaid(totalFinal);
                                         myThread.setCredit(0.0f);
                                         myThread.setPaymentType(1);
                                         myThread.setTotalDiscount(0.0f);
                                         myThread.setTableNumber(-1);
                                         myThread.setRoomName("");
                                         myThread.setClientThread();
                                         myThread.setRunBaby(true);
                                     }


                                 }
                             });

                    popupView.findViewById(R.id.reprint_order)
                             .setOnClickListener(new View.OnClickListener()
                             {
                                 @Override
                                 public void onClick(View v)
                                 {


                                     ClientThread myThread = ClientThread.getInstance();
                                     myThread.setProducts(listHeader);
                                     myThread.setModifiers(listChild);
                                     myThread.setPrintType(12);
                                     myThread.setIP(StaticValue.IP);
                                     myThread.setBillId(String.valueOf(billId));
                                     myThread.setDeviceName("device");
                                     myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                     myThread.setCost(totalFinal);
                                     myThread.setPaid(totalFinal);
                                     myThread.setCustomers(listCustomer);
                                     myThread.setIndexList(0);
                                     myThread.setTableNumber(-1);
                                     myThread.setRoomName("");
                                     myThread.setClientThread();
                                     myThread.setRunBaby(true);

                                 }
                             });
                }


            }
        });
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }

    public void showPaymentFromOrderWithTable(int billId, int orderNumber, int tableNumber)
    {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_specific_order, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        popupView.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (StaticValue.blackbox)
                {
                    myPopupView = popupView;
                    myPopupWindow = popupWindow;
                    RequestParam params = new RequestParam();
                    params.add("billId", String.valueOf(billId));
                    callHttpHandler("/getBillData", params);
                }

                else
                {
                    CustomTextView numberBillView = (CustomTextView) popupView.findViewById(R.id.cash_order_number);
                    numberBillView.setText("#" + orderNumber);

                    CustomTextView cash_table_number = (CustomTextView) popupView.findViewById(R.id.cash_table_number);
                    cash_table_number.setText(String.valueOf(tableNumber));

                    ExpandableListView expListView = (ExpandableListView) popupView.findViewById(R.id.cash_recyclerView);
                    ArrayList<Customer> listDataCustomer = new ArrayList<Customer>();
                    ArrayList<CashButtonLayout> listDataHeader = new ArrayList<CashButtonLayout>();
                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> listDataChild = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

                    // dbA.showData("product_unspec_bill");

                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = dbA.getBillData(billId, context);
                    //set total for bill if billId exist, else set 0.0f
                    double total = dbA.getOnlyBillPrice(billId);
                    if (total == 0)
                    {
                        DecimalFormat twoDForm = new DecimalFormat("#.00");
                        ((CustomTextView) popupView.findViewById(R.id.cash_euro_total)).setText("0,00");
                    }
                    else
                    {
                        DecimalFormat twoDForm = new DecimalFormat("#.00");
                        //viewTotal.setText(twoDForm.format(total).replace(",", "."));
                        ((CustomTextView) popupView.findViewById(R.id.cash_euro_total)).setText(twoDForm
                                .format(total)
                                .replace(".", ","));
                    }
                    listDataChild = map;
                    listDataHeader = new ArrayList<>(map.keySet());
                    //set cash adapter, is the parte where you see your product and modifier on the right (your bill pratically)
                    CashAdapterForOrder listAdapter = new CashAdapterForOrder(orderActivity, listDataHeader, listDataChild, dbA, billId, listDataCustomer, false);
                    //set customer if present
                    listDataCustomer = dbA.getCustomerData(billId);
                    if (listDataCustomer.size() > 0)
                    {
                        listAdapter.setCustomerList(listDataCustomer);
                        listAdapter.setFirstClient();
                    }
                    // setting list adapter
                    expListView.setAdapter(listAdapter);
                    listAdapter.notifyDataSetChanged();

                    //expand allgroups
                    for (int i = 0; i < listDataHeader.size(); i++)
                    {
                        expListView.expandGroup(i);
                    }
                    //listDataChild = map;
                    final ArrayList<Customer> listCustomer = listDataCustomer;
                    final ArrayList<CashButtonLayout> listHeader = listDataHeader;
                    final Map<CashButtonLayout, ArrayList<CashButtonListLayout>> listChild = listDataChild;
                    final double totalFinal = total;
                    listAdapter.notifyDataSetChanged();

                    popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            popupWindow.dismiss();
                        }
                    });


                    popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            String username = intent.getStringExtra("username");
                            int isAdmin = intent.getIntExtra("isAdmin", -1);

                            Intent intent = new Intent(orderActivity, Operative.class);
                            intent.putExtra("tableNumber", tableNumber);
                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.setAction("setTable");
                            intent.putExtra("orderNumber", orderNumber - 1);
                            intent.putExtra("billId", billId);
                            intent.putExtra("userId", userId);
                            intent.putExtra("userType", userType);

                            startActivity(intent);
                            popupWindow.dismiss();
                            finish();
                        }
                    });


                    popupView.findViewById(R.id.go_to_payment)
                             .setOnClickListener(new View.OnClickListener()
                             {
                                 @Override
                                 public void onClick(View v)
                                 {
                                     username = intent.getStringExtra("username");
                                     int isAdmin = intent.getIntExtra("isAdmin", -1);


                                     Intent intent = new Intent(orderActivity, PaymentActivity.class);
                                     intent.putExtra("username", username);
                                     intent.putExtra("isAdmin", isAdmin);
                                     intent.setAction("setTable");
                                     intent.putExtra("orderNumber", orderNumber);
                                     intent.putExtra("billId", billId);
                                     intent.putExtra("userId", userId);
                                     intent.putExtra("userType", userType);
                                     intent.putExtra("tableNumber", tableNumber);

                                     startActivity(intent);
                                     popupWindow.dismiss();
                                     finish();
                                 }
                             });


                    popupView.findViewById(R.id.print_order_1)
                             .setOnClickListener(new View.OnClickListener()
                             {
                                 @Override
                                 public void onClick(View v)
                                 {
                                     if (StaticValue.blackbox)
                                     {
                                         Map<String, ArrayList<CashButtonListLayout>> test = new HashMap<String, ArrayList<CashButtonListLayout>>();
                                         for (int i = 0; i < listHeader.size(); i++)
                                         {
                                             test.put(String.valueOf(listHeader.get(i)
                                                                               .getPosition()), map.get(listHeader
                                                     .get(i)));
                                         }

                                         RequestParam params = new RequestParam();
                                         Gson gson = new Gson();
                                         String prods = gson.toJson(listHeader);
                                         String mods = gson.toJson(test);
                                         params.add("products", prods);
                                         params.add("modifiers", mods);
                                         params.add("printType", String.valueOf(4));
                                         params.add("billId", String.valueOf(totalFinal));
                                         params.add("paymentType", String.valueOf(1));
                                         params.add("deviceName", String.valueOf("device"));
                                         params.add("orderNumber", String.valueOf(orderNumber));
                                         params.add("cost", String.valueOf(totalFinal));
                                         params.add("paid", String.valueOf(totalFinal));
                                         params.add("totalDiscount", String.valueOf(0.0));
                                         params.add("tableNumber", String.valueOf(-1));
                                         params.add("roomName", String.valueOf(""));

                                         callHttpHandler("/printItemBillNonFiscal", params);
                                     }
                                     else
                                     {

                                         int roomId = dbA.getRoomId(billId);
                                         Room room = dbA.fetchRoomById(roomId);
                                         if (StaticValue.printerName.equals("ditron"))
                                         {
                                             PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                                             ditron.closeAll();
                                             ditron.startSocket();
                                         }
                                         ClientThread myThread = ClientThread.getInstance();
                                         myThread.setProducts(listHeader);
                                         myThread.setModifiers(listChild);
                                         myThread.setPrintType(4);
                                         myThread.setBillId(String.valueOf(billId));
                                         myThread.setDeviceName("device");
                                         myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                         myThread.setCost(totalFinal);
                                         myThread.setPaid(totalFinal);
                                         myThread.setCredit(0.0f);
                                         myThread.setPaymentType(1);
                                         myThread.setTotalDiscount(0.0f);
                                         myThread.setTableNumber(tableNumber);
                                         myThread.setRoomName(room.getName());
                                         myThread.setClientThread();
                                         myThread.setRunBaby(true);
                                     }

                                 }
                             });


                    popupView.findViewById(R.id.reprint_order)
                             .setOnClickListener(new View.OnClickListener()
                             {
                                 @Override
                                 public void onClick(View v)
                                 {
                                     ClientThread myThread = ClientThread.getInstance();
                                     myThread.setProducts(listHeader);
                                     myThread.setModifiers(listChild);
                                     myThread.setPrintType(12);
                                     myThread.setIP(StaticValue.IP);
                                     myThread.setBillId(String.valueOf(billId));
                                     myThread.setDeviceName("device");
                                     myThread.setOrderNumberBill(String.valueOf(orderNumber));
                                     myThread.setCost(totalFinal);
                                     myThread.setPaid(totalFinal);
                                     myThread.setCustomers(listCustomer);
                                     myThread.setIndexList(0);
                                     myThread.setTableNumber(tableNumber);
                                     int roomId = dbA.getRoomId(billId);
                                     Room room = dbA.fetchRoomById(roomId);
                                     if (room.getId() > 0)
                                     { myThread.setRoomName(room.getName()); }
                                     else
                                     { myThread.setRoomName(""); }

                                     myThread.setClientThread();
                                     myThread.setRunBaby(true);

                                 }
                             });

                }

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    /**
     * (Added by Fabrizio)
     * metodo per passare dai current orders ai recent orders
     *
     * @param take_away_adapter
     * @param table_order_adapter
     * @param flag
     * @return
     */
    public boolean recentOrdersOrNot(TakeAwayAdapter take_away_adapter, TableOrderAdapter table_order_adapter, boolean flag)
    {

        if (!flag)
        {

            //setta gli ordini sul take away
            take_away_adapter.setPaidBillsList();
            take_away_adapter.notifyDataSetChanged();

            //setta gli ordini sui tables
            table_order_adapter.setPaidTables();
            table_order_adapter.notifyDataSetChanged();

            CustomButton currentOrdersButton = (CustomButton) findViewById(R.id.recent_orders);
            currentOrdersButton.setText(R.string.current_orders);
            flag = true;
        }
        else if (flag)
        {

            //setta gli ordini sui take away
            take_away_adapter.setBillsList();
            take_away_adapter.notifyDataSetChanged();

            //setta gli ordini sui tables
            table_order_adapter.setCurrentTables();
            table_order_adapter.notifyDataSetChanged();

            CustomButton recentOrdersButton = (CustomButton) findViewById(R.id.recent_orders);
            recentOrdersButton.setText(R.string.recent_orders);
            flag = false;
        }

        return flag;
    }

    @Override
    public void onTaskEndWithResult(String success)
    {

    }

    @Override
    public void onTaskFinishGettingData(String result)
    {

    }


}

