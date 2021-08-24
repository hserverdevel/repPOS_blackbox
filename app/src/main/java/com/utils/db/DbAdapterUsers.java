package com.utils.db;


import android.database.Cursor;
import android.util.Log;

import com.example.blackbox.model.DeviceInfo;
import com.example.blackbox.model.SessionModel;
import com.example.blackbox.model.User;

import java.util.ArrayList;



public class DbAdapterUsers extends DbAdapterInit
{



    // =============================================== //
    // [ DEVICES ]
    // =============================================== //

    public void insertDeviceInfo(String ragioneSociale, String partitaIva, String address, String provincia, String comune, String cap, String storeEmail, String androidId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            //int f = (admin?1:0);
            //database.execSQL("INSERT INTO device_info(store_name, address, email,android_id, online_check) VALUES('" + storeName + "','" + address + "','" + storeEmail + "','" + androidId + "'," + 0 + ")");

            database.execSQL("INSERT INTO device_info(ragione_sociale, partita_iva,address,provincia, comune, cap,  email,android_id, online_check) VALUES('" +
                    ragioneSociale +
                    "','" + partitaIva +
                    "','" + address +
                    "','" + provincia +
                    "','" + comune +
                    "','" + cap +
                    "','" + storeEmail +
                    "','" + androidId +
                    "'," + 0 + ")");


            database.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, String.format("Error on Database method " + Thread.currentThread().getStackTrace()[2].getMethodName()));
        }
    }


    public void updateDeviceInfo(int id, String ragioneSociale, String partitaIva, String address, String provincia, String comune, String cap, String storeEmail, String androidId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            //int f = (admin?1:0);
            //database.execSQL("INSERT INTO device_info(store_name, address, email,android_id, online_check) VALUES('" + storeName + "','" + address + "','" + storeEmail + "','" + androidId + "'," + 0 + ")");

            database.execSQL("UPDATE device_info set ragione_sociale ='" + ragioneSociale + "' ,partita_iva='" + partitaIva + "', address = '" + address + "',provincia='" + provincia + "', comune='" + comune + "', cap='" + cap + "',  email='" + storeEmail + "',android_id='" + androidId + "'  where id = " + id);

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Device Error", e.getMessage());
        }
    }


    public DeviceInfo selectDeviceInfo()
    {
        DeviceInfo deviceInfo = new DeviceInfo();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("SELECT * FROM device_info LIMIT 1", null);

            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    deviceInfo.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    deviceInfo.setRagioneSociale(mCursor.getString(mCursor.getColumnIndex("ragione_sociale")));
                    deviceInfo.setPartitaIva(mCursor.getString(mCursor.getColumnIndex("partita_iva")));
                    deviceInfo.setAddress(mCursor.getString(mCursor.getColumnIndex("address")));
                    deviceInfo.setCap(mCursor.getString(mCursor.getColumnIndex("cap")));
                    deviceInfo.setComune(mCursor.getString(mCursor.getColumnIndex("comune")));
                    deviceInfo.setProvincia(mCursor.getString(mCursor.getColumnIndex("provincia")));
                    deviceInfo.setAndroidId(mCursor.getString(mCursor.getColumnIndex("android_id")));
                    deviceInfo.setEmail(mCursor.getString(mCursor.getColumnIndex("email")));
                    deviceInfo.setStoreName(mCursor.getString(mCursor.getColumnIndex("store_name")));

                }
                mCursor.close();

            }
            // database.close();
            return deviceInfo;
        }

        catch (Exception e)
        {
            Log.e(TAG, String.format("Error on Database method " + Thread.currentThread().getStackTrace()[2].getMethodName()));
            return null;
        }
    }


    public void insertDeviceInfo(String storeName, String ragioneSociale, String partitaIva, String address, String comune, String provincia, String cap, String storeEmail, String androidId, String tokenId, String ip, String multicastIp, Integer master, Integer onlineCheck)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            //int f = (admin?1:0);
            database.execSQL("INSERT INTO device_info(store_name, address, email,android_id,token_id, ip, multicast_ip, master, online_check, ragione_sociale, partita_iva, comune, provincia, cap) VALUES('"
                    + storeName + "','" + address + "','" + storeEmail + "','" + androidId + "','" + tokenId + "','" + ip + "','" + multicastIp + "', " + master + ", " + onlineCheck + ",'" + ragioneSociale + "','" + partitaIva + "','" + comune + "','" + provincia + "','" + cap + "')");
            database.close();
        }

        catch (Exception e)
        {
            Log.e(TAG, String.format("Error on Database method " + Thread.currentThread().getStackTrace()[2].getMethodName()));
        }
    }


    public void insertDeviceInfoWithId(DeviceInfo deviceInfo)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
                { database = dbHelper.getWritableDatabase(); }

            database.execSQL("INSERT INTO device_info(id,  address, email,android_id,token_id, ip, multicast_ip, master, online_check, ragione_sociale, partita_iva, comune, provincia, cap, store_name) VALUES(" + deviceInfo
                    .getId() + ",'"
                    + deviceInfo.getAddress() + "','" + deviceInfo.getEmail() + "','" + deviceInfo.getAndroidId() + "','" + deviceInfo
                    .getTokenId() + "','" + deviceInfo.getIp() + "','" + deviceInfo.getMulticastIp() + "', " + deviceInfo
                    .getMaster() + ", " + deviceInfo.getOnlineCheck() + ",'" + deviceInfo.getRagioneSociale() + "','" + deviceInfo
                    .getPartitaIva() + "','" + deviceInfo.getComune() + "','" + deviceInfo.getProvincia() + "','" + deviceInfo
                    .getCap() + "','" + deviceInfo.getStoreName() + "')");
            database.close();
        }

        catch (Exception e)
        {
            Log.e(TAG, String.format("Error on Database method " + Thread.currentThread().getStackTrace()[2].getMethodName()));
        }
    }






    // =============================================== //
    // [ USERS ]
    // =============================================== //


    public Cursor fetchUserDataByPasscode(String passcode)
    {
        try
        {
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }

            return database.rawQuery("SELECT * FROM user WHERE password = '" + passcode + "'", null);

        }

        catch (Exception e)
        {
            Log.e(TAG, String.format("Error on Database method " + Thread.currentThread().getStackTrace()[2].getMethodName()));
            return null;
        }
    }


    public void deleteUser(int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM user WHERE id=" + id + ";");
            database.close();
        }

        catch (Exception e)
        {
            Log.e(TAG, String.format("Error on Database method " + Thread.currentThread().getStackTrace()[2].getMethodName()));
        }

    }


    public User getUserByUsername(String username)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("Select * from user where email='" + username + "'", null);
            //Log.d("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            User user = new User();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {

                    user.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    user.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    user.setSurname(mCursor.getString(mCursor.getColumnIndex("surname")));
                    user.setPasscode(mCursor.getString(mCursor.getColumnIndex("passcode")));
                    user.setUserRole(mCursor.getInt(mCursor.getColumnIndex("userType")));
                    user.setEmail(mCursor.getString(mCursor.getColumnIndex("email")));

                }
                mCursor.close();

            }
            database.close();
            return user;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            User array = new User();
            return array;
        }
    }


    public ArrayList<User> fetchAllUsers()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("Select * from user", null);
            ArrayList<User> array = new ArrayList<>();
            //Log.d("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    User user = new User();
                    user.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    user.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    user.setSurname(mCursor.getString(mCursor.getColumnIndex("surname")));
                    user.setPasscode(mCursor.getString(mCursor.getColumnIndex("passcode")));
                    user.setUserRole(mCursor.getInt(mCursor.getColumnIndex("userType")));
                    user.setEmail(mCursor.getString(mCursor.getColumnIndex("email")));

                    array.add(user);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            ArrayList<User> array = new ArrayList<>();
            return array;
        }
    }


    public ArrayList<User> fetchUsersModel(int userType)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("Select * from user where userType>=" + userType, null);
            ArrayList<User> array = new ArrayList<>();
            //Log.d("fetchButtonsByQuery: ", DatabaseUtils.dumpCursorToString(mCursor));
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    User user = new User();
                    user.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    user.setName(mCursor.getString(mCursor.getColumnIndex("name")));
                    user.setSurname(mCursor.getString(mCursor.getColumnIndex("surname")));
                    user.setPasscode(mCursor.getString(mCursor.getColumnIndex("passcode")));
                    user.setUserRole(mCursor.getInt(mCursor.getColumnIndex("userType")));
                    user.setEmail(mCursor.getString(mCursor.getColumnIndex("email")));

                    array.add(user);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            ArrayList<User> array = new ArrayList<>();
            return array;
        }
    }


    public Cursor fetchUserData(String email)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM user WHERE email = '" + email + "'", null);

            database.close();
            return c;
        }
        catch (Exception e)
        {
            Log.d("Fetch User Data Error", e.getMessage());
            return null;
        }
    }


    public void insertUser(String name, String surname, String email, String password, int userType, String passcode)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            //int f = (admin?1:0);
            database.execSQL("INSERT INTO user (name, surname, email,password,userType, passcode) VALUES('" + name + "','" + surname + "','" + email + "','" + password + "'," + userType + " ,'" + passcode + "')");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert User Error", e.getMessage());
        }
    }


    public boolean checkUserPasscode(String email, String passcode)
    {
        boolean check = false;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT passcode FROM user where email='" + email + "';", null);

            if (c.moveToFirst())
            {
                String pass = c.getString(0);
                if (pass.equals(passcode))
                {
                    check = true;
                }

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }
        return check;
    }


    public boolean checkIfPasscodeExists(String passcode)
    {
        boolean check = false;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT passcode FROM user where passcode='" + passcode + "' LIMIT 1;", null);

            if (c.moveToFirst())
            {
                String pass = c.getString(0);
                if (pass.equals(passcode))
                {
                    check = true;
                }

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }
        return check;
    }


    public boolean checkIfPasscodeExistsWithId(String passcode, int id)
    {
        boolean check = false;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT passcode FROM user where passcode='" + passcode + "' AND id!=" + id + " LIMIT 1;", null);

            if (c.moveToFirst())
            {
                String pass = c.getString(0);
                if (pass.equals(passcode))
                {
                    check = true;
                }

            }
            c.close();
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Check passcode error", e.getMessage());
        }
        return check;
    }



    public void updateUserList(ArrayList<User> userList)
    {
        try
        {
            if (database.isOpen()) { database.close(); }

            database = dbHelper.getWritableDatabase();
            //execOnDb("DELETE FROM user");
            for (User user : userList)
            {
                Cursor c = database.rawQuery("Select id from user where passcode=" + user.getPasscode(), null);
                if (c.moveToFirst())
                {
                    //name = cursor.getString(column_index);//to get other values
                    int id = c.getInt(0);//to get id, 0 is the column index
                    database.execSQL("UPDATE user SET name='" + user.getName() + "' , surname='" + user
                            .getSurname() + "' , passcode='" + user.getPasscode() + "', password='" + user
                            .getPasscode() + "', email ='" + user.getEmail() + "',userType=" + user.getUserRole() + " WHERE id=" + id + ";");


                }
                else
                {
                    database.execSQL("INSERT INTO user (id, name, surname, email,password,userType, passcode) " +
                            "VALUES(" + user.getId() + ", '" + user.getName() + "','" + user.getSurname() + "','" + user
                            .getEmail() + "','" + user.getPasscode() + "'," + user.getUserRole() + " ,'" + user
                            .getPasscode() + "')");
                }

                c.close();

            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Delete failure", e.getMessage());
        }
    }


    public void insertUserFromServer(User user)
    {
        try
        {
            if (database.isOpen()) { database.close(); }
            database = dbHelper.getWritableDatabase();
            database.execSQL("INSERT INTO user (id, name, surname, email,password,userType, passcode) " +
                    "VALUES(" + user.getId() + ", '" + user.getName() + "','" + user.getSurname() + "','" + user
                    .getEmail() + "','" + user.getPasscode() + "'," + user.getUserRole() + " ,'" + user
                    .getPasscode() + "')");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Delete failure", e.getMessage());
        }
    }


    public void updateUser(String name, String surname, String passcode, String email, int userRole, int id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE user SET name='" + name + "' , surname='" + surname + "' , passcode='" + passcode + "', password='" + passcode + "', email ='" + email + "',userType=" + userRole + " WHERE id=" + id + ";");
            database.close();

        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());

        }
    }


    public void updateUserByPasscode(String name, String surname, String passcode, String email, int userRole, int id, String oldPasscode)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE user SET name='" + name + "' , surname='" + surname + "' , passcode='" + passcode + "', password='" + passcode + "', email ='" + email + "',userType=" + userRole + " WHERE passcode='" + oldPasscode + "';");
            database.close();

        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());

        }
    }






    // =============================================== //
    // [ SESSIONS ]
    // =============================================== //

    public void saveNewSessionTime(String start, String end, String sessionName)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO sessions(sessionName,start, end) VALUES('" + sessionName + "','" + start + "','" + end + "');");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("Insert Session Error", e.getMessage());
        }

    }


    public void updateNewSessionTime(int sessionId, String start, String end, String sessionName)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("UPDATE sessions SET sessionName='" + sessionName + "', start='" + start + "', end='" + end + "' WHERE id=" + sessionId + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("UPDATE Session Error", e.getMessage());
        }

    }


    public void deleteSessionTime(int sessionId)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM sessions WHERE id=" + sessionId + ";");
            database.close();
        }
        catch (Exception e)
        {
            Log.e("DELETE Session Error", e.getMessage());
        }

    }


    public ArrayList<SessionModel> getSessionsTime()
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery("Select * from sessions;", null);
            ArrayList<SessionModel> array = new ArrayList<>();
            if (mCursor != null)
            {
                while (mCursor.moveToNext())
                {
                    SessionModel session = new SessionModel();

                    session.setId(mCursor.getInt(mCursor.getColumnIndex("id")));
                    session.setSessionName(mCursor.getString(mCursor.getColumnIndex("sessionName")));
                    session.setStartTime(mCursor.getString(mCursor.getColumnIndex("start")));
                    session.setEndTime(mCursor.getString(mCursor.getColumnIndex("end")));

                    array.add(session);
                }
                mCursor.close();

            }
            database.close();
            return array;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return null;
        }

    }


    public int saveNewSession()
    {
        int id = 0;
        try
        {
            if (database.isOpen()) { database.close(); }
            database = dbHelper.getWritableDatabase();
            Boolean falseB = false;
            database.execSQL("INSERT INTO last_session(last_session_creation_time, position) VALUES(datetime(CURRENT_TIMESTAMP, 'localtime'), " + 1 + ");");
            Cursor c = database.rawQuery("SELECT * FROM last_session ORDER BY id DESC LIMIT 1", null);
            if (c.moveToFirst())
            {
                //name = cursor.getString(column_index);//to get other values
                id = c.getInt(0);//to get id, 0 is the column index

                database.close();
            }
            c.close();
        }
        catch (Exception e)
        {
            Log.d("Insert Cash List Error", e.getMessage());
        }
        return id;
    }


    public void deleteNewSession()
    {

        try
        {
            if (database.isOpen()) { database.close(); }
            database = dbHelper.getWritableDatabase();
            database.execSQL("Delete from last_session;");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("delete session error", e.getMessage());
        }

    }


}
