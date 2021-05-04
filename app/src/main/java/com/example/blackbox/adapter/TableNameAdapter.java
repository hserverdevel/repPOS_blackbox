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
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.TableActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.Table;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TableNameAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {

    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    ArrayList<Table> tables;
    private TakeAwayAdapter.AdapterCallback mAdapterCallback;
    private boolean modify;
    private View popupView;


    public TableNameAdapter(Context c , DatabaseAdapter database, boolean modify, View popupView){
        context = c;
        dbA = database;
      //  this.mAdapterCallback = ((TakeAwayAdapter.AdapterCallback) context);
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        //View view = inflater.inflate(R.layout.take_away_recycler, null);
        tables = dbA.fetchTablesName();
        this.modify = modify;
        this.popupView = popupView;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.table_name_recycler, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Table table= tables.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        button.container1.setText(table.getTableName());
        //room id is used to count occurence of name, check db method
        button.container2.setText(String.valueOf(table.getRoomId()));


        button.relativeLayout.setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onClick() {
                ((TableActivity)context).setTableInfoInPopup(table, popupView);
            }

            public void onSwipeLeft(){
                button.container3.setVisibility(View.VISIBLE);
            }
            public void onSwipeRight(){
                button.container3.setVisibility(View.GONE);
            }

        });

        /*button.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TableActivity)context).setTableInfoInPopup(table, popupView);
            }
        });*/

    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public int getItemCount() {
        if(tables!=null) return tables.size();
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
        public RelativeLayout relativeLayout;
        public CustomTextView container1;
        public CustomTextView container2;
        public CustomButton container3;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            relativeLayout = (RelativeLayout) view.findViewById(R.id.tab_container);
            container1 = (CustomTextView) view.findViewById(R.id.tab_n_container);
            container2 = (CustomTextView) view.findViewById(R.id.tab_spec_container);
            container3 = (CustomButton) view.findViewById(R.id.tab_n_delete_container) ;

        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+container1 .getText().toString();}
    }



}
