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
 * Created by tiziano on 3/12/18.
 */

public class User {
    private int id;
    private String name;
    private String surname;
    private String passcode;
    private int userRole;
    private boolean isDelete = false;
    private String email;

    public static User fromJson(JSONObject jsonObject) {
        User user = new User();
        // Deserialize json into object fields
        try {
            user.id = jsonObject.getInt("id");
            user.name = jsonObject.getString("name");
            user.surname = jsonObject.getString("surname");
            user.passcode = jsonObject.getString("passcode");
            user.email = jsonObject.getString("email");
            user.userRole = jsonObject.getInt("userType");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return user;
    }

    public static ArrayList<User> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<User>>(){}.getType();
        ArrayList<User> userList = gson.fromJson(jsonObject.toString(), type);
        return userList;
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

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public int getUserRole() {
        return userRole;
    }

    public void setUserRole(int userRole) {
        this.userRole = userRole;
    }

    public boolean getIsDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
