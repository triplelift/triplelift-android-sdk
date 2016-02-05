package com.triplelift.sdk;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.List;

public class NativeAd {
    private boolean impressionFired = false, clickFired = false;
    private final Context context;
    private final List<String> impressionPixels, clickPixels;
    private final String brandName, clickthroughUrl, imageUrl, caption, header, logoUrl;
    private final long created;

    public long getCreated() {
        return created;
    }

    public NativeAd(Context context, String brandName, String clickthroughUrl, String imageUrl, String caption,
                    String header, String logoUrl, List<String> impressionPixels, List<String> clickPixels) {
        this.brandName = Utils.getStringElseNull(brandName);
        this.clickthroughUrl = clickthroughUrl;
        this.imageUrl = Utils.getStringElseNull(imageUrl);
        this.caption = Utils.getStringElseNull(caption);
        this.header = Utils.getStringElseNull(header);
        this.context = context;
        this.logoUrl = Utils.getStringElseNull(logoUrl);
        this.impressionPixels = impressionPixels;
        this.clickPixels = clickPixels;
        this.created = System.currentTimeMillis();
    }

    void fireImpression() {
        if (impressionFired) {
            return;
        }
        for (String impressionPixel: impressionPixels) {
            ImageRequest request = new ImageRequest(impressionPixel,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            impressionFired = true;
                        }
                    }, 1, 1, null, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
            request.setRetryPolicy(new DefaultRetryPolicy(20*1000, 1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Controller.getInstance(context).addToRequestQueue(request);
        }
    }

    void fireClick() {
        if (clickFired) {
            return;
        }
        for (String clickPixel: clickPixels) {
            ImageRequest request = new ImageRequest(clickPixel,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            impressionFired = true;
                        }
                    }, 1, 1, null, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
            request.setRetryPolicy(new DefaultRetryPolicy(20*1000, 1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Controller.getInstance(context).addToRequestQueue(request);
         }
    }

    public boolean isImpressionFired() {
        return impressionFired;
    }

    public boolean isClickFired() {
        return clickFired;
    }

    public Context getContext() {
        return context;
    }

    public List<String> getImpressionPixels() {
        return impressionPixels;
    }

    public List<String> getClickPixels() {
        return clickPixels;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getClickthroughUrl() {
        return clickthroughUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public String getHeader() {
        return header;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

}
