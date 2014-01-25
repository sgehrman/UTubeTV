package com.sickboots.sickvideos.misc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;
import com.sickboots.sickvideos.R;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Random;

public class Utils {
  private static final PeriodFormatter mFormatter = ISOPeriodFormat.standard();
  private static float sScreenDensity = 0;

  // interface for getting results
  public interface StringResultListener {
    public void onResults(StringResultListener listener, String result);
  }

  public static void toast(Context context, final String message) {
    // Toasts only work on the main thread
    if (context != null && message != null) {
      final Context appContext = context.getApplicationContext();

      Handler handler = new Handler(Looper.getMainLooper());

      handler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  public static void message(final Activity activity, final String message) {
    // Toasts only work on the main thread
    if (activity != null && message != null) {
      Handler handler = new Handler(Looper.getMainLooper());

      handler.post(new Runnable() {
        @Override
        public void run() {
          AppMsg.Style style = new AppMsg.Style(AppMsg.LENGTH_SHORT, R.drawable.app_msg_background);

          AppMsg.makeText(activity, message, style)
              .setLayoutGravity(Gravity.BOTTOM)
              .show();
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
//      case 1:
//        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left, R.animator.slide_in_right, R.animator.slide_out_right);
//        break;
//      case 2:
//        ft.setCustomAnimations(R.animator.slide_in_down, R.animator.slide_out_down, R.animator.slide_in_up, R.animator.slide_out_up);
//        break;
//      case 3:
//        ft.setCustomAnimations(R.animator.fraggy_enter, R.animator.fraggy_exit, R.animator.fraggy_pop_enter, R.animator.fraggy_pop_exit);
//        break;
      default:
        ft.setCustomAnimations(R.animator.fade_enter, R.animator.fade_exit, R.animator.fade_enter, R.animator.fade_exit);
        break;
    }

    ft.replace(resID, fragment);

    if (addToBackStack)
      ft.addToBackStack(null);

    ft.commit();
  }

  public static float screenDensity(Context context) {
    // assuming it's faster to cache this
    if (sScreenDensity == 0)
      sScreenDensity = context.getResources().getDisplayMetrics().density;

    return sScreenDensity;
  }

  public static float pxToDp(float px, Context context) {
    return px / screenDensity(context);
  }

  public static float dpToPx(float dp, Context context) {
    return dp * screenDensity(context);
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

  public static void setActionBarTitle(Activity activity, CharSequence title, CharSequence subtitle) {
    if (activity != null) {
      ActionBar bar = activity.getActionBar();

      if (bar != null) {
        // title is hidden in theme so we don't get ugly flicker on the app name
//        if ((bar.getDisplayOptions() & ActionBar.DISPLAY_SHOW_TITLE) != ActionBar.DISPLAY_SHOW_TITLE)
//          bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | bar.getDisplayOptions());

        bar.setTitle(title);
        bar.setSubtitle(subtitle);
      }
    }
  }

  public static boolean hasNetworkConnection(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    // could add this later (from dev sample)
    //    if (isConnected) {
    //      boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    //
    //      Debug.log("got wifi");
    //    }

    return isConnected;
  }

  public static Bitmap drawTextToBitmap(Context gContext, int width, int height, String gText, int textColor, int shadowColor, int fontSizeInDP, int fillColor, int fillRadius, int strokeColor, float strokeWidth) {
    Resources resources = gContext.getResources();
    float scale = resources.getDisplayMetrics().density;
    int fontSize = (int) (fontSizeInDP * scale);
    boolean debugging = false;

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ---------------
    // draw fill

    if (fillColor != 0) {
      paint.setStyle(Paint.Style.FILL);

      paint.setColor(fillColor);
      canvas.drawRoundRect(new RectF(0, 0, width, height), fillRadius, fillRadius, paint);
    }

    if (strokeColor != 0) {
      paint.setStyle(Paint.Style.STROKE);
      paint.setColor(strokeColor);
      paint.setStrokeWidth(strokeWidth);
      canvas.drawRoundRect(new RectF(0, 0, width, height), fillRadius, fillRadius, paint);
    }

    // ---------------
    // draw text
    paint.setStyle(Paint.Style.FILL_AND_STROKE);

    paint.setColor(textColor);
    paint.setTextSize(fontSize);
    paint.setShadowLayer(1f, 0f, 1f, shadowColor);

    // draw text to the Canvas center
    Rect bounds = new Rect();
    paint.setTextAlign(Paint.Align.CENTER);

    paint.getTextBounds(gText, 0, gText.length(), bounds);
    int x = (bitmap.getWidth()) / 2;
    int y = (bitmap.getHeight() + bounds.height()) / 2;

    canvas.drawText(gText, x, y, paint);

    if (debugging) {
      int xx = (bitmap.getWidth()) / 2;
      int yy = (bitmap.getHeight()) / 2;
      paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setColor(0xff880099);

      canvas.drawRect(new Rect(xx - 4, yy - 4, xx + 8, yy + 8), paint);
    }

    return bitmap;
  }
}