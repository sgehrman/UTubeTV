package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;
import com.sickboots.sickvideos.youtube.GoogleAccount;
import com.sickboots.sickvideos.youtube.GoogleAccountPicker;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer {
  GoogleAccountPicker mAccountPicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ApplicationHub.instance(this).addObserver(this);
  }

  public void update(Observable observable, Object data) {
    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(ApplicationHub.APPLICATION_READY_NOTIFICATION)) {
        // only need this on launch
        ApplicationHub.instance(this).deleteObserver(MainActivity.this);

        boolean switchToDrawer = true;
        boolean requiresAuth = true;

        if (requiresAuth) {
          // do we have an account name set?
          String accountName = ApplicationHub.preferences(this).getString(PreferenceCache.GOOGLE_ACCOUNT_PREF, null);

          if (accountName == null) {
            // this will set the account name
            mAccountPicker = new GoogleAccountPicker();
            mAccountPicker.chooseAccount(this);

            switchToDrawer = false;
          }
        }

        if (switchToDrawer)
          switchToDrawerActivity();

      }
    }
  }

  private void switchToDrawerActivity() {
    DrawerActivity.start(this);

    // we are done, finish us
    finish();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (mAccountPicker != null) {
      boolean handled = mAccountPicker.handleActivityResult(this, requestCode, resultCode, data);

      if (handled) {
        // now that an account name has been chosen, we continue
        switchToDrawerActivity();

        mAccountPicker = null; // only used once
        return;
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

}
