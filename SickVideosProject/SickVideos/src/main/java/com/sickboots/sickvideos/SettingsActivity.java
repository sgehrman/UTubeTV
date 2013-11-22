package com.sickboots.sickvideos;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
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
    loadHeadersFromResource(R.xml.pref_headers, target);
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
      } else if ("about".equals(settings)) {
        addPreferencesFromResource(R.xml.pref_about);
      }
    }
  }

  @Override
  protected boolean isValidFragment(String fragmentName) {
    if (fragmentName.equals("com.sickboots.sickvideos.SettingsActivity$SharedPreferenceFragment"))
      return true;

    return false;
  }
}

