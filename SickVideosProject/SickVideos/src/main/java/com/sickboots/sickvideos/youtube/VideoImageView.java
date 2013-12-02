package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sickboots.sickvideos.misc.Util;

public class VideoImageView extends ImageView {
  private boolean mDrawShadows = false;
  private int mCachedWidth = 0;

  // gradients shared between all views to cut down on memory allocations etc.
  private static GradientDrawable sTopGradient;
  private static GradientDrawable sBottomGradient;
  private static int sGradientHeight;

  public VideoImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDrawShadows(boolean set) {
    mDrawShadows = set;
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mDrawShadows) {
      createGradients(getContext());
      adjustGradientRects();

      sTopGradient.draw(canvas);

      int y = getHeight() - sGradientHeight;
      canvas.translate(0, y);
      sBottomGradient.draw(canvas);
      canvas.translate(0, -y);
    }
  }

  private void adjustGradientRects() {
    if (mCachedWidth != getWidth()) {
      mCachedWidth = getWidth();

      Rect rect = new Rect(0, 0, mCachedWidth, sGradientHeight);

      sTopGradient.setBounds(rect);
      sBottomGradient.setBounds(rect);
    }
  }

  private void createGradients(Context context) {
    if (sTopGradient == null) {
      sGradientHeight = (int) Util.dpToPx(40.0f, context);

      int topColors[] = {0xaa000000, 0x00000000};
      sTopGradient = createGradient(topColors);

      int bottomColors[] = {0x00000000, 0xaa000000};
      sBottomGradient = createGradient(bottomColors);
    }
  }

  private GradientDrawable createGradient(int colors[]) {
    GradientDrawable result = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    result.setShape(GradientDrawable.RECTANGLE);

    return result;
  }

}
