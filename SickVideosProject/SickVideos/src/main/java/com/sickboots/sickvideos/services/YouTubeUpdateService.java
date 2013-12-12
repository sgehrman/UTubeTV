package com.sickboots.sickvideos.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

import com.sickboots.sickvideos.AuthActivity;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YouTubeUpdateService extends IntentService {
  public YouTubeUpdateService() {
    super("YouTubeUpdateService");
  }

  public static void startRequest(Context context) {
    context = context.getApplicationContext();

    Intent i = new Intent(context, YouTubeUpdateService.class);
    context.startService(i);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      DatabaseAccess access = new DatabaseAccess(this, DatabaseTables.videoTable(), null);

      List<YouTubeData> items = access.getItems(DatabaseTables.NEEDS_DATA_UPDATE);

      if (items.size() > 0) {

        YouTubeAPI helper = new YouTubeAPI(this, new YouTubeAPI.YouTubeAPIListener() {
          @Override
          public void handleAuthIntent(final Intent authIntent) {
            Util.log("handleAuthIntent inside update Service.  not handled here");
          }
        });

        updateDataFromInternet(items, helper);
      }

    } catch (Exception e) {
    }
  }

  private void updateDataFromInternet(List<YouTubeData> items, YouTubeAPI helper) {

    Util.log("getting list from net...");

    List<String> videoIds = new ArrayList<String>();
    for (YouTubeData item : items) {
      videoIds.add(item.mVideo);
    }

    YouTubeAPI.BaseListResults listResults = helper.videoInfoListResults(videoIds);

    List<YouTubeData> infoList = listResults.getItems();

    // now need to take this data and merge it into the existing item
    for (YouTubeData item : infoList) {
      Util.log(item.mDuration);
    }

  }

}