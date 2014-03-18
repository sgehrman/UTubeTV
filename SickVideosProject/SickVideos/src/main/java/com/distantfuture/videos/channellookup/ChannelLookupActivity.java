package com.distantfuture.videos.channellookup;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.activities.StorageAccessActivity;
import com.distantfuture.videos.content.Content;
import com.distantfuture.videos.imageutils.ToolbarIcons;
import com.distantfuture.videos.misc.DUtils;
import com.distantfuture.videos.misc.JSONHelper;
import com.distantfuture.videos.misc.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelLookupActivity extends Activity {
  private static final int IMPORT_FILE = 889;
  private static final int EXPORT_FILE = 829;
  private ChannelLookupListFragment listFragment;
  private MenuItem mSearchItem;
  private SearchView mSearchView;
  private Drawable mSearchDrawable;
  private boolean mSearchSubmitted = false;

  public static void show(Activity activity) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.setClass(activity, ChannelLookupActivity.class);
    activity.startActivity(intent, opts.toBundle());
  }

  @Override
  public void finish() {
    super.finish();

    // animate out
    overridePendingTransition(R.anim.scale_out_rev, R.anim.scale_in_rev);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_channel_lookup);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    listFragment = (ChannelLookupListFragment) getFragmentManager().findFragmentById(R.id.channel_list_fragment);

    setActionBarTitle();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
        return true;

      case R.id.action_default_channels:
        Content.instance().resetToDefaults();
        break;

      case R.id.action_show_import_dialog:
        importFile();
        return true;

      case R.id.action_show_export_dialog:
        exportFile();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public String getQuery() {
    return listFragment.getQuery();
  }

  public void setQuery(String filter) {
    listFragment.setQuery(filter);
  }

  // called from adapter
  public void adapterDataChanged() {

    if (!TextUtils.isEmpty(getQuery()) && mSearchItem.isActionViewExpanded()) {
      int cnt = listFragment.getCount();

      Utils.message(this, String.format("Found: %d items", cnt));
    }

    setActionBarTitle();
  }

  private boolean endSearchActionBar() {
    mSearchSubmitted = true;  // had to add this for KitKat?  Maybe 19I'm doing something slightly non standard?
    if (mSearchItem.isActionViewExpanded())
      return mSearchItem.collapseActionView();

    return false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.edit_channels_menu, menu);

    setupSearchItem(menu);

    return super.onCreateOptionsMenu(menu);
  }

  private void setupSearchItem(Menu menu) {
    mSearchItem = menu.findItem(R.id.action_search);
    if (mSearchItem != null) {
      if (mSearchDrawable == null) {
        // seems insane, is this the best way of having a variable drawable resource by theme?
        int[] attrs = new int[]{R.attr.action_bar_icon_color};
        TypedArray ta = obtainStyledAttributes(attrs);
        int color = ta.getColor(0, 0);
        ta.recycle();

        mSearchDrawable = ToolbarIcons.icon(this, ToolbarIcons.IconID.SEARCH_PLUS, color, 32);
        mSearchDrawable.setAlpha(150);
      }
      mSearchItem.setIcon(mSearchDrawable);

      mSearchView = (SearchView) mSearchItem.getActionView();
      //      mSearchView.setSubmitButtonEnabled(true);
      mSearchView.setQueryHint("Search");

      // not sure if this is needed or not yet....
      SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
      SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
      mSearchView.setSearchableInfo(searchableInfo);

      mSearchItem.setActionView(mSearchView);

      // change the text color inside the searchView, There is no way to theme this shitty thing
      int textColor = getResources().getColor(android.R.color.primary_text_dark);
      int hintColor = getResources().getColor(android.R.color.secondary_text_dark);

      Utils.textViewColorChanger(mSearchView, textColor, hintColor);

      mSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
          // return false and clear the filter if clicked when a filter is set
          if (!TextUtils.isEmpty(getQuery())) {
            // this is needed since if the action bar refreshes, it will restore the query, kitkat seems to clear this though
            mSearchView.setQuery(null, true);

            // added for KitKat, previously setQuery to null would trigger the text listener
            setQuery(null);

            return false;
          }
          return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
          return true;
        }
      });

      mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
          if (mSearchItem != null) {
            return endSearchActionBar();
          }

          return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
          // Called when the action bar search text has changed.  Update
          // the search filter, and restart the loader to do a new query
          // with this filter.
          String filter = !TextUtils.isEmpty(newText) ? newText : null;
          boolean setFilter = true;

          if (mSearchSubmitted && filter == null) {
            // on KitKat collapseActionView call above on submit, sends null for newText, so preventing this from changing
            // the filter here
            setFilter = false;
          }

          mSearchSubmitted = false;  // had to add this for KitKat?  Maybe I'm doing something slightly non standard?

          if (setFilter)
            setQuery(filter);

          return setFilter;
        }
      });
    }
  }

  public void setActionBarTitle() {
    CharSequence title = "Channel Editor";
    CharSequence subtitle = "" + listFragment.getCount() + " Channels";

    ActionBar bar = getActionBar();

    if (bar != null) {
      bar.setTitle(title);
      bar.setSubtitle(subtitle);
    }
  }

  public void exportFile() {
    Map map = new HashMap();

    List<String> channels = listFragment.getChannels();

    if (channels != null && channels.size() > 0) {

      map.put("channels", channels);
      map.put("version", "1");

      String json = "";

      try {
        json = JSONHelper.toJSON(map).toString();
      } catch (Throwable t) {
        DUtils.log("exception " + t.toString());
        json = "";
      }

      if (json.length() > 0)
        StorageAccessActivity.save(this, null, json, "application/json");
    }
  }

  public void importFile() {
    StorageAccessActivity.load(this, "application/json");
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.setGroupVisible(R.id.kitkat_group, Utils.isKitKatOrNewer());

    return super.onPrepareOptionsMenu(menu);
  }

}
