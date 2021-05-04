package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.blackbox.model.Table;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by tiziano on 3/20/18.
 * this is used whe you configure the tables, for operative table look at TableUseAdapter
 */

public class TableAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperAdapter {
    private float density;
    private float dpHeight;
    private float dpWidth;
    private Context context;
    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private ArrayList<Table> tables;
    private boolean keyboard_next_flag = false;
    private int roomId = -12;
    public PopupWindow myPopupWindow;
    public View myPopupView;
    public PopupWindow myPopupDialog;
    public void closePopupWindow(){
        if(myPopupWindow!=null) myPopupWindow.dismiss();
    }
    public void closeTwoPopupWindow(){
        if(myPopupDialog!=null) myPopupDialog.dismiss();
        if(myPopupWindow!=null) myPopupWindow.dismiss();
    }

    public TableAdapter(Context c,DatabaseAdapter database, ArrayList<Table> tables){
        context = c;
        this.dbA = database;
        this.tables = tables;
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_table, null);
        /**DISPLAY METRICS:  used to center the window in the screen**/
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;


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
        switch(viewType){
            case -11:
                v = inflater.inflate(R.layout.table_plusbutton, null);
                vh = new PlusButtonHolder(v);
                break;
            default:
                v = inflater.inflate(R.layout.table_gridview, null);
                vh = new ButtonHolder(v);
                break;
        }
        return vh;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Table table = tables.get(position);
        switch(getItemViewType(position)) {
            /**
             *  CASE: BUTTON
             *  DEPENDING ON THE BUTTON ID( ADD BUTTON OR OTHER BUTTON)
             *  THE LAYOUT PARAMS ARE SET TO THOSE SPECIFIED IN THE BUTTONLAYOUT OBJECT
             */
            case -11:
                PlusButtonHolder plusbutton = (PlusButtonHolder) holder;
                plusbutton.imageContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        openNewTablesPopup();
                    }
                });
                break;
            default:
                ButtonHolder button = (ButtonHolder) holder;
                int color2 = Color.parseColor("#05a8c0");

                /** Rounded corners for internal image **/
                if(table.getShareTable()==1){

                    GradientDrawable border = new GradientDrawable();
                    int color3 = Color.parseColor("#222222");
                    border.setColor(color3);
                    border.setCornerRadius(8*density);
                    //border.setColor(0xFF000000); //black background
                    //border.setStroke((int) (3*density), 0xFFC6C5C6); //gray border with full opacity
                    /** Rounded corners for the buttons **/
                    button.view.setBackground(border);
                }else {

                }


                button.conf.setVisibility(View.VISIBLE);
                button.op.setVisibility(View.GONE);

                button.tableLabel.setText(R.string.table);
                button.tableNumber.setText(String.format("%02d", table.getTableNumber()));
                button.actualSeat.setText(String.valueOf(table.getPeopleNumber()));
                button.tableTextContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openModifyTablesPopup(table);
                    }
                });
                break;
        }
    }

    /**
     * set tables to display
     * @param t
     * @param roomId
     */
    public void setTables(ArrayList<Table> t, int roomId){
        tables.clear();
        this.roomId = roomId;
        tables = t;
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
        public CustomTextView actualSeat;
        public View tableLine;
        public LinearLayout conf;
        public LinearLayout op;
        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            tableTextContainer  = (RelativeLayout) view.findViewById(R.id.table_text_container);
            tableLabel = (CustomTextView) view.findViewById(R.id.table_text);
            tableNumber = (CustomTextView) view.findViewById(R.id.table_text_position);
            actualSeat = (CustomTextView) view.findViewById(R.id.table_seat_conf);
            tableLine = view.findViewById(R.id.table_vertical_line);
            conf  = (LinearLayout) view.findViewById(R.id.configure_container);
            op = (LinearLayout) view.findViewById(R.id.operative_container);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+tableLabel.getText().toString();}
    }

    public static class PlusButtonHolder extends ViewHolder{
        public View view;
        public RelativeLayout imageContainer;
        public ImageView image;
        public PlusButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageContainer = (RelativeLayout) view.findViewById(R.id.table_plus_container);
            image = (ImageView)view.findViewById(R.id.button_img);
        }
        @Override
        public String toString(){ return "PlusButton";}
    }

    public void loadTablesName(View popupView, boolean modify){
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

        //THIS MAKE LYNEAR LAYOUT MANAGER TO START FROM RIGHT AND NOT FROM LEFT

        //layoutManager.setReverseLayout(true);
        RecyclerView nameRecycler = (RecyclerView) popupView.findViewById(R.id.table_name_recycler);
        nameRecycler.setHasFixedSize(true);
        nameRecycler.setLayoutManager(layoutManager);

        TableNameAdapter nameAdapter = new TableNameAdapter(context, dbA, modify, popupView);
        nameRecycler.setAdapter(nameAdapter);

    }

    public void setTableValues(Table table, View popupView){
        CustomEditText capacity = (CustomEditText) popupView.findViewById(R.id.table_capacity_input);
        CustomEditText name = (CustomEditText) popupView.findViewById(R.id.table_name_input);
        capacity.setText(String.valueOf(table.getPeopleNumber()));
        name.setText(table.getTableName());
    }

    /**
     * open new table popup
     */
    public void openNewTablesPopup(){

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.new_table_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        popupView.post(new Runnable() {
            @Override
            public void run() {
            }
        });

        final ImageButton merge = (ImageButton)popupView.findViewById(R.id.table_merge_check);
        final ImageButton sharing = (ImageButton)popupView.findViewById(R.id.table_sharing_check);
        final CustomEditText capacity = (CustomEditText) popupView.findViewById(R.id.table_capacity_input);
        final CustomEditText name = (CustomEditText) popupView.findViewById(R.id.table_name_input);
        final CustomEditText quantity = (CustomEditText) popupView.findViewById(R.id.table_quantity_input);
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
                if(capacity.getText().toString().equals("")){
                    Toast.makeText(context, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }else if(Integer.parseInt(capacity.getText().toString())==0){
                    Toast.makeText(context, R.string.capacity_cant_be_0, Toast.LENGTH_SHORT).show();
                }else if(quantity.getText().toString().equals("")){
                    Toast.makeText(context, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }else if(Integer.parseInt(quantity.getText().toString())==0){
                    Toast.makeText(context, R.string.quantity_cant_be_0, Toast.LENGTH_SHORT).show();
                }else{
                    //save table(s) and close popup
                    int mergeValue = 0;
                    if(merge.isActivated()) mergeValue = 1;
                    int shareValue= 0;
                    if(sharing.isActivated()) shareValue = 1;
                    String tableName = "";
                    int quantityOfTables = tables.size()-1;
                    if(name.getText().toString().equals("")) tableName = "Table Type "+(dbA.selectDistinctValueTable()+1);
                    else tableName = name.getText().toString();

                    if(StaticValue.blackbox){
                        myPopupWindow = popupWindow;
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("quantity", quantity.getText().toString()));
                        params.add(new BasicNameValuePair("capacity", capacity.getText().toString()));
                        params.add(new BasicNameValuePair("roomId", String.valueOf(roomId)));
                        params.add(new BasicNameValuePair("tableName", tableName));
                        params.add(new BasicNameValuePair("mergeValue", String.valueOf(mergeValue)));
                        params.add(new BasicNameValuePair("shareValue", String.valueOf(shareValue)));
                        ((TableActivity) context).callHttpHandler("/insertTable", params);
                    }else {
                        for (int i = 1; i <= Integer.parseInt(quantity.getText().toString()); i++) {
                            dbA.insertTableConfiguration((quantityOfTables + i), Integer.parseInt(capacity.getText().toString()), roomId, tableName, mergeValue, shareValue);
                        }
                        tables.clear();
                        tables = dbA.fetchTables(roomId);
                        Table addTable = new Table();
                        addTable.setId(-11);
                        tables.add(addTable);
                        notifyDataSetChanged();
                        popupWindow.dismiss();
                    }

                }

            }
        });
        merge.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
            if(popupView.findViewById(R.id.table_sharing_check).isActivated())
                popupView.findViewById(R.id.table_sharing_check).setActivated(!popupView.findViewById(R.id.table_sharing_check).isActivated());


                popupView.findViewById(R.id.table_merge_check).setActivated(!popupView.findViewById(R.id.table_merge_check).isActivated());
            }
        });

        sharing.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(popupView.findViewById(R.id.table_merge_check).isActivated())
                popupView.findViewById(R.id.table_merge_check).setActivated(!popupView.findViewById(R.id.table_merge_check).isActivated());


                popupView.findViewById(R.id.table_sharing_check).setActivated(!popupView.findViewById(R.id.table_sharing_check).isActivated());
            }
        });
        setupDismissKeyboard(popupView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.table_main), 0, 0, 0);
    }


    public void addTableFromServer(ArrayList<Table> myTables ){
        int quantityOfTables = tables.size()-1;
        for (Table table : myTables) {
            dbA.insertTableConfigurationFromServer(table);
        }
        functionAddTableFromServer();

    }

    public void functionAddTableFromServer(){
        tables.clear();
        tables = dbA.fetchTables(roomId);
        Table addTable = new Table();
        addTable.setId(-11);
        tables.add(addTable);
        notifyDataSetChanged();
        myPopupWindow.dismiss();
    }

    /**
     * open popup to modify existing table
     * @param table
     */
    public void openModifyTablesPopup(Table table){

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.modify_table_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {

            }
        });

        final ImageButton merge = (ImageButton)popupView.findViewById(R.id.table_merge_check);
        final ImageButton sharing = (ImageButton)popupView.findViewById(R.id.table_sharing_check);
        final CustomEditText capacity = (CustomEditText) popupView.findViewById(R.id.table_capacity_input);
        final CustomEditText name = (CustomEditText) popupView.findViewById(R.id.table_name_input);
        final CustomEditText quantity = (CustomEditText) popupView.findViewById(R.id.table_quantity_input);
        final CustomButton delete = (CustomButton) popupView.findViewById(R.id.table_delete_button);
        boolean mergeb = (table.getMergeTable() != 0);
        merge.setActivated(mergeb);
        boolean shareb= (table.getShareTable() != 0);
        sharing.setActivated(shareb);
        capacity.setText(String.valueOf(table.getPeopleNumber()));
        name.setText(table.getTableName());
        quantity.setText(String.valueOf(1));
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
                if(capacity.getText().toString().equals("")){
                    Toast.makeText(context, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }else if(Integer.parseInt(capacity.getText().toString())==0){
                    Toast.makeText(context, R.string.capacity_cant_be_0, Toast.LENGTH_SHORT).show();
                }else if(quantity.getText().toString().equals("")){
                    Toast.makeText(context, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }else if(Integer.parseInt(quantity.getText().toString())==0){
                    Toast.makeText(context, R.string.quantity_cant_be_0, Toast.LENGTH_SHORT).show();
                }else{
                    //save table(s) and close popup
                    int mergeValue = 0;
                    if(merge.isActivated()) mergeValue = 1;
                    int shareValue= 0;
                    if(sharing.isActivated()) mergeValue = 1;
                    String tableName = "";
                    int quantityOfTables = tables.size()-1;
                    if(name.getText().toString().equals("")) tableName = "Table Type "+(dbA.selectDistinctValueTable()+1);
                    else tableName = name.getText().toString();
                    if(StaticValue.blackbox){
                        myPopupWindow = popupWindow;
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("quantity", quantity.getText().toString()));
                        params.add(new BasicNameValuePair("capacity", capacity.getText().toString()));
                        params.add(new BasicNameValuePair("roomId", String.valueOf(roomId)));
                        params.add(new BasicNameValuePair("tableName", tableName));
                        params.add(new BasicNameValuePair("mergeValue", String.valueOf(mergeValue)));
                        params.add(new BasicNameValuePair("shareValue", String.valueOf(shareValue)));
                        params.add(new BasicNameValuePair("tableId", String.valueOf(table.getId())));
                        ((TableActivity) context).callHttpHandler("/insertAndUpdateTable", params);
                    }else {
                        if (Integer.parseInt(quantity.getText().toString()) > 1) {
                            for (int i = 1; i <= Integer.parseInt(quantity.getText().toString()); i++) {
                                if (i == 1) {
                                    dbA.updateTable(table.getTableNumber(), Integer.parseInt(quantity.getText().toString()), roomId, tableName, mergeValue, shareValue, table.getId());

                                } else {
                                    dbA.insertTableConfiguration((quantityOfTables), Integer.parseInt(quantity.getText().toString()), roomId, tableName, mergeValue, shareValue);
                                }

                            }
                        } else {
                            dbA.updateTable(table.getTableNumber(), Integer.parseInt(capacity.getText().toString()), roomId, tableName, mergeValue, shareValue, table.getId());

                        }
                        tables.clear();
                        tables = dbA.fetchTables(roomId);
                        Table addTable = new Table();
                        addTable.setId(-11);
                        tables.add(addTable);
                        notifyDataSetChanged();
                        popupWindow.dismiss();
                    }
                }

            }
        });

        merge.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.table_merge_check).setActivated(!popupView.findViewById(R.id.table_merge_check).isActivated());
            }
        });

        sharing.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.table_sharing_check).setActivated(!popupView.findViewById(R.id.table_sharing_check).isActivated());
            }
        });

        delete.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(StaticValue.blackbox){
                    myPopupWindow = popupWindow;
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("roomId", String.valueOf(table.getRoomId())));
                    params.add(new BasicNameValuePair("tableNumber", String.valueOf(table.getTableNumber())));
                    ((TableActivity) context).callHttpHandler("/deleteTable", params);
                }else {
                    dbA.updateTableConfiguration(table.getRoomId(), table.getTableNumber());
                    tables.clear();
                    tables = dbA.fetchTables(roomId);
                    Table addTable = new Table();
                    addTable.setId(-11);
                    tables.add(addTable);
                    notifyDataSetChanged();
                    popupWindow.dismiss();
                }

            }
        });

        setupDismissKeyboard(popupView);
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
