package com.sickboots.sickvideos.misc;

import com.sickboots.sickvideos.youtube.VideoPlayer;

public class Events {

  public static class ContentEvent {
    public ContentEvent() {
    }
  }

  public static class ThemeChanged {
    public ThemeChanged() {
    }
  }

  public static class ConnectionChanged {
    public ConnectionChanged() {
    }
  }

  public static class PlayNextEvent {
    public VideoPlayer.PlayerParams params;

    public PlayNextEvent(VideoPlayer.PlayerParams params) {
      this.params = params;
    }
  }
}
