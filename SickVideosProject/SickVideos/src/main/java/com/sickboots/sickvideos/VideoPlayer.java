package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;

import com.sickboots.iconicdroid.IconicFontDrawable;
import com.sickboots.iconicdroid.icon.EntypoIcon;
import com.sickboots.iconicdroid.icon.FontAwesomeIcon;
import com.sickboots.iconicdroid.icon.Icon;

public class VideoPlayer {
  private View videoBox;
  Context mContext;
  VideoPlayerFragment mVideoFragment;
  VideoPlayerStateListener mListener;
  ImageButton mMuteButton;

  public enum IconID {SOUND, STEP_FORWARD, STEP_BACK, FULLSCREEN, CLOSE};

  abstract public interface VideoPlayerStateListener {
    abstract public void stateChanged();
  }

  public VideoPlayer(Activity activity, View rootView, int fragmentContainerResID, VideoPlayerStateListener l) {
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

    mListener = l;

    mContext = activity.getApplicationContext();
    videoBox = rootView.findViewById(R.id.slide_in_player_box);
    videoBox.setVisibility(View.INVISIBLE);

    ImageButton b;

    // close button
    b = (ImageButton) (ImageButton) rootView.findViewById(R.id.close_button);
    b.setBackground(icon(IconID.CLOSE));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        close();
      }
    });

    // Mute button
    mMuteButton = (ImageButton) rootView.findViewById(R.id.mute_button);
    mMuteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleMute();
      }

      ;
    });
    updateMuteButton();

    // Full screen button
    b = (ImageButton) rootView.findViewById(R.id.full_screen_button);
    b.setBackground(icon(IconID.FULLSCREEN));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleFullscreen();
      }

      ;
    });

    // Skip back button
    b = (ImageButton) rootView.findViewById(R.id.skip_back_button);
    b.setBackground(icon(IconID.STEP_BACK));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        skip(-10);
      }

      ;
    });

    // Skip ahead button
    b = (ImageButton) rootView.findViewById(R.id.skip_ahead_button);
    b.setBackground(icon(IconID.STEP_FORWARD));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        skip(10);
      }

      ;
    });
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
      videoBox.animate().translationY(-Util.dpToPx(45, mContext)).setInterpolator(new OvershootInterpolator()).setDuration(300).withEndAction(new Runnable() {
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

    updateMuteButton();
  }

  public void toggleFullscreen() {
    videoFragment().setFullscreen(true);
  }

  public void skip(int seconds) {
    videoFragment().seekRelativeSeconds(seconds);
  }

  private Drawable icon(IconID iconID) {
    return icon(iconID, Color.WHITE);
  }

  private void updateMuteButton() {
    if (videoFragment().isMute())
      mMuteButton.setBackground(icon(IconID.SOUND, Color.RED));
    else
      mMuteButton.setBackground(icon(IconID.SOUND));
  }

  private Drawable icon(IconID iconID, int iconColor) {
    Icon icon=null;

    switch (iconID) {
      case SOUND:
        icon = FontAwesomeIcon.VOLUME_UP;
        break;
      case STEP_BACK:
        icon = FontAwesomeIcon.STEP_BACKWARD;
        break;
      case STEP_FORWARD:
        icon = FontAwesomeIcon.STEP_FORWARD;
        break;
      case CLOSE:
        icon = FontAwesomeIcon.REMOVE_SIGN;
        break;
      case FULLSCREEN:
        icon = FontAwesomeIcon.FULLSCREEN;
        break;
      default:
        icon = EntypoIcon.NEW;  // error
        break;
    }

    IconicFontDrawable result = new IconicFontDrawable(mContext);
    result.setIcon(icon);
    result.setIconColor(iconColor);
    result.setContour(Color.GRAY, 1);
    result.setIconPadding(8);

    return result;
  }


}
