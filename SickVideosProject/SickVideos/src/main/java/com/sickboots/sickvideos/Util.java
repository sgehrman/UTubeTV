package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Random;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created by sgehrman on 9/24/13.
 */
public class Util {

  public interface PullToRefreshListener {
    void addRefreshableView(View theView, PullToRefreshAttacher.OnRefreshListener listener);

    public void setRefreshComplete();
  }

  // interface for getting results
  public interface StringResultListener {
    public void onResults(StringResultListener listener, String result);
  }

  public static void toast(final Activity activity, final String message) {
    // Toasts only work on the main thread
    if (activity != null && message != null) {
      activity.runOnUiThread(new Runnable() {
        public void run() {
          Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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

  public static void ignoreObsoleteCapacitiveMenuButton(Context context) {
    try {
      ViewConfiguration config = ViewConfiguration.get(context);
      Field menuKeyField = ViewConfiguration.class
          .getDeclaredField("sHasPermanentMenuKey");
      if (menuKeyField != null) {
        menuKeyField.setAccessible(true);
        menuKeyField.setBoolean(config, false);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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

  public static void showFragment(Activity activity, Fragment fragment, int resID, int animationType, boolean addToBackStack) {
    FragmentManager fragmentManager = activity.getFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction();

    switch (animationType) {
      case 0:
        break;
      case 1:
        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left, R.animator.slide_in_right, R.animator.slide_out_right);
        break;
      case 2:
        ft.setCustomAnimations(R.animator.slide_in_down, R.animator.slide_out_down, R.animator.slide_in_up, R.animator.slide_out_up);
        break;
    }

    ft.replace(resID, fragment);

    if (addToBackStack)
      ft.addToBackStack(null);

    ft.commit();
  }

  public static View emptyListView(Context context, String message) {
    ViewGroup result = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.empty_list_view, null);

    TextView textView = (TextView) result.findViewById(R.id.message);
    textView.setText(message);

    return result;
  }

  public static float pxToDp(float px, Context context) {
    return px / context.getResources().getDisplayMetrics().density;
  }

  public static float dpToPx(float dp, Context context) {
    return dp * context.getResources().getDisplayMetrics().density;
  }

  public static int randomColor() {
    Random random = new Random();
    return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
  }

  public static void vibrate(Context context) {
    Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

    vibe.vibrate(5);
  }

  public static String millisecondsToDuration(long milliseconds) {
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    df.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

    String result = df.format(milliseconds);

    // strip off 00: from begining if present
    final String zeros = "00:";
    if (result.startsWith(zeros))
      result = result.substring(zeros.length());

    return result;
  }

}
