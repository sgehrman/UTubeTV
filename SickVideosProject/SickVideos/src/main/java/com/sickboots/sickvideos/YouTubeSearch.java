package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.ThumbnailDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class YouTubeSearch implements YouTubeFragment.YouTubeListProvider {
  private static final String SEARCH_FIELDS = "items(id/kind, id/videoId, snippet/title, snippet/thumbnails/default/url)";
  private static final String SEARCH_TYPE = "video";
  private static final long NUMBER_OF_VIDEOS_RETURNED = 10;
  private Util.ListResultListener listener;

  @Override
  public YouTubeFragment.YouTubeListProvider start(Util.ListResultListener l) {
    listener = l;
    new YouTubeSearchTask().execute();

    return this;
  }

  @Override
  public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
    boolean handled = false;

    return handled;
  }

    private class YouTubeSearchTask extends AsyncTask<Void, Void, List<Map>> {
    protected List<Map> doInBackground(Void... params) {
      return startSearch();
    }

    protected void onPostExecute(List<Map> result) {
      listener.onResults(com.sickboots.sickvideos.YouTubeSearch.this.listener, result);
    }

    private Activity getActivity() {
      Fragment fragment = (Fragment) listener;

      return fragment.getActivity();
    }

    private List<Map> startSearch() {
      List<Map> result = new ArrayList<Map>();

      try {
        YouTube youtube = YouTubeHelper.youTube(null);

        String queryTerm = getInputQuery();

        YouTube.Search.List search = youtube.search().list("id,snippet");

        search.setQ(queryTerm);
        search.setKey(YouTubeHelper.DEVELOPER_KEY);
        search.setType(SEARCH_TYPE);
        search.setFields(SEARCH_FIELDS);
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();

        if (searchResultList != null) {
          Iterator<SearchResult> iteratorSearchResults = searchResultList.iterator();

          if(!iteratorSearchResults.hasNext()){
            Util.log("No results");
          }

          while (iteratorSearchResults.hasNext()){
            SearchResult singleVideo = iteratorSearchResults.next();

            ResourceId resID = singleVideo.getId();
            if (resID.getKind().equals("youtube#video")) {
              SearchResultSnippet snippet = singleVideo.getSnippet();

              if (snippet != null) {
                // get the title and video id and return an object
                HashMap map = new HashMap();

                String thumbnail = "none";
                ThumbnailDetails details = snippet.getThumbnails();
                if (details != null) {
                  thumbnail = details.getDefault().getUrl();
                }

                map.put("video", resID.getVideoId());
                map.put("title", snippet.getTitle());
                map.put("thumbnail", thumbnail);

                result.add(map);
              }
            } else {
              Util.log("nullll");
            }

          }

        } else {
          Util.log("no results");
        }

      } catch (IOException e) {
        System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        e.printStackTrace();
        Util.toast(getActivity(), "There was an IO error: " + e.getCause() + " : " + e.getMessage());
      } catch (Exception e) {
        e.printStackTrace();

        Util.toast(getActivity(), "Exception Occurred");
      }

      return result;
    }

    private String getInputQuery() throws IOException {

      String inputQuery = "";

      inputQuery = "Android";

      return inputQuery;
    }

  }

}
