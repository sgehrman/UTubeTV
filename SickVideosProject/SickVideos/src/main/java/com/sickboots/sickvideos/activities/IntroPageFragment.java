package com.sickboots.sickvideos.activities;

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
        title = "Dareshare";
        message = "Dare to beat 'em all!";
        break;
      case 1:
        title = "Dareshare";
        message = "What you gonna do?";
        break;
      case 2:
        title = "Dareshare";
        message = "Try to win!";
        break;
      case 3:
        title = "Dareshare";
        message = "It's all about fun!";
        break;
    }

    TextView titleView = (TextView) rootView.findViewById(R.id.title);
    TextView messageView = (TextView) rootView.findViewById(R.id.message);

    titleView.setText(title);
    messageView.setText(message);

    return rootView;
  }

}
