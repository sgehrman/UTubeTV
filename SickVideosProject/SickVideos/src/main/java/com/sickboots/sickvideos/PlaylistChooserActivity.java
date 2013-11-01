package com.sickboots.sickvideos;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created by sgehrman on 10/31/13.
 */
public class PlaylistChooserActivity extends Activity implements Util.PullToRefreshListener {
//  private PullToRefreshAttacher mPullToRefreshAttacher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_playlist_chooser);
    getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

    // This shit is buggy, must be created in onCreate of the activity, can't be created in the fragment.
    // This breaks it, no action bar in this mode. (see manifest) android:theme="@android:style/Theme.Dialog"
//    mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

    // However, if we're being restored from a previous state,
    // then we don't need to do anything and should return or else
    // we could end up with overlapping fragments.
    if (savedInstanceState != null) {
      return;
    }

    // Check that the activity is using the layout version with
    // the fragment_container FrameLayout
    FrameLayout container = (FrameLayout) findViewById(R.id.fragment_container);
    if (container != null) {
      // Create a new Fragment to be placed in the activity layout
      YouTubeFragment firstFragment = YouTubeFragment.newInstance(YouTubeListSpec.ListType.SUBSCRIPTIONS, null, null, null);

      // Add the fragment to the 'fragment_container' FrameLayout
      getFragmentManager().beginTransaction()
          .add(R.id.fragment_container, firstFragment).commit();
    }
  }

  // Add the Refreshable View and provide the refresh listener;
  @Override
  public void addRefreshableView(View theView, PullToRefreshAttacher.OnRefreshListener listener) {
//    mPullToRefreshAttacher.addRefreshableView(theView, listener);
  }

  @Override
  public void setRefreshComplete() {
//    mPullToRefreshAttacher.setRefreshComplete();
  }

}
