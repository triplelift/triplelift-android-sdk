package com.triplelift.sponsoredimages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by bdlee on 6/12/13.
 */
public class SponsoredImage {
    private static int READ_TIMEOUT = 20000; /* milliseconds */
    private static int CONN_TIMEOUT = 25000; /* milliseconds */
    
    private static int DEFAULT_IMAGE_WIDTH = 150;
    
    private static String IMPRESSION_ENDPOINT = "http://eb.3lift.com/mbi?id=%s&ii=%s&publisher=%s&&platform=%s";
    private static String CLICKTHROUGH_ENDPOINT = "http://eb.3lift.com/mbc?id=%s&ii=%s&publisher=%s&&platform=%s";
    private static String EVENT_ENDPOINT = "http://eb.3lift.com/mbs?id=%s&ii=%s&publisher=%s&&platform=%s&&st=%s";

    private String mPublisher;
    private String mContentID;
    private String mMobilePlatform;

    private String mImageID;
    private double mImageWidthOverHeight;
    private int mImageWidth;
    private int mImageHeight;

    private String mHeading;
    private String mCaption;
    private String mClickThroughLink;

    private String mAdpinrImageUrl;

    public SponsoredImage(JSONObject jsonObject, String publisher, String sponsoredContentID, String mobilePlatform) {
        mPublisher = publisher;
        mContentID = sponsoredContentID;
        mMobilePlatform = mobilePlatform;

        mImageWidthOverHeight = jsonObject.optInt("image_w_over_h");
        mImageWidth = DEFAULT_IMAGE_WIDTH;
        mImageHeight = (int) (DEFAULT_IMAGE_WIDTH / mImageWidthOverHeight);

        mHeading = jsonObject.optString("heading");
        mCaption = jsonObject.optString("caption");
        mClickThroughLink = jsonObject.optString("link");

        mAdpinrImageUrl = jsonObject.optString("image_url");
        mImageID = mAdpinrImageUrl.replaceAll("^(http://.*?/)(.*?)(\\.\\w*)$", "$2");
    }
    public int getImageWidth() {
        return this.mImageWidth;
    }
    public int getImageHeight() {
        return this.mImageHeight;
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
        return getImageUrl(this.mImageWidth, this.mImageHeight);
    }
    public String getImageUrl(int width) {
        int height = (int) (width / mImageWidthOverHeight);
        return getImageUrl(width, height);
    }
    public String getImageUrl(int width, int height) {
        try {
            String encodedAdpinUrl = URLEncoder.encode(this.mAdpinrImageUrl, "UTF-8");
            return String.format(Locale.US, "http://img.3lift.com/?alt=tl&width=%d&height=%d&url=%s", width, height, encodedAdpinUrl);
        } catch (Exception e) {
        }
        return null;
    }

    public Bitmap getImage() throws IOException {
        return getImage(this.mImageWidth, this.mImageHeight);
    }
    public Bitmap getImage(int width) throws IOException {
        int height = (int) (width / mImageWidthOverHeight);
        return getImage(width, height);
    }
    public Bitmap getImage(int width, int height) throws IOException {
        String imageUrl = this.getImageUrl(width, height);

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
            String url = String.format(
            		Locale.US,
            		IMPRESSION_ENDPOINT,
                    URLEncoder.encode(this.mContentID, "UTF-8"),
                    URLEncoder.encode(this.mImageID, "UTF-8"),
                    URLEncoder.encode(this.mPublisher, "UTF-8"),
                    URLEncoder.encode(this.mMobilePlatform, "UTF-8"));
            new genericRequestTask().execute(url);
        } catch (UnsupportedEncodingException e) {
        }
    }
    public void logClickThrough() {
        try {
            String url = String.format(
            		Locale.US,
            		CLICKTHROUGH_ENDPOINT,
                    URLEncoder.encode(this.mContentID, "UTF-8"),
                    URLEncoder.encode(this.mImageID, "UTF-8"),
                    URLEncoder.encode(this.mPublisher, "UTF-8"),
                    URLEncoder.encode(this.mMobilePlatform, "UTF-8"));
            new genericRequestTask().execute(url);
        } catch (UnsupportedEncodingException e) {
        }
    }
    public void logEvent(String eventName) {
        try {
            String url = String.format(
            		Locale.US,
            		EVENT_ENDPOINT,
                    URLEncoder.encode(this.mContentID, "UTF-8"),
                    URLEncoder.encode(this.mImageID, "UTF-8"),
                    URLEncoder.encode(this.mPublisher, "UTF-8"),
                    URLEncoder.encode(this.mMobilePlatform, "UTF-8"),
                    URLEncoder.encode(eventName, "UTF-8"));
            new genericRequestTask().execute(url);
        } catch (UnsupportedEncodingException e) {
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
