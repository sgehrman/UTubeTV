package com.sickboots.sickvideos.imageutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import com.sickboots.sickvideos.database.YouTubeData;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class BitmapLoader {
  private BitmapDiskCache mDiskCache;
  private Context mContext;
  private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(20);  // 20 is arbitrary, may adjust later

  public BitmapLoader(Context context) {
    mContext = context.getApplicationContext();
    mDiskCache = newCache();
  }

  public void refresh() {
    mDiskCache.clearCache();
    mDiskCache = newCache(); // cache must be recreated since clear closed it

    mLruCache.evictAll();
  }

  private BitmapDiskCache newCache() {
    final long diskCacheSize = 10 * 1024 * 1024;  // 10mb

    return new BitmapDiskCache(mContext, "bitmaps", diskCacheSize, Bitmap.CompressFormat.PNG, 0);
  }

  private String keyForChannel(String channelId, int thumbnailSize) {
    // keys must match regex [a-z0-9_-]{1,64}
    String result = channelId + thumbnailSize;
    result = result.toLowerCase();

    return result;
  }

  private void callCallbackOnMainThread(final Bitmap bitmap, final GetBitmapCallback callback) {
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        // on main thread
        callback.onLoaded(bitmap);
      }
    });
  }

  public Bitmap bitmap(YouTubeData data, int thumbnailSize) {
    final String key = keyForChannel(data.mChannel, thumbnailSize);

    // in our memory cache?
    return mLruCache.get(key);
  }

  public void requestBitmap(final YouTubeData data, final int thumbnailSize, final GetBitmapCallback callback) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        final String key = keyForChannel(data.mChannel, thumbnailSize);

        // is it in our disk cache?
        Bitmap result = mDiskCache.getBitmap(key);

        if (result != null) {
          mLruCache.put(key, result);
          callCallbackOnMainThread(result, callback);
        } else {
          Bitmap bitmap;
          try {
            RequestCreator requestCreator = Picasso.with(mContext)
                .load(data.mThumbnail)
                .skipMemoryCache();

            if (thumbnailSize != 0)
              requestCreator = requestCreator.resize(thumbnailSize, thumbnailSize);

            bitmap = requestCreator.get();

            if (bitmap != null) {
              // save image to our caches
              mDiskCache.put(key, bitmap);
              mLruCache.put(key, bitmap);
            }

            callCallbackOnMainThread(bitmap, callback);

          } catch (Throwable t) {
          }
        }
      }
    }).start();

  }

  public interface GetBitmapCallback {
    public void onLoaded(Bitmap bitmap);
  }
}
