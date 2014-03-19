package com.distantfuture.videos.misc;

import com.distantfuture.videos.youtube.VideoPlayer;

import java.util.Map;

public class BusEvents {

  public static class ContentEvent {
    public ContentEvent() {
    }
  }

  public static class PurchaseEvent {
    public String alert;
    public String message;
    public boolean successfulDonation;

    public PurchaseEvent(String message, String alert, boolean successfulDonation) {
      this.alert = alert;
      this.message = message;
      this.successfulDonation = successfulDonation;
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

  public static class YouTubeFragmentDataReady {
    public YouTubeFragmentDataReady() {
    }
  }

  public static class JSONImportEvent {
    public Map channels;

    public JSONImportEvent(Map channels) {
      this.channels = channels;
    }
  }

  public static class PlayNextEvent {
    public VideoPlayer.PlayerParams params;

    public PlayNextEvent(VideoPlayer.PlayerParams params) {
      this.params = params;
    }
  }
}
