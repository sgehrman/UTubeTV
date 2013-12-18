package com.sickboots.sickvideos.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.List;

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
      DatabaseAccess access = new DatabaseAccess(this, DatabaseTables.videoTable());

      List<YouTubeData> items = access.getItems(DatabaseTables.NEEDS_DATA_UPDATE, null, 50);

      if (items.size() > 0) {

        YouTubeAPI helper = new YouTubeAPI(this, new YouTubeAPI.YouTubeAPIListener() {
          @Override
          public void handleAuthIntent(final Intent authIntent) {
            Debug.log("handleAuthIntent inside update Service.  not handled here");
          }
        });

        List<String> videoIds = new ArrayList<String>();
        for (YouTubeData item : items) {
          videoIds.add(item.mVideo);
        }

        updateDataFromInternet(videoIds, helper);

        // start again assuming there is more waiting
        if (items.size() == 50)
          YouTubeUpdateService.startRequest(this);
      }

    } catch (Exception e) {
    }
  }

  private void updateDataFromInternet(List<String> videoIds, YouTubeAPI helper) {
    DatabaseTables.DatabaseTable table = DatabaseTables.videoTable();
    DatabaseAccess access = new DatabaseAccess(this, table);
    List<YouTubeData> updatedItems = new ArrayList<YouTubeData>();

    // get data from youtube api
    YouTubeAPI.BaseListResults listResults = helper.videoInfoListResults(videoIds);
    List<YouTubeData> fromYouTubeItems = listResults.getItems();

    // merge duration and other fields into the existing item
    for (YouTubeData itemFromYouTube : fromYouTubeItems) {
      // final item with matching videoId in the database and update it with duration and any other info we might get back

      String selection = DatabaseTables.VideoTable.VideoEntry.COLUMN_NAME_VIDEO + " = ?";
      String[] selectionArgs = new String[]{itemFromYouTube.mVideo};

      Cursor cursor = access.getCursor(selection, selectionArgs, table.defaultProjection());

      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          YouTubeData item = table.cursorToItem(cursor, null);

          item.mDuration = itemFromYouTube.mDuration;

          updatedItems.add(item);

          cursor.moveToNext();
        }
      } else {
        Debug.log("video not found?");
      }
    }

    Debug.log("SERVICE: updating info = " + updatedItems.size());

    // update items in the database
    access.updateItems(updatedItems);
  }

}