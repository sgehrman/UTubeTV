/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.distantfuture.castcompanionlibrary.lib.cast.player;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.distantfuture.castcompanionlibrary.lib.R;
import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.CastException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.NoConnectionException;
import com.distantfuture.castcompanionlibrary.lib.cast.exceptions.TransientNetworkDisconnectionException;
import com.distantfuture.castcompanionlibrary.lib.utils.CastUtils;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;

/**
 * This class provides an {@link Activity} that clients can easily add to their applications to
 * provide an out-of-the-box remote player when a video is casting to a cast device.
 * {@link VideoCastManager} can manage the lifecycle and presentation of this activity.
 * <p/>
 * This activity provides a number of controllers for managing the playback of the remote content:
 * play/pause (or play/stop when a live stream is used) and seekbar (for non-live streams).
 * <p/>
 * Clients who need to perform a pre-authorization process for playback can register a
 * {@link IMediaAuthListener} by calling
 * {@link VideoCastManager#startCastControllerActivity(android.content.Context, IMediaAuthService)}.
 * In that case, this activity manages starting the {@link IMediaAuthService} and will register a
 * listener to handle the result.
 */
public class VideoCastControllerActivity extends FragmentActivity implements IVideoCastController {

  private static final String TAG = CastUtils.makeLogTag(VideoCastControllerActivity.class);
  private VideoCastManager mCastManager;
  private View mPageView;
  private ImageView mPlayPause;
  private TextView mStart;
  private TextView mEnd;
  private SeekBar mSeekbar;
  private TextView mLine1;
  private TextView mLine2;
  private ProgressBar mLoading;
  private float mVolumeIncrement;
  private View mControllers;
  private Drawable mPauseDrawable;
  private Drawable mPlayDrawable;
  private Drawable mStopDrawable;
  private OnVideoCastControllerListener mListener;
  private int mStreamType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cast_activity);
    loadAndSetupViews();
    mVolumeIncrement = CastUtils.getFloatFromPreference(this, VideoCastManager.PREFS_KEY_VOLUME_INCREMENT);
    try {
      mCastManager = VideoCastManager.getInstance(this);
    } catch (CastException e) {
      // logged already
    }

    setupActionBar();
    Bundle extras = getIntent().getExtras();
    if (null == extras) {
      finish();
      return;
    }

    FragmentManager fm = getFragmentManager();
    VideoCastControllerFragment mediaAuthFragment = (VideoCastControllerFragment) fm.findFragmentByTag("task");

    // if fragment is null, it means this is the first time, so create it
    if (mediaAuthFragment == null) {
      mediaAuthFragment = VideoCastControllerFragment.newInstance(extras);
      fm.beginTransaction().add(mediaAuthFragment, "task").commit();
      mListener = mediaAuthFragment;
      setOnVideoCastControllerChangedListener(mListener);
    } else {
      mListener = mediaAuthFragment;
      mListener.onConfigurationChanged();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.cast_player_menu, menu);
    mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item, this, false);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    }
    return true;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (mVolumeIncrement == Float.MIN_VALUE) {
      return false;
    }
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      onVolumeChange(mVolumeIncrement);
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      onVolumeChange(-(double) mVolumeIncrement);
    } else {
      return super.onKeyDown(keyCode, event);
    }
    return true;
  }

  private void onVolumeChange(double volumeIncrement) {
    if (mCastManager == null) {
      return;
    }
    try {
      mCastManager.incrementVolume(volumeIncrement);
    } catch (Exception e) {
      CastUtils.LOGE(TAG, "onVolumeChange() Failed to change volume", e);
      CastUtils.showErrorDialog(VideoCastControllerActivity.this, R.string.failed_setting_volume);
    }
  }

  @Override
  protected void onResume() {
    CastUtils.LOGD(TAG, "onResume() was called");
    try {
      mCastManager = VideoCastManager.getInstance(VideoCastControllerActivity.this);
    } catch (CastException e) {
      // logged already
    }

    super.onResume();
  }

  private void loadAndSetupViews() {
    mPauseDrawable = getResources().getDrawable(R.drawable.ic_av_pause_dark);
    mPlayDrawable = getResources().getDrawable(R.drawable.ic_av_play_dark);
    mStopDrawable = getResources().getDrawable(R.drawable.ic_av_stop_dark);
    mPageView = findViewById(R.id.pageView);
    mPlayPause = (ImageView) findViewById(R.id.imageView1);
    mStart = (TextView) findViewById(R.id.startText);
    mEnd = (TextView) findViewById(R.id.endText);
    mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
    mLine1 = (TextView) findViewById(R.id.textView1);
    mLine2 = (TextView) findViewById(R.id.textView2);
    mLoading = (ProgressBar) findViewById(R.id.progressBar1);
    mControllers = findViewById(R.id.controllers);

    mPlayPause.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        try {
          mListener.onPlayPauseClicked(v);
        } catch (TransientNetworkDisconnectionException e) {
          CastUtils.LOGE(TAG, "Failed to toggle playback due to temporary network issue", e);
          CastUtils.showErrorDialog(VideoCastControllerActivity.this, R.string.failed_no_connection_trans);
        } catch (NoConnectionException e) {
          CastUtils.LOGE(TAG, "Failed to toggle playback due to network issues", e);
          CastUtils.showErrorDialog(VideoCastControllerActivity.this, R.string.failed_no_connection);
        } catch (Exception e) {
          CastUtils.LOGE(TAG, "Failed to toggle playback due to other issues", e);
          CastUtils.showErrorDialog(VideoCastControllerActivity.this, R.string.failed_perform_action);
        }
      }
    });

    mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        try {
          if (null != mListener) {
            mListener.onStopTrackingTouch(seekBar);
          }
        } catch (Exception e) {
          CastUtils.LOGE(TAG, "Failed to complete seek", e);
          finish();
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        try {
          if (null != mListener) {
            mListener.onStartTrackingTouch(seekBar);
          }
        } catch (Exception e) {
          CastUtils.LOGE(TAG, "Failed to start seek", e);
          finish();
        }
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mStart.setText(CastUtils.formatMillis(progress));
        try {
          if (null != mListener) {
            mListener.onProgressChanged(seekBar, progress, fromUser);
          }
        } catch (Exception e) {
          CastUtils.LOGE(TAG, "Failed to set teh progress result", e);
        }
      }
    });
  }

  private void setupActionBar() {
    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setDisplayUseLogoEnabled(false);
    getActionBar().setDisplayShowHomeEnabled(false);
    getActionBar().setDisplayShowTitleEnabled(true);
    getActionBar().setTitle(" "); // without a title, the "<" won't show
    getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg_gradient_light));
  }

  @Override
  public void showLoading(boolean visible) {
    mLoading.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
  }

  // -------------- IVideoCastController implementation ---------------- //
  @Override
  public void adjustControllersForLiveStream(boolean isLive) {
    int visibility = isLive ? View.INVISIBLE : View.VISIBLE;
    mEnd.setVisibility(visibility);
    mSeekbar.setVisibility(visibility);
  }

  @Override
  public void setPlaybackStatus(int state) {
    switch (state) {
      case MediaStatus.PLAYER_STATE_PLAYING:
        mLoading.setVisibility(View.INVISIBLE);
        mPlayPause.setVisibility(View.VISIBLE);

        if (mStreamType == MediaInfo.STREAM_TYPE_LIVE) {
          mPlayPause.setImageDrawable(mStopDrawable);
        } else {
          mPlayPause.setImageDrawable(mPauseDrawable);
        }

        mLine2.setText(getString(R.string.casting_to_device, mCastManager.getDeviceName()));
        mControllers.setVisibility(View.VISIBLE);
        break;
      case MediaStatus.PLAYER_STATE_PAUSED:
        mControllers.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
        mPlayPause.setVisibility(View.VISIBLE);
        mPlayPause.setImageDrawable(mPlayDrawable);
        mLine2.setText(getString(R.string.casting_to_device, mCastManager.getDeviceName()));
        break;
      case MediaStatus.PLAYER_STATE_IDLE:
        mLoading.setVisibility(View.INVISIBLE);
        mPlayPause.setImageDrawable(mPlayDrawable);
        mPlayPause.setVisibility(View.VISIBLE);
        mLine2.setText(getString(R.string.casting_to_device, mCastManager.getDeviceName()));
        break;
      case MediaStatus.PLAYER_STATE_BUFFERING:
        mPlayPause.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
        mLine2.setText(getString(R.string.loading));
        break;
      default:
        break;
    }
  }

  @Override
  public void updateSeekbar(int position, int duration) {
    mSeekbar.setProgress(position);
    mSeekbar.setMax(duration);
    mStart.setText(CastUtils.formatMillis(position));
    mEnd.setText(CastUtils.formatMillis(duration));
  }

  @Override
  public void setImage(Bitmap bitmap) {
    if (null != bitmap) {
      mPageView.setBackground(new BitmapDrawable(getResources(), bitmap));
    }
  }

  @Override
  public void setLine1(String text) {
    mLine1.setText(text);
  }

  @Override
  public void setLine2(String text) {
    mLine2.setText(text);
  }

  @Override
  public void setOnVideoCastControllerChangedListener(OnVideoCastControllerListener listener) {
    if (null != listener) {
      this.mListener = listener;
    }
  }

  @Override
  public void setStreamType(int streamType) {
    this.mStreamType = streamType;
  }

  @Override
  public void updateControllersStatus(boolean enabled) {
    mControllers.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    if (enabled) {
      adjustControllersForLiveStream(mStreamType == MediaInfo.STREAM_TYPE_LIVE);
    }
  }

  @Override
  public void closeActivity() {
    finish();
  }

}
