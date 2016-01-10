package com.triplelift.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.triplelift.demo.adapter.FeedListAdapter;
import com.triplelift.demo.app.AppController;
import com.triplelift.demo.data.FeedItem;
import com.triplelift.sdk.NativeAdAdapter;
import com.triplelift.sdk.NativeAdLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ListView listView;
    private FeedListAdapter listAdapter;
    private NativeAdAdapter nativeAdAdapter;
    private List<FeedItem> feedItems;
    private String URL_FEED = "http://s3.amazonaws.com/ops.triplelift.net/public/content.json";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);

        feedItems = new ArrayList<>();
        listAdapter = new FeedListAdapter(this, feedItems);

//        String placementId = "makersalley_main";
        String placementId = "makers_alley_370_174";


        NativeAdLayout nativeAdLayout = new NativeAdLayout(R.id.native_ad_brand_name,
                R.id.native_ad_image, 0, R.id.native_ad_title,
                R.id.native_ad_caption);

        nativeAdAdapter = new NativeAdAdapter(getApplicationContext(), listAdapter,
                placementId, R.layout.ad_item, 1, 3);

        nativeAdAdapter.registerNativeAdLayout(nativeAdLayout);


        listView.setAdapter(nativeAdAdapter);
//        listView.setAdapter(listAdapter);

        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(URL_FEED);
        if (entry != null) {
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    } else {
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, URL_FEED, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            VolleyLog.d(TAG, "Response: " + response.toString());
                            if (response != null) {
                                parseJsonFeed(response);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                        }

                    });
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                item.setId(feedObj.getInt("id"));
                item.setName(feedObj.getString("name"));

                String image = feedObj.isNull("image") ? null : feedObj.getString("image");
                item.setImage(image);
                item.setStatus(feedObj.getString("status"));
                item.setProfilePic(feedObj.getString("profilePic"));
                item.setTimeStamp(feedObj.getString("timeStamp"));

                String feedUrl = feedObj.isNull("url") ? null : feedObj.getString("url");
                item.setUrl(feedUrl);

                feedItems.add(item);
            }

            listAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onResume() {
        nativeAdAdapter.loadAds();
        super.onResume();
    }
}
