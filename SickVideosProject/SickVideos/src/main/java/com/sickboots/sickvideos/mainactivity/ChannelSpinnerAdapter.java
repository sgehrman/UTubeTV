package com.sickboots.sickvideos.mainactivity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by sgehrman on 1/31/14.
 */
public class ChannelSpinnerAdapter extends ArrayAdapter {

 public ChannelSpinnerAdapter(Context context) {
   super(context, android.R.layout.simple_spinner_item);
 }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return super.getView(position, convertView, parent);
  }
}


