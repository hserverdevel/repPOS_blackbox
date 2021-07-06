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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Fabrizio on 18/03/2019.
 */

public class WaitingListModel {

    private int id;
    private String name;
    private String surname;
    private int adults;
    private int children;
    private int disabled;
    private Date time;

    public WaitingListModel(){

    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public String getSurname(){return surname;}
    public void setSurname(String s){this.surname = s;}


    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }


    public int getAdults() {
        return adults;
    }
    public void setAdults(int adults) {
        this.adults = adults;
    }


    public int getChildren() {
        return children;
    }
    public void setChildren(int children) {
        this.children = children;
    }


    public int getDisabled() {
        return disabled;
    }
    public void setDisabled(int disabled) {
        this.disabled = disabled;
    }





    public static WaitingListModel fromJson(JSONObject jsonObject)
    {
        WaitingListModel wt  = new WaitingListModel();

        // Deserialize json into object fields
        try
        {
            wt.setId(jsonObject.getInt("id"));
            wt.name = jsonObject.getString("name");
            wt.adults = jsonObject.getInt("adults");
            wt.children = jsonObject.getInt("children");
            wt.disabled = jsonObject.getInt("disabled");
            wt.time = new Date(jsonObject.getLong("time"));
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        // Return new object
        return wt;
    }


    public static ArrayList<WaitingListModel> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException
                    {
                        return new Date(jsonElement.getAsJsonPrimitive().getAsLong());
                    }
                })
                .create();

        Type type = new TypeToken<List<WaitingListModel>>(){}.getType();
        ArrayList<WaitingListModel> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }


}
