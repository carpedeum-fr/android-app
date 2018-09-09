package com.classifieds;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
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
import android.view.Window;
import android.widget.Button;
import android.widget.DialerFilter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.ads.ManageAds;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;
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
 * Created by Guillaume on 14/09/13.
 * Liste des annonces
 */

public class Classifieds extends Activity implements ApiCaller {

    private String DATE = "recent";
    private String DISTANCE = "local";
    private int _resId = -1;
    private ProgressDialog _progressDialog = null;
    private int _drawableArrowID = -1;
    private HashMap<String, String> _classifiedType = null;
    private String TYPE = DATE;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fillClassifieldTypes();
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_classifieds_classifieds);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.GONE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.HOME_NUM_CLASSIFIEDS));
        setButtonsOnClickListeners();
        downloadClassifieds();
        try {
            getAdsFromAdsManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAdsFromAdsManager() throws Exception {
        final JSONObject ads = ManageAds.getInstance().getAdd();
        if (Tools.CDDEBUG)
            Log.d("Calendar:ads:", ads.toString());
        ImageView addIV = (ImageView)findViewById(R.id.adds);
        if (getScreenWidth() > Tools.REF_SCREEN_WIDTH)
            new DownloadImageTask(addIV, false).execute(ads.getString("image"));
        else
            new DownloadImageTask(addIV, true).execute(ads.getString("image"));
        addIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!ads.getString("title").equals("Carpe Deum")) {
                        Intent openPageIntent = new Intent(Intent.ACTION_VIEW);
                        openPageIntent.setData(Uri.parse(ads.getString("url")));
                        startActivity(openPageIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setButtonsOnClickListeners() {
        final Button dateBtn = (Button)findViewById(R.id.button_date_com_classifieds_menu);
        final Button distanceBtn = (Button)findViewById(R.id.button_distance_com_classifieds_menu);

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TYPE = DATE;
                dateBtn.setBackgroundResource(R.drawable.segment_on_single);
                distanceBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);
                dateBtn.setTextColor(getResources().getColor(R.color.black));
                distanceBtn.setTextColor(getResources().getColor(R.color.white));
                downloadClassifieds();
            }
        });
        distanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TYPE = DISTANCE;
                dateBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
                distanceBtn.setBackgroundResource(R.drawable.segment_on_single);
                dateBtn.setTextColor(getResources().getColor(R.color.white));
                distanceBtn.setTextColor(getResources().getColor(R.color.black));
                downloadClassifieds();
            }
        });
    }

    private void downloadClassifieds() {
        _progressDialog.show();
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("type", "all"));
        _args.add(new BasicNameValuePair("mode", TYPE));
        _args.add(new BasicNameValuePair("limit", "10"));
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
        String url = Tools.API + Tools.CDCLASSIFIEDSLIST;
        apiCaller.execute(url);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok") && resObj.getString("ok").equals("1") && !resObj.getString("results").equals("null")) {
            JSONArray classifiedsArray = resObj.getJSONArray("results");
            if (classifiedsArray.length() > 0) {
                TextView noResultTV = (TextView)findViewById(R.id.textView_no_results_com_classifieds);
                noResultTV.setVisibility(View.GONE);
                displayClassifieds(classifiedsArray);
            }
        }
        else {
            LinearLayout classifiedsLL = (LinearLayout)findViewById(R.id.linearLayout_classifieds_com_classifieds);
            classifiedsLL.removeAllViews();
            TextView noResultTV = (TextView)findViewById(R.id.textView_no_results_com_classifieds);
            noResultTV.setVisibility(View.VISIBLE);
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

    private void displayClassifieds(JSONArray classifiedsArray) throws Exception {
        LinearLayout classifiedsLL = (LinearLayout)findViewById(R.id.linearLayout_classifieds_com_classifieds);
        classifiedsLL.removeAllViews();
        for (int i = 0; i < classifiedsArray.length(); ++i) {
            final JSONObject classifiedObj = classifiedsArray.getJSONObject(i);
            LinearLayout classifiedLL = new LinearLayout(this);
            classifiedLL.setBackgroundColor(getResources().getColor(R.color.white));
            classifiedLL.setOrientation(LinearLayout.HORIZONTAL);
            ImageView profileIV = getImage(classifiedObj.getString("profilepic"), classifiedObj.getString("image"));
            if (profileIV != null) {
                classifiedLL.addView(profileIV);
            }
            TextView classifieldInfosTV = new TextView(this);
            String text = "<small><font color='#737373'>";
            if (classifiedObj.has("type") && classifiedObj.getString("type").length() > 0)
                text += _classifiedType.get(classifiedObj.getString("type")) + " ";
            text += "de " + classifiedObj.getString("profilename") + ", " + classifiedObj.getString("dateinfo") + "</font></small><br/>";
            text += classifiedObj.getString("title") + "<br/><br/>";
            text += "<small><font color='#737373'>" + classifiedObj.getString("distance");
            if (classifiedObj.getInt("num_comments") > 1) {
                text += " - " + classifiedObj.getString("num_comments") + " " + getString(R.string.COMMENTS).toLowerCase();
            }
            text += "</font></small>";
            classifieldInfosTV.setTextSize(15);
            classifieldInfosTV.setText(Html.fromHtml(text));
            LinearLayout.LayoutParams classifiedInfosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60);
            classifiedInfosLP.setMargins(5, 0, 0, 0);
            classifieldInfosTV.setTextColor(getResources().getColor(R.color.black));
            classifiedInfosLP.setMargins(5, 0, 0, 0);
            classifiedLL.addView(classifieldInfosTV, classifiedInfosLP);
            classifiedLL.setPadding(5, 5, 5, 5);
            classifiedLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent classifiedIntent = new Intent(Classifieds.this, Classified.class);
                        classifiedIntent.putExtra("resId", _resId);
                        classifiedIntent.putExtra("id", classifiedObj.getString("id"));
                        startActivity(classifiedIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            ImageView arrowIV = new ImageView(this);
            LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
            arrowLP.gravity = Gravity.CENTER_VERTICAL;
            arrowIV.setImageResource(_drawableArrowID);
            classifiedLL.addView(arrowIV, arrowLP);
            classifiedsLL.addView(classifiedLL);
            LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            View separatorView = new View(this);
            separatorView.setBackgroundResource(R.color.gris73);
            classifiedsLL.addView(separatorView, layoutparamsSeparator);
        }
    }

    private ImageView getImage(String profilepicURL, String imageURL) {
        if (imageURL != null && imageURL.length() > 0)
            profilepicURL = imageURL;
        ImageView profileIV = new ImageView(this);
        int size = getScreenWidth() / 5;
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        if (profilepicURL != null && profilepicURL.length() > 0 && internetConnectionOk()) {
            int [] sizeS = {size, size};
            new DownloadImageTask(profileIV, true, sizeS).execute(Tools.MEDIAROOT + profilepicURL);
            profileIV.setMaxWidth(size);
            profileIV.setMaxHeight(size);
            return profileIV;
        }
        profileIV.setImageResource(R.drawable.default_user);
        profileIV.setMaxWidth(size);
        profileIV.setMaxHeight(size);
        return profileIV;
    }

    private void setLayoutTheme() {
        if (_resId == 0 ||_resId == -1) {
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
        Intent messagesIntent = new Intent(Classifieds.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}