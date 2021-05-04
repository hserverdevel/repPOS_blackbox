package com.example.blackbox.fragments;

import com.example.blackbox.adapter.OModifierAdapter;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiziano on 14/06/17.
 */

public interface ActivityCommunicator{

        void passDataToActivity(ButtonLayout button, String someValue, Integer catId, Float price, Integer quantity);

        void passModifierToActivity(OModifierAdapter.OModifier modifier, Integer quantity);

        void passModifierToRemoveToActivity(OModifierAdapter.OModifier modifier, Integer quantity, Integer position);

        void endModifyModifier(int groupPosition);

        void setItemToDelete(Integer groupPosition, Boolean toLeft);

        void setCustomerToDelete(Integer customerPosition, Boolean toLeft);

        void setGroupClick(Integer groupPosition);

        void setCustomerClick(Integer customerPosition);

        void removeModifierFromCashListInModify(Integer position, OModifierAdapter.OModifier modifier, Integer groupPosition);

        void modifyProduct(Integer groupPosition);

        void addQuantityToCashList(Integer groupPosition, Boolean add);

        void deleteProduct(int groupPosition);

        void deleteAllProducts(int groupPosition);

        void deleteCustomer(int customerPosition);

        void modifyCustomer(int customerPosition, Customer customer);

        void goToMainPage();

        void deleteCurrentCash();

        void endModifyProduct();

        void showModifierPageToModify(Integer groupPosition, List<CashButtonListLayout> listOfValues, Integer modifierId, String currentProduct, Integer categoryId);

        ArrayList<CashButtonListLayout> getLastList();

        void passNoteModifierToActivity(CashButtonListLayout but, int i, boolean modify, List<CashButtonListLayout> cashButtonList);

        void deleteNoteFromList();

        void passClientLongClickToActivity(boolean clientLongClick);

        void selectFavourites();
}
