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
import com.example.blackbox.activities.Operative;
import com.example.blackbox.activities.ReservationsActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.DateUtils;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.Reservation;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.Table;
import com.example.blackbox.model.TableUse;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Fabrizio on 28/05/2018.
 */

public class ReservationsAdapter extends RecyclerView.Adapter
{

    private final float density;
    private DatabaseAdapter dbA;
    private Context context;
    private Resources resources;
    private float dpHeight;
    private float dpWidth;
    private ReservationsActivity reservationsActivity;
    private LayoutInflater inflater;
    private ArrayList<Reservation> reservations;
    private boolean popup;
    private Operative operative;

    private ArrayList<Reservation> reservationWithTables = new ArrayList<>();

    private int minsNotifyLimit;




    public ReservationsAdapter(DatabaseAdapter dbA, Context context)
    {
        this.context = context;
        this.dbA = dbA;
        reservationsActivity = (ReservationsActivity) context;
        inflater = ((Activity) context).getLayoutInflater();
        reservations = new ArrayList<>();
        resources = ((Activity) context).getResources();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;


        if (StaticValue.blackbox)
        {
            RequestParam params = new RequestParam();
            params.add("reservationChecksum", dbA.getChecksumForTable("reservation"));
            reservationsActivity.callHttpHandler("/getReservationList", params);
        }

        else
            {  reservations = dbA.getReservationList(); }

        Collections.sort(reservations);


        popup = false;


        // check if any of the reservation that is going to be present soon
        // have a table avaible
        //checkTablesAvailability();
    }



    public ReservationsAdapter(DatabaseAdapter dbA, Context context, Operative op, ArrayList<Integer> array)
    {
        this.context = context;
        this.dbA = dbA;
        inflater = ((Activity) context).getLayoutInflater();
        reservations = new ArrayList<>();
        operative = op;
        resources = operative.getResources();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;

        reservations = this.dbA.getReservationsFromArray(array);
        popup = true;
    }



    //without reservationsActivity
    public ReservationsAdapter(DatabaseAdapter dbA, Context context, ArrayList<Integer> array)
    {
        this.context = context;
        this.dbA = dbA;
        operative = (Operative) context;
        inflater = ((Activity) context).getLayoutInflater();
        reservations = new ArrayList<>();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;

        reservations = this.dbA.getReservationsFromArray(array);
        popup = true;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = inflater.inflate(R.layout.element_reservation_list, null);

        // TODO add a personalization
        minsNotifyLimit = dbA.getReservationPopupTimer();

        return new ReservationHolder(itemView);
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ReservationHolder resHolder = (ReservationHolder) holder;
        Reservation res = reservations.get(position);


        resHolder.resName.setText(res.getName());

        resHolder.resType.setText(resources.getString(R.string.adults_children_disabled, res.getAdults(), res.getChildren(), res.getDisabled()));

        resHolder.resTime.setText(String.format("%02d:%02d", res.getTime().getHours(), res.getTime().getMinutes()));

        resHolder.dateFooter.setVisibility(View.GONE);


        // based on the previous element on the list of reservations
        // make visible the dateFooter View, that will display
        // the date of the reservation
        Date previousReservationTime;
        try
            { previousReservationTime = reservations.get(position - 1).getTime(); }
        catch (Exception e)
            { previousReservationTime = new Date(); }

        // the idea is: if two subsequent reservation have the same date, they have to be grouped
        // under the same dateFooter View (like a calendar)
        // otherwise, a new dateFooter must be shown

        GradientDrawable border = new GradientDrawable();
        border.setStroke((int) (6 * density), 0xFF666666);

        Date now = new Date();

        // Expired reservations
        // check if this reservation is in the past (before now)
        // and also if the previous reservation on the list was not expired (to prevent the dateFooter to appear more than once)
        if (res.getTime().before(now) )
        {
            border.setColor(ContextCompat.getColor(context, R.color.red));

            if (!(previousReservationTime.before(now)))
            {
                resHolder.dateFooter.setVisibility(View.VISIBLE);
                resHolder.dateFooterText.setText("Expired");
            }
        }


        // Today reservations
        // check if this reservation will be today
        else if ( (DateUtils.isSameDay(res.getTime(), now)) )
        {
            border.setColor(ContextCompat.getColor(context, R.color.green_2));

            // if the previous reservation was not today, show the dateFooter
            if ( !(DateUtils.isSameDay(res.getTime(), previousReservationTime)) )
            {
                resHolder.dateFooter.setVisibility(View.VISIBLE);
                resHolder.dateFooterText.setText(resources.getString(R.string.today));
            }

        }


        // Reservations for tomorrow
        else if (DateUtils.isWithinDaysFuture(res.getTime(), 1))
        {
            border.setColor(ContextCompat.getColor(context, R.color.gray2));

            // if the previous reservation was not tomorrow, show the dateFooter
            if ( !(DateUtils.isSameDay(res.getTime(), previousReservationTime)) )
            {
                resHolder.dateFooter.setVisibility(View.VISIBLE);
                resHolder.dateFooterText.setText(resources.getString(R.string.tomorrow));
            }
        }


        // Reservations for some other day
        else if ( (res.getTime().after(now)) && !(DateUtils.isSameDay(res.getTime(), previousReservationTime)))
        {
            border.setColor(ContextCompat.getColor(context, R.color.gray2));

            // if the previous reservation was on another day, show the dateFooter
            if ( !(DateUtils.isSameDay(res.getTime(), previousReservationTime)) )
            {
                resHolder.dateFooter.setVisibility(View.VISIBLE);
                resHolder.dateFooterText.setText(formatDateReadable(res.getTime()));
            }
        }


        resHolder.totalLayout.setBackground(border);


        //TODO
        // 1: add a stripes gray background

        resHolder.totalView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // from ReservationsActivity
                if (!popup)
                {
                    //just today's reservations
                    if (DateUtils.isToday(res.getTime()) && DateUtils.isWithinMinutesFuture(res.getTime(), minsNotifyLimit))
                        { throwGreenResPopup(res); }

                    // reservations with a table set, but different day
                    else
                    {
                        throwModifyPopup(res);
                        reservationsActivity.setMode(ReservationsActivity.MODIFY_RESERVATION_MODE);
                    }
                }

                else
                    { operative.startTableIntentFromResPopup(res); }
            }
        });
    }




    public static class ReservationHolder extends RecyclerView.ViewHolder
    {
        View totalView;
        RelativeLayout totalLayout;
        CustomTextView resName;
        RelativeLayout resNameLayout;
        CustomTextView resTime;
        CustomTextView resType;
        RelativeLayout dateFooter;
        CustomTextView dateFooterText;

        ReservationHolder(View itemView)
        {
            super(itemView);
            totalView      = itemView;
            totalLayout    = itemView.findViewById(R.id.reservation_display);
            resName        = itemView.findViewById(R.id.reservation_name);
            resNameLayout  = itemView.findViewById(R.id.res_name_layout);
            resTime        = itemView.findViewById(R.id.reservation_time);
            resType        = itemView.findViewById(R.id.reservation_type);

            dateFooter     =  itemView.findViewById(R.id.reservation_date_footer);
            dateFooterText =  itemView.findViewById(R.id.reservation_date_footer_text);
        }
    }



    public void refreshReservationList(ArrayList<Reservation> reser)
    {
        reservations = reser;

        Collections.sort(reservations);

        notifyDataSetChanged();
    }


    public void refreshReservationList()
    {
        reservations = dbA.getReservationList();

        Collections.sort(reservations);

        notifyDataSetChanged();
    }




    @Override
    public int getItemCount()
        { return reservations != null ? reservations.size() : 0; }



    public void setSearchMode(boolean value)
    {
        if (value)
        {
            if (reservations != null)
            {
                reservations.clear();
                notifyDataSetChanged();
            }
        }

        else
        {
            reservations = dbA.getReservationList();
            notifyDataSetChanged();
        }
    }



    //it calls dbA method to search reservation
    public void searchReservation(String key)
    {
        if (!key.equals(""))
        {
            reservations = dbA.searchReservations(key);
            notifyDataSetChanged();
        }
    }



    public void filter(int kind)
    {
        reservations = dbA.getReservationList();
        Date now = new Date();
        ArrayList<Reservation> res = new ArrayList<>();

        switch (kind)
        {
            // expired reservations
            case -1:
                for (Reservation r : reservations)
                if (DateUtils.isBeforeDay(r.getTime(), now))
                    { res.add(r); }
                break;

            // today res
            case 0:
                for (Reservation r : reservations)
                if (DateUtils.isSameDay(r.getTime(), now))
                    { res.add(r); }
                break;

            // upcoming res
            case 1:
                for (Reservation r : reservations)
                if (DateUtils.isAfterDay(r.getTime(), now))
                    { res.add(r); }
                break;

            default:
                break;
        }

        reservations = res;
        notifyDataSetChanged();

    }








    private void throwModifyPopup(Reservation res)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_yes_no, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);



        RelativeLayout footer = popupView.findViewById(R.id.footer);
        footer.setVisibility(View.VISIBLE);

        CustomTextView header =  popupView.findViewById(R.id.delete_window);
        header.setText(R.string.status_of_reservation);

        CustomButton deleteButton = popupView.findViewById(R.id.cancel_button);
        deleteButton.setText(R.string.cancel_reservation);

        deleteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.popup_button_red));
        CustomButton modifyButton = popupView.findViewById(R.id.delete_button);
        modifyButton.setText(R.string.modify_reservation);

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
                    params.add("id", res.getReservation_id());

                    ((ReservationsActivity) context).callHttpHandler("/deleteReservation", params);
                }

                dbA.deleteReservation(res.getReservation_id());
                popupWindow.dismiss();
                refreshReservationList(dbA.getReservationList());
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();
                reservationsActivity.openModifyMode(res);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
                { popupWindow.dismiss(); }
        });

        kill.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
                { popupWindow.dismiss(); }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
    }



    private void throwGreenResPopup(Reservation res)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_reservation_green, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);


        popupView.findViewById(R.id.clients_arrived_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dbA.deleteReservation(res.getReservation_id());
                refreshReservationList(dbA.getReservationList());

                reservationsActivity.changeActivity();

                popupWindow.dismiss();
            }
        });


        popupView.findViewById(R.id.cancel_res_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dbA.deleteReservation(res.getReservation_id());
                refreshReservationList(dbA.getReservationList());

                popupWindow.dismiss();
            }
        });


        popupView.findViewById(R.id.clients_not_arrived_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();
                reservationsActivity.openModifyMode(res);
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
    }




    private void checkTablesAvailability()
    {
        ArrayList<Reservation> reservations = dbA.getReservationList();
        Date now = new Date();

        // for each reservation, if the reservation is in the next hours,
        // start the loop
        for (Reservation res : reservations)
        if (DateUtils.isSameDay(res.getTime(), now) && DateUtils.isWithinMinutesFuture(res.getTime(), minsNotifyLimit))
        {
            // for each room check if there is at least one table
            // that have enough people, and if that table is already in use,
            // there is a table available for that registration
            ArrayList<Room> rooms = dbA.fetchRooms();
            for (Room room : rooms)
            {
                // get the data from both the tables and the table_use database
                ArrayList<TableUse> tablesUsage = dbA.fetchTableUses(room.getId());
                ArrayList<Table> tables = dbA.fetchTablesName();

                // look at all the tables, and if one is found with enough seats,
                // check if it's in usage
                tableLoop:
                for (Table table : tables)
                if (table.getPeopleNumber() >= (res.getAdults() + res.getChildren() + res.getDisabled()))
                {
                    // loop over all table usage. If this table is not in usage,
                    // we have found our winner table!
                    for (TableUse tableUse : tablesUsage)
                    if (table.getId() == tableUse.getTableId())
                        { break tableLoop; }

                    // if the current table is not in usage, and have enough seats.
                    // this reservation can be used.
                    reservationWithTables.add(res);
                    break tableLoop;
                }
            }
        }
    }




    private String formatDateReadable(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String month = "Unk";
        switch (cal.get(Calendar.MONTH) + 1)
        {
            case 1:  month = resources.getString(R.string.monthJanuary); break;
            case 2:  month = resources.getString(R.string.monthFebruary); break;
            case 3:  month = resources.getString(R.string.monthMarch); break;
            case 4:  month = resources.getString(R.string.monthApril); break;
            case 5:  month = resources.getString(R.string.monthMay); break;
            case 6:  month = resources.getString(R.string.monthJune); break;
            case 7:  month = resources.getString(R.string.monthJuly); break;
            case 8:  month = resources.getString(R.string.monthAugust); break;
            case 9:  month = resources.getString(R.string.monthSeptember); break;
            case 10: month = resources.getString(R.string.monthOctober); break;
            case 11: month = resources.getString(R.string.monthNovember); break;
            case 12: month = resources.getString(R.string.monthDecember); break;
            default: break;
        }

        return String.format("%s %s %4d",
                    cal.get(Calendar.DAY_OF_MONTH), month.substring(0,3), cal.get(Calendar.YEAR));

    }

}
