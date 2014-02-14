package com.sickboots.sickvideos.introactivity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.LinePageIndicator;

/**
 * Created by sgehrman on 9/11/13.
 */
public class IntroFragment extends Fragment {

  public IntroFragment() {
    super();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_intro, container, false);

    Button button = (Button) rootView.findViewById(R.id.sign_up_button);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        getActivity().finish();
      }
    });

    // Create the adapter that will return a fragment for each of the
    // primary sections of the app.
    IntroPagerAdapter introPagerAdapter = new IntroPagerAdapter(getFragmentManager());

    // Set up the ViewPager with the sections adapter.
    ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.intro_pager);
    viewPager.setAdapter(introPagerAdapter);

    LinePageIndicator ind = (LinePageIndicator) rootView.findViewById(R.id.line_indicator);
    ind.setViewPager(viewPager);

    return rootView;
  }
}
