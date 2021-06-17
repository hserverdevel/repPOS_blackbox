package com.example.blackbox.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.DialogCreator;
import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.server.ExternalHttpHandler;
import com.example.blackbox.server.HttpHandler;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.NameValuePair;

/**
 * Created by Fabrizio on 11/02/2019.
 */

public class OverviewActivity extends AppCompatActivity implements ExternalHttpHandler.AsyncResponse
{

    private static final String TAG = "<OverviewActivity>";

    private Context context;
    private DatabaseAdapter dbA;
    private float density;
    private float dpHeight;
    private float dpWidth;
    private String openWeatherKey = "3db092703bfa449021b523cf73281834";
    private String openWeatherAPI;

    public String getOpenWeatherAPI() {return openWeatherAPI;}

    private ExternalHttpHandler httpHandler;
    private RelativeLayout generalContainer;
    private RelativeLayout splashScreen;

    private String status;
    private static int SPLASH_TIME_OUT = 1000;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView textView;
    private Handler handler = new Handler();

    private String username;
    private int isAdmin;
    private int billId;
    private int userId;
    private int userType;
    private int tableNumber;
    private int orderNumber;

    private ImageButton ok;
    private ImageButton kill;

    private RelativeLayout lastYearDatePicker;
    private CustomTextView last_year_year;
    private CustomTextView last_year_daily_text_view;
    private CustomTextView last_year_weekly_text_view;
    private CustomTextView last_year_monthly_text_view;
    private CustomTextView last_year_annually_text_view;

    //GENERAL INFO
    private CustomTextView current_date;
    private CustomTextView current_time;
    private CustomTextView current_temperature;
    private CustomTextView current_weather;
    private CustomTextView current_events;

    //CURRENT DAY ALL SESSIONS
    private CustomTextView cash_orders;
    private CustomTextView credit_card_orders;
    private CustomTextView bank_card_orders;
    private CustomTextView tickets_orders;
    private CustomTextView client_credit_orders;
    private CustomTextView deleted_orders;
    private CustomTextView partial_orders;
    private CustomTextView unpaid_orders;
    private CustomTextView table_orders;
    private CustomTextView take_out_orders;
    private CustomTextView reserved_tables_orders;
    private CustomTextView on_line_orders;
    private CustomTextView invoice_orders;
    private CustomTextView discount_orders;
    private CustomTextView homage_orders;
    private CustomTextView round_it_orders;

    //CURRENT YEAR INFOS
    private CustomTextView daily_net_sales;
    private CustomTextView daily_estimated_cost;
    private CustomTextView daily_vat_values;
    private CustomTextView daily_assumed_profit;
    private CustomTextView weekly_net_sales;
    private CustomTextView weekly_estimated_cost;
    private CustomTextView weekly_vat_values;
    private CustomTextView weekly_assumed_profit;
    private CustomTextView monthly_net_sales;
    private CustomTextView monthly_estimated_cost;
    private CustomTextView monthly_vat_values;
    private CustomTextView monthly_assumed_profit;
    private CustomTextView annually_net_sales;
    private CustomTextView annually_estimated_cost;
    private CustomTextView annually_vat_values;
    private CustomTextView annually_assumed_profit;

    //HORIZONTAL LINES LAYOUTS
    private RelativeLayout daily_vat_value_layout;
    private RelativeLayout weekly_vat_value_layout;
    private RelativeLayout monthly_vat_value_layout;
    private RelativeLayout annually_vat_value_layout;
    private RelativeLayout last_year_daily_net_sales_layout;
    private RelativeLayout last_year_daily_vat_value_layout;
    private RelativeLayout last_year_weekly_net_sales_layout;
    private RelativeLayout last_year_weekly_vat_value_layout;
    private RelativeLayout last_year_monthly_net_sales_layout;
    private RelativeLayout last_year_monthly_vat_value_layout;
    private RelativeLayout last_year_annually_net_sales_layout;
    private RelativeLayout last_year_annually_vat_value_layout;

    //CURRENT YEAR ESTIMATIONS
    private CustomTextView daily_increment;
    private CustomTextView daily_increment_value;
    private CustomTextView weekly_increment;
    private CustomTextView weekly_increment_value;
    private CustomTextView monthly_increment;
    private CustomTextView monthly_increment_value;
    private CustomTextView annually_increment;
    private CustomTextView annually_increment_value;

    //LAST YEAR'S GENERAL INFOS
    private CustomTextView last_year_date;
    private CustomTextView last_year_time;
    private CustomTextView last_year_temperature;
    private CustomTextView last_year_weather;
    private CustomTextView last_year_events;

    //LAST YEAR'S INFOS
    private CustomTextView last_year_daily_net_sales;
    private CustomTextView last_year_daily_net_sales_increment;
    private CustomTextView last_year_daily_estimated_cost;
    private CustomTextView last_year_daily_estimated_cost_increment;
    private CustomTextView last_year_daily_vat_values;
    private CustomTextView last_year_daily_vat_values_increment;
    private CustomTextView last_year_daily_assumed_profit;
    private CustomTextView last_year_daily_assumed_profit_increment;
    private CustomTextView last_year_weekly_net_sales;
    private CustomTextView last_year_weekly_net_sales_increment;
    private CustomTextView last_year_weekly_estimated_cost;
    private CustomTextView last_year_weekly_estimated_cost_increment;
    private CustomTextView last_year_weekly_vat_values;
    private CustomTextView last_year_weekly_vat_values_increment;
    private CustomTextView last_year_weekly_assumed_profit;
    private CustomTextView last_year_weekly_assumed_profit_increment;
    private CustomTextView last_year_monthly_net_sales;
    private CustomTextView last_year_monthly_net_sales_increment;
    private CustomTextView last_year_monthly_estimated_cost;
    private CustomTextView last_year_monthly_estimated_cost_increment;
    private CustomTextView last_year_monthly_vat_values;
    private CustomTextView last_year_monthly_vat_values_increment;
    private CustomTextView last_year_monthly_assumed_profit;
    private CustomTextView last_year_monthly_assumed_profit_increment;
    private CustomTextView last_year_annually_net_sales;
    private CustomTextView last_year_annually_net_sales_increment;
    private CustomTextView last_year_annually_estimated_cost;
    private CustomTextView last_year_annually_estimated_cost_increment;
    private CustomTextView last_year_annually_vat_values;
    private CustomTextView last_year_annually_vat_values_increment;
    private CustomTextView last_year_annually_assumed_profit;
    private CustomTextView last_year_annually_assumed_profit_increment;

    private float dailyNetSails = 0.0f;
    private float dailyEstimatedCost = 0.0f;
    private float dailyVatValue = 0.0f;
    private float dailyAssumedProfit = 0.0f;
    private float weeklyNetSails = 0.0f;
    private float weeklyEstimatedCost = 0.0f;
    private float weeklyVatValue = 0.0f;
    private float weeklyAssumedProfit = 0.0f;
    private float monthlyNetSails = 0.0f;
    private float monthlyEstimatedCost = 0.0f;
    private float monthlyVatValue = 0.0f;
    private float monthlyAssumedProfit = 0.0f;
    private float annuallyNetSails = 0.0f;
    private float annuallyEstimatedCost = 0.0f;
    private float annuallyVatValue = 0.0f;
    private float annuallyAssumedProfit = 0.0f;

    private float dailyNetSailslastYear = 0.0f;
    private float dailyEstimatedCostlastYear = 0.0f;
    private float dailyVatValuelastYear = 0.0f;
    private float dailyAssumedProfitlastYear = 0.0f;
    private float weeklyNetSailslastYear = 0.0f;
    private float weeklyEstimatedCostlastYear = 0.0f;
    private float weeklyVatValuelastYear = 0.0f;
    private float weeklyAssumedProfitlastYear = 0.0f;
    private float monthlyNetSailslastYear = 0.0f;
    private float monthlyEstimatedCostlastYear = 0.0f;
    private float monthlyVatValuelastYear = 0.0f;
    private float monthlyAssumedProfitlastYear = 0.0f;
    private float annuallyNetSailslastYear = 0.0f;
    private float annuallyEstimatedCostlastYear = 0.0f;
    private float annuallyVatValuelastYear = 0.0f;
    private float annuallyAssumedProfitlastYear = 0.0f;

    private Thread mySplashscreen;

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        StaticValue.setValuesSet(false);
        if (mySplashscreen.isAlive())
        { mySplashscreen.interrupt(); }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_overview);
        context = this;
        dbA = new DatabaseAdapter(this);
        httpHandler = new ExternalHttpHandler();
        httpHandler.delegate = this;

        setOpenWeatherAPI("https://api.openweathermap.org/data/2.5/weather?q=Turin&appid=");

        /* DISPLAY METRICS */
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;

        Intent intent = getIntent();

        status = intent.getStringExtra("status");
        username = intent.getStringExtra("username");
        userId = intent.getIntExtra("userId", -1);
        userType = intent.getIntExtra("userType", -1);
        isAdmin = intent.getIntExtra("isAdmin", -1);
        billId = intent.getIntExtra("billId", -1);
        orderNumber = intent.getIntExtra("orderNumber", -1);
        tableNumber = intent.getIntExtra("tableNumber", -1);

        generalContainer = findViewById(R.id.mainspace);
        splashScreen = findViewById(R.id.splash_screen_container);

        getCustomTextViewElements();

        generalContainer.setVisibility(View.GONE);
        splashScreen.setVisibility(View.VISIBLE);

        mySplashscreen = new Thread(new MySetSplashScreen());
        mySplashscreen.start();

        //setSplashScreen();


        final Handler ha = new Handler();
        //MyRunnable thread = new MyRunnable(ha);
        ha.postDelayed(new Runnable()
        {
            private volatile boolean exit = false;

            @Override
            public void run()
            {
                if (StaticValue.getValuesSet())
                {
                    // Do whatever
                    findViewById(R.id.mainspace).setVisibility(View.VISIBLE);
                    findViewById(R.id.footer).setVisibility(View.VISIBLE);
                    findViewById(R.id.splash_screen_container).setVisibility(View.GONE);
                }
                ha.postDelayed(this, 100);
            }

        }, 100);


    }


    class MyRunnable implements Runnable
    {
        private volatile boolean exit = false;
        private final Handler ha;

        public MyRunnable(Handler h)
        {
            ha = h;
        }

        @Override
        public void run()
        {
            while (!exit)
            {
                if (StaticValue.getValuesSet())
                {
                    // Do whatever
                    findViewById(R.id.mainspace).setVisibility(View.VISIBLE);
                    findViewById(R.id.splash_screen_container).setVisibility(View.GONE);

                    stop();
                }

                ha.postDelayed(this, 100);
            }
        }

        public void stop()
        {
            exit = true;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //setValues();
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
    }

    
    
    public void getCustomTextViewElements()
    {
        // Current day elements
        current_date        = findViewById(R.id.current_date);
        current_time        = findViewById(R.id.current_time);
        current_temperature = findViewById(R.id.current_temperature);
        current_weather     = findViewById(R.id.current_weather);
        current_events      = findViewById(R.id.current_event_report);


        // Cash elements
        cash_orders            = findViewById(R.id.cash_number_text_view);
        credit_card_orders     = findViewById(R.id.credit_card_number_text_view);
        bank_card_orders       = findViewById(R.id.bank_card_number_text_view);
        tickets_orders         = findViewById(R.id.tickets_number_text_view);
        client_credit_orders   = findViewById(R.id.client_credit_number_text_view);
        deleted_orders         = findViewById(R.id.deleted_order_number_text_view);
        partial_orders         = findViewById(R.id.partial_order_number_text_view);
        unpaid_orders          = findViewById(R.id.unpaid_order_number_text_view);
        table_orders           = findViewById(R.id.table_orders_number_text_view);
        take_out_orders        = findViewById(R.id.take_out_number_text_view);
        reserved_tables_orders = findViewById(R.id.reserved_tables_number_text_view);
        on_line_orders         = findViewById(R.id.on_line_number_text_view);
        invoice_orders         = findViewById(R.id.invoice_number_text_view);
        discount_orders        = findViewById(R.id.discount_number_text_view);
        homage_orders          = findViewById(R.id.homage_number_text_view);
        round_it_orders        = findViewById(R.id.round_it_number_text_view);





        // Stats elements
        daily_net_sales         = findViewById(R.id.net_sales_number_text_view);
        daily_estimated_cost    = findViewById(R.id.estimated_cost_number_text_view);
        daily_vat_values        = findViewById(R.id.vat_value_number_text_view);
        daily_assumed_profit    = findViewById(R.id.assumed_profit_number_text_view);
        weekly_net_sales        = findViewById(R.id.net_sales_weekly_number_text_view);
        weekly_estimated_cost   = findViewById(R.id.estimated_cost_weekly_number_text_view);
        weekly_vat_values       = findViewById(R.id.vat_value_weekly_number_text_view);
        weekly_assumed_profit   = findViewById(R.id.assumed_profit_weekly_number_text_view);
        monthly_net_sales       = findViewById(R.id.net_sales_monthly_number_text_view);
        monthly_estimated_cost  = findViewById(R.id.estimated_cost_monthly_number_text_view);
        monthly_vat_values      = findViewById(R.id.vat_value_monthly_number_text_view);
        monthly_assumed_profit  = findViewById(R.id.assumed_profit_monthly_number_text_view);
        annually_net_sales      = findViewById(R.id.net_sales_annually_number_text_view);
        annually_estimated_cost = findViewById(R.id.estimated_cost_annually_number_text_view);
        annually_vat_values     = findViewById(R.id.vat_value_annually_number_text_view);
        annually_assumed_profit = findViewById(R.id.assumed_profit_annually_number_text_view);

        daily_vat_value_layout            = findViewById(R.id.second_orange_line_container);
        weekly_vat_value_layout = findViewById(R.id.second_orange_line_container_2);
        monthly_vat_value_layout = findViewById(R.id.second_orange_line_container_3);
        annually_vat_value_layout = findViewById(R.id.second_orange_line_container_4);
        last_year_daily_net_sales_layout = findViewById(R.id.first_orange_line_daily_last_year_container);
        last_year_daily_vat_value_layout = findViewById(R.id.third_orange_line_daily_last_year_container);
        last_year_weekly_net_sales_layout = findViewById(R.id.first_orange_line_weekly_last_year_container);
        last_year_weekly_vat_value_layout = findViewById(R.id.third_orange_line_weekly_last_year_container);
        last_year_monthly_net_sales_layout = findViewById(R.id.first_orange_line_monthly_last_year_container);
        last_year_monthly_vat_value_layout = findViewById(R.id.third_orange_line_monthly_last_year_container);
        last_year_annually_net_sales_layout = findViewById(R.id.first_orange_line_annually_last_year_container);
        last_year_annually_vat_value_layout = findViewById(R.id.third_orange_line_annually_last_year_container);

        daily_increment = findViewById(R.id.increment_text_view);
        daily_increment_value = findViewById(R.id.daily_estimation_text_view);
        weekly_increment = findViewById(R.id.weekly_increment_text_view);
        weekly_increment_value = findViewById(R.id.weekly_estimation_text_view);
        monthly_increment = findViewById(R.id.monthly_increment_text_view);
        monthly_increment_value = findViewById(R.id.monthly_estimation_text_view);
        annually_increment = findViewById(R.id.annually_increment_text_view);
        annually_increment_value = findViewById(R.id.annually_estimation_text_view);

        last_year_date = findViewById(R.id.last_year_date);
        last_year_year = findViewById(R.id.last_year_year);
        last_year_temperature = findViewById(R.id.last_year_temperature);
        last_year_weather = findViewById(R.id.last_year_weather);
        last_year_events = findViewById(R.id.last_year_events);

        last_year_daily_net_sales                = findViewById(R.id.net_sales_number_daily_last_year_text_view);
        last_year_daily_net_sales_increment      = findViewById(R.id.net_sales_daily_last_year_increment_text_view);
        last_year_daily_estimated_cost           = findViewById(R.id.estimated_cost_number_daily_last_year_text_view);
        last_year_daily_estimated_cost_increment = findViewById(R.id.estimated_cost_last_year_increment_text_view);
        last_year_daily_vat_values_increment = findViewById(R.id.vat_value_last_year_increment_text_view);
        last_year_daily_vat_values = findViewById(R.id.vat_value_number_daily_last_year_text_view);
        last_year_daily_assumed_profit_increment = findViewById(R.id.assumed_profit_last_year_increment_text_view);
        last_year_daily_assumed_profit = findViewById(R.id.assumed_profit_number_daily_last_year_text_view);
        last_year_weekly_net_sales = findViewById(R.id.net_sales_weekly_last_year_number_text_view);
        last_year_weekly_net_sales_increment = findViewById(R.id.net_sales_weekly_last_year_increment_text_view);
        last_year_weekly_estimated_cost = findViewById(R.id.estimated_cost_weekly_last_year_number_text_view);
        last_year_weekly_estimated_cost_increment = findViewById(R.id.estimated_cost_weekly_last_year_increment_text_view);
        last_year_weekly_vat_values = findViewById(R.id.vat_value_weekly_last_year_number_text_view);
        last_year_weekly_vat_values_increment = findViewById(R.id.vat_value_weekly_last_year_increment_text_view);
        last_year_weekly_assumed_profit = findViewById(R.id.assumed_profit_weekly_last_year_number_text_view);
        last_year_weekly_assumed_profit_increment = findViewById(R.id.assumed_profit_weekly_last_year_increment_text_view);
        last_year_monthly_net_sales = findViewById(R.id.net_sales_monthly_last_year_number_text_view);
        last_year_monthly_net_sales_increment = findViewById(R.id.net_sales_monthly_last_year_increment_text_view);
        last_year_monthly_estimated_cost = findViewById(R.id.estimated_cost_monthly_last_year_number_text_view);
        last_year_monthly_estimated_cost_increment = findViewById(R.id.estimated_cost_monthly_last_year_increment_text_view);
        last_year_monthly_vat_values = findViewById(R.id.vat_value_monthly_last_year_number_text_view);
        last_year_monthly_vat_values_increment = findViewById(R.id.vat_value_monthly_last_year_increment_text_view);
        last_year_monthly_assumed_profit = findViewById(R.id.assumed_profit_monthly_last_year_number_text_view);
        last_year_monthly_assumed_profit_increment = findViewById(R.id.assumed_profit_monthly_last_year_increment_text_view);
        last_year_annually_net_sales = findViewById(R.id.net_sales_annually_last_year_number_text_view);
        last_year_annually_net_sales_increment = findViewById(R.id.net_sales_annually_last_year_increment_text_view);
        last_year_annually_estimated_cost = findViewById(R.id.estimated_cost_annually_last_year_number_text_view);
        last_year_annually_estimated_cost_increment = findViewById(R.id.estimated_cost_annually_last_year_increment_text_view);
        last_year_annually_vat_values = findViewById(R.id.vat_value_annually_last_year_number_text_view);
        last_year_annually_vat_values_increment = findViewById(R.id.vat_value_annually_last_year_increment_text_view);
        last_year_annually_assumed_profit = findViewById(R.id.assumed_profit_annually_last_year_number_text_view);
        last_year_annually_assumed_profit_increment = findViewById(R.id.assumed_profit_annually_last_year_increment_text_view);

        lastYearDatePicker = findViewById(R.id.yesterday_date_tv);
        last_year_daily_text_view = findViewById(R.id.last_year_daily_text_view);
        last_year_weekly_text_view = findViewById(R.id.last_year_weekly_text_view);
        last_year_monthly_text_view = findViewById(R.id.last_year_monthly_text_view);
        last_year_annually_text_view = findViewById(R.id.last_year_annually_text_view);

        lastYearDatePicker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Calendar currentDate = Calendar.getInstance();
                int year = currentDate.get(Calendar.YEAR);
                int month = currentDate.get(Calendar.MONTH);
                int day = currentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dPicker;
                dPicker = new DatePickerDialog(OverviewActivity.this, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    //i=year, i1=month, i2=day
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2)
                    {
                        //if date is different from today date
                        if (i != year || i1 != month || i2 != day)
                        {
                            Calendar date = Calendar.getInstance();
                            date.set(i, i1 + 1, i2);
                            last_year_date.setText(i2 + " " + getMonth(i1 + 1) + ", " + date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH));

                            last_year_year.setText(i + "");
                            last_year_daily_text_view.setText(i + "");
                            last_year_weekly_text_view.setText(i + "");
                            last_year_monthly_text_view.setText(i + "");
                            last_year_annually_text_view.setText(i + "");

                            dailyNetSailslastYear = dbA.getLastYearDailyTotal(1, i, i1, i2);
                            dailyVatValuelastYear = dbA.getLastYearDailyTotal(2, i, i1, i2);
                            weeklyNetSailslastYear = dbA.getLastYearDailyTotal(1, i, i1, i2);
                            weeklyVatValuelastYear = dbA.getLastYearDailyTotal(2, i, i1, i2);
                            monthlyNetSailslastYear = dbA.getLastYearDailyTotal(1, i, i1, i2);
                            monthlyVatValuelastYear = dbA.getLastYearDailyTotal(2, i, i1, i2);
                            annuallyNetSailslastYear = dbA.getLastYearDailyTotal(1, i, i1, i2);
                            annuallyVatValuelastYear = dbA.getLastYearDailyTotal(2, i, i1, i2);
                            DecimalFormat twoDForm = new DecimalFormat("#0.00");
                            last_year_daily_net_sales.setText(twoDForm.format(dailyNetSailslastYear).replace(".", ",") + "€");
                            last_year_daily_vat_values.setText(twoDForm.format(dailyVatValuelastYear).replace(".", ",") + "€");
                            last_year_weekly_net_sales.setText(twoDForm.format(weeklyNetSailslastYear).replace(".", ",") + "€");
                            last_year_weekly_vat_values.setText(twoDForm.format(weeklyVatValuelastYear).replace(".", ",") + "€");
                            last_year_monthly_net_sales.setText(twoDForm.format(monthlyNetSailslastYear).replace(".", ",") + "€");
                            last_year_monthly_vat_values.setText(twoDForm.format(monthlyVatValuelastYear).replace(".", ",") + "€");
                            last_year_annually_net_sales.setText(twoDForm.format(annuallyNetSailslastYear).replace(".", ",") + "€");
                            last_year_annually_vat_values.setText(twoDForm.format(annuallyVatValuelastYear).replace(".", ",") + "€");

                            float dailyNetSalesPercentage = ((dailyNetSailslastYear - dailyNetSails) / dailyNetSailslastYear) * 100;
                            float dailyVatValuePercentage = ((dailyVatValuelastYear - dailyVatValue) / dailyVatValuelastYear) * 100;
                            float weeklyNetSalesPercentage = ((weeklyNetSailslastYear - weeklyNetSails) / weeklyNetSailslastYear) * 100;
                            float weeklyVatValuePercentage = ((weeklyVatValuelastYear - weeklyVatValue) / weeklyVatValuelastYear) * 100;
                            float monthlyNetSalesPercentage = ((monthlyNetSailslastYear - monthlyNetSails) / monthlyNetSailslastYear) * 100;
                            float monthlyVatValuePercentage = ((monthlyVatValuelastYear - monthlyVatValue) / monthlyVatValuelastYear) * 100;
                            float annuallyNetSalesPercentage = ((annuallyNetSailslastYear - annuallyNetSails) / annuallyNetSailslastYear) * 100;
                            float annuallyVatValuePercentage = ((weeklyVatValuelastYear - annuallyVatValue) / annuallyVatValuelastYear) * 100;
                            String sign;
                            sign = dailyNetSalesPercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_daily_net_sales_increment.setText(sign + twoDForm.format(dailyNetSalesPercentage).replace(".", ",") + "%");
                            sign = dailyVatValuePercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_daily_vat_values_increment.setText(sign + twoDForm.format(dailyVatValuePercentage).replace(".", ",") + "%");
                            sign = weeklyNetSalesPercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_weekly_net_sales_increment.setText(sign + twoDForm.format(weeklyNetSalesPercentage).replace(".", ",") + "%");
                            sign = weeklyVatValuePercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_weekly_vat_values_increment.setText(sign + twoDForm.format(weeklyVatValuePercentage).replace(".", ",") + "%");
                            sign = monthlyNetSalesPercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_monthly_net_sales_increment.setText(sign + twoDForm.format(monthlyNetSalesPercentage).replace(".", ",") + "%");
                            sign = monthlyVatValuePercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_monthly_vat_values_increment.setText(sign + twoDForm.format(monthlyVatValuePercentage).replace(".", ",") + "%");
                            sign = annuallyNetSalesPercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_annually_net_sales_increment.setText(sign + twoDForm.format(annuallyNetSalesPercentage).replace(".", ",") + "%");
                            sign = annuallyVatValuePercentage < 0
                                   ? "-"
                                   : "+";
                            last_year_annually_vat_values_increment.setText(sign + twoDForm.format(annuallyVatValuePercentage).replace(".", ",") + "%");
                        }
                    }
                }, year, month, day);
                dPicker.show();
            }
        });

        ok = (ImageButton) findViewById(R.id.ok);
        kill = (ImageButton) findViewById(R.id.kill);

        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(OverviewActivity.this, Operative.class);
                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("billId", billId);
                intent.putExtra("userId", userId);
                intent.putExtra("userType", userType);
                intent.putExtra("tableNumber", tableNumber);
                intent.putExtra("orderNumber", orderNumber);
                startActivity(intent);
                finish();
            }
        });

        kill.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(OverviewActivity.this, Operative.class);
                intent.putExtra("username", username);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("billId", billId);
                intent.putExtra("userId", userId);
                intent.putExtra("userType", userType);
                intent.putExtra("tableNumber", tableNumber);
                intent.putExtra("orderNumber", orderNumber);
                startActivity(intent);
                finish();
            }
        });
    }

    public void setValues1()
    {

        current_date.setText(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " " + Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ITALY) + ", " + Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH));
        if (Calendar.getInstance().get(Calendar.MINUTE) < 10)
        { current_time.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":0" + Calendar.getInstance().get(Calendar.MINUTE)); }
        else
        { current_time.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE)); }

        cash_orders.setText(dbA.getDailyPayment(1) + "");
        credit_card_orders.setText(dbA.getDailyPayment(2) + "");
        bank_card_orders.setText(dbA.getDailyPayment(4) + "");
        tickets_orders.setText(dbA.getDailyPayment(5) + "");
        client_credit_orders.setText(dbA.getClientCreditPayment() + "");
        deleted_orders.setText(dbA.getDailyOrderInfo(3) + "");
        partial_orders.setText(dbA.getDailyOrderInfo(5) + "");
        unpaid_orders.setText(dbA.getDailyOrderInfo(4) + "");

    }

    public void setValues2()
    {
        table_orders.setText(dbA.getTotalTableOrders() + "");
        take_out_orders.setText(dbA.getTotalTakeAwayOrders() + "");
        reserved_tables_orders.setText(0 + "");
        on_line_orders.setText(0 + "");
        invoice_orders.setText(dbA.getInvoiceOrders() + "");
        discount_orders.setText(dbA.getTotalDiscountOrders() + "");
        round_it_orders.setText(dbA.getTotalDiscountOrders() + "");
        homage_orders.setText(dbA.getHomageOrders() + "");

        dailyNetSails = dbA.getDailyTotal(1);
        dailyVatValue = dbA.getDailyTotal(2);
        weeklyNetSails = dbA.getWeeklyTotal(1);
        weeklyVatValue = dbA.getWeeklyTotal(2);
    }

    public void setValues3()
    {
        monthlyNetSails = dbA.getMonthlyTotal(1);
        monthlyVatValue = dbA.getMonthlyTotal(2);
        annuallyNetSails = dbA.getAnnuallyTotal(1);
        annuallyVatValue = dbA.getAnnuallyTotal(2);

        DecimalFormat twoDForm = new DecimalFormat("#0.00");
        daily_net_sales.setText(twoDForm.format(dailyNetSails).replace(".", ",") + "€");
        daily_estimated_cost.setText("00,00€");
        daily_vat_values.setText(twoDForm.format(dailyVatValue).replace(".", ",") + "€");
        daily_assumed_profit.setText("00,00€");
        weekly_net_sales.setText(twoDForm.format(weeklyNetSails).replace(".", ",") + "€");
        weekly_estimated_cost.setText("00,00€");
        weekly_vat_values.setText(twoDForm.format(weeklyVatValue).replace(".", ",") + "€");
        weekly_assumed_profit.setText("00,00€");
        monthly_net_sales.setText(twoDForm.format(monthlyNetSails).replace(".", ",") + "€");
        monthly_estimated_cost.setText("00,00€");
        monthly_vat_values.setText(twoDForm.format(monthlyVatValue).replace(".", ",") + "€");
        monthly_assumed_profit.setText("00,00€");
        annually_net_sales.setText(twoDForm.format(annuallyNetSails).replace(".", ",") + "€");
        annually_estimated_cost.setText("00,00€");
        annually_vat_values.setText(twoDForm.format(annuallyVatValue).replace(".", ",") + "€");
        annually_assumed_profit.setText("00,00€");
    }

    public void setValues4()
    {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        last_year_date.setText(yesterday.get(Calendar.DAY_OF_MONTH) + " " + yesterday.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ITALY) + ", " + yesterday.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH));
        last_year_year.setText(Calendar.getInstance().get(Calendar.YEAR) + "");
        last_year_daily_text_view.setText(Calendar.getInstance().get(Calendar.YEAR) + "");
        last_year_weekly_text_view.setText(Calendar.getInstance().get(Calendar.YEAR) + "");
        last_year_monthly_text_view.setText(Calendar.getInstance().get(Calendar.YEAR) + "");
        last_year_annually_text_view.setText(Calendar.getInstance().get(Calendar.YEAR) + "");

        dailyNetSailslastYear = dbA.getLastYearDailyTotal(1, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        dailyEstimatedCostlastYear = 0.0f;
        dailyVatValuelastYear = dbA.getLastYearDailyTotal(2, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        dailyAssumedProfitlastYear = 0.0f;
        weeklyNetSailslastYear = dbA.getLastYearDailyTotal(1, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        weeklyEstimatedCostlastYear = 0.0f;
        weeklyVatValuelastYear = dbA.getLastYearDailyTotal(2, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        weeklyAssumedProfitlastYear = 0.0f;
        monthlyNetSailslastYear = dbA.getLastYearDailyTotal(1, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        monthlyEstimatedCostlastYear = 0.0f;
        monthlyVatValuelastYear = dbA.getLastYearDailyTotal(2, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        monthlyAssumedProfitlastYear = 0.0f;
        annuallyNetSailslastYear = dbA.getLastYearDailyTotal(1, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        annuallyEstimatedCostlastYear = 0.0f;
        annuallyVatValuelastYear = dbA.getLastYearDailyTotal(2, yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH));
        annuallyAssumedProfitlastYear = 0.0f;
    }

    public void setValues5()
    {
        DecimalFormat twoDForm = new DecimalFormat("#0.00");

        last_year_daily_net_sales.setText(twoDForm.format(dailyNetSailslastYear).replace(".", ",") + "€");
        last_year_daily_estimated_cost.setText(twoDForm.format(dailyEstimatedCostlastYear).replace(".", ",") + "€");
        last_year_daily_vat_values.setText(twoDForm.format(dailyVatValuelastYear).replace(".", ",") + "€");
        last_year_daily_assumed_profit.setText(twoDForm.format(dailyAssumedProfitlastYear).replace(".", ",") + "€");
        last_year_weekly_net_sales.setText(twoDForm.format(weeklyNetSailslastYear).replace(".", ",") + "€");
        last_year_weekly_estimated_cost.setText(twoDForm.format(weeklyEstimatedCostlastYear).replace(".", ",") + "€");
        last_year_weekly_vat_values.setText(twoDForm.format(weeklyVatValuelastYear).replace(".", ",") + "€");
        last_year_weekly_assumed_profit.setText(twoDForm.format(weeklyAssumedProfitlastYear).replace(".", ",") + "€");
        last_year_monthly_net_sales.setText(twoDForm.format(monthlyNetSailslastYear).replace(".", ",") + "€");
        last_year_monthly_estimated_cost.setText(twoDForm.format(monthlyEstimatedCostlastYear).replace(".", ",") + "€");
        last_year_monthly_vat_values.setText(twoDForm.format(monthlyVatValuelastYear).replace(".", ",") + "€");
        last_year_monthly_assumed_profit.setText(twoDForm.format(monthlyAssumedProfitlastYear).replace(".", ",") + "€");
        last_year_annually_net_sales.setText(twoDForm.format(annuallyNetSailslastYear).replace(".", ",") + "€");
        last_year_annually_estimated_cost.setText(twoDForm.format(annuallyEstimatedCostlastYear).replace(".", ",") + "€");
        last_year_annually_vat_values.setText(twoDForm.format(annuallyVatValuelastYear).replace(".", ",") + "€");
        last_year_annually_assumed_profit.setText(twoDForm.format(annuallyAssumedProfitlastYear).replace(".", ",") + "€");

        double dailyNetSalesPercentage = ((dailyNetSailslastYear - dailyNetSails) / dailyNetSailslastYear) * 100;
        double dailyVatValuePercentage = ((dailyVatValuelastYear - dailyVatValue) / dailyVatValuelastYear) * 100;
        double weeklyNetSalesPercentage = ((weeklyNetSailslastYear - weeklyNetSails) / weeklyNetSailslastYear) * 100;
        double weeklyVatValuePercentage = ((weeklyVatValuelastYear - weeklyVatValue) / weeklyVatValuelastYear) * 100;
        double monthlyNetSalesPercentage = ((monthlyNetSailslastYear - monthlyNetSails) / monthlyNetSailslastYear) * 100;
        double monthlyVatValuePercentage = ((monthlyVatValuelastYear - monthlyVatValue) / monthlyVatValuelastYear) * 100;
        double annuallyNetSalesPercentage = ((annuallyNetSailslastYear - annuallyNetSails) / annuallyNetSailslastYear) * 100;
        double annuallyVatValuePercentage = ((weeklyVatValuelastYear - annuallyVatValue) / annuallyVatValuelastYear) * 100;
        String sign;
        sign = dailyNetSalesPercentage < 0
               ? "-"
               : "+";
        if (dailyNetSalesPercentage != Double.NEGATIVE_INFINITY && dailyNetSalesPercentage != Double.POSITIVE_INFINITY)
        { last_year_daily_net_sales_increment.setText(sign + twoDForm.format(dailyNetSalesPercentage).replace(".", ",") + "%"); }
        else
        { last_year_daily_net_sales_increment.setText("0,00%"); }
        sign = dailyVatValuePercentage < 0
               ? "-"
               : "+";
        if (dailyVatValuePercentage != Double.NEGATIVE_INFINITY && dailyVatValuePercentage != Double.POSITIVE_INFINITY)
        { last_year_daily_vat_values_increment.setText(sign + twoDForm.format(dailyVatValuePercentage).replace(".", ",") + "%"); }
        else
        { last_year_daily_vat_values_increment.setText("0,00%"); }
        sign = weeklyNetSalesPercentage < 0
               ? "-"
               : "+";
        if (weeklyNetSalesPercentage != Double.NEGATIVE_INFINITY && weeklyNetSalesPercentage != Double.POSITIVE_INFINITY)
        { last_year_weekly_net_sales_increment.setText(sign + twoDForm.format(weeklyNetSalesPercentage).replace(".", ",") + "%"); }
        else
        { last_year_weekly_net_sales_increment.setText("0,00%"); }
        sign = weeklyVatValuePercentage < 0
               ? "-"
               : "+";
        if (weeklyVatValuePercentage != Double.NEGATIVE_INFINITY && weeklyVatValuePercentage != Double.POSITIVE_INFINITY)
        { last_year_weekly_vat_values_increment.setText(sign + twoDForm.format(weeklyVatValuePercentage).replace(".", ",") + "%"); }
        else
        { last_year_weekly_vat_values_increment.setText("0,00%"); }
        sign = monthlyNetSalesPercentage < 0
               ? "-"
               : "+";
        if (monthlyNetSalesPercentage != Double.NEGATIVE_INFINITY && monthlyNetSalesPercentage != Double.POSITIVE_INFINITY)
        { last_year_monthly_net_sales_increment.setText(sign + twoDForm.format(monthlyNetSalesPercentage).replace(".", ",") + "%"); }
        else
        { last_year_monthly_net_sales_increment.setText("0,00%"); }
        sign = monthlyVatValuePercentage < 0
               ? "-"
               : "+";
        if (monthlyVatValuePercentage != Double.NEGATIVE_INFINITY && monthlyVatValuePercentage != Double.POSITIVE_INFINITY)
        { last_year_monthly_vat_values_increment.setText(sign + twoDForm.format(monthlyVatValuePercentage).replace(".", ",") + "%"); }
        else
        { last_year_monthly_vat_values_increment.setText("0,00%"); }
        sign = annuallyNetSalesPercentage < 0
               ? "-"
               : "+";
        if (annuallyNetSalesPercentage != Double.NEGATIVE_INFINITY && annuallyNetSalesPercentage != Double.POSITIVE_INFINITY)
        { last_year_annually_net_sales_increment.setText(sign + twoDForm.format(annuallyNetSalesPercentage).replace(".", ",") + "%"); }
        else
        { last_year_annually_net_sales_increment.setText("0,00%"); }
        sign = annuallyVatValuePercentage < 0
               ? "-"
               : "+";
        if (annuallyVatValuePercentage != Double.NEGATIVE_INFINITY && annuallyVatValuePercentage != Double.POSITIVE_INFINITY)
        { last_year_annually_vat_values_increment.setText(sign + twoDForm.format(annuallyVatValuePercentage).replace(".", ",") + "%"); }
        else
        { last_year_annually_vat_values_increment.setText("0,00%"); }
    }

    public void setOpenWeatherAPI(String url)
    {
        openWeatherAPI = url + openWeatherKey + "";
        List<NameValuePair> vars = new ArrayList<>(2);
      /*  httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(openWeatherAPI, vars);
        httpHandler.execute();
*/

        httpHandler = new ExternalHttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(openWeatherAPI, vars);
        httpHandler.execute();
    }

    public String getMonth(int id)
    {
        switch (id)
        {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
        }
        return "";
    }

    private class MySetSplashScreen extends Thread
    {


        public void run()
        {

            while (!StaticValue.getValuesSet())
            {
                if (status == null)
                {
                    progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                    setValues1();

                    while (progressStatus < 100)
                    {
                        progressStatus += 1;
                        if (progressStatus == 20)
                        { setValues2(); }
                        else if (progressStatus == 40)
                        { setValues3(); }
                        else if (progressStatus == 60)
                        { setValues4(); }
                        else if (progressStatus == 80)
                        { setValues5(); }
                        // Update the progress bar and display the
                        //current value in the text view
                        handler.post(new Runnable()
                        {
                            public void run()
                            {
                                progressBar.setProgress(progressStatus);
                            }
                        });
                        try
                        {
                            // Sleep for 200 milliseconds.
                            Thread.sleep(10);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    StaticValue.setValuesSet(true);
                }
            }
        }
    }

    public void setSplashScreen()
    {

        if (status == null)
        {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    setValues1();

                    while (progressStatus < 100)
                    {
                        progressStatus += 1;
                        if (progressStatus == 20)
                        { setValues2(); }
                        else if (progressStatus == 40)
                        { setValues3(); }
                        else if (progressStatus == 60)
                        { setValues4(); }
                        else if (progressStatus == 80)
                        { setValues5(); }
                        // Update the progress bar and display the
                        //current value in the text view
                        handler.post(new Runnable()
                        {
                            public void run()
                            {
                                progressBar.setProgress(progressStatus);
                            }
                        });
                        try
                        {
                            // Sleep for 200 milliseconds.
                            Thread.sleep(10);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    StaticValue.setValuesSet(true);
                }
            });
            thread.start();
            thread.interrupt();
        }
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
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "[processFinish] " + route + "response: " + success);

        if (success)
        {
            try
            {
                JSONObject main = jsonObject.getJSONObject("main");
                String temperature = main.getString("temp");
                int temp = (int) (Double.parseDouble(temperature) - 273.15);
                current_temperature.setText(temp + "°C");
                last_year_temperature.setText(temp + "°C");

                JSONArray clouds = jsonObject.getJSONArray("weather");
                JSONObject cloudsObject = clouds.getJSONObject(0);
                String cloudsString = cloudsObject.getString("main");

                JSONObject wind = jsonObject.getJSONObject("wind");
                String windSrting = wind.getString("speed");

                if (Double.parseDouble(windSrting) > 10)
                {
                    current_weather.setText(cloudsString + " & Windy");
                    last_year_weather.setText(cloudsString + " & Windy");
                }
                else
                {
                    current_weather.setText(cloudsString + " & Calm");
                    last_year_weather.setText(cloudsString + " & Calm");
                }

            }
            catch (JSONException e)
            {
                Log.d("JSON ERROR", e.getLocalizedMessage());
            }
        }

        else
        {
            Toast.makeText(this, getString(R.string.error_blackbox_comm, StaticValue.blackboxInfo.getName(), StaticValue.blackboxInfo.getAddress()), Toast.LENGTH_LONG).show();
        }
    }
}
