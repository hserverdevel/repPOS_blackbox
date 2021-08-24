package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import com.example.blackbox.model.DateUtils;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.Reservation;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.WaitingListModel;
import com.utils.db.DatabaseAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Fabrizio on 18/03/2019.
 */

public class WaitingListAdapter extends RecyclerView.Adapter
{

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

    public WaitingListAdapter(Context c, DatabaseAdapter db, ReservationsActivity activity)
    {
        this.context = c;
        this.dbA = db;
        this.reservationsActivity = activity;
        resources = reservationsActivity.getResources();
        inflater = reservationsActivity.getLayoutInflater();

        SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = formatPre.format(cal.getTime());


        if (StaticValue.blackbox)
        {
            RequestParam params = new RequestParam();
            params.add("waitingListChecksum", dbA.getChecksumForTable("waiting_list"));
            reservationsActivity.callHttpHandler("/getWaitingList", params);
        }

        else
            {  waitingList = dbA.fetchWaitingList(); }


        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = inflater.inflate(R.layout.element_waiting_list, null);
        return new WaitingListHolder(itemView);
    }




    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        WaitingListHolder wlHolder = (WaitingListHolder) holder;
        WaitingListModel waiting = waitingList.get(position);

        wlHolder.waitListName.setText(waiting.getName());
        wlHolder.waitListType.setText(resources.getString(R.string.adults_children_disabled, waiting.getAdults(), waiting.getChildren(), waiting.getDisabled()));


        // get how many minutes have been passed since the creation date
        // of this waiting list
        long minDiff = ( (Calendar.getInstance().getTime().getTime() - waiting.getTime().getTime()) / (60 * 1000) );

        String minStr = minDiff > 999 ? "999" : String.valueOf(minDiff);

        wlHolder.waitListTime.setText(minStr);


        GradientDrawable border = new GradientDrawable();
        border.setStroke((int) (6 * density), 0xFF666666);

        if (minDiff > 10)
            { border.setColor(ContextCompat.getColor(context, R.color.red)); }
        else
            { border.setColor(ContextCompat.getColor(context, R.color.gray2)); }

        wlHolder.totalLayout.setBackground(border);



        wlHolder.totalLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fireWaitingListPopup(waiting);
            }
        });
    }



    @Override
    public int getItemCount()
    {
        if (waitingList != null)
        { return waitingList.size(); }
        else
        { return 0; }
    }



    public void setSearchMode(boolean value)
    {
        searchMode = value;
        if (searchMode)
        {
            if (waitingList != null)
            {
                waitingList.clear();
                notifyDataSetChanged();
            }
        }
        else
        {
            Calendar nowDate = Calendar.getInstance();
            waitingList = dbA.fetchWaitingList();
            notifyDataSetChanged();
        }
    }



    public void refreshWaitingList()
    {
        if (waitingList != null)
        { waitingList = dbA.fetchWaitingList(); }
        notifyDataSetChanged();
    }

    public void refreshWaitingList(ArrayList<WaitingListModel> wlt)
    {
        waitingList = wlt;

        notifyDataSetChanged();
    }




    //it calls dbA method to search reservation
    public void searchWaitingListElement(String key)
    {
        if (!key.equals(""))
        {
            waitingList = dbA.searchWaitingListElement(key);
            notifyDataSetChanged();
        }
    }



    public void filter(int kind)
    {
        waitingList = dbA.fetchWaitingList();
        Date                   now = new Date();
        ArrayList<WaitingListModel> res = new ArrayList<>();

        switch (kind)
        {
            // expired reservations
            case -1:
                for (WaitingListModel r : waitingList)
                    if (DateUtils.isBeforeDay(r.getTime(), now))
                    { res.add(r); }
                break;

            // today res
            case 0:
                for (WaitingListModel r : waitingList)
                    if (DateUtils.isSameDay(r.getTime(), now))
                    { res.add(r); }
                break;

            // upcoming res
            case 1:
                for (WaitingListModel r : waitingList)
                    if (DateUtils.isAfterDay(r.getTime(), now))
                    { res.add(r); }
                break;

            default:
                break;
        }

        waitingList = res;
        notifyDataSetChanged();

    }




    private void fireWaitingListPopup(WaitingListModel model)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_yes_no, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        RelativeLayout footer = popupView.findViewById(R.id.footer);
        footer.setVisibility(View.VISIBLE);

        CustomTextView header = popupView.findViewById(R.id.delete_window);
        header.setText(R.string.status_of_reservation);

        CustomButton deleteButton = popupView.findViewById(R.id.cancel_button);
        deleteButton.setText(R.string.cancel_element);

        deleteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.popup_button_red));
        CustomButton modifyButton =  popupView.findViewById(R.id.delete_button);
        modifyButton.setText(R.string.set_table);

        modifyButton.setBackground(ContextCompat.getDrawable(context, R.drawable.popup_button_green));

        ImageButton okButton = popupView.findViewById(R.id.ok);
        ImageButton kill = popupView.findViewById(R.id.kill);


        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (StaticValue.blackbox)
                {
                    RequestParam params = new RequestParam();
                    params.add("id", model.getId());

                    ((ReservationsActivity) context).callHttpHandler("/deleteWaitingList", params);
                }

                else
                { dbA.deleteWaitingListElement(model.getId()); }

                popupWindow.dismiss();

                refreshWaitingList();

            }
        });


        modifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();

                reservationsActivity.changeActivity();
                //reservationsActivity.openModifyMode(model);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                popupWindow.dismiss();
            }
        });

        kill.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                popupWindow.dismiss();
            }
        });

        // THROW POPUP WINDOW AFTER SETTING EVERYTHING UP
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
    }



    public static class WaitingListHolder extends RecyclerView.ViewHolder
    {
        public View totalView;
        public RelativeLayout totalLayout;
        public CustomTextView waitListName;
        public RelativeLayout waitListNameLayout;
        public CustomTextView waitListTime;
        public CustomTextView waitListType;

        public WaitingListHolder(View itemView)
        {
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
