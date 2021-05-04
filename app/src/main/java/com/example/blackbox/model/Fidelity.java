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
 * Created by tiziano on 4/3/19.
 */

public class Fidelity {

    private int id = -1;
    private String code;
    private int rule;
    private int active = 0;
    private double value;
    private double earned;
    private double used;



    public static Fidelity fromJson(JSONObject jsonObject) {
        Fidelity client = new Fidelity();
        // Deserialize json into object fields
        try {
            client.id = jsonObject.getInt("id");
            client.code = jsonObject.getString("code");
            client.rule= jsonObject.getInt("rule");
            client.active = jsonObject.getInt("active");
            client.value = jsonObject.getDouble("value");
            client.earned= jsonObject.getDouble("earned");
            client.used= jsonObject.getDouble("used");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return client;
    }

    public static ArrayList<Fidelity> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Fidelity>>(){}.getType();
        ArrayList<Fidelity> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getEarned() {
        return earned;
    }

    public void setEarned(double earned) {
        this.earned = earned;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }
}
