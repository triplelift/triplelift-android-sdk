package com.triplelift.demo.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.triplelift.demo.R;
import com.triplelift.demo.app.AppController;
import com.triplelift.demo.data.FeedItem;

import java.util.List;

public class FeedListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    ImageLoader imageLoader;

    static class ViewHolder {
        TextView title;
        TextView subtitle;
        TextView statusMsg;
        NetworkImageView logo;
        NetworkImageView mainImage;
    }

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int location) {
        return feedItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        //if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed_item, null);
        //}
        if (imageLoader == null) {
            imageLoader = AppController.getInstance().getImageLoader();
        }

        TextView title = (TextView) convertView.findViewById(R.id.name);
        TextView subtitle = (TextView) convertView.findViewById(R.id.timestamp);
        TextView statusMsg = (TextView) convertView.findViewById(R.id.txtStatusMsg);
        TextView url = (TextView) convertView.findViewById(R.id.txtUrl);
        NetworkImageView profilePic = (NetworkImageView) convertView.findViewById(R.id.profilePic);
        NetworkImageView feedImageView = (NetworkImageView) convertView.findViewById(R.id.feedImage1);

        FeedItem item = feedItems.get(position);

        title.setText(item.getName());

        subtitle.setText(item.getTimeStamp());

        if (!TextUtils.isEmpty(item.getStatus())) {
            statusMsg.setText(item.getStatus());
            statusMsg.setVisibility(View.VISIBLE);
        } else {
            statusMsg.setVisibility(View.GONE);
        }

        if (item.getUrl() != null) {
            url.setText(
                    Html.fromHtml("<a href=\"" + item.getUrl() + "\">" + item.getUrl() + "</a> "));
            url.setMovementMethod(LinkMovementMethod.getInstance());
            url.setVisibility(View.VISIBLE);
        } else {
            url.setVisibility(View.GONE);
        }


        profilePic.setImageUrl(item.getProfilePic(), imageLoader);

        if (item.getImage() != null) {

//            ImageAware imageAware = new ImageViewAware(feedImageView, false);
//            imageLoader.displayImage(imageUri, imageAware);

            feedImageView.setImageUrl(item.getImage(), imageLoader);
            //feedImageView.setVisibility(View.VISIBLE);

        } else {
            //feedImageView.setVisibility(View.GONE);
        }
        return convertView;
    }
}
