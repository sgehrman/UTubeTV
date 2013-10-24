package com.sickboots.sickvideos;

import java.util.HashMap;

/**
 * Created by sgehrman on 10/23/13.
 */
public class YouTubeListSpec {
  public enum ListType {PLAYLIST, RELATED, SUBSCRIPTIONS, SEARCH};

  private HashMap data;
  public ListType type;

  public static YouTubeListSpec playlistSpec(String playlistID) {
    YouTubeListSpec result = emptySpec(ListType.PLAYLIST);

    result.data.put("id", playlistID);

    return result;
  }

  public static YouTubeListSpec relatedSpec(YouTubeHelper.RelatedPlaylistType relatedPlayListType) {
    YouTubeListSpec result = emptySpec(ListType.RELATED);

    result.data.put("type", relatedPlayListType);

    return result;
  }

  public static YouTubeListSpec searchSpec(String query) {
    YouTubeListSpec result = emptySpec(ListType.SEARCH);

    result.data.put("query", query);

    return result;
  }

  public static YouTubeListSpec subscriptionsSpec() {
    YouTubeListSpec result = emptySpec(ListType.SUBSCRIPTIONS);

    return result;
  }

  public Object getData(String key) {
    return data.get(key);
  }

  // ===================================================================
  // private

  private static YouTubeListSpec emptySpec(ListType type) {
    YouTubeListSpec result = new YouTubeListSpec();

    result.type = type;
    result.data = new HashMap();

    return result;
  }

}
