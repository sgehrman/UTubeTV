package com.sickboots.sickvideos.youtube;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.misc.Util;

public class VideoPlayer {
  private View videoBox;
  private Context mContext;
  private VideoPlayerFragment mVideoFragment;
  private VideoPlayerStateListener mListener;
  private View mMuteButton;
  private final int mExtraSpaceOnTopOfPlayerView = 75;
  private TextView mSeekFlashTextView;
  private TextView mTimeRemainingTextView;
  private final int mAnimationDuration = 300;

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

    View b;

    // close button
    b = videoBox.findViewById(R.id.close_button);
    b.setBackground(ToolbarIcons.icon(mContext, ToolbarIcons.IconID.CLOSE, Color.WHITE));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        close(true);
      }
    });

    // Mute button
    mMuteButton = videoBox.findViewById(R.id.mute_button);
    mMuteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleMute();
      }

      ;
    });
    updateMuteButton();

    // Skip back button
    b = videoBox.findViewById(R.id.skip_back_button);
    b.setBackground(ToolbarIcons.icon(mContext, ToolbarIcons.IconID.STEP_BACK, Color.WHITE));
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        skip(-10);
      }

      ;
    });

    // Skip ahead button
    b = videoBox.findViewById(R.id.skip_ahead_button);
    b.setBackground(ToolbarIcons.icon(mContext, ToolbarIcons.IconID.STEP_FORWARD, Color.WHITE));
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
        // put it below toolbar
        View tb = (View) videoBox.findViewById(R.id.video_toolbar_view);

        showSeekPopupWindow(tb);
      }

      ;
    });

    mSeekFlashTextView = (TextView) videoBox.findViewById(R.id.seek_flash);

    // we let the video fragment update us in it's own timer
    mVideoFragment.setTimeRemainingListener(new VideoPlayerFragment.TimeRemainingListener() {

      // call this on the main thread
      @Override
      public void setTimeRemainingText(final String timeRemaining) {
        mTimeRemainingTextView.setText(timeRemaining);
      }

      @Override
      public void setSeekFlashText(final String seekFlash) {
        mSeekFlashTextView.setText(seekFlash);

        int duration = 300;

        // fade old one out
        mTimeRemainingTextView.animate()
            .setDuration(duration)
            .alpha(0.0f);

        // start off off the screen, make visible
        mSeekFlashTextView.setTranslationY(-60.0f);
        mSeekFlashTextView.setVisibility(View.VISIBLE);

        // run animation, new time slides in from top, old time slides off
        mSeekFlashTextView.animate()
            .setDuration(duration)
            .translationY(0)
            .setInterpolator(new BounceInterpolator())
            .withEndAction(new Runnable() {
              @Override
              public void run() {
                mSeekFlashTextView.setVisibility(View.INVISIBLE);

                mTimeRemainingTextView.setText(seekFlash);
                mTimeRemainingTextView.setAlpha(1.0f);
              }
            });
      }

    });
  }

  // video player fragment restores itself, so just show it and let it do its thing
  public void restore() {
    videoBox.setTranslationY(-Util.dpToPx(mExtraSpaceOnTopOfPlayerView, mContext));
    videoBox.setVisibility(View.VISIBLE);
  }

  public void open(String videoId, String title, boolean animate) {
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
      videoBox.animate()
          .translationY(-Util.dpToPx(mExtraSpaceOnTopOfPlayerView, mContext))
          .setInterpolator(new OvershootInterpolator())
          .setDuration(animate ? mAnimationDuration : 0)
          .withEndAction(new Runnable() {
            @Override
            public void run() {
              if (mListener != null) {
                mListener.stateChanged();
              }
            }
          });
    }
  }

  public void close(boolean animate) {
    if (visible()) {
      // pause immediately on click for better UX
      mVideoFragment.pause();

      Util.vibrate(mContext);
      videoBox.animate()
          .translationYBy(-videoBox.getHeight())
          .setInterpolator(new AnticipateInterpolator())
          .setDuration(animate ? mAnimationDuration : 0)
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

  private void updateMuteButton() {
    if (mVideoFragment.isMute())
      mMuteButton.setBackground(ToolbarIcons.icon(mContext, ToolbarIcons.IconID.SOUND, Color.RED));
    else
      mMuteButton.setBackground(ToolbarIcons.icon(mContext, ToolbarIcons.IconID.SOUND, Color.WHITE));
  }

  private void showSeekPopupWindow(View anchorView) {
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View popupContentsView = inflater.inflate(R.layout.video_seek_popup, null);
    final PopupWindow pw = new PopupWindow(popupContentsView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);  // if false, clicks to dismiss window also get passed to views below (should be true)

    // hack_alert: must set some kind of background so that clicking outside the view will dismiss the popup (known bug in Android)
    pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    pw.setOutsideTouchable(true);
    pw.setAnimationStyle(-1);
    pw.showAsDropDown(anchorView);

    float time = mVideoFragment.getCurrentTimeMillis();
    final float duration = mVideoFragment.getDurationMillis();
    float currentPercent = time / duration;
    int startValue = (int) (currentPercent * 100);

    SeekBar sb = (SeekBar) popupContentsView.findViewById(R.id.video_seek_bar);
    sb.setMax(100);   // using max like a percent
    sb.setProgress(startValue);

    sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float progressPercent = progress / 100.0f;
        int seekTo = (int) (progressPercent * duration);

        mTimeRemainingTextView.setText(Util.millisecondsToDuration(seekTo));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // seek when released
        float progressPercent = seekBar.getProgress() / 100.0f;
        int seekTo = (int) (progressPercent * duration);

        mVideoFragment.seekToMillis(seekTo);

        pw.dismiss();
      }
    });

  }
}
