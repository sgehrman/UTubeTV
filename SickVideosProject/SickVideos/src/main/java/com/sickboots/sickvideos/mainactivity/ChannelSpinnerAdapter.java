package com.sickboots.sickvideos.mainactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.imageutils.ImageLoader;

import java.util.List;

public class ChannelSpinnerAdapter extends ArrayAdapter {
  List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()
  Context mContext;

  public ChannelSpinnerAdapter(Context context) {
    super(context, android.R.layout.simple_spinner_item, android.R.id.text1);

    mContext = context.getApplicationContext();

    setDropDownViewResource(R.layout.channel_spinner_item);
  }

  public void updateChannels(List<YouTubeData> channels) {
    mChannels = channels;

    clear();
    for (YouTubeData data : mChannels)
      add(data.mTitle);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return super.getView(position, convertView, parent);
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    View result = super.getDropDownView(position, convertView, parent);

    ImageView imageView = (ImageView) result.findViewById(android.R.id.icon1);
    final YouTubeData data = mChannels.get(position);

    Bitmap bitmap = ImageLoader.instance(mContext).bitmap(data);
    if (bitmap != null)
      imageView.setImageBitmap(bitmap);
    else {
      ImageLoader.instance(mContext).requestBitmap(data, new ImageLoader.GetBitmapCallback() {

        @Override
        public void onLoaded() {
          ChannelSpinnerAdapter.this.notifyDataSetChanged();
        }

      });
    }

    return result;
  }
}
