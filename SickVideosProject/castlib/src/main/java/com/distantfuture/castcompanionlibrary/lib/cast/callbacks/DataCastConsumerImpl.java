
package com.distantfuture.castcompanionlibrary.lib.cast.callbacks;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.Status;

/**
 * A no-op implementation of the {@link IDataCastConsumer}
 */
public class DataCastConsumerImpl extends BaseCastConsumerImpl implements IDataCastConsumer {

  @Override
  public void onApplicationConnected(ApplicationMetadata appMetadata, String applicationStatus, String sessionId, boolean wasLaunched) {
  }

  @Override
  public void onApplicationDisconnected(int errorCode) {
  }

  @Override
  public void onApplicationStopFailed(int errorCode) {
  }

  @Override
  public boolean onApplicationConnectionFailed(int errorCode) {
    return true;
  }

  @Override
  public void onApplicationStatusChanged(String appStatus) {

  }

  @Override
  public void onVolumeChanged(double value, boolean isMute) {
  }

  @Override
  public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
  }

  @Override
  public void onMessageSendFailed(Status status) {
  }

  @Override
  public void onRemoved(CastDevice castDevice, String namespace) {
  }

}
