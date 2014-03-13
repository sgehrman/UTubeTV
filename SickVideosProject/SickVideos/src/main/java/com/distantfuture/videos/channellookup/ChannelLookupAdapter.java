package com.distantfuture.videos.channellookup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.distantfuture.videos.R;
import com.distantfuture.videos.content.Content;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.imageutils.ToolbarIcons;
import com.distantfuture.videos.misc.Debug;

import java.util.List;

public class ChannelLookupAdapter extends ArrayAdapter<YouTubeData> {

  private final Context mContext;
  private final float mAspectRatio = 9f / 16f;
  private View.OnClickListener mButtonListener;
  private Content mContent;

  public ChannelLookupAdapter(Context context) {
    super(context, 0);
    this.mContext = context;

    this.mContent = Content.instance();

    mButtonListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        YouTubeData data = (YouTubeData) v.getTag();

        if (mContent.hasChannel(data.mChannel))
          mContent.removeChannel(data.mChannel);
        else
          mContent.addChannel(data.mChannel);

        // needed to update buttons when doing a search
        notifyDataSetChanged();
      }
    };
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    ViewHolder holder;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    YouTubeData data = getItem(position);

    if (convertView == null) {
      convertView = inflater.inflate(R.layout.channel_lookup_list_item, null);
      holder = new ViewHolder();
      holder.imgView = (ImageView) convertView.findViewById(R.id.imageView1);
      holder.addButton = (ImageView) convertView.findViewById(R.id.add_remove_button);
      holder.titleView = (TextView) convertView.findViewById(R.id.textView1);
      holder.descrView = (TextView) convertView.findViewById(R.id.textView2);
      convertView.setTag(holder);

      holder.addButton.setOnClickListener(mButtonListener);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    AQuery aq = new AQuery(convertView);
    aq.id(holder.imgView)
        .width(100)
        .image(data.mThumbnail, true, true, 0, 0, null, 0, mAspectRatio);
    aq.id(holder.titleView).text(data.mTitle);
    aq.id(holder.descrView).text(data.mDescription);

    // used for clicks
    holder.addButton.setTag(data);

    holder.addButton.setImageDrawable(buttonDrawable(mContext, !mContent.hasChannel(data.mChannel)));

    return convertView;
  }

  public void setData(List<YouTubeData> dataList) {
    clear();

    if (dataList != null) {
      addAll(dataList);
    }
  }

  private class ViewHolder {
    TextView titleView;
    TextView descrView;
    ImageView imgView;
    ImageView addButton;
  }

  private Drawable buttonDrawable(Context context, boolean plusButton) {
    if (plusButton)
      return ToolbarIcons.icon(context, ToolbarIcons.IconID.ADD, Color.GREEN, 36);

    return ToolbarIcons.icon(context, ToolbarIcons.IconID.REMOVE, Color.RED, 36);
  }
}
