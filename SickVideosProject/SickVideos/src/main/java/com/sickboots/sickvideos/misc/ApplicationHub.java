package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

// Three things this hub does:
// Notification center
// Global data
// Cached preferences

public class ApplicationHub {
  private static ApplicationHub instance = null;
  private HashMap data;
  private Handler mainThreadHandler;
  private NotificationCenter notificationCenter;
  private PreferenceCache mPrefsCache;
  private boolean mApplicationReady = false;

  // public notifications
  public static final String APPLICATION_READY_NOTIFICATION = "application_ready";

  private ApplicationHub(Context context) {
    notificationCenter = new NotificationCenter();
    mainThreadHandler = new Handler(Looper.getMainLooper());

    data = new HashMap();

    mPrefsCache = new PreferenceCache(context, new PreferenceCache.PreferenceCacheListener() {
      @Override
      public void prefsLoaded() {
        // called back on a thread
        mApplicationReady = true;
        sendNotification(APPLICATION_READY_NOTIFICATION);
      }
    });
  }

  public static ApplicationHub instance() {
    if (instance == null)
      Util.log("Must call ApplicationHub.init() first!");

    return instance;
  }

  public static PreferenceCache preferences() {
    return instance().prefsCache();
  }

  public PreferenceCache prefsCache() {
    if (!mApplicationReady) {
      Util.log("Application not ready you freakin moron");
      return null;
    }

    return mPrefsCache;
  }

  public static void init(Context applicationContext) {
    if (instance != null)
      Util.log("Only call ApplicationHub.init once.");
    else
      instance = new ApplicationHub(applicationContext);
  }

  // -------------------------------------
  // -------------------------------------
  // Global data

  public Object getData(String key) {
    return data.get(key);
  }

  public void setData(String key, Object value) {
    data.put(key, value);
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