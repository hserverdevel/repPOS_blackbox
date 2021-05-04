package com.example.blackbox.server;

import android.os.AsyncTask;
import android.util.Log;

import com.example.blackbox.model.StaticValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;

public class ExternalHttpHandler extends AsyncTask<Void, Void, String> {
public static final String REQUEST_METHOD = "POST";
public static final int READ_TIMEOUT = 15000;
public static final int CONNECTION_TIMEOUT = 15000;
public String url;
public List<NameValuePair> jsonValues;
public boolean tmpConf = false;
public String tmpIP = "";


public void UpdateInfoAsyncTask(String url, List<NameValuePair> jsonValues){
    this.url = url;
    this.jsonValues= jsonValues;
}

public interface AsyncResponse {

    void processFinish(String output);
}

public AsyncResponse delegate = null;

@Override
protected String doInBackground(Void ...params){
    String stringUrl = url;
    List<NameValuePair> jsonBody = jsonValues;
    String result = "";
    String inputLine;
    OutputStream out = null;
    HttpURLConnection connection = null;

    try {

            //Create a URL object holding our url

            URL url1 = new URL(this.url);
            connection = (HttpURLConnection) url1.openConnection();
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

            InputStream is = null;
            try {
                is = connection.getInputStream();
                HttpURLConnection httpConn = (HttpURLConnection) connection;


                int statusCode = httpConn.getResponseCode();
                Log.i("STATUS CODE", "" + statusCode);
                InputStreamReader isw = new InputStreamReader(is);

                result = convertStreamToString(is);

            } catch (IOException ioe) {
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection httpConn = (HttpURLConnection) connection;


                    int statusCode = httpConn.getResponseCode();
                    Log.i("STATUS CODE", "" + statusCode);
                    if (statusCode != 200) {
                        is = httpConn.getErrorStream();
                    }
                }
            }



    }
    catch(IOException e){
        e.printStackTrace();
        result = null;
    } catch (JSONException e) {
        e.printStackTrace();
        result = null;
    }
    finally {
        if (connection != null) {
            connection.disconnect();
        }
    }

    return result;
}


private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
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
        return sb.toString();

}


protected void onPostExecute(String result){
    delegate.processFinish(result);


}

}
