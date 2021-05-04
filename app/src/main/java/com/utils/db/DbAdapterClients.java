package com.utils.db;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.example.blackbox.model.Client;
import com.example.blackbox.model.ClientInCompany;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.Company;

import java.util.ArrayList;

public class DbAdapterClients extends DbAdapterTables
{




    // =============================================== //
    // [ CLIENTS ]
    // =============================================== //


    /**
     * @param name
     * @param surname
     * @param email
     * @return returns the int id of the newly inserted client or a reference to the client data in db if it was already present
     */
    public int insertClient(String name, String surname, String email)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            int id;
            Cursor c = database.rawQuery("SELECT * FROM client WHERE email = '" + email + "'", null);

            if (!c.moveToFirst())
            {
                c.close();
                database.execSQL("INSERT INTO client (name, surname, email) VALUES('" + name + "','" + surname + "','" + email + "');");
                c = database.rawQuery("SELECT id FROM client ORDER BY id DESC", null);
                c.moveToFirst();
                id = c.getInt(0);
                c.close();
            }
            else
            {
                id = -1;
                c.close();
            }
            database.close();
            return id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }


    public void updateClientData(ClientInfo client)
    {
        //if (database.isOpen()) database.close();
        if (!database.isOpen())
        { database = dbHelper.getWritableDatabase(); }

        try
        {
            database.execSQL("UPDATE client SET name = '" + client.getName() + "', surname = '" + client
                    .getSurname() + "', " +
                    "email = '" + client.getEmail() + "', fidelity_id = " + client.getFidelity_id() + "' " +
                    " WHERE id =" + client.getClient_id());
            database.execSQL("UPDATE client_in_company SET company_id = " + client.getCompany_id() + " WHERE id =" + client
                    .getClient_in_company_id());
            if (client.getCompany_id() != -1)
            {
                database.execSQL("UPDATE company SET company_name = '" + client.getCompany_name() + "', " +
                        "address = '" + client.getCompany_address() + "', " +
                        "vat_number = '" + client.getCompany_vat_number() + "', " +
                        "postal_code = '" + client.getCompany_postal_code() + "', " +
                        "city = '" + client.getCompany_city() + "', " +
                        "country = '" + client.getCompany_country() + "', " +
                        "codice_fiscale = '" + client.getCodice_fiscale() + "', " +
                        "provincia = '" + client.getProvincia() + "', " +
                        "codice_destinatario = '" + client.getCodice_destinatario() + "', " +
                        "pec = '" + client.getPec() + "' " +
                        " WHERE id =" + client.getCompany_id());
            }
            database.execSQL("UPDATE customer_bill SET description=\'" + client.getName() + " " + client
                    .getSurname() +
                    "\' WHERE client_id=" + client.getClient_id() + ";");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        database.close();

    }


    /**
     * @param c_name
     * @param address
     * @param vat_number
     * @param postal_code
     * @param city
     * @param country
     * @return returns the int id of the newly inserted company or a reference to it's id if it was present already
     */
    public int insertCompany(String c_name, String address, String vat_number, String postal_code, String city, String country, String codiceFiscale, String provincia, String codiceDestinatario, String pec)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            int id;
            Cursor c = database.rawQuery("SELECT id FROM company WHERE vat_number = '" + vat_number + "'", null);

            if (!c.moveToFirst())
            {
                c.close();
                database.execSQL("INSERT INTO company (company_name, address, vat_number, postal_code, city, country, codice_fiscale, provincia, codice_destinatario, pec)" +
                        " VALUES('" + c_name + "','" + address + "','" + vat_number + "','" + postal_code + "','" + city + "','" + country + "','" + codiceFiscale + "' ,'" + provincia + "','" + codiceDestinatario + "','" + pec + "');");
                c = database.rawQuery("SELECT id FROM company ORDER BY id DESC", null);
                c.moveToFirst();
                id = c.getInt(0);
                c.close();
            }
            else
            {
                id = c.getInt(0);
                c.close();
            }
            database.close();
            return id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * @param client_id
     * @param company_id if  == -1 it means no company is present.
     */
    public void insertClientInCompany(int client_id, int company_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO client_in_company (client_id, company_id) VALUES(" + client_id + "," + company_id + ");");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        database.close();

    }


    public ClientInfo fetchSingleClient(int clientId)
    {
        ClientInfo clientInfo = new ClientInfo();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }

               /* client_in_company_id = c.getInt(c.getColumnIndex("id"));
                client_id = c.getInt(c.getColumnIndex("client_id"));
                company_id = c.getInt(c.getColumnIndex("company_id"));*/

            Cursor c1 = database.rawQuery("SELECT * FROM client WHERE id = " + clientId, null);
            if (c1.moveToFirst())
            {
                    /*name = c1.getString(c1.getColumnIndex("name"));
                    surname = c1.getString(c1.getColumnIndex("surname"));
                    email = c1.getString(c1.getColumnIndex("email"));*/
                clientInfo.setClient_id(clientId);
                clientInfo.setName(c1.getString(c1.getColumnIndex("name")));
                clientInfo.setSurname(c1.getString(c1.getColumnIndex("surname")));
                clientInfo.setEmail(c1.getString(c1.getColumnIndex("email")));
                clientInfo.setFidelity_id(c1.getInt(c1.getColumnIndex("fidelity_id")));
            }
            c1.close();
            database.close();

            return clientInfo;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return clientInfo;
        }
    }


    public ClientInfo fetchSingleClientByCode(String code)
    {
        ClientInfo clientInfo = new ClientInfo();
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }

               /* client_in_company_id = c.getInt(c.getColumnIndex("id"));
                client_id = c.getInt(c.getColumnIndex("client_id"));
                company_id = c.getInt(c.getColumnIndex("company_id"));*/

            Cursor c1 = database.rawQuery("SELECT * FROM client WHERE codeValue = '" + code + "' AND fidelity_id!=-1", null);
            if (c1.moveToFirst())
            {
                    /*name = c1.getString(c1.getColumnIndex("name"));
                    surname = c1.getString(c1.getColumnIndex("surname"));
                    email = c1.getString(c1.getColumnIndex("email"));*/
                clientInfo.setClient_id(c1.getInt(c1.getColumnIndex("id")));
                clientInfo.setName(c1.getString(c1.getColumnIndex("name")));
                clientInfo.setSurname(c1.getString(c1.getColumnIndex("surname")));
                clientInfo.setEmail(c1.getString(c1.getColumnIndex("email")));
                clientInfo.setFidelity_id(c1.getInt(c1.getColumnIndex("fidelity_id")));
            }
            else
            {
                clientInfo.setClient_id((-1));
            }
            c1.close();
            database.close();

            return clientInfo;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return clientInfo;
        }
    }


    public ClientInfo fetchSingleClientForPayment(int clientId)
    {
        try
        {
            String name;
            String surname;
            String email;
            String company_name;
            String company_vat;
            String codice_fiscale;
            int client_id;
            int company_id;
            int client_in_company_id;
            ClientInfo clientInfo = new ClientInfo();
            // int orders_made;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM client_in_company where client_id=" + clientId, null);
            while (c.moveToNext())
            {
                clientInfo = new ClientInfo();
                client_in_company_id = c.getInt(c.getColumnIndex("id"));
                client_id = c.getInt(c.getColumnIndex("client_id"));
                company_id = c.getInt(c.getColumnIndex("company_id"));
                clientInfo.setClient_id(client_id);
                clientInfo.setClient_in_company_id(client_in_company_id);
                clientInfo.setCompany_id(company_id);
                Cursor c1 = database.rawQuery("SELECT * FROM client WHERE id = " + client_id, null);
                if (c1.moveToFirst())
                {
                    name = c1.getString(c1.getColumnIndex("name"));
                    surname = c1.getString(c1.getColumnIndex("surname"));
                    email = c1.getString(c1.getColumnIndex("email"));
                    clientInfo.setName(name);
                    clientInfo.setSurname(surname);
                    clientInfo.setEmail(email);
                }
                c1.close();

                if (company_id != -1)
                {
                    clientInfo.setHasCompany(true);
                    Cursor c2 = database.rawQuery("SELECT * FROM company WHERE id=" + company_id, null);
                    if (c2.moveToFirst())
                    {
                        company_name = c2.getString(c2.getColumnIndex("company_name"));
                        String company_address = c2.getString(c2.getColumnIndex("address"));
                        company_vat = c2.getString(c2.getColumnIndex("vat_number"));
                        String company_postal_code = c2.getString(c2.getColumnIndex("postal_code"));
                        String company_city = c2.getString(c2.getColumnIndex("city"));
                        String company_country = c2.getString(c2.getColumnIndex("country"));
                        codice_fiscale = c2.getString(c2.getColumnIndex("codice_fiscale"));
                        String provincia = c2.getString(c2.getColumnIndex("provincia"));
                        clientInfo.setCompany_name(company_name);
                        clientInfo.setCompany_address(company_address);
                        clientInfo.setCompany_vat_number(company_vat);
                        clientInfo.setCompany_postal_code(company_postal_code);
                        clientInfo.setCompany_city(company_city);
                        clientInfo.setCompany_country(company_country);
                        clientInfo.setCodice_fiscale(codice_fiscale);
                        clientInfo.setProvincia(provincia);
                        String codiceDestinatario = c2.getString(c2.getColumnIndex("codice_destinatario"));
                        String pec = c2.getString(c2.getColumnIndex("pec"));
                        clientInfo.setCodice_destinatario(codiceDestinatario);
                        clientInfo.setPec(pec);
                    }
                    c2.close();
                }
                //array.add(clientInfo);
            }
            c.close();
            database.close();

            return clientInfo;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public ArrayList<ClientInfo> fetchClients()
    {
        try
        {
            String name;
            String surname;
            String email;
            String company_name;
            String company_vat;
            String codice_fiscale;
            int client_id;
            int company_id;
            int client_in_company_id;
            ClientInfo clientInfo;
            ArrayList<ClientInfo> array = new ArrayList<>();
            // int orders_made;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM client_in_company ORDER BY id DESC", null);
            while (c.moveToNext())
            {
                clientInfo = new ClientInfo();
                client_in_company_id = c.getInt(c.getColumnIndex("id"));
                client_id = c.getInt(c.getColumnIndex("client_id"));
                company_id = c.getInt(c.getColumnIndex("company_id"));
                clientInfo.setClient_id(client_id);
                clientInfo.setClient_in_company_id(client_in_company_id);
                clientInfo.setCompany_id(company_id);
                Cursor c1 = database.rawQuery("SELECT * FROM client WHERE id = " + client_id, null);
                if (c1.moveToFirst())
                {
                    name = c1.getString(c1.getColumnIndex("name"));
                    surname = c1.getString(c1.getColumnIndex("surname"));
                    email = c1.getString(c1.getColumnIndex("email"));
                    clientInfo.setName(name);
                    clientInfo.setSurname(surname);
                    clientInfo.setEmail(email);
                    clientInfo.setFidelity_id(c1.getInt(c1.getColumnIndex("fidelity_id")));
                }


                if (company_id != -1)
                {
                    clientInfo.setHasCompany(true);
                    Cursor c2 = database.rawQuery("SELECT * FROM company WHERE id=" + company_id, null);
                    if (c2.moveToFirst())
                    {
                        company_name = c2.getString(c2.getColumnIndex("company_name"));
                        String company_address = c2.getString(c2.getColumnIndex("address"));
                        company_vat = c2.getString(c2.getColumnIndex("vat_number"));
                        String company_postal_code = c2.getString(c2.getColumnIndex("postal_code"));
                        String company_city = c2.getString(c2.getColumnIndex("city"));
                        String company_country = c2.getString(c2.getColumnIndex("country"));
                        codice_fiscale = c2.getString(c2.getColumnIndex("codice_fiscale"));
                        String provincia = c2.getString(c2.getColumnIndex("provincia"));
                        clientInfo.setCompany_name(company_name);
                        clientInfo.setCompany_address(company_address);
                        clientInfo.setCompany_vat_number(company_vat);
                        clientInfo.setCompany_postal_code(company_postal_code);
                        clientInfo.setCompany_city(company_city);
                        clientInfo.setCompany_country(company_country);
                        clientInfo.setCodice_fiscale(codice_fiscale);
                        clientInfo.setProvincia(provincia);
                        String codiceDestinatario = c2.getString(c2.getColumnIndex("codice_destinatario"));
                        String pec = c2.getString(c2.getColumnIndex("pec"));
                        clientInfo.setCodice_destinatario(codiceDestinatario);
                        clientInfo.setPec(pec);
                    }
                    c2.close();
                }
                c1.close();
                array.add(clientInfo);
            }
            c.close();
            database.close();

            return array;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public ArrayList<ClientInfo> fetchExclusiveClients(int billId)
    {
        try
        {
            String name;
            String surname;
            String email;
            String company_name;
            String company_vat;
            String codice_fiscale;
            int client_id;
            int company_id;
            int client_in_company_id;
            ClientInfo clientInfo;
            ArrayList<ClientInfo> array = new ArrayList<>();
            // int orders_made;
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor c = database.rawQuery("SELECT * FROM client_in_company ORDER BY id DESC", null);
            while (c.moveToNext())
            {
                if (!checkCustomerInClient(c.getInt(c.getColumnIndex("client_id")), billId))
                {

                    clientInfo = new ClientInfo();
                    client_in_company_id = c.getInt(c.getColumnIndex("id"));
                    client_id = c.getInt(c.getColumnIndex("client_id"));
                    company_id = c.getInt(c.getColumnIndex("company_id"));
                    clientInfo.setClient_id(client_id);
                    clientInfo.setClient_in_company_id(client_in_company_id);
                    clientInfo.setCompany_id(company_id);
                    Cursor c1 = database.rawQuery("SELECT * FROM client WHERE id = " + client_id, null);
                    if (c1.moveToFirst())
                    {
                        name = c1.getString(c1.getColumnIndex("name"));
                        surname = c1.getString(c1.getColumnIndex("surname"));
                        email = c1.getString(c1.getColumnIndex("email"));
                        clientInfo.setName(name);
                        clientInfo.setSurname(surname);
                        clientInfo.setEmail(email);
                    }
                    c1.close();

                    if (company_id != -1)
                    {
                        clientInfo.setHasCompany(true);
                        Cursor c2 = database.rawQuery("SELECT * FROM company WHERE id=" + company_id, null);
                        if (c2.moveToFirst())
                        {
                            company_name = c2.getString(c2.getColumnIndex("company_name"));
                            String company_address = c2.getString(c2.getColumnIndex("address"));
                            company_vat = c2.getString(c2.getColumnIndex("vat_number"));
                            String company_postal_code = c2.getString(c2.getColumnIndex("postal_code"));
                            String company_city = c2.getString(c2.getColumnIndex("city"));
                            String company_country = c2.getString(c2.getColumnIndex("country"));
                            String provincia = c2.getString(c2.getColumnIndex("provincia"));
                            codice_fiscale = c2.getString(c2.getColumnIndex("codice_fiscale"));
                            clientInfo.setCompany_name(company_name);
                            clientInfo.setCompany_address(company_address);
                            clientInfo.setCompany_vat_number(company_vat);
                            clientInfo.setCompany_postal_code(company_postal_code);
                            clientInfo.setCompany_city(company_city);
                            clientInfo.setCompany_country(company_country);
                            clientInfo.setCodice_fiscale(codice_fiscale);
                            clientInfo.setProvincia(provincia);
                            String codiceDestinatario = c2.getString(c2.getColumnIndex("codice_destinatario"));
                            String pec = c2.getString(c2.getColumnIndex("pec"));
                            clientInfo.setCodice_destinatario(codiceDestinatario);
                            clientInfo.setPec(pec);
                        }
                        c2.close();
                    }
                    array.add(clientInfo);
                }
            }
            c.close();
            database.close();

            return array;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public boolean checkCustomerInClient(int clientId, int billId)
    {
        String query = "SELECT * FROM customer_bill " +
                " LEFT JOIN client " +
                " ON customer_bill.client_id=client.id" +
                " LEFT JOIN product_bill " +
                " ON product_bill.id = customer_bill.prod_bill_id" +
                " WHERE product_bill.bill_id= " + billId +
                " AND client.id= " + clientId;
        boolean check = false;
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getReadableDatabase(); }
            Cursor mCursor = database.rawQuery(query, null);
            while (mCursor.moveToNext())
            {
                check = true;
            }

            //   database.close();
            mCursor.close();
            /**
             * QUESTO DEVE RIMANERE SPENTO PERCHÈ VIENE CHIAMATO DA UN ALTRO METODO QUI DENTRO
             */
            //database.close();

            return check;
        }
        catch (Exception e)
        {
            Log.d("fetchFailure", e.getMessage());
            return false;
        }
    }


    public ArrayList<ClientInfo> searchClients(String key)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            String regexp = ".*" + key + ".*";
            String name;
            String surname;
            String email;
            String company_name;
            String company_vat;
            String codice_fiscale;
            int client_in_company_id;
            int client_id;
            int company_id;
            ArrayList<ClientInfo> array = new ArrayList<>();
            ArrayList<Integer> client_in_company_ids = new ArrayList<>();
            Cursor c = database.rawQuery("SELECT * FROM client WHERE upper(name) REGEXP '" + regexp.toUpperCase() +
                    "' OR upper(surname) REGEXP '" + regexp.toUpperCase() +
                    "' OR email REGEXP '" + regexp + "'", null);
            while (c.moveToNext())
            {
                client_id = c.getInt(c.getColumnIndex("id"));
                name = c.getString(c.getColumnIndex("name"));
                surname = c.getString(c.getColumnIndex("surname"));
                email = c.getString(c.getColumnIndex("email"));
                Cursor c1 = database.rawQuery("SELECT * FROM client_in_company WHERE client_id = " + client_id, null);
                while (c1.moveToNext())
                {
                    ClientInfo clientInfo = new ClientInfo();
                    client_in_company_id = c1.getInt(c1.getColumnIndex("id"));
                    client_in_company_ids.add(client_in_company_id);
                    company_id = c1.getInt(c1.getColumnIndex("company_id"));
                    clientInfo.setName(name);
                    clientInfo.setSurname(surname);
                    clientInfo.setEmail(email);
                    clientInfo.setClient_id(client_id);
                    clientInfo.setClient_in_company_id(client_in_company_id);
                    if (company_id != -1)
                    {
                        clientInfo.setHasCompany(true);
                        clientInfo.setCompany_id(company_id);
                        Cursor c2 = database.rawQuery("SELECT * FROM company WHERE id=" + company_id, null);
                        c2.moveToFirst();
                        company_name = c2.getString(c2.getColumnIndex("company_name"));
                        String company_address = c2.getString(c2.getColumnIndex("address"));
                        company_vat = c2.getString(c2.getColumnIndex("vat_number"));
                        String company_postal_code = c2.getString(c2.getColumnIndex("postal_code"));
                        String company_city = c2.getString(c2.getColumnIndex("city"));
                        String company_country = c2.getString(c2.getColumnIndex("country"));
                        codice_fiscale = c2.getString(c2.getColumnIndex("codice_fiscale"));
                        String provincia = c2.getString(c2.getColumnIndex("provincia"));
                        clientInfo.setCompany_name(company_name);
                        clientInfo.setCompany_address(company_address);
                        clientInfo.setCompany_vat_number(company_vat);
                        clientInfo.setCompany_postal_code(company_postal_code);
                        clientInfo.setCompany_city(company_city);
                        clientInfo.setCompany_country(company_country);
                        clientInfo.setCodice_fiscale(codice_fiscale);
                        clientInfo.setProvincia(provincia);
                        String codiceDestinatario = c2.getString(c2.getColumnIndex("codice_destinatario"));
                        String pec = c2.getString(c2.getColumnIndex("pec"));
                        clientInfo.setCodice_destinatario(codiceDestinatario);
                        clientInfo.setPec(pec);
                        c2.close();
                    }
                    array.add(clientInfo);
                }
                c1.close();
            }
            c.close();

            c = database.rawQuery("SELECT * FROM company WHERE company_name REGEXP '" + regexp + "' " +
                    "OR vat_number REGEXP '" + regexp + "'", null);
            while (c.moveToNext())
            {
                company_id = c.getInt(c.getColumnIndex("id"));
                Cursor c1 = database.rawQuery("SELECT * FROM client_in_company WHERE company_id = " + company_id, null);
                while (c1.moveToNext())
                {
                    client_in_company_id = c1.getInt(c1.getColumnIndex("id"));
                    if (!client_in_company_ids.contains(client_in_company_id))
                    {
                        ClientInfo clientInfo = new ClientInfo();
                        client_in_company_ids.add(client_in_company_id);
                        client_id = c1.getInt(c1.getColumnIndex("client_id"));
                        Cursor c2 = database.rawQuery("SELECT * FROM client WHERE id = " + client_id, null);
                        c2.moveToFirst();

                        clientInfo.setHasCompany(true);
                        clientInfo.setCompany_id(company_id);
                        company_name = c.getString(c.getColumnIndex("company_name"));
                        String company_address = c.getString(c.getColumnIndex("address"));
                        company_vat = c.getString(c.getColumnIndex("vat_number"));
                        String company_postal_code = c.getString(c.getColumnIndex("postal_code"));
                        String company_city = c.getString(c.getColumnIndex("city"));
                        String company_country = c.getString(c.getColumnIndex("country"));
                        String provincia = c.getString(c.getColumnIndex("provincia"));
                        codice_fiscale = c2.getString(c2.getColumnIndex("codice_fiscale"));
                        clientInfo.setCompany_name(company_name);
                        clientInfo.setCompany_address(company_address);
                        clientInfo.setCompany_vat_number(company_vat);
                        clientInfo.setCompany_postal_code(company_postal_code);
                        clientInfo.setCompany_city(company_city);
                        clientInfo.setCompany_country(company_country);
                        clientInfo.setClient_in_company_id(client_in_company_id);
                        clientInfo.setCodice_fiscale(codice_fiscale);
                        clientInfo.setProvincia(provincia);
                        name = c2.getString(c2.getColumnIndex("name"));
                        surname = c2.getString(c2.getColumnIndex("surname"));
                        email = c2.getString(c2.getColumnIndex("email"));
                        clientInfo.setName(name);
                        clientInfo.setSurname(surname);
                        clientInfo.setEmail(email);
                        clientInfo.setClient_id(client_id);
                        String codiceDestinatario = c2.getString(c2.getColumnIndex("codice_destinatario"));
                        String pec = c2.getString(c2.getColumnIndex("pec"));
                        clientInfo.setCodice_destinatario(codiceDestinatario);
                        clientInfo.setPec(pec);

                        array.add(clientInfo);
                        c2.close();
                    }
                }
                c1.close();
            }
            c.close();
            database.close();

            return array;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public void deleteClient(int client_id, int company_id)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("DELETE FROM client WHERE id=" + client_id + ";");
            database.execSQL("DELETE FROM client_in_company WHERE client_id=" + client_id + ";");
            database.execSQL("DELETE FROM discount WHERE client_id=" + client_id + ";");
            if (company_id != -1)
            {
                database.execSQL("DELETE FROM company WHERE id=" + company_id + ";");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Deleting error", e.getMessage());
        }
    }


    public int getClientFromProductBill(int bill_total_id)
    {
        int client_id = -1;
        try
        {
            if (database.isOpen()) { database.close(); }
            database = dbHelper.getReadableDatabase();
            Cursor c = database.rawQuery("SELECT * FROM customer_bill " +
                    "LEFT JOIN product_bill ON customer_bill.prod_bill_id=product_bill.id " +
                    "LEFT JOIN bill_total ON product_bill.bill_id=bill_total.id " +
                    "WHERE bill_total.id=" + bill_total_id + ";", null);
            if (c != null)
            {
                while (c.moveToNext())
                {
                    client_id = c.getInt(c.getColumnIndex("client_id"));
                }
                c.close();
            }
            database.close();
            return client_id;
        }
        catch (Exception e)
        {
            Log.d("Error", e.getMessage());
            return client_id;
        }
    }



    public void insertClientSync(ArrayList<Client> clients)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (Client c : clients)
            {
                database.execSQL("INSERT INTO client (id, name, surname, email, fidelity_id, codeValue) VALUES(" + c
                        .getId() + " ,'" + c.getName() + "','" + c.getSurname() + "','" + c.getEmail() + "', " + c
                        .getFidelity_id() + ", '" + c.getCodeValue() + "'  );");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert mA Error", e.getMessage());
        }
    }


    public void insertClientFromServer(Client c)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO client (id, name, surname, email, fidelity_id, codeValue) VALUES(" + c
                    .getId() + " ,'" + c.getName() + "','" + c.getSurname() + "','" + c.getEmail() + "' , " + c
                    .getFidelity_id() + ", '" + c.getCodeValue() + "'  );");

            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert mA Error", e.getMessage());
        }
    }


    public void insertCompanySync(ArrayList<Company> companies)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (Company c : companies)
            {
                database.execSQL("INSERT INTO company (id, company_name, address, vat_number, postal_code, city, country, codice_fiscale, provincia, codice_destinatario, pec)" +
                        " VALUES(" + c.getId() + ",'" + c.getCompanyName() + "','" + c.getAddress() + "','" + c
                        .getVatNumber() + "','" + c.getPostalCode() + "','" + c.getCity() + "','" + c
                        .getCountry() + "','" + c.getCodiceFiscale() + "' ,'" + c.getProvincia() + "','" + c
                        .getCodiceDestinatario() + "','" + c.getPec() + "');");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert mA Error", e.getMessage());
        }
    }


    public void insertCompanyFromServer(Company c)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO company (id, company_name, address, vat_number, postal_code, city, country, codice_fiscale, provincia, codice_destinatario, pec)" +
                    " VALUES(" + c.getId() + ",'" + c.getCompanyName() + "','" + c.getAddress() + "','" + c
                    .getVatNumber() + "','" + c.getPostalCode() + "','" + c.getCity() + "','" + c.getCountry() + "','" + c
                    .getCodiceFiscale() + "' ,'" + c.getProvincia() + "','" + c.getCodiceDestinatario() + "','" + c
                    .getPec() + "');");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert mA Error", e.getMessage());
        }
    }


    public void insertCiCSync(ArrayList<ClientInCompany> cics)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            for (ClientInCompany c : cics)
            {
                database.execSQL("INSERT INTO client_in_company (id, client_id, company_id) VALUES(" + c
                        .getId() + ", " + c.getClientId() + "," + c.getCompanyId() + ");");
            }
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert mA Error", e.getMessage());
        }
    }


    public void insertCiCFromServer(ClientInCompany c)
    {
        try
        {
            //if (database.isOpen()) database.close();
            if (!database.isOpen())
            { database = dbHelper.getWritableDatabase(); }
            database.execSQL("INSERT INTO client_in_company (id, client_id, company_id) VALUES(" + c
                    .getId() + ", " + c.getClientId() + "," + c.getCompanyId() + ");");
            database.close();
        }
        catch (Exception e)
        {
            Log.d("Insert mA Error", e.getMessage());
        }
    }



}
