package com.sickboots.sickvideos;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

public final class VideoPlayerFragment extends YouTubePlayerFragment implements YouTubePlayer.OnFullscreenListener, YouTubePlayer.OnInitializedListener {

  private YouTubePlayer player;
  private String videoId;

  public static VideoPlayerFragment newInstance() {
    return new VideoPlayerFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initialize(YouTubeAPI.devKey(), this);
  }

  @Override
  public void onDestroy() {
    if (player != null) {
      player.release();
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

  @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {
    this.player = player;
    player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
    player.setOnFullscreenListener(this);
    if (!restored && videoId != null) {
      player.loadVideo(videoId);
    }
  }

  @Override
  public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
    this.player = null;
  }

  // YouTubePlayer.OnFullscreenListener
  public void onFullscreen(boolean isFullscreen) {
//    this.isFullscreen = isFullscreen;
//
//    layout();
  }


}