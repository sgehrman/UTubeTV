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
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YouTubeListDB extends YouTubeList {
  YouTubeListDBTask runningTask = null;
  BaseDatabase database;

  public YouTubeListDB(YouTubeServiceRequest s, UIAccess a) {
    super(s, a);

    switch (s.type) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        database = new VideoDatabase(access.getContext(), s.databaseName());

        break;
      case PLAYLISTS:
        database = new PlaylistDatabase(access.getContext(), s.databaseName());
        break;
      case CATEGORIES:
        break;
    }

    loadData(TaskType.FIRSTLOAD);
  }

  @Override
  public void refresh() {
    loadData(TaskType.USER_REFRESH);
  }

  @Override
  protected void loadData(TaskType taskType) {
    YouTubeAPI helper = youTubeHelper();

    if (helper != null) {
      if (runningTask == null) {
        boolean showHiddenVideos = ApplicationHub.preferences(access.getContext()).getBoolean(PreferenceCache.SHOW_HIDDEN_VIDEOS, false);

        runningTask = new YouTubeListDBTask(taskType, showHiddenVideos);
        runningTask.execute(helper);
      }
    }
  }

  @Override
  public void updateItem(YouTubeData itemMap) {
    database.updateItem(itemMap);

    // reload the data so the UI updates
    loadData(TaskType.REFETCH);
  }

  private class YouTubeListDBTask extends AsyncTask<YouTubeAPI, Void, List<YouTubeData>> {
    boolean mFilterHidden = false;
    TaskType mTaskType;

    public YouTubeListDBTask(TaskType taskType, boolean showHiddenVideos) {
      super();

      mFilterHidden = !showHiddenVideos;
      mTaskType = taskType;
    }

    protected List<YouTubeData> doInBackground(YouTubeAPI... params) {
      YouTubeAPI helper = params[0];
      List<YouTubeData> result = null;

      Util.log("YouTubeListDBTask: started");

      switch (mTaskType) {
        case USER_REFRESH:
          Set currentListSavedData = saveExistingListState();
          result = loadFreshDataToDatabase(helper, currentListSavedData);

          break;
        case REFETCH:
          //  item hidden, or hidden visible pref toggled
          result = database.getItems(mFilterHidden ? VideoDatabase.FILTER_HIDDEN_ITEMS : 0);
          break;
        case FIRSTLOAD:
          result = database.getItems(mFilterHidden ? VideoDatabase.FILTER_HIDDEN_ITEMS : 0);

          // this is lame, fix later
          if (result.size() == 0) {
            result = loadFreshDataToDatabase(helper, null);
          }

          break;
      }

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

    private List<YouTubeData> loadFreshDataToDatabase(YouTubeAPI helper, Set<String> currentListSavedData) {
      List<YouTubeData> result = getDataFromInternet(helper);

      if (result != null) {
        result = prepareDataFromNet(result, currentListSavedData);

        // we are only deleting if we know we got good data
        // otherwise if we delete first a network failure would just make the app useless
        database.deleteAllRows();

        database.insertItems(result);

        // get items from the database, we could also just try to hand filter out the hidden items
        // which would alloc less memory and be faster, but this insures correctness
        result = database.getItems(mFilterHidden ? VideoDatabase.FILTER_HIDDEN_ITEMS : 0);
      }

      return result;
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

    private List<YouTubeData> getDataFromInternet(YouTubeAPI helper) {
      List<YouTubeData> result = null;
      String playlistID;

      YouTubeAPI.BaseListResults listResults = null;

      switch (type()) {
        case RELATED:
          YouTubeAPI.RelatedPlaylistType type = (YouTubeAPI.RelatedPlaylistType) listSpec.getData("type");
          String channelID = (String) listSpec.getData("channel");

          playlistID = helper.relatedPlaylistID(type, channelID);

          if (playlistID != null) // probably needed authorization and failed
            listResults = helper.videoListResults(playlistID);
          break;
        case VIDEOS:
          playlistID = (String) listSpec.getData("playlist");

          listResults = helper.videoListResults(playlistID);
          break;
        case SEARCH:
          String query = (String) listSpec.getData("query");
          listResults = helper.searchListResults(query);
          break;
        case LIKED:
          listResults = helper.likedVideosListResults();
          break;
        case PLAYLISTS:
          String channel = (String) listSpec.getData("channel");

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

    protected void onPostExecute(List<YouTubeData> result) {

      Util.log("YouTubeListDBTask: finished");

      items = result;
      access.onResults();

      runningTask = null;
    }
  }

}
