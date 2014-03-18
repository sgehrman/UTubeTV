package com.distantfuture.videos.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.debug.hv.ViewServer;
import com.distantfuture.videos.misc.DUtils;

public class ViewServerActivity extends FragmentActivity {
  private int mCounter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (DUtils.isDebugBuild())
      ViewServer.get(this).addWindow(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (DUtils.isDebugBuild())
      ViewServer.get(this).removeWindow(this);
  }

  @Override
  public void onResume() {
    super.onResume();

    if (DUtils.isDebugBuild())
      ViewServer.get(this).setFocusedWindow(this);
  }
}