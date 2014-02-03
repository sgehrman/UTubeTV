package com.sickboots.sickvideos.imageutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Debug;

/**
 * Created by sgehrman on 2/2/14.
 */
public class ImageLoader {
  private static ImageLoader instance = null;
  private BitmapCache mCache;
  private Context mContext;

  private ImageLoader(Context context) {
    mContext = context.getApplicationContext();
    mCache = newCache();
  }

  public static ImageLoader instance(Context context) {
    // make sure this is never null
    if (context == null) {
      Debug.log("### ImageLoader instance: context null ###.");
      return null;
    }

    if (instance == null)
      instance = new ImageLoader(context);

    return instance;
  }

  public void refresh() {
    mCache.clearCache();

    // cache must be recreated since clear closed it
    mCache = newCache();
  }

  private BitmapCache newCache() {
    final long diskCacheSize = 10 * 1024 * 1024;  // 10mb

    return new BitmapCache(mContext, "bitmaps", diskCacheSize, Bitmap.CompressFormat.PNG, 0);
  }

  public Bitmap get(String key) {
    // keys must match regex [a-z0-9_-]{1,64}
    key = key.toLowerCase();

    return mCache.getBitmap(key);
  }

  public void put(String key, Bitmap data) {
    // keys must match regex [a-z0-9_-]{1,64}
    key = key.toLowerCase();

    mCache.put(key, data);
  }

  public interface GetBitmapCallback {
    public void onLoaded();
  }

  public Bitmap bitmap(final YouTubeData data) {
    return get(data.mChannel);
  }

  public void requestBitmap(final YouTubeData data, final GetBitmapCallback callback) {
    Bitmap loadedBitmap = get(data.mChannel);

    if (loadedBitmap != null) {
      callback.onLoaded();
    } else {
      int defaultImageResID = 0;
      UrlImageViewHelper.loadUrlDrawable(mContext, data.mThumbnail, 100, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          put(data.mChannel, loadedBitmap);

          callback.onLoaded();
        }

      });
    }
  }
}
