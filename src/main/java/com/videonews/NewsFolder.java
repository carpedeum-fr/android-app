package com.videonews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsFolder extends Activity implements ApiCaller {

    private int _resId = -1;
    public ProgressDialog _progressDialog = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;
    private LinearLayout _videosLinearLayout;
    private int _drawableArrowID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_news_folder);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            _videosLinearLayout = (LinearLayout)findViewById(R.id.linearLayout_videos_com_news_folder);
            if (extras.containsKey("id"))
                downloadNewsFolder(extras.getString("id"));
        }
    }

    private void downloadNewsFolder(String id) {
        if (_args == null)
            _args = new ArrayList<NameValuePair>();
        _args.clear();
        _args.add(new BasicNameValuePair("folder", id));
        _apiCaller = new HttpApiCall(this, _args, 1);
        _apiCaller.execute(Tools.API + Tools.CDNEWSLIST);
        _progressDialog.show();
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
            JSONArray homeVideosInfos = new JSONArray(resObj.getString("results"));
            for (int i = 0; i < homeVideosInfos.length(); ++i) {
                JSONObject homeVideoInfos = homeVideosInfos.getJSONObject(i);
                if (homeVideoInfos.getString("has_video").equals("1")) {
                    String imgUrl = Tools.MEDIAROOT + homeVideoInfos.getString("image");
                    String text = homeVideoInfos.getString("title");
                    addNewVideo(imgUrl, text, homeVideoInfos.getString("id"));
                }
            }
        }
    }

    public void addNewVideo(String imgUrl, String text, final String id) {
        TextView loadingTV = (TextView)findViewById(R.id.textView_loading_videos_com_carpedeum_aujourdhui);
        if (loadingTV != null)
            loadingTV.setVisibility(View.GONE);
        LinearLayout imageVideoLL = new LinearLayout(this);
        imageVideoLL.setOrientation(LinearLayout.HORIZONTAL);
        ImageView videoImageIV = new ImageView(this);
        int size = getScreenWidth() / 4;
        if (size > Tools.MAX_IMAGE_SIZE)
            size = Tools.MAX_IMAGE_SIZE;

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
        imageVideoLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoNewsIntent = new Intent(NewsFolder.this, VideoNews.class);
                videoNewsIntent.putExtra("id", id);
                videoNewsIntent.putExtra("resId", _resId);
                startActivityForResult(videoNewsIntent, 12);
            }
        });
        LinearLayout.LayoutParams imageVideoLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageVideoLP.setMargins(5, 5, 0, 5);
        _videosLinearLayout.addView(imageVideoLL, imageVideoLP);
        LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        View separatorView = new View(this);
        separatorView.setBackgroundResource(R.color.gris80);
        _videosLinearLayout.addView(separatorView, layoutparamsSeparator);
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

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(NewsFolder.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}
