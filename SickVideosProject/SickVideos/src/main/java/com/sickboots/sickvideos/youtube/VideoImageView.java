package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.misc.Utils;

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
