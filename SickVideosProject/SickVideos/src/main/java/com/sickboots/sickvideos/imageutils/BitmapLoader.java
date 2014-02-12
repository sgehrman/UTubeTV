package com.sickboots.sickvideos.imageutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Debug;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.HashSet;
import java.util.Set;

public class BitmapLoader {
  private BitmapDiskCache mDiskCache;
  private Context mContext;
  private String mCacheName;
  private int mThumbnailSize;
  private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(20);  // 20 is arbitrary, may adjust later
  private Paint mThumbnailFillPaint;
  private Paint mThumbnailStrokePaint;
  private Set<String> mProcessingKeys=new HashSet<String>();
  private GetBitmapCallback mCallback;

  public BitmapLoader(Context context, String cacheName, int thumbnailSize, GetBitmapCallback callback) {
    mCacheName = cacheName;
    mCallback = callback;
    mThumbnailSize = thumbnailSize;
    mContext = context.getApplicationContext();
    mDiskCache = newCache();

    if (mThumbnailSize > 0) {
      mThumbnailFillPaint = new Paint();
      mThumbnailFillPaint.setAntiAlias(true);

      mThumbnailStrokePaint = new Paint();
      mThumbnailStrokePaint.setAntiAlias(true);
      mThumbnailStrokePaint.setStyle(Paint.Style.STROKE);
      mThumbnailStrokePaint.setStrokeWidth(2.0f);
      mThumbnailStrokePaint.setColor(0x99FFFFFF);
    }
  }

  public void refresh() {
    mDiskCache.clearCache();
    mDiskCache = newCache(); // cache must be recreated since clear closed it

    mLruCache.evictAll();
  }

  private BitmapDiskCache newCache() {
    final long diskCacheSize = 10 * 1024 * 1024;  // 10mb

    return new BitmapDiskCache(mContext, mCacheName, diskCacheSize, Bitmap.CompressFormat.PNG, 0);
  }

  private String keyForChannel(String channelId) {
    // keys must match regex [a-z0-9_-]{1,64}
    return channelId.toLowerCase();
  }

  private void callCallbackOnMainThread(final Bitmap bitmap, final String key) {
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        // on main thread
        mProcessingKeys.remove(key);

        mCallback.onLoaded(bitmap);
      }
    });
  }

  public Bitmap bitmap(YouTubeData data) {
    final String key = keyForChannel(data.mChannel);

    // in our memory cache?
    return mLruCache.get(key);
  }

  public void requestBitmap(final YouTubeData data) {
    final String key = keyForChannel(data.mChannel);

    // ignore duplicate request
    if (mProcessingKeys.contains(key))
      return;

    mProcessingKeys.add(key);

    new Thread(new Runnable() {
      @Override
      public void run() {

        // is it in our disk cache?
        Bitmap result = mDiskCache.getBitmap(key);

        if (result != null) {
          mLruCache.put(key, result);
          callCallbackOnMainThread(result, key);
        } else {
          Bitmap bitmap;
          try {
            RequestCreator requestCreator = Picasso.with(mContext)
                .load(data.mThumbnail)
                .skipMemoryCache();

            if (mThumbnailSize > 0)
              requestCreator = requestCreator.resize(mThumbnailSize, mThumbnailSize);

            bitmap = requestCreator.get();

            // put thumbnails in a circle
            if (bitmap != null && mThumbnailSize > 0) {
              int width = mThumbnailSize;
              int height = mThumbnailSize;

              Bitmap circleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
              Canvas canvas = new Canvas(circleBitmap);

              // draw using circle into another bitmap, add shadows and shit
              int centerX = width / 2, centerY = height / 2, radius = (width / 2);

              BitmapShader s = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
              mThumbnailFillPaint.setShader(s);

              canvas.drawCircle(centerX, centerY, radius, mThumbnailFillPaint);
              canvas.drawCircle(centerX, centerY, radius - 2, mThumbnailStrokePaint);

              bitmap = circleBitmap;
            }

            if (bitmap != null) {
              // save image to our caches
              mDiskCache.put(key, bitmap);
              mLruCache.put(key, bitmap);
            }

            callCallbackOnMainThread(bitmap, key);

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
