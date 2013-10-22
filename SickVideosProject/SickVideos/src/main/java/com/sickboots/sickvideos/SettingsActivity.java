package com.sickboots.sickvideos;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    setupSimplePreferencesScreen();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBuildHeaders(List<Header> target) {
    if (!isSimplePreferences(this)) {
      loadHeadersFromResource(R.xml.pref_headers, target);
    }
  }

  private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
      String stringValue = value.toString();

      // Set the summary to the value's
      // simple string representation.
      preference.setSummary(stringValue);

      return true;
    }
  };

  private static void bindPreferenceSummaryToValue(Preference preference) {
    // preference might be null, this is normal, we call it for every fragment
    if (preference == null) {
      return;
    }

    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

    // Trigger the listener immediately with the preference's
    // current value.
    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
        PreferenceManager
            .getDefaultSharedPreferences(preference.getContext())
            .getString(preference.getKey(), ""));
  }

  public static class SharedPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      String settings = getArguments().getString("fragmentID");
      if ("general".equals(settings)) {
        addPreferencesFromResource(R.xml.pref_general);
      } else if ("sharing".equals(settings)) {
        addPreferencesFromResource(R.xml.pref_sharing);
      } else if ("about".equals(settings)) {
        addPreferencesFromResource(R.xml.pref_about);
      }

      // Bind the summaries of EditText/List/Dialog preferences
      // to their values. When their values change, their summaries are
      // updated to reflect the new value, per the Android Design
      // guidelines.
      bindPreferenceSummaryToValue(findPreference("user_name"));
      bindPreferenceSummaryToValue(findPreference("user_handle"));
    }
  }

  private void setupSimplePreferencesScreen() {
    if (!isSimplePreferences(this)) {
      return;
    }

    // getPreferenceScreen() returns null unless we call this first
    addPreferencesFromResource(R.xml.pref_empty);

    PreferenceCategory fakeHeader = new PreferenceCategory(this);
    fakeHeader.setTitle(R.string.pref_header_general);
    getPreferenceScreen().addPreference(fakeHeader);
    addPreferencesFromResource(R.xml.pref_general);

    fakeHeader = new PreferenceCategory(this);
    fakeHeader.setTitle(R.string.pref_header_sharing);
    getPreferenceScreen().addPreference(fakeHeader);
    addPreferencesFromResource(R.xml.pref_sharing);

    fakeHeader = new PreferenceCategory(this);
    fakeHeader.setTitle(R.string.pref_header_about);
    getPreferenceScreen().addPreference(fakeHeader);
    addPreferencesFromResource(R.xml.pref_about);

    // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
    // their values. When their values change, their summaries are updated
    // to reflect the new value, per the Android Design guidelines.
    bindPreferenceSummaryToValue(findPreference("user_name"));
    bindPreferenceSummaryToValue(findPreference("user_handle"));
  }

  @Override
  public boolean onIsMultiPane() {
    return isXLargeTablet(this) && !isSimplePreferences(this);
  }

  private static boolean isXLargeTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  private static boolean isSimplePreferences(Context context) {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
        || !isXLargeTablet(context);
  }
}

