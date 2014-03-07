package com.distantfuture.videos.introactivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.distantfuture.videos.R;
import com.distantfuture.videos.content.Content;
import com.distantfuture.videos.misc.LinePageIndicator;

public class IntroActivity extends Activity implements IntroPageFragment.ActivityAccess, IntroXMLTaskFragment.Callbacks {
  private IntroPagerAdapter introPagerAdapter;
  private IntroXMLTaskFragment mTaskFragment;
  private ViewPager mViewPager;
  private int mSavedIndex = -1;
  private Button mCloseButton;

  public static void showIntroDelayed(final Activity activity, final boolean force) {
    Handler handler = new Handler(Looper.getMainLooper());

    // we don't want to show this until our channels have loaded, so wait more if needed
    // only going to try 10 times, if they don't have internet the whole app is useless anyway
    retry(handler, activity, force, 0);
  }

  private static void retry(final Handler handler, final Activity activity, final boolean force, final int count) {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Content content = Content.instance();

        if (content.channels() != null)
          IntroActivity.showIntro(activity, force);
        else {
          if (count < 10)
            retry(handler, activity, force, count + 1);
        }
      }
    }, 1500);
  }

  public static void showIntro(Activity activity, boolean force) {
    boolean show = false;
    final String PREF_KEY = "intro_first_launched_pref";

    if (force)
      show = true;
    else {
      SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

      final boolean firstLaunch = mPrefs.getBoolean(PREF_KEY, true);

      if (firstLaunch) {
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean(PREF_KEY, false);
        edit.commit();
        show = true;
      }
    }

    if (show) {
      // add animation, see finish below for the back transition
      ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.slidedown, 0);

      Intent intent = new Intent();
      intent.setClass(activity, IntroActivity.class);
      activity.startActivity(intent, opts.toBundle());
    }
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

    if (mSavedIndex != -1) {
      mViewPager.setCurrentItem(mSavedIndex, false);
      mSavedIndex = -1;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_intro);

    FragmentManager fm = getFragmentManager();
    mTaskFragment = (IntroXMLTaskFragment) fm.findFragmentByTag("task");

    // If the Fragment is non-null, then it is currently being
    // retained across a configuration change.
    if (mTaskFragment == null) {
      mTaskFragment = new IntroXMLTaskFragment();
      fm.beginTransaction().add(mTaskFragment, "task").commit();
    }

    mCloseButton = (Button) findViewById(R.id.close_button);
    mCloseButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    mViewPager = (ViewPager) findViewById(R.id.intro_pager);

    introPagerAdapter = new IntroPagerAdapter(this, getFragmentManager());
    introPagerAdapter.setPages(mTaskFragment.getPages());
    mViewPager.setAdapter(introPagerAdapter);

    final LinePageIndicator ind = (LinePageIndicator) findViewById(R.id.line_indicator);
    ind.setViewPager(mViewPager);

    mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        ind.updatePage(position);
        boolean lastPage = position == introPagerAdapter.getCount() - 1;

        mCloseButton.setText((lastPage) ? "Close" : "Read later");
      }
    });

    // show player if activity was destroyed and recreated
    if (savedInstanceState != null) {
      // must wait for the pages to load before we can restore it, so save it here
      mSavedIndex = savedInstanceState.getInt("pager_index");
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt("pager_index", mViewPager.getCurrentItem());
  }

  // ==================================================================

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
