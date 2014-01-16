package com.sickboots.sickvideos;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.sickboots.sickvideos.database.Database;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeContentProvider;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.AppUtils;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.EmptyListHelper;
import com.sickboots.sickvideos.misc.Preferences;
import com.sickboots.sickvideos.misc.ScrollTriggeredAnimator;
import com.sickboots.sickvideos.misc.Utils;
import com.sickboots.sickvideos.services.YouTubeListService;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.VideoPlayer;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class YouTubeGridFragment extends Fragment
    implements OnRefreshListener, OnDismissCallback, YouTubeCursorAdapter.YouTubeCursorAdapterListener {

  // Activity should host a player
  public static interface HostActivitySupport {
    public VideoPlayer videoPlayer(boolean createIfNeeded);

    public void showPlaylistsFragment();

    public void installFragment(Fragment fragment, boolean animate);
  }

  EmptyListHelper mEmptyListHelper;

  private YouTubeServiceRequest mRequest;
  private YouTubeCursorAdapter mAdapter;
  PullToRefreshLayout mPullToRefreshLayout;

  private DataReadyBroadcastReceiver broadcastReceiver;

  public static YouTubeGridFragment newInstance(YouTubeServiceRequest request) {
    YouTubeGridFragment fragment = new YouTubeGridFragment();

    Bundle args = new Bundle();

    args.putParcelable("request", request);

    fragment.setArguments(args);

    return fragment;
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

  public void playerStateChanged() {
    syncActionBarTitle();
  }

  private void syncActionBarTitle() {
    CharSequence title = null;
    CharSequence subtitle = null;
    if (mRequest != null) {
      title = mRequest.title();
      subtitle = mRequest.subtitle();
    }

    // if video player is up, show the video title
    VideoPlayer player = videoPlayer(false);
    if (player != null && player.visible()) {
      title = "Now Playing";
      subtitle = player.title();
    }

    Utils.setActionBarTitle(getActivity(), title, subtitle);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
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

    mEmptyListHelper = new EmptyListHelper(getActivity());

    mEmptyListHelper.updateEmptyListView("Talking to YouTube...", false);
    rootView.addView(mEmptyListHelper.view());
    gridView.setEmptyView(mEmptyListHelper.view());

    gridView.setOnItemClickListener(mAdapter);

    // enable this for swipe to dismiss to hide (TODO)
    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(mAdapter, this));
//    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
    swingBottomInAnimationAdapter.setInitialDelayMillis(200);
    swingBottomInAnimationAdapter.setAbsListView(gridView);

    gridView.setAdapter(swingBottomInAnimationAdapter);

    createLoader();

    // dimmer only exists for dark mode
    View dimmerView = rootView.findViewById(R.id.dimmer);
    if (dimmerView != null)
      new ScrollTriggeredAnimator(gridView, dimmerView);

    return rootView;
  }

  // OnDismissCallback
  @Override
  public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
    Utils.toast(getActivity(), "Item Hidden");
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

  // YouTubeCursorAdapterListener
  @Override
  public void handleClickFromAdapter(YouTubeData itemMap) {
    switch (mRequest.type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        String videoId = itemMap.mVideo;
        String title = itemMap.mTitle;
        boolean fullScreen = AppUtils.preferences(getActivity()).getBoolean(Preferences.PLAY_FULLSCREEN, false);

        if (fullScreen)
          YouTubeAPI.playMovie(getActivity(), videoId, true);
        else
          videoPlayer(true).open(videoId, title);

        break;
      case PLAYLISTS: {
        String playlistID = itemMap.mPlaylist;

        if (playlistID != null) {
          Fragment frag = YouTubeGridFragment.newInstance(YouTubeServiceRequest.videosRequest(playlistID, itemMap.mTitle));

          HostActivitySupport provider = (HostActivitySupport) getActivity();

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
    LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, intentFilter);

    syncActionBarTitle();
  }

  private VideoPlayer videoPlayer(boolean createIfNeeded) {
    HostActivitySupport provider = (HostActivitySupport) getActivity();

    if (provider != null)
      return provider.videoPlayer(createIfNeeded);

    Debug.log("Activity null, asking for videoplayer");

    return null;
  }

  // OnRefreshListener
  @Override
  public void onRefreshStarted(View view) {
    YouTubeListService.startRequest(getActivity(), mRequest, true);
  }

  private void createLoader() {
    getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
      @Override
      public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        DatabaseTables.DatabaseTable table = mRequest.databaseTable();

        String sortOrder = (DatabaseTables.videoTable() == table) ? "vi" : "pl"; // stupid hack

        Database.DatabaseQuery queryParams = table.queryParams(DatabaseTables.VISIBLE_ITEMS, mRequest.requestIdentifier());

        YouTubeListService.startRequest(getActivity(), mRequest, false);

        return new CursorLoader(getActivity(),
            YouTubeContentProvider.contentsURI(getActivity()), queryParams.mProjection, queryParams.mSelection, queryParams.mSelectionArgs, sortOrder);
      }

      @Override
      public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        mAdapter.swapCursor(c);
      }

      @Override
      public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
      }
    });

  }
}

