package com.distantfuture.videos.channellookup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.distantfuture.videos.R;
import com.distantfuture.videos.database.YouTubeData;

import java.util.List;

public class ChannelLookupAdapter extends ArrayAdapter<YouTubeData> {

  private final Context mContext;
  private final float mAspectRatio = 9f / 16f;

  public ChannelLookupAdapter(Context context) {
    super(context, 0);
    this.mContext = context;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    ViewHolder holder;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    YouTubeData mm = getItem(position);

    if (convertView == null) {
      convertView = inflater.inflate(R.layout.browse_row, null);
      holder = new ViewHolder();
      holder.imgView = (ImageView) convertView.findViewById(R.id.imageView1);
      holder.titleView = (TextView) convertView.findViewById(R.id.textView1);
      holder.descrView = (TextView) convertView.findViewById(R.id.textView2);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    AQuery aq = new AQuery(convertView);
    aq.id(holder.imgView)
        .width(110)
        .image(mm.mThumbnail, true, true, 0, R.drawable.default_video, null, 0, mAspectRatio);
    aq.id(holder.titleView).text(mm.mTitle);
    aq.id(holder.descrView).text(mm.mDescription);

    return convertView;
  }

  private class ViewHolder {
    TextView titleView;
    TextView descrView;
    ImageView imgView;
  }

  public void setData(List<YouTubeData> data) {
    clear();
    if (data != null) {
      for (YouTubeData item : data) {
        add(item);
      }
    }

  }
}
