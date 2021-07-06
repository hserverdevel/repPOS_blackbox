package com.example.blackbox.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.ClientsActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.model.FidelityPackage;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;


import static android.content.Context.LAYOUT_INFLATER_SERVICE;




public class FidelityPackageAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter
{
    private static final String TAG = "<FidelityCreditAdapter>";

    public Context context;
    public View popupview;

    private final LayoutInflater inflater;
    private  ArrayList<FidelityPackage> allFidelityPackages;

    private final ClientsActivity clientsActivity;

    private final DatabaseAdapter dbA;



    public FidelityPackageAdapter(Context c , DatabaseAdapter database, ClientsActivity cact)
    {
        context = c;

        dbA = database;

        allFidelityPackages = database.selectAllFidelityPackages();

        clientsActivity = cact;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = inflater.inflate(R.layout.recycler_fidelity_package, null);
        return new FidelityPackageAdapter.ButtonHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        FidelityPackage fidelityPackage = allFidelityPackages.get(position);
        ButtonHolder itemHolder = (ButtonHolder) holder;

        // Set the representation
        itemHolder.itemContainer.setText(String.format("%s: %d FC x %.2f EUR", fidelityPackage.getName(), fidelityPackage.getCreditAmount(), fidelityPackage.getPrice()));

        // on touch, move to payment activity
        itemHolder.itemContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // save a bill for the credits on the blackbox
                // the result will be handled by the ClientsActivity
                clientsActivity.saveFidelityPackageBill(fidelityPackage);
            }
        });



        itemHolder.itemContainer.setOnLongClickListener(new View.OnLongClickListener()
        {
           // on swipe left, show the delete button
           // on swipe right, show the normal button
            @Override
            public boolean onLongClick(View v)
            {
                if (itemHolder.deleteButton.getVisibility() == View.GONE)
                {
                    itemHolder.itemContainer.setVisibility(View.GONE);
                    itemHolder.deleteButton.setVisibility(View.VISIBLE);

                    return true;
                }

                return false;
            }
        });


        itemHolder.deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dbA.deleteFidelityPackageById(fidelityPackage.getButtonId());

                refreshFidelityPackageList();
            }
        });

        itemHolder.deleteButton.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (itemHolder.deleteButton.getVisibility() == View.VISIBLE)
                {
                    itemHolder.deleteButton.setVisibility(View.GONE);
                    itemHolder.itemContainer.setVisibility(View.VISIBLE);

                    return true;
                }

                return false;
            }
        });
    }


    /**
     * Re-set the list of fidelity packages from the internal database
     * Used to refrsh the visual elements of the list, after an insert
     * or delete of a fidelity package
     */
    public void refreshFidelityPackageList()
    {
        this.allFidelityPackages = dbA.selectAllFidelityPackages();

        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() { return this.allFidelityPackages.size(); }


    @Override
    public boolean onItemMove(int a , int b) { return false; }

    @Override
    public void onItemDismiss(int a) { }






    static class ButtonHolder extends RecyclerView.ViewHolder
    {
        public View view;
        public RelativeLayout container;
        public CustomTextView itemContainer;
        public CustomButton deleteButton;

        public ButtonHolder(View itemView)
        {
            super(itemView);
            view = itemView;

            container     = view.findViewById(R.id.fidelity_package_recycler_main);
            itemContainer = view.findViewById(R.id.fidelity_package_container);
            deleteButton  = view.findViewById(R.id.fidelity_package_delete);
        }

        @NonNull
        @Override
        public String toString()
            { return "TODO [[ FidelityCreditAdapter.ButtonHolder ]]"; }
    }


}
