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
 * Created by tiziano on 3/19/18.
 */

public class Room {
    private int id;
    private String name;
    private Date startTime;
    private Date endTime;
    private Date seasonStart;
    private Date seasonEnd;


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

    public Date getSeasonStart() {
        return seasonStart;
    }

    public void setSeasonStart(Date seasonStart) {
        this.seasonStart = seasonStart;
    }

    public Date getSeasonEnd() {
        return seasonEnd;
    }

    public void setSeasonEnd(Date seasonEnd) {
        this.seasonEnd = seasonEnd;
    }

    public static Room fromJson(JSONObject jsonObject) {
        Room client = new Room();
        // Deserialize json into object fields
        try {
            client.id = jsonObject.getInt("id");
            client.name= jsonObject.getString("name");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return client;
    }

    public static ArrayList<Room> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new Room.JsonDateDeserializer()).create();
        Type type = new TypeToken<List<Room>>(){}.getType();
        ArrayList<Room> list = gson.fromJson(jsonObject.toString(), type);
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
