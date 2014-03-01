
package com.distantfuture.castcompanionlibrary.lib.cast.dialog.video;

import android.support.v7.app.MediaRouteDialogFactory;

public class VideoMediaRouteDialogFactory extends MediaRouteDialogFactory {

  @Override
  public VideoMediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
    return new VideoMediaRouteControllerDialogFragment();
  }

}
