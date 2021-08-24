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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.ClientsActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.StaticValue;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.example.blackbox.activities.ClientsActivity.MODIFY_MODE;

/**
 * Created by DavideLiberato on 17/07/2017.
 */

public class ClientsAdapter extends RecyclerView.Adapter
{

    private final float                 density;
    public  boolean searchMode      = false;
    private       DatabaseAdapter       dbA;
    private       Context               context;
    private       float                 dpHeight;
    private       float                 dpWidth;
    private       ClientsActivity       clientsActivity;
    private       LayoutInflater        inflater;
    private       RecyclerView          parent;
    private       ArrayList<ClientInfo> clients;
    private       ArrayList<ClientInfo> notInsertedClients = new ArrayList<>();
    private boolean isActive        = true;
    private int     previous_parent; // which means the parent of the current buttons' parent
    private int     mode;
    private int     billId;
    private boolean clientLongClick = false;
    private ClientHolder myClientHolder;
    private PopupWindow  myPopupWindow;
    public ClientsAdapter(DatabaseAdapter dbA, Context context, int mode, int billId, boolean longClick)
    {
        this.dbA     = dbA;
        this.context = context;
        this.mode    = mode;

        clientsActivity = (ClientsActivity) context;
        clients         = new ArrayList<>();
        clientLongClick = longClick;
        //to be fixed to work only with -1
        this.billId = billId;

        if ((billId == -1 || billId == -11) || clientLongClick)
        {

            if (StaticValue.blackbox)
            {
                RequestParam params = new RequestParam();
                params.add("clientChecksum", dbA.getChecksumForTable("client"));

                clientsActivity.callHttpHandler("/fetchClients", params);
            }

            else
            {
                clients            = dbA.fetchClients();
                clientLongClick    = false;
                notInsertedClients = dbA.fetchExclusiveClients(billId);
            }

            clients            = dbA.fetchClients();
            clientLongClick    = false;
            notInsertedClients = dbA.fetchExclusiveClients(billId);

        }

        else
        {
            if (StaticValue.blackbox)
            {
                RequestParam params = new RequestParam();
                params.add("billId", String.valueOf(billId));
                params.add("clientChecksum", dbA.getChecksumForTable("client"));

                clientsActivity.callHttpHandler("/fetchExclusiveClients", params);
            }

            else
            {
                clients = dbA.fetchExclusiveClients(billId);
            }

            //clients = dbA.fetchExclusiveClients(billId);

        }
        inflater = ((Activity) context).getLayoutInflater();

        Display        display    = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;
    }

    public int getClientsSize()
    {
        if (clients == null)
        {
            return 0;
        }
        else
        {
            return clients.size();
        }
    }

    public void updateClientsList(ArrayList<ClientInfo> clients)
    {
        this.clients = clients;
        notifyDataSetChanged();
    }


    public void fetchClients()
    {
        if (billId == -1 || billId == -11)
        {
            clients = dbA.fetchClients();
        }
        else
        {
            clients = dbA.fetchExclusiveClients(billId);
        }
    }

    /**
     * A simple function to set the clients list to the
     * list present in the internal database.
     * This function is used in conjunction with the checksum check
     * in the Clients activity
     */
    public void setExclusiveClientsDefault()
    {
        clients = dbA.fetchExclusiveClients(billId);
    }

    public void setClientsDefault()
    {
        clients = dbA.fetchClients();
    }


    public void fetchClientsLast()
    {
        if (billId == -1 || billId == -11)
        {
            clients = dbA.fetchClients();
        }
        else
        {
            clients = dbA.fetchExclusiveClients(billId);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = inflater.inflate(R.layout.element_client_list, null);
        return new ClientHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        GradientDrawable border       = new GradientDrawable();
        ClientHolder     clientHolder = (ClientHolder) holder;
        ClientInfo       clientInfo   = clients.get(position);

        // set the color of the client element in the list
        // if this client has been selected, color it green
        if (clientInfo.isActive())
        {
            clientHolder.totalView.setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
        }

        // otherwise color it in black or gray, based on it's even or odd position
        else
        {
            if (position % 2 == 0)
            {
                border.setColor(0xEF151515);
                clientHolder.totalView.setBackground(border);
            }
            else
            {
                border.setColor(Color.TRANSPARENT);
                clientHolder.totalView.setBackground(border);
            }
        }


        if (clientInfo.getName().equals("") && clientInfo.getSurname().equals(""))
        {
            if (clientInfo.isHasCompany() && !clientInfo.getCompany_name().equals(""))
            {
                clientHolder.name.setText(clientInfo.getCompany_name());
            }
        }
        else
        {
            if (!clientInfo.getName().equals(""))
            {
                if (clientInfo.getSurname().equals(""))
                {
                    clientHolder.name.setText(clientInfo.getName());
                }
                else
                {
                    clientHolder.name.setText(clientInfo.getName() + " " + clientInfo.getSurname());
                }
            }
            else
            {
                clientHolder.name.setText(clientInfo.getSurname());
            }
        }
        clientHolder.email.setText(clientInfo.getEmail());
        clientHolder.totalView.setLayoutParams(new RelativeLayout.LayoutParams((int) (489 * density), (int) (56 * density)));
        clientHolder.totalView.setTag(clientInfo);

        clientHolder.totalView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            //you'll never use this
            public void onClick(View v)
            {
                //there's a selected client, you can only select that client
                if (clientsActivity.getCurrentClientId() != -1)
                {
                    clientHolder.totalView.setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));

                    if (((ClientInfo) v.getTag()).getClient_id() == clientsActivity.getCurrentClientId())
                    {
                        clientsActivity.setCurrentClient((ClientInfo) v.getTag());
                        clientsActivity.openModifyMode((ClientInfo) v.getTag());
                        /*if(mode != 2 && mode!=3 && mode != 4 && mode != 5)
                        clientsActivity.setMode(MODIFY_RESERVATION_MODE);*/
                        if (mode == 0 || mode == 1)
                        {
                            clientsActivity.setMode(MODIFY_MODE);
                        }
                    }
                    else
                    {
                        Toast.makeText(context, R.string.sorry_you_cant_modify_another_selected_client, Toast.LENGTH_SHORT).show();
                    }
                }
                //no client selected, you can select one
                else
                {
                    clientsActivity.setCurrentClient((ClientInfo) v.getTag());
                    clientsActivity.openModifyMode((ClientInfo) v.getTag());
                        /*if(mode != 2 && mode!=3 && mode != 4 && mode != 5)
                        clientsActivity.setMode(MODIFY_RESERVATION_MODE);*/
                    if (mode == 0 || mode == 1)
                    {
                        clientsActivity.setMode(MODIFY_MODE);
                    }
                }
            }
        });

        clientHolder.totalView.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            public void onSwipeLeft()
            {


                clientHolder.name.setVisibility(View.GONE);
                clientHolder.email.setVisibility(View.GONE);
                clientHolder.n_orders.setVisibility(View.GONE);
                clientHolder.delete_button.setVisibility(View.VISIBLE);

                clientHolder.delete_button.setOnTouchListener(new OnSwipeTouchListener(context)
                {
                    public void onSwipeRight()
                    {


                        if ((clientHolder.delete_button.getVisibility()) == View.VISIBLE)
                        {
                            clientHolder.name.setVisibility(View.VISIBLE);
                            clientHolder.email.setVisibility(View.VISIBLE);
                            clientHolder.n_orders.setVisibility(View.VISIBLE);
                            clientHolder.delete_button.setVisibility(View.GONE);
                        }
                    }

                    //a popup must appear
                    public void onClick()
                    {


                        throwPopupWindow(clientHolder, clientInfo);
                    }
                });
            }

            public void onClick()
            {


                clientInfo.setActive(true);
                if (clientsActivity.getCurrentClientId() != -1)
                {
                    //if client is not in notInsertedClients
                    if ((notInsertedClients.size() != 0 && notInsertedClients.contains(clientInfo))
                            //if client is selectedClient
                            || ((ClientInfo) clientHolder.totalView.getTag()).getClient_id() == clientsActivity.getCurrentClientId()
                    )
                    {
                        clientsActivity.setCurrentClient((ClientInfo) clientHolder.totalView.getTag());
                        clientsActivity.openModifyMode((ClientInfo) clientHolder.totalView.getTag());
                        if (mode == 0 || mode == 1)
                        {
                            clientsActivity.setMode(MODIFY_MODE);
                        }
                    }
                    else
                    {
                        Toast.makeText(context, R.string.sorry_you_cant_modify_another_selected_client, Toast.LENGTH_SHORT).show();
                    }
                }
                //no client selected, you can select one
                else
                {
                    clientsActivity.setCurrentClient((ClientInfo) clientHolder.totalView.getTag());
                    clientsActivity.openModifyMode((ClientInfo) clientHolder.totalView.getTag());
                        /*if(mode != 2 && mode!=3 && mode != 4 && mode != 5)
                        clientsActivity.setMode(MODIFY_RESERVATION_MODE);*/
                    if ((mode == 0 || mode == 1) && clientsActivity.getMode() != 6)
                    {
                        clientsActivity.setMode(MODIFY_MODE);
                    }
                }
                notifySetChanged(clientInfo);
            }
        });

        clientHolder.n_orders.setText(String.format("%02d", (position + 1)));

    }

    public void notifySetChanged(ClientInfo clientInfo)
    {
        if (clients != null)
        {
            for (ClientInfo client : clients)
            {
                if (client != clientInfo)
                {
                    client.setActive(false);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position)
    {
        return 0;
    }

    @Override
    public int getItemCount()
    {
        return clients != null ? clients.size() : 0;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        parent = recyclerView;
    }

    public void updateDataSet()
    {
        if ((billId == -1 || billId == -11) || clientLongClick)
        {
            dbA.showData("client");
            clients            = dbA.fetchClients();
            clientLongClick    = false;
            notInsertedClients = dbA.fetchExclusiveClients(billId);
        }
        else
        {
            clients = dbA.fetchExclusiveClients(billId);
        }
        if (clients.size() != 0)
        {
            ((Activity) context).findViewById(R.id.hline_rv_top).setVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
        ((CustomEditText) clientsActivity.findViewById(R.id.name_et)).setText("");
        ((CustomEditText) clientsActivity.findViewById(R.id.surname_et)).setText("");
        ((CustomEditText) clientsActivity.findViewById(R.id.email_et)).setText("");
        if (((CustomButton) clientsActivity.findViewById(R.id.discount_button)).isActivated())
        {
            ((CustomButton) clientsActivity.findViewById(R.id.discount_button)).setActivated(false);
            ((CustomButton) clientsActivity.findViewById(R.id.discount_button)).setText(R.string.set_discount);
        }
        if (((CustomButton) clientsActivity.findViewById(R.id.client_type_button)).isActivated())
        {
            ((CustomButton) clientsActivity.findViewById(R.id.client_type_button)).setActivated(false);
            ((CustomButton) clientsActivity.findViewById(R.id.client_type_button)).setText(R.string.set_client_type);
        }
    }

    public void setSearchMode(boolean bool)
    {
        searchMode = bool;
        if (bool)
        {
            clients.clear();
            notifyDataSetChanged();
        }
        else if (clientsActivity.getMode() == 6)
        {

        }
        else
        {
            updateDataSet();
        }
    }

    public void searchClients(String key)
    {
        clients = dbA.searchClients(key);
        notifyDataSetChanged();
    }

    public void deleteClientFromServer()
    {
        myClientHolder.delete_button.setVisibility(View.GONE);
        myClientHolder.totalView.setVisibility(View.VISIBLE);
        myClientHolder.name.setVisibility(View.VISIBLE);
        myClientHolder.email.setVisibility(View.VISIBLE);
        myClientHolder.n_orders.setVisibility(View.VISIBLE);
        updateDataSet();
        if (clients.size() == 0)
        {
            ((Activity) context).findViewById(R.id.hline_rv_top).setVisibility(View.GONE);
        }
        else
        {
            ((Activity) context).findViewById(R.id.hline_rv_top).setVisibility(View.VISIBLE);
        }
        clientsActivity.setMode(ClientsActivity.SELECTION_MODE);
        clientsActivity.closeModifyMode();
        myPopupWindow.dismiss();
    }

    public void throwPopupWindow(ClientHolder clientHolder, ClientInfo clientInfo)
    {
        final View popupView = inflater.inflate(R.layout.popup_yes_no, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        ((CustomTextView) popupView.findViewById(R.id.delete_window))
                .setText(R.string.delete_this_client);
        ((CustomButton) popupView.findViewById(R.id.delete_button))
                .setText(R.string.yes);
        ((CustomButton) popupView.findViewById(R.id.cancel_button))
                .setText(R.string.no);
        popupView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                if (StaticValue.blackbox)
                {
                    RequestParam params = new RequestParam();
                    params.add("clientId", String.valueOf(clientInfo.getClient_id()));
                    params.add("companyId", String.valueOf(clientInfo.getCompany_id()));


                    ((ClientsActivity) context).callHttpHandler("/deleteClient", params);
                    myClientHolder = clientHolder;
                    myPopupWindow  = popupWindow;
                }
                else
                {
                    dbA.deleteClient(clientInfo.getClient_id(), clientInfo.getCompany_id());
                    clientHolder.delete_button.setVisibility(View.GONE);
                    clientHolder.totalView.setVisibility(View.VISIBLE);
                    clientHolder.name.setVisibility(View.VISIBLE);
                    clientHolder.email.setVisibility(View.VISIBLE);
                    clientHolder.n_orders.setVisibility(View.VISIBLE);
                    updateDataSet();
                    if (clients.size() == 0)
                    {
                        ((Activity) context).findViewById(R.id.hline_rv_top).setVisibility(View.GONE);
                    }
                    else
                    {
                        ((Activity) context).findViewById(R.id.hline_rv_top).setVisibility(View.VISIBLE);
                    }
                    clientsActivity.setMode(ClientsActivity.SELECTION_MODE);
                    clientsActivity.closeModifyMode();
                    popupWindow.dismiss();
                }
            }
        });
        popupView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                popupWindow.dismiss();
                clientHolder.delete_button.setVisibility(View.GONE);
                clientHolder.totalView.setVisibility(View.VISIBLE);
                clientHolder.name.setVisibility(View.VISIBLE);
                clientHolder.email.setVisibility(View.VISIBLE);
                clientHolder.n_orders.setVisibility(View.VISIBLE);
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((ClientsActivity) context).findViewById(R.id.main), 0, 0, 0);
    }

    public static class ClientHolder extends RecyclerView.ViewHolder
    {
        public View           totalView;
        public CustomTextView name;
        public CustomTextView email;
        public CustomTextView n_orders;
        public LinearLayout   delete_button;

        public ClientHolder(View itemView)
        {
            super(itemView);
            totalView     = itemView;
            name          = (CustomTextView) totalView.findViewById(R.id.client_name_tv);
            email         = (CustomTextView) totalView.findViewById(R.id.client_email_tv);
            n_orders      = (CustomTextView) totalView.findViewById(R.id.number_of_orders_tv);
            delete_button = (LinearLayout) totalView.findViewById(R.id.client_delete_button);
        }
    }

}
