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
  private String mAccountName;

  public UIAccess(Fragment f, String accountName) {
    fragmentRef = new WeakReference<Fragment>(f);
    mAccountName = accountName;
  }

  public Fragment fragment() {
    return fragmentRef.get();
  }

  public String accountName() {
    return mAccountName;
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
