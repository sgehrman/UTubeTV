package com.sickboots.sickvideos.lists;

import android.app.Fragment;

import java.lang.ref.WeakReference;

/**
 * Created by sgehrman on 10/23/13.
 */
public class UIAccess {
  // interface for getting results
  public interface UIAccessListener {
    public void onResults();
  }

  private WeakReference<Fragment> fragmentRef;

  public UIAccess(Fragment f) {
    fragmentRef = new WeakReference<Fragment>(f);
  }

  public Fragment fragment() {
    return fragmentRef.get();
  }

  public void onListResults() {
    UIAccessListener listener = (UIAccessListener) fragment();

    if (listener != null) {
      listener.onResults();
    }
  }

}
