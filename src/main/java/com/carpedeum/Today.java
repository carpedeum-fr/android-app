package com.carpedeum;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.Network.ApiCaller;
import com.Network.DownloadImages;
import com.Tools.ImageCache;
import com.Tools.LogInFile;
import com.i2heaven.carpedeum.R;
import com.database.DataBaseAdapter;
import com.database.DataBaseHelper;
import com.database.DataBaseWrapper;
import com.Tools.Tools;
import com.Network.HttpApiCall;
import com.user.UserConnected;
import com.videonews.NewsFolder;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by COCH on 15/07/13.
 * Tools for Today funcs
 */

public class Today implements ApiCaller {

    private MainActivity _mainActivityInstance = null;
    private static Cursor _currentCursor = null;
    private JSONObject _homeElements = null;
    private JSONObject _godVoiceApiElements = null;
    private SQLiteDatabase _db = null;
    public static Today _instance = null;
    public static boolean bddLoaded = false;
    private String TAG = "Today";

    public static Bitmap _manVoiceBitmap = null;
    public static Bitmap _godVoiceBitmap = null;

    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;

    public Today(MainActivity mainActivity) {
        _mainActivityInstance = mainActivity;
        _instance = this;
    }

    public void loadBDD() {
        if (!bddLoaded) {
            if (Tools.CDDEBUG) {
                LogInFile.getInstance().WriteLog("Today::loadBDD", true);
                Log.d("Today::", "loadBDD");
            }
            DataBaseHelper myDbHelper = new DataBaseHelper(_mainActivityInstance);
            try {
                LogInFile.getInstance().WriteLog("Today::loadBDD::createDataBase", true);
                myDbHelper.createDataBase();
                LogInFile.getInstance().WriteLog("Today::loadBDD::datebasecreated", true);
            } catch (IOException ioe) {
                LogInFile.getInstance().WriteLog("Today::loadBDD::Unable to create database", true);
                throw new Error("Unable to create database");
            }
            try {
                LogInFile.getInstance().WriteLog("Today::loadBDD::openDataBase", true);
                myDbHelper.openDataBase();
                _db = myDbHelper.getDB();
                LogInFile.getInstance().WriteLog("Today::loadBDD::datebaseopened" + _db.isOpen(), true);
                Log.d("_db.isOpen() ", String.valueOf(_db.isOpen()));
                if (_db.isOpen())
                    bddLoaded = true;
            } catch (SQLException sqle){
                LogInFile.getInstance().WriteLog("Today::loadBDD::error opening", true);
                sqle.printStackTrace();
            }
        }
        else {
            if (Tools.CDDEBUG)
                Log.d(TAG, "Bdd already loaded");
        }
        loadCurrentDay();
    }

    public void loadCurrentDay() {
        String sql = "SELECT * FROM CALENDAR where DATE='" + getFormatedForDBCurrentDate() + "'";
        if (_db != null) {
            _currentCursor = _db.rawQuery(sql, null);
            _currentCursor.moveToFirst();
            bddLoaded = true;
        }
    }

    public static Today getInstance() {
        return _instance;
    }

    public SQLiteDatabase getDB() {
        return _db;
    }

    @SuppressWarnings("ConstantConditions")
    public int getTodayThemeHeader() {
        try {
            if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("BLUE")) {
                return 1;
            }
            else if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("GOLD")) {
                return 2;
            }
            else if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("GREEN")) {
                return 3;
            }
            else if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("MAUVE")) {
                return 4;
            }
            else if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("ORANGE")) {
                return 5;
            }
            else if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("PURPLE")) {
                return 6;
            }
            else if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("RED")) {
                return 7;
            }
            else if (_currentCursor.getString(_currentCursor.getColumnIndex("COLOR")).equals("WHITE")) {
                return 8;
            }
        } catch (java.lang.NullPointerException e) {
            return 0;
        }
        return 0;
    }

    public String getCelebrationName() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("CELEBRATION"));
    }

    public String getCurrentFete() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("HAPPY_NAME_DAY"));
    }

    public static String getCurrentDate() {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("EEEE dd MMMM yyyy").format(today);
        if (todayDate != null && todayDate.length() > 0)
            return todayDate;
        return "";
    }

    public static String getFormatedForDBCurrentDate() {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(today);
        if (todayDate != null && todayDate.length() > 0)
            return todayDate;
        return "";
    }

    public String getManVoice() {
        try {
            return _homeElements.getString("thought_text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void displayGodVoice() {
        if (_currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_PIC")).equals("")) {
            if (_args == null)
                _args = new ArrayList<NameValuePair>();
            _args.clear();
            _args.add(new BasicNameValuePair("date", Today.getFormatedForDBCurrentDate()));
            _apiCaller = new HttpApiCall(this, _args, 2);
            _apiCaller.execute(Tools.API + Tools.CDCALENDAR + Tools.CDGETGOSPEL);
        }
        else {
            String imgUrl = Tools.MEDIAROOT + _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_PIC"));
            _mainActivityInstance.addGodVoice(imgUrl);
        }
    }

    public void getGodVoiceFromApi(String result) {
        try {
            if (result != null) {
                _godVoiceApiElements = new JSONObject(result);
                if (!_godVoiceApiElements.has("image") || _godVoiceApiElements.getString("image").equals(""))
                    _mainActivityInstance.addGodVoice("");
                else
                    _mainActivityInstance.addGodVoice(Tools.MEDIAROOT + _godVoiceApiElements.getString("image"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayOnlinePeople() {
        try {
            int nbVisitor = Integer.valueOf(_homeElements.getString("num_online_visitors"));
            int nbMember = Integer.valueOf(_homeElements.getString("num_online_users"));
            String visitors;
            if (nbVisitor > 1)
                visitors = _mainActivityInstance.getString(R.string.visiteurs);
            else
                visitors = _mainActivityInstance.getString(R.string.visiteur);
            String members;
            if (nbMember > 1)
                members = _mainActivityInstance.getString(R.string.membres);
            else
                members = _mainActivityInstance.getString(R.string.membre);
            String text = nbVisitor + " utilisateurs (dont " + nbMember + " inscrits) en ligne<br/><u>Pourquoi s'inscrire ?</u>";


            //String text = nbMember + " " + members + " (dont " + nbVisitor + " " + visitors + " " + _mainActivityInstance.getString(R.string.enLigne) + " >";
            _mainActivityInstance.displayOnlinePeople(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadHomeElements() {
        if (_args == null)
            _args = new ArrayList<NameValuePair>();
        _args.clear();
        if (UserConnected.getInstance().IsUserConnected()) {
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        }
        _apiCaller = new HttpApiCall(this, _args, 1);
        _apiCaller.execute(Tools.API + Tools.HOME);
    }

    public void displayHomeVideos() {
        _mainActivityInstance.removeAllVideos();
        try {
            JSONArray homeVideosInfos = new JSONArray(_homeElements.getString("list_news"));
            for (int i = 0; i < homeVideosInfos.length(); ++i) {
                JSONObject homeVideoInfos = homeVideosInfos.getJSONObject(i);
                if (homeVideoInfos.getString("has_video").equals("1")) {
                    String imgUrl = Tools.MEDIAROOT + homeVideoInfos.getString("image");
                    String text = homeVideoInfos.getString("title");
                    _mainActivityInstance.addHomeVideo(imgUrl, text, homeVideoInfos.getString("id"));
                }
            }
            _mainActivityInstance.addLinkForKiosque();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (type == 1) {
            _homeElements = new JSONObject(result);
            //if (Tools.CDDEBUG)
                //Log.d("Today::homeElementsApiResult", _homeElements.toString());
            Tools.MEDIAROOT = _homeElements.getString("media_root");
            displayHomeFolder();
            displayHomeVideos();
            displayOnlinePeople();
            fillUserInfos();
        }
        else if (type == 2) {
            getGodVoiceFromApi(result);
        }
    }

    private void fillUserInfos() throws Exception {
        if (UserConnected.getInstance().IsUserConnected()) {
            if (_homeElements.has("account_candles"))
                UserConnected.getInstance().set_num_candles(_homeElements.getInt("account_candles"));
            if (_homeElements.has("count_visits"))
                UserConnected.getInstance().set_num_visited(_homeElements.getInt("count_visits"));
        }
    }

    private void displayHomeFolder() throws Exception {
        final LinearLayout folderNewsLL = (LinearLayout)_mainActivityInstance.findViewById(R.id.linearLayout_newsfolder_com_carpedeum_aujourdhui);
        if (folderNewsLL != null) {
            folderNewsLL.removeAllViews();
            if (!_homeElements.getString("list_newsfolders").equals("") && !_homeElements.getString("list_newsfolders").equals("null")) {
                folderNewsLL.setVisibility(View.VISIBLE);
                final JSONArray folderNewsObject = new JSONArray(_homeElements.getString("list_newsfolders"));
                int size = Tools.STD_W / 5;
                //if (size > Tools.MAX_IMAGE_SIZE)
                   // size = Tools.MAX_IMAGE_SIZE;
                ImageView videoIV = displayHomeFolderImg(folderNewsObject.getJSONObject(0).getString("image"), size);
                folderNewsLL.addView(videoIV, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (3 * size) / 4));
                LinearLayout.LayoutParams imageVideoLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                imageVideoLP.setMargins(5, 5, 5, 5);
                folderNewsLL.setBackgroundColor(_mainActivityInstance.getResources().getColor(R.color.white));
                folderNewsLL.setLayoutParams(imageVideoLP);
                folderNewsLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent newsFolderIntent = new Intent(_mainActivityInstance, NewsFolder.class);
                        newsFolderIntent.putExtra("resId", _mainActivityInstance.getResId());
                        try {
                            newsFolderIntent.putExtra("id", folderNewsObject.getJSONObject(0).getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        _mainActivityInstance.startActivity(newsFolderIntent);
                    }
                });
            }
            else {
                folderNewsLL.setVisibility(View.GONE);
            }
        }
    }

    private ImageView displayHomeFolderImg(String imgUrl, int size) {
        ImageView videoImageIV = new ImageView(_mainActivityInstance);
        Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgUrl + size);
        if (cachedImage == null) {
            new DownloadImages(videoImageIV, true, imgUrl + size).execute(Tools.MEDIAROOT + imgUrl, String.valueOf(size));
        }
        else {
            videoImageIV.setImageBitmap(cachedImage);
            videoImageIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        return videoImageIV;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public int getScreenWidth() {
        int apiLevel = android.os.Build.VERSION.SDK_INT;
        Display display = _mainActivityInstance.getWindowManager().getDefaultDisplay();
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

    public String getGodVoice() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL"));
    }

    public String getGodVoiceCredits() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_CREDITS"));
    }

    public String getGodVoicePic() {
        if (!_currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_PIC")).equals(""))
            return _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_PIC"));
        else {
            try {
                if (_godVoiceApiElements != null)
                    return _godVoiceApiElements.getString("image");
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getGodVoiceSrc() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_SOURCE"));
    }

    public String getGodVoiceAudioURL() {
        return Tools.MEDIAROOT + _currentCursor.getString(_currentCursor.getColumnIndex("GOSPEL_AUDIO"));
    }

    public String getGodVoiceMedText() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("GOSPELCOMMENTARY"));
    }

    public String getGodVoiceMedTextCredits() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("GOSPELCOMMENTARY_CREDITS"));
    }

    public String getGodVoiceOrationText() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("ORATION"));
    }

    public String getGodVoiceOrationCredits() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("ORATION_CREDITS"));
    }

    public String getGodVoiceReadingTitle() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING_TITLE"));
    }

    public String getGodVoiceReading2Title() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING2_TITLE"));
    }

    public String getGodVoicePsalmTitle() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("PSALM_TITLE"));
    }

    public String getGodVoiceReadingSubTitle() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING_SUBTITLE"));
    }

    public String getGodVoiceReading2SubTitle() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING2_SUBTITLE"));
    }

    public String getGodVoicePsalmSubTitle() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("PSALM_SUBTITLE"));
    }

    public String getGodVoiceReading() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING"));
    }

    public String getGodVoiceReading2() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING2"));
    }

    public String getGodVoicePsalm() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("PSALM"));
    }

    public String getGodVoiceReadingCredits() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING_CREDITS"));
    }

    public String getGodVoiceReading2Credits() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("READING2_CREDITS"));
    }

    public String getGodVoicePsalmCredits() {
        return _currentCursor.getString(_currentCursor.getColumnIndex("PSALM_CREDITS"));
    }
}
