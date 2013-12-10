package com.sickboots.sickvideos.lists;

import android.os.AsyncTask;

import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;
import com.sickboots.sickvideos.youtube.YouTubeAPIService;
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

import java.util.List;

public class YouTubeListDB extends YouTubeList {
  YouTubeListDBTask runningTask = null;
  DatabaseAccess database;

  public YouTubeListDB(YouTubeServiceRequest request, UIAccess a) {
    super(request, a);

    database = new DatabaseAccess(a.getContext(), request);

    loadData(TaskType.FIRSTLOAD);
  }

  @Override
  public void refresh() {
    loadData(TaskType.USER_REFRESH);
  }

  @Override
  public void refetch() {
    loadData(TaskType.REFETCH);
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
          YouTubeAPIService.startRequest(access.getContext(), mRequest);

          break;
        case REFETCH:
          //  item hidden, or hidden visible pref toggled
          result = database.getItems(mFilterHidden ? DatabaseTables.VideoTable.FILTER_HIDDEN_ITEMS : 0);
          break;
        case FIRSTLOAD:
          result = database.getItems(mFilterHidden ? DatabaseTables.VideoTable.FILTER_HIDDEN_ITEMS : 0);

          // this is lame, fix later
          if (result.size() == 0) {
            YouTubeAPIService.startRequest(access.getContext(), mRequest);
          }

          break;
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
