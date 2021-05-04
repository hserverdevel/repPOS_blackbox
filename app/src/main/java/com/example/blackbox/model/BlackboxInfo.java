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

public class BlackboxInfo {

    private int id = -1;
    private String name = "";
    private String address = "";

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
        this.name= name;
    }

    public String getAddress()
    {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }


    public static BlackboxInfo fromJson(JSONObject jsonObject) {
        BlackboxInfo printer = new BlackboxInfo();
        // Deserialize json into object fields
        try {
            printer.id = jsonObject.getInt("id");
            printer.name= jsonObject.getString("name");
            printer.address = jsonObject.getString("address");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return printer;
    }

    public static ArrayList<BlackboxInfo> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<BlackboxInfo>>(){}.getType();
        ArrayList<BlackboxInfo> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
