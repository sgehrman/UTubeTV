package com.sickboots.sickvideos.misc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.WebView;

/**
 * Created by sgehrman on 2/12/14.
 */
public class IntroDialog {
private static String PREF_KEY = "intro_first_launched_pref";

  public static void showDialog(Activity activity, boolean force) {
     boolean show = false;

    if (force)
      show = true;
    else {
      SharedPreferences mPrefs    = PreferenceManager.getDefaultSharedPreferences(activity);

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
  }
}
