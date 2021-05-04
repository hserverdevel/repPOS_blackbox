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

public class ModifierGroupAssigned {

    private int id;
    private int prodId;
    private int groupId;
    private int allTheGroup;
    private int fixed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProdId() {
        return prodId;
    }

    public void setProdId(int prodId) {
        this.prodId = prodId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getAllTheGroup() {
        return allTheGroup;
    }

    public void setAllTheGroup(int allTheGroup) {
        this.allTheGroup = allTheGroup;
    }

    public int getFixed() {
        return fixed;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    public static ModifierGroupAssigned fromJson(JSONObject jsonObject) {
        ModifierGroupAssigned mgA = new ModifierGroupAssigned();
        // Deserialize json into object fields
        try {
            mgA.id = jsonObject.getInt("id");
            mgA.prodId= jsonObject.getInt("prodId");
            mgA.groupId= jsonObject.getInt("groupId");
            mgA.allTheGroup= jsonObject.getInt("allTheGroup");
            mgA.fixed= jsonObject.getInt("fixed");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return mgA;
    }

    public static ArrayList<ModifierGroupAssigned> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ModifierGroupAssigned>>(){}.getType();
        ArrayList<ModifierGroupAssigned> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
