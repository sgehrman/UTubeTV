package com.sickboots.sickvideos;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

public final class VideoPlayerFragment extends YouTubePlayerFragment {
  private boolean mAutorepeat = false;
  private YouTubePlayer player;
  private String videoId;
  private boolean mMuteState = false;
  private boolean mMutedForAd = false;

  public static VideoPlayerFragment newInstance() {
    return new VideoPlayerFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initialize(YouTubeAPI.devKey(), new YouTubePlayer.OnInitializedListener() {
      @Override
      public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {
        VideoPlayerFragment.this.player = player;
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

        player.setShowFullscreenButton(true);

        setupFullscreenListener();
        setupStateChangeListener();

        if (!restored && videoId != null) {
          player.loadVideo(videoId);
        }
      }

      @Override
      public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        VideoPlayerFragment.this.player = null;
      }
    });
  }

  @Override
  public void onDestroy() {
    if (player != null) {
      player.release();
      player = null;
    }

    super.onDestroy();
  }

  public void setVideoId(String videoId) {
    if (videoId != null && !videoId.equals(this.videoId)) {
      this.videoId = videoId;
      if (player != null) {
        player.loadVideo(videoId);
      }
    }
  }

  public void pause() {
    if (player != null) {
      player.pause();
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

  private void setupFullscreenListener() {
    if (player == null)
      return;

    player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
      public void onFullscreen(boolean isFullscreen) {
//    this.isFullscreen = isFullscreen;
//
//    layout();
      }

    });
  }

  private void setupStateChangeListener() {
    if (player == null)
      return;

    player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
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
          player.play();  // back to the start
      }

      @Override
      public void onError(YouTubePlayer.ErrorReason errorReason) {

      }
    });
  }
}