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

  public static String[] drawerTitles() {
    String[] names = new String[]{"About", "Playlists"};

    return names;
  }

  public static Fragment fragmentForIndex(int index) {
    Fragment fragment = null;
    switch (index) {
      case 0:
        fragment = new ChannelAboutFragment();
        break;
      case 1:
        fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID()));
        break;
    }

    return fragment;
  }

  public static String channelID() {
    return "UC07XXQh04ukEX68loZFgnVw";  // connections
  }
}


/*
    old stuff

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
      fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest("UC07XXQh04ukEX68loZFgnVw"));
      break;
    case 8:
      YouTubeAPI.openPlaylistUsingIntent(this, "PLC5CD4355724A28FC");
      break;

 */