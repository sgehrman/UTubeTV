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
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static container class for holding a reference to your YouTube Developer Key.
 */
public class YouTubeHelper {
  public enum RelatedPlaylistType {FAVORITES, LIKES, UPLOADS, WATCHED, WATCHLATER}

  public static final int REQ_PLAYER_CODE = 334443;
  private YouTubeHelperListener listener;
  private HttpRequestInitializer credential;
  private YouTube youTube;

  // must implement this listener
  public interface YouTubeHelperListener {
    public void handleAuthIntent(Intent authIntent);

    public void handleExceptionMessage(String message);
  }

  public YouTubeHelper(HttpRequestInitializer c, YouTubeHelperListener l) {
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

    Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, YouTubeHelper.devKey(), movieID, 0, true, !fullScreen);
    activity.startActivityForResult(intent, REQ_PLAYER_CODE);
  }

  public YouTube youTube() {
    if (youTube == null) {
      try {
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("YouTubeHelper").build();
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

  private void handleException(Exception e) {
    if (e.getClass().equals(UserRecoverableAuthIOException.class)) {
      UserRecoverableAuthIOException r = (UserRecoverableAuthIOException) e;

      if (listener != null) {
        listener.handleAuthIntent(r.getIntent());
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

  // pass null for channelid to get our own channel
  public String relatedPlaylistID(RelatedPlaylistType type, String channelID) {
    Map<RelatedPlaylistType, String> playlistMap = relatedPlaylistIDs(channelID);

    return playlistMap.get(type);
  }

  // pass null for channelid to get our own channel
  public Map<RelatedPlaylistType, String> relatedPlaylistIDs(String channelID) {
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

  public PlayListResults playListResults(RelatedPlaylistType type, String channelID) {
    PlayListResults result = new PlayListResults(type, channelID);

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

  public Map playlistItemsForID(String playlistID, String nextToken) {
    HashMap result = new HashMap();

    if (playlistID != null) {
      try {
        YouTube.PlaylistItems.List playlistItemRequest = youTube().playlistItems().list("id, contentDetails, snippet");
        playlistItemRequest.setPlaylistId(playlistID);

        playlistItemRequest.setFields("items(contentDetails/videoId, snippet/title, snippet/thumbnails/default/url), nextPageToken, pageInfo");

        playlistItemRequest.setPageToken(nextToken);
        PlaylistItemListResponse response = playlistItemRequest.execute();

        int totalResults = response.getPageInfo().getTotalResults();

        result.put("items", response.getItems());
        result.put("total", totalResults);
        result.put("response", response);

      } catch (UserRecoverableAuthIOException e) {
        handleException(e);
      } catch (Exception e) {
        handleException(e);
      }
    }

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
  // PlayListResults

  public class PlayListResults extends BaseListResults {
    private String playlistID;

    public PlayListResults(YouTubeHelper.RelatedPlaylistType type, String channel) {
      super();

      playlistID = relatedPlaylistID(type, channel);
      items = itemsForNextToken("");
    }

    protected List<Map> itemsForNextToken(String token) {
      Map resultMap = playlistItemsForID(playlistID, token);

      List<PlaylistItem> playlistItemList = (List<PlaylistItem>) resultMap.get("items");
      response = resultMap.get("response");
      totalItem = ((Integer) resultMap.get("total")).intValue();

      return playlistItemsToMap(playlistItemList);
    }

    private List<Map> playlistItemsToMap(List<PlaylistItem> playlistItemList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (PlaylistItem playlistItem : playlistItemList) {
        HashMap map = new HashMap();

        map.put("video", playlistItem.getContentDetails().getVideoId());
        map.put("title", playlistItem.getSnippet().getTitle());
        map.put("thumbnail", thumbnailURL(playlistItem.getSnippet().getThumbnails()));

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
      items = itemsForNextToken("");
    }

    protected List<Map> itemsForNextToken(String token) {
      List<SearchResult> result = new ArrayList<SearchResult>();
      SearchListResponse searchListResponse = null;

      try {
        YouTube.Search.List listRequest = youTube().search().list("id, snippet");

        listRequest.setQ(query);
        listRequest.setKey(YouTubeHelper.devKey());
        listRequest.setType("video");
        listRequest.setFields("items(id/videoId, snippet/title, snippet/thumbnails/default/url), nextPageToken, pageInfo");

        listRequest.setPageToken(token);
        searchListResponse = listRequest.execute();

        totalItem = searchListResponse.getPageInfo().getTotalResults();

        // nasty double cast?
        response = searchListResponse;

        result.addAll(searchListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleException(e);
      } catch (Exception e) {
        handleException(e);
      }

      return searchResultsToMap(result);
    }

    private List<Map> searchResultsToMap(List<SearchResult> playlistItemList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (SearchResult playlistItem : playlistItemList) {
        HashMap map = new HashMap();

        map.put("video", playlistItem.getId().getVideoId());
        map.put("title", playlistItem.getSnippet().getTitle());
        map.put("thumbnail", thumbnailURL(playlistItem.getSnippet().getThumbnails()));

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

      items = itemsForNextToken("");
    }

    protected List<Map> itemsForNextToken(String token) {
      List<Subscription> result = new ArrayList<Subscription>();

      try {
        YouTube.Subscriptions.List listRequest = youTube().subscriptions().list("id, contentDetails, snippet");
        listRequest.setMine(true);

        listRequest.setFields("items(snippet/title, snippet/resourceId, snippet/thumbnails/default/url), nextPageToken, pageInfo");

        listRequest.setPageToken(token);
        SubscriptionListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;
        totalItem = subscriptionListResponse.getPageInfo().getTotalResults();

        result.addAll(subscriptionListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleException(e);
      } catch (Exception e) {
        handleException(e);
      }

      return playlistItemsToMap(result);
    }

    private List<Map> playlistItemsToMap(List<Subscription> subscriptionsList) {
      List<Map> result = new ArrayList<Map>();

      // convert the list into hash maps of video info
      for (Subscription subscription : subscriptionsList) {
        HashMap map = new HashMap();

        map.put("id", subscription.getId());
        map.put("title", subscription.getSnippet().getTitle());
        map.put("channel", subscription.getSnippet().getResourceId().getChannelId());
        map.put("description", subscription.getSnippet().getDescription());
        map.put("thumbnail", thumbnailURL(subscription.getSnippet().getThumbnails()));

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // BaseListResults

  abstract public class BaseListResults {
    protected Object response;
    protected List<Map> items;
    protected int totalItem;

    // subclasses must implement
    abstract protected List<Map> itemsForNextToken(String token);

    public List<Map> getItems() {
      return items;
    }

    public boolean getNext() {
      boolean result = false;

      if (totalItem > items.size()) {
        String token = nextToken();

        if (token != null) {
          List<Map> newItems = itemsForNextToken(token);

          if (newItems != null) {
            items.addAll(newItems);

            result = true;
          }
        }
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
        }
      }

      return result;
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
12) Get lists of playlists from youtube by users
13) Auto import your own youtube playlist
14)


console play

https://developers.google.com/youtube/v3/docs/activities/list
channel id: "UCCXAzufqBhwf_ib6xLv7gMw"
part id: "snippet"
fields: items(snippet/title, snippet/thumbnails/default/url), nextPageToken, pageInfo



https://developers.google.com/youtube/v3/docs/subscriptions/list
channel id: "UCCXAzufqBhwf_ib6xLv7gMw"
items/snippet/title

 */