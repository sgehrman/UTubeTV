package com.sickboots.sickvideos.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sickboots.sickvideos.misc.Debug;

import java.io.File;

public class Database extends SQLiteOpenHelper {
  private static Database singleton = null;

  private static final int DATABASE_VERSION = 5010;
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

    boolean debugInfo = false;
    if (debugInfo) {
      final String path = context.getDatabasePath(DATABASE_NAME).getPath();
      File file = new File(path);
      Debug.log(path + " size: " + ((float) (file.length()) / 1024.f) + "k");
    }
  }

  public void onCreate(SQLiteDatabase db) {
    for (DatabaseTables.DatabaseTable table : DatabaseTables.tables()) {
      db.execSQL(table.tableSQL());

      // not every table defines an index, check for null
      String indexSQL = table.indexSQL();
      if (indexSQL != null)
        db.execSQL(indexSQL);
    }
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    // don't upgrade, just drop and start over
    for (DatabaseTables.DatabaseTable table : DatabaseTables.tables())
      db.execSQL(DROP_TABLE + table.tableName());

    // recreate
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public Cursor getCursor(DatabaseQuery query) {
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = null;

    try {
      cursor = db.query(query.mTable,                     // The table to query
          query.mProjection,                     // The columns to return
          query.mSelection,                      // The columns for the WHERE clause
          query.mSelectionArgs,                  // The values for the WHERE clause
          null,                           // don't group the rows
          null,                           // don't filter by row groups
          query.mOrderBy                            // The sort order
      );

    } catch (Exception e) {
      Debug.log("Database.getCursor exception: " + e.getMessage());
    }

    return cursor;
  }

  public static class DatabaseQuery {
    public String mTable;
    public String mSelection;
    public String[] mSelectionArgs;
    public String[] mProjection;
    public String mOrderBy;

    public DatabaseQuery(String table, String selection, String[] selectionArgs, String[] projection, String orderBy) {
      super();

      mTable = table;
      mSelection = selection;
      mSelectionArgs = selectionArgs;
      mProjection = projection;
      mOrderBy = orderBy;
    }
  }

}


