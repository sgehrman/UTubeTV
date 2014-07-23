package com.distantfuture.castcompanionlibrary.lib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.distantfuture.castcompanionlibrary.lib.R;
import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.CastException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.NoConnectionException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.OnFailedListener;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;

import java.net.URL;

/**
 * A compound component that provides a superset of functionalities required for the global access
 * requirement. This component provides an image for the album art, a play/pause button, a seekbar
 * for trick-play with current time and duration and a mute/unmute button. Clients can add this
 * compound component to their layout xml and register that with the instance of
 * {@link VideoCastManager} by using the following pattern:<br/>
 * <p/>
 * <pre>
 * mMiniController = (MiniController) findViewById(R.id.miniController1);
 * mCastManager.addMiniController(mMiniController);
 * mMiniController.setOnMiniControllerChangedListener(mCastManager);
 * </pre>
 * <p/>
 * Then the {@link VideoCastManager} will manage the behavior, including its state and metadata and
 * interactions.
 */
public class MiniController extends RelativeLayout implements IMiniController {

  private static final String TAG = "MiniController";
  protected ImageView mIcon;
  protected TextView mTitle;
  protected TextView mSubTitle;
  protected ImageView mPlayPause;
  protected ProgressBar mLoading;
  private OnMiniControllerChangedListener mListener;
  private Uri mIconUri;
  private Drawable mPauseDrawable;
  private Drawable mPlayDrawable;
  private View mContainer;
  private int mStreamType = MediaInfo.STREAM_TYPE_BUFFERED;
  private Drawable mStopDrawable;

  public MiniController(Context context, AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater inflater = LayoutInflater.from(context);
    inflater.inflate(R.layout.mini_controller, this);
    mPauseDrawable = getResources().getDrawable(R.drawable.ic_av_pause_light);
    mPlayDrawable = getResources().getDrawable(R.drawable.ic_av_play_light);
    mStopDrawable = getResources().getDrawable(R.drawable.ic_av_stop_light);
    loadViews();
    setupCallbacks();
  }

  public MiniController(Context context) {
    super(context);
    loadViews();
  }

  /**
   * Sets the listener that should be notified when a relevant event is fired from this component.
   * Clients can register the {@link VideoCastManager} instance to be the default listener so it
   * can control the remote media playback.
   */
  @Override
  public void setOnMiniControllerChangedListener(OnMiniControllerChangedListener listener) {
    if (null != listener) {
      this.mListener = listener;
    }
  }

  public void removeOnMiniControllerChangedListener(OnMiniControllerChangedListener listener) {
    if (null != listener && this.mListener == listener) {
      this.mListener = null;
    }
  }

  @Override
  public void setStreamType(int streamType) {
    this.mStreamType = streamType;
  }

  private void setupCallbacks() {

    mPlayPause.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (null != mListener) {
          setLoadingVisibility(true);
          try {
            mListener.onPlayPauseClicked(v);
          } catch (CastException e) {
            mListener.onFailed(R.string.failed_perform_action, -1);
          } catch (TransientNetworkDisconnectionException e) {
            mListener.onFailed(R.string.failed_no_connection_trans, -1);
          } catch (NoConnectionException e) {
            mListener.onFailed(R.string.failed_no_connection, -1);
          }
        }
      }
    });

    mContainer.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        if (null != mListener) {
          setLoadingVisibility(false);
          try {
            mListener.onTargetActivityInvoked(mIcon.getContext());
          } catch (Exception e) {
            mListener.onFailed(R.string.failed_perform_action, -1);
          }
        }

      }
    });
  }

  private void setIcon(Bitmap bm) {
    mIcon.setImageBitmap(bm);
  }

  @Override
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
          bm = BitmapFactory.decodeResource(getResources(), R.drawable.dummy_album_art);
        }
        mIcon.post(new Runnable() {

          @Override
          public void run() {
            setIcon(bm);
          }
        });

      }
    }).start();
  }

  @Override
  public void setTitle(String title) {
    mTitle.setText(title);
  }

  @Override
  public void setSubTitle(String subTitle) {
    mSubTitle.setText(subTitle);
  }

  @Override
  public void setPlaybackStatus(int state, int idleReason) {
    switch (state) {
      case MediaStatus.PLAYER_STATE_PLAYING:
        mPlayPause.setVisibility(View.VISIBLE);
        mPlayPause.setImageDrawable(getPauseStopButton());
        setLoadingVisibility(false);
        break;
      case MediaStatus.PLAYER_STATE_PAUSED:
        mPlayPause.setVisibility(View.VISIBLE);
        mPlayPause.setImageDrawable(mPlayDrawable);
        setLoadingVisibility(false);
        break;
      case MediaStatus.PLAYER_STATE_IDLE:
        switch (mStreamType) {
          case MediaInfo.STREAM_TYPE_BUFFERED:
            mPlayPause.setVisibility(View.INVISIBLE);
            setLoadingVisibility(false);
            break;
          case MediaInfo.STREAM_TYPE_LIVE:
            if (idleReason == MediaStatus.IDLE_REASON_CANCELED) {
              mPlayPause.setVisibility(View.VISIBLE);
              mPlayPause.setImageDrawable(mPlayDrawable);
              setLoadingVisibility(false);
            } else {
              mPlayPause.setVisibility(View.INVISIBLE);
              setLoadingVisibility(false);
            }
            break;
        }
        break;
      case MediaStatus.PLAYER_STATE_BUFFERING:
        mPlayPause.setVisibility(View.INVISIBLE);
        setLoadingVisibility(true);
        break;
      default:
        mPlayPause.setVisibility(View.INVISIBLE);
        setLoadingVisibility(false);
        break;
    }
  }

  @Override
  public boolean isVisible() {
    return isShown();
  }

  private void loadViews() {
    mIcon = (ImageView) findViewById(R.id.iconView);
    mTitle = (TextView) findViewById(R.id.titleView);
    mSubTitle = (TextView) findViewById(R.id.subTitleView);
    mPlayPause = (ImageView) findViewById(R.id.playPauseView);
    mLoading = (ProgressBar) findViewById(R.id.loadingView);
    mContainer = findViewById(R.id.bigContainer);
  }

  private void setLoadingVisibility(boolean show) {
    mLoading.setVisibility(show ? View.VISIBLE : View.GONE);
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

  /**
   * The interface for a listener that will be called when user interacts with the
   * MiniController, like clicking on the play/pause button, etc.
   */
  public interface OnMiniControllerChangedListener extends OnFailedListener {

    /**
     * Notification that user has clicked on the Play/Pause button
     *
     * @param v
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     * @throws CastException
     */
    public void onPlayPauseClicked(View v) throws CastException, TransientNetworkDisconnectionException, NoConnectionException;

    /**
     * Notification that the user has clicked on the album art
     */
    public void onTargetActivityInvoked(Context context) throws TransientNetworkDisconnectionException, NoConnectionException;

  }

}
