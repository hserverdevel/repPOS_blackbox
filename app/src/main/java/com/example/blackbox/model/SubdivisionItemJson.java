package com.example.blackbox.model;


import com.example.blackbox.adapter.OrderListAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by DavideLiberato on 20/07/2017.
 */

public class SubdivisionItemJson {

    private int mode;
    private Float owed_money;
    private Float percentage;
    private int number_subdivision;
    private ArrayList<TwoString> items_map;
    private ArrayList<CashButtonLayout> items;
    private boolean isPaid = false;
    private boolean isShow = false;
    private float discount = 0.0f;
    private boolean homage = false;
    private int paymentType;
    private double fidelity = 0.0;

    public SubdivisionItemJson(SubdivisionItem i) {
        mode = i.getMode();
        owed_money = i.getOwed_money();
        percentage = i.getPercentage();
        number_subdivision = i.getNumber_subdivision();
        items = i.getItems();
        isPaid = i.isPaid();
        isShow = i.getIsShow();
        discount = i.getDiscount();
        homage = i.isHomage();
        paymentType = i.getPaymentType();
        fidelity = i.getFidelity();
    }

    public SubdivisionItemJson(){
        items = new ArrayList<>();
    }


    public void setIsShow(Boolean b){this.isShow = b;}
    public boolean getIsShow(){return this.isShow;}

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public Float getOwed_money() {
        return owed_money;
    }

    public void setOwed_money(Float owed_money) {
        this.owed_money = owed_money;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    public String getModeString(){
        switch(mode){
            case -1:
                return "Total Bill";
            case OrderListAdapter.PERCENTAGE_MODE:
                return "Per Amount";
            case OrderListAdapter.ITEM_MODE:
                return "Per Item";
            case OrderListAdapter.PERSON_MODE:
                return "Per Person";
            case OrderListAdapter.NUMBER_MODE:
                return "Per Number";
            case OrderListAdapter.PARTIAL_MODE:
                return "Partial";
            default:
                return "";
        }
    }

    public ArrayList<CashButtonLayout> getItems() {
        return items;
    }

    public void setItems(ArrayList<CashButtonLayout> items) {
        this.items = items;
    }

    public int getNumber_subdivision() {
        return number_subdivision;
    }

    public void setNumber_subdivision(int number_subdivision) {
        this.number_subdivision = number_subdivision;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public boolean isHomage() {
        return homage;
    }

    public void setHomage(boolean homage) {
        this.homage = homage;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public ArrayList<TwoString> getItems_map() {
        return items_map;
    }

    public void setItems_map(ArrayList<TwoString> items_map) {
        this.items_map = items_map;
    }

    public static ArrayList<SubdivisionItemJson> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new SubdivisionItemJson.JsonDateDeserializer()).create();
        Type type = new TypeToken<List<SubdivisionItemJson>>(){}.getType();
        ArrayList<SubdivisionItemJson> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }

    public double getFidelity() {
        return fidelity;
    }

    public void setFidelity(double fidelity) {
        this.fidelity = fidelity;
    }

    public static class JsonDateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            long s = json.getAsJsonPrimitive().getAsLong();
            Date d = new Date(s);
            return d;
        }
    }
}
