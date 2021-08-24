package com.example.blackbox.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.blackbox.R;
import com.example.blackbox.activities.Operative;
import com.example.blackbox.adapter.OModifierAdapter;
import com.example.blackbox.adapter.OModifierGroupAdapter;
import com.example.blackbox.adapter.OModifierLineSeparator;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.OperativeModifiersGroupLineSeparator;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * Created by tiziano on 19/06/17.
 */

public class ModifierFragment extends Fragment
{


    private static final String                STRING_VALUE          = "stringValue";
    public static        OModifierGroupAdapter mga;
    public static        OModifierAdapter      ma;
    public static        View                  view;
    private static Boolean modify = false;
    public               DatabaseAdapter       dbA;
    public               Context               context;
    public  ArrayList<OModifierGroupAdapter.OModifiersGroup> modifiers;
    public Integer categoryId = 0;
    public Integer modifierId = 0;
    public Boolean alreadyShowingModifier = false;
    List<CashButtonListLayout> cashButtonList;
    private              RecyclerView          p_recyclerview;
    private              GridLayoutManager     p_grid_manager;
    private              RecyclerView          m_recyclerview;
    private              GridLayoutManager     m_grid_manager;
    private              ActivityCommunicator  activityCommunicator;
    private              String                activityAssignedValue = "";
    private              String                currentProduct        = "";
    private float                                            density;


    public ModifierFragment()
    {
    }


    public static Boolean getModify()
    {
        return modify;
    }


    public static void setModify(Boolean b)
    {
        modify = b;
    }


    public static void showNotes()
    {
        ((CustomEditText) view.findViewById(R.id.modifier_notes_input)).setText("");
    }


    public static void showModifiers(Integer groupId, Integer catId, String text)
    {
        boolean        isEmpty = ma.showModifiers(groupId, catId);
        View           line    = (View) view.findViewById(R.id.modifiers_tv_hline);
        CustomTextView title   = (CustomTextView) view.findViewById(R.id.modifiers_tv);

        if (!isEmpty)
        {
            line.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            view.findViewById(R.id.modifiersRecyclerView).setVisibility(View.VISIBLE);
        }

        else
        {
            if (line.getVisibility() == VISIBLE)
            {
                line.setVisibility(GONE);
            }
            if (title.getVisibility() == VISIBLE)
            {
                title.setVisibility(GONE);
            }
        }



        ma.notifyDataSetChanged();

        mga.turnOnActivatedGroups(groupId);

    }


    public Boolean getAlreadyShowingModifier()
    {
        return alreadyShowingModifier;
    }


    public void setAlreadyShowingModifier(Boolean b)
    {
        alreadyShowingModifier = b;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.modifiers = new ArrayList<OModifierGroupAdapter.OModifiersGroup>();
        view           = inflater.inflate(R.layout.fragment_modifier, container, false);
        dbA            = new DatabaseAdapter(getActivity());

        density = context.getResources().getDisplayMetrics().density;
        view.findViewById(R.id.modifier_backButton).setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {

                if (!getModify())
                {
                    ((Operative) getContext()).goToMainPage();
                }
                return true;
            }
        });
        return view;

    }


    public void setCurrentProduct(String text)
    {
        currentProduct = text;
    }


    public Integer getCatId()
    {
        return categoryId;
    }


    public void setCatId(Integer catId)
    {
        categoryId = catId;
    }


    public void setModifierId(Integer mId)
    {
        modifierId = mId;
    }


    public void setCashButtonList(List<CashButtonListLayout> clb)
    {
        cashButtonList = clb;
    }


    /**
     * Fragment communication part
     */

    //since Fragment is Activity dependent you need Activity context in various cases
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        this.context         = getActivity();
        activityCommunicator = (ActivityCommunicator) this.context;
    }


    //now on your entire fragment use context rather than getActivity()
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            activityAssignedValue = savedInstanceState.getString(STRING_VALUE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(STRING_VALUE, activityAssignedValue);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (getModify())
        {
            initToModify();
        }
        else
        {
            init();
        }

    }


    public void init()
    {
        dbA = new DatabaseAdapter(getActivity());

        int cId = getCatId();

        p_recyclerview = (RecyclerView) getActivity().findViewById(R.id.omodifiersGroupRecyclerView);
        p_recyclerview.setHasFixedSize(true);
        p_grid_manager = new GridLayoutManager(getActivity(), 6);
        p_grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {

            @Override
            public int getSpanSize(int position)
            {
                return 1;
            }
        });

        modifiers = dbA.fetchOperativeModifiersGroupByQuery("SELECT * FROM modifiers_group LEFT JOIN modifiers_group_assigned ON modifiers_group.id=modifiers_group_assigned.group_id WHERE modifiers_group_assigned.prod_id=" + categoryId + " ORDER BY position");
        p_recyclerview.setLayoutManager(p_grid_manager);
        p_recyclerview.addItemDecoration(new OperativeModifiersGroupLineSeparator(getActivity(), (int) (14)));

        mga = new OModifierGroupAdapter(getActivity(), dbA, cId, modifiers, p_recyclerview, -11);

        p_recyclerview.setAdapter(mga);

        m_recyclerview = (RecyclerView) getActivity().findViewById(R.id.modifiersRecyclerView);
        m_grid_manager = new GridLayoutManager(getActivity(), 4);
        m_grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {
            @Override
            public int getSpanSize(int position)
            {
                return 1;
            }
        });
        m_recyclerview.setLayoutManager(m_grid_manager);
        m_recyclerview.addItemDecoration(new OModifierLineSeparator(getActivity(), (int) (14)));
        ButtonLayout current_product = new ButtonLayout(getActivity());

        ma = new OModifierAdapter(getActivity(), dbA, current_product, cId, mga, cashButtonList);

        m_recyclerview.setAdapter(ma);

        CustomTextView title = (CustomTextView) view.findViewById(R.id.currentProductTitle);
        title.setText(currentProduct);

        ma.resetCashButtonList();
        ArrayList<Integer> groupsInt    = dbA.fetchAssignedGroupModifiersByQuery(cId);
        ArrayList<Integer> modifiersInt = dbA.fetchAssignedModifiersByQuery(cId);
        if (groupsInt != null)
        {
            for (int i = 0; i < groupsInt.size(); i++)
            {
                ArrayList<OModifierAdapter.OModifier> mods = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE groupID=" + groupsInt.get(i));
                for (OModifierAdapter.OModifier m : mods)
                {
                    CashButtonListLayout list = new CashButtonListLayout();
                    list.setTitle(m.getTitle());
                    list.setPrice(m.getPrice());
                    list.setQuantity(1);
                    list.setModifierId(m.getID());
                    list.setID(m.getID());
                    ma.addToCashList(list);
                }
            }
        }
        if (modifiersInt != null)
        {
            for (int i = 0; i < modifiersInt.size(); i++)
            {
                ArrayList<OModifierAdapter.OModifier> mods = dbA.fetchOModifiersByQuery("SELECT * FROM modifier WHERE id=" + modifiersInt.get(i));
                for (OModifierAdapter.OModifier m : mods)
                {
                    CashButtonListLayout list = new CashButtonListLayout();
                    list.setTitle(m.getTitle());
                    list.setPrice(m.getPrice());
                    list.setQuantity(1);
                    list.setModifierId(m.getID());
                    list.setID(m.getID());
                    ma.addToCashList(list);
                }
            }
        }

        if (modifiers.size() == 1)
        {
            int                                   groupId = dbA.fetchModifiersGroupByQueryOne(cId);
            OModifierGroupAdapter.OModifiersGroup single  = dbA.fetchSingleModifiersGroupByQuery("Select * from modifiers_group where id=" + groupId);
            showModifiers(single.getID(), cId, single.getTitle());
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
    }


    public void removeModifierFromCashButtonList(Integer position, OModifierAdapter.OModifier modifier)
    {
        cashButtonList.remove(position);
        if (cashButtonList.size() == 0)
        {
            setModify(false);
            ma.setCashButtonList(new ArrayList<CashButtonListLayout>());
        }
        ma.notifyDataSetChanged();
    }


    public int returnModifyCashList(Integer position, OModifierAdapter.OModifier modifier)
    {
        return cashButtonList.size();
    }


    public void showModifierPageToModify(List<CashButtonListLayout> listOfValues, Integer modifierId, String currentProduct, Integer categoryId)
    {
        cashButtonList = listOfValues;
        setCashButtonList(listOfValues);
        setCatId(categoryId);
        setModify(true);
        setModifierId(modifierId);
    }


    public void showModifierPageToModify2(List<CashButtonListLayout> listOfValues, Integer modifierId, String currentProduct, Integer categoryId)
    {
        cashButtonList = listOfValues;
        setCashButtonList(listOfValues);
        setCatId(categoryId);
        setModify(true);
        setModifierId(modifierId);
        initToModify();
    }


    public void initToModify()
    {
        OModifierAdapter.OModifier            currentModifier      = dbA.fetchSingleOModifiersByQuery("SELECT * FROM modifier WHERE id = " + modifierId);
        OModifierGroupAdapter.OModifiersGroup currentModifierGroup = dbA.fetchSingleModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id = " + currentModifier.getGroupID());

        CustomTextView title = (CustomTextView) view.findViewById(R.id.modifiers_tv);
        title.setText(currentModifierGroup.getTitle());

        ArrayList<OModifierGroupAdapter.OModifiersGroup> newModifiers = dbA.fetchOperativeModifiersGroupByQuery("SELECT modifiers_group.id, modifiers_group.title, modifiers_group.position, modifiers_group.notes FROM modifiers_group LEFT JOIN modifiers_group_assigned ON modifiers_group.id=modifiers_group_assigned.group_id WHERE modifiers_group_assigned.prod_id=" + categoryId + " ORDER BY modifiers_group.position");
        Boolean                                          prova        = getAlreadyShowingModifier();
        if (!getAlreadyShowingModifier())
        {
            p_recyclerview = (RecyclerView) getActivity().findViewById(R.id.omodifiersGroupRecyclerView);
            p_recyclerview.setHasFixedSize(true);
            p_grid_manager = new GridLayoutManager(getActivity(), 6);
            p_grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
            {

                @Override
                public int getSpanSize(int position)
                {
                    return 1;
                }
            });
            p_recyclerview.setLayoutManager(p_grid_manager);
            p_recyclerview.addItemDecoration(new OperativeModifiersGroupLineSeparator(getActivity(), (int) (14 * density)));
        }
        mga = new OModifierGroupAdapter(getActivity(), dbA, getCatId(), newModifiers, p_recyclerview, currentModifierGroup.getID());

        p_recyclerview.setAdapter(mga);

        if (!getAlreadyShowingModifier())
        {
            m_recyclerview = (RecyclerView) getActivity().findViewById(R.id.modifiersRecyclerView);

            m_grid_manager = new GridLayoutManager(getActivity(), 4);
            m_grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
            {
                @Override
                public int getSpanSize(int position)
                {
                    return 1;
                }
            });
            m_recyclerview.setLayoutManager(m_grid_manager);
            m_recyclerview.addItemDecoration(new OModifierLineSeparator(getActivity(), (int) (14 * density)));
        }
        ButtonLayout current_product = new ButtonLayout(getActivity());
        ma = new OModifierAdapter(getActivity(), dbA, current_product, categoryId, mga, cashButtonList);
        m_recyclerview.setAdapter(ma);

        m_recyclerview.post(new Runnable()
        {
            @Override
            public void run()
            {
                ArrayList<OModifierGroupAdapter.OModifiersGroup> newModifiers = dbA.fetchOperativeModifiersGroupByQuery("SELECT modifiers_group.id, modifiers_group.title, modifiers_group.position , modifiers_group.notes FROM modifiers_group LEFT JOIN modifiers_group_assigned ON modifiers_group.id=modifiers_group_assigned.group_id WHERE modifiers_group_assigned.prod_id=" + categoryId + " ORDER BY modifiers_group.position");
                CustomTextView                                   productTitle = (CustomTextView) view.findViewById(R.id.currentProductTitle);
                productTitle.setText(currentProduct);
                mga.updateModifierGroups(newModifiers);
                mga.notifyDataSetChanged();
                Integer a = modifierId;

                if (modifierId != -15)
                {
                    OModifierAdapter.OModifier            currentModifier      = dbA.fetchSingleOModifiersByQuery("SELECT * FROM modifier WHERE id = " + modifierId);
                    OModifierGroupAdapter.OModifiersGroup currentModifierGroup = dbA.fetchSingleModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id = " + currentModifier.getGroupID());
                    ma.emptyModifiersList();
                    ma.setCashButtonList(cashButtonList);

                    ma.showModifiers(currentModifierGroup.getID(), 0);

                    if (currentModifierGroup.getNotes())
                    {
                        LinearLayout notecontainer = (LinearLayout) view.findViewById(R.id.modifier_notes_container);
                        notecontainer.setVisibility(VISIBLE);
                    }
                    else
                    {
                        LinearLayout notecontainer = (LinearLayout) view.findViewById(R.id.modifier_notes_container);
                        notecontainer.setVisibility(GONE);
                    }
                }
                else
                {
                    int firstNote = -1;
                    for (int i = 0; i < newModifiers.size(); i++)
                    {
                        if (newModifiers.get(i).getNotes())
                        {
                            firstNote = newModifiers.get(i).getID();
                            break;
                        }
                    }

                    OModifierGroupAdapter.OModifiersGroup currentModifierGroup = dbA.fetchSingleModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id = " + firstNote);
                    mga.turnOnActivatedGroups(currentModifierGroup.getID());
                    mga.notifyDataSetChanged();
                    ma.emptyModifiersList();
                    ma.setCashButtonList(cashButtonList);

                    View line = (View) view.findViewById(R.id.modifiers_tv_hline);
                    line.setVisibility(View.VISIBLE);

                    CustomTextView title = (CustomTextView) view.findViewById(R.id.modifiers_tv);
                    title.setVisibility(View.VISIBLE);
                    title.setText(currentModifierGroup.getTitle());
                    view.findViewById(R.id.modifiersRecyclerView).setVisibility(View.VISIBLE);

                    ma.showModifiers(currentModifierGroup.getID(), 0);

                    if (currentModifierGroup.getNotes())
                    {
                        LinearLayout notecontainer1 = (LinearLayout) view.findViewById(R.id.modifier_notes_container);
                        notecontainer1.setVisibility(GONE);
                    }
                    else
                    {
                        LinearLayout notecontainer1 = (LinearLayout) view.findViewById(R.id.modifier_notes_container);
                        notecontainer1.setVisibility(GONE);
                    }
                }

                ma.notifyDataSetChanged();
            }
        });


    }


    public void addToAdapter(CashButtonListLayout cbll)
    {
        ma.addToCashList(cbll);
    }

}
