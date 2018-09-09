package com.questions;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.DownloadImageTask;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.carpedeum.Today;

/**
 * Created by Guillaume on 13/08/13.
 * Question de Foi
 */

public class Questions extends Activity {

    private int _resId = -1;
    private SQLiteDatabase _database = null;
    private LinearLayout _mainLayout = null;
    private int _drawableArrowID = -1;
    private Cursor _currentCursor = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _database = Today.getInstance().getDB();
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_questions);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        //TODO La barre de recherches
        _mainLayout = (LinearLayout)findViewById(R.id.linearLayout_questions_com_questions);
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.INVISIBLE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.QuestionsDeFoi));
        if (_database != null) {
            setMainQuestions();
        }
    }

    private void setMainQuestions() {
        String sql = "SELECT * FROM QUESTIONS_FOLDERS where PARENT='0'";
        _currentCursor = _database.rawQuery(sql, null);
        if (Tools.CDDEBUG) Log.d("CurrentCursor::", String.valueOf(_currentCursor.getCount()));
        for (int i = 0; i < _currentCursor.getCount(); ++i) {
            _currentCursor.moveToPosition(i);
            if (Tools.CDDEBUG) Log.d("Questions::", _currentCursor.getString(_currentCursor.getColumnIndex("TITLE")));
            LinearLayout folderLL = new LinearLayout(this);
            folderLL.setOrientation(LinearLayout.HORIZONTAL);
            ImageView questionIV = getQuestionsIV(_currentCursor.getString(_currentCursor.getColumnIndex("ID")));
            if (questionIV != null) {
                LinearLayout.LayoutParams imageViewLP = new LinearLayout.LayoutParams(getScreenWidth() / 7, getScreenWidth() / 7);
                imageViewLP.setMargins(5, 5, 5, 5);
                folderLL.addView(questionIV, imageViewLP);
            }
            LinearLayout.LayoutParams folderTVLLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60);
            folderTVLLP.setMargins(5, 0, 0, 0);
            TextView folderTV = new TextView(this);
            folderTV.setText(_currentCursor.getString(_currentCursor.getColumnIndex("TITLE")));
            folderTV.setTextColor(getResources().getColor(R.color.black));
            folderTV.setTextSize(15);
            folderLL.addView(folderTV, folderTVLLP);


            ImageView arrowIV = new ImageView(this);
            LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
            arrowLP.gravity = Gravity.CENTER_VERTICAL;
            arrowIV.setImageResource(_drawableArrowID);
            folderLL.addView(arrowIV, arrowLP);

            final String parent = _currentCursor.getString(_currentCursor.getColumnIndex("ID"));
            final String title = _currentCursor.getString(_currentCursor.getColumnIndex("TITLE"));
            folderLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Questions.this, SubFolder.class);
                    intent.putExtra("parent", parent);
                    intent.putExtra("parentTitle", title);
                    intent.putExtra("resId", _resId);
                    startActivity(intent);
                }
            });
            _mainLayout.addView(folderLL, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getScreenWidth() / 7));
            addSeparator();
        }
    }

    private ImageView getQuestionsIV(String id) {
        /*if (internetConnectionOk()) {
            ImageView questionIV = new ImageView(this);
            new DownloadImageTask(questionIV, true).execute(Tools.MEDIAROOT + image);
            return questionIV;
        }
        return null;*/
        ImageView questionIV = new ImageView(this);
        if (id.equals("1"))
            questionIV.setImageDrawable(getResources().getDrawable(R.drawable.com_questions_profession_de_foi));
        else if (id.equals("2"))
            questionIV.setImageDrawable(getResources().getDrawable(R.drawable.com_question_sacrements));
        else if (id.equals("3"))
            questionIV.setImageDrawable(getResources().getDrawable(R.drawable.com_questions_vie_christ));
        else if (id.equals("4"))
            questionIV.setImageDrawable(getResources().getDrawable(R.drawable.com_questions_vie_chretienne));
        else if (id.equals("53"))
            questionIV.setImageDrawable(getResources().getDrawable(R.drawable.com_questions_fetes_chretiennes));
        else if (id.equals("67"))
            questionIV.setImageDrawable(getResources().getDrawable(R.drawable.com_questions_credo));
        questionIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return questionIV;
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

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public int getScreenWidth() {
        int apiLevel = android.os.Build.VERSION.SDK_INT;

        Display display = getWindowManager().getDefaultDisplay();
        int width = 0;
        if (apiLevel >= 13) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }
        else {
            width = display.getWidth();
        }
        return width;
    }

    private boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}