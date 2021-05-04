package com.example.blackbox.model;

/**
 * Created by tiziano on 6/7/18.
 */

public class PrinterInfo {

    //printer id in db
    private int id;
    //printer name given by user at registration
    private String name;
    //printer code ditron=1, hprt =2....
    private int code;
    //fiscal non fiscal
    private int type;
    //ip assignet to  printer
    private String IP;

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
}
