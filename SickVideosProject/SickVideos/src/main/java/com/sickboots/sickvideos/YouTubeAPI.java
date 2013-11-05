package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubeAPI {
  // constants used in Maps for youtube data
  public static final String PLAYLIST_KEY = "playlist";
  public static final String CHANNEL_KEY = "channel";
  public static final String VIDEO_KEY = "video";
  public static final String TITLE_KEY = "title";
  public static final String DESCRIPTION_KEY = "description";
  public static final String THUMBNAIL_KEY = "thumbnail";
  public static final String DURATION_KEY = "duration";

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
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
    boolean fullScreen = sp.getBoolean("play_full_screen", false);

    Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, YouTubeAPI.devKey(), movieID, 0, true, !fullScreen);
    activity.startActivityForResult(intent, REQ_PLAYER_CODE);
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

  public String channelID() {
    String result = null;

    try {
      YouTube.Channels.List channelRequest = youTube().channels().list("id");
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
        listener.handleExceptionMessage("Exception Occurred");
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

    protected List<Map> itemsForNextToken(String token) {
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

    private List<Map> playlistItemsToMap(List<PlaylistItem> playlistItemList) {
      // check parameters
      if (playlistItemList == null) return null;

      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (PlaylistItem playlistItem : playlistItemList) {
        HashMap map = new HashMap();

        map.put(VIDEO_KEY, playlistItem.getContentDetails().getVideoId());
        map.put(TITLE_KEY, playlistItem.getSnippet().getTitle());
        map.put(DESCRIPTION_KEY, playlistItem.getSnippet().getDescription());
        map.put(THUMBNAIL_KEY, thumbnailURL(playlistItem.getSnippet().getThumbnails()));

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

    protected List<Map> itemsForNextToken(String token) {
      List<SearchResult> result = new ArrayList<SearchResult>();
      SearchListResponse searchListResponse = null;

      try {
        YouTube.Search.List listRequest = youTube().search().list("id, snippet");

        listRequest.setQ(query);
        listRequest.setKey(YouTubeAPI.devKey());
        listRequest.setType(VIDEO_KEY);
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

    private List<Map> searchResultsToMap(List<SearchResult> playlistItemList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (SearchResult playlistItem : playlistItemList) {
        HashMap map = new HashMap();

        map.put(VIDEO_KEY, playlistItem.getId().getVideoId());
        map.put(TITLE_KEY, playlistItem.getSnippet().getTitle());
        map.put(DESCRIPTION_KEY, playlistItem.getSnippet().getDescription());
        map.put(THUMBNAIL_KEY, thumbnailURL(playlistItem.getSnippet().getThumbnails()));

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

    protected List<Map> itemsForNextToken(String token) {
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

    private List<Map> searchResultsToMap(List<Video> playlistItemList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (Video playlistItem : playlistItemList) {
        HashMap map = new HashMap();

        map.put(VIDEO_KEY, playlistItem.getId());
        map.put(TITLE_KEY, playlistItem.getSnippet().getTitle());
        map.put(DESCRIPTION_KEY, playlistItem.getSnippet().getDescription());
        map.put(THUMBNAIL_KEY, thumbnailURL(playlistItem.getSnippet().getThumbnails()));
        map.put(DURATION_KEY, playlistItem.getContentDetails().get("duration"));

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

    protected List<Map> itemsForNextToken(String regionCode) {
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

    private List<Map> searchResultsToMap(List<VideoCategory> itemList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (VideoCategory category : itemList) {
        HashMap map = new HashMap();

        map.put(CHANNEL_KEY, category.getSnippet().getChannelId());
        map.put(TITLE_KEY, category.getSnippet().getTitle());

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

    protected List<Map> itemsForNextToken(String token) {
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

    private List<Map> subscriptionListToMap(List<Subscription> subscriptionsList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (Subscription subscription : subscriptionsList) {
        HashMap map = new HashMap();

        map.put(TITLE_KEY, subscription.getSnippet().getTitle());
        map.put(CHANNEL_KEY, subscription.getSnippet().getResourceId().getChannelId());
        map.put(DESCRIPTION_KEY, subscription.getSnippet().getDescription());
        map.put(THUMBNAIL_KEY, thumbnailURL(subscription.getSnippet().getThumbnails()));

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

      List<Map> related = new ArrayList<Map>();
      if (addRelated) {
        Map<RelatedPlaylistType, String> playlistMap = relatedPlaylistIDs(c);

        for (Map.Entry<RelatedPlaylistType, String> entry : playlistMap.entrySet()) {
          String playlistID = entry.getValue();

          if (playlistID != null) {
            HashMap map = new HashMap();

            map.put(PLAYLIST_KEY, playlistID);

            switch (entry.getKey()) {
              case FAVORITES:
                map.put(TITLE_KEY, "Favorites");
                break;
              case LIKES:
                map.put(TITLE_KEY, "LIKES");
                break;
              case UPLOADS:
                map.put(TITLE_KEY, "UPLOADS");
                break;
              case WATCHED:
                map.put(TITLE_KEY, "WATCHED");
                break;
              case WATCHLATER:
                map.put(TITLE_KEY, "WATCHLATER");
                break;
            }

            related.add(map);
          }
        }
      }

      related.addAll(itemsForNextToken(""));

      setItems(related);
    }

    protected List<Map> itemsForNextToken(String token) {
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

    private List<Map> playlistItemsToMap(List<Playlist> subscriptionsList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (Playlist subscription : subscriptionsList) {
        HashMap map = new HashMap();

        map.put(PLAYLIST_KEY, subscription.getId());
        map.put(TITLE_KEY, subscription.getSnippet().getTitle());
        map.put(DESCRIPTION_KEY, subscription.getSnippet().getDescription());
        map.put(THUMBNAIL_KEY, thumbnailURL(subscription.getSnippet().getThumbnails()));

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // BaseListResults

  abstract public class BaseListResults {
    protected Object response;
    private List<Map> items;
    protected int totalItems;
    private int highestDisplayedIndex = 0;
    private boolean reloadingFlag = false;
    private boolean reachedEndOfList = false;
    private boolean getMaxItems = false;

    // subclasses must implement
    abstract protected List<Map> itemsForNextToken(String token);

    public BaseListResults() {
      super();
    }

    public BaseListResults(boolean getMaximum) {
      super();

      getMaxItems = getMaximum;
    }

    public List<Map> getItems() {
      return items;
    }

    public void setItems(List<Map> l) {
      items = l;
    }

    public boolean getNext() {
      boolean result = false;

      String token = nextToken();
      if (token != null) {
        List<Map> newItems = itemsForNextToken(token);

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
        } else {
          Util.log("nextToken not handed, this is a bug");
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

/*
  public static final String TEST_MOVIE_ID = "Il1IGKaol_M";  // XuBdf9jYj7o
  public static final String TEST_PLAYLIST_ID = "FLCXAzufqBhwf_ib6xLv7gMw";
  public static final String DEV_PLAYLIST_ID = "PLhBgTdAWkxeBX09BokINT1ICC5IZ4C0ju";

channel id = UCtVd0c0tGXuTSbU5d8cSBUg

App goals:

1) Simply watch a playlist of movies.
2) Mark which have been watched.
3) Remember where paused or stopped and resume next time
4) Display Full Title and credits below in a nice way
5) Should feel easy to watch a bit, relaunch and it takes you exactly back
6) Channel management (playlists)
7) Send playlists to friends.  Email or SMS or tap etc.
8) Educational playlists
2) ability to repeat vids
9) record favorite moments (bookmarks) in video with single click, remember list of points to be rewatched later
10) What youtube should be, but simplified for watching only.  No comments or sharing etc, just watching experience
11) Killer features
    1) always restores where you were last time you watched
    2) speed, cached lists
    3) share with friends
12) Get lists of playlists from youtube by users
13) Auto import your own youtube playlist
14) stationary header, page numbers and floating overlay showing when scrolling

console play

https://developers.google.com/youtube/v3/docs/activities/list
channel id: "UCCXAzufqBhwf_ib6xLv7gMw"
part id: "snippet"
fields: items(snippet/title, snippet/thumbnails/high/url), nextPageToken, pageInfo

https://developers.google.com/youtube/v3/docs/subscriptions/list
channel id: "UCCXAzufqBhwf_ib6xLv7gMw"
items/snippet/title

 */