package com.example.blackbox.model;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by tiziano on 9/12/17.
 */

public class TemporaryOrder {

    private static ArrayList<CashButtonLayout> products;
    private static Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers;
    private static ClientInfo client;

    public TemporaryOrder() {

    }

    public static void setProduct(ArrayList<CashButtonLayout> cbl){
        products = cbl;
    }

    public static void setModifiers( Map<CashButtonLayout,ArrayList<CashButtonListLayout>>  mods) {
        modifiers = mods;
    }

    public static ArrayList<CashButtonLayout> getProducts(){ return products;}

    public static Map<CashButtonLayout,ArrayList<CashButtonListLayout>> getModifiers(){return modifiers;}

    public static ClientInfo getClient() {
        return client;
    }

    public static void setClient(ClientInfo client) {
        TemporaryOrder.client = client;
    }
}
