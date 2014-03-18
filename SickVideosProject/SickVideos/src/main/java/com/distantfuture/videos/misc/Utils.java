package com.distantfuture.videos.misc;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.distantfuture.videos.R;

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
          FlappyMsg.makeText(activity, message);
        }
      });
    }
  }

  public static void ignoreObsoleteCapacitiveMenuButton(Context context) {
    try {
      ViewConfiguration config = ViewConfiguration.get(context);
      Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
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

  public static Point getDisplaySize(Context context) {
    return new Point(context.getResources().getDisplayMetrics().widthPixels, context.getResources()
        .getDisplayMetrics().heightPixels);
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

  public static String feedbackEmailAddress() {
    return "distantfuturist@gmail.com";
  }

  public static boolean onMainThread() {
    // can also use this
    // Looper.getMainLooper().getThread() == Thread.currentThread();

    return Looper.myLooper() == Looper.getMainLooper();
  }

  public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    // original code  used this for width and height, but our icons don't have Intrinsic size
    // drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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

  public static Bitmap drawTextToBitmap(Context gContext, int width, int height, String gText, int textColor, int shadowColor, int fontSizeInDP, int fillColor, int fillRadius, int strokeColor, float strokeWidth) {
    Resources resources = gContext.getResources();
    float scale = resources.getDisplayMetrics().density;
    int fontSize = (int) (fontSizeInDP * scale);

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

    boolean debugging = false;
    if (debugging) {
      int xx = (bitmap.getWidth()) / 2;
      int yy = (bitmap.getHeight()) / 2;
      paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setColor(0xff880099);

      canvas.drawRect(new Rect(xx - 4, yy - 4, xx + 8, yy + 8), paint);
    }

    return bitmap;
  }

  public static String getApplicationVersion(Context context, boolean includeBuild) {
    String result = "";

    try {
      PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

      result = "v" + pInfo.versionName;

      if (includeBuild)
        result += " (" + pInfo.versionCode + ")";

    } catch (Throwable t) {
    }

    return result;
  }

  public static String getApplicationName(Context context) {
    String appName = "Application";
    try {
      String packageName = context.getPackageName();
      PackageManager pm = context.getPackageManager();

      ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
      if (ai != null)
        appName = (String) pm.getApplicationLabel(ai);

    } catch (Exception e) {
    }

    return appName;
  }

  public static Uri getCompanyPlayStoreUri() {
    return Uri.parse("market://search?q=pub:Distant Future");
  }

  // using apprater for this, but if we ever get rid of that, this is the code
  public static Uri getApplicationPlayStoreUri(Context context) {
    return Uri.parse("market://details?id=" + getApplicationPackageName(context));
  }

  public static String getApplicationPackageName(Context context) {
    return context.getApplicationInfo().packageName;
  }

  public static void sendFeedbackEmail(Activity activity) {
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("message/rfc822");
    i.putExtra(Intent.EXTRA_EMAIL, new String[]{feedbackEmailAddress()});
    i.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + getApplicationName(activity));
    i.putExtra(Intent.EXTRA_TEXT, "");
    try {
      activity.startActivity(Intent.createChooser(i, "Send mail..."));
    } catch (android.content.ActivityNotFoundException ex) {
      Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
    }
  }

  public static void textViewColorChanger(ViewGroup group, int textColor, int hintColor) {
    for (int i = 0; i < group.getChildCount(); i++) {
      View child = group.getChildAt(i);
      if (child instanceof TextView) {
        ((TextView) child).setTextColor(textColor);
        ((TextView) child).setHintTextColor(hintColor);
      } else if (child instanceof ViewGroup)
        textViewColorChanger((ViewGroup) child, textColor, hintColor);
    }
  }

  public static String condenseWhiteSpace(String inString) {
    // trim text and then replace all occurrences of one or more than one whitespace character
    // (including tabs, line breaks, etc) by one single whitespace
    return inString.trim().replaceAll("\\s+", " ");
  }

  public static boolean isPortrait(Context context) {
    return (context.getResources()
        .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
  }

  public static void openWebPage(Activity activity, Uri webpage) {
    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
    if (intent.resolveActivity(activity.getPackageManager()) != null) {
      activity.startActivity(intent);
    }
  }

  // interface for getting results
  public interface StringResultListener {
    public void onResults(StringResultListener listener, String result);
  }

  public static boolean isKitKat() {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
  }

  public static boolean isJellyBean() {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
  }
}