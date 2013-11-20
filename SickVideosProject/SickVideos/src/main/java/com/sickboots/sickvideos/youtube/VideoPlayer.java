package com.sickboots.sickvideos.youtube;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sickboots.iconicdroid.IconicFontDrawable;
import com.sickboots.iconicdroid.icon.EntypoIcon;
import com.sickboots.iconicdroid.icon.FontAwesomeIcon;
import com.sickboots.iconicdroid.icon.Icon;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.VideoPlayerFragment;
import com.sickboots.sickvideos.misc.Util;

public class VideoPlayer {
  private View videoBox;
  private Context mContext;
  private VideoPlayerFragment mVideoFragment;
  private VideoPlayerStateListener mListener;
  private ImageButton mMuteButton;
  private final int mExtraSpaceOnTopOfPlayerView = 75;
  private TextView mSeekFlashTextView;
  private TextView mTimeRemainingTextView;

  public enum IconID {SOUND, STEP_FORWARD, STEP_BACK, FULLSCREEN, CLOSE}

  ;

  abstract public interface VideoPlayerStateListener {
    abstract public void stateChanged();
  }

  public VideoPlayer(Activity activity, int fragmentContainerResID, VideoPlayerStateListener l) {
    super();

    // will already exist if restoring Activity
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
    videoBox = activity.findViewById(R.id.video_player_box);

    ImageButton b;

    // close button
    b = (ImageButton) (ImageButton) videoBox.findViewById(R.id.close_button);
    b.setBackground(icon(IconID.CLOSE));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        close();
      }
    });

    // Mute button
    mMuteButton = (ImageButton) videoBox.findViewById(R.id.mute_button);
    mMuteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleMute();
      }

      ;
    });
    updateMuteButton();

    // Skip back button
    b = (ImageButton) videoBox.findViewById(R.id.skip_back_button);
    b.setBackground(icon(IconID.STEP_BACK));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        skip(-10);
      }

      ;
    });

    // Skip ahead button
    b = (ImageButton) videoBox.findViewById(R.id.skip_ahead_button);
    b.setBackground(icon(IconID.STEP_FORWARD));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        skip(10);
      }

      ;
    });

    mTimeRemainingTextView = (TextView) videoBox.findViewById(R.id.time_remaining);
    mTimeRemainingTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Util.log("text clicked, fix later");
      }

      ;
    });

    mSeekFlashTextView = (TextView) videoBox.findViewById(R.id.seek_flash);

    // we let the video fragment update us in it's own timer
    mVideoFragment.setTimeRemainingListener(new VideoPlayerFragment.TimeRemainingListener() {

      // call this on the main thread
      @Override
      public void setTimeRemainingText(String timeRemaining) {
        mTimeRemainingTextView.setText(timeRemaining);
      }

      @Override
      public void setSeekFlashText(String seekFlash) {
        mSeekFlashTextView.setText(seekFlash);
      }

    });
  }

  public void open(String videoId, String title) {
    mVideoFragment.setVideo(videoId, title);

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
      videoBox.animate().translationY(-Util.dpToPx(mExtraSpaceOnTopOfPlayerView, mContext)).setInterpolator(new OvershootInterpolator()).setDuration(300).withEndAction(new Runnable() {
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
      mVideoFragment.pause();

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

  public String title() {
    return mVideoFragment.getTitle();
  }

  public boolean visible() {
    return (videoBox.getVisibility() == View.VISIBLE);
  }

  public void toggleMute() {
    mVideoFragment.mute(!mVideoFragment.isMute());

    updateMuteButton();
  }

  public void toggleFullscreen() {
    mVideoFragment.setFullscreen(true);
  }

  public void skip(int seconds) {
    mVideoFragment.seekRelativeSeconds(seconds);
  }

  private Drawable icon(IconID iconID) {
    return icon(iconID, Color.WHITE);
  }

  private void updateMuteButton() {
    if (mVideoFragment.isMute())
      mMuteButton.setBackground(icon(IconID.SOUND, Color.RED));
    else
      mMuteButton.setBackground(icon(IconID.SOUND));
  }

  private Drawable icon(IconID iconID, int iconColor) {
    Icon icon = null;

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

    IconicFontDrawable pressed = new IconicFontDrawable(mContext);
    pressed.setIcon(icon);
    pressed.setIconColor(mContext.getResources().getColor(R.color.content_background));
    pressed.setContour(Color.GRAY, 1);
    pressed.setIconPadding(8);

    IconicFontDrawable normal = new IconicFontDrawable(mContext);
    normal.setIcon(icon);
    normal.setIconColor(iconColor);
    normal.setContour(Color.GRAY, 1);
    normal.setIconPadding(8);

    StateListDrawable states = new StateListDrawable();
    states.addState(new int[]{android.R.attr.state_pressed}, pressed);
    states.addState(new int[]{}, normal);

    return states;
  }


}
