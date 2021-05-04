package com.example.blackbox.model;

/**
 * Created by tiziano on 27/06/17.
 */

import android.content.Context;

import com.example.blackbox.adapter.OModifierAdapter;
import com.example.blackbox.adapter.OModifierGroupAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.utils.db.DatabaseAdapter;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.blackbox.fragments.CalculatorFragment.roundDecimal;

public class CashButtonListLayout  implements Comparable<CashButtonListLayout> {

    private int id;
    private int modifierId;
    private String title;
    private Float price;
    private Integer quantity;
    private String note = null;
    private int vat = StaticValue.staticVat;
    private int prodBillId = -1;
    private int prodPosition= -1;
    private int groupID = 0;

    private Context context;

    /*public int returnVatValue(int oldVat){
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
        }*/



    /**
     * GETTERS & SETTERS
     * !! getID != getId : the former is here defined, the latter is proper of RelativeLayout
     * !! and returns the id setted with setId ( which is not setID) !!
     */

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CashButtonListLayout){
            CashButtonListLayout element = (CashButtonListLayout) obj;
            if(element != null && this.getModifierId() == element.getModifierId() ){
                return true;
            }
        }
        return false;
    }

    public int compareTo(CashButtonListLayout other){
        DatabaseAdapter dbA = new DatabaseAdapter(context);
        OModifierAdapter.OModifier modifier = dbA.fetchSingleOModifiersByQuery("SELECT * FROM modifier WHERE id=" + this.modifierId);
        OModifierGroupAdapter.OModifiersGroup group = dbA.fetchSingleModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + modifier.getGroupID());

        OModifierAdapter.OModifier omodifier = dbA.fetchSingleOModifiersByQuery("SELECT * FROM modifier WHERE id=" + other.modifierId);
        OModifierGroupAdapter.OModifiersGroup ogroup = dbA.fetchSingleModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + omodifier.getGroupID());


        if(group.getPosition() < ogroup.getPosition()) return -1;
        if(group.getPosition() > ogroup.getPosition()) return 1;
        //If here, startTime == other.startTime
        if(modifier.getPosition() < omodifier.getPosition()) return -1;
        if(modifier.getPosition() > omodifier.getPosition()) return 1;
        return 0;
    }

    public int getPosition() {
        DatabaseAdapter dbA = new DatabaseAdapter(context);
       // dbA.showData("modifier");
        if(this.modifierId!=-15) {
            OModifierAdapter.OModifier modifier = dbA.fetchSingleOModifiersByQuery("SELECT * FROM modifier WHERE id=" + this.modifierId);
            return modifier.getPosition();
        }else return 10000000;
    }

    public int getGroupPosition() {
        DatabaseAdapter dbA = new DatabaseAdapter(context);
       // dbA.showData("modifier");
        if(this.modifierId!=-15) {
            OModifierAdapter.OModifier modifier = dbA.fetchSingleOModifiersByQuery("SELECT * FROM modifier WHERE id=" + this.modifierId);
            OModifierGroupAdapter.OModifiersGroup group = dbA.fetchSingleModifiersGroupByQuery("SELECT * FROM modifiers_group WHERE id=" + modifier.getGroupID());
            return group.getPosition();

        }else{
            return 10000000;
        }
    }

    public int getModifierId() {return modifierId;}
    public int getID(){ return id;}
    public String getTitle(){ return title;}
    public String getPrice(){ return price.toString();}
    public Float getPriceFloat(){ return price;}
    public String getQuantity(){ return quantity.toString();}
    public Integer getQuantityInt(){ return quantity;}
    public String getTotal(Integer quantity) {
        return String.format("%.2f", roundDecimal(quantity*getPriceFloat(),2) );
               // .replaceAll(",", ".");
       /* Float rounded = CalculatorFragment.roundDecimal(quantity * getPriceFloat(), 2);
        return String.valueOf(rounded);*/
    }
    public String getNote(){
        return note;
    }

    public void setModifierId(int id){modifierId = id;}
    public void setID(int id){ this.id = id;}
    public void setPrice(float f){ price = f;}
    public void setTitle(String s){ title = s;}
    public void setQuantity(int b){ quantity = b;}
    public void setNote(String n ){ note= n;}


    public String toString(){ return title;}

    public int getVat() {
        return vat;
    }

    public void setVat(int vat) {
        this.vat =vat;
    }

    public static ArrayList<CashButtonListLayout> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new CashButtonListLayout.JsonDateDeserializer()).create();
        Type type = new TypeToken<List<CashButtonListLayout>>(){}.getType();
        ArrayList<CashButtonListLayout> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }

    public int getProdPosition() {
        return prodPosition;
    }

    public void setProdPosition(int prodPosition) {
        this.prodPosition = prodPosition;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public static class JsonDateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            long s = json.getAsJsonPrimitive().getAsLong();
            Date d = new Date(s);
            return d;
        }
    }
}
