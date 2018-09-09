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

public class ChangePassword extends Activity {

    private int _resId = -1;
    public ProgressDialog _progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_user_change_password);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(Html.fromHtml(getString(R.string.LOGIN_INPUT_PASSWORD_PLACEHOLDER)));
            setButtonsListeners();
        }
    }

    private void setButtonsListeners() {
        Button saveBtn = (Button)findViewById(R.id.button_save_com_user_change_password);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText oldED = (EditText)findViewById(R.id.editText_oldpasswd_com_user_change_password);
                EditText newED = (EditText)findViewById(R.id.editText_newpasswd_com_user_change_password);
                EditText newrED = (EditText)findViewById(R.id.editText_newpasswdr_com_user_change_password);
                if (oldED.getText() != null && newED.getText() != null && newrED.getText() != null) {
                    if (newED.getText().toString().equals(newrED.getText().toString())) {
                        callApiToChangeEmail(oldED.getText().toString(), newED.getText().toString());
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

    private void callApiToChangeEmail(String oldpassword, String newpassword) {
        if (internetConnectionOk()) {
            _progressDialog.show();
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("oldpassword", oldpassword));
            _args.add(new BasicNameValuePair("newpassword", newpassword));
            HttpApiCall apiCaller = new HttpApiCall(this, _args);
            String url = Tools.API + Tools.CDACCOUNTCHANGEPASSWORD;
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
                LinearLayout emailLL = (LinearLayout)findViewById(R.id.linearLayout_old_com_user_change_password);
                LinearLayout emailRLL = (LinearLayout)findViewById(R.id.linearLayout_new_com_user_change_password);
                LinearLayout passwdLL = (LinearLayout)findViewById(R.id.linearLayout_newr_com_user_change_password);
                emailLL.setVisibility(View.GONE);
                emailRLL.setVisibility(View.GONE);
                passwdLL.setVisibility(View.GONE);
                TextView infosTV = (TextView)findViewById(R.id.textView_infos_com_user_change_password);
                infosTV.setText(getString(R.string.ACCOUNT_CHANGEPASSWORD_OK));
                Button saveBtn = (Button)findViewById(R.id.button_save_com_user_change_password);
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
