package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;
import com.sickboots.sickvideos.database.YouTubeData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BitmapCache {
  private static final int APP_VERSION = 1;
  private static final int VALUE_COUNT = 1;
  private DiskLruCache mDiskCache;
  private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.PNG;
  private int mCompressQuality = 70;

  public BitmapCache(Context context, String uniqueName, long diskCacheSize,
                     Bitmap.CompressFormat compressFormat, int quality) {
    try {
      final File diskCacheDir = getDiskCacheDir(context, uniqueName);
      mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
      mCompressFormat = compressFormat;
      mCompressQuality = quality;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // convenience method for common cache setup
  public static BitmapCache newInstance(Context context, String name) {
      final long diskCacheSize = 10 * 1024 * 1024;  // 10mb
    return new BitmapCache(context, name, diskCacheSize, Bitmap.CompressFormat.PNG, 0);
  }

  public static String channelImageCacheName() {
    return "channel_images";
  }

  // convenience method so we use the same cacheKey in multiple place
  public static String cacheKey(YouTubeData data) {
    // keys must match regex [a-z0-9_-]{1,64}
    // assuming the channel id is OK
    String result = data.mChannel.toLowerCase();

    return result;
  }

  private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
      throws IOException, FileNotFoundException {
    OutputStream out = null;
    try {
      out = new BufferedOutputStream(editor.newOutputStream(0), CacheUtils.IO_BUFFER_SIZE);
      return bitmap.compress(mCompressFormat, mCompressQuality, out);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  private File getDiskCacheDir(Context context, String uniqueName) {
    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
    // otherwise use internal cache dir
    final String cachePath =
        Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
            !CacheUtils.isExternalStorageRemovable() ?
            CacheUtils.getExternalCacheDir(context).getPath() :
            context.getCacheDir().getPath();

    return new File(cachePath + File.separator + uniqueName);
  }

  public void put(String key, Bitmap data) {
    DiskLruCache.Editor editor = null;
    try {
      editor = mDiskCache.edit(key);
      if (editor == null) {
        return;
      }

      if (writeBitmapToFile(data, editor)) {
        mDiskCache.flush();
        editor.commit();
        if (Debug.isDebugBuild()) {
          Debug.log("image put on disk cache " + key + "size: " + ((float)mDiskCache.size() / 1024.0f) + "k");
        }
      } else {
        editor.abort();
        if (Debug.isDebugBuild()) {
          Debug.log("ERROR on: image put on disk cache " + key);
        }
      }
    } catch (IOException e) {
      if (Debug.isDebugBuild()) {
        Debug.log("ERROR on: image put on disk cache " + key);
      }
      try {
        if (editor != null) {
          editor.abort();
        }
      } catch (IOException ignored) {
      }
    }
  }

  public Bitmap getBitmap(String key) {

    Bitmap bitmap = null;
    DiskLruCache.Snapshot snapshot = null;
    try {

      snapshot = mDiskCache.get(key);
      if (snapshot == null) {
        return null;
      }
      final InputStream in = snapshot.getInputStream(0);
      if (in != null) {
        final BufferedInputStream buffIn =
            new BufferedInputStream(in, CacheUtils.IO_BUFFER_SIZE);
        bitmap = BitmapFactory.decodeStream(buffIn);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (snapshot != null) {
        snapshot.close();
      }
    }

    if (Debug.isDebugBuild())
      Debug.log("bitmap read from cache: " + ((bitmap == null) ? "null" : key));

    return bitmap;
  }

  public boolean containsKey(String key) {

    boolean contained = false;
    DiskLruCache.Snapshot snapshot = null;
    try {
      snapshot = mDiskCache.get(key);
      contained = snapshot != null;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (snapshot != null) {
        snapshot.close();
      }
    }

    return contained;
  }

  public void clearCache() {
    if (Debug.isDebugBuild()) {
      Debug.log("disk cache CLEARED");
    }
    try {
      mDiskCache.delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public File getCacheFolder() {
    return mDiskCache.getDirectory();
  }

  public static class CacheUtils {
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    private CacheUtils() {
    }

    public static boolean isExternalStorageRemovable() {
      return Environment.isExternalStorageRemovable();
    }

    public static File getExternalCacheDir(Context context) {
      return context.getExternalCacheDir();
    }
  }
}


