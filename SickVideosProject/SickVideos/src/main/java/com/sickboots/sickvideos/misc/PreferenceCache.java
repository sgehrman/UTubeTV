package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by sgehrman on 11/26/13.
 */
public class PreferenceCache implements SharedPreferences.OnSharedPreferenceChangeListener {

  public interface PreferenceCacheListener {
    public void prefsLoaded();

    public void prefChanged(String prefName);
  }

  // public pref keys
  public static final String GOOGLE_ACCOUNT_PREF = "google_account";
  public static final String ACTION_BAR_COLOR = "action_bar_color";
  public static final String DRAWER_SECTION_INDEX = "drawer_section_index";
  public static final String SHOW_HIDDEN_VIDEOS = "show_hidden_videos";
  public static final String PLAY_FULLSCREEN = "play_fullscreen";
  public static final String MUTE_ADS = "mute_ads";
  public static final String THEME_STYLE = "theme_style";

  private SharedPreferences sharedPreferences;
  private HashMap prefs;
  private PreferenceCacheListener mListener;

  private List<String> stringPreferenceKeys = asList(GOOGLE_ACCOUNT_PREF, ACTION_BAR_COLOR, DRAWER_SECTION_INDEX, THEME_STYLE);
  private List<String> boolPreferenceKeys = asList(SHOW_HIDDEN_VIDEOS, PLAY_FULLSCREEN, MUTE_ADS);

  public PreferenceCache(Context context, PreferenceCacheListener listener) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    prefs = new HashMap();
    mListener = listener;

    sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    loadPrefsCache(true);
  }

  // this never gets called, but putting code that might belong here anyway for now
  void release() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  public String getString(String key, String defaultValue) {
    preflight(key);

    String result = (String) prefs.get(key);

    if (result == null)
      result = defaultValue;

    return result;
  }

  public void setString(String key, String value) {
    preflight(key);

    prefs.put(key, value);
    savePrefsCache();
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    boolean result = defaultValue;

    preflight(key);

    Boolean obj = (Boolean) prefs.get(key);

    if (obj != null)
      result = obj.booleanValue();

    return result;
  }

  public void setBoolean(String key, boolean value) {
    preflight(key);

    prefs.put(key, value);
    savePrefsCache();
  }

  private void loadPrefsCache(final boolean firstLoad) {
    Thread thread1 = new Thread() {
      public void run() {
        for (String key : stringPreferenceKeys)
          prefs.put(key, sharedPreferences.getString(key, null));

        for (String key : boolPreferenceKeys)
          prefs.put(key, sharedPreferences.getBoolean(key, false));

        if (firstLoad)
          mListener.prefsLoaded();
      }
    };
    thread1.start();
  }

  private void preflight(String key) {
    if (!cachingPrefKey(key))
      Util.log("Key is not handled by ApplicationHub: " + key);
  }

  private void savePrefsCache() {
    SharedPreferences.Editor editor = sharedPreferences.edit();

    for (String key : stringPreferenceKeys)
      editor.putString(key, (String) prefs.get(key));

    for (String key : boolPreferenceKeys)
      editor.putBoolean(key, (Boolean) prefs.get(key));

    editor.apply();  // this call writes to disk async
  }

  private boolean cachingPrefKey(String key) {
    return stringPreferenceKeys.contains(key) | boolPreferenceKeys.contains(key);
  }

  // SharedPreferences.OnSharedPreferenceChangeListener
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (cachingPrefKey(key)) {
      loadPrefsCache(false);

      mListener.prefChanged(key);
    }
  }

}
