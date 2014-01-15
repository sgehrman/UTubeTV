package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.misc.Utils;

public class ViewDecorations {

  private boolean mDrawShadows = false;
  private int mCachedWidth = 0;
  private boolean mDrawIcon = true;
  private boolean mIsPlaylist = false;
  private GradientDrawable mStrokeAndFill;
  private GradientDrawable mStrokeAndFill2;
  int mHeight = 0, mWidth = 0;

  // gradients shared between all views to cut down on memory allocations etc.
  private GradientDrawable sTopGradient;
  private GradientDrawable sBottomGradient;
  private int sGradientHeight;
  private BitmapDrawable playBitmap = null;

  public ViewDecorations(boolean isPlaylist) {
    super();

    mIsPlaylist = isPlaylist;
  }

  public void setDrawShadows(boolean set) {
    mDrawShadows = set;
  }

  public void strokeAndFill(Context context, int fillColor, int strokeColor, float radiusInDP, int thicknessInDP) {
    // convert thickness to px
    int thickness = (int) Utils.dpToPx(thicknessInDP, context);
    float radius = Utils.dpToPx(radiusInDP, context);

    mStrokeAndFill = new GradientDrawable();
    mStrokeAndFill.setStroke(thickness, strokeColor);
    mStrokeAndFill.setColor(fillColor);

    mStrokeAndFill2 = new GradientDrawable();
    mStrokeAndFill2.setStroke(thickness, 0xff111111);
    mStrokeAndFill2.setCornerRadius(radius);
    mStrokeAndFill2.setColor(0);
  }

  public void drawInView(View view, Canvas canvas) {
    if (mStrokeAndFill != null) {
      adjustStrokeAndFillRect(view);

      mStrokeAndFill.draw(canvas);
      mStrokeAndFill2.draw(canvas);
    }

    if (mDrawShadows) {
      createGradients(view.getContext());
      adjustGradientRects(view);

      sTopGradient.draw(canvas);

      int y = view.getHeight() - sGradientHeight;
      canvas.translate(0, y);
      sBottomGradient.draw(canvas);
      canvas.translate(0, -y);
    }

    if (mDrawIcon) {
      int playButtonSize = 110;
      // draw play button
      if (playBitmap == null) {
        if (mIsPlaylist)
          playBitmap = ToolbarIcons.iconBitmap(view.getContext(), ToolbarIcons.IconID.LIST, Color.WHITE, playButtonSize);
        else
          playBitmap = ToolbarIcons.iconBitmap(view.getContext(), ToolbarIcons.IconID.VIDEO_PLAY, Color.WHITE, playButtonSize);

        playBitmap.setAlpha(120);  // 0 - 255
      }

      int x = (view.getWidth() - playButtonSize) / 2;
      int y = (view.getHeight() - playButtonSize) / 2;
      playBitmap.setBounds(x, y, x + playButtonSize, y + playButtonSize);
      playBitmap.draw(canvas);
    }
  }

  private void adjustGradientRects(View view) {
    if (mCachedWidth != view.getWidth()) {
      mCachedWidth = view.getWidth();

      Rect rect = new Rect(0, 0, mCachedWidth, sGradientHeight);

      sTopGradient.setBounds(rect);
      sBottomGradient.setBounds(rect);
    }
  }

  private void adjustStrokeAndFillRect(View view) {
    int w = view.getWidth();
    int h = view.getHeight();

    if (mWidth != w || mHeight != h) {
      mWidth = w;
      mHeight = h;

      Rect rect = new Rect(0, 0, mWidth, mHeight);

      mStrokeAndFill.setBounds(rect);
      mStrokeAndFill2.setBounds(rect);
    }
  }

  private void createGradients(Context context) {
    if (sTopGradient == null) {
      sGradientHeight = (int) Utils.dpToPx(30.0f, context);

      int colors[] = {0x99000000, 0x00000000};
      sTopGradient = createGradient(colors, GradientDrawable.Orientation.TOP_BOTTOM);
      sBottomGradient = createGradient(colors, GradientDrawable.Orientation.BOTTOM_TOP);
    }
  }

  private GradientDrawable createGradient(int colors[], GradientDrawable.Orientation orientation) {
    GradientDrawable result = new GradientDrawable(orientation, colors);
    result.setShape(GradientDrawable.RECTANGLE);

    return result;
  }

}
