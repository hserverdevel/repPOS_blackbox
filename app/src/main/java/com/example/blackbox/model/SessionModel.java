package com.example.blackbox.model;

/**
 * Created by tiziano on 9/27/17.
 */

/**
 * class that modelize sessions table in database
 * quite simpèle to understand
 * times are in string, so they must be converted when used as Time
 */
public class SessionModel {
    private int id;
    private String sessionName;
    private String startTime;
    private String endTime;

    public int getId(){return id;}
    public String getSessionName() {return sessionName;}
    public String getStartTime(){return startTime;}
    public String getEndTime(){return endTime;}

    public void setId(int idToSet){id= idToSet;}
    public void setSessionName(String s) { sessionName = s;}
    public void setStartTime(String s) {startTime = s;}
    public void setEndTime(String s) { endTime = s;}
}
