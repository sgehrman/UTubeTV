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
import com.sickboots.sickvideos.youtube.YouTubeAPIService;
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

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
  private View mEmptyView;
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
      if (intent.getAction().equals(YouTubeAPIService.DATA_READY_INTENT)) {
        String param = intent.getStringExtra(YouTubeAPIService.DATA_READY_INTENT_PARAM);

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

    // use same instance if activity is recreated under our feet
    setRetainInstance(true);

    mRequest = (YouTubeServiceRequest) getArguments().getParcelable("request");

    ViewGroup rootView = (ViewGroup) inflater.inflate(mTheme_resId, container, false);
    mGridView = (GridView) rootView.findViewById(R.id.gridview);

    mEmptyView = Util.emptyListView(getActivity(), "Talking to YouTube...");
    rootView.addView(mEmptyView);

    mGridView.setEmptyView(mEmptyView);

    // .015 is the default
    mGridView.setFriction(0.005f);



    mAdapter = new YouTubeListAdapter(getActivity(),
        mTheme_itemResId, null,
        new String[] {},
        new int[] {}
        , 0);

    mGridView.setOnItemClickListener(mAdapter);

    // Load the content
    getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
      @Override
      public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        DatabaseTables.DatabaseTable table = DatabaseTables.videoTable();

        String sortOrder = null;
        String[] selectionArgs=table.whereArgs(DatabaseTables.ALL_ITEMS, mRequest.requestIdentifier());
        String selection=table.whereClause(DatabaseTables.ALL_ITEMS, mRequest.requestIdentifier());;
        String[] projection = table.projection(DatabaseTables.ALL_ITEMS);

        YouTubeAPIService.startRequest(getActivity(), mRequest);

        return new CursorLoader(getActivity(),
            YouTubeContentProvider.URI_PERSONS, projection, selection, selectionArgs, sortOrder);
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

    mGridView.setAdapter(mAdapter);



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
    IntentFilter intentFilter = new IntentFilter(YouTubeAPIService.DATA_READY_INTENT);
    LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, intentFilter);

      // triggers an update for the title, lame hack
      HostActivitySupport provider = (HostActivitySupport) getActivity();
      provider.fragmentWasInstalled();
  }

  public void handleClick(YouTubeData itemMap) {
//    switch (mList.type()) {
//      case RELATED:
//      case SEARCH:
//      case LIKED:
//      case VIDEOS:
//        String videoId = itemMap.mVideo;
//        String title = itemMap.mTitle;
//
//        player().open(videoId, title, true);
//        break;
//      case PLAYLISTS: {
//        String playlistID = itemMap.mPlaylist;
//
//        if (playlistID != null) {
//          Fragment frag = YouTubeGridFragment.newInstance(YouTubeServiceRequest.videosRequest(playlistID));
//
//          HostActivitySupport provider = (HostActivitySupport) getActivity();
//
//          provider.installFragment(frag, true);
//        }
//      }
//      break;
//    }
  }

  private void updateForVariablesTheme() {
    String themeStyle = ApplicationHub.preferences(this.getActivity()).getString(PreferenceCache.THEME_STYLE, "0");
    if (Integer.parseInt(themeStyle) != 0) {
      mTheme_itemResId = R.layout.youtube_item_cards;
      mTheme_imageAlpha = 1.0f;
      mTheme_drawImageShadows = false;
      mTheme_resId = R.layout.fragment_grid_cards;
    } else {
      mTheme_imageAlpha = .7f;
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
    YouTubeAPIService.startRequest(getActivity(), mRequest);

    mEmptyView.setVisibility(View.VISIBLE);

    mGridView.setEmptyView(mEmptyView);
  }




  // use this??


  // get rid of the empty view.  Its not used after initial load, and this also
  // handles the case of no results.  we don't want the progress spinner to sit there and spin forever.
//  mGridView.setEmptyView(null);
//  mEmptyView.setVisibility(View.INVISIBLE);




  // ===========================================================================
  // Adapter

  private class YouTubeListAdapter extends SimpleCursorAdapter implements AdapterView.OnItemClickListener, VideoMenuView.VideoMenuViewListener {
    private final LayoutInflater inflater;
    int animationID = 0;
    boolean showHidden = false;

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
        YouTubeData itemMap = DatabaseTables.videoTable().cursorToItem(cursor);

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
      YouTubeData itemMap = DatabaseTables.videoTable().cursorToItem(cursor);

      holder.image.setAnimation(null);

      holder.image.setDrawShadows(mTheme_drawImageShadows);

      int defaultImageResID = 0;

      UrlImageViewHelper.setUrlDrawable(holder.image, itemMap.mThumbnail, defaultImageResID, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          if (!loadedFromCache) {
            imageView.setAlpha(mTheme_imageAlpha / 2);
            imageView.animate().setDuration(300).alpha(mTheme_imageAlpha);
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
        holder.menuButton.mVideoMap = itemMap;
      } else {
        holder.menuButton.setVisibility(View.GONE);
      }

      return convertView;
    }

    // VideoMenuViewListener
    @Override
    public void showVideoInfo(YouTubeData videoMap) {

    }

    // VideoMenuViewListener
    @Override
    public void showVideoOnYouTube(YouTubeData videoMap) {
      YouTubeAPI.playMovieUsingIntent(getActivity(), videoMap.mVideo);
    }

    // VideoMenuViewListener
    @Override
    public void hideVideo(YouTubeData videoMap) {
      // hidden is either null or not null
      // toggle it
      videoMap.setHidden(!videoMap.isHidden());

      DatabaseAccess database = new DatabaseAccess(getActivity(), mRequest);
      database.updateItem(videoMap);
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

