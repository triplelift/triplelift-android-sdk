package com.triplelift.sponsoredimages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bdlee on 6/12/13.
 */
public class SponsoredImage {
    private static int READ_TIMEOUT = 20000; /* milliseconds */
    private static int CONN_TIMEOUT = 25000; /* milliseconds */

    private String mImageUrl;
    private String mImageThumbnailUrl;

    private String mAdvertiserName;
    private String mHeading;
    private String mCaption;
    private String mClickThroughLink;

    private JSONArray mImpressionPixels;
    private JSONArray mClickthroughPixels;
    private JSONArray mInteractionPixels;
    private JSONArray mSharePixels;

    public SponsoredImage(JSONObject jsonObject, String invCode, String mobilePlatform) {
        mAdvertiserName = jsonObject.optString("advertiser_name");
        mHeading = jsonObject.optString("heading");
        mCaption = jsonObject.optString("caption");
        mClickThroughLink = jsonObject.optString("clickthrough_url");

        mImageUrl = jsonObject.optString("image_url");
        mImageThumbnailUrl = jsonObject.optString("image_thumbnail_url");

        mImpressionPixels = jsonObject.optJSONArray("impression_pixels");
        mClickthroughPixels = jsonObject.optJSONArray("clickthrough_pixels");
        mInteractionPixels = jsonObject.optJSONArray("interaction_pixels");
        mSharePixels = jsonObject.optJSONArray("share_pixels");
    }
    public String getAdvertiserName() {
        return this.mAdvertiserName;
    }
    public String getHeading() {
        return this.mHeading;
    }
    public String getCaption() {
        return this.mCaption;
    }
    public String getClickThroughLink() {
        return this.mClickThroughLink;
    }

    public String getImageUrl() {
        return this.mImageUrl;
    }
    public String getImageThumbnailUrl() {
        return this.mImageThumbnailUrl;
    }

    public Bitmap getImage() throws IOException {
        String imageUrl = this.getImageUrl();
        return doGetImage(imageUrl);
    }
    public Bitmap getImageThumbnail() throws IOException {
        String imageUrl = this.getImageThumbnailUrl();
        return doGetImage(imageUrl);
    }
    private Bitmap doGetImage(String imageUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return BitmapFactory.decodeStream(is);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    
    }

    public void logImpression() {
        try {
            for(int i = 0; i < this.mImpressionPixels.length(); i++) {
            	String url = this.mImpressionPixels.getString(i);
            	new genericRequestTask().execute(url);
            }
        } catch (JSONException e) {
        }
    }
    public void logClickThrough() {
        try {
            for(int i = 0; i < this.mClickthroughPixels.length(); i++) {
            	String url = this.mClickthroughPixels.getString(i);
            	new genericRequestTask().execute(url);
            }
        } catch (JSONException e) {
        }
    }
    public void logInteraction() {
        try {
            for(int i = 0; i < this.mInteractionPixels.length(); i++) {
            	String url = this.mInteractionPixels.getString(i);
            	new genericRequestTask().execute(url);
            }
        } catch (JSONException e) {
        }
    }
    public void logShare() {
        try {
            for(int i = 0; i < this.mSharePixels.length(); i++) {
            	String url = this.mSharePixels.getString(i);
            	new genericRequestTask().execute(url);
            }
        } catch (JSONException e) {
        }
    }

    private class genericRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT /* milliseconds */);
                conn.setConnectTimeout(CONN_TIMEOUT /* milliseconds */);
                conn.setRequestMethod("GET");
                // Starts the query
                conn.connect();
                int responseCode = conn.getResponseCode();
                if(responseCode != HttpURLConnection.HTTP_OK) {
                	// wasn't able to connect
                }
            } catch (IOException e) {
            } finally {
                if(conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }
}
