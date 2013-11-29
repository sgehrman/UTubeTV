package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.misc.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseDatabase extends SQLiteOpenHelper {
  protected String mItemTable;

  protected static final String CREATE = "CREATE TABLE ";
  protected static final String TEXT_TYPE = " TEXT";
  protected static final String INT_TYPE = " INTEGER";
  protected static final String PRIMARY = " PRIMARY KEY";
  protected static final String COMMA_SEP = ",";
  protected static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
  protected static final int DATABASE_VERSION = 1000;

  // subclasses must take care of this shit
  abstract protected String[] projection(int flags);

  abstract protected YouTubeData cursorToItem(Cursor cursor);

  abstract protected ContentValues contentValuesForItem(YouTubeData item);

  abstract protected String[] tablesSQL();

  abstract protected String getItemsWhereClause(int flags);

  abstract protected String[] getItemsWhereArgs(int flags);

  public BaseDatabase(Context context, String databaseName) {
    super(context, databaseName.toLowerCase() + ".db", new CursorFactoryDebugger(true), DATABASE_VERSION);

    mItemTable = "item_table";
  }

  public void onCreate(SQLiteDatabase db) {
    String[] tables = tablesSQL();

    for (String table : tables)
      db.execSQL(table);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    db.execSQL(DROP_TABLE + mItemTable);
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public void deleteAllRows() {
    SQLiteDatabase db = getWritableDatabase();

    try {
      db.delete(mItemTable, null, null);
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
          db.insert(mItemTable, null, contentValuesForItem(item));

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
    List<YouTubeData> results = getItems(whereClauseForID(), whereArgsForID(id),  projection(0));

    if (results.size() == 1) {
      result = results.get(0);
    } else {
      Util.log("getItemWithID not found or too many results?");
    }

    return result;
  }

  public List<YouTubeData> getItems(int flags) {
    return getItems(getItemsWhereClause(flags), getItemsWhereArgs(flags), projection(flags));
  }

  public void updateItem(YouTubeData item) {
    SQLiteDatabase db = getWritableDatabase();

    try {
      Long id = item.mID;

      int result = db.update(mItemTable, contentValuesForItem(item), whereClauseForID(), whereArgsForID(id));

      if (result != 1)
        Util.log("updateItem didn't return 1");
    } catch (Exception e) {
      Util.log("updateItem exception: " + e.getMessage());
    } finally {
      db.close();
    }
  }

  // -----------------------------------------------------------------------------
  // private

  private String whereClauseForID() {
    return "_id=?";
  }

  private String[] whereArgsForID(Long id) {
    return new String[]{id.toString()};
  }

  private List<YouTubeData> getItems(String selection, String[] selectionArgs, String[] projection) {
    List<YouTubeData> result = new ArrayList<YouTubeData>();

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = null;

    try {
      cursor = db.query(
          mItemTable,                     // The table to query
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

}
