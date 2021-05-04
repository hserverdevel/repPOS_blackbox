package com.utils.db;

import android.database.Cursor;
import android.util.Log;

import com.example.blackbox.model.FiscalPrinter;
import com.example.blackbox.model.KitchenPrinter;
import com.example.blackbox.model.PrinterInfo;

import java.util.ArrayList;



public class DbAdapterPrinters extends DbAdapterClients
{



    // =============================================== //
    // [ PRINTERS ]
    // =============================================== //

    public void recursiveUpdatePrinter(int categoryId, int printerId)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            Cursor c = database.rawQuery("SELECT * FROM button WHERE catID=" + categoryId, null);
            while (c.moveToNext())
            {
                database.execSQL("UPDATE button SET printer=" + printerId + " WHERE id=" + c.getInt(c
                        .getColumnIndex(DatabaseAdapter.KEY_ID)) + ";");
                database.rawQuery("SELECT * FROM button WHERE catID=" + c.getInt(c.getColumnIndex(DatabaseAdapter.KEY_ID)), null);
                recursiveUpdatePrinter(c.getInt(c.getColumnIndex(KEY_ID)), printerId);
            }
            c.close();


        }
        catch (Exception e)
        {
            Log.d("Delete Error", e.getMessage());
        }
    }


    public PrinterInfo getFiscalPrinter()
    {
        try
        {
            PrinterInfo printerInfo = new PrinterInfo();
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM printer_info WHERE type=0 LIMIT 1;", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    printerInfo.setId(c.getInt(c.getColumnIndex("id")));
                    printerInfo.setName(c.getString(c.getColumnIndex("name")));
                    printerInfo.setCode(c.getInt(c.getColumnIndex("code")));
                    printerInfo.setType(c.getInt(c.getColumnIndex("type")));
                    printerInfo.setIP(c.getString(c.getColumnIndex("IP")));
                }
            }
            c.close();
            database.close();
            return printerInfo;
        }
        catch (Exception e)
        {
            Log.d("Fetch error", e.getMessage());
            return null;
        }
    }


    public ArrayList<PrinterInfo> getNonFiscalPrinter()
    {
        try
        {
            ArrayList<PrinterInfo> array = new ArrayList<PrinterInfo>();

            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM printer_info WHERE type=1;", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    PrinterInfo printerInfo = new PrinterInfo();
                    printerInfo.setId(c.getInt(c.getColumnIndex("id")));
                    printerInfo.setName(c.getString(c.getColumnIndex("name")));
                    printerInfo.setCode(c.getInt(c.getColumnIndex("code")));
                    printerInfo.setType(c.getInt(c.getColumnIndex("type")));
                    printerInfo.setIP(c.getString(c.getColumnIndex("IP")));
                    array.add(printerInfo);

                }
            }
            c.close();
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.d("Fetch error", e.getMessage());
            return null;
        }
    }


    public PrinterInfo getPrinterById(int id)
    {
        try
        {
            PrinterInfo printerInfo = new PrinterInfo();
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM printer_info WHERE id=" + id + " LIMIT 1;", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    printerInfo.setId(c.getInt(c.getColumnIndex("id")));
                    printerInfo.setName(c.getString(c.getColumnIndex("name")));
                    printerInfo.setCode(c.getInt(c.getColumnIndex("code")));
                    printerInfo.setType(c.getInt(c.getColumnIndex("type")));
                    printerInfo.setIP(c.getString(c.getColumnIndex("IP")));
                }
            }
            c.close();
            database.close();
            return printerInfo;
        }
        catch (Exception e)
        {
            Log.d("Fetch error", e.getMessage());
            return null;
        }

    }


    public FiscalPrinter selectFiscalPrinter()
    {
        FiscalPrinter fiscalPrinter = new FiscalPrinter();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM fiscal_printer LIMIT 1", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    fiscalPrinter.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    fiscalPrinter.setModel(mCursor.getString(mCursor.getColumnIndex("name")));
                    fiscalPrinter.setAddress(mCursor.getString(mCursor.getColumnIndex("address")));
                    fiscalPrinter.setPort(mCursor.getInt(mCursor.getColumnIndex("port")));
                    if (mCursor.getInt(mCursor.getColumnIndex("api")) == 1)
                    {
                        fiscalPrinter.setUseApi(true);
                    }
                    else
                    {
                        fiscalPrinter.setUseApi(false);
                    }
                }
                mCursor.close();

            }
            // database.close();
            return fiscalPrinter;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return fiscalPrinter;
        }
    }


    public void insertFiscalPrinter(String name, String address, String model, int port, int api)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO fiscal_printer(name, address, model, port, api) VALUES('" + name + "', '" + address + "', '" + model + "', " + port + ", " + api + ") ");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }


    public void updateFiscalPrinter(String name, String address, String model, int port, int api)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE fiscal_printer set name='" + name + "',  address='" + address + "' , model='" + name + "' , port =" + port + " , api=" + api);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }


    public KitchenPrinter selectKitchenPrinter()
    {
        KitchenPrinter kitchenPrinter = new KitchenPrinter();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM kitchen_printer LIMIT 1", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    kitchenPrinter.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    kitchenPrinter.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    kitchenPrinter.setAddress(mCursor.getString(mCursor.getColumnIndex("address")));
                    kitchenPrinter.setPort(mCursor.getInt(mCursor.getColumnIndex("port")));
                    if (mCursor.getInt(mCursor.getColumnIndex("single_order")) == 1)
                    {
                        kitchenPrinter.setSingleOrder(mCursor.getInt(mCursor.getColumnIndex("single_order")));
                    }
                    else
                    {
                        kitchenPrinter.setSingleOrder(mCursor.getInt(mCursor.getColumnIndex("single_order")));
                    }

                }
                mCursor.close();

            }
            // database.close();
            return kitchenPrinter;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return kitchenPrinter;
        }
    }


    public ArrayList<KitchenPrinter> selectAllKitchenPrinter()
    {
        ArrayList<KitchenPrinter> array = new ArrayList<>();

        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM kitchen_printer", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    KitchenPrinter kitchenPrinter = new KitchenPrinter();
                    kitchenPrinter.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    kitchenPrinter.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    kitchenPrinter.setAddress(mCursor.getString(mCursor.getColumnIndex("address")));
                    kitchenPrinter.setPort(mCursor.getInt(mCursor.getColumnIndex("port")));
                    if (mCursor.getInt(mCursor.getColumnIndex("single_order")) == 1)
                    {
                        kitchenPrinter.setSingleOrder(mCursor.getInt(mCursor.getColumnIndex("single_order")));
                    }
                    else
                    {
                        kitchenPrinter.setSingleOrder(mCursor.getInt(mCursor.getColumnIndex("single_order")));
                    }
                    array.add(kitchenPrinter);

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


    public KitchenPrinter selectKitchenPrinterByName(String name)
    {
        KitchenPrinter kitchenPrinter = new KitchenPrinter();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM kitchen_printer WHERE name='" + name + "' LIMIT 1", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    kitchenPrinter.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    kitchenPrinter.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    kitchenPrinter.setAddress(mCursor.getString(mCursor.getColumnIndex("address")));
                    kitchenPrinter.setPort(mCursor.getInt(mCursor.getColumnIndex("port")));
                    if (mCursor.getInt(mCursor.getColumnIndex("single_order")) == 1)
                    {
                        kitchenPrinter.setSingleOrder(mCursor.getInt(mCursor.getColumnIndex("single_order")));
                    }
                    else
                    {
                        kitchenPrinter.setSingleOrder(mCursor.getInt(mCursor.getColumnIndex("single_order")));
                    }


                }
                mCursor.close();

            }
            // database.close();
            return kitchenPrinter;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return kitchenPrinter;
        }
    }


    public void insertKitchenPrinter(String name, String address, int port, int singleOrder)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO kitchen_printer(name, address, port, single_order) VALUES('" + name + "', '" + address + "', " + port + "," + singleOrder + " ) ");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }


    public void updateKitchenPrinter(String name, String address, int port, int singleOrder, int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE kitchen_printer set name='" + name + "',  address='" + address + "' , port =" + port + ", single_order=" + singleOrder + " where id = " + id);
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }


    public void insertKitchenPrinterSync(ArrayList<KitchenPrinter> kitchenPrinters)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (KitchenPrinter k : kitchenPrinters)
            {
                database.execSQL("INSERT INTO kitchen_printer(id, name, address, port, single_order) VALUES(" + k
                        .getId() + ",  '" + k.getName() + "', '" + k.getAddress() + "', " + k.getPort() + "," + k
                        .isSingleOrder() + " ) ");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }

    }


    public void insertFiscalPrinterSync(FiscalPrinter f)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            int api = 0;
            if (f.isUseApi()) { api = 1; }
            database.execSQL("INSERT INTO fiscal_printer(id, name, address, model, port, api) VALUES(" + f
                    .getId() + " ,  '" + f.getModel() + "', '" + f.getAddress() + "', '" + f.getModel() + "', " + f
                    .getPort() + ", " + api + ") ");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error closing time", e.getMessage());
        }
    }

}
