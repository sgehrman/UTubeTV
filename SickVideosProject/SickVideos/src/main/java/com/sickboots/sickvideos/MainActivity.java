package com.sickboots.sickvideos;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
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

            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
          switch (position) {
            case 0:
              result = YouTubeFragment.newInstance(0);
            break;
            case 1:
              result = YouTubeFragment.newInstance(1);
            break;
            default:
              result = YouTubeFragment.newInstance(2);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}





/*


package com.undefeatedgames.dareshare;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.undefeatedgames.sfapulltorefresh.PullToRefreshAttacher;

import java.io.File;

public class MainActivity extends Activity implements ActionBar.TabListener {
  MainPagerAdapter mMainPagerAdapter;
  public PullToRefreshAttacher mPullToRefreshAttacher;
  ViewPager mViewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

//    activateStrictMode();

    // This shit is buggy, must be created in onCreate of the activity, can't be created in the fragment.
    mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

    // Set up the action bar.
    final ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    // Create the adapter that will return a fragment for each of the
    // primary sections of the app.
    mMainPagerAdapter = new MainPagerAdapter(getFragmentManager(), this);

    // Set up the ViewPager with the sections adapter.
    mViewPager = (ViewPager) findViewById(R.id.pager);
    mViewPager.setAdapter(mMainPagerAdapter);

    // When swiping between different sections, select the corresponding
    // tab. We can also use ActionBar.Tab#select() to do this if we have
    // a reference to the Tab.
    mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        actionBar.setSelectedNavigationItem(position);

        // want to set the title per tab, but makes tab bar jump as the width changes
        // actionBar.setTitle(mMainPagerAdapter.getPageTitle(position));
      }
    });

    // For each of the sections in the app, add a tab to the action bar.
    for (int i = 0; i < mMainPagerAdapter.getCount(); i++) {
      actionBar.addTab(
          actionBar.newTab()
              .setContentDescription(mMainPagerAdapter.getPageTitle(i))
              .setIcon(mMainPagerAdapter.getIconResID(i))
              .setTabListener(this));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;

    // Handle item selection
    switch (item.getItemId()) {
      case R.id.action_settings:
        intent = new Intent();
        intent.setClass(MainActivity.this, SettingsActivity.class);
        startActivity(intent);

        return true;
      case R.id.action_find_friends:
        intent = new Intent();
        intent.setClass(MainActivity.this, FindFriendsActivity.class);
        startActivity(intent);

        return true;
      case R.id.action_refresh:
        Boolean useCustomCamera = true;

        intent = new Intent();
        if (useCustomCamera) {
          intent.setClass(MainActivity.this, CameraActivity.class);
        } else {
          intent.setClass(MainActivity.this, CameraIntentActivity.class);
        }
        startActivityForResult(intent, 1337);

        // changes animation of activity
        MainActivity.this.overridePendingTransition(R.anim.activity_in_down, R.anim.activity_out_down);

        return true;

      case R.id.action_camera:
        intent = new Intent();
        intent.setClass(MainActivity.this, CameraIntentActivity.class);
        startActivityForResult(intent, 1337);

        // changes animation of activity
        MainActivity.this.overridePendingTransition(R.anim.activity_in_down, R.anim.activity_out_down);

        return true;
      case R.id.action_custom_camera:
        CameraActivity.showCameraActivity(MainActivity.this, "Camera");

        return true;

      case R.id.action_intro:
        intent = new Intent();
        intent.setClass(MainActivity.this, IntroActivity.class);
        startActivity(intent);

        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == 1337)
    {
      if (resultCode == RESULT_OK) {
        String path = data.getStringExtra("path");

        if (path != null) {
          File theFile = new File(path);
          if (theFile.exists()) {
            Toast.makeText(this, "Saved to: " + theFile.getPath(), Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(this, "Save OK? path doesn't exist.", Toast.LENGTH_LONG).show();
          }
        }
      }
    }
  }

  private void activateStrictMode() {
    if (Util.isDebugMode(this)) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//          .detectAll() // for all detectable problems
          .detectDiskReads()
          .detectDiskWrites()
          .detectNetwork()
          .penaltyLog()
          .build());
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
//          .penaltyDeath()
          .build());
    }
  }

}



 */