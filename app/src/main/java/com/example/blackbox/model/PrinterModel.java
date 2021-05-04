package com.example.blackbox.model;

/**
 * Created by tiziano on 2/27/19.
 */

public class PrinterModel{
    private String printerName;

    public PrinterModel(String name){
        printerName = name;
    }

    public String getPrinterName(){return printerName;}
    public void setPrinterName(String name){printerName = name;}

    @Override
    public String toString(){return printerName;}
}
