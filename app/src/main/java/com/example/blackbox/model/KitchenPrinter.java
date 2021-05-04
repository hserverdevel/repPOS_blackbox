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
 * Created by tiziano on 2/25/19.
 */

public class KitchenPrinter {

    private int id=-1;
    private String name ="";
    private String address="";
    private int port=0;
    private int singleOrder;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public int isSingleOrder() {
        return singleOrder;
    }

    public void setSingleOrder(int singleOrder) {
        this.singleOrder = singleOrder;
    }

    public static KitchenPrinter fromJson(JSONObject jsonObject) {
        KitchenPrinter  printer = new KitchenPrinter ();
        // Deserialize json into object fields
        try {
            printer.id = jsonObject.getInt("id");
            printer.name = jsonObject.getString("name");
            printer.address = jsonObject.getString("address");
            printer.port = jsonObject.getInt("port");
            if(jsonObject.getInt("singleOrder")==0)
                printer.singleOrder = 0;
            else printer.singleOrder = 1;
            //printer.singleOrder = jsonObject.getBoolean("singleOrder");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return printer;
    }

    public static ArrayList<KitchenPrinter> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<KitchenPrinter>>(){}.getType();
        ArrayList<KitchenPrinter> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
