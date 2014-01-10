package com.sickboots.sickvideos.misc;

import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.sickboots.sickvideos.R;

// set custom color
//    String customColor = AppUtils.instance().getPref(AppUtils.ACTION_BAR_COLOR, null);
//    if (customColor != null) {
//      int color = Integer.parseInt(customColor);
//      getActionBar().setBackgroundDrawable(new ColorDrawable(color));
//    }

public class ColorPickerFragment extends Fragment implements ColorPicker.OnColorChangedListener {
  private int mLastColor;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_color_picker, container, false);

    ColorPicker picker = (ColorPicker) rootView.findViewById(R.id.picker);
    SVBar svBar = (SVBar) rootView.findViewById(R.id.svbar);
    OpacityBar opacityBar = (OpacityBar) rootView.findViewById(R.id.opacitybar);
    SaturationBar saturationBar = (SaturationBar) rootView.findViewById(R.id.saturationbar);
    ValueBar valueBar = (ValueBar) rootView.findViewById(R.id.valuebar);

//  picker.addSVBar(svBar);
//  picker.addOpacityBar(opacityBar);
    svBar.setVisibility(View.GONE);  // hide it, not used
    opacityBar.setVisibility(View.GONE);  // hide it, not used

    picker.addSaturationBar(saturationBar);
    picker.addValueBar(valueBar);

    int color = picker.getColor();

    picker.setOldCenterColor(color);

    picker.setOnColorChangedListener(this);

    return rootView;
  }

  @Override
  public void onColorChanged(int color) {

    if (color != mLastColor) {
      mLastColor = color;
      Debug.log("setting: #" + Integer.toHexString(color));

      boolean actionbar = true;

      if (actionbar) {
        ActionBar bar = getActivity().getActionBar();

        bar.setBackgroundDrawable(new ColorDrawable(color));
        bar.setTitle(bar.getTitle());
      } else {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(color));

      }
    }
  }
}
