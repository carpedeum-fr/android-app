package com.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.account.Premium;
import com.ads.ManageAds;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 01/11/13.
 * Affiche les utilisateurs de l'application
 */

public class Parvis extends Activity implements ApiCaller {

    private int MESCONTACTS = 0;
    private int TOUS = 1;
    private int ONLINE = 2;
    private int ONOFFLIGNE = 3;

    private int _resId = -1;
    private ProgressDialog _progressDialog = null;
    private int _drawableArrowID = -1;
    private int _priestId = -1;
    private LinearLayout _usersLL = null;

    private int USERTYPE = TOUS;
    private int ONLINETYPE = ONLINE;

    private int _first = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_users_parvis);
        Bundle extras = getIntent().getExtras();
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        if (extras != null) {
            _resId = extras.getInt("resId");
            if (extras.containsKey("type") && extras.getString("type").equals("connected_contacts")) {
                USERTYPE = MESCONTACTS;
                final Button myContactsBtn = (Button)findViewById(R.id.button_mycontacts_com_users_parvis_headerbuttons);
                final Button allContactsBtn = (Button)findViewById(R.id.button_allcontacts_com_users_parvis_headerbuttons);
                myContactsBtn.setBackgroundResource(R.drawable.segment_on_single);
                allContactsBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);
                myContactsBtn.setTextColor(getResources().getColor(R.color.black));
                allContactsBtn.setTextColor(getResources().getColor(R.color.white));
            }
            setLayoutTheme();
        }
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.GONE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.VIEW_TITLE_DIRECTORY));
        setButtonsOnClickListeners();
        displayPub();
        getAllUsers(null);
    }

    private void setButtonsOnClickListeners() {
        final Button myContactsBtn = (Button)findViewById(R.id.button_mycontacts_com_users_parvis_headerbuttons);
        final Button allContactsBtn = (Button)findViewById(R.id.button_allcontacts_com_users_parvis_headerbuttons);
        final Button onlineBtn = (Button)findViewById(R.id.button_onlinecontacts_com_users_parvis_headerbuttons);
        final Button allOnOffBtn = (Button)findViewById(R.id.button_allonoffline_com_users_parvis_headerbuttons);

        myContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                USERTYPE = MESCONTACTS;
                myContactsBtn.setBackgroundResource(R.drawable.segment_on_single);
                allContactsBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);
                myContactsBtn.setTextColor(getResources().getColor(R.color.black));
                allContactsBtn.setTextColor(getResources().getColor(R.color.white));
                _first = 0;
                getAllUsers(null);
            }
        });
        allContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                USERTYPE = TOUS;
                myContactsBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
                allContactsBtn.setBackgroundResource(R.drawable.segment_on_single);
                myContactsBtn.setTextColor(getResources().getColor(R.color.white));
                allContactsBtn.setTextColor(getResources().getColor(R.color.black));
                _first = 0;
                getAllUsers(null);
            }
        });
        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ONLINETYPE = ONLINE;
                onlineBtn.setBackgroundResource(R.drawable.segment_on_single);
                allOnOffBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);
                onlineBtn.setTextColor(getResources().getColor(R.color.black));
                allOnOffBtn.setTextColor(getResources().getColor(R.color.white));
                _first = 0;
                getAllUsers(null);
            }
        });
        allOnOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ONLINETYPE = ONOFFLIGNE;
                onlineBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
                allOnOffBtn.setBackgroundResource(R.drawable.segment_on_single);
                onlineBtn.setTextColor(getResources().getColor(R.color.white));
                allOnOffBtn.setTextColor(getResources().getColor(R.color.black));
                _first = 0;
                getAllUsers(null);
            }
        });

        TextView loadMoreTV = (TextView)findViewById(R.id.textView_loadmore_com_users_parvis);
        loadMoreTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _first += 24;
                getAllUsers(null);
            }
        });

        final EditText searchED = (EditText)findViewById(R.id.editText_search_com_users_parvis);
        searchED.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && searchED.getText() != null && searchED.getText().toString().length() > 0) {
                    getAllUsers(searchED.getText().toString());
                }
                return false;
            }
        });
        searchED.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final ImageView clearIV = (ImageView)findViewById(R.id.imageView_clear_search_com_users_parvis);

                if (s != null && s.length() > 0) {
                    searchED.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    clearIV.setVisibility(View.VISIBLE);
                    clearIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchED.getText().clear();
                            searchED.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.search_icon), null, null, null);
                            clearIV.setVisibility(View.INVISIBLE);
                            getAllUsers(null);
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(searchED.getWindowToken(), 0);
                        }
                    });
                }
                else {
                    searchED.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.search_icon), null, null, null);
                    clearIV.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void displayPub() {
        try {
            final JSONObject addObj = ManageAds.getInstance().getAdd();
            ImageView addIV = (ImageView)findViewById(R.id.adds);
            new DownloadImageTask(addIV, true).execute(addObj.getString("image"));
            addIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (addObj.getString("url").contains("accountpremium")) {
                            Intent premiumIntent = new Intent(Parvis.this, Premium.class);
                            premiumIntent.putExtra("resId", _resId);
                            startActivity(premiumIntent);
                        }
                        else {
                            Intent openPageIntent = new Intent(Intent.ACTION_VIEW);
                            openPageIntent.setData(Uri.parse(addObj.getString("url")));
                            startActivity(openPageIntent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllUsers(String text) {
        _progressDialog.show();
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        if (UserConnected.getInstance().IsUserConnected()) {
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        }
        if (text != null) {
            _args.add(new BasicNameValuePair("q", text));
        }
        if (USERTYPE == TOUS && ONLINETYPE == ONLINE) {
            _args.add(new BasicNameValuePair("mode", "localonline"));
        }
        else if (USERTYPE == MESCONTACTS && ONLINETYPE == ONLINE) {
            _args.add(new BasicNameValuePair("mode", "contactsonline"));
        }
        else if (USERTYPE == MESCONTACTS && ONLINETYPE == ONOFFLIGNE) {
            _args.add(new BasicNameValuePair("mode", "contacts"));
        }
        else if (USERTYPE == TOUS && ONLINETYPE == ONOFFLIGNE) {
            _args.add(new BasicNameValuePair("mode", "local"));
        }
        _args.add(new BasicNameValuePair("limit", Tools.CDPROFILESLIMIT));
        int type = 1;
        if (_first > 1) {
            _args.add(new BasicNameValuePair("first", String.valueOf(_first)));
            type = 2;
        }
        HttpApiCall apiCaller = new HttpApiCall(this, _args, type);
        String url = Tools.API + Tools.CDPROFILESLIST;
        apiCaller.execute(url);
    }

    private void addUserToListing(final JSONObject userObj) throws Exception {
        LinearLayout userLL = new LinearLayout(this);
        userLL.setBackgroundColor(getResources().getColor(R.color.white));
        userLL.setOrientation(LinearLayout.HORIZONTAL);
        ImageView profileIV = getProfileImage(userObj.getString("image"));
        if (profileIV != null) {
            profileIV.setPadding(0, 0, 5, 0);
            userLL.addView(profileIV);
        }
        TextView placesInfosTV = new TextView(this);
        String text = userObj.getString("name") + "<br/>";
        text += "<small><font color='#737373'>" + userObj.getString("infos2") + "</font></small><br/>";
        placesInfosTV.setTextSize(15);
        placesInfosTV.setText(Html.fromHtml(text));
        LinearLayout.LayoutParams classifiedInfosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 55);
        placesInfosTV.setTextColor(getResources().getColor(R.color.black));
        userLL.addView(placesInfosTV, classifiedInfosLP);
        if (userObj.has("premium") && userObj.getString("premium").equals("1")) {
            ImageView premiumIV = new ImageView(this);
            premiumIV.setImageResource(R.drawable.list_angel);
            LinearLayout.LayoutParams premiumLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 20);
            premiumLP.gravity = Gravity.CENTER_VERTICAL;
            userLL.addView(premiumIV, premiumLP);
        }
        else {
            View v = new View(this);
            v.setBackgroundColor(getResources().getColor(R.color.white));
            userLL.addView(v, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 20));
        }
        ImageView rightImageIV = new ImageView(this);
        if (userObj.getString("type").equals("user"))
            rightImageIV.setImageResource(_drawableArrowID);
        else if (userObj.getString("type").equals("priest"))
            rightImageIV.setImageResource(_priestId);
        LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 15);
        arrowLP.gravity = Gravity.CENTER_VERTICAL;
        userLL.addView(rightImageIV, arrowLP);
        userLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(Parvis.this, MyProfile.class);
                profileIntent.putExtra("resId", _resId);
                try {
                    profileIntent.putExtra("profileId", userObj.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(profileIntent);
            }
        });
        _usersLL.addView(userLL);
        LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        View separatorView = new View(this);
        separatorView.setBackgroundResource(R.color.gris73);
        _usersLL.addView(separatorView, layoutparamsSeparator);
    }

    private ImageView getProfileImage(String profilepicURL) {
        ImageView profileIV = new ImageView(this);
        int size = getScreenWidth() / 5;
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        if (profilepicURL != null && profilepicURL.length() > 0 && internetConnectionOk()) {
            /*int [] sizeS = {size, size};
            new DownloadImageTask(profileIV, true, sizeS).execute(Tools.MEDIAROOT + profilepicURL);
            return profileIV;*/
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(profilepicURL + getScreenWidth() / 5);
            if (cachedImage == null) {
                new DownloadImages(profileIV, true, profilepicURL + getScreenWidth() / 5).execute(Tools.MEDIAROOT + profilepicURL, String.valueOf(getScreenWidth() / 5));
            }
            else {
                profileIV.setImageBitmap(cachedImage);
                profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            return profileIV;
        }
        profileIV.setImageResource(R.drawable.default_user);
        return profileIV;
    }

    private void setLayoutTheme() {
        if (_resId == 0 ||_resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _drawableArrowID = R.drawable.disclosure;
            _priestId = R.drawable.list_priest;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _drawableArrowID = R.drawable.disclosure_blue;
            _priestId = R.drawable.list_priest_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _drawableArrowID = R.drawable.disclosure_gold;
            _priestId = R.drawable.list_priest_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _drawableArrowID = R.drawable.disclosure_green;
            _priestId = R.drawable.list_priest_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _drawableArrowID = R.drawable.disclosure_mauve;
            _priestId = R.drawable.list_priest_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _drawableArrowID = R.drawable.disclosure_orange;
            _priestId = R.drawable.list_priest_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _drawableArrowID = R.drawable.disclosure_purple;
            _priestId = R.drawable.list_priest_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _drawableArrowID = R.drawable.disclosure_red;
            _priestId = R.drawable.list_priest_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _drawableArrowID = R.drawable.disclosure_silver;
            _priestId = R.drawable.list_priest_silver;
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
        Intent messagesIntent = new Intent(Parvis.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        _usersLL = (LinearLayout)findViewById(R.id.linearLayout_users_com_users_parvis);
        ScrollView parvisSV = (ScrollView)findViewById(R.id.scrollView_users_com_users_parvis);
        TextView infosTV = (TextView)findViewById(R.id.textView_infos_com_users_parvis);
        if (type == 1)
            _usersLL.removeAllViews();
        if (resObj.has("message") && resObj.getString("message").length() > 0) {
            parvisSV.setVisibility(View.GONE);
            infosTV.setVisibility(View.VISIBLE);
            infosTV.setText(resObj.getString("message"));
        }
        else {
            TextView moreTV = (TextView)findViewById(R.id.textView_loadmore_com_users_parvis);
            if (resObj.has("more") && resObj.getString("more").equals("1"))
                moreTV.setVisibility(View.VISIBLE);
            else
                moreTV.setVisibility(View.GONE);
            parvisSV.setVisibility(View.VISIBLE);
            infosTV.setVisibility(View.GONE);
            JSONArray usersArray = new JSONArray(resObj.getString("results"));
            for (int i = 0; i < usersArray.length(); ++i) {
                addUserToListing(usersArray.getJSONObject(i));
            }
        }
    }
}