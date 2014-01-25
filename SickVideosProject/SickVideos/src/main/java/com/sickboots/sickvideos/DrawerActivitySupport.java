package com.sickboots.sickvideos;

import android.app.Fragment;

import com.sickboots.sickvideos.youtube.VideoPlayer;

/**
 * Created by sgehrman on 1/24/14.
 */
// also used by about fragment, but had to put it somewhere
public interface DrawerActivitySupport {
  public VideoPlayer videoPlayer(boolean createIfNeeded);

  public void showPlaylistsFragment();
  public Content getContent();  // about fragment can't be passed data, it must request it

  public void installFragment(Fragment fragment, boolean animate);
}
