package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.ThumbnailDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubeChannelList implements GoogleAccount.GoogleAccountDelegate, YouTubeHelper.YouTubeHelperListener, YouTubeFragment.YouTubeListProvider {
  private Util.ListResultListener listener;
  private GoogleAccount account;
  private int relatedPlaylistID;
  private static final int REQUEST_AUTHORIZATION = 444;
  YouTubeHelper youTubeHelper;

  YouTubeChannelList(int r) {
    super();

    relatedPlaylistID = r;
  }

  @Override
  public YouTubeFragment.YouTubeListProvider start(Util.ListResultListener l) {
    account = new GoogleAccount(this);
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
      return playlist();
    }

    protected void onPostExecute(List<Map> result) {
      listener.onResults(YouTubeChannelList.this.listener, result);
    }

    private List<Map> playlist() {
      List<Map> result = new ArrayList<Map>();

      List<PlaylistItem> playlistItemList = youTubeHelper.playlistItemsForID(youTubeHelper.relatedPlaylistID(relatedPlaylistID));

      // convert the list into hash maps of video info
      for (PlaylistItem playlistItem: playlistItemList) {
        HashMap map = new HashMap();

        String thumbnail = "";
        ThumbnailDetails details = playlistItem.getSnippet().getThumbnails();
        if (details != null) {
          thumbnail = details.getDefault().getUrl();
        }

        map.put("video", playlistItem.getContentDetails().getVideoId());
        map.put("title", playlistItem.getSnippet().getTitle());
        map.put("thumbnail", thumbnail);

        result.add(map);
      }

      return result;
    }
  }

}
