package com.videonews;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.Network.ApiCaller;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.ads.ManageAds;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.i2heaven.carpedeum.R;
import com.comments.AddComment;
import com.messages.DisplayMessages;
import com.user.MyProfile;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.videonews.vimeo.HTML5WebView;
import com.videonews.vimeo.PlayVideo;
import com.videonews.youtube.DeveloperKey;
import com.videonews.youtube.YouTubeFailureRecoveryActivity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Guillaume on 20/08/13.
 * Video News
 */

public class VideoNews extends YouTubeBaseActivity implements ApiCaller, YouTubePlayer.OnInitializedListener {
    private int _resId = -1;
    private String _videoID = null;
    private String TAG = "VideoNews";
    public ProgressDialog _progressDialog = null;
    private int _drawableLikeID = -1;
    private int _header;

    public static String VIDEO_ID = "";
    private static boolean fullscreen;
    private YouTubePlayer player;
    private YouTubePlayerView playerView;

    private View _menuLayout = null;

    private int YOUTUBE = 1;
    private int VIMEO = 2;
    private int DAILYMOTION = 3;
    private int TYPE = 0;

    private String _imgURL = null;

    private RelativeLayout _headerLayout = null;

    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        _progressDialog.setCancelable(true);
        _progressDialog.show();
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _imgURL = extras.getString("image");
            setHeader();
            if (extras.containsKey("id")) {
                _videoID = extras.getString("id");
                callApiForNews();
            }
        }
        /*try {
            ManageAds.displayAdds(this, getScreenWidth(), false, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void setHeader() {
        Log.d(TAG, "setHeader::" + fullscreen);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.com_videonews);
        if (!fullscreen) {
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            if (imageLogoIV != null)
                imageLogoIV.setVisibility(View.INVISIBLE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            if (titleTV != null) {
                titleTV.setVisibility(View.VISIBLE);
                titleTV.setText(getResources().getString(R.string.leKiosque));
            }
        }
    }

    private void callApiForNews() {
        if (Tools.CDDEBUG)
            Log.d("VideosNews::id::", _videoID);
        if (_args == null)
            _args = new ArrayList<NameValuePair>();
        _args.clear();
        _args.add(new BasicNameValuePair("id", _videoID));
        if (UserConnected.getInstance().IsUserConnected()) {
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        }
        _apiCaller = new HttpApiCall(this, _args, 1);
        _apiCaller.execute(Tools.API + Tools.CDNEWSGET);
        _apiCaller = null;
    }

    private void manageLikes(final JSONObject classifiedObj) {
        try {
            ImageView iLikeIV = (ImageView)findViewById(R.id.imageView_like_com_videonews);
            if (classifiedObj.getString("i_like").equals("1"))
                iLikeIV.setImageResource(_drawableLikeID);
            else
                iLikeIV.setImageResource(R.drawable.footer_like);
            iLikeIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!UserConnected.getInstance().IsUserConnected()) {
                        Intent myIntent = getIntent();
                        myIntent.putExtra("startActivity", "profile");
                        setResult(RESULT_OK, myIntent);
                        finish();
                    }
                    else {
                        try {
                            String url;
                            if (classifiedObj.getString("i_like").equals("1"))
                                url = Tools.API + Tools.CDLIKESREMOVE;
                            else
                                url = Tools.API + Tools.CDLIKESADD;
                            _progressDialog.show();
                            if (_args == null)
                                _args = new ArrayList<NameValuePair>();
                            _args.clear();
                            _args.add(new BasicNameValuePair("item_type", "NEWS"));
                            _args.add(new BasicNameValuePair("item_id", classifiedObj.getString("id")));
                            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                            _apiCaller = new HttpApiCall(VideoNews.this, _args, 2);
                            _apiCaller.execute(url);
                            _apiCaller = null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDateFromString(String oldDate) {
        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat myFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");
        String date = null;
        try {
            date = myFormat.format(fromUser.parse(oldDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void addComment(String id, String latestComments) {
        if (UserConnected.getInstance().IsUserConnected()) {
            Intent addCommentIntent = new Intent(VideoNews.this, AddComment.class);
            addCommentIntent.putExtra("resId", _resId);
            addCommentIntent.putExtra("comments", latestComments);
            addCommentIntent.putExtra("item_type", "NEWS");
            addCommentIntent.putExtra("item_id", id);
            startActivityForResult(addCommentIntent, 1);
        }
        else {
            Intent myIntent = getIntent();
            myIntent.putExtra("startActivity", "profile");
            setResult(RESULT_OK, myIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                callApiForNews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        if (type == 1) {
            if (Tools.CDDEBUG) Log.d(TAG, "OnAPIResult::" + result);
            final JSONObject videoNewsObj = new JSONObject(result);
            addVideoTitle(videoNewsObj.getString("title"));
            addVideoText(videoNewsObj.getString("html"));
            addDate(videoNewsObj.getString("date"));
            addVideoLikes(videoNewsObj.getString("num_likes"));
            addVideoCredits(videoNewsObj.getString("byline"));
            if (videoNewsObj.getInt("num_comments") > 0) {
                addVideoComments(videoNewsObj);
            }
            manageLikes(videoNewsObj);
            LinearLayout addCommentsLL = (LinearLayout)findViewById(R.id.linearLayout_addComment_com_videonews);
            addCommentsLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        addComment(videoNewsObj.getString("id"), videoNewsObj.getString("latest_comments"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (Tools.CDDEBUG) Log.d(TAG, "Video:: " + videoNewsObj.getString("video"));
            //TODO lecture en local
            addVideoButtonListener(videoNewsObj.getString("video"));
        }
        else if (type == 2) {
            callApiForNews();
        }
    }

    private void addDate(String date) {
        TextView dateTV = (TextView)findViewById(R.id.textView_date_com_videonews);
        dateTV.setText(getDateFromString(date.substring(0, date.indexOf(' '))));
    }


    private LinearLayout mContentView;
    private FrameLayout mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);


    private void addVideoButtonListener(final String _url) {
        int width = getScreenWidth();
        int height = (width * 3) / 4;

        ImageView videoIV = (ImageView)findViewById(R.id.imageView_com_videonews);
        WebView videoWebView = (WebView) findViewById(R.id.webView_com_videonews);

        playerView = (YouTubePlayerView)findViewById(R.id.youtubeplayerview);
        if (_url.contains("youtube")) {
            videoWebView.setVisibility(View.GONE);
            TYPE = YOUTUBE;
            if (_url.contains("#")) {
                //TODO seek to
                VIDEO_ID = _url.substring(_url.indexOf(":") + 1, _url.indexOf("#"));
            }
            else {
                VIDEO_ID = _url.substring(_url.indexOf(":") + 1, _url.length());
            }

            playerView.getLayoutParams().height = height;
            playerView.getLayoutParams().width = width;
            playerView.setVisibility(View.VISIBLE);
            playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);
            videoIV.setVisibility(View.GONE);


        }
        else {
            playerView.setVisibility(View.GONE);
            if (_url.contains("vimeo")) {


                Log.d("URL : ", "" + _url);

                videoIV.setVisibility(View.VISIBLE);
                videoIV.getLayoutParams().height = height;

                if (_imgURL != null) {
                    Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(_imgURL + width);
                    if (cachedImage == null) {
                        new DownloadImages(videoIV, true, _imgURL + width).execute(_imgURL, String.valueOf(width));
                    }
                    else {
                        videoIV.setImageBitmap(cachedImage);
                        videoIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }

                videoIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent playViIntent = new Intent(VideoNews.this, PlayVideo.class);
                        playViIntent.putExtra("url", _url.substring(_url.indexOf(":") + 1, _url.length()));
                        startActivity(playViIntent);
                    }
                });
            }
            else if (_url.contains("dailymotion")) {
                videoIV.setVisibility(View.VISIBLE);
                videoWebView.setVisibility(View.GONE);
                videoIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openPageIntent = new Intent(Intent.ACTION_VIEW);
                        String url = "http://www.dailymotion.com/embed/video/" + _url.substring(_url.indexOf(":") + 1, _url.length());
                        openPageIntent.setData(Uri.parse(url));
                        startActivity(openPageIntent);
                    }
                });
                /* videoIV.setVisibility(View.GONE);
                videoWebView.setVisibility(View.VISIBLE);
                videoWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                videoWebView.getSettings().setBuiltInZoomControls(true);
                videoWebView.setWebViewClient(new GeoWebViewClient());
                videoWebView.getSettings().setJavaScriptEnabled(true);
                videoWebView.getSettings().setGeolocationEnabled(true);
                videoWebView.setWebChromeClient(new GeoWebChromeClient());


                //String url = "<iframe src=\"http://www.dailymotion.com/embed/video/" + _url.substring(_url.indexOf(":") + 1, _url.length()) + "?\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\"></iframe>";
                //videoWebView.loadData(url, "text/html; charset=UTF-8", null);
                videoWebView.loadUrl("http://www.dailymotion.com/embed/video/" + _url.substring(_url.indexOf(":") + 1, _url.length()));*/
            }
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {

        this.player = player;

        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);

        /** Start buffering **/
        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }

        /*player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        player.setOnFullscreenListener(this);
        if (!wasRestored) {
            Log.d(TAG, "VIDEOID::" + VIDEO_ID);
            player.cueVideo(VIDEO_ID);
        }*/
    }

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onPlaying() {

        }

        @Override
        public void onSeekTo(int arg0) {

        }

        @Override
        public void onStopped() {

        }

    };

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason arg0) {

        }

        @Override
        public void onLoaded(String arg0) {

        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onVideoStarted() {

        }
    };



    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, "ERROR YOUTUBE", Toast.LENGTH_LONG).show();
    }

    private void doLayout() {
        TextView dateTV = (TextView)findViewById(R.id.textView_date_com_videonews);
        TextView titleTV = (TextView)findViewById(R.id.textView_videoTitle_com_videonews);
        WebView webView = (WebView)findViewById(R.id.webView_com_videonews);
        LinearLayout videonewsLL = (LinearLayout)findViewById(R.id.linearLayout_content_com_videonews);
        ImageView shadowIV = (ImageView)findViewById(R.id.bottomshadow);
        ImageView addsIV = (ImageView)findViewById(R.id.adds);
        LinearLayout.LayoutParams playerParams = (LinearLayout.LayoutParams) playerView.getLayoutParams();
        if (fullscreen) {
            assert playerParams != null;
            playerParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            playerParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            dateTV.setVisibility(View.GONE);
            titleTV.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            videonewsLL.setVisibility(View.GONE);
            shadowIV.setVisibility(View.GONE);
            addsIV.setVisibility(View.GONE);
            //_menuLayout.setVisibility(View.GONE);
            //RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.relativeLayout_com_videonews);
            /*LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            _menuLayout = inflater.inflate(_header, null);*/
            //findViewById(R.id.header_include).setVisibility(View.GONE);
            _headerLayout.setVisibility(View.GONE);
        }
        else {
            //findViewById(R.id.header_include).setVisibility(View.VISIBLE);
            _headerLayout.setVisibility(View.VISIBLE);
            dateTV.setVisibility(View.VISIBLE);
            titleTV.setVisibility(View.VISIBLE);
            videonewsLL.setVisibility(View.VISIBLE);
            shadowIV.setVisibility(View.VISIBLE);
            addsIV.setVisibility(View.VISIBLE);
        }
    }



    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtubeplayerview);
    }

    public void onFullscreen(boolean isFullscreen) {
        fullscreen = isFullscreen;
        Log.d(TAG, "onFullscreen::" + isFullscreen);
        doLayout();
    }

    private void addVideoComments(JSONObject videoNewsObj) {
        try {
            LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_videonews);
            commentsLL.removeAllViews();
            JSONArray commentsArray = new JSONArray(videoNewsObj.getString("latest_comments"));
            for (int i = 0; i < commentsArray.length(); ++i) {
                final JSONObject commentObj = commentsArray.getJSONObject(i);
                LinearLayout commentLL = new LinearLayout(this);
                commentLL.setOrientation(LinearLayout.HORIZONTAL);
                ImageView profileIV = getProfileImageView(commentObj.getString("profilepic"), 6);
                if (profileIV != null) {
                    profileIV.setPadding(0, 0, 5, 0);
                    commentLL.addView(profileIV);
                }
                String text = "<small><font color='#888888'>" + commentObj.getString("profilename") + ", " + commentObj.getString("dateinfo") + "</small><br/><br/>";
                text += "<font color='#000000'>" + commentObj.getString("text") + "</font>";
                TextView commentTV = new TextView(this);
                commentTV.setText(Html.fromHtml(text));
                commentTV.setTextSize(16);
                commentLL.addView(commentTV, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60));
                commentLL.setPadding(5, 5, 5, 5);
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
                setShareOnClickListeners(videoNewsObj.getString("title"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setShareOnClickListeners(final String title) {
        TextView shareTV = (TextView)findViewById(R.id.textView_share);
        shareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareVideo(title);
            }
        });
        ImageView shareIV = (ImageView)findViewById(R.id.imageView_shareicon);
        shareIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareVideo(title);
            }
        });
    }

    private void shareVideo(String title) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = Tools.CDMOBILEROOT + Tools.CDDEFAULTLANG + Tools.CDNEWS + Tools.CDARTICLE + _videoID + " " + title;
        if (Tools.CDDEBUG)
            Log.d("ManVoice::shareManVoice::text", text);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(text));
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.PartagerVia)));
    }

    private void setCommentLLOnClickListener(String profileId) {
        Intent profileIntent = new Intent(VideoNews.this, MyProfile.class);
        profileIntent.putExtra("resId", _resId);
        profileIntent.putExtra("profileId", profileId);
        startActivity(profileIntent);
    }

    private void addVideoCredits(String byline) {
        TextView creditsTV = (TextView)findViewById(R.id.textView_textCredits_com_videonews);
        creditsTV.setText(Html.fromHtml(byline));
    }


    private void addVideoLikes(String num_likes) {
        TextView numLikesTV = (TextView)findViewById(R.id.textview_numLikes_com_videonews);
        numLikesTV.setText(num_likes);
    }

    private void addVideoText(String text) {
        TextView videoTextTV = (TextView)findViewById(R.id.textView_videoText_com_videonews);
        videoTextTV.setText(Html.fromHtml(text));
        videoTextTV.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void addVideoTitle(String title) {
        TextView videoTitleTV = (TextView)findViewById(R.id.textView_videoTitle_com_videonews);
        videoTitleTV.setText(Html.fromHtml(title));
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

    private boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private ImageView getProfileImageView(String imageURL, int divider) {
        ImageView profileIV = new ImageView(getApplicationContext());
        int size = getScreenWidth() / divider;
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        if (imageURL != null && imageURL.length() > 0 && internetConnectionOk()) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imageURL + getScreenWidth() / 6);
            if (cachedImage == null && internetConnectionOk()) {
                new DownloadImages(profileIV, true, imageURL + getScreenWidth() / 6).execute(Tools.MEDIAROOT + imageURL, String.valueOf(getScreenWidth() / 6));
            }
            else {
                profileIV.setImageBitmap(cachedImage);
                profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
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
        if (_resId == 0 || _resId == -1) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _header = R.layout.header;
            _drawableLikeID = R.drawable.footer_like_on;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include);
        }
        else if (_resId == 1) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _header = R.layout.header_blue;
            _drawableLikeID = R.drawable.footer_like_on_blue;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_blue);
        }
        else if (_resId == 2) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _header = R.layout.header_gold;
            _drawableLikeID = R.drawable.footer_like_on_gold;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_gold);
        }
        else if (_resId == 3) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _header = R.layout.header_green;
            _drawableLikeID = R.drawable.footer_like_on_green;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_green);
        }
        else if (_resId == 4) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _header = R.layout.header_mauve;
            _drawableLikeID = R.drawable.footer_like_on_mauve;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_mauve);
        }
        else if (_resId == 5) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _header = R.layout.header_orange;
            _drawableLikeID = R.drawable.footer_like_on_orange;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_orange);
        }
        else if (_resId == 6) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _header = R.layout.header_purple;
            _drawableLikeID = R.drawable.footer_like_on_purple;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_purple);
        }
        else if (_resId == 7) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _header = R.layout.header_red;
            _drawableLikeID = R.drawable.footer_like_on_red;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_red);
        }
        else if (_resId == 8) {
            //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _header = R.layout.header_silver;
            _drawableLikeID = R.drawable.footer_like_on_silver;
            _headerLayout = (RelativeLayout)findViewById(R.id.header_include_silver);
        }
        _headerLayout.setVisibility(View.VISIBLE);
        /*RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.relativeLayout_com_videonews);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _menuLayout = inflater.inflate(_header, mainLayout, true);*/
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(VideoNews.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE  && !fullscreen && TYPE == YOUTUBE)
        {
            player.setFullscreen(!fullscreen);
        }
        if (TYPE == YOUTUBE)
            doLayout();
    }
}