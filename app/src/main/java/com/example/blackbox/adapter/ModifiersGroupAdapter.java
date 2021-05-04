package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.MainActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.StaticValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.OnFocusChangeListener;
import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static android.view.View.VISIBLE;

public class ModifiersGroupAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {
    private Context context;
    private Resources resources;
    private ButtonLayout current_product;
    private ArrayList<ModifiersGroup> groups;
    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private RecyclerView parent;
    public ModifierAdapter childAdapter;
    public ModifiersGroup selectedGroup = null;
    public void setSelectedGroup(ModifiersGroup g){selectedGroup = g;}
    private float dpHeight;
    private float dpWidth;
    private float density;
    private boolean keyboard_next_flag = false;
    public boolean addGroupActive = false;
    public void setAddGroupActive(){
        addGroupActive = false;
    }
    public boolean getAddGroupActive(){return addGroupActive;}
    public boolean addGroupActiveFixed = false;
    public void setAddGroupActiveFixed(){
        addGroupActiveFixed = false;
    }
    public boolean getAddGroupActiveFixed(){return addGroupActiveFixed;}
    public boolean addModifierActive = false;
    public void setAddModifierActive(boolean value){addModifierActive = value;}
    public boolean getAddModifierActive(){return addModifierActive;}
    public boolean addModifierActiveFixed = false;
    public void setAddModifierActiveFixed(boolean value){addModifierActiveFixed = value;}
    public boolean getAddModifierActiveFixed(){return addModifierActiveFixed;}
    private int number_of_groups;
    public boolean checknotes = false;
    public boolean getChecknotes(){
        return checknotes;
    }

    private final int MAX_NUMBER_OF_GROUPS = 18;

    private PopupWindow myPopupWindow;
    public View myPopupView;
    public PopupWindow myPopupDialog;
    public void closePopupWindow(){
        if(myPopupWindow!=null) myPopupWindow.dismiss();
    }
    public void closeTwoPopupWindow(){
        if(myPopupDialog!=null) myPopupDialog.dismiss();
        if(myPopupWindow!=null) myPopupWindow.dismiss();
    }
    public int myGroupId;

    public ModifiersGroupAdapter(Context c, DatabaseAdapter dbA, ButtonLayout current_product, RecyclerView parent) {
        context = c;
        resources = context.getResources();
        this.dbA = dbA;
        this.current_product = current_product;
        this.parent = parent;
        getCurrentModifiersGroupSet();
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        /** DISPLAY METRICS USED TO CENTER POPUP WINDOW **/
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;
        /** Rounded Corners **/
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFD3D3D3); //black background ->todo set background color accordingly to db data
        border.setStroke((int) (3*density), 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8*density);
        (((Activity)context).findViewById(R.id.backButton)).setBackground(border);
        (((Activity)context).findViewById(R.id.backButton)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).switchView(MainActivity.DEFAULT_VIEW,0);
            }
        });
        ((CustomTextView)((Activity)context).findViewById(R.id.currentProductTitle)).setText(current_product.getTitle());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        switch (viewType) {
            case -11:
                v = inflater.inflate(R.layout.gridview_plus_button, null);
                vh = new GridAdapter.PlusButtonHolder(v);
                break;
            default:
                v = inflater.inflate(R.layout.gridview_subelement, null);
                v.findViewById(R.id.subtitle).setVisibility(GONE);
                vh = new GroupHolder(v);
                break;
        }
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        if (groups.get(position).getID() == -11) return -11;
        else return 1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ModifiersGroup g = groups.get(position);
        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFb1b0b0);//black background ->todo set background color accordingly to db data
        border.setStroke(2, 0xFFd3d3d3); //gray border with full opacity
        switch (getItemViewType(position)) {
            /**
             *  CASE: BUTTON
             *  DEPENDING ON THE BUTTON ID( ADD BUTTON OR OTHER BUTTON)
             *  THE LAYOUT PARAMS ARE SET TO THOSE SPECIFIED IN THE BUTTONLAYOUT OBJECT
             */
            case -11:
                GridAdapter.PlusButtonHolder plusbutton = (GridAdapter.PlusButtonHolder) holder;
                plusbutton.image.setImageResource(R.drawable.addsymbolcontrol);
                LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) plusbutton.image.getLayoutParams();
                ll.gravity = Gravity.CENTER;
                plusbutton.image.setLayoutParams(ll);
                plusbutton.view.setLayoutParams(new LinearLayout.LayoutParams((int) (98*density), (int) (34*density)));
//                border.setStroke(2, context.getColor(R.color.gray)); // gray border
                plusbutton.view.setBackground(border);
                plusbutton.view.setTag(g);
                plusbutton.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firePopupWindow(v);
                    }
                });
                break;
            default:
                GroupHolder button = (GroupHolder) holder;
                button.title.setTypeface(0);
                button.title.setLetterSpacing((float)0.08);
                button.title.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                button.title.setText(g.getTitle());
                button.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                LayoutParams rll = (LayoutParams) button.title.getLayoutParams();
                rll.setMargins((int) (-2*density), (int) (3*density), 0, 0);
                button.title.setLayoutParams(rll);
                button.view.setLayoutParams(new LayoutParams((int) (98*density), (int) (34*density)));
//                border.setStroke(3, context.getColor(R.color.gray)); // gray border
                button.view.setBackground(context.getDrawable(R.drawable.group_background));
                button.view.setTag(g);
                button.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!addGroupActive && !addModifierActive && !addGroupActiveFixed && !addModifierActiveFixed)
                            firePopupWindow(v);
                        else if(!addModifierActive && ! addGroupActiveFixed && !addModifierActiveFixed)
                            addGroupToProduct(v);
                        else if(addGroupActiveFixed && !addModifierActiveFixed)
                            addGroupToProductFixed(v);
                        else
                            ((MainActivity) context).openModifiersView(g.getID());
                    }
                });

        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        boolean f = true;
        if(toPosition == groups.size()-1) f = false;
        else {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(groups, i, i + 1);
                }
                if(!StaticValue.blackbox) {
                    for (int i = fromPosition; i <= toPosition; i++) {
                        // apply changes in database
                        dbA.execOnDb("UPDATE modifiers_group SET position = " + i + " WHERE id = " + groups.get(i).getID());
                    }
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(groups, i, i - 1);
                }
                if(!StaticValue.blackbox) {
                    for (int i = fromPosition; i >= toPosition; i--) {
                        // apply changes in database
                        dbA.execOnDb("UPDATE modifiers_group SET position = " + i + " WHERE id = " + groups.get(i).getID());
                    }
                }
            }
        }
        if(f){
            notifyItemMoved(fromPosition, toPosition);
        }
        return true;
    }

    public void swapModifierGroupFunction(int fromPosition, int toPosition){
        for(int i=0; i<groups.size(); i++){
            dbA.execOnDb("UPDATE modifiers_group SET position = " + (i+1) + " WHERE id = " + groups.get(i).getID());
        }
        dbA.showData("modifiers_group");

    }

    @Override
    public void onItemDismiss(int position) {
        if(StaticValue.blackbox) {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("toPosition", String.valueOf(position+1)));
            params.add(new BasicNameValuePair("groupsSize", String.valueOf(groups.size() - 1)));
            params.add(new BasicNameValuePair("groupID", String.valueOf(groups.get(position).getID())));
            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            params.add(new BasicNameValuePair("androidId", android_id));
            ((MainActivity) context).callHttpHandler("/moveModifierGroup", params);
        }

    }

    /** HOLDERS **/
    public static class GroupHolder extends ViewHolder{
        public View view;
        public CustomTextView title;
        public GroupHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (CustomTextView)view.findViewById(R.id.title);
        }
        @Override
        public String toString(){ return "GroupHolder, Title: "+title.getText().toString();}
    }

    public static class PlusButtonHolder extends ViewHolder{
        public View view;
        public ImageView image;
        public PlusButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            image = (ImageView)view.findViewById(R.id.button_img);
        }
        @Override
        public String toString(){ return "PlusButton";}
    }

    public View getItemFromId(int id){
        ArrayList<ModifiersGroup> array =
                dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id =" + id + "");
        if(array.size()!=0)
            for(int i = 0; i< parent.getChildCount(); i++){
                View v = parent.getChildAt(i);
                ModifiersGroup g = (ModifiersGroup)v.getTag();
                for(int j=0; j< array.size(); j++)
                    if(g.getID() == array.get(j).getID()) {
                        return v;
                    }
            }
        return null;
    }

    /**
     * these three methods set boolean values to correctly switch between addGroup and addModifier buttons
     */
    public void setAddGroupToProduct(){
        addModifierActive = false;
        addModifierActiveFixed = false;
        addGroupActiveFixed = false;
    }

    public void setAddGroupToProductFixed(){
        addGroupActiveFixed = true;
        addModifierActiveFixed = false;
    }

    public void setFireGroupPopupWindow(){
        addGroupActive = false;
        addModifierActive = false;
        addGroupActiveFixed = false;
        addModifierActiveFixed = false;
    }

    /**
     * these three methods set boolean values to correctly switch between addGroup and addModifier buttons
     * still need to complete set
     */

    public void setAddModifierToProduct(){
        if(childAdapter != null)
            childAdapter.setAddModifierActive(true);
        setAddModifierActive(true);
    }

    public void setAddModifierToProductFixed(){
        if(childAdapter != null)
            childAdapter.setAddModifierActiveFixed(true);
        setAddModifierActiveFixed(true);
    }

    public void setFireModifierPopupWindow(){
        if(childAdapter != null){
            childAdapter.setAddModifierActive(false);
            childAdapter.setAddModifierActiveFixed(false);
        }
        setAddModifierActive(false);
        setAddModifierActiveFixed(false);
    }

    /** Method firing popup window accordingly to which button has been pressed **/
    private void firePopupWindow(View v){
        ModifiersGroup g = (ModifiersGroup)v.getTag();
        View thisButton = v;
        if(g.getID()== -11) popupWindow(thisButton,true);
        else popupWindow(thisButton,false);
    }

    private void popupWindow(View thisButton, final boolean isBeingAdded){
        final ModifiersGroup g = (ModifiersGroup)thisButton.getTag();
        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_modifier_add, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        if(isBeingAdded){
            //nuovo elemento setto check a true
        }else{
            //recupero valore per check
        }

        LayoutParams rlp = (LayoutParams)popupView.findViewById(R.id.ModifierGroupSetup)
                .getLayoutParams();
        int t = (int)(dpHeight-52)/2 - rlp.height/2;
        rlp.topMargin = t;
        popupView.findViewById(R.id.ModifierGroupSetup).setLayoutParams(rlp);
        final EditText title = (EditText)popupView.findViewById(R.id.groupTitleInsert);
        popupView.findViewById(R.id.ModifierGroupSetup).setVisibility(VISIBLE);
        if(!isBeingAdded) title.setText(g.getTitle());
        checknotes = dbA.fetchNotesFromModifiersGroup("SELECT notes FROM modifiers_group WHERE id=" + g.getID());
        if(checknotes)
            popupView.findViewById(R.id.notesInputCheckBox).setActivated(true);
        else
            popupView.findViewById(R.id.notesInputCheckBox).setActivated(false);
        int groupID = g.getID();
        setupDismissKeyboard(popupView);

        ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }});

        ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                if(popupView.findViewById(R.id.notesInputCheckBox).isActivated())
                    checknotes = true;
                else
                    checknotes = false;
                int intChecknotes = checknotes ? 1 : 0;
                g.setNotes(intChecknotes);
                String t = title.getText().toString();
                if(!t.equals("")) {
                    /** CASE: popupWindow fired from plus_button **/
                    if(StaticValue.blackbox){
                        if (isBeingAdded) {
                            if (number_of_groups < MAX_NUMBER_OF_GROUPS) {
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'") ));
                                params.add(new BasicNameValuePair("position", String.valueOf(groups.size()-1)));
                                params.add(new BasicNameValuePair("notes", String.valueOf(checknotes ? 1 : 0)));
                                params.add(new BasicNameValuePair("open", String.valueOf(0)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));

                                myGroupId = groupID;
                                myPopupWindow = popupWindow;
                                myPopupView= popupView;
                                ((MainActivity) context).callHttpHandler("/insertModifierGroup", params);
                            }

                        }else{
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'") ));
                            params.add(new BasicNameValuePair("position", String.valueOf(groups.size()-1)));
                            params.add(new BasicNameValuePair("notes", String.valueOf(checknotes ? 1 : 0)));
                            params.add(new BasicNameValuePair("id", String.valueOf(g.getID())));
                            params.add(new BasicNameValuePair("open", String.valueOf(0)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myGroupId = groupID;
                            myPopupWindow = popupWindow;
                            myPopupView= popupView;
                            ((MainActivity) context).callHttpHandler("/updateModifierGroup", params);
                        }
                    }else {
                        if (isBeingAdded) {
                            if (number_of_groups < MAX_NUMBER_OF_GROUPS) {
                                dbA.execOnDb("INSERT INTO modifiers_group (title, position, notes) " +
                                        "VALUES(\"" + t.replaceAll("'", "\'") + "\"," + (groups.size() - 1) + ", "
                                        + (checknotes ? 1 : 0) + ");");
                                getCurrentModifiersGroupSet();
                                if (selectedGroup != null) {
                                    if (groupID != selectedGroup.getID()) {
                                        if (childAdapter != null) {
                                            ((MainActivity) context).closeModifiersView();
                                            turnGroupModifiersOFF();
                                        }
                                    } else {
                                        if (childAdapter != null)
                                            childAdapter.setNotesContainer(checknotes);
                                        turnGroupModifiersOFF();
                                        ((MainActivity) context).openModifiersView(groupID);
                                    }
                                }
                                if (number_of_groups == MAX_NUMBER_OF_GROUPS)
                                    Toast.makeText(context, R.string.max_number_of_groups_reached, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            dbA.execOnDb("UPDATE modifiers_group SET title = \"" + t + "\" , notes = " +
                                    (checknotes ? 1 : 0) + " WHERE id =" + g.getID() + ";");
                            getCurrentModifiersGroupSet();
                            if (selectedGroup != null) {
                                if (groupID != selectedGroup.getID()) {
                                    if (childAdapter != null) {
                                        ((MainActivity) context).closeModifiersView();
                                        turnGroupModifiersOFF();
                                    }
                                } else {
                                    if (childAdapter != null)
                                        childAdapter.setNotesContainer(checknotes);
                                    turnGroupModifiersOFF();
                                    ((MainActivity) context).openModifiersView(groupID);
                                }
                            }
                        }
                        popupWindow.dismiss();
                    }
                }
                else{
                    Toast toast = Toast.makeText(context,R.string.insert_title, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        // MODIFIERS button
        CustomButton openModifiers = (CustomButton)popupView.findViewById(R.id.openModifiers);
        openModifiers.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {
                /**
                 * If is being added then first adds to db, refreshes current adapter's button array
                 * and THEN opens modifiers by calling the other adapter's correct method
                **/
                if(popupView.findViewById(R.id.notesInputCheckBox).isActivated())
                    checknotes = true;
                else
                    checknotes = false;
                //dbA.showData("modifiers_group");
                int groupID = g.getID();
                int intChecknotes = checknotes ? 1 : 0;
                g.setNotes(intChecknotes);
                String t = title.getText().toString();
                if(!t.equals("")) {
                    if(StaticValue.blackbox){
                        if (isBeingAdded) {
                            if (number_of_groups < MAX_NUMBER_OF_GROUPS) {
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'") ));
                                params.add(new BasicNameValuePair("position", String.valueOf(groups.size()-1)));
                                params.add(new BasicNameValuePair("notes", String.valueOf(checknotes ? 1 : 0)));
                                params.add(new BasicNameValuePair("open", String.valueOf(1)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));

                                myGroupId = groupID;
                                myPopupWindow = popupWindow;
                                myPopupView= popupView;
                                ((MainActivity) context).callHttpHandler("/insertModifierGroup", params);
                            }else
                                Toast.makeText(context, R.string.max_number_of_groups_reached, Toast.LENGTH_SHORT).show();
                        }else{
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'") ));
                            params.add(new BasicNameValuePair("position", String.valueOf(groups.size()-1)));
                            params.add(new BasicNameValuePair("notes", String.valueOf(checknotes ? 1 : 0)));
                            params.add(new BasicNameValuePair("id", String.valueOf(g.getID())));
                            params.add(new BasicNameValuePair("open", String.valueOf(1)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myGroupId = groupID;
                            myPopupWindow = popupWindow;
                            myPopupView= popupView;
                            ((MainActivity) context).callHttpHandler("/updateModifierGroup", params);
                        }
                    }else {
                        if (isBeingAdded) {
                            if (number_of_groups < MAX_NUMBER_OF_GROUPS) {
                                dbA.execOnDb("INSERT INTO modifiers_group (title, position, notes) " +
                                        "VALUES(\"" + t.replaceAll("'", "\'") + "\"," + (groups.size() - 1) + ", "
                                        + (checknotes ? 1 : 0) + ");");
                                getCurrentModifiersGroupSet();
                                if (childAdapter != null) {
                                    childAdapter.notifyDataSetChanged();
                                    if (((MainActivity) context).wereModifiersOpen)
                                        childAdapter.setNotesContainer(checknotes);
                                }
                                selectedGroup = g;
                                Cursor c = dbA.fetchByQuery("SELECT * FROM modifiers_group ORDER BY id DESC");
                                while (c.moveToNext()) {
                                    groupID = c.getInt(c.getColumnIndex(DatabaseAdapter.KEY_ID));
                                    break;
                                }
                                //open modifiers
                                turnGroupModifiersOFF();
                                ((MainActivity) context).openModifiersView(groupID);
                                popupWindow.dismiss();
                            } else
                                Toast.makeText(context, R.string.max_number_of_groups_reached, Toast.LENGTH_SHORT).show();
                        } else {
                            t = title.getText().toString();
                            dbA.execOnDb("UPDATE modifiers_group SET title = \"" + t.replaceAll("'", "\'") +
                                    "\" , notes = " + (checknotes ? 1 : 0) + " WHERE id = " + g.getID());
                            getCurrentModifiersGroupSet();
                            if (childAdapter != null) {
                                childAdapter.notifyDataSetChanged();
                                if (((MainActivity) context).wereModifiersOpen)
                                    childAdapter.setNotesContainer(checknotes);
                            }
                            selectedGroup = g;
                            //open modifiers
                            turnGroupModifiersOFF();
                            ((MainActivity) context).openModifiersView(groupID);
                            popupWindow.dismiss();
                        }
                    }
                }
                else Toast.makeText(context,R.string.insert_title, Toast.LENGTH_SHORT).show();
            }
        });
        CustomButton deleteGroup = (CustomButton)popupView.findViewById(R.id.deleteGroup);
        deleteGroup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBeingAdded) Toast.makeText(context,R.string.group_has_not_been_created_yet,Toast.LENGTH_SHORT).show();
                else{
                    final View dialogView = inflater.inflate(R.layout.yes_no_dialog, null);
                    final PopupWindow popupDialog = new PopupWindow(
                            dialogView,
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT);
                    ((CustomTextView) dialogView.findViewById(R.id.delete_window))
                            .setText(resources.getString(R.string.you_are_deleting__group, g.getTitle().toUpperCase()));
                    ((CustomButton) dialogView.findViewById(R.id.delete_button))
                            .setText(R.string.delete_group);
                    dialogView.findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(StaticValue.blackbox){
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("id", String.valueOf(g.getID())));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));

                                myPopupWindow = popupWindow;
                                myPopupView= popupView;
                                myPopupDialog = popupDialog;
                                ((MainActivity) context).callHttpHandler("/deleteModifierGroup", params);
                            }else {
                                dbA.deleteModifierFromTableByID("modifiers_group", g.getID());
                                getCurrentModifiersGroupSet();
                                turnGroupModifiersOFF();
                                ((MainActivity) context).closeModifiersView();
                                ((MainActivity) context).findViewById(R.id.modifier_notes_container).setVisibility(GONE);
                                popupWindow.dismiss();
                                popupDialog.dismiss();
                            }
                        }
                    });
                    dialogView.findViewById(R.id.cancel_button).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupDialog.dismiss();
                        }
                    });
                    popupDialog.setFocusable(true);
                    popupDialog.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
                }
            }
        });

        ImageButton checkbox = (ImageButton)popupView.findViewById(R.id.notesInputCheckBox);
        checkbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checknotes){
                    //check notes is x
                    checknotes = false;
                    checkbox.setActivated(!checkbox.isActivated());
                    if(childAdapter != null)
                        childAdapter.notifyDataSetChanged();
                    int intChecknotes = checknotes ? 1 : 0;
                    g.setNotes(intChecknotes);

                }
                else{
                    //check notes is off
                    checknotes = true;
                    checkbox.setActivated(!checkbox.isActivated());
                    if(childAdapter != null)
                        childAdapter.notifyDataSetChanged();
                    int intChecknotes = checknotes ? 1 : 0;
                    g.setNotes(intChecknotes);
                }
            }
        });
        /*setupDismissKeyboard(popupView);*/
        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    public void getCurrentModifiersGroupSet(){
        groups = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group ORDER BY position");
        number_of_groups = groups.size();
        if(number_of_groups < MAX_NUMBER_OF_GROUPS) {
            ModifiersGroup plus_button = new ModifiersGroup();
            plus_button.setID(-11);
            plus_button.setPosition(groups.size());
            groups.add(plus_button);
        }
        notifyDataSetChanged();
    }

    /**
     *  Switches 'add group to product' mode on/off, which means that groups
     *  are lit up if they where previously assigned to that particular group
     */
    public boolean switchAddGroupActive(boolean active){
        int childCount = parent.getChildCount();
        if(!active){
            addGroupActive = false;
            // turns off the modifiers groups
            for(int i = 0; i< childCount; i++){
                View v = parent.getChildAt(i);
                v.setActivated(false);
            }
        }
        else{
            addGroupActive = true;
            // closes modifiers section as they are not intended to be added through this method
            ((Activity)context).findViewById(R.id.modifiers_tv).setVisibility(INVISIBLE);
            ((Activity)context).findViewById(R.id.modifiers_tv_hline).setVisibility(INVISIBLE);
            ((Activity)context).findViewById(R.id.modifiersRecyclerView).setVisibility(INVISIBLE);
            //((MainActivity)context).wereModifiersOpen = false;

            // turns ON th modifiers groups which were active on the current product
            ArrayList<ModifiersGroup> array =
                    dbA.fetchModifiersGroupByQuery("SELECT * " +
                            "FROM modifiers_group " +
                            "WHERE id IN    (SELECT group_id " +
                                            "FROM modifiers_group_assigned " +
                                            "WHERE prod_id ="+ current_product.getID()+
                                            " AND fixed = 0" +
                                            " AND all_the_group = 1 )" +
                            "ORDER BY position");
            if(array!=null) {
                if (array.size() != 0)
                    for (int i = 0; i < childCount; i++) {
                        View v = parent.getChildAt(i);
                        ModifiersGroup g = (ModifiersGroup) v.getTag();
                        for (int j = 0; j < array.size(); j++)
                            if (g.getID() == array.get(j).getID()) {
                                v.setActivated(true);
                                break;
                            }
                    }
            }
        }
        return addGroupActive;
    }


    public boolean switchAddGroupActiveFixed(boolean active){
      /*  dbA.showData("modifiers_group_assigned");*/
        int childCount = parent.getChildCount();
        if(!active){
            addGroupActiveFixed = false;
            // turns off the modifiers groups
            for(int i = 0; i< childCount; i++){
                View v = parent.getChildAt(i);
                v.setActivated(false);
            }
        }
        else{
            addGroupActiveFixed = true;
            // closes modifiers section as they are not intended to be added through this method
            ((Activity)context).findViewById(R.id.modifiers_tv).setVisibility(INVISIBLE);
            ((Activity)context).findViewById(R.id.modifiers_tv_hline).setVisibility(INVISIBLE);
            ((Activity)context).findViewById(R.id.modifiersRecyclerView).setVisibility(INVISIBLE);
            //((MainActivity)context).wereModifiersOpen = false;

            // turns ON th modifiers groups which were active on the current product
            ArrayList<ModifiersGroup> array =
                    dbA.fetchModifiersGroupByQuery("SELECT * " +
                            "FROM modifiers_group " +
                            "WHERE id IN    (SELECT group_id " +
                            "FROM modifiers_group_assigned " +
                            "WHERE prod_id ="+ current_product.getID()+
                            " AND fixed = 1)" +
                            "ORDER BY position");
            if(array.size()!=0)
                for(int i = 0; i< childCount; i++){
                    View v = parent.getChildAt(i);
                    ModifiersGroup g = (ModifiersGroup)v.getTag();
                    for(int j=0; j< array.size(); j++)
                        if(g.getID() == array.get(j).getID()) {
                            v.setActivated(true);
                            break;
                        }
                }
        }
        return addGroupActiveFixed;
    }

    /**
     * If group gets clicked while in ' add group to product' mode this method gets triggered
     * and group is attached to product in db
     */
    private void addGroupToProduct(View v){
        ModifiersGroup g = (ModifiersGroup)v.getTag();
        // If view is active, sets it to Inactive and deletes row from db
        if(v.isActivated()){
            if(StaticValue.blackbox){
                //v.setActivated(false);
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("groupId", String.valueOf(g.getID())));
                params.add(new BasicNameValuePair("productId", String.valueOf(current_product.getID())));
                params.add(new BasicNameValuePair("fixed", String.valueOf(0)));
                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                params.add(new BasicNameValuePair("androidId", android_id));

                myPopupView = v;
                ((MainActivity) context).callHttpHandler("/deleteFromModifierGroupAssigned", params);
            }else {
                v.setActivated(false);
                dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + g.getID() + " AND prod_id=" + current_product.getID());
            }
        }
        else{
            if(StaticValue.blackbox) {
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("groupId", String.valueOf(g.getID())));
                params.add(new BasicNameValuePair("productId", String.valueOf(current_product.getID())));
                params.add(new BasicNameValuePair("fixed", String.valueOf(0)));
                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                params.add(new BasicNameValuePair("androidId", android_id));


                myPopupView = v;
                ((MainActivity) context).callHttpHandler("/insertInModifierGroupAssigned", params);

            }else{
                v.setActivated(true);
                dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + g.getID() + " AND prod_id=" + current_product.getID());
                dbA.execOnDb("INSERT INTO modifiers_group_assigned(prod_id, group_id, all_the_group, fixed) " +
                    "VALUES(" + current_product.getID() + "," + g.getID() + ",1, 0);");
            }
        }
    }

    private void addGroupToProductFixed(View v){
        ModifiersGroup g = (ModifiersGroup)v.getTag();
        // If view is active, sets it to Inactive and deletes row from db

        if(v.isActivated()){
            if(StaticValue.blackbox){
                //v.setActivated(false);
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("groupId", String.valueOf(g.getID())));
                params.add(new BasicNameValuePair("productId", String.valueOf(current_product.getID())));
                params.add(new BasicNameValuePair("fixed", String.valueOf(1)));
                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                params.add(new BasicNameValuePair("androidId", android_id));

                myPopupView = v;
                ((MainActivity) context).callHttpHandler("/deleteFromModifierGroupAssigned", params);
            }else {
                v.setActivated(false);
                dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + g.getID() + " AND prod_id=" + current_product.getID());
            }

        }
        else{
            if(StaticValue.blackbox) {
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("groupId", String.valueOf(g.getID())));
                params.add(new BasicNameValuePair("productId", String.valueOf(current_product.getID())));
                params.add(new BasicNameValuePair("fixed", String.valueOf(1)));
                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                params.add(new BasicNameValuePair("androidId", android_id));


                myPopupView = v;
                ((MainActivity) context).callHttpHandler("/insertInModifierGroupAssigned", params);

            }else{
                v.setActivated(true);
                dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + g.getID() + " AND prod_id=" + current_product.getID());
                dbA.execOnDb("INSERT INTO modifiers_group_assigned(prod_id, group_id, all_the_group, fixed) " +
                        "VALUES(" + current_product.getID() + "," + g.getID() + ",1, 0);");
            }

        }
    }

    private void turnOnActivatedGroups(){
       /* dbA.showData("modifiers_group_assigned");*/
        int childCount = parent.getChildCount()-1;
        Cursor c = dbA.fetchByQuery("SELECT DISTINCT group_id FROM modifiers_group_assigned " +
                "LEFT JOIN modifiers_assigned ON modifiers_group_assigned.id=modifiers_assigned.assignment_id" +
                " WHERE prod_id ="+ current_product.getID() + " AND modifiers_assigned.fixed=0" );

/*
        Cursor c = dbA.fetchByQuery("SELECT DISTINCT group_id FROM modifiers_group_assigned WHERE prod_id ="+ current_product.getID()+ " AND fixed=0");
*/
        if (c != null) {
            while (c.moveToNext()) {
                for (int i = 0; i < childCount; i++) {
                    View v = parent.getChildAt(i);
                    ModifiersGroup g = (ModifiersGroup) v.getTag();
                    // todo: if group partially added change background someway
                    if (g.getID() == c.getInt(c.getColumnIndex("group_id"))) {
                        v.setActivated(true);
                        break;
                    }
                }
            }
        }
    }

    private void turnOnActivatedGroupsFixed(){
        int childCount = parent.getChildCount()-1;
        Cursor c = dbA.fetchByQuery("SELECT DISTINCT group_id FROM modifiers_group_assigned " +
                "LEFT JOIN modifiers_assigned ON modifiers_group_assigned.id=modifiers_assigned.assignment_id" +
                " WHERE prod_id ="+ current_product.getID() + " AND modifiers_assigned.fixed=1" );

        //Cursor c = dbA.fetchByQuery("SELECT DISTINCT group_id FROM modifiers_group_assigned WHERE prod_id ="+ current_product.getID() + " AND fixed=0" );
        if (c != null) {
            while (c.moveToNext()) {
                for (int i = 0; i < childCount; i++) {
                    View v = parent.getChildAt(i);
                    ModifiersGroup g = (ModifiersGroup) v.getTag();
                    // todo: if group partially added change background someway
                    if (g.getID() == c.getInt(c.getColumnIndex("group_id"))) {
                        v.setActivated(true);
                        break;
                    }

                }
            }
        }
    }

    public void turnGroupModifiersOFF(){
        int childCount = parent.getChildCount()-1;
        for(int i=0; i<childCount; i++)
            parent.getChildAt(i).setActivated(false);
    }

    /**
     *
     * Methods used during the add of single modifiers contained in group (groupID)
     *
     */
    public void setChildAdapter(ModifierAdapter child){
        childAdapter = child;
    }

    public boolean switchAddModifierActive(){
        addModifierActive = !addModifierActive;
        if(addModifierActive) turnOnActivatedGroups();
        else switchAddGroupActive(false);
        return addModifierActive;
    }

    public boolean switchAddModifierActiveFixed(){
        addModifierActiveFixed = !addModifierActiveFixed;
        if(addModifierActiveFixed) turnOnActivatedGroupsFixed();
        else switchAddGroupActiveFixed(false);
        return addModifierActiveFixed;
    }



    /**
     * @param groupID gets attached to product, but with the boolean "all_the_group" = 0 (false) in db
     */
    public void addPartialGroupToProduct(int groupID){
        int childCount = parent.getChildCount();
        View v;
        ModifiersGroup g;
        if(!StaticValue.blackbox) {
            for (int i = 0; i < childCount; i++) {
                v = parent.getChildAt(i);
                g = (ModifiersGroup) v.getTag();
                if (g.getID() == groupID) {
                    dbA.execOnDb("INSERT INTO modifiers_group_assigned(prod_id, group_id, all_the_group, fixed) " +
                            "VALUES(" + current_product.getID() + "," + g.getID() + ",0, 0);");
                    break;
                }
            }
        }
    }

    public View getChildAt(int i){
        return parent.getChildAt(i);
    }

    public static class ModifiersGroup{
        private String title;
        private int id;
        private int position;
        private boolean all_the_group;
        private int notes;

        public ModifiersGroup(){}
        public ModifiersGroup(String s, int id){
            title = s;
            this.id = id;
        }

        public void setTitle(String s){title = s;}
        public void setID(int id){this.id = id;}
        public void setPosition(int pos){position = pos;}
        public void setAll_the_group(boolean b){all_the_group = b;}
        public void setNotes(int n){
            notes = n;
        }

        public String getTitle(){return title;}
        public int getID(){return id;}
        public int getPosition(){return position;}
        public boolean getAll_the_group(){return all_the_group;}
        public int getNotes(){
            return notes;
        }


        public static ModifiersGroup fromJson(JSONObject jsonObject) {
            ModifiersGroup modifier = new ModifiersGroup();
            // Deserialize json into object fields
            try {
                modifier.id = jsonObject.getInt("id");
                modifier.title= jsonObject.getString("title");
                modifier.position = jsonObject.getInt("position");
                if(jsonObject.has("allTheGroup"))
                    modifier.all_the_group= jsonObject.getBoolean("allTheGroup");
                else
                    modifier.all_the_group=false;
                modifier.notes= jsonObject.getInt("notes");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            // Return new object
            return modifier;
        }
        public static ArrayList<ModifiersGroup> fromJsonArray(JSONArray jsonObject) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ModifiersGroup>>(){}.getType();
            ArrayList<ModifiersGroup> list = gson.fromJson(jsonObject.toString(), type);
            return list;
        }
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
            view.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if(!(((Activity)context).getCurrentFocus() instanceof EditText) && !keyboard_next_flag){
                            Log.d("OnFocusChange", "You clicked out of an Edit Text!");
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
