package com.sickboots.sickvideos.lists;

import android.os.AsyncTask;

import com.sickboots.sickvideos.database.BaseDatabase;
import com.sickboots.sickvideos.database.PlaylistDatabase;
import com.sickboots.sickvideos.database.VideoDatabase;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.List;
import java.util.Map;

public class YouTubeListDB extends YouTubeList {
  YouTubeListDBTask runningTask = null;
  BaseDatabase database;

  public YouTubeListDB(YouTubeListSpec s, UIAccess a) {
    super(s, a);

    switch (s.type) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        database = new VideoDatabase(getActivity(), s.databaseName());

        break;
      case PLAYLISTS:
        database = new PlaylistDatabase(getActivity(), s.databaseName());
        break;
    }

    loadData(true);
  }

  @Override
  public void updateHighestDisplayedIndex(int position) {
    // doi nothing - not used in DB list
  }

  @Override
  public void refresh() {
    // does this need to go in a task?
    database.deleteAllRows();

    loadData(false);
  }

  @Override
  protected void loadData(boolean askUser) {
    YouTubeAPI helper = youTubeHelper(askUser);

    if (helper != null) {
      if (runningTask == null) {
        runningTask = new YouTubeListDBTask();
        runningTask.execute(helper);
      }
    }
  }

  @Override
  public void handleClick(Map itemMap, boolean clickedIcon) {
    switch (type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        String movieID = (String) itemMap.get("video");

        if (movieID != null) {
          YouTubeAPI.playMovie(getActivity(), movieID);
        }
        break;
      case PLAYLISTS:

        break;
      case SUBSCRIPTIONS:

        break;
      case CATEGORIES:
        break;
    }
  }

  @Override
  public void updateItem(Map itemMap) {
    database.updateItem(itemMap);
  }

  private class YouTubeListDBTask extends AsyncTask<YouTubeAPI, Void, List<Map>> {
    protected List<Map> doInBackground(YouTubeAPI... params) {
      YouTubeAPI helper = params[0];
      List<Map> result = null;

      Util.log("YouTubeListDBTask: started");

      // are the results already in the DB?
      result = database.getItems();

      if (result.size() == 0) {
        YouTubeAPI.BaseListResults listResults = null;

        switch (type()) {
          case RELATED:
            YouTubeAPI.RelatedPlaylistType type = (YouTubeAPI.RelatedPlaylistType) listSpec.getData("type");
            String channelID = (String) listSpec.getData("channel");

            String playlistID = helper.relatedPlaylistID(type, channelID);

            listResults = helper.videoListResults(playlistID, true);
            break;
          case SEARCH:
          case LIKED:
          case VIDEOS:
            break;
          case PLAYLISTS:
            String channel = (String) listSpec.getData("channel");

            listResults = helper.playlistListResults(channel, true);
            break;
          case SUBSCRIPTIONS:
            listResults = helper.subscriptionListResults();
            break;
          case CATEGORIES:
            break;
        }

        if (listResults != null) {
          while (listResults.getNext()) {
            // getting all
          }

          result = listResults.getItems();
        }

        // add results to DB
        database.insertItems(result);
      }

      return result;
    }

    protected void onPostExecute(List<Map> result) {

      Util.log("YouTubeListDBTask: finished");

      items = result;
      access.onListResults();

      runningTask = null;
    }
  }

}
