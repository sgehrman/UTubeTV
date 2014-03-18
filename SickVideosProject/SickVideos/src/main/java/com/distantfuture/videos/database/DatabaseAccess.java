package com.distantfuture.videos.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.distantfuture.videos.misc.DUtils;
import com.distantfuture.videos.services.YouTubeServiceRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseAccess {
  private Database mDB;
  private DatabaseTables.DatabaseTable mTable;
  private String mRequestIdentifier;
  private Context mContext;

  public DatabaseAccess(Context context, YouTubeServiceRequest request) {
    this(context, request.databaseTable());
  }

  public DatabaseAccess(Context context, DatabaseTables.DatabaseTable table) {
    super();

    mDB = Database.instance(context);
    mContext = context.getApplicationContext();
    mTable = table;
  }

  public void deleteAllRows(String requestIdentifier) {
    SQLiteDatabase db = mDB.getWritableDatabase();

    try {
      Database.DatabaseQuery queryParams = mTable.queryParams(DatabaseTables.ALL_ITEMS, requestIdentifier, null);

      int result = db.delete(mTable.tableName(), queryParams.mSelection, queryParams.mSelectionArgs);

      if (result > 0)
        notifyProviderOfChange();

    } catch (Exception e) {
      DUtils.log("deleteAllRows exception: " + e.getMessage());
    }
  }

  public void deleteItem(Long id) {
    SQLiteDatabase db = mDB.getWritableDatabase();

    try {
      int result = db.delete(mTable.tableName(), whereClauseForID(), whereArgsForID(id));

      if (result > 0)
        notifyProviderOfChange();

    } catch (Exception e) {
      DUtils.log("deleteItem exception: " + e.getMessage());
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
        DUtils.log("Insert item exception: " + e.getMessage());
      } finally {
        db.endTransaction();
      }
    }
  }

  public YouTubeData getItemWithID(Long id) {
    YouTubeData result = null;

    Database.DatabaseQuery query = new Database.DatabaseQuery(mTable.tableName(), whereClauseForID(), whereArgsForID(id), mTable
        .defaultProjection(), mTable.orderBy());
    Cursor cursor = mDB.getCursor(query);

    if (cursor.moveToFirst()) {
      result = mTable.cursorToItem(cursor, null);
    } else {
      DUtils.log("getItemWithID not found or too many results?");
    }

    cursor.close();

    return result;
  }

  public Cursor getCursor(int flags, String requestIdentifier) {
    Database.DatabaseQuery query = mTable.queryParams(flags, requestIdentifier, null);

    return mDB.getCursor(query);
  }

  public Cursor getCursor(String selection, String[] selectionArgs, String[] projection) {
    Database.DatabaseQuery query = new Database.DatabaseQuery(mTable.tableName(), selection, selectionArgs, projection, mTable
        .orderBy());

    return mDB.getCursor(query);
  }

  public List<YouTubeData> getItems(int flags, String requestIdentifier, int maxResults) {
    Cursor cursor = getCursor(flags, requestIdentifier);

    List<YouTubeData> result = getItems(cursor, maxResults);

    cursor.close();

    return result;
  }

  public void updateItems(List<YouTubeData> items) {
    SQLiteDatabase db = mDB.getWritableDatabase();

    try {
      for (YouTubeData theItem : items) {
        int result = db.update(mTable.tableName(), mTable.contentValuesForItem(theItem), whereClauseForID(), whereArgsForID(theItem.mID));

        if (result != 1)
          DUtils.log("updateItem didn't return 1");
      }

      notifyProviderOfChange();
    } catch (Exception e) {
      DUtils.log("updateItem exception: " + e.getMessage());
    }

  }

  public void updateItem(YouTubeData item) {
    updateItems(Arrays.asList(item));
  }

  // -----------------------------------------------------------------------------
  // private

  private void notifyProviderOfChange() {
    mContext.getContentResolver().notifyChange(YouTubeContentProvider.contentsURI(mContext), null);
  }

  private String whereClauseForID() {
    return "_id=?";
  }

  private String[] whereArgsForID(Long id) {
    return new String[]{id.toString()};
  }

  // pass 0 to maxResults if you don't care
  private List<YouTubeData> getItems(Cursor cursor, int maxResults) {
    List<YouTubeData> result = new ArrayList<YouTubeData>();
    boolean stopOnMaxResults = maxResults > 0;

    try {
      int cnt = 0;

      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          result.add(mTable.cursorToItem(cursor, null));

          if (stopOnMaxResults) {
            if (++cnt == maxResults)
              break;
          }

          cursor.moveToNext();
        }
      }
    } catch (Exception e) {
      DUtils.log("getItems exception: " + e.getMessage());
    }

    return result;
  }
}
