package com.sickboots.sickvideos.mainactivity;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.content.Content;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.imageutils.BitmapLoader;
import com.sickboots.sickvideos.misc.EmptyListHelper;
import com.sickboots.sickvideos.misc.Events;

import java.util.Observable;
import java.util.Observer;

import de.greenrobot.event.EventBus;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ChannelAboutFragment extends Fragment implements OnRefreshListener {
  private TextView mTitle;
  private TextView mDescription;
  private ImageView mImage;
  private Content mContent;
  private PullToRefreshLayout mPullToRefreshLayout;
  private EmptyListHelper mEmptyListHelper;
  private View mContentView;
  private BitmapLoader mBitmapLoader;

  // can't add params! fragments can be recreated randomly
  public ChannelAboutFragment() {
    super();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_channel_about, container, false);

    EventBus.getDefault().register(this);

    mContent = Content.instance();
    mBitmapLoader = new BitmapLoader(getActivity(), "aboutBitmaps", 0, new BitmapLoader.GetBitmapCallback() {
      @Override
      public void onLoaded(Bitmap bitmap) {
        // put in to prevent an endless loop if the thumbnail fails to load the first time
        if (bitmap != null)
          ChannelAboutFragment.this.updateUI();
      }
    });

    mTitle = (TextView) rootView.findViewById(R.id.text_view);
    mDescription = (TextView) rootView.findViewById(R.id.description_view);
    mImage = (ImageView) rootView.findViewById(R.id.image);
    mContentView = rootView.findViewById(R.id.content_view);

    Button button = (Button) rootView.findViewById(R.id.watch_button);
    LinearLayout card = (LinearLayout) rootView.findViewById(R.id.card);

    card.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showPlaylistsFragment();
      }
    });
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showPlaylistsFragment();
      }
    });

    // setup empty view
    mEmptyListHelper = new EmptyListHelper(rootView.findViewById(R.id.empty_view));
    mEmptyListHelper.updateEmptyListView("Talking to YouTube...", false);

    // Now find the PullToRefreshLayout to setup
    mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.about_frame_layout);

    // Now setup the PullToRefreshLayout
    ActionBarPullToRefresh.from(this.getActivity())
        // Mark All Children as pullable
        .allChildrenArePullable()
            // Set the OnRefreshListener
        .listener(this)
            // Finally commit the setup to our PullToRefreshLayout
        .setup(mPullToRefreshLayout);

    mContentView.setVisibility(View.GONE);
    updateUI();

    return rootView;
  }

  // OnRefreshListener
  @Override
  public void onRefreshStarted(View view) {
    // empty cache
    mBitmapLoader.refresh();
    mContent.refreshChannelInfo();
  }

  private void showPlaylistsFragment() {
    DrawerActivitySupport provider = (DrawerActivitySupport) getActivity();

    provider.showPlaylistsFragment();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    EventBus.getDefault().unregister(this);
  }

  // eventbus event
  public void onEvent(Events.ContentEvent event) {
    updateUI();
    mPullToRefreshLayout.setRefreshComplete();
  }

  private void updateUI() {
    final YouTubeData data = mContent.channelInfo();

    // if data == null, we'll wait for the eventbus event to arrive
    if (data != null) {
      mEmptyListHelper.view().setVisibility(View.GONE);
      mContentView.setVisibility(View.VISIBLE);

      mTitle.setText(data.mTitle);
      mDescription.setText(data.mDescription);

      // uncomment to get the thumbnail image for generating icons
      // Debug.log(data.mThumbnail);

      Bitmap bitmap = mBitmapLoader.bitmap(data);
      if (bitmap != null)
        mImage.setImageBitmap(bitmap);
      else {
        mBitmapLoader.requestBitmap(data);
      }

      DrawerActivitySupport provider = (DrawerActivitySupport) getActivity();
      provider.setActionBarTitle(data.mTitle, "About");
    }
  }
}
