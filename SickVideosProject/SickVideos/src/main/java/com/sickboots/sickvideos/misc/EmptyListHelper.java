package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sickboots.sickvideos.R;

public class EmptyListHelper {
  ViewGroup mEmptyView;
  TextView mTextView;
  ProgressBar mProgress;
  Handler mDelayedOperation;
  String mMessage = "";  // set so we don't have to check for null in update
  boolean mHideProgress = true;

  public EmptyListHelper(Context context) {
    super();

    mEmptyView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.empty_view_grid, null);

    mTextView = (TextView) mEmptyView.findViewById(R.id.message);
    mProgress = (ProgressBar) mEmptyView.findViewById(R.id.progress);
  }

  public View view() {
    return mEmptyView;
  }

  public void updateEmptyListView(String message, boolean hideProgress) {
    // no change, return
    if (mHideProgress == hideProgress && mMessage.equals(message))
      return;

    mMessage = message;
    mHideProgress = hideProgress;

    // updating with a bit of a delay so we don't see it quickly change before the new list data loads
    if (mDelayedOperation == null) {
      mDelayedOperation = new Handler(Looper.getMainLooper());

      mDelayedOperation.postDelayed(new Runnable() {
        @Override
        public void run() {
          mTextView.setText(mMessage);
          mTextView.setVisibility(View.VISIBLE);

          if (mHideProgress) {
            mProgress.setVisibility(View.INVISIBLE);
          } else {
            mProgress.setVisibility(View.VISIBLE);
          }

          mDelayedOperation = null;
        }
      }, 500);
    }
  }
}
