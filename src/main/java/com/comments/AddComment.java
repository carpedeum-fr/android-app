package com.comments;

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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.user.MyProfile;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 24/09/13.
 * Ajouter un commentaire
 */
public class AddComment extends Activity implements ApiCaller {

    private int _resId = -1;
    private ProgressDialog _progressDialog = null;
    private String _itemType = null;
    private String _itemId = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;
    private String TAG = "AddComment";
    private int _type = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_comments_add_comment);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _itemType = extras.getString("item_type");
            _itemId = extras.getString("item_id");
            _progressDialog = new ProgressDialog(this);
            _progressDialog.setMessage(getString(R.string.ChargementEnCours));
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            EditText commentED = (EditText)findViewById(R.id.editText_comment_com_comments_add_comment);
            if (extras.containsKey("type") && extras.getString("type").equals("priere")) {
                titleTV.setText(getResources().getString(R.string.COMMENTS_PRAYER_HEADER));
                commentED.setHint(getResources().getString(R.string.COMMENTS_PLACEHOLDER_TEXT_PRAYER));
                _type = 1;
            }
            else {
                titleTV.setText(getResources().getString(R.string.COMMENTS));
                commentED.setHint(getResources().getString(R.string.COMMENTS_PLACEHOLDER_TEXT));
            }

            downloadComments();
            //displayCommentSection(comments);
            try {
                getAdsFromAdsManager();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getAdsFromAdsManager() throws Exception {
        final JSONObject ads = ManageAds.getInstance().getAdd();
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
                        Intent premiumIntent = new Intent(AddComment.this, Premium.class);
                        premiumIntent.putExtra("resId", _resId);
                        startActivity(premiumIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void downloadComments() {
        if (UserConnected.getInstance().IsUserConnected() && internetConnectionOk()) {
            _progressDialog.show();
            if (_args == null)
                _args = new ArrayList<NameValuePair>();
            _args.clear();
            _args.add(new BasicNameValuePair("item_type", _itemType));
            _args.add(new BasicNameValuePair("item_id", _itemId));
            _args.add(new BasicNameValuePair("mode", "all"));
            _args.add(new BasicNameValuePair("limit", Tools.CDCOMMENTSLIMIT));
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _apiCaller = new HttpApiCall(this, _args, 2);
            _apiCaller.execute(Tools.API + Tools.CDCOMMENTSGET);
            _apiCaller = null;
        }
    }

    private void displayCommentSection(String comments) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject commentObj = new JSONObject(comments);
        if (commentObj.getInt("num") > 0 && commentObj.has("comments")) {
            TextView noCommentsTV = (TextView)findViewById(R.id.textView_nocomments_com_comments_add_comment);
            noCommentsTV.setVisibility(View.GONE);
            JSONArray commentsArray = new JSONArray(commentObj.getString("comments"));
            displayComments(commentsArray);
        }
        else {
            TextView noCommentTv = (TextView)findViewById(R.id.textView_nocomments_com_comments_add_comment);
            noCommentTv.setVisibility(View.VISIBLE);
        }
        Button sendComment = (Button)findViewById(R.id.button_send_com_comments_add_comment);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });
    }

    private void sendComment() {
        EditText commentED = (EditText)findViewById(R.id.editText_comment_com_comments_add_comment);
        if (commentED.getText() != null && commentED.getText().toString().length() > 0) {
            if (internetConnectionOk()) {
                _progressDialog.show();
                if (_args == null)
                    _args = new ArrayList<NameValuePair>();
                _args.clear();
                _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                _args.add(new BasicNameValuePair("item_type", _itemType));
                _args.add(new BasicNameValuePair("item_id", _itemId));
                _args.add(new BasicNameValuePair("text", commentED.getText().toString()));
                _apiCaller = new HttpApiCall(this, _args, 1);
                _apiCaller.execute(Tools.API + Tools.CDCOMMENTSADD);
                _apiCaller = null;
            }

            else {
                //TODO message erreur pas de connexion internet
            }
        }
        else {

        }
    }

    private void displayComments(JSONArray commentsArray) {
        try {
            LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_comments_add_comment);
            commentsLL.removeAllViews();
            for (int i = 0; i < commentsArray.length(); ++i) {
                final JSONObject commentObj = commentsArray.getJSONObject(i);
                LinearLayout commentLL = new LinearLayout(this);
                commentLL.setOrientation(LinearLayout.HORIZONTAL);
                ImageView profileIV = getProfileImageView(commentObj.getString("profilepic"));
                if (profileIV != null) {
                    LinearLayout.LayoutParams imageViewLP = new LinearLayout.LayoutParams(Tools.STD_W / 6, Tools.STD_W / 6);
                    imageViewLP.setMargins(5, 5, 5, 5);
                    commentLL.addView(profileIV, imageViewLP);
                    profileIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                setCommentLLOnClickListener(commentObj.getString("profileid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                String text = "<small><font color='#888888'>" + commentObj.getString("profilename") + ", " + commentObj.getString("dateinfo") + "</small><br/><br/>";
                text += "<font color='#000000'>" + commentObj.getString("text") + "</font>";
                TextView commentTV = new TextView(this);
                commentTV.setText(Html.fromHtml(text));
                commentTV.setPadding(5, 0, 5, 5);
                commentTV.setTextSize(16);
                commentLL.addView(commentTV, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 80));

                if (commentObj.getString("profileid").equals(UserConnected.getInstance().get_uid())) {
                    TextView deleteComTV = new TextView(this);
                    deleteComTV.setText("x");
                    deleteComTV.setTextColor(getResources().getColor(R.color.red));
                    LinearLayout.LayoutParams deleteLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5);
                    deleteLP.gravity = Gravity.RIGHT | Gravity.TOP;
                    commentLL.addView(deleteComTV, deleteLP);
                    deleteComTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                deleteOnClickListener(commentObj.getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
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

    private void deleteOnClickListener(final String id) {
        AlertDialog.Builder deleteAD = new AlertDialog.Builder(AddComment.this);
        deleteAD.setTitle(getString(R.string.COMMENTS_REMOVE));
        if (_type == 1)
            deleteAD.setMessage(getString(R.string.COMMENTS_REMOVE_CONFIRM_PRAYER));
        else
            deleteAD.setMessage(getString(R.string.COMMENTS_REMOVE_CONFIRM));
        deleteAD.setCancelable(false);
        deleteAD.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        deleteAD.setPositiveButton(getString(R.string.DELETE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendDeleteAction(id);
            }
        });
        deleteAD.create().show();
    }

    private void sendDeleteAction(String id) {
        if (_args == null)
            _args = new ArrayList<NameValuePair>();
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", id));
        _apiCaller = new HttpApiCall(this, _args, 3);
        _apiCaller.execute(Tools.API + Tools.CDCOMMENTSREMOVE);
    }

    private void setCommentLLOnClickListener(String profileId) {
        Intent profileIntent = new Intent(AddComment.this, MyProfile.class);
        profileIntent.putExtra("resId", _resId);
        profileIntent.putExtra("profileId", profileId);
        startActivity(profileIntent);
    }

    private ImageView getProfileImageView(String imageURL) {
        ImageView profileIV = new ImageView(getApplicationContext());

        int size = Tools.STD_W / 6;

        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        if (imageURL != null && imageURL.length() > 0 && internetConnectionOk()) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imageURL + size);
            if (cachedImage == null && internetConnectionOk()) {
                new DownloadImages(profileIV, true, imageURL + size).execute(Tools.MEDIAROOT + imageURL, String.valueOf(size));
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
        Intent messagesIntent = new Intent(AddComment.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();
        if (type == 1 || type == 3) {
            JSONObject resultObj = new JSONObject(result);
            if (!resultObj.has("ok") || !resultObj.getString("ok").equals("1")) {
                //TODO message erreur commentaire non ajouté
                finish();
            }
            else {
                Intent myIntent = getIntent();
                setResult(RESULT_OK, myIntent);
                finish();
            }
        }
        else if (type == 2)
            displayCommentSection(result);
    }
}