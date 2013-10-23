package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelListResponse;

import java.io.IOException;
import java.util.List;

/**
 * Static container class for holding a reference to your YouTube Developer Key.
 */
public class YouTubeHelper {
  private YouTubeHelperListener listener;
  private HttpRequestInitializer credential;
  private YouTube youTube;

  // must implement this listener
  public interface YouTubeHelperListener {
    public void handleAuthIntent(Intent authIntent);
    public void handleExceptionMessage(String message);
  }

  public YouTubeHelper(HttpRequestInitializer c, YouTubeHelperListener l) {
    super();

    listener = l;

    if (c == null) {
      c = new HttpRequestInitializer() {
        public void initialize(HttpRequest request) throws IOException {}
      };
    }

    credential = c;
  }

  public static String devKey() {
    return "AIzaSyD0gRStgO5O0hBRp4UeAxtsLFFw9bMinOI";
  }

  public static void playMovie(Activity activity, String movieID) {
    Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, YouTubeHelper.devKey(), movieID, 0, true, true);
    activity.startActivity(intent);
  }

  public YouTube youTube() {
    if (youTube == null) {
      try {
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("YouTubeHelper").build();
      } catch (Exception e) {
        e.printStackTrace();
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    return youTube;
  }

  public String channelID() {
    String result = null;

    try {
      YouTube.Channels.List channelRequest = youTube().channels().list("id");
      channelRequest.setMine(true);
      channelRequest.setMaxResults(1L);

      channelRequest.setFields("items/id");
      ChannelListResponse channelResult = channelRequest.execute();

      List<Channel> channelsList = channelResult.getItems();

      result = channelsList.get(0).getId();
    } catch (UserRecoverableAuthIOException e) {
      handleException(e);
    } catch (Exception e) {
      handleException(e);
    }

    return result;
  }

  private void handleException(Exception e) {
    if (e.getClass().equals(UserRecoverableAuthIOException.class)) {
      UserRecoverableAuthIOException r = (UserRecoverableAuthIOException) e;

      if (listener != null) {
        listener.handleAuthIntent(r.getIntent());
      }
    } else if (e.getClass().equals(GoogleJsonResponseException.class)) {
      GoogleJsonResponseException r = (GoogleJsonResponseException) e;

      e.printStackTrace();

      if (listener != null) {
        listener.handleExceptionMessage( "JSON Error: " + r.getDetails().getCode() + " : " + r.getDetails().getMessage());
      }
    } else {
      if (listener != null) {
        listener.handleExceptionMessage( "Exception Occurred");
      }

      e.printStackTrace();
    }
  }

}

/*

  public static final String TEST_MOVIE_ID = "Il1IGKaol_M";  // XuBdf9jYj7o
  public static final String TEST_PLAYLIST_ID = "FLCXAzufqBhwf_ib6xLv7gMw";
  public static final String DEV_PLAYLIST_ID = "PLhBgTdAWkxeBX09BokINT1ICC5IZ4C0ju";

App goals:

1) Simply watch a playlist of movies.
2) Mark which have been watched.
3) Remember where paused or stopped and resume next time
4) Display Full Title and credits below in a nice way
5) Should feel easy to watch a bit, relaunch and it takes you exactly back
6) Channel management (playlists)
7) Send playlists to friends.  Email or SMS or tap etc.
8) Educational playlists
9) record favorite moments (bookmarks) in video with single click, remember list of points to be rewatched later
10) What youtube should be, but simplified for watching only.  No comments or sharing etc, just watching experience
11) Killer features
    1) always restores where you were last time you watched
12) Get lists of playlists from youtube by users
13) Auto import your own youtube playlist
14)


console play

https://developers.google.com/youtube/v3/docs/activities/list
channel id: "UCCXAzufqBhwf_ib6xLv7gMw"
part id: "snippet"
fields: items(snippet/title, snippet/thumbnails/default/url), nextPageToken, pageInfo



https://developers.google.com/youtube/v3/docs/subscriptions/list
channel id: "UCCXAzufqBhwf_ib6xLv7gMw"
items/snippet/title

 */