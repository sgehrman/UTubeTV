package com.sickboots.sickvideos.youtube;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.Util;

import java.util.Timer;
import java.util.TimerTask;

public final class VideoPlayerFragment extends YouTubePlayerFragment {

  public interface TimeRemainingListener {
    // call this on the main thread
    public void setTimeRemainingText(final String timeRemaining);
    public void setSeekFlashText(final String seekFlash);
  }

  private boolean mAutorepeat = false;
  private YouTubePlayer mPlayer;
  private String mVideoId;
  private String mTitle;
  private boolean mMuteState = false;
  private boolean mMutedForAd = false;
  private Timer mTimer;
  TimeRemainingListener mTimeRemainingListener;

  // added for debugging, remove this shit once we know it's solid
  private String mLastTimeString;

  public static VideoPlayerFragment newInstance() {
    return new VideoPlayerFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initialize(YouTubeAPI.devKey(), new YouTubePlayer.OnInitializedListener() {
      @Override
      public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {
        VideoPlayerFragment.this.mPlayer = player;
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

        player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);

        setupFullscreenListener();
        setupStateChangeListener();
        setupPlaybackEventListener();

        if (!restored && mVideoId != null) {
          player.loadVideo(mVideoId);
        }
      }

      @Override
      public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        VideoPlayerFragment.this.mPlayer = null;
      }
    });
  }

  @Override
  public void onDestroy() {
    if (mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }

    super.onDestroy();
  }

  public String getTitle() {
    return mTitle;
  }

  public void setVideo(String videoId, String title) {
    if (videoId != null && !videoId.equals(mVideoId)) {

      mVideoId = videoId;
      mTitle = title;

      if (mPlayer != null) {
        mPlayer.loadVideo(mVideoId);
      }
    }
  }

  public void pause() {
    if (mPlayer != null) {
      mPlayer.pause();
    }
  }

  public void mute(boolean muteState) {
    AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
    if (mMuteState != muteState) {
      mMuteState = muteState;
      manager.setStreamMute(AudioManager.STREAM_MUSIC, mMuteState);
    }
  }

  public boolean isMute() {
    return mMuteState;
  }

  public void setFullscreen(boolean state) {
    if (mPlayer != null) {
      mPlayer.setFullscreen(state);
    }
  }

  public boolean fullScreen() {
    if (mPlayer != null) {
      return false;  // SNG needs fix
    }

    return false;
  }

  public void seekRelativeSeconds(int seconds) {
    if (mPlayer != null) {
      mPlayer.seekRelativeMillis(seconds * 1000);
    }
  }

  public void setTimeRemainingListener(TimeRemainingListener listener) {
    mTimeRemainingListener = listener;
  }

  private void setupFullscreenListener() {
    if (mPlayer == null)
      return;

    mPlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
      public void onFullscreen(boolean isFullscreen) {
//    this.isFullscreen = isFullscreen;
//
//    layout();
      }

    });
  }

  private void setupStateChangeListener() {
    if (mPlayer == null)
      return;

    mPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
      @Override
      public void onLoading() {

      }

      @Override
      public void onLoaded(String s) {

      }

      @Override
      public void onAdStarted() {
        if (!isMute()) {
          mMutedForAd = true;
          mute(true);
        }
      }

      @Override
      public void onVideoStarted() {
        if (mMutedForAd) {
          mMutedForAd = false;
          mute(false);
        }
      }

      @Override
      public void onVideoEnded() {
        if (mAutorepeat)
          mPlayer.play();  // back to the start
      }

      @Override
      public void onError(YouTubePlayer.ErrorReason errorReason) {

      }
    });
  }

  private void setupPlaybackEventListener() {
    if (mPlayer == null)
      return;

    mPlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
      @Override
      public void onPlaying() {
        startElapsedTimer();
      }

      @Override
      public void onPaused() {
        stopElapsedTimer();
      }

      @Override
      public void onStopped() {
        stopElapsedTimer();
      }

      @Override
      public void onBuffering(boolean b) {
        Util.log("buffering: " + ((b) ? "yes" : "no"));
      }

      @Override
      public void onSeekTo(int newPositionMillis) {
        Util.log("seeking: " + newPositionMillis/1000 + " seconds");

        final String seekString = Util.millisecondsToDuration(newPositionMillis);
        ApplicationHub.instance().runOnMainThread(new Runnable() {
          @Override
          public void run() {
            // we're on the main thread...
            mTimeRemainingListener.setSeekFlashText(seekString);
          }
        });

      }
    });

  }

  private void stopElapsedTimer() {
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
  }

  private void startElapsedTimer() {
  if (mTimer == null) {
      mTimer = new Timer();
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          if (mTimeRemainingListener != null) {
            long millis = 0;

            if (mPlayer != null)
              millis = mPlayer.getCurrentTimeMillis();

            final String timeString = Util.millisecondsToDuration(millis);

            // added for debugging, remove this shit once we know it's solid
            mLastTimeString = (mLastTimeString == null) ? "" : mLastTimeString;
            if (timeString.equals(mLastTimeString))
              Util.log("equal to last");
            else {
              mLastTimeString = timeString;
            }

            ApplicationHub.instance().runOnMainThread(new Runnable() {
              @Override
              public void run() {
                // we're on the main thread...
                mTimeRemainingListener.setTimeRemainingText(timeString);
              }
            });
          }
        }
      };

      mTimer.schedule(timerTask, 0, 1000);
    }
  }

}