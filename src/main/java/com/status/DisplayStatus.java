package com.status;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.comments.AddComment;
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
 * Created by Guillaume on 15/09/13.
 * Display Status
 */

public class DisplayStatus extends Activity {

    private int _resId = -1;
    private ProgressDialog _progressDialog = null;
    private int _drawableLikeID = -1;
    private String _statusId = null;
    private static int COMMENT = 12;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_status_display_status);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.VIEW_TITLE_STATUS));
            _statusId = extras.getString("statusId");
            getStatusFromApi(extras.getString("statusId"));
        }
    }

    private void getStatusFromApi(String id) {
        if (Tools.CDDEBUG)
            Log.d("DisplayStatus::getStatusFromApi::id::", id);
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        _progressDialog.show();
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", id));
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
        String url = Tools.API + Tools.CDSTATUSGET;
        apiCaller.execute(url);
    }

    public void onApiResult(String result, int type) {
        if (_progressDialog != null)
            _progressDialog.cancel();
        if (result != null && result.length() > 0) {
            try {
                if (type == 1) {
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                        displayStatus(resultObj);
                    }
                    else {
                        displayError("Error");
                    }
                }
                else if (type == 3) {
                    getStatusFromApi(_statusId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                displayError("Error");
            }
        }
        else
            displayError("Error");
    }

    private void displayStatus(final JSONObject resultObj) {
        try {
            TextView statusInfosTV = (TextView)findViewById(R.id.textView_statusInfos_com_status_display_status);
            statusInfosTV.setText(resultObj.getString("profilename"));
            TextView statusDateInfosTV = (TextView)findViewById(R.id.textView_statusDateInfos_com_status_display_status);
            statusDateInfosTV.setText(resultObj.getString("dateinfo"));
            setProfilePic(resultObj.getString("profileimage"));
            TextView textTV = (TextView)findViewById(R.id.textView_status_com_status_display_status);
            if (Tools.CDDEBUG)
                Log.d("DisplayStatus::displayStatus::", resultObj.getString("text"));
            textTV.setText(resultObj.getString("text"));
            manageLikes(resultObj);
            setComments(resultObj);
            TextView numLikes = (TextView)findViewById(R.id.textview_numLikes_com_status_display_status);
            numLikes.setText(resultObj.getString("num_likes"));
            Button deleteBtn = (Button)findViewById(R.id.button_delete_status_com_status_display_status);
            if (resultObj.getString("accountid").equals(UserConnected.getInstance().get_uid())) {
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            confirmDeleteStatus(resultObj.getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            else {
                deleteBtn.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == COMMENT && resultCode == RESULT_OK) {
            if (Tools.CDDEBUG) {
                Log.d("DisplayStatus::onActivityResult::Commentaire ajouté !", "On reload la page.");
            }
            getStatusFromApi(_statusId);
        }
    }

    private void setComments(JSONObject result) throws JSONException {
        LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_status_display_status);
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
            if (result.getInt("num_comments") > 4) {
                moreComments(commentsArray.length(), result.getString("latest_comments"), result.getString("id"));
            }
        }
    }

    private void setCommentOnClickListener(final String lastestComments, final String id) {
        TextView addCommentTv = (TextView)findViewById(R.id.textView_addComment_com_status_display_status);
        addCommentTv.setVisibility(View.VISIBLE);
        addCommentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserConnected.getInstance().IsUserConnected()) {
                    Intent addCommentIntent = new Intent(DisplayStatus.this, AddComment.class);
                    addCommentIntent.putExtra("resId", _resId);
                    addCommentIntent.putExtra("comments", lastestComments);
                    addCommentIntent.putExtra("item_type", "STATUS");
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

    private ImageView getProfileImageView(String profilepic) {
        if (internetConnectionOk()) {
            ImageView profilePicIV = new ImageView(this);
            new DownloadImageTask(profilePicIV, true).execute(Tools.MEDIAROOT + profilepic);
            return profilePicIV;
        }
        return null;
    }

    private void setCommentLLOnClickListener(String profileId) {
        if (Tools.CDDEBUG)
            Log.d("DisplayStatus::setCommentLLOnClickListener::profileId::", profileId);
        Intent profileIntent = new Intent(DisplayStatus.this, MyProfile.class);
        profileIntent.putExtra("resId", _resId);
        profileIntent.putExtra("profileId", profileId);
        startActivity(profileIntent);
    }

    private void moreComments(int totalComments, final String lastestComments, final String id) {
        LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_status_display_status);
        int restComments = totalComments - 4;
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
                    Intent addCommentIntent = new Intent(DisplayStatus.this, AddComment.class);
                    addCommentIntent.putExtra("resId", _resId);
                    addCommentIntent.putExtra("comments", lastestComments);
                    addCommentIntent.putExtra("item_type", "STATUS");
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

    private void manageLikes(final JSONObject classifiedObj) {
        try {
            ImageView iLikeIV = (ImageView)findViewById(R.id.imageView_like_com_status_display_status);
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
                            List<NameValuePair> _args = new ArrayList<NameValuePair>();
                            String url;
                            _args.add(new BasicNameValuePair("item_type", "STATUS"));
                            _args.add(new BasicNameValuePair("item_id", classifiedObj.getString("id")));
                            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                            if (classifiedObj.getString("i_like").equals("1"))
                                url = Tools.API + Tools.CDLIKESREMOVE;
                            else
                                url = Tools.API + Tools.CDLIKESADD;
                            _progressDialog.show();
                            HttpApiCall apiCaller = new HttpApiCall(DisplayStatus.this, _args, 3);
                            apiCaller.execute(url);
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

    private void confirmDeleteStatus(final String id) {
        AlertDialog.Builder deleteAD = new AlertDialog.Builder(DisplayStatus.this);
        deleteAD.setTitle(getString(R.string.PRAYER_DELETE));
        deleteAD.setMessage(getString(R.string.PRAYER_DELETE_CONFIRM));
        deleteAD.setCancelable(false);
        deleteAD.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        deleteAD.setPositiveButton(getString(R.string.DELETE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStatus(id);
            }
        });
        deleteAD.create().show();
    }

    private void deleteStatus(String id) {
        if (Tools.CDDEBUG)
            Log.d("DisplayStatus::deleteStatus::", "deleting status with id::" + id);
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", id));
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 2);
        String url = Tools.API + Tools.CDSTATUSREMOVE;
        apiCaller.execute(url);
    }

    public void deletedStatus(String result) {
        if (result != null && result.length() > 0) {
            try {
                JSONObject resObj = new JSONObject(result);
                if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                    if (Tools.CDDEBUG)
                        Log.d("DisplayStatus::deletedStatus::", "ok");
                    Intent myIntent = getIntent();
                    myIntent.putExtra("reload", "yes");
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
                else {
                    displayError("Error while deleting status");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            displayError("Error while deleting status");
        }
    }

    private void setProfilePic(String profilepicURL) {
        if (profilepicURL != null && profilepicURL.length() > 0) {
            ImageView profilePicIV = (ImageView)findViewById(R.id.imageView_profilepic_com_status_display_status);
            int size = getScreenWidth() / 7;
            LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
            profilePicIV.setLayoutParams(profileLP);
            new DownloadImageTask(profilePicIV, true).execute(Tools.MEDIAROOT + profilepicURL);
        }
    }

    private void displayError(String error) {
        //TODO display error
        Log.d("DisplayStatus::Error::", error);
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

    private boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(DisplayStatus.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}