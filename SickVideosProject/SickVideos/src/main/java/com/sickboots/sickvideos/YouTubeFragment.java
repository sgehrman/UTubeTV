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
  private int mType;
  private static final String TAB_INDEX = "index";
  private static final String CHANNEL_ID = "channel";
  private YouTubeList mList;

  public static YouTubeFragment newInstance(int type, String channelID) {
    YouTubeFragment fragment = new YouTubeFragment();

    Bundle args = new Bundle();
    args.putInt(TAB_INDEX, type);
    args.putString(CHANNEL_ID, channelID);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_youtube_list, container, false);

    ListView listView = (ListView) rootView.findViewById(R.id.listview);

    int tabIndex = getArguments().getInt(TAB_INDEX);
    String channelID = getArguments().getString(CHANNEL_ID);
    mList = createListForIndex(tabIndex, channelID);

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
    mAdapter.addAll(mList.getItems());
  }

  private YouTubeList createListForIndex(int tabIndex, String channelID) {
    YouTubeList result = null;

    UIAccess access = new UIAccess(this, tabIndex);

    final String keyPrefix = "list-";
    result = (YouTubeList) AppData.getInstance().getData(keyPrefix + tabIndex);
    if (result != null) {
      result.restart(access);
    } else {
      switch (tabIndex) {
        case 0:
          result = new YouTubeList(YouTubeListSpec.relatedSpec(YouTubeHelper.RelatedPlaylistType.FAVORITES, channelID), access);
          break;
        case 1:
          result = new YouTubeList(YouTubeListSpec.searchSpec("Keyboard cat"), access);
          break;
        case 2:
//          result = new YouTubeList(YouTubeListSpec.subscriptionsSpec(), access);
          result = new YouTubeList(YouTubeListSpec.categoriesSpec(), access);
          break;
      }

      // save in cache
      AppData.getInstance().setData(keyPrefix + tabIndex, result);
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
      TextView text;
      ImageButton button;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      if (convertView == null) {
        int resID = R.layout.youtube_list_item_large;
//        if (true) resID = R.layout.youtube_list_item;

        convertView = getActivity().getLayoutInflater().inflate(resID, null);

        ViewHolder holder = new ViewHolder();
        holder.button = (ImageButton) convertView.findViewById(R.id.image);
        holder.text = (TextView) convertView.findViewById(R.id.text_view);
        convertView.setTag(holder);
      }

      ViewHolder holder = (ViewHolder) convertView.getTag();

      holder.button.setAnimation(null);

      UrlImageViewHelper.setUrlDrawable(holder.button, (String) getItem(position).get("thumbnail"), android.R.drawable.ic_input_get, new UrlImageViewCallback() {

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

      holder.text.setText((String) getItem(position).get("title"));

      // load more data if at the end
      mList.updateHighestDisplayedIndex(position);

      return convertView;
    }
  }

  // ===========================================================================

}

