package com.Network;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 04/12/13.
 * Appel avec params + images
 */

public class HttpApiImageCall extends AsyncTask<String, Void, String> {

    private ApiCaller _instance = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private int _type = 0;

    public HttpApiImageCall(ApiCaller instance, List<NameValuePair> args, int type) {
        _instance = instance;
        _args = args;
        _type = type;
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            HttpClient httpclient = getNewHttpClient();
            HttpPost httppost = new HttpPost(urls[0]);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            FileBody fileBody = new FileBody(new File(urls[1]));
            builder.addPart("file", fileBody);

            for (NameValuePair p : _args) {
                Log.d(p.getName(), p.getValue());
                builder.addTextBody(p.getName(), p.getValue());
            }
            HttpEntity entity = builder.build();
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line;
                StringBuilder sbuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sbuilder.append(line);
                }
                return sbuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*
        try {
            MultipartEntity entity = new MultipartEntity();
            Log.d("HttpImageApiCall::doInBackground::url image::", urls[1]);
            for (NameValuePair p : _args) {
                Log.d(p.getName(), p.getValue());
                entity.addPart(p.getName(), new StringBody(p.getValue()));
            }
            entity.addPart("file", new FileBody(new File(urls[1])));
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        return null;
    }


    public HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String body = "";
        String content = "";

        while ((body = rd.readLine()) != null)
        {
            content += body + "\n";
        }
        return content.trim();
    }

    @Override
    protected void onPostExecute(String result) {

        System.err.println("onPostExecute" + result);

        if (result != null) {
            Log.d("HttpApiImageCall::", result);
            try {
                _instance.onApiResult(result, _type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
