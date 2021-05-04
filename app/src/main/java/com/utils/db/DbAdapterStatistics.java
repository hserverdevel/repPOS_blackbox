package com.utils.db;

import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DbAdapterStatistics extends DbAdapterFidelity
{

    // =============================================== //
    // [ STATISTICS ]
    // =============================================== //

    //type: solo vat oppure importo netto: 1 -> importo netto, 2 -> iva
    public float getDailyTotal(int type)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>='" + yesterday + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    public float getWeeklyTotal(int type)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, 2);
            String monday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>='" + monday + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            assert c3 != null;
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    public float getMonthlyTotal(int type)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String firstOfMonth = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>='" + firstOfMonth + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    public float getAnnuallyTotal(int type)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_YEAR, 1);
            String firstOfYear = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>='" + firstOfYear + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    //type: solo vat oppure importo netto: 1 -> importo netto, 2 -> iva
    public float getLastYearDailyTotal(int type, int year, int month, int day)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar nowDate = Calendar.getInstance();
            nowDate.set(year, month, day);
            String now = formatPre.format(nowDate);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total_statistic WHERE pay_time<='" + now +
                    "' AND creation_time>='" + yesterday + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    public float getLastYearWeeklyTotal(int type, int year, int month, int day)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar nowDate = Calendar.getInstance();
            nowDate.set(year, month, day);
            String now = formatPre.format(nowDate);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, 2);
            String monday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total_statistic WHERE pay_time<='" + now +
                    "' AND creation_time>='" + monday + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    public float getLastYearMonthlyTotal(int type, int year, int month, int day)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar nowDate = Calendar.getInstance();
            nowDate.set(year, month, day);
            String now = formatPre.format(nowDate);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String firstOfMonth = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total_statistic WHERE pay_time<='" + now +
                    "' AND creation_time>='" + firstOfMonth + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    public float getLastYearAnnuallyTotal(int type, int year, int month, int day)
    {
        float ivato = 0.0f;
        float lordo = 0.0f;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar nowDate = Calendar.getInstance();
            nowDate.set(year, month, day);
            String now = formatPre.format(nowDate);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_YEAR, 1);
            String firstOfYear = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total_statistic WHERE pay_time<='" + now +
                    "' AND creation_time>='" + firstOfYear + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int billid = c.getInt(c.getColumnIndex("id"));
                    lordo += c.getFloat(c.getColumnIndex("total"));

                    Cursor c1 = database.rawQuery("SELECT * FROM product_bill WHERE bill_id=" + billid + ";", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            int id = c1.getInt(c1.getColumnIndex("id"));
                            int prodId = c1.getInt(c1.getColumnIndex("prod_id"));

                            Cursor c2 = database.rawQuery("SELECT * FROM button WHERE id=" + prodId + ";", null);
                            if (c2 != null)
                            {
                                while (c2.moveToNext())
                                {
                                    float price = c2.getFloat(c2.getColumnIndex("price"));
                                    int ivaID = c2.getInt(c2.getColumnIndex("vat"));
                                    double iva = (double) ivaID / 100;

                                    if (iva != 0)
                                    { ivato += (price * iva); }
                                }
                            }
                            c2.close();

                            Cursor c3 = database.rawQuery("SELECT * FROM modifier_bill WHERE prod_bill_id=" + id + ";", null);
                            if (c3 != null)
                            {
                                while (c3.moveToNext())
                                {
                                    int modId = c3.getInt(c3.getColumnIndex("mod_id"));

                                    Cursor c4 = database.rawQuery("SELECT * FROM modifier WHERE id=" + modId + ";", null);
                                    if (c4 != null)
                                    {
                                        while (c4.moveToNext())
                                        {
                                            float price = c4.getFloat(c4.getColumnIndex("price"));
                                            int ivaID = c4.getInt(c4.getColumnIndex("vat"));
                                            double iva = (double) ivaID / 100;

                                            if (iva != 0)
                                            { ivato += (price * iva); }
                                        }
                                    }
                                    c4.close();
                                }
                            }
                            c3.close();
                        }
                    }
                    c1.close();
                }
            }
            c.close();

            database.close();

            if (type == 1)
            { return (lordo - ivato); }
            else
            { return ivato; }
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0.0f;
        }
    }


    public int getDailyPayment(int paymentType)
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE paid=1 AND pay_time<='" + now
                    + "' AND creation_time>='" + yesterday + "' AND payment_type=" + paymentType + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    total++;
                }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return total;
        }
    }


    public int getClientCreditPayment()
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now
                    + "' AND creation_time>='" + yesterday + "' AND payment_type=" + 6 + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    total++;
                }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return total;
        }
    }


    public int getDailyOrderInfo(int orderType)
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE creation_time>='"
                    + yesterday + "' AND paid=" + orderType + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    total++;
                }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return total;
        }
    }


    public int getTotalTableOrders()
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM table_use WHERE end_time <='" + now +
                    "' AND start_time >'" + yesterday + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                { total++; }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return total;
        }
    }


    public int getTotalTakeAwayOrders()
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>'" + yesterday + "' AND id NOT IN (SELECT total_bill_id FROM table_use);", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    total++;
                }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return total;
        }
    }


    public int getInvoiceOrders()
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>'" + yesterday + "' AND invoice=1;", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    total++;
                }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return total;
        }
    }


    public int getTotalDiscountOrders()
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>'" + yesterday + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int id = c.getInt(c.getColumnIndex("id"));
                    Cursor c1 = database.rawQuery("SELECT * FROM bill_total_extra WHERE bill_total_id=" + id
                            + " AND homage IS NOT 1;", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            total++;
                        }
                    }
                    c1.close();
                }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return total;
        }
    }


    public int getHomageOrders()
    {
        int total = 0;
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = formatPre.format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = formatPre.format(cal.getTime());

            Cursor c = database.rawQuery("SELECT * FROM bill_total WHERE pay_time<='" + now +
                    "' AND creation_time>'" + yesterday + "';", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int id = c.getInt(c.getColumnIndex("id"));
                    Cursor c1 = database.rawQuery("SELECT * FROM bill_total_extra WHERE bill_total_id=" + id
                            + " AND homage=1;", null);
                    if (c1 != null)
                    {
                        while (c1.moveToNext())
                        {
                            total++;
                        }
                    }
                    c1.close();
                }
            }
            c.close();
            database.close();

            return total;
        }
        catch (Exception e)
        {
            Log.d("Fetching Error", e.getLocalizedMessage());
            return 0;
        }
    }


    public void insertIntoStatistic()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            //bill total
            Log.i("TAG", "" + 1);
            database.execSQL("INSERT INTO bill_total_statistic(id , total ,paid , creation_time ,pay_time  ,bill_number ,payment_type ,invoice ,print_index)" +
                    " SELECT id ," +
                    " total  ," +
                    " paid  ," +
                    " creation_time ," +
                    " pay_time ," +
                    " bill_number ," +
                    " payment_type ," +
                    " invoice ," +
                    " print_index  FROM bill_total WHERE id NOT IN ( SELECT id FROM bill_total_statistic)");
            //product bill
            Log.i("TAG", "" + 2);
            database.execSQL("INSERT INTO product_bill_statistic" +
                    "               (id , " +
                    "position ," +
                    "prod_id , " +
                    "qty ," +
                    "bill_id  ," +
                    "homage ," +
                    "discount )" +
                    " SELECT id ," +
                    " position  ," +
                    " prod_id  ," +
                    " qty ," +
                    " bill_id ," +
                    " homage ," +
                    " discount  FROM product_bill WHERE id NOT IN ( SELECT id FROM product_bill_statistic)");

            //product product_unspec_bill_statistic
            Log.i("TAG", "" + 3);
            database.execSQL("INSERT INTO product_unspec_bill_statistic" +
                    "               (id , " +
                    "prod_bill_id ," +
                    "price , " +
                    "description )" +
                    " SELECT id ," +
                    "prod_bill_id ," +
                    "price , " +
                    "description  FROM product_unspec_bill WHERE id NOT IN ( SELECT id FROM product_unspec_bill_statistic)");
            //modifier bill
            Log.i("TAG", "" + 4);
            database.execSQL("INSERT INTO modifier_bill_statistic" +
                    "               (id , " +
                    "position ," +
                    "mod_id , " +
                    "qty ," +
                    "prod_bill_id  ," +
                    "homage ," +
                    "discount )" +
                    " SELECT id ," +
                    " position  ," +
                    " mod_id  ," +
                    " qty ," +
                    " prod_bill_id ," +
                    " homage ," +
                    " discount  FROM modifier_bill WHERE id NOT IN ( SELECT id FROM modifier_bill_statistic)");
            //modifier bill
            Log.i("TAG", "" + 5);
            database.execSQL("INSERT INTO modifier_bill_notes_statistic" +
                    "               (id , " +
                    "modifier_bill_id ," +
                    "note )" +
                    " SELECT id ," +
                    " modifier_bill_id  ," +
                    " note  FROM modifier_bill_notes WHERE id NOT IN ( SELECT id FROM modifier_bill_notes_statistic)");
            //bill_total_customer_invoice
            Log.i("TAG", "" + 6);
            database.execSQL("INSERT INTO bill_total_customer_invoice_statistic" +
                    "               (id , " +
                    "bill_total_id ," +
                    "client_id )" +
                    " SELECT id ," +
                    " bill_total_id  ," +
                    " client_id  FROM bill_total_customer_invoice WHERE id NOT IN ( SELECT id FROM bill_total_customer_invoice_statistic)");
            //customer_bill bill
            Log.i("TAG", "" + 7);
            database.execSQL("INSERT INTO customer_bill_statistic" +
                    "               (id , " +
                    "position ," +
                    "description , " +
                    "client_id ," +
                    "prod_bill_id )" +
                    " SELECT id ," +
                    " position  ," +
                    " description  ," +
                    " client_id ," +
                    " prod_bill_id  FROM customer_bill WHERE id NOT IN ( SELECT id FROM customer_bill_statistic)");
            //bill_total_credit_statistic
            Log.i("TAG", "" + 8);
            database.execSQL("INSERT INTO bill_total_credit_statistic" +
                    "               (id , " +
                    "creditValue ," +
                    "bill_total_id )" +
                    " SELECT id ," +
                    " creditValue  ," +
                    " bill_total_id  FROM bill_total_credit WHERE id NOT IN ( SELECT id FROM bill_total_credit_statistic)");
            //bill_total_extra_statistic
            Log.i("TAG", "" + 9);
            database.execSQL("INSERT INTO bill_total_extra_statistic" +
                    "               (id , " +
                    "discountTotal ," +
                    "homage ," +
                    "bill_total_id )" +
                    " SELECT id ," +
                    " discountTotal  ," +
                    "homage ," +
                    " bill_total_id  FROM bill_total_extra WHERE id NOT IN ( SELECT id FROM bill_total_extra_statistic)");
            //bill_subdivision_paid_statistic
            Log.i("TAG", "" + 10);
            database.execSQL("INSERT INTO bill_subdivision_paid_statistic" +
                    "               (id , " +
                    "bill_id ," +
                    "subdivision_mode , " +
                    "subdivision_value ," +
                    "paid_amount  ," +
                    "payment_type  ," +
                    "homage ," +
                    "discount ," +
                    "invoice )" +
                    " SELECT id ," +
                    "bill_id ," +
                    "subdivision_mode , " +
                    "subdivision_value ," +
                    "paid_amount  ," +
                    "payment_type  ," +
                    "homage ," +
                    "discount ," +
                    "invoice  FROM bill_subdivision_paid WHERE id NOT IN ( SELECT id FROM bill_subdivision_paid_statistic)");

            //item_subdivisions_statistic
            Log.i("TAG", "" + 11);
            database.execSQL("INSERT INTO item_subdivisions_statistic" +
                    "               (id , " +
                    "bill_subdivision_id ," +
                    "product_bill_id , " +
                    "quantity ," +
                    "discount  ," +
                    "percentage  ," +
                    "price )" +
                    " SELECT id ," +
                    "bill_subdivision_id ," +
                    "product_bill_id , " +
                    "quantity ," +
                    "discount  ," +
                    "percentage  ," +
                    "price FROM item_subdivisions WHERE id NOT IN ( SELECT id FROM item_subdivisions_statistic)");
            //item_paid_spec_statistic
            Log.i("TAG", "" + 12);
            database.execSQL("INSERT INTO item_paid_spec_statistic" +
                    "               (id , " +
                    "paid_amount ," +
                    "payment_type , " +
                    "bill_subdivision_paid_id )" +
                    " SELECT id ," +
                    "paid_amount ," +
                    "payment_type , " +
                    "bill_subdivision_paid_id FROM item_paid_spec WHERE id NOT IN ( SELECT id FROM item_paid_spec_statistic)");
            database.execSQL("DELETE FROM item_paid_spec");
            database.execSQL("DELETE FROM item_subdivisions");
            database.execSQL("DELETE FROM bill_subdivision_paid");
            database.execSQL("DELETE FROM bill_total_extra");
            database.execSQL("DELETE FROM bill_total_credit");
            database.execSQL("DELETE FROM customer_bill");
            database.execSQL("DELETE FROM bill_total_customer_invoice");
            database.execSQL("DELETE FROM modifier_bill_notes");
            database.execSQL("DELETE FROM modifier_bill");
            database.execSQL("DELETE FROM product_unspec_bill");
            database.execSQL("DELETE FROM product_bill");
            database.execSQL("DELETE FROM bill_total");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Error update statistic", e.getMessage());
        }

    }


}
