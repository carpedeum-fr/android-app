package com.voice;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.carpedeum.Today;
import com.comments.AddComment;
import com.messages.DisplayMessages;
import com.user.MyProfile;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by COCH on 18/07/13.
 * Com ManVoice: La Parole d'Homme
 */

public class ManVoice extends Activity implements ApiCaller {

    private int _resId = -1;
    private JSONObject _elementsArray = null;
    public ProgressDialog _progressDialog = null;
    private int _drawableLikeID = -1;
    private String _date = null;
    private String _elements = null;
    private static int COMMENT = 12;
    private String TAG = "ManVoice";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_voice_manvoice);
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));

        if (!internetConnectionOk()) {
            displayError();
        }
        _progressDialog.show();
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _date = extras.getString("date");
            _elements = extras.getString("elements");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.INVISIBLE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            if (titleTV != null) {
                titleTV.setVisibility(View.VISIBLE);
                titleTV.setText(getResources().getString(R.string.ParoledHomme));
            }
            try {
                reloadLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void reloadLayout() throws Exception {
       // if (_elements == null) {
            List<NameValuePair> _args = new ArrayList<NameValuePair>();
            if (UserConnected.getInstance().IsUserConnected()) {
                _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            }
            String url = Tools.API + Tools.CDMANVOICE;
            if (_date == null) {
                _args.add(new BasicNameValuePair("date", getCurrentDate()));
                HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
                apiCaller.execute(url);
            }
            else {
                _args.add(new BasicNameValuePair("date", _date));
                HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
                apiCaller.execute(url);
            }
        /*}
        else {
            setLayoutElems(_elements);
        }*/
    }

    private void displayError() {
        LinearLayout allElems = (LinearLayout)findViewById(R.id.linearLayout_manvoice_com_voice_manvoice);
        allElems.removeAllViews();
        TextView internetErrorTV = new TextView(this);
        internetErrorTV.setText(getString(R.string.INFO_FAILED_CONNECTION));
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();
        if (result != null) {
            JSONObject resultObj = new JSONObject(result);
            if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                if (type == 1) {
                    setLayoutElems(result);
                }
                else if (type == 2) {
                    reloadLayout();
                }
            }
        }
    }

    public void setLayoutElems(String resString) throws Exception {
        if (resString != null) {
            JSONObject result = new JSONObject(resString);
            _elementsArray = result;
            String voice = result.getString("text");
            String voiceAuth = "- <i>" + result.getString("credits") + "</i>";
            String imgURL = result.getString("image");
            ImageView manVoiceIV = (ImageView)findViewById(R.id.imageView_manVoice_com_voice_manvoice);

            /*if (Today.getInstance() == null || Today.getInstance()._godVoiceBitmap == null)
                manVoiceIV.setImageBitmap(Today.getInstance()._manVoiceBitmap);
            else
               new DownloadImageTask(manVoiceIV, false).execute(Tools.MEDIAROOT + imgURL);
            */
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgURL + Tools.STD_W / 2);
            if (cachedImage == null) {
                new DownloadImages(manVoiceIV, true, imgURL + Tools.STD_W / 6).execute(Tools.MEDIAROOT + imgURL, String.valueOf(Tools.STD_W / 2));
            }
            else {
                manVoiceIV.setImageBitmap(cachedImage);
            }

            int width = Tools.STD_W;
            int height = (3 * width) / 4;

            LinearLayout.LayoutParams manVoiceLP = new LinearLayout.LayoutParams(width, height);
            manVoiceLP.gravity = Gravity.CENTER;
            manVoiceLP.setMargins(0, 20, 0, 20);
            manVoiceIV.setLayoutParams(manVoiceLP);
            TextView voiceTV = (TextView)findViewById(R.id.textView_manVoice_com_voice_manvoice);
            voiceTV.setPadding(0, 10, 0, 0);
            voiceTV.setText(Html.fromHtml(voice));
            TextView voiceAuthTV = (TextView)findViewById(R.id.textView_manVoiceAuthor_com_voice_manvoice);
            voiceAuthTV.setPadding(10, 0, 10, 0);
            voiceAuthTV.setText(Html.fromHtml(voiceAuth));
            manageLikes(result);
            setSharedContent(result);
            setComments(result);
            setShareOnClickListeners();
            if (_progressDialog != null && _progressDialog.isShowing())
                _progressDialog.cancel();
        }
    }

    private void manageLikes(final JSONObject classifiedObj) throws Exception {


        RelativeLayout likeRL = (RelativeLayout)findViewById(R.id.relativeLayout_like_com_voice_manvoice);
        ImageView iLikeIV = (ImageView)findViewById(R.id.imageView_like_com_manvoice);

        if (classifiedObj.getString("i_like").equals("1")) {
            iLikeIV.setImageResource(_drawableLikeID);
        }
        else {
            iLikeIV.setImageResource(R.drawable.footer_like);
        }
        likeRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeListener(classifiedObj);
            }
        });


        /*try {
            ImageView iLikeIV = (ImageView)findViewById(R.id.imageView_like_com_manvoice);
            if (classifiedObj.getString("i_like").equals("1"))
                iLikeIV.setImageResource(_drawableLikeID);
            else
                iLikeIV.setImageResource(R.drawable.footer_like);
            iLikeIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLikeListener(classifiedObj);
                }
            });
            TextView likeNumTV = (TextView)findViewById(R.id.textview_numLikes_com_voice_manvoice);
            likeNumTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLikeListener(classifiedObj);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }

    private void onLikeListener(JSONObject classifiedObj) {
        if (!UserConnected.getInstance().IsUserConnected()) {
            Intent myIntent = getIntent();
            myIntent.putExtra("startActivity", "profile");
            setResult(RESULT_OK, myIntent);
            finish();
        }
        else {
            try {
                List<NameValuePair> _args = new ArrayList<NameValuePair>();
                String url;
                _args.add(new BasicNameValuePair("item_type", "THOUGHT"));
                _args.add(new BasicNameValuePair("item_id", classifiedObj.getString("id")));
                _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                if (classifiedObj.getString("i_like").equals("1"))
                    url = Tools.API + Tools.CDLIKESREMOVE;
                else
                    url = Tools.API + Tools.CDLIKESADD;
                _progressDialog.show();
                HttpApiCall apiCaller = new HttpApiCall(ManVoice.this, _args, 2);
                apiCaller.execute(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setShareOnClickListeners() {
        TextView shareTV = (TextView)findViewById(R.id.textView_share_com_voice_manvoice);
        shareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareManVoice();
            }
        });
        ImageView shareIV = (ImageView)findViewById(R.id.imageView_shareicon_com_voice_manvoice);
        shareIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareManVoice();
            }
        });
    }

    private void shareManVoice() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = null;
        try {
            text = Tools.CDMOBILEROOT + Tools.CDDEFAULTLANG + Tools.CDCONTENT + Tools.CDTHOUGHT + _elementsArray.getString("id") + " " + getString(R.string.PenseeDuJour);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (Tools.CDDEBUG)
            Log.d("ManVoice::shareManVoice::text", text);
        //sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(text));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.PartagerVia)));
    }

    private void setComments(JSONObject result) {
        try {
            LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_voice_manvoice);
            commentsLL.removeAllViews();
            setCommentOnClickListener(result.getString("latest_comments"), result.getString("id"));
            if (result.getInt("num_comments") > 0) {
                if (Tools.CDDEBUG)
                    Log.d("Comments", result.getString("num_comments"));
                JSONArray commentsArray = new JSONArray(result.getString("latest_comments"));
                for (int i = 0; i < commentsArray.length() && i < 4; ++i) {
                    final JSONObject commentObj = commentsArray.getJSONObject(i);
                    if (Tools.CDDEBUG)
                        Log.d("Comment::", commentObj.toString());
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
                    commentLL.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                setCommentLLOnClickListener(commentObj.getString("profileid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    LinearLayout.LayoutParams separatorLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    View separatorView = new View(this);
                    separatorView.setBackgroundResource(R.color.gris80);
                    commentsLL.addView(separatorView, separatorLP);
                }
                if (Tools.CDDEBUG) {
                    Log.d("ManVoice::setComments::num::", result.getString("num_comments") + ", latest : " + result.getString("latest_comments").length());
                }
                if (result.getInt("num_comments") > 4) {
                    moreComments(result.getInt("num_comments"), result.getString("latest_comments"), result.getString("id"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCommentLLOnClickListener(String profileId) {
        Intent profileIntent = new Intent(ManVoice.this, MyProfile.class);
        profileIntent.putExtra("resId", _resId);
        profileIntent.putExtra("profileId", profileId);
        startActivity(profileIntent);
    }

    private void moreComments(int totalComments, final String lastestComments, final String id) {
        LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_voice_manvoice);
        int restComments = totalComments - 4;
        if (Tools.CDDEBUG)
            Log.d("ManVoice::moreComments::", "rest : " + restComments);
        LinearLayout moreCommentsLL = new LinearLayout(this);
        moreCommentsLL.setOrientation(LinearLayout.HORIZONTAL);
        ImageView commentIV = new ImageView(this);
        commentIV.setImageResource(R.drawable.footer_comments);
        LinearLayout.LayoutParams commentsIvLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 20);
        commentsIvLP.gravity = Gravity.CENTER;
        moreCommentsLL.addView(commentIV, commentsIvLP);
        TextView moreCommentsTV = new TextView(this);
        moreCommentsTV.setText(getString(R.string.DISPLAY_MORE_COMMENTS, restComments));
        LinearLayout.LayoutParams textLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 80);
        textLP.gravity = Gravity.CENTER;
        moreCommentsLL.addView(moreCommentsTV, textLP);
        moreCommentsLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserConnected.getInstance().IsUserConnected()) {
                    Intent addCommentIntent = new Intent(ManVoice.this, AddComment.class);
                    addCommentIntent.putExtra("resId", _resId);
                    //addCommentIntent.putExtra("comments", lastestComments);
                    addCommentIntent.putExtra("item_type", "THOUGHT");
                    addCommentIntent.putExtra("item_id", id);
                    startActivityForResult(addCommentIntent, COMMENT);
                }
                else {
                    Intent myIntent = getIntent();
                    myIntent.putExtra("startActivity", "profile");
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
            }
        });
        commentsLL.addView(moreCommentsLL, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getScreenWidth() / 6));
    }

    private void setCommentOnClickListener(final String lastestComments, final String id) {
        TextView addCommentTv = (TextView)findViewById(R.id.textView_addComment_com_voice_manvoice);
        addCommentTv.setVisibility(View.VISIBLE);
        addCommentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserConnected.getInstance().IsUserConnected()) {
                    Intent addCommentIntent = new Intent(ManVoice.this, AddComment.class);
                    addCommentIntent.putExtra("resId", _resId);
                    addCommentIntent.putExtra("comments", lastestComments);
                    addCommentIntent.putExtra("item_type", "THOUGHT");
                    addCommentIntent.putExtra("item_id", id);
                    startActivityForResult(addCommentIntent, COMMENT);
                }
                else {
                    Intent myIntent = getIntent();
                    myIntent.putExtra("startActivity", "profile");
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COMMENT && resultCode == RESULT_OK) {
            try {
                reloadLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private void setSharedContent(JSONObject result) {
        try {
            TextView numLikesTV = (TextView)findViewById(R.id.textview_numLikes_com_voice_manvoice);
            numLikesTV.setText(result.getString("num_likes"));
        } catch (JSONException e) {
            e.printStackTrace();
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

    //TOOLS

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
        else {
            width = display.getWidth();
        }
        return width;
    }

    public String getCurrentDate() {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(today);
        if (todayDate != null && todayDate.length() > 0)
            return todayDate;
        return "";
    }

    private boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onMessageButtonClicked(View v) {
        Intent messagesIntent = new Intent(ManVoice.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}