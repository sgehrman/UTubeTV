package com.sickboots.sickvideos.youtube;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoCategory;
import com.google.api.services.youtube.model.VideoCategoryListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.AppUtils;
import com.sickboots.sickvideos.misc.Auth;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.Preferences;
import com.sickboots.sickvideos.misc.Utils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class YouTubeAPI {

  public interface YouTubeAPIListener {
    public void handleAuthIntent(final Intent authIntent);
  }

  ;

  public enum RelatedPlaylistType {FAVORITES, LIKES, UPLOADS, WATCHED, WATCHLATER}

  public static final int REQ_PLAYER_CODE = 334443;
  private YouTube youTube;
  boolean highQualityImages = true;
  Context mContext;
  YouTubeAPIListener mListener;

  public YouTubeAPI(Context context, YouTubeAPIListener listener) {
    super();

    mListener = listener;
    mContext = context.getApplicationContext();
  }

  public static void playMovie(Activity activity, String movieID) {
    boolean fullScreen = AppUtils.preferences(activity).getBoolean(Preferences.PLAY_FULLSCREEN, false);

    Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, Auth.devKey(), movieID, 0, true, !fullScreen);
    activity.startActivityForResult(intent, REQ_PLAYER_CODE);
  }

  public static void playMovieUsingIntent(Context context, String videoId) {
    Intent intent = YouTubeIntents.createPlayVideoIntent(context, videoId);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // need this to start activity if we just have a plain context (not an Activity)
    context.startActivity(intent);
  }

  public YouTube youTube() {
    if (youTube == null) {
      try {
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), Auth.getCredentials(mContext)).setApplicationName("YouTubeAPI").build();
      } catch (Exception e) {
        e.printStackTrace();
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    return youTube;
  }

  // pass null for your personal channelID
  public String channelID(String userName) {
    String result = null;

    try {
      YouTube.Channels.List channelRequest = youTube().channels().list("id");

      if (userName != null)
        channelRequest.setForUsername(userName);  // for example: JamesBurkeConnection
      else
        channelRequest.setMine(true);

      channelRequest.setMaxResults(1L);

      channelRequest.setFields("items/id");
      ChannelListResponse channelResult = channelRequest.execute();

      List<Channel> channelsList = channelResult.getItems();

      result = channelsList.get(0).getId();
    } catch (UserRecoverableAuthIOException e) {
      handleException(e);
    } catch (Exception e) {
      handleException(e);
    }

    return result;
  }

  public VideoListResults videoListResults(String playlistID) {
    VideoListResults result = new VideoListResults(playlistID);

    return result;
  }

  public PlaylistListResults playlistListResults(String channelID, boolean addRelatedPlaylists) {
    PlaylistListResults result = new PlaylistListResults(channelID, addRelatedPlaylists);

    return result;
  }

  public SearchListResults searchListResults(String query) {
    SearchListResults result = new SearchListResults(query);

    return result;
  }

  public SubscriptionListResults subscriptionListResults() {
    SubscriptionListResults result = new SubscriptionListResults();

    return result;
  }

  public CategoriesListResults categoriesListResults(String regionCode) {
    CategoriesListResults result = new CategoriesListResults(regionCode);

    return result;
  }

  public LikedVideosListResults likedVideosListResults() {
    LikedVideosListResults result = new LikedVideosListResults();

    return result;
  }

  public VideoInfoListResults videoInfoListResults(List<String> videoIds) {
    VideoInfoListResults result = new VideoInfoListResults(videoIds);

    return result;
  }

  // pass null for channelid to get our own channel
  public String relatedPlaylistID(RelatedPlaylistType type, String channelID) {
    Map<RelatedPlaylistType, String> playlistMap = relatedPlaylistIDs(channelID);

    return playlistMap.get(type);
  }

  private String removeNewLinesFromString(String text) {
    return text.replace('\n', ' ');
  }

  private void doHandleAuthIntent(Intent authIntent) {
    Utils.toast(mContext, "Need Authorization");
    if (mListener != null)
      mListener.handleAuthIntent(authIntent);
  }

  private void doHandleExceptionMessage(String message) {
    Utils.toast(mContext, message);
  }

  // pass null for channelid to get our own channel
  private Map<RelatedPlaylistType, String> relatedPlaylistIDs(String channelID) {
    Map<RelatedPlaylistType, String> result = new EnumMap<RelatedPlaylistType, String>(RelatedPlaylistType.class);

    try {
      YouTube.Channels.List channelRequest = youTube().channels().list("contentDetails");
      if (channelID != null) {
        channelRequest.setId(channelID);
      } else {
        channelRequest.setMine(true);
      }

      channelRequest.setFields("items/contentDetails, nextPageToken");
      ChannelListResponse channelResult = channelRequest.execute();

      List<Channel> channelsList = channelResult.getItems();
      if (channelsList != null) {
        ChannelContentDetails.RelatedPlaylists relatedPlaylists = channelsList.get(0).getContentDetails().getRelatedPlaylists();

        result.put(RelatedPlaylistType.FAVORITES, relatedPlaylists.getFavorites());
        result.put(RelatedPlaylistType.LIKES, relatedPlaylists.getLikes());
        result.put(RelatedPlaylistType.UPLOADS, relatedPlaylists.getUploads());
        result.put(RelatedPlaylistType.WATCHED, relatedPlaylists.getWatchHistory());
        result.put(RelatedPlaylistType.WATCHLATER, relatedPlaylists.getWatchLater());
      }
    } catch (UserRecoverableAuthIOException e) {
      handleException(e);
    } catch (Exception e) {
      handleException(e);
    }

    return result;
  }

  private void handleException(Exception e) {
    if (e.getClass().equals(UserRecoverableAuthIOException.class)) {
      UserRecoverableAuthIOException r = (UserRecoverableAuthIOException) e;

      Intent intent = null;
      try {
        intent = r.getIntent();
      } catch (Exception ee) {
        // ignore, this happens if we kill the activity quickly before our async task finishes
      }
      if (intent != null)
        doHandleAuthIntent(intent);
    } else if (e.getClass().equals(GoogleAuthIOException.class)) {
      // could be a bad user name, let's pass it to the listener to check
      doHandleAuthIntent(null);
    } else if (e.getClass().equals(GoogleJsonResponseException.class)) {
      GoogleJsonResponseException r = (GoogleJsonResponseException) e;

      e.printStackTrace();

      doHandleExceptionMessage("JSON Error: " + r.getDetails().getCode() + " : " + r.getDetails().getMessage());
    } else {
      doHandleExceptionMessage("Exception Occurred: " + e.toString());

      e.printStackTrace();
    }
  }

  private String thumbnailField() {
    String result = "snippet/thumbnails/default/url";

    if (highQualityImages)
      result = "snippet/thumbnails/high/url";

    return result;
  }

  private String thumbnailURL(ThumbnailDetails details) {
    String result = null;

    if (details != null) {
      Thumbnail thumbnail = details.getMaxres();

      // is this necessary?  not sure
      if (thumbnail == null) {
        thumbnail = details.getHigh();
      }

      if (thumbnail == null) {
        thumbnail = details.getDefault();
      }

      if (thumbnail != null) {
        result = thumbnail.getUrl();
      }
    }

    return result;
  }

  // ========================================================
  // VideoListResults

  public class VideoListResults extends BaseListResults {
    private String playlistID;

    public VideoListResults(String p) {
      super();

      playlistID = p;
      setItems(itemsForNextToken(""));
    }

    protected List<YouTubeData> itemsForNextToken(String token) {
      List<PlaylistItem> playlistItemList = null;

      if (playlistID != null) {
        try {
          YouTube.PlaylistItems.List listRequest = youTube().playlistItems().list("id, contentDetails, snippet");
          listRequest.setPlaylistId(playlistID);

          listRequest.setFields(String.format("items(contentDetails/videoId, snippet/title, snippet/description, %s), nextPageToken", thumbnailField()));

          listRequest.setPageToken(token);
          listRequest.setMaxResults(getMaxResultsNeeded());
          PlaylistItemListResponse playListResponse = listRequest.execute();

          playlistItemList = playListResponse.getItems();
          response = playListResponse;

        } catch (UserRecoverableAuthIOException e) {
          handleResultsException(e);
        } catch (Exception e) {
          handleResultsException(e);
        }
      }

      return playlistItemsToMap(playlistItemList);
    }

    private List<YouTubeData> playlistItemsToMap(List<PlaylistItem> playlistItemList) {
      // check parameters
      if (playlistItemList == null) return null;

      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (PlaylistItem playlistItem : playlistItemList) {
        YouTubeData map = new YouTubeData();

        map.mVideo = playlistItem.getContentDetails().getVideoId();
        map.mTitle = playlistItem.getSnippet().getTitle();
        map.mDescription = removeNewLinesFromString(playlistItem.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(playlistItem.getSnippet().getThumbnails());

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // SearchListResults

  public class SearchListResults extends BaseListResults {
    private String query;

    public SearchListResults(String q) {
      query = q;
      setItems(itemsForNextToken(""));
    }

    protected List<YouTubeData> itemsForNextToken(String token) {
      List<SearchResult> result = new ArrayList<SearchResult>();
      SearchListResponse searchListResponse = null;

      try {
        YouTube.Search.List listRequest = youTube().search().list("id, snippet");

        listRequest.setQ(query);
        listRequest.setKey(Auth.devKey());
        listRequest.setType("video");
        listRequest.setFields(String.format("items(id/videoId, snippet/title, snippet/description, %s), nextPageToken", thumbnailField()));
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        searchListResponse = listRequest.execute();

        // nasty double cast?
        response = searchListResponse;

        result.addAll(searchListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return searchResultsToMap(result);
    }

    private List<YouTubeData> searchResultsToMap(List<SearchResult> playlistItemList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (SearchResult playlistItem : playlistItemList) {
        YouTubeData map = new YouTubeData();

        map.mVideo = playlistItem.getId().getVideoId();
        map.mTitle = playlistItem.getSnippet().getTitle();
        map.mDescription = removeNewLinesFromString(playlistItem.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(playlistItem.getSnippet().getThumbnails());

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // LikedVideosListResults

  public class LikedVideosListResults extends BaseListResults {
    public LikedVideosListResults() {
      setItems(itemsForNextToken(""));
    }

    protected List<YouTubeData> itemsForNextToken(String token) {
      List<Video> result = new ArrayList<Video>();
      VideoListResponse searchListResponse = null;

      try {
        YouTube.Videos.List listRequest = youTube().videos().list("id, snippet, contentDetails");

        listRequest.setKey(Auth.devKey());
        listRequest.setFields(String.format("items(id, snippet/title, snippet/description, contentDetails/duration, %s), nextPageToken", thumbnailField()));
        listRequest.setMyRating("like");
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        searchListResponse = listRequest.execute();

        // nasty double cast?
        response = searchListResponse;

        result.addAll(searchListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return searchResultsToMap(result);
    }

    private List<YouTubeData> searchResultsToMap(List<Video> playlistItemList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Video playlistItem : playlistItemList) {
        YouTubeData map = new YouTubeData();

        map.mVideo = playlistItem.getId();
        map.mTitle = playlistItem.getSnippet().getTitle();
        map.mDescription = removeNewLinesFromString(playlistItem.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(playlistItem.getSnippet().getThumbnails());
        map.mDuration = Utils.durationToDuration((String) playlistItem.getContentDetails().get("duration"));

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // VideoInfoListResults

  public class VideoInfoListResults extends BaseListResults {
    List<String> mVideoIds;

    public VideoInfoListResults(List<String> videoIds) {
      mVideoIds = videoIds;

      if (mVideoIds.size() > 50) {
        Debug.log("VideoInfoListResults can only handle 50 videos at a time.");

        mVideoIds = videoIds.subList(0, 50);
      }

      setItems(itemsForNextToken(""));
    }

    protected List<YouTubeData> itemsForNextToken(String tokenNotUsed) {
      List<Video> result = new ArrayList<Video>();
      VideoListResponse searchListResponse = null;

      try {
        YouTube.Videos.List listRequest = youTube().videos().list("id, contentDetails");

        listRequest.setKey(Auth.devKey());
        listRequest.setFields("items(id, contentDetails/duration)");
        listRequest.setId(TextUtils.join(",", mVideoIds));

        searchListResponse = listRequest.execute();

        result.addAll(searchListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return searchResultsToMap(result);
    }

    private List<YouTubeData> searchResultsToMap(List<Video> playlistItemList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Video playlistItem : playlistItemList) {
        YouTubeData map = new YouTubeData();

        map.mVideo = playlistItem.getId();
        map.mDuration = Utils.durationToDuration((String) playlistItem.getContentDetails().get("duration"));

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // CategoriesListResults

  public class CategoriesListResults extends BaseListResults {
    public CategoriesListResults(String regionCode) {
      setItems(itemsForNextToken(regionCode));
    }

    protected List<YouTubeData> itemsForNextToken(String regionCode) {
      List<VideoCategory> result = new ArrayList<VideoCategory>();
      VideoCategoryListResponse categoryListResponse = null;

      try {
        YouTube.VideoCategories.List listRequest = youTube().videoCategories().list("id, snippet");

        listRequest.setKey(Auth.devKey());
        listRequest.setRegionCode(regionCode);
        listRequest.setFields("items(snippet/title, snippet/channelId)");

        categoryListResponse = listRequest.execute();

        result.addAll(categoryListResponse.getItems());

        response = categoryListResponse;
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return searchResultsToMap(result);
    }

    private List<YouTubeData> searchResultsToMap(List<VideoCategory> itemList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (VideoCategory category : itemList) {
        YouTubeData map = new YouTubeData();

        map.mChannel = category.getSnippet().getChannelId();
        map.mTitle = category.getSnippet().getTitle();

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // SubscriptionListResults

  public class SubscriptionListResults extends BaseListResults {
    public SubscriptionListResults() {
      super();

      setItems(itemsForNextToken(""));
    }

    protected List<YouTubeData> itemsForNextToken(String token) {
      List<Subscription> result = new ArrayList<Subscription>();

      try {
        YouTube.Subscriptions.List listRequest = youTube().subscriptions().list("id, snippet");
        listRequest.setMine(true);

        listRequest.setFields(String.format("items(snippet/title, snippet/resourceId, snippet/description, %s), nextPageToken", thumbnailField()));
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        SubscriptionListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;

        result.addAll(subscriptionListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return subscriptionListToMap(result);
    }

    private List<YouTubeData> subscriptionListToMap(List<Subscription> subscriptionsList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Subscription subscription : subscriptionsList) {
        YouTubeData map = new YouTubeData();

        map.mTitle = subscription.getSnippet().getTitle();
        map.mChannel = subscription.getSnippet().getResourceId().getChannelId();
        map.mDescription = removeNewLinesFromString(subscription.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(subscription.getSnippet().getThumbnails());

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // PlaylistListResults

  public class PlaylistListResults extends BaseListResults {
    private String channelID;

    public PlaylistListResults(String c, boolean addRelated) {
      super();

      channelID = c;

      List<YouTubeData> related = new ArrayList<YouTubeData>();
      if (addRelated) {
        Map<RelatedPlaylistType, String> playlistMap = relatedPlaylistIDs(c);

        for (Map.Entry<RelatedPlaylistType, String> entry : playlistMap.entrySet()) {
          String playlistID = entry.getValue();

          if (playlistID != null) {
            YouTubeData map = new YouTubeData();

            map.mPlaylist = playlistID;

            switch (entry.getKey()) {
              case FAVORITES:
                map.mTitle = "Favorites";
                break;
              case LIKES:
                map.mTitle = "LIKES";
                break;
              case UPLOADS:
                map.mTitle = "UPLOADS";
                break;
              case WATCHED:
                map.mTitle = "WATCHED";
                break;
              case WATCHLATER:
                map.mTitle = "WATCHLATER";
                break;
            }

            related.add(map);
          }
        }
      }

      related.addAll(itemsForNextToken(""));

      setItems(related);
    }

    protected List<YouTubeData> itemsForNextToken(String token) {
      List<Playlist> result = new ArrayList<Playlist>();

      try {
        YouTube.Playlists.List listRequest = youTube().playlists().list("id, snippet");

        // if channel null, assume the users channel
        if (channelID == null)
          listRequest.setMine(true);
        else
          listRequest.setChannelId(channelID);

        listRequest.setFields(String.format("items(id, snippet/title, snippet/description, %s), nextPageToken", thumbnailField()));
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        PlaylistListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;

        result.addAll(subscriptionListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return playlistItemsToMap(result);
    }

    private List<YouTubeData> playlistItemsToMap(List<Playlist> subscriptionsList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Playlist subscription : subscriptionsList) {
        YouTubeData map = new YouTubeData();

        map.mPlaylist = subscription.getId();
        map.mTitle = subscription.getSnippet().getTitle();
        map.mDescription = removeNewLinesFromString(subscription.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(subscription.getSnippet().getThumbnails());

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // BaseListResults

  abstract public class BaseListResults {
    protected Object response;
    private List<YouTubeData> items;
    private boolean reloadingFlag = false;

    // subclasses must implement
    abstract protected List<YouTubeData> itemsForNextToken(String token);

    public BaseListResults() {
      super();
    }

    public List<YouTubeData> getItems() {
      List<YouTubeData> tmp = items;

      // clear for every get
      items = null;

      return tmp;
    }

    public void setItems(List<YouTubeData> l) {
      items = l;
    }

    public boolean getNext() {
      boolean result = false;

      String token = nextToken();
      if (token != null) {
        List<YouTubeData> newItems = itemsForNextToken(token);

        if (newItems != null) {
          if (items == null)
            items = new ArrayList<YouTubeData>();

          items.addAll(newItems);

          result = true;
        }
      } else {
        // no more tokens, we are done
        done();
      }

      return result;
    }

    private String nextToken() {
      String result = null;

      if (response != null) {
        // is there a better way of doing this?
        if (response instanceof SearchListResponse) {
          result = ((SearchListResponse) response).getNextPageToken();
        } else if (response instanceof PlaylistItemListResponse) {
          result = ((PlaylistItemListResponse) response).getNextPageToken();
        } else if (response instanceof SubscriptionListResponse) {
          result = ((SubscriptionListResponse) response).getNextPageToken();
        } else if (response instanceof VideoListResponse) {
          result = ((VideoListResponse) response).getNextPageToken();
        } else if (response instanceof PlaylistListResponse) {
          result = ((PlaylistListResponse) response).getNextPageToken();
        } else {
          doHandleExceptionMessage("nextToken bug!");
        }
      }

      return result;
    }

    public void setIsReloading(boolean set) {
      reloadingFlag = set;
    }

    public boolean isReloading() {
      return reloadingFlag;
    }

    protected long getMaxResultsNeeded() {
      // avoid exception with setMaxResults: Values must be within the range: [0, 50]
      return 50;
    }

    private void done() {
      response = null; // avoid trying to get nextToken or possibly retriggering an exception
    }

    protected void handleResultsException(Exception e) {
      // must call done otherwise we get an endless loop as it continues to retry since it thinks there are still items to fetch
      done();
      handleException(e);
    }

  }

}
