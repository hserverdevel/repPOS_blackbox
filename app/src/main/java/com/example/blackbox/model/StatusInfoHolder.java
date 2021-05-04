package com.example.blackbox.model;


public class StatusInfoHolder {
    private ButtonLayout current_product;
    private int current_product_id;
    private int dept_level;
    private int current_category_id;
    private int previous_category_id;
    private String previous_category_title;


    public void setCurrent_category_id(int current_category_id) {
        this.current_category_id = current_category_id;
    }

    public void setCurrent_product_id(int current_product_id) {
        this.current_product_id = current_product_id;
    }

    public void setCurrent_product(ButtonLayout current_product) {
        this.current_product = current_product;
    }

    public void setDept_level(int dept_level) {
        this.dept_level = dept_level;
    }

    public void setPrevious_category_title(String previous_category_title) {
        this.previous_category_title = previous_category_title;
    }

    public void setPrevious_category_id(int previous_category_id) {
        this.previous_category_id = previous_category_id;
    }

    public ButtonLayout getCurrent_product() {
        return current_product;
    }

    public int getDept_level() {
        return dept_level;
    }

    public int getCurrent_category_id() {
        return current_category_id;
    }

    public int getPrevious_category_id() {
        return previous_category_id;
    }

    public int getCurrent_product_id() {
        return current_product_id;
    }

    public String getPrevious_category_title() {
        return previous_category_title;
    }

}
