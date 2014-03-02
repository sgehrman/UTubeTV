package com.distantfuture.videos.misc;

import android.app.Application;
import android.content.Context;

import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.utils.CastUtils;
import com.google.android.gms.cast.CastMediaControlIntent;

public class MainApplication extends Application {
  private static String sApplicationID;
  private static VideoCastManager sCastManager = null;
  public static final double VOLUME_INCREMENT = 0.05;

  @Override
  public void onCreate() {
    super.onCreate();
    sApplicationID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID; // "6142AE0B"; // "5A3D7A5C";
    CastUtils.saveFloatToPreference(getApplicationContext(), VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);
  }

  public static VideoCastManager getCastManager(Context context) {
    if (null == sCastManager) {
      sCastManager = VideoCastManager.initialize(context, sApplicationID, null);
      sCastManager.enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
          VideoCastManager.FEATURE_LOCKSCREEN |
          VideoCastManager.FEATURE_DEBUGGING);

    }
    sCastManager.setContext(context);

    //      add pref later
    //      String destroyOnExitStr = CastUtils.getStringFromPreference(context, CastPreference.TERMINATION_POLICY_KEY);
    //      mCastMgr.setStopOnDisconnect(null != destroyOnExitStr && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));

    sCastManager.setStopOnDisconnect(true);

    return sCastManager;
  }

}

