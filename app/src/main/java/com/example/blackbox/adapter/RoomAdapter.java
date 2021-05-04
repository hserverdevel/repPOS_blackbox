package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.InputFilter;
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
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.model.Room;
import com.example.blackbox.model.StaticValue;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by tiziano on 3/19/18.
 */

public class RoomAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperAdapter {
    private final AdapterRoomCallback adapterCallback;
    private float density;
    private float dpHeight;
    private float dpWidth;


    private final LayoutInflater inflater;
    private Context context;
    private DatabaseAdapter dbA;
    private ArrayList<Room> rooms;
    private boolean keyboard_next_flag = false;
    private int activatedButton = -1;

    private boolean operative;

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

    public RoomAdapter(Context c, DatabaseAdapter database, ArrayList<Room> rooms, boolean operative, int activatedButton){
        context = c;
        this.dbA = database;
        this.rooms = rooms;
        this.operative = operative;
        this.activatedButton = activatedButton;
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        /**DISPLAY METRICS:  used to center the window in the screen**/
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;

        this.adapterCallback = ((AdapterRoomCallback) context);

    }

    /**
     * set rooms to be displayed
     * @param rooms
     * @param roomId
     */
    public void setRooms(ArrayList<Room> rooms, int roomId){
        this.rooms = rooms;
        this.activatedButton = roomId;
        notifyDataSetChanged();
    }

    /**
     * interface to communicate with activity (TableActivity)
     */
    public interface AdapterRoomCallback {
        void openTableView(int roomId);

        void openTableUseView(int roomId);

        void restartRoom();

        void showLastRoom(int roomPosition);

    }

    /**
     * return next room position, if there aren't any room return -15
     * @return
     */
    public int nextRoomPosition(){
        int returnPosition = -15;
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).getId()==activatedButton){
                if(i!=rooms.size()-1){
                   returnPosition = i+1;
                   break;
                }
            }
        }
        return returnPosition;
    }

    /**
     * same as above but for previus room
     * @return
     */
    public int prevRoomPosition(){
        int returnPosition = -15;
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).getId()==activatedButton){
                if(i!=0 ){
                    returnPosition = i-1;
                    break;
                }
            }
        }
        return returnPosition;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        switch(viewType){
            case -15:
                v = inflater.inflate(R.layout.room_gridview_plus, null);
                vh = new ButtonHolder(v);
                break;
            default:
                v = inflater.inflate(R.layout.room_gridview, null);
                vh = new ButtonHolder(v);
                break;
        }
        return vh;
       /*
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.room_gridview, null);
        vh = new ButtonHolder(v);

        return vh;*/
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Room room = rooms.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        if(room.getId()==activatedButton) {
            button.circleView.setActivated(true);
        }
        else button.circleView.setActivated(false);
    }

    @Override
    public int getItemViewType(int position){
        return rooms.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    /**
     * holder for room view
     */
    public static class ButtonHolder extends ViewHolder{
        public View view;
        public View circleView;
        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            circleView = view.findViewById(R.id.circle_view);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: ";}
    }

    /**
     * open pop up for new room name
     */
    public void openNewRoomPopup(){
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        final View popupView = layoutInflater.inflate(R.layout.single_input_dialog, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        ((CustomEditText)popupView.findViewById(R.id.single_input)).setHint(R.string.name_new_zone);
        //to show automatically soft keyboard
        InputMethodManager imm = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                CustomEditText editText = (CustomEditText)popupView.findViewById(R.id.single_input);
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(18); //Filter to 10 characters
                editText .setFilters(filters);
            }
        });

        final ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((TableActivity)context).restartRoom();
                popupWindow.dismiss();
            }
        });
        final ImageButton buttonOk = (ImageButton)popupView.findViewById(R.id.ok);
        buttonOk.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                String name = ((CustomEditText)popupView.findViewById(R.id.single_input)).getText().toString();
                if(name.equals("")){
                    Toast.makeText(context, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }else{
                    int id = dbA.checkIfExists("Select * from room where name='"+name+"';");
                    if(id==-11){
                        if(StaticValue.blackbox){
                            myPopupWindow = popupWindow;
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("name", name));
                            ((TableActivity) context).callHttpHandler("/insertRoom", params);
                        }else {
                            dbA.execOnDb("INSERT INTO room (name) VALUES('" + name + "');");
                            popupWindow.dismiss();
                            adapterCallback.showLastRoom(rooms.size() - 1);
                        }
                    }else{
                        Toast.makeText(context, R.string.this_name_is_already_saved, Toast.LENGTH_SHORT).show();
                    }
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

    /**
     * return room selected
     * @return
     */
    public Room getRoomSelected(){
        Room room = new Room();
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).getId()==activatedButton){
                room = rooms.get(i);
                break;
           }
        }
        return room;
    }

    /**
     * return position of room selected in rooms array
     * @return
     */
    public int getRoomePosition(){
        int position = -15;
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).getId()==activatedButton){
                position = i;
                break;
            }
        }
        return position;
    }

    /**
     * open modify room popup
     */
    public void openModifyRoomPopup(){
        Room room = getRoomSelected();
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.room_modify_dialog, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                CustomEditText nameText = (CustomEditText) popupView.findViewById(R.id.room_name_modify);
                nameText.setText(room.getName());
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(18); //Filter to 10 characters
                nameText .setFilters(filters);
            }
        });

        final ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        final CustomButton delete = (CustomButton)popupView.findViewById(R.id.delete_button);
        delete.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(StaticValue.blackbox){
                    myPopupWindow = popupWindow;
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("id", String.valueOf(room.getId())));
                    ((TableActivity) context).callHttpHandler("/deleteRoom", params);
                }else {
                    dbA.execOnDb("DELETE FROM room WHERE id=" + room.getId());
                    dbA.execOnDb("DELETE FROM table_configuration WHERE room_id=" + room.getId() + ";");
                    popupWindow.dismiss();
                    adapterCallback.restartRoom();
                }
            }
        });
        final ImageButton buttonOk = (ImageButton)popupView.findViewById(R.id.ok);
        buttonOk.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                CustomEditText nameText = (CustomEditText) popupView.findViewById(R.id.room_name_modify);
                String name = nameText.getText().toString();
                if(name.equals("")){
                    Toast.makeText(context, R.string.name_cant_be_empty, Toast.LENGTH_SHORT).show();
                }else{
                    int id = dbA.checkIfExists("Select * from room where name='"+name+"';");
                    if(id==-11){
                        if(StaticValue.blackbox){
                            myPopupWindow = popupWindow;
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("id", String.valueOf(room.getId())));
                            params.add(new BasicNameValuePair("name", name.replaceAll("'", "\'") ));
                            ((TableActivity) context).callHttpHandler("/updateRoom", params);
                        }else {
                            dbA.execOnDb("UPDATE room SET name= '" + name.replaceAll("'", "\'") + "' WHERE id = " + room.getId() + "");
                            popupWindow.dismiss();
                            adapterCallback.showLastRoom(getRoomePosition());
                        }
                    }else{
                        if(id==room.getId()){
                            popupWindow.dismiss();
                        }else{
                            Toast.makeText(context, R.string.this_room_already_exists, Toast.LENGTH_SHORT).show();
                        }
                    }

                }

            }
        });

        setupDismissKeyboard(popupView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.table_main), 0, 0, 0);
    }
}
