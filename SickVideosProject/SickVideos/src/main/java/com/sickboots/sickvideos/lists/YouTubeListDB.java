package com.sickboots.sickvideos.lists;

import android.os.AsyncTask;

import com.sickboots.sickvideos.database.BaseDatabase;
import com.sickboots.sickvideos.database.PlaylistDatabase;
import com.sickboots.sickvideos.database.VideoDatabase;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.List;

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
        boolean showHiddenVideos = ApplicationHub.preferences().getBoolean(PreferenceCache.SHOW_HIDDEN_VIDEOS, false);

        runningTask = new YouTubeListDBTask(showHiddenVideos);
        runningTask.execute(helper);
      }
    }
  }

  @Override
  public void updateItem(YouTubeData itemMap) {
    database.updateItem(itemMap);

    // reload the data so the UI updates
    loadData(false);
  }

  private class YouTubeListDBTask extends AsyncTask<YouTubeAPI, Void, List<YouTubeData>> {
    boolean mShowHidden = false;

    public YouTubeListDBTask(boolean showHidden) {
      super();

      mShowHidden = showHidden;
    }

    protected List<YouTubeData> doInBackground(YouTubeAPI... params) {
      YouTubeAPI helper = params[0];
      List<YouTubeData> result = null;

      Util.log("YouTubeListDBTask: started");

      // filter out hidden values
      if (!mShowHidden)
        database.setFlags(VideoDatabase.FILTER_HIDDEN_ITEMS);

      // are the results already in the DB?
      result = database.getItems();

      // needs improvement
      // might just be all hidden items, no use to refetch from youtube

      if (result.size() == 0) {
        fillDatabaseWithData(helper);

        // now get from the database so we are always using values from the db
        // hidden flag for example is null in the raw data from youtube api which crashed
        // this is slower, but not sure if slow enough warrant a more bug prone fix.
        result = database.getItems();
      }

      return result;
    }

    private void fillDatabaseWithData(YouTubeAPI helper) {
      List<YouTubeData> result = null;

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

    protected void onPostExecute(List<YouTubeData> result) {

      Util.log("YouTubeListDBTask: finished");

      items = result;
      access.onListResults();

      runningTask = null;
    }
  }

}
