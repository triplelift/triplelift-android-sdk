package com.triplelift.sponsoredimages;

import android.os.NetworkOnMainThreadException;
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
    private static int READ_TIMEOUT = 20000; /* milliseconds */
    private static int CONN_TIMEOUT = 25000; /* milliseconds */
    
    private static String IBP_ENDPOINT = "http://ibp.3lift.com/ttj?inv_code=%s";
    
    // The url endpoints for getting images
    private String _invCode;
    private String _ibp_endpoint;

    public SponsoredImageFactory(String invCode) {
    	this._invCode = invCode;
        this._ibp_endpoint = String.format(Locale.US, IBP_ENDPOINT, invCode);
    }

    public SponsoredImage getSponsoredImage() throws NetworkOnMainThreadException, IOException, JSONException {
        String jsonString = downloadStringFromUrl(this._ibp_endpoint);

        JSONObject jsonObject = new JSONObject(jsonString);
        if(jsonObject.has("image_url")) {
            SponsoredImage sponsoredImage = new SponsoredImage(jsonObject, this._invCode, "android");
            return sponsoredImage;
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
        try {
            // Starts the request for the string
            is = new BufferedInputStream(conn.getInputStream());
            return readStream(is);
        } finally {
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
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