package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.fragments.CalculatorFragment;
import com.example.blackbox.fragments.OrderFragment;
import com.example.blackbox.fragments.PaymentActivityCommunicator;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.CustomerView;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.example.blackbox.model.TemporaryOrder;
import com.example.blackbox.model.TotalBill;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.view.View.GONE;


public class OrderListAdapter extends BaseExpandableListAdapter
{

    public static final int DEFAULT_MODE                = 0;
    public static final int NUMBER_MODE                 = 1;
    public static final int ITEM_MODE                   = 2;
    public static final int PERSON_MODE                 = 3;
    public static final int PERCENTAGE_MODE             = 4;
    public static final int PAY_PARTIAL_MODE            = 5;
    public static final int PAY_TOTAL_MODE              = 6;
    public static final int TIPS_MODE                   = 7;
    public static final int HOMAGE_MODE                 = 8;
    public static final int DISCOUNT_MODE               = 9;
    public static final int ADD_DISCOUNT                = 10;
    public static final int TOTAL_DISCOUNT_MODE         = 11;
    public static final int PAY_TICKET_MODE             = 12;
    public static final int INSERT_CREDIT_MODE          = 13;
    public static final int PARTIAL_TOTAL_DISCOUNT_MODE = 14;
    public static final int PARTIAL_MODE                = 15;
    public static final int MODIFY_DISCOUNT_MODE        = 16;
    public static final int TOTAL_MODIFY_DISCOUNT_MODE  = 17;
    public static final int ELEMENT_ITEM_SPLIT          = 18;
    public static final int PAY_LEFT_PAYMENT            = 19;
    public static final int INSERT_FIDELITY_MODE        = 20;
    private final float                                                  density;
    public        boolean                                                isSplit         = false;
    public        Float                                                  discountRemain  = 0.0F;
    public        int                                                    billId;
    public ArrayList<CustomerView> customerViews = new ArrayList<CustomerView>();
    public boolean billClose = false;
    protected     ArrayList<CashButtonLayout>                            groups;
    protected     Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map;
    private       ArrayList<Customer>                                    customers;
    private       ArrayList<CashButtonLayout>                            groups_backup;
    private       ArrayList<CashButtonLayout>                            groups_original;
    private       HashMap<CashButtonLayout, Integer>                     groups_total_selected_quantity;
    private       HashMap<CashButtonLayout, Integer>                     partial_not_yet_saved_quantity;
    private       Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map_backup;
    private       Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map_original;
    private       Map<CashButtonLayout, Integer>                         split_qty_map;
    private       HashMap<CashButtonLayout, Integer>                     homage_list;
    private       HashMap<CashButtonLayout, Integer>                     discount_list;
    private       DatabaseAdapter                                        dbA;
    private       Context                                                context;
    private       Float                                                  total_cost;
    private       Float                                                  partial_cost;
    private       Float                                                  left_cost;
    private       int                                                    mode;
    private       OrderFragment                                          myFragment;
    private       PaymentActivityCommunicator                            communicator;
    private       SubdivisionItem                                        subdivisionItem = null;


    public OrderListAdapter(int bill_id, DatabaseAdapter dbA, Context context)
    {
        ArrayList<CashButtonLayout> pp = TemporaryOrder.getProducts();
        this.dbA     = dbA;
        this.context = context;
        communicator = (PaymentActivityCommunicator) context;
        this.billId  = bill_id;

        if (StaticValue.blackbox)
        {
            customers = new ArrayList<Customer>();

            map          = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
            map_original = map;

            groups = new ArrayList<>(map.keySet());

            groups_original                = groups;
            groups_total_selected_quantity = new HashMap<>();
            partial_not_yet_saved_quantity = new HashMap<>();
            homage_list                    = new HashMap<>();
            discount_list                  = new HashMap<>();
            for (CashButtonLayout p : groups)
            {
                partial_not_yet_saved_quantity.put(p, 0);
                groups_total_selected_quantity.put(p, 0);
                homage_list.put(p, 0);
                discount_list.put(p, 0);
            }
            myFragment = (OrderFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("order");
            total_cost = 0.0f;
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
            ((PaymentActivity) context).callHttpHandler("/getBillDataPayment", params);
        }
        else
        {
            customers = dbA.getCustomerData(bill_id);

            map          = dbA.getBillData2(bill_id, context);
            map_original = map;

            groups = new ArrayList<>(map.keySet());

            groups_original                = groups;
            groups_total_selected_quantity = new HashMap<>();
            partial_not_yet_saved_quantity = new HashMap<>();
            homage_list                    = new HashMap<>();
            discount_list                  = new HashMap<>();
            for (CashButtonLayout p : groups)
            {
                partial_not_yet_saved_quantity.put(p, 0);
                groups_total_selected_quantity.put(p, 0);
                homage_list.put(p, 0);
                discount_list.put(p, 0);
            }
            myFragment = (OrderFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("order");
            total_cost = 0.0f;

            // Calculating total bill cost
            Collections.sort(groups);
            for (CashButtonLayout prod : groups)
            {
                if (prod.getPercentage() == 1)
                {
                    ArrayList<CashButtonListLayout> mods = map.get(prod);
                    for (CashButtonListLayout mod : mods)
                    {
                        total_cost += mod.getPriceFloat() * mod.getQuantityInt();
                    }
                    total_cost += (prod.getPriceFloat() * (prod.getQuantityInt() - prod.getHomage()));
                }
            }

            Float discount = dbA.getBillDiscountPrice(bill_id);
            left_cost = total_cost - discount;

            communicator.setProducts(groups);
            communicator.setModifiers(map);
        }

        Display        display    = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = context.getResources().getDisplayMetrics().density;


    }


    public void orderGroups()
    {
        Collections.sort(groups);
    }


    public float getLeftCost()
    {
        return left_cost;
    }


    public void setLeftCost(float c)
    {
        left_cost = c;
    }


    public ArrayList<CashButtonLayout> getProducts()
    {
        return groups;
    }


    public ArrayList<CashButtonLayout> getCustomerProducts(CashButtonLayout product)
    {
        ArrayList<CashButtonLayout> toReturn = new ArrayList<CashButtonLayout>();
        for (int i = 0; i < groups.size(); i++)
        {
            if (groups.get(i).getClientPosition() == product.getClientPosition())
            {
                toReturn.add(groups.get(i));
            }
        }

        return toReturn;
    }


    public Customer getCustomer(int customerPosition)
    {
        Customer customer = customers.get(customerPosition - 1);
        return customer;
    }


    public ArrayList<Customer> getCustomers()
    {
        return customers;
    }


    public int getCustomerSize()
    {
        return customers.size();
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getModifier()
    {
        return map;
    }


    public void setBillClose(boolean b)
    {
        billClose = b;
    }


    public void setBillDataFromServer(
            TotalBill totals,
            ArrayList<Customer> myCustomers,
            ArrayList<CashButtonLayout> myGroups,
            Map<CashButtonLayout, ArrayList<CashButtonListLayout>> myMap)
    {
        customers = myCustomers;

        map          = myMap;
        map_original = map;

        groups = myGroups;

        groups_original                = groups;
        groups_total_selected_quantity = new HashMap<>();
        partial_not_yet_saved_quantity = new HashMap<>();
        homage_list                    = new HashMap<>();
        discount_list                  = new HashMap<>();
        for (CashButtonLayout p : groups)
        {
            partial_not_yet_saved_quantity.put(p, 0);
            groups_total_selected_quantity.put(p, 0);
            homage_list.put(p, 0);
            discount_list.put(p, 0);
        }
        myFragment = (OrderFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("order");
        total_cost = totals.getTotal();

        //TODO crete discount class
        Float discount = 0.0f;
        left_cost = total_cost - discount;

        communicator.setProducts(groups);
        communicator.setModifiers(map);
    }


    public int returnFirstVisible()
    {
        int firstVisible = 0;
        for (int i = 0; i < groups.size(); i++)
        {
            if (!groups.get(i).isSelected())
            {
                firstVisible = i;
                break;
            }
        }
        return firstVisible;
    }


    @Override
    public int getGroupCount()
    {
        if (groups != null)
        {
            return groups.size();
        }
        else
        {
            return 0;
        }
    }


    @Override
    public int getChildrenCount(int groupPosition)
    {
        CashButtonLayout                group     = groups.get(groupPosition);
        ArrayList<CashButtonListLayout> modifiers = map.get(group);
        return modifiers != null ? modifiers.size() : 0;
    }


    @Override
    public Object getGroup(int groupPosition)
    {
        return groups.get(groupPosition);
    }


    public CashButtonLayout getElement(int groupPosition)
    {
        return groups.get(groupPosition);
    }


    public float getGroupsDiscount()
    {
        float itemsD = 0.0f;
        for (CashButtonLayout g : groups)
        {
            itemsD += g.getDiscount();
        }
        return itemsD;
    }


    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        CashButtonLayout                group     = groups.get(groupPosition);
        ArrayList<CashButtonListLayout> modifiers = map.get(group);
        return modifiers.get(childPosition);
    }


    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }


    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }


    @Override
    public boolean hasStableIds()
    {
        return true;
    }


    public float getElementDiscount(int groupPosition)
    {
        CashButtonLayout product  = groups.get(groupPosition);
        Float            discount = product.getDiscount();
        if (discount == null)
        {
            discount = 0.0f;
        }
        return discount;

    }


    public float getElementsHomage()
    {
        float homage = 0.0f;
        for (int i = 0; i < groups.size(); i++)
        {
            if (groups.get(i).getHomage() == 1)
            {
                homage = homage + groups.get(i).getPriceFloat();
            }
        }
        return homage;

    }


    public boolean checkIfAllSelected()
    {
        boolean check = true;
        for (int i = 0; i < groups.size(); i++)
        {
            if (groups.get(i).isSelected())
            {
                check = false;
            }
        }

        return check;
    }


    public int returnQuantity(CashButtonLayout product)
    {
        int     qty;
        int     homageQty   = 0;
        Integer pnysq       = 0;
        int     discountQty = 0;
        if (homage_list.containsKey(product))
        {
            homageQty = homage_list.get(product);
        }
        else if (product.getOriginalCBL() != null)
        {
            if (homage_list.containsKey(product.getOriginalCBL()))
            {
                homageQty = homage_list.get(product.getOriginalCBL());
            }
        }
        if (discount_list.containsKey(product))
        {
            discountQty = discount_list.get(product);
        }
        else if (product.getOriginalCBL() != null)
        {
            if (discount_list.containsKey(product.getOriginalCBL()))
            {
                discountQty = discount_list.get(product.getOriginalCBL());
            }
        }
        if (partial_not_yet_saved_quantity.containsKey(product))
        {
            pnysq = partial_not_yet_saved_quantity.get(product);
        }
        if (split_qty_map == null)
        {
            qty = (product.getQuantityInt()) - pnysq - homageQty/*-discountQty*/;
        }
        else
        {
            if (split_qty_map.containsKey(product))
            {
                int a1 = split_qty_map.get(product);
                int b1 = product.getHomage();
                if (partial_not_yet_saved_quantity.containsKey(product))
                {
                    pnysq = partial_not_yet_saved_quantity.get(product);
                }
                qty = split_qty_map.get(product) - homageQty - pnysq;
            }
            else
            {
                qty = 1;
            }
        }
        return qty;
    }


    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        ArrayList<CashButtonLayout> a       = groups;
        CashButtonLayout            product = groups.get(groupPosition);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.element_gridview_cash,
                    null
            );
        }


        CustomTextView title    = (CustomTextView) convertView.findViewById(R.id.cash_title);
        CustomTextView quantity = (CustomTextView) convertView.findViewById(R.id.cash_quantity);
        CustomTextView price    = (CustomTextView) convertView.findViewById(R.id.cash_total);

        /*if (!billClose) {*/
        title.setSelected(product.isSelected());
        price.setSelected(product.isSelected());
        /*}*/
        title.setText(product.getTitle());


        int     qty;
        int     homageQty   = 0;
        Integer pnysq       = 0;
        int     discountQty = 0;
        if (homage_list.containsKey(product))
        {
            homageQty = homage_list.get(product);
        }
        else if (product.getOriginalCBL() != null)
        {
            if (homage_list.containsKey(product.getOriginalCBL()))
            {
                homageQty = homage_list.get(product.getOriginalCBL());
            }
        }
        if (discount_list.containsKey(product))
        {
            discountQty = discount_list.get(product);
        }
        else if (product.getOriginalCBL() != null)
        {
            if (discount_list.containsKey(product.getOriginalCBL()))
            {
                discountQty = discount_list.get(product.getOriginalCBL());
            }
        }
        if (partial_not_yet_saved_quantity.containsKey(product))
        {
            pnysq = partial_not_yet_saved_quantity.get(product);
        }
        //not sure of this for division
        //I added getHomage whic return int of homage quantity
        if (split_qty_map == null)
        {
            qty = (product.getQuantityInt()) - pnysq - homageQty/*-discountQty*/;
        }
        else
        {
            if (split_qty_map.containsKey(product))
            {
                int a1 = split_qty_map.get(product);
                int b1 = product.getHomage();
                //qty = split_qty_map.get(product) - product.getHomage();
                /*if(product.getHomage()==0) {
                    if (homage_list.containsKey(product)) homageQty = homage_list.get(product);
                    else homageQty = 0;
                }
*/
                if (partial_not_yet_saved_quantity.containsKey(product))
                {
                    pnysq = partial_not_yet_saved_quantity.get(product);
                }
                qty = split_qty_map.get(product) - homageQty - pnysq;
            }
            else
            {
                //qty = 0;
                qty = 1;
            }
        }


        quantity.setText(Integer.toString(qty));


        Float discount = product.getDiscount();
        if (discount == null)
        {
            discount = 0.0f;
        }
        if (discount >= (product.getPriceFloat() * qty))
        {
            String txt = String.format("%.2f", 0.0f).replace(".", ",");
            //.replace(",", ".");
            price.setText(txt);
            if (product.getPriceFloat() > 0.0f)
            {
                quantity.setText(Integer.toString(0));
            }
            discountRemain = discount - (product.getPriceFloat() * qty);
        }
        else
        {

            Float  p   = CalculatorFragment.roundDecimal(((product.getPriceFloat() * qty) - discount), 2);
            String txt = String.format("%.2f", p).replace(".", ",");
            //.replace(",", ".");

            price.setText(txt);
        }
        ImageButton discountContainer = (ImageButton) convertView.findViewById(R.id.cash_discount_icon);

        if (discount > 0.0f || product.getHomage() != 0)
        {
            discountContainer.setVisibility(View.VISIBLE);
            discountContainer.setBackground(context.getDrawable(R.drawable.selected_item));
            discountContainer.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    int     a1 = ((PaymentActivity) context).getMode();
                    boolean b  = !((PaymentActivity) context).checkIfTotalHomage();
                    if (((PaymentActivity) context).getMode() == 0 && !((PaymentActivity) context).checkIfTotalHomage())
                    {
                        ((PaymentActivity) context).openModifyPopup(groupPosition, v);
                    }
                }
            });

        }
        else
        {
            discountContainer.setBackground(context.getDrawable(R.drawable.selected_item));
            discountContainer.setVisibility(View.GONE);
        }

        View groupDivider = (View) convertView.findViewById(R.id.group_divider_hline);
        if (groupPosition != 0)
        {

            if (groupPosition == returnFirstVisible())
            {
                groupDivider.setVisibility(GONE);
            }
            else
            {
                groupDivider.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            groupDivider.setVisibility(View.GONE);
        }

        if (product.isSelected() /*&& billClose*/)
        {
            LinearLayout linear = (LinearLayout) convertView.findViewById(R.id.cash_group_row);
            // linear.setVisibility(GONE);
            linear.setAlpha(75);
            linear.setBackgroundColor(Color.GRAY);
            groupDivider.setVisibility(View.GONE);

        }
        else
        {
            LinearLayout linear = (LinearLayout) convertView.findViewById(R.id.cash_group_row);
            linear.setVisibility(View.VISIBLE);
            if (groupPosition != 0)
            {
                if (groupPosition == returnFirstVisible())
                {
                    groupDivider.setVisibility(GONE);
                }
                else
                {
                    groupDivider.setVisibility(View.VISIBLE);
                }
            }

        }

        price.getLayoutParams().width = (int) (1 * density);
        if (split_qty_map == null)
        {
            if (product.getHomage() != 0)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins((int) -(30 * density), 0, 0, 0);
                price.setLayoutParams(params);
                price.getLayoutParams().width  = (int) (101 * density);
                price.getLayoutParams().height = (int) (36 * density);
                price.setText(R.string.gratis_all_caps);
                RelativeLayout discountC = (RelativeLayout) convertView.findViewById(R.id.cash_discount_icon_container);
                discountC.setVisibility(View.VISIBLE);

                quantity.setText("1");
            }
            else
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 0);
                price.setLayoutParams(params);
                price.getLayoutParams().width = (int) (71 * density);
                RelativeLayout discountC = (RelativeLayout) convertView.findViewById(R.id.cash_discount_icon_container);
                discountC.setVisibility(View.VISIBLE);

            }
        }
        else
        {
            if (product.getHomage() != 0)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins((int) -(30 * density), 0, 0, 0);
                price.setLayoutParams(params);
                price.getLayoutParams().width  = (int) (101 * density);
                price.getLayoutParams().height = (int) (36 * density);
                price.setText(R.string.gratis_all_caps);
                RelativeLayout discountC = (RelativeLayout) convertView.findViewById(R.id.cash_discount_icon_container);
                discountC.setVisibility(View.VISIBLE);
                quantity.setText("1");
            }
            else
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 0);
                price.getLayoutParams().width = (int) (71 * density);
                RelativeLayout discountC = (RelativeLayout) convertView.findViewById(R.id.cash_discount_icon_container);
                discountC.setVisibility(View.VISIBLE);

            }
        }

        //else groupDivider.setVisibility(View.GONE);
        convertView.setTag(product);

        LinearLayout   productRow             = (LinearLayout) convertView.findViewById(R.id.cash_group_row);
        View           finalConvertView       = convertView;
        RelativeLayout discountContainerTotal = (RelativeLayout) convertView.findViewById(R.id.cash_discount_icon_container);
        if (((PaymentActivity) context).getTempPositionDiscount() != groupPosition)
        {
            title.setBackground(context.getDrawable(R.drawable.selected_item));
            // price.setBackground(context.getDrawable(R.drawable.selected_item));
            discountContainerTotal.setBackground(context.getDrawable(R.drawable.selected_item));
            productRow.setBackground(context.getDrawable(R.drawable.selected_item));
            productRow.setActivated(false);
        }
        else
        {
            discountContainerTotal.setBackground(context.getDrawable(R.drawable.selected_item_1));
            discountContainerTotal.setActivated(true);
            productRow.setBackground(context.getDrawable(R.drawable.selected_item_1));
            title.setBackground(context.getDrawable(R.drawable.selected_item_1));
            // price.setBackground(context.getDrawable(R.drawable.selected_item_1));
            //title.setActivated(true);
            productRow.setActivated(true);
        }

        productRow.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (((PaymentActivity) context).getMode() == 3)
                //CLICk PER ITEM ON ITEM
                {
                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);
                }
                else if (((PaymentActivity) context).getMode() == 2)
                //CLICK PER PERSON ON ITEM
                {
                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                }
                else if (((PaymentActivity) context).getMode() == 9)
                {
                    //CLICK DISCOUNT FOR ITEM
                    if (((PaymentActivity) context).checkSelectedItem())
                    {
                        ((PaymentActivity) context).setTempPositionDiscount(groupPosition);

                        ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 9);

                      /*  discountContainerTotal.setBackground(context.getDrawable(R.drawable.selected_item_1));
                        discountContainerTotal.setActivated(true);
                        productRow.setBackground(context.getDrawable(R.drawable.selected_item_1));
                        title.setBackground(context.getDrawable(R.drawable.selected_item_1));
                        price.setBackground(context.getDrawable(R.drawable.selected_item_1));
                        //title.setActivated(true);
                        productRow.setActivated(true);*/
                        notifyDataSetChanged();
                    }
                }

                else if (((PaymentActivity) context).getMode() == 8)
                {
                    //homage mode -> now split element item
                    if (product.getHomage() == 0 && product.getDiscount() == 0.0f)
                    {
                        if (product.getPercentage() == 1)
                        {
                            ((PaymentActivity) context).setTempPositionDiscount(groupPosition);
                            ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 8);
                            notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(context, R.string.splitting_the_split_is_not_allowed, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(context, R.string.please_split_before_giving_discount, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


        LinearLayout customer = (LinearLayout) convertView.findViewById(R.id.cash_customer_row);


        if (customers.size() > 0)
        {
            if (product.isSelected()/* && !billClose*/)
            {
                if (groupPosition == 0)
                {
                    if (checkIfClientIsVisible(product))
                    {
                        customer.setVisibility(View.GONE);
                    }
                    else
                    {
                        customer.setVisibility(View.VISIBLE);
                        if (!checkIfCustomersViewContainesView(groupPosition))
                        {
                            CustomerView c = new CustomerView(finalConvertView, groupPosition, 1);
                            customerViews.add(c);
                        }
                        customer.setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                //CLICK PER PERSON ON COSTUMER
                                if (((PaymentActivity) context).getMode() != 2)
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 1);
                                }
                            }
                        });
                        productRow.setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                if ((((PaymentActivity) context).getMode() == 2))
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);
                                }
                                else if ((((PaymentActivity) context).getMode() == 8))
                                {
                                    if (product.getPercentage() == 1)
                                    {
                                        ((PaymentActivity) context).setTempPositionDiscount(groupPosition);
                                        ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 8);
                                        notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        Toast.makeText(context, R.string.splitting_the_split_is_not_allowed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                }

                               /* if (((PaymentActivity) context).getMode() != 2)

                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                else
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);*/

                            }
                        });
                        CustomTextView customerText = (CustomTextView) convertView.findViewById(R.id.cash_user_text);
                        Customer       cost         = customers.get(product.getClientPosition() - 1);
                        if (cost != null)
                        {
                            customer.setBackgroundColor(ContextCompat.getColor(context, R.color.eletric_blue));
                            customerText.setText(cost.getDescription());
                        }
                    }

                }
                else
                {
                    if (groups.get(groupPosition - 1).getClientPosition() != product.getClientPosition())
                    {
                        //quello prima è di un altro customer
                        if (checkIfClientIsVisible(product))
                        {
                            customer.setVisibility(View.GONE);
                        }
                        else
                        {
                            customer.setVisibility(View.VISIBLE);
                            if (!checkIfCustomersViewContainesView(groupPosition))
                            {
                                CustomerView c = new CustomerView(finalConvertView, groupPosition, 1);
                                customerViews.add(c);
                            }
                            customer.setOnClickListener(new View.OnClickListener()
                            {
                                public void onClick(View v)
                                {
                                    //CLICK PER PERSON ON COSTUMER
                                    if (((PaymentActivity) context).getMode() != 2)
                                    {
                                        ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 1);
                                    }
                                }
                            });
                            productRow.setOnClickListener(new View.OnClickListener()
                            {
                                public void onClick(View v)
                                {
                                    if ((((PaymentActivity) context).getMode() == 2))
                                    {
                                        ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);
                                    }
                                    else if ((((PaymentActivity) context).getMode() == 8))
                                    {
                                        if (product.getPercentage() == 1)
                                        {
                                            ((PaymentActivity) context).setTempPositionDiscount(groupPosition);
                                            ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 8);
                                            notifyDataSetChanged();
                                        }
                                        else
                                        {
                                            Toast.makeText(context, R.string.splitting_the_split_is_not_allowed, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else
                                    {
                                        ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                    }

                               /* if (((PaymentActivity) context).getMode() != 2)

                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                else
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);*/

                                }
                            });
                            CustomTextView customerText = (CustomTextView) convertView.findViewById(R.id.cash_user_text);
                            Customer       cost         = customers.get(product.getClientPosition() - 1);
                            if (cost != null)
                            {
                                customer.setBackgroundColor(ContextCompat.getColor(context, R.color.eletric_blue));
                                customerText.setText(cost.getDescription());
                            }
                        }
                    }
                    else
                    {
                        //è dello stesso customer
                        customer.setVisibility(GONE);

                    }
                    //customer.setVisibility(GONE);
                }

            }
            else
            {
                if (groupPosition == 0)
                {
                    if (product.getClientPosition() != 0)
                    {
                        customer.setVisibility(View.VISIBLE);
                        if (!checkIfCustomersViewContainesView(groupPosition))
                        {
                            CustomerView c = new CustomerView(finalConvertView, groupPosition, 1);
                            customerViews.add(c);
                        }
                        customer.setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                //CLICK PER PERSON ON COSTUMER
                                if (((PaymentActivity) context).getMode() != 2)
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 1);
                                }
                            }
                        });
                        productRow.setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                if ((((PaymentActivity) context).getMode() == 2))
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);
                                }
                                else if ((((PaymentActivity) context).getMode() == 8))
                                {
                                    if (product.getPercentage() == 1)
                                    {
                                        ((PaymentActivity) context).setTempPositionDiscount(groupPosition);
                                        ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 8);
                                        notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        Toast.makeText(context, R.string.splitting_the_split_is_not_allowed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                }

                               /* if (((PaymentActivity) context).getMode() != 2)

                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                else
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);*/

                            }
                        });
                        CustomTextView customerText = (CustomTextView) convertView.findViewById(R.id.cash_user_text);
                        Customer       cost         = customers.get(product.getClientPosition() - 1);
                        if (cost != null)
                        {
                            customer.setBackgroundColor(ContextCompat.getColor(context, R.color.eletric_blue));
                            customerText.setText(cost.getDescription());
                        }
                    }
                }
                else
                {
                    CashButtonLayout productBefore = (CashButtonLayout) getGroup(groupPosition - 1);
                    if (productBefore.getClientPosition() == product.getClientPosition())
                    {
                        customer.setVisibility(View.GONE);
                    }
                    else
                    {
                        customer.setVisibility(View.VISIBLE);
                        if (!checkIfCustomersViewContainesView(groupPosition))
                        {
                            CustomerView c = new CustomerView(finalConvertView, groupPosition, 1);
                            customerViews.add(c);
                        }
                        customer.setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                if (((PaymentActivity) context).getMode() != 2)
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 1);
                                }
                            }
                        });
                        productRow.setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                if ((((PaymentActivity) context).getMode() == 2))
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);
                                }
                                else if ((((PaymentActivity) context).getMode() == 8))
                                {
                                    if (product.getPercentage() == 1)
                                    {
                                        ((PaymentActivity) context).setTempPositionDiscount(groupPosition);
                                        ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 8);
                                        notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        Toast.makeText(context, R.string.splitting_the_split_is_not_allowed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                }

                               /* if (((PaymentActivity) context).getMode() != 2)
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 2);
                                else
                                    ((PaymentActivity) context).doSomething(finalConvertView, groupPosition, 0);*/
                            }
                        });
                        CustomTextView customerText = (CustomTextView) convertView.findViewById(R.id.cash_user_text);
                        Customer       cost         = customers.get(product.getClientPosition() - 1);
                        if (cost != null)
                        {
                            customer.setBackgroundColor(ContextCompat.getColor(context, R.color.eletric_blue));
                            customerText.setText(cost.getDescription());
                        }
                    }


                }
            }
        }

        return convertView;
    }


    public boolean checkIfCustomersViewContainesView(int groupPosition)
    {
        if (customerViews.size() > 0)
        {
            for (CustomerView c : customerViews)
            {
                if (c.getPosition() == groupPosition)
                {
                    return true;
                }
            }
        }

        return false;
    }


    public void perfromClicksOnCustomer()
    {


        /*for(CustomerView c : customerViews){
            ((PaymentActivity) context).setPersonClick(c.getView(), c.getPosition(), 1);
            ((PaymentActivity) context).addCustomersMethod();
            ((PaymentActivity) context).setPersonMode();
            mode = PERSON_MODE;

            break;
        }
        customerViews = new ArrayList<>();
        notifyDataSetInvalidated();
        this.notifyDataSetChanged();*/


    }


    public int firstCustomerElement(CashButtonLayout button)
    {
        int returnTo = 100000000;
        for (int i = 0; i < groups.size(); i++)
        {
            if (groups.get(i).getClientPosition() == button.getClientPosition())
            {
                if (groups.get(i).getPosition() < returnTo)
                {
                    returnTo = groups.get(i).getPosition();
                }
            }
        }
        return returnTo;
    }


    //ritorna true se almeno uno degli elementi non è selezionato
    public boolean checkIfClientIsVisible1(CashButtonLayout button)
    {
        boolean check = false;
        for (int i = 0; i < groups.size(); i++)
        {
            if (groups.get(i).getClientPosition() == button.getClientPosition())
            {
                if (!groups.get(i).isSelected())
                {
                    check = true;
                }
            }
        }
        return check;
    }


    public boolean checkIfClientIsVisible(CashButtonLayout button)
    {
        boolean check = true;
        for (int i = 0; i < groups.size(); i++)
        {
            if (groups.get(i).getClientPosition() == button.getClientPosition())
            {
                if (groups.get(i).isSelected())
                {
                    check = true;
                }
                else
                {
                    check = false;
                }
            }
        }
        return check;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        CashButtonLayout     product  = groups.get(groupPosition);
        CashButtonListLayout modifier = (CashButtonListLayout) getChild(groupPosition, childPosition);
        LayoutInflater       inflater = ((Activity) context).getLayoutInflater();

        if (product.isSelected())
        {
            convertView = inflater.inflate(R.layout.element_gridview_cash_modifier_null, null);
        }

        else
        {
            if (product.getPercentage() == 1)
            {
                //if (convertView == null) {
                convertView = inflater.inflate(R.layout.element_gridview_cash_modifier, null);
                // }
                if (product.getPercentage() == 1)
                {
                    if (modifier.getModifierId() != -15)
                    {
                        CustomTextView title    = (CustomTextView) convertView.findViewById(R.id.cash_modifier_title);
                        CustomTextView quantity = (CustomTextView) convertView.findViewById(R.id.cash_modifier_quantity);
                        CustomTextView price    = (CustomTextView) convertView.findViewById(R.id.cash_modifier_price);

                        title.setText(modifier.getTitle());

                        int qty = 0;
                        if (split_qty_map == null)
                        {
                            Integer gtsq = groups_total_selected_quantity.get(product);
                            if (gtsq == null)
                            {
                                gtsq = 0;
                            }
                            Integer pnysq = partial_not_yet_saved_quantity.get(product);
                            if (pnysq == null)
                            {
                                pnysq = 0;
                            }
                            //if(product.getQuantityInt()-product.getHomage()-gtsq-pnysq==0){
                            if (product.getQuantityInt() - gtsq - pnysq == 0)
                            {
                                qty = 0;
                            }
                            else
                            {
                                int division = modifier.getQuantityInt() / product.getQuantityInt();
                                qty = modifier.getQuantityInt() - gtsq * division - pnysq * division;
                            }

                        }

                        else
                        {
                            if (split_qty_map.containsKey(product))
                            {
                                Integer a        = split_qty_map.get(product) - product.getHomage();
                                int     division = modifier.getQuantityInt() / product.getQuantityInt();
                                qty = a * division;
                            }
                        }

                        quantity.setText(Integer.toString(qty));//modifier.getQuantity());
                        if (product.getDiscount() != null)
                        {
                            if (product.getDiscount() > 0.0f)
                            {
                                if (discountRemain > 0)
                                {
                                    if (discountRemain >= modifier.getPriceFloat() * qty)
                                    {
                                        String txt = String.format("%.2f", 0.0f).replace(".", ",");
                                        price.setText(txt);
                                        discountRemain = discountRemain - (modifier.getPriceFloat() * qty);
                                        quantity.setText(Integer.toString(0));//modifier.getQuantity());
                                    }

                                    else
                                    {
                                        Float  p   = CalculatorFragment.roundDecimal((modifier.getPriceFloat() * qty) - discountRemain, 2);
                                        String txt = String.format("%.2f", p).replace(".", ",");//.replace(",", ".");
                                        price.setText(txt);
                                        //price.setText("" + CalculatorFragment.roundDecimal((modifier.getPriceFloat() * qty) - discountRemain, 2));
                                        discountRemain = 0.0f;
                                    }
                                }

                                else
                                {
                                    Float  p   = CalculatorFragment.roundDecimal(modifier.getPriceFloat() * qty, 2);
                                    String txt = String.format("%.2f", p).replace(".", ",");//.replace(",", ".");
                                    price.setText(txt);
                                }

                            }

                            else
                            {
                                Float  p   = CalculatorFragment.roundDecimal(modifier.getPriceFloat() * qty, 2);
                                String txt = String.format("%.2f", p).replace(".", ",");//.replace(",", ".");
                                price.setText(txt);
                            }
                        }

                        else
                        {
                            Float  p   = CalculatorFragment.roundDecimal(modifier.getPriceFloat() * qty, 2);
                            String txt = String.format("%.2f", p).replace(".", ",");//.replace(",", ".");
                            price.setText(txt);
                            //price.setText("" + CalculatorFragment.roundDecimal(modifier.getPriceFloat() * qty, 2));
                        }

                        if (product.isSelected())
                        {
                            RelativeLayout linear = (RelativeLayout) convertView.findViewById(R.id.cash_modifier_container);
                            linear.setVisibility(GONE);
                        }
                        convertView.setTag(modifier);

                    }

                    else
                    {
                        RelativeLayout linear = (RelativeLayout) convertView.findViewById(R.id.cash_modifier_container);
                        linear.setVisibility(GONE);
                        CustomTextView title    = (CustomTextView) convertView.findViewById(R.id.cash_modifier_title);
                        CustomTextView quantity = (CustomTextView) convertView.findViewById(R.id.cash_modifier_quantity);
                        CustomTextView price    = (CustomTextView) convertView.findViewById(R.id.cash_modifier_price);
                        title.setVisibility(GONE);
                        quantity.setVisibility(GONE);
                        price.setVisibility(GONE);
                    }
                }

                else
                {
                    convertView.setVisibility(GONE);
                }
            }

            else // product.getPercentage() != 1
            {
                convertView = inflater.inflate(R.layout.element_gridview_cash_modifier_null, null);
            }
        }

        return convertView;
    }


    public int returnQuantityForModifier(CashButtonLayout product, CashButtonListLayout modifier)
    {
        int qty = 0;
        if (split_qty_map == null)
        {
            Integer gtsq = groups_total_selected_quantity.get(product);
            if (gtsq == null)
            {
                gtsq = 0;
            }
            Integer pnysq = partial_not_yet_saved_quantity.get(product);
            if (pnysq == null)
            {
                pnysq = 0;
            }
            //if(product.getQuantityInt()-product.getHomage()-gtsq-pnysq==0){
            if (product.getQuantityInt() - gtsq - pnysq == 0)
            {
                qty = 0;
            }
            else
            {

                int division = modifier.getQuantityInt() / product.getQuantityInt();
                //qty = (modifier.getQuantityInt() - (product.getHomage())) - gtsq*division - pnysq*division;
                //qty = (modifier.getQuantityInt() - (product.getHomage())*division) - gtsq*division - pnysq*division;
                qty = modifier.getQuantityInt() - gtsq * division - pnysq * division;

            }
        }
        else
        {
            if (split_qty_map.containsKey(product))
            {
                Integer a        = split_qty_map.get(product) - product.getHomage();
                int     division = modifier.getQuantityInt() / product.getQuantityInt();

                qty = a * division;
            }
        }
        return qty;
    }


    public int setGroupSelected(boolean selected, int groupPosition, int qty_to_reset)
    {
        CashButtonLayout product                = groups.get(groupPosition);
        int              qty_selected           = groups_total_selected_quantity.get(product);
        int              qty_currently_selected = partial_not_yet_saved_quantity.get(product);
        if (qty_to_reset <= 0)
        {
            if (product.getQuantityInt() - qty_selected - qty_currently_selected >= 0)
            {
                qty_currently_selected++;
                partial_not_yet_saved_quantity.put(product, qty_currently_selected);
                /**
                 ArrayList<CashButtonListLayout> modifiers = map.get(product);
                 for(int i =0; i<modifiers.size();i++){
                 Integer modQty = modifiers.get(i).getQuantityInt();
                 int division = modQty/product.getQuantityInt();
                 modifiers.get(i).setQuantity(modQty-division);
                 }
                 */
                int homageQty = 0;
                if (homage_list.containsKey(product))
                {
                    homageQty = homage_list.get(product);
                }
                else if (product.getOriginalCBL() != null)
                {
                    if (homage_list.containsKey(product.getOriginalCBL()))
                    {
                        homageQty = homage_list.get(product.getOriginalCBL());
                    }
                }
                if (product.getQuantityInt() - qty_selected - qty_currently_selected - homageQty <= 0)
                {
                    product.setSelected(true);
                    //groups.get(groupPosition).setSelected_quantity(product.getQuantityInt());
                }
            }

            notifyDataSetChanged();
        }
        else
        {
            qty_currently_selected -= qty_to_reset;
            partial_not_yet_saved_quantity.put(product, qty_currently_selected);
            product.setSelected(false);
            notifyDataSetChanged();
        }
        return qty_selected + qty_currently_selected;
    }


    public int setCustomerGroupSelected(boolean selected, int groupPosition, int qty_to_reset)
    {
        CashButtonLayout product                = groups.get(groupPosition);
        int              qty_selected           = groups_total_selected_quantity.get(product);
        int              to_save                = partial_not_yet_saved_quantity.get(product);
        int              qty_currently_selected = partial_not_yet_saved_quantity.get(product);
        if (qty_to_reset <= 0)
        {
            if (product.getQuantityInt() - qty_selected - qty_currently_selected >= 0)
            {
                qty_currently_selected = product.getQuantityInt();
                partial_not_yet_saved_quantity.put(product, qty_currently_selected);
                /**
                 ArrayList<CashButtonListLayout> modifiers = map.get(product);
                 for(int i =0; i<modifiers.size();i++){
                 Integer modQty = modifiers.get(i).getQuantityInt();
                 int division = modQty/product.getQuantityInt();
                 modifiers.get(i).setQuantity(modQty-division);
                 }
                 */
                if (product.getQuantityInt() - qty_selected - qty_currently_selected <= 0)
                {
                    product.setSelected(true);
                    //groups.get(groupPosition).setSelected_quantity(product.getQuantityInt());
                }
            }
            notifyDataSetChanged();
        }
        else
        {
            qty_currently_selected -= qty_to_reset;
            partial_not_yet_saved_quantity.put(product, qty_currently_selected);
            product.setSelected(false);
            notifyDataSetChanged();
        }
        int homage = 0;
        if (homage_list.containsKey(product))
        {
            homage = homage_list.get(product);
        }
        return qty_selected + qty_currently_selected - homage - to_save;
    }

    /*public int setCustomerGroupSelectedNotSelected(boolean selected, int groupPosition, int qty_to_reset){
        CashButtonLayout product = groups.get(groupPosition);
        int qty_selected = groups_total_selected_quantity.get(product);
        int to_save = partial_not_yet_saved_quantity.get(product);
        int qty_currently_selected = partial_not_yet_saved_quantity.get(product);
        if (qty_to_reset<=0) {
            if(product.getQuantityInt()-qty_selected-qty_currently_selected>=0){
                qty_currently_selected = product.getQuantityInt() ;
                partial_not_yet_saved_quantity.put(product,qty_currently_selected);
                */


    /**
     * ArrayList<CashButtonListLayout> modifiers = map.get(product);
     * for(int i =0; i<modifiers.size();i++){
     * Integer modQty = modifiers.get(i).getQuantityInt();
     * int division = modQty/product.getQuantityInt();
     * modifiers.get(i).setQuantity(modQty-division);
     * }
     *//*
                if(product.getQuantityInt()-qty_selected-qty_currently_selected<=0) {
                    product.setSelected(true);
                    //groups.get(groupPosition).setSelected_quantity(product.getQuantityInt());
                }
            }
            //notifyDataSetChanged();
        }
        else{
            qty_currently_selected -= qty_to_reset;
            partial_not_yet_saved_quantity.put(product,qty_currently_selected);
            product.setSelected(false);
            //notifyDataSetChanged();
        }
        int homage = 0;
        if(homage_list.containsKey(product)) homage = homage_list.get(product);
        return qty_selected+qty_currently_selected-homage-to_save;
    }*/
    public void saveQty()
    {
        for (CashButtonLayout prod : groups
        )
        {
            if (groups_total_selected_quantity.get(prod) != null)
            {
                int              a = groups_total_selected_quantity.get(prod);
                int              b = partial_not_yet_saved_quantity.get(prod);
                CashButtonLayout p = prod;
                groups_total_selected_quantity.put(prod, (groups_total_selected_quantity.get(prod) + partial_not_yet_saved_quantity.get(prod)));
                partial_not_yet_saved_quantity.put(prod, 0);
            }

        }
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }


    public void setMode(int mode)
    {
        this.mode = mode;
    }


    public Float getTotal_cost()
    {
        if (total_cost == null)
        {
            total_cost = 0.0f;
        }
        return total_cost;
    }


    public void setTotal_cost(Float t)
    {
        total_cost = t;
    }


    public Float getPartial_cost()
    {
        Float cost                = 0.0f;
        Float discountAmountTotal = 0.0f;
        Float discountAmount      = 0.0f;
        // Calculating remaining cost
        for (CashButtonLayout prod : groups)
        {
            Integer a = groups_total_selected_quantity.get(prod);
            if (a == null)
            {
                a = 0;
            }
            if (prod.getHomage() != 1)
            {
                if (prod.getDiscount() != null)
                {
                    discountAmountTotal = discountAmount + prod.getDiscount();
                    discountAmount      = prod.getDiscount();
                    if (discountAmount > (prod.getPriceFloat() * ((prod.getQuantityInt() - prod.getHomage()) - a)))
                    {
                        discountAmount = discountAmount - (prod.getPriceFloat() * ((prod.getQuantityInt() - prod.getHomage())));
                        ArrayList<CashButtonListLayout> mods = map.get(prod);
                        if (mods != null)
                        {
                            for (CashButtonListLayout mod : mods)
                            {
                                if (discountAmount >= mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage())))
                                {
                                    discountAmount = discountAmount - mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()));
                                }
                                else
                                {
                                    Integer b = mod.getQuantityInt();
                                    Float   c = mod.getPriceFloat();

                                    Log.d("a", " " + a);
                                    Log.d("b", " " + b);
                                    Log.d("c", " " + c);
                                    if (a == 0)
                                    {
                                        cost += (mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()))) - discountAmount;
                                    }
                                    discountAmount = 0.0f;
                                    //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                                }
                            }
                        }
                    }
                    else
                    {
                        cost           = cost + (prod.getPriceFloat() * ((prod.getQuantityInt() - prod.getHomage()) - a)) - discountAmount;
                        discountAmount = 0.0f;
                        ArrayList<CashButtonListLayout> mods = map.get(prod);
                        if (mods != null)
                        {
                            for (CashButtonListLayout mod : mods)
                            {
                                if (mod != null)
                                {
                                    Integer b = mod.getQuantityInt();
                                    Float   c = mod.getPriceFloat();

                                    Log.d("a", " " + a);
                                    Log.d("b", " " + b);
                                    Log.d("c", " " + c);
                                    cost += mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()) - a);
                                    //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                                }
                            }
                        }
                    }
                }
                else
                {
                    discountAmount = 0.0f;
                    if (prod.getQuantityInt() - prod.getHomage() != 0)
                    {

                        cost += (prod.getPriceFloat() * ((prod.getQuantityInt() /*+ prod.getHomage()*/) - a));
                        ArrayList<CashButtonListLayout> mods = map.get(prod);
                        if (mods != null)
                        {
                            for (CashButtonListLayout mod : mods)
                            {

                                Integer b = mod.getQuantityInt();
                                Float   c = mod.getPriceFloat();

                                Log.d("a", " " + a);
                                Log.d("b", " " + b);
                                Log.d("c", " " + c);
                                int division = mod.getQuantityInt() / prod.getQuantityInt() - prod.getHomage();

                                cost += mod.getPriceFloat() * (mod.getQuantityInt() - a * division);
                                //cost += mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()) - a);
                                //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                            }
                        }
                    }
                }


            }
        }
        Float c = dbA.getBillDiscountPrice(billId);
        if (c != 0.0)
        {
            cost = cost - (c - discountAmountTotal);
        }
        //dbA.showData("bill_total");
        Double c1 = dbA.getBillPrice(billId);
        //cost = c1.floatValue()-c;
        if (c1.floatValue() != 0)
        {
            cost = c1.floatValue() - c - getHomage();
        }
        //cost = c1.floatValue()-c;

        return cost;
    }


    public float getHomage()
    {
        float homage = 0.0f;

        for (CashButtonLayout prod : groups)
        {
            Integer a = groups_total_selected_quantity.get(prod);
            if (a == null)
            {
                a = 0;
            }
            if (prod.getHomage() == 1)
            {

                homage += (prod.getPriceFloat() * ((prod.getQuantityInt() /*+ prod.getHomage()*/) - a));
                ArrayList<CashButtonListLayout> mods = map.get(prod);
                if (mods != null)
                {
                    for (CashButtonListLayout mod : mods)
                    {

                        Integer b = mod.getQuantityInt();
                        Float   c = mod.getPriceFloat();

                        Log.d("a", " " + a);
                        Log.d("b", " " + b);
                        Log.d("c", " " + c);
                        int division = mod.getQuantityInt() / prod.getQuantityInt();

                        homage += mod.getPriceFloat() * (mod.getQuantityInt() - a * division);
                        //cost += mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()) - a);
                        //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                    }
                }

            }
        }
        return homage;
    }


    public Float getPartial_cost_for_Item()
    {
        Float cost           = 0.0f;
        Float discountAmount = 0.0f;
        // Calculating remaining cost
        for (CashButtonLayout prod : groups)
        {
            if (!prod.isSelected())
            {
                Integer a = groups_total_selected_quantity.get(prod);
                if (a == null)
                {
                    a = 0;
                }
                if (prod.getDiscount() != null)
                {
                    discountAmount = prod.getDiscount();
                    if (discountAmount > (prod.getPriceFloat() * ((prod.getQuantityInt() - prod.getHomage()) - a)))
                    {
                        discountAmount = discountAmount - (prod.getPriceFloat() * ((prod.getQuantityInt() - prod.getHomage())));
                        ArrayList<CashButtonListLayout> mods = map.get(prod);
                        if (mods != null)
                        {
                            for (CashButtonListLayout mod : mods)
                            {
                                if (discountAmount >= mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage())))
                                {
                                    discountAmount = discountAmount - mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()));
                                }
                                else
                                {
                                    Integer b = mod.getQuantityInt();
                                    Float   c = mod.getPriceFloat();

                                    Log.d("a", " " + a);
                                    Log.d("b", " " + b);
                                    Log.d("c", " " + c);
                                    if (a == 0)
                                    {
                                        cost += (mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()))) - discountAmount;
                                    }
                                    discountAmount = 0.0f;
                                    //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                                }
                            }
                        }
                    }
                    else
                    {
                        cost           = cost + (prod.getPriceFloat() * ((prod.getQuantityInt() - prod.getHomage()) - a)) - discountAmount;
                        discountAmount = 0.0f;
                        ArrayList<CashButtonListLayout> mods = map.get(prod);
                        if (mods != null)
                        {
                            for (CashButtonListLayout mod : mods)
                            {

                                Integer b = mod.getQuantityInt();
                                Float   c = mod.getPriceFloat();

                                Log.d("a", " " + a);
                                Log.d("b", " " + b);
                                Log.d("c", " " + c);
                                cost += mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()) - a);
                                //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                            }
                        }
                    }
                }
                else
                {
                    discountAmount = 0.0f;
                    if (prod.getQuantityInt() - prod.getHomage() != 0)
                    {

                        cost += (prod.getPriceFloat() * ((prod.getQuantityInt() /*+ prod.getHomage()*/) - a));
                        ArrayList<CashButtonListLayout> mods = map.get(prod);
                        if (mods != null)
                        {
                            for (CashButtonListLayout mod : mods)
                            {

                                Integer b = mod.getQuantityInt();
                                Float   c = mod.getPriceFloat();

                                Log.d("a", " " + a);
                                Log.d("b", " " + b);
                                Log.d("c", " " + c);
                                int division = mod.getQuantityInt() / prod.getQuantityInt() - prod.getHomage();

                                cost += mod.getPriceFloat() * (mod.getQuantityInt() - a * division);
                                //cost += mod.getPriceFloat() * ((mod.getQuantityInt() - prod.getHomage()) - a);
                                //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                            }
                        }
                    }
                }

            }
            Float c = dbA.getBillDiscountPrice(billId);
            if (c != 0.0)
            {
                cost = cost - c;
            }
        }
        return cost;
    }

    /*public CashButtonLayout returnProduct(int groupPosition){
        return groups.get(groupPosition);
    }*/


    public HashMap<CashButtonLayout, Integer> getProductsLeft()
    {
        if (groups_total_selected_quantity == null)
        {
            groups_total_selected_quantity = new HashMap<CashButtonLayout, Integer>();
        }
        return groups_total_selected_quantity;
    }


    public void restoreFromItemSplit(SubdivisionItem item, boolean areThereSplitsLeft)
    {
        ArrayList<CashButtonLayout> notToRemoveAnymore = new ArrayList<CashButtonLayout>();
        ArrayList<CashButtonLayout> wish               = new ArrayList<CashButtonLayout>();
        for (CashButtonLayout cbl : item.getItems()
        )
        {
            for (CashButtonLayout prod : groups_total_selected_quantity.keySet()
            )
            {
                /**
                 * When there is correspondence between the two CashButtonLayouts in the maps, update the
                 * total_selected quantity by getting the old one and subtracting the one contained in item
                 */
                if (cbl.getID() == prod.getID() && cbl.getOriginalCBL() == prod /*!notToRemoveAnymore.contains(prod) && !notToRemoveAnymore.contains(cbl)*/)
                {
                    notToRemoveAnymore.add(prod);
                    notToRemoveAnymore.add(cbl);
                    if (partial_not_yet_saved_quantity.get(prod) != null)
                    {
                        if (partial_not_yet_saved_quantity.get(prod) != null)
                        {
                            Integer a = groups_total_selected_quantity.get(prod);
                            Integer b = item.getItems_map().get(cbl);
                            Integer c = partial_not_yet_saved_quantity.get(prod);
                            if (c - item.getItems_map().get(cbl) < 0)
                            {
                                partial_not_yet_saved_quantity.put(prod, 0);
                            }
                            else
                            {
                                partial_not_yet_saved_quantity.put(prod, c - item.getItems_map().get(cbl));
                            }
                        }
                    }
                    prod.setSelected(false);


                       /* Integer a = groups_total_selected_quantity.get(prod);
                        Integer b = item.getItems_map().get(cbl);
                        if (a - b >= 0) {
                            groups_total_selected_quantity.put(prod, groups_total_selected_quantity.get(prod) - item.getItems_map().get(cbl));
                        }
                        if (partial_not_yet_saved_quantity.get(prod) != null) {
                            Integer c = partial_not_yet_saved_quantity.get(prod);
                            if(cbl.getHomage()==0) {
                                if(c-item.getItems_map().get(cbl)<0)
                                    partial_not_yet_saved_quantity.put(prod, 0);
                                else
                                    partial_not_yet_saved_quantity.put(prod, c - item.getItems_map().get(cbl));
                                if(homage_list.containsKey(prod)){
                                    //homage_list.remove(prod);
                                    if(prod.getHomage()!=0){
                                        wish.add(prod);
                                    }
                                }
                            }else{
                                wish.add(cbl);


                            }
                        }
                        prod.setSelected(false);
                        prod.setSelected_quantity(prod.getQuantityInt() - prod.getSelected_quantity());
                        //prod.setHomage(cbl.getHomage());
                        //prod.setNewDiscount(cbl.getDiscount());
                        CashButtonLayout aa = prod;

                        //break;*/

                }
            }
        }
     /*   for (CashButtonLayout prod: wish) {
            if(prod.getHomage()!=0) {
                *//*if(homage_list.containsKey(prod)){
                    if(homage_list.get(prod)>1) homage_list.put(prod,homage_list.get(prod)-1);
                    else homage_list.put(prod,0);
                }*//*
     *//*if(groups_total_selected_quantity.containsKey(prod)){
                    if(groups_total_selected_quantity.get(prod)>1) groups_total_selected_quantity.put(prod,groups_total_selected_quantity.get(prod)-1);
                    else groups_total_selected_quantity.put(prod, 0);
                }*//*
                if(partial_not_yet_saved_quantity.containsKey(prod)){
                    if(partial_not_yet_saved_quantity.get(prod)>1) partial_not_yet_saved_quantity.put(prod,partial_not_yet_saved_quantity.get(prod)-1);
                    else partial_not_yet_saved_quantity.put(prod, 0);
                }

            }
        }*/

        isSplit = areThereSplitsLeft;
        notifyDataSetChanged();
    }


    public void showOriginalBill1(ArrayList<CashButtonLayout> products, HashMap<CashButtonLayout, Integer> items_map)
    {
        map    = map_original;
        groups = products;
        Collections.sort(groups);
        notifyDataSetChanged();
        split_qty_map   = null;
        subdivisionItem = null;
        // ((PaymentActivity)context).setPay_mode(PAY_TOTAL_BILL);
        myFragment.expandGroups();

        for (CashButtonLayout prod : groups)
        {
            if (partial_not_yet_saved_quantity.get(prod) != null)
            {
                if (partial_not_yet_saved_quantity.get(prod) == 0)
                {
                    Integer a = groups_total_selected_quantity.get(prod);
                    Integer c = partial_not_yet_saved_quantity.get(prod);
                    prod.setSelected(false);
                }
            }

        }
        Collections.sort(groups);
        notifyDataSetChanged();

    }


    public void showOriginalBill()
    {
        map    = map_original;
        groups = groups_original;
        notifyDataSetChanged();
        split_qty_map   = null;
        subdivisionItem = null;
        // ((PaymentActivity)context).setPay_mode(PAY_TOTAL_BILL);
        myFragment.expandGroups();
    }


    public void removeAllSelectedItem()
    {
        for (CashButtonLayout g : groups)
        {
            g.setSelected(false);
        }
    }


    public int getElementId(int groupPosition)
    {
        if (groups.size() <= groupPosition)
        {
            return groups.get(groupPosition).getID();
        }
        else
        {
            return -1;
        }
    }


    public int returnOriginalPosition(int groupPosition)
    {
        CashButtonLayout product  = groups.get(groupPosition);
        int              toReturn = -1;
        if (groups_backup != null)
        {
            for (CashButtonLayout original : groups_backup)
            {
                if (product.getID() == original.getID())
                {
                    toReturn = original.getPosition();
                    break;
                }
            }
        }
        else
        {
            toReturn = groupPosition;
        }
        return toReturn;
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getGroupsBackup()
    {
        return map_backup;
    }


    public ArrayList<CashButtonLayout> getGroups()
    {
        return groups;
    }


    public ArrayList<CashButtonLayout> getGroupsProductBackup()
    {
        return groups_backup;
    }


    // passare sia split che item è ridondante, modifica in modo tale da usare solo item.
    public void showSplitBill(Map<CashButtonLayout, Integer> split, SubdivisionItem item)
    {
        if (split == null)
        {
            // restore normal order list;
            if (groups_backup != null)
            {
                groups        = new ArrayList<>(groups_backup);
                map           = new HashMap<>(map_backup);
                groups_backup = null;
                map_backup    = null;
                split_qty_map = null;
            }
        }
        else
        {
            /*if(groups_backup == null) {
                groups_backup = new ArrayList<>(groups);
                map_backup = new HashMap<>(map);
            }*/
            if (item.getMode() == -1)
            {
                split_qty_map = null;
            }
            else
            {
                split_qty_map = split;
            }
            groups = new ArrayList<>(split.keySet());
            map    = new HashMap<>();

            for (CashButtonLayout prod : groups
            )
            {
                CashButtonLayout p = null;

                if (groups_backup != null)
                {
                    for (CashButtonLayout p1 : groups_backup
                    )
                    {
                        if (prod.getID() == p1.getID())
                        {
                            p = p1;
                            break;
                        }
                    }
                }
                if (map_backup != null)
                {
                    if (map_backup.get(p) != null)
                    {
                        map.put(prod, map_backup.get(p));
                    }
                    else
                    {
                        map.put(prod, prod.getCashList());
                    }
                }
                else
                {
                    map.put(prod, prod.getCashList());
                }
                /*if(prod.getQuantityInt()>0){
                    if(prod.getOriginalCBL()!=null){
                        homage_list.put(prod, homage_list.get(prod.getOriginalCBL()));
                    }
                }*/

            }
        }
        Collections.sort(groups);

        subdivisionItem = item;
        notifyDataSetChanged();

        ((PaymentActivity) context).expandGroup();
    }


    public void showSplitBillOriginal(Map<CashButtonLayout, Integer> split, SubdivisionItem item)
    {
        if (split == null)
        {
            // restore normal order list;
            if (groups_backup != null)
            {
                groups        = new ArrayList<>(groups_backup);
                map           = new HashMap<>(map_backup);
                groups_backup = null;
                map_backup    = null;
                split_qty_map = null;
            }
        }
        else
        {
            if (groups_backup == null)
            {
                groups_backup = new ArrayList<>(groups);
                map_backup    = new HashMap<>(map);
            }
            split_qty_map = null;
            groups        = new ArrayList<>(split.keySet());
            map           = new HashMap<>();
            for (CashButtonLayout prod : groups
            )
            {
                CashButtonLayout p = null;
                for (CashButtonLayout p1 : groups_backup
                )
                {
                    if (prod.getID() == p1.getID())
                    {
                        p = p1;
                        break;
                    }
                }
                if (map_backup != null)
                {
                    if (map_backup.get(p) != null)
                    {
                        map.put(prod, map_backup.get(p));
                    }
                    else
                    {
                        map.put(prod, prod.getCashList());
                    }
                }
                else
                {
                    map.put(prod, prod.getCashList());
                }
                //map.put(prod, map_backup.get(p));
            }
        }
        Collections.sort(groups);

        subdivisionItem = item;
        notifyDataSetChanged();
    }


    public void showPartialSplitBill(SubdivisionItem item)
    {
        if (groups_backup == null)
        {
            groups_backup = new ArrayList<>(groups);
            map_backup    = new HashMap<>(map);
        }
        CashButtonLayout prod = new CashButtonLayout();
        if (item.getMode() == 4 || item.getMode() == 15)
        {
            prod.setTitle("Per Amount");
            prod.setQuantity(1);
            prod.setPrice(item.getOwed_money());
        }
        else if (item.getMode() == 1)
        {
            prod.setTitle("Per Number");
            prod.setQuantity(item.getNumber_subdivision());
            prod.setPrice(item.getOwed_money());
        }
        groups = new ArrayList<CashButtonLayout>();
        groups.add(prod);
        //map.put(prod, map_backup.get(p));


        subdivisionItem = item;
        notifyDataSetChanged();
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> showSplitBillToPrint(Map<CashButtonLayout, Integer> split)
    {
        map = new HashMap<>();
        if (groups_backup == null)
        {
            groups_backup = new ArrayList<>(groups);
            map_backup    = new HashMap<>(map);
        }
        split_qty_map = split;
        groups        = new ArrayList<>(split.keySet());
        map           = new HashMap<>();
        for (CashButtonLayout prod : groups
        )
        {
            CashButtonLayout p = null;
            for (CashButtonLayout p1 : groups_backup
            )
            {
                if (prod.getID() == p1.getID())
                {
                    p = p1;
                    break;
                }
            }
            map.put(prod, map_backup.get(p));
        }

        return map;
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> showSplitBillToPrint2(ArrayList<CashButtonLayout> products)
    {
        HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> returnmap = new HashMap<>();
        for (CashButtonLayout prod : groups_backup
        )
        {
            CashButtonLayout p = null;
            for (CashButtonLayout p1 : products
            )
            {
                if (prod.getID() == p1.getID())
                {
                    p = p1;
                    break;
                }
            }
            map.put(prod, map_backup.get(p));
        }

        return map;
    }


    public ArrayList<CashButtonLayout> showSplitBillToPrintProducts(Map<CashButtonLayout, Integer> split)
    {


        ArrayList<CashButtonLayout> myproducts = new ArrayList<CashButtonLayout>();

        for (Map.Entry<CashButtonLayout, Integer> p : split.entrySet())
        {
            myproducts.add(p.getKey());
            int qty = 0;
            if (split_qty_map == null)
            {
                Integer gtsq = groups_total_selected_quantity.get(myproducts.get(myproducts.size() - 1));
                if (gtsq == null)
                {
                    gtsq = 0;
                }
                Integer pnysq = partial_not_yet_saved_quantity.get(myproducts.get(myproducts.size() - 1));
                if (pnysq == null)
                {
                    pnysq = 0;
                }
                //qty = (product.getQuantityInt()-product.getHomage())-gtsq-pnysq;
                qty = (myproducts.get(myproducts.size() - 1).getQuantityInt()) - gtsq - pnysq;
            }
            else
            {
                if (split_qty_map.containsKey(myproducts.get(myproducts.size() - 1)))
                {
                    int a1 = split_qty_map.get(myproducts.get(myproducts.size() - 1));
                    int b1 = myproducts.get(myproducts.size() - 1).getHomage();
                    qty = split_qty_map.get(myproducts.get(myproducts.size() - 1)) /*- myproducts.get(myproducts.size()-1).getHomage()*/;
                }
                else
                {
                    qty = 0;
                }
            }
            //myproducts.get(myproducts.size()-1).setQuantity(p.getValue());
            if (qty != 0)
            {
                myproducts.get(myproducts.size() - 1).setQuantity(qty);
            }
            else
            {
                myproducts.remove(p.getKey());
            }

        }


        return myproducts;
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getCurrentSplitMap(ArrayList<CashButtonLayout> prods)
    {

        map = new HashMap<>();

        /**
         *  If ArrayList prods is null then the method will return the map corresponding to the current products set
         *  while if it is not null it will return the map 'product->modifiers' related to prods array.
         */
        for (CashButtonLayout prod : (prods == null ? groups : prods)
        )
        {
            CashButtonLayout p = null;
            if (groups_backup != null)
            {
                for (CashButtonLayout p1 : groups_backup
                )
                {
                    if (prod.getID() == p1.getID())
                    {
                        p = p1;
                        break;
                    }
                }
                map.put(prod, map_backup.get(p));
            }
        }
        return map;
    }


    public SubdivisionItem getSubdivisionItem()
    {
        return subdivisionItem;
    }


    public void setSubdivisionItem(SubdivisionItem i)
    {
        this.subdivisionItem = i;
    }


    public boolean isSplit()
    {
        return isSplit;
    }


    public void setSplit(boolean split)
    {
        isSplit = split;
    }


    public void loadBillSplits(ArrayList<SubdivisionItem> items)
    {
        Float   f           = 0.0f;
        boolean numberSplit = false;
        for (SubdivisionItem item : items
        )
        {
            /**
             * IF the current item subdivision mode is "ITEM MODE", then finds the match between each CashButtonLayout
             * in the list inside the subdivision item and the groups inside OrderListAdapter:
             * in this way we can use the matched CashButtonLayout as the key to increase the selected quantity
             * inside the map named "groups_total_selected_quantity"
             */
            if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE)
            {
                for (CashButtonLayout p : item.getItems()
                )
                {
                    for (CashButtonLayout g : groups
                    )
                    {
                        if (p.getID() == g.getID() && g.getPercentage() == 1)
                        {
                            groups_total_selected_quantity.put(g, groups_total_selected_quantity.get(g) + item.getItems_map().get(p));
                            if (g.getQuantityInt() - groups_total_selected_quantity.get(g) == 0)
                            {
                                g.setSelected(true);
                                myFragment.addSentItem(g);
                            }
                            if (partial_not_yet_saved_quantity.containsKey(g))
                            {
                                int a = p.getQuantityInt() + partial_not_yet_saved_quantity.get(g);
                                partial_not_yet_saved_quantity.put(g, p.getQuantityInt() + partial_not_yet_saved_quantity.get(g));
                            }
                            else
                            {
                                partial_not_yet_saved_quantity.put(g, p.getQuantityInt());
                            }
                            g.setNewDiscount(g.getDiscount() - p.getDiscount());
                            //break;
                        }
                        else if (p.getID() == g.getID() && g.isSelected())
                        {
                            groups_total_selected_quantity.put(g, groups_total_selected_quantity.get(g) + item.getItems_map().get(p));
                            if (g.getQuantityInt() - groups_total_selected_quantity.get(g) == 0)
                            {
                                g.setSelected(true);
                                myFragment.addSentItem(g);
                            }

                            partial_not_yet_saved_quantity.put(g, p.getQuantityInt());
                            g.setNewDiscount(g.getDiscount() - p.getDiscount());
                        }
                    }
                }
            }
            else if (item.getMode() == PERCENTAGE_MODE)
            {
                ((PaymentActivity) context).setPercentageSplit(true);
                //myFragment.percentageSplit(item.getPercentage().intValue());
                f += item.getOwed_money();
            }
            else if (item.getMode() == NUMBER_MODE)
            {
                ((PaymentActivity) context).setNumberSplit(true);
                myFragment.setNumberSplit(true);
            }
        }
        if (f > 0 && !numberSplit)
        {
           /* myFragment.setRemainingPercentageCost(this.getPartial_cost()-f);
            myFragment.setPrice(this.getPartial_cost()-f);*/

            myFragment.setRemainingPercentageCost(this.getPartial_cost());
            myFragment.setPrice(this.getPartial_cost());
        }
        else if (!numberSplit)
        {
            myFragment.setPrice(this.getPartial_cost());
        }


    }


    public void setRealGroupHomage(int groupPosition)
    {
        groups.get(groupPosition).setHomage(1);
        groups.get(groupPosition).setNewDiscount(0.0f);
    }


    public float setGroupHomage(int groupPosition)
    {
        if (!isSplit)
        {
            Float modCost = 0.0f;
            //if(groups.get(groupPosition).getHomage()!=groups.get(groupPosition).getQuantityInt() &&groups.get(groupPosition).getQuantityInt()>0){
            //groups.get(groupPosition).setHomage(groups.get(groupPosition).getHomage()+1);
            //dbA.updateProductBill(1, groups.get(groupPosition).getID());
            ArrayList<CashButtonListLayout> mods = map.get(groups.get(groupPosition));
            if (mods != null)
            {
                for (CashButtonListLayout mod : mods)
                {
                    Integer b      = mod.getQuantityInt();
                    Float   c      = mod.getPriceFloat();
                    int     intQty = mod.getQuantityInt() / groups.get(groupPosition).getQuantityInt();
                    modCost += mod.getPriceFloat() * intQty;
                }
            }
            notifyDataSetChanged();

            myFragment.setTotalCost(total_cost - groups.get(groupPosition).getPriceFloat() - modCost);
            total_cost = total_cost - groups.get(groupPosition).getPriceFloat() - modCost;

            return total_cost;
        }
        else
        {
            ArrayList<CashButtonLayout> a       = groups;
            Float                       modCost = 0.0f;
            partial_cost = getPartial_cost();
            Integer gtsq = groups_total_selected_quantity.get(groups.get(groupPosition));
            if (gtsq == null)
            {
                gtsq = 0;
            }
            Integer a2 = groups.get(groupPosition).getHomage();
            Integer a3 = groups.get(groupPosition).getQuantityInt();
            if (groups.get(groupPosition).getQuantityInt() - gtsq - groups.get(groupPosition).getHomage() != 0)
            {
                if (groups.get(groupPosition).getHomage() != groups.get(groupPosition).getQuantityInt() && groups.get(groupPosition).getQuantityInt() > 0)
                {
                    groups.get(groupPosition).setHomage(groups.get(groupPosition).getHomage() + 1);
                    //dbA.updateProductBill(1, groups.get(groupPosition).getID());
                    ArrayList<CashButtonListLayout> mods = map.get(groups.get(groupPosition));
                    for (CashButtonListLayout mod : mods)
                    {
                        //Integer a = groups_total_selected_quantity.get(prod);
                        Integer b          = mod.getQuantityInt();
                        Float   c          = mod.getPriceFloat();
                        int     d          = groups.get(groupPosition).getHomage();
                        Integer e          = groups_total_selected_quantity.get(groups.get(groupPosition));
                        Float   provvision = mod.getPriceFloat() * ((groups.get(groupPosition).getHomage()) - gtsq);
                        int     intQty     = mod.getQuantityInt() / groups.get(groupPosition).getQuantityInt();
                        modCost += mod.getPriceFloat() * intQty;
                    }
                    notifyDataSetChanged();
                }
                myFragment.setPartialCost(partial_cost - groups.get(groupPosition).getPriceFloat() - modCost);

            }

            return partial_cost = partial_cost - groups.get(groupPosition).getPriceFloat() - modCost;
        }

    }


    public void resetGroupHomage(int groupPosition)
    {
        if (groups.get(groupPosition).getHomage() != 0)
        {
            Float                           modCost = 0.0f;
            ArrayList<CashButtonListLayout> mods    = map.get(groups.get(groupPosition));
            if (mods != null)
            {
                for (CashButtonListLayout mod : mods)
                {
                    modCost += mod.getPriceFloat() * (groups.get(groupPosition).getHomage() - groups_total_selected_quantity.get(groups.get(groupPosition)));
                    //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
                }
            }
            groups.get(groupPosition).setHomage(groups.get(groupPosition).getHomage() - 1);
            total_cost = total_cost + groups.get(groupPosition).getPriceFloat() + modCost;
        }
        notifyDataSetChanged();
    }


    public void resetGroupHomageElement(int groupPosition)
    {
        groups.get(groupPosition).setHomage(0);
        //partial_not_yet_saved_quantity.put(groups.get(groupPosition).getOriginalCBL(),partial_not_yet_saved_quantity.get(groups.get(groupPosition).getOriginalCBL())-1);
        //groups.get(groupPosition).setOriginalCBL(null);

        if (homage_list.containsKey(groups.get(groupPosition)))
        {
            int a = homage_list.get(groups.get(groupPosition));
            if (a != 0)
            {
                homage_list.put(groups.get(groupPosition), homage_list.get(groups.get(groupPosition)) - 1);
            }
        }
        if (homage_list.containsKey(groups.get(groupPosition).getOriginalCBL()))
        {
            int b = homage_list.get(groups.get(groupPosition).getOriginalCBL());
            if (b != 0)
            {
                homage_list.put(groups.get(groupPosition).getOriginalCBL(), homage_list.get(groups.get(groupPosition).getOriginalCBL()) - 1);
            }
        }

        notifyDataSetChanged();
    }


    public void resetGroupHomageElement2(int groupPosition)
    {
        groups.get(groupPosition).setHomage(0);
        //partial_not_yet_saved_quantity.put(groups.get(groupPosition).getOriginalCBL(),partial_not_yet_saved_quantity.get(groups.get(groupPosition).getOriginalCBL())-1);
        //groups.get(groupPosition).setOriginalCBL(null);

        /*if(homage_list.containsKey(groups.get(groupPosition)))
        {
            int a = homage_list.get(groups.get(groupPosition));
            homage_list.put(groups.get(groupPosition), homage_list.get(groups.get(groupPosition))-1);
        }*/
        if (homage_list.containsKey(groups.get(groupPosition).getOriginalCBL()))
        {
            int b = homage_list.get(groups.get(groupPosition).getOriginalCBL());
            if (b != 0)
            {
                homage_list.put(groups.get(groupPosition).getOriginalCBL(), homage_list.get(groups.get(groupPosition).getOriginalCBL()) - 1);
            }
        }

        notifyDataSetChanged();
    }


    /**
     * DSCOUNT PART
     */
    public float getElementPrice(int groupPosition)
    {
        float                           cost    = 0.0f;
        CashButtonLayout                product = groups.get(groupPosition);
        ArrayList<CashButtonListLayout> mods    = map.get(groups.get(groupPosition));
        if (mods != null)
        {
            for (CashButtonListLayout mod : mods)
            {
                Integer a = product.getQuantityInt();
                Integer b = groups.get(groupPosition).getHomage();
                Integer c = groups_total_selected_quantity.get(groups.get(groupPosition));
                if (c == null)
                {
                    c = 0;
                }
                float d       = mod.getPriceFloat();
                int   myValue = 0;
                if (partial_not_yet_saved_quantity.containsKey(product))
                {
                    myValue = partial_not_yet_saved_quantity.get(product);
                }
                Float a1 = mod.getPriceFloat();
                int   b1 = mod.getQuantityInt();
                int   c1 = product.getQuantityInt();
                int   d1 = myValue;
                int   e1 = groups.get(groupPosition).getHomage();
                int   c2 = c;

                //cost += mod.getPriceFloat()*(mod.getQuantityInt()/product.getQuantityInt()) * (product.getQuantityInt() -myValue - groups.get(groupPosition).getHomage() - c);
                //cost += mod.getPriceFloat()* (mod.getQuantityInt()/(product.getQuantityInt() -myValue - groups.get(groupPosition).getHomage() - c));
                cost += mod.getPriceFloat() * (mod.getQuantityInt() / product.getQuantityInt());

                //cost += mod.getPriceFloat()*mod.getQuantityInt() * (product.getQuantityInt() -partial_not_yet_saved_quantity.get(product) - groups.get(groupPosition).getHomage() - c);
                //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
            }
        }
        Integer a     = product.getQuantityInt();
        Integer b     = groups.get(groupPosition).getHomage();
        Integer c     = groups_total_selected_quantity.get(groups.get(groupPosition));
        int     pnysq = 0;
        if (partial_not_yet_saved_quantity.containsKey(product))
        {
            pnysq = partial_not_yet_saved_quantity.get(product);
        }
        /*if(groups_total_selected_quantity.containsKey(groups.get(groupPosition)))
            pnysq = groups_total_selected_quantity.get(groups.get(groupPosition));*/
        cost += (product.getPriceFloat()) * (product.getQuantityInt() - pnysq - product.getHomage());
        cost -= product.getDiscount();
        return cost;

    }


    public float getSingleElementPrice(int groupPosition)
    {
        float                           cost    = 0.0f;
        CashButtonLayout                product = groups.get(groupPosition);
        ArrayList<CashButtonListLayout> mods    = map.get(groups.get(groupPosition));
        if (mods != null)
        {
            for (CashButtonListLayout mod : mods)
            {
                Integer a = product.getQuantityInt();
                Integer b = groups.get(groupPosition).getHomage();
                Integer c = groups_total_selected_quantity.get(groups.get(groupPosition));
                if (c == null)
                {
                    c = 0;
                }
                float d = mod.getPriceFloat();
                cost += mod.getPriceFloat() * (mod.getQuantityInt() / product.getQuantityInt());
                //cost += mod.getPriceFloat()*mod.getQuantityInt() * (product.getQuantityInt() - groups.get(groupPosition).getHomage() - c);
                //cost += mod.getPriceFloat() * mod.getQuantityInt() * ( prod.getQuantityInt() - groups_total_selected_quantity.get(prod) );
            }
        }
        cost += product.getPriceFloat() - product.getDiscount();
        return cost;

    }


    public void setDiscountElement(int groupPosition, float amount, boolean reset)
    {
        CashButtonLayout product = groups.get(groupPosition);
        if (reset && amount == 0.0f)
        {
            product.setNewDiscount(amount);
        }
        else
        {
            product.setDiscount(amount);
        }
        notifyDataSetChanged();


        ((PaymentActivity) context).setProductsList(groups);
        ((PaymentActivity) context).setModifiersList(map);


    }


    public void resetDiscountElement(int groupPosition, float amount)
    {
        CashButtonLayout product = groups.get(groupPosition);
        resetGroupHomage(groupPosition);
        product.setHomage(0);
        product.setNewDiscount(amount);
        notifyDataSetChanged();


        ((PaymentActivity) context).setProductsList(groups);
        ((PaymentActivity) context).setModifiersList(map);
    }


    public int separateElement(int groupPosition)
    {
        CashButtonLayout product = groups.get(groupPosition);
        int              qty;
        //not sure of this for division
        //I added getHomage whic return int of homage quantity
        if (split_qty_map == null)
        {
            Integer gtsq = groups_total_selected_quantity.get(product);
            if (gtsq == null)
            {
                gtsq = 0;
            }
            Integer pnysq = partial_not_yet_saved_quantity.get(product);
            if (pnysq == null)
            {
                pnysq = 0;
            }
            //qty = (product.getQuantityInt()-product.getHomage())-gtsq-pnysq;
            qty = (product.getQuantityInt()) - gtsq - pnysq;
        }
        else
        {
            if (split_qty_map.containsKey(product))
            {
                int a1 = split_qty_map.get(product);
                int b1 = product.getHomage();
                qty = split_qty_map.get(product) - product.getHomage();
            }
            else
            {
                //qty = 0;
                qty = 1;
            }
        }

        if (qty > 1)
        {
            //if (product.getQuantityInt() - product.getSelected_quantity() > 1) {
            CashButtonLayout newProduct = new CashButtonLayout();
            newProduct.setTitle(product.getTitle());
            newProduct.setPrice(product.getPriceFloat());
            newProduct.setQuantity(1);
            newProduct.setProductId(product.getProductId());
            newProduct.setIsDelete(false);
            newProduct.setModifyModifier(false);
            newProduct.setID(product.getID());
            newProduct.setNewDiscount(0.0f);
            newProduct.setClientPosition(product.getClientPosition());
            newProduct.setPosition(groupPosition + 1);
            groups.add(newProduct);
            ArrayList<CashButtonListLayout> mods = map.get(groups.get(groupPosition));
            if (mods != null)
            {
                for (CashButtonListLayout mod : mods)
                {
                    CashButtonListLayout newMod = new CashButtonListLayout();
                    newMod.setTitle(mod.getTitle());
                    newMod.setPrice(mod.getPriceFloat());
                    int division = mod.getQuantityInt() / (product.getQuantityInt());
                    newMod.setQuantity(division);
                    newMod.setModifierId(mod.getID());
                    newMod.setID(mod.getID());
                    newProduct.setCashList(newMod);
                    mod.setQuantity(mod.getQuantityInt() - division);
                }
            }
            map.put(newProduct, newProduct.getCashList());
            //product.setQuantity(1);
            //Integer pnysq = partial_not_yet_saved_quantity.get(product);
            //if(pnysq==null)
            if (partial_not_yet_saved_quantity.containsKey(product))
            {
                partial_not_yet_saved_quantity.put(product, partial_not_yet_saved_quantity.get(product) + 1);
            }
            else
            {
                partial_not_yet_saved_quantity.put(product, 1);
            }
            partial_not_yet_saved_quantity.put(newProduct, 0);
            groups_total_selected_quantity.put(newProduct, 0);
                /*if (!discount_list.containsKey(product))
                    discount_list.put(product, 1);
                else
                    discount_list.put(product, (discount_list.get(product))+1);
                if (!discount_list.containsKey(newProduct))
                    discount_list.put(newProduct, 1);
                else
                    discount_list.put(newProduct, (discount_list.get(newProduct))+1);*/
            if (!discount_list.containsKey(newProduct))
            {
                discount_list.put(newProduct, 0);
            }
            else
            {
                discount_list.put(newProduct, (discount_list.get(newProduct)));
            }
            if (!homage_list.containsKey(newProduct))
            {
                homage_list.put(newProduct, 0);
            }
            else
            {
                homage_list.put(newProduct, (homage_list.get(newProduct)));
            }
            partial_not_yet_saved_quantity.put(newProduct, 0);
            groups_total_selected_quantity.put(newProduct, 0);
            Collections.sort(groups);
            for (int i = 0; i < groups.size(); i++)
            {
                groups.get(i).setPosition(i);
            }
            notifyDataSetChanged();
            //int positionToReturn = groups.size() - 1;
            int positionToReturn = groupPosition + 1;
                /*for(int i=0; i< groups.size(); i++){
                    if(groups.get(i)==newProduct) positionToReturn = i;
                }*/
            return positionToReturn;


                /*notifyDataSetChanged();
                return groups.size() - 1;*/
        }
        else
        {
            return groupPosition;
        }

    }


    public void setHomageListQuantity(CashButtonLayout product)
    {
        if (homage_list.containsKey(product))
        {
            homage_list.put(product, homage_list.get(product) + 1);
        }
        else
        {
            homage_list.put(product, 1);
        }
    }


    public void setDiscountListQuantity(CashButtonLayout product)
    {
        discount_list.put(product, discount_list.get(product) + 1);
    }


    public int separateElementHomage(int groupPosition)
    {

        CashButtonLayout product = groups.get(groupPosition);
        if (product.getQuantityInt() - product.getHomage() > 1)
        {
            CashButtonLayout newProduct = new CashButtonLayout();
            newProduct.setTitle(product.getTitle());
            newProduct.setPrice(product.getPriceFloat());
            newProduct.setQuantity(1);
            newProduct.setProductId(product.getProductId());
            newProduct.setIsDelete(false);
            newProduct.setModifyModifier(false);
            newProduct.setID(product.getID());
            newProduct.setHomage(1);
            newProduct.setNewDiscount(0.0f);
            newProduct.setPosition(groups.size());
            newProduct.setClientPosition(product.getClientPosition());
            groups.add(newProduct);
            ArrayList<CashButtonListLayout> mods = map.get(groups.get(groupPosition));
            if (mods != null)
            {
                for (CashButtonListLayout mod : mods)
                {
                    CashButtonListLayout newMod = new CashButtonListLayout();
                    newMod.setTitle(mod.getTitle());
                    newMod.setPrice(mod.getPriceFloat());
                    int division = mod.getQuantityInt() / (product.getQuantityInt());
                    newMod.setQuantity(division);
                    newMod.setModifierId(mod.getID());
                    newMod.setID(mod.getID());
                    newProduct.setCashList(newMod);
                    mod.setQuantity(mod.getQuantityInt() - division);
                }
            }
            map.put(newProduct, newProduct.getCashList());
            //product.setQuantity(product.getQuantityInt() - 1);
            /*product.setHomage(product.getHomage()+1);
            split_qty_map.put(product, split_qty_map.get(product));*/
            /*if (!homage_list.containsKey(product))
                homage_list.put(product, 1);
            else
                homage_list.put(product, (homage_list.get(product))+1);*/
            if (partial_not_yet_saved_quantity.containsKey(product))
            {
                partial_not_yet_saved_quantity.put(product, partial_not_yet_saved_quantity.get(product) + 1);
            }
            else
            {
                partial_not_yet_saved_quantity.put(product, 1);
            }
            if (!homage_list.containsKey(newProduct))
            {
                homage_list.put(newProduct, 1);
            }
            else
            {
                homage_list.put(newProduct, (homage_list.get(newProduct)) + 1);
            }
            partial_not_yet_saved_quantity.put(newProduct, 0);
            groups_total_selected_quantity.put(newProduct, 0);
            Collections.sort(groups);
            for (int i = 0; i < groups.size(); i++)
            {
                groups.get(i).setPosition(i);
            }
            notifyDataSetChanged();
            int positionToReturn = groups.size() - 1;
            for (int i = 0; i < groups.size(); i++)
            {
                if (groups.get(i) == newProduct)
                {
                    positionToReturn = i;
                }
            }
            ((PaymentActivity) context).setProductsList(groups);
            ((PaymentActivity) context).setModifiersList(map);
            return positionToReturn;
            //return groups.size() - 1;
        }
        else
        {

          /*  if (!homage_list.containsKey(product))
                homage_list.put(product, 1);
            else
                homage_list.put(product, (homage_list.get(product))+1);*/
            return groupPosition;
        }

    }


    public void separateElementItem(int quantity, int groupPosition)
    {
        CashButtonLayout product = groups.get(groupPosition);

        float                           newCost   = CalculatorFragment.roundDecimal((product.getPriceFloat() * product.getQuantityInt()) / quantity, 2);
        float                           modsValue = 0.0f;
        ArrayList<CashButtonListLayout> mods      = map.get(groups.get(groupPosition));
        if (mods != null)
        {
            for (CashButtonListLayout mod : mods)
            {
                modsValue += mod.getPriceFloat() * mod.getQuantityInt();
            }
        }
        newCost += modsValue / quantity;
        product.setSelected(true);

        //set product so doesn't show anymore
        if (partial_not_yet_saved_quantity.containsKey(product))
        {
            partial_not_yet_saved_quantity.put(product, partial_not_yet_saved_quantity.get(product) + product.getQuantityInt());
        }
        else
        {
            partial_not_yet_saved_quantity.put(product, product.getQuantityInt());
        }

        for (int i = 1; i <= quantity; i++)
        {
            if (i == quantity)
            {
                if ((newCost * quantity) != product.getPriceFloat())
                {
                    newCost = ((product.getPriceFloat() * product.getQuantityInt()) + modsValue) - (newCost * (quantity - 1));
                }
            }

            CashButtonLayout newProduct = new CashButtonLayout();
            if (product.getQuantityInt() == 1)
            {
                newProduct.setTitle(product.getTitle() + " " + i + "/" + quantity);
            }
            else
            {
                newProduct.setTitle(product.getTitle() + " x " + product.getQuantityInt() + " " + i + "/" + quantity);
            }
            newProduct.setPrice(newCost);
            newProduct.setQuantity(1);
            newProduct.setProductId(-30);
            newProduct.setIsDelete(false);
            newProduct.setModifyModifier(false);
            newProduct.setID(product.getID());
            newProduct.setNewDiscount(0.0f);
            newProduct.setSplit(true);
            newProduct.setClientPosition(product.getClientPosition());
            newProduct.setPosition(groupPosition + 1);
            newProduct.setNewCashList(new ArrayList<CashButtonListLayout>());
            newProduct.setVat(0);
            newProduct.setPercentage(quantity);
            groups.add(newProduct);
            map.put(newProduct, new ArrayList<CashButtonListLayout>());

            partial_not_yet_saved_quantity.put(newProduct, 0);
            groups_total_selected_quantity.put(newProduct, 0);

        }

        Collections.sort(groups);
        for (int i = 0; i < groups.size(); i++)
        {
            groups.get(i).setPosition(i);
        }

        ((PaymentActivity) context).setProductsList(groups);
        ((PaymentActivity) context).setModifiersList(map);
        ((PaymentActivity) context).setTotalBillItems(getProducts());

        notifyDataSetChanged();


    }


}
