package com.utils.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.blackbox.fragments.CalculatorFragment;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.SubdivisionItem;
import com.example.blackbox.model.TotalBill;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.example.blackbox.adapter.OrderListAdapter.ITEM_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.NUMBER_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERCENTAGE_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PERSON_MODE;



public class DbAdapterBills extends DbAdapterProducts
{



    // =============================================== //
    // [ BILLS ]
    // =============================================== //


    public int saveTotalBillForPayment(Double totalPrice, Integer numberBill, int printIndex)
    {
        int id = 0;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Boolean falseB = false;
            database.execSQL("INSERT INTO bill_total (total,paid,bill_number, creation_time, pay_time, print_index) VALUES(" + totalPrice + "," + (falseB ? 1 : 0) + "," + numberBill + ",datetime(CURRENT_TIMESTAMP, 'localtime')," + null + "," + printIndex + ");");
            Cursor c = database.rawQuery("SELECT * FROM bill_total ORDER BY id DESC", null);
            if (c.moveToFirst())
            {
                //name = cursor.getString(column_index);//to get other values
                id = c.getInt(0);//to get id, 0 is the column index


            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
        return id;
    }


    public void updateTotalBillWithDiscount(double totalPrice, int numberBill)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total SET total=" +
                    totalPrice + ", paid=" + 1 + ", " +
                    "pay_time = datetime(CURRENT_TIMESTAMP, 'localtime')" +
                    " WHERE id =" + numberBill);
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getBillData(int bill_id, Context context)
    {
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }
        LinkedHashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new LinkedHashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map1 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        ArrayList<CashButtonListLayout> mod_list;
        CashButtonLayout product;
        CashButtonListLayout modifier;
        //showData("product_bill");
        Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + bill_id + " ORDER BY cast(position as REAL) ASC;", null);
        while (c.moveToNext())
        {
            int product_line_id = c.getInt(c.getColumnIndex("id"));

            product = new CashButtonLayout();
            product.setID(c.getInt(c.getColumnIndex("id")));
            product.setProductId(c.getInt(c.getColumnIndex("prod_id")));
            product.setQuantity(c.getInt(c.getColumnIndex("qty")));
            int position = c.getInt(c.getColumnIndex("position"));
            product.setPosition(c.getInt(c.getColumnIndex("position")));
            product.setNewDiscount(c.getFloat(c.getColumnIndex("discount")));


            Cursor cCustomer = database.rawQuery("SELECT * FROM customer_bill WHERE prod_bill_id=" + product
                    .getID(), null);
            if (cCustomer.moveToNext())
            {
                product.setClientPosition(cCustomer.getInt(cCustomer.getColumnIndex("position")));

            }
            cCustomer.close();

            //showData("bill_subdivision_paid");
            Cursor cSubdivision = database.rawQuery("SELECT * FROM bill_subdivision_paid WHERE bill_id=" + bill_id + " ;", null);
            if (cSubdivision.moveToFirst())
            {
                int id = cSubdivision.getInt(0);
                // showData("item_subdivisions");
                Cursor cSubdivision1 = database.rawQuery("SELECT * FROM item_subdivisions WHERE bill_subdivision_id=" + id + " AND product_bill_id=" + product_line_id + ";", null);
                if (cSubdivision1.moveToFirst())
                {
                    //product is already paid
                   /* Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = "+product.getProductId(),null);
                    c1.moveToFirst();
                    product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                    product.setTitle(c1.getString(c1.getColumnIndex("title")));
                    product.setIsPaid(true);
                    c1.close();*/
                    int perc = (cSubdivision1.getInt(cSubdivision1.getColumnIndex("percentage")));
                    //if(perc == 1) {
                    if (product.getProductId() == -30)
                    {
                        Cursor c5 = database.rawQuery("SELECT * FROM product_unspec_bill WHERE prod_bill_id=" + product_line_id, null);
                        c5.moveToFirst();
                        product.setPrice(c5.getFloat(c5.getColumnIndex("price")));
                        product.setTitle(c5.getString(c5.getColumnIndex("description")));
                        product.setVat(StaticValue.staticVat);
                        c5.close();
                    }
                    else
                    {
                        Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                        c1.moveToFirst();
                        product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                        product.setTitle(c1.getString(c1.getColumnIndex("title")));
                        product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                        product.setPrinterId(c1.getInt(c1.getColumnIndex("printer")));
                        product.setIsPaid(false);
                        c1.close();
                    }
                    Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                    mod_list = new ArrayList<>();
                    while (c3.moveToNext())
                    {
                        modifier = new CashButtonListLayout();
                        modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                        modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                        modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                        if (modifier.getModifierId() == -15)
                        {
                            Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                                    .getInt(c3.getColumnIndex("id")), null);
                            if (((c4 != null) && (c4.getCount() > 0)))
                            {
                                c4.moveToFirst();
                                modifier.setTitle("nota");
                                modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                                modifier.setPrice(0.0f);
                                modifier.setVat(StaticValue.staticVat);
                                mod_list.add(modifier);
                            }
                            c4.close();
                        }
                        else
                        {
                            Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                    .getModifierId(), null);
                            c2.moveToFirst();
                            modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                            modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                            modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                            mod_list.add(modifier);
                            c2.close();
                        }
                    }
                    map.put(product, mod_list);
                    c3.close();
                    //}else{
                       /* Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                        c1.moveToFirst();
                        product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                        product.setTitle(c1.getString(c1.getColumnIndex("title")));
                        product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                        product.setIsPaid(false);
                        c1.close();
                        Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                        mod_list = new ArrayList<>();
                        while (c3.moveToNext()) {
                            modifier = new CashButtonListLayout();
                            modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                            modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                            modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                            if (modifier.getModifierId() == -15) {
                                Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3.getInt(c3.getColumnIndex("id")), null);
                                if (((c4 != null) && (c4.getCount() > 0))) {
                                    c4.moveToFirst();
                                    modifier.setTitle("nota");
                                    modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                                    modifier.setPrice(0.0f);
                                    modifier.setVat(StaticValue.staticVat);
                                    mod_list.add(modifier);
                                }
                                c4.close();
                            } else {
                                Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier.getModifierId(), null);
                                c2.moveToFirst();
                                modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                                modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                                modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                                mod_list.add(modifier);
                                c2.close();
                            }
                        }
                        map.put(product, mod_list);
                        c3.close();

                        float newCost = CalculatorFragment.roundDecimal((product.getPriceFloat()*product.getQuantityInt())/perc, 2);
                        float modsValue = 0.0f;
                        for (CashButtonListLayout mod : mod_list) {
                                modsValue = mod.getPriceFloat()*mod.getQuantityInt();
                        }
                        newCost += modsValue/perc;
                        product.setSelected(true);
                        //set product so doesn't show anymore

                        for(int i=1; i<=perc; i++){
                            if(i==perc){
                                if((newCost*perc)!=product.getPriceFloat()){
                                    newCost = (product.getPriceFloat()*product.getQuantityInt()) - (newCost*(perc-1));
                                }
                            }

                            CashButtonLayout newProduct = new CashButtonLayout();
                            if(product.getQuantityInt()==1)
                                newProduct.setTitle(product.getTitle() + " 1/"+perc);
                            else
                                newProduct.setTitle(product.getTitle() +" x "+product.getQuantityInt() + " 1/"+perc);
                            newProduct.setPrice(newCost);
                            newProduct.setQuantity(1);
                            newProduct.setProductId(-30);
                            newProduct.setIsDelete(false);
                            newProduct.setModifyModifier(false);
                            newProduct.setID(product.getID());
                            newProduct.setNewDiscount(0.0f);
                            newProduct.setClientPosition(product.getClientPosition());
                            newProduct.setPosition(position+1);
                            newProduct.setNewCashList(new ArrayList<CashButtonListLayout>());
                            newProduct.setVat(0);
                            newProduct.setPercentage(perc);

                            map.put(newProduct, new ArrayList<>());

                        }*/

                    //}

                }
                else
                {
                    //product is not paid
                    if (product.getProductId() == -30)
                    {
                        Cursor c5 = database.rawQuery("SELECT * FROM product_unspec_bill WHERE prod_bill_id=" + product_line_id, null);
                        c5.moveToFirst();
                        product.setPrice(c5.getFloat(c5.getColumnIndex("price")));
                        product.setTitle(c5.getString(c5.getColumnIndex("description")));
                        product.setVat(StaticValue.staticVat);
                        c5.close();
                    }
                    else
                    {
                        Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                        c1.moveToFirst();
                        product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                        product.setTitle(c1.getString(c1.getColumnIndex("title")));
                        product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                        product.setPrinterId(c1.getInt(c1.getColumnIndex("printer")));
                        product.setIsPaid(false);
                        c1.close();
                    }
                   /* Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = "+product.getProductId(),null);
                    c1.moveToFirst();
                    product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                    product.setTitle(c1.getString(c1.getColumnIndex("title")));
                    product.setIsPaid(false);
                    c1.close();*/
                    Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                    mod_list = new ArrayList<>();
                    while (c3.moveToNext())
                    {
                        modifier = new CashButtonListLayout();
                        modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                        modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                        modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                        if (modifier.getModifierId() == -15)
                        {
                            Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                                    .getInt(c3.getColumnIndex("id")), null);
                            if (((c4 != null) && (c4.getCount() > 0)))
                            {
                                c4.moveToFirst();
                                modifier.setTitle("nota");
                                modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                                modifier.setPrice(0.0f);
                                modifier.setVat(StaticValue.staticVat);
                                mod_list.add(modifier);
                            }
                            c4.close();
                        }
                        else
                        {
                            Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                    .getModifierId(), null);
                            c2.moveToFirst();
                            modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                            modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                            modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                            mod_list.add(modifier);
                            c2.close();
                        }
                    }
                    map.put(product, mod_list);
                    c3.close();
                }
                cSubdivision1.close();
            }
            else
            {
                //no splitt, all no paid product

                if (product.getProductId() == -30)
                {
                    Cursor c5 = database.rawQuery("SELECT * FROM product_unspec_bill WHERE prod_bill_id=" + product_line_id, null);
                    c5.moveToFirst();
                    product.setPrice(c5.getFloat(c5.getColumnIndex("price")));
                    product.setTitle(c5.getString(c5.getColumnIndex("description")));
                    product.setVat(StaticValue.staticVat);
                    c5.close();
                }
                else if (product.getProductId() == -20)
                {
                    Cursor cCustomer1 = database.rawQuery("SELECT * FROM customer_bill WHERE prod_bill_id=" + product
                            .getID(), null);
                    if (cCustomer1.moveToNext())
                    {
                        cCustomer1.moveToFirst();
                        product.setPrice(0.0f);
                        product.setTitle(cCustomer1.getString(cCustomer1.getColumnIndex("description")));
                        product.setIsPaid(false);
                    }
                    cCustomer1.close();
                }
                else
                {
                    Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                    if (c1.moveToNext())
                    {
                        c1.moveToFirst();
                        product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                        product.setTitle(c1.getString(c1.getColumnIndex("title")));
                        product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                        product.setPrinterId(c1.getInt(c1.getColumnIndex("printer")));
                        product.setIsPaid(false);
                    }
                    c1.close();
                }
                Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                mod_list = new ArrayList<>();
                while (c3.moveToNext())
                {
                    modifier = new CashButtonListLayout();
                    modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                    modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                    modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                    if (modifier.getModifierId() == -15)
                    {


                        Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                                .getInt(c3.getColumnIndex("id")), null);
                        if (((c4 != null) && (c4.getCount() > 0)))
                        {
                            c4.moveToFirst();
                            modifier.setTitle("nota");
                            modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                            modifier.setPrice(0.0f);
                            mod_list.add(modifier);
                        }
                        c4.close();
                    }
                    else
                    {
                        Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                .getModifierId(), null);
                        c2.moveToFirst();
                        modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                        modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                        modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                        mod_list.add(modifier);
                        c2.close();
                    }
                }
                map.put(product, mod_list);
                c3.close();

            }
            cSubdivision.close();


        }
        c.close();
        database.close();
        /*int i=0;
        for (CashButtonLayout p: map.keySet()
             ) {
            Log.i(""+(i++),""+p.getTitle());
        }*/


        return map;
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getBillData2(int bill_id, Context context)
    {
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }
        LinkedHashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new LinkedHashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map1 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        ArrayList<CashButtonListLayout> mod_list;
        CashButtonLayout product;
        CashButtonListLayout modifier;
        //showData("product_bill");
        Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + bill_id + " ORDER BY cast(position as REAL) ASC;", null);
        while (c.moveToNext())
        {
            int product_line_id = c.getInt(c.getColumnIndex("id"));

            product = new CashButtonLayout();
            product.setID(c.getInt(c.getColumnIndex("id")));
            product.setProductId(c.getInt(c.getColumnIndex("prod_id")));
            product.setQuantity(c.getInt(c.getColumnIndex("qty")));
            int position = c.getInt(c.getColumnIndex("position"));
            product.setPosition(c.getInt(c.getColumnIndex("position")));
            product.setNewDiscount(c.getFloat(c.getColumnIndex("discount")));


            Cursor cCustomer = database.rawQuery("SELECT * FROM customer_bill WHERE prod_bill_id=" + product
                    .getID(), null);
            if (cCustomer.moveToNext())
            {
                product.setClientPosition(cCustomer.getInt(cCustomer.getColumnIndex("position")));

            }
            cCustomer.close();

            // showData("bill_subdivision_paid");
            Cursor cSubdivision = database.rawQuery("SELECT * FROM bill_subdivision_paid WHERE bill_id=" + bill_id + " ;", null);
            if (cSubdivision.moveToFirst())
            {
                int id = cSubdivision.getInt(0);
                //  showData("item_subdivisions");
                Cursor cSubdivision1 = database.rawQuery("SELECT * FROM item_subdivisions WHERE bill_subdivision_id=" + id + " AND product_bill_id=" + product_line_id + ";", null);
                if (cSubdivision1.moveToFirst())
                {
                    //product is already paid
                   /* Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = "+product.getProductId(),null);
                    c1.moveToFirst();
                    product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                    product.setTitle(c1.getString(c1.getColumnIndex("title")));
                    product.setIsPaid(true);
                    c1.close();*/
                    int perc = (cSubdivision1.getInt(cSubdivision1.getColumnIndex("percentage")));
                    if (perc == 1)
                    {
                        if (product.getProductId() == -30)
                        {
                            Cursor c5 = database.rawQuery("SELECT * FROM product_unspec_bill WHERE prod_bill_id=" + product_line_id, null);
                            c5.moveToFirst();
                            product.setPrice(c5.getFloat(c5.getColumnIndex("price")));
                            product.setTitle(c5.getString(c5.getColumnIndex("description")));
                            product.setVat(StaticValue.staticVat);
                            c5.close();
                        }
                        else
                        {
                            Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product
                                    .getProductId(), null);
                            c1.moveToFirst();
                            product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                            product.setTitle(c1.getString(c1.getColumnIndex("title")));
                            product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                            product.setIsPaid(false);
                            c1.close();
                        }
                        product.setIsPaid(true);
                        Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                        mod_list = new ArrayList<>();
                        while (c3.moveToNext())
                        {
                            modifier = new CashButtonListLayout();
                            modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                            modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                            modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                            if (modifier.getModifierId() == -15)
                            {
                                Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                                        .getInt(c3.getColumnIndex("id")), null);
                                if (((c4 != null) && (c4.getCount() > 0)))
                                {
                                    c4.moveToFirst();
                                    modifier.setTitle("nota");
                                    modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                                    modifier.setPrice(0.0f);
                                    modifier.setVat(StaticValue.staticVat);
                                    mod_list.add(modifier);
                                }
                                c4.close();
                            }
                            else
                            {
                                Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                        .getModifierId(), null);
                                c2.moveToFirst();
                                modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                                modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                                modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                                mod_list.add(modifier);
                                c2.close();
                            }
                        }
                        product.setNewCashList(mod_list);
                        map.put(product, mod_list);
                        c3.close();
                    }
                    else
                    {
                        Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                        c1.moveToFirst();
                        product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                        product.setTitle(c1.getString(c1.getColumnIndex("title")));
                        product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                        product.setIsPaid(false);
                        c1.close();
                        Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                        mod_list = new ArrayList<>();
                        while (c3.moveToNext())
                        {
                            modifier = new CashButtonListLayout();
                            modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                            modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                            modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                            if (modifier.getModifierId() == -15)
                            {
                                Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                                        .getInt(c3.getColumnIndex("id")), null);
                                if (((c4 != null) && (c4.getCount() > 0)))
                                {
                                    c4.moveToFirst();
                                    modifier.setTitle("nota");
                                    modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                                    modifier.setPrice(0.0f);
                                    modifier.setVat(StaticValue.staticVat);
                                    mod_list.add(modifier);
                                }
                                c4.close();
                            }
                            else
                            {
                                Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                        .getModifierId(), null);
                                c2.moveToFirst();
                                modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                                modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                                modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                                mod_list.add(modifier);
                                c2.close();
                            }
                        }
                        product.setNewCashList(mod_list);
                        map.put(product, mod_list);
                        c3.close();

                        float newCost = CalculatorFragment.roundDecimal((product.getPriceFloat() * product
                                .getQuantityInt()) / perc, 2);
                        float modsValue = 0.0f;
                        for (CashButtonListLayout mod : mod_list)
                        {
                            modsValue += mod.getPriceFloat() * mod.getQuantityInt();
                        }
                        newCost += modsValue / perc;
                        product.setSelected(true);
                        //set product so doesn't show anymore


                        showData("item_subdivisions");
                        Log.i("MY PRODUCT", "" + product.getID());
                        Cursor ccount = database.rawQuery("SELECT COUNT(id) AS count FROM item_subdivisions WHERE product_bill_id = " + product
                                .getID() /*+" AND ABS(price - "+newCost+") < 0.00000005 "*/, null);
                        int count = 0;
                        while (ccount.moveToNext())
                        {
                            count = ccount.getInt(ccount.getColumnIndex("count"));
                        }
                        ccount.close();
                        for (int i = 1; i <= perc; i++)
                        {
                            if (i == perc)
                            {
                                if ((newCost * perc) != product.getPriceFloat())
                                {
                                    newCost = ((product.getPriceFloat() * product.getQuantityInt()) + modsValue) - (newCost * (perc - 1));
                                }
                            }

                            showData("item_subdivisions");
                            Cursor c8 = database.rawQuery("SELECT * FROM item_subdivisions WHERE product_bill_id = " + product
                                    .getID() + " AND ABS(price - " + newCost + ") < 0.00000005 ", null);
                            int total = 0;
                            while (c8.moveToNext())
                            {
                                total = 1;
                            }
                            c8.close();

                            CashButtonLayout newProduct = new CashButtonLayout();
                            if (product.getQuantityInt() == 1)
                            { newProduct.setTitle(product.getTitle() + " 1/" + perc); }
                            else
                            { newProduct.setTitle(product.getTitle() + " x " + product.getQuantityInt() + " 1/" + perc); }
                            newProduct.setPrice(newCost);
                            newProduct.setQuantity(1);
                            newProduct.setProductId(-30);
                            newProduct.setIsDelete(false);
                            newProduct.setModifyModifier(false);
                            newProduct.setID(product.getID());
                            newProduct.setNewDiscount(0.0f);
                            newProduct.setClientPosition(product.getClientPosition());
                            newProduct.setPosition(position + i);
                            newProduct.setNewCashList(new ArrayList<CashButtonListLayout>());
                            newProduct.setVat(0);
                            newProduct.setPercentage(perc);
                            if (i > count)
                            {
                                newProduct.setSelected(false);
                            }
                            else
                            {
                                newProduct.setSelected(true);
                            }
                            //newProduct.setSelected(false);


                            map.put(newProduct, new ArrayList<>());

                        }

                    }

                }
                else
                {
                    //product is not paid
                    if (product.getProductId() == -30)
                    {
                        Cursor c5 = database.rawQuery("SELECT * FROM product_unspec_bill WHERE prod_bill_id=" + product_line_id, null);
                        c5.moveToFirst();
                        product.setPrice(c5.getFloat(c5.getColumnIndex("price")));
                        product.setTitle(c5.getString(c5.getColumnIndex("description")));
                        product.setVat(StaticValue.staticVat);
                        c5.close();
                    }
                    else
                    {
                        Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                        c1.moveToFirst();
                        product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                        product.setTitle(c1.getString(c1.getColumnIndex("title")));
                        product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                        product.setIsPaid(false);
                        c1.close();
                    }
                   /* Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = "+product.getProductId(),null);
                    c1.moveToFirst();
                    product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                    product.setTitle(c1.getString(c1.getColumnIndex("title")));
                    product.setIsPaid(false);
                    c1.close();*/
                    Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                    mod_list = new ArrayList<>();
                    while (c3.moveToNext())
                    {
                        modifier = new CashButtonListLayout();
                        modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                        modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                        modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                        if (modifier.getModifierId() == -15)
                        {
                            Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                                    .getInt(c3.getColumnIndex("id")), null);
                            if (((c4 != null) && (c4.getCount() > 0)))
                            {
                                c4.moveToFirst();
                                modifier.setTitle("nota");
                                modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                                modifier.setPrice(0.0f);
                                modifier.setVat(StaticValue.staticVat);
                                mod_list.add(modifier);
                            }
                            c4.close();
                        }
                        else
                        {
                            Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                    .getModifierId(), null);
                            c2.moveToFirst();
                            modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                            modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                            modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                            mod_list.add(modifier);
                            c2.close();
                        }
                    }
                    product.setNewCashList(mod_list);
                    map.put(product, mod_list);
                    c3.close();
                }
            }
            else
            {
                //no splitt, all no paid product

                if (product.getProductId() == -30)
                {
                    Cursor c5 = database.rawQuery("SELECT * FROM product_unspec_bill WHERE prod_bill_id=" + product_line_id, null);
                    c5.moveToFirst();
                    product.setPrice(c5.getFloat(c5.getColumnIndex("price")));
                    product.setTitle(c5.getString(c5.getColumnIndex("description")));
                    product.setVat(StaticValue.staticVat);
                    c5.close();
                }
                else if (product.getProductId() == -20)
                {
                    Cursor cCustomer1 = database.rawQuery("SELECT * FROM customer_bill WHERE prod_bill_id=" + product
                            .getID(), null);
                    if (cCustomer1.moveToNext())
                    {
                        cCustomer1.moveToFirst();
                        product.setPrice(0.0f);
                        product.setTitle(cCustomer1.getString(cCustomer1.getColumnIndex("description")));
                        product.setIsPaid(false);
                    }
                    cCustomer1.close();
                }
                else
                {
                    Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                    if (c1.moveToNext())
                    {
                        c1.moveToFirst();
                        product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                        product.setTitle(c1.getString(c1.getColumnIndex("title")));
                        product.setVat(c1.getInt(c1.getColumnIndex("vat")));
                        product.setIsPaid(false);
                    }
                    c1.close();
                }
                Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                mod_list = new ArrayList<>();
                while (c3.moveToNext())
                {
                    modifier = new CashButtonListLayout();
                    modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                    modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                    modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));
                    if (modifier.getModifierId() == -15)
                    {


                        Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                                .getInt(c3.getColumnIndex("id")), null);
                        if (((c4 != null) && (c4.getCount() > 0)))
                        {
                            c4.moveToFirst();
                            modifier.setTitle("nota");
                            modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                            modifier.setPrice(0.0f);
                            mod_list.add(modifier);
                        }
                        c4.close();
                    }
                    else
                    {
                        Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                .getModifierId(), null);
                        c2.moveToFirst();
                        modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                        modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                        modifier.setVat(c2.getInt(c2.getColumnIndex("vat")));
                        mod_list.add(modifier);
                        c2.close();
                    }
                }
                product.setNewCashList(mod_list);
                map.put(product, mod_list);
                c3.close();

            }
            cSubdivision.close();


        }
        c.close();
        database.close();
        /*int i=0;
        for (CashButtonLayout p: map.keySet()
             ) {
            Log.i(""+(i++),""+p.getTitle());
        }*/


        return map;
    }


    public Map<CashButtonLayout, ArrayList<CashButtonListLayout>> getBillData1(int bill_id, Context context)
    {
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }
        LinkedHashMap<CashButtonLayout, ArrayList<CashButtonListLayout>> map = new LinkedHashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();

        Map<CashButtonLayout, ArrayList<CashButtonListLayout>> map1 = new HashMap<CashButtonLayout, ArrayList<CashButtonListLayout>>();
        ArrayList<CashButtonListLayout> mod_list;
        CashButtonLayout product;
        CashButtonListLayout modifier;


        Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + bill_id + " ORDER BY cast(position as REAL) ASC;", null);
        while (c.moveToNext())
        {
            int product_line_id = c.getInt(c.getColumnIndex("id"));

            product = new CashButtonLayout();
            product.setID(c.getInt(c.getColumnIndex("id")));
            product.setProductId(c.getInt(c.getColumnIndex("prod_id")));
            product.setQuantity(c.getInt(c.getColumnIndex("qty")));
            int position = c.getInt(c.getColumnIndex("position"));
            product.setPosition(c.getInt(c.getColumnIndex("position")));
            //no splitt, all no paid product

            if (product.getProductId() == -20)
            {
                Cursor cCustomer1 = database.rawQuery("SELECT * FROM customer_bill WHERE prod_bill_id=" + product
                        .getID(), null);
                if (cCustomer1.moveToNext())
                {
                    cCustomer1.moveToFirst();
                    product.setPrice(0.0f);
                    product.setTitle(cCustomer1.getString(cCustomer1.getColumnIndex("description")));
                    product.setIsPaid(false);
                }
                cCustomer1.close();
            }
            else
            {
                Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = " + product.getProductId(), null);
                if (c1.moveToNext())
                {
                    c1.moveToFirst();
                    product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                    product.setTitle(c1.getString(c1.getColumnIndex("title")));
                    product.setIsPaid(false);
                }
                c1.close();
            }

             /*   Cursor c1 = database.rawQuery("SELECT * FROM button WHERE id = "+product.getProductId(),null);
                c1.moveToFirst();
                product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                product.setTitle(c1.getString(c1.getColumnIndex("title")));
                product.setIsPaid(false);
                c1.close();*/
            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
            mod_list = new ArrayList<>();
            while (c3.moveToNext())
            {
                modifier = new CashButtonListLayout();
                modifier.setID(c3.getInt(c3.getColumnIndex("id")));
                modifier.setModifierId(c3.getInt(c3.getColumnIndex("mod_id")));
                modifier.setQuantity(c3.getInt(c3.getColumnIndex("qty")));

                if (modifier.getModifierId() == -15)
                {
                    Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c3
                            .getInt(c3.getColumnIndex("id")), null);
                    if (((c4 != null) && (c4.getCount() > 0)))
                    {
                        c4.moveToFirst();
                        modifier.setTitle("nota");
                        modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                        modifier.setPrice(0.0f);
                        mod_list.add(modifier);
                    }
                    c4.close();
                }
                else
                {
                    Cursor c2 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier.getModifierId(), null);
                    c2.moveToFirst();
                    modifier.setTitle(c2.getString(c2.getColumnIndex("title")));
                    modifier.setPrice(c2.getFloat(c2.getColumnIndex("price")));
                    mod_list.add(modifier);
                    c2.close();
                }
            }
            map.put(product, mod_list);
            c3.close();


        }
        c.close();
        database.close();
        /*int i=0;
        for (CashButtonLayout p: map.keySet()
             ) {
            Log.i(""+(i++),""+p.getTitle());
        }*/


        return map;
    }


    public Double getBillPrice(int billId)
    {
        Double price = 0.0;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT total FROM bill_total WHERE id=" + billId + " LIMIT 1;", null);
        while (c.moveToNext())
        {
            price = c.getDouble(c.getColumnIndex("total"));


            Cursor c1 = database.rawQuery("SELECT paid_amount FROM bill_subdivision_paid WHERE bill_id=" + billId + " ;", null);
            while (c1.moveToNext())
            {
                price = price - c1.getDouble(c1.getColumnIndex("paid_amount"));

            }
            c1.close();

        }
        c.close();
        database.close();


        return price;
    }


    public int getBillPayment(int billId)
    {
        int payment = 0;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT payment_type FROM bill_total WHERE id=" + billId + " LIMIT 1;", null);
        while (c.moveToNext())
        {
            payment = c.getInt(c.getColumnIndex("payment_type"));


        }
        c.close();
        database.close();


        return payment;
    }


    public Double getOnlyBillPrice(int billId)
    {
        Double price = 0.0;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT total FROM bill_total WHERE id=" + billId + " LIMIT 1;", null);
        while (c.moveToNext())
        {
            price = c.getDouble(c.getColumnIndex("total"));
        }
        c.close();
        database.close();

        return price;
    }


    public boolean checkIfBillSplitPaid(int billId)
    {
        boolean check = false;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c1 = database.rawQuery("SELECT id FROM bill_subdivision_paid WHERE bill_id=" + billId + " ;", null);
        while (c1.moveToNext())
        {
            check = true;
        }
        c1.close();
        database.close();

        return check;

    }


    public boolean checkIfBillIsPaid(int billId)
    {
        boolean check = false;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c1 = database.rawQuery("SELECT id FROM bill_total WHERE id=" + billId + " AND paid=1;", null);
        while (c1.moveToNext())
        {
            check = true;
        }
        c1.close();
        database.close();

        return check;

    }


    public void updateBillPrice(int billId, double price)
    {
        Double price1 = 0.0;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c1 = database.rawQuery("SELECT paid_amount FROM bill_subdivision_paid WHERE bill_id=" + billId + " ;", null);
        while (c1.moveToNext())
        {
            //    price= price+c1.getDouble(c1.getColumnIndex("paid_amount"));
        }
        c1.close();

        try
        {
            if (database.isOpen()) { database.close(); }
            database = dbHelper.getWritableDatabase();
            database.execSQL("UPDATE bill_total SET total=" + price + " WHERE id=" + billId + ";");
            //  database.close();
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());

        }
        database.close();

    }


    public void updateBillPaid(int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total SET paid=" + true + " WHERE id=" + billId + ";");
            //    database.close();
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());

        }
        database.close();

    }


    public void deleteBillData1(int bill_id, Context context)
    {
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE id=" + bill_id, null);
        if (c.moveToFirst())
        {
            int billId = c.getInt(c.getColumnIndex("id"));

            Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billId, null);
            c1.moveToFirst();

            while (c1.moveToNext())
            {
                int productBillId = c1.getInt(c1.getColumnIndex("id"));
                database.execSQL("DELETE FROM modifier_bill WHERE prod_bill_id=" + productBillId);
            }
            database.execSQL("DELETE FROM product_bill WHERE bill_id=" + billId);
            database.execSQL("DELETE FROM temp_table WHERE total_bill_id=" + billId);
            c1.close();
            database.execSQL("DELETE FROM bill_total WHERE id=" + billId);
        }

        c.close();
        database.close();

    }


    public void deleteBillData(int bill_id, Context context)
    {
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE bill_number=" + bill_id, null);
        if (c.moveToFirst())
        {
            int billId = c.getInt(c.getColumnIndex("id"));

            Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billId, null);
            c1.moveToFirst();

            while (c1.moveToNext())
            {
                int productBillId = c1.getInt(c1.getColumnIndex("id"));
                database.execSQL("DELETE FROM modifier_bill WHERE prod_bill_id=" + productBillId);
            }
            database.execSQL("DELETE FROM product_bill WHERE bill_id=" + billId);
            database.execSQL("DELETE FROM temp_table WHERE total_bill_id=" + billId);
            c1.close();
            database.execSQL("DELETE FROM bill_total WHERE id=" + billId);
        }

        c.close();
        database.close();

    }


    public int getBillTotalId(int bill_number)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT * FROM bill_total WHERE " +
                    "bill_number =" + bill_number + ";", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    id = mCursor.getInt(0);//to get id, 0 is the column index

                }
                mCursor.close();
            }
            database.close();
            return id;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public int getBillTotalNumberById(int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int bill_number = -1;
            Cursor mCursor = database.rawQuery("SELECT bill_number FROM bill_total WHERE " +
                    "id =" + id + ";", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    bill_number = mCursor.getInt(0);//to get id, 0 is the column index

                }
                mCursor.close();
            }
            database.close();
            return bill_number;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    /**
     * checks if bill is paid
     *
     * @param billId
     * @return
     */
    public int getPaidBill(int billId)
    {
        int paid = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM bill_total WHERE id=" + billId, null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    paid = mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_PAID));

                }
                mCursor.close();
            }
            database.close();
            return paid;
        }
        catch (Exception e)
        {
            Log.d("Get error", e.getMessage());
            return paid;
        }
    }


    public long returnBillLastDate()
    {
        long toReturn = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            Cursor mCursor = database.rawQuery("SELECT MAX(creation_time) FROM bill_total WHERE paid=0", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    toReturn = Timestamp.valueOf(mCursor.getString(0)).getTime();

                }
                mCursor.close();
            }
            mCursor.close();
            database.close();
            return toReturn;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public int getMaxNumberOrderId(long sessionStart)
    {
        Date date = new Date(sessionStart);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateText = df2.format(date);
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT bill_number FROM bill_total WHERE " +
                    "creation_time =(" +
                    "SELECT MAX(creation_time) FROM bill_total WHERE paid=0 and  creation_time >'" + dateText + "')" +
                    "ORDER BY bill_number DESC LIMIT 1;", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    id = mCursor.getInt(0);//to get id, 0 is the column index
                }
                mCursor.close();
            }
            mCursor.close();
            database.close();
            return id;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public int getMaxOrderId(long sessionStart)
    {
        Date date = new Date(sessionStart);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateText = df2.format(date);
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT bill_number FROM bill_total WHERE " +
                    "creation_time =(" +
                    " SELECT MAX(creation_time) FROM bill_total WHERE creation_time >'" + dateText + "' ) " +
                    "ORDER BY bill_number DESC LIMIT 1;", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    id = mCursor.getInt(0);//to get id, 0 is the column index
                }
                mCursor.close();
            }
            mCursor.close();
            database.close();
            return id;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public void updateClosingTime()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO fiscal_close(last_close) VALUES(datetime(CURRENT_TIMESTAMP, 'localtime')) ");
            database.execSQL("DELETE FROM product_bill WHERE bill_id=-1");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }


    public long getLastClosing()
    {
        long toReturn = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT MAX(last_close) FROM fiscal_close;", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    toReturn = Timestamp.valueOf(mCursor.getString(0))
                                        .getTime();//to get id, 0 is the column index


                }
                mCursor.close();
            }
            mCursor.close();
            database.close();
            return toReturn;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public ArrayList<TotalBill> getBillsList(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<TotalBill> array = new ArrayList<>();
            //Log.d("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    TotalBill c = new TotalBill();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    c.setId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTotal(mCursor.getFloat(mCursor.getColumnIndex(DatabaseAdapter.KEY_TOTAL)));
                    c.setPaid(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_PAID)));
                    c.setBillNumber(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_BILL_NUMBER)));

                    String current = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_CREATION_TIME));
                    Date currentDate = format.parse(current);
                    //Date date = new Date(mCursor.getLong(mCursor.getColumnIndex(DatabaseAdapter.KEY_CREATION_TIME))*1000);
                    c.setCreationTime(currentDate);

                    String pay = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_PAID_TIME));
                    if (pay != null)
                    {
                        Date payDate = format.parse(pay);
                        c.setPaidTime(payDate);
                    }

                    array.add(c);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return null;
        }
    }


    public boolean savePaidBill(SubdivisionItem item, int bill_id, int paymentType)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            if (item != null)
            {
                float subdivision_value;
                if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE)
                { subdivision_value = 0; }
                else if (item.getMode() == NUMBER_MODE)
                { subdivision_value = item.getNumber_subdivision(); }
                else { subdivision_value = item.getPercentage(); }

                float itemDiscount = 0.0f;
                for (CashButtonLayout prod : item.getItems())
                {
                    itemDiscount += prod.getDiscount();
                }
                database.execSQL("INSERT INTO bill_subdivision_paid (bill_id, subdivision_mode, subdivision_value, paid_amount, payment_type, discount) " +
                        "VALUES(" + bill_id + "," + item.getMode() + "," + subdivision_value + "," + item
                        .getOwed_money() + "," + paymentType + "," + (item.getDiscount() + itemDiscount) + ");");
                if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE)
                {
                    Cursor c = database.rawQuery("SELECT id FROM bill_subdivision_paid ORDER BY id DESC", null);
                    c.moveToFirst();
                    int subd_id = c.getInt(0);
                    for (CashButtonLayout p : item.getItems()
                    )
                    {

                        database.execSQL("INSERT INTO item_subdivisions (bill_subdivision_id, product_bill_id,quantity, discount, percentage, price) " +
                                "VALUES(" + subd_id + "," + p.getID() + "," + p.getQuantityInt() + "," + p
                                .getDiscount() + "," + p.getPercentage() + "," + p.getPriceFloat() + ");");
                    }
                    c.close();

                }
            }
            else
            {
                // saves the whole bill as paid == updates the pay_time to current time
                database.execSQL("UPDATE bill_total SET pay_time = datetime(CURRENT_TIMESTAMP, 'localtime') , paid=" + 1 + ", payment_type=" + paymentType + " WHERE id = " + bill_id);
                database.execSQL("UPDATE table_use SET end_time = datetime(CURRENT_TIMESTAMP, 'localtime') WHERE total_bill_id = " + bill_id);

                // deletes information regarding bill subdivisions linked to the current bill_id
                ArrayList<Integer> subd_ids = new ArrayList();
                Cursor c = database.rawQuery("SELECT id FROM bill_subdivision_paid WHERE bill_id =" + bill_id, null);
                while (c.moveToNext()) { subd_ids.add(c.getInt(0)); }
                c.close();
                /**
                 database.execSQL("DELETE FROM bill_subdivision_paid WHERE bill_id = "+bill_id);
                 for (int i: subd_ids
                 ) {
                 database.execSQL("DELETE FROM item_subdivisions WHERE bill_subdivision_id = "+i);
                 }
                 */

            }
            database.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }


    public void azzeramentoScontrini()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total SET pay_time = datetime(CURRENT_TIMESTAMP, 'localtime') , paid=" + 4 + " WHERE paid= " + 0);
            database.execSQL("UPDATE table_use SET end_time = datetime(CURRENT_TIMESTAMP, 'localtime')");
            database.execSQL("DELETE FROM product_bill WHERE bill_id=-1");
            database.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }


    public void deleteWrongProduct()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM product_bill WHERE bill_id=-1 OR bill_id=-11");
            database.execSQL("DELETE FROM bill_total WHERE id=-11 OR id=-1");
            database.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }


    public boolean savePaidBillInvoice(SubdivisionItem item, int bill_id, int paymentType, int clientId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            if (item != null)
            {
                float subdivision_value;
                if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE)
                { subdivision_value = 0; }
                else if (item.getMode() == NUMBER_MODE)
                { subdivision_value = item.getNumber_subdivision(); }
                else { subdivision_value = item.getPercentage(); }

                float itemDiscount = 0.0f;
                for (CashButtonLayout prod : item.getItems())
                {
                    itemDiscount += prod.getDiscount();
                }
                database.execSQL("INSERT INTO bill_subdivision_paid (bill_id, subdivision_mode, subdivision_value, paid_amount, payment_type, discount, invoice) " +
                        "VALUES(" + bill_id + "," + item.getMode() + "," + subdivision_value + "," + item
                        .getOwed_money() + "," + paymentType + "," + (item.getDiscount() + itemDiscount) + "," + 1 + ");");
                if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE)
                {
                    Cursor c = database.rawQuery("SELECT id FROM bill_subdivision_paid ORDER BY id DESC", null);
                    c.moveToFirst();
                    int subd_id = c.getInt(0);
                    for (CashButtonLayout p : item.getItems()
                    )
                    {

                        database.execSQL("INSERT INTO item_subdivisions (bill_subdivision_id, product_bill_id,quantity, discount, percentage, price) " +
                                "VALUES(" + subd_id + "," + p.getID() + "," + p.getQuantityInt() + "," + p
                                .getDiscount() + "," + p.getPercentage() + "," + p.getPriceFloat() + ");");
                    }
                    c.close();

                }
            }
            else
            {
                // saves the whole bill as paid == updates the pay_time to current time
                database.execSQL("UPDATE bill_total SET pay_time = datetime(CURRENT_TIMESTAMP, 'localtime') , paid=" + 1 + ", payment_type=" + paymentType + " , invoice=" + 1 + " WHERE id = " + bill_id);
                database.execSQL("UPDATE table_use SET end_time = datetime(CURRENT_TIMESTAMP, 'localtime') WHERE total_bill_id = " + bill_id);

                // deletes information regarding bill subdivisions linked to the current bill_id
                ArrayList<Integer> subd_ids = new ArrayList();
                Cursor c = database.rawQuery("SELECT id FROM bill_subdivision_paid WHERE bill_id =" + bill_id, null);
                while (c.moveToNext()) { subd_ids.add(c.getInt(0)); }
                c.close();
                /**
                 database.execSQL("DELETE FROM bill_subdivision_paid WHERE bill_id = "+bill_id);
                 for (int i: subd_ids
                 ) {
                 database.execSQL("DELETE FROM item_subdivisions WHERE bill_subdivision_id = "+i);
                 }
                 */

            }
            database.execSQL("INSERT INTO bill_total_customer_invoice (bill_total_id, client_id) " +
                    "VALUES(" + bill_id + "," + clientId + ");");
            database.close();

            //database.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }


    public boolean savePaidBillForLeftPayment(SubdivisionItem item, int bill_id, int paymentType)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            if (item != null)
            {
                float subdivision_value;
                if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE)
                { subdivision_value = 0; }
                else if (item.getMode() == NUMBER_MODE)
                { subdivision_value = item.getNumber_subdivision(); }
                else if (item.getMode() == PERCENTAGE_MODE)
                { subdivision_value = item.getPercentage(); }
                else { subdivision_value = 100; }

                float itemDiscount = 0.0f;
                for (CashButtonLayout prod : item.getItems())
                {
                    itemDiscount += prod.getDiscount();
                }
                database.execSQL("INSERT INTO bill_subdivision_paid (bill_id, subdivision_mode, subdivision_value, paid_amount, payment_type, discount) " +
                        "VALUES(" + bill_id + "," + item.getMode() + "," + subdivision_value + "," + item
                        .getOwed_money() + "," + paymentType + "," + (item.getDiscount() + itemDiscount) + ");");
                if (item.getMode() == ITEM_MODE || item.getMode() == PERSON_MODE)
                {
                    Cursor c = database.rawQuery("SELECT id FROM bill_subdivision_paid ORDER BY id DESC", null);
                    c.moveToFirst();
                    int subd_id = c.getInt(0);
                    for (CashButtonLayout p : item.getItems()
                    )
                    {
                        database.execSQL("INSERT INTO item_subdivisions (bill_subdivision_id, product_bill_id,quantity, discount, percentage, price) " +
                                "VALUES(" + subd_id + "," + p.getID() + "," + p.getQuantityInt() + "," + p
                                .getDiscount() + "," + p.getPercentage() + "," + p.getPriceFloat() + ");");
                    }
                    c.close();
                }

                if (item.getMode() == -1)
                {
                    database.execSQL("UPDATE bill_total SET pay_time = datetime(CURRENT_TIMESTAMP, 'localtime') , paid=" + 1 + ", payment_type=" + paymentType + " WHERE id = " + bill_id);
                    database.execSQL("UPDATE table_use SET end_time = datetime(CURRENT_TIMESTAMP, 'localtime') WHERE total_bill_id = " + bill_id);
                }
                // deletes information regarding bill subdivisions linked to the current bill_id


            }
            database.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }


    //saves bill as homage
    public void savePaidBillHomage(int billId, float discount)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM bill_total " +
                    "WHERE id=" + billId + ";", null);
            if (mCursor != null)
            {
                database.execSQL("INSERT INTO bill_total_extra (discountTotal, discountValue, homage, bill_total_id)" +
                        "VALUES(" + discount + "," + discount + "," + 1 + "," + billId + ");");
            }
            mCursor.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("saveFailure", e.getMessage());
        }
    }


    public ArrayList<SubdivisionItem> getBillSplits(int billId)
    {
        ArrayList<SubdivisionItem> items = new ArrayList<>();
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }
        //showData("bill_subdivision_paid");
        database = dbHelper.getWritableDatabase();
        Cursor c = database.rawQuery("SELECT * FROM bill_subdivision_paid WHERE bill_id = " + billId, null);

        /**
         * For each bill split found an item is created and added to the list.
         */
        while (c.moveToNext())
        {
            SubdivisionItem item = new SubdivisionItem();
            int mode = c.getInt(c.getColumnIndex("subdivision_mode"));
            item.setDiscount(c.getFloat(c.getColumnIndex("discount")));
            item.setMode(mode);
            item.setPaymentType(c.getInt(c.getColumnIndex("payment_type")));
            if (mode == PERCENTAGE_MODE)
            { item.setPercentage(c.getFloat(c.getColumnIndex("subdivision_value"))); }

            else if (mode == NUMBER_MODE)
            { item.setNumber_subdivision(c.getInt(c.getColumnIndex("subdivision_value"))); }

            else
            {
                /**
                 * ITEM MODE:
                 * for each bill_subdivision done with "item mode" create one item and sets its CashButtonLayouts up
                 * with the selected qty specified in item_subdivisions
                 */
                int bill_subdivision_id = c.getInt(c.getColumnIndex("id"));
                ArrayList<CashButtonLayout> products = new ArrayList<>();
                ArrayList<CashButtonListLayout> mod_list;
                CashButtonListLayout modifier;
                HashMap<CashButtonLayout, Integer> map = new HashMap<>();
                Cursor c1 = database.rawQuery("SELECT * FROM item_subdivisions " +
                        "WHERE bill_subdivision_id = " + bill_subdivision_id, null);

                while (c1.moveToNext())
                {
                    int product_line_id = c1.getInt(c1.getColumnIndex("product_bill_id"));

                    //showData("product_bill");
                    Cursor c2 = database.rawQuery("SELECT * FROM product_bill WHERE id = " + product_line_id, null);

                    while (c2.moveToNext())
                    {
                        CashButtonLayout product = new CashButtonLayout();
                        Cursor cCustomer = database.rawQuery("SELECT * FROM customer_bill WHERE prod_bill_id=" + product_line_id, null);
                        if (cCustomer.moveToNext())
                        {
                            Integer position = cCustomer.getInt(cCustomer.getColumnIndex("position"));
                            product.setClientPosition(position);
                            cCustomer.close();
                        }

                        product.setID(product_line_id);
                        product.setProductId(c2.getInt(c2.getColumnIndex("prod_id")));
                        //product.setQuantity(c2.getInt(c2.getColumnIndex("qty")));
                        product.setQuantity(c1.getInt(c1.getColumnIndex("quantity")));
                        //product.setNewDiscount(c2.getFloat(c2.getColumnIndex("discount")));
                        product.setNewDiscount(c1.getFloat(c1.getColumnIndex("discount")));
                        if (product.getProductId() == -30)
                        {
                            Cursor c5 = database.rawQuery("SELECT * FROM product_unspec_bill WHERE prod_bill_id=" + product_line_id, null);
                            c5.moveToFirst();
                            product.setPrice(c5.getFloat(c5.getColumnIndex("price")));
                            product.setTitle(c5.getString(c5.getColumnIndex("description")));
                            product.setVat(StaticValue.staticVat);
                            c5.close();
                        }
                        else
                        {
                            int percentage = c1.getInt(c1.getColumnIndex("percentage"));
                            Cursor c3 = database.rawQuery("SELECT * FROM button WHERE id = " + product
                                    .getProductId(), null);
                            c3.moveToFirst();
                            if (percentage == 1)
                            {
                                product.setPrice(c3.getFloat(c3.getColumnIndex("price")));
                                product.setTitle(c3.getString(c3.getColumnIndex("title")));
                                product.setPercentage(1);
                                product.setVat(c3.getInt(c3.getColumnIndex("vat")));
                            }
                            else
                            {
                                product.setPrice(c1.getFloat(c1.getColumnIndex("price")));
                                product.setTitle(c3.getString(c3.getColumnIndex("title")) + "1/" + percentage);
                                product.setProductId(-30);
                                product.setPercentage(c1.getInt(c1.getColumnIndex("percentage")));
                                product.setVat(StaticValue.staticVat);
                            }
                            c3.close();


                        }

                        Cursor c5 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id = " + product_line_id + " ORDER BY id", null);
                        mod_list = new ArrayList<>();
                        while (c5.moveToNext())
                        {
                            modifier = new CashButtonListLayout();
                            modifier.setID(c5.getInt(c5.getColumnIndex("id")));
                            modifier.setModifierId(c5.getInt(c5.getColumnIndex("mod_id")));
                            modifier.setQuantity(c5.getInt(c5.getColumnIndex("qty")));
                            if (modifier.getModifierId() == -15)
                            {
                                Cursor c4 = database.rawQuery("SELECT * FROM modifier_bill_notes WHERE modifier_bill_id = " + c5
                                        .getInt(c5.getColumnIndex("id")), null);
                                if (((c4 != null) && (c4.getCount() > 0)))
                                {
                                    c4.moveToFirst();
                                    modifier.setTitle("nota");
                                    modifier.setNote(c4.getString(c4.getColumnIndex("note")));
                                    modifier.setPrice(0.0f);
                                    modifier.setVat(StaticValue.staticVat);
                                    mod_list.add(modifier);
                                }
                                c4.close();
                            }
                            else
                            {
                                Cursor c6 = database.rawQuery("SELECT * FROM modifier WHERE id = " + modifier
                                        .getModifierId(), null);
                                c6.moveToFirst();
                                modifier.setTitle(c6.getString(c6.getColumnIndex("title")));
                                modifier.setPrice(c6.getFloat(c6.getColumnIndex("price")));
                                modifier.setVat(c6.getInt(c6.getColumnIndex("vat")));
                                mod_list.add(modifier);
                                c6.close();
                            }
                        }
                        //map.put(product, mod_list);
                        product.setNewCashList(mod_list);
                        c5.close();


                        products.add(product);
                        map.put(product, c1.getInt(c1.getColumnIndex("quantity")));
                        /**
                         * There is no need to adding the CashButtonListLayouts related to the current CashButtonLayout:
                         * they are stored in the OrderListAdapter.
                         */
                    }
                    c2.close();
                }
                c1.close();
                item.setItems(products);
                item.setItems_map(map);
            }
            item.setOwed_money(c.getFloat(c.getColumnIndex("paid_amount")));
            item.setPaid(true);
            items.add(item);
        }
        c.close();
        database.close();

        return items;
    }


    /**
     * if there are item_subdivisions whit specific bill_subdivision_paid id, then it deletes everything in item_subdivisions
     *
     * @param billId
     */
    public void deleteSubdivisionItems(int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            int sub_id = -1;
            Cursor c = database.rawQuery("SELECT * FROM bill_subdivision_paid WHERE bill_id = " + billId, null);
            if (c != null)
            {
                if (c.moveToFirst())
                {
                    sub_id = c.getInt(c.getColumnIndex("id"));
                    database.execSQL("DELETE FROM item_subdivisions WHERE id=" + sub_id + ";");
                }
            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }


    public int getLatestBillId()
    {
        try
        {
            int billId = -1;
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            ArrayList<TotalBill> bills = getBillsList("SELECT * FROM bill_total ORDER BY id DESC");

            if (!bills.isEmpty())
            {
                billId = bills.get(0).getId();
            }

            database.close();
            return billId;
        }
        catch (Exception e)
        {
            Log.d("FetchFailure.", e.getMessage());
            return -1;
        }
    }


    public int getLatestBillNumber()
    {
        try
        {
            int billNum = -1;
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            ArrayList<TotalBill> bills = getBillsList("SELECT * FROM bill_total ORDER BY bill_number DESC");

            if (!bills.isEmpty())
            {
                billNum = bills.get(0).getBillNumber();
            }

            database.close();
            return billNum;
        }
        catch (Exception e)
        {
            Log.d("FetchFailure.", e.getMessage());
            return -1;
        }
    }




    // =============================================== //
    // [ PRODUCT BILLS ]
    // =============================================== //


    public void deleteLeftProductFromBill(Integer billId, Integer position)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * from product_bill where  bill_id=" + billId + " AND position>" + position, null);
            while (c.moveToNext())
            {
                //name = cursor.getString(column_index);//to get other values
                database.execSQL("delete from modifier_bill where prod_bill_id=" + c.getInt(0));
                database.execSQL("delete from product_bill where id=" + c.getInt(0));
                database.execSQL("delete from product_unspec_bill where prod_bill_id=" + c.getInt(0));
                database.execSQL("delete from customer_bill where prod_bill_id=" + c.getInt(0));

            }
            c.close();

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
    }


    public void deleteProductFromBill(Integer billId, Integer position)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * from product_bill where  bill_id=" + billId, null);
            while (c.moveToNext())
            {
                //name = cursor.getString(column_index);//to get other values
                int posi = c.getInt(1);
                if (c.getInt(1) == position)
                {
                    database.execSQL("delete from modifier_bill where prod_bill_id=" + c.getInt(0));
                    database.execSQL("delete from product_bill where id=" + c.getInt(0));
                    database.execSQL("delete from product_unspec_bill where prod_bill_id=" + c.getInt(0));
                    database.execSQL("delete from customer_bill where prod_bill_id=" + c.getInt(0));
                }
                else if (c.getInt(1) > position)
                {
                    // database.execSQL("delete from product_bill where bill_id="+billId);
                    database.execSQL("UPDATE product_bill SET position=" + (c.getInt(1) - 1) + " WHERE id=" + c
                            .getInt(0) + ";");
                }
                float cfloat = c.getFloat(6);
                if (c.getFloat(6) > 0.0f)
                {
                    Cursor c2 = database.rawQuery("SELECT * from bill_total_extra where  bill_total_id=" + billId, null);
                    while (c2.moveToNext())
                    {
                        float newDiscount = c2.getFloat(1) - c.getFloat(6);
                        if (newDiscount > 0)
                        {
                            database.execSQL("UPDATE bill_total_extra SET discountTotal=" + newDiscount + " WHERE bill_total_id=" + billId + ";");
                        }
                        else
                        { database.execSQL("UPDATE bill_total_extra SET discountTotal=" + 0.0 + " WHERE bill_total_id=" + billId + ";"); }
                    }
                    c2.close();

                }
            }
            c.close();

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
    }


    public void deleteModifierFromBill(Integer billId, Integer position, Integer prodId, Integer groupPosition)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * from product_bill where  bill_id=" + billId + " and prod_id=" + prodId + " and position=" + groupPosition, null);
            while (c.moveToNext())
            {
                //name = cursor.getString(column_index);//to get other values
                Cursor c1 = database.rawQuery("SELECT * from modifier_bill where  prod_bill_id=" + c
                        .getInt(0), null);

                while (c1.moveToNext())
                {
                    int a = c.getInt(1);
                    if (c1.getInt(1) == position)
                    {
                        database.execSQL("delete from modifier_bill where id=" + c1.getInt(0));
                        database.execSQL("delete from modifier_bill_notes WHERE modifier_bill_id=" + c1
                                .getInt(0) + ";");
                    }
                    else if (c1.getInt(1) > position)
                    {
                        database.execSQL("UPDATE modifier_bill SET position=" + (c1.getInt(1) - 1) + " WHERE id=" + c1
                                .getInt(0) + ";");

                    }
                }


                c1.close();
            }
            c.close();

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
    }


    public void updateModifierFromBill(Integer billId, Integer position, Integer prodId, Integer groupPosition, Integer toRemove)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * from product_bill where  bill_id=" + billId + " and prod_id=" + prodId + " and position=" + groupPosition, null);
            while (c.moveToNext())
            {
                //name = cursor.getString(column_index);//to get other values
                Cursor c1 = database.rawQuery("SELECT * from modifier_bill where  prod_bill_id=" + c
                        .getInt(0), null);

                while (c1.moveToNext())
                {
                    if (c1.getInt(1) == position)
                    {
                        database.execSQL("UPDATE modifier_bill SET qty=" + (c1.getInt(3) - toRemove) + " WHERE id=" + c1
                                .getInt(0) + ";");
                    }
                }


                c1.close();
            }
            c.close();

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
    }


    public void addNewModifierFromBill(Integer billId, Integer position, Integer prodId, Integer groupPosition, CashButtonListLayout cbll)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * from product_bill where  bill_id=" + billId + " and prod_id=" + prodId + " and position=" + groupPosition, null);
            while (c.moveToNext())
            {
                //name = cursor.getString(column_index);//to get other values

                database.execSQL("INSERT INTO modifier_bill(position,mod_id,qty, prod_bill_id) VALUES(" + position + "," + cbll
                        .getModifierId() + "," + cbll.getQuantityInt() + "," + c.getInt(0) + ")");

                //Cursor c1 = database.rawQuery("SELECT * from modifier_bill where  prod_bill_id="+c.getInt(0),null);
                Cursor c1 = database.rawQuery("SELECT * from modifier_bill where  position=" + position + " AND mod_id=" + cbll
                        .getModifierId() + " AND prod_bill_id=" + c.getInt(0), null);

                while (c1.moveToNext())
                {
                    if (cbll.getModifierId() == -15)
                    {
                        int id = c1.getInt(0);
                        database.execSQL("INSERT INTO modifier_bill_notes(modifier_bill_id,note) VALUES(" + id + ",'" + cbll
                                .getNote() + "')");

                    }
                }
                c1.close();

            }
            c.close();

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
    }


    public void updateProductBillForPaymentQuantity(Integer qty, Integer id, Integer prodId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE product_bill SET qty=" + qty + ", prod_id=" + prodId + " WHERE id=" + id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }

    }


    public int checkProductBillForPayment(Integer position, Integer billId, CashButtonLayout cbl)
    {
        int check = -11;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            // Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE position="+position+" AND bill_id="+billId+" AND prod_id="+cbl.getProductId()+" and qty="+cbl.getQuantityInt(), null);
            Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE position=" + position + " AND bill_id=" + billId + " AND prod_id=" + cbl
                    .getProductId(), null);

            if (c.moveToFirst())
            {
                check = c.getInt(0);

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }
        return check;
    }


    public int saveProductBillForPayment(Integer position, Integer billId, CashButtonLayout cbl)
    {
        int id = 0;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }


            Cursor ca = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billId + " AND position=" + position, null);
            if (ca.moveToFirst())
            {
                database.execSQL("DELETE FROM product_bill WHERE position=" + position + " AND bill_id=" + billId);

                database.execSQL("INSERT INTO product_bill(position,prod_id,qty, bill_id, discount) VALUES(" + position + "," + cbl
                        .getProductId() + "," + cbl.getQuantityInt() + "," + billId + "," + cbl.getDiscount() + ")");

                Cursor c6 = database.rawQuery("SELECT * FROM product_bill ORDER BY id DESC", null);
                if (c6.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    id = c6.getInt(0);//to get id, 0 is the column index
                }
                if (cbl.getProductId() == -30)
                {
                    database.execSQL("INSERT INTO product_unspec_bill(prod_bill_id,price,description) VALUES(" + id + "," + cbl
                            .getPriceFloat() + ",'" + cbl.getTitle() + "');");
                }
                c6.close();
                while (ca.moveToNext())
                {
                    int idpb = ca.getInt(0);//to get id, 0 is the column index
                    Cursor c1 = database.rawQuery("SELECT * FROM item_subdivisions " +
                            "WHERE bill_subdivision_id = " + idpb, null);

                    /*if (!c1.moveToFirst()) {

                        Cursor c = database.rawQuery("SELECT * FROM product_bill ORDER BY id DESC", null);
                        if (c.moveToFirst()) {
                            //name = cursor.getString(column_index);//to get other values
                            id = c.getInt(0);//to get id, 0 is the column index
                            if (cbl.getProductId() == -30) {
                                database.execSQL("INSERT INTO product_unspec_bill(prod_bill_id,price,description) VALUES(" + id + "," + cbl.getPriceFloat() + ",'" + cbl.getTitle() + "');");
                            }

                            database.close();
                        }
                        c.close();
                    }*/
                    c1.close();
                    ca.close();
                }

            }
            else
            {
                database.execSQL("INSERT INTO product_bill(position,prod_id,qty, bill_id, discount) VALUES(" + position + "," + cbl
                        .getProductId() + "," + cbl.getQuantityInt() + "," + billId + "," + cbl.getDiscount() + ")");

                Cursor c = database.rawQuery("SELECT * FROM product_bill ORDER BY id DESC", null);
                if (c.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    id = c.getInt(0);//to get id, 0 is the column index
                    if (cbl.getProductId() == -30)
                    {
                        database.execSQL("INSERT INTO product_unspec_bill(prod_bill_id,price,description) VALUES(" + id + "," + cbl
                                .getPriceFloat() + ",'" + cbl.getTitle() + "');");
                    }

                    //  database.close();
                }
                c.close();
                ca.close();
            }

            database.close();


        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
        return id;
    }


    public ArrayList<ButtonLayout> selectFavoritesButton()
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            Calendar myDate = Calendar.getInstance();
            myDate.add(Calendar.DATE, -7);
            Date utilDate = myDate.getTime();
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateText = df2.format(utilDate);
            String query = "SELECT prod_id, count(prod_id) AS prodId from product_bill " +
                    " LEFT JOIN bill_total ON bill_total.id=product_bill.bill_id " +
                    " WHERE creation_time> '" + dateText + "' " +
                    " group by prod_id" +
                    " order by prodId desc;";


            //String query = "SELECT prod_id from product_bill_statistic  group by prod_id;" ;

            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<ButtonLayout> array = new ArrayList<>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    Cursor mCursor1 = database.rawQuery("SELECT * FROM button WHERE id= " + mCursor.getInt(0), null);

                    if (mCursor1 != null)
                    {
                        while (mCursor1.moveToNext())
                        {
                            if (mCursor1.getInt(mCursor1.getColumnIndex(DatabaseAdapter.KEY_ID)) != -30)
                            {
                                ButtonLayout c = new ButtonLayout(context);
                                c.setID(mCursor1.getInt(mCursor1.getColumnIndex(DatabaseAdapter.KEY_ID)));
                                c.setTitle(mCursor1.getString(mCursor1.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                                c.setSubTitle(mCursor1.getString(mCursor1.getColumnIndex(DatabaseAdapter.KEY_SUBTITLE)));
                                c.setImg(mCursor1.getString(mCursor1.getColumnIndex(DatabaseAdapter.KEY_IMG)));
                                c.setColor(mCursor1.getInt(mCursor1.getColumnIndex("color")));
                                c.setPos(mCursor1.getInt(mCursor1.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                                c.setPrice(mCursor1.getFloat(mCursor1.getColumnIndex(DatabaseAdapter.KEY_PRICE)));
                                c.setProductCode(mCursor1.getString(mCursor1.getColumnIndex(DatabaseAdapter.KEY_PRODUCT_CODE)));
                                c.setBarcode(mCursor1.getString(mCursor1.getColumnIndex(DatabaseAdapter.KEY_BARCODE)));
                                c.setCatID(mCursor1.getInt(mCursor1.getColumnIndex(DatabaseAdapter.KEY_CAT_ID)));
                                c.setCat((mCursor1.getInt(mCursor1.getColumnIndex(DatabaseAdapter.KEY_CAT_BOOL))));
                                c.setVat((mCursor1.getInt(mCursor1.getColumnIndex(DatabaseAdapter.KEY_VAT_TABLE))));
                                c.setPrinterId((mCursor1.getInt(mCursor1.getColumnIndex("printer"))));
                                c.setFidelity_discount((mCursor.getInt(mCursor.getColumnIndex("fidelity_discount"))));
                                c.setFidelity_credit((mCursor.getInt(mCursor.getColumnIndex("fidelity_discount"))));
                                c.setCredit_value((mCursor.getDouble(mCursor.getColumnIndex("credit_value"))));

                                array.add(c);
                            }
                        }
                        mCursor1.close();

                    }
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());

            ArrayList<ButtonLayout> newarray = new ArrayList<>();
            return newarray;
        }
    }


    public int getBillProduct(int bill_id, int position)
    {
        int id = -1;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT id FROM product_bill WHERE bill_id=" + bill_id + " AND position=" + position, null);
        while (c.moveToNext())
        {
            id = c.getInt(c.getColumnIndex("id"));
            c.close();
        }
        database.close();
        return id;
    }


    public void updateProductBillHomage(int homage, int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE product_bill SET homage=" + homage + " WHERE id=" + id + ";");
            database.close();

        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());

        }
    }




    // =============================================== //
    // [ MODIFIER BILLS ]
    // =============================================== //

    public int checkModifierBillForPayment(Integer position, Integer productBillId, CashButtonListLayout cbll)
    {
        int id = -11;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM modifier_bill WHERE position=" + position + " AND prod_bill_id=" + productBillId + " AND mod_id=" + cbll
                    .getModifierId() /*+ " and qty=" + cbll.getQuantity()*/, null);

            if (c.moveToFirst())
            {
                id = c.getInt(0);

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
        return id;
    }


    public void updateModifierBillForPaymentQuantity(Integer qty, Integer id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE modifier_bill SET qty=" + qty + " WHERE id=" + id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }

    }


    public void updateModifierBillNote(CashButtonListLayout cbll, Integer id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE modifier_bill_notes SET note='" + cbll.getNote() + "' WHERE modifier_bill_id=" + id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }

    }


    public void deleteModifierBillNote(Integer id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("delete from modifier_bill_notes WHERE modifier_bill_id=" + id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }

    }


    public int saveModifierBillForPayment(Integer position, Integer productBillId, CashButtonListLayout cbll)
    {
        int id = 0;

        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO modifier_bill(position,mod_id,qty, prod_bill_id) VALUES(" + position + "," + cbll
                    .getModifierId() + "," + cbll.getQuantityInt() + "," + productBillId + ")");
            Cursor c = database.rawQuery("SELECT * FROM modifier_bill ORDER BY id DESC", null);
            if (c.moveToFirst())
            {
                //name = cursor.getString(column_index);//to get other values
                id = c.getInt(0);//to get id, 0 is the column index
                if (cbll.getModifierId() == -15)
                {
                    database.execSQL("INSERT INTO modifier_bill_notes(modifier_bill_id,note) VALUES(" + id + ",'" + cbll
                            .getNote() + "')");

                }

            }
            //showData("modifier_bill");
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
        return id;
    }





    // =============================================== //
    // [ CUSTOMERS ]
    // =============================================== //

    public void deleteCustomerForBill(int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billId, null);
            while (c.moveToNext())
            {
                int id = c.getInt(0);//to get id, 0 is the column index
                database.execSQL("DELETE FROM customer_bill WHERE prod_bill_id=" + id);
            }
            c.close();

        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
    }


    public void saveCustomerBillForPayment(Customer c, Integer productBillId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO customer_bill(position,description,client_id, prod_bill_id) VALUES(" + c
                    .getPosition() + ",'" + c.getDescription() + "'," + c.getCustomerId() + "," + productBillId + ")");

        }
        catch (Exception e)
        {
            Log.d("Insert Customer Error", e.getMessage());
        }

    }


    public ArrayList<Customer> getCustomerData(int bill_id)
    {
        /*String TOTAL_BILL_COSTUMER_INVOICE_CREATE = "CREATE TABLE bill_total_customer_invoice (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bill_total_id INTEGER, " +
                "client_id INTEGER, " +
                "FOREIGN KEY(bill_total_id) REFERENCES bill_total(id),"+
                "FOREIGN KEY(client_id) REFERENCES client(id));";
        dbA.execOnDb(TOTAL_BILL_COSTUMER_INVOICE_CREATE);*/
        /* if (database.isOpen()) database.close();*/
        database = dbHelper.getReadableDatabase();
        ArrayList<Integer> customerPosition = new ArrayList<Integer>();
        ArrayList<Customer> customerList = new ArrayList<Customer>();
        Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + bill_id + " ORDER BY cast(position as REAL) ASC;", null);
        while (c.moveToNext())
        {
            int productId = c.getInt(c.getColumnIndex("id"));
            Cursor c1 = database.rawQuery("SELECT * FROM customer_bill WHERE prod_bill_id=" + productId, null);
            if (c1.moveToNext())
            {
                Integer position = c1.getInt(c1.getColumnIndex("position"));
                boolean check1 = customerPosition.contains(position);
                boolean check = Arrays.asList(customerPosition)
                                      .contains(c1.getColumnIndex("position"));
                if (!check1)
                {
                    customerPosition.add(position);
                    Customer customer = new Customer();
                    customer.setPosition(c1.getInt(c1.getColumnIndex("position")));
                    customer.setDescription(c1.getString(c1.getColumnIndex("description")));
                    customer.setCustomerId(c1.getInt(c1.getColumnIndex("client_id")));
                    customer.setActive(false);
                    customerList.add(customer);
                }
            }
            c1.close();

        }
        c.close();
        database.close();
        return customerList;
    }






    // =============================================== //
    // [ PRINT INDEX ]
    // =============================================== //

    public int getTotalBillPrintedIndex(int billId)
    {
        int check = 0;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            // Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE position="+position+" AND bill_id="+billId+" AND prod_id="+cbl.getProductId()+" and qty="+cbl.getQuantityInt(), null);
            Cursor c = database.rawQuery("SELECT print_index FROM bill_total WHERE id=" + billId, null);

            if (c.moveToFirst())
            {
                check = c.getInt(0);

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }
        return check;
    }


    public void updateBillTotalPrintedIndex(Integer printedIndex, Integer billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total SET print_index=" + printedIndex + " WHERE id=" + billId + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }

    }




}
