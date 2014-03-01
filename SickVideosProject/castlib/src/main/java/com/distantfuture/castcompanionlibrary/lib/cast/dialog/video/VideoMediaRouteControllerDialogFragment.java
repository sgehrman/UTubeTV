
package com.distantfuture.castcompanionlibrary.lib.cast.dialog.video;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialogFragment;

/**
 * An extension of MediaRouteControllerDialogFragment which contains a
 * VideoMediaRouteControllerDialog.
 */
public class VideoMediaRouteControllerDialogFragment extends MediaRouteControllerDialogFragment {

  private VideoMediaRouteControllerDialog mCustomControllerDialog;

  @Override
  public VideoMediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
    mCustomControllerDialog = new VideoMediaRouteControllerDialog(context);
    mCustomControllerDialog.setVolumeControlEnabled(false);
    return mCustomControllerDialog;
  }
}
