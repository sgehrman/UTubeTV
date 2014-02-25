package com.distantfuture.videos.misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.distantfuture.videos.R;

/**
 * Created by sgehrman on 2/13/14.
 */
public class LinePageIndicator extends View {
  private ViewPager mViewPager;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private int mCurrentPage;

  public LinePageIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);

    int indicatorColor = context.getResources().getColor(R.color.holo_blue);
    mPaint.setColor(indicatorColor);
  }

  public void setViewPager(ViewPager viewPager) {
    mViewPager = viewPager;

    // set up callback so radio group syncs to the viewPager
    mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        mCurrentPage = position;
        invalidate();
      }
    });
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // super.onDraw(canvas);

    if (mViewPager == null) {
      return;
    }
    final int count = mViewPager.getAdapter().getCount();
    if (count == 0) {
      return;
    }

    if (mCurrentPage >= count) {
      return;
    }

    final int paddingLeft = getPaddingLeft();
    final float pageWidth = (getWidth() - paddingLeft - getPaddingRight()) / (1f * count);
    final float left = paddingLeft + pageWidth * mCurrentPage;
    final float right = left + pageWidth;
    final float top = getPaddingTop();
    final float bottom = getHeight() - getPaddingBottom();
    canvas.drawRect(left, top, right, bottom, mPaint);
  }

}
