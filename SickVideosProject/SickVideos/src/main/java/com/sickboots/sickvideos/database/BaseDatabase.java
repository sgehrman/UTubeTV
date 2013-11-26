package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseDatabase extends SQLiteOpenHelper {
  String mTableName;
  protected static final int DATABASE_VERSION = 102;

  // subclasses must take care of this shit
  abstract protected String[] projection();
  abstract protected Map cursorToItem(Cursor cursor);
  abstract protected ContentValues contentValuesForItem(Map item);
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

  public void insertItems(List<Map> items) {
    if (items != null) {
      // Gets the data repository in write mode
      SQLiteDatabase db = getWritableDatabase();

      db.beginTransaction();
      try {
        for (Map item : items)
          db.insert(mTableName, null, contentValuesForItem(item));

        db.setTransactionSuccessful();
      } catch (Exception e) {
        Util.log("Insert item exception: " + e.getMessage());
      } finally {
        db.endTransaction();
        db.close();
      }
    }
  }

  public Map getItemWithID(Long id) {
    Map result = null;
    List<Map> results = getItems(whereClauseForID(), whereArgsForID(id));

    if (results.size() == 1) {
      result = results.get(0);
    } else {
      Util.log("getItemWithID not found or too many results?");
    }

    return result;
  }

  public List<Map> getItems() {
    return getItems(null, null);
  }

  public List<Map> getItems(String selection, String[] selectionArgs) {
    List<Map> result = new ArrayList<Map>();

    SQLiteDatabase db = getReadableDatabase();

    String[] projection = projection();

    Cursor cursor = db.query(
        mTableName,                     // The table to query
        projection,                     // The columns to return
        selection,                      // The columns for the WHERE clause
        selectionArgs,                  // The values for the WHERE clause
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

  private String whereClauseForID() {
   return "_id=?";
  }

  private String[] whereArgsForID(Long id) {
    return new String[]{ id.toString() };
  }

  public void updateItem(Map item) {
    Long id = (Long) item.get(YouTubeAPI.ID_KEY);

    SQLiteDatabase db = getWritableDatabase();

    int result = db.update(mTableName, contentValuesForItem(item), whereClauseForID(), whereArgsForID(id));

    if (result != 1)
    Util.log("updateItem didn't return 1");
  }

}
