package com.sickboots.sickvideos.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

import com.sickboots.sickvideos.AuthActivity;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sgehrman on 12/6/13.
 */
public class YouTubeListService extends IntentService {
  public static final String DATA_READY_INTENT = "com.sickboots.sickvideos.DataReady";
  public static final String DATA_READY_INTENT_PARAM = "com.sickboots.sickvideos.DataReady.param";
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
          DatabaseAccess access = new DatabaseAccess(this, request);

          Cursor cursor = access.getCursor(DatabaseTables.ALL_ITEMS);
          if (!cursor.moveToFirst())
            refresh = true;
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

        Intent messageIntent = new Intent(DATA_READY_INTENT);
        messageIntent.putExtra(DATA_READY_INTENT_PARAM, "sending this over");

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(messageIntent);

        // start this up to get duration values
        YouTubeUpdateService.startRequest(this);
      }

    } catch (Exception e) {
    }
  }

  private List<YouTubeData> prepareDataFromNet(List<YouTubeData> inList, Set<String> currentListSavedData, String requestID) {
    for (YouTubeData data : inList) {
      // set the request id
      data.mRequest = requestID;

      if (currentListSavedData != null && currentListSavedData.size() > 0) {
        if (data.mVideo != null) {
          if (currentListSavedData.contains(data.mVideo))
            data.setHidden(true);
        }
      }
    }

    return inList;
  }

  private Set<String> saveExistingListState(DatabaseAccess database) {
    Set<String> result = null;

    // ask the database for the hidden items
    // they won't be in "items" since that is what's in the UI, not what's in the db and it won't include hidden items
    List<YouTubeData> hiddenItems = database.getItems(DatabaseTables.HIDDEN_ITEMS);

    if (hiddenItems != null) {
      result = new HashSet<String>();

      for (YouTubeData data : hiddenItems) {
        if (data.mVideo != null) {
          result.add(data.mVideo);
        }
      }
    }

    return result;
  }

  private void updateDataFromInternet(YouTubeServiceRequest request, YouTubeAPI helper) {
    String playlistID;

    Util.log("getting list from net...");

    YouTubeAPI.BaseListResults listResults = null;

    switch (request.type()) {
      case RELATED:
        YouTubeAPI.RelatedPlaylistType type = (YouTubeAPI.RelatedPlaylistType) request.getData("type");
        String channelID = (String) request.getData("channel");

        playlistID = helper.relatedPlaylistID(type, channelID);

        if (playlistID != null) // probably needed authorization and failed
          listResults = helper.videoListResults(playlistID);
        break;
      case VIDEOS:
        playlistID = (String) request.getData("playlist");

        listResults = helper.videoListResults(playlistID);
        break;
      case SEARCH:
        String query = (String) request.getData("query");
        listResults = helper.searchListResults(query);
        break;
      case LIKED:
        listResults = helper.likedVideosListResults();
        break;
      case PLAYLISTS:
        String channel = (String) request.getData("channel");

        listResults = helper.playlistListResults(channel, false);
        break;
      case SUBSCRIPTIONS:
        listResults = helper.subscriptionListResults();
        break;
      case CATEGORIES:
        listResults = helper.categoriesListResults("US");
        break;
    }

    if (listResults != null) {
      DatabaseAccess database = new DatabaseAccess(this, request);

      Set currentListSavedData = saveExistingListState(database);
      database.deleteAllRows();

      do {
        List<YouTubeData> batch = listResults.getItems();
        batch = prepareDataFromNet(batch, currentListSavedData, request.requestIdentifier());

        Util.log("batch...");

        database.insertItems(batch);
      } while (listResults.getNext());
    }
  }

}