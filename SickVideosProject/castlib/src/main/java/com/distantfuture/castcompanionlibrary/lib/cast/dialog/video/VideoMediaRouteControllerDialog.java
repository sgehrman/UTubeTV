package com.distantfuture.castcompanionlibrary.lib.cast.dialog.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.distantfuture.castcompanionlibrary.lib.R;
import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.cast.callbacks.VideoCastConsumerImpl;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.CastException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.NoConnectionException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.TransientNetworkDisconnectionException;
import com.distantfuture.castcompanionlibrary.lib.utils.CastUtils;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;

import java.net.URL;

/**
 * A custom {@link MediaRouteControllerDialog} that provides an album art, a play/pause button and
 * the ability to take user to the target activity when the album art is tapped.
 */
public class VideoMediaRouteControllerDialog extends MediaRouteControllerDialog {

  private static final String TAG = CastUtils.makeLogTag(VideoMediaRouteControllerDialog.class);
  protected int mState;
  private ImageView mIcon;
  private ImageView mPausePlay;
  private TextView mTitle;
  private TextView mSubTitle;
  private TextView mEmptyText;
  private ProgressBar mLoading;
  private Uri mIconUri;
  private VideoCastManager mCastManager;
  private VideoCastConsumerImpl castConsumerImpl;
  private Drawable mPauseDrawable;
  private Drawable mPlayDrawable;
  private Drawable mStopDrawable;
  private Context mContext;
  private boolean mClosed;
  private View mIconContainer;

  private int mStreamType;

  public VideoMediaRouteControllerDialog(Context context, int theme) {
    super(context, theme);
  }

  /**
   * Creates a new VideoMediaRouteControllerDialog in the given context.
   */
  public VideoMediaRouteControllerDialog(Context context) {
    super(context, R.style.CastDialog);
    try {
      this.mContext = context;
      mCastManager = VideoCastManager.getInstance();
      mState = mCastManager.getPlaybackStatus();
      castConsumerImpl = new VideoCastConsumerImpl() {

        @Override
        public void onRemoteMediaPlayerStatusUpdated() {
          mState = mCastManager.getPlaybackStatus();
          updatePlayPauseState(mState);
        }

        /*
         * (non-Javadoc)
         * @see com.distantfuture.castcompanionlibrary.lib.cast.VideoCastConsumerImpl
         * #onMediaChannelMetadataUpdated()
         */
        @Override
        public void onRemoteMediaPlayerMetadataUpdated() {
          updateMetadata();
        }

      };
      mCastManager.addVideoCastConsumer(castConsumerImpl);
      mPauseDrawable = context.getResources().getDrawable(R.drawable.ic_av_pause_sm_dark);
      mPlayDrawable = context.getResources().getDrawable(R.drawable.ic_av_play_sm_dark);
      mStopDrawable = context.getResources().getDrawable(R.drawable.ic_av_stop_sm_dark);
    } catch (CastException e) {
      CastUtils.LOGE(TAG, "Failed to update the content of dialog", e);
    } catch (IllegalStateException e) {
      CastUtils.LOGE(TAG, "Failed to update the content of dialog", e);
    }
  }

  @Override
  protected void onStop() {
    if (null != mCastManager) {
      mCastManager.removeVideoCastConsumer(castConsumerImpl);
    }
    super.onStop();
  }

  /*
   * Hides/show the icon and metadata and play/pause if there is no media
   */
  private void hideControls(boolean hide, int resId) {
    int visibility = hide ? View.GONE : View.VISIBLE;
    mIcon.setVisibility(visibility);
    mIconContainer.setVisibility(visibility);
    mPausePlay.setVisibility(visibility);
    mTitle.setVisibility(visibility);
    mSubTitle.setVisibility(visibility);
    mEmptyText.setText(resId == 0 ? R.string.no_media_info : resId);
    mEmptyText.setVisibility(hide ? View.VISIBLE : View.GONE);
  }

  private void updateMetadata() {
    MediaInfo info = null;
    try {
      info = mCastManager.getRemoteMediaInformation();
    } catch (TransientNetworkDisconnectionException e) {
      hideControls(true, R.string.failed_no_connection_short);
      return;
    } catch (Exception e) {
      CastUtils.LOGE(TAG, "Failed to get media information", e);
    }
    if (null == info) {
      hideControls(true, R.string.no_media_info);
      return;
    }
    mStreamType = info.getStreamType();
    hideControls(false, 0);
    MediaMetadata mm = info.getMetadata();
    mTitle.setText(mm.getString(MediaMetadata.KEY_TITLE));
    mSubTitle.setText(mm.getString(MediaMetadata.KEY_SUBTITLE));
    setIcon(mm.getImages().get(0).getUrl());
  }

  public void setIcon(Uri uri) {
    if (null != mIconUri && mIconUri.equals(uri)) {
      return;
    }
    mIconUri = uri;
    new Thread(new Runnable() {
      Bitmap bm = null;

      @Override
      public void run() {
        try {
          URL imgUrl = new URL(mIconUri.toString());
          bm = BitmapFactory.decodeStream(imgUrl.openStream());
        } catch (Exception e) {
          CastUtils.LOGE(TAG, "setIcon(): Failed to load the image with url: " +
              mIconUri + ", using the default one", e);
          bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.video_placeholder_200x200);
        }
        if (mClosed) {
          return;
        }
        mIcon.post(new Runnable() {

          @Override
          public void run() {
            mIcon.setImageBitmap(bm);
          }
        });

      }
    }).start();
  }

  private void updatePlayPauseState(int state) {
    if (null != mPausePlay) {
      switch (state) {
        case MediaStatus.PLAYER_STATE_PLAYING:
          mPausePlay.setVisibility(View.VISIBLE);
          mPausePlay.setImageDrawable(getPauseStopButton());
          setLoadingVisibility(false);
          break;
        case MediaStatus.PLAYER_STATE_PAUSED:
          mPausePlay.setVisibility(View.VISIBLE);
          mPausePlay.setImageDrawable(mPlayDrawable);
          setLoadingVisibility(false);
          break;
        case MediaStatus.PLAYER_STATE_IDLE:
          mPausePlay.setVisibility(View.INVISIBLE);
          setLoadingVisibility(false);

          if (mState == MediaStatus.PLAYER_STATE_IDLE && mCastManager.getIdleReason() == MediaStatus.IDLE_REASON_FINISHED) {
            hideControls(true, R.string.no_media_info);
          } else {
            switch (mStreamType) {
              case MediaInfo.STREAM_TYPE_BUFFERED:
                mPausePlay.setVisibility(View.INVISIBLE);
                setLoadingVisibility(false);
                break;
              case MediaInfo.STREAM_TYPE_LIVE:
                int idleReason = mCastManager.getIdleReason();
                if (idleReason == MediaStatus.IDLE_REASON_CANCELED) {
                  mPausePlay.setVisibility(View.VISIBLE);
                  mPausePlay.setImageDrawable(mPlayDrawable);
                  setLoadingVisibility(false);
                } else {
                  mPausePlay.setVisibility(View.INVISIBLE);
                  setLoadingVisibility(false);
                }
                break;
            }
          }
          break;
        case MediaStatus.PLAYER_STATE_BUFFERING:
          mPausePlay.setVisibility(View.INVISIBLE);
          setLoadingVisibility(true);
          break;
        default:
          mPausePlay.setVisibility(View.INVISIBLE);
          setLoadingVisibility(false);
      }
    }
  }

  private Drawable getPauseStopButton() {
    switch (mStreamType) {
      case MediaInfo.STREAM_TYPE_BUFFERED:
        return mPauseDrawable;
      case MediaInfo.STREAM_TYPE_LIVE:
        return mStopDrawable;
      default:
        return mPauseDrawable;
    }
  }

  private void setLoadingVisibility(boolean show) {
    mLoading.setVisibility(show ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (null != castConsumerImpl) {
      mCastManager.removeVideoCastConsumer(castConsumerImpl);
    }
    mClosed = true;
  }

  /**
   * Initializes this dialog's set of playback buttons and adds click listeners.
   */
  @Override
  public View onCreateMediaControlView(Bundle savedInstanceState) {
    LayoutInflater inflater = getLayoutInflater();
    View controls = inflater.inflate(R.layout.custom_media_route_controller_controls_dialog, null);

    loadViews(controls);
    mState = mCastManager.getPlaybackStatus();
    updateMetadata();
    updatePlayPauseState(mState);
    setupCallbacks();
    return controls;
  }

  private void setupCallbacks() {

    mPausePlay.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (null == mCastManager) {
          return;
        }
        try {
          mCastManager.togglePlayback();
          setLoadingVisibility(true);
        } catch (CastException e) {
          setLoadingVisibility(false);
          CastUtils.LOGE(TAG, "Failed to toggle playback", e);
        } catch (TransientNetworkDisconnectionException e) {
          setLoadingVisibility(false);
          CastUtils.LOGE(TAG, "Failed to toggle playback due to network issues", e);
        } catch (NoConnectionException e) {
          setLoadingVisibility(false);
          CastUtils.LOGE(TAG, "Failed to toggle playback due to network issues", e);
        }
      }
    });

    mIcon.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {

        if (null != mCastManager) {
          try {
            mCastManager.onTargetActivityInvoked(mContext);
          } catch (TransientNetworkDisconnectionException e) {
            CastUtils.LOGE(TAG, "Failed to start the target activity due to network issues", e);
          } catch (NoConnectionException e) {
            CastUtils.LOGE(TAG, "Failed to start the target activity due to network issues", e);
          }
          cancel();
        }

      }
    });
  }

  private void loadViews(View controls) {
    mIcon = (ImageView) controls.findViewById(R.id.iconView);
    mIconContainer = controls.findViewById(R.id.iconContainer);
    mPausePlay = (ImageView) controls.findViewById(R.id.playPauseView);
    mTitle = (TextView) controls.findViewById(R.id.titleView);
    mSubTitle = (TextView) controls.findViewById(R.id.subTitleView);
    mLoading = (ProgressBar) controls.findViewById(R.id.loadingView);
    mEmptyText = (TextView) controls.findViewById(R.id.emptyView);
  }
}
