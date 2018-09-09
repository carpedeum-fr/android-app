package com.status;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 14/09/13.
 * Creer une nouvelle humeur
 */

public class CreateStatus extends Activity {

    private ProgressDialog _homeProgressDialog = null;
    private int _resId = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_status_create_status);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            if (imageLogoIV != null)
                imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            if (titleTV != null) {
                titleTV.setVisibility(View.VISIBLE);
                titleTV.setText(getResources().getString(R.string.VIEW_TITLE_STATUS));
            }
        }
        setButtonsOnClickListeners();
    }

    private void setButtonsOnClickListeners() {
        Button publishBtn = (Button)findViewById(R.id.button_publish_com_status_create_status);
        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConnectionOk()) {
                    callApiForAddingStatus();
                }
                else {
                    //TODO Message erreur !
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void callApiForAddingStatus() {
        EditText textED = (EditText)findViewById(R.id.editText_status_com_status_create_status);

        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        if (textED != null && textED.getText() != null && textED.getText().toString() != null)
            _args.add(new BasicNameValuePair("text", textED.getText().toString()));
        //TODO add media
        _homeProgressDialog = new ProgressDialog(this);
        _homeProgressDialog.setMessage(getString(R.string.ChargementEnCours));
        _homeProgressDialog.show();
        _args.add(new BasicNameValuePair("media[]", ""));
        HttpApiCall apiCaller = new HttpApiCall(this, _args);
        String url = Tools.API + Tools.CDSTATUSADD;
        apiCaller.execute(url);
    }

    public void onApiResult(String result) {
        if (_homeProgressDialog != null)
            _homeProgressDialog.cancel();
        if (result != null && !result.equals("")) {
            try {
                if (Tools.CDDEBUG)
                    Log.d("CreateStatus::onApiResult::result::", result);
                JSONObject resObj = new JSONObject(result);
                if (resObj.getString("ok").equals("1")) {
                    Intent myIntent = getIntent();
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
                else {
                    // TODO message erreur
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            // TODO message erreur
        }
    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1)
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

    private boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(CreateStatus.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}