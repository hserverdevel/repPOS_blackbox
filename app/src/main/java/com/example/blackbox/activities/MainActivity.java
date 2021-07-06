package com.example.blackbox.activities;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.database.DatabaseUtils;


import com.example.blackbox.R;
import com.example.blackbox.adapter.BlackboxSettingAdapter;
import com.example.blackbox.adapter.GridAdapter;
import com.example.blackbox.adapter.ModifierAdapter;
import com.example.blackbox.adapter.ModifiersGroupAdapter;
import com.example.blackbox.adapter.PrinterSettingAdapter;
import com.example.blackbox.adapter.SessionAdapter;
import com.example.blackbox.adapter.UserAdapter;
import com.example.blackbox.client.ClientThread;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperCallback;
import com.example.blackbox.graphics.LineSeparator;
import com.example.blackbox.graphics.ModifierLineSeparator;
import com.example.blackbox.graphics.ModifiersGroupLineSeparator;
import com.example.blackbox.model.BlackboxInfo;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashManagement;
import com.example.blackbox.model.DeviceInfo;
import com.example.blackbox.model.FiscalPrinter;
import com.example.blackbox.model.KitchenPrinter;
import com.example.blackbox.model.ModifierAssigned;
import com.example.blackbox.model.ModifierGroupAssigned;
import com.example.blackbox.model.PrinterModel;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.StatusInfoHolder;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.model.User;
import com.example.blackbox.model.Vat;
import com.example.blackbox.model.VatModel;
import com.example.blackbox.printer.PrinterDitronThread;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.example.blackbox.server.Server;
import com.utils.db.DatabaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.blackbox.model.StaticValue.deviceInfo;

public class MainActivity extends AppCompatActivity implements
        SessionAdapter.AdapterSessionCallback,
        UserAdapter.AdapterUserCallback,
        AdapterView.OnItemSelectedListener,
        ClientThread.TaskDelegate,
        HttpHandler.AsyncResponse
{

    public static final int                     DEFAULT_VIEW      = 0;
    public static final int                     MODIFIERS_VIEW    = 1;
    public static       ArrayList<ButtonLayout> buttons;
    private static      GridAdapter             rv_adapter;
    private static      ModifiersGroupAdapter   group_rv_adapter;
    private static      ModifierAdapter         modifiers_rv_adapter;
    private final       String                  TAG               = "<MainActivity>";
    public              boolean                 wereModifiersOpen = false;
    public              float                   density;
    public              float                   dpHeight;
    public              float                   dpWidth;
    public              String                  IP                = StaticValue.IP;
    String         barcode                = "";
    String         printerModel;
    KitchenPrinter selectedKitchenPrinter = new KitchenPrinter();
    int            show                   = 1;
    private HttpHandler            httpHandler;
    private View                   myPopupView;
    private PopupWindow            myPopupWindow;
    private User                   myUser             = new User();
    private ButtonLayout           myButtonLayout;
    private Vat                    myVat;
    private Context                me;
    private Resources              resources;
    private StatusInfoHolder       grid_status;
    private ButtonLayout           big_plus_button;
    private DatabaseAdapter        dbA;
    private RecyclerView           recyclerview;
    private RecyclerView           mod_group_recyclerview;
    private RecyclerView           modifiers_recyclerview;
    private GridLayoutManager      grid_manager;
    private ModifierLineSeparator  myLine;
    private boolean                isAdded            = false;
    private int                    isAdmin;
    private String                 username;
    private SessionAdapter         sessionAdapter;
    private Server                 server;
    private UserAdapter            userAdapter;
    private int                    userType;
    private int                    userId;
    private Intent                 intentPasscode;
    private PrinterSettingAdapter  printerSettingAdapter;
    private BlackboxSettingAdapter bbSettingAdaper;
    private MainActivity           myself;
    private boolean                keyboard_next_flag = false;
    private boolean                isBarcodeShow      = false;
    private String                 licenseString      = "";
    private CustomButton           licenseButton;

    /**
     * UPDATES THE BUTTON SET AND NOTIFIES IT TO THE ADAPTER
     **/
    public static void setButtonSet(ArrayList<ButtonLayout> bs)
    {
        buttons.clear();
        buttons = bs;
        rv_adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Hides app title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        me        = this;
        myself    = this;
        resources = getResources();
        dbA       = new DatabaseAdapter(this);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        isAdmin  = intent.getIntExtra("isAdmin", -1);
        userId   = intent.getIntExtra("userId", -1);
        userType = intent.getIntExtra("userType", -1);

        /**DISPLAY METRICS**/
        Display        display    = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density  = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;
        // dbA.restartDb();
        // dbA.ciccioPasticcio();

        setDefaultSettings(false);

        myLine = new ModifierLineSeparator(me, 14);
    }

    /**
     * Handles the ouput of the HttpHandler async process
     * which send a POST request to the blackbox server
     *
     * @param output: the JSON response from the blackbox after the POST request
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

        if (success || (!success && route.equals("testBlackboxComm")))
        {
            boolean check = false;

            try
            {
                jsonObject = new JSONObject(output);
                switch (route)
                {
                    case "testBlackboxComm":
                        try
                        {
                            // if check is present, is always true,
                            // since this is a simple test of communication
                            jsonObject.getBoolean("check");

                            Toast.makeText(this, "Blackbox connection succesful", Toast.LENGTH_LONG)
                                 .show();
                        }

                        // the check is not present, thus the test failed
                        catch (Exception e)
                        {
                            Toast.makeText(this, "Blackbox connection unsuccessful", Toast.LENGTH_LONG)
                                 .show();
                        }
                        break;

                    case "insertUser":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            myUser = User.fromJson(jObject);
                            dbA.insertUserFromServer(myUser);
                            myPopupView.findViewById(R.id.AdminWindow).setVisibility(VISIBLE);
                            myPopupView.findViewById(R.id.AddUserWindow).setVisibility(GONE);

                            setupAdminWindow(myPopupView, myPopupWindow);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertButton":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("button");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            dbA.insertButtonFromServer(myButtonLayout);
                            dbA.showData("button");
                            //da cambiare -1

                            rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());

                            rv_adapter.colorChosen     = -1;
                            rv_adapter.chosenColorView = null;
                            rv_adapter.chosenIcon      = "";
                            rv_adapter.closePopupWindow();
                            rv_adapter.notifyDataSetChanged();
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertButton2":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("button");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            dbA.insertButtonFromServer(myButtonLayout);
                            //da cambiare -1
                            if (rv_adapter.deepnessLevel + 1 <= rv_adapter.MAX_DEEPNESS_LEVEL + 1)
                            {
                                rv_adapter.deepnessLevel += 1;
                                rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());
                                int catID = rv_adapter.returnCatIdBlackbox();
                                rv_adapter.goToCategory(myButtonLayout.getCatID(), catID, myButtonLayout
                                        .getTitle());
                            }
                            rv_adapter.closePopupWindow();
                            rv_adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertButton3":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("button");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            dbA.insertButtonFromServer(myButtonLayout);

                            //da cambiare -1
                            rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());
                            StatusInfoHolder sh = new StatusInfoHolder();
                            sh.setCurrent_category_id(myButtonLayout.getCatID());
                            sh.setDept_level(rv_adapter.deepnessLevel);
                            sh.setPrevious_category_title(rv_adapter.currentCatTitle);
                            sh.setPrevious_category_id(rv_adapter.previousCatID);
                            sh.setCurrent_product(rv_adapter.returnProductForBlackBos());
                            setGridStatus(sh);
                            int catID = rv_adapter.returnCatIdBlackbox();
                            switchView(MainActivity.MODIFIERS_VIEW, catID);

                            rv_adapter.closePopupWindow();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertButton4":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("button");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            dbA.insertButtonFromServer(myButtonLayout);
                            rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());
                            rv_adapter.closePopupWindow();
                            rv_adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertButton5":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("button");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            dbA.insertButtonFromServer(myButtonLayout);
                            dbA.showData("button");
                            rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());
                            if (!rv_adapter.colorState)
                            {

                            }
                            else
                            {

                                rv_adapter.myPopupView.findViewById(R.id.ProductSetup)
                                                      .setVisibility(VISIBLE);
                                rv_adapter.myPopupView.findViewById(R.id.colors_container)
                                                      .setVisibility(GONE);
                                rv_adapter.colorState = false;
                            }

                            rv_adapter.closePopupWindow();
                            rv_adapter.notifyDataSetChanged();
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, jsonObject
                                    .getString("reason")), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "moveButton":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int fromPosition = jsonObject.getInt("fromPosition");
                            int toPosition   = jsonObject.getInt("toPosition");

                            rv_adapter.swapButtonFunction(fromPosition, toPosition);
                            rv_adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "updateButton":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject        = new JSONObject(output).getJSONObject("button");
                            Boolean    updatePrinter  = jsonObject.getBoolean("updatePrinter");
                            Boolean    updateDiscount = jsonObject.getBoolean("updateDiscount");
                            Boolean    updateCredit   = jsonObject.getBoolean("updateCredit");
                            String     action         = jsonObject.getString("action");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            dbA.execOnDb("UPDATE button SET title = \"" + myButtonLayout.getTitle()
                                                                                        .replaceAll("'", "'") +
                                                 "\", subtitle = \"" + myButtonLayout.getSubTitle()
                                                                                     .replaceAll("'", "'") + "\", img_name = \"" + myButtonLayout
                                    .getImg() + "\"," +
                                                 "color = " + myButtonLayout.getColor() + " , printer= " + myButtonLayout
                                    .getPrinterId() + ", fidelity_discount=" + myButtonLayout.getFidelity_discount() + ", fidelity_credit=" + myButtonLayout
                                    .getFidelity_credit() + " WHERE id = " + myButtonLayout.getID() + "");
                            if (updatePrinter)
                            {
                                dbA.recursiveUpdatePrinter(myButtonLayout.getID(), myButtonLayout.getPrinterId());
                            }
                            if (updateDiscount)
                            {
                                dbA.recursiveUpdateFidelityDiscount(myButtonLayout.getID(), myButtonLayout
                                        .getFidelity_discount());
                            }
                            if (updateCredit)
                            {
                                dbA.recursiveUpdateFidelityCredit(myButtonLayout.getID(), myButtonLayout
                                        .getFidelity_credit());
                            }
                            if (action.equals("update"))
                            {
                                rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());
                            }
                            else
                            {
                                rv_adapter.goToCategory(myButtonLayout.getCatID(), myButtonLayout.getID(), myButtonLayout
                                        .getTitle());
                            }
                            rv_adapter.closePopupWindow();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "updateButton2":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("button");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            dbA.execOnDb("UPDATE button SET title = \"" + myButtonLayout.getTitle()
                                                                                        .replaceAll("'", "'") +
                                                 "\", subtitle = \"" + myButtonLayout.getSubTitle()
                                                                                     .replaceAll("'", "'") + "\", img_name = \"" + myButtonLayout
                                    .getImg() + "\", isCat = " + myButtonLayout.getCat() + ", price= " + myButtonLayout
                                    .getPrice() + ", vat=" + myButtonLayout.getVat() +
                                                 ",color = " + myButtonLayout.getColor() + " , printer= " + myButtonLayout
                                    .getPrinterId() + ", fidelity_discount=" + myButtonLayout.getFidelity_discount() + ", fidelity_credit=" + myButtonLayout
                                    .getFidelity_credit() + ", credit_value=" + myButtonLayout.getCredit_value() + " WHERE id = " + myButtonLayout
                                    .getID() + "");
                            dbA.showData("button");
                            //dbA.insertButtonFromServer(myButtonLayout);
                            rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());
                            rv_adapter.closePopupWindow();
                            rv_adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "updateButton3":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("button");
                            myButtonLayout = ButtonLayout.fromJson(jObject);
                            //dbA.insertButtonFromServer(myButtonLayout);
                            dbA.execOnDb("UPDATE button SET title = \"" + myButtonLayout.getTitle()
                                                                                        .replaceAll("'", "'") +
                                                 "\", subtitle = \"" + myButtonLayout.getSubTitle()
                                                                                     .replaceAll("'", "'") + "\", img_name = \"" + myButtonLayout
                                    .getImg() + "\", isCat = " + myButtonLayout.getCat() + ", price= " + myButtonLayout
                                    .getPrice() + ", vat=" + myButtonLayout.getVat() +
                                                 ",color = " + myButtonLayout.getColor() + " , printer= " + myButtonLayout
                                    .getPrinterId() + ", fidelity_discount=" + myButtonLayout.getFidelity_discount() + ", fidelity_credit=" + myButtonLayout
                                    .getFidelity_credit() + ", credit_value=" + myButtonLayout.getCredit_value() + " WHERE id = " + myButtonLayout
                                    .getID() + "");

                            //da cambiare -1
                            dbA.showData("button");
                            rv_adapter.getCurrentCatButtonSet(myButtonLayout.getCatID());
                            StatusInfoHolder sh = new StatusInfoHolder();
                            sh.setCurrent_category_id(myButtonLayout.getCatID());
                            sh.setDept_level(rv_adapter.deepnessLevel);
                            sh.setPrevious_category_title(rv_adapter.currentCatTitle);
                            sh.setPrevious_category_id(rv_adapter.previousCatID);
//                        sh.setCurrent_product(rv_adapter.returnProductForBlackBos());
                            sh.setCurrent_product(myButtonLayout);

                            setGridStatus(sh);
                            int catID = rv_adapter.returnCatIdBlackbox();
                            switchView(MainActivity.MODIFIERS_VIEW, /**catID*/myButtonLayout.getID());

                            rv_adapter.closePopupWindow();


                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "deleteButton":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int id         = jsonObject.getInt("id");
                            int currentCat = jsonObject.getInt("currentId");
                            dbA.deleteButton(id, 1);
                            ArrayList<ButtonLayout> bs = dbA.fetchButtonsByQuery("Select * from button where catID=" + currentCat + " AND id!=-30 AND id!=-20");
                            for (int i = 0; i < bs.size(); i++)
                            {
                                dbA.execOnDb("UPDATE button set position=" + (i + 1) + " WHERE id=" + bs
                                        .get(i)
                                        .getID());
                            }
                            rv_adapter.getCurrentCatButtonSet(currentCat);
                            rv_adapter.closeTwoPopupWindow();
                            rv_adapter.notifyDataSetChanged();
                            // if the current button to be deleted is present in an existing unpaid order
                            // or it's present in any of its subcategories or products
                            // cancel the delete
                        }
                        else if (jsonObject.getString("reason").equals("buttonInOrder"))
                        {
                            Toast.makeText(getApplicationContext(),
                                           "This button or any of its sub-buttons is present in order N. " + (jsonObject
                                                   .getInt("billNumber") + 1),
                                           Toast.LENGTH_LONG
                            ).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertVat":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int status = jsonObject.getInt("status");
                            switch (status)
                            {
                                case 1:
                                    JSONObject jObject = new JSONObject(output).getJSONObject("vat");
                                    myVat = Vat.fromJson(jObject);
                                    dbA.addVatValueFromServer(myVat.getId(), myVat.getValue(), myVat
                                            .getPerc());
                                    ArrayList<VatModel> newVats = dbA.fetchVatArrayByQuery();
                                    rv_adapter.setVatFromServer(newVats, myPopupView);
                                    break;
                                case 2:
                                    JSONObject jObject1 = new JSONObject(output).getJSONObject("vat");
                                    myVat = Vat.fromJson(jObject1);
                                    dbA.updateVatValueFromServer(myVat.getId(), myVat.getValue(), myVat
                                            .getPerc());
                                    ArrayList<VatModel> newVats1 = dbA.fetchVatArrayByQuery();
                                    rv_adapter.setVatFromServer(newVats1, myPopupView);
                                    break;
                                case 3:
                                    break;
                                default:
                                    break;
                            }

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertVat1":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject   jObject  = new JSONObject(output).getJSONObject("vat");
                            int          buttonId = jsonObject.getInt("buttonId");
                            ButtonLayout b        = dbA.fetchButtonByQuery("select * from button where id=" + buttonId);
                            myVat = Vat.fromJson(jObject);
                            rv_adapter.addVatFromServer(myVat, b);


                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "updateVat":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("vat");
                            myVat = Vat.fromJson(jObject);
                            dbA.updateVatValueFromServer(myVat.getId(), myVat.getValue(), myVat.getPerc());
                            ArrayList<VatModel> newVats1 = dbA.fetchVatArrayByQuery();
                            rv_adapter.setVatFromServer(newVats1, myPopupView);

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "deleteVat":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {

                            int status = jsonObject.getInt("status");
                            switch (status)
                            {
                                case 1:
                                    int vatId = jsonObject.getInt("id");
                                    rv_adapter.deleteVat(vatId);
                                    break;
                                case 2:
                                    Toast.makeText(me, "Vat is used", Toast.LENGTH_SHORT).show();
                                    break;

                                default:
                                    break;

                            }

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertFiscalPrinter":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int           printerId       = jsonObject.getInt("printerId");
                            JSONObject    jObject         = new JSONObject(output).getJSONObject("fiscalPrinter");
                            FiscalPrinter myFiscalPrinter = FiscalPrinter.fromJson(jObject);
                            int           api             = 0;
                            if (myFiscalPrinter.isUseApi())
                            {
                                api = 1;
                            }
                            if (printerId == -1)
                            {
                                dbA.insertFiscalPrinter(myFiscalPrinter.getModel(), myFiscalPrinter.getAddress(), myFiscalPrinter
                                        .getModel(), myFiscalPrinter.getPort(), api);
                            }
                            else
                            {
                                dbA.updateFiscalPrinter(myFiscalPrinter.getModel(), myFiscalPrinter.getAddress(), myFiscalPrinter
                                        .getModel(), myFiscalPrinter.getPort(), api);
                            }
                            printerSettingAdapter.setPrinters();
                            new StaticValue(getApplicationContext(), StaticValue.blackboxInfo);

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertKitchenPrinter":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int            printerId   = jsonObject.getInt("printerId");
                            JSONObject     jObject     = new JSONObject(output).getJSONObject("kitchenPrinter");
                            KitchenPrinter myPrinter   = KitchenPrinter.fromJson(jObject);
                            int            singleOrder = 0;
                            if (myPrinter.isSingleOrder() == 1)
                            {
                                singleOrder = 1;
                            }
                            if (printerId == -1)
                            {
                                dbA.insertKitchenPrinter(myPrinter.getName(), myPrinter.getAddress(), myPrinter
                                        .getPort(), singleOrder);
                            }
                            else
                            {
                                dbA.updateKitchenPrinter(myPrinter.getName(), myPrinter.getAddress(), myPrinter
                                        .getPort(), singleOrder, myPrinter.getId());
                            }
                            printerSettingAdapter.setPrinters();

                            printerSettingAdapter.setPrinters();
                            ((CustomEditText) myPopupView.findViewById(R.id.kitchen_printer_name)).setText("");
                            ((CustomEditText) myPopupView.findViewById(R.id.kitchen_printer_IP)).setText("");
                            ((CustomEditText) myPopupView.findViewById(R.id.kitchen_printer_port)).setText("");
                            ImageButton checkboxK = myPopupView.findViewById(R.id.apiInputCheckBox);
                            checkboxK.setActivated(false);
                            selectedKitchenPrinter = new KitchenPrinter();
                            Toast.makeText(MainActivity.this, "Update", Toast.LENGTH_SHORT).show();

                            new StaticValue(getApplicationContext(), StaticValue.blackboxInfo);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "updateUser":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            myUser = User.fromJson(jObject);
                            String oldPassword = jsonObject.getString("oldPassword");
                            dbA.updateUserByPasscode(myUser.getName(), myUser.getSurname(), myUser.getPasscode(), myUser
                                    .getEmail(), myUser.getUserRole(), myUser.getId(), oldPassword);
                            dbA.showData("user");
                            setupNewUserWindow(myPopupView, myPopupWindow);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "passcode already in use", Toast.LENGTH_SHORT)
                                 .show();
                        }

                        break;

                    case "insertModifierGroup":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output)
                                    .getJSONObject("modifierGroup");
                            ModifiersGroupAdapter.ModifiersGroup myModifierGroup = ModifiersGroupAdapter.ModifiersGroup
                                    .fromJson(jObject);
                            dbA.insertModifierGroupsFromServer(myModifierGroup);
                            group_rv_adapter.getCurrentModifiersGroupSet();
                            int open = jsonObject.getInt("open");
                            if (open == 0)
                            {
                                if (group_rv_adapter.selectedGroup != null)
                                {
                                    if (group_rv_adapter.myGroupId != group_rv_adapter.selectedGroup
                                            .getID())
                                    {
                                        if (group_rv_adapter.childAdapter != null)
                                        {
                                            closeModifiersView();
                                            group_rv_adapter.turnGroupModifiersOFF();
                                        }
                                    }
                                    else
                                    {
                                        if (group_rv_adapter.childAdapter != null)
                                        {
                                            boolean check1 = myModifierGroup.getNotes() > 0;
                                            group_rv_adapter.childAdapter.setNotesContainer(check1);
                                        }
                                        group_rv_adapter.turnGroupModifiersOFF();
                                        openModifiersView(group_rv_adapter.myGroupId);
                                    }
                                }
                            }
                            else
                            {
                                if (group_rv_adapter.childAdapter != null)
                                {
                                    group_rv_adapter.childAdapter.notifyDataSetChanged();
                                    boolean check1 = myModifierGroup.getNotes() > 0;
                                    if (wereModifiersOpen)
                                    {
                                        group_rv_adapter.childAdapter.setNotesContainer(check1);
                                    }
                                }
                                group_rv_adapter.selectedGroup = myModifierGroup;
                                group_rv_adapter.turnGroupModifiersOFF();
                                openModifiersView(myModifierGroup.getID());
                            }
                            group_rv_adapter.closePopupWindow();

                        }
                        else
                        {
                            //findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(getApplicationContext(), "error inserting modifier group", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "updateModifierGroup":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output)
                                    .getJSONObject("modifierGroup");
                            ModifiersGroupAdapter.ModifiersGroup myModifierGroup = ModifiersGroupAdapter.ModifiersGroup
                                    .fromJson(jObject);
                            dbA.updateModifierGroupsFromServer(myModifierGroup);
                            group_rv_adapter.getCurrentModifiersGroupSet();
                            int open = jsonObject.getInt("open");
                            if (open == 0)
                            {
                                if (group_rv_adapter.selectedGroup != null)
                                {
                                    if (group_rv_adapter.myGroupId != group_rv_adapter.selectedGroup
                                            .getID())
                                    {
                                        if (group_rv_adapter.childAdapter != null)
                                        {
                                            closeModifiersView();
                                            group_rv_adapter.turnGroupModifiersOFF();
                                        }
                                    }
                                    else
                                    {
                                        boolean check1 = myModifierGroup.getNotes() > 0;
                                        if (group_rv_adapter.childAdapter != null)
                                        {
                                            group_rv_adapter.childAdapter.setNotesContainer(check1);
                                        }
                                        group_rv_adapter.turnGroupModifiersOFF();
                                        openModifiersView(group_rv_adapter.myGroupId);
                                    }
                                }
                            }
                            else
                            {
                                if (group_rv_adapter.childAdapter != null)
                                {
                                    group_rv_adapter.childAdapter.notifyDataSetChanged();
                                    boolean check1 = myModifierGroup.getNotes() > 0;
                                    if (wereModifiersOpen)
                                    {
                                        group_rv_adapter.childAdapter.setNotesContainer(check1);
                                    }
                                }
                                group_rv_adapter.selectedGroup = myModifierGroup;
                                //open modifiers
                                group_rv_adapter.turnGroupModifiersOFF();
                                openModifiersView(myModifierGroup.getID());
                            }
                            group_rv_adapter.closePopupWindow();

                        }
                        else
                        {
                            //findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(getApplicationContext(), "error inserting modifier group", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "deleteModifierGroup":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int id = jsonObject.getInt("id");
                            dbA.deleteModifierFromTableByID("modifiers_group", id);
                            group_rv_adapter.getCurrentModifiersGroupSet();
                            group_rv_adapter.turnGroupModifiersOFF();
                            closeModifiersView();
                            findViewById(R.id.modifier_notes_container).setVisibility(GONE);
                            group_rv_adapter.closeTwoPopupWindow();
                            rv_adapter.closeTwoPopupWindow();
                        }
                        else
                        {
                            //findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(getApplicationContext(), "error deleting modifier group", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "insertInModifierGroupAssigned":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject            jObject = new JSONObject(output).getJSONObject("mga");
                            ModifierGroupAssigned mga     = ModifierGroupAssigned.fromJson(jObject);
                            group_rv_adapter.myPopupView.setActivated(true);
                            dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + mga.getGroupId() + " AND prod_id=" + mga.getProdId());
                            dbA.insertModifiersGroupGroupsFromServer(mga);
                        }
                        else
                        {
                            //findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(getApplicationContext(), "error deleting modifier group", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "moveModifierGroup":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int fromPosition = jsonObject.getInt("fromPosition");
                            int toPosition   = jsonObject.getInt("toPosition");

                            group_rv_adapter.swapModifierGroupFunction(fromPosition, toPosition);
                            group_rv_adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "updateModifierVat":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject               jObject  = new JSONObject(output).getJSONObject("modifier");
                            ModifierAdapter.Modifier modifier = ModifierAdapter.Modifier.fromJson(jObject);

                            modifiers_rv_adapter.myModifier.setVat(0);
                            dbA.execOnDb("UPDATE modifier SET vat=" + modifier.getVat() + " WHERE id=" + modifier
                                    .getID() + ";");
                            CustomButton vatContainer = modifiers_rv_adapter.myPopupView.findViewById(R.id.modVatInsert);
                            vatContainer.setText("");
                            modifiers_rv_adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertModifier":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject               jObject  = new JSONObject(output).getJSONObject("modifier");
                            ModifierAdapter.Modifier modifier = ModifierAdapter.Modifier.fromJson(jObject);
                            dbA.insertModifierFromServer(modifier);
                            modifiers_rv_adapter.getCurrentModifiersSet();
                            if (modifiers_rv_adapter.number_of_modifiers == modifiers_rv_adapter.MAX_NUMBER_OF_MODIFIERS)
                            {
                                Toast.makeText(getApplicationContext(), R.string.max_number_of_modifiers_reached, Toast.LENGTH_SHORT)
                                     .show();
                            }
                            modifiers_rv_adapter.closePopupWindow();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "updateModifier":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject               jObject  = new JSONObject(output).getJSONObject("modifier");
                            ModifierAdapter.Modifier modifier = ModifierAdapter.Modifier.fromJson(jObject);
                            dbA.updateModifierFromServer(modifier);
                            modifiers_rv_adapter.getCurrentModifiersSet();
                            modifiers_rv_adapter.closePopupWindow();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "deleteModifier":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int id = jsonObject.getInt("id");
                            dbA.deleteModifierFromTableByID("modifier", id);
                            modifiers_rv_adapter.getCurrentModifiersSet();
                            modifiers_rv_adapter.closeTwoPopupWindow();
                        }
                        else
                        {
                            //findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(getApplicationContext(), "error deleting modifier group", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "moveModifier":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int fromPosition = jsonObject.getInt("fromPosition");
                            int toPosition   = jsonObject.getInt("toPosition");

                            modifiers_rv_adapter.swapModifierFunction(fromPosition, toPosition);
                            modifiers_rv_adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "deleteFromModifierAssigned":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int assignmentId = jsonObject.getInt("assignmentId");
                            int modId        = jsonObject.getInt("modifierId");

                            dbA.execOnDb(String.format("DELETE FROM modifiers_assigned WHERE assignment_id = %s AND modifier_id = %s;", assignmentId, modId));
                            dbA.execOnDb(String.format("DELETE FROM modifiers_group_assigned WHERE id = %s;", assignmentId));

                            modifiers_rv_adapter.myPopupView.setActivated(false);

                            modifiers_rv_adapter.nModifiersAttached--;
                        }

                        else
                        {
                            //findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(getApplicationContext(), "error deleting modifier group", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertInModifierAssigned":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject            jObject  = new JSONObject(output).getJSONObject("ma");
                            JSONObject            jObject2 = new JSONObject(output).getJSONObject("mga");
                            ModifierGroupAssigned mga      = ModifierGroupAssigned.fromJson(jObject2);

                            dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + mga
                                    .getGroupId() + " AND prod_id=" + mga.getProdId());
                            dbA.insertModifiersGroupGroupsFromServer(mga);

                            ModifierAssigned ma      = ModifierAssigned.fromJson(jObject);
                            int              groupId = jsonObject.getInt("groupId");

                            modifiers_rv_adapter.myPopupView.setActivated(true);

                            int i = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupId)
                                       .get(0).getPosition();
                            group_rv_adapter.getChildAt(i - 1).setActivated(true);

                            dbA.execOnDb("DELETE FROM modifiers_assigned WHERE assignment_id=" + ma.getAssignementId() + " AND modifier_id = " + ma
                                    .getModifierId());

                            dbA.execOnDb("INSERT INTO modifiers_assigned(assignment_id, modifier_id, fixed) " +
                                                 "VALUES(" + ma.getAssignementId() + "," + ma.getModifierId() + ", " + ma
                                    .getFixed() + ");");

                            modifiers_rv_adapter.nModifiersAttached++;

                            dbA.showData("modifiers_group_assigned");
                            dbA.showData("modifiers_assigned");
                        }
                        else
                        {
                            //findViewById(R.id.AddUserWindow).startAnimation(shake);
                            Toast.makeText(getApplicationContext(), "error inserting modifier group", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "insertVatFromModifier":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("vat");
                            int        from    = jsonObject.getInt("from");
                            myVat = Vat.fromJson(jObject);
                            if (from == 0)
                            {
                                int status = jsonObject.getInt("status");
                                switch (status)
                                {
                                    case 1:
                                        //dbA.addVatValue(myVat.getValue());
                                        dbA.addVatValueFromServer(myVat.getId(), myVat.getValue(), myVat
                                                .getPerc());
                                        break;
                                    case 2:
                                        dbA.updateVatValueFromServer(myVat.getId(), myVat.getValue(), myVat
                                                .getPerc());
                                        break;
                                    default:
                                        break;

                                }
                                ArrayList<VatModel> newVats = dbA.fetchVatArrayByQuery();
                                modifiers_rv_adapter.vatAdapter.setVats(newVats);
                                modifiers_rv_adapter.vatAdapter.notifyDataSetChanged();
                                CustomButton vatContainer = modifiers_rv_adapter.myPopupView.findViewById(R.id.modVatInsert);
                                vatContainer.setText("");
                            }
                            else
                            {
                                modifiers_rv_adapter.setVatAdapterFromServer(myVat.getValue(), modifiers_rv_adapter.myPopupView, modifiers_rv_adapter.myPopupWindow);
                            }

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "No check passed for route " + route, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;

                    case "azzeramentoScontrini":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            Intent intent = new Intent(getApplicationContext(), SplashScreen1.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "error in azzeramentoOrdini ", Toast.LENGTH_SHORT)
                                 .show();

                        }
                        break;

                    case "updateInvoiceNumber":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int numeroFattura = jsonObject.getInt("numeroFattura");
                            dbA.updateNumeroFatture(Integer.valueOf(numeroFattura));
                            openSettingPopup();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "error in updateInvoiceNumber ", Toast.LENGTH_SHORT)
                                 .show();

                        }
                        break;

                    case "getInvoiceNumber":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int numeroFattura = jsonObject.getInt("numeroFattura");
                            dbA.updateNumeroFatture(Integer.valueOf(numeroFattura));
                            openUpdateInvoiceNumber();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "error in updateInvoiceNumber ", Toast.LENGTH_SHORT)
                                 .show();

                        }
                        break;

                    case "updateAdminDeviceInfo":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("deviceInfo");
                            DeviceInfo device  = DeviceInfo.fromJson(jObject);
                            dbA.execOnDb("delete from device_info");
                            dbA.insertDeviceInfoWithId(device);

                            new StaticValue(getApplicationContext(), StaticValue.blackboxInfo);
                            myPopupWindow.dismiss();
                        }
                        else
                        {

                            Toast.makeText(getApplicationContext(), "error in updateInvoiceNumber ", Toast.LENGTH_SHORT)
                                 .show();

                        }
                        break;

                    case "exportProductDatabase":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            Toast.makeText(getApplicationContext(), "Exporting successful", Toast.LENGTH_SHORT)
                                 .show();
                            Log.i(TAG, "Exporting to blackbox successful");
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Exporting error. Check blackbox log", Toast.LENGTH_LONG)
                                 .show();
                            Log.i(TAG, "Exporting to blackbox error");
                        }
                        break;

                    default:
                        Log.e(TAG, "Got unknown route: " + route);
                        Toast.makeText(getApplicationContext(), "got unknown route: " + route, Toast.LENGTH_LONG)
                             .show();
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
        }
    }

    public boolean getIsAdded()
    {
        return isAdded;
    }

    public void setBarcodeShow(boolean b)
    {
        isBarcodeShow = b;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e)
    {

        if (e.getAction() == KeyEvent.ACTION_DOWN)
        {
            char pressedKey = (char) e.getUnicodeChar();
            barcode += pressedKey;
        }
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER)
        {
            Toast.makeText(getApplicationContext(),
                           "barcode--->>>" + barcode, Toast.LENGTH_LONG
            )
                 .show();
            if (isBarcodeShow)
            {
                ((CustomEditText) findViewById(R.id.single_input)).setText(barcode);
            }
            barcode = "";
        }

        return super.dispatchKeyEvent(e);
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

    public void callHttpHandler(String route, List<NameValuePair> params)
    {
        httpHandler          = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }


    public void callHttpHandler(String route, RequestParam params)
    {
        httpHandler          = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }


    public void callHttpHandlerTMP(String route, List<NameValuePair> params, String ip)
    {
        httpHandler          = new HttpHandler();
        httpHandler.testIp   = ip;
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
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

    /**
     * END OF onCreate
     **/


    @Override
    protected void onResume()
    {
        super.onResume();
        switchView(3, 0);
    }

    // Updates adapter and sets it in gridview
    public void switchView(int view_id, int id)
    {
        switch (view_id)
        {
            case DEFAULT_VIEW:
                setContentView(R.layout.activity_main);
                setDefaultSettings(true);
                break;
            case MODIFIERS_VIEW:
                setContentView(R.layout.activity_main_modifiers);
                setModifiersSettings();
                break;
            default:
                setContentView(R.layout.activity_main);
                setDefaultSettings(false);
        }
    }

    private void fireUserWindow()
    {

        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_user_interface, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        setupAdminWindow(popupView, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    public void setupAdminWindow(final View popupView, final PopupWindow popupWindow)
    {
        popupView.findViewById(R.id.operation_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                long rowCount = dbA.getTablesRowCount("button");

                //at least one is present is fake button for article
                if (rowCount > 1)
                {
                    Intent intent = new Intent(MainActivity.this, Operative.class);
                    intent.putExtra("isAdmin", 0);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Please, configure at least one button before use operative part", Toast.LENGTH_SHORT)
                         .show();
                }

            }
        });
        popupView.findViewById(R.id.addUser_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupView.findViewById(R.id.AdminWindow).setVisibility(View.GONE);
                popupView.findViewById(R.id.AddUserWindow).setVisibility(View.VISIBLE);
                setupNewUserWindow(popupView, popupWindow);
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
        popupView.findViewById(R.id.configure_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.printerOption_button)
                 .setOnClickListener(new View.OnClickListener()
                 {

                     @Override
                     public void onClick(View v)
                     {
                         Toast.makeText(me, R.string.please_perform_this_operation_from_operative, Toast.LENGTH_SHORT)
                              .show();
               /* popupView.findViewById(R.id.AdminWindow).setVisibility(View.GONE);
                popupView.findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                popupView.findViewById(R.id.SessionWindow_ma).setVisibility(View.VISIBLE);

                @SuppressLint("WrongViewCast")
                RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.SessionWindow_ma).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.SessionWindow_ma).setLayoutParams(rlp1);

                setupNewSessionWindow(popupView, popupWindow);*/

                     }
                 });
        popupView.findViewById(R.id.switchUser_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent(getApplicationContext(), PinpadActivity.class);
                startActivity(intent);
                finish();
            }
        });

        popupView.findViewById(R.id.cashstatus_button).setOnClickListener(new View.OnClickListener()
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
                    myThread.delegate = myself;
                    myThread.setPrintType(14);
                    myThread.setIP(IP);

                    myThread.setClientThread();
                    myThread.setRunBaby(true);
                }
                else
                {
                    Toast.makeText(me, R.string.please_fill_cash_management_first, Toast.LENGTH_SHORT)
                         .show();
                }

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

        setupDismissKeyboard(popupView);

        //dbA.showData("cash_management_set");

        setupCashStatus(popupView, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    public void setupCashStatus(final View popupView, final PopupWindow popupWindow)
    {
        CustomEditText total_deposit    = popupView.findViewById(R.id.total_deposit);
        CustomEditText five_cents       = popupView.findViewById(R.id.amount_005);
        CustomEditText ten_cents        = popupView.findViewById(R.id.amount_010);
        CustomEditText twenty_cents     = popupView.findViewById(R.id.amount_020);
        CustomEditText fifty_cents      = popupView.findViewById(R.id.amount_050);
        CustomEditText one_euro         = popupView.findViewById(R.id.amount_100);
        CustomEditText two_euros        = popupView.findViewById(R.id.amount_200);
        CustomEditText five_euros       = popupView.findViewById(R.id.amount_500);
        CustomEditText ten_euros        = popupView.findViewById(R.id.amount_1000);
        CustomEditText twenty_euros     = popupView.findViewById(R.id.amount_2000);
        CustomEditText fifty_euros      = popupView.findViewById(R.id.amount_5000);
        CustomEditText hundred_euros    = popupView.findViewById(R.id.amount_10000);
        CustomEditText twohundred_euros = popupView.findViewById(R.id.amount_20000);

        RelativeLayout deposit_window         = popupView.findViewById(R.id.deposit_window);
        RelativeLayout withdraw_amount_window = popupView.findViewById(R.id.withdrawamount_window);

        withdraw_amount_window.setVisibility(View.GONE);
        deposit_window.setVisibility(View.VISIBLE);

        DecimalFormat twoD = new DecimalFormat("#.00");

        ImageButton okButton = popupView.findViewById(R.id.ok);
        ImageButton cancel   = popupView.findViewById(R.id.kill);

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
                        if (five_cents.getText().toString().equals("") && ten_cents.getText()
                                                                                   .toString()
                                                                                   .equals("") && twenty_cents
                                .getText()
                                .toString()
                                .equals("")
                                && fifty_cents.getText().toString().equals("") && one_euro.getText()
                                                                                          .toString()
                                                                                          .equals("") && two_euros
                                .getText()
                                .toString()
                                .equals("")
                                && five_euros.getText().toString().equals("") && ten_euros.getText()
                                                                                          .toString()
                                                                                          .equals("") && twenty_euros
                                .getText()
                                .toString()
                                .equals("")
                                && fifty_euros.getText()
                                              .toString()
                                              .equals("") && hundred_euros.getText()
                                                                          .toString()
                                                                          .equals("")
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
                                                 fiveCents.equals("") ? 0 : Integer.parseInt(fiveCents), tenCents
                                                         .equals("") ? 0 : Integer.parseInt(tenCents),
                                                 twentyCents.equals("") ? 0 : Integer.parseInt(twentyCents), fiftyCents
                                                         .equals("") ? 0 : Integer.parseInt(fiftyCents),
                                                 oneE.equals("") ? 0 : Integer.parseInt(oneE), twoE.equals("") ? 0 : Integer
                                            .parseInt(twoE),
                                                 fiveE.equals("") ? 0 : Integer.parseInt(fiveE), tenE
                                                         .equals("") ? 0 : Integer.parseInt(tenE),
                                                 twentyE.equals("") ? 0 : Integer.parseInt(twentyE), fiftyE
                                                         .equals("") ? 0 : Integer.parseInt(fiftyE),
                                                 hundred.equals("") ? 0 : Integer.parseInt(hundred), twoHundred
                                                         .equals("") ? 0 : Integer.parseInt(twoHundred)
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
                            CustomEditText five_cents_a       = popupView.findViewById(R.id.amount_005a);
                            CustomEditText ten_cents_a        = popupView.findViewById(R.id.amount_010a);
                            CustomEditText twenty_cents_a     = popupView.findViewById(R.id.amount_020a);
                            CustomEditText fifty_cents_a      = popupView.findViewById(R.id.amount_050a);
                            CustomEditText one_euro_a         = popupView.findViewById(R.id.amount_100a);
                            CustomEditText two_euros_a        = popupView.findViewById(R.id.amount_200a);
                            CustomEditText five_euros_a       = popupView.findViewById(R.id.amount_500a);
                            CustomEditText ten_euros_a        = popupView.findViewById(R.id.amount_1000a);
                            CustomEditText twenty_euros_a     = popupView.findViewById(R.id.amount_2000a);
                            CustomEditText fifty_euros_a      = popupView.findViewById(R.id.amount_5000a);
                            CustomEditText hundred_euros_a    = popupView.findViewById(R.id.amount_10000a);
                            CustomEditText twohundred_euros_a = popupView.findViewById(R.id.amount_20000a);

                            double amount = dbA.checkIfCashTotalIsDifferent();

                            int[] counter = {cash.getTwoHundredEuros() - cash_static.getTwoHundredEuros(), cash
                                    .getHundredEuros() - cash_static.getHundredEuros(),
                                    cash.getFiftyEuros() - cash_static.getFiftyEuros(), cash.getTwentyEuros() - cash_static
                                    .getTwentyEuros(),
                                    cash.getTenEuros() - cash_static.getTenEuros(), cash.getFiveEuros() - cash_static
                                    .getFiveEuros(),
                                    cash.getTwoEuros() - cash_static.getTwoEuros(), cash.getOneEuros() - cash_static
                                    .getOneEuros(), cash.getFiftyCents() - cash_static.getFiftyCents(),
                                    cash.getTwentyCents() - cash_static.getTwentyCents(), cash.getTenCents() - cash_static
                                    .getTenCents(),
                                    cash.getFiveCents() - cash_static.getFiveCents()};

                            withdraw_amount_window.setVisibility(View.VISIBLE);
                            deposit_window.setVisibility(View.GONE);

                            DecimalFormat  twoD            = new DecimalFormat("#.00");
                            CustomEditText withdraw_amount = popupView.findViewById(R.id.withdraw_amount);
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
                        Toast.makeText(me, R.string.please_fill_withdraw_value, Toast.LENGTH_SHORT)
                             .show();
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
    public void setupNewSessionWindow(final View popupView, final PopupWindow popupWindow)
    {

        RecyclerView session_recycler = popupView.findViewById(R.id.session_time_recycler);
        session_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        session_recycler.setHasFixedSize(true);
        sessionAdapter = new SessionAdapter(this, dbA, popupView, popupWindow);
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
        final CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
        newSessionNameContainer.setText("");
        CustomButton startTimeContainer = popupView.findViewById(R.id.start_session_button_et);
        startTimeContainer.setText(R.string.startTime);
        CustomButton endTimeContainer = popupView.findViewById(R.id.end_session_button_et);
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
                mTimePicker = new TimePickerDialog(MainActivity.this, 4, new TimePickerDialog.OnTimeSetListener()
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
                mTimePicker = new TimePickerDialog(MainActivity.this, 4, new TimePickerDialog.OnTimeSetListener()
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


        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
                CustomButton         startTimeContainer      = popupView.findViewById(R.id.start_session_button_et);
                CustomButton         endTimeContainer        = popupView.findViewById(R.id.end_session_button_et);
                String sessionName = newSessionNameContainer.getText()
                                                            .toString();
                String startTime = startTimeContainer.getText()
                                                     .toString();
                String endTime = endTimeContainer.getText()
                                                 .toString();
                if (!sessionName.equals("") && !startTime.equals("Start Time") && !endTime.equals("End Time"))
                {
                    dbA.saveNewSessionTime(startTime + ":00", endTime + ":00", sessionName);
//                    sessionAdapter.notifyDataSetChanged();
                    RecyclerView session_recycler = popupView.findViewById(R.id.session_time_recycler);
                    session_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    session_recycler.setHasFixedSize(true);
                    sessionAdapter = new SessionAdapter(me, dbA, popupView, popupWindow);
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
                    Toast.makeText(MainActivity.this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT)
                         .show();

                }
            }
        });
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams)popupView.findViewById(R.id.SessionWindow_ma).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.SessionWindow_ma).setLayoutParams(rlp1);*/
                popupView.findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                popupView.findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                popupView.findViewById(R.id.SessionWindow_ma).setVisibility(View.GONE);
                setupAdminWindow(popupView, popupWindow);

            }
        });

    }

    @Override
    public void setButtonSet(int sessionTimeId, String sessionName, String start, String end)
    {

    }

    @Override
    public void deleteSession(int sessionTimeId)
    {

    }

    public void setButtonSetPopup(int sessionTimeId, String sessionName, String start, String end, View popupView, PopupWindow popupWindow)
    {

        CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
        newSessionNameContainer.setText(sessionName);
        CustomButton startTimeContainer = popupView.findViewById(R.id.start_session_button_et);
        startTimeContainer.setText(start);
        CustomButton endTimeContainer = popupView.findViewById(R.id.end_session_button_et);
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
                mTimePicker = new TimePickerDialog(MainActivity.this, 4, new TimePickerDialog.OnTimeSetListener()
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
                mTimePicker = new TimePickerDialog(MainActivity.this, 4, new TimePickerDialog.OnTimeSetListener()
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
        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final CustomEditText newSessionNameContainer = popupView.findViewById(R.id.new_session_name_et);
                CustomButton         startTimeContainer      = popupView.findViewById(R.id.start_session_button_et);
                CustomButton         endTimeContainer        = popupView.findViewById(R.id.end_session_button_et);
                String sessionName = newSessionNameContainer.getText()
                                                            .toString();
                String startTime = startTimeContainer.getText()
                                                     .toString();
                String endTime = endTimeContainer.getText()
                                                 .toString();
                if (!sessionName.equals("") && !startTime.equals("Start Time") && !endTime.equals("End Time"))
                {
                    dbA.updateNewSessionTime(sessionTimeId, startTime + ":00", endTime + ":00", sessionName);

                    setupNewSessionWindow(popupView, popupWindow);
                }
                else
                {
                    Toast.makeText(MainActivity.this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT)
                         .show();

                }
            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupNewSessionWindow(popupView, popupWindow);

            }
        });
    }

    public void deleteSessionPopup(int sessionId, View popupView, PopupWindow popupWindow)
    {
        dbA.deleteSessionTime(sessionId);
        setupNewSessionWindow(popupView, popupWindow);
    }

    public void setupNewUserWindow(final View popupView, final PopupWindow popupWindow)
    {
        RecyclerView user_recycler = popupView.findViewById(R.id.users_recycler);
        user_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        user_recycler.setHasFixedSize(true);
        userAdapter = new UserAdapter(this, dbA, userType, userId, popupView, popupWindow);
        user_recycler.setAdapter(userAdapter);


        final CustomEditText Name     = popupView.findViewById(R.id.name_et);
        final CustomEditText Surname  = popupView.findViewById(R.id.surname_et);
        final CustomEditText Email    = popupView.findViewById(R.id.email_et);
        final ImageButton    manager  = popupView.findViewById(R.id.manager_checkbox);
        final CustomEditText Passcode = popupView.findViewById(R.id.passcode_et);
        //set max length to 4 for passcode and 6 for password
        //  Password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        Passcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
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
        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name    = Name.getText().toString();
                String surname = Surname.getText().toString();
                String email   = Email.getText().toString();
                // String passwordField = Password.getText().toString();
                String passcode = Passcode.getText().toString();
                if (name.equals("") || surname.equals("") || email.equals("") || passcode.equals(""))
                {
                    Toast.makeText(getBaseContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT)
                         .show();
                }
                else if (dbA.checkIfPasscodeExists(passcode))
                {
                    Toast.makeText(getBaseContext(), R.string.passcode_is_already_used, Toast.LENGTH_SHORT)
                         .show();
                }
                else
                {
                    if (!Pattern.matches("^[-a-zA-Z0-9_]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                    {
                        Toast.makeText(getBaseContext(), R.string.not_a_valid_email, Toast.LENGTH_SHORT)
                             .show();
                    }
                    else
                    {
                        //String password = Login.randomAlphaNumericWord(6);
                        String password = passcode;

                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("name", name));
                            params.add(new BasicNameValuePair("surname", surname));
                            params.add(new BasicNameValuePair("email", email));
                            params.add(new BasicNameValuePair("password", passcode));
                            myPopupView   = popupView;
                            myPopupWindow = popupWindow;
                            if (manager.isActivated())
                            {
                                params.add(new BasicNameValuePair("userType", String.valueOf(1)));
                            }
                            else
                            {
                                params.add(new BasicNameValuePair("userType", String.valueOf(2)));
                            }
                            callHttpHandler("/insertUser", params);
                        }
                        else
                        {
                            if (manager.isActivated())
                            {
                                dbA.insertUser(name, surname, email, passcode, 1, passcode);
                            }
                            else /*if(cashier.isActivated())*/
                            {
                                dbA.insertUser(name, surname, email, passcode, 2, passcode);
                            }
                            Toast.makeText(MainActivity.this, resources.getString(R.string.new_user_created_password_, password), Toast.LENGTH_LONG)
                                 .show();
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
        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupView.findViewById(R.id.AdminWindow).setVisibility(View.VISIBLE);
                popupView.findViewById(R.id.AddUserWindow).setVisibility(View.GONE);
                /*@SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams)popupView.findViewById(R.id.AdminWindow).getLayoutParams();
                int top1 = (int)(dpHeight-52)/2 - rlp1.height/2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.AdminWindow).setLayoutParams(rlp1);*/
                setupAdminWindow(popupView, popupWindow);
            }
        });
    }

    /**
     * Default settings setup: grid manager, recycler view, rv adapter etc..
     */
    private void setDefaultSettings(boolean restorePreviousView)
    {
        this.findViewById(R.id.cashier).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openSettingPopup();
            }
        });

        this.findViewById(R.id.orders).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // dbA.restartDb();
                /*Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                startActivity(intent);*/
            }
        });
        findViewById(R.id.clients).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /*Intent intent = new Intent(MainActivity.this, ClientsActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.setAction("clientsFromConfigurator");
                startActivity(intent);*/
            }
        });

        findViewById(R.id.reservations).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fireSingleInputDialogForTimerPopup();
            }
        });
        //if(isAdmin)
        ((CustomTextView) findViewById(R.id.admin_username_button_tv)).setText(username);
        findViewById(R.id.admin).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (isAdmin)
                {
                    case 0:
                        fireUserWindow();
                        break;
                    case 1:
                        fireUserWindow();
                        break;
                    case 2:

                        break;
                    default:
                        break;
                }

                /*Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);*/
            }
        });

        findViewById(R.id.table_set).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, TableActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.setAction("configuration");
                startActivity(intent);
                finish();
            }
        });

        wereModifiersOpen = false;
        recyclerview      = findViewById(R.id.recyclerView);
        recyclerview.setHasFixedSize(true);
        grid_manager = new GridLayoutManager(this, 4);
        grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {

            @Override
            public int getSpanSize(int position)
            {
                return 1;
            }
        });
        recyclerview.setLayoutManager(grid_manager);
        if (restorePreviousView)
        {
            rv_adapter.goToCategory(0, grid_status.getCurrent_category_id(), grid_status.getPrevious_category_title());
        }
        else
        {
            getHomeButtonSet();
        }
        recyclerview.addItemDecoration(new LineSeparator(this, 14));
        if (!restorePreviousView)
        {
            rv_adapter = new GridAdapter(this, buttons, dbA);
        }
        recyclerview.setAdapter(rv_adapter);

        ItemTouchHelper.Callback callback =
                new ItemTouchHelperCallback(rv_adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerview);
    }


    private void openSettingPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View     popupView      = layoutInflater.inflate(R.layout.popup_setting, null);

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
                setupSettingWindows(popupView, popupWindow);
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }


    private void setupSettingWindows(final View popupview, final PopupWindow popupWindow)
    {
        popupview.findViewById(R.id.registration_button)
                 .setOnClickListener(new View.OnClickListener()
                 {
                     @Override
                     public void onClick(View v)
                     {
                         popupWindow.dismiss();
                         setupRegistrationLayout();
                     }
                 });

        popupview.findViewById(R.id.ipsetting_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
                setupIpSettingsPopup();
            }
        });

        popupview.findViewById(R.id.cash_management_button)
                 .setOnClickListener(new View.OnClickListener()
                 {
                     @Override
                     public void onClick(View view)
                     {
                         openCashManagementPopup();
                     }
                 });

        popupview.findViewById(R.id.rfid_setting_button)
                 .setOnClickListener(new View.OnClickListener()
                 {
                     @Override
                     public void onClick(View v)
                     {
                         popupWindow.dismiss();
                         openRFIDPopup();

                     }
                 });

        popupview.findViewById(R.id.database_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
                openDatabasePopup();
            }
        });

        popupview.findViewById(R.id.update_invoice_number_button)
                 .setOnClickListener(new View.OnClickListener()
                 {
                     @Override
                     public void onClick(View v)
                     {
                         Toast.makeText(me, "Update Invoice Number", Toast.LENGTH_SHORT).show();
                         popupWindow.dismiss();
                         if (!StaticValue.blackbox)
                         {
                             openUpdateInvoiceNumber();
                         }
                         else
                         {
                             openUpdateInvoiceForBlackbox();
                         }


                     }
                 });

        popupview.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupview.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
    }

    public void openUpdateInvoiceForBlackbox()
    {
        List<NameValuePair> params     = new ArrayList<NameValuePair>(2);
        String              android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        params.add(new BasicNameValuePair("androidId", android_id));

        callHttpHandler("/getInvoiceNumber", params);

    }

    public void openUpdateInvoiceNumber()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_single_input, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        setupUpdateInvoicePopup(popupView, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

        setupDismissKeyboard(popupView);

    }

    public void setupUpdateInvoicePopup(View popupview, final PopupWindow popupWindow)
    {
        CustomEditText text = popupview.findViewById(R.id.single_input);
        text.setInputType(InputType.TYPE_CLASS_NUMBER);
        text.setHint(R.string.update_invoice_number);
        int numero = dbA.selectNumeroFattura();
        text.setText(String.valueOf(numero));
        popupview.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String newNumber = ((CustomEditText) popupview.findViewById(R.id.single_input)).getText()
                                                                                               .toString();
                if (!newNumber.equals(""))
                {
                    if (StaticValue.blackbox)
                    {
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("numeroFattura", newNumber));
                        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));

                        callHttpHandler("/updateInvoiceNumber", params);

                        popupWindow.dismiss();
                    }
                    else
                    {
                        dbA.updateNumeroFatture(Integer.valueOf(newNumber));
                        popupWindow.dismiss();
                        openSettingPopup();
                    }
                }
                else
                {

                }
            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupview.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
                openSettingPopup();
            }
        });


    }

    public void openDatabasePopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_database_options, null);
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

                //setupSettingWindows(popupView, popupWindow);

            }
        });
        setupDatabasePopup(popupView, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

    }

    private void setupDatabasePopup(View popupview, final PopupWindow popupWindow)
    {
        CustomButton export = popupview.findViewById(R.id.export_button);
        export.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String database = ((CustomEditText) popupview.findViewById(R.id.database_name)).getText()
                                                                                               .toString();
                if (!database.equals(""))
                {
                    exportDatabase(database, popupview);
                }
                else
                {
                    Toast.makeText(me, "Fill file name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        CustomButton export_blackbox = popupview.findViewById(R.id.export_button_blackbox);
        export_blackbox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String cursorButtonsString  = DatabaseUtils.dumpCursorToString(dbA.fetchByQuery("SELECT * FROM button"));
                String cursorModifierString = DatabaseUtils.dumpCursorToString(dbA.fetchByQuery("SELECT * FROM modifier"));

                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("deviceId", String.valueOf(deviceInfo.getId())));
                params.add(new BasicNameValuePair("button_table", cursorButtonsString));
                params.add(new BasicNameValuePair("modifier_table", cursorModifierString));

                callHttpHandler("/exportProductDatabase", params);
            }
        });

        CustomButton import_button = popupview.findViewById(R.id.import_button);
        import_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String database = ((CustomEditText) popupview.findViewById(R.id.database_name)).getText()
                                                                                               .toString();
                if (!database.equals(""))
                {
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator
                                                 + "Download" + File.separator + database + ".db");
                    if (file.exists())
                    {
                        //Do something
                        importDatabase(database);
                    }
                    else
                    {
                        Toast.makeText(me, "No file " + database + " found", Toast.LENGTH_SHORT)
                             .show();
                    }


                }
                else
                {
                    Toast.makeText(me, "Fille file name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        popupview.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupview.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();

            }
        });

    }

    public void exportDatabase(String databaseName, View popupview)
    {
        try
        {
            File backupDB = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    databaseName + ".db"
            ); // for example "my_data_backup.db"
            File currentDB = getApplicationContext().getDatabasePath("mydatabase.db");
            if (currentDB.exists())
            {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
            ((CustomEditText) popupview.findViewById(R.id.database_name)).setText("");
            Toast.makeText(me, "Database Esportato", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "BACKUP OK");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(me, "Errore Esporto Database", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "BACKUP FAIL");
        }

    }

    public void importDatabase(String databaseName)
    {
        try
        {
            dbA.getDbHelper()
               .importDatabase(Environment.getExternalStorageDirectory() + File.separator
                                       + "Download" + File.separator + databaseName + ".db", getApplicationContext()
                                       .getDatabasePath("mydatabase.db")
                                       .getPath());

            Intent i = getBaseContext().getPackageManager()
                                       .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            ActivityCompat.finishAfterTransition(MainActivity.this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void openRFIDPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_rfiddevice, null);
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

                //setupSettingWindows(popupView, popupWindow);

            }
        });


        //setupRFIDPopup(popupView, popupWindow);


        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    //class for Printer Model Selection

    private void setupIpSettingsPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View     popupView      = layoutInflater.inflate(R.layout.popup_ip_settings, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        popupView.post(() ->
                       {
                           //setupSettingWindows(popupView, popupWindow);
                       });

        setUpIpSettingsLayout(popupView, popupWindow);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
    {
        //forse va bene anche if(pos != 0)
        if (pos != 0)
        {
            printerModel = adapterView.getItemAtPosition(pos).toString();
        }
        else
        {
            Toast.makeText(me, R.string.select_printer_model, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    public void setSelectedPrinter(View popupview, KitchenPrinter kitchenPrinter)
    {
        popupview.findViewById(R.id.pos_printer)
                 .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
        popupview.findViewById(R.id.kitchen_printer)
                 .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.green_2));
        popupview.findViewById(R.id.black_box)
                 .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
        popupview.findViewById(R.id.kitchen_printer_selection).setVisibility(View.VISIBLE);
        popupview.findViewById(R.id.pos_printer_selection).setVisibility(View.GONE);
        popupview.findViewById(R.id.blackbox_selection).setVisibility(View.GONE);
        ImageButton checkboxK = popupview.findViewById(R.id.apiInputCheckBox);
        selectedKitchenPrinter = kitchenPrinter;
        if (kitchenPrinter.getId() != -1)
        {

            if (!kitchenPrinter.getName().equals(""))
            {
                ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_name)).setText(kitchenPrinter
                                                                                                     .getName());
            }

            if (!kitchenPrinter.getAddress().equals(""))
            {
                ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_IP)).setText(kitchenPrinter
                                                                                                   .getAddress());
            }

            if (kitchenPrinter.getPort() > 0)
            {
                ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_port)).setText(String.valueOf(kitchenPrinter
                                                                                                                    .getPort()));

            }

            checkboxK.setActivated(kitchenPrinter.isSingleOrder() == 1);

        }
    }

    public void setSelectedBlackbox(View popupview, BlackboxInfo blackbox)
    {
        popupview.findViewById(R.id.pos_printer)
                 .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
        popupview.findViewById(R.id.kitchen_printer)
                 .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
        popupview.findViewById(R.id.black_box)
                 .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.green_2));
        popupview.findViewById(R.id.kitchen_printer_selection).setVisibility(View.GONE);
        popupview.findViewById(R.id.pos_printer_selection).setVisibility(View.GONE);
        popupview.findViewById(R.id.blackbox_selection).setVisibility(View.VISIBLE);
        ((CustomEditText) popupview.findViewById(R.id.blackbox_IP)).setText(blackbox.getAddress());
    }

    private void setUpIpSettingsLayout(View popupview, final PopupWindow popupWindow)
    {
        show = 1;

        Spinner                 spinner     = popupview.findViewById(R.id.pos_printer_name);
        ArrayList<PrinterModel> spinnerList = new ArrayList<>();
        spinnerList.add(new PrinterModel("Select Printer Model"));
        spinnerList.add(new PrinterModel("Ditron"));
        spinnerList.add(new PrinterModel("Custom"));
        spinnerList.add(new PrinterModel("Epson"));
        spinnerList.add(new PrinterModel("RCH"));

        ArrayAdapter<PrinterModel> adapter = new ArrayAdapter<PrinterModel>(getApplicationContext(), R.layout.spinner_dropdown, spinnerList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        ImageButton checkbox = popupview.findViewById(R.id.api_checkBox);
        checkbox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkbox.setActivated(!checkbox.isActivated());

            }
        });

        ImageButton checkboxK = popupview.findViewById(R.id.apiInputCheckBox);
        checkboxK.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkboxK.setActivated(!checkboxK.isActivated());

            }
        });

        FiscalPrinter fiscalPrinter = dbA.selectFiscalPrinter();
        if (fiscalPrinter.getId() != -1)
        {
            if (fiscalPrinter.getModel().equals("Ditron"))
            {
                spinner.setSelection(1);
            }
            else if (fiscalPrinter.getModel().equals("Custom"))
            {
                spinner.setSelection(2);
            }
            else if (fiscalPrinter.getModel().equals("Epson"))
            {
                spinner.setSelection(3);
            }
            else if (fiscalPrinter.getModel().equals("RCH"))
            {
                spinner.setSelection(4);
            }
            if (!fiscalPrinter.getAddress().equals(""))
            {
                ((CustomEditText) popupview.findViewById(R.id.pos_printer_IP)).setText(fiscalPrinter
                                                                                               .getAddress());
            }

            if (fiscalPrinter.getPort() > 0)
            {
                ((CustomEditText) popupview.findViewById(R.id.pos_printer_port)).setText(String.valueOf(fiscalPrinter
                                                                                                                .getPort()));

            }

            if (fiscalPrinter.isUseApi())
            {
                checkbox.setActivated(true);
            }
        }


        RecyclerView printer_recycler = popupview.findViewById(R.id.printer_selected);
        printer_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        printer_recycler.setHasFixedSize(true);
        printerSettingAdapter = new PrinterSettingAdapter(this, dbA, popupview, popupWindow);
        printer_recycler.setAdapter(printerSettingAdapter);

        RecyclerView blackbox_recycler = popupview.findViewById(R.id.blackbox_recycler);
        blackbox_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        blackbox_recycler.setHasFixedSize(true);
        bbSettingAdaper = new BlackboxSettingAdapter(this, dbA, popupview, popupWindow);
        blackbox_recycler.setAdapter(bbSettingAdaper);


        popupview.findViewById(R.id.pos_printer).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                show = 1;
                popupview.findViewById(R.id.pos_printer)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.green_2));
                popupview.findViewById(R.id.kitchen_printer)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
                popupview.findViewById(R.id.black_box)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
                popupview.findViewById(R.id.kitchen_printer_selection).setVisibility(View.GONE);
                popupview.findViewById(R.id.pos_printer_selection).setVisibility(View.VISIBLE);
                popupview.findViewById(R.id.blackbox_selection).setVisibility(View.GONE);
                FiscalPrinter fiscalPrinter = dbA.selectFiscalPrinter();
                if (fiscalPrinter.getId() != -1)
                {
                    if (fiscalPrinter.getModel().equals("Ditron"))
                    {
                        spinner.setSelection(1);
                    }
                    else if (fiscalPrinter.getModel().equals("Custom"))
                    {
                        spinner.setSelection(2);
                    }
                    else if (fiscalPrinter.getModel().equals("Epson"))
                    {
                        spinner.setSelection(3);
                    }
                    else if (fiscalPrinter.getModel().equals("RCH"))
                    {
                        spinner.setSelection(4);
                    }
                    if (!fiscalPrinter.getAddress().equals(""))
                    {
                        ((CustomEditText) popupview.findViewById(R.id.pos_printer_IP)).setText(fiscalPrinter
                                                                                                       .getAddress());
                    }

                    if (fiscalPrinter.getPort() > 0)
                    {
                        ((CustomEditText) popupview.findViewById(R.id.pos_printer_port)).setText(String.valueOf(fiscalPrinter
                                                                                                                        .getPort()));

                    }
                    checkbox.setActivated(fiscalPrinter.isUseApi());
                }

            }
        });

        popupview.findViewById(R.id.kitchen_printer).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                show = 2;
                popupview.findViewById(R.id.pos_printer)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
                popupview.findViewById(R.id.kitchen_printer)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.green_2));
                popupview.findViewById(R.id.black_box)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
                popupview.findViewById(R.id.kitchen_printer_selection).setVisibility(View.VISIBLE);
                popupview.findViewById(R.id.pos_printer_selection).setVisibility(View.GONE);
                popupview.findViewById(R.id.blackbox_selection).setVisibility(View.GONE);
            }
        });

        popupview.findViewById(R.id.black_box).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                show = 3;
                popupview.findViewById(R.id.pos_printer)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
                popupview.findViewById(R.id.kitchen_printer)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.blackTransparent));
                popupview.findViewById(R.id.black_box)
                         .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.green_2));
                popupview.findViewById(R.id.kitchen_printer_selection).setVisibility(View.GONE);
                popupview.findViewById(R.id.pos_printer_selection).setVisibility(View.GONE);
                popupview.findViewById(R.id.blackbox_selection).setVisibility(View.VISIBLE);
            }
        });


        popupview.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (show)
                {
                    //
                    case 1:
                        String address = ((CustomEditText) popupview.findViewById(R.id.pos_printer_IP))
                                .getText()
                                .toString();
                        String port = ((CustomEditText) popupview.findViewById(R.id.pos_printer_port))
                                .getText()
                                .toString();
                        ImageButton checkbox = popupview.findViewById(R.id.api_checkBox);
                        int i = 0;
                        if (checkbox.isActivated())
                        {
                            i = 1;
                        }
                        if (printerModel == null)
                        {
                            Toast.makeText(MainActivity.this, R.string.select_printer_model, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else if (printerModel.equals(""))
                        {
                            Toast.makeText(MainActivity.this, R.string.select_printer_model, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else if (address.equals(""))
                        {
                            Toast.makeText(MainActivity.this, R.string.select_an_address, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else if (port.equals(""))
                        {
                            Toast.makeText(MainActivity.this, R.string.select_a_port_number, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else
                        {
                            if (StaticValue.blackbox)
                            {
                                FiscalPrinter       fiscalPrinter = dbA.selectFiscalPrinter();
                                List<NameValuePair> params        = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("model", printerModel));
                                params.add(new BasicNameValuePair("address", address));
                                params.add(new BasicNameValuePair("port", port));
                                params.add(new BasicNameValuePair("api", String.valueOf(i)));
                                params.add(new BasicNameValuePair("printerId", String.valueOf(fiscalPrinter
                                                                                                      .getId())));

                                callHttpHandler("/insertFiscalPrinter", params);
                            }
                            else
                            {

                                FiscalPrinter fiscalPrinter = dbA.selectFiscalPrinter();
                                if (fiscalPrinter.getId() == -1)
                                {
                                    dbA.insertFiscalPrinter(printerModel, address, printerModel, Integer
                                            .valueOf(port), i);
                                }
                                else
                                {
                                    dbA.updateFiscalPrinter(printerModel, address, printerModel, Integer
                                            .valueOf(port), i);
                                }
                                printerSettingAdapter.setPrinters();
                                Toast.makeText(MainActivity.this, R.string.updated, Toast.LENGTH_SHORT)
                                     .show();
                            }
                        }
                        new StaticValue(getApplicationContext(), StaticValue.blackboxInfo);
                        break;

                    // kitchen settings window
                    case 2:
                        String nameK = ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_name))
                                .getText()
                                .toString();
                        String addressK = ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_IP))
                                .getText()
                                .toString();
                        String portK = ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_port))
                                .getText()
                                .toString();
                        int iK = 0;
                        if (checkboxK.isActivated())
                        {
                            iK = 1;
                        }
                        if (nameK.equals(""))
                        {
                            Toast.makeText(MainActivity.this, "Select one name", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else if (addressK.equals(""))
                        {
                            Toast.makeText(MainActivity.this, "Select an address", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else if (portK.equals(""))
                        {
                            Toast.makeText(MainActivity.this, "Select a port number", Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else
                        {
                            //KitchenPrinter kitchenPrinter = dbA.selectKitchenPrinter();

                            if (StaticValue.blackbox)
                            {
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("name", nameK));
                                params.add(new BasicNameValuePair("address", addressK));
                                params.add(new BasicNameValuePair("port", portK));
                                params.add(new BasicNameValuePair("singleOrder", String.valueOf(iK)));
                                params.add(new BasicNameValuePair("printerId", String.valueOf(selectedKitchenPrinter
                                                                                                      .getId())));
                                myPopupView = popupview;
                                callHttpHandler("/insertKitchenPrinter", params);
                            }
                            else
                            {

                                if (selectedKitchenPrinter.getId() == -1)
                                {
                                    dbA.insertKitchenPrinter(nameK, addressK, Integer.valueOf(portK), iK);
                                }
                                else
                                {
                                    dbA.updateKitchenPrinter(nameK, addressK, Integer.valueOf(portK), iK, selectedKitchenPrinter
                                            .getId());
                                }
                                printerSettingAdapter.setPrinters();
                                ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_name))
                                        .setText("");
                                ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_IP)).setText("");
                                ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_port))
                                        .setText("");
                                checkboxK.setActivated(false);
                                selectedKitchenPrinter = new KitchenPrinter();
                                Toast.makeText(MainActivity.this, "Update", Toast.LENGTH_SHORT)
                                     .show();
                            }

                        }
                        new StaticValue(getApplicationContext(), StaticValue.blackboxInfo);
                        break;


                    // blackbox settings window
                    case 3:
                        String blackboxName = ((CustomEditText) popupview.findViewById(R.id.blackbox_name))
                                .getText()
                                .toString();
                        String blackboxAddress = ((CustomEditText) popupview.findViewById(R.id.blackbox_IP))
                                .getText()
                                .toString();

                        if (blackboxAddress.isEmpty() && bbSettingAdaper.blackbox != null)
                        {
                            new StaticValue(getApplicationContext(), bbSettingAdaper.blackbox);
                            popupWindow.dismiss();
                        }

                        else
                        {
                            myPopupView = popupview;
                            Log.i(TAG, "[onCreate] Testing blackbox: " + blackboxName + " @ " + blackboxAddress);

                            HttpHandler httpHandler = new HttpHandler();
                            httpHandler.testIp   = blackboxAddress;
                            httpHandler.delegate = MainActivity.this;

                            ArrayList<NameValuePair> params = new ArrayList<>();
                            params.add(new BasicNameValuePair("androidId", StaticValue.androidId));
                            httpHandler.UpdateInfoAsyncTask("/testBlackboxComm", params);

                            try
                            {
                                String     res        = httpHandler.execute().get();
                                JSONObject jsonObject = new JSONObject(res);

                                if (jsonObject.getBoolean("success"))
                                {
                                    BlackboxInfo b = new BlackboxInfo();
                                    b.setAddress(blackboxAddress);
                                    b.setName(blackboxName);
                                    dbA.insertBlackboxSync(b);

                                    bbSettingAdaper.notifyDataSetChanged();

                                    new StaticValue(getApplicationContext(), b);
                                }
                            }

                            // should never happen
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        break;

                    default:
                        break;
                }

            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupview.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (selectedKitchenPrinter.getId() == -1)
                {
                    popupWindow.dismiss();
                }
                else
                {
                    ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_name)).setText("");
                    ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_IP)).setText("");
                    ((CustomEditText) popupview.findViewById(R.id.kitchen_printer_port)).setText("");
                    checkboxK.setActivated(false);
                    selectedKitchenPrinter = new KitchenPrinter();
                }

            }
        });
    }

    public void openCashManagementPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_cash_management, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        dbA.showData("cash_management_set");

        setupDismissKeyboard(popupView);

        setupCashManagement(popupView, popupWindow);

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    public void setupCashManagement(final View popupView, final PopupWindow popupWindow)
    {
        CustomEditText min_cash         = popupView.findViewById(R.id.min_cash);
        CustomEditText max_cash         = popupView.findViewById(R.id.max_cash);
        CustomEditText min_withdraw     = popupView.findViewById(R.id.min_withdraw);
        CustomEditText five_cents       = popupView.findViewById(R.id.amount_005);
        CustomEditText ten_cents        = popupView.findViewById(R.id.amount_010);
        CustomEditText twenty_cents     = popupView.findViewById(R.id.amount_020);
        CustomEditText fifty_cents      = popupView.findViewById(R.id.amount_050);
        CustomEditText one_euro         = popupView.findViewById(R.id.amount_100);
        CustomEditText two_euros        = popupView.findViewById(R.id.amount_200);
        CustomEditText five_euros       = popupView.findViewById(R.id.amount_500);
        CustomEditText ten_euros        = popupView.findViewById(R.id.amount_1000);
        CustomEditText twenty_euros     = popupView.findViewById(R.id.amount_2000);
        CustomEditText fifty_euros      = popupView.findViewById(R.id.amount_5000);
        CustomEditText hundred_euros    = popupView.findViewById(R.id.amount_10000);
        CustomEditText twohundred_euros = popupView.findViewById(R.id.amount_20000);

        DecimalFormat twoD = new DecimalFormat("#.00");

        if (dbA.checkIfCashManagementIsSet() == 1)
        {
            CashManagement cash = dbA.getCashManagementStatic();
            min_cash.setText(twoD.format(cash.getMinCash()).replace(".", ",") + "");
            max_cash.setText(twoD.format(cash.getMaxCash()).replace(".", ",") + "");
            min_withdraw.setText(twoD.format(cash.getMinWithdraw()).replace(".", ",") + "");
            five_cents.setText(cash.getFiveCents() + "");
            ten_cents.setText(cash.getTenCents() + "");
            twenty_cents.setText(cash.getTwentyCents() + "");
            fifty_cents.setText(cash.getFiftyCents() + "");
            one_euro.setText(cash.getOneEuros() + "");
            two_euros.setText(cash.getTwoEuros() + "");
            five_euros.setText(cash.getFiveEuros() + "");
            ten_euros.setText(cash.getTenEuros() + "");
            twenty_euros.setText(cash.getTwentyEuros() + "");
            fifty_euros.setText(cash.getFiftyEuros() + "");
            hundred_euros.setText(cash.getHundredEuros() + "");
            twohundred_euros.setText(cash.getTwoHundredEuros() + "");
        }

        ImageButton okButton = popupView.findViewById(R.id.ok);
        ImageButton cancel   = popupView.findViewById(R.id.kill);

        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!min_cash.getText().toString().equals("") && !max_cash.getText()
                                                                          .toString()
                                                                          .equals("") && !min_withdraw
                        .getText()
                        .toString()
                        .equals(""))
                {

                    String min_cash_string     = min_cash.getText().toString();
                    String max_cash_string     = max_cash.getText().toString();
                    String min_withdraw_string = min_withdraw.getText().toString();

                    //all other fields not filled
                    if (five_cents.getText().toString().equals("") && ten_cents.getText()
                                                                               .toString()
                                                                               .equals("") && twenty_cents
                            .getText()
                            .toString()
                            .equals("")
                            && fifty_cents.getText().toString().equals("") && one_euro.getText()
                                                                                      .toString()
                                                                                      .equals("") && two_euros
                            .getText()
                            .toString()
                            .equals("")
                            && five_euros.getText().toString().equals("") && ten_euros.getText()
                                                                                      .toString()
                                                                                      .equals("") && twenty_euros
                            .getText()
                            .toString()
                            .equals("")
                            && fifty_euros.getText()
                                          .toString()
                                          .equals("") && hundred_euros.getText()
                                                                      .toString()
                                                                      .equals("")
                            && twohundred_euros.getText().toString().equals(""))
                    {

                        dbA.insertCashManagementGeneral(Float.parseFloat(min_cash_string), Float.parseFloat(max_cash_string), Float
                                .parseFloat(min_withdraw_string));

                        min_cash.setText("");
                        max_cash.setText("");
                        min_withdraw.setText("");

                        popupWindow.dismiss();
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

                        dbA.insertCashManagement(Float.parseFloat(min_cash_string.replace(",", ".")),
                                                 Float.parseFloat(max_cash_string.replace(",", ".")),
                                                 Float.parseFloat(min_withdraw_string.replace(",", ".")), fiveCents
                                                         .equals("") ? 0 : Integer.parseInt(fiveCents),
                                                 tenCents.equals("") ? 0 : Integer.parseInt(tenCents), twentyCents
                                                         .equals("") ? 0 : Integer.parseInt(twentyCents),
                                                 fiftyCents.equals("") ? 0 : Integer.parseInt(fiftyCents), oneE
                                                         .equals("") ? 0 : Integer.parseInt(oneE),
                                                 twoE.equals("") ? 0 : Integer.parseInt(twoE), fiveE
                                                         .equals("") ? 0 : Integer.parseInt(fiveE), tenE
                                                         .equals("") ? 0 : Integer.parseInt(tenE),
                                                 twentyE.equals("") ? 0 : Integer.parseInt(twentyE), fiftyE
                                                         .equals("") ? 0 : Integer.parseInt(fiftyE),
                                                 hundred.equals("") ? 0 : Integer.parseInt(hundred), twoHundred
                                                         .equals("") ? 0 : Integer.parseInt(twoHundred)
                        );

                        min_cash.setText("");
                        max_cash.setText("");
                        min_withdraw.setText("");
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
                }
                else
                {
                    Toast.makeText(me, R.string.please_fill_cash_and_withdraw_fields, Toast.LENGTH_SHORT)
                         .show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                min_cash.setText("");
                max_cash.setText("");
                min_withdraw.setText("");
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

    private void setupRegistrationLayout()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_registration, null);
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

                setUpRegistrationLayout(popupView, popupWindow);


            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);


    }

    private void setUpRegistrationLayout(final View popupview, final PopupWindow popupWindow)
    {
        DeviceInfo deviceInfo = dbA.selectDeviceInfo();
        dbA.showData("device_info");
        CustomEditText Address        = popupview.findViewById(R.id.address_et);
        CustomEditText StoreEmail     = popupview.findViewById(R.id.first_r_email_et);
        CustomEditText RagioneSociale = popupview.findViewById(R.id.store_name_et);
        CustomEditText PartitaIva     = popupview.findViewById(R.id.store_IVA);
        CustomEditText Cap            = popupview.findViewById(R.id.cap_et);
        CustomEditText Comune         = popupview.findViewById(R.id.comune_et);
        CustomEditText Provincia      = popupview.findViewById(R.id.provincia_et);
        CustomEditText StoreName      = popupview.findViewById(R.id.store_name_invoice_et);
        RagioneSociale.setText(deviceInfo.getRagioneSociale());
        PartitaIva.setText(deviceInfo.getPartitaIva());
        Address.setText(deviceInfo.getAddress());
        StoreEmail.setText(deviceInfo.getEmail());
        Cap.setText(deviceInfo.getCap());
        Comune.setText(deviceInfo.getComune());
        Provincia.setText(deviceInfo.getProvincia());
        StoreName.setText(deviceInfo.getStoreName());


        licenseButton = popupview.findViewById(R.id.licensing);
        licenseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                licenseString = fireSingleInputDialogPopup();
            }
        });

        popupview.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(me, R.string.updated, Toast.LENGTH_SHORT).show();
                String ragioneSociale = RagioneSociale.getText().toString();
                String partitaIva     = PartitaIva.getText().toString();
                String address        = Address.getText().toString();
                String storeEmail     = StoreEmail.getText().toString().replaceAll(" ", "");
                String cap            = Cap.getText().toString();
                String comune         = Comune.getText().toString();
                String provincia      = Provincia.getText().toString();
                String storeName      = StoreName.getText().toString();

                if (ragioneSociale.equals(""))
                {
                    Toast.makeText(getApplicationContext(), R.string.please_insert_your_store_name, Toast.LENGTH_SHORT)
                         .show();
                }
                else if (!storeEmail.equals("") && !Pattern.matches("^[-a-zA-Z0-9._%]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", storeEmail))
                {
                    Toast.makeText(getApplicationContext(), R.string.store_email_is_not_well_formed, Toast.LENGTH_SHORT)
                         .show();
                }
                else
                {

                    if (StaticValue.blackbox)
                    {
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        String android_id = Settings.Secure.getString(getApplicationContext()
                                                                              .getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));
                        params.add(new BasicNameValuePair("deviceId", String.valueOf(deviceInfo.getId())));
                        params.add(new BasicNameValuePair("ragioneSociale", ragioneSociale));
                        params.add(new BasicNameValuePair("partitaIva", partitaIva));
                        params.add(new BasicNameValuePair("address", address));
                        params.add(new BasicNameValuePair("comune", comune));
                        params.add(new BasicNameValuePair("cap", cap));
                        params.add(new BasicNameValuePair("provincia", provincia));
                        params.add(new BasicNameValuePair("email", storeEmail));
                        params.add(new BasicNameValuePair("storeName", storeName));
                        String license = "";
                        if (!licenseString.equals(""))
                        {
                            license = licenseString;
                        }
                        else
                        {
                            license = "-1";
                        }
                        params.add(new BasicNameValuePair("license", license));
                        callHttpHandler("/updateAdminDeviceInfo", params);
                        myPopupWindow = popupWindow;
                    }
                    else
                    {
                        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        dbA.updateDeviceInfo(deviceInfo.getId(), ragioneSociale, partitaIva, address, provincia, comune, cap, storeEmail, android_id);

                        if (!licenseString.equals(""))
                        {
                            if (dbA.checkIfStaticCodeIsUsed(licenseString))
                            {
                                Date             c             = Calendar.getInstance().getTime();
                                SimpleDateFormat df            = new SimpleDateFormat("dd/MM/yyyy");
                                String           formattedDate = df.format(c);
                                dbA.execOnDb("UPDATE registered_activation_code SET code='" + licenseString + "' , SET registration='" + formattedDate + "'");
                                dbA.execOnDb("UPDATE static_activation_code SET used=1 where code='" + licenseString + "'");
                                Toast.makeText(me, R.string.license_updated, Toast.LENGTH_SHORT)
                                     .show();
                            }
                            else
                            {
                                Toast.makeText(me, R.string.cant_update_license_code, Toast.LENGTH_SHORT)
                                     .show();
                            }
                        }
                        dbA.showData("static_activation_code");
                        new StaticValue(getApplicationContext(), StaticValue.blackboxInfo);
                        popupWindow.dismiss();
                    }
                }


            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupview.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();

            }
        });
    }

    public void fireSingleInputDialogForTimerPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_single_input, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );


        RelativeLayout other_input  = popupView.findViewById(R.id.single_input_window);
        RelativeLayout timer_layout = popupView.findViewById(R.id.single_input_timer_window);

        timer_layout.setVisibility(View.VISIBLE);
        other_input.setVisibility(View.GONE);

        CustomEditText timer_input = popupView.findViewById(R.id.single_input_timer);
        timer_input.setHint(R.string.insert_reservation_timer_period);
        if (dbA.getReservationPopupTimer() != 0)
        {
            timer_input.setText(resources.getString(R.string._minutes, dbA.getReservationPopupTimer()));
        }

        ImageButton okButton = popupView.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!timer_input.getText().toString().equals("") && timer_input.getText()
                                                                               .toString()
                                                                               .length() <= 2)
                {
                    int timer = Integer.parseInt(timer_input.getText().toString());
                    timer_input.setText("");

                    dbA.setReservationPopupTimer(timer);

                    popupWindow.dismiss();
                }
                else
                {
                    timer_input.setText("");
                    popupWindow.dismiss();
                }
            }
        });

        ImageButton killButton = popupView.findViewById(R.id.kill);
        killButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);
    }

    public String fireSingleInputDialogPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_single_input, null);
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
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.single_input_window).getLayoutParams();
                int top1 = (int) (dpHeight - 52) / 2 - rlp1.height / 2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.single_input_window).setLayoutParams(rlp1);*/
            }
        });

        CustomEditText license_input = popupView.findViewById(R.id.single_input);
        license_input.setHint(R.string.insert_license_code);
        //license_input.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);

        ImageButton okButton = popupView.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!license_input.getText().toString().equals(""))
                {
                    licenseString = license_input.getText().toString();
                    license_input.setText("");

                    licenseButton.setText(licenseString);

                    popupWindow.dismiss();
                }
                else
                {
                    license_input.setText("");
                    Toast.makeText(getApplicationContext(), R.string.insert_a_valid_license_key, Toast.LENGTH_SHORT)
                         .show();
                }
            }
        });

        ImageButton killButton = popupView.findViewById(R.id.kill);
        killButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

        return licenseString;
    }

    /**
     * Modifiers settings setup
     */
    private void setModifiersSettings()
    {
        mod_group_recyclerview = findViewById(R.id.modifiersGroupRecyclerView);
        grid_manager           = new GridLayoutManager(this, 6);
        grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {

            @Override
            public int getSpanSize(int position)
            {
                return 1;
            }
        });
        mod_group_recyclerview.setLayoutManager(grid_manager);
        mod_group_recyclerview.addItemDecoration(new ModifiersGroupLineSeparator(this, 14));
        group_rv_adapter = new ModifiersGroupAdapter(this, dbA, grid_status.getCurrent_product(), mod_group_recyclerview);
        ((CustomTextView) ((Activity) me).findViewById(R.id.admin_username_tv)).setText(username);
        mod_group_recyclerview.setAdapter(group_rv_adapter);
        ItemTouchHelper.Callback callback =
                new ItemTouchHelperCallback(group_rv_adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mod_group_recyclerview);
        isAdded = false;

        // buttons onClick setup
        ((CustomButton) findViewById(R.id.addGroup)).setText(R.string.select_group);
        findViewById(R.id.addGroup).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //addModifier non era attivo
                if (!((Activity) me).findViewById(R.id.addModifier).isActivated())
                {
                    v.setActivated(!v.isActivated());
                    //addGroup is now active
                    if (v.isActivated())
                    {
                        group_rv_adapter.turnGroupModifiersOFF();
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_modifier_group);
                        ((CustomButton) v).setText(R.string.add_group);
                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        group_rv_adapter.setAddGroupToProduct();
                        group_rv_adapter.switchAddGroupActive(true);
                        if (modifiers_rv_adapter != null)
                        {
                            modifiers_rv_adapter.setNotesContainer(false);
                        }
                    }
                    //addGroup is now inactive
                    else
                    {
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_modifier_group);
                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        ((CustomButton) v).setText(R.string.select_group);
                        group_rv_adapter.setFireGroupPopupWindow();
                        group_rv_adapter.switchAddGroupActive(false);
                    }
                }
                //addModifier era attivo
                else
                {
                    ((Activity) me).findViewById(R.id.addModifier).setActivated(false);
                    ((CustomButton) findViewById(R.id.addModifier)).setText(R.string.select_modifier);
                    ((CustomTextView) findViewById(R.id.infoTextView))
                            .setText(R.string.info_tv_main_modifiers);
                    if (group_rv_adapter.addModifierActive)
                    {
                        group_rv_adapter.switchAddModifierActive();
                    }
                    if (group_rv_adapter.addModifierActiveFixed)
                    {
                        group_rv_adapter.switchAddModifierActiveFixed();
                    }
                    v.setActivated(!v.isActivated());
                    //addGroup is now active
                    if (v.isActivated())
                    {
                        group_rv_adapter.turnGroupModifiersOFF();
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_modifier_group);
                        ((CustomButton) v).setText(R.string.add_groups);
                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        group_rv_adapter.setAddGroupToProduct();
                        group_rv_adapter.switchAddGroupActive(true);
                        if (modifiers_rv_adapter != null)
                        {
                            modifiers_rv_adapter.setNotesContainer(false);
                        }
                    }
                    //addGroup is now inactive
                    else
                    {
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_modifier_group);
                        ((CustomButton) v).setText(R.string.select_group);
                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        group_rv_adapter.setFireGroupPopupWindow();
                        group_rv_adapter.switchAddGroupActive(false);
                    }
                }
            }
        });

        findViewById(R.id.addGroup).setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {

                LayoutInflater inflater = (LayoutInflater) me
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.popup_two_button, null);
                /*RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)v.findViewById(R.id.popup_container)
                        .getLayoutParams();
                int t = (int)(dpHeight-52)/2 - rlp.height/2;
                rlp.topMargin = t;
                v.findViewById(R.id.popup_container).setLayoutParams(rlp);*/
                CustomTextView title        = v.findViewById(R.id.popup_text);
                CustomButton   firstButton  = v.findViewById(R.id.firstButton);
                CustomButton   secondButton = v.findViewById(R.id.secondButton);

                title.setText(R.string.switch_to_permanent_modifiers_mode);
                title.setAllCaps(true);
                firstButton.setText(R.string.cancel);
                firstButton.setAllCaps(false);
                firstButton.setLetterSpacing(0);
                secondButton.setText(R.string.ok);

                final PopupWindow popupWindow = new PopupWindow(v, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

                firstButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        popupWindow.dismiss();
                    }
                });

                secondButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        popupWindow.dismiss();
                        //addGroup was inactive
                        if (!((Activity) me).findViewById(R.id.addGroup).isActivated())
                        {
                            ((Activity) me).findViewById(R.id.addGroup).setActivated(!
                                                                                             ((Activity) me)
                                                                                                     .findViewById(R.id.addGroup)
                                                                                                     .isActivated());
                            group_rv_adapter.setAddGroupActive();
                        }
                        //addModifier era attivo
                        if (((Activity) me).findViewById(R.id.addModifier).isActivated())
                        {
                            ((Activity) me).findViewById(R.id.addModifier).setActivated(false);
                            ((CustomButton) findViewById(R.id.addModifier)).setText(R.string.select_modifier);
                        }
                        group_rv_adapter.turnGroupModifiersOFF();
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_permanent_modifier_group);
                        ((CustomTextView) findViewById(R.id.infoTextView)).
                                                                                  setText(R.string.info_tv_main_permanent_modifiers);
                        ((CustomButton) findViewById(R.id.addGroup)).setText(R.string.add_group);
                        //group_rv_adapter.setAddGroupToProductFixed();
                        group_rv_adapter.switchAddGroupActiveFixed(true);
                        //se era attiva la barra delle note, la disattivo
                        if (modifiers_rv_adapter != null)
                        {
                            modifiers_rv_adapter.setNotesContainer(false);
                        }
                    }
                });

                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(((Activity) me).findViewById(R.id.main), 0, 0, 0);

                return true;
            }
        });

        // addModifier
        ((CustomButton) findViewById(R.id.addModifier)).setText(R.string.select_modifier);
        findViewById(R.id.addModifier).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //addGroup non era attivo
                if (!((Activity) me).findViewById(R.id.addGroup).isActivated())
                {
                    v.setActivated(!v.isActivated());
                    //addModifier is now inactive
                    if (!v.isActivated())
                    {
                        //v.setActivated(!v.isActivated());
                        ((CustomButton) v).setText(R.string.select_modifier);
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv))
                                .setText(R.string.select_modifier_group);
                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        group_rv_adapter.turnGroupModifiersOFF();
                        group_rv_adapter.setFireModifierPopupWindow();
                        if (group_rv_adapter.getAddModifierActive())
                        {
                            group_rv_adapter.setAddGroupActive();
                        }
                        if (group_rv_adapter.getAddGroupActiveFixed())
                        {
                            group_rv_adapter.setAddGroupActiveFixed();
                        }
                        if (group_rv_adapter.addModifierActive)
                        {
                            group_rv_adapter.switchAddModifierActive();
                        }
                        if (group_rv_adapter.addModifierActiveFixed)
                        {
                            group_rv_adapter.switchAddModifierActiveFixed();
                        }

                        if (modifiers_rv_adapter != null)
                        {
                            modifiers_rv_adapter.switchAddModifierActive(false);
                            modifiers_rv_adapter.switchAddModifierActiveFixed(false);
                            wereModifiersOpen = false;
                            findViewById(R.id.modifiers_tv).setVisibility(View.INVISIBLE);
                            findViewById(R.id.modifiers_tv_hline).setVisibility(View.INVISIBLE);
                            findViewById(R.id.modifiersRecyclerView).setVisibility(View.INVISIBLE);
                            //se c'era la barra delle note, la disattivo
                            modifiers_rv_adapter.setNotesContainer(false);
                            //myLine.setOffset(0);
                        }
                    }
                    //addModifier is now active
                    else
                    {
                        //v.setActivated(!v.isActivated());
                        ((CustomButton) v).setText(R.string.add_modifier);
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv))
                                .setText(R.string.select_modifier_group);
                        ((CustomTextView) findViewById(R.id.modifiers_tv))
                                .setText(R.string.select_modifiers);
                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        group_rv_adapter.turnGroupModifiersOFF();
                        //addModifierActive viene già settato qui
                        group_rv_adapter.switchAddModifierActive();
                        //per adesso questo lo tengo, poi vediamo
                        if (wereModifiersOpen && v.isActivated())
                        {

                            modifiers_rv_adapter.switchAddModifierActive(true);
                            modifiers_rv_adapter.setAddModifierActiveFixed(false);
                        }
                        else if (wereModifiersOpen)
                        {
                            modifiers_rv_adapter.switchAddModifierActive(false);
                        }
                    }
                }
                //AddGroup was active (here i've changed something)
                else if (((Activity) me).findViewById(R.id.addGroup).isActivated())
                {
                    ((Activity) me).findViewById(R.id.addGroup).setActivated(false);
                    ((CustomButton) findViewById(R.id.addGroup)).setText(R.string.select_group);

                    //dato che addGroup era attivo, allora devo settare opportunamente i booleani
                    if (group_rv_adapter.getAddGroupActive())
                    {
                        group_rv_adapter.setAddGroupActive();
                    }
                    if (group_rv_adapter.getAddGroupActiveFixed())
                    {
                        group_rv_adapter.setAddGroupActiveFixed();
                    }
                    v.setActivated(!v.isActivated());
                    //addModifier is now active
                    if (v.isActivated())
                    {
                        ((CustomButton) v).setText(R.string.add_modifier);
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv))
                                .setText(R.string.select_modifier_group);
                        ((CustomTextView) findViewById(R.id.modifiers_tv))
                                .setText(R.string.select_modifiers);
                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        //group_rv_adapter.setAddModifierToProduct();
                        group_rv_adapter.turnGroupModifiersOFF();
                        group_rv_adapter.switchAddModifierActive();
                        if (wereModifiersOpen && v.isActivated())
                        {

                            modifiers_rv_adapter.switchAddModifierActive(true);
                            modifiers_rv_adapter.setAddModifierActiveFixed(false);
                        }
                        else if (wereModifiersOpen)
                        {
                            modifiers_rv_adapter.switchAddModifierActive(false);
                        }
                    }
                    //addModifier is now inactive
                    else
                    {
                        ((CustomButton) v).setText(R.string.select_modifier);
                        ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_modifier_group);

                        ((CustomTextView) findViewById(R.id.infoTextView))
                                .setText(R.string.info_tv_main_modifiers);
                        group_rv_adapter.turnGroupModifiersOFF();
                        group_rv_adapter.setFireModifierPopupWindow();

                        if (group_rv_adapter.addModifierActive)
                        {
                            group_rv_adapter.switchAddModifierActive();
                        }

                        if (group_rv_adapter.addModifierActiveFixed)
                        {
                            group_rv_adapter.switchAddModifierActiveFixed();
                        }

                        if (modifiers_rv_adapter != null)
                        {
                            modifiers_rv_adapter.switchAddModifierActive(false);
                            modifiers_rv_adapter.switchAddModifierActiveFixed(false);
                            wereModifiersOpen = false;
                            findViewById(R.id.modifiers_tv).setVisibility(View.INVISIBLE);
                            findViewById(R.id.modifiers_tv_hline).setVisibility(View.INVISIBLE);
                            findViewById(R.id.modifiersRecyclerView).setVisibility(View.INVISIBLE);
                            //se c'era la barra delle note, la disattivo
                            modifiers_rv_adapter.setNotesContainer(false);
                            //myLine.setOffset(0);
                        }
                    }
                }
            }
        });



        findViewById(R.id.addModifier).setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                LayoutInflater inflater = (LayoutInflater) me.getSystemService(LAYOUT_INFLATER_SERVICE);

                v = inflater.inflate(R.layout.popup_two_button, null);


                CustomTextView title        = v.findViewById(R.id.popup_text);
                CustomButton   firstButton  = v.findViewById(R.id.firstButton);
                CustomButton   secondButton = v.findViewById(R.id.secondButton);

                title.setText(R.string.switch_to_permanent_modifiers_mode);
                title.setAllCaps(true);
                firstButton.setText(R.string.cancel);
                firstButton.setAllCaps(false);
                firstButton.setLetterSpacing(0);
                secondButton.setText(R.string.ok);

                final PopupWindow popupWindow = new PopupWindow(v, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

                firstButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        popupWindow.dismiss();
                    }
                });

                secondButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        popupWindow.dismiss();
                        //addGroup was inactive
                        if (!((Activity) me).findViewById(R.id.addGroup).isActivated())
                        {
                            if (!((Activity) me).findViewById(R.id.addModifier).isActivated())
                            {
                                ((Activity) me).findViewById(R.id.addModifier).setActivated(!((Activity) me).findViewById(R.id.addModifier).isActivated());
                            }

                            ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_permanent_modifier_group);

                            ((CustomTextView) findViewById(R.id.infoTextView)).setText(R.string.info_tv_main_permanent_modifiers);

                            ((CustomButton) findViewById(R.id.addModifier)).setText(R.string.add_modifier);

                            group_rv_adapter.turnGroupModifiersOFF();
                            group_rv_adapter.setAddModifierToProductFixed();

                            if (group_rv_adapter.addModifierActive)
                            {
                                group_rv_adapter.switchAddModifierActive();
                            }

                            if (modifiers_rv_adapter != null)
                            {
                                modifiers_rv_adapter.switchAddModifierActive(false);
                                modifiers_rv_adapter.switchAddModifierActiveFixed(false);
                                wereModifiersOpen = false;
                                ((CustomTextView) findViewById(R.id.modifiers_tv)).setText(R.string.select_permanent_modifiers);
                                findViewById(R.id.modifiers_tv).setVisibility(View.INVISIBLE);
                                findViewById(R.id.modifiers_tv_hline).setVisibility(View.INVISIBLE);
                                findViewById(R.id.modifiersRecyclerView).setVisibility(View.INVISIBLE);
                                //se c'era la barra delle note, la disattivo
                                modifiers_rv_adapter.setNotesContainer(false);
                                //myLine.setOffset(0);
                            }
                        }

                        //addGroup was active
                        else if (((Activity) me).findViewById(R.id.addGroup).isActivated())
                        {
                            ((Activity) me).findViewById(R.id.addGroup).setActivated(false);
                            ((CustomButton) findViewById(R.id.addGroup)).setText(R.string.select_group);
                            ((Activity) me).findViewById(R.id.addModifier).setActivated(!((Activity) me).findViewById(R.id.addModifier).isActivated());

                            if (group_rv_adapter.getAddGroupActive())
                            {
                                group_rv_adapter.setAddGroupActive();
                            }

                            if (group_rv_adapter.getAddGroupActiveFixed())
                            {
                                group_rv_adapter.setAddGroupActiveFixed();
                            }

                            ((CustomTextView) findViewById(R.id.modifiers_group_tv)).setText(R.string.select_permanent_modifier_group);

                            ((CustomTextView) findViewById(R.id.infoTextView)).setText(R.string.info_tv_main_permanent_modifiers);

                            ((CustomButton) findViewById(R.id.addModifier)).setText(R.string.add_modifier);

                            group_rv_adapter.setAddModifierToProductFixed();
                            group_rv_adapter.turnGroupModifiersOFF();

                            if (group_rv_adapter.addModifierActive)
                            {
                                group_rv_adapter.switchAddModifierActive();
                            }

                            if (modifiers_rv_adapter != null)
                            {
                                modifiers_rv_adapter.switchAddModifierActive(false);
                                modifiers_rv_adapter.switchAddModifierActiveFixed(false);
                                wereModifiersOpen = false;
                                ((CustomTextView) findViewById(R.id.modifiers_tv)).setText(R.string.select_permanent_modifiers);
                                findViewById(R.id.modifiers_tv).setVisibility(View.INVISIBLE);
                                findViewById(R.id.modifiers_tv_hline).setVisibility(View.INVISIBLE);
                                findViewById(R.id.modifiersRecyclerView).setVisibility(View.INVISIBLE);
                                //se c'era la barra delle note, la disattivo
                                modifiers_rv_adapter.setNotesContainer(false);
                                //myLine.setOffset(0);
                            }
                        }
                    }
                });

                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(((Activity) me).findViewById(R.id.main), 0, 0, 0);

                return true;
            }
        });

        findViewById(R.id.backToMainMenu).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchView(3, 0);
            }
        });
    }


    public void closeModifiersView()
    {
        if (wereModifiersOpen)
        {
            findViewById(R.id.modifiers_tv).setVisibility(View.INVISIBLE);
            findViewById(R.id.modifiers_tv_hline).setVisibility(View.INVISIBLE);
            findViewById(R.id.modifiersRecyclerView).setVisibility(View.INVISIBLE);
            findViewById(R.id.modifier_notes_container).setVisibility(View.INVISIBLE);
            //wereModifiersOpen = false;
        }
        isAdded = false;
    }


    public void openModifiersView(int groupID)
    {
        findViewById(R.id.modifiers_tv).setVisibility(View.VISIBLE);
        findViewById(R.id.modifiers_tv_hline).setVisibility(View.VISIBLE);
        findViewById(R.id.modifiersRecyclerView).setVisibility(View.VISIBLE);
        ((CustomTextView) findViewById(R.id.modifiers_tv)).setText(R.string.select_modifiers);
        if (!wereModifiersOpen)
        {
            grid_manager = new GridLayoutManager(this, 4);
            grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
            {

                @Override
                public int getSpanSize(int position)
                {
                    return 1;
                }
            });
            modifiers_recyclerview = findViewById(R.id.modifiersRecyclerView);
            modifiers_recyclerview.setLayoutManager(grid_manager);
            if (!isAdded)
            {
                modifiers_recyclerview.addItemDecoration(myLine);
                isAdded = true;
            }
            modifiers_rv_adapter = new ModifierAdapter(this, dbA, grid_status.getCurrent_product(),
                                                       groupID, group_rv_adapter
            );
            modifiers_recyclerview.setAdapter(modifiers_rv_adapter);
            ItemTouchHelper.Callback callback =
                    new ItemTouchHelperCallback(modifiers_rv_adapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(modifiers_recyclerview);
            wereModifiersOpen = true;
            group_rv_adapter.setChildAdapter(modifiers_rv_adapter);
            if (group_rv_adapter.getChecknotes())
            {
                modifiers_rv_adapter.setNotesContainer(true);
            }
            modifiers_recyclerview.post(new Runnable()
            {
                @Override
                public void run()
                {
                    modifiers_rv_adapter.turnModifiersOFF();
                    if (group_rv_adapter.addModifierActive)
                    {
                        modifiers_rv_adapter.switchAddModifierActive(true);
                    }
                    else if (group_rv_adapter.addModifierActiveFixed)
                    {
                        modifiers_rv_adapter.switchAddModifierActiveFixed(true);
                    }
                    group_rv_adapter.turnGroupModifiersOFF();
                    group_rv_adapter.getItemFromId(groupID).setActivated(true);
                    group_rv_adapter.setSelectedGroup(
                            (ModifiersGroupAdapter.ModifiersGroup) group_rv_adapter.getItemFromId(groupID)
                                                                                   .getTag()
                    );
                }
            });
        }

        else
        {
            if (group_rv_adapter.getChecknotes())
            {
                modifiers_rv_adapter.setNotesContainer(true);
            }

            modifiers_rv_adapter.updateDataSet(groupID);
            modifiers_recyclerview.post(new Runnable()
            {
                @Override
                public void run()
                {
                    modifiers_rv_adapter.turnModifiersOFF();
                    if (group_rv_adapter.addModifierActive)
                    {
                        modifiers_rv_adapter.switchAddModifierActive(true);
                    }

                    else if (group_rv_adapter.addModifierActiveFixed)
                    {
                        modifiers_rv_adapter.switchAddModifierActiveFixed(true);
                    }

                    group_rv_adapter.turnGroupModifiersOFF();
                    group_rv_adapter.getItemFromId(groupID).setActivated(true);
                    group_rv_adapter.setSelectedGroup(
                            (ModifiersGroupAdapter.ModifiersGroup) group_rv_adapter.getItemFromId(groupID)
                                                                                   .getTag()
                    );
                }
            });
        }
    }


    private void getHomeButtonSet()
    {
        buttons         = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = 0 AND id!=-30 and id!=-20 ORDER BY position");
        big_plus_button = new ButtonLayout(this);
        big_plus_button.setID(-11);
        big_plus_button.setPos(buttons.size());
        big_plus_button.setImg("big_plus_button");
        buttons.add(big_plus_button);
    }


    public void setGridStatus(StatusInfoHolder s)
    {
        grid_status = s;
    }

    public void notifyVatValueModified()
    {

    }


    @Override
    public void setModifyUser(User user, final View popupview, final PopupWindow popupWindow)
    {
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
        Passcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        final ImageButton manager = popupview.findViewById(R.id.manager_checkbox);
        //final ImageButton cashier = (ImageButton)findViewById(R.id.cashier_checkbox);
        if (user.getUserRole() == 1 || user.getUserRole() == 0)
        {
            manager.setActivated(true);
        }
        // cashier.setActivated(false);
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
                    Toast.makeText(MainActivity.this, R.string.you_cant_change_your_admin_role, Toast.LENGTH_SHORT)
                         .show();
                }
            }
        });
        /**
         * OK Button behavior while in New User window
         */
        popupview.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
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
                    Toast.makeText(getBaseContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT)
                         .show();
                }
                else if (dbA.checkIfPasscodeExistsWithId(passcode, user.getId()))
                {
                    Toast.makeText(getBaseContext(), R.string.passcode_is_already_used, Toast.LENGTH_SHORT)
                         .show();
                }
                else if (!Pattern.matches("^[-a-zA-Z0-9_]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                {
                    Toast.makeText(getBaseContext(), R.string.not_a_valid_email, Toast.LENGTH_SHORT)
                         .show();
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
                        myPopupView   = popupview;
                        myPopupWindow = popupWindow;
                        httpHandler.UpdateInfoAsyncTask("/updateUser", params);
                        httpHandler.execute();
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
                        setupNewUserWindow(popupview, popupWindow);
                    }

                   /* String password = passcode;
                    if(user.getUserRole()==0){
                        dbA.updateUser(name, surname, passcode,email,  0, user.getId());
                    }else {
                        if (manager.isActivated()) {
                            dbA.updateUser(name, surname, passcode,email,  1, user.getId());
                        } else *//*if(cashier.isActivated())*//* {
                            dbA.updateUser(name, surname, passcode, email, 2, user.getId());
                        }
                    }
                    setupNewUserWindow(popupview, popupWindow);*/

                }
            }
        });
        /**
         *  X button behavior while in New User window
         */
        popupview.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupNewUserWindow(popupview, popupWindow);
            }
        });
    }

    @Override
    public void onTaskEndWithResult(String success)
    {

    }

    @Override
    public void onTaskFinishGettingData(String result)
    {

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
                            Log.d(TAG, "[setupdDismissKeyboard]::[OnFocusChange] You clicked out of an Edit Text!");
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
} // END OF MainActivity




