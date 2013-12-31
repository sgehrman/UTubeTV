package com.sickboots.sickvideos.misc;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.sickboots.sickvideos.R;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Random;

public class Utils {
  static final PeriodFormatter mFormatter = ISOPeriodFormat.standard();

  // interface for getting results
  public interface StringResultListener {
    public void onResults(StringResultListener listener, String result);
  }

  public static void toast(final Context context, final String message) {
    // Toasts only work on the main thread
    if (context != null && message != null) {
      Handler handler = new Handler(Looper.getMainLooper());

      handler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
      });
    }
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

  public static void showFragment(Activity activity, Fragment fragment, int resID, int animationType, boolean addToBackStack) {
    // check params and bail if necessary
    if (fragment == null || activity == null) {
      Debug.log("bad params: " + Debug.currentMethod());
      return;
    }

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
      case 3:
        ft.setCustomAnimations(R.animator.fraggy_enter, R.animator.fraggy_exit, R.animator.fraggy_pop_enter, R.animator.fraggy_pop_exit);
        break;
    }

    ft.replace(resID, fragment);

    if (addToBackStack)
      ft.addToBackStack(null);

    ft.commit();
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

  public static boolean onMainThread() {
    // can also use this
    // Looper.getMainLooper().getThread() == Thread.currentThread();

    return Looper.myLooper() == Looper.getMainLooper();
  }

  public static Bitmap drawableToBitmap(Drawable drawable, int size) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    // original code  used this for width and height, but our icons don't have Intrinsic size
    // drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()

    Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }

  public static String durationToDuration(String isoDuration) {
    Period p = mFormatter.parsePeriod(isoDuration);
    Seconds s = p.toStandardSeconds();

    return millisecondsToDuration(s.getSeconds() * 1000);
  }

}
