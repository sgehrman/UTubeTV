package com.sickboots.sickvideos.services;

import android.os.Parcel;
import android.os.Parcelable;

import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.misc.Utils;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.HashMap;

public class YouTubeServiceRequest implements Parcelable {
  public enum RequestType {RELATED, SUBSCRIPTIONS, SEARCH, CATEGORIES, LIKED, PLAYLISTS, VIDEOS}

  private HashMap data;
  private RequestType type;

  public static YouTubeServiceRequest relatedRequest(YouTubeAPI.RelatedPlaylistType relatedPlayListType, String channelID) {
    YouTubeServiceRequest result = emptyRequest(RequestType.RELATED);

    result.data.put("type", relatedPlayListType);
    result.data.put("channel", channelID);

    return result;
  }

  public static YouTubeServiceRequest videosRequest(String playlistID) {
    YouTubeServiceRequest result = emptyRequest(RequestType.VIDEOS);

    result.data.put("playlist", playlistID);

    return result;
  }

  public static YouTubeServiceRequest searchRequest(String query) {
    YouTubeServiceRequest result = emptyRequest(RequestType.SEARCH);

    result.data.put("query", query);

    return result;
  }

  public static YouTubeServiceRequest subscriptionsRequest() {
    YouTubeServiceRequest result = emptyRequest(RequestType.SUBSCRIPTIONS);

    return result;
  }

  public static YouTubeServiceRequest categoriesRequest() {
    YouTubeServiceRequest result = emptyRequest(RequestType.CATEGORIES);

    return result;
  }

  public static YouTubeServiceRequest likedRequest() {
    YouTubeServiceRequest result = emptyRequest(RequestType.LIKED);

    return result;
  }

  public static YouTubeServiceRequest playlistsRequest(String channelID) {
    YouTubeServiceRequest result = emptyRequest(RequestType.PLAYLISTS);

    result.data.put("channel", channelID);

    return result;
  }

  public RequestType type() {
    return type;
  }

  public Object getData(String key) {
    return data.get(key);
  }

  public String name() {
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

        YouTubeAPI.RelatedPlaylistType type = (YouTubeAPI.RelatedPlaylistType) getData("type");
        switch (type) {
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
    String result = name();

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

    Utils.log("databaseTable null");

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
