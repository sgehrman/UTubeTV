package com.sickboots.sickvideos.lists;

import android.app.Activity;
import android.app.Fragment;

import java.lang.ref.WeakReference;

public interface UIAccess {
  // interface for getting results
    public void onResults();
    public Fragment fragment();
    public Activity getActivity();
  }

