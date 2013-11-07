package com.sickboots.sickvideos;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class YouTubeFragment extends Fragment
    implements PullToRefreshAttacher.OnRefreshListener, UIAccess.UIAccessListener {
  private static final String SEARCH_QUERY = "query";
  private static final String LIST_TYPE = "type";
  private static final String CHANNEL_ID = "channel";
  private static final String RELATED_TYPE = "related";
  private static final String PLAYLIST_ID = "playlist";
  private YouTubeListSpec.ListType listType;
  private MyAdapter mAdapter;
  private YouTubeList mList;
  private int itemResID = 0;

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

      if (title != null)
        actionBar.setTitle(title);
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

    // Create a progress bar to display while the list loads
    ProgressBar progressBar = new ProgressBar(this.getActivity());
    progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    progressBar.setIndeterminate(true);
    listOrGridView.setEmptyView(progressBar);
    rootView.addView(progressBar);

    mAdapter = new MyAdapter();

    listOrGridView.setAdapter(mAdapter);
    listOrGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Map map = mAdapter.getItem(position);

//        mList.handleClick(map, false);
        handleClick(map, false);

      }
    });

    // .015 is the default
    listOrGridView.setFriction(0.01f);

    // Add the Refreshable View and provide the refresh listener;
    Util.PullToRefreshListener ptrl = (Util.PullToRefreshListener) getActivity();
    ptrl.addRefreshableView(listOrGridView, this);

    setupSlideInPlayerView(rootView);

    // load data if we have it already
    onResults();
    setActionBarTitle();

    return rootView;
  }

  public void handleClick(Map itemMap, boolean clickedIcon) {
    String videoId = (String) itemMap.get("video");

    VideoPlayerFragment videoFragment =
        (VideoPlayerFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);
    videoFragment.setVideoId(videoId);

    // The videoBox is INVISIBLE if no video was previously selected, so we need to show it now.
    if (videoBox.getVisibility() != View.VISIBLE) {
      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Initially translate off the screen so that it can be animated in from below.
        videoBox.setTranslationY(videoBox.getHeight());
      }
      videoBox.setVisibility(View.VISIBLE);
    }

    // If the fragment is off the screen, we animate it in.
    if (videoBox.getTranslationY() > 0) {
      videoBox.animate().translationY(0).setDuration(300);
    }
  }

  private void setupSlideInPlayerView(View rootView) {
    // video player
    videoBox = rootView.findViewById(R.id.slide_in_player_box);
    videoBox.setVisibility(View.INVISIBLE);

    ImageButton b = (ImageButton) rootView.findViewById(R.id.close_button);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        videoBox.animate()
            .translationYBy(videoBox.getHeight())
            .setDuration(300)
            .withEndAction(new Runnable() {
              @Override
              public void run() {
                videoBox.setVisibility(View.INVISIBLE);
              }
            });
      }

    });
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

  private class MyAdapter extends ArrayAdapter<Map> {
    private final LayoutInflater inflater;

    public MyAdapter() {
      super(getActivity(), 0);

      inflater = LayoutInflater.from(getActivity());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = inflater.inflate(itemResourceID(), null);

        ViewHolder holder = new ViewHolder();
        holder.image = (ImageView) convertView.findViewById(R.id.image);
        holder.title = (TextView) convertView.findViewById(R.id.text_view);
        holder.description = (TextView) convertView.findViewById(R.id.description_view);
        holder.duration = (TextView) convertView.findViewById(R.id.duration);
        holder.gradientOverlay = (View) convertView.findViewById(R.id.gradient_overlay);
        convertView.setTag(holder);
      }

      ViewHolder holder = (ViewHolder) convertView.getTag();

      Map itemMap = getItem(position);

      holder.image.setAnimation(null);

      int defaultImageResID = 0; // looks better black or try this: R.drawable.loading_thumbnail;
      UrlImageViewHelper.setUrlDrawable(holder.image, (String) itemMap.get(YouTubeAPI.THUMBNAIL_KEY), defaultImageResID, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          if (!loadedFromCache) {
//            ScaleAnimation scale = new ScaleAnimation(.6f, 1, .6f, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
            AlphaAnimation scale = new AlphaAnimation(0, 1);
            scale.setDuration(300);
//            scale.setInterpolator(new OvershootInterpolator());
            imageView.startAnimation(scale);
          }
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
        if ((position % 2) == 0) {
          holder.gradientOverlay.setBackgroundResource(R.drawable.blue_gradient);
        } else {
          holder.gradientOverlay.setBackgroundResource(R.drawable.black_gradient);
        }
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

