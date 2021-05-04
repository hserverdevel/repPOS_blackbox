package com.example.blackbox.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.adapter.OrderListAdapter;
import com.example.blackbox.adapter.SubdivisionAdapter;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.example.blackbox.model.TotalBill;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.example.blackbox.adapter.OrderListAdapter.DEFAULT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.ELEMENT_ITEM_SPLIT;
import static com.example.blackbox.adapter.OrderListAdapter.HOMAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.ITEM_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.MODIFY_DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.NUMBER_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PARTIAL_TOTAL_DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERCENTAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERSON_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.TOTAL_DISCOUNT_MODE;
import static com.example.blackbox.fragments.CalculatorFragment.roundDecimal;

/**
 * Created by DavideLiberato on 07/07/2017.
 */

public class OrderFragment extends Fragment
{

    private String TAG = "<OrderFragment>";

    private ExpandableListView orderList;
    private OrderListAdapter orderListAdapter;
    private View myself;
    private DatabaseAdapter dbA;
    private int billId;
    private int mode;
    private ArrayList<Object> subdivisionList;
    private Map<CashButtonLayout,Integer> subdivisionMap;
    private ArrayList<Integer> homageList = new ArrayList<Integer>();
    public ArrayList<Integer> getHomageList(){return homageList;}
    public void setHomageList(ArrayList<Integer> l ){homageList = l;}
    private ArrayList<Integer> discountList = new ArrayList<Integer>();
    public ArrayList<Integer> getDiscountList(){return discountList;}
    public void setDiscountList(ArrayList<Integer> l ){discountList = l;}
    private Map<CashButtonLayout,CashButtonLayout> subdivisionCorrispondenceMap;
    private ArrayList<CashButtonLayout> sentItems;
    private CustomTextView o_tv;
    private int orderNumber;
    private int tableNumber = -1;
    public int getTableNumber(){ return tableNumber;}
    private String  roomName = "";
    public String getRoomName(){return roomName;}
    private Float cost = 0.0f;
    private Float total_cost;
    private Float partial_cost;
    public void setPartialCost(float f){
        partial_cost = f;
    }
    private boolean number_division_done = false;
    private boolean percentage_division_done = false;

    private Float remainingPercentageCost;

    public boolean totalHomage = false;
    public void setTotalHomage(boolean b){totalHomage = b;}
    public boolean getTotalHomage(){return totalHomage;}
    public void setTotalCost(Float p){total_cost = p;}
    public float getTotalCost(){return total_cost;}

    public boolean totalDiscount = false;
    public void setTotalDiscount(boolean b){totalDiscount = b;}
    public boolean getTotalDiscount(){return totalDiscount;}
    public boolean partialTotalDiscount = false;
    public void setPartialTotalDiscount(boolean b){partialTotalDiscount = b;}
    public boolean getPartialTotalDiscount(){return partialTotalDiscount;}

    public boolean discountSet = false;

    //used to remove items when added hrough item add
    public ArrayList<CashButtonLayout> myButton = new ArrayList<CashButtonLayout>();

    public ArrayList<CashButtonLayout> myProduct= new ArrayList<CashButtonLayout>();


    public OrderFragment(){
        super();
    }

    public float returnRemaningTotal(){
        Float c = total_cost;
        if(number_division_done) c = 0.0f;
        else if(percentage_division_done) c = remainingPercentageCost;
        //else if(percentage_division_done) c = remainingPercentageCost;
            //else c = total_cost-orderListAdapter.getPartial_cost();
        else c = orderListAdapter.getPartial_cost();
        partial_cost = c;
      //  dbA.showData("bill_total_extra");
       /*float discount = total_cost - dbA.getBillDiscountPrice(billId);
        c=c-discount;*/


        return c;
    }

    public void setDescriptionItemSplit(int itemType){
        switch(itemType){
            case -1 :
                ((CustomTextView) myself.findViewById(R.id.client_tv)).setText(R.string.original_bill);
                break;
            case 1 :
                ((CustomTextView) myself.findViewById(R.id.client_tv)).setText(R.string.per_number);
                break;
            case 2 :
                ((CustomTextView) myself.findViewById(R.id.client_tv)).setText(R.string.per_item);
                break;
            case 3:
                ((CustomTextView) myself.findViewById(R.id.client_tv)).setText(R.string.per_person);
                break;
            case 4 :
                ((CustomTextView) myself.findViewById(R.id.client_tv)).setText(R.string.per_clients);
                break;
            default :
                ((CustomTextView) myself.findViewById(R.id.client_tv)).setText(" ");
                break;
        }
    }

    public void setBillDataFromServer(
            TotalBill totals,
            ArrayList<Customer> myCustomers,
            ArrayList<CashButtonLayout> myGroups,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> myMap) {
            orderListAdapter.setBillDataFromServer(totals, myCustomers, myGroups, myMap);
            orderListAdapter.notifyDataSetChanged();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        myself = inflater.inflate(R.layout.order_fragment, container, false);
        dbA = new DatabaseAdapter(this.getContext());

        o_tv = (CustomTextView)myself.findViewById(R.id.orderNumber_tv);

        Intent intent = getActivity().getIntent();
        orderNumber = intent.getIntExtra("orderNumber", 158);
        billId = intent.getIntExtra("billId", -1);

        Log.i(TAG, "ENTRO IN PaymentActivity PER ORDER NUMBER " + orderNumber );
        setSubOrderNumber("");
        orderList = (ExpandableListView)myself.findViewById(R.id.order_list);
        orderListAdapter = new OrderListAdapter(billId, dbA, getContext());
        orderList.setAdapter(orderListAdapter);
        subdivisionList = new ArrayList<>();
        sentItems = new ArrayList<>();
        subdivisionMap = new HashMap<>();
        subdivisionCorrispondenceMap = new HashMap<>();


        if(dbA.checkIfBillSplitPaid(billId) ) {
            Double a = dbA.getBillPrice(billId);
            percentage_division_done = true;
            remainingPercentageCost = Float.parseFloat(String.valueOf(a).replace(",", "."));
            partial_cost = Float.parseFloat(String.valueOf(a).replace(",", "."));
            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
            String txt = String.format("%.2f", a);
            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(String.valueOf(txt));

            ((PaymentActivity)getContext()).setCalculatorCost(txt);
        }

        String query = "SELECT * FROM table_use WHERE total_bill_id="+billId+" and main_table=1;";
        int oldPosition = dbA.getOldTablePosition(query);

        CustomTextView tNumber = (CustomTextView) myself.findViewById(R.id.tableNumber_tv);
        if(oldPosition!=-1){
            tableNumber = oldPosition;
            if(tNumber!=null) tNumber.setText(""+(oldPosition));
            int roomId = dbA.getRoomIdAgain(billId);
            Room room = dbA.fetchRoomById(roomId);
            roomName = room.getName();
        }else{
            if(tNumber!=null) tNumber.setText("");

        }

        ImageView discountImage = (ImageView) myself.findViewById(R.id.euro_discount_image_vv);
        discountImage.bringToFront();
        discountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PaymentActivity) getContext()).openModifyPopupForTotal();

            }
        });

        orderList.post(new Runnable() {
            @Override
            public void run() {
                if(!StaticValue.blackbox){
                    setPostRunnable(0, 0.0f, false);
                }
            }
        });

        return myself;
    }


    public void setPostRunnable(int myHomage, float myDiscount, boolean blackbox) {

        int count = orderListAdapter.getGroupCount();
        for (int i=0; i< count; i++) {
            orderList.expandGroup(i);
        }

        total_cost = roundDecimal(orderListAdapter.getTotal_cost(),2);
        Float c;
        if (number_division_done)
            { c = 0.0f; }
        else if (percentage_division_done)
            { c = remainingPercentageCost; }
        else
            { c = orderListAdapter.getPartial_cost(); }

        int homage = 0;
        if(blackbox)
            homage = myHomage;
        else
            homage = dbA.getBillTotalHomage(billId);
        if(homage==0) {
            Float discount = 0.0f;
            if(blackbox)
                discount = myDiscount;
            else
                discount = roundDecimal(dbA.getBillDiscountPrice(billId), 2);
            if (discount != 0) {
                //c'è discount
                if (((PaymentActivity) getContext()).returnSubdivisionSize() == 1) {
                    //c'è solo un subdivision item = total item
                    hideRemaingTotal();
                    float itemsDiscount = roundDecimal(orderListAdapter.getGroupsDiscount(), 2);
                    if (itemsDiscount != discount)
                        showDiscountContainer();
                    remainingPercentageCost = total_cost - discount;
                    String txtTotal = String.format("%.2f", total_cost - discount).replace(".", ",");
                    setTotalAmountAndCalculator(txtTotal);
                } else {
                    // ci sono più subdivision item
                    if (((PaymentActivity) getContext()).checkIfThereIsOneAmount()) {
                        //c'è almeno un subdivision amount, faccio vedere vista splittata
                        showRemaingTotal();
                        float itemsDiscount = orderListAdapter.getGroupsDiscount();
                        if (itemsDiscount != discount)
                            showDiscountContainer();
                        remainingPercentageCost = total_cost - discount;
                        String txtTotal = String.format("%.2f", total_cost - discount).replace(".", ",");
                        setTotalAmountAndCalculator(txtTotal);
                    } else {
                        //sono item, perso o number, faccio vedere tutto
                        hideRemaingTotal();
                        float itemsDiscount = orderListAdapter.getGroupsDiscount();
                        if (itemsDiscount != discount)
                            showDiscountContainer();
                        remainingPercentageCost = total_cost - discount;
                        String txtTotal = String.format("%.2f", total_cost - discount).replace(".", ",");
                        setTotalAmountAndCalculator(txtTotal);
                    }
                }
            } else {
                //non c'è discount
                if (((PaymentActivity) getContext()).returnSubdivisionSize() == 1) {
                    //c'è solo un subdivision item = total item
                    String txtTotal = String.format("%.2f", total_cost).replace(".", ",");
                    ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                            .setText(txtTotal);
                    ((PaymentActivityCommunicator) getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txtTotal, 0.0f);
                } else {
                    // ci sono più subdivision item
                    if (((PaymentActivity) getContext()).checkIfThereIsOneAmount()) {
                        //c'è almeno un subdivision amount, faccio vedere vista splittata
                        showRemaingTotal();
                        float itemsDiscount = orderListAdapter.getGroupsDiscount();
                        if (itemsDiscount != discount)
                            showDiscountContainer();
                        remainingPercentageCost = total_cost - discount;
                        String txtTotal = String.format("%.2f", total_cost - discount).replace(".", ",");
                        setTotalAmountAndCalculator(txtTotal);
                    } else {
                        //sono item, perso o number, faccio vedere tutto
                        hideRemaingTotal();
                        float itemsDiscount = orderListAdapter.getGroupsDiscount();
                        if (itemsDiscount != discount)
                            showDiscountContainer();
                        remainingPercentageCost = total_cost - discount;
                        String txtTotal = String.format("%.2f", total_cost - discount).replace(".", ",");
                        setTotalAmountAndCalculator(txtTotal);

                        if(dbA.checkIfBillIsPaid(billId)) {
                            paidOrder();
                        }
                    }
                }
            }
        }else{
            //il total bill è homaggio
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(R.string.gratis_all_caps);
            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
            RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
            RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
            discount.setVisibility(View.GONE);
            nodiscount.setVisibility(View.VISIBLE);
            ((PaymentActivity) getContext()).hideSplitButton();
            ((PaymentActivity) getContext()).hidePaymentButton();
            ((PaymentActivity) getContext()).hidenBlueButtonForTotalHomage();

        }
    }

    public void setTotalAmountAndCalculator(String txt){
        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                .setText(txt);
        ((PaymentActivityCommunicator) getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
    }

    public void paidOrder(){
        myself.findViewById(R.id.order_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
        myself.findViewById(R.id.orderNumber_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
        myself.findViewById(R.id.table_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
        myself.findViewById(R.id.tableNumber_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
   }

    public void notPaidOrder(){
        myself.findViewById(R.id.order_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        myself.findViewById(R.id.orderNumber_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        myself.findViewById(R.id.table_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        myself.findViewById(R.id.tableNumber_tv).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        if(((PaymentActivity)getContext()).checkIfTotalBillHasDiscount())
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
        else
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));

    }


    public void doSomething(View v, int groupPosition, int click){
        CashButtonLayout p = (CashButtonLayout)v.getTag();
        CashButtonLayout b;
        switch(mode){
            case PERCENTAGE_MODE:
                break;
            case PERSON_MODE:
                if(click==1) {
                    //click for whole customer
                    ArrayList<CashButtonLayout> products = orderListAdapter.getCustomerProducts(p);
                    float itemCost = 0.0f;
                    float homageV = 0.0f;
                    for(CashButtonLayout pp : products){
                        if(!pp.isSelected()) {
                            if (pp.getHomage() == 0)
                                itemCost += orderListAdapter.getElementPrice(pp.getPosition());
                            else {
                                homageV += pp.getPriceFloat();
                                itemCost -= pp.getPriceFloat();
                            }
                        }
                    }
                    float mycost = orderListAdapter.getLeftCost();
                    if (roundDecimal( mycost, 2)>= roundDecimal( itemCost, 2)) {

                        for (CashButtonLayout pp : products) {
                            if (!sentItems.contains(pp) && !pp.isSelected()) {
                                /**
                                 * p = product corresponding to group clicked;
                                 * b = copy of p; needs to be done because we need same id BUT different object
                                 * A correspondence table between p and b is kept for each 'per item' subdivision,
                                 * Now, if b was already instantiated for a certain p (product) during the current selection
                                 * then its 'selected quantity' gets increased by one ONLY IF the total qty selected is not > then the qty
                                 * on the bill.
                                 * When OK gets hit the maps are set to 0
                                 */
                                //float itemCost = orderListAdapter.getElementPrice(groupPosition);
                                float itemCost1 = orderListAdapter.getSingleElementPrice(groupPosition);
                                float a = orderListAdapter.getLeftCost();
                                float c = dbA.getBillDiscountPrice(billId);
                                if ((b = subdivisionCorrispondenceMap.get(pp)) == null) {
                                        b = new CashButtonLayout();
                                        b.setID(pp.getID());
                                        b.setTitle(pp.getTitle());
                                        b.setQuantity(pp.getQuantityInt());
                                        //b.setQuantity(p.getQuantityInt());
                                        b.setPrice(pp.getPriceFloat());
                                        b.setProductId(pp.getProductId());
                                        b.setHomage(pp.getHomage());
                                        b.setDiscount(pp.getDiscount());
                                        b.setClientPosition(pp.getClientPosition());
                                        b.setPosition(pp.getPosition());
                                        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = orderListAdapter.getModifier();
                                        ArrayList<CashButtonListLayout> list = myMap.get(pp);
                                        b.setNewCashList(list);
                                        b.setOriginalCBL(pp);
                                        b.setVat(pp.getVat());

                                        // modifiers list?
                                        subdivisionCorrispondenceMap.put(pp, b);
                                        subdivisionMap.put(b, 0);


                                 }else{
                                        //se è già stato aggiunto aumeno solo la quantità
                                        b = subdivisionCorrispondenceMap.get(pp);
                                        subdivisionMap.put(b, subdivisionMap.get(b)+1);
                                        b.setQuantity(b.getQuantityInt()+1);
                                 }
                                    if (!pp.isSelected()) {
                                        myButton.add(b);
                                        myProduct.add(pp);
                                        int qty = orderListAdapter.setCustomerGroupSelected(false, pp.getPosition(), 0);subdivisionCorrispondenceMap.put(pp, b);
                                        if (pp.getQuantityInt() - qty >= 0) {
                                            if(b.getQuantityInt()==1)
                                            subdivisionMap.put(b, subdivisionMap.get(b) + 1);
                                        else {

                                                subdivisionMap.put(b, subdivisionMap.get(b) /*pp.getQuantityInt()*/  +qty/*b.getQuantityInt()*/);
                                            }

                                        }
                                    } else {

                                    }
                            }
                        }
                        cost += itemCost+homageV;
                        orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() - (itemCost+homageV));

                        String txt = String.format("%.2f", roundDecimal((orderListAdapter.getLeftCost()), 2));
                        ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(txt.replace(".", ","));

                        showRemaingTotalContainer();
                        if(((PaymentActivity) getContext()).checkIfTotalBillHasDiscount()){
                            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));

                        }
                        orderListAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(getContext(), R.string.items_price_is_bigger_than_left_cost, Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //click for on item from customer
                    if (!sentItems.contains(p)) {
                        /**
                         * p = product corresponding to group clicked;
                         * b = copy of p; needs to be done because we need same id BUT different object
                         * A correspondence table between p and b is kept for each 'per item' subdivision,
                         * Now, if b was already instantiated for a certain p (product) during the current selection
                         * then its 'selected quantity' gets increased by one ONLY IF the total qty selected is not > then the qty
                         * on the bill.
                         * When OK gets hit the maps are set to 0
                         */
                        //float itemCost = orderListAdapter.getElementPrice(groupPosition);
                        float itemCost = orderListAdapter.getSingleElementPrice(groupPosition);
                        float a = orderListAdapter.getLeftCost();
                        float c = dbA.getBillDiscountPrice(billId);
                        if (roundDecimal(orderListAdapter.getLeftCost(),2)/*-dbA.getBillDiscountPrice(billId)*/ >= itemCost) {

                            if ((b = subdivisionCorrispondenceMap.get(p)) == null) {
                                b = new CashButtonLayout();
                                b.setID(p.getID());
                                b.setTitle(p.getTitle());
                                b.setQuantity(p.getQuantityInt());
                                //b.setQuantity(p.getQuantityInt());
                                b.setPrice(p.getPriceFloat());
                                b.setProductId(p.getProductId());
                                b.setHomage(p.getHomage());
                                b.setDiscount(p.getDiscount());
                                b.setClientPosition(p.getClientPosition());
                                b.setPosition(p.getPosition());
                                Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = orderListAdapter.getModifier();
                                ArrayList<CashButtonListLayout> list = myMap.get(p);
                                b.setNewCashList(list);
                                b.setOriginalCBL(p);
                                b.setVat(p.getVat());

                                // modifiers list?
                                subdivisionCorrispondenceMap.put(p, b);
                                subdivisionMap.put(b, 0);


                            }else{
                                //se è già stato aggiunto aumeno solo la quantità
                                b = subdivisionCorrispondenceMap.get(p);
                                b.setQuantity(b.getQuantityInt()+1);
                            }
                            if (!p.isSelected()) {
                                myButton.add(b);
                                myProduct.add(p);
                                int qty = orderListAdapter.setGroupSelected(false, groupPosition, 0);
                                subdivisionCorrispondenceMap.put(p, b);
                                if (p.getQuantityInt() - qty >= 0) {
                                    subdivisionMap.put(b, subdivisionMap.get(b) + 1);

                                    float provacost = p.getPriceFloat();
                                    float disc = 0.0f;
                                    disc = p.getDiscount();

                                    for (int i = 0; i < orderListAdapter.getChildrenCount(groupPosition); i++) {
                                        CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, i);
                                        provacost += mod.getPriceFloat();
                                    }

                                    //cost += p.getPriceFloat();
                                    float toSubstract = 0.0f;
                                    if(p.getHomage()==1){

                                    }
                                    else {
                                        toSubstract = provacost-disc;
                                        cost+= provacost-disc;
                                    }
                                    float aa = orderListAdapter.getLeftCost();
                                    orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() - (toSubstract));
                                    String txt = String.format("%.2f", roundDecimal((orderListAdapter.getLeftCost()), 2));
                                            //.replace(",", ".");
                                    ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(txt.replace(".", ","));

                                    showRemaingTotalContainer();
                                    if(((PaymentActivity) getContext()).checkIfTotalBillHasDiscount()){
                                        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));

                                    }
                                    orderListAdapter.notifyDataSetChanged();

                                }
                            } else {

                            }
                        }else{
                            Toast.makeText(getContext(),R.string.items_price_is_bigger_than_left_cost, Toast.LENGTH_SHORT).show();
                        }
                                       }
                    else Toast.makeText(getContext(),R.string.item_already_processed, Toast.LENGTH_SHORT).show();
                }
                break;
            case ITEM_MODE:
                if (!sentItems.contains(p)) {
                    /**
                     * p = product corresponding to group clicked;
                     * b = copy of p; needs to be done because we need same id BUT different object
                     * A correspondence table between p and b is kept for each 'per item' subdivision,
                     * Now, if b was already instantiated for a certain p (product) during the current selection
                     * then its 'selected quantity' gets increased by one ONLY IF the total qty selected is not > then the qty
                     * on the bill.
                     * When OK gets hit the maps are set to 0
                     */
                    //float itemCost = orderListAdapter.getElementPrice(groupPosition);
                    float itemCost = orderListAdapter.getSingleElementPrice(groupPosition);
                    float a = orderListAdapter.getLeftCost();
                    float c = dbA.getBillDiscountPrice(billId);
                    if (roundDecimal(orderListAdapter.getLeftCost(), 2)/*-dbA.getBillDiscountPrice(billId)*/ >= itemCost) {

                        if ((b = subdivisionCorrispondenceMap.get(p)) == null) {
                            b = new CashButtonLayout();
                            b.setID(p.getID());
                            b.setTitle(p.getTitle());
                            b.setQuantity(p.getQuantityInt());
                            //b.setQuantity(1);
                            //b.setQuantity(p.getQuantityInt());
                            b.setPrice(p.getPriceFloat());
                            b.setProductId(p.getProductId());
                            b.setHomage(p.getHomage());
                            b.setDiscount(p.getDiscount());
                            b.setClientPosition(p.getClientPosition());
                            b.setPosition(p.getPosition());
                            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = orderListAdapter.getModifier();
                            ArrayList<CashButtonListLayout> list = myMap.get(p);

                            b.setNewCashList(list);
                            b.setOriginalCBL(p);
                            b.setVat(p.getVat());
                            b.setPercentage(p.getPercentage());

                            // modifiers list?
                            subdivisionCorrispondenceMap.put(p, b);
                            subdivisionMap.put(b, 0);


                        }else{
                            //se è già stato aggiunto aumeno solo la quantità
                            b = subdivisionCorrispondenceMap.get(p);
                            //b.setQuantity(b.getQuantityInt()+1);
                        }
                        if (!p.isSelected()) {
                            myButton.add(b);
                            myProduct.add(p);
                            int qty = orderListAdapter.setGroupSelected(false, groupPosition, 0);
                            subdivisionCorrispondenceMap.put(p, b);
                            if (p.getQuantityInt() - qty >= 0) {
                                subdivisionMap.put(b, subdivisionMap.get(b) + 1);
                             float provacost = p.getPriceFloat();
                                float disc = 0.0f;
                                disc = p.getDiscount();

                                for (int i = 0; i < orderListAdapter.getChildrenCount(groupPosition); i++) {
                                    CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, i);
                                    provacost += mod.getPriceFloat()*(mod.getQuantityInt()/p.getQuantityInt());
                                   // provacost += mod.getPriceFloat();
                                }

                                //cost += p.getPriceFloat();
                                float toSubstract = 0.0f;
                                if(p.getHomage()==1){
                                    //toSubstract = provacost-disc;
                                    //cost+= provacost-disc;
                                }
                                else {
                                    toSubstract = provacost-disc;
                                    cost+= provacost-disc;
                                }
                                float aa = orderListAdapter.getLeftCost();
                                float prova = orderListAdapter.getLeftCost() - (toSubstract);
                                orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() - (toSubstract));
                                String txt = String.format("%.2f", roundDecimal((orderListAdapter.getLeftCost()), 2));
                                        //.replace(",", ".");
                                ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(txt.replace(".", ","));

                                showRemaingTotalContainer();
                                if(((PaymentActivity) getContext()).checkIfTotalBillHasDiscount()){
                                    myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));

                                }
                                orderListAdapter.notifyDataSetChanged();

                            }
                        } else {
                            int qty_to_remove = subdivisionMap.get(b);
                            orderListAdapter.setGroupSelected(true, groupPosition, qty_to_remove);
                            subdivisionMap.remove(b);
                            subdivisionCorrispondenceMap.remove(p);
                            for (int i = 0; i < orderListAdapter.getChildrenCount(groupPosition); i++) {
                                CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, i);
                                cost -= (mod.getPriceFloat()*mod.getQuantityInt()) * qty_to_remove;
                            }
                            cost -= p.getPriceFloat() * qty_to_remove;
                            String txt = String.format("%.2f", roundDecimal((orderListAdapter.getTotal_cost() - cost), 2));
                                    //.replace(",", ".");
                            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                                    .setText(txt.replace(".", ","));
                            showRemaingTotalContainer();
                        }
                    }else{
                        Toast.makeText(getContext(),R.string.items_price_is_bigger_than_left_cost, Toast.LENGTH_SHORT).show();
                    }
                }
                else Toast.makeText(getContext(),R.string.item_already_processed, Toast.LENGTH_SHORT).show();
                break;
            case HOMAGE_MODE:
                SubdivisionItem item = ((PaymentActivity) getContext()).getSelectedItem();
                if(item==null) item = ((PaymentActivity) getContext()).getTotalItem();
                //element item split only allowed from original bill
                if(item.getMode()==-1){
                    CashButtonLayout product = (CashButtonLayout) orderListAdapter.getGroup(groupPosition);
                    if(product.getHomage()==0) {
                        ((PaymentActivity) getContext()).activateFunction(ELEMENT_ITEM_SPLIT, product, (float) groupPosition);
                    }else{
                        Toast.makeText(getContext(), R.string.you_cant_split_item_which_is_already_homage, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), R.string.you_can_split_item_only_on_original_bill, Toast.LENGTH_SHORT).show();
                }

                break;
            case DISCOUNT_MODE:
                if(!discountSet) {

                    discountSet = true;
                    int position = orderListAdapter.separateElement(groupPosition);
                    SubdivisionItem itemD = ((PaymentActivity) getContext()).getSelectedItem();
                    if(itemD==null) itemD = ((PaymentActivity) getContext()).getTotalItem();
                    if(itemD.getMode()==-1)
                        ((PaymentActivity)getContext()).setTotalBillItems(orderListAdapter.getProducts());
                    else if(itemD.getMode()==PERSON_MODE || itemD.getMode()==ITEM_MODE)
                        ((PaymentActivity)getContext()).setItemProduct(orderListAdapter.getProducts());
                    discountList.add(position);
                    ((PaymentActivity)getContext()).setTempPositionDiscount(position);
                    orderList.expandGroup(position);
                    orderList.setSelection(position);
                    CashButtonLayout product = (CashButtonLayout) orderListAdapter.getGroup(position);
                    ((PaymentActivity) getContext()).activateFunction(DISCOUNT_MODE, product, (float) position);

                }
                break;
            case MODIFY_DISCOUNT_MODE:

                if(!discountSet) {

                    discountSet = true;
                    int position = orderListAdapter.separateElement(groupPosition);
                    discountList.add(position);
                    orderList.expandGroup(position);
                    orderList.setSelection(position);
                    CashButtonLayout product = (CashButtonLayout) orderListAdapter.getGroup(position);
                    ((PaymentActivity) getContext()).activateFunction(MODIFY_DISCOUNT_MODE, product, (float) groupPosition);

                }

                break;
            default:

                break;
        }
    }


    public void removeAllFromItem(){
        while(myButton.size()!=0){
            removeLastAddSubdivision( );
        }
    }

    public void setHomageMethod(int groupPosition) {
        SubdivisionItem item = ((PaymentActivity) getContext()).getSelectedItem();
        if(item==null) item = ((PaymentActivity) getContext()).getTotalItem();
        if (getTotalHomage() || item.isHomage()) {
            Toast.makeText(getContext(), R.string.you_have_already_homage_the_whole_bill, Toast.LENGTH_SHORT).show();
        } else {

            CashButtonLayout product = (CashButtonLayout) orderListAdapter.getGroup(groupPosition);
            if(product.getHomage()==0) {
                float a = returnRemaningTotal();
                float b1 = product.getPriceFloat() / product.getQuantityInt();

                if(StaticValue.blackbox){
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                    params.add(new BasicNameValuePair("position", String.valueOf(groupPosition)));
                    ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountForHomage", params);
                }else {
                    int productBillId = dbA.getBillProduct(billId, groupPosition);
                    float discountProduct = dbA.getBillProductDiscount(billId, groupPosition);
                    dbA.updateProductBillDiscount(0.0f, productBillId);
                    float totalDiscount = dbA.getBillDiscountPrice(billId);
                    float newDiscount = totalDiscount - discountProduct;
                    dbA.updateBillExtra(billId, roundDecimal(newDiscount, 2), roundDecimal(newDiscount, 2));
                }
                    int position = orderListAdapter.separateElementHomage(groupPosition);
                    if(item.getMode()==-1)
                        if(item.getMode()==2 || item.getMode()==3) {
                            ((PaymentActivity)getContext()).setSubdivisionCostLeft(product.getPriceFloat()-product.getDiscount(), false);
                        }

                    float provacost = product.getPriceFloat();
                    float disc = 0.0f;
                    disc = product.getDiscount();

                    for (int i = 0; i < orderListAdapter.getChildrenCount(groupPosition); i++) {
                        CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, i);
                        provacost += mod.getPriceFloat();
                    }
                    orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() - (provacost-disc));
                    orderListAdapter.setHomageListQuantity(product);
                    if(item.getMode()==-1)((PaymentActivity)getContext()).setTotalBillItems(orderListAdapter.getProducts());
                    orderListAdapter.setRealGroupHomage(position);
                    remainingPercentageCost = orderListAdapter.getLeftCost();
                    if (item.getMode() != -1) {
                        item.setOwed_money(item.getOwed_money() - provacost);
                        float discount = 0.0f;
                        ArrayList<CashButtonLayout> group = item.getItems();
                        for(int i=0; i< group.size(); i++){
                            discount+=group.get(i).getDiscount();
                        }
                        String txt = String.format("%.2f", roundDecimal(item.getOwed_money()-item.getDiscount()-discount, 2));
                        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt.replace(".", ","));
                        ((PaymentActivity) getContext()).setCalculatorCost(txt.replace(".", ","));
                    }else{
                        String txt = String.format("%.2f", roundDecimal(orderListAdapter.getLeftCost(), 2));
                        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt.replace(".", ","));
                        ((PaymentActivity) getContext()).setCalculatorCost(txt.replace(".", ","));
                    }

                    orderList.expandGroup(position);
                    orderList.setSelection(position);
                    orderListAdapter.setMode(DEFAULT_MODE);
                    mode = DEFAULT_MODE;
                    ((PaymentActivity) getContext()).exitHomageMode();

                    orderListAdapter.orderGroups();
                    orderListAdapter.notifyDataSetChanged();
            }else{
                float provacost = product.getPriceFloat();
                float disc = 0.0f;
                disc = product.getDiscount();
                for (int i = 0; i < orderListAdapter.getChildrenCount(groupPosition); i++) {
                    CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, i);
                    provacost += mod.getPriceFloat();
                }
                if(item.getMode()==2 || item.getMode()==3) {

                }
                orderListAdapter.resetGroupHomageElement(groupPosition);
                if (item.getMode() != -1) {
                    item.setOwed_money(item.getOwed_money()+provacost);
                    float discount = 0.0f;
                    ArrayList<CashButtonLayout> group = item.getItems();
                    for(int i=0; i< group.size(); i++){
                        discount+=group.get(i).getDiscount();
                    }
                    String txt = String.format("%.2f", roundDecimal(item.getOwed_money()-item.getDiscount()-discount, 2));
                    ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt.replace(".", ","));
                    ((PaymentActivity) getContext()).setCalculatorCost(txt.replace(".", ","));
                }else{
                    float discount = 0.0f;
                    float homage = 0.0f;
                    ArrayList<CashButtonLayout> group = item.getItems();
                    for(int i=0; i< group.size(); i++){
                        discount+=group.get(i).getDiscount();
                    }
                    for(int i=0; i< group.size(); i++){
                        if(group.get(i).getHomage()!=0)
                            homage+=group.get(i).getPriceFloat();
                    }
                    orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() + (provacost-disc));
                    if(remainingPercentageCost!=null) remainingPercentageCost = orderListAdapter.getLeftCost();
                    String txt = String.format("%.2f", roundDecimal(item.getOwed_money()-item.getDiscount()-discount-homage, 2));
                            //.replace(",", ".");
                    ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt.replace(".", ","));
                    ((PaymentActivity) getContext()).setCalculatorCost(txt.replace(".", ","));
                }
                orderList.expandGroup(groupPosition);
                orderList.setSelection(groupPosition);
                orderListAdapter.setMode(DEFAULT_MODE);
                mode = DEFAULT_MODE;

                ((PaymentActivity) getContext()).exitHomageMode();
            }
        }
    }

    public boolean removeLastAddSubdivision( ){
        if(myButton.size()>0 && subdivisionMap.size()>0) {
            CashButtonLayout b = myButton.get(myButton.size() - 1);
            CashButtonLayout p = myProduct.get(myProduct.size() - 1);
            int groupPosition = b.getPosition();
            if(subdivisionMap.containsKey(b)) {
                int qty_to_remove = subdivisionMap.get(b);
                if (qty_to_remove == 1) {
                    float modiVal = 0.0f;

                    orderListAdapter.setGroupSelected(true, groupPosition, qty_to_remove);
                    subdivisionMap.remove(b);
                    subdivisionCorrispondenceMap.remove(p);
                    myButton.remove(myButton.size() - 1);
                    myProduct.remove(myProduct.size() - 1);
                    if (b.getHomage() != 1) {
                        for (int i = 0; i < orderListAdapter.getChildrenCount(groupPosition); i++) {
                            CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, i);
                            modiVal += mod.getPriceFloat() * qty_to_remove;
                            cost -= mod.getPriceFloat() * qty_to_remove;
                        }
                        cost -= (b.getPriceFloat() - b.getDiscount()) * qty_to_remove;
                        orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() + p.getPriceFloat() + modiVal - p.getDiscount());
                    }
                    if (myButton.size() != 0) {
                        String txt = String.format("%.2f", roundDecimal(orderListAdapter.getLeftCost(), 2));
                        ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                                .setText(txt.replace(".", ","));
                        showRemaingTotalContainer();
                    } else {
                        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
                        myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
                        if (((PaymentActivity) getContext()).checkIfTotalBillHasDiscount())
                            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
                        else
                            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));

                    }

                } else {
                    orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() + p.getPriceFloat());
                    orderListAdapter.setGroupSelected(true, groupPosition, 1);

                    subdivisionMap.put(b, subdivisionMap.get(b) - 1);
                    for (int i = 0; i < orderListAdapter.getChildrenCount(groupPosition); i++) {
                        CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, i);
                        cost -= mod.getPriceFloat() * 1;
                    }
                    cost -= b.getPriceFloat() * 1;
                    if (orderListAdapter.getLeftCost() != (orderListAdapter.getTotal_cost())) {
                        String txt = String.format("%.2f", roundDecimal(orderListAdapter.getLeftCost(), 2));
                        ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                                .setText(txt.replace(".", ","));
                        showRemaingTotalContainer();
                    } else {
                        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
                        myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
                        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));

                    }

                }
                return true;
            }else {

                return false;
            }
        }else{
            return false;
        }
    }

    public void removeAllFromItemCustomer(){
        while(myButton.size()!=0){
            removeLastAddSubdivisionCustomer( );
        }
    }

    public boolean removeLastAddSubdivisionCustomer( ){
        if(myButton.size()>0 && subdivisionMap.size()>0) {
            CashButtonLayout b = myButton.get(myButton.size() - 1);
            CashButtonLayout p = myProduct.get(myProduct.size() - 1);
            for(int i=myButton.size()-1; i>=0;i--){
                if(myButton.get(i).getClientPosition()==b.getClientPosition() ){
                    int groupPosition = myButton.get(i).getPosition();
                    int qty_to_remove = subdivisionMap.get(myButton.get(i));
                    float modiVal = 0.0f;
                    if(qty_to_remove==1) {

                        orderListAdapter.setGroupSelected(true, groupPosition, qty_to_remove);
                        subdivisionMap.remove(myButton.get(i));
                        subdivisionCorrispondenceMap.remove(myProduct.get(i));
                        if(myProduct.get(i).getHomage()==0) {
                            for (int j = 0; j < orderListAdapter.getChildrenCount(groupPosition); j++) {
                                CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, j);
                                modiVal += mod.getPriceFloat() * qty_to_remove;
                                cost -= mod.getPriceFloat() * qty_to_remove;
                            }
                            cost -= myButton.get(i).getPriceFloat() * qty_to_remove;
                            float left = orderListAdapter.getLeftCost();
                            float set = orderListAdapter.getLeftCost() + myProduct.get(i).getPriceFloat() + modiVal - myProduct.get(i).getDiscount();
                            orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() + myProduct.get(i).getPriceFloat() + modiVal - myProduct.get(i).getDiscount());

                        }else{
                            for (int j = 0; j < orderListAdapter.getChildrenCount(groupPosition); j++) {
                                CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, j);
                                modiVal += mod.getPriceFloat() * qty_to_remove;
                                cost -= mod.getPriceFloat() * qty_to_remove;
                            }
                            cost += myButton.get(i).getPriceFloat() * qty_to_remove;
                            float left = orderListAdapter.getLeftCost();
                            float set = orderListAdapter.getLeftCost() + myProduct.get(i).getPriceFloat() + modiVal - myProduct.get(i).getDiscount();
                        }
                        myButton.remove(i);
                        myProduct.remove(i);

                        if(myButton.size()!=0)
                        {
                            String txt = String.format("%.2f", roundDecimal(orderListAdapter.getLeftCost(), 2));
                            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                                    .setText(txt.replace(".", ","));
                            showRemaingTotalContainer();
                        }else{
                            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
                            myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
                            if(((PaymentActivity)getContext()).checkIfTotalBillHasDiscount())
                                myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
                            else
                                myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));

                        }

                    }else{

                        orderListAdapter.setGroupSelected(true, groupPosition, qty_to_remove);
                        subdivisionMap.remove(myButton.get(i));
                        subdivisionCorrispondenceMap.remove(myProduct.get(i));
                        for (int j = 0; j < orderListAdapter.getChildrenCount(groupPosition); j++) {
                            CashButtonListLayout mod = (CashButtonListLayout) orderListAdapter.getChild(groupPosition, j);
                            modiVal += mod.getPriceFloat() * qty_to_remove;
                            cost -= mod.getPriceFloat() * qty_to_remove;
                        }
                        orderListAdapter.setLeftCost(orderListAdapter.getLeftCost() + myProduct.get(i).getPriceFloat()*qty_to_remove+modiVal);
                        cost -= b.getPriceFloat() * qty_to_remove;
                        if(orderListAdapter.getLeftCost()!=(orderListAdapter.getTotal_cost()))
                        {
                            String txt = String.format("%.2f", roundDecimal(orderListAdapter.getLeftCost(), 2));
                            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                                    .setText(txt.replace(".", ","));
                            showRemaingTotalContainer();
                        }else{
                            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
                            myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
                            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));

                        }
                        myButton.remove(i);
                        myProduct.remove(i);
                        if(myButton.size()!=0)
                        {
                            String txt = String.format("%.2f", roundDecimal(orderListAdapter.getLeftCost(), 2));
                            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                                    .setText(txt.replace(".", ","));
                            showRemaingTotalContainer();
                        }else{
                            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
                            myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
                            if(((PaymentActivity)getContext()).checkIfTotalBillHasDiscount())
                                myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
                            else
                                myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));

                        }

                    }
                }else{
                    if(i!=myButton.size()-1) break;
                }
            }



            return true;
        }else{
            return false;
        }
    }

   public void setMode(int m){
        mode=m;
    }

    public ArrayList<Customer> customerAdded = new ArrayList<>();

    public void splitClientsBill() {
        int clientPos = -1;
        final int groupCount = orderList.getExpandableListAdapter().getGroupCount();
        for(int i =0; i<groupCount; i++) {
            View vParent = orderListAdapter.getGroupView(i, true, null, null);
            CashButtonLayout myTag = (CashButtonLayout) vParent.getTag();
            if(i!=clientPos && !customerAdded.contains(orderListAdapter.getCustomer(myTag.getClientPosition()))){
                clientPos = i;
                setMode(PERSON_MODE);
                ((PaymentActivity) getContext()).doSomething(vParent, i, 1);
                addCustomersMethod();
                customerAdded.add( orderListAdapter.getCustomer(myTag.getClientPosition()));
                break;
            }
            myTag.isSplit();
        }
    }

    public void addCustomersMethod(){
        if (subdivisionMap.size() > 0) {
            ((PaymentActivityCommunicator) getActivity())
                    .activateFunction(PaymentActivity.ADD_SUBDIVISION_ELEMENT, new HashMap<>(subdivisionMap), cost);
            orderListAdapter.saveQty();
            cost = 0.0f;
            subdivisionMap.clear();
            subdivisionCorrispondenceMap.clear();
            myButton.clear();
            myProduct.clear();
            HashMap<CashButtonLayout, Integer> m = orderListAdapter.getProductsLeft();
            for (CashButtonLayout p : m.keySet()
                    ) {
                if ((p.getQuantityInt() - m.get(p)) <= 0 && !sentItems.contains(p))
                    sentItems.add(p);
            }

        } else {

            Toast.makeText(getContext(), R.string.no_person_selected, Toast.LENGTH_SHORT).show();
        }
    }

    public void activateSelectionMode(int mode_code){
        mode = mode_code;
        orderListAdapter.setMode(mode);
        switch (mode){
            case DEFAULT_MODE:
                subdivisionMap.clear();
                subdivisionCorrispondenceMap.clear();
                myButton.clear();
                myProduct.clear();
                cost = 0.0f;
                break;
            case NUMBER_MODE:
                break;
            case PERSON_MODE: {
                getActivity().findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (subdivisionMap.size() > 0) {
                            ((PaymentActivityCommunicator) getActivity())
                                    .activateFunction(PaymentActivity.ADD_SUBDIVISION_ELEMENT, new HashMap<>(subdivisionMap), cost);
                            orderListAdapter.saveQty();
                            cost = 0.0f;
                            subdivisionMap.clear();
                            subdivisionCorrispondenceMap.clear();
                            myButton.clear();
                            myProduct.clear();
                            HashMap<CashButtonLayout, Integer> m = orderListAdapter.getProductsLeft();
                            for (CashButtonLayout p : m.keySet()
                                    ) {
                                if ((p.getQuantityInt() - m.get(p)) <= 0 && !sentItems.contains(p))
                                    sentItems.add(p);
                            }

                        } else {

                            Toast.makeText(getContext(), R.string.no_person_selected, Toast.LENGTH_SHORT).show();
                        }

                    }

                });
                break;
            }

            case ITEM_MODE: {
                getActivity().findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (subdivisionMap.size() > 0) {
                            ((PaymentActivityCommunicator) getActivity())
                                    .activateFunction(PaymentActivity.ADD_SUBDIVISION_ELEMENT, new HashMap<>(subdivisionMap), cost);
                            orderListAdapter.saveQty();
                            cost = 0.0f;
                            subdivisionMap.clear();
                            subdivisionCorrispondenceMap.clear();
                            myButton.clear();
                            myProduct.clear();
                            HashMap<CashButtonLayout, Integer> m = orderListAdapter.getProductsLeft();
                            for (CashButtonLayout p : m.keySet()
                                    ) {
                                if ((p.getQuantityInt() - m.get(p)) <= 0 && !sentItems.contains(p))
                                    sentItems.add(p);
                            }

                        } else {

                            Toast.makeText(getContext(), R.string.no_item_selected, Toast.LENGTH_SHORT).show();
                        }

                    }

                });
                break;
            }
            case HOMAGE_MODE :
                getActivity().findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                break;


        }
    }

    public void setPriceForPaidItem(SubdivisionItem item){
        float homage = 0.0f;
        float itemDiscount = 0.0f;
        if(item.getMode()==ITEM_MODE || item.getMode()==PERSON_MODE){

            for (CashButtonLayout prod:item.getItems()) {
                if(prod.getHomage()==1) {
                    homage+=prod.getPriceFloat();
                    ArrayList<CashButtonListLayout> mods = prod.getCashList();
                    if(mods!=null) {
                        for (CashButtonListLayout mod : mods) {
                            homage += mod.getPriceFloat() * mod.getQuantityInt();
                        }
                    }
                }
                if(prod.getDiscount()!=0.0f) {
                    itemDiscount+=prod.getDiscount();
                }
            }

        }
        if(item.getMode()!=NUMBER_MODE) {
            String txt = String.format("%.2f", roundDecimal(item.getOwed_money() - item.getDiscount() - itemDiscount - homage, 2));

            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt.replace(".", ","));
            ((PaymentActivityCommunicator) getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
        }else{
            String txt = String.format("%.2f", roundDecimal((item.getOwed_money() - item.getDiscount() - itemDiscount - homage)*item.getNumber_subdivision(), 2));

            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt.replace(".", ","));
            ((PaymentActivityCommunicator) getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);

        }
        paidOrder();
    }



    public void setItemCost(float price, float remainingCost){
        Float p;
        if(price == -1.0f){
            if(number_division_done) p = 0.0f;
            else if(percentage_division_done) p = remainingPercentageCost;
            else p = orderListAdapter.getPartial_cost();
        }
        else p = price;
        String txt = String.format("%.2f", roundDecimal(p,2) ).replace(".", ",");
        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                .setText(txt);
        ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
    }

    public void setPrice(Float price){
        Float p;
        if(price == -1.0f){
            if(number_division_done) p = 0.0f;
            else if(percentage_division_done) p = remainingPercentageCost;
            else p = orderListAdapter.getPartial_cost();
        }
        else p = price;
        String txt = String.format("%.2f", roundDecimal(p,2) ).replace(".", ",");

        if(((PaymentActivity) getContext()).getSubdivisionAdapterSize()==1) {
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
        }else{
            //da controllare se c'è discount nel total bill in subdivision adapter
            if (!((PaymentActivity) getContext()).getIfSplitBillsAreItem())
                    ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                            .setText(txt);
            else
                    ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                            .setText(txt);
        }
        ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
    }

    public void setPriceTotalBillA(float price, float price2, float itemsPrice, float discountValue, float itemsDiscount, float homage){
        if(price2-itemsPrice!=0.0f){
            //ho uno split bille di tipo amount
            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));


            float credit = ((PaymentActivity) getContext()).getCredit();
            float discount = ((PaymentActivity) getContext()).getDiscount();
            Float firstValue = roundDecimal(price - credit - discountValue - itemsDiscount - homage, 2);
            String txt = String.format("%.2f", firstValue).replace(".", ",");
            Float secondValue = roundDecimal(price-(price2-itemsPrice)-itemsDiscount-discountValue-credit-homage, 2);
            String txt1 = String.format("%.2f", secondValue).replace(".", ",");
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                    .setText(txt1);
            ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt1, 0.0f);
        }else{
            //total bill è intero
            Float firstValue = roundDecimal(price-discountValue-itemsDiscount-homage, 2);
            String txt = String.format("%.2f", firstValue).replace(".", ",");
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
            ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            RelativeLayout container1 = (RelativeLayout) myself.findViewById(R.id.first_bottom_container_1);
            container1.setVisibility(View.GONE);
            if(discountValue==0.0f ){
                RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
                container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
                RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
                discount.setVisibility(View.GONE);
                RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
                nodiscount.setVisibility(View.VISIBLE);
            }
        }
        RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
        RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
        RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
        if(discountValue==0.0f){
            discount.setVisibility(View.GONE);
            nodiscount.setVisibility(View.VISIBLE);
        }else {
            container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
            discount.setVisibility(View.VISIBLE);
            nodiscount.setVisibility(View.GONE);
        }
    }


    public void setPriceTotalBill(float price, float price2, float itemsPrice, float discountValue, float itemsDiscount){
        float myDiscount = 0.0f;
        float diffDiscount = discountValue-itemsDiscount;
        if(diffDiscount>0.0f){
            //c'è un disc
            myDiscount = discountValue;
        }else if(diffDiscount<0.0){
            myDiscount = itemsDiscount;
        }else{
            //sono uguali
            myDiscount = discountValue;
        }
        if(price2==itemsPrice){
            Float firstValue = roundDecimal(price-myDiscount, 2);
            String txt = String.format("%.2f", firstValue).replace(".", ",");
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
            ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            if(discountValue==0.0f || discountValue==itemsDiscount){
                RelativeLayout container1 = (RelativeLayout) myself.findViewById(R.id.first_bottom_container_1);
                container1.setVisibility(View.GONE);
                RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
                container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
                RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
                discount.setVisibility(View.GONE);
                RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
                nodiscount.setVisibility(View.VISIBLE);
            }
        }else{
            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
            float credit = ((PaymentActivity) getContext()).getCredit();
            float discount = ((PaymentActivity) getContext()).getDiscount();
            Float firstValue = roundDecimal(price-credit-myDiscount, 2);
            String txt = String.format("%.2f", firstValue).replace(".", ",");
            Float secondValue = roundDecimal((price)-(price2-itemsPrice)-myDiscount-credit, 2);
            String txt1= String.format("%.2f", secondValue).replace(".", ",");
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                    .setText(txt1);
            ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt1, 0.0f);
        }
        RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
        RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
        RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
        if(discountValue==0.0f){
            discount.setVisibility(View.GONE);
            nodiscount.setVisibility(View.VISIBLE);
        }else {
            if(discountValue-itemsDiscount<=0.0f){
                discount.setVisibility(View.GONE);
                nodiscount.setVisibility(View.VISIBLE);
            }else {
                container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
                discount.setVisibility(View.VISIBLE);
                nodiscount.setVisibility(View.GONE);
            }
        }
    }

    public void setPriceA(Float price, float discountValue, float itemDiscount){
        Float p;
        if(price == -1.0f){
            if(number_division_done) p = 0.0f;
            else if(percentage_division_done) p = remainingPercentageCost;
            else p = orderListAdapter.getPartial_cost();
        }
        else p = price;
        Float firstValue  = roundDecimal(p-itemDiscount, 2);
        String txt = String.format("%.2f", firstValue).replace(".", ",");

        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
        if(((PaymentActivity) getContext()).getSubdivisionAdapterSize()>1 && !((PaymentActivity)getContext()).checkIfOtherSplitBillAreItemOrPerson())
        {
            //più split bill ma di tipo item
            float remain = ((PaymentActivity) getContext()).getRemainingCostSubdivisionItem();
            Float secondValue  = roundDecimal(remain, 2);
            String txt1 = String.format("%.2f", secondValue).replace(".", ",");

            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv))
                    .setText(txt1);
        }else{
            RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.first_bottom_container_1);
            container.setVisibility(View.GONE);
        }
        ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);

        RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
        RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
        RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
        if(discountValue==0.0f){
            container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            discount.setVisibility(View.GONE);
            nodiscount.setVisibility(View.VISIBLE);
        }else {
            if(discountValue-itemDiscount>0) {
                container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
                discount.setVisibility(View.VISIBLE);
                nodiscount.setVisibility(View.GONE);
            }else{
                container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
                discount.setVisibility(View.GONE);
                nodiscount.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setNumberSplit(boolean b){
        number_division_done = b;
        String txt1 = String.format("%.2f", 0.0f);
        ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(String.valueOf(txt1));
        String txt;
        if (b) {
            txt = String.format("%.2f", 0.0f);
        }
        else{
            Float cost;
            if(percentage_division_done) cost = remainingPercentageCost;
            else cost = orderListAdapter.getPartial_cost();
            txt = String.format("%.2f", cost);
        }
        ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
    }

    /**
     * TODO THIS TWO METHODS ARE TO BE UNIFIED, BUT RIGHT NOW IT'S DIFFICULT FOR ME
     *
     * @param perc
     * @return
     */
    public Float percentageSplit(float perc){
        Float cost;
        if(((PaymentActivity)getContext()).getPercentageAmount()){
            Float a = orderListAdapter.getPartial_cost();
            float b = ((PaymentActivity) getContext()).getCreditValueAgain();
            cost = roundDecimal(returnRemaningTotal() - ((PaymentActivity) getContext()).getCreditValueAgain() , 2);
        }else {
            if (percentage_division_done) {
                remainingPercentageCost = remainingPercentageCost- ((PaymentActivity) getContext()).getActualCreditValue();
                float remaining = orderListAdapter.getPartial_cost();
                cost = roundDecimal(remainingPercentageCost - (remainingPercentageCost * perc / 100), 2);
            } else {
                Float a = orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain();
                float b = ((orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain()) * perc) / 100;
                cost = roundDecimal(((orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain()) - ((orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain()) * perc) / 100), 2);
            }
        }
        String txt = String.format("%.2f", cost).replace(".", ",");
        ((PaymentActivityCommunicator)getContext()).activateFunction(PaymentActivity.CALCULATOR_NOTIFY_COST, txt, 0.0f);
        remainingPercentageCost = cost;
        percentage_division_done = true;
        return cost;
    }

    public Float percentageSplitForAmount(float perc){
        Float cost;
        if(((PaymentActivity)getContext()).getPercentageAmount()){
            Float a = orderListAdapter.getPartial_cost();
            float b = ((PaymentActivity) getContext()).getCreditValueAgain();
            cost = roundDecimal(orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain() , 2);
        }else {
            if (percentage_division_done) {
                remainingPercentageCost = remainingPercentageCost- ((PaymentActivity) getContext()).getActualCreditValue();
                float remaining = orderListAdapter.getPartial_cost();
                cost = roundDecimal(((orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain() ) ) , 2);
            } else {
                Float a = orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain();
                float b = ((orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain()) * perc) / 100;
                cost = roundDecimal(((orderListAdapter.getPartial_cost() - ((PaymentActivity) getContext()).getCreditValueAgain() ) ) , 2);
            }
        }
        remainingPercentageCost = cost;
        percentage_division_done = true;
        return cost;
    }

    public void setSubOrderNumber(String subOrderNumber){
        StringBuilder sb = new StringBuilder("#");
        sb.append(orderNumber).append(subOrderNumber);
        o_tv.setText(sb.toString());
    }

    public void expandGroups(){
        int count = orderListAdapter.getGroupCount();
        for(int i=0; i< count; i++) orderList.expandGroup(i);
    }

    public void addSentItem(CashButtonLayout product){ sentItems.add(product);}

    public void setRemainingPercentageCost(Float remainingPercentageCost) {
        percentage_division_done = true;
        this.remainingPercentageCost = remainingPercentageCost;
    }

    public OrderListAdapter getOrderListAdapter(){ return orderListAdapter;}

    public void setTotalCostHomage1(){
        myself.findViewById(R.id.totalAmount_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Not today pretty lady", Toast.LENGTH_SHORT).show();
                if(((PaymentActivity)getContext()).getDiscountSet()) {
                    SubdivisionItem item = ((PaymentActivity) getContext()).getSelectedItem();
                    if (item == null) {
                        if (((PaymentActivity) getContext()).checkIfOtherSplitBillArePaid()) {
                            item = ((PaymentActivity) getContext()).getTotalItem();
                            setTotalHomageMethod(item);
                        } else {
                            Toast.makeText(getContext(), R.string.please_pay_other_split_bill_first, Toast.LENGTH_SHORT).show();
                        }
                    } else if (item.getMode() == -1) {
                        if (((PaymentActivity) getContext()).checkIfOtherSplitBillArePaid()) {
                            setTotalHomageMethod(item);
                        } else {
                            Toast.makeText(getContext(), R.string.please_pay_other_split_bill_first, Toast.LENGTH_SHORT).show();
                        }
                    } else if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE) {
                        int position = ((PaymentActivity) getContext()).getSelectedItemPosition(item);
                        setTotalHomageMethodForItem(item, position);

                    } else if (item.getMode() == PERCENTAGE_MODE) {
                        int position = ((PaymentActivity) getContext()).getSelectedItemPosition(item);
                        setTotalHomageMethodForItem(item, position);

                    }
                }else{
                    String myValue = ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).getText().toString();
                    if(myValue.equals("GRATIS")){

                        SubdivisionItem item = ((PaymentActivity) getContext()).getSelectedItem();
                        if (item == null) item = ((PaymentActivity) getContext()).getTotalItem();
                        if(item.getMode()==-1) setTotalHomageMethod(item);
                        else  {
                            int position = ((PaymentActivity) getContext()).getSelectedItemPosition(item);
                            setTotalHomageMethodForItem(item, position);
                            switch (item.getMode()) {
                                case PERCENTAGE_MODE :
                                    break;
                                case PERSON_MODE :
                                    ((PaymentActivity)getContext()).activatePaymentButtonsOnly();
                                    ((PaymentActivity)getContext()).hideSplitButton();
                                    ((PaymentActivity)getContext()).showAllBlueButtonExSplit();
                                    break;
                                case ITEM_MODE:
                                    ((PaymentActivity)getContext()).activatePaymentButtonsOnly();
                                    ((PaymentActivity)getContext()).hideSplitButton();
                                    ((PaymentActivity)getContext()).showAllBlueButtonExSplit();
                                    break;
                                case NUMBER_MODE:
                                    break;
                                default :
                                    break;


                            }
                        }
                    }
                }


            }
        });
    }

    public void setTotalCostHomage(){
                    SubdivisionItem item = ((PaymentActivity) getContext()).getSelectedItem();
                    if (item == null) {
                        if (((PaymentActivity) getContext()).checkIfOtherSplitBillArePaid()) {
                            item = ((PaymentActivity) getContext()).getTotalItem();
                            setTotalHomageMethod(item);
                        } else {
                            Toast.makeText(getContext(), R.string.please_pay_other_split_bill_first, Toast.LENGTH_SHORT).show();
                        }
                    } else if (item.getMode() == -1) {
                        if (((PaymentActivity) getContext()).checkIfOtherSplitBillArePaid()) {
                            setTotalHomageMethod(item);
                        } else {
                            Toast.makeText(getContext(), R.string.please_pay_other_split_bill_first, Toast.LENGTH_SHORT).show();
                        }
                    } else if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE) {
                        int position = ((PaymentActivity) getContext()).getSelectedItemPosition(item);
                        setTotalHomageMethodForItem(item, position);

                    } else if (item.getMode() == PERCENTAGE_MODE) {
                        int position = ((PaymentActivity) getContext()).getSelectedItemPosition(item);
                        setTotalHomageMethodForItem(item, position);

                    }

    }

    public void setTotalHomageMethodForItem(SubdivisionItem item, int position){
        if(!item.isHomage()) {
            //omaggio da metter
            ((PaymentActivity) getContext()).setTotalHomageForItem(position, true);

            String txt = String.format("%.2f", 0.0f);

            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(R.string.gratis_all_caps);
            ((PaymentActivity) getContext()).setCalculatorCost(txt);

            orderListAdapter.setMode(DEFAULT_MODE);
            mode = DEFAULT_MODE;

            ((PaymentActivity) getContext()).exitHomageModeForTotal();

            orderListAdapter.notifyDataSetChanged();
            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
            RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
            RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
            discount.setVisibility(View.GONE);
            nodiscount.setVisibility(View.VISIBLE);
        }else{
            //omaggio da togliere
            ((PaymentActivity) getContext()).setTotalHomageForItem(1, false);
            float itemsDiscount = 0.0f;
            ArrayList<CashButtonLayout> products = item.getItems();
            for (int i = 0; i < products.size(); i++) {
                if (!products.get(i).isSelected())
                    itemsDiscount += products.get(i).getDiscount();
            }

            float homage =  returnItemHomageForTotalBill(item);
            String txt = String.format("%.2f", item.getOwed_money()-item.getDiscount()-itemsDiscount-homage);
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
            ((PaymentActivity) getContext()).setCalculatorCost(txt);

            orderListAdapter.setMode(DEFAULT_MODE);
            mode = DEFAULT_MODE;

            ((PaymentActivity) getContext()).exitHomageMode();

            orderListAdapter.notifyDataSetChanged();

            //still to check if there are split bill
            hideRemaingTotal();

        }
    }

    /*public void setTotalHomageLabel(){
        orderListAdapter.setMode(RESERVATIONS_MODE);
        mode = RESERVATIONS_MODE;

        ((PaymentActivity) getContext()).exitHomageModeForTotal();

        orderListAdapter.notifyDataSetChanged();
        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
        RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
        RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
        discount.setVisibility(View.GONE);
        nodiscount.setVisibility(View.VISIBLE);
    }*/

    public void setTotalHomageMethod(SubdivisionItem item){
        if(!item.isHomage()) {
            //omaggio da metter
            ((PaymentActivity) getContext()).setTotalHomageForItem(1, true);
            if(StaticValue.blackbox){
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("discount", String.valueOf(total_cost)));
                params.add(new BasicNameValuePair("homage", String.valueOf(1)));
                ((PaymentActivity) getContext()).callHttpHandler("/saveHomageTotal", params);
            }else {
                float dis = dbA.getBillDiscountPrice(billId);
                if (dis == 0.0f) dbA.insertBillExtraHomage(billId, total_cost);
                else dbA.updateBillExtra(billId, 0.0f, total_cost);
            }
            total_cost = 0.0f;
            setTotalHomage(true);
            String txt = String.format("%.2f", total_cost);
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(R.string.gratis_all_caps);
            ((PaymentActivity) getContext()).setCalculatorCost(txt);

            orderListAdapter.setMode(DEFAULT_MODE);
            mode = DEFAULT_MODE;

            ((PaymentActivity) getContext()).exitHomageModeForTotal();

            orderListAdapter.notifyDataSetChanged();
            myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
            myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_2));
            RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
            RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
            discount.setVisibility(View.GONE);
            nodiscount.setVisibility(View.VISIBLE);
        }else{
            //omaggio da togliere
            ((PaymentActivity) getContext()).setTotalHomageForItem(1, false);
            float itemsDiscount = 0.0f;
            ArrayList<CashButtonLayout> products = item.getItems();
            for (int i = 0; i < products.size(); i++) {
                if (!products.get(i).isSelected()) {
                    itemsDiscount += products.get(i).getDiscount();
                }
            }
            if(StaticValue.blackbox){
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("homage", String.valueOf(0)));
                params.add(new BasicNameValuePair("discount", String.valueOf(item.getDiscount() + itemsDiscount)));
                ((PaymentActivity) getContext()).callHttpHandler("/saveHomageTotal", params);
            }else {
                dbA.updateBillExtra(billId, 0.0f, item.getDiscount() + itemsDiscount);
                dbA.updateBillExtraHomage(billId, 0);
            }
            total_cost = item.getOwed_money()-item.getDiscount()-itemsDiscount;
            setTotalHomage(false);
            float homage =  returnItemHomageForTotalBill(item);
            String txt = String.format("%.2f", item.getOwed_money()-item.getDiscount()-itemsDiscount-homage);
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                    .setText(txt);
            ((PaymentActivity) getContext()).setCalculatorCost(txt);

            orderListAdapter.setMode(DEFAULT_MODE);
            mode = DEFAULT_MODE;

            ((PaymentActivity) getContext()).exitHomageMode();

            orderListAdapter.notifyDataSetChanged();

            float left = ((PaymentActivity)getContext()).getRemainingCostSubdivisionItem();
            float itemsPrice = ((PaymentActivity)getContext()).getRemainingCostForItemAndPerson();

            setPriceTotalBillA(item.getOwed_money(), item.getOwed_money()-(left-itemsPrice) , itemsPrice, item.getDiscount(), itemsDiscount, homage);


        }
    }

    public void refreshAdapter(){
        int count = orderListAdapter.getGroupCount();
        for(int i=0; i< count; i++) orderList.expandGroup(i);
        total_cost = roundDecimal(orderListAdapter.getTotal_cost(),2);
        Float c;
        if(number_division_done) c = 0.0f;
        else if(percentage_division_done) c = remainingPercentageCost;
        else c = total_cost;
        String txt = String.format("%.2f", c).replace(".", ",");
                //.replace(",",".");
        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv))
                .setText(txt);
    }

   /* public void resetHomage(){
        Integer groupPosition = homageList.get(homageList.size()-1);
        orderListAdapter.resetGroupHomage(groupPosition);
        homageList.remove(homageList.size()-1);
        refreshAdapter();
    }*/

    public void setDiscountElementFromFragment(int groupPosition,float discountValue, boolean reset){
        orderListAdapter.setDiscountElement(groupPosition, discountValue, reset);

    }



    /**
     * DISCOUNT PART
     */

    public float getElementPrice(){
        float costToReturn = 0.0f;
        costToReturn = orderListAdapter.getElementPrice(discountList.get(discountList.size()-1));
        return costToReturn;
    }

    public float getElementDiscount(int groupPosition){
        float costToReturn = 0.0f;
        costToReturn = orderListAdapter.getElementDiscount(groupPosition);
        return costToReturn;
    }

    public float getElementsHomage(){
        float costToReturn = 0.0f;
        costToReturn = orderListAdapter.getElementsHomage();
        return costToReturn;
    }

    public float getElementsHomageForItem(SubdivisionItem item){
        float costToReturn = 0.0f;
        ArrayList<CashButtonLayout> group = item.getItems();
        for(int i=0; i< group.size(); i++){
            if(group.get(i).getHomage()==1){
                costToReturn = costToReturn+ group.get(i).getPriceFloat();
            }
        }
        return costToReturn;
    }

    public void setTotalCostDiscount(){
        int size = ((PaymentActivity)getContext()).returnSubdivisionSize();
        SubdivisionItem item = ((PaymentActivity) getContext()).returnSplitPosition();
        if(item==null) {
            if (size > 1) {
                //non sò se entro ancora qua dentro

                    myself.findViewById(R.id.first_bottom_container_1).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (((PaymentActivity) getContext()).getDiscountSet()) {

                                setPartialTotalDiscount(true);
                                setTotalDiscount(false);
                                ((PaymentActivity) getContext()).activateFunction(PARTIAL_TOTAL_DISCOUNT_MODE, "", partial_cost);


                            }
                        }
                    });

            } else {
                myself.findViewById(R.id.totalAmount_tv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((PaymentActivity) getContext()).getDiscountSet()) {

                            setPartialTotalDiscount(false);
                            setTotalDiscount(true);
                            ((PaymentActivity) getContext()).activateFunction(TOTAL_DISCOUNT_MODE, "", total_cost);
                        }else{
                            String myValue = ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).getText().toString();
                            if(myValue.equals("GRATIS")){

                                SubdivisionItem item = ((PaymentActivity) getContext()).getSelectedItem();
                                if (item == null) item = ((PaymentActivity) getContext()).getTotalItem();
                                if(item.getMode()==-1) setTotalHomageMethod(item);
                                else  {
                                    int position = ((PaymentActivity) getContext()).getSelectedItemPosition(item);
                                    setTotalHomageMethodForItem(item, position);
                                    switch (item.getMode()) {
                                        case PERCENTAGE_MODE :
                                            break;
                                        case PERSON_MODE :
                                            ((PaymentActivity)getContext()).activatePaymentButtonsOnly();
                                            ((PaymentActivity)getContext()).hideSplitButton();
                                            ((PaymentActivity)getContext()).showAllBlueButtonExSplit();
                                            break;
                                        case ITEM_MODE:
                                            ((PaymentActivity)getContext()).activatePaymentButtonsOnly();
                                            ((PaymentActivity)getContext()).hideSplitButton();
                                            ((PaymentActivity)getContext()).showAllBlueButtonExSplit();
                                            break;
                                        case NUMBER_MODE:
                                            break;
                                        default :
                                            break;


                                    }
                                }
                            }
                        }


                    }
                });
            }
        }else{
            if(item.getMode()==2 || item.getMode()==3 || item.getMode()==-1){
                myself.findViewById(R.id.totalAmount_tv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((PaymentActivity) getContext()).getDiscountSet()) {
                            setPartialTotalDiscount(false);
                            setTotalDiscount(true);
                            ((PaymentActivity) getContext()).activateFunction(TOTAL_DISCOUNT_MODE, "", total_cost);
                        }

                    }
                });
            }else if(item.getMode()==4){
                    myself.findViewById(R.id.totalAmount_tv).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (((PaymentActivity) getContext()).getDiscountSet()) {
                                setPartialTotalDiscount(false);
                                setTotalDiscount(true);
                                ((PaymentActivity) getContext()).activateFunction(TOTAL_DISCOUNT_MODE, "", total_cost);
                            }

                        }
                    });

            }
        }

    }

    public void setTotalDiscountAmount(float discountAmount, float discountValue, boolean reset, boolean fidelity){
        RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
        RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
        RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
        if(discountValue==0.0f){
            container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            discount.setVisibility(View.GONE);
            nodiscount.setVisibility(View.VISIBLE);
        }else {
            if (fidelity) {
                container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.orange));
                discount.setVisibility(View.VISIBLE);
                nodiscount.setVisibility(View.GONE);

            } else {
                container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
                discount.setVisibility(View.VISIBLE);
                nodiscount.setVisibility(View.GONE);
            }
        }
        double fidelityValue = ((PaymentActivity) getContext()).getFidelityValue();
        int customerId = ((PaymentActivity) getContext()).getFidelityCustomer();
        if(remainingPercentageCost==null) remainingPercentageCost = total_cost;
        if( Float.compare(remainingPercentageCost, total_cost)==0) {
            remainingPercentageCost = total_cost - discountAmount;
            orderListAdapter.setLeftCost(remainingPercentageCost);
            String txt = String.format("%.2f", remainingPercentageCost);//.replace(",", ".");
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);

            Float dis = dbA.getBillDiscountPrice(billId);
            if (dis != 0.0) {
                if(reset) {
                    if(discountValue==0.0){
                        if(StaticValue.blackbox){
                            float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                            params.add(new BasicNameValuePair("discount", String.valueOf(discountValue)));
                            params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                            params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                            params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                            params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                            params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                            ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                        }else {
                            dbA.updateBillExtra(billId, discountAmount, discountValue);
                        }
                        remainingPercentageCost = remainingPercentageCost + dis;
                        total_cost = remainingPercentageCost;
                    }
                    else {
                        if(StaticValue.blackbox){
                            float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                            params.add(new BasicNameValuePair("discount", String.valueOf(discountValue + dis)));
                            params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                            params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                            params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                            params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                            params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                            ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                        }else {
                            dbA.updateBillExtra(billId, discountAmount + dis, discountValue + dis);
                        }
                    }

                }else
                if(StaticValue.blackbox){
                    float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                    params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount + dis)));
                    params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                    params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                    params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                    params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                    params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                    ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                }else {
                    dbA.updateBillExtra(billId, discountAmount + dis, discountValue + dis);
                }
            } else {
                if(StaticValue.blackbox){
                    float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                    params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount)));
                    params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                    params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                    params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                    params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                    params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                    ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                }else {
                    dbA.insertBillExtra(billId, discountAmount, discountAmount);
                }
            }

        }else {
            //this chech if i am resetting total discount
            if (reset && discountAmount == 0.0f) {
                    float discountElements = ((PaymentActivity) getContext()).getDiscountForItem();
                    remainingPercentageCost = total_cost - discountElements - ((PaymentActivity) getContext()).getHomageForItem();
            } else {
                    remainingPercentageCost = remainingPercentageCost - discountAmount;
            }
            orderListAdapter.setLeftCost(remainingPercentageCost);
              String txt = String.format("%.2f", remainingPercentageCost- ((PaymentActivity)getContext()).getCredit());//.replace(",", ".");
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
            Float dis = dbA.getBillDiscountPrice(billId);
            if (dis != 0.0) {
                float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
                if(dis>discountElements)
                    if(StaticValue.blackbox){
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                        params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount+dis)));
                        params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                        params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                        params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                        params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                        params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                        ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                    }else {
                        dbA.updateBillExtra(billId, discountAmount + (dis), discountAmount + (dis));
                    }
                else
                if(StaticValue.blackbox){
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                    params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount+dis)));
                    params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                    params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                    params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                    params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                    params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                    ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                }else {
                    dbA.updateBillExtra(billId, discountAmount + (dis), discountAmount + (dis));

                }

            } else {
                if(StaticValue.blackbox){
                    float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                    params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount)));
                    params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                    params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                    params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                    params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                    params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                    ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                }else {
                    dbA.insertBillExtra(billId, discountAmount, discountValue);
                }
            }
            float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
            if(discountAmount==0.0f && reset) {
                if(StaticValue.blackbox){
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                    params.add(new BasicNameValuePair("discount", String.valueOf(discountAmount+discountElements)));
                    params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
                    params.add(new BasicNameValuePair("fidelity", String.valueOf(fidelity)));
                    params.add(new BasicNameValuePair("reset", String.valueOf(reset)));
                    params.add(new BasicNameValuePair("fidelityValue", String.valueOf(fidelityValue)));
                    params.add(new BasicNameValuePair("customerId", String.valueOf(customerId)));
                    ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
                }else {
                    dbA.updateBillExtra(billId, discountAmount + discountElements, discountValue + discountElements);
                }
            }
        }
    }

    public void setPartialTotalDiscountAmount(float discountAmount, float discountValue){
        float a = 0.0f;
        if(remainingPercentageCost!=null) a =remainingPercentageCost;
        if(remainingPercentageCost==null) remainingPercentageCost = total_cost;
        remainingPercentageCost = remainingPercentageCost-discountAmount;
        partial_cost = remainingPercentageCost;
        float creditValue = ((PaymentActivity) getContext()).getActualCreditValue();
        remainingPercentageCost = remainingPercentageCost-creditValue;
        orderListAdapter.setLeftCost(partial_cost);
        String txt = String.format("%.2f", partial_cost);
        if( ((PaymentActivity)getContext()).returnSubdivisionSize()>1)
        ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(txt);
        else
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
        if(StaticValue.blackbox){
            float discountElements = ((PaymentActivity)getContext()).getDiscountForItem();
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            params.add(new BasicNameValuePair("discount", String.valueOf(discountValue)));
            params.add(new BasicNameValuePair("discountElement", String.valueOf(discountElements)));
            params.add(new BasicNameValuePair("reset", String.valueOf(false)));
            params.add(new BasicNameValuePair("fidelity", String.valueOf(false)));
            ((PaymentActivity) getContext()).callHttpHandler("/saveDiscountTotal", params);
        }else {
            Float dis = dbA.getBillDiscountPrice(billId);
            if (dis != 0.0) {
                dbA.updateBillExtra(billId, discountAmount + dis, discountValue + dis);
            } else dbA.insertBillExtra(billId, discountAmount, discountValue);
        }
        discountSet = false;
    }

    public void setModifyPartialTotalDiscountAmount(float discountAmount, float discountValue){
        if(remainingPercentageCost==null) remainingPercentageCost = total_cost;
        remainingPercentageCost = remainingPercentageCost-discountValue;
        partial_cost = remainingPercentageCost;
        float creditValue = ((PaymentActivity) getContext()).getActualCreditValue();
        remainingPercentageCost = remainingPercentageCost-creditValue;
        orderListAdapter.setLeftCost(partial_cost);
        String txt = String.format("%.2f", partial_cost);//.replace(",",".");
        if( ((PaymentActivity)getContext()).returnSubdivisionSize()>1)
            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(txt);
        else
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
        Float dis = dbA.getBillDiscountPrice(billId);
        if(dis!=0.0){
            dbA.updateBillExtra(billId, dis+discountValue, dis+discountValue);
        }else dbA.deleteDiscuntTotal(billId);
        discountSet = false;

    }

    public void setNewPartialTotalDiscountAmount(float discountAmount, float discountValue){
        if(remainingPercentageCost==null) remainingPercentageCost = total_cost;
        if(discountValue>=discountAmount)
            remainingPercentageCost = remainingPercentageCost+discountValue-discountAmount;
        else
            remainingPercentageCost = remainingPercentageCost+discountAmount-discountValue;
        partial_cost = remainingPercentageCost;
        float creditValue = ((PaymentActivity) getContext()).getActualCreditValue();
        remainingPercentageCost = remainingPercentageCost-creditValue;
        orderListAdapter.setLeftCost(partial_cost);
        String txt = String.format("%.2f", partial_cost);//.replace(",",".");
        if( ((PaymentActivity)getContext()).returnSubdivisionSize()>1)
            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(txt);
        else
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
        Float dis = dbA.getBillDiscountPrice(billId);
        if(dis!=0.0){
            dbA.updateBillExtra(billId, dis+discountAmount, discountValue);
        }else dbA.deleteDiscuntTotal(billId);
        discountSet = false;

    }

    public float returnItemHomageForTotalBill(SubdivisionItem item){
        float homage = 0.0f;
        ArrayList<CashButtonLayout> products = item.getItems();
        for(int i=0; i<products.size(); i++){
            if(!products.get(i).isSelected()) {
                if (products.get(i).getHomage() == 1) {
                    homage += products.get(i).getPriceFloat();
                    ArrayList<CashButtonListLayout> modifiers = products.get(i).getCashList();
                    if (modifiers != null) {
                        for (int j = 0; j < modifiers.size(); j++) {
                            homage += modifiers.get(j).getPriceFloat() * modifiers.get(j).getQuantityInt();
                        }
                    }
                }
            }
        }
        return homage;
    }

    public void resetPartialTotalDiscountAmount(float discountAmount, float discountValue){
        if(remainingPercentageCost==null) remainingPercentageCost = total_cost;
        if(remainingPercentageCost!=total_cost)
            remainingPercentageCost = remainingPercentageCost+discountAmount;
        partial_cost = remainingPercentageCost;
        float creditValue = ((PaymentActivity) getContext()).getActualCreditValue();
        remainingPercentageCost = remainingPercentageCost-creditValue;
        orderListAdapter.setLeftCost(partial_cost);
        String txt = String.format("%.2f", partial_cost);//.replace(",",".");
        if( ((PaymentActivity)getContext()).returnSubdivisionSize()>1)
            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(txt);
        else
            ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
        Float dis = dbA.getBillDiscountPrice(billId);
        if(dis-discountAmount!=0.0){
            dbA.updateBillExtra(billId, dis-discountAmount, dis-discountAmount);
        }else dbA.deleteDiscuntTotal(billId);
        discountSet = false;

    }

    public void setItemDiscountAmount(SubdivisionItem item ,float discountAmount, float discountValue, boolean reset, int groupPosition, boolean fidelity){
        float oldDiscount = item.getDiscount();
        float itemsDiscount = 0.0f;
        if(item.getMode()==2 || item.getMode()==3){
            ArrayList<CashButtonLayout> products = item.getItems();
            for(int i=0; i<products.size(); i++){
                itemsDiscount+= products.get(i).getDiscount();
            }
        }

        boolean tocheck = false;

        //dio cane che schifo che è diventato questo pezzo
        if(groupPosition==-1) {
            tocheck = true;
            if (reset && discountValue == 0.0f)
                item.setDiscount(discountValue);
            else item.setDiscount(discountValue + oldDiscount);
        }else{
            if (reset && discountValue == 0.0f) {
                ArrayList<CashButtonLayout> products = item.getItems();
              products.get(groupPosition).setHomage(0);
                products.get(groupPosition).setDiscount(discountValue);
                item.setItems(products);
            }else {

            }

        }
        if(fidelity){
            if (reset && discountValue == 0.0f && groupPosition==-1)
                item.setFidelity(0.0);
            else if( groupPosition==-1)
                item.updateFidelity(discountValue);
        }

        float price = 0.0f;
        if (reset && Float.compare(discountValue ,0.0f)==0) {
            if(item.getMode()==-1 ) {
                float remain = ((PaymentActivity) getContext()).getRemainingForTotal();
                if (remain != item.getOwed_money() &&
                        !((PaymentActivity) getContext()).checkIfOtherSplitBillAreItemOrPerson()
                        ) {
                    myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
                    myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));

                    //float remaing= total_cost;
                    if (item.getMode() == -1) {
                        ArrayList<CashButtonLayout> products = item.getItems();
                        for (int i = 0; i < products.size(); i++) {
                            if (!products.get(i).isSelected() && products.get(i).getPosition() != groupPosition)
                                itemsDiscount += products.get(i).getDiscount();
                        }
                    }
                    float homage = returnItemHomageForTotalBill(item);
                    String txt = String.format("%.2f", item.getOwed_money() - item.getDiscount() - remain-homage/*-discountAmount*/ - itemsDiscount);
                    ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(String.valueOf(txt));
                    price = item.getOwed_money() - item.getDiscount()-itemsDiscount;
                    remainingPercentageCost = price + discountAmount-homage;
                    Float secondValue = roundDecimal(price-homage/*-remain*/, 2);
                    String txt1 = String.format("%.2f", secondValue).replace(".", ",");

                    ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt1);
                } else {
                    ArrayList<CashButtonLayout> products = item.getItems();
                    for (int i = 0; i < products.size(); i++) {
                        if(!products.get(i).isSelected())
                        itemsDiscount += products.get(i).getDiscount();
                    }
                    price = item.getOwed_money()-item.getDiscount()-itemsDiscount;// - (oldDiscount - discountAmount);//-item.getDiscount();
                    Float firstValue = roundDecimal(price, 2);
                    String txt = String.format("%.2f", firstValue).replace(".", ",");

                    ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
                }

            }else if(item.getMode()==ITEM_MODE || item.getMode()==PERSON_MODE){
                itemsDiscount = 0.0f;
                ArrayList<CashButtonLayout> products = item.getItems();
                for (int i = 0; i < products.size(); i++) {
                    if(i!=groupPosition)
                    itemsDiscount += products.get(i).getDiscount();
                }
                price = item.getOwed_money()-item.getDiscount()-itemsDiscount;// - (oldDiscount - discountAmount);//-item.getDiscount();
                Float firstValue = roundDecimal(price, 2);
                String txt = String.format("%.2f", firstValue).replace(".", ",");

                ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
            }else {
                price = item.getOwed_money();// - (oldDiscount - discountAmount);//-item.getDiscount();
                Float firstValue = roundDecimal(price, 2);
                String txt = String.format("%.2f", firstValue).replace(".", ",");
                ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
            }
        }else{
            if(item.getMode()==ITEM_MODE || item.getMode()==PERSON_MODE){
               if(!reset) {
                   itemsDiscount = 0.0f;
                   ArrayList<CashButtonLayout> products = item.getItems();
                   for (int i = 0; i < products.size(); i++) {
                       if (products.get(i).getPosition() != groupPosition)
                           itemsDiscount += products.get(i).getDiscount();
                   }
                   price = item.getOwed_money() - oldDiscount - discountValue /*-discountAmount*/- itemsDiscount/*- item.getDiscount()*/;
                   Float firstValue = roundDecimal(price, 2);
                   String txt = String.format("%.2f", firstValue).replace(".", ",");

                   ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
               }else{
                   itemsDiscount = 0.0f;
                   ArrayList<CashButtonLayout> products = item.getItems();
                   for (int i = 0; i < products.size(); i++) {
                           itemsDiscount += products.get(i).getDiscount();
                   }
                   price = item.getOwed_money() - oldDiscount /*- discountValue*/ /*-discountAmount*/- itemsDiscount/*- item.getDiscount()*/;
                   Float firstValue = roundDecimal(price, 2);
                   String txt = String.format("%.2f", firstValue).replace(".", ",");

                   ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
               }
            }else {
                float remain = ((PaymentActivity) getContext()).getRemainingForTotal();
                if(item.getMode()==-1 ){
                    if (!((PaymentActivity) getContext()).checkIfOtherSplitBillAreItemOrPerson()) {
                        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.VISIBLE);
                        myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
                        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));

                        //float remaing= total_cost;
                        if (item.getMode() == -1) {
                            ArrayList<CashButtonLayout> products = item.getItems();
                            for (int i = 0; i < products.size(); i++) {
                                if (!products.get(i).isSelected() && products.get(i).getPosition() != groupPosition)
                                    itemsDiscount += products.get(i).getDiscount();
                            }
                        }
                        float homage = returnItemHomageForTotalBill(item);
                        if (tocheck) {
                            String txt = String.format("%.2f", item.getOwed_money() - item.getDiscount() - remain-homage/*-discountAmount*/ - itemsDiscount);
                            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(String.valueOf(txt));
                        } else {
                            String txt = String.format("%.2f", item.getOwed_money() - item.getDiscount() - remain - discountAmount - itemsDiscount-homage);
                            ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(String.valueOf(txt));

                        }
                        price = item.getOwed_money() - item.getDiscount()-itemsDiscount;
                        remainingPercentageCost = price + discountAmount-homage;
                        Float secondValue  = roundDecimal(price-homage/*-remain*/, 2);
                        String txt1 = String.format("%.2f", secondValue).replace(".", ",");

                        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt1);
                    }else{
                        if (item.getMode() == -1) {
                            ArrayList<CashButtonLayout> products = item.getItems();
                            for (int i = 0; i < products.size(); i++) {
                                if (!products.get(i).isSelected() && products.get(i).getPosition() != groupPosition)
                                    itemsDiscount += products.get(i).getDiscount();
                            }
                        }

                        if (item.getMode() == 2 || item.getMode() == 3) {
                            itemsDiscount = 0.0f;
                            ArrayList<CashButtonLayout> products = item.getItems();
                            for (int i = 0; i < products.size(); i++) {
                                if (products.get(i).getPosition() != groupPosition)
                                    itemsDiscount += products.get(i).getDiscount();
                            }
                        }

                        price = item.getOwed_money() - oldDiscount - discountValue - itemsDiscount/*- item.getDiscount()*/;
                        remainingPercentageCost = item.getOwed_money()-oldDiscount-itemsDiscount;
                        Float firstValue = roundDecimal(price, 2);
                        String txt = String.format("%.2f", firstValue).replace(".", ",");

                        ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
                    }
                }else if(item.getMode()==PERSON_MODE|| item.getMode()==ITEM_MODE || item.getMode()==PERCENTAGE_MODE){
                    if (item.getMode() == -1) {
                        ArrayList<CashButtonLayout> products = item.getItems();
                        for (int i = 0; i < products.size(); i++) {
                            if (!products.get(i).isSelected() && products.get(i).getPosition() != groupPosition)
                                itemsDiscount += products.get(i).getDiscount();
                        }
                    }

                    if (item.getMode() == 2 || item.getMode() == 3) {
                        itemsDiscount = 0.0f;
                        ArrayList<CashButtonLayout> products = item.getItems();
                        for (int i = 0; i < products.size(); i++) {
                            if (products.get(i).getPosition() != groupPosition)
                                itemsDiscount += products.get(i).getDiscount();
                        }
                    }

                    price = item.getOwed_money() - oldDiscount - discountValue - itemsDiscount/*- item.getDiscount()*/;
                    remainingPercentageCost = item.getOwed_money()-oldDiscount-itemsDiscount;
                    Float firstValue = roundDecimal(price, 2);
                    String txt = String.format("%.2f", firstValue).replace(".", ",");

                    ((CustomTextView) myself.findViewById(R.id.totalAmount_tv)).setText(txt);
                }
            }
        }

    }


    /**
     * Used to restore values from the splits removed.
     * @param item: the bill-split item removed
     * @param subdivisionAdapter
     */
    public void restoreFromRemovedItem(SubdivisionItem item, SubdivisionAdapter subdivisionAdapter){
        switch(item.getMode()){
            case PERCENTAGE_MODE:
                if(subdivisionAdapter.getPerc_split_number()<=0) {
                    // if no more perc splits, reset the boolean to false
                    percentage_division_done = false;
                    ((PaymentActivity)getContext()).setPercentageSplit(false);
                }
                else remainingPercentageCost += item.getOwed_money();
                setPrice(-1.0f);
                break;
            case NUMBER_MODE:
                number_division_done = false;
                ((PaymentActivity)getContext()).setNumberSplit(false);
                setPrice(-1.0f);
                break;
            case ITEM_MODE :
                for (CashButtonLayout product: item.getItems()
                        ) {
                    for (CashButtonLayout sent: sentItems
                            ) {
                        if(product.getID() == sent.getID()){
                            sentItems.remove(sent);
                            break;
                        }
                    }
                }
                if(percentage_division_done) remainingPercentageCost+=item.getOwed_money()+item.getDiscount();

                orderListAdapter.restoreFromItemSplit(item, subdivisionAdapter.getItemCount()!=0);
                setPrice(-1.0f);
                break;
            case PERSON_MODE:
                for (CashButtonLayout product: item.getItems()
                        ) {
                    for (CashButtonLayout sent: sentItems
                            ) {
                        if(product.getID() == sent.getID()){
                            sentItems.remove(sent);
                            break;
                        }
                    }
                    if(product.getClientPosition()>0) {
                        Customer customer = orderListAdapter.getCustomer(product.getClientPosition());
                        if (customerAdded.contains(customer)) {
                            customerAdded.remove(customer);
                        }
                    }
                }


                if(percentage_division_done) remainingPercentageCost+=item.getOwed_money()+item.getDiscount();
                orderListAdapter.restoreFromItemSplit(item, subdivisionAdapter.getItemCount()!=0);
                setPrice(-1.0f);
                break;
            default:

                break;
        }
    }



    public void hideRemaingTotal(){
        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
        myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
    }

    public void hideRemainingWithDiscount() {
        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.GONE);
        RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
        RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
        RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
        container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
        discount.setVisibility(View.VISIBLE);
        nodiscount.setVisibility(View.GONE);

    }

    public void showRemaingTotalContainer(){
        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));
    }

    public void showDiscountContainer(){
        RelativeLayout container = (RelativeLayout) myself.findViewById(R.id.second_bottom_container);
        RelativeLayout discount = (RelativeLayout) myself.findViewById(R.id.euro_icon_discount);
        RelativeLayout nodiscount = (RelativeLayout) myself.findViewById(R.id.euro_icon_no_discount);
        container.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.eletric_blue));
        discount.setVisibility(View.VISIBLE);
        nodiscount.setVisibility(View.GONE);
    }


    public void showRemaingTotal(){
        myself.findViewById(R.id.first_bottom_container_1).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.first_bottom_container_1).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        myself.findViewById(R.id.second_bottom_container).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray_2));

        float remaing = returnRemaningTotal();
        String txt = String.format("%.2f", remaing);
        ((CustomTextView) myself.findViewById(R.id.leftAmount_tv)).setText(String.valueOf(txt));

         ((PaymentActivity) getContext()).setCalculatorCost(txt);
    }

}
