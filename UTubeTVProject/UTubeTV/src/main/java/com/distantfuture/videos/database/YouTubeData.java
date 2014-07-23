package com.distantfuture.videos.database;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class YouTubeData {
  // is this faster?  no idea
  private static final String mNotNull = "";
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
  public long mItemCount;  // number of videos in a playlist
  // use convenience methods
  private String mHidden;

  // ----------------------------------------------------
  // public methods

  public static List<YouTubeData> sortByDate(List<YouTubeData> videoIDs) {
    Collections.sort(videoIDs, new Comparator<YouTubeData>() {
      public int compare(YouTubeData lhs, YouTubeData rhs) {
        return (int) (lhs.mPublishedDate - rhs.mPublishedDate);
      }
    });

    return videoIDs;
  }

  public static List<YouTubeData> sortByTitle(List<YouTubeData> videoIDs) {
    Collections.sort(videoIDs, new Comparator<YouTubeData>() {
      public int compare(YouTubeData lhs, YouTubeData rhs) {
        return lhs.mTitle.compareTo(rhs.mTitle);
      }
    });

    return videoIDs;
  }

// ----------------------------------------------------
  // static helper functions

  // video, channel or playlist ids
  public static List<String> contentIdsList(List<YouTubeData> videoData) {
    List<String> result = new ArrayList<String>(videoData.size());

    for (YouTubeData data : videoData) {
      if (data.mVideo != null)
        result.add(data.mVideo);
      else if (data.mPlaylist != null)
        result.add(data.mPlaylist);
      else if (data.mChannel != null)
        result.add(data.mChannel);
    }

    return result;
  }

  public static Bundle toBundle(YouTubeData data) {
    Bundle result = new Bundle();
    result.putString("title", data.mTitle);
    result.putString("description", data.mDescription);
    result.putString("channel", data.mChannel);
    result.putString("thumbnail", data.mThumbnail);
    result.putString("hidden", data.mHidden);
    result.putString("request", data.mRequest);
    result.putString("video", data.mVideo);
    result.putString("duration", data.mDuration);
    result.putString("playlist", data.mPlaylist);

    result.putLong("itemCount", data.mItemCount);
    result.putLong("id", data.mID);
    result.putLong("publishedDate", data.mPublishedDate);

    return result;
  }

  public static YouTubeData fromBundle(Bundle bundle) {
    YouTubeData result = new YouTubeData();

    result.mTitle = bundle.getString("title");
    result.mDescription = bundle.getString("description");
    result.mChannel = bundle.getString("channel");
    result.mThumbnail = bundle.getString("thumbnail");
    result.mHidden = bundle.getString("hidden");
    result.mRequest = bundle.getString("request");
    result.mVideo = bundle.getString("video");
    result.mDuration = bundle.getString("duration");
    result.mPlaylist = bundle.getString("playlist");

    result.mItemCount = bundle.getLong("itemCount");
    result.mID = bundle.getLong("id");
    result.mPublishedDate = bundle.getLong("publishedDate");

    return result;
  }

  // hidden string is either '' or null,
  public boolean isHidden() {
    return mHidden != null;
  }

  public void setHidden(boolean hidden) {
    mHidden = hidden ? mNotNull : null;
  }

}
