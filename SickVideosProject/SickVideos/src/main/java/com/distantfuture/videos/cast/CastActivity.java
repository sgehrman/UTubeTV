package com.distantfuture.videos.cast;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.distantfuture.castcompanionlibrary.lib.cast.VideoCastManager;
import com.distantfuture.castcompanionlibrary.lib.cast.callbacks.VideoCastConsumerImpl;
import com.distantfuture.castcompanionlibrary.lib.utils.MiniController;
import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.MainApplication;
import com.distantfuture.videos.misc.Utils;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

public class CastActivity extends FragmentActivity {
  private static final String TAG = "CastActivity";
  protected MediaInfo mRemoteMediaInformation;
  private VideoCastManager mCastManager;
  private PlaybackState mPlaybackState = PlaybackState.PAUSED;
  private MiniController mMini;
  private VideoCastConsumerImpl mCastConsumer;
  private MediaInfo mSelectedMedia;


  private static String TAG_MEDIA = "videos";
  private static String THUMB_PREFIX_URL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/";
  private static String TAG_CATEGORIES = "categories";
  private static String TAG_NAME = "name";
  private static String TAG_STUDIO = "studio";
  private static String TAG_SOURCES = "sources";
  private static String TAG_SUBTITLE = "subtitle";
  private static String TAG_THUMB = "image-480x270"; // "thumb";
  private static String TAG_IMG_780_1200 = "image-780x1200";
  private static String TAG_TITLE = "title";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cast);

    mCastManager = MainApplication.getCastManager(this);

    getActionBar().setDisplayHomeAsUpEnabled(false);
    getActionBar().setDisplayUseLogoEnabled(false);
    getActionBar().setDisplayShowHomeEnabled(false);
    getActionBar().setDisplayShowTitleEnabled(false);

    setupMiniController();
    setupCastListener();

    MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

    movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, "Animal Fats");
    movieMetadata.putString(MediaMetadata.KEY_TITLE, "Stuff that Sucks");
    movieMetadata.putString(MediaMetadata.KEY_STUDIO, "Google");
    movieMetadata.addImage(new WebImage(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images_480x270/ForBiggerEscapes.jpg")));
    movieMetadata.addImage(new WebImage(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images_780x1200/Escape-780x1200.jpg")));

    mSelectedMedia = new MediaInfo.Builder("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4")
        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
        .setContentType("video/mp4")
        .setMetadata(movieMetadata)
        .build();

    View button = findViewById(R.id.play_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        togglePlayback();
      }
    });
  }

  private void setupCastListener() {
    mCastConsumer = new VideoCastConsumerImpl() {
      @Override
      public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
        Log.d(TAG, "onApplicationLaunched() is reached");
        if (null != mSelectedMedia) {

          if (mPlaybackState == PlaybackState.PLAYING) {
            try {
              loadRemoteMedia(0, true);
              finish();
            } catch (Exception e) {
              Debug.log(e.getMessage());
            }
          } else {
          }
        }
      }

      @Override
      public void onApplicationDisconnected(int errorCode) {
        Log.d(TAG, "onApplicationDisconnected() is reached with errorCode: " + errorCode);
      }

      @Override
      public void onDisconnected() {
        Log.d(TAG, "onDisconnected() is reached");
        mPlaybackState = PlaybackState.PAUSED;
      }

      @Override
      public void onRemoteMediaPlayerMetadataUpdated() {
        try {
          mRemoteMediaInformation = mCastManager.getRemoteMediaInformation();
        } catch (Exception e) {
          // silent
        }
      }

      @Override
      public void onFailed(int resourceId, int statusCode) {

      }

      @Override
      public void onConnectionSuspended(int cause) {
        Utils.toast(CastActivity.this, "Connection Lost");
      }

      @Override
      public void onConnectivityRecovered() {
        Utils.toast(CastActivity.this, "Connection Recovered");
      }
    };
  }

  private void setupMiniController() {
    mMini = (MiniController) findViewById(R.id.miniController1);
    mCastManager.addMiniController(mMini);
  }

  private void togglePlayback() {
    switch (mPlaybackState) {
      case PAUSED:
        try {
          mCastManager.checkConnectivity();
          loadRemoteMedia(0, true);
          finish();
        } catch (Exception e) {
          Debug.log("Exception: " + e.getMessage());
          return;
        }
        break;
      case PLAYING:
        mPlaybackState = PlaybackState.PAUSED;
        break;
      case IDLE:
        mPlaybackState = PlaybackState.PLAYING;
        break;
      default:
        break;
    }
  }

  private void loadRemoteMedia(int position, boolean autoPlay) {
    mCastManager.startCastControllerActivity(this, mSelectedMedia, position, autoPlay);
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "onPause() was called");
    mCastManager.removeVideoCastConsumer(mCastConsumer);
    mMini.removeOnMiniControllerChangedListener(mCastManager);
    mCastManager.decrementUiCounter();
  }

  @Override
  protected void onDestroy() {
    if (null != mCastManager) {
      mMini.removeOnMiniControllerChangedListener(mCastManager);
      mCastConsumer = null;
    }
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    mCastManager = MainApplication.getCastManager(this);
    mCastManager.addVideoCastConsumer(mCastConsumer);
    mCastManager.incrementUiCounter();
    super.onResume();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (!mCastManager.isConnected()) {
      return super.onKeyDown(keyCode, event);
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      onVolumeChange(MainApplication.VOLUME_INCREMENT);
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      onVolumeChange(-MainApplication.VOLUME_INCREMENT);
    } else {
      return super.onKeyDown(keyCode, event);
    }
    return true;
  }

  private void onVolumeChange(double volumeIncrement) {
    if (mCastManager == null) {
      try {
        mCastManager.incrementVolume(volumeIncrement);
      } catch (Exception e) {
        Log.e(TAG, "onVolumeChange() Failed to change volume", e);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.cast_menu, menu);
    mCastManager.addMediaRouterButton(menu, R.id.action_cast, this, false);
    return true;
  }

  public static enum PlaybackState {
    PLAYING, PAUSED, BUFFERING, IDLE
  }

}
