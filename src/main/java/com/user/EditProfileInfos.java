package com.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.Network.HttpApiCall;
import com.Tools.LogInFile;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 27/08/13.
 * Edit your Profile
 */

public class EditProfileInfos extends Activity {

    private int _resId = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_profile_edit_profile);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.MonProfil));
            setUserInfos();
        }
    }

    private void setUserInfos() {
        final EditText firstName = (EditText)findViewById(R.id.editText_firstname_com_profile_edit_profile);
        firstName.setText(UserConnected.getInstance().get_firstName());
        final EditText lastName = (EditText)findViewById(R.id.editText_sirname_com_profile_edit_profile);
        lastName.setText(UserConnected.getInstance().get_lastName());
        final Spinner genderSpinner = (Spinner)findViewById(R.id.spinner_gender_com_profile_edit_profile);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        if (UserConnected.getInstance().get_gender() == UserConnected.GENDER_M)
            genderSpinner.setSelection(0);
        else
            genderSpinner.setSelection(1);
        final DatePicker birthDatePicker = (DatePicker)findViewById(R.id.datePicker_birthdate_com_profile_edit_profile);
        String[] birthDate = UserConnected.getInstance().get_birthDate().split("-");
        birthDatePicker.updateDate(Integer.parseInt(birthDate[0]), Integer.parseInt(birthDate[1]) - 1, Integer.parseInt(birthDate[2]));
        final EditText selfPortraitET = (EditText)findViewById(R.id.editText_selfportrait_com_profile_edit_profile);
        selfPortraitET.setText(UserConnected.getInstance().get_selfportrait());
        Button saveBtn = (Button)findViewById(R.id.button_save_com_profile_edit_profile);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                checkChangesAfterSaving(firstName.getText().toString(), lastName.getText().toString(), genderSpinner.getSelectedItem().toString(), date, selfPortraitET.getText().toString());
            }
        });
    }

    private void checkChangesAfterSaving(String firstName, String lastName, String gender, String dateOfBirth, String selfPortrait) {
        if (Tools.CDDEBUG) {
            LogInFile.getInstance().WriteLog("EditProfileInfos::checkChangesAfterSaving::" + firstName + "," + lastName + "," + gender + "," + dateOfBirth + "," + selfPortrait, true);
            Log.d("EditProfileInfos::checkChangesAfterSaving::", UserConnected.getInstance().get_birthDate());
        }
        int genderType = UserConnected.GENDER_M;
        if (gender.equals("Femme"))
            genderType = UserConnected.GENDER_F;
        if (!firstName.equals(UserConnected.getInstance().get_firstName()) || !lastName.equals(UserConnected.getInstance().get_lastName()) || genderType != UserConnected.getInstance().get_gender() ||
                !dateOfBirth.equals(UserConnected.getInstance().get_birthDate()) || !selfPortrait.equals(UserConnected.getInstance().get_selfportrait()))
        {
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            if (!firstName.equals(UserConnected.getInstance().get_firstName())) {
                _args.add(new BasicNameValuePair("first_name", firstName));
                _args.add(new BasicNameValuePair("firstname", firstName));
            }
            if (!lastName.equals(UserConnected.getInstance().get_lastName())) {
                _args.add(new BasicNameValuePair("last_name", lastName));
                _args.add(new BasicNameValuePair("lastname", lastName));
            }
            if (genderType != UserConnected.getInstance().get_gender()) {
                if (genderType == UserConnected.GENDER_F)
                    _args.add(new BasicNameValuePair("gender", "F"));
                else
                    _args.add(new BasicNameValuePair("gender", "M"));
            }
            if (!dateOfBirth.equals(UserConnected.getInstance().get_birthDate()))
                _args.add(new BasicNameValuePair("date_birth", dateOfBirth));
            if (!selfPortrait.equals(UserConnected.getInstance().get_selfportrait()))
                _args.add(new BasicNameValuePair("selfportrait", selfPortrait));
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("id", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            HttpApiCall apiCaller = new HttpApiCall(this, _args);
            String url = Tools.API + Tools.CDPROFILESET;
            apiCaller.execute(url);
        }
        else {
            Intent myIntent = getIntent();
            myIntent.putExtra("changes", "no");
            setResult(RESULT_OK, myIntent);
            finish();
        }
    }

    public void setResultCode(String result) {
        if (result != null) {
            if (Tools.CDDEBUG)
                Log.d("EditProfileInfos::setResultCode::result::", result);
            try {
                JSONObject resultObj = new JSONObject(result);
                if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                    Intent myIntent = getIntent();
                    myIntent.putExtra("changes", "yes");
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
                else {
                    //TODO AFFICHER MESSAGE ERREUR
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
}