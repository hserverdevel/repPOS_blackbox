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

public class Client {

    private int id;
    private String name;
    private String surname;
    private String email;
    private int fidelity_id = -1;
    private String codeValue =  "";


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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static Client fromJson(JSONObject jsonObject) {
        Client client = new Client();
        // Deserialize json into object fields
        try {
            client.id = jsonObject.getInt("id");
            client.name = jsonObject.getString("name");
            client.surname = jsonObject.getString("surname");
            client.email = jsonObject.getString("email");
            if(jsonObject.has("fidelity_id"))
                client.fidelity_id= jsonObject.getInt("fidelity_id");
            if(jsonObject.has("codeValue"))
                client.codeValue= jsonObject.getString("codeValue");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return client;
    }

    public static ArrayList<Client> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Client>>(){}.getType();
        ArrayList<Client> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }


    public int getFidelity_id() {
        return fidelity_id;
    }

    public void setFidelity_id(int fidelity_id) {
        this.fidelity_id = fidelity_id;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }
}
