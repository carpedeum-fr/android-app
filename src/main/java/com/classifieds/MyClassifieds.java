package com.classifieds;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.DownloadImageTask;
import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;
import com.prayers.Prayer;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Guillaume on 16/09/13.
 * Afficher mes petites annonces
 */

public class MyClassifieds extends Activity {

    private int _resId = -1;
    private int _drawableArrowID = -1;
    private ProgressDialog _homeProgressDialog = null;
    private ImageView _profileIV = null;
    private HashMap<String, String> _classifiedType = null;
    public static int CREATECLASSIFIED = 1;
    public static int DISPLAYCLASSIFIED = 2;
    private String _userId = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_classifieds_my_classifieds);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        fillClassifieldTypes();
        if (extras != null) {
            _resId = extras.getInt("resId");
            if (extras.containsKey("userId"))
                _userId = extras.getString("userId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            if (_userId != null)
                titleTV.setText(getResources().getString(R.string.HOME_NUM_CLASSIFIEDS));
            else
                titleTV.setText(getResources().getString(R.string.HOME_MYLIST_CLASSIFIEDS));
            downloadMyClassifieds();
        }
    }

    private void fillClassifieldTypes() {
        _classifiedType = new HashMap<String, String>();
        _classifiedType.put("tip", getString(R.string.UPDATE_POSTED_CLASSIFIED_TIP));
        _classifiedType.put("event", getString(R.string.UPDATE_POSTED_CLASSIFIED_EVENT));
        _classifiedType.put("sell", getString(R.string.UPDATE_POSTED_CLASSIFIED_SELL));
        _classifiedType.put("buy", getString(R.string.UPDATE_POSTED_CLASSIFIED_BUY));
        _classifiedType.put("job_search", getString(R.string.UPDATE_POSTED_CLASSIFIED_JOB_SEARCH));
        _classifiedType.put("job_offer", getString(R.string.UPDATE_POSTED_CLASSIFIED_JOB_OFFER));
        _classifiedType.put("jobs", getString(R.string.UPDATE_POSTED_CLASSIFIED_JOBS));
        _classifiedType.put("forsale", getString(R.string.UPDATE_POSTED_CLASSIFIED_FORSALE));
        _classifiedType.put("trade", getString(R.string.UPDATE_POSTED_CLASSIFIED_TRADE));
        _classifiedType.put("housing", getString(R.string.UPDATE_POSTED_CLASSIFIED_HOUSING));
        _classifiedType.put("misc", getString(R.string.UPDATE_POSTED_CLASSIFIED_MISC));
        _classifiedType.put("notice", getString(R.string.UPDATE_POSTED_CLASSIFIED_NOTICE));
    }

    private void downloadMyClassifieds() {
        if (internetConnectionOk()) {
            _homeProgressDialog = new ProgressDialog(this);
            _homeProgressDialog.setMessage(getString(R.string.ChargementEnCours));
            _homeProgressDialog.show();
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            if (_userId == null)
                _args.add(new BasicNameValuePair("id", UserConnected.getInstance().get_uid()));
            else
                _args.add(new BasicNameValuePair("id", _userId));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("type", "all"));
            _args.add(new BasicNameValuePair("mode", "profile"));
            _args.add(new BasicNameValuePair("limit", "20"));
            HttpApiCall apiCaller = new HttpApiCall(this, _args);
            String url = Tools.API + Tools.CDCLASSIFIEDSLIST;
            apiCaller.execute(url);
        }
        else {
            //TODO Message erreur pas de connection internet
        }
    }

    public void onApiResult(String result) {
        if (_homeProgressDialog != null)
            _homeProgressDialog.cancel();
        Button createClassifiedBtn = (Button)findViewById(R.id.button_create_classified_com_classifieds_my_classifieds);
        createClassifiedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createClassifiedIntent = new Intent(MyClassifieds.this, CreateClassified.class);
                createClassifiedIntent.putExtra("resId", _resId);
                startActivityForResult(createClassifiedIntent, CREATECLASSIFIED);
            }
        });
        if (result != null) {
            try {
                JSONObject resObj = new JSONObject(result);
                if (resObj.has("ok") && resObj.getString("ok").equals("1") && !resObj.getString("results").equals("null")) {
                    JSONArray classifiedsArray = resObj.getJSONArray("results");
                    if (classifiedsArray.length() > 0) {
                        TextView noResultTV = (TextView)findViewById(R.id.textView_no_results_com_classifieds_my_classifieds);
                        noResultTV.setVisibility(View.GONE);
                        displayClassifieds(classifiedsArray);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyClassifieds::onActivityResult::", requestCode + "-" + resultCode);
        if (resultCode == RESULT_OK && requestCode == CREATECLASSIFIED) {
            if (Tools.CDDEBUG)
                Log.d("MyClassified::", "classified created, reloading all classifieds");
            downloadMyClassifieds();
        }
        else if (resultCode == RESULT_OK && requestCode == DISPLAYCLASSIFIED) {
            if (Tools.CDDEBUG)
                Log.d("MyClassified::", "classifieds changed, reloading all classifieds");
            downloadMyClassifieds();
        }
    }

    private void displayClassifieds(JSONArray classifiedsArray) {
        LinearLayout classifiedsLL = (LinearLayout)findViewById(R.id.linearLayout_prayers_com_classifieds_my_classifieds);
        classifiedsLL.removeAllViews();
        try {
            for (int i = 0; i < classifiedsArray.length(); ++i) {
                final JSONObject classifiedObj = classifiedsArray.getJSONObject(i);
                LinearLayout classifiedLL = new LinearLayout(this);
                classifiedLL.setBackgroundColor(getResources().getColor(R.color.white));
                LinearLayout.LayoutParams profileImageLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 20);
                classifiedLL.setOrientation(LinearLayout.HORIZONTAL);
                //TODO faire un cache pour l'imageview
                ImageView profileIV = getProfileImage(classifiedObj.getString("profilepic"));
                if (profileIV != null) {
                    profileIV.setPadding(0, 0, 5, 0);
                    classifiedLL.addView(profileIV, profileImageLP);
                    _profileIV = profileIV;
                }
                TextView classifieldInfosTV = new TextView(this);
                String text = "<small><font color='#737373'>" + _classifiedType.get(classifiedObj.getString("type")) + ", " + classifiedObj.getString("dateinfo") + "</font></small><br/>";
                text += classifiedObj.getString("title") + "<br/><br/>";
                text += "<small><font color='#737373'>" + classifiedObj.getString("num_likes") + " " + getString(R.string.LIKE);
                if (classifiedObj.getInt("num_comments") > 1) {
                    text += " - " + classifiedObj.getString("num_comments") + " " + getString(R.string.COMMENTS).toLowerCase();
                }
                text += "</font></small>";
                classifieldInfosTV.setTextSize(15);
                classifieldInfosTV.setText(Html.fromHtml(text));
                LinearLayout.LayoutParams classifiedInfosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60);
                classifieldInfosTV.setTextColor(getResources().getColor(R.color.black));
                classifiedLL.addView(classifieldInfosTV, classifiedInfosLP);
                classifiedLL.setPadding(5, 5, 5, 5);
                classifiedLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    try {
                        Intent classifiedIntent = new Intent(MyClassifieds.this, Classified.class);
                        classifiedIntent.putExtra("resId", _resId);
                        classifiedIntent.putExtra("id", classifiedObj.getString("id"));
                        startActivityForResult(classifiedIntent, DISPLAYCLASSIFIED);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    }
                });
                classifiedsLL.addView(classifiedLL);
                LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                View separatorView = new View(this);
                separatorView.setBackgroundResource(R.color.gris73);
                classifiedsLL.addView(separatorView, layoutparamsSeparator);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ImageView getProfileImage(String profilepicURL) {
        if (profilepicURL != null && profilepicURL.length() > 0) {
            ImageView profileIV = new ImageView(this);
            int size = getScreenWidth() / 5;
            LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
            profileIV.setLayoutParams(profileLP);
            new DownloadImageTask(profileIV, true).execute(Tools.MEDIAROOT + profilepicURL);
            return profileIV;
        }
        return null;
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
        Intent messagesIntent = new Intent(MyClassifieds.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}