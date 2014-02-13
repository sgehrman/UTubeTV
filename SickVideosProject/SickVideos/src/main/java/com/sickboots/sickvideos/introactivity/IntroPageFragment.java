package com.sickboots.sickvideos.introactivity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sickboots.sickvideos.R;

public class IntroPageFragment extends Fragment {
  private int mPageNumber;

  public IntroPageFragment(int pageNumber) {
    super();

    mPageNumber = pageNumber;
  }

  public IntroPageFragment() {
    super();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("pageNumber", mPageNumber);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_intro_page, container, false);

    String title = "", message = "";

    if (savedInstanceState != null && mPageNumber == 0) {
      mPageNumber = savedInstanceState.getInt("pageNumber", 0);
    }

    switch (mPageNumber) {
      case 0:
        title = "Thanks!";
        message = "I'm glad you found my app.  Here's a quick start guide on how to use it.  Swipe screen to the next page...";
        break;
      case 1:
        title = "Why use this app?";
        message = "YouTube is great, but I find the app to be overwhelming.  This app is like a video magazine.  One subject with the top channels.";
        break;
      case 2:
        title = "General Usage";
        message = "Select a channel from the action bar on the top left.  Use the left drawer to navigate within the current channel.  Swipe cards away when your finished watching or not interested.";
        break;
      case 3:
        title = "I need your help!";
        message = "I'm just getting started and want to create the best app possible, so please send your feedback.  Did I include the BEST channels?  Are there other channels you would want to see in an app?";
        break;
    }

    TextView titleView = (TextView) rootView.findViewById(R.id.title);
    TextView messageView = (TextView) rootView.findViewById(R.id.message);

    titleView.setText(title);
    messageView.setText(message);

    return rootView;
  }

}
