package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.MainActivity;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.StaticValue;
import com.example.blackbox.model.VatModel;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.VISIBLE;

/**
 * Created by Fabrizio on 02/03/2018.
 */

public class VatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    private View popView;
    private PopupWindow popWindow;
    private ArrayList<VatModel> vats = new ArrayList<>();
    private ModifierAdapter.Modifier myModifier = null;
    private ButtonLayout myButton = null;
    private int selectedVatId = -1;
    public void setSelectedVatId(int value){selectedVatId = value;}
    private boolean vatState = true;
    public void setVatState(boolean value){
        vatState = value;}
    private int vat1 = 0;
    private int oldVat = 0;
    private GridAdapter myGrid = null;
    private ModifierAdapter myMod = null;
    private OperativeGridAdapter myOGrid = null;
    private boolean exiting = false;
    private boolean firstClick = false;

    private ButtonVatHolder selectedButton = null;
    private ButtonVatHolder selectedDeleteButton = null;

    //costruttore con il modifier
    public VatAdapter(DatabaseAdapter dbA, Context context, View popupView, PopupWindow popupWindow,
                      ModifierAdapter.Modifier m, ModifierAdapter myModAd, boolean s) {
        this.dbA = dbA;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        popView = popupView;
        this.vats = dbA.fetchVatArrayByQuery();
        popWindow = popupWindow;
        myModifier = m;
        myMod = myModAd;
        vatState = s;
    }

    //costruttore con un Button
    public VatAdapter(DatabaseAdapter dbA, Context context, View popupView, PopupWindow popupWindow,
                      ButtonLayout b, GridAdapter grid, boolean s) {
        this.dbA = dbA;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        popView = popupView;
        this.popWindow = popupWindow;
        this.vats = dbA.fetchVatArrayByQuery();
        myButton = b;
        vatState = s;
        myGrid = grid;
    }

    public VatAdapter(DatabaseAdapter dbA, Context context, View popupView, PopupWindow popupWindow,
                      ButtonLayout b, OperativeGridAdapter grid, boolean s) {
        this.dbA = dbA;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        popView = popupView;
        this.popWindow = popupWindow;
        this.vats = dbA.fetchVatArrayByQuery();
        myButton = b;
        vatState = s;
        myOGrid = grid;
    }

    //costruttore per i product (quindi con nulla di particolare)
    public VatAdapter(DatabaseAdapter dbA, Context context, View popupView, PopupWindow popupWindow, boolean s) {
        this.dbA = dbA;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        popView = popupView;
        this.vats = dbA.fetchVatArrayByQuery();
        this.popWindow = popupWindow;
        vatState = s;
    }

    //costruttore per quando creiamo un nuovo button (con il GridAdapter)
    public VatAdapter(DatabaseAdapter dbA, Context context, View popupView, PopupWindow popupWindow,
                      GridAdapter myGridAdapter, boolean s) {
        this.dbA = dbA;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        popView = popupView;
        this.vats = dbA.fetchVatArrayByQuery();
        this.popWindow = popupWindow;
        vatState = s;
        myGrid = myGridAdapter;
    }

    public int getSelectedVatId(){
        return this.selectedVatId;
    }

    public void setVats(ArrayList<VatModel> myVats){
        vats = myVats;
    }

    public ArrayList<VatModel> getVats(){return vats;}

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder vh;
        v = inflater.inflate(R.layout.vat_values_recycler, null);
        vh = new ButtonVatHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VatModel vat = vats.get(position);
        ButtonVatHolder button = (ButtonVatHolder) holder;
        button.vatContainer.setText(vat.toString());

        //TODO: settare bene il booleano
        CustomEditText vatInsert = ((CustomEditText)popView.findViewById(R.id.vat_value_insert));
        vatInsert.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(s.length() >= 1)
                    vatInsert.setSelection(s.length()-1);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() >= 1 && !firstClick) {
                    firstClick = true;
                    vatInsert.setText(editable.toString() );
                    /*if(!editable.toString().contains("%"))
                        vatInsert.setText(editable.toString() + "%");*/
                }
            }
        });

        button.view.setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onSwipeLeft()
            {
                button.vatContainer.setVisibility(View.GONE);
                button.deleteButton.setVisibility(View.VISIBLE);

                if (selectedDeleteButton != button && selectedDeleteButton != null)
                {
                    selectedDeleteButton.deleteButton.setVisibility(View.GONE);
                    selectedDeleteButton.vatContainer.setVisibility(VISIBLE);
                }

                //you can't have selected value and delete button on same time
                if(selectedButton != button && selectedButton != null){
                    selectedButton.selectButton.setVisibility(View.GONE);
                    selectedButton.vatContainer.setVisibility(VISIBLE);
                    selectedButton = null;
                    ((EditText)popView.findViewById(R.id.vat_value_insert)).setText("");
                    ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                }

                selectedDeleteButton = button;
                firstClick = false;

                button.deleteButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(StaticValue.blackbox)
                        {
                            button.deleteButton.setVisibility(View.GONE);
                            button.vatContainer.setVisibility(VISIBLE);
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair("vatId", String.valueOf(vat.getReferenceId())));
                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            params.add(new BasicNameValuePair("androidId", android_id));

                            ((MainActivity) context).callHttpHandler("/deleteVat", params);
                        }

                        else
                        {
                            dbA.deleteVatValue(vat.getReferenceId(), vat.getVatValue());
                            button.deleteButton.setVisibility(View.GONE);
                            button.vatContainer.setVisibility(VISIBLE);
                            ((EditText) popView.findViewById(R.id.vat_value_insert)).setText("");
                            ((EditText) popView.findViewById(R.id.vat_perc_insert)).setText("");
                            if (myGrid != null) {
                                ((CustomButton) popView.findViewById(R.id.prodTaxInsert)).setText("");
                                myGrid.updateVatValue(0);
                            } else if (myMod != null) {
                                ((CustomButton) popView.findViewById(R.id.modVatInsert)).setText("");
                            }
                            vat1 = 0;
                            vats = dbA.fetchVatArrayByQuery();
                            notifyDataSetChanged();
                            selectedDeleteButton = null;
                            ((EditText) popView.findViewById(R.id.vat_value_insert)).clearFocus();
                            firstClick = false;
                        }
                    }
                });
            }

            //questo è per modificare o per selezionare
            public void onClick(){
                button.vatContainer.setVisibility(View.GONE);
                button.selectButton.setVisibility(VISIBLE);
                int a = vat.getVatValue();
                button.selectButton.setText(vat.toString());

                if(selectedButton != button && selectedButton != null){
                    selectedButton.vatContainer.setVisibility(VISIBLE);
                    selectedButton.selectButton.setVisibility(View.GONE);
                }
                if(selectedDeleteButton != button && selectedDeleteButton != null){
                    selectedDeleteButton.deleteButton.setVisibility(View.GONE);
                    selectedDeleteButton.vatContainer.setVisibility(VISIBLE);
                    selectedDeleteButton = null;
                }
                selectedButton = button;

                selectedVatId = vat.getReferenceId();
                EditText vatModifier = (EditText)popView.findViewById(R.id.vat_value_insert);
                vatModifier.setText(""+vat.getVatValue());

                EditText perc = (EditText)popView.findViewById(R.id.vat_perc_insert);
                perc.setText(""+vat.getPerc());
                //vatModifier.setText(vat.getVatValue() );
                vatModifier.clearFocus();
                firstClick = false;
                oldVat = vat.getVatValue();

                ImageButton btnOk = (ImageButton)popView.findViewById(R.id.ok);
                btnOk.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        vatModifier.clearFocus();
                        firstClick = false;
                        if(vatState){
                            String textVat = String.valueOf(vatModifier.getText().toString());
                            textVat = textVat.replaceAll("%", "");
                            if(!textVat.equals("")){
                                if(textVat.matches("[0-9]*\\.[0-9]*")){
                                    float floatVat = Float.parseFloat(textVat);
                                    floatVat = floatVat*100;
                                    vat1 = (int) floatVat;
                                }
                                else
                                    vat1 = Integer.parseInt(textVat);
                            }
                            else {
                                Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            exiting = false;
                        }
                        //vatAdapter not from GridAdapter
                        if(myGrid == null && (myModifier == null && myButton == null)){

                            popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                            popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                            //((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(vat1 + "%");
                            ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(""+vat1 );
                            vatState = false;
                        }
                        //colorState = true -> esco dalla schermata dei colori e torno al ProductSetup,
                        //settando il valore booleano a false
                        else if(myGrid != null && (!vatState && myGrid.getColorState())){
                            popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                            popView.findViewById(R.id.colors_container).setVisibility(View.GONE);

                            myGrid.setColorState(false);
                            ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(""+vat1);
                           /* if(vat1 != 0)
                                ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(vat1 + "%");*/
                        }
                        //modifying vat Values in db
                        else if(oldVat != vat1 && oldVat != 0 && !exiting){
                            VatModel newVat = dbA.fetchVatByValueQuery(oldVat);
                            newVat.setVatValue(vat1);
                            dbA.updateVatValue(newVat);
                            vatModifier.setText("");
                            vatModifier.clearFocus();
                            if(selectedButton.selectButton.getVisibility() == VISIBLE) {
                                //selectedButton.selectButton.setText(vat1 + "%");
                                selectedButton.selectButton.setText(""+vat1 );

                            }
                            vats = dbA.fetchVatArrayByQuery();
                            notifyDataSetChanged();

                            vat1 = oldVat;

                        }
                        //specific case
                        else if(exiting) {
                            if(myGrid != null)
                                myGrid.okBehaviorInModifyingButton(popView, popWindow, myButton);
                            else if(myMod != null)
                                myMod.okBehaviourWhenCreatingModifier(myModifier,popView,popWindow);
                            exiting = false;
                        }
                        //ho passato un modifier, allora setto il suo valore
                        else if(oldVat == vat1){
                            //il modifier già esiste, devo modificarlo
                            if(myModifier != null){
                                if(vatState){
                                    if(dbA.checkIfModifierExists(myModifier.getID())){
                                        myModifier.setVat(vat1);
                                        ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                                        vatModifier.setText("");
                                        vatModifier.clearFocus();
                                        popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                                        popView.findViewById(R.id.ModifierSetup).setVisibility(VISIBLE);
                                        // ((CustomButton)popView.findViewById(R.id.modVatInsert)).setText(vat1 + "%");
                                        ((CustomButton)popView.findViewById(R.id.modVatInsert)).setText(""+vat1 );
                                        vatState = false;
                                        if(myGrid != null)
                                            myGrid.setVatState(false);
                                    }
                                    else{
                                        myModifier.setVat(vat1);
                                        ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                                        vatModifier.setText("");
                                        vatModifier.clearFocus();
                                        popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                                        popView.findViewById(R.id.ModifierSetup).setVisibility(VISIBLE);
                                        //((CustomButton)popView.findViewById(R.id.modVatInsert)).setText(vat1 + "%");
                                        ((CustomButton)popView.findViewById(R.id.modVatInsert)).setText(""+vat1 );
                                        vatState = false;
                                        //myMod.okBehaviourWhenCreatingModifier(myModifier, popView, popWindow);
                                    }
                                }
                                //il modifier non esiste ancora, setto il VAT e aspetto di crearlo
                                else if(!vatState){
                                    myModifier.setVat(vat1);
                                    myMod.okBehaviourWhenCreatingModifier(myModifier, popView, popWindow);
                                }
                            }
                            //qui il button esiste, sto modificando il valore VAT
                            if(myButton != null){
                                if(vatState){
                                    myButton.setVat(vat1);
                                    if(StaticValue.blackbox){
                                        VatModel myModel = dbA.fetchVatByValueQuery(vat1);
                                        String percentuale = ((EditText)popView.findViewById(R.id.vat_perc_insert)).getText().toString();
                                        if(myModel.getPerc()!=Integer.valueOf(percentuale)){
                                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                                            params.add(new BasicNameValuePair("perc", String.valueOf(percentuale)));
                                            params.add(new BasicNameValuePair("vatId", String.valueOf(myModel.getReferenceId())));
                                            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                            params.add(new BasicNameValuePair("androidId", android_id));

                                            ((MainActivity) context).callHttpHandler("/updateVat", params);

                                        }
                                    }else{

                                    }
                                    dbA.execOnDb("UPDATE button SET vat=" + vat1 + " WHERE id=" + myButton.getID() + ";");
                                    vatModifier.setText("");
                                    ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                                    vatModifier.clearFocus();
                                    popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                                    popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                                    //((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(vat1 + "%");
                                    ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(""+vat1);
                                    vatState = false;
                                    if(myGrid != null)
                                        myGrid.setVatState(false);
                                }
                                else if(!vatState){
                                    if(myButton.getID()>0)
                                        myGrid.okBehaviorInModifyingButton(popView, popWindow, myButton);
                                    else
                                        myGrid.okBehaviourInProductSetting(popView, popWindow);
                                }
                                //boh
                                else if(myGrid.getColorState()){
                                    //myButton.setColor();
                                }
                            }
                            else if(myButton == null && myModifier == null){
                                //sono nella finestra dei color
                                if(myGrid.getColorState()){
                                    if(selectedVatId != -1){
                                        //quindi devo solo selezionare il VAT e tornare al ProductSetup
                                        if(vatState){
                                            popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                                            popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                                            //((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(vat1 + "%");
                                            ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(""+vat1 );
                                            vatState = false;
                                        }
                                        else if(!vatState){
                                            myGrid.okBehaviourInProductSetting(popView,popWindow);
                                            //popWindow.dismiss();
                                        }
                                    }
                                    else{
                                        Toast.makeText(context, R.string.vat_value_not_found, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else if(!myGrid.getColorState()){
                                    if(vatState) {
                                        popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                                        popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                                        //((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(vat1 + "%");
                                        ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(""+vat1 );
                                        vatState = false;
                                    }
                                    else{
                                        myGrid.okBehaviourInProductSetting(popView, popWindow);
                                    }
                                }
                            }
                        }
                        //qui non ho passato nè modifier, nè button
                        else if((myButton == null && myModifier == null) && !myGrid.getColorState()){

                            popWindow.dismiss();
                        }
                        else{
                            if(!dbA.checkIfVatIsAdded(vat1))
                                okBehaviourInVatAdapter();
                            else {
                                throwConfirmPopup("duplicate");
                                ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                                vatModifier.setText("");
                                vatModifier.clearFocus();
                            }
                        }
                    }
                });
            }
        });

        ImageButton btnOk = (ImageButton)popView.findViewById(R.id.ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstClick = false;
                if(myGrid != null) {
                    //qui il button esiste, sto modificando il valore VAT
                    if(myButton != null){
                        if(vatState && !myGrid.getVatState()){
                            EditText vatModifier = (EditText)popView.findViewById(R.id.vat_value_insert);
                            myButton.setVat(vat1);
                            dbA.execOnDb("UPDATE button SET vat=" + vat1 + " WHERE id=" + myButton.getID() + ";");

                            ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                            vatModifier.setText("");
                            vatModifier.clearFocus();
                            popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                            popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                            //((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(vat1 + "%");
                            ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setText(vat1 );
                            vatState = false;
                            if(myGrid != null)
                                myGrid.setVatState(false);
                        }
                        else if(!vatState){
                            myGrid.okBehaviorInModifyingButton(popView, popWindow, myButton);
                        }
                        else if(myGrid.getVatState())
                            okBehaviourInVatAdapter();
                    }
                    else if(vatState)
                        okBehaviourInVatAdapter();
                    else if (!myGrid.getColorState())
                        myGrid.okBehaviourInProductSetting(popView,popWindow);
                    else
                        myGrid.okBehaviourInCreatingProduct(popView, popWindow);
                }
                //I've passed a modifier
                else if(myMod != null){
                    if(vatState && myModifier != null && myMod.getState()){
                        EditText vatModifier = (EditText)popView.findViewById(R.id.vat_value_insert);
                        myModifier.setVat(vat1);
                        dbA.execOnDb("UPDATE modifier SET vat=" + vat1 + " WHERE id=" + myModifier.getID() + ";");
                        ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                        vatModifier.setText("");
                        vatModifier.clearFocus();
                        popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                        popView.findViewById(R.id.ModifierSetup).setVisibility(VISIBLE);
                        // ((CustomButton)popView.findViewById(R.id.modVatInsert)).setText(vat1 + "%");
                        ((CustomButton)popView.findViewById(R.id.modVatInsert)).setText(vat1 );
                        vatState = false;
                        if(myGrid != null)
                            myGrid.setVatState(false);
                    }
                    else if(!myMod.getState()){
                        okBehaviourInVatAdapter();
                    }
                    //il modifier non esiste ancora, setto il VAT e aspetto di crearlo
                    else if(!vatState){
                        myModifier.setVat(vat1);
                        myMod.okBehaviourWhenCreatingModifier(myModifier, popView, popWindow);
                    }
                }
            }
        });

        ImageButton btnDismiss = (ImageButton)popView.findViewById(R.id.kill);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstClick = false;
                if(selectedButton != null)
                    dismissBehaviourInProductSettings(selectedButton);
                else if(selectedDeleteButton != null)
                    dismissBehaviourInProductSettings(selectedDeleteButton);
                else
                    dismissBehaviourInProductSettings(null);
            }
        });
    }

    public void deleteVatFromServer(int id){
        dbA.deleteVatValueFromServer(id);
        ((EditText) popView.findViewById(R.id.vat_value_insert)).setText("");
        ((EditText) popView.findViewById(R.id.vat_perc_insert)).setText("");
        if (myGrid != null) {
            ((CustomButton) popView.findViewById(R.id.prodTaxInsert)).setText("");
            myGrid.updateVatValue(0);
        } else if (myMod != null) {
            ((CustomButton) popView.findViewById(R.id.modVatInsert)).setText("");
        }
        vat1 = 0;
        vats = dbA.fetchVatArrayByQuery();
        notifyDataSetChanged();
        selectedDeleteButton = null;
        ((EditText) popView.findViewById(R.id.vat_value_insert)).clearFocus();
        firstClick = false;
    }

    public void okBehaviourInVatAdapter(){
        EditText vatModifier = (EditText)popView.findViewById(R.id.vat_value_insert);
        EditText percContainer= (EditText)popView.findViewById(R.id.vat_perc_insert);
        if(vatState){
            String textVat = String.valueOf(vatModifier.getText().toString());
            textVat = textVat.replaceAll("%", "");
            if(textVat.equals("")){
                Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
            }
            else if(textVat.matches("[0-9]*\\.[0-9]*")){
                float floatVat = Float.parseFloat(textVat);
                floatVat = floatVat*100;
                vat1 = (int) floatVat;
            }
            else
                vat1 = Integer.parseInt(textVat);
            //adding new Vat Value
            if(vat1 != 0 && !dbA.checkIfVatIsAdded(vat1)){
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


                    ((MainActivity) context).callHttpHandler("/insertVat", params);
                }else {
                    dbA.addVatValue(vat1);
                    vats = dbA.fetchVatArrayByQuery();
                    notifyDataSetChanged();
                    ((EditText) popView.findViewById(R.id.vat_perc_insert)).setText("");
                    vatModifier.setText("");
                    vatModifier.clearFocus();
                    Toast.makeText(context, R.string.new_vat_value_inserted, Toast.LENGTH_SHORT).show();
                }
            }
            else if(vat1 == 0){
                Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
            }
            else if(dbA.checkIfVatIsAdded(vat1)){
                //it throws a simple popup
                throwConfirmPopup("duplicate");
                vatModifier.setText("");
                vatModifier.clearFocus();
                ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
            }
        }
        else if(vats.isEmpty()){
            String textVat = String.valueOf(vatModifier.getText().toString());
            textVat = textVat.replaceAll("%", "");
            if(textVat.equals("")){
                Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
            }
            else if(textVat.matches("[0-9]*\\.[0-9]*")){
                float floatVat = Float.parseFloat(textVat);
                floatVat = floatVat*100;
                vat1 = (int) floatVat;
            }
            else
                vat1 = Integer.parseInt(textVat);
            //adding new vat value
            if(vat1 != 0 && !dbA.checkIfVatIsAdded(vat1)){
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


                    ((MainActivity) context).callHttpHandler("/insertVat", params);
                }else {
                    dbA.addVatValue(vat1);
                    vats = dbA.fetchVatArrayByQuery();
                    notifyDataSetChanged();
                    vatModifier.setText("");
                    vatModifier.clearFocus();
                    ((EditText) popView.findViewById(R.id.vat_perc_insert)).setText("");
                    Toast.makeText(context, R.string.new_vat_value_inserted, Toast.LENGTH_SHORT).show();
                }
            }
            else if(vat1 == 0){
                Toast.makeText(context, R.string.insert_a_valid_vat_value, Toast.LENGTH_SHORT).show();
            }
            else if(dbA.checkIfVatIsAdded(vat1)){
                //it thorws a simple popup
                throwConfirmPopup("duplicate");
                vatModifier.setText("");
                vatModifier.clearFocus();
                ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                firstClick = false;
            }
        }
        else if(myMod != null){
            myMod.okBehaviourWhenCreatingModifier(myModifier, popView, popWindow);
        }
        else if(myGrid != null){
            if(myGrid.getColorState()){
                myGrid.okBehaviourForSettingColor(popView);
            }
            else if(!myGrid.getColorState())
                myGrid.okBehaviourInProductSetting(popView,popWindow);
        }
        else{
            popWindow.dismiss();
        }
    }

    public void dismissBehaviourInProductSettings(ButtonVatHolder button){
        if(vatState){
            selectedVatId = -1;
            if(!((EditText)popView.findViewById(R.id.vat_value_insert)).getText().toString().equals("")){
                ((EditText)popView.findViewById(R.id.vat_value_insert)).setText("");
                ((EditText)popView.findViewById(R.id.vat_value_insert)).clearFocus();
                ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                exiting = true;
                firstClick = false;
                if(button != null){
                    if(button.selectButton.getVisibility() == VISIBLE){
                        button.selectButton.setVisibility(View.GONE);
                        button.vatContainer.setVisibility(VISIBLE);
                        selectedButton = null;
                        oldVat = 0;
                    }
                    else if(button.deleteButton.getVisibility() == VISIBLE){
                        button.deleteButton.setVisibility(View.GONE);
                        button.vatContainer.setVisibility(VISIBLE);
                        selectedDeleteButton = null;
                    }
                }
            }
            else if(button != null){
                if(button.selectButton.getVisibility() == VISIBLE){
                    button.selectButton.setVisibility(View.GONE);
                    button.vatContainer.setVisibility(VISIBLE);
                    selectedButton = null;
                    oldVat = 0;
                }
                else if(button.deleteButton.getVisibility() == VISIBLE){
                    button.deleteButton.setVisibility(View.GONE);
                    button.vatContainer.setVisibility(VISIBLE);
                    selectedDeleteButton = null;
                }
            }
            //I've passed a modifier
            else if(myMod != null){
                popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                popView.findViewById(R.id.ModifierSetup).setVisibility(VISIBLE);
                ((CustomButton)popView.findViewById(R.id.modVatInsert)).setHint(R.string.vat);
                ((EditText)popView.findViewById(R.id.vat_value_insert)).setText("");
                ((EditText)popView.findViewById(R.id.vat_value_insert)).clearFocus();
                ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                vatState = false;
            }
            //I've passed a Button
            else if(myGrid != null){
                popView.findViewById(R.id.new_Popup_Vat_Insert).setVisibility(View.GONE);
                popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                ((CustomButton)popView.findViewById(R.id.prodTaxInsert)).setHint(R.string.vat);
                ((EditText)popView.findViewById(R.id.vat_value_insert)).setText("");
                ((EditText)popView.findViewById(R.id.vat_value_insert)).clearFocus();
                ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                vatState = false;
                myGrid.setVatState(false);
            }
        }
        else{
            if(myGrid != null){
                if(myGrid.getColorState()){
                    popView.findViewById(R.id.colors_container).setVisibility(View.GONE);
                    popView.findViewById(R.id.ProductSetup).setVisibility(VISIBLE);
                    myGrid.setColorState(false);
                }
                else
                    popWindow.dismiss();
            }
            else{
                ((CustomEditText)popView.findViewById(R.id.vat_value_insert)).setText("");
                ((EditText)popView.findViewById(R.id.vat_perc_insert)).setText("");
                popWindow.dismiss();
            }
        }
    }

    //popup after adding a new reservation
    public void throwConfirmPopup(String header){
        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_confirm_dialog, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)popupView.findViewById(R.id.confirm_window)
                .getLayoutParams();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        int dpHeight = outMetrics.heightPixels;
        int t = (int)(dpHeight-52)/2 - rlp.height/2;
        rlp.topMargin = t;

        if(header.equals("duplicate"))
            ((CustomTextView)popupView.findViewById(R.id.confirm_header)).setText(R.string.duplicate_vat_value);

        CustomButton confirmButton = (CustomButton)popupView.findViewById(R.id.res_confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                firstClick = false;
            }
        });

        /** THROW POPUP WINDOW AFTER SETTING EVERYTHING UP **/
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(((Activity)context).findViewById(R.id.main),0,0,0);
    }

    public boolean getVatState(){
        return vatState;
    }

    @Override
    public int getItemCount() {

        if(vats == null || vats.isEmpty())
            return 0;
        return vats.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    /** HOLDER **/
    class ButtonVatHolder extends RecyclerView.ViewHolder {
        public View view;
        public LinearLayout linearLayout;
        public CustomTextView vatContainer;
        public CustomButton deleteButton;
        public CustomButton selectButton;

        public ButtonVatHolder(View itemView) {
            super(itemView);
            view = itemView;
            linearLayout = (LinearLayout) view.findViewById(R.id.vat_value_recycler);
            vatContainer = (CustomTextView) view.findViewById(R.id.vat_value_container);
            deleteButton = (CustomButton) view.findViewById(R.id.custom_delete_vat_button);
            selectButton = (CustomButton) view.findViewById(R.id.custom_select_vat_button);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: " + vatContainer.getText().toString();}
    }

}