package com.sickboots.sickvideos.activities;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.Utils;

public class SettingsActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // must set the theme before we do anything else
    Utils.setActivityTheme(this);
    super.onCreate(savedInstanceState);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    // Display the fragment as the main content.
    getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();
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

  // ----------------------------------------------------------------------
  // ----------------------------------------------------------------------

  public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.preferences);

      try {
        Preference pref = findPreference("version");

        if (pref != null) {
          PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);

          pref.setTitle("Version: " + pInfo.versionName);
          pref.setSummary("Code: " + pInfo.versionCode);
        }

      } catch (Throwable throwable) {
        Debug.log("exception: " + throwable.getMessage());
      }
    }

  }
}


