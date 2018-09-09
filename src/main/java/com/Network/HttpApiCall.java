package com.Network;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;

import com.Tools.Tools;
import com.ads.ManageAds;
import com.carpedeum.Today;
import com.classifieds.Classified;
import com.classifieds.Classifieds;
import com.classifieds.CreateClassified;
import com.classifieds.EditClassified;
import com.classifieds.MyClassifieds;
import com.status.CreateStatus;
import com.status.DisplayStatus;
import com.together.Together;
import com.user.ChangeEmail;
import com.user.ChangePassword;
import com.user.DeleteProfile;
import com.user.EditProfileInfos;
import com.user.MyProfile;
import com.user.Parvis;
import com.user.Profile;
import com.user.Register;
import com.voice.GodVoice;
import com.voice.ManVoice;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by COCH on 15/07/13.
 * Call CarpeDeum API
 */

public class HttpApiCall extends AsyncTask<String, Void, Void> {

    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private String _result = null;
    private int _type = 0;
    //private MyProfile _myProfileInstance = null;
    private EditProfileInfos _editProfileInstance = null;
    private CreateStatus _createStatusInstance = null;
    private DisplayStatus _displayStatus = null;
    private MyClassifieds _myclassifieds = null;
    private Classified _classified = null;
    private ChangeEmail _changeEmail = null;
    private ChangePassword _changePassword = null;
    private ManageAds _manageAds = null;
    private Register _registerInstance = null;
    private DeleteProfile _deleteProfileInstance = null;

    private static final long CONN_MGR_TIMEOUT = 10000;
    private static final int CONN_TIMEOUT = 50000;
    private static final int SO_TIMEOUT = 50000;


    public final static String INIT = "INIT";
    public final static String PENDING = "PENDING";
    public final static String FINISHED = "FINISHED";

    private String statut = null;
    public String getStatut() {
        return statut;
    }
    public void setStatut(String statut) {
        this.statut = statut;
    }

    private ApiCaller _instance = null;

    public HttpApiCall(ApiCaller instance, List<NameValuePair> args, int type) {
        _instance = instance;

        _args = args;
        _type = type;

        statut = INIT;
    }

    /*
    public HttpApiCall(MyProfile myProfile, String uid, String sid) {
        _myProfileInstance = myProfile;
        _args.add(new BasicNameValuePair("uid", uid));
        _args.add(new BasicNameValuePair("id", uid));
        _args.add(new BasicNameValuePair("sid", sid));
    }*/

    /*
    public HttpApiCall(MyProfile myProfile, List<NameValuePair> args, int type) {
        _myProfileInstance = myProfile;
        _args = args;
        _type = type;
    }*/

    public HttpApiCall(EditProfileInfos editProfileInfos, List<NameValuePair> args) {
        _editProfileInstance = editProfileInfos;
        _args = args;
    }

    public HttpApiCall(CreateStatus createStatus, List<NameValuePair> args) {
        _createStatusInstance = createStatus;
        _args = args;
    }

    public HttpApiCall(DisplayStatus displayStatus, List<NameValuePair> args, int type) {
        _displayStatus = displayStatus;
        _args = args;
        _type = type;
    }

    public HttpApiCall(MyClassifieds myClassifieds, List<NameValuePair> args) {
        _myclassifieds = myClassifieds;
        _args = args;
    }

    public HttpApiCall(Classified classified, List<NameValuePair> args, int type) {
        _classified = classified;
        _args = args;
        _type = type;
    }

    public HttpApiCall(ChangeEmail changeEmail, List<NameValuePair> args) {
        _args = args;
        _changeEmail = changeEmail;
    }

    public HttpApiCall(ChangePassword changePassword, List<NameValuePair> args) {
        _args = args;
        _changePassword = changePassword;
    }

    /*
    public HttpApiCall(Together together, List<NameValuePair> args, int type) {
        _args = args;
        _type = type;
        _together = together;
    }*/

    public HttpApiCall(ManageAds manageAds, List<NameValuePair> args) {
        _manageAds = manageAds;
        _args = args;
    }

    public HttpApiCall(Register register, List<NameValuePair> args, int type) {
        _args = args;
        _type = type;
        _registerInstance = register;
    }

    public HttpApiCall(DeleteProfile deleteProfile, List<NameValuePair> args) {
        _deleteProfileInstance = deleteProfile;
        _args = args;
    }

    @Override
    protected Void doInBackground(String... urls) {
        try {
            //Log.d("URL: ", urls[0]);

            //HttpParams params = new BasicHttpParams();
            //HttpConnectionParams.setConnectionTimeout(params, 3000);
            //HttpConnectionParams.setSoTimeout(params, 10000);

            HttpPost httppost = new HttpPost(urls[0]);

            HttpClient httpclient = getNewHttpClient();

            if (_args != null && _args.size() > 0) {
                _args.add(new BasicNameValuePair("v", "android-1"));
                _args.add(new BasicNameValuePair("l", "fr"));
                java.util.Date date = new java.util.Date();
                _args.add(new BasicNameValuePair("t", String.valueOf(new Timestamp(date.getTime()))));

                System.out.println("------------------------------ : " + urls[0]);

                for (NameValuePair np : _args)  {

                    System.out.println("-> " + np.getName() + " - " + np.getValue());

                }

                System.out.println("------------------------------");

                httppost.setEntity(new UrlEncodedFormEntity(_args, "UTF-8"));
            }
            try {
                HttpResponse response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    //_result = reader.readLine();
                    String line;
                    StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    _result = builder.toString();
                }
            } catch (Exception e) {
                //TODO message erreur
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onPostExecute(Void result) {

        statut = FINISHED;

        //if (Tools.CDDEBUG && _result != null)
          //  Log.d("Result::", _result);
        try {
            if (_result != null && _instance != null) {
                _instance.onApiResult(_result, _type);
                _instance = null;
            }
            if (_editProfileInstance != null) {
                _editProfileInstance.setResultCode(_result);
                _editProfileInstance = null;
            }
            if (_createStatusInstance != null) {
                _createStatusInstance.onApiResult(_result);
                _createStatusInstance = null;
            }
            if (_displayStatus != null) {
                if (_type == 1 || _type == 3)
                    _displayStatus.onApiResult(_result, _type);
                else if (_type == 2)
                    _displayStatus.deletedStatus(_result);
                _displayStatus = null;
            }
            if (_myclassifieds != null) {
                _myclassifieds.onApiResult(_result);
                _myclassifieds = null;
            }
            if (_classified != null) {
                _classified.onApiResult(_result, _type);
                _classified = null;
            }
            if (_changeEmail != null) {
                _changeEmail.onApiResult(_result);
                _changeEmail = null;
            }
            if (_changePassword != null) {
                _changePassword.onApiResult(_result);
                _changePassword = null;
            }
            if (_manageAds != null) {
                ManageAds.getInstance().onApiResult(_result);
            }
            if (_registerInstance != null) {
                _registerInstance.onApiResult(_result, _type);
                _registerInstance = null;
            }
            if (_deleteProfileInstance != null) {
                _deleteProfileInstance.onApiResult(_result);
                _deleteProfileInstance = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
