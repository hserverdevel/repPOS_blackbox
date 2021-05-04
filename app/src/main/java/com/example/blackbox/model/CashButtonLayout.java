package com.example.blackbox.model;

/**
 * Created by tiziano on 27/06/17.
 */

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.blackbox.fragments.CalculatorFragment.roundDecimal;

public class CashButtonLayout implements Comparable<CashButtonLayout>
{
    private int id;
    private int productId;
    private String title;
    private Float price;
    private Integer quantity;
    private Integer selected_quantity = 1;
    private ArrayList<CashButtonListLayout> cashList;
    private Boolean isDelete;
    private Boolean modifyModifier;
    private boolean isSelected = false;
    private Boolean toDelete = false;
    private int position;
    private int homage;
    private Float discount;
    private boolean isPaid;
    private int vat = 0;

    private int clientPosition;

    private CashButtonLayout originalCBL = null;
    private int percentage = 1;
    private boolean isSplit = false;
    private int printerId = -1;
    private int course = 1;
    private int printed = 0;


    /**
     * fro GSON
     */
    public CashButtonLayout(){


    }

   /* public int returnVatValue(int oldVat){
        if(oldVat==0){
            return StaticValue.staticVat;
        }else {
            int[] vats = StaticValue.vats;
            int distance = Math.abs(vats[0] - oldVat);
            int idx = 0;
            for (int c = 1; c < vats.length; c++) {
                int cdistance = Math.abs(vats[c] - oldVat);
                if (cdistance < distance) {
                    idx = c;
                    distance = cdistance;
                }
            }
            return idx;
        }
    }*/


/*

    public CashButtonLayout(Context context){
        this.context = context;
    }
*/

    /**
     * GETTERS & SETTERS
     * !! getID != getId : the former is here defined, the latter is proper of RelativeLayout
     * !! and returns the id setted with setId ( which is not setID) !!
     */

    public CashButtonLayout getCashButtonLayout(){ return this;}
    public int getID(){ return id;}
    public int getProductId(){return productId;}
    public String getTitle(){ return title;}
    public String getPrice(){ return price.toString();}
    public Float getPriceFloat(){ return price;}
    public String getQuantity(){ return quantity.toString();}
    public Integer getQuantityInt(){
        if(quantity!=null)
        return quantity;
        else return -1;
    }
    public ArrayList<CashButtonListLayout> getCashList() {return cashList;}
    public Boolean getIsDelete() {if(isDelete!=null)return isDelete;
                                  else return false;
    }
    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace,BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public Boolean getIsModifyModifier() {return modifyModifier;}
    public String getTotal(Integer quantity) {
        return String.format("%.2f", roundDecimal(quantity*getPriceFloat(),2) );
                //.replaceAll(",", ".");
        //return String.valueOf( round(quantity*getPriceFloat(), 2));
    }
    public Boolean getToDelete() {if(toDelete!=null)return toDelete;
                                  else return false;
    }
    public int getPosition() {return position;
    }
    public int getHomage(){return homage;}
    public Float getDiscount(){
        if(discount==null) return 0.0f;
        else return discount;
    }
    public boolean getIsPaid(){return isPaid;}
    public int getClientPosition(){return clientPosition;}


    public void setID(int id){ this.id = id;}
    public void setProductId(int id) {productId = id;}
    public void setPrice(float f){ price = f;}
    public void setTitle(String s){ title = s;}
    public void setQuantity(int b){ quantity = b;}
    public void setModifyModifier(Boolean b){ modifyModifier = b;}
    public void setToDelete(Boolean b){
        toDelete = b;
    }
    public void setCashList(CashButtonListLayout cbll) {
        if(cashList!=null) cashList.add(cbll);
        else {
            cashList = new ArrayList<CashButtonListLayout>();
            cashList.add(cbll);
        }
    }
    public void setNewCashList(ArrayList<CashButtonListLayout> cblls) {
            cashList = new ArrayList<CashButtonListLayout>();
            cashList = cblls;

    }

    public void resetCashList() {
        cashList = new ArrayList<CashButtonListLayout>();
    }
    public void setIsDelete(Boolean b) {
        isDelete = b;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

   /* public Integer getSelected_quantity() {
        return selected_quantity;
    }

    public void setSelected_quantity(Integer selected_quantity) {
        this.selected_quantity = selected_quantity;
    }

    public void increaseSelected_quantity(){ if(quantity-selected_quantity>0)selected_quantity++;}

    public void decreaseSelected_quantity(){ if(selected_quantity>0)selected_quantity--;}*/

    public void setPosition(int p){ this.position= p;}

    public void setHomage(int h){this.homage = h;}

    public void setDiscount(Float d){
        if(this.discount!=null) this.discount=this.discount+d;
        else this.discount = d;
    }

    public void setNewDiscount(Float d){
        if(this.discount!=null) this.discount=d;
        else this.discount = d;
    }

    public void setIsPaid(boolean b){this.isPaid = b;}

    public void setClientPosition(int p){this.clientPosition = p;}

    public int getCashListSize(){
        if(cashList==null) return 0;
        else return cashList.size();
    }


    /*@Override
    public int compareTo(@NonNull CashButtonLayout button) {
        int compareage=((CashButtonLayout)button).getPosition();
        *//* For Ascending order*//*
        return this.clientPosition-compareage;
    }*/

    @Override
    public int compareTo(@NonNull CashButtonLayout button) {
        int compareage=((CashButtonLayout)button).getClientPosition();
        /* For Ascending order*/
        if(this.clientPosition == compareage){
            int pos =((CashButtonLayout)button).getPosition();
            return this.position-pos;
        }else
            return this.clientPosition-compareage;
    }

    public CashButtonLayout getOriginalCBL() {
        return originalCBL;
    }

    public void setOriginalCBL(CashButtonLayout originalCBL) {
        this.originalCBL = originalCBL;
    }

    public int getVat() {
        return vat;
    }

    public void setVat(int vat) {
        this.vat = vat;
       /* if(vat>=1 && vat<5)
            this.vat = vat;
        else this.vat = returnVatValue(vat);*/
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public boolean isSplit() {
        return isSplit;
    }

    public void setSplit(boolean split) {
        isSplit = split;
    }

    public int getPrinterId() {
        return printerId;
    }

    public void setPrinterId(int printerId) {
        this.printerId = printerId;
    }

    public static ArrayList<CashButtonLayout> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new CashButtonLayout.JsonDateDeserializer()).create();
        Type type = new TypeToken<List<CashButtonLayout>>(){}.getType();
        ArrayList<CashButtonLayout> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public int getPrinted() {
        return printed;
    }

    public void setPrinted(int printed) {
        this.printed = printed;
    }

    public static class JsonDateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            long s = json.getAsJsonPrimitive().getAsLong();
            Date d = new Date(s);
            return d;
        }
    }



}
