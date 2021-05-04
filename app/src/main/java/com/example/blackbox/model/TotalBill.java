package com.example.blackbox.model;


import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiziano on 8/31/17.
 */

public class TotalBill {

    private int id;
    private float total;
    private int paid;
    private int billNumber;
    private Date creationTime;
    private Date payTime;
    private String device_name;

    public int getId(){return id;}
    public float getTotal(){return total;}
    public int getPaid(){return paid;}
    public int getBillNumber(){return billNumber;}
    public Date getCreationTime(){return creationTime;}
    public Date getPaidTime(){return payTime;}

    public void setId(int idToSet){id= idToSet;}
    public void setTotal(float t){total=t;}
    public void setPaid(int p){paid=p;}
    public void setBillNumber(int i){billNumber=i;}
    public void setCreationTime(Date t){creationTime = t;}
    public void setPaidTime(Date t){payTime = t;}

    public static TotalBill fromJson(JSONObject jsonObject) {
        TotalBill  totalBill = new TotalBill ();
        // Deserialize json into object fields
        try {
            totalBill.id = jsonObject.getInt("id");
            totalBill.total= (float) jsonObject.getDouble("total");
            totalBill.paid = jsonObject.getInt("paid");
            totalBill.billNumber = jsonObject.getInt("billNumber");
            long date= jsonObject.getLong("creationTime");
            totalBill.creationTime =new Date(date);
            if(jsonObject.has("payTime")) {
                long date1 = jsonObject.getLong("creationTime");
                totalBill.payTime  =new Date(date1);
            }
            totalBill.device_name = jsonObject.getString("device_name");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return totalBill;
    }

    public static ArrayList<TotalBill> fromJsonArray(JSONArray jsonObject) {
        //Gson gson = new Gson();
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
        Type type = new TypeToken<List<TotalBill>>(){}.getType();
        ArrayList<TotalBill> list = gson.fromJson(jsonObject.toString(), type);
       return list;


    }

    public String getDevice_name() {
        if(device_name!=null)
            return device_name;
        else
            return "Unknown";
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public static class JsonDateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            long s = json.getAsJsonPrimitive().getAsLong();
            Date d = new Date(s);
             return d;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "TotalBill: {id: " + id + ", total: " + total + ", paid: " + paid + ", billNumber: " + billNumber + ", creationTime: " + creationTime + ", payTime: " + payTime + ", device: " + device_name + "}";
    }


}
