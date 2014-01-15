package com.sickboots.iconicdroid;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sickboots.iconicdroid.icon.EntypoIcon;
import com.sickboots.iconicdroid.icon.EntypoSocialIcon;
import com.sickboots.iconicdroid.icon.FontAwesomeIcon;
import com.sickboots.iconicdroid.icon.Icon;
import com.sickboots.iconicdroid.icon.IconicIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class IconicTypefaceActivity extends ListActivity {

  private static final String EXTRA_ICON_TYPE = "extra_icon_type";

  public static final int ICON_TYPE_ENTYPO = 1001;
  public static final int ICON_TYPE_ENTYPO_SOCIAL = 1002;
  public static final int ICON_TYPE_FONT_AWESOME = 1003;
  public static final int ICON_TYPE_ICONIC = 1004;

  private SampleIconsAdapter mAdapter;

  public static Intent createIntent(final Context context, final int iconType) {
    Intent intent = new Intent(context, IconicTypefaceActivity.class);
    intent.putExtra(EXTRA_ICON_TYPE, iconType);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    int iconType = getIntent().getExtras().getInt(EXTRA_ICON_TYPE);
    initIconsList(iconType);

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

  @Override
  protected void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);
//   Icon icon = (Icon) listView.getAdapter().getItem(position);
//   startActivity(SimpleSampleActivity.createIntent(this, icon));
  }

  private void initIconsList(int iconType) {
    List<Icon> values = new ArrayList<Icon>();
    Icon[] valuesArray = null;

    switch (iconType) {
      case ICON_TYPE_ENTYPO: {
        valuesArray = EntypoIcon.values();
        setTitle("Entypo");
        break;
      }
      case ICON_TYPE_ENTYPO_SOCIAL: {
        valuesArray = EntypoSocialIcon.values();
        setTitle("Entypo-Social");
        break;
      }
      case ICON_TYPE_FONT_AWESOME: {
        valuesArray = FontAwesomeIcon.values();
        setTitle("Font Awesome");
        break;
      }
      case ICON_TYPE_ICONIC: {
        valuesArray = IconicIcon.values();
        setTitle("Iconic");
        break;
      }
    }

    values.addAll(Arrays.asList(valuesArray));

    mAdapter = new SampleIconsAdapter(this, values);
    getListView().setAdapter(mAdapter);
  }

  private class SampleIconsAdapter extends ArrayAdapter<Icon> {

    private LayoutInflater mInflater;
    private List<Icon> mIcons;

    public SampleIconsAdapter(Context context, List<Icon> icons) {
      super(context, R.layout.list_item_iconic, icons);
      mIcons = icons;
      mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      final Icon icon = mIcons.get(position);
      ViewHolder holder = null;

      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.list_item_iconic, null);
        holder = new ViewHolder();
        holder.title = (TextView) convertView.findViewById(R.id.tv_title);
        holder.icon = convertView.findViewById(R.id.view_icon);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      holder.title.setText(icon.toString());

      Random random = new Random();
      int color = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));

      IconicFontDrawable iconicFontDrawable = new IconicFontDrawable(getContext());
      iconicFontDrawable.setIcon(icon);
      iconicFontDrawable.setIconColor(color);
      holder.icon.setBackground(iconicFontDrawable);

      return convertView;
    }

    private class ViewHolder {

      public TextView title;
      public View icon;

    }
  }
}

