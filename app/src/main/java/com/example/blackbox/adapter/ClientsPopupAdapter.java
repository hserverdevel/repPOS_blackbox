package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.ClientInfo;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

/**
 * Created by DavideLiberato on 17/07/2017.
 */

public class ClientsPopupAdapter extends RecyclerView.Adapter {

    private final float density;
    private DatabaseAdapter dbA;
    private Context context;
    private float dpHeight;
    private float dpWidth;
    private LayoutInflater inflater;
    private RecyclerView parent;
    private ArrayList<ClientInfo> clients;
    private ArrayList<ClientInfo> notInsertedClients = new ArrayList<>();
    public boolean searchMode = false;

    private int mode;
    private int billId;
    private boolean clientLongClick = false;

    public ClientsPopupAdapter(DatabaseAdapter dbA, Context context, int mode, int billId){
        this.dbA = dbA;
        this.context = context;
        this.mode = mode;
        clients = new ArrayList<>();
        //to be fixed to work only with -1
        this.billId = billId;
        this.billId = -1;
        dbA.showData("client");
        clients = dbA.fetchClients();

        inflater = ((Activity)context).getLayoutInflater();

        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;
    }

    public void reloadCLients(){
        clients = dbA.fetchClients();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.client_list_element, null);
        return new ClientHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GradientDrawable border = new GradientDrawable();
        ClientHolder clientHolder = (ClientHolder)holder;
        ClientInfo clientInfo = clients.get(position);
        if(clientInfo.isActive()){
            clientHolder.totalView.setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
        }else {
            if (position % 2 == 0) {
                border.setColor(0xEF151515);
                clientHolder.totalView.setBackground(border);
            } else {
                border.setColor(Color.TRANSPARENT);
                clientHolder.totalView.setBackground(border);
            }
        }
        if(clientInfo.getName().equals("") && clientInfo.getSurname().equals("")) {
            if (clientInfo.isHasCompany() && !clientInfo.getCompany_name().equals(""))
                clientHolder.name.setText(clientInfo.getCompany_name());
        }
        else{
            if(!clientInfo.getName().equals("")){
                if(clientInfo.getSurname().equals(""))
                    clientHolder.name.setText(clientInfo.getName());
                else clientHolder.name.setText(clientInfo.getName()+" "+ clientInfo.getSurname());
            }
            else clientHolder.name.setText(clientInfo.getSurname());
        }
        clientHolder.email.setText(clientInfo.getEmail());
        clientHolder.totalView.setLayoutParams(new RelativeLayout.LayoutParams((int) (489*density), (int) (56*density)));
        clientHolder.totalView.setTag(clientInfo);

        clientHolder.totalView.setOnTouchListener(new OnSwipeTouchListener(context){
            public void onSwipeLeft(){
                clientHolder.name.setVisibility(View.GONE);
                clientHolder.email.setVisibility(View.GONE);
                clientHolder.n_orders.setVisibility(View.GONE);
                clientHolder.delete_button.setVisibility(View.VISIBLE);

                clientHolder.delete_button.setOnTouchListener(new OnSwipeTouchListener(context){
                    public void onSwipeRight(){
                        if((clientHolder.delete_button.getVisibility()) == View.VISIBLE) {
                            clientHolder.name.setVisibility(View.VISIBLE);
                            clientHolder.email.setVisibility(View.VISIBLE);
                            clientHolder.n_orders.setVisibility(View.VISIBLE);
                            clientHolder.delete_button.setVisibility(View.GONE);
                        }
                    }
                    //a popup must appear
                    public void onClick() {
                        /*LayoutInflater layoutInflater = (LayoutInflater)context
                                .getSystemService(LAYOUT_INFLATER_SERVICE);*/

                    }
                });
            }

            public void onClick(){

                ((PaymentActivity) context).setSelectedClient(clientInfo);
                notifySetChanged(clientInfo);
                clientInfo.setActive(true);

            }
        });

        clientHolder.n_orders.setText(String.format("%02d", (position+1)));

    }



    public void notifySetChanged(ClientInfo clientInfo){
        for(ClientInfo client : clients){
            if(client!=clientInfo) client.setActive(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return clients!=null?clients.size():0;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parent = recyclerView;
    }

    public void setSearchMode(boolean b) {
        searchMode = b;
    }

    public void searchClients(String key){
        clients = dbA.searchClients(key);
        notifyDataSetChanged();
    }


    public static class ClientHolder extends RecyclerView.ViewHolder{
        public View totalView;
        public CustomTextView name;
        public CustomTextView email;
        public CustomTextView n_orders;
        public LinearLayout delete_button;
        public ClientHolder(View itemView){
            super(itemView);
            totalView = itemView;
            name = (CustomTextView)totalView.findViewById(R.id.client_name_tv);
            email = (CustomTextView)totalView.findViewById(R.id.client_email_tv);
            n_orders = (CustomTextView)totalView.findViewById(R.id.number_of_orders_tv);
            delete_button = (LinearLayout) totalView.findViewById(R.id.client_delete_button);
        }
    }




}
