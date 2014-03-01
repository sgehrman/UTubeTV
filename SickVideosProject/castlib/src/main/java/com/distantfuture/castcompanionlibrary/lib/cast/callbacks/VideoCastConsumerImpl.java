
package com.distantfuture.castcompanionlibrary.lib.cast.callbacks;

import com.google.android.gms.cast.ApplicationMetadata;

/**
 * This is a no-ops implementation of {@link IVideoCastConsumer} so that the clients that like to
 * (partially) implement {@link IVideoCastConsumer} can extend this class and only override the
 * desired methods.
 */
public class VideoCastConsumerImpl extends BaseCastConsumerImpl implements IVideoCastConsumer {

  @Override
  public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
  }

  @Override
  public boolean onApplicationConnectionFailed(int errorCode) {
    return true;
  }

  @Override
  public void onApplicationStatusChanged(String appStatus) {
  }

  @Override
  public void onApplicationDisconnected(int errorCode) {
  }

  @Override
  public void onRemoteMediaPlayerMetadataUpdated() {
  }

  @Override
  public void onRemoteMediaPlayerStatusUpdated() {
  }

  @Override
  public void onVolumeChanged(double value, boolean isMute) {
  }

  @Override
  public void onApplicationStopFailed(int errorCode) {
  }

  @Override
  public void onRemovedNamespace() {
  }

  @Override
  public void onDataMessageSendFailed(int errorCode) {
  }

  @Override
  public void onDataMessageReceived(String message) {
  }

}
