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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 29/09/13.
 * Create a classified (une annonce)
 */

public class CreateClassified extends Activity implements ApiCaller {

    private int _resId = -1;
    private String _type = null;
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
        setContentView(R.layout.com_classifieds_create_classified);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        _photoManager = new PhotoManager(this);
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.HOME_NUM_CLASSIFIEDS));
            setChoiceButtonsOnClickListeners();
        }
    }

    private void setChoiceButtonsOnClickListeners() {
        //TODO faire un truc plus propre

        Button tipBtn = (Button)findViewById(R.id.button_tip_com_classified_create_classified);
        tipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = "tip";
                displayFormCreateClassified();
            }
        });
        Button noticeBtn = (Button)findViewById(R.id.button_notice_com_classified_create_classified);
        noticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = "notice";
                displayFormCreateClassified();
            }
        });
        Button eventBtn = (Button)findViewById(R.id.button_event_com_classified_create_classified);
        eventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = "event";
                displayFormCreateClassified();
            }
        });
        Button miscBtn = (Button)findViewById(R.id.button_misc_com_classified_create_classified);
        miscBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = "misc";
                displayFormCreateClassified();
            }
        });
        Button forsaleBtn = (Button)findViewById(R.id.button_forsale_com_classified_create_classified);
        forsaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = "forsale";
                displayFormCreateClassified();
            }
        });
        Button jobsBtn = (Button)findViewById(R.id.button_jobs_com_classified_create_classified);
        jobsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = "jobs";
                displayFormCreateClassified();
            }
        });
        Button housingBtn = (Button)findViewById(R.id.button_housing_com_classified_create_classified);
        housingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = "housing";
                displayFormCreateClassified();
            }
        });
    }

    private void displayFormCreateClassified() {
        setContentView(R.layout.com_classifieds_edit_classified);
        Button saveBtn = (Button)findViewById(R.id.button_save_com_classified_edit_classified);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConnectionOk())
                    sendNewClassified();
                else {
                    //TODO message erreur pas internet
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
            if (mImageView == null)
                Log.d("1", "null");
            if (mImageBitmap == null)
                Log.d("2", "null");
            mImageView.setImageBitmap(mImageBitmap);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    private void sendNewClassified() {
        EditText titleED = (EditText)findViewById(R.id.editText_title_com_classified_edit_classified);
        EditText textED = (EditText)findViewById(R.id.editText_text_com_classified_edit_classified);
        CheckBox isPrivateCB = (CheckBox)findViewById(R.id.checkBox_private_com_classified_edit_classified);
        CheckBox allowCommentCB = (CheckBox)findViewById(R.id.checkBox_allow_comment_com_classified_edit_classified);
        if (titleED != null && titleED.getText() != null && textED != null && textED.getText() != null) {
            _args.clear();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("id", ""));
            _args.add(new BasicNameValuePair("type", _type));
            _args.add(new BasicNameValuePair("image", _photoPath));
            _args.add(new BasicNameValuePair("title", titleED.getText().toString()));
            _args.add(new BasicNameValuePair("text", textED.getText().toString()));
            _args.add(new BasicNameValuePair("private", String.valueOf(isPrivateCB.isChecked())));
            _args.add(new BasicNameValuePair("allow_comments", String.valueOf(allowCommentCB.isChecked())));
            //TODO coordonnes GPS
            if (Tools.CDDEBUG)
                Log.d("CreateClassified::sendNewClassified::", "creating classified with type :" + _type + ", title :" +
                        titleED.getText().toString() + ", text : " + textED.getText().toString() + ", private ? " +
                        isPrivateCB.isChecked() + ", allowComments ? " + allowCommentCB.isChecked());
            _apiCaller = new HttpApiCall(this, _args, 1);
            _apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSEDIT);
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
        JSONObject resultObj = new JSONObject(result);
        if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
            if (type == 1) {
                Intent myIntent = getIntent();
                setResult(RESULT_OK, myIntent);
                finish();
            }
            else if (type == 2) {
                Toast.makeText(this, getString(R.string.SENDED_PHOTO), Toast.LENGTH_SHORT).show();
                _sending = false;
                _photoPath = resultObj.getString("path");
            }
        }
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(CreateClassified.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }
}