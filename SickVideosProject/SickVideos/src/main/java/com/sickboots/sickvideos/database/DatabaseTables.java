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

  private static final String CREATE = "CREATE TABLE ";
  private static final String TEXT_TYPE = " TEXT";
  private static final String INT_TYPE = " INTEGER";
  private static final String PRIMARY = " PRIMARY KEY";
  private static final String COMMA_SEP = ",";

  private static VideoTable mVideoTable = null;
  private static PlaylistTable mPlaylistTable = null;
  private static ChannelTable mChannelTable = null;

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

  public static ChannelTable channelTable() {
    if (mChannelTable == null)
      mChannelTable = new ChannelTable();

    return mChannelTable;
  }

  public static interface DatabaseTable {
    public String tableName();

    public YouTubeData cursorToItem(Cursor cursor, YouTubeData reuseData);

    public ContentValues contentValuesForItem(YouTubeData item);

    public String tableSQL();

    public String indexSQL();

    public Database.DatabaseQuery queryParams(int queryID, String requestId, String filter);

    public String[] defaultProjection();

    public String orderBy();

  }

  public static DatabaseTable[] tables() {
    return new DatabaseTable[]{DatabaseTables.videoTable(), DatabaseTables.playlistTable(), DatabaseTables
        .channelTable()};
  }

  // =====================================================================
  // =====================================================================

  public static class ChannelTable implements DatabaseTable {
    // stores information about a playlist
    public class Entry implements BaseColumns {
      public static final String COLUMN_NAME_CHANNEL = "channel";
      public static final String COLUMN_NAME_TITLE = "title";
      public static final String COLUMN_NAME_DESCRIPTION = "description";
      public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
    }

    private static ChannelTable singleton = null;

    public static ChannelTable instance() {
      if (singleton == null)
        singleton = new ChannelTable();

      return singleton;
    }

    private ChannelTable() {
      super();
    }

    @Override
    public String tableName() {
      return "channels";
    }

    @Override
    public YouTubeData cursorToItem(Cursor cursor, YouTubeData reuseData) {
      YouTubeData result = reuseData;  // avoiding memory alloc during draw
      if (result == null)
        result = new YouTubeData();

      result.mID = cursor.getLong(cursor.getColumnIndex(Entry._ID));
      result.mTitle = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_TITLE));
      result.mChannel = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_CHANNEL));
      result.mDescription = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_DESCRIPTION));
      result.mThumbnail = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_THUMBNAIL));

      return result;
    }

    @Override
    public ContentValues contentValuesForItem(YouTubeData item) {
      ContentValues values = new ContentValues();

      values.put(Entry.COLUMN_NAME_TITLE, item.mTitle);
      values.put(Entry.COLUMN_NAME_DESCRIPTION, item.mDescription);
      values.put(Entry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);
      values.put(Entry.COLUMN_NAME_CHANNEL, item.mChannel);

      return values;
    }

    @Override
    public String tableSQL() {
      return CREATE + tableName() + " (" + Entry._ID + INT_TYPE + PRIMARY + COMMA_SEP + Entry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_CHANNEL + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_THUMBNAIL + TEXT_TYPE + " )";
    }

    @Override
    public String indexSQL() {
      return null;
    }

    @Override
    public String[] defaultProjection() {
      return new String[]{Entry._ID, Entry.COLUMN_NAME_TITLE, Entry.COLUMN_NAME_DESCRIPTION, Entry.COLUMN_NAME_CHANNEL, Entry.COLUMN_NAME_THUMBNAIL,};
    }

    @Override
    public String orderBy() {
      return null;
    }

    @Override
    public Database.DatabaseQuery queryParams(int queryID, String requestId, String filter) {
      String selection = null;
      String[] selectionArgs = null;
      String[] projection = defaultProjection();

      // requestId is the channel id
      if (requestId != null) {
        selectionArgs = new String[]{requestId};

        if (selection == null)
          selection = "";
        else
          selection += " AND ";

        selection += Entry.COLUMN_NAME_CHANNEL + " = ?";
      }

      return new Database.DatabaseQuery(tableName(), selection, selectionArgs, projection, orderBy());
    }
  }

  // =====================================================================
  // =====================================================================

  public static class PlaylistTable implements DatabaseTable {
    // stores information about a playlist
    public class Entry implements BaseColumns {
      public static final String COLUMN_NAME_REQUEST = "request";
      public static final String COLUMN_NAME_PLAYLIST = "playlist";
      public static final String COLUMN_NAME_TITLE = "title";
      public static final String COLUMN_NAME_DESCRIPTION = "description";
      public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
      public static final String COLUMN_NAME_ITEM_COUNT = "itemCount";
      public static final String COLUMN_NAME_PUBLISHED_DATE = "published_date";
      public static final String COLUMN_NAME_HIDDEN = "hidden";
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

      int col;

      col = cursor.getColumnIndex(Entry._ID);
      if (col != -1)
        result.mID = cursor.getLong(cursor.getColumnIndex(Entry._ID));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_REQUEST);
      if (col != -1)
        result.mRequest = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_REQUEST));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_PLAYLIST);
      if (col != -1)
        result.mPlaylist = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_PLAYLIST));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_TITLE);
      if (col != -1)
        result.mTitle = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_TITLE));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_DESCRIPTION);
      if (col != -1)
        result.mDescription = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_DESCRIPTION));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_THUMBNAIL);
      if (col != -1)
        result.mThumbnail = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME_THUMBNAIL));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_ITEM_COUNT);
      if (col != -1)
        result.mItemCount = cursor.getLong(cursor.getColumnIndex(Entry.COLUMN_NAME_ITEM_COUNT));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_PUBLISHED_DATE);
      if (col != -1)
        result.mPublishedDate = cursor.getLong(cursor.getColumnIndex(Entry.COLUMN_NAME_PUBLISHED_DATE));

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_HIDDEN);
      if (col != -1)
        result.setHidden(cursor.getString(col) != null);

      return result;
    }

    @Override
    public ContentValues contentValuesForItem(YouTubeData item) {
      ContentValues values = new ContentValues();

      values.put(Entry.COLUMN_NAME_REQUEST, item.mRequest);
      values.put(Entry.COLUMN_NAME_PLAYLIST, item.mPlaylist);
      values.put(Entry.COLUMN_NAME_TITLE, item.mTitle);
      values.put(Entry.COLUMN_NAME_DESCRIPTION, item.mDescription);
      values.put(Entry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);
      values.put(Entry.COLUMN_NAME_ITEM_COUNT, item.mItemCount);
      values.put(Entry.COLUMN_NAME_PUBLISHED_DATE, item.mPublishedDate);
      values.put(Entry.COLUMN_NAME_HIDDEN, item.isHidden() ? "" : null);

      return values;
    }

    @Override
    public String tableSQL() {
      return CREATE + tableName() + " (" + Entry._ID + INT_TYPE + PRIMARY + COMMA_SEP + Entry.COLUMN_NAME_REQUEST + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_PLAYLIST + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_THUMBNAIL + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_ITEM_COUNT + INT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_PUBLISHED_DATE + INT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_HIDDEN + TEXT_TYPE  // this is string since we use null or not null like a boolean, getInt returns 0 for null which makes it more complex to deal with null, 0, or 1.
          + " )";
    }

    @Override
    public String indexSQL() {
      return null;
    }

    @Override
    public String[] defaultProjection() {
      return new String[]{Entry._ID, Entry.COLUMN_NAME_REQUEST, Entry.COLUMN_NAME_PLAYLIST, Entry.COLUMN_NAME_TITLE, Entry.COLUMN_NAME_DESCRIPTION, Entry.COLUMN_NAME_THUMBNAIL, Entry.COLUMN_NAME_ITEM_COUNT, Entry.COLUMN_NAME_PUBLISHED_DATE, Entry.COLUMN_NAME_HIDDEN};
    }

    @Override
    public String orderBy() {
      return Entry.COLUMN_NAME_PUBLISHED_DATE + " DESC";
    }

    @Override
    public Database.DatabaseQuery queryParams(int queryID, String requestId, String filter) {
      String[] hiddenProjection = new String[]{Entry._ID, Entry.COLUMN_NAME_REQUEST, Entry.COLUMN_NAME_PLAYLIST, Entry.COLUMN_NAME_HIDDEN};

      return standardQueryParams(queryID, requestId, filter, this, Entry.COLUMN_NAME_HIDDEN, Entry._ID, Entry.COLUMN_NAME_REQUEST, Entry.COLUMN_NAME_TITLE, Entry.COLUMN_NAME_DESCRIPTION, hiddenProjection);
    }
  }

  // =====================================================================
  // =====================================================================

  public static class VideoTable implements DatabaseTable {
    // stores information about a video
    public class Entry implements BaseColumns {
      public static final String COLUMN_NAME_REQUEST = "request";
      public static final String COLUMN_NAME_VIDEO = "video";
      public static final String COLUMN_NAME_TITLE = "title";
      public static final String COLUMN_NAME_DESCRIPTION = "description";
      public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
      public static final String COLUMN_NAME_DURATION = "duration";
      public static final String COLUMN_NAME_PUBLISHED_DATE = "published_date";
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

      col = cursor.getColumnIndex(Entry._ID);
      if (col != -1)
        result.mID = cursor.getLong(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_REQUEST);
      if (col != -1)
        result.mRequest = cursor.getString(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_VIDEO);
      if (col != -1)
        result.mVideo = cursor.getString(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_TITLE);
      if (col != -1)
        result.mTitle = cursor.getString(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_DESCRIPTION);
      if (col != -1)
        result.mDescription = cursor.getString(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_THUMBNAIL);
      if (col != -1)
        result.mThumbnail = cursor.getString(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_DURATION);
      if (col != -1)
        result.mDuration = cursor.getString(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_PUBLISHED_DATE);
      if (col != -1)
        result.mPublishedDate = cursor.getLong(col);

      col = cursor.getColumnIndex(Entry.COLUMN_NAME_HIDDEN);
      if (col != -1)
        result.setHidden(cursor.getString(col) != null);

      return result;
    }

    @Override
    public ContentValues contentValuesForItem(YouTubeData item) {
      ContentValues values = new ContentValues();

      values.put(Entry.COLUMN_NAME_VIDEO, item.mVideo);
      values.put(Entry.COLUMN_NAME_REQUEST, item.mRequest);
      values.put(Entry.COLUMN_NAME_TITLE, item.mTitle);
      values.put(Entry.COLUMN_NAME_DESCRIPTION, item.mDescription);
      values.put(Entry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);
      values.put(Entry.COLUMN_NAME_DURATION, item.mDuration);
      values.put(Entry.COLUMN_NAME_PUBLISHED_DATE, item.mPublishedDate);
      values.put(Entry.COLUMN_NAME_HIDDEN, item.isHidden() ? "" : null);

      return values;
    }

    @Override
    public String indexSQL() {
      String indexName = Entry.COLUMN_NAME_VIDEO + "_idx";

      return "CREATE INDEX " + indexName + " on " + tableName() + "(" + Entry.COLUMN_NAME_VIDEO + ")";
    }

    @Override
    public String tableSQL() {
      return CREATE + tableName() + " (" + Entry._ID + INT_TYPE + PRIMARY + COMMA_SEP + Entry.COLUMN_NAME_REQUEST + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_VIDEO + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_THUMBNAIL + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_DURATION + TEXT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_PUBLISHED_DATE + INT_TYPE + COMMA_SEP + Entry.COLUMN_NAME_HIDDEN + TEXT_TYPE  // this is string since we use null or not null like a boolean, getInt returns 0 for null which makes it more complex to deal with null, 0, or 1.
          + " )";
    }

    @Override
    public String[] defaultProjection() {
      return new String[]{Entry._ID, Entry.COLUMN_NAME_REQUEST, Entry.COLUMN_NAME_VIDEO, Entry.COLUMN_NAME_TITLE, Entry.COLUMN_NAME_DESCRIPTION, Entry.COLUMN_NAME_THUMBNAIL, Entry.COLUMN_NAME_DURATION, Entry.COLUMN_NAME_PUBLISHED_DATE, Entry.COLUMN_NAME_HIDDEN};
    }

    @Override
    public String orderBy() {
      return Entry.COLUMN_NAME_PUBLISHED_DATE + " DESC";
    }

    @Override
    public Database.DatabaseQuery queryParams(int queryID, String requestId, String filter) {
      String[] hiddenProjection = new String[]{Entry._ID, Entry.COLUMN_NAME_REQUEST, Entry.COLUMN_NAME_VIDEO, Entry.COLUMN_NAME_HIDDEN};

      return standardQueryParams(queryID, requestId, filter, this, Entry.COLUMN_NAME_HIDDEN, Entry._ID, Entry.COLUMN_NAME_REQUEST, Entry.COLUMN_NAME_TITLE, Entry.COLUMN_NAME_DESCRIPTION, hiddenProjection);
    }
  }

  public static Database.DatabaseQuery standardQueryParams(int queryID, String requestId, String filter, DatabaseTable table, String HIDDEN_COL, String ID_COL, String REQUEST_COL, String TITLE_COL, String DESC_COL, String[] hiddenProjection) {
    String selection = null;
    String[] selectionArgs = null;
    String[] projection = table.defaultProjection();

    switch (queryID) {
      case HIDDEN_ITEMS:
        selection = "" + HIDDEN_COL + " IS NOT NULL";

        projection = hiddenProjection;
        break;
      case VISIBLE_ITEMS:
        selection = "" + HIDDEN_COL + " IS NULL";
        break;
      case ALL_ITEMS:
        break;
    }

    if (filter != null) {
      if (selection == null)
        selection = "";
      else
        selection += " AND ";

      // single quotes must be doubled up
      filter = filter.replace("'", "''");

      selection += "(" + TITLE_COL + " LIKE '%" + filter + "%'";
      selection += " OR " + DESC_COL + " LIKE '%" + filter + "%'" + ")";
    }

    if (requestId != null) {
      selectionArgs = new String[]{requestId};

      if (selection == null)
        selection = "";
      else
        selection += " AND ";

      selection += REQUEST_COL + " = ?";
    }

    return new Database.DatabaseQuery(table.tableName(), selection, selectionArgs, projection, table
        .orderBy());
  }
}
