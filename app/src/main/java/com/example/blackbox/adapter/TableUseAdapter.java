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

public class TableUseAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperAdapter {
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

    private int billId;
    private boolean isMergeSet = false;
    public void setIsMerge(Boolean b){isMergeSet=b;}
    public boolean getIsMergeSet(){return isMergeSet;}

    private long mainMergeId = -11;
    public void setMainMergeId(long id){mainMergeId=id;}
    public long getMainMergeId(){return mainMergeId;}

    private int tableNumber = -11;
    public int getTableNumber() {return tableNumber;}
    public void setTableNumber(int tableNumber) {this.tableNumber = tableNumber;}

    /*private int mainTableNumber = -1;
    public void setMainTableNumber(int number){mainTableNumber = number;}
    private int getMainTableNumber(){return mainTableNumber;}*/

    private int roomId = -11;
    public int getRoomId() {return roomId;}
    public void setRoomId(int roomId) {this.roomId= roomId;}

    public PopupWindow myPopupWindow;
    public ButtonHolder myButton;
    public void setMyButton() {
        Typeface typeItalic = Typeface.createFromAsset(context.getAssets(), "fonts/FRANGBI_.TTF");
        myButton.tableNumber.setTypeface(typeItalic);
    }


    public TableUseAdapter(Context c, DatabaseAdapter database, ArrayList<TableUse> tables, int billId){
        context = c;
        this.dbA = database;
        this.tables = tables;
        this.billId = billId;
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_table, null);
        resources = context.getResources();
        /**DISPLAY METRICS:  used to center the window in the screen**/
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;

        this.adapterCallback = ((AdapterTableUseCallback) context);
    }



    /**
     * interface to communicate with activity (TableActivity)
     */
    public interface AdapterTableUseCallback {
        void setIsMergeActivated(TableUse table);

    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public int getItemViewType(int position){
        return tables.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.table_gridview, null);
        vh = new ButtonHolder(v);
        return vh;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TableUse table = tables.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        //selected table color
        int color2 = Color.parseColor("#05a8c0");

        /** Rounded corners for internal image **/

        //different typeface for main and secondary table when merged tables
        Typeface typeItalic = Typeface.createFromAsset(context.getAssets(), "fonts/FRANGBI_.TTF");
        Typeface typeDemi = Typeface.createFromAsset(context.getAssets(), "fonts/FRANGD__.ttf");
        //cd0046

        if(table.getStartTime()!=null){
            if(table.getEndTime()!=null){
                button.view.setBackgroundColor(Color.parseColor("#75ba51"));
            }
            else {
                //selected table
                if (table.getBillId() == billId){
                    button.view.setBackgroundColor(color2);
                    if(table.getMainTable() == 0)
                        button.tableNumber.setTypeface(typeItalic);
                }
                //table already in use
                else button.view.setBackgroundColor(Color.parseColor("#cd0046"));
            }
            button.actualSeat.setText(String.format("%01d", table.getTotalSeats()));
            button.tableNumber.setText(String.format("%02d", table.getMainTableNumber()));
        }else {
            if (table.isShareTable() == 1) {
                GradientDrawable border = new GradientDrawable();
                int color3 = Color.parseColor("#222222");
                border.setColor(color3);
                border.setStroke((int) (3 * density), color3); //gray border with full opacity
                /** Rounded corners for the buttons **/
                border.setCornerRadius(8 * density);
                /** Rounded corners for the buttons **/
                button.view.setBackground(border);
                button.actualSeat.setText(String.valueOf(table.getTotalSeats()));
                button.tableNumber.setTypeface(typeItalic);
            } else{
                //default color
                button.view.setBackgroundColor(Color.parseColor("#444444"));
                button.actualSeat.setText("0");
                button.tableNumber.setTypeface(typeDemi);
            }
            button.tableNumber.setText(String.format("%02d", table.getTableNumber()));

        }

        button.tableLabel.setText(R.string.table);

        button.seatNumber.setText(String.valueOf(table.getTableSeat()));

        //if table is not assigned
        if(table.getStartTime()==null) {
            button.tableTextContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getIsMergeSet()) {
                        //click to add merge table
                        if (StaticValue.blackbox) {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            long id =getMainMergeId();
                           // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                            params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                            myButton = button;
                            ((TableActivity) context).callHttpHandler("/insertTableUseMerge", params);
                        } else {
                            if (table.isMergeTable() == 1) {

                                TableUse tu = dbA.fetchTableUseById(getMainMergeId());
                                button.tableNumber.setTypeface(typeItalic);
                                dbA.insertTableUse(table.getTableId(), tu.getTotalSeats(), billId, 0, tu.getMainTableNumber());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                            } else {
                                openNoMergableTable();
                            }
                        }

                    } else {
                        //open popup to set table use
                        openTableUse(table);
                    }
                }
            });

            button.tableTextContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (StaticValue.blackbox) {
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        long id =getMainMergeId();
                        // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                        myButton = button;
                        ((TableActivity) context).callHttpHandler("/deleteTableUse", params);
                    } else {
                        if (table.isMergeTable() == 1) {
                            //to deselect one merged table, that is not the main table
                            if (dbA.checkIfTableIsInUseInMerging(table.getTableId())) {

                                dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId());
                                tables = dbA.fetchTableUses(roomId);
                                notifyDataSetChanged();
                                setTableNumber(-11);
                                setRoomId(-11);
                            } else {
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
        else{
            if(table.getBillId()==billId){
                button.tableTextContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getIsMergeSet()) {
                            //click to add merge table
                            if (table.isMergeTable() == 1) {
                                if(!dbA.checkIfTableIsInUseInMerging(table.getTableId())
                                        && table.getMainTable() == 0){

                                    TableUse tu = dbA.fetchTableUseById(getMainMergeId());
                                    dbA.insertTableUse(table.getTableId(), tu.getTotalSeats(), billId, 0, tu.getMainTableNumber());
                                    tables = dbA.fetchTableUses(roomId);
                                    button.tableNumber.setTypeface(typeItalic);
                                    notifyDataSetChanged();
                                }
                                //click to delete this table from merging
                                else if(dbA.checkIfTableIsInUseInMerging(table.getTableId())){
                                    dbA.deleteFromTableUse(table.getTableId());
                                    tables = dbA.fetchTableUses(roomId);
                                    button.tableNumber.setTypeface(typeDemi);
                                    notifyDataSetChanged();
                                }
                                else
                                    Toast.makeText(context, R.string.thats_the_main_table, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, R.string.this_table_cannot_be_merged, Toast.LENGTH_SHORT).show();
                                openNoMergableTable();
                            }
                        }
                        else {
                            //open popup to set table use
                            if(!dbA.checkIfTableIsInUseInMerging(table.getTableId()))
                                openTableUse(table);
                            //deselect table and set mainMergeId
                            else{
                                //first save id, then delete from db

                                long id = dbA.getTableUseIdFromTableNumber(table.getTableId());
                                if(id!=-1) {
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
                button.tableTextContainer.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        //if it's a merge table, then deselect it and go in mergeMode
                        if (StaticValue.blackbox) {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            long id =getMainMergeId();
                            // params.add(new BasicNameValuePair("mainTableId", String.valueOf(id)));
                            params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                            params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                            myButton = button;
                            ((TableActivity) context).callHttpHandler("/deleteTableUse", params);
                        } else {
                            if (dbA.checkIfTableIsInUseInMerging(table.getTableId())) {
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
                            } else {
                                if (table.getMainTable() == 1)
                                    dbA.execOnDb("DELETE FROM table_use WHERE total_bill_id=" + table.getBillId());
                                else
                                    dbA.execOnDb("DELETE FROM table_use WHERE id=" + table.getId());

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
    }

    public void setTables(ArrayList<TableUse> t, int roomId){
        tables.clear();
        this.roomId = roomId;
        tables = t;
        //Collections.reverse(tables);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(tables!=null)
        return tables.size();
        else return 0;
    }

    public static class ButtonHolder extends ViewHolder{
        public View view;
        public RelativeLayout tableTextContainer;
        public CustomTextView tableLabel;
        public CustomTextView tableNumber;
        public CustomTextView seatNumber;
        public CustomTextView actualSeat;
        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            tableTextContainer  = (RelativeLayout) view.findViewById(R.id.table_text_container);
            tableLabel = (CustomTextView) view.findViewById(R.id.table_text);
            tableNumber = (CustomTextView) view.findViewById(R.id.table_text_position);
            seatNumber = (CustomTextView) view.findViewById(R.id.table_seat);
            actualSeat = (CustomTextView) view.findViewById(R.id.table_actual_seat);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+tableLabel.getText().toString();}
    }


    /**
     * TODO open popup to select quantity of seat at table
     * @param table
     */
    public void openTableUse(TableUse table){
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.use_table_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                CustomTextView header = (CustomTextView) popupView.findViewById(R.id.table_header_use_text);
                header.setText(resources.getString(R.string.table_capacity_type, table.getTableNumber(), table.getTableSeat(), 1));
            }
        });

        final CustomEditText quantitySeats = (CustomEditText) popupView.findViewById(R.id.customer_seat_input);
        //to show automatically soft keyboard
        /*InputMethodManager imm = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);*/
        final ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        final ImageButton buttonOk = (ImageButton)popupView.findViewById(R.id.ok);
        buttonOk.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(quantitySeats.getText().toString().equals("") || Integer.valueOf(quantitySeats.getText().toString())==0){
                    Toast.makeText(context, "hey....come on....", Toast.LENGTH_SHORT).show();
                }
                else{
                    int seats = Integer.valueOf(quantitySeats.getText().toString());
                    if(StaticValue.blackbox){
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("seats", String.valueOf(seats)));
                        params.add(new BasicNameValuePair("billId", String.valueOf(billId)));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(table.getTableId())));
                        myPopupWindow = popupWindow;
                        ((TableActivity) context).callHttpHandler("/insertTableUse", params);
                    }else {
                        if (seats <= table.getTableSeat()) {
                            if (table.isShareTable() == 1) {
                                dbA.execOnDb("delete from table_use where total_bill_id=" + billId);
                                dbA.insertTableUse(table.getTableId(), seats, billId, 1, table.getTableNumber());
                            } else {
                                int oldId = dbA.checkIfExists("Select id from table_use where total_bill_id=" + billId);
                                if (oldId == -11) {
                                    dbA.insertTableUse(table.getTableId(), seats, billId, 1, table.getTableNumber());
                                } else {
                                    dbA.execOnDb("delete from table_use where total_bill_id=" + billId);
                                    dbA.insertTableUse(table.getTableId(), seats, billId, 1, table.getTableNumber());

                                    dbA.showData("table_use");

                                    if (table.getStartTime() == null)
                                        table.setStartTime(new Date());

                                }
                            }
                            setTableNumber(table.getTableNumber());
                            setRoomId(table.getRoomId());

                            tables = dbA.fetchTableUses(roomId);
                            notifyDataSetChanged();
                            popupWindow.dismiss();
                        } else {
                            if (table.isMergeTable() == 1) {
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
                            } else {
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
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.table_main), 0, 0, 0);
    }

    public void openNoMergableTable(){
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.one_button_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
              /*  @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.popup_container).getLayoutParams();
                int top1 = (int) (dpHeight - 52*density) / 2 - rlp1.height / 2;
                rlp1.topMargin = (int) (150*density);
                popupView.findViewById(R.id.popup_container).setLayoutParams(rlp1);
*/
            }
        });

        final CustomButton okButton = (CustomButton)popupView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null)
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.table_main), 0, 0, 0);

    }

    public void setupDismissKeyboard(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if((view instanceof EditText)) {
            ((EditText)view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_NEXT) keyboard_next_flag = true;
                    return false;
                }
            });
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if(!(((Activity)context).getCurrentFocus() instanceof EditText) && !keyboard_next_flag){
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        keyboard_next_flag = false;
                    }
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupDismissKeyboard(innerView);
            }
        }
    }
}
