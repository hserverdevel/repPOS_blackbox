package com.example.blackbox.fragments;

/**
 * Created by tiziano on 14/06/17.
 */

public interface FragmentCommunicator {

    public void passDataToFragment(String someValue, Integer catId, Float price, Integer quantity);

}
