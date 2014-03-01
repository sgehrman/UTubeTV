
package com.distantfuture.castcompanionlibrary.lib.cast.callbacks;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;

public interface IVideoCastConsumer extends IBaseCastConsumer {

  /**
   * Called when the application is successfully launched or joined. Upon successful connection, a
   * session ID is returned. <code>wasLaunched</code> indicates if the application was launched or
   * joined.
   */
  public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched);

  /**
   * Called when an application launch has failed. Failure reason is captured in the
   * <code>errorCode</code> argument. Here is a list of possible values:
   * <ul>
   * <li>4 : Application not found
   * <li>5 : Application not currently running
   * <li>6 : Application already running
   * </ul>
   * If this method returns <code>true</code>, then the library will provide an error dialog to
   * inform the user. Clients can extend this method and return <code>false</code> to handle the
   * error message themselves.
   *
   * return <code>true</code> if you want the library handle the error message
   */
  public boolean onApplicationConnectionFailed(int errorCode);

  /**
   * Called when an attempt to stop a receiver application has failed.
   */
  public void onApplicationStopFailed(int errorCode);

  /**
   * Called when application status changes. The argument is built by the receiver
   */
  public void onApplicationStatusChanged(String appStatus);

  /**
   * Called when the device's volume is changed. Note not to mix that with the stream's volume
   */
  public void onVolumeChanged(double value, boolean isMute);

  /**
   * Called when the current application has stopped
   */
  public void onApplicationDisconnected(int errorCode);

  /**
   * Called when metadata of the current media changes
   */
  public void onRemoteMediaPlayerMetadataUpdated();

  /**
   * Called when media's status updated.
   */
  public void onRemoteMediaPlayerStatusUpdated();

  /**
   * Called when the data channel callback is removed from the {@link Cast} object.
   */
  public void onRemovedNamespace();

  /**
   * Called when there is an error sending a message.
   *
   * param messageId The ID of the message that could not be sent.
   * param errorCode An error code indicating the reason for the disconnect. One of the error
   *                  constants defined in CastErrors.
   */
  public void onDataMessageSendFailed(int errorCode);

  /**
   * Called when a message is received from a given {@link CastDevice}.
   */
  public void onDataMessageReceived(String message);

}
