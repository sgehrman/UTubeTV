package com.sickboots.sickvideos;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class YouTubeFragment extends Fragment
    implements Observer, PullToRefreshAttacher.OnRefreshListener, UIAccess.UIAccessListener {
  private static final String SEARCH_QUERY = "query";
  private static final String LIST_TYPE = "type";
  private static final String CHANNEL_ID = "channel";
  private static final String RELATED_TYPE = "related";
  private static final String PLAYLIST_ID = "playlist";
  private YouTubeListSpec.ListType listType;
  private MyAdapter mAdapter;
  private YouTubeList mList;
  private int itemResID = 0;
  private ScrollTriggeredAnimator mScrollAnimator;

  // video player
  private View videoBox;

  public static YouTubeFragment relatedFragment(YouTubeAPI.RelatedPlaylistType relatedType) {
    return newInstance(YouTubeListSpec.ListType.RELATED, null, null, relatedType, null);
  }

  public static YouTubeFragment videosFragment(String playlistID) {
    return newInstance(YouTubeListSpec.ListType.VIDEOS, null, playlistID, null, null);
  }

  public static YouTubeFragment playlistsFragment(String channelID) {
    return newInstance(YouTubeListSpec.ListType.PLAYLISTS, channelID, null, null, null);
  }

  public static YouTubeFragment likedFragment() {
    return newInstance(YouTubeListSpec.ListType.LIKED, null, null, null, null);
  }

  public static YouTubeFragment subscriptionsFragment() {
    return newInstance(YouTubeListSpec.ListType.SUBSCRIPTIONS, null, null, null, null);
  }

  public static YouTubeFragment searchFragment(String searchQuery) {
    return newInstance(YouTubeListSpec.ListType.SEARCH, null, null, null, searchQuery);
  }

  private static YouTubeFragment newInstance(YouTubeListSpec.ListType listType, String channelID, String playlistID, YouTubeAPI.RelatedPlaylistType relatedType, String searchQuery) {
    YouTubeFragment fragment = new YouTubeFragment();

    Bundle args = new Bundle();

    args.putSerializable(LIST_TYPE, listType);
    args.putSerializable(RELATED_TYPE, relatedType);
    args.putString(CHANNEL_ID, channelID);
    args.putString(PLAYLIST_ID, playlistID);
    args.putString(SEARCH_QUERY, searchQuery);

    fragment.setArguments(args);

    return fragment;
  }

  private void setActionBarTitle() {
    ActionBar actionBar = getActivity().getActionBar();

    // not every host activity has an ActionBar
    if (actionBar != null) {
      CharSequence title = null;
      if (mList != null)
        title = mList.name();

      // if video player is up, show the video title
      if (videoPlayerIsVisible()) {
        VideoPlayerFragment videoFragment = (VideoPlayerFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);
        title = videoFragment.getTitle();
      }

      if (title != null)
        actionBar.setTitle(title);
    }
  }

  // Observer
  @Override
  public void update(Observable observable, Object data) {
    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(ApplicationHub.BACK_BUTTON_NOTIFICATION)) {
        // if the video player is visible, close it
        closeVideoPlayer();
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    listType = (YouTubeListSpec.ListType) getArguments().getSerializable(LIST_TYPE);

    ViewGroup rootView = null;
    AbsListView listOrGridView = null;

    if (useGridView(getArguments())) {
      rootView = (ViewGroup) inflater.inflate(R.layout.fragment_youtube_grid, container, false);
      listOrGridView = (AbsListView) rootView.findViewById(R.id.gridview);
    } else {
      rootView = (ViewGroup) inflater.inflate(R.layout.fragment_youtube_list, container, false);
      listOrGridView = (AbsListView) rootView.findViewById(R.id.listview);
    }

    mList = createList(getArguments());

    View emptyView = Util.emptyListView(getActivity(), "Talking to YouTube...");
    listOrGridView.setEmptyView(emptyView);
    rootView.addView(emptyView);

    mAdapter = new MyAdapter();

    listOrGridView.setAdapter(mAdapter);
    listOrGridView.setOnItemClickListener(mAdapter);

    // .015 is the default
    listOrGridView.setFriction(0.01f);

    // Add the Refreshable View and provide the refresh listener;
    Util.PullToRefreshListener ptrl = (Util.PullToRefreshListener) getActivity();
    ptrl.addRefreshableView(listOrGridView, this);

    setupSlideInPlayerView(savedInstanceState, rootView);

    // load data if we have it already
    onResults();
    setActionBarTitle();

    View dimmerView = rootView.findViewById(R.id.dimmer);
    mScrollAnimator = new ScrollTriggeredAnimator(listOrGridView, dimmerView);

    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();

    // for back button notification
    ApplicationHub.instance().addObserver(this);
  }

  @Override
  public void onStop() {
    super.onStop();

    // for back button notification
    ApplicationHub.instance().deleteObserver(this);
  }

  public void handleClick(Map itemMap) {
    String videoId = (String) itemMap.get("video");
    String title = (String) itemMap.get("title");

    VideoPlayerFragment videoFragment = (VideoPlayerFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);
    videoFragment.setVideo(videoId, title);

    // The videoBox is INVISIBLE if no video was previously selected, so we need to show it now.
    if (!videoPlayerIsVisible()) {
      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Initially translate off the screen so that it can be animated in from below.
        videoBox.setTranslationY(-videoBox.getHeight());
      }
      videoBox.setVisibility(View.VISIBLE);
    }

    // If the fragment is off the screen, we animate it in.
    if (videoBox.getTranslationY() < 0) {
      videoBox.animate().translationY(0).setDuration(300);
    }
  }

  private boolean videoPlayerIsVisible() {
    return (videoBox.getVisibility() == View.VISIBLE);
  }

  private void setupSlideInPlayerView(Bundle savedInstanceState, View rootView) {
    // Don't add the fragment if restoring, it's already set up
    if (savedInstanceState == null) {
      // had to add this manually rather than setting the class in xml to avoid duplicate id errors
      Fragment fragment = new VideoPlayerFragment();
      FragmentManager fm = getFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      ft.replace(R.id.video_fragment_container, fragment);
      ft.commit();
    }

    // video player
    videoBox = rootView.findViewById(R.id.slide_in_player_box);

    videoBox.setVisibility(View.INVISIBLE);

    // close button
    ImageButton b = (ImageButton) rootView.findViewById(R.id.close_button);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        closeVideoPlayer();
      }
    });

    // Mute button
    b = (ImageButton) rootView.findViewById(R.id.mute_button);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        videoFragment().mute(!videoFragment().isMute());
      }

      ;
    });

    // Full screen button
    b = (ImageButton) rootView.findViewById(R.id.full_screen_button);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        videoFragment().setFullscreen(true);
      }

      ;
    });

    // Skip ahead button
    b = (ImageButton) rootView.findViewById(R.id.skip_ahead_button);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        videoFragment().seekRelativeSeconds(10);
      }

      ;
    });

  }

  private void closeVideoPlayer() {
    if (videoPlayerIsVisible()) {
      // pause immediately on click for better UX
      videoFragment().pause();

      videoBox.animate()
          .translationYBy(-videoBox.getHeight())
          .setDuration(300)
          .withEndAction(new Runnable() {
            @Override
            public void run() {
              videoBox.setVisibility(View.INVISIBLE);

              // set the activity's title back
              setActionBarTitle();
            }
          });
    }
  }

  private VideoPlayerFragment videoFragment() {
    VideoPlayerFragment videoFragment = (VideoPlayerFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);

    return videoFragment;
  }

  private int itemResourceID() {
    if (itemResID == 0) {
      itemResID = R.layout.youtube_list_item_large;
      switch (listType) {
        case PLAYLISTS:
        case CATEGORIES:
        case SUBSCRIPTIONS:
          itemResID = R.layout.youtube_list_item;
          break;
        case LIKED:
        case RELATED:
        case SEARCH:
        case VIDEOS:
          break;
      }
    }

    return itemResID;
  }

  private boolean useGridView(Bundle argsBundle) {
    switch (listType) {
      case PLAYLISTS:
      case CATEGORIES:
      case SUBSCRIPTIONS:
        return false;
      case LIKED:
      case RELATED:
      case SEARCH:
      case VIDEOS:
        break;
    }

    return true;
  }

  public void onRefreshStarted(View view) {
    new android.os.Handler().postDelayed(
        new Runnable() {
          public void run() {
            mList.refresh();

            Util.PullToRefreshListener ptrl = (Util.PullToRefreshListener) getActivity();
            ptrl.setRefreshComplete();
          }
        }, 2000);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    boolean handled = mList.handleActivityResult(requestCode, requestCode, data);

    if (!handled) {
      switch (requestCode) {
      }
    }
  }

  @Override
  public void onResults() {
    mAdapter.clear();

    List items = mList.getItems();

    if (items != null) {
      mAdapter.addAll(items);
    }
  }

  private YouTubeList createList(Bundle argsBundle) {
    YouTubeList result = null;

    String channelID = argsBundle.getString(CHANNEL_ID);
    String playlistID = argsBundle.getString(PLAYLIST_ID);
    String query = argsBundle.getString(SEARCH_QUERY);
    YouTubeAPI.RelatedPlaylistType relatedType = (YouTubeAPI.RelatedPlaylistType) argsBundle.getSerializable(RELATED_TYPE);

    UIAccess access = new UIAccess(this);

    switch (listType) {
      case SUBSCRIPTIONS:
        result = new YouTubeListDB(YouTubeListSpec.subscriptionsSpec(), access);
        break;
      case PLAYLISTS:
        result = new YouTubeListDB(YouTubeListSpec.playlistsSpec(channelID), access);
        break;
      case CATEGORIES:
        result = new YouTubeListLive(YouTubeListSpec.categoriesSpec(), access);
        break;
      case LIKED:
        result = new YouTubeListLive(YouTubeListSpec.likedSpec(), access);
        break;
      case RELATED:
        result = new YouTubeListDB(YouTubeListSpec.relatedSpec(relatedType, channelID), access);
        break;
      case SEARCH:
        result = new YouTubeListLive(YouTubeListSpec.searchSpec(query), access);
        break;
      case VIDEOS:
        result = new YouTubeListLive(YouTubeListSpec.videosSpec(playlistID), access);
        break;
    }

    return result;
  }

  // ===========================================================================
  // Adapter

  private class MyAdapter extends ArrayAdapter<Map> implements AdapterView.OnItemClickListener {
    private final LayoutInflater inflater;

    public MyAdapter() {
      super(getActivity(), 0);

      inflater = LayoutInflater.from(getActivity());
    }

    private void animateViewForClick(final View theView) {
      if (theView != null) {
        theView.animate().alpha(0).setDuration(500).scaleX(.5f).scaleY(.5f).withEndAction(new Runnable() {
          public void run() {

            theView.setScaleX(1.0f);
            theView.setScaleY(1.0f);

            theView.animate().setDuration(500).alpha(1).withEndAction(new Runnable() {
              public void run() {

              }
            });
          }
        });
      }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        ViewHolder holder = (ViewHolder) v.getTag();

      if (holder != null) {
        animateViewForClick(holder.image);

        Map map = getItem(position);
        handleClick(map);
      } else {
        Util.log("no holder on click?");
      }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;

      if (convertView == null) {
        convertView = inflater.inflate(itemResourceID(), null);

        holder = new ViewHolder();
        holder.image = (ImageView) convertView.findViewById(R.id.image);
        holder.title = (TextView) convertView.findViewById(R.id.text_view);
        holder.description = (TextView) convertView.findViewById(R.id.description_view);
        holder.duration = (TextView) convertView.findViewById(R.id.duration);
        holder.gradientOverlay = (View) convertView.findViewById(R.id.gradient_overlay);
        convertView.setTag(holder);
      }
      else {
        holder = (ViewHolder) convertView.getTag();

        // reset some stuff that might have been set on an animation (not sure if this is needed)
        holder.image.setAlpha(1.0f);
        holder.image.setScaleX(1.0f);
        holder.image.setScaleY(1.0f);
      }

      Map itemMap = getItem(position);

      holder.image.setAnimation(null);

      int defaultImageResID = 0;

      UrlImageViewHelper.setUrlDrawable(holder.image, (String) itemMap.get(YouTubeAPI.THUMBNAIL_KEY), defaultImageResID, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          float imageAlpha = .6f;
          if (!loadedFromCache) {

            imageView.setAlpha(0.15f);
            imageView.animate().setDuration(600).alpha(imageAlpha);
          } else
            imageView.setAlpha(imageAlpha);
        }

      });

      holder.title.setText((String) itemMap.get(YouTubeAPI.TITLE_KEY));

      String duration = (String) itemMap.get(YouTubeAPI.DURATION_KEY);
      if (duration != null) {
        holder.duration.setVisibility(View.VISIBLE);

        PeriodFormatter formatter = ISOPeriodFormat.standard();
        Period p = formatter.parsePeriod(duration);
        Seconds s = p.toStandardSeconds();

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

        holder.duration.setText(df.format(s.getSeconds() * 1000));
      } else {
        holder.duration.setVisibility(View.GONE);
      }

      // hide description if empty
      if (holder.description != null) {
        String desc = (String) itemMap.get(YouTubeAPI.DESCRIPTION_KEY);
        if (desc != null && (desc.length() > 0)) {
          holder.description.setVisibility(View.VISIBLE);
          holder.description.setText(desc);
        } else {
          holder.description.setVisibility(View.GONE);
        }
      }

      if (holder.gradientOverlay != null) {
//        if ((position % 2) == 0) {
//          holder.gradientOverlay.setBackgroundResource(R.drawable.blue_gradient);
//        } else {
//          holder.gradientOverlay.setBackgroundResource(R.drawable.black_gradient);
//        }
      }

      // load more data if at the end
      mList.updateHighestDisplayedIndex(position);

      return convertView;
    }

    class ViewHolder {
      TextView title;
      TextView description;
      TextView duration;
      ImageView image;
      View gradientOverlay;
    }
  }

}

