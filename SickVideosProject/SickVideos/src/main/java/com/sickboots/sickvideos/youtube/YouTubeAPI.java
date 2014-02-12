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
import com.sickboots.sickvideos.misc.Auth;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.Utils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubeAPI {

  public interface YouTubeAPIListener {
    public void handleAuthIntent(final Intent authIntent);
  }

  public enum RelatedPlaylistType {FAVORITES, LIKES, UPLOADS, WATCHED, WATCHLATER}

  public static final int REQ_PLAYER_CODE = 334443;
  private YouTube youTube;
  boolean highQualityImages = true;
  Context mContext;
  YouTubeAPIListener mListener;
  private final int mYouTubeMaxResultsLimit = 50;

  public YouTubeAPI(Context context, YouTubeAPIListener listener) {
    super();

    mListener = listener;
    mContext = context.getApplicationContext();
  }

  public static void playMovie(Activity activity, String movieID, boolean fullScreen) {
    Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, Auth.devKey(), movieID, 0, true, !fullScreen);
    activity.startActivityForResult(intent, REQ_PLAYER_CODE);
  }

  public static void playMovieUsingIntent(Context context, String videoId) {
    Intent intent = YouTubeIntents.createPlayVideoIntent(context, videoId);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // need this to start activity if we just have a plain context (not an Activity)
    context.startActivity(intent);
  }

  public static void openPlaylistUsingIntent(Activity activity, String playlistId) {
    Intent intent = YouTubeIntents.createOpenPlaylistIntent(activity, playlistId);
    activity.startActivity(intent);
  }

  public YouTube youTube() {
    if (youTube == null) {
      try {
        boolean needsAuth = false; // needs to check Content or something to get this for realz
        HttpRequestInitializer credentials;

        if (needsAuth)
          credentials = Auth.getCredentials(mContext);
        else
          credentials = Auth.nullCredentials(mContext);

        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credentials).setApplicationName("YouTubeAPI")
            .build();
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
      channelRequest.setKey(Auth.devKey());

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

  public VideosFromPlaylistResults videosFromPlaylistResults(String playlistID) {
    return new VideosFromPlaylistResults(playlistID);
  }

  public ChannelPlaylistsResults channelPlaylistsResults(String channelID, boolean addRelatedPlaylists) {
    return new ChannelPlaylistsResults(channelID, addRelatedPlaylists);
  }

  public PlaylistInfoListResults playlistInfoListResults(List<String> playlistIds) {
    return new PlaylistInfoListResults(playlistIds);
  }

  public SearchListResults searchListResults(String query) {
    return new SearchListResults(query);
  }

  public SubscriptionListResults subscriptionListResults() {
    return new SubscriptionListResults();
  }

  public CategoriesListResults categoriesListResults(String regionCode) {
    return new CategoriesListResults(regionCode);
  }

  public LikedVideosListResults likedVideosListResults() {
    return new LikedVideosListResults();
  }

  public VideoInfoListResults videoInfoListResults(List<String> videoIds) {
    return new VideoInfoListResults(videoIds);
  }

  // pass null for channelid to get our own channel
  public String relatedPlaylistID(RelatedPlaylistType type, String channelID) {
    Map<RelatedPlaylistType, String> playlistMap = relatedPlaylistIDs(channelID);

    return playlistMap.get(type);
  }

  // pass null for channelid to get our own channel
  public Map<String, YouTubeData> channelInfo(List<String> channelIds) {
    Map<String, YouTubeData> result = new HashMap<String, YouTubeData>();

    try {
      YouTube.Channels.List channelRequest = youTube().channels().list("id, snippet");
      if (channelIds != null) {
        channelRequest.setId(TextUtils.join(",", channelIds));
      } else {
        channelRequest.setMine(true);
      }

      channelRequest.setFields(String.format("items(id, snippet/title, snippet/description, %s)", thumbnailField()));
      channelRequest.setKey(Auth.devKey());
      ChannelListResponse channelResult = channelRequest.execute();

      List<Channel> channelsList = channelResult.getItems();
      if (channelsList != null) {

        for (Channel channel : channelsList) {
          YouTubeData data = new YouTubeData();

          data.mChannel = channel.getId();
          data.mTitle = channel.getSnippet().getTitle();
          data.mDescription = channel.getSnippet().getDescription();
          data.mThumbnail = thumbnailURL(channel.getSnippet().getThumbnails());

          result.put(data.mChannel, data);
        }
      }
    } catch (UserRecoverableAuthIOException e) {
      handleException(e);
    } catch (Exception e) {
      handleException(e);
    }

    return result;
  }

  public String channelIdFromUsername(String userName) {
    String result = null;

    try {
      YouTube.Channels.List channelRequest = youTube().channels().list("id");
      channelRequest.setForUsername(userName);

      channelRequest.setFields("items/id");
      channelRequest.setKey(Auth.devKey());

      ChannelListResponse channelResult = channelRequest.execute();

      List<Channel> channelsList = channelResult.getItems();
      if (channelsList != null) {

        for (Channel channel : channelsList) {
          result = channel.getId();

          break;  // we only want the first item?
        }
      }
    } catch (UserRecoverableAuthIOException e) {
      handleException(e);
    } catch (Exception e) {
      handleException(e);
    }

    return result;
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
      channelRequest.setKey(Auth.devKey());
      ChannelListResponse channelResult = channelRequest.execute();

      List<Channel> channelsList = channelResult.getItems();
      if (channelsList != null) {
        ChannelContentDetails.RelatedPlaylists relatedPlaylists = channelsList.get(0)
            .getContentDetails()
            .getRelatedPlaylists();

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

  public List<YouTubeData> relatedPlaylists(String channelID) {
    List<YouTubeData> related = new ArrayList<YouTubeData>();

    Map<RelatedPlaylistType, String> playlistMap = relatedPlaylistIDs(channelID);

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

    return related;
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

      doHandleExceptionMessage("JSON Error: " + r.getDetails().getCode() + " : " + r.getDetails()
          .getMessage());
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

  public class VideosFromPlaylistResults extends BaseListResults {
    private String mPlaylistID;

    public VideosFromPlaylistResults(String playlistID) {
      super();

      mPlaylistID = playlistID;
      mPart = "contentDetails";
      mFields = "items(contentDetails/videoId), nextPageToken";
    }

    protected List<YouTubeData> itemsForNextToken(String token, long maxResults) {
      List<PlaylistItem> playlistItemList = null;

      if (mPlaylistID != null) {
        try {
          YouTube.PlaylistItems.List listRequest = youTube().playlistItems().list(mPart);
          listRequest.setPlaylistId(mPlaylistID);

          listRequest.setFields(mFields);

          listRequest.setPageToken(token);
          listRequest.setMaxResults(maxResults);
          listRequest.setKey(Auth.devKey());
          PlaylistItemListResponse playListResponse = listRequest.execute();

          playlistItemList = playListResponse.getItems();
          response = playListResponse;

        } catch (UserRecoverableAuthIOException e) {
          handleResultsException(e);
        } catch (Exception e) {
          handleResultsException(e);
        }
      }

      return itemsToMap(playlistItemList);
    }

    private List<YouTubeData> itemsToMap(List<PlaylistItem> playlistItemList) {
      // check parameters
      if (playlistItemList == null)
        return null;

      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (PlaylistItem playlistItem : playlistItemList) {
        YouTubeData map = new YouTubeData();

        map.mVideo = playlistItem.getContentDetails().getVideoId();

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
      mPart = "id, snippet";
      mFields = String.format("items(id/videoId, snippet/title, snippet/description, %s), nextPageToken", thumbnailField());
    }

    protected List<YouTubeData> itemsForNextToken(String token, long maxResults) {
      List<SearchResult> result = new ArrayList<SearchResult>();
      SearchListResponse searchListResponse;

      try {
        YouTube.Search.List listRequest = youTube().search().list(mPart);

        listRequest.setQ(query);
        listRequest.setKey(Auth.devKey());
        listRequest.setType("video");
        listRequest.setFields(mFields);
        listRequest.setMaxResults(maxResults);

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

      return itemsToMap(result);
    }

    private List<YouTubeData> itemsToMap(List<SearchResult> playlistItemList) {
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
      mPart = "id, snippet, contentDetails";
      mFields = String.format("items(id, snippet/title, snippet/description, contentDetails/duration, %s), nextPageToken", thumbnailField());
    }

    protected List<YouTubeData> itemsForNextToken(String token, long maxResults) {
      List<Video> result = new ArrayList<Video>();
      VideoListResponse searchListResponse;

      try {
        YouTube.Videos.List listRequest = youTube().videos().list(mPart);

        listRequest.setKey(Auth.devKey());
        listRequest.setFields(mFields);
        listRequest.setMyRating("like");
        listRequest.setMaxResults(maxResults);

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

      return itemsToMap(result);
    }

    private List<YouTubeData> itemsToMap(List<Video> playlistItemList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Video playlistItem : playlistItemList) {
        YouTubeData map = new YouTubeData();

        map.mVideo = playlistItem.getId();
        map.mTitle = playlistItem.getSnippet().getTitle();
        map.mDescription = removeNewLinesFromString(playlistItem.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(playlistItem.getSnippet().getThumbnails());
        map.mDuration = Utils.durationToDuration((String) playlistItem.getContentDetails()
            .get("duration"));

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

      if (mVideoIds.size() > mYouTubeMaxResultsLimit) {
        Debug.log("VideoInfoListResults can only handle 50 videos at a time.");

        mVideoIds = videoIds.subList(0, mYouTubeMaxResultsLimit);
      }

      mPart = "id, contentDetails, snippet";
      mFields = String.format("items(id, contentDetails/duration, snippet/title, snippet/description, snippet/publishedAt, %s)", thumbnailField());
    }

    protected List<YouTubeData> itemsForNextToken(String tokenNotUsed, long maxResultsNotUsed) {
      List<Video> result = new ArrayList<Video>();
      VideoListResponse searchListResponse;

      try {
        YouTube.Videos.List listRequest = youTube().videos().list(mPart);

        listRequest.setKey(Auth.devKey());
        listRequest.setFields(mFields);
        listRequest.setId(TextUtils.join(",", mVideoIds));

        searchListResponse = listRequest.execute();

        result.addAll(searchListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return itemsToMap(result);
    }

    private List<YouTubeData> itemsToMap(List<Video> playlistItemList) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Video playlistItem : playlistItemList) {
        YouTubeData map = new YouTubeData();

        map.mVideo = playlistItem.getId();
        map.mDuration = Utils.durationToDuration((String) playlistItem.getContentDetails()
            .get("duration"));
        map.mTitle = playlistItem.getSnippet().getTitle();
        map.mDescription = removeNewLinesFromString(playlistItem.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(playlistItem.getSnippet().getThumbnails());
        map.mPublishedDate = playlistItem.getSnippet().getPublishedAt().getValue();

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // CategoriesListResults

  public class CategoriesListResults extends BaseListResults {
    String mRegionCode;

    public CategoriesListResults(String regionCode) {
      mPart = "snippet";
      mFields = "items(snippet/title, snippet/channelId)";
      mRegionCode = regionCode;
    }

    protected List<YouTubeData> itemsForNextToken(String token, long maxResults) {
      List<VideoCategory> result = new ArrayList<VideoCategory>();
      VideoCategoryListResponse categoryListResponse;

      try {
        YouTube.VideoCategories.List listRequest = youTube().videoCategories().list(mPart);

        listRequest.setKey(Auth.devKey());
        listRequest.setRegionCode(mRegionCode);
        listRequest.setFields(mFields);

        categoryListResponse = listRequest.execute();

        result.addAll(categoryListResponse.getItems());

        response = categoryListResponse;
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return itemsToMap(result);
    }

    private List<YouTubeData> itemsToMap(List<VideoCategory> itemList) {
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

      mPart = "snippet";
      mFields = String.format("items(snippet/title, snippet/resourceId, snippet/description, %s), nextPageToken", thumbnailField());
    }

    protected List<YouTubeData> itemsForNextToken(String token, long maxResults) {
      List<Subscription> result = new ArrayList<Subscription>();

      try {
        YouTube.Subscriptions.List listRequest = youTube().subscriptions().list(mPart);
        listRequest.setMine(true);

        listRequest.setFields(mFields);
        listRequest.setMaxResults(maxResults);
        listRequest.setKey(Auth.devKey());

        listRequest.setPageToken(token);
        SubscriptionListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;

        result.addAll(subscriptionListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return itemsToMap(result);
    }

    private List<YouTubeData> itemsToMap(List<Subscription> subscriptionsList) {
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
  // ChannelPlaylistsResults

  public class ChannelPlaylistsResults extends BaseListResults {
    private String mChannelID;

    public ChannelPlaylistsResults(String channelID, boolean addRelated) {
      super();

      mChannelID = channelID;
      mPart = "id";
      mFields = String.format("items(id), nextPageToken");
    }

    protected List<YouTubeData> itemsForNextToken(String token, long maxResults) {
      List<Playlist> result = new ArrayList<Playlist>();

      try {
        YouTube.Playlists.List listRequest = youTube().playlists().list(mPart);

        // if channel null, assume the users channel
        if (mChannelID == null)
          listRequest.setMine(true);
        else
          listRequest.setChannelId(mChannelID);

        listRequest.setFields(mFields);
        listRequest.setMaxResults(maxResults);
        listRequest.setKey(Auth.devKey());

        listRequest.setPageToken(token);
        PlaylistListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;

        result.addAll(subscriptionListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return itemsToMap(result);
    }

    private List<YouTubeData> itemsToMap(List<Playlist> playlists) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Playlist playlist : playlists) {
        YouTubeData map = new YouTubeData();

        map.mPlaylist = playlist.getId();

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // PlaylistInfoListResults

  public class PlaylistInfoListResults extends BaseListResults {
    List<String> mPlaylistIds;

    public PlaylistInfoListResults(List<String> playlistIds) {
      mPlaylistIds = playlistIds;

      if (mPlaylistIds.size() > mYouTubeMaxResultsLimit) {
        Debug.log("VideoInfoListResults can only handle 50 videos at a time.");

        mPlaylistIds = mPlaylistIds.subList(0, mYouTubeMaxResultsLimit);
      }

      mPart = "id, snippet, contentDetails";
      mFields = String.format("items(id, contentDetails/itemCount, snippet/title, snippet/description, snippet/publishedAt, %s)", thumbnailField());
    }

    protected List<YouTubeData> itemsForNextToken(String tokenNotUsed, long maxResultsNotUsed) {
      List<Playlist> result = new ArrayList<Playlist>();

      try {
        YouTube.Playlists.List listRequest = youTube().playlists().list(mPart);

        listRequest.setFields(mFields);
        listRequest.setKey(Auth.devKey());
        listRequest.setId(TextUtils.join(",", mPlaylistIds));

        PlaylistListResponse subscriptionListResponse = listRequest.execute();

        response = subscriptionListResponse;

        result.addAll(subscriptionListResponse.getItems());
      } catch (UserRecoverableAuthIOException e) {
        handleResultsException(e);
      } catch (Exception e) {
        handleResultsException(e);
      }

      return itemsToMap(result);
    }

    private List<YouTubeData> itemsToMap(List<Playlist> playlists) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      // convert the list into hash maps of video info
      for (Playlist playlist : playlists) {
        YouTubeData map = new YouTubeData();

        map.mPlaylist = playlist.getId();
        map.mTitle = playlist.getSnippet().getTitle();
        map.mItemCount = playlist.getContentDetails().getItemCount();
        map.mDescription = removeNewLinesFromString(playlist.getSnippet().getDescription());
        map.mThumbnail = thumbnailURL(playlist.getSnippet().getThumbnails());
        map.mPublishedDate = playlist.getSnippet().getPublishedAt().getValue();

        result.add(map);
      }

      return result;
    }
  }

  // ========================================================
  // BaseListResults

  abstract public class BaseListResults {
    protected Object response;
    protected String mPart;
    protected String mFields;

    // subclasses must implement
    abstract protected List<YouTubeData> itemsForNextToken(String token, long maxResults);

    public BaseListResults() {
      super();
    }

    public List<YouTubeData> getItems(long maxResults) {
      return getNext(maxResults);
    }

    // we need all items at once if we reverse sort, otherwise the top items in the list will jump
    // down as more data is loaded and look annoying.  YouTube API doesn't support sorting, so we must do this crap
    public List<YouTubeData> getAllItems(int maxResults) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      long maxToRequest = maxResults;

      while (true) {
        List<YouTubeData> items = getItems(maxToRequest);

        if (items.size() == 0)
          break;
        else {
          result.addAll(items);

          // break out if we reached the max requested
          if (maxResults != 0) {
            if (result.size() >= maxResults) {
              // could truncate results, but not that concerned about exact size
              break;
            } else {
              maxToRequest = maxResults - result.size();
            }
          }
        }

      }

      return result;
    }

    private List<YouTubeData> getNext(long maxResults) {
      List<YouTubeData> result = new ArrayList<YouTubeData>();

      if (maxResults <= 0 || maxResults > mYouTubeMaxResultsLimit)
        maxResults = mYouTubeMaxResultsLimit;  // youTube limit

      String token = nextToken();
      if (token != null) {
        List<YouTubeData> newItems = itemsForNextToken(token, maxResults);

        if (newItems != null) {

          result.addAll(newItems);
        }
      } else {
        // no more tokens, we are done
        done();
      }

      return result;
    }

    private String nextToken() {
      String result = null;

      if (response == null)
        result = ""; // first time
      else {
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
