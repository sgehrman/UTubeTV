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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeContentProvider;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;
import com.sickboots.sickvideos.misc.ScrollTriggeredAnimator;
import com.sickboots.sickvideos.misc.StandardAnimations;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.VideoImageView;
import com.sickboots.sickvideos.youtube.VideoPlayer;
import com.sickboots.sickvideos.youtube.YouTubeAPI;
import com.sickboots.sickvideos.services.YouTubeListService;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class YouTubeGridFragment extends Fragment
    implements PullToRefreshAttacher.OnRefreshListener {

  // Activity should host a player
  public interface HostActivitySupport {
    public VideoPlayer videoPlayer();

    void fragmentWasInstalled();

    public void installFragment(Fragment fragment, boolean animate);
  }

  private YouTubeServiceRequest mRequest;
  private GridView mGridView;
  private YouTubeListAdapter mAdapter;

  private DataReadyBroadcastReceiver broadcastReceiver;

  // theme parameters
  private float mTheme_imageAlpha;
  private int mTheme_itemResId;
  private int mTheme_resId;
  private boolean mTheme_drawImageShadows;

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
        Util.PullToRefreshListener ptrl = (Util.PullToRefreshListener) getActivity();
        if (ptrl != null) // could be null if activity was destroyed
          ptrl.setRefreshComplete();
      }
    }
  }

  public CharSequence actionBarTitle() {
    CharSequence title = null;
    if (mRequest != null)
      title = mRequest.name();

    // if video player is up, show the video title
    if (player().visible()) {
      title = player().title();
    }

    return title;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    updateForVariablesTheme();

    mRequest = (YouTubeServiceRequest) getArguments().getParcelable("request");

    ViewGroup rootView = (ViewGroup) inflater.inflate(mTheme_resId, container, false);
    mGridView = (GridView) rootView.findViewById(R.id.gridview);

    View emptyView = Util.emptyListView(getActivity(), "Talking to YouTube...");
    rootView.addView(emptyView);
    mGridView.setEmptyView(emptyView);

    // .015 is the default
    mGridView.setFriction(0.005f);

    mAdapter = new YouTubeListAdapter(getActivity(),
        mTheme_itemResId, null,
        new String[]{},
        new int[]{}
        , 0);

    mGridView.setOnItemClickListener(mAdapter);
    mGridView.setAdapter(mAdapter);

    // Load the content
    getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
      @Override
      public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        DatabaseTables.DatabaseTable table = mRequest.databaseTable();

        String sortOrder = (DatabaseTables.videoTable() == table) ? "vi" : "pl";
        String[] selectionArgs = table.whereArgs(DatabaseTables.VISIBLE_ITEMS, mRequest.requestIdentifier());
        String selection = table.whereClause(DatabaseTables.VISIBLE_ITEMS, mRequest.requestIdentifier());
        String[] projection = table.projection(DatabaseTables.VISIBLE_ITEMS);

        YouTubeListService.startRequest(getActivity(), mRequest, false);

        return new CursorLoader(getActivity(),
            YouTubeContentProvider.URI_CONTENTS, projection, selection, selectionArgs, sortOrder);
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

    // Add the Refreshable View and provide the refresh listener;
    Util.PullToRefreshListener ptrl = (Util.PullToRefreshListener) getActivity();
    ptrl.addRefreshableView(mGridView, this);

    // dimmer only exists for dark mode
    View dimmerView = rootView.findViewById(R.id.dimmer);
    if (dimmerView != null)
      new ScrollTriggeredAnimator(mGridView, dimmerView);

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();

    if (broadcastReceiver == null) {
      broadcastReceiver = new DataReadyBroadcastReceiver();
    }
    IntentFilter intentFilter = new IntentFilter(YouTubeListService.DATA_READY_INTENT);
    LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, intentFilter);

    // triggers an update for the title, lame hack
    HostActivitySupport provider = (HostActivitySupport) getActivity();
    provider.fragmentWasInstalled();
  }

  public void handleClick(YouTubeData itemMap) {
    switch (mRequest.type()) {
      case RELATED:
      case SEARCH:
      case LIKED:
      case VIDEOS:
        String videoId = itemMap.mVideo;
        String title = itemMap.mTitle;

        player().open(videoId, title, true);
        break;
      case PLAYLISTS: {
        String playlistID = itemMap.mPlaylist;

        if (playlistID != null) {
          Fragment frag = YouTubeGridFragment.newInstance(YouTubeServiceRequest.videosRequest(playlistID));

          HostActivitySupport provider = (HostActivitySupport) getActivity();

          provider.installFragment(frag, true);
        }
      }
      break;
    }
  }

  private void updateForVariablesTheme() {
    String themeStyle = ApplicationHub.preferences(this.getActivity()).getString(PreferenceCache.THEME_STYLE, "0");
    if (Integer.parseInt(themeStyle) != 0) {
      mTheme_itemResId = R.layout.youtube_item_cards;
      mTheme_imageAlpha = 1.0f;
      mTheme_drawImageShadows = false;
      mTheme_resId = R.layout.fragment_grid_cards;
    } else {
      mTheme_imageAlpha = 1.0f;
      mTheme_itemResId = R.layout.youtube_item_dark;
      mTheme_drawImageShadows = true;
      mTheme_resId = R.layout.fragment_grid_dark;
    }
  }

  private VideoPlayer player() {
    HostActivitySupport provider = (HostActivitySupport) getActivity();

    return provider.videoPlayer();
  }

  public void onRefreshStarted(View view) {
    YouTubeListService.startRequest(getActivity(), mRequest, true);
  }

  // ===========================================================================
  // Adapter

  private class YouTubeListAdapter extends SimpleCursorAdapter implements AdapterView.OnItemClickListener, VideoMenuView.VideoMenuViewListener {
    private final LayoutInflater inflater;
    private int animationID = 0;
    private boolean showHidden = false;
    private final YouTubeData mReusedData = new YouTubeData(); // avoids a memory alloc when drawing

    public YouTubeListAdapter(Context context, int layout, Cursor c, String[] from,
                              int[] to, int flags) {
      super(context, layout, c, from, to, flags);

      inflater = LayoutInflater.from(getActivity());
    }

    private void animateViewForClick(final View theView) {
      switch (animationID) {
        case 0:
          StandardAnimations.dosomething(theView);
          break;
        case 1:
          StandardAnimations.upAndAway(theView);
          break;
        case 2:
          StandardAnimations.rockBounce(theView);
          break;
        case 3:
          StandardAnimations.winky(theView, mTheme_imageAlpha);
          break;
        default:
          StandardAnimations.rubberClick(theView);
          animationID = -1;
          break;
      }

      animationID += 1;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
      ViewHolder holder = (ViewHolder) v.getTag();

      if (holder != null) {
        animateViewForClick(holder.image);

        Cursor cursor = (Cursor) getItem(position);
        YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, null);

        handleClick(itemMap);
      } else {
        Util.log("no holder on click?");
      }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;

      if (convertView == null) {
        convertView = inflater.inflate(mTheme_itemResId, null);

        holder = new ViewHolder();
        holder.image = (VideoImageView) convertView.findViewById(R.id.image);
        holder.title = (TextView) convertView.findViewById(R.id.text_view);
        holder.description = (TextView) convertView.findViewById(R.id.description_view);
        holder.duration = (TextView) convertView.findViewById(R.id.duration);
        holder.menuButton = (VideoMenuView) convertView.findViewById(R.id.menu_button);

        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();

        // reset some stuff that might have been set on an animation
        holder.image.setAlpha(1.0f);
        holder.image.setScaleX(1.0f);
        holder.image.setScaleY(1.0f);
        holder.image.setRotationX(0.0f);
        holder.image.setRotationY(0.0f);
      }

      Cursor cursor = (Cursor) getItem(position);
      YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, mReusedData);

      holder.image.setAnimation(null);

      holder.image.setDrawShadows(mTheme_drawImageShadows);

      int defaultImageResID = 0;

      UrlImageViewHelper.setUrlDrawable(holder.image, itemMap.mThumbnail, defaultImageResID, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          if (!loadedFromCache) {
            imageView.setAlpha(0.0f); // mTheme_imageAlpha / 2);
            imageView.animate().setDuration(200).alpha(mTheme_imageAlpha);
          } else
            imageView.setAlpha(mTheme_imageAlpha);
        }

      });

      boolean hidden = false;
      if (!showHidden)
        hidden = itemMap.isHidden();

      if (hidden)
        holder.title.setText("(Hidden)");
      else
        holder.title.setText(itemMap.mTitle);

      String duration = itemMap.mDuration;
      if (duration != null) {
        holder.duration.setVisibility(View.VISIBLE);

        PeriodFormatter formatter = ISOPeriodFormat.standard();
        Period p = formatter.parsePeriod(duration);
        Seconds s = p.toStandardSeconds();

        holder.duration.setText(Util.millisecondsToDuration(s.getSeconds() * 1000));
      } else {
        holder.duration.setVisibility(View.GONE);
      }

      // hide description if empty
      if (holder.description != null) {
        String desc = (String) itemMap.mDescription;
        if (desc != null && (desc.length() > 0)) {
          holder.description.setVisibility(View.VISIBLE);
          holder.description.setText(desc);
        } else {
          holder.description.setVisibility(View.GONE);
        }
      }

      // set video id on menu button so clicking can know what video to act on
      // only set if there is a videoId, playlists and others don't need this menu
      String videoId = (String) itemMap.mVideo;
      if (videoId != null && (videoId.length() > 0)) {
        holder.menuButton.setVisibility(View.VISIBLE);
        holder.menuButton.setListener(this);
        holder.menuButton.mId = itemMap.mID;
      } else {
        holder.menuButton.setVisibility(View.GONE);
      }

      return convertView;
    }

    // VideoMenuViewListener
    @Override
    public void showVideoInfo(Long itemId) {
      DatabaseAccess database = new DatabaseAccess(getActivity(), mRequest);
      YouTubeData videoMap = database.getItemWithID(itemId);

      if (videoMap != null)
      {
        Util.log("fix me");
      }
    }

    // VideoMenuViewListener
    @Override
    public void showVideoOnYouTube(Long itemId) {
      DatabaseAccess database = new DatabaseAccess(getActivity(), mRequest);
      YouTubeData videoMap = database.getItemWithID(itemId);

      if (videoMap != null)
        YouTubeAPI.playMovieUsingIntent(getActivity(), videoMap.mVideo);
    }

    // VideoMenuViewListener
    @Override
    public void hideVideo(Long itemId) {
      DatabaseAccess database = new DatabaseAccess(getActivity(), mRequest);
      YouTubeData videoMap = database.getItemWithID(itemId);

      if (videoMap != null) {
        videoMap.setHidden(!videoMap.isHidden());
        database.updateItem(videoMap);

      }
    }

    class ViewHolder {
      TextView title;
      TextView description;
      TextView duration;
      VideoImageView image;
      VideoMenuView menuButton;
    }
  }

}

