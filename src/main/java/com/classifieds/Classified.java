package com.classifieds;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.ads.ManageAds;
import com.i2heaven.carpedeum.R;
import com.comments.AddComment;
import com.messages.DisplayMessage;
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
import java.util.TooManyListenersException;

/**
 * Created by Guillaume on 24/09/13.
 * Une annonce
 */

public class Classified extends Activity {

    private int _resId = -1;
    private int _drawableLikeID = -1;
    private ProgressDialog _progressDialog = null;
    private String _classifiedId = null;
    public static int COMMENT = 1;
    public static int EDITCLASSIFIED = 2;
    private String _title = null;
    private String _text = null;
    private boolean _changes = false;
    private HashMap<String, String> _classifiedType = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_classifieds_classified);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _classifiedId = extras.getString("id");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.HOME_NUM_CLASSIFIEDS));
            fillClassifieldTypes();
            downloadClassified();
            try {
                ManageAds.displayAdds(this, getScreenWidth(), false, 0, getScreenWidth());
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private void downloadClassified() {
        if (internetConnectionOk()) {
            _progressDialog = new ProgressDialog(this);
            _progressDialog.setMessage(getString(R.string.ChargementEnCours));
            _progressDialog.show();
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("id", _classifiedId));
            HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
            String url = Tools.API + Tools.CDCLASSIFIEDSGET;
            apiCaller.execute(url);
        }
        else {
            //TODO Message erreur pas de connection internet
        }
    }

    public void onApiResult(String result, int type) {
        boolean error = false;
        if (result != null) {
            try {
                if (_progressDialog != null)
                    _progressDialog.cancel();
                JSONObject classifiedObj = new JSONObject(result);
                if (classifiedObj.has("ok") && classifiedObj.getString("ok").equals("1")) {
                    if (Tools.CDDEBUG)
                        Log.d("Classified::onApiResult::", "mise a jour ok !");
                    if (type == 1) {
                        if (Tools.CDDEBUG)
                            Log.d("Classified::onApiResult", "displayClassified::" + classifiedObj.toString());
                        displayClassified(classifiedObj);
                    }
                    else if (type == 2) {
                        if (Tools.CDDEBUG)
                            Log.d("Classified::onApiResult", "downloadClassified");
                        _changes = true;
                        downloadClassified();
                    }
                    else if (type == 3) {
                        if (Tools.CDDEBUG)
                            Log.d("Classified::onApiResult::", "kill activity, classified deleted !");
                        _changes = true;
                        Intent myIntent = getIntent();
                        setResult(RESULT_OK, myIntent);
                        finish();
                    }
                }
                else {
                    error = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            error = true;
        }
        if (error) {
            //TODO display error
        }
    }

    private void displayClassified(JSONObject classifiedObj) {
        if (_progressDialog != null)
            _progressDialog.cancel();
        try {
            if (classifiedObj.has("image") && classifiedObj.getString("image").length() > 0)
                displayImg(classifiedObj.getString("image"));
            TextView nameTV = (TextView)findViewById(R.id.textView_classifiedInfos_com_classifieds_classified);
            nameTV.setText(classifiedObj.getString("accountname"));
            displayProfilePic(classifiedObj.getString("accountimage"));
            TextView dateTV = (TextView)findViewById(R.id.textView_classifiedDateInfos_com_classifieds_classified);
            dateTV.setText(classifiedObj.getString("dateinfo"));
            _title = classifiedObj.getString("title");
            String text = "";
            if (classifiedObj.has("type") && classifiedObj.getString("type").length() > 0)
                text = "<font color='#888888'>" + _classifiedType.get(classifiedObj.getString("type")).toUpperCase() + "</font><br/>";
            text += "<strong>" + classifiedObj.getString("title") + "</strong><br/><br/>";
            _text = classifiedObj.getString("text");
            if (!classifiedObj.getString("text").equals("null"))
                text += classifiedObj.getString("text");
            TextView classifiedTV = (TextView)findViewById(R.id.textView_classified_com_classifieds_classified);
            classifiedTV.setText(Html.fromHtml(text));
            TextView likesTV = (TextView)findViewById(R.id.textview_numLikes_com_classifieds_classified);
            likesTV.setText(classifiedObj.getString("num_likes"));
            manageLikes(classifiedObj);
            if (!classifiedObj.getString("can_edit").equals("1")) {
                Button editBtn = (Button)findViewById(R.id.button_edit_classified_com_classifieds_classified);
                editBtn.setVisibility(View.GONE);
                Button sendBtn = (Button)findViewById(R.id.button_send_message_com_classifieds_classified);
                sendBtn.setBackgroundResource(R.drawable.roundedcorner_white_home);
            }
            if (!classifiedObj.getString("can_delete").equals("1")) {
                Button editBtn = (Button)findViewById(R.id.button_delete_classified_com_classifieds_classified);
                editBtn.setVisibility(View.GONE);
            }
            if (classifiedObj.getString("allow_comments").equals("1")) {
                setCommentOnClickListener(classifiedObj.getString("latest_comments"));
            }
            else {
                LinearLayout addCommentLL = (LinearLayout)findViewById(R.id.linearLayout_addComment_com_classifieds_classified);
                addCommentLL.setVisibility(View.GONE);
            }
            if (!classifiedObj.getString("latest_comments").equals("") && !classifiedObj.getString("latest_comments").equals("null") && classifiedObj.getString("allow_comments").equals("1")) {
                displayComments(new JSONArray(classifiedObj.getString("latest_comments")));
            }
            setButtonsOnClickListener(classifiedObj.getString("allow_comments"), classifiedObj.getString("private"), classifiedObj.getString("accountid"), classifiedObj.getString("accountname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayImg(String imgUrl) {
        ImageView prayerIV = (ImageView)findViewById(R.id.imageView_photo_com_classifieds_classified);
        Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgUrl + getScreenWidth() / 3);
        if (cachedImage == null) {
            new DownloadImages(prayerIV, true, imgUrl + getScreenWidth() / 3).execute(Tools.MEDIAROOT + imgUrl, String.valueOf(getScreenWidth() / 3));
        }
        else {
            prayerIV.setImageBitmap(cachedImage);
            prayerIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void setButtonsOnClickListener(final String allow_comments, final String is_private, final String userId, final String userName) {
        Button editClassified = (Button)findViewById(R.id.button_edit_classified_com_classifieds_classified);
        editClassified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editClassifiedIntent = new Intent(Classified.this, EditClassified.class);
                editClassifiedIntent.putExtra("resId", _resId);
                editClassifiedIntent.putExtra("title", _title);
                editClassifiedIntent.putExtra("text", _text);
                //TODO ajouter photo
                //TODO coordonnes GPS
                editClassifiedIntent.putExtra("id", _classifiedId);
                editClassifiedIntent.putExtra("allow_comments", allow_comments);
                editClassifiedIntent.putExtra("private", is_private);
                startActivityForResult(editClassifiedIntent, EDITCLASSIFIED);
            }
        });
        Button deleteBtn = (Button)findViewById(R.id.button_delete_classified_com_classifieds_classified);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClassified();
            }
        });
        Button sendMessageBtn = (Button)findViewById(R.id.button_send_message_com_classifieds_classified);
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendMessageIntent = new Intent(Classified.this, DisplayMessage.class);
                sendMessageIntent.putExtra("resId", _resId);
                sendMessageIntent.putExtra("messageID", userId);
                sendMessageIntent.putExtra("headerName", userName);
                startActivity(sendMessageIntent);
            }
        });
    }

    private void deleteClassified() {
        AlertDialog.Builder deleteAD = new AlertDialog.Builder(Classified.this);
        deleteAD.setTitle(getString(R.string.CLASSIFIED_DELETE));
        deleteAD.setMessage(getString(R.string.CLASSIFIED_DELETE_CONFIRM));
        deleteAD.setCancelable(false);
        deleteAD.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        deleteAD.setPositiveButton(getString(R.string.DELETE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendDeleteAction();
            }
        });
        deleteAD.create().show();
    }

    private void sendDeleteAction() {
        if (Tools.CDDEBUG)
            Log.d("CLassified::sendDeleteAction::", "deleting classified with id::" + _classifiedId);
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", _classifiedId));
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 3);
        String url = Tools.API + Tools.CDCLASSIFIEDSDELETE;
        apiCaller.execute(url);
    }

    private void setCommentOnClickListener(final String lastestComments) {
        LinearLayout addCommentLL = (LinearLayout)findViewById(R.id.linearLayout_addComment_com_classifieds_classified);
        addCommentLL.setVisibility(View.VISIBLE);
        addCommentLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addCommentIntent = new Intent(Classified.this, AddComment.class);
                addCommentIntent.putExtra("resId", _resId);
                addCommentIntent.putExtra("comments", lastestComments);
                addCommentIntent.putExtra("item_type", "CLASSIFIED");
                addCommentIntent.putExtra("item_id", _classifiedId);
                startActivityForResult(addCommentIntent, COMMENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == COMMENT && Tools.CDDEBUG)
                Log.d("Classified::onActivityResult::", "commentaire ajoute !");
            else if (requestCode == EDITCLASSIFIED && Tools.CDDEBUG)
                Log.d("Classified::onActivityResult::", "Annonce mise a jour !");
            _changes = true;
            downloadClassified();
        }
    }

    private void manageLikes(final JSONObject classifiedObj) {
        try {
            ImageView iLikeIV = (ImageView)findViewById(R.id.imageView_like_com_classifieds_classified);
            if (classifiedObj.getString("i_like").equals("1"))
                iLikeIV.setImageResource(_drawableLikeID);
            else
                iLikeIV.setImageResource(R.drawable.footer_like);
            iLikeIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        List<NameValuePair> _args = new ArrayList<NameValuePair>();
                        String url;
                        _args.add(new BasicNameValuePair("item_type", "CLASSIFIED"));
                        _args.add(new BasicNameValuePair("item_id", classifiedObj.getString("id")));
                        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                        if (classifiedObj.getString("i_like").equals("1"))
                            url = Tools.API + Tools.CDLIKESREMOVE;
                        else
                            url = Tools.API + Tools.CDLIKESADD;
                        HttpApiCall apiCaller = new HttpApiCall(Classified.this, _args, 2);
                        apiCaller.execute(url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayComments(JSONArray commentsArray) {
        try {
            LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_classifieds_classified);
            commentsLL.removeAllViews();
            for (int i = 0; i < commentsArray.length(); ++i) {
                JSONObject commentObj = commentsArray.getJSONObject(i);
                if (Tools.CDDEBUG) Log.d("Comment::", commentObj.toString());
                LinearLayout commentLL = new LinearLayout(this);
                commentLL.setOrientation(LinearLayout.HORIZONTAL);
                ImageView profileIV = getProfileImageView(commentObj.getString("profilepic"));
                if (profileIV != null) {
                    LinearLayout.LayoutParams imageViewLP = new LinearLayout.LayoutParams(getScreenWidth() / 6, getScreenWidth() / 6);
                    imageViewLP.setMargins(5, 5, 5, 5);
                    commentLL.addView(profileIV, imageViewLP);
                }
                String text = "<small><font color='#888888'>" + commentObj.getString("profilename") + ", " + commentObj.getString("dateinfo") + "</small><br/><br/>";
                text += "<font color='#000000'>" + commentObj.getString("text") + "</font>";
                TextView commentTV = new TextView(this);
                commentTV.setText(Html.fromHtml(text));
                commentTV.setPadding(5, 0, 5, 5);
                commentTV.setTextSize(16);
                commentLL.addView(commentTV, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 80));
                commentsLL.addView(commentLL);

                LinearLayout.LayoutParams separatorLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                View separatorView = new View(this);
                separatorView.setBackgroundResource(R.color.gris80);
                commentsLL.addView(separatorView, separatorLP);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ImageView getProfileImageView(String profilepic) {
        if (internetConnectionOk()) {
            ImageView profilePicIV = new ImageView(this);
            new DownloadImageTask(profilePicIV, true).execute(Tools.MEDIAROOT + profilepic);
            return profilePicIV;
        }
        return null;
    }

    private void displayProfilePic(String profilepicURL) {
        if (profilepicURL != null && profilepicURL.length() > 0) {
            ImageView profileIV = (ImageView)findViewById(R.id.imageView_profilepic_com_classifieds_classified);
            int size = getScreenWidth() / 5;
            LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
            profileIV.setLayoutParams(profileLP);
            new DownloadImageTask(profileIV, true).execute(Tools.MEDIAROOT + profilepicURL);
        }
    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _drawableLikeID = R.drawable.footer_like_on;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _drawableLikeID = R.drawable.footer_like_on_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _drawableLikeID = R.drawable.footer_like_on_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _drawableLikeID = R.drawable.footer_like_on_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _drawableLikeID = R.drawable.footer_like_on_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _drawableLikeID = R.drawable.footer_like_on_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _drawableLikeID = R.drawable.footer_like_on_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _drawableLikeID = R.drawable.footer_like_on_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _drawableLikeID = R.drawable.footer_like_on_silver;
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

    @Override
    public void onBackPressed() {
        Intent myIntent = getIntent();
        Log.d("Classified::onBackPressed::", "changes::" + _changes);
        if (_changes) {
            setResult(RESULT_OK, myIntent);
            if (Tools.CDDEBUG)
                Log.d("Classified::onBackPressed::", "RESULT_OK::" + RESULT_OK);
        }
        else {
            setResult(RESULT_CANCELED, myIntent);
            if (Tools.CDDEBUG)
                Log.d("Classified::onBackPressed::", "RESULT_CANCELED::" + RESULT_CANCELED);
        }
        finish();
        super.onBackPressed();
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(Classified.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}