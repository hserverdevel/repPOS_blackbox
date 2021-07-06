package com.example.blackbox.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.fragments.CalculatorFragment;
import com.example.blackbox.fragments.OrderFragment;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.example.blackbox.model.TotalBill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.widget.RelativeLayout.GONE;
import static android.widget.RelativeLayout.OnClickListener;
import static android.widget.RelativeLayout.VISIBLE;
import static com.example.blackbox.activities.PaymentActivity.PAY_PARTIAL_BILL;
import static com.example.blackbox.adapter.OrderListAdapter.ITEM_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.NUMBER_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PARTIAL_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERCENTAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERSON_MODE;
import static com.example.blackbox.fragments.CalculatorFragment.roundDecimal;

/**
 * Created by DavideLiberato on 13/07/2017.
 */

/**
 * NUMBER MODE = 1
 * ITEM MODE = 2
 * PERSON MODE = 3
 * PERECENTAGE MODE = 4
 */

public class SubdivisionAdapter extends RecyclerView.Adapter {

    private final float density;
    private Map<String, ArrayList<CashButtonLayout>> map;
    private Map<String, Float> costs_map;
    private ArrayList<String> element_titles;
    private ArrayList<SubdivisionItem> items;
    private LayoutInflater inflater;
    private RecyclerView parent;
    private OrderFragment of;
    private SubdivisionItem total_bill;
    private SubdivisionItem split_opened = null;
    private Integer split_opened_position = -1;
    private Context context;
    private Float total_subdivisions_cost = 0.0f;
    private Float total_subdivisions_paid = 0.0f;
    private int perc_split_number = 0;  // counter for percentage subdivisions
    private int mode;


    public SubdivisionAdapter(Context context, OrderFragment orderFragment)
    {
        this.context = context;
        of = orderFragment;
        element_titles = new ArrayList<>();
        map = new HashMap<>();
        costs_map = new HashMap<>();
        items = new ArrayList<>();
        if(!StaticValue.blackbox) {
            SubdivisionItem item = new SubdivisionItem();
            item.setItems_map(new HashMap<>(of.getOrderListAdapter().getProductsLeft()));
            item.setItems(new ArrayList<>(item.getItems_map().keySet()));
            item.setMode(-1);
            Float thiscost = of.getOrderListAdapter().getTotal_cost();
            item.setOwed_money(of.getOrderListAdapter().getTotal_cost());
            item.setPaid(((PaymentActivity) context).returnTotalIsPaid());
            item.setDiscount(((PaymentActivity) context).returnTotalBillDiscount());
            this.total_bill = item;
            items.add(total_bill);
        }
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
    }




    public void setTotalSubdivisionPaid(float c){
        total_subdivisions_paid += c;
    }

    public void setTotalSubdivisionCostForHomage(float c, boolean add){
        if(add)
            total_subdivisions_cost += c;
        else
            total_subdivisions_cost -= c;
    }

    public void setItemPaid(SubdivisionItem item){
        int position = items.indexOf(item);
        items.get(position).setPaid(true);
    }

    public void setSubdivisionItemFromServer(TotalBill billTotal, double discount)
    {
        SubdivisionItem item = new SubdivisionItem();
        item.setItems_map(new HashMap<>(of.getOrderListAdapter().getProductsLeft()));
        item.setItems(new ArrayList<>(item.getItems_map().keySet()));
        item.setMode(-1);
        Float thiscost = of.getOrderListAdapter().getTotal_cost();
        item.setOwed_money(of.getOrderListAdapter().getTotal_cost());
        item.setPaid(((PaymentActivity) context).returnTotalIsPaid());
        //item.setDiscount(((PaymentActivity) context).returnTotalBillDiscount());
        item.setDiscount((float)discount);
        if(billTotal.getPaid()==1)
            item.setPaid(true);
        this.total_bill = item;
        items.add(total_bill);
    }

    public void returnPercOfVat(int position, int vat){
        float total = items.get(0).getOwed_money();
        SubdivisionItem item = items.get(position);
        ArrayList<CashButtonLayout> products = item.getItems();
        for(CashButtonLayout product : products){
            if(!product.isSelected()){
                if(product.getVat()==vat){

                }
                ArrayList<CashButtonListLayout> modifiers = product.getCashList();
                if(modifiers!=null){

                }
            }

        }

    }
    public void setTotalItemPayment(int payment){
        total_bill.setPaymentType(payment);
        items.get(0).setPaymentType(payment);
        notifyDataSetChanged();
    }

    public void setTotalBill(Float c) {
        total_bill.setOwed_money(c);
    }

    public void setTotalBillItemsForHomage(ArrayList<CashButtonLayout> products) {
        total_bill.setItems(products);
        items.get(0).setItems(products);
        HashMap<CashButtonLayout, Integer> map = new HashMap<CashButtonLayout, Integer>();
        for(CashButtonLayout product : products){
            map.put(product, 0);
        }
        total_bill.setItems_map(map);
        items.get(0).setItems_map(map);

    }

    public void setItemProduct(ArrayList<CashButtonLayout> products) {
        SubdivisionItem sItem = getSelectedItem();
        sItem.setItems(products);
        for(SubdivisionItem item : items){
            if(item==sItem)
                item.setItems(products);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.element_subdivision, null);
        SubdivisionElementHolder seh = new SubdivisionElementHolder(v);
        return seh;
    }

    public SubdivisionItem getTotalItem(){
        return items.get(0);
    }

    public boolean getItemsSize(){
        if(items.size()>1)
            return true;
        else return false;
    }

    public SubdivisionItem getFirstItemAvaiable(){
        SubdivisionItem returnItem = new SubdivisionItem();
        for(SubdivisionItem item : items){
            if(!item.isPaid() && item.getMode()!=-1) {
                split_opened = item;
                returnItem = item;
                break;
            }
        }
        return returnItem;
    }

    public void resetIsShow(){
        SubdivisionItem returnItem = new SubdivisionItem();
        for(int i=1; i<items.size(); i++){
            items.get(i).setIsShow(true);

        }

    }

    public void showFirstItemAvaiable(){
        SubdivisionItem returnItem = new SubdivisionItem();
        for(int i=1; i<items.size(); i++){
            if(!items.get(i).isPaid() && items.get(i).getMode()!=-1) {
                showItem(i+1);
                items.get(i).setIsShow(false);
                notifyDataSetChanged();
                break;
            }
        }

    }

    public int getFirstItemAvaiablePosition(){
        int myposition = 1;
        for(int i=1; i< items.size(); i++){
            if(!items.get(i).isPaid()) {
                myposition = i+1;
                break;
            }
        }
        return myposition;
    }

    public boolean checkIfTotalHasDiscount(){
        float dis = returnDiscountForItemForTotalBill(items.get(0).getItems());
        if(items.get(0).getDiscount()!=0.0f) return true;
        else return false;
    }

    public float actualPrice = 0.0f;



    public void resetDiscount(SubdivisionItem item, float discountValue){
        for(int i=0; i<items.size(); i++){
            if(item==items.get(i)){
                if(items.get(i).getDiscount()>0.0f) {
                    items.get(i).setDiscount(items.get(i).getDiscount() - discountValue);
                    split_opened.setDiscount(split_opened.getDiscount() - discountValue);
                }
            }
        }
    }

    public void notifyChange(){
        notifyDataSetChanged();
    }

    /**
     * bind to items array
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final SubdivisionElementHolder seh = (SubdivisionElementHolder)holder;
        final SubdivisionItem item = items.get(position);
        //if size = 1 the theri only the total bil so do not show

        if (items.size()>1)
        {
            //set visible or total bill won't be showed because onfirst time vwe hide it
            seh.view.setVisibility(VISIBLE);

            //if item is per number split show  quantity x price on vignetta, else just show price
            if (item.getMode()== 1 ) {
                seh.amount_tv.setText(item.getNumber_subdivision() + " x " + CalculatorFragment.roundDecimal(item.getOwed_money(), 2).toString().replace(".", ","));
            }else {
                String txt = String.format("%.2f", CalculatorFragment.roundDecimal(item.getOwed_money(),2) );
                       // .replaceAll(",", ".");
                seh.amount_tv.setText(txt.replace(".", ","));
                //seh.amount_tv.setText(CalculatorFragment.roundDecimal(item.getOwed_money()/*-item.getDiscount()*/, 2).toString());
            }

            //set mode string on vignetta eg : total bill, item, per number, per item
            seh.mode_tv.setText(item.getModeString());

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (154 * density), (int) (95 * density));
            if (position > 0) {
                //set margin , if total bill has non more element set it to 0, so you can hide it (see below)
                if(position==1 && total_bill.getOwed_money()==0.0f)
                    lp.leftMargin = (int) (0 * density);
                else
                    lp.leftMargin = (int) (14 * density);
            }
            seh.view.setLayoutParams(lp);

            //if item is paid set background color to green using setActivated, else set correct display for selected split
            if(item.isPaid()){
                seh.view.setSelected(false);
                seh.view.setActivated(item.isPaid());
            }else {
                if (!item.getIsShow()) {
                    seh.view.setSelected(true);
                    seh.view.setActivated(false);
                } else {
                    seh.view.setSelected(false);
                    seh.view.setActivated(item.isPaid());
                }
            }

            //set splid number on vignetta, if original set "ORIGINAL"
            if(item==total_bill){
                seh.split_id.setText(R.string.original_all_caps);
                if(!item.getIsShow()) {
                    of.setSubOrderNumber("");
                }
                //if total bill has no left element hide it
                if(item.getOwed_money()==0.0f){
                    seh.subdivision_container.setVisibility(GONE);
                    seh.view.setLayoutParams(new LinearLayout.LayoutParams((int) (0*density), (int) (0*density)));

                }else{
                    seh.view.setVisibility(VISIBLE);
                    seh.view.setLayoutParams(new LinearLayout.LayoutParams((int) (154*density), (int) (95*density)));
                }
            }else{
                seh.split_id.setText(String.format("#%02d", position));
                if(item.getMode()!=PERSON_MODE)
                    seh.split_id.setText(String.format("#%02d", position));
                else {
                    int clientPosition = item.getItems().get(0).getClientPosition();
                    if(clientPosition>0) {
                        Log.i("CLIENT POSITION", "" + clientPosition);
                        Customer customer = ((PaymentActivity) context).getCustomer(clientPosition);
                        seh.split_id.setText(customer.getDescription());
                    }
                    //of.setSubOrderNumber(" / " + position);
                }

                if(!item.getIsShow()) {
                    of.setSubOrderNumber(" / " + position);

                }
            }

            //set on click on vignetta
            seh.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((PaymentActivity) context).checkIfButtonActivated() && !((PaymentActivity) context).isCalculatorOn && !v.isActivated() && !((PaymentActivity)context).getDiscountSet() && !((PaymentActivity)context).splitItemSet) {
                            // if position ==0   show original bill



                        if (position == 0) {
                                performClickOnTotal();
                                seh.view.setSelected(true);

                            } else {
                                //we are selecting an itemo from items that isn't total bill

                                for (int x=0; x<items.size(); x++) items.get(x).setIsShow(true);
                                item.setIsShow(false);

                                //show bill for amount and number
                                if (item.getMode() == 4 || item.getMode() == 1) {
                                    of.getOrderListAdapter().showPartialSplitBill(item);
                                } else {
                                    //show for item
                                    of.getOrderListAdapter().showSplitBill(item.getItems_map(), item);

                                }

                                //set green and blue button for different item
                                if(item.getMode()==1) {
                                    //number
                                    of.setPriceA(item.getOwed_money()*item.getNumber_subdivision() , 0.0f, 0.0f);
                                    of.hideRemaingTotal();
                                    if(items.size()==2){
                                        if(getSelectedItem()!=null)
                                            if(getSelectedItem()!=item)
                                                ((PaymentActivity)context).hideButtonPerNumber();
                                    }else{
                                        if(item.getMode()==NUMBER_MODE){
                                            ((PaymentActivity) context).hidePaymentButton();
                                            ((PaymentActivity) context).hidenBlueButtonExPrint();
                                        }else {
                                            ((PaymentActivity) context).resetButtonPerNumberGreen();
                                            ((PaymentActivity) context).activatePaymentButtonsOnly();
                                            ((PaymentActivity) context).hideBlueButton();
                                        }
                                    }
                                }else if(item.getMode()==4 || item.getMode()==2 || item.getMode()==3){
                                    //aount and item
                                    ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>(item.getItems());
                                    float itemDiscount = 0.0f;
                                    for (CashButtonLayout prod:myProducts) {
                                        if(prod.getDiscount()!=0.0f) {
                                            itemDiscount+=prod.getDiscount();
                                        }

                                    }
                                    of.setPriceA(item.getOwed_money()-item.getDiscount(), item.getDiscount(), itemDiscount);
                                    ((PaymentActivity)context).resetButtonPerNumberGreen();
                                    ((PaymentActivity)context).activatePaymentButtonsOnly();
                                    ((PaymentActivity)context).showAllBlueButton();
                                    ((PaymentActivity)context).showAllBlueButtonExSplit();
                                    //((PaymentActivity)context).hideBlueButton();
                                }
                                else {
                                    ((PaymentActivity)context).resetButtonPerNumberBlue();
                                    ((PaymentActivity)context).resetButtonPerNumberGreen();
                                    ((PaymentActivity)context).activatePaymentButtonsOnly();
                                    of.setPriceA(item.getOwed_money()-item.getDiscount() , item.getDiscount(), 0.0f);
                                }
                                of.setSubOrderNumber(" / " + seh.split_id.getText().toString().substring(1));

                                split_opened_position = position;
                                split_opened = item;
                                seh.view.setSelected(true);
                                ((PaymentActivity)context).buttonOpacitySetting1();
                                ((PaymentActivity)context).setOptionButton(false);
                                if(item.getMode()==4 || item.getMode()==2) {
                                    float dis = returnDiscountForItem(item.getItems());
                                    if(item.getDiscount()/*-dis*/!=0)
                                        ((PaymentActivity) context).hidePartialWithDiscount();
                                     else
                                        ((PaymentActivity) context).hidePartial();
                                }
                                of.setDescriptionItemSplit(item.getMode());
                            }
                            of.getOrderListAdapter().notifyDataSetChanged();

                        notifyDataSetChanged();
                    }else{
                        if(item.isPaid()){
                            if (item.getMode() == 4 || item.getMode() == 1 || item.getMode() == 15) {
                                of.getOrderListAdapter().showPartialSplitBill(item);
                            } else {
                                //show for item
                                of.getOrderListAdapter().showSplitBill(item.getItems_map(), item);

                            }
                            of.setPriceForPaidItem(item);
                            ((PaymentActivity)context).hideBlueButton();
                            ((PaymentActivity)context).buttonOpacitySetting1();
                            ((PaymentActivity)context).hidePaymentButton();
                            split_opened_position = position;
                            split_opened = item;
                        }
                    }
                }
            });
        }else{
            //hiding total bill
            /**
             if(items.size()!=1)
             of.setSubOrderNumber(" / " + position);
             else  of.setSubOrderNumber("");
             of.setPriceA(item.getOwed_money() );
             */
            ((PaymentActivity)context).setPay_mode(101);
            seh.view.setVisibility(GONE);
        }
    }

    public float returnPaidValue(){
        float value = 0.0f;
        for(int i=0; i<items.size();i++){
            if(items.get(0).isPaid()) value += items.get(i).getOwed_money();
        }
        return value;
    }

    public float returnUnpaidValue(){
        float value = 0.0f;
        for(int i=1; i<items.size();i++){
            if(!items.get(i).isPaid()) value += items.get(i).getOwed_money();
        }
        return value;
    }

    public double returnDouble(float f){
        double d = f;
        return d;
    }

    public void performClickOnTotal(){
        SubdivisionItem item = items.get(0);
        of.setDescriptionItemSplit(item.getMode());
        if(total_bill.getOwed_money()==0.0){

            int position = getFirstItemAvaiablePosition();
            if(position<items.size()) {
                showItem(position + 1);
                setItemShow(position);
            }

        }else {
            for (int x = 0; x < items.size(); x++)
                items.get(x).setIsShow(true);
            item.setIsShow(false);

            //show original bill in order fragment-> adapter
                               /* of.getOrderListAdapter().showOriginalBill();
                                of.setSubOrderNumber("");*/

            //item AND PERSON price
            float itemAndPersonObjectPrice = returnSubdivisionItemsPrice();
            float discount = returnDiscountForItemForTotalBill(total_bill.getItems());
            float homage = returnItemHomageForTotalBill(total_bill);
            boolean checkIfZero = false;
            float myTotal = roundDecimal(total_bill.getOwed_money(),2);
            float totalDiscount = roundDecimal(total_bill.getDiscount(),2);
            float totalItemDiscount = roundDecimal(discount,2);

            float tcs = roundDecimal(total_subdivisions_cost,2);
            float ia = roundDecimal(itemAndPersonObjectPrice,2);

            float first = roundDecimal(myTotal-totalDiscount-totalItemDiscount,2);
            float second = roundDecimal(tcs-ia,2);
            float last = roundDecimal(first-second,2);
            if(last>0.0f){
            //if ((total_bill.getOwed_money()-total_bill.getDiscount()-discount) - (total_subdivisions_cost - itemAndPersonObjectPrice) > 0.0f) {
                //ancora da splittare
                of.setPriceTotalBillA(total_bill.getOwed_money(), total_subdivisions_cost, itemAndPersonObjectPrice, total_bill.getDiscount(), discount, homage);
            } else {
                //il total bill è a 0.0
                if (returnIfPerNumberPresent()) {
                    float perNumberDivision = returnSubdivisionNumberPrice();
                    checkIfZero = true;
                    of.setPriceA(perNumberDivision, total_bill.getDiscount(), 0.0f);
                    ((PaymentActivity) context).resetButtonPerNumberGreen();
                    ((PaymentActivity) context).activatePaymentButtonsOnly();
                    //((PaymentActivity) context).hidenBlueButtonExPrint();
                    ((PaymentActivity) context).hidenBlueButtonExPrintAndEmail();
                }else {
                    checkIfZero = true;
                    of.setPriceA(0.0f, 0.0f, 0.0f);
                }

            }

            if (returnIfPerNumberPresent()) {
                //there is a split number
                ((PaymentActivity) context).hidenBlueButtonExPrintAndEmail();
                                       /* if (items.size() == 2) {
                                            //è presente solo il per number quindi faccio pagare
                                            ((PaymentActivity) context).hidenBlueButtonExPrint();
                                        } else {
                                            //si può pagare solo da number
                                            ((PaymentActivity) context).hideButtonPerNumber();
                                        }*/
            } else {
                //no number is presente
                //still split bill to be done
                if (!checkIfZero) {//check if other split
                    if (checkIfThereIsOneAmount()) {
                        //c'è almeno un per amount -> per person e per item vanno spenti
                        ((PaymentActivity) context).hidePersonAndItemDivision();
                        ((PaymentActivity) context).activatePaymentButtonsOnly();
                    } else {
                        ((PaymentActivity) context).buttonOpacitySetting();
                        ((PaymentActivity) context).resetButtonPerNumberBlue();
                        ((PaymentActivity) context).showSplitButton();

                        ((PaymentActivity) context).showPaymentButton();
                        ((PaymentActivity) context).activatePaymentButtonsOnly();

                    }

                } else {
                    //no splitt bill to do
                    ((PaymentActivity) context).closeBill(true);
                    ((PaymentActivity) context).hideButtonPerNumber();


                }
            }
            if (checkIfZero) of.getOrderListAdapter().showOriginalBill();
            else
                of.getOrderListAdapter().showOriginalBill1(item.getItems(), item.getItems_map());
            //of.getOrderListAdapter().showOriginalBill();
            of.setSubOrderNumber("");
            of.notPaidOrder();

            split_opened = item;
            split_opened_position = -1;
           // seh.view.setSelected(true);
        }
        notifyDataSetChanged();
    }


    /*public void showItem1(int position){
        SubdivisionItem item = items.get(position-1);
        if(item.getMode()==4 || item.getMode()==1){
            of.getOrderListAdapter().showPartialSplitBill(item);
            of.hideRemaingTotal();
        }else if(item.getMode()==-1){
            of.getOrderListAdapter().showSplitBillOriginal(item.getItems_map(), item);
        }else{
            of.getOrderListAdapter().showSplitBill(item.getItems_map(), item);
        }
        if(item.getMode()==1){
            of.setPriceA(item.getOwed_money()*item.getNumber_subdivision() , 0.0f, 0.0f);
        }else if(item.getMode()==-1){
            float  a = getRemainingCost() ;
            float b = returnSubdivisionItemsPrice();
            //  of.setPriceA(getRemainingCost());
            //of.setPriceA(item.getOwed_money());
            of.setPriceA(item.getOwed_money(), item.getDiscount(), 0.0f);
            //of.setPriceA(item.getOwed_money()-total_subdivisions_cost+ returnSubdivisionItemsPrice());
        }
        else {
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = of.getOrderListAdapter().getModifier();
            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>(item.getItems());
            float homage = 0.0f;
            float itemDiscount = 0.0f;
            for (CashButtonLayout prod:myProducts) {
                if(prod.getHomage()==1) {
                    homage+=prod.getPriceFloat();
                    ArrayList<CashButtonListLayout> mods = myMap.get(prod);
                    for (CashButtonListLayout mod : mods) {
                            homage += mod.getPriceFloat() * mod.getQuantityInt();
                    }
                }
                if(prod.getDiscount()!=0.0f) {
                    itemDiscount+=prod.getDiscount();
                }
            }

            of.setPriceA(item.getOwed_money()-homage, item.getDiscount() , itemDiscount);
        }
        split_opened = item;

        if(item.getMode()==1) {
            if (items.size() == 2 || items.size() == 3) {

            } else {
                ((PaymentActivity) context).hidePerNumberDivision();
                ((PaymentActivity)context).resetButtonPerNumberGreen();
            }
        }
        if(item.getMode()!=-1)((PaymentActivity)context).activatePaymentButtonsOnly();
        notifyDataSetChanged();


    }
*/
    public int returnOriginalPosition(int groupPosition){
        int toreturn = -1;
        SubdivisionItem item =getSelectedItem();
        if(item==null)
            item = total_bill;
        int idTmp = -1;
        ArrayList<CashButtonLayout> products = item.getItems();
        if(groupPosition<= products.size())
            idTmp = products.get(groupPosition).getID();
        ArrayList<CashButtonLayout> allProducts = items.get(0).getItems();
        Collections.sort(allProducts);
        for(int i=0; i< allProducts.size();i++){
            if(allProducts.get(i).getID()==idTmp){
                toreturn = i;
                break;
            }
        }
        return toreturn;
    }

    public void setItemShow(int position){
        for(int i =0; i< items.size(); i++){
            if(i==position)
                items.get(i).setIsShow(false);
            else
                items.get(i).setIsShow(true);
        }
        notifyDataSetChanged();
    }

    public void showItem(int position){
        if(items !=null){
            if(items.size()>=position) {
                SubdivisionItem item = items.get(position - 1);
                of.setDescriptionItemSplit(item.getMode());
                if (item.getMode() == -1) {
                    split_opened = items.get(0);
                    split_opened_position = -1;
                    items.get(0).setIsShow(false);
                    of.getOrderListAdapter().showSplitBillOriginal(item.getItems_map(), item);
                    float a = getRemainingCost();
                    float b = returnSubdivisionItemsPrice();
                    //  of.setPriceA(getRemainingCost());
                    //of.setPriceA(item.getOwed_money());
                    of.setPriceA(item.getOwed_money(), item.getDiscount(), 0.0f);

                    performClickOnTotal();

                } else if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE) {
                    of.getOrderListAdapter().showSplitBill(item.getItems_map(), item);
                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = of.getOrderListAdapter().getModifier();
                    ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>(item.getItems());
                    float homage = 0.0f;
                    float itemDiscount = 0.0f;
                    for (CashButtonLayout prod : myProducts) {
                        if (prod.getHomage() == 1) {
                            homage += prod.getPriceFloat();
                            ArrayList<CashButtonListLayout> mods = myMap.get(prod);
                            if (mods != null) {
                                for (CashButtonListLayout mod : mods) {
                                    homage += mod.getPriceFloat() * mod.getQuantityInt();
                                }
                            }
                        }
                        if (prod.getDiscount() != 0.0f) {
                            itemDiscount += prod.getDiscount();
                        }
                    }

                    of.setPriceA(item.getOwed_money() - homage, item.getDiscount(), itemDiscount);
                    split_opened = item;
                    split_opened_position = position;
                    ((PaymentActivity) context).activatePaymentButtonsOnly();
                } else if (item.getMode() == PERCENTAGE_MODE) {
                    of.getOrderListAdapter().showPartialSplitBill(item);
                    of.hideRemaingTotal();
                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = of.getOrderListAdapter().getModifier();
                    ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>(item.getItems());
                    float homage = 0.0f;
                    float itemDiscount = 0.0f;
                    for (CashButtonLayout prod : myProducts) {
                        if (prod.getHomage() == 1) {
                            homage += prod.getPriceFloat();
                            ArrayList<CashButtonListLayout> mods = myMap.get(prod);
                            for (CashButtonListLayout mod : mods) {
                                homage += mod.getPriceFloat() * mod.getQuantityInt();
                            }
                        }
                        if (prod.getDiscount() != 0.0f) {
                            itemDiscount += prod.getDiscount();
                        }
                    }

                    of.setPriceA(item.getOwed_money() - homage, item.getDiscount(), itemDiscount);

                    split_opened = item;
                    split_opened_position = position;
                    ((PaymentActivity) context).activatePaymentButtonsOnly();
                } else if (item.getMode() == NUMBER_MODE) {
                    of.getOrderListAdapter().showPartialSplitBill(item);
                    of.hideRemaingTotal();
                    of.setPriceA(item.getOwed_money() * item.getNumber_subdivision(), 0.0f, 0.0f);
                    split_opened = item;
                    split_opened_position = position;

                    ((PaymentActivity) context).buttonOpacitySetting1();


                    ((PaymentActivity) context).resetOpacityForSlplittButton();
                    ((PaymentActivity) context).buttonOpacitySettingForPerNumber();
                }
                //split_opened = item;
                notifyDataSetChanged();

        /*if(item.getMode()==PERCENTAGE_MODE || item.getMode()==NUMBER_MODE){
            of.getOrderListAdapter().showPartialSplitBill(item);
            of.hideRemaingTotal();
        }else if(item.getMode()==-1){
            //TOTAL
            of.getOrderListAdapter().showSplitBillOriginal(item.getItems_map(), item);
        }else{
            //PER PERSON E PER ITEM
            of.getOrderListAdapter().showSplitBill(item.getItems_map(), item);
        }
        if(item.getMode()==1){
            of.setPriceA(item.getOwed_money()*item.getNumber_subdivision() , 0.0f, 0.0f);
        }else if(item.getMode()==-1){
            float  a = getRemainingCost() ;
            float b = returnSubdivisionItemsPrice();
            //  of.setPriceA(getRemainingCost());
            //of.setPriceA(item.getOwed_money());
            of.setPriceA(item.getOwed_money(), item.getDiscount(), 0.0f);
            //of.setPriceA(item.getOwed_money()-total_subdivisions_cost+ returnSubdivisionItemsPrice());
        }
        else {
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = of.getOrderListAdapter().getModifier();
            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>(item.getItems());
            float homage = 0.0f;
            float itemDiscount = 0.0f;
            for (CashButtonLayout prod:myProducts) {
                if(prod.getHomage()==1) {
                    homage+=prod.getPriceFloat();
                    ArrayList<CashButtonListLayout> mods = myMap.get(prod);
                    for (CashButtonListLayout mod : mods) {
                        homage += mod.getPriceFloat() * mod.getQuantityInt();
                    }
                }
                if(prod.getDiscount()!=0.0f) {
                    itemDiscount+=prod.getDiscount();
                }
            }

            of.setPriceA(item.getOwed_money()-homage, item.getDiscount() , itemDiscount);
        }
        split_opened = item;

        if(item.getMode()==1) {
            if (items.size() == 2 || items.size() == 3) {

            } else {
                ((PaymentActivity) context).hidePerNumberDivision();
                ((PaymentActivity)context).resetButtonPerNumberGreen();
            }
        }
        if(item.getMode()!=-1)((PaymentActivity)context).activatePaymentButtonsOnly();
        notifyDataSetChanged();*/
            }
        }

    }



    public void showItemOriginal(){

       /* SubdivisionItem item = items.get(0);
        of.setDescriptionItemSplit(item.getMode());
        of.getOrderListAdapter().showSplitBillOriginal(item.getItems_map(), item);
        float  a = getRemainingCost() ;
        float b = returnSubdivisionItemsPrice();
        of.setPriceA(getRemainingCost()-returnUnpaidValue(), item.getDiscount(),0.0f);
        split_opened = item;
        of.setDescriptionItemSplit(item.getMode());
        notifyDataSetChanged();*/
       performClickOnTotal();
    }

    public SubdivisionItem getSelectedItem(){
        if(split_opened==null) return null;
        else return split_opened;
    }

    public SubdivisionItem getItemByPosition(int position){
        if(position<items.size()){
            return items.get(position);
        }else return null;
    }

    public int getSelectedItemPosition(SubdivisionItem item){
        int itoReturn=-1;
        for(int i=1; i< items.size(); i++){
            if(items.get(i)==item) itoReturn = i+1;
        }
        return itoReturn;
    }

    public SubdivisionItem getPerNumberItem(){
        int itoReturn=-1;
        for(int i=1; i< items.size(); i++){
            if(items.get(i).getMode()==NUMBER_MODE) itoReturn = i;
        }
        if(itoReturn!=-1) return items.get(itoReturn);
        else return null;
    }

    public boolean checkIfLastItemIsPerNumber(){
        if(items.get(items.size()-1).getMode()==NUMBER_MODE) return true;
        else return false;
    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parent = recyclerView;
    }

    public float returnItemHomage(SubdivisionItem item){
        float homage = 0.0f;
        ArrayList<CashButtonLayout> products = item.getItems();
        for(int i=0; i<products.size(); i++){
            if(products.get(i).getHomage()==1) {
                homage += products.get(i).getPriceFloat();
                ArrayList<CashButtonListLayout> modifiers = products.get(i).getCashList();
                if(modifiers!=null) {
                    for (int j = 0; j < modifiers.size(); j++) {
                        homage += modifiers.get(j).getPriceFloat() * modifiers.get(j).getQuantityInt();
                    }
                }
            }

        }
        return homage;
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

    public void setItemDiscount(float discount){
        float old = items.get(0).getDiscount();
        if(discount!=0.0f)
            items.get(0).setDiscount(old+discount);
        else
            items.get(0).setDiscount(discount);
    }

    public int addElement(HashMap<CashButtonLayout,Integer> map, Float cost, Object o, int quantity){

        SubdivisionItem item = new SubdivisionItem();
        item.setMode(this.mode);
        float homage = 0.0f;
        if(mode == ITEM_MODE || mode == PERSON_MODE) {
            item.setItems(new ArrayList<>(map.keySet()));
            item.setItems_map(map);
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap = of.getOrderListAdapter().getModifier();
            ArrayList<CashButtonLayout> myProducts = new ArrayList<CashButtonLayout>(item.getItems());

            for (CashButtonLayout prod:myProducts) {
                if(prod.getHomage()==1) {
                    homage+=prod.getPriceFloat();
                    ArrayList<CashButtonListLayout> mods = prod.getCashList();
                    if(mods!=null) {
                        for (CashButtonListLayout mod :
                                mods) {
                            homage += mod.getPriceFloat() * mod.getQuantityInt();
                        }
                    }

                }
            }
            ArrayList<CashButtonLayout> products = item.getItems();
            Collections.sort(products);
            for (int i = 0; i < products.size(); i++) {
               products.get(i).setPosition(i);
            }
            item.setItems(products);

            of.getOrderListAdapter().showSplitBill(item.getItems_map(), item);


        }
        else{
            if(mode == PARTIAL_MODE) {
                item.setPercentage((Float) o);
                item.setNumber_subdivision(1);
            }
            if(mode == PERCENTAGE_MODE) {
                item.setPercentage((Float) o);
                item.setNumber_subdivision(1);
            }
            if(mode == NUMBER_MODE)
                item.setNumber_subdivision(((Float)o).intValue());
        }
       // item.setQuantity(quantity);
        float discount  = 0.0f;
        if(map!=null)discount = returnDiscountForItem(new ArrayList<>(map.keySet()));



        item.setOwed_money(cost+discount);
        //item.setDiscount(discount);
        item.setIsShow(false);
        if(mode == NUMBER_MODE){
            total_subdivisions_cost += cost*((Float)o).intValue();
           // ((PaymentActivity)context).hideButtonPerNumber();
      //      ((PaymentActivity)context).buttonOpacitySetting1();
        }
        else{
            total_subdivisions_cost += cost+discount;
      //      ((PaymentActivity)context).buttonOpacitySetting();
        }
        for (int x=0; x<items.size(); x++) items.get(x).setIsShow(true);
        items.add(item);
        ((PaymentActivity)context).setPay_mode(PAY_PARTIAL_BILL);

        //setto l'owed money per total bill, solo per item e person, per amount e number l'ownd rimane uguale
        if(item.getMode()==ITEM_MODE || item.getMode() == PERSON_MODE/* || item.getMode() == PERCENTAGE_MODE*/){
            if(items.get(0).getDiscount()-item.getDiscount()>0) {
                //items.get(0).setDiscount(items.get(0).getDiscount()-item.getDiscount());
            }else {
                //items.get(0).setDiscount(0.0f);
            }

            items.get(0).setOwed_money(roundDecimal(items.get(0).getOwed_money()-item.getOwed_money()-homage,2));
        }


        notifyDataSetChanged();
        split_opened = item;

        ((PaymentActivity)context).hidePartial();
        return items.size();
        //if(mode == PERCENTAGE_MODE) item.setPercentage(20);
        /*String s = new String("Item List n."+(element_titles.size()+1)+
                "\nCost: "+ cost );
        element_titles.add(s);
        map.put(s,list);
        costs_map.put(s, cost);
        notifyDataSetChanged();*/
    }

    public float returnDiscountForItem(ArrayList<CashButtonLayout> products){
        float discount = 0.0f;
        for(int i=0; i< products.size(); i++){
            discount+= products.get(i).getDiscount();
        }
        return discount;

    }


    public float returnDiscountForItemForTotalBill(ArrayList<CashButtonLayout> products){
        float discount = 0.0f;
        for(int i=0; i< products.size(); i++){
            if(!products.get(i).isSelected())
            discount+= products.get(i).getDiscount();
        }
        return discount;

    }

    public int returnFirstPositionToShow(){
        int myposition = 1;
        for(int i=0; i< items.size(); i++){
            if(!items.get(i).isPaid() && items.get(i).getOwed_money()>0.0f) {
                myposition = i+1;
                break;
            }
        }
        return myposition;
    }

    public void setOpenSplitPaid(){
        if(split_opened_position==items.size()) {
            //mostro il total bill

            for (int x = 0; x < items.size(); x++) items.get(x).setIsShow(true);

            items.get(0).setIsShow(false);

            of.getOrderListAdapter().showOriginalBill();
            float forItemPrice = returnSubdivisionItemsPrice();
            float dis = returnDiscountForItem(total_bill.getItems());
            if (total_bill.getOwed_money() - (total_subdivisions_cost - forItemPrice) > 0.0f)
                of.setPriceTotalBill(total_bill.getOwed_money(), total_subdivisions_cost, forItemPrice, total_bill.getDiscount(), dis);
            else {
                ((PaymentActivity) context).resetButtonPerNumberGreen();
                of.setPriceA(total_bill.getOwed_money(), total_bill.getDiscount(), 0.0f);
            }
            of.setSubOrderNumber("");
            // items.remove(0);
            // items.add(split_opened_position, split_opened);
            split_opened = items.get(0);
            //    split_opened = null;
            split_opened_position = 1;
            if (total_bill.getOwed_money() - total_subdivisions_cost > 0.0f) {
                ((PaymentActivity) context).buttonOpacitySetting();
                ((PaymentActivity) context).resetButtonPerNumberBlue();
            } else {
                ((PaymentActivity) context).buttonOpacitySetting1();
                //     ((PaymentActivity)context).hideButtonPerNumber();
            }
            notifyDataSetChanged();
        }else{
            showItem(returnFirstPositionToShow());
            items.get(returnFirstPositionToShow()-1).setIsShow(false);
            notifyDataSetChanged();
        }
        // -1.0f => set price to current Remaining price
        /**
        of.setPriceNoText(-1.0f);
        of.setSubOrderNumber("");
        if (split_opened!=null) {
            items.remove(0);
            items.add(split_opened_position, split_opened);
            split_opened.setPaid(true);
            int  quantity = 1;
            if(split_opened.getNumber_subdivision()>1) quantity = split_opened.getNumber_subdivision();
            total_subdivisions_paid += split_opened.getOwed_money()*quantity;
            //parent.getChildAt(split_opened_position).setActivated(true);
            split_opened = null;
            split_opened_position = -1;
            notifyDataSetChanged();
        }
         */
    }



    public int getMode() {
        return mode;
    }

    public Float getRemainingCost(){
        Float a = total_bill.getOwed_money();
        Float b = total_subdivisions_paid;
        float c = ((PaymentActivity) context).getCreditValueAgain();
        float forItemPrice =  returnSubdivisionItemsPrice();

        return (total_bill.getOwed_money()+forItemPrice)-(total_subdivisions_paid) - ((PaymentActivity)context).getCreditValueAgain();
    }

    public Float getRemainingCostForTotal(){
        float value = 0.0f;
        for(int i=1; i<items.size();i++){
            if(items.get(i).getMode()==PERCENTAGE_MODE || items.get(i).getMode()==NUMBER_MODE)
                value += items.get(i).getOwed_money();

        }
        return value;
    }

    public Float getRemainingCostNoSplit(){
        Float a = total_bill.getOwed_money();
        Float b = total_subdivisions_paid;
        float c = ((PaymentActivity) context).getCreditValueAgain();
        float forItemPrice =  0.0f;

        return (total_bill.getOwed_money()+forItemPrice)-(total_subdivisions_paid) - ((PaymentActivity)context).getCreditValueAgain();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setTotalHomageForItem(int position, boolean activate) {
        items.get(position-1).setHomage(activate);
        notifyDataSetChanged();
    }
    public void showFirstPaid() {
        if(items.size()>0)
            if(items.get(0).isPaid() && items.size()>1) showItem(2);
    }

    public void loadPaidBillSplits(ArrayList<SubdivisionItem> splits_list){
        int n = -1;
        SubdivisionItem numberItem = null;
        for (SubdivisionItem item:splits_list
             ) {
            if(item.isPaid()) {
                if(item.getMode()!=1)
                    total_subdivisions_cost += item.getOwed_money();
                else
                    total_subdivisions_cost += item.getOwed_money()*item.getNumber_subdivision();
            }
            //if( (item.getMode()==PERCENTAGE_MODE) && item.isPaid()) total_subdivisions_paid+= item.getOwed_money();
            if( (item.getMode()==PERSON_MODE || item.getMode()==ITEM_MODE) && item.isPaid()){
                of.getOrderListAdapter().setTotal_cost(items.get(0).getOwed_money()-item.getOwed_money());
                of.getOrderListAdapter().setLeftCost(items.get(0).getOwed_money()-item.getOwed_money());
                items.get(0).setOwed_money(items.get(0).getOwed_money()-item.getOwed_money());
                total_subdivisions_paid += item.getOwed_money();
            }
            if(item.getMode()==4) {
                ((PaymentActivity) context).setButtonPermission();
                ((PaymentActivity) context).buttonOpacitySetting();
            }
            items.add(item);
            if(item.getMode()==NUMBER_MODE){
                if(n == -1){
                    n = item.getNumber_subdivision();
                    numberItem = item;
                }
                n--;
            }
        }
        /*for(int i=0; i<n;i++){
            SubdivisionItem it = new SubdivisionItem();
            it.setMode(numberItem.getMode());
            it.setOwed_money(numberItem.getOwed_money());
            it.setNumber_subdivision(numberItem.getNumber_subdivision());
            it.setPaid(false);
            items.add(it);
        }*/
        if(items.size()>0)
            ((PaymentActivity)context).setPay_mode(PAY_PARTIAL_BILL);
      /*  if(items.get(0).isPaid() && items.size()>1) showItem(2);*/
        notifyDataSetChanged();
    }

    // Adds already paid element
    public SubdivisionItem addElement(int mode, Float owed_money, Float value, boolean paid, Object map_cbl){
        SubdivisionItem item = new SubdivisionItem();
        item.setMode(mode);
        if( mode == PERCENTAGE_MODE || mode == PARTIAL_MODE)item.setPercentage(value);
        else if( mode == NUMBER_MODE) item.setNumber_subdivision(value.intValue());
        item.setOwed_money(owed_money);
        item.setPaid(paid);
        items.add(item);
        total_subdivisions_cost+=item.getOwed_money();
        total_subdivisions_paid+=item.getOwed_money();
        notifyDataSetChanged();
        return item;
    }





    public void removeItem(int position){
        if(position<items.size()){
            if(position!=0) {
                SubdivisionItem item = items.get(position);
                if ((split_opened_position != -1 && position != 0 || split_opened_position == -1) && !item.isPaid()) {

                    // split bill was open, restore normal view - part 1
                    /**
                    if (split_opened_position != -1) {
                        items.remove(0);
                        items.add(split_opened_position, split_opened);
                    }
                    if (item.getMode() == PERCENTAGE_MODE)
                        perc_split_number--;
                    if (item.getMode() == NUMBER_MODE) {
                         * If the item being deleted is a number subdivision item then delete all the others too.

                        ArrayList<SubdivisionItem> toRemove = new ArrayList<>();
                        for (int i = 0; i < items.size(); i++) {
                            SubdivisionItem si = items.get(i);
                            if (si.getMode() == NUMBER_MODE) {
                                total_subdivisions_cost -= si.getOwed_money();
                                toRemove.add(si);
                            }
                        }
                        items.removeAll(toRemove);
                    } else {
                        total_subdivisions_cost -= item.getOwed_money();
                        items.remove(item);
                    }
                    // split bill was open, restore normal view - part 2 ( needed to subdivide into two parts because of total_subdivision_cost
                    if (split_opened_position != -1) {
                        of.getOrderListAdapter().showOriginalBill();
                        of.setPrice(total_bill.getOwed_money() - total_subdivisions_cost);
                        of.setSubOrderNumber("");
                        split_opened = null;
                        split_opened_position = -1;
                    }
                    of.restoreFromRemovedItem(item, this);
                    */
                    if(item.getMode()==2 || item.getMode()==3) {
                        items.get(0).setOwed_money(items.get(0).getOwed_money()+item.getOwed_money()+returnItemHomage(item)/*-item.getDiscount()*/);
                        float dis = returnDiscountForItem(item.getItems());
                        items.get(0).setDiscount(items.get(0).getDiscount()/*+dis*/);
                        //items.get(0).setDiscount(item.getDiscount());
                        float a = of.getOrderListAdapter().getLeftCost();
                        float b = returnItemHomage(item);
                        of.getOrderListAdapter().setLeftCost(of.getOrderListAdapter().getLeftCost()+item.getOwed_money()-item.getDiscount()-dis/*-returnItemHomage(item)*/);
                    }
                    of.restoreFromRemovedItem(item, this);
                    items.remove(item);

                    if(item.getMode()==1) total_subdivisions_cost -= item.getOwed_money()*item.getNumber_subdivision();
                    else total_subdivisions_cost -= item.getOwed_money()+returnItemHomage(item);
                    if(total_subdivisions_cost<0) total_subdivisions_cost =0.0f;
                    boolean checkAmount = true;
                    for (int x=0; x<items.size(); x++) {
                        items.get(x).setIsShow(true);
                        if(items.get(x).getMode()==4) checkAmount = false;
                        if(items.get(x).getMode()==1) checkAmount = false;
                    }
                    items.get(0).setIsShow(false);
                    if(checkAmount) ((PaymentActivity)context).setAllButtonPermission();
                    of.getOrderListAdapter().showOriginalBill1(items.get(0).getItems(), items.get(0).getItems_map());
                    //of.getOrderListAdapter().showOriginalBill();
                    of.setSubOrderNumber("");
                    Float a = total_bill.getOwed_money();
                    Float b = total_subdivisions_cost;
                    Float itemsPrice = returnSubdivisionItemsPrice();

                    float dis1 = returnDiscountForItemForTotalBill(total_bill.getItems());
                    //returnDiscountForItem
                    float homage = returnItemHomage(total_bill);
                    if(total_bill.getOwed_money()- (total_subdivisions_cost-itemsPrice)>0.0f ) {
                   //     of.getOrderListAdapter().setLeftCost(total_bill.getOwed_money()-total_bill.getDiscount());
                        of.setPriceTotalBillA(total_bill.getOwed_money()/*+item.getDiscount()*/, total_subdivisions_cost, itemsPrice, total_bill.getDiscount(), dis1, homage);

                        //of.setPriceTotalBill(total_bill.getOwed_money()/*+item.getDiscount()*/, total_subdivisions_cost, itemsPrice, total_bill.getDiscount()+dis1, dis1);
                    }else {
                        of.setPriceA(total_bill.getOwed_money(), total_bill.getDiscount(), 0.0f );
                    }
                    ((PaymentActivity)context).setNumberSplit();
                    if(items.size()==1)  {
                        float dis=returnDiscountForItem(items.get(0).getItems());
                        if(items.get(0).getDiscount()/*-dis<=*/==0.0f)
                            of.hideRemaingTotal();
                        else of.hideRemainingWithDiscount();
                    }
                    if(item.getMode()==ITEM_MODE || item.getMode()==PERSON_MODE ){
                        if(items.size()>1)
                        of.setRemainingPercentageCost(total_bill.getOwed_money()-(total_subdivisions_cost-itemsPrice)-total_bill.getDiscount()-dis1-returnItemHomage(item));
                    }

                    of.setSubOrderNumber("");
                    of.setDescriptionItemSplit(-1);
                    // items.remove(0);
                    // items.add(split_opened_position, split_opened);
                    split_opened = items.get(0);
                    split_opened_position = -1;
                    if(total_bill.getOwed_money()- total_subdivisions_cost+itemsPrice>0.0f ) {
                        if(Float.compare(total_subdivisions_cost,itemsPrice)==0)((PaymentActivity)context).buttonOpacitySetting();
                        else ((PaymentActivity)context).hidePersonAndItemDivision();
                    }
                    else ((PaymentActivity)context).buttonOpacitySetting1();
                    ((PaymentActivity)context).resetButtonPerNumberBlue();
                    ((PaymentActivity)context).resetButtonPerNumberGreen();
                    if(items.size()==1) {
                        float dis = returnDiscountForItemForTotalBill(total_bill.getItems());
                        of.getOrderListAdapter().setLeftCost(total_bill.getOwed_money()-total_bill.getDiscount()-dis-returnItemHomageForTotalBill(total_bill));
                        ((PaymentActivity) context).reactivatePaymentButtons();
                    }

                    ((PaymentActivity) context).showSplitButton();
                    if(checkIfThereIsOneAmount()){
                        ((PaymentActivity)context).hidePersonAndItemDivision();
                    }else{
                        ((PaymentActivity)context).resetOpacityForSplit();
                    }

                    if(items.size()==1) of.getOrderListAdapter().setSplit(false);
                    ((PaymentActivity) context).closeBill(false);
                    ((PaymentActivity) context).setMode(0);
                    notifyDataSetChanged();

                } else {
                    // Need to be done because onSwipe automatically removes the ViewHolder
                    notifyDataSetChanged();
                }
            }else{
                notifyDataSetChanged();
            }
        }
    }

    public int getPerc_split_number() {
        return perc_split_number;
    }

    public float returnSubdivisionItemsPrice(){
        float price = 0.0f;
        for (int x=0; x<items.size(); x++) {
            if(items.get(x).getMode()==2 || items.get(x).getMode()==3 ){
                //price += items.get(x).getOwed_money()+items.get(x).getDiscount();
                price += items.get(x).getOwed_money();
                /*float homage = returnItemHomage(items.get(x));
                price += homage;*/
            }
        }
        return price;
    }

    public float returnSubdivisionNumberPrice(){
        float price = 0.0f;
        for (int x=0; x<items.size(); x++) {
            if(items.get(x).getMode()==NUMBER_MODE){
                //price += items.get(x).getOwed_money()+items.get(x).getDiscount();
                price += items.get(x).getOwed_money()*items.get(x).getNumber_subdivision();
                /*float homage = returnItemHomage(items.get(x));
                price += homage;*/
            }
        }
        return price;
    }

    public SubdivisionItem returnSplitOpen(){
        return split_opened;
    }

    public boolean returnIfPerNumberPresent(){
        boolean present= false;
        for (int x=0; x<items.size(); x++) {
            if(items.get(x).getMode()==1){
                present = true;
            }
        }
        return present;
    }

    //check if all subdivision order are items and all are paid
    //this is used when you pay the total and you hav alreay paid the oder items
    public boolean checkIfOtherAreItemSelectAndPaid(){
        boolean check = true;
        if(items.size()>1) {
            for (int x = 1; x < items.size(); x++) {
                if ((items.get(x).getMode() != 2 && items.get(x).getMode() != 3 && items.get(x).isPaid())) {
                    check = false;
                }

            }
        }
        return check;
    }

    public boolean checkIfOtherAreItemSelectAndPaid2(){
        boolean check = true;
        if(items.size()>1) {
            for (int x = 1; x < items.size(); x++) {
                if (((items.get(x).getMode() != 1 && items.get(x).getMode() != 4 )&& items.get(x).isPaid())) {
                    check = false;
                }

            }
        }
        return check;
    }

    public boolean checkIfOtherPersonOrItem(){
        boolean check = true;
        if(items.size()>1) {
            for (int x = 1; x < items.size(); x++) {
                if ((items.get(x).getMode() == 1 || items.get(x).getMode() == 4)) {
                    check = false;
                }

            }
        }
        return check;
    }

    public boolean checkIfThereIsOneAmount(){
        boolean check = false;
        if(items.size()>1) {
            for (int x = 1; x < items.size(); x++) {
                if ((items.get(x).getMode() == PERCENTAGE_MODE)) {
                    check = true;
                }

            }
        }
        return check;
    }

    public boolean checkIfThereIsPernumber(){
        boolean check = false;
        if(items.size()>1) {
            for (int x = 1; x < items.size(); x++) {
                if ((items.get(x).getMode() == NUMBER_MODE)) {
                    check = true;
                }

            }
        }
        return check;
    }

    public boolean checkIfOtherArePaid(){
        boolean check = true;
        if(items.size()>1) {
            for (int x = 1; x < items.size(); x++) {
                if(items.get(x).getMode()!=NUMBER_MODE) {
                    if (!items.get(x).isPaid()) {
                        check = false;
                    }
                }
            }
        }
        return check;
    }

    /** HOLDER **/
    public static class SubdivisionElementHolder extends RecyclerView.ViewHolder{
        public RelativeLayout subdivision_container;
        public CustomTextView split_id;
        public CustomTextView amount_tv;
        public CustomTextView mode_tv;
        public View view;
        public SubdivisionElementHolder(View itemView) {
            super(itemView);
            view = itemView;
            subdivision_container = (RelativeLayout) itemView.findViewById(R.id.subdivision_total_contiainer);
            split_id = (CustomTextView)itemView.findViewById(R.id.split_id);
            amount_tv = (CustomTextView)itemView.findViewById(R.id.amount_tv);
            mode_tv = (CustomTextView)itemView.findViewById(R.id.mode);
        }

    }
}
