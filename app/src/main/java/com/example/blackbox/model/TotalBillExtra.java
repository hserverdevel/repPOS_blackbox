package com.example.blackbox.model;


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

public class TotalBillExtra {
    private int id;
    private float discountTotal;
    private int homage;
    private int billTotalId;

    public static TotalBillExtra fromJson(JSONObject jsonObject) {
        TotalBillExtra totalBill = new TotalBillExtra();
        // Deserialize json into object fields
        try {
            totalBill.id = jsonObject.getInt("id");
            totalBill.discountTotal= (float) jsonObject.getDouble("discountTotal");
            totalBill.homage = jsonObject.getInt("homage");
            totalBill.billTotalId = jsonObject.getInt("billTotalId");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return totalBill;
    }

    public static ArrayList<TotalBillExtra> fromJsonArray(JSONArray jsonObject) {
        //Gson gson = new Gson();
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
        Type type = new TypeToken<List<TotalBillExtra>>(){}.getType();
        ArrayList<TotalBillExtra> list = gson.fromJson(jsonObject.toString(), type);
       return list;


    }

    public float getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(float discountTotal) {
        this.discountTotal = discountTotal;
    }

    public int getHomage() {
        return homage;
    }

    public void setHomage(int homage) {
        this.homage = homage;
    }

    public static class JsonDateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            long s = json.getAsJsonPrimitive().getAsLong();
            Date d = new Date(s);
             return d;
        }
    }

}
