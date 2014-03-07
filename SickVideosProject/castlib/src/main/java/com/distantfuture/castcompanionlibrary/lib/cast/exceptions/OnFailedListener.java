package com.distantfuture.castcompanionlibrary.lib.cast.exceptions;

public interface OnFailedListener {

  /**
   * An interface for reporting back errors in an asynchronous way.
   * <p/>
   * param resourceId The resource that has a textual description of the problem
   * param statusCode An additional integer to further specify the error. Value -1 would be
   * interpreted as no data available.
   */
  public void onFailed(int resourceId, int statusCode);
}
