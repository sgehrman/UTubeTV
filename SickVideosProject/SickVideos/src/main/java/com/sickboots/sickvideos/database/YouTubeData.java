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
  //  private static final DateFormat sDateFormatter = DateFormat.getDateInstance();
  private static final PrettyTime sDateFormatter = new PrettyTime();
  private static final Date sDate = new Date();  // avoiding an alloc every call, just set the time
  private static final String sTitle = "Published: ";
  private static final StyleSpan sBoldSpan = new StyleSpan(Typeface.BOLD);
  private static final ForegroundColorSpan sColorSpan = new ForegroundColorSpan(0xffb1e2ff);

  // cached, used as an optimization, getView() 0,0,0, muliple times, so don't keep building this over and over
  private static Spannable sCachedDateSpannable;
  private static long sCachedDate;

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

  public Spannable getPublishedDateString() {
    // cached, used as an optimization, getView() 0,0,0, muliple times, so don't keep building this over and over
    if (sCachedDateSpannable != null) {
      if (sCachedDate != mPublishedDate)
        sCachedDateSpannable = null;
    }

    if (sCachedDateSpannable == null) {
      sCachedDate = mPublishedDate;

      // avoiding an alloc during draw, reusing Date
      sDate.setTime(mPublishedDate);
      String content = sDateFormatter.format(sDate);

      sCachedDateSpannable = new SpannableString(sTitle + content);
      sCachedDateSpannable.setSpan(sBoldSpan, 0, sTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      sCachedDateSpannable.setSpan(sColorSpan, sTitle.length(), sTitle.length() + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    return sCachedDateSpannable;
  }
}
