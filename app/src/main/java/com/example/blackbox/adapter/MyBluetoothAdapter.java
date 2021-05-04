package com.example.blackbox.adapter;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.MyBluetoothDevice;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by tiziano on 3/8/19.
 */

public class MyBluetoothAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {
    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    ArrayList<MyBluetoothDevice> devices;
    private View popupview;
    private PopupWindow popupWindow;
    BluetoothSocket socket;
    private Thread bluetoothThread;

    public MyBluetoothAdapter(Context c , DatabaseAdapter database, View popupview, PopupWindow popupWindow, ArrayList<MyBluetoothDevice> d){
        context = c;
        this.popupview = popupview;
        this.popupWindow = popupWindow;
        dbA = database;
        devices = d;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

    }

    public void setListOfDevice(ArrayList<MyBluetoothDevice> myList){
        //if(devices!=null)devices.clear();
        devices = myList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder vh;
        v = inflater.inflate(R.layout.printer_setting_recycler, null);
        vh = new ButtonHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyBluetoothDevice dev = devices.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        if(dev.getDeviceName()==null)
            button.printerContainer.setText("Unknown Device");
        else
            button.printerContainer.setText(dev.getDeviceName());

        button.view.setOnTouchListener(new OnSwipeTouchListener(context) {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onClick(){

            }
        } );

    }

    @Override
    public int getItemCount() {
        if(devices == null || devices.isEmpty())
            return 0;
        return devices.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    /** HOLDER **/
    class ButtonHolder extends RecyclerView.ViewHolder {
        public View view;
        public RelativeLayout container;
        public CustomTextView printerContainer;
        public CustomButton deleteButton;
        public CustomButton selectButton;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;

            container  = (RelativeLayout) view.findViewById(R.id.printer_setting_main);
            printerContainer  = (CustomTextView) view.findViewById(R.id.printer_value_container);
            deleteButton = (CustomButton) view.findViewById(R.id.delete_printer_button);
            selectButton = (CustomButton) view.findViewById(R.id.select_printer_button);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: " + printerContainer.getText().toString();}
    }




}
