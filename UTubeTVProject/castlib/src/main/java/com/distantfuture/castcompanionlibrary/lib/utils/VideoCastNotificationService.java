package com.distantfuture.castcompanionlibrary.lib.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.distantfuture.castcompanionlibrary.lib.R;
import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.cast.callbacks.VideoCastConsumerImpl;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.CastException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.NoConnectionException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.TransientNetworkDisconnectionException;
import com.distantfuture.castcompanionlibrary.lib.cast.player.VideoCastControllerActivity;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A service to provide status bar Notifications when we are casting. For JB+ versions, notification
 * area provides a play/pause toggle and an "x" button to disconnect but that for GB, we do not show
 * that due to the framework limitations.
 */
public class VideoCastNotificationService extends Service {

  public static final String ACTION_TOGGLE_PLAYBACK = "com.distantfuture.castcompanionlibrary.lib.action.toggleplayback";
  public static final String ACTION_STOP = "com.distantfuture.castcompanionlibrary.lib.action.stop";
  public static final String ACTION_VISIBILITY = "com.distantfuture.castcompanionlibrary.lib.action.notificationvisibility";
  private static final String TAG = CastUtils.makeLogTag(VideoCastNotificationService.class);
  private static int NOTIFICATION_ID = 1;
  private String mApplicationId;
  private Bitmap mVideoArtBitmap;
  private Uri mVideoArtUri;
  private boolean mIsPlaying;
  private Notification mNotification;
  private boolean mVisible;
  private BroadcastReceiver mBroadcastReceiver;
  private VideoCastManager mCastManager;
  private VideoCastConsumerImpl mConsumer;

  @Override
  public void onCreate() {
    super.onCreate();
    readPersistedData();
    mCastManager = VideoCastManager.initialize(this, mApplicationId, null);
    if (!mCastManager.isConnected()) {
      mCastManager.reconnectSessionIfPossible(this, false);
    }
    mConsumer = new VideoCastConsumerImpl() {
      @Override
      public void onApplicationDisconnected(int errorCode) {
        CastUtils.LOGD(TAG, "onApplicationDisconnected() was reached");
        stopSelf();
      }

      @Override
      public void onRemoteMediaPlayerStatusUpdated() {
        int mediaStatus = mCastManager.getPlaybackStatus();
        VideoCastNotificationService.this.onRemoteMediaPlayerStatusUpdated(mediaStatus);
      }

    };
    mCastManager.addVideoCastConsumer(mConsumer);
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (null != intent) {

      String action = intent.getAction();
      if (ACTION_TOGGLE_PLAYBACK.equals(action)) {
        CastUtils.LOGD(TAG, "onStartCommand(): Action: ACTION_TOGGLE_PLAYBACK");
        togglePlayback();
      } else if (ACTION_STOP.equals(action)) {
        CastUtils.LOGD(TAG, "onStartCommand(): Action: ACTION_STOP");
        stopApplication();
      } else if (ACTION_VISIBILITY.equals(action)) {
        mVisible = intent.getBooleanExtra("visible", false);
        CastUtils.LOGD(TAG, "onStartCommand(): Action: ACTION_VISIBILITY " + mVisible);
        if (mVisible && null != mNotification) {
          startForeground(NOTIFICATION_ID, mNotification);
        } else {
          stopForeground(true);
        }
      } else {
        CastUtils.LOGD(TAG, "onStartCommand(): Action: none");
      }

    } else {
      CastUtils.LOGD(TAG, "onStartCommand(): Intent was null");
    }

    return Service.START_FLAG_REDELIVERY;
  }

  private void setupNotification(final MediaInfo info, final boolean visible) throws TransientNetworkDisconnectionException, NoConnectionException {
    if (null == info) {
      return;
    }
    try {
      MediaMetadata mm = info.getMetadata();
      Uri uri = null;
      if (!mm.getImages().isEmpty()) {
        uri = mm.getImages().get(0).getUrl();
      }
      if (null == uri) {
        build(info, null, mIsPlaying);
        if (visible) {
          startForeground(NOTIFICATION_ID, mNotification);
        }
      } else if (null != mVideoArtBitmap && null != mVideoArtUri &&
          mVideoArtUri.equals(uri)) {
        build(info, mVideoArtBitmap, mIsPlaying);
        if (visible) {
          startForeground(NOTIFICATION_ID, mNotification);
        }
      } else {
        new Thread(new Runnable() {

          @Override
          public void run() {
            URL imgUrl = null;
            try {
              MediaMetadata mm = info.getMetadata();
              mVideoArtUri = mm.getImages().get(0).getUrl();
              imgUrl = new URL(mVideoArtUri.toString());
              mVideoArtBitmap = BitmapFactory.decodeStream(imgUrl.openStream());
              build(info, mVideoArtBitmap, mIsPlaying);
              if (visible) {
                startForeground(NOTIFICATION_ID, mNotification);
              }
            } catch (MalformedURLException e) {
              CastUtils.LOGE(TAG, "setIcon(): Failed to load the image with url: " +
                  imgUrl + ", using the default one", e);
            } catch (IOException e) {
              CastUtils.LOGE(TAG, "setIcon(): Failed to load the image with url: " +
                  imgUrl + ", using the default one", e);
            } catch (CastException e) {
              CastUtils.LOGE(TAG, "setIcon(): Failed to load the image with url: " +
                  imgUrl + ", using the default one", e);
            } catch (TransientNetworkDisconnectionException e) {
              CastUtils.LOGE(TAG, "setIcon(): Failed to load the image with url: " +
                  imgUrl + " due to network issues, using the default one", e);
            } catch (NoConnectionException e) {
              CastUtils.LOGE(TAG, "setIcon(): Failed to load the image with url: " +
                  imgUrl + " due to network issues, using the default one", e);
            }

          }
        }).start();
      }
    } catch (CastException e) {
      // already logged
    }
  }

  /**
   * Removes the existing notification.
   */
  private void removeNotification() {
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).
        cancel(NOTIFICATION_ID);
  }

  private void onRemoteMediaPlayerStatusUpdated(int mediaStatus) {
    CastUtils.LOGD(TAG, "onRemoteMediaPlayerMetadataUpdated() reached with status: " + mediaStatus);
    try {
      switch (mediaStatus) {
        case MediaStatus.PLAYER_STATE_BUFFERING: // (== 4)
          mIsPlaying = false;
          setupNotification(mCastManager.getRemoteMediaInformation(), mVisible);
          break;
        case MediaStatus.PLAYER_STATE_PLAYING: // (== 2)
          mIsPlaying = true;
          setupNotification(mCastManager.getRemoteMediaInformation(), mVisible);
          break;
        case MediaStatus.PLAYER_STATE_PAUSED: // (== 3)
          mIsPlaying = false;
          setupNotification(mCastManager.getRemoteMediaInformation(), mVisible);
          break;
        case MediaStatus.PLAYER_STATE_IDLE: // (== 1)
          mIsPlaying = false;
          if (!mCastManager.shouldRemoteUiBeVisible(mediaStatus, mCastManager.getIdleReason())) {
            stopForeground(true);
          } else {
            setupNotification(mCastManager.getRemoteMediaInformation(), mVisible);
          }
          break;
        case MediaStatus.PLAYER_STATE_UNKNOWN: // (== 0)
          mIsPlaying = false;
          stopForeground(true);
          break;
        default:
          break;
      }
    } catch (TransientNetworkDisconnectionException e) {
      CastUtils.LOGE(TAG, "Failed to update the playback status due to network issues", e);
    } catch (NoConnectionException e) {
      CastUtils.LOGE(TAG, "Failed to update the playback status due to network issues", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see android.app.Service#onDestroy()
   */
  @Override
  public void onDestroy() {
    CastUtils.LOGD(TAG, "onDestroy was called");
    removeNotification();
    if (null != mBroadcastReceiver) {
      unregisterReceiver(mBroadcastReceiver);
    }
    if (null != mCastManager && null != mConsumer) {
      mCastManager.removeVideoCastConsumer(mConsumer);
      mCastManager = null;
    }
  }

  /*
   * Build the RemoteViews for the notification. We also need to add the appropriate "back stack"
   * so when user goes into the CastPlayerActivity, she can have a meaningful "back" experience.
   */
  private RemoteViews build(MediaInfo info, Bitmap bitmap, boolean isPlaying) throws CastException, TransientNetworkDisconnectionException, NoConnectionException {
    Bundle mediaWrapper = CastUtils.fromMediaInfo(mCastManager.getRemoteMediaInformation());
    Intent contentIntent = null;
    contentIntent = new Intent(this, VideoCastControllerActivity.class);

    contentIntent.putExtra("media", mediaWrapper);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

    stackBuilder.addParentStack(VideoCastControllerActivity.class);

    stackBuilder.addNextIntent(contentIntent);

    stackBuilder.editIntentAt(1).putExtra("media", mediaWrapper);

    // Gets a PendingIntent containing the entire back stack
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_UPDATE_CURRENT);

    MediaMetadata mm = info.getMetadata();

    RemoteViews rv = new RemoteViews(getPackageName(), R.layout.custom_notification);
    addPendingIntents(rv, isPlaying, info);

    if (null != bitmap) {
      rv.setImageViewBitmap(R.id.iconView, bitmap);
    }
    rv.setTextViewText(R.id.titleView, mm.getString(MediaMetadata.KEY_TITLE));
    String castingTo = getResources().getString(R.string.casting_to_device, mCastManager.getDeviceName());
    rv.setTextViewText(R.id.subTitleView, castingTo);
    mNotification = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_stat_action_notification)
        .setContentIntent(resultPendingIntent)
        .setContent(rv)
        .setAutoCancel(false)
        .setOngoing(true)
        .build();

    // to get around a bug in GB version, we add the following line
    // see https://code.google.com/p/android/issues/detail?id=30495
    mNotification.contentView = rv;

    return rv;
  }

  private void addPendingIntents(RemoteViews rv, boolean isPlaying, MediaInfo info) {
    Intent playbackIntent = new Intent(ACTION_TOGGLE_PLAYBACK);
    playbackIntent.setPackage(getPackageName());
    PendingIntent playbackPendingIntent = PendingIntent.getBroadcast(this, 0, playbackIntent, 0);

    Intent stopIntent = new Intent(ACTION_STOP);
    stopIntent.setPackage(getPackageName());
    PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

    rv.setOnClickPendingIntent(R.id.playPauseView, playbackPendingIntent);
    rv.setOnClickPendingIntent(R.id.removeView, stopPendingIntent);

    if (isPlaying) {
      if (info.getStreamType() == MediaInfo.STREAM_TYPE_LIVE) {
        rv.setImageViewResource(R.id.playPauseView, R.drawable.ic_av_stop_sm_dark);
      } else {
        rv.setImageViewResource(R.id.playPauseView, R.drawable.ic_av_pause_sm_dark);
      }

    } else {
      rv.setImageViewResource(R.id.playPauseView, R.drawable.ic_av_play_sm_dark);
    }
  }

  private void togglePlayback() {
    try {
      mCastManager.togglePlayback();
    } catch (Exception e) {
      CastUtils.LOGE(TAG, "Failed to toggle the playback", e);
    }
  }

  /*
   * We try to disconnect application but even if that fails, we need to remove notification since
   * that is the only way to get rid of it without going to the application
   */
  private void stopApplication() {
    try {
      CastUtils.LOGD(TAG, "Calling stopApplication");
      mCastManager.disconnect();
    } catch (Exception e) {
      CastUtils.LOGE(TAG, "Failed to disconnect application", e);
    }
    stopSelf();
  }

  /*
   * Reads application ID and target activity from preference storage.
   */
  private void readPersistedData() {
    mApplicationId = CastUtils.getStringFromPreference(this, VideoCastManager.PREFS_KEY_APPLICATION_ID);
  }
}
