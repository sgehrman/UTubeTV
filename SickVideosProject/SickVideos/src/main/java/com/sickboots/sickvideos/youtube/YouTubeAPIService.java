package com.sickboots.sickvideos.youtube;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.sickboots.sickvideos.YouTubeGridFragment;
import com.sickboots.sickvideos.database.BaseDatabase;
import com.sickboots.sickvideos.database.PlaylistDatabase;
import com.sickboots.sickvideos.database.VideoDatabase;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sgehrman on 12/6/13.
 */
public class YouTubeAPIService extends IntentService {
  private BaseDatabase database;

  public YouTubeAPIService() {
    super("YouTubeAPIService");
  }

  public static void startRequest(Context context, YouTubeServiceRequest request) {
    Intent i = new Intent(context, YouTubeAPIService.class);
    i.putExtra("request", request);
    context.startService(i);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      YouTubeServiceRequest request = intent.getParcelableExtra("request");

      YouTubeAPI helper = new YouTubeAPI(this);
      List<YouTubeData> result = getDataFromInternet(request, helper);

      Util.toast(this, "got some shit: " + result.size());


      if (result != null) {
        database = getDatabase(request);

        Set currentListSavedData = saveExistingListState();

        result = prepareDataFromNet(result, currentListSavedData);

        // we are only deleting if we know we got good data
        // otherwise if we delete first a network failure would just make the app useless
        database.deleteAllRows();

        database.insertItems(result);
      }

      Intent messageIntent = new Intent(YouTubeGridFragment.DATA_READY_INTENT);
      messageIntent.putExtra(YouTubeGridFragment.DATA_READY_INTENT_PARAM, "sending this over");

      LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
      manager.sendBroadcast(messageIntent);

      Util.log(String.format("Sent broadcast %s", YouTubeGridFragment.DATA_READY_INTENT));

    } catch (Exception e) {
    }
  }

  private List<YouTubeData> prepareDataFromNet(List<YouTubeData> inList, Set<String> currentListSavedData) {
    if (currentListSavedData != null && currentListSavedData.size() > 0) {
      for (YouTubeData data : inList) {
        if (data.mVideo != null) {
          if (currentListSavedData.contains(data.mVideo))
            data.setHidden(true);
        }
      }
    }

    return inList;
  }

  private BaseDatabase getDatabase(YouTubeServiceRequest request) {
    BaseDatabase result = null;

    BaseDatabase.DatabaseTable table=null;
    switch (request.type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        table = new VideoDatabase();
        break;
      case PLAYLISTS:
        table = new PlaylistDatabase();
        break;
      case CATEGORIES:
        break;
    }

    if (table != null)
      result = new BaseDatabase(this, request.databaseName(), table);

    return result;
  }

  private Set<String> saveExistingListState() {
    Set<String> result = null;

    // ask the database for the hidden items
    // they won't be in "items" since that is what's in the UI, not what's in the db and it won't include hidden items
    List<YouTubeData> hiddenItems = database.getItems(VideoDatabase.ONLY_HIDDEN_ITEMS);

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

  private List<YouTubeData> getDataFromInternet(YouTubeServiceRequest request, YouTubeAPI helper) {
    List<YouTubeData> result = null;
    String playlistID;

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
      while (listResults.getNext()) {
        // getting all
      }

      result = listResults.getItems();
    }

    return result;
  }

}