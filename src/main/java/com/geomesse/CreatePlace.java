package com.geomesse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.HttpApiCall;
import com.Network.HttpApiImageCall;
import com.Tools.LogInFile;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Guillaume on 11/11/13.
 * Create place
 */

public class CreatePlace extends Activity implements ApiCaller {

    private int _resId = -1;
    private int CAMERA = 1;
    private int TAKE_PIC = 2;
    private String _tempUrl = "";
    private JSONObject _place = null;
    private static final String TAG = "CreatePlace";
    private HttpApiCall _apiCaller = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    public ProgressDialog _progressDialog = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_geomesse_create_place);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            if (extras.containsKey("place")) {
                try {
                    _place = new JSONObject(extras.getString("place"));
                    Log.d(TAG, "place : " + _place);
                    fillInfosOfPlace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setLayoutTheme();
        }
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.GONE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.Geomesse));
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        setButtonsOnClickListeners();
    }

    private void fillInfosOfPlace() throws Exception {
        EditText nameED = (EditText)findViewById(R.id.editText_name_com_geomesse_create_place);
        Spinner typeSpinner = (Spinner)findViewById(R.id.spinnerType_com_geomesse_create_place);
        EditText addrLine1 = (EditText)findViewById(R.id.editText_address_com_geomesse_create_place);
        EditText addrLine2 = (EditText)findViewById(R.id.editText_address2_com_geomesse_create_place);
        EditText postalED = (EditText)findViewById(R.id.editText_postal_com_geomesse_create_place);
        EditText cityED = (EditText)findViewById(R.id.editText_city_com_geomesse_create_place);
        EditText countryED = (EditText)findViewById(R.id.editText_country_com_geomesse_create_place);
        EditText telED = (EditText)findViewById(R.id.editText_tel_com_geomesse_create_place);
        EditText urlED = (EditText)findViewById(R.id.editText_url_com_geomesse_create_place);
        EditText sundayED = (EditText)findViewById(R.id.editText_sunday_mass_com_geomesse_create_place);
        EditText weekED = (EditText)findViewById(R.id.editText_week_mass_com_geomesse_create_place);
        EditText eucharistED = (EditText)findViewById(R.id.editText_eucharist_mass_com_geomesse_create_place);
        EditText notesED = (EditText)findViewById(R.id.editText_notes_mass_com_geomesse_create_place);
        EditText photosED = (EditText)findViewById(R.id.editText_photos_com_geomesse_create_place);
        EditText internalED = (EditText)findViewById(R.id.editText_internal_com_geomesse_create_place);

        JSONArray telArray = new JSONArray(_place.getString("tel"));
        telED.setText(telArray.getString(0));

        countryED.setText(_place.getString("address_country"));
        cityED.setText(_place.getString("address_city"));
        nameED.setText(_place.getString("name"));
        addrLine1.setText(_place.getString("address_1"));
        addrLine2.setText(_place.getString("address_2"));
        postalED.setText(_place.getString("address_zip"));
        urlED.setText(new JSONArray(_place.getString("url")).getString(0));

        notesED.setText(_place.getString("schedule_notes"));
        /*

        int i = 0;
        for (String key : Tools.CDPLACESCORRES.keySet()) {
            if (key.equals(_place.getString("type"))) {
                Log.d(TAG, "SELECTED : " + key);
                //typeSpinner.setSelection(i);
            }
            ++i;
        }


        //internalED.setText(_place.getString("schedule_notes"));


        //weekED.setText(new JSONArray(_place.getString("schedule")).toString());
        //eucharistED.setText(_place.getString("schedule_eucharist"));
        if (_place.getString("schedule_mass_sunday").length() > 0 && !_place.getString("schedule_mass_sunday").equals("null"))
            sundayED.setText(_place.getString("schedule_mass_sunday"));
            */
    }

    private void setButtonsOnClickListeners() {
        Button addPhotoBtn = (Button)findViewById(R.id.button_addphoto_com_geomesse_create_place);
        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhotoIntent();
            }
        });
        Button saveBtn = (Button)findViewById(R.id.button_save_com_geomesse_create_place);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendForm();
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void sendForm() {
        EditText nameED = (EditText)findViewById(R.id.editText_name_com_geomesse_create_place);
        Spinner typeSpinner = (Spinner)findViewById(R.id.spinnerType_com_geomesse_create_place);
        EditText addrLine1 = (EditText)findViewById(R.id.editText_address_com_geomesse_create_place);
        EditText addrLine2 = (EditText)findViewById(R.id.editText_address2_com_geomesse_create_place);
        EditText postalED = (EditText)findViewById(R.id.editText_postal_com_geomesse_create_place);
        EditText cityED = (EditText)findViewById(R.id.editText_city_com_geomesse_create_place);
        EditText countryED = (EditText)findViewById(R.id.editText_country_com_geomesse_create_place);
        EditText telED = (EditText)findViewById(R.id.editText_tel_com_geomesse_create_place);
        EditText urlED = (EditText)findViewById(R.id.editText_url_com_geomesse_create_place);
        EditText sundayED = (EditText)findViewById(R.id.editText_sunday_mass_com_geomesse_create_place);
        EditText weekED = (EditText)findViewById(R.id.editText_week_mass_com_geomesse_create_place);
        EditText eucharistED = (EditText)findViewById(R.id.editText_eucharist_mass_com_geomesse_create_place);
        EditText notesED = (EditText)findViewById(R.id.editText_notes_mass_com_geomesse_create_place);
        EditText photosED = (EditText)findViewById(R.id.editText_photos_com_geomesse_create_place);
        EditText internalED = (EditText)findViewById(R.id.editText_internal_com_geomesse_create_place);

        if ((nameED.getText().length() == 0) || (addrLine1.getText().length() == 0) || (postalED.getText().length() == 0) ||
                (cityED.getText().length() == 0) || (countryED.getText().length() == 0)) {
            displayError(getString(R.string.PLACE_FORM_MISSING_FIELDS));
        }
        else {
            if (_args == null)
                _args = new ArrayList<NameValuePair>();
            _args.clear();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("geolat", UserConnected.getInstance().get_geolat()));
            _args.add(new BasicNameValuePair("geolng", UserConnected.getInstance().get_geolng()));
            if (_place == null)
                _args.add(new BasicNameValuePair("id", ""));
            else {
                try {
                    _args.add(new BasicNameValuePair("id", _place.getString("id")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            _args.add(new BasicNameValuePair("name", nameED.getText().toString()));
            _args.add(new BasicNameValuePair("type", Tools.CDPLACESCORRES.get(typeSpinner.getSelectedItem().toString())));
            _args.add(new BasicNameValuePair("address_1", addrLine1.getText().toString()));
            _args.add(new BasicNameValuePair("address_2", addrLine2.getText().toString()));
            _args.add(new BasicNameValuePair("address_zip", postalED.getText().toString()));
            _args.add(new BasicNameValuePair("address_city", cityED.getText().toString()));
            _args.add(new BasicNameValuePair("address_country", countryED.getText().toString()));
            _args.add(new BasicNameValuePair("urls", urlED.getText().toString()));
            _args.add(new BasicNameValuePair("tels", telED.getText().toString()));
            _args.add(new BasicNameValuePair("pic", ""));
            _args.add(new BasicNameValuePair("pics", photosED.getText().toString()));
            _args.add(new BasicNameValuePair("schedule_mass_sunday", sundayED.getText().toString()));
            _args.add(new BasicNameValuePair("schedule_mass_week", weekED.getText().toString()));
            _args.add(new BasicNameValuePair("schedule_notes", notesED.getText().toString()));
            _args.add(new BasicNameValuePair("schedule_eucharist", eucharistED.getText().toString()));
            _args.add(new BasicNameValuePair("internal", internalED.getText().toString()));

            for (NameValuePair pair : _args) {
                Log.d(TAG, pair.getName() + " : " + pair.getValue());
            }

            _apiCaller = new HttpApiCall(this, _args, 2);
            _apiCaller.execute(Tools.API + Tools.CDPLACESEDIT);
            _progressDialog.show();
        }
    }

    private void displayError(String err) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(err);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void addPhotoIntent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.UPLOAD_PHOTO_CAMERA), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                startCamaraActivity();
            }
        });
        builder.setNegativeButton(getString(R.string.UPLOAD_PHOTO_LIBRARY), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startChoosePhotoActivity();
            }
        });
        builder.create().show();
    }

    private void startChoosePhotoActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, TAKE_PIC);
    }

    private void startCamaraActivity() {
        PackageManager pm = getPackageManager();
        if (pm != null && (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                File photo = PhotoManager.createImageFile();
                Log.d("Photo", photo.getAbsolutePath());
                _tempUrl = photo.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivityForResult(takePictureIntent, CAMERA);
        }
        else {
            //TODO message erreur
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA) {
                /*Bundle extras = data.getExtras();
                Bitmap result = (Bitmap)extras.get("data");
                if (result != null) {
                    addPhotoOnLayout(result);
                }*/
                addPhotoOnLayout(BitmapFactory.decodeFile(_tempUrl));
            }
            else if (requestCode == TAKE_PIC) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor;
                if (selectedImage != null) {
                    cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();
                        _tempUrl = picturePath;
                        addPhotoOnLayout(BitmapFactory.decodeFile(picturePath));
                    }
                }
            }
        }
    }

    private void addPhotoOnLayout(Bitmap mImageBitmap) {
        if (_args == null)
            _args = new ArrayList<NameValuePair>();
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("type", "photo"));
        _args.add(new BasicNameValuePair("temp", "0"));

        HttpApiImageCall apiCaller = new HttpApiImageCall(this, _args, 1);
        apiCaller.execute(Tools.API + Tools.CDUPLOAD, _tempUrl);

        LinearLayout photoLL = (LinearLayout)findViewById(R.id.linearLayout_fotos_com_geomesse_create_place);
        ImageView mImageView = new ImageView(this);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(View.VISIBLE);
        photoLL.addView(mImageView, new LinearLayout.LayoutParams(getScreenWidth() / 5, getScreenWidth() / 5));
    }

    private void setLayoutTheme() {
        if (_resId == 0 ||_resId == -1) {
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

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(CreatePlace.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
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

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok")) {
            if (type == 2)
                displayDialog(getString(R.string.PLACE_FORM_OK), true);
            else if (type == 1) {
                EditText photosED = (EditText)findViewById(R.id.editText_photos_com_geomesse_create_place);
                String curText = photosED.getText().toString();
                if (curText.length() > 0)
                    photosED.setText(curText + "\n" + resObj.getString("path"));
                else
                    photosED.setText(resObj.getString("path"));

            }
        }
    }

    private void displayDialog(String err, final boolean quit) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(err);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
                if (quit)
                    finish();
            }
        });
        alertDialog.show();
    }
}