package com.sickboots.sickvideos.misc;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sickboots.sickvideos.R;

class FlappyMsg {
  public static final int LENGTH_SHORT = 3000;
  public static final int LENGTH_LONG = 5000;

  private static final int MESSAGE_DISPLAY = 0xc2007;
  private static final int MESSAGE_ADD_VIEW = 0xc20074dd;
  private static final int MESSAGE_REMOVE = 0xc2007de1;

  private static FlappyMsg mInstance;
  private Handler mRemoveHandler;
  private Runnable mRemoveRunnable;

  private FlappyView mCurrentMsg;

  private Animation inAnimation, outAnimation;

  private FlappyMsg(Context context) {
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

  static synchronized FlappyMsg getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new FlappyMsg(context);
    }
    return mInstance;
  }

  public static void makeText(Activity activity, CharSequence text) {
    getInstance(activity).updateText(activity, text);
  }

  public void updateText(Activity activity, CharSequence text) {
    if (mCurrentMsg == null) {
      mCurrentMsg = new FlappyView(activity);
      mCurrentMsg.setText(text);

      addMsgToView();
    } else {
      // stop remove handler
      mRemoveHandler.removeCallbacks(mRemoveRunnable);

      mCurrentMsg.setText(text);
    }

    startRemoveTimer();
  }

  public void startRemoveTimer() {
    mRemoveHandler.postDelayed(mRemoveRunnable, 3000);
  }

  private void removeMsg() {
    ViewGroup parent = ((ViewGroup) mCurrentMsg.mView.getParent());
    if (parent != null) {
      mCurrentMsg.mView.startAnimation(outAnimation);

      parent.removeView(mCurrentMsg.mView);

      mCurrentMsg = null;
    }
  }

  private void addMsgToView() {
    View view = mCurrentMsg.mView;
    if (view.getParent() == null) {
      mCurrentMsg.getActivity().addContentView(view, mCurrentMsg.getLayoutParams());
    }
    view.startAnimation(inAnimation);
    if (view.getVisibility() != View.VISIBLE) {
      view.setVisibility(View.VISIBLE);
    }
  }

  public static class FlappyView {
    private final Activity mContext;
    public View mView;
    private ViewGroup.LayoutParams mLayoutParams;

    public FlappyView(Activity context) {
      mContext = context;

      LayoutInflater inflate = LayoutInflater.from(context);
      mView = inflate.inflate(R.layout.app_msg, null);

      mView.setBackgroundResource(R.drawable.app_msg_background);
    }

    public Activity getActivity() {
      return mContext;
    }

    public void setText(int resId) {
      setText(mContext.getText(resId));
    }

    public void setText(CharSequence s) {
      TextView tv = (TextView) mView.findViewById(android.R.id.message);
      tv.setText(s);
    }

    public ViewGroup.LayoutParams getLayoutParams() {
      if (mLayoutParams == null) {
        mLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP);
      }
      return mLayoutParams;
    }

  }


}