package com.example.blackbox.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Fidelity;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


/**
 * This adapter handle the client list when paying with Fidelity credits
 * When the option to pay with fidelity is selected in the payment activity (under the payment options adapter)
 * a popup with the list of clients appears. This adapter handle that list
 */
public class ClientPayFidelityAdapter extends RecyclerView.Adapter {


    private Context context;

    private ArrayList<ClientInfo> allClients;

    private DatabaseAdapter dbA;

    private final LayoutInflater inflater;

    private CustomTextView prevView;

    public ClientInfo selectedClient;
    public Fidelity fidelity;




    public ClientPayFidelityAdapter(Context c)
    {
        context = c;

        dbA = new DatabaseAdapter(context);

        allClients = dbA.fetchClients();

        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = inflater.inflate(R.layout.recycler_client_pay_fidelity, null);
        return new ClientPayFidelityAdapter.ButtonHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        ClientInfo client = allClients.get(position);
        ButtonHolder itemHolder = (ButtonHolder) holder;

        // Set the representation
        fidelity = dbA.fetchFidelityById(client.getFidelity_id());

        itemHolder.itemContainer.setText(String.format("%s %s | Fidelity ID: %s, Credits: %s", client.getName(), client.getSurname(), client.getFidelity_id(), fidelity.getValue()));

        // on touch, make a clear selection on the client
        // when in the popup the OK button is pressed, the value of the selected client
        // will be read by the calculator fragment
        itemHolder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // when a client is selected, highlight it in green
                // when a different one is selected, highlight the new one and deselect the old one
                if (selectedClient != client)
                {
                    if (prevView != null)
                    {
                        prevView.setBackgroundColor(Color.WHITE);
                        prevView.setTextColor(Color.BLACK);
                    }

                    selectedClient = client;
                    fidelity = dbA.fetchFidelityById(client.getFidelity_id());

                    itemHolder.itemContainer.setBackgroundColor(Color.GREEN);
                    itemHolder.itemContainer.setTextColor(Color.WHITE);
                    prevView = itemHolder.itemContainer;
                }
            }
        });

    }


    @Override
    public int getItemCount() { return this.allClients.size(); }




    static class ButtonHolder extends RecyclerView.ViewHolder
    {
        public View view;
        public RelativeLayout container;
        public CustomTextView itemContainer;

        public ButtonHolder(View itemView)
        {
            super(itemView);
            view = itemView;

            container     = view.findViewById(R.id.client_pay_fidelity_recycler_main);
            itemContainer = view.findViewById(R.id.client_pay_fidelity_container);
        }

        @NonNull
        @Override
        public String toString()
            { return "TODO [[ ButtonHolder ]]"; }
    }





}
