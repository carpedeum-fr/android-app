package com.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 02/11/13.
 * Supprimer son profil
 */

public class DeleteProfile extends Activity {

    private int _resId = -1;
    private ProgressDialog _progressDialog = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_profile_delete_profile);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            setDeleteBtnOnClickListener();
        }
    }

    private void setDeleteBtnOnClickListener() {
        Button deleteBtn = (Button)findViewById(R.id.button_ok_com_delete_profile);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText passwdED = (EditText)findViewById(R.id.editText_password_com_delete_profile);
                if (passwdED.getText() == null || passwdED.getText().length() == 0) {
                    displayError(getString(R.string.ERR_EMAIL));
                }
                else {
                    _progressDialog.show();
                    List<NameValuePair> _args = new ArrayList<NameValuePair>();
                    String url = Tools.API + Tools.CDACCOUNTCLOSE;
                    _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                    _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                    _args.add(new BasicNameValuePair("password", passwdED.getText().toString()));
                    HttpApiCall apiCaller = new HttpApiCall(DeleteProfile.this, _args);
                    apiCaller.execute(url);
                }
            }
        });
    }

    public void onApiResult(String result) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("err")) {
            displayError(resObj.getString("err"));
        }
        else {
            if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                Login.getInstance().logOut();
                Intent myIntent = getIntent();
                setResult(RESULT_OK, myIntent);
                finish();
            }
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

    private void setLayoutTheme() {
        if (_resId == 0)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        else if (_resId == 1)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
        else if (_resId == 2)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
        else if (_resId == 3)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
        else if (_resId == 4)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
        else if (_resId == 5)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
        else if (_resId == 6)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
        else if (_resId == 7)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
        else if (_resId == 8)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(DeleteProfile.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}