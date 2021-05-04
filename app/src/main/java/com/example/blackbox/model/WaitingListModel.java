package com.example.blackbox.model;

import java.util.Date;

/**
 * Created by Fabrizio on 18/03/2019.
 */

public class WaitingListModel {

    private int id;
    private String name;
    private String surname;
    private int adults;
    private int children;
    private int disabled;
    private Date arrivalTime;
    private int status;

    public WaitingListModel(){

    }

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

    public String getSurname(){return surname;}

    public void setSurname(String s){this.surname = s;}

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int arrived) {
        this.status = arrived;
    }

    public int getAdults() {
        return adults;
    }

    public void setAdults(int adults) {
        this.adults = adults;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public int getDisabled() {
        return disabled;
    }

    public void setDisabled(int disabled) {
        this.disabled = disabled;
    }
}
