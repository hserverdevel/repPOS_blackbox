package com.example.blackbox.model;

import android.util.Log;

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

public class Company {

    private int id;
    private String companyName;
    private String address;
    private String vatNumber;
    private String postalCode;
    private String city;
    private String country;
    private String codiceFiscale;
    private String provincia;
    private String codiceDestinatario;
    private String pec;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodiceDestinatario() {
        return codiceDestinatario;
    }

    public void setCodiceDestinatario(String codiceDestinatario) {
        this.codiceDestinatario = codiceDestinatario;
    }

    public String getPec() {
        return pec;
    }

    public void setPec(String pec) {
        this.pec = pec;
    }

    public static Company fromJson(JSONObject jsonObject) {
        Company company = new Company();
        // Deserialize json into object fields
        try {
            company.id = jsonObject.getInt("id");
            company.companyName = jsonObject.getString("companyName");
            company.address = jsonObject.getString("address");
            company.vatNumber= jsonObject.getString("vatNumber");
            company.postalCode= jsonObject.getString("postalCode");
            company.city= jsonObject.getString("city");
            company.country= jsonObject.getString("country");
            company.codiceFiscale= jsonObject.getString("codiceFiscale");
            company.provincia= jsonObject.getString("provincia");
            company.codiceDestinatario= jsonObject.getString("codiceDestinatario");
            company.pec= jsonObject.getString("pec");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return company;
    }

    public static ArrayList<Company> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Company>>(){}.getType();
        ArrayList<Company> list = gson.fromJson(jsonObject.toString(), type);
        return list;
    }
}
