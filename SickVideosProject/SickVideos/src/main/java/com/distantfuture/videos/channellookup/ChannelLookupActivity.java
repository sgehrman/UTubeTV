package com.distantfuture.videos.channellookup;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.imageutils.ToolbarIcons;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.Utils;

public class ChannelLookupActivity extends Activity {
  private ChannelLookupListFragment listFragment;
  private MenuItem mSearchItem;
  private SearchView mSearchView;
  private Drawable mSearchDrawable;
  private boolean mSearchSubmitted = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_channel_lookup);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    listFragment = (ChannelLookupListFragment) getFragmentManager().findFragmentById(R.id.channel_list_fragment);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
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
  }

  private boolean endSearchActionBar() {
    mSearchSubmitted = true;  // had to add this for KitKat?  Maybe I'm doing something slightly non standard?
    if (mSearchItem.isActionViewExpanded())
      return mSearchItem.collapseActionView();

    return false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.search_menu, menu);

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

        mSearchDrawable = ToolbarIcons.icon(this, ToolbarIcons.IconID.SEARCH, color, 32);
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

}
