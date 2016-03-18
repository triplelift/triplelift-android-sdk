package com.triplelift.sdk;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface NativeAdCallback {
    void onSuccess(NativeAd nativeAd);
    void onFailure(JSONObject jsonObject);
    void onError(VolleyError error);
}
