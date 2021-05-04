package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.Operative;
import com.example.blackbox.activities.ReservationsActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.Reservation;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Fabrizio on 28/05/2018.
 */

public class ReservationsAdapter extends RecyclerView.Adapter{

    private final float density;
    private DatabaseAdapter dbA;
    private Context context;
    private Resources resources;
    private float dpHeight;
    private float dpWidth;
    private ReservationsActivity reservationsActivity;
    private LayoutInflater inflater;
    private ArrayList<Reservation> reservations;
    private boolean searchMode = false;
    private int nowYear;
    private int nowMonth;
    private int nowDay;
    private int year;
    private int month;
    private int day;
    private boolean popup = false;
    private Operative operative;

    public ReservationsAdapter(DatabaseAdapter dbA, Context context) {
        this.context = context;
        this.dbA = dbA;
        reservationsActivity = (ReservationsActivity)context;
        inflater = ((Activity)context).getLayoutInflater();
        reservations = new ArrayList<>();
        resources = ((Activity)context).getResources();

        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;

        Calendar now = Calendar.getInstance();
        nowYear = now.get(Calendar.YEAR);
        nowMonth = now.get(Calendar.MONTH)+1;
        nowDay = now.get(Calendar.DAY_OF_MONTH);

        //before taking all reservations, delete old reservations
        this.dbA.deleteOldReservations(formatNowDate());
        reservations = this.dbA.getReservationList(formatNowDate());

        dbA.showData("reservation");
        popup = false;
    }

    public ReservationsAdapter(DatabaseAdapter dbA, Context context, Operative op, ArrayList<Integer> array) {
        this.context = context;
        this.dbA = dbA;
        inflater = ((Activity)context).getLayoutInflater();
        reservations = new ArrayList<>();
        operative = op;
        resources = operative.getResources();

        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;

        Calendar now = Calendar.getInstance();
        nowYear = now.get(Calendar.YEAR);
        nowMonth = now.get(Calendar.MONTH)+1;
        nowDay = now.get(Calendar.DAY_OF_MONTH);

        reservations = this.dbA.getReservationsFromArray(array);
        popup = true;
    }

    //without reservationsActivity
    public ReservationsAdapter(DatabaseAdapter dbA, Context context, ArrayList<Integer> array) {
        this.context = context;
        this.dbA = dbA;
        operative = (Operative)context;
        inflater = ((Activity)context).getLayoutInflater();
        reservations = new ArrayList<>();

        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;

        reservations = this.dbA.getReservationsFromArray(array);
        popup = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.reservation_list_element, null);
        return new ReservationHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ReservationHolder resHolder = (ReservationHolder) holder;
        Reservation res = reservations.get(position);

        resHolder.resName.setText(res.getName() + " " + res.getSurname());
        resHolder.resType.setText(
                resources.getString(
                        R.string.adults_children_disabled,
                        res.getAdults(), res.getChildren(), res.getDisabled()));

        String resTime = res.getReservation_time();
        if(resTime != null)
            resHolder.resTime.setText(parseTime(resTime));
        else
            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();

        if(compareDate(res.getReservation_date(), formatNowDate()) > 0){
            resHolder.resDate.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams ll = (RelativeLayout.LayoutParams)resHolder.resNameLayout.getLayoutParams();
            ll.setMarginStart((int)(20*density));
            ll.height = (int) (51*density);
            ll.width = (int) (239*density);
            resHolder.resNameLayout.setLayoutParams(ll);
            resHolder.resDate.setText(formatDate(res.getReservation_date()));
        }

        dbA.showData("reservation");

        //grey: not today reservations
        //red: today reservations with no table
        //green: today reservations with table
        if(res.getTable_use_id() == -1){
            GradientDrawable border = new GradientDrawable();
            border.setColor(ContextCompat.getColor(context, R.color.red));
            border.setStroke((int) (6*density), 0xFF666666);
            resHolder.totalLayout.setBackground(border);
        }
        else{
            GradientDrawable border = new GradientDrawable();
            border.setColor(ContextCompat.getColor(context, R.color.green_2));
            border.setStroke((int) (6*density), 0xFF666666);
            resHolder.totalLayout.setBackground(border);
        }
        resHolder.totalView.setTag(res);

        year = Integer.parseInt(res.getReservation_date().substring(0,4));
        month = Integer.parseInt(res.getReservation_date().substring(5,7));
        day = Integer.parseInt(res.getReservation_date().substring(8,10));

        resHolder.totalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //from ReservationsActivity
                if(!popup){
                    //just today's reservations
                    if(year == nowYear && month == nowMonth && day == nowDay){
                        //just reservations without a table
                        if(res.getTable_use_id() == -1){
                            Log.d("RESERVATION TABLE", res.getTable_use_id() + "");
                            throwModifyPopup(res);
                            reservationsActivity.setMode(ReservationsActivity.MODIFY_RESERVATION_MODE);
                        }
                        else{
                            Log.d("RESERVATION TABLE", res.getTable_use_id() + "");
                            throwGreenResPopup(res);
                        }

                    }
                    //reservations with a table set, but different day
                    else{
                        throwModifyPopup(res);
                        reservationsActivity.setMode(ReservationsActivity.MODIFY_RESERVATION_MODE);
                    }
                }
                else if(popup){
                    operative.startTableIntentFromResPopup(res);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservations!=null?reservations.size():0;
    }

    public void setSearchMode(boolean value){
        searchMode = value;
        if(searchMode){
            if(reservations != null){
                reservations.clear();
                notifyDataSetChanged();
            }
        }
        else{
            this.dbA.deleteOldReservations(formatNowDate());
            reservations = this.dbA.getReservationList(formatNowDate());
            notifyDataSetChanged();
        }
    }

    public void refreshReservationList(){
        if(reservations != null)
            reservations = dbA.getReservationList(formatNowDate());
        notifyDataSetChanged();
    }

    //it calls dbA method to search reservation
    public void searchReservation(String key){
        if(!key.equals("")){
            reservations = dbA.searchReservations(key);
            notifyDataSetChanged();
        }
    }

    /**
     * HOLDER
     */
    public static class ReservationHolder extends RecyclerView.ViewHolder{
        public View totalView;
        public RelativeLayout totalLayout;
        public CustomTextView resName;
        public RelativeLayout resNameLayout;
        public CustomTextView resDate;
        public CustomTextView resTime;
        public CustomTextView resType;

        public ReservationHolder(View itemView){
            super(itemView);
            totalView = itemView;
            totalLayout = (RelativeLayout) itemView.findViewById(R.id.reservation_display);
            resName = (CustomTextView) itemView.findViewById(R.id.reservation_name);
            resNameLayout = (RelativeLayout) itemView.findViewById(R.id.res_name_layout);
            resDate = (CustomTextView) itemView.findViewById(R.id.reservation_date);
            resTime = (CustomTextView) itemView.findViewById(R.id.reservation_time);
            resType = (CustomTextView) itemView.findViewById(R.id.reservation_type);
        }
    }

    public void throwModifyPopup(Reservation res){
        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.yes_no_dialog, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        /*RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)popupView.findViewById(R.id.delete_window)
                .getLayoutParams();
        int t = (int)(dpHeight-52)/2 - rlp.height/2;
        rlp.topMargin = t;*/

        RelativeLayout footer = (RelativeLayout)popupView.findViewById(R.id.footer);
        footer.setVisibility(View.VISIBLE);

        CustomTextView header = (CustomTextView)popupView.findViewById(R.id.delete_window);
        header.setText(R.string.status_of_reservation);
        CustomButton deleteButton = (CustomButton)popupView.findViewById(R.id.cancel_button);
        deleteButton.setText(R.string.cancel_reservation);
        deleteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.popup_button_red));
        CustomButton modifyButton = (CustomButton)popupView.findViewById(R.id.delete_button);
        modifyButton.setText(R.string.modify_reservation);
        modifyButton.setBackground(ContextCompat.getDrawable(context, R.drawable.popup_button_green));

        ImageButton okButton = (ImageButton)popupView.findViewById(R.id.ok);
        ImageButton kill = (ImageButton)popupView.findViewById(R.id.kill);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbA.deleteReservationWithoutTable(res.getReservation_id());
                popupWindow.dismiss();
                refreshReservationList();
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                reservationsActivity.openModifyMode(res);
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

    public void throwGreenResPopup(Reservation res){
        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.reservation_green_popup, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        /*RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)popupView.findViewById(R.id.confirm_window)
                .getLayoutParams();
        int t = (int)(dpHeight-52)/2 - rlp.height/2;
        rlp.topMargin = t;*/

        popupView.findViewById(R.id.clients_arrived_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbA.setReservationArrivalTime(res.getReservation_id());
                Calendar now = Calendar.getInstance();
                StringBuilder dateTime = new StringBuilder();
                StringBuilder timeDate = new StringBuilder();
                dateTime.append(now.get(Calendar.YEAR));
                int month = now.get(Calendar.MONTH)+1;
                if(month >= 10){
                    dateTime.append("-");
                    dateTime.append(month);
                }
                else{
                    dateTime.append("-0");
                    dateTime.append(month);
                }
                int day = now.get(Calendar.DAY_OF_MONTH);
                if(day >= 10){
                    dateTime.append("-");
                    dateTime.append(day);
                }
                else{
                    dateTime.append("/0");
                    dateTime.append(day);
                }
                timeDate.append(now.get(Calendar.HOUR_OF_DAY));
                timeDate.append(":");
                timeDate.append(now.get(Calendar.MINUTE));
                res.setStart_time(dateTime.toString() + " " + timeDate.toString());
                reservations = dbA.getReservationList(formatNowDate());
                notifyDataSetChanged();

                reservationsActivity.changeActivity();

                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.cancel_res_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbA.setCancelledStatus(res.getReservation_id());
                reservations = dbA.getReservationList(formatNowDate());
                notifyDataSetChanged();
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.clients_not_arrived_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbA.setNotArrivedStatus(res.getReservation_id());
                reservations = dbA.getReservationList(formatNowDate());
                notifyDataSetChanged();
                popupWindow.dismiss();
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    /**
     * dd-mm-yyyy
     * @param dateString
     * @return
     */
    public String parseDate(String dateString){
        StringBuilder dateSB = new StringBuilder();
        StringBuilder monthSB = new StringBuilder();
        StringBuilder daySB = new StringBuilder();

        dateSB.append(dateString.charAt(8));
        dateSB.append(dateString.charAt(9));
        day = Integer.parseInt(dateSB.toString());
        dateSB.append("-");
        dateSB.append(dateString.charAt(5));
        monthSB.append(dateString.charAt(5));
        dateSB.append(dateString.charAt(6));
        monthSB.append(dateString.charAt(6));
        month = Integer.parseInt(monthSB.toString());
        dateSB.append("-");
        dateSB.append(dateString.charAt(2));
        daySB.append(dateString.charAt(2));
        dateSB.append(dateString.charAt(3));
        daySB.append(dateString.charAt(3));
        year = Integer.parseInt(daySB.toString());

        return dateSB.toString();
    }

    /**
     * HH:MM
     * @param timeString
     * @return
     */
    public String parseTime(String timeString){
        StringBuilder timeSB = new StringBuilder();
        timeSB.append(timeString);

        return timeSB.toString();
    }

    //YYYY-MM-DD
    public String formatNowDate(){
        Calendar now = Calendar.getInstance();
        StringBuilder date = new StringBuilder();

        date.append(now.get(Calendar.YEAR));
        int month = now.get(Calendar.MONTH) + 1;
        if(month >= 10){
            date.append("-");
            date.append(month);
        }
        else{
            date.append("-0");
            date.append(month);
        }
        int day = now.get(Calendar.DAY_OF_MONTH);
        if(day >= 10){
            date.append("-");
            date.append(day);
        }
        else{
            date.append("-0");
            date.append(day);
        }

        return date.toString();
    }

    public String formatDate(String date){
        int year = Integer.parseInt(date.substring(0,4));
        int month = Integer.parseInt(date.substring(5,7));
        int day = Integer.parseInt(date.substring(8,10));
        StringBuilder realDate = new StringBuilder();

        if(day >= 10)
            realDate.append(day + "-");
        else
            realDate.append("0" + day + "-");
        if(month >= 10)
            realDate.append(month + "-");
        else
            realDate.append("0" + month + "-");
        realDate.append(year-2000);

        return realDate.toString();
    }

    public int compareDate(String date1, String date2){
        int year1 = Integer.parseInt(date1.substring(0,4));
        int year2 = Integer.parseInt(date2.substring(0,4));
        int month1 = Integer.parseInt(date1.substring(5,7));
        int month2 = Integer.parseInt(date2.substring(5,7));
        int day1 = Integer.parseInt(date1.substring(8,10));
        int day2 = Integer.parseInt(date2.substring(8,10));

        if(year1 >= year2){
            if(month1 >= month2){
                if(day1 > day2)
                    return 1;
                else if(day1 == day2)
                    return 0;
                else
                    return -1;
            }
            else
                return -1;
        }
        else
            return -1;
    }
}
