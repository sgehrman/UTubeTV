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
  private static ProductCode mProductCode = ProductCode.ROGAN;

  public static String[] drawerTitles() {
    switch (mProductCode) {
      case NEURO_SOUP:
      case CONNECTIONS:
      case ROGAN:
      case VICE:
        return new String[]{"About", "Playlists"};
      case USER:
        return new String[]{"About", "Favorites", "Likes", "History", "Uploads", "Watch Later", "Color Picker", "Connections", "Connections Intent"};
    }

    return null;
  }

  public static Fragment fragmentForIndex(int index) {
    Fragment fragment = null;

    switch (mProductCode) {
      case NEURO_SOUP:
      case CONNECTIONS:
      case ROGAN:
      case VICE:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment();
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID()));
            break;
        }
        break;
      case USER:
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
      case CONNECTIONS:
        return "UC07XXQh04ukEX68loZFgnVw";
      case USER:
        return "UC07XXQh04ukEX68loZFgnVw";
      case NEURO_SOUP:
        return "UCf--Le-Ssa_R5ERoM7PbdcA";
      case VICE:
        return "UCn8zNIfYAQNdrFRrr8oibKw";
      case ROGAN:
        return "UCzQUP1qoWDoEbmsQxvdjxgQ";

    }

    return null;
  }

  public static enum ProductCode {NEURO_SOUP, CONNECTIONS, VICE, ROGAN, USER}
}
