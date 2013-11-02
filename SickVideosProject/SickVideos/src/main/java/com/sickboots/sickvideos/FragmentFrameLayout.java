package com.sickboots.sickvideos;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by sgehrman on 11/1/13.
 */
  public class FragmentFrameLayout extends FrameLayout {

  public FragmentFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public float getXFraction() {
      return getX() / getWidth();
    }

    public void setXFraction(float xFraction) {
      final int width = getWidth();
      setX((width > 0) ? (xFraction * width) : -9999);
    }
  }

