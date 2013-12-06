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
import com.sickboots.sickvideos.youtube.YouTubeAPIService;

public class MainActivity extends Activity {
  public static final String REQUEST_AUTHORIZATION_INTENT = "com.sickboots.sickvideos.RequestAuth";
  public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.sickboots.sickvideos.RequestAuth.param";
  private UploadBroadcastReceiver broadcastReceiver;
  private static final int INTENT_REQUEST_AUTHORIZATION = 3;
  private static final int INTENT_REQUEST_ACCOUNT_PICKER = 2;
  private GoogleAccountCredential credential;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Util.log("mainActivity onCreate");

    credential = Auth.getCredentials(this);


    Intent i = new Intent(this, YouTubeAPIService.class);
    i.putExtra("KEY1", "Value to be used by the service");
    startService(i);


    // testing, this is wrong
    if (savedInstanceState == null) {
      boolean switchToDrawer = true;
      boolean requiresAuth = true;

      if (requiresAuth) {
        // do we have an account name set?
        String accountName = ApplicationHub.instance(this).getAccountName();

        if (accountName == null) {
          chooseAccount();
          switchToDrawer = false;
        } else
          credential.setSelectedAccountName(accountName);
      }

      if (switchToDrawer)
        switchToDrawerActivity();
    }
  }

  private void switchToDrawerActivity() {
    // start drawer activity
    Intent intent = new Intent();
    intent.setClass(this, DrawerActivity.class);
    this.startActivity(intent);
  }

  private void chooseAccount() {
    startActivityForResult(credential.newChooseAccountIntent(), INTENT_REQUEST_ACCOUNT_PICKER);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case INTENT_REQUEST_AUTHORIZATION:
        if (resultCode != Activity.RESULT_OK) {
          chooseAccount();
        }
        break;

      case INTENT_REQUEST_ACCOUNT_PICKER:
        String accountName = null;
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
        }

        // save to preferences, listen to the pref change to get notified on change
        if (accountName != null) {
          ApplicationHub.instance(this).setAccountName(accountName);

          Auth.getCredentials(this).setSelectedAccountName(accountName);

          // now that an account name has been chosen, we continue
          switchToDrawerActivity();
        }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (broadcastReceiver == null) {
      broadcastReceiver = new UploadBroadcastReceiver();
    }
    IntentFilter intentFilter = new IntentFilter(REQUEST_AUTHORIZATION_INTENT);
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
  }

  private class UploadBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(REQUEST_AUTHORIZATION_INTENT)) {
        Util.log("Request auth received - executing the intent");
        Intent toRun = intent.getParcelableExtra(REQUEST_AUTHORIZATION_INTENT_PARAM);
        startActivityForResult(toRun, INTENT_REQUEST_AUTHORIZATION);
      }
    }
  }

}
