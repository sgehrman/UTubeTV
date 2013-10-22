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

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.services.youtube.YouTubeScopes;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;
import java.util.Map;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class YouTubeFragment extends Fragment
    implements PullToRefreshAttacher.OnRefreshListener, Util.ListResultListener {

  public interface YouTubeListProvider {
    public YouTubeListProvider start(Util.ListResultListener l);  // could I make a generic constructor?
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data);
  }

  private MyAdapter mAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_youtube_list, container, false);

    ListView listView = (ListView) rootView.findViewById(R.id.listview);

    mAdapter = new MyAdapter(getActivity(), 1);
    listView.setAdapter(mAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        String movieID = (String) mAdapter.getItem(position).get("video");

        Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), YouTubeHelper.DEVELOPER_KEY, movieID, 0, true, true);
        startActivity(intent);
      }
    });

    // Add the Refreshable View and provide the refresh listener;
    ((MainActivity)getActivity()).mPullToRefreshAttacher.addRefreshableView(listView, this);

    String scope = "oauth2:" + YouTubeScopes.YOUTUBE_READONLY;

    return rootView;
  }

  public void onRefreshStarted(View view) {
    new android.os.Handler().postDelayed(
        new Runnable() {
          public void run() {
            ((MainActivity) getActivity()).mPullToRefreshAttacher.setRefreshComplete();
          }
        }, 2000);
  }

  private class MyAdapter extends ArrayAdapter<Map> implements View.OnClickListener {
    private YouTubeListProvider mSearch;

    public void onClick(View v) {
      int position = v.getId();

      String movieID = (String) getItem(position).get("video");

      Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), YouTubeHelper.DEVELOPER_KEY, movieID, 0, true, true);
      startActivity(intent);
    }

    public MyAdapter(Context context, int type) {
      super(context, 0);

      switch (type) {
        case 0:
          mSearch = new YouTubePlaylist().start(com.sickboots.sickvideos.YouTubeFragment.this);

          break;
        case 1:
          mSearch = new YouTubeSearch().start(com.sickboots.sickvideos.YouTubeFragment.this);

          break;
      }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
      boolean handled = mSearch.handleActivityResult(requestCode, requestCode, data);

      if (!handled) {
        switch (requestCode) {
        }
      }

      return handled;
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

      return convertView;
    }

    public void handleResults(List<Map> result) {
      for (Map map : result) {
        add(map);
      }

    }

  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    boolean handled = mAdapter.handleActivityResult(requestCode, requestCode, data);

    if (!handled) {
      switch (requestCode) {
      }
    }
  }

  @Override
  public void onResults(Util.ListResultListener search, List<Map> result) {
    mAdapter.handleResults(result);
  }

}

