package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.DialogCreator;
import com.example.blackbox.R;
import com.example.blackbox.activities.TableActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.TableUse;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by tiziano on 3/20/18.
 * Used on operative function, if you want to see configurational for table look at TableAdapter
 */

public class TableUseAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperAdapter
{
    private final AdapterTableUseCallback adapterCallback;
    private float density;
    private float dpHeight;
    private float dpWidth;
    private Context context;
    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    public ArrayList<TableUse> tables;
    private boolean keyboard_next_flag = false;
    private Resources resources;

    // used in the modify table use function,
    // to select another table when used
    private boolean MODE_MODIFY_TABLE_USE = false;
    private TableUse tableModifyUse;

    // used to modify the amount of seats used for a table
    private boolean MODE_CHANGE_SEATS_USE = false;

    private int billId;
    private boolean isMergeSet = false;

    public void setIsMerge(Boolean b) {isMergeSet = b;}

    public boolean getIsMergeSet() {return isMergeSet;}

    private long mainMergeId = -11;

    public void setMainMergeId(long id) {mainMergeId = id;}

    public long getMainMergeId() {return mainMergeId;}

    private int tableNumber = -11;

    public int getTableNumber() {return tableNumber;}

    public void setTableNumber(int tableNumber) {this.tableNumber = tableNumber;}

    /*private int mainTableNumber = -1;
    public void setMainTableNumber(int number){mainTableNumber = number;}
    private int getMainTableNumber(){return mainTableNumber;}*/

    private int roomId = -11;

    public int getRoomId() {return roomId;}

    public void setRoomId(int roomId) {this.roomId = roomId;}

    public PopupWindow myPopupWindow;
    public ButtonHolder myButton;

    public void setMyButton()
    {
        Typeface typeItalic = Typeface.createFromAsset(context.getAssets(), "fonts/FRANGBI_.TTF");
        myButton.tableNumber.setTypeface(typeItalic);
    }


    public TableUseAdapter(Context c, DatabaseAdapter database, ArrayList<TableUse> tables, int billId)
    {
        context = c;
        this.dbA = database;
        this.tables = tables;
        this.billId = billId;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_table, null);
        resources = context.getResources();
        /**DISPLAY METRICS:  used to center the window in the screen**/
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;

        this.adapterCallback = ((AdapterTableUseCallback) context);
    }


    /**
     * interface to communicate with activity (TableActivity)
     */
    public interface AdapterTableUseCallback
    {
        void setIsMergeActivated(TableUse table);

    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition)
    {
        return false;
    }

    @Override
    public void onItemDismiss(int position)
    {

    }

    @Override
    public int getItemViewType(int position)
    {
        return tables.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.element_table_gridview, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        TableUse table = tables.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        //selected table color
        int colorLightBlue = Color.parseColor("#05a8c0");
        int colorRed = Color.parseColor("#cd0046");
        int colorBlack = Color.parseColor("#222222");

        /* Rounded corners for internal image */

        //different typeface for main and secondary table when merged tables
        Typeface typeItalic = Typeface.createFromAsset(context.getAssets(), "fonts/FRANGBI_.TTF");
        Typeface typeDemi = Typeface.createFromAsset(context.getAssets(), "fonts/FRANGD__.ttf");


        // 1. Set the table colors //
        // ----------------------- //

        // if the current table is in any form being used (reservation or people at the table)
        if (table.getStartTime() != null)
        {
            // TODO
            // does this refer to a reservation time?
            if (table.getEndTime() != null)
                { button.view.setBackgroundColor(Color.parseColor("#75ba51")); }

            else
            {
                // this table is the current bill (in Operative) table
                if (table.getBillId() == billId)
                {
                    button.view.setBackgroundColor(colorLightBlue);
                    if (table.getMainTable() == 0)
                        { button.tableNumber.setTypeface(typeItalic); }
                }

                //table already in use
                else
                    { button.view.setBackgroundColor(colorRed); }
            }

            button.actualSeat.setText(String.format("%01d", table.getTotalSeats()));
            button.tableNumber.setText(String.format("%02d", table.getMainTableNumber()));
        }

        // this table is not in use,
        // nothing special to do
        else
        {
            if (table.isShareTable() == 1)
            {
                GradientDrawable border = new GradientDrawable();
                border.setColor(colorBlack);
                border.setStroke((int) (3 * density), colorBlack); //gray border with full opacity
                border.setCornerRadius(8 * density);
                button.view.setBackground(border);
                button.actualSeat.setText(String.valueOf(table.getTotalSeats()));
                button.tableNumber.setTypeface(typeItalic);
            }
            else
            {
                //default color
                button.view.setBackgroundColor(Color.parseColor("#444444"));
                button.actualSeat.setText("0");
                button.tableNumber.setTypeface(typeDemi);
            }
            button.tableNumber.setText(String.format("%02d", table.getTableNumber()));

        }

        button.tableLabel.setText(R.string.table);

        button.seatNumber.setText(String.valueOf(table.getTableSeat()));





        // 2. handle table onClick //
        // ----------------------- //


        button.tableTextContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // if this mode is set, any click on another table will move the people from this table to another one
                if (MODE_MODIFY_TABLE_USE)
                {
                    // if the table that has been clicked is not empty
                    if (table.getStartTime() != null)
                        { DialogCreator.error(context, "Please select an empty table"); }

                    else if (StaticValue.blackbox)
                    {
                        // first, delete the current table usage

                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);

                        params.add(new BasicNameValuePair("billId", String.valueOf(tableModifyUse.getBillId())));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(tableModifyUse.getTableId())));

                        ((TableActivity) context).callHttpHandler("/deleteTableUse", params);


                        // then activate the other table
                        List<NameValuePair> params2 = new ArrayList<NameValuePair>(2);

                        params2.add(new BasicNameValuePair("seats", String.valueOf(tableModifyUse.getTotalSeats())));
                        params2.add(new BasicNameValuePair("billId", String.valueOf(tableModifyUse.getBillId())));
                        params2.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                        params2.add(new BasicNameValuePair("androidId", StaticValue.androidId));

                        ((TableActivity) context).callHttpHandler("/insertTableUse", params2);

                    }

                    MODE_MODIFY_TABLE_USE = false;
                    tableModifyUse = null;
                }


                // if this table is in use
                else if (table.getStartTime() != null)
                {
                    // Handle table merge process
                    if (getIsMergeSet())
                    {
                        //click to add merge table
                        if (table.isMergeTable() == 1)
                        {
                            if (!dbA.checkIfTableIsInUseInMerging(table.getTableId()) && table.getMainTable() == 0)
                            {
                                TableUse tu = dbA.fetchTableUseById(getMainMergeId());
                                dbA.insertTableUse(table.getTableId(), tu.getTotalSeats(), billId, 0, tu.getMainTableNumber());
                                tables = dbA.fetchTableUses(roomId);
                                button.tableNumber.setTypeface(typeItalic);
                                notifyDataSetChanged();
                            }
                            //click to delete this table from merging
                            else if (dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                            {
                                dbA.deleteFromTableUse(table.getTableId());
                                tables = dbA.fetchTableUses(roomId);
                                button.tableNumber.setTypeface(typeDemi);
                                notifyDataSetChanged();
                            }

                            else
                                { DialogCreator.message(context, R.string.thats_the_main_table); }
                        }

                        else
                        {
                            DialogCreator.error(context, R.string.this_table_cannot_be_merged);
                            openNoMergableTable();
                        }
                    }

                    else
                    {
                        // check if this table is used in some merging,
                        // this if it's in use but it's not a main table
                        if (!dbA.checkIfTableIsInUseInMerging(table.getTableId()))
// TODO why??? ->                            { /* openTableUse(table); */ }
                            { openModifyTableUse(table); }


                        // this table is merged, and is not a main table
                        // thus deselect it and reset the merge Id
                        else
                        {
                            //first save id, then delete from db
                            long id = dbA.getTableUseIdFromTableNumber(table.getTableId());
                            if (id != -1)
                            {
                                dbA.deleteFromTableUse(table.getTableId());

                                tables = dbA.fetchTableUses(roomId);
                                button.tableNumber.setTypeface(typeDemi);

                                notifyDataSetChanged();

                                isMergeSet = true;
                                adapterCallback.setIsMergeActivated(table);

                                TableUse tu = dbA.fetchTableUseById(id);
                                setMainMergeId(id);
                                setTableNumber(tu.getMainTableNumber());
                                setRoomId(tu.getRoomId());
                            }
                        }
                    }
                }


                // this is a brand new, not in use, table
                else
                {
                    // if we want to use this table to be merged with another one
                    if (getIsMergeSet())
                    {
                        //click to add merge table
                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            long id = getMainMergeId();

                            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                            params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));

                            myButton = button;
                            ((TableActivity) context).callHttpHandler("/insertTableUseMerge", params);
                        }

                        else
                        {
                            if (table.isMergeTable() == 1)
                            {
                                TableUse tu = dbA.fetchTableUseById(getMainMergeId());
                                button.tableNumber.setTypeface(typeItalic);
                                dbA.insertTableUse(table.getTableId(), tu.getTotalSeats(), billId, 0, tu.getMainTableNumber());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                            }

                            else
                                { openNoMergableTable(); }
                        }
                    }

                    // otherwise, open the popup to add some people to this table,
                    // and thus to make this table in use
                    // TODO
                    else
                        { openTableUse(table); }
                }
            }
        });


        // TODO should this be still present?
        /*
        button.tableTextContainer.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick (View view)
            {
                if (table.getStartTime() != null)
                {
                    //if it's a merge table, then deselect it and go in mergeMode
                    if (StaticValue.blackbox)
                    {
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        long id = getMainMergeId();
                        // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                        myButton = button;
                        ((TableActivity) context).callHttpHandler("/deleteTableUse", params);
                    }

                    else
                    {
                        if (dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                        {
                            long id = dbA.getTableUseIdFromSecondaryTableNumber(table.getTableId());
                            dbA.deleteFromTableUse(table.getTableId());
                            tables = dbA.fetchTableUses(roomId);
                            button.tableNumber.setTypeface(typeDemi);
                            notifyDataSetChanged();
                            isMergeSet = true;
                            adapterCallback.setIsMergeActivated(table);
                            TableUse tu = dbA.fetchTableUseById(id);
                            setMainMergeId(id);
                            setTableNumber(tu.getMainTableNumber());
                        }

                        else
                        {
                            if (table.getMainTable() == 1)
                                { dbA.execOnDb("DELETE FROM table_use WHERE total_bill_id=" + table.getBillId()); }
                            else
                                { dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId()); }

                            tables = dbA.fetchTableUses(roomId);
                            notifyDataSetChanged();
                            setTableNumber(-11);
                            setRoomId(-11);
                        }
                    }

                    return true;

                }

                else
                {
                    if (StaticValue.blackbox)
                    {
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        long id = getMainMergeId();

                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                        myButton = button;
                        ((TableActivity) context).callHttpHandler("/deleteTableUse", params);
                    }

                    else
                    {
                        if (table.isMergeTable() == 1)
                        {
                            //to deselect one merged table, that is not the main table
                            if (dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                            {
                                dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                                setTableNumber(-11);
                                setRoomId(-11);
                            }

                            else
                            {
                                dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                                setTableNumber(-11);
                                setRoomId(-11);
                            }
                        }
                    }

                    return true;
                }
            }
        });
         */



        /*

        // this table is not in use
        if (table.getStartTime() == null)
        {
            button.tableTextContainer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (getIsMergeSet())
                    {
                        //click to add merge table
                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            long id = getMainMergeId();
                            // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                            params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                            myButton = button;
                            ((TableActivity) context).callHttpHandler("/insertTableUseMerge", params);
                        }
                        else
                        {
                            if (table.isMergeTable() == 1)
                            {

                                TableUse tu = dbA.fetchTableUseById(getMainMergeId());
                                button.tableNumber.setTypeface(typeItalic);
                                dbA.insertTableUse(table.getTableId(), tu.getTotalSeats(), billId, 0, tu.getMainTableNumber());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                            }
                            else
                            {
                                openNoMergableTable();
                            }
                        }

                    }
                    else
                    {
                        //open popup to set table use
                        openTableUse(table);
                    }
                }
            });

            button.tableTextContainer.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    if (StaticValue.blackbox)
                    {
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        long id = getMainMergeId();
                        // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                        myButton = button;
                        ((TableActivity) context).callHttpHandler("/deleteTableUse", params);
                    }

                    else
                    {
                        if (table.isMergeTable() == 1)
                        {
                            //to deselect one merged table, that is not the main table
                            if (dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                            {
                                dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                                setTableNumber(-11);
                                setRoomId(-11);
                            }

                            else
                            {
                                dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                                setTableNumber(-11);
                                setRoomId(-11);
                            }
                        }
                    }

                    return true;
                }
            });
        }

        // the selected table is already in use,
        // and is linked to some order
        else
        {
            // if the table selected has the same bill ID
            // as the current bill.
            if (table.getBillId() == billId)
            {
                button.tableTextContainer.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (getIsMergeSet())
                        {
                            //click to add merge table
                            if (table.isMergeTable() == 1)
                            {
                                if (!dbA.checkIfTableIsInUseInMerging(table.getTableId()) && table.getMainTable() == 0)
                                {
                                    TableUse tu = dbA.fetchTableUseById(getMainMergeId());
                                    dbA.insertTableUse(table.getTableId(), tu.getTotalSeats(), billId, 0, tu.getMainTableNumber());
                                    tables = dbA.fetchTableUses(roomId);
                                    button.tableNumber.setTypeface(typeItalic);
                                    notifyDataSetChanged();
                                }
                                //click to delete this table from merging
                                else if (dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                                {
                                    dbA.deleteFromTableUse(table.getTableId());
                                    tables = dbA.fetchTableUses(roomId);
                                    button.tableNumber.setTypeface(typeDemi);
                                    notifyDataSetChanged();
                                }

                                else
                                { Toast.makeText(context, R.string.thats_the_main_table, Toast.LENGTH_SHORT).show(); }
                            }

                            else
                            {
                                Toast.makeText(context, R.string.this_table_cannot_be_merged, Toast.LENGTH_SHORT).show();
                                openNoMergableTable();
                            }
                        }

                        else
                        {
                            //open popup to set table use
                            if (!dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                            { openTableUse(table); }
                            //deselect table and set mainMergeId

                            else
                            {
                                //first save id, then delete from db

                                long id = dbA.getTableUseIdFromTableNumber(table.getTableId());
                                if (id != -1)
                                {
                                    dbA.deleteFromTableUse(table.getTableId());
                                    tables = dbA.fetchTableUses(roomId);
                                    button.tableNumber.setTypeface(typeDemi);
                                    notifyDataSetChanged();
                                    isMergeSet = true;
                                    adapterCallback.setIsMergeActivated(table);
                                    TableUse tu = dbA.fetchTableUseById(id);
                                    setMainMergeId(id);
                                    setTableNumber(tu.getMainTableNumber());
                                    setRoomId(tu.getRoomId());
                                }
                            }
                        }
                    }
                });

                //that's for deselect the table
                button.tableTextContainer.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View view)
                    {
                        //if it's a merge table, then deselect it and go in mergeMode
                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            long id = getMainMergeId();
                            // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                            params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                            myButton = button;
                            ((TableActivity) context).callHttpHandler("/deleteTableUse", params);
                        }

                        else
                        {
                            if (dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                            {
                                long id = dbA.getTableUseIdFromSecondaryTableNumber(table.getTableId());
                                dbA.deleteFromTableUse(table.getTableId());
                                tables = dbA.fetchTableUses(roomId);
                                button.tableNumber.setTypeface(typeDemi);
                                notifyDataSetChanged();
                                isMergeSet = true;
                                adapterCallback.setIsMergeActivated(table);
                                TableUse tu = dbA.fetchTableUseById(id);
                                setMainMergeId(id);
                                setTableNumber(tu.getMainTableNumber());
                            }

                            else
                            {
                                if (table.getMainTable() == 1)
                                { dbA.execOnDb("DELETE FROM table_use WHERE total_bill_id=" + table.getBillId()); }
                                else
                                { dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId()); }

                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                                setTableNumber(-11);
                                setRoomId(-11);
                            }
                        }

                        return true;
                    }
                });
            }
        }

         */


    }

    public void setTables(ArrayList<TableUse> t, int roomId)
    {
        tables.clear();
        this.roomId = roomId;
        tables = t;
        //Collections.reverse(tables);
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount()
    {
        if (tables != null)
        { return tables.size(); }
        else
        { return 0; }
    }



    public static class ButtonHolder extends ViewHolder
    {
        public View view;
        public RelativeLayout tableTextContainer;
        public CustomTextView tableLabel;
        public CustomTextView tableNumber;
        public CustomTextView seatNumber;
        public CustomTextView actualSeat;

        public ButtonHolder(View itemView)
        {
            super(itemView);
            view = itemView;
            tableTextContainer = view.findViewById(R.id.table_text_container);
            tableLabel =  view.findViewById(R.id.table_text);
            tableNumber =  view.findViewById(R.id.table_text_position);
            seatNumber = view.findViewById(R.id.table_seat);
            actualSeat =  view.findViewById(R.id.table_actual_seat);
        }

        @Override
        public String toString() { return "ButtonHolder, Title: " + tableLabel.getText().toString();}
    }



    /**
     * open popup to select quantity of seat at table
     *
     * @param table
     */
    public void openTableUse(TableUse table)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_use_table, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable()
        {
            @Override
            public void run()
            {
                CustomTextView header = (CustomTextView) popupView.findViewById(R.id.table_header_use_text);
                header.setText(resources.getString(R.string.table_capacity_type, table.getTableNumber(), table.getTableSeat(), 1));
            }
        });

        final CustomEditText quantitySeats = popupView.findViewById(R.id.customer_seat_input);

        final ImageButton btnDismiss = popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();

                // just in case
                MODE_CHANGE_SEATS_USE = false;
            }
        });


        final ImageButton buttonOk = popupView.findViewById(R.id.ok);
        buttonOk.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (quantitySeats.getText().toString().equals("") || Integer.valueOf(quantitySeats.getText().toString()) == 0)
                    { DialogCreator.error(context, "Please insert a valid number of seats"); }

                else
                {
                    int seats = Integer.valueOf(quantitySeats.getText().toString());
                    if (StaticValue.blackbox)
                    {

                        // if this function was called by modify table use,
                        // in order to change the amount of seats being used by another table,
                        // delete the previous table, and regenerate it with the amount of seats now given
                        if (MODE_CHANGE_SEATS_USE)
                        {
                            List<NameValuePair> paramsX = new ArrayList<NameValuePair>(2);

                            paramsX.add(new BasicNameValuePair("billId", String.valueOf(tableModifyUse.getBillId())));
                            paramsX.add(new BasicNameValuePair("tableId", String.valueOf(tableModifyUse.getTableId())));

                            ((TableActivity) context).callHttpHandler("/deleteTableUse", paramsX);

                            MODE_CHANGE_SEATS_USE = false;
                            tableModifyUse = null;
                        }


                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("seats", String.valueOf(seats)));
                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                        params.add(new BasicNameValuePair("androidId", StaticValue.androidId));

                        ((TableActivity) context).callHttpHandler("/insertTableUse", params);

                        popupWindow.dismiss();
                    }

                    else
                    {
                        if (seats <= table.getTableSeat())
                        {
                            if (table.isShareTable() == 1)
                            {
                                dbA.execOnDb("delete from table_use where total_bill_id=" + billId);
                                dbA.insertTableUse(table.getTableId(), seats, billId, 1, table.getTableNumber());
                            }
                            else
                            {
                                int oldId = dbA.checkIfExists("Select id from table_use where total_bill_id=" + billId);
                                if (oldId == -11)
                                {
                                    dbA.insertTableUse(table.getTableId(), seats, billId, 1, table.getTableNumber());
                                }
                                else
                                {
                                    dbA.execOnDb("delete from table_use where total_bill_id=" + billId);
                                    dbA.insertTableUse(table.getTableId(), seats, billId, 1, table.getTableNumber());

                                    dbA.showData("table_use");

                                    if (table.getStartTime() == null)
                                    { table.setStartTime(new Date()); }

                                }
                            }
                            setTableNumber(table.getTableNumber());
                            setRoomId(table.getRoomId());

                            tables = dbA.fetchTableUses(roomId);
                            notifyDataSetChanged();
                            popupWindow.dismiss();
                        }

                        else
                        {
                            if (table.isMergeTable() == 1)
                            {
                                dbA.execOnDb("delete from table_use where total_bill_id=" + billId);
                                isMergeSet = true;
                                long id = dbA.insertTableUse(table.getTableId(), seats, billId, 1, table.getTableNumber());
                                setTableNumber(table.getTableNumber());
                                setRoomId(table.getRoomId());
                                setMainMergeId(id);
                                dbA.showData("table_use");
                                adapterCallback.setIsMergeActivated(table);
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                            else
                            {
                                Toast.makeText(context, R.string.this_table_cannot_be_merged, Toast.LENGTH_SHORT).show();
                                openNoMergableTable();
                            }

                        }
                    }
                }
            }
        });

        setupDismissKeyboard(popupView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.table_main), 0, 0, 0);
    }



    /**
     *
     * @param table
     */
    private void openModifyTableUse(TableUse table)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_modify_table_use, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);


        CustomButton buttonFreeTable = popupView.findViewById(R.id.modify_table_use_button_free);
        CustomButton buttonChangeSeats = popupView.findViewById(R.id.modify_table_use_button_change_seats);
        CustomButton buttonChangeTable = popupView.findViewById(R.id.modify_table_use_button_change_table);


        // free this table, thus remove it from the table use
        buttonFreeTable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (StaticValue.blackbox)
                {
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    long id = getMainMergeId();
                    // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                    params.add(new BasicNameValuePair("billId", String.valueOf(table.getBillId())));
                    params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));

                    ((TableActivity) context).callHttpHandler("/deleteTableUse", params);
                }

                popupWindow.dismiss();
            }
        });


        // open the table use popup,
        // to allow the user to change the number of people that are present
        // at this table
        buttonChangeSeats.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();

                MODE_CHANGE_SEATS_USE = true;
                tableModifyUse = table;

                openTableUse(table);
            }
        });



        // open the modify table use selection, which allow the user to click on any other table,
        // to transfer the amount of people present at this table to another one
        buttonChangeTable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MODE_MODIFY_TABLE_USE = true;
                tableModifyUse = table;

                popupWindow.dismiss();
            }
        });







        final ImageButton btnDismiss = (ImageButton) popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });



        final ImageButton buttonOk = popupView.findViewById(R.id.ok);
        buttonOk.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });

        setupDismissKeyboard(popupView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.table_main), 0, 0, 0);
    }




    public void openNoMergableTable()
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_one_button, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable()
        {
            @Override
            public void run()
            {
              /*  @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.popup_container).getLayoutParams();
                int top1 = (int) (dpHeight - 52*density) / 2 - rlp1.height / 2;
                rlp1.topMargin = (int) (150*density);
                popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);
*/
            }
        });

        final CustomButton okButton = (CustomButton) popupView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new Button.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                { imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); }
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.table_main), 0, 0, 0);

    }

    public void setupDismissKeyboard(View view)
    {
        //Set up touch listener for non-text box views to hide keyboard.
        if ((view instanceof EditText))
        {
            ((EditText) view).setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_NEXT)
                    { keyboard_next_flag = true; }
                    return false;
                }
            });
            view.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (!hasFocus)
                    {
                        if (!(((Activity) context).getCurrentFocus() instanceof EditText) && !keyboard_next_flag)
                        {
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        keyboard_next_flag = false;
                    }
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupDismissKeyboard(innerView);
            }
        }
    }
}
