package com.example.blackbox.adapter;

/**
 * Created by tiziano on 9/2/17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.OrderActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TotalBill;
import com.utils.db.DatabaseAdapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TakeAwayAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {

    private final static String TAG = "<TakeAwayAdapter>";

    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    ArrayList<TotalBill> totalBillsList = new ArrayList<>();
    private AdapterCallback mAdapterCallback;
    private boolean paidOrders = false;
    public View myPopupView;
    public PopupWindow myPopupWindow;

    public TakeAwayAdapter(Context c , DatabaseAdapter database){
        context = c;
        this.mAdapterCallback = ((AdapterCallback) context);
        dbA = database;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        if (StaticValue.blackbox)
        {
            RequestParam params = new RequestParam();
            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            params.add("androidId", android_id);
            // params.add("billTotalChecksum", dbA.getChecksumForTable("bill_total"));

            ((OrderActivity) context).callHttpHandler("/getTakeAwayOrders", params);
        }

        else
        {
            long sessionTime = dbA.getLastClosing();
            Date date = new Date(sessionTime);
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateText = df2.format(date);
            totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 AND creation_time>='" + dateText + "' and id NOT IN (SELECT total_bill_id FROM table_use);");
            if (totalBillsList != null)
                Collections.reverse(totalBillsList);
        }
    }

    public void setTotalBillLists(ArrayList<TotalBill> myList){
        totalBillsList = myList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.recycler_take_away, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TotalBill totalBill = totalBillsList.get(position);
        ButtonHolder button = (ButtonHolder) holder;

        if(paidOrders){
            button.takeAwayNumberText.setVisibility(View.GONE);
            button.takeAwayNumberGreenText.setVisibility(View.VISIBLE);
            int client_id = dbA.getClientFromProductBill(totalBill.getId());
            if(client_id != -1){
                button.takeAwayNumberGreenText.setText(dbA.fetchSingleClient(client_id).getName() + " " + totalBill.getDevice_name());
            }
            else
                button.takeAwayNumberGreenText.setText("#"+ (totalBill.getBillNumber()+1) + " " + totalBill.getDevice_name());
        }
        else if(!paidOrders){
            button.takeAwayNumberGreenText.setVisibility(View.GONE);
            button.takeAwayNumberText.setVisibility(View.VISIBLE);
            int client_id = dbA.getClientFromProductBill(totalBill.getId());
            if(client_id != -1){
                button.takeAwayNumberText.setText(dbA.fetchSingleClient(client_id).getName()+ " " + totalBill.getDevice_name());
            }
            else
                button.takeAwayNumberText.setText("#"+ (totalBill.getBillNumber()+1)+ " " + totalBill.getDevice_name());
        }
        long creationt = totalBill.getCreationTime().getTime();
        long actualtime = Calendar.getInstance().getTime().getTime();
        Date d = totalBill.getCreationTime();
        Date d2 = Calendar.getInstance().getTime();
        long diffInSec =    ((totalBill.getCreationTime().getTime() -  Calendar.getInstance().getTime().getTime())/1000);

        /*long hours = TimeUnit.SECONDS.toHours(diffInSec);
        String formattedHours = new DecimalFormat("00").format((hours*-1 ));
        long minute = TimeUnit.SECONDS.toMinutes(diffInSec) - (TimeUnit.SECONDS.toHours(diffInSec)* 60);
        String formattedMinute = new DecimalFormat("00").format((minute*-1));
        System.out.println(" Hour " + hours + " Minute " + minute );
        button.customChronometer.setText(formattedHours+":"+formattedMinute);
*/
        long minuteDifference = (TimeUnit.SECONDS.toMinutes(diffInSec));
        if(minuteDifference<=999) {
            String formattedMinute = new DecimalFormat("000").format((minuteDifference * -1));
            button.customChronometer.setText(formattedMinute);
        } else button.customChronometer.setText("999");

        if(position%2==0){
            //parin
            int color2 = Color.parseColor("#444444");
            int color = Color.parseColor("#222222");
            button.linearLayout.setBackgroundColor(color);
            button.customChronometer.setBackgroundColor(color2);
        }else{
            //dispari
            int color = Color.parseColor("#444444");
            button.linearLayout.setBackgroundColor(color);
            int color2 = Color.parseColor("#222222");
            button.customChronometer.setBackgroundColor(color2);
        }
        final int total = totalBill.getId();
        final int number = totalBill.getBillNumber()+1;
        button.view.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        try {
                            mAdapterCallback.showPaymentFromOrder(total, number);
                        } catch (ClassCastException exception) {
                            // do something
                        }
                    }
                }
        );

        button.view.setOnTouchListener(new OnSwipeTouchListener(context){

            public void onSwipeLeft() { setupPopupOrderOperationsButtons(totalBill); }

            public void onLongClick() { setupPopupOrderOperationsButtons(totalBill); }

            public void onClick(){
                try {
                    mAdapterCallback.showPaymentFromOrder(total, number);
                } catch (ClassCastException exception) {
                    // do something
                }
            }

        });
    }

    private void setupPopupOrderOperationsButtons(TotalBill totalBill)
    {
        if (!paidOrders)
        {
            Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics ();
            display.getMetrics(outMetrics);

            float density  = context.getResources().getDisplayMetrics().density;
            float dpHeight = outMetrics.heightPixels;// / density;
            float dpWidth  = outMetrics.widthPixels;// / density;

            final View popupView = inflater.inflate(R.layout.popup_recent_orders, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

            RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams)popupView.findViewById(R.id.recent_orders_window)
                    .getLayoutParams();

            /** 52 => footer height ; 31 => popupwindow height/2 **/
            int t = (int) ((int)(dpHeight - 52)/2 -62*density);
            rll.setMargins(0,t,0,0);
            popupView.findViewById(R.id.recent_orders_window).setLayoutParams(rll);

            CustomButton delete = popupView.findViewById(R.id.first_button);
            CustomButton unpaid = popupView.findViewById(R.id.second_button);
            CustomButton partial = popupView.findViewById(R.id.third_button);
            CustomButton cancel = popupView.findViewById(R.id.cancel_delete_button);

            final int CODE_DELETE = 3;
            final int CODE_UNPAID = 4;
            final int CODE_PARTIAL = 5;
            final int CODE_WHITEOUT = 9;


            delete.setOnClickListener(view ->
            {
                if (StaticValue.blackbox)
                {
                    myPopupView = popupView;
                    myPopupWindow = popupWindow;

                    RequestParam params = new RequestParam();
                    params.add("billId", String.valueOf(totalBill.getId()));
                    params.add("type", String.valueOf(CODE_DELETE));
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);

                    ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTAList", params);
                }

                else
                {
                    dbA.modifyTablePaymentInBill(totalBill.getId(), CODE_DELETE);
                    long sessionTime = dbA.getLastClosing();
                    Date date = new Date(sessionTime);
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dateText = df2.format(date);
                    totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 AND creation_time>='" + dateText + "' and id NOT IN (SELECT total_bill_id FROM table_use);");
                    Collections.reverse(totalBillsList);
                    notifyDataSetChanged();
                    popupWindow.dismiss();
                }
            });

            // add the whiteout function to the delete button
            delete.setOnLongClickListener(view ->
            {
                if (StaticValue.blackbox)
                {
                    myPopupView = popupView;
                    myPopupWindow = popupWindow;

                    RequestParam params = new RequestParam();
                    params.add("billId", String.valueOf(totalBill.getId()));
                    params.add("type", String.valueOf(CODE_WHITEOUT));
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);

                    ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTAList", params);

                    return true;
                }

                else
                {
                    dbA.modifyTablePaymentInBill(totalBill.getId(), CODE_WHITEOUT);
                    long sessionTime = dbA.getLastClosing();
                    Date date = new Date(sessionTime);
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dateText = df2.format(date);
                    totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 AND creation_time>='" + dateText + "' and id NOT IN (SELECT total_bill_id FROM table_use);");
                    Collections.reverse(totalBillsList);
                    notifyDataSetChanged();
                    popupWindow.dismiss();

                    return false;
                }
            });

            unpaid.setOnClickListener(view ->
            {
                if (StaticValue.blackbox)
                {
                    myPopupView = popupView;
                    myPopupWindow = popupWindow;

                    RequestParam params = new RequestParam();
                    params.add("billId", String.valueOf(totalBill.getId()));
                    params.add("type", String.valueOf(CODE_UNPAID));
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);

                    ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTAList", params);
                }

                else
                {
                    dbA.modifyTablePaymentInBill(totalBill.getId(), 4);
                    long time = System.currentTimeMillis();
                    //long sessionTime = dbA.getSessionTime(time);
                    long sessionTime = dbA.getLastClosing();
                    Date date = new Date(sessionTime);
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dateText = df2.format(date);
                    totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 AND creation_time>='" + dateText + "' and id NOT IN (SELECT total_bill_id FROM table_use);");
                    Collections.reverse(totalBillsList);
                    notifyDataSetChanged();
                    popupWindow.dismiss();
                }
            });

            partial.setOnClickListener(view ->
            {

                if (StaticValue.blackbox)
                {
                    myPopupView = popupView;
                    myPopupWindow = popupWindow;

                    RequestParam params = new RequestParam();
                    params.add("billId", String.valueOf(totalBill.getId()));
                    params.add("type", String.valueOf(CODE_PARTIAL));
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);

                    ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTAList", params);
                }

                else
                {
                    dbA.modifyTablePaymentInBill(totalBill.getId(), 5);
                    dbA.deleteSubdivisionItems(totalBill.getId());

                    long time = System.currentTimeMillis();
                    //long sessionTime = dbA.getSessionTime(time);
                    long sessionTime = dbA.getLastClosing();
                    Date date = new Date(sessionTime);
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dateText = df2.format(date);
                    totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 AND creation_time>='" + dateText + "' and id NOT IN (SELECT total_bill_id FROM table_use);");
                    Collections.reverse(totalBillsList);
                    notifyDataSetChanged();
                    popupWindow.dismiss();
                }
            });

            cancel.setOnClickListener(view -> popupWindow.dismiss());

            popupWindow.setFocusable(true);
            popupWindow.showAtLocation(((OrderActivity)context).findViewById(R.id.main),0,0,0);

        }

        else
            { Toast.makeText(context, R.string.you_cant_modify, Toast.LENGTH_SHORT).show(); }
    }


    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public int getItemCount() {
        if(totalBillsList != null && totalBillsList.size() != 0)
            return totalBillsList.size();
        else
            return 0;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    /** HOLDERS **/
    public static class ButtonHolder extends ViewHolder{
        public View view;
        public LinearLayout linearLayout;
        public CustomTextView takeAwayNumberText;
        public CustomTextView takeAwayNumberGreenText;
        public CustomTextView customChronometer;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            linearLayout = (LinearLayout) view.findViewById(R.id.take_away_container);
            takeAwayNumberText = (CustomTextView) view.findViewById(R.id.take_away_number);
            takeAwayNumberGreenText = (CustomTextView) view.findViewById(R.id.take_away_number_green);
            customChronometer = (CustomTextView) view.findViewById(R.id.take_away_chronometer) ;

        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+takeAwayNumberText .getText().toString();}
    }

    public interface AdapterCallback {
        void showPaymentFromOrder(int billId, int orderNumber);

        void showPaymentFromOrderWithTable(int billId, int orderNumber, int tableNumber);
    }

    /**
     * (Added by Fabrizio)
     * due funzioni per fare il refresh delle liste degli ordini, pagati o no
     */
    public void setPaidBillsList(){
        long time = System.currentTimeMillis();
        //long sessionTime = dbA.getSessionTime(time);
        long sessionTime = dbA.getLastClosing();
        Date date=new Date(sessionTime);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateText = df2.format(date);
        totalBillsList = dbA.getBillsList("Select * from bill_total where paid!=0 AND creation_time>='"
                + dateText + "' and id NOT IN (SELECT total_bill_id FROM table_use) ORDER BY pay_time;");
        Collections.reverse(totalBillsList);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_tv)).setText(R.string.recent_take_away);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_central_tv)).setText(R.string.recent_tables);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_right_tv)).setText(R.string.recent_applications);
        paidOrders = true;
    }

    public void setPaidBillsListFromServer(ArrayList<TotalBill> myTotals){
        totalBillsList = myTotals;
        ((CustomTextView)((Activity)context).findViewById(R.id.title_tv)).setText(R.string.recent_take_away);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_central_tv)).setText(R.string.recent_tables);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_right_tv)).setText(R.string.recent_applications);
        paidOrders = true;
    }

    public void setBillsList(){
        long time = System.currentTimeMillis();
        //long sessionTime = dbA.getSessionTime(time);
        long sessionTime = dbA.getLastClosing();
        Date date=new Date(sessionTime);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateText = df2.format(date);
        totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 AND creation_time>='"
                + dateText + "' and id NOT IN (SELECT total_bill_id FROM table_use);");
        Collections.reverse(totalBillsList);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_tv)).setText(R.string.take_away);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_central_tv)).setText(R.string.tables);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_right_tv)).setText(R.string.applications);
        paidOrders = false;
    }

    public void setBillsListFromServer(ArrayList<TotalBill> myTotals){
        totalBillsList = myTotals;
        ((CustomTextView)((Activity)context).findViewById(R.id.title_tv)).setText(R.string.take_away);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_central_tv)).setText(R.string.tables);
        ((CustomTextView)((Activity)context).findViewById(R.id.title_right_tv)).setText(R.string.applications);
        paidOrders = false;
    }


}
