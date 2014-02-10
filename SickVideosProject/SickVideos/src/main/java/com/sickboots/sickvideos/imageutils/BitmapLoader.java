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

            // put thumbnails in a circle
            if (bitmap != null && thumbnailSize != 0) {
              int width = thumbnailSize;
              int height = thumbnailSize;

              Bitmap circleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
              Canvas canvas = new Canvas(circleBitmap);

              // draw using circle into another bitmap, add shadows and shit
              int centerX = width / 2, centerY = height / 2, radius = (width / 2);

              BitmapShader s = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

              Paint p = new Paint();
              p.setShader(s);
              p.setAntiAlias(true);

              canvas.drawCircle(centerX, centerY, radius, p);

              p = new Paint();
              p.setAntiAlias(true);
              p.setStyle(Paint.Style.STROKE);
              p.setStrokeWidth(2.0f);
              p.setColor(0x99FFFFFF);

              canvas.drawCircle(centerX, centerY, radius - 2, p);

              // gloss
              //              p = new Paint();
              //              p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
              //
              //              canvas.drawCircle(centerX, centerY, radius, p);


              bitmap = circleBitmap;
            }

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
