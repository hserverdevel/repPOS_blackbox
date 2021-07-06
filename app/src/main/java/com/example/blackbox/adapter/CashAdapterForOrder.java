package com.example.blackbox.adapter;

/**
 * Created by tiziano on 23/06/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.fragments.ActivityCommunicator;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.utils.db.DatabaseAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CashAdapterForOrder extends BaseExpandableListAdapter {
    private Activity context;
    private List<CashButtonLayout> cashProductList;
    private Map<CashButtonLayout, ArrayList<CashButtonListLayout>> cashModifierList;
    private ArrayList<Customer> cashCustomerList;
    public  View v;
    private  DatabaseAdapter dbA;
    private boolean showFirstClient = false;
    private boolean paid;
    private boolean clientLongClick = false;

    private ActivityCommunicator activityCommunicator;

    public CashAdapterForOrder(Activity c,
                               List<CashButtonLayout> firstList,
                               Map<CashButtonLayout , ArrayList<CashButtonListLayout>> secondList,
                               DatabaseAdapter dbA,
                               int bill_id,
                               ArrayList<Customer> customers,
                               boolean paid) {


        this.context = c;
        this.cashModifierList = secondList;
        this.cashProductList = firstList;
        this.cashCustomerList = customers;
        this.paid = paid;
    }

    public void setFirstClient(){
        showFirstClient = true;
    }

    public void setCustomerList(ArrayList<Customer> c){
        cashCustomerList = c;
    }

    @Override
    public int getGroupCount() {
        return cashProductList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(groupPosition< cashProductList.size()) {
            CashButtonLayout prova = cashProductList.get(groupPosition);
            List<CashButtonListLayout> prova2 = cashModifierList.get(prova);
            if (prova2 != null) {
                return cashModifierList.get(prova).size();
            }else {
                return 0;
            }
        }else return 0;

    }

    public List<CashButtonLayout> getGroups() {
        return cashProductList;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return cashProductList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return cashModifierList.get(cashProductList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public int isFirstVisible(){
        int returnI  =-1;
        for(int i = 0; i<cashProductList.size(); i++){
            if(!cashProductList.get(i).getIsPaid()){
                returnI = i;
                break;
            }
        }
        return returnI  ;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        CashButtonLayout product = (CashButtonLayout) getGroup(groupPosition);

            //modify part layout

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.element_gridview_cash, null);
            }
        if(!product.getIsPaid()){


            /**
             * common part for all layout, set title price and quantity and hiode group
             * divider line if in first position
             */
            CustomTextView title = (CustomTextView) convertView.findViewById(R.id.cash_title);
            CustomTextView titleModify = (CustomTextView) convertView.findViewById(R.id.cash_title_modify);
            CustomTextView quantity = (CustomTextView) convertView.findViewById(R.id.cash_quantity);
            CustomTextView price = (CustomTextView) convertView.findViewById(R.id.cash_total);

            title.setText(product.getTitle());
            titleModify.setText(product.getTitle());
            quantity.setText(product.getQuantity());
            String total = product.getTotal(product.getQuantityInt());
            DecimalFormat twoDForm = new DecimalFormat("#,00");

            price.setText(total.replace(".", ","));

            View groupDivider = convertView.findViewById(R.id.group_divider_hline);
            if (groupDivider != null) {
                if (groupPosition != 0) {


                    if(groupPosition==isFirstVisible()){
                            groupDivider.setVisibility(View.GONE);
                    }else {

                            groupDivider.setVisibility(View.VISIBLE);

                    }

                } else groupDivider.setVisibility(GONE);

            }






            LinearLayout productRow = (LinearLayout) convertView.findViewById(R.id.cash_group_row);
            productRow.setVisibility(VISIBLE);
            LinearLayout customer = (LinearLayout) convertView.findViewById(R.id.cash_customer_row);
            //productRow.setVisibility(GONE);
                int activeColor= Color.parseColor("#5cae32");
                int notActiveColor = Color.parseColor("#05a8c0");

                if(showFirstClient && groupPosition==0){

                    if(product!=null){
                        if(product.getProductId()!=-20){
                            productRow.setVisibility(VISIBLE);
                            customer.setVisibility(VISIBLE);


                        }else{
                            productRow.setVisibility(GONE);
                            customer.setVisibility(VISIBLE);


                        }
                        if(cashCustomerList.size()>0) {
                            CustomTextView customerText = (CustomTextView) convertView.findViewById(R.id.cash_user_text);
                            Customer cost = cashCustomerList.get(product.getClientPosition()-1);
                            if(cost!=null){
                                if(cost.getActive()){
                                    customer.setBackgroundColor(activeColor);
                                }else{
                                    customer.setBackgroundColor(notActiveColor);
                                }
                                customerText.setText(cost.getDescription());
                            }
                        }
                    }
                }else{
                    if(groupPosition>0){
                        CashButtonLayout productBefore = (CashButtonLayout) getGroup(groupPosition-1);
                        if(productBefore.getClientPosition()==product.getClientPosition()){
                            if(product.getProductId()!=-20) {
                                productRow.setVisibility(VISIBLE);
                            }else{
                                productRow.setVisibility(GONE);
                            }
                            customer.setVisibility(GONE);
                        }else{
                            if(product.getProductId()!=-20) {
                                productRow.setVisibility(VISIBLE);
                            }else{
                                productRow.setVisibility(GONE);
                            }
                            customer.setVisibility(VISIBLE);

                            if(cashCustomerList.size()>0) {
                                CustomTextView customerText = (CustomTextView) convertView.findViewById(R.id.cash_user_text);
                                Customer cost = cashCustomerList.get(product.getClientPosition()-1);
                                if(cost!=null){
                                    if(cost.getActive()){
                                        customer.setBackgroundColor(activeColor);
                                    }else{
                                        customer.setBackgroundColor(notActiveColor);
                                    }
                                    customerText.setText(cost.getDescription());
                                }
                            }
                        }
                    }else{
                        productRow.setVisibility(VISIBLE);
                        customer.setVisibility(GONE);
                    }
                }
                /**
                 * touch listener helper for all layout
                 */









              /*convertView.findViewById(R.id.cash_group_row).setVisibility(VISIBLE);
                convertView.setVisibility(VISIBLE);*/
            }else{
                convertView.findViewById(R.id.group_divider_hline).setVisibility(GONE);
                convertView.findViewById(R.id.cash_group_row).setVisibility(GONE);
                convertView.setVisibility(GONE);
            }


        return convertView;
    }

    /**
     * child layout display
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        CashButtonLayout product = (CashButtonLayout) getGroup(groupPosition);

            final CashButtonListLayout modifier = (CashButtonListLayout) getChild(groupPosition, childPosition);
            LayoutInflater inflater = context.getLayoutInflater();

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.element_gridview_cash_modifier_modify, null);
            }
            CustomTextView title = (CustomTextView) convertView.findViewById(R.id.cash_modifier_title);
            CustomTextView quantity = (CustomTextView) convertView.findViewById(R.id.cash_modifier_quantity);
            CustomTextView price = (CustomTextView) convertView.findViewById(R.id.cash_modifier_price);
        RelativeLayout note = (RelativeLayout) convertView.findViewById(R.id.cash_modifier_note);
        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.cash_modifier_container);
        if(!product.getIsPaid()){
            if(modifier.getModifierId()==-15){

                note.setVisibility(VISIBLE);
                container.setVisibility(GONE);
                CustomTextView noteText = (CustomTextView) convertView.findViewById(R.id.cash_modifier_note_title);
                noteText.setText(modifier.getNote());

            }else {
                note.setVisibility(GONE);
                container.setVisibility(VISIBLE);
                title.setText(modifier.getTitle());
                quantity.setText(modifier.getQuantity());
                String total = modifier.getTotal(modifier.getQuantityInt());
                price.setText(total.replace(".", ","));

                quantity.setVisibility(VISIBLE);
                price.setVisibility(VISIBLE);
                title.setVisibility(VISIBLE);
                convertView.setVisibility(VISIBLE);
            }
        }else{
            title.setVisibility(GONE);
            quantity.setVisibility(GONE);
            price.setVisibility(GONE);
            convertView.setVisibility(GONE);
        }


        return convertView;
    }

}
