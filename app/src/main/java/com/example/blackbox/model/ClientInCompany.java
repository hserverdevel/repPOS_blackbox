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

public class ClientInCompany {
    private int id;
    private int clientId;
    private int companyId;
    private int ordersMade;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getOrdersMade() {
        return ordersMade;
    }

    public void setOrdersMade(int ordersMade) {
        this.ordersMade = ordersMade;
    }

    public static ClientInCompany fromJson(JSONObject jsonObject) {
        ClientInCompany client = new ClientInCompany();
        // Deserialize json into object fields
        try {
            client.id = jsonObject.getInt("id");
            client.clientId = jsonObject.getInt("clientId");
            client.companyId= jsonObject.getInt("companyId");
            if(jsonObject.has("ordersMade"))
                client.ordersMade = jsonObject.getInt("ordersMade");
            else client.ordersMade = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return client;
    }

    public static ArrayList<ClientInCompany> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ClientInCompany>>(){}.getType();
        ArrayList<ClientInCompany> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
