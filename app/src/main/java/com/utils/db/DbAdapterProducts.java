package com.utils.db;

import android.database.Cursor;
import android.util.Log;

import com.example.blackbox.adapter.ModifierAdapter;
import com.example.blackbox.adapter.ModifiersGroupAdapter;
import com.example.blackbox.adapter.OModifierAdapter;
import com.example.blackbox.adapter.OModifierGroupAdapter;
import com.example.blackbox.model.ButtonLayout;
import com.example.blackbox.model.ModifierAssigned;
import com.example.blackbox.model.ModifierGroupAssigned;

import java.util.ArrayList;

public class DbAdapterProducts extends DbAdapterUsers
{


    // =============================================== //
    // [ BUTTONS]
    // =============================================== //


    /**
     * fetch buttons and returns an ArrayList of them
     * */
    public ArrayList<ButtonLayout> fetchButtonsByQuery(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }

            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<ButtonLayout> array = new ArrayList<>();

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    ButtonLayout c = new ButtonLayout(context);
                    c.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    c.setSubTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_SUBTITLE)));
                    c.setImg(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_IMG)));
                    c.setColor(mCursor.getInt(mCursor.getColumnIndex("color")));
                    c.setPos(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    c.setPrice(mCursor.getFloat(mCursor.getColumnIndex(DatabaseAdapter.KEY_PRICE)));
                    c.setProductCode(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_PRODUCT_CODE)));
                    c.setBarcode(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_BARCODE)));
                    c.setCatID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_CAT_ID)));
                    c.setCat((mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_CAT_BOOL))));
                    c.setVat((mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_VAT_TABLE))));
                    c.setPrinterId((mCursor.getInt(mCursor.getColumnIndex("printer"))));
                    c.setFidelity_discount((mCursor.getInt(mCursor.getColumnIndex("fidelity_discount"))));
                    c.setFidelity_credit((mCursor.getInt(mCursor.getColumnIndex("fidelity_discount"))));
                    c.setCredit_value((mCursor.getDouble(mCursor.getColumnIndex("credit_value"))));

                    array.add(c);
                }
                mCursor.close();
            }
            database.close();
            return array;
        }

        catch (Exception e)
        {
            Log.e(TAG, "[fetchButtonsByQuery] Fail: " + e.getMessage());
            return null;
        }
    }


    /**
     * Fetch a single button and returns it
     * */
    public ButtonLayout fetchButtonByQuery(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            ButtonLayout c = new ButtonLayout(context);
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {

                    c.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    c.setSubTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_SUBTITLE)));
                    c.setImg(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_IMG)));
                    c.setColor(mCursor.getInt(mCursor.getColumnIndex("color")));
                    c.setPos(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    c.setPrice(mCursor.getFloat(mCursor.getColumnIndex(DatabaseAdapter.KEY_PRICE)));
                    c.setProductCode(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_PRODUCT_CODE)));
                    c.setBarcode(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_BARCODE)));
                    c.setCatID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_CAT_ID)));
                    c.setCat((mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_CAT_BOOL))));
                    c.setVat((mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_VAT_TABLE))));
                    c.setPrinterId((mCursor.getInt(mCursor.getColumnIndex("printer"))));
                    c.setFidelity_discount((mCursor.getInt(mCursor.getColumnIndex("fidelity_discount"))));
                    c.setFidelity_credit((mCursor.getInt(mCursor.getColumnIndex("fidelity_discount"))));
                    c.setCredit_value((mCursor.getDouble(mCursor.getColumnIndex("credit_value"))));
                }
                mCursor.close();

            }
            // database.close();
            return c;
        }
        catch (Exception e)
        {
            Log.e(TAG, "[fetchButtonByQuery] Fail: " + e.getMessage());
            return null;
        }
    }


    public void deleteButton(ButtonLayout b)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            recursiveButtonDelete(b.getID(), b.getCat());
        }
        catch (Exception e)
        {
            Log.e("DELETE ERROR", e.getMessage());
        }
    }


    private void recursiveButtonDelete(int b_id, int isCat)
    {
        try
        {
            if (isCat == 0) { database.execSQL("DELETE FROM button WHERE id = " + b_id); }
            else
            {
                Cursor c = database.rawQuery("SELECT * FROM button WHERE " + KEY_CAT_ID + "=" + b_id, null);
                while (c.moveToNext())
                { recursiveButtonDelete(c.getInt(c.getColumnIndex(KEY_ID)), c.getInt(c.getColumnIndex(KEY_CAT_BOOL))); }
                c.close();
                database.execSQL("DELETE FROM button WHERE id =" + b_id);
            }

        }
        catch (Exception e)
        {
            Log.e("Delete Error", e.getMessage());
        }
    }


    /**
     * prende ogni button, ogni modifier, ogni modifiers_group, ogni modifiers_group_assigned associato
     * ad ogni button
     */
    public void saveButtonsAndModifiersInDatabase()
    {
        ArrayList<ButtonLayout> buttonsSaved = fetchButtonsByQuery("SELECT * from button");
        ArrayList<ModifierAdapter.Modifier> modifiersSaved = fetchModifiersByQuery("SELECT * from modifier");
        ArrayList<ModifiersGroupAdapter.ModifiersGroup> modifiersGroupSaved = fetchModifiersGroupByQuery("SELECT * from modifiers_group");
        //mi faccio un array di mod_group_ass_id (quindi di interi)
        ArrayList<Integer> modifiersGroupAssignedSaved = new ArrayList<>();
        for (ButtonLayout b : buttonsSaved)
        {
            Integer bId = b.getID();
            for (ModifierAdapter.Modifier m : modifiersSaved)
            {
                Integer mId = m.getID();
                modifiersGroupAssignedSaved.add(getAssignmentID(mId, bId));
            }
        }
        //anche qui mi faccio un array di mod_ass_id (quindi di interi)
        ArrayList<Integer> modifierAssignedSaved = new ArrayList<>();
        for (ModifierAdapter.Modifier m : modifiersSaved)
        {
            Integer modId = m.getID();
            ArrayList<Integer> assMod = fetchAssignedModifiersByQuery(modId);
            modifierAssignedSaved.addAll(assMod);
        }

        //flushTables();
        //insert in button_table
        for (ButtonLayout b : buttonsSaved)
        {
            execOnDb("INSERT INTO button (id, title, subtitle, img_name, color, position, price, vat, catID, isCat) " +
                    "VALUES(" + b.getID() + "," +
                    "\"" + b.getTitle().replaceAll("'", "\'") + "\"," +
                    "\"" + b.getSubTitle().replaceAll("'", "\'") + "\"," +
                    "\"" + b.getImg() + "\"," + b.getColor() + "," + b.getPos() + "," +
                    b.getPrice() + "," + b.getVat() + "," + b.getCatID() + ", 1);");
        }
        //insert in modifier_table
        for (ModifierAdapter.Modifier m : modifiersSaved)
        {
            execOnDb("INSERT INTO modifier (id, title, position, price, vat, groupID) " +
                    "VALUES(" + m.getID() + ", \"" + m.getTitle().replaceAll("'", "\'") + "\"," +
                    m.getPosition() + ", " + m.getPrice() + ", " + m.getVat() + ", " + m.getGroupID() + ");");
        }
        //insert in modifiers_group
        for (ModifiersGroupAdapter.ModifiersGroup mg : modifiersGroupSaved)
        {
            execOnDb("INSERT INTO modifiers_group (id, title, position) " +
                    "VALUES(" + mg.getID() + ", \"" + mg.getTitle().replaceAll("'", "\'") + "\"," +
                    mg.getPosition() + ");");
        }
        //insert in modifiers_group_assigned
        for (Integer i : modifiersGroupAssignedSaved)
        {
            execOnDb("INSERT INTO modifiers_group_assigned(id, prod_id, group_id, all_the_group, fixed) " +
                    "VALUES(" + i + ", (SELECT modifiers_group_assigned.prod_id from modifiers_group_assigned WHERE "
                    + "modifiers_group_assigned.id=" + i + "),(" +
                    "SELECT modifiers_group_assigned.group_id from modifiers_group_assigned WHERE "
                    + "modifiers_group_assigned.id=" + i + "), 1, 1);");
        }
        for (Integer i : modifierAssignedSaved)
        {
            execOnDb("INSERT INTO modifiers_assigned(id, assignment_id, modifier_id, fixed) " +
                    "VALUES(" + i + ", (SELECT modifiers_assigned.assignment_id from modifiers_assigned" +
                    "WHERE modifiers_assigned.id=" + i + "), (SELECT modifiers_assigned.modifier_id " +
                    "from modifiers_assigned WHERE modifiers_assigned.id=" + i + "), 1);");
        }
    }


    public void insertButtonsSync(ArrayList<ButtonLayout> buttons)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (ButtonLayout b : buttons)
            {
                execOnDb("INSERT INTO button (id, title, subtitle, img_name, color, position, price, vat, catID, isCat, barcode, productCode, printer, fidelity_discount, fidelity_credit, credit_value) " +
                        "VALUES(" + b.getID() + "," +
                        "\"" + b.getTitle().replaceAll("'", "\'") + "\"," +
                        "\"" + b.getSubTitle().replaceAll("'", "\'") + "\"," +
                        "\"" + b.getImg() + "\"," + b.getColor() + "," + b.getPos() + "," +
                        b.getPrice() + "," + b.getVat() + "," + b.getCatID() + ", " + b.getCat() + ", '" + b
                        .getBarcode() + "', '" + b.getProductCode() + "', " + b.getPrinterId() + ", " + b
                        .getFidelity_discount() + "," + b.getFidelity_credit() + "," + b.getCredit_value() + ");");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert Buttons Error", e.getMessage());
        }
    }


    public void insertButtonFromServer(ButtonLayout b)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }

            execOnDb("INSERT INTO button (id, title, subtitle, img_name, color, position, price, vat, catID, isCat, barcode, productCode, printer, fidelity_discount, fidelity_credit, credit_value) " +
                    "VALUES(" + b.getID() + "," +
                    "\"" + b.getTitle().replaceAll("'", "\'") + "\"," +
                    "\"" + b.getSubTitle().replaceAll("'", "\'") + "\"," +
                    "\"" + b.getImg() + "\"," + b.getColor() + "," + b.getPos() + "," +
                    b.getPrice() + "," + b.getVat() + "," + b.getCatID() + ", " + b.getCat() + ", '" + b
                    .getBarcode() + "', '" + b.getProductCode() + "', " + b.getPrinterId() + ", " + b
                    .getFidelity_discount() + "," + b.getFidelity_credit() + "," + b.getCredit_value() + ");");

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert Buttons Error", e.getMessage());
        }
    }


    public void deleteButton(int id, int isCat)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen()) { database = dbHelper.getWritableDatabase(); }
            recursiveButtonDeleteFromServer(id, isCat);
        }
        catch (Exception e)
        {
            Log.e("DELETE ERROR", e.getMessage());
        }
    }


    public void recursiveButtonDeleteFromServer(int b_id, int isCat)
    {
        try
        {
            if (isCat == 0) { database.execSQL("DELETE FROM button WHERE id = " + b_id); }
            else
            {
                Cursor c = database.rawQuery("SELECT * FROM button WHERE " + KEY_CAT_ID + "=" + b_id, null);
                while (c.moveToNext())
                { recursiveButtonDeleteFromServer(c.getInt(c.getColumnIndex(KEY_ID)), c.getInt(c.getColumnIndex(KEY_CAT_BOOL))); }
                c.close();
                database.execSQL("DELETE FROM button WHERE id =" + b_id);
            }
        }
        catch (Exception e)
        {
            Log.e("Delete Error", e.getMessage());
        }
    }





    // =============================================== //
    // [ MODIFIERS]
    // =============================================== //

    public ArrayList<ModifiersGroupAdapter.ModifiersGroup> fetchModifiersGroupByQuery(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<ModifiersGroupAdapter.ModifiersGroup> array = new ArrayList<>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    ModifiersGroupAdapter.ModifiersGroup c = new ModifiersGroupAdapter.ModifiersGroup();
                    c.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    c.setPosition(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    array.add(c);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            ArrayList<ModifiersGroupAdapter.ModifiersGroup> array = new ArrayList<>();
            Log.e("fetchFailure", e.getMessage());
            return array;
        }
    }


    public OModifierGroupAdapter.OModifiersGroup fetchSingleModifiersGroupByQuery(String query)
    {
        OModifierGroupAdapter.OModifiersGroup c = new OModifierGroupAdapter.OModifiersGroup();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    c.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    c.setPosition(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    int notes = mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    String prova = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    if (prova.equals("true"))
                    { c.setNotes(true); }
                    else
                    { c.setNotes(false); }
                }
                mCursor.close();


            }
            // database.close();

            return c;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            c.setPosition(1000000);
            return c;
        }
    }


    public int fetchModifiersGroupByQueryOne(int catId)
    {
        int returnId = -1;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            String myQuery = "Select group_id from modifiers_group_assigned where prod_id=" + catId;
            Cursor mCursor = database.rawQuery(myQuery, null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    returnId = mCursor.getInt(0);

                }
                mCursor.close();


            }
            database.close();

            return returnId;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return returnId;
        }
    }


    public ArrayList<OModifierAdapter.OModifier> fetchOModifiersByQuery(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<OModifierAdapter.OModifier> array = new ArrayList<>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    OModifierAdapter.OModifier m = new OModifierAdapter.OModifier();
                    m.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    m.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    m.setPosition(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    m.setPrice(mCursor.getFloat(mCursor.getColumnIndex(DatabaseAdapter.KEY_PRICE)));
                    m.setGroup(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_GROUP_ID)));
                    array.add(m);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return null;
        }
    }


    public OModifierAdapter.OModifier fetchSingleOModifiersByQuery(String query)
    {
        OModifierAdapter.OModifier m = new OModifierAdapter.OModifier();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    m.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    m.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    m.setPosition(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    m.setPrice(mCursor.getFloat(mCursor.getColumnIndex(DatabaseAdapter.KEY_PRICE)));
                    m.setGroup(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_GROUP_ID)));
                }
                mCursor.close();

            }
            // database.close();

            return m;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            m.setPosition(10000000);
            return m;
        }
    }


    public ArrayList<ModifierAdapter.Modifier> fetchModifiersByQuery(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<ModifierAdapter.Modifier> array = new ArrayList<>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    ModifierAdapter.Modifier m = new ModifierAdapter.Modifier();
                    m.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    m.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    m.setPosition(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    m.setPrice(mCursor.getFloat(mCursor.getColumnIndex(DatabaseAdapter.KEY_PRICE)));
                    m.setGroup(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_GROUP_ID)));
                    m.setVat(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_VAT_TABLE)));
                    array.add(m);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return null;
        }
    }


    public boolean fetchNotesFromModifiersGroup(String query)
    {
        boolean n = false;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    int notes = mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    String prova = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    n = prova.equals("true") || notes == 1;
                }
                mCursor.close();

            }
            database.close();
            return n;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return false;
        }
    }


    /**
     * Fetch the id of the assignement button <-> modifier
     * @param groupID the group of the modifier
     * @param prodID  the id of the button
     *
     * @return the id present in the <modifier_group_assigned>
     */
    public int getAssignmentID(int groupID, int prodID)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT * FROM modifiers_group_assigned " +
                    "WHERE prod_id =" + prodID + " AND group_id =" + groupID + "", null);
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    if (mCursor.getInt(mCursor.getColumnIndex("all_the_group")) > 0)
                    { id = -2; }
                    else
                    { id = mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)); }
                }
                mCursor.close();

            }
            database.close();
            return id;
        }

        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return -1;
        }
    }


    /**
     * Fetch the id of the assignement button <-> modifier
     * @param groupID the group of the modifier
     * @param prodID  the id of the button
     *
     * @return the id present in the <modifier_group_assigned>
     *
     * @note the difference with <getAssigmentID> is that this function return the first id that it gets
     */
    public int getSingleAssignmentID(int groupID, int prodID)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            int id = -1;
            Cursor mCursor = database.rawQuery("SELECT * FROM modifiers_group_assigned " +
                    "WHERE prod_id =" + prodID + " AND group_id =" + groupID + ";", null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    if (mCursor.getInt(mCursor.getColumnIndex("all_the_group")) > 0)
                    { id = -2; }
                    else
                    {
                        id = mCursor.getInt(0);//to get id, 0 is the column index
                    }
                }
                mCursor.close();
            }
            database.close();
            return id;
        }

        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return -1;
        }
    }


    /**
     * Fetch the all the ids of the assignement button <-> modifier
     * @param prodId  the id of the button
     *
     * @return an ArrayList of id linked to a button, present in the <modifier_group_assigned>
     *
     */
    public ArrayList<Integer> fetchAssignedGroupModifiersByQuery(int prodId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("Select group_id from modifiers_group_assigned " +
                    " WHERE prod_id = " + prodId + " AND fixed=1", null);
            ArrayList<Integer> array = new ArrayList<Integer>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    //name = cursor.getString(column_index);//to get other values
                    array.add(mCursor.getInt(mCursor.getColumnIndex("group_id")));
                }
                mCursor.close();
            }
            database.close();

            return array;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return null;
        }
    }


    /**
     * Fetch the all the modifiers of the assignement button <-> modifier
     * @param prodId  the id of the button
     *
     * @return an ArrayList of modifiers linked to a button, present in the <modifier_group_assigned>
     *
     */
    public ArrayList<Integer> fetchAssignedModifiersByQuery(int prodId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("Select modifiers_assigned.modifier_id from modifiers_assigned " +
                    " LEFT JOIN modifiers_group_assigned ON modifiers_group_assigned.id=modifiers_assigned.assignment_id " +
                    " WHERE modifiers_group_assigned.prod_id = " + prodId + " AND modifiers_assigned.fixed=1", null);
            ArrayList<Integer> array = new ArrayList<Integer>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    //name = cursor.getString(column_index);//to get other values
                    array.add(mCursor.getInt(mCursor.getColumnIndex("modifier_id")));
                }
                mCursor.close();
            }
            database.close();

            return array;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());
            return null;
        }
    }


    public void deleteModifierFromTableByID(String table, int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            switch (table)
            {
                case "modifier":
                    database.execSQL("DELETE FROM " + table + " WHERE id = " + id);
                    break;
                case "modifiers_group":
                    Cursor c = database.rawQuery("SELECT * FROM modifier WHERE groupID =" + id, null);
                    while (c.moveToNext())
                    {
                        int modID = c.getInt(c.getColumnIndex("id"));
                        deleteModifierFromTableByID("modifier", modID);
                    }
                    c.close();
                    database.execSQL("DELETE FROM " + table + " WHERE id =" + id);
                    break;
                default:
                    throw new Exception("No Such a Table in DB");
            }
        }
        catch (Exception e)
        {
            Log.e("DELETE ERROR", e.getMessage());
        }
    }


    public boolean checkIfModifierExists(int id)
    {
        try
        {
            Boolean rowExist = false;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM modifier WHERE id=" + id, null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    rowExist = true;
                }
            }
            //showData(MODIFIER_TABLE);
            mCursor.close();
            database.close();
            return rowExist;
        }
        catch (Exception e)
        {
            Log.e("fetchFailure", e.getMessage());

            return false;
        }
    }


    public ArrayList<OModifierGroupAdapter.OModifiersGroup> fetchOperativeModifiersGroupByQuery(String query)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    OModifierGroupAdapter.OModifiersGroup c = new OModifierGroupAdapter.OModifiersGroup();
                    c.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    c.setPosition(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    String prova = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    int notes = mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    if (prova.equals("1"))
                    { c.setNotes(true); }
                    else
                    { c.setNotes(false); }
                    array.add(c);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            Log.e("fetchFailure", e.getMessage());

            ArrayList<OModifierGroupAdapter.OModifiersGroup> newarray = new ArrayList<>();
            return newarray;
        }
    }


    public ArrayList<OModifierGroupAdapter.OModifiersGroup> checkToOpenModifiersGroup(int categoryId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            String query = "SELECT modifiers_group.id, modifiers_group.title, modifiers_group.position, modifiers_group.notes , modifiers_group_assigned.fixed " +
                    "FROM modifiers_group " +
                    "LEFT JOIN modifiers_group_assigned " +
                    "ON modifiers_group.id=modifiers_group_assigned.group_id " +
                    "WHERE modifiers_group_assigned.prod_id=" + categoryId + " ORDER BY modifiers_group.position";
            Cursor mCursor = database.rawQuery(query, null);
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            //Log.e("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    OModifierGroupAdapter.OModifiersGroup c = new OModifierGroupAdapter.OModifiersGroup();
                    c.setID(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_ID)));
                    c.setTitle(mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_TITLE)));
                    c.setPosition(mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_POSITION)));
                    String prova = mCursor.getString(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    int notes = mCursor.getInt(mCursor.getColumnIndex(DatabaseAdapter.KEY_NOTES));
                    if (prova.equals("true"))
                    { c.setNotes(true); }
                    else
                    { c.setNotes(false); }
                    c.setFixed(mCursor.getInt(mCursor.getColumnIndex("fixed")));
                    array.add(c);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            ArrayList<OModifierGroupAdapter.OModifiersGroup> array = new ArrayList<>();
            Log.e("fetchFailure", e.getMessage());

            ArrayList<OModifierGroupAdapter.OModifiersGroup> newarray = new ArrayList<>();
            return newarray;
        }
    }


    public void insertModifierGroupsSync(ArrayList<ModifiersGroupAdapter.ModifiersGroup> modifierGroups)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (ModifiersGroupAdapter.ModifiersGroup mg : modifierGroups)
            {
                execOnDb("INSERT INTO modifiers_group (id, title, position, notes) " +
                        "VALUES(" + mg.getID() + ", \"" + mg.getTitle()
                                                            .replaceAll("'", "\'") + "\"," +
                        mg.getPosition() + ", " + mg.getNotes() + ");");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }


    public void insertModifiersSync(ArrayList<ModifierAdapter.Modifier> modifier)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (ModifierAdapter.Modifier m : modifier)
            {
                execOnDb("INSERT INTO modifier (id, title, position, price, vat, groupID) " +
                        "VALUES(" + m.getID() + ", \"" + m.getTitle()
                                                          .replaceAll("'", "\'") + "\"," +
                        m.getPosition() + ", " + m.getPrice() + ", " + m.getVat() + ", " + m.getGroupID() + ");");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert modifiers Error", e.getMessage());
        }
    }


    public void insertModifierGroupAssignedSync(ArrayList<ModifierGroupAssigned> mgAs)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (ModifierGroupAssigned m : mgAs)
            {
                execOnDb("INSERT INTO modifiers_group_assigned(id, prod_id, group_id, all_the_group, fixed) " +
                        "VALUES(" + m.getId() + ", " + m.getProdId() + ", " + m.getGroupId() + ", " + m
                        .getAllTheGroup() + ", " + m.getFixed() + ")");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert mgA Error", e.getMessage());
        }
    }


    public void insertModifierAssignedSync(ArrayList<ModifierAssigned> mgAs)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (ModifierAssigned m : mgAs)
            {
                execOnDb("INSERT INTO modifiers_assigned(id, assignment_id, modifier_id, fixed) " +
                        "VALUES(" + m.getId() + ", " + m.getAssignementId() + ", " + m.getModifierId() + ", " + m
                        .getFixed() + ")");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert mA Error", e.getMessage());
        }
    }


    public void insertModifierGroupsFromServer(ModifiersGroupAdapter.ModifiersGroup mg)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("INSERT INTO modifiers_group (id, title, position, notes) " +
                    "VALUES(" + mg.getID() + ", \"" + mg.getTitle().replaceAll("'", "\'") + "\"," +
                    mg.getPosition() + ", " + mg.getNotes() + ");");

            database.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }


    public void updateModifierGroupsFromServer(ModifiersGroupAdapter.ModifiersGroup mg)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("UPDATE modifiers_group SET title ='" + mg.getTitle() + "', notes= " + mg.getNotes() + " WHERE id=" + mg
                    .getID());

            database.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }


    public void insertModifiersGroupGroupsFromServer(ModifierGroupAssigned mga)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("INSERT INTO modifiers_group_assigned(id, prod_id, group_id, all_the_group, fixed) " +
                    "VALUES(" + mga.getId() + "," + mga.getProdId() + "," + mga.getGroupId() + "," + mga
                    .getAllTheGroup() + "," + mga.getFixed() + ");");

            database.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }


    public void insertModifierFromServer(ModifierAdapter.Modifier m)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("INSERT INTO modifier (id, title, position, price, vat, groupID) " +
                    "VALUES(" + m.getID() + ", \"" + m.getTitle().replaceAll("'", "\'") + "\"," +
                    m.getPosition() + ", " + m.getPrice() + ", " + m.getVat() + ", " + m.getGroupID() + ");");

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert modifiers Error", e.getMessage());
        }
    }


    public void updateModifierFromServer(ModifierAdapter.Modifier m)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            execOnDb("UPDATE modifier SET title= '" + m.getTitle() + "', price= " + m.getPrice() + ", vat=" + m
                    .getVat() + ", groupID=" + m.getGroupID() + " WHERE id=" + m.getID());

            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert modifiers Error", e.getMessage());
        }
    }


}
