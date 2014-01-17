package com.sickboots.sickvideos.misc;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sickboots.sickvideos.R;

public class EmptyListHelper {
  View mEmptyView;
  TextView mTextView;
  ProgressBar mProgress;
  Handler mDelayedOperation;
  String mMessage = "";  // set so we don't have to check for null in update
  boolean mHideProgress = true;

  public EmptyListHelper(View emptyView) {
    super();

    mEmptyView = emptyView;
    mTextView = (TextView) emptyView.findViewById(R.id.message);
    mProgress = (ProgressBar) emptyView.findViewById(R.id.progress);
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
            mProgress.setVisibility(View.GONE);
          } else {
            mProgress.setVisibility(View.VISIBLE);
          }

          mDelayedOperation = null;
        }
      }, 500);
    }
  }
}
