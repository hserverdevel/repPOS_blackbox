package com.example.blackbox.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.adapter.RoomAdapter;
import com.example.blackbox.adapter.TableAdapter;
import com.example.blackbox.adapter.TableUseAdapter;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.Reservation;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.Table;
import com.example.blackbox.model.TableUse;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.revicer.PinpadBroadcastReciver;
import com.example.blackbox.server.HttpHandler;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by tiziano on 8/29/17.
 */

public class TableActivity extends AppCompatActivity implements
        RoomAdapter.AdapterRoomCallback,
        TableUseAdapter.AdapterTableUseCallback,
        HttpHandler.AsyncResponse
{

    private static final String TAG = "<TableActivity>";

    public TableActivity() {}

    private GridLayoutManager grid_manager;
    private DatabaseAdapter dbA;
    private int tableNumber = -1;
    private int roomId = -1;
    private int billId = -1;

    public int getRoomId() {return roomId;}

    public void setRoomId(int roomId) {this.roomId = roomId;}

    public void setTableNumber(int number)
    {
        tableNumber = number;
    }

    private int getTableNumber()
    {
        return tableNumber;
    }

    private RecyclerView roomRecycler;
    private RoomAdapter roomAdapter;

    private RecyclerView tableRecycler;
    private TableAdapter tableAdapter;
    private TableUseAdapter tableUseAdapter;
    private ArrayList<Room> rooms = new ArrayList<Room>();
    private Intent intent;
    private Intent intentPasscode;

    private Reservation currentReservation;
    private String username;
    private int isAdmin;

    private HttpHandler httpHandler;

    @Override
    public void processFinish(String output)
    {
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        // Each possible response is handled with a swtich case
        JSONObject jsonObject = new JSONObject();
        // the response route
        String route = "";
        // a bool indicating if the connection was succesful
        boolean success = false;

        try
        {
            jsonObject = new JSONObject(output);
            route = jsonObject.getString("route");
            success = jsonObject.getBoolean("success");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if (success)
        {
            try
            {
                jsonObject = new JSONObject(output);
                boolean check = false;

                switch (route)
                {
                    case "insertRoom":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("room");
                            Room room = Room.fromJson(jObject);
                            dbA.insertRoomFromServer(room);
                            roomAdapter.closePopupWindow();
                            roomAdapter.notifyDataSetChanged();
                            rooms = dbA.fetchRooms();
                            showLastRoom(rooms.size() - 1);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "deleteRoom":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int roomId = jsonObject.getInt("id");
                            dbA.execOnDb("DELETE FROM room WHERE id=" + roomId);
                            dbA.execOnDb("DELETE FROM table_configuration WHERE room_id=" + roomId + ";");
                            roomAdapter.closePopupWindow();
                            restartRoom();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "updateRoom":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONObject jObject = new JSONObject(output).getJSONObject("room");
                            Room room = Room.fromJson(jObject);
                            dbA.execOnDb("UPDATE room SET name= '" + room.getName() + "' WHERE id = " + room
                                    .getId() + "");
                            roomAdapter.closePopupWindow();
                            showLastRoom(roomAdapter.getRoomePosition());
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "insertTable":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONArray usersObject = new JSONObject(output).getJSONArray("tables");
                            ArrayList<Table> tables = Table.fromJsonArray(usersObject);
                            tableAdapter.addTableFromServer(tables);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "insertAndUpdateTable":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONArray usersObject = new JSONObject(output).getJSONArray("tables");
                            ArrayList<Table> tables = Table.fromJsonArray(usersObject);
                            JSONObject jObject = new JSONObject(output).getJSONObject("table");
                            Table table = Table.fromJson(jObject);
                            dbA.updateTable(table.getTableNumber(), table.getPeopleNumber(), table.getRoomId(), table
                                    .getTableName(), table.getMergeTable(), table.getShareTable(), table
                                    .getId());
                            tableAdapter.addTableFromServer(tables);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "deleteTable":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            int roomId = jsonObject.getInt("roomId");
                            int tableNumber = jsonObject.getInt("tableNumber");
                            dbA.updateTableConfiguration(roomId, tableNumber);
                            tableAdapter.functionAddTableFromServer();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "getTableUse":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONArray usersObject = new JSONObject(output).getJSONArray("tableUse");
                            ArrayList<TableUse> tables = TableUse.fromJsonArray(usersObject);
                            int rId = jsonObject.getInt("roomId");
                            roomId = rId;
                            int tableNumber = jsonObject.getInt("tableNumber");
                            int billId = jsonObject.getInt("billId");
                            CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                            if (rooms.size() > 0)
                            {
                                if (tableNumber == -1)
                                {
                                    roomName.setText(rooms.get(0).getName());
                                    tableUseAdapter = new TableUseAdapter(this, dbA, tables, billId);
                                    tableRecycler.setAdapter(tableUseAdapter);
                                    tableUseAdapter.setRoomId(rooms.get(0).getId());

                                }
                                else
                                {
                                    Room setRoom = dbA.fetchRoomById(roomId);
                                    roomName.setText(setRoom.getName());
                                    tableUseAdapter = new TableUseAdapter(this, dbA, tables, billId);
                                    tableRecycler.setAdapter(tableUseAdapter);
                                    tableUseAdapter.setRoomId(roomId);
                                    roomAdapter.setRooms(rooms, roomId);
                                    tableUseAdapter.setTableNumber(getTableNumber());
                                }
                            }
                            else
                            {
                                roomName.setText("");
                                tables = new ArrayList<>();
                                tableUseAdapter = new TableUseAdapter(this, dbA, tables, billId);
                                tableRecycler.setAdapter(tableUseAdapter);
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "insertTableUse":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            billId = jsonObject.getInt("billId");

                            JSONArray usersObject = new JSONObject(output).getJSONArray("tables");
                            ArrayList<TableUse> mytables = TableUse.fromJsonArray(usersObject);

                            JSONObject jObject = new JSONObject(output).getJSONObject("tableUse");
                            TableUse tableUse = TableUse.fromJson(jObject);

                            int type = jsonObject.getInt("type");

                            if (type == 1)
                            {
                                setTableNumber(tableUse.getTableNumber());
                                setRoomId(tableUse.getRoomId());

                                tableUseAdapter.setTableNumber(tableUse.getTableNumber());
                                tableUseAdapter.setRoomId(tableUse.getRoomId());
                                tableUseAdapter.tables = mytables;
                                tableUseAdapter.notifyDataSetChanged();
                            }

                            else
                            {
                                if (type == 2)
                                {
                                    tableUseAdapter.setIsMerge(true);
                                    tableUseAdapter.setTableNumber(tableUse.getTableNumber());
                                    tableUseAdapter.setRoomId(tableUse.getRoomId());
                                    tableUseAdapter.setMainMergeId(tableUse.getId());
                                    setIsMergeActivated(tableUse);
                                    tableUseAdapter.tables = mytables;
                                    tableUseAdapter.notifyDataSetChanged();
                                    tableUseAdapter.myPopupWindow.dismiss();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), R.string.this_table_cannot_be_merged, Toast.LENGTH_SHORT)
                                         .show();
                                    tableUseAdapter.openNoMergableTable();
                                }

                            }
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "insertTableUseMerge":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            billId = jsonObject.getInt("billId");

                            int type = jsonObject.getInt("type");
                            if (type == 1)
                            {
                                JSONArray usersObject = new JSONObject(output).getJSONArray("tables");
                                ArrayList<TableUse> mytables = TableUse.fromJsonArray(usersObject);
                                tableUseAdapter.setMyButton();
                                tableUseAdapter.tables = mytables;
                                tableUseAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                tableUseAdapter.openNoMergableTable();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT)
                                 .show();
                        }
                        break;


                    case "deleteTableUse":
                        check = jsonObject.getBoolean("check");
                        if (check)
                        {
                            JSONArray usersObject = new JSONObject(output).getJSONArray("tables");
                            ArrayList<TableUse> mytables = TableUse.fromJsonArray(usersObject);

                            int type = jsonObject.getInt("type");
                            if (type == 1)
                            {
                                tableUseAdapter.setIsMerge(false);
                                tableUseAdapter.setTableNumber(-11);
                                tableUseAdapter.setRoomId(-11);
                                tableUseAdapter.tables = mytables;
                                tableUseAdapter.notifyDataSetChanged();

                                CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                                Room room = dbA.fetchRoomById(roomId);
                                roomName.setText(room.getName());
                            }

                            else if (type == 2)
                            {
                                tableUseAdapter.setTableNumber(-11);
                                tableUseAdapter.setRoomId(-11);
                                tableUseAdapter.tables = mytables;
                                tableUseAdapter.notifyDataSetChanged();
                            }
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    default:
                        break;
                }
            }

            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        else
        {
            Toast.makeText(this,
                    getString(R.string.error_blackbox_comm, StaticValue.blackboxInfo.getName(), StaticValue.blackboxInfo
                            .getAddress()),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void callHttpHandler(String route, List<NameValuePair> params)
    {
        httpHandler = new HttpHandler();
        httpHandler.delegate = this;
        httpHandler.UpdateInfoAsyncTask(route, params);
        httpHandler.execute();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_table);

        dbA = new DatabaseAdapter(this);
        intent = this.getIntent();
        username = intent.getStringExtra("username");
        isAdmin = intent.getIntExtra("isAdmin", -1);

        this.rooms = dbA.fetchRooms();
        int tNumber = intent.getIntExtra("tableNumber", -1);
        if (tNumber != -1) { setTableNumber(intent.getIntExtra("tableNumber", -1)); }

        /* LAYOUT GRID FOR ROOMS*/
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        //THIS MAKE LYNEAR LAYOUT MANAGER TO START FROM RIGHT AND NOT FROM LEFT
        layoutManager.setStackFromEnd(true);
        roomRecycler = (RecyclerView) findViewById(R.id.room_grid);
        roomRecycler.setLayoutManager(layoutManager);

        /* LAYOUT FOR TABLES*/
        tableRecycler = (RecyclerView) findViewById(R.id.table_grid);
        grid_manager = new GridLayoutManager(this, 6);
        grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {
            @Override
            public int getSpanSize(int position)
            {
                return 1;
            }
        });
        tableRecycler.setLayoutManager(grid_manager);

        DividerItemDecoration divider = new
                DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.divider_black_for_table_v));
        tableRecycler.addItemDecoration(divider);

        DividerItemDecoration dividerH = new
                DividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL);
        dividerH.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.divider_black_for_table_h));
        tableRecycler.addItemDecoration(dividerH);

        if (intent.getAction().equals("configuration"))
        {
            //configure
            //SET ROOMS
            setSwipeForRoom();
            //if room isn't empty set tables adapter using first room
            if (rooms.size() > 0)
            {
                Room addRoom = new Room();
                addRoom.setId(-15);
                rooms.add(addRoom);

                roomAdapter = new RoomAdapter(this, dbA, rooms, false, rooms.get(0).getId());
                roomRecycler.setAdapter(roomAdapter);
                rooms.get(0).getName();

                CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                roomName.setText(rooms.get(0).getName());

                //SET TABLES, array is empty because is fill when you click on room
                ArrayList<Table> tables = new ArrayList<Table>();
                tableAdapter = new TableAdapter(this, dbA, tables);
                tableRecycler.setAdapter(tableAdapter);

                openTableView(rooms.get(0).getId());
            }

            else
            {
                //room is empty, table adapter is not set
                Room addRoom = new Room();
                addRoom.setId(-15);
                rooms.add(addRoom);
                roomAdapter = new RoomAdapter(this, dbA, rooms, false, rooms.get(0).getId());
                roomRecycler.setAdapter(roomAdapter);

                CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                roomName.setVisibility(View.GONE);
                RelativeLayout addRoomButton = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                addRoomButton.setVisibility(View.VISIBLE);

                ArrayList<Table> tables = new ArrayList<Table>();
                tableAdapter = new TableAdapter(this, dbA, tables);
                tableRecycler.setAdapter(tableAdapter);
            }

            //set ok and x button
            findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //get back to
                    Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                    int billId = intent.getIntExtra("billId", -1);
                    int orderNumber = intent.getIntExtra("orderNumber", 1);
                    String username = intent.getStringExtra("username");
                    int isAdmin = intent.getIntExtra("isAdmin", -1);

                    if (billId != -1)
                    {
                        int tnumb = dbA.getTableNumberId(billId);
                        setTableNumber(tnumb);
                        int roomId = dbA.getRoomId(billId);
                        setRoomId(roomId);
                    }

                    newIntent.putExtra("tableNumber", getTableNumber());
                    newIntent.putExtra("roomId", getRoomId());
                    newIntent.putExtra("username", username);
                    newIntent.putExtra("isAdmin", isAdmin);
                    newIntent.setAction("setTable");
                    newIntent.putExtra("billId", billId);
                    newIntent.putExtra("orderNumber", orderNumber);
                    startActivity(newIntent);
                    finish();
                }
            });

            findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                    int billId = intent.getIntExtra("billId", -1);
                    int orderNumber = intent.getIntExtra("orderNumber", 1);
                    String username = intent.getStringExtra("username");
                    int isAdmin = intent.getIntExtra("isAdmin", -1);
                    if (billId != -1)
                    {
                        int tnumb = dbA.getTableNumberId(billId);
                        setTableNumber(tnumb);
                        int roomId = dbA.getRoomId(billId);
                        setRoomId(roomId);
                    }

                    newIntent.putExtra("username", username);
                    newIntent.putExtra("isAdmin", isAdmin);
                    newIntent.setAction("setTable");
                    newIntent.putExtra("billId", billId);
                    newIntent.putExtra("orderNumber", orderNumber);
                    int a = getTableNumber();
                    newIntent.putExtra("tableNumber", getTableNumber());
                    newIntent.putExtra("roomId", getRoomId());
                    startActivity(newIntent);
                    finish();
                }
            });

        }

        else
        {
            //operative
            tableNumber = intent.getIntExtra("tableNumber", -1);
            roomId = intent.getIntExtra("roomId", -1);
            int id = intent.getIntExtra("reservation", -1);
            if (id != -1)
            { currentReservation = dbA.getReservation(id); }
            if (rooms.size() > 0)
            { roomAdapter = new RoomAdapter(this, dbA, rooms, false, rooms.get(0).getId()); }
            else
            { roomAdapter = new RoomAdapter(this, dbA, rooms, false, -15); }

            roomRecycler.setAdapter(roomAdapter);

            CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
            roomName.setVisibility(View.VISIBLE);
            RelativeLayout addRoomButton = (RelativeLayout) findViewById(R.id.room_plus_edittext);
            addRoomButton.setVisibility(View.GONE);

            if (StaticValue.blackbox)
            {
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                if (tableNumber == -1 || tableNumber == -11)
                {
                    if (rooms.size() > 0)
                    {
                        roomId = rooms.get(0).getId();
                    }
                }
                params.add(new BasicNameValuePair("roomId", String.valueOf(roomId)));
                params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                params.add(new BasicNameValuePair("tableNumber", String.valueOf(tableNumber)));
                callHttpHandler("/getTableUse", params);
            }
            else
            {
                ArrayList<TableUse> tables = new ArrayList<TableUse>();
                if (rooms.size() > 0)
                {
                    if (tableNumber == -1 || tableNumber == -11)
                    {
                        tables = dbA.fetchTableUses(rooms.get(0).getId());
                        roomName.setText(rooms.get(0).getName());
                        tableUseAdapter = new TableUseAdapter(this, dbA, tables, billId);
                        tableRecycler.setAdapter(tableUseAdapter);
                        tableUseAdapter.setRoomId(rooms.get(0).getId());

                    }
                    else
                    {
                        int prova = dbA.getRoomIdAgain(billId);
                        if (roomId == -1) { roomId = dbA.getRoomIdAgain(billId); }
                        if (roomId == -1) { roomId = rooms.get(0).getId(); }
                        Room setRoom = dbA.fetchRoomById(roomId);
                        tables = dbA.fetchTableUses(roomId);
                        roomName.setText(setRoom.getName());
                        tableUseAdapter = new TableUseAdapter(this, dbA, tables, billId);
                        tableRecycler.setAdapter(tableUseAdapter);
                        tableUseAdapter.setRoomId(roomId);
                        roomAdapter.setRooms(rooms, roomId);
                        tableUseAdapter.setTableNumber(getTableNumber());
                    }
                }
                else
                {
                    roomName.setText("");
                    tableUseAdapter = new TableUseAdapter(this, dbA, tables, billId);
                    tableRecycler.setAdapter(tableUseAdapter);
                }
            }

            setSwipeForRoomOperative();

            findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View view)
                {

                    Intent newIntent = new Intent(getApplicationContext(), Operative.class);
                    int billId = intent.getIntExtra("billId", -1);
                    int orderNumber = intent.getIntExtra("orderNumber", 1);
                    String username = intent.getStringExtra("username");
                    int isAdmin = intent.getIntExtra("isAdmin", -1);
                    if (billId != -1)
                    {
                        int tnumb = dbA.getTableNumberId(billId);
                        setTableNumber(tnumb);
                        int roomId = dbA.getRoomId(billId);
                        setRoomId(roomId);
                    }
                    int tableNumber = tableUseAdapter.getTableNumber();
                    if (tableNumber == -11)
                    { newIntent.putExtra("tableNumber", -1); }
                    else
                    { newIntent.putExtra("tableNumber", tableNumber); }
                    int roomId = tableUseAdapter.getRoomId();
                    if (roomId == -11)
                    { newIntent.putExtra("roomId", -1); }
                    else
                    { newIntent.putExtra("roomId", roomId); }

                    newIntent.putExtra("username", username);
                    newIntent.putExtra("isAdmin", isAdmin);
                    newIntent.setAction("setTable");
                    newIntent.putExtra("billId", billId);
                    newIntent.putExtra("orderNumber", orderNumber);
                    startActivity(newIntent);
                    finish();

                }
            });

            findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //go to normal use
                    Intent newIntent = new Intent(getApplicationContext(), Operative.class);

                    int orderNumber = intent.getIntExtra("orderNumber", 1);
                    String username = intent.getStringExtra("username");
                    int isAdmin = intent.getIntExtra("isAdmin", -1);

                    if (billId != -1)
                    {
                        int tnumb = dbA.getTableNumberId(billId);
                        setTableNumber(tnumb);
                    }

                    int tableNumber = tableUseAdapter.getTableNumber();
                    if (currentReservation != null)
                    {
                        //currentReservation.setTable_use_id(tableNumber);
                        dbA.modifyReservation(currentReservation);
                    }

                    if (tableNumber == -11)
                        { newIntent.putExtra("tableNumber", -1); }
                    else
                        { newIntent.putExtra("tableNumber", tableNumber); }


                    int roomId = tableUseAdapter.getRoomId();
                    if (roomId == -11)
                        { newIntent.putExtra("roomId", -1); }
                    else
                        { newIntent.putExtra("roomId", roomId); }


                    newIntent.putExtra("username", username);
                    newIntent.putExtra("isAdmin", isAdmin);
                    newIntent.setAction("setTable");
                    newIntent.putExtra("billId", billId);
                    newIntent.putExtra("orderNumber", orderNumber);
                    startActivity(newIntent);
                    finish();
                }
            });
        }
    }


    /**
     * set swipe for configuration to swipe between rooms
     */
    public void setSwipeForRoom()
    {

        RelativeLayout addRoomButton = (RelativeLayout) findViewById(R.id.room_plus_edittext);
        addRoomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                roomAdapter.openNewRoomPopup();
            }
        });

        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);

        roomName.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                int nextPosition = roomAdapter.nextRoomPosition();
                if (nextPosition != -15)
                {
                    if (nextPosition == rooms.size() - 1)
                    {
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setVisibility(View.GONE);
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.VISIBLE);
                        roomAdapter.setRooms(rooms, -15);
                        ArrayList<Table> tables = new ArrayList<Table>();
                        tableAdapter.setTables(tables, -15);
                        roomAdapter.openNewRoomPopup();
                    }
                    else
                    {
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.GONE);
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableView(rooms.get(nextPosition).getId());
                    }
                }
            }

            public void onSwipeRight()
            {

                int prevPosition = roomAdapter.prevRoomPosition();
                if (prevPosition != -15)
                {
                    RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                    addRoom.setVisibility(View.GONE);
                    roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                    CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                    roomName.setText(rooms.get(prevPosition).getName());
                    roomName.setVisibility(View.VISIBLE);

                    openTableView(rooms.get(prevPosition).getId());
                }

            }

            public void onClick()
            {


                roomAdapter.openModifyRoomPopup();
            }
        });


        findViewById(R.id.room_grid).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                int nextPosition = roomAdapter.nextRoomPosition();
                if (nextPosition != -15)
                {
                    if (nextPosition == rooms.size() - 1)
                    {
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setVisibility(View.GONE);
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.VISIBLE);
                        roomAdapter.setRooms(rooms, -15);
                        ArrayList<Table> tables = new ArrayList<Table>();
                        tableAdapter.setTables(tables, -15);
                    }
                    else
                    {
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.GONE);
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableView(rooms.get(nextPosition).getId());
                    }
                }
            }

            public void onSwipeRight()
            {

                int prevPosition = roomAdapter.prevRoomPosition();
                if (prevPosition != -15)
                {
                    RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                    addRoom.setVisibility(View.GONE);
                    roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                    CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                    roomName.setText(rooms.get(prevPosition).getName());
                    roomName.setVisibility(View.VISIBLE);

                    openTableView(rooms.get(prevPosition).getId());
                }

            }
        });


        findViewById(R.id.room_container_rec).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                int nextPosition = roomAdapter.nextRoomPosition();
                if (nextPosition != -15)
                {
                    if (nextPosition == rooms.size() - 1)
                    {
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setVisibility(View.GONE);
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.VISIBLE);
                        roomAdapter.setRooms(rooms, -15);
                        ArrayList<Table> tables = new ArrayList<Table>();
                        tableAdapter.setTables(tables, -15);
                    }
                    else
                    {
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.GONE);
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableView(rooms.get(nextPosition).getId());
                    }
                }
            }

            public void onSwipeRight()
            {

                int prevPosition = roomAdapter.prevRoomPosition();
                if (prevPosition != -15)
                {
                    RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                    addRoom.setVisibility(View.GONE);
                    roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                    CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                    roomName.setText(rooms.get(prevPosition).getName());
                    roomName.setVisibility(View.VISIBLE);

                    openTableView(rooms.get(prevPosition).getId());
                }

            }
        });

        findViewById(R.id.room_container).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                int nextPosition = roomAdapter.nextRoomPosition();
                if (nextPosition != -15)
                {
                    if (nextPosition == rooms.size() - 1)
                    {
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setVisibility(View.GONE);
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.VISIBLE);
                        roomAdapter.setRooms(rooms, -15);
                        ArrayList<Table> tables = new ArrayList<Table>();
                        tableAdapter.setTables(tables, -15);
                    }
                    else
                    {
                        RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                        addRoom.setVisibility(View.GONE);
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableView(rooms.get(nextPosition).getId());
                    }
                }
            }

            public void onSwipeRight()
            {

                int prevPosition = roomAdapter.prevRoomPosition();
                if (prevPosition != -15)
                {
                    RelativeLayout addRoom = (RelativeLayout) findViewById(R.id.room_plus_edittext);
                    addRoom.setVisibility(View.GONE);
                    roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                    CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                    roomName.setText(rooms.get(prevPosition).getName());
                    roomName.setVisibility(View.VISIBLE);

                    openTableView(rooms.get(prevPosition).getId());
                }

            }
        });
    }


    /**
     * set swipe for operative to swipe between rooms
     */
    public void setSwipeForRoomOperative()
    {


        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);

        roomName.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int nextPosition = roomAdapter.nextRoomPosition();
                    if (nextPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(nextPosition).getId());

                    }
                }
            }

            public void onSwipeRight()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int prevPosition = roomAdapter.prevRoomPosition();
                    if (prevPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(prevPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(prevPosition).getId());
                    }
                }

            }


        });


        findViewById(R.id.room_grid).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int nextPosition = roomAdapter.nextRoomPosition();
                    if (nextPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(nextPosition).getId());

                    }
                }
            }

            public void onSwipeRight()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int prevPosition = roomAdapter.prevRoomPosition();
                    if (prevPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(prevPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(prevPosition).getId());
                    }
                }

            }
        });


        findViewById(R.id.room_container_rec).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int nextPosition = roomAdapter.nextRoomPosition();
                    if (nextPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(nextPosition).getId());

                    }
                }
            }

            public void onSwipeRight()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int prevPosition = roomAdapter.prevRoomPosition();
                    if (prevPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(prevPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(prevPosition).getId());
                    }
                }

            }
        });

        findViewById(R.id.room_container).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext())
        {
            public void onSwipeLeft()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int nextPosition = roomAdapter.nextRoomPosition();
                    if (nextPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(nextPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(nextPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(nextPosition).getId());

                    }
                }
            }

            public void onSwipeRight()
            {

                if (!tableUseAdapter.getIsMergeSet())
                {
                    int prevPosition = roomAdapter.prevRoomPosition();
                    if (prevPosition != -15)
                    {
                        roomAdapter.setRooms(rooms, rooms.get(prevPosition).getId());
                        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
                        roomName.setText(rooms.get(prevPosition).getName());
                        roomName.setVisibility(View.VISIBLE);

                        openTableUseView(rooms.get(prevPosition).getId());
                    }
                }

            }
        });
    }


    public void resetPinpadTimer(int type)
    {
        TimerManager.stopPinpadAlert();
        TimerManager.setContext(getApplicationContext());
        intentPasscode = new Intent(getApplicationContext(), PinpadBroadcastReciver.class);

        intentPasscode.putExtra("isAdmin", isAdmin);
        intentPasscode.putExtra("username", username);

        TimerManager.setIntentPinpad(intentPasscode);
        TimerManager.startPinpadAlert(type);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            resetPinpadTimer(1);
            View v = getCurrentFocus();
            if (v instanceof CustomEditText)
            {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    /**
     * from click on room in roomadapter, show tables for that room
     *
     * @param roomId
     */
    @Override
    public void openTableView(int roomId)
    {
        dbA = new DatabaseAdapter(this);
        dbA.showData("table_configuration");
        dbA.showData("table_use");
        ArrayList<Table> tables = dbA.fetchTables(roomId);
        Table addTable = new Table();
        addTable.setId(-11);
        tables.add(addTable);
        tableAdapter.setTables(tables, roomId);
    }


    /**
     * in operative show table for one rooms
     *
     * @param roomId
     */
    @Override
    public void openTableUseView(int roomId)
    {
        dbA = new DatabaseAdapter(this);
        ArrayList<TableUse> tables = new ArrayList<TableUse>();
        tables = dbA.fetchTableUses(roomId);
        tableUseAdapter.setTables(tables, roomId);
    }


    /**
     * show all rooms again, used when you delete one room
     */
    @Override
    public void restartRoom()
    {
        rooms = dbA.fetchRooms();
        if (rooms.size() > 0)
        {
            CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
            roomName.setVisibility(View.VISIBLE);
            RelativeLayout addRoomButton = (RelativeLayout) findViewById(R.id.room_plus_edittext);
            addRoomButton.setVisibility(View.GONE);
            roomName.setText(rooms.get(0).getName());
            ArrayList<Table> tables = dbA.fetchTables(rooms.get(0).getId());
            Table addTable = new Table();
            addTable.setId(-11);
            tables.add(addTable);
            tableAdapter.setTables(tables, rooms.get(0).getId());

        }
        else
        {
            CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
            roomName.setVisibility(View.GONE);
            RelativeLayout addRoomButton = (RelativeLayout) findViewById(R.id.room_plus_edittext);
            addRoomButton.setVisibility(View.VISIBLE);

            ArrayList<Table> tables = dbA.fetchTables(-15);
            tableAdapter.setTables(tables, -15);
        }
        Room addRoom = new Room();
        addRoom.setId(-15);
        rooms.add(addRoom);
        roomAdapter.setRooms(rooms, rooms.get(0).getId());


    }


    /**
     * show last inserted room
     *
     * @param roomPosition
     */
    @Override
    public void showLastRoom(int roomPosition)
    {
        rooms = dbA.fetchRooms();
        int roomId = rooms.get(roomPosition).getId();
        RelativeLayout addRoomButton = (RelativeLayout) findViewById(R.id.room_plus_edittext);
        CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
        roomName.setText(rooms.get(roomPosition).getName());
        roomName.setVisibility(View.VISIBLE);
        addRoomButton.setVisibility(View.GONE);
        Room addRoom = new Room();
        addRoom.setId(-15);
        rooms.add(addRoom);
        roomAdapter.setRooms(rooms, roomId);
        openTableView(roomId);

    }


    /**
     * adapter table use implementation
     */
    @Override
    public void setIsMergeActivated(TableUse table)
    {
        if (tableUseAdapter.getIsMergeSet())
        {
            //cambio nome
            CustomTextView roomName = (CustomTextView) findViewById(R.id.room_name_edittext);
            roomName.setText(R.string.select_tables_to_merge);
            findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    //open popup to update table use
                    tableUseAdapter.setIsMerge(false);
                    tableUseAdapter.setMainMergeId(-11);
                    Room room = dbA.fetchRoomById(tableUseAdapter.getRoomId());
                    roomName.setText(room.getName());
                    setOkKillButton();
                    tableUseAdapter.openTableUse(table);

                }
            });

            findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    //go to normal use
                    tableUseAdapter.setIsMerge(false);
                    tableUseAdapter.setMainMergeId(-11);
                    Room room = dbA.fetchRoomById(tableUseAdapter.getRoomId());
                    roomName.setText(room.getName());
                    setOkKillButton();

                }
            });

        }
        else
        {
            //rimetto nome stanza
        }
    }


    public void setOkKillButton()
    {
        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent newIntent = new Intent(getApplicationContext(), Operative.class);
                int billId = intent.getIntExtra("billId", -1);
                int orderNumber = intent.getIntExtra("orderNumber", 1);
                String username = intent.getStringExtra("username");
                int isAdmin = intent.getIntExtra("isAdmin", -1);
                if (billId != -1)
                {
                    int tnumb = dbA.getTableNumberId(billId);
                    setTableNumber(tnumb);
                }
                int tableNumber = tableUseAdapter.getTableNumber();
                if (tableNumber == -11) { newIntent.putExtra("tableNumber", -1); }
                else { newIntent.putExtra("tableNumber", tableNumber); }
                int roomId = tableUseAdapter.getRoomId();
                if (roomId == -11) { newIntent.putExtra("roomId", -1); }
                else { newIntent.putExtra("roomId", roomId); }

                newIntent.putExtra("username", username);
                newIntent.putExtra("isAdmin", isAdmin);
                newIntent.setAction("setTable");
                newIntent.putExtra("billId", billId);
                newIntent.putExtra("orderNumber", orderNumber);
                startActivity(newIntent);
                finish();

            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //go to normal use
                Intent newIntent = new Intent(getApplicationContext(), Operative.class);
                int billId = intent.getIntExtra("billId", -1);
                int orderNumber = intent.getIntExtra("orderNumber", 1);
                String username = intent.getStringExtra("username");
                int isAdmin = intent.getIntExtra("isAdmin", -1);

                if (billId != -1)
                {
                    int tnumb = dbA.getTableNumberId(billId);
                    setTableNumber(tnumb);
                }
                int tableNumber = tableUseAdapter.getTableNumber();
                if (tableNumber == -11) { newIntent.putExtra("tableNumber", -1); }
                else { newIntent.putExtra("tableNumber", tableNumber); }
                int roomId = tableUseAdapter.getRoomId();
                if (roomId == -11) { newIntent.putExtra("roomId", -1); }
                else { newIntent.putExtra("roomId", roomId); }

                newIntent.putExtra("username", username);
                newIntent.putExtra("isAdmin", isAdmin);
                newIntent.setAction("setTable");
                newIntent.putExtra("billId", billId);
                newIntent.putExtra("orderNumber", orderNumber);
                startActivity(newIntent);
                finish();
            }
        });
    }


    public void setTableInfoInPopup(Table table, View popupView)
    {
        tableAdapter.setTableValues(table, popupView);
    }


}
