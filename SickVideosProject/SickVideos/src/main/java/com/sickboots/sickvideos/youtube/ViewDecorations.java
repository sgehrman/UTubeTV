package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;

import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.misc.Utils;

public class ViewDecorations {

  int mHeight = 0, mWidth = 0;
  private boolean mDrawShadows = false;
  private int mCachedWidth = 0;
  private boolean mDrawIcon = true;
  private boolean mIsPlaylist = false;
  private GradientDrawable mStrokeAndFill;
  private GradientDrawable mStrokeAndFill2;
  // gradients shared between all views to cut down on memory allocations etc.
  private GradientDrawable mTopGradient;
  private GradientDrawable mBottomGradient;
  private int mGradientHeight;
  private BitmapDrawable mPlayBitmap = null;

  private BitmapDrawable mHiddenDrawable;
  private int mHiddenDrawableWidth;

  public ViewDecorations(Context context, boolean isPlaylist) {
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

  public void drawInView(View view, boolean drawHiddenIndicator, Canvas canvas) {
    if (mStrokeAndFill != null) {
      adjustStrokeAndFillRect(view);

      mStrokeAndFill.draw(canvas);
      mStrokeAndFill2.draw(canvas);
    }

    if (mDrawShadows) {
      createGradients(view.getContext());
      adjustGradientRects(view);

      mTopGradient.draw(canvas);

      int y = view.getHeight() - mGradientHeight;
      canvas.translate(0, y);
      mBottomGradient.draw(canvas);
      canvas.translate(0, -y);
    }

    if (mDrawIcon) {
      int playButtonSize = 100;
      // draw play button
      if (mPlayBitmap == null) {
        if (mIsPlaylist)
          mPlayBitmap = ToolbarIcons.iconBitmap(view.getContext(), ToolbarIcons.IconID.LIST, Color.WHITE, playButtonSize);
        else
          mPlayBitmap = ToolbarIcons.iconBitmap(view.getContext(), ToolbarIcons.IconID.VIDEO_PLAY, Color.WHITE, playButtonSize);

        mPlayBitmap.setAlpha(100);  // 0 - 255
      }

      int x = (view.getWidth() - playButtonSize) / 2;
      int y = (view.getHeight() - playButtonSize) / 2;
      mPlayBitmap.setBounds(x, y, x + playButtonSize, y + playButtonSize);
      mPlayBitmap.draw(canvas);
    }

    if (drawHiddenIndicator)
      drawHiddenIndicator(view, canvas);
  }

  private void adjustGradientRects(View view) {
    if (mCachedWidth != view.getWidth()) {
      mCachedWidth = view.getWidth();

      Rect rect = new Rect(0, 0, mCachedWidth, mGradientHeight);

      mTopGradient.setBounds(rect);
      mBottomGradient.setBounds(rect);
    }
  }

  private void drawHiddenIndicator(View view, Canvas canvas) {
    int w = view.getWidth();
    int h = view.getHeight();

    if (mHiddenDrawableWidth != w) {
      mHiddenDrawableWidth = w;

      mHiddenDrawable = null;
    }

    if (mHiddenDrawable == null) {
      Bitmap bitmap = Utils.drawTextToBitmap(view.getContext(), w, h/2, "Click to Unhide", 0xffffffff, 0xff000000, 26, 0xaa000000, 33, 0xaaffffff, 1f);

      mHiddenDrawable = new BitmapDrawable(view.getContext().getResources(), bitmap);
      mHiddenDrawable.setGravity(Gravity.CENTER);

      Rect rect = new Rect(0, 0, w, h/2);

      mHiddenDrawable.setBounds(rect);
  }

  mHiddenDrawable.draw(canvas);
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
    if (mTopGradient == null) {
      mGradientHeight = (int) Utils.dpToPx(30.0f, context);

      int colors[] = {0x99000000, 0x00000000};
      mTopGradient = createGradient(colors, GradientDrawable.Orientation.TOP_BOTTOM);
      mBottomGradient = createGradient(colors, GradientDrawable.Orientation.BOTTOM_TOP);
    }
  }

  private GradientDrawable createGradient(int colors[], GradientDrawable.Orientation orientation) {
    GradientDrawable result = new GradientDrawable(orientation, colors);
    result.setShape(GradientDrawable.RECTANGLE);

    return result;
  }

}
