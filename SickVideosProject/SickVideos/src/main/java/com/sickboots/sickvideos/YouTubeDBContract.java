package com.sickboots.sickvideos;

import android.provider.BaseColumns;

public final class YouTubeDBContract {
  // To prevent someone from accidentally instantiating the contract class,
  // give it an empty constructor.
  public YouTubeDBContract() {
  }

  /* Inner class that defines the table contents */
  public static class VideoEntry implements BaseColumns {
    public static final String TABLE_NAME = "videos";
    public static final String COLUMN_NAME_VIDEO = "video";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_THUMBNAIL = "thumbnail";
    public static final String COLUMN_NAME_DURATION = "duration";
  }
}

