package com.sickboots.sickvideos.introactivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.List;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class IntroPagerAdapter extends FragmentPagerAdapter {
  List<IntroXMLParser.IntroPage> pages;

  public IntroPagerAdapter(Context context, FragmentManager fm) {
    super(fm);
  }

  public void setPages(List<IntroXMLParser.IntroPage> pages) {
    this.pages = pages;
    notifyDataSetChanged();
  }

  public IntroXMLParser.IntroPage pageAtIndex(int position) {
    if (pages != null)
      return pages.get(position);

    return null;
  }

  @Override
  public Fragment getItem(int position) {
    return IntroPageFragment.newInstance(position);
  }

  @Override
  public int getCount() {
    if (pages != null)
      return pages.size();

    return 0;
  }
}
