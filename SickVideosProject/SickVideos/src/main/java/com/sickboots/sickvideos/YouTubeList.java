package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.youtube.model.PlaylistItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YouTubeList implements GoogleAccount.GoogleAccountDelegate, YouTubeHelper.YouTubeHelperListener, YouTubeFragment.YouTubeListProvider {
  private UIAccess access;
  private GoogleAccount account;
  private YouTubeListSpec listSpec;
  private static final int REQUEST_AUTHORIZATION = 444;
  private YouTubeHelper youTubeHelper;
  private List<Map> items = new ArrayList<Map>();

  @Override
  public YouTubeFragment.YouTubeListProvider start(YouTubeListSpec s, UIAccess a) {
    listSpec = s;
    access = a;
    account = GoogleAccount.newYouTube(this);

    loadData(true);

    return this;
  }

  public void restart(UIAccess a) {
    access = a;
  }

  @Override
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

  public List<Map>getItems() {
    return items;
  }

  public YouTubeListSpec.ListType type() {
    return listSpec.type;
  }

  @Override
  public void moreData() {
    loadData(false);
  }

  @Override
  public void refresh() {
    youTubeHelper.refresh();

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
      new YouTubePlaylistTask().execute();
    }
  }

  public void handleClick(Map itemMap, boolean clickedIcon) {
    switch (type()) {
      case RELATED:
      case SEARCH:
      case PLAYLIST:
        String movieID = (String) itemMap.get("video");

        if (movieID != null) {
          YouTubeHelper.playMovie(getActivity(), movieID);
        }
        break;
      case SUBSCRIPTIONS:
        String channel = (String) itemMap.get("channel");

        Util.toast(getActivity(), channel != null ? channel : "no channel");

        Fragment frag = YouTubeFragment.newInstance(0, channel);

        FragmentManager fragmentManager = getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(frag, "scusk");
        fragmentTransaction.commit();


        break;
    }
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

  private class YouTubePlaylistTask extends AsyncTask<Void, Void, List<Map>> {
    protected List<Map> doInBackground(Void... params) {
      List<Map> result = null;

      switch (listSpec.type)
      {
        case SUBSCRIPTIONS:
          result = youTubeHelper.subscriptionsListToMap();
          break;

        case RELATED:
          YouTubeHelper.RelatedPlaylistType type = (YouTubeHelper.RelatedPlaylistType) listSpec.getData("type");
          String channel = (String) listSpec.getData("channel");

          List<PlaylistItem> playlistItemList = youTubeHelper.playlistItemsForID(youTubeHelper.relatedPlaylistID(type, channel));

          result = youTubeHelper.playlistItemsToMap(playlistItemList);
          break;

        case SEARCH:
          String query = (String) listSpec.getData("query");

          result = youTubeHelper.searchListToMap(query);
          break;
      }

      return result;
    }

    protected void onPostExecute(List<Map> result) {
      items.addAll(result);
      access.onListResults();
    }
  }

}
