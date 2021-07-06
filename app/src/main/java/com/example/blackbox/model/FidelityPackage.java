package com.example.blackbox.model;

import org.json.JSONException;
import org.json.JSONObject;

public class FidelityPackage
{
    // each fidelity package is defined by three element

    // 1. the amount of credit that can be obtained by purchasing this package
    public int creditAmount;

    // 2. the price of this package
    public double price;

    // 3. the name of the package
    public String name;

    // 4. the id in the button table, where the details for this package are stored.
    public int buttonId;

    public int getCreditAmount() { return this.creditAmount; }
    public void setCreditAmount(int n) { this.creditAmount = n; }

    public double getPrice() { return this.price; }
    public void setPrice(double p) { this.price = p; }

    public int getButtonId() { return this.buttonId; }
    public void setButtonId(int p) { this.buttonId = p; }

    public String getName() { return this.name; }
    public void setName(String n) { this.name = n; }
}
