package com.sickboots.sickvideos.misc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.sickboots.sickvideos.R;

import de.greenrobot.event.EventBus;

public class AppUtils {
  private static AppUtils instance = null;
  private Handler mainThreadHandler;
  private Preferences mPreferences;
  private Context mApplicationContext;
  private ConnectionMonitor mConnectionMonitor;

  private AppUtils(Context context) {
    mApplicationContext = context.getApplicationContext();

    mainThreadHandler = new Handler(Looper.getMainLooper());
    mConnectionMonitor = new ConnectionMonitor(mApplicationContext);

    mPreferences = new Preferences(mApplicationContext, new Preferences.PreferenceCacheListener() {
      @Override
      public void prefChanged(String prefName) {
        if (prefName.equals(Preferences.THEME_STYLE)) {
          EventBus.getDefault().post(new Events.ThemeChanged());
        }
      }
    });
  }

  public static Uri companyPlayStoreUri() {
    return Uri.parse("market://search?q=pub:Sick Boots");
  }

  public static Uri applicationPlayStoreUri(Context context) {
    return Uri.parse("market://details?id=" + context.getApplicationInfo().packageName);
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

  public boolean hasNetworkConnection() {
    return mConnectionMonitor.hasNetworkConnection();
  }

  public void runOnMainThread(Runnable action) {
    if (action != null)
      mainThreadHandler.post(action);
  }

  public static void pickViewStyleDialog(final Context context) {

    AlertDialog.Builder builder = new AlertDialog.Builder(context);

    String themeStyle = AppUtils.preferences(context)
        .getString(Preferences.THEME_STYLE, Preferences.THEME_STYLE_DEFAULT);
    int selectedIndex = Integer.parseInt(themeStyle);

    builder.setTitle("Pick a style")
        .setSingleChoiceItems(R.array.view_styles, selectedIndex, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                AppUtils.preferences(context).setString(Preferences.THEME_STYLE, "0");
                break;
              case 1:
                AppUtils.preferences(context).setString(Preferences.THEME_STYLE, "1");
                break;
              case 2:
                AppUtils.preferences(context).setString(Preferences.THEME_STYLE, "2");
                break;
              case 3:
                AppUtils.preferences(context).setString(Preferences.THEME_STYLE, "3");
                break;
              default:
                break;
            }

            dialog.dismiss();
          }
        });

    builder.create().show();
  }

  // ================================================================
  // preferences
  // ================================================================

  public static Preferences preferences(Context context) {
    return instance(context).mPreferences;
  }

  public String getAccountName() {
    return preferences(mApplicationContext).getString("google_account", null);
  }

  public void setAccountName(String accountName) {
    preferences(mApplicationContext).setString("google_account", accountName);
  }

  public boolean alwaysPlayFullscreen() {
    return preferences(mApplicationContext).getBoolean(Preferences.PLAY_FULLSCREEN, false);
  }

  public boolean showHiddenItems() {
    return preferences(mApplicationContext).getBoolean("show_hidden_items", false);
  }

  public void setShowHiddenItems(boolean set) {
    preferences(mApplicationContext).setBoolean("show_hidden_items", set);
  }

  public boolean playNext() {
    return preferences(mApplicationContext).getBoolean("play_next", false);
  }

  public void setPlayNext(boolean set) {
    preferences(mApplicationContext).setBoolean("play_next", set);
  }

}