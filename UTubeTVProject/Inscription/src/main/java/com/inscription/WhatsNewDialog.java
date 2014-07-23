/*
 * (c) 2012 Martin van Zuilekom (http://martin.cubeactive.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.inscription;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public class WhatsNewDialog extends ChangeLogDialog {
  private static final String WHATS_NEW_FIRST_LAUNCH = "whats_new_first_launch";
  private static final String WHATS_NEW_LAST_SHOWN = "whats_new_last_shown";
  private SharedPreferences mPrefs;
  private int mAppVersionCode = -1;

  private WhatsNewDialog(final Activity activity) {
    super(activity);

    mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
  }

  public static void showWhatsNew(Activity activity, boolean force) {
    new WhatsNewDialog(activity).show(force);
  }

  private int getAppVersionCode() {
    if (mAppVersionCode == -1) {
      try {
        final PackageInfo packageInfo = mActivity.getPackageManager()
            .getPackageInfo(mActivity.getPackageName(), 0);

        mAppVersionCode = packageInfo.versionCode;
      } catch (NameNotFoundException ignored) {
        mAppVersionCode = 0;
      }
    }

    return mAppVersionCode;
  }

  private void show(boolean force) {
    boolean show = false;

    if (force)
      show = true;
    else {
      final boolean firstLaunch = mPrefs.getBoolean(WHATS_NEW_FIRST_LAUNCH, true);

      // don't show on users first launch, they don't care about what's new, they just got the app
      if (firstLaunch) {
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putInt(WHATS_NEW_LAST_SHOWN, getAppVersionCode());  // they will see the next update
        edit.putBoolean(WHATS_NEW_FIRST_LAUNCH, false);
        edit.commit();
      } else {
        final int versionShown = mPrefs.getInt(WHATS_NEW_LAST_SHOWN, 0);
        if (versionShown != getAppVersionCode()) {
          show = true;

          final SharedPreferences.Editor edit = mPrefs.edit();
          edit.putInt(WHATS_NEW_LAST_SHOWN, getAppVersionCode());
          edit.commit();
        }
      }
    }

    if (show)
      showDialog(getAppVersionCode());
  }
}
