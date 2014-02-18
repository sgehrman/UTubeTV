package com.sickboots.sickvideos.mainactivity;

import android.app.Fragment;

// also used by about fragment, but had to put it somewhere
public interface DrawerActivitySupport {
  public boolean actionBarTitleHandled();

  // used in the about fragment when clicking on Watch Now or image
  public void showPlaylistsFragment();

  public void playVideo(String videoId, String title);

  public boolean isPlayerVisible();

  public void installFragment(Fragment fragment, boolean animate);

  public void setActionBarTitle(CharSequence title, CharSequence subtitle);
}
