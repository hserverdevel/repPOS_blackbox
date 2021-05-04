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
 * Created by tiziano on 2/21/19.
 */

public class TwoString {

    private String firstString;
    private String secondString;

    public String getFirstString() {
        return firstString;
    }

    public void setFirstString(String firstString) {
        this.firstString = firstString;
    }

    public String getSecondString() {
        if(secondString!=null)
        return secondString;
        else return "-1";
    }

    public void setSecondString(String secondString) {
        this.secondString = secondString;
    }

    public static TwoString fromJson(JSONObject jsonObject) {
        TwoString twoString = new TwoString();
        // Deserialize json into object fields
        try {
            twoString.firstString = jsonObject.getString("firstString");
            twoString.secondString = jsonObject.getString("secondString");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return twoString;
    }

    public static ArrayList<TwoString> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<TwoString>>(){}.getType();
        ArrayList<TwoString> userList = gson.fromJson(jsonObject.toString(), type);
        return userList;
    }
}
