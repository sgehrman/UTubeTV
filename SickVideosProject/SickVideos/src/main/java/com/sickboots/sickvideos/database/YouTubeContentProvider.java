package com.sickboots.sickvideos.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.sickboots.sickvideos.misc.Utils;

public class YouTubeContentProvider extends ContentProvider {

  // All URIs share these parts
  public static final String AUTHORITY = "com.sickboots.sickvideos.provider";
  public static final String SCHEME = "content://";

  // URIs
  // Used for all persons
  public static final String CONTENTS = SCHEME + AUTHORITY + "/content";
  public static final Uri URI_CONTENTS = Uri.parse(CONTENTS);
  // Used for a single person, just add the id to the end
  public static final String CONTENT_BASE = CONTENTS + "/";

  public YouTubeContentProvider() {
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    // Implement this to handle requests to delete one or more rows.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public String getType(Uri uri) {
    // TODO: Implement this to handle requests for the MIME type of the data
    // at the given URI.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    // TODO: Implement this to handle requests to insert a new row.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
    Cursor cursor = null;

    if (URI_CONTENTS.equals(uri)) {
      if (sortOrder.equals("pl")) {
        DatabaseAccess access = new DatabaseAccess(getContext(), DatabaseTables.playlistTable());

        cursor = access.getCursor(selection, selectionArgs, projection);
      } else {
        DatabaseAccess access = new DatabaseAccess(getContext(), DatabaseTables.videoTable());

        cursor = access.getCursor(selection, selectionArgs, projection);
      }

      cursor.setNotificationUri(getContext().getContentResolver(), URI_CONTENTS);
    } else if (uri.toString().startsWith(CONTENT_BASE)) {
      final long id = Long.parseLong(uri.getLastPathSegment());
      Utils.log("" + id);
    }

    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    // TODO: Implement this to handle requests to update one or more rows.
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
