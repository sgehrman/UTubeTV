package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sickboots.sickvideos.misc.Util;

/**
 * Created by sgehrman on 11/21/13.
 */
public class VideoImageView extends ImageView {
  private GradientDrawable mTopGradient;
  private GradientDrawable mBottomGradient;
  private int mCachedWidth = 0;
  private int mGradientHeight;

  public VideoImageView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mGradientHeight = (int) Util.dpToPx(40.0f, getContext());

    int topColors[] = {0xaa000000, 0x00000000};
    mTopGradient = createGradient(topColors);

    int bottomColors[] = {0x00000000, 0xaa000000};
    mBottomGradient = createGradient(bottomColors);
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    adjustGradientRects();

    mTopGradient.draw(canvas);

    int y = getHeight() - mGradientHeight;
    canvas.translate(0, y);
    mBottomGradient.draw(canvas);
    canvas.translate(0, -y);
  }

  private void adjustGradientRects() {
    if (mCachedWidth != getWidth()) {
      mCachedWidth = getWidth();

      Rect rect = new Rect(0, 0, mCachedWidth, mGradientHeight);

      mTopGradient.setBounds(rect);
      mBottomGradient.setBounds(rect);
    }
  }

  private GradientDrawable createGradient(int colors[]) {
    GradientDrawable result = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    result.setShape(GradientDrawable.RECTANGLE);

    return result;
  }

}
