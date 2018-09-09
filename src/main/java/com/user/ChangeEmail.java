package com.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 03/10/13.
 * Changement d'email
 */

public class ChangeEmail extends Activity {

    private int _resId = -1;
    public ProgressDialog _progressDialog = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_user_change_email);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(Html.fromHtml(getString(R.string.LOGIN_INPUT_EMAIL_PLACEHOLDER)));
            setButtonsListeners();
        }
    }

    private void setButtonsListeners() {
        Button saveBtn = (Button)findViewById(R.id.button_save_com_user_change_email);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailED = (EditText)findViewById(R.id.editText_email_com_user_change_email);
                EditText emailRED = (EditText)findViewById(R.id.editText_emailrepeat_com_user_change_email);
                EditText passwdED = (EditText)findViewById(R.id.editText_password_com_user_change_email);
                if (emailED.getText() != null && emailRED.getText() != null && passwdED.getText() != null) {
                    if (emailED.getText().toString().equals(emailRED.getText().toString())) {
                        callApiToChangeEmail(emailED.getText().toString(), passwdED.getText().toString());
                    }
                    else {
                        //TODO message erreur pas le meme email !
                    }
                }
                else {
                    //TODO message erreur entrez du texte !
                }
            }
        });
    }

    private void callApiToChangeEmail(String email, String password) {
        if (internetConnectionOk()) {
            _progressDialog.show();
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("newemail", email));
            _args.add(new BasicNameValuePair("password", password));
            HttpApiCall apiCaller = new HttpApiCall(this, _args);
            String url = Tools.API + Tools.CDACCOUNTCHANGEEMAIL;
            apiCaller.execute(url);
        }
        else {
            //TODO Message erreur pas de connection internet
        }
    }

    public void onApiResult(String result) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();
        if (result != null) {
            JSONObject resultObj = new JSONObject(result);
            if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                LinearLayout emailLL = (LinearLayout)findViewById(R.id.linearLayout_email_com_user_change_email);
                LinearLayout emailRLL = (LinearLayout)findViewById(R.id.linearLayout_emailR_com_user_change_email);
                LinearLayout passwdLL = (LinearLayout)findViewById(R.id.linearLayout_password_com_user_change_email);
                emailLL.setVisibility(View.GONE);
                emailRLL.setVisibility(View.GONE);
                passwdLL.setVisibility(View.GONE);
                TextView infosTV = (TextView)findViewById(R.id.textView_infos_com_user_change_email);
                infosTV.setText(getString(R.string.ACCOUNT_CHANGEMAIL_OK));
                Button saveBtn = (Button)findViewById(R.id.button_save_com_user_change_email);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            else {
                //TODO message erreur
            }
        }
    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
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

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}