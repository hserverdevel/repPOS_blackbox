package com.example.blackbox.model;

import android.view.View;

/**
 * Created by tiziano on 10/30/18.
 * this is used to store view for cosumer to perform click on all customers
 */

public class CustomerView {
    private View view;
    private Integer position;
    private Integer click;

    public CustomerView(View v, Integer p , Integer c) {
        this.view = v;
        this.position = p;
        this.click = c;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        this.click = click;
    }
}
