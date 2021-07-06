package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.Operative;
import com.example.blackbox.fragments.ActivityCommunicator;
import com.example.blackbox.fragments.ModifierFragment;
import com.example.blackbox.fragments.OperativeFragment;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.VibrationClass;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;
import static android.view.View.TEXT_ALIGNMENT_CENTER;

/**
 * Created by tiziano on 15/06/17.
 */

public class OModifierGroupAdapter extends Adapter<ViewHolder>  {

    public ArrayList<OModifiersGroup> modifiers;
    private static DatabaseAdapter dbA;
    private Context context;
    private OperativeFragment of;
    private RecyclerView parent;


    LayoutInflater inflater;

    private float density;
    private float dpHeight;
    private float dpWidth;

    private ActivityCommunicator activityCommunicator;

    private Integer catId = 0;

    private Integer groupId = -11;

    public OModifierGroupAdapter(Context c, DatabaseAdapter dbA,  Integer catId, ArrayList<OModifiersGroup> modifiersGroup, RecyclerView parent, Integer groupId) {
        this.context = c;
        this.dbA = dbA;
        this.catId = catId;
        this.parent = parent;
        this.groupId = groupId;
        //this.modifiers = new ArrayList<OModifiersGroup>();
        //this.modifiers = modifiersGroup;
        //getCurrentModifiersGroupSet(catId);
        // TODO : modifiers not working with second query but working with first, why????
        modifiers = dbA.fetchOperativeModifiersGroupByQuery("SELECT modifiers_group.id, modifiers_group.title, modifiers_group.position, modifiers_group.notes FROM modifiers_group LEFT JOIN modifiers_group_assigned ON modifiers_group.id=modifiers_group_assigned.group_id WHERE modifiers_group_assigned.prod_id="+ catId+" ORDER BY modifiers_group.position");
       // modifiers = dbA.fetchOperativeModifiersGroupByQuery("SELECT modifiers_group.id, modifiers_group.title, modifiers_group.position, modifiers_group_assigned.all_the_group  FROM modifiers_group LEFT JOIN modifiers_group_assigned ON modifiers_group.id=modifiers_group_assigned.group_id WHERE modifiers_group_assigned.prod_id="+ catId+" ORDER BY modifiers_group.position");
       // modifiers = dbA.fetchOperativeModifiersGroupByQuery("SELECT * FROM modifiers_group ORDER BY position");
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth = outMetrics.widthPixels;// / density;

        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFD3D3D3); //black background ->todo set background color accordingly to db data
        border.setStroke((int) (3*density), 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8*density);


        View modyBack = (((Activity) context).findViewById(R.id.modifier_backButton));
        modyBack.setBackground(border);

        final Integer cId = catId;
        modyBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ModifierFragment.getModify()) {

                    } else {
                        if(StaticValue.showProducts || StaticValue.showFavourites){
                            ((Operative) context).goToMainPage();
                        }else {

                            ((Operative) context).getBackToButtons("cazzi", cId);
                        }
                    }
                }
            });







    }

    public void updateModifierGroups(ArrayList<OModifiersGroup> newGroups){
        modifiers = newGroups;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.element_gridview_subelement, null);
        v.findViewById(R.id.subtitle).setVisibility(GONE);
        ViewHolder vh = new GroupHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final OModifiersGroup g = modifiers.get(position);
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFb1b0b0);//black background ->todo set background color accordingly to db data
        border.setStroke((int) (2*density), 0xFFd3d3d3); //gray border with full opacity
        GroupHolder button = (GroupHolder) viewHolder;
        button.title.setTypeface(0);
        button.title.setLetterSpacing((float)0.08);
        button.title.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        button.title.setText(g.getTitle());
        button.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) button.title.getLayoutParams();
        rll.setMargins((int) (-2*density), (int) (3*density), 0, 0);
        button.title.setLayoutParams(rll);
        button.view.setLayoutParams(new RelativeLayout.LayoutParams((int) (98*density), (int) (34*density)));
//        border.setStroke(3, context.getColor(R.color.gray)); // gray border
        button.view.setBackground(context.getDrawable(R.drawable.group_background));
        button.view.setTag(g);
        if(g.getID()==groupId){
            VibrationClass.vibeOn(context);
            button.view.setActivated(true);
            ModifierFragment.showModifiers(g.getID(), catId, g.getTitle());
        }else{
            button.view.setActivated(false);
        }
        button.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VibrationClass.vibeOn(context);
                ArrayList<OModifierAdapter.OModifier> myModifiers =
                        dbA.fetchOModifiersByQuery("SELECT * FROM modifiers_assigned WHERE assignment_id=" + g.getID());
                v.setActivated(true);
                ModifierFragment.showModifiers(g.getID(), catId, g.getTitle());
                ModifierFragment.showNotes();
            }
        });

        /**
         * necessary because if you show only one modifiers group it has to sow modifiers, but you can't
         * activate button because it's triggered from modifierFragment.init() that end's before you have your xml tree
         */
        if(modifiers.size()==1){
            button.view.setActivated(true);
        }



    }


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



    @Override
    public int getItemCount() {
        if(modifiers!=null) {
            return modifiers.size();
        }else{
            return 0;
        }
    }


    public void turnOnActivatedGroups(Integer groupId){
        int childCount = parent.getChildCount();

        //aspetta un attimo a farlo

        for(int i = 0; i < childCount; i++){
                View v = parent.getChildAt(i);
                OModifiersGroup g = (OModifiersGroup)v.getTag();
                // todo: if group partially added change background someway
                if(groupId==g.getID()) {
                    v.setActivated(true);
                    this.groupId = groupId;
                }else{
                    v.setActivated(false);
                }

        }
    }



    public static class OModifiersGroup{
        private String title;
        private int id;
        private int position;
        private boolean all_the_group;
        private boolean notes;
        private int fixed;

        public OModifiersGroup(){}
        public OModifiersGroup(String s, int id){
            title = s;
            this.id = id;
        }

        public void setTitle(String s){title = s;}
        public void setID(int id){this.id = id;}
        public void setPosition(int pos){position = pos;}
        public void setAll_the_group(boolean b){all_the_group = b;}
        public void setNotes(boolean b){notes = b;}

        public String getTitle(){return title;}
        public int getID(){return id;}
        public int getPosition(){return position;}
        public boolean getAll_the_group(){return all_the_group;}
        public boolean getNotes(){return notes;}

        public int isFixed() {return fixed;}

        public void setFixed(int fixed) {this.fixed = fixed;}
    }
}
