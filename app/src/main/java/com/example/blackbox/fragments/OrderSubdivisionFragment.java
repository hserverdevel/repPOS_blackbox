package com.example.blackbox.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blackbox.R;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.adapter.SubdivisionAdapter;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.example.blackbox.adapter.OrderListAdapter.ITEM_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.NUMBER_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERCENTAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERSON_MODE;
/**
 * Created by DavideLiberato on 13/07/2017.
 */

public class OrderSubdivisionFragment extends Fragment {

    private View myself;
    private RecyclerView recyclerView;
    private SubdivisionAdapter subdivisionAdapter;
    private OrderFragment of;
    private int mode;
    private int billId;
    private DatabaseAdapter dbA;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        myself = inflater.inflate(R.layout.fragment_order_subdivision, null);
        dbA = new DatabaseAdapter(this.getContext());
        // che orribile riga di codice
        of = (OrderFragment)((FragmentActivity)getContext()).getSupportFragmentManager().findFragmentByTag("order");
        subdivisionAdapter = new SubdivisionAdapter(getContext(), of);
        recyclerView = (RecyclerView)myself.findViewById(R.id.order_subdivision_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        /** setting swipe-to-delete up **/
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //Toast.makeText(getContext(), "on Move", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView

                if (
                        !((PaymentActivity) getActivity()).isCalculatorOn &&
                        !((PaymentActivity)getContext()).getDiscountSet() &&
                        !((PaymentActivity)getContext()).splitItemSet
                        ) {
                    final int position = viewHolder.getAdapterPosition();
                    SubdivisionItem item = subdivisionAdapter.getItemByPosition(position);
                    if(item!=null) {
                        if(item.getMode()!=-1) {
                            subdivisionAdapter.removeItem(position);
                            int itemsCount = subdivisionAdapter.getItemCount();
                            if (subdivisionAdapter.checkIfLastItemIsPerNumber()) {
                                subdivisionAdapter.removeItem(itemsCount - 1);
                            }
                        }else{
                            subdivisionAdapter.notifyDataSetChanged();
                        }
                    }else{
                        subdivisionAdapter.removeItem(position);
                        int itemsCount = subdivisionAdapter.getItemCount();
                        if (subdivisionAdapter.checkIfLastItemIsPerNumber()) {
                            subdivisionAdapter.removeItem(itemsCount - 1);
                        }
                    }
                }else{
                    subdivisionAdapter.notifyDataSetChanged();
                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(subdivisionAdapter);
        billId = ((FragmentActivity) getContext()).getIntent().getIntExtra("billId", -1);
        //TODO fare questo load!!!
        loadBillSplits(billId);
        if(StaticValue.blackbox){
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            ((PaymentActivity) getContext()).callHttpHandler("/checkIfBillIsPaid", params);
        }else {
            if (((PaymentActivity) getContext()).returnTotalIsPaid()) {
                int payment = dbA.getBillPayment(billId);
                subdivisionAdapter.setTotalItemPayment(payment);
                SubdivisionItem item = getSubdivisionAdapter().getTotalItem();
                if (item.isPaid()) {

                    if (item.getOwed_money() > 0.0f) {
                        getSubdivisionAdapter().showItem(1);

                    } else {
                        getSubdivisionAdapter().showItem(2);
                    }
                    of.paidOrder();
                }
            }
        }
        return myself;
    }


    public void setMode(int mode){
        this.mode = mode;
        subdivisionAdapter.setMode(mode);
    }


    public void addElement(Object o, Float cost){
        switch(mode){
            case PERSON_MODE:
                int position1 = subdivisionAdapter.addElement((HashMap<CashButtonLayout,Integer>)o, cost, o, 1);
                showSubdivisionItem(position1);
                break;
            case ITEM_MODE:
                int position = subdivisionAdapter.addElement((HashMap<CashButtonLayout,Integer>)o, cost, o, 1);
                showSubdivisionItem(position);
                break;
            case NUMBER_MODE:
                int n = ((Float)o).intValue();
                float resto = 0.0f;
                resto = cost - n*(cost/n);

                int position2 = subdivisionAdapter.addElement(null, ((cost / n) + resto), o, n);
                showSubdivisionItem(position2);

                break;
            case PERCENTAGE_MODE:
                if(((PaymentActivity) getContext()).getPercentageAmount()){
                    int position3 = subdivisionAdapter.addElement(null, (Float) o, o, 1);
                    showSubdivisionItem(position3);

                }
                else {
                    int position4 = subdivisionAdapter.addElement(null, (cost * (float)o)/100, o, 1);
                    showSubdivisionItem(position4);
                }
                break;
        }
    }


    private void loadBillSplits(int billId){
        if(StaticValue.blackbox){
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            ((PaymentActivity) getContext()).callHttpHandler("/getBillSplits", params);
        }else {
            ArrayList<SubdivisionItem> bill_splits;
            bill_splits = dbA.getBillSplits(billId);

            if (bill_splits.size() > 0) {
                subdivisionAdapter.loadPaidBillSplits(bill_splits);
                of.getOrderListAdapter().setSplit(true);
                of.getOrderListAdapter().loadBillSplits(bill_splits);

                if (!((PaymentActivity) getContext()).checkIfOtherSplitBillArePaid())
                    of.showRemaingTotal();
            }
            subdivisionAdapter.showFirstPaid();
        }
        /*if(bill_splits.size()>0)
            if(bill_splits.get(bill_splits.size()-1).isPaid()) of.paidOrder();*/


    }

    public SubdivisionAdapter getSubdivisionAdapter(){
        if(subdivisionAdapter==null) return null;
        else return subdivisionAdapter;
    }

    public void showSubdivisionItem(int position){
        subdivisionAdapter.showItem(position);

        recyclerView.smoothScrollToPosition(subdivisionAdapter.getItemCount()-1);

    }

    public void showOriginalBill(){
        subdivisionAdapter.showItemOriginal();

    }
}
