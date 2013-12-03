

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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.ColorPickerFragment;
import com.sickboots.sickvideos.misc.PreferenceCache;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.VideoPlayer;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.Observable;
import java.util.Observer;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class DrawerActivity extends Activity implements YouTubeGridFragment.HostActivitySupport, Util.PullToRefreshListener, Observer {
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;
  VideoPlayer mPlayer;
  private int mCurrentSection = -1;

  private PullToRefreshAttacher mPullToRefreshAttacher;

  // MainActivity creates us using this
  public static void start(Activity activity) {
    // start drawer activity
    Intent intent = new Intent();
    intent.setClass(activity, DrawerActivity.class);
    activity.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // must set the theme before we do anything else
    String themeStyle = ApplicationHub.preferences().getString(PreferenceCache.THEME_STYLE, "0");
    int flag = Integer.parseInt(themeStyle);
    if (flag != 0)
      setTheme(R.style.ActivityThemeLight);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawer);

    String[] names = new String[]{"Favorites", "Likes", "History", "Uploads", "Watch Later", "Color Picker", "Connections"};
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.left_drawer);

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    // set up the drawer's list view with items and click listener
    mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, names));
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    // enable ActionBar app icon to behave as action to toggle nav drawer
    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);

    // set custom color
//    String customColor = ApplicationHub.instance().getPref(ApplicationHub.ACTION_BAR_COLOR, null);
//    if (customColor != null) {
//      int color = Integer.parseInt(customColor);
//      getActionBar().setBackgroundDrawable(new ColorDrawable(color));
//    }

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the sliding drawer and the action bar app icon
    mDrawerToggle = new ActionBarDrawerToggle(
        this,                  /* host Activity */
        mDrawerLayout,         /* DrawerLayout object */
        R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
        R.string.drawer_open,  /* "open drawer" description for accessibility */
        R.string.drawer_close  /* "close drawer" description for accessibility */
    ) {
      public void onDrawerClosed(View view) {
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      public void onDrawerOpened(View drawerView) {
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    int section = 0;
    if (savedInstanceState != null) {
      section = savedInstanceState.getInt("section");
    } else {
      String sectionIndexString = ApplicationHub.preferences().getString(PreferenceCache.DRAWER_SECTION_INDEX, "0");
      section = Integer.parseInt(sectionIndexString);
    }

    selectSection(section, false);

    // general app tweaks
//  Util.activateStrictMode(this);
    Util.ignoreObsoleteCapacitiveMenuButton(this);

    // This shit is buggy, must be created in onCreate of the activity, can't be created in the fragment.
    mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

    // show player if activity was destroyed and recreated
    if (savedInstanceState != null) {
      boolean showPlayer = savedInstanceState.getBoolean("player_visible");

      // video player fragment restores itself, so just show it and let it do its thing
      if (showPlayer)
        videoPlayer().restore();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // for ApplicationHub.THEME_CHANGED
    ApplicationHub.instance().deleteObserver(this);
  }

  @Override
  public void onStart() {
    super.onStart();

    // for ApplicationHub.THEME_CHANGED
    ApplicationHub.instance().addObserver(this);
  }

  @Override  // Observer
  public void update(Observable observable, Object data) {
    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(ApplicationHub.THEME_CHANGED)) {
        recreate();
      }
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt("section", mCurrentSection);

    if (mPlayer != null) {
      if (mPlayer.visible()) {
        outState.putBoolean("player_visible", true);
      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    // save current section to prefs file so we can start where the user left off on a relaunch
    ApplicationHub.preferences().setString(PreferenceCache.DRAWER_SECTION_INDEX, Integer.toString(mCurrentSection));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onBackPressed() {
    if (getFragmentManager().getBackStackEntryCount() == 0) {

      // hides the video player if visible
      if (mPlayer != null)
        mPlayer.close(true);

      // do nothing, we don't want the app to disappear
      return;
    }

    super.onBackPressed();
  }

  // Add the Refreshable View and provide the refresh listener;
  @Override
  public void addRefreshableView(View theView, PullToRefreshAttacher.OnRefreshListener listener) {
    mPullToRefreshAttacher.addRefreshableView(theView, listener);
  }

  @Override
  public void setRefreshComplete() {
    mPullToRefreshAttacher.setRefreshComplete();
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
            Util.toast(this, errorMessage);
          }
        }

        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }

  public Dialog pickViewStyleDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    String themeStyle = ApplicationHub.preferences().getString(PreferenceCache.THEME_STYLE, "0");
    int selectedIndex = Integer.parseInt(themeStyle);

    builder.setTitle("Pick a style")
        .setSingleChoiceItems(R.array.view_styles, selectedIndex, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                ApplicationHub.preferences().setString(PreferenceCache.THEME_STYLE, "0");
                break;
              case 1:
                ApplicationHub.preferences().setString(PreferenceCache.THEME_STYLE, "1");
                break;
              case 2:
                ApplicationHub.preferences().setString(PreferenceCache.THEME_STYLE, "2");
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
    if (mDrawerToggle.onOptionsItemSelected(item)) {
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
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /* The click listner for ListView in the navigation drawer */
  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      selectSection(position, true);

      mDrawerLayout.closeDrawer(mDrawerList);
    }
  }

  private void selectSection(int position, boolean animate) {
    // short curcuit trying to select the same position
    if (mCurrentSection == position)
      return;

    Fragment fragment = null;

    switch (position) {
      case 0:
        fragment = YouTubeGridFragment.relatedFragment(YouTubeAPI.RelatedPlaylistType.FAVORITES);
        break;
      case 1:
        fragment = YouTubeGridFragment.relatedFragment(YouTubeAPI.RelatedPlaylistType.LIKES);
        break;
      case 2:
        fragment = YouTubeGridFragment.relatedFragment(YouTubeAPI.RelatedPlaylistType.WATCHED);
        break;
      case 3:
        fragment = YouTubeGridFragment.relatedFragment(YouTubeAPI.RelatedPlaylistType.UPLOADS);
        break;
      case 4:
        fragment = YouTubeGridFragment.relatedFragment(YouTubeAPI.RelatedPlaylistType.WATCHLATER);
        break;
      case 5:
        fragment = new ColorPickerFragment();
        break;
      case 6:
        fragment = YouTubeGridFragment.playlistsFragment("UC07XXQh04ukEX68loZFgnVw");
        break;
    }

    mCurrentSection = position;
    mDrawerList.setItemChecked(position, true);

    Util.showFragment(this, fragment, R.id.fragment_holder, animate ? 3 : 0, false);
  }

  private YouTubeGridFragment installedFragment() {
    Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_holder);

    if (fragment instanceof YouTubeGridFragment)
      return (YouTubeGridFragment) fragment;

    return null;
  }

  /**
   * When using the ActionBarDrawerToggle, you must call it during
   * onPostCreate() and onConfigurationChanged()...
   */

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggls
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  private void updateActionBarTitle() {
    YouTubeGridFragment fragment = installedFragment();
    CharSequence title = null;

    if (fragment != null)
      title = fragment.actionBarTitle();

    if (title != null)
      getActionBar().setTitle(title);
  }

  // ----------------------------------------------------
  // HostActivitySupport

  @Override
  public void installFragment(Fragment fragment, boolean animate) {
    Util.showFragment(this, fragment, R.id.fragment_holder, animate ? 1 : 0, true);
  }

  @Override
  public void fragmentWasInstalled() {
    updateActionBarTitle();
  }

  @Override
  public VideoPlayer videoPlayer() {
    if (mPlayer == null) {
      mPlayer = new VideoPlayer(this, R.id.video_fragment_container, new VideoPlayer.VideoPlayerStateListener() {

        // called when the video player opens or closes, adjust the action bar title

        @Override
        public void stateChanged() {
          updateActionBarTitle();
        }
      });
    }

    return mPlayer;
  }

  // ----------------------------------------------------

}
