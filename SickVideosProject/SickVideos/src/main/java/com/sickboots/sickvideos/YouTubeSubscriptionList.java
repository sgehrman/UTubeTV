package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.ThumbnailDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubeSubscriptionList implements GoogleAccount.GoogleAccountDelegate, YouTubeFragment.YouTubeListProvider {
  private Util.ListResultListener listener;
  private GoogleAccount account;
  YouTubeHelper youTubeHelper;
  private int channelID;
  private static final int REQUEST_AUTHORIZATION = 444;
  private SubscriptionListResponse currentItemListResponse;

  YouTubeSubscriptionList(int c) {
    super();

    channelID = c;
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
    // this will make nextToken be ""
    currentItemListResponse = null;

    loadData(false);
  }

  private String nextToken() {
    String result = null;

    if (currentItemListResponse != null) {
      result = currentItemListResponse.getNextPageToken();
    }

    if (result == null) {
      result = "";
    }

    return result;
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
      return playlist();
    }

    protected void onPostExecute(List<Map> result) {
      listener.onResults(YouTubeSubscriptionList.this.listener, result);
    }

    private void handleException(Exception e) {
      if (e.getClass().equals(UserRecoverableAuthIOException.class)) {
        UserRecoverableAuthIOException r = (UserRecoverableAuthIOException) e;
        Util.toast(getActivity(), "Need Authorization");

        Fragment f = (Fragment)listener;

        // start intent asking the user to authorize the app for google api
        f.startActivityForResult(r.getIntent(), REQUEST_AUTHORIZATION);
      } else if (e.getClass().equals(GoogleJsonResponseException.class)) {
        GoogleJsonResponseException r = (GoogleJsonResponseException) e;

        Util.toast(getActivity(), "JSON Error: " + r.getDetails().getCode() + " : " + r.getDetails().getMessage());
        e.printStackTrace();
      } else {
        Util.toast(getActivity(), "Exception Occurred");

        e.printStackTrace();
      }
    }

    private List<Subscription> playlistItemsForID() {
      List<Subscription> result = new ArrayList<Subscription>();

        try {
          YouTube.Subscriptions.List listRequest = youTubeHelper.youTube().subscriptions().list("id, contentDetails, snippet");
          listRequest.setMine(true);

          listRequest.setFields("items(snippet/title, snippet/thumbnails/default/url), nextPageToken, pageInfo");

          listRequest.setPageToken(nextToken());
          currentItemListResponse = listRequest.execute();

          result.addAll(currentItemListResponse.getItems());
        } catch (UserRecoverableAuthIOException e) {
          handleException(e);
        } catch (Exception e) {
          handleException(e);
        }

      return result;
    }

    private List<Map> playlist() {
      List<Map> result = new ArrayList<Map>();

      List<Subscription> playlistItemList = playlistItemsForID();

      // convert the list into hash maps of video info
      for (Subscription playlistItem: playlistItemList) {
        HashMap map = new HashMap();

        String thumbnail = "";
        ThumbnailDetails details = playlistItem.getSnippet().getThumbnails();
        if (details != null) {
          thumbnail = details.getDefault().getUrl();
        }

        map.put("id", playlistItem.getId());
        map.put("title", playlistItem.getSnippet().getTitle());
        map.put("description", playlistItem.getSnippet().getDescription());
        map.put("thumbnail", thumbnail);

        result.add(map);
      }

      return result;
    }
  }

}
