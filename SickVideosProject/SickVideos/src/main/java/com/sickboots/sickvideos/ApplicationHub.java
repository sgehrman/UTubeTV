package com.sickboots.sickvideos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

// Three things this hub does:
// Notification center
// Global data
// Cached preferences

public class ApplicationHub implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static ApplicationHub instance = null;
  private SharedPreferences sharedPreferences;
  private HashMap data;
  private HashMap prefs;
  private Handler mainThreadHandler;
  private NotificationCenter notificationCenter;
  private boolean mApplicationReady = false;

  // public pref keys
  public static final String GOOGLE_ACCOUNT_PREF = "google_account";
  public static final String APPLICATION_READY_NOTIFICATION = "application_ready";

  private ApplicationHub(Context context) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    notificationCenter = new NotificationCenter();
    mainThreadHandler = new Handler(Looper.getMainLooper());

    data = new HashMap();
    prefs = new HashMap();

    sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    loadPrefsCache(new Runnable() {
      @Override
      public void run() {
        mApplicationReady = true;
        sendNotification(APPLICATION_READY_NOTIFICATION);
      }
    });
  }

  // this never gets called, but putting code that might belong here anyway for now
  void release() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  public static ApplicationHub instance() {
    if (instance == null)
      Util.log("Must call ApplicationHub.init() first!");

    return instance;
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
  // Cached prefs

  private void preflight(String key) {
    if (!mApplicationReady)
      Util.log("Application not ready you freakin moron");

    if (!cachingPrefKey(key))
      Util.log("Key is not handled by ApplicationHub: " + key);
  }

  public String getPref(String key) {
    preflight(key);

    return (String) prefs.get(key);
  }

  public void setPref(String key, String value) {
    preflight(key);

    prefs.put(key, value);
    savePrefsCache();
  }

  private void loadPrefsCache(final Runnable callbackRunnable) {
    Thread thread1 = new Thread() {
      public void run() {
        // GOOGLE_ACCOUNT
        String account = sharedPreferences.getString(GOOGLE_ACCOUNT_PREF, null);
        if (account != null)
          prefs.put(GOOGLE_ACCOUNT_PREF, account);

        // signal that thread is done so we can initialize the rest of the app
        runOnMainThread(callbackRunnable);
      }
    };
    thread1.start();
  }

  private void savePrefsCache() {
    SharedPreferences.Editor editor = sharedPreferences.edit();

    String value = (String) prefs.get(GOOGLE_ACCOUNT_PREF);
    editor.putString(GOOGLE_ACCOUNT_PREF, value);

    editor.apply();  // this call writes to disk async
  }

  private boolean cachingPrefKey(String key) {
    if (key.equals(GOOGLE_ACCOUNT_PREF))
      return true;

    return false;
  }

  // SharedPreferences.OnSharedPreferenceChangeListener
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (cachingPrefKey(key))
      loadPrefsCache(null);
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