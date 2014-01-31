package com.sickboots.sickvideos;

import android.app.Fragment;

/**
 * Created by sgehrman on 1/24/14.
 */
// also used by about fragment, but had to put it somewhere
public interface DrawerActivitySupport {
  public boolean actionBarTitleHandled();

  public void showPlaylistsFragment();

  public Content getContent();  // about fragment can't be passed data, it must request it

  public void playVideo(String videoId, String title);

  public void installFragment(Fragment fragment, boolean animate);
}
