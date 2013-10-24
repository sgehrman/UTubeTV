package com.sickboots.sickvideos;

import android.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by sgehrman on 10/23/13.
 */
public class UIAccess {
  // interface for getting results
  public interface UIAccessListener {
    public void onResults();
  }

  private WeakReference<Fragment> fragmentRef;
  private int uiID;

  public UIAccess(Fragment f, int id) {
    fragmentRef = new WeakReference<Fragment>(f);
    uiID = id;
  }

  public int uiID() {
    return uiID;
  }

  public Fragment fragment() {
    return fragmentRef.get();
  }

  public void onListResults() {
    UIAccessListener listener = (UIAccessListener) fragment();
    listener.onResults();
  }

}
