package com.Network;

/**
 * Created by COCH on 16/07/13.
 * Télécharge une image
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.messages.DisplayMessage;
import com.user.UserConnected;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;

    private boolean _error = false;
    private boolean _crop = false;
    private ProgressDialog _progressDialog = null;
    private boolean _user = false;

    private DisplayMessage _displayMessage = null;
    private boolean _received = false;

    private int[] _paddings = null;
    private int[] _sizes = null;

    private Context _context = null;

    private int _type = 0;

    public DownloadImageTask(ImageView bmImage, boolean crop) {
        this.bmImage = bmImage;
        _crop = crop;
    }

    public DownloadImageTask(ImageView bmImage, boolean crop, boolean user) {
        this.bmImage = bmImage;
        _crop = crop;
        _user = user;
    }

    public DownloadImageTask(ImageView imageIV, boolean crop, DisplayMessage displayMessage, boolean received) {
        this.bmImage = imageIV;
        _crop = crop;
        _displayMessage = displayMessage;
        _received = received;
    }

    public DownloadImageTask(ImageView manVoiceIV, boolean crop, ProgressDialog homeProgressDialog, int[] paddings) {
        this.bmImage = manVoiceIV;
        _crop = crop;
        _progressDialog = homeProgressDialog;
        _paddings = paddings;
    }

    public DownloadImageTask(ImageView profileIV, boolean crop, int[] sizeS) {
        this.bmImage = profileIV;
        _crop = crop;
        _sizes = sizeS;
    }

    public DownloadImageTask(Context c, ImageView view, int type) {
        _context = c;
        bmImage = view;
        _type = type;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        if (urldisplay != null) {
            Bitmap mIcon11;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error File", e.getMessage());
                e.printStackTrace();
                _error = true;
                return null;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                _error = true;
                return null;
            }
            return mIcon11;
        }
        return null;
    }

    protected void onPostExecute(Bitmap result) {
        if (!_error) {
            if (result != null) {
                if (_context == null) {
                    if (UserConnected.getInstance().IsUserConnected() && _user)
                        UserConnected.getInstance().set_profilePic(result);
                    bmImage.setImageBitmap(result);
                    if (_sizes != null) {
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(_sizes[0], _sizes[1]);
                        bmImage.setLayoutParams(lp);
                    }
                    if (_crop) {
                        bmImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    if (_displayMessage != null)
                        _displayMessage.adjustImage(bmImage, result, _received);
                    if (_paddings != null)
                        bmImage.setPadding(_paddings[0], _paddings[1], _paddings[2], _paddings[3]);
                }
                else {
                    try {
                        ((ImageTaskCaller)_context).onImageResult(result, bmImage, _type);
                        _context = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}