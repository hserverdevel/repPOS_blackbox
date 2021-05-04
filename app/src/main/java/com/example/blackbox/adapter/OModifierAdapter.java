package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.example.blackbox.R;
import com.example.blackbox.activities.MainActivity;
import com.example.blackbox.activities.Operative;
import com.example.blackbox.fragments.ActivityCommunicator;
import com.example.blackbox.fragments.ModifierFragment;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.VibrationClass;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class OModifierAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {
    private Context context;
    private RecyclerView parent;
    private OModifierGroupAdapter groups_rv_adapter;
    private ButtonLayout current_product;
    private ArrayList<OModifier> modifiers;
    private DatabaseAdapter dbA;
    private int groupID;
    private int assignmentID;
    private int nModifiersAttached;
    private float dpHeight;
    private float dpWidth;
    private float density;
    private LayoutInflater inflater;
    private boolean keyboard_next_flag = false;
    private boolean addModifierActive = false;
    private int number_of_modifiers;

    private final int MAX_NUMBER_OF_MODIFIERS = 24;

    private ActivityCommunicator activityCommunicator;

    List<CashButtonListLayout> cashButtonList;

    public OModifierAdapter(Context c, DatabaseAdapter dbA, ButtonLayout current_product,
                            int groupID, OModifierGroupAdapter groups_rv_adapter,  List<CashButtonListLayout> listOfValues) {

        context = c;
        this.dbA  = dbA;
        this.current_product = current_product;
        this.groupID = groupID;
        modifiers = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE groupID = "+groupID+" ORDER BY position");
        notifyDataSetChanged();
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        cashButtonList =listOfValues;

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
    }



    public void setCashButtonList(List<CashButtonListLayout> list) {
        cashButtonList = list;
    }
    public void resetCashButtonList() {
        cashButtonList = new ArrayList<CashButtonListLayout>();
    }

    public void emptyModifiersList(){
        modifiers = new ArrayList<OModifier>();
    }

    public boolean showModifiers(Integer groupId, Integer catId) {
        boolean isEmpty = true;
        ButtonLayout a = dbA.fetchButtonByQuery("select * from button where id=" + catId);
        //dbA.showData("modifiers_group");
        //dbA.showData("modifiers_group_assigned");
        OModifierGroupAdapter.OModifiersGroup b = dbA.fetchSingleModifiersGroupByQuery("select * from modifiers_group where id=" + groupId);
        int assignementId = dbA.getSingleAssignmentID(groupId, catId);
        if(assignementId!=-2 && assignementId!=-1) {
            modifiers = dbA.fetchOModifiersByQuery("SELECT * " +
                    "FROM modifier " +
                    "WHERE id IN (SELECT modifier_id " +
                    "FROM modifiers_assigned " +
                    "WHERE assignment_id = " + assignementId +
                    ")" +
                    "ORDER BY position");
        }else{
           modifiers = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE groupID = "+groupId+" ORDER BY position");
        }
        RecyclerView recyclerView = (RecyclerView) ((Activity) context).findViewById(R.id.modifiersRecyclerView);
        if(!modifiers.isEmpty() && recyclerView!=null) {
            recyclerView.setVisibility(VISIBLE);
            notifyDataSetChanged();
            isEmpty = false;
            //   ((Activity) context).findViewById(R.id.modifiersRecyclerView).setVisibility(View.VISIBLE);
        }
        //modifiers = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE groupID = "+groupId+" ORDER BY position");
        //b.getNotes()
        if(dbA.fetchNotesFromModifiersGroup("SELECT notes FROM modifiers_group WHERE id=" + b.getID())){

            LinearLayout notes = (LinearLayout) ((Activity) context).findViewById(R.id.modifier_notes_container);
            notes.setVisibility(VISIBLE);

            ImageButton addNotes= (ImageButton) ((Activity) context).findViewById(R.id.modifier_notes_input_add);

            addNotes.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCommunicator = (ActivityCommunicator) context;
                    CustomEditText inputNote = (CustomEditText) ((Activity) context).findViewById(R.id.modifier_notes_input);
                    if(inputNote.getText().toString().matches("")){

                    }else{

                        CashButtonListLayout but = new CashButtonListLayout();
                        but.setTitle("nota");
                        but.setModifierId(-15);
                        but.setPrice(0.0f);
                        but.setQuantity(1);
                        but.setNote(inputNote.getText().toString());
                        //cashButtonList.add(but);
                        if(cashButtonList==null) cashButtonList = new ArrayList<>();
                        int position = returnPosition(-15);
                        if(position==-1){
                            //non c'è
                            cashButtonList.add(but);
                            activityCommunicator.passNoteModifierToActivity(but, position, false, cashButtonList);
                        }else{
                            //c'e
                            cashButtonList.get(position).setNote(inputNote.getText().toString());
                            activityCommunicator.passNoteModifierToActivity(but, position, true, cashButtonList);
                        }
                        inputNote.setText("");

                    }


                }


            });

            ImageButton deleteNotes= (ImageButton) ((Activity) context).findViewById(R.id.modifier_notes_input_delete);

            deleteNotes.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    activityCommunicator = (ActivityCommunicator) context;
                    activityCommunicator.deleteNoteFromList();
                    removeNoteFromCashList();

                }


            });

        }else{

            LinearLayout notes = (LinearLayout) ((Activity) context).findViewById(R.id.modifier_notes_container);
            notes.setVisibility(GONE);
        }
        if(cashButtonList==null){
            CustomEditText inputNote = (CustomEditText) ((Activity) context).findViewById(R.id.modifier_notes_input);
            inputNote.setText("");
        }else{
            for(int i=0; i<=cashButtonList.size()-1; i++){
                if(cashButtonList.get(i).getModifierId()==-15){
                    CustomEditText inputNote = (CustomEditText) ((Activity) context).findViewById(R.id.modifier_notes_input);
                    inputNote.setText(cashButtonList.get(i).getNote());
                    break;
                }
            }
        }

        return isEmpty;
    }

    public void removeNoteFromCashList(){
        if (cashButtonList != null) {
            for (int i = 0; i < cashButtonList.size(); i++) {
                if (cashButtonList.get(i).getModifierId() == -15) {
                    cashButtonList.remove(i);
                    break;
                }
            }
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;

        v = inflater.inflate(R.layout.gridview_subelement, null);
        v.findViewById(R.id.subtitle).setVisibility(GONE);
        vh = new GroupHolder(v);

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        if (modifiers.get(position).getID() == -11) return -11;
        else return 1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final OModifier m = modifiers.get(position);
        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFF000000); //black background
        border.setStroke((int) (3*density), 0xFFC6C5C6); //gray border with full opacity

                final GroupHolder button = (GroupHolder) holder;
                button.title.setText(m.getTitle());
                LayoutParams rll = (LayoutParams) button.title.getLayoutParams();
                rll.setMargins(0, (int) (7*density), 0, 0);
                button.title.setLayoutParams(rll);
                button.view.setLayoutParams(new LayoutParams((int) (154*density), (int) (46*density)));
//                border.setStroke(3, context.getColor(R.color.yellow)); // gray border
                button.view.setBackground(context.getDrawable(R.drawable.modifier_background));
                button.view.setTag(m);

                if(ModifierFragment.getModify()) {
                    if(checkIfPresent(m.getID())){
                        button.view.setActivated(true);

                    }else{
                        button.view.setActivated(false);
                    }


                }else{
                    if(checkIfLast(m.getID())){
                        button.view.setActivated(true);

                    }else{
                        button.view.setActivated(false);
                    }
                    //button.view.setActivated(false);

                }

                activityCommunicator = (ActivityCommunicator) context;
                button.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VibrationClass.vibeOn(context);
                         if(ModifierFragment.getModify()) {
                           /* if(checkIfPresent(m.getID())) {
                                //già presente da scrivere il metodo per toglierlo
                                activityCommunicator.passModifierToRemoveToActivity(m, 1, returnPosition(m.getID()));

                            }else {

                                activityCommunicator.passModifierToActivity(m, 1);
                                button.view.setActivated(true);
                            }*/
                             activityCommunicator.passModifierToActivity(m, 1);
                             button.view.setActivated(true);
                        }else {

                            CashButtonListLayout but = new CashButtonListLayout();
                            but.setTitle(m.getTitle());
                            but.setModifierId(m.getID());
                            but.setPrice(m.getPrice());
                            but.setQuantity(1);
                            v.setActivated(true);


                            activityCommunicator.passModifierToActivity(m, 1);
                            addToCashList(but);
                            //button.view.setActivated(true);

                        }


                    }
                });

                button.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(button.view.isActivated()){

                        }
                        if(ModifierFragment.getModify()) {
                            if (checkIfPresent(m.getID())) {
                                activityCommunicator.passModifierToRemoveToActivity(m, 1, returnPosition(m.getID()));
                            }
                        }else {
                            if (checkIfPresent(m.getID())) {
                                //già presente da scrivere il metodo per toglierlo

                                int position = ((Operative) context).setGroupPosition();
                                if (position >= 0) {
                                    ((Operative) context).setMyGroupPosition(position);
                                    ((Operative) context).setCashButtonList(cashButtonList);
                                    activityCommunicator.passModifierToRemoveToActivity(m, 1, returnPosition(m.getID()));
                                }
                            } else {

                            }
                        }
                        return true;
                    }
                });

    }

    public void addToCashList(CashButtonListLayout butt){
        Integer position =0;
        Boolean check = false;
        if(cashButtonList!=null) {
            for (int i = 0; i < cashButtonList.size(); i++) {
                if (cashButtonList.get(i).getModifierId() == butt.getModifierId()) {
                    check = true;
                }
            }
            if (check) {
                cashButtonList.get(position).setQuantity(cashButtonList.get(position).getQuantityInt() + 1);
            } else {
                cashButtonList.add(butt);
            }
        }else{
            cashButtonList = new ArrayList<CashButtonListLayout>();
            cashButtonList.add(butt);
        }


    }




    public Boolean checkIfPresent(Integer id) {
        Boolean check = false;
        if (cashButtonList != null) {
           for (int i = 0; i < cashButtonList.size(); i++) {
                if (cashButtonList.get(i).getModifierId() == id) {
                    check = true;
                }
            }
        }
        return check;
    }

    public Boolean checkIfLast(Integer id) {
        activityCommunicator = (ActivityCommunicator) context;
        ArrayList<CashButtonListLayout> list = activityCommunicator.getLastList();
        Boolean check = false;
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getModifierId() == id) {
                    check = true;
                }
            }
        }
        return check;
    }

    public Integer returnPosition(Integer id) {
        Integer position = -1;
        for(int i=0; i< cashButtonList.size(); i++){
            if(cashButtonList.get(i).getModifierId()==id){
                position = i;

            }
        }
        return position;
    }



    @Override
    public int getItemCount() {
        return modifiers.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        boolean f = true;
        if(toPosition == modifiers.size()-1) f = false;
        else {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(modifiers, i, i + 1);
                }
                for(int i = fromPosition; i <= toPosition; i++){
                    // apply changes in database
                    dbA.execOnDb("UPDATE modifier SET position = "+i+" WHERE id = "+modifiers.get(i).getID());
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(modifiers, i, i - 1);
                }
                for(int i = fromPosition; i >= toPosition; i--){
                    // apply changes in database
                    dbA.execOnDb("UPDATE modifier SET position = "+i+" WHERE id = "+modifiers.get(i).getID());
                }
            }
        }
        if(f){
            notifyItemMoved(fromPosition, toPosition);
        }
        return true;
    }

    @Override
    public void onItemDismiss(int position) {}

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
        parent = recyclerView;
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





    public void getCurrentModifiersSet(){
        modifiers = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE groupID = "+groupID+" ORDER BY position");
        number_of_modifiers = modifiers.size();
        if(number_of_modifiers < MAX_NUMBER_OF_MODIFIERS) {
            OModifier plus_button = new OModifier();
            plus_button.setID(-11);
            plus_button.setPosition(modifiers.size());
            modifiers.add(plus_button);
        }
        notifyDataSetChanged();
    }



    /**
     *
     * Start of methods for Modifiers Selection
     *
     */

    private void turnModifiersON(){
        int childCount = parent.getChildCount()-1;
        ArrayList<OModifier> array =
                dbA.fetchOModifiersByQuery("SELECT * " +
                        "FROM modifier " +
                        "WHERE id IN (SELECT modifier_id " +
                        "FROM modifiers_assigned " +
                        "WHERE assignment_id="+assignmentID+
                        ")" +
                        "ORDER BY position");
        nModifiersAttached = array.size();
        if(array.size()!=0 && ((MainActivity)context).wereModifiersOpen)
            for(int i = 0; i< childCount; i++){
                View v = parent.getChildAt(i);
                OModifier m = (OModifier)v.getTag();
                for(int j=0; j< array.size(); j++)
                    if(m.getID() == array.get(j).getID()) {
                        v.setActivated(true);
                        break;
                    }
            }
    }
    private void turnModifiersOFF(){
        int childCount = parent.getChildCount()-1;
        for(int i=0; i<childCount; i++)
            parent.getChildAt(i).setActivated(false);
    }

    public void emptyCashButtonList(){
        cashButtonList = new ArrayList<>();
    }




    public static class OModifier {
        private  int id;
        private String title;
        private float price;
        private int position;
        private int groupID;
        private String note = "";

        public  OModifier(){
        }

        public OModifier(int id, String title, double price, int position, int groupID){
            this.id = id;
            this.title = title;
            this.price = (float)price;
            this.position = position;
        }

        public int getID(){return id;}
        public String getTitle(){return title;}
        public float getPrice(){return price;}
        public int getPosition(){return position;}
        public int getGroupID(){return groupID;}

        public void setID(int id){this.id = id;}
        public void setTitle(String title){this.title = title;}
        public void setPrice(double price){this.price = (float)price;}
        public void setPosition(int pos){position = pos;}
        public void setGroup(int gID){groupID = gID;}
        public void setNote(String n){note = n;}
    }

}
