package com.distantfuture.videos.channellookup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.cocosw.undobar.UndoBarController;
import com.distantfuture.videos.R;
import com.distantfuture.videos.content.Content;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.imageutils.ToolbarIcons;

import java.util.List;

public class ChannelLookupAdapter extends ArrayAdapter<YouTubeData> {

  private final Activity mActivity;
  private final float mAspectRatio = 4f / 5f;
  private View.OnClickListener mButtonListener;
  private Content mContent;

  public ChannelLookupAdapter(Activity activity) {
    super(activity, 0);
    this.mActivity = activity;

    this.mContent = Content.instance();

    mButtonListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        YouTubeData data = (YouTubeData) v.getTag();

        if (mContent.hasChannel(data.mChannel)) {
          if (mContent.removeChannel(data.mChannel))
            showUndoBar(data.mChannel);
        } else
          mContent.addChannel(data.mChannel);

        notifyDataSetChanged();  // needed to refresh find results
      }
    };
  }

  private void showUndoBar(String channelId) {
    UndoBarController.UndoListener listener = new UndoBarController.UndoListener() {
      @Override
      public void onUndo(Parcelable parcelable) {
        // was getting crashes here, this fixed it.  I think quickly double tapping the undo button triggers this
        if (parcelable == null) {
          return;
        }

        Bundle info = (Bundle) parcelable;
        String channelId = info.getString("channelId");

        mContent.addChannel(channelId);
        notifyDataSetChanged();  // needed to refresh find results
      }
    };
    Bundle info = new Bundle();
    info.putString("channelId", channelId);
    UndoBarController.show(mActivity, "Channel removed", listener, info);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    ViewHolder holder;
    LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        .width(90)
        .image(data.mThumbnail, true, true, 0, 0, null, 0, mAspectRatio);
    aq.id(holder.titleView).text(data.mTitle);
    aq.id(holder.descrView).text(data.mDescription);

    // used for clicks
    holder.addButton.setTag(data);

    holder.addButton.setImageDrawable(buttonDrawable(mActivity, !mContent.hasChannel(data.mChannel)));

    return convertView;
  }

  public void setData(List<YouTubeData> dataList) {
    clear();

    if (dataList != null) {
      addAll(dataList);
    }
  }

  private Drawable buttonDrawable(Context context, boolean plusButton) {
    if (plusButton)
      return ToolbarIcons.icon(context, ToolbarIcons.IconID.ADD, 0xff009900, 36);

    return ToolbarIcons.icon(context, ToolbarIcons.IconID.REMOVE, Color.RED, 36);
  }

  private class ViewHolder {
    TextView titleView;
    TextView descrView;
    ImageView imgView;
    ImageView addButton;
  }
}
