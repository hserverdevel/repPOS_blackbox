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

public class FiscalPrinter {

    private int id=-1;
    private String model ="";
    private String address="";
    private int port=0;
    private boolean useApi=false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUseApi() {
        return useApi;
    }

    public void setUseApi(boolean useApi) {
        this.useApi = useApi;
    }


    public static FiscalPrinter fromJson(JSONObject jsonObject) {
        FiscalPrinter  printer = new FiscalPrinter ();
        // Deserialize json into object fields
        try {
            printer.id = jsonObject.getInt("id");
            printer.model = jsonObject.getString("model");
            printer.address = jsonObject.getString("address");
            printer.port = jsonObject.getInt("port");
            int api = jsonObject.getInt("useApi");
            if(api==0)
                printer.useApi = false;
            else
                printer.useApi = true;
            //printer.useApi = jsonObject.getBoolean("useApi");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return printer;
    }

    public static ArrayList<FiscalPrinter> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<FiscalPrinter>>(){}.getType();
        ArrayList<FiscalPrinter> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
