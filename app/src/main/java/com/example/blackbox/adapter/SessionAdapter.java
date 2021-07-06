package com.example.blackbox.adapter;

/**
 * Created by tiziano on 9/2/17.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.SessionModel;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SessionAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {

    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    ArrayList<SessionModel> sessionsList;
    private AdapterSessionCallback mAdapterCallback;
    private View pop;
    private PopupWindow popwindow;

    public SessionAdapter(Context c , DatabaseAdapter database){
        context = c;
        this.mAdapterCallback = ((AdapterSessionCallback) context);
        dbA = database;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        sessionsList = dbA.getSessionsTime();
    }

    public SessionAdapter(Context c , DatabaseAdapter database, View popupview, PopupWindow popupWindow){
        context = c;
        this.mAdapterCallback = ((AdapterSessionCallback) context);
        dbA = database;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        sessionsList = dbA.getSessionsTime();
        pop = popupview;
        popwindow = popupWindow;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.recycler_sessions_time, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SessionModel sessionModel = sessionsList.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        button.sessionNameContainer.setText(sessionModel.getSessionName());
        button.timeContainer.setText(sessionModel.getStartTime().substring(0, sessionModel.getStartTime().length() - 3)
                +"/"+sessionModel.getEndTime().substring(0, sessionModel.getEndTime().length() - 3));

        button.view.setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onSwipeRight() {


            }

            /**
             * show delete view
             */
            public void onSwipeLeft() {
                if(pop==null) {
                    mAdapterCallback.deleteSession(sessionModel.getId());
                }else{
                    mAdapterCallback.deleteSessionPopup(sessionModel.getId(), pop, popwindow);

                }

             }

            public void onClick() {

                if(pop==null) {
                    mAdapterCallback.setButtonSet(sessionModel.getId(), sessionModel.getSessionName(), sessionModel.getStartTime().substring(0, sessionModel.getStartTime().length() - 3), sessionModel.getEndTime().substring(0, sessionModel.getEndTime().length() - 3));
                }else{
                    mAdapterCallback.setButtonSetPopup(sessionModel.getId(), sessionModel.getSessionName(), sessionModel.getStartTime().substring(0, sessionModel.getStartTime().length() - 3), sessionModel.getEndTime().substring(0, sessionModel.getEndTime().length() - 3), pop, popwindow);
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
        return sessionsList.size();
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
        public CustomTextView sessionNameContainer;
        public CustomTextView timeContainer;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            linearLayout = (LinearLayout) view.findViewById(R.id.session_time_container);
            sessionNameContainer = (CustomTextView) view.findViewById(R.id.session_name_container);
            timeContainer= (CustomTextView) view.findViewById(R.id.time_container);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+sessionNameContainer.getText().toString();}
    }

    public interface AdapterSessionCallback {
        void setButtonSet(int sessionTimeId, String sessionName, String start, String end);

        void deleteSession(int sessionTimeId);

        void setButtonSetPopup(int sessionTimeId, String sessionName, String start, String end, View popupview, PopupWindow popupwindow);

        void deleteSessionPopup(int sessionTimeId, View popupview, PopupWindow popupwindow);

    }




}
