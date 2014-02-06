package com.sickboots.sickvideos.misc;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;

public class StandardAnimations {
  public static void rockBounce(final View theView) {
    theView.animate()
        .rotationXBy(30.0f)
        .setDuration(200)
        .setInterpolator(new AnticipateOvershootInterpolator())
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            theView.animate()
                .setDuration(100)
                .rotationX(0)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .start();
          }
        });
  }

  public static void upAndAway(final View theView) {
    if (theView != null) {
      theView.animate()
          .alpha(.9f)
          .setDuration(100)
          .scaleX(.9f)
          .scaleY(.9f)
          .withEndAction(new Runnable() {
            public void run() {

              theView.animate()
                  .setInterpolator(new AnticipateInterpolator())
                  .translationYBy(-theView.getHeight())
                  .setDuration(200)
                  .withEndAction(new Runnable() {
                    public void run() {

                      theView.setAlpha(0.0f);
                      theView.setTranslationY(theView.getHeight());

                      theView.animate()
                          .setDuration(200)
                          .alpha(1)
                          .setStartDelay(200)
                          .translationY(0)
                          .scaleX(1.0f)
                          .scaleY(1.0f)
                          .setInterpolator(new BounceInterpolator())
                          .withEndAction(new Runnable() {
                            public void run() {

                            }
                          })
                          .start();
                    }
                  })
                  .start();
            }

          });
    }
  }

  public static  void rubberClick(final View theView) {
    ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(theView, "rotationY", 60f);
    ObjectAnimator rotateBack = ObjectAnimator.ofFloat(theView, "rotationY", 0f);
    ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(theView, "scaleX", .4f);
    ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(theView, "scaleY", .4f);
    ObjectAnimator scaleXBack = ObjectAnimator.ofFloat(theView, "scaleX", 1f);
    ObjectAnimator scaleYBack = ObjectAnimator.ofFloat(theView, "scaleY", 1f);

    AnimatorSet bouncer = new AnimatorSet();
    bouncer.setInterpolator(new AnticipateOvershootInterpolator());
    bouncer.play(scaleXDown).with(scaleYDown);
    bouncer.play(scaleXBack).with(scaleYBack);
    bouncer.play(scaleXBack).after(scaleXDown);
    bouncer.play(rotateAnim).after(scaleXBack);
    bouncer.play(rotateBack).after(rotateAnim);

    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(theView, "alpha", 0f);
    ObjectAnimator fadeBack = ObjectAnimator.ofFloat(theView, "alpha", 1f);
    fadeOut.setDuration(250);
    fadeBack.setDuration(250);
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.play(bouncer).before(fadeOut);
    animatorSet.play(fadeBack).after(fadeOut);
    animatorSet.start();

    //    theView.animate().scaleX(.8f).scaleY(.8f).setDuration(200).setInterpolator(new AnticipateOvershootInterpolator()).withEndAction(new Runnable() {
    //      @Override
    //      public void run() {
    //        theView.animate().setDuration(100).scaleX(1.0f).scaleY(1.0f).setInterpolator(new AnticipateOvershootInterpolator()).start();
    //      }
    //    });
  }

  public static void winky(View theView, float normalImageAlpha) {

    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(theView, "alpha", 1f);
    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(theView, "alpha", 0f);
    ObjectAnimator fadeBack = ObjectAnimator.ofFloat(theView, "alpha", normalImageAlpha);
    fadeIn.setDuration(150);
    fadeOut.setDuration(150);
    fadeBack.setDuration(250);
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

    animatorSet.play(fadeIn).after(fadeOut);
    animatorSet.play(fadeBack).after(fadeOut);
    animatorSet.start();
  }

  public static void dosomething(View theView) {
    ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(theView, "rotationY", 0f, 60f);
    ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(theView, "scaleX", 1f, .5f);
    ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(theView, "scaleY", 1f, .5f);

    AnimatorSet bouncer = new AnimatorSet();
    bouncer.play(rotateAnim).before(scaleXDown);
    ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(theView, "alpha", 1f, 0f);
    fadeAnim.setDuration(250);
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.play(bouncer).before(fadeAnim);
    animatorSet.start();

  }

}
