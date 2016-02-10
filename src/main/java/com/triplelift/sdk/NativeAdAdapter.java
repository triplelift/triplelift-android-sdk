package com.triplelift.sdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NativeAdAdapter extends BaseAdapter {

    private static final double DEFAULT_AR = 1.618;
    private Adapter originalAdapter;
    private Context context;
    private NativeAdLayout nativeAdLayout;
    private String invCode;
    private Map<String, String> userData;
    private int nativeAdLayoutId;
    private NativeAdController nativeAdController;
    private NativeFeedPlacement nativeFeedPlacement;
    private NativeDisplayAdViewHolder viewHolder;
    private double aspectRatio;
    private Integer width;
    private Integer height;

    static class NativeDisplayAdViewHolder {
        TextView brand;
        TextView header;
        TextView caption;
        NetworkImageView mainImage;
        NetworkImageView logo;
    }

    public NativeAdAdapter(Context context, BaseAdapter adapter,
                           String invCode, int nativeAdLayoutId, int initialPosition,
                           int repeatInterval) {
        this.context = context;
        this.originalAdapter = adapter;
        this.nativeAdLayoutId = nativeAdLayoutId;
        this.invCode = invCode;
        this.userData = new ConcurrentHashMap<>();
        this.viewHolder = new NativeDisplayAdViewHolder();
        this.aspectRatio = DEFAULT_AR;

        this.nativeAdController = new NativeAdController(context);
        this.nativeAdController.registerInvCode(invCode);
        NativeFeedPositions nativeFeedPosition = new NativeFeedPositions(new int[] {initialPosition}, repeatInterval);
        this.nativeFeedPlacement = new NativeFeedPlacement(nativeFeedPosition);

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

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    private NativeAd getNativeAd(int position) {
        if (nativeFeedPlacement.isAdPosition(position)) {
            if (nativeFeedPlacement.isAdPositionLive(position)) {
                return nativeFeedPlacement.getNativeAd(position);
            } else {
                NativeAd nativeAd = nativeAdController.retrieveNativeAd(invCode);
                if (nativeAd != null) {
                    nativeFeedPlacement.placeNativeAd(nativeAd, position);
                    notifyDataSetChanged();
                    return nativeAd;
                }
                loadAds();
            }
        }
        return null;
    }

    // Currently supports only a single ad format
    public int getNativeAdViewType(int position) {
        if (getNativeAd(position) != null) {
            return 1;
        }
        return 0;
    }

    public void registerNativeAdLayout(NativeAdLayout layout) {
        this.nativeAdLayout = layout;
    }

    public void addUserData(Map<String, String> userData) {
        this.userData.putAll(userData);
    }

    public void loadAds() {
        nativeAdController.requestAds(invCode, userData, null);
    }

    private View getNativeAdView(int position, View view, ViewGroup parent) {

        final NativeAd nativeAd = getNativeAd(position);
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

//            userData.put("width", Integer.toString(viewHolder.mainImage.getWidth()));
//            userData.put("height", Integer.toString(viewHolder.mainImage.getHeight()));
        } else {
            if (view.getTag() == null) {
                return null;
            }
            viewHolder = (NativeDisplayAdViewHolder) view.getTag();
        }

        ImageLoader imageLoader = Controller.getInstance(context).getImageLoader();

        viewHolder.mainImage.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        int width = viewHolder.mainImage.getMeasuredWidth();
                        int height = viewHolder.mainImage.getMeasuredHeight();
                        setDimensions(width, height);
                        return true;
                    }
                });

        try {

            viewHolder.brand.setText("Sponsored by " + nativeAd.getBrandName());
            viewHolder.header.setText(nativeAd.getHeader());
            viewHolder.caption.setText(nativeAd.getCaption());
            //viewHolder.logo.setImageUrl(nativeAd.getLogoUrl(), imageLoader);
            viewHolder.mainImage.setImageUrl(nativeAd.getImageUrl(), imageLoader);

            //TODO open in webview
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

    public int getNativeAdViewTypeCount() {
        // TODO implement
        return 1;
    }

    // TODO implement correctly
    @Override
    public int getCount() {
        if (originalAdapter.getCount() == 0) {
            return 0;
        }
        return nativeFeedPlacement.adsLive() + originalAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {

        Object nativeAd = getNativeAd(position);
        if (nativeAd != null) {
            return nativeAd;
        }

        int originalPosition = nativeFeedPlacement.getContentPosition(position);

        return originalAdapter.getItem(originalPosition);
    }

    @Override
    public long getItemId(int position) {
        Object nativeAd = getNativeAd(position);
        if (nativeAd != null) {
            return nativeAd.hashCode();
        }

        int originalPosition = nativeFeedPlacement.getContentPosition(position);

        return originalAdapter.getItemId(originalPosition);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View nativeAdView = getNativeAdView(position, view, viewGroup);

        if (nativeAdView != null) {
            return nativeAdView;
        }
        int originalPosition = nativeFeedPlacement.getContentPosition(position);
        return originalAdapter.getView(originalPosition, view, viewGroup);
    }

    @Override
    public int getItemViewType(final int position) {
        int nativeAdViewType = getNativeAdViewType(position);
        if (nativeAdViewType == 1) {
            return nativeAdViewType + originalAdapter.getViewTypeCount() - 1;
        }

        int originalPosition = nativeFeedPlacement.getContentPosition(position);
        return originalAdapter.getItemViewType(originalPosition);
    }

    @Override
    public boolean isEmpty() {
        return originalAdapter.isEmpty();
    }

    @Override
    public int getViewTypeCount() {
        return originalAdapter.getViewTypeCount() + getNativeAdViewTypeCount();
    }

    public void setDebug() {
        nativeAdController.setDebug(true);
    }

}
