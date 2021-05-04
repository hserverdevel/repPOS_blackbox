package com.example.blackbox.model;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tiz
 */

public class Vat {

    private int id;
    private int value;
    private int perc = 0;



    public static Vat fromJson(JSONObject jsonObject) {
        Vat mgA = new Vat();
        // Deserialize json into object fields
        try {
            mgA.id= jsonObject.getInt("id");
            mgA.value= jsonObject.getInt("value");
            mgA.perc= jsonObject.getInt("perc");
         } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return mgA;
    }

    public static ArrayList<Vat> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Vat>>(){}.getType();
        ArrayList<Vat> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getPerc() {
        return perc;
    }

    public void setPerc(int perc) {
        this.perc = perc;
    }
}
