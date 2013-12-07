package com.sickboots.sickvideos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.misc.Util;

import java.util.ArrayList;
import java.util.List;

public class BaseDatabase extends SQLiteOpenHelper {
  protected static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
  protected static final int DATABASE_VERSION = 1000;
  protected DatabaseTable mTable;

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

    public String getItemsWhereClause(int flags);

    public String[] getItemsWhereArgs(int flags);
  }

  public BaseDatabase(Context context, String databaseName, DatabaseTable table) {
    super(context, databaseName.toLowerCase() + ".db", new CursorFactoryDebugger(false), DATABASE_VERSION);

    mTable = table;
  }

  public void onCreate(SQLiteDatabase db) {
    String[] tables = mTable.tablesSQL();

    for (String table : tables)
      db.execSQL(table);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    db.execSQL(DROP_TABLE + mTable.tableName());
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public void deleteAllRows() {
    SQLiteDatabase db = getWritableDatabase();

    try {
      db.delete(mTable.tableName(), null, null);
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
          db.insert(mTable.tableName(), null, mTable.contentValuesForItem(item));

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
    List<YouTubeData> results = getItems(whereClauseForID(), whereArgsForID(id), mTable.projection(0));

    if (results.size() == 1) {
      result = results.get(0);
    } else {
      Util.log("getItemWithID not found or too many results?");
    }

    return result;
  }

  public List<YouTubeData> getItems(int flags) {
    return getItems(mTable.getItemsWhereClause(flags), mTable.getItemsWhereArgs(flags), mTable.projection(flags));
  }

  public void updateItem(YouTubeData item) {
    SQLiteDatabase db = getWritableDatabase();

    try {
      Long id = item.mID;

      int result = db.update(mTable.tableName(), mTable.contentValuesForItem(item), whereClauseForID(), whereArgsForID(id));

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
          mTable.tableName(),                     // The table to query
          projection,                     // The columns to return
          selection,                      // The columns for the WHERE clause
          selectionArgs,                  // The values for the WHERE clause
          null,                           // don't group the rows
          null,                           // don't filter by row groups
          null                            // The sort order
      );

      cursor.moveToFirst();
      while (!cursor.isAfterLast()) {
        result.add(mTable.cursorToItem(cursor));
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
