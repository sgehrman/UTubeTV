package com.sickboots.sickvideos.introactivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.webkit.WebView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Utils;

public class IntroActivity extends Activity {
  private static String PREF_KEY = "intro_first_launched_pref";

  public static void showIntro(Activity activity, boolean dialogStyle, boolean force) {

    boolean show = false;

    if (force)
      show = true;
    else {
      SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

      final boolean firstLaunch = mPrefs.getBoolean(PREF_KEY, true);

      // don't show on users first launch, they don't care about what's new, they just got the app
      if (firstLaunch) {
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean(PREF_KEY, false);
        edit.commit();
        show = true;
      }
    }

    if (show) {

      if (dialogStyle)
        showIntroDialog(activity, force);
      else {
        // add animation, see finish below for the back transition
        ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.slidedown, 0);

        Intent intent = new Intent();
        intent.setClass(activity, IntroActivity.class);
        activity.startActivity(intent, opts.toBundle());
      }
    }
  }

  @Override
  public void finish() {
    super.finish();

    // animate out
    overridePendingTransition(0, R.anim.slidedown_rev);
  }

  private static void showIntroDialog(Activity activity, boolean force) {
    String title = Utils.getApplicationName(activity) + " - " + Utils.getApplicationVersion(activity, false);

    final WebView webview = new WebView(activity);

    webview.loadUrl("file:///android_asset/intro.html");

    final AlertDialog.Builder builder = new AlertDialog.Builder(activity).setTitle(title)
        .setView(webview)
        .setPositiveButton("Close", new Dialog.OnClickListener() {
          public void onClick(final DialogInterface dialogInterface, final int i) {
            dialogInterface.dismiss();
          }
        })
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
          }
        });
    builder.create().show();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // using a fragment at the contents
    if (savedInstanceState == null) {
      android.app.Fragment fragment = new IntroFragment();
      FragmentManager fragmentManager = getFragmentManager();

      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.replace(android.R.id.content, fragment);
      fragmentTransaction.commit();
    }
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
}
