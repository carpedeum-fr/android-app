package com.user;

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
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.HttpApiCall;
import com.Network.HttpApiImageCall;
import com.Network.ImageTaskCaller;
import com.Tools.PhotoManager;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 07/12/13.
 * Editer ses photos
 */

public class EditPhotos extends Activity implements ApiCaller {

    int _resId = 0;
    private final static String TAG = "EditPhotos";

    /* PHOTOS */
    private final int CAMERA = 101;
    private final int TAKE_PIC = 102;
    private static String _tempUrl = "";

    private HttpApiCall _apiCaller = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    public ProgressDialog _progressDialog = null;

    private String _newPhotoPath = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_user_edit_photos);
        Intent myIntent = getIntent();
        if (myIntent.getExtras() != null) {
            setLayoutTheme(myIntent.getExtras().getInt("resId"));
            findViewById(R.id.imageView_headerLogo).setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.MonProfil));
        }
        setButtonsOnClickListeners();
    }

    private void setButtonsOnClickListeners() {
        Button addPhotoBtn = (Button)findViewById(R.id.button_add_photo_com_user_edit_photos);
        ImageView photoIV = (ImageView)findViewById(R.id.imageView_photo_com_user_edit_photos);
        if (UserConnected.getInstance().getPic().length() > 0) {
            addPhotoBtn.setText(getString(R.string.PROFILE_FORM_BUTTON_PHOTO_CHANGE));
            if (UserConnected.getInstance().get_profilePic() != null) {
                photoIV.setImageBitmap(UserConnected.getInstance().get_profilePic());
                photoIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }

        // Ajouter une photo
        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChoosePhotoActivity();
            }
        });

        // Bouton sauvegarder
        Button saveBtn = (Button)findViewById(R.id.button_save_com_user_edit_photos);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }


    private void saveProfile() {

        if (_newPhotoPath != null && _newPhotoPath.length() > 0) {

            _progressDialog = new ProgressDialog(this);
            _progressDialog.setMessage(getString(R.string.ChargementEnCours));
            _progressDialog.show();

            if (_args == null)
                _args = new ArrayList<NameValuePair>();
            _args.clear();
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            _args.add(new BasicNameValuePair("image", _newPhotoPath));
            _args.add(new BasicNameValuePair("public_photos", ""));
            _args.add(new BasicNameValuePair("private_photos", ""));
            _apiCaller = new HttpApiCall(this, _args, 2);
            _apiCaller.execute(Tools.API + Tools.CDPROFILESET);


            // On met à jour l'image de l'utilisateur
            UserConnected.getInstance().setPic(_newPhotoPath);
        }
    }


    /* GESTION DES PHOTOS */

    private void addPhotoIntent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.UPLOAD_PHOTO_CAMERA), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    startCamaraActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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



    private void startCamaraActivity() throws IOException {

        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA);
        }*/

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = PhotoManager.createImageFile();
            Log.d("Photo", photoFile.getAbsolutePath());
            _tempUrl = photoFile.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(takePictureIntent, CAMERA);
        }
    }

    /*
    private void startCamaraActivity() {
        PackageManager pm = getPackageManager();
        if (pm != null && (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                File photo = PhotoManager.createImageFile();
                Log.d("Photo", photo.getAbsolutePath());
                _tempUrl = photo.getAbsolutePath();
                System.err.println("startCamaraActivity::photo::" + _tempUrl);
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
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA) {


                //addPhotoOnLayout(BitmapFactory.decodeFile(_tempUrl));

                getTakenPicture(data);

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


    /**
     * Récupérer la photo prise depuis la gallerie
     * @param data
     */
    private void getTakenPicture(Intent data) {


        // On récupère l'image
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");


        // On affiche l'image
        ImageView mImageView = (ImageView)findViewById(R.id.imageView_photo_com_user_edit_photos);
        mImageView.setImageBitmap(imageBitmap);
        mImageView.setVisibility(View.VISIBLE);


    }

    private void addPhotoOnLayout(Bitmap mImageBitmap) {
        if (_args == null)
            _args = new ArrayList<>();
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("type", "photo"));
        _args.add(new BasicNameValuePair("temp", "0"));



        System.err.println("addPhotoOnLayout:: -> " + _tempUrl);
        HttpApiImageCall apiCaller = new HttpApiImageCall(this, _args, 1);
        apiCaller.execute(Tools.API + Tools.CDUPLOAD, _tempUrl);

        ImageView mImageView = (ImageView)findViewById(R.id.imageView_photo_com_user_edit_photos);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();

        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok")) {
            if (type == 1) {

                System.err.println("Photo uploaded ! On appelle pour changer la photo");

                _newPhotoPath = resObj.getString("path");
                if (Tools.CDDEBUG) Log.d(TAG, "Path:: " + _newPhotoPath);
            }
            else if (type == 2) {
                //TODO supprimer bien la photo actuelle
                if (Tools.CDDEBUG)
                    Log.d(TAG, "Profil bien mis à jour");
                Intent myIntent = getIntent();
                myIntent.putExtra("changes", "yes");
                setResult(RESULT_OK, myIntent);
                finish();
            }
        }
    }

    /* TOOLS */

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

    private void setLayoutTheme(int resId) {
        _resId = resId;
        if (resId == 0 || resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        }
        else if (resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
        }
        else if (resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
        }
        else if (resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
        }
        else if (resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
        }
        else if (resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
        }
        else if (resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
        }
        else if (resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
        }
        else if (resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
        }
    }

}