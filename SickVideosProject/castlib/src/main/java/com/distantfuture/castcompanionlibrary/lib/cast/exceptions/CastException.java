
package com.distantfuture.castcompanionlibrary.lib.cast.exceptions;

import android.content.Context;

public class CastException extends Exception {

  private static final long serialVersionUID = 1L;

  public CastException() {
    super();
  }

  public CastException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public CastException(String detailMessage) {
    super(detailMessage);
  }

  public CastException(Context ctx, int resId) {
    super(ctx.getResources().getString(resId));
  }

  public CastException(Context ctx, int resId, Exception e) {
    super(ctx.getResources().getString(resId), e);
  }

  public CastException(Throwable throwable) {
    super(throwable);
  }

}
