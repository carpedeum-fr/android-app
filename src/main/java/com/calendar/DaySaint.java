package com.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.DownloadImages;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.carpedeum.Today;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DaySaint extends Activity {

    private SQLiteDatabase _database = null;
    private int _resId = -1;
    private Cursor _currentCursor = null;
    private LinearLayout _daysLL = null;
    private int _drawableArrowID = -1;
    String[] _monthsArray = null;
    private String TAG = "DaySaint";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _database = Today.getInstance().getDB();
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_day_saint);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        _daysLL = (LinearLayout)findViewById(R.id.linearLayout_days_com_calendar);
        _monthsArray = getResources().getStringArray(R.array.month);

        displaySaintDay();

        // Remplacement du logo CarpeDeum par le titre de l'activité
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.GONE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.Calendrier));

    }

    private void displaySaintDay() {

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(c.getTime());

        String dateTitle = new SimpleDateFormat("dd MMMM").format(c.getTime());

        TextView dateTV = (TextView)findViewById(R.id.textView_date_activity_day_saint);
        dateTV.setText(dateTitle);

        Log.d(TAG, date);

        String sql = "SELECT * FROM CALENDAR where DATE='" + date + "'";
        if (_database != null) {
            _currentCursor = _database.rawQuery(sql, null);
            if (_currentCursor != null) {
                if (_currentCursor.moveToFirst()) {

                    TextView textTV = (TextView)findViewById(R.id.textView_text_activity_day_saint);
                    textTV.setText(Html.fromHtml(_currentCursor.getString(_currentCursor.getColumnIndex("SAINT_TEXT"))));

                    ImageView imageView = (ImageView)findViewById(R.id.imageView_activity_day_saint);

                    String _picture = _currentCursor.getString(_currentCursor.getColumnIndex("SAINT_PIC"));

                    Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(_picture + getScreenWidth() / 2);
                    if (cachedImage == null) {
                        new DownloadImages(imageView, true, _picture + getScreenWidth() / 6).execute(Tools.MEDIAROOT + _picture, String.valueOf(getScreenWidth() / 2));
                    }
                    else {
                        imageView.setImageBitmap(cachedImage);

                    }
                    int width = getScreenWidth() / 2;
                    int height = (3 * width) / 4;
                    LinearLayout.LayoutParams GodVoiceLP = new LinearLayout.LayoutParams(width, height);
                    GodVoiceLP.gravity = Gravity.CENTER;
                    GodVoiceLP.setMargins(0, 10, 0, 10);
                    imageView.setLayoutParams(GodVoiceLP);

                }
            }
        }

    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
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

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(DaySaint.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    // TOOLS
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public int getScreenWidth() {
        int apiLevel = android.os.Build.VERSION.SDK_INT;

        Display display = getWindowManager().getDefaultDisplay();
        int width;
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
}
