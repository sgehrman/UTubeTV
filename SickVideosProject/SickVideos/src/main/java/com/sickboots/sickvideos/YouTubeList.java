package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewParent;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubeList implements GoogleAccount.GoogleAccountDelegate, YouTubeHelper.YouTubeHelperListener {
  private UIAccess access;
  private GoogleAccount account;
  private YouTubeListSpec listSpec;
  private static final int REQUEST_AUTHORIZATION = 444;
  private YouTubeHelper youTubeHelper;
  private List<Map> items = new ArrayList<Map>();
  private YouTubeHelper.BaseListResults listResults;

  public YouTubeList(YouTubeListSpec s, UIAccess a) {
    super();

    listSpec = s;
    access = a;
    account = GoogleAccount.newYouTube(this);

    loadData(true);
  }

  public void restart(UIAccess a) {
    access = a;
  }

  public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
    boolean handled = false;

    if (!handled) {
      switch (requestCode) {
        case REQUEST_AUTHORIZATION:
          if (resultCode != Activity.RESULT_OK) {
            loadData(true);
          }

          handled = true;
          break;
      }
    }

    return handled;
  }

  public List<Map> getItems() {
    return items;
  }

  public YouTubeListSpec.ListType type() {
    return listSpec.type;
  }

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

  public void refresh() {
    listResults = null;

    loadData(false);
  }

  private void loadData(boolean askUser) {
    if (youTubeHelper == null) {
      GoogleAccountCredential credential = account.credential(askUser);

      if (credential != null) {
        youTubeHelper = new YouTubeHelper(credential, this);
      }
    }

    if (youTubeHelper != null) {
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

        new YouTubeListTask().execute();
      }
    }
  }

  public void handleClick(Map itemMap, boolean clickedIcon) {
    switch (type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        String movieID = (String) itemMap.get("video");

        if (movieID != null) {
          YouTubeHelper.playMovie(getActivity(), movieID);
        }
        break;
      case PLAYLISTS:
      {
        String playlistID = (String) itemMap.get("playlist");

        if (playlistID != null) {
          Fragment frag = YouTubeFragment.newInstance(YouTubeListSpec.ListType.VIDEOS, null, playlistID);

          replaceFragment(frag);
        }
      }
        break;
      case SUBSCRIPTIONS:
        String channel = (String) itemMap.get("channel");

        if (channel != null) {
          Fragment frag = YouTubeFragment.newInstance(YouTubeListSpec.ListType.PLAYLISTS, channel, null);

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

  // =================================================================================
  // YouTubeHelperListener

  @Override
  public void handleAuthIntent(Intent intent) {
    Util.toast(getActivity(), "Need Authorization");

    Fragment f = access.fragment();

    // start intent asking the user to authorize the app for google api
    f.startActivityForResult(intent, REQUEST_AUTHORIZATION);
  }

  @Override
  public void handleExceptionMessage(String message) {
    Util.toast(getActivity(), message);
  }

  // =================================================================================

  @Override   // in GoogleAccountDelegate
  public void credentialIsReady() {
    loadData(false);
  }

  @Override
  public Activity getActivity() {
    return access.fragment().getActivity();
  }

  private class YouTubeListTask extends AsyncTask<Void, Void, List<Map>> {
    protected List<Map> doInBackground(Void... params) {
      List<Map> result = null;

      Util.log("YouTubeListTask: started");

      if (listResults != null) {
        listResults.getNext();
      } else {
        switch (listSpec.type) {
          case SUBSCRIPTIONS:
            listResults = youTubeHelper.subscriptionListResults();
            break;
          case PLAYLISTS: {
            String channel = (String) listSpec.getData("channel");

            listResults = youTubeHelper.playlistListResults(channel, true);
            break;
          }
          case CATEGORIES:
            listResults = youTubeHelper.categoriesListResults("US");
            break;
          case LIKED:
            listResults = youTubeHelper.likedVideosListResults();
            break;

          case RELATED: {
            YouTubeHelper.RelatedPlaylistType type = (YouTubeHelper.RelatedPlaylistType) listSpec.getData("type");
            String channelID = (String) listSpec.getData("channel");

            String playlistID = youTubeHelper.relatedPlaylistID(type, channelID);

            listResults = youTubeHelper.videoListResults(playlistID);
            break;
          }
          case VIDEOS: {
            String playlistID = (String) listSpec.getData("playlist");

            listResults = youTubeHelper.videoListResults(playlistID);
            break;
          }
          case SEARCH: {
            String query = (String) listSpec.getData("query");
            listResults = youTubeHelper.searchListResults(query);
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

      Util.log("YouTubeListTask: finished");

      items = result;
      access.onListResults();

      loadMoreIfNeeded();
    }
  }

}
