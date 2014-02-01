package com.sickboots.sickvideos.mainactivity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.sickboots.sickvideos.R;

/**
 * Created by sgehrman on 1/31/14.
 */
public class ChannelSpinnerAdapter extends ArrayAdapter {

 public ChannelSpinnerAdapter(Context context) {
   super(context, android.R.layout.simple_spinner_item, android.R.id.text1);

   setDropDownViewResource(R.layout.channel_spinner_item);
 }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return super.getView(position, convertView, parent);
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    return super.getDropDownView(position, convertView, parent);
  }

}


