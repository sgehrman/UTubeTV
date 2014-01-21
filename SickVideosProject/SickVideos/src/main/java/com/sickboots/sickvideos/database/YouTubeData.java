package com.sickboots.sickvideos.database;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.google.api.client.util.DateTime;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by sgehrman on 11/26/13.
 */
public class YouTubeData {
//  private static final DateFormat sDateFormatter = DateFormat.getDateInstance();
  private static final PrettyTime sDateFormatter = new PrettyTime();
  private static final Date sDate = new Date();  // avoiding an alloc every call, just set the time
  private static final String sTitle = "Published: ";
  private static final StyleSpan sBoldSpan = new StyleSpan(Typeface.BOLD);
  private static final ForegroundColorSpan sColorSpan = new ForegroundColorSpan(0xffb1e2ff);

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
  private long mPublishedDate;

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

  public void setPublishedDate(long date) {
    mPublishedDate = date;

    // avoiding an alloc during draw, reusing Date
    sDate.setTime(date);
    String content = sDateFormatter.format(sDate);

    mPublishedDateString = new SpannableString(sTitle+content);
    mPublishedDateString.setSpan(sBoldSpan, 0, sTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    mPublishedDateString.setSpan(sColorSpan, sTitle.length(), sTitle.length() + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  public long getPublishedDate() {
    return mPublishedDate;
  }

  public Spannable getPublishedDateString() {
    return mPublishedDateString;
  }
}
