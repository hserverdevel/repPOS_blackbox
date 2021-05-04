/* This class contains methods to communicate with the db */

package com.utils.db;

import android.content.Context;



/**
 * this class act as the intermediary between DatabaseHelper (which is used to write and read from the SQL database)
 * and the rest of the app.
 * Thus here are contained the functions to retrieve and write data to the database
 *
 * Since this class would be enormous, it has been splitted in several other classes,
 * and all the methods are avaible to the class <DataBaseAdapter>, thanks to a chain
 * of inheritance:
 *
 * - DbAdapterInit
 *      - DbAdapterUsers
 *          - DbAdapterProducts
 *              - DbAdapterBills
 *                  - DbAdapterBillExtra
 *                      - DbAdapterTables
 *                          - DbAdapterClients
 *                              - DbAdapterPrinters
 *                                  - DbAdapterFidelity
 *                                      - DbAdapterStatistics
 *                                          - DatabaseAdapter
 *
 * */


@SuppressWarnings("unused")
public class DatabaseAdapter extends DbAdapterStatistics
{
    private static final String TAG = "<DatabaseAdapter>";



    public DatabaseAdapter(Context context)
    {
        this.context = context;
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
    }



    public DatabaseHelper getDbHelper() { return dbHelper; }

}
