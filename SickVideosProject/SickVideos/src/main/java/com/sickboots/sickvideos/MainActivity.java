package com.sickboots.sickvideos;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.Auth;
import com.sickboots.sickvideos.misc.Util;

public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // start drawer activity
    Intent intent = new Intent();
    intent.setClass(this, DrawerActivity.class);
    this.startActivity(intent);

    finish();
  }
}
