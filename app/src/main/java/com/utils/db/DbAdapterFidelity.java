package com.utils.db;

import android.database.Cursor;
import android.util.Log;

import com.example.blackbox.model.Fidelity;
import com.example.blackbox.model.FidelityPackage;

import java.util.ArrayList;

public class DbAdapterFidelity extends DbAdapterPrinters
{

    // =============================================== //
    // [ FIDELITY ]
    // =============================================== //

    public Fidelity fetchFidelityById(int id)
    {
        Fidelity fidelity = new Fidelity();
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c1 = database.rawQuery("SELECT * FROM fidelity WHERE id = " + id, null);
            if (c1.moveToFirst())
            {
                fidelity.setId(c1.getInt(c1.getColumnIndex("id")));
                fidelity.setCode(c1.getString(c1.getColumnIndex("code")));
                fidelity.setRule(c1.getInt(c1.getColumnIndex("rule")));
                fidelity.setActive(c1.getInt(c1.getColumnIndex("active")));
                fidelity.setValue(c1.getDouble(c1.getColumnIndex("value")));
                fidelity.setEarned(c1.getDouble(c1.getColumnIndex("earned")));
                fidelity.setUsed(c1.getDouble(c1.getColumnIndex("used")));
            }
            c1.close();
            database.close();
            return fidelity;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return fidelity;
        }
    }


    public void updateFidelityPoint(double value, double earned, double used, int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getWritableDatabase(); }

            execOnDb("UPDATE fidelity " +
                    " SET value= " + value + ", " +
                    " earned = " + earned + ", " +
                    " used = " + used + " " +
                    " WHERE id=" + id + ";");

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert modifiers Error", e.getMessage());
        }
    }


    public void addFidelityPoint(int id, double amount)
    {
        Fidelity fidelity = fetchFidelityById(id);
        updateFidelityPoint(
                fidelity.getValue() + amount,
                fidelity.getEarned() + amount,
                fidelity.getUsed(),
                id
        );
    }


    public void subtractFidelityPoint(int id, double amount)
    {
        Fidelity fidelity = fetchFidelityById(id);
        if ((fidelity.getValue() - amount) >= 0)
        {
            updateFidelityPoint(
                    fidelity.getValue() - amount,
                    fidelity.getEarned(),
                    fidelity.getUsed() + amount,
                    id);
        }
    }


    public void insertFidelitySync(ArrayList<Fidelity> fidelities)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (Fidelity f : fidelities)
            {
                database.execSQL("INSERT INTO fidelity(id, code, rule, active, value, earned, used) VALUES(" + f
                        .getId() + ", '" + f.getCode() + "'," + f.getRule() + "," + f.getActive() + ", " + f
                        .getValue() + "," + f.getEarned() + ", " + f.getUsed() + " );");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert mA Error", e.getMessage());
        }
    }






    // =============================================== //
    // [ FIDELITY PACKAGES ]
    // =============================================== //


    public ArrayList<FidelityPackage> selectAllFidelityPackages()
    {
        ArrayList<FidelityPackage> array = new ArrayList<>();

        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            Cursor mCursor = database.rawQuery("SELECT * FROM button WHERE catID=-5", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    // a fidelity package does not have a table
                    // but use the button table
                    FidelityPackage fp = new FidelityPackage();

                    fp.setButtonId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    fp.setPrice(mCursor.getDouble(mCursor.getColumnIndex("price")));

                    // the title of a fidelity package button is always "fidelity credit (50)",
                    // thus the number in parenthesis, representing the amount of credits, can be extracted
                    String title = mCursor.getString(mCursor.getColumnIndex("title"));
                    fp.setCreditAmount(Integer.parseInt(title.replaceAll(".+\\s*\\(([0-9]+)\\)\\s*FC", "$1")));

                    fp.setName(title.replaceAll("(.+)\\s+\\([0-9]+\\)", "$1"));

                    array.add(fp);
                }
                mCursor.close();
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Fail on [selectAllFidelityPackages]");
            e.printStackTrace();
        }
        finally
        {
            return array;
        }
    }


    public FidelityPackage fetchFidelityPackageById(int id)
    {
        FidelityPackage fp = new FidelityPackage();
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            Cursor mCursor = database.rawQuery("SELECT * FROM button WHERE catID = -5", null);

            if (mCursor.moveToFirst())
            {
                // a fidelity package does not have a table
                // but use the button table

                fp.setButtonId(mCursor.getInt(mCursor.getColumnIndex("id")));
                fp.setPrice(mCursor.getDouble(mCursor.getColumnIndex("price")));

                // the title of a fidelity package button is always "fidelity credit (50)",
                // thus the number in parenthesis, representing the amount of credits, can be extracted
                String title = mCursor.getString(mCursor.getColumnIndex("title"));
                fp.setCreditAmount(Integer.parseInt(title.replaceAll("fidelity credit \\(([0-9]+)\\)", "$1")));
            }
            mCursor.close();
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
        }
        finally
        {
            return fp;
        }
    }


    public void deleteFidelityPackageById(int id)
    {
        try
        {
            if (!database.isOpen()) { database = dbHelper.getWritableDatabase(); }

            database.execSQL(String.format("DELETE FROM button WHERE id = %s;", id));

            database.close();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
