package com.jminovasi.zakat.app;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.jminovasi.zakat.volley.LruBitmapCache;

import static com.android.volley.VolleyLog.TAG;

public class AppController extends Application {

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    LruBitmapCache lruBitmapCache;

    private static  AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance(){
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public ImageLoader getImageLoader(){
        getRequestQueue();
        if(imageLoader == null){
            getLruBitmapCache();
            imageLoader = new ImageLoader(this.requestQueue, lruBitmapCache);
        }
        return this.imageLoader;
    }

    public LruBitmapCache getLruBitmapCache() {
        if(lruBitmapCache == null){
            lruBitmapCache = new LruBitmapCache();
        }
        return lruBitmapCache;
    }

    public <T> void addToRequestQeue(Request<T> request, String tag){
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQeue(Request<T> request){
        request.setTag(request);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequest(Object tag){
        if(requestQueue != null){
            requestQueue.cancelAll(tag);
        }
    }
}
