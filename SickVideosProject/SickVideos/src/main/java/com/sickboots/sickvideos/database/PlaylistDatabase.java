
package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.HashMap;
import java.util.Map;

public class PlaylistDatabase extends BaseDatabase {
  // stores information about a playlist
  public static class PlaylistEntry implements BaseColumns {
    public static final String COLUMN_NAME_PLAYLIST = "playlist";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
  }

  public PlaylistDatabase(Context context, String databaseName) {
    super(context, databaseName);
  }

  @Override
  protected String[] projection() {
    String[] result = {
        PlaylistEntry._ID,
        PlaylistEntry.COLUMN_NAME_PLAYLIST,
        PlaylistEntry.COLUMN_NAME_TITLE,
        PlaylistEntry.COLUMN_NAME_DESCRIPTION,
        PlaylistEntry.COLUMN_NAME_THUMBNAIL
    };

    return result;
  }

  @Override
  protected Map cursorToItem(Cursor cursor) {
    Map result = new HashMap();

    result.put(YouTubeAPI.ID_KEY, cursor.getLong(cursor.getColumnIndex(PlaylistEntry._ID)));
    result.put(YouTubeAPI.PLAYLIST_KEY, cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_PLAYLIST)));
    result.put(YouTubeAPI.TITLE_KEY, cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_TITLE)));
    result.put(YouTubeAPI.DESCRIPTION_KEY, cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_DESCRIPTION)));
    result.put(YouTubeAPI.THUMBNAIL_KEY, cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_THUMBNAIL)));

    return result;
  }

  @Override
  protected void insertItem(SQLiteDatabase db, Map item) {
    // Create a new map of values, where column names are the keys
    ContentValues values = new ContentValues();

    values.put(PlaylistEntry.COLUMN_NAME_PLAYLIST, (String) item.get(YouTubeAPI.PLAYLIST_KEY));
    values.put(PlaylistEntry.COLUMN_NAME_TITLE, (String) item.get(YouTubeAPI.TITLE_KEY));
    values.put(PlaylistEntry.COLUMN_NAME_DESCRIPTION, (String) item.get(YouTubeAPI.DESCRIPTION_KEY));
    values.put(PlaylistEntry.COLUMN_NAME_THUMBNAIL, (String) item.get(YouTubeAPI.THUMBNAIL_KEY));

    // Insert the new row, returning the primary key value of the new row
    db.insert(mTableName, null, values);
  }

  @Override
  protected String createTableSQL() {
    String TEXT_TYPE = " TEXT";
    String COMMA_SEP = ",";

    String result = "CREATE TABLE "
        + mTableName
        + " ("
        + PlaylistEntry._ID + " INTEGER PRIMARY KEY,"
        + PlaylistEntry.COLUMN_NAME_PLAYLIST + TEXT_TYPE
        + COMMA_SEP
        + PlaylistEntry.COLUMN_NAME_TITLE + TEXT_TYPE
        + COMMA_SEP
        + PlaylistEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE
        + COMMA_SEP
        + PlaylistEntry.COLUMN_NAME_THUMBNAIL + TEXT_TYPE
        + " )";

    return result;
  }
}
