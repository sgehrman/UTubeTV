package com.sickboots.sickvideos;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgehrman on 11/4/13.
 */
public class YouTubeListDB extends YouTubeList {
  YouTubeListDBTask runningTask=null;
  YouTubeDBHelper database;

  public YouTubeListDB(YouTubeListSpec s, UIAccess a) {
    super(s, a);

       database = new YouTubeDBHelper(getActivity());
  }

  @Override
  public void updateHighestDisplayedIndex(int position) {
    // doi nothing - not used in DB list
  }

  @Override
  public void refresh() {
    // empty DB and refetch
  }

  @Override
  protected void loadData(boolean askUser) {
    YouTubeHelper helper = youTubeHelper(askUser);

    if (helper != null) {
      if (runningTask == null) {
        runningTask = new YouTubeListDBTask();
        runningTask.execute(helper);
      }
    }
  }

  @Override
  public void handleClick(Map itemMap, boolean clickedIcon) {
    String movieID = (String) itemMap.get("video");

    if (movieID != null) {
      YouTubeHelper.playMovie(getActivity(), movieID);
    }

  }

  private class YouTubeListDBTask extends AsyncTask<YouTubeHelper, Void, List<Map>> {
    protected List<Map> doInBackground(YouTubeHelper... params) {
      YouTubeHelper helper = params[0];
      List<Map> result = null;

      Util.log("YouTubeListDBTask: started");

      // are the results already in the DB?
      result = database.getVideos();

      if (result.size() == 0) {
        YouTubeHelper.RelatedPlaylistType type = (YouTubeHelper.RelatedPlaylistType) listSpec.getData("type");
        String channelID = (String) listSpec.getData("channel");

        String playlistID = helper.relatedPlaylistID(type, channelID);

        YouTubeHelper.BaseListResults listResults = helper.videoListResults(playlistID);

        result = listResults.getItems();

        // add results to DB
        database.insertVideos(result);
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
