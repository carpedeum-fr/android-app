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
import com.messages.DisplayMessages;

/**
 * Created by Guillaume on 19/08/13.
 * Display a prayer
 */

public class Prayer extends Activity {

    private int _resId = -1;
    private String _prayerTitle = null;
    private String _prayer = null;
    private String _imageURL = null;
    private LinearLayout _mainLayout = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_prayerbook_prayer);
        _mainLayout = (LinearLayout)findViewById(R.id.linearLayout_prayer_com_prayerbook_prayer);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _prayerTitle = extras.getString("prayerTitle");
            _prayer = extras.getString("prayer");
            if (extras.containsKey("image"))
                _imageURL = extras.getString("image");
            if (Tools.CDDEBUG)
                Log.d("Prayer::", _prayerTitle + "::text::" + _prayer);
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.INVISIBLE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);


            if (extras.getString("type").equals("medittext")) {
                titleTV.setText(getResources().getString(R.string.TextesMediter));
            }
            else if (extras.getString("type").equals("ordinaire")) {
                titleTV.setText(getResources().getString(R.string.Ordinaire));
            }
            else {
                titleTV.setText(getResources().getString(R.string.CarnetDePrieres));
            }
            displayPrayer();
        }
    }

    private void displayPrayer() {
        if (_imageURL != null) {
            ImageView prayerIV = (ImageView)findViewById(R.id.imageView_prayerImage_com_prayerbook_prayer);

            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(_imageURL + Tools.STD_W / 2);
            if (cachedImage == null) {
                new DownloadImages(prayerIV, true, _imageURL + Tools.STD_W / 2).execute(Tools.MEDIAROOT + _imageURL, String.valueOf(Tools.STD_W / 2));
            }
            else {
                prayerIV.setImageBitmap(cachedImage);
                prayerIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            LinearLayout.LayoutParams imageLP = new LinearLayout.LayoutParams(getScreenWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT);
            imageLP.gravity = Gravity.CENTER;
            prayerIV.setLayoutParams(imageLP);
        }
        TextView titleTV = (TextView)findViewById(R.id.textView_prayername_com_prayerbook_prayer);
        titleTV.setText(_prayerTitle);
        TextView prayerTV = (TextView)findViewById(R.id.textView_prayer_com_prayerbook_prayer);
        prayerTV.setText(Html.fromHtml(_prayer));
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
        Intent messagesIntent = new Intent(Prayer.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}