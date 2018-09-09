package com.classifieds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.HttpApiCall;
import com.Network.HttpApiImageCall;
import com.Tools.PhotoManager;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 28/09/13.
 * Editer une annonce
 */

public class EditClassified extends Activity implements ApiCaller {

    private int _resId = -1;
    private String _classifiedId = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;

    PhotoManager _photoManager;
    private final int CAMERA = 101;
    private final int TAKE_PIC = 102;
    boolean _sending = false;
    private String _photoPath = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_classifieds_edit_classified);
        _photoManager = new PhotoManager(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _classifiedId = extras.getString("id");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.HOME_NUM_CLASSIFIEDS));
            setClassifiedInfos(extras.getString("title"), extras.getString("text"), extras.getString("allow_comments"), extras.getString("private"));
        }
    }

    private void setClassifiedInfos(String title, String text, final String allowComment, String is_private) {
        //TODO et GPS
        final EditText titleED = (EditText)findViewById(R.id.editText_title_com_classified_edit_classified);
        titleED.setText(title);
        final EditText textED = (EditText)findViewById(R.id.editText_text_com_classified_edit_classified);
        textED.setText(text);
        final CheckBox allowCommentCB = (CheckBox)findViewById(R.id.checkBox_allow_comment_com_classified_edit_classified);
        final CheckBox gpsCB = (CheckBox)findViewById(R.id.checkBox_gps_com_classified_edit_classified);
        if (allowComment.equals("1"))
            allowCommentCB.setChecked(true);
        else
            allowCommentCB.setChecked(false);
        final CheckBox isPrivateCB = (CheckBox)findViewById(R.id.checkBox_private_com_classified_edit_classified);
        if (is_private.equals("1"))
            isPrivateCB.setChecked(true);
        else
            isPrivateCB.setChecked(false);
        Button saveBtn = (Button)findViewById(R.id.button_save_com_classified_edit_classified);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConnectionOk()) {
                    _args.clear();
                    _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                    _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                    _args.add(new BasicNameValuePair("id", _classifiedId));
                    _args.add(new BasicNameValuePair("image", _photoPath));
                    _args.add(new BasicNameValuePair("title", titleED.getText().toString()));
                    _args.add(new BasicNameValuePair("text", textED.getText().toString()));
                    if (isPrivateCB.isChecked())
                        _args.add(new BasicNameValuePair("private", "1"));
                    else
                        _args.add(new BasicNameValuePair("private", "0"));
                    if (allowCommentCB.isChecked())
                        _args.add(new BasicNameValuePair("allow_comments", "1"));
                    else
                        _args.add(new BasicNameValuePair("allow_comments", "0"));
                    if (gpsCB.isChecked()) {
                        _args.add(new BasicNameValuePair("geolat", UserConnected.getInstance().get_geolat()));
                        _args.add(new BasicNameValuePair("geolng", UserConnected.getInstance().get_geolng()));
                    }
                    else {
                        _args.add(new BasicNameValuePair("geolat", "0.00000"));
                        _args.add(new BasicNameValuePair("geolng", "0.00000"));
                    }
                    _apiCaller = new HttpApiCall(EditClassified.this, _args, 1);
                    _apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSEDIT);
                }
            }
        });
        Button addPhotoBtn = (Button)findViewById(R.id.button_add_photo_com_classified_edit_classified);
        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _photoManager.takePhoto();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA) {
                addPhotoOnLayout(_photoManager.getBitmapInGoodOrientation(BitmapFactory.decodeFile(PhotoManager._tempUrl, _photoManager.getBitmapOptionsForResiszing())));
            }
            else if (requestCode == TAKE_PIC) {
                addPhotoOnLayout(_photoManager.getBitmapInGoodOrientation(data));
            }
        }
    }

    private void addPhotoOnLayout(Bitmap mImageBitmap) {
        if (mImageBitmap != null) {
            _sending = true;
            if (_args == null)
                _args = new ArrayList<NameValuePair>();
            _args.clear();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("type", "photo"));
            _args.add(new BasicNameValuePair("temp", "0"));

            HttpApiImageCall apiCaller = new HttpApiImageCall(this, _args, 2);
            apiCaller.execute(Tools.API + Tools.CDUPLOAD, _photoManager.createRotatedImage(mImageBitmap));

            ImageView mImageView = (ImageView)findViewById(R.id.imageView_photo_com_classified_edit_classified);
            mImageView.setImageBitmap(mImageBitmap);
            mImageView.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onApiResult(String result, int type) throws Exception {
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
            if (type == 1) {
                Intent myIntent = getIntent();
                setResult(RESULT_OK, myIntent);
                finish();
            }
            else if (type == 2) {
                Toast.makeText(this, getString(R.string.SENDED_PHOTO), Toast.LENGTH_SHORT).show();
                _sending = false;
                _photoPath = resObj.getString("path");
            }
        }
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(EditClassified.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}