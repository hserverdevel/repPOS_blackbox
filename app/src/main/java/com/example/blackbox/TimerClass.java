package com.example.blackbox;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.blackbox.activities.Operative;
import com.example.blackbox.adapter.ReservationsAdapter;
import com.example.blackbox.model.DateUtils;
import com.example.blackbox.model.Reservation;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Fabrizio on 07/06/2018.
 */


public class TimerClass
{

    private final String TAG = "<Timer>";

    private final Timer reservationTimer = new Timer();
    private final Handler handler = new Handler();
    private final DatabaseAdapter dbA;
    private final Context context;
    private final Operative operative;
    private final float density;
    private final float dpHeight;
    private final float dpWidth;
    private boolean isRunning = false;

    public void setIsRunning(boolean value) {isRunning = value;}

    public boolean getIsRunning() {return isRunning;}

    public TimerClass(DatabaseAdapter database, Context me, Operative op)
    {
        this.dbA = database;
        this.context = me;
        this.operative = op;

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;
    }


    public void launchTimer()
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            StringBuilder date = new StringBuilder();
                            StringBuilder time = new StringBuilder();

                            Calendar now = Calendar.getInstance();
                            date.append(now.get(Calendar.YEAR));

                            int month = now.get(Calendar.MONTH) + 1;
                            date.append((month > 10)
                                        ? "-"
                                        : "-0");
                            date.append(month);

                            int day = now.get(Calendar.DAY_OF_MONTH);
                            date.append((day > 10)
                                        ? "-"
                                        : "-0");
                            date.append(day);


                            time.append(now.get(Calendar.HOUR_OF_DAY));
                            time.append((Calendar.MINUTE > 10)
                                        ? ":"
                                        : ":0");

                            time.append(now.get(Calendar.MINUTE));

                            //ArrayList<Integer> reservationArray = dbA.checkReservationTime(date.toString(), time.toString());

                            ArrayList<Integer> reservationIds = new ArrayList<>();
                            ArrayList<Reservation> reservations = dbA.getReservationList();

                            for (Reservation res : reservations)
                            if (DateUtils.isToday(res.getTime()) && DateUtils.isWithinMinutesFutureExactly(res.getTime(), 60))    // TODO personalize timer
                                { reservationIds.add(res.getReservation_id()); }


                            if (!reservationIds.isEmpty())
                                { throwTimerPopup(reservationIds); }
                            else
                                { Log.i(TAG, "no reservations for the next hour"); }

                            isRunning = true;
                        }

                        catch (Exception e)
                        {
                            Log.d(TAG, "Timer error: " + e.getMessage());
                            isRunning = false;
                        }
                    }
                });
            }
        };

        reservationTimer.schedule(task, 5 * 1000, dbA.getReservationPopupTimer() * 60 * 1000); //every 5 minutes, with five seconds delay
    }




    private void throwTimerPopup(ArrayList<Integer> array)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        final View popupView = layoutInflater.inflate(R.layout.popup_reservation_automatic, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);



        ReservationsAdapter popup_reservations_list = new ReservationsAdapter(dbA, context, operative, array);

        RecyclerView popup_rv = (RecyclerView) popupView.findViewById(R.id.popup_reservations_rv);
        popup_rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        popup_rv.setAdapter(popup_reservations_list);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) popupView.findViewById(R.id.automatic_popup_window).getLayoutParams();


        int t = (int) ((int) (dpHeight) / 2 - 130 * density);
        rlp.topMargin = t;
        rlp.setMargins((int) (270 * density), t, 0, 0);
        popupView.findViewById(R.id.automatic_popup_window).setLayoutParams(rlp);


        popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
            }
        });


        popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();
            }
        });


        // THROW POPUP WINDOW AFTER SETTING EVERYTHING UP
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Operative) context).findViewById(R.id.operative), 0, 0, 0);
    }
}
