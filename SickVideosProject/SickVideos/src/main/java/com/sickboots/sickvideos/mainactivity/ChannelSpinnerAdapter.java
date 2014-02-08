package com.sickboots.sickvideos.mainactivity;

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

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.imageutils.BitmapLoader;
import com.sickboots.sickvideos.imageutils.ToolbarIcons;

import java.util.List;

public class ChannelSpinnerAdapter extends ArrayAdapter<String> {
  private List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()
  private Context mContext;
  private Drawable mCheckDrawable;
  private BitmapLoader mBitmapLoader;

  public ChannelSpinnerAdapter(Context context) {
    super(context, android.R.layout.simple_spinner_item, android.R.id.text1);

    mContext = context.getApplicationContext();
    mBitmapLoader = new BitmapLoader(context);

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

    final int thumbnailSize = 64;

    Bitmap bitmap = mBitmapLoader.bitmap(data, thumbnailSize);
    if (bitmap != null)
      holder.imageView.setImageBitmap(bitmap);
    else {
      mBitmapLoader.requestBitmap(data, thumbnailSize, new BitmapLoader.GetBitmapCallback() {
        @Override
        public void onLoaded(Bitmap bitmap) {
          if (bitmap != null)  // avoid and endless loop update if bitmap is null, don't refresh
            ChannelSpinnerAdapter.this.notifyDataSetChanged();
        }
      });
    }

    return view;
  }

  private static class ViewHolder {
    ImageView imageView;
    CheckedTextView textView;
  }
}
