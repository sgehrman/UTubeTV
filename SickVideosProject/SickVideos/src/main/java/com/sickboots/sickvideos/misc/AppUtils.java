package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Observable;
import java.util.Observer;

public class AppUtils {
  private static AppUtils instance = null;
  private Handler mainThreadHandler;
  private NotificationCenter notificationCenter;
  private Preferences mPrefsCache;
  private Context mApplicationContext;

  // public notifications
  public static final String THEME_CHANGED = "theme_changed";

  private AppUtils(Context context) {
    mApplicationContext = context.getApplicationContext();
    notificationCenter = new NotificationCenter();
    mainThreadHandler = new Handler(Looper.getMainLooper());

    mPrefsCache = new Preferences(mApplicationContext, new Preferences.PreferenceCacheListener() {
      @Override
      public void prefChanged(String prefName) {
        if (prefName.equals(Preferences.THEME_STYLE)) {
          sendNotification(THEME_CHANGED);
        }
      }
    });
  }

  public static AppUtils instance(Context context) {
    // make sure this is never null
    if (context == null) {
      Utils.log("### AppUtils instance: context null ###.");
      return null;
    }

    if (instance == null)
      instance = new AppUtils(context);

    return instance;
  }

  public static Preferences preferences(Context context) {
    return instance(context).prefsCache();
  }

  public String getAccountName() {
    return preferences(mApplicationContext).getString(Preferences.GOOGLE_ACCOUNT_PREF, null);
  }

  public void setAccountName(String accountName) {
    preferences(mApplicationContext).setString(Preferences.GOOGLE_ACCOUNT_PREF, accountName);
  }

  private Preferences prefsCache() {
    return mPrefsCache;
  }

  // -------------------------------------
  // -------------------------------------
  // Notification center

  public void runOnMainThread(Runnable action) {
    if (action != null)
      mainThreadHandler.post(action);
  }

  public void addObserver(Observer observer) {
    notificationCenter.addObserver(observer);
  }

  public void deleteObserver(Observer observer) {
    notificationCenter.deleteObserver(observer);
  }

  public void sendNotification(final String message) {
    // always sends on main thread
    runOnMainThread(new Runnable() {
      @Override
      public void run() {
        notificationCenter.sendNotification(message);
      }
    });
  }

  // -------------------------------------
  // Notification center class

  class NotificationCenter extends Observable {
    protected void sendNotification(String message) {
      setChanged();
      notifyObservers(message);
    }
  }
}