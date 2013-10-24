package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.Context;
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

  public interface YouTubeListProvider {
    public YouTubeListProvider start(YouTubeListSpec s, UIAccess a);
    public void restart(UIAccess a);
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data);
    public void refresh();
    public void moreData();
    public void handleClick(Map itemMap, boolean clickedIcon);
    public List<Map>getItems();
  }

  private MyAdapter mAdapter;
  private int mType;
  private static final String ARG_TYPE_NUMBER = "TYPENUMber";
  private YouTubeListProvider mList;

  public static YouTubeFragment newInstance(int type) {
    YouTubeFragment fragment = new YouTubeFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_TYPE_NUMBER, type);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_youtube_list, container, false);

    ListView listView = (ListView) rootView.findViewById(R.id.listview);

    int tabIndex = getArguments().getInt(ARG_TYPE_NUMBER);
    UIAccess access = new UIAccess(this, tabIndex);

    final String keyPrefix = "list-";
    mList = (YouTubeListProvider) YouTubeListCache.getInstance().getData(keyPrefix + tabIndex);
    if (mList != null) {
      mList.restart(access);
    } else {
      switch (tabIndex) {
        case 0:
          mList = new YouTubeList().start(YouTubeListSpec.relatedSpec(YouTubeHelper.RelatedPlaylistType.FAVORITES), access);
          break;
        case 1:
          mList = new YouTubeList().start(YouTubeListSpec.searchSpec("Pigmies"), access);
          break;
        case 2:
          mList = new YouTubeList().start(YouTubeListSpec.subscriptionsSpec(), access);
          break;
      }

      // save in cache
      YouTubeListCache.getInstance().setData(keyPrefix + tabIndex, mList);
    }

    mAdapter = new MyAdapter();
    listView.setAdapter(mAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
      Map map = mAdapter.getItem(position);

      mList.handleClick(map, false);
      }
    });

    // Add the Refreshable View and provide the refresh listener;
    ((MainActivity)getActivity()).mPullToRefreshAttacher.addRefreshableView(listView, this);

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

      if (convertView == null)
      {
        convertView = getActivity().getLayoutInflater().inflate(R.layout.youtube_list_item_large, null);

        ViewHolder holder = new ViewHolder();
        holder.button = (ImageButton) convertView.findViewById(R.id.image);
        holder.text = (TextView) convertView.findViewById(R.id.text_view);
        convertView.setTag(holder);
      }

      ViewHolder holder = (ViewHolder) convertView.getTag();

      holder.button.setAnimation(null);

      // yep, that's it. it handles the downloading and showing an interstitial image automagically.
      UrlImageViewHelper.setUrlDrawable(holder.button, (String)getItem(position).get("thumbnail"), android.R.drawable.ic_input_get, new UrlImageViewCallback() {

        @Override
        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
          if (!loadedFromCache) {
            ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
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
      if (position == (getCount()-1)) {
        mList.moreData();
      }

      return convertView;
    }
  }

  // ===========================================================================

}

