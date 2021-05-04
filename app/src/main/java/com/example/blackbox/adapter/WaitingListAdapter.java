package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.ReservationsActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.WaitingListModel;
import com.utils.db.DatabaseAdapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Fabrizio on 18/03/2019.
 */

public class WaitingListAdapter extends RecyclerView.Adapter{

    private final float density;
    private DatabaseAdapter dbA;
    private Context context;
    private Resources resources;
    private float dpHeight;
    private float dpWidth;
    private ReservationsActivity reservationsActivity;
    private LayoutInflater inflater;
    private ArrayList<WaitingListModel> waitingList;
    private boolean searchMode = false;

    public WaitingListAdapter(Context c, DatabaseAdapter db, ReservationsActivity activity){
        this.context = c;
        this.dbA = db;
        this.reservationsActivity = activity;
        resources = reservationsActivity.getResources();
        inflater = reservationsActivity.getLayoutInflater();
        /*Date now = Calendar.getInstance().getTime();
        dbA.deleteOldWaitingList(now.toString());*/

        SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = formatPre.format(cal.getTime());
        dbA.deleteOldWaitingList(yesterday);

        waitingList = dbA.fetchWaitingList();

        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.waiting_list_element, null);
        return new WaitingListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WaitingListHolder wlHolder = (WaitingListHolder)holder;
        WaitingListModel waiting = waitingList.get(position);

        wlHolder.waitListName.setText(waiting.getName() + " " + waiting.getSurname());
        wlHolder.waitListType.setText(resources.getString(R.string.adults_children_disabled,
                waiting.getAdults(), waiting.getChildren(), waiting.getDisabled()));

        GradientDrawable border = new GradientDrawable();
        border.setColor(ContextCompat.getColor(context, R.color.red));
        border.setStroke((int) (6*density), 0xFF666666);
        wlHolder.totalLayout.setBackground(border);

        long creationt = waiting.getArrivalTime().getTime();
        long actualtime = Calendar.getInstance().getTime().getTime();
        long diffInSec = ((creationt - actualtime)/1000);
        long minuteDifference = (TimeUnit.SECONDS.toMinutes(diffInSec));

        if(minuteDifference<=999) {
            String formattedMinute = new DecimalFormat("000").format((minuteDifference * -1));
            wlHolder.waitListTime.setText(formattedMinute);
        }
        else wlHolder.waitListTime.setText("999");

        wlHolder.totalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireWaitingListPopup(waiting);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(waitingList != null)
            return waitingList.size();
        else
            return 0;
    }

    public void setSearchMode(boolean value){
        searchMode = value;
        if(searchMode){
            if(waitingList != null){
                waitingList.clear();
                notifyDataSetChanged();
            }
        }
        else{
            Calendar nowDate = Calendar.getInstance();
            dbA.deleteOldWaitingList(nowDate.getTime().toString());
            waitingList = dbA.fetchWaitingList();
            notifyDataSetChanged();
        }
    }

    public void refreshWaitingList(){
        if(waitingList != null)
            waitingList = dbA.fetchWaitingList();
        notifyDataSetChanged();
    }

    //it calls dbA method to search reservation
    public void searchWaitingListElement(String key){
        if(!key.equals("")){
            waitingList = dbA.searchWaitingListElement(key);
            notifyDataSetChanged();
        }
    }

    public void fireWaitingListPopup(WaitingListModel model){
        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.yes_no_dialog, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        RelativeLayout footer = (RelativeLayout)popupView.findViewById(R.id.footer);
        footer.setVisibility(View.VISIBLE);

        CustomTextView header = (CustomTextView)popupView.findViewById(R.id.delete_window);
        header.setText(R.string.status_of_reservation);
        CustomButton deleteButton = (CustomButton)popupView.findViewById(R.id.cancel_button);
        deleteButton.setText(R.string.cancel_element);
        deleteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.popup_button_red));
        CustomButton modifyButton = (CustomButton)popupView.findViewById(R.id.delete_button);
        modifyButton.setText(R.string.set_table);
        modifyButton.setBackground(ContextCompat.getDrawable(context, R.drawable.popup_button_green));

        ImageButton okButton = (ImageButton)popupView.findViewById(R.id.ok);
        ImageButton kill = (ImageButton)popupView.findViewById(R.id.kill);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dbA.deleteWaitingListElement(model.getId());
                popupWindow.dismiss();
                refreshWaitingList();
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                dbA.deleteWaitingListElement(model.getId());
                popupWindow.dismiss();

                reservationsActivity.changeActivity();
                //reservationsActivity.openModifyMode(model);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });

        kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    /**
     * HOLDER
     */
    public static class WaitingListHolder extends RecyclerView.ViewHolder{
        public View totalView;
        public RelativeLayout totalLayout;
        public CustomTextView waitListName;
        public RelativeLayout waitListNameLayout;
        public CustomTextView waitListTime;
        public CustomTextView waitListType;

        public WaitingListHolder(View itemView){
            super(itemView);
            totalView = itemView;
            totalLayout = (RelativeLayout) itemView.findViewById(R.id.waiting_list_display);
            waitListName = (CustomTextView) itemView.findViewById(R.id.waiting_list_name);
            waitListNameLayout = (RelativeLayout) itemView.findViewById(R.id.res_name_layout);
            waitListTime = (CustomTextView) itemView.findViewById(R.id.waiting_time);
            waitListType = (CustomTextView) itemView.findViewById(R.id.waiting_list_type);
        }
    }
}
