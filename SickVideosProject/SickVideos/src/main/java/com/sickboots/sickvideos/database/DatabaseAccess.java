package com.sickboots.sickvideos.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
  private Database mDB;
  private DatabaseTables.DatabaseTable mTable;
  private String mRequestIdentifier;
  private Context mContext;

  public DatabaseAccess(Context context, YouTubeServiceRequest request) {
    this(context, request.databaseTable(), request.requestIdentifier());
  }

  public DatabaseAccess(Context context, DatabaseTables.DatabaseTable table, String requestIdentifier) {
    super();

    mDB = Database.instance(context);
    mContext = context.getApplicationContext();
    mRequestIdentifier = requestIdentifier;
    mTable = table;
  }

  public void deleteAllRows() {
    SQLiteDatabase db = mDB.getWritableDatabase();

    try {
      int result = db.delete(mTable.tableName(), mTable.whereClause(DatabaseTables.ALL_ITEMS, mRequestIdentifier), mTable.whereArgs(DatabaseTables.ALL_ITEMS, mRequestIdentifier));

      if (result > 0)
        notifyProviderOfChange();

    } catch (Exception e) {
      Util.log("deleteAllRows exception: " + e.getMessage());
    } finally {
    }
  }

  public void insertItems(List<YouTubeData> items) {
    if (items != null) {
      // Gets the data repository in write mode
      SQLiteDatabase db = mDB.getWritableDatabase();

      db.beginTransaction();
      try {
        for (YouTubeData item : items)
          db.insert(mTable.tableName(), null, mTable.contentValuesForItem(item));

        db.setTransactionSuccessful();

        notifyProviderOfChange();
      } catch (Exception e) {
        Util.log("Insert item exception: " + e.getMessage());
      } finally {
        db.endTransaction();
      }
    }
  }

  public YouTubeData getItemWithID(Long id) {
    YouTubeData result = null;
    Cursor cursor = mDB.getCursor(mTable.tableName(), whereClauseForID(), whereArgsForID(id), mTable.projection(0));

    if (cursor.moveToFirst()) {
      result = mTable.cursorToItem(cursor, null);
    } else {
      Util.log("getItemWithID not found or too many results?");
    }

    cursor.close();

    return result;
  }

  public Cursor getCursor(int flags) {
    return mDB.getCursor(mTable.tableName(), mTable.whereClause(flags, mRequestIdentifier), mTable.whereArgs(flags, mRequestIdentifier), mTable.projection(flags));
  }

  public List<YouTubeData> getItems(int flags) {
    Cursor cursor = getCursor(flags);

    List<YouTubeData> result = getItems(cursor);

    cursor.close();

    return result;
  }

  public void updateItem(YouTubeData item) {
    SQLiteDatabase db = mDB.getWritableDatabase();

    try {
      int result = db.update(mTable.tableName(), mTable.contentValuesForItem(item), whereClauseForID(), whereArgsForID(item.mID));

      if (result != 1)
        Util.log("updateItem didn't return 1");
      else
        notifyProviderOfChange();

    } catch (Exception e) {
      Util.log("updateItem exception: " + e.getMessage());
    } finally {
    }
  }

  // -----------------------------------------------------------------------------
  // private

  private void notifyProviderOfChange() {
    mContext.getContentResolver().notifyChange(YouTubeContentProvider.URI_CONTENTS, null);
  }

  private String whereClauseForID() {
    return "_id=?";
  }

  private String[] whereArgsForID(Long id) {
    return new String[]{id.toString()};
  }

  private List<YouTubeData> getItems(Cursor cursor) {
    List<YouTubeData> result = new ArrayList<YouTubeData>();

    try {
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          result.add(mTable.cursorToItem(cursor, null));
          cursor.moveToNext();
        }
      }
    } catch (Exception e) {
      Util.log("getItems exception: " + e.getMessage());
    } finally {
    }

    return result;
  }
}
