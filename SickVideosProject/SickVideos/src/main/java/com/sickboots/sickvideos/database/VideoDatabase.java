package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.HashMap;
import java.util.Map;

public class VideoDatabase extends BaseDatabase {
  // stores information about a video
  public static class VideoEntry implements BaseColumns {
    public static final String COLUMN_NAME_VIDEO = "video";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
    public static final String COLUMN_NAME_DURATION = "duration";
    public static final String COLUMN_NAME_HIDDEN = "hidden";
  }

  public VideoDatabase(Context context, String databaseName) {
    super(context, databaseName);
  }

  @Override
  protected String[] projection() {
    String[] result = {
        VideoEntry._ID,
        VideoEntry.COLUMN_NAME_VIDEO,
        VideoEntry.COLUMN_NAME_TITLE,
        VideoEntry.COLUMN_NAME_DESCRIPTION,
        VideoEntry.COLUMN_NAME_THUMBNAIL,
        VideoEntry.COLUMN_NAME_DURATION,
        VideoEntry.COLUMN_NAME_HIDDEN
    };

    return result;
  }

  @Override
  protected Map cursorToItem(Cursor cursor) {
    Map result = new HashMap();

    result.put(YouTubeAPI.VIDEO_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_VIDEO)));
    result.put(YouTubeAPI.TITLE_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_TITLE)));
    result.put(YouTubeAPI.DESCRIPTION_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DESCRIPTION)));
    result.put(YouTubeAPI.THUMBNAIL_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_THUMBNAIL)));
    result.put(YouTubeAPI.DURATION_KEY, cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DURATION)));
    result.put(YouTubeAPI.HIDDEN_KEY, cursor.getInt(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_HIDDEN)));

    return result;
  }

  @Override
  protected void insertItem(SQLiteDatabase db, Map item) {
    // Create a new map of values, where column names are the keys
    ContentValues values = new ContentValues();

    values.put(VideoEntry.COLUMN_NAME_VIDEO, (String) item.get(YouTubeAPI.VIDEO_KEY));
    values.put(VideoEntry.COLUMN_NAME_TITLE, (String) item.get(YouTubeAPI.TITLE_KEY));
    values.put(VideoEntry.COLUMN_NAME_DESCRIPTION, (String) item.get(YouTubeAPI.DESCRIPTION_KEY));
    values.put(VideoEntry.COLUMN_NAME_THUMBNAIL, (String) item.get(YouTubeAPI.THUMBNAIL_KEY));
    values.put(VideoEntry.COLUMN_NAME_DURATION, (String) item.get(YouTubeAPI.DURATION_KEY));
    values.put(VideoEntry.COLUMN_NAME_HIDDEN, (Integer) item.get(YouTubeAPI.HIDDEN_KEY));

    // Insert the new row, returning the primary key value of the new row
    db.insert(mTableName, null, values);
  }

  @Override
  protected String createTableSQL() {
    final String TEXT_TYPE = " TEXT";
    final String INT_TYPE = " INT";
    final String COMMA_SEP = ",";

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
        + COMMA_SEP
        + VideoEntry.COLUMN_NAME_HIDDEN + INT_TYPE
        + " )";

    return result;
  }

  protected void updateItem(SQLiteDatabase db, Map item) {

  }
}
