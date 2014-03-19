package com.distantfuture.videos.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.distantfuture.videos.activities.AuthActivity;
import com.distantfuture.videos.database.DatabaseAccess;
import com.distantfuture.videos.database.DatabaseTables;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.BusEvents;
import com.distantfuture.videos.misc.DUtils;
import com.distantfuture.videos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

public class YouTubeService extends IntentService {
  private Set mHasFetchedDataMap = new HashSet<String>();

  public YouTubeService() {
    super("YouTubeListService");
  }

  public static void startRequest(Context context, ListServiceRequest request, boolean refresh) {
    context = context.getApplicationContext();

    Intent i = new Intent(context, YouTubeService.class);
    i.putExtra("request", request);
    i.putExtra("refresh", refresh);
    context.startService(i);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      ListServiceRequest request = intent.getParcelableExtra("request");
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
        final ListServiceRequest currentRequest = request;
        YouTubeAPI helper = new YouTubeAPI(this, new YouTubeAPI.YouTubeAPIListener() {
          @Override
          public void handleAuthIntent(final Intent authIntent) {
            AuthActivity.show(YouTubeService.this, authIntent, currentRequest);
          }
        });

        updateDataFromInternet(request, helper);
      }

      // notify that we handled an intent so pull to refresh can stop it's animation and other stuff
      EventBus.getDefault().post(new BusEvents.YouTubeFragmentDataReady());

    } catch (Exception e) {
      e.printStackTrace();
      DUtils.log(String.format("%s exception: %s", DUtils.currentMethod(), e.getMessage()));
    }
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

  private void updateDataFromInternet(ListServiceRequest request, YouTubeAPI helper) {
    String playlistID;
    boolean removeAllFromDB = true;
    List<YouTubeData> resultList = null;

    // do we have internet access?
    if (!AppUtils.instance(this).hasNetworkConnection()) {
      DUtils.log("No internet connection.  Method: " + DUtils.currentMethod());
      return;
    }

    YouTubeAPI.BaseListResults listResults = null;

    switch (request.type()) {
      case RELATED:
        YouTubeAPI.RelatedPlaylistType type = request.relatedType();
        String channelID = (String) request.channel();

        playlistID = helper.relatedPlaylistID(type, channelID);

        if (playlistID != null) // probably needed authorization and failed
          resultList = retrieveVideoList(request, helper, playlistID, null, request.maxResults());

        removeAllFromDB = false;
        break;
      case VIDEOS:
        playlistID = (String) request.playlist();

        // can't use request.maxResults() since we have to get everything and sort it
        resultList = retrieveVideoList(request, helper, playlistID, null, 0);
        removeAllFromDB = false;
        break;
      case SEARCH:
        String query = (String) request.query();
        listResults = helper.searchListResults(query, false);
        break;
      case LIKED:
        listResults = helper.likedVideosListResults();
        break;
      case PLAYLISTS:
        String channel = (String) request.channel();

        resultList = retrieveVideoList(request, helper, null, channel, request.maxResults());
        removeAllFromDB = false;
        break;
      case SUBSCRIPTIONS:
        listResults = helper.subscriptionListResults(false);
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

  private List<YouTubeData> retrieveVideoList(ListServiceRequest request, YouTubeAPI helper, String playlistID, String channelID, int maxResults) {
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

  private List<String> removeVideosWeAlreadyHave(ListServiceRequest request, List<String> newVideoIds) {
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
      DUtils.log("removed: " + (newVideoIds.size() - result.size()));
      DUtils.log("returning: " + result.size());
    }

    return result;
  }

}