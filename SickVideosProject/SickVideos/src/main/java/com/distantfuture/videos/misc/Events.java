package com.distantfuture.videos.misc;

import com.distantfuture.videos.youtube.VideoPlayer;

public class Events {

  public static class ContentEvent {
    public ContentEvent() {
    }
  }

  public static class PurchaseEvent {
    public String alert;
    public String message;

    public PurchaseEvent(String message, String alert) {
      this.alert = alert;
      this.message = message;
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
