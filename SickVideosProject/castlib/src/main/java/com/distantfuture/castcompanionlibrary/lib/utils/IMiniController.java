
package com.distantfuture.castcompanionlibrary.lib.utils;

import android.net.Uri;

import com.distantfuture.castcompanionlibrary.lib.utils.MiniController.OnMiniControllerChangedListener;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;

/**
 * An interface to abstract {@link com.distantfuture.castcompanionlibrary.lib.utils.MiniController} so that other components can also control the
 * MiniControllers. Clients should code against this interface when they want to control the
 * provided {@link com.distantfuture.castcompanionlibrary.lib.utils.MiniController} or other custom implementations.
 */
public interface IMiniController {

  /**
   * Sets the uri for the album art
   */
  public void setIcon(Uri uri);

  /**
   * Sets the title
   */
  public void setTitle(String title);

  /**
   * Sets the subtitle
   */
  public void setSubTitle(String subTitle);

  /**
   * Sets the playback state, and the idleReason (this is only reliable when the state is idle).
   * Values that can be passed to this method are from {@link MediaStatus}
   */
  public void setPlaybackStatus(int state, int idleReason);

  /**
   * Sets whether this component should be visible or hidden.
   */
  public void setVisibility(int visibility);

  /**
   * Returns the visibility state of this widget
   */
  public boolean isVisible();

  /**
   * Assigns a {@link OnMiniControllerChangedListener} listener to be notified of the changes in
   * the mini controller
   */
  public void setOnMiniControllerChangedListener(OnMiniControllerChangedListener listener);

  /**
   * Sets the type of stream. <code>streamType</code> can be MediaInfo.STREAM_TYPE_LIVE or
   * MediaInfo.STREAM_TYPE_BUFFERED
   */
  public void setStreamType(int streamType);

}
