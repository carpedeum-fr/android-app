package com.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 24/08/13.
 * Login
 */

public class Login extends Activity implements ApiCaller {

    private int _resId = -1;
    private Handler _countHandler = new Handler();
    private static final Login _instance = new Login();
    private Register _registerInstance = null;
    private ProgressDialog _progressDialog = null;
    List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private String TAG = "Login";

    public static Intent _mPingService = null;

    public static Login getInstance() {
        return _instance;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_login);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        setButtonsOnClickListeners();
    }

    private void setButtonsOnClickListeners() {
        Button sendBtn = (Button)findViewById(R.id.button_send_com_login);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText emailET = (EditText)findViewById(R.id.editText_email_com_login);
                EditText mdpET = (EditText)findViewById(R.id.editText_password_com_login);
                testLogin(emailET.getText().toString(), mdpET.getText().toString(), null);
            }
        });
        final EditText passwordED = (EditText)findViewById(R.id.editText_password_com_login);
        passwordED.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND && passwordED.getText() != null && passwordED.getText().toString().length() > 0) {
                    EditText emailET = (EditText)findViewById(R.id.editText_email_com_login);
                    EditText mdpET = (EditText)findViewById(R.id.editText_password_com_login);
                    testLogin(emailET.getText().toString(), mdpET.getText().toString(), null);
                }
                return false;
            }
        });
    }

    /**
     * Appel de l'API pour s'identifier
     *
     * @param email : email
     * @param password : mot de passe
     * @param registerInstance : instance de l'objet register si l'on vient de s'inscrire
     */
    public void testLogin(String email, String password, Register registerInstance) {
        if (_progressDialog == null && registerInstance == null) {
            _progressDialog = new ProgressDialog(this);
            _progressDialog.setMessage(getString(R.string.ChargementEnCours));
            _progressDialog.setCancelable(false);
        }
        if (email != null && password != null && email.length() > 0 && password.length() > 0) {


            /**
             * Enregistrement de l'email et mdp pour éventuelle utilisation future
             */
            /*UserConnected.getInstance().set_password(password);
            UserConnected.getInstance().set_mail(email);
            */
            try {
                UserConnected.getInstance().saveCredentials(email, password);
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (Tools.CDDEBUG)
                Log.d("Login::testLogin::", email + "::" + password);
            if (_progressDialog != null)
                _progressDialog.show();
            _registerInstance = registerInstance;
            String url = Tools.API + Tools.CDACCOUNTLOGIN;
            _args.add(new BasicNameValuePair("u", email));
            _args.add(new BasicNameValuePair("p", password));
            HttpApiCall apiCaller = new HttpApiCall(Login.this, _args, 1);
            apiCaller.execute(url);
        }
        else {
            displayError(getString(R.string.LOGIN_EMAIL_MDP));
        }
    }

    private void displayError(String err) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(err);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    /**
     * L'utilisateur a réussi à se connecter à l'API
     *
     * @param loginObj
     */
    private void sucessLogin(JSONObject loginObj) {
        UserConnected.getInstance().setUserConnected(true);
        try {
            UserConnected.getInstance().set_uid(loginObj.getString("uid"));
            UserConnected.getInstance().set_sid(loginObj.getString("sid"));
            UserConnected.getInstance().setName(loginObj.getString("profilename"));
            UserConnected.getInstance().setPic(loginObj.getString("profilepic"));
            UserConnected.getInstance().set_jsonObj(loginObj.toString());
            try {
                UserConnected.getInstance().saveSession(loginObj.getString("uid"), loginObj.getString("sid"));
                UserConnected.getInstance().saveJsonObj(loginObj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (UserConnected.getInstance().IsUserConnected()) {
                editor.putString("jsonUser", UserConnected.getInstance().get_jsonObj());
                editor.putString("sid", UserConnected.getInstance().get_sid());
                editor.putString("user", "true");
                editor.putString("pass", UserConnected.getInstance().get_password());
                editor.putString("email", UserConnected.getInstance().get_mail());
            }
            editor.commit();
            */

            //TODO que faire si je viens de register ?
            /* Starting PING services */
            if (_registerInstance == null) {
                _mPingService = new Intent(this, PingService.class);
                startService(_mPingService);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (_registerInstance != null) {
            _registerInstance.onSuccessRegisterAndLogin();
        }
        else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", "ok");
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    public void logOut() {
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        String url = Tools.API + Tools.CDACCOUNTLOGOUT;
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        HttpApiCall apiCaller = new HttpApiCall(Login.this, _args, 2);
        apiCaller.execute(url);
        UserConnected.getInstance().setUserConnected(false);
        UserConnected.getInstance().disconnectUser();
        finish();
    }

    private void errorLogin(String err) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(err);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void setLayoutTheme() {
        if (_resId == 0) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
        }
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(Login.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        Log.d("Login::loginResult::", result + ", type = " + type);
        if (type == 1) {
            JSONObject loginObj = new JSONObject(result);
            if (loginObj.has("ok") && loginObj.getString("ok").equals("1")) {
                sucessLogin(loginObj);
            }
            else if (loginObj.has("err")) {
                errorLogin(loginObj.getString("err"));
            }
        }
        else if (type == 3) {
            JSONObject resObj = new JSONObject(result);
            if (resObj.has("num_unread"))
                UserConnected.getInstance().set_numUnreadMessages(resObj.getInt("num_unread"));
        }
    }
}