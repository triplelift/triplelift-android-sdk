package com.triplelift.sdk;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Instantiate with the application context, not the activity context */
public class Controller {

    public static final String TAG = Controller.class.getSimpleName();

    private static Controller mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    LruBitmapCache mLruBitMapCache;
    private Runnable mAdRequester;

    private final List<NativeAd> ads;

    private final Handler handler;

    private Controller(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        ads = new CopyOnWriteArrayList<>();
        mImageLoader = new ImageLoader(mRequestQueue,
                new LruBitmapCache());

        handler = new Handler();

        mAdRequester = new Runnable() {
            public void run() {
                requestAds();
            }
        };
    }

    private void requestAds() {

        if (!Utils.isNetworkAvailable(mCtx)) {
            return;
        }

    }

    public static synchronized Controller getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Controller(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitMapCache);
        }
        return this.mImageLoader;
    }

    public LruBitmapCache getLruBitmapCache() {
        if (mLruBitMapCache == null) {
            mLruBitMapCache = new LruBitmapCache();
        }
        return this.mLruBitMapCache;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG: tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
