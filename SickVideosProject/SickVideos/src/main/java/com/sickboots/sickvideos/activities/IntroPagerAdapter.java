package com.sickboots.sickvideos.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class IntroPagerAdapter extends FragmentPagerAdapter {
  private final Context context;

  public IntroPagerAdapter(FragmentManager fm, Context context) {
    super(fm);
    this.context = context;
  }

  @Override
  public Fragment getItem(int position) {
    return new IntroPageFragment(position);
  }

  @Override
  public int getCount() {
    return 4;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return null;
  }

  public int getIconResID(int position) {
    return 0;
  }
}
