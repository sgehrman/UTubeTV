package com.distantfuture.videos.channellookup;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.distantfuture.videos.content.Content;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.List;

public class ChannelLookupItemLoader extends AsyncTaskLoader<List<YouTubeData>> {

  private static final String TAG = "ChannelLookupItemLoader";
  private final String query;

  public ChannelLookupItemLoader(Context context, String query) {
    super(context);

    if (query != null && query.isEmpty())
      query = null;

    this.query = query;
  }

  @Override
  public List<YouTubeData> loadInBackground() {
    List<YouTubeData> result = null;

    try {
      YouTubeAPI helper = new YouTubeAPI(getContext(), new YouTubeAPI.YouTubeAPIListener() {
        @Override
        public void handleAuthIntent(final Intent authIntent) {
          Debug.log("handleAuthIntent inside update Service.  not handled here");
        }
      });

      if (query == null) {
        result = new ArrayList<YouTubeData>();

        List<YouTubeData> list = Content.instance().channels();
        for (YouTubeData data : list) {
          result.add(data);
        }
      } else {
        YouTubeAPI.SearchListResults searchList = helper.searchListResults(query, true);
        result = searchList.getAllItems(80);
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to fetch media data", e);
    }

    return result;
  }

  @Override
  protected void onStartLoading() {
    super.onStartLoading();
    forceLoad();
  }

  /**
   * Handles a request to stop the Loader.
   */
  @Override
  protected void onStopLoading() {
    // Attempt to cancel the current load task if possible.
    cancelLoad();
  }

}




