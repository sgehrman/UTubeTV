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

public class YouTubeChannelList implements GoogleAccount.GoogleAccountDelegate, YouTubeFragment.YouTubeListProvider {
  private Util.ListResultListener listener;
  private GoogleAccount account;
  private int channelID;
  private static final int REQUEST_AUTHORIZATION = 444;
  private PlaylistItemListResponse currentItemListResponse;

  YouTubeChannelList(int c) {
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
    GoogleAccountCredential credential = account.credential(askUser);

    if (credential != null) {
      new YouTubePlaylistTask().execute(credential);
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

  private class YouTubePlaylistTask extends AsyncTask<GoogleAccountCredential, Void, List<Map>> {
    protected List<Map> doInBackground(GoogleAccountCredential... credentials) {
      GoogleAccountCredential credential = credentials[0];

      YouTube youtube = YouTubeHelper.youTube(credential);

      return playlist(youtube);
    }

    protected void onPostExecute(List<Map> result) {
      listener.onResults(YouTubeChannelList.this.listener, result);
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

    private String playlistID(YouTube youTube, int channelID) {
      String result = null;

      try {
        YouTube.Channels.List channelRequest = youTube.channels().list("contentDetails");
        channelRequest.setMine(true);

        channelRequest.setFields("items/contentDetails, nextPageToken, pageInfo");
        ChannelListResponse channelResult = channelRequest.execute();

        List<Channel> channelsList = channelResult.getItems();

        ChannelContentDetails.RelatedPlaylists relatedPlaylists = channelsList.get(0).getContentDetails().getRelatedPlaylists();

        if (channelsList != null) {
          // Gets user's default channel id (first channel in list).
          switch (channelID)
          {
              case 0:
                  result = relatedPlaylists.getFavorites();
                  break;
              case 1:
                  result = relatedPlaylists.getLikes();
                  break;
              case 2:
                  result = relatedPlaylists.getUploads();
                  break;
              case 3:
                  result = relatedPlaylists.getWatchHistory();
                  break;
              case 4:
                  result = relatedPlaylists.getWatchLater();
                  break;
          }
        }
      } catch (UserRecoverableAuthIOException e) {
        handleException(e);
      } catch (Exception e) {
        handleException(e);
      }

      return result;
    }

    private List<PlaylistItem> playlistItemsForID(YouTube youTube, String playlistID) {
      List<PlaylistItem> result = new ArrayList<PlaylistItem>();

      if (playlistID != null) {
        try {
          YouTube.PlaylistItems.List playlistItemRequest = youTube.playlistItems().list("id, contentDetails, snippet");
          playlistItemRequest.setPlaylistId(playlistID);

          playlistItemRequest.setFields("items(contentDetails/videoId, snippet/title, snippet/thumbnails/default/url), nextPageToken, pageInfo");

          playlistItemRequest.setPageToken(nextToken());
          currentItemListResponse = playlistItemRequest.execute();

          result.addAll(currentItemListResponse.getItems());
        } catch (UserRecoverableAuthIOException e) {
          handleException(e);
        } catch (Exception e) {
          handleException(e);
        }
      }

      return result;
    }

    private List<Map> playlist(YouTube youTube) {
      List<Map> result = new ArrayList<Map>();

      List<PlaylistItem> playlistItemList = playlistItemsForID(youTube, playlistID(youTube, channelID));

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
