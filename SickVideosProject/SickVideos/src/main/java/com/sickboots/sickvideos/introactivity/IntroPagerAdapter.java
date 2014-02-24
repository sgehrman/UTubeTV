package com.sickboots.sickvideos.introactivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.List;

public class IntroPagerAdapter extends FragmentPagerAdapter {
  private List<IntroXMLParser.IntroPage> pages;
  private static int sChangeCount=0;

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

  public void notifyDataSetChanged() {
    sChangeCount += 100;

    super.notifyDataSetChanged();
  }

  public long getItemId(int position) {
    return position + sChangeCount;
  }
}
