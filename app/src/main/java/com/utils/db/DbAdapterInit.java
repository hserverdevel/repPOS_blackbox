package com.utils.db;


import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.blackbox.model.BlackboxInfo;
import com.example.blackbox.model.PaymentButton;
import com.example.blackbox.model.TotalBill;
import com.example.blackbox.model.TwoString;
import com.example.blackbox.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is the master class for all the database adapters
 *
 * here are stored the essential element, that will be inherited from the rest of the dbAdapter
 *
 * */
public class DbAdapterInit
{

    public static final String TAG = "<DatabaseAdapter>";

    public Context context;
    public SQLiteDatabase database;
    public DatabaseHelper dbHelper;

    // Database fields
    public static final String CATEGORY_TABLE = "category";
    public static final String BUTTON_TABLE = "button";
    public static final String MODIFIER_TABLE = "modifier";

    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_SUBTITLE = "subtitle";
    public static final String KEY_IMG = "img_name";
    public static final String KEY_POSITION = "position";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_CAT_BOOL = "isCat";
    public static final String KEY_PRICE = "price";
    public static final String KEY_BARCODE = "barcode";
    public static final String KEY_PRODUCT_CODE = "productCode";
    public static final String KEY_CAT_ID = "catID";
    public static final String KEY_GROUP_ID = "groupID";
    public static final String KEY_ADMIN_BOOL = "userType";

    public static final String KEY_TOTAL = "total";
    public static final String KEY_PAID = "paid";
    public static final String KEY_BILL_NUMBER = "bill_number";
    public static final String KEY_CREATION_TIME = "creation_time";
    public static final String KEY_PAID_TIME = "pay_time";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_VAT_TABLE = "vat";
    public static final String KEY_VAT_VALUE = "value";




    // =============================================== //
    // [ DATABASE OPERATIONS ]
    // =============================================== //


    public void restartDb()
        { dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); }


    public long getTablesRowCount(String tableName)
    {
        long result = -1;

        try
        {
            if (!database.isOpen()) { database = dbHelper.getWritableDatabase(); }

            result = DatabaseUtils.queryNumEntries(database, tableName);

            Log.v(TAG, "Rows of table " + tableName + ": " + result);
        }

        catch (Exception e)
        {
            Log.d("rawQueryError", e.getMessage());
        }

        return result;
    }


    public boolean isColumnExists(String table, String column)
    {
        Cursor cursor = database.rawQuery("PRAGMA table_info(" + table + ")", null);

        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                if (column.equalsIgnoreCase(name))
                {
                    cursor.close();
                    return true;
                }
            }
        }

        cursor.close();
        return false;
    }


    /**
     * Present a raw query to the SQL database
     * @param query
     * */
    public void queryToDb(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            Cursor c = database.rawQuery(query, null);
            c.moveToFirst();
            c.close();

            database.close();
        }
        catch (Exception e)
        {
            Log.d("rawQueryError", e.getMessage());
        }
    }


    /**
     * Exec the given query in the SQL database
     * @param query
     * */
    public void execOnDb(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }
            database.execSQL(query);
            //  database.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("execSQLError", e.getMessage());
        }
    }


    /**
     * Present a query to the database, to obtain a table data
     * @param query like "SELECT * from table where id = 5"
     *
     * @return a cursors object that represent the rows in the obtained data
     * */
    public Cursor fetchByQuery(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            // database.close();
            return database.rawQuery(query, null);
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return null;
        }
    }


    /**
     * Print the content of the given
     * @param table
     * */
    public void showData(String table)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            Cursor c = database.rawQuery("SELECT * FROM " + table /*+ " ORDER BY id DESC"*/, null);

            StringBuilder sb = new StringBuilder();

            String[] cols = c.getColumnNames();

            c.moveToPosition(-1);

            // for each row
            while (c.moveToNext())
            {
                sb.append(String.format("%s : ", c.getPosition()));

                // for each col
                for (int i = 0; i < cols.length; i++)
                {
                    String value;
                    try
                        { value = c.getString(i); }
                    catch (SQLiteException e)
                        { value = "<unprintable>"; }

                    if (i > 0)
                        { sb.append(","); }

                    sb.append(String.format(" %s=%s", cols[i], value));
                }

                sb.append(";\n");
            }

            Log.i(TAG, String.format("Dump SQL table `%s`:\n%s", table, sb));

            c.close();
            database.close();
        }

        catch (Exception e)
            { Log.e("execSQLError", e.getMessage()); }
    }



    /**
     * execute the SQL command, and return the result
     * @param cmd
     * */
    public void showDataExec(String cmd)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            Cursor c = database.rawQuery(cmd, null);

            StringBuilder sb = new StringBuilder();

            String[] cols = c.getColumnNames();

            c.moveToPosition(-1);

            int lenField = 10;
            int lenValue = 10;
            // for each row
            while (c.moveToNext())
            {
                sb.append(String.format("%s : ", c.getPosition()));

                // for each col
                for (int i = 0; i < cols.length; i++)
                {
                    String value;
                    try
                        { value = c.getString(i); }
                    catch (SQLiteException e)
                        { value = "<unprintable>"; }

                    if (i > 0)
                        { sb.append(","); }

                    if (i == 0)
                        { lenField = cols[i].length() + 2; lenValue = value.length() + 2; }

                    sb.append(String.format(" %" + lenField + "s=%" + lenValue + "s", cols[i], value));
                }

                sb.append(";\n");
            }

            Log.i(TAG, String.format("SQL command result [%s]:\n%s", cmd, sb));

            c.close();
            database.close();
        }

        catch (Exception e)
        { Log.e("execSQLError", e.getMessage()); }
    }



    /**
     * get last inserted autogenerated id
     * */
    public long lastInsertedId()
    {
        int id = 0;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            Cursor c = database.rawQuery("SELECT last_insert_rowid()", null);

            if (c.moveToFirst())  { id = (int) c.getLong(0); }

            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("execSQLError", e.getMessage());
        }
        return id;
    }


    public int checkIfExists(String query)
    {
        Boolean check = false;
        int id = -11;

        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            Cursor mCursor = database.rawQuery(query, null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    check = true;//to get id, 0 is the column index
                    id = mCursor.getInt(0);
                }
                mCursor.close();
            }
            database.close();


        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
        }
        finally
        {
            return id;
        }
    }


    // =============================================== //
    // [ TIMESTAMP ]
    // =============================================== //

    /**
     * get the checksum (last modified table)
     * for the input table
     * */
    public String getChecksumForTable(String tableName)
    {
        String result = "";

        try
        {
            if (!database.isOpen())
                { database = dbHelper.getReadableDatabase(); }

            Cursor cursor = database.rawQuery(String.format("SELECT * FROM checksum_registry WHERE name = '%s'", tableName), null);

            if (cursor != null) while (cursor.moveToNext())
                { result = cursor.getString(cursor.getColumnIndex("Checksum")); }

            cursor.close();
        }

        catch (Exception e)
            { Log.e("DatabaseFailure", "Exception: " + e);  e.printStackTrace(); }

        return result;

    }


    /**
     * update the checksum for the specified table
     * */
    public void updateChecksumForTable(String tableName, String checksum)
    {

        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            execOnDb(String.format("UPDATE checksum_registry SET Checksum = '%s' WHERE name = '%s'", checksum, tableName));

            database.close();
        }

        catch (Exception e)
            { Log.e("DatabaseFailure", "Exception: " + e);  e.printStackTrace(); }
    }








    // =============================================== //
    // [ BLACKBOX ]
    // =============================================== //

    public void insertBlackboxSync(BlackboxInfo b)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getWritableDatabase(); }

            // database.execSQL("DELETE FROM blackbox_info");
            database.execSQL("INSERT INTO blackbox_info(id, name, address) VALUES(" + b.getId() + " ,  '" + b
                    .getName() + "', '" + b.getAddress() + "');");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }
    }


    public BlackboxInfo getBlackboxInfo()
    {
        BlackboxInfo blackboxInfo = new BlackboxInfo();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM blackbox_info LIMIT 1", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    blackboxInfo.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    blackboxInfo.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    blackboxInfo.setAddress(mCursor.getString(mCursor.getColumnIndex("address")));
                }
                mCursor.close();

            }
            // database.close();
            return blackboxInfo;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return blackboxInfo;
        }
    }


    public ArrayList<BlackboxInfo> selectAllBlackbox()
    {
        ArrayList<BlackboxInfo> array = new ArrayList<>();

        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM blackbox_info", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    BlackboxInfo blackbox = new BlackboxInfo();
                    blackbox.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    blackbox.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    blackbox.setAddress(mCursor.getString(mCursor.getColumnIndex("address")));

                    array.add(blackbox);

                }
                mCursor.close();

            }
            // database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return array;
        }
    }


    public void deleteBlackbox(BlackboxInfo blackbox)
    {
        try
        {
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            database.execSQL("DELETE FROM blackbox_info WHERE id='" + blackbox.getId() + "'");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }






    // =============================================== //
    // [ TODO ]
    // =============================================== //


    public void recursiveUpdateFidelityCredit(int categoryId, int fidelityCredit)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            Cursor c = database.rawQuery("SELECT * FROM button WHERE catID=" + categoryId, null);
            while (c.moveToNext())
            {
                database.execSQL("UPDATE button SET fidelity_credit=" + fidelityCredit + " WHERE id=" + c
                        .getInt(c.getColumnIndex(DatabaseAdapter.KEY_ID)) + ";");
                database.rawQuery("SELECT * FROM button WHERE catID=" + c.getInt(c.getColumnIndex(DatabaseAdapter.KEY_ID)), null);
                recursiveUpdateFidelityCredit(c.getInt(c.getColumnIndex(KEY_ID)), fidelityCredit);
            }
            c.close();


        }
        catch (Exception e)
        {
            Log.d("Delete Error", e.getMessage());
        }
    }


    public boolean checkIfStaticCodeIsUsed(String code)
    {
        boolean check = false;
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        Cursor c1 = database.rawQuery("SELECT id FROM static_activation_code WHERE code='" + code + "' AND used=0;", null);
        while (c1.moveToNext())
        {
            check = true;
        }
        c1.close();
        database.close();

        return check;

    }


    public void recursiveUpdateFidelityDiscount(int categoryId, int fidelityDiscount)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            Cursor c = database.rawQuery("SELECT * FROM button WHERE catID=" + categoryId, null);
            while (c.moveToNext())
            {
                database.execSQL("UPDATE button SET fidelity_discount=" + fidelityDiscount + " WHERE id=" + c
                        .getInt(c.getColumnIndex(DatabaseAdapter.KEY_ID)) + ";");
                database.rawQuery("SELECT * FROM button WHERE catID=" + c.getInt(c.getColumnIndex(DatabaseAdapter.KEY_ID)), null);
                recursiveUpdateFidelityDiscount(c.getInt(c.getColumnIndex(KEY_ID)), fidelityDiscount);
            }
            c.close();


        }
        catch (Exception e)
        {
            Log.d("Delete Error", e.getMessage());
        }
    }


    public void insertLoginRecord(String username)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO login_record(username, login_time) VALUES('" + username + "',datetime(CURRENT_TIMESTAMP, 'localtime')) ");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }


    public int selectNumeroFattura()
    {
        int numeroFattura = 0;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT numero_fattura FROM fattura", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    numeroFattura = c.getInt(c.getColumnIndex("numero_fattura"));
                }
            }
            c.close();
            database.close();
//
            return numeroFattura;
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
            return 0;
        }
    }


    public void updateNumeroFatture(int numeroFattura)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }
            execOnDb("UPDATE fattura SET numero_fattura=" + numeroFattura);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Modify failure", e.getMessage());
        }
    }


    public TwoString selectCodeDuration()
    {
        TwoString pairs = new TwoString();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM registered_activation_code LIMIT 1", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    pairs.setFirstString(c.getString(c.getColumnIndex("code")));
                    pairs.setSecondString(c.getString(c.getColumnIndex("registration")));

                }
            }
            c.close();
            database.close();
//
            return pairs;
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
            return pairs;
        }
    }

    public int getCodeDuration(String code)
    {
        int duration = 0;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT duration FROM static_activation_code where code='" + code + "' LIMIT 1", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    duration = c.getInt(c.getColumnIndex("duration"));

                }
            }
            c.close();
            database.close();
//
            return duration;
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
            return duration;
        }
    }




    public void setupDefaultPaymentValues()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }

            database.execSQL("delete from payment_option_button;");
            database.execSQL("INSERT INTO payment_option_button VALUES(1,0,1,'CASH')");
            database.execSQL("INSERT INTO payment_option_button VALUES(2,0,2,'CREDIT CARD')");
            database.execSQL("INSERT INTO payment_option_button VALUES(3,0,4,'BANK CARD')");
            //database.execSQL("INSERT INTO payment_option_button VALUES(4,2,3,'MASTERCARD')");
            //database.execSQL("INSERT INTO payment_option_button VALUES(5,2,3,'VISA')");
            database.execSQL("INSERT INTO payment_option_button VALUES(6,0,5,'TICKETS')");
            database.execSQL("INSERT INTO payment_option_button VALUES(7,0,6,'CREDIT')");
            database.execSQL("INSERT INTO payment_option_button VALUES(8,0,8, 'FIDELITY')");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    public ArrayList<PaymentButton> getPaymentButtons(int parent_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            ArrayList<PaymentButton> array = new ArrayList<>();
            PaymentButton pb;
            Cursor c = database.rawQuery("SELECT * FROM payment_option_button WHERE parent_id =" + parent_id, null);
            while (c.moveToNext())
            {
                pb = new PaymentButton();
                pb.setId(c.getInt(c.getColumnIndex("id")));
                pb.setParent_id(c.getInt(c.getColumnIndex("parent_id")));
                pb.setButton_type(c.getInt(c.getColumnIndex("button_type")));
                pb.setTitle(c.getString(c.getColumnIndex("button_title")));
                array.add(pb);
            }
            c.close();
            database.close();

            return array;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public ArrayList<PaymentButton> getPaymentButtonsForPartial()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            ArrayList<PaymentButton> array = new ArrayList<>();
            PaymentButton pb;
            Cursor c = database.rawQuery("SELECT * FROM payment_option_button WHERE id=1 or id=2 or id=3;", null);
            while (c.moveToNext())
            {
                pb = new PaymentButton();
                pb.setId(c.getInt(c.getColumnIndex("id")));
                pb.setParent_id(c.getInt(c.getColumnIndex("parent_id")));
                pb.setButton_type(c.getInt(c.getColumnIndex("button_type")));
                pb.setTitle(c.getString(c.getColumnIndex("button_title")));
                array.add(pb);
            }
            c.close();
            database.close();

            return array;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public ArrayList<PaymentButton> getOnlyPaymentButtons()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            ArrayList<PaymentButton> array = new ArrayList<>();
            PaymentButton pb;
            Cursor c = database.rawQuery("SELECT * FROM payment_option_button WHERE id=1 or id=2 or id=3 or id=6 or id=7 or id=8;", null);
            while (c.moveToNext())
            {
                pb = new PaymentButton();
                pb.setId(c.getInt(c.getColumnIndex("id")));
                pb.setParent_id(c.getInt(c.getColumnIndex("parent_id")));
                pb.setButton_type(c.getInt(c.getColumnIndex("button_type")));
                pb.setTitle(c.getString(c.getColumnIndex("button_title")));
                array.add(pb);
            }
            c.close();
            database.close();

            return array;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public ArrayList<PaymentButton> getPaymentButtonsBuyFidelity()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getWritableDatabase(); }

            ArrayList<PaymentButton> array = new ArrayList<>();
            PaymentButton pb;
            Cursor c = database.rawQuery("SELECT * FROM payment_option_button WHERE id=1 or id=2 or id=3 or id=6 or id=7;", null);
            while (c.moveToNext())
            {
                pb = new PaymentButton();
                pb.setId(c.getInt(c.getColumnIndex("id")));
                pb.setParent_id(c.getInt(c.getColumnIndex("parent_id")));
                pb.setButton_type(c.getInt(c.getColumnIndex("button_type")));
                pb.setTitle(c.getString(c.getColumnIndex("button_title")));
                array.add(pb);
            }
            c.close();
            database.close();

            return array;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }








    public void insertIntoItemSpecBill(float paidAmount, int paymentType, int billId)
    {
       /* showData("bill_total");
        showData("bill_subdivision_paid");*/
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT id FROM bill_subdivision_paid WHERE bill_id=" + billId, null);
            int a = 15;
            //c.moveToFirst();
            if (c.moveToFirst())
            {
                int subd_id = c.getInt(c.getColumnIndex("id"));

                execOnDb("INSERT INTO item_paid_spec (paid_amount, payment_type,bill_subdivision_paid_id) VALUES(" + paidAmount + "," + paymentType + "," + subd_id + ");");

            }

            c.close();

            //database = dbHelper.getWritableDatabase();
            //execOnDb("INSERT INTO item_paid_spec (paid_amount, payment_type,bill_subdivision_paid_id) VALUES(" +paidAmount+","+paymentType+","+subd_id +");");

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }







    // =============================================== //
    // [ DEPRECATED ]
    // =============================================== //


    public boolean checkIfCategoryIsFullOfProduct(String query)
    {
        try
        {
            Boolean isWholeCat = true;
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            Cursor mCursor = database.rawQuery(query, null);
            if (mCursor != null)
            {
                while (mCursor.moveToNext() && isWholeCat)
                {
                    if (mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_CAT_BOOL)) == 0)
                    {
                        isWholeCat = false;
                    }
                }
                mCursor.close();
            }

            database.close();
            return isWholeCat;
        }

        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return false;
        }
    }

    public boolean checkIfCategoryIsFullOfProduct2(String query)
    {
        try
        {
            Boolean isWholeCat = true;
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            Cursor mCursor = database.rawQuery(query, null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    if (mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_CAT_BOOL)) == 1)
                    {
                        isWholeCat = false;
                    }
                }
                mCursor.close();
            }

            database.close();
            return isWholeCat;
        }

        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return false;
        }
    }

    public Cursor fetchUsers()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM user;", null);
            //database.close();
            //Log.d("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(c));
            c.moveToFirst();
            return c;
        }
        catch (Exception e)
        {
            Log.d("Fetch User Data Error", e.getMessage());
            return null;
        }
    }

    public TotalBill getTotalBill(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            TotalBill totalBill = new TotalBill();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    totalBill.setId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    totalBill.setTotal(mCursor.getFloat(mCursor.getColumnIndex(DatabaseAdapter.KEY_TOTAL)));
                    totalBill.setPaid(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_PAID)));

                    String current = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_CREATION_TIME));
                    Date currentDate = format.parse(current);
                    //Date date = new Date(mCursor.getLong(mCursor.getColumnIndex(DatabaseAdapter.KEY_CREATION_TIME))*1000);
                    totalBill.setCreationTime(currentDate);

                    String pay = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_PAID_TIME));
                    if (pay != null)
                    {
                        Date payDate = format.parse(pay);
                        totalBill.setPaidTime(payDate);
                    }
                    //date = new Date(mCursor.getLong(mCursor.getColumnIndex(DatabaseAdapter.KEY_PAID_TIME))*1000);

                }
                mCursor.close();

            }
            database.close();
            return totalBill;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return null;
        }
    }


    public void insertIntoItemSpec(float paidAmount, int paymentType)
    {
        try
        {
            if (database.isOpen()) { database.close(); }
            Cursor c = database.rawQuery("SELECT id FROM bill_subdivision_paid ORDER BY id DESC", null);
            c.moveToFirst();
            int subd_id = c.getInt(0);


            database = dbHelper.getWritableDatabase();
            execOnDb("INSERT INTO item_paid_spec (paid_amount, payment_type,bill_subdivision_paid_id) VALUES(" + paidAmount + "," + paymentType + "," + subd_id + ");");
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }

    public void insertIntoStatistic1()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO bill_total_statistic(id , total ,paid , creation_time ,pay_time  ,bill_number ,payment_type ,invoice ,print_index)" +
                    " VALUES( (SELECT id FROM bill_total)," +
                    " (SELECT total FROM bill_total) ," +
                    " (SELECT paid FROM bill_total) ," +
                    " (SELECT creation_time FROM bill_total) ," +
                    " (SELECT pay_time FROM bill_total) ," +
                    " (SELECT bill_number FROM bill_total) ," +
                    " (SELECT payment_type FROM bill_total) ," +
                    " (SELECT invoice FROM bill_total) ," +
                    " (SELECT print_index  FROM bill_total) )");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error update statistic", e.getMessage());
        }

    }


    public void inserUserAsync(ArrayList<User> userList)
    {
        try
        {
            if (database.isOpen()) { database.close(); }
            database = dbHelper.getWritableDatabase();
            for (User user : userList)
            {
                database.execSQL("INSERT INTO user (id, name, surname, email,password,userType, passcode) " +
                        "VALUES(" + user.getId() + ", '" + user.getName() + "','" + user.getSurname() + "','" + user
                        .getEmail() + "','" + user.getPasscode() + "'," + user.getUserRole() + " ,'" + user
                        .getPasscode() + "')");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Delete failure", e.getMessage());
        }
    }


    public boolean checkIfUserHasAuthorization(String passcode)
    {
        boolean check = false;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT id FROM user where passcode='" + passcode + "' AND (userType=1 || userType=2) LIMIT 1;", null);

            if (c.moveToFirst())
            {
                String pass = c.getString(0);
                if (pass.equals(passcode))
                {
                    check = true;
                }

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


    public void createSessionTable()
    {

        final String LAST_SESSION_CREATE = "CREATE TABLE IF NOT EXISTS last_session(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "last_session_creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "position INTEGER);";
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL(LAST_SESSION_CREATE);
            database.close();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }














}
