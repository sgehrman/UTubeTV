package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.sickboots.sickvideos.R;

import java.io.IOException;

public class ChromecastHelper {
  private static final String TAG = "MainActivity";
  private Context mContext;
  private MediaRouter mMediaRouter;
  private MediaRouter.Callback mMediaRouterCallback;
  private MediaRouteSelector mMediaRouteSelector;
  private CastDevice mCastDevice;
  private GoogleApiClient mGoogleApiClient;
  private Cast.Listener mCastListener;
  private ConnectionCallbacks mConnectionCallbacks;
  private ConnectionFailedListener mConnectionFailedListener;
  private TextChannel mTextChannel;
  private boolean mApplicationStarted;
  private boolean mWaitingForReconnect;

  public ChromecastHelper(Context context) {
    mContext = context.getApplicationContext();

    mMediaRouter = MediaRouter.getInstance(mContext);
    mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent
        .categoryForCast(mContext.getString(R.string.chromecast_app_id))).build();
    mMediaRouterCallback = new MediaRouterCallback();
  }

  // ## must call from activity
  public void createOptionsMenu(Menu menu) {

    /*
        <item
        android:id="@+id/action_cast"
        android:title="@string/action_cast"
        android:orderInCategory="100"
        android:showAsAction="always"/>

     */

    MenuItem item = menu.findItem(R.id.action_cast);
    if (item != null) {
      MediaRouteButton button = new MediaRouteButton(mContext);
      button.setRouteSelector(mMediaRouteSelector);

      item.setActionView(button);
    }
  }

  // ## must call from activity
  public void resume() {
    mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
  }

  // ## must call from activity
  public void pause(boolean isFinishing) {
    if (isFinishing) {
      mMediaRouter.removeCallback(mMediaRouterCallback);
    }
  }

  // ## must call from activity
  public void destroy() {
    doneCast();
  }

  private void sendText(String text) {
    if (null != mGoogleApiClient && null != mTextChannel) {
      try {
        Cast.CastApi.sendMessage(mGoogleApiClient, mTextChannel.getNamespace(), text)
            .setResultCallback(new ResultCallback<Status>() {
              @Override
              public void onResult(Status status) {
                if (!status.isSuccess()) {
                  // TODO
                }
              }
            });
      } catch (Exception ex) {
        Log.e(TAG, "sendText: ", ex);
      }
    } else {
      Debug.log(text);
    }
  }

  private void initCast(Bundle bundle) {
    try {
      mCastDevice = CastDevice.getFromBundle(bundle);
      mCastListener = new CastListener();
      mConnectionCallbacks = new ConnectionCallbacks();
      mConnectionFailedListener = new ConnectionFailedListener();

      Cast.CastOptions.Builder optionsBuilder = Cast.CastOptions.builder(mCastDevice, mCastListener);
      mGoogleApiClient = new GoogleApiClient.Builder(mContext).addApi(Cast.API, optionsBuilder.build())
          .addConnectionCallbacks(mConnectionCallbacks)
          .addOnConnectionFailedListener(mConnectionFailedListener)
          .build();

      mGoogleApiClient.connect();
    } catch (Exception ex) {
      Log.e(TAG, "initCast: ", ex);
    }
  }

  private void doneCast() {
    if (null != mGoogleApiClient) {
      if (mApplicationStarted) {
        try {
          Cast.CastApi.stopApplication(mGoogleApiClient);
          if (null != mTextChannel) {
            Cast.CastApi.removeMessageReceivedCallbacks(mGoogleApiClient, mTextChannel.getNamespace());
            mTextChannel = null;
          }
        } catch (IOException ex) {
          Log.e(TAG, "doneCast: ", ex);
        }
        mApplicationStarted = false;
      }

      if (mGoogleApiClient.isConnected()) {
        mGoogleApiClient.disconnect();
      }
      mGoogleApiClient = null;
    }

    mCastDevice = null;
    mWaitingForReconnect = false;
  }

  private void setChannel() {
    try {
      Cast.CastApi.setMessageReceivedCallbacks(mGoogleApiClient, mTextChannel.getNamespace(), mTextChannel);
    } catch (IOException ex) {
      Log.e(TAG, "setChannel: ", ex);
    }
  }

  private class MediaRouterCallback extends MediaRouter.Callback {
    @Override
    public void onRouteSelected(MediaRouter router, RouteInfo info) {
      initCast(info.getExtras());
    }

    @Override
    public void onRouteUnselected(MediaRouter router, RouteInfo info) {
      doneCast();
    }
  }

  private class CastListener extends Cast.Listener {
    @Override
    public void onApplicationDisconnected(int errorCode) {
      doneCast();
    }
  }

  private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
    @Override
    public void onConnected(Bundle connectionHint) {
      if (null == mGoogleApiClient)
        return;

      try {
        if (mWaitingForReconnect) {
          mWaitingForReconnect = false;

          if ((null != connectionHint) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
            doneCast();
          } else {
            setChannel();
          }
        } else {
          Cast.CastApi.launchApplication(mGoogleApiClient, mContext.getString(R.string.chromecast_app_id), false)
              .setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                @Override
                public void onResult(Cast.ApplicationConnectionResult result) {
                  Status status = result.getStatus();
                  if (status.isSuccess()) {
                    mApplicationStarted = true;
                    mTextChannel = new TextChannel();
                    setChannel();
                    sendText("yo, niggas");
                  } else {
                    doneCast();
                  }
                }
              });

        }
      } catch (Exception ex) {
        Log.e(TAG, "onConnected: ", ex);
      }
    }

    @Override
    public void onConnectionSuspended(int cause) {
      mWaitingForReconnect = true;
    }
  }

  private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(ConnectionResult result) {
      doneCast();
    }
  }

  private class TextChannel implements MessageReceivedCallback {
    public String getNamespace() {
      return "urn:x-cast:" + Utils.getApplicationPackageName(mContext);
    }

    @Override
    public void onMessageReceived(CastDevice device, String namespace, String message) {
      Debug.log(message);
    }
  }

}
