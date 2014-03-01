package com.distantfuture.videos.misc;

import android.app.Application;
import android.content.Context;

import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.utils.CastUtils;
import com.google.android.gms.cast.CastMediaControlIntent;

public class MainApplication extends Application {
  private static String APPLICATION_ID;
  private static VideoCastManager mCastMgr = null;
  public static final double VOLUME_INCREMENT = 0.05;
  private static Context mAppContext;

  @Override
  public void onCreate() {
    super.onCreate();
    mAppContext = getApplicationContext();
    APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID; // "6142AE0B"; // "5A3D7A5C";
    CastUtils.saveFloatToPreference(getApplicationContext(), VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);
  }

  public static VideoCastManager getCastManager(Context context) {
    if (null == mCastMgr) {
      mCastMgr = VideoCastManager.initialize(context, APPLICATION_ID, null);
      mCastMgr.enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
          VideoCastManager.FEATURE_LOCKSCREEN |
          VideoCastManager.FEATURE_DEBUGGING);

    }
    mCastMgr.setContext(context);

    //      add pref later
    //      String destroyOnExitStr = CastUtils.getStringFromPreference(context, CastPreference.TERMINATION_POLICY_KEY);
    //      mCastMgr.setStopOnDisconnect(null != destroyOnExitStr && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));

    mCastMgr.setStopOnDisconnect(true);

    return mCastMgr;
  }

}

