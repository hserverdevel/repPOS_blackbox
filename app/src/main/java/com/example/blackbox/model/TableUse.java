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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiziano on 3/23/18.
 */

public class TableUse {
    private int id;
    private int tableId;
    private int tableNumber;
    private String tableName;
    private int shareTable;
    private int mergeTable;
    private int totalSeats;
    private int tableSeat;
    private Date startTime;
    private Date endTime;
    private int billId;
    private int mainTable;
    private int mainTableNumber;
    private int roomId;
    private ArrayList<TableUseExt> extensionList = new ArrayList<TableUseExt>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public ArrayList<TableUseExt> getExtensionList() {
        return extensionList;
    }

    public void setExtensionList(ArrayList<TableUseExt> extensionList) {
        this.extensionList = extensionList;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int isShareTable() {
        return shareTable;
    }

    public void setShareTable(int shareTable) {
        this.shareTable = shareTable;
    }

    public int isMergeTable() {
        return mergeTable;
    }

    public void setMergeTable(int mergeTable) {
        this.mergeTable = mergeTable;
    }

    public int getTableSeat() {
        return tableSeat;
    }

    public void setTableSeat(int tableSeat) {
        this.tableSeat = tableSeat;
    }

    public int getMainTable() {
        return mainTable;
    }

    public void setMainTable(int mainTable) {
        this.mainTable = mainTable;
    }

    public int getMainTableNumber() {
        return mainTableNumber;
    }

    public void setMainTableNumber(int mainTableNumber) {
        this.mainTableNumber = mainTableNumber;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public static TableUse fromJson(JSONObject jsonObject) {
        TableUse  tableUse = new TableUse ();
        // Deserialize json into object fields
        try {
            tableUse.id = jsonObject.getInt("id");
            tableUse.tableId= jsonObject.getInt("tableId");
            tableUse.tableNumber= jsonObject.getInt("tableNumber");
            tableUse.tableName= jsonObject.getString("tableName");
            tableUse.shareTable= jsonObject.getInt("shareTable");
            tableUse.mergeTable= jsonObject.getInt("mergeTable");
            tableUse.totalSeats= jsonObject.getInt("totalSeats");
            tableUse.tableSeat= jsonObject.getInt("tableSeat");
            if(jsonObject.has("startTime")){
                long date= jsonObject.getLong("startTime");
                tableUse.startTime =new Date(date);
            }
            if(jsonObject.has("endTime")){
                long date= jsonObject.getLong("endTime");
                tableUse.endTime =new Date(date);
            }
            tableUse.billId= jsonObject.getInt("billId");
            tableUse.mainTable= jsonObject.getInt("mainTable");
            tableUse.mainTableNumber= jsonObject.getInt("mainTableNumber");
            tableUse.roomId= jsonObject.getInt("roomId");


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return tableUse;
    }

    public static ArrayList<TableUse> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new TableUse.JsonDateDeserializer()).create();
        Type type = new TypeToken<List<TableUse>>(){}.getType();
        ArrayList<TableUse> list = gson.fromJson(jsonObject.toString(), type);
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


