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

public class ModifierAssigned {

    private int id;
    private int assignementId;
    private int modifierId;
    private int fixed;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssignementId() {
        return assignementId;
    }

    public void setAssignementId(int assignementId) {
        this.assignementId = assignementId;
    }

    public int getModifierId() {
        return modifierId;
    }

    public void setModifierId(int modifierId) {
        this.modifierId = modifierId;
    }

    public int getFixed() {
        return fixed;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }


    public static ModifierAssigned fromJson(JSONObject jsonObject) {
        ModifierAssigned  mgA = new ModifierAssigned ();
        // Deserialize json into object fields
        try {
            mgA.id = jsonObject.getInt("id");
            mgA.assignementId= jsonObject.getInt("assignementId");
            mgA.modifierId= jsonObject.getInt("modifierId");
            mgA.fixed= jsonObject.getInt("fixed");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return mgA;
    }

    public static ArrayList<ModifierAssigned> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ModifierAssigned>>(){}.getType();
        ArrayList<ModifierAssigned> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
