package com.sickboots.sickvideos.misc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.webkit.WebView;

import com.inscription.R;

/**
 * Created by sgehrman on 2/12/14.
 */
public class IntroDialog {

  public  static void showDialog(Activity activity) {
String    title = Utils.getApplicationName(activity) + " " + Utils.getApplicationVersion(activity, false);

    final WebView webview = new WebView(activity);

    webview.loadUrl("file:///android_asset/tos.html");

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
