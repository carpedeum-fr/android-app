package com.carpedeum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Network.ImageTaskCaller;
import com.Network.PoolRequetes;
import com.Tools.ImageCache;
import com.Tools.LogInFile;
import com.Tools.ReadUserFile;
import com.Tools.Tools;
import com.ads.ManageAds;
import com.calendar.Calendar;
import com.calendar.DaySaint;
import com.i2heaven.carpedeum.R;
import com.geomesse.Geomesse;
import com.messages.DisplayMessages;
import com.prayerbook.PrayerBook;
import com.questions.Questions;
import com.together.Together;
import com.user.Login;
import com.user.PingService;
import com.user.Profile;
import com.user.Register;
import com.user.UserConnected;
import com.videonews.Kiosque;
import com.videonews.TestVideo;
import com.videonews.VideoNews;
import com.videonews.youtube.FullscreenDemoActivity;
import com.voice.GodVoice;
import com.voice.ManVoice;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ApiCaller, ImageTaskCaller {

    private int _currentLayout = AUJOURDHUI;
    public static MainActivity _context;
    public static int AUJOURDHUI = 0;
    public static int MONPROFL = 1;
    public static int ENSEMBLE = 2;
    public ProgressDialog _homeProgressDialog = null;
    private Today _todayElem = null;
    private String _todayDate = null;
    private int _resId = -1;
    private LinearLayout _homeVideosLinearLayout = null;
    private int LOGIN = 1;
    private Profile _profile = null;
    private Together _together = null;
    private int _drawableArrowID = -1;
    private int _calendarLeftId = -1;
    private boolean _changes = false;
    private JSONObject _manVoice = null;
    private String TAG = "MainActivity";
    private DownloadImageTask _downloadImageTask = null;
    private String _jsonUser = null;
    private String _sid = null;
    public boolean _reload = true;


    public int getResId() { return _resId; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_carpedeum_aujourdhui);
        _homeProgressDialog = new ProgressDialog(this);
        _homeProgressDialog.setMessage(getString(R.string.ChargementEnCours));
        _context = this;

        if (Tools.CDDEBUG)
            Log.d(TAG, "onCreate !");
    }

    //TODO time out
    // ALL

    private void resumeLayout() {
        LinearLayout firstElmensLayout = (LinearLayout)findViewById(R.id.linearLayout_first_content_com_carpedeum_aujourdhui);
        LinearLayout voicesLL = (LinearLayout)findViewById(R.id.linearLayout_voices_com_carpedeum_aujourdhui);
        if (firstElmensLayout != null) {

            double inches = getScreeInches();
            if (inches <= Tools.STD_INCHES) {

                Tools.STD_W = getScreenWidth();
                Tools.STD_H = getScreenHeight();

                // Horizontal
                if (getScreenOrientation() == 1) {

                    firstElmensLayout.getLayoutParams().width = getScreenHeight();
                    voicesLL.getLayoutParams().height = ((firstElmensLayout.getLayoutParams().width / 2) * 3) / 4;
                }
            }
            else if ((inches > Tools.STD_INCHES) && (inches < (Tools.STD_INCHES * 2))) {

                double ratio = getScreeInches() / Tools.STD_INCHES;

                int newW = (int)((double)getScreenWidth() / ratio);
                int newH = (int)((double)getScreenHeight() / ratio);

                Tools.STD_W = newW;
                Tools.STD_H = newH;

                if (getScreenOrientation() == 1) {
                    firstElmensLayout.getLayoutParams().width = newH;
                    voicesLL.getLayoutParams().height = ((firstElmensLayout.getLayoutParams().width / 2) * 3) / 4;
                }
                else {
                    firstElmensLayout.getLayoutParams().width = newW;
                    voicesLL.getLayoutParams().height = ((firstElmensLayout.getLayoutParams().width / 2) * 3) / 4;
                }

            }
            else {

                int newW = getScreenWidth() / 3;
                int newH = getScreenHeight() / 3;

                Tools.STD_W = newW;
                Tools.STD_H = newH;

                if (getScreenOrientation() == 1) {
                    firstElmensLayout.getLayoutParams().width = newH;
                    voicesLL.getLayoutParams().height = ((firstElmensLayout.getLayoutParams().width / 2) * 3) / 4;
                }
                else {
                    firstElmensLayout.getLayoutParams().width = newW;
                    voicesLL.getLayoutParams().height = ((firstElmensLayout.getLayoutParams().width / 2) * 3) / 4;
                }

            }

            /*
            if (getScreenOrientation() == 1) {
                firstElmensLayout.getLayoutParams().width = getScreenHeight() / 2;
                voicesLL.getLayoutParams().height = ((firstElmensLayout.getLayoutParams().width / 2) * 3) / 4;
            }*/
        }
        _changes = false;
        if (_todayDate != null) {
            String date = Today.getCurrentDate();
            if (!date.equals(_todayDate)) {
                if (_todayElem != null) {
                    _todayElem.loadCurrentDay();
                    _todayDate = Today.getCurrentDate();
                    _changes = true;
                }
            }
        }
        if (!_changes && _todayDate != null)
            _reload = false;
        setTodayLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PoolRequetes.getInstance().start();

        Log.d("MainActivity", "onResume");

        if (Tools.CDDEBUG) {
            Log.d("MainActivity::", "onResume ! + UserConnected ? " + UserConnected.getInstance().IsUserConnected());
            LogInFile.getInstance().WriteLog("MainActivity::onResume ! + UserConnected ? " + UserConnected.getInstance().IsUserConnected(), true);

        }
        if (!UserConnected.getInstance().IsUserConnected()) {

            String uid;
            String sid;
            String email;
            String passwd;


            try {
                uid = ReadUserFile.getInstance().getUID();
                sid = ReadUserFile.getInstance().getSID();
                String jsonObj = ReadUserFile.getInstance().getJsonObj();
                _jsonUser = jsonObj;
                _sid = sid;

                LogInFile.getInstance().WriteLog("OnResume, UID: " + uid + " - sid: " + sid + " - json : " + jsonObj, true);
                Log.d(TAG, "OnResume, UID: " + uid + " - sid: " + sid + " - json : " + jsonObj);

                loginFromUserFile(uid, sid);

            } catch (Exception e) {
                LogInFile.getInstance().WriteLog("OnResume never connected or error !", true);
                LogInFile.getInstance().WriteLog(e.getMessage(), true);
                Log.e(TAG, "OnResume never connected or error !" + e.getMessage());
                e.printStackTrace();
            }



            /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPreferences.contains("jsonUser")) {
                _jsonUser = sharedPreferences.getString("jsonUser", "jsonUser");
                Log.d(TAG, "onResume::jsonUser::" + _jsonUser);
                Log.d(TAG, "Password::" + sharedPreferences.getString("pass", "pass"));
                _sid = sharedPreferences.getString("sid", "sid");

                UserConnected.getInstance().set_password(sharedPreferences.getString("pass", "pass"));

                if (_jsonUser != null) {
                    try {
                        loggedin();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }*/
        }
        ManageAds.getInstance().init();
        if (_currentLayout == AUJOURDHUI) {

            betaMessage();

            loadBdd();
            resumeLayout();


        }
        else if (_currentLayout == MONPROFL && _profile != null) {
            _profile.setButtonsOnClickListeners();
            _profile.getMyProfileFromAPI();
        }
        else if (_currentLayout == ENSEMBLE && _together != null) {
            _together.displayTogetherInfos();
            getCandlesFromApi();
        }




    }


    /**
     * Affichage du message béta et du sytème d'envoie de log
     */
    private void betaMessage() {

        TextView betaTV = (TextView)findViewById(R.id.textView_betaalert_com_carpedeum_aujourdhui);
        betaTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"android@carpedeum.fr"});
                i.putExtra(Intent.EXTRA_SUBJECT, "CarpeDeum Android - Signaler un dysfonctionnement");
                i.putExtra(Intent.EXTRA_TEXT, "Merci de détailler ici le problème rencontré");
                i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + LogInFile.getInstance()._file.getAbsolutePath()));

                startActivity(Intent.createChooser(i, "Select email application."));
            }
        });


    }


    /**
     * Dans le onCreate ou onResume on tente de se reconnecter avec les uid et sid sauvegardés
     */
    private void loginFromUserFile(String uid, String sid) {

        ArrayList<NameValuePair> _args = new ArrayList<>();
        _args.clear();
        _args.add(new BasicNameValuePair("uid", uid));
        _args.add(new BasicNameValuePair("sid", sid));

        HttpApiCall _apiCaller = new HttpApiCall(this, _args, 3);

        LogInFile.getInstance().WriteLog(TAG + "::loginFromUserFile:: try to loggedin with uid: " + uid + " - sid: " + sid, true);

        _apiCaller.execute(Tools.API + Tools.CDACCOUNTLOGGEDIN);
    }


    private void loggedin() throws JSONException {
        ArrayList<NameValuePair> _args = new ArrayList<>();
        _args.clear();
        if (_jsonUser != null && !_jsonUser.equals("jsonUser")) {
            JSONObject userObj = new JSONObject(_jsonUser);
            if (Tools.CDDEBUG) {
                Log.d(TAG, "loggedin::jsonObj::" + userObj.toString() + ", sid = " + _sid);
                LogInFile.getInstance().WriteLog(TAG + " loggedin::userObj :: " + userObj.toString() + ", sid = " + _sid, true);
            }
            if (Tools.CDDEBUG) Log.d(TAG, "loggedin::uid::" + userObj.getString("id") + " - sid::" + _sid);
            _args.add(new BasicNameValuePair("uid", userObj.getString("id")));
            _args.add(new BasicNameValuePair("sid", _sid));
            HttpApiCall _apiCaller = new HttpApiCall(this, _args, 3);
            _apiCaller.execute(Tools.API + Tools.CDACCOUNTLOGGEDIN);
        }
    }


    private void loggedinWithPassword() throws Exception {

        ArrayList<NameValuePair> _args = new ArrayList<>();
        _args.clear();
        if (_jsonUser != null && !_jsonUser.equals("jsonUser")) {
            JSONObject userObj = new JSONObject(_jsonUser);

            if (Tools.CDDEBUG) Log.d(TAG, "loggedin::uid::" + userObj.getString("id") + " - sid::" + _sid);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            _args.add(new BasicNameValuePair("u", sharedPreferences.getString("email", "email")));
            _args.add(new BasicNameValuePair("p", sharedPreferences.getString("pass", "pass")));

            if (Tools.CDDEBUG) Log.d(TAG, "loggedinWithPassword;; trying second login with u: " + sharedPreferences.getString("email", "email") + " - pass : " + sharedPreferences.getString("pass", "pass"));

            HttpApiCall _apiCaller = new HttpApiCall(this, _args, 7);
            _apiCaller.execute(Tools.API + Tools.CDACCOUNTLOGIN);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Today.bddLoaded = false;
        if (Tools.CDDEBUG) {
            Log.d("MainActivity::onPause::Pass::", UserConnected.getInstance().get_password());
            LogInFile.getInstance().WriteLog("MainActivity::onPause ! + UserConnected ? " + UserConnected.getInstance().IsUserConnected(), true);
        }

        /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (UserConnected.getInstance().IsUserConnected()) {
            editor.putString("jsonUser", UserConnected.getInstance().get_jsonObj());
            editor.putString("sid", UserConnected.getInstance().get_sid());
            editor.putString("user", "true");
            editor.putString("pass", UserConnected.getInstance().get_password());
            editor.putString("email", UserConnected.getInstance().get_mail());
        }
        else {
            editor.putString("user", "false");
        }
        editor.commit();
        */

        /* Stopping Ping Service */
        if (Login._mPingService != null) {
            if (Tools.CDDEBUG)
                Log.d(TAG, "Stopping Ping & Count Service");
            stopService(Login._mPingService);
        }
    }

    private void loadBdd() {
        if (_todayElem == null) {
            _todayElem = new Today(this);
            Log.d("MainActivity::loadBdd::_todayElem::", "null");
        }
        else
            Log.d("MainActivity::loadBdd::_todayElem::", "not null");
        if (!Today.bddLoaded) {
            _todayElem.loadBDD();
            Log.d("MainActivity::loadBdd::_todayElem::bddLoaded::", "false");
        }
        else
            Log.d("MainActivity::loadBdd::_todayElem::bddLoaded::", "true");
    }

    private void setHomeButtonsListeners() {
        setHomeButtonOnclickListener(R.id.button_aujourdhui_ComCarpeDeumHomeButtons, R.layout.com_carpedeum_aujourdhui);
        setHomeButtonOnclickListener(R.id.button_monprofil_ComCarpeDeumHomeButtons, R.layout.com_carpedeum_monprofil);
        setHomeButtonOnclickListener(R.id.button_ensemble_ComCarpeDeumHomeButtons, R.layout.com_carpedeum_ensemble);
    }

    private void setHomeButtonOnclickListener(int btnId, final int layoutId) {
        final ImageButton btn = (ImageButton)findViewById(btnId);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutId == R.layout.com_carpedeum_aujourdhui && _currentLayout != AUJOURDHUI) {
                    setContentView(layoutId);
                    ImageButton im = (ImageButton)findViewById(R.id.button_aujourdhui_ComCarpeDeumHomeButtons);
                    im.setImageResource(R.drawable.tabicon_aujourdhui_selected);
                    im.setBackgroundResource(getTodayDrawableFooterTheme());
                    setLayoutTheme();
                    resumeLayout();
                    _currentLayout = AUJOURDHUI;
                }
                else if (layoutId == R.layout.com_carpedeum_monprofil && _currentLayout != MONPROFL) {
                    set_currentLayout_Profile(layoutId);
                }
                else if (layoutId == R.layout.com_carpedeum_ensemble && _currentLayout != ENSEMBLE) {
                    setContentView(layoutId);
                    ImageButton im = (ImageButton)findViewById(R.id.button_ensemble_ComCarpeDeumHomeButtons);
                    im.setImageResource(R.drawable.tabicon_ensemble_selected);
                    im.setBackgroundResource(getTodayDrawableFooterTheme());
                    setLayoutTheme();
                    _currentLayout = ENSEMBLE;
                    displayTogetherContents();
                }
                setHomeButtonsListeners();
            }
        });
        setLogOutBtn();
    }

    private void displayFirstTab() {
        setContentView(R.layout.com_carpedeum_aujourdhui);
        setHomeButtonsListeners();
    }

    private void setLogOutBtn() {
        Button logOutBtn = (Button)findViewById(R.id.button_logout_com_carpedeum_footer);
        if (logOutBtn != null) {
            if (UserConnected.getInstance().IsUserConnected()) {
                logOutBtn.setText(getString(R.string.HOME_LOGOUT));
            }
            else {
                logOutBtn.setText(getString(R.string.HOME_LOGIN));
            }
            logOutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserConnected.getInstance().IsUserConnected()) {
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle(getString(R.string.LOGOUT_CONFIRM));
                        alertDialog.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.OK,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Login.getInstance().logOut();
                                        dialog.dismiss();
                                        displayFirstTab();
                                        resumeLayout();
                                    }
                                }).create();
                        alertDialog.show();
                    }
                    else {
                        set_currentLayout_Profile(R.layout.com_carpedeum_monprofil);
                        setHomeButtonsListeners();
                    }
                }
            });
        }
        Button contactBtn = (Button)findViewById(R.id.button_contact_com_carpedeum_footer);
        if (contactBtn != null) {
            contactBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"carpedeum.75@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Contact");
                    intent.putExtra(Intent.EXTRA_TEXT, "Votre message à l'équipe CarpeDeum");
                    startActivity(Intent.createChooser(intent, "Send Email"));
                }
            });
        }
    }

    // TOGETHER
    private void displayTogetherContents() {
        if (UserConnected.getInstance().IsUserConnected()) {
            setContentView(R.layout.com_together);
            ImageButton im = (ImageButton)findViewById(R.id.button_ensemble_ComCarpeDeumHomeButtons);
            im.setImageResource(R.drawable.tabicon_ensemble_selected);
            im.setBackgroundResource(getTodayDrawableFooterTheme());
            _together = new Together(this, _resId);
            _together.displayTogetherInfos();
        }
        else {
            setContentView(R.layout.com_carpedeum_monprofil);
            ImageButton im = (ImageButton)findViewById(R.id.button_ensemble_ComCarpeDeumHomeButtons);
            im.setImageResource(R.drawable.tabicon_ensemble_selected);
            im.setBackgroundResource(getTodayDrawableFooterTheme());
            TextView notConnectedTV = (TextView)findViewById(R.id.textView_connect_or_login_com_monprofil);
            notConnectedTV.setText(Html.fromHtml(getString(R.string.LOGIN_OR_REGISTER_INTRO)));
            Button loginBtn = (Button)findViewById(R.id.button_login_com_monprofil);
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
                    loginIntent.putExtra("resId", _resId);
                    startActivityForResult(loginIntent, LOGIN);
                }
            });
        }
    }

    //PROFILE
    public void set_currentLayout_Profile(int layoutId) {
        setContentView(layoutId);
        ImageButton im = (ImageButton)findViewById(R.id.button_monprofil_ComCarpeDeumHomeButtons);
        im.setImageResource(R.drawable.tabicon_monprofil_selected);
        im.setBackgroundResource(getTodayDrawableFooterTheme());
        setLayoutTheme();
        _currentLayout = MONPROFL;
        if (!UserConnected.getInstance().IsUserConnected()) {
            TextView notConnectedTV = (TextView)findViewById(R.id.textView_connect_or_login_com_monprofil);
            notConnectedTV.setText(Html.fromHtml(getString(R.string.LOGIN_OR_REGISTER_INTRO)));
            Button loginBtn = (Button)findViewById(R.id.button_login_com_monprofil);
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
                    loginIntent.putExtra("resId", _resId);
                    startActivityForResult(loginIntent, LOGIN);
                }
            });
            Button registerBtn = (Button)findViewById(R.id.button_register_com_monprofil);
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent registerIntent = new Intent(MainActivity.this, Register.class);
                    registerIntent.putExtra("resId", _resId);
                    startActivityForResult(registerIntent, Tools.REGISTER);
                }
            });
        }
        else {
            displayProfileLayoutForConnectedUser();
        }
    }

    private void displayProfileLayoutForConnectedUser() {
        setContentView(R.layout.com_profile);
        ImageButton im = (ImageButton)findViewById(R.id.button_monprofil_ComCarpeDeumHomeButtons);
        im.setImageResource(R.drawable.tabicon_monprofil_selected);
        im.setBackgroundResource(getTodayDrawableFooterTheme());
        _profile = new Profile(this, _resId);
        _profile.setProfileLayout();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN) {
            if (resultCode == RESULT_OK){
                String result = data.getStringExtra("result");
                if (Tools.CDDEBUG)
                    Log.d("MainActivity::onActivityResult::", result + ", Layout::" + String.valueOf(_currentLayout));
                if (result != null && result.equals("ok")) {
                    //callHomeWithUserConnected();
                    //set_currentLayout_Profile(R.layout.com_carpedeum_monprofil);

                    setContentView(R.layout.com_carpedeum_aujourdhui);
                    setHomeButtonsListeners();
                    setTodayLayout();
                }
            }
        }
        else if (requestCode == Profile.PRAYER) {
            if (Tools.CDDEBUG)
                Log.d("MainActivity::onActivityResult::requestCode::", "prayer");
            _profile.getMyProfileFromAPI();
        }
        else if ((requestCode == 40 && resultCode == RESULT_OK)) {
            if (data.hasExtra("startActivity")) {
                set_currentLayout_Profile(R.layout.com_carpedeum_monprofil);
                setHomeButtonsListeners();
            }
        }
        else if ((requestCode == Tools.MANVOICE && resultCode == RESULT_OK) || (requestCode == 12 && resultCode == RESULT_OK)) {
            if (data.hasExtra("startActivity")) {
                set_currentLayout_Profile(R.layout.com_carpedeum_monprofil);
                setHomeButtonsListeners();
            }
        }
        else if (requestCode == Tools.DELETEPROFILE && resultCode == RESULT_OK) {
            if (Tools.CDDEBUG)
                Log.d("MainActivity::finished::", "deleted profile");
            setContentView(R.layout.com_carpedeum_aujourdhui);
            _profile = null;
            setTodayLayout();
        }
        else if (requestCode == Tools.REGISTER && resultCode == RESULT_OK) {
            if (Tools.CDDEBUG)
                Log.d("MainActivity::finished::", "register");
            displayProfileLayoutForConnectedUser();
            setHomeButtonsListeners();
        }
    }

/*    private void callHomeWithUserConnected() {
        _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _apiCaller = new HttpApiCall(this, _args, 1);
        _apiCaller.execute(Tools.API + Tools.HOME);
        _apiCaller = null;
    }
*/
    //TODAY

    private void setTodayLayout() {

        _todayDate = Today.getCurrentDate();
        _currentLayout = AUJOURDHUI;
        loadElementsFromBDD();
        callApiForManVoice();
        if (internetConnectionOk()) {
            loadElementsFromAPI();
        }
        else {
            displayListenersForInternetKo();
            if (_homeProgressDialog != null)
                _homeProgressDialog.cancel();
        }
        setHomeButtonsListeners();
    }

    private void displayListenersForInternetKo() {
        TextView manVoiceTV = (TextView)findViewById(R.id.textView_manVoice_com_carpedeum_aujourdhui);
        manVoiceTV.setVisibility(View.GONE);
        ImageView manVoiceIV = (ImageView)findViewById(R.id.imageButton_manVoice_com_carpedeum_aujourdhui);
        manVoiceIV.setVisibility(View.GONE);
        ImageView godVoiceIV = (ImageView)findViewById(R.id.imageButton_GodVoice_com_carpedeum_aujourdhui);
        godVoiceIV.setVisibility(View.GONE);
        TextView godVoiceTV = (TextView)findViewById(R.id.textView_GodVoice_com_carpedeum_aujourdhui);
        godVoiceTV.setBackgroundResource(R.drawable.roundedcornerquestions_home);
        godVoiceTV.setText(getString(R.string.LaParoledeDieu));
        godVoiceTV.setTextSize(15);
        godVoiceTV.setPadding(10, 10, 10, 10);
        setGodVoiceListener(godVoiceTV);
        TextView alertTV = (TextView)findViewById(R.id.textView_alerts_com_carpedeum_aujourdhui);
        alertTV.setVisibility(View.VISIBLE);
        alertTV.setText(Html.fromHtml(getString(R.string.HOME_OFFLINE)));
        ImageView geomesseIV = (ImageView)findViewById(R.id.imageView_geomesse_com_carpedeum_aujourdhui);
        geomesseIV.setVisibility(View.GONE);
        TextView onlineTV = (TextView)findViewById(R.id.textview_onlinepeople_com_carpedeum_aujourdhui);
        onlineTV.setVisibility(View.GONE);
        Button questionsBtn = (Button)findViewById(R.id.button_questions_com_carpedeum_aujourdhui);
        questionsBtn.setGravity(Gravity.CENTER);
        Button prayerBtn = (Button)findViewById(R.id.button_prayerbook_com_carpedeum_aujourdhui);
        prayerBtn.setGravity(Gravity.CENTER);
    }

    public void loadElementsFromBDD() {
        ImageButton im = (ImageButton)findViewById(R.id.button_aujourdhui_ComCarpeDeumHomeButtons);
        im.setImageResource(R.drawable.tabicon_aujourdhui_selected);
        im.setBackgroundResource(getTodayDrawableFooterTheme());
        setLayoutTheme();


        // Affiche nombre de messages en haut à droit dans la bulle
        if (UserConnected.getInstance().IsUserConnected() && _currentLayout == AUJOURDHUI) {
            TextView nbMessagesTV = (TextView)findViewById(R.id.textView_nb_messages);

            if (nbMessagesTV != null) {
                nbMessagesTV.setText(String.valueOf(UserConnected.getInstance().get_numUnreadMessages()));
            }
        }


        if (Tools.CDDEBUG)
            Log.d("MainActivity::loadElementsFromBDD::", String.valueOf(_currentLayout));
        if (_currentLayout == AUJOURDHUI) {
            if (Tools.CDDEBUG)
                Log.d("MainActivity::loadElementsFromBDD::", "AUJOURDHUI");
            displayElemsFromBDD();
        }
    }

    private void displayElemsFromBDD() {
        TextView dateEtFeteTV = (TextView)findViewById(R.id.textView_DateEtFete_ComCarpeDeumAujourdhui);
        dateEtFeteTV.setText(Html.fromHtml("<font color='#FFFFFF'><strong>" + Today.getCurrentDate().toUpperCase() + "</strong></font><br/><font color='#404040'>" + _todayElem.getCurrentFete() + "</font>"));
        _todayElem.displayGodVoice();


        /**
         * Calendrier : date et on click listener
         */
        TextView calendarBtn = (TextView)findViewById(R.id.button_calendrier_com_carpedeum_aujourdhui);
        calendarBtn.setText(Html.fromHtml(getString(R.string.CalendrierJour)));
        String calendarText = _todayElem.getCelebrationName();
        calendarBtn.setText(calendarText);
        calendarBtn.setBackgroundResource(_calendarLeftId);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCalendarActivity();
            }
        });

        /**
         * Le Saint du jour
         */
        TextView calendarBtn2 = (TextView)findViewById(R.id.button_calendar_com_carpedeum_aujourdhui);
        calendarBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDaySaintActivity();
            }
        });



        Button questionsBtn = (Button)findViewById(R.id.button_questions_com_carpedeum_aujourdhui);
        questionsBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
        questionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent questionsIntent = new Intent(MainActivity.this, Questions.class);
                questionsIntent.putExtra("resId", _resId);
                startActivity(questionsIntent);
            }
        });


        /**
         * Carnet de prières
         */
        Button prayerbookBtn = (Button)findViewById(R.id.button_prayerbook_com_carpedeum_aujourdhui);
        prayerbookBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
        prayerbookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent prayerbookIntent = new Intent(MainActivity.this, PrayerBook.class);
                prayerbookIntent.putExtra("resId", _resId);
                prayerbookIntent.putExtra("type", "prayerbook");
                startActivity(prayerbookIntent);
            }
        });


        /**
         * Ordinaire de la messe
         */

        ImageView ordbookBtn = (ImageView)findViewById(R.id.imageView_ordinaire_com_carpedeum_aujourdhui);
        ordbookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent prayerbookIntent = new Intent(MainActivity.this, PrayerBook.class);
                prayerbookIntent.putExtra("resId", _resId);
                prayerbookIntent.putExtra("type", "ordinaire");
                startActivity(prayerbookIntent);
            }
        });

        /**
         * Textes à méditer
         */
        Button meditTextBtn = (Button)findViewById(R.id.button_text_com_carpedeum_aujourdhui);
        meditTextBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
        meditTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent meditTextIntent = new Intent(MainActivity.this, PrayerBook.class);
                meditTextIntent.putExtra("resId", _resId);
                meditTextIntent.putExtra("type", "medittext");
                startActivity(meditTextIntent);
            }
        });



        Button aproposBtn = (Button)findViewById(R.id.button_apropos_com_aujourdhui);
        aproposBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aproposIntent = new Intent(MainActivity.this, aPropos.class);
                aproposIntent.putExtra("resId", _resId);
                startActivity(aproposIntent);
            }
        });

        Button condGenBtn = (Button)findViewById(R.id.button_conditionsgenerales_com_aujourdhui);
        condGenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent condGenIntent = new Intent(MainActivity.this, ConditionsGenerales.class);
                condGenIntent.putExtra("resId", _resId);
                startActivity(condGenIntent);
            }
        });
    }

    private void startDaySaintActivity() {
        Intent calendarIntent = new Intent(MainActivity.this, DaySaint.class);
        calendarIntent.putExtra("resId", _resId);
        startActivityForResult(calendarIntent, 40);
    }

    private void loadElementsFromAPI() {
        _todayElem.loadHomeElements();
        setGeomesseOnClickListener();
    }

    private void setGeomesseOnClickListener() {
        ImageView geomesseIV = (ImageView)findViewById(R.id.imageView_geomesse_com_carpedeum_aujourdhui);
        geomesseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent geomesseIntent = new Intent(MainActivity.this, Geomesse.class);
                geomesseIntent.putExtra("resId", _resId);
                startActivity(geomesseIntent);
            }
        });
    }

    /**
     * Démarrer l'activité du calendrier
     */
    public void startCalendarActivity() {
        Intent calendarIntent = new Intent(MainActivity.this, Calendar.class);
        calendarIntent.putExtra("resId", _resId);
        startActivityForResult(calendarIntent, 40);
    }

    public void displayOnlinePeople(String text) {
        TextView onlinePeopleTV = (TextView)findViewById(R.id.textview_onlinepeople_com_carpedeum_aujourdhui);
        if (onlinePeopleTV != null) {
            onlinePeopleTV.setText(Html.fromHtml(text));
            onlinePeopleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    set_currentLayout_Profile(R.layout.com_carpedeum_monprofil);
                    setHomeButtonsListeners();
                }
            });
        }
    }


    /**
     * Télécharger la parole d'homme
     */
    private void callApiForManVoice() {


        ArrayList<NameValuePair> _args = new ArrayList<>();
        _args.clear();
        _args.add(new BasicNameValuePair("date", Today.getFormatedForDBCurrentDate()));
        //_apiCaller = new HttpApiCall(this, _args, 2);
        //_apiCaller.execute(Tools.API + Tools.CDMANVOICE);

        PoolRequetes.getInstance().ajouterNouvelleRequete(this, _args, Tools.API + Tools.CDMANVOICE, 2);


        /*if (_manVoice == null || _todayElem == null || _todayElem._manVoiceBitmap == null || _changes) {
            if (Tools.CDDEBUG) Log.d(TAG, "LoadManVoice");
            if (_args == null)
                _args = new ArrayList<NameValuePair>();
            _args.clear();
            _args.add(new BasicNameValuePair("date", Today.getFormatedForDBCurrentDate()));
            _apiCaller = new HttpApiCall(this, _args, 2);
            _apiCaller.execute(Tools.API + Tools.CDMANVOICE);
        }
        else {
            if (Tools.CDDEBUG) Log.d(TAG, "Dont reloadManVoice");
            ImageView manVoiceIV = (ImageView)findViewById(R.id.imageButton_manVoice_com_carpedeum_aujourdhui);
            if (manVoiceIV != null && _todayElem._manVoiceBitmap != null) {
                manVoiceIV.setImageBitmap(_todayElem._manVoiceBitmap);
                manVoiceIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent manVoiceIntent = new Intent(MainActivity.this, ManVoice.class);
                        manVoiceIntent.putExtra("resId", _resId);
                        manVoiceIntent.putExtra("elements", _manVoice.toString());
                        startActivityForResult(manVoiceIntent, Tools.MANVOICE);
                    }
                });
            }
        }
        */
    }

    /**
     * Afficher la praole d'homme
     *
     * @param manVoiceObj
     * @throws Exception
     */
    private void displayManVoice(final JSONObject manVoiceObj) throws Exception {


        ImageView manVoiceIV = (ImageView)findViewById(R.id.imageButton_manVoice_com_carpedeum_aujourdhui);

        String imageURL = manVoiceObj.getString("image");
        int size = Tools.STD_W / 2;

        if (imageURL != null && imageURL.length() > 0 && internetConnectionOk()) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imageURL + size);
            if (cachedImage == null) {
                //new DownloadImages(imageIV, true, profilepicURL + Tools.STD_W / 5).execute(Tools.MEDIAROOT + profilepicURL, String.valueOf(Tools.STD_W / 5));
                PoolRequetes.getInstance().ajouterNouvelleRequeteImage(manVoiceIV, true, imageURL, size);
            }
            else {
                manVoiceIV.setImageBitmap(cachedImage);
                Today._manVoiceBitmap = cachedImage;
            }
            manVoiceIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        manVoiceIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manVoiceIntent = new Intent(MainActivity.this, ManVoice.class);
                manVoiceIntent.putExtra("resId", _resId);
                manVoiceIntent.putExtra("elements", _manVoice.toString());
                startActivityForResult(manVoiceIntent, Tools.MANVOICE);
            }
        });

        /*
        if (Today._manVoiceBitmap == null) {
            _downloadImageTask = new DownloadImageTask(this, manVoiceIV, 1);
            if (manVoiceObj.has("image")) {
                _downloadImageTask.execute(Tools.MEDIAROOT + manVoiceObj.getString("image"));
                _downloadImageTask = null;
            }
        }
        else {
            manVoiceIV.setImageBitmap(Today._manVoiceBitmap);
        }
        manVoiceIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manVoiceIntent = new Intent(MainActivity.this, ManVoice.class);
                manVoiceIntent.putExtra("resId", _resId);
                manVoiceIntent.putExtra("elements", _manVoice.toString());
                startActivityForResult(manVoiceIntent, Tools.MANVOICE);
            }
        });
        */
    }

    /*
        GOD VOICE
     */

    public void addGodVoice(String imgUrl) {
        ImageView GodVoiceIV = (ImageView)findViewById(R.id.imageButton_GodVoice_com_carpedeum_aujourdhui);
        if (_todayElem == null || Today._godVoiceBitmap == null || _changes) {
            if (imgUrl != null && imgUrl.length() > 0 && !imgUrl.equals("null")) {
                Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgUrl + getScreenWidth() / 2);
                if (cachedImage == null) {
                    new DownloadImages(GodVoiceIV, true, imgUrl + getScreenWidth() / 2).execute(imgUrl, String.valueOf(getScreenWidth() / 2));
                }
                else {
                    GodVoiceIV.setImageBitmap(cachedImage);
                    GodVoiceIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
            else {
                GodVoiceIV.setImageResource(R.drawable.com_godvoice_default);
            }
        }
        else {
            if (GodVoiceIV != null)
                GodVoiceIV.setImageBitmap(Today._godVoiceBitmap);
        }
        if (GodVoiceIV != null)
            setGodVoiceListener(GodVoiceIV);
    }

    private void setGodVoiceListener(View GodVoiceView) {
        GodVoiceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GodVoiceIntent = new Intent(MainActivity.this, GodVoice.class);
                GodVoiceIntent.putExtra("resId", _resId);
                GodVoiceIntent.putExtra("GodVoiceSrc", _todayElem.getGodVoiceSrc());
                GodVoiceIntent.putExtra("GodVoiceAudioURL", _todayElem.getGodVoiceAudioURL());
                GodVoiceIntent.putExtra("GodVoicePic", _todayElem.getGodVoicePic());
                GodVoiceIntent.putExtra("GodVoice", _todayElem.getGodVoice());
                GodVoiceIntent.putExtra("GodVoiceCredits", _todayElem.getGodVoiceCredits());
                GodVoiceIntent.putExtra("MedTextGodVoice", _todayElem.getGodVoiceMedText());
                GodVoiceIntent.putExtra("MedTextCreditsGodVoice", _todayElem.getGodVoiceMedTextCredits());
                GodVoiceIntent.putExtra("Oration", _todayElem.getGodVoiceOrationText());
                GodVoiceIntent.putExtra("OrationCredits", _todayElem.getGodVoiceOrationCredits());
                GodVoiceIntent.putExtra("readingTitle", _todayElem.getGodVoiceReadingTitle());
                GodVoiceIntent.putExtra("readingSubTitle", _todayElem.getGodVoiceReadingSubTitle());
                GodVoiceIntent.putExtra("reading", _todayElem.getGodVoiceReading());
                GodVoiceIntent.putExtra("readingCredits", _todayElem.getGodVoiceReadingCredits());
                GodVoiceIntent.putExtra("reading2Title", _todayElem.getGodVoiceReading2Title());
                GodVoiceIntent.putExtra("reading2SubTitle", _todayElem.getGodVoiceReading2SubTitle());
                GodVoiceIntent.putExtra("reading2", _todayElem.getGodVoiceReading2());
                GodVoiceIntent.putExtra("reading2Credits", _todayElem.getGodVoiceReading2Credits());
                GodVoiceIntent.putExtra("psalmTitle", _todayElem.getGodVoicePsalmTitle());
                GodVoiceIntent.putExtra("psalmSubTitle", _todayElem.getGodVoicePsalmSubTitle());
                GodVoiceIntent.putExtra("psalm", _todayElem.getGodVoicePsalm());
                GodVoiceIntent.putExtra("psalmCredits", _todayElem.getGodVoicePsalmCredits());
                startActivityForResult(GodVoiceIntent, 40);
            }
        });
    }

    /* **************************** */
    /* HOME VIDEOS */

    public void addHomeVideo(final String imgUrl, String text, final String id) {
        TextView loadingTV = (TextView)findViewById(R.id.textView_loading_videos_com_carpedeum_aujourdhui);
        if (loadingTV != null)
            loadingTV.setVisibility(View.GONE);
        LinearLayout imageVideoLL = new LinearLayout(this);
        imageVideoLL.setOrientation(LinearLayout.HORIZONTAL);
        final ImageView videoImageIV = new ImageView(this);

        int size = getScreenWidth() / 5;

        double inches = getScreeInches();

        if (inches <= Tools.STD_INCHES) {
            size = getScreenWidth() / 5;
            if (getScreenOrientation() == 1) {
                //LinearLayout voicesLL = (LinearLayout)findViewById(R.id.linearLayout_voices_com_carpedeum_aujourdhui);
                size = getScreenHeight() / 5;
            }

        }
        else if ((inches > Tools.STD_INCHES) && (inches < (Tools.STD_INCHES * 2))) {
            size = Tools.STD_W / 5;
            if (getScreenOrientation() == 1) {
                size = Tools.STD_H / 5;
            }
        }
        else {

            size = (getScreenWidth() / 3) / 5;
            if (getScreenOrientation() == 1) {
                size = (getScreenHeight() / 3) / 5;
            }

        }




        //if (size > Tools.MAX_IMAGE_SIZE)
          //  size = Tools.MAX_IMAGE_SIZE;
        Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgUrl + size);
        if (cachedImage == null) {
            new DownloadImages(videoImageIV, true, imgUrl + size).execute(imgUrl, String.valueOf(size));
        }
        else {
            videoImageIV.setImageBitmap(cachedImage);
            videoImageIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        imageVideoLL.addView(videoImageIV, new LinearLayout.LayoutParams(size, (3 * size) / 4));
        videoImageIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        TextView imageTitleTV = new TextView(this);
        imageTitleTV.setText(text);
        imageTitleTV.setTextColor(getResources().getColor(R.color.black));
        imageTitleTV.setTextSize(15);
        imageTitleTV.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams imageTitleLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 60);
        imageTitleLP.setMargins(10, 0, 0, 0);
        imageVideoLL.addView(imageTitleTV, imageTitleLP);
        ImageView arrowIV = new ImageView(this);
        LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
        arrowLP.gravity = Gravity.CENTER_VERTICAL;
        arrowIV.setImageResource(_drawableArrowID);
        imageVideoLL.addView(arrowIV, arrowLP);


        // On click listener
        imageVideoLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoNewsIntent = new Intent(MainActivity.this, VideoNews.class);
                videoNewsIntent.putExtra("id", id);
                videoNewsIntent.putExtra("resId", _resId);
                videoNewsIntent.putExtra("image", imgUrl);
                startActivityForResult(videoNewsIntent, 12);
                /*Intent videoTest = new Intent(MainActivity.this, TestVideo.class);
                startActivity(videoTest);
                */
            }
        });



        LinearLayout.LayoutParams imageVideoLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageVideoLP.setMargins(5, 5, 0, 5);
        _homeVideosLinearLayout.addView(imageVideoLL, imageVideoLP);
        LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        View separatorView = new View(this);
        separatorView.setBackgroundResource(R.color.gris80);
        _homeVideosLinearLayout.addView(separatorView, layoutparamsSeparator);
    }

    public void addLinkForKiosque() {
        LinearLayout moreVideosLL = new LinearLayout(this);
        moreVideosLL.setOrientation(LinearLayout.HORIZONTAL);
        moreVideosLL.setBackgroundColor(getResources().getColor(R.color.white));

        TextView moreVideosTV = new TextView(this);
        moreVideosTV.setText(getString(R.string.HOME_MORE_NEWS));
        moreVideosTV.setTextSize(15);
        moreVideosTV.setTextColor(getResources().getColor(R.color.black));
        LinearLayout.LayoutParams textLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 90);
        moreVideosLL.addView(moreVideosTV, textLP);

        ImageView arrowIV = new ImageView(this);
        LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
        arrowLP.gravity = Gravity.CENTER_VERTICAL;
        arrowIV.setImageResource(_drawableArrowID);
        moreVideosLL.addView(arrowIV, arrowLP);

        LinearLayout.LayoutParams imageVideoLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageVideoLP.setMargins(5, 10, 0, 10);
        _homeVideosLinearLayout.addView(moreVideosLL, imageVideoLP);

        moreVideosLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moreVideosIntent = new Intent(MainActivity.this, Kiosque.class);
                moreVideosIntent.putExtra("resId", _resId);
                startActivity(moreVideosIntent);
            }
        });
    }

    public void removeAllVideos() {
        if (_currentLayout == AUJOURDHUI) {
            _homeVideosLinearLayout = (LinearLayout)findViewById(R.id.linearlayout_homevideos_com_carpedeum_aujourdhui);
            if (_homeVideosLinearLayout != null)
                _homeVideosLinearLayout.removeAllViews();
        }
    }

    /* ------------------------------ */


    /* ALL -- TOOLS */

    private int getTodayDrawableFooterTheme() {
        _resId = _todayElem.getTodayThemeHeader();

        Tools.RESID = _resId;

        GradientDrawable bgShape = null;
        if (_currentLayout == AUJOURDHUI) {
            TextView calendarBtn = (TextView)findViewById(R.id.button_calendrier_com_carpedeum_aujourdhui);
            if (calendarBtn != null)
                bgShape = (GradientDrawable)calendarBtn.getBackground();
        }
        if (_resId == 0)
            return R.drawable.tabselected;
        else if (_resId == 1)
            return R.drawable.tabselectedblue;
        else if (_resId == 2)
            return R.drawable.tabselectedgold;
        else if (_resId == 3)
            return R.drawable.tabselectedgreen;
        else if (_resId == 4)
            return R.drawable.tabselectedmauve;
        else if (_resId == 5)
            return R.drawable.tabselectedorange;
        else if (_resId == 6)
            return R.drawable.tabselectedpurple;
        else if (_resId == 7)
            return R.drawable.tabselectedred;
        else if (_resId == 8) {
            if (bgShape != null)
                bgShape.setColor(getResources().getColor(R.color.home_silver));
            return R.drawable.tabselectedsilver;
        }
        return R.drawable.tabselected;
    }

    private void setLayoutTheme() {
        _resId = _todayElem.getTodayThemeHeader();
        if (_resId == 0) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _calendarLeftId = R.drawable.roundedcornercalendarl_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _calendarLeftId = R.drawable.roundedcornercalendarl_blue_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _calendarLeftId = R.drawable.roundedcornercalendarl_gold_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _calendarLeftId = R.drawable.roundedcornercalendarl_green_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _calendarLeftId = R.drawable.roundedcornercalendarl_mauve_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _calendarLeftId = R.drawable.roundedcornercalendarl_orange_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _calendarLeftId = R.drawable.roundedcornercalendarl_purple_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _calendarLeftId = R.drawable.roundedcornercalendarl_red_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _calendarLeftId = R.drawable.roundedcornercalendarl_silver_com_carpedeum_home;
            _drawableArrowID = R.drawable.disclosure_silver;
        }
    }



    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(MainActivity.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (Tools.CDDEBUG) {
            Log.d("MainActivity::onApiResult::", result + "-" + type);
        }
        JSONObject resObj = new JSONObject(result);
        if (type == 1 && UserConnected.getInstance().IsUserConnected() && resObj.has("account_candles")) {
            UserConnected.getInstance().set_num_candles(resObj.getInt("account_candles"));
            UserConnected.getInstance().set_num_visited(resObj.getInt("count_visits"));
        }
        else if (type == 2) {
            displayManVoice(resObj);
            _manVoice = resObj;
        }
        else if (type == 3 || type == 7) {
            if (Tools.CDDEBUG) Log.d(TAG, "RES : -> " + result);
            LogInFile.getInstance().WriteLog(TAG + ", res: " + result, true);

            if (type == 3) {
                testifSuccessLoggedin(resObj);
            }
            else {
                testIfSucessLogin(resObj);
            }
        }
        else if (type == 5) {
            if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                UserConnected.getInstance().set_num_candles(resObj.getInt("num_available"));
                if (Tools.CDDEBUG) {
                    LogInFile.getInstance().WriteLog("Prayer::cierges restants: " + resObj.getString("num_available"), true);
                    Log.d("Prayer::cierges restants: ", resObj.getString("num_available"));
                }
            }
        }
    }

    /**
     * Le login avec uid et sid n'a pas fonctionné, on essaye avec l'email et le mot de passe
     *
     * @param resObj
     */
    private void testIfSucessLogin(JSONObject resObj) throws Exception {

        if (resObj.has("ok") && resObj.getString("ok").equals("1")) {

            if (Tools.CDDEBUG) Log.d(TAG, "login OK !");
            LogInFile.getInstance().WriteLog(TAG + " : login OK !", true);

            UserConnected.getInstance().setUserConnected(true);
            UserConnected.getInstance().set_uid(resObj.getString("uid"));
            UserConnected.getInstance().set_sid(resObj.getString("sid"));
            UserConnected.getInstance().setName(resObj.getString("profilename"));
            UserConnected.getInstance().setPic(resObj.getString("profilepic"));
        }

    }

    /**
     * Après avoir réouvert l'application on vérifie si le login a fonctionné en utilisant le uid et sid
     *
     * @param resObj
     */
    private void testifSuccessLoggedin(JSONObject resObj) throws Exception {
        if (resObj.has("loggedin") && resObj.has("ok") && resObj.getString("ok").equals("1") && resObj.getString("loggedin").equals("1")) {
            if (Tools.CDDEBUG) Log.d(TAG, "loggedin !");
            LogInFile.getInstance().WriteLog(TAG + " : loggedin !", true);
            Profile.setUserInfos(new JSONObject(_jsonUser), _sid);

                /* Starting PING services */
            Intent mServiceIntent = new Intent(this, PingService.class);
            startService(mServiceIntent);
        }
        else if (resObj.has("loggedin") && resObj.has("ok") && resObj.getString("ok").equals("1") && !resObj.getString("loggedin").equals("1")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPreferences.contains("jsonUser")) {
                _jsonUser = sharedPreferences.getString("jsonUser", "jsonUser");
                Log.d(TAG, "onResume::jsonUser::" + _jsonUser);
                Log.d(TAG, "Password::" + sharedPreferences.getString("pass", "pass"));
                _sid = sharedPreferences.getString("sid", "sid");
                if (_jsonUser != null) {
                    try {
                        loggedinWithPassword();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            Log.d(TAG, "not loggedin...");
            LogInFile.getInstance().WriteLog(TAG + " : not loggedin...", true);
        }
    }


    @Override
    public void onImageResult(Bitmap bitmap, ImageView view, int type) throws Exception {
        if (view != null)
            view.setImageBitmap(bitmap);
        _downloadImageTask = null;
        if (_todayElem != null && type == 1) {
            Today._manVoiceBitmap = bitmap;
        }
        else if (_todayElem != null && type == 2) {
            Today._godVoiceBitmap = bitmap;
        }
    }

    /**
     * Récupère le nombre de cierges depuis l'API
     */
    private void getCandlesFromApi() {
        ArrayList<NameValuePair> _args = new ArrayList<>(); _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));

        HttpApiCall _apiCaller = new HttpApiCall(this, _args, 5);
        _apiCaller.execute(Tools.API + Tools.CDACCOUNTCANDLEGET);
    }


    @Override
    public void onBackPressed() {
        //

        Log.e("onbackPressed", "Current layout : " + _currentLayout);
        if (_currentLayout != AUJOURDHUI) {
            setContentView(R.layout.com_carpedeum_aujourdhui);
            setHomeButtonsListeners();
            setTodayLayout();
        }
        else {
            super.onBackPressed();
        }
    }



    // Obtenir l'orientation de l'écran pour le dimensionnement des vignettes
    public int getScreenOrientation() {

        int height = getScreenHeight();
        int width = getScreenWidth();

        if (height > width) {
            return 0;
        }
        return 1;
    }


    /*
        Méthode pour obtenir la largeur de l'écran
     */
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

    /*
        Méthode pour obtenir la hauteur de l'écran
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public int getScreenHeight() {
        int apiLevel = android.os.Build.VERSION.SDK_INT;
        Display display = getWindowManager().getDefaultDisplay();
        int height;
        if (apiLevel >= 13) {
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        }
        else
            height = display.getHeight();
        return height;
    }

    public double getScreeInches() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float widthDpi = metrics.xdpi;
        float heightDpi = metrics.ydpi;
        float widthInches = widthPixels / widthDpi;
        float heightInches = heightPixels / heightDpi;
        return Math.sqrt((widthInches * widthInches) + (heightInches * heightInches));
    }
}