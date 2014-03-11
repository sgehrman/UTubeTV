package com.distantfuture.videos.misc;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

/**
 * workaround for this bug: https://code.google.com/p/android/issues/detail?id=26194.
 */

public class SwitchPref extends SwitchPreference {

  public SwitchPref(Context context) {
    this(context, null);
  }

  public SwitchPref(Context context, AttributeSet attrs) {
    this(context, attrs, android.R.attr.switchPreferenceStyle);
  }

  public SwitchPref(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

}
