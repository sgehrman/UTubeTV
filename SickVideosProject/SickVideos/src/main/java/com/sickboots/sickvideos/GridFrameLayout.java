package com.sickboots.sickvideos;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * This was added so we could do a custom sliding animation for fragments.
 * also extends PullToRefreshLayout which is needed for the pull to refresh lib
 */
public class GridFrameLayout extends PullToRefreshLayout {

  public GridFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public float getXFraction() {
    return getX() / getWidth();
  }

  public void setXFraction(float xFraction) {
    final int width = getWidth();
    setX((width > 0) ? (xFraction * width) : -9999);
  }

  public float getYFraction() {
    return getY() / getHeight();
  }

  public void setYFraction(float yFraction) {
    final int height = getHeight();
    setY((height > 0) ? (yFraction * height) : -9999);
  }

}

