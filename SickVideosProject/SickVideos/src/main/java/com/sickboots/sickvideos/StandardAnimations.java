package com.sickboots.sickvideos;

import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;

public class StandardAnimations {
  public static final void rockBounce(final View theView) {
    theView.animate().rotationXBy(30.0f).setDuration(200).setInterpolator(new AnticipateOvershootInterpolator()).withEndAction(new Runnable() {
      @Override
      public void run() {
        theView.animate().setDuration(100).rotationX(0).setInterpolator(new AnticipateOvershootInterpolator()).start();
      }
    });
  }

  public static final void upAndAway(final View theView) {
    if (theView != null) {
      theView.animate().alpha(.9f).setDuration(100).scaleX(.9f).scaleY(.9f).withEndAction(new Runnable() {
        public void run() {

          theView.animate().setInterpolator(new AnticipateInterpolator()).translationYBy(-theView.getHeight()).setDuration(200).withEndAction(new Runnable() {
            public void run() {

              theView.setAlpha(0.0f);
              theView.setTranslationY(theView.getHeight());

              theView.animate().setDuration(200).alpha(1).setStartDelay(200).translationY(0).scaleX(1.0f).scaleY(1.0f).setInterpolator(new BounceInterpolator()).withEndAction(new Runnable() {
                public void run() {

                }
              }).start();
            }
          }).start();
        }

      });
    }
  }

  public static final void rubberClick(final View theView) {
    theView.animate().scaleX(.8f).scaleY(.8f).setDuration(200).setInterpolator(new AnticipateOvershootInterpolator()).withEndAction(new Runnable() {
      @Override
      public void run() {
        theView.animate().setDuration(100).scaleX(1.0f).scaleY(1.0f).setInterpolator(new AnticipateOvershootInterpolator()).start();
      }
    });
  }

}
