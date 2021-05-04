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
 * Created by DavideLiberato on 18/07/2017.
 */

public class ClientInfo
{
    private String name;
    private String surname;
    private String email;
    private String companyName;
    private String companyAddress;
    private String companyVatNumber;
    private String companyPostalCode;
    private String companyCity;
    private String companyCountry;
    private String codiceFiscale;
    private String provincia;
    private String codiceDestinatario;
    private String pec;
    private int clientId;
    private int company_id;
    private int client_in_company_id;
    private boolean hasCompany = false;
    private boolean isActive = false;
    private int fidelity_id = -1;

    public ClientInfo(){}

    public static ClientInfo fromJson(JSONObject jsonObject)
    {
        ClientInfo client = new ClientInfo();
        // Deserialize json into object fields
        try {
            client.clientId = jsonObject.getInt("clientId");
            client.company_id = jsonObject.getInt("company_id");
            if(jsonObject.has("client_in_company_id"))
                client.client_in_company_id = jsonObject.getInt("client_in_company_id");
            client.name = jsonObject.getString("name");
            client.surname = jsonObject.getString("surname");
            client.email = jsonObject.getString("email");
            if(jsonObject.has("companyName"))
                client.companyName = jsonObject.getString("companyName");
            if(jsonObject.has("companyAddress"))
                client.companyAddress = jsonObject.getString("companyAddress");
            if(jsonObject.has("companyVatNumber"))
                client.companyVatNumber = jsonObject.getString("companyVatNumber");
            if(jsonObject.has("companyPostalCode"))
                client.companyPostalCode = jsonObject.getString("companyPostalCode");
            if(jsonObject.has("companyCity"))
                client.companyCity = jsonObject.getString("companyCity");
            if(jsonObject.has("companyCountry"))
                client.companyCountry = jsonObject.getString("companyCountry");
            if(jsonObject.has("codiceFiscale"))
                client.codiceFiscale = jsonObject.getString("codiceFiscale");
            if(jsonObject.has("provincia"))
                client.provincia = jsonObject.getString("provincia");
            if(jsonObject.has("codiceDestinatario"))
                client.codiceDestinatario = jsonObject.getString("codiceDestinatario");
            if(jsonObject.has("pec"))
                client.pec = jsonObject.getString("pec");
            if(jsonObject.has("fidelity_id"))
                client.fidelity_id= jsonObject.getInt("fidelity_id");
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return client;
    }

    public static ArrayList<ClientInfo> fromJsonArray(JSONArray jsonObject) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ClientInfo>>(){}.getType();
        ArrayList<ClientInfo> clientsList = gson.fromJson(jsonObject.toString(), type);
        return clientsList;
    }



    public int getCompany_id() {
        return company_id<0?-1:company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public int getClient_id() {
        return clientId;
    }

    public void setClient_id(int client_id) {
        this.clientId = client_id;
    }

    public String getCompany_country() {
        return companyCountry==null?"":companyCountry;
    }

    public void setCompany_country(String company_country) {
        this.companyCountry = company_country;
    }

    public String getCompany_city() {
        return companyCity==null?"":companyCity;
    }

    public void setCompany_city(String company_city) {
        this.companyCity = company_city;
    }

    public String getCompany_postal_code() {
        return companyPostalCode==null?"":companyPostalCode;
    }

    public void setCompany_postal_code(String company_postal_code) {
        this.companyPostalCode = company_postal_code;
    }

    public String getCompany_vat_number() {
        return companyVatNumber==null?"":companyVatNumber;
    }

    public void setCompany_vat_number(String company_vat_number) {
        this.companyVatNumber = company_vat_number;
    }

    public String getCompany_address() {
        return companyAddress==null?"":companyAddress;
    }

    public void setCompany_address(String company_address) {
        this.companyAddress = company_address;
    }

    public String getCompany_name() {
        return companyName==null?"":companyName;
    }

    public void setCompany_name(String company_name) {
        this.companyName = company_name;
    }

    public String getEmail() {
        return email==null?"":email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSurname() {
        return surname==null?"":surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name==null?"":name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClient_in_company_id() {
        return client_in_company_id;
    }

    public void setClient_in_company_id(int client_in_company_id) {
        this.client_in_company_id = client_in_company_id;
    }

    public boolean isHasCompany() {
        return hasCompany;
    }

    public void setHasCompany(boolean hasCompany) {
        this.hasCompany = hasCompany;
    }

    public String getCodice_fiscale() {
        return codiceFiscale;
    }

    public void setCodice_fiscale(String codice_fiscale) {
        this.codiceFiscale = codice_fiscale;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodice_destinatario() {
        return codiceDestinatario==null?"":codiceDestinatario;
    }

    public void setCodice_destinatario(String codice_destinatario) {
        if(codice_destinatario==null) codice_destinatario = "";
        this.codiceDestinatario = codice_destinatario;
    }

    public String getPec() {
            return pec==null?"":pec;
    }

    public void setPec(String pec) {
        if(pec==null) pec = "";
        this.pec = pec;
    }

    public int getFidelity_id() {
        return fidelity_id;
    }

    public void setFidelity_id(int fidelity_id) {
        this.fidelity_id = fidelity_id;
    }

    public String toString()
    {
        return "Client - " + "name: " + name + ", email: " + email + ", clientId: " + clientId + ", fidelity_id: " + fidelity_id;
    }

}
