package com.sickboots.sickvideos.mainactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.database.YouTubeData;

import java.util.List;

public class ChannelSpinnerAdapter extends ArrayAdapter {
  List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()

 public ChannelSpinnerAdapter(Context context) {
   super(context, android.R.layout.simple_spinner_item, android.R.id.text1);

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

    String thumbnail = mChannels.get(position).mThumbnail;
    int defaultImageResID = 0;
    UrlImageViewHelper.setUrlDrawable(imageView, thumbnail, defaultImageResID, new UrlImageViewCallback() {

      @Override
      public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {

      }

    });

    return result;
  }

}


