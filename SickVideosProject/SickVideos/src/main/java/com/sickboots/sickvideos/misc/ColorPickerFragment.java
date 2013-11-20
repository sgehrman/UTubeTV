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
import com.sickboots.sickvideos.misc.ApplicationHub;

/**
 * Created by sgehrman on 11/12/13.
 */
public class ColorPickerFragment extends Fragment implements ColorPicker.OnColorChangedListener {

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
  public void onColorChanged(int i) {
    ActionBar bar = getActivity().getActionBar();
    bar.setBackgroundDrawable(new ColorDrawable(i));

    // this here to trigger a refresh only
    bar.setTitle(bar.getTitle());

    // log it in a format xml can use
    Util.log("color: #" + Integer.toHexString(i));

    // save the preference
    ApplicationHub.instance().setPref(ApplicationHub.ACTION_BAR_COLOR, Integer.toString(i));
  }
}
