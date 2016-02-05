package com.triplelift.sdk;

import android.content.Context;
import android.os.Handler;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NativeAdController {

    private static final String TAG = NativeAdController.class.getSimpleName();
    //private static final String BASE_URL = "http://tlx.3lift.com/mj/auction?invType=app&";
    private static final String BASE_URL = "http://10.0.1.86:8076/mj/auction?invType=app&";
    private static final int CACHE_EXPIRATION = 5 * 60 * 1000;
    private static final int[] RETRY_DELAY = new int[]{1000, 1000 * 5, 1000 * 30, 1000 * 60, 1000 * 60 * 3};
    private static final int CACHE_SIZE = 1;
    private Map<String, String> requestParams;
    private boolean requestFired = false;
    private boolean retryFired = false;
    private int retryIndex = 0;
    private boolean debug = false;

    private final Context context;
    private final Handler cacheHandler;
    private final Runnable cacheRunnable;
    private final Map<String, List<NativeAd>> nativeAdCache;
    private final Set<String> invCodes;

    NativeAdController(Context context) {
        this.requestParams = new ConcurrentHashMap<>();
        this.context = context;
        this.nativeAdCache = new HashMap<>();
        this.cacheHandler = new Handler();
        this.invCodes = new HashSet<>();
        this.cacheRunnable = new Runnable() {
            @Override
            public void run() {
                retryFired = false;
                for (String invCode: invCodes) {
                    fillCache(invCode);
                }
            }
        };
    }

    public void registerInvCode(String invCode) {
        invCodes.add(invCode);
    }

    public boolean adsAvailable() {
        return !nativeAdCache.isEmpty();
    }

    public void requestAds(String invCode, Map<String, String> requestParams) {
        this.requestParams = requestParams;
        fillCache(invCode);
    }

    protected NativeAd retrieveNativeAd(String invCode) {
        NativeAd nativeAd = null;
        long now = System.currentTimeMillis();

        List<NativeAd> placementCache = nativeAdCache.get(invCode);

        while (placementCache != null && !placementCache.isEmpty()) {
            nativeAd = placementCache.remove(0);
            long created = nativeAd.getCreated();
            if (now - created <= CACHE_EXPIRATION) {
                break;
            }
        }

        if (nativeAdCache.size() < CACHE_SIZE && !requestFired && !retryFired) {
            cacheHandler.post(cacheRunnable);
        }

        return nativeAd;
    }

    private void fillCache(String invCode) {
        List<NativeAd> cache;
        if (!nativeAdCache.containsKey(invCode)) {
            cache = new ArrayList<>(CACHE_SIZE);
            nativeAdCache.put(invCode, cache);
        } else {
            cache = nativeAdCache.get(invCode);
        }
        if (cache.size() < CACHE_SIZE) {
            requestFired = true;
            requestAd(invCode);
        }
    }

    private void requestAd(final String invCode) {

        if (!nativeAdCache.containsKey(invCode)) {
            nativeAdCache.put(invCode, new ArrayList<NativeAd>());
        }

        final String requestUrl = generateRequestUrl(invCode, requestParams);
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyLog.d(TAG, "Response: " + response.toString());
                        if (response != null) {
                            NativeAd nativeAd = parseNativeAd(response);
                            if (nativeAd != null) {
                                List<NativeAd> placementCache = nativeAdCache.get(invCode);
                                placementCache.add(nativeAd);
                                requestFired = false;
                                retryReset();
                            }
                        }
                        requestFired = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                requestFired = false;
                if (retryIndex >= RETRY_DELAY.length) {
                    retryReset();
                    return;
                }
                cacheHandler.postDelayed(cacheRunnable, RETRY_DELAY[retryIndex]);
                retryIndex++;
            }
        }
        );

        jsonReq.setRetryPolicy(new DefaultRetryPolicy(5*1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Controller.getInstance(context).addToRequestQueue(jsonReq);
    }

    private String generateRequestUrl(String invCode, Map<String, String> userData) {
        String debugString = "";
        if (debug) {
            debugString = "test=true&";
        }
        StringBuilder sb = new StringBuilder(BASE_URL + debugString +"inv_code=" + invCode + "&");
        for (Map.Entry<String, String> entry: userData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key);
            sb.append("=");
            sb.append(value);
            sb.append("&");
        }
        return sb.toString();
    }

    private NativeAd parseNativeAd(JSONObject response) {
        try {
            System.out.println(response.toString());
            if (response.has("status")) {
                return null;
            }

            String advertiser = response.getString("advertiser_name");
            String clickthroughUrl = response.getString("clickthrough_url");
            String imageUrl = response.getString("image_url");
            String caption = response.getString("caption");
            String heading = response.getString("heading");

            imageUrl = imageUrl.replace("https", "http"); //sand image server doesn't support https

            //TODO fix this? & maybe null check
            List<String> clickthroughPixels = jsonArrayToList((JSONArray) response.get("clickthrough_pixels"));
            List<String> impressionPixels = jsonArrayToList((JSONArray) response.get("impression_pixels"));

            // TODO logo URL
            NativeAd nativeAd = new NativeAd(context, advertiser, clickthroughUrl, imageUrl, caption,
                    heading, "http://i.forbesimg.com/media/lists/companies/triplelift_416x416.jpg",
                    impressionPixels, clickthroughPixels);

            return nativeAd;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List jsonArrayToList(JSONArray jsonArray) {
        ArrayList<String> list = new ArrayList<>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i=0;i<len;i++){
                try {
                    list.add(jsonArray.get(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    private void retryReset() {
        retryIndex = 0;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
