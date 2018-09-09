package com.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.Network.DownloadImages;
import com.i2heaven.carpedeum.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Guillaume on 02/12/13.
 * Take and pic photos
 */

public class PhotoManager {

    private final String TAG = "PhotoManager";
    private static String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    public static String _tempUrl = "";
    private Activity _instance = null;

    private final int CAMERA = 101;
    private final int TAKE_PIC = 102;

    public static void takePhoto(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(takePictureIntent, 1);
    }

    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    }

    public static File createImageFileRotated() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    }

    private static String getAlbumName() {
        return "CarpeDeum";
    }

    private static File getAlbumDir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()){
                        Log.d("getAlbumDir", "failed to create directory");
                        return null;
                    }
                }
            }
        }
        return storageDir;
    }

    public PhotoManager(Activity instance) {
        _instance = instance;
    }

    public void takePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
        builder.setCancelable(true);
        builder.setPositiveButton(_instance.getString(R.string.UPLOAD_PHOTO_CAMERA), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                startCamaraActivity();
            }
        });
        builder.setNegativeButton(_instance.getString(R.string.UPLOAD_PHOTO_LIBRARY), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startChoosePhotoActivity();
            }
        });
        builder.create().show();
    }

    private void startChoosePhotoActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        _instance.startActivityForResult(intent, TAKE_PIC);
    }

    private void startCamaraActivity() {
        PackageManager pm = _instance.getPackageManager();
        if (pm != null && (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                File photo = PhotoManager.createImageFile();
                //Log.d("Photo", photo.getAbsolutePath());
                _tempUrl = photo.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            } catch (IOException e) {
                e.printStackTrace();
            }
            _instance.startActivityForResult(takePictureIntent, CAMERA);
        }
        else {
            //TODO message erreur
        }
    }

    private Bitmap setOrientation(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public int getPhotoOrientation(Uri imageUri, String imagePath) {
        Log.d(TAG, "getPhotoOrientation::URI::" + imageUri + ", imagePath::" + imagePath);
        int rotate = 0;
        try {
            _instance.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
            Log.d(TAG, "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public Bitmap getBitmapInGoodOrientation(Bitmap originBitmap) {
        Log.d(TAG, "getBitmapInGoodOrientation::size::" + originBitmap.getWidth());
        int rotate = getPhotoOrientation(Uri.fromFile(new File(_tempUrl)), _tempUrl);
        return setOrientation(originBitmap, rotate);
    }

    public Bitmap getBitmapInGoodOrientation(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor;
        if (selectedImage != null) {
            cursor = _instance.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                PhotoManager._tempUrl = picturePath;
                getPhotoOrientation(Uri.fromFile(new File(_tempUrl)), _tempUrl);

                return getBitmapInGoodOrientation(BitmapFactory.decodeFile(picturePath));
            }
        }
        return null;
    }

    public String createRotatedImage(Bitmap mImageBitmap) {
        try {
            File newFile = createImageFileRotated();
            FileOutputStream out = new FileOutputStream(newFile.getPath());
            mImageBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            Log.d(TAG, "NEW FILE PATH: " + newFile.getAbsolutePath());
            return newFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public BitmapFactory.Options getBitmapOptionsForResiszing() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(PhotoManager._tempUrl, opts);
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = DownloadImages.calculateInSampleSize(opts, Tools.CDMAXPHOTOSIZE, Tools.CDMAXPHOTOSIZE);
        return opts;
    }
/*
    public Bitmap reduceBitmap(Bitmap origin, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        return bitmap;
    }
*/

}
