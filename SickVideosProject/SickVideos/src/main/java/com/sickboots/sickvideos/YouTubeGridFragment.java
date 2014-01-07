package com.sickboots.sickvideos;

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
import android.widget.GridView;

import com.sickboots.sickvideos.database.Database;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeContentProvider;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.EmptyListHelper;
import com.sickboots.sickvideos.misc.ScrollTriggeredAnimator;
import com.sickboots.sickvideos.misc.Utils;
import com.sickboots.sickvideos.services.YouTubeListService;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.VideoPlayer;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class YouTubeGridFragment extends Fragment
    implements OnRefreshListener, YouTubeCursorAdapter.YouTubeCursorAdapterListener {

  EmptyListHelper mEmptyListHelper;

  // Activity should host a player
  public interface HostActivitySupport {
    public VideoPlayer videoPlayer(boolean createIfNeeded);

    public void installFragment(Fragment fragment, boolean animate);
  }

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
    Utils.setActionBarTitle(getActivity(), actionBarTitle());
  }

  private CharSequence actionBarTitle() {
    CharSequence title = null;
    if (mRequest != null)
      title = mRequest.title();

    // if video player is up, show the video title
    VideoPlayer player = videoPlayer(false);
    if (player != null && player.visible()) {
      title = player.title();
    }

    return title;
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

    // .015 is the default
    gridView.setFriction(0.005f);

    gridView.setOnItemClickListener(mAdapter);
    gridView.setAdapter(mAdapter);

    createLoader();

    // dimmer only exists for dark mode
    View dimmerView = rootView.findViewById(R.id.dimmer);
    if (dimmerView != null)
      new ScrollTriggeredAnimator(gridView, dimmerView);

    return rootView;
  }

  @Override
  public void handleClickFromAdapter(YouTubeData itemMap) {
    switch (mRequest.type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        String videoId = itemMap.mVideo;
        String title = itemMap.mTitle;

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

  @Override
  public void onResume() {
    super.onResume();

    if (broadcastReceiver == null) {
      broadcastReceiver = new DataReadyBroadcastReceiver();
    }
    IntentFilter intentFilter = new IntentFilter(YouTubeListService.DATA_READY_INTENT);
    LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, intentFilter);

    Utils.setActionBarTitle(getActivity(), actionBarTitle());
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
            YouTubeContentProvider.URI_CONTENTS, queryParams.mProjection, queryParams.mSelection, queryParams.mSelectionArgs, sortOrder);
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

