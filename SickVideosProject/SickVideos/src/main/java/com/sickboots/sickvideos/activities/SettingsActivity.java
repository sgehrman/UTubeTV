package com.sickboots.sickvideos.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Debug;

public class SettingsActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
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

          pref.setTitle("Version");
          pref.setSummary(pInfo.versionName + " (" + pInfo.versionCode + ")");
        }


// listen for clicks
        pref = findPreference("credits");

        if (pref != null) {
          pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

              if (preference.getKey().equals("credits")) {
                Intent intent = new Intent();
                intent.putExtra("infoID", "cr");
                intent.setClass(getActivity(), InfoActivity.class);
                startActivity(intent);

                return true;
              }

              return false;
            }
          });
        }



      } catch (Throwable throwable) {
        Debug.log("exception: " + throwable.getMessage());
      }
    }

  }
}


