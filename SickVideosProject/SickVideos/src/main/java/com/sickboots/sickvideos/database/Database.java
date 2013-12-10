package com.sickboots.sickvideos.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.misc.Util;

public class Database extends SQLiteOpenHelper {
  private static Database singleton = null;

  private static final int DATABASE_VERSION = 42;
  private static final String DATABASE_NAME = "database.db";

  private final DatabaseTables mTables = new DatabaseTables();

  public static Database instance(Context context) {
    if (singleton == null) {
      singleton = new Database(context);
    }

    return singleton;
  }

  // private, use instance() singleton above
  private Database(Context context) {
    super(context, DATABASE_NAME, new CursorFactoryDebugger(false), DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db) {
    for (DatabaseTables.DatabaseTable table : mTables.tables())
      db.execSQL(table.tableSQL());
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    // don't upgrade, just drop and start over
    for (DatabaseTables.DatabaseTable table : mTables.tables())
      db.execSQL(DROP_TABLE + table.tableName());

    // recreate
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public Cursor geCursor(String tableName, String selection, String[] selectionArgs, String[] projection) {
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = null;

    try {
      cursor = db.query(
          tableName,                     // The table to query
          projection,                     // The columns to return
          selection,                      // The columns for the WHERE clause
          selectionArgs,                  // The values for the WHERE clause
          null,                           // don't group the rows
          null,                           // don't filter by row groups
          null                            // The sort order
      );

    } catch (Exception e) {
      Util.log("getItemsCursor exception: " + e.getMessage());
    } finally {
    }

    return cursor;
  }
}


