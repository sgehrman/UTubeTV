package com.sickboots.sickvideos.lists;

import android.os.AsyncTask;

import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.List;

public class YouTubeListLive extends YouTubeList {
  private YouTubeAPI.BaseListResults listResults;

  public YouTubeListLive(YouTubeListSpec s, UIAccess a) {
    super(s, a);

    loadData(TaskType.FIRSTLOAD, true);
  }

  @Override
  public void updateHighestDisplayedIndex(int position) {
    if (listResults != null) {
      listResults.updateHighestDisplayedIndex(position);
    }

    loadMoreIfNeeded();
  }

  private void loadMoreIfNeeded() {
    if (listResults != null) {
      // don't reload if already loading
      if (!listResults.isReloading()) {

        if (listResults.needsToLoadMoreItems()) {
          loadData(TaskType.REFETCH, false);
        }
      }
    }
  }

  @Override
  public void refresh() {
    listResults = null;

    loadData(TaskType.USER_REFRESH, false);
  }

  @Override
  protected void loadData(TaskType taskType, boolean askUser) {
    YouTubeAPI helper = youTubeHelper(askUser);

    if (helper != null) {
      boolean startTask = false;

      // don't launch twice if it's busy running
      if (listResults == null) {
        startTask = true;
      } else if (!listResults.isReloading()) {
        startTask = true;
      }

      if (startTask) {
        if (listResults != null)
          listResults.setIsReloading(true);

        new YouTubeListLiveTask().execute(helper);
      }
    }
  }

  @Override
  public void updateItem(YouTubeData itemMap) {
    // not supported for now
  }

  private class YouTubeListLiveTask extends AsyncTask<YouTubeAPI, Void, List<YouTubeData>> {
    protected List<YouTubeData> doInBackground(YouTubeAPI... params) {
      YouTubeAPI helper = params[0];
      List<YouTubeData> result = null;

      Util.log("YouTubeListLiveTask: started");

      if (listResults != null) {
        listResults.getNext();
      } else {
        switch (type()) {
          case SUBSCRIPTIONS:
            listResults = helper.subscriptionListResults();
            break;
          case PLAYLISTS: {
            String channel = (String) listSpec.getData("channel");

            listResults = helper.playlistListResults(channel, true);
            break;
          }
          case CATEGORIES:
            listResults = helper.categoriesListResults("US");
            break;
          case LIKED:
            listResults = helper.likedVideosListResults();
            break;

          case RELATED: {
            YouTubeAPI.RelatedPlaylistType type = (YouTubeAPI.RelatedPlaylistType) listSpec.getData("type");
            String channelID = (String) listSpec.getData("channel");

            String playlistID = helper.relatedPlaylistID(type, channelID);

            listResults = helper.videoListResults(playlistID, false);
            break;
          }
          case VIDEOS: {
            String playlistID = (String) listSpec.getData("playlist");

            listResults = helper.videoListResults(playlistID, false);
            break;
          }
          case SEARCH: {
            String query = (String) listSpec.getData("query");
            listResults = helper.searchListResults(query);
            break;
          }
        }
      }

      result = listResults.getItems();

      if (result != null) {
        // add empty entries
        int diff = (listResults.getTotalItems() - result.size());
        if (diff > 0) {
          // don't mutate the original, make a new copy first
          result = new ArrayList<YouTubeData>(result);

          YouTubeData empty = new YouTubeData();
          while (diff-- > 0) {
            result.add(empty);
          }
        }
      }

      return result;
    }

    protected void onPostExecute(List<YouTubeData> result) {
      listResults.setIsReloading(false);

      Util.log("YouTubeListLiveTask: finished");

      items = result;
      access.onListResults();

      loadMoreIfNeeded();
    }
  }

}
