package com.example.blackbox.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ButtonLayout
{
    private int id;
    private int pos;
    private int catID;
    private float price;
    private int vat;
    private String barcode;
    private String productCode;
    private String title;
    private String subTitle;
    private String img;
    private int color = 0xFF000000;
    private int isCat;
    private int quantity = 1;   //default value
    private int printerId = -1;
    private int fidelity_discount = 0;
    private int fidelity_credit = 0;
    private double credit_value = 0.0;


    //TODO remove this, but there are some references, I don't have time now
    public ButtonLayout(Context context){
    }

    public ButtonLayout() {

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

    /**
     * GETTERS & SETTERS
     * !! getID != getId : the former is here defined, the latter is proper of RelativeLayout
     * !! and returns the id setted with setId ( which is not setID) !!
     */

    public int getID(){ return id;}
    public int getPos(){ return pos;}
    public String getTitle(){ return title;}
    public String getSubTitle(){ return subTitle;}
    public String getImg(){ return img;}
    public int getColor(){return color==0?0xFF000000:color;}
    public float getPrice(){ return price;}
    public int getVat(){return vat;}
    public String getBarcode(){ return barcode;}
    public String getProductCode(){ return productCode;}
    public int getCatID(){ return catID;}
    public int getCat(){ return isCat; }
    public int getQuantity(){return quantity;}
    public int getPrinterId(){return printerId;}


    public void setID(int id){ this.id = id;}
    public void setPos(int p){ pos = p;}
    public void setPrice(float f){ price = f;}
    //public void setVat(int v){vat = returnVatValue(v);}
    public void setVat(int v){vat = v;}
    public void setBarcode(String s){ barcode = (s == null? "":s);}
    public void setProductCode(String s){ productCode = (s == null? "":s);}
    public void setCatID(int id){ catID = id;}
    public void setTitle(String s){ title = s;}
    public void setSubTitle(String s){ subTitle = s;}
    public void setImg(String s){ img = s==null?"":s;}
    public void setColor(int c){ color = c;}
    public void setCat(int b){ isCat = b;}
    public void setQuantity(int value){quantity = value;}
    public void setPrinterId(int i) {printerId = i;}

    public static ButtonLayout getInstance(Context c){ return new ButtonLayout(c);}

    public static ButtonLayout fromJson(JSONObject jsonObject) {
        ButtonLayout  cbl = new ButtonLayout ();
        // Deserialize json into object fields
        try {
            cbl.id = jsonObject.getInt("id");
            cbl.title= jsonObject.getString("title");
            if(jsonObject.has("subTitle"))
                cbl.subTitle= jsonObject.getString("subTitle");
            if(jsonObject.has("img")) {
                cbl.img = jsonObject.getString("img");
                if(cbl.img==null) cbl.img="";
            }else cbl.img="";
            if(jsonObject.has("color"))
                cbl.color = jsonObject.getInt("color");
            cbl.pos = jsonObject.getInt("pos");
            if(jsonObject.has("price"))
                cbl.price = (float) jsonObject.getDouble("price");
            cbl.vat = jsonObject.getInt("vat");
            if(jsonObject.has("barcode")) {
                cbl.barcode = jsonObject.getString("barcode");
                if(cbl.barcode==null) cbl.barcode="";
            }else cbl.barcode ="";
            if(jsonObject.has("productCode"))
                cbl.productCode = jsonObject.getString("productCode");
            cbl.catID = jsonObject.getInt("catID");
            cbl.isCat = jsonObject.getInt("isCat");
            if(jsonObject.has("printerId"))
                cbl.printerId = jsonObject.getInt("printerId");
            cbl.fidelity_discount = jsonObject.getInt("fidelity_discount");
            cbl.fidelity_credit= jsonObject.getInt("fidelity_credit");
            if(jsonObject.has("credit_value"))
                cbl.credit_value = jsonObject.getDouble("credit_value");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return cbl;
    }

    public static ArrayList<ButtonLayout> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ButtonLayout>>(){}.getType();
        ArrayList<ButtonLayout> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }

    public int getFidelity_discount() {
        return fidelity_discount;
    }

    public void setFidelity_discount(int fidelity_discount) {
        this.fidelity_discount = fidelity_discount;
    }

    public int getFidelity_credit() {
        return fidelity_credit;
    }

    public void setFidelity_credit(int fidelity_credit) {
        this.fidelity_credit = fidelity_credit;
    }

    public double getCredit_value() {
        return credit_value;
    }

    public void setCredit_value(double credit_value) {
        this.credit_value = credit_value;
    }
}
