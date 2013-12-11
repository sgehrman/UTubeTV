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
  private PreferenceCacheListener mListener;

  public PreferenceCache(Context context, PreferenceCacheListener listener) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    mListener = listener;

    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }

  // this never gets called, but putting code that might belong here anyway for now
  void release() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  public String getString(String key, String defaultValue) {
    return sharedPreferences.getString(key, defaultValue);
  }

  public void setString(String key, String value) {
      sharedPreferences.edit().putString(key, value);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    return sharedPreferences.getBoolean(key, defaultValue);
  }

  public void setBoolean(String key, boolean value) {
    sharedPreferences.edit().putBoolean(key, value);
  }

  // SharedPreferences.OnSharedPreferenceChangeListener
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      mListener.prefChanged(key);
  }

}
