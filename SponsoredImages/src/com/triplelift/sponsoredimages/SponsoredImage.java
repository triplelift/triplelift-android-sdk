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
    private static String IMPRESSION_ENDPOINT = "http://eb.3lift.com/mbi?id=%s&ii=%s&publisher=%s&&platform=%s";
    private static String CLICKTHROUGH_ENDPOINT = "http://eb.3lift.com/mbc?id=%s&ii=%s&publisher=%s&&platform=%s";
    private static String EVENT_ENDPOINT = "http://eb.3lift.com/mbs?id=%s&ii=%s&publisher=%s&&platform=%s&&st=%s";

    private String _publisher;
    private String _contentID;
    private String _mobilePlatform;

    private String _imageID;
    private String _imageExtension;
    private int _imageWidth;
    private int _imageHeight;

    private String _caption;
    private String _fullCaption;
    private String _clickThroughLink;

    private String _adpinrImageUrl;

    public SponsoredImage(JSONObject jsonObject, String publisher, String sponsoredContentID, String mobilePlatform) {
        this._publisher = publisher;
        this._contentID = sponsoredContentID;
        this._mobilePlatform = mobilePlatform;

        this._imageID = jsonObject.optString("id");
        this._imageExtension = jsonObject.optString("extension");
        this._imageWidth = jsonObject.optInt("width");
        this._imageHeight = jsonObject.optInt("height");

        this._caption = jsonObject.optString("caption");
        this._fullCaption = jsonObject.optString("full_caption");
        this._clickThroughLink = jsonObject.optString("link");

        this._adpinrImageUrl = String.format(Locale.US, "http://images.adpinr.com/%s%s", this._imageID, this._imageExtension);
    }
    public int getImageWidth() {
        return this._imageWidth;
    }
    public int getImageHeight() {
        return this._imageHeight;
    }
    public String getCaption() {
        return this._caption;
    }
    public String getFullCaption() {
        return this._fullCaption;
    }
    public String getClickThroughLink() {
        return this._clickThroughLink;
    }

    public Bitmap getImage() throws IOException {
        return getImage(this._imageWidth, this._imageHeight);
    }
    public Bitmap getImage(int width) throws IOException {
        int height = this._imageHeight * this._imageWidth / width;
        return getImage(width, height);
    }
    public Bitmap getImage(int width, int height) throws IOException {
        String imageUrl = this.getImageUrl(width, height);

        InputStream is = null;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
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

    public String getImageUrl() {
        return getImageUrl(this._imageWidth, this._imageHeight);
    }
    public String getImageUrl(int width) {
        int height = this._imageHeight * this._imageWidth / width;
        return getImageUrl(width, height);
    }
    public String getImageUrl(int width, int height) {
        try {
            String encodedAdpinUrl = URLEncoder.encode(this._adpinrImageUrl, "UTF-8");
            return String.format(Locale.US, "http://img.3lift.com/?width=%d&height=%d&url=%s", width, height, encodedAdpinUrl);
        } catch (Exception e) {
        }
        return null;
    }

    public void logImpression() {
        try {
            String url = String.format(
            		Locale.US,
            		IMPRESSION_ENDPOINT,
                    URLEncoder.encode(this._contentID, "UTF-8"),
                    URLEncoder.encode(this._imageID, "UTF-8"),
                    URLEncoder.encode(this._publisher, "UTF-8"),
                    URLEncoder.encode(this._mobilePlatform, "UTF-8"));
            new genericRequestTask().execute(url);
        } catch (UnsupportedEncodingException e) {
        }
    }
    public void logClickThrough() {
        try {
            String url = String.format(
            		Locale.US,
            		CLICKTHROUGH_ENDPOINT,
                    URLEncoder.encode(this._contentID, "UTF-8"),
                    URLEncoder.encode(this._imageID, "UTF-8"),
                    URLEncoder.encode(this._publisher, "UTF-8"),
                    URLEncoder.encode(this._mobilePlatform, "UTF-8"));
            new genericRequestTask().execute(url);
        } catch (UnsupportedEncodingException e) {
        }
    }
    public void logEvent(String eventName) {
        try {
            String url = String.format(
            		Locale.US,
            		EVENT_ENDPOINT,
                    URLEncoder.encode(this._contentID, "UTF-8"),
                    URLEncoder.encode(this._imageID, "UTF-8"),
                    URLEncoder.encode(this._publisher, "UTF-8"),
                    URLEncoder.encode(this._mobilePlatform, "UTF-8"),
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
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
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
