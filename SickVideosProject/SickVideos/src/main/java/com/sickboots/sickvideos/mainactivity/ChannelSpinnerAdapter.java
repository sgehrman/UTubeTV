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
import com.sickboots.sickvideos.misc.BitmapCache;

import java.util.List;

public class ChannelSpinnerAdapter extends ArrayAdapter {
  List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()
  BitmapCache mBitmapCache;
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
    convertView = null;  // reusing views is buggy with images, fix later
    View result = super.getDropDownView(position, convertView, parent);

    ImageView imageView = (ImageView) result.findViewById(android.R.id.icon1);
    final YouTubeData data = mChannels.get(position);

    // is the bitmap in our diskcache?
    Bitmap bm = cachedBitmap(data);

    if (bm != null) {
      imageView.setImageBitmap(bm);
    } else {
      String thumbnail = data.mThumbnail;
      int defaultImageResID = 0;
      UrlImageViewHelper.setUrlDrawable(imageView, thumbnail, defaultImageResID, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          if (mBitmapCache != null)
            mBitmapCache.put(BitmapCache.cacheKey(data), loadedBitmap);
        }

      });
    }

    return result;
  }

  private Bitmap cachedBitmap(YouTubeData data) {
    if (mBitmapCache == null)
      mBitmapCache = BitmapCache.newInstance(mContext, BitmapCache.channelImageCacheName());

    if (mBitmapCache != null)
      return mBitmapCache.getBitmap(BitmapCache.cacheKey(data));

    return null;
  }
}
