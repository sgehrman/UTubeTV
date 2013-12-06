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

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity {
  public static final String REQUEST_AUTHORIZATION_INTENT = "com.google.example.yt.RequestAuth";
  public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.google.example.yt.RequestAuth.param";
  private UploadBroadcastReceiver broadcastReceiver;
  private static final int INTENT_REQUEST_AUTHORIZATION = 3;
  private static final int INTENT_REQUEST_ACCOUNT_PICKER = 2;
  private GoogleAccountCredential credential;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Util.log("mainActivity onCreate");

    credential = Auth.getCredentials(this);

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

  private void switchToDrawerActivity() {
    DrawerActivity.start(this);
  }

  private void  chooseAccount() {
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



/*

package sk.toth.lies;

    import sk.toth.lies.util.Auth;
    import sk.toth.lies.util.Preferences;
    import sk.toth.lies.util.youtube.UploadService;
    import android.accounts.AccountManager;
    import android.app.Activity;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.support.v4.content.LocalBroadcastManager;
    import android.util.Log;


    import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class MainActivity extends Activity {

  public static final String REQUEST_AUTHORIZATION_INTENT = "com.google.example.yt.RequestAuth";
  public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.google.example.yt.RequestAuth.param";
  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int INTENT_CAPTURE_VIDEO = 1;
  private static final int INTENT_REQUEST_ACCOUNT_PICKER = 2;
  private static final int INTENT_REQUEST_AUTHORIZATION = 3;
  public static final String YOUTUBE_ID = "youtubeId";
  public static final String ACCOUNT_KEY = "accountName";

  private GoogleAccountCredential credential;
  private UploadBroadcastReceiver broadcastReceiver;
  private Preferences preferences;
  private String accountName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    preferences = Preferences.getPreferences(this);
    accountName = preferences.getString(AccountManager.KEY_ACCOUNT_NAME);
    credential = Auth.getCredentials(this);
    if (accountName == null) {
      startActivityForResult(credential.newChooseAccountIntent(), INTENT_REQUEST_ACCOUNT_PICKER);
    } else {
      credential.setSelectedAccountName(accountName);
      startCameraIntent();
    }
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
        Log.d("Tag", "Request auth received - executing the intent");
        Intent toRun = intent.getParcelableExtra(REQUEST_AUTHORIZATION_INTENT_PARAM);
        startActivityForResult(toRun, INTENT_REQUEST_AUTHORIZATION);
      }
    }
  }

  private void startCameraIntent() {
    Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    startActivityForResult(cameraIntent, INTENT_CAPTURE_VIDEO);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    switch (requestCode) {
      case INTENT_CAPTURE_VIDEO:
        if (resultCode == Activity.RESULT_OK) {
          Intent uploadIntent = new Intent(this, UploadService.class);
          uploadIntent.setData(data.getData());
          Auth.setCredentials(credential);
          startService(uploadIntent);
        }
      case INTENT_REQUEST_AUTHORIZATION:
        if (resultCode != Activity.RESULT_OK) {
          chooseAccount();
        }
        break;
      case INTENT_REQUEST_ACCOUNT_PICKER:
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
          if (accountName != null) {
            preferences.put(AccountManager.KEY_ACCOUNT_NAME, accountName);
            credential.setSelectedAccountName(accountName);
            startCameraIntent();
          }
        }
        break;
    }
  }

  private void chooseAccount() {
    startActivityForResult(credential.newChooseAccountIntent(), INTENT_REQUEST_ACCOUNT_PICKER);
  }
}




*/
