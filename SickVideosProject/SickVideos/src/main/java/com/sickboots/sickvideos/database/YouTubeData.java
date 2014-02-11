package com.sickboots.sickvideos.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class YouTubeData {
  // raw access for speed
  public long mID;
  public String mRequest;
  public String mTitle;
  public String mDescription;
  public String mThumbnail;
  public long mPublishedDate;

  // used for videos
  public String mVideo;
  public String mDuration;

  // used only for subscriptions and categories and channel info
  public String mChannel;

  // used for playlists
  public String mPlaylist;
  public Long mItemCount;  // number of videos in a playlist

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

  // ----------------------------------------------------
  // static helper functions

  public static List<YouTubeData> sortByDate(List<YouTubeData> videoIDs) {
    Collections.sort(videoIDs, new Comparator<YouTubeData>() {
      public int compare(YouTubeData lhs, YouTubeData rhs) {
        return (int) (lhs.mPublishedDate - rhs.mPublishedDate);
      }
    });

    return videoIDs;
  }

  // video or playlist ids
  public static List<String> contentIdsList(List<YouTubeData> videoData) {
    List<String> result = new ArrayList<String>(videoData.size());

    for (YouTubeData data : videoData) {
      result.add(data.mVideo == null ? data.mPlaylist : data.mVideo);
    }

    return result;
  }

}
