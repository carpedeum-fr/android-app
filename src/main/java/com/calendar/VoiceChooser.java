package com.calendar;

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
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Network.PoolRequetes;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.ads.ManageAds;
import com.i2heaven.carpedeum.R;
import com.carpedeum.Today;
import com.voice.GodVoice;
import com.voice.ManVoice;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Guillaume on 09/08/13.
 * Dans le calendrier: choix entre Parole de Dieu et Parole d'homme
 */

public class VoiceChooser extends Activity implements ApiCaller {

    private SQLiteDatabase _database = null;
    private int _resId = -1;
    private String _date = null;
    private Cursor _currentCursor = null;
    private String _godVoicePicURL = "null";
    private String _manVoiceElements = null;
    private int _drawableArrowID = -1;
    private final String TAG = "VoiceChooser";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _database = Today.getInstance().getDB();

        // Affichage du layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_calendar_voice_chooser);

        // On récupère les paramètres de l'intent
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _date = extras.getString("date");
            Log.d(TAG, "date: " + _date);
            setLayoutTheme();
        }

        // Affichage du titre de l'activité
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.INVISIBLE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.Calendrier));


        if (_date != null) {
            setCurrentCursor();
            setTitle();
            assert extras != null;
            _godVoicePicURL = extras.getString("gospel_pic");
            getGodVoiceImages();
            downloadManVoice();
            setOnclickListeners();
        }

        try {
            ManageAds.displayAdds(this, Tools.getScreenWidth(this), false, R.id.textView_DateEtFete_com_calendar_voice_chooser, Tools.getScreenWidth(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Téléchargement de la parole d'homme
     */
    private void downloadManVoice() {
        /*
        ArrayList<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("date", _date));
        HttpApiCall _apiCaller = new HttpApiCall(this, _args, 1);
        _apiCaller.execute(Tools.API + Tools.CDMANVOICE);
        */

        ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
        args.add(new BasicNameValuePair("date", _date));
        PoolRequetes.getInstance().ajouterNouvelleRequete(this, args, Tools.API + Tools.CDMANVOICE, 1);
    }

    private void setOnclickListeners() {
        ImageView godArrowIV = (ImageView)findViewById(R.id.imageView_arrow_com_voice_chooser);
        ImageView manArrowIV = (ImageView)findViewById(R.id.imageView_arrow2_com_voice_chooser);

        godArrowIV.setImageResource(_drawableArrowID);
        manArrowIV.setImageResource(_drawableArrowID);

        ImageView godIV = (ImageView)findViewById(R.id.imageView_godVoice_com_calendar_voice_chooser);
        ImageView manIV = (ImageView)findViewById(R.id.imageView_manVoice_com_calendar_voice_chooser);

        TextView godTV = (TextView)findViewById(R.id.textView_godVoice_com_calendar_voice_chooser);
        TextView manTV = (TextView)findViewById(R.id.textView_manVoice_com_calendar_voice_chooser);

        if (!internetConnectionOk()) {
            manIV.setVisibility(View.GONE);
            manTV.setVisibility(View.GONE);
            manArrowIV.setVisibility(View.GONE);
        }
        else {
            manIV.setVisibility(View.VISIBLE);
            manTV.setVisibility(View.VISIBLE);
            manArrowIV.setVisibility(View.VISIBLE);


            manIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startManVoiceIntent();
                }
            });
            manTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startManVoiceIntent();
                }
            });
            manArrowIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startManVoiceIntent();
                }
            });

        }

        godIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGodVoiceIntent();
            }
        });
        godTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGodVoiceIntent();
            }
        });
        godArrowIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGodVoiceIntent();
            }
        });
    }

    /**
     * Démarrer l'intent sur ManVoice
     */
    private void startManVoiceIntent() {
        Intent manVoiceIntent = new Intent(VoiceChooser.this, ManVoice.class);
        manVoiceIntent.putExtra("resId", _resId);
        manVoiceIntent.putExtra("date", _date);
        if (_manVoiceElements != null) {
            manVoiceIntent.putExtra("elements", _manVoiceElements);
        }
        startActivityForResult(manVoiceIntent, 1);
    }

    private void startGodVoiceIntent() {
        Intent GodVoiceIntent = new Intent(VoiceChooser.this, GodVoice.class);
        GodVoiceIntent.putExtra("resId", _resId);
        GodVoiceIntent.putExtra("GodVoiceSrc", _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_SOURCE")));
        GodVoiceIntent.putExtra("GodVoiceAudioURL", Tools.MEDIAROOT + _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_AUDIO")));
        GodVoiceIntent.putExtra("GodVoicePic", _godVoicePicURL);
        GodVoiceIntent.putExtra("GodVoice", _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL")));
        GodVoiceIntent.putExtra("GodVoiceCredits", _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_CREDITS")));
        GodVoiceIntent.putExtra("MedTextGodVoice", _currentCursor.getString(_currentCursor.getColumnIndex("GOSPELCOMMENTARY")));
        GodVoiceIntent.putExtra("MedTextCreditsGodVoice", _currentCursor.getString(_currentCursor.getColumnIndex("GOSPELCOMMENTARY_CREDITS")));
        GodVoiceIntent.putExtra("Oration", _currentCursor.getString(_currentCursor.getColumnIndex("ORATION")));
        GodVoiceIntent.putExtra("OrationCredits", _currentCursor.getString(_currentCursor.getColumnIndex("ORATION_CREDITS")));
        GodVoiceIntent.putExtra("date", _date);
        GodVoiceIntent.putExtra("readingTitle", _currentCursor.getString(_currentCursor.getColumnIndex("READING_TITLE")));
        GodVoiceIntent.putExtra("readingSubTitle", _currentCursor.getString(_currentCursor.getColumnIndex("READING_SUBTITLE")));
        GodVoiceIntent.putExtra("reading", _currentCursor.getString(_currentCursor.getColumnIndex("READING")));
        GodVoiceIntent.putExtra("readingCredits", _currentCursor.getString(_currentCursor.getColumnIndex("READING_CREDITS")));
        GodVoiceIntent.putExtra("reading2Title", _currentCursor.getString(_currentCursor.getColumnIndex("READING2_TITLE")));
        GodVoiceIntent.putExtra("reading2SubTitle", _currentCursor.getString(_currentCursor.getColumnIndex("READING2_SUBTITLE")));
        GodVoiceIntent.putExtra("reading2", _currentCursor.getString(_currentCursor.getColumnIndex("READING2")));
        GodVoiceIntent.putExtra("reading2Credits", _currentCursor.getString(_currentCursor.getColumnIndex("READING2_CREDITS")));
        GodVoiceIntent.putExtra("psalmTitle", _currentCursor.getString(_currentCursor.getColumnIndex("PSALM_TITLE")));
        GodVoiceIntent.putExtra("psalmSubTitle", _currentCursor.getString(_currentCursor.getColumnIndex("PSALM_SUBTITLE")));
        GodVoiceIntent.putExtra("psalm", _currentCursor.getString(_currentCursor.getColumnIndex("PSALM")));
        GodVoiceIntent.putExtra("psalmCredits", _currentCursor.getString(_currentCursor.getColumnIndex("PSALM_CREDITS")));
        startActivityForResult(GodVoiceIntent, 2);
    }

    /**
     * Télécharger l'image de God Voice
     */
    private void getGodVoiceImages() {
        if (internetConnectionOk()) {
            int size = Tools.STD_W / 5;
            ImageView godVoiceIV = (ImageView)findViewById(R.id.imageView_godVoice_com_calendar_voice_chooser);
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(_godVoicePicURL + size);
            if (cachedImage == null) {
                new DownloadImages(godVoiceIV, true, _godVoicePicURL + size).execute(Tools.MEDIAROOT + _godVoicePicURL, String.valueOf(size));
            }
            else {
                godVoiceIV.setImageBitmap(cachedImage);
                godVoiceIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
            godVoiceIV.setLayoutParams(lp);
        }


    }

    /**
     * Télécharger l'image de ManVoice
     * @param result
     */
    private void getManVoice(String result) throws Exception {

        int size = Tools.STD_W / 5;

        JSONObject resObj = new JSONObject(result);
        ImageView manVoiceIV = (ImageView)findViewById(R.id.imageView_manVoice_com_calendar_voice_chooser);
        Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(resObj.getString("image") + size);
        if (cachedImage == null) {
            new DownloadImages(manVoiceIV, true, resObj.getString("image") + size).execute(Tools.MEDIAROOT + resObj.getString("image"), String.valueOf(size));
        }
        else {
            manVoiceIV.setImageBitmap(cachedImage);
            manVoiceIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
        lp.addRule(RelativeLayout.BELOW, R.id.view_separator_com_calendar_voice_chooser);
        manVoiceIV.setLayoutParams(lp);
    }

    private void setCurrentCursor() {
        String sql = "SELECT * FROM CALENDAR where DATE='" + _date + "'";
        if (_database != null) {
            _currentCursor = _database.rawQuery(sql, null);
            if (_currentCursor != null) {
                _currentCursor.moveToFirst();
            }
        }
    }

    private void setTitle() {
        TextView dateEtFeteTv = (TextView)findViewById(R.id.textView_DateEtFete_com_calendar_voice_chooser);
        dateEtFeteTv.setText(Html.fromHtml("<font color='#FFFFFF'><strong>" + getDateFromString().toUpperCase() + "</strong></font><br/><font color='#404040'>" + _currentCursor.getString(_currentCursor.getColumnIndex("HAPPY_NAME_DAY")) + "</font>"));
    }

    private String getDateFromString() {
        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat myFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");
        String date = null;
        try {
             date = myFormat.format(fromUser.parse(_date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
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

    private boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (type == 1) {
            _manVoiceElements = result;
            getManVoice(result);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data.hasExtra("startActivity")) {
            Log.d(TAG, "TOTA");
            Intent myIntent = getIntent();
            myIntent.putExtra("startActivity", "profile");
            setResult(RESULT_OK, myIntent);
            finish();
        }
    }
}