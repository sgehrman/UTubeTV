package com.sickboots.sickvideos;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;

/**
 * Static container class for holding a reference to your YouTube Developer Key.
 */
public class YouTubeHelper {

  /**
   * Please replace this with a valid API key which is enabled for the YouTube
   * Data API v3 service. Go to the <a
   * href="https://code.google.com/apis/console/">Google APIs Console</a> to
   * register a new developer key.
   */
  public static final String DEVELOPER_KEY = "AIzaSyD0gRStgO5O0hBRp4UeAxtsLFFw9bMinOI";

  public static final String TEST_MOVIE_ID = "Il1IGKaol_M";  // XuBdf9jYj7o
  public static final String TEST_PLAYLIST_ID = "FLCXAzufqBhwf_ib6xLv7gMw";
  public static final String DEV_PLAYLIST_ID = "PLhBgTdAWkxeBX09BokINT1ICC5IZ4C0ju";
  private static final String APP_NAME = "SickBoots";

  public static YouTube youTube(HttpRequestInitializer credential) {
    YouTube result=null;

    try {
      if (credential == null) {
        credential = new HttpRequestInitializer() {
          public void initialize(HttpRequest request) throws IOException {}
        };
      }

      result = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(APP_NAME).build();
    } catch (Exception e) {
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    }

    return result;
  }
}


/*

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