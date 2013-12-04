package com.sickboots.sickvideos.youtube;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.AccountPicker;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;

/**
 * Created by sgehrman on 12/3/13.
 */
public class GoogleAccountPicker {
  String mNameResult;
  final int REQUEST_ACCOUNT_PICKER = 88773209;

  public GoogleAccountPicker() {
    super();
  }

  public void chooseAccount(Activity activity) {
    Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);

    activity.startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
  }

  public boolean handleActivityResult(Context context, int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          mNameResult = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
        }

        // save to preferences, listen to the pref change to get notified on change
        if (mNameResult != null)
          ApplicationHub.preferences(context).setString(PreferenceCache.GOOGLE_ACCOUNT_PREF, mNameResult);

        return true;
    }

    return false;
  }

}
