package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by sgehrman on 12/13/13.
 */
public class DrawerManager {

  public interface DrawerManagerListener {
    public void onDrawerClick(int position);

    public void onDrawerOpen(boolean openOrClose);
  }

  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;
  private DrawerManagerListener mListener;

  public DrawerManager(Activity activity, Content content, DrawerManagerListener listener) {
    super();

    mListener = listener;

    ArrayAdapter adapter = new ArrayAdapter<String>(activity,
        R.layout.drawer_list_item, content.drawerTitles());

    mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) activity.findViewById(R.id.left_drawer);

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    // set up the drawer's list view with items and click listener
    mDrawerList.setAdapter(adapter);
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the sliding drawer and the action bar app icon
    mDrawerToggle = new ActionBarDrawerToggle(
        activity,                  /* host Activity */
        mDrawerLayout,         /* DrawerLayout object */
        R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
        R.string.drawer_open,  /* "open drawer" description for accessibility */
        R.string.drawer_close  /* "close drawer" description for accessibility */
    ) {
      public void onDrawerClosed(View view) {
        mListener.onDrawerOpen(false);
      }

      public void onDrawerOpened(View drawerView) {
        mListener.onDrawerOpen(true);
      }
    };

    mDrawerLayout.setDrawerListener(mDrawerToggle);
  }

  public void setDrawerIndicatorEnabled(boolean set) {
    mDrawerToggle.setDrawerIndicatorEnabled(set);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    return mDrawerToggle.onOptionsItemSelected(item);
  }

  public void openDrawer() {
    mDrawerLayout.openDrawer(mDrawerList);
  }

  public void closeDrawer() {
    mDrawerLayout.closeDrawer(mDrawerList);
  }

  public void setItemChecked(int position, boolean checked) {
    mDrawerList.setItemChecked(position, checked);
  }

  public void syncState() {
    mDrawerToggle.syncState();
  }

  public void onConfigurationChanged(Configuration newConfig) {
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  /* The click listner for ListView in the navigation drawer */
  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      mListener.onDrawerClick(position);
      closeDrawer();
    }
  }


}
