package com.distantfuture.castcompanionlibrary.lib.cast;

import android.content.Context;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;

import com.distantfuture.castcompanionlibrary.lib.cast.BaseCastManager.ReconnectionStatus;
import com.distantfuture.castcompanionlibrary.lib.utils.CastUtils;
import com.google.android.gms.cast.CastDevice;

/**
 * Provides a handy implementation of {@link MediaRouter.Callback}. When a {@link RouteInfo} is
 * selected by user from the list of available routes, this class will call the
 * DeviceSelectionListener#setDevice(CastDevice)) of the listener that was passed to it in
 * the constructor. In addition, as soon as a non-default route is discovered, the
 * DeviceSelectionListener#onCastDeviceDetected(CastDevice)) is called.
 * <p/>
 * There is also logic in this class to help with the process of previous session recovery.
 */
public class CastMediaRouterCallback extends MediaRouter.Callback {
  private static final String TAG = CastUtils.makeLogTag(CastMediaRouterCallback.class);
  private final DeviceSelectionListener selectDeviceInterface;
  private final Context mContext;

  public CastMediaRouterCallback(DeviceSelectionListener callback, Context context) {
    this.selectDeviceInterface = callback;
    this.mContext = context;
  }

  @Override
  public void onRouteSelected(MediaRouter router, RouteInfo info) {
    CastUtils.LOGD(TAG, "onRouteSelected: info=" + info);
    if (BaseCastManager.getCastManager()
        .getReconnectionStatus() == BaseCastManager.ReconnectionStatus.FINALIZE) {
      BaseCastManager.getCastManager().setReconnectionStatus(ReconnectionStatus.INACTIVE);
      BaseCastManager.getCastManager().cancelReconnectionTask();
      return;
    }
    CastUtils.saveStringToPreference(mContext, BaseCastManager.PREFS_KEY_ROUTE_ID, info.getId());
    CastDevice device = CastDevice.getFromBundle(info.getExtras());
    selectDeviceInterface.onDeviceSelected(device);
    CastUtils.LOGD(TAG, "onResult: mSelectedDevice=" + device.getFriendlyName());
  }

  @Override
  public void onRouteUnselected(MediaRouter router, RouteInfo route) {
    CastUtils.LOGD(TAG, "onRouteUnselected: route=" + route);
    selectDeviceInterface.onDeviceSelected(null);
  }

  @Override
  public void onRouteAdded(MediaRouter router, RouteInfo route) {
    super.onRouteAdded(router, route);
    if (!router.getDefaultRoute().equals(route)) {
      selectDeviceInterface.onCastDeviceDetected(route);
    }
    if (BaseCastManager.getCastManager().getReconnectionStatus() == ReconnectionStatus.STARTED) {
      String routeId = CastUtils.getStringFromPreference(mContext, BaseCastManager.PREFS_KEY_ROUTE_ID);
      if (route.getId().equals(routeId)) {
        // we found the route, so lets go with that
        CastUtils.LOGD(TAG, "onRouteAdded: Attempting to recover a session with info=" + route);
        BaseCastManager.getCastManager().setReconnectionStatus(ReconnectionStatus.IN_PROGRESS);

        CastDevice device = CastDevice.getFromBundle(route.getExtras());
        CastUtils.LOGD(TAG, "onRouteAdded: Attempting to recover a session with device: " + device.getFriendlyName());
        selectDeviceInterface.onDeviceSelected(device);
      }
    }
  }

}
