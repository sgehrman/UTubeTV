package com.sickboots.sickvideos.misc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sickboots.sickvideos.R;

public class FlappyMsg {
  public static final int LENGTH_SHORT = 3000;
  private int mDuration = LENGTH_SHORT;
  public static final int LENGTH_LONG = 5000;
  private final Activity mContext;
  private View mView;
  private LayoutParams mLayoutParams;

  public FlappyMsg(Activity context) {
    mContext = context;
  }

  public static FlappyMsg makeText(Activity context, CharSequence text, Style style) {
    return makeText(context, text, style, R.layout.app_msg);
  }

  public static FlappyMsg makeText(Activity context, CharSequence text, Style style, float textSize) {
    return makeText(context, text, style, R.layout.app_msg, textSize);
  }

  public static FlappyMsg makeText(Activity context, CharSequence text, Style style, int layoutId) {
    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflate.inflate(layoutId, null);

    return makeText(context, text, style, v);
  }

  public static FlappyMsg makeText(Activity context, CharSequence text, Style style, int layoutId, float textSize) {
    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflate.inflate(layoutId, null);

    return makeText(context, text, style, v, textSize);
  }

  public static FlappyMsg makeText(Activity context, CharSequence text, Style style, View customView) {
    return makeText(context, text, style, customView, 0);
  }

  private static FlappyMsg makeText(Activity context, CharSequence text, Style style, View view, float textSize) {
    FlappyMsg result = new FlappyMsg(context);

    view.setBackgroundResource(style.background);

    TextView tv = (TextView) view.findViewById(android.R.id.message);
    if (textSize > 0)
      tv.setTextSize(textSize);
    tv.setText(text);

    result.mView = view;
    result.mDuration = style.duration;

    return result;
  }

  public static FlappyMsg makeText(Activity context, int resId, Style style, View customView) {
    return makeText(context, context.getResources().getText(resId), style, customView);
  }

  public static FlappyMsg makeText(Activity context, int resId, Style style) throws Resources.NotFoundException {
    return makeText(context, context.getResources().getText(resId), style);
  }

  public static FlappyMsg makeText(Activity context, int resId, Style style, int layoutId) throws Resources.NotFoundException {
    return makeText(context, context.getResources().getText(resId), style, layoutId);
  }

  public static void cancelAll() {
    FlappyMsgMgr.getInstance().clearAllMsg();
  }

  public void show() {
    FlappyMsgMgr manager = FlappyMsgMgr.getInstance();
    manager.add(this);
  }

  public boolean isShowing() {
      return mView != null && mView.getParent() != null;
  }

  public void cancel() {
    FlappyMsgMgr.getInstance().clearMsg(this);

  }

  public Activity getActivity() {
    return mContext;
  }

  public View getView() {
    return mView;
  }

  public void setView(View view) {
    mView = view;
  }

  public int getDuration() {
    return mDuration;
  }

  public void setDuration(int duration) {
    mDuration = duration;
  }

  public void setText(int resId) {
    setText(mContext.getText(resId));
  }

  public void setText(CharSequence s) {
    if (mView == null) {
      throw new RuntimeException("This FlappyMsg was not created with FlappyMsg.makeText()");
    }
    TextView tv = (TextView) mView.findViewById(android.R.id.message);
    if (tv == null) {
      throw new RuntimeException("This FlappyMsg was not created with FlappyMsg.makeText()");
    }
    tv.setText(s);
  }

  public LayoutParams getLayoutParams() {
    if (mLayoutParams == null) {
      mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
    return mLayoutParams;
  }

  public FlappyMsg setLayoutParams(LayoutParams layoutParams) {
    mLayoutParams = layoutParams;
    return this;
  }

  public FlappyMsg setLayoutGravity(int gravity) {
    mLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, gravity);
    return this;
  }

  public static class Style {

    private final int duration;
    private final int background;

    public Style(int duration, int resId) {
      this.duration = duration;
      this.background = resId;
    }

    public int getDuration() {
      return duration;
    }

    public int getBackground() {
      return background;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof FlappyMsg.Style)) {
        return false;
      }
      Style style = (Style) o;
      return style.duration == duration && style.background == background;
    }

  }

}
