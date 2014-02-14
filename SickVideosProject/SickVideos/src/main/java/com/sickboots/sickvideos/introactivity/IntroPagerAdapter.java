package com.sickboots.sickvideos.introactivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class IntroPagerAdapter extends FragmentPagerAdapter {
  public IntroPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int position) {
    return IntroPageFragment.newInstance(position);
  }

  @Override
  public int getCount() {
    return IntroPageFragment.numberOfPages();
  }
}
