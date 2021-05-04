package com.example.blackbox.client;

/**
 * Created by tiziano on 1/2/18.
 */

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.blackbox.model.StaticValue;

import java.io.File;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;


public class SendFileThread extends AsyncTask<String, Void, String> {


    private int serverResponseCode;
    private String result;
    private String fileName;

    @Override
    protected String doInBackground(String... strings) {
        //File myFile = new File("/path/to/file.png");

        File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                strings[0]);

        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://www.mynoomi.com/saveInvoiceFile");

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

           /* entityBuilder.addTextBody(USER_ID, userId);
            entityBuilder.addTextBody(NAME, name);
            entityBuilder.addTextBody(TYPE, type);
            entityBuilder.addTextBody(COMMENT, comment);
            entityBuilder.addTextBody(LATITUDE, String.valueOf(User.Latitude));
            entityBuilder.addTextBody(LONGITUDE, String.valueOf(User.Longitude));*/
            entityBuilder.addTextBody("shopName", StaticValue.shopName);
            if(myFile != null)
            {
                entityBuilder.addBinaryBody("uploadedfile", myFile);
            }

            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            result = EntityUtils.toString(httpEntity);
            Log.i("RESPONSE", result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
