package com.Network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.Tools.ImageCache;
import com.Tools.Tools;

import java.io.InputStream;

/**
 * Created by Guillaume on 10/11/13.
 * Fonction pour télécharger des images
 */
public class DownloadImages extends AsyncTask<String, Void, Bitmap> {

    private ImageView _view = null;
    private boolean _crop = false;
    private String _token = null;

    public final static String INIT = "INIT";
    public final static String PENDING = "PENDING";
    public final static String FINISHED = "FINISHED";

    private String statut = null;
    public String getStatut() {
        return statut;
    }
    public void setStatut(String statut) {
        this.statut = statut;
    }


    public DownloadImages(ImageView view, boolean crop, String token) {
        _crop = crop;
        _view = view;
        _token = token;

        statut = INIT;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected Bitmap doInBackground(String... infos) {
        if (infos[0] != null) {
            try {

                InputStream in = new java.net.URL(infos[0]).openStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);
                in.close();
                options.inSampleSize = calculateInSampleSize(options, Integer.parseInt(infos[1]), Integer.parseInt(infos[1]));
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeStream(new java.net.URL(infos[0]).openStream(), null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void onPostExecute(Bitmap result) {
        try {
            if (result != null) {
                _view.setImageBitmap(result);
                ImageCache.getInstance().addBitmapToMemoryCache(_token, result);
                if (_crop) {
                    _view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                statut = FINISHED;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
