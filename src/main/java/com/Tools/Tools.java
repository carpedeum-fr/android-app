package com.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.Display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by COCH on 15/07/13.
 * Outils
 */

public class Tools {

    public static int RESID = 0;
    public static boolean CDDEBUG = true;
    public static String API = "https://api.carpedeum.fr/1/";
    public static String HOME = "home";
    public static String CDMANVOICE = "calendar/get-thought";
    public static String MEDIAROOT = "http://media.carpedeum.fr";
    public static String DATABASENAME = "v1-fr-20180327-001.db";
    public static String CDMOBILEROOT = "http://m.carpedeum.fr/";
    public static String CDDEFAULTLANG = "fr/";
    public static String CDCONTENT = "content/";
    public static String CDGOSPEL = "gospel/";
    public static String CDCALENDAR = "calendar/";
    public static String CDGETGOSPEL = "get-gospel/";
    public static String CDGETGOSPELCOMMENTARY = "get-gospelcommentary";
    public static String CDGOSPELCOMMENTARY = "gospelcommentary/";
    public static String CDTHOUGHT = "thought/";
    public static String CDORATION = "oration/";
    public static String CDREADING = "reading/";
    public static String CDPSALM = "psalm/";

    public static String CDNEWSGET = "news/get";
    public static String CDNEWSLIST = "news/list";
    public static String CDNEWS = "news/";

    public static String CDARTICLE = "article/";

    public static String CDYOUTUBEROOT = "http://www.youtube.com/embed/";

    public static String CDPROFILESGET = "profiles/get/";
    public static String CDPROFILESET = "profiles/edit/";
    public static String CDPROFILESLIST = "profiles/list";
    public static String CDPROFILESLIMIT = "25";

    public static String CDCLASSIFIEDSEDIT = "classifieds/edit";
    public static String CDCLASSIFIEDSLIST = "classifieds/list";
    public static String CDCLASSIFIEDSGET = "classifieds/get";
    public static String CDCLASSIFIEDSPRAYED = "classifieds/prayed";
    public static String CDCLASSIFIEDSDELETE = "classifieds/delete";
    public static String CDCLASSIFIEDSCANDLE = "classifieds/candle";

    public static String CDSTATUSADD = "status/add";
    public static String CDSTATUSGET = "status/get";
    public static String CDSTATUSREMOVE = "status/remove";

    public static String CDLIKESADD = "likes/add";
    public static String CDLIKESREMOVE = "likes/remove";

    public static String CDCOMMENTSADD = "comments/add";
    public static String CDCOMMENTSGET = "comments/get";
    public static String CDCOMMENTSREMOVE = "comments/remove";
    public static String CDCOMMENTSLIMIT = "10";

    public static String CDPLACESEDIT = "places/edit";
    public static String CDPLACESLIST = "places/list";
    public static String CDPLACESGET = "places/get";
    public static String CDPLACESCHECKIN = "places/checkin";
    public static String CDPLACELIMIT = "75";


    public static String CDMESSAGESLIST = "messages/list";
    public static String CDMESSAGESGET = "messages/get";
    public static String CDMESSAGESCOUNT = "messages/count";
    public static String CDMESSAGESSEND = "messages/send";
    public static String CDMESSAGESMARKREAD = "messages/markread";
    public static String CDMESSAGESLIMIT = "20";

    public static String CDACCOUNTCHANGEEMAIL = "account/changeemail";
    public static String CDACCOUNTCHANGEPASSWORD = "account/changepassword";
    public static String CDACCOUNTLOGIN = "account/login/";
    public static String CDACCOUNTLOGOUT = "account/logout/";
    public static String CDACCOUNTCLOSE = "account/close";
    public static String CDACCOUNTINVISIBLE = "account/set-invisible/";
    public static String CDACCOUNTPING = "account/ping";
    public static String CDACCOUNTSIGNUP = "account/signup";
    public static String CDACCOUNTLOGGEDIN = "account/loggedin";
    public static String CDACCOUNTCANDLEGET = "account/candles-get";
    public static String CDACCOUNTCANDLEBUY = "account/candles-buy-androidplaystore";
    public static String CDACCOUNTPREMIUMBUY = "account/premium-buy-androidplaystore";
    public static String CDACCOUNTPREMIUMGET = "account/premium-get";

    public static String CDCONTACTSEDIT = "contacts/edit";

    public static String CDUPLOAD = "upload";

    public static String CDDEFAULTLIMIT = "75";

    public static String CDADSGET = "ads/get";

    public static String GOOGLE_PLAY_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlvKer4Nm3nZskxTTcmKCrd3CWD5FlpXWsbWPHazQzWJEefkH7ucxa8JKW6t+bSnXUajvlV16Pb9VYTfpCq4e1YB0kcnL5FRZcay6DCv+mEuaj+vmeDvPK+Y7AKaXXCfT82gNLmwP+MfIDNkGAVY4nwj91/Cknam3GqbNi/8oPddkgQAWvjH/UT+yo2SKe8v7kWM/KPGXA7QadKHJLVASBO9Ma/GgP1DyRx5qzX6aAnbjR6bMI6LGAukEMWyeghpk4hQXW5lUYWMthLv6a8UgKC8KVBC3iRzHA1YxnJTnkPzE4ghwuMGcW7xugyITvcsLgatQ6zAwAjzFGKIsuAuULwIDAQAB";

    public static int MANVOICE = 11;
    public static int DELETEPROFILE = 23;
    public static int REGISTER = 24;

    public static int CDMAXPHOTOSIZE = 1200;

    public static int LITTLE_SCREEN_PORTRAIT_MODE = 640;
    public static int REF_SCREEN_WIDTH = 540;
    public static int MAX_IMAGE_SIZE = 100;
    public static int MAX_W_IMAGE_SIZE = 450;


    public static int STD_W = 0;
    public static int STD_H = 0;
    public static double STD_INCHES = 6.f;

    public static HashMap<String, String> CDPLACESCORRES = new HashMap<String, String>() {
        {
            put("CHAPEL", "Chapelle");
            put("CHURCH", "Eglise");
            put("PARISH", "Paroisse");
            put("CATHEDRAL", "Cathédrale");
            put("BASILICA", "Basilique");
            put("SANCTUARY", "Santuaire");
            put("MONASTERY", "Monastère");
            put("CONVENT", "Couvent");
            put("ABBEY", "Abbaye");
            put("COLLEGIATE", "Collégiale");
            put("ORATORY", "Oratoire");
        }
    };

    public static ArrayList<String> CDCANDLEBUYS = new ArrayList<String>() {
        {
            add("seven_candles");
            add("fifteen_candles");
            add("thirty_candles");
        }
    };

    public static ArrayList<String> CDANGELBUYS = new ArrayList<String>() {
        {
            add("thirty_angel");
            add("ninety_angel");
            add("one_year_angel");
        }
    };


    /**
     * Renvoie le nombre de pouces de l'écran
     * @param activity
     * @return
     */
    public static double getScreenInches(Activity activity) {

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float widthDpi = metrics.xdpi;
        float heightDpi = metrics.ydpi;
        float widthInches = widthPixels / widthDpi;
        float heightInches = heightPixels / heightDpi;
        return Math.sqrt((widthInches * widthInches) + (heightInches * heightInches));
    }


    // Obtenir l'orientation de l'écran pour le dimensionnement des vignettes
    public static int getScreenOrientation(Activity activity) {

        int height = getScreenHeight(activity);
        int width = getScreenWidth(activity);

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
    public static int getScreenWidth(Activity activity) {
        int apiLevel = android.os.Build.VERSION.SDK_INT;
        Display display = activity.getWindowManager().getDefaultDisplay();
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
    public static int getScreenHeight(Activity activity) {
        int apiLevel = android.os.Build.VERSION.SDK_INT;
        Display display = activity.getWindowManager().getDefaultDisplay();
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

    /**
     * Vérifie si l'appareil est bien connecté à internet
     * @return
     */
    private boolean internetConnectionOk(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
