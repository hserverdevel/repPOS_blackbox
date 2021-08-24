package com.example.blackbox.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.DialogCreator;
import com.example.blackbox.R;
import com.example.blackbox.adapter.ClientsAdapter;
import com.example.blackbox.adapter.DiscountAdapter;
import com.example.blackbox.adapter.FidelityPackageAdapter;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Client;
import com.example.blackbox.model.ClientInCompany;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Company;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.Fidelity;
import com.example.blackbox.model.FidelityPackage;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TemporaryOrder;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.google.gson.Gson;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by DavideLiberato on 14/07/2017.
 */

public class ClientsActivity extends AppCompatActivity implements HttpHandler.AsyncResponse
{

    public static final  int                    MODIFY_MODE            = 0;
    public static final  int                    SELECTION_MODE         = 1;
    public static final  int                    COSTUMER_MODE          = 2;
    public static final  int                    EMAIL_SELECTION_MODE   = 3;
    public static final  int                    INVOICE_MODE           = 4;
    public static final  int                    NEW_EMAIL_MODE         = 5;
    public static final  int                    SEARCH_CLIENT_MODE     = 6;
    private static final String                 TAG                    = "<ClientsActivity>";
    public               boolean                company                = false;
    private              ClientsAdapter         clientsAdapter;
    private              DatabaseAdapter        dbA;
    private              Context                context;
    private              CustomButton           addCompanyInfo;
    private              CustomButton           addPersonalInfo;
    private              CustomButton           addNewClient;
    private              CustomButton           searchClients;
    private              CustomButton           setDiscount;
    private              Animation              shake;
    private              CustomButton           setClientType;
    private              TextView               add_fidelity_credit_bt;
    private              TextView               fidelity_code_text;
    private              TextView               show_fidelity_credit;
    private              CustomEditText         name_et;
    private              CustomEditText         surname_et;
    private              CustomEditText         email_et;
    private              CustomEditText         company_name_et;
    private              CustomEditText         vat_number_et;
    private              CustomEditText         address_et;
    private              CustomEditText         postal_code_et;
    private              CustomEditText         country_et;
    private              CustomEditText         city_et;
    private              CustomEditText         codice_fiscale_et;
    private              CustomEditText         provincia_et;
    private              CustomEditText         codice_destinatario_et;
    private              CustomEditText         pec_et;
    private              CustomEditText         vat_number_et_p;
    private              CustomEditText         address_et_p;
    private              CustomEditText         postal_code_et_p;
    private              CustomEditText         country_et_p;
    private              CustomEditText         city_et_p;
    private              CustomEditText         codice_fiscale_et_p;
    private              CustomEditText         provincia_et_p;
    private              CustomEditText         codice_destinatario_et_p;
    private              CustomEditText         pec_et_p;
    // the difference of this two variables is:
    // currentClient: indicate the client that is passed from the Intent,
    //                and it's used usually to denote a client selected for other purposes
    // fidelitySelectedClient: used maynly by the fidelity credit handling, it referes to the client that is
    //                 selected by the user on the client list, while this activity is being used
    private              ClientInfo             currentClient          = null;
    private              ClientInfo             fidelitySelectedClient = null;
    private              DiscountAdapter        discountAdapter;
    private              FidelityPackageAdapter fidelityPackageAdapter;
    private              int                    billId;
    private              int                    tableNumber;
    private              int                    orderNumber;
    private              int                    mode                   = MODIFY_MODE;
    private              boolean                newMode                = false;
    private              boolean                keyboard_next_flag     = false;
    private              boolean                paid                   = false;
    private              boolean                clientLongClick        = false;
    private              int                    currentClientId        = -1;
    private              int                    userType;
    private              int                    userId;
    private              String                 username;

    public CustomButton getDiscountButton()
    {
        return setDiscount;
    }

    public CustomButton getClientButton()
    {
        return setClientType;
    }

    public int getCurrentClientId()
    {
        return currentClientId;
    }

    public ClientInfo getCurrentClient()
    {
        return currentClient;
    }

    public void setCurrentClient(ClientInfo currentClient)
    {
        this.currentClient = currentClient;
    }

    public int getMode()
    {
        return mode;
    }

    public void setMode(int mode)
    {
        this.mode = mode;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Hides app title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_clients);
        context = this;
        ClientsActivity myself = this;

        dbA = new DatabaseAdapter(this);

        shake = AnimationUtils.loadAnimation(this, R.anim.shake);

        Intent intent = getIntent();
        billId   = intent.getIntExtra("billId", -1);
        userId   = intent.getIntExtra("userId", -1);
        username = intent.getStringExtra("username");
        userType = intent.getIntExtra("userType", -1);
        boolean modifyCustomer = intent.getBooleanExtra("customerModify", false);
        tableNumber     = intent.getIntExtra("tableNumber", -1);
        orderNumber     = intent.getIntExtra("orderNumber", -1);
        clientLongClick = intent.getBooleanExtra("clientLongClick", false);
        currentClientId = intent.getIntExtra("currentCustomer", -1);

        resetPinpadTimer(1);
        int billIdToAdapter = -1;
        //check the action in intent to know from where clients activity is called
        switch (intent.getAction())
        {
            //from payment activity
            case "modifyCustomer":
                mode = SELECTION_MODE;
                newMode = true;
                break;
            case "selectClient":
                mode = SELECTION_MODE;
                break;
            //from operative activity
            case "clientsFromOperative":
                mode = SELECTION_MODE;
                break;
            //from operative activity popup to select customer
            case "selectCustomer":
                mode = COSTUMER_MODE;
                billIdToAdapter = billId;
                break;
            case "selectEmail":
                mode = EMAIL_SELECTION_MODE;
                break;
            case "invoice":
                mode = INVOICE_MODE;
                break;
            case "newEmail":
                mode = NEW_EMAIL_MODE;
                break;
            default:
                mode = SELECTION_MODE;
                break;
        }
        //mode = (intent.getAction().equals("selectClient")? SELECTION_MODE : MODIFY_RESERVATION_MODE);


        name_et         = (CustomEditText) findViewById(R.id.name_et);
        surname_et      = (CustomEditText) findViewById(R.id.surname_et);
        email_et        = (CustomEditText) findViewById(R.id.email_et);
        company_name_et = (CustomEditText) findViewById(R.id.company_name_et);
        vat_number_et   = (CustomEditText) findViewById(R.id.vat_number_et);
        address_et      = (CustomEditText) findViewById(R.id.address_et);
        postal_code_et  = (CustomEditText) findViewById(R.id.postal_code_et);
        country_et      = (CustomEditText) findViewById(R.id.country_et);
        city_et         = (CustomEditText) findViewById(R.id.city_et);
        //codice_fiscale_et = (CustomEditText)findViewById(R.id.codice_fiscale_et);
        provincia_et           = (CustomEditText) findViewById(R.id.provincia_et);
        codice_destinatario_et = (CustomEditText) findViewById(R.id.codice_destinatario_et);
        pec_et                 = (CustomEditText) findViewById(R.id.pec_et);
        fidelity_code_text     = findViewById(R.id.fidelity_code);

        // the button to buy fidelity package. It can be used only after a client has been selected
        // (in `openModifyMode`)
        add_fidelity_credit_bt = findViewById(R.id.add_fidelity_credit_button);
        add_fidelity_credit_bt.setEnabled(false);

        show_fidelity_credit = findViewById(R.id.fidelity_credit_amount);


        vat_number_et_p          = (CustomEditText) findViewById(R.id.vat_number_et_p);
        address_et_p             = (CustomEditText) findViewById(R.id.address_et_p);
        postal_code_et_p         = (CustomEditText) findViewById(R.id.postal_code_et_p);
        country_et_p             = (CustomEditText) findViewById(R.id.country_et_p);
        city_et_p                = (CustomEditText) findViewById(R.id.city_et_p);
        codice_fiscale_et_p      = (CustomEditText) findViewById(R.id.codice_fiscale_et_p);
        provincia_et_p           = (CustomEditText) findViewById(R.id.provincia_et_p);
        codice_destinatario_et_p = (CustomEditText) findViewById(R.id.codice_destinatario_et_p);
        pec_et_p                 = (CustomEditText) findViewById(R.id.pec_et_p);

        InputFilter[] input = new InputFilter[2];

        input[0] = new InputFilter.LengthFilter(16);
        input[1] = new InputFilter.AllCaps();
        codice_fiscale_et_p.setFilters(input);

        InputFilter[] input1 = new InputFilter[2];
        input1[0] = new InputFilter.LengthFilter(2);
        input1[1] = new InputFilter.AllCaps();
        provincia_et.setFilters(input1);
        provincia_et_p.setFilters(input1);

        // set up the adapter and the recycler for the client list
        clientsAdapter = new ClientsAdapter(dbA, this, mode, billIdToAdapter, clientLongClick);

        RecyclerView clients_list_rv = (RecyclerView) findViewById(R.id.clients_rv);
        clients_list_rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        clients_list_rv.setAdapter(clientsAdapter);
        clients_list_rv.addItemDecoration(new RecyclerView.ItemDecoration()
        {
            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state)
            {
                ClientInfo c = (ClientInfo) view.getTag();
                //parent.getChildAdapterPosition(view) == 3
                outRect.set(0, 0, 0, 1);
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state)
            {
                //super.onDraw(c, parent, state);
                Drawable  divider    = ClientsActivity.this.getDrawable(R.drawable.divider_line_horizontal1dp);
                final int size       = divider.getIntrinsicHeight();
                int       left       = parent.getLeft() + parent.getPaddingLeft();
                int       right      = parent.getRight() - parent.getPaddingRight();
                int       top        = 0;
                int       bottom     = 0;
                int       childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++)
                {
                    final View child = parent.getChildAt(i);
                    if (i != childCount)
                    {
                        top    = child.getBottom();
                        bottom = top + 1;
                        divider.setBounds(left, top, right, bottom);
                        divider.draw(c);
                    }
                }
            }
        });

        if (clientsAdapter.getClientsSize() != 0)
        {
            ((View) findViewById(R.id.hline_rv_top)).setVisibility(View.VISIBLE);
        }
        else
        {
            ((View) findViewById(R.id.hline_rv_top)).setVisibility(View.GONE);
        }


        setupButtons();
        setupXOK();

        RequestParam numOrderParams = new RequestParam();
        callHttpHandler("/getLastBillNumber", numOrderParams);


        if (mode == MODIFY_MODE && newMode)
        {
            if (StaticValue.blackbox)
            {
                RequestParam params = new RequestParam();
                params.add("currentClientId", String.valueOf(currentClientId));
                callHttpHandler("fetchSingleClient", params);
            }

            else
            {
                currentClient = dbA.fetchSingleClient(currentClientId);
                openModifyMode(currentClient);
                newMode = false;
            }
        }

        //activityCommunicator = (ActivityCommunicator) this.context;
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
            route      = jsonObject.getString("route");
            success    = jsonObject.getBoolean("success");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (success)
        {
            boolean check;
            try
            {
                jsonObject = new JSONObject(output);

                switch (route)
                {
                    case "fetchClients":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            if (!jsonObject.getBoolean("updated"))
                            {
                                JSONArray             clientsList = new JSONObject(output).getJSONArray("clients");
                                ArrayList<ClientInfo> prova       = ClientInfo.fromJsonArray(clientsList);
                                clientsAdapter.updateClientsList(ClientInfo.fromJsonArray(clientsList));

                                dbA.updateChecksumForTable("client", jsonObject.getString("clientChecksum"));
                            }

                            else
                            {
                                clientsAdapter.setClientsDefault();
                            }
                        }

                        else
                        {
                            clientsAdapter.updateClientsList(null);
                        }
                        break;

                    case "fetchExclusiveClients":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            if (!jsonObject.getBoolean("updated"))
                            {
                                JSONArray             clientsList = new JSONObject(output).getJSONArray("clients");
                                ArrayList<ClientInfo> prova       = ClientInfo.fromJsonArray(clientsList);
                                clientsAdapter.updateClientsList(ClientInfo.fromJsonArray(clientsList));

                                dbA.updateChecksumForTable("client", jsonObject.getString("clientChecksum"));
                            }

                            else
                            {
                                clientsAdapter.setExclusiveClientsDefault();
                            }
                        }

                        else
                        {
                            clientsAdapter.updateClientsList(null);
                        }
                        break;

                    case "updateClient":
                        addNewClient.setEnabled(true);
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("client");
                            currentClient = ClientInfo.fromJson(jObject);
                            dbA.updateClientData(currentClient);
                            dbA.showData("client");
                            if (!clientsAdapter.searchMode)
                            {
                                clientsAdapter.updateDataSet();
                            }
                            closeModifyMode();
                        }

                        else
                        {
                            Toast.makeText(context, getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_LONG).show();
                        }

                        break;

                    case "updateClientWithCompany":
                        addNewClient.setEnabled(true);
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("client");
                            currentClient = ClientInfo.fromJson(jObject);
                            dbA.updateClientData(currentClient);
                            if (!clientsAdapter.searchMode)
                            {
                                clientsAdapter.updateDataSet();
                            }
                            closeModifyMode();
                        }

                        else
                        {
                            Toast.makeText(context, getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_LONG).show();
                        }

                        break;

                    case "fetchSingleClient":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("client");
                            currentClient = ClientInfo.fromJson(jObject);
                            //selectedClient = dbA.fetchSingleClient(currentClientId);
                            openModifyMode(currentClient);
                            newMode = false;

                        }

                        else
                        {
                            Toast.makeText(context, getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_LONG).show();
                        }

                        break;

                    case "insertClient":
                        addNewClient.setEnabled(true);
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            // get the information of the client, that has been added to the blackbox
                            // annd add them to the internal database
                            JSONObject jObject   = new JSONObject(output).getJSONObject("client");
                            JSONObject jObject1  = new JSONObject(output).getJSONObject("cic");
                            JSONObject jFidelity = new JSONObject(output).getJSONObject("fidelity");

                            Client          client = Client.fromJson(jObject);
                            ClientInCompany cic    = ClientInCompany.fromJson(jObject1);
                            Fidelity        f      = Fidelity.fromJson(jFidelity);

                            dbA.insertClientFromServer(client);
                            dbA.insertCiCFromServer(cic);
                            dbA.insertFidelitySync(new ArrayList<Fidelity>()
                            {{
                                add(f);
                            }});

                            clientsAdapter.updateDataSet();
                            name_et.setText("");
                            surname_et.setText("");
                            email_et.setText("");
                            fidelity_code_text.setText("");
                        }

                        else
                        {
                            Toast.makeText(context, getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_LONG).show();
                        }

                        break;

                    case "insertClientWithCompany":
                        addNewClient.setEnabled(true);
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject   = new JSONObject(output).getJSONObject("client");
                            JSONObject jObject1  = new JSONObject(output).getJSONObject("cic");
                            JSONObject jObject2  = new JSONObject(output).getJSONObject("company");
                            JSONObject jFidelity = new JSONObject(output).getJSONObject("fidelity");


                            Client          client  = Client.fromJson(jObject);
                            ClientInCompany cic     = ClientInCompany.fromJson(jObject1);
                            Company         company = Company.fromJson(jObject2);
                            Fidelity        f       = Fidelity.fromJson(jFidelity);

                            dbA.insertClientFromServer(client);
                            dbA.insertCiCFromServer(cic);
                            dbA.insertCompanyFromServer(company);

                            ArrayList<Fidelity> fidelityList = new ArrayList<>();
                            fidelityList.add(f);
                            dbA.insertFidelitySync(fidelityList);


                            if (!clientsAdapter.searchMode)
                            {
                                clientsAdapter.updateDataSet();
                            }
                            //reset dataFields
                            name_et         = (CustomEditText) findViewById(R.id.name_et);
                            surname_et      = (CustomEditText) findViewById(R.id.surname_et);
                            email_et        = (CustomEditText) findViewById(R.id.email_et);
                            company_name_et = (CustomEditText) findViewById(R.id.company_name_et);
                            vat_number_et   = (CustomEditText) findViewById(R.id.vat_number_et);
                            address_et      = (CustomEditText) findViewById(R.id.address_et);
                            postal_code_et  = (CustomEditText) findViewById(R.id.postal_code_et);
                            country_et      = (CustomEditText) findViewById(R.id.country_et);
                            city_et         = (CustomEditText) findViewById(R.id.city_et);
                            //codice_fiscale_et = (CustomEditText) findViewById(R.id.codice_fiscale_et);
                            provincia_et           = (CustomEditText) findViewById(R.id.provincia_et);
                            codice_destinatario_et = (CustomEditText) findViewById(R.id.codice_destinatario_et);
                            pec_et                 = (CustomEditText) findViewById(R.id.pec_et);
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
                            fidelity_code_text.setText("");
                            addCompanyInfo = (CustomButton) findViewById(R.id.add_company_info_button);
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
                            {
                                addPersonalInfo.performClick();
                            }
                        }

                        else
                        {
                            Toast.makeText(context, getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_LONG).show();
                        }

                        break;

                    case "deleteClient":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int clientId  = jsonObject.getInt("clientId");
                            int companyId = jsonObject.getInt("companyId");
                            dbA.deleteClient(clientId, companyId);
                            clientsAdapter.deleteClientFromServer();
                        }

                        else
                        {
                            Toast.makeText(context, getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_LONG).show();
                        }

                        break;


                    // used to add a new button under the special category (-5),
                    // reserved for the fidelity credit package products
                    case "insertButton5":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            // if all went correct, add the button to the database
                            dbA.insertButtonFromServer(ButtonLayout.fromJson(new JSONObject(output).getJSONObject("button")));

                            // and also update the fidelityPackageAdapter visual state
                            fidelityPackageAdapter.refreshFidelityPackageList();
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_SHORT).show();
                        }
                        break;


                    // this case is activated when a fidelity package is selected
                    case "saveBill":
                        if (jsonObject.getBoolean("check"))
                        {
                            // this intent is used when moving from this activity
                            // to the payment activity, in order to buy fidelity credits
                            Intent buyFidelityIntent = new Intent(ClientsActivity.this, PaymentActivity.class);

                            // the payment activity will need the billId of the purchasing of the fidelity package,
                            // and the clientId, from which the fidelity Id will be extracted
                            buyFidelityIntent.putExtra("billId", jsonObject.getInt("billId"));
                            buyFidelityIntent.putExtra("fidelityClientId", fidelitySelectedClient.getClient_id());
                            buyFidelityIntent.putExtra("username", username);
                            buyFidelityIntent.putExtra("orderNumber", orderNumber);

                            startActivity(buyFidelityIntent);
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_SHORT).show();
                        }

                        break;

                    // used to get the latest bill number,
                    // to send it to payment activity when a fidelity package is bought
                    case "getLastBillNumber":
                        if (jsonObject.getBoolean("check"))
                        {
                            orderNumber = jsonObject.getInt("numberOrder");
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, jsonObject
                                    .getString("reason")), Toast.LENGTH_SHORT).show();
                        }

                        break;


                    default:
                        break;
                }
            }
            catch (JSONException e)
            {
                Log.d("Error", e.getLocalizedMessage());
            }
        }

        else
        {
            Toast.makeText(this,
                           getString(R.string.error_blackbox_comm, StaticValue.blackboxInfo.getName(), StaticValue.blackboxInfo
                                   .getAddress()),
                           Toast.LENGTH_LONG
            ).show();
        }
    }


    public void clientPopup(String title, String hint, int value)
    {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = context.getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels;// / density;
        float dpWidth  = outMetrics.widthPixels;// / density;

        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_client_type, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) popupView.findViewById(R.id.popupWindow)
                                                                                 .getLayoutParams();
        /** 52 => footer height ; 31 => popupwindow height/2 **/
        int t = (int) ((int) (dpHeight - 52) / 2 - 175 * density);
        rll.setMargins(0, t, 0, 0);
        popupView.findViewById(R.id.popupWindow).setLayoutParams(rll);

        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((CustomTextView) popupView.findViewById(R.id.clientType_tv)).setText(title);
        ((CustomEditText) popupView.findViewById(R.id.client_name_type_et)).setHint(hint);

        setupDismissKeyboard(popupView);

        if (currentClient != null)
        {
            if (title.equals("Discount Type"))
            {
                discountAdapter = new DiscountAdapter(dbA, context, popupView, popupWindow, 0, currentClient);
            }
            else if (title.equals("Client Type"))
            {
                discountAdapter = new DiscountAdapter(dbA, context, popupView, popupWindow, 1, currentClient);
            }
        }
        else
        {
            if (title.equals("Discount Type"))
            {
                discountAdapter = new DiscountAdapter(dbA, context, popupView, popupWindow, 0);
            }
            else if (title.equals("Client Type"))
            {
                discountAdapter = new DiscountAdapter(dbA, context, popupView, popupWindow, 1);
            }
        }

        RecyclerView discount_recycler = (RecyclerView) popupView.findViewById(R.id.discount_value_recycler);
        discount_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayout.VERTICAL, false));
        discount_recycler.setAdapter(discountAdapter);
        discount_recycler.setHasFixedSize(true);

        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
        discount_recycler.addItemDecoration(divider);

        if (value != 0)
        {
            String name = dbA.getDiscountFromValueAndClient(currentClient.getClient_id(), value);
            ((CustomEditText) popupView.findViewById(R.id.client_name_type_et)).setText(
                    discountAdapter.getCapString(name));
            ((CustomEditText) popupView.findViewById(R.id.set_discount_et)).setText(value + "");
            discountAdapter.setSelectedDiscount(name);
            discountAdapter.setSelectedValue(value);
            discountAdapter.setModify(true);
        }

        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                discountAdapter.okBehaviour();
                ////Toast.makeText(context, "Nice click!", //Toast.LENGTH_SHORT).show();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((ClientsActivity) context).findViewById(R.id.main), 0, 0, 0);
    }


    // ------ SETUP ------ //


    public void setupButtons()
    {
        addCompanyInfo  = (CustomButton) findViewById(R.id.add_company_info_button);
        addPersonalInfo = (CustomButton) findViewById(R.id.add_personal_info_button);
        addNewClient    = (CustomButton) findViewById(R.id.add_new_client_button);
        searchClients   = (CustomButton) findViewById(R.id.search_client_button);
        setDiscount     = (CustomButton) findViewById(R.id.discount_button);
        setClientType   = (CustomButton) findViewById(R.id.client_type_button);

        addCompanyInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!paid)
                {
                    v.setActivated(!v.isActivated());
                    if (v.isActivated())
                    {
                        company = true;

                        findViewById(R.id.company_info_scroll).setVisibility(View.VISIBLE);
                        findViewById(R.id.company_info_container).setVisibility(View.VISIBLE);

                        findViewById(R.id.personal_info_scroll).setVisibility(View.GONE);
                        findViewById(R.id.personal_info_container).setVisibility(View.GONE);
                        addPersonalInfo.setActivated(false);
                    }
                    else
                    {
                        company = false;

                        findViewById(R.id.company_info_container).setVisibility(View.GONE);
                        findViewById(R.id.company_info_scroll).setVisibility(View.GONE);

                        findViewById(R.id.personal_info_scroll).setVisibility(View.GONE);
                        findViewById(R.id.personal_info_container).setVisibility(View.GONE);
                        addPersonalInfo.setActivated(false);
                    }
                }
                else
                {
                }
                ////Toast.makeText(ClientsActivity.this, "You can't modify.", //Toast.LENGTH_SHORT).show();
            }
        });

        addPersonalInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!paid)
                {
                    v.setActivated(!v.isActivated());
                    if (v.isActivated())
                    {
                        company = false;

                        findViewById(R.id.company_info_scroll).setVisibility(View.GONE);
                        findViewById(R.id.company_info_container).setVisibility(View.GONE);

                        findViewById(R.id.personal_info_scroll).setVisibility(View.VISIBLE);
                        findViewById(R.id.personal_info_container).setVisibility(View.VISIBLE);

                        addCompanyInfo.setActivated(false);
                    }
                    else
                    {
                        company = false;

                        findViewById(R.id.company_info_container).setVisibility(View.GONE);
                        findViewById(R.id.company_info_scroll).setVisibility(View.GONE);

                        findViewById(R.id.personal_info_scroll).setVisibility(View.GONE);
                        findViewById(R.id.personal_info_container).setVisibility(View.GONE);

                        addCompanyInfo.setActivated(false);
                    }
                }
                else
                {
                }
                ////Toast.makeText(ClientsActivity.this, "You can't modify.", //Toast.LENGTH_SHORT).show();
            }
        });

        addNewClient.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = ((CustomEditText) findViewById(R.id.name_et)).getText()
                                                                           .toString()
                                                                           .trim()
                                                                           .replaceAll("'", "\'");
                String surname = ((CustomEditText) findViewById(R.id.surname_et)).getText()
                                                                                 .toString()
                                                                                 .trim()
                                                                                 .replaceAll("'", "\'");
                String email = ((CustomEditText) findViewById(R.id.email_et)).getText()
                                                                             .toString()
                                                                             .trim()
                                                                             .replaceAll("'", "\'");

                // if the addCompanyInfo button is active then  there's the need to satisfy two more conditions(company name and vat_number)
                // in order to add(or update) a client.
                if (addCompanyInfo.isActivated())
                {
                    String company_name = ((CustomEditText) findViewById(R.id.company_name_et)).getText()
                                                                                               .toString()
                                                                                               .replaceAll("'", "\'");
                    company_name = company_name.replaceAll("'", "''");
                    String vat_number = ((CustomEditText) findViewById(R.id.vat_number_et)).getText()
                                                                                           .toString()
                                                                                           .replaceAll("'", "\'");
                    String address = (((CustomEditText) findViewById(R.id.address_et)).getText()
                                                                                      .toString())
                            .replaceAll("'", "\'");
                    address = address.replaceAll("'", "''");

                    String postal_code = ((CustomEditText) findViewById(R.id.postal_code_et)).getText()
                                                                                             .toString()
                                                                                             .replace("'", "\'");
                    String country = ((CustomEditText) findViewById(R.id.country_et)).getText()
                                                                                     .toString()
                                                                                     .replaceAll("'", "\'");
                    country = country.replaceAll("'", "''");

                    String city = ((CustomEditText) findViewById(R.id.city_et)).getText()
                                                                               .toString()
                                                                               .replaceAll("'", "\'");
                    city = city.replaceAll("'", "''");

                    String codicefiscale = "";
                    String provincia = ((CustomEditText) findViewById(R.id.provincia_et)).getText()
                                                                                         .toString()
                                                                                         .replaceAll("'", "\'");
                    String codiceDestinatario = ((CustomEditText) findViewById(R.id.codice_destinatario_et))
                            .getText()
                            .toString()
                            .replaceAll("'", "\'");
                    String pec = ((CustomEditText) findViewById(R.id.pec_et)).getText()
                                                                             .toString()
                                                                             .replaceAll("'", "\'");


                    // The following set of If statements checks whether the minimum conditions to create a new user are
                    // satisfied.
                    if (!company_name.equals("") &&
                            !vat_number.equals("") &&
                            !address.equals("") &&
                            !postal_code.equals("") &&
                            !country.equals("") &&
                            !city.equals("") &&
                            !provincia.equals("") &&
                            (!codiceDestinatario.equals("") || !pec.equals(""))
                    )
                    {
                        if (!company_name.equals(""))
                        {
                            if (vat_number.length() == 11)
                            {
                                if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec
                                        .equals("") && pec.length() > 1))
                                {
                                    if (!name.equals("") || !email.equals("") || !surname.equals(""))
                                    {
                                        if (!email.equals(""))
                                        {
                                            if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email))
                                            {
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
                                        if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email))
                                        {
                                            findViewById(R.id.email_et).startAnimation(shake);
                                            ////Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                        }

                                        else
                                        {
                                            if (mode == MODIFY_MODE)
                                            {
                                                if (StaticValue.blackbox)
                                                {
                                                    RequestParam params = new RequestParam();
                                                    params.add("name", name);
                                                    params.add("surname", surname);
                                                    params.add("email", email);
                                                    if (getDiscountButton().isActivated())
                                                    {
                                                        params.add("discountButton", getDiscountButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("discountButton", String.valueOf(-1));
                                                    }
                                                    if (getClientButton().isActivated())
                                                    {
                                                        params.add("clientButton", getClientButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("clientButton", String.valueOf(-1));
                                                    }
                                                    params.add("id", String.valueOf(currentClient.getClient_id()));
                                                    params.add("companyId", String.valueOf(currentClient.getCompany_id()));
                                                    params.add("companyName", String.valueOf(company_name));
                                                    params.add("address", String.valueOf(address));
                                                    params.add("vat_number", String.valueOf(vat_number));
                                                    params.add("postal_code", String.valueOf(postal_code));
                                                    params.add("city", String.valueOf(city));
                                                    params.add("country", String.valueOf(country));
                                                    params.add("codicefiscale", String.valueOf(codicefiscale));
                                                    params.add("provincia", String.valueOf(provincia));
                                                    params.add("codiceDestinatario", String.valueOf(codiceDestinatario));
                                                    params.add("pec", String.valueOf(pec));

                                                    callHttpHandler("/updateClientWithCompany", params);
                                                    addNewClient.setEnabled(false);
                                                }

                                                else
                                                {
                                                    currentClient.setName(name);
                                                    currentClient.setSurname(surname);
                                                    currentClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    currentClient.setCompany_id(company_id);
                                                    currentClient.setCompany_name(company_name);
                                                    currentClient.setCompany_address(address);
                                                    currentClient.setCompany_vat_number(vat_number);
                                                    currentClient.setCompany_postal_code(postal_code);
                                                    currentClient.setCompany_city(city);
                                                    currentClient.setCompany_country(country);
                                                    currentClient.setCodice_fiscale(codicefiscale);
                                                    currentClient.setProvincia(provincia);
                                                    currentClient.setCodice_destinatario(codiceDestinatario);
                                                    currentClient.setPec(pec);
                                                    dbA.updateClientData(currentClient);
                                                    if (!clientsAdapter.searchMode)
                                                    {
                                                        clientsAdapter.updateDataSet();
                                                    }
                                                    closeModifyMode();
                                                }
                                            }

                                            else if (mode == COSTUMER_MODE)
                                            {
                                                currentClient.setName(name);
                                                currentClient.setSurname(surname);
                                                currentClient.setEmail(email);
                                                company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                currentClient.setCompany_id(company_id);
                                                currentClient.setCompany_name(company_name);
                                                currentClient.setCompany_address(address);
                                                currentClient.setCompany_vat_number(vat_number);
                                                currentClient.setCompany_postal_code(postal_code);
                                                currentClient.setCompany_city(city);
                                                currentClient.setCompany_country(country);
                                                currentClient.setCodice_fiscale(codicefiscale);
                                                currentClient.setProvincia(provincia);
                                                currentClient.setCodice_destinatario(codiceDestinatario);
                                                currentClient.setPec(pec);
                                                dbA.updateClientData(currentClient);
                                                if (!clientsAdapter.searchMode)
                                                {
                                                    clientsAdapter.updateDataSet();
                                                }

                                            }
                                            else
                                            {
                                                if (StaticValue.blackbox)
                                                {
                                                    RequestParam params = new RequestParam();
                                                    params.add("name", name);
                                                    params.add("surname", surname);
                                                    params.add("email", email);
                                                    if (getDiscountButton().isActivated())
                                                    {
                                                        params.add("discountButton", getDiscountButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("discountButton", String.valueOf(-1));
                                                    }
                                                    if (getClientButton().isActivated())
                                                    {
                                                        params.add("clientButton", getClientButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("clientButton", String.valueOf(-1));
                                                    }

                                                    params.add("companyName", company_name);
                                                    params.add("address", address);
                                                    params.add("vat_number", vat_number);
                                                    params.add("postal_code", postal_code);
                                                    params.add("city", city);
                                                    params.add("country", country);
                                                    params.add("codicefiscale", codicefiscale);
                                                    params.add("provincia", provincia);
                                                    params.add("codiceDestinatario", codiceDestinatario);
                                                    params.add("pec", pec);

                                                    callHttpHandler("/insertClientWithCompany", params);
                                                    addNewClient.setEnabled(false);

                                                }
                                                else
                                                {
                                                    int client_id = dbA.insertClient(name, surname, email);
                                                    if (client_id != -1)
                                                    {
                                                        company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                        dbA.insertClientInCompany(client_id, company_id);
                                                        if (!clientsAdapter.searchMode)
                                                        {
                                                            clientsAdapter.updateDataSet();
                                                        }

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
                                                        codice_fiscale_et.setText("");
                                                        provincia_et.setText("");
                                                        codice_destinatario_et.setText("");
                                                        pec_et.setText("");
                                                        addCompanyInfo.performClick();
                                                    }
                                                }
                                            }
                                        }

                                    }
                                    else
                                    {
                                        if (email.equals(""))
                                        {
                                            findViewById(R.id.email_et).startAnimation(shake);
                                        }
                                        if (name.equals(""))
                                        {
                                            findViewById(R.id.name_et).startAnimation(shake);
                                        }
                                        if (surname.equals(""))
                                        {
                                            findViewById(R.id.surname_et).startAnimation(shake);
                                        }
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7))
                                    {
                                        findViewById(R.id.codice_destinatario_et).startAnimation(shake);
                                    }
                                    else
                                    {
                                        findViewById(R.id.pec_et).startAnimation(shake);
                                    }
                                }
                            }
                            else
                            {
                                findViewById(R.id.vat_number_et).startAnimation(shake);
                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            findViewById(R.id.company_name_et).startAnimation(shake);
                            //findViewById(R.id.client_info_container).startAnimation(shake);
                            ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        findViewById(R.id.client_info_container).startAnimation(shake);
                    }
                }

                else if (addPersonalInfo.isActivated())
                {
                    String company_name = "";
                    String vat_number = ((CustomEditText) findViewById(R.id.vat_number_et_p)).getText()
                                                                                             .toString()
                                                                                             .replaceAll("'", "\'");
                    String address = (((CustomEditText) findViewById(R.id.address_et_p)).getText()
                                                                                        .toString())
                            .replaceAll("'", "\'");
                    address = address.replaceAll("'", "''");
                    String postal_code = ((CustomEditText) findViewById(R.id.postal_code_et_p)).getText()
                                                                                               .toString()
                                                                                               .replace("'", "\'");
                    String country = ((CustomEditText) findViewById(R.id.country_et_p)).getText()
                                                                                       .toString()
                                                                                       .replaceAll("'", "\'");
                    country = country.replaceAll("'", "''");
                    String city = ((CustomEditText) findViewById(R.id.city_et_p)).getText()
                                                                                 .toString()
                                                                                 .replaceAll("'", "\'");
                    city = city.replaceAll("'", "''");
                    String codicefiscale = ((CustomEditText) findViewById(R.id.codice_fiscale_et_p))
                            .getText()
                            .toString()
                            .replaceAll("'", "\'");
                    String provincia = ((CustomEditText) findViewById(R.id.provincia_et_p))
                            .getText()
                            .toString()
                            .replaceAll("'", "\'");
                    String codiceDestinatario = ((CustomEditText) findViewById(R.id.codice_destinatario_et_p))
                            .getText()
                            .toString()
                            .replaceAll("'", "\'");
                    String pec = ((CustomEditText) findViewById(R.id.pec_et_p)).getText()
                                                                               .toString()
                                                                               .replaceAll("'", "\'");


                    // The following set of If statements checks whether the minimum conditions to create a new user are satisfied.
                    if (!codicefiscale.equals("") &&
                            !vat_number.equals("") &&
                            !address.equals("") &&
                            !postal_code.equals("") &&
                            !country.equals("") &&
                            !city.equals("") &&
                            !provincia.equals("") &&
                            (!codiceDestinatario.equals("") || !pec.equals(""))
                    )
                    {
                        if (codicefiscale.length() == 16)
                        {
                            if (vat_number.length() == 11)
                            {
                                if ((!codiceDestinatario.equals("") && codiceDestinatario.length() == 7) || (!pec
                                        .equals("") && pec.length() > 1))
                                {
                                    if (!name.equals("") || !email.equals("") || !surname.equals(""))
                                    {
                                        if (!email.equals(""))
                                        {
                                            if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email))
                                            {
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
                                        if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email))
                                        {
                                            findViewById(R.id.email_et).startAnimation(shake);
                                            ////Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            if (mode == MODIFY_MODE)
                                            {
                                                if (StaticValue.blackbox)
                                                {
                                                    RequestParam params = new RequestParam();
                                                    params.add("name", name);
                                                    params.add("surname", surname);
                                                    params.add("email", email);
                                                    if (getDiscountButton().isActivated())
                                                    {
                                                        params.add("discountButton", getDiscountButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("discountButton", String.valueOf(-1));
                                                    }
                                                    if (getClientButton().isActivated())
                                                    {
                                                        params.add("clientButton", getClientButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("clientButton", String.valueOf(-1));
                                                    }
                                                    params.add("id", String.valueOf(currentClient.getClient_id()));
                                                    params.add("companyId", String.valueOf(currentClient.getCompany_id()));
                                                    params.add("companyName", String.valueOf(company_name));
                                                    params.add("address", String.valueOf(address));
                                                    params.add("vat_number", String.valueOf(vat_number));
                                                    params.add("postal_code", String.valueOf(postal_code));
                                                    params.add("city", String.valueOf(city));
                                                    params.add("country", String.valueOf(country));
                                                    params.add("codicefiscale", String.valueOf(codicefiscale));
                                                    params.add("provincia", String.valueOf(provincia));
                                                    params.add("codiceDestinatario", String.valueOf(codiceDestinatario));
                                                    params.add("pec", String.valueOf(pec));


                                                    callHttpHandler("/updateClientWithCompany", params);
                                                    addNewClient.setEnabled(false);
                                                }
                                                else
                                                {

                                                    currentClient.setName(name);
                                                    currentClient.setSurname(surname);
                                                    currentClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                    currentClient.setCompany_id(company_id);
                                                    currentClient.setCompany_name(company_name);
                                                    currentClient.setCompany_address(address);
                                                    currentClient.setCompany_vat_number(vat_number);
                                                    currentClient.setCompany_postal_code(postal_code);
                                                    currentClient.setCompany_city(city);
                                                    currentClient.setCompany_country(country);
                                                    currentClient.setCodice_fiscale(codicefiscale);
                                                    currentClient.setProvincia(provincia);
                                                    currentClient.setCodice_destinatario(codiceDestinatario);
                                                    currentClient.setPec(pec);
                                                    dbA.updateClientData(currentClient);
                                                    if (!clientsAdapter.searchMode)
                                                    {
                                                        clientsAdapter.updateDataSet();
                                                    }
                                                    closeModifyMode();
                                                }
                                            }
                                            else if (mode == COSTUMER_MODE)
                                            {
                                                currentClient.setName(name);
                                                currentClient.setSurname(surname);
                                                currentClient.setEmail(email);
                                                company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                currentClient.setCompany_id(company_id);
                                                currentClient.setCompany_name(company_name);
                                                currentClient.setCompany_address(address);
                                                currentClient.setCompany_vat_number(vat_number);
                                                currentClient.setCompany_postal_code(postal_code);
                                                currentClient.setCompany_city(city);
                                                currentClient.setCompany_country(country);
                                                currentClient.setCodice_fiscale(codicefiscale);
                                                currentClient.setProvincia(provincia);
                                                currentClient.setCodice_destinatario(codiceDestinatario);
                                                currentClient.setPec(pec);
                                                dbA.updateClientData(currentClient);
                                                if (!clientsAdapter.searchMode)
                                                {
                                                    clientsAdapter.updateDataSet();
                                                }

                                            }
                                            else
                                            {
                                                if (StaticValue.blackbox)
                                                {
                                                    RequestParam params = new RequestParam();
                                                    params.add("name", name);
                                                    params.add("surname", surname);
                                                    params.add("email", email);
                                                    if (getDiscountButton().isActivated())
                                                    {
                                                        params.add("discountButton", getDiscountButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("discountButton", String.valueOf(-1));
                                                    }
                                                    if (getClientButton().isActivated())
                                                    {
                                                        params.add("clientButton", getClientButton()
                                                                .getText()
                                                                .toString()
                                                                .toLowerCase());
                                                    }
                                                    else
                                                    {
                                                        params.add("clientButton", String.valueOf(-1));
                                                    }

                                                    params.add("companyName", String.valueOf(company_name));
                                                    params.add("address", String.valueOf(address));
                                                    params.add("vat_number", String.valueOf(vat_number));
                                                    params.add("postal_code", String.valueOf(postal_code));
                                                    params.add("city", String.valueOf(city));
                                                    params.add("country", String.valueOf(country));
                                                    params.add("codicefiscale", String.valueOf(codicefiscale));
                                                    params.add("provincia", String.valueOf(provincia));
                                                    params.add("codiceDestinatario", String.valueOf(codiceDestinatario));
                                                    params.add("pec", String.valueOf(pec));


                                                    callHttpHandler("/insertClientWithCompany", params);
                                                    addNewClient.setEnabled(false);

                                                }
                                                else
                                                {
                                                    int client_id = dbA.insertClient(name, surname, email);
                                                    if (client_id != -1)
                                                    {
                                                        company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codiceDestinatario, pec);
                                                        dbA.insertClientInCompany(client_id, company_id);
                                                        if (!clientsAdapter.searchMode)
                                                        {
                                                            clientsAdapter.updateDataSet();
                                                        }

                                                        //reset dataFields
                                                        name_et.setText("");
                                                        surname_et.setText("");
                                                        email_et.setText("");
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
                                        }

                                    }
                                    else
                                    {
                                        if (email.equals(""))
                                        {
                                            findViewById(R.id.email_et).startAnimation(shake);
                                        }
                                        if (name.equals(""))
                                        {
                                            findViewById(R.id.name_et).startAnimation(shake);
                                        }
                                        if (surname.equals(""))
                                        {
                                            findViewById(R.id.surname_et).startAnimation(shake);
                                        }
                                        //findViewById(R.id.client_info_container).startAnimation(shake);
                                        ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    if ((codiceDestinatario.equals("") || codiceDestinatario.length() != 7))
                                    {
                                        findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                    }
                                    else
                                    {
                                        findViewById(R.id.pec_et_p).startAnimation(shake);
                                    }
                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                    ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                findViewById(R.id.vat_number_et_p).startAnimation(shake);
                                //findViewById(R.id.client_info_container).startAnimation(shake);
                                ////Toast.makeText(ClientsActivity.this, "VAT Number must be present", //Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                            //findViewById(R.id.client_info_container).startAnimation(shake);
                            ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        findViewById(R.id.client_info_container).startAnimation(shake);
                        ////Toast.makeText(ClientsActivity.this, "Company Name must or Codice Fiscale must be present", //Toast.LENGTH_SHORT).show();
                    }
                }

                else
                {
                    if (!name.equals("") || !email.equals("") || !surname.equals(""))
                    {
                        if (!email.equals(""))
                        {
                            if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email))
                            {
                               /* findViewById(R.id.email_et).startAnimation(shake);
                                //Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();*/
                            }
                        }
                        /**
                         * If Modify Mode is not on, then the "addNewClient" button will actually add a new client,
                         *  otherwise it will update the selected client.
                         */
                        if (!Pattern.matches("^[-a-zA-Z0-9_.\\-]+@[a-zA-Z0-9_.\\-]+\\.[a-zA-Z]{2,5}$", email))
                        {
                            findViewById(R.id.email_et).startAnimation(shake);
                            ////Toast.makeText(getBaseContext(), "Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            if (mode != MODIFY_MODE)
                            {
                                if (!StaticValue.blackbox)
                                {
                                    int client_id = dbA.insertClient(name, surname, email);
                                    if (client_id == -1)
                                    {
                                        findViewById(R.id.email_et).startAnimation(shake);
                                        Toast.makeText(ClientsActivity.this, R.string.client_already_present, Toast.LENGTH_SHORT)
                                             .show();
                                    }
                                    else
                                    {
                                        if (getDiscountButton().isActivated())
                                        {
                                            if (!dbA.checkIfClientHasASingleDiscount(client_id,
                                                                                     getDiscountButton()
                                                                                             .getText()
                                                                                             .toString()
                                                                                             .toLowerCase()
                                            ))
                                            {
                                                dbA.assignDiscountToClient(client_id, getDiscountButton()
                                                        .getText()
                                                        .toString()
                                                        .toLowerCase());
                                                getDiscountButton().setActivated(false);
                                                getDiscountButton().setText(R.string.set_discount);
                                            }
                                        }
                                        if (getClientButton().isActivated())
                                        {
                                            if (!dbA.checkIfClientHasASingleDiscount(client_id,
                                                                                     getClientButton()
                                                                                             .getText()
                                                                                             .toString()
                                                                                             .toLowerCase()
                                            ))
                                            {
                                                dbA.assignDiscountToClient(client_id, getClientButton()
                                                        .getText()
                                                        .toString()
                                                        .toLowerCase());
                                                getClientButton().setActivated(false);
                                                getClientButton().setText(R.string.set_client_type);
                                            }
                                        }

                                        dbA.insertClientInCompany(client_id, -1);
                                        if (!clientsAdapter.searchMode)
                                        {
                                            clientsAdapter.updateDataSet();
                                        }

                                        //reset data fields
                                        name_et.setText("");
                                        surname_et.setText("");
                                        email_et.setText("");
                                    }
                                }
                                else
                                {
                                    /*
                                    RequestParam params = new RequestParam();
                                    params.add("name", name);
                                    params.add("surname", surname);
                                    params.add("email", email);
                                    if (getDiscountButton().isActivated())
                                    {
                                        params.add("discountButton", getDiscountButton(
                                                .getText()
                                                .toString()
                                                .toLowerCase()));
                                    }
                                    else
                                    {
                                        params.add("discountButton", String.valueOf(-1));
                                    }
                                    if (getClientButton().isActivated())
                                    {
                                        params.add("clientButton", getClientButton(
                                                .getText()
                                                .toString()
                                                .toLowerCase()));
                                    }
                                    else
                                    {
                                        params.add("clientButton", String.valueOf(-1));
                                    }
                                    params.add("companyId", String.valueOf(-1));

                                     */

                                    String mDiscount = "-1";
                                    if (getDiscountButton().isActivated())
                                    {
                                        mDiscount = getDiscountButton().getText().toString().toLowerCase();
                                    }

                                    String mClient = "-1";
                                    if (getClientButton().isActivated())
                                    {
                                        mClient = getClientButton().getText().toString().toLowerCase();
                                    }

                                    RequestParam params = new RequestParam();

                                    params.add("name", name);
                                    params.add("surname", surname);
                                    params.add("email", email);
                                    params.add("discountButton", mDiscount);
                                    params.add("clientButton", mClient);
                                    params.add("companyId", -1);

                                    callHttpHandler("/insertClient", params);
                                    addNewClient.setEnabled(false);
                                }
                            }
                            else
                            {
                                if (StaticValue.blackbox)
                                {
                                    RequestParam params = new RequestParam();
                                    params.add("name", name);
                                    params.add("surname", surname);
                                    params.add("email", email);
                                    params.add("id", String.valueOf(currentClient.getClient_id()));

                                    params.add("companyId", String.valueOf(-1));


                                    callHttpHandler("/updateClient", params);
                                    addNewClient.setEnabled(false);
                                }
                                else
                                {
                                    currentClient.setName(name);
                                    currentClient.setSurname(surname);
                                    currentClient.setEmail(email);
                                    currentClient.setCompany_id(-1);
                                    dbA.updateClientData(currentClient);
                                    if (!clientsAdapter.searchMode)
                                    {
                                        clientsAdapter.updateDataSet();
                                    }
                                    closeModifyMode();
                                }
                            }
                        }
                    }
                    else
                    {
                        findViewById(R.id.client_info_container).startAnimation(shake);
                        ////Toast.makeText(ClientsActivity.this, "At least one of Name, Surname or Email must be present", //Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        searchClients.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (mode != MODIFY_MODE)
                {
                    searchClients.setText(R.string.end_search);
                    v.setActivated(!v.isActivated());
                    CustomEditText search_et = (CustomEditText) ClientsActivity.this.findViewById(R.id.search_et);
                    search_et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    search_et.setSingleLine();
                    if (v.isActivated())
                    {
                        clientsAdapter.setSearchMode(true);
                        mode = SEARCH_CLIENT_MODE;
                        search_et.setVisibility(View.VISIBLE);
                        ClientsActivity.this.findViewById(R.id.title_tv)
                                            .setVisibility(View.INVISIBLE);
                        ClientsActivity.this.findViewById(R.id.hline_rv_top)
                                            .setVisibility(View.INVISIBLE);
                        search_et.requestFocus();
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .showSoftInput(search_et, InputMethodManager.SHOW_IMPLICIT);

                        search_et.addTextChangedListener(new TextWatcher()
                        {
                            private Timer timer = new Timer();

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after)
                            {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count)
                            {
                            }

                            @Override
                            public void afterTextChanged(final Editable s)
                            {
                                timer.cancel();
                                timer = new Timer();
                                // milliseconds
                                long DELAY = 500;
                                timer.schedule(
                                        new TimerTask()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                runOnUiThread(new Runnable()
                                                {

                                                    @Override
                                                    public void run()
                                                    {
                                                        clientsAdapter.searchClients(s.toString());
                                                    }
                                                });
                                            }
                                        },
                                        DELAY
                                );

                            }
                        });
                    }
                    else
                    {
                        searchClients.setText(R.string.search_clients);
                        clientsAdapter.setSearchMode(false);
                        mode = COSTUMER_MODE;
                        ClientsActivity.this.findViewById(R.id.title_tv)
                                            .setVisibility(View.VISIBLE);
                        ClientsActivity.this.findViewById(R.id.hline_rv_top)
                                            .setVisibility(View.VISIBLE);
                        ((CustomEditText) ClientsActivity.this.findViewById(R.id.search_et)).setText("");
                        ClientsActivity.this.findViewById(R.id.search_et).setVisibility(View.GONE);
                    }
                }
            }
        });

        setClientType.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (setClientType.isActivated())
                {
                    int value = dbA.getDiscountValue(setClientType.getText()
                                                                  .toString()
                                                                  .toLowerCase());
                    clientPopup("Client Type", "Name Client Type", value);
                }
                else
                {
                    clientPopup("Client Type", "Name Client Type", 0);
                }
            }
        });

        setClientType.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {

                if (setClientType.isActivated())
                {
                    if (currentClient != null)
                    {
                        dbA.deleteSpecificClientDiscount(currentClient.getClient_id(),
                                                         setClientType.getText()
                                                                      .toString()
                                                                      .toLowerCase()
                        );
                    }
                    setClientType.setActivated(false);
                    setClientType.setText(R.string.set_client_type);
                }

                return true;
            }
        });

        setDiscount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (setDiscount.isActivated())
                {
                    int value = Integer.parseInt(setDiscount.getText().toString().replace("%", ""));
                    clientPopup("Discount Type", "Name Discount Type", value);
                }
                else
                {
                    clientPopup("Discount Type", "Name Discount Type", 0);
                }
            }
        });

        setDiscount.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {

                if (setDiscount.isActivated())
                {
                    if (currentClient != null)
                    {
                        ArrayList<String> discounts = dbA.checkIfClientHasDiscount(currentClient.getClient_id());
                        String            discount  = "";
                        if (discounts.size() > 1)
                        {
                            discount = discounts.get(0);
                            if (dbA.getDiscountMode(discount) != 0)
                            {
                                discount = discounts.get(1);
                            }
                        }
                        dbA.deleteSpecificClientDiscount(currentClient.getClient_id(),
                                                         discount
                        );
                    }
                    setDiscount.setActivated(false);
                    setDiscount.setText(R.string.set_discount);
                }

                return true;
            }
        });

        add_fidelity_credit_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupAddFidelityCreditPopup();
            }
        });
    }


    private void setupXOK()
    {
        Intent intent = getIntent();
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String sender = intent.getStringExtra("sender");
                clientsAdapter.notifySetChanged(null);

                switch (mode)
                {
                    case 0:
                        if (clientsAdapter.searchMode)
                        {
                            clientsAdapter.setSearchMode(false);
                            ClientsActivity.this.findViewById(R.id.title_tv)
                                                .setVisibility(View.VISIBLE);
                            ((CustomEditText) ClientsActivity.this.findViewById(R.id.search_et)).setText("");
                            ClientsActivity.this.findViewById(R.id.search_et)
                                                .setVisibility(View.GONE);
                            clientsAdapter.fetchClients();
                            clientsAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            if (currentClient != null)
                            {
                                closeModifyMode();
                            }
                            else
                            {
                                Intent intentToGo = new Intent(ClientsActivity.this, Operative.class);
                                intentToGo.putExtra("username", intent.getStringExtra("username"));
                                intentToGo.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                                int billId = intent.getIntExtra("billId", -1);
                                intentToGo.putExtra("billId", billId);
                                int orderNumber;
                                if (billId == -1 || dbA.getPaidBill(billId) == 1)
                                {
                                    orderNumber = -1;
                                }
                                else
                                {
                                    orderNumber = intent.getIntExtra("orderNumber", -1);
                                }
                                intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                                intentToGo.putExtra("orderNumber", orderNumber);

                                intentToGo.putExtra("userId", userId);
                                intentToGo.putExtra("userType", userType);
                                startActivity(intentToGo);
                                finish();
                            }
                        }
                        /*if(sender!=null) {
                            closeModifyMode();

                            if (sender.equals("operative")) {
                                Intent newIntent = new Intent();
                                finish();
                            }
                        }else{
                            Intent intentToGo = new Intent(ClientsActivity.this, Operative.class);
                            intentToGo.putExtra("username", intent.getStringExtra("username"));
                            intentToGo.putExtra("isAdmin",intent.getIntExtra("isAdmin", -1));
                            intentToGo.putExtra("billId",intent.getIntExtra("billId", -1));
                            intentToGo.putExtra("userId", userId);
                            intentToGo.putExtra("userType", userType);
                            startActivity(intentToGo);
                            //finish();
                        }*/

                        break;
                    case 1:
                        if (sender == null)
                        {
                            Intent intentToGo = new Intent(ClientsActivity.this, Operative.class);
                            intentToGo.putExtra("username", intent.getStringExtra("username"));
                            intentToGo.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                            int billId = intent.getIntExtra("billId", -1);
                            intentToGo.putExtra("billId", billId);
                            int orderNumber;
                            if (billId == -1 || dbA.getPaidBill(billId) == 1)
                            {
                                orderNumber = -1;
                            }
                            else
                            {
                                orderNumber = intent.getIntExtra("orderNumber", -1);
                            }
                            intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                            intentToGo.putExtra("userId", userId);
                            intentToGo.putExtra("userType", userType);
                            intentToGo.putExtra("orderNumber", orderNumber);
                            startActivity(intentToGo);
                            finish();
                        }
                        else
                        {
                            if (sender.equals("payment-invoice"))
                            {
                                TemporaryOrder.setClient(null);
                                setResult(RESULT_CANCELED, intent);
                                finish();
                            }
                        }
                        break;
                    case 2:
                        if (clientsAdapter.searchMode)
                        {
                            clientsAdapter.setSearchMode(false);
                            ClientsActivity.this.findViewById(R.id.title_tv)
                                                .setVisibility(View.VISIBLE);
                            ((CustomEditText) ClientsActivity.this.findViewById(R.id.search_et)).setText("");
                            ClientsActivity.this.findViewById(R.id.search_et)
                                                .setVisibility(View.GONE);
                            clientsAdapter.fetchClients();
                            clientsAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            if (currentClient != null)
                            {
                                closeModifyMode();
                            }
                            else
                            {
                                int    billId     = intent.getIntExtra("billId", -1);
                                Intent intentToGo = new Intent(ClientsActivity.this, Operative.class);
                                intentToGo.putExtra("username", intent.getStringExtra("username"));
                                intentToGo.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                                intentToGo.putExtra("billId", intent.getIntExtra("billId", -1));
                                intentToGo.putExtra("customer", intent.getIntExtra("customer", -1));
                                intentToGo.putExtra("customerModify", intent.getBooleanExtra("customerModify", false));
                                intentToGo.putExtra("modifyPosition", intent.getIntExtra("modifyPosition", -1));
                                intentToGo.putExtra("cashListIndex", intent.getIntExtra("cashListIndex", -1));
                                intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                                intentToGo.putExtra("userId", userId);
                                intentToGo.putExtra("userType", userType);
                                intentToGo.putExtra("orderNumber", intent.getIntExtra("orderNumber", -1));

                                startActivity(intentToGo);
                                finish();
                            }
                        }

                        break;
                    case 3:
                        //e-mail selection
                        Intent intentToGo = new Intent(ClientsActivity.this, PaymentActivity.class);
                        intentToGo.putExtra("username", intent.getStringExtra("username"));
                        intentToGo.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                        //int billId = intent.getIntExtra("billId", -1);
                        intentToGo.putExtra("billId", billId);
                        int orderNumber;
                        if (billId == -1 || dbA.getPaidBill(billId) == 1)
                        {
                            orderNumber = -1;
                        }
                        else
                        {
                            orderNumber = intent.getIntExtra("orderNumber", -1);
                        }
                        intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                        intentToGo.putExtra("userId", userId);
                        intentToGo.putExtra("userType", userType);
                        intentToGo.putExtra("orderNumber", orderNumber + 2);
                        startActivity(intentToGo);
                        finish();
                        break;
                    case 4:
                        //invoice section
                        Intent backIntent = new Intent(ClientsActivity.this, PaymentActivity.class);
                        backIntent.putExtra("username", intent.getStringExtra("username"));
                        backIntent.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                        backIntent.putExtra("billId", billId);
                        int ordNumber;
                        if (billId == -1 || dbA.getPaidBill(billId) == 1)
                        {
                            ordNumber = -1;
                        }
                        else
                        {
                            ordNumber = intent.getIntExtra("orderNumber", -1);
                        }
                        backIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                        backIntent.putExtra("userId", userId);
                        backIntent.putExtra("userType", userType);
                        backIntent.putExtra("orderNumber", ordNumber + 2);
                        startActivity(backIntent);
                        finish();
                        break;
                    case 5:
                        //new e-mail selection
                        Intent newBackIntent = new Intent(ClientsActivity.this, PaymentActivity.class);
                        newBackIntent.putExtra("username", intent.getStringExtra("username"));
                        newBackIntent.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                        newBackIntent.putExtra("billId", billId);
                        int number;
                        if (billId == -1
                            //|| dbA.getPaidBill(billId) == 1
                        )
                        {
                            number = -1;
                        }
                        else
                        {
                            number = intent.getIntExtra("orderNumber", -1);
                        }
                        newBackIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                        newBackIntent.putExtra("userId", userId);
                        newBackIntent.putExtra("userType", userType);
                        if (number < 1)
                        {
                            newBackIntent.putExtra("orderNumber", number + 2);
                        }
                        else
                        {
                            newBackIntent.putExtra("orderNumber", number);
                        }
                        startActivity(newBackIntent);
                        finish();
                        break;
                    case 6:
                        searchClients.setText(R.string.search_clients);
                        clientsAdapter.setSearchMode(false);
                        mode = SELECTION_MODE;
                        ClientsActivity.this.findViewById(R.id.title_tv)
                                            .setVisibility(View.VISIBLE);
                        ClientsActivity.this.findViewById(R.id.hline_rv_top)
                                            .setVisibility(View.VISIBLE);
                        ((CustomEditText) ClientsActivity.this.findViewById(R.id.search_et)).setText("");
                        ClientsActivity.this.findViewById(R.id.search_et).setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }

               /* if(intent.getAction().equals("selectClient")){
                    if(sender.equals("payment-invoice")){
                        TemporaryOrder.setClient(null);
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                }
                else{
                    if(sender.equals("operative")){
                        Intent newIntent = new Intent();
                        finish();
                    }
                }*/
            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                switch (mode)
                {
                    case 0:
                        break;
                    case 1:
                        String sender = intent.getStringExtra("sender");
                        if (sender != null)
                        {
                            if (sender.equals("payment-invoice"))
                            {
                                TemporaryOrder.setClient(currentClient);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                        else
                        {
                            Intent intentToGo = new Intent(ClientsActivity.this, Operative.class);
                            intentToGo.putExtra("username", intent.getStringExtra("username"));
                            intentToGo.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                            intentToGo.putExtra("billId", intent.getIntExtra("billId", -1));
                            int on = intent.getIntExtra("orderNumber", -1);
                            intentToGo.putExtra("orderNumber", intent.getIntExtra("orderNumber", -1));
                            intentToGo.putExtra("customerModify", intent.getBooleanExtra("customerModify", false));
                            intentToGo.putExtra("modifyPosition", intent.getIntExtra("modifyPosition", -1));
                            intentToGo.putExtra("cashListIndex", intent.getIntExtra("cashListIndex", -1));
                            intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                            intentToGo.putExtra("userId", userId);
                            intentToGo.putExtra("userType", userType);
                            intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                            if (currentClient != null)
                            {
                                intentToGo.putExtra("customer", currentClient.getClient_id());
                                intentToGo.putExtra("email", currentClient.getEmail());
                            }
                            else
                            {
                                intentToGo.putExtra("customer", -1);
                            }
                            if (clientLongClick)
                            {
                                intentToGo.putExtra("modifiedCustomer", true);
                            }

                            startActivity(intentToGo);
                            finish();

                        }
                        break;
                    case 2:
                        Intent intentToGo = new Intent(ClientsActivity.this, Operative.class);
                        intentToGo.putExtra("username", intent.getStringExtra("username"));
                        intentToGo.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                        int myBillId = intent.getIntExtra("billId", -1);
                        intentToGo.putExtra("billId", intent.getIntExtra("billId", -1));
                        intentToGo.putExtra("orderNumber", intent.getIntExtra("orderNumber", -1));
                        intentToGo.putExtra("customerModify", intent.getBooleanExtra("customerModify", false));
                        intentToGo.putExtra("modifyPosition", intent.getIntExtra("modifyPosition", -1));
                        intentToGo.putExtra("cashListIndex", intent.getIntExtra("cashListIndex", -1));
                        intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                        intentToGo.putExtra("userId", userId);
                        intentToGo.putExtra("userType", userType);
                        intentToGo.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                        if (currentClient != null)
                        {
                            intentToGo.putExtra("customer", currentClient.getClient_id());
                            intentToGo.putExtra("email", currentClient.getEmail());
                        }
                        else
                        {
                            intentToGo.putExtra("customer", -1);
                        }
                        if (clientLongClick)
                        {
                            intentToGo.putExtra("modifiedCustomer", true);
                        }

                        startActivity(intentToGo);
                        finish();
                        break;
                    case 3:
                        //TODO: gestire i vari casi e far funzionare anche ApplyChange
                        if (currentClient != null)
                        {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            String mailto = "mailto:" + currentClient.getEmail() +
                                    "?subject=" + Uri.encode("Scontrino Burgheria") +
                                    "&body=" + Uri.encode("corpo del messaggio contenente lo scontrino o fattura");
                            emailIntent.setData(Uri.parse(mailto));

                            try
                            {
                                startActivity(emailIntent);
                                finish();
                            }
                            catch (ActivityNotFoundException e)
                            {
                                e.printStackTrace();
                                ////Toast.makeText(ClientsActivity.this, "No application found for sending emails", //Toast.LENGTH_LONG).show();
                            }
                        }
                        else if (currentClient == null)
                        {
                            break;
                        }
                    case 4:
                        //TODO: when modifying an existing client, it adds it as a new one
                        if (currentClient == null)
                        {
                            ////Toast.makeText(ClientsActivity.this, "Select one client.", //Toast.LENGTH_SHORT).show();
                        }
                        else if (addCompanyInfo.isActivated())
                        {
                            if (currentClient.getCompany_id() == -1)
                            {
                                String name = ((CustomEditText) findViewById(R.id.name_et)).getText()
                                                                                           .toString()
                                                                                           .replaceAll("'", "\'");
                                String surname = ((CustomEditText) findViewById(R.id.surname_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String email = ((CustomEditText) findViewById(R.id.email_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String company_name = ((CustomEditText) findViewById(R.id.company_name_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                company_name = company_name.replaceAll("'", "''");
                                String vat_number = ((CustomEditText) findViewById(R.id.vat_number_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String address = (((CustomEditText) findViewById(R.id.address_et))
                                        .getText()
                                        .toString()).replaceAll("'", "\'");
                                address = address.replaceAll("'", "''");
                                String postal_code = ((CustomEditText) findViewById(R.id.postal_code_et))
                                        .getText()
                                        .toString()
                                        .replace("'", "\'");
                                String country = ((CustomEditText) findViewById(R.id.country_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                country = country.replaceAll("'", "''");
                                String city = ((CustomEditText) findViewById(R.id.city_et)).getText()
                                                                                           .toString()
                                                                                           .replaceAll("'", "\'");
                                city = city.replaceAll("'", "''");
                                String codicefiscale = "";
                                String provincia = ((CustomEditText) findViewById(R.id.provincia_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String codice_destinatario = ((CustomEditText) findViewById(R.id.codice_destinatario_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String pec = ((CustomEditText) findViewById(R.id.pec_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");


                                if (!company_name.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codice_destinatario.equals("") || !pec.equals(""))
                                )
                                {

                                    if (!company_name.equals(""))
                                    {
                                        if (!vat_number.equals("") && vat_number.length() == 11)
                                        {
                                            if ((!codice_destinatario.equals("") && codice_destinatario
                                                    .length() == 7) || (!pec.equals("") && pec.length() > 1))
                                            {
                                                if (!name.equals("") || !email.equals("") || !surname
                                                        .equals(""))
                                                {
                                                    if (!email.equals(""))
                                                    {
                                                        if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                                                        {
                                                            ////Toast.makeText(getBaseContext(),"Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    int company_id;
                                                    currentClient.setName(name);
                                                    currentClient.setSurname(surname);
                                                    currentClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codice_destinatario, pec);
                                                    currentClient.setCompany_id(company_id);
                                                    currentClient.setCompany_name(company_name);
                                                    currentClient.setCompany_address(address);
                                                    currentClient.setCompany_vat_number(vat_number);
                                                    currentClient.setCompany_postal_code(postal_code);
                                                    currentClient.setCompany_city(city);
                                                    currentClient.setCompany_country(country);
                                                    currentClient.setCodice_fiscale(codicefiscale);
                                                    currentClient.setProvincia(provincia);
                                                    currentClient.setCodice_destinatario(codice_destinatario);
                                                    currentClient.setPec(pec);
                                                    dbA.updateClientData(currentClient);

                                                    /**
                                                     * NEW WAY: CALLING BLACKBOX
                                                     *//*
                                            String myUrl = url + "/updateClient";
                                            RequestParam params = new RequestParam();
                                            params.add("client_id", String.valueOf(selectedClient.getClient_id()));
                                            params.add("company_id", String.valueOf(company_id));
                                            params.add("client_in_company_id", String.valueOf(selectedClient.getClient_in_company_id()));
                                            params.add("client_name", name);
                                            params.add("client_surname", surname);
                                            params.add("email", email);
                                            params.add("company_name", company_name);
                                            params.add("company_address", address);
                                            params.add("company_vat_number", vat_number);
                                            params.add("company_postal_code", postal_code);
                                            params.add("city", city);
                                            params.add("country", country);
                                            params.add("province", provincia);
                                            params.add("fiscal_code", codicefiscale);
                                            params.add("sdi", codice_destinatario);
                                            params.add("pec", pec);
                                            httpHandler = new HttpHandler();
                                            httpHandler.delegate = myself;
                                            httpHandler.UpdateInfoAsyncTask(myUrl, params);
                                            httpHandler.execute();*/

                                                    if (!clientsAdapter.searchMode)
                                                    {
                                                        clientsAdapter.updateDataSet();
                                                    }
                                                    closeModifyMode();
                                                    Toast.makeText(ClientsActivity.this, R.string.client_updated, Toast.LENGTH_SHORT)
                                                         .show();
                                                }
                                                else
                                                {
                                                    if (email.equals(""))
                                                    {
                                                        findViewById(R.id.email_et).startAnimation(shake);
                                                    }
                                                    if (name.equals(""))
                                                    {
                                                        findViewById(R.id.name_et).startAnimation(shake);
                                                    }
                                                    if (surname.equals(""))
                                                    {
                                                        findViewById(R.id.surname_et).startAnimation(shake);
                                                    }
                                              /*      findViewById(R.id.client_info_container).startAnimation(shake);
                                                    Toast.makeText(ClientsActivity.this, R.string.at_least_one_of_name_surname_or_email_must_be_present, Toast.LENGTH_SHORT).show();
                                             */
                                                }
                                            }
                                            else
                                            {
                                                if ((codice_destinatario.equals("") || codice_destinatario
                                                        .length() != 7))
                                                {
                                                    findViewById(R.id.codice_destinatario_et).startAnimation(shake);
                                                }
                                                else
                                                {
                                                    findViewById(R.id.pec_et).startAnimation(shake);
                                                }
                                               /* findViewById(R.id.client_info_container).startAnimation(shake);
                                                Toast.makeText(ClientsActivity.this,"wrong pec od code", Toast.LENGTH_SHORT).show();*/

                                            }
                                        }
                                        else
                                        {
                                            findViewById(R.id.vat_number_et).startAnimation(shake);
                                            //findViewById(R.id.client_info_container).startAnimation(shake);
                                            //Toast.makeText(ClientsActivity.this, R.string.vat_number_must_be_present+ "and longer than 11 digits", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else
                                    {
                                        findViewById(R.id.company_name_et).startAnimation(shake);
//                                        findViewById(R.id.client_info_container).startAnimation(shake);
//                                        Toast.makeText(ClientsActivity.this, R.string.company_name_or_fiscal_code_must_be_present, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    findViewById(R.id.client_info_container).startAnimation(shake);
/*
                                    Toast.makeText(ClientsActivity.this, "Missingo field", Toast.LENGTH_SHORT).show();*/
                                }
                            }
                            else
                            {
                                String name = ((CustomEditText) findViewById(R.id.name_et)).getText()
                                                                                           .toString()
                                                                                           .replaceAll("'", "\'");
                                String surname = ((CustomEditText) findViewById(R.id.surname_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String email = ((CustomEditText) findViewById(R.id.email_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String company_name = ((CustomEditText) findViewById(R.id.company_name_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                company_name = company_name.replaceAll("'", "''");
                                String vat_number = ((CustomEditText) findViewById(R.id.vat_number_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String address = (((CustomEditText) findViewById(R.id.address_et))
                                        .getText()
                                        .toString()).replaceAll("'", "\'");
                                address = address.replaceAll("'", "''");
                                String postal_code = ((CustomEditText) findViewById(R.id.postal_code_et))
                                        .getText()
                                        .toString()
                                        .replace("'", "\'");
                                String country = ((CustomEditText) findViewById(R.id.country_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                country = country.replaceAll("'", "''");
                                String city = ((CustomEditText) findViewById(R.id.city_et)).getText()
                                                                                           .toString()
                                                                                           .replaceAll("'", "\'");
                                city = city.replaceAll("'", "''");
                                String codicefiscale = "";
                                String provincia = ((CustomEditText) findViewById(R.id.provincia_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String codice_destinatario = ((CustomEditText) findViewById(R.id.codice_destinatario_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String pec = ((CustomEditText) findViewById(R.id.pec_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");

                                if (!company_name.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codice_destinatario.equals("") || !pec.equals(""))
                                )
                                {
                                    if (!company_name.equals(""))
                                    {
                                        if (!vat_number.equals("") && vat_number.length() == 11)
                                        {
                                            if ((!codice_destinatario.equals("") && codice_destinatario
                                                    .length() == 7) || (!pec.equals("") && pec.length() > 1))
                                            {
                                                if (!name.equals("") || !email.equals("") || !surname
                                                        .equals(""))
                                                {
                                                    if (!email.equals(""))
                                                    {
                                                        if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                                                        {
                                                            //Toast.makeText(getBaseContext(),"Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    currentClient.setName(name);
                                                    currentClient.setSurname(surname);
                                                    currentClient.setEmail(email);
                                                    currentClient.setCompany_id(currentClient.getCompany_id());
                                                    currentClient.setCompany_name(company_name);
                                                    currentClient.setCompany_address(address);
                                                    currentClient.setCompany_vat_number(vat_number);
                                                    currentClient.setCompany_postal_code(postal_code);
                                                    currentClient.setCompany_city(city);
                                                    currentClient.setCompany_country(country);
                                                    currentClient.setCodice_fiscale(codicefiscale);
                                                    currentClient.setProvincia(provincia);
                                                    currentClient.setCodice_destinatario(codice_destinatario);
                                                    currentClient.setPec(pec);
                                                    dbA.updateClientData(currentClient);
                                                    if (!clientsAdapter.searchMode)
                                                    {
                                                        clientsAdapter.updateDataSet();
                                                    }
                                                    //closeModifyMode();
                                                    Intent myIntent = new Intent(ClientsActivity.this, PaymentActivity.class);
                                                    myIntent.putExtra("username", intent.getStringExtra("username"));
                                                    myIntent.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                                                    myIntent.putExtra("billId", intent.getIntExtra("billId", -1));
                                                    myIntent.putExtra("orderNumber", intent.getIntExtra("orderNumber", -1) + 2);
                                                    myIntent.putExtra("customerModify", intent.getBooleanExtra("customerModify", false));
                                                    myIntent.putExtra("modifyPosition", intent.getIntExtra("modifyPosition", -1));
                                                    myIntent.putExtra("cashListIndex", intent.getIntExtra("cashListIndex", -1));
                                                    myIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                                                    myIntent.putExtra("userId", userId);
                                                    myIntent.putExtra("userType", userType);
                                                    myIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                                                    if (currentClient != null)
                                                    {
                                                        myIntent.putExtra("customer", currentClient.getClient_id());
                                                        myIntent.putExtra("email", currentClient.getEmail());
                                                    }
                                                    else
                                                    {
                                                        myIntent.putExtra("customer", -1);
                                                    }

                                                    startActivity(myIntent);
                                                    finish();
                                                }
                                                else
                                                {
                                                    if (email.equals(""))
                                                    {
                                                        findViewById(R.id.email_et).startAnimation(shake);
                                                    }
                                                    if (name.equals(""))
                                                    {
                                                        findViewById(R.id.name_et).startAnimation(shake);
                                                    }
                                                    if (surname.equals(""))
                                                    {
                                                        findViewById(R.id.surname_et).startAnimation(shake);
                                                    }
                                                    //findViewById(R.id.client_info_container).startAnimation(shake);
                                                }
                                                //    Toast.makeText(ClientsActivity.this, R.string.at_least_one_of_name_surname_or_email_must_be_present, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else
                                        {
                                            if ((codice_destinatario.equals("") || codice_destinatario
                                                    .length() != 7))
                                            {
                                                findViewById(R.id.codice_destinatario_et).startAnimation(shake);
                                            }
                                            else
                                            {
                                                findViewById(R.id.pec_et).startAnimation(shake);
                                            }
                                           /* findViewById(R.id.client_info_container).startAnimation(shake);
                                            Toast.makeText(ClientsActivity.this,"wrong pec od code", Toast.LENGTH_SHORT).show();*/
                                        }
                                    }
                                    else
                                    {
                                        findViewById(R.id.vat_number_et).startAnimation(shake);
                                   /*     findViewById(R.id.client_info_container).startAnimation(shake);
                                        Toast.makeText(ClientsActivity.this, R.string.vat_number_must_be_present+ "and longer than 11 digits", Toast.LENGTH_SHORT).show();
                                  */
                                    }
                                }
                                else
                                {
                                    findViewById(R.id.client_info_container).startAnimation(shake);
                                    Toast.makeText(ClientsActivity.this, "empty field", Toast.LENGTH_SHORT)
                                         .show();
                                }
                            }
                        }
                        else if (currentClient.getCompany_id() == -1)
                        {
                            Toast.makeText(ClientsActivity.this, R.string.vat_number_must_be_declared, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else if (addPersonalInfo.isActivated())
                        {
                            if (currentClient.getCompany_id() == -1)
                            {
                                String name = ((CustomEditText) findViewById(R.id.name_et)).getText()
                                                                                           .toString()
                                                                                           .replaceAll("'", "\'");
                                String surname = ((CustomEditText) findViewById(R.id.surname_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String email = ((CustomEditText) findViewById(R.id.email_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String company_name = "";
                                company_name = company_name.replaceAll("'", "''");
                                String vat_number = ((CustomEditText) findViewById(R.id.vat_number_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String address = (((CustomEditText) findViewById(R.id.address_et_p))
                                        .getText()
                                        .toString()).replaceAll("'", "\'");
                                address = address.replaceAll("'", "''");
                                String postal_code = ((CustomEditText) findViewById(R.id.postal_code_et_p))
                                        .getText()
                                        .toString()
                                        .replace("'", "\'");
                                String country = ((CustomEditText) findViewById(R.id.country_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                country = country.replaceAll("'", "''");
                                String city = ((CustomEditText) findViewById(R.id.city_et_p)).getText()
                                                                                             .toString()
                                                                                             .replaceAll("'", "\'");
                                city = city.replaceAll("'", "''");
                                String codicefiscale = ((CustomEditText) findViewById(R.id.codice_fiscale_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String provincia = ((CustomEditText) findViewById(R.id.provincia_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String codice_destinatario = ((CustomEditText) findViewById(R.id.codice_destinatario_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String pec = ((CustomEditText) findViewById(R.id.pec_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");

                                if (!codicefiscale.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codice_destinatario.equals("") || !pec.equals(""))
                                )
                                {
                                    if (!codicefiscale.equals("") && vat_number.length() == 16)
                                    {
                                        if (!vat_number.equals("") && vat_number.length() == 11)
                                        {
                                            if ((!codice_destinatario.equals("") && codice_destinatario
                                                    .length() == 7) || (!pec.equals("") && pec.length() > 1))
                                            {
                                                if (!name.equals("") || !email.equals("") || !surname
                                                        .equals(""))
                                                {
                                                    if (!email.equals(""))
                                                    {
                                                        if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                                                        {
                                                            ////Toast.makeText(getBaseContext(),"Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    int company_id;
                                                    currentClient.setName(name);
                                                    currentClient.setSurname(surname);
                                                    currentClient.setEmail(email);
                                                    company_id = dbA.insertCompany(company_name, address, vat_number, postal_code, city, country, codicefiscale, provincia, codice_destinatario, pec);
                                                    currentClient.setCompany_id(company_id);
                                                    currentClient.setCompany_name(company_name);
                                                    currentClient.setCompany_address(address);
                                                    currentClient.setCompany_vat_number(vat_number);
                                                    currentClient.setCompany_postal_code(postal_code);
                                                    currentClient.setCompany_city(city);
                                                    currentClient.setCompany_country(country);
                                                    currentClient.setCodice_fiscale(codicefiscale);
                                                    currentClient.setProvincia(provincia);
                                                    currentClient.setCodice_destinatario(codice_destinatario);
                                                    currentClient.setPec(pec);
                                                    dbA.updateClientData(currentClient);


                                                    if (!clientsAdapter.searchMode)
                                                    {
                                                        clientsAdapter.updateDataSet();
                                                    }
                                                    closeModifyMode();
                                                    Toast.makeText(ClientsActivity.this, R.string.client_updated, Toast.LENGTH_SHORT)
                                                         .show();
                                                }
                                                else
                                                {
                                                    if (email.equals(""))
                                                    {
                                                        findViewById(R.id.email_et).startAnimation(shake);
                                                    }
                                                    if (name.equals(""))
                                                    {
                                                        findViewById(R.id.name_et).startAnimation(shake);
                                                    }
                                                    if (surname.equals(""))
                                                    {
                                                        findViewById(R.id.surname_et).startAnimation(shake);
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                if ((codice_destinatario.equals("") || codice_destinatario
                                                        .length() != 7))
                                                {
                                                    findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                                }
                                                else
                                                {
                                                    findViewById(R.id.pec_et_p).startAnimation(shake);
                                                }
                                            }
                                        }
                                        else
                                        {
                                            findViewById(R.id.vat_number_et_p).startAnimation(shake);

                                        }
                                    }
                                    else
                                    {
                                        findViewById(R.id.codice_fiscale_et_p).startAnimation(shake);
                                    }
                                }
                                else
                                {
                                    findViewById(R.id.client_info_container).startAnimation(shake);
                                    Toast.makeText(ClientsActivity.this, "empty field", Toast.LENGTH_SHORT)
                                         .show();
                                }
                            }
                            else
                            {
                                String name = ((CustomEditText) findViewById(R.id.name_et)).getText()
                                                                                           .toString()
                                                                                           .replaceAll("'", "\'");
                                String surname = ((CustomEditText) findViewById(R.id.surname_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String email = ((CustomEditText) findViewById(R.id.email_et))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String company_name = "";
                                company_name = company_name.replaceAll("'", "''");
                                String vat_number = ((CustomEditText) findViewById(R.id.vat_number_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String address = (((CustomEditText) findViewById(R.id.address_et_p))
                                        .getText()
                                        .toString()).replaceAll("'", "\'");
                                address = address.replaceAll("'", "''");
                                String postal_code = ((CustomEditText) findViewById(R.id.postal_code_et_p))
                                        .getText()
                                        .toString()
                                        .replace("'", "\'");
                                String country = ((CustomEditText) findViewById(R.id.country_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                country = country.replaceAll("'", "''");
                                String city = ((CustomEditText) findViewById(R.id.city_et_p)).getText()
                                                                                             .toString()
                                                                                             .replaceAll("'", "\'");
                                city = city.replaceAll("'", "''");
                                String codicefiscale = ((CustomEditText) findViewById(R.id.codice_fiscale_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String provincia = ((CustomEditText) findViewById(R.id.provincia_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String codice_destinatario = ((CustomEditText) findViewById(R.id.codice_destinatario_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");
                                String pec = ((CustomEditText) findViewById(R.id.pec_et_p))
                                        .getText()
                                        .toString()
                                        .replaceAll("'", "\'");

                                if (!codicefiscale.equals("") &&
                                        !vat_number.equals("") &&
                                        !address.equals("") &&
                                        !postal_code.equals("") &&
                                        !country.equals("") &&
                                        !city.equals("") &&
                                        !provincia.equals("") &&
                                        (!codice_destinatario.equals("") || !pec.equals(""))
                                )
                                {
                                    if (!codicefiscale.equals("") && vat_number.length() == 16)
                                    {
                                        if (!vat_number.equals("") && vat_number.length() == 11)
                                        {
                                            if ((!codice_destinatario.equals("") && codice_destinatario
                                                    .length() == 7) || (!pec.equals("") && pec.length() > 1))
                                            {
                                                if (!name.equals("") || !email.equals("") || !surname
                                                        .equals(""))
                                                {
                                                    if (!email.equals(""))
                                                    {
                                                        if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                                                        {
                                                            //Toast.makeText(getBaseContext(),"Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    currentClient.setName(name);
                                                    currentClient.setSurname(surname);
                                                    currentClient.setEmail(email);
                                                    currentClient.setCompany_id(currentClient.getCompany_id());
                                                    currentClient.setCompany_name(company_name);
                                                    currentClient.setCompany_address(address);
                                                    currentClient.setCompany_vat_number(vat_number);
                                                    currentClient.setCompany_postal_code(postal_code);
                                                    currentClient.setCompany_city(city);
                                                    currentClient.setCompany_country(country);
                                                    currentClient.setCodice_fiscale(codicefiscale);
                                                    currentClient.setProvincia(provincia);
                                                    currentClient.setCodice_destinatario(codice_destinatario);
                                                    currentClient.setPec(pec);
                                                    dbA.updateClientData(currentClient);
                                                    if (!clientsAdapter.searchMode)
                                                    {
                                                        clientsAdapter.updateDataSet();
                                                    }
                                                    //closeModifyMode();
                                                    //Toast.makeText(ClientsActivity.this, "Client Updated", //Toast.LENGTH_SHORT).show();

                                                    //Toast.makeText(ClientsActivity.this, "You just printed an invoice, nice!", //Toast.LENGTH_SHORT).show();
                                                    Intent myIntent = new Intent(ClientsActivity.this, PaymentActivity.class);
                                                    myIntent.putExtra("username", intent.getStringExtra("username"));
                                                    myIntent.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                                                    myIntent.putExtra("billId", intent.getIntExtra("billId", -1));
                                                    myIntent.putExtra("orderNumber", intent.getIntExtra("orderNumber", -1) + 2);
                                                    myIntent.putExtra("customerModify", intent.getBooleanExtra("customerModify", false));
                                                    myIntent.putExtra("modifyPosition", intent.getIntExtra("modifyPosition", -1));
                                                    myIntent.putExtra("cashListIndex", intent.getIntExtra("cashListIndex", -1));
                                                    myIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                                                    myIntent.putExtra("userId", userId);
                                                    myIntent.putExtra("userType", userType);
                                                    myIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                                                    if (currentClient != null)
                                                    {
                                                        myIntent.putExtra("customer", currentClient.getClient_id());
                                                        myIntent.putExtra("email", currentClient.getEmail());
                                                    }
                                                    else
                                                    {
                                                        myIntent.putExtra("customer", -1);
                                                    }

                                                    startActivity(myIntent);
                                                    finish();
                                                }
                                                else
                                                {
                                                    if (email.equals(""))
                                                    {
                                                        findViewById(R.id.email_et).startAnimation(shake);
                                                    }
                                                    if (name.equals(""))
                                                    {
                                                        findViewById(R.id.name_et).startAnimation(shake);
                                                    }
                                                    if (surname.equals(""))
                                                    {
                                                        findViewById(R.id.surname_et).startAnimation(shake);
                                                    }
                                                    Toast.makeText(ClientsActivity.this, R.string.at_least_one_of_name_surname_or_email_must_be_present, Toast.LENGTH_SHORT)
                                                         .show();
                                                }
                                            }
                                            else
                                            {
                                                if ((codice_destinatario.equals("") || codice_destinatario
                                                        .length() != 7))
                                                {
                                                    findViewById(R.id.codice_destinatario_et_p).startAnimation(shake);
                                                }
                                                else
                                                {
                                                    findViewById(R.id.pec_et_p).startAnimation(shake);
                                                }

                                            }
                                        }
                                        else
                                        {
                                            findViewById(R.id.vat_number_et_p).startAnimation(shake);

                                        }
                                    }
                                    else
                                    {
                                        findViewById(R.id.codice_fiscale_et_p).startAnimation(shake);
                                    }
                                }
                                else
                                {
                                    findViewById(R.id.client_info_container).startAnimation(shake);
                                    Toast.makeText(ClientsActivity.this, "empty field", Toast.LENGTH_SHORT)
                                         .show();
                                }
                            }


                        }
                        else if (currentClient.getCompany_id() == -1)
                        {
                            Toast.makeText(ClientsActivity.this, R.string.vat_number_must_be_declared, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else
                        {
                            //print
                            //Toast.makeText(ClientsActivity.this, "You just printed an invoice, nice!", //Toast.LENGTH_SHORT).show();
                            Intent myIntent = new Intent(ClientsActivity.this, PaymentActivity.class);
                            myIntent.putExtra("username", intent.getStringExtra("username"));
                            myIntent.putExtra("isAdmin", intent.getIntExtra("isAdmin", -1));
                            myIntent.putExtra("billId", intent.getIntExtra("billId", -1));
                            myIntent.putExtra("orderNumber", intent.getIntExtra("orderNumber", -1) + 2);
                            myIntent.putExtra("customerModify", intent.getBooleanExtra("customerModify", false));
                            myIntent.putExtra("modifyPosition", intent.getIntExtra("modifyPosition", -1));
                            myIntent.putExtra("cashListIndex", intent.getIntExtra("cashListIndex", -1));
                            myIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                            myIntent.putExtra("userId", userId);
                            myIntent.putExtra("userType", userType);
                            myIntent.putExtra("tableNumber", intent.getIntExtra("tableNumber", -1));
                            if (currentClient != null)
                            {
                                myIntent.putExtra("customer", currentClient.getClient_id());
                                myIntent.putExtra("email", currentClient.getEmail());
                            }
                            else
                            {
                                myIntent.putExtra("customer", -1);
                            }

                            startActivity(myIntent);
                            finish();
                        }
                        break;
                    //client not registered, I just want to send him an email
                    case 5:
                        String email = ((CustomEditText) findViewById(R.id.email_et)).getText()
                                                                                     .toString()
                                                                                     .replaceAll("'", "\'");
                        if (email.equals(""))
                        {
                            //Toast.makeText(ClientsActivity.this, "Insert an Email.", //Toast.LENGTH_SHORT).show();
                        }
                        else if (!email.equals(""))
                        {
                            if (!Pattern.matches("^[-a-zA-Z0-9_.]+@[a-zA-Z]+\\.[a-zA-Z]{2,5}$", email))
                            {
                                //Toast.makeText(getBaseContext(),"Not a Valid E-Mail", //Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                String mailto = "mailto:" + email +
                                        "?subject=" + Uri.encode("Scontrino Burgheria") +
                                        "&body=" + Uri.encode("corpo del messaggio contenente lo scontrino o fattura");
                                emailIntent.setData(Uri.parse(mailto));

                                try
                                {
                                    startActivity(emailIntent);
                                    finish();
                                }
                                catch (ActivityNotFoundException e)
                                {
                                    e.printStackTrace();
                                    //Toast.makeText(ClientsActivity.this, "No application found for sending emails", //Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        break;
                    case 6:
                        searchClients.setText(R.string.search_clients);
                        clientsAdapter.setSearchMode(false);
                        mode = SELECTION_MODE;
                        ClientsActivity.this.findViewById(R.id.title_tv)
                                            .setVisibility(View.VISIBLE);
                        ClientsActivity.this.findViewById(R.id.hline_rv_top)
                                            .setVisibility(View.VISIBLE);
                        ((CustomEditText) ClientsActivity.this.findViewById(R.id.search_et)).setText("");
                        ClientsActivity.this.findViewById(R.id.search_et).setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }


                /*if(intent.getAction().equals("selectClient")){
                    String sender = intent.getStringExtra("sender");
                    if(sender.equals("payment-invoice")){
                        TemporaryOrder.setClient(selectedClient);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }*/
            }
        });
    }


    // ------- MODIFY CLIENT ------- //

    /**
     * @param client - selected client
     *               Triggered when client is clicked.
     */
    public void openModifyMode(ClientInfo client)
    {

        name_et.setText(client.getName());
        surname_et.setText(client.getSurname());
        email_et.setText(client.getEmail());


        // activate the button to add more fidelity credits for a specific client
        fidelity_code_text.setText(String.format("ID: %s", client.getFidelity_id()));

        Fidelity clientFidelity = dbA.fetchFidelityById(client.getFidelity_id());
        show_fidelity_credit.setText(String.format("%.0f FC", clientFidelity.getValue()));

        add_fidelity_credit_bt.setBackgroundColor(getResources().getColor(R.color.green_2));
        add_fidelity_credit_bt.setEnabled(true);

        // save this for the processFinish on route /saveBill
        fidelitySelectedClient = client;


        if (addCompanyInfo.isActivated())
        {
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

        if (addPersonalInfo.isActivated())
        {
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

        if (client.getCompany_id() > 0)
        {
            if (!client.getCompany_name().equals("") && client.getCodice_fiscale().equals(""))
            {
                if (!addCompanyInfo.isActivated())
                {
                    addCompanyInfo.performClick();
                }

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
            }

            else
            {
                if (!addPersonalInfo.isActivated())
                {
                    addPersonalInfo.performClick();
                }
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

        ArrayList<String> discounts = dbA.checkIfClientHasDiscount(client.getClient_id());
        if (discounts != null)
        {
            //client has just one discount
            if (discounts.size() != 0)
            {
                if (discounts.size() == 1)
                {
                    //discountButton must show percentage
                    if (dbA.getDiscountMode(discounts.get(0)) == 1)
                    {
                        setClientType.setText(discounts.get(0));
                        setClientType.setActivated(true);
                        setDiscount.setText(R.string.set_discount);
                        setDiscount.setActivated(false);
                    }
                    //clientTypeButton must show name
                    else if (dbA.getDiscountMode(discounts.get(0)) == 0)
                    {
                        int value = dbA.getDiscountValue(discounts.get(0));
                        setDiscount.setText(value + "%");
                        setDiscount.setActivated(true);
                        setClientType.setText(R.string.set_client_type);
                        setClientType.setActivated(false);
                    }
                }
                //more than one (maximum 2 discounts)
                else
                {
                    for (String discount : discounts)
                    {
                        //discountButton must show percentage
                        if (dbA.getDiscountMode(discount) == 1)
                        {
                            setClientType.setText(discount);
                            setClientType.setActivated(true);
                        }
                        //clientTypeButton must show discount name
                        else if (dbA.getDiscountMode(discount) == 0)
                        {
                            int value1 = dbA.getDiscountValue(discount);
                            setDiscount.setText(value1 + "%");
                            setDiscount.setActivated(true);
                        }
                    }
                }
            }
            else
            {
                setClientType.setText(R.string.set_client_type);
                setClientType.setActivated(false);
                setDiscount.setText(R.string.set_discount);
                setDiscount.setActivated(false);
            }
        }
        else
        {
            setClientType.setText(R.string.set_client_type);
            setClientType.setActivated(false);
            setDiscount.setText(R.string.set_discount);
            setDiscount.setActivated(false);
        }

        addNewClient.setText(R.string.apply_changes);
        /*if(mode != 2 && mode!=3 && mode != 4 && mode != 5)
            setMode(MODIFY_RESERVATION_MODE);*/
        if (mode == 0 || mode == 1)
        {
            setMode(MODIFY_MODE);
        }
    }


    public void closeModifyMode()
    {
        name_et.setText("");
        surname_et.setText("");
        email_et.setText("");

        // dectivate the button to add more fidelity credits for a specific client
        fidelity_code_text.setText("");
        ;
        show_fidelity_credit.setText("");
        add_fidelity_credit_bt.setBackgroundColor(getResources().getColor(R.color.dark_gray_2));
        add_fidelity_credit_bt.setEnabled(false);


        if (addCompanyInfo.isActivated())
        {
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
            addCompanyInfo.performClick();
        }

        if (addPersonalInfo.isActivated())
        {
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
        if (setClientType.isActivated())
        {
            setClientType.setActivated(false);
            setClientType.setText(R.string.set_client_type);
        }
        if (setDiscount.isActivated())
        {
            setDiscount.setActivated(false);
            setDiscount.setText(R.string.set_discount);
        }

        setCurrentClient(null);
        addNewClient.setText(R.string.add_new_client);
        setMode(SELECTION_MODE);
    }


    // ------- FIDELITY CREDIT/PACKAGE -------- //

    // setup the popup that allows to buy new credits
    private void setupAddFidelityCreditPopup()
    {
        // this represent a simple trick
        // with a negative catId number, the fidelity package can be saved in the button table,
        // but will not be represented in the main operative layout
        final String FIDELITY_CREDIT_CATEGORY_ID = "-5";

        final LayoutInflater inflater  = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View           popupView = inflater.inflate(R.layout.popup_fidelity_package_list, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        // setup the fidelity package adapter
        RecyclerView fidelityPackage_rv = popupView.findViewById(R.id.fidelity_package_rv);
        fidelityPackage_rv.setLayoutManager(new LinearLayoutManager(ClientsActivity.this, LinearLayoutManager.VERTICAL, false));
        fidelityPackage_rv.setHasFixedSize(true);

        fidelityPackageAdapter = new FidelityPackageAdapter(this, dbA, this);
        fidelityPackage_rv.setAdapter(fidelityPackageAdapter);


        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CustomEditText fidelity_package_creditAmount_et = popupView.findViewById(R.id.fidelity_package_creditAmount_et);
                CustomEditText fidelity_package_price_et        = popupView.findViewById(R.id.fidelity_package_price_et);
                CustomEditText fidelity_pacakge_name_et         = popupView.findViewById(R.id.fidelity_pacakge_name_et);


                String creditAmount_input = fidelity_package_creditAmount_et.getText().toString().trim();
                String price_input        = fidelity_package_price_et.getText().toString().trim();
                String name_input         = fidelity_pacakge_name_et.getText().toString().trim();

                // if no input has been given, just dismiss this
                if (creditAmount_input.isEmpty() && price_input.isEmpty() && name_input.isEmpty())
                {
                    popupWindow.dismiss();
                }

                // else, we have to check if the input is correct
                // but first, check that if one field is empty, the other is not
                else if (creditAmount_input.isEmpty() || price_input.isEmpty() || name_input.isEmpty())
                {
                    DialogCreator.error(ClientsActivity.this, "Please input name, price and amount.\nOtherwise, leave empty");
                }

                else if (name_input.contains("(") || name_input.contains(")"))
                {
                    DialogCreator.error(ClientsActivity.this, "Sorry! Characters '(' and ')' are not allowed");
                }

                // if anything is correct, we can insert a new fidelity package
                // TODO add checking of input format
                else if (StaticValue.blackbox)
                {
                    // there is no table for a fidelity package,
                    // but we use the button table (with the products)
                    // and put all the fidelity credit products (the packages)
                    // in the button table, under the special category of value -5

                    RequestParam params = new RequestParam();

                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);

                    // the title is fundamental, since it will be used to recreate the FiscalPackage model class
                    params.add("title", String.format("%s (%s) FC", name_input, creditAmount_input));
                    params.add("subtitle", "");
                    params.add("color", "-690665");
                    params.add("position", "1");
                    params.add("catID", FIDELITY_CREDIT_CATEGORY_ID);
                    params.add("isCat", "0");
                    params.add("price", price_input);
                    params.add("vat", "1"); // TODO which VAT????
                    params.add("printerId", "-1"); // TODO
                    params.add("fidelityDiscount", "false");
                    params.add("fidelityCredit", "0");

                    callHttpHandler("/insertButton5", params);

                    // restore the edit text to empty
                    fidelity_package_creditAmount_et.setText("");
                    fidelity_package_price_et.setText("");

                }

                else
                {
                    // TODO
                    // add a button offline
                }

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

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((ClientsActivity) context).findViewById(R.id.main), 0, 0, 0);
    }


    /**
     * Upon selecting a fidelity package to buy in the fidelity package popup,
     * this function is called. The package is a button (like any other product),
     * and a new bill is created, with the only product being the fidelity package
     *
     * @param fidelityPackage the fidelityPackage that is being bought
     */
    public void saveFidelityPackageBill(FidelityPackage fidelityPackage)
    {
        // create a fake CashButtonLayout
        CashButtonLayout creditProduct = new CashButtonLayout();
        creditProduct.setID(-1); // TODO
        creditProduct.setProductId(fidelityPackage.getButtonId());
        creditProduct.setQuantity(1);
        creditProduct.setPosition(1);

        // NOTE: the title is fundamental, since it's the only element that will store the info on the credit amount
        creditProduct.setTitle(String.format("%s (%d) FC", fidelityPackage.getName(), fidelityPackage.getCreditAmount()));
        creditProduct.setPrice((float) fidelityPackage.getPrice());
        creditProduct.setHomage(0);
        creditProduct.setIsPaid(false);
        creditProduct.setIsDelete(false);
        creditProduct.setClientPosition(1);
        creditProduct.setVat(1); // TODO

        // TODO
        // it would be nice if a customer, with the detail of the selected client, is passed
        Customer thisClient = new Customer();
        thisClient.setId(currentClient.getClient_id());
        thisClient.setPosition(1);
        thisClient.setDescription(currentClient.getName() + " " + currentClient.getSurname());

        // create two array with only one element,
        // the fidelity package and the customer
        Gson      gson = new Gson();
        ArrayList cp   = new ArrayList<CashButtonLayout>();
        cp.add(creditProduct);
        ArrayList cust = new ArrayList<Customer>();
        cust.add(thisClient);

        // then convert the array with the product and the customer
        // in a JSON string object, in order to send it to the blackbox
        String creditProducts = gson.toJson(cp);
        String emptyModifiers = gson.toJson(new HashMap<String, ArrayList<CashButtonListLayout>>());
        String customers      = gson.toJson(cust);


        RequestParam params = new RequestParam();
        params.add("username", username);
        params.add("androidId", StaticValue.androidId);
        params.add("billId", String.valueOf(-1));               // here both billId and orderNumber have -1 as value.
        params.add("orderNumber", String.valueOf(orderNumber + 1));           // this allow the blackbox to generate a new billId
        params.add("total", String.valueOf(fidelityPackage.getPrice()));
        params.add("products", creditProducts);
        params.add("customers", customers);
        params.add("modifiers", emptyModifiers);
        params.add("totalDiscount", String.valueOf(0));
        params.add("from", "clientActivity");
        params.add("cashListIndex", String.valueOf(1)); // TODO what's this??

        callHttpHandler("/saveBill", params);

        // after the bill is saved, update the order number,
        // thus if a new
    }


    // --------- APP GENERIC FUN ------- //


    public void resetPinpadTimer(int type)
    {
        TimerManager.stopPinpadAlert();
        TimerManager.setContext(getApplicationContext());
        Intent intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);
        Intent intent         = getIntent();
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


    public void callHttpHandler(String route, RequestParam params)
    {
        HttpHandler httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
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
                        if (!(((Activity) context).getCurrentFocus() instanceof EditText) && !keyboard_next_flag)
                        {
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
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
