package com.triplelift.sdk;

import android.content.Context;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NativeAdUnit {

    private static final double DEFAULT_AR = 1.618;

    private Context context;
    private NativeAdLayout nativeAdLayout;
    private int nativeAdLayoutId;
    private double aspectRatio;
    private String invCode;
    private Map<String, String> userData;
    private NativeDisplayAdViewHolder viewHolder;
    private NativeAdController nativeAdController;

    static class NativeDisplayAdViewHolder {
        TextView brand;
        TextView header;
        TextView caption;
        NetworkImageView mainImage;
        NetworkImageView logo;
    }

    public NativeAdUnit(Context context, String invCode, int nativeAdLayoutId) {
        this.context = context;
        this.nativeAdLayoutId = nativeAdLayoutId;
        this.invCode = invCode;
        this.userData = new ConcurrentHashMap<>();
        this.viewHolder = new NativeDisplayAdViewHolder();
        this.aspectRatio = DEFAULT_AR;

        this.nativeAdController = new NativeAdController(context);
        setImplicitUserData();
    }

    private void setImplicitUserData() {
        int deviceWidth = Utils.getWidth(context);
        int adjustedHeight = (int) Math.round(deviceWidth / aspectRatio);
        this.userData.put("width", Integer.toString(deviceWidth));
        this.userData.put("height", Integer.toString(adjustedHeight));
        try {
            String ip = Utils.getIpAddress();
            this.userData.put("ip", ip);
        } catch (Exception e) {
            //DO NOTHING
        }
    }

    public void setDimensions(Integer width, Integer height) {

        if (width != null) {
            this.userData.put("width", Integer.toString(width));
        }

        if (height != null) {
            this.userData.put("height", Integer.toString(height));
        }
    }

    public void getNativeAd(String invCode) {
        NativeAd nativeAd = nativeAdController.retrieveNativeAd(invCode);
    }
}
