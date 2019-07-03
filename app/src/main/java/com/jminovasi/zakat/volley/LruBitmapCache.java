package com.jminovasi.zakat.volley;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class LruBitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public LruBitmapCache(){
        this(getDefaultLruCacheSize());
    }

    public LruBitmapCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    private static int getDefaultLruCacheSize() {
        int maxMemory = (int)Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;

        return cacheSize;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}
