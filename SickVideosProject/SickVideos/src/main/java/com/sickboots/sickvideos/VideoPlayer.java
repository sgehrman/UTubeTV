package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;

public class VideoPlayer {
  private View videoBox;
  Context mContext;
  VideoPlayerFragment mVideoFragment;
  VideoPlayerStateListener mListener;

  abstract public interface VideoPlayerStateListener {
    abstract public void stateChanged();
  }

  public VideoPlayer(Activity activity, View view, int fragmentContainerResID) {
    super();

    // will already exist if restoring fragments
    mVideoFragment = (VideoPlayerFragment) activity.getFragmentManager().findFragmentById(fragmentContainerResID);
    if (mVideoFragment == null) {
      // had to add this manually rather than setting the class in xml to avoid duplicate id errors
      mVideoFragment = new VideoPlayerFragment();
      FragmentManager fm = activity.getFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      ft.replace(fragmentContainerResID, mVideoFragment);
      ft.commit();
    }

    mContext = activity.getApplicationContext();
    videoBox = view;
    videoBox.setVisibility(View.INVISIBLE);
  }

  public void setStateListener(VideoPlayerStateListener l) {
    mListener = l;
  }

  public void open(String videoId, String title) {
    videoFragment().setVideo(videoId, title);

    if (!visible()) {
      if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Initially translate off the screen so that it can be animated in from below.
        videoBox.setTranslationY(-videoBox.getHeight());
      }
      videoBox.setVisibility(View.VISIBLE);
    }

    // If the fragment is off the screen, we animate it in.
    if (videoBox.getTranslationY() < 0) {
      Util.vibrate(mContext);
      videoBox.animate().translationY(-Util.dpToPx(45, mContext)).setInterpolator(new OvershootInterpolator()).setDuration(300).withEndAction( new Runnable() {
        @Override
        public void run() {
          if (mListener != null) {
            mListener.stateChanged();
          }
        }
      });
    }
  }

  public void close() {
    if (visible()) {
      // pause immediately on click for better UX
      videoFragment().pause();

      Util.vibrate(mContext);
      videoBox.animate()
          .translationYBy(-videoBox.getHeight())
          .setInterpolator(new AnticipateInterpolator())
          .setDuration(300)
          .withEndAction(new Runnable() {
            @Override
            public void run() {
              videoBox.setVisibility(View.INVISIBLE);

              if (mListener != null) {
                mListener.stateChanged();
              }
            }
          });
    }
  }


  private VideoPlayerFragment videoFragment() {
    return mVideoFragment;
  }


public String title() {
  return videoFragment().getTitle();

}
  public boolean visible() {
    return (videoBox.getVisibility() == View.VISIBLE);
  }

    public void toggleMute() {
      videoFragment().mute(!videoFragment().isMute());
    }

    public void toggleFullscreen() {
      videoFragment().setFullscreen(true);
    }

    public void skip(int seconds) {
      videoFragment().seekRelativeSeconds(seconds);
    }

}
