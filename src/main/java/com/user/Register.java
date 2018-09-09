package com.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.carpedeum.ConditionsGenerales;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Guillaume on 24/08/13.
 * Inscription à CarpeDeum
 */

public class Register extends Activity {

    private int _resId = -1;
    private ProgressDialog _progressDialog = null;
    private String _email = null;
    private String _password = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_register);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        setLayoutOnClickListeners();
    }

    private void setLayoutOnClickListeners() {
        final Spinner genderSpinner = (Spinner)findViewById(R.id.spinner_gender_com_register);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        final CheckBox condCB = (CheckBox)findViewById(R.id.checkBox_allow_comment_com_prayers_create_prayer);
        condCB.setText(Html.fromHtml(getString(R.string.REGISTER_INPUT_ACCEPT_LABEL)));
        condCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent condGenIntent = new Intent(Register.this, ConditionsGenerales.class);
                condGenIntent.putExtra("resId", _resId);
                startActivity(condGenIntent);
            }
        });
        final DatePicker birthDatePicker = (DatePicker)findViewById(R.id.datePicker_birthdate_com_register);
        final EditText email1ED = (EditText)findViewById(R.id.editText_email_com_register);
        final EditText email2ED = (EditText)findViewById(R.id.editText_emailrepeat_com_register);
        final EditText passwd1ED = (EditText)findViewById(R.id.editText_password_com_register);
        final EditText passwd2ED = (EditText)findViewById(R.id.editText_passwordrepeat_com_register);
        final EditText firstNameED = (EditText)findViewById(R.id.editText_firstname_com_register);
        final EditText lastNameED = (EditText)findViewById(R.id.editText_sirname_com_register);
        Button sendBtn = (Button)findViewById(R.id.button_send_com_register);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean err = false;
                String error = "";
                if (email1ED.getText() == null || email1ED.getText().length() == 0 || email2ED.getText() == null || email2ED.getText().length() == 0
                        || !isEmailValid(email1ED.getText().toString()) || !isEmailValid(email2ED.getText().toString())) {
                    error = getString(R.string.ERR_EMAIL);
                    err = true;
                }
                if (passwd1ED.getText() == null || passwd1ED.getText().length() == 0 || passwd2ED.getText() == null || passwd2ED.getText().length() == 0) {
                    error = getString(R.string.ERR_PASSWORD_NOT_MATCH);
                    err = true;
                }
                if ((passwd1ED.getText() != null && passwd1ED.getText().length() < 5) || (passwd2ED.getText() != null && passwd2ED.getText().length() < 5)) {
                    error = getString(R.string.ERR_PASSWORD_TOO_SHORT);
                    err = true;
                }
                if (firstNameED.getText() == null || firstNameED.getText().length() == 0 || lastNameED.getText() == null || lastNameED.getText().length() == 0) {
                    error = getString(R.string.ERR_NAMES);
                    err = true;
                }
                if (!condCB.isChecked()) {
                    error = getString(R.string.ERR_CONDITIONS);
                    err = true;
                }
                if (!err) {
                    sendInscription(email1ED.getText().toString(), passwd1ED.getText().toString(), firstNameED.getText().toString(), lastNameED.getText().toString(),
                            genderSpinner.getSelectedItem().toString(), getDateFromPicker(birthDatePicker));
                }
                else {
                    displayError(error);
                }
            }
        });
    }

    private String getDateFromPicker(DatePicker birthDatePicker) {
        int month = birthDatePicker.getMonth() + 1;
        String monthS = "";
        if (month < 10)
            monthS = "0";
        monthS += String.valueOf(month);
        String day = "";
        if (birthDatePicker.getDayOfMonth() < 10)
            day += "0";
        day += birthDatePicker.getDayOfMonth();
        String date = birthDatePicker.getYear() + "-" + monthS + "-" + day;
        return date;
    }

    private void sendInscription(String email, String password, String firstname, String lastname, String gender, String date) {
        _progressDialog.show();
        _email = email;
        _password = password;
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        String url = Tools.API + Tools.CDACCOUNTSIGNUP;
        _args.add(new BasicNameValuePair("email", email));
        _args.add(new BasicNameValuePair("password", password));
        _args.add(new BasicNameValuePair("firstname", firstname));
        _args.add(new BasicNameValuePair("lastname", lastname));
        _args.add(new BasicNameValuePair("gender", gender));
        _args.add(new BasicNameValuePair("dob", date));
        _args.add(new BasicNameValuePair("accept", "1"));
        HttpApiCall apiCaller = new HttpApiCall(Register.this, _args, 1);
        apiCaller.execute(url);
    }

    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (type == 1) {
            if (resObj.has("err")) {
                displayError(resObj.getString("err"));
            }
            else {
                if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                    Login.getInstance().testLogin(_email, _password, this);
                }
            }
        }
    }

    public void onSuccessRegisterAndLogin() {
        if (Tools.CDDEBUG)
            Log.d("Register::onSuccessRegisterAndLogin::", "finishing activity");
        Intent myIntent = getIntent();
        setResult(RESULT_OK, myIntent);
        finish();
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
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
        Intent messagesIntent = new Intent(Register.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}