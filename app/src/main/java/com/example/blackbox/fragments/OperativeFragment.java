package com.example.blackbox.fragments;

/**
 * Created by tiziano on 13/06/17.
 */

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.blackbox.R;
import com.example.blackbox.adapter.OGridAdapter;
import com.example.blackbox.adapter.OModifierGroupAdapter;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.LineSeparator;
import com.example.blackbox.model.ButtonLayout;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.view.View.VISIBLE;

public class OperativeFragment extends Fragment {

    private String TAG = "<OperativeFragment>";

    public DatabaseAdapter dbA;
    private GridLayoutManager op_grid_manager;
    private RecyclerView op_recyclerview;
    public static OGridAdapter op_rv_adapter;
    public static View view;
    public Context context;
    private ActivityCommunicator activityCommunicator;
    private String activityAssignedValue ="";
    private static final String STRING_VALUE ="stringValue";

    public ArrayList<OModifierGroupAdapter.OModifiersGroup> modifiers;

    public Integer categoryId = 0;

    private Boolean isModify;

    public Boolean getIsModify() {
        return isModify;
    }

    public void setIsModify(Boolean b) {
        isModify = b;
    }

    private int groupPosition;

    public int getGroupPosition() {
        return groupPosition;
    }

    public void setGroupPosition(int p) {
        groupPosition = p;
    }

    public OperativeFragment(){}

    public void setCatId(Integer catId){
        categoryId = catId;
    }

    public Integer getCatId(){
        return categoryId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.operative_fragment, container, false);
        view.findViewById(R.id.backButton).setOnLongClickListener(view -> {
            Log.i(TAG, "[onCreateView] backButton click");
            goToPreviousCatFromModifiers(0);
            return true;
        });
        return view;
    }


    public void goToPreviousCatFromModifiers(Integer catId) {
        Log.i(TAG, "MY INFO " + catId + " " + catId + " " + "2");
        op_rv_adapter.goToCategory(catId, catId, "", 2);
    }


    public static void setButtonSet( Integer catID, String categoryTitle){
        op_rv_adapter.notifyDataSetChanged();

        RecyclerView recy = (RecyclerView) view.findViewById(R.id.recyclerView);
        RelativeLayout above_rv = (RelativeLayout) view.findViewById(R.id.above_recyclerView);

        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFd3d3d3); //light-gray background
        border.setStroke(3, 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8);

        if(catID!=0) {
            above_rv.setVisibility(VISIBLE);
            ((CustomTextView) above_rv.findViewById(R.id.categoryTitle)).setText(categoryTitle);
            above_rv.findViewById(R.id.backButton).setBackground(border);
            RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) recy.getLayoutParams();
           // rll.setMargins(0, -9, 0, 0);

        }else{
            above_rv.setVisibility(View.GONE);
            RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) recy.getLayoutParams();
            rll.setMargins(0, 0, 0, 0);

        }
    }


    public static void setBackButton(final Integer currentCatID,final Integer  previousCatID,final String previousCatTitle) {
        RelativeLayout above_rv = (RelativeLayout) view.findViewById(R.id.above_recyclerView);
        above_rv.findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_rv_adapter.goToCategory(currentCatID, previousCatID, previousCatTitle, 1);
            }
        });
    }


    /**
     * Fragment communication part
     */

    //since Fragment is Activity dependent you need Activity context in various cases

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        context = getActivity();
        activityCommunicator = (ActivityCommunicator)context;
    }

    //now on your entire fragment use context rather than getActivity()
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            { activityAssignedValue = savedInstanceState.getString(STRING_VALUE); }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(STRING_VALUE,activityAssignedValue);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();


    }

    public void init() {
            dbA = new DatabaseAdapter(getActivity());
            Integer catId = getCatId();
            Log.i(TAG, "[init] catID: " + catId);
            ButtonLayout actualProduct = dbA.fetchButtonByQuery("SELECT * FROM button WHERE id = " + catId + " AND id!=-30 AND id!=-20");

            op_recyclerview = (RecyclerView) view.findViewById(R.id.recyclerView);
            op_recyclerview.setHasFixedSize(true);
            op_grid_manager = new GridLayoutManager(getActivity(), 4);
            op_grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                @Override
                public int getSpanSize(int position) {
                    return 1;
                }
            });
            op_recyclerview.setLayoutManager(op_grid_manager);
            op_rv_adapter = new OGridAdapter(getActivity(), dbA, actualProduct.getCatID());
            op_recyclerview.addItemDecoration(new LineSeparator(getActivity(), 14));

            op_recyclerview.setAdapter(op_rv_adapter);
    }

    public void setTypeOfFavourites(){
        op_rv_adapter.setButtons();
    }

    public void selectFavourites(ArrayList<ButtonLayout> butt) {
        op_rv_adapter.setFavouritesButton(butt);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //FragmentCommunicator interface implementation


    /**
     * check if products has modifiers and check if all groups are fixed
     * @param categoryId
     * @return
     */
    public Boolean checkIfProductHasModifiers(Integer categoryId){
        ArrayList<OModifierGroupAdapter.OModifiersGroup> modyToCheck = new ArrayList<>();
        modyToCheck = dbA.checkToOpenModifiersGroup(categoryId);
        if (modyToCheck.isEmpty()) {
            return false;
        } else {
            boolean check = false;
            for (int i=0; i<modyToCheck.size(); i++){
                if (modyToCheck.get(i).isFixed()==0)
                    { check = true; }
            }
            return check;
        }
    }


}
