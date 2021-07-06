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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Fabrizio on 28/05/2018.
 */

public class Reservation implements Comparable<Reservation>{

    private int id;
    private String name;
    private int adults;
    private int children;
    private int disabled;
    private Date time;
    private String telephone;



    public Reservation() {}

    public int getReservation_id() {
        return id;
    }
    public void setReservation_id(int reservation_id) {
        this.id = reservation_id;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public int getAdults() {
        return adults;
    }
    public void setAdults(int adults) {
        this.adults = adults;
    }


    public int getChildren() {
        return children;
    }
    public void setChildren(int children) {
        this.children = children;
    }


    public int getDisabled() {
        return disabled;
    }
    public void setDisabled(int disabled) {
        this.disabled = disabled;
    }


    public Date getTime() {
        return time;
    }
    public void setTime(Date reservation_time) { this.time = reservation_time; }
    public void setTime(String strTime)
    {
        try
        {
            DateFormat format = new SimpleDateFormat("yyyy/MM/dd H:m", Locale.ROOT);
            time = format.parse(strTime);
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    public void setTime(long lTime)
        { this.time = new Date(lTime); }




    public String getTelephone(){
        return telephone;
    }
    public void setTelephone(String number){
        telephone = number;
    }
    
    
    
    public static Reservation fromJson(JSONObject jsonObject)
    {
        Reservation res = new Reservation();


        // Deserialize json into object fields
        try
        {
            res.setReservation_id(jsonObject.getInt("id"));
            res.name = jsonObject.getString("name");
            res.adults = jsonObject.getInt("adults");
            res.children = jsonObject.getInt("children");
            res.disabled = jsonObject.getInt("disabled");
            res.setTime(jsonObject.getLong("time"));

            if (jsonObject.has("telephone"))
                res.telephone = jsonObject.getString("telephone");

            else
                res.telephone = "";
        }

        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }

        // Return new object
        return res;
    }


    public static ArrayList<Reservation> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException
                    {
                        return new Date(jsonElement.getAsJsonPrimitive().getAsLong());
                    }
                })
                .create();

        Type type = new TypeToken<List<Reservation>>(){}.getType();

        ArrayList<Reservation> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }



    @Override
    public int compareTo(Reservation r)
    {
        return time.compareTo(r.getTime());
    }





}
