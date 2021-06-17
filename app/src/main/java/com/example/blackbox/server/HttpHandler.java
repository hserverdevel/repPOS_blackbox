package com.example.blackbox.server;

import android.os.AsyncTask;
import android.util.Log;

import com.example.blackbox.model.StaticValue;
import com.utils.db.DatabaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class HttpHandler extends AsyncTask<Void, Void, String> {

    private static final String TAG = "<HttpHandler>";

    public static final String REQUEST_METHOD = "POST";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    public String url;
    public List<NameValuePair> jsonValues;
    private String address;

    // a test ip used to test a blackbox
    // and thus avoiding using the StaticValue blackbox info
    public String testIp = "";

    // this deletegate will be later used in the interface AsyncResponse hereby defined
    public AsyncResponse delegate = null;



    /** define the input value for the POST request
     * @param url : the route of the request
     * @param jsonValues : the json format params to pass with the request
    */
    public void UpdateInfoAsyncTask(String url, List<NameValuePair> jsonValues) {
        this.url = url;
        this.jsonValues = jsonValues;
    }


    @Override
    protected String doInBackground(Void... params) {
        String stringUrl = url;
        List<NameValuePair> jsonBody = jsonValues;

        // the output to be returned
        String route = stringUrl.replace("/", "");
        // this is the base configuration of the result, that is returned if the connection fails
        // otherwise the `result` var will be overwritten with the result of the connection
        String result = "{\"route\": \"" + route + "\", \"success\": \"false\"}";

        HttpURLConnection connection = null;

        try
        {
            // the address on which to send the POST request
            // if the testIp is defined, use that instead of the StaticValue blackbox defined
            address = "http://" + ((testIp.equals("")) ? StaticValue.blackboxInfo.getAddress() : testIp) + stringUrl;

            // if a testIp was used, remove it
            // allowing any next connection to communicate
            // with the current defined blackbox
            testIp = "";

            Log.i(TAG, String.format("--------------> %s@%s\n\tinput:\t%s", delegate, address, jsonValues));


            //Create a URL object holding our url
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.connect();

            JSONObject jsonParam = new JSONObject();
            for (NameValuePair values : jsonBody) {
                jsonParam.put(values.getName(), values.getValue());
            }

            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();
            os.close();

            InputStream is;
            // recieve the output
            try
            {
                is = connection.getInputStream();
                int statusCode = connection.getResponseCode();

                Log.i(TAG, "InputStream status code: " + statusCode);

                result = convertStreamToString(is);

                // add a success value, to indicate that the connection to the blackbox was successful
                result = result.replace("}", ", \"success\": \"true\"}");

            }

            catch (IOException ioe)
            {
                int statusCode = connection.getResponseCode();

                Log.i(TAG, "status code: " + statusCode);

                if (statusCode != 200)
                    { Log.d(TAG, "InputStream status code != 200: " + connection.getErrorStream()); }
            }

        }

        // if any exception, return a success: false value
        // to indicate that the communication with the blackbox failed
        catch (Exception e)
            { e.printStackTrace(); }

        finally // disconnect
        {
            if (connection != null)
            { connection.disconnect(); }
        }

        return result;
    }


    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // return the result as a String,
        // and also add success: true flag
        // this flag is used to indicate that the communication with the blackbox went fine
        return sb.toString();
    }


    // pass the result of the request to the delegate
    // which is any other class that implements the AsyncResponse interface
    protected void onPostExecute(String result)
    {
        Log.i(TAG, String.format("%s@%s -------------->\n\toutput:\t%s", delegate, address, result));

        delegate.processFinish(result);
    }



    /**
     * An interface to allow the various activities to use HttpHandler.
     * Each activity will be the `delegate` variable,
     * and as such, they will be able to call `processFinish` to obtain
     * the output of the Http Request sent from HttpHandler
     **/
    public interface AsyncResponse {

        void processFinish(String output);
    }



}
