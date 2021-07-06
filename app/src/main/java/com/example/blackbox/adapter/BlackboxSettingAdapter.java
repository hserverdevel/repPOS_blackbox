package com.example.blackbox.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.MainActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.model.BlackboxInfo;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Fabrizio on 02/03/2018.
 */

public class BlackboxSettingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter
{

    private final DatabaseAdapter dbA;
    private final LayoutInflater inflater;
    private final Context context;
    private ArrayList<BlackboxInfo> blackboxes;
    private final View popupview;

    public BlackboxInfo blackbox;


    public BlackboxSettingAdapter(Context c , DatabaseAdapter database, View popupview, PopupWindow popupWindow)
    {
        context = c;
        this.popupview = popupview;
        dbA = database;
        blackboxes = dbA.selectAllBlackbox();
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }


    public void setPrinters(){
        blackboxes.clear();
        blackboxes = dbA.selectAllBlackbox();
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder vh;
        v = inflater.inflate(R.layout.recycler_printer_setting, null);
        vh = new ButtonHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
            blackbox = blackboxes.get(position);

            ButtonHolder button = (ButtonHolder) holder;
            button.printerContainer.setText(String.format("%s (%s)", blackbox.getName(), blackbox.getAddress()));

            button.printerContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                    { ((MainActivity) context).setSelectedBlackbox(popupview, blackbox); }
            });


            button.printerContainer.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    if (button.deleteButton.getVisibility() == View.GONE)
                    {
                        button.printerContainer.setVisibility(View.GONE);
                        button.deleteButton.setVisibility(View.VISIBLE);

                        return true;
                    }

                    return false;
                }
            });


            button.deleteButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dbA.deleteBlackbox(blackbox);

                    notifyDataSetChanged();
                }
            });


            button.deleteButton.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    if (button.deleteButton.getVisibility() == View.VISIBLE)
                    {
                        button.printerContainer.setVisibility(View.VISIBLE);
                        button.deleteButton.setVisibility(View.GONE);

                        return true;
                    }

                    return false;
                }
            });
    }


    @Override
    public int getItemCount()
    {
        if (blackboxes == null || blackboxes.isEmpty())
            return 0;
        return blackboxes.size();
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return true;
    }


    @Override
    public void onItemDismiss(int position) { }


    /** HOLDER **/
    static class ButtonHolder extends RecyclerView.ViewHolder
    {
        public View view;
        public RelativeLayout container;
        public CustomTextView printerContainer;
        public CustomButton deleteButton;
        public CustomButton selectButton;

        public ButtonHolder(View itemView)
        {
            super(itemView);
            view = itemView;

            container  = (RelativeLayout) view.findViewById(R.id.printer_setting_main);
            printerContainer  = (CustomTextView) view.findViewById(R.id.printer_value_container);
            deleteButton = (CustomButton) view.findViewById(R.id.delete_printer_button);
            selectButton = (CustomButton) view.findViewById(R.id.select_printer_button);
        }

        @NonNull
        @Override
        public String toString()
            { return "ButtonHolder, Title: " + printerContainer.getText().toString();}
    }

}
