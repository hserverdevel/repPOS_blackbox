package com.example.blackbox.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;


import com.example.blackbox.R;
import com.example.blackbox.adapter.ModifierAdapter;
import com.example.blackbox.adapter.ModifiersGroupAdapter;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.model.BlackboxInfo;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.Client;
import com.example.blackbox.model.ClientInCompany;
import com.example.blackbox.model.Company;
import com.example.blackbox.model.DeviceInfo;
import com.example.blackbox.model.Fidelity;
import com.example.blackbox.model.FiscalPrinter;
import com.example.blackbox.model.KitchenPrinter;
import com.example.blackbox.model.ModifierAssigned;
import com.example.blackbox.model.ModifierGroupAssigned;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.Table;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.model.User;
import com.example.blackbox.model.Vat;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.example.blackbox.server.LocalNotification;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by tiziano on 4/2/19.
 */

public class SplashScreen1 extends Activity implements HttpHandler.AsyncResponse
{

    private static final String TAG = "<SplashScreen1>";

    public float density;
    public float dpHeight;
    public float dpWidth;

    private Context context;

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private String status = "";
    private String username = "";
    private int userType;
    private DatabaseAdapter dbA;

    private String licenseString = "";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        context = this;

        Intent intent = getIntent();
        status = intent.getStringExtra("status");

        username = intent.getStringExtra("username");
        if (username==null) { username= ""; }

        userType = intent.getIntExtra("isAdmin", -1);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        this.dbA = new DatabaseAdapter(this);

        // check if any of the blackbox that have been saved
        // can be used. Otherwise, ask for a new one with a Popup
        List<BlackboxInfo> allBlackbox = dbA.selectAllBlackbox();
        // if the goodBlackbox contains the blackbox that has passed
        // the test, and thus can be used. If no blackbox pass the test,
        // goodBlackbox is null.
        BlackboxInfo goodBlackbox = testBlackboxes(allBlackbox);
        if (allBlackbox.isEmpty())
            { openPopupBlackboxIP(1); }

        // if none of the stored blackboxes manage to connect
        else if (goodBlackbox == null)
        {
            int lastBlackboxId = allBlackbox.get(allBlackbox.size() - 1).getId();
            openPopupBlackboxIP(lastBlackboxId);
        }

        // one of the blacbox that was saved in memory works
        else
        {
            Toast.makeText(this, "Connecting to blackbox " + goodBlackbox.getName() + "@" + goodBlackbox.getAddress(), Toast.LENGTH_LONG).show();
            new StaticValue(getApplicationContext(), goodBlackbox);
        }

        LocalNotification notificationService = LocalNotification.getInstance();
        notificationService.setLocalNotification(getApplicationContext());
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

        try
        {
            jsonObject = new JSONObject(output);
            route = jsonObject.getString("route");
            success = jsonObject.getBoolean("success");
        }

        catch (Exception e )
            { e.printStackTrace(); }


        // if we are testing the blackbox, a success: false result could be expected
        if (success || (!success && route.equals("testBlackboxComm")))
        {
            boolean check = false;

            try
            {
                switch (route)
                {
                    case "testBlackboxComm":
                        try
                        {
                            // if check is present, is always true,
                            // since this is a simple test of communication
                            jsonObject.getBoolean("check");

                            // is this device known to the blackbox?
                            // if not, start the registration
                            if (!jsonObject.getBoolean("deviceKnown"))
                                { setupRegistrationPopupLayout(); }

                            // if the device is already known to the blackbox,
                            // start the sync with the blackbox
                            else
                            {
                                // since the test worked, start the pipeline of updates
                                String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);

                                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

                                params.add(new BasicNameValuePair("androidId", android_id));
                                params.add(new BasicNameValuePair("ip", ip));

                                callHttpHandler("/updateDeviceInfo", params);
                            }
                        }

                        // the check is not present, thus the test failed
                        catch (Exception e)
                            { /* do nothing */ }
                        break;


                    case "saveFirstRegistration":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            // if the registration was successful,
                            // go back to the splashscreen layout
                            findViewById(R.id.firstRegistrationWindow).setVisibility(View.GONE);
                            findViewById(R.id.splash_screen_window).setVisibility(View.VISIBLE);
                            progressBar.setProgress(0);

                            // save the created user and device info in the database
                            JSONObject jObject = new JSONObject(output).getJSONObject("user");
                            JSONObject jObjectDevice = new JSONObject(output).getJSONObject("deviceInfo");
                            User myUser = User.fromJson(jObject);
                            DeviceInfo dInfo = DeviceInfo.fromJson(jObjectDevice);

                            dbA.insertDeviceInfo(
                                    "Store Name",
                                    dInfo.getRagioneSociale(), dInfo.getPartitaIva(), dInfo.getAddress(),
                                    dInfo.getComune(), dInfo.getProvincia(), dInfo.getCap(),
                                    dInfo.getEmail(), dInfo.getAndroidId(), dInfo.getTokenId(),
                                    dInfo.getIp(), dInfo.getMulticastIp(), dInfo.getMaster(),
                                    dInfo.getOnlineCheck());

                            dbA.insertUser(myUser.getName(), myUser.getSurname(), myUser.getEmail(),
                                           myUser.getPasscode(), 0, myUser.getPasscode());

                            // TODO
                            // handle registration licence codes
                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                            String formattedDate = df.format(c);

                            dbA.execOnDb("INSERT INTO registered_activation_code(code, registration) VALUES('" + licenseString + "', '" + formattedDate + "')");
                            dbA.execOnDb("UPDATE static_activation_code SET used=1 where code='" + licenseString + "'");
                        }

                        else
                        {
                            String reason = jsonObject.getString("reason");
                            Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, reason), Toast.LENGTH_SHORT).show();
                        }


                    case "updateDeviceInfo":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONObject jObjectDevice = new JSONObject(output).getJSONObject("deviceInfo");
                            DeviceInfo dInfo = DeviceInfo.fromJson(jObjectDevice);
                            dbA.execOnDb("delete from device_info");
                            dbA.insertDeviceInfoWithId(dInfo);
                            progressStatus += 20;
                            progressBar.setProgress(progressStatus);

                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params = new ArrayList<NameValuePair>(2);
                            callHttpHandler("/updateButtons", params);
                        }

                        else
                        {
                            String reason = jsonObject.getString("reason");
                            progressStatus += 100;
                            Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, reason), Toast.LENGTH_SHORT).show();
                        }
                        break;


                    case "updateButtons" :
                        check = jsonObject.getBoolean("check");
                        if(check){
                            JSONArray jVats = new JSONObject(output).getJSONArray("vats");
                            JSONArray jButtons= new JSONObject(output).getJSONArray("buttons");

                            ArrayList<Vat> vats = Vat.fromJsonArray(jVats);
                            ArrayList<ButtonLayout> buttons = ButtonLayout.fromJsonArray(jButtons);

                            dbA.execOnDb("delete from button");
                            dbA.execOnDb("delete from vat");
                            dbA.insertVatsSync(vats);
                            dbA.insertButtonsSync(buttons);


                            progressStatus += 20;
                            progressBar.setProgress(progressStatus);
                            progressBar.setProgress(progressStatus);
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params = new ArrayList<NameValuePair>(2);
                            callHttpHandler("/updateModifierButtons",params );
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Errore buttons", Toast.LENGTH_SHORT).show();
                        }
                        break;


                    case "updateModifierButtons" :
                        check = jsonObject.getBoolean("check");
                        if(check){
                            JSONArray jModifierGroups= new JSONObject(output).getJSONArray("modifierGroups");
                            JSONArray jModifiers= new JSONObject(output).getJSONArray("modifiers");

                            ArrayList<ModifiersGroupAdapter.ModifiersGroup> modifierGroups = ModifiersGroupAdapter.ModifiersGroup.fromJsonArray(jModifierGroups);
                            ArrayList<ModifierAdapter.Modifier> modifiers = ModifierAdapter.Modifier.fromJsonArray(jModifiers);

                            dbA.execOnDb("delete from modifiers_group");
                            dbA.execOnDb("delete from modifier");
                            dbA.insertModifierGroupsSync(modifierGroups);
                            dbA.insertModifiersSync(modifiers);

                            progressStatus += 10;
                            progressBar.setProgress(progressStatus);
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params = new ArrayList<NameValuePair>(2);
                            callHttpHandler("/updateModifierAssigned",params );
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Errore buttons", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "updateModifierAssigned" :
                        check = jsonObject.getBoolean("check");
                        if(check){
                            JSONArray jmgA= new JSONObject(output).getJSONArray("modifierGroupAssigned");
                            JSONArray jmA= new JSONObject(output).getJSONArray("modifierAssigned");

                            ArrayList<ModifierGroupAssigned> modifierGroupAssigned = ModifierGroupAssigned.fromJsonArray(jmgA);
                            ArrayList<ModifierAssigned> modifierAssigned = ModifierAssigned.fromJsonArray(jmA);


                            dbA.execOnDb("delete from modifiers_group_assigned");
                            dbA.execOnDb("delete from modifiers_assigned");
                            dbA.insertModifierGroupAssignedSync(modifierGroupAssigned);
                            dbA.insertModifierAssignedSync(modifierAssigned);

                            progressStatus += 10;
                            progressBar.setProgress(progressStatus);
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params = new ArrayList<NameValuePair>(2);
                            callHttpHandler("/updateClients",params );
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Errore buttons", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "updateClients":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONArray jClients = new JSONObject(output).getJSONArray("clients");
                            JSONArray jCompanies = new JSONObject(output).getJSONArray("companies");
                            JSONArray jCics = new JSONObject(output).getJSONArray("cics");
                            JSONArray jFielities = new JSONObject(output).getJSONArray("fidelities");
                            ArrayList<Client> clients = Client.fromJsonArray(jClients);
                            ArrayList<Company> companies = Company.fromJsonArray(jCompanies);
                            ArrayList<ClientInCompany> cics = ClientInCompany.fromJsonArray(jCics);
                            ArrayList<Fidelity> fidelities = Fidelity.fromJsonArray(jFielities);

                            dbA.execOnDb("delete from client");
                            dbA.execOnDb("delete from company");
                            dbA.execOnDb("delete from client_in_company");
                            dbA.execOnDb("delete from fidelity");
                            dbA.insertClientSync(clients);
                            dbA.insertCompanySync(companies);
                            dbA.insertCiCSync(cics);
                            dbA.insertFidelitySync(fidelities);

                            dbA.showData("fidelity");

                            progressStatus += 10;
                            progressBar.setProgress(progressStatus);
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params = new ArrayList<NameValuePair>(2);
                            callHttpHandler("/updateRooms",params );
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "updateRooms":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONArray jRooms = new JSONObject(output).getJSONArray("rooms");
                            JSONArray jTables = new JSONObject(output).getJSONArray("tables");
                            ArrayList<Room> rooms = Room.fromJsonArray(jRooms);
                            ArrayList<Table> tables = Table.fromJsonArray(jTables);

                            dbA.execOnDb("delete from room");
                            dbA.execOnDb("delete from table_configuration");
                            dbA.insertRoomSync(rooms);
                            dbA.insertTableSync(tables);
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params = new ArrayList<NameValuePair>(2);
                            callHttpHandler("/updateUsers",params );
                            progressStatus += 10;
                            progressBar.setProgress(progressStatus);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "updateUsers":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            JSONArray usersObject = new JSONObject(output).getJSONArray("users");
                            ArrayList<User> userList = User.fromJsonArray(usersObject);
                            dbA.execOnDb("delete from user");
                            dbA.updateUserList(userList);
                            progressStatus += 10;
                            progressBar.setProgress(progressStatus);
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params = new ArrayList<NameValuePair>(2);
                            callHttpHandler("/updatePrinters",params );
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "updatePrinters":
                        check = jsonObject.getBoolean("check");
                        if (check) {
                            boolean fiscalExist = jsonObject.getBoolean("fiscalExist");
                            dbA.execOnDb("delete from fiscal_printer");
                            dbA.execOnDb("delete from kitchen_printer");
                            if (fiscalExist) {
                                JSONObject fiscalPrinter = new JSONObject(output).getJSONObject("fiscalPrinter");
                                FiscalPrinter fPrinter = FiscalPrinter.fromJson(fiscalPrinter);
                                dbA.insertFiscalPrinterSync(fPrinter);
                            }
                            JSONArray kitchenPrinter = new JSONObject(output).getJSONArray("kitchenPrinter");
                            ArrayList<KitchenPrinter> kPrinters = KitchenPrinter.fromJsonArray(kitchenPrinter);
                            dbA.insertKitchenPrinterSync(kPrinters);
                            progressStatus += 10;
                            progressBar.setProgress(progressStatus);

                            // this was the last call to the server
                            // move to the login activity
                            Intent i = new Intent(SplashScreen1.this, Login.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        progressStatus = 100;
                        break;
                }

                /* OLD TODO
                if (progressStatus >= 100) {
                    if (status == null) {
                        Intent i = new Intent(SplashScreen1.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    } else if (status.equals("pinpad")) {
                        Intent i = new Intent(SplashScreen1.this, Operative.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("isAdmin", userType);
                        i.putExtra("username", username);
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(SplashScreen1.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                }
                 */
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else
        {
            if (progressStatus!=100)
            {
                Toast.makeText(this, "NO BLACKBOX", Toast.LENGTH_SHORT).show();
                StaticValue.blackbox = false;
                progressStatus = 100;
            }
        }
    }


    // ----- POPUP ------ //

    /**
     * Open the popup that allow the user to insert the hostname
     * and address of a new blackbox
     * */
    private void openPopupBlackboxIP(int lastId)
    {
        LayoutInflater inflater = this.getLayoutInflater();

        // create the popup, which can't be cancelled until a valid ip address is given
        Dialog dialog = new Dialog(SplashScreen1.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(inflater.inflate(R.layout.popup_setup_blackbox, null));

        EditText blackboxIpEditText = dialog.findViewById(R.id.editText_blackbox_ip);
        EditText blackboxNameEditText = dialog.findViewById(R.id.editText_blackbox_name);
        CustomButton okButton = dialog.findViewById(R.id.ok_button_blackbox_ip);
        CustomButton refresh = dialog.findViewById(R.id.refresh_button_blackbox_ip);

        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String ipText = blackboxIpEditText.getText().toString().trim();
                String nameText = blackboxNameEditText.getText().toString().trim();

                // check first if the given name and address are correctly formatted
                if (!Pattern.matches("[A-Za-z]+[A-Za-z0-9_\\-\\.]", nameText))
                {
                    Toast.makeText(SplashScreen1.this,
                            "Malformed name of blackbox. Names must start with a letter and accept only [A-Z0-9_-.]",
                            Toast.LENGTH_LONG).show();
                }

                // if the pattern of the address is of an accetable format
                else if (Pattern.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{2,4})?$", ipText))
                {
                    // create a new blackbox and test it
                    BlackboxInfo b = new BlackboxInfo();

                    b.setId(lastId + 1);
                    b.setName(nameText);
                    b.setAddress(ipText);

                    // if the test of blackbox communication went fine,
                    // we have found a new blackbox
                    if (testBlackboxes(Collections.singletonList(b)) != null)
                    {
                        // save the new blackbox to the internal database
                        dbA.insertBlackboxSync(b);
                        dbA.execOnDb("ALTER TABLE device_info ADD COLUMN store_name TEXT DEFAULT NULL;");

                        Toast.makeText(SplashScreen1.this, "Connecting to blackbox "+nameText + "@" + ipText, Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                    }

                    else
                        { Toast.makeText(SplashScreen1.this, getString(R.string.error_blackbox_comm, nameText, ipText), Toast.LENGTH_LONG).show(); }
                }

                else
                    { Toast.makeText(SplashScreen1.this, "IP address is malformed", Toast.LENGTH_LONG).show(); }
            }
        });


        refresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                BlackboxInfo bb = testBlackboxes(dbA.selectAllBlackbox());
                if (bb == null)
                {
                    Toast.makeText(SplashScreen1.this, "No blackbox found on refresh", Toast.LENGTH_LONG).show();
                    dialog.show();
                }

                else
                {
                    Toast.makeText(SplashScreen1.this, "Connecting to blackbox " + bb.getName() + "@" + bb.getAddress(), Toast.LENGTH_LONG).show();
                    new StaticValue(getApplicationContext(), bb);
                }
            }
        });

        dialog.show();

    }


    public void callHttpHandler(String route,  List<NameValuePair> params ){
        HttpHandler httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }


    private void setupRegistrationPopupLayout()
    {
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;/// density;
        dpWidth  = outMetrics.widthPixels;/// density;

        findViewById(R.id.splash_screen_window).setVisibility(View.GONE);
        findViewById(R.id.firstRegistrationWindow).setVisibility(View.VISIBLE);

        CustomEditText AdminPasscode = findViewById(R.id.first_admin_passcode_et);
        AdminPasscode.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });

        CustomButton licenseButton = findViewById(R.id.licensing);

        licenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                licenseString = fireSingleInputDialogPopup();
            }
        });


        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, R.string.please_fill_first_registration_form, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFirstRegistration();
            }});
    }


    private void saveFirstRegistration()
    {
        //final CustomEditText StoreName = (CustomEditText)findViewById(R.id.store_name_et);
        CustomEditText  Address= findViewById(R.id.address_et);
        CustomEditText  StoreEmail = findViewById(R.id.first_r_email_et);
        CustomEditText  RagioneSociale = findViewById(R.id.store_name_et);
        CustomEditText  PartitaIva = findViewById(R.id.store_IVA);
        CustomEditText  AdminName = findViewById(R.id.first_admin_name_et);
        CustomEditText  AdminSurname = findViewById(R.id.first_admin_surname_et);
        CustomEditText  AdminEmail = findViewById(R.id.first_admin_email_et);
        CustomEditText  AdminPasscode = findViewById(R.id.first_admin_passcode_et);
        CustomEditText  Cap = findViewById(R.id.cap_et);
        CustomEditText  Comune = findViewById(R.id.comune_et);
        CustomEditText  Provincia = findViewById(R.id.provincia_et);

        String ragioneSociale = RagioneSociale.getText().toString().trim();
        String partitaIva= PartitaIva.getText().toString().trim();
        String address = Address.getText().toString().trim();
        String storeEmail = StoreEmail.getText().toString().trim();
        String adminName = AdminName.getText().toString().trim();
        String adminSurname = AdminSurname.getText().toString().trim();
        String adminEmail = AdminEmail.getText().toString().trim();
        String adminPasscode = AdminPasscode.getText().toString().trim();
        String cap= Cap.getText().toString().trim();
        String comune= Comune.getText().toString().trim();
        String provincia= Provincia.getText().toString().trim();


        // if any of the fields is not correctly formatted
        if (ragioneSociale.equals(""))
            { Toast.makeText(context, R.string.please_insert_your_store_name, Toast.LENGTH_SHORT).show(); }
        else if (!storeEmail.equals("") && !Pattern.matches("^[-a-zA-Z0-9._%]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", storeEmail))
            { Toast.makeText(context, R.string.store_email_is_not_well_formed, Toast.LENGTH_SHORT).show(); }
        else if(!adminEmail.equals("") && !Pattern.matches("^[-a-zA-Z0-9._%]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", adminEmail))
            { Toast.makeText(context, R.string.admin_email_is_not_well_formed, Toast.LENGTH_SHORT).show(); }
        else if(adminName.equals("") || adminSurname.equals("") || adminPasscode.equals(""))
            { Toast.makeText(context, R.string.fill_admin_info_please, Toast.LENGTH_SHORT).show(); }
        else if(licenseString.equals("") )
            { Toast.makeText(context, R.string.fill_license_string_please, Toast.LENGTH_SHORT).show(); }
        /*else if(!dbA.checkIfStaticCodeIsUsed(licenseString)){
            Toast.makeText(context, R.string.license_code_already_used, Toast.LENGTH_SHORT).show();}*/

        // if all the fields have a correct output, send the registration form to the blackbox
        else
        {
            String android_id = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
            LocalNotification notificationService = LocalNotification.getInstance();
            String myIp = notificationService.myIp;

            if (StaticValue.blackbox)
            {
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("ragioneSociale", ragioneSociale));
                params.add(new BasicNameValuePair("partitaIva", partitaIva));
                params.add(new BasicNameValuePair("provincia", provincia));
                params.add(new BasicNameValuePair("comune", comune));
                params.add(new BasicNameValuePair("cap", cap));
                params.add(new BasicNameValuePair("address", address));
                params.add(new BasicNameValuePair("email", storeEmail));
                params.add(new BasicNameValuePair("androidId", android_id));
                params.add(new BasicNameValuePair("ip", myIp));
                params.add(new BasicNameValuePair("tokenId", StaticValue.myTag));
                params.add(new BasicNameValuePair("multicastIp", StaticValue.multicastGroup));
                params.add(new BasicNameValuePair("master", String.valueOf(StaticValue.master ? 1 : 0)));
                params.add(new BasicNameValuePair("name", adminName));
                params.add(new BasicNameValuePair("surname", adminSurname));
                params.add(new BasicNameValuePair("adminEmail", adminEmail));
                params.add(new BasicNameValuePair("password", adminPasscode));
                params.add(new BasicNameValuePair("userType", String.valueOf(0)));
                // params.add(new BasicNameValuePair("passcode", adminPasscode));
                params.add(new BasicNameValuePair("code", licenseString));

                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(c);
                params.add(new BasicNameValuePair("registration", formattedDate));

                callHttpHandler("/saveFirstRegistration", params);
            }

            else
            {
                dbA.insertDeviceInfo(ragioneSociale,partitaIva,  address, provincia, comune, cap,  storeEmail, android_id);
                dbA.insertUser(adminName, adminSurname, adminEmail, adminPasscode,0, adminPasscode);
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(c);
                dbA.execOnDb("INSERT INTO registered_activation_code(code, registration) VALUES('"+licenseString+"', '"+formattedDate+"')");
                dbA.execOnDb("UPDATE static_activation_code SET used=1 where code='"+licenseString+"'");
            }
        }
    }

    // show the popup to input the licence code
    public String fireSingleInputDialogPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.single_input_dialog, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        popupView.post(new Runnable() {
            @Override
            public void run() {}
        });

        CustomEditText license_input = popupView.findViewById(R.id.single_input);
        license_input.setHint(R.string.insert_license_code);

        CustomButton licenseButton = findViewById(R.id.licensing);

        ImageButton okButton = popupView.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!license_input.getText().toString().trim().equals("")){
                    licenseString = license_input.getText().toString();
                    license_input.setText("");
                    // TODO add a verification on the license code
                    licenseButton.setText(licenseString);

                    popupWindow.dismiss();
                }
                else{
                    license_input.setText("");
                    Toast.makeText(context, R.string.insert_a_valid_license_key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton killButton = popupView.findViewById(R.id.kill);
        killButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

        return licenseString;
    }


    /**
     * Test if a list of blackbox are active or not
     * @param allbb: the input list of blackbox to test
     *
     * @return the first blackbox that is able to communicate successfully with this app
     * */
    public BlackboxInfo testBlackboxes(List<BlackboxInfo> allbb)
    {
        String androidId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        for (BlackboxInfo bb : allbb)
        {
            // create a new temporary http connection
            HttpHandler httpHandler = new HttpHandler();
            httpHandler.testIp = bb.getAddress();
            httpHandler.delegate = this;

            // run the /testBlackboxComm POST request
            // also test if the blackbox already know this device, and thus if the device
            // must be registered or not
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("androidId", androidId));

            httpHandler.UpdateInfoAsyncTask("/testBlackboxComm", params);

            try
            {
                // get the result of the communication
                String res = httpHandler.execute().get();
                JSONObject jsonObject = new JSONObject(res);

                // if the connection was successful,
                // the success value will be true
                if (jsonObject.getBoolean("success"))
                {
                    new StaticValue(getApplicationContext(), bb);
                    return bb;
                }
            }

            // should never happen
            catch (Exception e)
                { e.printStackTrace(); }
        }

        return null;
    }


    public String getIpAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

}
