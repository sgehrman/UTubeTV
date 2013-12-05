package com.sickboots.sickvideos.youtube;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
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
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class YouTubeAPI {
  public enum RelatedPlaylistType {FAVORITES, LIKES, UPLOADS, WATCHED, WATCHLATER}

  public static final int REQ_PLAYER_CODE = 334443;
  private YouTubeHelperListener listener;
  private HttpRequestInitializer credential;
  private YouTube youTube;
  boolean highQualityImages = true;

  // must implement this listener
  public interface YouTubeHelperListener {
    public void handleAuthIntent(Intent authIntent);

    public void handleExceptionMessage(String message);
  }

  public YouTubeAPI(HttpRequestInitializer c, YouTubeHelperListener l) {
    super();

    listener = l;

    if (c == null) {
      c = new HttpRequestInitializer() {
        public void initialize(HttpRequest request) throws IOException {
        }
      };
    }

    credential = c;
  }

  public static String devKey() {
    return "AIzaSyD0gRStgO5O0hBRp4UeAxtsLFFw9bMinOI";
  }

  public static void playMovie(Activity activity, String movieID) {
    boolean fullScreen = ApplicationHub.preferences(activity).getBoolean(PreferenceCache.PLAY_FULLSCREEN, false);

    Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, YouTubeAPI.devKey(), movieID, 0, true, !fullScreen);
    activity.startActivityForResult(intent, REQ_PLAYER_CODE);
  }

  public static void playMovieUsingIntent(Context context, String videoId) {
    Intent intent = YouTubeIntents.createPlayVideoIntent(context, videoId);
    context.startActivity(intent);
  }

  public YouTube youTube() {
    if (youTube == null) {
      try {
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("YouTubeAPI").build();
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

  public VideoListResults videoListResults(String playlistID, boolean getMaximum) {
    VideoListResults result = new VideoListResults(playlistID, getMaximum);

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

  // pass null for channelid to get our own channel
  public String relatedPlaylistID(RelatedPlaylistType type, String channelID) {
    Map<RelatedPlaylistType, String> playlistMap = relatedPlaylistIDs(channelID);

    return playlistMap.get(type);
  }

  private String removeNewLinesFromString(String text) {
    return text.replace('\n', ' ');
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

      channelRequest.setFields("items/contentDetails, nextPageToken, pageInfo");
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

      if (listener != null) {
        Intent intent = null;
        try {
          intent = r.getIntent();
        } catch (Exception ee) {
          // ignore, this happens if we kill the activity quickly before our async task finishes
        }
        if (intent != null)
          listener.handleAuthIntent(intent);
      }
    } else if (e.getClass().equals(GoogleJsonResponseException.class)) {
      GoogleJsonResponseException r = (GoogleJsonResponseException) e;

      e.printStackTrace();

      if (listener != null) {
        listener.handleExceptionMessage("JSON Error: " + r.getDetails().getCode() + " : " + r.getDetails().getMessage());
      }
    } else {
      if (listener != null) {
        listener.handleExceptionMessage("Exception Occurred: " + e.toString());
      }

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

    public VideoListResults(String p, boolean getMaximum) {
      super(getMaximum);

      playlistID = p;
      setItems(itemsForNextToken(""));
    }

    protected List<YouTubeData> itemsForNextToken(String token) {
      List<PlaylistItem> playlistItemList = null;

      if (playlistID != null) {
        try {
          YouTube.PlaylistItems.List listRequest = youTube().playlistItems().list("id, contentDetails, snippet");
          listRequest.setPlaylistId(playlistID);

          listRequest.setFields(String.format("items(contentDetails/videoId, snippet/title, snippet/description, %s), nextPageToken, pageInfo", thumbnailField()));

          listRequest.setPageToken(token);
          listRequest.setMaxResults(getMaxResultsNeeded());
          PlaylistItemListResponse playListResponse = listRequest.execute();

          totalItems = playListResponse.getPageInfo().getTotalResults();

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
        listRequest.setKey(YouTubeAPI.devKey());
        listRequest.setType("video");
        listRequest.setFields(String.format("items(id/videoId, snippet/title, snippet/description, %s), nextPageToken, pageInfo", thumbnailField()));
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        searchListResponse = listRequest.execute();

        totalItems = searchListResponse.getPageInfo().getTotalResults();

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

        listRequest.setKey(YouTubeAPI.devKey());
        listRequest.setFields(String.format("items(id, snippet/title, snippet/description, contentDetails/duration, %s), nextPageToken, pageInfo", thumbnailField()));
        listRequest.setMyRating("like");
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        searchListResponse = listRequest.execute();

        totalItems = searchListResponse.getPageInfo().getTotalResults();

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
        map.mDuration = (String) playlistItem.getContentDetails().get("duration");

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

        listRequest.setKey(YouTubeAPI.devKey());
        listRequest.setRegionCode(regionCode);
        listRequest.setFields("items(snippet/title, snippet/channelId)");

        categoryListResponse = listRequest.execute();

        result.addAll(categoryListResponse.getItems());

        totalItems = result.size();
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

        listRequest.setFields(String.format("items(snippet/title, snippet/resourceId, snippet/description, %s), nextPageToken, pageInfo", thumbnailField()));
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        SubscriptionListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;
        totalItems = subscriptionListResponse.getPageInfo().getTotalResults();

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

        listRequest.setFields(String.format("items(id, snippet/title, snippet/description, %s), nextPageToken, pageInfo", thumbnailField()));
        listRequest.setMaxResults(getMaxResultsNeeded());

        listRequest.setPageToken(token);
        PlaylistListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;
        totalItems = subscriptionListResponse.getPageInfo().getTotalResults();

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
    protected int totalItems;
    private int highestDisplayedIndex = 0;
    private boolean reloadingFlag = false;
    private boolean reachedEndOfList = false;
    private boolean getMaxItems = false;

    // subclasses must implement
    abstract protected List<YouTubeData> itemsForNextToken(String token);

    public BaseListResults() {
      super();
    }

    public BaseListResults(boolean getMaximum) {
      super();

      getMaxItems = getMaximum;
    }

    public List<YouTubeData> getItems() {
      return items;
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
          items.addAll(newItems);

          result = true;
        }
      } else {
        // no more tokens, we are done
        done();
      }

      return result;
    }

    public int getTotalItems() {
      return totalItems;
    }

    public int getHighestDisplayedIndex() {
      return highestDisplayedIndex;
    }

    public void updateHighestDisplayedIndex(int index) {
      if (index > highestDisplayedIndex) {
        highestDisplayedIndex = index + 4; // load a few more
      }
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
          listener.handleExceptionMessage("nextToken bug!");
        }
      }

      return result;
    }

    public boolean needsToLoadMoreItems() {
      if (items != null && !reachedEndOfList) {
        return highestDisplayedIndex >= items.size();
      }

      return false;
    }

    public void setIsReloading(boolean set) {
      reloadingFlag = set;
    }

    public boolean isReloading() {
      return reloadingFlag;
    }

    protected long getMaxResultsNeeded() {
      long result = 5;

      if (getMaxItems)
        result = 50;
      else {
        if (needsToLoadMoreItems()) {
          result = highestDisplayedIndex - (items.size() - 1);
        }
      }

      // avoid exception with setMaxResults: Values must be within the range: [0, 50]
      return Math.min(50, result);
    }

    private void done() {
      // set a flag to stop trying to get more data.  the totalItems is not accurrate.  We don't get results for deleted videos.
      if (items != null)
        totalItems = items.size();
      reachedEndOfList = true;
    }

    protected void handleResultsException(Exception e) {
      // must call done otherwise we get an endless loop as it continues to retry since it thinks there are still items to fetch
      done();
      handleException(e);
    }

  }

}
