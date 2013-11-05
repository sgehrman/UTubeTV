package com.sickboots.sickvideos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.YouTubeDBContract.VideoEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubeDatabase extends SQLiteOpenHelper {
  String mTableName;

  private static final int DATABASE_VERSION = 21;

  public YouTubeDatabase(Context context, String databaseName) {
    super(context, databaseName.toLowerCase() + ".db", null, DATABASE_VERSION);

    mTableName = "item_table";
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(createTableSQL());
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    db.execSQL("DROP TABLE IF EXISTS " + mTableName);
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public void deleteAllRows() {
    SQLiteDatabase db = getWritableDatabase();

    db.delete(mTableName, null, null);
  }

  public void insertVideos(List<Map> videos) {
    if (videos != null) {
      // Gets the data repository in write mode
      SQLiteDatabase db = getWritableDatabase();

      db.beginTransaction();
      try {
        for (Map video : videos)
          insertVideo(db, video);

        db.setTransactionSuccessful();
      } catch (Exception e) {
        Util.log("Insert Videos exception: " + e.getMessage());
      } finally {
        db.endTransaction();
        db.close();
      }
    }
  }

  public List<Map> getVideos() {
    List<Map> result = new ArrayList<Map>();

    SQLiteDatabase db = getReadableDatabase();

    String[] projection = {
        VideoEntry._ID,
        VideoEntry.COLUMN_NAME_VIDEO,
        VideoEntry.COLUMN_NAME_TITLE,
        VideoEntry.COLUMN_NAME_DESCRIPTION,
        VideoEntry.COLUMN_NAME_THUMBNAIL,
        VideoEntry.COLUMN_NAME_DURATION
    };

    Cursor cursor = db.query(
        mTableName,                     // The table to query
        projection,                     // The columns to return
        null,                           // The columns for the WHERE clause
        null,                           // The values for the WHERE clause
        null,                           // don't group the rows
        null,                           // don't filter by row groups
        null                            // The sort order
    );

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      result.add(cursorToVideo(cursor));
      cursor.moveToNext();
    }

    cursor.close();
    db.close();

    return result;
  }

  // ============================================================
  // private

  private Map cursorToVideo(Cursor cursor) {
    Map result = new HashMap();

    result.put(YouTubeAPI.VIDEO_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_VIDEO)));
    result.put(YouTubeAPI.TITLE_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_TITLE)));
    result.put(YouTubeAPI.DESCRIPTION_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DESCRIPTION)));
    result.put(YouTubeAPI.THUMBNAIL_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_THUMBNAIL)));
    result.put(YouTubeAPI.DURATION_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DURATION)));

    return result;
  }

  private void insertVideo(SQLiteDatabase db, Map video) {
    // Create a new map of values, where column names are the keys
    ContentValues values = new ContentValues();

    values.put(VideoEntry.COLUMN_NAME_VIDEO, (String) video.get(YouTubeAPI.VIDEO_KEY));
    values.put(VideoEntry.COLUMN_NAME_TITLE, (String) video.get(YouTubeAPI.TITLE_KEY));
    values.put(VideoEntry.COLUMN_NAME_DESCRIPTION, (String) video.get(YouTubeAPI.DESCRIPTION_KEY));
    values.put(VideoEntry.COLUMN_NAME_THUMBNAIL, (String) video.get(YouTubeAPI.THUMBNAIL_KEY));
    values.put(VideoEntry.COLUMN_NAME_DURATION, (String) video.get(YouTubeAPI.DURATION_KEY));

    // Insert the new row, returning the primary key value of the new row
    db.insert(mTableName, null, values);
  }

  private String createTableSQL() {
    String TEXT_TYPE = " TEXT";
    String COMMA_SEP = ",";

    String result = "CREATE TABLE "
        + mTableName
        + " ("
        + VideoEntry._ID + " INTEGER PRIMARY KEY,"
        + VideoEntry.COLUMN_NAME_VIDEO + TEXT_TYPE
        + COMMA_SEP
        + VideoEntry.COLUMN_NAME_TITLE + TEXT_TYPE
        + COMMA_SEP
        + VideoEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE
        + COMMA_SEP
        + VideoEntry.COLUMN_NAME_THUMBNAIL + TEXT_TYPE
        + COMMA_SEP
        + VideoEntry.COLUMN_NAME_DURATION + TEXT_TYPE
        + " )";

    return result;
  }
}
