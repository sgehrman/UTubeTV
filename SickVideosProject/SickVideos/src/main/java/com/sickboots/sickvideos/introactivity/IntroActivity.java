package com.sickboots.sickvideos.introactivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.content.Content;
import com.sickboots.sickvideos.misc.Events;
import com.sickboots.sickvideos.misc.LinePageIndicator;
import com.sickboots.sickvideos.misc.Utils;

import de.greenrobot.event.EventBus;

public class IntroActivity extends Activity implements IntroPageFragment.ActivityAccess, IntroXMLTaskFragment.Callbacks {
  private static String PREF_KEY = "intro_first_launched_pref";
  private IntroPagerAdapter introPagerAdapter;
  private IntroXMLTaskFragment mTaskFragment;

  public static void showIntroDelayed(final Activity activity, final boolean force) {
    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
      @Override
      public void run() {
        IntroActivity.showIntro(activity, false, force);
      }
    }, 2000);
  }

  public static void showIntro(Activity activity, boolean dialogStyle, boolean force) {
    boolean show = false;

    if (force)
      show = true;
    else {
      SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

      final boolean firstLaunch = mPrefs.getBoolean(PREF_KEY, true);

      // don't show on users first launch, they don't care about what's new, they just got the app
      if (firstLaunch) {
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean(PREF_KEY, false);
        edit.commit();
        show = true;
      }
    }

    if (show) {

      if (dialogStyle)
        showIntroDialog(activity);
      else {
        // add animation, see finish below for the back transition
        ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.slidedown, 0);

        Intent intent = new Intent();
        intent.setClass(activity, IntroActivity.class);
        activity.startActivity(intent, opts.toBundle());
      }
    }
  }

  private static void showIntroDialog(Activity activity) {
    String title = Utils.getApplicationName(activity) + " - " + Utils.getApplicationVersion(activity, false);

    final WebView webview = new WebView(activity);

    webview.loadUrl("file:///android_asset/intro.html");

    final AlertDialog.Builder builder = new AlertDialog.Builder(activity).setTitle(title)
        .setView(webview)
        .setPositiveButton("Close", new Dialog.OnClickListener() {
          public void onClick(final DialogInterface dialogInterface, final int i) {
            dialogInterface.dismiss();
          }
        })
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
          }
        });
    builder.create().show();
  }

  @Override
  public void finish() {
    super.finish();

    // animate out
    overridePendingTransition(0, R.anim.slidedown_rev);
  }

  public IntroXMLParser.IntroPage pageAtIndex(int position) {
    return introPagerAdapter.pageAtIndex(position);
  }

  @Override
  public void onNewPages() {
    introPagerAdapter.setPages(mTaskFragment.getPages());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.fragment_intro);

    FragmentManager fm = getFragmentManager();
    mTaskFragment = (IntroXMLTaskFragment) fm.findFragmentByTag("task");

    // If the Fragment is non-null, then it is currently being
    // retained across a configuration change.
    if (mTaskFragment == null) {
      mTaskFragment = new IntroXMLTaskFragment();
      fm.beginTransaction().add(mTaskFragment, "task").commit();
    }

    Button button = (Button) findViewById(R.id.sign_up_button);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    ViewPager viewPager = (ViewPager) findViewById(R.id.intro_pager);

    introPagerAdapter = new IntroPagerAdapter(this, getFragmentManager());
    introPagerAdapter.setPages(mTaskFragment.getPages());
    viewPager.setAdapter(introPagerAdapter);

    LinePageIndicator ind = (LinePageIndicator) findViewById(R.id.line_indicator);
    ind.setViewPager(viewPager);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
