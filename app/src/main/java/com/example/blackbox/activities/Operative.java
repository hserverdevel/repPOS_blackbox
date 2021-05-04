package com.example.blackbox.activities;


import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.TimerClass;
import com.example.blackbox.adapter.OModifierAdapter;
import com.example.blackbox.adapter.ReservationsAdapter;
import com.example.blackbox.adapter.SessionAdapter;
import com.example.blackbox.adapter.UserAdapter;
import com.example.blackbox.client.ClientThread;
import com.example.blackbox.fragments.ActivityCommunicator;
import com.example.blackbox.fragments.CashFragment;
import com.example.blackbox.fragments.ModifierFragment;
import com.example.blackbox.fragments.OperativeFragment;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.CashManagement;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.Reservation;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.model.TotalBill;
import com.example.blackbox.model.User;
import com.example.blackbox.printer.PrinterDitronThread;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.example.blackbox.server.Server;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


public class Operative extends FragmentActivity implements
                                            ActivityCommunicator,
                                            SessionAdapter.AdapterSessionCallback ,
                                            UserAdapter.AdapterUserCallback,
                                            ClientThread.TaskDelegate ,
                                            HttpHandler.AsyncResponse {

    private String TAG = "<Operative>";

    OperativeFragment operativeFragment;
    CashFragment cashFragment;
    ModifierFragment modifierFragment;
    private String username;
    private int billId;
    public void setBillId(int bill){billId = bill;}
    private int isAdmin;
    public float density;
    public float dpHeight;
    public float dpWidth;
    private Context me;
    private SessionAdapter sessionAdapter;
    private DatabaseAdapter dbA;
    private boolean keyboard_next_flag = false;
    int customer = -1;
    int tableNumber = -1;
    private String activityAssignedValue ="";
    private int orderNumber = -1;
    private UserAdapter userAdapter;
    private int userType;
    private int userId;
    private String email = "";
    public String IP = StaticValue.IP;
    public boolean isErrorShow = false;

    private boolean clientLongClick = false;
    private TimerClass timer;
    Operative forClient = null;
    private Intent intentPasscode;
    private String passcode = "";
    private int red = Color.parseColor("#cd0046");
    private int black = Color.parseColor("#DD000000");

    private HttpHandler httpHandler;
    private View myPopupView;
    private PopupWindow myPopupWindow;

    public void resetPinpadTimer(int type){
        TimerManager.stopPinpadAlert();
        TimerManager.setContext(getApplicationContext());
        intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);

        intentPasscode.putExtra("isAdmin", isAdmin);
        intentPasscode.putExtra("username", username);

        TimerManager.setIntentPinpad(intentPasscode);
        TimerManager.startPinpadAlert(type);
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();

        Intent intent = new Intent(getApplicationContext(), SplashScreen1.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUserInteraction() {
        // TODO Auto-generated method stub
        super.onUserInteraction();
        //TimerManager.startPinpadAlert();
        //TimerManager.startLogoutAlert();
     }

    @Override
    public void onDestroy() {

         super.onDestroy();

     }


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
                    case "insertUser":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            User myUser = User.fromJson(jObject);
                            dbA.insertUserFromServer(myUser);
                            myPopupView.findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                            myPopupView.findViewById(R.id.AddUserWindow).setVisibility(View.GONE);

                            setupAdminWindow(myPopupView, myPopupWindow);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "updateUser":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            User myUser = User.fromJson(jObject);
                            String oldPassword = jsonObject.getString("oldPassword");
                            dbA.updateUserByPasscode(myUser.getName(), myUser.getSurname(), myUser.getPasscode(), myUser.getEmail(), myUser.getUserRole(), myUser.getId(), oldPassword);
                            setupNewUserWindow(myPopupView, myPopupWindow);
                        } else {
                            Toast.makeText(getApplicationContext(), "passcode already in use", Toast.LENGTH_SHORT).show();
                        }

                    case "getLastBillNumber":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            int maxNumber = jsonObject.getInt("numberOrder");
                            cashFragment.setOrderNumberFromServer(maxNumber);
                        } else {
                            Toast.makeText(getApplicationContext(), "error in save Bill ", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case "saveBill":
                        check = jsonObject.getBoolean("check");
                        int numberBill = jsonObject.getInt("numberOrder");
                        int billId = jsonObject.getInt("billId");
                        RelativeLayout saveButton = (RelativeLayout) findViewById(R.id.layout_3);
                        saveButton.setEnabled(true);
                        if (check) {
                            String fromWhere = jsonObject.getString("from");
                            switch (fromWhere) {
                                case "saveButton":
                                    int maxNumber = jsonObject.getInt("numberOrder");
                                    if (maxNumber == -1)
                                        maxNumber = 0;
                                    cashFragment.resetListForSaveFromServer(maxNumber);
                                    break;
                                case "paymentButton":
                                    cashFragment.goToPaymentFromServer(numberBill, billId);
                                    break;
                                case "tableButton":
                                    cashFragment.goToTableFromServer(numberBill, billId);
                                    break;
                                case "customerPopup":

                                    Intent intent = new Intent(Operative.this, ClientsActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("isAdmin", isAdmin);
                                    intent.putExtra("billId", billId);
                                    intent.putExtra("orderNumber", (numberBill - 1));
                                    intent.putExtra("clientLongClick", clientLongClick);
                                    intent.setAction("selectCustomer");
                                    intent.putExtra("currentCustomer", -1);
                                    intent.putExtra("customerModify", false);
                                    intent.putExtra("modifyPosition", -1);
                                    intent.putExtra("cashListIndex", cashFragment.getCashListIndex());
                                    intent.putExtra("userId", userId);
                                    intent.putExtra("userType", userType);
                                    intent.putExtra("tableNumber", tableNumber);
                                    startActivity(intent);
                                    finish();

                                    break;

                                default:
                                    break;
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "error in save Bill ", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case "saveBillForCustomer":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            int orderNumber = jsonObject.getInt("orderNumber");
                            int billId1 = jsonObject.getInt("billId");
                            int customer_id = jsonObject.getInt("customerId");
                            boolean modify = jsonObject.getBoolean("modify");
                            int modifyPosition = jsonObject.getInt("modifyPosition");

                            Intent intent = new Intent(Operative.this, ClientsActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.putExtra("billId", billId1);
                            intent.putExtra("orderNumber", orderNumber);
                            intent.putExtra("clientLongClick", clientLongClick);
                            intent.setAction("selectCustomer");
                            intent.putExtra("currentCustomer", customer_id);
                            intent.putExtra("customerModify", modify);
                            intent.putExtra("modifyPosition", modifyPosition);
                            intent.putExtra("cashListIndex", cashFragment.getCashListIndex());
                            intent.putExtra("userId", userId);
                            intent.putExtra("userType", userType);
                            intent.putExtra("tableNumber", tableNumber);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "error in save Bill ", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "getBillData":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONObject tbArray = new JSONObject(output).getJSONObject("totalBill");
                            JSONArray productArray = new JSONObject(output).getJSONArray("products");
                            JSONArray modifierArray = new JSONObject(output).getJSONArray("modifiers");
                            JSONArray customerArray = new JSONObject(output).getJSONArray("customers");

                            TotalBill totals = TotalBill.fromJson(tbArray);
                            ArrayList<CashButtonLayout> products = CashButtonLayout.fromJsonArray(productArray);
                            ArrayList<CashButtonListLayout> modifiers = CashButtonListLayout.fromJsonArray(modifierArray);
                            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                            for (CashButtonLayout product : products) {
                                map.put(product, product.getCashList());
                            }

                            ArrayList<Customer> customers = Customer.fromJsonArray(customerArray);
                            setCashListFromServer(totals, products, map, customers);

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "saveBillFromPopup":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            myPopupWindow.dismiss();
                            int mybillId = jsonObject.getInt("billId");
                            int code = jsonObject.getInt("code");
                            switch (code) {
                                case 1:
                                    fireUserWindow();
                                    break;
                                case 2:
                                    Intent intent2 = new Intent(Operative.this, ClientsActivity.class);
                                    intent2.putExtra("username", username);
                                    intent2.putExtra("isAdmin", isAdmin);
                                    // intent2.putExtra("billId", mybillId );
                                    intent2.putExtra("userId", userId);
                                    intent2.putExtra("userType", userType);
                                    intent2.putExtra("tableNumber", tableNumber);
                                    intent2.putExtra("orderNumber", orderNumber);
                                    intent2.setAction("clientsFromOperative");
                                    startActivity(intent2);
                                    finish();
                                    break;
                                case 3:
                                    Intent intent = new Intent(Operative.this, OrderActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("isAdmin", isAdmin);
                                    intent.putExtra("userId", userId);
                                    intent.putExtra("userType", userType);
                                    intent.putExtra("tableNumber", tableNumber);
                                    intent.putExtra("orderNumber", orderNumber);
                                    intent.setAction("ordersFromOperative");
                                    startActivity(intent);
                                    finish();
                                    break;
                                case 4:
                                    Intent intent4 = new Intent(Operative.this, OverviewActivity.class);
                                    intent4.putExtra("username", username);
                                    intent4.putExtra("isAdmin", isAdmin);
                                    intent4.putExtra("userId", userId);
                                    intent4.putExtra("userType", userType);
                                    intent4.putExtra("tableNumber", tableNumber);
                                    intent4.putExtra("orderNumber", orderNumber);
                                    startActivity(intent4);
                                    finish();
                                    break;
                                case 5:
                                    Intent intent5 = new Intent(Operative.this, ReservationsActivity.class);
                                    intent5.putExtra("username", username);
                                    intent5.putExtra("isAdmin", isAdmin);
                                    intent5.putExtra("userId", userId);
                                    intent5.putExtra("userType", userType);
                                    intent5.putExtra("tableNumber", tableNumber);
                                    intent5.putExtra("orderNumber", orderNumber);
                                    startActivity(intent5);
                                    finish();
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "payBillFromOperative":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            int maxNumber = jsonObject.getInt("numberOrder");
                            cashFragment.resetListForSaveFromServer(maxNumber);
                            cashFragment.myPopupWindow.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), "error in save Bill ", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case "checkIfBillSplitPaid":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            boolean exist = jsonObject.getBoolean("exist");
                            if (!exist) {
                                int groupPosition = jsonObject.getInt("groupPosition");
                                cashFragment.deleteAllProductsFromServer(groupPosition);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.part_of_this_bill_is_already_paid, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "error in checkIfBillSplitPaid ", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case "azzeramentoScontrini":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            Intent intent = new Intent(getApplicationContext(), SplashScreen1.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "error in azzeramentoOrdini ", Toast.LENGTH_SHORT).show();

                        }
                        break;

                    case "chiusuraCassa":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            Intent intent = new Intent(getApplicationContext(), SplashScreen1.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "esistono scontrini aperti", Toast.LENGTH_SHORT).show();

                        }
                        break;

                    case "getFavouritesButtons":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONArray buttonsArray = new JSONObject(output).getJSONArray("buttons");
                            ArrayList<ButtonLayout> buttons = ButtonLayout.fromJsonArray(buttonsArray);
                            operativeFragment.selectFavourites(buttons);
                        } else {
                            Toast.makeText(getApplicationContext(), "errore in favourites", Toast.LENGTH_SHORT).show();

                        }
                        break;

                    case "printSingleOrder":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONObject tbArray = new JSONObject(output).getJSONObject("totalBill");

                            JSONArray productArray = new JSONObject(output).getJSONArray("products");
                            JSONArray modifierArray = new JSONObject(output).getJSONArray("modifiers");
                            JSONArray customerArray = new JSONObject(output).getJSONArray("customers");

                            TotalBill totals = TotalBill.fromJson(tbArray);
                            ArrayList<CashButtonLayout> products = CashButtonLayout.fromJsonArray(productArray);
                            ArrayList<CashButtonListLayout> modifiers = CashButtonListLayout.fromJsonArray(modifierArray);
                            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                            for (CashButtonLayout product : products) {
                                map.put(product, product.getCashList());
                            }

                            ArrayList<Customer> customers = Customer.fromJsonArray(customerArray);
                            setCashListFromServer(totals, products, map, customers);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
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

    public void callHttpHandler(String route, List<NameValuePair> params) {
        httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }

    public void setCashListFromServer(TotalBill totalBill, ArrayList<CashButtonLayout> products,  Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map,  ArrayList<Customer> customers ) {
     cashFragment.setCashListFromServer(totalBill, products,  map,  customers );
    }

    public void selectFavourites() {
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        callHttpHandler("/getFavouritesButtons", params);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operative);
//        TimerManager.startPinpadAlert();
//        TimerManager.startLogoutAlert();
        Intent intent = getIntent();
        me = this;

        dbA = new DatabaseAdapter(me);

        /** Sets current user on top right button**/
        username = intent.getStringExtra("username");
        userId = intent.getIntExtra("userId", -1);
        userType = intent.getIntExtra("userType", -1);

        isAdmin = intent.getIntExtra("isAdmin", -1);
        billId = intent.getIntExtra("billId", -1);
        customer = intent.getIntExtra("customer", -1);
        orderNumber = intent.getIntExtra("orderNumber", -1);
        email = intent.getStringExtra("email");

        forClient = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        /** Control the three dots on the operative activity, that display the status of {printer, blackbox, wifi} **/

        findViewById(R.id.check_printer).setActivated(StaticValue.printerOn);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        findViewById(R.id.check_wi_fi).setActivated(mWifi.isConnected());

        findViewById(R.id.check_blackbox).setActivated(StaticValue.blackbox);


        ((CustomTextView)findViewById(R.id.admin_button_tv)).setText(R.string.user);
        User myUser = dbA.getUserByUsername(username);
        if(myUser.getName()!=null) {
            if(myUser.getName().length()>=1) {
                if (myUser.getSurname() != null) {
                    if (myUser.getSurname().length() >= 1) {
                        String surname = myUser.getSurname().substring(0, 1).toUpperCase() + ".";
                        String name = myUser.getName().substring(0, 1).toUpperCase() + myUser.getName().substring(1);

                        ((CustomTextView) findViewById(R.id.admin_username_button_tv)).setText(name + " " + surname);
                    } else {
                        String name = myUser.getName().substring(0, 1).toUpperCase() + myUser.getName().substring(1);
                        ((CustomTextView) findViewById(R.id.admin_username_button_tv)).setText(name);
                    }
                } else {
                    String name = myUser.getName().substring(0, 1).toUpperCase() + myUser.getName().substring(1);
                    ((CustomTextView) findViewById(R.id.admin_username_button_tv)).setText(name);
                }
            }else
                ((CustomTextView) findViewById(R.id.admin_username_button_tv)).setText(R.string.user);

        }


        findViewById(R.id.cashier).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                cashFragment.deleteBill();
                return false;
            }
        });


        findViewById(R.id.admin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                    Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                }else {
                    if (cashFragment.getListDataHeader() == 0) {
                        fireUserWindow();
                    } else {
                        openSaveBillPopup(1);
                    }
                }
            }
        });


        findViewById(R.id.cashier).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cashFragment.getListDataHeader() == 0 && (dbA.getPaidBill(billId) != 1 || billId == -1) ){
                    Intent intent = new Intent(Operative.this, OverviewActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    // intent.putExtra("billId", billId);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userType", userType);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("orderNumber", orderNumber);
                    startActivity(intent);
                    finish();
                }
                else{
                    if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                        Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                    } else {
                        openSaveBillPopup(4);
                    }
                }
            }
        });

        findViewById(R.id.clients).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cashFragment.getListDataHeader() == 0 && (dbA.getPaidBill(billId) != 1 || billId == -1)) {
                    if(cashFragment.getListDataHeader()==0  && billId>0){
                        dbA.deleteBillData1(billId, getApplicationContext());
                    }

                    Intent intent = new Intent(Operative.this, ClientsActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.putExtra("billId", billId);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userType", userType);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("orderNumber", orderNumber);
                    intent.setAction("clientsFromOperative");
                    startActivity(intent);
                    finish();
                } else {
                    if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                        Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                    } else {
                        openSaveBillPopup(2);
                    }
                }
            }
        });

        findViewById(R.id.orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cashFragment.getListDataHeader()==0 && (dbA.getPaidBill(billId) != 1 || billId == -1)) {
                    //go directly to orders
                    dbA.showData("bill_total");
                    if(cashFragment.getListDataHeader()==0  && billId>0){
                        dbA.deleteBillData1(billId, getApplicationContext());
                    }
                    Intent intent = new Intent(Operative.this, OrderActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userType", userType);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("orderNumber", orderNumber);
                    intent.setAction("ordersFromOperative");
                    startActivity(intent);
                    finish();
                }else{
                    if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                        Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                    }
                    else { openSaveBillPopup(3); }
                }
             }
        });

        findViewById(R.id.reservations).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cashFragment.getListDataHeader()==0 && (dbA.getPaidBill(billId) != 1 || billId == -1)) {

                    dbA.showData("bill_total");
                    if(cashFragment.getListDataHeader()==0  && billId>0){
                        dbA.deleteBillData1(billId, getApplicationContext());
                    }
                    Intent intent = new Intent(Operative.this, ReservationsActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userType", userType);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("orderNumber", orderNumber);
                    startActivity(intent);
                    finish();
                } else {
                    if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                        Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                    }
                    else { openSaveBillPopup(5); }
                }
            }
        });


        findViewById(R.id.open_cash_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StaticValue.blackbox){
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    callHttpHandler("/openCashDrawer", params);
                }else {
                    if (StaticValue.printerName.equals("ditron")) {
                        PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                        ditron.closeAll();
                        ditron.startSocket();
                    }

                    ClientThread myThread = ClientThread.getInstance();
                    myThread.delegate = forClient;
                    myThread.setPrintType(14);
                    myThread.setIP(IP);

                    myThread.setClientThread();
                    myThread.setRunBaby(true);
                }

                openPinpad();
            }
        });


        if(StaticValue.showProducts){
            findViewById(R.id.showMainMenu).setBackgroundResource(R.drawable.open_drawer);
            findViewById(R.id.showProducts).setBackgroundResource(R.drawable.selected_button_footer);
            findViewById(R.id.showFavourites).setBackgroundResource(R.drawable.open_drawer);
        } else if (StaticValue.showFavourites) {
            findViewById(R.id.showMainMenu).setBackgroundResource(R.drawable.open_drawer);
            findViewById(R.id.showProducts).setBackgroundResource(R.drawable.open_drawer);
            findViewById(R.id.showFavourites).setBackgroundResource(R.drawable.selected_button_footer);
        } else {
            findViewById(R.id.showMainMenu).setBackgroundResource(R.drawable.selected_button_footer);
            findViewById(R.id.showProducts).setBackgroundResource(R.drawable.open_drawer);
            findViewById(R.id.showFavourites).setBackgroundResource(R.drawable.open_drawer);
        }

        findViewById(R.id.showMainMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                    Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                }else {
                    exitModify();
                    goToMainPage();
                    findViewById(R.id.showMainMenu).setBackgroundResource(R.drawable.selected_button_footer);
                    findViewById(R.id.showProducts).setBackgroundResource(R.drawable.open_drawer);
                    findViewById(R.id.showFavourites).setBackgroundResource(R.drawable.open_drawer);
                    StaticValue.setShowMainMenu();

                    operativeFragment.setTypeOfFavourites();
                }

            }
        });

        findViewById(R.id.showProducts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                    Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                }else {
                    exitModify();
                    goToMainPage();
                    findViewById(R.id.showMainMenu).setBackgroundResource(R.drawable.open_drawer);
                    findViewById(R.id.showProducts).setBackgroundResource(R.drawable.selected_button_footer);
                    findViewById(R.id.showFavourites).setBackgroundResource(R.drawable.open_drawer);
                    StaticValue.setShowProducts();

                    operativeFragment.setTypeOfFavourites();
                }
            }
        });

        findViewById(R.id.showFavourites).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
                    Toast.makeText(me, "Please finish to modify", Toast.LENGTH_SHORT).show();
                }else {
                    exitModify();
                    goToMainPage();
                    ((View) findViewById(R.id.showMainMenu)).setBackgroundResource(R.drawable.open_drawer);
                    ((View) findViewById(R.id.showProducts)).setBackgroundResource(R.drawable.open_drawer);
                    ((View) findViewById(R.id.showFavourites)).setBackgroundResource(R.drawable.selected_button_footer);

                    StaticValue.setShowFavourites();
                    operativeFragment.setTypeOfFavourites();
                }

            }
        });

        operativeFragment = new OperativeFragment();
        cashFragment = new CashFragment();
        modifierFragment = new ModifierFragment();
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        if(!operativeFragment.isAdded()) {
            transaction.add(R.id.operative_fragment, operativeFragment);
        }

        operativeFragment.setIsModify(false);

        transaction.add(R.id.cash_fragment, cashFragment);
        transaction.commit();

        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        density  = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;

        if(email != null)
            cashFragment.setEmail(email);

        if(timer == null){
            timer = new TimerClass(dbA, me, forClient);
            timer.launchTimer();
        }
        else{
            if(!timer.getIsRunning()){
                timer = new TimerClass(dbA, me, forClient);
                timer.launchTimer();
            }
        }

        TimerManager.setContext(getApplicationContext());
        intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);
        intentPasscode.putExtra("isAdmin", isAdmin);
        intentPasscode.putExtra("username", username);

        TimerManager.setIntentPinpad(intentPasscode);
        TimerManager.startPinpadAlert(1);

        final Handler ha=new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                //call function
                //Server server = Server.getInstance();

                if(StaticValue.printerOn){
                    findViewById(R.id.check_printer).setActivated(true);
                }else{
                    findViewById(R.id.check_printer).setActivated(false);
                }
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected()) {
                    // Do whatever
                    findViewById(R.id.check_wi_fi).setActivated(true);

                }else{
                    findViewById(R.id.check_wi_fi).setActivated(false);
                }
                ha.postDelayed(this, 100);
            }
        }, 100);

        resetPinpadTimer(1);


    }


    public void setTypeOfFavourites(){
        operativeFragment.setTypeOfFavourites();
    }

    public void exitModify(){

        cashFragment.keypad = false;
        cashFragment.calculate = false;
        findViewById(R.id.layout_5).setActivated(false);
        findViewById(R.id.layout_6).setActivated(false);
        findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
        findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
        findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
        cashFragment.resetCustomerToDelete();
        if (cashFragment.getModifyProduct() || ModifierFragment.getModify() || cashFragment.deleteProduct) {
            endModifyModifier(-1);
            ModifierFragment.setModify(false);
            endModifyModifier(-1);
            cashFragment.setModifyProduct(false);
            endModifyProduct();
            cashFragment.resetCashNotChanghed();
            cashFragment.deleteProduct = false;
        }
    }

    public void openSaveBillPopup(int code){
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.save_bill_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        popupView.post(new Runnable() {
            @Override
            public void run() {
                    setUpSaveBillPopup(popupView, popupWindow, code);
                 }
        });

        setupDismissKeyboard(popupView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }

    public void setUpSaveBillPopup(final View popupView, final PopupWindow popupWindow, int code){
        //create a new existing client, same as existing client
        popupView.findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "[setUpSaveBillPopup]::[no.onClick]" );
                if (StaticValue.blackbox) {}

                popupWindow.dismiss();

                switch (code) {
                    case 1 :
                        fireUserWindow();
                        break;
                    case 2 :
                        Intent intent2 = new Intent(Operative.this, ClientsActivity.class);
                        intent2.putExtra("username", username);
                        intent2.putExtra("isAdmin", isAdmin);
                        intent2.putExtra("billId", billId);
                        intent2.putExtra("userId", userId);
                        intent2.putExtra("userType", userType);
                        intent2.putExtra("tableNumber", tableNumber);
                        intent2.putExtra("orderNumber", orderNumber);
                        intent2.setAction("clientsFromOperative");
                        startActivity(intent2);
                        finish();
                        break;
                    case 3 :
                        Intent intent = new Intent(Operative.this, OrderActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("isAdmin", isAdmin);
                        intent.putExtra("userId", userId);
                        intent.putExtra("userType", userType);
                        intent.putExtra("tableNumber", tableNumber);
                        intent.putExtra("orderNumber", orderNumber);
                        intent.setAction("ordersFromOperative");
                        startActivity(intent);
                        finish();
                        break;
                    case 4:
                        Intent intent3 = new Intent(Operative.this, OverviewActivity.class);
                        intent3.putExtra("username", username);
                        intent3.putExtra("isAdmin", isAdmin);
                        intent3.putExtra("userId", userId);
                        intent3.putExtra("userType", userType);
                        intent3.putExtra("tableNumber", tableNumber);
                        intent3.putExtra("orderNumber", orderNumber);
                        intent3.setAction("overviewFromOperative");
                        startActivity(intent3);
                        finish();
                        break;
                    default : break;
                }

            }
        });

        //set existing client, same as new client
        popupView.findViewById(R.id.yes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "[setUpSaveBillPopup]::[yes.onClick]");
                if (StaticValue.blackbox) {
                  myPopupWindow = popupWindow;
                  cashFragment.saveBillFromPopupOnServer(code);
                } else {
                    popupWindow.dismiss();
                    int bid = billId;
                    if (bid == -1) {

                        cashFragment.saveBillAndPrint(0);
                        switch (code) {
                            case 1:
                                fireUserWindow();
                                break;
                            case 2:
                                Intent intent2 = new Intent(Operative.this, ClientsActivity.class);
                                intent2.putExtra("username", username);
                                intent2.putExtra("isAdmin", isAdmin);
                                intent2.putExtra("billId", billId);
                                intent2.putExtra("userId", userId);
                                intent2.putExtra("userType", userType);
                                intent2.putExtra("tableNumber", tableNumber);
                                intent2.putExtra("orderNumber", orderNumber);
                                intent2.setAction("clientsFromOperative");
                                startActivity(intent2);
                                finish();
                                break;
                            case 3:
                                Intent intent = new Intent(Operative.this, OrderActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("isAdmin", isAdmin);
                                intent.putExtra("userId", userId);
                                intent.putExtra("userType", userType);
                                intent.putExtra("tableNumber", tableNumber);
                                intent.putExtra("orderNumber", orderNumber);
                                intent.setAction("ordersFromOperative");
                                startActivity(intent);
                                finish();
                                break;
                            case 4:
                                Intent intent3 = new Intent(Operative.this, OverviewActivity.class);
                                intent3.putExtra("username", username);
                                intent3.putExtra("isAdmin", isAdmin);
                                intent3.putExtra("userId", userId);
                                intent3.putExtra("userType", userType);
                                intent3.putExtra("tableNumber", tableNumber);
                                intent3.putExtra("orderNumber", orderNumber);
                                intent3.setAction("overviewFromOperative");
                                startActivity(intent3);
                                finish();
                                break;
                            default:
                                break;
                        }
                    } else {
                        cashFragment.updateBill(billId);
                        cashFragment.printOrderBill(billId);
                        switch (code) {
                            case 1:
                                fireUserWindow();
                                break;
                            case 2:
                                Intent intent2 = new Intent(Operative.this, ClientsActivity.class);
                                intent2.putExtra("username", username);
                                intent2.putExtra("isAdmin", isAdmin);
                                intent2.putExtra("billId", billId);
                                intent2.putExtra("userId", userId);
                                intent2.putExtra("userType", userType);
                                intent2.putExtra("tableNumber", tableNumber);
                                intent2.putExtra("orderNumber", orderNumber);
                                intent2.setAction("clientsFromOperative");
                                startActivity(intent2);
                                finish();
                                break;
                            case 3:
                                Intent intent = new Intent(Operative.this, OrderActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("isAdmin", isAdmin);
                                intent.putExtra("userId", userId);
                                intent.putExtra("userType", userType);
                                intent.putExtra("tableNumber", tableNumber);
                                intent.putExtra("orderNumber", orderNumber);
                                intent.setAction("ordersFromOperative");
                                startActivity(intent);
                                finish();
                                break;
                            case 4:
                                Intent intent3 = new Intent(Operative.this, OverviewActivity.class);
                                intent3.putExtra("username", username);
                                intent3.putExtra("isAdmin", isAdmin);
                                intent3.putExtra("userId", userId);
                                intent3.putExtra("userType", userType);
                                intent3.putExtra("tableNumber", tableNumber);
                                intent3.putExtra("orderNumber", orderNumber);
                                intent3.setAction("overviewFromOperative");
                                startActivity(intent3);
                                finish();
                                break;
                            default:
                                break;
                        }

                    }
                }
            }
        });


    }

    public void throwTimerPopup(ArrayList<Integer> array){
        LayoutInflater layoutInflater = (LayoutInflater)me
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.reservation_automatic_popup, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        //IT FUCKING WORKS!
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ReservationsAdapter popup_reservations_list = new ReservationsAdapter(dbA, me, array);
        RecyclerView popup_rv = popupView.findViewById(R.id.popup_reservations_rv);
        popup_rv.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        popup_rv.setAdapter(popup_reservations_list);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)popupView.findViewById(R.id.automatic_popup_window)
                .getLayoutParams();
        int t = (int) ((int)(dpHeight - 52)/2 -175*density);
        rlp.topMargin = t;
        rlp.setMargins((int)(320*density),t,0,0);
        popupView.findViewById(R.id.automatic_popup_window).setLayoutParams(rlp);


        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)me).findViewById(R.id.operative),0,0,0);
    }

    public void startTableIntentFromResPopup(Reservation res){

        Intent tableIntent = new Intent(Operative.this, TableActivity.class);
        tableIntent.putExtra("tableNumber", -1);
        tableIntent.putExtra("billId", billId);
        tableIntent.putExtra("roomId", -1);
        tableIntent.putExtra("reservation", res.getReservation_id());
        tableIntent.putExtra("isAdmin", isAdmin);
        tableIntent.putExtra("userType", userType);
        tableIntent.putExtra("username", username);
        tableIntent.setAction("setTableForReservation");

        startActivity(tableIntent);
        finish();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(customer!=-1){
            //if customer id != -1 i am coming back from clients activity so I open customer popup
            Intent intent = getIntent();
            boolean customerModify = intent.getBooleanExtra("customerModify", false);
            int modifyPosition = intent.getIntExtra("modifyPosition", -1);
            ClientInfo clientInfo = dbA.fetchSingleClient(customer);
            openCustomerPopup(clientInfo.getName()+" " + clientInfo.getSurname(), clientInfo.getClient_id(),
                    orderNumber, customerModify, modifyPosition, tableNumber);
            cashFragment.setCashListIndex(intent.getIntExtra("cashListIndex", -1));
        }
    }

    //sets CashFragment's values returning from ClientsActivity
    public void setCashFragmentValues(int id, int number){
        this.billId = id;
        this.orderNumber = number;
    }


    /**
     * open customer set up popup
     * @param customerName
     * @param customer_id
     * @param orderNumber
     * @param modify
     * @param modifyPosition
     * @param tNumber
     */
    public void openCustomerPopup(String customerName, int customer_id,
                                  int orderNumber, boolean modify, int modifyPosition, int tNumber){
        tableNumber = tNumber;
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.new_customer_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                setupCustomerWindow(popupView, popupWindow, customer_id, orderNumber, modify, modifyPosition);
                CustomEditText inputText = popupView.findViewById(R.id.customer_text_input);
                inputText.setText(customerName);
            }
        });
        setupDismissKeyboard(popupView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }


    /**
     * set up customer popup
     * @param popupView
     * @param popupWindow
     * @param orderNumber
     * @param modify
     * @param modifyPosition
     */
    public void setupCustomerWindow(final View popupView, final PopupWindow popupWindow, int customer_id,
                                    int orderNumber, boolean modify, int modifyPosition){
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "[setupCustomerWindow]::[kill.onClick]");
                findViewById(R.id.cash_client_container).setEnabled(true);
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "[setupCustomerWindow]::[ok.onClick]");
                CustomEditText inputText = popupView.findViewById(R.id.customer_text_input);
                if(modify){
                    //modify already present customer
                    if (customer != -1) {
                        //if customer is in db
                        ClientInfo clientInfo = dbA.fetchSingleClient(customer);
                        customer = -1;
                        cashFragment.modifyCustomerFromPopup(clientInfo.getClient_id(), clientInfo.getName() + " " + clientInfo.getSurname(), modifyPosition, popupView.findViewById(R.id.customer_notesInputCheckBox).isActivated());

                    } else if (!inputText.getText().toString().equals("")) {
                        //customer isn't in db
                        cashFragment.modifyCustomerFromPopup(-1, String.valueOf(inputText.getText()), modifyPosition, popupView.findViewById(R.id.customer_notesInputCheckBox).isActivated());
                    }
                }
                else {
                    //add a new customer
                    if (customer != -1) {
                        //in db
                        ClientInfo clientInfo = dbA.fetchSingleClient(customer);
                        customer = -1;
                        cashFragment.setNewCustomerFromPopup(clientInfo.getClient_id(), clientInfo.getName() + " " + clientInfo.getSurname(), popupView.findViewById(R.id.customer_notesInputCheckBox).isActivated());
                    } else/* if (!inputText.getText().toString().equals(""))*/ {
                        //not in db
                        dbA.showData("client");
                        String testo = inputText.getText().toString();
                        ClientInfo clientInfo = dbA.fetchSingleClientByCode(inputText.getText().toString());
                        if(clientInfo.getClient_id()!=-1){
                            cashFragment.setNewCustomerFromPopup(clientInfo.getClient_id(), clientInfo.getName() + " " + clientInfo.getSurname(), popupView.findViewById(R.id.customer_notesInputCheckBox).isActivated());
                        }else
                            cashFragment.setNewCustomerFromPopup(-1, String.valueOf(inputText.getText()), popupView.findViewById(R.id.customer_notesInputCheckBox).isActivated());
                    }
                }
                findViewById(R.id.cash_client_container).setEnabled(true);
                popupWindow.dismiss();
            }
        });

        //if costumr is group....
        popupView.findViewById(R.id.customer_check_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.customer_notesInputCheckBox).setActivated(!popupView.findViewById(R.id.customer_notesInputCheckBox).isActivated());
            }
        });

        popupView.findViewById(R.id.customer_notesInputCheckBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.customer_notesInputCheckBox).setActivated(!popupView.findViewById(R.id.customer_notesInputCheckBox).isActivated());
            }
        });
        //create a new existing client, same as existing client
        popupView.findViewById(R.id.customer_firstButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!StaticValue.blackbox) {

                    cashFragment.saveBillForCustomer();
                    int miId = cashFragment.getBillId();
                    Intent intent = new Intent(Operative.this, ClientsActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.putExtra("billId", cashFragment.getBillId());
                    intent.putExtra("orderNumber", orderNumber);
                    intent.putExtra("customerModify", modify);
                    intent.putExtra("modifyPosition", modifyPosition);
                    intent.putExtra("cashListIndex", cashFragment.getCashListIndex());
                    intent.putExtra("userId", userId);
                    intent.putExtra("userType", userType);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.setAction("selectCustomer");
                    startActivity(intent);
                    finish();
                }else{
                    cashFragment.saveBillOnServer("customerPopup", 0);
                }
            }
        });

        //set existing client, same as new client
        popupView.findViewById(R.id.customer_secondButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(!StaticValue.blackbox) {
                        cashFragment.saveBillForCustomer();
                        Intent intent = new Intent(Operative.this, ClientsActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("isAdmin", isAdmin);
                        intent.putExtra("billId", cashFragment.getBillId());
                        intent.putExtra("orderNumber", orderNumber);
                        intent.putExtra("clientLongClick", clientLongClick);
                        intent.setAction("selectCustomer");
                        intent.putExtra("currentCustomer", customer_id);
                        intent.putExtra("customerModify", modify);
                        intent.putExtra("modifyPosition", modifyPosition);
                        intent.putExtra("cashListIndex", cashFragment.getCashListIndex());
                        intent.putExtra("userId", userId);
                        intent.putExtra("userType", userType);
                        intent.putExtra("tableNumber", tableNumber);
                        startActivity(intent);
                        finish();
                    }else{
                        cashFragment.saveBillOnServer("customerPopup", 0);
                    }


            }
        });


    }


    /**
     * open popup to insert unspecificed items to cash adapter
     */
    public void openUnspecProductPopup(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.single_input_dialog, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                setupUnspecItemWindow(popupView, popupWindow);

            }
        });
        setupDismissKeyboard(popupView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }

    /**
     * set up click for unspecificed item popup
     * @param popupView
     * @param popupWindow
     */
    public void setupUnspecItemWindow(final View popupView, final PopupWindow popupWindow){
        final EditText title = popupView.findViewById(R.id.single_input);
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = title.getText().toString();
                cashFragment.unspecItemName = t;
                popupWindow.dismiss();
            }
        });
    }

    public void setupDismissKeyboard(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if((view instanceof EditText)) {
            ((EditText)view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_NEXT) keyboard_next_flag = true;
                    return false;
                }
            });
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if(!(getCurrentFocus() instanceof EditText) && !keyboard_next_flag){
                            Log.d(TAG, "[setupDismissKeyboard]::[OnFocusChange] " + "You clicked out of an Edit Text!");
                            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        keyboard_next_flag = false;
                    }
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupDismissKeyboard(innerView);
            }
        }
    }

    /**
     * fire user popup qwindow
     */
    private void fireUserWindow(){
        if(isAdmin==2){
            logoutFromSimpleUser();
        }else {
            LayoutInflater layoutInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.popup_user_interface, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            setupAdminWindow(popupView, popupWindow);
            popupWindow.setFocusable(true);
            popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
        }
    }

    public void logoutFromSimpleUser(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_simple_user_interface, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                setupSimpleUserWindow(popupView, popupWindow);

            }
        });
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }

    /**
     * set up layout for admin user popup in user popup window
     * @param popupView
     * @param popupWindow
     */
    public void setupSimpleUserWindow(final View popupView, final PopupWindow popupWindow){

        popupView.findViewById(R.id.u_printer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopupForPrinterMethod();
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.u_switchUser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(getApplicationContext(), PinpadActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    /**
     * set up layout for admin user popup in user popup window
     * @param popupView
     * @param popupWindow
     */
    public void setupAdminWindow(final View popupView, final PopupWindow popupWindow){
        popupView.findViewById(R.id.operation_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.addUser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.AdminWindow).setVisibility(View.GONE);
                popupView.findViewById(R.id.AddUserWindow).setVisibility(View.VISIBLE);
                setupNewUserWindow(popupView, popupWindow);
            }
        });

        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.configure_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAdmin!=0){
                    Toast.makeText(Operative.this, R.string.no_you_cant, Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(Operative.this, MainActivity.class);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.putExtra("username", username);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userType", userType);
                    startActivity(intent);
                    finish();
                }
            }
        });
        popupView.findViewById(R.id.switchUser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(getApplicationContext(), PinpadActivity.class);
                startActivity(intent);
                finish();
            }
        });

        popupView.findViewById(R.id.switchUser_button).setOnLongClickListener(new View.OnLongClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onLongClick(View view) {
                /*openPopupForPrinterMethod();
                popupWindow.dismiss();*/
                return true;
            }
        });

        popupView.findViewById(R.id.printerOption_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopupForPrinterMethod();
                popupWindow.dismiss();

            }
        });

        popupView.findViewById(R.id.cashstatus_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dbA.checkIfCashManagementIsSet() == 1)
                    openGeneralCashStatusPopup();
                else
                    Toast.makeText(me, R.string.please_fill_cash_management_first, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void openGeneralCashStatusPopup(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_cashstatus, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        dbA.showData("cash_management_set");
        dbA.showData("cash_management_real");

        setupDismissKeyboard(popupView);

        setupCashStatus(popupView, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }

    public void setupCashStatus(final View popupView, final PopupWindow popupWindow){
        CustomEditText total_deposit = (CustomEditText)popupView.findViewById(R.id.total_deposit);
        CustomEditText five_cents = (CustomEditText)popupView.findViewById(R.id.amount_005);
        CustomEditText ten_cents = (CustomEditText)popupView.findViewById(R.id.amount_010);
        CustomEditText twenty_cents = (CustomEditText)popupView.findViewById(R.id.amount_020);
        CustomEditText fifty_cents = (CustomEditText)popupView.findViewById(R.id.amount_050);
        CustomEditText one_euro = (CustomEditText)popupView.findViewById(R.id.amount_100);
        CustomEditText two_euros = (CustomEditText)popupView.findViewById(R.id.amount_200);
        CustomEditText five_euros = (CustomEditText)popupView.findViewById(R.id.amount_500);
        CustomEditText ten_euros = (CustomEditText)popupView.findViewById(R.id.amount_1000);
        CustomEditText twenty_euros = (CustomEditText)popupView.findViewById(R.id.amount_2000);
        CustomEditText fifty_euros = (CustomEditText)popupView.findViewById(R.id.amount_5000);
        CustomEditText hundred_euros = (CustomEditText)popupView.findViewById(R.id.amount_10000);
        CustomEditText twohundred_euros = (CustomEditText)popupView.findViewById(R.id.amount_20000);

        RelativeLayout deposit_window = (RelativeLayout)popupView.findViewById(R.id.deposit_window);
        RelativeLayout withdraw_amount_window = (RelativeLayout)popupView.findViewById(R.id.withdrawamount_window);

        withdraw_amount_window.setVisibility(View.GONE);
        deposit_window.setVisibility(View.VISIBLE);

        DecimalFormat twoD = new DecimalFormat("#.00");

        ImageButton okButton = (ImageButton)popupView.findViewById(R.id.ok);
        ImageButton cancel = (ImageButton)popupView.findViewById(R.id.kill);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(dbA.checkIfCashTotalIsDifferent() == 0.0f){
                if(deposit_window.getVisibility() == View.VISIBLE){
                    if(!total_deposit.getText().toString().equals("")){

                        String total_deposit_string = total_deposit.getText().toString();
                        CashManagement cash = dbA.getCashManagement();

                        //all other fields not filled
                        if(five_cents.getText().toString().equals("") && ten_cents.getText().toString().equals("") && twenty_cents.getText().toString().equals("")
                                && fifty_cents.getText().toString().equals("") && one_euro.getText().toString().equals("") && two_euros.getText().toString().equals("")
                                && five_euros.getText().toString().equals("") && ten_euros.getText().toString().equals("") && twenty_euros.getText().toString().equals("")
                                && fifty_euros.getText().toString().equals("") && hundred_euros.getText().toString().equals("")
                                && twohundred_euros.getText().toString().equals("")){

                            dbA.insertTotalDeposit(Float.parseFloat(total_deposit_string));

                            total_deposit.setText("");
                        }
                        else{
                            String fiveCents = five_cents.getText().toString();
                            String tenCents = ten_cents.getText().toString();
                            String twentyCents = twenty_cents.getText().toString();
                            String fiftyCents = fifty_cents.getText().toString();
                            String oneE = one_euro.getText().toString();
                            String twoE = two_euros.getText().toString();
                            String fiveE = five_euros.getText().toString();
                            String tenE = ten_euros.getText().toString();
                            String twentyE = twenty_euros.getText().toString();
                            String fiftyE = fifty_euros.getText().toString();
                            String hundred = hundred_euros.getText().toString();
                            String twoHundred = twohundred_euros.getText().toString();

                            dbA.insertCashStatus(Float.parseFloat(total_deposit_string.replace(",", ".")),
                                    fiveCents.equals("")?0:Integer.parseInt(fiveCents), tenCents.equals("")?0:Integer.parseInt(tenCents),
                                    twentyCents.equals("")?0:Integer.parseInt(twentyCents), fiftyCents.equals("")?0:Integer.parseInt(fiftyCents),
                                    oneE.equals("")?0:Integer.parseInt(oneE), twoE.equals("")?0:Integer.parseInt(twoE),
                                    fiveE.equals("")?0:Integer.parseInt(fiveE), tenE.equals("")?0:Integer.parseInt(tenE),
                                    twentyE.equals("")?0:Integer.parseInt(twentyE), fiftyE.equals("")?0:Integer.parseInt(fiftyE),
                                    hundred.equals("")?0:Integer.parseInt(hundred), twoHundred.equals("")?0:Integer.parseInt(twoHundred));

                            total_deposit.setText("");
                            five_cents.setText("");
                            ten_cents.setText("");
                            twenty_cents.setText("");
                            fifty_cents.setText("");
                            one_euro.setText("");
                            two_euros.setText("");
                            five_euros.setText("");
                            ten_euros.setText("");
                            twenty_euros.setText("");
                            fifty_euros.setText("");
                            hundred_euros.setText("");
                            twohundred_euros.setText("");
                        }

                        CashManagement cash_static = dbA.getCashManagementStatic();

                        if(dbA.checkIfCashTotalIsDifferent() >= cash_static.getMinWithdraw()){
                            CustomEditText five_cents_a = (CustomEditText)popupView.findViewById(R.id.amount_005a);
                            CustomEditText ten_cents_a = (CustomEditText)popupView.findViewById(R.id.amount_010a);
                            CustomEditText twenty_cents_a = (CustomEditText)popupView.findViewById(R.id.amount_020a);
                            CustomEditText fifty_cents_a = (CustomEditText)popupView.findViewById(R.id.amount_050a);
                            CustomEditText one_euro_a = (CustomEditText)popupView.findViewById(R.id.amount_100a);
                            CustomEditText two_euros_a = (CustomEditText)popupView.findViewById(R.id.amount_200a);
                            CustomEditText five_euros_a = (CustomEditText)popupView.findViewById(R.id.amount_500a);
                            CustomEditText ten_euros_a = (CustomEditText)popupView.findViewById(R.id.amount_1000a);
                            CustomEditText twenty_euros_a = (CustomEditText)popupView.findViewById(R.id.amount_2000a);
                            CustomEditText fifty_euros_a = (CustomEditText)popupView.findViewById(R.id.amount_5000a);
                            CustomEditText hundred_euros_a = (CustomEditText)popupView.findViewById(R.id.amount_10000a);
                            CustomEditText twohundred_euros_a = (CustomEditText)popupView.findViewById(R.id.amount_20000a);

                            double amount = dbA.checkIfCashTotalIsDifferent();

                            int[] counter = {cash.getTwoHundredEuros()-cash_static.getTwoHundredEuros(),cash.getHundredEuros()-cash_static.getHundredEuros(),
                                    cash.getFiftyEuros()-cash_static.getFiftyEuros(),cash.getTwentyEuros()-cash_static.getTwentyEuros(),
                                    cash.getTenEuros()-cash_static.getTenEuros(),cash.getFiveEuros()-cash_static.getFiveEuros(),
                                    cash.getTwoEuros()-cash_static.getTwoEuros(),cash.getOneEuros()-cash_static.getOneEuros(),cash.getFiftyCents()-cash_static.getFiftyCents(),
                                    cash.getTwentyCents()-cash_static.getTwentyCents(),cash.getTenCents()-cash_static.getTenCents(),
                                    cash.getFiveCents()-cash_static.getFiveCents()};

                            withdraw_amount_window.setVisibility(View.VISIBLE);
                            deposit_window.setVisibility(View.GONE);

                            DecimalFormat twoD = new DecimalFormat("#.00");
                            CustomEditText withdraw_amount = (CustomEditText)popupView.findViewById(R.id.withdraw_amount);
                            withdraw_amount.setText(twoD.format(amount).replace(".", ","));

                            if(counter[11] <= 0)
                                five_cents_a.setText("");
                            else
                                five_cents_a.setText(counter[11] + "");
                            if(counter[10] <= 0)
                                ten_cents_a.setText("");
                            else
                                ten_cents_a.setText(counter[10] + "");
                            if(counter[9] <= 0)
                                twenty_cents_a.setText("");
                            else
                                twenty_cents_a.setText(counter[9] + "");
                            if(counter[8] <= 0)
                                fifty_cents_a.setText("");
                            else
                                fifty_cents_a.setText(counter[8] + "");
                            if(counter[7] <= 0)
                                one_euro_a.setText("");
                            else
                                one_euro_a.setText(counter[7] + "");
                            if(counter[6] <= 0)
                                two_euros_a.setText("");
                            else
                                two_euros_a.setText(counter[6] + "");
                            if(counter[5] <= 0)
                                five_euros_a.setText("");
                            else
                                five_euros_a.setText(counter[5] + "");
                            if(counter[4] <= 0)
                                ten_euros_a.setText("");
                            else
                                ten_euros_a.setText(counter[4] + "");
                            if(counter[3] <= 0)
                                twenty_euros_a.setText("");
                            else
                                twenty_euros_a.setText(counter[3] + "");
                            if(counter[2] <= 0)
                                fifty_euros_a.setText("");
                            else
                                fifty_euros_a.setText(counter[2] + "");
                            if(counter[1] <= 0)
                                hundred_euros_a.setText("");
                            else
                                hundred_euros_a.setText(counter[1] + "");
                            if(counter[0] <= 0)
                                twohundred_euros_a.setText("");
                            else
                                twohundred_euros_a.setText(counter[0] + "");
                        }
                        else
                            popupWindow.dismiss();
                    }
                    else
                        Toast.makeText(me, R.string.please_fill_withdraw_value, Toast.LENGTH_SHORT).show();
                }
                else
                    popupWindow.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                total_deposit.setText("");
                five_cents.setText("");
                ten_cents.setText("");
                twenty_cents.setText("");
                fifty_cents.setText("");
                one_euro.setText("");
                two_euros.setText("");
                five_euros.setText("");
                ten_euros.setText("");
                twenty_euros.setText("");
                fifty_euros.setText("");
                hundred_euros.setText("");
                twohundred_euros.setText("");

                popupWindow.dismiss();
            }
        });
    }

    public void openPinpad(){
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
                setUpPinpadPopup(popupView, popupWindow);
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }

    private void setUpPinpadPopup(View popupView, PopupWindow popupWindow) {
        setupPinpadDigits(popupView, popupWindow);
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void setupPinpadDigits(View popupView, PopupWindow popupWindow){
        RelativeLayout digitContainer = (RelativeLayout) popupView.findViewById(R.id.digits_container);
        View v;
        for(int i = 0; i < digitContainer.getChildCount(); i++){
            v = digitContainer.getChildAt(i);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    char digit = (((CustomButton)v).getText().charAt(0));
                    setDigitsPinpad(digit, popupView, popupWindow);
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

    private void setDigitsPinpad(char digit, View popupView, PopupWindow popupWindow){

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
                        popupWindow.dismiss();

                        openCashStatusPopup();
                    }
                    else {
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


    public void openCashStatusPopup(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_cashdrawer, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        setupDismissKeyboard(popupView);

        setupCashDrawer(popupView, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }

    public void setupCashDrawer(final View popupView, final PopupWindow popupWindow){
        RelativeLayout cashDrawerWindow = (RelativeLayout)popupView.findViewById(R.id.cashdrawer_window);
        RelativeLayout depositWindow = (RelativeLayout)popupView.findViewById(R.id.deposit_window);
        RelativeLayout withdrawWindow = (RelativeLayout)popupView.findViewById(R.id.withdraw_window);

        cashDrawerWindow.setVisibility(View.VISIBLE);
        depositWindow.setVisibility(View.GONE);
        withdrawWindow.setVisibility(View.GONE);

        CustomButton deposit = (CustomButton)popupView.findViewById(R.id.cashdrawer_deposit);
        CustomButton withdraw = (CustomButton)popupView.findViewById(R.id.cashdrawer_withdraw);
        CustomButton exchange = (CustomButton)popupView.findViewById(R.id.cashdrawer_exchange);

        CashManagement cash = dbA.getCashManagement();

        deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                depositWindow.setVisibility(View.VISIBLE);
                cashDrawerWindow.setVisibility(View.GONE);
                withdrawWindow.setVisibility(View.GONE);

                //open cash drawer
                if(StaticValue.printerName.equals("ditron")){
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.closeAll();
                    ditron.startSocket();
                }

                ClientThread myThread = ClientThread.getInstance();
                myThread.delegate = forClient;
                myThread.setPrintType(14);
                myThread.setIP(IP);

                myThread.setClientThread();
                myThread.setRunBaby(true);
            }
        });

        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withdrawWindow.setVisibility(View.VISIBLE);
                depositWindow.setVisibility(View.GONE);
                cashDrawerWindow.setVisibility(View.GONE);

                //open cash drawer
                if(StaticValue.printerName.equals("ditron")){
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.closeAll();
                    ditron.startSocket();
                }

                ClientThread myThread = ClientThread.getInstance();
                myThread.delegate = forClient;
                myThread.setPrintType(14);
                myThread.setIP(IP);

                myThread.setClientThread();
                myThread.setRunBaby(true);
            }
        });

        exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open cash drawer
                if(StaticValue.printerName.equals("ditron")){
                    PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                    ditron.closeAll();
                    ditron.startSocket();
                }

                ClientThread myThread = ClientThread.getInstance();
                myThread.delegate = forClient;
                myThread.setPrintType(14);
                myThread.setIP(IP);

                myThread.setClientThread();
                myThread.setRunBaby(true);

                popupWindow.dismiss();
            }
        });

        CustomEditText withdraw_et = (CustomEditText)popupView.findViewById(R.id.total_withdraw);
        CustomEditText deposit_et = (CustomEditText)popupView.findViewById(R.id.total_deposit);
        CustomEditText five_cents = (CustomEditText)popupView.findViewById(R.id.amount_005);
        CustomEditText ten_cents = (CustomEditText)popupView.findViewById(R.id.amount_010);
        CustomEditText twenty_cents = (CustomEditText)popupView.findViewById(R.id.amount_020);
        CustomEditText fifty_cents = (CustomEditText)popupView.findViewById(R.id.amount_050);
        CustomEditText one_euro = (CustomEditText)popupView.findViewById(R.id.amount_100);
        CustomEditText two_euros = (CustomEditText)popupView.findViewById(R.id.amount_200);
        CustomEditText five_euros = (CustomEditText)popupView.findViewById(R.id.amount_500);
        CustomEditText ten_euros = (CustomEditText)popupView.findViewById(R.id.amount_1000);
        CustomEditText twenty_euros = (CustomEditText)popupView.findViewById(R.id.amount_2000);
        CustomEditText fifty_euros = (CustomEditText)popupView.findViewById(R.id.amount_5000);
        CustomEditText hundred_euros = (CustomEditText)popupView.findViewById(R.id.amount_10000);
        CustomEditText twohundred_euros = (CustomEditText)popupView.findViewById(R.id.amount_20000);

        DecimalFormat twoD = new DecimalFormat("#.00");

        ImageButton okButton = (ImageButton)popupView.findViewById(R.id.ok);
        ImageButton cancel = (ImageButton)popupView.findViewById(R.id.kill);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(depositWindow.getVisibility() == View.VISIBLE){
                    if(!deposit_et.getText().toString().equals("")){
                        String deposit_string = deposit_et.getText().toString();
                        //all other fields not filled
                        if(five_cents.getText().toString().equals("") && ten_cents.getText().toString().equals("") && twenty_cents.getText().toString().equals("")
                                && fifty_cents.getText().toString().equals("") && one_euro.getText().toString().equals("") && two_euros.getText().toString().equals("")
                                && five_euros.getText().toString().equals("") && ten_euros.getText().toString().equals("") && twenty_euros.getText().toString().equals("")
                                && fifty_euros.getText().toString().equals("") && hundred_euros.getText().toString().equals("")
                                && twohundred_euros.getText().toString().equals("")){

                            dbA.modifySimpleTotalCashManagement(cash.getCurrentTotal() + Float.parseFloat(deposit_string.replace(",", ".")));

                            deposit_et.setText("");

                            popupWindow.dismiss();
                        }
                        else{
                            //if at least one is filled, every field should be filled
                            if(!five_cents.getText().toString().equals("") && !ten_cents.getText().toString().equals("") && !twenty_cents.getText().toString().equals("")
                                    && !fifty_cents.getText().toString().equals("") && !one_euro.getText().toString().equals("") && !two_euros.getText().toString().equals("")
                                    && !five_euros.getText().toString().equals("") && !ten_euros.getText().toString().equals("") && !twenty_euros.getText().toString().equals("")
                                    && !fifty_euros.getText().toString().equals("") && !hundred_euros.getText().toString().equals("")
                                    && !twohundred_euros.getText().toString().equals("")){

                                String fiveCents = five_cents.getText().toString();
                                String tenCents = ten_cents.getText().toString();
                                String twentyCents = twenty_cents.getText().toString();
                                String fiftyCents = fifty_cents.getText().toString();
                                String oneE = one_euro.getText().toString();
                                String twoE = two_euros.getText().toString();
                                String fiveE = five_euros.getText().toString();
                                String tenE = ten_euros.getText().toString();
                                String twentyE = twenty_euros.getText().toString();
                                String fiftyE = fifty_euros.getText().toString();
                                String hundred = hundred_euros.getText().toString();
                                String twoHundred = twohundred_euros.getText().toString();

                                dbA.modifyTotalCashManagement(cash.getCurrentTotal() + Float.parseFloat(deposit_string.replace(",", ".")),
                                        cash.getFiveCents() + Integer.parseInt(fiveCents), cash.getTenCents() + Integer.parseInt(tenCents),
                                        cash.getTwentyCents() + Integer.parseInt(twentyCents),
                                        cash.getFiftyCents() + Integer.parseInt(fiftyCents), cash.getOneEuros() + Integer.parseInt(oneE),
                                        cash.getTwoEuros() + Integer.parseInt(twoE), cash.getFiveEuros() + Integer.parseInt(fiveE),
                                        cash.getTenEuros() + Integer.parseInt(tenE), cash.getTwentyEuros() + Integer.parseInt(twentyE),
                                        cash.getFiftyEuros() + Integer.parseInt(fiftyE),cash.getHundredEuros() + Integer.parseInt(hundred),
                                        cash.getTwoHundredEuros() + Integer.parseInt(twoHundred));

                                deposit_et.setText("");
                                five_cents.setText("");
                                ten_cents.setText("");
                                twenty_cents.setText("");
                                fifty_cents.setText("");
                                one_euro.setText("");
                                two_euros.setText("");
                                five_euros.setText("");
                                ten_euros.setText("");
                                twenty_euros.setText("");
                                fifty_euros.setText("");
                                hundred_euros.setText("");
                                twohundred_euros.setText("");

                                popupWindow.dismiss();
                            }
                            else
                                Toast.makeText(me, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else if(withdrawWindow.getVisibility() == View.VISIBLE){
                    if(!withdraw_et.getText().toString().equals("")){
                        String withdraw_string = withdraw_et.getText().toString();
                        float withdrawal = Float.parseFloat(withdraw_string.replace(",", "."));
                        if(withdrawal > 0.0f){
                            if(cash.getCurrentTotal() > withdrawal){
                                if(withdrawal >= cash.getMinWithdraw()) {
                                    dbA.modifySimpleTotalCashManagement(cash.getCurrentTotal() - withdrawal);

                                    withdraw_et.setText("");

                                    popupWindow.dismiss();
                                }
                                else
                                    Toast.makeText(me, getResources().getString(R.string.min_withdraw_is_, cash.getMinWithdraw()), Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(me, R.string.you_cant_withdraw_more_than_current_total, Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(me, R.string.insert_a_valid_value, Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(me, R.string.please_fill_withdraw_value, Toast.LENGTH_SHORT).show();
                }
                else
                    popupWindow.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cashDrawerWindow.getVisibility() == View.VISIBLE)
                    popupWindow.dismiss();
                else if(withdrawWindow.getVisibility() == View.VISIBLE || depositWindow.getVisibility() == View.VISIBLE){
                    cashDrawerWindow.setVisibility(View.VISIBLE);
                    depositWindow.setVisibility(View.GONE);
                    withdrawWindow.setVisibility(View.GONE);
                }
            }
        });
    }

    public void openPopupForPrinterMethod(){
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_printer_method, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.printer_function_container).getLayoutParams();
                int top1 = (int) (dpHeight - 52) / 2 - rlp1.height / 2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.printer_function_container).setLayoutParams(rlp1);*/
                setUpPrinterMethodPopup(popupView, popupWindow);


            }
        });
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.operative), 0, 0, 0);
    }

    public void setUpPrinterMethodPopup(final View popupView, final PopupWindow popupWindow){

       popupView.findViewById(R.id.azzeramento_ordini).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                azzeramentoNumeroScontrini();
            }
        });
        popupView.findViewById(R.id.print_report_x).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printeReport(1);
                popupWindow.dismiss();

            }
        });
        popupView.findViewById(R.id.chiusura_fiscale_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chiusuraCassa();
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });
        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });
    }

    public void printerTest(){
        Server server = Server.getInstance();
        server.killAll();
        server.setServer(getApplicationContext());
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if(!StaticValue.printerOn){
                Toast.makeText(me, R.string.cant_connect_printer, Toast.LENGTH_LONG).show();
            }
        }

    }

    public void azzeramentoNumeroScontrini(){
        if(StaticValue.blackbox){
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            callHttpHandler("/azzeramentoScontrini", params);
        }else {
            dbA.azzeramentoScontrini();
            dbA.updateClosingTime();
            // your code here
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
    }

    public void chiusuraCassa(){
        if(StaticValue.blackbox){
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            callHttpHandler("/chiusuraCassa", params);
        }else {

            long lastSession = dbA.getLastClosing();
            long now = System.currentTimeMillis();
            long last = dbA.returnBillLastDate();
            if (last >= lastSession && last <= now) {
                Toast.makeText(me, R.string.open_bill_please_close, Toast.LENGTH_SHORT).show();
            } else {
                dbA.updateClosingTime();
                dbA.insertIntoStatistic();
                printeReport(0);

                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                // your code here
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        10000
                );

            }
        }
    }

    public void printeReport(int report){
        if(StaticValue.blackbox){
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("report", String.valueOf(report)));
            callHttpHandler("/printReport", params);
        }else {
            if (StaticValue.printerName.equals("ditron")) {
                PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                ditron.closeAll();
                ditron.startSocket();
            }

            ClientThread myThread = ClientThread.getInstance();
            myThread.delegate = forClient;
            myThread.setPrintType(13);
            myThread.setIP(IP);
            myThread.setBillId("1");
            myThread.setDeviceName("a");
            myThread.setOrderNumber("1");
            myThread.setReport(report);

            myThread.setClientThread();
            myThread.setRunBaby(true);
        }

    }

    /**
     * set up new user popup window
     * @param popupView
     * @param popupWindow
     */
    public void setupNewUserWindow(final View popupView, final PopupWindow popupWindow){
        /**
         * part to add other user
         */
        RecyclerView user_recycler = popupView.findViewById(R.id.users_recycler);
        user_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        user_recycler.setHasFixedSize(true);
        userAdapter = new UserAdapter(this, dbA, userType, userId, popupView, popupWindow);
        user_recycler.setAdapter(userAdapter);

        setupDismissKeyboard(popupView);
        final CustomEditText Name = popupView.findViewById(R.id.name_et);
        Name.setText("");
        final CustomEditText Surname = popupView.findViewById(R.id.surname_et);
        Surname.setText("");
        final CustomEditText Passcode = popupView.findViewById(R.id.passcode_et);
        Passcode.setText("");
        final CustomEditText Email = popupView.findViewById(R.id.email_et);
        Email.setText("");
        //set max length to 4 for passcode and 6 for password
        Passcode.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        final ImageButton manager = popupView.findViewById(R.id.manager_checkbox);
        manager.setActivated(false);
        manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setActivated(!manager.isActivated());
            }
        });
        /**
         * OK Button behavior while in New User window
         */
        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = Name.getText().toString();
                String surname = Surname.getText().toString();
                String passcode = Passcode.getText().toString();
                String email = Email.getText().toString();
                if(name.equals("") || surname.equals("")  || email.equals("") || passcode.equals(""))
                    Toast.makeText(getBaseContext(),R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                else if(dbA.checkIfPasscodeExists(passcode)){
                    Toast.makeText(getBaseContext(),R.string.passcode_is_already_used, Toast.LENGTH_SHORT).show();
                }else{
                    if(!Pattern.matches("^[-a-zA-Z0-9_]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email)){
                        Toast.makeText(getBaseContext(),R.string.not_a_valid_email, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String password = passcode;
                        if (StaticValue.blackbox) {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("name", name));
                            params.add(new BasicNameValuePair("surname", surname));
                            params.add(new BasicNameValuePair("email", ""));
                            params.add(new BasicNameValuePair("password", passcode));
                            myPopupView = popupView;
                            myPopupWindow = popupWindow;
                            if (manager.isActivated())
                                params.add(new BasicNameValuePair("userType", String.valueOf(1)));
                            else
                                params.add(new BasicNameValuePair("userType", String.valueOf(2)));
                            callHttpHandler("/insertUser", params);
                        } else {
                            if (manager.isActivated()) {
                                dbA.insertUser(name, surname, "", passcode, 1, passcode);
                            } else {
                                dbA.insertUser(name, surname, "", passcode, 2, passcode);
                            }
                            popupView.findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                            popupView.findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                            setupAdminWindow(popupView, popupWindow);
                        }
                    }

                }
            }
        });




        /**
         *  X button behavior while in New User window
         */
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                popupView.findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                setupAdminWindow(popupView, popupWindow);
            }
        });
    }

    /**
     * show setup new session windows to create sessions, only admin can do this
     */
    public void setupNewSessionWindow(final View popupView, final PopupWindow popupWindow) {


        RecyclerView session_recycler = popupView.findViewById(R.id.session_time_recycler);
        session_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        session_recycler.setHasFixedSize(true);
        sessionAdapter = new SessionAdapter(this, dbA, popupView, popupWindow);
        session_recycler.setAdapter(sessionAdapter);

        DividerItemDecoration divider = new
                DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(),
                R.drawable.divider_line_horizontal1dp));
        session_recycler.addItemDecoration(divider);

        final CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
        newSessionNameContainer .setText("");
        CustomButton startTimeContainer = popupView.findViewById(R.id.start_session_button_et);
        startTimeContainer.setText(R.string.startTime);
        CustomButton endTimeContainer = popupView.findViewById(R.id.end_session_button_et);
        endTimeContainer.setText(R.string.endTime);
        startTimeContainer .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Operative.this,4, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String formattedHours = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes= new DecimalFormat("00").format((selectedMinute));
                        startTimeContainer.setText( formattedHours + ":" + formattedMinutes);
                        String startTime = formattedHours+":"+formattedMinutes+":00";

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        endTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Operative.this,4, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String formattedHours = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes= new DecimalFormat("00").format((selectedMinute));
                        endTimeContainer.setText( formattedHours + ":" + formattedMinutes);
                        String endTime = formattedHours+":"+formattedMinutes+":00";
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
                CustomButton startTimeContainer = popupView.findViewById(R.id.start_session_button_et);
                CustomButton endTimeContainer = popupView.findViewById(R.id.end_session_button_et);
                String sessionName= newSessionNameContainer .getText().toString();
                String startTime = startTimeContainer.getText().toString();
                String endTime = endTimeContainer.getText().toString();
                if(!sessionName.equals("") && !startTime.equals("Start Time") && !endTime.equals("End Time")) {
                    dbA.saveNewSessionTime( startTime+":00",endTime+":00", sessionName );
                    RecyclerView session_recycler = popupView.findViewById(R.id.session_time_recycler);
                    session_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    session_recycler.setHasFixedSize(true);
                    sessionAdapter = new SessionAdapter(me, dbA, popupView, popupWindow);
                    session_recycler.setAdapter(sessionAdapter);

                    DividerItemDecoration divider = new
                            DividerItemDecoration(getApplicationContext(),
                            DividerItemDecoration.VERTICAL);
                    divider.setDrawable(ContextCompat.getDrawable(getBaseContext(),
                            R.drawable.divider_line_horizontal1dp));
                    session_recycler.addItemDecoration(divider);

                    newSessionNameContainer .setText("");
                    startTimeContainer.setText(R.string.startTime);
                    endTimeContainer.setText(R.string.endTime);
                }else{
                    Toast.makeText(Operative.this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();

                }
            }
        });
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                popupView.findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                popupView.findViewById(R.id.SessionWindow_ma).setVisibility(View.GONE);
                setupAdminWindow(popupView, popupWindow);

            }
        });

    }

    /**
     * thi goes away
     * @param sessionTimeId
     * @param sessionName
     * @param start
     * @param end
     */
    @Override
    public void setButtonSet(int sessionTimeId, String sessionName, String start, String end) {

    }
    @Override
    public void deleteSession(int sessionTimeId) {

    }

    /**
     * set button for session popup in usere popup
     * @param sessionTimeId
     * @param sessionName
     * @param start
     * @param end
     * @param popupView
     * @param popupWindow
     */
    public void setButtonSetPopup(int sessionTimeId, String sessionName, String start, String end, View popupView, PopupWindow popupWindow){

        CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
        newSessionNameContainer .setText(sessionName);
        CustomButton startTimeContainer = popupView.findViewById(R.id.start_session_button_et);
        startTimeContainer.setText(start);
        CustomButton endTimeContainer = popupView.findViewById(R.id.end_session_button_et);
        endTimeContainer.setText(end);
        startTimeContainer .setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Operative.this,4, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String formattedHours = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes= new DecimalFormat("00").format((selectedMinute));
                        startTimeContainer.setText( formattedHours + ":" + formattedMinutes);
                        String startTime = formattedHours+":"+formattedMinutes+":00";

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        endTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Operative.this,4, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String formattedHours = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes= new DecimalFormat("00").format((selectedMinute));
                        endTimeContainer.setText( formattedHours + ":" + formattedMinutes);
                        String endTime = formattedHours+":"+formattedMinutes+":00";
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });




        /**
         * OK Button behavior while in New User window
         */
        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
                CustomButton startTimeContainer = popupView.findViewById(R.id.start_session_button_et);
                CustomButton endTimeContainer = popupView.findViewById(R.id.end_session_button_et);
                String sessionName= newSessionNameContainer .getText().toString();
                String startTime = startTimeContainer.getText().toString();
                String endTime = endTimeContainer.getText().toString();
                if(!sessionName.equals("") && !startTime.equals("Start Time") && !endTime.equals("End Time")) {
                    dbA.updateNewSessionTime(sessionTimeId,  startTime+":00",endTime+":00", sessionName );

                    setupNewSessionWindow(popupView, popupWindow);
                }else{
                    Toast.makeText(Operative.this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();

                }
            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setupNewSessionWindow(popupView, popupWindow);

            }
        });
    }

    /**
     * delete session from list
     * @param sessionId
     * @param popupView
     * @param popupWindow
     */
    public void deleteSessionPopup(int sessionId, View popupView, PopupWindow popupWindow) {
        dbA.deleteSessionTime(sessionId);
        setupNewSessionWindow(popupView, popupWindow);
    }

    @Override
    public void passDataToActivity(ButtonLayout button, String someValue, Integer catId, Float price, Integer quantity) {
        modifierFragment.setCatId(catId);
        modifierFragment.setCurrentProduct(someValue);
        if(!cashFragment.getPaid()){
            Boolean isModify = operativeFragment.getIsModify();
            if(cashFragment.listDataCustomer.size()>0){
                if(operativeFragment.checkIfProductHasModifiers(catId) && (cashFragment.currentCustomerArray.size()>0|| isModify)) {
                    FragmentTransaction transaction =
                            getSupportFragmentManager().beginTransaction();
                    transaction.remove(operativeFragment);
                    transaction.add(R.id.operative_fragment, modifierFragment);
                    transaction.commit();
                }
            }else{
                if(operativeFragment.checkIfProductHasModifiers(catId)/* && cashFragment.listDataCustomer.size()>0 && cashFragment.currentCustomerArray.size()>0*/) {
                    FragmentTransaction transaction =
                            getSupportFragmentManager().beginTransaction();
                    transaction.remove(operativeFragment);
                    transaction.add(R.id.operative_fragment, modifierFragment);
                    transaction.commit();

                }
            }



            activityAssignedValue = someValue;
            Integer groupPosition = operativeFragment.getGroupPosition();
            CashFragment cashFragment = (CashFragment) getSupportFragmentManager().findFragmentById(R.id.cash_fragment);

            if(isModify){
                cashFragment.updateSpecificText(activityAssignedValue, price, quantity, groupPosition, catId);
            }
            else{
                //   ArrayList<CashButtonListLayout> newList = new ArrayList<CashButtonListLayout>();
                //   modifierFragment.setCashButtonList(newList);
                operativeFragment.setGroupPosition(-1);
                cashFragment.updateText(button, activityAssignedValue, price, quantity);
            }
        }
        else if(cashFragment.getPaid())
            Toast.makeText(this, R.string.you_cant_add_products, Toast.LENGTH_SHORT).show();


    }



    public void getBackToButtons(String categoryTitle, Integer catId){
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.operative_fragment, operativeFragment);
        transaction.commit();

        operativeFragment.setCatId(catId);
        operativeFragment.goToPreviousCatFromModifiers(catId);

    }



    @Override
    public void passModifierToActivity(OModifierAdapter.OModifier modifier, Integer quantity) {

        Boolean isModify = operativeFragment.getIsModify();

        CashFragment cashFragment = (CashFragment) getSupportFragmentManager().findFragmentById(R.id.cash_fragment);

        if(isModify){
            Integer groupPosition = operativeFragment.getGroupPosition();
            cashFragment.updateSpecificModifierText(modifier, quantity, groupPosition);

        }else {
            if(ModifierFragment.getModify()){
                Integer groupPosition = operativeFragment.getGroupPosition();
                cashFragment.updateSpecificModifierText(modifier, 1, groupPosition);

            }else {
                cashFragment.updateModifierText(modifier, quantity);
            }
        }
    }


    public void setCashButtonList(List<CashButtonListLayout> modifiers){
        modifierFragment.setCashButtonList(modifiers);
    }

    @Override
    public void passModifierToRemoveToActivity(OModifierAdapter.OModifier modifier, Integer quantity, Integer position) {
        Integer groupPosition = operativeFragment.getGroupPosition();
        cashFragment.removeSpecificModifierText(modifier, quantity, groupPosition, position);

    }

    public int setGroupPosition(){
          return  cashFragment.getListDataHeader()-1;
    }

    public void setMyGroupPosition(int groupPosition){
        operativeFragment.setGroupPosition(groupPosition);
    }


    @Override
    public void modifyProduct(Integer groupPosition) {
        if(!operativeFragment.isAdded()) {
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.remove(modifierFragment);
            transaction.add(R.id.operative_fragment, operativeFragment);
            transaction.commit();
        }
        cashFragment.triggerCancelButton();
        operativeFragment.setIsModify(true);
        operativeFragment.setGroupPosition(groupPosition);
        cashFragment.setModifyBar(true);

    }

    @Override
    public void endModifyProduct() {
        operativeFragment.setIsModify(false);
        cashFragment.setModifyBar(false);
        cashFragment.endModifyLayout();
    }



    @Override
    public void deleteProduct(int groupPosition){
        CashFragment cashFragment = (CashFragment) getSupportFragmentManager().findFragmentById(R.id.cash_fragment);
        cashFragment.deleteProduct(groupPosition);

    }

    @Override
    public void deleteAllProducts(int groupPosition) {
        CashFragment cashFragment = (CashFragment) getSupportFragmentManager().findFragmentById(R.id.cash_fragment);
        cashFragment.deleteAllProducts(groupPosition);
    }

    @Override
    public void deleteCustomer(int customerPosition){
        CashFragment cashFragment = (CashFragment) getSupportFragmentManager().findFragmentById(R.id.cash_fragment);
        cashFragment.deleteCustomer(customerPosition);

    }

    @Override
    public void  modifyCustomer(int customerPosition, Customer customer){
        openCustomerPopup(customer.getDescription(), customer.getCustomerId(),
                orderNumber, true, customerPosition, tableNumber);

    }

    public void goToMainPageMethod() {
        if(!operativeFragment.isAdded()) {
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.remove(modifierFragment);
            transaction.add(R.id.operative_fragment, operativeFragment);
            transaction.commit();
        }

    }

    @Override
    public void goToMainPage(){
        goToMainPageMethod();
        operativeFragment.goToPreviousCatFromModifiers(0);
    }

    @Override
    public void deleteCurrentCash(){
        endModifyModifier(-1);
        goToMainPageMethod();
        operativeFragment.goToPreviousCatFromModifiers(0);
        cashFragment.setTotalToZero();
    }

    @Override
    public void addQuantityToCashList(Integer groupPosition, Boolean add){
        cashFragment.addQuantityToCashList(groupPosition, add);
    }

    @Override
    public void showModifierPageToModify(Integer groupPosition, List<CashButtonListLayout> listOfValues, Integer modifierId, String currentProduct, Integer categoryId){
        operativeFragment.setGroupPosition(groupPosition);
        if(!modifierFragment.isAdded()) {
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.remove(operativeFragment);
            transaction.add(R.id.operative_fragment, modifierFragment);
            transaction.commit();

            cashFragment.setOldList(groupPosition);
            cashFragment.setModifyPosition(groupPosition);

            modifierFragment.setAlreadyShowingModifier(false);
            modifierFragment.setCashButtonList(listOfValues);
            ModifierFragment.setModify(true);
            modifierFragment.setCatId(categoryId);
            modifierFragment.setCurrentProduct(currentProduct);

            modifierFragment.showModifierPageToModify(listOfValues, modifierId, currentProduct, categoryId);
        }else{
            cashFragment.setOldList(groupPosition);

            modifierFragment.setAlreadyShowingModifier(true);
            modifierFragment.setCashButtonList(listOfValues);
            ModifierFragment.setModify(true);
            modifierFragment.setCatId(categoryId);
            modifierFragment.setCurrentProduct(currentProduct);
            cashFragment.setModifyPosition(groupPosition);
            modifierFragment.showModifierPageToModify2(listOfValues, modifierId, currentProduct, categoryId);
        }
    }



    @Override
    public ArrayList<CashButtonListLayout> getLastList() {
        return cashFragment.getLastList();
    }

    @Override
    public void passNoteModifierToActivity(CashButtonListLayout modifier, int quantity, boolean modify, List<CashButtonListLayout> cashButtonList) {
        Integer groupPosition = operativeFragment.getGroupPosition();
        cashFragment.updateNoteModifierText(modifier, quantity, modify, groupPosition, cashButtonList);

    }

    @Override
    public void removeModifierFromCashListInModify(Integer position, OModifierAdapter.OModifier modifier, Integer groupPosition){
        modifierFragment.removeModifierFromCashButtonList(position, modifier);
        if(modifierFragment.returnModifyCashList(position, modifier)==0){
            endModifyModifier(groupPosition);
            goToMainPage();
        }
    }

    @Override
    public void endModifyModifier(int groupPosition){
        List<CashButtonListLayout> listOfValues = new ArrayList<CashButtonListLayout>();
        modifierFragment.setCashButtonList(listOfValues);
        modifierFragment.setCatId(-11);
        modifierFragment.setModifierId(-11);
        ModifierFragment.setModify(false);
        cashFragment.endModifyModifier(groupPosition);
    }

    @Override
    public void setItemToDelete(Integer groupPosition, Boolean toLeft){
        endModifyProduct();
        cashFragment.setModifyProduct(false);
        if(!operativeFragment.getIsModify() && !ModifierFragment.getModify()) {
            cashFragment.setItemToDelete(groupPosition, toLeft);
        }
    }

    @Override
    public void setCustomerToDelete(Integer customerPosition, Boolean toLeft){
        if(!operativeFragment.getIsModify() && !ModifierFragment.getModify()) {
            cashFragment.setCustomerToDelete(customerPosition, toLeft);
        }
    }

    @Override
    public void setGroupClick(Integer groupPosition) {
        if(!cashFragment.getDeleteProduct() && !cashFragment.getModifyProduct()) {
            cashFragment.setModifyProduct(true);
            cashFragment.setOnGroupClick(groupPosition);
        }
    }

    @Override
    public void setCustomerClick(Integer customerPosition) {
        if(!cashFragment.getDeleteProduct()) {
            cashFragment.addToArrayCustomer(customerPosition);
            goToMainPage();
            operativeFragment.setTypeOfFavourites();
        }
    }

    public String getUser() {
        return username;
    }

    public int isAdmin() {
        return isAdmin;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            resetPinpadTimer(1);
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

    @Override
    public void deleteNoteFromList(){
        int groupPosition = operativeFragment.getGroupPosition();
        cashFragment.deleteNoteFromList(groupPosition);
    }

    @Override
    public void passClientLongClickToActivity(boolean clientLongClick) {
        this.clientLongClick = clientLongClick;
    }

    @Override
    public void setModifyUser(User user, final View popupview, final PopupWindow popupWindow) {
        final CustomEditText Name = popupview.findViewById(R.id.name_et);
        Name.setText(user.getName());
        final CustomEditText Surname = popupview.findViewById(R.id.surname_et);
        Surname.setText(user.getSurname());
        final CustomEditText Passcode = popupview.findViewById(R.id.passcode_et);
        Passcode.setText(user.getPasscode());
        final CustomEditText Email = popupview.findViewById(R.id.email_et);
        Email.setText(user.getEmail());
        //set max length to 4 for passcode and 6 for password
        //   Password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        Passcode.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        final ImageButton manager = popupview.findViewById(R.id.manager_checkbox);
        //final ImageButton cashier = (ImageButton)findViewById(R.id.cashier_checkbox);
        if(user.getUserRole()==1 || user.getUserRole()==0)
            manager.setActivated(true);
        // cashier.setActivated(false);
        manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.getUserRole()!=0)
                    manager.setActivated(!manager.isActivated());
                else Toast.makeText(Operative.this, R.string.you_cant_change_your_admin_role, Toast.LENGTH_SHORT).show();
            }
        });
        /**
         * OK Button behavior while in New User window
         */
        popupview.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = Name.getText().toString();
                String surname = Surname.getText().toString();
                String passcode = Passcode.getText().toString();
                String email = Email.getText().toString();
                if(name.equals("") || surname.equals("") /*|| email.equals("") */|| passcode.equals(""))
                    Toast.makeText(getBaseContext(),R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                else if(dbA.checkIfPasscodeExistsWithId(passcode, user.getId())){
                    Toast.makeText(getBaseContext(),R.string.passcode_is_already_used, Toast.LENGTH_SHORT).show();
                }else if(!Pattern.matches("^[-a-zA-Z0-9_]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email)){
                    Toast.makeText(getBaseContext(),R.string.not_a_valid_email, Toast.LENGTH_SHORT).show();
                }else{
                    String password = passcode;
                    if(StaticValue.blackbox){
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("password", passcode));
                        params.add(new BasicNameValuePair("oldPassword", user.getPasscode()));
                        params.add(new BasicNameValuePair("name", name));
                        params.add(new BasicNameValuePair("surname", surname));
                        params.add(new BasicNameValuePair("email", email));
                        params.add(new BasicNameValuePair("passcode", passcode));
                        params.add(new BasicNameValuePair("userType", String.valueOf(user.getUserRole())));
                        params.add(new BasicNameValuePair("id", String.valueOf(user.getId())));
                        myPopupView = popupview;
                        myPopupWindow = popupWindow;
                        httpHandler.UpdateInfoAsyncTask("/updateUser", params);
                        httpHandler.execute();
                    }else {
                        if (user.getUserRole() == 0) {
                            dbA.updateUser(name, surname, passcode, email, 0, user.getId());
                        } else {
                            if (manager.isActivated()) {
                                dbA.updateUser(name, surname, passcode, email, 1, user.getId());
                            } else /*if(cashier.isActivated())*/ {
                                dbA.updateUser(name, surname, passcode, email, 2, user.getId());
                            }
                        }
                        setupNewUserWindow(popupview, popupWindow);
                    }

                }
            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupview.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupNewUserWindow(popupview, popupWindow);
            }
        });
    }

    @Override
    public void onTaskEndWithResult(String success) {

    }

    @Override
    public void onTaskFinishGettingData(String result) {

    }

    String barcode="";
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {

        if(e.getAction()==KeyEvent.ACTION_DOWN){
            //Toast.makeText(context,"dispatchKeyEvent: "+e.toString(), Toast.LENGTH_SHORT).show();
            char pressedKey = (char) e.getUnicodeChar();
            barcode += pressedKey;
        }
        if (e.getAction()==KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            Toast.makeText(getApplicationContext(),
                    "barcode--->>>" + barcode, Toast.LENGTH_LONG)
                    .show();
            Log.i(TAG, "barcode: " + barcode.toString());
            ButtonLayout button =dbA.fetchButtonByQuery("SELECT * FROM button WHERE barcode='"+barcode.replaceAll("[^A-Za-z0-9]", "")+"' LIMIT 1");
            if(button!=null) {
                if(button.getID()>0) {
                    passDataToActivity(button, button.getTitle(), button.getCatID(), button.getPrice(), 1);
                }else{
                    Toast.makeText(me, "NO BARCODE ASSOCIATED", Toast.LENGTH_SHORT).show();
                }
            }else
                Toast.makeText(me, "NO BARCODE ASSOCIATED", Toast.LENGTH_SHORT).show();
            goToMainPage();
            barcode="";
        }

        return false;
    }


}

