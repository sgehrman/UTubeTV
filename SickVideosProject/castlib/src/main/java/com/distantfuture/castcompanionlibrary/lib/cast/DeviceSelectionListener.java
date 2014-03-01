
package com.distantfuture.castcompanionlibrary.lib.cast;

import android.support.v7.media.MediaRouter.RouteInfo;

import com.google.android.gms.cast.CastDevice;

/**
 * An interface that will be used to inform clients that a {@link CastDevice} is discovered by the
 * system or selected by the user.
 */
public interface DeviceSelectionListener {

  /**
   * Called when a {@link CastDevice} is extracted from the {@link RouteInfo}. This is where all
   * the fun starts!
   */
  public void onDeviceSelected(CastDevice device);

  /**
   * Called as soon as a non-default {@link RouteInfo} is discovered. The main usage for this is
   * to provide a hint to clients that the cast button is going to become visible/available soon.
   * A client, for example, can use this to show a quick help screen to educate the user on the
   * cast concept and the usage of the cast button.
   */
  public void onCastDeviceDetected(RouteInfo route);

}
