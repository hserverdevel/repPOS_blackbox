package com.example.blackbox.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestParam
{

    public HashMap<String, String> items;


    public RequestParam()
    {
        items = new HashMap<>();
    }


    public void add(String key, Object value)
        { items.put(key, String.valueOf(value)); }


    public boolean hasKey(String key)
        { return items.containsKey(key); }


    public JSONObject toJson()
    {
        JSONObject jsonParam = new JSONObject();

        for (HashMap.Entry<String, String> entry : items.entrySet())
        {
            try
                { jsonParam.put(entry.getKey(), entry.getValue()); }

            catch (JSONException e)
                { Log.e("<RequestParam>", "Got error on RequestParam.toJson\n" + e.getMessage()); }
        }

        return jsonParam;
    }




}


