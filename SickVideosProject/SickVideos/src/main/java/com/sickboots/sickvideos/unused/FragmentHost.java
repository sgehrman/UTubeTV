package com.sickboots.sickvideos.unused;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.youtube.YouTubeFragment;

/**
 * Created by sgehrman on 10/28/13.
 */
public class FragmentHost extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_host, container, false);
  }

  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // Check that the activity is using the layout version with
    // the fragment_container FrameLayout
    if (getActivity().findViewById(R.id.fragment_container) != null) {

      // However, if we're being restored from a previous state,
      // then we don't need to do anything and should return or else
      // we could end up with overlapping fragments.
      if (savedInstanceState != null) {
        return;
      }

      // Create a new Fragment to be placed in the activity layout
      YouTubeFragment firstFragment = YouTubeFragment.subscriptionsFragment();

      // Add the fragment to the 'fragment_container' FrameLayout
      getFragmentManager().beginTransaction()
          .add(R.id.fragment_container, firstFragment).commit();
    }
  }

}
