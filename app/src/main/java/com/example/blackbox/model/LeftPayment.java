package com.example.blackbox.model;

/**
 * Created by tiziano on 10/3/18.
 */

public class LeftPayment {

    private int paymentType;
    private float cost;
    private float paid;
    private float totalCost;
    private SubdivisionItem item = new SubdivisionItem();

    public LeftPayment(int paymentType, float cost, float paid, float totalCost, SubdivisionItem item){
        this.paymentType = paymentType;
        this.cost = cost;
        this.paid = paid;
        this.item = item;
        this.totalCost = totalCost;
    }

    public LeftPayment() {}

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public float getPaid() {
        return paid;
    }

    public void setPaid(float paid) {
        this.paid = paid;
    }

    public SubdivisionItem getItem() {
        return item;
    }

    public void setItem(SubdivisionItem item) {
        this.item = item;
    }

    public float getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(float totalCost) {
        this.totalCost = totalCost;
    }
}
