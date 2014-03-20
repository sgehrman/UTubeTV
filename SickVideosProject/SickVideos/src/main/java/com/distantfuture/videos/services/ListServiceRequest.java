package com.distantfuture.videos.services;

import android.content.Context;
import android.os.Bundle;

import com.distantfuture.videos.database.DatabaseTables;
import com.distantfuture.videos.misc.DUtils;
import com.distantfuture.videos.youtube.YouTubeAPI;

public class ListServiceRequest {
  private static final int CLASS_TYPE_KEY = 1887;
  private ServiceRequest serviceRequest;

  private ListServiceRequest() {
    super();
  }

  private ListServiceRequest(ServiceRequest serviceRequest) {
    super();

    this.serviceRequest = serviceRequest;
  }

  public static ListServiceRequest fromBundle(Bundle bundle) {
    ListServiceRequest result = null;
    ServiceRequest request = ServiceRequest.fromBundle(bundle);

    Integer intValue = (Integer) request.getData(ServiceRequest.REQUEST_CLASS_TYPE_KEY);

    if (intValue != null) {
      if (intValue == CLASS_TYPE_KEY)
        result = new ListServiceRequest(request);
    }

    return result;
  }

  public static ListServiceRequest relatedRequest(YouTubeAPI.RelatedPlaylistType relatedPlayListType, String channelID, String containerName, int maxResults) {
    ListServiceRequest result = emptyRequest(RequestType.RELATED);

    result.serviceRequest.putInt("maxResults", maxResults);
    result.serviceRequest.putString("containerName", containerName);
    result.serviceRequest.putString("relatedType", relatedPlayListType.toString());
    result.serviceRequest.putString("channel", channelID);

    return result;
  }

  public static ListServiceRequest videosRequest(String playlistID, String containerName) {
    ListServiceRequest result = emptyRequest(RequestType.VIDEOS);

    result.serviceRequest.putString("containerName", containerName);
    result.serviceRequest.putString("playlist", playlistID);

    return result;
  }

  public static ListServiceRequest searchRequest(String query, String containerName) {
    ListServiceRequest result = emptyRequest(RequestType.SEARCH);

    result.serviceRequest.putString("containerName", containerName);
    result.serviceRequest.putString("query", query);

    return result;
  }

  public static ListServiceRequest subscriptionsRequest(String containerName) {
    ListServiceRequest result = emptyRequest(RequestType.SUBSCRIPTIONS);

    result.serviceRequest.putString("containerName", containerName);

    return result;
  }

  public static ListServiceRequest categoriesRequest(String containerName) {
    ListServiceRequest result = emptyRequest(RequestType.CATEGORIES);

    result.serviceRequest.putString("containerName", containerName);

    return result;
  }

  public static ListServiceRequest likedRequest(String containerName) {
    ListServiceRequest result = emptyRequest(RequestType.LIKED);

    result.serviceRequest.putString("containerName", containerName);

    return result;
  }

  public static ListServiceRequest playlistsRequest(String channelID, String containerName, int maxResults) {
    ListServiceRequest result = emptyRequest(RequestType.PLAYLISTS);

    result.serviceRequest.putInt("maxResults", maxResults);
    result.serviceRequest.putString("containerName", containerName);
    result.serviceRequest.putString("channel", channelID);

    return result;
  }

  private static ListServiceRequest emptyRequest(RequestType type) {
    ListServiceRequest result = new ListServiceRequest();

    result.serviceRequest = new ServiceRequest();
    result.serviceRequest.putInt(ServiceRequest.REQUEST_CLASS_TYPE_KEY, CLASS_TYPE_KEY);

    result.serviceRequest.putString("type", type.toString());

    return result;
  }

  public Bundle toBundle() {
    return ServiceRequest.toBundle(serviceRequest);
  }

  public RequestType type() {
    return RequestType.valueOf((String) serviceRequest.getData("type"));
  }

  public int maxResults() {
    int result = 0;

    Integer intObject = (Integer) serviceRequest.getData("maxResults");

    if (intObject != null)
      result = intObject;

    return result;
  }

  public String containerName() {
    return (String) serviceRequest.getData("containerName");
  }

  public String channel() {
    return (String) serviceRequest.getData("channel");
  }

  public String playlist() {
    return (String) serviceRequest.getData("playlist");
  }

  public String query() {
    return (String) serviceRequest.getData("query");
  }

  public YouTubeAPI.RelatedPlaylistType relatedType() {
    return YouTubeAPI.RelatedPlaylistType.valueOf((String) serviceRequest.getData("relatedType"));
  }

  public String unitName(boolean plural) {
    String result = (plural) ? "Items" : "Item";

    switch (type()) {
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
        switch (relatedType()) {
          case UPLOADS:
            result = (plural) ? "Recent Uploads" : "Recent Upload";
            break;
          case LIKES:
            result = (plural) ? "Liked Videos" : "Liked Video";
            break;
          case FAVORITES:
          case WATCHED:
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

  // ===================================================================
  //  Parcelable - we send this to the service inside an intent

  private String typeToString() {
    String result = "YouTube";

    switch (type()) {
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

        switch (relatedType()) {
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

    switch (type()) {
      case SUBSCRIPTIONS:
        break;
      case PLAYLISTS:
        result += serviceRequest.getData("channel");
        break;
      case CATEGORIES:
        break;
      case LIKED:
        break;
      case RELATED:
        result += serviceRequest.getData("channel");
        break;
      case VIDEOS:
        result += serviceRequest.getData("playlist");
        break;
      case SEARCH:
        result += serviceRequest.getData("query");
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

      case SUBSCRIPTIONS:
      case CATEGORIES:
        break;
    }

    DUtils.log("databaseTable null");

    return null;
  }

  public void runTask(Context context, boolean hasFetchedData, boolean refresh) {
    new ListServiceTask(context, this, hasFetchedData, refresh);
  }

  public enum RequestType {RELATED, SUBSCRIPTIONS, SEARCH, CATEGORIES, LIKED, PLAYLISTS, VIDEOS}
}
