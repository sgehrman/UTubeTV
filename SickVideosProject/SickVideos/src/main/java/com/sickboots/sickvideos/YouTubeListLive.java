package com.sickboots.sickvideos;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;

import com.sickboots.sickvideos.youtube.YouTubeAPI;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubeListLive extends YouTubeList {
  private YouTubeAPI.BaseListResults listResults;

  public YouTubeListLive(YouTubeListSpec s, UIAccess a) {
    super(s, a);

    loadData(true);
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
          loadData(false);
        }
      }
    }
  }

  @Override
  public void refresh() {
    listResults = null;

    loadData(false);
  }

  @Override
  protected void loadData(boolean askUser) {
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
      case PLAYLISTS: {
        String playlistID = (String) itemMap.get("playlist");

        if (playlistID != null) {
          Fragment frag = YouTubeFragment.videosFragment(playlistID);

          replaceFragment(frag);
        }
      }
      break;
      case SUBSCRIPTIONS:
        String channel = (String) itemMap.get("channel");

        if (channel != null) {
          Fragment frag = YouTubeFragment.playlistsFragment(channel);

          replaceFragment(frag);
        }
        break;
      case CATEGORIES:
        break;
    }
  }

  private void replaceFragment(Fragment fragment) {
    FragmentManager fragmentManager = getActivity().getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    fragmentTransaction.replace(R.id.fragment_container, fragment);
    fragmentTransaction.addToBackStack(null);

    fragmentTransaction.commit();
  }

  private class YouTubeListLiveTask extends AsyncTask<YouTubeAPI, Void, List<Map>> {
    protected List<Map> doInBackground(YouTubeAPI... params) {
      YouTubeAPI helper = params[0];
      List<Map> result = null;

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
          result = new ArrayList<Map>(result);

          HashMap empty = new HashMap();
          while (diff-- > 0) {
            result.add(empty);
          }
        }
      }

      return result;
    }

    protected void onPostExecute(List<Map> result) {
      listResults.setIsReloading(false);

      Util.log("YouTubeListLiveTask: finished");

      items = result;
      access.onListResults();

      loadMoreIfNeeded();
    }
  }

}
