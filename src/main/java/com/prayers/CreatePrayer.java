package com.prayers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 29/08/13.
 * Create a prayer
 */

public class CreatePrayer extends Activity implements ApiCaller {

    private int _resId = -1;

    private final int CAMERA = 101;
    private final int TAKE_PIC = 102;
    private String _photoPath = "";
    PhotoManager _photoManager;

    boolean _sending = false;

    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    public ProgressDialog _progressDialog = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_prayers_create_prayer);
        _photoManager = new PhotoManager(this);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.VIEW_TITLE_MY_PRAYERS));
        }
        Button savePrayerBtn = (Button)findViewById(R.id.button_save_com_prayers_create_prayers);
        savePrayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_sending) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreatePrayer.this);
                    builder.setMessage(getString(R.string.SENDING_PHOTO));
                    builder.setCancelable(true);
                    builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    builder.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        savePrayer();
                        }
                    });
                    builder.create().show();
                }
                else {
                    savePrayer();
                }
            }
        });
        Button addPhotoBtn = (Button)findViewById(R.id.button_add_photo_com_prayers_create_prayer);
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

            HttpApiImageCall apiCaller = new HttpApiImageCall(this, _args, 1);
            apiCaller.execute(Tools.API + Tools.CDUPLOAD, _photoManager.createRotatedImage(mImageBitmap));

            ImageView mImageView = (ImageView)findViewById(R.id.imageView_photo_com_prayers_create_prayer);
            mImageView.setImageBitmap(mImageBitmap);
            mImageView.setVisibility(View.VISIBLE);
        }
    }


    private void savePrayer() {
        List<NameValuePair> _args = new ArrayList<NameValuePair>();

        EditText titleED = (EditText)findViewById(R.id.editText_title_com_prayers_create_prayer);
        EditText textED = (EditText)findViewById(R.id.editText_text_com_prayers_create_prayer);

        CheckBox allowComsCB = (CheckBox)findViewById(R.id.checkBox_allow_comment_com_prayers_create_prayer);
        CheckBox privateCB = (CheckBox)findViewById(R.id.checkBox_private_com_prayers_create_prayer);
        CheckBox gpsCB = (CheckBox)findViewById(R.id.checkBox_gps_com_prayers_create_prayer);
        if (titleED.getText() != null && titleED.getText().toString().length() > 0
                && textED.getText() != null && textED.getText().toString().length() > 0) {
            if (Tools.CDDEBUG)
                Log.d("CreatePrayer::savePrayer::title::", titleED.getText().toString() + "");
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("type", "pray"));
            _args.add(new BasicNameValuePair("image", _photoPath));
            _args.add(new BasicNameValuePair("title", titleED.getText().toString()));
            _args.add(new BasicNameValuePair("text", textED.getText().toString()));
            if (allowComsCB.isChecked())
                _args.add(new BasicNameValuePair("allow_comments", "1"));
            else
                _args.add(new BasicNameValuePair("allow_comments", "0"));
            if (privateCB.isChecked())
                _args.add(new BasicNameValuePair("private", "1"));
            else
                _args.add(new BasicNameValuePair("private", "0"));
            if (gpsCB.isChecked()) {
                _args.add(new BasicNameValuePair("geolat", UserConnected.getInstance().get_geolat()));
                _args.add(new BasicNameValuePair("geolng", UserConnected.getInstance().get_geolng()));
            }
            else {
                _args.add(new BasicNameValuePair("geolat", "0.00000"));
                _args.add(new BasicNameValuePair("geolng", "0.00000"));
            }
            HttpApiCall apiCaller = new HttpApiCall(this, _args, 2);
            apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSEDIT);
            Intent myIntent = getIntent();
            setResult(RESULT_OK, myIntent);
            finish();
        }
    }

    /*
    public void setApiResult(String result) {
        //TODO virer l'attente et afficher erreur si priere non envoyee
        if (result != null) {
            try {
                JSONObject resultObj = new JSONObject(result);
                if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                    Log.d("CreatePrayer::setApiResult::ok::", result);
                }
                else {
                    Log.d("CreatePrayer::setApiResult::", "error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
            Log.d("CreatePrayer::setApiResult::", "error");
        Intent myIntent = getIntent();
        setResult(RESULT_OK, myIntent);
        finish();
    }
    */

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        else if (_resId == 1)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
        else if (_resId == 2)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
        else if (_resId == 3)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
        else if (_resId == 4)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
        else if (_resId == 5)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
        else if (_resId == 6)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
        else if (_resId == 7)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
        else if (_resId == 8)
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(CreatePrayer.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok")) {
            if (type == 1) {
                Toast.makeText(this, getString(R.string.SENDED_PHOTO), Toast.LENGTH_SHORT).show();
                _sending = false;
                _photoPath = resObj.getString("path");
            }
        }
    }
}