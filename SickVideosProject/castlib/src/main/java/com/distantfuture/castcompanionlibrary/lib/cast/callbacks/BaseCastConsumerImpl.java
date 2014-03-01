
package com.distantfuture.castcompanionlibrary.lib.cast.callbacks;

import android.support.v7.media.MediaRouter.RouteInfo;

import com.google.android.gms.common.ConnectionResult;

public class BaseCastConsumerImpl implements IBaseCastConsumer {

  @Override
  public void onConnected() {
  }

  @Override
  public void onDisconnected() {
  }

  @Override
  public boolean onConnectionFailed(ConnectionResult result) {
    return true;
  }

  @Override
  public void onCastDeviceDetected(RouteInfo info) {
  }

  @Override
  public void onConnectionSuspended(int cause) {
  }

  @Override
  public void onConnectivityRecovered() {
  }

  @Override
  public void onFailed(int resourceId, int statusCode) {
  }

}
