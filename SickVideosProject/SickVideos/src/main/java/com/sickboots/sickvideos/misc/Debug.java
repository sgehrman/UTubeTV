package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import com.sickboots.sickvideos.BuildConfig;

import java.util.Date;

public class Debug {

  public static void log(String message) {
    if (isDebugBuild())
      Log.d("####", (message == null ? "null" : message));
  }

  public static boolean isDebuggable(Context context) {
    PackageManager pm = context.getPackageManager();
    try {
      ApplicationInfo info = pm.getApplicationInfo(context.getPackageName(), 0);
      return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    } catch (PackageManager.NameNotFoundException e) {
    }

    return true;
  }

  public static void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch(InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  public static boolean isDebugBuild() {
    return BuildConfig.DEBUG;
  }

  public static void activateStrictMode() {
    if (isDebugBuild()) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          //          .detectAll() // for all detectable problems
          .detectDiskReads()
              //          .detectDiskWrites()
          .detectNetwork().penaltyLog()
              //          .penaltyDialog()
          .build());
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
          .detectActivityLeaks().detectLeakedRegistrationObjects()
              //          .penaltyDeath()
          .build());
    }
  }

  public static String currentMethod() {
    return Thread.currentThread().getStackTrace()[3].getMethodName() + "()";
  }

  public static String systemUIVisibilityString(View theView) {
    int crap = theView.getWindowSystemUiVisibility();

    int duhs[] = {View.SYSTEM_UI_FLAG_LOW_PROFILE, View.SYSTEM_UI_FLAG_HIDE_NAVIGATION, View.SYSTEM_UI_FLAG_FULLSCREEN, View.SYSTEM_UI_FLAG_LAYOUT_STABLE, View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION, View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, View.SYSTEM_UI_FLAG_IMMERSIVE, View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY};
    String result = "OK: 0";

    for (int duh : duhs) {
      if ((crap & duh) == duh) {
        switch (duh) {
          case View.SYSTEM_UI_FLAG_LOW_PROFILE:
            result += " | SYSTEM_UI_FLAG_LOW_PROFILE";
            break;
          case View.SYSTEM_UI_FLAG_HIDE_NAVIGATION:
            result += " | SYSTEM_UI_FLAG_HIDE_NAVIGATION";
            break;
          case View.SYSTEM_UI_FLAG_FULLSCREEN:
            result += " | SYSTEM_UI_FLAG_FULLSCREEN";
            break;
          case View.SYSTEM_UI_FLAG_LAYOUT_STABLE:
            result += " | SYSTEM_UI_FLAG_LAYOUT_STABLE";
            break;
          case View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION:
            result += " | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION";
            break;
          case View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN:
            result += " | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN";
            break;
          case View.SYSTEM_UI_FLAG_IMMERSIVE:
            result += " | SYSTEM_UI_FLAG_IMMERSIVE";
            break;
          case View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY:
            result += " | SYSTEM_UI_FLAG_IMMERSIVE_STICKY";
            break;
        }
      }
    }

    return result;
  }

  public static void timeLog(String message, Date startTime) {
    Date now = new Date();
    Debug.log("message: " + (now.getTime() - startTime.getTime()) / 1000);
  }

}
