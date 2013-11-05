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

/**
 * Created by sgehrman on 11/4/13.
 */
public class YouTubeDBHelper extends SQLiteOpenHelper {
  private static final String TEXT_TYPE = " TEXT";
  private static final String COMMA_SEP = ",";
  private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
      + VideoEntry.TABLE_NAME
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
  private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME;
  private static final int DATABASE_VERSION = 7;
  private static final String DATABASE_NAME = "YouTube.db";

  public YouTubeDBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_ENTRIES);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
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
      }
      catch(Exception e) {
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
        VideoEntry.TABLE_NAME,          // The table to query
        projection,                     // The columns to return
        null,                           // The columns for the WHERE clause
        null,                           // The values for the WHERE clause
        null,                           // don't group the rows
        null,                           // don't filter by row groups
        null                            // The sort order
    );

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Map comment = cursorToVideo(cursor);
      result.add(comment);
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

    result.put(YouTubeHelper.VIDEO_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_VIDEO)));
    result.put(YouTubeHelper.TITLE_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_TITLE)));
    result.put(YouTubeHelper.DESCRIPTION_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DESCRIPTION)));
    result.put(YouTubeHelper.THUMBNAIL_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_THUMBNAIL)));
    result.put(YouTubeHelper.DURATION_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DURATION)));

    return result;
  }

  private void insertVideo(SQLiteDatabase db, Map video) {
    // Create a new map of values, where column names are the keys
    ContentValues values = new ContentValues();

    values.put(VideoEntry.COLUMN_NAME_VIDEO, (String) video.get(YouTubeHelper.VIDEO_KEY));
    values.put(VideoEntry.COLUMN_NAME_TITLE, (String) video.get(YouTubeHelper.TITLE_KEY));
    values.put(VideoEntry.COLUMN_NAME_DESCRIPTION, (String) video.get(YouTubeHelper.DESCRIPTION_KEY));
    values.put(VideoEntry.COLUMN_NAME_THUMBNAIL, (String) video.get(YouTubeHelper.THUMBNAIL_KEY));
    values.put(VideoEntry.COLUMN_NAME_DURATION, (String) video.get(YouTubeHelper.DURATION_KEY));

    // Insert the new row, returning the primary key value of the new row
    long newRowId = db.insert(
        VideoEntry.TABLE_NAME,
        null,
        values);
  }

}
