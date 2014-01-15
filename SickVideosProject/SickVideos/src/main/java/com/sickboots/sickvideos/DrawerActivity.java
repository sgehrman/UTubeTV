

package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.sickboots.iconicdroid.IconicActivity;
import com.sickboots.sickvideos.activities.ChannelLookupActivity;
import com.sickboots.sickvideos.activities.SettingsActivity;
import com.sickboots.sickvideos.misc.AppUtils;
import com.sickboots.sickvideos.misc.ColorPickerFragment;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.Preferences;
import com.sickboots.sickvideos.misc.Utils;
import com.sickboots.sickvideos.youtube.VideoPlayer;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.Observable;
import java.util.Observer;

public class DrawerActivity extends Activity implements YouTubeGridFragment.HostActivitySupport, Observer {
  VideoPlayer mPlayer;
  private int mCurrentSection = -1;
  private DrawerManager mDrawerMgr;
  private Toast backButtonToast;
  private long lastBackPressTime = 0;
  private PurchaseHelper mPurchaseHelper;
  private Content mContent;

  @Override
  public void showPlaylistsFragment() {
    selectSection(1, true);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_drawer);

    Content.ProductCode code = Content.ProductCode.valueOf(getResources().getString(R.string.content));
    mContent = new Content(this, code);

    setupDrawer();

    // enable ActionBar app icon to behave as action to toggle nav drawer
    getActionBar().setDisplayHomeAsUpEnabled(true);

    int section = 0;
    if (savedInstanceState != null) {
      section = savedInstanceState.getInt("section");
    } else {
      String sectionIndexString = AppUtils.preferences(this).getString(Preferences.DRAWER_SECTION_INDEX, "0");
      section = Integer.parseInt(sectionIndexString);
    }

    selectSection(section, false);

    // disabled until polished
    // mPurchaseHelper = new PurchaseHelper(this);

    // general app tweaks
    // Debug.activateStrictMode();
    Utils.ignoreObsoleteCapacitiveMenuButton(this);

    // show player if activity was destroyed and recreated
    if (savedInstanceState != null) {
      boolean showPlayer = savedInstanceState.getBoolean("player_visible");

      // video player fragment restores itself, so just show it and let it do its thing
      if (showPlayer)
        videoPlayer(true).restore();
    }
  }

  // We're being destroyed. It's important to dispose of the helper here!
  @Override
  public void onDestroy() {
    super.onDestroy();

    // very important:
    if (mPurchaseHelper != null) {
      mPurchaseHelper.destroy();
      mPurchaseHelper = null;
    }
  }

  @Override
  public void onStop() {
    super.onStop();

    // for AppUtils.THEME_CHANGED
    AppUtils.instance(this).deleteObserver(this);
  }

  @Override
  public void onStart() {
    super.onStart();

    // for AppUtils.THEME_CHANGED
    AppUtils.instance(this).addObserver(this);
  }

  @Override  // Observer
  public void update(Observable observable, Object data) {
    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(AppUtils.THEME_CHANGED)) {
        // animate doesn't work, puts new activity in the background.  use recreate instead
        boolean animate = false;
        if (animate) {
          startActivity(getIntent());
          overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
          finish();
        } else {
          // not sure how to to get recreate to animate, so we use the above code when animating which is like a recreate
          recreate();
        }

      }
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (mCurrentSection != -1) {
      outState.putInt("section", mCurrentSection);

      if (mPlayer != null) {
        if (mPlayer.visible()) {
          outState.putBoolean("player_visible", true);
        }
      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    // save current section to prefs file so we can start where the user left off on a relaunch
    AppUtils.preferences(this).setString(Preferences.DRAWER_SECTION_INDEX, Integer.toString(mCurrentSection));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);

    if (!Debug.isDebugBuild()) {
      menu.removeItem(R.id.action_buy_taco);
      menu.removeItem(R.id.action_channel_lookup);
      menu.removeItem(R.id.action_color_picker);
      menu.removeItem(R.id.action_show_icons);
      menu.removeItem(R.id.action_switch_view);
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onBackPressed() {
    // hides the video player if visible
    if (mPlayer != null && mPlayer.visible())
      mPlayer.close();
    else {
      if (getFragmentManager().getBackStackEntryCount() == 0) {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
          backButtonToast = Toast.makeText(this, "Press back again to close", 4000);
          backButtonToast.show();
          this.lastBackPressTime = System.currentTimeMillis();
        } else {
          if (backButtonToast != null) {
            backButtonToast.cancel();
          }
          super.onBackPressed();
        }
      } else {
        super.onBackPressed();
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Pass on the activity result to the helper for handling
    if (mPurchaseHelper != null && mPurchaseHelper.handleActivityResult(requestCode, resultCode, data)) {
      // handled by helper
    } else {
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
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;

    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    if (mDrawerMgr.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle action buttons
    switch (item.getItemId()) {
      case R.id.action_settings:
        intent = new Intent();
        intent.setClass(DrawerActivity.this, SettingsActivity.class);
        startActivity(intent);

        return true;

      case R.id.action_more_apps:
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://search?q=pub:Sick Boots"));
        startActivity(intent);

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

      case R.id.action_buy_taco:
        if (mPurchaseHelper != null)
          mPurchaseHelper.onBuyGasButtonClicked(null, this);
        return true;
      case R.id.action_channel_lookup:
        intent = new Intent();
        intent.setClass(DrawerActivity.this, ChannelLookupActivity.class);
        startActivity(intent);
        return true;
      case R.id.action_color_picker:
        Fragment fragment = new ColorPickerFragment();
        Utils.showFragment(this, fragment, R.id.fragment_holder, 0, true);

        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setupDrawer() {
    mDrawerMgr = new DrawerManager(this, mContent, new DrawerManager.DrawerManagerListener() {
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
    // Pass any configuration change to the drawer toggls
    mDrawerMgr.onConfigurationChanged(newConfig);
  }

  // ----------------------------------------------------
  // HostActivitySupport

  @Override
  public void installFragment(Fragment fragment, boolean animate) {
    Utils.showFragment(this, fragment, R.id.fragment_holder, animate ? 1 : 0, true);
  }

  @Override
  public VideoPlayer videoPlayer(boolean createIfNeeded) {
    if (createIfNeeded) {
      if (mPlayer == null) {
        mPlayer = new VideoPlayer(this, R.id.youtube_fragment, new VideoPlayer.VideoPlayerStateListener() {

          // called when the video player opens or closes, adjust the action bar title

          @Override
          public void stateChanged() {

            Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_holder);

            if (fragment instanceof YouTubeGridFragment) {
              YouTubeGridFragment ytgf = (YouTubeGridFragment) fragment;

              ytgf.playerStateChanged();

            }
          }
        });
      }
    }

    return mPlayer;
  }
}
