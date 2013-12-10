package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by sgehrman on 12/9/13.
 */
public class DatabaseTables {
  // filter flags
  public static final int DELETE_ALL_ITEMS = -10;

  private static final String CREATE = "CREATE TABLE ";
  private static final String TEXT_TYPE = " TEXT";
  private static final String INT_TYPE = " INTEGER";
  private static final String PRIMARY = " PRIMARY KEY";
  private static final String COMMA_SEP = ",";

  public static interface DatabaseTable {
    public String tableName();

    public YouTubeData cursorToItem(Cursor cursor);

    public ContentValues contentValuesForItem(YouTubeData item);

    public String tableSQL();

    public String[] projection(int flags);

    public String whereClause(int flags, String requestId);

    public String[] whereArgs(int flags, String requestId);
  }

  public static DatabaseTable[] tables() {
    return new DatabaseTable[]{VideoTable.instance(), PlaylistTable.instance()};
  }

  public static class PlaylistTable implements DatabaseTable {
    // stores information about a playlist
    public class PlaylistEntry implements BaseColumns {
      public static final String COLUMN_NAME_REQUEST = "request";
      public static final String COLUMN_NAME_PLAYLIST = "playlist";
      public static final String COLUMN_NAME_TITLE = "title";
      public static final String COLUMN_NAME_DESCRIPTION = "description";
      public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
    }

    private static PlaylistTable singleton=null;
    public static PlaylistTable instance() {
      if (singleton == null)
        singleton = new PlaylistTable();

      return singleton;
    }

    private PlaylistTable() {
      super();
    }

    @Override
    public String tableName() {
      return "playlists";
    }

    @Override
    public YouTubeData cursorToItem(Cursor cursor) {
      YouTubeData result = new YouTubeData();

      result.mID = cursor.getLong(cursor.getColumnIndex(PlaylistEntry._ID));
      result.mRequest = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_REQUEST));
      result.mPlaylist = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_PLAYLIST));
      result.mTitle = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_TITLE));
      result.mDescription = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_DESCRIPTION));
      result.mThumbnail = cursor.getString(cursor.getColumnIndex(PlaylistEntry.COLUMN_NAME_THUMBNAIL));

      return result;
    }

    @Override
    public ContentValues contentValuesForItem(YouTubeData item) {
      ContentValues values = new ContentValues();

      values.put(PlaylistEntry.COLUMN_NAME_REQUEST, item.mRequest);
      values.put(PlaylistEntry.COLUMN_NAME_PLAYLIST, item.mPlaylist);
      values.put(PlaylistEntry.COLUMN_NAME_TITLE, item.mTitle);
      values.put(PlaylistEntry.COLUMN_NAME_DESCRIPTION, item.mDescription);
      values.put(PlaylistEntry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);

      return values;
    }

    @Override
    public String tableSQL() {
      String result = CREATE + tableName()
          + " ("
          + PlaylistEntry._ID + INT_TYPE + PRIMARY
          + COMMA_SEP
          + PlaylistEntry.COLUMN_NAME_REQUEST + TEXT_TYPE
          + COMMA_SEP
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
    public String[] projection(int flags) {
      String[] result = null;

      switch (flags) {
        default:
          result = new String[]{
              PlaylistEntry._ID,
              PlaylistEntry.COLUMN_NAME_REQUEST,
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
    public String whereClause(int flags, String requestId) {
      String result = PlaylistEntry.COLUMN_NAME_REQUEST + " = '" + requestId + "'";

      return result;
    }

    @Override
    public String[] whereArgs(int flags, String requestId) {
      return null;
    }
  }

  // =====================================================================
  // =====================================================================

  public static class VideoTable implements DatabaseTable {
    // filter flags
    public static final int FILTER_HIDDEN_ITEMS = 10;
    public static final int ONLY_HIDDEN_ITEMS = 20;

    // stores information about a video
    public class VideoEntry implements BaseColumns {
      public static final String COLUMN_NAME_REQUEST = "request";
      public static final String COLUMN_NAME_VIDEO = "video";
      public static final String COLUMN_NAME_TITLE = "title";
      public static final String COLUMN_NAME_DESCRIPTION = "description";
      public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
      public static final String COLUMN_NAME_DURATION = "duration";
      public static final String COLUMN_NAME_HIDDEN = "hidden";
      public static final String COLUMN_NAME_START = "start";
    }

    private static VideoTable singleton=null;
    public static VideoTable instance() {
      if (singleton == null)
        singleton = new VideoTable();

      return singleton;
    }

    private VideoTable() {
      super();
    }

    @Override
    public String tableName() {
      return "videos";
    }

    @Override
    public YouTubeData cursorToItem(Cursor cursor) {
      YouTubeData result = new YouTubeData();

      result.mID = cursor.getLong(cursor.getColumnIndex(VideoEntry._ID));
      result.mRequest = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_REQUEST));
      result.mVideo = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_VIDEO));
      result.mTitle = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_TITLE));
      result.mDescription = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DESCRIPTION));
      result.mThumbnail = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_THUMBNAIL));
      result.mDuration = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DURATION));
      result.setHidden(cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_HIDDEN)) != null);
      result.mStart = cursor.getInt(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_START));

      return result;
    }

    @Override
    public ContentValues contentValuesForItem(YouTubeData item) {
      ContentValues values = new ContentValues();

      values.put(VideoEntry.COLUMN_NAME_VIDEO, item.mVideo);
      values.put(VideoEntry.COLUMN_NAME_REQUEST, item.mRequest);
      values.put(VideoEntry.COLUMN_NAME_TITLE, item.mTitle);
      values.put(VideoEntry.COLUMN_NAME_DESCRIPTION, item.mDescription);
      values.put(VideoEntry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);
      values.put(VideoEntry.COLUMN_NAME_DURATION, item.mDuration);
      values.put(VideoEntry.COLUMN_NAME_HIDDEN, item.isHidden() ? "" : null);
      values.put(VideoEntry.COLUMN_NAME_START, item.mStart);

      return values;
    }

    @Override
    public String tableSQL() {
      String itemTable = CREATE + tableName()
          + " ("
          + VideoEntry._ID + INT_TYPE + PRIMARY
          + COMMA_SEP
          + VideoEntry.COLUMN_NAME_REQUEST + TEXT_TYPE
          + COMMA_SEP
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
          + VideoEntry.COLUMN_NAME_HIDDEN + TEXT_TYPE  // this is string since we use null or not null like a boolean, getInt returns 0 for null which makes it more complex to deal with null, 0, or 1.
          + COMMA_SEP
          + VideoEntry.COLUMN_NAME_START + INT_TYPE
          + " )";

      return itemTable;
    }

    @Override
    public String[] projection(int flags) {
      String[] result = null;

      switch (flags) {
        case ONLY_HIDDEN_ITEMS:
          result = new String[]{
              VideoEntry._ID,
              VideoEntry.COLUMN_NAME_REQUEST,
              VideoEntry.COLUMN_NAME_VIDEO,
              VideoEntry.COLUMN_NAME_HIDDEN
          };

        case FILTER_HIDDEN_ITEMS:
        default:
          result = new String[]{
              VideoEntry._ID,
              VideoEntry.COLUMN_NAME_REQUEST,
              VideoEntry.COLUMN_NAME_VIDEO,
              VideoEntry.COLUMN_NAME_TITLE,
              VideoEntry.COLUMN_NAME_DESCRIPTION,
              VideoEntry.COLUMN_NAME_THUMBNAIL,
              VideoEntry.COLUMN_NAME_DURATION,
              VideoEntry.COLUMN_NAME_HIDDEN,
              VideoEntry.COLUMN_NAME_START
          };
          break;
      }

      return result;
    }

    @Override
    public String whereClause(int flags, String requestId) {
      String result = null;

      switch (flags) {
        case ONLY_HIDDEN_ITEMS:
          result = "" + VideoEntry.COLUMN_NAME_HIDDEN + " IS NOT NULL";
          break;
        case FILTER_HIDDEN_ITEMS:
          result = "" + VideoEntry.COLUMN_NAME_HIDDEN + " IS NULL";
          break;
        case DELETE_ALL_ITEMS:
          break;
      }

      if (result == null)
        result = "";
      else
        result += " AND ";

      result += VideoEntry.COLUMN_NAME_REQUEST + " = '" + requestId + "'";

      return result;
    }

    @Override
    public String[] whereArgs(int flags, String requestId) {
      return null;
    }

  }
}
