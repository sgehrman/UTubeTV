package com.distantfuture.castcompanionlibrary.lib.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.distantfuture.castcompanionlibrary.lib.BuildConfig;
import com.distantfuture.castcompanionlibrary.lib.R;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.images.WebImage;

import java.util.ArrayList;

/**
 * A collection of utility methods, all static.
 */
public class CastUtils {

  private static final String TAG = CastUtils.makeLogTag(CastUtils.class);
  private static final String KEY_IMAGES = "images";
  private static final String KEY_URL = "movie-urls";
  private static final String KEY_CONTENT_TYPE = "content-type";
  private static final String KEY_STREAM_TYPE = "stream-type";

  /**
   * Formats time in milliseconds to hh:mm:ss string format.
   */
  public static String formatMillis(int millis) {
    String result = "";
    int hr = millis / 3600000;
    millis %= 3600000;
    int min = millis / 60000;
    millis %= 60000;
    int sec = millis / 1000;
    if (hr > 0) {
      result += hr + ":";
    }
    if (min >= 0) {
      if (min > 9) {
        result += min + ":";
      } else {
        result += "0" + min + ":";
      }
    }
    if (sec > 9) {
      result += sec;
    } else {
      result += "0" + sec;
    }
    return result;
  }

  /**
   * A utility method to show a simple error dialog. The textual content of the dialog is provided
   * through the passed-in resource id.
   */
  public static void showErrorDialog(Context context, int resourceId) {
    showErrorDialog(context, context.getString(resourceId));
  }

  /**
   * A utility method to show a simple error dialog.
   * <p/>
   * param message The message to be shown in the dialog
   */
  public static void showErrorDialog(Context context, String message) {
    new AlertDialog.Builder(context).setTitle(R.string.error)
        .setMessage(message)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        })
        .create()
        .show();
  }

  /**
   * Returns the URL of an image for the MediaInformation at the given level. Level should
   * be a number between 0 and <code>n - 1</code> where <code>n
   * </code> is the number of images for that given item.
   */
  public static String getImageUrl(MediaInfo info, int level) {
    MediaMetadata mm = info.getMetadata();
    if (null != mm && null != mm.getImages() && mm.getImages().size() > level) {
      return mm.getImages().get(level).getUrl().toString();
    }
    return null;
  }

  /**
   * Saves a string value under the provided key in the preference manager. If <code>value</code>
   * is <code>null</code>, then the provided key will be removed from the preferences.
   */
  public static void saveStringToPreference(Context context, String key, String value) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    if (null == value) {
      // we want to remove
      pref.edit().remove(key).apply();
    } else {
      pref.edit().putString(key, value).apply();
    }
  }

  /**
   * Saves a float value under the provided key in the preference manager. If <code>value</code>
   * is <code>Float.MIN_VALUE</code>, then the provided key will be removed from the preferences.
   */
  public static void saveFloatToPreference(Context context, String key, float value) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    if (Float.MIN_VALUE == value) {
      // we want to remove
      pref.edit().remove(key).apply();
    } else {
      pref.edit().putFloat(key, value).apply();
    }

  }

  /**
   * Retrieves a String value from preference manager. If no such key exists, it will return
   * <code>null</code>.
   */
  public static String getStringFromPreference(Context context, String key) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getString(key, null);
  }

  /**
   * Retrieves a float value from preference manager. If no such key exists, it will return
   * <code>Float.MIN_VALUE</code>.
   */
  public static float getFloatFromPreference(Context context, String key) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getFloat(key, Float.MIN_VALUE);
  }

  /**
   * Retrieves a boolean value from preference manager. If no such key exists, it will return the
   * value provided as <code>defaultValue</code>
   */
  public static boolean getBooleanFromPreference(Context context, String key, boolean defaultValue) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getBoolean(key, defaultValue);
  }

  /**
   * A utility method to validate that the appropriate version of the Google Play Services is
   * available on the device. If not, it will open a dialog to address the issue. The dialog
   * displays a localized message about the error and upon user confirmation (by tapping on
   * dialog) will direct them to the Play Store if Google Play services is out of date or missing,
   * or to system settings if Google Play services is disabled on the device.
   */
  public static boolean checkGooglePlayServices(final Activity activity) {
    final int googlePlayServicesCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
    switch (googlePlayServicesCheck) {
      case ConnectionResult.SUCCESS:
        return true;
      default:
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck, activity, 0);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialogInterface) {
            activity.finish();
          }
        });
        dialog.show();
    }
    return false;
  }

  /**
   * Builds and returns a {@link Bundle} which contains a select subset of data in the
   * {@link MediaInfo}. Since {@link MediaInfo} is not {@link Parcelable}, one can use this
   * container bundle to pass around from one activity to another.
   */
  public static Bundle fromMediaInfo(MediaInfo info) {
    if (null == info) {
      return null;
    }

    MediaMetadata md = info.getMetadata();
    Bundle wrapper = new Bundle();
    wrapper.putString(MediaMetadata.KEY_TITLE, md.getString(MediaMetadata.KEY_TITLE));
    wrapper.putString(MediaMetadata.KEY_SUBTITLE, md.getString(MediaMetadata.KEY_SUBTITLE));
    wrapper.putString(KEY_URL, info.getContentId());
    wrapper.putString(MediaMetadata.KEY_STUDIO, md.getString(MediaMetadata.KEY_STUDIO));
    wrapper.putString(KEY_CONTENT_TYPE, info.getContentType());
    wrapper.putInt(KEY_STREAM_TYPE, info.getStreamType());
    if (null != md.getImages()) {
      ArrayList<String> urls = new ArrayList<String>();
      for (WebImage img : md.getImages()) {
        urls.add(img.getUrl().toString());
      }
      wrapper.putStringArrayList(KEY_IMAGES, urls);
    }

    return wrapper;
  }

  /**
   * Builds and returns a {@link MediaInfo} that was wrapped in a {@link Bundle} by
   */
  public static MediaInfo toMediaInfo(Bundle wrapper) {
    if (null == wrapper) {
      return null;
    }

    MediaMetadata metaData = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

    metaData.putString(MediaMetadata.KEY_SUBTITLE, wrapper.getString(MediaMetadata.KEY_SUBTITLE));
    metaData.putString(MediaMetadata.KEY_TITLE, wrapper.getString(MediaMetadata.KEY_TITLE));
    metaData.putString(MediaMetadata.KEY_STUDIO, wrapper.getString(MediaMetadata.KEY_STUDIO));
    ArrayList<String> images = wrapper.getStringArrayList(KEY_IMAGES);
    if (null != images && !images.isEmpty()) {
      for (String url : images) {
        Uri uri = Uri.parse(url);
        metaData.addImage(new WebImage(uri));
      }
    }
    return new MediaInfo.Builder(wrapper.getString(KEY_URL)).setStreamType(wrapper.getInt(KEY_STREAM_TYPE))
        .setContentType(wrapper.getString(KEY_CONTENT_TYPE))
        .setMetadata(metaData)
        .build();
  }

  public static String makeLogTag(String str) {
    final String LOG_PREFIX = "ccl_";
    final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    final int MAX_LOG_TAG_LENGTH = 23;

    if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
      return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
    }

    return LOG_PREFIX + str;
  }

  /**
   * WARNING: Don't use this when obfuscating class names with Proguard!
   */
  public static String makeLogTag(Class<?> cls) {
    return makeLogTag(cls.getSimpleName());
  }

  public static void LOGD(final String tag, String message) {
    if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, message);
    }
  }

  public static void LOGD(final String tag, String message, Throwable cause) {
    if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, message, cause);
    }
  }

  public static void LOGE(final String tag, String message) {
    Log.e(tag, message);
  }

  public static void LOGE(final String tag, String message, Throwable cause) {
    Log.e(tag, message, cause);
  }

}
