package com.sickboots.sickvideos.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
  private Database mDB;
  private DatabaseTables.DatabaseTable mTable;
  private YouTubeServiceRequest mRequest;
  private Context mContext;

  public DatabaseAccess(Context context, YouTubeServiceRequest request) {
    super();

    mDB = Database.instance(context);
    mContext = context;
    mRequest = request;

    switch (request.type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        mTable = DatabaseTables.VideoTable.instance();
        break;
      case PLAYLISTS:
        mTable = DatabaseTables.PlaylistTable.instance();
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

        notifyProviderOfChange();
      } catch (Exception e) {
        Util.log("Insert item exception: " + e.getMessage());
      } finally {
        db.endTransaction();
        db.close();
      }
    }
  }

  private void notifyProviderOfChange() {
    mContext.getContentResolver().notifyChange(
        YouTubeContentProvider.URI_PERSONS, null, false);
  }

  public YouTubeData getItemWithID(Long id) {
    YouTubeData result = null;
    Cursor cursor = getItemsCursor(whereClauseForID(), whereArgsForID(id), mTable.projection(0));

    List<YouTubeData> results = getItems(cursor);

    if (results.size() == 1) {
      result = results.get(0);
    } else {
      Util.log("getItemWithID not found or too many results?");
    }

    return result;
  }

  public Cursor getCursor(int flags) {
    return getItemsCursor(mTable.whereClause(flags, mRequest.requestIdentifier()), mTable.whereArgs(flags, mRequest.requestIdentifier()), mTable.projection(flags));
  }

  public List<YouTubeData> getItems(int flags) {
    return getItems(getCursor(flags));
  }

  public void updateItem(YouTubeData item) {
    SQLiteDatabase db = mDB.getWritableDatabase();

    try {
      Long id = item.mID;

      int result = db.update(mTable.tableName(), mTable.contentValuesForItem(item), whereClauseForID(), whereArgsForID(id));

      if (result != 1)
        Util.log("updateItem didn't return 1");
      else
        notifyProviderOfChange();

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

  private List<YouTubeData> getItems(Cursor cursor) {
    List<YouTubeData> result = new ArrayList<YouTubeData>();

    try {
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

  private Cursor getItemsCursor(String selection, String[] selectionArgs, String[] projection) {
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

}
