package com.sickboots.sickvideos.database;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.google.api.client.util.DateTime;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by sgehrman on 11/26/13.
 */
public class YouTubeData {
  private static final DateFormat sDateFormatter = DateFormat.getDateInstance();
//  private static final PrettyTime sDateFormatter = new PrettyTime();

  // raw access for speed
  public long mID;
  public String mRequest;
  public String mTitle;
  public String mDescription;
  public String mThumbnail;

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
  private Date mPublishedDate;

  // is this faster?  no idea
  private static final String mNotNull = "";

  // not saved in database, set when read from database
  // don't want to convert date when drawing, so it's set in setPublishedDate
  public Spannable mPublishedDateString;

  // ----------------------------------------------------
  // public methods

  // hidden string is either '' or null,
  public boolean isHidden() {
    return mHidden != null;
  }

  public void setHidden(boolean hidden) {
    mHidden = hidden ? mNotNull : null;
  }

  public void setPublishedDate(Date date) {
    mPublishedDate = date;

    String title = "Published: ";
    String content = sDateFormatter.format(date);

    mPublishedDateString = new SpannableString(title+content);
    mPublishedDateString.setSpan( new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    mPublishedDateString.setSpan( new ForegroundColorSpan(0xffb1e2ff), title.length(), title.length() + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  public Date getPublishedDate() {
    return mPublishedDate;
  }

  public Spannable getPublishedDateString() {
    return mPublishedDateString;
  }
}
