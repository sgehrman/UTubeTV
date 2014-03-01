
package com.distantfuture.castcompanionlibrary.lib.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.CastException;

/**
 * A broadcastreceiver for receiving media button actions (from the lock screen) as well as
 * the the status bar notification media actions.
 */
public class VideoIntentReceiver extends BroadcastReceiver {

  private static final String TAG = CastUtils.makeLogTag(VideoIntentReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    VideoCastManager castMgr = null;
    try {
      castMgr = VideoCastManager.getInstance();
    } catch (CastException e1) {
      CastUtils.LOGE(TAG, "onReceive(): No CastManager instance exists");
    }
    String action = intent.getAction();
    if (null == action) {
      return;
    }
    if (action.equals(VideoCastNotificationService.ACTION_TOGGLE_PLAYBACK)) {
      try {
        if (null != castMgr) {
          CastUtils.LOGD(TAG, "Toggling playback via CastManager");
          castMgr.togglePlayback();
        } else {
          CastUtils.LOGD(TAG, "Toggling playback via NotificationService");
          context.startService(new Intent(VideoCastNotificationService.ACTION_TOGGLE_PLAYBACK));
        }

      } catch (Exception e) {
        // already logged
      }
    } else if (action.equals(VideoCastNotificationService.ACTION_STOP)) {

      try {
        if (null != castMgr) {
          CastUtils.LOGD(TAG, "Calling stopApplication from intent");
          castMgr.disconnect();
        } else {
          context.startService(new Intent(VideoCastNotificationService.ACTION_STOP));
        }
      } catch (Exception e) {
        CastUtils.LOGE(TAG, "onReceive(): Failed to stop application");
      }
    } else if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {

      KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
      if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
        return;
      }

      switch (keyEvent.getKeyCode()) {
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
          try {
            castMgr.togglePlayback();
          } catch (Exception e) {
            // already logged
          }
          break;
      }
    }

  }

}
