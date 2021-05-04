package com.example.blackbox.model;

/**
 * Created by DavideLiberato on 11/07/2017.
 */

public class PaymentButton {
    private String title;
    private int button_type;
    private int id;
    private int parent_id;
    private boolean focused;

    public PaymentButton(){
    }

    public String getTitle(){return title;}
    public int getButton_type(){return button_type;}
    public int getId() {
        return id;
    }
    public int getParent_id() {
        return parent_id;
    }
    public boolean  getFocused(){return focused;}

    public void setTitle(String s){ title = s;}
    public void setId(int id) {
        this.id = id;
    }
    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }
    public void setButton_type(int button_type) {
        this.button_type = button_type;
    }
    public void setFocused(boolean b){this.focused = b;}
}
