package com.sickboots.sickvideos;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.data.d;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.Locale;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends Activity implements ActionBar.TabListener {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    public PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

//      Util.activateStrictMode(this);

      // Set up the action bar.
      final ActionBar actionBar = getActionBar();
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

      // This shit is buggy, must be created in onCreate of the activity, can't be created in the fragment.
      mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

      // Create the adapter that will return a fragment for each of the three
      // primary sections of the activity.
      mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

      // Set up the ViewPager with the sections adapter.
      mViewPager = (ViewPager) findViewById(R.id.pager);
      mViewPager.setAdapter(mSectionsPagerAdapter);

      // When swiping between different sections, select the corresponding
      // tab. We can also use ActionBar.Tab#select() to do this if we have
      // a reference to the Tab.
      mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
          @Override
          public void onPageSelected(int position) {
              actionBar.setSelectedNavigationItem(position);
          }
      });

      // For each of the sections in the app, add a tab to the action bar.
      for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
          actionBar.addTab(actionBar.newTab()
                          .setText(mSectionsPagerAdapter.getPageTitle(i))
                          .setTabListener(this));
      }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
              Intent intent = new Intent();
              intent.setClass(MainActivity.this, SettingsActivity.class);
              startActivity(intent);

              return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
          Fragment result = null;

          switch (position) {
            case 0:
              result = YouTubeFragment.newInstance(0, null);
            break;
            case 1:
              result = YouTubeFragment.newInstance(1, null);
            break;
            default:
              result = YouTubeFragment.newInstance(2, null);
            break;
          }

          return result;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      // called when playing a movie, could fail and this dialog shows the user how to fix it
      case YouTubeHelper.REQ_PLAYER_CODE:
        if (resultCode != RESULT_OK) {
          YouTubeInitializationResult errorReason = YouTubeStandalonePlayer.getReturnedInitializationResult(data);
          if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, 0).show();
          } else {
            String errorMessage = String.format("PLAYER ERROR!! - %s", errorReason.toString());
            Util.toast(this, errorMessage);
          }
        }

        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }

}

