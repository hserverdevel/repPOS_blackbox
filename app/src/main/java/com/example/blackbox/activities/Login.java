package com.example.blackbox.activities;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.example.blackbox.adapter.SessionAdapter;
import com.example.blackbox.adapter.UserAdapter;
import com.example.blackbox.client.ClientThread;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.model.CashManagement;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.model.User;
import com.example.blackbox.printer.PrinterDitronThread;
import com.example.blackbox.revicer.LogoutBroadcastReciver;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

//import java.net.URL;


public class Login extends AppCompatActivity implements SessionAdapter.AdapterSessionCallback, UserAdapter.AdapterUserCallback, ClientThread.TaskDelegate, HttpHandler.AsyncResponse
{

    public static final String          alphaNumChars      = "h2iMN09jkl3mnWXop4st87uv5wxabJKe1yz65ABCfgD6EY0ZF43GH7ILqrOP8QR21ST9UcdV";
    private static final String TAG = "<Login>";
    // Storage Permissions
    private static final int      REQUEST_EXTERNAL_STORAGE = 1;
    private static final int      PERMISSION_REQUEST_CODE  = 200;
    private static final String[] PERMISSIONS_STORAGE      = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final       String          IP                 = StaticValue.IP;
    private final int red   = Color.parseColor("#cd0046");
    private final int black = Color.parseColor("#DD000000");
    public              float           density;
    public              float           dpHeight;
    public              float           dpWidth;
    public              DatabaseAdapter dbA;
    Animation shake;
    Login forClient;
    String barcode = "";
    private             String          user;
    private             Intent          intentPasscode;
    private             int             userType           = -1;
    private             int             userId;
    private             SessionAdapter  sessionAdapter;
    private             Context         context;
    private             String          passcode           = "";
    private             String          licenseString      = "";
    private             CustomButton    licenseButton;
    private             boolean         keyboard_next_flag = false;

    // the views that show how many number has
    // been inputed in the pinpad
    private List<View> sixInputCounterViews;
    private HttpHandler httpHandler;
    private User        myUser = new User();
    public Login()
    {
    }

    public static Login newInstance()
    {
        return new Login();
    }


    public static void verifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }

        dbA       = new DatabaseAdapter(this);
        context   = this;
        shake     = AnimationUtils.loadAnimation(this, R.anim.shake);
        forClient = this;

        verifyStoragePermissions(this);

        // TODO
        // this is uncessary. Maybe this should happen only at the first start of the application
        // to be tested
        dbA.execOnDb("ALTER TABLE kitchen_printer ADD COLUMN single_order INTEGER DEFAULT 0;");
        dbA.execOnDb("ALTER TABLE button ADD COLUMN printer INTEGER DEFAULT -1;");
        dbA.execOnDb("ALTER TABLE client ADD COLUMN fidelity_id INTEGER DEFAULT null;");
        dbA.execOnDb("ALTER TABLE client ADD COLUMN codeValue TEXT DEFAULT null;");
        dbA.execOnDb("ALTER TABLE bill_total ADD COLUMN android_id TEXT ;");
        dbA.execOnDb("ALTER TABLE vat ADD COLUMN perc INTEGER DEFAULT NULL;");
        dbA.execOnDb("ALTER TABLE device_info ADD COLUMN store_name TEXT DEFAULT NULL;");


        // TODO
        // this function empty various table at login. Why?
        dropInitial();


        sixInputCounterViews = Arrays.asList(
                findViewById(R.id.first_d),
                findViewById(R.id.second_d),
                findViewById(R.id.third_d),
                findViewById(R.id.fourth_d),
                findViewById(R.id.fifth_d),
                findViewById(R.id.sixth_d)
        );

        // setup the pin buttons
        setupDigits();

//        dbA.execOnDb("INSERT INTO general_settings(reservation_timer) values(1)");

        dbA.deleteWrongProduct();

        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // TODO change password, don't use a static one
        // dbA.insertUser("admin", "", "admin@me.com", "463791", 0, "463791");

        dbA.setupDefaultPaymentValues();

        // dbA.execOnDb("insert into fattura(numero_fattura) values(0)");

        // TODO
        insertDurationCode();

        // check if the app was started after a lock (status = `pindpad`)
        // or if it's a first start (status = null)
        Intent startIntent = getIntent();

        String status = startIntent.getStringExtra("status");
        if (status == null)
            { setupLoginLayout(); }

        else if (status.equals("pinpad"))
        {
            int isAdmin = (int) startIntent.getExtras().get("userType");

            allowAccess(true, isAdmin);
        }

    }


    // ---- SETUP ---- //

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

        try
        {
            jsonObject = new JSONObject(output);
            route      = jsonObject.getString("route");
            success    = jsonObject.getBoolean("success");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "[processFinish] " + route + "success: " + success);

        if (success)
        {
            boolean check = false;

            try
            {
                jsonObject = new JSONObject(output);

                Log.i(TAG, "route response: " + output);

                switch (route)
                {
                    case "login":
                        check = jsonObject.getBoolean("check");
                        JSONArray usersObject = new JSONObject(output).getJSONArray("users");
                        ArrayList<User> userList = User.fromJsonArray(usersObject);
                        dbA.updateUserList(userList);
                        if (check)
                        {

                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            myUser = User.fromJson(jObject);
                            user   = myUser.getEmail();
                            allowAccess(check, myUser.getUserRole());
                        }
                        else
                        {
                            allowAccess(check, 0);
                        }

                        break;

                    case "getSessionTime":
                        int sessionInt = jsonObject.getInt("sessionInt");
                        checkSession(sessionInt);
                        break;

                    case "insertUser":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            myUser = User.fromJson(jObject);
                            dbA.insertUserFromServer(myUser);
                            findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                            findViewById(R.id.AddUserWindow).setVisibility(View.GONE);

                            setupAdminWindow();
                        }
                        else
                        {
                            httpHandler          = new HttpHandler();
                            httpHandler.delegate = this;
                            Toast.makeText(context, "This user already exist", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.AddUserWindow).startAnimation(shake);
                        }
                        break;

                    case "saveNewSession":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            String startTime   = jsonObject.getString("startTime");
                            String endTime     = jsonObject.getString("endTime");
                            String sessionName = jsonObject.getString("sessionName");
                            dbA.saveNewSessionTime(startTime, endTime, sessionName);
                            sessionAdapter.notifyDataSetChanged();
                            RecyclerView session_recycler = (RecyclerView) findViewById(R.id.session_time_recycler);
                            session_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                            session_recycler.setHasFixedSize(true);
                            sessionAdapter = new SessionAdapter(context, dbA);
                            session_recycler.setAdapter(sessionAdapter);

                            DividerItemDecoration divider = new
                                    DividerItemDecoration(getApplicationContext(),
                                                          DividerItemDecoration.VERTICAL
                            );
                            divider.setDrawable(ContextCompat.getDrawable(getBaseContext(),
                                                                          R.drawable.divider_line_horizontal1dp
                            ));
                            session_recycler.addItemDecoration(divider);
                            final CustomEditText newSessionNameContainer = (CustomEditText) findViewById(R.id.new_session_name_et);
                            CustomButton         startTimeContainer      = (CustomButton) findViewById(R.id.start_session_button_et);
                            CustomButton         endTimeContainer        = (CustomButton) findViewById(R.id.end_session_button_et);
                            newSessionNameContainer.setText("");
                            startTimeContainer.setText("Start Time");
                            endTimeContainer.setText("End Time");
                            newSessionNameContainer.setText("");
                            startTimeContainer.setText("Start Time");
                            endTimeContainer.setText("End Time");
                        }
                        else
                        {

                        }

                        break;
                    case "updateUser":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            myUser = User.fromJson(jObject);
                            String oldPassword = jsonObject.getString("oldPassword");
                            //dbA.updateUser(myUser.getName(), myUser.getSurname(), myUser.getPasscode(), myUser.getEmail(), myUser.getUserRole(), myUser.getId());
                            dbA.updateUserByPasscode(myUser.getName(), myUser.getSurname(), myUser.getPasscode(), myUser.getEmail(), myUser.getUserRole(), myUser.getId(), oldPassword);
                            dbA.showData("user");
                            setupNewUserWindow();
                        }
                        else
                        {
                            findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(context, "passcode already in use", Toast.LENGTH_SHORT).show();
                        }
                        break;


                    case "azzeramentoScontrini":
                        Log.i("SERVER RESPONSE FROM", route);
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            Intent intent = new Intent(getApplicationContext(), SplashScreen1.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "error in azzeramentoOrdini ", Toast.LENGTH_SHORT).show();

                        }
                        break;
                    case "chiusuraCassa":
                        Log.i("SERVER RESPONSE FROM", route);
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            Intent intent = new Intent(getApplicationContext(), SplashScreen1.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "esistono scontrini aperti", Toast.LENGTH_SHORT).show();

                        }
                        break;

                    default:
                        break;
                }

            }

            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        else
        {
            Toast.makeText(this,
                           getString(R.string.error_blackbox_comm, StaticValue.blackboxInfo.getName(), StaticValue.blackboxInfo.getAddress()),
                           Toast.LENGTH_LONG
            ).show();
        }
    }


    public void callHttpHandler(String route, List<NameValuePair> params)
    {
        httpHandler          = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }


    public void dropInitial()
    {
        dbA.execOnDb("delete from temp_table");
        dbA.execOnDb("delete from bill_total");
        dbA.execOnDb("delete from product_bill");
        dbA.execOnDb("delete from modifier_bill");
        dbA.execOnDb("delete from modifier_bill_notes");
        dbA.execOnDb("delete from bill_subdivision_paid");
        dbA.execOnDb("delete from item_subdivisions");
        dbA.execOnDb("delete from bill_total_credit");
        dbA.execOnDb("delete from bill_total_extra");
        dbA.execOnDb("delete from product_unspec_bill");
        dbA.execOnDb("delete from customer_bill");
        dbA.execOnDb("delete from table_use");
        dbA.execOnDb("delete from item_paid_spec");
    }


    private void setupDigits()
    {
        RelativeLayout digitContainer = findViewById(R.id.digits_container);
        View           v;

        for (int i = 0; i < digitContainer.getChildCount(); i++)
        {
            v = digitContainer.getChildAt(i);
            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    char digit = (((CustomButton) v).getText().charAt(0));
                    if (passcode.length() < 6)
                    {
                        passcode += digit;
                        sixInputCounterViews.get(passcode.length() - 1).setBackgroundColor(red);
                        if (passcode.length() == 6)
                        {
                            checkLogin();
                        }
                    }

                    else if (passcode.length() == 6)
                    {
                        checkLogin();
                    }
                }
            });
        }
    }

    // reset the six input contianer to black color
    private void resetFields()
    {
        for (View v : sixInputCounterViews)
        {
            v.setBackgroundColor(black);
        }
        ;

        passcode = "";
    }


    private void setupLoginLayout()
    {
        Display        display    = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density  = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;/// density;
        dpWidth  = outMetrics.widthPixels;/// density;

        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (passcode.length() <= 6 && passcode.length() > 0)
                {
                    int position = passcode.length();
                    passcode = new StringBuilder(passcode).deleteCharAt(position - 1).toString();
                    sixInputCounterViews.get(position - 1).setBackgroundColor(black);
                }
            }
        });


        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkLogin();
            }
        });
    }


    public void setupAdminWindow()
    {

        findViewById(R.id.configure_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (userType != 0)
                {
                    Toast.makeText(Login.this, R.string.sorry_you_dont_have_permission, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.putExtra("isAdmin", 0);
                    intent.putExtra("username", user);
                    startActivity(intent);
                    finish();
                }
            }
        });

        findViewById(R.id.operation_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                long rowCount = dbA.getTablesRowCount("button");
                if (rowCount > 2)
                {
                    Intent intent = new Intent(Login.this, Operative.class);
                    intent.putExtra("isAdmin", userType);
                    intent.putExtra("username", user);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(getBaseContext(), R.string.please_configure_at_least_one_button, Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.addUser_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findViewById(R.id.AdminWindow).setVisibility(View.GONE);
                findViewById(R.id.AddUserWindow).setVisibility(View.VISIBLE);
                setupNewUserWindow();
            }
        });
        findViewById(R.id.switchUser_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findViewById(R.id.AdminWindow).setVisibility(View.GONE);
                findViewById(R.id.LoginWindow).setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), PinpadActivity.class);
                startActivity(intent);
                finish();
                setupLoginLayout();
            }
        });
        findViewById(R.id.switchUser_button).setOnLongClickListener(new View.OnLongClickListener()
        {

            @Override
            public boolean onLongClick(View view)
            {

                return true;
            }
        });
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findViewById(R.id.AdminWindow).setVisibility(View.GONE);
                findViewById(R.id.LoginWindow).setVisibility(View.VISIBLE);
                setupLoginLayout();
            }
        });
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            }
        });

        findViewById(R.id.printerOption_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openPopupForPrinterMethod();
            }
        });

        findViewById(R.id.cashstatus_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (dbA.checkIfCashManagementIsSet() == 1)
                {
                    openCashStatusPopup();

                    //open cash drawer
                    if (StaticValue.printerName.equals("ditron"))
                    {
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
                else
                {
                    Toast.makeText(context, R.string.please_fill_cash_management_first, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void openPopupForPrinterMethod()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_printer_method, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        popupView.post(new Runnable()
        {
            @Override
            public void run()
            {
                setUpPrinterMethodPopup(popupView, popupWindow);


            }
        });
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    public void setUpPrinterMethodPopup(final View popupView, final PopupWindow popupWindow)
    {

        popupView.findViewById(R.id.azzeramento_ordini).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                azzeramentoNumeroScontrini();
            }
        });
        popupView.findViewById(R.id.print_report_x).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                printeReport(1);
                popupWindow.dismiss();

            }
        });
        popupView.findViewById(R.id.chiusura_fiscale_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chiusuraCassa();
                popupWindow.dismiss();
            }
        });
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
                popupWindow.dismiss();
            }
        });
    }

    public void openCashStatusPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_cashstatus, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        dbA.showData("cash_management_set");

        setupDismissKeyboard(popupView);

        setupCashStatus(popupView, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    public void setupCashStatus(final View popupView, final PopupWindow popupWindow)
    {
        CustomEditText total_deposit    = (CustomEditText) popupView.findViewById(R.id.total_deposit);
        CustomEditText five_cents       = (CustomEditText) popupView.findViewById(R.id.amount_005);
        CustomEditText ten_cents        = (CustomEditText) popupView.findViewById(R.id.amount_010);
        CustomEditText twenty_cents     = (CustomEditText) popupView.findViewById(R.id.amount_020);
        CustomEditText fifty_cents      = (CustomEditText) popupView.findViewById(R.id.amount_050);
        CustomEditText one_euro         = (CustomEditText) popupView.findViewById(R.id.amount_100);
        CustomEditText two_euros        = (CustomEditText) popupView.findViewById(R.id.amount_200);
        CustomEditText five_euros       = (CustomEditText) popupView.findViewById(R.id.amount_500);
        CustomEditText ten_euros        = (CustomEditText) popupView.findViewById(R.id.amount_1000);
        CustomEditText twenty_euros     = (CustomEditText) popupView.findViewById(R.id.amount_2000);
        CustomEditText fifty_euros      = (CustomEditText) popupView.findViewById(R.id.amount_5000);
        CustomEditText hundred_euros    = (CustomEditText) popupView.findViewById(R.id.amount_10000);
        CustomEditText twohundred_euros = (CustomEditText) popupView.findViewById(R.id.amount_20000);

        RelativeLayout deposit_window         = (RelativeLayout) popupView.findViewById(R.id.deposit_window);
        RelativeLayout withdraw_amount_window = (RelativeLayout) popupView.findViewById(R.id.withdrawamount_window);

        withdraw_amount_window.setVisibility(View.GONE);
        deposit_window.setVisibility(View.VISIBLE);

        DecimalFormat twoD = new DecimalFormat("#.00");

        ImageButton okButton = (ImageButton) popupView.findViewById(R.id.ok);
        ImageButton cancel   = (ImageButton) popupView.findViewById(R.id.kill);

        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (deposit_window.getVisibility() == View.VISIBLE)
                {
                    if (!total_deposit.getText().toString().equals(""))
                    {

                        String total_deposit_string = total_deposit.getText().toString();

                        //all other fields not filled
                        if (five_cents.getText().toString().equals("") && ten_cents.getText().toString().equals("") && twenty_cents.getText().toString().equals("")
                                && fifty_cents.getText().toString().equals("") && one_euro.getText().toString().equals("") && two_euros.getText().toString().equals("")
                                && five_euros.getText().toString().equals("") && ten_euros.getText().toString().equals("") && twenty_euros.getText().toString().equals("")
                                && fifty_euros.getText().toString().equals("") && hundred_euros.getText().toString().equals("")
                                && twohundred_euros.getText().toString().equals(""))
                        {

                            dbA.insertTotalDeposit(Float.parseFloat(total_deposit_string));

                            total_deposit.setText("");
                        }
                        else
                        {
                            String fiveCents   = five_cents.getText().toString();
                            String tenCents    = ten_cents.getText().toString();
                            String twentyCents = twenty_cents.getText().toString();
                            String fiftyCents  = fifty_cents.getText().toString();
                            String oneE        = one_euro.getText().toString();
                            String twoE        = two_euros.getText().toString();
                            String fiveE       = five_euros.getText().toString();
                            String tenE        = ten_euros.getText().toString();
                            String twentyE     = twenty_euros.getText().toString();
                            String fiftyE      = fifty_euros.getText().toString();
                            String hundred     = hundred_euros.getText().toString();
                            String twoHundred  = twohundred_euros.getText().toString();

                            dbA.insertCashStatus(Float.parseFloat(total_deposit_string.replace(",", ".")),
                                                 fiveCents.equals("") ? 0 : Integer.parseInt(fiveCents), tenCents.equals("") ? 0 : Integer.parseInt(tenCents),
                                                 twentyCents.equals("") ? 0 : Integer.parseInt(twentyCents), fiftyCents.equals("") ? 0 : Integer.parseInt(fiftyCents),
                                                 oneE.equals("") ? 0 : Integer.parseInt(oneE), twoE.equals("") ? 0 : Integer.parseInt(twoE),
                                                 fiveE.equals("") ? 0 : Integer.parseInt(fiveE), tenE.equals("") ? 0 : Integer.parseInt(tenE),
                                                 twentyE.equals("") ? 0 : Integer.parseInt(twentyE), fiftyE.equals("") ? 0 : Integer.parseInt(fiftyE),
                                                 hundred.equals("") ? 0 : Integer.parseInt(hundred), twoHundred.equals("") ? 0 : Integer.parseInt(twoHundred)
                            );

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

                        CashManagement cash        = dbA.getCashManagement();
                        CashManagement cash_static = dbA.getCashManagementStatic();

                        if (dbA.checkIfCashTotalIsDifferent() >= cash_static.getMinWithdraw())
                        {
                            CustomEditText five_cents_a       = (CustomEditText) popupView.findViewById(R.id.amount_005a);
                            CustomEditText ten_cents_a        = (CustomEditText) popupView.findViewById(R.id.amount_010a);
                            CustomEditText twenty_cents_a     = (CustomEditText) popupView.findViewById(R.id.amount_020a);
                            CustomEditText fifty_cents_a      = (CustomEditText) popupView.findViewById(R.id.amount_050a);
                            CustomEditText one_euro_a         = (CustomEditText) popupView.findViewById(R.id.amount_100a);
                            CustomEditText two_euros_a        = (CustomEditText) popupView.findViewById(R.id.amount_200a);
                            CustomEditText five_euros_a       = (CustomEditText) popupView.findViewById(R.id.amount_500a);
                            CustomEditText ten_euros_a        = (CustomEditText) popupView.findViewById(R.id.amount_1000a);
                            CustomEditText twenty_euros_a     = (CustomEditText) popupView.findViewById(R.id.amount_2000a);
                            CustomEditText fifty_euros_a      = (CustomEditText) popupView.findViewById(R.id.amount_5000a);
                            CustomEditText hundred_euros_a    = (CustomEditText) popupView.findViewById(R.id.amount_10000a);
                            CustomEditText twohundred_euros_a = (CustomEditText) popupView.findViewById(R.id.amount_20000a);

                            double amount = dbA.checkIfCashTotalIsDifferent();

                            int[] counter = {cash.getTwoHundredEuros() - cash_static.getTwoHundredEuros(), cash.getHundredEuros() - cash_static.getHundredEuros(),
                                    cash.getFiftyEuros() - cash_static.getFiftyEuros(), cash.getTwentyEuros() - cash_static.getTwentyEuros(),
                                    cash.getTenEuros() - cash_static.getTenEuros(), cash.getFiveEuros() - cash_static.getFiveEuros(),
                                    cash.getTwoEuros() - cash_static.getTwoEuros(), cash.getOneEuros() - cash_static.getOneEuros(), cash.getFiftyCents() - cash_static.getFiftyCents(),
                                    cash.getTwentyCents() - cash_static.getTwentyCents(), cash.getTenCents() - cash_static.getTenCents(),
                                    cash.getFiveCents() - cash_static.getFiveCents()};

                            withdraw_amount_window.setVisibility(View.VISIBLE);
                            deposit_window.setVisibility(View.GONE);

                            DecimalFormat  twoD            = new DecimalFormat("#.00");
                            CustomEditText withdraw_amount = (CustomEditText) popupView.findViewById(R.id.withdraw_amount);
                            withdraw_amount.setText(twoD.format(amount).replace(".", ","));

                            if (counter[11] <= 0)
                            {
                                five_cents_a.setText("");
                            }
                            else
                            {
                                five_cents_a.setText(counter[11] + "");
                            }
                            if (counter[10] <= 0)
                            {
                                ten_cents_a.setText("");
                            }
                            else
                            {
                                ten_cents_a.setText(counter[10] + "");
                            }
                            if (counter[9] <= 0)
                            {
                                twenty_cents_a.setText("");
                            }
                            else
                            {
                                twenty_cents_a.setText(counter[9] + "");
                            }
                            if (counter[8] <= 0)
                            {
                                fifty_cents_a.setText("");
                            }
                            else
                            {
                                fifty_cents_a.setText(counter[8] + "");
                            }
                            if (counter[7] <= 0)
                            {
                                one_euro_a.setText("");
                            }
                            else
                            {
                                one_euro_a.setText(counter[7] + "");
                            }
                            if (counter[6] <= 0)
                            {
                                two_euros_a.setText("");
                            }
                            else
                            {
                                two_euros_a.setText(counter[6] + "");
                            }
                            if (counter[5] <= 0)
                            {
                                five_euros_a.setText("");
                            }
                            else
                            {
                                five_euros_a.setText(counter[5] + "");
                            }
                            if (counter[4] <= 0)
                            {
                                ten_euros_a.setText("");
                            }
                            else
                            {
                                ten_euros_a.setText(counter[4] + "");
                            }
                            if (counter[3] <= 0)
                            {
                                twenty_euros_a.setText("");
                            }
                            else
                            {
                                twenty_euros_a.setText(counter[3] + "");
                            }
                            if (counter[2] <= 0)
                            {
                                fifty_euros_a.setText("");
                            }
                            else
                            {
                                fifty_euros_a.setText(counter[2] + "");
                            }
                            if (counter[1] <= 0)
                            {
                                hundred_euros_a.setText("");
                            }
                            else
                            {
                                hundred_euros_a.setText(counter[1] + "");
                            }
                            if (counter[0] <= 0)
                            {
                                twohundred_euros_a.setText("");
                            }
                            else
                            {
                                twohundred_euros_a.setText(counter[0] + "");
                            }
                        }
                        else
                        {
                            popupWindow.dismiss();
                        }

                    }
                    else
                    {
                        Toast.makeText(context, R.string.please_fill_withdraw_value, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    popupWindow.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
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

    /**
     * show setup new session windows to create sessions, only admin can do this
     */
    public void setupNewSessionWindow()
    {

        RecyclerView session_recycler = findViewById(R.id.session_time_recycler);
        session_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        session_recycler.setHasFixedSize(true);
        sessionAdapter = new SessionAdapter(this, dbA);
        session_recycler.setAdapter(sessionAdapter);

        DividerItemDecoration divider = new
                DividerItemDecoration(this,
                                      DividerItemDecoration.VERTICAL
        );
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(),
                                                      R.drawable.divider_line_horizontal1dp
        ));
        session_recycler.addItemDecoration(divider);


        String               startTime               = "";
        String               endTime                 = "";
        final CustomEditText newSessionNameContainer = findViewById(R.id.new_session_name_et);
        newSessionNameContainer.setText("");
        CustomButton startTimeContainer = findViewById(R.id.start_session_button_et);
        startTimeContainer.setText(R.string.startTime);
        CustomButton endTimeContainer = findViewById(R.id.end_session_button_et);
        endTimeContainer.setText(R.string.endTime);
        startTimeContainer.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                Calendar         mcurrentTime = Calendar.getInstance();
                int              hour         = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int              minute       = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Login.this, 4, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                    {
                        String formattedHours   = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes = new DecimalFormat("00").format((selectedMinute));
                        startTimeContainer.setText(formattedHours + ":" + formattedMinutes);
                        String startTime = formattedHours + ":" + formattedMinutes + ":00";

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        endTimeContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                Calendar         mcurrentTime = Calendar.getInstance();
                int              hour         = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int              minute       = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Login.this, 4, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                    {
                        String formattedHours   = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes = new DecimalFormat("00").format((selectedMinute));
                        endTimeContainer.setText(formattedHours + ":" + formattedMinutes);
                        String endTime = formattedHours + ":" + formattedMinutes + ":00";
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        /**
         * OK Button behavior while in New User window
         */

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final CustomEditText newSessionNameContainer = findViewById(R.id.new_session_name_et);
                CustomButton         startTimeContainer      = findViewById(R.id.start_session_button_et);
                CustomButton         endTimeContainer        = findViewById(R.id.end_session_button_et);
                String               sessionName             = newSessionNameContainer.getText().toString();
                String               startTime               = startTimeContainer.getText().toString();
                String               endTime                 = endTimeContainer.getText().toString();
                if (!sessionName.equals("") && !startTime.equals("Start Time") && !endTime.equals("End Time"))
                {
                    dbA.saveNewSessionTime(startTime + ":00", endTime + ":00", sessionName);
//                    sessionAdapter.notifyDataSetChanged();
                    RecyclerView session_recycler = findViewById(R.id.session_time_recycler);
                    session_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    session_recycler.setHasFixedSize(true);
                    sessionAdapter = new SessionAdapter(context, dbA);
                    session_recycler.setAdapter(sessionAdapter);

                    DividerItemDecoration divider = new
                            DividerItemDecoration(getApplicationContext(),
                                                  DividerItemDecoration.VERTICAL
                    );
                    divider.setDrawable(ContextCompat.getDrawable(getBaseContext(),
                                                                  R.drawable.divider_line_horizontal1dp
                    ));
                    session_recycler.addItemDecoration(divider);

                    newSessionNameContainer.setText("");
                    startTimeContainer.setText(R.string.startTime);
                    endTimeContainer.setText(R.string.endTime);
                }
                else
                {
                    Toast.makeText(Login.this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();

                }
            }
        });
        /**
         *  X button behavior while in New User window
         */
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                findViewById(R.id.SessionWindow).setVisibility(View.GONE);
                setupAdminWindow();

            }
        });
    }


    // --- LOGIN ---- //

    // used by SessionAdapter
    public void setButtonSet(int sessionTimeId, String sessionName, String start, String end)
    {
        final CustomEditText newSessionNameContainer = findViewById(R.id.new_session_name_et);
        newSessionNameContainer.setText(sessionName);

        CustomButton startTimeContainer = findViewById(R.id.start_session_button_et);
        startTimeContainer.setText(start);

        CustomButton endTimeContainer = findViewById(R.id.end_session_button_et);
        endTimeContainer.setText(end);

        startTimeContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                Calendar         mcurrentTime = Calendar.getInstance();
                int              hour         = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int              minute       = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Login.this, 4, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                    {
                        String formattedHours   = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes = new DecimalFormat("00").format((selectedMinute));
                        startTimeContainer.setText(formattedHours + ":" + formattedMinutes);
                        String startTime = formattedHours + ":" + formattedMinutes + ":00";

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        endTimeContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                Calendar         mcurrentTime = Calendar.getInstance();
                int              hour         = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int              minute       = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Login.this, 4, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                    {
                        String formattedHours   = new DecimalFormat("00").format((selectedHour));
                        String formattedMinutes = new DecimalFormat("00").format((selectedMinute));
                        endTimeContainer.setText(formattedHours + ":" + formattedMinutes);
                        String endTime = formattedHours + ":" + formattedMinutes + ":00";
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        /**
         * OK Button behavior while in New User window
         */
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final CustomEditText newSessionNameContainer = findViewById(R.id.new_session_name_et);
                CustomButton         startTimeContainer      = findViewById(R.id.start_session_button_et);
                CustomButton         endTimeContainer        = findViewById(R.id.end_session_button_et);
                String               sessionName             = newSessionNameContainer.getText().toString();
                String               startTime               = startTimeContainer.getText().toString();
                String               endTime                 = endTimeContainer.getText().toString();
                if (!sessionName.equals("") && !startTime.equals("Start Time") && !endTime.equals("End Time"))
                {
                    dbA.updateNewSessionTime(sessionTimeId, startTime + ":00", endTime + ":00", sessionName);
                    setupNewSessionWindow();
                }
                else
                {
                    Toast.makeText(Login.this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();

                }
            }
        });

        /**
         *  X button behavior while in New User window
         */
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupNewSessionWindow();

            }
        });
    }

    public void setupNewUserWindow()
    {
        RecyclerView user_recycler = findViewById(R.id.users_recycler);
        user_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        user_recycler.setHasFixedSize(true);
        UserAdapter userAdapter = new UserAdapter(this, dbA, userType, userId, null, null);
        user_recycler.setAdapter(userAdapter);


        final CustomEditText Name = findViewById(R.id.name_et);
        Name.setText("");
        final CustomEditText Surname = findViewById(R.id.surname_et);
        Surname.setText("");
        final CustomEditText Email = findViewById(R.id.email_et);
        Email.setText("");
        final CustomEditText Passcode = findViewById(R.id.passcode_et);
        Passcode.setText("");
        //set max length to 4 for passcode and 6 for password
        //   Password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        Passcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        final ImageButton manager = findViewById(R.id.manager_checkbox);
        manager.setActivated(false);
        manager.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                manager.setActivated(!manager.isActivated());
            }
        });

        /**
         * OK Button behavior while in New User window
         */
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name     = Name.getText().toString();
                String surname  = Surname.getText().toString();
                String email    = Email.getText().toString();
                String passcode = Passcode.getText().toString();
                if (name.equals("") || surname.equals("") || email.equals("") || passcode.equals(""))
                {
                    Toast.makeText(getBaseContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }
                else if (dbA.checkIfPasscodeExists(passcode))
                {
                    Toast.makeText(getBaseContext(), R.string.passcode_is_already_used, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!Pattern.matches("^[-a-zA-Z0-9_]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                    {
                        Toast.makeText(getBaseContext(), R.string.not_a_valid_email, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String password = passcode;
                        if (StaticValue.blackbox)
                        {
                            if (manager.isActivated())
                            {
                                //dbA.insertUser(name, surname, email , passcode, 1, passcode);
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("password", passcode));
                                params.add(new BasicNameValuePair("name", name));
                                params.add(new BasicNameValuePair("surname", surname));
                                params.add(new BasicNameValuePair("email", email));
                                params.add(new BasicNameValuePair("passcode", passcode));
                                params.add(new BasicNameValuePair("userType", String.valueOf(1)));

                                callHttpHandler("/insertUser", params);
                              /*  httpHandler.UpdateInfoAsyncTask("/insertUser", params);
                                httpHandler.execute();*/
                            }
                            else /*if(cashier.isActivated())*/
                            {
                                //dbA.insertUser(name, surname, email, passcode, 2, passcode);
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("password", passcode));
                                params.add(new BasicNameValuePair("name", name));
                                params.add(new BasicNameValuePair("surname", surname));
                                params.add(new BasicNameValuePair("email", email));
                                params.add(new BasicNameValuePair("passcode", passcode));
                                params.add(new BasicNameValuePair("userType", String.valueOf(2)));
                                callHttpHandler("/insertUser", params);
                               /* httpHandler.UpdateInfoAsyncTask("/insertUser", params);
                                httpHandler.execute();*/
                            }
                        }
                        else
                        {
                            //String password = passcode;
                            if (manager.isActivated())
                            {
                                dbA.insertUser(name, surname, email, passcode, 1, passcode);
                            }
                            else /*if(cashier.isActivated())*/
                            {
                                dbA.insertUser(name, surname, email, passcode, 2, passcode);
                            }
                            findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                            findViewById(R.id.AddUserWindow).setVisibility(View.GONE);

                            setupAdminWindow();
                        }
                    }
                }
            }
        });
        /**
         *  X button behavior while in New User window
         */
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                setupAdminWindow();
            }
        });
    }

    /**
     * check if the input PIN is correct
     */
    private void checkLogin()
    {
        int isAdmin;

        // when the PIN is inserted, check if any user own this pin
        if (passcode.length() == 6)
        {
            Cursor c = dbA.fetchUserDataByPasscode(passcode);
            if (c.getCount() > 0)
            {
                if (c.moveToNext())
                {
                    isAdmin = c.getInt(c.getColumnIndex("userType"));
                    user    = c.getString(c.getColumnIndex("email"));
                    allowAccess(true, isAdmin);
                }
            }

            else
            {
                Toast.makeText(getBaseContext(), "No user found with passcode " + passcode, Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * if a correct PIN was passed,
     * allow the user access to the app
     */
    public void allowAccess(boolean allowAccess, int isAdmin)
    {
        if (!allowAccess)
        {
            Toast.makeText(getBaseContext(), "Invalid PIN", Toast.LENGTH_LONG).show();
            findViewById(R.id.LoginWindow).startAnimation(shake);
            passcode = "";
        }

        else
        {

            if (StaticValue.blackbox)
            {
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                httpHandler          = new HttpHandler();
                httpHandler.delegate = this;
                httpHandler.UpdateInfoAsyncTask("/getSessionTime", params);
                httpHandler.execute();
            }
            else
            {
                dbA.insertLoginRecord(user);
                loginFunction(isAdmin, user);
                TimerManager.setContext(getApplicationContext());
                intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);
                intentPasscode.putExtra("isAdmin", isAdmin);
                intentPasscode.putExtra("username", user);

                TimerManager.setIntentPinpad(intentPasscode);
                TimerManager.startPinpadAlert(1);
            }
        }
        resetFields();
    }


    // --- PERMISSION --- //

    /**
     * Handle the HTTP response of getSessionTime
     * and start the login
     */
    public void checkSession(int sessionInt)
    {
        switch (sessionInt)
        {
            case 0:
            {
                //start session
                dbA.saveNewSession();
                loginFunction(myUser.getUserRole(), user);
                break;
            }
            case 1:
            {
                // normal op
                loginFunction(myUser.getUserRole(), user);
                break;
            }
            case 2:
            {
                //force close
                //TODO CHANGE TO FORCE
                dbA.deleteNewSession();
                dbA.execOnDb("delete from temp_table");
                dbA.execOnDb("delete from bill_total");

                dbA.saveNewSession();
                loginFunction(myUser.getUserRole(), user);
                break;
            }
        }
    }

    public void loginFunction(int isAdmin, String username)
    {
        intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);
        intentPasscode.putExtra("isAdmin", isAdmin);
        intentPasscode.putExtra("username", username);

        Intent intentLogout = new Intent(getApplicationContext(), LogoutBroadcastReciver.class);
        intentLogout.putExtra("isAdmin", isAdmin);
        intentLogout.putExtra("username", username);


        // if the current user is Admin, open the corresponding amdinWindow
        // otherwise, move to operative
        userType = isAdmin;
        switch (isAdmin)
        {
            case 0:
            case 1:
                findViewById(R.id.LoginWindow).setVisibility(View.GONE);
                findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                setupAdminWindow();
                break;

            case 2:
                Intent intent = new Intent(Login.this, Operative.class);
                intent.putExtra("isAdmin", 2);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == PERMISSION_REQUEST_CODE)
        {
            if (permissions.length > 0 && grantResults.length > 0)
            {

                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++)
                {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        flag = false;
                    }
                }
                if (flag)
                {
                    openActivity();
                }
                else
                {
                    finish();
                }

            }
            else
            {
                finish();
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    // ---- OTHER --- //

    private void openActivity()
    {
        //add your further process after giving permission or to download images from remote server.
        //exportDatabse("");
    }

    public void insertDurationCode()
    {
        dbA.execOnDb("INSERT INTO static_activation_code(code, duration,position) VALUES('015578941326', 3 , 1)");
        dbA.execOnDb("INSERT INTO static_activation_code(code, duration,position) VALUES('023715498657', 6 , 2)");
        dbA.execOnDb("INSERT INTO static_activation_code(code, duration,position) VALUES('034446982179', 12, 3)");
        dbA.execOnDb("INSERT INTO static_activation_code(code, duration,position) VALUES('041239857924', 12, 4)");
        dbA.execOnDb("INSERT INTO static_activation_code(code, duration,position) VALUES('054545789996', 12, 5)");


      /*  Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = df.format(c);
        dbA.execOnDb("INSERT INTO registered_activation_code(code, registration) VALUES('015578941326', '"+formattedDate+"')");*/
    }

    public void azzeramentoNumeroScontrini()
    {
        if (StaticValue.blackbox)
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            httpHandler          = new HttpHandler();
            httpHandler.delegate = this;
            httpHandler.UpdateInfoAsyncTask("/azzeramentoScontrini", params);
            httpHandler.execute();
        }
        else
        {
            dbA.azzeramentoScontrini();
            dbA.updateClosingTime();
            // your code here
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
    }

    public void chiusuraCassa()
    {
        if (StaticValue.blackbox)
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            httpHandler          = new HttpHandler();
            httpHandler.delegate = this;
            httpHandler.UpdateInfoAsyncTask("/chiusuraCassa", params);
            httpHandler.execute();
        }
        else
        {
            long lastSession = dbA.getLastClosing();
            long now         = System.currentTimeMillis();
            long last        = dbA.returnBillLastDate();
            if (last >= lastSession && last <= now)
            {
                Toast.makeText(getApplicationContext(), R.string.open_bill_please_close, Toast.LENGTH_SHORT).show();
            }
            else
            {
                dbA.updateClosingTime();
                dbA.insertIntoStatistic();
                printeReport(0);


                new java.util.Timer().schedule(
                        new java.util.TimerTask()
                        {
                            @Override
                            public void run()
                            {
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

    public void printeReport(int report)
    {
        if (StaticValue.blackbox)
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("report", String.valueOf(report)));
            httpHandler          = new HttpHandler();
            httpHandler.delegate = this;
            httpHandler.UpdateInfoAsyncTask("/printReport", params);
            httpHandler.execute();
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

    public void deleteSession(int sessionId)
    {
        dbA.deleteSessionTime(sessionId);
        setupNewSessionWindow();
    }

    @Override
    public void setButtonSetPopup(int sessionTimeId, String sessionName, String start, String end, View popupview, PopupWindow popupwindow)
    {

    }

    @Override
    public void deleteSessionPopup(int sessionTimeId, View popupview, PopupWindow popupwindow)
    {

    }

    @Override
    public void onTaskEndWithResult(String success)
    {

    }

    @Override
    public void onTaskFinishGettingData(String result)
    {

    }

    @Override
    public void setModifyUser(User user, final View popupview, final PopupWindow popupWindow)
    {
        final CustomEditText Name = findViewById(R.id.name_et);
        Name.setText(user.getName());

        final CustomEditText Surname = findViewById(R.id.surname_et);
        Surname.setText(user.getSurname());

        final CustomEditText Passcode = findViewById(R.id.passcode_et);
        Passcode.setText(user.getPasscode());

        final CustomEditText Email = findViewById(R.id.email_et);
        Email.setText(user.getEmail());

        //set max length to 4 for passcode and 6 for password
        //   Password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        Passcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        final ImageButton manager = findViewById(R.id.manager_checkbox);

        if (user.getUserRole() == 1 || user.getUserRole() == 0)
        {
            manager.setActivated(true);
        }

        manager.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (user.getUserRole() != 0)
                {
                    manager.setActivated(!manager.isActivated());
                }
                else
                {
                    Toast.makeText(Login.this, R.string.you_cant_change_your_admin_role, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // OK Button behavior while in New User window
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name     = Name.getText().toString();
                String surname  = Surname.getText().toString();
                String passcode = Passcode.getText().toString();
                String email    = Email.getText().toString();
                if (name.equals("") || surname.equals("") /*|| email.equals("") */ || passcode.equals(""))
                {
                    Toast.makeText(getBaseContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }
                else if (dbA.checkIfPasscodeExistsWithId(passcode, user.getId()))
                {
                    Toast.makeText(getBaseContext(), R.string.passcode_is_already_used, Toast.LENGTH_SHORT).show();
                }
                else if (!Pattern.matches("^[-a-zA-Z0-9_]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                {
                    Toast.makeText(getBaseContext(), R.string.not_a_valid_email, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (StaticValue.blackbox)
                    {
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("password", passcode));
                        params.add(new BasicNameValuePair("oldPassword", user.getPasscode()));
                        params.add(new BasicNameValuePair("name", name));
                        params.add(new BasicNameValuePair("surname", surname));
                        params.add(new BasicNameValuePair("email", email));
                        params.add(new BasicNameValuePair("passcode", passcode));
                        params.add(new BasicNameValuePair("userType", String.valueOf(user.getUserRole())));
                        params.add(new BasicNameValuePair("id", String.valueOf(user.getId())));
                        callHttpHandler("/updateUser", params);

                    /*    httpHandler.UpdateInfoAsyncTask("/updateUser", params);
                        httpHandler.execute();*/
                    }
                    else
                    {
                        String password = passcode;
                        if (user.getUserRole() == 0)
                        {
                            dbA.updateUser(name, surname, passcode, email, 0, user.getId());
                        }
                        else
                        {
                            if (manager.isActivated())
                            {
                                dbA.updateUser(name, surname, passcode, email, 1, user.getId());
                            }
                            else /*if(cashier.isActivated())*/
                            {
                                dbA.updateUser(name, surname, passcode, email, 2, user.getId());
                            }
                        }
                        setupNewUserWindow();
                    }
                }
            }
        });

        //X button behavior while in New User window
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupNewUserWindow();
            }
        });
    }

    public void resetPinpadTimer(int type)
    {
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent e)
    {

        if (e.getAction() == KeyEvent.ACTION_DOWN)
        {
            //Toast.makeText(context,"dispatchKeyEvent: "+e.toString(), Toast.LENGTH_SHORT).show();
            char pressedKey = (char) e.getUnicodeChar();
            barcode += pressedKey;
        }
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER)
        {
            Toast.makeText(getApplicationContext(),
                           "barcode--->>>" + barcode, Toast.LENGTH_LONG
            )
                 .show();
            Log.i("BARCODE", barcode);
            barcode = "";
        }

        return super.dispatchKeyEvent(e);
    }


    public void setupDismissKeyboard(View view)
    {
        //Set up touch listener for non-text box views to hide keyboard.
        if ((view instanceof EditText))
        {
            ((EditText) view).setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_NEXT)
                    {
                        keyboard_next_flag = true;
                    }
                    return false;
                }
            });
            view.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (!hasFocus)
                    {
                        if (!(getCurrentFocus() instanceof EditText) && !keyboard_next_flag)
                        {
                            Log.d("OnFocusChange", "You clicked out of an Edit Text!");
                            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        keyboard_next_flag = false;
                    }
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupDismissKeyboard(innerView);
            }
        }
    }
}


/* --- OLD ---

    public void firstRun()
    {
        // TODO make a better check
        if (dbA.checkIfExists("select id from button where id=-30") == -11)
        {
            // TODO change password, don't use a static one
            dbA.insertUser("admin", "", "admin@me.com", "463791", 0, "463791");

            //metodi per inserire i payment option....picchiare davide a vista
            dbA.execOnDb("delete from payment_option_button;");
            dbA.setupDefaultPaymentValues();
            //inserisce il ptodotto id=-30 usato per inserire gli unspecificed products
            dbA.queryToDb("INSERT INTO button (id,title, subtitle, img_name, color, position, " +
                    "price, catID, isCat) " +
                    "VALUES( -30, 'Articolo', '', null, null, -30, 0.0, 0,0);");
            dbA.execOnDb("insert into fattura(numero_fattura) values(0)");

            dbA.execOnDb("insert into vat(value) values(5)");
            dbA.execOnDb("insert into vat(value) values(10)");
            dbA.execOnDb("insert into vat(value) values(15)");
            dbA.execOnDb("insert into vat(value) values(22)");

            insertDurationCode();

        }
    }


       public void importDatabase(String databaseName) {
        try {
            dbA.getDbHelper().importDatabase(Environment.getExternalStorageDirectory() + File.separator
                    + "Download" + File.separator + databaseName+".db", getApplicationContext().getDatabasePath("mydatabase.db").getPath());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void insertIngridients() {

        dbA.execOnDb("INSERT INTO unit(id, description) VALUES(1, 'pz.');");
        dbA.execOnDb("INSERT INTO unit(id, description) VALUES(2, 'gr.');");

        //meat patty 1
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(1, 'meat patty', 1);");
        //bread bun 2
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(2, 'bread bun', 1);");
        //guacamole 3
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(3, 'guacamole', 2);");
        //tomato 4
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(4, 'tomato', 2);");
        //salad  5
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(5, 'salad', 2);");
        //ketchup 6
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(6, 'ketchup', 2);");
        //mayo 7
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(7, 'mayo', 2);");
        //cheddar 8
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(8, 'cheddar', 2);");
        //onion 9
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(9, 'onion', 2);");
        //pickle 10
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(10, 'pickle', 2);");
        //tomino 11
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(11, 'tomino', 2);");
        //pesto anacardi 12
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(12, 'pesto anachardi', 2);");
        //mini meat patty 13
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(13, 'mini meat patty', 1);");
        //mini bread bun 14
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(14, 'mini bread bun', 1);");
        //provolone 15
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(15, 'provolone', 2);");
        //salsa argodolce 16
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(16, 'salsa agrodolce', 2);");
        //crispy bacon 17
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(17, 'crispy bacon', 2);");
        //fontina 18
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(18, 'fontina', 2);");
        //gorgonzola 19
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit) VALUES(19, 'gorgonzola', 2);");
        //vegi-patty 20
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(20, 'vegi-patty', 1);");
        //chicken 21
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(21, 'chicken', 1);");
        //BBQ 22
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(22, 'BBQ', 2);");
        //EDAMER 23
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(23, 'edamer', 2);");
        //bacon 24
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(24, 'bacon', 2);");

        //small fries 25
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(25, 'small fries', 2);");
        //spieces 26
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(26, 'spices', 2);");
        //backed potato 27
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(27, 'backed potato', 2);");
        //cheddar sauce 28
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(28, 'cheddar sauce', 2);");
        //sour cream 29
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(29, 'sour cream', 2);");
        //chives 30
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(30, 'chives', 2);");
        //potato 31
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(31, 'potatoe', 2);");
        //mayo 32
        //dbA.execOnDb("INSERT INTO ingridients(id, description, unit) VALUES(24, 'bacon', 2);");
        //capers 33
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(33, 'caper', 2);");
        //dry tomatoer 34
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(34, 'dry tomatoes', 2);");
        //salt 35
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(35, 'salt', 2);");
        //pepper 36
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(36, 'pepper', 2);");
        //green cabbage 37
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(37, 'green cabbage', 2);");
        //red cabbage 38
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(38, 'red cabbage', 2);");
        //carrot 39
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(39, 'carrot', 2);");
        //vinegar 40
        dbA.execOnDb("INSERT INTO ingridients(id, description, unit_id) VALUES(40, 'vinegar', 2);");
    }
    public void insertRecipe() {
        String BUTTON_RECIPE_CREATE = "CREATE TABLE button_recipe (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "product_id INT, " +
                "ingridient_id INT, " +
                "quantity INT, " +
                "unit INT, " +
                "FOREIGN KEY(product_id) REFERENCES button(if),"+
                "FOREIGN KEY(ingridient_id) REFERENCES ingridients(id));";

        String MODIFIER_RECIPE_CREATE = "CREATE TABLE modifier_recipe (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "modifier_id INT, " +
                "ingridient_id INT, " +
                "quantity INT, " +
                "unit INT, " +
                "FOREIGN KEY(modifier_id) REFERENCES modifier(id),"+
                "FOREIGN KEY(ingridient_id) REFERENCES ingridients(id));";

        dbA.execOnDb("drop table button_recipe");
        dbA.execOnDb("drop table modifier_recipe");
        dbA.execOnDb(BUTTON_RECIPE_CREATE);
        dbA.execOnDb(MODIFIER_RECIPE_CREATE);

        //insert Dirty Sanchez
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(3, 1, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(3, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(3, 3, 45, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(3, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(3, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(3, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(3, 7, 10, 1);");

        //jucy lucy
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 1, 2, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 8, 45, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 9,15,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 10,10,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 6,10,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(4, 7,10,1);");

        //insert Italian Stallion
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(5, 1,1,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(5, 2,1,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(5, 11,45,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(5, 12,20,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(5, 4,15,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(5, 6,10,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(5, 7,10,1);");

        //insert Disco set
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 13,3,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20,14,3,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 5,45,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 4,60,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 6,30,1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 7,30,1);");

        //insert mini burgher
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(21, 13, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20,14, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 5, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 4, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(20, 7, 10, 1);");

        //insert Simple Jack
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(7, 1, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(7, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(7, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(7, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(7, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(7, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(7, 7, 10, 1);");

        //insert mountaine girl
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 1, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 18, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(8, 7, 10, 1);");

        //insert stinky roise
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 1, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 19, 45, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 9, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(9, 7, 10, 1);");

        //insert che geddara
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 1, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 8, 30, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 9, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(10, 7, 10, 1);");

        //insert pork destiny
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 1, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 24, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 9, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(11, 7, 10, 1);");

        //insert high roller
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 1, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 17, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 8, 30, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(12, 7, 10, 1);");

        //insert vegi san
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 20, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 9, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 6, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 7, 10, 1);");

        //insert chicken deal
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 21, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 9, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 22, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(14, 7, 10, 1);");

        //insert magic chicken
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 21, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 2, 1, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 23, 25, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 4, 20, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 5, 15, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 10, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 9, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 22, 10, 1);");
        dbA.execOnDb("INSERT INTO button_recipe(product_id, ingridient_id, quantity, unit) VALUES(13, 7, 10, 1);");


        //insert modifiers
        //insert magic chicken
        dbA.execOnDb("INSERT INTO modifier_recipe(modifier_id, ingridient_id, quantity, unit) VALUES(30, 27, 250, 1);");
        dbA.execOnDb("INSERT INTO modifier_recipe(modifier_id, ingridient_id, quantity, unit) VALUES(30, 28, 30, 1);");
        dbA.execOnDb("INSERT INTO modifier_recipe(modifier_id, ingridient_id, quantity, unit) VALUES(30, 29, 20, 1);");
        dbA.execOnDb("INSERT INTO modifier_recipe(modifier_id, ingridient_id, quantity, unit) VALUES(30, 4, 15, 1);");
    }

  private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                //alertBuilder.setTitle(getString(R.string.permission_necessary));
                //alertBuilder.setMessage(R.string.storage_permission_is_encessary_to_wrote_event);
                //alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Login.this, new String[]{WRITE_EXTERNAL_STORAGE
                            , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                            }
                            });
                            AlertDialog alert = alertBuilder.create();
                            alert.show();
                            Log.e("", "permission denied, show dialog");
                            } else {
                            ActivityCompat.requestPermissions(Login.this, new String[]{WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                            }
                            } else {
                            openActivity();
                            }
                            }



    public void updateStats() {
        //add
        String TOTAL_BILL_CREATE_STATISTIC = "CREATE TABLE bill_total_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "total FLOAT, " +
                "paid INTEGER, " +
                "creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "pay_time  TIMESTAMP DEFAULT null," +
                "bill_number INTEGER,"+
                "payment_type INTEGER," +
                "invoice INTEGER DEFAULT 0," +
                "print_index INTEGER DEFAULT 0);";
        dbA.execOnDb(TOTAL_BILL_CREATE_STATISTIC);
        String TOTAL_BILL_COSTUMER_INVOICE_CREATE_STATISTIC = "CREATE TABLE bill_total_customer_invoice_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bill_total_id INTEGER, " +
                "client_id INTEGER, " +
                "FOREIGN KEY(bill_total_id) REFERENCES bill_total_statistic(id),"+
                "FOREIGN KEY(client_id) REFERENCES client(id));";
        //add
        dbA.execOnDb(TOTAL_BILL_COSTUMER_INVOICE_CREATE_STATISTIC);

        //add
        String PRODUCT_BILL_CREATE_STATISTIC= "CREATE TABLE product_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "position INTEGER, " +
                "prod_id INTEGER, " +
                "qty INTEGER, " +
                "bill_id INTEGER, " +
                "homage INTEGER DEFAULT 0," +
                "discount FLOAT DEFAULT 0,"+
                "FOREIGN KEY(bill_id) REFERENCES bill_total_statistic(id), " +
                "FOREIGN KEY(prod_id) REFERENCES button(id));";
        dbA.execOnDb(PRODUCT_BILL_CREATE_STATISTIC);
        //add
        String COSTUMER_BILL_CREATE_STATISTIC = "CREATE TABLE customer_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "position INTEGER, " +
                "description TEXT, " +
                "client_id INTEGER DEFAULT 0, " +
                "prod_bill_id INTEGER, " +
                "FOREIGN KEY(prod_bill_id) REFERENCES product_bill_statistic(id));";
        dbA.execOnDb(COSTUMER_BILL_CREATE_STATISTIC);
        //add
        String PRODUCT_BILL_UNSPECIFIC_CREATE_STATISTIC= "CREATE TABLE product_unspec_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "prod_bill_id INTEGER, " +
                "price FLOAT, " +
                "description TEXT, " +
                "FOREIGN KEY(prod_bill_id) REFERENCES product_bill_statistic(id));";
        dbA.execOnDb(PRODUCT_BILL_UNSPECIFIC_CREATE_STATISTIC);
        //add
        String MODIFIER_BILL_CREATE_STATISTIC= "CREATE TABLE modifier_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "position INTEGER, " +
                "mod_id INTEGER, " +
                "qty INTEGER, " +
                "prod_bill_id INTEGER, " +
                "homage INTEGER DEFAULT 0," +
                "discount FLOAT DEFAULT 0,"+
                "FOREIGN KEY(prod_bill_id) REFERENCES product_bill_statistic(id), " +
                "FOREIGN KEY(mod_id) REFERENCES modifier(id));";
        dbA.execOnDb(MODIFIER_BILL_CREATE_STATISTIC);
        //add
        String MODIFIER_BILL_NOTES_CREATE_STATISTIC= "CREATE TABLE modifier_bill_notes_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "modifier_bill_id INTEGER, " +
                "note TEXT, " +
                "FOREIGN KEY(modifier_bill_id) REFERENCES modifier_bill_statistic(id));";
        dbA.execOnDb(MODIFIER_BILL_NOTES_CREATE_STATISTIC);
        //add
        String TOTAL_BILL_CREDIT_CREATE_STATISTIC= "CREATE TABLE bill_total_credit_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "creditValue FLOAT, " +
                "bill_total_id INTEGER, "+
                "FOREIGN KEY(bill_total_id) REFERENCES bill_total_statistic(id));";
        dbA.execOnDb(TOTAL_BILL_CREDIT_CREATE_STATISTIC);
        //add
        String TOTAL_BILL_EXTRA_CREATE_STATISTIC = "CREATE TABLE bill_total_extra_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "discountTotal FLOAT, "+
                "homage INTEGER, " +
                "bill_total_id INTEGER, "+
                "FOREIGN KEY(bill_total_id) REFERENCES bill_total_statistic(id));";
        dbA.execOnDb(TOTAL_BILL_EXTRA_CREATE_STATISTIC);
        //payment type cash 1, credit card 2, debt card 3, ticket 4
        String BILL_SUBDIVISION_PAID_CREATE_STATISTIC = "CREATE TABLE bill_subdivision_paid_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bill_id INTEGER, " +
                "subdivision_mode INTEGER, " +
                "subdivision_value FLOAT, " +
                "paid_amount FLOAT, " +
                "payment_type INT,"+
                "discount FLOAT,"+
                "homage INT,"+
                "invoice INTEGER DEFAULT 0," +
                "FOREIGN KEY(bill_id) REFERENCES bill_total_statistic(id)); ";
        dbA.execOnDb(BILL_SUBDIVISION_PAID_CREATE_STATISTIC);
        String ITEM_SUBDIVISIONS_CREATE_STATISTIC = "CREATE TABLE item_subdivisions_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bill_subdivision_id INTEGER, " +
                "product_bill_id INTEGER, " +
                "quantity INTEGER, " +
                " discount FLOAT, " +
                " percentage INT DEFAULT 1," +
                " price DECIMAL ," +
                "FOREIGN KEY(bill_subdivision_id) REFERENCES bill_subdivision_paid_statistic(id), " +
                "FOREIGN KEY(product_bill_id) REFERENCES product_bill_statistic(id));";
        dbA.execOnDb(ITEM_SUBDIVISIONS_CREATE_STATISTIC);
        String ITEM_PAID_SPEC_CREATE_STATISTIC = "CREATE TABLE item_paid_spec_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "paid_amount FLOAT, " +
                "payment_type INTEGER, " +
                "bill_subdivision_paid_id INTEGER, " +
                "FOREIGN KEY(bill_subdivision_paid_id) REFERENCES bill_subdivision_paid_statistic(id));";
        dbA.execOnDb(ITEM_PAID_SPEC_CREATE_STATISTIC);
    }

    private void setDigitsPinpad(char digit){

        int stringSize = passcode.length();
        if(stringSize<6){
            passcode += digit;
            switch(stringSize){
                case 0 : {
                    container1.setBackgroundColor(red);
                    break;
                }
                case 1 : {
                    container2.setBackgroundColor(red);
                    break;
                }
                case 2 :{
                    container3.setBackgroundColor(red);
                    break;
                }
                case 3 :{
                    container4.setBackgroundColor(red);
                    break;
                }
                case 4 :{
                    container5.setBackgroundColor(red);
                    break;
                }
                case 5 :{
                    container6.setBackgroundColor(red);
                    break;
                }
                default : {
                    break;
                }
            }
        }
        if(passcode.length()==6) checkLogin();
    }


        public void exportDatabse(String databaseName) {
        try {
            File backupDB = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "emptyDB.db"); // for example "my_data_backup.db"
            File currentDB = getApplicationContext().getDatabasePath("mydatabase.db");
            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
            Log.i("BACKUP", "OK");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("BACKUP", "FAIL");

        }

    }




    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
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
                Toast.makeText(getApplicationContext(), R.string.cant_connect_printer, Toast.LENGTH_LONG).show();
            }
        }

    }



* */