package com.sickboots.sickvideos.database;

/**
 * Created by sgehrman on 11/26/13.
 */
public class YouTubeData {
  // raw access for speed
  public long mID;
  public String mTitle;
  public String mDescription;
  public String mThumbnail;

  // used for videos
  public String mVideo;
  public String mDuration;
  public int mStart;

  // used only for subscriptions and categories
  public String mChannel;

  // used for playlists
  public String mPlaylist;

  // use convenience methods
  private String mHidden;

  // is this faster?  no idea
  private static final String mNotNull = "";

  // ----------------------------------------------------
  // public methods

  // hidden string is either '' or null,
  public boolean isHidden() {
    return mHidden != null;
  }

  public void setHidden(boolean hidden) {
    mHidden = hidden ? mNotNull : null;
  }
}
