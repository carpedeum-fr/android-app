package com.messages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Guillaume on 02/10/13.
 * Affiche une conversation
 */

public class DisplayMessage extends Activity implements ApiCaller {

    private int _resId = -1;
    public ProgressDialog _progressDialog = null;
    private String _messageId = null;
    List<NameValuePair> _args = new ArrayList<NameValuePair>();
    HttpApiCall _apiCaller = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_messages_display_message);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _messageId = extras.getString("messageID");
            String header = extras.getString("headerName");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(Html.fromHtml(header));
            Log.d("DisplayMessages::textView::", String.valueOf(titleTV.getWidth()));
            downloadMyMessage();
        }
    }

    private void downloadMyMessage() {
        final EditText addMessageED = (EditText)findViewById(R.id.editText_add_message_com_messages_display_message);
        addMessageED.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND && addMessageED.getText() != null && addMessageED.getText().toString().length() > 0) {
                    sendMessage(addMessageED.getText().toString());
                }
                return false;
            }
        });
        if (internetConnectionOk()) {
            _progressDialog.show();
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("id", _messageId));
            _args.add(new BasicNameValuePair("profile", "1"));
            _apiCaller = new HttpApiCall(this, _args, 1);
            _apiCaller.execute(Tools.API + Tools.CDMESSAGESGET);
        }
        else {
            //TODO Message erreur pas de connection internet
        }
    }

    private String getDate(long timeStamp) {
        DateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        Date netDate = (new Date(timeStamp));
        return sdf.format(netDate);
    }

    private void displayMessage(final JSONObject messageObj) throws Exception {
        LinearLayout allMessages = (LinearLayout)findViewById(R.id.linearLayout_messages_display_message);
        //TODO all timestamps
        //TODO images
        if (!messageObj.getString("images").equals("") && !messageObj.getString("images").equals("null")) {
            JSONArray imagesArray = new JSONArray(messageObj.getString("images"));
            for (int i = 0; i < imagesArray.length(); ++i) {
                if (Tools.CDDEBUG)
                    Log.d("DisplayMessage::displayMessage::pic::", imagesArray.getString(0) + ", all : " + imagesArray.toString());
                ImageView pic;
                if (messageObj.getString("type").equals("received"))
                    pic = getImage(imagesArray.getString(i), true);
                else
                    pic = getImage(imagesArray.getString(i), false);
                if (pic != null) {
                    allMessages.addView(pic);
                }
            }
        }
        if (!messageObj.getString("text").equals("")) {
            LinearLayout.LayoutParams messageLP = new LinearLayout.LayoutParams((int)(getScreenWidth() * 0.75), LinearLayout.LayoutParams.WRAP_CONTENT);
            TextView messageTV = new TextView(this);
            messageTV.setText(Html.fromHtml(messageObj.getString("text")));
            messageTV.setTextColor(getResources().getColor(R.color.black));
            messageTV.setTextSize(15);
            messageTV.setPadding(5, 5, 5, 5);
            if (messageObj.getString("type").equals("received")) {
                messageTV.setBackgroundColor(getResources().getColor(R.color.yellowMessages));
                messageLP.gravity = Gravity.LEFT;
            }
            else {
                messageTV.setBackgroundColor(getResources().getColor(R.color.white));
                messageLP.gravity = Gravity.RIGHT;
            }
            messageLP.setMargins(5, 5, 5, 5);
            allMessages.addView(messageTV, messageLP);
        }
    }

    private void callApiForMarkedReadedMessage(String id) {
        if (Tools.CDDEBUG) {
            Log.d("DisplayMessage::callApiForMarkedReadedMessage::message id::", id);
        }
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", id));
        _apiCaller = new HttpApiCall(this, _args, 0);
        _apiCaller.execute(Tools.API + Tools.CDMESSAGESMARKREAD);
    }

    private void sendMessage(String message) {
        if (Tools.CDDEBUG)
            Log.d("DisplayMessage::sendMessage::", message);
        if (internetConnectionOk()) {
            _progressDialog.show();
            _args.clear();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("id", _messageId));
            _args.add(new BasicNameValuePair("text", message));
            _apiCaller = new HttpApiCall(this, _args, 2);
            _apiCaller.execute(Tools.API + Tools.CDMESSAGESSEND);
        }
        else {
            //TODO Message erreur pas de connection internet
        }
    }

    public void adjustImage(ImageView pic, Bitmap image, boolean received) {
        int width = (int)((getScreenWidth() * 0.75) / 2);
        int ratio = image.getWidth() / width;
        int height = image.getHeight() / ratio;
        LinearLayout.LayoutParams imageLP = new LinearLayout.LayoutParams(width, height);
        if (received)
            imageLP.gravity = Gravity.LEFT;
        else
            imageLP.gravity = Gravity.RIGHT;
        pic.setLayoutParams(imageLP);
    }

    private ImageView getImage(String picURL, boolean received) {
        if (picURL != null && picURL.length() > 0) {
            ImageView imageIV = new ImageView(this);
            int size = getScreenWidth() / 6;
            LinearLayout.LayoutParams picLP = new LinearLayout.LayoutParams(size, size);
            imageIV.setLayoutParams(picLP);
            new DownloadImageTask(imageIV, true, this, received).execute(Tools.MEDIAROOT + picURL);
            return imageIV;
        }
        return null;
    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
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

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
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

    public void onMessageButtonClicked(View view) {

    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resultObj = new JSONObject(result);
        if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
            if (type == 1) {
                if (Tools.CDDEBUG) {
                    Log.d("DisplayMessage::onApiResult::result::", resultObj.toString());
                }
                if (resultObj.has("num_unread") && !resultObj.getString("num_unread").equals("null") && resultObj.getInt("num_unread") > 0) {
                    callApiForMarkedReadedMessage(resultObj.getString("id"));
                }
                boolean hasdate = false;
                TextView lastDateTV = (TextView)findViewById(R.id.textView_lastDate_com_messages_display_message);
                LinearLayout allMessages = (LinearLayout)findViewById(R.id.linearLayout_messages_display_message);
                allMessages.removeAllViews();
                if (!resultObj.getString("latest_received").equals("") && !resultObj.getString("latest_received").equals("null")
                        && !resultObj.getString("latest_received").equals("0000-00-00 00:00:00")) {
                    if (Tools.CDDEBUG)
                        Log.d("DisplayMessage::onApiResult::", resultObj.getString("latest_received"));
                    lastDateTV.setText(resultObj.getString("latest_received"));
                }
                else if (!resultObj.getString("latest_sent").equals("") && !resultObj.getString("latest_sent").equals("null")
                        && !resultObj.getString("latest_sent").equals("0000-00-00 00:00:00")) {
                    lastDateTV.setText(resultObj.getString("latest_sent"));
                }
                else {
                    hasdate = true;
                }
                if (resultObj.has("messages")) {
                    JSONArray resArray = new JSONArray(resultObj.getString("messages"));
                    for (int i = 0; i < resArray.length(); ++i) {
                        JSONObject messageObj = resArray.getJSONObject(i);
                        if (i == 0 && hasdate) {
                            lastDateTV.setText(getDate(messageObj.getLong("timestamp") * 1000));
                        }
                        displayMessage(messageObj);
                    }
                }
            }
            else if (type == 2) {
                EditText addMessageED = (EditText)findViewById(R.id.editText_add_message_com_messages_display_message);
                addMessageED.setText("");
                downloadMyMessage();
            }
        }
        else {
            //TODO message erreur
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        _args = null;
        _apiCaller = null;
        finish();
    }
}