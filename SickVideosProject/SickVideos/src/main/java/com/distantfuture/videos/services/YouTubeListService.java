package com.distantfuture.videos.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

import com.distantfuture.videos.activities.AuthActivity;
import com.distantfuture.videos.database.DatabaseAccess;
import com.distantfuture.videos.database.DatabaseTables;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YouTubeListService extends IntentService {
  public static final String DATA_READY_INTENT = "com.sickboots.sickvideos.DataReady";
  private Set mHasFetchedDataMap = new HashSet<String>();

  public YouTubeListService() {
    super("YouTubeListService");
  }

  public static void startRequest(Context context, YouTubeServiceRequest request, boolean refresh) {
    context = context.getApplicationContext();

    Intent i = new Intent(context, YouTubeListService.class);
    i.putExtra("request", request);
    i.putExtra("refresh", refresh);
    context.startService(i);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      YouTubeServiceRequest request = intent.getParcelableExtra("request");
      boolean refresh = intent.getBooleanExtra("refresh", false);

      boolean hasFetchedData = mHasFetchedDataMap.contains(request.requestIdentifier());
      mHasFetchedDataMap.add(request.requestIdentifier());

      if (!refresh) {
        if (!hasFetchedData) {
          DatabaseAccess access = new DatabaseAccess(this, request.databaseTable());

          Cursor cursor = access.getCursor(DatabaseTables.ALL_ITEMS, request.requestIdentifier());
          if (!cursor.moveToFirst())
            refresh = true;

          cursor.close();
        }
      }

      if (refresh) {
        final YouTubeServiceRequest currentRequest = request;
        YouTubeAPI helper = new YouTubeAPI(this, new YouTubeAPI.YouTubeAPIListener() {
          @Override
          public void handleAuthIntent(final Intent authIntent) {

            Intent intent = new Intent(YouTubeListService.this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // need this to start activity from service

            if (authIntent != null)
              intent.putExtra(AuthActivity.REQUEST_AUTHORIZATION_INTENT_PARAM, authIntent);
            intent.putExtra(AuthActivity.REQUEST_AUTHORIZATION_REQUEST_PARAM, currentRequest);

            YouTubeListService.this.startActivity(intent);

          }
        });

        updateDataFromInternet(request, helper);
      }

      // notify that we handled an intent so pull to refresh can stop it's animation and other stuff
      sendServiceDoneBroadcast();

    } catch (Exception e) {
      e.printStackTrace();
      Debug.log(String.format("%s exception: %s", Debug.currentMethod(), e.getMessage()));
    }
  }

  private void sendServiceDoneBroadcast() {
    Intent messageIntent = new Intent(DATA_READY_INTENT);

    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
    manager.sendBroadcast(messageIntent);
  }

  private List<YouTubeData> prepareDataFromNet(List<YouTubeData> inList, Set<String> currentListSavedData, String requestID) {
    for (YouTubeData data : inList) {
      // set the request id
      data.mRequest = requestID;

      if (currentListSavedData != null && currentListSavedData.size() > 0) {
        String videoOrPl = data.mVideo == null ? data.mPlaylist : data.mVideo;

        if (videoOrPl != null) {
          if (currentListSavedData.contains(videoOrPl)) {
            currentListSavedData.remove(videoOrPl); // faster?
            data.setHidden(true);
          }
        }
      }
    }

    return inList;
  }

  private Set<String> saveExistingListState(DatabaseAccess database, String requestIdentifier) {
    Set<String> result = null;

    // ask the database for the hidden items
    List<YouTubeData> hiddenItems = database.getItems(DatabaseTables.HIDDEN_ITEMS, requestIdentifier, 0);

    if (hiddenItems != null) {
      result = new HashSet<String>();

      for (YouTubeData data : hiddenItems) {
        String videoOrPl = data.mVideo == null ? data.mPlaylist : data.mVideo;

        if (videoOrPl != null) {
          result.add(videoOrPl);
        }
      }
    }

    return result;
  }

  private void updateDataFromInternet(YouTubeServiceRequest request, YouTubeAPI helper) {
    String playlistID;
    boolean removeAllFromDB = true;
    List<YouTubeData> resultList = null;

    // do we have internet access?
    if (!AppUtils.instance(this).hasNetworkConnection()) {
      Debug.log("No internet connection.  Method: " + Debug.currentMethod());
      return;
    }

    Debug.log("getting list from net...");

    YouTubeAPI.BaseListResults listResults = null;

    switch (request.type()) {
      case RELATED:
        YouTubeAPI.RelatedPlaylistType type = (YouTubeAPI.RelatedPlaylistType) request.getData("type");
        String channelID = (String) request.getData("channel");

        playlistID = helper.relatedPlaylistID(type, channelID);

        if (playlistID != null) // probably needed authorization and failed
          resultList = retrieveVideoList(request, helper, playlistID, null, request.maxResults());

        removeAllFromDB = false;
        break;
      case VIDEOS:
        playlistID = (String) request.getData("playlist");

        // can't use request.maxResults() since we have to get everything and sort it
        resultList = retrieveVideoList(request, helper, playlistID, null, 0);
        removeAllFromDB = false;
        break;
      case SEARCH:
        String query = (String) request.getData("query");
        listResults = helper.searchListResults(query, false);
        break;
      case LIKED:
        listResults = helper.likedVideosListResults();
        break;
      case PLAYLISTS:
        String channel = (String) request.getData("channel");

        resultList = retrieveVideoList(request, helper, null, channel, request.maxResults());
        removeAllFromDB = false;
        break;
      case SUBSCRIPTIONS:
        listResults = helper.subscriptionListResults();
        break;
      case CATEGORIES:
        listResults = helper.categoriesListResults("US");
        break;
    }

    if (resultList == null) {
      if (listResults != null) {
        resultList = listResults.getAllItems(request.maxResults());
      }
    }

    if (resultList != null) {
      DatabaseAccess database = new DatabaseAccess(this, request.databaseTable());

      Set currentListSavedData = saveExistingListState(database, request.requestIdentifier());

      resultList = prepareDataFromNet(resultList, currentListSavedData, request.requestIdentifier());

      if (removeAllFromDB)
        database.deleteAllRows(request.requestIdentifier());
      database.insertItems(resultList);
    }
  }

  private List<YouTubeData> retrieveVideoList(YouTubeServiceRequest request, YouTubeAPI helper, String playlistID, String channelID, int maxResults) {
    List<YouTubeData> result = new ArrayList<YouTubeData>();
    YouTubeAPI.BaseListResults videoResults;

    if (playlistID != null)
      videoResults = helper.videosFromPlaylistResults(playlistID);
    else
      videoResults = helper.channelPlaylistsResults(channelID, false);

    if (videoResults != null) {
      List<YouTubeData> videoData = videoResults.getAllItems(maxResults);

      // extract just the video ids from list
      List<String> videoIds = YouTubeData.contentIdsList(videoData);

      // remove videos that we already have...
      videoIds = removeVideosWeAlreadyHave(request, videoIds);

      final int limit = YouTubeAPI.youTubeMaxResultsLimit();

      for (int n = 0; n < videoIds.size(); n += limit) {
        int chunkSize = Math.min(videoIds.size(), n + limit);
        List<String> chunk = videoIds.subList(n, chunkSize);

        if (playlistID != null)
          videoResults = helper.videoInfoListResults(chunk);
        else
          videoResults = helper.playlistInfoListResults(chunk);

        result.addAll(videoResults.getItems(0));
      }
    }

    return result;
  }

  private List<String> removeVideosWeAlreadyHave(YouTubeServiceRequest request, List<String> newVideoIds) {
    List<String> result = newVideoIds;  // return same list if not modified

    DatabaseAccess database = new DatabaseAccess(this, request.databaseTable());
    List<YouTubeData> existingItems = database.getItems(DatabaseTables.CONTENT_ONLY, request.requestIdentifier(), 0);

    if (existingItems != null) {
      Set existingIds = new HashSet<String>(existingItems.size());

      for (YouTubeData data : existingItems) {
        String videoOrPl = data.mVideo == null ? data.mPlaylist : data.mVideo;

        if (videoOrPl != null) {
          existingIds.add(videoOrPl);
        }
      }

      if (existingIds.size() > 0) {
        result = new ArrayList<String>(newVideoIds.size());

        for (String videoId : newVideoIds) {
          if (!existingIds.contains(videoId)) {
            result.add(videoId);
          }
        }
      }
    }

    boolean debugging = false;
    if (debugging) {
      Debug.log("removed: " + (newVideoIds.size() - result.size()));
      Debug.log("returning: " + result.size());
    }

    return result;
  }

}