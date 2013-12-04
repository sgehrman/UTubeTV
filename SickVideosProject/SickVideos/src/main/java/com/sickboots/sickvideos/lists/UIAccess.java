package com.sickboots.sickvideos.lists;

import android.app.Activity;
import android.app.Fragment;

import java.lang.ref.WeakReference;

public class UIAccess {
  // interface for getting results
  public interface UIAccessListener {
    public void onResults();
  }

  private WeakReference<Fragment> fragmentRef;

  public UIAccess(Fragment f) {
    super();
    fragmentRef = new WeakReference<Fragment>(f);
  }

  public Fragment fragment() {
    return fragmentRef.get();
  }

  public Activity getActivity() {
    return fragment().getActivity();
  }

  public void onListResults() {
    UIAccessListener listener = (UIAccessListener) fragment();

    if (listener != null) {
      listener.onResults();
    }
  }

}
