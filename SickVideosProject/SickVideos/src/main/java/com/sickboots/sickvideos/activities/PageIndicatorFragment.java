package com.sickboots.sickvideos.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sickboots.sickvideos.R;

/**
 * Created by sgehrman on 9/10/13.
 */
public class PageIndicatorFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_page_indicator, container, false);

    return rootView;
  }

  @Override
  public void onViewCreated(android.view.View view, android.os.Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.group);
    radioGroup.setOnCheckedChangeListener(this);

    ViewPager viewPager = viewPager();
    if (viewPager != null) {

      // set up callback so radio group syncs to the viewPager
      viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
          RadioGroup radioGroup = (RadioGroup) getView().findViewById(R.id.group);

          if (radioGroup != null) {
            radioGroup.check(position + 100);
          }
        }
      });

      int count = viewPager.getAdapter().getCount();
      for (int i = count - 1; i >= 0; i--) {
        addRadioButton(radioGroup, i);
      }

      radioGroup.check(100);
    }
  }

  private void addRadioButton(RadioGroup radioGroup, int radioID) {
    RadioButton newRadioButton = new RadioButton(getActivity());
    newRadioButton.setText("");
    newRadioButton.setId(radioID + 100);
    LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
        RadioGroup.LayoutParams.WRAP_CONTENT,
        RadioGroup.LayoutParams.WRAP_CONTENT);
    radioGroup.addView(newRadioButton, 0, layoutParams);
  }

  public void onCheckedChanged(RadioGroup group, int checkedId) {
    viewPager().setCurrentItem(checkedId - 100);
  }

  private ViewPager viewPager() {
    return (ViewPager) getActivity().findViewById(R.id.intro_pager);
  }

}
