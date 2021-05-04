package com.utils.db;

import android.database.Cursor;
import android.util.Log;

import com.example.blackbox.model.CashManagement;
import com.example.blackbox.model.DiscountModel;
import com.example.blackbox.model.Vat;
import com.example.blackbox.model.VatModel;

import java.util.ArrayList;

public class DbAdapterBillExtra extends DbAdapterBills
{




    // =============================================== //
    // [ BILL EXTRA ]
    // =============================================== //

    public void insertBillExtra(int billId, float discount, float discountValue)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            int sub_id = -1;
            Cursor c = database.rawQuery("SELECT * FROM bill_total_extra WHERE bill_total_id = " + billId, null);
            if (c.moveToFirst())
            {
                database.execSQL("UPDATE bill_total_extra SET discountTotal=" + discountValue + " WHERE bill_total_id=" + billId + " ;");

            }
            else
            {
                database.execSQL("INSERT INTO bill_total_extra(discountTotal, bill_total_id) VALUES(" + discountValue + "," + billId + ")");

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }


    }


    public void updateBillExtra(int billId, float discount, float discountValue)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total_extra SET discountTotal=" + discountValue + " WHERE bill_total_id=" + billId + " ;");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert User Error", e.getMessage());
        }
    }


    public void updateBillExtraHomage(int billId, int homage)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total_extra SET homage=" + homage + " WHERE bill_total_id=" + billId + " ;");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert User Error", e.getMessage());
        }
    }


    public void insertBillExtraHomage(int billId, float discount)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO bill_total_extra(discountTotal, homage, bill_total_id) VALUES(" + discount + ",1," + billId + ")");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert User Error", e.getMessage());
        }
    }


    public Float getBillDiscountPrice(int billId)
    {
        Float price = 0.0f;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getReadableDatabase(); }

        Cursor c = database.rawQuery("SELECT discountTotal FROM bill_total_extra WHERE bill_total_id=" + billId + " LIMIT 1;", null);
        while (c.moveToNext())
        {
            price = c.getFloat(c.getColumnIndex("discountTotal"));
        }
        c.close();
        database.close();

        return price;
    }


    public int getBillTotalHomage(int billId)
    {
        int discount = 0;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getReadableDatabase(); }

        Cursor c = database.rawQuery("SELECT homage FROM bill_total_extra WHERE bill_total_id=" + billId + " LIMIT 1;", null);
        while (c.moveToNext())
        {
            discount = c.getInt(c.getColumnIndex("homage"));
        }
        c.close();
        database.close();

        return discount;
    }


    public void insertBillCredit(int billId, float credit)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO bill_total_credit(creditValue,  bill_total_id) VALUES(" + credit + "," + billId + ")");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert User Error", e.getMessage());
        }
    }


    public Float getBillCreditPrice(int billId)
    {
        Float price = 0.0f;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT creditValue FROM bill_total_credit WHERE bill_total_id=" + billId + " LIMIT 1;", null);
        while (c.moveToNext())
        {
            price = c.getFloat(c.getColumnIndex("creditValue"));
        }
        c.close();
        database.close();

        return price;
    }


    public void deleteCredit(int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM bill_total_credit WHERE bill_total_id=" + billId + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("DELETE total_credit", e.getMessage());
        }

    }


    public void updateTotalCredit(int billId, float credit)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total_credit SET creditValue =" + credit + " WHERE bill_total_credit=" + billId + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("UPDATE Total credit", e.getMessage());
        }

    }








    // =============================================== //
    // [ DISCOUNT ]
    // =============================================== //

    public void updateDiscount(double total, int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total SET total=" + total + " WHERE id=" + billId);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Update error", e.getMessage());
        }
    }


    public float getProductBillDiscount(int billId, int groupPosition)
    {
        float discount = 0;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            // Cursor c = database.rawQuery("SELECT * FROM product_bill WHERE position="+position+" AND bill_id="+billId+" AND prod_id="+cbl.getProductId()+" and qty="+cbl.getQuantityInt(), null);
            Cursor c = database.rawQuery("SELECT discount FROM product_bill WHERE bill_id=" + billId + " AND position=" + groupPosition, null);

            if (c.moveToFirst())
            {
                discount = c.getFloat(0);

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }
        return discount;
    }


    public void addDiscountToTable(float discount, int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            if (discount != 0.0f)
            {
                database.execSQL("INSERT INTO bill_total_extra (discountTotal, homage, bill_total_id)" +
                        " VALUES(" + discount + ", " + 0 + ", " + billId + ")");
            }
            database.close();

        }
        catch (Exception e)
        {
            Log.d("Add Error", e.getMessage());
        }
    }


    public void updateDiscountToZero(int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE bill_total_extra SET discountTotal=" + 0 + " WHERE id=" + billId);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Update failure", e.getMessage());
        }
    }


    public void deleteDiscuntTotal(int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM bill_total_extra WHERE bill_total_id=" + billId);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Update failure", e.getMessage());
        }
    }


    public float checkIfDiscountExists(int billId)
    {
        float discount = -10;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM bill_total_extra " +
                    "WHERE bill_total_id=" + billId, null);
            if (c != null)
            {
                if (c.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    discount = c.getFloat(c.getColumnIndex("discountTotal"));

                }
                c.close();
            }
            database.close();
            return discount;
        }
        catch (Exception e)
        {
            Log.d("Check failure", e.getMessage());
            return -9;
        }
    }


    public float getBillProductDiscount(int bill_id, int position)
    {
        float discount = 0.0f;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c = database.rawQuery("SELECT discount FROM product_bill WHERE bill_id=" + bill_id + " AND position=" + position, null);
        while (c.moveToNext())
        {
            discount = c.getFloat(c.getColumnIndex("discount"));
            c.close();
        }
        database.close();
        return discount;
    }


    public void updateProductBillDiscount(float discount, int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE product_bill SET discount=" + discount + " WHERE id=" + id + ";");
            database.close();

        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());

        }
    }


    public void addDiscountMode(String description, int value, int mode)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("INSERT INTO discount_mode (description, value, mode) " +
                    "VALUES(\"" + description.replaceAll("'", "\'") + "\", " + value + ", " + mode + ");");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }


    public void deleteDiscount(String description)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("DELETE FROM discount_mode WHERE description=\"" + description + "\";");
            deleteClientDiscountFromDescription(description);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Delete Error", e.getMessage());
        }
    }


    //fetch all discounts in db
    public ArrayList<DiscountModel> fetchDiscountArray(int mode)
    {
        try
        {
            ArrayList<DiscountModel> myDiscounts = new ArrayList<>();
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM discount_mode WHERE mode=" + mode + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    DiscountModel d = new DiscountModel();
                    d.setDescription(c.getString(c.getColumnIndex("description")));
                    d.setValue(c.getInt(c.getColumnIndex("value")));
                    myDiscounts.add(d);
                }
            }
            c.close();
            database.close();
            return myDiscounts;
        }
        catch (Exception e)
        {
            Log.d("Fetching error", e.getMessage());
            return null;
        }
    }


    //method to fetch a single discount from discount_mode
    public DiscountModel fetchSingleDiscount(String desc)
    {
        try
        {
            DiscountModel myDiscount = null;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM discount_mode WHERE description=\"" +
                    desc.replaceAll("'", "\'") + "\";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    myDiscount = new DiscountModel();
                    myDiscount.setDescription(c.getString(c.getColumnIndex("description")));
                    myDiscount.setValue(c.getInt(c.getColumnIndex("value")));
                    myDiscount.setMode(c.getInt(c.getColumnIndex("mode")));
                }
            }
            c.close();
            database.close();

            return myDiscount;
        }
        catch (Exception e)
        {
            Log.d("fetch failure", e.getMessage());
            return null;
        }
    }


    //method to check if a discount already exists
    public boolean checkIfDiscountExists(String desc)
    {
        try
        {
            boolean value = false;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM discount_mode WHERE description=\""
                    + desc.replaceAll("'", "\'") + "\";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    value = true;
                }
            }
            c.close();
            database.close();

            return value;
        }
        catch (Exception e)
        {
            Log.d("Fetch failure", e.getMessage());
            return false;
        }
    }


    //get discount value from discount description
    public int getDiscountValue(String desc)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int value = -1;
            Cursor mCursor = database.rawQuery("SELECT * FROM discount_mode WHERE description=\""
                    + desc.replaceAll("'", "\'") + "\";", null);
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    value = mCursor.getInt(mCursor.getColumnIndex("value"));
                }
            }
            mCursor.close();
            database.close();
            return value;
        }
        catch (Exception e)
        {
            Log.d("fetch failure", e.getMessage());
            return -1;
        }
    }


    //method to obtain mode from discount description
    public int getDiscountMode(String desc)
    {
        try
        {
            int mode = -1;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM discount_mode WHERE description=\""
                    + desc.replaceAll("'", "\'") + "\";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    mode = c.getInt(c.getColumnIndex("mode"));
                }
            }
            c.close();
            database.close();
            return mode;
        }
        catch (Exception e)
        {
            Log.d("Fetch error", e.getMessage());
            return -1;
        }
    }


    //method to insert a new discount in discount table
    public void assignDiscountToClient(int client_id, String desc)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("INSERT INTO discount (client_id, discount_mode_id) VALUES(" + client_id + "," +
                    " \"" + desc.replaceAll("'", "\'") + "\");");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }


    //fetch all discounts that that client has
    public ArrayList<String> checkIfClientHasDiscount(int client_id)
    {
        try
        {
            ArrayList<String> discounts = new ArrayList<>();
            String desc = "";
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM discount WHERE client_id=" + client_id + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    desc = c.getString(c.getColumnIndex("discount_mode_id"));
                    discounts.add(desc);
                }
            }
            c.close();
            database.close();
            return discounts;
        }
        catch (Exception e)
        {
            Log.d("Fetch error", e.getMessage());
            return null;
        }
    }


    //checks if client has that specific discount set
    public boolean checkIfClientHasASingleDiscount(int client_id, String desc)
    {
        boolean value = false;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM discount WHERE client_id=" + client_id +
                    " AND discount_mode_id=\"" + desc.replaceAll("'", "\'") + "\";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    value = true;
                }
            }
            c.close();
            database.close();

            return value;
        }
        catch (Exception e)
        {
            Log.d("Fetch failure", e.getMessage());
            return value;
        }
    }


    //it obtains discount's name from clientId and discount value
    public String getDiscountFromValueAndClient(int clientId, int value)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            String name = "";
            Cursor c = database.rawQuery("SELECT discount_mode_id FROM discount " +
                    "LEFT JOIN discount_mode ON discount.discount_mode_id=discount_mode.description " +
                    "WHERE discount.client_id=" + clientId + " " +
                    "AND discount_mode.value=" + value + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    name = c.getString(c.getColumnIndex("discount_mode_id"));
                }
            }
            c.close();
            database.close();

            return name;
        }
        catch (Exception e)
        {
            Log.d("Failure", e.getMessage());
            return null;
        }
    }


    //delete discount from discount table assigned to that client
    public void deleteClientDiscount(int client_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("DELETE FROM discount WHERE client_id=" + client_id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }


    //delete discount from discount table with specific description assigned to that client
    public void deleteClientDiscountFromDescription(String desc)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("DELETE FROM discount WHERE discount_mode_id=\"" + desc.replaceAll("'", "\'") + "\";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }


    public void deleteSpecificClientDiscount(int client_id, String desc)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("DELETE FROM discount WHERE client_id=" + client_id +
                    " AND discount_mode_id=\"" + desc.replaceAll("'", "\'") + "\";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Delete error", e.getMessage());
        }
    }


    //modify existing discount, updating value
    public void modifyExistingDiscountValue(String desc, int value)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("UPDATE discount_mode SET value=" + value + " WHERE description=\"" +
                    desc.replaceAll("'", "\'") + "\";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Modifying error", e.getMessage());
        }
    }


    //modify existing discount, updating description
    //it modifies also existing client's discounts
    public void modifyExistingDiscountDescription(String oldDesc, String desc)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("UPDATE discount_mode SET description=\"" + desc.replaceAll("'", "\'")
                    + "\" WHERE description=\"" +
                    oldDesc.replaceAll("'", "\'") + "\";");
            execOnDb("UPDATE discount SET discount_mode_id=\"" + desc.replaceAll("'", "\'")
                    + "\" WHERE discount_mode_id=\"" + oldDesc.replaceAll("'", "\'") + "\";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Modifying error", e.getMessage());
        }
    }




    // =============================================== //
    // [ VATS ]
    // =============================================== //

    public int fetchVatByIdQuery(int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * from vat where id=" + id + "", null);
            VatModel vat = new VatModel();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    vat.setReferenceId(id);
                    vat.setVatValue(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_VAT_VALUE)));
                    vat.setPerc(mCursor.getInt(mCursor.getColumnIndex("perc")));
                }
                mCursor.close();

            }
            database.close();
            return vat.getVatValue();
        }
        catch (Exception e)
        {
            Log.d("fetch Failure", e.getMessage());
            return -1;
        }
    }


    //ritorna un valore VatModel trovato tramite il valore
    public VatModel fetchVatByValueQuery(int value)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * from vat where value=" + value + "", null);
            VatModel vat = new VatModel();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    vat.setReferenceId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    vat.setVatValue(value);
                    vat.setPerc(mCursor.getInt(mCursor.getColumnIndex("perc")));
                }
                mCursor.close();

            }
            database.close();
            return vat;
        }
        catch (Exception e)
        {
            Log.d("fetch Failure", e.getMessage());
            return null;
        }
    }


    public int fetchVatButtonValue(int id)
    {
        try
        {
            int vat = 0;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor myCursor = database.rawQuery("SELECT * FROM button WHERE id=" + id + ";", null);
            if (myCursor != null)
            {
                if (myCursor.moveToFirst())
                {
                    vat = myCursor.getInt(myCursor.getColumnIndex(KEY_VAT_TABLE));
                }
            }
            myCursor.close();
            database.close();
            return vat;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    //it returns modifier's vat value, or zero if not set
    public int fetchVatModifierValue(int id)
    {
        try
        {
            int vat = 0;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor myCursor = database.rawQuery("SELECT * FROM modifier WHERE id=" + id + ";", null);
            if (myCursor != null)
            {
                if (myCursor.moveToFirst())
                {
                    vat = myCursor.getInt(myCursor.getColumnIndex(KEY_VAT_TABLE));
                }
            }
            myCursor.close();
            database.close();
            return vat;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public boolean checkIfVatIsAdded(int vatValue)
    {
        try
        {
            Boolean rowExist = false;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * from vat WHERE value=" + vatValue + ";", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    rowExist = true;
                }
            }
            mCursor.close();
            database.close();
            return rowExist;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return false;
        }
    }


    public ArrayList<VatModel> fetchVatArrayByQuery()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * from vat ORDER BY id DESC", null);
            ArrayList<VatModel> array = new ArrayList<>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    VatModel vat = new VatModel();
                    vat.setReferenceId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    vat.setVatValue(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_VAT_VALUE)));
                    vat.setPerc(mCursor.getInt(mCursor.getColumnIndex("perc")));
                    array.add(vat);
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


    public void deleteVatValueFromServer(int vat_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM vat WHERE id=" + vat_id + ";");

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Delete failed.", e.getMessage());
            return;
        }
    }


    public void deleteVatValue(int vat_id, int vatValue)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM vat WHERE id=" + vat_id + ";");
            database.execSQL("UPDATE button SET vat=" + 0 + " WHERE vat=" + vatValue + ";");
            database.execSQL("UPDATE modifier SET vat=" + 0 + " WHERE vat=" + vatValue + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Delete failed.", e.getMessage());
            return;
        }
    }


    public void updateVatValue(VatModel vat)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            database.execSQL("UPDATE vat SET value=" + vat.getVatValue() + " WHERE id=" + vat.getReferenceId());
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Add failed.", e.getMessage());
        }
    }


    public void addVatValue(int vatValue)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            database.execSQL("INSERT into vat (value) VALUES(" + vatValue + ");");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Add VatValue failed.", e.getMessage());
        }
    }


    public void insertVatsSync(ArrayList<Vat> vats)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            for (Vat vat : vats)
            {
                database.execSQL("INSERT into vat (id, value, perc) VALUES(" + vat.getId() + ", " + vat
                        .getValue() + ", " + vat.getPerc() + ");");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Add VatValue failed.", e.getMessage());
        }
    }


    public void addVatValueFromServer(int id, int vatValue, int perc)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            database.execSQL("INSERT into vat (id, value, perc) VALUES(" + id + "," + vatValue + "," + perc + ");");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Add VatValue failed.", e.getMessage());
        }
    }


    public void updateVatValueFromServer(int id, int vatValue, int perc)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            database.execSQL("UPDATE vat set value=" + vatValue + " , perc=" + perc + " WHERE id=" + id);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Add VatValue failed.", e.getMessage());
        }
    }







    // =============================================== //
    // [ CASH MANAGEMENT]
    // =============================================== //

    public int checkIfCashManagementIsSet()
    {
        int result = -1;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM cash_management_real;", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    result = 1;
                }
            }
            c.close();
            database.close();

            return result;
        }
        catch (Exception e)
        {
            Log.d("CHECK ERROR", e.getLocalizedMessage());
            return -11;
        }
    }

    public double checkIfCashTotalIsDifferent()
    {
        double result;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            float currentTotal = 0.0f;
            float max_cash = 0.0f;
            Cursor c1 = database.rawQuery("SELECT current_total FROM cash_management_real;", null);
            if (c1 != null)
            {
                while (c1.moveToNext())
                {
                    currentTotal = c1.getFloat(c1.getColumnIndex("current_total"));
                }
            }
            c1.close();

            Cursor c2 = database.rawQuery("SELECT max_cash FROM cash_management_set;", null);
            if (c2 != null)
            {
                while (c2.moveToNext())
                {
                    max_cash = c2.getFloat(c2.getColumnIndex("max_cash"));
                }
            }
            c2.close();

            if (currentTotal <= max_cash)
            { result = 0.0; }
            else
            { result = (double) (currentTotal - max_cash); }

            return result;
        }
        catch (Exception e)
        {
            Log.d("CHECK ERROR", e.getLocalizedMessage());
            return -11;
        }
    }

    public void insertCashManagementGeneral(float minCash, float maxCash, float minWithdraw)
    {
        try
        {
            if (checkIfCashManagementIsSet() == -1)
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("INSERT INTO cash_management_set(min_cash, max_cash, min_withdraw) VALUES(" + minCash
                        + ", " + maxCash + ", " + minWithdraw + ");");
                database.execSQL("INSERT INTO cash_management_real(min_cash, max_cash, min_withdraw, current_total) VALUES(" + minCash
                        + ", " + maxCash + ", " + minWithdraw + ", 0);");
            }
            else
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("UPDATE cash_management_set SET min_cash=" + minCash + ", max_cash=" + maxCash
                        + ", min_withdraw=" + minWithdraw + ";");
                database.execSQL("UPDATE cash_management_real SET min_cash=" + minCash + ", max_cash=" + maxCash
                        + ", min_withdraw=" + minWithdraw + ", current_total=0;");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("CASH MANAGEMENT ERROR", e.getMessage());
        }
    }

    public void insertCashManagement(float minCash, float maxCash, float minWithdraw, int fiveC, int tenC, int twentyC, int fiftyC,
                                     int oneE, int twoE, int fiveE, int tenE, int twentyE, int fiftyE, int hundred, int twoHundred)
    {
        try
        {
            if (checkIfCashManagementIsSet() == -1)
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("INSERT INTO cash_management_set(min_cash, max_cash, min_withdraw, five_cents, ten_cents, twenty_cents, " +
                        "fifty_cents, one_euros, two_euros, five_euros, ten_euros, twenty_euros, fifty_euros, " +
                        "hundred_euros, two_hundred_euros) VALUES(" + minCash + ", " + maxCash + ", " + minWithdraw + ", " +
                        "" + fiveC + ", " + tenC + ", " + twentyC + ", " + fiftyC + ", " + oneE + ", " + twoE + ", "
                        + fiveE + ", " + tenE + ", " + twentyE + ", " + fiftyE + ", " + hundred + ", " + twoHundred + ");");
                database.execSQL("INSERT INTO cash_management_real(min_cash, max_cash, min_withdraw, current_total, five_cents, ten_cents, twenty_cents, " +
                        "fifty_cents, one_euros, two_euros, five_euros, ten_euros, twenty_euros, fifty_euros, " +
                        "hundred_euros, two_hundred_euros) VALUES(" + minCash + ", " + maxCash + ", " + minWithdraw + ", 0, " +
                        "" + fiveC + ", " + tenC + ", " + twentyC + ", " + fiftyC + ", " + oneE + ", " + twoE + ", "
                        + fiveE + ", " + tenE + ", " + twentyE + ", " + fiftyE + ", " + hundred + ", " + twoHundred + ");");
            }
            else
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("UPDATE cash_management_set SET min_cash=" + minCash + ", max_cash=" + maxCash
                        + ", min_withdraw=" + minWithdraw + ", five_cents=" + fiveC + ", ten_cents=" + tenC
                        + ", twenty_cents=" + twentyC + ", fifty_cents=" + fiftyC + ", one_euros=" + oneE + ", two_euros=" + twoE + ", five_euros="
                        + fiveE + ", ten_euros=" + tenE + ", twenty_euros=" + twentyE + ", fifty_euros=" + fiftyE + ", hundred_euros=" + hundred
                        + ", two_hundred_euros=" + twoHundred + ";");
                database.execSQL("UPDATE cash_management_real SET min_cash=" + minCash + ", max_cash=" + maxCash
                        + ", min_withdraw=" + minWithdraw + ", current_total=0, five_cents=" + fiveC + ", ten_cents=" + tenC
                        + ", twenty_cents=" + twentyC + ", fifty_cents=" + fiftyC + ", one_euros=" + oneE + ", two_euros=" + twoE + ", five_euros="
                        + fiveE + ", ten_euros=" + tenE + ", twenty_euros=" + twentyE + ", fifty_euros=" + fiftyE + ", hundred_euros=" + hundred
                        + ", two_hundred_euros=" + twoHundred + ";");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("CASH MANAGEMENT ERROR", e.getLocalizedMessage());
        }
    }

    public CashManagement getCashManagement()
    {
        try
        {
            CashManagement cash = new CashManagement();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM cash_management_real;", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    cash.setMinCash(c.getFloat(c.getColumnIndex("min_cash")));
                    cash.setMaxCash(c.getFloat(c.getColumnIndex("max_cash")));
                    cash.setMinWithdraw(c.getFloat(c.getColumnIndex("min_withdraw")));
                    cash.setCurrentTotal(c.getFloat(c.getColumnIndex("current_total")));
                    cash.setFiveCents(c.getInt(c.getColumnIndex("five_cents")));
                    cash.setTenCents(c.getInt(c.getColumnIndex("ten_cents")));
                    cash.setTwentyCents(c.getInt(c.getColumnIndex("twenty_cents")));
                    cash.setFiftyCents(c.getInt(c.getColumnIndex("fifty_cents")));
                    cash.setOneEuros(c.getInt(c.getColumnIndex("one_euros")));
                    cash.setTwoEuros(c.getInt(c.getColumnIndex("two_euros")));
                    cash.setFiveEuros(c.getInt(c.getColumnIndex("five_euros")));
                    cash.setTenEuros(c.getInt(c.getColumnIndex("ten_euros")));
                    cash.setTwentyEuros(c.getInt(c.getColumnIndex("twenty_euros")));
                    cash.setFiftyEuros(c.getInt(c.getColumnIndex("fifty_euros")));
                    cash.setHundredEuros(c.getInt(c.getColumnIndex("hundred_euros")));
                    cash.setTwoHundredEuros(c.getInt(c.getColumnIndex("two_hundred_euros")));
                }
            }

            c.close();
            database.close();

            return cash;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public CashManagement getCashManagementStatic()
    {
        try
        {
            CashManagement cash = new CashManagement();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM cash_management_set;", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    cash.setMinCash(c.getFloat(c.getColumnIndex("min_cash")));
                    cash.setMaxCash(c.getFloat(c.getColumnIndex("max_cash")));
                    cash.setMinWithdraw(c.getFloat(c.getColumnIndex("min_withdraw")));
                    cash.setFiveCents(c.getInt(c.getColumnIndex("five_cents")));
                    cash.setTenCents(c.getInt(c.getColumnIndex("ten_cents")));
                    cash.setTwentyCents(c.getInt(c.getColumnIndex("twenty_cents")));
                    cash.setFiftyCents(c.getInt(c.getColumnIndex("fifty_cents")));
                    cash.setOneEuros(c.getInt(c.getColumnIndex("one_euros")));
                    cash.setTwoEuros(c.getInt(c.getColumnIndex("two_euros")));
                    cash.setFiveEuros(c.getInt(c.getColumnIndex("five_euros")));
                    cash.setTenEuros(c.getInt(c.getColumnIndex("ten_euros")));
                    cash.setTwentyEuros(c.getInt(c.getColumnIndex("twenty_euros")));
                    cash.setFiftyEuros(c.getInt(c.getColumnIndex("fifty_euros")));
                    cash.setHundredEuros(c.getInt(c.getColumnIndex("hundred_euros")));
                    cash.setTwoHundredEuros(c.getInt(c.getColumnIndex("two_hundred_euros")));
                }
            }

            c.close();
            database.close();

            return cash;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void insertTotalDeposit(float total)
    {
        try
        {
            if (checkIfCashManagementIsSet() == -1)
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("INSERT INTO cash_management_real(current_total) VALUES(" + total + ");");
            }
            else
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("UPDATE cash_management_real SET current_total=" + total + ";");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("CASH MANAGEMENT ERROR", e.getMessage());
        }
    }

    public void insertCashStatus(float total, int fiveC, int tenC, int twentyC, int fiftyC,
                                 int oneE, int twoE, int fiveE, int tenE, int twentyE, int fiftyE, int hundred, int twoHundred)
    {
        try
        {
            if (checkIfCashManagementIsSet() == -1)
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("INSERT INTO cash_management_real(current_total, five_cents, ten_cents, twenty_cents, " +
                        "fifty_cents, one_euros, two_euros, five_euros, ten_euros, twenty_euros, fifty_euros, " +
                        "hundred_euros, two_hundred_euros) VALUES(" + total + ", " +
                        "" + fiveC + ", " + tenC + ", " + twentyC + ", " + fiftyC + ", " + oneE + ", " + twoE + ", "
                        + fiveE + ", " + tenE + ", " + twentyE + ", " + fiftyE + ", " + hundred + ", " + twoHundred + ");");
            }
            else
            {
                if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

                database.execSQL("UPDATE cash_management_real SET current_total=" + total + ", five_cents=" + fiveC + ", ten_cents=" + tenC
                        + ", twenty_cents=" + twentyC + ", fifty_cents=" + fiftyC + ", one_euros=" + oneE + ", two_euros=" + twoE + ", five_euros="
                        + fiveE + ", ten_euros=" + tenE + ", twenty_euros=" + twentyE + ", fifty_euros=" + fiftyE + ", hundred_euros=" + hundred
                        + ", two_hundred_euros=" + twoHundred + ";");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("CASH MANAGEMENT ERROR", e.getLocalizedMessage());
        }
    }

    public void modifySimpleTotalCashManagement(float total)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE cash_management_real SET current_total=" + total + ";");

            database.close();
        }
        catch (Exception e)
        {
            Log.d("UPDATING ERROR", e.getMessage());
        }
    }

    public void modifyTotalCashManagement(float total, int fiveC, int tenC, int twentyC, int fiftyC,
                                          int oneE, int twoE, int fiveE, int tenE, int twentyE, int fiftyE, int hundred, int twoHundred)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }

            database.execSQL("UPDATE cash_management_real SET current_total=" + total + ", five_cents=" + fiveC + ", ten_cents=" + tenC
                    + ", twenty_cents=" + twentyC + ", fifty_cents=" + fiftyC + ", one_euros=" + oneE + ", two_euros=" + twoE + ", five_euros="
                    + fiveE + ", ten_euros=" + tenE + ", twenty_euros=" + twentyE + ", fifty_euros=" + fiftyE + ", hundred_euros=" + hundred
                    + ", two_hundred_euros=" + twoHundred + ";");
        }
        catch (Exception e)
        {
            Log.d("CASH MANAGEMENT ERROR", e.getLocalizedMessage());
        }
    }









}
