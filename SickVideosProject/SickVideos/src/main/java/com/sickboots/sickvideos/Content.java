package com.sickboots.sickvideos;

import android.app.Fragment;

import com.sickboots.sickvideos.misc.ChannelAboutFragment;
import com.sickboots.sickvideos.misc.ColorPickerFragment;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

/**
 * Created by sgehrman on 1/2/14.
 */
public class Content {
  private static int mProductCode=0;

  public static String[] drawerTitles() {
    switch (mProductCode) {
      case 0:
        return new String[]{"About", "Playlists"};
      case 1:
        return new String[]{"About", "Favorites", "Likes", "History", "Uploads", "Watch Later", "Color Picker", "Connections", "Connections Intent"};
    }

    return null;
  }

  public static Fragment fragmentForIndex(int index) {
    Fragment fragment = null;

    switch (mProductCode) {
      case 0:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment();
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID()));
            break;
        }
      case 1:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment();
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.FAVORITES, null));
            break;
          case 2:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.LIKES, null));
            break;
          case 3:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.WATCHED, null));
            break;
          case 4:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, null));
            break;
          case 5:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.WATCHLATER, null));
            break;
          case 6:
            fragment = new ColorPickerFragment();
            break;
          case 7:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID()));
            break;
          case 8:
//            YouTubeAPI.openPlaylistUsingIntent(this, "PLC5CD4355724A28FC");
            break;
        }
    }

    return fragment;
  }

  public static String channelID() {
    switch (mProductCode) {
      case 0:
        return "UC07XXQh04ukEX68loZFgnVw";  // connections
      case 1:
        return "UC07XXQh04ukEX68loZFgnVw";  // connections
    }

    return null;
  }
}
