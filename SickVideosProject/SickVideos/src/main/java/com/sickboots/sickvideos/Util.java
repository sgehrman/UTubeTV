package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by sgehrman on 9/24/13.
 */
public class Util {
  // interface for getting results
  public interface StringResultListener {
    public void onResults(StringResultListener listener, String result);
  }

  public static void toast(final Activity activity, final String message) {
    // Toasts only work on the main thread
    if (activity != null && message != null) {
      activity.runOnUiThread(new Runnable() {
        public void run() {
          Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
      });
    }
  }

  public static void log(String message) {
    Log.d("####", message);
  }

  public static boolean isDebugMode(Context context) {
    PackageManager pm = context.getPackageManager();
    try {
      ApplicationInfo info = pm.getApplicationInfo(context.getPackageName(), 0);
      return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    } catch (PackageManager.NameNotFoundException e) {
    }
    return true;
  }

  public static void activateStrictMode(Context context) {
    if (Util.isDebugMode(context)) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectAll() // for all detectable problems
//          .detectDiskReads()
//          .detectDiskWrites()
//          .detectNetwork()
          .penaltyLog()
          .build());
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
          .penaltyDeath()
          .build());
    }
  }

}
