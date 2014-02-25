package com.distantfuture.videos.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.imageutils.BitmapLoader;
import com.distantfuture.videos.imageutils.ToolbarIcons;

import java.util.List;

public class ChannelSpinnerAdapter extends ArrayAdapter<String> {
  private List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()
  private Context mContext;
  private Drawable mCheckDrawable;
  private BitmapLoader mBitmapLoader;

  public ChannelSpinnerAdapter(Context context) {
    super(context, android.R.layout.simple_spinner_item, android.R.id.text1);

    mContext = context.getApplicationContext();
    mBitmapLoader = new BitmapLoader(context, "drawerSpinner", 64, new BitmapLoader.GetBitmapCallback() {
      @Override
      public void onLoaded(Bitmap bitmap) {
        if (bitmap != null)  // avoid and endless loop update if bitmap is null, don't refresh
          ChannelSpinnerAdapter.this.notifyDataSetChanged();
      }
    });

    setDropDownViewResource(R.layout.view_channel_spinner);
  }

  public void updateChannels(List<YouTubeData> channels) {
    mChannels = channels;

    clear();
    for (YouTubeData data : mChannels)
      add(data.mTitle);
  }

  @Override
  public View getDropDownView(int position, View view, ViewGroup parent) {
    ViewHolder holder;
    if (view == null) {
      view = LayoutInflater.from(mContext).inflate(R.layout.view_channel_spinner, parent, false);
      holder = new ViewHolder();
      holder.imageView = (ImageView) view.findViewById(android.R.id.icon1);
      holder.textView = (CheckedTextView) view.findViewById(android.R.id.text1);
      view.setTag(holder);
    } else {
      holder = (ViewHolder) view.getTag();
    }

    final YouTubeData data = mChannels.get(position);

    // is this right?  seems crazy
    if (((ListView) parent).isItemChecked(position)) {

      if (mCheckDrawable == null) {
        mCheckDrawable = ToolbarIcons.icon(mContext, ToolbarIcons.IconID.CHECK, 0xff000000, 30);
        mCheckDrawable.setAlpha(60);
      }

      holder.textView.setCheckMarkDrawable(mCheckDrawable);
    } else
      holder.textView.setCheckMarkDrawable(null);

    holder.textView.setText(getItem(position));

    Bitmap bitmap = mBitmapLoader.bitmap(data);
    if (bitmap != null)
      holder.imageView.setImageBitmap(bitmap);
    else {
      mBitmapLoader.requestBitmap(data);
    }

    return view;
  }

  private static class ViewHolder {
    ImageView imageView;
    CheckedTextView textView;
  }
}
