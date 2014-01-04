package com.sickboots.iconicdroid;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class IconicActivity extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initSamplesList();

    getActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void initSamplesList() {
    List<SampleItem> sampleItems = new ArrayList<SampleItem>();

    sampleItems.add(new SampleItem("Entypo icons", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(IconicTypefaceActivity.createIntent(IconicActivity.this,
            IconicTypefaceActivity.ICON_TYPE_ENTYPO));
      }
    }));
    sampleItems.add(new SampleItem("Entypo Social icons", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(IconicTypefaceActivity.createIntent(IconicActivity.this,
            IconicTypefaceActivity.ICON_TYPE_ENTYPO_SOCIAL));
      }
    }));
    sampleItems.add(new SampleItem("Font Awesome icons", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(IconicTypefaceActivity.createIntent(IconicActivity.this,
            IconicTypefaceActivity.ICON_TYPE_FONT_AWESOME));
      }
    }));
    sampleItems.add(new SampleItem("Iconic icons", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(IconicTypefaceActivity.createIntent(IconicActivity.this,
            IconicTypefaceActivity.ICON_TYPE_ICONIC));
      }
    }));

    SamplesAdapter adapter = new SamplesAdapter(this, sampleItems);
    getListView().setAdapter(adapter);
  }

  private class SampleItem {

    public String title;
    public View.OnClickListener onClickListener;

    public SampleItem(String title, View.OnClickListener onClickListener) {
      this.title = title;
      this.onClickListener = onClickListener;
    }
  }

  private class SamplesAdapter extends ArrayAdapter<SampleItem> {

    private LayoutInflater mInflater;
    private List<SampleItem> mSamples;

    public SamplesAdapter(Context context, List<SampleItem> samples) {
      super(context, R.id.tv_title, samples);
      mSamples = samples;
      mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      final SampleItem sampleItem = mSamples.get(position);

      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.list_item_group, null);
      }

      ((TextView) convertView.findViewById(R.id.tv_title)).setText(sampleItem.title);
      convertView.setOnClickListener(sampleItem.onClickListener);

      return convertView;
    }
  }
}

