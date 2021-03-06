package com.triplelift.sdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
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

    public NativeAdUnit(Context context, String invCode, int width, int height, boolean includeLogoOnImage) {
        this(context, invCode, 0, includeLogoOnImage);
        setDimensions(width, height);
    }

    public NativeAdUnit(Context context, String invCode, int width, int height) {
        this(context, invCode, 0, true);
        setDimensions(width, height);
    }

    public NativeAdUnit(Context context, String invCode, int nativeAdLayoutId, boolean includeLogoOnImage) {
        this.context = context;
        this.nativeAdLayoutId = nativeAdLayoutId;
        this.invCode = invCode;
        this.userData = new ConcurrentHashMap<>();
        this.viewHolder = new NativeDisplayAdViewHolder();
        this.aspectRatio = DEFAULT_AR;
        this.nativeAdController = new NativeAdController(context, includeLogoOnImage);
        this.nativeAdController.registerInvCode(invCode);
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

    public NativeAd getNativeAdRaw(String invCode) {
        NativeAd nativeAd = nativeAdController.retrieveNativeAd(invCode);
        return nativeAd;
    }

    public void requestAds() {
        nativeAdController.requestAds(invCode, userData);
    }

    public void requestAd(NativeAdCallback nativeAdCallback) {
        nativeAdController.requestAd(invCode, userData, nativeAdCallback);
    }

    public void getNativeAd(ViewGroup parent) {
        getNativeAd(null, parent);
    }

    public View getNativeAd(View view, ViewGroup parent, final NativeAd nativeAd) {

        if (nativeAd == null) {
            return null;
        }

        if (view == null) {
            view = LayoutInflater.from(context).inflate(nativeAdLayoutId, parent, false);
            viewHolder = new NativeDisplayAdViewHolder();
            viewHolder.brand = (TextView) view.findViewById(nativeAdLayout.getBrandId());
            viewHolder.header = (TextView) view.findViewById(nativeAdLayout.getHeaderId());
            viewHolder.caption = (TextView) view.findViewById(nativeAdLayout.getCaptionId());
            viewHolder.mainImage = (NetworkImageView) view.findViewById(nativeAdLayout.getImageId());
            //viewHolder.logo = (NetworkImageView) view.findViewById(nativeAdLayout.getLogoId());
            view.setTag(viewHolder);
        } else {
            if (view.getTag() == null) {
                return null;
            }
            viewHolder = (NativeDisplayAdViewHolder) view.getTag();
        }

        final ImageLoader imageLoader = Controller.getInstance(context).getImageLoader();

        viewHolder.mainImage.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        int width = viewHolder.mainImage.getMeasuredWidth();
                        int height = viewHolder.mainImage.getMeasuredHeight();
                        setDimensions(width, height);
//                        viewHolder.mainImage.setImageUrl(Utils.setWidthAndHeight(nativeAd.getImageUrl(), width, height), imageLoader);
                        return true;
                    }
                });

        try {

            viewHolder.brand.setText("Sponsored by " + nativeAd.getBrandName());
            viewHolder.header.setText(nativeAd.getHeader());
            viewHolder.caption.setText(nativeAd.getCaption());
            viewHolder.mainImage.setImageUrl(nativeAd.getImageUrl(), imageLoader);
            viewHolder.logo.setImageUrl(nativeAd.getLogoUrl(), imageLoader);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse(nativeAd.getClickthroughUrl()));
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    nativeAd.fireClick();
                    context.startActivity(browserIntent);
                }
            });

            nativeAd.fireImpression();

        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return null;
        }
        return view;
    }


    public View getNativeAd(View view, ViewGroup parent) {
        final NativeAd nativeAd = nativeAdController.retrieveNativeAd(invCode);
        return getNativeAd(view, parent, nativeAd);
    }

    public void registerNativeAdLayout(NativeAdLayout layout) {
        this.nativeAdLayout = layout;
    }

    public boolean adIsAvailable() {
        final NativeAd nativeAd = nativeAdController.retrieveNativeAd(invCode);
        if (nativeAd == null) {
            return false;
        }
        return true;
    }

    public void setDebug() {
        nativeAdController.setDebug(true);
    }

}