package com.distantfuture.videos.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {

  public interface PreferenceCacheListener {
    public void prefChanged(String prefName);
  }

  private SharedPreferences sharedPreferences;
  private PreferenceCacheListener mListener;

  public Preferences(Context context, PreferenceCacheListener listener) {
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
    sharedPreferences.edit().putString(key, value).commit();
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    return sharedPreferences.getBoolean(key, defaultValue);
  }

  public void setBoolean(String key, boolean value) {
    sharedPreferences.edit().putBoolean(key, value).commit();
  }

  public int getInt(String key, int defaultValue) {
    return sharedPreferences.getInt(key, defaultValue);
  }

  public void setInt(String key, int value) {
    sharedPreferences.edit().putInt(key, value).commit();
  }

  // SharedPreferences.OnSharedPreferenceChangeListener
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    mListener.prefChanged(key);
  }

}
