package com.distantfuture.videos.misc;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.distantfuture.videos.R;
import com.distantfuture.videos.imageutils.ToolbarIcons;

public class DonateThanksHelper {
  private Handler mRemoveHandler;
  private Runnable mRemoveRunnable;

  private ContentView mContentView;

  private Animation inAnimation, outAnimation;

  public DonateThanksHelper(Context context) {
    super();

    inAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
    outAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);

    mRemoveHandler = new Handler(Looper.getMainLooper());
    mRemoveRunnable = new Runnable() {
      @Override
      public void run() {
        removeMsg();
      }
    };
  }

  public void install(Activity activity) {
    mContentView = new ContentView(activity);

    View view = mContentView.mView;
    if (view.getParent() == null) {
      activity.addContentView(view, mContentView.getLayoutParams());
    }
    view.startAnimation(inAnimation);
    if (view.getVisibility() != View.VISIBLE) {
      view.setVisibility(View.VISIBLE);
    }

    startRemoveTimer();
  }

  public void startRemoveTimer() {
    mRemoveHandler.postDelayed(mRemoveRunnable, 3000);
  }

  private void removeMsg() {
    ViewGroup parent = ((ViewGroup) mContentView.mView.getParent());
    if (parent != null) {
      mContentView.mView.startAnimation(outAnimation);

      parent.removeView(mContentView.mView);

      mContentView = null;
    }
  }

  public static class ContentView {
    private final static int mIconSize = 64;
    private final Activity mActivity;
    public ViewGroup mView;
    private ViewGroup.LayoutParams mLayoutParams;
    private ImageView mImageView;

    public ContentView(Activity activity) {
      mActivity = activity;

      LayoutInflater inflate = LayoutInflater.from(activity);
      mView = (ViewGroup) inflate.inflate(R.layout.view_donate_thanks, null);

      Drawable heartDrawable = ToolbarIcons.icon(mView.getContext(), ToolbarIcons.IconID.HEART, 0xffffffff, mIconSize);
      heartDrawable.setAlpha(233);
      Bitmap heartBitmap = Utils.drawableToBitmap(heartDrawable, mIconSize);


      int offsetX = 0;
      int offsetY = 0;
      Point displaySize = Utils.getDisplaySize(activity);
      int maxY = displaySize.y - mIconSize;
      int maxX = displaySize.x - mIconSize;

      for (int y = 0; y < maxY; y += mIconSize) {
        Debug.log("new row");
        for (int x = 0; x < maxX; x += mIconSize) {
          addImageView(activity, heartBitmap, x, y);
        }
      }
    }

    public ViewGroup.LayoutParams getLayoutParams() {
      if (mLayoutParams == null) {
        mLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP);
      }
      return mLayoutParams;
    }

    private void addImageView(Activity activity, Bitmap heartBitmap, int offsetX, int offsetY) {
      mImageView = createImageView(activity, heartBitmap);
      mImageView.setAlpha(0.0f);

      mView.addView(mImageView);

      mImageView.animate().setDuration((long) (Math.random() * 2000));
      mImageView.animate().translationY(offsetY);
      mImageView.animate().translationX(offsetX);
      mImageView.animate().alpha(1.0f);
      mImageView.animate().setInterpolator(new AccelerateDecelerateInterpolator());
      mImageView.animate().setListener(new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
      });

    }

    private ImageView createImageView(Activity activity, Bitmap heartBitmap) {
      ImageView imageView = new ImageView(activity);
      imageView.setImageBitmap(heartBitmap);

      LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      int marginPx = (int) Utils.dpToPx(2, activity);
      imageParams.setMargins(marginPx, marginPx, marginPx, marginPx);
      imageView.setLayoutParams(imageParams);

      return imageView;
    }
  }
}