package com.example.blackbox.model;

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
 * Created by tiziano on 2/6/18.
 */

public class Customer
{
    private int id;
    private String description = "";
    private Integer position;
    private boolean delete = false;
    private boolean active = true;
    private int customerId = -1;

    /**
     * SETTER
     */
    public void setDescription(String s) {
        this.description = s;
    }

    public void setPosition(int p){
        this.position = p;
    }

    public void setDelete(boolean b){
        this.delete = b;
    }

    public void setActive(boolean b){
        this.active= b;
    }

    public void setCustomerId(int i) {this.customerId = i;}

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }



    /**
     * GETTER
     */

    public String getDescription(){return this.description;}
    public int getPosition(){return this.position;}
    public boolean getDelete(){return this.delete;}
    public boolean getActive(){return this.active;}
    public int getCustomerId(){return this.customerId;}

    public static ArrayList<Customer> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new Customer.JsonDateDeserializer()).create();
        Type type = new TypeToken<List<Customer>>(){}.getType();
        ArrayList<Customer> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }


    public static class JsonDateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            long s = json.getAsJsonPrimitive().getAsLong();
            Date d = new Date(s);
            return d;
        }
    }
}
