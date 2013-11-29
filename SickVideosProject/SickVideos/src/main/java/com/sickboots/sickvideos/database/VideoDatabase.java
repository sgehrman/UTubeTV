package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

public class VideoDatabase extends BaseDatabase {
  // filter flags
  public static int FILTER_HIDDEN_ITEMS = 10;
  public static int ONLY_HIDDEN_ITEMS = 20;

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
  protected YouTubeData cursorToItem(Cursor cursor) {
    YouTubeData result = new YouTubeData();

    result.mID = cursor.getLong(cursor.getColumnIndex(VideoEntry._ID));
    result.mVideo = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_VIDEO));
    result.mTitle = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_TITLE));
    result.mDescription = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DESCRIPTION));
    result.mThumbnail = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_THUMBNAIL));
    result.mDuration = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_DURATION));
    result.setHidden(cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME_HIDDEN)) != null);

    return result;
  }

  @Override
  protected ContentValues contentValuesForItem(YouTubeData item) {
    ContentValues values = new ContentValues();

    values.put(VideoEntry.COLUMN_NAME_VIDEO, item.mVideo);
    values.put(VideoEntry.COLUMN_NAME_TITLE, item.mTitle);
    values.put(VideoEntry.COLUMN_NAME_DESCRIPTION, item.mDescription);
    values.put(VideoEntry.COLUMN_NAME_THUMBNAIL, item.mThumbnail);
    values.put(VideoEntry.COLUMN_NAME_DURATION, item.mDuration);
    values.put(VideoEntry.COLUMN_NAME_HIDDEN, item.isHidden() ? "" : null);

    return values;
  }

  @Override
  protected String[] tablesSQL() {
    String result = CREATE + mTableName
        + " ("
        + VideoEntry._ID + INT_TYPE + PRIMARY
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

    return new String[] {result};
  }

  @Override
  protected String getItemsWhereClause(int flags) {
    if (flags == FILTER_HIDDEN_ITEMS)
      return "" + VideoEntry.COLUMN_NAME_HIDDEN + " IS NULL";
    else if (flags == ONLY_HIDDEN_ITEMS)
      return "" + VideoEntry.COLUMN_NAME_HIDDEN + " IS NOT NULL";

    return null;
  }

  @Override
  protected String[] getItemsWhereArgs(int flags) {
    return null;
  }

}
