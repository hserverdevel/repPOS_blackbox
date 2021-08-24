package com.utils.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.blackbox.adapter.OModifierGroupAdapter;
import com.example.blackbox.model.Reservation;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.Table;
import com.example.blackbox.model.TableUse;
import com.example.blackbox.model.WaitingListModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DbAdapterTables extends DbAdapterBillExtra
{



    // =============================================== //
    // [ TABLES ]
    // =============================================== //

    //SELECT * FROM temp_table WHERE table_number=?;
    public boolean checkIfNumberTableExists(String query)
    {
        try
        {
            Boolean rowExist = false;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
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
            Log.e("fetchFailure", e.getMessage());
            return false;
        }
    }


    //return table_temp id
    public int getBillIdFromTempTableId(int tableNumber)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT * FROM temp_table " +
                    "WHERE table_number=" + tableNumber + ";", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    id = mCursor.getInt(2);//to get id, 0 is the column index
                    //     database.close();
                }
                mCursor.close();
            }
            database.close();

            return id;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public void deleteFromTempTableByTableId(int tableId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM temp_table WHERE table_number = " + tableId);
            database.close();

        }
        catch (Exception e)
        {
            Log.e("DELETE ERROR", e.getMessage());
        }
    }


    public void insertIntoTempTable(int tableNumber, int totalBillId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO temp_table(table_number,total_bill_id) VALUES(" + tableNumber + ", " + totalBillId + ")");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert User Error", e.getMessage());
        }
    }


    public void saveNewTempTable(String query, int tableNumber, int totalBillId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //updatePosition
                    database = dbHelper.getWritableDatabase();
                    database.execSQL("UPDATE temp_table SET table_number=" + tableNumber + " WHERE id=" + mCursor
                            .getInt(0) + ";");
                    //database.close();
                }
                else
                {
                    //create new
                    database = dbHelper.getWritableDatabase();
                    database.execSQL("INSERT INTO temp_table(table_number,total_bill_id) VALUES(" + tableNumber + "," + totalBillId + ")");
                    //database.close();
                }
            }
            mCursor.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());

        }
    }


    public int getOldTablePosition(String query)
    {
        int oldPosition = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    Cursor mCursor2 = database.rawQuery("SELECT * FROM table_configuration where id=" + mCursor
                            .getInt(1), null);
                    if (mCursor2 != null)
                    {
                        while (mCursor2.moveToNext())
                        {
                            oldPosition = mCursor2.getInt(1);
                        }
                    }
                    mCursor2.close();
                    // oldPosition = mCursor.getInt(1);//to get id, 0 is the column index

                }
                mCursor.close();

            }
            database.close();
            return oldPosition;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return oldPosition;
        }
    }


    public int getTableNumberId(int bill_number)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT * FROM table_use WHERE " +
                    "total_bill_id =" + bill_number + ";", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    id = mCursor.getInt(mCursor.getColumnIndex("main_table_number"));
                    //id = mCursor.getInt(1);//to get id, 0 is the column index

                }
                mCursor.close();
            }
            database.close();
            return id;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public void updateTableConfiguration(int roomId, Integer position)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * from table_configuration where  room_id=" + roomId, null);
            while (c.moveToNext())
            {
                //name = cursor.getString(column_index);//to get other values
                if (c.getInt(1) == position)
                {
                    //delete
                    database.execSQL("DELETE FROM table_configuration WHERE id=" + c.getInt(0) + ";");

                }
                else if (c.getInt(1) > position)
                {
                    database.execSQL("UPDATE table_configuration SET table_number=" + (c.getInt(1) - 1) + " WHERE id=" + c
                            .getInt(0) + ";");

                }


            }
            c.close();

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert Cash List Error", e.getMessage());
        }
    }


    public ArrayList<Table> fetchTablesName()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM table_configuration GROUP BY table_name", null);
            ArrayList<Table> array = new ArrayList<>(); //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    Table c = new Table();
                    c.setId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTableNumber(mCursor.getInt(mCursor.getColumnIndex("table_number")));
                    c.setPeopleNumber(mCursor.getInt(mCursor.getColumnIndex("seat_number")));
                    c.setTableName(mCursor.getString(mCursor.getColumnIndex("table_name")));
                    c.setMergeTable(mCursor.getInt(mCursor.getColumnIndex("merge_table")));
                    c.setShareTable(mCursor.getInt(mCursor.getColumnIndex("share_table")));
                    c.setRoomId(mCursor.getInt(mCursor.getColumnIndex("room_id")));

                    //TODO add time


                    Cursor mCursor2 = database.rawQuery("SELECT COUNT(DISTINCT table_name) FROM table_configuration WHERE table_name='" + mCursor
                            .getString(mCursor.getColumnIndex("table_name")) + "'", null);
                    if (mCursor2 != null)
                    {
                        while (mCursor2.moveToNext())
                        {
                            c.setRoomId(mCursor2.getInt(0));
                        }
                    }
                    mCursor2.close();
                    array.add(c);

                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            Log.e("fetchFailure", e.getMessage());

            ArrayList<Table> newarray = new ArrayList<Table>();
            return newarray;

        }
    }


    public ArrayList<Table> fetchTables(int roomId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM table_configuration where room_id=" + roomId, null);
            ArrayList<Table> array = new ArrayList<>(); //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    Table c = new Table();
                    c.setId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTableNumber(mCursor.getInt(mCursor.getColumnIndex("table_number")));
                    c.setPeopleNumber(mCursor.getInt(mCursor.getColumnIndex("seat_number")));
                    c.setTableName(mCursor.getString(mCursor.getColumnIndex("table_name")));
                    c.setMergeTable(mCursor.getInt(mCursor.getColumnIndex("merge_table")));
                    c.setShareTable(mCursor.getInt(mCursor.getColumnIndex("share_table")));
                    c.setRoomId(roomId);

                    //TODO add time
                    array.add(c);
                }
                mCursor.close();
                database.close();
            }
            return array;
        }
        catch (Exception e)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            Log.e("fetchFailure", e.getMessage());

            ArrayList<Table> newarray = new ArrayList<Table>();
            return newarray;

        }
    }


    public Table getTableById(int tableId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM table_configuration where id=" + tableId, null);
            Table c = new Table();

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    c.setId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTableNumber(mCursor.getInt(mCursor.getColumnIndex("table_number")));
                    c.setPeopleNumber(mCursor.getInt(mCursor.getColumnIndex("seat_number")));
                    c.setTableName(mCursor.getString(mCursor.getColumnIndex("table_name")));
                    c.setMergeTable(mCursor.getInt(mCursor.getColumnIndex("merge_table")));
                    c.setShareTable(mCursor.getInt(mCursor.getColumnIndex("share_table")));
                    c.setRoomId(mCursor.getInt(mCursor.getColumnIndex("room_id")));

                    //TODO add time
                }
                mCursor.close();
                database.close();
            }
            return c;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());

            Table newarray = new Table();
            return newarray;

        }
    }


    public void insertTableConfiguration(int tableNumber, int seatNumber, int roomId, String tableName, int mergeTable, int shareTable)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            {
                database = dbHelper.getWritableDatabase(); //int f = (admin?1:0);
            }
            database.execSQL("INSERT INTO table_configuration(table_number, seat_number, room_id,table_name, merge_table, share_table) VALUES(" + tableNumber + "," + seatNumber + "," + roomId + ",'" + tableName + "'," + mergeTable + "," + shareTable + ")");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert Device Error", e.getMessage());
        }
    }


    public int selectDistinctValueTable()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT COUNT(DISTINCT table_name) AS nameCount FROM table_configuration", null);

            int count = 0;
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    count = mCursor.getInt(mCursor.getColumnIndex("nameCount"));
                    //TODO add time
                }
                mCursor.close();

            }
            database.close();
            return count;
        }
        catch (Exception e)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            return 0;

        }
    }


    public void updateTable(int tableNumber, int seatNumber, int roomId, String tableName, int mergeTable, int shareTable, int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE table_configuration SET table_number=" + tableNumber + ", " +
                    "seat_number=" + seatNumber + "," +
                    "room_id=" + roomId + "," +
                    "table_name='" + tableName + "'," +
                    "merge_table=" + mergeTable + ", " +
                    "share_table=" + shareTable +
                    " WHERE id=" + id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("UPDATE Total credit", e.getMessage());
        }

    }


    public ArrayList<TableUse> fetchTableUses1(int roomId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT table_configuration.id AS tcID," +
                    " table_configuration.table_number AS tcTNUMBER," +
                    " table_configuration.seat_number AS tcSN," +
                    " table_configuration.room_id AS tcRID," +
                    " table_configuration.table_name AS tcTNAME," +
                    " table_configuration.merge_table AS tcMT," +
                    " table_configuration.share_table AS tcST," +
                    " table_use.id AS tuID," +
                    " table_use.total_seat AS tuTS," +
                    " table_use.start_time AS tuST," +
                    " table_use.end_time AS tuET," +
                    " table_use.total_bill_id AS tuTBID " +
                    " FROM table_configuration " +
                    " LEFT JOIN table_use ON table_configuration.id=table_use.table_id" +
                    " LEFT JOIN reservation ON table_configuration.id=reservation.table_use_id" +
                    " WHERE table_configuration.room_id=" + roomId, null);
            ArrayList<TableUse> array = new ArrayList<TableUse>(); //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    TableUse c = new TableUse();
                    c.setId(mCursor.getInt(mCursor.getColumnIndex("tuID")));
                    c.setBillId(mCursor.getInt(mCursor.getColumnIndex("tuTBID")));
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startString = mCursor.getString(mCursor.getColumnIndex("tuST"));
                    if (startString != null)
                    {
                        Date startDate = format.parse(startString);
                        c.setStartTime(startDate);
                    }
                    String endString = mCursor.getString(mCursor.
                                                                        getColumnIndex("tuET"));
                    if (
                            endString != null)
                    {
                        Date endDate = format.parse(endString);
                        c.setEndTime(endDate);
                    }
                    c.setMergeTable(mCursor.getInt(mCursor.getColumnIndex("tcMT")));
                    c.setShareTable(mCursor.getInt(mCursor.getColumnIndex("tcST")));
                    c.setTableName(mCursor.getString(mCursor.getColumnIndex("tcTNAME")));
                    c.setTableNumber(mCursor.getInt(mCursor.getColumnIndex("tcTNUMBER")));
                    c.setTotalSeats(mCursor.getInt(mCursor.getColumnIndex("tuTS")));
                    array.add(c);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            ArrayList<TableUse> newarray = new ArrayList<TableUse>();
            return newarray;

        }
    }


    public ArrayList<TableUse> fetchTableUses(int roomId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat formatPre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Date datePre = new Date(System.currentTimeMillis()-5*60*1000);
            Date datePre = new Date(System.currentTimeMillis());
            String fiveminute = formatPre.format(datePre);
            String now = formatPre.format(new Date());


            Cursor mCursor = database.rawQuery("SELECT table_configuration.id AS tcID," +
                    " table_configuration.table_number AS tcTNUMBER," +
                    " table_configuration.seat_number AS tcSN," +
                    " table_configuration.room_id AS tcRID," +
                    " table_configuration.table_name AS tcTNAME," +
                    " table_configuration.merge_table AS tcMT," +
                    " table_configuration.share_table AS tcST" +
                    " FROM table_configuration " +
                    "WHERE table_configuration.room_id=" + roomId, null);

            ArrayList<TableUse> array = new ArrayList<TableUse>(); //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    TableUse c = new TableUse();
                    c.setTableId(mCursor.getInt(mCursor.getColumnIndex("tcID")));
                    c.setMergeTable(mCursor.getInt(mCursor.getColumnIndex("tcMT")));
                    c.setShareTable(mCursor.getInt(mCursor.getColumnIndex("tcST")));
                    c.setTableName(mCursor.getString(mCursor.getColumnIndex("tcTNAME")));
                    c.setTableNumber(mCursor.getInt(mCursor.getColumnIndex("tcTNUMBER")));
                    c.setTableSeat(mCursor.getInt(mCursor.getColumnIndex("tcSN")));
                    c.setRoomId(mCursor.getInt(mCursor.getColumnIndex("tcRID")));

                    if (mCursor.getInt(mCursor.getColumnIndex("tcST")) == 1)
                    {
                        Cursor mCursor4 = database.rawQuery("SELECT SUM(total_seat)" +
                                " FROM table_use " +
                                " LEFT JOIN bill_total" +
                                " ON table_use.total_bill_id=bill_total.id" +
                                " WHERE table_id=" + mCursor.getInt(mCursor.getColumnIndex("tcID")) +
                                " AND ((bill_total.paid=0) OR (bill_total.paid=1 AND bill_total.pay_time>='" + fiveminute + "' AND bill_total.pay_time<='" + now + "'))", null);

                        if (mCursor4 != null)
                        {
                            while (mCursor4.moveToNext())
                            {
                                c.setTotalSeats(mCursor4.getInt(0));
                            }
                        }
                        mCursor4.close();
                        array.add(c);
                        Cursor mCursor3 = database.rawQuery("SELECT " +
                                " table_use.id AS tuID," +
                                " table_use.total_seat AS tuTS," +
                                " table_use.start_time AS tuST," +
                                " bill_total.pay_time AS tuET," +
                                " table_use.total_bill_id AS tuTBID, " +
                                " table_use.main_table AS tuMT," +
                                " table_use.main_table_number AS tuMTN" +
                                " FROM table_use " +
                                " LEFT JOIN bill_total" +
                                " ON table_use.total_bill_id=bill_total.id" +
                                " WHERE table_id=" + mCursor.getInt(mCursor.getColumnIndex("tcID")) +
                                " AND ((bill_total.paid=0) OR (bill_total.paid=1 AND bill_total.pay_time>='" + fiveminute + "' AND bill_total.pay_time<='" + now + "'))", null);


                        if (mCursor3 != null)
                        {
                            while (mCursor3.moveToNext())
                            {
                                TableUse c1 = new TableUse();
                                c1.setTableId(mCursor.getInt(mCursor.getColumnIndex("tcID")));
                                c1.setMergeTable(mCursor.getInt(mCursor.getColumnIndex("tcMT")));
                                c1.setShareTable(mCursor.getInt(mCursor.getColumnIndex("tcST")));
                                c1.setTableName(mCursor.getString(mCursor.getColumnIndex("tcTNAME")));
                                c1.setTableNumber(mCursor.getInt(mCursor.getColumnIndex("tcTNUMBER")));
                                c1.setTableSeat(mCursor.getInt(mCursor.getColumnIndex("tcSN")));
                                c1.setId(mCursor3.getInt(mCursor3.getColumnIndex("tuID")));
                                c1.setBillId(mCursor3.getInt(mCursor3.getColumnIndex("tuTBID")));

                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                String startString = mCursor3.getString(mCursor3.getColumnIndex("tuST"));
                                if (startString != null)
                                {
                                    Date startDate = format.parse(startString);
                                    c1.setStartTime(startDate);
                                }

                                String endString = mCursor3.getString(mCursor3.getColumnIndex("tuET"));
                                if (endString != null)
                                {
                                    Date endDate = format.parse(endString);
                                    c1.setEndTime(endDate);
                                }

                                c1.setTotalSeats(mCursor3.getInt(mCursor3.getColumnIndex("tuTS")));
                                c1.setMainTable(mCursor3.getInt(mCursor3.getColumnIndex("tuMT")));
                                c1.setMainTableNumber(mCursor3.getInt(mCursor3.getColumnIndex("tuMTN")));
                                array.add(c1);
                            }

                            mCursor3.close();
                        }

                    }

                    else
                    {
                        Cursor mCursor2 = database.rawQuery("SELECT " +
                                " table_use.id AS tuID," +
                                " table_use.total_seat AS tuTS," +
                                " table_use.start_time AS tuST," +
                                " bill_total.pay_time AS tuET," +
                                " table_use.total_bill_id AS tuTBID, " +
                                " table_use.main_table AS tuMT," +
                                " table_use.main_table_number AS tuMTN" +
                                " FROM table_use " +
                                " LEFT JOIN bill_total" +
                                " ON table_use.total_bill_id=bill_total.id" +
                                " WHERE table_id=" + c.getTableId()/*mCursor.getInt(mCursor.getColumnIndex("tcID"))*/ +
                                " AND ((bill_total.paid=0) OR (bill_total.paid=1 AND bill_total.pay_time>='" + fiveminute + "' AND bill_total.pay_time<='" + now + "'))", null);

                        if (mCursor2 != null)
                        {
                            while (mCursor2.moveToNext())
                            {
                                c.setId(mCursor2.getInt(mCursor2.getColumnIndex("tuID")));
                                c.setBillId(mCursor2.getInt(mCursor2.getColumnIndex("tuTBID")));
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                String startString = mCursor2.getString(mCursor2.getColumnIndex("tuST"));
                                if (startString != null)
                                {
                                    Date startDate = format.parse(startString);
                                    c.setStartTime(startDate);
                                }
                                String endString = mCursor2.getString(mCursor2.getColumnIndex("tuET"));
                                if (endString != null)
                                {
                                    Date endDate = format.parse(endString);
                                    c.setEndTime(endDate);
                                }
                                c.setTotalSeats(mCursor2.getInt(mCursor2.getColumnIndex("tuTS")));
                                c.setMainTable(mCursor2.getInt(mCursor2.getColumnIndex("tuMT")));
                                c.setMainTableNumber(mCursor2.getInt(mCursor2.getColumnIndex("tuMTN")));
                            }
                            mCursor2.close();
                        }
                        array.add(c);
                    }

                }
                mCursor.close();
                database.close();
            }
            return array;
        }

        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            ArrayList<TableUse> newarray = new ArrayList<TableUse>();
            return newarray;

        }
    }


    public long insertTableUse(int tableId, int seatNumber, int billId, int mainTable, int tableNumber)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            {
                database = dbHelper.getWritableDatabase(); //int f = (admin?1:0);
            }
            ContentValues insertValues = new ContentValues();
            insertValues.put("table_id", tableId);
            insertValues.put("total_seat", seatNumber);
            Date dat = new Date();
            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String a = date.format(dat);
            insertValues.put("start_time", date.format(dat));
            insertValues.put("total_bill_id", billId);
            insertValues.put("main_table", mainTable);
            insertValues.put("main_table_number", tableNumber);
            long id = database.insert("table_use", null, insertValues);
            //database.insert("INSERT INTO table_use(table_id, total_seat,start_time, total_bill_id, main_table, main_table_number) VALUES("+tableId+","+seatNumber+",datetime(CURRENT_TIMESTAMP, 'localtime'),"+billId+","+mainTable+","+tableNumber+")");
            database.close();
            return id;
        }
        catch (Exception e)
        {
            Log.e("Insert Device Error", e.getMessage());
            return -1;
        }
    }


    public void updateTableUse(int tableId, int seatNumber, int id, int mainTable, int tableNumber)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE table_use SET table_id=" + tableId + ", " +
                    "total_seat=" + seatNumber +
                    ", main_table=" + mainTable +
                    ", main_table_number=" + tableNumber +
                    " WHERE id=" + id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("UPDATE table USE", e.getMessage());
        }

    }


    public TableUse fetchTableUseById(long id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            TableUse c = new TableUse();
            Cursor mCursor2 = database.rawQuery("SELECT " +
                    " table_use.id AS tuID," +
                    " table_use.total_seat AS tuTS," +
                    " table_use.start_time AS tuST," +
                    " table_use.end_time AS tuET," +
                    " table_use.total_bill_id AS tuTBID, " +
                    " table_use.main_table AS tuMT," +
                    " table_use.table_id AS tuTID," +
                    " table_use.main_table_number AS tuMTN" +
                    " FROM table_use " +
                    " WHERE id=" + id, null);
            if (mCursor2 != null)
            {
                while (mCursor2.moveToNext())
                {
                    Cursor mCursor = database.rawQuery("SELECT table_configuration.id AS tcID," +
                            " table_configuration.table_number AS tcTNUMBER," +
                            " table_configuration.seat_number AS tcSN," +
                            " table_configuration.room_id AS tcRID," +
                            " table_configuration.table_name AS tcTNAME," +
                            " table_configuration.merge_table AS tcMT," +
                            " table_configuration.share_table AS tcST" +
                            " FROM table_configuration " +
                            "WHERE table_configuration.table_number=" + mCursor2.getInt(mCursor2.getColumnIndex("tuTID")), null);


                    if (mCursor != null)
                    {
                        while (mCursor.moveToNext())
                        {
                            c.setTableId(mCursor.getInt(mCursor.getColumnIndex("tcID")));
                            c.setMergeTable(mCursor.getInt(mCursor.getColumnIndex("tcMT")));
                            c.setShareTable(mCursor.getInt(mCursor.getColumnIndex("tcST")));
                            c.setTableName(mCursor.getString(mCursor.getColumnIndex("tcTNAME")));
                            c.setTableNumber(mCursor.getInt(mCursor.getColumnIndex("tcTNUMBER")));
                            c.setTableSeat(mCursor.getInt(mCursor.getColumnIndex("tcSN")));
                            c.setRoomId(mCursor.getInt(mCursor.getColumnIndex("tcRID")));
                        }
                    }
                    mCursor.close();


                    c.setId(mCursor2.getInt(mCursor2.getColumnIndex("tuID")));
                    c.setBillId(mCursor2.getInt(mCursor2.getColumnIndex("tuTBID")));
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startString = mCursor2.getString(mCursor2.getColumnIndex("tuST"));
                    if (startString != null)
                    {
                        Date startDate = format.parse(startString);
                        c.setStartTime(startDate);
                    }
                    String endString = mCursor2.getString(mCursor2.getColumnIndex("tuET"));
                    if (endString != null)
                    {
                        Date endDate = format.parse(endString);
                        c.setEndTime(endDate);
                    }
                    c.setTotalSeats(mCursor2.getInt(mCursor2.getColumnIndex("tuTS")));
                    c.setMainTable(mCursor2.getInt(mCursor2.getColumnIndex("tuMT")));
                    c.setMainTableNumber(mCursor2.getInt(mCursor2.getColumnIndex("tuMTN")));
                }
                mCursor2.close();
            }


            database.close();
            return c;
        }
        catch (Exception e)
        {
            Log.e("UPDATE table USE", e.getMessage());
            TableUse fake = new TableUse();
            return fake;
        }
    }


    //method to check if a table is in table_use, and it's not the main table
    public boolean checkIfTableIsInUseInMerging(int table_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getReadableDatabase(); }

            boolean value = false;
            Cursor c = database.rawQuery("SELECT * FROM table_use WHERE table_id=" + table_id +
                    " AND main_table=" + 0 + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                    { value = true; }
            }

            c.close();
            database.close();

            return value;
        }

        catch (Exception e)
        {
            Log.e("Check failure", e.getMessage());
            return false;
        }
    }


    //it deletes an entry in table_use
    public void deleteFromTableUse(int table_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("DELETE FROM table_use WHERE table_id=" + table_id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Delete error", e.getMessage());
        }
    }


    //method to retrieve tableUseId from table number
    public long getTableUseIdFromTableNumber(int tNumber)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            long id = -1;
            Cursor c = database.rawQuery("SELECT * FROM table_use WHERE main_table=1" +
                    " AND table_use.table_id IN " +
                    "(SELECT table_configuration.id " +
                    "FROM table_configuration " +
                    "WHERE table_configuration.table_number=" + tNumber + ");", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    id = c.getLong(c.getColumnIndex("id"));
                }
            }
            c.close();
            database.close();

            return id;
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
            return -1;
        }
    }


    public long getTableUseIdFromSecondaryTableNumber(int tNumber)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            long id = -1;
            Cursor c = database.rawQuery("SELECT * FROM table_use " +
                    "WHERE table_id=" + tNumber + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    int main_table_number = c.getInt(c.getColumnIndex("main_table_number"));
                    Cursor c2 = database.rawQuery("SELECT * FROM table_use " +
                            "WHERE main_table_number=" + main_table_number + " AND main_table=" + 1 + ";", null);
                    if (c2 != null)
                    {
                        while (c2.moveToNext())
                        { id = c2.getLong(c2.getColumnIndex("id")); }
                    }
                    c2.close();
                }
            }
            c.close();
            database.close();

            return id;
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
            return -1;
        }
    }


    //takes tables from table_configuration, checks size and usage (it must not be already in use)
    public int getTableUse(int seats)
    {
        int id = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM table_configuration WHERE seat_number >= " + seats +
                    " AND table_configuration.id NOT IN " +
                    "(SELECT table_id FROM table_use) " +
                    "ORDER BY merge_table, share_table, id", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    id = c.getInt(c.getColumnIndex("id"));
                }
            }
            c.close();
            database.close();

            return id;
        }
        catch (Exception e)
        {
            Log.e("failure", e.getMessage());
            return id;
        }
    }


    //if getTableUse doesn't work, we must merge some tables
    public int getMergeTableUse(int seats)
    {
        int id = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM table_configuration WHERE table_configuration.id NOT IN" +
                    " (SELECT table_id FROM table_use) ORDER BY merge_table, share_table, table_number;", null);
            if (c != null)
            {
                boolean done = false;
                int sum = 0;
                ArrayList<Integer> array = new ArrayList<>();
                while (c.moveToNext())
                {
                    if (sum >= seats)
                    { done = true; }
                    else
                    {
                        array.add(c.getInt(c.getColumnIndex("id")));
                        sum += c.getInt(c.getColumnIndex("seat_number"));
                    }
                }
                if (!done)
                {
                    return id;
                }
                else
                {
                    id = array.get(0);
                    for (int j = 0; j < array.size(); j++)
                    {
                        execOnDb("INSERT INTO table_use (table_id, total_seat, main_table," +
                                "main_table_number) VALUES (" + array.get(j) + ", " + seats + ", " + (j == 0 ? 1 : 0) +
                                ", " + id + ");");
                    }
                }
            }
            c.close();
            database.close();

            return id;
        }
        catch (Exception e)
        {
            Log.e("Insert failure", e.getMessage());
            return id;
        }
    }



    public void insertTableSync(ArrayList<Table> tables)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (Table t : tables)
            {
                database.execSQL("INSERT INTO table_configuration(id,table_number, seat_number, room_id,table_name, merge_table, share_table)" +
                        " VALUES(" + t.getId() + "," + t.getTableNumber() + "," + t.getPeopleNumber() + "," + t
                        .getRoomId() + ",'" + t.getTableName() + "'," + t.getMergeTable() + "," + t.getShareTable() + ")");

            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert mA Error", e.getMessage());
        }
    }


    public void insertTableConfigurationFromServer(Table table)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            {
                database = dbHelper.getWritableDatabase(); //int f = (admin?1:0);
            }
            database.execSQL("INSERT INTO table_configuration(id, table_number, seat_number, room_id,table_name, merge_table, share_table) VALUES(" + table
                    .getId() + ", " + table.getTableNumber() + "," + table.getPeopleNumber() + "," + table
                    .getRoomId() + ",'" + table.getTableName() + "'," + table.getMergeTable() + "," + table
                    .getShareTable() + ")");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert Device Error", e.getMessage());
        }
    }


    /**
     * changes payment value
     *
     * @param billId
     */
    public void modifyTablePaymentInBill(int billId, int payment)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("UPDATE bill_total SET paid=" + payment +
                    " WHERE id=" + billId + ";");
            if (getTableNumberId(billId) != -1)
            {
                execOnDb("UPDATE table_use SET end_time=datetime(CURRENT_TIMESTAMP, 'localtime')");
                execOnDb("UPDATE bill_total SET pay_time=datetime(CURRENT_TIMESTAMP, 'localtime')");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
        }
    }






    // =============================================== //
    // [ ROOMS ]
    // =============================================== //

    public int getRoomId(int bill_number)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT * FROM table_use WHERE " +
                    "total_bill_id =" + bill_number + ";", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    int tableId = mCursor.getInt(mCursor.getColumnIndex("table_id"));
                    //id = mCursor.getInt(1);//to get id, 0 is the column index
                    Cursor mCursor1 = database.rawQuery("SELECT * FROM table_configuration WHERE " +
                            "id =" + tableId + ";", null);
                    if (mCursor1 != null)
                    {
                        if (mCursor1.moveToFirst())
                        {
                            //name = cursor.getString(column_index);//to get other values
                            id = mCursor1.getInt(mCursor1.getColumnIndex("room_id"));
                            //id = mCursor.getInt(1);//to get id, 0 is the column index
                            //database.close();
                        }
                        mCursor1.close();
                    }


                }
                mCursor.close();
            }
            database.close();
            return id;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public int getRoomIdAgain(int billId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor1 = database.rawQuery("SELECT * FROM table_use WHERE " +
                    "total_bill_id=" + billId + ";", null);
            if (mCursor1 != null)
            {
                if (mCursor1.moveToFirst())
                {

                    Cursor mCursor = database.rawQuery("SELECT * FROM table_configuration WHERE " +
                            "id =" + mCursor1.getInt(mCursor1.getColumnIndex("table_id")) + ";", null);
                    if (mCursor != null)
                    {
                        if (mCursor.moveToFirst())
                        {
                            //name = cursor.getString(column_index);//to get other values
                            id = mCursor.getInt(mCursor.getColumnIndex("room_id"));
                            //id = mCursor.getInt(1);//to get id, 0 is the column index

                        }
                        mCursor.close();
                    }
                }
            }
            mCursor1.close();
            database.close();
            return id;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return -1;
        }
    }


    public ArrayList<Room> fetchRooms()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM room;", null);
            ArrayList<Room> array = new ArrayList<>();
            //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    Room c = new Room();
                    c.setId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    //TODO add time
                    array.add(c);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            Log.e("fetchFailure", e.getMessage());

            ArrayList<Room> newarray = new ArrayList<Room>();
            return newarray;

        }
    }


    public Room fetchRoomById(int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM room WHERE id=" + id, null);
            Room c = new Room();
            //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {

                    c.setId(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    //TODO add time

                }
                mCursor.close();

            }
            database.close();
            return c;
        }
        catch (Exception e)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            Log.e("fetchFailure", e.getMessage());

            Room newarray = new Room();
            return newarray;

        }
    }


    public void insertRoomSync(ArrayList<Room> rooms)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (Room r : rooms)
            {
                database.execSQL("INSERT INTO room (id, name) VALUES(" + r.getId() + ",'" + r.getName() + "');");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert mA Error", e.getMessage());
        }
    }



    public void insertRoomFromServer(Room room)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("INSERT INTO room (id,name) VALUES(" + room.getId() + " , '" + room.getName() + "');");

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert modifiers Error", e.getMessage());
        }
    }







    // =============================================== //
    // [ RESERVATIONS ]
    // =============================================== //


    public void insertReservationSync(ArrayList<Reservation> reservations)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            for (Reservation res : reservations)
            {
                execOnDb(String.format(
                    "INSERT INTO reservation (id, name, adults, children, disabled, time, telephone) " +
                    "VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                    res.getReservation_id(), res.getName(), res.getAdults(), res.getChildren(), res.getDisabled(), res.getTime(), res.getTelephone()
                ));
            }

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert mA Error", e.getMessage());
        }
    }




    /**
     * DATE FORMAT: yyyy-mm-dd
     * HOUR FORMAT: HH:MM
     *
     * @param res
     */
    public void addReservation(Reservation res, Date today)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            showData("reservation");
            
            execOnDb(String.format(
                "INSERT INTO reservation (id, name, adults, children, disabled, time, telephone) VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                res.getReservation_id(), res.getName(), res.getAdults(), res.getChildren(), res.getDisabled(), res.getTime(), res.getTelephone()
            ));

            showData("reservation");

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Add failure", e.getMessage());
        }
    }


    //returns only today reservations not started
    public ArrayList<Reservation> getReservationList()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }


            ArrayList<Reservation> myReservations = new ArrayList<>();
            Cursor curs = database.rawQuery("SELECT * FROM reservation ORDER BY time;", null);

            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzzzzzzz yyyy", Locale.ROOT);

            while (curs.moveToNext())
            {
                Reservation r = new Reservation();

                r.setReservation_id(curs.getInt(curs.getColumnIndex("id")));
                r.setName(curs.getString(curs.getColumnIndex("name")));
                r.setAdults(curs.getInt(curs.getColumnIndex("adults")));
                r.setChildren(curs.getInt(curs.getColumnIndex("children")));
                r.setDisabled(curs.getInt(curs.getColumnIndex("disabled")));
                r.setTime(format.parse(curs.getString(curs.getColumnIndex("time"))));
                r.setTelephone(curs.getString(curs.getColumnIndex("telephone")));

                myReservations.add(r);
            }

            curs.close();
            database.close();

            return myReservations;
        }

        catch (Exception e)
        {
            Log.e("Fetch failure", e.getMessage());
            return null;
        }
    }


    //it returns reservations from id array (for popup)
    public ArrayList<Reservation> getReservationsFromArray(ArrayList<Integer> array)
    {
        ArrayList<Reservation> reservations = new ArrayList<>();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getReadableDatabase(); }

            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzzzzzzz yyyy", Locale.ROOT);


            for (int i : array)
            {
                Cursor c = database.rawQuery("SELECT * FROM reservation WHERE id=" + i + ";", null);
                if (c != null)
                {
                    while (c.moveToNext())
                    {
                        Reservation r = new Reservation();
                        r.setReservation_id(c.getInt(c.getColumnIndex("id")));
                        r.setName(c.getString(c.getColumnIndex("name")));
                        r.setAdults(c.getInt(c.getColumnIndex("adults")));
                        r.setChildren(c.getInt(c.getColumnIndex("children")));
                        r.setDisabled(c.getInt(c.getColumnIndex("disabled")));
                        r.setTime(format.parse(c.getString(c.getColumnIndex("time"))));
                        r.setTelephone(c.getString(c.getColumnIndex("telephone")));
                        reservations.add(r);
                    }
                }
                c.close();
            }
            database.close();

            return reservations;
        }
        catch (Exception e)
        {
            Log.e("Fetch failure", e.getMessage());
            return reservations;
        }
    }


    //fetch a single reservation from id
    public Reservation getReservation(int id)
    {
        Reservation r = new Reservation();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM reservation WHERE id=" + id + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    r.setReservation_id(c.getInt(c.getColumnIndex("id")));
                    r.setName(c.getString(c.getColumnIndex("name")));
                    r.setAdults(c.getInt(c.getColumnIndex("adult")));
                    r.setChildren(c.getInt(c.getColumnIndex("children")));
                    r.setDisabled(c.getInt(c.getColumnIndex("disabled")));
                    r.setTime(c.getString(c.getColumnIndex("time")));
                    r.setTelephone(c.getString(c.getColumnIndex("telephone")));
                }
            }
            c.close();
            database.close();

            return r;
        }
        catch (Exception e)
        {
            Log.e("Fetch failure", e.getMessage());
            return r;
        }
    }


    //it deletes a "red" or "grey" reservation
    public void deleteReservation(int res_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("DELETE FROM reservation WHERE id=" + res_id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Delete Error", e.getMessage());
        }
    }


    //it modifies existing reservation (not start_time)
    public void modifyReservation(Reservation res)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            execOnDb(String.format(
                    "UPDATE reservation " +
                    "SET name = '%s', adults = '%s', children = '%s', disabled = '%s', time = '%s', telephone = '%s'" +
                    "WHERE id = '%s';",
                    res.getName(),  res.getAdults(), res.getChildren(), res.getDisabled(), res.getTime(), res.getTelephone(), res.getReservation_id()
            ));

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Delete Error", e.getMessage());
        }
    }


    public int getLatestReservationId()
    {
        int id = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM reservation ORDER BY id DESC", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    id = c.getInt(c.getColumnIndex("id"));
                }
            }
            c.close();
            database.close();

            return id;
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
            return id;
        }
    }


    //it returns an array of res_id of reservations of that day and one hour earlier, with no table assigned
    public ArrayList<Integer> checkReservationTime(String date, String time)
    {
        ArrayList<Integer> resIdArray = new ArrayList<>();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getReadableDatabase(); }

            Cursor c = database.rawQuery(String.format(
                    "SELECT * FROM reservation WHERE (strftime('%H', '%1$s') - strftime('%H', '%1$s')) <= 1 ORDER BY time;",
                    time
            ), null);

            if (c != null)
            {
                while (c.moveToNext())
                {
                    int id = c.getInt(c.getColumnIndex("id"));
                    resIdArray.add(id);
                }
            }
            c.close();
            database.close();

            return resIdArray;
        }
        catch (Exception e)
        {
            Log.e("Check Time Failure", e.getMessage());
            return resIdArray;
        }
    }


    public void deleteOldReservations(Date now)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("UPDATE reservation SET status=" + 3 + " WHERE (strftime('%Y-%m-%d', '" + now + "') - " +
                    "strftime('%Y-%m-%d', reservation.date))>=1;");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Delete failure", e.getMessage());
        }
    }


    //search through all reservations in database, just with status = 0
    public ArrayList<Reservation> searchReservations(String key)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            String regexp = ".*" + key.toUpperCase() + ".*";

            ArrayList<Reservation> array = new ArrayList<>();
            Cursor c = database.rawQuery(
                    String.format("SELECT * FROM reservation WHERE (UPPER(name) REGEXP '%1$s' OR UPPER(surname) REGEXP '%1$s') ORDER BY time;", regexp),
                    null);

            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzzzzzzz yyyy", Locale.ROOT);

            if (c != null)
            {
                while (c.moveToNext())
                {
                    Reservation r = new Reservation();
                    r.setReservation_id(c.getInt(c.getColumnIndex("id")));
                    r.setName(c.getString(c.getColumnIndex("name")));
                    r.setAdults(c.getInt(c.getColumnIndex("adults")));
                    r.setChildren(c.getInt(c.getColumnIndex("children")));
                    r.setDisabled(c.getInt(c.getColumnIndex("disabled")));
                    r.setTime(format.parse(c.getString(c.getColumnIndex("time"))));
                    r.setTelephone(c.getString(c.getColumnIndex("telephone")));
                    array.add(r);
                }
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






    public void insertWaitingListSync(ArrayList<WaitingListModel> wls)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            for (WaitingListModel res : wls)
            {
                execOnDb(String.format(
                        "INSERT INTO waiting_list (id, name, adults, children, disabled, time) " +
                                "VALUES('%s', '%s', '%s', '%s', '%s', '%s');",
                        res.getId(), res.getName(), res.getAdults(), res.getChildren(), res.getDisabled(), res.getTime()
                ));
            }

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert mA Error", e.getMessage());
        }
    }




    public ArrayList<WaitingListModel> fetchWaitingList()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getReadableDatabase(); }

            ArrayList<WaitingListModel> waitingList = new ArrayList<>();
            Cursor c = database.rawQuery("SELECT * FROM waiting_list ORDER BY time", null);

            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzzzzzzz yyyy", Locale.ROOT);

            if (c != null)
            {
                while (c.moveToNext())
                {
                    WaitingListModel r = new WaitingListModel();
                    r.setId(c.getInt(c.getColumnIndex("id")));
                    r.setName(c.getString(c.getColumnIndex("name")));
                    r.setAdults(c.getInt(c.getColumnIndex("adults")));
                    r.setChildren(c.getInt(c.getColumnIndex("children")));
                    r.setDisabled(c.getInt(c.getColumnIndex("disabled")));
                    r.setTime(format.parse(c.getString(c.getColumnIndex("time"))));

                    waitingList.add(r);
                }
            }
            c.close();
            database.close();

            return waitingList;
        }
        catch (Exception e)
        {
            Log.e("Fetch failure", e.getMessage());
            return null;
        }
    }


    public int getLatestWaitingListId()
    {
        int id = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM waiting_list ORDER BY id DESC", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    id = c.getInt(c.getColumnIndex("id"));
                }
            }
            c.close();
            database.close();

            return id;
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
            return id;
        }
    }


    public void addWaitingListElement(WaitingListModel wlm)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            //table check, -1 if table is not available
            //just for today reservations
            execOnDb(String.format(
                    "INSERT INTO waiting_list (name, adults, children, disabled, time) VALUES('%s', '%s', '%s', '%s', '%s');",
                    wlm.getName(), wlm.getAdults(), wlm.getChildren(), wlm.getDisabled(), wlm.getTime()
            ));
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Add failure", e.getMessage());
        }
    }



    //search through all reservations in database, just with status = 0
    public ArrayList<WaitingListModel> searchWaitingListElement(String key)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            String regexp = ".*" + key.toUpperCase() + ".*";

            ArrayList<WaitingListModel> array = new ArrayList<>();

            Cursor c = database.rawQuery(
                    String.format(
                        "SELECT * FROM waiting_list WHERE (UPPER(name) REGEXP '%1$s' OR UPPER(surname) REGEXP '%1$s') ORDER BY time;",
                        regexp), null);

            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzzzzzzz yyyy", Locale.ROOT);


            if (c != null)
            {
                while (c.moveToNext())
                {
                    WaitingListModel r = new WaitingListModel();
                    r.setId(c.getInt(c.getColumnIndex("id")));
                    r.setName(c.getString(c.getColumnIndex("name")));
                    r.setAdults(c.getInt(c.getColumnIndex("adults")));
                    r.setChildren(c.getInt(c.getColumnIndex("children")));
                    r.setDisabled(c.getInt(c.getColumnIndex("disabled")));
                    r.setTime(format.parse(c.getString(c.getColumnIndex("time"))));
                    array.add(r);
                }
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


    public void deleteWaitingListElement(int id)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("DELETE FROM waiting_list WHERE id=" + id + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("DELETING ERROR", e.getMessage());
        }
    }





    public void setReservationPopupTimer(int timer)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO general_settings (timer) VALUES(" + timer + ");");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("ERROR", e.getMessage());
        }
    }


    public int getReservationPopupTimer()
    {
        int timer = 1;
        try
        {
            if (!database.isOpen())
                { database = dbHelper.getReadableDatabase(); }

            Cursor c = database.rawQuery("SELECT * FROM general_settings;", null);
            if (c != null)
            {
                while (c.moveToNext())
                    { timer = c.getInt(c.getColumnIndex("reservation_timer")); }
            }

            c.close();
            database.close();

            return timer;
        }
        catch (Exception e)
        {
            Log.e("ERROR", e.getMessage());
            return timer;
        }
    }


}
