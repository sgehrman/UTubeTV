package com.distantfuture.videos.channellookup;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.distantfuture.videos.channellookup.ChannelLookupAdapter;
import com.distantfuture.videos.channellookup.ChannelLookupItemLoader;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.Debug;

import java.util.List;

public class ChannelLookupListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<YouTubeData>> {
  private ChannelLookupAdapter mAdapter;
  private String query;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    getListView().setFastScrollEnabled(true);

    mAdapter = new ChannelLookupAdapter(getActivity());
    setEmptyText("Empty");
    setListAdapter(mAdapter);
    setListShown(false);
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public void onLoadFinished(Loader<List<YouTubeData>> arg0, List<YouTubeData> data) {
    mAdapter.setData(data);

    ChannelLookupActivity activity = (ChannelLookupActivity) getActivity();
    activity.adapterDataChanged();

    if (isResumed()) {
      setListShown(true);
    } else {
      setListShownNoAnimation(true);
    }
  }

  public int getCount() {
    return mAdapter.getCount();
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    if (!TextUtils.equals(this.query, query)) {
      this.query = query;
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override
  public void onLoaderReset(Loader<List<YouTubeData>> arg0) {
    mAdapter.setData(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    YouTubeData selectedMedia = mAdapter.getItem(position);
    handleNavigation(selectedMedia, false);
  }

  private void handleNavigation(YouTubeData info, boolean autoStart) {
    Debug.log(info.mChannel);
  }

  @Override
  public Loader<List<YouTubeData>> onCreateLoader(int arg0, Bundle arg1) {
    return new ChannelLookupItemLoader(getActivity(), query);
  }
}


