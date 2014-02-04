package com.sickboots.sickvideos.mainactivity;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.database.Database;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeContentProvider;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.AppUtils;
import com.sickboots.sickvideos.misc.EmptyListHelper;
import com.sickboots.sickvideos.misc.Preferences;
import com.sickboots.sickvideos.misc.ScrollTriggeredAnimator;
import com.sickboots.sickvideos.misc.Utils;
import com.sickboots.sickvideos.services.YouTubeListService;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class YouTubeGridFragment extends Fragment implements OnRefreshListener, OnDismissCallback, YouTubeCursorAdapter.YouTubeCursorAdapterListener, LoaderManager.LoaderCallbacks<Cursor> {

  private EmptyListHelper mEmptyListHelper;
  private YouTubeServiceRequest mRequest;
  private YouTubeCursorAdapter mAdapter;
  private PullToRefreshLayout mPullToRefreshLayout;
  private boolean mCachedHiddenPref;
  private DataReadyBroadcastReceiver broadcastReceiver;
  private String mFilter;

  public static YouTubeGridFragment newInstance(YouTubeServiceRequest request) {
    YouTubeGridFragment fragment = new YouTubeGridFragment();

    Bundle args = new Bundle();

    args.putParcelable("request", request);

    fragment.setArguments(args);

    return fragment;
  }

  public void syncActionBarTitle() {
    DrawerActivitySupport provider = (DrawerActivitySupport) getActivity();
    if (provider != null) {
      // activity can control the actionbar title, for example the player sets the Now Playing title
      if (!provider.actionBarTitleHandled()) {
        String subtitle = null;
        String title = null;

        if (mRequest != null) {
          int cnt = mAdapter.getCount();

          subtitle = String.format("%d ", cnt) + mRequest.unitName(cnt > 1 || cnt == 0);

          String channelName = provider.getContent().channelName();
          if (channelName != null)
            title = channelName;

          String containerName = mRequest.containerName();
          if (containerName != null)
            subtitle += ": " + containerName;
        }

        Utils.setActionBarTitle(getActivity(), title, subtitle);
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    GridView gridView;

    mRequest = (YouTubeServiceRequest) getArguments().getParcelable("request");
    mAdapter = YouTubeCursorAdapter.newAdapter(getActivity(), mRequest, this);

    ViewGroup rootView = mAdapter.rootView(container);

    // Now find the PullToRefreshLayout to setup
    mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.grid_frame_layout);

    // Now setup the PullToRefreshLayout
    ActionBarPullToRefresh.from(this.getActivity())
        // Mark All Children as pullable
        .allChildrenArePullable()
            // Set the OnRefreshListener
        .listener(this)
            // Finally commit the setup to our PullToRefreshLayout
        .setup(mPullToRefreshLayout);

    gridView = (GridView) rootView.findViewById(R.id.gridview);
    gridView.setOnItemClickListener(mAdapter);

    // enable this for swipe to dismiss to hide (TODO)
    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(mAdapter, this));
    //    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
    swingBottomInAnimationAdapter.setInitialDelayMillis(200);
    swingBottomInAnimationAdapter.setAbsListView(gridView);

    gridView.setAdapter(swingBottomInAnimationAdapter);

    // setup empty view
    mEmptyListHelper = new EmptyListHelper(rootView.findViewById(R.id.empty_view));
    gridView.setEmptyView(mEmptyListHelper.view());

    // create the loader
    getLoaderManager().initLoader(0, null, this);

    // dimmer only exists for dark mode
    View dimmerView = rootView.findViewById(R.id.dimmer);
    if (dimmerView != null)
      new ScrollTriggeredAnimator(gridView, dimmerView);

    return rootView;
  }

  // OnDismissCallback
  @Override
  public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
    Utils.message(getActivity(), String.format("Item Hidden"));

    for (int position : reverseSortedPositions) {
      Cursor cursor = (Cursor) mAdapter.getItem(position);
      YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, null);

      DatabaseAccess database = new DatabaseAccess(getActivity(), mRequest);

      // could get from database, but it's the same item
      // itemMap = database.getItemWithID(itemMap.mID);

      if (itemMap != null) {
        itemMap.setHidden(!itemMap.isHidden());
        database.updateItem(itemMap);
      }
    }
  }

  public void updateFilter(String filter) {
    if (!TextUtils.equals(mFilter, filter)) {
      mFilter = filter;
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  // YouTubeCursorAdapterListener
  @Override
  public void adapterDataChanged() {
    syncActionBarTitle();

    boolean enabled = false;

    if (enabled) {
      int cnt = mAdapter.getCount();

      if (cnt > 0)
        Utils.message(getActivity(), String.format("%d items", cnt));
    }
  }

  // YouTubeCursorAdapterListener
  @Override
  public void handleClickFromAdapter(YouTubeData itemMap) {
    DrawerActivitySupport provider = (DrawerActivitySupport) getActivity();

    switch (mRequest.type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        String videoId = itemMap.mVideo;
        String title = itemMap.mTitle;

        if (provider != null)
          provider.playVideo(videoId, title);

        break;
      case PLAYLISTS: {
        String playlistID = itemMap.mPlaylist;

        if (playlistID != null) {
          Fragment frag = YouTubeGridFragment.newInstance(YouTubeServiceRequest.videosRequest(playlistID, itemMap.mTitle));

          if (provider != null)
            provider.installFragment(frag, true);
        }
      }
      break;
    }
  }

  // YouTubeCursorAdapterListener
  @Override
  public Activity accesActivity() {
    return getActivity();
  }

  @Override
  public void onResume() {
    super.onResume();

    if (broadcastReceiver == null) {
      broadcastReceiver = new DataReadyBroadcastReceiver();
    }
    IntentFilter intentFilter = new IntentFilter(YouTubeListService.DATA_READY_INTENT);
    LocalBroadcastManager.getInstance(this.getActivity())
        .registerReceiver(broadcastReceiver, intentFilter);

    // reload if the hidden pref is now how we remember it
    reloadForPrefChange();

    syncActionBarTitle();
  }

  // OnRefreshListener
  @Override
  public void onRefreshStarted(View view) {
    YouTubeListService.startRequest(getActivity(), mRequest, true);
  }

  // called by activity on menu item action for show hidden files toggle
  public void reloadForPrefChange() {
    boolean showHidden = AppUtils.preferences(getActivity())
        .getBoolean(Preferences.SHOW_HIDDEN_ITEMS, false);

    if (mCachedHiddenPref != showHidden) {
      mCachedHiddenPref = showHidden;
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    DatabaseTables.DatabaseTable table = mRequest.databaseTable();

    // Debug.log(mRequest.toString());

    String sortOrder = (DatabaseTables.videoTable() == table) ? "vi" : "pl"; // stupid hack

    mCachedHiddenPref = AppUtils.preferences(getActivity())
        .getBoolean(Preferences.SHOW_HIDDEN_ITEMS, false);
    int queryID = DatabaseTables.VISIBLE_ITEMS;
    if (mCachedHiddenPref)
      queryID = DatabaseTables.ALL_ITEMS;

    Database.DatabaseQuery queryParams = table.queryParams(queryID, mRequest.requestIdentifier(), mFilter);

    // startRequest below will notify when done and we hide the progress bar
    mEmptyListHelper.updateEmptyListView("Talking to YouTube...", false);

    YouTubeListService.startRequest(getActivity(), mRequest, false);

    return new CursorLoader(getActivity(), YouTubeContentProvider.contentsURI(getActivity()), queryParams.mProjection, queryParams.mSelection, queryParams.mSelectionArgs, sortOrder);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
    mAdapter.swapCursor(c);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    mAdapter.swapCursor(null);
  }

  private class DataReadyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(YouTubeListService.DATA_READY_INTENT)) {
        String param = intent.getStringExtra(YouTubeListService.DATA_READY_INTENT_PARAM);

        // stop the pull to refresh indicator
        // Notify PullToRefreshLayout that the refresh has finished
        mPullToRefreshLayout.setRefreshComplete();

        // in the case of no results, we need to update the emptylist view to reflect that
        // This only shows up at launch, or the first time a list is requested
        mEmptyListHelper.updateEmptyListView("List is Empty", true);
      }
    }
  }
}

