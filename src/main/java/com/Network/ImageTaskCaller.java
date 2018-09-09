package com.Network;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Guillaume on 09/11/13.
 * Interface pour les appelant à ImageTaskCaller
 */

public interface ImageTaskCaller {
    void onImageResult(Bitmap bitmap, ImageView view, int type) throws Exception;
}
