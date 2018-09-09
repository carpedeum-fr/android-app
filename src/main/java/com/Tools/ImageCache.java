package com.Tools;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Created by Guillaume on 10/11/13.
 * Cache pour les images
 */

public class ImageCache {

    private String TAG = "ImageCache";
    private static final ImageCache _instance = new ImageCache();
    public static ImageCache getInstance() {
        return _instance;
    }
    private LruCache<String, Bitmap> _mMemoryCache;

    private ImageCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.d(TAG, "cache size: " + cacheSize);
        _mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= 12)
                    return bitmap.getByteCount() / 1024;
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            _mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return _mMemoryCache.get(key);
    }
}
