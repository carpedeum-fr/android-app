package com.questions;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.carpedeum.Today;

/**
 * Created by Guillaume on 17/08/13.
 */

public class SubFolder extends Activity {
    private int _resId = -1;
    private SQLiteDatabase _database = null;
    private LinearLayout _mainLayout = null;
    private int _drawableArrowID = -1;
    private Cursor _currentCursor = null;

    private TextView _tabTV[] = null;
    private Boolean _tabTvBool[] = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _database = Today.getInstance().getDB();
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_questions_subfolder);
        _mainLayout = (LinearLayout)findViewById(R.id.linearLayout_subfolder_com_questions_subfolder);

        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null && _database != null) {
            String parent = extras.getString("parent");
            TextView title2TV = (TextView)findViewById(R.id.textView_title_com_questions_subfolder);
            title2TV.setText(extras.getString("parentTitle"));
            _resId = extras.getInt("resId");
            setLayoutTheme();

            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.INVISIBLE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.QuestionsDeFoi));

            displayQuestions(parent);
            displayShadow();
            displaySubFolders(parent);
        }
    }

    private void displayShadow() {
        ImageView shadowIV = new ImageView(this);
        shadowIV.setImageResource(R.drawable.footer_shadow);
        _mainLayout.addView(shadowIV, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void displayQuestions(String parent) {
        String sql = "SELECT * FROM QUESTIONS_TEXT where PARENT='" + parent + "'";
        _currentCursor = _database.rawQuery(sql, null);
        _tabTV = new TextView[_currentCursor.getCount()];
        _tabTvBool = new Boolean[_currentCursor.getCount()];
        for (int i = 0; i < _currentCursor.getCount(); ++i) {
            _currentCursor.moveToPosition(i);
            LinearLayout questionLL = new LinearLayout(this);
            questionLL.setOrientation(LinearLayout.HORIZONTAL);
            TextView questionTV = new TextView(this);
            questionTV.setText(_currentCursor.getString(_currentCursor.getColumnIndex("QUESTION")));
            questionTV.setTextColor(getResources().getColor(R.color.black));
            questionTV.setTypeface(null, Typeface.BOLD);
            questionTV.setTextSize(15);

            final int j = i;
            questionTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayAnswer(j);
                }
            });
            questionLL.addView(questionTV, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams questionsLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            questionsLP.setMargins(15, 10, 15, 10);
            _mainLayout.addView(questionLL, questionsLP);
            createAnswer(_currentCursor.getString(_currentCursor.getColumnIndex("ANSWER")), i);
        }
    }

    private void createAnswer(String answer, int index) {
        TextView answerTV = new TextView(this);
        answerTV.setText(Html.fromHtml(answer));
        answerTV.setTextColor(getResources().getColor(R.color.black));
        answerTV.setTextSize(15);
        LinearLayout.LayoutParams answerLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        answerLP.setMargins(15, 0, 15, 20);
        _mainLayout.addView(answerTV, answerLP);
        _tabTV[index] = answerTV;
        _tabTvBool[index] = false;
        _tabTV[index].setVisibility(View.GONE);
    }

    private void displayAnswer(int index) {
        if (_tabTvBool[index]) {
            _tabTV[index].setVisibility(View.GONE);
            _tabTvBool[index] = false;
        }
        else {
            _tabTV[index].setVisibility(View.VISIBLE);
            _tabTvBool[index] = true;
        }
    }

    private void displaySubFolders(String parent) {
        Log.d("Parent::", parent);
        String sql = "SELECT * FROM QUESTIONS_FOLDERS where PARENT='" + parent + "'";
        _currentCursor = _database.rawQuery(sql, null);
        for (int i = 0; i < _currentCursor.getCount(); ++i) {
            _currentCursor.moveToPosition(i);
            Log.d("Questions::", _currentCursor.getString(_currentCursor.getColumnIndex("TITLE")));
            LinearLayout folderLL = new LinearLayout(this);
            folderLL.setOrientation(LinearLayout.HORIZONTAL);
            TextView folderTV = new TextView(this);
            folderTV.setText(_currentCursor.getString(_currentCursor.getColumnIndex("TITLE")));
            folderTV.setTextColor(getResources().getColor(R.color.black));
            folderTV.setGravity(Gravity.CENTER_VERTICAL);
            folderTV.setPadding(0, 10, 0, 10);
            folderTV.setTextSize(15);
            folderLL.addView(folderTV);
            final String parentS = _currentCursor.getString(_currentCursor.getColumnIndex("ID"));
            final String titleS = _currentCursor.getString(_currentCursor.getColumnIndex("TITLE"));
            folderLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SubFolder.this, SubFolder.class);
                    intent.putExtra("parent", parentS);
                    intent.putExtra("parentTitle", titleS);
                    intent.putExtra("resId", _resId);
                    startActivity(intent);
                }
            });
            LinearLayout.LayoutParams subFolderLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            subFolderLP.setMargins(15, 0, 15, 0);
            _mainLayout.addView(folderLL, subFolderLP);
            addSeparator();
        }
    }

    private void addSeparator() {
        View separatorV = new View(this);
        LinearLayout.LayoutParams separatorLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        separatorV.setBackgroundResource(R.color.gris80);
        _mainLayout.addView(separatorV, separatorLP);
    }

    private void setLayoutTheme() {
        if (_resId == 0) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _drawableArrowID = R.drawable.disclosure;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _drawableArrowID = R.drawable.disclosure_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _drawableArrowID = R.drawable.disclosure_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _drawableArrowID = R.drawable.disclosure_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _drawableArrowID = R.drawable.disclosure_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _drawableArrowID = R.drawable.disclosure_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _drawableArrowID = R.drawable.disclosure_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _drawableArrowID = R.drawable.disclosure_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _drawableArrowID = R.drawable.disclosure_silver;
        }
    }
}