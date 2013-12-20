package com.sickboots.sickvideos.activities;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import java.util.List;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.Utils;

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

    try {
      Activity activity = this;
      PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
      String version = pInfo.versionName;
    } catch (Throwable t) {

    }
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
    if (fragmentName.equals("com.sickboots.sickvideos.activities.SettingsActivity$SharedPreferenceFragment"))
      return true;

    return false;
  }
}

