package com.example.blackbox.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.blackbox.DialogCreator;
import com.example.blackbox.R;
import com.example.blackbox.adapter.ReservationsAdapter;
import com.example.blackbox.adapter.WaitingListAdapter;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.Reservation;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.WaitingListModel;
import com.example.blackbox.server.HttpHandler;
import com.google.gson.Gson;
import com.utils.db.DatabaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.view.View.GONE;

/**
 * Created by Fabrizio on 28/05/2018.
 */

public class ReservationsActivity extends AppCompatActivity implements HttpHandler.AsyncResponse
{

    private static final String TAG = "<ReservationActivity>";

    private RecyclerView reservations_list_rv;
    private DatabaseAdapter dbA;
    private Context context;
    private float density;
    private int dpHeight;
    private int dpWidth;
    private Resources resources;

    private int billId;
    private int orderNumber;
    private int tableNumber = -1;
    private int userId = -1;
    private int userType;
    private String username;
    private int isAdmin;

    public static final int RESERVATIONS_MODE = 0;
    public static final int WAITING_LIST_MODE = 1;
    public static final int SEARCH_RESERVATIONS_MODE = 2;
    public static final int MODIFY_RESERVATION_MODE = 3;
    public static final int SEARCH_WAITING_LIST_MODE = 4;
    public static final int MODIFY_WAITING_LIST_MODE = 5;

    int mode = RESERVATIONS_MODE;

    public void setMode(int value) {mode = value;}

    private CustomButton searchReservations;
    private CustomEditText search_et;
    private CustomButton insertReservations;
    private CustomButton setReservationDate;
    private CustomButton setReservationTime;
    private CustomTextView newReservationDate;
    private CustomTextView reservationsTV;
    private CustomTextView newReservationTV;
    private CustomEditText telephoneNumber;
    private View hline;
    private ReservationsAdapter reservationsAdapter;
    private WaitingListAdapter waitingListAdapter;
    private boolean keyboard_next_flag = false;

    private String name = "";
    private String surname = "";
    private int adults = 0;
    private int children = 0;
    private int disabled = 0;
    private Calendar time;
    private Date nowDate;

    private Reservation currentReservation = null;
    private boolean dividerAdded = false;


    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Hides app title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_reservations);

        context = this;
        resources = getResources();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;

        dbA = new DatabaseAdapter(this);


        Intent intent = getIntent();
        billId = intent.getIntExtra("billId", -1);
        userId = intent.getIntExtra("userId", -1);
        userType = intent.getIntExtra("userType", -1);
        tableNumber = intent.getIntExtra("tableNumber", -1);
        orderNumber = intent.getIntExtra("orderNumber", -1);
        username = intent.getStringExtra("username");
        isAdmin = intent.getIntExtra("isAdmin", -1);

        searchReservations = findViewById(R.id.search_reservations_button);
        search_et =  ReservationsActivity.this.findViewById(R.id.search_reservation_et);
        insertReservations =  findViewById(R.id.insert_reservation_button);
        setReservationTime =  findViewById(R.id.set_reservation_time_button);
        setReservationDate =  findViewById(R.id.set_reservation_date_button);
        reservationsTV = findViewById(R.id.title_tv);
        newReservationTV =  findViewById(R.id.title_right_tv);
        telephoneNumber = findViewById(R.id.new_res_input_telephone);
        hline = findViewById(R.id.hline4_right);

        reservations_list_rv = findViewById(R.id.reservations_rv);
        reservations_list_rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.divider_black_for_reservation));

        reservationsAdapter = new ReservationsAdapter(dbA, context);
        reservations_list_rv.setAdapter(reservationsAdapter);

        if (!dividerAdded)
        {
            reservations_list_rv.addItemDecoration(divider);
            dividerAdded = true;
        }


        // initialize the time variable
        time = Calendar.getInstance();




        ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(resources.getString(R.string.adults_children_disabled, adults, children, disabled));

        //search reservation
        searchReservations.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mode == RESERVATIONS_MODE)
                {
                    mode = SEARCH_RESERVATIONS_MODE;
                    search_et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    search_et.setSingleLine();
                    view.setActivated(!view.isActivated());

                    if (view.isActivated())
                    {
                        reservationsAdapter.setSearchMode(true);

                        findViewById(R.id.hline1_search).setVisibility(View.VISIBLE);
                        search_et.setVisibility(View.VISIBLE);
                        findViewById(R.id.hline2_search).setVisibility(View.VISIBLE);
                        findViewById(R.id.title_tv).setVisibility(GONE);
                        findViewById(R.id.hline1).setVisibility(GONE);
                        search_et.requestFocus();
                        ((InputMethodManager) getSystemService(ReservationsActivity.this.INPUT_METHOD_SERVICE)).showSoftInput(search_et, InputMethodManager.SHOW_IMPLICIT);

                        search_et.addTextChangedListener(new TextWatcher()
                        {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            private Timer timer = new Timer();
                            private final long DELAY = 500; // milliseconds

                            @Override
                            public void afterTextChanged(final Editable s)
                            {
                                timer.cancel();
                                timer = new Timer();
                                timer.schedule( new TimerTask()
                                {
                                    @Override
                                    public void run()
                                    {
                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                                { reservationsAdapter.searchReservation(s.toString()); }
                                            });
                                        }
                                }, DELAY);

                            }
                        });
                    }

                    else
                    {
                        mode = RESERVATIONS_MODE;
                        search_et.setText("");
                        search_et.setVisibility(GONE);
                        findViewById(R.id.hline2_search).setVisibility(GONE);
                        findViewById(R.id.hline1_search).setVisibility(GONE);
                        findViewById(R.id.hline1).setVisibility(View.VISIBLE);
                        findViewById(R.id.title_tv).setVisibility(View.VISIBLE);
                        reservationsAdapter.setSearchMode(false);
                    }
                }
                else if (mode == WAITING_LIST_MODE)
                {
                    mode = SEARCH_WAITING_LIST_MODE;
                    search_et.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    search_et.setSingleLine();
                    view.setActivated(!view.isActivated());
                    if (view.isActivated())
                    {
                        waitingListAdapter.setSearchMode(true);
                        findViewById(R.id.hline1_search).setVisibility(View.VISIBLE);
                        search_et.setVisibility(View.VISIBLE);
                        findViewById(R.id.hline2_search).setVisibility(View.VISIBLE);
                        findViewById(R.id.title_tv).setVisibility(GONE);
                        findViewById(R.id.hline1).setVisibility(GONE);
                        search_et.requestFocus();
                        ((InputMethodManager) getSystemService(ReservationsActivity.this.INPUT_METHOD_SERVICE))
                                .showSoftInput(search_et, InputMethodManager.SHOW_IMPLICIT);

                        search_et.addTextChangedListener(new TextWatcher()
                        {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after)
                            {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count)
                            {
                            }

                            private Timer timer = new Timer();
                            private final long DELAY = 500; // milliseconds

                            @Override
                            public void afterTextChanged(final Editable s)
                            {
                                timer.cancel();
                                timer = new Timer();
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
                                                        waitingListAdapter.searchWaitingListElement(s
                                                                .toString());
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
                        mode = WAITING_LIST_MODE;
                        search_et.setText("");
                        search_et.setVisibility(GONE);
                        findViewById(R.id.hline2_search).setVisibility(GONE);
                        findViewById(R.id.hline1_search).setVisibility(GONE);
                        findViewById(R.id.hline1).setVisibility(View.VISIBLE);
                        findViewById(R.id.title_tv).setVisibility(View.VISIBLE);
                        waitingListAdapter.setSearchMode(false);
                    }
                }
            }
        });


        // add a swipe gesture on the TV (the upper bar, with the Reservations/Waiting List),
        // such that a swipe will move from reservation to waiting_list, and vice verse
        reservationsTV.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            public void onSwipeLeft()
            {
                if (mode == RESERVATIONS_MODE)
                    { mode = WAITING_LIST_MODE; }
                else
                    { mode = RESERVATIONS_MODE; }
                switchMode();
            }

            public void onSwipeRight()
            {
                if (mode == WAITING_LIST_MODE)
                { mode = RESERVATIONS_MODE; }
                else
                { mode = WAITING_LIST_MODE; }
                switchMode();
            }
        });


        setupNewReservationsButtons();

        setupKillOKButtons();

        final Handler ha = new Handler();
        ha.postDelayed(new Runnable()
        {
            @Override
            public void run()
            { fireResWaitingListPopup(); }

        }, 100);
    }


    @Override
    public void processFinish(String output)
    {

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
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (success)
        {
            try
            {
                jsonObject = new JSONObject(output);

                switch (route)
                {
                    case "insertReservation":
                    if (jsonObject.getBoolean("check"))
                    {
                        Reservation res = Reservation.fromJson(jsonObject.getJSONObject("reservation"));
                        dbA.addReservation(res, nowDate);

                        reservationsAdapter.refreshReservationList();

                        dbA.updateChecksumForTable("reservation", jsonObject.getString("reservationChecksum"));
                    }

                    else
                    { Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, jsonObject.getString("reason")), Toast.LENGTH_SHORT).show(); }

                    break;


                    case "deleteReservation":
                        int id = jsonObject.getInt("reservationId");

                        dbA.deleteReservation(id);
                        reservationsAdapter.refreshReservationList();

                        break;


                    case "modifyReservation":
                        if (jsonObject.getBoolean("check"))
                        { dbA.updateChecksumForTable("reservation", jsonObject.getString("reservationChecksum")); }

                        else
                        { Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, jsonObject.getString("reason")), Toast.LENGTH_SHORT).show(); }


                    // --------------- WAITING LIST -------------------- //

                    case "insertWaitingList":
                        if (jsonObject.getBoolean("check"))
                        {
                            WaitingListModel wt = WaitingListModel.fromJson(jsonObject.getJSONObject("waitingList"));
                            dbA.addWaitingListElement(wt);

                            waitingListAdapter.refreshWaitingList();

                            dbA.updateChecksumForTable("waiting_list", jsonObject.getString("waitingListChecksum"));
                        }

                        else
                            { Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, jsonObject.getString("reason")), Toast.LENGTH_SHORT).show(); }

                        break;


                    case "deleteWaitingList":
                        int wtID = jsonObject.getInt("waitingListId");

                        dbA.deleteWaitingListElement(wtID);
                        waitingListAdapter.refreshWaitingList();

                        break;


                    case "modifyWaitingList":
                        if (jsonObject.getBoolean("check"))
                            { dbA.updateChecksumForTable("waiting_list", jsonObject.getString("waitingListChecksum")); }

                        else
                            { Toast.makeText(getApplicationContext(), getString(R.string.route_check_false, route, jsonObject.getString("reason")), Toast.LENGTH_SHORT).show(); }


                    default:
                        Toast.makeText(getApplicationContext(), "Got unkown route: " + route, Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            catch (JSONException e)
                { e.printStackTrace(); }
        }

        else
            { Toast.makeText(this, getString(R.string.error_blackbox_comm, StaticValue.blackboxInfo.getName(), StaticValue.blackboxInfo.getAddress()), Toast.LENGTH_LONG).show(); }
    }



    public void setupNewReservationsButtons()
    {
        ImageButton adultsPlus = findViewById(R.id.adults_plus_button);
        ImageButton adultsMinus = findViewById(R.id.adults_minus_button);
        ImageButton childrenPlus = findViewById(R.id.children_plus_button);
        ImageButton childrenMinus = findViewById(R.id.children_minus_button);
        ImageButton disabledPlus = findViewById(R.id.disabled_plus_button);
        ImageButton disabledMinus = findViewById(R.id.disabled_minus_button);
        CustomEditText newName = findViewById(R.id.new_res_input_name);
        CustomEditText newSurname = findViewById(R.id.new_res_input_surname);
        CustomEditText telephoneNumber = findViewById(R.id.new_res_input_telephone);
        newReservationDate = findViewById(R.id.new_reservation_date);
        RelativeLayout newResLayout = findViewById(R.id.new_res_name_layout);


        adultsPlus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                adults += 1;
                ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(resources.getString(R.string.adults_children_disabled, adults, children, disabled));
            }
        });


        adultsMinus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (adults > 0) { adults -= 1; }
                ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(resources.getString(R.string.adults_children_disabled, adults, children, disabled));
            }
        });


        childrenPlus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                children += 1;
                ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(resources.getString(R.string.adults_children_disabled, adults, children, disabled));
            }
        });


        childrenMinus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (children > 0) { children -= 1; }
                ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(
                        resources.getString(R.string.adults_children_disabled, adults, children, disabled));
            }
        });

        disabledPlus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                disabled++;
                ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(
                        resources.getString(R.string.adults_children_disabled, adults, children, disabled));
            }
        });

        disabledMinus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (disabled > 0)
                { disabled--; }
                ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(
                        resources.getString(R.string.adults_children_disabled, adults, children, disabled));
            }
        });




        setReservationDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Calendar currentDate = Calendar.getInstance();
                int year = currentDate.get(Calendar.YEAR);
                int month = currentDate.get(Calendar.MONTH);
                int day = currentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dPicker = new DatePickerDialog(ReservationsActivity.this, R.style.DatePickerTheme,
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2)
                            {
                                time.set(Calendar.YEAR, i);
                                time.set(Calendar.MONTH, i1);
                                time.set(Calendar.DAY_OF_MONTH, i2);
                            }
                        }, year, month, day);

                dPicker.show();
            }
        });


        setReservationTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker = new TimePickerDialog(ReservationsActivity.this,4, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                    {
                        time.set(Calendar.HOUR_OF_DAY, selectedHour);
                        time.set(Calendar.MINUTE, selectedMinute);
                        ((CustomTextView) findViewById(R.id.new_reservation_time)).setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time

                mTimePicker.show();
            }
        });



        insertReservations.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                name = newName.getText().toString().trim();
                surname = newSurname.getText().toString().trim();

                if (name.isEmpty() || surname.isEmpty())
                    { DialogCreator.error(context, getString(R.string.insert_a_valid_client_name)); }

                else if (time == null && mode == RESERVATIONS_MODE)
                    { DialogCreator.error(context, getString(R.string.insert_a_valid_reservation_time)); }

                else if (adults <= 0)
                    { DialogCreator.error(context, getString(R.string.please_add_at_least_one_adult)); }

                else
                {
                    if (mode == RESERVATIONS_MODE)
                    {
                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);

                            params.add(new BasicNameValuePair("name", name));
                            params.add(new BasicNameValuePair("surname", surname));
                            params.add(new BasicNameValuePair("adults", String.valueOf(adults)));
                            params.add(new BasicNameValuePair("children", String.valueOf(children)));
                            params.add(new BasicNameValuePair("disabled", String.valueOf(disabled)));
                            params.add(new BasicNameValuePair("time", String.valueOf(time.getTime().getTime())));
                            params.add(new BasicNameValuePair("telephone", telephoneNumber.getText().toString()));

                            callHttpHandler("/insertReservation", params);
                        }

                        else
                        {
                            Reservation res = new Reservation();

                            res.setName(name);
                            res.setSurname(surname);
                            res.setAdults(adults);
                            res.setChildren(children);
                            res.setDisabled(disabled);
                            res.setTime(time.getTime());
                            res.setReservation_id(dbA.getLatestReservationId() + 1);

                            if (!telephoneNumber.getText().toString().isEmpty())
                                { res.setTelephone(telephoneNumber.getText().toString().trim()); }

                            dbA.addReservation(res, nowDate);

                            reservationsAdapter.refreshReservationList();
                        }

                        resetFields();

                    }

                    else if (mode == WAITING_LIST_MODE)
                    {
                        Calendar now = Calendar.getInstance();

                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);

                            params.add(new BasicNameValuePair("name", name));
                            params.add(new BasicNameValuePair("surname", surname));
                            params.add(new BasicNameValuePair("adults", String.valueOf(adults)));
                            params.add(new BasicNameValuePair("children", String.valueOf(children)));
                            params.add(new BasicNameValuePair("disabled", String.valueOf(disabled)));
                            params.add(new BasicNameValuePair("time", String.valueOf(now.getTime().getTime())));

                            callHttpHandler("/insertWaitingList", params);
                        }

                        else
                        {
                            WaitingListModel res = new WaitingListModel();

                            res.setId(dbA.getLatestWaitingListId() + 1);
                            res.setName(name);
                            res.setSurname(surname);
                            res.setAdults(adults);
                            res.setChildren(children);
                            res.setDisabled(disabled);
                            res.setTime(now.getTime());

                            dbA.addWaitingListElement(res);

                            waitingListAdapter.refreshWaitingList();
                        }

                    }

                    resetFields();
                }
            }
        });
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
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

                    CustomEditText newName = (CustomEditText) findViewById(R.id.new_res_input_name);
                    CustomEditText newSurname = (CustomEditText) findViewById(R.id.new_res_input_surname);

                    StringBuilder sb = new StringBuilder();
                    name = newName.getText().toString();
                    if (!newSurname.getText().toString().equals(""))
                    {
                        surname = newSurname.getText().toString();
                    }

                    if (!name.equals("") || !surname.equals(""))
                    { sb.append(name + " " + surname); }
                    ((CustomTextView) findViewById(R.id.new_reservation_name)).setText(sb.toString());
                    if (mode == MODIFY_RESERVATION_MODE && currentReservation != null)
                    {
                        currentReservation.setName(name);
                        currentReservation.setSurname(surname);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }



    public void fireResWaitingListPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_res_waiting_list_choose, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        CustomButton waitingList = (CustomButton) popupView.findViewById(R.id.waiting_list);
        CustomButton reservations = (CustomButton) popupView.findViewById(R.id.reservations);

        waitingList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                mode = WAITING_LIST_MODE;
                switchMode();

                popupWindow.dismiss();
            }
        });

        reservations.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                mode = RESERVATIONS_MODE;
                switchMode();

                popupWindow.dismiss();
            }
        });


        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);

    }



    public void switchMode()
    {
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.divider_black_for_reservation));

        switch (mode)
        {
            case RESERVATIONS_MODE:
                if (reservationsAdapter == null)
                {
                    reservationsAdapter = new ReservationsAdapter(dbA, context);
                    reservations_list_rv.setAdapter(reservationsAdapter);
                    if (!dividerAdded)
                    {
                        reservations_list_rv.addItemDecoration(divider);
                        dividerAdded = true;
                    }
                }
                else
                {
                    reservations_list_rv.setAdapter(reservationsAdapter);
                    reservationsAdapter.refreshReservationList();
                }

                reservationsTV.setText(R.string.reservations);
                newReservationTV.setText(R.string.new_reservations);
                searchReservations.setText(R.string.search_reservations);
                insertReservations.setText(R.string.insert_reservation);

                setReservationTime.setVisibility(View.VISIBLE);
                setReservationDate.setVisibility(View.VISIBLE);
                hline.setVisibility(View.VISIBLE);
                telephoneNumber.setVisibility(View.VISIBLE);

                //nowDate = reservationsAdapter.formatNowDate();
                break;

            case WAITING_LIST_MODE:
                if (waitingListAdapter == null)
                {
                    waitingListAdapter = new WaitingListAdapter(context, dbA, this);
                    reservations_list_rv.setAdapter(waitingListAdapter);
                    if (!dividerAdded)
                    {
                        reservations_list_rv.addItemDecoration(divider);
                        dividerAdded = true;
                    }
                }
                else
                {
                    reservations_list_rv.setAdapter(waitingListAdapter);
                    waitingListAdapter.refreshWaitingList();
                }

                reservationsTV.setText(R.string.waiting_list);
                newReservationTV.setText(R.string.new_waiting_list_element);
                searchReservations.setText(R.string.search_waiting_list);
                insertReservations.setText(R.string.insert_waiting_list_element);

                setReservationTime.setVisibility(View.GONE);
                setReservationDate.setVisibility(View.GONE);
                hline.setVisibility(View.GONE);
                telephoneNumber.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }



    private void resetFields()
    {
        CustomEditText newName = (CustomEditText) findViewById(R.id.new_res_input_name);
        CustomEditText newSurname = (CustomEditText) findViewById(R.id.new_res_input_surname);
        CustomEditText telephoneNumber = (CustomEditText) findViewById(R.id.new_res_input_telephone);

        ((CustomTextView) findViewById(R.id.new_reservation_name)).setText(R.string.new_reservation);
        ((CustomTextView) findViewById(R.id.new_reservation_date)).setText("");
        newReservationDate.setVisibility(GONE);
        ((CustomTextView) findViewById(R.id.new_reservation_time)).setText("00:00");

        newName.setText("");
        newSurname.setText("");
        telephoneNumber.setText("");

        name = "";
        surname = "";
        adults = 0;
        children = 0;
        disabled = 0;
        time = Calendar.getInstance();

        ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(resources.getString(R.string.adults_children_disabled, adults, children, disabled));

        reservationsAdapter.notifyDataSetChanged();
    }




    public void changeActivity()
    {
        Intent intent1 = getIntent();
        int numberBill = intent1.getIntExtra("orderNumber", 1);
        Intent intent = new Intent(ReservationsActivity.this, TableActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("isAdmin", isAdmin);
        intent.setAction("setTable");
        intent.putExtra("orderNumber", numberBill);
        intent.putExtra("tableNumber", tableNumber);
        //intent.putExtra("roomId", 1);
        intent.setAction("operation");

        long lastClose = dbA.getLastClosing();
        Date date = new Date(lastClose);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateText = df2.format(date);

        int newBillId = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateText + "';");
        if (newBillId == -11)
        {
            //saveBillForTable();
            intent.putExtra("billId", billId);
        }
        else
        {
            //updateBill(newBillId);
            intent.putExtra("billId", newBillId);
        }
        startActivity(intent);
        finish();
    }


    public void setupKillOKButtons()
    {
        CustomEditText newName = (CustomEditText) findViewById(R.id.new_res_input_name);
        CustomEditText newSurname = (CustomEditText) findViewById(R.id.new_res_input_surname);
        CustomTextView resName = (CustomTextView) findViewById(R.id.new_reservation_name);
        newReservationDate = (CustomTextView) findViewById(R.id.new_reservation_date);
        CustomTextView resTime = (CustomTextView) findViewById(R.id.new_reservation_time);
        RelativeLayout newResLayout = (RelativeLayout) findViewById(R.id.new_res_name_layout);

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch (mode)
                {
                    //0
                    case RESERVATIONS_MODE:
                        break;
                    //1
                    case 1:
                        break;
                    //2
                    case SEARCH_RESERVATIONS_MODE:
                        break;
                }
            }
        });

        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //for now it just returns to Operative
                switch (mode)
                {
                    //0
                    case RESERVATIONS_MODE:
                        Intent backIntent = new Intent(ReservationsActivity.this, Operative.class);
                        backIntent.putExtra("billId", billId);
                        backIntent.putExtra("userId", userId);
                        backIntent.putExtra("userType", userType);
                        backIntent.putExtra("tableNumber", tableNumber);
                        backIntent.putExtra("orderNumber", orderNumber);
                        backIntent.putExtra("username", username);
                        backIntent.putExtra("isAdmin", isAdmin);
                        startActivity(backIntent);
                        finish();
                        break;
                    case WAITING_LIST_MODE:
                        Intent intent = new Intent(ReservationsActivity.this, Operative.class);
                        intent.putExtra("billId", billId);
                        intent.putExtra("userId", userId);
                        intent.putExtra("userType", userType);
                        intent.putExtra("tableNumber", tableNumber);
                        intent.putExtra("orderNumber", orderNumber);
                        intent.putExtra("username", username);
                        intent.putExtra("isAdmin", isAdmin);
                        startActivity(intent);
                        finish();
                        break;
                    //2
                    case SEARCH_RESERVATIONS_MODE:
                        mode = RESERVATIONS_MODE;
                        search_et.setText("");
                        searchReservations.setActivated(false);
                        search_et.setVisibility(GONE);
                        findViewById(R.id.hline2_search).setVisibility(GONE);
                        findViewById(R.id.hline1_search).setVisibility(GONE);
                        findViewById(R.id.hline1).setVisibility(View.VISIBLE);
                        findViewById(R.id.title_tv).setVisibility(View.VISIBLE);
                        reservationsAdapter.setSearchMode(false);
                        break;
                    //3
                    case MODIFY_RESERVATION_MODE:
                        newName.setText("");
                        newSurname.setText("");
                        resName.setText(R.string.new_reservation);
                        resTime.setText("00:00");
                        adults = 0;
                        children = 0;
                        disabled = 0;
                        time = Calendar.getInstance();
                        name = "";
                        surname = "";
                        ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(
                                resources.getString(R.string.adults_children_disabled, adults, children, disabled));
                        mode = RESERVATIONS_MODE;
                        break;
                    //4
                    case SEARCH_WAITING_LIST_MODE:
                        mode = WAITING_LIST_MODE;
                        search_et.setText("");
                        searchReservations.setActivated(false);
                        search_et.setVisibility(GONE);
                        findViewById(R.id.hline2_search).setVisibility(GONE);
                        findViewById(R.id.hline1_search).setVisibility(GONE);
                        findViewById(R.id.hline1).setVisibility(View.VISIBLE);
                        findViewById(R.id.title_tv).setVisibility(View.VISIBLE);
                        waitingListAdapter.setSearchMode(false);
                        break;
                }
            }
        });
    }


    //popup after adding a new reservation
    public void throwConfirmPopup(String header)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_confirm_dialog, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        //IT FUCKING WORKS!
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) popupView.findViewById(R.id.confirm_window)
                                                                                 .getLayoutParams();
        int t = (int) (dpHeight - 52) / 2 - rlp.height / 2;
        rlp.topMargin = t;

        if (header.equals("new"))
        { ((CustomTextView) popupView.findViewById(R.id.confirm_header)).setText(R.string.you_have_added_new_reservation_); }
        else if (header.equals("modified"))
        { ((CustomTextView) popupView.findViewById(R.id.confirm_header)).setText(R.string.you_have_reinserted_modified_reservation_); }

        CustomButton confirmButton = (CustomButton) popupView.findViewById(R.id.res_confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();
                //mode = RESERVATIONS_MODE;
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
    }


    public void openModifyMode(Reservation res)
    {
        currentReservation = res;
        mode = MODIFY_RESERVATION_MODE;

        ((CustomTextView) findViewById(R.id.title_right_tv)).setText(R.string.modify_reservation);
        ((CustomButton) findViewById(R.id.insert_reservation_button)).setText(R.string.reinsert_reservation);
        ((CustomTextView) findViewById(R.id.new_reservation_name)).setText(res.getName() + " " + res.getSurname());
        ((CustomTextView) findViewById(R.id.new_reservation_time)).setText(res.getTime().toString());

        if (!res.getTelephone().isEmpty() && !res.getTelephone().equals("null"))
            { ((CustomEditText) findViewById(R.id.new_res_input_telephone)).setText(res.getTelephone()); }

        if (res.getChildren() != 0 || res.getDisabled() != 0)
            { ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(resources.getString(R.string.adults_children_disabled, res.getAdults(), res.getChildren(), res
                            .getDisabled())); }

        else
            { ((CustomTextView) findViewById(R.id.new_reservation_type)).setText(resources.getString(R.string._adults, res
                    .getAdults())); }

        adults = res.getAdults();
        children = res.getChildren();
        disabled = res.getDisabled();
        time.setTime(res.getTime());

        CustomEditText newName = (CustomEditText) findViewById(R.id.new_res_input_name);
        CustomEditText newSurname = (CustomEditText) findViewById(R.id.new_res_input_surname);
        newName.setText(res.getName());
        newSurname.setText(res.getSurname());

        ((CustomButton) findViewById(R.id.insert_reservation_button)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                res.setAdults(adults);
                res.setChildren(children);
                res.setDisabled(disabled);
                res.setTime(time.getTime());

                dbA.modifyReservation(res);
                reservationsAdapter.refreshReservationList();

                if (StaticValue.blackbox)
                {
                    Gson gson = new Gson();

                    ArrayList<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("reservation", gson.toJson(res)));

                    ((ReservationsActivity) context).callHttpHandler("/modifyReservation", params);
                }

                resetFields();

                currentReservation = null;

                mode = RESERVATIONS_MODE;
            }
        });
    }




    public void callHttpHandler(String route, List<NameValuePair> params)
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
                    if (actionId == EditorInfo.IME_ACTION_NEXT) { keyboard_next_flag = true; }
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
