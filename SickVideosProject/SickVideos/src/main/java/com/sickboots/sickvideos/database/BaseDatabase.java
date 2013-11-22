package com.sickboots.sickvideos.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.misc.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseDatabase extends SQLiteOpenHelper {
  String mTableName;
  protected static final int DATABASE_VERSION = 100;

  // subclasses must take care of this shit
  protected abstract String[] projection();

  abstract protected Map cursorToItem(Cursor cursor);

  abstract protected void insertItem(SQLiteDatabase db, Map video);

  abstract protected String createTableSQL();

  public BaseDatabase(Context context, String databaseName) {
    super(context, databaseName.toLowerCase() + ".db", null, DATABASE_VERSION);

    mTableName = "item_table";
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(createTableSQL());
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    db.execSQL("DROP TABLE IF EXISTS " + mTableName);
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public void deleteAllRows() {
    SQLiteDatabase db = getWritableDatabase();

    db.delete(mTableName, null, null);
  }

  public void insertItems(List<Map> videos) {
    if (videos != null) {
      // Gets the data repository in write mode
      SQLiteDatabase db = getWritableDatabase();

      db.beginTransaction();
      try {
        for (Map video : videos)
          insertItem(db, video);

        db.setTransactionSuccessful();
      } catch (Exception e) {
        Util.log("Insert Videos exception: " + e.getMessage());
      } finally {
        db.endTransaction();
        db.close();
      }
    }
  }

  public List<Map> getItems() {
    List<Map> result = new ArrayList<Map>();

    SQLiteDatabase db = getReadableDatabase();

    String[] projection = projection();

    Cursor cursor = db.query(
        mTableName,                     // The table to query
        projection,                     // The columns to return
        null,                           // The columns for the WHERE clause
        null,                           // The values for the WHERE clause
        null,                           // don't group the rows
        null,                           // don't filter by row groups
        null                            // The sort order
    );

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      result.add(cursorToItem(cursor));
      cursor.moveToNext();
    }

    cursor.close();
    db.close();

    return result;
  }

}
