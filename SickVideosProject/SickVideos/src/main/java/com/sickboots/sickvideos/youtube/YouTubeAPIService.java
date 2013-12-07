package com.sickboots.sickvideos.youtube;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.sickboots.sickvideos.MainActivity;
import com.sickboots.sickvideos.YouTubeGridFragment;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.lists.UIAccess;
import com.sickboots.sickvideos.lists.YouTubeListDB;
import com.sickboots.sickvideos.misc.Util;

import java.util.List;

/**
 * Created by sgehrman on 12/6/13.
 */
public class YouTubeAPIService extends IntentService {

  public static void startRequest(Context context, YouTubeServiceRequest request) {
    Intent i = new Intent(context, YouTubeAPIService.class);
    i.putExtra("request", request);
    context.startService(i);
  }

  public YouTubeAPIService() {
    super("YouTubeAPIService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      YouTubeServiceRequest request = intent.getParcelableExtra("request");

      YouTubeAPI helper = new YouTubeAPI(this);
      List<YouTubeData> data = getDataFromInternet( request, helper);

      Util.toast(this, "got some shit: " + data.size());

      } catch (Exception e) {
    }
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