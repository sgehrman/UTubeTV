package com.sickboots.sickvideos.database;

/**
 * Created by sgehrman on 11/26/13.
 */
public class YouTubeData {
  // raw access for speed
  public long mID;
  public String mPlaylist;
  public String mChannel;
  public String mVideo;
  public String mTitle;
  public String mDescription;
  public String mThumbnail;
  public String mDuration;

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
