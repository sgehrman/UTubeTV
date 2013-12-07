package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.lists.UIAccess;
import com.sickboots.sickvideos.lists.YouTubeList;
import com.sickboots.sickvideos.lists.YouTubeListDB;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;
import com.sickboots.sickvideos.misc.ScrollTriggeredAnimator;
import com.sickboots.sickvideos.misc.StandardAnimations;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.VideoImageView;
import com.sickboots.sickvideos.youtube.VideoPlayer;
import com.sickboots.sickvideos.youtube.YouTubeAPI;
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class YouTubeGridFragment extends Fragment
    implements PullToRefreshAttacher.OnRefreshListener {

  // Activity should host a player
  public interface HostActivitySupport {
    public VideoPlayer videoPlayer();

    void fragmentWasInstalled();

    public void installFragment(Fragment fragment, boolean animate);
  }

  private static final String SEARCH_QUERY = "query";
  private static final String LIST_TYPE = "type";
  private static final String CHANNEL_ID = "channel";
  private static final String RELATED_TYPE = "related";
  private static final String PLAYLIST_ID = "playlist";
  private YouTubeServiceRequest.RequestType requestType;
  private YouTubeListAdapter mAdapter;
  private YouTubeList mList;
  private View mEmptyView;
  private GridView mGridView;

  public static final String DATA_READY_INTENT = "com.sickboots.sickvideos.DataReady";
  public static final String DATA_READY_INTENT_PARAM = "com.sickboots.sickvideos.DataReady.param";
  private UploadBroadcastReceiver broadcastReceiver;

  // theme parameters
  private float mTheme_imageAlpha;
  private int mTheme_itemResId;
  private int mTheme_resId;
  private boolean mTheme_drawImageShadows;

  public static YouTubeGridFragment relatedFragment(YouTubeAPI.RelatedPlaylistType relatedType) {
    return newInstance(YouTubeServiceRequest.RequestType.RELATED, null, null, relatedType, null);
  }

  public static YouTubeGridFragment videosFragment(String playlistID) {
    return newInstance(YouTubeServiceRequest.RequestType.VIDEOS, null, playlistID, null, null);
  }

  public static YouTubeGridFragment playlistsFragment(String channelID) {
    return newInstance(YouTubeServiceRequest.RequestType.PLAYLISTS, channelID, null, null, null);
  }

  public static YouTubeGridFragment likedFragment() {
    return newInstance(YouTubeServiceRequest.RequestType.LIKED, null, null, null, null);
  }

  public static YouTubeGridFragment subscriptionsFragment() {
    return newInstance(YouTubeServiceRequest.RequestType.SUBSCRIPTIONS, null, null, null, null);
  }

  public static YouTubeGridFragment searchFragment(String searchQuery) {
    return newInstance(YouTubeServiceRequest.RequestType.SEARCH, null, null, null, searchQuery);
  }

  private static YouTubeGridFragment newInstance(YouTubeServiceRequest.RequestType requestType, String channelID, String playlistID, YouTubeAPI.RelatedPlaylistType relatedType, String searchQuery) {
    YouTubeGridFragment fragment = new YouTubeGridFragment();

    Bundle args = new Bundle();

    args.putSerializable(LIST_TYPE, requestType);
    args.putSerializable(RELATED_TYPE, relatedType);
    args.putString(CHANNEL_ID, channelID);
    args.putString(PLAYLIST_ID, playlistID);
    args.putString(SEARCH_QUERY, searchQuery);

    fragment.setArguments(args);

    return fragment;
  }

  private class UploadBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(DATA_READY_INTENT)) {
        String param = intent.getStringExtra(DATA_READY_INTENT_PARAM);

        mList.refetch();
      }
    }
  }

  public CharSequence actionBarTitle() {
    CharSequence title = null;
    if (mList != null)
      title = mList.name();

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

    requestType = (YouTubeServiceRequest.RequestType) getArguments().getSerializable(LIST_TYPE);

    ViewGroup rootView = (ViewGroup) inflater.inflate(mTheme_resId, container, false);
    mGridView = (GridView) rootView.findViewById(R.id.gridview);

    mEmptyView = Util.emptyListView(getActivity(), "Talking to YouTube...");
    rootView.addView(mEmptyView);

    mGridView.setEmptyView(mEmptyView);

    // .015 is the default
    mGridView.setFriction(0.005f);

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
      broadcastReceiver = new UploadBroadcastReceiver();
    }
    IntentFilter intentFilter = new IntentFilter(DATA_READY_INTENT);
    LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, intentFilter);


    if (mList == null) {
      mList = createList(getArguments());
      mAdapter = new YouTubeListAdapter();

      mGridView.setOnItemClickListener(mAdapter);
      mGridView.setAdapter(mAdapter);
      // load data if we have it already

      loadFromList();

      // triggers an update for the title, lame hack
      HostActivitySupport provider = (HostActivitySupport) getActivity();
      provider.fragmentWasInstalled();
    }
  }

  public void handleClick(YouTubeData itemMap) {
    switch (mList.type()) {
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
          Fragment frag = YouTubeGridFragment.videosFragment(playlistID);

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
    mList.refresh();

    mEmptyView.setVisibility(View.VISIBLE);

    mGridView.setEmptyView(mEmptyView);
  }

  private void loadFromList() {
    int savedScrollState = mGridView.getFirstVisiblePosition();

    mAdapter.clear();

    List items = mList.getItems();

    if (items != null) {
      mAdapter.addAll(items);

      // restore scroll position if we saved it
      mGridView.setSelection(savedScrollState);
    }

    // stop the pull to refresh indicator
    Util.PullToRefreshListener ptrl = (Util.PullToRefreshListener) getActivity();
    if (ptrl != null) // could be null if activity was destroyed
      ptrl.setRefreshComplete();
  }

  private UIAccess createUIAccess() {
    final Context appContext = getActivity().getApplicationContext();
    UIAccess access = new UIAccess() {
      @Override
      public void onResults() {
        loadFromList();

        // get rid of the empty view.  Its not used after initial load, and this also
        // handles the case of no results.  we don't want the progress spinner to sit there and spin forever.
        mGridView.setEmptyView(null);
        mEmptyView.setVisibility(View.INVISIBLE);
      }

      @Override
      public Context getContext() {
        return appContext;
      }
    };

    return access;
  }

  private YouTubeList createList(Bundle argsBundle) {
    YouTubeList result = null;

    String channelID = argsBundle.getString(CHANNEL_ID);
    String playlistID = argsBundle.getString(PLAYLIST_ID);
    String query = argsBundle.getString(SEARCH_QUERY);
    YouTubeAPI.RelatedPlaylistType relatedType = (YouTubeAPI.RelatedPlaylistType) argsBundle.getSerializable(RELATED_TYPE);

    UIAccess access = createUIAccess();

    switch (requestType) {
      case SUBSCRIPTIONS:
        result = new YouTubeListDB(YouTubeServiceRequest.subscriptionsSpec(), access);
        break;
      case PLAYLISTS:
        result = new YouTubeListDB(YouTubeServiceRequest.playlistsSpec(channelID), access);
        break;
      case CATEGORIES:
        Util.log("THIS is broken... Categories, just fix db integration");
        result = new YouTubeListDB(YouTubeServiceRequest.categoriesSpec(), access);
        break;
      case LIKED:
        result = new YouTubeListDB(YouTubeServiceRequest.likedSpec(), access);
        break;
      case RELATED:
        result = new YouTubeListDB(YouTubeServiceRequest.relatedSpec(relatedType, channelID), access);
        break;
      case SEARCH:
        result = new YouTubeListDB(YouTubeServiceRequest.searchSpec(query), access);
        break;
      case VIDEOS:
        result = new YouTubeListDB(YouTubeServiceRequest.videosSpec(playlistID), access);
        break;
    }

    return result;
  }

  // ===========================================================================
  // Adapter

  private class YouTubeListAdapter extends ArrayAdapter<YouTubeData> implements AdapterView.OnItemClickListener, VideoMenuView.VideoMenuViewListener {
    private final LayoutInflater inflater;
    int animationID = 0;
    boolean showHidden = false;

    public YouTubeListAdapter() {
      super(getActivity(), 0);

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

        YouTubeData map = getItem(position);
        handleClick(map);
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

      YouTubeData itemMap = getItem(position);

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

      mList.updateItem(videoMap);
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

