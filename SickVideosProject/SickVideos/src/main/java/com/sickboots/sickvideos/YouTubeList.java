package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.youtube.model.PlaylistItem;

import java.util.List;
import java.util.Map;

public class YouTubeList implements GoogleAccount.GoogleAccountDelegate, YouTubeHelper.YouTubeHelperListener, YouTubeFragment.YouTubeListProvider {
  private Util.ListResultListener listener;
  private GoogleAccount account;
  private YouTubeListSpec listSpec;
  private static final int REQUEST_AUTHORIZATION = 444;
  YouTubeHelper youTubeHelper;

  YouTubeList(YouTubeListSpec r) {
    super();

    listSpec = r;
  }

  @Override
  public YouTubeFragment.YouTubeListProvider start(Util.ListResultListener l) {
    account = GoogleAccount.newYouTube(this);
    listener = l;

    loadData(true);

    return this;
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

  // =================================================================================
  // YouTubeHelperListener

  @Override
  public void handleAuthIntent(Intent intent) {
    Util.toast(getActivity(), "Need Authorization");

    Fragment f = (Fragment)listener;

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
    Fragment f = (Fragment)listener;

    return f.getActivity();
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

          List<PlaylistItem> playlistItemList = youTubeHelper.playlistItemsForID(youTubeHelper.relatedPlaylistID(type));

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
      listener.onResults(YouTubeList.this.listener, result);
    }
  }

}
