package com.example.blackbox.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.ClientsActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.DiscountModel;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Fabrizio on 11/05/2018.
 */

public class DiscountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    private View popView;
    private PopupWindow popWindow;
    private int mode = -1;
    private ArrayList<DiscountModel> discounts = new ArrayList<>();
    private ClientInfo selectedClient = new ClientInfo();
    private DiscountModel thisDiscount = new DiscountModel();
    public void setSelectedDiscount(String text){selectedDiscount = text;}
    public void setSelectedValue(int value){selectedValue = value;}
    private String selectedDiscount = "";
    private int selectedValue = -1;
    private int newDiscount = -1;
    private boolean modify = false;
    public boolean getModify(){return modify;}
    public void setModify(boolean value){modify = value;}

    private final static DiscountModel emptyDiscount = new DiscountModel();

    public DiscountAdapter(DatabaseAdapter dbA, Context context, View popupView, PopupWindow popupWindow, int value){
        this.dbA = dbA;
        this.context = context;
        this.popView = popupView;
        this.popWindow = popupWindow;
        this.mode = value;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        //dbA.showData("discount_mode");
       // dbA.showData("discount");
        discounts = dbA.fetchDiscountArray(mode);
    }

    public DiscountAdapter(DatabaseAdapter dbA, Context context, View popupView, PopupWindow popupWindow, int value,
                           ClientInfo myClient){
        this.dbA = dbA;
        this.context = context;
        this.popView = popupView;
        this.popWindow = popupWindow;
        this.mode = value;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        discounts = dbA.fetchDiscountArray(mode);
       // dbA.showData("discount_mode");
       // dbA.showData("discount");
        selectedClient = myClient;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder vh;
        v = inflater.inflate(R.layout.discount_value_recycler, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DiscountModel myDiscount = discounts.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        String name = myDiscount.getDescription();
        button.discountContainer.setText(getCapString(name));
        button.valueContainer.setText(myDiscount.getValue() + "%");

        button.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisDiscount = myDiscount;
                CustomEditText discountModifier = (CustomEditText)popView.findViewById(R.id.client_name_type_et);
                CustomEditText valueModifier = (CustomEditText)popView.findViewById(R.id.set_discount_et);
                discountModifier.setText(getCapString(myDiscount.getDescription()));
                valueModifier.setText(myDiscount.getValue() + "%");
                modify = true;
            }
        });

        button.view.setOnTouchListener(new OnSwipeTouchListener(context){
            public void onSwipeLeft(){
                if(button.deleteButton.getVisibility() == View.GONE){
                    button.deleteButton.setVisibility(View.VISIBLE);
                    button.discountContainer.setVisibility(View.GONE);
                    button.valueContainer.setVisibility(View.GONE);
                }

                button.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dbA.deleteDiscount(thisDiscount.getDescription());
                        discounts = dbA.fetchDiscountArray(mode);
                        button.deleteButton.setVisibility(View.GONE);
                        button.discountContainer.setVisibility(View.VISIBLE);
                        button.valueContainer.setVisibility(View.VISIBLE);
                        notifyDataSetChanged();

                        //deseleziono lo sconto appena cancellato
                        thisDiscount = emptyDiscount;
                    }
                });

                button.deleteButton.setOnTouchListener(new OnSwipeTouchListener(context){
                    public void onSwipeRight(){
                        if(button.deleteButton.getVisibility() == View.VISIBLE){
                            button.deleteButton.setVisibility(View.GONE);
                            button.discountContainer.setVisibility(View.VISIBLE);
                            button.valueContainer.setVisibility(View.VISIBLE);
                        }
                    }

                    public void onClick() {
                        dbA.deleteDiscount(thisDiscount.getDescription());
                        discounts = dbA.fetchDiscountArray(mode);
                        button.deleteButton.setVisibility(View.GONE);
                        button.discountContainer.setVisibility(View.VISIBLE);
                        button.valueContainer.setVisibility(View.VISIBLE);
                        notifyDataSetChanged();

                        //deseleziono lo sconto appena cancellato
                        thisDiscount = emptyDiscount;
                    }
                });
            }

            //to set or to add new discount
            public void onClick(){
                /*selectedDiscount = thisDiscount.getDescription();
                selectedValue = thisDiscount.getValue();*/
                /*selectedDiscount = button.discountContainer.getText().toString();
                selectedValue = Integer.parseInt(button.valueContainer.getText().toString().replace("%", ""));*/
                thisDiscount = myDiscount;
                selectedDiscount = thisDiscount.getDescription();
                selectedValue = thisDiscount.getValue();
                CustomEditText discountModifier = (CustomEditText)popView.findViewById(R.id.client_name_type_et);
                CustomEditText valueModifier = (CustomEditText)popView.findViewById(R.id.set_discount_et);
                discountModifier.setText(getCapString(selectedDiscount));
                valueModifier.setText(selectedValue + "");
                /*discountModifier.setText(getCapString(selectedDiscount));
                valueModifier.setText(selectedValue + "");*/
                modify = true;
            }
        });

        ImageButton btnOk = (ImageButton)popView.findViewById(R.id.ok_button);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okBehaviour();
            }
        });

        ImageButton kill = (ImageButton)popView.findViewById(R.id.kill);
        kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = ((CustomEditText)popView.findViewById(R.id.client_name_type_et)).getText().toString();
                String value = ((CustomEditText)popView.findViewById(R.id.set_discount_et)).getText().toString();
                if(name.equals("") || value.equals(""))
                    popWindow.dismiss();
                else{
                    ((CustomEditText)popView.findViewById(R.id.client_name_type_et)).setText("");
                    ((CustomEditText)popView.findViewById(R.id.set_discount_et)).setText("");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(discounts == null || discounts.isEmpty())
            return 0;
        else
            return discounts.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    /** HOLDER **/
    class ButtonHolder extends RecyclerView.ViewHolder {
        public View view;
        public LinearLayout linearLayout;
        public CustomTextView discountContainer;
        public CustomTextView valueContainer;
        public CustomButton deleteButton;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            linearLayout = (LinearLayout) view.findViewById(R.layout.discount_value_recycler);
            discountContainer = (CustomTextView) view.findViewById(R.id.discount_name_container);
            valueContainer = (CustomTextView)view.findViewById(R.id.discount_value_container);
            deleteButton = (CustomButton) view.findViewById(R.id.custom_delete_discount_button);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: " + discountContainer.getText().toString();}
    }

    public void okBehaviour(){
        CustomEditText discountModifier = (CustomEditText)popView.findViewById(R.id.client_name_type_et);
        CustomEditText valueModifier = (CustomEditText)popView.findViewById(R.id.set_discount_et);
        String discountText = String.valueOf(discountModifier.getText().toString());
        String realText = discountText.toLowerCase();
        if(modify){
            //modify existing discount's name
            if(!thisDiscount.getDescription().equals(realText)){
                dbA.modifyExistingDiscountDescription(selectedDiscount, realText);
                discounts = dbA.fetchDiscountArray(mode);
                notifyDataSetChanged();
                modify = false;
                discountModifier.setText("");
                valueModifier.setText("");

                if(mode == 0){
                    if(((ClientsActivity) context).getDiscountButton().isActivated()){
                        int value = dbA.getDiscountValue(realText);
                        ((ClientsActivity) context).getDiscountButton().setText(value + "%");
                    }
                }
                else if(mode == 1){
                    if(((ClientsActivity) context).getClientButton().isActivated()) {
                        ((ClientsActivity) context).getClientButton().setText(realText);
                    }
                }
            }
            else if(dbA.checkIfDiscountExists(realText)){
                int oldValue = dbA.getDiscountValue(realText);
                String textValue = String.valueOf(valueModifier.getText().toString());
                if (textValue.matches("[0-9]*\\.[0-9]*")) {
                    float floatValue = Float.parseFloat(textValue);
                    floatValue = floatValue * 100;
                    newDiscount = (int) floatValue;
                }
                else
                    newDiscount = Integer.parseInt(textValue);
                if(oldValue != -1){
                    if(oldValue == newDiscount){
                        //if client is selected
                        if(selectedClient.getClient_id() != 0){
                            //if client already has this discount set, do nothing
                            if(!dbA.checkIfClientHasASingleDiscount(selectedClient.getClient_id(), realText))
                                dbA.assignDiscountToClient(selectedClient.getClient_id(), realText);
                            else
                                modify = false;
                        }
                        if(mode == 0) {
                            ((ClientsActivity) context).getDiscountButton().setActivated(true);
                            int value = dbA.getDiscountValue(realText);
                            ((ClientsActivity) context).getDiscountButton().setText(value + "%");
                        }
                        else if(mode == 1) {
                            ((ClientsActivity) context).getClientButton().setActivated(true);
                            ((ClientsActivity) context).getClientButton().setText(realText);
                        }
                        popWindow.dismiss();
                    }
                    else{
                        dbA.modifyExistingDiscountValue(realText, newDiscount);
                        discounts = dbA.fetchDiscountArray(mode);
                        notifyDataSetChanged();
                        discountModifier.setText("");
                        valueModifier.setText("");

                    }
                }
                else{
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if(!realText.equals("")) {
            if(!dbA.checkIfDiscountExists(realText)){
                if (!realText.matches("^[a-z0-9 ]+$"))
                    Toast.makeText(context, R.string.discount_name_not_accepted, Toast.LENGTH_SHORT).show();
                else {
                    String textValue = String.valueOf(valueModifier.getText().toString());
                    if (textValue.matches("[0-9]*\\.[0-9]*")) {
                        float floatValue = Float.parseFloat(textValue);
                        floatValue = floatValue * 100;
                        newDiscount = (int) floatValue;
                    } else
                        newDiscount = Integer.parseInt(textValue);
                    if (newDiscount != 0) {
                        dbA.addDiscountMode(realText, newDiscount, mode);
                        discounts = dbA.fetchDiscountArray(mode);
                        notifyDataSetChanged();
                        discountModifier.setText("");
                        valueModifier.setText("");

                    } else if (newDiscount == 0) {
                        Toast.makeText(context, R.string.discount_value_not_valid, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else
            popWindow.dismiss();

    }

    //method to capitalize each word
    public String getCapString(String name){
        String[] splits = name.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<splits.length; i++){
            String word = splits[i];
            if(i>0 && word.length()>0){
                sb.append(" ");
            }
            if(word.length()>0) {
                String cap = word.substring(0, 1).toUpperCase() + word.substring(1);
                sb.append(cap);
            }
        }

        return sb.toString();
    }

}
