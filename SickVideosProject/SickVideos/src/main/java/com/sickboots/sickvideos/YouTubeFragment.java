package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;
import java.util.Map;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class YouTubeFragment extends Fragment
    implements PullToRefreshAttacher.OnRefreshListener, UIAccess.UIAccessListener {
  private MyAdapter mAdapter;
  private static final String ITEM_RES_ID = "res_id";
  private static final String LIST_TYPE = "list_type";
  private static final String CHANNEL_ID = "channel";
  private static final String RELATED_TYPE = "related";
  private static final String PLAYLIST_ID = "playlist";
  private YouTubeList mList;
  private int itemResID = 0;
  YouTubeListSpec.ListType listType;

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

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_youtube_list, container, false);

    ListView listView = (ListView) rootView.findViewById(R.id.listview);

    itemResID = getArguments().getInt(ITEM_RES_ID);

    listType = (YouTubeListSpec.ListType) getArguments().getSerializable(LIST_TYPE);

    String channelID = getArguments().getString(CHANNEL_ID);
    String playlistID = getArguments().getString(PLAYLIST_ID);
    YouTubeHelper.RelatedPlaylistType relatedType = (YouTubeHelper.RelatedPlaylistType) getArguments().getSerializable(RELATED_TYPE);
    mList = createListForIndex(channelID, playlistID, relatedType);

    mAdapter = new MyAdapter();
    listView.setAdapter(mAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Map map = mAdapter.getItem(position);

        mList.handleClick(map, false);
      }
    });

    // Add the Refreshable View and provide the refresh listener;
    ((MainActivity) getActivity()).mPullToRefreshAttacher.addRefreshableView(listView, this);

    // load data if we have it already
    onResults();

    return rootView;
  }

  public void onRefreshStarted(View view) {
    new android.os.Handler().postDelayed(
        new Runnable() {
          public void run() {
            mList.refresh();

            ((MainActivity) getActivity()).mPullToRefreshAttacher.setRefreshComplete();
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

    if (items != null)
      mAdapter.addAll(items);
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
    public void onClick(View v) {
      int position = v.getId();
      Map map = mAdapter.getItem(position);

      mList.handleClick(map, true);
    }

    public MyAdapter() {
      super(getActivity(), 0);
    }

    class ViewHolder {
      TextView title;
      TextView description;
      TextView pageNumber;
      ImageButton button;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      if (convertView == null) {
        convertView = getActivity().getLayoutInflater().inflate(itemResID, null);

        ViewHolder holder = new ViewHolder();
        holder.button = (ImageButton) convertView.findViewById(R.id.image);
        holder.title = (TextView) convertView.findViewById(R.id.text_view);
        holder.description = (TextView) convertView.findViewById(R.id.description_view);
        holder.pageNumber = (TextView) convertView.findViewById(R.id.page_number);
        convertView.setTag(holder);
      }

      ViewHolder holder = (ViewHolder) convertView.getTag();

      holder.button.setAnimation(null);

      UrlImageViewHelper.setUrlDrawable(holder.button, (String) getItem(position).get("thumbnail"), android.R.drawable.ic_media_play, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          if (!loadedFromCache) {
            ScaleAnimation scale = new ScaleAnimation(.6f, 1, .6f, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
            scale.setDuration(300);
            scale.setInterpolator(new OvershootInterpolator());
            imageView.startAnimation(scale);
          }
        }

      });

      holder.button.setOnClickListener(this);
      holder.button.setId(position);

      holder.title.setText((String) getItem(position).get("title"));
      holder.pageNumber.setText(Integer.toString(position + 1));

      // hide description if empty
      if (holder.description != null) {
        String desc = (String) getItem(position).get("description");
        if (desc != null && (desc.length() > 0)) {
          holder.description.setVisibility(View.VISIBLE);

          holder.description.setText(desc);
        } else {
          holder.description.setVisibility(View.GONE);
        }
      }

      // load more data if at the end
      mList.updateHighestDisplayedIndex(position);

      return convertView;
    }
  }

  // ===========================================================================

}

