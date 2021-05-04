package com.example.blackbox.fragments;

import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by DavideLiberato on 11/07/2017.
 */

public interface PaymentActivityCommunicator {

    void activateFunction(int function_id, Object dataBundle, Float any_value);

    void printBill(float paid, float cost, float credit, int paymentType);

    void setProducts(ArrayList<CashButtonLayout> products);

    void setModifiers(Map<CashButtonLayout, ArrayList<CashButtonListLayout>> modifiers);

    void printFiscalPartial(float paid, float cost, String description, int billId, int paymentType);

}
