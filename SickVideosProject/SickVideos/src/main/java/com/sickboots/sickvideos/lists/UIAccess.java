package com.sickboots.sickvideos.lists;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

import java.lang.ref.WeakReference;

public interface UIAccess {
  public void onResults();
  public Fragment fragment();
  public Activity getActivity();
  public Context getContext();
}

