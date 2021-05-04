package com.example.blackbox.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiziano on 2/25/19.
 */

public class DeviceInfo {

    private int id;
    private String storeName;
    private String ragioneSociale;
    private String partitaIva;
    private String address;
    private String cap;
    private String comune;
    private String provincia;
    private String androidId;
    private String email;
    private String tokenId;
    private String ip;
    private String multicastIp;
    private Integer master;
    private Integer onlineCheck;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRagioneSociale() {
        return ragioneSociale;
    }

    public void setRagioneSociale(String ragioneSociale) {
        this.ragioneSociale = ragioneSociale;
    }

    public String getPartitaIva() {
        return partitaIva;
    }

    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getComune() {
        return comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMulticastIp() {
        return multicastIp;
    }

    public void setMulticastIp(String multicastIp) {
        this.multicastIp = multicastIp;
    }

    public Integer getMaster() {
        return master;
    }

    public void setMaster(Integer master) {
        this.master = master;
    }

    public Integer getOnlineCheck() {
        return onlineCheck;
    }

    public void setOnlineCheck(Integer onlineCheck) {
        this.onlineCheck = onlineCheck;
    }

    public static DeviceInfo fromJson(JSONObject jsonObject) {
        DeviceInfo deviceInfo = new DeviceInfo();
        // Deserialize json into object fields
        try {
            deviceInfo.id = jsonObject.getInt("id");
            deviceInfo.storeName = jsonObject.getString("storeName");
            deviceInfo.ragioneSociale = jsonObject.getString("ragioneSociale");
            deviceInfo.partitaIva = jsonObject.getString("partitaIva");
            deviceInfo.address= jsonObject.getString("address");
            deviceInfo.comune = jsonObject.getString("comune");
            deviceInfo.provincia = jsonObject.getString("provincia");
            deviceInfo.cap = jsonObject.getString("cap");
            deviceInfo.email = jsonObject.getString("email");
            deviceInfo.androidId = jsonObject.getString("androidId");
            deviceInfo.tokenId = jsonObject.getString("tokenId");
            deviceInfo.ip = jsonObject.getString("ip");
            deviceInfo.multicastIp = jsonObject.getString("multicastIp");
            deviceInfo.master = jsonObject.getInt("master");
            deviceInfo.onlineCheck = jsonObject.getInt("onlineCheck");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return deviceInfo;
    }


    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
