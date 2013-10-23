package com.sickboots.sickvideos;

import java.util.HashMap;

/**
 * Created by sgehrman on 10/23/13.
 */
public class YouTubeListSpec {
  private HashMap data;

  public static YouTubeListSpec playlistSpec(String playlistID) {
    YouTubeListSpec result = emptySpec();

    result.data.put("type", "playlist");

    return result;
  }

  public static YouTubeListSpec relatedPlaylistSpec(int relatedPlayListIndex) {
    YouTubeListSpec result = emptySpec();

    result.data.put("type", "related");

    return result;
  }

  public static YouTubeListSpec searchSpec(String query) {
    YouTubeListSpec result = emptySpec();

    result.data.put("type", "search");

    return result;
  }

  public static YouTubeListSpec subscriptionsSpec(String query) {
    YouTubeListSpec result = emptySpec();

    result.data.put("type", "subscriptions");

    return result;
  }

  // ===================================================================
  // private

  private static YouTubeListSpec emptySpec() {
    YouTubeListSpec result = new YouTubeListSpec();

    result.data = new HashMap();

    return result;
  }

}
