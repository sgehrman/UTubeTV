package com.sickboots.sickvideos.database;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * Created by sgehrman on 11/26/13.
 */
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
}
