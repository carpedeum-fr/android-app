package com.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.PoolRequetes;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.ads.ManageAds;
import com.i2heaven.carpedeum.R;
import com.carpedeum.Today;
import com.messages.DisplayMessages;
import com.user.UserConnected;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Created by Guillaume on 05/08/13.
 * Calendar
 */

public class Calendar extends Activity implements ScrollViewListener {

    private SQLiteDatabase _database = null;
    private int _resId = -1;
    private Cursor _currentCursor = null;
    private LinearLayout _daysLL = null;
    private int _drawableArrowID = -1;
    String[] _monthsArray = null;
    private String TAG = "Calendar";
    private ObservableScrollView _scrollView = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _database = Today.getInstance().getDB();
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_calendar);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        _daysLL = (LinearLayout)findViewById(R.id.linearLayout_days_com_calendar);
        _monthsArray = getResources().getStringArray(R.array.month);

        // Téléchargement des jours
        displayDays(null, null);

        // Remplacement du logo CarpeDeum par le titre de l'activité
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.GONE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.Calendrier));


        try {
            ManageAds.displayAdds(this, getScreenWidth(), true, R.id.headershadow, getScreenWidth());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean testIfDateExist(String month, String year) {
        String sql = "SELECT * FROM CALENDAR where DATE='" + year + '-' + month + '-' + "01" + "'";
        if (_database != null) {
            _currentCursor = _database.rawQuery(sql, null);
            return _currentCursor != null && _currentCursor.moveToFirst();
        }
        return false;
    }

    private void checkIfPrevNextMonths(String monthToDisplay, String yearToDisplay) {
        if (monthToDisplay == null)
            monthToDisplay = getCurrentMonth();
        if (yearToDisplay == null)
            yearToDisplay = getCurrentYear();
        int countYear = 0;
        String prevMonth = "";
        String temp = String.valueOf(Integer.parseInt(monthToDisplay) - 1);
        if (Integer.parseInt(temp) == 0) {
            temp = "12";
            countYear = -1;
        }
        if (Integer.parseInt(temp) < 10)
            prevMonth = "0";
        prevMonth += temp;
        final String prevYear = String.valueOf(Integer.parseInt(yearToDisplay) + countYear);
        countYear = 0;
        String nextMonth = "";
        temp = String.valueOf(Integer.parseInt(monthToDisplay) + 1);
        if (Integer.parseInt(temp) == 13) {
            temp = "1";
            countYear = 1;
        }
        if (Integer.parseInt(temp) < 10)
            nextMonth = "0";
        nextMonth += temp;
        final String nextYear = String.valueOf(Integer.parseInt(yearToDisplay) + countYear);
        if (testIfDateExist(prevMonth, prevYear)) {
            ImageView prevIV = (ImageView)findViewById(R.id.imageView_prev_com_calendar);
            prevIV.setVisibility(View.VISIBLE);
            final String finalPrevMonth = prevMonth;
            prevIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayDays(finalPrevMonth, prevYear);
                }
            });
        }
        else {
            ImageView prevIV = (ImageView)findViewById(R.id.imageView_prev_com_calendar);
            prevIV.setVisibility(View.GONE);
        }
        if (testIfDateExist(nextMonth, nextYear)) {
            ImageView nextIV = (ImageView)findViewById(R.id.imageView_next_com_calendar);
            nextIV.setVisibility(View.VISIBLE);
            final String finalNextMonth = nextMonth;
            nextIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayDays(finalNextMonth, nextYear);
                }
            });
        }
        else {
            ImageView nextIV = (ImageView)findViewById(R.id.imageView_next_com_calendar);
            nextIV.setVisibility(View.GONE);
        }
    }

    private void displayHeader(String monthToDisplay, String yearToDisplay) {
        TextView headerTV = (TextView)findViewById(R.id.textView_header_com_calendar);
        if (monthToDisplay == null || yearToDisplay == null)
            headerTV.setText(getCurrentMonthAndYear());
        else {
            //TODO format en good
            headerTV.setText(_monthsArray[Integer.parseInt(monthToDisplay) - 1] + " " + yearToDisplay);
        }
    }

    private void displayDays(String monthToDisplay, String yearToDisplay) {
        checkIfPrevNextMonths(monthToDisplay, yearToDisplay);
        displayHeader(monthToDisplay, yearToDisplay);
        String year = yearToDisplay;
        if (yearToDisplay == null)
            year = getCurrentYear();
        String month = monthToDisplay;
        if (monthToDisplay == null)
             month = getCurrentMonth();
        _daysLL.removeAllViews();

        int curIdx = 0;

        for (int i = 1; i <= 31; ++i) {
            String day = "";
            if (i < 10) {
                day = "0";
            }
            day += String.valueOf(i);
            String dateRequested = year + '-' + month + '-' + day;

            if (dateRequested.equals(getFormatedForDBCurrentDate())) {
                curIdx = i;
            }

            addOneDay(dateRequested);
            addSeparator();
        }
        scrollToCurrentDay(curIdx);
    }

    /**
     * Scroll au jour courant
     * curIdx : index du jour dans le mois (1-31)
     */
    private void scrollToCurrentDay(int curIdx) {

        //hauteur d'une image
        int size = getScreenWidth() / 6;
        if (size > Tools.MAX_IMAGE_SIZE)
            size = Tools.MAX_IMAGE_SIZE;

        final int scrollY = (size * curIdx) - 200;

        final ScrollView scrollV = (ScrollView)findViewById(R.id.scrollView_days_com_calendar);
        ViewTreeObserver vto = scrollV.getViewTreeObserver();
        assert vto != null;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                scrollV.scrollTo(0, scrollY);
            }
        });
    }

    private void addSeparator() {
        View separatorV = new View(this);
        LinearLayout.LayoutParams separatorLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        separatorV.setBackgroundResource(R.color.gris80);
        _daysLL.addView(separatorV, separatorLP);
    }

    /**
     * Ajout d'une ligne, correspondant à une journée
     *
     * @param date
     */
    private void addOneDay(String date) {
        String sql = "SELECT * FROM CALENDAR where DATE='" + date + "'";
        if (_database != null) {
            _currentCursor = _database.rawQuery(sql, null);
            if (_currentCursor != null) {
                if (_currentCursor.moveToFirst()) {
                    LinearLayout onDayLL = new LinearLayout(this);
                    onDayLL.setOrientation(LinearLayout.HORIZONTAL);


                    // Téléchargement d'une image - l'image de la parole d'homme
                    ImageView leftImageIV = getImage(_currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_PIC")));
                    if (leftImageIV != null) {
                        leftImageIV.setPadding(0, 0, 5, 0);
                        onDayLL.addView(leftImageIV);
                    }


                    TextView contentTV = new TextView(this);
                    String content = _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_SOURCE"));
                    content += "<br/>" + _currentCursor.getString(_currentCursor.getColumnIndex("CELEBRATION"));
                    contentTV.setText(Html.fromHtml(content));
                    contentTV.setTextSize(12);
                    contentTV.setTextColor(getResources().getColor(R.color.black));
                    contentTV.setPadding(10, 0, 0, 0);
                    contentTV.setGravity(Gravity.CENTER_VERTICAL);
                    ImageView arrowIV = new ImageView(this);
                    LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
                    arrowLP.gravity = Gravity.CENTER_VERTICAL;
                    arrowIV.setImageResource(_drawableArrowID);
                    if (date.equals(getFormatedForDBCurrentDate())) {
                        onDayLL.setBackgroundColor(getResources().getColor(R.color.yellowCalendar));
                        arrowIV.setBackgroundColor(getResources().getColor(R.color.yellowCalendar));
                    }
                    onDayLL.addView(contentTV, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 65));
                    onDayLL.addView(arrowIV, arrowLP);

                    // Listener d'un click sur la ligne
                    addClickListener(onDayLL, date, _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_PIC")));
                    _daysLL.addView(onDayLL);
                }
            }
        }
    }

    private void isVisibleOnScreen(ImageView imageView) {
        ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView_days_com_calendar);

        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (imageView.getLocalVisibleRect(scrollBounds)) {
            Log.d(TAG, "VISIBLE");
        } else {
            Log.d(TAG, "NOT VISIBLE");
        }
    }

    /**
     * Gestion de l'évènement lorsque l'on clique sur la ligne
     *
     * @param v
     * @param date
     * @param pic
     */
    private void addClickListener(View v, final String date, final String pic) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent voiceChooserIntent = new Intent(Calendar.this, VoiceChooser.class);
                voiceChooserIntent.putExtra("resId", _resId);
                voiceChooserIntent.putExtra("date", date);
                voiceChooserIntent.putExtra("gospel_pic", pic);
                startActivityForResult(voiceChooserIntent, 40);
            }
        });
    }

    /**
     * Téléchargement de l'image
     *
     * @param imgUrl
     * @return
     */
    private ImageView getImage(String imgUrl) {


        ImageView profileIV = new ImageView(this);
        int size = Tools.STD_W / 6;

        PoolRequetes.getInstance().ajouterNouvelleRequeteImage(profileIV, false, imgUrl, size);

        /*

        if (size > Tools.MAX_IMAGE_SIZE) {
            size = Tools.MAX_IMAGE_SIZE;
        }
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        if (imgUrl != null && imgUrl.length() > 0 && internetConnectionOk()) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgUrl + size);
            if (cachedImage == null) {
                new DownloadImages(profileIV, false, imgUrl + size).execute(Tools.MEDIAROOT + imgUrl, String.valueOf(size));
            }
            else {
                profileIV.setImageBitmap(cachedImage);
            }
            profileIV.setScaleType(ImageView.ScaleType.CENTER);
            profileIV.setMaxWidth(size);
            profileIV.setMaxHeight(size);
            return profileIV;
        }
        */
        profileIV.setScaleType(ImageView.ScaleType.CENTER);
        profileIV.setMaxWidth(size);
        profileIV.setMaxHeight(size);
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        profileIV.setImageResource(R.drawable.com_godvoice_default);
        profileIV.setMaxWidth(size);
        profileIV.setMaxHeight(size);
        return profileIV;
    }

    // TOOLS
    private String getFormatedForDBCurrentDate() {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(today);
        if (todayDate != null && todayDate.length() > 0)
            return todayDate;
        return "";
    }

    private String getCurrentMonthAndYear() {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("MMMM yyyy").format(today);
        if (todayDate != null && todayDate.length() > 0)
            return todayDate;
        return "";
    }

    private String getCurrentMonth() {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("MM").format(today);
        if (todayDate != null && todayDate.length() > 0)
            return todayDate;
        return "";
    }

    private String getCurrentYear() {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("yyyy").format(today);
        if (todayDate != null && todayDate.length() > 0)
            return todayDate;
        return "";
    }

    // LAYOUT
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
        else
            width = display.getWidth();
        return width;
    }

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(Calendar.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data.hasExtra("startActivity")) {
            Intent myIntent = getIntent();
            myIntent.putExtra("startActivity", "profile");
            setResult(RESULT_OK, myIntent);
            finish();
        }
    }

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        //Log.d(TAG, "y: " + y);
    }
}