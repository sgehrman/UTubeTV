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
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

public class AuthActivity extends Activity {
  public static final String REQUEST_AUTHORIZATION_INTENT = "com.sickboots.sickvideos.RequestAuth";
  public static final String REQUEST_AUTHORIZATION_REQUEST_PARAM = "com.sickboots.sickvideos.Request.param";
  public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.sickboots.sickvideos.Intent.param";
  private static final int INTENT_REQUEST_AUTHORIZATION = 3;
  private static final int INTENT_REQUEST_ACCOUNT_PICKER = 2;
  private GoogleAccountCredential credential;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    credential = Auth.getCredentials(this);

    String accountName = credential.getSelectedAccountName();

    if (accountName == null) {
      chooseAccount();
    } else {
      showAuthIntent();
    }
  }

  private void showAuthIntent() {
    Intent authIntent = getIntent().getParcelableExtra(REQUEST_AUTHORIZATION_INTENT_PARAM);

    if (authIntent != null) {
      Util.log("Request auth received - executing the intent");

      startActivityForResult(authIntent, INTENT_REQUEST_AUTHORIZATION);
    }
    else
      doLastStep();
  }

  private void doLastStep() {
    // refetch the request
    YouTubeServiceRequest request = getIntent().getParcelableExtra(REQUEST_AUTHORIZATION_REQUEST_PARAM);

    if (request != null) {
      // forcing a refresh since it is flagged as received in the service to avoid double attempts at a request
      YouTubeAPIService.startRequest(this, request, true);
    }

    finish();
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
        } else {
          doLastStep();
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

          credential.setSelectedAccountName(accountName);

          showAuthIntent();
        }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

}
