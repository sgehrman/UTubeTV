package com.distantfuture.videos.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

import com.distantfuture.videos.misc.DUtils;

public class CursorFactoryDebugger implements SQLiteDatabase.CursorFactory {

  private boolean debugQueries = false;

  public CursorFactoryDebugger(boolean debugQueries) {
    super();

    this.debugQueries = debugQueries;
  }

  @Override
  public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
    if (debugQueries) {
      DUtils.log(query.toString());
    }
    return new SQLiteCursor(masterQuery, editTable, query);
  }
}