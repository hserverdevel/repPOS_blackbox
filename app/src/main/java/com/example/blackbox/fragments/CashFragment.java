package com.example.blackbox.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.Operative;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.activities.TableActivity;
import com.example.blackbox.adapter.CashAdapter;
import com.example.blackbox.adapter.OModifierAdapter;
import com.example.blackbox.adapter.OModifierGroupAdapter;
import com.example.blackbox.client.ClientThread;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TemporaryOrder;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.model.TotalBill;
import com.example.blackbox.printer.PrinterDitronThread;
import com.google.gson.Gson;
import com.utils.db.DatabaseAdapter;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;


/**
 * Created by tiziano on 13/06/17.
 */

public class CashFragment extends Fragment implements ClientThread.TaskDelegate
{

    private static final String               STRING_VALUE          = "stringValue";
    private static String                                                 PREF_NAME = "prefs";
    public ArrayList<CashButtonLayout> cashList;
    public boolean paid = false;
    public boolean discountMode = false;
    public  float   density;
    public ArrayList<Customer> listDataCustomer;
    public ArrayList<Integer> currentCustomerArray = new ArrayList<Integer>();
    public CustomTextView viewTotal;
    public CustomTextView tNumber;
    public Boolean        modifyProduct = false;
    public Boolean longClickOnChild = false;
    public  int                                                    modifyChangePosition = -1;
    public  CashButtonLayout                                       newModifiedProduct   = new CashButtonLayout();
    public  Context                                                context;
    public Boolean deleteProduct = false;
    //boolean var used to set keypad and calculate
    public  Boolean       keypad          = false;
    public  Boolean       calculate       = false;
    public  String        unspecItemName  = "";
    public  int           tableNumber     = -1;
    public  int           roomId          = -1;
    public  String        roomName        = "";
    public  boolean       firstOrderClick = false;
    public  boolean errorPopupOpen = false;
    public PopupWindow myPopupWindow;
    View             view;
    CashButtonLayout button;
    CashAdapter listAdapter;
    ExpandableListView expListView;
    int currentCustomer = 0;
    ArrayList<CashButtonLayout> listDataHeader;
    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> listDataChild;
    String codiceFiscale = " ", IP;
    CashFragment forClient = null;
    private String deviceName = "A";
    private int    bill_id    = -1;
    private View   myself;
    private boolean discountEuro        = true;
    private boolean discountToBeDeleted = false;
    private float   totalDiscount       = 0;
    private String email = "";
    private boolean mailSelected = false;
    private boolean keyboard_next_flag = false;
    private              ActivityCommunicator activityCommunicator;
    private              String               activityAssignedValue = "";
    private int cashListIndex = -1;
    private Double  total = 0.00;
    private Integer billId;
    private DatabaseAdapter                                        dbA;
    private CashButtonLayout                                       oldModifiedProduct   = new CashButtonLayout();
    private Map<CashButtonLayout, ArrayList<CashButtonListLayout>> oldListDataChild     = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
    private        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map;
    private StringBuilder amount          = new StringBuilder();
    private boolean       operationDone   = false;
    private boolean dotAdded       = false;
    private int modifyPosition = -1;
    public CashFragment()
    {
    }


    public static Float roundDecimal(float floatNum, int numberOfDecimals)
    {
        BigDecimal value = new BigDecimal(floatNum);
        value = value.setScale(numberOfDecimals, RoundingMode.HALF_EVEN);
        return value.floatValue();
    }


    public boolean getPaid()
    {
        return paid;
    }


    public void setPaid(boolean value)
    {
        paid = value;
    }


    public boolean getDiscountMode()
    {
        return discountMode;
    }


    public void setDiscountMode(boolean value)
    {
        discountMode = value;
    }


    public void setTotalDiscount(float value)
    {
        totalDiscount = value;
    }


    public void setEmail(String mail)
    {
        email = mail;
    }


    public void setMailSelected(boolean value)
    {
        mailSelected = value;
    }


    public CashAdapter getListAdapter()
    {
        return listAdapter;
    }


    public void addToArrayCustomer(int position)
    {
        boolean add = true;
        for (int i = 0; i < currentCustomerArray.size(); i++)
        {
            if (currentCustomerArray.get(i) == position)
            {
                currentCustomerArray.remove(i);
                add = false;
            }
        }
        if (add)
        {
            currentCustomerArray.add(position);
            for (int i = 0; i < listDataCustomer.size(); i++)
            {
                if (listDataCustomer.get(i).getPosition() == position)
                {
                    listDataCustomer.get(i).setActive(true);
                }
            }
        }
        else
        {
            for (int i = 0; i < listDataCustomer.size(); i++)
            {
                if (listDataCustomer.get(i).getPosition() == position)
                {
                    listDataCustomer.get(i).setActive(false);
                }
            }
        }
        listAdapter.setCustomerList(listDataCustomer);
        listAdapter.notifyDataSetChanged();
    }


    public void activateAllCustomer()
    {
        resetArrayCustomer();

        // first check that all are active.
        // If so deactivate them
        boolean allActive = true;
        for (int i = 0; i < listDataCustomer.size(); i++)
        {
            if (!listDataCustomer.get(i).getActive())
            {
                allActive = false;
            }
        }

        for (int i = 0; i < listDataCustomer.size(); i++)
        {
            currentCustomerArray.add(i + 1);
            listDataCustomer.get(i).setActive(!allActive);
        }
    }


    public void resetArrayCustomer()
    {

        currentCustomerArray = new ArrayList<Integer>();

    }


    public int getListDataHeader()
    {
        if (listDataHeader != null)
        {
            return listDataHeader.size();
        }
        else
        {
            return 0;
        }
    }


    public int getCashListIndex()
    {
        return cashListIndex;
    }


    public void setCashListIndex(int i)
    {
        cashListIndex = i;
    }


    public int getBillId()
    {
        return billId;
    }


    public Boolean getModifyProduct()
    {
        return modifyProduct;
    }


    public void setModifyProduct(Boolean b)
    {
        modifyProduct = b;
    }


    public Boolean getLongClickOnChild()
    {
        return longClickOnChild;
    }


    public void setLongClickOnChild(Boolean b)
    {
        longClickOnChild = b;
    }


    public boolean getDeleteProduct()
    {
        return deleteProduct;
    }


    public int getModifyPosition()
    {
        return modifyPosition;
    }


    public void setModifyPosition(int i)
    {
        modifyPosition = i;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view   = inflater.inflate(R.layout.fragment_cash, container, false);
        myself = view;

        viewTotal     = (CustomTextView) view.findViewById(R.id.cash_euro_total);
        button        = new CashButtonLayout();
        this.cashList = new ArrayList<CashButtonLayout>();
        dbA           = new DatabaseAdapter(this.getContext());

        forClient = this;
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, getContext().MODE_PRIVATE);
        IP          = StaticValue.IP;
        expListView = (ExpandableListView) view.findViewById(R.id.cash_recyclerView);

        Intent intent = getActivity().getIntent();
        billId = intent.getIntExtra("billId", -1);

        //set table number get from intent
        tableNumber = intent.getIntExtra("tableNumber", -1);
        CustomTextView tNumberLayout = (CustomTextView) view.findViewById(R.id.cash_table_not_set);

        if (tableNumber != -1 && tableNumber != -11)
        {
            //table set
            tNumberLayout.setVisibility(View.GONE);
            ((CustomTextView) view.findViewById(R.id.cash_table)).setVisibility(View.VISIBLE);
            tNumber = (CustomTextView) view.findViewById(R.id.cash_table_number);

            if (tNumber != null)
            {
                tNumber.setVisibility(View.VISIBLE);
                tNumber.setText(String.valueOf((tableNumber)));
                //int roomId = dbA.getRoomIdAgain(billId);
                Room room = dbA.fetchRoomById(roomId);
                roomName = room.getName();
            }

            else
            {
                roomName = "";
            }
        }

        else
        {
            //no table
            tNumberLayout.setVisibility(View.VISIBLE);
            ((CustomTextView) view.findViewById(R.id.cash_table)).setVisibility(View.GONE);
            tNumber = (CustomTextView) view.findViewById(R.id.cash_table_number);
            if (tNumber != null)
            {
                tNumber.setVisibility(View.GONE);
            }
        }


        if (!StaticValue.blackbox)
        {
            if (dbA.getPaidBill(billId) != 0 && billId != -1 && billId != -11)
            {
                view.findViewById(R.id.cash_order)
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
                view.findViewById(R.id.cash_order_number)
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
                view.findViewById(R.id.cash_table)
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
                view.findViewById(R.id.cash_table_number)
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
                view.findViewById(R.id.layout_1)
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
                paid = true;
            }
            else
            {
                paid = false;
            }
        }

        roomId = intent.getIntExtra("roomId", -1);

        //set order number
        int orderNumber = intent.getIntExtra("orderNumber", -1);
        if (orderNumber == -1)
        {
            //check database, se nitne
            if (StaticValue.blackbox)
            {
                RequestParam params = new RequestParam();
                params.add("androidId", StaticValue.androidId);
                ((Operative) context).callHttpHandler("/getLastBillNumber", params);
            }

            else
            {
                long lastClose = dbA.getLastClosing();
                int  maxNumber = dbA.getMaxOrderId(lastClose);
                if (maxNumber == -1)
                {
                    //first time you open cashFragment in this session
                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                    orderNumber = 0;
                    numberBillView.setText(String.format("#%d", orderNumber + 1));
                    intent.putExtra("orderNumber", orderNumber);
                }
                else
                {
                    //set lates ordernumber
                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                    numberBillView.setText(String.format("#%d", maxNumber + 2));
                    intent.putExtra("orderNumber", maxNumber + 1);

                }
            }
        }
        else
        {
            //here you came from anoter activity, e.g. clientsActivity
            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
            numberBillView.setText(String.format("#%d", orderNumber + 1));
            intent.putExtra("orderNumber", orderNumber);

        }

        if (StaticValue.blackbox && billId > 0)
        {
            RequestParam params = new RequestParam();
            params.add("billId", String.valueOf(billId));
            ((Operative) context).callHttpHandler("/getBillData", params);
        }
        else
        {
            listDataCustomer = new ArrayList<Customer>();
            listDataHeader   = new ArrayList<CashButtonLayout>();
            listDataChild    = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            map              = dbA.getBillData(billId, context);
            //set total for bill if billId exist, else set 0.0f
            total = dbA.getOnlyBillPrice(billId);
            if (total == 0)
            {
                DecimalFormat twoDForm = new DecimalFormat("#.00");
                viewTotal.setText("0,00");
            }
            else
            {
                DecimalFormat twoDForm = new DecimalFormat("#.00");
                viewTotal.setText(twoDForm.format(total).replace(".", ","));
            }
            listDataChild  = map;
            listDataHeader = new ArrayList<>(map.keySet());
            boolean checkifAllCustomer = true;
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (listDataHeader.get(i).getProductId() != -20)
                {
                    checkifAllCustomer = false;
                }
            }
            if (checkifAllCustomer)
            {
                cashListIndex = 0;
            }
            else
            {
                if (listDataHeader.get(listDataHeader.size() - 1).getProductId() == -20)
                {
                    cashListIndex = listDataHeader.size() - 1;
                }
                else
                {
                    cashListIndex = listDataHeader.size();
                }
            }
            //set cash adapter, is the parte where you see your product and modifier on the right (your bill pratically)
            listAdapter = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, billId, listDataCustomer, paid);
            //set customer if present
            listDataCustomer = dbA.getCustomerData(billId);
            if (listDataCustomer.size() > 0)
            {
                listAdapter.setCustomerList(listDataCustomer);
                listAdapter.setFirstClient();
            }
            // setting list adapter
            expListView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
            //expand allgroups
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                expListView.expandGroup(i);
            }
            listAdapter.notifyDataSetChanged();

            totalDiscount = dbA.getBillDiscountPrice(billId);
            if (!paid)
            {
                if (totalDiscount == 0.0f)
                {
                    setDiscountLayout(false);
                }
                else
                {
                    setDiscountLayout(true);
                }
            }

            //maybe this should work
            if (intent.getBooleanExtra("modifiedCustomer", false))
            {
                //set customer if present
                listDataCustomer = dbA.getCustomerData(billId);
                if (listDataCustomer.size() > 0)
                {
                    listAdapter.setCustomerList(listDataCustomer);
                    listAdapter.setFirstClient();
                }
                // setting list adapter
                expListView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();

                //expand allgroups
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    expListView.expandGroup(i);
                }
                //listDataChild = map;
                listAdapter.notifyDataSetChanged();
            }
        }

        Display        display    = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = getResources().getDisplayMetrics().density;


        // set on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l)
            {
                //close all other children modify open and open selected one

                if (!paid)
                {
                    if (!dbA.checkIfBillSplitPaid(billId))
                    {
                        Boolean a = getModifyProduct();
                        Boolean b = getLongClickOnChild();
                        if (!getModifyProduct() && !getLongClickOnChild() && !deleteProduct)
                        {
                            for (int i = 0; i < listDataHeader.size(); i++)
                            {
                                listDataHeader.get(i).setModifyModifier(i == groupPosition);

                            }
                            listAdapter.notifyDataSetChanged();
                            for (int i = 0; i < listDataHeader.size(); i++)
                            {
                                expListView.expandGroup(i);
                            }

                            activityCommunicator = (ActivityCommunicator) getActivity();
                            activityCommunicator.showModifierPageToModify(groupPosition, listDataChild
                                    .get(listDataHeader.get(groupPosition)), listDataChild.get(listDataHeader
                                    .get(groupPosition))
                                      .get(childPosition)
                                      .getModifierId(), listDataHeader
                                    .get(groupPosition)
                                    .getTitle(), listDataHeader.get(groupPosition).getProductId());
                            expListView.setSelectedGroup(groupPosition);
                        }
                    }
                }

                return true;
            }
        });

        /*
         * set click on cancel button to delete cash list
         * todo OPEN POPUP TO ASK TO DELETE. do I delete completly the bill
         */
        RelativeLayout cancelButton = (RelativeLayout) view.findViewById(R.id.layout_2);
        cancelButton.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {

                dbA.deleteWrongProduct();
                if (!paid)
                {
                    if (billId == -1)
                    {
                        if (!getModifyProduct() && !ModifierFragment.getModify())
                        {
                            Intent intent         = getActivity().getIntent();
                            int    numberBill     = intent.getIntExtra("orderNumber", -1);
                            int    bestNumberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                            if (numberBill < bestNumberBill)
                            {
                                numberBill = bestNumberBill;
                            }

                            listDataCustomer     = new ArrayList<Customer>();
                            currentCustomerArray = new ArrayList<Integer>();
                            listDataHeader       = new ArrayList<CashButtonLayout>();
                            listDataChild        = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                            listAdapter          = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);

                            expListView.setAdapter(listAdapter);
                            listDataCustomer     = new ArrayList<Customer>();
                            currentCustomerArray = new ArrayList<Integer>();

                            listAdapter.notifyDataSetChanged();
                            activityCommunicator.deleteCurrentCash();
                            setModifyBar(false);

                            total = 0.0;
                            viewTotal.setText("0,00");
                            totalDiscount = 0.0f;

                            if (view.findViewById(R.id.euro_icon_discount).getVisibility() == View.VISIBLE)
                            {
                                setDiscountLayout(false);
                            }

                            long sessionTime = dbA.getLastClosing();
                            int  number      = dbA.getMaxNumberOrderId(sessionTime);

                            intent.putExtra("tableNumber", -1);
                            if (number + 1 > numberBill + 1)
                            {
                                intent.putExtra("orderNumber", (number + 1));
                                CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                                numberBillView.setText("#" + (number + 2));
                            }
                            else if (dbA.getPaidBill(billId) != 0 && dbA.getPaidBill(billId) != -1)
                            {
                                intent.putExtra("orderNumber", numberBill + 1);
                                CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                                numberBillView.setText("#" + (numberBill + 2));
                            }

                            CustomTextView numberTableSet = (CustomTextView) myself.findViewById(R.id.cash_table_number);
                            String         setto          = "";
                            numberTableSet.setText(setto);
                            if (dbA.getPaidBill(billId) == 0)
                            {
                                dbA.deleteBillData(numberBill, context);
                                dbA.deleteDiscuntTotal(billId);
                            }
                            dbA.updateDiscountToZero(billId);

                            intent.putExtra("billId", -1);
                            billId = -1;
                        }
                    }
                    else
                    {
                        openBillDecisionPopup(view);
                    }
                }
                else
                {
                    //paid bill
                    paid = false;
                    view.findViewById(R.id.cash_order)
                        .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    view.findViewById(R.id.cash_order_number)
                        .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    view.findViewById(R.id.cash_table)
                        .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    CustomTextView tNumberLayout = (CustomTextView) view.findViewById(R.id.cash_table_not_set);
                    tNumberLayout.setVisibility(View.VISIBLE);
                    ((CustomTextView) view.findViewById(R.id.cash_table)).setVisibility(View.GONE);
                    tNumber = (CustomTextView) view.findViewById(R.id.cash_table_number);
                    if (tNumber != null)
                    {
                        tNumber.setVisibility(View.GONE);
                    }
                    view.findViewById(R.id.layout_1)
                        .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    int numberBill = intent.getIntExtra("orderNumber", -1);
                    if (numberBill == -1)
                    {
                        numberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                    }
                    totalDiscount = 0.0f;
                    billId        = -1;
                    intent.putExtra("billId", billId);
                    activityCommunicator = (ActivityCommunicator) getActivity();
                    listDataCustomer     = new ArrayList<Customer>();
                    currentCustomerArray = new ArrayList<Integer>();
                    currentCustomerArray = new ArrayList<Integer>();
                    listDataHeader       = new ArrayList<CashButtonLayout>();
                    listDataChild        = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                    listAdapter          = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);


                    expListView.setAdapter(listAdapter);
                    listAdapter.notifyDataSetChanged();
                    activityCommunicator.deleteCurrentCash();
                    setModifyBar(false);
                    total = 0.0;
                    viewTotal.setText("0,00");
                    ArrayList<TotalBill> paidBills = dbA.getBillsList("Select * from bill_total where paid=" + 1);
                    long                 lastClose = dbA.getLastClosing();
                    int                  maxNumber = dbA.getMaxOrderId(lastClose);
                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);


                    if (maxNumber == -1)
                    {
                        //first time you open cashFragment in this session
                        numberBillView.setText(String.format("#%d", 1));
                        intent.putExtra("orderNumber", 0);
                    }
                    else
                    {
                        //set lates ordernumber
                        numberBillView.setText(String.format("#%d", maxNumber + 2));
                        intent.putExtra("orderNumber", maxNumber + 1);
                    }
                    long sessionTime = dbA.getLastClosing();
                    int  number      = dbA.getMaxNumberOrderId(sessionTime);
                    intent.putExtra("paidBills", paidBills);
                    if (number + 1 > numberBill + 1)
                    {
                        intent.putExtra("orderNumber", (number + 1));
                        ((Operative) context).setCashFragmentValues(billId, number + 1);
                        numberBillView.setText(String.format("#%d", number + 2));
                    }
                    else
                    {
                        intent.putExtra("orderNumber", (numberBill + 1));
                        ((Operative) context).setCashFragmentValues(billId, numberBill + 1);
                        numberBillView.setText("#" + (numberBill + 2));
                    }
                    mailSelected = false;
                    email        = "";
                    intent.putExtra("email", email);
                }

                return true;
            }

        });


        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                int groupPosition = getModifyPosition();

                keypad    = false;
                calculate = false;
                myself.findViewById(R.id.layout_5).setActivated(false);
                myself.findViewById(R.id.layout_6).setActivated(false);
                myself.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                myself.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                myself.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                resetCustomerToDelete();
                if (getModifyProduct() || ModifierFragment.getModify() || deleteProduct)
                {
                    activityCommunicator = (ActivityCommunicator) context;
                    activityCommunicator.endModifyModifier(groupPosition);
                    //activityCommunicator.goToMainPage();

                    //    activityCommunicator = (ActivityCommunicator) getActivity();
                    //    activityCommunicator.endModifyModifier(-1);
                    ModifierFragment.setModify(false);
                    endModifyModifier(-1);
                    setModifyProduct(false);
                    activityCommunicator.endModifyProduct();
                    activityCommunicator.goToMainPage();
                    resetCashNotChanghed();
                    deleteProduct = false;
                }
            }
        });



        // TODO set click to save current cash list
        RelativeLayout saveButton = (RelativeLayout) view.findViewById(R.id.layout_3);

        RelativeLayout paymentButton  = (RelativeLayout) view.findViewById(R.id.layout_4);
        RelativeLayout keypadButton   = (RelativeLayout) view.findViewById(R.id.layout_5);
        RelativeLayout calculator     = (RelativeLayout) view.findViewById(R.id.layout_6);
        RelativeLayout roundButton    = (RelativeLayout) view.findViewById(R.id.layout_7);
        RelativeLayout discountButton = (RelativeLayout) view.findViewById(R.id.layout_8);


        saveButton.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            /**
             * show delete view
             */
            public void onSwipeLeft()
            {
                saveButton.setVisibility(View.GONE);
                paymentButton.setVisibility(View.GONE);
                keypadButton.setVisibility(View.VISIBLE);
                calculator.setVisibility(View.VISIBLE);

            }


            public void onClick()
            {
                if (!paid)
                {
                    if (StaticValue.blackbox)
                    {
                        saveButton.setEnabled(false);
                        saveBillOnServer("saveButton", listDataHeader.size());
                    }

                    else
                    {
                        if (!getModifyProduct() && !ModifierFragment.getModify() && listDataHeader.size() != 0)
                        {
                            saveButton.setEnabled(false);
                            Intent intent = getActivity().getIntent();
                            intent.setAction("normal");
                            int numberBill = intent.getIntExtra("orderNumber", -1);
                            if (numberBill == -1)
                            {
                                numberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                            }
                            long             sessionTime = dbA.getLastClosing();
                            int              number      = dbA.getMaxOrderId(sessionTime);
                            Date             date        = new Date(sessionTime);
                            SimpleDateFormat df2         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String           dateText    = df2.format(date);
                            int              newBillId   = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateText + "';");
                            billId  = newBillId;
                            bill_id = newBillId;
                            if (newBillId == -11)
                            {
                                saveBill(0);

                            }
                            else
                            {
                                if (listDataHeader.size() != 0)
                                {
                                    updateBill(billId);
                                    printOrderBill(billId);
                                }
                                listDataHeader   = new ArrayList<CashButtonLayout>();
                                listDataChild    = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                                listDataCustomer = new ArrayList<>();
                                listAdapter      = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, false);
                                expListView.setAdapter(listAdapter);
                                listAdapter.notifyDataSetChanged();
                                activityCommunicator.deleteCurrentCash();

                                setModifyBar(false);

                                //incremento numero ordine

                            }

                            if (number + 1 > numberBill + 1)
                            {
                                intent.putExtra("orderNumber", (number + 1));
                                CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                                numberBillView.setText("#" + (number + 2));
                            }
                            else
                            {
                                if (StaticValue.blackbox)
                                {
                                    intent.putExtra("orderNumber", (numberBill + 1));
                                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                                    numberBillView.setText("#" + (numberBill + 1));
                                }
                                else
                                {
                                    intent.putExtra("orderNumber", (numberBill + 1));
                                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                                    numberBillView.setText("#" + (numberBill + 2));
                                }
                            }
                            CustomTextView numberTableSet = (CustomTextView) myself.findViewById(R.id.cash_table_number);
                            String         setto          = "";
                            numberTableSet.setText(setto);

                            listDataCustomer     = new ArrayList<Customer>();
                            currentCustomerArray = new ArrayList<Integer>();

                            intent.putExtra("billId", -1);
                            bill_id     = -1;
                            billId      = -1;
                            tableNumber = -11;
                            ((Operative) context).setBillId(-1);
                            saveButton.setEnabled(true);

                        }
                    }
                }

                else
                {
                    reprintOrderBill(billId);
                }

                // refresh the color of the table button
                view.findViewById(R.id.cash_table_not_set).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cash_table).setVisibility(View.GONE);
                view.findViewById(R.id.cash_table_number).setVisibility(View.GONE);
            }

        });


        paymentButton.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            //to be implemented if needed
            public void onSwipeRight()
            {
                /*if (saveButton.getVisibility() == View.VISIBLE) {
                    // Its visible
                    saveButton.setVisibility(View.VISIBLE);
                    paymentButton.setVisibility(View.VISIBLE);
                    keypadButton.setVisibility(View.GONE);
                    calculator.setVisibility(View.GONE);
                } else {
                    // Either gone or invisible
                    saveButton.setVisibility(View.VISIBLE);
                    paymentButton.setVisibility(View.VISIBLE);
                    keypadButton.setVisibility(View.GONE);
                    calculator.setVisibility(View.GONE);
                }*/
            }


            /**
             * show delete view
             */
            public void onSwipeLeft()
            {
                if (saveButton.getVisibility() == View.VISIBLE)
                {
                    // Its visible
                    saveButton.setVisibility(View.GONE);
                    paymentButton.setVisibility(View.GONE);
                    keypadButton.setVisibility(View.VISIBLE);
                    calculator.setVisibility(View.VISIBLE);
                }
                else
                {
                    // Either gone or invisible
                    saveButton.setVisibility(View.GONE);
                    paymentButton.setVisibility(View.GONE);
                    keypadButton.setVisibility(View.VISIBLE);
                    calculator.setVisibility(View.VISIBLE);
                }


            }


            /**
             * Override OnGroupClick for expandable list
             */
            public void onClick()
            {
                Intent  intent1          = getActivity().getIntent();
                boolean isSetTableAction = false;
                int     numberBill       = intent1.getIntExtra("orderNumber", 1);

                if (!isSetTableAction)
                {
                    boolean myCheck = checkIfAllCustomerHaveProducts();
                    if (myCheck)
                    {
                        if (listDataHeader.size() > 0)
                        {
                            if (StaticValue.blackbox)
                            {
                                paymentButton.setEnabled(false);
                                saveBillOnServer("paymentButton", listDataHeader.size());
                            }

                            else
                            {
                                //controllo se lo scontrino esiste già
                                int bid = intent1.getIntExtra("billId", -1);
                                bid = billId;
                                if (bid == -1 || bid == -11)
                                {
                                    TemporaryOrder.setProduct(listDataHeader);
                                    saveBillAndPrint(0);
                                    Operative op     = (Operative) getActivity();
                                    Intent    intent = new Intent(getActivity(), PaymentActivity.class);
                                    intent.putExtra("username", op.getUser());
                                    intent.putExtra("isAdmin", op.isAdmin());
                                    intent.setAction("orderPayment");
                                    intent.putExtra("billId", bill_id);

                                    intent.putExtra("orderNumber", numberBill + 1);
                                    intent.putExtra("tableNumber", tableNumber);
                                    //just one already registered customer
                                    if (email != null && !mailSelected)
                                    {
                                        intent.putExtra("email", email);
                                        mailSelected = true;
                                    }
                                    //more than one registered customers
                                    else if (email != null && mailSelected)
                                    {
                                        intent.putExtra("more", mailSelected);
                                    }
                                    startActivity(intent);

                                }
                                else
                                {
                                    Double t = total;
                                    updateBill(billId);
                                    printOrderBill(billId);
                                    Operative op = (Operative) getActivity();

                                    Intent intent = new Intent(getActivity(), PaymentActivity.class);
                                    intent.putExtra("username", op.getUser());
                                    intent.putExtra("isAdmin", op.isAdmin());
                                    intent.setAction("orderPayment");
                                    intent.putExtra("billId", billId);
                                    intent.putExtra("orderNumber", numberBill + 1);
                                    intent.putExtra("tableNumber", tableNumber);
                                    if (email != null && !mailSelected)
                                    {
                                        intent.putExtra("email", email);
                                        mailSelected = true;
                                    }
                                    else if (email != null && mailSelected)
                                    {
                                        intent.putExtra("more", mailSelected);
                                    }
                                    startActivity(intent);

                                }
                            }
                        }
                        else
                        {
                            //avvio payment solo per tips e scontrini a caso

                        }
                    }
                    else
                    {
                        Toast.makeText(context, R.string.please_insert_products_for_all_customers_before_pay, Toast.LENGTH_SHORT)
                             .show();
                    }
                }


            }


            public void onLongClick()
            {
                if (listDataHeader.size() > 0)
                {
                    showSpinner();
                    Gson       gson     = new Gson();
                    JSONObject combined = new JSONObject();

                    Float  totalDiscount = dbA.getBillDiscountPrice(billId);
                    Double costo         = dbA.getBillPrice(billId);
                    int    orderNumber   = intent.getIntExtra("orderNumber", -1);
                    if (StaticValue.printerName.equals("ditron"))
                    {
                        PrinterDitronThread ditron = PrinterDitronThread.getInstance();
                        ditron.closeAll();
                        ditron.startSocket();
                    }
                    float cost = 0.0f;
                    for (CashButtonLayout cashButton : listDataHeader)
                    {
                        cost += cashButton.getQuantityInt() * cashButton.getPriceFloat();
                        ArrayList<CashButtonListLayout> mList = cashButton.getCashList();
                        if (mList != null)
                        {
                            for (CashButtonListLayout m : mList)
                            {
                                cost += m.getQuantityInt() * m.getPriceFloat();
                            }
                        }


                    }
                    ClientThread myThread = ClientThread.getInstance();
                    myThread.setProducts(listDataHeader);
                    myThread.setModifiers(listDataChild);
                    myThread.setPrintType(4);
                    myThread.setBillId(String.valueOf(billId));
                    myThread.setDeviceName(deviceName);
                    myThread.setOrderNumberBill(String.valueOf(orderNumber));
                    myThread.setCost(cost);
                    myThread.setPaid(cost);
                    myThread.setCredit(0.0f);
                    myThread.setPaymentType(1);
                    myThread.setTotalDiscount(totalDiscount);
                    myThread.setTableNumber(tableNumber);
                    Room room = dbA.fetchRoomById(roomId);
                    if (room.getId() > 0)
                    {
                        myThread.setRoomName(room.getName());
                    }

                    else
                    {
                        myThread.setRoomName("");
                    }

                    myThread.delegate = forClient;
                    myThread.setClientThread();
                    myThread.setRunBaby(true);
                }

            }

        });


        // set click on modify button to finish modify product in cash list part
        CustomButton modify = (CustomButton) view.findViewById(R.id.modify_end_button);
        modify.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                int mcpTemp = modifyChangePosition;
                modifyChangePosition = -1;
                activityCommunicator.goToMainPage();
                //clean cash list (normal view and all child visible)
                reCalculateTotal();
                resetCashNotChanghed();
                expListView.setAdapter(listAdapter);
                //end modify product method
                setModifyProduct(false);
                if (StaticValue.blackbox)
                {
                    if (billId > 0)
                    {
                        saveBillOnServer("modifyProduct", cashListIndex);
                        if (mcpTemp <= cashListIndex && cashListIndex != 0)
                        {
                            //send errata corridge to order
                            Customer customer = new Customer();
                            if (listDataCustomer.size() > 0)
                            {
                                customer = listDataCustomer.get(listDataHeader.get(mcpTemp)
                                                                              .getClientPosition() - 1);
                            }
                            printOrderCorrection(mcpTemp, customer);
                        }

                        activityCommunicator.endModifyProduct();
                    }
                    else
                    {
                        activityCommunicator.endModifyProduct();
                    }
                }
                else
                {
                    if (billId <= 0)
                    {
                        Intent intent     = getActivity().getIntent();
                        int    numberBill = intent.getIntExtra("orderNumber", 1);
                        int    billId     = dbA.saveTotalBillForPayment(total, numberBill, 0);
                        ((Operative) context).setBillId(billId);
                        bill_id = billId;

                        CashFragment.this.billId  = billId;
                        CashFragment.this.bill_id = billId;
                        if (billId != 0)
                        {
                            for (int i = 0; i < listDataHeader.size(); i++)
                            {
                                //salvo prodotto
                                int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader
                                        .get(i));
                                if (listDataChild.get(listDataHeader.get(i)) != null)
                                {
                                    for (int j = 0; j < listDataChild.get(listDataHeader.get(i))
                                                                     .size(); j++)
                                    {
                                        //salvo tutti i figli
                                        dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader
                                                .get(i)).get(j));
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        updateBill(billId);
                        if (mcpTemp <= cashListIndex && cashListIndex != 0)
                        {
                            //send errata corridge to order
                            Customer customer = new Customer();
                            if (listDataCustomer.size() > 0)
                            {
                                customer = listDataCustomer.get(listDataHeader.get(mcpTemp)
                                                                              .getClientPosition() - 1);
                            }
                            printOrderCorrection(mcpTemp, customer);
                        }

                    }
                    activityCommunicator.endModifyProduct();
                }
            }
        });


        view.findViewById(R.id.cash_table_container).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        if (!paid)
                        {
                            Intent intent1 = getActivity().getIntent();

                            Operative op = (Operative) getActivity();


                            int numberBill = intent1.getIntExtra("orderNumber", 1);

                            Intent intent = new Intent(getActivity(), TableActivity.class);
                            intent.putExtra("username", op.getUser());
                            intent.putExtra("isAdmin", op.isAdmin());
                            intent.setAction("setTable");
                            intent.putExtra("orderNumber", numberBill);
                            intent.putExtra("tableNumber", tableNumber);
                            intent.putExtra("roomId", roomId);
                            intent.setAction("operation");

                            long             lastClose = dbA.getLastClosing();
                            Date             date      = new Date(lastClose);
                            SimpleDateFormat df2       = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String           dateText  = df2.format(date);

                            int newBillId = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateText + "';");
                            if (newBillId == -11)
                            {
                                intent.putExtra("billId", bill_id);
                            }

                            else
                            {
                                intent.putExtra("billId", newBillId);
                            }

                            startActivity(intent);
                        }

                    }
                }
        );


        keypadButton.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            //to be implemented if needed
            public void onSwipeRight()
            {

                if (paymentButton.getVisibility() == View.VISIBLE)
                {
                    // Its visible
                    saveButton.setVisibility(View.VISIBLE);
                    paymentButton.setVisibility(View.VISIBLE);
                    keypadButton.setVisibility(View.GONE);
                    calculator.setVisibility(View.GONE);
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    keypad    = false;
                    calculate = false;
                    calculator.setActivated(false);
                    keypadButton.setActivated(false);

                }
                else
                {
                    // Either gone or invisible
                    saveButton.setVisibility(View.VISIBLE);
                    paymentButton.setVisibility(View.VISIBLE);
                    keypadButton.setVisibility(View.GONE);
                    calculator.setVisibility(View.GONE);
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    keypad    = false;
                    calculate = false;
                    keypadButton.setActivated(false);
                    calculator.setActivated(false);
                }
            }


            /**
             * show delete view
             */
            public void onSwipeLeft()
            {
                /*keypadButton.setVisibility(View.GONE);
                calculator.setVisibility(View.GONE);
                roundButton.setVisibility(View.VISIBLE);
                discountButton.setVisibility(View.VISIBLE);
                if(keypadButton.isActivated()){
                    keypadButton.setActivated(false);
                    keypad = false;
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    calculate = false;
                }
                else if(calculator.isActivated()){
                    calculator.setActivated(false);
                    keypad = false;
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    calculate = false;
                }*/

            }


            /**
             * Override OnGrouopClick for expandable list
             */
            public void onClick()
            {


                if (!paid)
                {
                    if (calculate)
                    {
                        calculate = false;
                        keypad    = true;
                        view.findViewById(R.id.layout_6).setActivated(false);
                        view.findViewById(R.id.keypad_add_button_img).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.calculator_add_button_img).setVisibility(View.GONE);
                        keypadButton.setActivated(true);
                        initKeypad();

                    }
                    else if (!discountMode)
                    {
                        if (keypad)
                        {

                            view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                            view.findViewById(R.id.cash_recycler_container)
                                .setVisibility(View.VISIBLE);
                            keypad = false;
                            keypadButton.setActivated(false);

                        }
                        else
                        {
                            view.findViewById(R.id.cash_hline).setVisibility(View.GONE);
                            view.findViewById(R.id.cash_calculator_pad).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.cash_recycler_container)
                                .setVisibility(View.GONE);
                            keypadButton.setActivated(true);
                            keypad = true;
                            view.findViewById(R.id.keypad_add_button_img)
                                .setVisibility(View.VISIBLE);
                            view.findViewById(R.id.calculator_add_button_img)
                                .setVisibility(View.GONE);
                            initKeypad();
                        }
                    }
                    else if (discountMode)
                    {
                        view.findViewById(R.id.cash_hline).setVisibility(View.GONE);
                        view.findViewById(R.id.cash_calculator_pad).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.cash_recycler_container).setVisibility(View.GONE);
                       /* view.findViewById(R.id.tv_eurosign).setVisibility(View.GONE);
                        view.findViewById(R.id.tv_percentage).setVisibility(View.GONE);*/
                        keypadButton.setActivated(true);
                        //discountButton.setActivated(false);
                        keypad       = true;
                        discountMode = false;
                        view.findViewById(R.id.keypad_add_button_img).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.calculator_add_button_img).setVisibility(View.GONE);
                        initKeypad();
                    }
                }

            }

        });


        //cALCULATE CLICK
        calculator.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            //to be implemented if needed
            public void onSwipeRight()
            {

                saveButton.setVisibility(View.VISIBLE);
                paymentButton.setVisibility(View.VISIBLE);
                keypadButton.setVisibility(View.GONE);
                calculator.setVisibility(View.GONE);
                view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                calculate = false;
                keypad    = false;
                calculator.setActivated(false);
            }


            /**
             * show delete view
             */
            public void onSwipeLeft()
            {
                /*keypadButton.setVisibility(View.GONE);
                calculator.setVisibility(View.GONE);
                roundButton.setVisibility(View.VISIBLE);
                discountButton.setVisibility(View.VISIBLE);
                if(keypadButton.isActivated()){
                    keypadButton.setActivated(false);
                    keypad = false;
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    calculate = false;
                }
                else if(calculator.isActivated()){
                    calculator.setActivated(false);
                    keypad = false;
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    calculate = false;
                }
*/
            }


            /**
             * Override OnGrouopClick for expandable list
             */
            public void onClick()
            {


                if (keypad)
                {
                    keypad    = false;
                    calculate = true;
                    view.findViewById(R.id.layout_5).setActivated(false);
                    view.findViewById(R.id.keypad_add_button_img).setVisibility(View.GONE);
                    view.findViewById(R.id.calculator_add_button_img).setVisibility(View.VISIBLE);
                    calculator.setActivated(true);
                    initCalculate();

                }
                else if (!discountMode)
                {
                    if (calculate)
                    {
                        view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                        view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                        calculate = false;
                        calculator.setActivated(false);
                        view.findViewById(R.id.tv_DIV).setVisibility(View.GONE);
                        view.findViewById(R.id.tv_PER).setVisibility(View.GONE);
                        view.findViewById(R.id.tv_MINUS).setVisibility(View.GONE);
                        view.findViewById(R.id.tv_PLUS).setVisibility(View.GONE);
                    }
                    else
                    {
                        view.findViewById(R.id.cash_hline).setVisibility(View.GONE);
                        view.findViewById(R.id.cash_calculator_pad).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.cash_recycler_container).setVisibility(View.GONE);
                        view.findViewById(R.id.keypad_add_button_img).setVisibility(View.GONE);
                        view.findViewById(R.id.calculator_add_button_img)
                            .setVisibility(View.VISIBLE);
                        calculator.setActivated(true);
                        calculate = true;
                        initCalculate();

                    }
                }
                else if (discountMode)
                {
                    discountMode = false;
                    view.findViewById(R.id.cash_hline).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.GONE);
                    view.findViewById(R.id.keypad_add_button_img).setVisibility(View.GONE);
                    view.findViewById(R.id.calculator_add_button_img).setVisibility(View.VISIBLE);
                    calculator.setActivated(true);
                    calculate = true;
                    initCalculate();
                }

            }

        });


        view.findViewById(R.id.calculator_add_button).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        executeCalculatorAction();
                    }
                }
        );


        view.findViewById(R.id.calculator_add_button_img)
            .setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    executeCalculatorAction();
                }
            });


        view.findViewById(R.id.cash_client_container).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {


                        if (!paid)
                        {
                            if (StaticValue.blackbox)
                            {
                                ((Operative) getContext()).openCustomerPopup("", -1,
                                        intent.getIntExtra("orderNumber", -1), false, -1, tableNumber
                                );
                            }
                            else
                            {
                                view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.cash_calculator_pad)
                                    .setVisibility(View.GONE);
                                view.findViewById(R.id.cash_recycler_container)
                                    .setVisibility(View.VISIBLE);
                                keypad    = false;
                                calculate = false;
                                calculator.setActivated(false);
                                keypadButton.setActivated(false);

                                int numberBill = intent.getIntExtra("orderNumber", -1);
                                if (numberBill == -1)
                                {
                                    numberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                                }
                                long             sessionTime = dbA.getLastClosing();
                                Date             date        = new Date(sessionTime);
                                SimpleDateFormat df2         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String           dateText    = df2.format(date);
                                int              newBillId   = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateText + "'");
                                if (newBillId == -11)
                                {
                                    if (listDataHeader.size() > 0)
                                    {

                                        //salvo i data header e i suoi subelementi
                                        Intent intent  = getActivity().getIntent();
                                        int    billId  = dbA.saveTotalBillForPayment(total, numberBill, 0);
                                        Intent intent1 = getActivity().getIntent();
                                        intent1.putExtra("billId", billId);
                                        bill_id                   = billId;
                                        CashFragment.this.bill_id = billId;
                                        if (billId != 0)
                                        {
                                            for (int i = 0; i < listDataHeader.size(); i++)
                                            {
                                                //salvo prodotto
                                                int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader
                                                        .get(i));
                                                if (listDataCustomer.size() > 0)
                                                {
                                                    Customer c = listDataCustomer.get(listDataHeader
                                                            .get(i)
                                                            .getClientPosition() - 1);
                                                    dbA.saveCustomerBillForPayment(c, prodId);
                                                }
                                                if (listDataChild.get(listDataHeader.get(i)) != null)
                                                {
                                                    for (int j = 0; j < listDataChild.get(listDataHeader
                                                            .get(i)).size(); j++)
                                                    {
                                                        //salvo tutti i figli
                                                        dbA.saveModifierBillForPayment(j, prodId, listDataChild
                                                                .get(listDataHeader.get(i))
                                                                .get(j));

                                                    }
                                                }
                                            }
                                            setModifyBar(false);
                                        }
                                    }

                                }
                                else
                                {
                                    if (listDataHeader.size() != 0)
                                    {
                                        dbA.updateBillPrice(billId, total);
                                        dbA.deleteCustomerForBill(billId);
                                        for (int i = 0; i < listDataHeader.size(); i++)
                                        {
                                            //salvo prodotto
                                            int check = dbA.checkProductBillForPayment(i, billId, listDataHeader
                                                    .get(i));
                                            if (check == -11)
                                            {
                                                //salvo nuovo prodotto
                                                int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader
                                                        .get(i));
                                                if (listDataCustomer.size() > 0)
                                                {
                                                    Customer c = listDataCustomer.get(listDataHeader
                                                            .get(i)
                                                            .getClientPosition() - 1);
                                                    dbA.saveCustomerBillForPayment(c, prodId);
                                                }
                                                if (listDataChild.get(listDataHeader.get(i)) != null)
                                                {
                                                    for (int j = 0; j < listDataChild.get(listDataHeader
                                                            .get(i)).size(); j++)
                                                    {
                                                        //salvo tutti i figli
                                                        dbA.saveModifierBillForPayment(j, prodId, listDataChild
                                                                .get(listDataHeader.get(i))
                                                                .get(j));
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                dbA.updateProductBillForPaymentQuantity(listDataHeader
                                                        .get(i)
                                                        .getQuantityInt(), check, listDataHeader.get(i)
                                                                                                .getProductId());
                                                if (listDataCustomer.size() > 0)
                                                {
                                                    Customer c = listDataCustomer.get(listDataHeader
                                                            .get(i)
                                                            .getClientPosition() - 1);
                                                    dbA.saveCustomerBillForPayment(c, check);
                                                }
                                                if (listDataChild.get(listDataHeader.get(i)) != null)
                                                {
                                                    for (int j = 0; j < listDataChild.get(listDataHeader
                                                            .get(i)).size(); j++)
                                                    {
                                                        int check1 = dbA.checkModifierBillForPayment(j, check, listDataChild
                                                                .get(listDataHeader.get(i))
                                                                .get(j));
                                                        if (check1 == -11)
                                                        {
                                                            //mod non c'è
                                                            dbA.saveModifierBillForPayment(j, check, listDataChild
                                                                    .get(listDataHeader.get(i))
                                                                    .get(j));
                                                        }
                                                        else
                                                        {
                                                            //update
                                                            if (listDataChild.get(listDataHeader.get(i))
                                                                             .get(j)
                                                                             .getModifierId() == -15)
                                                            {
                                                                dbA.updateModifierBillNote(listDataChild
                                                                        .get(listDataHeader.get(i))
                                                                        .get(j), check1);
                                                            }
                                                            dbA.updateModifierBillForPaymentQuantity(listDataChild
                                                                    .get(listDataHeader.get(i))
                                                                    .get(j)
                                                                    .getQuantityInt(), check1);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    dbA.deleteLeftProductFromBill(billId, listDataHeader.size() - 1);
                                }
                                long             sessionTimea = dbA.getLastClosing();
                                Date             datea        = new Date(sessionTimea);
                                SimpleDateFormat df2a         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String           dateTexta    = df2a.format(datea);
                                billId = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateTexta + "'");
                                view.findViewById(R.id.cash_client_container).setEnabled(false);
                                ((Operative) getContext()).openCustomerPopup("", -1,
                                        intent.getIntExtra("orderNumber", -1), false, -1, tableNumber
                                );

                            }
                        }
                    }
                }
        );


        view.findViewById(R.id.cash_client_container).setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                ArrayList<CashButtonLayout> cashList = (ArrayList<CashButtonLayout>) listAdapter.getGroups();

                resetArrayCustomer();

                for (Customer current : listDataCustomer)
                {
                    current.setActive(false);
                }

                listAdapter.setCustomerList(listDataCustomer);
                listAdapter.notifyDataSetChanged();
                activateAllCustomer();
                activityCommunicator = (ActivityCommunicator) context;
                activityCommunicator.goToMainPage();
                return true;
            }
        });


        roundButton.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            public void onSwipeRight()
            {

                keypadButton.setVisibility(View.VISIBLE);
                calculator.setVisibility(View.VISIBLE);
                roundButton.setVisibility(View.GONE);
                discountButton.setVisibility(View.GONE);
                if (discountButton.isActivated())
                {
                    discountMode = false;
                    discountEuro = true;
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.tv_eurosign).setVisibility(View.GONE);
                    view.findViewById(R.id.tv_percentage).setVisibility(View.GONE);
                    view.findViewById(R.id.tv_CANCEL).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    discountButton.setActivated(false);
                }
            }


            public void onSwipeLeft()
            {
            }


            public void onClick()
            {
                Log.i("POS CLICCATO", "RIGHT roundButton IN CashFragment AT onCreate");
                if (!paid)
                {
                    if (total != 0)
                    {
                        setRoundDiscount();
                    }
                    else
                    {
                        Toast.makeText(context, R.string.you_cant_apply_discount_to_a_null_total, Toast.LENGTH_SHORT)
                             .show();
                    }
                }

            }
        });


        discountButton.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            public void onSwipeRight()
            {

                keypadButton.setVisibility(View.VISIBLE);
                calculator.setVisibility(View.VISIBLE);
                roundButton.setVisibility(View.GONE);
                discountButton.setVisibility(View.GONE);
                if (discountButton.isActivated())
                {
                    discountMode = false;
                    discountEuro = true;
                    view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                    view.findViewById(R.id.tv_eurosign).setVisibility(View.GONE);
                    view.findViewById(R.id.tv_percentage).setVisibility(View.GONE);
                    view.findViewById(R.id.tv_CANCEL).setVisibility(View.GONE);
                    view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                    discountButton.setActivated(false);
                }
            }


            public void onSwipeLeft()
            {
            }


            public void onClick()
            {

                if (!paid)
                {
                    if (total != 0)
                    {
                        if (!discountMode)
                        {
                            discountMode = true;
                            view.findViewById(R.id.cash_calculator_pad).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.cash_recycler_container)
                                .setVisibility(View.GONE);
                            view.findViewById(R.id.cash_hline).setVisibility(View.GONE);
                            discountButton.setActivated(true);
                            if (keypad)
                            {
                                keypad = false;
                                keypadButton.setActivated(false);
                            }
                            if (calculate)
                            {
                                calculate = false;
                                calculator.setActivated(false);
                                view.findViewById(R.id.tv_DIV).setVisibility(View.GONE);
                                view.findViewById(R.id.tv_PER).setVisibility(View.GONE);
                                view.findViewById(R.id.tv_MINUS).setVisibility(View.GONE);
                                view.findViewById(R.id.tv_PLUS).setVisibility(View.GONE);
                            }
                            initDiscount();
                        }
                        else
                        {
                            discountMode = false;
                            discountEuro = true;
                            view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                            view.findViewById(R.id.tv_eurosign).setVisibility(View.GONE);
                            view.findViewById(R.id.tv_percentage).setVisibility(View.GONE);
                            view.findViewById(R.id.tv_CANCEL).setVisibility(View.GONE);
                            view.findViewById(R.id.cash_recycler_container)
                                .setVisibility(View.VISIBLE);
                            view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                            discountButton.setActivated(false);
                        }
                    }
                    else
                    {
                        Toast.makeText(context, R.string.you_cant_apply_discount_to_a_null_total, Toast.LENGTH_SHORT)
                             .show();
                    }
                }

            }
        });


        ImageView percentageButton = (ImageView) view.findViewById(R.id.euro_discount_image_vv);
        if (percentageButton.getVisibility() == View.VISIBLE)
        {
            percentageButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    //it should do what discountButton does
                    if (!paid)
                    {
                        if (total != 0)
                        {
                            if (!discountMode)
                            {
                                discountMode = true;
                                view.findViewById(R.id.cash_calculator_pad)
                                    .setVisibility(View.VISIBLE);
                                view.findViewById(R.id.cash_recycler_container)
                                    .setVisibility(View.GONE);
                                view.findViewById(R.id.cash_hline).setVisibility(View.GONE);
                                discountButton.setActivated(true);
                                roundButton.setVisibility(View.VISIBLE);
                                discountButton.setVisibility(View.VISIBLE);
                                if (saveButton.getVisibility() == View.VISIBLE)
                                {
                                    saveButton.setVisibility(View.GONE);
                                    paymentButton.setVisibility(View.GONE);
                                }
                                if (keypadButton.getVisibility() == View.VISIBLE)
                                {
                                    keypadButton.setVisibility(View.GONE);
                                    calculator.setVisibility(View.GONE);
                                }
                                if (keypad)
                                {
                                    keypad = false;
                                    keypadButton.setActivated(false);
                                }
                                if (calculate)
                                {
                                    calculate = false;
                                    calculator.setActivated(false);
                                    view.findViewById(R.id.tv_DIV).setVisibility(View.GONE);
                                    view.findViewById(R.id.tv_PER).setVisibility(View.GONE);
                                    view.findViewById(R.id.tv_MINUS).setVisibility(View.GONE);
                                    view.findViewById(R.id.tv_PLUS).setVisibility(View.GONE);
                                }
                                initDiscount();
                            }
                            else
                            {
                                discountMode = false;
                                discountEuro = true;
                                view.findViewById(R.id.cash_calculator_pad)
                                    .setVisibility(View.GONE);
                                view.findViewById(R.id.tv_eurosign).setVisibility(View.GONE);
                                view.findViewById(R.id.tv_percentage).setVisibility(View.GONE);
                                view.findViewById(R.id.cash_recycler_container)
                                    .setVisibility(View.VISIBLE);
                                view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                                discountButton.setActivated(false);
                            }
                        }
                        else
                        {
                            Toast.makeText(context, R.string.you_cant_apply_discount_to_a_null_total, Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }

                }
            });
        }


        return view;
    }


    public void goToPaymentFromServer(int numberBill, int billId)
    {
        Operative op     = (Operative) getActivity();
        Intent    intent = new Intent(getActivity(), PaymentActivity.class);
        intent.putExtra("username", op.getUser());
        intent.putExtra("isAdmin", op.isAdmin());
        intent.setAction("orderPayment");
        intent.putExtra("billId", billId);
        intent.putExtra("orderNumber", (numberBill));
        intent.putExtra("tableNumber", tableNumber);
        if (email != null && !mailSelected)
        {
            intent.putExtra("email", email);
            mailSelected = true;
        }
        else if (email != null && mailSelected)
        {
            intent.putExtra("more", mailSelected);
        }
        startActivity(intent);
    }


    public void goToTableFromServer(int numberBill, int billId)
    {
        Operative op     = (Operative) getActivity();
        Intent    intent = new Intent(getActivity(), TableActivity.class);
        intent.putExtra("username", op.getUser());
        intent.putExtra("isAdmin", op.isAdmin());
        intent.setAction("setTable");
        intent.putExtra("orderNumber", (numberBill - 1));
        intent.putExtra("tableNumber", tableNumber);
        intent.putExtra("roomId", roomId);
        intent.setAction("operation");
        intent.putExtra("billId", billId);
        startActivity(intent);
    }


    public void setCashListFromServer(TotalBill totalBill, ArrayList<CashButtonLayout> products, Map<CashButtonLayout, ArrayList<CashButtonListLayout>> mapServer, ArrayList<Customer> customers)
    {
        if (totalBill.getPaid() != 0)
        {
            view.findViewById(R.id.cash_order)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
            view.findViewById(R.id.cash_order_number)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
            view.findViewById(R.id.cash_table)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
            view.findViewById(R.id.cash_table_number)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
            view.findViewById(R.id.layout_1)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.green_2));
            paid = true;
        }
        else
        {
            paid = false;
        }

        listDataCustomer = new ArrayList<Customer>();
        listDataHeader   = new ArrayList<CashButtonLayout>();
        listDataChild    = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        map              = mapServer;
        //set total for bill if billId exist, else set 0.0f
        total = (double) totalBill.getTotal();
        DecimalFormat twoDForm = new DecimalFormat("#.00");
        if (total == 0)
        {
            viewTotal.setText("0,00");
        }
        else
        {
            viewTotal.setText(twoDForm.format(total).replace(".", ","));
        }
        listDataChild  = map;
        listDataHeader = products;
        boolean checkifAllCustomer = true;
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            if (listDataHeader.get(i).getProductId() != -20)
            {
                checkifAllCustomer = false;
            }
        }
        if (checkifAllCustomer)
        {
            cashListIndex = 0;
        }
        else
        {
            if (listDataHeader.get(listDataHeader.size() - 1).getProductId() == -20)
            {
                cashListIndex = listDataHeader.size() - 1;
            }
            else
            {
                cashListIndex = listDataHeader.size();
            }
        }
        //set cash adapter, is the parte where you see your product and modifier on the right (your bill pratically)
        listAdapter = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, billId, listDataCustomer, paid);
        //set customer if present
        listDataCustomer = customers;
        if (listDataCustomer.size() > 0)
        {
            listAdapter.setCustomerList(listDataCustomer);
            listAdapter.setFirstClient();
        }
        // setting list adapter
        expListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        //expand allgroups
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            expListView.expandGroup(i);
        }
        listAdapter.notifyDataSetChanged();

        totalDiscount = dbA.getBillDiscountPrice(billId);
        if (!paid)
        {
            setDiscountLayout(totalDiscount != 0.0f);
        }

        //maybe this should work
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("modifiedCustomer", false))
        {
            //set customer if present
            listDataCustomer = customers;
            if (listDataCustomer.size() > 0)
            {
                listAdapter.setCustomerList(listDataCustomer);
                listAdapter.setFirstClient();
            }
            // setting list adapter
            expListView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();

            //expand allgroups
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                expListView.expandGroup(i);
            }
            //listDataChild = map;
            listAdapter.notifyDataSetChanged();
        }
    }


    public void saveBillFromPopupOnServer(int code)
    {
        boolean check = false;
        if (listDataHeader.size() != 0)
        {
            check = true;
        }
        Intent intent     = getActivity().getIntent();
        int    numberBill = intent.getIntExtra("orderNumber", 1);

        RequestParam params = new RequestParam();

        Gson                                         gson      = new Gson();
        String                                       products  = gson.toJson(listDataHeader);
        String                                       customers = gson.toJson(listDataCustomer);
        Map<String, ArrayList<CashButtonListLayout>> test      = new HashMap<String, ArrayList<CashButtonListLayout>>();
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            test.put(String.valueOf(listDataHeader.get(i)
                                                  .getPosition()), listDataChild.get(listDataHeader.get(i)));
        }

        //String modifiers = gson.toJson(listDataChild);
        String    modifiers  = gson.toJson(test);
        Operative op         = (Operative) getActivity();

        params.add("username", op.getUser());
        params.add("androidId", StaticValue.androidId);
        params.add("billId", String.valueOf(billId));
        params.add("orderNumber", String.valueOf(numberBill));
        params.add("total", String.valueOf(total));
        params.add("products", products);
        params.add("customers", customers);
        params.add("modifiers", modifiers);
        params.add("totalDiscount", String.valueOf(totalDiscount));
        params.add("code", String.valueOf(code));
        params.add("cashListIndex", String.valueOf(cashListIndex));

        ((Operative) context).callHttpHandler("/saveBillFromPopup", params);
    }


    public void saveBillOnServer(String fromWhere, int cashIndex)
    {
        boolean check = false;
        if (listDataHeader.size() != 0)
        {
            check = true;
        }
        if (fromWhere.equals("tableButton") || fromWhere.equals("customerPopup"))
        {
            check = true;
        }

        if (!getModifyProduct() && !ModifierFragment.getModify() && check)
        {
            Intent intent     = getActivity().getIntent();
            int    numberBill = intent.getIntExtra("orderNumber", 1);

            RequestParam params = new RequestParam();

            Gson   gson      = new Gson();
            String products  = gson.toJson(listDataHeader);
            String customers = gson.toJson(listDataCustomer);
            Map<String, ArrayList<CashButtonListLayout>> test =
                    new HashMap<String, ArrayList<CashButtonListLayout>>();
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                test.put(String.valueOf(listDataHeader.get(i)
                                                      .getPosition()), listDataChild.get(listDataHeader
                        .get(i)));
            }

            //String modifiers = gson.toJson(listDataChild);
            String    modifiers = gson.toJson(test);
            Operative op        = (Operative) getActivity();

            params.add("username", op.getUser());
            params.add("androidId", StaticValue.androidId);
            params.add("billId", String.valueOf(billId));

            params.add("orderNumber", String.valueOf(numberBill));
            params.add("total", String.valueOf(total));
            params.add("products", products);
            params.add("customers", customers);
            params.add("modifiers", modifiers);
            params.add("totalDiscount", String.valueOf(totalDiscount));
            params.add("from", fromWhere);
            params.add("cashListIndex", String.valueOf(cashIndex));

            ((Operative) context).callHttpHandler("/saveBill", params);
        }
    }


    public void dropInitial()
    {
        dbA.execOnDb("delete from temp_table");
        dbA.execOnDb("delete from bill_total");
        dbA.execOnDb("delete from product_bill");
        dbA.execOnDb("delete from modifier_bill");
        dbA.execOnDb("delete from modifier_bill_notes");
        dbA.execOnDb("delete from bill_subdivision_paid");
        dbA.execOnDb("delete from item_subdivisions");
        dbA.execOnDb("delete from bill_total_credit");
        dbA.execOnDb("delete from bill_total_extra");
        dbA.execOnDb("delete from product_unspec_bill");
        dbA.execOnDb("delete from customer_bill");
        dbA.execOnDb("delete from table_use");
        dbA.execOnDb("delete from item_paid_spec");
    }


    public void resetListForSaveFromServer(int orderNumber)
    {
        dropInitial();

        listDataHeader   = new ArrayList<CashButtonLayout>();
        listDataChild    = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        listDataCustomer = new ArrayList<>();
        listAdapter      = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, false);

        expListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        activityCommunicator.deleteCurrentCash();
        totalDiscount = 0;

        setModifyBar(false);

        CustomTextView numberBillView = myself.findViewById(R.id.cash_order_number);
        numberBillView.setText(String.format("#%d", orderNumber + 1));

        CustomTextView numberTableSet = myself.findViewById(R.id.cash_table_number);
        String         setto          = "";
        numberTableSet.setText(setto);

        currentCustomerArray = new ArrayList<Integer>();
        Intent intent = getActivity().getIntent();
        intent.putExtra("orderNumber", orderNumber);
        intent.putExtra("billId", -1);

        bill_id     = -1;
        billId      = -1;
        tableNumber = -11;

        ((Operative) context).setBillId(-1);
    }


    public void setOrderNumberFromServer(int maxNumber)
    {
        Intent intent = getActivity().getIntent();
        if (maxNumber == -1)
        {
            //first time you open cashFragment in this session
            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
            int            orderNumber    = 0;
            numberBillView.setText(String.format("#%d", orderNumber + 1));
            intent.putExtra("orderNumber", orderNumber);
        }
        else
        {
            //set lates ordernumber
            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
            numberBillView.setText(String.format("#%d", maxNumber + 2));
            intent.putExtra("orderNumber", maxNumber + 1);

        }
    }


    public void showSpinner()
    {
        LayoutInflater layoutInflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_spinner, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        popupView.post(new Runnable()
        {
            @Override
            public void run()
            {
              /*  @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.spinner_container).getLayoutParams();
                rlp1.topMargin = (int) (150*density);
                popupView.findViewById(R.id.spinner_container).setLayoutParams(rlp1);*/

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(getActivity().findViewById(R.id.operative), 0, 0, 0);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //Do something after 100ms
                popupWindow.dismiss();
            }
        }, 5000);
    }


    public boolean checkIfAllCustomerHaveProducts()
    {
        if (listDataCustomer.size() > 0)
        {
            int listSize = listDataCustomer.size();
            int check    = 0;
            for (int i = 0; i < listDataCustomer.size(); i++)
            {
                for (CashButtonLayout list : listDataHeader)
                {
                    if (list.getClientPosition() == i + 1 && list.getProductId() != -20)
                    {
                        check++;
                        break;
                    }
                }
            }
            if (check == listSize)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    /**
     * popup that fires when you long click CancelButton, it asks if you want to save
     * current bill or to delete it
     */
    public void deleteBill()
    {
        if (!dbA.checkIfBillSplitPaid(billId))
        {
            if (!getModifyProduct() && !ModifierFragment.getModify())
            {
                Intent intent         = getActivity().getIntent();
                int    numberBill     = intent.getIntExtra("orderNumber", -1);
                int    bestNumberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                if (numberBill < bestNumberBill)
                {
                    numberBill = bestNumberBill;
                }
                listDataCustomer     = new ArrayList<Customer>();
                currentCustomerArray = new ArrayList<Integer>();
                listDataHeader       = new ArrayList<CashButtonLayout>();
                listDataChild        = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                listAdapter          = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);
                expListView.setAdapter(listAdapter);
                listDataCustomer     = new ArrayList<Customer>();
                currentCustomerArray = new ArrayList<Integer>();
                listAdapter.notifyDataSetChanged();
                activityCommunicator.deleteCurrentCash();
                setModifyBar(false);
                total = 0.0;
                viewTotal.setText("0,00");
                totalDiscount = 0.0f;
                if (view.findViewById(R.id.euro_icon_discount).getVisibility() == View.VISIBLE)
                {
                    setDiscountLayout(false);
                }
                long sessionTime = dbA.getLastClosing();
                int  number      = dbA.getMaxNumberOrderId(sessionTime);
                intent.putExtra("tableNumber", -1);
                if (number + 1 > numberBill + 1)
                {
                    intent.putExtra("orderNumber", (number + 1));
                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                    numberBillView.setText(String.format("#%d", number + 2));
                }
                else if (dbA.getPaidBill(billId) != 0 && dbA.getPaidBill(billId) != -1)
                {
                    intent.putExtra("orderNumber", numberBill + 1);
                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                    numberBillView.setText(String.format("#%d", numberBill + 2));
                }
                CustomTextView numberTableSet = (CustomTextView) myself.findViewById(R.id.cash_table_number);
                String         setto          = "";
                numberTableSet.setText(setto);
                dbA.deleteBillData1(billId, context);
                dbA.deleteDiscuntTotal(billId);
                dbA.updateDiscountToZero(billId);

                intent.putExtra("billId", -1);
                billId = -1;
            }
        }
    }


    /**
     * No button: it deletes everything
     * Yes button: it saves bill, but no printing; then it clears everything
     *
     * @param popView
     * @param popWindow
     */
    public void setUpSaveBillPopup(View popView, PopupWindow popWindow, View v)
    {
        popView.findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //delete everything
                if (!dbA.checkIfBillSplitPaid(billId))
                {
                    if (!getModifyProduct() && !ModifierFragment.getModify())
                    {
                        Intent intent         = getActivity().getIntent();
                        int    numberBill     = intent.getIntExtra("orderNumber", -1);
                        int    bestNumberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                        if (numberBill < bestNumberBill)
                        {
                            numberBill = bestNumberBill;
                        }
                        listDataCustomer     = new ArrayList<Customer>();
                        currentCustomerArray = new ArrayList<Integer>();
                        listDataHeader       = new ArrayList<CashButtonLayout>();
                        listDataChild        = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                        listAdapter          = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);
                        expListView.setAdapter(listAdapter);
                        listDataCustomer     = new ArrayList<Customer>();
                        currentCustomerArray = new ArrayList<Integer>();
                        listAdapter.notifyDataSetChanged();
                        activityCommunicator.deleteCurrentCash();
                        setModifyBar(false);
                        total = 0.0;
                        viewTotal.setText("0,00");

                        totalDiscount = 0.0f;
                        setTotalDiscount(0.0f);
                        if (v.findViewById(R.id.euro_icon_discount).getVisibility() == View.VISIBLE)
                        {
                            setDiscountLayout(false);
                        }
                        long sessionTime = dbA.getLastClosing();
                        int  number      = dbA.getMaxNumberOrderId(sessionTime);
                        intent.putExtra("tableNumber", -1);
                        if (number + 1 > numberBill + 1)
                        {
                            intent.putExtra("orderNumber", (number + 1));
                            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                            numberBillView.setText(String.format("#%d", number + 2));
                        }
                        else if (dbA.getPaidBill(billId) != 0 && dbA.getPaidBill(billId) != -1)
                        {
                            intent.putExtra("orderNumber", numberBill + 1);
                            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                            numberBillView.setText(String.format("#%d", numberBill + 2));
                        }
                        else
                        {
                            intent.putExtra("orderNumber", numberBill + 1);
                            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                            numberBillView.setText(String.format("#%d", numberBill + 1));
                        }
                        CustomTextView tNumberLayout = (CustomTextView) v.findViewById(R.id.cash_table_not_set);
                        tNumberLayout.setVisibility(View.VISIBLE);
                        ((CustomTextView) v.findViewById(R.id.cash_table)).setVisibility(View.GONE);
                        tNumber = (CustomTextView) v.findViewById(R.id.cash_table_number);
                        if (tNumber != null)
                        {
                            tNumber.setVisibility(View.GONE);
                        }
                        if (dbA.getPaidBill(billId) == 0)
                        {
                            dbA.deleteBillData(numberBill, context);
                            dbA.deleteDiscuntTotal(billId);
                        }
                        dbA.updateDiscountToZero(billId);

                        intent.putExtra("billId", -1);
                        billId = -1;
                    }
                }

                popWindow.dismiss();
            }
        });

        popView.findViewById(R.id.yes_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (!getModifyProduct() && !ModifierFragment.getModify() && listDataHeader.size() != 0)
                {
                    //addDiscountToBill();
                    Intent intent = getActivity().getIntent();
                    intent.setAction("normal");
                    int numberBill = intent.getIntExtra("orderNumber", -1);
                    if (numberBill == -1)
                    {
                        numberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                    }
                    long             sessionTime = dbA.getLastClosing();
                    Date             date        = new Date(sessionTime);
                    SimpleDateFormat df2         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String           dateText1   = df2.format(date);
                    int              newBillId   = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateText1 + "';");
                    if (newBillId == -11)
                    {
                        saveBill(0);
                    }
                    else
                    {
                        if (listDataHeader.size() != 0)
                        {
                            updateBill(billId);
                            printOrderBill(billId);
                        }
                        listDataHeader = new ArrayList<CashButtonLayout>();
                        listDataChild  = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                        listAdapter    = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, false);
                        expListView.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                        activityCommunicator.deleteCurrentCash();

                        setModifyBar(false);
                    }

                    int number = dbA.getMaxOrderId(sessionTime);
                    if (number + 1 > numberBill + 1)
                    {
                        intent.putExtra("orderNumber", (number + 1));
                        CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                        numberBillView.setText("#" + (number + 2));
                    }
                    else
                    {
                        intent.putExtra("orderNumber", (numberBill + 1));
                        CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                        numberBillView.setText("#" + (numberBill + 2));
                    }
                    CustomTextView numberTableSet = (CustomTextView) myself.findViewById(R.id.cash_table_number);
                    String         setto          = "";
                    numberTableSet.setText(setto);

                    listDataCustomer     = new ArrayList<Customer>();
                    currentCustomerArray = new ArrayList<Integer>();

                    intent.putExtra("billId", -1);
                }
                popWindow.dismiss();
            }
        });
    }


    public void setupDismissKeyboard(View view)
    {
        //Set up touch listener for non-text box views to hide keyboard.
        if ((view instanceof EditText))
        {
            ((EditText) view).setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_NEXT)
                    {
                        keyboard_next_flag = true;
                    }
                    return false;
                }
            });
            view.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (!hasFocus)
                    {
                        if (!(((Activity) context).getCurrentFocus() instanceof EditText) && !keyboard_next_flag)
                        {
                            Log.d("OnFocusChange", "You clicked out of an Edit Text!");
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        keyboard_next_flag = false;
                    }
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupDismissKeyboard(innerView);
            }
        }
    }


    public void setOldList(int groupPosition)
    {
        if (oldModifiedProduct.getCashListSize() == 0)
        {
            oldModifiedProduct = listDataHeader.get(groupPosition);
            ArrayList<CashButtonListLayout> a = listDataChild.get(listDataHeader.get(groupPosition));

            oldModifiedProduct.setNewCashList(listDataChild.get(listDataHeader.get(groupPosition)));

            oldListDataChild.put(oldModifiedProduct, oldModifiedProduct.getCashList());
        }

        Customer customer = new Customer();
        if (listDataCustomer.size() > 0)
        {
            customer     = listDataCustomer.get(listDataHeader.get(groupPosition)
                                                              .getClientPosition() - 1);
            mailSelected = true;
        }
        if (billId > 0 && groupPosition < cashListIndex)
        {
            printOrderDelete(groupPosition, customer);
        }
    }


    public void triggerCancelButton()
    {
        keypad    = false;
        calculate = false;
        myself.findViewById(R.id.layout_5).setActivated(false);
        myself.findViewById(R.id.layout_6).setActivated(false);
        myself.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
        myself.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
        myself.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
        activityCommunicator = (ActivityCommunicator) getActivity();
        ModifierFragment.setModify(false);
        deleteProduct = false;
    }


    public void setNewCustomerFromPopup(int customerId, String customerName, boolean isGroup)
    {
        if (listDataHeader != null)
        {
            if (listDataHeader.size() > 0)
            {
                boolean present = false;
                if (listDataCustomer.size() == 0)
                {
                    Customer customer = new Customer();
                    customer.setPosition(listDataCustomer.size() + 1);
                    int position = listDataCustomer.size() + 1;
                    customer.setDescription("Customer " + position);
                    customer.setActive(false);
                    listDataCustomer.add(customer);
                    for (CashButtonLayout b : listDataHeader)
                    {
                        b.setClientPosition(listDataCustomer.size());
                    }
                }
                else
                {
                    for (Customer c : listDataCustomer)
                    {
                        c.setActive(false);
                        if (customerId != -1)
                        {
                            if (c.getCustomerId() == customerId)
                            {
                                present = true;
                            }
                        }
                    }
                }
                if (!present)
                {
                    Customer lastCustomer = new Customer();
                    lastCustomer.setPosition(listDataCustomer.size() + 1);
                    int position = listDataCustomer.size() + 1;
                    if (customerName.equals(""))
                    {
                        if (isGroup)
                        {
                            lastCustomer.setDescription("Group " + position);
                        }
                        else
                        {
                            lastCustomer.setDescription("Customer " + position);
                        }
                    }
                    else
                    {
                        lastCustomer.setDescription(customerName);
                    }
                    lastCustomer.setCustomerId(customerId);

                    lastCustomer.setActive(true);
                    listDataCustomer.add(lastCustomer);

                    resetArrayCustomer();
                    currentCustomerArray.add(listDataCustomer.size());

                    button = new CashButtonLayout();
                    button.setTitle("");
                    button.setPrice(0.0f);
                    button.setQuantity(1);
                    button.setProductId(-20);
                    button.setIsDelete(false);
                    button.setModifyModifier(false);
                    button.setPosition(listDataHeader.size());
                    button.setID(-1);
                    button.setClientPosition(listDataCustomer.size());
                    //add button to listDataHeader (groups list)
                    listDataHeader.add(button);
                    listAdapter.setFirstClient();
                    listAdapter.setCustomerList(listDataCustomer);
                    listAdapter.notifyDataSetChanged();
                    expListView.setSelectedGroup(listDataHeader.size() - 1);
                    mailSelected = true;
                }
                else
                {
                    Toast.makeText(context, "Customer Already inserted", Toast.LENGTH_SHORT).show();
                    for (Customer c : listDataCustomer)
                    {
                        c.setActive(false);
                    }
                    resetArrayCustomer();
                    listAdapter.notifyDataSetChanged();
                }
            }
            else
            {

                Customer customer = new Customer();
                customer.setPosition(listDataCustomer.size() + 1);
                int position = listDataCustomer.size() + 1;
                if (customerName.equals(""))
                {
                    if (isGroup)
                    {
                        customer.setDescription("Group " + position);
                    }
                    else
                    {
                        customer.setDescription("Customer " + position);
                    }
                }
                else
                {
                    customer.setDescription(customerName);
                }
                customer.setCustomerId(customerId);
                customer.setActive(true);
                listDataCustomer.add(customer);

                resetArrayCustomer();
                currentCustomerArray.add(listDataCustomer.size());

                button = new CashButtonLayout();
                button.setTitle("");
                button.setPrice(0.0f);
                button.setQuantity(1);
                button.setProductId(-20);
                button.setIsDelete(false);
                button.setModifyModifier(false);
                button.setPosition(listDataHeader.size());
                button.setID(-1);
                button.setClientPosition(listDataCustomer.size());
                //add button to listDataHeader (groups list)
                listDataHeader.add(button);
                listAdapter.setFirstClient();
                listAdapter.setCustomerList(listDataCustomer);
                listAdapter.notifyDataSetChanged();
                expListView.setSelectedGroup(listDataHeader.size() - 1);
                //non c'è nessun elemento dentro per il momento
                //aggiungo un posto vuoto
            }
            activityCommunicator = (ActivityCommunicator) context;
            activityCommunicator.goToMainPage();
            ((Operative) getContext()).setTypeOfFavourites();
        }
    }


    public void saveBillForCustomer()
    {
        Intent intent1     = getActivity().getIntent();
        int    orderNumber = intent1.getIntExtra("orderNumber", -1);
        if (orderNumber == -1)
        {
            //check database, se nitne
            long lastClose = dbA.getLastClosing();
            int  maxNumber = dbA.getMaxOrderId(lastClose);
            //int maxNumber = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
            if (maxNumber == -1)
            {
                //first time you open cashFragment in this session
                orderNumber = 0;
                intent1.putExtra("orderNumber", orderNumber);
            }
            else
            {
                //set lates ordernumber
                intent1.putExtra("orderNumber", maxNumber + 1);

            }
        }
        else
        {
            //here you came from anoter activity, e.g. clientsActivity
            intent1.putExtra("orderNumber", orderNumber);

        }
        long             sessionTime = dbA.getLastClosing();
        Date             date        = new Date(sessionTime);
        SimpleDateFormat df2         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String           dateText    = df2.format(date);
        int              newBillId   = dbA.checkIfExists("Select * from bill_total where bill_number=" + orderNumber + " and creation_time>='" + dateText + "'");
        if (newBillId == -11)
        {
            saveBillForTable();
        }
        else
        {
            updateBill(newBillId);

        }
    }


    public void saveBillForCustomerForServer(int orderNumber, int customer_id, boolean modify, int modifyPosition)
    {

        RequestParam params = new RequestParam();

        Gson   gson      = new Gson();
        String products  = gson.toJson(listDataHeader);
        String customers = gson.toJson(listDataCustomer);
        Map<String, ArrayList<CashButtonListLayout>> test =
                new HashMap<String, ArrayList<CashButtonListLayout>>();
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            test.put(String.valueOf(i), listDataChild.get(listDataHeader.get(i)));
        }

        //String modifiers = gson.toJson(listDataChild);
        String modifiers = gson.toJson(test);
        params.add("orderNumber", String.valueOf(orderNumber));
        params.add("total", String.valueOf(total));
        params.add("printIndex", String.valueOf(0));
        params.add("products", products);
        params.add("customers", customers);
        params.add("modifiers", modifiers);
        params.add("totalDiscount", String.valueOf(totalDiscount));

        params.add("customerId", String.valueOf(customer_id));
        params.add("modify", String.valueOf(modify));
        params.add("modifyPosition", String.valueOf(modifyPosition));

        ((Operative) context).callHttpHandler("/saveBillForCustomer", params);

        /**
         Intent intent1 = getActivity().getIntent();
         int orderNumber = intent1.getIntExtra("orderNumber", -1);
         if(orderNumber==-1){
         //check database, se nitne
         long lastClose = dbA.getLastClosing();
         int maxNumber = dbA.getMaxOrderId(lastClose);
         //int maxNumber = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
         if(maxNumber==-1){
         //first time you open cashFragment in this session
         orderNumber = 0;
         intent1.putExtra("orderNumber", orderNumber);
         }else{
         //set lates ordernumber
         intent1.putExtra("orderNumber", maxNumber+1);

         }
         }else{
         //here you came from anoter activity, e.g. clientsActivity
         intent1.putExtra("orderNumber", orderNumber);

         }
         long sessionTime = dbA.getLastClosing();
         Date date=new Date(sessionTime);
         SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         String dateText = df2.format(date);
         int newBillId = dbA.checkIfExists("Select * from bill_total where bill_number=" + orderNumber+ " and creation_time>='"+dateText+"'");
         if(newBillId==-11 ) {
         saveBillForTable();
         }else{
         updateBill(newBillId);

         }
         */
    }


    public void modifyCustomerFromPopup(int customerId, String customerName, int customerPosition, boolean isGroup)
    {
        if (customerPosition <= listDataCustomer.size())
        {
            listDataCustomer.get(customerPosition - 1).setCustomerId(customerId);
            listDataCustomer.get(customerPosition - 1).setDescription(customerName);
            listDataCustomer.get(customerPosition - 1).setActive(true);
            resetArrayCustomer();
            currentCustomerArray.add(listDataCustomer.get(customerPosition - 1).getPosition());
            listAdapter.setCustomerList(listDataCustomer);
            listAdapter.notifyDataSetChanged();

            activityCommunicator = (ActivityCommunicator) context;
            activityCommunicator.goToMainPage();
        }
        else
        {
            listDataCustomer = dbA.getCustomerData(billId);

            if (listDataCustomer.size() > 0)
            {
                listDataCustomer.get(customerPosition - 1).setCustomerId(customerId);
                listDataCustomer.get(customerPosition - 1).setDescription(customerName);
                listDataCustomer.get(customerPosition - 1).setActive(true);
                resetArrayCustomer();
                currentCustomerArray.add(listDataCustomer.get(customerPosition - 1).getPosition());
                listAdapter.setCustomerList(listDataCustomer);
                listAdapter.notifyDataSetChanged();
            }
            else
            {
                Customer customer = new Customer();
                customer.setPosition(listDataCustomer.size() + 1);
                int position = listDataCustomer.size() + 1;
                if (customerName.equals(""))
                {
                    if (isGroup)
                    {
                        customer.setDescription("Group " + position);
                    }
                    else
                    {
                        customer.setDescription("Customer " + position);
                    }
                }
                else
                {
                    customer.setDescription(customerName);
                }
                customer.setCustomerId(customerId);
                customer.setActive(true);
                listDataCustomer.add(customer);

                resetArrayCustomer();
                currentCustomerArray.add(listDataCustomer.size());

                button = new CashButtonLayout();
                button.setTitle("");
                button.setPrice(0.0f);
                button.setQuantity(1);
                button.setProductId(-20);
                button.setIsDelete(false);
                button.setModifyModifier(false);
                button.setPosition(listDataHeader.size());
                button.setID(-20);
                button.setClientPosition(listDataCustomer.size());
                //add button to listDataHeader (groups list)
                listDataHeader.add(button);
                listAdapter.setFirstClient();
                listAdapter.setCustomerList(listDataCustomer);
                listAdapter.notifyDataSetChanged();
                expListView.setSelectedGroup(listDataHeader.size() - 1);
            }
            activityCommunicator = (ActivityCommunicator) context;
            activityCommunicator.goToMainPage();
        }
    }


    /**
     * KEYPAD AND CALCULATE PART
     */

    public void initKeypad()
    {
        //Open popup to name unspecific item and initialize button in keypad
        ((Operative) getContext()).openUnspecProductPopup();
        CustomTextView textView = (CustomTextView) view.findViewById(R.id.calculator_input_text);
        textView.setText("€");
        amount = new StringBuilder();
        amount.append("€");

        view.findViewById(R.id.tv_DIV).setVisibility(View.GONE);
        view.findViewById(R.id.tv_PER).setVisibility(View.GONE);
        view.findViewById(R.id.tv_MINUS).setVisibility(View.GONE);
        view.findViewById(R.id.tv_PLUS).setVisibility(View.GONE);

        view.findViewById(R.id.tv_10).setVisibility(View.VISIBLE);
        view.findViewById(R.id.d_10_label).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_20).setVisibility(View.VISIBLE);
        view.findViewById(R.id.d_20_label).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_30).setVisibility(View.VISIBLE);
        ((CustomTextView) view.findViewById(R.id.tv_30)).setText("3");
        view.findViewById(R.id.d_30_label).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_50).setVisibility(View.VISIBLE);
        ((CustomTextView) view.findViewById(R.id.tv_50)).setText("5");
        view.findViewById(R.id.d_50_label).setVisibility(View.VISIBLE);
        setupDigits();

    }


    public void initCalculate()
    {
        //init calculator
        CustomTextView textView = (CustomTextView) view.findViewById(R.id.calculator_input_text);
        textView.setText("");
        amount = new StringBuilder();
        view.findViewById(R.id.tv_DIV).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_PER).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_MINUS).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_PLUS).setVisibility(View.VISIBLE);

        view.findViewById(R.id.tv_10).setVisibility(View.GONE);
        view.findViewById(R.id.d_10_label).setVisibility(View.GONE);
        view.findViewById(R.id.tv_20).setVisibility(View.GONE);
        view.findViewById(R.id.d_20_label).setVisibility(View.GONE);
        view.findViewById(R.id.tv_30).setVisibility(View.GONE);
        view.findViewById(R.id.d_30_label).setVisibility(View.GONE);
        view.findViewById(R.id.tv_50).setVisibility(View.GONE);
        view.findViewById(R.id.d_50_label).setVisibility(View.GONE);
        setupDigits();
    }


    public void initDiscount()
    {
        //init discount keypad
        ((CustomTextView) view.findViewById(R.id.calculator_input_text)).setText("");
        amount = new StringBuilder();
        view.findViewById(R.id.tv_eurosign).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_percentage).setVisibility(View.VISIBLE);
        ((CustomTextView) view.findViewById(R.id.tv_30)).setText("");
        (view.findViewById(R.id.tv_30)).setVisibility(View.GONE);
        ((CustomTextView) view.findViewById(R.id.tv_50)).setText("");
        (view.findViewById(R.id.tv_50)).setVisibility(View.GONE);
        view.findViewById(R.id.calculator_add_button_img).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_CANCEL).setVisibility(View.VISIBLE);

        view.findViewById(R.id.tv_10).setVisibility(View.GONE);
        view.findViewById(R.id.tv_20).setVisibility(View.GONE);
        view.findViewById(R.id.d_10_label).setVisibility(View.GONE);
        view.findViewById(R.id.d_20_label).setVisibility(View.GONE);
        view.findViewById(R.id.d_30_label).setVisibility(View.GONE);
        view.findViewById(R.id.d_50_label).setVisibility(View.GONE);
        view.findViewById(R.id.keypad_add_button_img).setVisibility(View.GONE);

        setupDigits();
    }


    /**
     * Set up number button for calculator, keypad and discount
     */
    private void setupDigits()
    {
        RelativeLayout digitContainer = (RelativeLayout) myself.findViewById(R.id.digits_subcontainer);
        View           v;
        for (int i = 0; i < digitContainer.getChildCount(); i++)
        {
            v = digitContainer.getChildAt(i);
            if (discountMode)
            {
                if (discountEuro)
                {
                    amount = new StringBuilder();
                    amount.append("€");
                    ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount);
                }
            }
            if ((v.getId() == R.id.d_10))
            {
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (keypad)
                        {
                            amount = new StringBuilder();
                            amount.append("1€");
                        }
                        else if (!discountMode)
                        {
                            if (amount.length() > 0)
                            {
                                //negative number
                                if (amount.substring(0, 1).equals("-"))
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+"))
                                    {
                                        amount.append("/");
                                        operationDone = false;
                                    }
                                }
                                //positive number
                                else
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+") && !amount.toString()
                                                                                       .contains("-"))
                                    {
                                        amount.append("/");
                                        operationDone = false;
                                    }
                                }
                            }
                        }
                        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                .toString());
                        if (discountMode)
                        {
                            discountEuro = true;
                            amount       = new StringBuilder();
                            amount.append("€");
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                        }
                    }
                });
            }
            else if ((v.getId() == R.id.tv_DIV))
            {
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (keypad)
                        {
                            amount = new StringBuilder();
                            amount.append("1€");
                        }
                        else if (!discountMode)
                        {
                            if (amount.length() > 0)
                            {
                                //negative number
                                if (amount.substring(0, 1).equals("-"))
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+"))
                                    {
                                        amount.append("/");
                                        operationDone = false;
                                    }
                                }
                                //positive number
                                else
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+") && !amount.toString()
                                                                                       .contains("-"))
                                    {
                                        amount.append("/");
                                        operationDone = false;
                                    }
                                }
                            }
                        }
                        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                .toString());
                        if (discountMode)
                        {
                            discountEuro = true;
                            amount       = new StringBuilder();
                            amount.append("€");
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                        }
                    }
                });
            }
            else if ((v.getId() == R.id.tv_eurosign))
            {
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (keypad)
                        {
                            amount = new StringBuilder();
                            amount.append("1€");
                        }
                        else if (!discountMode)
                        {
                            if (amount.length() > 0)
                            {
                                //negative number
                                if (amount.substring(0, 1).equals("-"))
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+"))
                                    {
                                        amount.append("/");
                                        operationDone = false;
                                    }
                                }
                                //positive number
                                else
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+") && !amount.toString()
                                                                                       .contains("-"))
                                    {
                                        amount.append("/");
                                        operationDone = false;
                                    }
                                }
                            }
                        }
                        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                .toString());
                        if (discountMode)
                        {
                            discountEuro = true;
                            amount       = new StringBuilder();
                            amount.append("€");
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                        }
                    }
                });
            }
            else if ((v.getId() == R.id.d_20) || (v.getId() == R.id.tv_PER) || (v.getId() == R.id.tv_percentage))
            {
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (keypad)
                        {
                            amount = new StringBuilder();
                            amount.append("2€");
                        }
                        else if (!discountMode)
                        {
                            if (amount.length() > 0)
                            {
                                //negative number
                                if (amount.substring(0, 1).equals("-"))
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+"))
                                    {
                                        amount.append("x");
                                        operationDone = false;
                                    }
                                }
                                //positive number
                                else
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+") && !amount.toString()
                                                                                       .contains("-"))
                                    {
                                        amount.append("x");
                                        operationDone = false;
                                    }
                                }
                            }
                        }
                        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                .toString());
                        if (discountMode)
                        {
                            discountEuro = false;
                            amount       = new StringBuilder();
                            amount.append("%");
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                        }
                    }
                });
            }
            else if ((v.getId() == R.id.d_30) || (v.getId() == R.id.tv_MINUS))
            {
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (keypad)
                        {
                            amount = new StringBuilder();
                            amount.append("3€");
                        }
                        else if (!discountMode)
                        {
                            if (amount.length() > 0)
                            {
                                //negative number
                                if (amount.substring(0, 1).equals("-"))
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+"))
                                    {
                                        amount.append("-");
                                        operationDone = false;
                                    }
                                }
                                //positive number
                                else
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+") && !amount.toString()
                                                                                       .contains("-"))
                                    {
                                        amount.append("-");
                                        operationDone = false;
                                    }
                                }
                            }
                        }
                        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                .toString());
                        if (discountMode)
                        {
                            amount = new StringBuilder();
                            amount.append("€");
                            discountToBeDeleted = true;
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                        }
                    }
                });
            }
            else if ((v.getId() == R.id.d_50) || (v.getId() == R.id.tv_PLUS))
            {
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (keypad)
                        {
                            amount = new StringBuilder();
                            amount.append("5€");
                        }
                        else if (!discountMode)
                        {
                            if (amount.length() > 0)
                            {
                                //negative number
                                if (amount.substring(0, 1).equals("-"))
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+"))
                                    {
                                        amount.append("+");
                                        operationDone = false;
                                    }
                                }
                                //positive number
                                else
                                {
                                    if (!amount.toString().contains("/") && !amount.toString()
                                                                                   .contains("x") &&
                                            !amount.toString().contains("+") && !amount.toString()
                                                                                       .contains("-"))
                                    {
                                        amount.append("+");
                                        operationDone = false;
                                    }
                                }
                            }
                        }
                        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                .toString());
                    }
                });
            }
            else
            {
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (operationDone)
                        {
                            amount = new StringBuilder();
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                            buildString(((CustomButton) v).getText().charAt(0));
                            operationDone = false;
                        }
                        else
                        {
                            buildString(((CustomButton) v).getText().charAt(0));
                        }
                    }
                });
            }

            v.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    char c = ((CustomButton) v).getText().charAt(0);
                    if (c == 'C')
                    {
                        amount        = new StringBuilder();
                        operationDone = false;
                        if (keypad)
                        {
                            amount.append("€");
                        }
                        dotAdded = false;
                        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                .toString());
                    }
                    return false;
                }
            });
        }
    }


    /**
     * build string to show in calculator or keypad
     *
     * @param c
     */
    private void buildString(char c)
    {
        //keypad
        if (keypad)
        {
            if (c != 'C')
            {
                //append char and € char
                amount.deleteCharAt(amount.length() - 1);
                amount.append(c);
                amount.append("€");
            }
            else
            {
                //clear amount and add € char
                if (amount.length() > 1)
                {
                    amount.deleteCharAt(amount.length() - 1);
                    amount.deleteCharAt(amount.length() - 1);
                    amount.append("€");
                }
            }
        }
        else if (!discountMode)
        {
            if (c != 'C')
            {
                if (c == '.')
                {
                    if (!dotAdded)
                    {
                        amount.append(c);
                        dotAdded = true;
                    }
                }
                else
                {
                    amount.append(c);
                }
            }
            else
            {
                if (amount.length() > 0)
                {
                    if (amount.length() - 1 == '.')
                    {
                        amount.deleteCharAt(amount.length() - 1);
                        dotAdded = false;
                    }
                    else
                    {
                        amount.deleteCharAt(amount.length() - 1);
                    }
                }
            }
        }
        else if (discountMode)
        {
            if (c != 'C')
            {
                discountToBeDeleted = false;
                if (amount.toString().contains("€"))
                {
                    amount.deleteCharAt(amount.length() - 1);
                    amount.append(c);
                    amount.append("€");
                }
                else if (amount.toString().contains("%"))
                {
                    amount.deleteCharAt(amount.length() - 1);
                    amount.append(c);
                    amount.append("%");
                }
            }
            else
            {
                discountToBeDeleted = false;
                if (amount.length() > 0 && amount.length() != 1)
                {
                    amount.deleteCharAt(amount.length() - 2);
                }
                if (amount.length() == 1)
                {
                    amount.deleteCharAt(amount.length() - 1);
                }
            }
        }
        ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount.toString());
    }


    /**
     * execute keypad or calculator action
     */
    public void executeCalculatorAction()
    {
        //if keypad add an unspec article
        if (keypad)
        {
            //add to list
            if (amount.length() != 1)
            {
                if (unspecItemName.isEmpty())
                {
                    unspecItemName = "Articolo";
                }

                float        price        = Float.parseFloat(amount.substring(0, amount.length() - 1));
                ButtonLayout buttonLayout = dbA.fetchButtonByQuery("SELECT * FROM button WHERE id=-30;");

                buttonLayout.setPrice(price);
                if (listDataCustomer.size() > 0)
                {
                    for (int j = 0; j < currentCustomerArray.size(); j++)
                    {

                        int lastPosition = returnLastPositionForCustomer(currentCustomerArray.get(j));
                        if (listDataHeader.get(lastPosition).getProductId() == -20)
                        {
                            button = new CashButtonLayout();
                            listDataHeader.get(lastPosition).setTitle(unspecItemName);
                            listDataHeader.get(lastPosition).setPrice(price);
                            listDataHeader.get(lastPosition).setQuantity(1);
                            listDataHeader.get(lastPosition).setProductId(-30);
                            listDataHeader.get(lastPosition).setIsDelete(false);
                            listDataHeader.get(lastPosition).setModifyModifier(false);
                            listDataHeader.get(lastPosition).setID(buttonLayout.getID());
                            listDataHeader.get(lastPosition).setPosition(lastPosition);
                            listDataHeader.get(lastPosition).setVat(StaticValue.staticVat);


                        }
                        else
                        {
                            button = new CashButtonLayout();
                            button.setTitle(unspecItemName);
                            button.setPrice(price);
                            button.setQuantity(1);
                            button.setProductId(-30);
                            button.setIsDelete(false);
                            button.setModifyModifier(false);
                            button.setID(buttonLayout.getID());
                            button.setVat(StaticValue.staticVat);

                            button.setClientPosition(currentCustomerArray.get(j));
                            if (lastPosition == listDataHeader.size() - 1)
                            {
                                //add button to listDataHeader (groups list)
                                button.setPosition(listDataHeader.size());
                                listDataHeader.add(button);
                            }
                            else
                            {
                                button.setPosition(lastPosition);
                                listDataHeader.add(lastPosition + 1, button);
                            }
                        }

                    }

                    updateTotal(price * currentCustomerArray.size());
                }
                else
                {
                    button = new CashButtonLayout();
                    button.setTitle(unspecItemName);
                    button.setPrice(price);
                    button.setQuantity(1);
                    button.setProductId(-30);
                    button.setIsDelete(false);
                    button.setModifyModifier(false);
                    button.setID(buttonLayout.getID());
                    button.setPosition(listDataHeader.size());
                    button.setClientPosition(0);
                    button.setVat(StaticValue.staticVat);
                    //add button to listDataHeader (groups list)
                    listDataHeader.add(button);

                    updateTotal(price);
                }
                view.findViewById(R.id.layout_5).setActivated(false);

                listAdapter.notifyDataSetChanged();
                resetCashNotChanghed();
                expListView.setSelection(listDataHeader.size() - 1);

                view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
                view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
                keypad = false;

            }

        }
        else if (calculate)
        {
            //execute calculation
            String firstNumber  = "";
            String secondNumber = "";
            float  result       = 0.0f;
            int    opType       = searchOperation();
            switch (opType)
            {
                case 1:
                    // operation /
                    firstNumber = amount.substring(0, amount.indexOf("/"));
                    secondNumber = amount.substring(amount.indexOf("/") + 1, amount.length());
                    result = Float.valueOf(firstNumber) / Float.valueOf(secondNumber);
                    amount = new StringBuilder();
                    amount.append(result);
                    operationDone = true;
                    ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                            .toString());

                    break;
                case 2:
                    firstNumber = amount.substring(0, amount.indexOf("x"));
                    secondNumber = amount.substring(amount.indexOf("x") + 1, amount.length());
                    result = Float.valueOf(firstNumber) * Float.valueOf(secondNumber);
                    amount = new StringBuilder();
                    amount.append(result);
                    operationDone = true;
                    ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                            .toString());
                    break;
                case 3:
                    firstNumber = amount.substring(0, amount.indexOf("+"));
                    secondNumber = amount.substring(amount.indexOf("+") + 1, amount.length());
                    result = Float.valueOf(firstNumber) + Float.valueOf(secondNumber);
                    amount = new StringBuilder();
                    amount.append(result);
                    operationDone = true;
                    ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                            .toString());
                    break;
                case 4:
                    firstNumber = amount.substring(0, amount.indexOf("-"));
                    secondNumber = amount.substring(amount.indexOf("-") + 1, amount.length());
                    result = Float.valueOf(firstNumber) - Float.valueOf(secondNumber);
                    amount = new StringBuilder();
                    amount.append(result);
                    operationDone = true;
                    ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                            .toString());
                    break;
                default:
                    //-1 no operation to do
                    break;
            }
        }
        else if (discountMode)
        {
            String discount   = "";
            String percentage = "";
            double result;
            int    opType     = searchOperation();
            switch (opType)
            {
                case 5:
                    //€ discount
                    if (!discountToBeDeleted)
                    {
                        if (total != 0)
                        {
                            discount = amount.substring(0, amount.length() - 1);
                            result   = Float.valueOf(discount);
                            if (result > total)
                            {

                                amount.delete(0, amount.length() - 1);
                            }
                            //homage
                            else if (result == total)
                            {
                                saveBillWithHomage((float) result);
                                totalDiscount = 0;
                                ((CustomTextView) view.findViewById(R.id.cash_euro_total)).setText(total + "");
                                if (view.findViewById(R.id.euro_icon_discount)
                                        .getVisibility() == View.VISIBLE)
                                {
                                    setDiscountLayout(false);
                                }
                                discountMode = false;
                                discountEuro = true;
                                hideDiscountDigits();
                            }
                            //discount
                            else
                            {
                                total -= result;
                                totalDiscount += result;
                                if (dbA.checkIfDiscountExists(billId) < 0)
                                {
                                    dbA.addDiscountToTable(totalDiscount, billId);
                                }
                                else
                                {
                                    dbA.updateBillExtra(billId, totalDiscount, totalDiscount);
                                }
                                ((CustomTextView) view.findViewById(R.id.cash_euro_total)).setText((total + "")
                                        .concat("0"));
                                setDiscountLayout(true);
                                discountMode        = false;
                                discountEuro        = true;
                                discountToBeDeleted = false;
                                hideDiscountDigits();
                            }
                        }
                        else
                        {

                            ((CustomTextView) view.findViewById(R.id.cash_euro_total)).setText(total + "");
                            discountMode = false;
                            hideDiscountDigits();
                        }
                    }
                    //delete discount from billId, directly in db
                    else
                    {

                        dbA.deleteDiscuntTotal(billId);
                        discountMode        = false;
                        discountEuro        = true;
                        discountToBeDeleted = false;
                        total += totalDiscount;
                        totalDiscount       = 0.9f;
                        DecimalFormat twoDForm = new DecimalFormat("#.00");
                        viewTotal.setText(twoDForm.format(total).replace(".", ","));
                        if (view.findViewById(R.id.euro_icon_discount)
                                .getVisibility() == View.VISIBLE)
                        {
                            setDiscountLayout(false);
                        }
                        hideDiscountDigits();
                    }
                    break;
                case 6:
                    //% discount
                    if (total != 0)
                    {
                        result = 0;
                        float percDiscount = 0.0f;
                        if (amount.length() <= 4)
                        {
                            percentage   = amount.substring(0, amount.length() - 1);
                            percDiscount = Float.valueOf(percentage) / 100;
                            if (percDiscount > 1)
                            {
                                Toast.makeText(context, R.string.percentage_cant_be_greater_than_100, Toast.LENGTH_SHORT)
                                     .show();
                                amount.delete(0, amount.length() - 1);
                                ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                        .toString());
                                result = 0;
                            }
                            else
                            {
                                result = total * percDiscount;
                            }
                        }
                        else
                        {
                            Toast.makeText(context, R.string.percentage_cant_be_greater_than_100, Toast.LENGTH_SHORT)
                                 .show();
                            amount.delete(0, amount.length() - 1);
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                            result = 0;
                        }
                        if (result > total)
                        {
                            Toast.makeText(context, R.string.discount_is_bigger_than_price, Toast.LENGTH_SHORT)
                                 .show();
                            amount.delete(0, amount.length() - 1);
                            ((CustomTextView) myself.findViewById(R.id.calculator_input_text)).setText(amount
                                    .toString());
                            result = 0;
                        }
                        else if (result == 0)
                        {
                            break;
                        }
                        //homage
                        else if (result == total)
                        {
                            saveBillWithHomage((float) result);
                            ((CustomTextView) view.findViewById(R.id.cash_euro_total)).setText((total + "")
                                    .concat("0"));
                            if (view.findViewById(R.id.euro_icon_discount)
                                    .getVisibility() == View.VISIBLE)
                            {
                                setDiscountLayout(false);
                            }
                            discountMode = false;
                            discountEuro = true;
                            hideDiscountDigits();
                        }
                        else
                        {
                            total -= result;
                            totalDiscount += result;
                            if (dbA.checkIfDiscountExists(billId) != 0.0f)
                            {
                                dbA.addDiscountToTable(totalDiscount, billId);
                            }
                            else
                            {
                                dbA.updateDiscount(totalDiscount, billId);
                            }
                            DecimalFormat twoDForm = new DecimalFormat("#.00");
                            viewTotal.setText(twoDForm.format(total).replace(".", ","));
                            setDiscountLayout(true);
                            discountMode        = false;
                            discountEuro        = true;
                            discountToBeDeleted = false;
                            hideDiscountDigits();
                        }
                    }
                    else
                    {
                        ((CustomTextView) view.findViewById(R.id.cash_euro_total)).setText(total + "");
                        discountMode = false;
                        discountEuro = true;
                        hideDiscountDigits();
                    }
                    break;
                default:
                    break;
            }
        }
    }


    public void hideDiscountDigits()
    {
        view.findViewById(R.id.cash_calculator_pad).setVisibility(View.GONE);
        view.findViewById(R.id.cash_recycler_container).setVisibility(View.VISIBLE);
        view.findViewById(R.id.cash_hline).setVisibility(View.VISIBLE);
        view.findViewById(R.id.layout_8).setActivated(false);
    }


    public void setDiscountLayout(boolean value)
    {

    }


    /**
     * in executeCalculatorAction (above) check which calculator action is performed : + - x /
     * and checks which discount is performed: % €
     *
     * @return
     */
    public int searchOperation()
    {
        if (amount.indexOf("/") != -1)
        {
            return 1;
        }
        else if (amount.indexOf("x") != -1)
        {
            return 2;
        }
        else if (amount.indexOf("+") != -1)
        {
            return 3;
        }
        else if (amount.indexOf("-") != -1)
        {
            return 4;
        }
        else if (amount.indexOf("€") != -1)
        {
            return 5;
        }
        else if (amount.indexOf("%") != -1)
        {
            return 6;
        }
        else
        {
            return -1;
        }
    }


    /**
     * ipdate already saved bill
     *
     * @param billId
     */
    public void updateBill(int billId)
    {
        float discount = dbA.getBillDiscountPrice(billId);
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            //salvo prodotto
            int check = dbA.checkProductBillForPayment(i, billId, listDataHeader.get(i));
            if (check == -11)
            {
                //salvo nuovo prodotto
                int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader.get(i));
                if (listDataCustomer.size() > 0)
                {
                    Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                    .getClientPosition() - 1);
                    dbA.saveCustomerBillForPayment(c, prodId);
                }
                if (listDataChild.get(listDataHeader.get(i)) != null)
                {
                    for (int j = 0; j < listDataChild.get(listDataHeader.get(i)).size(); j++)
                    {
                        //salvo tutti i figli
                        dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader.get(i))
                                                                               .get(j));
                    }
                }
            }
            else
            {
                dbA.showData("product_bill");
                dbA.updateProductBillForPaymentQuantity(listDataHeader.get(i)
                                                                      .getQuantityInt(), check, listDataHeader
                        .get(i)
                        .getProductId());
                if (listDataCustomer.size() > 0)
                {
                    Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                    .getClientPosition() - 1);
                    dbA.saveCustomerBillForPayment(c, check);
                }
                dbA.showData("modifier_bill");
                if (listDataChild.get(listDataHeader.get(i)) != null)
                {
                    for (int j = 0; j < listDataChild.get(listDataHeader.get(i)).size(); j++)
                    {
                        int check1 = dbA.checkModifierBillForPayment(j, check, listDataChild.get(listDataHeader
                                .get(i)).get(j));
                        if (check1 == -11)
                        {
                            //mod non c'è
                            dbA.saveModifierBillForPayment(j, check, listDataChild.get(listDataHeader
                                    .get(i)).get(j));
                        }
                        else
                        {
                            //update
                            if (listDataChild.get(listDataHeader.get(i))
                                             .get(j)
                                             .getModifierId() == -15)
                            {
                                dbA.updateModifierBillNote(listDataChild.get(listDataHeader.get(i))
                                                                        .get(j), check1);
                            }
                            dbA.updateModifierBillForPaymentQuantity(listDataChild.get(listDataHeader
                                    .get(i)).get(j).getQuantityInt(), check1);
                        }
                    }
                }
            }
        }
        dbA.deleteLeftProductFromBill(billId, listDataHeader.size() - 1);

    }


    /**
     * save bill for first time
     */
    public void saveBill(int printed_index)
    {
        if (listDataHeader.size() > 0)
        {
            Intent intent     = getActivity().getIntent();
            int    numberBill = intent.getIntExtra("orderNumber", 1);

           /* if(StaticValue.blackbox){

                RequestParam params = new RequestParam();

                Gson gson = new Gson();
                String products = gson.toJson(listDataHeader);
                String customers = gson.toJson(listDataCustomer);
                Map<String,ArrayList<CashButtonListLayout>> test =
                        new HashMap<String,ArrayList<CashButtonListLayout>>();
                for(int i =0; i<listDataHeader.size(); i++){
                    test.put(String.valueOf(i), listDataChild.get(listDataHeader.get(i)));
                }

                //String modifiers = gson.toJson(listDataChild);
                String modifiers = gson.toJson(test);
                params.add("orderNumber", String.valueOf(numberBill));
                params.add("total", String.valueOf(total));
                params.add("printIndex", String.valueOf(printed_index));
                params.add("products", products);
                params.add("customers", customers);
                params.add("modifiers", modifiers);
                params.add("totalDiscount", String.valueOf(totalDiscount));

                ((Operative) context).callHttpHandler("/saveBill", params);

            }else {*/
            int billId = dbA.saveTotalBillForPayment(total, numberBill, printed_index);
            bill_id                   = billId;
            CashFragment.this.bill_id = billId;
            //salvo i data header e i suoi subelementi
            if (billId != 0)
            {
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    //salvo prodotto
                    int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader.get(i));
                    if (listDataCustomer.size() > 0)
                    {
                        Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                        .getClientPosition() - 1);
                        dbA.saveCustomerBillForPayment(c, prodId);
                    }
                    if (listDataChild.get(listDataHeader.get(i)) != null)
                    {
                        for (int j = 0; j < listDataChild.get(listDataHeader.get(i)).size(); j++)
                        {
                            //salvo tutti i figli
                            dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader
                                    .get(i)).get(j));

                        }
                    }
                }
                addDiscountToBill();

                if (printed_index >= 0)
                {
                    printOrderBill(billId);
                }

                listDataHeader   = new ArrayList<CashButtonLayout>();
                listDataChild    = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                listDataCustomer = new ArrayList<>();
                listAdapter      = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);
                expListView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
                activityCommunicator.deleteCurrentCash();

                setModifyBar(false);
            }
            //  }


        }
    }


    public void saveBillAndPrint(int printed_index)
    {
        if (listDataHeader.size() > 0)
        {
            //salvo i data header e i suoi subelementi
            Intent intent     = getActivity().getIntent();
            int    numberBill = intent.getIntExtra("orderNumber", 1);
            this.billId               = dbA.saveTotalBillForPayment(total, numberBill, printed_index);
            bill_id                   = billId;
            CashFragment.this.bill_id = billId;
            if (billId != 0)
            {
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    //salvo prodotto
                    int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader.get(i));
                    if (listDataCustomer.size() > 0)
                    {
                        Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                        .getClientPosition() - 1);
                        dbA.saveCustomerBillForPayment(c, prodId);
                    }
                    if (listDataChild.get(listDataHeader.get(i)) != null)
                    {
                        for (int j = 0; j < listDataChild.get(listDataHeader.get(i)).size(); j++)
                        {
                            //salvo tutti i figli
                            dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader
                                    .get(i)).get(j));

                        }
                    }
                }
                printOrderBill(billId);
            }
        }
    }


    public void saveBillForTable()
    {
        if (listDataHeader.size() > 0)
        {
            //salvo i data header e i suoi subelementi
            saveBill(0);
        }
        else
        {
            if (listDataCustomer.size() > 0)
            {
                saveBill(0);
            }
            Intent intent     = getActivity().getIntent();
            int    numberBill = intent.getIntExtra("orderNumber", 1);

            int billId = dbA.saveTotalBillForPayment(total, numberBill, 0);
            bill_id                   = billId;
            CashFragment.this.bill_id = billId;
            CashFragment.this.billId  = billId;
        }
    }


    /**
     * set specific group to modify using its position
     *
     * @param groupPosition
     */
    public void setOnGroupClick(Integer groupPosition)
    {
        if (!dbA.checkIfBillSplitPaid(billId))
        {
            if (listDataHeader.get(groupPosition).getIsDelete())
            {
                listDataHeader.get(groupPosition).setIsDelete(false);
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    expListView.expandGroup(i);
                }
            }
            else
            {
                listDataHeader.get(groupPosition).setIsDelete(true);
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    if (i != groupPosition)
                    {
                        expListView.expandGroup(i);
                        listDataHeader.get(i).setIsDelete(false);
                    }
                    else
                    {
                        expListView.collapseGroup(i);
                    }
                }
            }
            setModifyBar(false);
            listAdapter.notifyDataSetChanged();
            expListView.setSelection(groupPosition);
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                listDataHeader.get(i).setModifyModifier(false);
            }
            listAdapter.notifyDataSetChanged();
            activityCommunicator.goToMainPage();
        }

    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        this.context         = getActivity();
        activityCommunicator = (ActivityCommunicator) this.context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            activityAssignedValue = savedInstanceState.getString(STRING_VALUE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(STRING_VALUE, activityAssignedValue);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume()
    {
        super.onResume();
    }


    /**
     * open an close modify button at the end of cash list depending on boolean b
     * (hope you get boolean b= true | false.....)
     *
     * @param b
     */
    public void setModifyBar(Boolean b)
    {
        View           modifyButton  = view.findViewById(R.id.modify_end_button);
        ImageView      totalBarLabel = (ImageView) view.findViewById(R.id.cash_euro_label);
        CustomTextView totalBar      = (CustomTextView) view.findViewById(R.id.cash_euro_total);
        if (b)
        {
            modifyButton.setVisibility(View.VISIBLE);
            totalBarLabel.setVisibility(View.GONE);
            totalBar.setVisibility(View.GONE);
        }
        else
        {
            modifyButton.setVisibility(View.GONE);
            totalBarLabel.setVisibility(View.VISIBLE);
            totalBar.setVisibility(View.VISIBLE);
        }
    }


    /**
     * used to recaulcate total to show when modifyitem
     */
    public void reCalculateTotal()
    {
        float cash = 0.0f;
        for (CashButtonLayout product : listDataHeader)
        {
            ArrayList<CashButtonListLayout> modifiersList = listDataChild.get(product);
            if (modifiersList != null)
            {
                for (CashButtonListLayout mody : modifiersList)
                {
                    cash += mody.getPriceFloat() * mody.getQuantityInt();
                }
            }
            cash += product.getPriceFloat() * product.getQuantityInt();
        }
        total = Math.round(cash * 10000) / 10000.00;
        DecimalFormat twoDForm = new DecimalFormat("#.00");
        viewTotal.setText(twoDForm.format(total).replace(".", ","));
    }


    /**
     * set total price label to zero
     */
    public void setTotalToZero()
    {
        total = 0.00;
        viewTotal.setText("0,00");
    }


    /**
     * update total price label
     *
     * @param price
     */
    public void updateTotal(Float price)
    {
        total = total + price;
        total = Math.round(total * 10000) / 10000.00;
        DecimalFormat twoDForm = new DecimalFormat("#.00");
        viewTotal.setText(twoDForm.format(total).replace(".", ","));
    }


    /**
     * substrac from total label cost of deleted item
     *
     * @param button
     */
    public void subPrice(CashButtonLayout button)
    {
        total -= button.getPriceFloat();
        List<CashButtonListLayout> dataChild       = listDataChild.get(button);
        int                        productQuantity = button.getQuantityInt();
        if (dataChild != null)
        {
            for (int i = 0; i < dataChild.size(); i++)
            {
                Integer qty         = dataChild.get(i).getQuantityInt();
                int     toSubstract = qty / productQuantity;
                total -= dataChild.get(i).getPriceFloat() * (toSubstract);
            }
        }
        total = Math.round(total * 10000) / 10000.00;
        if (total <= 0)
        {
            total = 0.00;
        }
        DecimalFormat twoDForm = new DecimalFormat("#0.00");
        viewTotal.setText(twoDForm.format(total).replace(".", ","));
    }

/*
    public void subPriceModifier(CashButtonListLayout button) {
        total -= button.getPriceFloat();
        total = Math.round(total*10000)/10000.00;
        DecimalFormat twoDForm = new DecimalFormat("#.00");
        viewTotal.setText(twoDForm.format(total).replace(".", ","));
        //viewTotal.setText(twoDForm.format(total));
        //viewTotal.setText(total.toString());
    }
*/


    public void subPriceAllProduct(CashButtonLayout button)
    {
        total -= button.getPriceFloat() * button.getQuantityInt();
        List<CashButtonListLayout> dataChild       = listDataChild.get(button);
        int                        productQuantity = button.getQuantityInt();
        if (dataChild != null)
        {
            for (int i = 0; i < dataChild.size(); i++)
            {
                Integer qty         = dataChild.get(i).getQuantityInt();
                int     toSubstract = qty / productQuantity;
                total -= dataChild.get(i).getPriceFloat() * dataChild.get(i).getQuantityInt();
            }
        }
        total = Math.round(total * 10000) / 10000.00;
        if (total <= 0)
        {
            total = 0.00;
        }
        DecimalFormat twoDForm = new DecimalFormat("#0.00");
        viewTotal.setText(twoDForm.format(total).replace(".", ","));
    }


    public void subPriceModifierWithQty(CashButtonListLayout button, int toSubstract)
    {
        total -= button.getPriceFloat() * toSubstract;
        total = Math.round(total * 10000) / 10000.00;
        DecimalFormat twoDForm = new DecimalFormat("#0.00");
        viewTotal.setText(twoDForm.format(total).replace(".", ","));
    }


    /**
     * used outside cashFragment
     *
     * @return
     */
    public ArrayList<CashButtonListLayout> getLastList()
    {
        if (modifyChangePosition == -1)
        {
            ArrayList<CashButtonListLayout> listOfValues = listDataChild.get(listDataHeader.get(listDataHeader
                    .size() - 1));
            return listOfValues;
        }
        else
        {
            ArrayList<CashButtonListLayout> listOfValues = listDataChild.get(listDataHeader.get(modifyChangePosition));
            return listOfValues;

        }
    }


    /**
     * add one product TODO use only buttonLayout
     *
     * @param buttonLayout
     * @param text
     * @param price
     * @param quantity
     * @param customerPosition
     */
    public void addProduct(ButtonLayout buttonLayout, String text, Float price, Integer quantity, Integer customerPosition)
    {
        setModifyProduct(false);
        deleteProduct = false;
        activityCommunicator.endModifyProduct();
        if (listDataHeader.size() > 0)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> modifiersList = dbA.checkToOpenModifiersGroup(buttonLayout
                    .getID());
            if (listDataHeader.get(listDataHeader.size() - 1)
                              .getProductId() == buttonLayout.getID() && modifiersList.size() == 0)
            {
                if (listDataChild.get(listDataHeader.get(listDataHeader.size() - 1)) != null)
                {
                    if (listDataChild.get(listDataHeader.get(listDataHeader.size() - 1))
                                     .size() == 0)
                    {
                        //update quantity
                        listDataHeader.get(listDataHeader.size() - 1)
                                      .setQuantity(listDataHeader.get(listDataHeader.size() - 1)
                                                                 .getQuantityInt() + quantity);

                        if (billId > 0)
                        {
                            ArrayList<CashButtonListLayout> newModifier = new ArrayList<CashButtonListLayout>();
                            Customer                        customer    = new Customer();
                            CashButtonLayout                newbutton   = new CashButtonLayout();
                            newbutton.setTitle(text);
                            newbutton.setPrice(price);
                            newbutton.setQuantity(1);
                            newbutton.setProductId(buttonLayout.getID());
                            newbutton.setIsDelete(false);
                            newbutton.setModifyModifier(false);
                            newbutton.setPosition(listDataHeader.size());
                            //newbutton.setID(buttonLayout.getID());
                            newbutton.setID(-1);
                            newbutton.setClientPosition(customerPosition);
                            newbutton.setVat(buttonLayout.getVat());
                            newbutton.setPrinterId(buttonLayout.getPrinterId());
                            printSingleOrderBill(billId, newbutton, newModifier, customer);
                        }
                    }
                    else
                    {
                        //add new one
                        button = new CashButtonLayout();
                        button.setTitle(text);
                        button.setPrice(price);
                        button.setQuantity(quantity);
                        button.setProductId(buttonLayout.getID());
                        button.setIsDelete(false);
                        button.setModifyModifier(false);
                        button.setPosition(listDataHeader.size());
                        //button.setID(buttonLayout.getID());
                        button.setID(-1);
                        button.setClientPosition(customerPosition);
                        button.setVat(buttonLayout.getVat());
                        button.setPrinterId(buttonLayout.getPrinterId());
                        //add button to listDataHeader (groups list)
                        listDataHeader.add(button);
                    }
                }
                else
                {
                    //update quantity
                    listDataHeader.get(listDataHeader.size() - 1)
                                  .setQuantity(listDataHeader.get(listDataHeader.size() - 1)
                                                             .getQuantityInt() + quantity);
                    if (billId > 0)
                    {
                        ArrayList<CashButtonListLayout> newModifier = new ArrayList<CashButtonListLayout>();
                        Customer                        customer    = new Customer();
                        CashButtonLayout                newbutton   = new CashButtonLayout();
                        newbutton.setTitle(text);
                        newbutton.setPrice(price);
                        newbutton.setQuantity(1);
                        newbutton.setProductId(buttonLayout.getID());
                        newbutton.setIsDelete(false);
                        newbutton.setModifyModifier(false);
                        newbutton.setPosition(listDataHeader.size());
                        //newbutton.setID(buttonLayout.getID());
                        newbutton.setID(-1);

                        newbutton.setClientPosition(customerPosition);
                        newbutton.setVat(buttonLayout.getVat());
                        newbutton.setPrinterId(buttonLayout.getPrinterId());
                        //printSingleOrderBill(billId, newbutton, newModifier, customer);
                    }
                }
            }
            else
            {
                //create a new object CashButtonLayout and set its values
                button = new CashButtonLayout();
                button.setTitle(text);
                button.setPrice(price);
                button.setQuantity(quantity);
                button.setProductId(buttonLayout.getID());
                button.setIsDelete(false);
                button.setModifyModifier(false);
                //button.setID(buttonLayout.getID());
                button.setID(-1);
                button.setPosition(listDataHeader.size());
                button.setClientPosition(customerPosition);
                button.setVat(buttonLayout.getVat());
                button.setPrinterId(buttonLayout.getPrinterId());
                //add button to listDataHeader (groups list)
                listDataHeader.add(button);
            }
        }
        else
        {
            if (listDataCustomer.size() == 0)
            {
                //add one button
                button = new CashButtonLayout();
                button.setTitle(text);
                button.setPrice(price);
                button.setQuantity(quantity);
                button.setProductId(buttonLayout.getID());
                button.setIsDelete(false);
                button.setModifyModifier(false);
                //button.setID(buttonLayout.getID());
                button.setID(-1);
                button.setPosition(listDataHeader.size());
                button.setClientPosition(0);
                button.setVat(buttonLayout.getVat());
                button.setPrinterId(buttonLayout.getPrinterId());
                //add button to listDataHeader (groups list)

                listAdapter.setFirstClientFalse();
                listDataHeader.add(button);
            }
            else
            {
                //add button on last customer
                for (CashButtonLayout b : listDataHeader)
                {
                    b.setClientPosition(1);
                }
                Customer lastCustomer = new Customer();
                lastCustomer.setPosition(listDataCustomer.size() + 1);
                lastCustomer.setDescription("Cliente " + listDataCustomer.size() + 1);
                listDataCustomer.add(lastCustomer);
            }
        }
    }


    /**
     * add fixed modifier after a product is added, if there are fixed modifiers for that product
     *
     * @param buttonId
     * @param listPosition
     */
    public void addFixedModifiers(int buttonId, int listPosition)
    {
        ArrayList<Integer> groupsInt    = dbA.fetchAssignedGroupModifiersByQuery(buttonId);
        ArrayList<Integer> modifiersInt = dbA.fetchAssignedModifiersByQuery(buttonId);
        /*if(!StaticValue.blackbox) {
            if (groupsInt != null) {
                for (int i = 0; i < groupsInt.size(); i++) {
                    ArrayList<OModifierAdapter.OModifier> mods = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE groupID=" + groupsInt.get(i));
                    for (OModifierAdapter.OModifier m : mods) {
                        CashButtonLayout button1 = listDataHeader.get(listPosition);
                        CashButtonListLayout list = new CashButtonListLayout();
                        list.setTitle(m.getTitle());
                        list.setPrice(m.getPrice());
                        list.setQuantity(1 * listDataHeader.get((listPosition)).getQuantityInt());
                        list.setModifierId(m.getID());
                        //list.setID(m.getID());
                        list.setID(-1);
                        button1.setCashList(list);
                        listDataChild.put(listDataHeader.get((listPosition)), button1.getCashList());
                        updateTotal(m.getPrice() * (1 * listDataHeader.get((listPosition)).getQuantityInt()));
                    }
                }
            }
        }*/
        if (modifiersInt != null)
        {
            for (int i = 0; i < modifiersInt.size(); i++)
            {
                ArrayList<OModifierAdapter.OModifier> mods = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE id=" + modifiersInt
                        .get(i));
                for (OModifierAdapter.OModifier m : mods)
                {
                    CashButtonLayout     button1 = listDataHeader.get(listPosition);
                    CashButtonListLayout list    = new CashButtonListLayout();
                    list.setTitle(m.getTitle());
                    list.setPrice(m.getPrice());
                    list.setQuantity(1 * listDataHeader.get((listPosition)).getQuantityInt());
                    list.setModifierId(m.getID());
                    //list.setID(m.getID());
                    list.setID(-1);
                    button1.setCashList(list);
                    listDataChild.put(listDataHeader.get((listPosition)), button1.getCashList());
                    updateTotal(m.getPrice() * (1 * listDataHeader.get((listPosition))
                                                                  .getQuantityInt()));
                }
            }
        }

    }


    /**
     * TODO remove text, price and quantity params and just use button layout
     * add product to cash list
     *
     * @param buttonLayout
     * @param text
     * @param price
     * @param quantity
     */
    public void updateText(ButtonLayout buttonLayout, String text, Float price, Integer quantity)
    {
        //update price
        if (listDataCustomer.size() == 0)
        {
            addProduct(buttonLayout, text, price, quantity, 0);
            addFixedModifiers(buttonLayout.getID(), listDataHeader.size() - 1);
            updateTotal(price * quantity);
            listAdapter.notifyDataSetChanged();
            resetCashNotChanghed();
            expListView.setSelectedGroup(listDataHeader.size() - 1);
            dbA.showData("product_bill");
        }
        else
        {
            ArrayList<CashButtonLayout>                                listDataHeaderTemp = new ArrayList<CashButtonLayout>();
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> listDataChildTemp  = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            for (int j = 0; j < currentCustomerArray.size(); j++)
            {
                if (listDataCustomer.size() == currentCustomerArray.get(j))
                {
                    //add last to list
                    if (listDataHeader.get(listDataHeader.size() - 1).getProductId() == -20)
                    {
                        listDataHeader.get(listDataHeader.size() - 1).setTitle(text);
                        listDataHeader.get(listDataHeader.size() - 1).setPrice(price);
                        listDataHeader.get(listDataHeader.size() - 1).setQuantity(quantity);
                        listDataHeader.get(listDataHeader.size() - 1)
                                      .setProductId(buttonLayout.getID());
                        listDataHeader.get(listDataHeader.size() - 1).setIsDelete(false);
                        listDataHeader.get(listDataHeader.size() - 1).setModifyModifier(false);
                        listDataHeader.get(listDataHeader.size() - 1).setID(-1);
                        listDataHeader.get(listDataHeader.size() - 1)
                                      .setPosition(listDataHeader.size());
                        listDataHeader.get(listDataHeader.size() - 1)
                                      .setClientPosition(currentCustomerArray.get(j));
                        addFixedModifiers(buttonLayout.getID(), listDataHeader.size() - 1);
                        updateTotal(price * quantity);
                        listAdapter.notifyDataSetChanged();
                        resetCashNotChanghed();
                        expListView.setSelectedGroup(listDataHeader.size() - 1);
                        listDataHeaderTemp.add(listDataHeader.get(listDataHeader.size() - 1));
                        listDataChildTemp.put(listDataHeaderTemp.get(listDataHeaderTemp.size() - 1), listDataChild
                                .get(listDataHeader.get(listDataHeader.size() - 1)));

                    }
                    else
                    {
                        addProduct(buttonLayout, text, price, quantity, currentCustomerArray.get(j));
                        addFixedModifiers(buttonLayout.getID(), listDataHeader.size() - 1);
                        updateTotal(price);
                        listAdapter.notifyDataSetChanged();
                        resetCashNotChanghed();
                        expListView.setSelectedGroup(listDataHeader.size() - 1);
                        int lastP = returnLastPositionForCustomer(currentCustomerArray.get(j));
                        listDataHeaderTemp.add(listDataHeader.get(lastP));
                        listDataChildTemp.put(listDataHeaderTemp.get(listDataHeaderTemp.size() - 1), listDataChild
                                .get(listDataHeader.get(lastP)));

                    }
                }
                else
                {
                    int positionToAdd = -1;
                    for (int i = 0; i < listDataHeader.size(); i++)
                    {
                        if (listDataHeader.get(i)
                                          .getClientPosition() == currentCustomerArray.get(j))
                        {
                            positionToAdd = i;
                        }
                    }

                    if (positionToAdd != -1)
                    {
                        if (listDataHeader.get(positionToAdd).getProductId() == -20)
                        {
                            //aggiungo ad un cliente vuoto
                            listDataHeader.get(positionToAdd).setTitle(text);
                            listDataHeader.get(positionToAdd).setPrice(price);
                            listDataHeader.get(positionToAdd).setQuantity(quantity);
                            listDataHeader.get(positionToAdd).setProductId(buttonLayout.getID());
                            listDataHeader.get(positionToAdd).setIsDelete(false);
                            listDataHeader.get(positionToAdd).setModifyModifier(false);
                            listDataHeader.get(positionToAdd).setID(-1);
                            listDataHeader.get(positionToAdd).setPosition(positionToAdd);
                            listDataHeader.get(positionToAdd)
                                          .setClientPosition(currentCustomerArray.get(j));

                            int lastP = returnLastPositionForCustomer(currentCustomerArray.get(j));
                            listDataHeaderTemp.add(listDataHeader.get(lastP));

                        }
                        else
                        {
                            //aggiungo in fondo
                            button = new CashButtonLayout();
                            button.setTitle(text);
                            button.setPrice(price);
                            button.setQuantity(quantity);
                            button.setProductId(buttonLayout.getID());
                            button.setIsDelete(false);
                            button.setModifyModifier(false);
                            button.setID(-1);
                            button.setPosition(listDataHeader.size());
                            button.setClientPosition(currentCustomerArray.get(j));
                            button.setVat(buttonLayout.getVat());
                            button.setPrinterId(buttonLayout.getPrinterId());

                            listDataHeader.add(positionToAdd + 1, button);
                            int lastP = returnLastPositionForCustomer(currentCustomerArray.get(j));
                            listDataHeaderTemp.add(listDataHeader.get(lastP));


                            positionToAdd += 1;
                        }
                        for (int i = 0; i < listDataHeader.size(); i++)
                        {
                            listDataHeader.get(i).setPosition(i);
                        }

                        addFixedModifiers(buttonLayout.getID(), positionToAdd);
                        int lastP = returnLastPositionForCustomer(currentCustomerArray.get(j));
                        listDataChildTemp.put(listDataHeaderTemp.get(listDataHeaderTemp.size() - 1), listDataChild
                                .get(listDataHeader.get(lastP)));

                        updateTotal(price);
                        listAdapter.notifyDataSetChanged();
                        resetCashNotChanghed();
                        expListView.setSelectedGroup(positionToAdd - 1);

                    }

                }
            }
            cashListIndex = dbA.getTotalBillPrintedIndex(billId);
           /* if(billId>0 && cashListIndex!=0){
                ArrayList<CashButtonLayout> newProducts = new ArrayList<CashButtonLayout>();
                HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                //position is neded because in server method the modifiers are remapped on product using map key position
                //so I set the new position using an int and incrementing it (maybe this could be done better?)
                int position = 0;
                for (int i = 0; i < listDataHeaderTemp.size(); i++) {
                        CashButtonLayout newButton = listDataHeaderTemp.get(i);
                        newButton.setPosition(position);
                        position++;
                        newProducts.add(newButton);
                        newModifiers.put(newButton, listDataChildTemp.get(listDataHeaderTemp.get(i)));

                }



                Gson gson = new Gson();
                JSONObject combined = new JSONObject();

                Intent intent = getActivity().getIntent();
                int orderNumber = intent.getIntExtra("orderNumber", -1);
                try {
                    combined.put("products", gson.toJson(newProducts));
                    Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization()
                            .setPrettyPrinting().create();
                    combined.put("modifiers", gson1.toJson(newModifiers));
                    //print type ==8 is order print only
                    combined.put("printType", 8);
                    combined.put("IP", IP);
                    combined.put("deviceName", deviceName);
                    combined.put("orderNumber", orderNumber);
                    combined.put("billId", billId);
                    combined.put("indexList", cashListIndex);
                    combined.put("customerList", gson.toJson(listDataCustomer));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cashListIndex = listDataHeader.size();
                dbA.updateBillTotalPrintedIndex(billId, listDataHeader.size());
                gson.toJson(combined);

                ClientThread myThread = ClientThread.getInstance();
                myThread.delegate = forClient;
                //myThread.delegate = forClient;
                myThread.setClientThread();
                myThread.addJsonString(combined.toString());
            }*/

        }
        ArrayList<Customer> a = listDataCustomer;
    }


    /**
     * TODO same as above
     * modify cash list product to a specific position
     *
     * @param text
     * @param price
     * @param quantity
     * @param groupPosition
     */
    public void updateSpecificText(String text, Float price, Integer quantity, Integer groupPosition, Integer prodId)
    {
        oldModifiedProduct.setTitle(listDataHeader.get(groupPosition).getTitle());
        oldModifiedProduct.setPrice(listDataHeader.get(groupPosition).getPriceFloat());
        oldModifiedProduct.setQuantity(listDataHeader.get(groupPosition).getQuantityInt());
        oldModifiedProduct.setIsDelete(false);
        oldModifiedProduct.setProductId(listDataHeader.get(groupPosition).getProductId());
        oldModifiedProduct.setNewCashList(listDataChild.get(listDataHeader.get(groupPosition)));
        oldModifiedProduct.setPrinterId(listDataHeader.get(groupPosition).getPrinterId());
        oldModifiedProduct.setClientPosition(listDataHeader.get(groupPosition).getClientPosition());
        oldListDataChild.put(oldModifiedProduct, oldModifiedProduct.getCashList());

        subPrice(listDataHeader.get(groupPosition));
        updateTotal(price);

        listDataHeader.get(groupPosition).setTitle(text);
        listDataHeader.get(groupPosition).setPrice(price);
        listDataHeader.get(groupPosition).setQuantity(quantity);
        listDataHeader.get(groupPosition).setIsDelete(true);
        listDataHeader.get(groupPosition).setProductId(prodId);
        listDataHeader.get(groupPosition).resetCashList();

        CashButtonLayout b = oldModifiedProduct;
        listDataChild.remove(listDataHeader.get(groupPosition));
        ArrayList<Integer> groupsInt    = dbA.fetchAssignedGroupModifiersByQuery(prodId);
        ArrayList<Integer> modifiersInt = dbA.fetchAssignedModifiersByQuery(prodId);
        if (groupsInt != null)
        {
            for (int i = 0; i < groupsInt.size(); i++)
            {
                ArrayList<OModifierAdapter.OModifier> mods = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE groupID=" + groupsInt
                        .get(i));
                for (OModifierAdapter.OModifier m : mods)
                {
                    CashButtonLayout     button1 = listDataHeader.get(groupPosition);
                    CashButtonListLayout list    = new CashButtonListLayout();
                    list.setTitle(m.getTitle());
                    list.setPrice(m.getPrice());
                    list.setQuantity(1 * listDataHeader.get(groupPosition).getQuantityInt());
                    list.setModifierId(m.getID());
                    list.setID(m.getID());
                    button1.setCashList(list);
                    listDataChild.put(listDataHeader.get(groupPosition), button1.getCashList());

                    updateTotal(m.getPrice() * (1 * listDataHeader.get(groupPosition)
                                                                  .getQuantityInt()));
                }
            }
        }
        if (modifiersInt != null)
        {
            for (int i = 0; i < modifiersInt.size(); i++)
            {
                ArrayList<OModifierAdapter.OModifier> mods = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE id=" + modifiersInt
                        .get(i));
                for (OModifierAdapter.OModifier m : mods)
                {
                    CashButtonLayout     button1 = listDataHeader.get(groupPosition);
                    CashButtonListLayout list    = new CashButtonListLayout();
                    list.setTitle(m.getTitle());
                    list.setPrice(m.getPrice());
                    list.setQuantity(1 * listDataHeader.get(groupPosition).getQuantityInt());
                    list.setModifierId(m.getID());
                    list.setID(m.getID());
                    button1.setCashList(list);
                    listDataChild.put(listDataHeader.get(groupPosition), button1.getCashList());
                    updateTotal(m.getPrice() * (1 * listDataHeader.get(groupPosition)
                                                                  .getQuantityInt()));
                }
            }
        }
        modifyChangePosition = groupPosition;
        expListView.setAdapter(listAdapter);
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            if (groupPosition != i)
            {
                expListView.expandGroup(i);
            }
        }
        expListView.setSelection(groupPosition);
        listAdapter.notifyDataSetChanged();
        resetCashNotChanghed();
    }


    /**
     * return the position of the last product item for one customer
     *
     * @param customerPosition
     * @return
     */
    public int returnLastPositionForCustomer(int customerPosition)
    {
        int returnPosition = 0;
        for (int j = 0; j < listDataHeader.size(); j++)
        {
            if (listDataHeader.get(j).getClientPosition() == customerPosition)
            {
                if (j >= returnPosition)
                {
                    returnPosition = j;
                }
            }
        }
        return returnPosition;
    }


    /**
     * same as product but with modifier
     *
     * @param modifier
     * @param quantity
     */
    public void updateModifierText(OModifierAdapter.OModifier modifier, Integer quantity)
    {
        //update price
        //updateTotal(modifier.getPrice());
        CashButtonLayout button1 = listDataHeader.get(listDataHeader.size() - 1);
        button1.setModifyModifier(false);
        Integer position = 0;
        Boolean check    = false;
        //check modifiers list to see if that modifier is already present
        if (listDataCustomer.size() > 0)
        {
            if (currentCustomerArray.size() > 0)
            {
                for (int i = 0; i < currentCustomerArray.size(); i++)
                {
                    int j = returnLastPositionForCustomer(currentCustomerArray.get(i));
                    if (listDataHeader.get(j)
                                      .getClientPosition() == currentCustomerArray.get(i) && listDataCustomer
                            .get(currentCustomerArray.get(i) - 1)
                            .getActive())
                    {
                        CashButtonLayout button2 = listDataHeader.get(j);

                        List<CashButtonListLayout> listOfValues = listDataChild.get(listDataHeader.get(j));
                        if (listOfValues != null)
                        {
                            for (int k = 0; k < listOfValues.size(); k++)
                            {
                                if (listOfValues.get(k).getModifierId() == modifier.getID())
                                {
                                    check    = true;
                                    position = k;
                                }
                            }
                        }

                        //if modifier is already present just update its quantioty and price
                        if (check)
                        {
                            Integer oldQuantity = listDataChild.get(listDataHeader.get(j))
                                                               .get(position)
                                                               .getQuantityInt();
                            //Float oldPrice = listDataChild.get(listDataHeader.get(listDataHeader.size() - 1)).get(position).getPriceFloat();
                            listDataChild.get(listDataHeader.get(j))
                                         .get(position)
                                         .setQuantity(oldQuantity + listDataHeader.get((j))
                                                                                  .getQuantityInt());

                            updateTotal(modifier.getPrice() * listDataHeader.get((j))
                                                                            .getQuantityInt());

                        }
                        else
                        {
                            //if its not present add new modifier
                            updateTotal(modifier.getPrice() * (quantity * listDataHeader.get((j))
                                                                                        .getQuantityInt()));
                            CashButtonListLayout list = new CashButtonListLayout();
                            list.setTitle(modifier.getTitle());
                            list.setPrice(modifier.getPrice());
                            list.setQuantity(quantity * listDataHeader.get((j)).getQuantityInt());
                            list.setModifierId(modifier.getID());
                            list.setID(modifier.getID());
                            if (listOfValues != null)
                            {
                                button2.setNewCashList((ArrayList<CashButtonListLayout>) listOfValues);
                            }
                            button2.setCashList(list);
                            listDataChild.put(listDataHeader.get((j)), button2.getCashList());
                            //((Operative) getContext()).addToModifierCashList(list);
                        }
                        //expand last product children list(is where we are)
                        expListView.expandGroup(j);

                        listAdapter.notifyDataSetChanged();

                        resetCashNotChanghed();
                        //scroll down to last children
                        expListView.setSelection(listDataHeader.size() - 1);
                        expListView.setSelectedGroup(j);
                    }
                }
            }
        }
        else
        {

            List<CashButtonListLayout> listOfValues = listDataChild.get(listDataHeader.get(listDataHeader
                    .size() - 1));
            if (listOfValues != null)
            {
                for (int i = 0; i < listOfValues.size(); i++)
                {
                    if (listOfValues.get(i).getModifierId() == modifier.getID())
                    {
                        check    = true;
                        position = i;
                    }
                }
            }

            //if modifier is already present just update its quantioty and price
            if (check)
            {
                Integer oldQuantity = listDataChild.get(listDataHeader.get(listDataHeader.size() - 1))
                                                   .get(position)
                                                   .getQuantityInt();
                listDataChild.get(listDataHeader.get(listDataHeader.size() - 1))
                             .get(position)
                             .setQuantity(oldQuantity + listDataHeader.get((listDataHeader.size() - 1))
                                                                      .getQuantityInt());
                updateTotal(modifier.getPrice() * listDataHeader.get((listDataHeader.size() - 1))
                                                                .getQuantityInt());

            }
            else
            {
                //if its not present add new modifier
                updateTotal(modifier.getPrice() * (quantity * listDataHeader.get((listDataHeader.size() - 1))
                                                                            .getQuantityInt()));
                CashButtonListLayout list = new CashButtonListLayout();
                list.setTitle(modifier.getTitle());
                list.setPrice(modifier.getPrice());
                list.setQuantity(quantity * listDataHeader.get((listDataHeader.size() - 1))
                                                          .getQuantityInt());
                list.setModifierId(modifier.getID());
                //list.setID(modifier.getID());
                list.setID(-1);
                if (listOfValues != null)
                {
                    button1.setNewCashList((ArrayList<CashButtonListLayout>) listOfValues);
                }
                button1.setCashList(list);
                listDataChild.put(listDataHeader.get((listDataHeader.size() - 1)), button1.getCashList());
            }
            expListView.expandGroup(listDataHeader.size() - 1);
            listAdapter.notifyDataSetChanged();
            resetCashNotChanghed();
            //scroll down to last children
            expListView.setSelection(listDataHeader.size() - 1);
            expListView.setSelectedGroup(listDataHeader.size() - 1);
        }
    }


    /**
     * same as modify product but for modifier
     *
     * @param modifier
     * @param quantity
     * @param groupPosition
     */
    public void updateSpecificModifierText(OModifierAdapter.OModifier modifier, Integer quantity, Integer groupPosition)
    {
        updateTotal(modifier.getPrice() * listDataHeader.get(groupPosition).getQuantityInt());
        CashButtonLayout                button1      = listDataHeader.get(groupPosition);
        ArrayList<CashButtonListLayout> prova1       = listDataChild.get(listDataHeader.get(groupPosition));
        Integer                         position     = 0;
        Boolean                         check        = false;
        List<CashButtonListLayout>      listOfValues = listDataChild.get(listDataHeader.get(groupPosition));
        if (listOfValues != null)
        {
            for (int i = 0; i < listOfValues.size(); i++)
            {
                if (listOfValues.get(i).getModifierId() == modifier.getID())
                {
                    check    = true;
                    position = i;
                }
            }
        }

        //check if modifier is already present
        if (check)
        {
            //true, increment mquantity
            Integer oldQuantity = listDataChild.get(listDataHeader.get(groupPosition))
                                               .get(position)
                                               .getQuantityInt();
            listDataChild.get(listDataHeader.get(groupPosition))
                         .get(position)
                         .setQuantity(oldQuantity + (1 * listDataHeader.get(groupPosition)
                                                                       .getQuantityInt()));
        }
        else
        {
            //add modifier
            CashButtonListLayout list = new CashButtonListLayout();
            list.setTitle(modifier.getTitle());
            list.setPrice(modifier.getPrice());
            list.setQuantity(quantity * listDataHeader.get(groupPosition).getQuantityInt());
            list.setModifierId(modifier.getID());
            if (listDataChild.get(listDataHeader.get(groupPosition)) != null)
            {
                listDataChild.get(listDataHeader.get(groupPosition)).add(list);
            }
            else
            {
                button1.setCashList(list);
                listDataChild.put(listDataHeader.get(groupPosition), button1.getCashList());
            }
            dbA.addNewModifierFromBill(billId, listDataChild.get(listDataHeader.get(groupPosition))
                                                            .size() - 1, listDataHeader.get(groupPosition)
                                                                                       .getProductId(), groupPosition, list);
        }
        listAdapter.notifyDataSetChanged();
        ArrayList<CashButtonListLayout> prova = listDataChild.get(listDataHeader.get(groupPosition));
        //expand group
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            if (groupPosition != i)
            {
                listDataHeader.get(i).setIsDelete(false);
            }
            expListView.expandGroup(i);
        }
        expListView.setSelection(groupPosition);
        expListView.setSelectedGroup(groupPosition);
    }


    /**
     * remove a specific modifier
     *
     * @param modifier
     * @param quantity
     * @param groupPosition
     * @param cashListPosition
     */
    public void removeSpecificModifierText(OModifierAdapter.OModifier modifier, Integer quantity, Integer groupPosition, Integer cashListPosition)
    {
        if (listDataChild.get(listDataHeader.get(groupPosition)) != null)
        {
            for (int i = 0; i < listDataChild.get(listDataHeader.get(groupPosition)).size(); i++)
            {
                if (modifier.getID() == listDataChild.get(listDataHeader.get(groupPosition))
                                                     .get(i)
                                                     .getModifierId())
                {
                    Integer productQuantity = listDataHeader.get(groupPosition).getQuantityInt();
                    Integer qty = listDataChild.get(listDataHeader.get(groupPosition))
                                               .get(i)
                                               .getQuantityInt();
                    int toSubstract = productQuantity;
                    subPriceModifierWithQty(listDataChild.get(listDataHeader.get(groupPosition))
                                                         .get(i), toSubstract);
                    if ((qty - toSubstract) > 0)
                    {
                        dbA.updateModifierFromBill(billId, i, listDataHeader.get(groupPosition)
                                                                            .getProductId(), groupPosition, toSubstract);
                        listDataChild.get(listDataHeader.get(groupPosition))
                                     .get(i)
                                     .setQuantity(qty - toSubstract);
                    }
                    else
                    {
                        dbA.deleteModifierFromBill(billId, i, listDataHeader.get(groupPosition)
                                                                            .getProductId(), groupPosition);
                        listDataChild.get(listDataHeader.get(groupPosition)).remove(i);
                        activityCommunicator.removeModifierFromCashListInModify(cashListPosition, modifier, groupPosition);
                        break;
                    }
                }
            }
        }
        listAdapter.notifyDataSetChanged();
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            expListView.expandGroup(i);
        }
    }


    /**
     * can't remember where we use this
     * but its reset cash list---
     */
    public void resetList()
    {
        listAdapter = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);
        expListView.setAdapter(listAdapter);
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            listDataHeader.get(i).setIsDelete(false);
            listDataHeader.get(i).setToDelete(false);
            expListView.expandGroup(i);
        }
        listAdapter.notifyDataSetChanged();
    }


    public int returnProductForCustomer(int clientPosition)
    {
        int count = 0;
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            if (listDataHeader.get(i).getClientPosition() != 0)
            {
                if (listDataHeader.get(i).getClientPosition() == clientPosition)
                {
                    count++;
                }
            }
        }
        return count;
    }


    /**
     * delet a product from list
     *
     * @param groupPosition
     */
    public void deleteProduct(Integer groupPosition)
    {
        //substract price
        if (!dbA.checkIfBillSplitPaid(billId))
        {
            dbA.deleteProductFromBill(billId, groupPosition);
            int qq = listDataHeader.get(groupPosition).getQuantityInt();
            oldModifiedProduct = listDataHeader.get(groupPosition);
            oldModifiedProduct.setQuantity(1);

            oldModifiedProduct.setNewCashList(listDataChild.get(listDataHeader.get(groupPosition)));
            cashListIndex = dbA.getTotalBillPrintedIndex(billId);
            oldListDataChild.put(oldModifiedProduct, oldModifiedProduct.getCashList());
            if (billId != -1 && groupPosition <= cashListIndex)
            {
                Customer customer = new Customer();
                if (listDataCustomer.size() > 0)
                {
                    customer = listDataCustomer.get(oldModifiedProduct.getClientPosition() - 1);
                }
                ArrayList<CashButtonLayout> bb = listDataHeader;
                printOrderDelete(1, customer);

            }
            listDataHeader.get(groupPosition).setQuantity(qq);

            //printOrderDelete(groupPosition);
            subPrice(listDataHeader.get(groupPosition));
            if (listDataHeader.get(groupPosition).getQuantityInt() > 1)
            {
                Integer productQuantity = listDataHeader.get(groupPosition).getQuantityInt();
                listDataHeader.get(groupPosition)
                              .setQuantity(listDataHeader.get(groupPosition).getQuantityInt() - 1);
                for (int i = 0; i < listDataChild.get(listDataHeader.get(groupPosition))
                                                 .size(); i++)
                {

                    Integer qty = listDataChild.get(listDataHeader.get(groupPosition))
                                               .get(i)
                                               .getQuantityInt();
                    int toSubstract = qty / productQuantity;
                    listDataChild.get(listDataHeader.get(groupPosition))
                                 .get(i)
                                 .setQuantity(qty - toSubstract);

                }
                //printOrderDelete(int changedPosition)
            }
            else
            {
                if (returnProductForCustomer(listDataHeader.get(groupPosition)
                                                           .getClientPosition()) == 1)
                {
                    listDataChild.remove(listDataHeader.get(groupPosition));
                    listDataHeader.get(groupPosition).setTitle("");
                    listDataHeader.get(groupPosition).setPrice(0.0f);
                    listDataHeader.get(groupPosition).setQuantity(1);
                    listDataHeader.get(groupPosition).setProductId(-20);
                    listDataHeader.get(groupPosition).setIsDelete(false);
                    listDataHeader.get(groupPosition).setModifyModifier(false);
                    listDataHeader.get(groupPosition).setPosition(listDataHeader.size());
                    listDataHeader.get(groupPosition).setID(-20);

                }
                else
                {
                    listDataChild.remove(listDataHeader.get(groupPosition));
                    listDataHeader.remove(groupPosition);
                    for (int i = 0; i < listDataHeader.size(); i++)
                    {
                        if (i > groupPosition)
                        {
                            listDataHeader.get(i).setPosition(i);
                        }
                        if (groupPosition != i)
                        {
                            listDataHeader.get(i).setIsDelete(false);

                        }
                        else
                        {
                            listDataHeader.remove(i);
                        }
                        expListView.expandGroup(i);
                    }
                }


            }
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                listDataHeader.get(i).setPosition(i);
            }

            if (listDataHeader.size() == 0)
            {
                dbA.updateBillTotalPrintedIndex(0, billId);
            }
            else
            {
                dbA.updateBillTotalPrintedIndex(listDataHeader.size(), billId);
            }
            ArrayList<Customer> a = listDataCustomer;
            cashListIndex = listDataHeader.size();
            listAdapter.notifyDataSetChanged();
            deleteProduct = false;
            activityCommunicator.goToMainPage();

        }
        else
        {
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (groupPosition != i)
                {
                    listDataHeader.get(i).setIsDelete(false);
                }
                else
                {
                    listDataHeader.remove(i);
                }
                expListView.expandGroup(i);
            }
            deleteProduct = false;
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                listDataHeader.get(i).setPosition(i);
            }
            listAdapter.notifyDataSetChanged();
        }
    }


    public void deleteAllProductsFromServer(int groupPosition)
    {
        int qq = listDataHeader.get(groupPosition).getQuantityInt();
        if (oldModifiedProduct.getTitle() == null)
        {

            oldModifiedProduct = listDataHeader.get(groupPosition);
            oldModifiedProduct.setQuantity(qq);
        }
        oldModifiedProduct.setNewCashList(listDataChild.get(listDataHeader.get(groupPosition)));
        oldListDataChild.put(oldModifiedProduct, oldModifiedProduct.getCashList());
        Customer customer = new Customer();
        if (listDataCustomer.size() > 0)
        {
            customer = listDataCustomer.get(oldModifiedProduct.getClientPosition() - 1);
        }
        printOrderDelete(1, customer);
        listDataHeader.get(groupPosition).setQuantity(qq);

        subPriceAllProduct(listDataHeader.get(groupPosition));
        if (returnProductForCustomer(listDataHeader.get(groupPosition).getClientPosition()) == 1)
        {
            listDataChild.remove(listDataHeader.get(groupPosition));

            listDataHeader.get(groupPosition).setTitle("");
            listDataHeader.get(groupPosition).setPrice(0.0f);
            listDataHeader.get(groupPosition).setQuantity(1);
            listDataHeader.get(groupPosition).setProductId(-20);
            listDataHeader.get(groupPosition).setIsDelete(false);
            listDataHeader.get(groupPosition).setModifyModifier(false);
            listDataHeader.get(groupPosition).setPosition(listDataHeader.size());
            listDataHeader.get(groupPosition).setID(-20);
            listDataHeader.get(groupPosition).setNewCashList(new ArrayList<>());

        }
        else
        {
            listDataChild.remove(listDataHeader.get(groupPosition));
            listDataHeader.remove(groupPosition);
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (i > groupPosition)
                {
                    listDataHeader.get(i).setPosition(i);
                }
                if (groupPosition != i)
                {
                    listDataHeader.get(i).setIsDelete(false);

                }
                else
                {
                    //listDataHeader.remove(i);
                }
                expListView.expandGroup(i);
            }
        }
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            listDataHeader.get(i).setPosition(i);
        }

        if (listDataHeader.size() == 0)
        {
            cashListIndex = 0;
        }
        else
        {
            int i = 0;
            for (CashButtonLayout button : listDataHeader)
            {
                if (button.getProductId() != -20)
                {
                    i++;
                }
            }
            cashListIndex = i;
        }
        listAdapter.notifyDataSetChanged();
        deleteProduct = false;
        activityCommunicator.goToMainPage();
    }


    public void deleteAllProducts(Integer groupPosition)
    {
        if (StaticValue.blackbox)
        {
            RequestParam params = new RequestParam();
            params.add("billId", String.valueOf(billId));
            params.add("groupPosition", String.valueOf(groupPosition));
            ((Operative) getContext()).callHttpHandler("/checkIfBillSplitPaid", params);
        }
        else
        {

            if (!dbA.checkIfBillSplitPaid(billId))
            {
                //il conto non è splittato
                dbA.deleteProductFromBill(billId, groupPosition);
                int qq = listDataHeader.get(groupPosition).getQuantityInt();
                if (oldModifiedProduct.getTitle() == null)
                {

                    oldModifiedProduct = listDataHeader.get(groupPosition);
                    oldModifiedProduct.setQuantity(qq);
                }

                oldModifiedProduct.setNewCashList(listDataChild.get(listDataHeader.get(groupPosition)));
                cashListIndex = dbA.getTotalBillPrintedIndex(billId);
                oldListDataChild.put(oldModifiedProduct, oldModifiedProduct.getCashList());
                if (billId != -1 && groupPosition <= cashListIndex)
                {
                    Customer customer = new Customer();
                    if (listDataCustomer.size() > 0)
                    {
                        customer = listDataCustomer.get(oldModifiedProduct.getClientPosition() - 1);
                    }
                    printOrderDelete(1, customer);

                }
                listDataHeader.get(groupPosition).setQuantity(qq);

                subPriceAllProduct(listDataHeader.get(groupPosition));
                if (returnProductForCustomer(listDataHeader.get(groupPosition)
                                                           .getClientPosition()) == 1)
                {
                    listDataChild.remove(listDataHeader.get(groupPosition));

                    listDataHeader.get(groupPosition).setTitle("");
                    listDataHeader.get(groupPosition).setPrice(0.0f);
                    listDataHeader.get(groupPosition).setQuantity(1);
                    listDataHeader.get(groupPosition).setProductId(-20);
                    listDataHeader.get(groupPosition).setIsDelete(false);
                    listDataHeader.get(groupPosition).setModifyModifier(false);
                    listDataHeader.get(groupPosition).setPosition(listDataHeader.size());
                    listDataHeader.get(groupPosition).setID(-20);
                    listDataHeader.get(groupPosition).setNewCashList(new ArrayList<>());

                }
                else
                {
                    listDataChild.remove(listDataHeader.get(groupPosition));
                    listDataHeader.remove(groupPosition);
                    for (int i = 0; i < listDataHeader.size(); i++)
                    {
                        if (i > groupPosition)
                        {
                            listDataHeader.get(i).setPosition(i);
                        }
                        if (groupPosition != i)
                        {
                            listDataHeader.get(i).setIsDelete(false);

                        }
                        else
                        {
                            listDataHeader.remove(i);
                        }
                        expListView.expandGroup(i);
                    }
                }
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    listDataHeader.get(i).setPosition(i);
                }

                if (listDataHeader.size() == 0)
                {
                    dbA.updateBillTotalPrintedIndex(0, billId);
                    cashListIndex = 0;
                }
                else
                {
                    int i = 0;
                    for (CashButtonLayout button : listDataHeader)
                    {
                        if (button.getProductId() != -20)
                        {
                            i++;
                        }
                    }
                    //if(cashListIndex>0)
                    dbA.updateBillTotalPrintedIndex(cashListIndex - 1, billId);
                }
                ArrayList<Customer> a = listDataCustomer;
                //cashListIndex = listDataHeader.size();
                listAdapter.notifyDataSetChanged();
                deleteProduct = false;
                activityCommunicator.goToMainPage();

            }
            else
            {
                //il conto è splittato
            /*for (int i = 0; i < listDataHeader.size(); i++) {
                if (groupPosition != i) {
                    listDataHeader.get(i).setIsDelete(false);

                } else {
                    listDataHeader.remove(i);
                }
                expListView.expandGroup(i);
            }
            deleteProduct = false;
            for (int i = 0; i < listDataHeader.size(); i++) {
                listDataHeader.get(i).setPosition(i);
            }
            listAdapter.notifyDataSetChanged();*/
                Toast.makeText(context, R.string.part_of_this_bill_is_already_paid, Toast.LENGTH_SHORT)
                     .show();

            }
        }
    }


    /**
     * delete customer from cash list
     *
     * @param customerPosition
     */
    public void deleteCustomer(int customerPosition)
    {
        //remove all product associated to costume
        for (int i = listDataHeader.size() - 1; i >= 0; i--)
        {
            if (listDataHeader.get(i).getClientPosition() == customerPosition)
            {
                subPrice(listDataHeader.get(i));

                if (listDataHeader.get(i).getProductId() != -20)
                {
                    oldModifiedProduct = listDataHeader.get(i);
                    oldListDataChild.put(oldModifiedProduct, oldModifiedProduct.getCashList());
                    Customer customer = new Customer();
                    if (listDataCustomer.size() > 0)
                    {
                        customer = listDataCustomer.get(oldModifiedProduct.getClientPosition() - 1);
                    }
                    printOrderDelete(i, customer);
                }

                listDataHeader.remove(i);
                oldModifiedProduct = new CashButtonLayout();
                oldListDataChild   = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            }
        }

        //set customer position for left product
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            if (listDataHeader.get(i).getClientPosition() > customerPosition)
            {
                listDataHeader.get(i)
                              .setClientPosition(listDataHeader.get(i).getClientPosition() - 1);
            }
            listDataHeader.get(i).setPosition(i);

        }

        //remove customer from customer list
        for (int i = 0; i < listDataCustomer.size(); i++)
        {
            if (i == customerPosition - 1)
            {
                listDataCustomer.remove(i);
            }
        }

        //set position for customers in list
        for (int i = 0; i < listDataCustomer.size(); i++)
        {
            if (listDataCustomer.get(i).getPosition() > customerPosition)
            {
                listDataCustomer.get(i).setPosition(listDataCustomer.get(i).getPosition() - 1);
                //listDataCustomer.get(i).setDescription("Customer " + (i+1));
                listDataCustomer.get(i).setActive(false);


            }
        }

        //reset current customer
        currentCustomerArray = new ArrayList<Integer>();
        listAdapter.setCustomerList(listDataCustomer);
        if (listDataCustomer.size() == 0)
        {
            listAdapter.setFirstClientFalse();
        }
        listAdapter.notifyDataSetChanged();

        activityCommunicator = (ActivityCommunicator) context;
        activityCommunicator.goToMainPage();

    }


    /**
     * set cash list to normal appareance
     */
    public void resetCashNotChanghed()
    {
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            listDataHeader.get(i).setIsDelete(false);
            listDataHeader.get(i).setToDelete(false);
            expListView.expandGroup(i);
        }
    }


    /**
     * add or remove quantity to product
     *
     * @param groupPosition
     * @param add           boolean true-> add, false -> remove
     */
    public void addQuantityToCashList(Integer groupPosition, Boolean add)
    {
        View modifyButton = view.findViewById(R.id.modify_end_button);
        modifyButton.setVisibility(View.VISIBLE);
        ImageView totalBarLabel = (ImageView) view.findViewById(R.id.cash_euro_label);
        totalBarLabel.setVisibility(View.GONE);
        CustomTextView totalBar = (CustomTextView) view.findViewById(R.id.cash_euro_total);
        totalBar.setVisibility(View.GONE);
        CustomTextView price = (CustomTextView) view.findViewById(R.id.cash_total);
        if (add)
        {
            //aggiungo uno

            oldModifiedProduct.setTitle(listDataHeader.get(groupPosition).getTitle());
            oldModifiedProduct.setPrice(listDataHeader.get(groupPosition).getPriceFloat());
            if (oldModifiedProduct.getQuantityInt() == -1)
            {
                oldModifiedProduct.setQuantity(listDataHeader.get(groupPosition).getQuantityInt());
            }
            oldModifiedProduct.setIsDelete(false);
            oldModifiedProduct.setProductId(listDataHeader.get(groupPosition).getProductId());
            oldModifiedProduct.setPrinterId(listDataHeader.get(groupPosition).getPrinterId());
            oldModifiedProduct.resetCashList();

            modifyChangePosition = groupPosition;

            CashButtonLayout button1    = listDataHeader.get(groupPosition);
            Integer          previusqty = listDataHeader.get(groupPosition).getQuantityInt();
            listDataHeader.get(groupPosition).setQuantity(previusqty + 1);
            String total = button1.getTotal(button1.getQuantityInt());
            price.setText(total);
            updateTotal(button1.getPriceFloat());

            if (listDataChild.get(listDataHeader.get(groupPosition)) != null)
            {
                for (int i = 0; i < listDataChild.get(listDataHeader.get(groupPosition))
                                                 .size(); i++)
                {
                    Integer qty = listDataChild.get(listDataHeader.get(groupPosition))
                                               .get(i)
                                               .getQuantityInt();
                    listDataChild.get(listDataHeader.get(groupPosition))
                                 .get(i)
                                 .setQuantity((qty / previusqty) * (previusqty + 1));
                    updateTotal(listDataChild.get(listDataHeader.get(groupPosition))
                                             .get(i)
                                             .getPriceFloat());

                }
            }
            listDataHeader.get(groupPosition).setIsDelete(true);
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (i != groupPosition)
                {
                    expListView.expandGroup(i);
                }

            }

            newModifiedProduct = listDataHeader.get(groupPosition);

        }
        else
        {
            //elimino uno
            if (listDataHeader.get(groupPosition).getQuantityInt() == 1)
            {
                deleteAllProducts(groupPosition);
                activityCommunicator.endModifyModifier(-1);
                ModifierFragment.setModify(false);
                endModifyModifier(-1);
                setModifyProduct(false);
                activityCommunicator.endModifyProduct();
                activityCommunicator.goToMainPage();
                resetCashNotChanghed();
                deleteProduct = false;
            }
            else
            {

                oldModifiedProduct.setTitle(listDataHeader.get(groupPosition).getTitle());
                oldModifiedProduct.setPrice(listDataHeader.get(groupPosition).getPriceFloat());
                if (oldModifiedProduct.getQuantityInt() == -1)
                {
                    oldModifiedProduct.setQuantity(listDataHeader.get(groupPosition)
                                                                 .getQuantityInt());
                }
                oldModifiedProduct.setIsDelete(false);
                oldModifiedProduct.setProductId(listDataHeader.get(groupPosition).getProductId());
                oldModifiedProduct.setPrinterId(listDataHeader.get(groupPosition).getPrinterId());
                oldModifiedProduct.resetCashList();

                modifyChangePosition = groupPosition;
                if (listDataHeader.get(groupPosition).getQuantityInt() > 1)
                {
                    CashButtonLayout button1    = listDataHeader.get(groupPosition);
                    Integer          previusqty = listDataHeader.get(groupPosition).getQuantityInt();
                    listDataHeader.get(groupPosition).setQuantity(previusqty - 1);
                    //  quantity.setText(listDataHeader.get(groupPosition).getQuantity());
                    String total = button1.getTotal(button1.getQuantityInt());
                    price.setText(total);
                    subPrice(button1);
                    if (listDataChild.get(listDataHeader.get(groupPosition)) != null)
                    {
                        for (int i = 0; i < listDataChild.get(listDataHeader.get(groupPosition))
                                                         .size(); i++)
                        {
                            Integer qty = listDataChild.get(listDataHeader.get(groupPosition))
                                                       .get(i)
                                                       .getQuantityInt();
                            listDataChild.get(listDataHeader.get(groupPosition))
                                         .get(i)
                                         .setQuantity((qty / previusqty) * (previusqty - 1));
                        }
                    }
                }
                listDataHeader.get(groupPosition).setIsDelete(true);
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    if (i != groupPosition)
                    {
                        expListView.expandGroup(i);
                    }

                }
                newModifiedProduct = listDataHeader.get(groupPosition);
            }
        }
        listAdapter.notifyDataSetChanged();
        //printOrderCorrection(modifyChangePosition);
    }


    /**
     * end modify layout for product
     */
    public void endModifyLayout()
    {
        oldModifiedProduct = new CashButtonLayout();
        listAdapter.notifyDataSetChanged();
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            listDataHeader.get(i).setIsDelete(false);
            expListView.expandGroup(i);
        }
        listAdapter.notifyDataSetChanged();

    }


    /**
     * end modify layout for modifier
     */
    public void endModifyModifier(int groupPosition)
    {
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            listDataHeader.get(i).setIsDelete(false);
            listDataHeader.get(i).setModifyModifier(false);
        }
        listAdapter.notifyDataSetChanged();
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            expListView.expandGroup(i);
        }
        if (billId > 0 && groupPosition < cashListIndex && groupPosition != -1)
        {
            Customer         customer = new Customer();
            CashButtonLayout a        = oldModifiedProduct;
            if (listDataCustomer.size() > 0)
            {
                customer = listDataCustomer.get(listDataHeader.get(groupPosition)
                                                              .getClientPosition() - 1);
            }
            if (StaticValue.blackbox)
            {
                ArrayList<CashButtonLayout> toList = new ArrayList<CashButtonLayout>();
                toList.add(listDataHeader.get(groupPosition));
                Map<String, ArrayList<CashButtonListLayout>> test =
                        new HashMap<String, ArrayList<CashButtonListLayout>>();
                test.put(String.valueOf(0), listDataChild.get(listDataHeader.get(groupPosition)));
                for (int i = 0; i < toList.size(); i++)
                {
                    toList.get(i).setPrinted(0);
                }
                RequestParam params      = new RequestParam();
                Gson         gson        = new Gson();
                String       myProducts  = gson.toJson(toList);
                String       mods        = gson.toJson(test);
                String       myCustomers = gson.toJson(listDataCustomer);
                params.add("products", myProducts);
                params.add("modifiers", mods);
                params.add("printType", String.valueOf(10));
                params.add("billId", String.valueOf(billId));
                params.add("customers", myCustomers);
                ((Operative) context).callHttpHandler("/printSingleOrder", params);
            }
            else
            {
                printOrderBill(billId);
                printSingleOrderBill(billId, listDataHeader.get(groupPosition), listDataChild.get(listDataHeader
                        .get(groupPosition)), customer);
            }
        }
    }


    public void printBillOnServerModify()
    {

    }


    /**
     * show item viw for element to be deleted
     * if swipe left delete, if swipe right hide delete
     *
     * @param groupPosition
     * @param toLeft
     */
    public void setItemToDelete(Integer groupPosition, Boolean toLeft)
    {
        if (toLeft)
        {
            if (billId != -1)
            {
                double left = dbA.getBillPrice(billId);
                float  cash = 0.0f;
                for (CashButtonLayout product : listDataHeader)
                {
                    ArrayList<CashButtonListLayout> modifiersList = listDataChild.get(product);
                    if (modifiersList != null)
                    {
                        for (CashButtonListLayout mody : modifiersList)
                        {
                            cash += mody.getPriceFloat() * mody.getQuantityInt();
                        }
                    }
                    cash += product.getPriceFloat() * product.getQuantityInt();
                }
                if (left != cash)
                {
                    left = cash;
                }
                if (left == 0.0f)
                {
                    listDataHeader.get(groupPosition).setToDelete(true);
                    deleteProduct = true;

                    listAdapter.notifyDataSetChanged();
                }
                else
                {
                    float modiP = 0.0f;
                    if (listDataChild.get(listDataHeader.get(groupPosition)) != null)
                    {
                        for (int i = 0; i < listDataChild.get(listDataHeader.get(groupPosition))
                                                         .size(); i++)
                        {
                            Integer qty = listDataChild.get(listDataHeader.get(groupPosition))
                                                       .get(i)
                                                       .getQuantityInt();
                            modiP = modiP + listDataChild.get(listDataHeader.get(groupPosition))
                                                         .get(i)
                                                         .getQuantityInt() * listDataChild.get(listDataHeader
                                    .get(groupPosition)).get(i).getPriceFloat();
                        }
                    }
                    if (modiP + listDataHeader.get(groupPosition)
                                              .getQuantityInt() * listDataHeader.get(groupPosition)
                                                                                .getPriceFloat() <= left)
                    {
                        listDataHeader.get(groupPosition).setToDelete(true);
                        deleteProduct = true;

                        listAdapter.notifyDataSetChanged();
                    }
                }
            }
            else
            {
                listDataHeader.get(groupPosition).setToDelete(true);
                deleteProduct = true;

                listAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            listDataHeader.get(groupPosition).setToDelete(false);
            deleteProduct = false;

            listAdapter.notifyDataSetChanged();
        }
    }


    public void resetCustomerToDelete()
    {
        if (listDataCustomer.size() > 0)
        {
            for (int i = 0; i < listDataCustomer.size(); i++)
            {
                listDataCustomer.get(i).setDelete(false);
            }
            listAdapter.setCustomerList(listDataCustomer);
            resetArrayCustomer();
            listAdapter.resetCustomerList();
            listAdapter.notifyDataSetChanged();
        }
    }


    /**
     * WRONG NAME, NOW WHEN I SWIPE TO RIGHT IF DELETE IS NOT ACTIVE IT FIRE POPUP COSTUMER MODIFY WINDOW
     *
     * @param customerPosition
     * @param toLeft
     */
    public void setCustomerToDelete(Integer customerPosition, Boolean toLeft)
    {
        if (toLeft)
        {
            listDataCustomer.get(customerPosition - 1).setDelete(true);
            listAdapter.setCustomerList(listDataCustomer);
            listAdapter.notifyDataSetChanged();
        }
        else
        {
            if (listDataCustomer.get(customerPosition - 1).getDelete())
            {
                listDataCustomer.get(customerPosition - 1).setDelete(false);
                listAdapter.setCustomerList(listDataCustomer);
                listAdapter.notifyDataSetChanged();
            }
            else
            {
                Intent intent     = getActivity().getIntent();
                int    numberBill = intent.getIntExtra("orderNumber", -1);
                if (numberBill == -1)
                {
                    numberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
                }
                long             sessionTime = dbA.getLastClosing();
                Date             date        = new Date(sessionTime);
                SimpleDateFormat df2         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String           dateText    = df2.format(date);

                int newBillId = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateText + "'");
                if (newBillId == -11)
                {
                    if (listDataHeader.size() > 0)
                    {
                        //salvo i data header e i suoi subelementi
                        int billId = dbA.saveTotalBillForPayment(total, numberBill, 0);
                        bill_id                   = billId;
                        CashFragment.this.bill_id = billId;
                        if (billId != 0)
                        {
                            for (int i = 0; i < listDataHeader.size(); i++)
                            {
                                //salvo prodotto
                                int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader
                                        .get(i));
                                if (listDataCustomer.size() > 0)
                                {
                                    Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                                    .getClientPosition() - 1);
                                    dbA.saveCustomerBillForPayment(c, prodId);
                                }
                                if (listDataChild.get(listDataHeader.get(i)) != null)
                                {
                                    for (int j = 0; j < listDataChild.get(listDataHeader.get(i))
                                                                     .size(); j++)
                                    {
                                        //salvo tutti i figli
                                        dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader
                                                .get(i)).get(j));

                                    }
                                }
                            }
                            setModifyBar(false);
                        }
                    }

                }
                else
                {
                    if (listDataHeader.size() != 0)
                    {
                        dbA.updateBillPrice(billId, total);
                        dbA.deleteCustomerForBill(billId);
                        for (int i = 0; i < listDataHeader.size(); i++)
                        {
                            //salvo prodotto
                            int check = dbA.checkProductBillForPayment(i, billId, listDataHeader.get(i));
                            if (check == -11)
                            {
                                //salvo nuovo prodotto
                                int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader
                                        .get(i));
                                if (listDataCustomer.size() > 0)
                                {
                                    Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                                    .getClientPosition() - 1);
                                    dbA.saveCustomerBillForPayment(c, prodId);
                                }
                                if (listDataChild.get(listDataHeader.get(i)) != null)
                                {
                                    for (int j = 0; j < listDataChild.get(listDataHeader.get(i))
                                                                     .size(); j++)
                                    {
                                        //salvo tutti i figli
                                        dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader
                                                .get(i)).get(j));
                                    }
                                }
                            }
                            else
                            {
                                dbA.updateProductBillForPaymentQuantity(listDataHeader.get(i)
                                                                                      .getQuantityInt(), check, listDataHeader
                                        .get(i)
                                        .getProductId());
                                if (listDataCustomer.size() > 0)
                                {
                                    Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                                    .getClientPosition() - 1);
                                    dbA.saveCustomerBillForPayment(c, check);
                                }
                                if (listDataChild.get(listDataHeader.get(i)) != null)
                                {
                                    for (int j = 0; j < listDataChild.get(listDataHeader.get(i))
                                                                     .size(); j++)
                                    {
                                        int check1 = dbA.checkModifierBillForPayment(j, check, listDataChild
                                                .get(listDataHeader.get(i))
                                                .get(j));
                                        if (check1 == -11)
                                        {
                                            //mod non c'è
                                            dbA.saveModifierBillForPayment(j, check, listDataChild.get(listDataHeader
                                                    .get(i)).get(j));
                                        }
                                        else
                                        {
                                            //update
                                            if (listDataChild.get(listDataHeader.get(i))
                                                             .get(j)
                                                             .getModifierId() == -15)
                                            {
                                                dbA.updateModifierBillNote(listDataChild.get(listDataHeader
                                                        .get(i)).get(j), check1);
                                            }
                                            dbA.updateModifierBillForPaymentQuantity(listDataChild.get(listDataHeader
                                                    .get(i)).get(j).getQuantityInt(), check1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    dbA.deleteLeftProductFromBill(billId, listDataHeader.size() - 1);
                }
                long             sessionTimeb = dbA.getLastClosing();
                Date             dateb        = new Date(sessionTimeb);
                SimpleDateFormat df2b         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String           dateTextb    = df2b.format(dateb);
                billId = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill + " and creation_time>='" + dateTextb + "';");
                ((Operative) getContext()).openCustomerPopup(listDataCustomer.get(customerPosition - 1)
                                                                             .getDescription(),
                        (listDataCustomer.get(customerPosition - 1)).getCustomerId(), intent.getIntExtra("orderNumber", -1), true, (customerPosition - 1), tableNumber
                );

            }
        }
    }


    /**
     * update note modifier text, used in modify and insert new
     *
     * @param modifier
     * @param position
     * @param modify
     * @param groupPosition
     * @param cashButtonList
     */
    public void updateNoteModifierText(CashButtonListLayout modifier, Integer position, boolean modify, int groupPosition, List<CashButtonListLayout> cashButtonList)
    {
        //check if i'm modifin
        if (modify)
        {
            //modify
            if (groupPosition != -1)
            {
                if (listDataChild.get(listDataHeader.get(groupPosition)) != null)
                {
                    for (int i = 0; i < listDataChild.get(listDataHeader.get(groupPosition))
                                                     .size(); i++)
                    {
                        if (-15 == listDataChild.get(listDataHeader.get(groupPosition))
                                                .get(i)
                                                .getModifierId())
                        {
                            listDataChild.get(listDataHeader.get(groupPosition))
                                         .get(i)
                                         .setNote(modifier.getNote());
                            break;
                        }
                    }
                }
            }
            else
            {
                if (listDataCustomer.size() > 0)
                {
                    if (currentCustomer != listDataCustomer.size())
                    {
                        int lastposition = -1;
                        for (int i = 0; i < listDataHeader.size(); i++)
                        {
                            if (listDataHeader.get(i).getClientPosition() == currentCustomer)
                            {
                                lastposition = i;
                            }
                        }
                        for (int i = 0; i < listDataChild.get(listDataHeader.get(lastposition))
                                                         .size(); i++)
                        {
                            if (-15 == listDataChild.get(listDataHeader.get(lastposition))
                                                    .get(i)
                                                    .getModifierId())
                            {
                                listDataChild.get(listDataHeader.get(lastposition))
                                             .get(i)
                                             .setNote(modifier.getNote());
                                break;
                            }
                        }

                    }
                    else
                    {
                        int lastposition = -1;
                        for (int i = 0; i < listDataHeader.size(); i++)
                        {
                            if (listDataHeader.get(i)
                                              .getClientPosition() == listDataCustomer.size())
                            {
                                lastposition = i;
                            }
                        }
                        for (int i = 0; i < listDataChild.get(listDataHeader.get(lastposition))
                                                         .size(); i++)
                        {
                            if (-15 == listDataChild.get(listDataHeader.get(lastposition))
                                                    .get(i)
                                                    .getModifierId())
                            {
                                listDataChild.get(listDataHeader.get(lastposition))
                                             .get(i)
                                             .setNote(modifier.getNote());
                                break;
                            }
                        }
                    }
                }
            }
            listAdapter.notifyDataSetChanged();

        }
        else
        {
            //insert new
            if (groupPosition != -1)
            {
                listDataChild.put(listDataHeader.get(groupPosition), (ArrayList<CashButtonListLayout>) cashButtonList);
                dbA.addNewModifierFromBill(billId, listDataChild.get(listDataHeader.get(groupPosition))
                                                                .size() - 1, listDataHeader.get(groupPosition)
                                                                                           .getProductId(), groupPosition, cashButtonList
                        .get(cashButtonList.size() - 1));

            }
            else
            {
                if (listDataCustomer.size() > 0)
                {
                    if (currentCustomer != listDataCustomer.size())
                    {
                        int lastposition = -1;
                        for (int i = 0; i < listDataHeader.size(); i++)
                        {
                            if (listDataHeader.get(i).getClientPosition() - 1 == currentCustomer)
                            {
                                lastposition = i;
                            }
                        }
                        listDataChild.put(listDataHeader.get(lastposition), (ArrayList<CashButtonListLayout>) cashButtonList);
                    }
                    else
                    {
                        int lastposition = -1;
                        for (int i = 0; i < listDataHeader.size(); i++)
                        {
                            if (listDataHeader.get(i)
                                              .getClientPosition() == listDataCustomer.size())
                            {
                                lastposition = i;
                            }
                        }
                        listDataChild.put(listDataHeader.get(lastposition), (ArrayList<CashButtonListLayout>) cashButtonList);

                    }


                }
                else
                {
                    listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), (ArrayList<CashButtonListLayout>) cashButtonList);
                }
            }
            listAdapter.notifyDataSetChanged();


        }
        for (int i = 0; i < listDataHeader.size(); i++)
        {
            expListView.expandGroup(i);
        }
        listAdapter.notifyDataSetChanged();
    }


    //delete note
    public void deleteNoteFromList(int groupPosition)
    {
        if (groupPosition != -1)
        {
            for (int i = 0; i < listDataChild.get(listDataHeader.get(groupPosition)).size(); i++)
            {
                if (-15 == listDataChild.get(listDataHeader.get(groupPosition))
                                        .get(i)
                                        .getModifierId())
                {
                    dbA.deleteModifierFromBill(billId, i, listDataHeader.get(groupPosition)
                                                                        .getProductId(), groupPosition);
                    listDataChild.get(listDataHeader.get(groupPosition)).remove(i);
                    listAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
        else
        {
            for (int i = 0; i < listDataChild.get(listDataHeader.get(listDataHeader.size() - 1))
                                             .size(); i++)
            {
                if (-15 == listDataChild.get(listDataHeader.get(listDataHeader.size() - 1))
                                        .get(i)
                                        .getModifierId())
                {
                    dbA.deleteModifierFromBill(billId, i, listDataHeader.get(listDataHeader.size() - 1)
                                                                        .getProductId(), listDataHeader
                            .size() - 1);
                    listDataChild.get(listDataHeader.get(listDataHeader.size() - 1)).remove(i);
                    listAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }


    public void reprintOrderBill(int billId)
    {
        if (listDataCustomer.size() == 0)
        {
            ArrayList<CashButtonLayout>                                newProducts  = new ArrayList<CashButtonLayout>();
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            //position is neded because in server method the modifiers are remapped on product using map key position
            //so I set the new position using an int and incrementing it (maybe this could be done better?)
            int position = 0;
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (listDataHeader.get(i).getProductId() != -20)
                {
                    CashButtonLayout newButton = listDataHeader.get(i);
                    newButton.setPosition(position);
                    position++;
                    newProducts.add(newButton);
                    newModifiers.put(newButton, listDataChild.get(listDataHeader.get(i)));
                }
            }


            Gson       gson     = new Gson();
            JSONObject combined = new JSONObject();
/*             ClientDelegate myClient = new ClientDelegate(8080);
             myClient.delegate = forClient;*/
            Intent intent      = getActivity().getIntent();
            int    orderNumber = intent.getIntExtra("orderNumber", -1);
         /*    try {
                 combined.put("products", gson.toJson(newProducts));
                 Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization()
                         .setPrettyPrinting().create();
                 combined.put("modifiers", gson1.toJson(newModifiers));
                 //print type ==8 is order print only
                 combined.put("printType", 12);
                 combined.put("IP", IP);
                 combined.put("deviceName", deviceName);
                 combined.put("orderNumber", orderNumber);
                 combined.put("billId", billId);
                 combined.put("indexList", cashListIndex);
                 combined.put("customerList", gson.toJson(listDataCustomer));
                 combined.put("tableNumber", tableNumber);
                 Room room = dbA.fetchRoomById(roomId);
                 if(room.getId()>0)
                     combined.put("roomName", room.getName());
                 else combined.put("roomName", "");


             } catch (JSONException e) {
                 e.printStackTrace();
             }
             gson.toJson(combined);
             ClientThread myThread = ClientThread.getInstance();
             myThread.delegate = forClient;
             myThread.setClientThread();*/
            //myThread.addJsonString(combined.toString());


            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(newProducts);
            myThread.setModifiers(newModifiers);
            myThread.setPrintType(12);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCustomers(listDataCustomer);
            myThread.setTableNumber(tableNumber);
            myThread.setIndexList(cashListIndex);
            Room room = dbA.fetchRoomById(roomId);
            if (room.getId() > 0)
            {
                myThread.setRoomName(room.getName());
            }
            else
            {
                myThread.setRoomName("");
            }
            myThread.setClientThread();
            myThread.setRunBaby(true);


            if (listDataHeader.size() == 0)
            {
                dbA.updateBillTotalPrintedIndex(0, billId);
            }
            else
            {
                dbA.updateBillTotalPrintedIndex(listDataHeader.size(), billId);
            }
        }
        else
        {
            ArrayList<CashButtonLayout>                                newProducts  = new ArrayList<CashButtonLayout>();
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            //position is neded because in server method the modifiers are remapped on product using map key position
            //so I set the new position using an int and incrementing it (maybe this could be done better?)
            int position = 0;
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (listDataHeader.get(i).getProductId() != -20)
                {
                    CashButtonLayout newButton = listDataHeader.get(i);
                    newButton.setPosition(position);
                    position++;
                    newProducts.add(newButton);
                    newModifiers.put(newButton, listDataChild.get(listDataHeader.get(i)));
                }
            }


            Gson       gson     = new Gson();
            JSONObject combined = new JSONObject();
             /*ClientDelegate myClient = new ClientDelegate(8080);
             myClient.delegate = forClient;*/
            Intent intent      = getActivity().getIntent();
            int    orderNumber = intent.getIntExtra("orderNumber", -1);
            /* try {
                 combined.put("products", gson.toJson(newProducts));
                 Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization()
                         .setPrettyPrinting().create();
                 combined.put("modifiers", gson1.toJson(newModifiers));
                 //print type ==8 is order print only
                 combined.put("printType", 12);
                 combined.put("IP", IP);
                 combined.put("deviceName", deviceName);
                 combined.put("orderNumber", orderNumber);
                 combined.put("billId", billId);
                 combined.put("indexList", cashListIndex);
                 combined.put("customerList", gson.toJson(listDataCustomer));
                 combined.put("tableNumber", tableNumber);
                 Room room = dbA.fetchRoomById(roomId);
                 combined.put("roomName", room.getName());


             } catch (JSONException e) {
                 e.printStackTrace();
             }
             gson.toJson(combined);
             ClientThread myThread = ClientThread.getInstance();
             myThread.delegate = forClient;
             myThread.setClientThread();
             myThread.addJsonString(combined.toString());

             myThread.addJsonString(combined.toString());*/


            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(newProducts);
            myThread.setModifiers(newModifiers);
            myThread.setPrintType(12);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCustomers(listDataCustomer);
            myThread.setTableNumber(tableNumber);
            myThread.setIndexList(cashListIndex);
            Room room = dbA.fetchRoomById(roomId);
            if (room.getId() > 0)
            {
                myThread.setRoomName(room.getName());
            }
            else
            {
                myThread.setRoomName("");
            }
            myThread.setClientThread();
            myThread.setRunBaby(true);

            if (listDataHeader.size() == 0)
            {
                dbA.updateBillTotalPrintedIndex(0, billId);
            }
            else
            {
                dbA.updateBillTotalPrintedIndex(listDataHeader.size(), billId);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void printOrderBill(int billId)
    {
        Room room = dbA.fetchRoomById(roomId);
        if (listDataCustomer.size() == 0)
        {
            ArrayList<CashButtonLayout>                                newProducts  = new ArrayList<CashButtonLayout>();
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            //position is neded because in server method the modifiers are remapped on product using map key position
            //so I set the new position using an int and incrementing it (maybe this could be done better?)
            int position     = 0;
            int printedIndex = dbA.getTotalBillPrintedIndex(billId);
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (i >= (printedIndex) && listDataHeader.get(i).getProductId() != -20)
                {
                    CashButtonLayout newButton = listDataHeader.get(i);
                    newButton.setPosition(position);
                    newButton.setVat(newButton.getVat());
                    position++;
                    newProducts.add(newButton);
                    ArrayList<CashButtonListLayout> mList = new ArrayList<CashButtonListLayout>();
                    mList = listDataChild.get(listDataHeader.get(i));
                    if (mList != null)
                    {
                        mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getPosition() - c2
                                .getPosition());
                        mList.sort((CashButtonListLayout c1, CashButtonListLayout c2) -> c1.getGroupPosition() - c2
                                .getGroupPosition());

                    }
                    newModifiers.put(newButton, listDataChild.get(listDataHeader.get(i)));
                    //newModifiers.put(newButton, mList);
                }
            }


            Intent intent      = getActivity().getIntent();
            int    orderNumber = intent.getIntExtra("orderNumber", -1);

            if (listDataHeader.size() == 0)
            {
                dbA.updateBillTotalPrintedIndex(0, billId);
            }
            else
            {
                dbA.updateBillTotalPrintedIndex(listDataHeader.size(), billId);
            }

            ClientThread myThread = ClientThread.getInstance();
            myThread.setContext(getActivity());
            myThread.setProducts(newProducts);
            myThread.setModifiers(newModifiers);
            myThread.setPrintType(8);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCustomers(listDataCustomer);
            myThread.setTableNumber(tableNumber);
            myThread.setIndexList(cashListIndex);

            if (room.getId() > 0)
            {
                myThread.setRoomName(room.getName());
            }
            else
            {
                myThread.setRoomName("");
            }
            myThread.setClientThread();
            myThread.setRunBaby(true);


        }
        else
        {
            ArrayList<CashButtonLayout>                                newProducts  = new ArrayList<CashButtonLayout>();
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            //position is neded because in server method the modifiers are remapped on product using map key position
            //so I set the new position using an int and incrementing it (maybe this could be done better?)
            int position      = 0;
            int cashListIndex = dbA.getTotalBillPrintedIndex(billId);
            for (int i = 0; i < listDataHeader.size(); i++)
            {
                if (i >= (cashListIndex) && listDataHeader.get(i).getProductId() != -20)
                {
                    CashButtonLayout newButton = listDataHeader.get(i);
                    newButton.setPosition(position);
                    newButton.setVat(newButton.getVat());
                    position++;
                    newProducts.add(newButton);
                    newModifiers.put(newButton, listDataChild.get(listDataHeader.get(i)));
                }
            }


            Intent intent      = getActivity().getIntent();
            int    orderNumber = intent.getIntExtra("orderNumber", -1);


            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(newProducts);
            myThread.setModifiers(newModifiers);
            myThread.setPrintType(8);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCustomers(listDataCustomer);
            myThread.setTableNumber(tableNumber);
            myThread.setIndexList(cashListIndex);
            if (room.getId() > 0)
            {
                myThread.setRoomName(room.getName());
            }
            else
            {
                myThread.setRoomName("");
            }
            myThread.setClientThread();
            myThread.setRunBaby(true);

            if (listDataHeader.size() == 0)
            {
                dbA.updateBillTotalPrintedIndex(0, billId);
            }
            else
            {
                dbA.updateBillTotalPrintedIndex(listDataHeader.size(), billId);
            }
        }
    }


    public void printOrderCorrection(int changedPosition, Customer customer)
    {
        int tryme = (cashListIndex - 1);
        if (changedPosition <= tryme)
        {
            if (oldModifiedProduct.getProductId() == newModifiedProduct.getProductId() &&
                    oldModifiedProduct.getQuantityInt() <= newModifiedProduct.getQuantityInt())
            {
                //same products different
                //two if because I don't do anithing if equals....
                if (oldModifiedProduct.getQuantityInt() != newModifiedProduct.getQuantityInt())
                {
                    HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                    CashButtonLayout                                           newButton    = listDataHeader.get(changedPosition);
                    newButton.setPosition(0);
                    newModifiers.put(newButton, listDataChild.get(listDataHeader.get(changedPosition)));


                    Intent intent      = getActivity().getIntent();
                    int    orderNumber = intent.getIntExtra("orderNumber", -1);

                    if (StaticValue.blackbox)
                    {
                        Map<String, ArrayList<CashButtonListLayout>> test =
                                new HashMap<String, ArrayList<CashButtonListLayout>>();

                        test.put(String.valueOf(0), listDataChild.get(listDataHeader.get(changedPosition)));

                        RequestParam params      = new RequestParam();
                        Gson         gson        = new Gson();
                        String       newProduct  = gson.toJson(newModifiedProduct);
                        String       mods        = gson.toJson(test);
                        String       myCustomers = gson.toJson(listDataCustomer);
                        String       myCustomer  = gson.toJson(customer);
                        params.add("oldProduct", newProduct);

                        if (newModifiedProduct.getQuantityInt() >= oldModifiedProduct.getQuantityInt())
                        {
                            params.add("quantity", String.valueOf((newModifiedProduct.getQuantityInt() - oldModifiedProduct.getQuantityInt())));
                        }
                        else
                        {
                            params.add("quantity", String.valueOf((oldModifiedProduct.getQuantityInt() - newModifiedProduct.getQuantityInt())));
                        }

                        params.add("modifiers", mods);
                        params.add("printType", String.valueOf(10));
                        params.add("billId", String.valueOf(billId));
                        params.add("deviceName", String.valueOf(deviceName));
                        params.add("orderNumber", String.valueOf(orderNumber + 1)); // add +1,
                        params.add("tableNumber", String.valueOf(tableNumber));
                        params.add("customers", myCustomers);
                        params.add("customer", myCustomer);
                        params.add("changedPosition", String.valueOf(changedPosition));

                        ((Operative) context).callHttpHandler("/printOrderCorrectionInc", params);
                    }
                    else
                    {
                        ClientThread myThread = ClientThread.getInstance();
                        myThread.setOldProducts(newModifiedProduct);
                        if (newModifiedProduct.getQuantityInt() >= oldModifiedProduct.getQuantityInt())
                        {
                            myThread.setQuantity(newModifiedProduct.getQuantityInt() - oldModifiedProduct
                                    .getQuantityInt());
                        }
                        else
                        {
                            myThread.setQuantity(oldModifiedProduct.getQuantityInt() - newModifiedProduct
                                    .getQuantityInt());
                        }
                        myThread.setModifiers(newModifiers);
                        myThread.setPrintType(10);
                        myThread.setBillId(String.valueOf(billId));
                        myThread.setDeviceName(deviceName);
                        myThread.setOrderNumberBill(String.valueOf(orderNumber));
                        myThread.setCustomers(listDataCustomer);
                        myThread.setTableNumber(tableNumber);
                        myThread.setCustomer(customer);
                        myThread.setChangedPosition(String.valueOf(changedPosition));

                        myThread.setClientThread();
                        myThread.setRunBaby(true);
                    }


                }

            }
            else
            {

                if (oldModifiedProduct.getQuantityInt() - newModifiedProduct.getQuantityInt() == 0)
                {
                    printOrderDelete(changedPosition, customer);

                }
                else
                {
                    ArrayList<CashButtonLayout>                                newProducts  = new ArrayList<CashButtonLayout>();
                    HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                    //position is neded because in server method the modifiers are remapped on product using map key position
                    //so I set the new position using an int and incrementing it (maybe this could be done better?)

                    CashButtonLayout newButton = new CashButtonLayout();

                    //CashButtonLayout newbutton = new CashButtonLayout();
                    newButton.setTitle(listDataHeader.get(changedPosition).getTitle());
                    newButton.setPrice(listDataHeader.get(changedPosition).getPriceFloat());
                    newButton.setQuantity(oldModifiedProduct.getQuantityInt() - newModifiedProduct.getQuantityInt());
                    newButton.setProductId(listDataHeader.get(changedPosition).getID());
                    newButton.setIsDelete(false);
                    newButton.setModifyModifier(false);
                    newButton.setPosition(0);
                    newButton.setID(listDataHeader.get(changedPosition).getID());
                    newButton.setClientPosition(listDataHeader.get(changedPosition)
                                                              .getClientPosition());
                    newButton.setVat(listDataHeader.get(changedPosition).getVat());
                    newButton.setPrinterId(listDataHeader.get(changedPosition).getPrinterId());

                     /*newButton = listDataHeader.get(changedPosition);
                     newButton.setPosition(0);
                     newButton.setQuantity(oldModifiedProduct.getQuantityInt() - newModifiedProduct.getQuantityInt());
                     */
                    newProducts.add(newButton);
                    newModifiers.put(newButton, listDataChild.get(listDataHeader.get(changedPosition)));


                    Intent                                                 intent      = getActivity().getIntent();
                    int                                                    orderNumber = intent.getIntExtra("orderNumber", -1);
                    Map<CashButtonLayout, ArrayList<CashButtonListLayout>> b           = oldListDataChild;


                    if (StaticValue.blackbox)
                    {
                        boolean change = false;
                        if (newModifiedProduct.getQuantityInt() == null)
                        {
                            Log.i("cazzi", "cazzi");
                        }
                        int q = newModifiedProduct.getQuantityInt();
                        if (q == -1)
                        {
                            change = true;
                            newButton.setQuantity(1);
                        }
                        else
                        {
                            oldModifiedProduct.setQuantity(oldModifiedProduct.getQuantityInt() - newModifiedProduct
                                    .getQuantityInt());
                        }
                        Map<String, ArrayList<CashButtonListLayout>> test =
                                new HashMap<String, ArrayList<CashButtonListLayout>>();
                        //test.put(String.valueOf(0), listDataChild.get(listDataHeader.get(changedPosition)));
                        ArrayList<CashButtonListLayout> meineTest = new ArrayList<CashButtonListLayout>();
                        ArrayList<CashButtonListLayout> myListProva = new ArrayList<CashButtonListLayout>(listDataChild
                                .get(listDataHeader.get(changedPosition)));
                        //if(!change){
                        for (int i = 0; i < myListProva.size(); i++)
                        {
                            CashButtonListLayout list = new CashButtonListLayout();
                            list.setTitle(myListProva.get(i).getTitle());
                            list.setPrice(myListProva.get(i).getPriceFloat());
                            int a      = myListProva.get(i).getQuantityInt();
                            int b1     = listDataHeader.get(changedPosition).getQuantityInt();
                            int c      = oldModifiedProduct.getQuantityInt();
                            int result = (a / b1);
                            list.setQuantity(result);
                            list.setModifierId(myListProva.get(i).getModifierId());
                            list.setID(myListProva.get(i).getID());
                            list.setNote(myListProva.get(i).getNote());
                            list.setProdPosition(myListProva.get(i).getProdPosition());
                            list.setVat(myListProva.get(i).getVat());
                            meineTest.add(list);
                            //myListProva.get(i).setQuantity(myListProva.get(i).getQuantityInt()/(listDataHeader.get(changedPosition).getQuantityInt()+oldModifiedProduct.getQuantityInt()));
                        }
                        //}
                        test.put(String.valueOf(0), meineTest);

                        Map<String, ArrayList<CashButtonListLayout>> test1 =
                                new HashMap<String, ArrayList<CashButtonListLayout>>();

                        test1.put(String.valueOf(0), oldListDataChild.get(oldModifiedProduct));

                        RequestParam params      = new RequestParam();
                        Gson         gson        = new Gson();
                        String       newProduct  = gson.toJson(newButton);
                        String       oldProduct  = gson.toJson(oldModifiedProduct);
                        String       newMods     = gson.toJson(test);
                        String       oldMods     = gson.toJson(test1);
                        String       myCustomers = gson.toJson(listDataCustomer);
                        String       myCustomer  = gson.toJson(customer);

                        params.add("newProduct", newProduct);
                        params.add("oldProduct", oldProduct);
                        params.add("newModifiers", newMods);
                        params.add("oldModifiers", oldMods);
                        params.add("change", String.valueOf(change));
                        params.add("printType", String.valueOf(9));
                        params.add("billId", String.valueOf(billId));
                        params.add("deviceName", String.valueOf(deviceName));
                        params.add("orderNumber", String.valueOf(orderNumber));
                        params.add("tableNumber", String.valueOf(tableNumber));
                        params.add("customers", myCustomers);
                        params.add("changedPosition", String.valueOf(changedPosition));

                        ((Operative) context).callHttpHandler("/printOrderCorrection", params);
                    }
                    else
                    {

                        ClientThread myThread = ClientThread.getInstance();
                        myThread.setOldProducts(newButton);
                        myThread.setModifiers(newModifiers);
                        myThread.setOldModifiers(newModifiers);
                        myThread.setPrintType(9);
                        myThread.setIP(IP);
                        myThread.setBillId(String.valueOf(billId));
                        myThread.setDeviceName(deviceName);
                        myThread.setOrderNumberBill(String.valueOf(orderNumber));
                        myThread.setCustomer(customer);
                        myThread.setChangedPosition(String.valueOf(changedPosition));

                        myThread.setClientThread();
                        myThread.setRunBaby(true);
                    }
                }


            }

        }
        oldModifiedProduct = new CashButtonLayout();
        newModifiedProduct = new CashButtonLayout();
    }


    public void printOrderDelete(int changedPosition, Customer customer)
    {
        Intent intent      = getActivity().getIntent();
        int    orderNumber = intent.getIntExtra("orderNumber", -1);
        if (StaticValue.blackbox)
        {
            Map<String, ArrayList<CashButtonListLayout>> test =
                    new HashMap<String, ArrayList<CashButtonListLayout>>();
            test.put(String.valueOf(0), oldListDataChild.get(oldModifiedProduct));
            RequestParam params      = new RequestParam();
            Gson         gson        = new Gson();
            String       newProduct  = gson.toJson(oldModifiedProduct);
            String       mods        = gson.toJson(test);
            String       myCustomers = gson.toJson(listDataCustomer);
            String       myCustomer  = gson.toJson(customer);

            Operative op = (Operative) getActivity();
            params.add("username", op.getUser());
            params.add("oldProduct", newProduct);
            params.add("modifiers", mods);

            params.add("printType", String.valueOf(9));
            params.add("billId", String.valueOf(billId));
            params.add("deviceName", String.valueOf(deviceName));
            params.add("orderNumber", String.valueOf(orderNumber));
            params.add("tableNumber", String.valueOf(tableNumber));
            //params.add("customers", myCustomers);
            params.add("changedPosition", String.valueOf(changedPosition));

            ((Operative) context).callHttpHandler("/printOrderDelete", params);
            oldModifiedProduct = new CashButtonLayout();
            oldListDataChild   = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        }
        else
        {
            if (dbA.getTotalBillPrintedIndex(billId) > 0)
            {
                //position is neded because in server method the modifiers are remapped on product using map key position
                //so I set the new position using an int and incrementing it (maybe this could be done better?)

                ClientThread myThread = ClientThread.getInstance();
                myThread.setOldProducts(oldModifiedProduct);
                myThread.setModifiers(oldListDataChild);
                myThread.setPrintType(11);
                myThread.setIP(IP);
                myThread.setBillId(String.valueOf(billId));
                myThread.setDeviceName(deviceName);
                myThread.setOrderNumberBill(String.valueOf(orderNumber));
                myThread.setCustomer(customer);
                myThread.setChangedPosition(String.valueOf(changedPosition));

                myThread.setClientThread();
                myThread.setRunBaby(true);

                oldModifiedProduct = new CashButtonLayout();
                oldListDataChild   = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            }
        }
    }


    public void printSingleOrderBill(int billId, CashButtonLayout newProduct, ArrayList<CashButtonListLayout> newModifier, Customer customer)
    {
        if (customer.getDescription().equals(""))
        {
            ArrayList<Customer> newCustomer = new ArrayList<Customer>();

            ArrayList<CashButtonLayout>                                newProducts  = new ArrayList<CashButtonLayout>();
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            //position is neded because in server method the modifiers are remapped on product using map key position
            //so I set the new position using an int and incrementing it (maybe this could be done better?)
            newProducts.add(newProduct);
            newModifiers.put(newProduct, newModifier);
            Gson       gson        = new Gson();
            JSONObject combined    = new JSONObject();
            Intent     intent      = getActivity().getIntent();
            int        orderNumber = intent.getIntExtra("orderNumber", -1);

            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(newProducts);
            myThread.setModifiers(newModifiers);
            myThread.setPrintType(8);
            myThread.setIP(IP);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCustomer(customer);
            myThread.setIndexList(cashListIndex);
            myThread.setCustomers(newCustomer);
            myThread.setTableNumber(tableNumber);
            Room room = dbA.fetchRoomById(roomId);
            if (room.getId() > 0)
            {
                myThread.setRoomName(room.getName());
            }
            else
            {
                myThread.setRoomName("");
            }

            myThread.setClientThread();
            myThread.setRunBaby(true);


            // myClient.execute(combined.toString());
        }
        else
        {
            ArrayList<Customer> newCustomer = new ArrayList<Customer>();

            ArrayList<CashButtonLayout>                                newProducts  = new ArrayList<CashButtonLayout>();
            HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> newModifiers = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            //position is neded because in server method the modifiers are remapped on product using map key position
            //so I set the new position using an int and incrementing it (maybe this could be done better?)
            newProduct.setClientPosition(1);
            newProducts.add(newProduct);
            newModifiers.put(newProduct, newModifier);
            newCustomer.add(customer);

            Gson       gson     = new Gson();
            JSONObject combined = new JSONObject();
           /* ClientDelegate myClient = new ClientDelegate(8080);
            myClient.delegate = forClient;*/
            Intent intent      = getActivity().getIntent();
            int    orderNumber = intent.getIntExtra("orderNumber", -1);

            ClientThread myThread = ClientThread.getInstance();
            myThread.setProducts(newProducts);
            myThread.setModifiers(newModifiers);
            myThread.setPrintType(8);
            myThread.setIP(IP);
            myThread.setBillId(String.valueOf(billId));
            myThread.setDeviceName(deviceName);
            myThread.setOrderNumberBill(String.valueOf(orderNumber));
            myThread.setCustomer(customer);
            myThread.setIndexList(cashListIndex);
            myThread.setCustomers(listDataCustomer);
            myThread.setTableNumber(tableNumber);
            Room room = dbA.fetchRoomById(roomId);
            if (room.getId() > 0)
            {
                myThread.setRoomName(room.getName());
            }
            else
            {
                myThread.setRoomName("");
            }

            myThread.setClientThread();
            myThread.setRunBaby(true);


        }
    }


    /**
     * DISCOUNT AND ROUND SECTION
     *
     * @param discount
     */

    public void saveBillWithHomage(float discount)
    {
        if (listDataHeader.size() > 0)
        {
            //salvo i data header e i suoi subelementi
            Intent intent     = getActivity().getIntent();
            int    numberBill = intent.getIntExtra("orderNumber", 1);

            int billId = dbA.saveTotalBillForPayment(total, numberBill, listDataHeader.size());
            bill_id                   = billId;
            CashFragment.this.bill_id = billId;
            if (billId != 0)
            {
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    //salvo prodotto
                    int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader.get(i));
                    if (listDataCustomer.size() > 0)
                    {
                        Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                        .getClientPosition() - 1);
                        dbA.saveCustomerBillForPayment(c, prodId);
                    }
                    if (listDataChild.get(listDataHeader.get(i)) != null)
                    {
                        for (int j = 0; j < listDataChild.get(listDataHeader.get(i)).size(); j++)
                        {
                            //salvo tutti i figli
                            dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader
                                    .get(i)).get(j));
                        }
                    }
                    dbA.savePaidBillHomage(bill_id, discount);
                    dbA.updateTotalBillWithDiscount(discount, bill_id);
                }

                printOrderBill(billId);

                listDataHeader = new ArrayList<CashButtonLayout>();
                listDataChild  = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
                listAdapter    = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);
                expListView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
                activityCommunicator.deleteCurrentCash();

                ArrayList<TotalBill> paidBills = dbA.getBillsList("Select * from bill_total where paid=" + 1);

                long sessionTime = dbA.getLastClosing();
                int  number      = dbA.getMaxOrderId(sessionTime);

                intent.putExtra("paidBills", paidBills);
                if (number + 1 > numberBill + 1)
                {
                    intent.putExtra("orderNumber", (number + 1));
                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                    numberBillView.setText("#" + (number + 2));
                }
                else
                {
                    intent.putExtra("orderNumber", (numberBill + 1));
                    CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
                    numberBillView.setText("#" + (numberBill + 2));
                }
                CustomTextView numberTableSet = (CustomTextView) myself.findViewById(R.id.cash_table_number);
                String         setto          = "";
                numberTableSet.setText(setto);

                listDataCustomer     = new ArrayList<Customer>();
                currentCustomerArray = new ArrayList<Integer>();

                setModifyBar(false);
            }
        }
    }


    //ci vuole un metodo che tenga traccia di tutti gli sconti
    public void addDiscountToBill()
    {
        if (listDataHeader.size() > 0)
        {
            //salvo i data header e i suoi subelementi
            Intent intent     = getActivity().getIntent();
            int    numberBill = intent.getIntExtra("orderNumber", 1);
            int    billId     = dbA.checkIfExists("SELECT id FROM bill_total");
            bill_id                   = billId;
            CashFragment.this.bill_id = billId;
            if (billId <= 0)
            {
                for (int i = 0; i < listDataHeader.size(); i++)
                {
                    //salvo prodotto
                    int prodId = dbA.saveProductBillForPayment(i, billId, listDataHeader.get(i));
                    if (listDataCustomer.size() > 0)
                    {
                        Customer c = listDataCustomer.get(listDataHeader.get(i)
                                                                        .getClientPosition() - 1);
                        dbA.saveCustomerBillForPayment(c, prodId);
                    }
                    if (listDataChild.get(listDataHeader.get(i)) != null)
                    {
                        for (int j = 0; j < listDataChild.get(listDataHeader.get(i)).size(); j++)
                        {
                            //salvo tutti i figli
                            dbA.saveModifierBillForPayment(j, prodId, listDataChild.get(listDataHeader
                                    .get(i)).get(j));
                        }
                    }
                }
            }
        }
        dbA.updateDiscount(total, bill_id);
        int id = dbA.checkIfExists("SELECT id FROM bill_total_extra WHERE bill_total_id=" + billId);
        if (id == -11)
        {
            dbA.addDiscountToTable(totalDiscount, bill_id);
        }
        totalDiscount = 0;
    }


    //adds discount to global variable totalDiscount
    public void setRoundDiscount()
    {
        double remain   = total;
        float  perc     = (float) remain % 1;
        double newTotal = 0;
        if (perc == 0.0f)
        {
            newTotal = remain - 1;
            String txt = String.format("%.2f", roundDecimal(((float) newTotal), 2));//.replaceAll(",", ".");
            total = newTotal;
            viewTotal.setText(txt.replace(".", ","));
            totalDiscount += 1;
            if (dbA.checkIfDiscountExists(billId) < 0)
            {
                dbA.addDiscountToTable(1, billId);
            }
            else
            {
                dbA.updateBillExtra(billId, totalDiscount, totalDiscount);
            }
        }
        else
        {

            newTotal = remain - perc;
            String txt = String.format("%.2f", roundDecimal(((float) newTotal), 2));//.replaceAll(",", ".");
            total = newTotal;
            viewTotal.setText(txt.replace(".", ","));
            totalDiscount += perc;
            if (dbA.checkIfDiscountExists(billId) < 0)
            {
                dbA.addDiscountToTable(1, billId);
            }
            else
            {
                dbA.updateBillExtra(billId, totalDiscount, totalDiscount);
            }
        }
    }


    @Override
    public void onTaskEndWithResult(String success)
    {


    }


    @Override
    public void onTaskFinishGettingData(String result)
    {
        // Toast.makeText(context, "fine", Toast.LENGTH_SHORT).show();
    }


    public void openBillDecisionPopup(View view)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        Display        display    = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = context.getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels;// / density;
        float dpWidth  = outMetrics.widthPixels;// / density;

        final View popupView = layoutInflater.inflate(R.layout.popup_recent_orders, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) popupView.findViewById(R.id.recent_orders_window)
                                                                                 .getLayoutParams();
        /** 52 => footer height ; 31 => popupwindow height/2 **/
        int t = (int) ((int) (dpHeight - 52) / 2 - 62 * density);
        rll.setMargins(0, t, 0, 0);
        popupView.findViewById(R.id.recent_orders_window).setLayoutParams(rll);

        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.operative), 0, 0, 0);

        CustomButton delete  = (CustomButton) popupView.findViewById(R.id.first_button);
        CustomButton unpaid  = (CustomButton) popupView.findViewById(R.id.second_button);
        CustomButton partial = (CustomButton) popupView.findViewById(R.id.third_button);
        CustomButton cancel  = (CustomButton) popupView.findViewById(R.id.cancel_delete_button);

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (StaticValue.blackbox)
                {
                    myPopupWindow = popupWindow;
                    RequestParam params     = new RequestParam();
                    String       android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);
                    params.add("billId", String.valueOf(billId));
                    params.add("type", String.valueOf(3));
                    ((Operative) context).callHttpHandler("/payBillFromOperative", params);
                }
                else
                {
                    dbA.modifyTablePaymentInBill(billId, 3);
                    setOrderStatus();
                    popupWindow.dismiss();
                }
            }
        });

        unpaid.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (StaticValue.blackbox)
                {
                    myPopupWindow = popupWindow;
                    RequestParam params     = new RequestParam();
                    String       android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);
                    params.add("billId", String.valueOf(billId));
                    params.add("type", String.valueOf(4));
                    ((Operative) context).callHttpHandler("/payBillFromOperative", params);
                }
                else
                {
                    dbA.modifyTablePaymentInBill(billId, 4);
                    setOrderStatus();
                    popupWindow.dismiss();
                }
            }
        });

        partial.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (StaticValue.blackbox)
                {
                    myPopupWindow = popupWindow;
                    RequestParam params     = new RequestParam();
                    String       android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add("androidId", android_id);
                    params.add("billId", String.valueOf(billId));
                    params.add("type", String.valueOf(5));
                    ((Operative) context).callHttpHandler("/payBillFromOperative", params);
                }
                else
                {

                    dbA.modifyTablePaymentInBill(billId, 5);
                    setOrderStatus();
                    popupWindow.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupWindow.dismiss();
            }
        });


    }


    public void setOrderStatus()
    {
        Intent intent = getActivity().getIntent();
        paid = false;
        view.findViewById(R.id.cash_order)
            .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        view.findViewById(R.id.cash_order_number)
            .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        view.findViewById(R.id.cash_table)
            .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        CustomTextView tNumberLayout = (CustomTextView) view.findViewById(R.id.cash_table_not_set);
        tNumberLayout.setVisibility(View.VISIBLE);
        ((CustomTextView) view.findViewById(R.id.cash_table)).setVisibility(View.GONE);
        tNumber = (CustomTextView) view.findViewById(R.id.cash_table_number);
        if (tNumber != null)
        {
            tNumber.setVisibility(View.GONE);
        }
        view.findViewById(R.id.layout_1)
            .setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        int numberBill = intent.getIntExtra("orderNumber", -1);
        if (numberBill == -1)
        {
            numberBill = dbA.getMaxOrderId(TimerManager.getSessionTimeStart());
        }
        totalDiscount             = 0.0f;
        billId                    = -1;
        bill_id                   = -1;
        CashFragment.this.bill_id = billId;
        intent.putExtra("billId", billId);
        activityCommunicator = (ActivityCommunicator) getActivity();
        listDataCustomer     = new ArrayList<>();
        listDataHeader       = new ArrayList<CashButtonLayout>();
        listDataChild        = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        listAdapter          = new CashAdapter(getActivity(), listDataHeader, listDataChild, dbA, 0, listDataCustomer, paid);
        expListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        activityCommunicator.deleteCurrentCash();
        setModifyBar(false);
        total = 0.0;
        viewTotal.setText("0,00");

        //int newBillId = dbA.checkIfExists("Select * from bill_total where bill_number=" + numberBill);
        //boolean f = dbA.savePaidBill(null, newBillId, 0);
        ArrayList<TotalBill> paidBills = dbA.getBillsList("Select * from bill_total where paid=" + 1);

        long sessionTime = dbA.getLastClosing();
        int  number      = dbA.getMaxOrderId(sessionTime);
        intent.putExtra("paidBills", paidBills);
        if (number + 1 > numberBill + 1)
        {
            intent.putExtra("orderNumber", (number + 1));
            ((Operative) context).setCashFragmentValues(billId, number + 1);
            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
            numberBillView.setText("#" + (number + 2));
        }
        else
        {
            intent.putExtra("orderNumber", (numberBill + 1));
            ((Operative) context).setCashFragmentValues(billId, numberBill + 1);
            CustomTextView numberBillView = (CustomTextView) myself.findViewById(R.id.cash_order_number);
            numberBillView.setText("#" + (numberBill + 2));
        }
        mailSelected = false;
        email        = "";
        intent.putExtra("email", email);
    }
}
