package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.database.Cursor;

public interface DatabaseTable {
  public static final String CREATE = "CREATE TABLE ";
  public static final String TEXT_TYPE = " TEXT";
  public static final String INT_TYPE = " INTEGER";
  public static final String PRIMARY = " PRIMARY KEY";
  public static final String COMMA_SEP = ",";

  public String tableName();

  public String[] projection(int flags);

  public YouTubeData cursorToItem(Cursor cursor);

  public ContentValues contentValuesForItem(YouTubeData item);

  public String[] tablesSQL();

  public String whereClause(int flags);

  public String[] whereArgs(int flags);
}

