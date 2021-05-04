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
 * Created by tiziano on 3/20/18.
 */

public class Table {
    private int id;
    private int tableNumber;
    private int peopleNumber;
    private int roomId;
    private String tableName;
    private int mergeTable;
    private int shareTable;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getPeopleNumber() {
        return peopleNumber;
    }

    public void setPeopleNumber(int peopleNumber) {
        this.peopleNumber = peopleNumber;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getMergeTable() {
        return mergeTable;
    }

    public void setMergeTable(int mergeTable) {
        this.mergeTable = mergeTable;
    }

    public int getShareTable() {
        return shareTable;
    }

    public void setShareTable(int shareTable) {
        this.shareTable = shareTable;
    }

    public static Table fromJson(JSONObject jsonObject) {
        Table client = new Table();
        // Deserialize json into object fields
        try {
            client.id = jsonObject.getInt("id");
            client.tableName = jsonObject.getString("tableNumber");
            client.tableNumber= jsonObject.getInt("tableNumber");
            client.peopleNumber= jsonObject.getInt("peopleNumber");
            client.mergeTable= jsonObject.getInt("mergeTable");
            client.shareTable= jsonObject.getInt("shareTable");
            client.roomId= jsonObject.getInt("roomId");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return client;
    }

    public static ArrayList<Table> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Table>>(){}.getType();
        ArrayList<Table> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
