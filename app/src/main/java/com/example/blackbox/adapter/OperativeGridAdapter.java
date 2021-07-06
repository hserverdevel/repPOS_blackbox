package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.MainActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.StatusInfoHolder;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.OnFocusChangeListener;
import static android.view.View.VISIBLE;

public class OperativeGridAdapter extends Adapter<ViewHolder> {
    private Context context;
    private Resources resources;
    private ArrayList<ButtonLayout> buttons;
    private int previousCatID = 0;
    private int currentCatID = 0;
    private String previousCatTitle;
    private String currentCatTitle;
    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private int deepnessLevel = 0;
    private int numberOfCategories;
    private int numberOfProducts;
    private int MAX_NUMBER_OF_CATEGORIES;
    private boolean keyboard_next_flag = false;
    private float density;
    private float dpHeight;
    private float dpWidth;
    private int vatValue = 0;
    private OperativeGridAdapter thisGrid = this;

    private final int MAX_DEEPNESS_LEVEL = 3;
    private final int MAX_NUMBER_OF_PRODUCTS = 64;

        public OperativeGridAdapter(Context c, DatabaseAdapter dbA) {
        context = c;
        this.dbA = dbA;
        resources = context.getResources();

        buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = 0");
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        /**DISPLAY METRICS USED TO CENTER POPUP WINDOW **/
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels / density;
        dpWidth = outMetrics.widthPixels / density;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        switch (viewType) {
            case -1:
                v = inflater.inflate(R.layout.element_gridview_subelement, null);
                vh = new SubButtonHolder(v);
                break;
            default:
                v = inflater.inflate(R.layout.element_gridview_element, null);
                vh = new ButtonHolder(v);
                break;
        }
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        if (deepnessLevel > MAX_DEEPNESS_LEVEL)
            return -1; //sub button type
        else return 1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ButtonLayout b = buttons.get(position);
        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(b.getColor());
        border.setStroke(3, 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8);
        switch (getItemViewType(position)) {
            /**
             *  CASE: BUTTON
             *  DEPENDING ON THE BUTTON ID( ADD BUTTON OR OTHER BUTTON)
             *  THE LAYOUT PARAMS ARE SET TO THOSE SPECIFIED IN THE BUTTONLAYOUT OBJECT
             */
            case -1: //SUB CATEGORY CASE
                SubButtonHolder subbutton = (SubButtonHolder) holder;
                subbutton.title.setText(b.getTitle());
                if (!b.getSubTitle().equals("")) {
                    subbutton.subtitle.setVisibility(VISIBLE);
                    subbutton.subtitle.setText(b.getSubTitle());
                    LayoutParams rll = (LayoutParams) subbutton.title.getLayoutParams();
                    rll.setMargins(0, -1, 0, 0);
                    subbutton.title.setLayoutParams(rll);
                } else {
                    subbutton.subtitle.setVisibility(View.GONE);
                    LayoutParams rll = (LayoutParams) subbutton.title.getLayoutParams();
                    rll.setMargins(0, 6, 0, 0);
                    subbutton.title.setLayoutParams(rll);
                }
                subbutton.view.setLayoutParams(new LayoutParams(154, 46));
                border.setStroke(3, context.getColor(R.color.yellow)); // yellow border
                subbutton.view.setBackground(border);
                subbutton.view.setTag(b);
                subbutton.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                break;
            default:
                ButtonHolder button = (ButtonHolder) holder;
                button.title.setText(b.getTitle());
                if (!b.getSubTitle().equals("")) {
                    button.subtitle.setVisibility(VISIBLE);
                    button.subtitle.setText(b.getSubTitle());
                    LayoutParams rll = (LayoutParams) button.title.getLayoutParams();
                    rll.setMargins(0, -5, 0, 0);
                    button.title.setLayoutParams(rll);
                } else {
                    button.subtitle.setVisibility(View.GONE);
                    LayoutParams rll = (LayoutParams) button.title.getLayoutParams();
                    rll.setMargins(0, 4, 0, 0);
                    button.title.setLayoutParams(rll);
                }
                //button.frame.setVisibility(VISIBLE);
                button.image2.setImageResource(getImageId(context, b.getImg()));
                button.view.setLayoutParams(new LayoutParams(154, 146));
                button.view.setBackground(border);
                button.view.setTag(b);
                button.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (b.getCat() == 1) goToCategory(currentCatID, b.getID(), b.getTitle());
                        // else product
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        return buttons.size();
    }

    /**
     * HOLDERS
     **/
    public static class ButtonHolder extends ViewHolder {
        public View view;
        public CustomTextView title;
        public CustomTextView subtitle;
        public RelativeLayout txtcontainer;
        public FrameLayout frame;
        public ImageView image2;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            txtcontainer = (RelativeLayout) view.findViewById(R.id.text_container);
            title = (CustomTextView) view.findViewById(R.id.title);
            subtitle = (CustomTextView) view.findViewById(R.id.subtitle);
            image2 = (ImageView) view.findViewById(R.id.button_frame_img);
            //frame = (FrameLayout) view.findViewById(R.id.img_frame);
            //subtitle.setLetterSpacing(0.05);
        }

        @Override
        public String toString() {
            return "ButtonHolder, Title: " + title.getText().toString();
        }
    }

    public static class SubButtonHolder extends ViewHolder {
        public View view;
        public CustomTextView title;
        public CustomTextView subtitle;
        public RelativeLayout txtcontainer;

        public SubButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            txtcontainer = (RelativeLayout) view.findViewById(R.id.text_container);
            title = (CustomTextView) view.findViewById(R.id.title);
            subtitle = (CustomTextView) view.findViewById(R.id.subtitle);

            //subtitle.setLetterSpacing(0.05);
        }

        @Override
        public String toString() {
            return "ButtonHolder, Title: " + title.getText().toString();
        }
    }

    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    /**
     *  START OF DEFINITION OF METHODS CALLED AT ONCLICK()
     */

    /**
     * PRODUCT
     * Popup window that will show up for PRODUCT settings
     */

    public void prodSettingsPopupWindow(final View grid_element) {
        final ButtonLayout b = (ButtonLayout) grid_element.getTag();
        final LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_button_add, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        popupView.findViewById(R.id.ButtonTypeChooser).setVisibility(View.GONE);
        popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
        LayoutParams rll = (LayoutParams) popupView
                .findViewById(R.id.ProductSetup).getLayoutParams();
        /** 52 => footer height ; 379 => popupWindow height/2 **/
        int t = (int) (dpHeight - 52) / 2 - 379 / 2;
        rll.setMargins(0, t, 0, 0);
        popupView.findViewById(R.id.ProductSetup).setLayoutParams(rll);

        // GETTING View elements
        EditText title = (EditText) popupView.findViewById(R.id.prodTitleInsert);
        EditText subtitle = (EditText) popupView.findViewById(R.id.prodSubTitleInsert);
        EditText price = (EditText) popupView.findViewById(R.id.prodPriceInsert);
        CustomButton vat = (CustomButton) popupView.findViewById(R.id.prodTaxInsert);
        title.setText(b.getTitle());
        subtitle.setText(b.getSubTitle());
        price.setText(new String("" + b.getPrice()));
        if(b.getVat() != 0)
            vat.setText(new String(b.getVat() + "%"));
        else
            vat.setText(R.string.vat);
        setupDismissKeyboard(popupView);

        vat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupVatWindow(view, popupWindow, b);
            }
        });

        // Closure Button
        ImageButton btnDismiss = (ImageButton) popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        // Ok button onClick
        ImageButton btnOk = (ImageButton) popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // todo: missing button color info and img update
                if (((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals("")) {
                    Toast toast = Toast.makeText(context, R.string.insert_price, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    dbA.execOnDb("UPDATE button SET title = '" + title + "', subtitle = '" + subtitle + "', img_name = 'buttonimage', " +
                            "price = " + price +
                            "WHERE " + DatabaseAdapter.KEY_ID + " = " + b.getID() + ";");

                    getCurrentCatButtonSet(currentCatID);
                    popupWindow.dismiss();
                }
            }
        });

        // goToModifiers
        CustomButton btnOpenModifiers = (CustomButton) popupView.findViewById(R.id.goToModifiers);
        btnOpenModifiers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusInfoHolder sh = new StatusInfoHolder();
                sh.setCurrent_category_id(currentCatID);
                sh.setDept_level(deepnessLevel);
                sh.setPrevious_category_title(currentCatTitle);
                sh.setPrevious_category_id(previousCatID);
                sh.setCurrent_product(b);
                ((MainActivity) context).setGridStatus(sh);
                ((MainActivity) context).switchView(MainActivity.MODIFIERS_VIEW, b.getID());
                popupWindow.dismiss();
            }
        });


        // DELETE
        CustomButton btnDelete = (CustomButton) popupView.findViewById(R.id.deleteProduct);
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //recursively deleting buttons, dialog with YES/NO will show up
                v.setElevation(0.0f);
                final View dialogView = layoutInflater.inflate(R.layout.popup_yes_no, null);
                final PopupWindow popupDialog = new PopupWindow(
                        dialogView,
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
               /* LayoutParams rll = (LayoutParams) dialogView.findViewById(R.id.delete_window).getLayoutParams();
                *//** 52 => footer height ;  322 => delete_window height **//*
                int t = (int) (dpHeight - 52) / 2 - 134 / 2;
                rll.setMargins(0, t, 0, 0);
                dialogView.findViewById(R.id.delete_window).setLayoutParams(rll);*/
                ((CustomTextView) dialogView.findViewById(R.id.delete_window))
                        .setText(resources.getString(R.string.you_are_deleting__product, b.getTitle().toUpperCase()));
                ((CustomButton) dialogView.findViewById(R.id.delete_button))
                        .setText(R.string.deleteProduct);
                dialogView.findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbA.deleteButton(b);
                        getCurrentCatButtonSet(currentCatID);
                        popupWindow.dismiss();
                        popupDialog.dismiss();
                    }
                });
                dialogView.findViewById(R.id.cancel_button).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupDialog.dismiss();
                    }
                });
                popupDialog.setFocusable(true);
                popupDialog.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity) context).findViewById(R.id.main), 0, 0, 0);
    }


    /**
     * GO_TO_CATEGORY
     * Method called when "Go To Category" is clicked
     **/
    public void goToCategory(final int currCatID, int newCatID, String categoryTitle) {
        RelativeLayout above_rv = (RelativeLayout) ((Activity) context).findViewById(R.id.above_recyclerView);
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFd3d3d3); //light-gray background
        border.setStroke(3, 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8);
        this.currentCatTitle = categoryTitle;
        this.currentCatID = newCatID;
        if (newCatID == 0) {
            //Main page case
            deepnessLevel = 0;
            above_rv.setVisibility(GONE);
            LayoutParams lp = (LayoutParams) ((Activity) context).findViewById(R.id.recyclerView).getLayoutParams();
            lp.topMargin = 0;
            ((Activity) context).findViewById(R.id.recyclerView).setLayoutParams(lp);
            getCurrentCatButtonSet(newCatID);
        } else {
            deepnessLevel++;
            above_rv.setVisibility(VISIBLE);
            LayoutParams lp = (LayoutParams) ((Activity) context).findViewById(R.id.recyclerView).getLayoutParams();
            lp.topMargin = -9;
            ((CustomTextView) above_rv.findViewById(R.id.infoTextView)).setText(R.string.to_be_decided);
            ((Activity) context).findViewById(R.id.recyclerView).setLayoutParams(lp);
            ((CustomTextView) above_rv.findViewById(R.id.categoryTitle)).setText(categoryTitle);
            Cursor c = dbA.fetchByQuery("SELECT * FROM button WHERE id=" + currentCatID);
            c.moveToFirst();
            previousCatID = c.getInt(c.getColumnIndex(DatabaseAdapter.KEY_CAT_ID));
            c.close();
            if (previousCatID == 0) previousCatTitle = null;
            else {
                c = dbA.fetchByQuery("SELECT * FROM button WHERE id=" + previousCatID);
                c.moveToFirst();
                previousCatTitle = c.getString(c.getColumnIndex(DatabaseAdapter.KEY_TITLE));
                c.close();
            }
            above_rv.findViewById(R.id.backButton).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deepnessLevel--;
                    goToCategory(currentCatID, previousCatID, previousCatTitle);
                }
            });
            above_rv.findViewById(R.id.backButton).setBackground(border);
            getCurrentCatButtonSet(newCatID);
        }
    }

    public void getCurrentCatButtonSet(int catID) {
        buttons.clear();
        buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = " + catID + " ORDER BY position");
        MainActivity.setButtonSet(buttons);
    }

    private String getCurrentLevelName() {
        String name = new String();
        if (MAX_DEEPNESS_LEVEL == 0) name = "CATEGORY";
        else
            switch (deepnessLevel) {
                case MAX_DEEPNESS_LEVEL:
                    name = "SUBCATEGORY";
                    break;
                case MAX_DEEPNESS_LEVEL - 1:
                    name = "CATEGORY";
                    break;
                case MAX_DEEPNESS_LEVEL - 2:
                    name = "MAIN CATEGORY";
                    break;
                case MAX_DEEPNESS_LEVEL - 3:
                    name = "SECTION";
                    break;
            }
        return name;
    }


    public void setupDismissKeyboard(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if ((view instanceof EditText)) {
            ((EditText) view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) keyboard_next_flag = true;
                    return false;
                }
            });
            view.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if (!(((Activity) context).getCurrentFocus() instanceof EditText) && !keyboard_next_flag) {
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


    private void popupVatWindow(View v, PopupWindow p, ButtonLayout b){

        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_vat_insert, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        LayoutParams rlp = (LayoutParams)popupView.findViewById(R.id.popup_vat_Insert)
                .getLayoutParams();
        int t = (int)(dpHeight-52)/2 - rlp.height/2;
        rlp.topMargin = t;
        popupView.findViewById(R.id.popup_vat_Insert).setLayoutParams(rlp);

        final EditText vatContainer = (EditText)popupView.findViewById(R.id.vat_group_insert);
        RecyclerView vat_recycler = (RecyclerView)popupView.findViewById(R.id.vat_value_recycler);
        vat_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayout.VERTICAL, false));
        VatAdapter vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, b, thisGrid, true);
        vat_recycler.setAdapter(vatAdapter);
        vat_recycler.setHasFixedSize(true);

        popupView.findViewById(R.id.popup_vat_Insert).setVisibility(VISIBLE);
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
        vat_recycler.addItemDecoration(divider);
        p.dismiss();
        setupDismissKeyboard(popupView);

        ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                int vat1;
                if(vatContainer.getText().toString().equals("")){
                    Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
                    vat1 = 0;
                }
                else{
                    String textVat = String.valueOf(vatContainer.getText().toString());
                    if(textVat.matches("[0-9]*\\.[0-9]*")){
                        float floatVat = Float.parseFloat(textVat);
                        floatVat = floatVat*100;
                        vat1 = (int) floatVat;
                    }
                    else
                        vat1 = Integer.parseInt(textVat);
                }
                if(!dbA.checkIfVatIsAdded(vat1) && vat1 != 0){
                    dbA.addVatValue(vat1);
                    vatContainer.setText("");
                    RecyclerView vat_recycler = (RecyclerView)popupView.findViewById(R.id.vat_value_recycler);
                    VatAdapter vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, b, thisGrid, true);
                    vat_recycler.setAdapter(vatAdapter);
                    vat_recycler.setHasFixedSize(true);
                    vat_recycler.setVisibility(VISIBLE);
                    popupView.findViewById(R.id.popup_vat_Insert).setVisibility(VISIBLE);
                    DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
                    divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
                    vat_recycler.addItemDecoration(divider);
                    vatAdapter.notifyDataSetChanged();

                }
                else if(dbA.checkIfVatIsAdded(vat1) && vat1 != 0){
                    b.setVat(vat1);

                    popupWindow.dismiss();
                }
                else if(vat1 == 0){
                    Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);

    }

}

