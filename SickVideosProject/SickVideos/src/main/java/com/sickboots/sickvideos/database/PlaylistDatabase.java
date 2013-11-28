
package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

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
  protected YouTubeData cursorToItem(Cursor cursor) {
    YouTubeData result = new YouTubeData();

    result.mID = cursor.getLong(cursor.getColumnIndex(PlaylistEntry._ID));
    result.mPlaylist = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_PLAYLIST));
    result.mTitle = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_TITLE));
    result.mDescription = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_DESCRIPTION));
    result.mThumbnail = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_THUMBNAIL));

    return result;
  }

  @Override
  protected ContentValues contentValuesForItem(YouTubeData item) {
    ContentValues values = new ContentValues();

    values.put(PlaylistEntry.COLUMN_NAME_PLAYLIST, item.mPlaylist);
    values.put(PlaylistEntry.COLUMN_NAME_TITLE, item.mTitle);
    values.put(PlaylistEntry.COLUMN_NAME_DESCRIPTION, item.mDescription);
    values.put(PlaylistEntry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);

    return values;
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

  @Override
  protected String getItemsWhereClause(int flags) {
    return null;
  }

  @Override
  protected String[] getItemsWhereArgs(int flags) {
    return null;
  }

}
