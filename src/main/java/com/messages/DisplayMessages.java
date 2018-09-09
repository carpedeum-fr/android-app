package com.messages;

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
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.classifieds.Classified;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 02/10/13.
 * Afficher les messages recus/envoyés
 */

public class DisplayMessages extends Activity implements ApiCaller {

    private int _resId = -1;
    public ProgressDialog _progressDialog = null;
    private int _drawableArrowID = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_messages_display_messages);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView headerIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            headerIV.setVisibility(View.GONE);
            LinearLayout buttonsLL = (LinearLayout)findViewById(R.id.linearLayout_messages_header);
            buttonsLL.setVisibility(View.VISIBLE);
            TextView nbMessagesTV = (TextView)findViewById(R.id.textView_nb_messages);
            if (nbMessagesTV != null && UserConnected.getInstance().IsUserConnected()) {
                nbMessagesTV.setText(String.valueOf(UserConnected.getInstance().get_numUnreadMessages()));
            }
            if (UserConnected.getInstance().IsUserConnected()) {
                setButtonsListeners();
                downloadAllMessages();
            }
            else {
                TextView notConnected = (TextView)findViewById(R.id.textView_nomesssages_display_messages);
                notConnected.setText(getString(R.string.MESSAGE_NOT_CONNECTED_USER));
            }
        }
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
                    else {
                        Toast.makeText(getApplicationContext(), "En cours de développement...", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setButtonsListeners() {
        final TextView unreadTV = (TextView)findViewById(R.id.textView_unread_message_header);
        final TextView allTV = (TextView)findViewById(R.id.textView_all_messages_header);
        unreadTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unreadTV.setBackgroundResource(R.drawable.segment_on_single);
                unreadTV.setTextColor(getResources().getColor(R.color.black));
                allTV.setBackgroundColor(getResources().getColor(R.color.blackTransparentMessages));
                allTV.setTextColor(getResources().getColor(R.color.white));
                downloadUnreadMessages();
            }
        });
        allTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allTV.setBackgroundResource(R.drawable.segment_on_single);
                allTV.setTextColor(getResources().getColor(R.color.black));
                unreadTV.setBackgroundColor(getResources().getColor(R.color.blackTransparentMessages));
                unreadTV.setTextColor(getResources().getColor(R.color.white));
                downloadAllMessages();
            }
        });
    }


    private void downloadUnreadMessages() {
        if (internetConnectionOk() && UserConnected.getInstance().IsUserConnected()) {
            _progressDialog.show();
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("limit", Tools.CDMESSAGESLIMIT));
            _args.add(new BasicNameValuePair("filter", "unread"));
            HttpApiCall apiCaller = new HttpApiCall(this, _args, 0);
            String url = Tools.API + Tools.CDMESSAGESLIST;
            apiCaller.execute(url);
        }
        else {
            //TODO Message erreur pas de connection internet
        }
    }

    /*
        Cette méthode permet de télécharger tous les messages
     */
    private void downloadAllMessages() {
        if (internetConnectionOk() && UserConnected.getInstance().IsUserConnected()) {
            _progressDialog.show();
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("limit", Tools.CDMESSAGESLIMIT));
            HttpApiCall apiCaller = new HttpApiCall(this, _args, 0);
            String url = Tools.API + Tools.CDMESSAGESLIST;
            apiCaller.execute(url);
        }
        else {
            //TODO Message erreur pas de connection internet
        }
    }

    private void displayMessage(final JSONObject message) throws Exception {
        LinearLayout allMessagesLL = (LinearLayout)findViewById(R.id.linearLayout_messages_display_messages);
        LinearLayout messageLL = new LinearLayout(this);
        messageLL.setBackgroundColor(getResources().getColor(R.color.white));
        messageLL.setOrientation(LinearLayout.HORIZONTAL);
        ImageView profileIV = getProfileImage(message.getString("profilepic"));
        if (profileIV != null) {
            profileIV.setPadding(0, 0, 5, 0);
            profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            messageLL.addView(profileIV);
        }
        TextView messageTV = new TextView(this);
        String text = message.getString("name") + "<br/>";
        text += "<small><font color='#737373'>" + message.getString("infos") + "</font></small>";
        messageTV.setTextSize(15);
        messageTV.setText(Html.fromHtml(text));
        LinearLayout.LayoutParams messageInfosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 70);
        messageTV.setTextColor(getResources().getColor(R.color.black));
        messageLL.addView(messageTV, messageInfosLP);
        messageLL.setPadding(5, 5, 5, 5);
        messageLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent displayMessageIntent = new Intent(getApplicationContext(), DisplayMessage.class);
                displayMessageIntent.putExtra("resId", _resId);
                try {
                    displayMessageIntent.putExtra("messageID", message.getString("id"));
                    displayMessageIntent.putExtra("headerName", message.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(displayMessageIntent);
            }
        });
        if (message.getInt("num_unread") > 0) {
            TextView numUnreadTV = new TextView(this);
            numUnreadTV.setText(message.getString("num_unread"));
            numUnreadTV.setTextColor(getResources().getColor(R.color.white));
            numUnreadTV.setBackgroundResource(R.drawable.roundedcornergris);
            numUnreadTV.setPadding(5, 5, 5, 5);
            numUnreadTV.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams numUnreadLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
            numUnreadLP.gravity = Gravity.CENTER;
            messageLL.addView(numUnreadTV, numUnreadLP);
        }
        ImageView arrowIV = new ImageView(this);
        LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
        arrowLP.gravity = Gravity.CENTER_VERTICAL;
        arrowIV.setImageResource(_drawableArrowID);
        messageLL.addView(arrowIV, arrowLP);
        allMessagesLL.addView(messageLL, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getScreenWidth() / 6));
        LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        View separatorView = new View(this);
        separatorView.setBackgroundResource(R.color.gris73);
        allMessagesLL.addView(separatorView, layoutparamsSeparator);
    }

    private ImageView getProfileImage(String profilepicURL) {
        ImageView profileIV = new ImageView(this);
        int size = getScreenWidth() / 6;
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        if (profilepicURL != null && profilepicURL.length() > 0) {
            profileIV.setLayoutParams(profileLP);
            new DownloadImageTask(profileIV, true).execute(Tools.MEDIAROOT + profilepicURL);
            return profileIV;
        }
        profileIV.setImageResource(R.drawable.default_user);
        profileIV.setLayoutParams(profileLP);
        return profileIV;
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

    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();
        if (result != null) {
            JSONObject resultObj = new JSONObject(result);
            if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                TextView noMessagesTv = (TextView)findViewById(R.id.textView_nomesssages_display_messages);
                LinearLayout allMessagesLL = (LinearLayout)findViewById(R.id.linearLayout_messages_display_messages);
                allMessagesLL.removeAllViews();
                if (resultObj.has("message") && resultObj.getString("message").length() > 0) {
                    if (noMessagesTv != null) {
                        noMessagesTv.setVisibility(View.VISIBLE);
                        noMessagesTv.setText(resultObj.getString("message"));
                    }
                }
                else {
                    if (noMessagesTv != null)
                        noMessagesTv.setVisibility(View.GONE);
                    JSONArray resArray = new JSONArray(resultObj.getString("results"));
                    for (int i = 0; i < resArray.length(); ++i) {
                        displayMessage(resArray.getJSONObject(i));
                    }
                }
            }
        }
    }
}