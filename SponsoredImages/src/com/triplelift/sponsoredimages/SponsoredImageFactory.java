package com.triplelift.sponsoredimages;

import android.os.NetworkOnMainThreadException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by bdlee on 6/12/13.
 */
public class SponsoredImageFactory {
    private static int READ_TIMEOUT = 10000; /* milliseconds */
    private static int CONN_TIMEOUT = 15000; /* milliseconds */

    // The url endpoints for getting images
    private String _appNexusAuctionEndpoint;
    // publisher ID
    private String _publisher;

    public SponsoredImageFactory(String tagCode, String publisher) {
        this._publisher = publisher;
        this._appNexusAuctionEndpoint = String.format(Locale.US, "http://ib.adnxs.com/ttj?inv_code=%s&member=1314", tagCode);
    }

    public SponsoredImage getSponsoredImage() throws NetworkOnMainThreadException, IOException, JSONException {
        String creativeCode = downloadStringFromUrl(this._appNexusAuctionEndpoint);
        String sponsoredImageEndpoint = String.format(Locale.US, "http://dynamic.3lift.com/live_stream/%s",creativeCode);

        String sponsoredImageData = downloadStringFromUrl(sponsoredImageEndpoint);

        if(sponsoredImageData.startsWith("live_stream(") && sponsoredImageData.endsWith(");")) {
            String jsonString = sponsoredImageData.substring(12, sponsoredImageData.length() - 2);

            JSONObject jsonObject = new JSONObject(jsonString);
            if(jsonObject.has("trending_items")) {
                JSONArray sponsoredImages = jsonObject.getJSONArray("trending_items");
                if(sponsoredImages.length() > 0) {
                    int i = (int) Math.floor(Math.random() * sponsoredImages.length());
                    JSONObject imageData = sponsoredImages.getJSONObject(i);

                    SponsoredImage sponsoredImage = new SponsoredImage(imageData, this._publisher, creativeCode, "android");
                    return sponsoredImage;
                }
            }
        } else {
            throw new JSONException("Incorrect format received in TripleLift live stream");
        }

        return null;
    }

    private String downloadStringFromUrl(String urlString) throws IOException {
        InputStream is = null;

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setConnectTimeout(CONN_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        try {
            is = new BufferedInputStream(conn.getInputStream());
            return readStream(is);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            conn.disconnect();
            if(is != null) {
                is.close();
            }
        }
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }
}