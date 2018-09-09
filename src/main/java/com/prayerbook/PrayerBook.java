package com.prayerbook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
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
import com.Network.DownloadImages;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.carpedeum.Today;
import com.questions.SubFolder;
import com.voice.ListenGodVoice;

import java.util.HashMap;

/**
 * Created by Guillaume on 13/08/13.
 * Carnet de prières
 */

public class PrayerBook extends Activity {

    private SQLiteDatabase _database = null;
    private LinearLayout _mainLayout = null;
    private int _resId = -1;
    private Cursor _currentCursor = null;
    private String _parent = null;
    private String _text = null;
    private String _parentTitle = null;
    private String TAG = "PrayerBook";

    private HashMap<String, Integer> _imagesSrc = null;

    private String datatable = "";
    private String type = "";

    //TODO: telecharger toutes les images en local pour ne pas avoir à les telecharger

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _database = Today.getInstance().getDB();
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_prayerbook);
        _mainLayout = (LinearLayout)findViewById(R.id.linearLayout_prayerbook_com_prayerbook);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        fillSrcsHashMap();
        if (extras != null) {
            _resId = extras.getInt("resId");
            if (extras.containsKey("parent")) {
                _parent = extras.getString("parent");
            }
            if (extras.containsKey("parentTitle")) {
                _parentTitle = extras.getString("parentTitle");
            }
            if (extras.containsKey("text")) {
                _text = extras.getString("text");
            }
            if (extras.containsKey("type")) {
                this.type = extras.getString("type");
                if (extras.getString("type").equals("prayerbook")) {
                    datatable = "PRAYERBOOK";
                }
                else if (extras.getString("type").equals("ordinaire")) {
                    datatable = "MASSBOOK";
                }
                else {
                    datatable = "MEDITATIONBOOK";
                }
            }
            setLayoutTheme();

            // Remplacement du logo CarpeDeum par l'en-tête de l'activité
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.INVISIBLE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);

            if (extras.getString("type").equals("medittext")) {
                titleTV.setText(getResources().getString(R.string.TextesMediter));
                TextView titlePageTV = (TextView)findViewById(R.id.textView_prayerTitle_com_prayerbook);
                titlePageTV.setText(getString(R.string.TextesMediter));
            }
            else if (extras.getString("type").equals("ordinaire")) {
                titleTV.setText(getResources().getString(R.string.Ordinaire));
                TextView titlePageTV = (TextView)findViewById(R.id.textView_prayerTitle_com_prayerbook);
                titlePageTV.setText(getString(R.string.Ordinaire));
            }
            else {
                titleTV.setText(getResources().getString(R.string.CarnetDePrieres));
            }

        }
        if (_database != null) {
            parsePrayers();
        }
    }

    private void fillSrcsHashMap() {
        _imagesSrc = new HashMap<String, Integer>();
        _imagesSrc.put("1", R.drawable.com_prayerbook_1);
        _imagesSrc.put("8", R.drawable.com_prayerbook_8);
        _imagesSrc.put("23", R.drawable.com_prayerbook_23);
        _imagesSrc.put("28", R.drawable.com_prayerbook_28);
        _imagesSrc.put("32", R.drawable.com_prayerbook_32);
        _imagesSrc.put("38", R.drawable.com_prayerbook_38);
        _imagesSrc.put("180", R.drawable.com_prayerbook_180);
        _imagesSrc.put("333", R.drawable.com_prayerbook_333);
        _imagesSrc.put("349", R.drawable.com_prayerbook_349);
        _imagesSrc.put("354", R.drawable.com_prayerbook_354);
        _imagesSrc.put("359", R.drawable.com_prayerbook_359);
        _imagesSrc.put("364", R.drawable.com_prayerbook_364);
        _imagesSrc.put("206", R.drawable.com_prayerbook_206);
        _imagesSrc.put("338", R.drawable.com_prayerbook_338);
        _imagesSrc.put("343", R.drawable.com_prayerbook_343);
        _imagesSrc.put("328", R.drawable.com_prayerbook_328);
        _imagesSrc.put("2", R.drawable.com_prayerbook_2);
        _imagesSrc.put("9", R.drawable.com_prayerbook_9);
        _imagesSrc.put("15", R.drawable.com_prayerbook_15);
        _imagesSrc.put("24", R.drawable.com_prayerbook_24);
        _imagesSrc.put("29", R.drawable.com_prayerbook_29);
        _imagesSrc.put("33", R.drawable.com_prayerbook_33);
        _imagesSrc.put("39", R.drawable.com_prayerbook_39);
        _imagesSrc.put("339", R.drawable.com_prayerbook_339);
        _imagesSrc.put("344", R.drawable.com_prayerbook_344);
        _imagesSrc.put("334", R.drawable.com_prayerbook_334);
        _imagesSrc.put("350", R.drawable.com_prayerbook_350);
        _imagesSrc.put("355", R.drawable.com_prayerbook_355);
        _imagesSrc.put("360", R.drawable.com_prayerbook_360);

        _imagesSrc.put("286", R.drawable.com_prayerbook_286);


    }

    private void parsePrayers() {
        if (_parentTitle != null) {
            TextView titleTV = (TextView)findViewById(R.id.textView_prayerTitle_com_prayerbook);
            LinearLayout.LayoutParams titleLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleTV.setLayoutParams(titleLP);
            titleTV.setText(_parentTitle);
        }
        if (_text != null) {
            TextView introTV = (TextView)findViewById(R.id.textView_prayerIntro_com_prayerbook);
            introTV.setText(Html.fromHtml(_text));
        }
        else {
            TextView introTV = (TextView)findViewById(R.id.textView_prayerIntro_com_prayerbook);
            introTV.setVisibility(View.GONE);
        }
        if (_parent == null) {
            String sql = "SELECT * FROM " + datatable + " where PARENT='0'";
            _currentCursor = _database.rawQuery(sql, null);
        }
        else {
            String sql = "SELECT * FROM " + datatable + " where PARENT='" + _parent + "'";
            _currentCursor = _database.rawQuery(sql, null);
        }
        displayPrayers();
    }

    private void displayPrayers() {
        for (int i = 0; i < _currentCursor.getCount(); ++i) {
            _currentCursor.moveToPosition(i);
            displayTitle();
        }
    }

    private void displayTitle() {
        //Log.d("Prayers::", _currentCursor.getString(_currentCursor.getColumnIndex("TITLE")));
        LinearLayout folderLL = new LinearLayout(this);
        folderLL.setOrientation(LinearLayout.HORIZONTAL);

        ImageView prayerIV = getPrayerIV(_currentCursor.getString(_currentCursor.getColumnIndex("IMAGE")), _currentCursor.getString(_currentCursor.getColumnIndex("ID")));
        if (prayerIV != null && !_currentCursor.getString(_currentCursor.getColumnIndex("IMAGE")).equals("")) {
            int size = getScreenWidth() / 6;
            if (size > Tools.MAX_IMAGE_SIZE)
                size = Tools.MAX_IMAGE_SIZE;
            LinearLayout.LayoutParams imageViewLP = new LinearLayout.LayoutParams(size, size);
            imageViewLP.setMargins(5, 5, 5, 5);
            folderLL.addView(prayerIV, imageViewLP);
        }

        TextView folderTV = new TextView(this);
        folderTV.setPadding(10, 10, 10, 10);
        folderTV.setText(_currentCursor.getString(_currentCursor.getColumnIndex("TITLE")));
        folderTV.setTextColor(getResources().getColor(R.color.black));
        folderTV.setGravity(Gravity.CENTER_VERTICAL);
        folderTV.setTextSize(16);
        folderLL.setGravity(Gravity.CENTER_VERTICAL);
        folderLL.addView(folderTV);

        final String parent = _currentCursor.getString(_currentCursor.getColumnIndex("ID"));
        final String title = _currentCursor.getString(_currentCursor.getColumnIndex("TITLE"));
        final String text = _currentCursor.getString(_currentCursor.getColumnIndex("TEXT"));
        final String image = _currentCursor.getString(_currentCursor.getColumnIndex("IMAGE"));

        final String audio = _currentCursor.getString(_currentCursor.getColumnIndex("AUDIO"));

        asChildren(parent);
        folderLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "audio : " + _currentCursor.getString(_currentCursor.getColumnIndex("AUDIO")));

                if (audio.length() > 0) {
                    Intent listenVoiceIntent = new Intent(PrayerBook.this, ListenGodVoice.class);
                    listenVoiceIntent.putExtra("audioURL", audio);
                    listenVoiceIntent.putExtra("imgURL", image);
                    startActivity(listenVoiceIntent);
                }
                else {
                    if (asChildren(parent)) {
                        Intent intent = new Intent(PrayerBook.this, PrayerBook.class);
                        intent.putExtra("parent", parent);
                        intent.putExtra("parentTitle", title);
                        if (text != null && text.length() > 0) {
                            intent.putExtra("text", text);
                        }
                        intent.putExtra("resId", _resId);
                        intent.putExtra("type", type);
                        startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(PrayerBook.this, Prayer.class);
                        intent.putExtra("prayerTitle", title);
                        intent.putExtra("prayer", text);
                        intent.putExtra("resId", _resId);
                        intent.putExtra("type", type);
                        if (image != null && image.length() > 0)
                            intent.putExtra("image", image);
                        startActivity(intent);
                    }
                }
            }
        });
        _mainLayout.addView(folderLL, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addSeparator();
    }

    private boolean asChildren(String parent) {
        String sql = "SELECT * FROM " + datatable + " where PARENT='" + parent + "'";
        Cursor cursor = _database.rawQuery(sql, null);
        if (cursor.getCount() > 0)
            return true;
        return false;
    }

    /**
     * Renvoie l'image d'une prière
     *
     * @param image
     * @param id
     * @return
     */
    private ImageView getPrayerIV(String image, String id) {
        ImageView questionIV = new ImageView(this);
        if (_imagesSrc.containsKey(id)) {
            questionIV.setImageResource(_imagesSrc.get(id));
            questionIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return questionIV;
        }
        else {
            if (internetConnectionOk()) {
                int size = getScreenWidth() / 6;
                if (size > Tools.MAX_IMAGE_SIZE)
                    size = Tools.MAX_IMAGE_SIZE;
                Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(image + size);
                if (cachedImage == null) {
                    new DownloadImages(questionIV, true, image + size).execute(Tools.MEDIAROOT + image, String.valueOf(size));
                }
                else {
                    questionIV.setImageBitmap(cachedImage);
                    questionIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                return questionIV;
            }
        }
        return null;
    }

    private void addSeparator() {
        View separatorV = new View(this);
        LinearLayout.LayoutParams separatorLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        separatorV.setBackgroundResource(R.color.gris80);
        _mainLayout.addView(separatorV, separatorLP);
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
        else {
            width = display.getWidth();
        }
        return width;
    }

    private boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
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