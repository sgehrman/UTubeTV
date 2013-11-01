package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
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
  private static final String ITEM_RES_ID = "res_id";
  private static final String LIST_TYPE = "list_type";
  private static final String CHANNEL_ID = "channel";
  private static final String RELATED_TYPE = "related";
  private static final String PLAYLIST_ID = "playlist";
  YouTubeListSpec.ListType listType;
  private MyAdapter mAdapter;
  private YouTubeList mList;
  private int itemResID = 0;

  public static YouTubeFragment newInstance(YouTubeListSpec.ListType listType, String channelID, String playlistID, YouTubeHelper.RelatedPlaylistType relatedType) {
    YouTubeFragment fragment = new YouTubeFragment();

    Bundle args = new Bundle();

    args.putSerializable(LIST_TYPE, listType);
    args.putSerializable(RELATED_TYPE, relatedType);
    args.putString(CHANNEL_ID, channelID);
    args.putString(PLAYLIST_ID, playlistID);

    // item resource id
    int resID = R.layout.youtube_list_item_large;
    switch (listType) {
      case PLAYLISTS:
      case CATEGORIES:
      case SUBSCRIPTIONS:
        resID = R.layout.youtube_list_item;
        break;
      case LIKED:
        break;
      case RELATED:
        break;
      case SEARCH:
        break;
      case VIDEOS:
        break;
    }
    args.putInt(ITEM_RES_ID, resID);

    fragment.setArguments(args);

    return fragment;
  }

  public String title() {
    return "Favorites";
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    boolean useGridView = true;

    View rootView = null;
    View listOrGridView = null;

    if (useGridView) {
      rootView = inflater.inflate(R.layout.fragment_youtube_grid, container, false);
      listOrGridView = rootView.findViewById(R.id.gridview);
    } else {
      rootView = inflater.inflate(R.layout.fragment_youtube_list, container, false);
      listOrGridView = rootView.findViewById(R.id.listview);
    }

    itemResID = getArguments().getInt(ITEM_RES_ID);

    listType = (YouTubeListSpec.ListType) getArguments().getSerializable(LIST_TYPE);

    String channelID = getArguments().getString(CHANNEL_ID);
    String playlistID = getArguments().getString(PLAYLIST_ID);
    YouTubeHelper.RelatedPlaylistType relatedType = (YouTubeHelper.RelatedPlaylistType) getArguments().getSerializable(RELATED_TYPE);
    mList = createListForIndex(channelID, playlistID, relatedType);

    mAdapter = new MyAdapter();

    if (listOrGridView instanceof ListView) {
      ListView lv = (ListView) listOrGridView;
      lv.setAdapter(mAdapter);

      lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          Map map = mAdapter.getItem(position);

          mList.handleClick(map, false);
        }
      });
    } else {
      GridView gv = (GridView) listOrGridView;
      gv.setAdapter(mAdapter);

      gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          Map map = mAdapter.getItem(position);

          mList.handleClick(map, false);
        }
      });
    }

    // Add the Refreshable View and provide the refresh listener;
    Util.PullToRefreshListener ptrl = (Util.PullToRefreshListener) getActivity();
    ptrl.addRefreshableView(listOrGridView, this);

    // load data if we have it already
    onResults();

    return rootView;
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

  private YouTubeList createListForIndex(String channelID, String playlistID, YouTubeHelper.RelatedPlaylistType relatedType) {
    YouTubeList result = null;

    UIAccess access = new UIAccess(this);

    final String keyPrefix = "list-" + channelID;
    result = null; // disabled: (YouTubeList) AppData.getInstance().getData(keyPrefix + tabIndex);
    if (result != null) {
      result.restart(access);
    } else {
      switch (listType) {
        case SUBSCRIPTIONS:
          result = new YouTubeList(YouTubeListSpec.subscriptionsSpec(), access);
          break;
        case PLAYLISTS:
          result = new YouTubeList(YouTubeListSpec.playlistsSpec(channelID), access);
          break;
        case CATEGORIES:
          result = new YouTubeList(YouTubeListSpec.categoriesSpec(), access);
          break;
        case LIKED:
          result = new YouTubeList(YouTubeListSpec.likedSpec(), access);
          break;
        case RELATED:
          result = new YouTubeList(YouTubeListSpec.relatedSpec(relatedType, channelID), access);
          break;
        case SEARCH:
          result = new YouTubeList(YouTubeListSpec.searchSpec("Keyboard cat"), access);
          break;
        case VIDEOS:
          result = new YouTubeList(YouTubeListSpec.videosSpec(playlistID), access);
          break;
      }

      // save in cache
//      AppData.getInstance().setData(keyPrefix + tabIndex, result);
    }

    return result;
  }

  // ===========================================================================
  // Adapter

  private class MyAdapter extends ArrayAdapter<Map> implements View.OnClickListener {
    public MyAdapter() {
      super(getActivity(), 0);
    }

    public void onClick(View v) {
      int position = v.getId();
      Map map = mAdapter.getItem(position);

      mList.handleClick(map, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = getActivity().getLayoutInflater().inflate(itemResID, null);

        ViewHolder holder = new ViewHolder();
        holder.button = (ImageView) convertView.findViewById(R.id.image);
        holder.title = (TextView) convertView.findViewById(R.id.text_view);
        holder.description = (TextView) convertView.findViewById(R.id.description_view);
        holder.pageNumber = (TextView) convertView.findViewById(R.id.page_number);
        holder.gradientOverlay = (View) convertView.findViewById(R.id.gradient_overlay);
        convertView.setTag(holder);
      }

      ViewHolder holder = (ViewHolder) convertView.getTag();

      Map itemMap = getItem(position);

      holder.button.setAnimation(null);

      UrlImageViewHelper.setUrlDrawable(holder.button, (String) itemMap.get("thumbnail"), 0, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          if (!loadedFromCache) {
//            ScaleAnimation scale = new ScaleAnimation(.6f, 1, .6f, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
            AlphaAnimation scale = new AlphaAnimation(0, 1);
            scale.setDuration(400);
//            scale.setInterpolator(new OvershootInterpolator());
            imageView.startAnimation(scale);
          }
        }

      });

      holder.button.setOnClickListener(this);
      holder.button.setId(position);

      holder.title.setText((String) itemMap.get("title"));

      String duration = (String) itemMap.get("duration");
      if (duration != null) {
        holder.pageNumber.setVisibility(View.VISIBLE);

        PeriodFormatter formatter = ISOPeriodFormat.standard();
        Period p = formatter.parsePeriod(duration);
        Seconds s = p.toStandardSeconds();

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

        holder.pageNumber.setText(df.format(s.getSeconds() * 1000));
      } else {
        holder.pageNumber.setVisibility(View.GONE);
      }

      // hide description if empty
      if (holder.description != null) {
        String desc = (String) itemMap.get("description");
        if (desc != null && (desc.length() > 0)) {
          holder.description.setVisibility(View.VISIBLE);

          // shorten description
          if (desc.length() > 100) {
            desc = desc.substring(0, Math.min(desc.length(), 100));
            desc += "...";
          }

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
      TextView pageNumber;
      ImageView button;
      View gradientOverlay;
    }
  }

}

