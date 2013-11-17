package com.sickboots.sickvideos;

import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;

public class StandardAnimations {
  public static final void rockBounce(View theView) {
    theView.animate().alpha(1.0f);
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
              });
            }
          });
        }

      });
    }
  }
}
