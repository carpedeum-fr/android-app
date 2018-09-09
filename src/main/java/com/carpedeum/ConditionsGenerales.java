package com.carpedeum;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.widget.TextView;

import com.i2heaven.carpedeum.R;

/**
 * Created by Guillaume on 23/08/13.
 */
public class ConditionsGenerales extends Activity {

    private int _resId = -1;
    private SQLiteDatabase _database = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_conditions_generales);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        _database = Today.getInstance().getDB();
        if (_database != null) {
            displayConditions();
        }
    }

    private void displayConditions() {
        String sql = "SELECT * FROM TEXT where NAME='terms'";
        Cursor cursor = _database.rawQuery(sql, null);
        if (cursor != null) {
            cursor.moveToFirst();
            TextView titleTV = (TextView)findViewById(R.id.textView_title_com_conditions_generales);
            titleTV.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex("TITLE"))));
            TextView textTV = (TextView)findViewById(R.id.textView_text_com_conditions_generales);
            textTV.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex("TEXT"))));
            textTV.setMovementMethod(LinkMovementMethod.getInstance());
        }
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
}