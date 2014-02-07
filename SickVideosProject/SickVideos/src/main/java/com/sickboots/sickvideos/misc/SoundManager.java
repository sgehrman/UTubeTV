package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

public class SoundManager {
  private boolean mMuteState = false;

  private boolean mSavedMute;
  private int mSavedVolume;
  private boolean mWasModified;
  private Context mContext;

  public SoundManager(Context context) {
    super();

    mContext = context.getApplicationContext();
  }

  // call when owning fragment or activity onPause
  public void restoreMuteIfNeeded() {
    if (mMuteState) {
      mute(false);
    }
  }

  public boolean isMute() {
    return mMuteState;
  }

  public void mute(boolean mute) {
    if (mMuteState != mute) {
      mMuteState = mute;

      AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

      // setStreamMute is broken on my 4.1 (16) galaxy nexus, so using volume instead
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
        int saved = manager.getStreamVolume(AudioManager.STREAM_MUSIC);

        manager.setStreamVolume(AudioManager.STREAM_MUSIC, (mMuteState ? 0 : mSavedVolume), 0);

        mSavedVolume = saved;
      } else
        manager.setStreamMute(AudioManager.STREAM_MUSIC, mMuteState);
    }
  }

}


