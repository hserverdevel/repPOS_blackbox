package com.example.blackbox.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RequestParam
{

    private HashMap<String, Object> items;


    public void add(String s, Object arg)
        { items.put(s, arg); }


    public boolean hasKey(String k)
        { return items.containsKey(k); }


    public JSONObject toJson()
    {
        JSONObject jsonParam = new JSONObject();

        for (HashMap.Entry<String, Object> entry : items.entrySet())
        {
            try
                { jsonParam.put(entry.getKey(), entry.getValue()); }
            catch (JSONException e)
                { Log.e("<RequestParam>", "Got error on RequestParam.toJson\n" + e.getMessage()); }
        }

        return jsonParam;
    }




}


