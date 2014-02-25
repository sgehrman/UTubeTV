package com.distantfuture.videos.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.distantfuture.videos.R;
import com.distantfuture.videos.introactivity.IntroActivity;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.Utils;
import com.inscription.ChangeLogDialog;

import org.codechimp.apprater.AppRater;

public class SettingsActivity extends Activity {

  public static void show(Activity activity) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.setClass(activity, SettingsActivity.class);
    activity.startActivity(intent, opts.toBundle());
  }

  @Override
  public void finish() {
    super.finish();

    // animate out
    overridePendingTransition(R.anim.scale_out_rev, R.anim.scale_in_rev);
  }

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

      if (Debug.isDebugBuild())
        addPreferencesFromResource(R.xml.preferences_debug);

      try {
        Preference pref = findPreference("credits");

        if (pref != null) {
          pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
              return handlePrefClick(preference);
            }
          });
        }

        pref = findPreference("rate");
        if (pref != null) {
          pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
              return handlePrefClick(preference);
            }
          });
        }
        pref = findPreference("log");
        if (pref != null) {
          pref.setSummary(Utils.getApplicationVersion(getActivity(), true));

          pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
              return handlePrefClick(preference);
            }
          });
        }

        pref = findPreference("intro");
        if (pref != null) {
          pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
              return handlePrefClick(preference);
            }
          });
        }

        pref = findPreference("feedback");
        if (pref != null) {
          pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
              return handlePrefClick(preference);
            }
          });
        }

      } catch (Throwable throwable) {
        Debug.log("exception: " + throwable.getMessage());
      }
    }

    private boolean handlePrefClick(Preference preference) {
      if (preference.getKey().equals("credits")) {
        InfoActivity.show(getActivity(), "cr");
        return true;
      } else if (preference.getKey().equals("rate")) {
        AppRater.rateNow(getActivity());
        return true;
      } else if (preference.getKey().equals("log")) {
        ChangeLogDialog.showChangeLog(getActivity());
        return true;
      } else if (preference.getKey().equals("intro")) {
        IntroActivity.showIntro(getActivity(), true);
        return true;
      } else if (preference.getKey().equals("feedback")) {
        Utils.sendFeedbackEmail(getActivity());
        return true;
      }

      return false;
    }
  }

}


