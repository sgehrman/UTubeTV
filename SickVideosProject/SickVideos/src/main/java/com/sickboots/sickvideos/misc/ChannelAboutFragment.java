package com.sickboots.sickvideos.misc;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.Content;
import com.sickboots.sickvideos.DrawerActivitySupport;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.database.YouTubeData;

import java.util.Observable;
import java.util.Observer;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ChannelAboutFragment extends Fragment implements Observer, OnRefreshListener {
  private final long diskCacheSize = 10 * 1024 * 1024;  // 10mb
  private TextView mTitle;
  private TextView mDescription;
  private ImageView mImage;
  private Content mContent;
  private PullToRefreshLayout mPullToRefreshLayout;
  private EmptyListHelper mEmptyListHelper;
  private View mContentView;
  private BitmapCache mBitmapCache;
  private final String mAboutBitmapKey = "about";  // keys must match regex [a-z0-9_-]{1,64}

  // can't add params! fragments can be recreated randomly
  public ChannelAboutFragment() {
    super();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_channel_about, container, false);

    mContent = ((DrawerActivitySupport) getActivity()).getContent();

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
    if (mBitmapCache != null) {
      mBitmapCache.clearCache();

      // cache is not closed and useless, must be reopened
      mBitmapCache = null;
    }

    mContent.addObserver(this);
    mContent.refreshChannelInfo();
  }

  private void showPlaylistsFragment() {
    DrawerActivitySupport provider = (DrawerActivitySupport) getActivity();

    provider.showPlaylistsFragment();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // make sure we are not going to be notified after we are gone.
    // our activity will be null and we crash
    mContent.deleteObserver(this);
  }

  @Override
  public void update(Observable observable, Object data) {

    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(Content.CONTENT_UPDATED_NOTIFICATION)) {

        updateUI();
        mPullToRefreshLayout.setRefreshComplete();

        // only need this called once
        mContent.deleteObserver(this);
      }
    }
  }

  private void updateUI() {
    YouTubeData data = mContent.channelInfo();
    if (data == null) {
      mContent.addObserver(this);
    } else {
      mEmptyListHelper.view().setVisibility(View.GONE);
      mContentView.setVisibility(View.VISIBLE);

      mTitle.setText(data.mTitle);
      mDescription.setText(data.mDescription);

      // uncomment to get the thumbnail image for generating icons
      // Debug.log(data.mThumbnail);

      // is the bitmap in our diskcache?
      Bitmap bm = cachedBitmap();

      if (bm != null) {
        mImage.setImageBitmap(bm);
      } else {
        int defaultImageResID = 0;
        final ImageView image = mImage;
        UrlImageViewHelper.setUrlDrawable(image, data.mThumbnail, defaultImageResID, new UrlImageViewCallback() {

          @Override
          public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
            if (!loadedFromCache) {
              image.setAlpha(.5f);
              image.animate().setDuration(300).alpha(1);
            }

            // save to the cache
            if (mBitmapCache != null)
              mBitmapCache.put(mAboutBitmapKey, loadedBitmap);
          }

        });
      }

      // update the action bar title
      Utils.setActionBarTitle(getActivity(), "About", data.mTitle);
    }

  }

  private Bitmap cachedBitmap() {
    if (mBitmapCache == null) {
      Context context = getActivity();
      if (context != null)
        mBitmapCache = new BitmapCache(context, "AboutImageCache", diskCacheSize, Bitmap.CompressFormat.PNG, 0);
    }

    if (mBitmapCache != null)
      return mBitmapCache.getBitmap(mAboutBitmapKey);

    return null;
  }
}
