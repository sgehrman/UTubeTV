package com.sickboots.sickvideos.activities;

import android.app.Activity;
import android.os.Bundle;

import com.android.debug.hv.ViewServer;
import com.sickboots.sickvideos.misc.Debug;

public class ViewServerActivity extends Activity {
  private int mCounter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Debug.isDebugBuild())
      ViewServer.get(this).addWindow(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (Debug.isDebugBuild())
      ViewServer.get(this).removeWindow(this);
  }

  @Override
  public void onResume() {
    super.onResume();

    if (Debug.isDebugBuild())
      ViewServer.get(this).setFocusedWindow(this);
  }
}