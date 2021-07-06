package com.example.blackbox.adapter;

/**
 * Created by tiziano on 9/2/17.
 */

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
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.Table;
import com.example.blackbox.model.TotalBill;
import com.example.blackbox.model.TwoString;
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

public class TableOrderAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {

    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    ArrayList<TotalBill> totalBillsList;
    private TakeAwayAdapter.AdapterCallback mAdapterCallback;
    private boolean paidOrders = false;
    ArrayList<TwoString> tableNumbers;
    public View myPopupView;
    public PopupWindow myPopupWindow;

    public void setTableNumbers(ArrayList<TwoString>  t){tableNumbers = t;}

    public int returnTableNumberForBillId(int billId){
        int toReturn = -1;
        for(TwoString t : tableNumbers){
            if(Integer.valueOf(t.getSecondString())==billId) {
                toReturn = Integer.valueOf(t.getFirstString());
            }
        }
        return toReturn;
    }

    public TableOrderAdapter(Context c , DatabaseAdapter database){
        context = c;
        dbA = database;
        this.mAdapterCallback = ((TakeAwayAdapter.AdapterCallback) context);
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        //View view = inflater.inflate(R.layout.take_away_recycler, null);
        if(StaticValue.blackbox){
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            params.add(new BasicNameValuePair("androidId", android_id));
            ((OrderActivity) context).callHttpHandler("/getTableOrders", params);
        }else {
            totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 and id IN (SELECT total_bill_id FROM table_use);");
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
        v = inflater.inflate(R.layout.recycler_table_order, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TotalBill totalBill = totalBillsList.get(position);
        ButtonHolder button = (ButtonHolder) holder;

        int tnumber = -1;
        int billId = totalBill.getId();
        //paidOrder -> green
        if(StaticValue.blackbox){
            int numberId = returnTableNumberForBillId(billId);
            Table table = dbA.getTableById(numberId);
            tnumber = table.getTableNumber();
        }else {
          tnumber= dbA.getTableNumberId(billId);
        }
        int tableNumber =tnumber;
        if(paidOrders){
            button.tableOrderNumberText.setVisibility(View.GONE);
            button.tableOrderNumberGreenText.setVisibility(View.VISIBLE);
            if(!StaticValue.blackbox) {
                int client_id = dbA.getClientFromProductBill(totalBill.getId());
                if (client_id != -1) {
                    button.tableOrderText.setText(dbA.fetchSingleClient(client_id).getName());
                }
                if (tableNumber != -1) {
                    String formattedTable = new DecimalFormat("00").format((tableNumber));
                    button.tableOrderNumberGreenText.setText(formattedTable);
                }
                if (tableNumber != -1 && client_id == -1) {
                    int roomId = dbA.getRoomIdAgain(billId);
                    Room room = dbA.fetchRoomById(roomId);
                    String roomName = room.getName();
                    button.tableOrderText.setText(roomName);
                } else {
                    button.tableOrderText.setText("");
                }
            }else{
                if(StaticValue.blackbox){
                    int numberId = returnTableNumberForBillId(billId);
                    Table table = dbA.getTableById(numberId);
                    Room room = dbA.fetchRoomById(table.getRoomId());
                    String formattedTable = new DecimalFormat("00").format((table.getTableNumber()));
                    button.tableOrderNumberGreenText.setText(formattedTable);
                    button.tableOrderText.setText(String.valueOf(room.getName()));
                }else {
                    Table table = dbA.getTableById(tableNumber);
                    tnumber= dbA.getTableNumberId(billId);
                    Room room = dbA.fetchRoomById(table.getRoomId());
                    String formattedTable = new DecimalFormat("00").format((table.getTableNumber()));
                    button.tableOrderNumberGreenText.setText(formattedTable);
                    button.tableOrderText.setText(String.valueOf(room.getName()));
                }

            }
        }
        //unpaidOrder -> red
        else if(!paidOrders){
            button.tableOrderNumberGreenText.setVisibility(View.GONE);
            button.tableOrderNumberText.setVisibility(View.VISIBLE);
            if(!StaticValue.blackbox) {
                int client_id = dbA.getClientFromProductBill(totalBill.getId());
                if(client_id != -1){
                    button.tableOrderText.setText(dbA.fetchSingleClient(client_id).getName());
                }
                if(tableNumber!=-1) {
                    String formattedTable = new DecimalFormat("00").format((tableNumber));
                    button.tableOrderNumberText.setText(formattedTable);
                }
                if (tableNumber != -1 && client_id == -1) {
                    int roomId = dbA.getRoomIdAgain(billId);
                    Room room = dbA.fetchRoomById(roomId);
                    String roomName = room.getName();
                    button.tableOrderText.setText(roomName);
                } else {
                    button.tableOrderText.setText("");
                }
            }else{
                int numberId = returnTableNumberForBillId(billId);
                Table table = dbA.getTableById(numberId);
                Room room = dbA.fetchRoomById(table.getRoomId());
                String formattedTable = new DecimalFormat("00").format((table.getTableNumber()));
                button.tableOrderNumberText.setText(formattedTable);
                button.tableOrderText.setText(String.valueOf(room.getName()));
            }
        }

        Date d = totalBill.getCreationTime();
        Date d2 = Calendar.getInstance().getTime();
        long diffInSec =    ((totalBill.getCreationTime().getTime() -  Calendar.getInstance().getTime().getTime())/1000);

        long minuteDifference = (TimeUnit.SECONDS.toMinutes(diffInSec));
        if(minuteDifference<=999) {
            String formattedMinute = new DecimalFormat("000").format((minuteDifference * -1));
            button.customChronometer.setText(formattedMinute);
        }else button.customChronometer.setText("999");

        if(position%2==0){
            //pari
            int color2 = Color.parseColor("#222222");
            int color = Color.parseColor("#444444");
            button.linearLayout.setBackgroundColor(color);
            button.customChronometer.setBackgroundColor(color2);
            button.tableOrderText.setBackgroundColor(color);
        }else{
            //dispari
            int color = Color.parseColor("#222222");
            button.linearLayout.setBackgroundColor(color);
            int color2 = Color.parseColor("#444444");
            button.customChronometer.setBackgroundColor(color2);
            button.tableOrderText.setBackgroundColor(color);
        }
        final int total = totalBill.getId();
        final int number = totalBill.getBillNumber()+1;
        button.view.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {

                        try {
                            mAdapterCallback.showPaymentFromOrderWithTable(total, number, tableNumber);
                        } catch (ClassCastException exception) {
                            // do something
                        }

                    }
                }
        );

        button.view.setOnTouchListener(new OnSwipeTouchListener(context){
            public void onSwipeLeft(){
                if(!paidOrders){
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

                    CustomButton delete = (CustomButton)popupView.findViewById(R.id.first_button);
                    CustomButton unpaid = (CustomButton)popupView.findViewById(R.id.second_button);
                    CustomButton partial = (CustomButton)popupView.findViewById(R.id.third_button);
                    CustomButton cancel = (CustomButton)popupView.findViewById(R.id.cancel_delete_button);

                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(StaticValue.blackbox){
                                myPopupView = popupView;
                                myPopupWindow = popupWindow;
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(totalBill.getId())));
                                params.add(new BasicNameValuePair("type", String.valueOf(3)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTableList", params);
                            }else {

                                dbA.modifyTablePaymentInBill(totalBill.getId(), 3);
                                totalBillsList = dbA.getBillsList
                                        ("Select * from bill_total where paid=0 and id IN (SELECT total_bill_id FROM table_use);");
                                notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                        }
                    });

                    unpaid.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(StaticValue.blackbox){
                                myPopupView = popupView;
                                myPopupWindow = popupWindow;
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(totalBill.getId())));
                                params.add(new BasicNameValuePair("type", String.valueOf(4)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTableList", params);
                            }else {
                                dbA.modifyTablePaymentInBill(totalBill.getId(), 4);
                                totalBillsList = dbA.getBillsList
                                        ("Select * from bill_total where paid=0 and id IN (SELECT total_bill_id FROM table_use);");
                                notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                        }
                    });

                    partial.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(StaticValue.blackbox){
                                myPopupView = popupView;
                                myPopupWindow = popupWindow;
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(totalBill.getId())));
                                params.add(new BasicNameValuePair("type", String.valueOf(5)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTableList", params);
                            }else {
                                dbA.modifyTablePaymentInBill(totalBill.getId(), 5);
                                dbA.deleteSubdivisionItems(totalBill.getId());
                                totalBillsList = dbA.getBillsList
                                        ("Select * from bill_total where paid=0 and id IN (SELECT total_bill_id FROM table_use);");
                                notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                        }
                    });

                    popupWindow.setFocusable(true);
                    popupWindow.showAtLocation(((OrderActivity)context).findViewById(R.id.main),0,0,0);
                }
                else{
                    Toast.makeText(context, R.string.you_cant_modify, Toast.LENGTH_SHORT).show();
                }
            }

            public void onClick(){
                try {
                    mAdapterCallback.showPaymentFromOrderWithTable(total, number, tableNumber);
                }
                catch (ClassCastException exception) {
                    // do something
                }
            }

            public void onLongClick(){
                if(!paidOrders){
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

                    CustomButton delete = (CustomButton)popupView.findViewById(R.id.first_button);
                    CustomButton unpaid = (CustomButton)popupView.findViewById(R.id.second_button);
                    CustomButton partial = (CustomButton)popupView.findViewById(R.id.third_button);
                    CustomButton cancel = (CustomButton)popupView.findViewById(R.id.cancel_delete_button);

                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(StaticValue.blackbox){
                                myPopupView = popupView;
                                myPopupWindow = popupWindow;
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(totalBill.getId())));
                                params.add(new BasicNameValuePair("type", String.valueOf(3)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTableList", params);
                            }else {
                                dbA.modifyTablePaymentInBill(totalBill.getId(), 3);
                                totalBillsList = dbA.getBillsList
                                        ("Select * from bill_total where paid=0 and id IN (SELECT total_bill_id FROM table_use);");
                                notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                        }
                    });

                    unpaid.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(StaticValue.blackbox){
                                myPopupView = popupView;
                                myPopupWindow = popupWindow;
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(totalBill.getId())));
                                params.add(new BasicNameValuePair("type", String.valueOf(4)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTableList", params);
                            }else {
                                dbA.modifyTablePaymentInBill(totalBill.getId(), 4);
                                totalBillsList = dbA.getBillsList
                                        ("Select * from bill_total where paid=0 and id IN (SELECT total_bill_id FROM table_use);");
                                notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                        }
                    });

                    partial.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(StaticValue.blackbox){
                                myPopupView = popupView;
                                myPopupWindow = popupWindow;
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("billId", String.valueOf(totalBill.getId())));
                                params.add(new BasicNameValuePair("type", String.valueOf(5)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                ((OrderActivity) context).callHttpHandler("/payBillTypeReturnTableList", params);
                            }else {
                                dbA.modifyTablePaymentInBill(totalBill.getId(), 5);
                                dbA.deleteSubdivisionItems(totalBill.getId());
                                totalBillsList = dbA.getBillsList
                                        ("Select * from bill_total where paid=0 and id IN (SELECT total_bill_id FROM table_use);");
                                notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                        }
                    });

                    popupWindow.setFocusable(true);
                    popupWindow.showAtLocation(((OrderActivity)context).findViewById(R.id.main),0,0,0);
                }
                else{
                    Toast.makeText(context, R.string.you_cant_modify, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public int getItemCount() {
        if(totalBillsList!=null) return totalBillsList.size();
        else return 0;
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
        public CustomTextView tableOrderNumberText;
        public CustomTextView tableOrderNumberGreenText;
        public CustomTextView tableOrderText;
        public CustomTextView customChronometer;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            linearLayout = (LinearLayout) view.findViewById(R.id.table_order_container);
            tableOrderNumberText = (CustomTextView) view.findViewById(R.id.table_order_number);
            tableOrderNumberGreenText = (CustomTextView) view.findViewById(R.id.table_order_number_green);
            tableOrderText = (CustomTextView) view.findViewById(R.id.table_order_text);
            customChronometer = (CustomTextView) view.findViewById(R.id.table_order_chronometer) ;

        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+tableOrderNumberText .getText().toString();}
    }

    /**
     * (Added by Fabrizio)
     * Metodi per refresh dei current orders (non pagati) o dei recent orders (pagati)
     */
    public void setPaidTables(){
        long sessionTime = dbA.getLastClosing();
        Date date=new Date(sessionTime);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateText = df2.format(date);
        totalBillsList = dbA.getBillsList("Select * from bill_total where paid!=0 " +
                "AND creation_time>='"+ dateText + "' and id IN (SELECT total_bill_id FROM table_use) ORDER BY pay_time");
        paidOrders = true;
    }

    public void setPaidTablesFromServer(ArrayList<TotalBill> myTotals, ArrayList<TwoString> myTablesNumbers){
        totalBillsList = myTotals;
        setTableNumbers(myTablesNumbers);
        paidOrders = true;
    }

    public void setCurrentTables(){
        long sessionTime = dbA.getLastClosing();
        Date date=new Date(sessionTime);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateText = df2.format(date);
        totalBillsList = dbA.getBillsList("Select * from bill_total where paid=0 " +
                "AND creation_time>='"+ dateText + "' and id IN (SELECT total_bill_id FROM table_use);");
        paidOrders = false;
    }

    public void setCurrentTablesFromServer(ArrayList<TotalBill> myTotals, ArrayList<TwoString> myTablesNumbers){
        totalBillsList = myTotals;
        setTableNumbers(myTablesNumbers);
        paidOrders = false;
    }

}
