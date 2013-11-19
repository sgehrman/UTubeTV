package com.sickboots.sickvideos;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.Timer;
import java.util.TimerTask;

public final class VideoPlayerFragment extends YouTubePlayerFragment {
  private boolean mAutorepeat = false;
  private YouTubePlayer mPlayer;
  private String mVideoId;
  private String mTitle;
  private boolean mMuteState = false;
  private boolean mMutedForAd = false;
  private Timer mTimer;

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
        stopElapsedTimer();
      }

      @Override
      public void onSeekTo(int i) {
        stopElapsedTimer();
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

          Util.log("video time: " + mPlayer.getCurrentTimeMillis());
        }
      };

      mTimer.schedule(timerTask, 0, 1000);
    }
  }

}