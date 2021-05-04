package com.example.blackbox.adapter;

/**
 * Created by tiziano on 13/06/17.
 */

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.fragments.ActivityCommunicator;
import com.example.blackbox.fragments.FragmentCommunicator;
import com.example.blackbox.fragments.OperativeFragment;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.VibrationClass;
import com.utils.db.DatabaseAdapter;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class OGridAdapter extends Adapter<ViewHolder> {
    private Context context;
    private ArrayList<ButtonLayout> buttons;
    private ArrayList<ButtonLayout> oldButtons;
    public ArrayList<OModifierGroupAdapter.OModifiersGroup> modifiers;
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

    private final int MAX_DEEPNESS_LEVEL = 3;
    private final int MAX_NUMBER_OF_PRODUCTS = 64;

    private ActivityCommunicator activityCommunicator;
    private FragmentCommunicator fragmentCommunicator;

    private OModifierGroupAdapter oma;

    private Boolean lastIs;
    String direction ="";


    public void setFavouritesButton(ArrayList<ButtonLayout> b) {
        this.buttons = b;
        notifyDataSetChanged();
    }

    public void setButtons(){
        if(StaticValue.showFavourites){
            if(StaticValue.blackbox) {
                activityCommunicator = (ActivityCommunicator) context;
                activityCommunicator.selectFavourites();
            }else
                this.buttons = dbA.selectFavoritesButton();
        }else if(StaticValue.showProducts){
            this.buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE isCat=0  AND id!=-30 AND id!=-20 ORDER BY catId"); // home button set

        }else {
            this.buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = " +myCatId+" AND id!=-30 AND id!=-20 ORDER BY isCat "+direction+", position"); // home button set
        }
        notifyDataSetChanged();

    }


    public int myCatId;

    public OGridAdapter(Context c, DatabaseAdapter dbA, Integer catId) {
        this.context = c;
        this.dbA = dbA;
        this.myCatId = catId;
        ButtonLayout b = dbA.fetchButtonByQuery("SELECT * FROM button WHERE catID = " + catId + " AND position=1 LIMIT 1");

            if (b.getCat() == 0) {
                direction = "ASC";
            } else {
                direction = "DESC";
            }

            if (StaticValue.showFavourites) {
                if(StaticValue.blackbox) {
                    activityCommunicator = (ActivityCommunicator) context;
                    activityCommunicator.selectFavourites();
                }else
                    this.buttons = dbA.selectFavoritesButton();
            } else if (StaticValue.showProducts) {
                this.buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE isCat=0  AND id!=-30 AND id!=-20 ORDER BY catId"); // home button set

            } else {
                this.buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = " + catId + " AND id!=-30 AND id!=-20 ORDER BY isCat " + direction + ", position"); // home button set
            }

            ButtonLayout buttonLayout = dbA.fetchButtonByQuery("SELECT * FROM button WHERE id = " + catId);

            if (catId != 0) {
                functionSetBackButton(catId, catId);
                setGetBackFuncion(catId, catId, buttonLayout.getTitle());

            }
            notifyDataSetChanged();

            inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            /**DISPLAY METRICS USED TO CENTER POPUP WINDOW **/
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            density = context.getResources().getDisplayMetrics().density;
            dpHeight = outMetrics.heightPixels;// / density;
            dpWidth = outMetrics.widthPixels;// / density;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        switch (viewType) {
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
        final ButtonLayout b = buttons.get(position);
       if(b.getCat()==0 || b.getCat()==2 ){
           return -1;
       }else{

           return 0;
       }
       /**
        boolean prova =dbA.checkIfCategoryIsFullOfProduct2("Select * from button where catID=" + b.getCatID());
        if(b.getCatID()!=0) {
            if (dbA.checkIfCategoryIsFullOfProduct("Select * from button where catID=" + b.getCatID())) {
                return 0;
            } else {
                return -1;
            }
        }else{

            if (prova) {
                return -1;
            } else {
                return 0;
            }
        }
        */

    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ButtonLayout b = buttons.get(position);
        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(b.getColor()); //black background
        border.setStroke((int) (3*density), 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8*density);

        GradientDrawable img_border = new GradientDrawable();
        img_border.setStroke((int) (2*density), 0xFFC6C5C6);
        img_border.setCornerRadius(5*density);

        if(b.getCat()==1){
            /**
            if(lastIs!=null){
                //se last è false quello prima era un prodotto
                if(!lastIs){
                    //aggiungo finti bottoni e e setto a true
                    lastIs = true;
                }
            }else{
                lastIs = true;
            }
             */
            ButtonHolder button = (ButtonHolder) holder;
            button.title.setText(b.getTitle());
            if (!b.getSubTitle().equals("")) {
                button.subtitle.setVisibility(VISIBLE);
                button.subtitle.setText(b.getSubTitle());
                RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) button.title.getLayoutParams();
                rll.setMargins(0, (int) (-5*density), 0, 0);
                button.title.setLayoutParams(rll);
            } else {
                button.subtitle.setVisibility(View.GONE);
                RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) button.title.getLayoutParams();
                rll.setMargins(0, (int) (4*density), 0, 0);
                button.title.setLayoutParams(rll);
            }
            //button.frame.setVisibility(VISIBLE);
            // button.image2.setImageResource(getImageId(context, b.getImg()));
            // button.frame.setBackground(img_border);
            if (b.getImg().equals("")) {
                button.image2.setVisibility(GONE);
                button.bigText.setVisibility(VISIBLE);
                if(b.getTitle().length()>3)
                    button.bigText.setText(b.getTitle().substring(0, 3).toUpperCase());
                else{
                    button.bigText.setText(b.getTitle().toUpperCase());
                }
            } else {
                button.image2.setVisibility(VISIBLE);
                button.bigText.setVisibility(GONE);
                // button.image2.setImageDrawable(rbd);
                try {
                    button.image2.setImageDrawable(Drawable.createFromStream(context.getAssets().open("drawable_icons/"+b.getImg()),null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                button.bigText.setText(b.getTitle().substring(0, 3).toUpperCase());
            }
            button.view.setLayoutParams(new RelativeLayout.LayoutParams((int) (154*density), (int) (145*density)));

            button.view.setBackground(border);
            button.view.setTag(b);
            button.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setAlpha(0.5f);
                    VibrationClass.vibeOn(context);
                    goToCategory(currentCatID, b.getID(), b.getTitle(), 0);
                    v.setAlpha(1.0f);

                }
            });

            if(b.getTitle()=="!!FAKE0701!!"){
                button.view.setVisibility(GONE);
            }else{
                button.view.setVisibility(VISIBLE);
            }
            Log.d("POSITION", ""+position);
            Log.d("BUTTON SIZE -1", ""+(buttons.size() - 1));
            if (position < buttons.size() - 1) {
                    if (buttons.get(position + 1).getCat() == 0) {
                        int count = (position+1)%4;
                        if(count!=0) {

                            for (int i = 1; i <= 4 - count; i++) {
                                ButtonLayout fakeButton = new ButtonLayout();
                                fakeButton.setCat(0);
                                fakeButton.setTitle("!!FAKE0701!!");
                                fakeButton.setSubTitle("FAKE");
                                fakeButton.setImg("");
                                buttons.add(position + i, fakeButton);

                            }
                        }

                    }
            }

        }else{
            /**
            if(lastIs!=null){
                //se last è false quello prima era un prodotto
                if(lastIs){
                    //aggiungo finti bottoni e e setto a trueù
                    for(int i=0; i<position%4; i++) {
                        ButtonLayout fakeButton = new ButtonLayout();
                        fakeButton.setCat(true);
                        fakeButton.setTitle("FAKE");
                        fakeButton.setSubTitle("FAKE");
                        fakeButton.setImg("");
                        buttons.add(position+1, fakeButton);
                        lastIs = false;
                    }

                }
            }else{
                lastIs = false;
            }*/
            SubButtonHolder subbutton = (SubButtonHolder) holder;
            subbutton.title.setText(b.getTitle());
            if (!b.getSubTitle().equals("")) {
                subbutton.subtitle.setVisibility(VISIBLE);
                subbutton.subtitle.setText(b.getSubTitle());
                RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) subbutton.title.getLayoutParams();
                rll.setMargins(0, (int) (-1*density), 0, 0);
                subbutton.title.setLayoutParams(rll);
            } else {
                subbutton.subtitle.setVisibility(View.GONE);
                RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) subbutton.title.getLayoutParams();
                rll.setMargins(0, (int) (6*density), 0, 0);
                subbutton.title.setLayoutParams(rll);
            }
            subbutton.view.setLayoutParams(new LayoutParams((int) (154*density), (int) (46*density)));
            border.setStroke((int) (3*density), context.getColor(R.color.yellow)); // yellow border
            subbutton.view.setBackground(border);
            subbutton.view.setTag(b);

            activityCommunicator = (ActivityCommunicator) context;
            subbutton.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* subbutton.gridcontainer.setAlpha(0.5f);
                    AlphaAnimation alpha = new AlphaAnimation(0.5F, 1.0F);
                    alpha.setDuration(0); // Make animation instant
                    alpha.setFillAfter(true); // Tell it to persist after the animation ends
                    subbutton.gridcontainer.startAnimation(alpha);*/

                    AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                    animation1.setDuration(10);
                    subbutton.gridcontainer.setAlpha(1f);
                    subbutton.gridcontainer.startAnimation(animation1);

                    VibrationClass.vibeOn(context);
                    activityCommunicator.passDataToActivity(b, b.getTitle(), b.getID(), b.getPrice(), b.getQuantity());
                }
            });

            subbutton.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    LayoutInflater layoutInflater = (LayoutInflater)context
                            .getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View popupView = layoutInflater.inflate(R.layout.operative_quantity_popup, null);

                    final PopupWindow popupWindow = new PopupWindow(
                            popupView,
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)
                            popupView.findViewById(R.id.quantity_popup)
                                    .getLayoutParams();
                    int t = (int)(dpHeight-52)/2 - rlp.height/2;
                    rlp.topMargin = t;
                    popupView.findViewById(R.id.quantity_popup).setLayoutParams(rlp);
                    EditText qntyInsert = (EditText)popupView.findViewById(R.id.quantity_insert);
                    setupDismissKeyboard(popupView);

                    ImageButton btnOK = (ImageButton)popupView.findViewById(R.id.ok);
                    btnOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(qntyInsert.getText().toString().equals("")){
                                Toast.makeText(context, R.string.insert_a_valid_quantity_value, Toast.LENGTH_SHORT).show();
                            }
                            else if(Integer.valueOf(qntyInsert.getText().toString())>100){
                                Toast.makeText(context, R.string.max_100, Toast.LENGTH_SHORT).show();

                            }else {
                                int qnty = Integer.parseInt(qntyInsert.getText().toString());
                                if(qnty == 0){
                                    Toast.makeText(context, R.string.insert_a_valid_quantity_value, Toast.LENGTH_SHORT).show();
                                    qntyInsert.setText("");
                                }
                                else{
                                    //it must already add it to the total
                                    AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                                    animation1.setDuration(10);
                                    subbutton.gridcontainer.setAlpha(1f);
                                    subbutton.gridcontainer.startAnimation(animation1);

                                    VibrationClass.vibeOn(context);
                                    activityCommunicator.passDataToActivity(b, b.getTitle(), b.getID(), b.getPrice(), qnty);
                                    popupWindow.dismiss();
                                }
                            }
                        }
                    });

                    ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.kill);
                    btnDismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                        }
                    });

                    popupWindow.setFocusable(true);
                    popupWindow.showAtLocation(((Activity)context).findViewById(R.id.operative),0,0,0);

                    return false;
                }
            });

            if(b.getTitle()=="!!FAKE0701!!"){
                subbutton.view.setVisibility(GONE);
            }else{
                subbutton.view.setVisibility(VISIBLE);
            }
            if (position < buttons.size()-1 ) {
                if (buttons.get(position + 1).getCat() == 1) {
                    int count = (position+1)%4;
                    if(count!=0) {

                        for (int i = 1; i <= 4 - count; i++) {
                            ButtonLayout fakeButton = new ButtonLayout();
                            fakeButton.setCat(0);
                            fakeButton.setTitle("!!FAKE0701!!");
                            fakeButton.setSubTitle("FAKE");
                            fakeButton.setImg("");
                            buttons.add(position + i, fakeButton);

                        }
                    }

                }
            }
        }
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
        public CustomTextView bigText;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            txtcontainer = (RelativeLayout) view.findViewById(R.id.text_container);
            title = (CustomTextView) view.findViewById(R.id.title);
            subtitle = (CustomTextView) view.findViewById(R.id.subtitle);
            image2 = (ImageView) view.findViewById(R.id.button_frame_img);
            bigText = (CustomTextView)view.findViewById(R.id.bigText_tv);
            //frame = (FrameLayout) view.findViewById(R.id.button_frame_img_border);
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
        public RelativeLayout gridcontainer;

        public SubButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            gridcontainer = (RelativeLayout) view.findViewById(R.id.grid_container);
            txtcontainer = (RelativeLayout) view.findViewById(R.id.text_container);
            title = (CustomTextView) view.findViewById(R.id.title);
            subtitle = (CustomTextView) view.findViewById(R.id.subtitle);

        }

        @Override
        public String toString() {
            return "ButtonHolder, Title: " + title.getText().toString();
        }
    }


    @Override
    public int getItemCount() {
        if(buttons!=null)
        return buttons.size();
        else return 0;
    }


    public void goToCategory(final int currCatID, int newCatID, String categoryTitle, int direction) {
        RelativeLayout above_rv = (RelativeLayout) ((Activity) context).findViewById(R.id.above_recyclerView);
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFd3d3d3); //light-gray background
        border.setStroke(3, 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8);
        this.currentCatTitle = categoryTitle;
        this.currentCatID = newCatID;


            if (newCatID == 0) {
                //going back to first page
                myCatId = newCatID;
                getCurrentCatButtonSet(newCatID, categoryTitle);
                deepnessLevel = 0;

            } else {
                //gettin' dooooown :)

                goForwardToButton(newCatID, categoryTitle);

                functionSetBackButton(newCatID, currentCatID);

                switch (direction) {
                    case 0:
                        //vado avanti

                        deepnessLevel += 1;
                        break;
                    case 1:
                        //vado indietro
                        deepnessLevel -= 1;
                        break;
                    case 2:
                        //torno indietro dai modifiers
                        break;
                }
            }
        }



    public void setGetBackFuncion(Integer newCatId, Integer currentId, String title){
        functionSetBackButton(newCatId, currentId);
        OperativeFragment.setButtonSet( currentId, title);
    }

    public void functionSetBackButton(Integer newCatId, Integer currentId){
        Cursor c = dbA.fetchByQuery("SELECT * FROM button WHERE id=" + currentId);
        if (!c.moveToFirst())
            c.moveToFirst();
        previousCatID = c.getInt(c.getColumnIndex(DatabaseAdapter.KEY_CAT_ID));
        if(previousCatID==0) previousCatTitle=null;
        else {
            c = dbA.fetchByQuery("SELECT * FROM button WHERE id=" + previousCatID);
            c.moveToFirst();
            previousCatTitle = c.getString(c.getColumnIndex(DatabaseAdapter.KEY_TITLE));
        }
        c.close();
        OperativeFragment.setBackButton(newCatId, previousCatID , previousCatTitle);
    }

    public void goForwardToButton(int catID, String categoryTitle) {
        //buttons.clear();
        oldButtons = buttons;
        //per qualche motivo dento le categorie partiamo da 1.......
        /*int position = 1;
        if(catID==0)
           */
        int position=1;
        ButtonLayout b = dbA.fetchButtonByQuery("SELECT * FROM button WHERE catID = " + catID + " AND position="+position+" LIMIT 1");
        if(b.getCat()==0 || b.getCat()==2){
            direction = "ASC";
        }else{
            direction = "DESC";
        }
        buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = " + catID + " AND id!=-30 AND id!=-20 ORDER BY isCat "+direction+", position");
        OperativeFragment.setButtonSet(catID, categoryTitle);
    }

    public void getCurrentCatButtonSet(int catID, String categoryTitle) {
      //  buttons.clear();
        int position=1;
        ButtonLayout b = dbA.fetchButtonByQuery("SELECT * FROM button WHERE catID = " + catID + " AND id!=-30 AND id!=-20 AND position="+position+" LIMIT 1");
        if (b.getCat() == 0) {
                direction = "ASC";
        } else {
                direction = "DESC";
        }

        buttons = dbA.fetchButtonsByQuery("SELECT * FROM button WHERE catID = " + catID + " AND id!=-30 AND id!=-20 ORDER BY isCat "+direction+", position");
        OperativeFragment.setButtonSet(catID, categoryTitle);
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
