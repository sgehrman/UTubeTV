
package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

public class PlaylistTable implements DatabaseTable {
  // stores information about a playlist
  public static class PlaylistEntry implements BaseColumns {
    public static final String COLUMN_NAME_PLAYLIST = "playlist";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
  }

  public PlaylistTable() {
    super();
  }

  @Override
  public String tableName() {
    return "playlists";
  }

  @Override
  public String[] projection(int flags) {
    String[] result = null;

    switch (flags) {
      default:
        result = new String[]{
            PlaylistEntry._ID,
            PlaylistEntry.COLUMN_NAME_PLAYLIST,
            PlaylistEntry.COLUMN_NAME_TITLE,
            PlaylistEntry.COLUMN_NAME_DESCRIPTION,
            PlaylistEntry.COLUMN_NAME_THUMBNAIL
        };
        break;
    }

    return result;
  }

  @Override
  public YouTubeData cursorToItem(Cursor cursor) {
    YouTubeData result = new YouTubeData();

    result.mID = cursor.getLong(cursor.getColumnIndex(PlaylistEntry._ID));
    result.mPlaylist = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_PLAYLIST));
    result.mTitle = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_TITLE));
    result.mDescription = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_DESCRIPTION));
    result.mThumbnail = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_THUMBNAIL));

    return result;
  }

  @Override
  public ContentValues contentValuesForItem(YouTubeData item) {
    ContentValues values = new ContentValues();

    values.put(PlaylistEntry.COLUMN_NAME_PLAYLIST, item.mPlaylist);
    values.put(PlaylistEntry.COLUMN_NAME_TITLE, item.mTitle);
    values.put(PlaylistEntry.COLUMN_NAME_DESCRIPTION, item.mDescription);
    values.put(PlaylistEntry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);

    return values;
  }

  @Override
  public String[] tablesSQL() {
    String result = CREATE + tableName()
        + " ("
        + PlaylistEntry._ID + INT_TYPE + PRIMARY
        + COMMA_SEP
        + PlaylistEntry.COLUMN_NAME_PLAYLIST + TEXT_TYPE
        + COMMA_SEP
        + PlaylistEntry.COLUMN_NAME_TITLE + TEXT_TYPE
        + COMMA_SEP
        + PlaylistEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE
        + COMMA_SEP
        + PlaylistEntry.COLUMN_NAME_THUMBNAIL + TEXT_TYPE
        + " )";

    return new String[]{result};
  }

  @Override
  public String whereClause(int flags) {
    return null;
  }

  @Override
  public String[] whereArgs(int flags) {
    return null;
  }

}
