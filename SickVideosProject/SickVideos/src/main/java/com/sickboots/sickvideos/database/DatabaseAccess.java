package com.sickboots.sickvideos.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.DrawerActivity;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
  private Database mDB;
  private DatabaseTables.DatabaseTable mTable;
  private YouTubeServiceRequest mRequest;

  public DatabaseAccess(Context context, YouTubeServiceRequest request) {
    super();

    mDB = Database.instance(context);
    mRequest = request;

    DatabaseTables.DatabaseTable table = null;
    switch (request.type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        mTable = new DatabaseTables.VideoTable();
        break;
      case PLAYLISTS:
        mTable = new DatabaseTables.PlaylistTable();
        break;
      case CATEGORIES:
        break;
    }
  }

  public void deleteAllRows() {
    SQLiteDatabase db = mDB.getWritableDatabase();

    try {
      db.delete(mTable.tableName(), mTable.whereClause(DatabaseTables.DELETE_ALL_ITEMS, mRequest.requestIdentifier()), mTable.whereArgs(DatabaseTables.DELETE_ALL_ITEMS, mRequest.requestIdentifier()));
    } catch (Exception e) {
      Util.log("deleteAllRows exception: " + e.getMessage());
    } finally {
      db.close();
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
    return getItems(mTable.whereClause(flags, mRequest.requestIdentifier()), mTable.whereArgs(flags, mRequest.requestIdentifier()), mTable.projection(flags));
  }

  public void updateItem(YouTubeData item) {
    SQLiteDatabase db = mDB.getWritableDatabase();

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

  // called publicly by ContentProvider
  public Cursor getItemsCursor(String selection, String[] selectionArgs, String[] projection) {
    SQLiteDatabase db = mDB.getReadableDatabase();
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

    } catch (Exception e) {
      Util.log("getItemsCursor exception: " + e.getMessage());
    } finally {
    }

    return cursor;
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

    Cursor cursor = null;
    try {
      cursor = getItemsCursor(selection, selectionArgs, projection);

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
    }

    return result;
  }
}
