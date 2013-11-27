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
  protected String mTableName;
  protected int mFlags = 0;  // can be used for any flags need in a subclass

  protected static final int DATABASE_VERSION = 501;

  // subclasses must take care of this shit
  abstract protected String[] projection();

  abstract protected YouTubeData cursorToItem(Cursor cursor);

  abstract protected ContentValues contentValuesForItem(YouTubeData item);

  abstract protected String createTableSQL();

  abstract protected String getItemsWhereClause();

  abstract protected String[] getItemsWhereArgs();

  public BaseDatabase(Context context, String databaseName) {
    super(context, databaseName.toLowerCase() + ".db", new CursorFactoryDebugger(true), DATABASE_VERSION);

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

    try {
      db.delete(mTableName, null, null);
    } catch (Exception e) {
      Util.log("deleteAllRows exception: " + e.getMessage());
    } finally {
      db.close();
    }
  }

  public void insertItems(List<YouTubeData> items) {
    if (items != null) {
      // Gets the data repository in write mode
      SQLiteDatabase db = getWritableDatabase();

      db.beginTransaction();
      try {
        for (YouTubeData item : items)
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

  public YouTubeData getItemWithID(Long id) {
    YouTubeData result = null;
    List<YouTubeData> results = getItems(whereClauseForID(), whereArgsForID(id));

    if (results.size() == 1) {
      result = results.get(0);
    } else {
      Util.log("getItemWithID not found or too many results?");
    }

    return result;
  }

  public void setFlags(int flags) {
    mFlags = flags;
  }

  public List<YouTubeData> getItems() {
    return getItems(getItemsWhereClause(), getItemsWhereArgs());
  }

  public List<YouTubeData> getItems(String selection, String[] selectionArgs) {
    List<YouTubeData> result = new ArrayList<YouTubeData>();

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor=null;

    try {
      String[] projection = projection();

      cursor = db.query(
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
    } catch (Exception e) {
      Util.log("getItems exception: " + e.getMessage());
    } finally {
      if (cursor != null)
        cursor.close();

      db.close();
    }

    return result;
  }

  private String whereClauseForID() {
    return "_id=?";
  }

  private String[] whereArgsForID(Long id) {
    return new String[]{id.toString()};
  }

  public void updateItem(YouTubeData item) {
    SQLiteDatabase db = getWritableDatabase();

    try {
      Long id = item.mID;

      int result = db.update(mTableName, contentValuesForItem(item), whereClauseForID(), whereArgsForID(id));

      if (result != 1)
        Util.log("updateItem didn't return 1");
    } catch (Exception e) {
      Util.log("updateItem exception: " + e.getMessage());
    } finally {
      db.close();
    }
  }

}
