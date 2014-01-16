package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class VideoImageView extends ImageView {
  private ViewDecorations mDecorations;
  private boolean mDrawHiddenIndicator = false;

  public VideoImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDecorations(ViewDecorations decorations) {
    mDecorations = decorations;
  }

  public void setDrawHiddenIndicator(boolean set) {
    mDrawHiddenIndicator = set;
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mDecorations != null) {
      mDecorations.drawInView(this, mDrawHiddenIndicator, canvas);
    }


  }
}
