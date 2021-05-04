package com.example.blackbox.model;

/**
 * Created by Fabrizio on 28/05/2018.
 */

public class Reservation {

    private int id;
    private String name;
    private String surname;
    private int adults;
    private int children;
    private int disabled;
    private String reservation_date;
    private String reservation_time;
    private String start_time = "";
    private int table_use_id;
    private int status = 0;
    private long telephone;

    public Reservation(){
    }

    public int getReservation_id() {
        return id;
    }

    public void setReservation_id(int reservation_id) {
        this.id = reservation_id;
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

    public String getReservation_date(){return reservation_date;}

    public void setReservation_date(String reservation_date){
        this.reservation_date = reservation_date;
    }

    public String getReservation_time() {
        return reservation_time;
    }

    public void setReservation_time(String reservation_time) {
        this.reservation_time = reservation_time;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public int getTable_use_id() {
        return table_use_id;
    }

    public void setTable_use_id(int table_use_id) {
        this.table_use_id = table_use_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTelephone(){
        return telephone;
    }

    public void setTelephone(long number){
        telephone = number;
    }
}
