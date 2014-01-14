package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class VideoImageView extends ImageView {
  private ViewDecorations mDecorations;

  public VideoImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDecorations(ViewDecorations decorations) {
    mDecorations = decorations;
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mDecorations != null) {
      mDecorations.drawInView(this, canvas);
    }


  }
}
