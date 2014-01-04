

package com.sickboots.sickvideos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.sickboots.sickvideos.activities.SettingsActivity;
import com.sickboots.sickvideos.misc.AppUtils;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // must set the theme before we do anything else
    String themeStyle = AppUtils.preferences(this).getString(Preferences.THEME_STYLE, "0");
    int flag = Integer.parseInt(themeStyle);
    switch (flag) {
      case 0:
        break;
      case 1:
        setTheme(R.style.ActivityThemeLight);
        break;
      case 2:
        setTheme(R.style.ActivityThemeLight);
        break;
    }

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawer);

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

    mPurchaseHelper = new PurchaseHelper(this);

    // general app tweaks
//  Utils.activateStrictMode(this);
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
        boolean animate = true;

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

  public Dialog pickViewStyleDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    String themeStyle = AppUtils.preferences(DrawerActivity.this).getString(Preferences.THEME_STYLE, "0");
    int selectedIndex = Integer.parseInt(themeStyle);

    builder.setTitle("Pick a style")
        .setSingleChoiceItems(R.array.view_styles, selectedIndex, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                AppUtils.preferences(DrawerActivity.this).setString(Preferences.THEME_STYLE, "0");
                break;
              case 1:
                AppUtils.preferences(DrawerActivity.this).setString(Preferences.THEME_STYLE, "1");
                break;
              case 2:
                AppUtils.preferences(DrawerActivity.this).setString(Preferences.THEME_STYLE, "2");
                break;
              default:
                break;
            }

            dialog.dismiss();
          }
        });

    return builder.create();
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

      case R.id.action_rate_app:
        intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(Uri.parse("market://details?id=" + getApplicationInfo().packageName));
        startActivity(intent);

        return true;
      case R.id.action_switch_view:
        Dialog theDialog = pickViewStyleDialog();

        theDialog.show();
        return true;

      case R.id.action_buy_taco:
        mPurchaseHelper.onBuyGasButtonClicked(null, this);

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setupDrawer() {
    mDrawerMgr = new DrawerManager(this, new DrawerManager.DrawerManagerListener() {
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

    Utils.showFragment(this, Content.fragmentForIndex(position), R.id.fragment_holder, animate ? 3 : 0, false);
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
        mPlayer = new VideoPlayer(this, R.id.video_fragment_container, new VideoPlayer.VideoPlayerStateListener() {

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
