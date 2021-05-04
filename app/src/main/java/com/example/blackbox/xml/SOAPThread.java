package com.example.blackbox.xml;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

/**
 * Created by tiziano on 12/18/18.
 */

/*
public class SOAPThread {
}
*/

public class SOAPThread extends AsyncTask<String, Void, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... arg) {
        String fdfd = "";
        try {
            fdfd = sendSoapRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fdfd;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Log.i("transactionresponse", result);

       /* if (!result.equalsIgnoreCase("")) {
            try {
                Object helpher;
                helpher.deleteTotalRecord();
                String ffsd = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + result.toString();
                XmlToJson xmlToJson = new XmlToJson.Builder(ffsd.trim()).build();
                JSONObject jsonObject = xmlToJson.toJson();


            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

        }
*/

    }

    public String sendSoapRequest() throws Exception {


        String finalString = "Paste your whole request through which you can send request from browser sucessfully";
        Log.i("TAG", "*********************** FinalString Before "
                + finalString);



        // send SOAP request
        InputStream resInputStream = sendRequest(finalString);

        // create the response SOAP envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        // process SOAP response
        parseResponse(resInputStream, envelope);

        Object bodyIn = envelope.bodyIn;

        SoapObject RequestSOAP = (SoapObject) envelope.bodyIn;
        String response = RequestSOAP.getProperty(0).toString();
        if (bodyIn instanceof SoapFault) {
            throw (SoapFault) bodyIn;
        }
        return response.toString();
    }

    private InputStream sendRequest(String requestContent) throws Exception {

        // initialize HTTP post
        HttpPost httpPost = null;

        try {
            httpPost = new HttpPost("");

            httpPost.addHeader("Content-Type", "text/xml;charset=UTF-8");
            httpPost.addHeader("SOAPAction", "Your Soap Action");
        } catch (Throwable e) {
            Log.e("LOG_TAG", "Error initializing HTTP post for SOAP request", e);
            // throw e;
        }

        // load content to be sent
        try {
            HttpEntity postEntity = new StringEntity(requestContent);
            httpPost.setEntity(postEntity);
        } catch (UnsupportedEncodingException e) {
            Log.e("LOG_TAG",
                    "Unsupported ensoding of content for SOAP request", e);
            throw e;
        }

        // send request
        HttpResponse httpResponse = null;
        HttpClient httpClient = new DefaultHttpClient();
        try {
            httpResponse = httpClient.execute(httpPost);
        } catch (Throwable e) {
            Log.e("LOG_TAG", "Error sending SOAP request", e);
            // throw e;
        }

        // get SOAP response
        try {
            // get response code
            int responseStatusCode = httpResponse.getStatusLine()
                    .getStatusCode();

            // if the response code is not 200 - OK, or 500 - Internal error,
            // then communication error occurred
            if (responseStatusCode != 200 && responseStatusCode != 500) {
                String errorMsg = "Got SOAP response code "
                        + responseStatusCode + " "
                        + httpResponse.getStatusLine().getReasonPhrase();
                // ...
            }

            // get the response content
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream is = httpEntity.getContent();
            return is;
        } catch (Throwable e) {
            Log.e("LOG_TAG", "Error getting SOAP response", e);
            // throw e;
        }
        return null;
    }

    /**
     * Parses the input stream from the response into SoapEnvelope object.
     */
    private void parseResponse(InputStream is, SoapEnvelope envelope)
            throws Exception {

        try {
            XmlPullParser xp = new KXmlParser();
            xp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            xp.setInput(is, "UTF-8");
            envelope.parse(xp);
        } catch (Throwable e) {
            Log.e("LOG_TAG", "Error reading/parsing SOAP response", e);

        }

    }
}
