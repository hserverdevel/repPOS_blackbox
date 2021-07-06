package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.example.blackbox.model.RequestParam;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.VatModel;
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
import static android.view.View.OnClickListener;
import static android.view.View.OnFocusChangeListener;
import static android.view.View.OnLongClickListener;
import static android.view.View.VISIBLE;

public class ModifierAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter
{
    public final int MAX_NUMBER_OF_MODIFIERS = 24;
    public  VatAdapter            vatAdapter;
    public  int                   assignmentID;
    public  int                   nModifiersAttached;
    public int number_of_modifiers;
    public PopupWindow myPopupWindow;
    public View        myPopupView;
    public PopupWindow myPopupDialog;
    public Modifier myModifier;
    private Context               context;
    private RecyclerView          parent;
    private Resources             resources;
    private ModifiersGroupAdapter groups_rv_adapter;
    private ButtonLayout          current_product;
    private ArrayList<Modifier>   modifiers;
    private DatabaseAdapter       dbA;
    private ModifierAdapter       thisAdapter            = this;
    private int                   groupID;
    private float                 dpHeight;
    private float                 dpWidth;
    private float                 density;
    private LayoutInflater        inflater;
    private boolean               keyboard_next_flag     = false;
    private boolean               addModifierActive      = false;
    private boolean               addModifierActiveFixed = false;
    private boolean               state                  = false;  //controlla in quale popup siamo

    public ModifierAdapter(Context c, DatabaseAdapter dbA, ButtonLayout current_product,
                           int groupID, ModifiersGroupAdapter groups_rv_adapter)
    {
        context                = c;
        resources              = context.getResources();
        this.dbA               = dbA;
        this.current_product   = current_product;
        this.groupID           = groupID;
        this.groups_rv_adapter = groups_rv_adapter;

        getCurrentModifiersSet();
        assignmentID = dbA.getAssignmentID(groupID, current_product.getID());

        inflater     = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        // DISPLAY METRICS USED TO CENTER POPUP WINDOW
        Display        display    = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;//; / density;
        dpWidth  = outMetrics.widthPixels;//; / density;

        ((CustomTextView) ((Activity) context).findViewById(R.id.currentProductTitle)).setText(current_product.getTitle());
    }

    public boolean getState()
    {
        return state;
    }

    public boolean getAddModifierActive()
    {
        return addModifierActive;
    }

    public void setAddModifierActive(boolean value)
    {
        addModifierActive = value;
    }

    public boolean getAddModifierActiveFixed()
    {
        return addModifierActiveFixed;
    }

    public void setAddModifierActiveFixed(boolean value)
    {
        addModifierActiveFixed = value;
    }

    public void closePopupWindow()
    {
        if (myPopupWindow != null)
        {
            myPopupWindow.dismiss();
        }
    }

    public void closeTwoPopupWindow()
    {
        if (myPopupDialog != null)
        {
            myPopupDialog.dismiss();
        }
        if (myPopupWindow != null)
        {
            myPopupWindow.dismiss();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View       v;
        ViewHolder vh;
        switch (viewType)
        {
            case -11:
                v = inflater.inflate(R.layout.button_gridview_plus, null);
                vh = new GridAdapter.PlusButtonHolder(v);
                break;
            default:
                v = inflater.inflate(R.layout.element_gridview_subelement, null);
                v.findViewById(R.id.subtitle).setVisibility(GONE);
                vh = new GroupHolder(v);
                break;
        }
        return vh;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (modifiers.get(position).getID() == -11)
        {
            return -11;
        }
        else
        {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Modifier m = modifiers.get(position);
        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFF000000); //black background
        border.setStroke((int) (3 * density), 0xFFC6C5C6); //gray border with full opacity

        switch (getItemViewType(position))
        {
            /*
             *  CASE: BUTTON
             *  DEPENDING ON THE BUTTON ID( ADD BUTTON OR OTHER BUTTON)
             *  THE LAYOUT PARAMS ARE SET TO THOSE SPECIFIED IN THE BUTTONLAYOUT OBJECT
             */
            case -11:
                GridAdapter.PlusButtonHolder plusbutton = (GridAdapter.PlusButtonHolder) holder;
                plusbutton.image.setImageResource(R.drawable.addplussubbutton);
                LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) plusbutton.image.getLayoutParams();
                ll.gravity = Gravity.CENTER;
                plusbutton.image.setLayoutParams(ll);
                plusbutton.view.setLayoutParams(new LinearLayout.LayoutParams((int) (154 * density), (int) (46 * density)));
//                border.setStroke(3, context.getColor(R.color.yellow)); // yellow border
                plusbutton.view.setBackground(context.getDrawable(R.drawable.modifier_background));
                plusbutton.view.setTag(m);
                plusbutton.view.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (!addModifierActive)
                        {
                            firePopupWindow(v);
                        }
                    }
                });
                break;


            default:
                GroupHolder button = (GroupHolder) holder;
                button.title.setText(m.getTitle());

                LayoutParams rll = (LayoutParams) button.title.getLayoutParams();
                rll.setMargins(0, (int) (7 * density), 0, 0);
                button.title.setLayoutParams(rll);

                button.view.setLayoutParams(new LayoutParams((int) (154 * density), (int) (46 * density)));

                button.view.setBackground(context.getDrawable(R.drawable.modifier_background));
                button.view.setTag(m);


                // ERROR FIXME TODO NOTE
                // why the hell is setting button.view.setActivated(true);
                // NOT doing anything???

                if (dbA.getAssignmentID(groupID, current_product.getID()) > 0 && (dbA.fetchAssignedModifiersByQuery(current_product.getID()).contains(m.getID())))
                {
                    boolean act = button.view.isActivated();
                    button.view.setActivated(true);
                }




                button.view.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (addModifierActive /*&& (groups_rv_adapter.getAddGroupActive() ||
                                groups_rv_adapter.getAddGroupActiveFixed())*/)
                        {
                            addModifierToProduct(v);
                        }

                        else if (addModifierActiveFixed /*&& (groups_rv_adapter.getAddGroupActive()
                                || groups_rv_adapter.getAddGroupActiveFixed())*/)
                        {
                            addModifierToProductFixed(v);
                        }

                        else
                        {
                            firePopupWindow(v);
                        }
                    }
                });
        }
    }

    @Override
    public int getItemCount()
    {
        return modifiers.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition)
    {
        boolean f = true;
        if (toPosition == modifiers.size() - 1)
        {
            f = false;
        }
        else
        {
            if (fromPosition < toPosition)
            {
                for (int i = fromPosition; i < toPosition; i++)
                {
                    Collections.swap(modifiers, i, i + 1);
                }
                if (!StaticValue.blackbox)
                {
                    for (int i = fromPosition; i <= toPosition; i++)
                    {
                        // apply changes in database
                        dbA.execOnDb("UPDATE modifier SET position = " + i + " WHERE id = " + modifiers.get(i).getID());
                    }
                }
            }
            else
            {
                for (int i = fromPosition; i > toPosition; i--)
                {
                    Collections.swap(modifiers, i, i - 1);
                }
                if (!StaticValue.blackbox)
                {
                    for (int i = fromPosition; i >= toPosition; i--)
                    {
                        // apply changes in database
                        dbA.execOnDb("UPDATE modifier SET position = " + i + " WHERE id = " + modifiers.get(i).getID());
                    }
                }
            }
        }
        if (f)
        {
            notifyItemMoved(fromPosition, toPosition);
        }
        return true;
    }

    public void swapModifierFunction(int fromPosition, int toPosition)
    {
        for (int i = 0; i < modifiers.size(); i++)
        {
            dbA.execOnDb("UPDATE modifier SET position = " + (i + 1) + " WHERE id = " + modifiers.get(i).getID());
        }

    }


    @Override
    public void onItemDismiss(int position)
    {
        if (StaticValue.blackbox)
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("toPosition", String.valueOf(position + 1)));
            params.add(new BasicNameValuePair("groupsSize", String.valueOf(modifiers.size() - 1)));
            params.add(new BasicNameValuePair("modifierId", String.valueOf(modifiers.get(position).getID())));
            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            params.add(new BasicNameValuePair("androidId", android_id));
            ((MainActivity) context).callHttpHandler("/moveModifier", params);
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        parent = recyclerView;
    }


    /**
     * Method firing popup window accordingly to which button has been clicked
     **/
    private void firePopupWindow(View v)
    {
        Modifier m = (Modifier) v.getTag();
        if (m.getID() == -11)
        {
            popupWindow(v, true);
        }
        else
        {
            popupWindow(v, false);
        }
    }


    private void popupWindow(View v, final boolean isBeingAdded)
    {
        state = false;
        final Modifier m = (Modifier) v.getTag();
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_modifier_add, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        //IT FUCKING WORKS!
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        LayoutParams rlp = (LayoutParams) popupView.findViewById(R.id.ModifierSetup)
                                                   .getLayoutParams();
        int t = (int) (dpHeight - 52) / 2 - rlp.height / 2;
        rlp.topMargin = t;
        popupView.findViewById(R.id.ModifierSetup).setLayoutParams(rlp);
        final EditText title          = (EditText) popupView.findViewById(R.id.modTitleInsert);
        final EditText priceContainer = (EditText) popupView.findViewById(R.id.modPriceInsert);
        CustomButton   vatContainer   = (CustomButton) popupView.findViewById(R.id.modVatInsert);

        popupView.findViewById(R.id.ModifierSetup).setVisibility(VISIBLE);
        popupView.findViewById(R.id.ModifierGroupSetup).setVisibility(GONE);
        if (!isBeingAdded)
        {
            title.setText(m.getTitle());
        }
        if (!isBeingAdded)
        {
            priceContainer.setText(String.valueOf(m.getPrice()));
        }
        //if already set, VAT value will be shown
        if (!isBeingAdded && (dbA.fetchVatModifierValue(m.getID()) != 0))
        {
            int vatValue = dbA.fetchVatByIdQuery(m.getVat());
            //vatContainer.setText(dbA.fetchVatModifierValue(m.getID()) + "%");
            vatContainer.setText("" + vatValue);
        }

        setupDismissKeyboard(popupView);

        vatContainer.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popupView.findViewById(R.id.ModifierSetup).setVisibility(GONE);
                popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);

                state = popupVatWindow(popupView, popupWindow, m);
            }
        });

        if (!isBeingAdded && (dbA.fetchVatModifierValue(m.getID()) != 0))
        {
            int vatValue = dbA.fetchVatByIdQuery(m.getVat());
            //vatContainer.setText(dbA.fetchVatModifierValue(m.getID()) + "%");
            vatContainer.setText("" + vatValue);

        }

        vatContainer.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if (dbA.fetchVatModifierValue(m.getID()) != 0)
                {
                    //unset vatValue
                    if (StaticValue.blackbox)
                    {
                        myModifier  = m;
                        myPopupView = popupView;
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("vat", String.valueOf(0)));
                        params.add(new BasicNameValuePair("id", String.valueOf(m.getID())));
                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));
                        ((MainActivity) context).callHttpHandler("/updateModifierVat", params);
                    }
                    else
                    {
                        m.setVat(0);
                        dbA.execOnDb("UPDATE modifier SET vat=" + 0 + " WHERE id=" + m.getID() + ";");
                        vatContainer.setText("");
                        notifyDataSetChanged();
                    }
                }

                return true;
            }
        });

        ImageButton btnDismiss = (ImageButton) popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });

        ImageButton btnOk = (ImageButton) popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String t    = title.getText().toString();
                float  price;
                int    vat1 = dbA.fetchVatModifierValue(m.getID());
                if (((EditText) popupView.findViewById(R.id.modPriceInsert)).getText().toString().equals(""))
                {
                    price = 0.0f;
                }
                else
                {
                    price = Float.valueOf(((EditText) popupView.findViewById(R.id.modPriceInsert)).getText().toString());
                }

                if (!t.equals(""))
                {
                    /** CASE: popupWindow fired from plus_button **/
                    if (StaticValue.blackbox)
                    {
                        if (isBeingAdded)
                        {
                            if (number_of_modifiers < MAX_NUMBER_OF_MODIFIERS)
                            {
                                myPopupWindow = popupWindow;
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'")));
                                params.add(new BasicNameValuePair("position", String.valueOf((modifiers.size() - 1))));
                                params.add(new BasicNameValuePair("price", String.valueOf(price)));
                                params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                                params.add(new BasicNameValuePair("groupID", String.valueOf(groupID)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                ((MainActivity) context).callHttpHandler("/insertModifier", params);
                            }
                        }
                        else
                        {
                            String vatV = vatContainer.getText().toString();
                            //String newvat = vatV.substring(0, vatV.length() - 1);
                            myPopupWindow = popupWindow;
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'")));
                            params.add(new BasicNameValuePair("position", String.valueOf(m.getPosition())));
                            params.add(new BasicNameValuePair("price", String.valueOf(price)));
                            params.add(new BasicNameValuePair("vat", vatV));
                            params.add(new BasicNameValuePair("groupID", String.valueOf(groupID)));
                            params.add(new BasicNameValuePair("id", String.valueOf(m.getID())));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));
                            ((MainActivity) context).callHttpHandler("/updateModifier", params);

                        }
                    }
                    else
                    {
                        if (isBeingAdded)
                        {
                            if (number_of_modifiers < MAX_NUMBER_OF_MODIFIERS)
                            {
                                dbA.execOnDb("INSERT INTO modifier (title, position, price, vat, groupID) " +
                                                     "VALUES(\"" + t.replaceAll("'", "\'") +
                                                     "\"," + (modifiers.size() - 1) + "," + price + "," + vat1 + "," + groupID + ");");
                                getCurrentModifiersSet();
                                if (number_of_modifiers == MAX_NUMBER_OF_MODIFIERS)
                                {
                                    Toast.makeText(context, R.string.max_number_of_modifiers_reached, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else
                        {
                            dbA.execOnDb("UPDATE modifier SET title = \"" + t.replaceAll("'", "\'") +
                                                 "\", price = " + price + ", vat=" + vat1 + " WHERE id =" + m.getID() + ";");
                            getCurrentModifiersSet();
                        }
                        popupWindow.dismiss();
                    }
                }
                else
                {
                    Toast toast = Toast.makeText(context, R.string.insert_title, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        CustomButton deleteModifier = (CustomButton) popupView.findViewById(R.id.deleteModifier);
        deleteModifier.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isBeingAdded)
                {
                    Toast.makeText(context, R.string.modifier_has_not_been_created_yet, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    final View dialogView = inflater.inflate(R.layout.popup_yes_no, null);
                    final PopupWindow popupDialog = new PopupWindow(
                            dialogView,
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT
                    );
                    /*LayoutParams rll = (LayoutParams) dialogView.findViewById(R.id.delete_window).getLayoutParams();
                     *//** 52 => footer height ;  322 => delete_window height **//*
                    int t = (int) ((int) (dpHeight - 52) / 2 - (134*density) / 2);
                    rll.setMargins(0, t, 0, 0);
                    dialogView.findViewById(R.id.delete_window).setLayoutParams(rll);*/
                    ((CustomTextView) dialogView.findViewById(R.id.delete_window))
                            .setText(resources.getString(R.string.you_are_deleting__modifier, m.getTitle().toUpperCase()));
                    ((CustomButton) dialogView.findViewById(R.id.delete_button))
                            .setText(R.string.delete_modifier);
                    dialogView.findViewById(R.id.delete_button).setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (StaticValue.blackbox)
                            {
                                List<NameValuePair> params     = new ArrayList<NameValuePair>(2);
                                String              android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                params.add(new BasicNameValuePair("id", String.valueOf(m.getID())));
                                myPopupWindow = popupWindow;
                                myPopupView   = popupView;
                                myPopupDialog = popupDialog;
                                ((MainActivity) context).callHttpHandler("/deleteModifier", params);
                            }
                            else
                            {
                                dbA.deleteModifierFromTableByID("modifier", m.getID());
                                getCurrentModifiersSet();
                                popupWindow.dismiss();
                                popupDialog.dismiss();
                            }
                        }
                    });
                    dialogView.findViewById(R.id.cancel_button).setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            popupDialog.dismiss();
                        }
                    });
                    popupDialog.setFocusable(true);
                    popupDialog.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
                }
            }
        });
        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
    }


    public void getCurrentModifiersSet()
    {
        modifiers           = dbA.fetchModifiersByQuery("SELECT * FROM modifier WHERE groupID = " + groupID + " ORDER BY position");
        number_of_modifiers = modifiers.size();

        if (number_of_modifiers < MAX_NUMBER_OF_MODIFIERS)
        {
            Modifier plus_button = new Modifier();
            plus_button.setID(-11);
            plus_button.setPosition(modifiers.size());
            modifiers.add(plus_button);
        }
        notifyDataSetChanged();

    }


    public void updateDataSet(int newGroupID)
    {
        this.groupID = newGroupID;
        getCurrentModifiersSet();
        assignmentID = dbA.getAssignmentID(newGroupID, current_product.getID());
        parent.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (addModifierActive)
                {
                    turnModifiersOFF();
                    turnModifiersON();
                }
            }
        });
    }

    /**
     * Start of methods for Modifiers Selection
     * <p>
     * <p>
     * switchAddModifierActive: if active = true, all the single modifiers previously attached
     * to the current product are lit up (IF the modifiers view was already open)
     */
    public boolean switchAddModifierActive(boolean active)
    {
        int childCount = parent.getChildCount() - 1;
        if (!active)
        {
            addModifierActive = false;
            // turns off the modifiers
            turnModifiersOFF();
        }
        else
        {
            addModifierActive = true;
            // turns ON the modifiers groups which were active on the current product
            ArrayList<Modifier> array =
                    dbA.fetchModifiersByQuery("SELECT * " +
                                                      "FROM modifier " +
                                                      "WHERE id IN (SELECT modifier_id " +
                                                      "FROM modifiers_assigned " +
                                                      "WHERE assignment_id=" + assignmentID +
                                                      " AND fixed =0" +
                                                      ")" +
                                                      "ORDER BY position");
            if (array != null)
            {
                nModifiersAttached = array.size();
                if (array.size() != 0 && ((MainActivity) context).wereModifiersOpen)
                {
                    for (int i = 0; i < childCount; i++)
                    {
                        View     v = parent.getChildAt(i);
                        Modifier m = (Modifier) v.getTag();
                        for (int j = 0; j < array.size(); j++)
                        {
                            if (m.getID() == array.get(j).getID())
                            {
                                v.setActivated(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return addModifierActive;
    }


    public boolean switchAddModifierActiveFixed(boolean active)
    {

        int childCount = parent.getChildCount() - 1;
        if (!active)
        {
            addModifierActiveFixed = false;
            // turns off the modifiers
            turnModifiersOFF();
        }
        else
        {
            addModifierActiveFixed = true;
            // turns ON the modifiers groups which were active on the current product
            ArrayList<Modifier> array =  dbA.fetchModifiersByQuery(
                                        String.format("" +
                                              " SELECT * FROM modifier WHERE id IN" +
                                              " ( " +
                                              " SELECT modifier_id FROM modifiers_assigned " +
                                              "   WHERE " +
                                              "        assignment_id IN (SELECT id FROM modifiers_group_assigned WHERE prod_id = %s) " +
                                              "   AND" +
                                              "        fixed = 1" +
                                              " ) " +
                                              " ORDER BY position;",
                                             current_product.getID()
                                        ));

            nModifiersAttached = array.size();

            if (array.size() != 0 && ((MainActivity) context).wereModifiersOpen)
            {
                for (int i = 0; i < childCount; i++)
                {
                    View     v = parent.getChildAt(i);
                    Modifier m = (Modifier) v.getTag();
                    for (int j = 0; j < array.size(); j++)
                    {
                        if (m.getID() == array.get(j).getID())
                        {
                            v.setActivated(true);
                            break;
                        }
                    }
                }
            }
        }
        return addModifierActiveFixed;
    }


    private void turnModifiersON()
    {
        int childCount = parent.getChildCount() - 1;
        ArrayList<Modifier> array =
                dbA.fetchModifiersByQuery("SELECT * " +
                                                  "FROM modifier " +
                                                  "WHERE id IN (SELECT modifier_id " +
                                                  "FROM modifiers_assigned " +
                                                  "WHERE assignment_id=" + assignmentID +
                                                  ")" +
                                                  "ORDER BY position");
        nModifiersAttached = array.size();
        if (array.size() != 0 && ((MainActivity) context).wereModifiersOpen)
        {
            for (int i = 0; i < childCount; i++)
            {
                View     v = parent.getChildAt(i);
                Modifier m = (Modifier) v.getTag();
                for (int j = 0; j < array.size(); j++)
                {
                    if (m.getID() == array.get(j).getID())
                    {
                        v.setActivated(true);
                        break;
                    }
                }
            }
        }
    }


    public void turnModifiersOFF()
    {
        int childCount = parent.getChildCount() - 1;

        for (int i = 0; i < childCount; i++)
        {
            parent.getChildAt(i).setActivated(false);
        }
    }


    private void addModifierToProduct(View v)
    {
        // No modifier from the current group was ever added before
        if (assignmentID == -1)
        {
            groups_rv_adapter.addPartialGroupToProduct(groupID);
            assignmentID = dbA.getAssignmentID(groupID, current_product.getID());
            // Accende gruppo
            int i = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupID)
                       .get(0).getPosition();
            Log.i("GROUP POSITION", "" + i);
            groups_rv_adapter.getChildAt(i - 1).setActivated(true);
        }
        if (assignmentID != -2)
        {
            Modifier m = (Modifier) v.getTag();
            if (v.isActivated())
            {
                if (StaticValue.blackbox)
                {
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("modifierId", String.valueOf(m.getID())));
                    params.add(new BasicNameValuePair("groupId", String.valueOf(groupID)));
                    params.add(new BasicNameValuePair("assignementId", String.valueOf(assignmentID)));
                    params.add(new BasicNameValuePair("fixed", String.valueOf(0)));
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add(new BasicNameValuePair("androidId", android_id));

                    myPopupView = v;
                    ((MainActivity) context).callHttpHandler("/deleteFromModifierAssigned", params);
                }
                else
                {

                    v.setActivated(false);
                    dbA.execOnDb("DELETE FROM modifiers_assigned WHERE modifier_id = " + m.getID());
                    nModifiersAttached--;
                    // As the last modifier of the group gets un-attached, the group assignment entry gets erased
                    if (nModifiersAttached <= 0)
                    {
                        dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + groupID);
                        // Spegne gruppo
                        int i = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupID)
                                   .get(0).getPosition();
                        groups_rv_adapter.getChildAt(i).setActivated(false);
                        Log.i("GROUP POSITION", "" + i);

                        assignmentID       = -1;
                        nModifiersAttached = -1;
                    }
                }
            }
            else
            {
                if (StaticValue.blackbox)
                {
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("modifierId", String.valueOf(m.getID())));
                    params.add(new BasicNameValuePair("groupId", String.valueOf(groupID)));
                    params.add(new BasicNameValuePair("assignementId", String.valueOf(assignmentID)));
                    params.add(new BasicNameValuePair("fixed", String.valueOf(0)));
                    params.add(new BasicNameValuePair("productId", String.valueOf(current_product.getID())));
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add(new BasicNameValuePair("androidId", android_id));

                    myPopupView = v;
                    ((MainActivity) context).callHttpHandler("/insertInModifierAssigned", params);
                }
                else
                {
                    v.setActivated(true);
                    int i = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupID)
                               .get(0).getPosition();
                    groups_rv_adapter.getChildAt(i).setActivated(true);
                    dbA.execOnDb("DELETE FROM modifiers_assigned WHERE assignment_id=" + assignmentID + " AND modifier_id = " + m.getID());

                    dbA.execOnDb("INSERT INTO modifiers_assigned(assignment_id, modifier_id, fixed) " +
                                         "VALUES(" + assignmentID + "," + m.getID() + ", 0);");
                    nModifiersAttached++;
                }
            }
        }
        else
        {
            Toast toast = Toast.makeText(context, R.string.the_whole_group_has_been_attached_to_product_already, Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    private void addModifierToProductFixed(View v)
    {
        // No modifier from the current group was ever added before
        //dbA.showData("modifiers_group_assigned");
        if (assignmentID == -1)
        {
            groups_rv_adapter.addPartialGroupToProduct(groupID);
            assignmentID = dbA.getAssignmentID(groupID, current_product.getID());

            // Accende gruppo
            ArrayList<ModifiersGroupAdapter.ModifiersGroup> p = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupID);
            int i = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupID).get(0).getPosition();

            groups_rv_adapter.getChildAt(i - 1).setActivated(true);
        }

        if (assignmentID != -2)
        {
            Modifier m = (Modifier) v.getTag();
            if (v.isActivated())
            {
                if (StaticValue.blackbox)
                {
                    ArrayList<Integer> assignDetails = dbA.fetchAssignmentDetailByProductAndModifierId(current_product.getID(), groupID, m.getID());

                    if (assignDetails.size() == 2)
                    {
                        RequestParam params = new RequestParam();
                        params.add("modifierId", assignDetails.get(0));
                        params.add("assignementId", assignDetails.get(1));

                        myPopupView = v;
                        ((MainActivity) context).callHttpHandler("/deleteFromModifierAssigned", params);
                    }
                }

                else
                {
                    v.setActivated(false);
                    dbA.execOnDb("DELETE FROM modifiers_assigned WHERE modifier_id = " + m.getID());
                    nModifiersAttached--;
                    // As the last modifier of the group gets un-attached, the group assignment entry gets erased
                    if (nModifiersAttached <= 0)
                    {
                        dbA.execOnDb("DELETE FROM modifiers_group_assigned WHERE group_id = " + groupID);
                        // Spegne gruppo
                        int i = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupID)
                                   .get(0).getPosition();
                        groups_rv_adapter.getChildAt(i).setActivated(false);
                        Log.i("GROUP POSITION", "" + i);

                        assignmentID       = -1;
                        nModifiersAttached = -1;
                    }
                }
            }

            else
            {
                if (StaticValue.blackbox)
                {
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("modifierId", String.valueOf(m.getID())));
                    params.add(new BasicNameValuePair("groupId", String.valueOf(groupID)));
                    params.add(new BasicNameValuePair("assignementId", String.valueOf(assignmentID)));
                    params.add(new BasicNameValuePair("fixed", String.valueOf(1)));
                    params.add(new BasicNameValuePair("productId", String.valueOf(current_product.getID())));
                    params.add(new BasicNameValuePair("androidId", StaticValue.androidId));

                    myPopupView = v;
                    ((MainActivity) context).callHttpHandler("/insertInModifierAssigned", params);
                }

                else
                {
                    v.setActivated(true);
                    int i = dbA.fetchModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + groupID)
                               .get(0).getPosition();
                    groups_rv_adapter.getChildAt(i).setActivated(true);
                    dbA.execOnDb("DELETE FROM modifiers_assigned WHERE assignment_id=" + assignmentID + " AND modifier_id = " + m.getID());
                    dbA.execOnDb("INSERT INTO modifiers_assigned(assignment_id, modifier_id, fixed) " +
                                         "VALUES(" + assignmentID + "," + m.getID() + ", 1);");
                    nModifiersAttached++;
                }
            }
        }

        else
        {
            Toast toast = Toast.makeText(context, R.string.the_whole_group_has_been_attached_to_product_already, Toast.LENGTH_SHORT);
            toast.show();
        }
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
                    {
                        keyboard_next_flag = true;
                    }
                    return false;
                }
            });
            view.setOnFocusChangeListener(new OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (!hasFocus)
                    {
                        if (!(((Activity) context).getCurrentFocus() instanceof EditText) && !keyboard_next_flag)
                        {
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
        if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupDismissKeyboard(innerView);
            }
        }
    }

    /**
     * (added by Fabrizio)
     */
    private boolean popupVatWindow(View popupView, PopupWindow popupWindow, Modifier m)
    {

        state = false;
        LayoutParams rlp = (LayoutParams) popupView.findViewById(R.id.new_Popup_Vat_Insert).getLayoutParams();
        int          t   = (int) (dpHeight - 52) / 2 - rlp.height / 2;
        rlp.topMargin = t;
        popupView.findViewById(R.id.new_Popup_Vat_Insert).setLayoutParams(rlp);

        RecyclerView vat_recycler = (RecyclerView) popupView.findViewById(R.id.vat_value_recycler);
        vat_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayout.VERTICAL, false));
        vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, m, thisAdapter, true);
        vat_recycler.setAdapter(vatAdapter);
        vat_recycler.setHasFixedSize(true);

        EditText vatContainer  = (EditText) popupView.findViewById(R.id.vat_group_insert);
        EditText percContainer = (EditText) popupView.findViewById(R.id.vat_perc_insert);

        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
        vat_recycler.addItemDecoration(divider);
        setupDismissKeyboard(popupView);

        ImageButton btnDismiss = (ImageButton) popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //due comportamenti diversi, in base ai popup visibili
                if (state && vatAdapter.getVatState())
                {
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(GONE);
                    popupView.findViewById(R.id.ModifierSetup).setVisibility(VISIBLE);
                    if (dbA.fetchVatModifierValue(m.getID()) != 0)
                    {
                        ((CustomButton) popupView.findViewById(R.id.modVatInsert)).setText("" + dbA.fetchVatModifierValue(m.getID()));
                    }
                    state = false;
                    vatAdapter.setVatState(false);
                }
                else if (!vatAdapter.getVatState() || (vatAdapter.getVatState() && !state))
                {
                    popupWindow.dismiss();
                }
            }
        });

        ImageButton btnOk = (ImageButton) popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int vat1 = 0;
                if (vatAdapter.getVatState())
                {
                    if (vatContainer.getText().toString().equals(""))
                    {
                        Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String textVat = String.valueOf(vatContainer.getText().toString());
                        textVat = textVat.replaceAll("%", "");
                        if (textVat.matches("[0-9]*\\.[0-9]*"))
                        {
                            float floatVat = Float.parseFloat(textVat);
                            floatVat = floatVat * 100;
                            vat1     = (int) floatVat;
                        }
                        else
                        {
                            vat1 = Integer.parseInt(textVat);
                        }
                    }
                    if (vat1 != 0 && !dbA.checkIfVatIsAdded(vat1))
                    {
                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                            params.add(new BasicNameValuePair("from", String.valueOf(0)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myPopupView = popupView;
                            ((MainActivity) context).callHttpHandler("/insertVatFromModifier", params);
                        }
                        else
                        {
                            dbA.addVatValue(vat1);
                            ArrayList<VatModel> newVats = dbA.fetchVatArrayByQuery();
                            vatAdapter.setVats(newVats);
                            vatAdapter.notifyDataSetChanged();
                            vatContainer.setText("");
                            percContainer.setText("");
                        }
                    }
                }
                else if (!vatAdapter.getVatState())
                {
                    if (!dbA.checkIfVatIsAdded(vat1) && vat1 != 0)
                    {
                        if (StaticValue.blackbox)
                        {
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                            params.add(new BasicNameValuePair("from", String.valueOf(1)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myModifier    = m;
                            myPopupView   = popupView;
                            myPopupWindow = popupWindow;
                            ((MainActivity) context).callHttpHandler("/insertVatFromModifier", params);
                        }
                        else
                        {
                            dbA.addVatValue(vat1);
                            vatContainer.setText("");
                            percContainer.setText("");
                            RecyclerView vat_recycler = (RecyclerView) popupView.findViewById(R.id.vat_value_recycler);
                            vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, m, thisAdapter, true);
                            vat_recycler.setAdapter(vatAdapter);
                            vat_recycler.setHasFixedSize(true);
                            vat_recycler.setVisibility(VISIBLE);
                            popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);
                            DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
                            divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
                            vat_recycler.addItemDecoration(divider);
                            vatAdapter.notifyDataSetChanged();
                        }

                    }
                    else if (dbA.checkIfVatIsAdded(vat1) && vat1 != 0)
                    {
                        m.setVat(vat1);

                        popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(GONE);
                        popupView.findViewById(R.id.ModifierSetup).setVisibility(VISIBLE);
                        ((CustomButton) popupView.findViewById(R.id.modVatInsert)).setText("" + vat1);
                        state = false;
                    }
                    else
                    {
                        okBehaviourWhenCreatingModifier(m, popupView, popupWindow);
                    }
                }
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);

        return state;
    }

    public void setVatAdapterFromServer(int vat1, View popupView, PopupWindow popupWindow)
    {
        dbA.addVatValue(vat1);
        EditText vatContainer = (EditText) popupView.findViewById(R.id.vat_group_insert);
        vatContainer.setText("");
        EditText percContainer = (EditText) popupView.findViewById(R.id.vat_perc_insert);
        percContainer.setText("");
        RecyclerView vat_recycler = (RecyclerView) popupView.findViewById(R.id.vat_value_recycler);
        vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, myModifier, thisAdapter, true);
        vat_recycler.setAdapter(vatAdapter);
        vat_recycler.setHasFixedSize(true);
        vat_recycler.setVisibility(VISIBLE);
        popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
        vat_recycler.addItemDecoration(divider);
        vatAdapter.notifyDataSetChanged();
    }

    public void okBehaviourWhenCreatingModifier(Modifier m, View popupView, PopupWindow popupWindow)
    {
        String t    = ((EditText) popupView.findViewById(R.id.modTitleInsert)).getText().toString();
        float  price;
        int    vat1 = m.getVat();
        if (vat1 == 0)
        {
            dbA.fetchVatModifierValue(m.getID());
        }
        if (((EditText) popupView.findViewById(R.id.modPriceInsert)).getText().toString().equals(""))
        {
            price = 0.0f;
        }
        else
        {
            price = Float.valueOf(((EditText) popupView.findViewById(R.id.modPriceInsert)).getText().toString());
        }

        if (!t.equals(""))
        {
            if (StaticValue.blackbox)
            {
                if (number_of_modifiers < MAX_NUMBER_OF_MODIFIERS)
                {
                    if (!dbA.checkIfModifierExists(m.getID()))
                    {
                        myPopupWindow = popupWindow;
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'")));
                        params.add(new BasicNameValuePair("position", String.valueOf((modifiers.size() - 1))));
                        params.add(new BasicNameValuePair("price", String.valueOf(price)));
                        params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                        params.add(new BasicNameValuePair("groupID", String.valueOf(groupID)));
                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));
                        ((MainActivity) context).callHttpHandler("/insertModifier", params);
                    }
                    else
                    {
                        CustomButton vatContainer = (CustomButton) popupView.findViewById(R.id.modVatInsert);
                        String       vatV         = vatContainer.getText().toString();
                        //vatV.substring(0, vatV.length() - 1);
                        myPopupWindow = popupWindow;
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("title", t.replaceAll("'", "\'")));
                        params.add(new BasicNameValuePair("position", String.valueOf((modifiers.size() - 1))));
                        params.add(new BasicNameValuePair("price", String.valueOf(price)));
                        params.add(new BasicNameValuePair("vat", vatV));
                        params.add(new BasicNameValuePair("groupID", String.valueOf(groupID)));
                        params.add(new BasicNameValuePair("id", String.valueOf(m.getID())));
                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));
                        ((MainActivity) context).callHttpHandler("/updateModifier", params);
                    }
                }

            }
            else
            {
                if (number_of_modifiers < MAX_NUMBER_OF_MODIFIERS)
                {
                    if (!dbA.checkIfModifierExists(m.getID()))
                    {
                        dbA.execOnDb("INSERT INTO modifier (title, position, price, vat, groupID) " +
                                             "VALUES(\"" + t.replaceAll("'", "\'") +
                                             "\"," + (modifiers.size() - 1) + "," + price + "," + vat1 + "," + groupID + ");");
                        getCurrentModifiersSet();
                        if (number_of_modifiers == MAX_NUMBER_OF_MODIFIERS)
                        {
                            Toast.makeText(context, R.string.max_number_of_modifiers_reached, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        dbA.execOnDb("UPDATE modifier SET title = \"" + t.replaceAll("'", "\'")
                                             + "\", price = " + price + ", vat = " + vat1 + " WHERE id=" + m.getID() + ";");
                    }
                }
                popupWindow.dismiss();
            }
        }
        else
        {
            Toast toast = Toast.makeText(context, R.string.insert_title, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void setNotesContainer(boolean value)
    {
        if (value)
        {
            //((Activity)context).findViewById(R.id.notes_container_hline).setVisibility(VISIBLE);
            ((Activity) context).findViewById(R.id.modifier_notes_container).setVisibility(VISIBLE);
        }
        else if (!value)
        {
            //((Activity)context).findViewById(R.id.notes_container_hline).setVisibility(GONE);
            if (((Activity) context).findViewById(R.id.modifier_notes_container) != null)
            {
                if (((Activity) context).findViewById(R.id.modifier_notes_container).getVisibility() == VISIBLE)
                {
                    ((Activity) context).findViewById(R.id.modifier_notes_container).setVisibility(GONE);
                }
            }
        }
    }




    /**
     * HOLDERS
     **/
    public static class GroupHolder extends ViewHolder
    {
        public View           view;
        public CustomTextView title;

        public GroupHolder(View itemView)
        {
            super(itemView);
            view  = itemView;
            title = (CustomTextView) view.findViewById(R.id.title);
        }

        @Override
        public String toString()
        {
            return "GroupHolder, Title: " + title.getText().toString();
        }
    }

    public static class PlusButtonHolder extends ViewHolder
    {
        public View      view;
        public ImageView image;

        public PlusButtonHolder(View itemView)
        {
            super(itemView);
            view  = itemView;
            image = (ImageView) view.findViewById(R.id.button_img);
        }

        @Override
        public String toString()
        {
            return "PlusButton";
        }
    }

    /**
     * Modifier class containing the single modifier's data
     */
    public static class Modifier
    {
        private int    id;
        private String title;
        private float  price;
        private int    position;
        private int    groupID;
        private int    vat;

        public Modifier()
        {
        }

        public Modifier(int id, String title, double price, int position, int groupID)
        {
            this.id       = id;
            this.title    = title;
            this.price    = (float) price;
            this.position = position;
        }

        public static Modifier fromJson(JSONObject jsonObject)
        {
            Modifier modifier = new Modifier();
            // Deserialize json into object fields
            try
            {
                modifier.id       = jsonObject.getInt("id");
                modifier.title    = jsonObject.getString("title");
                modifier.price    = (float) jsonObject.getDouble("price");
                modifier.position = jsonObject.getInt("position");
                modifier.groupID  = jsonObject.getInt("groupID");
                modifier.vat      = jsonObject.getInt("vat");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                return null;
            }
            // Return new object
            return modifier;
        }

        public static ArrayList<Modifier> fromJsonArray(JSONArray jsonObject)
        {
            Gson                gson = new Gson();
            Type                type = new TypeToken<List<Modifier>>()
            {
            }.getType();
            ArrayList<Modifier> list = gson.fromJson(jsonObject.toString(), type);
            return list;
        }

        public int getID()
        {
            return id;
        }

        public void setID(int id)
        {
            this.id = id;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public float getPrice()
        {
            return price;
        }

        public void setPrice(double price)
        {
            this.price = (float) price;
        }

        public int getPosition()
        {
            return position;
        }

        public void setPosition(int pos)
        {
            position = pos;
        }

        public int getGroupID()
        {
            return groupID;
        }

        public int getVat()
        {
            return vat;
        }

        public void setVat(int val)
        {
            vat = val;
        }

        public void setGroup(int gID)
        {
            groupID = gID;
        }

    }
}
