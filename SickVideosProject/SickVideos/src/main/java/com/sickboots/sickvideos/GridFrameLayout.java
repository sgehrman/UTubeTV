package com.sickboots.sickvideos;

import android.content.Context;
import android.util.AttributeSet;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * extends PullToRefreshLayout which is needed for the pull to refresh lib
 */
public class GridFrameLayout extends PullToRefreshLayout {

  public GridFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}

