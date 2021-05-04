package com.example.blackbox.model;

/**
 * Created by Fabrizio on 14/05/2018.
 */

public class DiscountModel {

    private String description;
    private int value;
    private int mode;

    public DiscountModel(){
        description = "";
        value = -1;
        mode = -1;
    }

    public void setDescription(String d){description = d;}
    public void setValue(int v){value = v;}
    public String getDescription(){return description;}
    public int getValue(){return value;}
    public int getMode(){return mode;}
    public void setMode(int m){mode = m;}

}
