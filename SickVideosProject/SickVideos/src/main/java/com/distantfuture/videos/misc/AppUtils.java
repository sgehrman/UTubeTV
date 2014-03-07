package com.distantfuture.videos.misc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.distantfuture.videos.R;

import de.greenrobot.event.EventBus;

public class AppUtils {
  private static AppUtils instance = null;
  private Handler mainThreadHandler;
  private Preferences mPreferences;
  private ConnectionMonitor mConnectionMonitor;

  private AppUtils(Context context) {
    context = context.getApplicationContext();

    mainThreadHandler = new Handler(Looper.getMainLooper());
    mConnectionMonitor = new ConnectionMonitor(context);

    mPreferences = new Preferences(context, new Preferences.PreferenceCacheListener() {
      @Override
      public void prefChanged(String prefName) {
        if (prefName.equals("theme_id")) {
          EventBus.getDefault().post(new Events.ThemeChanged());
        }
      }
    });
  }

  public static AppUtils instance(Context context) {
    // make sure this is never null
    if (context == null) {
      Debug.log("### AppUtils instance: context null ###.");
      return null;
    }

    if (instance == null)
      instance = new AppUtils(context);

    return instance;
  }

  public static void pickViewStyleDialog(final Context context) {

    AlertDialog.Builder builder = new AlertDialog.Builder(context);

    int themeStyle = AppUtils.instance(context).themeId();

    builder.setTitle("Pick a style")
        .setSingleChoiceItems(R.array.view_styles, themeStyle, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            AppUtils.instance(context).setThemeId(which);

            dialog.dismiss();
          }
        });

    builder.create().show();
  }

  public boolean hasNetworkConnection() {
    return mConnectionMonitor.hasNetworkConnection();
  }

  public void runOnMainThread(Runnable action) {
    if (action != null)
      mainThreadHandler.post(action);
  }

  // ================================================================
  // preferences
  // ================================================================

  public String getAccountName() {
    return mPreferences.getString("google_account", null);
  }

  public void setAccountName(String accountName) {
    mPreferences.setString("google_account", accountName);
  }

  public boolean alwaysPlayFullscreen() {
    return mPreferences.getBoolean("play_fullscreen", false);
  }

  public boolean showHiddenItems() {
    return mPreferences.getBoolean("show_hidden_items", false);
  }

  public void setShowHiddenItems(boolean set) {
    mPreferences.setBoolean("show_hidden_items", set);
  }

  public boolean playNext() {
    return mPreferences.getBoolean("play_next", false);
  }

  public boolean repeatVideo() {
    return mPreferences.getBoolean("repeat_video", false);
  }

  public boolean muteAds() {
    return mPreferences.getBoolean("mute_ads", false);
  }

  public boolean showDevTools() {
    return mPreferences.getBoolean("show_dev_tools", false);
  }

  public int themeId() {
    return mPreferences.getInt("theme_id", 3);
  }

  public void setThemeId(int set) {
    mPreferences.setInt("theme_id", set);
  }

  public int savedSectionIndex(String currentChannelId) {
    return mPreferences.getInt(sectionPrefsKey(currentChannelId), 0);
  }

  // we save the last requested drawerSelection as requested
  public void saveSectionIndex(int sectionIndex, String currentChannelId) {
    mPreferences.setInt(sectionPrefsKey(currentChannelId), sectionIndex);
  }

  public String sectionPrefsKey(String currentChannelId) {
    return "section_index" + currentChannelId;
  }

  public String defaultChannelID(String defaultValue) {
    return mPreferences.getString("channel_index", defaultValue);
  }

  // we save the last requested drawerSelection as requested
  public void saveDefaultChannelID(String channelId) {
    mPreferences.setString("channel_index", channelId);
  }

}