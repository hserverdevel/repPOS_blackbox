package com.example.blackbox.model;


/**
 * Created by Fabrizio on 02/03/2018.
 */

public class VatModel {

    private int referenceId;
    private int vatValue;
    private int perc = 0;

    public VatModel() {

    }


    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public int getVatValue() {
        return vatValue;
    }

    public void setVatValue(int vatValue) {
        this.vatValue = vatValue;
    }

   @Override
    public String toString()
        { return "VAT " + perc + "% " + "(Group: " + vatValue + ")"; }


    public int getPerc() {
        return perc;
    }

    public void setPerc(int perc) {
        this.perc = perc;
    }

}
