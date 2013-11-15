package com.sickboots.sickvideos;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

public class ScrollTriggeredAnimator {
  private Animation mInAnimation;
  private Animation mOutAnimation;
  private View mAnimationTarget;
  private boolean mGlowing=false;
  private final Handler mHandler = new Handler(Looper.getMainLooper());

  private final Runnable mBackgroundDimmerFadeRunnable = new Runnable() {
    @Override
    public void run() {
        mGlowing = false;
        mAnimationTarget.startAnimation(mOutAnimation);
    }
  };

  public ScrollTriggeredAnimator(AbsListView absListView, View animationTarget) {
    super();

    mAnimationTarget = animationTarget;

    createAnimations();
    absListView.setOnScrollListener(setupListener());
  }

  private void createAnimations() {
     int scrollBarPanelFadeDuration = 500; // ViewConfiguration.getScrollBarFadeDuration();

    mOutAnimation = new AlphaAnimation(0, 1);
    mOutAnimation.setFillAfter(true);
    mOutAnimation.setDuration(scrollBarPanelFadeDuration * 2);
    mOutAnimation.setZAdjustment(Animation.ZORDER_BOTTOM);

    mInAnimation = new AlphaAnimation(1, 0);
    mInAnimation.setFillAfter(true);
    mInAnimation.setDuration(scrollBarPanelFadeDuration / 2);
    mInAnimation.setZAdjustment(Animation.ZORDER_BOTTOM);
  }

  private AbsListView.OnScrollListener setupListener() {
    // listen for scroll, animate glow effect
    AbsListView.OnScrollListener listener = new AbsListView.OnScrollListener() {

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
          case SCROLL_STATE_IDLE:
            if (mGlowing) {
              mHandler.removeCallbacks(mBackgroundDimmerFadeRunnable);
              mHandler.postAtTime(mBackgroundDimmerFadeRunnable, AnimationUtils.currentAnimationTimeMillis());
            }
            break;
          case SCROLL_STATE_TOUCH_SCROLL:
            // kill any animation waiting to happen
            mHandler.removeCallbacks(mBackgroundDimmerFadeRunnable);

            // make it glow bright
            if (!mGlowing) {
              mGlowing = true;
              mAnimationTarget.startAnimation(mInAnimation);
            }

            break;
          case SCROLL_STATE_FLING:
            break;
          default:
            break;
        }
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                           int totalItemCount) {
      }
    };

    return listener;
  }
}


