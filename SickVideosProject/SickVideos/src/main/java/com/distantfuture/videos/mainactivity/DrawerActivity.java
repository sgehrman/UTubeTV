package com.distantfuture.videos.mainactivity;

import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.distantfuture.iconicdroid.IconicActivity;
import com.distantfuture.videos.R;
import com.distantfuture.videos.activities.ChannelLookupActivity;
import com.distantfuture.videos.activities.SettingsActivity;
import com.distantfuture.videos.activities.ViewServerActivity;
import com.distantfuture.videos.cast.CastActivity;
import com.distantfuture.videos.content.Content;
import com.distantfuture.videos.donate.DonateActivity;
import com.distantfuture.videos.introactivity.IntroActivity;
import com.distantfuture.videos.misc.ActionBarSpinnerAdapter;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.ColorPickerFragment;
import com.distantfuture.videos.misc.Events;
import com.distantfuture.videos.misc.MainApplication;
import com.distantfuture.videos.misc.Utils;
import com.distantfuture.videos.youtube.VideoPlayer;
import com.distantfuture.videos.youtube.YouTubeAPI;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.inscription.WhatsNewDialog;

import org.codechimp.apprater.AppRater;

import de.greenrobot.event.EventBus;

public class DrawerActivity extends ViewServerActivity implements DrawerActivitySupport {
  VideoPlayer mPlayer;
  private int mCurrentSection = -1;
  private DrawerManager mDrawerMgr;
  private Toast backButtonToast;
  private long lastBackPressTime = 0;
  private Content mContent;
  private ActionBarSpinnerAdapter mActionBarSpinnerAdapter;
  private boolean mSpinnerSucksBalls;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // we set the activity to NoActionBar in the manifest to avoid the title flickering in the actionbar
    setTheme(R.style.DrawerActivityTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_drawer);

    mContent = Content.instance(this);

    if (mContent.needsChannelSwitcher()) {
      mActionBarSpinnerAdapter = new ActionBarSpinnerAdapter(this, mContent);
      ActionBar.OnNavigationListener listener = new ActionBar.OnNavigationListener() {
        @Override
        public boolean onNavigationItemSelected(int position, long itemId) {

          // be aware that this call back gets called when the spinner contents are built
          // we need to ignore that one, so not going to do anything if channel not changing
          if (!mSpinnerSucksBalls) {
            mSpinnerSucksBalls = true;

            // ## taking advantage of this feature/bug to set the real value of the actionbar spinner
            // if we don't do this, the spinner defaults to value 0, so selecting the first item
            // in the list will not work since it doesn't respond when selecting the same index as the current value
            getActionBar().setSelectedNavigationItem(mContent.currentChannelIndex());
          } else {
            if (mContent.changeChannel(position))
              updateSectionForChannel();
          }
          return true;
        }
      };

      getActionBar().setDisplayShowTitleEnabled(false);
      getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
      getActionBar().setListNavigationCallbacks(mActionBarSpinnerAdapter, listener);
    }

    setupDrawer();

    // enable ActionBar app icon to behave as action to toggle nav drawer
    getActionBar().setDisplayHomeAsUpEnabled(true);

    selectSection(mContent.savedSectionIndex(), false);

    AppRater.app_launched(this);

    // general app tweaks
    //    Debug.activateStrictMode();
    Utils.ignoreObsoleteCapacitiveMenuButton(this);

    // show player if activity was destroyed and recreated
    if (savedInstanceState != null) {
      VideoPlayer.PlayerParams params = (VideoPlayer.PlayerParams) savedInstanceState.getParcelable("player_params");

      if (params != null)
        playVideo(params);
    }

    WhatsNewDialog.showWhatsNew(this, false);

    // only show intro for multi channels
    if (mContent.needsChannelSwitcher())
      IntroActivity.showIntroDelayed(this, false);
  }

  @Override
  public void onStop() {
    super.onStop();

    // for AppUtils.THEME_CHANGED
    EventBus.getDefault().unregister(this);
  }

  @Override
  public void onStart() {
    super.onStart();

    // for AppUtils.THEME_CHANGED
    EventBus.getDefault().register(this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (mPlayer != null) {
      if (mPlayer.visible()) {
        VideoPlayer.PlayerParams params = mPlayer.playerParams();

        if (params != null) {
          outState.putParcelable("player_params", params);
        }
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);

    MainApplication.getCastManager(this).addMediaRouterButton(menu, R.id.action_cast, this, true);

    return super.onCreateOptionsMenu(menu);
  }

  private boolean closePlayerIfOpen() {
    if (mPlayer != null && mPlayer.visible()) {
      mPlayer.close();
      return true;
    }

    return false;
  }

  public void onEvent(Events.ThemeChanged event) {
    // animate doesn't work, puts new activity in the background.  use recreate instead
    boolean animate = false;
    if (animate) {
      ActivityOptions opts = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out);

      startActivity(getIntent(), opts.toBundle());

      finish();
    } else {
      // not sure how to to get recreate to animate, so we use the above code when animating which is like a recreate
      recreate();
    }
  }

  // FragmentActivity uses some supportFragment garbage
  // backbutton will close the activity rather than popBack a fragment
  public void superOnBackPressedHack() {
    if (!getFragmentManager().popBackStackImmediate()) {
      finish();
    }
  }

  @Override
  public void onBackPressed() {
    // hides the video player if visible
    if (!closePlayerIfOpen()) {
      if (getFragmentManager().getBackStackEntryCount() == 0) {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
          backButtonToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
          backButtonToast.show();
          this.lastBackPressTime = System.currentTimeMillis();
        } else {
          if (backButtonToast != null) {
            backButtonToast.cancel();
          }
          // this works around FragmentActivity incorrect behavior
          superOnBackPressedHack();
        }
      } else {
        // this works around FragmentActivity incorrect behavior
        superOnBackPressedHack();
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      // called when playing a movie, could fail and this dialog shows the user how to fix it
      case YouTubeAPI.REQ_PLAYER_CODE:
        if (resultCode != RESULT_OK) {
          YouTubeInitializationResult errorReason = YouTubeStandalonePlayer.getReturnedInitializationResult(data);
          if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, 0).show();
          } else {
            String errorMessage = String.format("PLAYER ERROR!! - %s", errorReason.toString());
            Utils.toast(this, errorMessage);
          }
        }

        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.action_show_hidden);

    if (item != null) {
      boolean showHidden = AppUtils.instance(this).showHiddenItems();

      item.setTitle((showHidden ? R.string.action_hide_hidden : R.string.action_show_hidden));
    }

    boolean showDevTools = AppUtils.instance(this).showDevTools();
    menu.setGroupVisible(R.id.dev_tools_group, showDevTools);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;

    // close player if open
    if (item.getItemId() == android.R.id.home) {
      // close player if back button in action bar hit
      if (closePlayerIfOpen())
        return true;
    }

    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    if (mDrawerMgr.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle action buttons
    switch (item.getItemId()) {
      case R.id.action_settings:
        SettingsActivity.show(DrawerActivity.this);
        return true;

      case R.id.action_show_hidden: {
        boolean toggle = AppUtils.instance(this).showHiddenItems();
        AppUtils.instance(this).setShowHiddenItems(!toggle);
        YouTubeGridFragment fragment = currentYouTubeFragment();

        if (fragment != null)
          fragment.reloadForPrefChange();
        return true;
      }
      case R.id.action_more_apps:
        Utils.openWebPage(this, Utils.getCompanyPlayStoreUri());
        return true;

      case R.id.action_switch_view:
        AppUtils.pickViewStyleDialog(this);
        return true;

      // for development only, hide for release
      case R.id.action_show_icons:
        intent = new Intent();
        intent.setClass(DrawerActivity.this, IconicActivity.class);
        startActivity(intent);
        return true;

      case R.id.rate_my_app:
        AppRater.rateNow(this);
        return true;

      case R.id.action_show_rate_dialog:
        AppRater.showRateDialog(this);
        return true;

      case R.id.action_show_cast:
        intent = new Intent();
        intent.setClass(DrawerActivity.this, CastActivity.class);
        startActivity(intent);
        return true;

      case R.id.action_donate:
        DonateActivity.show(this);
        return true;

      case R.id.action_channel_lookup:
        intent = new Intent();
        intent.setClass(DrawerActivity.this, ChannelLookupActivity.class);
        startActivity(intent);
        return true;

      case R.id.action_whats_new:
        WhatsNewDialog.showWhatsNew(this, true);
        return true;

      case R.id.action_color_picker: {
        Fragment fragment = new ColorPickerFragment();
        Utils.showFragment(this, fragment, R.id.fragment_holder, 0, true);
        return true;
      }
    }

    return super.onOptionsItemSelected(item);
  }

  private void updateSectionForChannel() {
    mCurrentSection = -1; // force it to reload fragment if same position
    selectSection(mContent.savedSectionIndex(), true);
  }

  private void setupDrawer() {
    mDrawerMgr = new DrawerManager(this, mContent, new DrawerManager.DrawerManagerListener() {
      @Override
      public void onChannelClick() {
        updateSectionForChannel();
      }

      @Override
      public void onDrawerClick(int position) {
        selectSection(position, true);
      }

      @Override
      public void onDrawerOpen(boolean opened) {
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    });

    // open on first launch?
    // mDrawerMgr.openDrawer();
  }

  private void selectSection(int position, boolean animate) {
    // short curcuit trying to select the same position
    if (mCurrentSection == position)
      return;

    mCurrentSection = position;

    mDrawerMgr.setItemChecked(position, true);

    // clear back stack when using drawer
    getFragmentManager().popBackStack();
    Utils.showFragment(this, mContent.fragmentForIndex(position), R.id.fragment_holder, animate ? 3 : 0, false);
  }

  /**
   * When using the ActionBarDrawerToggle, you must call it during
   * onPostCreate() and onConfigurationChanged()...
   */

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerMgr.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggles
    mDrawerMgr.onConfigurationChanged(newConfig);

    // the spinners title and subtitle change on different orientations
    // must tell the adaptor it's views need to be refreshed
    if (mActionBarSpinnerAdapter != null) {
      mActionBarSpinnerAdapter.notifyDataSetChanged();
    }
  }

  private void syncActionBarTitle() {
    YouTubeGridFragment fragment = currentYouTubeFragment();

    if (fragment != null)
      fragment.syncActionBarTitle();
  }

  private YouTubeGridFragment currentYouTubeFragment() {
    YouTubeGridFragment result = null;

    Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_holder);

    if (fragment instanceof YouTubeGridFragment)
      result = (YouTubeGridFragment) fragment;

    return result;
  }

  private VideoPlayer videoPlayer(boolean createIfNeeded) {
    if (createIfNeeded) {
      if (mPlayer == null) {
        mPlayer = new VideoPlayer(this, R.id.youtube_fragment, new VideoPlayer.VideoPlayerStateListener() {
          // called when the video player opens or closes, adjust the action bar title
          @Override
          public void stateChanged() {
            syncActionBarTitle();
          }
        });
      }
    }

    return mPlayer;
  }

  // DrawerActivitySupport
  @Override
  public void showPlaylistsFragment() {
    selectSection(1, true);
  }

  // DrawerActivitySupport
  @Override
  public void installFragment(Fragment fragment, boolean animate) {
    Utils.showFragment(this, fragment, R.id.fragment_holder, animate ? 1 : 0, true);
  }

  // DrawerActivitySupport
  @Override
  public void setActionBarTitle(CharSequence title, CharSequence subtitle) {
    if (mActionBarSpinnerAdapter != null) {
      mActionBarSpinnerAdapter.setTitleAndSubtitle(title, subtitle);
    } else {
      ActionBar bar = getActionBar();

      if (bar != null) {
        bar.setTitle(title);
        bar.setSubtitle(subtitle);
      }
    }
  }

  // DrawerActivitySupport
  @Override
  public boolean actionBarTitleHandled() {
    // if video player is up, show the video title
    VideoPlayer player = videoPlayer(false);
    if (player != null && player.visible()) {
      setActionBarTitle(getResources().getString(R.string.now_playing), player.title());

      return true;
    }

    return false;
  }

  // DrawerActivitySupport
  @Override
  public boolean isPlayerVisible() {
    VideoPlayer player = videoPlayer(false);
    return (player != null && player.visible());
  }

  // DrawerActivitySupport
  @Override
  public void playVideo(VideoPlayer.PlayerParams params) {
    // could use this, but we have no control for mute commericals and play next automatically etc.
    // YouTubeAPI.playMovie(this, videoId, true);
    videoPlayer(true).open(params);
  }
}

