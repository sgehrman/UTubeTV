package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by sgehrman on 12/9/13.
 */
public class DatabaseTables {
  // filter flags
  public static final int ALL_ITEMS = 0;
  public static final int HIDDEN_ITEMS = 10;
  public static final int VISIBLE_ITEMS = 20;
  public static final int NEEDS_DATA_UPDATE = 30;

  private static final String CREATE = "CREATE TABLE ";
  private static final String TEXT_TYPE = " TEXT";
  private static final String INT_TYPE = " INTEGER";
  private static final String PRIMARY = " PRIMARY KEY";
  private static final String COMMA_SEP = ",";

  private static VideoTable mVideoTable = null;
  private static PlaylistTable mPlaylistTable = null;

  public static VideoTable videoTable() {
    if (mVideoTable == null)
      mVideoTable = new VideoTable();

    return mVideoTable;
  }

  public static PlaylistTable playlistTable() {
    if (mPlaylistTable == null)
      mPlaylistTable = new PlaylistTable();

    return mPlaylistTable;
  }

  public static interface DatabaseTable {
    public String tableName();

    public YouTubeData cursorToItem(Cursor cursor, YouTubeData reuseData);

    public ContentValues contentValuesForItem(YouTubeData item);

    public String tableSQL();

    public String indexSQL();

    public Database.DatabaseQuery queryParams(int queryID, String requestId);

    public String[] defaultProjection();

  }

  public static DatabaseTable[] tables() {
    return new DatabaseTable[]{DatabaseTables.videoTable(), DatabaseTables.playlistTable()};
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

    private static PlaylistTable singleton = null;

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
    public YouTubeData cursorToItem(Cursor cursor, YouTubeData reuseData) {
      YouTubeData result = reuseData;  // avoiding memory alloc during draw
      if (result == null)
        result = new YouTubeData();

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
    public String indexSQL() {
      return null;
    }

    @Override
    public String[] defaultProjection() {
      String[] projection = new String[]{
          PlaylistEntry._ID,
          PlaylistEntry.COLUMN_NAME_REQUEST,
          PlaylistEntry.COLUMN_NAME_PLAYLIST,
          PlaylistEntry.COLUMN_NAME_TITLE,
          PlaylistEntry.COLUMN_NAME_DESCRIPTION,
          PlaylistEntry.COLUMN_NAME_THUMBNAIL
      };

      return projection;
    }

    @Override
    public Database.DatabaseQuery queryParams(int queryID, String requestId) {
      String selection = null;
      String[] selectionArgs = null;
      String[] projection = defaultProjection();

      if (requestId != null) {
        selectionArgs = new String[]{requestId};

        if (selection == null)
          selection = "";
        else
          selection += " AND ";

        selection += PlaylistEntry.COLUMN_NAME_REQUEST + " = ?";
      }

      return new Database.DatabaseQuery(tableName(), selection, selectionArgs, projection);
    }
  }

  // =====================================================================
  // =====================================================================

  public static class VideoTable implements DatabaseTable {
    // stores information about a video
    public class VideoEntry implements BaseColumns {
      public static final String COLUMN_NAME_REQUEST = "request";
      public static final String COLUMN_NAME_VIDEO = "video";
      public static final String COLUMN_NAME_TITLE = "title";
      public static final String COLUMN_NAME_DESCRIPTION = "description";
      public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
      public static final String COLUMN_NAME_DURATION = "duration";
      public static final String COLUMN_NAME_HIDDEN = "hidden";
    }

    private VideoTable() {
      super();
    }

    @Override
    public String tableName() {
      return "videos";
    }

    @Override
    public YouTubeData cursorToItem(Cursor cursor, YouTubeData reuseData) {
      YouTubeData result = reuseData;  // avoiding memory alloc during draw
      if (result == null)
        result = new YouTubeData();

      int col;

      col = cursor.getColumnIndex(VideoEntry._ID);
      if (col != -1)
        result.mID = cursor.getLong(col);

      col = cursor.getColumnIndex(VideoEntry.COLUMN_NAME_REQUEST);
      if (col != -1)
        result.mRequest = cursor.getString(col);

      col = cursor.getColumnIndex(VideoEntry.COLUMN_NAME_VIDEO);
      if (col != -1)
        result.mVideo = cursor.getString(col);

      col = cursor.getColumnIndex(VideoEntry.COLUMN_NAME_TITLE);
      if (col != -1)
        result.mTitle = cursor.getString(col);

      col = cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DESCRIPTION);
      if (col != -1)
        result.mDescription = cursor.getString(col);

      col = cursor.getColumnIndex(VideoEntry.COLUMN_NAME_THUMBNAIL);
      if (col != -1)
        result.mThumbnail = cursor.getString(col);

      col = cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DURATION);
      if (col != -1)
        result.mDuration = cursor.getString(col);

      col = cursor.getColumnIndex(VideoEntry.COLUMN_NAME_HIDDEN);
      if (col != -1)
        result.setHidden(cursor.getString(col) != null);

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

      return values;
    }

    @Override
    public String indexSQL() {
      String indexName = VideoEntry.COLUMN_NAME_VIDEO + "_idx";

      String result = "CREATE INDEX " + indexName + " on " + tableName() + "(" + VideoEntry.COLUMN_NAME_VIDEO + ")";

      return result;
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
          + " )";

      return itemTable;
    }

    @Override
    public String[] defaultProjection() {
      String[] projection = new String[]{
          VideoEntry._ID,
          VideoEntry.COLUMN_NAME_REQUEST,
          VideoEntry.COLUMN_NAME_VIDEO,
          VideoEntry.COLUMN_NAME_TITLE,
          VideoEntry.COLUMN_NAME_DESCRIPTION,
          VideoEntry.COLUMN_NAME_THUMBNAIL,
          VideoEntry.COLUMN_NAME_DURATION,
          VideoEntry.COLUMN_NAME_HIDDEN
      };

      return projection;
    }

    @Override
    public Database.DatabaseQuery queryParams(int queryID, String requestId) {
      String selection = null;
      String[] selectionArgs = null;
      String[] projection = defaultProjection();

      switch (queryID) {
        case HIDDEN_ITEMS:
          selection = "" + VideoEntry.COLUMN_NAME_HIDDEN + " IS NOT NULL";

          projection = new String[]{
              VideoEntry._ID,
              VideoEntry.COLUMN_NAME_REQUEST,
              VideoEntry.COLUMN_NAME_VIDEO,
              VideoEntry.COLUMN_NAME_HIDDEN
          };
          break;
        case NEEDS_DATA_UPDATE:
          selection = "" + VideoEntry.COLUMN_NAME_DURATION + " IS NULL";

          projection = new String[]{
              VideoEntry._ID,
              VideoEntry.COLUMN_NAME_VIDEO,
              VideoEntry.COLUMN_NAME_DURATION
          };
          break;
        case VISIBLE_ITEMS:
          selection = "" + VideoEntry.COLUMN_NAME_HIDDEN + " IS NULL";
          break;
        case ALL_ITEMS:
          break;
      }

      if (requestId != null) {
        selectionArgs = new String[]{requestId};

        if (selection == null)
          selection = "";
        else
          selection += " AND ";

        selection += VideoEntry.COLUMN_NAME_REQUEST + " = ?";
      }

      return new Database.DatabaseQuery(tableName(), selection, selectionArgs, projection);
    }
  }
}
