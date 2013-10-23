package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.List;
import java.util.Map;

public class YouTubeSubscriptionList implements GoogleAccount.GoogleAccountDelegate, YouTubeFragment.YouTubeListProvider {
  private Util.ListResultListener listener;
  private GoogleAccount account;
  YouTubeHelper youTubeHelper;
  private int channelID;
  private static final int REQUEST_AUTHORIZATION = 444;

  YouTubeSubscriptionList(int c) {
    super();

    channelID = c;
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
    // this will make nextToken be ""
    youTubeHelper.refresh();

    loadData(false);
  }

  private void loadData(boolean askUser) {
    if (youTubeHelper == null) {
      GoogleAccountCredential credential = account.credential(askUser);

      if (credential != null) {
        youTubeHelper = new YouTubeHelper(credential, null);
      }
    }

    if (youTubeHelper != null) {
      new YouTubePlaylistTask().execute();
    }
  }

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
      return youTubeHelper.subscriptionsListToMap();
    }

    protected void onPostExecute(List<Map> result) {
      listener.onResults(YouTubeSubscriptionList.this.listener, result);
    }

  }

}
