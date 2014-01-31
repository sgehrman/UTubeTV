package com.sickboots.sickvideos.services;

import android.os.Parcel;
import android.os.Parcelable;

import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.HashMap;

public class YouTubeServiceRequest implements Parcelable {
  public enum RequestType {RELATED, SUBSCRIPTIONS, SEARCH, CATEGORIES, LIKED, PLAYLISTS, VIDEOS}

  private HashMap data;
  private RequestType type;

  public static YouTubeServiceRequest relatedRequest(YouTubeAPI.RelatedPlaylistType relatedPlayListType, String channelID, String containerName, int maxResults) {
    YouTubeServiceRequest result = emptyRequest(RequestType.RELATED);

    result.data.put("maxResults", maxResults);
    result.data.put("containerName", containerName);
    result.data.put("type", relatedPlayListType);
    result.data.put("channel", channelID);

    return result;
  }

  public static YouTubeServiceRequest videosRequest(String playlistID, String containerName) {
    YouTubeServiceRequest result = emptyRequest(RequestType.VIDEOS);

    result.data.put("containerName", containerName);
    result.data.put("playlist", playlistID);

    return result;
  }

  public static YouTubeServiceRequest searchRequest(String query, String containerName) {
    YouTubeServiceRequest result = emptyRequest(RequestType.SEARCH);

    result.data.put("containerName", containerName);
    result.data.put("query", query);

    return result;
  }

  public static YouTubeServiceRequest subscriptionsRequest(String containerName) {
    YouTubeServiceRequest result = emptyRequest(RequestType.SUBSCRIPTIONS);

    result.data.put("containerName", containerName);

    return result;
  }

  public static YouTubeServiceRequest categoriesRequest(String containerName) {
    YouTubeServiceRequest result = emptyRequest(RequestType.CATEGORIES);

    result.data.put("containerName", containerName);

    return result;
  }

  public static YouTubeServiceRequest likedRequest(String containerName) {
    YouTubeServiceRequest result = emptyRequest(RequestType.LIKED);

    result.data.put("containerName", containerName);

    return result;
  }

  public static YouTubeServiceRequest playlistsRequest(String channelID, String containerName, int maxResults) {
    YouTubeServiceRequest result = emptyRequest(RequestType.PLAYLISTS);

    result.data.put("maxResults", maxResults);
    result.data.put("containerName", containerName);
    result.data.put("channel", channelID);

    return result;
  }

  public RequestType type() {
    return type;
  }

  public int maxResults() {
    int result = 0;

    Integer intObject = (Integer) getData("maxResults");

    if (intObject != null)
      result = intObject.intValue();

    return result;
  }

  public Object getData(String key) {
    Object result = null;

    if (data.containsKey(key))
      result = data.get(key);

    return result;
  }

  public String containerName() {
    return (String) getData("containerName");
  }

  public String unitName(boolean plural) {
    String result = (plural) ? "Items" : "Item";

    switch (type) {
      case SUBSCRIPTIONS:
          result = (plural) ? "Subscriptions" : "Subscription";
        break;
      case CATEGORIES:
          result = (plural) ? "Categories" : "Category";
        break;
      case PLAYLISTS:
        result = (plural) ? "Playlists" : "Playlist";
        break;
      case RELATED:
        result = (plural) ? "Videos" : "Video";
        YouTubeAPI.RelatedPlaylistType playlistType = (YouTubeAPI.RelatedPlaylistType) getData("type");
        switch (playlistType) {
          case UPLOADS:
            result = (plural) ? "Recent Uploads" : "Recent Upload";
            break;
          case FAVORITES:
          case WATCHED:
          case LIKES:
          case WATCHLATER:
          default:
            break;
        }

        break;
      case LIKED:
      case VIDEOS:
      case SEARCH:
          result = (plural) ? "Videos" : "Video";
        break;
    }

    return result;
  }

  // just used for debugging
  @Override
  public String toString() {
    String result = ((Object) this).getClass().getName();

    result += "\ntype: " + type().toString();
    result += "\ndata: " + data.toString();

    return result;
  }

  private String typeToString() {
    String result = "YouTube";

    switch (type) {
      case SUBSCRIPTIONS:
        result = "Subscriptions";
        break;
      case PLAYLISTS:
        result = "Playlists";
        break;
      case CATEGORIES:
        result = "Categories";
        break;
      case LIKED:
        result = "Liked";
        break;
      case RELATED:
        result = "Related Playlists";

        YouTubeAPI.RelatedPlaylistType playlistType = (YouTubeAPI.RelatedPlaylistType) getData("type");
        switch (playlistType) {
          case FAVORITES:
            result = "Favorites";
            break;
          case LIKES:
            result = "Likes";
            break;
          case UPLOADS:
            result = "Uploads";
            break;
          case WATCHED:
            result = "History";
            break;
          case WATCHLATER:
            result = "Watch later";
            break;
        }
        break;
      case VIDEOS:
        result = "Videos";
        break;
      case SEARCH:
        result = "Search";
        break;
    }

    return result;
  }

  // all items are added to db, but use group to get a specific list
  public String requestIdentifier() {
    String result = typeToString();

    switch (type) {
      case SUBSCRIPTIONS:
        break;
      case PLAYLISTS:
        result += getData("channel");
        break;
      case CATEGORIES:
        break;
      case LIKED:
        break;
      case RELATED:
        result += getData("channel");
        break;
      case VIDEOS:
        result += getData("playlist");
        break;
      case SEARCH:
        result += getData("query");
        break;
    }

    return result;
  }

  public DatabaseTables.DatabaseTable databaseTable() {
    switch (type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        return DatabaseTables.videoTable();

      case PLAYLISTS:
        return DatabaseTables.playlistTable();

      case CATEGORIES:
        break;
    }

    Debug.log("databaseTable null");

    return null;
  }

  // ===================================================================
  //  Parcelable - we send this to the service inside an intent

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeSerializable(type);
    dest.writeSerializable(data);
  }

  public static final Parcelable.Creator<YouTubeServiceRequest> CREATOR = new Parcelable.Creator<YouTubeServiceRequest>() {
    public YouTubeServiceRequest createFromParcel(Parcel in) {
      return new YouTubeServiceRequest(in);
    }

    public YouTubeServiceRequest[] newArray(int size) {
      return new YouTubeServiceRequest[size];
    }
  };

  private YouTubeServiceRequest(Parcel in) {
    type = (RequestType) in.readSerializable();
    data = (HashMap) in.readSerializable();
  }

  // ===================================================================
  // private

  private YouTubeServiceRequest() {
    super();
  }

  private static YouTubeServiceRequest emptyRequest(RequestType type) {
    YouTubeServiceRequest result = new YouTubeServiceRequest();

    result.type = type;
    result.data = new HashMap();

    return result;
  }
}
