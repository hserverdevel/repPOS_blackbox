package com.example.blackbox.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.Operative;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.adapter.ClientPayFidelityAdapter;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Fidelity;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.utils.db.DatabaseAdapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.example.blackbox.adapter.OrderListAdapter.DEFAULT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.ELEMENT_ITEM_SPLIT;
import static com.example.blackbox.adapter.OrderListAdapter.INSERT_CREDIT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.INSERT_FIDELITY_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.ITEM_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.MODIFY_DISCOUNT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.NUMBER_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PARTIAL_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_LEFT_PAYMENT;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_PARTIAL_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_TICKET_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_TOTAL_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERCENTAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERSON_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.TIPS_MODE;

/**
 * Created by DavideLiberato on 07/07/2017.
 */

public class CalculatorFragment extends Fragment
{

    private View myself;
    public StringBuilder amount;

    private PaymentActivityCommunicator communicator;

    private OrderFragment orderFragment;
    private OrderSubdivisionFragment orderSubdivisionFragment;

    private static String PREF_NAME = "prefs";

    private float paidVar = 0;
    private boolean shortcutPressed = false;
    private boolean isActive = false;
    private int mode = 0;
    private Float cost;
    private boolean discountMode = false;
    private boolean percentageAmount = false;
    private int tmpPositionDiscount = -1;
    private float tmpDiscount = 0.0f;
    private int fidelityClientId = -1;


    public int okPressedNTimes = 0;
    public float discountAmount = 0.0f;
    public float actualCredit = 0.0f;
    public boolean digitPressed = true;
    public ArrayList<LeftPayment> leftPayments = new ArrayList<LeftPayment>();
    public ClientInfo selectedClientPayFidelity;


    int typeDisplay = 1;
    Animation shake;



    // -------- GET / SET -------- //

    public void setPressedTime(int i){okPressedNTimes = i;}

    public float getCost(){return cost;}

    public void setDiscountMode(Boolean b){discountMode=b;}
    public boolean getDiscountMode(){return discountMode;}

    public boolean getPercentageAmount(){return percentageAmount;}

    public float getActualCredit(){return actualCredit;}
    public void resetActualCredit(){actualCredit = 0.0f;}
    public void setActualCredit(float f){actualCredit =  actualCredit + f;}


    public void setTmpDiscount(float f) { tmpDiscount = f;}

    public void setTmpPositionDiscount(int i) { tmpPositionDiscount= i;}
    public int getTmpPositionDiscount() { return tmpPositionDiscount;}

    public ArrayList<LeftPayment> getLeftPayment() {return leftPayments;}
    public void setLeftPayment(ArrayList<LeftPayment> leftPayments) {this.leftPayments=leftPayments;}

    public void setMode(int mode) { this.mode = mode;}


    public void setIsActive(Boolean b) {
        isActive = b;
    }
    public boolean isActive(){return isActive;}
    public void setIsActiveToFalse(){
        isActive = false;
    }






    // --------- MAIN ------------ //



    public CalculatorFragment(){
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        myself = inflater.inflate(R.layout.calculator_fragment, container, false);
        amount = new StringBuilder();
        communicator = (PaymentActivityCommunicator)getContext();

        setupDigits();

        //setupXOK();

        shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);

        return myself;
    }


    /**
     * Setup of the calculator digits (numbers and special buttons (the blue ones on the side))
     */
    private void setupDigits()
    {
        RelativeLayout digitContainer = (RelativeLayout)myself.findViewById(R.id.digits_subcontainer);
        View v;

        for (int i = 0; i < digitContainer.getChildCount(); i++)
        {
            v = digitContainer.getChildAt(i);

            // the d_10, d_20 ... buttons are the blue buttons on the side of the calculator,
            // that are used for special functions
            if(v.getId() == R.id.d_10)
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {

                        if (isActive &&
                            (mode==PAY_LEFT_PAYMENT || mode==DEFAULT_MODE || mode==PAY_PARTIAL_MODE || mode==PAY_TOTAL_MODE))
                        {
                            okPressedNTimes = 0;
                            amount = new StringBuilder();
                            amount.append(10);

                            ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());

                            if(mode==PAY_PARTIAL_MODE)
                            {
                                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText( String.format("%.2f", roundDecimal(10, 2)).replace(".", ","));//.replace(",", "."));
                            }
                            shortcutPressed = true;
                        }

                        else if (mode==DISCOUNT_MODE || mode==MODIFY_DISCOUNT_MODE)
                        {
                            amount = new StringBuilder();
                            amount.append("€");

                            discountMode = false;
                            digitPressed = true;
                            // ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());

                            //
                            //amount.append(10);
                            //((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"%");
                            //shortcutPressed = true;
                        }

                        else if (mode==PAY_TICKET_MODE)
                        {
                            float amount1 = 0.0f;
                            if(((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).getText().toString().length()>0) {
                                amount1 = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                //amount.append(5.29);
                                amount1 = amount1 + 4.00f;
                            }else{
                                amount1= 4.00f;
                            }
                            amount = new StringBuilder();
                            amount.append( String.format("%.2f", roundDecimal(amount1, 2)));//.replace(",", "."));
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText( String.format("%.2f", roundDecimal(amount1, 2)).replace(".", ","));//.replace(",", "."));

                        }else if(mode==PERCENTAGE_MODE){
                            amount = new StringBuilder();
                            ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                            percentageAmount =true;
                        }
                    }
                });

            else if(v.getId() == R.id.d_20)
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (isActive &&
                            (mode==PAY_LEFT_PAYMENT || mode==DEFAULT_MODE || mode==PAY_PARTIAL_MODE || mode==PAY_TOTAL_MODE))
                        {
                            okPressedNTimes = 0;
                            amount = new StringBuilder();
                            amount.append(20);

                            ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                            shortcutPressed = true;
                        }

                        else if(mode==DISCOUNT_MODE || mode==MODIFY_DISCOUNT_MODE)
                        {
                            digitPressed = true;
                            amount = new StringBuilder();
                            amount.append("%");
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                            discountMode=true;
                            //amount.append(20);
                            //((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"%");
                            //shortcutPressed = true;
                        }else if(mode==PAY_TICKET_MODE){
                            float amount1 = 0.0f;
                            if(((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).getText().toString().length()>0) {
                                amount1 = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                //amount.append(5.29);
                                amount1 = amount1 + 5.00f;
                            }else{
                                amount1= 5.00f;
                            }
                            amount = new StringBuilder();
                            amount.append( String.format("%.2f", roundDecimal(amount1, 2)));//.replace(",", "."));
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText( String.format("%.2f", roundDecimal(amount1, 2)));//.replace(",", "."));


                        }else if(mode==PERCENTAGE_MODE){
                            percentageAmount =false;
                        }
                    }
                });

            else if(v.getId() == R.id.d_50)
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (/*amount.length()==0 && */isActive && (mode==PAY_LEFT_PAYMENT||mode==DEFAULT_MODE||mode==PAY_PARTIAL_MODE||mode==PAY_TOTAL_MODE)) {
                            okPressedNTimes = 0;
                            amount = new StringBuilder();
                            amount.append(50);
                            ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                            shortcutPressed = true;
                        }else if(mode==DISCOUNT_MODE){
                            digitPressed = true;
                            amount = new StringBuilder();
                            amount.append(0);
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(R.string.gratis_all_caps);
                        }else if(mode==MODIFY_DISCOUNT_MODE){
                            digitPressed = true;
                            amount = new StringBuilder();
                            amount.append(0);
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(R.string.gratis_all_caps);
                        }else if(mode==PAY_TICKET_MODE){
                            float amount1 = 0.0f;
                            if(((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).getText().toString().length()>0) {
                                amount1 = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                //amount.append(5.29);
                                amount1 = amount1 + 5.20f;
                            }else{
                                amount1= 5.20f;
                            }
                            amount = new StringBuilder();
                            amount.append( String.format("%.2f", roundDecimal(amount1, 2)).replace(".", ","));//.replace(",", "."));
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText( String.format("%.2f", roundDecimal(amount1, 2)).replace(".", ","));//.replace(",", "."));

                        }
                    }
                });

            else if(v.getId() == R.id.d_100)
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (/*amount.length()==0 && */isActive && (mode==PAY_LEFT_PAYMENT||mode==DEFAULT_MODE||mode==PAY_PARTIAL_MODE||mode==PAY_TOTAL_MODE)) {
                            okPressedNTimes = 0;
                            amount = new StringBuilder();
                            amount.append(100);
                            ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                            shortcutPressed = true;
                        }else if(mode==DISCOUNT_MODE){
                            digitPressed = true;
                            amount = new StringBuilder();
                            amount.append(0);
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                        }else if(mode==MODIFY_DISCOUNT_MODE){
                            digitPressed = true;
                            amount = new StringBuilder();
                            amount.append(0);
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                        }else if(mode==PAY_TICKET_MODE){
                            float amount1 = 0.0f;
                            if(((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).getText().toString().length()>0) {
                                amount1 = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                //amount.append(5.29);
                                amount1 = amount1 + 5.29f;
                            }else{
                                amount1= 5.29f;
                            }
                            amount = new StringBuilder();
                            amount.append( String.format("%.2f", roundDecimal(amount1, 2)));//.replace(",", "."));
                            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText( String.format("%.2f", roundDecimal(amount1, 2)).replace(".", ","));//.replace(",", "."));


                        }
                    }
                });

            // if any other digit is pressed,
            // simply add it to the string builder
            else
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (isActive) {
                            digitPressed = true;
                            buildString(((CustomButton)v).getText().charAt(0));
                        }
                    }
                });

            // handle the `C` button to delete.
            // if it's long pressed, reset the stringbuilder
            v.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    char c =  ((CustomButton)v).getText().charAt(0);
                    if (c=='C')
                    {
                        amount = new StringBuilder();
                        switch(mode){
                            case PAY_PARTIAL_MODE:
                                if(okPressedNTimes==1){
                                    ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                                }else
                                    ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                                break;
                            case PAY_TICKET_MODE:

                                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                                break;
                            case INSERT_CREDIT_MODE:
                                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                            case NUMBER_MODE:
                                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                                break;
                            case PERCENTAGE_MODE:
                                if(percentageAmount) ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                                else ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"%");
                                break;

                            case DISCOUNT_MODE:
                                digitPressed = true;
                                if (okPressedNTimes == 1)
                                {
                                    okPressedNTimes = 0;
                                    amount = new StringBuilder();
                                }
                                if(!discountMode) ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString()+"€");
                                else ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString()+"%");
                                break;

                            case TIPS_MODE:
                                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                                break;

                            default:
                                ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                        }
                    }
                    return false;
                }
            });
        }
    }


    public void setupXOK()
    {
        getActivity().findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isActive)
                {
                    //check if split
                    if(leftPayments.size()>0)
                    { }

                    else
                    {
                        ((PaymentActivity) getContext()).setNormalKillOkButton();

                        ((PaymentActivity) getContext()).resetOpacityForPayementButtons();
                        ((PaymentActivity) getContext()).activatePaymentButtons();
                        ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText("");

                        amount = new StringBuilder();

                        turnOnOffCalculator();
                        setMode(0);

                        okPressedNTimes = 0;

                        ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                        orderFragment.getOrderListAdapter().notifyDataSetChanged();
                       /* ((PaymentActivity) getContext()).resetOtherButtons();
                        ((PaymentActivity) getContext()).showAllBlueButton();*/
                        orderFragment.discountSet = false;

                        ((PaymentActivity) getContext()).setMode(0);
                        ((PaymentActivity) getContext()).setDiscountSet(false);

                        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                        if (item == null)
                        { item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

                        switch (item.getMode())
                        {
                            case PERCENTAGE_MODE :
                                break;

                            case PERSON_MODE :
                                ((PaymentActivity) getContext()).activatePaymentButtonsOnly();
                                ((PaymentActivity) getContext()).hideSplitButton();
                                ((PaymentActivity) getContext()).showAllBlueButtonExSplit();
                                break;

                            case ITEM_MODE:
                                ((PaymentActivity) getContext()).activatePaymentButtonsOnly();
                                ((PaymentActivity) getContext()).hideSplitButton();
                                ((PaymentActivity) getContext()).showAllBlueButtonExSplit();
                                break;

                            case NUMBER_MODE:
                                break;
                            default :
                                ((PaymentActivity) getContext()).showSubdivisionItem();
                                break;
                        }
                    }
                }

                else
                {
                    Intent intent = new Intent(getContext(), Operative.class);
                    Intent intentPay= getActivity().getIntent();

                    int billId = intentPay.getIntExtra("billId", -1);
                    int orderNumber = intentPay.getIntExtra("orderNumber", 1);
                    String username= intentPay.getStringExtra("username");
                    Boolean isAdmin = intentPay.getBooleanExtra("isAdmin", false);

                    intent.putExtra("username", username);
                    intent.putExtra("isAdmin", isAdmin);
                    // intent.setAction("setTable");
                    intent.putExtra("billId", billId);
                    intent.putExtra("orderNumber", (orderNumber-1));
                    startActivity(intent);
                }
            }
        });


        // OK BUTTON
        final ImageButton okButton = (ImageButton) getActivity().findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isActive)
                {
                    switch (mode)
                    {

                        case DEFAULT_MODE:
                            if (okPressedNTimes == 0)
                            {
                                StringBuffer s = new StringBuffer();
                                String mycost = ((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString();//.replace(",", ".");
                                float cost = Float.parseFloat(mycost.replace(",", "."));
                                //float cost = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString());
                                String mypaid = (((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString());//.replace(",", "."));
                                if(!mypaid.equals(""))
                                {
                                    float paid = Float.parseFloat(mypaid.replace(",", "."));
                                    //float paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString());
                                    //s.append(roundDecimal(paid-cost,2));
                                    paidVar = paid;
                                    s.append(String.format("%.2f", roundDecimal(paid - cost, 2)).replace(".", ","));//.replace(",", "."));
                                    ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText(s);
                                    if (amount.length() != 0) amount.delete(0, amount.length());
                                    shortcutPressed = false;
                                    okPressedNTimes = 1;
                                }
                            }

                            else
                            {
                                //Init. Fiscal Printer
                                communicator.printBill(paidVar, cost, 0, 1);
                                turnOnOffCalculator();

                                // Apri cassa? termina transazione? qualcosa ma non ulteriore sottrazione.
                                // aggiorna valore di okPressedNTimes;
                                okPressedNTimes = 0;
                            }
                            break;


                        case NUMBER_MODE:
                        case PERCENTAGE_MODE:
                            String value = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                            if (!value.equals(""))
                            {
                                float myValue  = Float.parseFloat(value.replace(",", "."));
                                if(myValue>1)
                                {
                                    if (mode == PERCENTAGE_MODE)
                                        value = value.substring(0, value.length() - 1);
                                    float cost1 = Float.parseFloat(value.replace(",", "."));

                                    if ((percentageAmount && cost1 > cost) || (!percentageAmount && cost1 > 99))
                                    {
                                        if (percentageAmount) {
                                            Toast.makeText(getContext(), R.string.amount_cant_be_greater_than_cost, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), R.string.percentage_cant_be_greater_than_100, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    else
                                    {
                                        float n = Float.parseFloat(value.replace(",", "."));
                                        ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                                        ((PaymentActivityCommunicator) getContext()).activateFunction(PaymentActivity.ADD_SUBDIVISION_ELEMENT, n, cost);
                                        amount = new StringBuilder();
                                    }
                                }

                                else
                                {
                                    myself.findViewById(R.id.main_calc).startAnimation(shake);
                                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                                    amount = new StringBuilder();
                                }
                            }
                            break;


                        case TIPS_MODE:
                            break;


                        case PAY_TOTAL_MODE:
                            //pay total, check if first step
                            if (okPressedNTimes == 0) {
                                //first step, inserting payment amount ( calculate change)
                                StringBuffer s = new StringBuffer();
                                String paidString = ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString();
                                if(paidString.equals(",")){

                                }else {
                                    float cost = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));

                                    float paid = cost;
                                    if (paidString.length() != 0) {
                                        paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString().replace(",", "."));
                                    }
                                    //if cost in less than paid
                                    if (cost <= paid) {
                                        //set change, prepare for second step
                                        paidVar = paid;
                                        s.append(String.format("%.2f", roundDecimal(paid - cost, 2)).replace(".", ","));//.replace(",", "."));
                                        ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText(s);
                                        ((CustomTextView) myself.findViewById(R.id.change_tv)).setText(R.string.change);
                                        if (amount.length() != 0) amount.delete(0, amount.length());
                                        shortcutPressed = false;
                                        okPressedNTimes = 1;
                                        ((PaymentActivity)getContext()).openCashDrawer();
                                    } else {
                                        //cost is bigger than paid

                                        if(((PaymentActivity)getContext()).invoiceBill){
                                            Toast.makeText(getContext(), R.string.not_allowed_for_invoice, Toast.LENGTH_SHORT).show();
                                        }else {
                                            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                            if (item == null)
                                                item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                            //here you pay with left payement system
                                            //open popup left
                                            setMode(PAY_LEFT_PAYMENT);
                                            //add leftPayment object to leftPayments array
                                            int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                            LeftPayment leftPayment = new LeftPayment(paymentType, cost, paid, cost, item);
                                            leftPayments.add(leftPayment);
                                            //open popup to select left payment type
                                            ((PaymentActivity) getContext()).openPopupForPaymentLeft(leftPayments);

                                        }
                                    }
                                }
                            }

                            else {
                                //second step, I want to pay and print the recipiet
                                //controllo se ci sono più di un item

                                if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() > 1) {

                                    if (orderFragment.getOrderListAdapter().getSubdivisionItem() == null) { // => no split selected
                                        //QUA DA AGGIUNGERE SE STO PAGANDO IL TOTAL BILL E GLI ALTRI SONO TUTTI PAGATI
                                        if(orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherArePaid()) {
                                            if(!orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherPersonOrItem()) {
                                                // qua controllo se c'è solo un number non pagato
                                                if(orderSubdivisionFragment.getSubdivisionAdapter().checkIfThereIsPernumber()) {
                                                    SubdivisionItem subitem = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                                    SubdivisionItem pernumber = orderSubdivisionFragment.getSubdivisionAdapter().getPerNumberItem();
                                                    int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                                    if (paymentType == 4) {
                                                        if (subitem.getMode() == -1) {
                                                            orderSubdivisionFragment.getSubdivisionAdapter().setItemPaid(pernumber);
                                                            orderSubdivisionFragment.getSubdivisionAdapter().notifyDataSetChanged();
                                                            ((PaymentActivity) getContext()).openProcessCardPopupForItem();
                                                        }
                                                    }else {
                                                        ((PaymentActivity) getContext()).savePaidSubdivisionBill(pernumber, 1);
                                                        orderSubdivisionFragment.getSubdivisionAdapter().setItemPaid(pernumber);
                                                        orderSubdivisionFragment.getSubdivisionAdapter().notifyDataSetChanged();
                                                        ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paidVar, 1);
                                                        ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_TOTAL_BILL);
                                                        ((PaymentActivity) getContext()).savePaidBill(1);
                                                    }
                                                }else {
                                                    orderFragment.getOrderListAdapter()
                                                            .setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter()
                                                                    .addElement(PERCENTAGE_MODE, cost, 100.0f, true, null));
                                                    ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
                                                    ((PaymentActivity) getContext()).printFiscalBillWithNonFiscal(0.0f, paidVar, cost, "Partial Amount", (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()), 1);

                                                    if (orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost() - cost > 0) {
                                                        // check needed to know if there are any more bill splits to pay
                                                        orderFragment.percentageSplit(100f);
                                                    }
                                                }
                                            }else{
                                                //PAY AN ITEM OBJECT
                                                SubdivisionItem subitem = orderFragment.getOrderListAdapter().getSubdivisionItem();
                                                if(subitem==null)
                                                    subitem = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                                int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                                if (paymentType == 4) {
                                                    if (subitem.getMode() == -1) {
                                                        ((PaymentActivity) getContext()).openProcessCardPopupForItem();
                                                       /*((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_TOTAL_BILL);
                                                       ((PaymentActivity) getContext()).savePaidBill(1);*/
                                                    }
                                                    //((PaymentActivity) getContext()).openProcessCardPopupForItem();

                                                }else {
                                                    ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paidVar, 1);
                                                    if (subitem.getMode() == -1) {
                                                        ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_TOTAL_BILL);
                                                        ((PaymentActivity) getContext()).savePaidBill(1);
                                                    }
                                                }

                                            }
                                        }else{
                                            ((PaymentActivity)getContext()).openPayOtherPopup();
                                            // Toast.makeText(getContext(), "Please pay other separated bill first", Toast.LENGTH_SHORT).show();
                                        }


                                    } else { // split selected
                                        int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                        if (paymentType == 4) {
                                            ((PaymentActivity) getContext()).openProcessCardPopupForItem();

                                        } else {
                                            SubdivisionItem subitem = orderFragment.getOrderListAdapter().getSubdivisionItem();
                                            if (subitem.getMode() == 4) {
                                                ((PaymentActivity) getContext()).savePaidBill(1);
                                                ((PaymentActivity) getContext()).printFiscalPartial(paidVar, subitem.getOwed_money(), "Per Amount", subitem.getNumber_subdivision() + 1, 1);
                                                setIsActive(true);
                                                isActive = true;
                                                ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money());


                                            } else if (subitem.getMode() == 1) {
                                                //here to check if numbers cost equals to total cost to print total bill
                                                ((PaymentActivity) getContext()).savePaidBill(1);
                                                ((PaymentActivity) getContext()).printFiscalPartial(subitem.getOwed_money() * subitem.getNumber_subdivision(), paidVar, "Per Number", subitem.getNumber_subdivision() + 1, 1);
                                                setIsActive(true);
                                                isActive = true;
                                                ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money() * subitem.getNumber_subdivision());


                                            } else {
                                                //item payment
                                                //if(orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherArePaid()) {
                                                boolean c = orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherAreItemSelectAndPaid();
                                                if (orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherAreItemSelectAndPaid()) {
                                                    ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paidVar, 1);

                                                } else {
                                                    ((PaymentActivity) getContext()).printFiscalPartial(cost, paidVar, "Per Amount", subitem.getNumber_subdivision() + 1, 1);

                                                    //((PaymentActivity) getContext()).printFiscalBillWithNonFiscal(0.0f, paidVar, cost, "Partial Amount", (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()), 1);

                                                }
                                                //((PaymentActivity)getContext()).printItemBill(subitem, subitem.getOwed_money(), paidVar, 1);
                                                setIsActive(true);
                                                isActive = true;
                                                ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money());
                                                ((PaymentActivity) getContext()).setItemPaid(subitem);
                                                ((PaymentActivity) getContext()).savePaidBill(1);

                                                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter()
                                                        .getTotalItem();
                                                if (item.getOwed_money() == 0.0f) {
                                                    //total item cost = 0, all other item are
                                                   /*orderFragment.getOrderListAdapter()
                                                           .setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter()
                                                                   .getFirstItemAvaiable());*/
                                                    orderSubdivisionFragment.getSubdivisionAdapter()
                                                            .showFirstItemAvaiable();

                                                } else {
                                                    orderSubdivisionFragment.getSubdivisionAdapter()
                                                            .showItemOriginal();

                                                  /* orderFragment.getOrderListAdapter()
                                                           .setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter()
                                                                   .getTotalItem());*/
                                                }

                                            }

                                            float a = ((PaymentActivity) getContext()).getRemainingCostSubdivisionItem();
                                            if (((PaymentActivity) getContext()).getRemainingCostSubdivisionItem() <= 0.0f) {
                                                ((PaymentActivity) getContext()).savePaidBill(1);
                                               /*if(!((PaymentActivity) getContext()).checkIfOtherSplitBillAreItemOrPerson())
                                                ((PaymentActivity) getContext()).printNonFiscal();
                                               else
                                                   ((PaymentActivity) getContext()).savePaidSubdivisionBill(subitem, paymentType);*/
                                            }
                                            ((OrderFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("order"))
                                                    .setRemainingPercentageCost(cost);
                                            ((PaymentActivity) getContext()).setNormalKillOkButton();
                                            ((CalculatorFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("calc"))
                                                    .setMode(4);
                                            turnOnOffCalculator();
                                            // reset payment options buttons to original state.
                                            ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                                    .getAdapter().loadButtons(0);
                                            okPressedNTimes = 0;


                                        }
                                    }

                                } else { // Actual Total bill
                                    int paymentType = ((PaymentActivity)getContext()).getPaymentType();
                                    if(paymentType==4){
                                        ((PaymentActivity)getContext()).openProcessCardPopup();
                                    }else {
                                        communicator.printBill(paidVar, cost, actualCredit, 1);
                                        ((PaymentActivity) getContext()).setNormalKillOkButton();
                                        ((CalculatorFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("calc"))
                                                .setMode(4);
                                        turnOnOffCalculator();
                                        // reset payment options buttons to original state.
                                        ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                                .getAdapter().loadButtons(0);
                                        okPressedNTimes = 0;
                                    }
                                }
                            }
                            break;


                        case PAY_PARTIAL_MODE:
                            if(okPressedNTimes==2){
                                payPartial(1);
                                ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                        .getAdapter().setIsPartial(false);
                                ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                        .getAdapter().setActive(true);
                            }else if(okPressedNTimes==1){
                                String paidString = ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString();
                                StringBuffer s = new StringBuffer();
                                float cost = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));

                                float paid = 0.0f;
                                if(paidString.length()>0){
                                    paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString().replace(",", "."));
                                } else{
                                    paid = cost;
                                }
                                if(cost>paid){
                                    Toast.makeText(getContext(), R.string.cost_cant_be_greater_than_pay_amount, Toast.LENGTH_SHORT).show();
                                }else {
                                    paidVar = paid;
                                    s.append(String.format("%.2f", roundDecimal(paid - cost, 2)).replace(".", ","));//.replace(",", "."));
                                    ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText(s);
                                    if (amount.length() != 0) amount.delete(0, amount.length());
                                    shortcutPressed = false;
                                    okPressedNTimes = 2;
                                }
                            }else{
                                String paidString = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                if (paidString.length() > 0) {
                                    if(!paidString.equals("Skip Partial Payment")) {
                                        if (((PaymentActivity) getContext()).getIsCardFromActivity()) {
                                            float costC = orderFragment.returnRemaningTotal();
                                            float paidC = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                            if (costC > paidC) {

                                                ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
                                                ((PaymentActivity) getContext()).openProcessCardPopup();
                                                okPressedNTimes = 0;
                                            } else {
                                                CustomTextView myPaid = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv));
                                                myPaid.setText(R.string.skip_partial_payment);
                                                amount = new StringBuilder();
                                                okPressedNTimes = 0;
                                                Toast.makeText(getContext(), R.string.partial_cant_be_greater_than_cost, Toast.LENGTH_SHORT).show();
                                            }

                                        } else {
                                            float cost = orderFragment.returnRemaningTotal();
                                            float paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                            if (cost > paid) {
                                                myself.findViewById(R.id.cost_tv).setVisibility(View.VISIBLE);
                                                myself.findViewById(R.id.costValue_tv).setVisibility(View.VISIBLE);
                                                myself.findViewById(R.id.change_tv).setVisibility(View.VISIBLE);
                                                myself.findViewById(R.id.changeValue_tv).setVisibility(View.VISIBLE);
                                                myself.findViewById(R.id.vline).setVisibility(View.VISIBLE);
                                                myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.INVISIBLE);
                                                myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.INVISIBLE);


                                                ((CustomTextView) myself.findViewById(R.id.costValue_tv)).setText(String.valueOf(paid));
                                                ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText("");
                                                amount = new StringBuilder();
                                                okPressedNTimes = 1;
                                            } else {
                                                CustomTextView myPaid = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv));
                                                myPaid.setText(R.string.skip_partial_payment);
                                                amount = new StringBuilder();
                                                okPressedNTimes = 0;
                                                Toast.makeText(getContext(), R.string.partial_cant_be_greater_than_cost, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }else{
                                        CustomTextView myPaid = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv));
                                        myPaid.setText(R.string.skip_partial_payment);
                                        amount = new StringBuilder();
                                        okPressedNTimes = 0;

                                    }
                                }

                            }
                            break;


                        case DISCOUNT_MODE:
                            if(digitPressed) {
                                if (orderFragment.getTotalDiscount() || orderFragment.getPartialTotalDiscount()) {
                                    if (!discountMode) {
                                        //discount mode % or amount
                                        String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                        if (value1 != "" && value1.length() > 1) {
                                            if (value1.equals("GRATIS")) {
                                                ((PaymentActivity) getContext()).setTotalHomage();
                                                okPressedNTimes = 0;
                                                orderFragment.setTotalDiscount(false);
                                                setIsActive(true);
                                                turnOnOffCalculator();
                                                ((PaymentActivity) getContext()).hideSplitButton();
                                                okPressedNTimes = 0;
                                            } else {
                                                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                                if (item == null) {
                                                    float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                                    discountAmount = discount;
                                                    float costItem = orderFragment.returnRemaningTotal();
                                                    float homage = orderFragment.getElementsHomage();
                                                    if (costItem - homage > discountAmount) {
                                                        float total = costItem - discountAmount;
                                                        if (orderFragment.getTotalDiscount()) {
                                                            ((PaymentActivity) getContext()).setTotalDiscountAmount(discountAmount, discountAmount, false, false);
                                                            okPressedNTimes = 0;
                                                            orderFragment.setTotalDiscount(false);
                                                            mode = DEFAULT_MODE;
                                                            okPressedNTimes = 0;
                                                            ((PaymentActivity) getContext()).closeSetDiscount();
                                                        } else if (orderFragment.getPartialTotalDiscount()) {
                                                            ((PaymentActivity) getContext()).setPartialTotalDiscountAmount(discountAmount, discountAmount);
                                                            okPressedNTimes = 0;
                                                            orderFragment.setPartialTotalDiscount(false);
                                                        }
                                                    } else {
                                                        myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                        Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    if (item.getMode() == 2 || item.getMode() == 3) {
                                                        //set discount on total for item an person split bill
                                                        float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                                        discountAmount = discount;
                                                        float costItem = item.getOwed_money() - item.getDiscount();
                                                        float homage = orderFragment.getElementsHomageForItem(item);
                                                        if (costItem - homage > discountAmount) {
                                                            float total = costItem - discountAmount;
                                                            if (orderFragment.getTotalDiscount()) {

                                                                ((PaymentActivity) getContext()).setTotalDiscountAmount(discountAmount, discountAmount, false, false);
                                                                okPressedNTimes = 0;
                                                                orderFragment.setTotalDiscount(false);
                                                                mode = DEFAULT_MODE;
                                                                okPressedNTimes = 0;
                                                                ((PaymentActivity) getContext()).closeSetDiscount();
                                                            } else if (orderFragment.getPartialTotalDiscount()) {
                                                                ((PaymentActivity) getContext()).setPartialTotalDiscountAmount(discountAmount, discountAmount);
                                                                okPressedNTimes = 0;
                                                                orderFragment.setPartialTotalDiscount(false);
                                                            }
                                                        } else {
                                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                            Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                                        discountAmount = discount;
                                                        //COST ITEMS IS WRONG BECAUSE IN SPLIT BILL ALWAYS TAKE TOTAL MONEY
                                                        float costItem = item.getOwed_money() - item.getDiscount();
                                                        float homage = orderFragment.getElementsHomage();
                                                        if (costItem - homage > discountAmount) {
                                                            float total = costItem - discountAmount;
                                                            if (orderFragment.getTotalDiscount()) {
                                                                ((PaymentActivity) getContext()).setTotalDiscountAmount(discountAmount, discountAmount, false, false);
                                                                okPressedNTimes = 0;
                                                                orderFragment.setTotalDiscount(false);
                                                                mode = DEFAULT_MODE;
                                                                okPressedNTimes = 0;
                                                                ((PaymentActivity) getContext()).closeSetDiscount();
                                                            } else if (orderFragment.getPartialTotalDiscount()) {
                                                                ((PaymentActivity) getContext()).setPartialTotalDiscountAmount(discountAmount, discountAmount);
                                                                okPressedNTimes = 0;
                                                                orderFragment.setPartialTotalDiscount(false);
                                                            }
                                                        } else {
                                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                            Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                            Toast.makeText(getContext(), R.string.please_insert_value, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        float costItem = orderFragment.returnRemaningTotal();
                                        String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                        if (value1 != "") {
                                            float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                            discountAmount = discount;
                                            if (discount < 100) {
                                                float discountTotal = (costItem * discount) / 100;
                                                if (costItem >= discountTotal) {
                                                    float total = costItem - discountTotal;
                                                    if (orderFragment.getTotalDiscount()) {
                                                        ((PaymentActivity) getContext()).setTotalDiscountAmount(discountTotal, discountTotal, false, false);
                                                        okPressedNTimes = 0;
                                                        orderFragment.setTotalDiscount(false);
                                                        mode = DEFAULT_MODE;
                                                        okPressedNTimes = 0;
                                                        ((PaymentActivity) getContext()).closeSetDiscount();
                                                    } else if (orderFragment.getPartialTotalDiscount()) {
                                                        ((PaymentActivity) getContext()).setPartialTotalDiscountAmount(discountTotal, discountAmount);
                                                        okPressedNTimes = 0;
                                                        orderFragment.setPartialTotalDiscount(false);

                                                    }
                                                }
                                            } else {
                                                Toast.makeText(getContext(), R.string.percentage_cant_be_greater_than_100, Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                            Toast.makeText(getContext(), R.string.please_insert_value, Toast.LENGTH_SHORT).show();
                                        }

                                        mode = DEFAULT_MODE;
                                    }


                                } else {
                                    //discount sull'elemento
                                    if (!discountMode) {
                                        //euro
                                        if (okPressedNTimes == 0) {
                                            String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                            if (value1.length() > 1) {
                                                if (value1.equals("GRATIS")) {
                                                    //sto dando un omaggio
                                                    orderFragment.setHomageMethod(tmpPositionDiscount);
                                                    mode = DEFAULT_MODE;
                                                    okPressedNTimes = 0;
                                                    ((PaymentActivity) getContext()).closeSetDiscount();
                                                    ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                    setIsActive(true);
                                                    turnOnOffCalculator();
                                                } else {
                                                    CashButtonLayout productElement = orderFragment.getOrderListAdapter().getElement(tmpPositionDiscount);
                                                    if (productElement.getHomage() == 0) {
                                                        float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                                        discountAmount = discount;
                                                        float costItem = orderFragment.returnRemaningTotal();
                                                        float costItemToPay = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));
                                                        if (costItemToPay >= discount) {
                                                            SubdivisionItem item = ((PaymentActivity) getContext()).getSelectedItem();
                                                            if (item == null) {
                                                                if (discount < orderFragment.returnRemaningTotal()) {
                                                                    float dis = orderFragment.getOrderListAdapter().getElementDiscount(tmpPositionDiscount);
                                                                    if (dis == 0.0f)
                                                                        ((PaymentActivity) getContext()).setDiscountAmount(discount, discountAmount, false, tmpPositionDiscount, false);
                                                                    else
                                                                        ((PaymentActivity) getContext()).setDiscountAmount(dis, discountAmount, true, tmpPositionDiscount, false);

                                                                    okPressedNTimes = 1;
                                                                    setPayementShortcut();
                                                                    //per esettare la vista del discount su order adaopter
                                                                    ((PaymentActivity) getContext()).setTempPositionDiscount(-1);

                                                                } else {
                                                                    myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                                    Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();

                                                                }
                                                            } else {
                                                                float remainOther = ((PaymentActivity) getContext()).getRemainingForTotal();
                                                                if (item.getOwed_money() - item.getDiscount() - remainOther > discount) {
                                                                    float dis = orderFragment.getOrderListAdapter().getElementDiscount(tmpPositionDiscount);
                                                                    if (dis == 0.0f)
                                                                        ((PaymentActivity) getContext()).setDiscountAmount(discount, discountAmount, false, tmpPositionDiscount, false);
                                                                    else
                                                                        ((PaymentActivity) getContext()).setDiscountAmount(dis, discountAmount, true, tmpPositionDiscount, false);

                                                                    okPressedNTimes = 1;
                                                                    setPayementShortcut();
                                                                    ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                                    orderFragment.discountSet = false;
                                                                } else {
                                                                    myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                                    Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();


                                                                }
                                                            }
                                                        } else {
                                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                            Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                                        }
                                                        isActive = true;
                                                    } else {
                                                        orderFragment.setHomageMethod(tmpPositionDiscount);
                                                        mode = DEFAULT_MODE;
                                                        okPressedNTimes = 0;
                                                        ((PaymentActivity) getContext()).closeSetDiscount();
                                                        ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                        setIsActive(true);
                                                        turnOnOffCalculator();
                                                    }
                                                }
                                            } else {
                                                myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                Toast.makeText(getContext(), R.string.please_insert_value, Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            mode = DEFAULT_MODE;
                                            okPressedNTimes = 0;
                                            ((PaymentActivity) getContext()).closeSetDiscount();
                                            ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                        }
                                    } else {
                                        //perc
                                        if (okPressedNTimes == 0) {

                                            String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                            float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                            float costItem = orderFragment.returnRemaningTotal();
                                            float costItemToPay = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));

                                            discountAmount = discount;
                                            float discountTotal = (costItemToPay * discount) / 100;

                                            if (costItemToPay >= discountTotal) {
                                                if (discountTotal < orderFragment.returnRemaningTotal()) {
                                                    ((PaymentActivity) getContext()).setDiscountAmount(discountTotal, discountTotal, false, tmpPositionDiscount, false);
                                                    okPressedNTimes = 1;
                                                    setPayementShortcut();
                                                    // mode = RESERVATIONS_MODE;
                                                    ((PaymentActivity) getContext()).setTempPositionDiscount(-1);

                                                } else {
                                                    myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                    Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                            }
                                            isActive = true;
                                        } else {

                                            mode = DEFAULT_MODE;
                                            okPressedNTimes = 0;
                                            ((PaymentActivity) getContext()).closeSetDiscount();
                                        }
                                    }


                                }
                            }else{
                                myself.findViewById(R.id.main_calc).startAnimation(shake);
                                Toast.makeText(getContext(), R.string.please_insert_a_new_discount_value, Toast.LENGTH_SHORT).show();
                            }
                            break;


                        case MODIFY_DISCOUNT_MODE:
                            if(digitPressed){
                                if (orderFragment.getTotalDiscount() || orderFragment.getPartialTotalDiscount()) {
                                    //resetto il discount sul total
                                    if (!discountMode) {
                                        //discount mode % or amount
                                        String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                        if (value1 != "") {
                                            float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                            discountAmount = discount;
                                            float costItem = orderFragment.returnRemaningTotal();
                                            float homage = orderFragment.getElementsHomage();
                                            if (costItem/*-homage */>= discountAmount) {
                                                float total = costItem - discountAmount;
                                                if (orderFragment.getTotalDiscount()) {
                                                    ((PaymentActivity) getContext()).setTotalDiscountAmount(discountAmount, discountAmount, true, false);
                                                    okPressedNTimes = 0;
                                                    orderFragment.setTotalDiscount(false);
                                                    mode = DEFAULT_MODE;
                                                    okPressedNTimes = 0;
                                                    ((PaymentActivity) getContext()).closeSetDiscount();
                                                } else if (orderFragment.getPartialTotalDiscount()) {
                                                    ((PaymentActivity) getContext()).setPartialTotalDiscountAmount(discountAmount, discountAmount);
                                                    okPressedNTimes = 0;
                                                    orderFragment.setPartialTotalDiscount(false);
                                                }
                                            } else {
                                                myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                            Toast.makeText(getContext(), R.string.please_insert_value, Toast.LENGTH_SHORT).show();

                                        }
                                    } else {
                                        float costItem = orderFragment.returnRemaningTotal();
                                        String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                        if (value1 != "") {
                                            float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                            discountAmount = discount;
                                            if (discount < 100) {
                                                float discountTotal = (costItem * discount) / 100;
                                                if (costItem >= discountTotal) {
                                                    float total = costItem - discountTotal;
                                                    if (orderFragment.getTotalDiscount()) {
                                                        ((PaymentActivity) getContext()).setTotalDiscountAmount(discountTotal, discountAmount, true, false);
                                                        okPressedNTimes = 0;
                                                        orderFragment.setTotalDiscount(false);
                                                        mode = DEFAULT_MODE;
                                                        okPressedNTimes = 0;
                                                        ((PaymentActivity) getContext()).closeSetDiscount();
                                                    } else if (orderFragment.getPartialTotalDiscount()) {
                                                        ((PaymentActivity) getContext()).setPartialTotalDiscountAmount(discountTotal, discountAmount);
                                                        okPressedNTimes = 0;
                                                        orderFragment.setPartialTotalDiscount(false);

                                                    }
                                                }
                                            } else {
                                                Toast.makeText(getContext(), R.string.percentage_cant_be_greater_than_100, Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                            Toast.makeText(getContext(), R.string.please_insert_value, Toast.LENGTH_SHORT).show();
                                        }

                                        mode = DEFAULT_MODE;
                                    }
                                }else{
                                    if (!discountMode) {
                                        //resetto discount sull'elemento in euro
                                        if(okPressedNTimes == 0) {

                                            String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                            String myValue = value1.substring(0, value1.length() - 1);
                                            if(myValue.equals("GRATI")){
                                                CashButtonLayout productElement = orderFragment.getOrderListAdapter().getElement(tmpPositionDiscount);
                                                if(productElement.getHomage()==0) {
                                                    orderFragment.setHomageMethod(tmpPositionDiscount);
                                                    mode = DEFAULT_MODE;
                                                    okPressedNTimes = 0;
                                                    ((PaymentActivity) getContext()).closeSetDiscount();
                                                    ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                    setIsActive(true);
                                                    turnOnOffCalculator();

                                                }else{
                                                    myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                    Toast.makeText(getContext(), R.string.already_gratis, Toast.LENGTH_SHORT).show();

                                                }

                                            }else {
                                                CashButtonLayout productElement = orderFragment.getOrderListAdapter().getElement(tmpPositionDiscount);
                                                if(productElement.getHomage()==0) {
                                                    float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                                    discountAmount = discount;
                                                    float costItemToPay = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));
                                                    if (costItemToPay >= discount) {
                                                        float test2 = orderFragment.getOrderListAdapter().getElementPrice(tmpPositionDiscount);
                                                        if (discount < test2) {
                                                            ((PaymentActivity) getContext()).setDiscountAmount(tmpDiscount, discountAmount, true, tmpPositionDiscount, false);
                                                            okPressedNTimes = 1;
                                                            setPayementShortcut();
                                                            okPressedNTimes = 0;
                                                            mode = DEFAULT_MODE;
                                                            ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                            ((PaymentActivity) getContext()).closeSetDiscount();
                                                        } else {
                                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                            Toast.makeText(getContext(), R.string.value_is_already_zero, Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                        Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                                    }
                                                    isActive = true;
                                                }else{
                                                    float discount = Float.parseFloat(value1.substring(0, value1.length() - 1).replace(",", "."));
                                                    if(discount>0){
                                                        myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                        Toast.makeText(getContext(), R.string.already_homage, Toast.LENGTH_SHORT).show();

                                                    }else {
                                                        orderFragment.setHomageMethod(tmpPositionDiscount);
                                                        mode = DEFAULT_MODE;
                                                        okPressedNTimes = 0;
                                                        ((PaymentActivity) getContext()).closeSetDiscount();
                                                        ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                        setIsActive(true);
                                                        turnOnOffCalculator();
                                                    }
                                                }
                                            }
                                        }else{
                                            mode = DEFAULT_MODE;
                                            okPressedNTimes = 0;
                                            ((PaymentActivity) getContext()).closeSetDiscount();

                                        }
                                    }else{
                                        //resetto discount sull'elemento in %
                                        String value1 = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                        if(okPressedNTimes == 0) {
                                            String val = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                            String myValue = val.substring(0, val.length() - 1);
                                            if(myValue.equals("GRATI")){
                                                CashButtonLayout productElement = orderFragment.getOrderListAdapter().getElement(tmpPositionDiscount);
                                                if(productElement.getHomage()==0) {
                                                    orderFragment.setHomageMethod(tmpPositionDiscount);
                                                    mode = DEFAULT_MODE;
                                                    okPressedNTimes = 0;
                                                    ((PaymentActivity) getContext()).closeSetDiscount();
                                                    ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                    setIsActive(true);
                                                    turnOnOffCalculator();
                                                }else{
                                                    myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                    Toast.makeText(getContext(), R.string.already_homage, Toast.LENGTH_SHORT).show();

                                                }

                                            }else {
                                                float discount = Float.parseFloat(val.substring(0, value1.length() - 1).replace(",", "."));
                                                float costItem = orderFragment.returnRemaningTotal();
                                                float costItemToPay = orderFragment.getOrderListAdapter().getElementPrice(tmpPositionDiscount);
                                                float discountTotal = (costItemToPay * discount) / 100;
                                                discountAmount = discountTotal;
                                                CashButtonLayout productElement = orderFragment.getOrderListAdapter().getElement(tmpPositionDiscount);
                                                if(productElement.getHomage()==0) {
                                                    if (costItemToPay >= discountTotal) {
                                                        float test2 = orderFragment.getOrderListAdapter().getElementPrice(tmpPositionDiscount);
                                                        if (discountTotal < test2) {
                                                            ((PaymentActivity) getContext()).setDiscountAmount(tmpDiscount, discountAmount, true, tmpPositionDiscount, false);
                                                            okPressedNTimes = 1;
                                                            setPayementShortcut();
                                                            okPressedNTimes = 0;
                                                            mode = DEFAULT_MODE;
                                                            ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                            ((PaymentActivity) getContext()).closeSetDiscount();
                                                        } else {
                                                            myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                            Toast.makeText(getContext(), R.string.value_is_already_zero, Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                        Toast.makeText(getContext(), R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT).show();
                                                    }
                                                    isActive = true;
                                                }else{
                                                    if(discount>0){
                                                        myself.findViewById(R.id.main_calc).startAnimation(shake);
                                                        Toast.makeText(getContext(), R.string.already_homage, Toast.LENGTH_SHORT).show();

                                                    }else {
                                                        orderFragment.setHomageMethod(tmpPositionDiscount);
                                                        mode = DEFAULT_MODE;
                                                        okPressedNTimes = 0;
                                                        ((PaymentActivity) getContext()).closeSetDiscount();
                                                        ((PaymentActivity) getContext()).setTempPositionDiscount(-1);
                                                        setIsActive(true);
                                                        turnOnOffCalculator();
                                                    }
                                                }
                                            }
                                        }else{
                                            mode = DEFAULT_MODE;
                                            okPressedNTimes = 0;
                                            ((PaymentActivity) getContext()).closeSetDiscount();
                                        }

                                    }
                                }
                            }else{
                                myself.findViewById(R.id.main_calc).startAnimation(shake);
                                Toast.makeText(getContext(), R.string.please_insert_a_new_discount_value, Toast.LENGTH_SHORT).show();
                            }

                            Toast.makeText(getContext(), R.string.reset_discount, Toast.LENGTH_SHORT).show();
                            break;


                        case PAY_TICKET_MODE :
                            if(((PaymentActivity)getContext()).invoiceBill){
                                Toast.makeText(getContext(), R.string.not_allowed_for_invoice, Toast.LENGTH_SHORT).show();
                            }else {
                                String ticketAmount = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                if (ticketAmount.length() > 0) {
                                    //ticket amount is not 0
                                    float cost = orderFragment.returnRemaningTotal();
                                    float paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                    if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() > 1) {
                                        if (orderFragment.getOrderListAdapter().getSubdivisionItem() == null) { // => no split selected
                                            if (orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherArePaid()) {
                                                if (!orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherPersonOrItem()) {
                                                    // qua controllo se c'è solo un number non pagato
                                                    if (orderSubdivisionFragment.getSubdivisionAdapter().checkIfThereIsPernumber()) {

                                                        SubdivisionItem subitem = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                                        float itemsDiscount = orderSubdivisionFragment.getSubdivisionAdapter().returnDiscountForItem(subitem.getItems());
                                                        if (paid < subitem.getOwed_money()) {
                                                            setMode(PAY_LEFT_PAYMENT);
                                                            //add leftPayment object to leftPayments array
                                                            int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                                            LeftPayment leftPayment = new LeftPayment(2, subitem.getOwed_money() - subitem.getDiscount() - itemsDiscount, paid, subitem.getOwed_money() - subitem.getDiscount() - itemsDiscount, subitem);
                                                            leftPayments.add(leftPayment);
                                                            //open popup to select left payment type
                                                            ((PaymentActivity) getContext()).openPopupForPaymentLeft(leftPayments);
                                                            Toast.makeText(getContext(), R.string.total_bill_error_in_amount, Toast.LENGTH_SHORT).show();
                                                            break;
                                                        } else {
                                                            SubdivisionItem pernumber = orderSubdivisionFragment.getSubdivisionAdapter().getPerNumberItem();
                                                            ((PaymentActivity) getContext()).savePaidSubdivisionBill(pernumber, 2);
                                                            orderSubdivisionFragment.getSubdivisionAdapter().setItemPaid(pernumber);
                                                            orderSubdivisionFragment.getSubdivisionAdapter().notifyDataSetChanged();
                                                            ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paidVar, 2);
                                                            ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_TOTAL_BILL);
                                                            ((PaymentActivity) getContext()).savePaidBill(1);
                                                        }
                                                    } else {
                                                        orderFragment.getOrderListAdapter()
                                                                .setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter()
                                                                        .addElement(PERCENTAGE_MODE, cost, 100.0f, true, null));
                                                        ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
                                                        ((PaymentActivity) getContext()).printFiscalBillWithNonFiscal(0.0f, paidVar, cost, "Partial Amount", (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()), 2);

                                                        if (orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost() - cost > 0) {
                                                            // check needed to know if there are any more bill splits to pay
                                                            orderFragment.percentageSplit(100f);
                                                        }
                                                    }
                                                    if (orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost() - cost > 0) {
                                                        // check needed to know if there are any more bill splits to pay
                                                        orderFragment.percentageSplit(100f);
                                                    }
                                                    ((PaymentActivity) getContext()).setNormalKillOkButton();
                                                    ((CalculatorFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("calc"))
                                                            .setMode(4);
                                                    turnOnOffCalculator();
                                                    // reset payment options buttons to original state.
                                                    ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                                            .getAdapter().loadButtons(0);
                                                } else {
                                                    //PAY AN ITEM OBJECT
                                                    SubdivisionItem subitem = orderFragment.getOrderListAdapter().getSubdivisionItem();
                                                    if (subitem == null)
                                                        subitem = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                              /* ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paid, 2);
                                               ((PaymentActivity) getContext()).savePaidBill(4);
*/
                                                    if (paid < subitem.getOwed_money()) {
                                                        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                                        if (item == null)
                                                            item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                                        //here you pay with left payement system
                                                        //open popup left
                                                        setMode(PAY_LEFT_PAYMENT);
                                                        //add leftPayment object to leftPayments array
                                                        int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                                        LeftPayment leftPayment = new LeftPayment(2, cost, paid, cost, item);
                                                        leftPayments.add(leftPayment);
                                                        //open popup to select left payment type
                                                        ((PaymentActivity) getContext()).openPopupForPaymentLeft(leftPayments);
                                                        Toast.makeText(getContext(), R.string.total_bill_error_in_amount, Toast.LENGTH_SHORT).show();
                                                    } else {

                                                        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                                        if (item == null)
                                                            item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                                        ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paid, 2);
                                                        setIsActive(true);
                                                        isActive = true;
                                                        ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                        ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money());
                                                        ((PaymentActivity) getContext()).savePaidBill(4);

                                                        ((PaymentActivity) getContext()).setNormalKillOkButton();
                                                        ((CalculatorFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("calc"))
                                                                .setMode(4);
                                                        turnOnOffCalculator();
                                                        // reset payment options buttons to original state.
                                                        ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                                                .getAdapter().loadButtons(0);


                                                    }
                                                }
                                            } else {
                                                ((PaymentActivity) getContext()).openPayOtherPopup();
                                                // Toast.makeText(getContext(), "Please pay other separated bill first", Toast.LENGTH_SHORT).show();
                                            }



                                        } else { // split selected
                                            SubdivisionItem subitem = orderFragment.getOrderListAdapter().getSubdivisionItem();
                                            paidVar = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                            float usedMoney = 0.0f;
                                            if (subitem.getMode() == 1)
                                                usedMoney = subitem.getOwed_money() * subitem.getNumber_subdivision();
                                            else usedMoney = subitem.getOwed_money();
                                            if (usedMoney == paidVar) {
                                                if (subitem.getMode() == 4) {
                                                    ((PaymentActivity) getContext()).savePaidBill(4);
                                                    ((PaymentActivity) getContext()).printFiscalPartial(paidVar, subitem.getOwed_money(), "Per Amount", subitem.getNumber_subdivision() + 1, 2);
                                                    setIsActive(true);
                                                    isActive = true;
                                                    ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                    ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money());
                                                } else if (subitem.getMode() == 1) {
                                                    //here to check if numbers cost equals to total cost to print total bill
                                                    ((PaymentActivity) getContext()).savePaidBill(4);
                                                    ((PaymentActivity) getContext()).printFiscalPartial(subitem.getOwed_money() * subitem.getNumber_subdivision(), paidVar, "Per Number", subitem.getNumber_subdivision() + 1, 2);
                                                    setIsActive(true);
                                                    isActive = true;
                                                    ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                    ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money() * subitem.getNumber_subdivision());
                                                } else {
                                                    //item payment

                                                    ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paidVar, 2);
                                                    setIsActive(true);
                                                    isActive = true;
                                                    ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                    ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money());
                                                    ((PaymentActivity) getContext()).savePaidBill(4);
                                                }

                                                if (((PaymentActivity) getContext()).getRemainingCostSubdivisionItem() == 0.0f) {
                                                    ((PaymentActivity) getContext()).savePaidBill(4);
                                                    //((PaymentActivity) getContext()).printNonFiscal();
                                                }
                                                ((PaymentActivity) getContext()).setNormalKillOkButton();
                                                ((CalculatorFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("calc"))
                                                        .setMode(4);
                                                turnOnOffCalculator();
                                                // reset payment options buttons to original state.
                                                ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                                        .getAdapter().loadButtons(0);

                                            } else {
                                                //TODO LEFT PAYMENT FOR TICKET
                                                //cost is bigger than paid
                                                if (paidVar <= usedMoney) {
                                                    SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                                    if (item == null)
                                                        item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                                    //here you pay with left payement system
                                                    //open popup left
                                                    setMode(PAY_LEFT_PAYMENT);
                                                    //add leftPayment object to leftPayments array
                                                    int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                                    LeftPayment leftPayment = new LeftPayment(2, item.getOwed_money(), paidVar, item.getOwed_money(), item);
                                                    leftPayments.add(leftPayment);
                                                    //open popup to select left payment type
                                                    ((PaymentActivity) getContext()).openPopupForPaymentLeft(leftPayments);
                                                    Toast.makeText(getContext(), R.string.total_bill_error_in_amount, Toast.LENGTH_SHORT).show();
                                                } else {

                                                    SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                                    if (item == null)
                                                        item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                                    ((PaymentActivity) getContext()).printItemBill(subitem, subitem.getOwed_money(), paidVar, 2);
                                                    setIsActive(true);
                                                    isActive = true;
                                                    ((PaymentActivity) getContext()).setIsActivePayementsOptions(true);
                                                    ((PaymentActivity) getContext()).setTotalSubdivisionPaid(subitem.getOwed_money());
                                                    ((PaymentActivity) getContext()).savePaidBill(4);

                                                    ((PaymentActivity) getContext()).setNormalKillOkButton();
                                                    ((CalculatorFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("calc"))
                                                            .setMode(4);
                                                    turnOnOffCalculator();
                                                    // reset payment options buttons to original state.
                                                    ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                                            .getAdapter().loadButtons(0);
                                                }
                                            }
                                        }

                                    } else {

                                        if (paid >= cost - actualCredit) {
                                            //sto pagando con i ticket una cifra maggiore e devo dare il resto
                                            // Actual Total bill
                                            float credit = ((paid * 1000) - ((cost - actualCredit) * 1000)) / 1000;
                                            communicator.printBill(paid, cost - actualCredit, credit, 2);

                                            ((PaymentActivity) getContext()).setNormalKillOkButton();
                                            turnOnOffCalculator();
                                            // reset payment options buttons to original state.
                                            ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                                    .getAdapter().loadButtons(0);
                                            okPressedNTimes = 0;

                                            ((PaymentActivity) getContext()).savePaidBill(4);

                                        } else {
                                            //creo un partial con quei ticket

                                            SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                            if (item == null)
                                                item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                            //here you pay with left payement system
                                            //open popup left
                                            setMode(PAY_LEFT_PAYMENT);
                                            //add leftPayment object to leftPayments array
                                            int paymentType = 2;
                                            LeftPayment leftPayment = new LeftPayment(paymentType, cost, paid, cost, item);
                                            leftPayments.add(leftPayment);
                                            //open popup to select left payment type
                                            ((PaymentActivity) getContext()).showPaymentButton();
                                            ((PaymentActivity) getContext()).openPopupForPaymentLeft(leftPayments);

                                        }
                                    }
                                } else {

                                }
                            }
                            break;


                        case INSERT_CREDIT_MODE :
                            if (okPressedNTimes == 1)
                            {
                                float costItem = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                                if (costItem < cost)
                                {
                                    okPressedNTimes = 0;
                                    orderFragment.setTotalCost(cost - costItem);
                                    orderFragment.setPrice(cost - costItem);
                                    setActualCredit(costItem);
                                    setPayementShortcut();
                                    mode = DEFAULT_MODE;

                                    ((PaymentActivity) getContext()).setNormalKillOkButton();
                                    turnOnOffCalculator();
                                    // reset payment options buttons to original state.
                                    ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                                            .getAdapter().loadButtons(0);
                                }

                                else
                                {
                                    if(costItem==cost)
                                    {
                                        if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() > 1) {
                                            ((PaymentActivity) getContext()).printFiscalBillWithNonFiscal(
                                                    0.0f,0.0f, 0.0f, "Partial Amount", (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()),1);
                                        }

                                        else
                                        { communicator.printBill(0.0f, 0.0f, 0.0f, 1); }
                                    }

                                    else if(costItem>cost)
                                    {
                                        if (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount() > 0)
                                        {
                                            float left = costItem - cost;
                                            ((PaymentActivity) getContext()).printFiscalBillWithNonFiscal(
                                                    ((left)*10)/10,cost, cost, "Partial Amount", (orderSubdivisionFragment.getSubdivisionAdapter().getItemCount()),1);

                                        }

                                        else
                                        { ((PaymentActivity) getContext()).printBill(0.0f, 0.0f, (costItem - cost), 1); }
                                    }
                                }
                            }

                            else
                            {
                                String numberId = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                                if (numberId.length() > 0) {
                                    okPressedNTimes = 1;
                                    if(!numberId.equals("NO CREDIT"))
                                    {
                                        if (StaticValue.blackbox)
                                        {
                                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                            params.add(new BasicNameValuePair("billId", numberId));
                                            ((PaymentActivity) getContext()).callHttpHandler("/getCredit", params);
                                        }

                                        else
                                        {
                                            float value1 = ((PaymentActivity) getContext()).getCreditValue(Integer.parseInt(numberId));
                                            if (value1 > 0)
                                            {
                                                // return true if the numberId is not present in the creditId element in PaymentActivity
                                                if (((PaymentActivity) getContext()).setCreditId(Integer.parseInt(numberId)))
                                                {
                                                    ((CustomTextView) myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.credit_amount);
                                                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText(String.valueOf(value1));
                                                }

                                                else
                                                { okPressedNTimes = 0; }
                                            }

                                            else
                                            {
                                                okPressedNTimes = 0;
                                                amount = new StringBuilder();
                                                CustomTextView creditInfo = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv));
                                                ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText(R.string.no_credit);
                                            }
                                        }
                                    }

                                    else
                                    {
                                        okPressedNTimes = 0;
                                        amount = new StringBuilder();
                                        CustomTextView creditInfo = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv));
                                        myself.findViewById(R.id.main_calc).startAnimation(shake);
                                    }
                                }
                            }
                            break;


                        case ELEMENT_ITEM_SPLIT :
                            String divisionValue = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
                            if(divisionValue!="") {
                                float myValue  = Float.parseFloat(divisionValue.replace(",", "."));

                                if(myValue>1) {
                                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                                    ((PaymentActivity) getContext()).setElementItemSplitValue(Integer.parseInt(divisionValue), getTmpPositionDiscount());
                                    amount = new StringBuilder();
                                }else{
                                    myself.findViewById(R.id.main_calc).startAnimation(shake);
                                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                                    amount = new StringBuilder();
                                    Toast.makeText(getContext(), R.string.no_split_for_one, Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;


                        case PAY_LEFT_PAYMENT :
                            if (okPressedNTimes == 0) {
                                StringBuffer s = new StringBuffer();
                                String paidString = ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString();
                                if(paidString.equals(",")){

                                }else {
                                    float cost = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));
                                    float paid = cost;
                                    if (paidString.length() != 0) {
                                        paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString().replace(",", "."));
                                    }
                                    if (cost <= paid) {
                                        paidVar = paid;
                                        s.append(String.format("%.2f", roundDecimal(paid - cost, 2)).replace(".", ","));//.replace(",", "."));
                                        ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText(s);
                                        if (amount.length() != 0) amount.delete(0, amount.length());
                                        shortcutPressed = false;
                                        okPressedNTimes = 1;
                                    } else {
                                        SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                        if (item == null)
                                            item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                        //here you pay with left payement system
                                        //open popup left
                                        item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                        setMode(PAY_LEFT_PAYMENT);
                                        //add leftPayment object to leftPayments array
                                        if(leftPayments.size()>0) {
                                            LeftPayment leftPayment = new LeftPayment(((PaymentActivity) getContext()).getPaymentType(), cost, paid, leftPayments.get(0).getTotalCost(), item);
                                            leftPayments.add(leftPayment);
                                        }else{
                                            LeftPayment leftPayment = new LeftPayment(((PaymentActivity) getContext()).getPaymentType(), cost, paid,cost, item);
                                            leftPayments.add(leftPayment);

                                        }
                                        //open popup to select left payment type
                                        ((PaymentActivity) getContext()).openPopupForPaymentLeft(leftPayments);
                                        Toast.makeText(getContext(), R.string.total_bill_error_in_amount, Toast.LENGTH_SHORT).show();

                                    }
                                }

                            } else {
                                //PRINT ALL THE BILL
                                int paymentType = ((PaymentActivity)getContext()).getPaymentType();
                                float cost = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));
                                SubdivisionItem item = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                if(item==null)   item = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem();
                                LeftPayment leftPayment = new LeftPayment(((PaymentActivity)getContext()).getPaymentType(), cost, paidVar, leftPayments.get(0).getTotalCost(), item);
                                leftPayments.add(leftPayment);
                                switch(paymentType){
                                    case 1 :
                                        //case cash
                                        ((PaymentActivity) getContext()).printLeftPayment();
                                        endLeftPayment();
                                        break;
                                    case 2 :
                                        break;
                                    case 3 :
                                        break;
                                    case 4 :
                                        //case credit card
                                        if(paidVar> cost) {
                                            leftPayments.get(leftPayments.size()-1).setPaid(leftPayments.get(leftPayments.size()-1).getCost());
                                        }
                                        ((PaymentActivity) getContext()).openProcessLeftCreditCardPopup(item);
                                        break;
                                    case 5 :
                                        break;
                                    case 6 :
                                        break;
                                    default :
                                        Toast.makeText(getContext(), R.string.cant_define_payment_type_please_ask_t, Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                SubdivisionItem totalitem = orderSubdivisionFragment.getSubdivisionAdapter()
                                        .getTotalItem();
                                if (totalitem.getOwed_money() == 0.0f) {
                                    orderSubdivisionFragment.getSubdivisionAdapter()
                                            .showFirstItemAvaiable();

                                } else {
                                    orderSubdivisionFragment.getSubdivisionAdapter()
                                            .showItemOriginal();
                                }
                                orderSubdivisionFragment.getSubdivisionAdapter().setTotalSubdivisionPaid(item.getOwed_money());

                                // float delta = orderSubdivisionFragment.getSubdivisionAdapter().getRemainingCost();
                              /* if(delta<=0){
                                   ((PaymentActivity) getContext()).savePaidBill(paymentType);
                               }*/
                            }
                            break;


                        case INSERT_FIDELITY_MODE :
                            if (okPressedNTimes == 0)
                            {
                                String creditAmountInput = ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString();

                                SubdivisionItem currentItem = orderSubdivisionFragment.getSubdivisionAdapter().getSelectedItem();
                                if (currentItem == null)
                                    { currentItem = orderSubdivisionFragment.getSubdivisionAdapter().getTotalItem(); }

                                float itemTotal = currentItem.getOwed_money();
                                float billTotal = orderFragment.getTotalCost();

                                // TODO what about comma?
                                if (!creditAmountInput.matches("[0-9]+"))
                                    { Toast.makeText(getActivity(), "Insert a valid amount of fidelity credit", Toast.LENGTH_LONG).show(); }

                                else if (selectedClientPayFidelity == null)
                                    { Toast.makeText(getActivity(), "Please, select a client", Toast.LENGTH_LONG).show(); }

                                // if a client is given, detract the amount of credits from it's fidelity account
                                else
                                {
                                    // this var will be used at the end of this else clause
                                    int creditUsed = -1;

                                    DatabaseAdapter dbA = new DatabaseAdapter(getActivity());
                                    Fidelity fidelity = dbA.fetchFidelityById(selectedClientPayFidelity.getFidelity_id());

                                    float creditPaid = Float.parseFloat(creditAmountInput);

                                    if (creditPaid > fidelity.getValue())
                                        { Toast.makeText(getActivity(), "This client does not have enough Fidelity Credit", Toast.LENGTH_LONG).show(); }

                                    // there are enough credit to pay, but the amount of credits given is lower than the total bill cost
                                    // thus let the client pay, then shift to left payment mode
                                    else if (creditPaid < itemTotal)
                                    {
                                        creditUsed = (int) creditPaid;

                                        // left payment is not allowed in invoice mode
                                        if (((PaymentActivity) getContext()).invoiceBill)
                                            { Toast.makeText(getContext(), R.string.not_allowed_for_invoice, Toast.LENGTH_SHORT).show(); }

                                        // subtract the amount of credits that were used to pay, then move to the left payment mode
                                        else
                                        {
                                            //here you pay with left payement system
                                            //open popup left
                                            setMode(PAY_LEFT_PAYMENT);

                                            //add leftPayment object to leftPayments array
                                            int paymentType = ((PaymentActivity) getContext()).getPaymentType();
                                            LeftPayment leftPayment = new LeftPayment(paymentType, itemTotal, creditPaid, billTotal, currentItem);

                                            leftPayments.add(leftPayment);

                                            //open popup to select left payment type
                                            ((PaymentActivity) getContext()).openPopupForPaymentLeft(leftPayments);

                                        }
                                    }

                                    // if the amount of credit given is sufficient to pay the bill,
                                    // just reset some var and display elements.
                                    else if (creditPaid >= itemTotal)
                                    {
                                        creditUsed = (int) itemTotal;

                                        ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText("OK!");

                                        currentItem.setPaid(true);

                                        if (amount.length() != 0) amount.delete(0, amount.length());

                                        shortcutPressed = false;
                                        okPressedNTimes = 1;
                                    }


                                    // now finally subtract the amount of credits from the fidelity account
                                    // if all check passed, creditUsed will be > -1
                                    if (creditUsed > 0)
                                    {
                                        if (StaticValue.blackbox)
                                        {
                                            ArrayList<NameValuePair> params = new ArrayList<>();
                                            params.add(new BasicNameValuePair("clientId", String.valueOf(selectedClientPayFidelity.getClient_id())));
                                            params.add(new BasicNameValuePair("amount", String.valueOf(creditUsed)));

                                            ((PaymentActivity) getContext()).callHttpHandler("/subtractFidelityCredit", params);

                                        }

                                        else
                                        {
                                            dbA.updateFidelityPoint(
                                                    fidelity.getValue() - creditUsed,
                                                    fidelity.getEarned(),
                                                    fidelity.getUsed() + creditUsed,
                                                    fidelity.getId());
                                        }
                                    }

                                    // reset some visual elements, that where changed in fidelity mode
                                    ((CustomTextView) myself.findViewById(R.id.cost_tv)).setText(R.string.cost);
                                    ((CustomTextView) myself.findViewById(R.id.costValue_tv)).setTextSize(32);
                                    ((CustomTextView) myself.findViewById(R.id.costValue_tv)).setText(String.valueOf(itemTotal - creditUsed));

                                    ((CustomTextView) myself.findViewById(R.id.change_tv)).setText(R.string.change);
                                    ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText(String.valueOf(0));

                                }

                            }

                            // at the second press of OK button,
                            // check if all other items are paid
                            // if not, just reset
                            else if (!orderSubdivisionFragment.getSubdivisionAdapter().checkIfOtherArePaid())
                            {
                                okPressedNTimes = 0;

                                ((PaymentActivity) getContext()).setNormalKillOkButton();
                                ((PaymentActivity) getContext()).resetOpacityForPayementButtons();
                                ((PaymentActivity) getContext()).activatePaymentButtons();

                                turnOnOffCalculator();
                                setMode(DEFAULT_MODE);
                            }

                            else
                            {
                                // TODO payment type
                                ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_TOTAL_BILL);
                                ((PaymentActivity) getContext()).savePaidBill(8); // FIDELITY PAYMENT

                                communicator.printBill(paidVar, cost, actualCredit, 8);

                                ((PaymentActivity) getContext()).setNormalKillOkButton();

                                ((CalculatorFragment) ((FragmentActivity) getContext())
                                        .getSupportFragmentManager()
                                        .findFragmentByTag("calc"))
                                        .setMode(4);

                                turnOnOffCalculator();

                                // reset payment options buttons to original state.
                                ((OptionsFragment) ((FragmentActivity) getContext())
                                        .getSupportFragmentManager()
                                        .findFragmentByTag("options"))
                                        .getAdapter()
                                        .loadButtons(0);

                                okPressedNTimes = 0;

                            }
                            break;
                    }
                }
            }
        });
    }


    /**
     * activate or deactivate the calculator
     * also, handle the display of the calculator, based on which mode is set
     */
    public void turnOnOffCalculator()
    {
        if (!isActive)
        {
            setupXOK();

            orderFragment = (OrderFragment)((FragmentActivity)getContext()).getSupportFragmentManager().findFragmentByTag("order");

            orderSubdivisionFragment = (OrderSubdivisionFragment)((FragmentActivity)getContext()).getSupportFragmentManager().findFragmentByTag("orderSubdivision");

            myself.findViewById(R.id.black_cloth).setVisibility(View.GONE);

            switch (mode)
            {
                case NUMBER_MODE:

                case PERCENTAGE_MODE:

                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    if( mode == PERCENTAGE_MODE) {
                        ((CustomTextView) myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.insert_percentage_);

                    }else if( mode == NUMBER_MODE){
                        ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.insert_number_);
                        setNOShortCut();
                    }
                    else if( mode == TIPS_MODE)((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.tips_);
                    else  if( mode == PAY_PARTIAL_MODE)
                        ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.partial_amount);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");

                    break;

                case DISCOUNT_MODE:
                    setModifyDiscountShorcut();
                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.insert_discount);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("€");
                    okPressedNTimes=0;
                    break;

                case MODIFY_DISCOUNT_MODE:
                    setModifyDiscountShorcut();
                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.insert_discount);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");

                    break;

                case PAY_PARTIAL_MODE:
                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.partial_amount);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                    break;

                case PAY_TICKET_MODE:
                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.ticket_amount_all_caps);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                    break;

                case INSERT_CREDIT_MODE:
                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.credit_id);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                    break;

                case TIPS_MODE :
                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.tips_all_caps);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                    break;

                case ELEMENT_ITEM_SPLIT:
                    setElementItemSplit();
                    myself.findViewById(R.id.cost_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.costValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.change_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.changeValue_tv).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.vline).setVisibility(View.INVISIBLE);
                    myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.VISIBLE);
                    myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.VISIBLE);
                    ((CustomTextView)myself.findViewById(R.id.numberInsertionInfo_tv)).setText(R.string.insert_split_quantity_all_caps);
                    ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText("");
                    okPressedNTimes=0;

                case INSERT_FIDELITY_MODE:
                    setElementItemSplit();

                    okPressedNTimes=0;

                    setupPayWithFidelity();


                default:
                    break;
            }
            isActive = !isActive;
        }

        else
        {
            okPressedNTimes=0;

            myself.findViewById(R.id.black_cloth).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.cost_tv).setVisibility(View.VISIBLE);
            ((CustomTextView) myself.findViewById(R.id.cost_tv)).setText(R.string.cost);
            myself.findViewById(R.id.costValue_tv).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.change_tv).setVisibility(View.VISIBLE);
            ((CustomTextView) myself.findViewById(R.id.change_tv)).setText(R.string.amount);
            myself.findViewById(R.id.changeValue_tv).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.vline).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.GONE);
            myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.GONE);
            isActive = !isActive;

            if (amount.length() != 0) amount.delete(0, amount.length());

            ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());

            switch(mode)
            {
                case NUMBER_MODE:
                    setPayementShortcut();
                    break;
                case PERCENTAGE_MODE:
                    setMode(0);
                    break;
                case PAY_PARTIAL_MODE:
                case PAY_TOTAL_MODE:
                case DISCOUNT_MODE:
                    okPressedNTimes=0;
                    communicator.activateFunction(PaymentActivity.DISCOUNT_MODE_OFF, null, 0.0f);
                    break;
                case MODIFY_DISCOUNT_MODE:
                    communicator.activateFunction(PaymentActivity.DISCOUNT_MODE_OFF, null, 0.0f);
                    break;
                case ELEMENT_ITEM_SPLIT:
                    setPayementShortcut();
                    break;
                default:
                    communicator.activateFunction(PaymentActivity.CALCULATOR_OFF, null, 0.0f);
                    break;
            }
        }
        ((PaymentActivity)getContext()).isCalculatorOn = isActive;
    }


    public void turnOffCalculator(){
        if(isActive) {
            okPressedNTimes=0;
            myself.findViewById(R.id.black_cloth).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.cost_tv).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.costValue_tv).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.change_tv).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.changeValue_tv).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.vline).setVisibility(View.VISIBLE);
            myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.GONE);
            myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.GONE);
            isActive = !isActive;
            ((PaymentActivity)getContext()).setIsActivePayementsOptions(isActive);
            if (amount.length() != 0) amount.delete(0, amount.length());
            ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
            switch(mode){
                case NUMBER_MODE:
                    setPayementShortcut();
                    break;
                case PERCENTAGE_MODE:
                    setMode(0);
                    break;
                case PAY_PARTIAL_MODE:
                case PAY_TOTAL_MODE:
                case DISCOUNT_MODE:
                    communicator.activateFunction(PaymentActivity.DISCOUNT_MODE_OFF, null, 0.0f);
                    break;
                default:
                    communicator.activateFunction(PaymentActivity.CALCULATOR_OFF, null, 0.0f);
                    break;
            }
        }
    }


    private void buildString(char c)
    {
        if( c != 'C' && !shortcutPressed){
            if((mode == NUMBER_MODE || mode == PERCENTAGE_MODE || mode==ELEMENT_ITEM_SPLIT) && c==','){
                if(!percentageAmount) {
                    Toast.makeText(getContext(), R.string.only_integer_numbers_allowed, Toast.LENGTH_SHORT).show();
                }else  {

                    if(c==','){
                        if(amount.toString().indexOf(',')==-1)
                            amount.append(c);
                    }else
                        amount.append(c);
                }
            }
            else {

                if(amount.length()>0) {
                    String firstChar = ""+amount.charAt(0);
                    if (firstChar.equals("€") || firstChar.equals("%")) {
                        amount = new StringBuilder();
                    }
                }
                if(c==','){
                    if(amount.toString().indexOf(',')==-1)
                        amount.append(c);
                }else
                    amount.append(c);
            }
        }
        else if(c == 'C' && amount.length()!=0 && !shortcutPressed) amount.deleteCharAt(amount.length()-1);
        else if(shortcutPressed && c == 'C'){
            amount.delete(0,amount.length());
            shortcutPressed = false;
        }else if(shortcutPressed){
            amount.delete(0,amount.length());
            shortcutPressed = false;
            if((mode == NUMBER_MODE || mode == PERCENTAGE_MODE || mode==ELEMENT_ITEM_SPLIT) && c==','){
                if(!percentageAmount) {
                    Toast.makeText(getContext(), R.string.only_integer_numbers_allowed, Toast.LENGTH_SHORT).show();
                }else  {
                    if(c==','){
                        if(amount.toString().indexOf(',')==-1)
                            amount.append(c);
                    }else
                        amount.append(c);
                }
            }
            else {
                if(amount.length()>0) {
                    String firstChar = ""+amount.charAt(0);
                    if (firstChar.equals("€") || firstChar.equals("%")) {
                        amount = new StringBuilder();
                    }
                }
                if(c==','){
                    if(amount.toString().indexOf(',')==-1)
                        amount.append(c);
                }else
                    amount.append(c);
            }
        }
        switch(mode){
            case PAY_PARTIAL_MODE:
                if(okPressedNTimes==1){
                    ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
                }else
                    ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                break;
            case PAY_TICKET_MODE:

                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                break;
            case INSERT_CREDIT_MODE:
                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
            case NUMBER_MODE:
                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                break;
            case PERCENTAGE_MODE:
                if(percentageAmount) ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                else ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"%");
                break;
            case DISCOUNT_MODE:
                if(okPressedNTimes==1) {
                    okPressedNTimes = 0;
                    amount = new StringBuilder();
                    if(c!='C') amount.append(c);
                }
                if(!discountMode) ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                else ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"%");
                break;
            case MODIFY_DISCOUNT_MODE:
                if(okPressedNTimes==1) {
                    okPressedNTimes = 0;
                    amount = new StringBuilder();
                    if(c!='C') amount.append(c);
                }
                if(!discountMode) ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                else ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"%");
                break;
            case TIPS_MODE:
                ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString()+"€");
                break;
            case ELEMENT_ITEM_SPLIT:

                ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).setText(amount.toString());
                break;

            default:
                ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText(amount.toString());
        }

    }






    // ------- CALC OPTIONS -------- //

    public void setLastLeftPayment(){
        float totalCost = leftPayments.get(0).getTotalCost();

        setCost(String.valueOf(totalCost));
        resetChange();
        setLeftPayment(new ArrayList<LeftPayment>());
    }


    public void setDiscountValue(float value, CashButtonLayout product) {
        if(product.getHomage()==0)
            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText( String.format("%.2f", roundDecimal(value, 2)).replace(".", ","));//.replace(",", "."));
        else
            ((CustomTextView)myself.findViewById(R.id.numberInsertion_tv)).setText(R.string.gratis_all_caps);
    }


    public void setModifyDiscountShorcut() {
        digitPressed = false;
        CustomTextView d10= (CustomTextView) myself.findViewById(R.id.tv_10);
        //d10.setText("€");
        d10.setVisibility(View.GONE);
        CustomTextView d20= (CustomTextView) myself.findViewById(R.id.tv_20);
        //d20.setText("%");
        d20.setVisibility(View.GONE);
        CustomTextView d50= (CustomTextView) myself.findViewById(R.id.tv_50);
        d50.setVisibility(View.GONE);
        d50.setText("");
        CustomTextView d100= (CustomTextView) myself.findViewById(R.id.tv_100);
        d100.setText("");


        CustomTextView d10Label = (CustomTextView) myself.findViewById(R.id.d_10_label);
        //d10Label.setText("");
        d10Label.setVisibility(View.GONE);
        CustomTextView d20Label = (CustomTextView) myself.findViewById(R.id.d_20_label);
        d20Label.setText("");
        d20Label.setVisibility(View.GONE);
        CustomTextView d50Label = (CustomTextView) myself.findViewById(R.id.d_50_label);
        d50Label.setVisibility(View.GONE);
        d50Label.setText("");
        CustomTextView d100Label = (CustomTextView) myself.findViewById(R.id.d_100_label);
        d100Label.setText("");

        ImageView deuro= (ImageView) myself.findViewById(R.id.tv_EURO);
        ImageView dperc = (ImageView) myself.findViewById(R.id.tv_PERC);
        ImageView dcancel= (ImageView) myself.findViewById(R.id.tv_CANCEL);
        ImageView dhomage = (ImageView) myself.findViewById(R.id.tv_HOMAGE);
        dhomage.setVisibility(View.VISIBLE);
        deuro.setVisibility(View.VISIBLE);
        dperc.setVisibility(View.VISIBLE);
        dcancel.setVisibility(View.VISIBLE);
    }


    public void setNOShortCut() {
        ImageView deuro= (ImageView) myself.findViewById(R.id.tv_EURO);
        ImageView dperc = (ImageView) myself.findViewById(R.id.tv_PERC);
        deuro.setVisibility(View.GONE);
        dperc.setVisibility(View.GONE);
        CustomTextView d10= (CustomTextView) myself.findViewById(R.id.tv_10);
        d10.setText("");
        d10.setVisibility(View.VISIBLE);
        CustomTextView d20= (CustomTextView) myself.findViewById(R.id.tv_20);
        d20.setText("");
        d20.setVisibility(View.VISIBLE);
        CustomTextView d50= (CustomTextView) myself.findViewById(R.id.tv_50);
        d50.setText("");
        CustomTextView d100= (CustomTextView) myself.findViewById(R.id.tv_100);
        d100.setText("");


        CustomTextView d10Label = (CustomTextView) myself.findViewById(R.id.d_10_label);
        d10Label.setVisibility(View.VISIBLE);
        d10Label.setText("");
        CustomTextView d20Label = (CustomTextView) myself.findViewById(R.id.d_20_label);
        d20Label.setText("");
        d20Label.setVisibility(View.VISIBLE);
        CustomTextView d50Label = (CustomTextView) myself.findViewById(R.id.d_50_label);
        d50Label.setText("");
        d50Label.setVisibility(View.VISIBLE);
        CustomTextView d100Label = (CustomTextView) myself.findViewById(R.id.d_100_label);
        d100Label.setText("");

        ImageView dcancel= (ImageView) myself.findViewById(R.id.tv_CANCEL);
        dcancel.setVisibility(View.GONE);
        ImageView dhomage = (ImageView) myself.findViewById(R.id.tv_HOMAGE);
        dhomage.setVisibility(View.GONE);
    }


    public void setTicketShortCut() {
        ImageView deuro= (ImageView) myself.findViewById(R.id.tv_EURO);
        ImageView dperc = (ImageView) myself.findViewById(R.id.tv_PERC);
        deuro.setVisibility(View.GONE);
        dperc.setVisibility(View.GONE);
        CustomTextView d10= (CustomTextView) myself.findViewById(R.id.tv_10);
        d10.setText("4.00");
        d10.setVisibility(View.VISIBLE);
        CustomTextView d20= (CustomTextView) myself.findViewById(R.id.tv_20);
        d20.setText("5.00");
        d20.setVisibility(View.VISIBLE);
        CustomTextView d50= (CustomTextView) myself.findViewById(R.id.tv_50);
        d50.setText("5.20");
        CustomTextView d100= (CustomTextView) myself.findViewById(R.id.tv_100);
        d100.setText("5.29");


        CustomTextView d10Label = (CustomTextView) myself.findViewById(R.id.d_10_label);
        d10Label.setVisibility(View.VISIBLE);
        d10Label.setText(R.string.euro_all_caps);
        CustomTextView d20Label = (CustomTextView) myself.findViewById(R.id.d_20_label);
        d20Label.setVisibility(View.VISIBLE);
        d20Label.setText(R.string.euro_all_caps);
        CustomTextView d50Label = (CustomTextView) myself.findViewById(R.id.d_50_label);
        d50Label.setVisibility(View.VISIBLE);
        d50Label.setText(R.string.euro_all_caps);
        CustomTextView d100Label = (CustomTextView) myself.findViewById(R.id.d_100_label);
        d100Label.setText(R.string.euro_all_caps);

        ImageView dcancel= (ImageView) myself.findViewById(R.id.tv_CANCEL);
        dcancel.setVisibility(View.GONE);
        ImageView dhomage = (ImageView) myself.findViewById(R.id.tv_HOMAGE);
        dhomage.setVisibility(View.GONE);
    }


    public void setPercentageShortCut() {
        percentageAmount = true;
        CustomTextView d10= (CustomTextView) myself.findViewById(R.id.tv_10);
        //d10.setText("€");
        d10.setVisibility(View.GONE);
        CustomTextView d20= (CustomTextView) myself.findViewById(R.id.tv_20);
        //d20.setText("%");
        d20.setVisibility(View.GONE);
        CustomTextView d50= (CustomTextView) myself.findViewById(R.id.tv_50);
        d50.setText("");
        CustomTextView d100= (CustomTextView) myself.findViewById(R.id.tv_100);
        d100.setText("");


        CustomTextView d10Label = (CustomTextView) myself.findViewById(R.id.d_10_label);
        //d10Label.setText("");
        d10Label.setVisibility(View.GONE);
        CustomTextView d20Label = (CustomTextView) myself.findViewById(R.id.d_20_label);
        d20Label.setText("");
        d20Label.setVisibility(View.GONE);
        CustomTextView d50Label = (CustomTextView) myself.findViewById(R.id.d_50_label);
        d50Label.setText("");
        CustomTextView d100Label = (CustomTextView) myself.findViewById(R.id.d_100_label);
        d100Label.setText("");

        ImageView deuro= (ImageView) myself.findViewById(R.id.tv_EURO);
        ImageView dperc = (ImageView) myself.findViewById(R.id.tv_PERC);
        deuro.setVisibility(View.VISIBLE);
        dperc.setVisibility(View.VISIBLE);

        ImageView dcancel= (ImageView) myself.findViewById(R.id.tv_CANCEL);
        dcancel.setVisibility(View.GONE);
        ImageView dhomage = (ImageView) myself.findViewById(R.id.tv_HOMAGE);
        dhomage.setVisibility(View.GONE);
    }


    public void setPayementShortcut() {
        ImageView deuro= (ImageView) myself.findViewById(R.id.tv_EURO);
        ImageView dperc = (ImageView) myself.findViewById(R.id.tv_PERC);
        ImageView dhomage = (ImageView) myself.findViewById(R.id.tv_HOMAGE);
        dhomage.setVisibility(View.GONE);
        deuro.setVisibility(View.GONE);
        dperc.setVisibility(View.GONE);

        ImageView dcancel= (ImageView) myself.findViewById(R.id.tv_CANCEL);
        dcancel.setVisibility(View.GONE);

        CustomTextView d10= (CustomTextView) myself.findViewById(R.id.tv_10);
        d10.setVisibility(View.VISIBLE);
        d10.setText("10");
        CustomTextView d20= (CustomTextView) myself.findViewById(R.id.tv_20);
        d20.setVisibility(View.VISIBLE);
        d20.setText("20");
        CustomTextView d50= (CustomTextView) myself.findViewById(R.id.tv_50);
        d50.setVisibility(View.VISIBLE);
        d50.setText("50");
        CustomTextView d100= (CustomTextView) myself.findViewById(R.id.tv_100);
        d100.setText("100");


        CustomTextView d10Label = (CustomTextView) myself.findViewById(R.id.d_10_label);
        d10Label.setVisibility(View.VISIBLE);
        d10Label.setText(R.string.euro);
        CustomTextView d20Label = (CustomTextView) myself.findViewById(R.id.d_20_label);
        d20Label.setText(R.string.euro);
        d20Label.setVisibility(View.VISIBLE);
        CustomTextView d50Label = (CustomTextView) myself.findViewById(R.id.d_50_label);
        d50Label.setVisibility(View.VISIBLE);
        d50Label.setText(R.string.euro);
        CustomTextView d100Label = (CustomTextView) myself.findViewById(R.id.d_100_label);
        d100Label.setText(R.string.euro);
    }


    public void setElementItemSplit() {
        CustomTextView d10= (CustomTextView) myself.findViewById(R.id.tv_10);
        d10.setVisibility(View.GONE);
        CustomTextView d20= (CustomTextView) myself.findViewById(R.id.tv_20);
        d20.setVisibility(View.GONE);
        CustomTextView d50= (CustomTextView) myself.findViewById(R.id.tv_50);
        d50.setText("");
        CustomTextView d100= (CustomTextView) myself.findViewById(R.id.tv_100);
        d100.setText("");

        CustomTextView d10Label = (CustomTextView) myself.findViewById(R.id.d_10_label);
        d10Label.setVisibility(View.GONE);
        CustomTextView d20Label = (CustomTextView) myself.findViewById(R.id.d_20_label);
        d20Label.setText("");
        d20Label.setVisibility(View.GONE);
        CustomTextView d50Label = (CustomTextView) myself.findViewById(R.id.d_50_label);
        d50Label.setText("");
        CustomTextView d100Label = (CustomTextView) myself.findViewById(R.id.d_100_label);
        d100Label.setText("");

    }


    public void endLeftPayment() {
        ((PaymentActivity) getContext()).setNormalKillOkButton();
        ((CalculatorFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("calc"))
                .setMode(4);
        turnOnOffCalculator();
        // reset payment options buttons to original state.
        ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                .getAdapter().loadButtons(0);
        okPressedNTimes = 0;
    }


    public void payFromPartialButton(){
        String paidString = ((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString();
        if (paidString.length() > 0) {
            if(((PaymentActivity) getContext()).getIsCardFromActivity()){
                ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
                ((PaymentActivity) getContext()).openProcessCardPopup();
                okPressedNTimes = 0;
            }else {
                if(!paidString.equals("SKIP PARTIAL PAYMENT")) {
                    float cost = orderFragment.returnRemaningTotal();
                    float paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
                    if (cost > paid) {
                        myself.findViewById(R.id.cost_tv).setVisibility(View.VISIBLE);
                        myself.findViewById(R.id.costValue_tv).setVisibility(View.VISIBLE);
                        myself.findViewById(R.id.change_tv).setVisibility(View.VISIBLE);
                        myself.findViewById(R.id.changeValue_tv).setVisibility(View.VISIBLE);
                        myself.findViewById(R.id.vline).setVisibility(View.VISIBLE);
                        myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.INVISIBLE);
                        myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.INVISIBLE);


                        ((CustomTextView) myself.findViewById(R.id.costValue_tv)).setText(String.valueOf(paid));
                        ((CustomTextView) myself.findViewById(R.id.changeValue_tv)).setText("");
                        amount = new StringBuilder();
                        okPressedNTimes = 1;
                    } else {
                        CustomTextView myPaid = (CustomTextView) myself.findViewById(R.id.numberInsertion_tv);
                        myPaid.setText(R.string.skip_partial_payment);
                        amount = new StringBuilder();
                        Toast.makeText(getContext(), R.string.partial_cant_be_greater_than_cost, Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }


    public void payPartial(int paymentType){
        okPressedNTimes = 0;
        float totalCost= orderFragment.returnRemaningTotal();

        float credit = ((PaymentActivity) getContext()).getCredit();
        totalCost = totalCost -credit;
        float cost = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.costValue_tv)).getText().toString().replace(",", "."));
        float change = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.changeValue_tv)).getText().toString().replace(",", "."));

        paidVar = cost;
        if (amount.length() != 0)
            amount.delete(0, amount.length());
        shortcutPressed = false;
        //Init Fiscal Printer
        ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
        // check needed to know if there are any more bill splits to pay
        Float p = cost / (totalCost-actualCredit)* 100;
        turnOnOffCalculator();
        orderFragment.percentageSplit(roundDecimal((p > 100 ? 100 : p), 3));
        orderFragment.getOrderListAdapter().setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter().addElement(PARTIAL_MODE, paidVar, p, true, null));
        ((PaymentActivity) getContext()).savePaidBill(paymentType);
        ((PaymentActivity) getContext()).setNormalKillOkButton();
        // reset payment options buttons to original state.
        ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                .getAdapter().loadButtons(0);
        int itemcount = orderSubdivisionFragment.getSubdivisionAdapter().getItemCount();
        orderFragment.getOrderListAdapter().setLeftCost(totalCost-cost);
        ((PaymentActivity)getContext()).printFiscalPartial((cost+change), cost, "Partial Division", itemcount, paymentType);
    }


    // po un partial stampa il resto dello scontrino con carta
    public void acceptRemainingPartialCard() {
        orderFragment = ((OrderFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("order"));
        orderSubdivisionFragment = (OrderSubdivisionFragment)((FragmentActivity)getContext()).getSupportFragmentManager().findFragmentByTag("orderSubdivision");
        float cost = orderFragment.returnRemaningTotal();
        float paid = orderFragment.returnRemaningTotal();
        if (amount.length() != 0)
            amount.delete(0, amount.length());
        shortcutPressed = false;

        //Init Fiscal Printer
        ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
        // check needed to know if there are any more bill splits to pay
        Float p = 0.0f;
        p = (paid / (cost-actualCredit) )* 100;
        orderFragment.setTotalCost(cost-((PaymentActivity)getContext()).getCreditValueAgain() - paid);

        orderFragment.percentageSplit(roundDecimal((p > 100 ? 100 : p), 3));
        orderFragment.getOrderListAdapter().setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter().addElement(PARTIAL_MODE, paidVar, p, true, null));
        // reset payment options buttons to original state.
        int itemcount = orderSubdivisionFragment.getSubdivisionAdapter().getItemCount();
        float a  = getActualCredit();
        ((PaymentActivity)getContext()).printFiscalBillWithNonFiscal(0.0f,(paid-((PaymentActivity)getContext()).getActualCreditValue()), paid-((PaymentActivity)getContext()).getActualCreditValue(), "Partial Division", itemcount, 4);
        ((PaymentActivity) getContext()).savePaidBill(1);
        ((PaymentActivity) getContext()).setNormalKillOkButton();

        turnOnOffCalculator();

        ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                .getAdapter().loadButtons(0);
        orderFragment.showRemaingTotal();
    }


    public void acceptPartialCard(){
        float cost = orderFragment.returnRemaningTotal();
        StringBuffer s = new StringBuffer();
        float paid = Float.parseFloat(((CustomTextView) myself.findViewById(R.id.numberInsertion_tv)).getText().toString().replace(",", "."));
        if (cost > paid) {
            if (cost == paid) {
                paidVar = paid;
                if (amount.length() != 0)
                    amount.delete(0, amount.length());
                shortcutPressed = false;
                //Init Fiscal Printer
                ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
                // check needed to know if there are any more bill splits to pay
                Float p = paid / (cost-actualCredit) * 100;
                orderFragment.setTotalCost(cost-actualCredit - paid);

                orderFragment.percentageSplit(roundDecimal((p > 100 ? 100 : p), 3));
                orderFragment.getOrderListAdapter().setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter().addElement(PARTIAL_MODE, paidVar, p, true, null));
                communicator.printBill((paidVar > cost ? cost : paidVar), paidVar, 0, 4);
                ((PaymentActivity) getContext()).setNormalKillOkButton();
                turnOnOffCalculator();
                // reset payment options buttons to original state.
                ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                        .getAdapter().loadButtons(0);
                orderFragment.getOrderListAdapter().setLeftCost(cost-paid);
                orderFragment.showRemaingTotal();

            } else {
                paidVar = paid;
                if (amount.length() != 0)
                    amount.delete(0, amount.length());
                shortcutPressed = false;
                //Init Fiscal Printer
                ((PaymentActivity) getContext()).setPay_mode(PaymentActivity.PAY_PARTIAL_BILL);
                // check needed to know if there are any more bill splits to pay
                Float p = paid / (cost-actualCredit )* 100;
                orderFragment.setTotalCost(cost-actualCredit - paid);
                orderFragment.percentageSplit(roundDecimal((p > 100 ? 100 : p), 3));
                orderFragment.getOrderListAdapter().setSubdivisionItem(orderSubdivisionFragment.getSubdivisionAdapter().addElement(PARTIAL_MODE, paidVar, p, true, null));
                ((PaymentActivity) getContext()).savePaidBill(1);
                ((PaymentActivity) getContext()).setNormalKillOkButton();
                turnOnOffCalculator();
                // reset payment options buttons to original state.
                int itemcount = orderSubdivisionFragment.getSubdivisionAdapter().getItemCount();
                ((PaymentActivity)getContext()).printFiscalPartial((paid), paid, "Partial Division", itemcount, 4);
                ((OptionsFragment) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag("options"))
                        .getAdapter().loadButtons(0);
                orderFragment.getOrderListAdapter().setLeftCost(cost-paid);
                orderFragment.showRemaingTotal();
            }
        } else {
            Toast.makeText(getContext(), R.string.partial_cant_be_greater_than_cost, Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Handle the pay with fidelity mode
     * in brief, add the possibility to select a client by opening a popup,
     */
    public void setupPayWithFidelity()
    {
        myself.findViewById(R.id.cost_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.costValue_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.change_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.changeValue_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.vline).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.INVISIBLE);
        myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.INVISIBLE);

        CustomTextView cost_tv = myself.findViewById(R.id.cost_tv);
        CustomTextView cost_value = myself.findViewById(R.id.costValue_tv);
        CustomTextView change_tv = myself.findViewById(R.id.change_tv);

        // set the default appearence of the calculator section
        cost_tv.setText("FIDELITY");
        cost_value.setText(" - ");

        cost_value.setOnClickListener(new View.OnClickListener()
        {
            // on click, create a popup to display the list of current clients that are saved in the app
            @Override
            public void onClick(View v)
            {
                View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_client_pay_fidelity, null);

                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);

                // setup the fidelity package adapter
                RecyclerView clientPayFidelity_rv = popupView.findViewById(R.id.client_pay_fidelity_rv);
                clientPayFidelity_rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                clientPayFidelity_rv.setHasFixedSize(true);

                ClientPayFidelityAdapter clientPayFidelityAdapter = new ClientPayFidelityAdapter(getActivity());
                clientPayFidelity_rv.setAdapter(clientPayFidelityAdapter);

                // when OK is pressed, if a client was selected in the popup,
                // set that client for paying with fidelity
                popupView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (clientPayFidelityAdapter.selectedClient != null)
                        {
                            cost_value.setTextSize(17);

                            cost_value.setText(String.format("ID: %s, Credits: %s",
                                    clientPayFidelityAdapter.selectedClient.getFidelity_id(),
                                    clientPayFidelityAdapter.fidelity.getValue()));

                            selectedClientPayFidelity = clientPayFidelityAdapter.selectedClient;

                            popupWindow.dismiss();
                        }
                    }
                });

                popupView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                        { popupWindow.dismiss(); }
                });



                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(( getActivity() ).findViewById(R.id.main), 0, 0, 0);
            }
        });

    }


    public void setCost(String cost){
        float mycost= roundDecimal( Float.parseFloat(cost.replace(",", ".")), 2);
        this.cost = mycost;
        String ty = String.format("%.2f", mycost).replace(".", ",");
        ((CustomTextView)myself.findViewById(R.id.costValue_tv)).setText(String.format("%.2f", mycost).replace(".", ","));//.replace(",", "."));

    }


    public void resetChange(){
        ((CustomTextView)myself.findViewById(R.id.changeValue_tv)).setText("");
        amount = new StringBuilder();
        okPressedNTimes = 0;
    }



    public static Float roundDecimal(float floatNum, int numberOfDecimals) {
        BigDecimal value = new BigDecimal(floatNum);
        value = value.setScale(numberOfDecimals, RoundingMode.HALF_EVEN);
        return value.floatValue();
    }


    public void activatePaymentCalculator()
    {
        setPayementShortcut();
        okPressedNTimes=0;
        myself.findViewById(R.id.cost_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.costValue_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.change_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.changeValue_tv).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.vline).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.numberInsertionInfo_tv).setVisibility(View.GONE);
        myself.findViewById(R.id.numberInsertion_tv).setVisibility(View.GONE);
    }


}
