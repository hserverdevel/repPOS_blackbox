package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.MainActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.IconSeparator;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.KitchenPrinter;
import com.example.blackbox.model.PrinterModel;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.StatusInfoHolder;
import com.example.blackbox.model.Vat;
import com.example.blackbox.model.VatModel;
import com.utils.db.DatabaseAdapter;

import java.io.IOException;
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

public class GridAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter, AdapterView.OnItemSelectedListener  {
    private Context context;
    private GridAdapter thisGrid = this;
    private Resources resources;
    private ArrayList<ButtonLayout> buttons;
    public int previousCatID = 0;
    private int currentCatID = 0;
    private String previousCatTitle;
    public String currentCatTitle;
    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    public int deepnessLevel = 0;
    private int numberOfCategories;
    private int numberOfProducts;
    private int MAX_NUMBER_OF_CATEGORIES;
    private boolean keyboard_next_flag = false;
    private float density;
    private float dpHeight;
    private float dpWidth;
    private VatAdapter vatAdapter = null;
    private int vatValue = 0;
    private int vatId = -1;
    private boolean vatState = false;
    public void setVatState(boolean value){vatState = value;}
    public boolean getVatState(){return vatState;}
    public boolean colorState = false;
    public boolean getColorState(){
        return colorState;
    }
    public void setColorState(boolean s){
        colorState = s;
    }

    public int defaultColor = 0xFF000000;
    public int colorChosen = -1;
    private int definiteColor = defaultColor;
    public View chosenColorView;

    private ArrayList<String> icons;
    public String chosenIcon = "";
    private IconHolder chosenIconHolder = null;
    private boolean iconChanged = false;
    private boolean isSorting = false;
    private boolean isIconChosen = false;
    private boolean isColorChosen = false;
    private boolean colorSetupShown = false;
    private boolean iconSetupShown = false;

    private static final String[] SURPRISE = {"ffaad5","ee9090","f4f9a9","91e1ba","5cafb6"};
    private static final String[] FORESTY = {"045c2e","098243","70b576","a5df93","dce7db"};
    private static final String[] STONE_COLD = {"d9d3db","b5b9c2","6c6f7c","4a4e5d","213446"};
    private static final String[] SUNDOWN = {"f2a426","f57617","c01a1a","7a1e05","581504"};
    private static final String[] BASICS = {"84a944","f0d42c","4e91c2","ec463d","a40f7c"};
    private static final String[] GREY_RULES = {"494545","676363","8d8888","aba7a7","c7bebe"};
    private static final String[] SKYISH = {"9de1ff","66e6ff","40c0ff","0e92e9","0073b2"};
    private static final String[] NATURAL = {"7b801a","a8ae23","dde26d","ecefae","f6f7d9"};

    public final int MAX_DEEPNESS_LEVEL = 3;
    private final int MAX_NUMBER_OF_PRODUCTS = 64;

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

    public int returnCatIdBlackbox(){
        return buttons.get(buttons.size() - 2).getID();
    }

    public ButtonLayout returnProductForBlackBos(){
        return buttons.get(buttons.size() - 2);
    }

    public GridAdapter(Context c, ArrayList<ButtonLayout> arrayList, DatabaseAdapter dbA){
        context = c;
        buttons = arrayList;
        this.dbA = dbA;
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        resources = context.getResources();
        /**DISPLAY METRICS:  used to center the window in the screen**/
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;
        icons = new ArrayList<>();
        try {
            String[] i = context.getAssets().list("drawable_icons");
            for (String s: i
                    ) {
                icons.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //dbA.showData("button");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        switch(viewType){
           case -11:
                v = inflater.inflate(R.layout.gridview_plus_button, null);
                vh = new PlusButtonHolder(v);
                break;
            case -1:
                v = inflater.inflate(R.layout.gridview_subelement, null);
                vh = new SubButtonHolder(v);
                break;
            default:
                v = inflater.inflate(R.layout.gridview_element, null);
                vh = new ButtonHolder(v);
                break;
        }
        return vh;
    }

    @Override
    public int getItemViewType(int position){
        if(buttons.get(position).getID() == -11) return -11;
        else if(deepnessLevel > MAX_DEEPNESS_LEVEL)
            return -1; //sub button type
        else return 1;


    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ButtonLayout b = buttons.get(position);
        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(b.getColor());
        border.setStroke((int) (3*density), 0xFFC6C5C6); //gray border with full opacity
        /** Rounded corners for the buttons **/
        border.setCornerRadius(8*density);
        /** Rounded corners for internal image **/
        GradientDrawable img_border = new GradientDrawable();
        img_border.setStroke((int) (2*density), 0xFFC6C5C6);
        img_border.setCornerRadius(5*density);
        switch(getItemViewType(position)) {
            /**
             *  CASE: BUTTON
             *  DEPENDING ON THE BUTTON ID( ADD BUTTON OR OTHER BUTTON)
             *  THE LAYOUT PARAMS ARE SET TO THOSE SPECIFIED IN THE BUTTONLAYOUT OBJECT
             */
             case -11:
                PlusButtonHolder plusbutton = (PlusButtonHolder) holder;
                /** Main page case **/
                if(deepnessLevel<=MAX_DEEPNESS_LEVEL) {
                    plusbutton.image.setImageResource(getImageId(context, b.getImg()));
                    LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) plusbutton.image.getLayoutParams();
                    ll.gravity = Gravity.CENTER;
                    plusbutton.image.setLayoutParams(ll);
                    plusbutton.view.setLayoutParams(new LinearLayout.LayoutParams((int) (154*density), (int) (145*density)));
                    plusbutton.view.setBackground(border);
                    plusbutton.view.setTag(b);
                    plusbutton.view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firePopupWindow(v);
                        }
                    });
                }
                else{
                    /** deepest level case, button is smaller **/
                    plusbutton.image.setImageResource(getImageId(context, b.getImg()));
                    LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) plusbutton.image.getLayoutParams();
                    ll.gravity = Gravity.CENTER;
                    plusbutton.image.setLayoutParams(ll);
                    plusbutton.view.setLayoutParams(new LinearLayout.LayoutParams((int) (154*density), (int) (46*density)));
                    border.setStroke((int) (3*density), context.getColor(R.color.yellow)); // yellow border
                    plusbutton.view.setBackground(border);
                    plusbutton.view.setTag(b);
                    plusbutton.view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firePopupWindow(v);
                        }
                    });
                }
                break;
            case -1: //SUB CATEGORY CASE
                SubButtonHolder subbutton = (SubButtonHolder) holder;
                subbutton.title.setText(b.getTitle());
                /** If subtitle is null, hides subtitle text view and centers title **/
                if(!b.getSubTitle().equals("")) {
                    subbutton.subtitle.setVisibility(VISIBLE);
                    subbutton.subtitle.setText(b.getSubTitle());
                    LayoutParams rll = (LayoutParams)subbutton.title.getLayoutParams();
                    rll.setMargins(0, (int) (-1*density),0,0);
                    subbutton.title.setLayoutParams(rll);
                }
                else {
                    subbutton.subtitle.setVisibility(View.GONE);
                    LayoutParams rll = (LayoutParams) subbutton.title.getLayoutParams();
                    rll.setMargins(0, (int) (6*density), 0, 0);
                    subbutton.title.setLayoutParams(rll);
                }
                subbutton.view.setLayoutParams(new LayoutParams((int) (154*density), (int) (46*density)));
                border.setStroke((int) (3*density), context.getColor(R.color.yellow)); // yellow border
                subbutton.view.setBackground(border);
                subbutton.view.setTag(b);
                subbutton.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firePopupWindow(v);
                    }
                });

                break;

            default:
                ButtonHolder button = (ButtonHolder) holder;
                button.title.setText(b.getTitle());
                if(!b.getSubTitle().equals("")) {
                    button.subtitle.setVisibility(VISIBLE);
                    button.subtitle.setText(b.getSubTitle());
                    LayoutParams rll = (LayoutParams)button.title.getLayoutParams();
                    rll.setMargins(0, (int) (-4*density),0,0);
                    button.title.setLayoutParams(rll);
                }
                else {
                        button.subtitle.setVisibility(View.GONE);
                        LayoutParams rll = (LayoutParams) button.title.getLayoutParams();
                        rll.setMargins(0, (int) (4*density), 0, 0);
                        button.title.setLayoutParams(rll);
                }
                if(b.getImg().equals("")){
                    button.image2.setVisibility(GONE);
                    button.bigText.setVisibility(VISIBLE);
                    if(b.getTitle().length()>3) {
                        button.bigText.setText(b.getTitle().substring(0, 3).toUpperCase());
                    }else{
                        button.bigText.setText(b.getTitle().toUpperCase());

                    }
                }
                else {
                    button.image2.setVisibility(VISIBLE);
                    button.bigText.setVisibility(GONE);
                    try {
                        if(b.getImg()!=null)
                            button.image2.setImageDrawable(Drawable.createFromStream(context.getAssets().open("drawable_icons/"+b.getImg()),null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    button.bigText.setText(b.getTitle().substring(0,3).toUpperCase());
                }
                if(b.getCat()!=1 ){
                    border.setStroke((int) (3*density), context.getColor(R.color.yellow)); // yellow border
                    button.view.setBackground(border);
                    button.image2.setVisibility(VISIBLE);
                    button.bigText.setVisibility(GONE);
                    try {
                        button.image2.setImageDrawable(Drawable.createFromStream(context.getAssets().open("default_product_icon/producticon.png"),null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    border.setStroke((int) (3*density), context.getColor(R.color.gray)); // yellow border
                    button.view.setBackground(border);
                }


                button.view.setLayoutParams(new LayoutParams((int) (154*density), (int) (145*density)));
                button.view.setBackground(border);
                button.view.setTag(b);
                button.view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firePopupWindow(v);
                        }
                    });


            }
        }

    @Override
    public int getItemCount() {
        return buttons.size();
    }

    /**
     * Here is defined the method which handles the swapping movement performed thanks
     * to the ItemTouchHelperAdapter/Callback system
     */
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        boolean f = true;
            if (toPosition == buttons.size() - 1) f = false;
            else {
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(buttons, i, i + 1);
                    }
                    if(!StaticValue.blackbox) {
                        for (int i = fromPosition; i <= toPosition; i++) {
                            // apply changes in database
                            dbA.execOnDb("UPDATE button SET position = " + i + " WHERE id = " + buttons.get(i).getID());
                        }
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(buttons, i, i - 1);
                    }
                    if(!StaticValue.blackbox) {
                        for (int i = fromPosition; i >= toPosition; i--) {
                            // apply changes in database
                            //possibly with the right position number.....
                            dbA.execOnDb("UPDATE button SET position = " + (i + 1) + " WHERE id = " + buttons.get(i).getID());
                        }
                    }
                }
            }
            if (f) {
                notifyItemMoved(fromPosition, toPosition);
            }
        return true;
    }

    //execute db update from /moveButton in MainActivity
    public void swapButtonFunction(int fromPosition, int toPosition){
        for(int i=0; i<buttons.size(); i++){
            dbA.execOnDb("UPDATE button SET position = " + (i+1) + " WHERE id = " + buttons.get(i).getID());
        }

    }

    public void deleteVat(int id){
        vatAdapter.deleteVatFromServer(id);
        ArrayList<VatModel> newVats1 = dbA.fetchVatArrayByQuery();
        vatAdapter.setVats(newVats1);
        vatAdapter.notifyDataSetChanged();
    }

    public void setVatFromServer(ArrayList<VatModel> vats, View popupView) {
        vatAdapter.setVats(vats);
        vatAdapter.notifyDataSetChanged();
        if(popupView!=null) {
            EditText vatContainer = (EditText) popupView.findViewById(R.id.vat_group_insert);
            vatContainer.setText("");
            EditText percContainer = (EditText) popupView.findViewById(R.id.vat_perc_insert);
            percContainer.setText("");
        }
    }


    /**
     * Method to Override. Gets called when element gets swiped?
     */
    @Override
    public void onItemDismiss(int position) {
        //added because can't make an http request every time i move a button, this wait its end
        if(StaticValue.blackbox) {
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("toPosition", String.valueOf(position+1)));
            params.add(new BasicNameValuePair("buttonSize", String.valueOf(buttons.size() - 1)));
            params.add(new BasicNameValuePair("catID", String.valueOf(buttons.get(position).getCatID())));
            params.add(new BasicNameValuePair("buttonID", String.valueOf(buttons.get(position).getID())));
            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            params.add(new BasicNameValuePair("androidId", android_id));

            ((MainActivity) context).callHttpHandler("/moveButton", params);
        }
    }

    /** HOLDERS **/
    public static class ButtonHolder extends ViewHolder{
        public View view;
        public CustomTextView title;
        public CustomTextView subtitle;
        public RelativeLayout txtcontainer;
        public FrameLayout frame;
        public ImageView image2;
        public CustomTextView bigText;
        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            txtcontainer = (RelativeLayout)view.findViewById(R.id.text_container);
            title = (CustomTextView)view.findViewById(R.id.title);
            subtitle = (CustomTextView)view.findViewById(R.id.subtitle);
            image2 = (ImageView)view.findViewById(R.id.button_frame_img);
            bigText = (CustomTextView)view.findViewById(R.id.bigText_tv);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+title.getText().toString();}
    }
    public static class SubButtonHolder extends ViewHolder{
        public View view;
        public CustomTextView title;
        public CustomTextView subtitle;
        public RelativeLayout txtcontainer;
        public SubButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            txtcontainer = (RelativeLayout)view.findViewById(R.id.text_container);
            title = (CustomTextView)view.findViewById(R.id.title);
            subtitle = (CustomTextView)view.findViewById(R.id.subtitle);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+title.getText().toString();}
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

    /**
     * @param context
     * @param imageName : imagename WITHOUT its extension. If image is stored inside a folder inside "drawable"
     *                  then is must be specified while inserting in db( e.g. imagename = "folder/imagename" )
     *
     * Method returns the resource ID of the image specified, if exists
     */
    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    /**
     *  START OF DEFINITION OF METHODS CALLED AT ONCLICK()
     *
     *  Depending on which item has been clicked, firePopupWindow calls a specific method
     */
    private void firePopupWindow(View v){
        ButtonLayout b = (ButtonLayout)v.getTag();
        // Add Button
        if( b.getID() == -11){
            if(deepnessLevel<=MAX_DEEPNESS_LEVEL) addPopupWindow();
            else addProductOnlyPopupWindow(v);
        }
        // Category Button
        else if(b.getCat() == 1){
            catSettingsPopupWindow(v);
        }
        // Product Button
        else{
            prodSettingsPopupWindow(v);
        }
    }


    /**
     *
     *  NEW BUTTON
     *  Shows popup Window for choosing which button to create
     *
     */
    private int showProduct = 0;

    private void addPopupWindow(){
        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_button_add, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        //IT FUCKING WORKS!
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setupDismissKeyboard(popupView);

        //just to avoid crash
        vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, thisGrid, vatState);

        printerModel = "";
        Spinner spinner = (Spinner)popupView.findViewById(R.id.prodBarcode);
        Spinner spinnerCategory = (Spinner)popupView.findViewById(R.id.select_kitchen_printer);
        ArrayList<PrinterModel> spinnerList = new ArrayList<>();
        spinnerList.add(new PrinterModel("Select Printer"));
        ArrayList<KitchenPrinter> kitchenPrinters = dbA.selectAllKitchenPrinter();
        if(kitchenPrinters.size()>0) {
            for(KitchenPrinter kitchenPrinter : kitchenPrinters) {
                spinnerList.add(new PrinterModel(kitchenPrinter.getName()));
            }
        }

        ArrayAdapter<PrinterModel> adapter = new ArrayAdapter<PrinterModel>(context, R.layout.spinner_dropdown, spinnerList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Closure Button
        final ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }});
        /*
         * CATEGORY BUTTON AND CATEGORY SETUP WINDOW
         *
         * when category button gets hit, the category setup window gets displayed
         */
        Button btnCategory = (Button)popupView.findViewById(R.id.category);

        //btnCategory.setText(getCurrentLevelName());
        btnCategory.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.ButtonTypeChooser).setVisibility(View.GONE);
                popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                /** Changing texts according to the current dept level **/
                ((CustomTextView)popupView.findViewById(R.id.category_tv)).setText(getCurrentLevelName());
                ((CustomButton)popupView.findViewById(R.id.goToCategory)).setText(resources.getString(R.string.goTo_,
                        resources.getString(getCurrentLevelName())));
                ((CustomButton)popupView.findViewById(R.id.deleteCategory)).setText(resources.getString(R.string.delete_,
                        resources.getString(getCurrentLevelName())));
                popupView.findViewById(R.id.goToCategory).setElevation(0.0f);
                popupView.findViewById(R.id.deleteCategory).setElevation(0.0f);
                popupView.findViewById(R.id.catImgChooser).setElevation(0.0f);
                popupView.findViewById(R.id.catColorPicker).setElevation(0.0f);

                ImageButton credit =  popupView.findViewById(R.id.fidelity_credit_checkbox);
                ImageButton discount = popupView.findViewById(R.id.fidelity_discount_checkbox);
                credit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        credit.setActivated(!credit.isActivated());
                    }
                });

                discount.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        discount.setActivated(!discount.isActivated());
                    }
                });


                // Icon pick
                CustomButton iconPick = (CustomButton)popupView.findViewById(R.id.catImgChooser);
                iconPick.findViewById(R.id.catImgChooser).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupView.findViewById(R.id.CategorySetup).setVisibility(GONE);
                        popupView.findViewById(R.id.iconsContainer).setVisibility(VISIBLE);
                        iconChanged = false;
                        setupIconSelector(popupView.findViewById(R.id.iconsContainer));
                        iconSetupShown = true;

                        // dismiss button is set to "go back" and at onClick resets its behavior to the previous one
                        btnDismiss.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(iconSetupShown) {
                                    popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                    popupView.findViewById(R.id.iconsContainer).setVisibility(GONE);
                                    v.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            popupWindow.dismiss();
                                            iconSetupShown = false;
                                        }
                                    });
                                    iconSetupShown = false;
                                }
                                else
                                    popupWindow.dismiss();
                            }
                        });
                    }
                });
                //Colors
                popupView.findViewById(R.id.catColorPicker).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupView.findViewById(R.id.CategorySetup).setVisibility(GONE);
                        popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                        setupColors(popupView.findViewById(R.id.colors_container), defaultColor);
                        colorState = true;
                        colorSetupShown = true;

                        // dismiss button is set to "go back" and at onClick resets its behavior to the previous one
                        btnDismiss.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(colorSetupShown){
                                    popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                                    v.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            popupWindow.dismiss();
                                            colorState = false;
                                            colorSetupShown = false;
                                        }
                                    });
                                    colorSetupShown = false;
                                }
                                else
                                    popupWindow.dismiss();
                            }
                        });
                    }
                });
                // Ok button onClick
                ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
                btnOk.setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        getCurrentLevelMaxData();
                        if(numberOfCategories>=MAX_NUMBER_OF_CATEGORIES){
                            Toast.makeText(context,resources.getString(R.string.max_number_of__reached,
                                    resources.getString(getCurrentLevelName())), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ((CustomTextView) popupView.findViewById(R.id.category_tv)).setText(getCurrentLevelName());
                            String title = ((CustomEditText) popupView.findViewById(R.id.catTitleInsert)).getText().toString();
                            if(!title.equals("")) {
                                int color = defaultColor;
                                if(colorChosen != -1){
                                    color = colorChosen;
                                }
                                String subtitle = ((EditText) popupView.findViewById(R.id.catSubTitleInsert)).getText().toString();
                                if(colorSetupShown){
                                    popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                                    colorSetupShown = false;
                                }
                                else if(iconSetupShown){
                                    popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                    popupView.findViewById(R.id.iconsContainer).setVisibility(GONE);
                                    iconSetupShown = false;
                                }
                                else{
                                    int printerId = -1;
                                    if(!printerModel.equals("")){
                                        printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                                    }

                                    if(StaticValue.blackbox){
                                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                        params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                                        params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                                        params.add(new BasicNameValuePair("imgName", chosenIcon));
                                        params.add(new BasicNameValuePair("color", String.valueOf(color)));
                                        params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                                        // TODO the vat value should not be hardcoded
                                        params.add(new BasicNameValuePair("vat", "1"));
                                        params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                                        params.add(new BasicNameValuePair("isCat", "1"));
                                        params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                                        params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                                        params.add(new BasicNameValuePair("fidelityCredit", String.valueOf(credit.isActivated())));
                                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                        params.add(new BasicNameValuePair("androidId", android_id));

                                        myPopupWindow = popupWindow;
                                        ((MainActivity) context).callHttpHandler("/insertButton", params);
                                    }else {
                                        dbA.execOnDb("INSERT INTO button (title, subtitle, img_name, color, position, catID, isCat, printer) " +
                                                "VALUES(\"" + title.replaceAll("'", "\'") +
                                                "\",\"" + subtitle.replaceAll("'", "\'") + "\",\"" + chosenIcon + "\"," + color + "," + buttons.size() + "," + currentCatID + ", 1, " + printerId + ")");
                                        getCurrentCatButtonSet(currentCatID);
                                        colorChosen = -1;
                                        chosenColorView = null;
                                        chosenIcon = "";
                                        popupWindow.dismiss();
                                    }
                                }
                            }
                            else if(isIconChosen){
                                popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                popupView.findViewById(R.id.iconsContainer).setVisibility(GONE);
                                isIconChosen = false;
                                iconSetupShown = false;
                            }
                            else if(isColorChosen){
                                popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                                isColorChosen = false;
                                colorSetupShown = false;
                            }
                            else{
                                if(colorSetupShown){
                                    popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                                    colorSetupShown = false;
                                }
                                else if(iconSetupShown){
                                    popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                                    popupView.findViewById(R.id.iconsContainer).setVisibility(GONE);
                                    iconSetupShown = false;
                                }
                                else
                                    Toast.makeText(context,R.string.insert_title, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                // goToCategory button Listener
                CustomButton goToCategory= (CustomButton)popupView.findViewById(R.id.goToCategory);
                goToCategory.setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        v.setElevation(0.0f);
                        getCurrentLevelMaxData();
                        if(numberOfCategories>=MAX_NUMBER_OF_CATEGORIES){
                            Toast.makeText(context,resources.getString(R.string.max_number_of__reached,
                                    resources.getString(getCurrentLevelName())), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.i("RTESTO 2", getCurrentLevelName() + "");

                            ((CustomTextView) popupView.findViewById(R.id.category_tv)).setText(getCurrentLevelName());
                            String title = ((CustomEditText) popupView.findViewById(R.id.catTitleInsert)).getText().toString();
                            if(!title.equals("")) {
                                int color = defaultColor;
                                if(colorChosen >= 0){
                                    color = colorChosen;
                                    colorChosen = -1;
                                    chosenColorView = null;
                                }
                                int printerId = -1;
                                if(!printerModel.equals("")){
                                    printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                                }
                                String subtitle = ((EditText) popupView.findViewById(R.id.catSubTitleInsert)).getText().toString();
                                if(StaticValue.blackbox){
                                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                    params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                                    params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                                    params.add(new BasicNameValuePair("imgName", chosenIcon));
                                    params.add(new BasicNameValuePair("color", String.valueOf(color)));
                                    params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                                    params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                                    params.add(new BasicNameValuePair("isCat", String.valueOf("1")));
                                    params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                                    params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                                    params.add(new BasicNameValuePair("fidelityCredit", String.valueOf(credit.isActivated())));
                                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                    params.add(new BasicNameValuePair("androidId", android_id));
                                    myPopupWindow = popupWindow;
                                    ((MainActivity) context).callHttpHandler("/insertButton2", params);
                                }else {

                                    dbA.execOnDb("INSERT INTO button (title, subtitle, img_name, color, position, catID, isCat, printer) " +
                                            "VALUES(\"" + title.replaceAll("'", "\'") +
                                            "\",\"" + subtitle.replaceAll("'", "\'") +
                                            "\",\"" + chosenIcon + "\"," + color + "," + buttons.size() + "," + currentCatID + ", 1, " + printerId + ")");


                                    if (deepnessLevel + 1 <= MAX_DEEPNESS_LEVEL + 1) {
                                        deepnessLevel += 1;
                                        getCurrentCatButtonSet(currentCatID);
                                        int catID = buttons.get(buttons.size() - 2).getID();
                                        goToCategory(currentCatID, catID, title);
                                    }
                                    popupWindow.dismiss();
                                }
                            }
                            else Toast.makeText(context,R.string.insert_title, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                CustomButton deleteCategory = (CustomButton)popupView.findViewById(R.id.deleteCategory);
                deleteCategory.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setElevation(0.0f);
                        Toast.makeText(context,R.string.category_has_not_been_created_yet, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        /*
         * PRODUCT BUTTON AND PRODUCT SETUP WINDOW
         *
         * same as category button
         */
        showProduct = 0;
        Button btnProduct = (Button)popupView.findViewById(R.id.product);

        btnProduct.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.ButtonTypeChooser).setVisibility(View.GONE);
                popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                CustomButton vatButton = (CustomButton)popupView.findViewById(R.id.prodTaxInsert);

                popupView.findViewById(R.id.product_tv).setOnClickListener(new OnClickListener() {
                    @Override
                    //we're creating a new button
                    public void onClick(View v) {
                        showProduct =0;
                        popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));
                        popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));

                    }
                });

                popupView.findViewById(R.id.combo_tv).setOnClickListener(new OnClickListener() {
                    @Override
                    //we're creating a new button
                    public void onClick(View v) {
                        showProduct =2;

                        popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));
                        popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));

                    }
                });

                ImageButton discount =  popupView.findViewById(R.id.fidelity_discount_checkbox_prod);
                discount.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        discount.setActivated(!discount.isActivated());
                    }
                });

                popupView.findViewById(R.id.prodButtonColor).setOnClickListener(new OnClickListener() {
                    @Override
                    //we're creating a new button
                    public void onClick(View v) {
                        if(!colorState){
                            popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                            popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                            setupColors(popupView.findViewById(R.id.colors_container), defaultColor);
                            colorState = true;
                        }
                        else
                            colorState = false;
                    }
                });

                //vat insert popup
                vatButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                        popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);
                        popupVatWindow(popupView, popupWindow);
                        vatState = true;
                    }
                });

                // Ok button onClick
                ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
                btnOk.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //I'm choosing (or not) a color
                        if(colorState && !vatState){
                            okBehaviourForSettingColor(popupView);
                        }
                        //I'm choosing (or not) a vat value (also adding new values in db)
                        else if(vatState && !colorState){
                            vatAdapter.okBehaviourInVatAdapter();
                            vatState = false;
                            vatId = vatAdapter.getSelectedVatId();
                            int vat1 = dbA.fetchVatByIdQuery(vatId);
                            if(vat1 != 0)
                                vatButton.setText(""+vat1);
                        }
                        //I'm adding a new Product
                        else if(!vatState && !colorState){
                            getCurrentLevelMaxData();
                            okBehaviourInProductSetting(popupView, popupWindow);
                        }
                        //Something's wrong
                        else if(vatState && colorState)
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });

                ImageButton btnKill = (ImageButton)popupView.findViewById(R.id.kill);
                btnKill.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(colorState){
                            popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                            popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                            colorState = false;
                        }
                        else if(vatState){
                            vatAdapter.dismissBehaviourInProductSettings(null);
                            vatState = false;
                        }
                        else if(!vatState && !colorState){
                            popupWindow.dismiss();
                        }
                    }
                });

                CustomButton btnModifiers = (CustomButton)popupView.findViewById(R.id.goToModifiers);
                btnModifiers.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getCurrentLevelMaxData();
                        vatId = vatAdapter.getSelectedVatId();
                        int vat1 = dbA.fetchVatByIdQuery(vatId);
                        if(numberOfProducts>=MAX_NUMBER_OF_PRODUCTS){
                            Toast toast = Toast.makeText(context,R.string.max_number_of_products_reached, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else if(((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().trim().equals(""))
                            { Toast.makeText(context,R.string.insert_price, Toast.LENGTH_SHORT).show(); }

                        else if ( Float.parseFloat( ((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString() ) <= 0.01 )
                            { Toast.makeText(context, R.string.price_not_zero, Toast.LENGTH_LONG).show(); }

                        else{
                            String title = ((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString();
                            if(title.equals(""))
                                Toast.makeText(context,R.string.insert_title, Toast.LENGTH_SHORT).show();
                            else {
                                String subtitle = ((EditText) popupView.findViewById(R.id.prodSubTitleInsert)).getText().toString();
                                float price = Float.parseFloat(((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString());

                                int color = defaultColor;
                                if(colorChosen != -1){
                                    color = colorChosen;
                                    colorChosen = -1;
                                    chosenColorView = null;
                                }
                                int printerId = -1;
                                if(!printerModel.equals("")){
                                    printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                                }

                                double cValue = 0;
                                String creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input)).getText().toString();
                                if(!creditValue.equals("")){
                                    cValue = Double.valueOf(creditValue);
                                }

                                if(StaticValue.blackbox){
                                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                    params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                                    params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                                    params.add(new BasicNameValuePair("color", String.valueOf(color)));
                                    params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                                    params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                                    params.add(new BasicNameValuePair("isCat", String.valueOf(showProduct)));
                                    params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                                    params.add(new BasicNameValuePair("price", String.valueOf(price)));
                                    params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                                    params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                                    params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                    params.add(new BasicNameValuePair("androidId", android_id));
                                    myPopupWindow = popupWindow;
                                    ((MainActivity) context).callHttpHandler("/insertButton3", params);
                                }else {


                                    dbA.queryToDb("INSERT INTO button (title, subtitle, img_name, color, position, " +
                                            "price, vat, catID, isCat, printer) " +
                                            "VALUES(\"" + title.replaceAll("'", "\'") +
                                            "\",\"" + subtitle.replaceAll("'", "\'") +
                                            "\",\"\"," + color + "," + buttons.size() +
                                            "," + price + "," + vat1 + "," + currentCatID + ", 0, " + printerId + ")");
                                    getCurrentCatButtonSet(currentCatID);
                                    StatusInfoHolder sh = new StatusInfoHolder();
                                    sh.setCurrent_category_id(currentCatID);
                                    sh.setDept_level(deepnessLevel);
                                    sh.setPrevious_category_title(currentCatTitle);
                                    sh.setPrevious_category_id(previousCatID);
                                    sh.setCurrent_product(buttons.get(buttons.size() - 2));
                                    ((MainActivity) context).setGridStatus(sh);
                                    ((MainActivity) context).switchView(MainActivity.MODIFIERS_VIEW, buttons.get(buttons.size() - 2).getID());
                                    popupWindow.dismiss();
                                }
                            }
                        }
                    }
                });
                CustomButton deleteProduct = (CustomButton)popupView.findViewById(R.id.deleteProduct);
                deleteProduct.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setElevation(0.0f);
                        Toast.makeText(context,R.string.product_has_not_been_created_yet, Toast.LENGTH_SHORT).show();
                    }
                });

                CustomButton barcodeButton = (CustomButton)popupView.findViewById(R.id.read_barcode);
                barcodeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //recursively deleting buttons, dialog with YES/NO will show up
                        v.setElevation(0.0f);
                        final View dialogView = layoutInflater.inflate(R.layout.barcode_input_dialog, null);
                        final PopupWindow popupDialog = new PopupWindow(
                                dialogView,
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                        ((MainActivity)context).setBarcodeShow(true);

                        dialogView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((MainActivity)context).setBarcodeShow(false);
                                popupDialog.dismiss();

                            }
                        });

                        dialogView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((MainActivity)context).setBarcodeShow(false);
                                String myBarcode = ((CustomEditText)dialogView.findViewById(R.id.single_input)).getText().toString();
                                if(myBarcode!=null && !myBarcode.equals(""))
                                    ((CustomButton)popupView.findViewById(R.id.read_barcode)).setText(myBarcode);
                                popupDialog.dismiss();

                            }
                        });

                        popupDialog.setFocusable(true);
                        popupDialog.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
                    }
                });


            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    /**
     *  Shows popup window for creating new product only ( triggered when in deepest level )
     */
    public void addProductOnlyPopupWindow(View v){
        final ButtonLayout b = (ButtonLayout) v.getTag();
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_button_add, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        //IT FUCKING WORKS!
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // PRODUCT SETUP WINDOW
        popupView.findViewById(R.id.ButtonTypeChooser).setVisibility(View.GONE);
        popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);

        LayoutParams rll = (LayoutParams)popupView.findViewById(R.id.ProductSetup).getLayoutParams();
        int tot = (int) ((int)(dpHeight - 52)/2 - (379*density)/2);
        rll.setMargins(0,tot,0,0);
        popupView.findViewById(R.id.ProductSetup).setLayoutParams(rll);
        setupDismissKeyboard(popupView);

        //SET PRINTER SPINNER
        printerModel = "";
        Spinner spinner = (Spinner)popupView.findViewById(R.id.prodBarcode);

        ArrayList<PrinterModel> spinnerList = new ArrayList<>();
        spinnerList.add(new PrinterModel("Select Printer"));
        ArrayList<KitchenPrinter> kitchenPrinters = dbA.selectAllKitchenPrinter();
        if(kitchenPrinters.size()>0) {
            for(KitchenPrinter kitchenPrinter : kitchenPrinters) {
                spinnerList.add(new PrinterModel(kitchenPrinter.getName()));
            }
        }

        ArrayAdapter<PrinterModel> adapter = new ArrayAdapter<PrinterModel>(context, R.layout.spinner_dropdown, spinnerList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(b.getPrinterId()!=-1) spinner.setSelection(1);

        // GETTING View elements
        EditText title = (EditText)popupView.findViewById(R.id.prodTitleInsert);
        EditText subtitle = (EditText)popupView.findViewById(R.id.prodSubTitleInsert);
        EditText price = (EditText)popupView.findViewById(R.id.prodPriceInsert);
        CustomButton myVatButton = (CustomButton)popupView.findViewById(R.id.prodTaxInsert);

        title.setText(b.getTitle());
        subtitle.setText(b.getSubTitle());
        price.setText(new String("" + b.getPrice()));
        if(dbA.fetchVatButtonValue(b.getID()) != 0){
            int vatValue = dbA.fetchVatByIdQuery(b.getVat());
            myVatButton.setText(""+vatValue);
            //myVatButton.setText(dbA.fetchVatButtonValue(b.getID()) + "%");
        }
        setupDismissKeyboard(popupView);

        showProduct = 0;
        popupView.findViewById(R.id.product_tv).setOnClickListener(new OnClickListener() {
            @Override
            //we're creating a new button
            public void onClick(View v) {
                showProduct =0;
                popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));
                popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));

            }
        });

        popupView.findViewById(R.id.combo_tv).setOnClickListener(new OnClickListener() {
            @Override
            //we're creating a new button
            public void onClick(View v) {
                showProduct =2;

                popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));
                popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));

            }
        });

        ImageButton discount =  popupView.findViewById(R.id.fidelity_discount_checkbox_prod);
        discount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                discount.setActivated(!discount.isActivated());
            }
        });

        // Closure Button
        final ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }});

        popupView.findViewById(R.id.prodButtonColor).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vatAdapter == null || !vatAdapter.getVatState()){
                    popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                    popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                    setupColors(popupView.findViewById(R.id.colors_container), b.getColor());
                    colorState = true;
                }
                else if(vatAdapter.getVatState() && colorState){
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    //setupColors(popupView.findViewById(R.id.colors_container));
                    colorState = false;
                }
                else if(vatAdapter.getVatState() && !colorState){
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(GONE);
                    popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                    colorState = true;
                }
                popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                setupColors(popupView.findViewById(R.id.colors_container), b.getColor());
                colorState = true;

                // dismiss button is set to "go back" and at onClick resets its behavior to the previous one
                btnDismiss.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                        popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                        colorState = false;
                        v.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });
                    }
                });
            }
        });

        //vat insert popup
        myVatButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!colorState) {
                    popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);

                    popupVatWindow(popupView, popupWindow, b);
                    vatState = true;
                }
                else{
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);

                    popupVatWindow(popupView, popupWindow, b);
                    vatState = true;
                }
            }
        });

        myVatButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(dbA.fetchVatButtonValue(b.getID()) != -1){
                    myVatButton.setText("");
                    notifyDataSetChanged();
                    if(vatAdapter != null)
                        vatAdapter.setSelectedVatId(-1);
                }
                return true;
            }
        });

        // Ok button onClick
        ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //TODO CONTOLLARE SE QUA CI ARRIVO ANCORA, NON CREDO PERCHE CI ARRIVO DA okBehaviourInProductCreatingProduct
                if(!colorState)
                {
                    int vat1 = 0;
                    if(vatAdapter != null){
                        vatId = vatAdapter.getSelectedVatId();
                        vat1 = dbA.fetchVatByIdQuery(vatId);
                        if(vat1 != 0)
                            b.setVat(vat1);
                    }

                    if ( ((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().trim().equals(""))
                        { Toast.makeText(context,R.string.insert_price, Toast.LENGTH_SHORT).show(); }

                    else if ( Float.parseFloat( ((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString() ) <= 0.01 )
                        { Toast.makeText(context, R.string.price_not_zero, Toast.LENGTH_LONG).show(); }

                    else if (((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString().equals(""))
                        Toast.makeText(context, R.string.insert_title, Toast.LENGTH_SHORT).show();

                    else if (!((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString().equals("")
                            && !((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals("")){
                        String title = ((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString();
                        String subtitle = ((EditText)popupView.findViewById(R.id.prodSubTitleInsert)).getText().toString();
                        float price = Float.valueOf(((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString());

                        int color = b.getColor();
                        if(colorChosen != -1){
                            color = colorChosen;
                            colorChosen = -1;
                            chosenColorView = null;
                            b.setColor(color);
                        }
                        int printerId = -1;
                        if(!printerModel.equals("")){
                            printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                        }
                        String barcode = ((CustomButton)popupView.findViewById(R.id.read_barcode)).getText().toString();

                        double cValue = 0;
                        String creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input)).getText().toString();
                        if(!creditValue.equals("")){
                            cValue = Double.valueOf(creditValue);
                        }

                        if(StaticValue.blackbox){
                            if (b.getID() > 0) {
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                                params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                                params.add(new BasicNameValuePair("color", String.valueOf(color)));
                                params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                                params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                                params.add(new BasicNameValuePair("isCat", String.valueOf(showProduct)));
                                params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                                params.add(new BasicNameValuePair("price", String.valueOf(price)));
                                params.add(new BasicNameValuePair("vat", String.valueOf(vatId)));
                                params.add(new BasicNameValuePair("barcode", barcode));
                                params.add(new BasicNameValuePair("id", String.valueOf(b.getID())));
                                params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                                params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));

                                myPopupWindow = popupWindow;
                                ((MainActivity) context).callHttpHandler("/updateButton2", params);

                            } else {
                                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                                params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                                params.add(new BasicNameValuePair("color", String.valueOf(color)));
                                params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                                params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                                params.add(new BasicNameValuePair("isCat", String.valueOf("0")));
                                params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                                params.add(new BasicNameValuePair("price", String.valueOf(price)));
                                params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                                params.add(new BasicNameValuePair("barcode", barcode));
                                params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                                params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                params.add(new BasicNameValuePair("androidId", android_id));
                                myPopupWindow = popupWindow;
                                ((MainActivity) context).callHttpHandler("/insertButton4", params);

                            }


                        }else {


                            if (b.getID() > 0) {
                                dbA.execOnDb("UPDATE button SET title = \"" + title.replaceAll("'", "\'") +
                                        "\", subtitle = \"" + subtitle.replaceAll("'", "\'") +
                                        "\", img_name = \"\", color = " + color + ", " +
                                        "barcode='" + barcode + "'," +
                                        "price = " + price + ", vat = " + vat1 + ", printer = " + printerId +
                                        " WHERE " + DatabaseAdapter.KEY_ID + " = " + b.getID() + ";");
                            } else {
                                dbA.queryToDb("INSERT INTO button (title, subtitle, img_name, color, position, " +
                                        "price, vat, catID, isCat, printer, barcode) " +
                                        "VALUES(\"" + title.replaceAll("'", "\'") +
                                        "\",\"" + subtitle.replaceAll("'", "\'") +
                                        "\",\"\"," + color + "," + buttons.size() +
                                        "," + price + "," + vat1 + "," + currentCatID + ", 0, " + printerId + ", '" + barcode + "')");
                            }


                            getCurrentCatButtonSet(currentCatID);
                            popupWindow.dismiss();
                        }
                    }
                }
                else{
                    Toast.makeText(context, R.string.color_set, Toast.LENGTH_SHORT).show();
                    popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    colorState = false;
                }
            }
        });

        // goToModifiers
        CustomButton btnOpenModifiers = (CustomButton)popupView.findViewById(R.id.goToModifiers);
        btnOpenModifiers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int vat1 = 0;
                if(vatAdapter != null){
                    vatId = vatAdapter.getSelectedVatId();
                    vat1 = dbA.fetchVatByIdQuery(vatId);
                    if(vat1 != 0)
                        b.setVat(vat1);
                }else{
                    if(b.getVat()!=0)
                        vat1 = dbA.fetchVatByIdQuery(b.getVat());
                }
                if (((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals(""))
                    { Toast.makeText(context,R.string.insert_price, Toast.LENGTH_SHORT).show(); }
                else if ( Float.parseFloat( ((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString() ) <= 0.01 )
                    { Toast.makeText(context, R.string.price_not_zero, Toast.LENGTH_LONG).show(); }
                else {
                    String title = ((EditText) popupView.findViewById(R.id.prodTitleInsert)).getText().toString();
                    String subtitle = ((EditText) popupView.findViewById(R.id.prodSubTitleInsert)).getText().toString();
                    float price = Float.parseFloat(((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString());

                    int color = b.getColor();
                    if (colorChosen != -1) {
                        color = colorChosen;
                        colorChosen = -1;
                        chosenColorView = null;
                        b.setColor(color);
                    }

                    double cValue = 0;
                    String creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input)).getText().toString();
                    if(!creditValue.equals("")){
                        cValue = Double.valueOf(creditValue);
                    }

                    if(StaticValue.blackbox){
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                        params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                        params.add(new BasicNameValuePair("color", String.valueOf(color)));
                        params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                        params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                        params.add(new BasicNameValuePair("isCat", String.valueOf(showProduct)));
                        params.add(new BasicNameValuePair("printerId", String.valueOf(-1)));
                        params.add(new BasicNameValuePair("price", String.valueOf(price)));
                        params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                        params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                        params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));
                        myPopupWindow = popupWindow;
                        ((MainActivity) context).callHttpHandler("/insertButton3", params);
                    }else {


                        dbA.execOnDb("UPDATE button SET title = \"" + title.replaceAll("'", "\'") +
                                "\", subtitle = \"" + subtitle.replaceAll("'", "\'") +
                                "\", img_name = \"\", color = " + color + ", " +
                                "price = " + price + ", vat =" + vat1 +
                                " WHERE " + DatabaseAdapter.KEY_ID + " = " + b.getID() + ";");
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
                }
            }
        });

        // DELETE
        CustomButton btnDelete = (CustomButton)popupView.findViewById(R.id.deleteProduct);
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //recursively deleting buttons, dialog with YES/NO will show up
                v.setElevation(0.0f);
                final View dialogView = layoutInflater.inflate(R.layout.yes_no_dialog, null);
                final PopupWindow popupDialog = new PopupWindow(
                        dialogView,
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                ((CustomTextView) dialogView.findViewById(R.id.delete_window))
                        .setText(resources.getString(R.string.you_are_deleting__product, b.getTitle().toUpperCase()));
                ((CustomButton) dialogView.findViewById(R.id.delete_button))
                        .setText(R.string.deleteProduct);
                dialogView.findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(StaticValue.blackbox){
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("id", String.valueOf(b.getID())));
                            params.add(new BasicNameValuePair("currentCatID", String.valueOf(currentCatID)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myPopupWindow = popupWindow;
                            myPopupDialog = popupDialog;
                            ((MainActivity) context).callHttpHandler("/deleteButton", params);
                        } else {
                            dbA.deleteButton(b);
                            getCurrentCatButtonSet(currentCatID);
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
        });

        // BARCODE
        CustomButton barcodeButton = (CustomButton)popupView.findViewById(R.id.read_barcode);
        barcodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //recursively deleting buttons, dialog with YES/NO will show up
                v.setElevation(0.0f);
                final View dialogView = layoutInflater.inflate(R.layout.barcode_input_dialog, null);
                final PopupWindow popupDialog = new PopupWindow(
                        dialogView,
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                ((MainActivity)context).setBarcodeShow(true);

                dialogView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)context).setBarcodeShow(false);
                        popupDialog.dismiss();

                    }
                });

                dialogView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)context).setBarcodeShow(false);
                        String myBarcode = ((CustomEditText)dialogView.findViewById(R.id.single_input)).getText().toString();
                        if(myBarcode!=null && !myBarcode.equals(""))
                            ((CustomButton)popupView.findViewById(R.id.read_barcode)).setText(myBarcode);
                        popupDialog.dismiss();

                    }
                });

                popupDialog.setFocusable(true);
                popupDialog.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
            }
        });






        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }
    /**
     *
     *  CATEGORY
     *  Popup window that will show up for CATEGORY settings
     *
     *
     */

    private void catSettingsPopupWindow(final View grid_element){
        final ButtonLayout b = (ButtonLayout)grid_element.getTag();
        final LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_button_add, null);
        popupView.findViewById(R.id.ButtonTypeChooser).setVisibility(View.GONE);
        popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        EditText title = (CustomEditText)popupView.findViewById(R.id.catTitleInsert);
        EditText subtitle = (EditText)popupView.findViewById(R.id.catSubTitleInsert);
        title.setText(b.getTitle());
        subtitle.setText(b.getSubTitle());
        setupDismissKeyboard(popupView);

        ((CustomTextView)popupView.findViewById(R.id.category_tv)).setText(getCurrentLevelName());
        ((CustomButton)popupView.findViewById(R.id.goToCategory)).setText(
                resources.getString(R.string.goTo_, resources.getString(getCurrentLevelName())));
        ((CustomButton)popupView.findViewById(R.id.deleteCategory)).setText(
                resources.getString(R.string.delete_, resources.getString(getCurrentLevelName())));
        ((CustomButton)popupView.findViewById(R.id.catImgChooser)).setText(R.string.selectIcon);
        ((CustomButton)popupView.findViewById(R.id.catColorPicker)).setText(R.string.buttonColor);
        popupView.post(new Runnable() {
            @Override
            public void run() {
                popupView.findViewById(R.id.goToCategory).setElevation(0.0f);
                popupView.findViewById(R.id.deleteCategory).setElevation(0.0f);
                popupView.findViewById(R.id.catImgChooser).setElevation(0.0f);
                popupView.findViewById(R.id.catColorPicker).setElevation(0.0f);
            }
        });

        popupView.findViewById(R.id.catImgChooser).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        ImageButton credit =  popupView.findViewById(R.id.fidelity_credit_checkbox);
        ImageButton discount = popupView.findViewById(R.id.fidelity_discount_checkbox);
        credit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                credit.setActivated(!credit.isActivated());
            }
        });
        if(b.getFidelity_credit()==1)
            credit.setActivated(true);

        discount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                discount.setActivated(!discount.isActivated());
            }
        });

        if(b.getFidelity_discount()==1)
            discount.setActivated(true);


        printerModel = "";
        Spinner spinner = (Spinner)popupView.findViewById(R.id.select_kitchen_printer);
        ArrayList<PrinterModel> spinnerList = new ArrayList<>();
        spinnerList.add(new PrinterModel("Select Printer"));
        ArrayList<KitchenPrinter> kitchenPrinters = dbA.selectAllKitchenPrinter();
        if(kitchenPrinters.size()>0) {
            for(KitchenPrinter kitchenPrinter : kitchenPrinters) {
                spinnerList.add(new PrinterModel(kitchenPrinter.getName()));
            }
        }
        ArrayAdapter<PrinterModel> adapter = new ArrayAdapter<PrinterModel>(context, R.layout.spinner_dropdown, spinnerList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        if(b.getPrinterId()!=-1){
            for(int i=0; i < kitchenPrinters.size(); i++){
                if(b.getPrinterId()==kitchenPrinters.get(i).getId())
                    spinner.setSelection(i+1);
            }
        }


        // Closure Button
        final ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }});

        // Icon pick
        CustomButton iconPick = (CustomButton)popupView.findViewById(R.id.catImgChooser);
        iconPick.findViewById(R.id.catImgChooser).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.CategorySetup).setVisibility(GONE);
                popupView.findViewById(R.id.iconsContainer).setVisibility(VISIBLE);
                chosenIcon = b.getImg();
                iconChanged = false;
                setupIconSelector(popupView.findViewById(R.id.iconsContainer));

                // dismiss button is set to "go back" and at onClick resets its behavior to the previous one
                btnDismiss.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                        popupView.findViewById(R.id.iconsContainer).setVisibility(GONE);
                        v.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });
                    }
                });
            }
        });
        popupView.findViewById(R.id.catColorPicker).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.findViewById(R.id.CategorySetup).setVisibility(GONE);
                popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                setupColors(popupView.findViewById(R.id.colors_container), b.getColor());

                // dismiss button is set to "go back" and at onClick resets its behavior to the previous one
                btnDismiss.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(colorState){
                            popupView.findViewById(R.id.CategorySetup).setVisibility(VISIBLE);
                            popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                            v.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    popupWindow.dismiss();
                                }
                            });
                        }
                        else
                            popupWindow.dismiss();
                    }
                });
            }
        });

        // Ok button onClick
        ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                String title = ((CustomEditText)popupView.findViewById(R.id.catTitleInsert)).getText().toString();
                String subtitle = ((EditText)popupView.findViewById(R.id.catSubTitleInsert)).getText().toString();
                int color = b.getColor();
                if(colorChosen != -1){
                    color = colorChosen;
                    colorChosen = -1;
                    chosenColorView = null;
                    b.setColor(color);
                }
                int printerId = -1;
                if(!printerModel.equals("")){
                    printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                }

                String icon;
                if(iconChanged) icon = chosenIcon;
                else icon = b.getImg();
                if(StaticValue.blackbox){
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                    params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                    params.add(new BasicNameValuePair("color", String.valueOf(color)));
                    params.add(new BasicNameValuePair("imgName",icon));
                    params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                    params.add(new BasicNameValuePair("id",String.valueOf(b.getID())));
                    params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                    // TODO params.add(new BasicNameValuePair("fidelityCredit", String.valueOf(credit.isActivated())));
                    params.add(new BasicNameValuePair("fidelityCredit", String.valueOf(0.0)));
                    params.add(new BasicNameValuePair("action","update"));
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    params.add(new BasicNameValuePair("androidId", android_id));

                    myPopupWindow = popupWindow;
                    ((MainActivity) context).callHttpHandler("/updateButton", params);
                }else {
                    dbA.execOnDb("UPDATE button SET title = \"" + title.replaceAll("'", "\'") +
                            "\", subtitle = \"" + subtitle.replaceAll("'", "\'") + "\", img_name = \"" + icon + "\"," +
                            "color = " + color + " , printer= " + printerId + " WHERE id = " + b.getID() + "");
                    if (printerId != b.getPrinterId())
                        dbA.recursiveUpdatePrinter(b.getID(), printerId);

                    getCurrentCatButtonSet(currentCatID);
                    popupWindow.dismiss();
                }
            }
        });

        // GO_TO_CATEGORY button onClick
        CustomButton goToCategory = (CustomButton)popupView.findViewById(R.id.goToCategory);
        goToCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setElevation(0.0f);
                int catID = b.getID();
                if(deepnessLevel+1<=MAX_DEEPNESS_LEVEL+1) {
                    deepnessLevel+=1;
                    String title = ((CustomEditText)popupView.findViewById(R.id.catTitleInsert)).getText().toString();
                    String subtitle = ((EditText)popupView.findViewById(R.id.catSubTitleInsert)).getText().toString();
                    int color = b.getColor();
                    if(colorChosen != -1){
                        color = colorChosen;
                        colorChosen = -1;
                        chosenColorView = null;
                    }
                    int printerId = -1;
                    if(!printerModel.equals("")){
                        printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                    }

                    String icon;
                    if(iconChanged) icon = chosenIcon;
                    else icon = b.getImg();
                    if(StaticValue.blackbox){
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                        params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                        params.add(new BasicNameValuePair("color", String.valueOf(color)));
                        params.add(new BasicNameValuePair("imgName",icon));
                        params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                        params.add(new BasicNameValuePair("id",String.valueOf(b.getID())));
                        params.add(new BasicNameValuePair("action","goToCategory"));
                        params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                        // TODO params.add(new BasicNameValuePair("fidelityCredit", String.valueOf(credit.isActivated())));
                        params.add(new BasicNameValuePair("fidelityCredit", String.valueOf(0.0) ));
                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));

                        myPopupWindow = popupWindow;
                        ((MainActivity) context).callHttpHandler("/updateButton", params);
                    }else {

                        dbA.execOnDb("UPDATE button SET title = \"" + title.replaceAll("'", "\'") +
                                "\", subtitle = \"" + subtitle.replaceAll("'", "\'") + "\", img_name = \"" + icon + "\"," +
                                " color = " + color + ", printer=" + printerId + " WHERE id = " + b.getID() + "");
                        if (printerId != b.getPrinterId())
                            dbA.recursiveUpdatePrinter(b.getID(), printerId);
                        //dbA.execOnDb("UPDATE button SET printer= "+printerId+" WHERE catID= "+b.getID()+"");
                        goToCategory(currentCatID, catID, b.getTitle());
                    }
                }
                popupWindow.dismiss();
            }
        });
        CustomButton deleteCategory = (CustomButton)popupView.findViewById(R.id.deleteCategory);
        deleteCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //recursively deleting buttons, dialog with YES/NO will show up
                v.setElevation(0.0f);
                final View dialogView = layoutInflater.inflate(R.layout.yes_no_dialog, null);
                final PopupWindow popupDialog = new PopupWindow(
                        dialogView,
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                ((CustomTextView)dialogView.findViewById(R.id.delete_window))
                        .setText(resources.getString(R.string.you_are_deleting_2, resources.getString(getCurrentLevelName()), b.getTitle().toUpperCase()));
                ((CustomButton)dialogView.findViewById(R.id.delete_button))
                        .setText(resources.getString(R.string.delete_,resources.getString(getCurrentLevelName())));
                dialogView.findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(StaticValue.blackbox){
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("id",String.valueOf(b.getID())));
                            params.add(new BasicNameValuePair("currentCatID",String.valueOf(currentCatID)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myPopupWindow = popupWindow;
                            myPopupDialog = popupDialog;
                            ((MainActivity) context).callHttpHandler("/deleteButton", params);
                        }else {
                            dbA.deleteButton(b);
                            getCurrentCatButtonSet(currentCatID);
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
        });
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    String printerModel ;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        //forse va bene anche if(pos != 0)
        if(pos != 0){
            printerModel = adapterView.getItemAtPosition(pos).toString();
        }
        else{
            printerModel = "";
            //Toast.makeText(context, R.string.select_printer_model, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     *
     *  PRODUCT
     *  Popup window that will show up for PRODUCT settings
     *  and Vat window
     *
     */

    public void prodSettingsPopupWindow(final View grid_element){
        final ButtonLayout b = (ButtonLayout)grid_element.getTag();
        final LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_button_add, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        //IT FUCKING WORKS!
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        popupView.findViewById(R.id.ButtonTypeChooser).setVisibility(View.GONE);
        popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);

        //SET PRINTER SPINNER
        printerModel = "";
        Spinner spinner = (Spinner)popupView.findViewById(R.id.prodBarcode);
        ArrayList<PrinterModel> spinnerList = new ArrayList<>();
        spinnerList.add(new PrinterModel("Select Printer"));
        ArrayList<KitchenPrinter> kitchenPrinters = dbA.selectAllKitchenPrinter();
        if(kitchenPrinters.size()>0) {
            for(KitchenPrinter kitchenPrinter : kitchenPrinters) {
                spinnerList.add(new PrinterModel(kitchenPrinter.getName()));
            }
        }
        ArrayAdapter<PrinterModel> adapter = new ArrayAdapter<PrinterModel>(context, R.layout.spinner_dropdown, spinnerList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(b.getPrinterId()!=-1){
            for(int i=0; i < kitchenPrinters.size(); i++){
                if(b.getPrinterId()==kitchenPrinters.get(i).getId())
                    spinner.setSelection(i+1);
            }
        }

        // GETTING View elements
        EditText title = (EditText)popupView.findViewById(R.id.prodTitleInsert);
        EditText subtitle = (EditText)popupView.findViewById(R.id.prodSubTitleInsert);
        EditText price = (EditText)popupView.findViewById(R.id.prodPriceInsert);
        CustomButton myVatButton = (CustomButton)popupView.findViewById(R.id.prodTaxInsert);
        CustomButton myBarcode = (CustomButton)popupView.findViewById(R.id.read_barcode);



        title.setText(b.getTitle());
        subtitle.setText(b.getSubTitle());
        price.setText(new String("" + b.getPrice()));
        if(!b.getBarcode().equals(""))
            myBarcode.setText(b.getBarcode());
        if(dbA.fetchVatButtonValue(b.getID()) != 0){
            int vatValue = dbA.fetchVatByIdQuery(b.getVat());
            myVatButton.setText(""+vatValue);
            if(vatAdapter == null)
                vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, thisGrid, vatState);
            vatAdapter.setSelectedVatId(b.getVat());
            //myVatButton.setText(dbA.fetchVatButtonValue(b.getID()) + "%");
        }
        setupDismissKeyboard(popupView);
        showProduct = 0;
        popupView.findViewById(R.id.product_tv).setOnClickListener(new OnClickListener() {
            @Override
            //we're creating a new button
            public void onClick(View v) {
                showProduct =0;
                popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));
                popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));

            }
        });

        popupView.findViewById(R.id.combo_tv).setOnClickListener(new OnClickListener() {
            @Override
            //we're creating a new button
            public void onClick(View v) {
                showProduct =2;

                popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));
                popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));

            }
        });

        ImageButton discount =  popupView.findViewById(R.id.fidelity_discount_checkbox_prod);
        discount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                discount.setActivated(!discount.isActivated());
            }
        });

        if(b.getCat()==0){
            showProduct =0;
            popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));
            popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));
        }else{
            showProduct =2;
            popupView.findViewById(R.id.product_tv).setBackground(ContextCompat.getDrawable(context, R.color.blackTransparent));
            popupView.findViewById(R.id.combo_tv).setBackground(ContextCompat.getDrawable(context, R.color.green_2));
        }

        if(b.getFidelity_discount()==1)
            discount.setActivated(true);

        EditText creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input));
        creditValue.setText(String.valueOf(b.getCredit_value()));

        // Closure Button
        final ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }});

        popupView.findViewById(R.id.prodButtonColor).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vatAdapter == null || !vatAdapter.getVatState()){
                    popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                    popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                    setupColors(popupView.findViewById(R.id.colors_container), b.getColor());
                    colorState = true;
                }
                else if(vatAdapter.getVatState() && colorState){
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    //setupColors(popupView.findViewById(R.id.colors_container));
                    colorState = false;
                }
                else if(vatAdapter.getVatState() && !colorState){
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(GONE);
                    popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                    colorState = true;
                }
                popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                popupView.findViewById(R.id.colors_container).setVisibility(VISIBLE);
                setupColors(popupView.findViewById(R.id.colors_container), b.getColor());
                colorState = true;

                // dismiss button is set to "go back" and at onClick resets its behavior to the previous one
                btnDismiss.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                        popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                        colorState = false;
                        v.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });
                    }
                });
            }
        });

        //vat insert popup
        myVatButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!colorState) {
                    popupView.findViewById(R.id.ProductSetup).setVisibility(GONE);
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);

                    popupVatWindow(popupView, popupWindow, b);
                    vatState = true;
                }
                else{
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);

                    popupVatWindow(popupView, popupWindow, b);
                    vatState = true;
                }
            }
        });

        myVatButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(dbA.fetchVatButtonValue(b.getID()) != -1){
                    /*dbA.execOnDb("UPDATE button SET vat=" + 0 + " WHERE id=" + b.getID() + ";");*/
                    myVatButton.setText("");
                    notifyDataSetChanged();
                    if(vatAdapter != null)
                        vatAdapter.setSelectedVatId(-1);
                }
                return true;
            }
        });

        // Ok button onClick
        ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                if(!colorState){
                    int vat1 = 0;
                    if(vatAdapter != null){
                        dbA.showData("vat");
                        vatId = vatAdapter.getSelectedVatId();
                        vat1 = dbA.fetchVatByIdQuery(vatId);
                        if(vat1 != 0)
                            b.setVat(vat1);
                    }
                    if (((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals(""))
                        { Toast.makeText(context,R.string.insert_price, Toast.LENGTH_SHORT).show(); }

                    else if ( Float.parseFloat( ((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString() ) <= 0.01 )
                        { Toast.makeText(context, R.string.price_not_zero, Toast.LENGTH_LONG).show(); }

                    else if (((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString().equals(""))
                        Toast.makeText(context, R.string.insert_title, Toast.LENGTH_SHORT).show();

                    else if(!((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString().equals("")
                            && !((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals(""))
                    {
                        String title = ((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString();
                        String subtitle = ((EditText)popupView.findViewById(R.id.prodSubTitleInsert)).getText().toString();
                        float price = Float.valueOf(((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString());

                        int color = b.getColor();
                        if(colorChosen != -1){
                            color = colorChosen;
                            colorChosen = -1;
                            chosenColorView = null;
                            b.setColor(color);
                        }
                        int printerId = -1;
                        if(!printerModel.equals("")){
                            dbA.showData("kitchen_printer");
                            printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                        }

                        String barcode = ((CustomButton)popupView.findViewById(R.id.read_barcode)).getText().toString();

                        double cValue = 0;
                        String creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input)).getText().toString();
                        if(!creditValue.equals("")){
                            cValue = Double.valueOf(creditValue);
                        }

                        if(StaticValue.blackbox){
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                            params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                            params.add(new BasicNameValuePair("color", String.valueOf(color)));
                            params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                            params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                            params.add(new BasicNameValuePair("isCat", String.valueOf(showProduct)));
                            params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                            params.add(new BasicNameValuePair("price", String.valueOf(price)));
                            params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                            params.add(new BasicNameValuePair("barcode", barcode));
                            params.add(new BasicNameValuePair("id", String.valueOf(b.getID())));
                            params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                            params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myPopupWindow = popupWindow;
                            ((MainActivity) context).callHttpHandler("/updateButton2", params);

                        }else {
                            dbA.execOnDb("UPDATE button SET title = \"" + title.replaceAll("'", "\'") +
                                    "\", subtitle = \"" + subtitle.replaceAll("'", "\'") +
                                    "\", img_name = \"\", color = " + color + ", " +
                                    "barcode='" + barcode +
                                    "',price = " + price + ", vat = " + vat1 + ", printer = " + printerId +
                                    " WHERE " + DatabaseAdapter.KEY_ID + " = " + b.getID() + ";");

                            getCurrentCatButtonSet(currentCatID);
                            popupWindow.dismiss();
                        }
                    }
                }
                else{
                    Toast.makeText(context, R.string.color_set, Toast.LENGTH_SHORT).show();
                    popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    colorState = false;
                }
            }
        });

        // goToModifiers
        CustomButton btnOpenModifiers = (CustomButton)popupView.findViewById(R.id.goToModifiers);
        btnOpenModifiers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int vat1 = 0;
                if(vatAdapter != null){
                    vatId = vatAdapter.getSelectedVatId();
                    vat1 = dbA.fetchVatByIdQuery(vatId);
                    if(vat1 != 0)
                        b.setVat(vat1);
                }else{
                    if(b.getVat()!=0)
                    vat1 = dbA.fetchVatByIdQuery(b.getVat());
                }
                if (((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals(""))
                    { Toast.makeText(context,R.string.insert_price, Toast.LENGTH_SHORT).show(); }

                else if ( Float.parseFloat( ((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString() ) <= 0.01 )
                    { Toast.makeText(context, R.string.price_not_zero, Toast.LENGTH_LONG).show(); }

                else
                {
                    String title = ((EditText) popupView.findViewById(R.id.prodTitleInsert)).getText().toString();
                    String subtitle = ((EditText) popupView.findViewById(R.id.prodSubTitleInsert)).getText().toString();
                    float price = Float.valueOf(((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString());

                    int color = b.getColor();
                    if (colorChosen != -1) {
                        color = colorChosen;
                        colorChosen = -1;
                        chosenColorView = null;
                        b.setColor(color);
                    }
                    int printerId = -1;
                    if(!printerModel.equals("")){
                        printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
                    }

                    double cValue = 0;
                    String creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input)).getText().toString();
                    if(!creditValue.equals("")){
                        cValue = Double.valueOf(creditValue);
                    }

                    String barcode = ((CustomButton)popupView.findViewById(R.id.read_barcode)).getText().toString();

                    if(StaticValue.blackbox){
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                        params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                        params.add(new BasicNameValuePair("color", String.valueOf(color)));
                        params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                        params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                        params.add(new BasicNameValuePair("isCat", String.valueOf("0")));
                        params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                        params.add(new BasicNameValuePair("price", String.valueOf(price)));
                        params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                        params.add(new BasicNameValuePair("barcode", barcode));
                        params.add(new BasicNameValuePair("id", String.valueOf(b.getID())));
                        params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                        params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        params.add(new BasicNameValuePair("androidId", android_id));


                        myPopupWindow = popupWindow;
                        ((MainActivity) context).callHttpHandler("/updateButton3", params);

                    }else {

                        dbA.execOnDb("UPDATE button SET title = \"" + title.replaceAll("'", "\'") +
                                "\", subtitle = \"" + subtitle.replaceAll("'", "\'") +
                                "\", img_name = \"\", color = " + color + ", " +
                                "barcode='" + barcode +
                                "',price = " + price + ", vat = " + vat1 + ", printer = " + printerId +
                                " WHERE " + DatabaseAdapter.KEY_ID + " = " + b.getID() + ";");
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
                }
            }
        });

        // DELETE
        CustomButton btnDelete = (CustomButton)popupView.findViewById(R.id.deleteProduct);
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //recursively deleting buttons, dialog with YES/NO will show up
                v.setElevation(0.0f);
                final View dialogView = layoutInflater.inflate(R.layout.yes_no_dialog, null);
                final PopupWindow popupDialog = new PopupWindow(
                        dialogView,
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                ((CustomTextView) dialogView.findViewById(R.id.delete_window))
                        .setText(resources.getString(R.string.you_are_deleting__product, b.getTitle().toUpperCase()));
                ((CustomButton) dialogView.findViewById(R.id.delete_button))
                        .setText(R.string.deleteProduct);
                dialogView.findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(StaticValue.blackbox){
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("id",String.valueOf(b.getID())));
                            params.add(new BasicNameValuePair("currentCatID",String.valueOf(currentCatID)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myPopupWindow = popupWindow;
                            myPopupDialog = popupDialog;
                            ((MainActivity) context).callHttpHandler("/deleteButton", params);
                        }else {
                            dbA.deleteButton(b);
                            getCurrentCatButtonSet(currentCatID);
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
        });

        // BARCODE
        CustomButton barcodeButton = (CustomButton)popupView.findViewById(R.id.read_barcode);
        barcodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //recursively deleting buttons, dialog with YES/NO will show up
                v.setElevation(0.0f);
                final View dialogView = layoutInflater.inflate(R.layout.barcode_input_dialog, null);
                final PopupWindow popupDialog = new PopupWindow(
                        dialogView,
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                ((MainActivity)context).setBarcodeShow(true);

                dialogView.findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)context).setBarcodeShow(false);
                            popupDialog.dismiss();

                    }
                });

                dialogView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)context).setBarcodeShow(false);
                        String myBarcode = ((CustomEditText)dialogView.findViewById(R.id.single_input)).getText().toString();
                        if(myBarcode!=null && !myBarcode.equals(""))
                            ((CustomButton)popupView.findViewById(R.id.read_barcode)).setText(myBarcode);
                        popupDialog.dismiss();

                    }
                });

                popupDialog.setFocusable(true);
                popupDialog.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }



    public void okBehaviourForSettingColor(View popupView){
        definiteColor = defaultColor;
        if (colorChosen != -1) {
            definiteColor = colorChosen;
            colorChosen = -1;
            chosenColorView = null;
        }
        popupView.findViewById(R.id.colors_container).setVisibility(GONE);
        popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
        colorState = false;
    }

    public void okBehaviourInCreatingProduct(View popupView, PopupWindow popupWindow){
        //I'm choosing (or not) a color
        if(colorState && !vatState){
            definiteColor = defaultColor;
            if (colorChosen != -1) {
                definiteColor = colorChosen;
                colorChosen = -1;
                chosenColorView = null;
            }
            popupView.findViewById(R.id.colors_container).setVisibility(GONE);
            popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
            colorState = false;
        }
        //I'm choosing (or not) a vat value (also adding new values in db)
        else if(vatState && !colorState){
            vatAdapter.okBehaviourInVatAdapter();
            vatState = false;
            vatId = vatAdapter.getSelectedVatId();
            int vat1 = dbA.fetchVatByIdQuery(vatId);
        }
        //I'm adding a new Product
        else if(!vatState && !colorState){
            getCurrentLevelMaxData();
            okBehaviourInProductSetting(popupView, popupWindow);
        }
        //Something's wrong
        else if(vatState && colorState)
            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to set up the Vat Insert Window, while modifying an existing button
     * @param popupView
     * @param popupWindow
     * @param b
     * @return
     * Added by Fabrizio
     */
    private void popupVatWindow(View popupView, PopupWindow popupWindow, ButtonLayout b){

        vatState = true;
        LayoutParams rlp = (LayoutParams)popupView.findViewById(R.id.new_Popup_Vat_Insert)
                .getLayoutParams();
        int t = (int)(dpHeight-52)/2 - rlp.height/2;
        rlp.topMargin = t;
        popupView.findViewById(R.id.new_Popup_Vat_Insert).setLayoutParams(rlp);

        RecyclerView vat_recycler = (RecyclerView)popupView.findViewById(R.id.vat_value_recycler);
        vat_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayout.VERTICAL, false));
        vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, b, thisGrid, true);
        vat_recycler.setAdapter(vatAdapter);
        vat_recycler.setHasFixedSize(true);

        EditText vatContainer = (EditText)popupView.findViewById(R.id.vat_group_insert);
        EditText percContainer = (EditText)popupView.findViewById(R.id.vat_perc_insert);

        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
        vat_recycler.addItemDecoration(divider);
        setupDismissKeyboard(popupView);

        ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //due comportamenti diversi, in base ai popup visibili
                if(vatState && vatAdapter.getVatState()){
                    popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(GONE);
                    popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                    if(dbA.fetchVatButtonValue(b.getID()) != 0) {
                        int vatValue = dbA.fetchVatByIdQuery(b.getVat());
                        ((CustomButton) popupView.findViewById(R.id.prodTaxInsert)).setText(""+vatValue );
                        /*((CustomButton) popupView.findViewById(R.id.prodTaxInsert)).setText(
                                dbA.fetchVatButtonValue(b.getID()) + "%");*/
                    }
                    vatState = false;
                    vatAdapter.setVatState(false);
                }
                else if(!vatAdapter.getVatState() || (vatAdapter.getVatState() && !vatState)) {
                    popupWindow.dismiss();
                }
            }
        });

        ImageButton btnOk = (ImageButton)popupView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                int vat1 = 0;
                if(vatAdapter.getVatState()){
                    if(vatContainer.getText().toString().equals("")){
                        Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
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
                    //if(vat1 != 0 && !dbA.checkIfVatIsAdded(vat1)){
                    if(vat1 != 0 ){
                        if(StaticValue.blackbox){
                            String perc ="0";
                            if(!percContainer.getText().toString().equals("")){
                                perc = percContainer.getText().toString();
                            }

                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("vatValue",String.valueOf(vat1)));
                            params.add(new BasicNameValuePair("perc",String.valueOf(perc)));

                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myPopupView = popupView;
                            ((MainActivity) context).callHttpHandler("/insertVat", params);
                        }else {
                            if(!dbA.checkIfVatIsAdded(vat1)) {
                                dbA.addVatValue(vat1);
                                ArrayList<VatModel> newVats = dbA.fetchVatArrayByQuery();
                                vatAdapter.setVats(newVats);
                                vatAdapter.notifyDataSetChanged();
                                vatContainer.setText("");
                                percContainer.setText("");
                                Toast.makeText(context, R.string.new_vat_value_inserted, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                else if(!vatAdapter.getVatState()){
                    //if(!dbA.checkIfVatIsAdded(vat1) && vat1 != 0){
                    if(vat1 != 0){
                        if(StaticValue.blackbox){
                            String perc ="0";
                            if(!percContainer.getText().toString().equals("")){
                                perc = percContainer.getText().toString();
                            }
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("vatValue",String.valueOf(vat1)));
                            params.add(new BasicNameValuePair("buttonId",String.valueOf(b.getID())));
                            params.add(new BasicNameValuePair("perc",String.valueOf(perc)));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            myPopupView = popupView;
                            myPopupWindow = popupWindow;
                            ((MainActivity) context).callHttpHandler("/insertVat1", params);
                        }else {
                            if(!dbA.checkIfVatIsAdded(vat1)) {
                                dbA.addVatValue(vat1);
                                vatContainer.setText("");
                                percContainer.setText("");
                                RecyclerView vat_recycler = (RecyclerView) popupView.findViewById(R.id.vat_value_recycler);
                                vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, b, thisGrid, true);
                                vat_recycler.setAdapter(vatAdapter);
                                vat_recycler.setHasFixedSize(true);
                                vat_recycler.setVisibility(VISIBLE);
                                popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);
                                DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
                                divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
                                vat_recycler.addItemDecoration(divider);
                                vatAdapter.notifyDataSetChanged();
                                Toast.makeText(context, R.string.new_vat_value_inserted, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else if(dbA.checkIfVatIsAdded(vat1) && vat1 != 0){
                        b.setVat(vat1);
                        Toast.makeText(context, R.string.vat_set, Toast.LENGTH_SHORT).show();
                        popupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(GONE);
                        popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                        if(vat1 != 0)
                            ((CustomButton)popupView.findViewById(R.id.modVatInsert)).setText(""+vat1 );
                        vatState = false;
                    }
                    else if(vat1 == 0){
                        Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    public void addVatFromServer(Vat myVat, ButtonLayout b) {
        dbA.addVatValueFromServer(myVat.getId(), myVat.getValue(), myVat.getPerc());
        //dbA.addVatValue(vat);
        EditText vatContainer = (EditText)myPopupView.findViewById(R.id.vat_group_insert);
        EditText percContainer = (EditText)myPopupView.findViewById(R.id.vat_perc_insert);
        vatContainer.setText("");
        percContainer.setText("");
        RecyclerView vat_recycler = (RecyclerView) myPopupView.findViewById(R.id.vat_value_recycler);
        vatAdapter = new VatAdapter(dbA, context, myPopupView, myPopupWindow, b, thisGrid, true);
        vat_recycler.setAdapter(vatAdapter);
        vat_recycler.setHasFixedSize(true);
        vat_recycler.setVisibility(VISIBLE);
        myPopupView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(VISIBLE);
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
        vat_recycler.addItemDecoration(divider);
        vatAdapter.notifyDataSetChanged();
        Toast.makeText(context, R.string.new_vat_value_inserted, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to launch the Vat Insert Window, when creating a new button
     * @param popupView
     * @param popupWindow
     * @return
     * Added by Fabrizio
     */
    private void popupVatWindow(View popupView, PopupWindow popupWindow){

        vatState = true;
        LayoutParams rlp = (LayoutParams)popupView.findViewById(R.id.new_Popup_Vat_Insert)
                .getLayoutParams();
        int t = (int)(dpHeight-52)/2 - rlp.height/2;
        rlp.topMargin = t;
        popupView.findViewById(R.id.new_Popup_Vat_Insert).setLayoutParams(rlp);

        RecyclerView vat_recycler = (RecyclerView)popupView.findViewById(R.id.vat_value_recycler);
        vat_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayout.VERTICAL, false));
        vatAdapter = new VatAdapter(dbA, context, popupView, popupWindow, thisGrid,true);
        vat_recycler.setAdapter(vatAdapter);
        vat_recycler.setHasFixedSize(true);

        EditText vatContainer = (EditText)popupView.findViewById(R.id.vat_group_insert);

        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line_horizontal1dp));
        vat_recycler.addItemDecoration(divider);
        setupDismissKeyboard(popupView);

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    /**
     *  GO_TO_CATEGORY
     *  Method called when "Go To Category" is clicked
     *  above_recyclerView is the RelativeLayout container for the back button and category title button
     **/
    public void goToCategory(final int currCatID, int newCatID, String categoryTitle){
        RelativeLayout above_rv = (RelativeLayout)((Activity)context).findViewById(R.id.above_recyclerView);
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFd3d3d3); //light-gray background
        border.setStroke(3, 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8);
        this.currentCatTitle = categoryTitle;
        this.currentCatID = newCatID;
        if(newCatID == 0){
            //Main page case
            above_rv.setVisibility(GONE);
            LayoutParams lp =(LayoutParams)((Activity)context).findViewById(R.id.recyclerView).getLayoutParams();
            lp.topMargin = 0;
            ((Activity)context).findViewById(R.id.recyclerView).setLayoutParams(lp);
            getCurrentCatButtonSet(newCatID);
        }
        else {
            above_rv.setVisibility(VISIBLE);
            LayoutParams lp =(LayoutParams)((Activity)context).findViewById(R.id.recyclerView).getLayoutParams();
            ((CustomTextView)above_rv.findViewById(R.id.infoTextView)).setText(R.string.info_tv_category);
            ((CustomTextView)above_rv.findViewById(R.id.infoTextView)).setLetterSpacing(0);
            ((Activity)context).findViewById(R.id.recyclerView).setLayoutParams(lp);
            ((CustomTextView)above_rv.findViewById(R.id.categoryTitle)).setText(categoryTitle);
            /**
             * Saving previous category id, needed for back button
             *  e.g. CAT1, id = 15;
             *       CAT2, id = 20;
             *  CAT2 is contained in CAT1, if CAT2 is clicked then CAT1.id gets saved so that
             *  when making the query to the database( as back button gets clicked ) all the buttons having a catID = CAT1.id
             *  are taken (in this case CAT2 is one them, of course)
             *
             **/

            Cursor c = dbA.fetchByQuery("SELECT * FROM button WHERE id="+currentCatID);
            c.moveToFirst();
            previousCatID = c.getInt(c.getColumnIndex(DatabaseAdapter.KEY_CAT_ID));
            c.close();
            if(previousCatID == 0) previousCatTitle = null;
            else{
                c = dbA.fetchByQuery("SELECT * FROM button WHERE id="+previousCatID);
                c.moveToFirst();
                previousCatTitle = c.getString(c.getColumnIndex(DatabaseAdapter.KEY_TITLE));
                c.close();
            }
            above_rv.findViewById(R.id.backButton).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deepnessLevel--;
                    goToCategory(currentCatID,previousCatID,previousCatTitle);
                }
            });
            above_rv.findViewById(R.id.backButton).setBackground(border);
            getCurrentCatButtonSet(newCatID);
        }
    }

    //new behaviour for ok button when creating new product
    public void okBehaviourInProductSetting(View popupView, PopupWindow popupWindow){
        vatId = vatAdapter.getSelectedVatId();
        int iva = dbA.fetchVatByIdQuery(vatId);
        if(((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals(""))
            { Toast.makeText(context,R.string.insert_price, Toast.LENGTH_SHORT).show(); }

        else if ( Float.parseFloat( ((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString() ) <= 0.01 )
            { Toast.makeText(context, R.string.price_not_zero, Toast.LENGTH_LONG).show(); }

        else if(((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString().equals("")){
            Toast.makeText(context, R.string.insert_title, Toast.LENGTH_SHORT).show();
        }

        else if(!((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString().equals("") &&
                !((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals("")){
            String title = ((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString();
            String subtitle = ((EditText)popupView.findViewById(R.id.prodSubTitleInsert)).getText().toString();
            float price = Float.valueOf(((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString());

            int color = definiteColor;
            if(colorChosen != -1){
                color = colorChosen;
                colorChosen = -1;
                chosenColorView = null;
            }
            definiteColor = defaultColor;

            int printerId =-1;
            if(!printerModel.equals("")){
                dbA.showData("kitchen_printer");
                printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
            }

            String barcode = ((CustomButton)popupView.findViewById(R.id.read_barcode)).getText().toString();

            double cValue = 0;
            String creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input)).getText().toString();
            if(!creditValue.equals("")){
                cValue = Double.valueOf(creditValue);
            }

            ImageButton discount =  popupView.findViewById(R.id.fidelity_discount_checkbox_prod);


            if(StaticValue.blackbox){
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                params.add(new BasicNameValuePair("color", String.valueOf(color)));
                params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
             //   params.add(new BasicNameValuePair("isCat", String.valueOf("0")));
                params.add(new BasicNameValuePair("price", String.valueOf(price)));
                params.add(new BasicNameValuePair("vat", String.valueOf(iva)));

                params.add(new BasicNameValuePair("isCat", String.valueOf(showProduct)));
                params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                params.add(new BasicNameValuePair("androidId", android_id));


                myPopupWindow = popupWindow;
                myPopupView= popupView;
                ((MainActivity) context).callHttpHandler("/insertButton5", params);
            }else {

                dbA.queryToDb("INSERT INTO button (title, subtitle, img_name, color, position, " +
                        "price, vat, catID, isCat) " +
                        "VALUES(\"" + title.replaceAll("'", "\'") +
                        "\",\"" + subtitle.replaceAll("'", "\'") +
                        "\",\"\"," + color + "," + buttons.size() +
                        "," + price + "," + iva + "," + currentCatID + ", 0)");
                getCurrentCatButtonSet(currentCatID);
                if (!colorState)
                    popupWindow.dismiss();
                else {

                    popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    colorState = false;
                }
            }
        }
    }

    //new behaviour for okButton when modifying an existing button
    public void okBehaviorInModifyingButton(View popupView, PopupWindow popupWindow, ButtonLayout b){
        int vat1 = 0;
        if(vatAdapter != null){
            vatId = vatAdapter.getSelectedVatId();
            vat1 = dbA.fetchVatByIdQuery(vatId);
            b.setVat(vat1);
        }
        if(((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString().equals(""))
            { Toast.makeText(context,R.string.insert_price, Toast.LENGTH_SHORT).show(); }

        else if ( Float.parseFloat( ((EditText) popupView.findViewById(R.id.prodPriceInsert)).getText().toString() ) <= 0.01 )
            { Toast.makeText(context, R.string.price_not_zero, Toast.LENGTH_LONG).show(); }

        else if(((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString().equals(""))
            Toast.makeText(context, R.string.insert_title, Toast.LENGTH_SHORT).show();
        else{
            String title = ((EditText)popupView.findViewById(R.id.prodTitleInsert)).getText().toString();
            String subtitle = ((EditText)popupView.findViewById(R.id.prodSubTitleInsert)).getText().toString();
            float price = Float.valueOf(((EditText)popupView.findViewById(R.id.prodPriceInsert)).getText().toString());

            int color = b.getColor();
            if(colorChosen != -1){
                color = colorChosen;
                colorChosen = -1;
                chosenColorView = null;
                b.setColor(color);
            }
            int printerId = -1;
            if(!printerModel.equals("")){
                dbA.showData("kitchen_printer");
                printerId = dbA.selectKitchenPrinterByName(printerModel).getId();
            }

            String barcode = ((CustomButton)popupView.findViewById(R.id.read_barcode)).getText().toString();

            double cValue = 0;
            String creditValue = ((EditText) popupView.findViewById(R.id.credit_value_input)).getText().toString();
            if(!creditValue.equals("")){
                cValue = Double.valueOf(creditValue);
            }

            ImageButton discount =  popupView.findViewById(R.id.fidelity_discount_checkbox_prod);

            if(StaticValue.blackbox){
              /*  List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                params.add(new BasicNameValuePair("color", String.valueOf(color)));
                params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                params.add(new BasicNameValuePair("isCat", String.valueOf("0")));
                params.add(new BasicNameValuePair("price", String.valueOf(price)));
                params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));

                myPopupWindow = popupWindow;
                myPopupView= popupView;
                ((MainActivity) context).callHttpHandler("/insertButton5", params);*/

                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("title", title.replaceAll("'", "\'") ));
                params.add(new BasicNameValuePair("subtitle", subtitle.replaceAll("'", "\'") ));
                params.add(new BasicNameValuePair("color", String.valueOf(color)));
                params.add(new BasicNameValuePair("position", String.valueOf(buttons.size())));
                params.add(new BasicNameValuePair("catID", String.valueOf(currentCatID)));
                params.add(new BasicNameValuePair("isCat", String.valueOf(showProduct)));
                params.add(new BasicNameValuePair("printerId", String.valueOf(printerId)));
                params.add(new BasicNameValuePair("price", String.valueOf(price)));
                params.add(new BasicNameValuePair("vat", String.valueOf(vat1)));
                params.add(new BasicNameValuePair("barcode", barcode));
                params.add(new BasicNameValuePair("id", String.valueOf(b.getID())));
                params.add(new BasicNameValuePair("fidelityDiscount", String.valueOf(discount.isActivated())));
                params.add(new BasicNameValuePair("creditValue", String.valueOf(cValue)));
                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                params.add(new BasicNameValuePair("androidId", android_id));


                myPopupWindow = popupWindow;
                myPopupView= popupView;
                ((MainActivity) context).callHttpHandler("/updateButton2", params);

            }else {

                dbA.execOnDb("UPDATE button SET title = \"" + title.replaceAll("'", "\'") +
                        "\", subtitle = \"" + subtitle.replaceAll("'", "\'") +
                        "\", img_name = \"\", color = " + color + ", " +
                        "price = " + price + ", vat = " + vat1 +
                        " WHERE " + DatabaseAdapter.KEY_ID + " = " + b.getID() + ";");
                getCurrentCatButtonSet(currentCatID);
                if (!colorState)
                    popupWindow.dismiss();
                else {

                    popupView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                    popupView.findViewById(R.id.colors_container).setVisibility(GONE);
                    colorState = false;
                }
            }
        }
    }

    /** Method which updates the data set according to the category **/
    public void getCurrentCatButtonSet(int catID){
        dbA.showData("button");
        buttons.clear();
        buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = " + catID + " AND id!=-30 AND id!=-20 ORDER BY position");
        ButtonLayout big_plus_button = new ButtonLayout(context);
        big_plus_button.setID(-11);
        big_plus_button.setPos(buttons.size());
        if(deepnessLevel<=MAX_DEEPNESS_LEVEL) big_plus_button.setImg("big_plus_button");
        else big_plus_button.setImg("addplussubbutton");
        big_plus_button.setCatID(catID);
        buttons.add(big_plus_button);
        MainActivity.setButtonSet(buttons);
    }


    /**
     * Method returning the current level name (category, subcateogory etc..)
     * independently from the number of level bought by the user.
     **/
    private int getCurrentLevelName(){
        int name = -1;
        if(MAX_DEEPNESS_LEVEL == 0)
            name = R.string.category;
        else
            switch (deepnessLevel) {
                case MAX_DEEPNESS_LEVEL:
                    name = R.string.subcategory;
                    break;
                case MAX_DEEPNESS_LEVEL-1:
                    name = R.string.category;
                    break;
                case MAX_DEEPNESS_LEVEL-2:
                    name = R.string.mainCategory;
                    break;
                case MAX_DEEPNESS_LEVEL-3:
                    name = R.string.section;
                    break;
            }
        return name;
    }

    /**
     * Mathod getting the max number of elements allowed for each level
     */
    private void getCurrentLevelMaxData(){
        numberOfCategories = 0;
        numberOfProducts = 0;
        switch (deepnessLevel) {
            case MAX_DEEPNESS_LEVEL:
                MAX_NUMBER_OF_CATEGORIES = 48;
                break;
            case MAX_DEEPNESS_LEVEL-1:
                MAX_NUMBER_OF_CATEGORIES = 32;
                break;
            case MAX_DEEPNESS_LEVEL-2:
                MAX_NUMBER_OF_CATEGORIES = 32;
                break;
            case MAX_DEEPNESS_LEVEL-3:
                MAX_NUMBER_OF_CATEGORIES = 16;
                break;
        }
        for (ButtonLayout b : buttons) {
            if(b.getCat()==1) numberOfCategories++;
            else numberOfProducts++;
        }
    }


    /**
     * Method that recursively takes each EditText inside the specified @param view
     * and sets up to dismiss keyboard when user performs a click out of any EditText
     */
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

    public void setupColors(final View view, int buttonColor){
            if((view instanceof ImageButton)) {
                if(((ColorDrawable)view.getBackground()).getColor() == buttonColor) {
                    ((ImageButton) view).setImageDrawable(context.getDrawable(R.drawable.colorpick));
                    chosenColorView = view;
                }
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(chosenColorView != view){
                            colorChosen = ((ColorDrawable) view.getBackground()).getColor();
                            if(chosenColorView != null)
                                ((ImageButton)chosenColorView).setImageDrawable(null);
                            chosenColorView = view;
                            isColorChosen = true;
                            ((ImageButton)chosenColorView).setImageDrawable(context.getDrawable(R.drawable.colorpick));
                        }
                        else{
                            colorChosen = defaultColor;
                            isColorChosen = false;
                            ((ImageButton)chosenColorView).setImageDrawable(null);
                        }
                    }
                });
            }
            else if( view instanceof ViewGroup)  {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View innerView = ((ViewGroup) view).getChildAt(i);
                    setupColors(innerView, buttonColor);
                }
        }
    }

    private void setupIconSelector(final View view){
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.icons_rv);
        GridLayoutManager grid_manager = new GridLayoutManager(context, 8);
        grid_manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){

            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        recyclerView.setLayoutManager(grid_manager);
        recyclerView.addItemDecoration(new IconSeparator(context, 0));

        recyclerView.setAdapter(new Adapter() {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v;
                IconHolder iconHolder;
                v = inflater.inflate(R.layout.icon_element, null);
                iconHolder = new IconHolder(v);
                return iconHolder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, final int position) {
                IconHolder iconHolder = (IconHolder)holder;
                try {
                    iconHolder.imageView.setImageDrawable(Drawable.createFromStream(context.getAssets().open("drawable_icons/"+icons.get(position)),null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int width = (position%8==0||position%8==7)? (int) (124 * density) : (int) (122 * density);
                if(chosenIcon.equals(icons.get(position))) {
                    iconHolder.iconPick.setVisibility(VISIBLE);
                    chosenIconHolder = iconHolder;
                }
                iconHolder.view.setLayoutParams(new LayoutParams(width, (int) (116*density)));
                iconHolder.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(chosenIconHolder!=null) chosenIconHolder.iconPick.setVisibility(GONE);
                        if(chosenIconHolder == iconHolder) {
                            chosenIconHolder = null;
                            chosenIcon = "";
                        }
                        else {
                            chosenIconHolder = iconHolder;
                            chosenIconHolder.iconPick.setVisibility(VISIBLE);
                            chosenIcon = icons.get(position);
                            isIconChosen = true;
                        }
                        iconChanged = true;
                    }
                });
            }

            @Override
            public int getItemCount() {
                return icons.size();
            }

        });
    }

    private class IconHolder extends ViewHolder{
        public ImageView imageView;
        public ImageView iconPick;
        public View view;

        public IconHolder(View v){
            super(v);
            view = v;
            imageView = (ImageView)v.findViewById(R.id.icon_image_view);
            iconPick = (ImageView)v.findViewById(R.id.iconPick);
        }
    }

    public void updateVatValue(int vat){
        this.vatValue = vat;
    }

}

