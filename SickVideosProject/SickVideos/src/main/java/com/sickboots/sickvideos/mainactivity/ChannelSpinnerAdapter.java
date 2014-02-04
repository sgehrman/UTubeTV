package com.sickboots.sickvideos.mainactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.imageutils.ImageLoader;
import com.sickboots.sickvideos.imageutils.ToolbarIcons;
import com.sickboots.sickvideos.misc.Debug;

import java.util.List;

public class ChannelSpinnerAdapter extends ArrayAdapter {
  private List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()
  private Context mContext;
  private static Drawable sCheckDrawable;

  public ChannelSpinnerAdapter(Context context) {
    super(context, android.R.layout.simple_spinner_item, android.R.id.text1);

    mContext = context.getApplicationContext();

    setDropDownViewResource(R.layout.channel_spinner_item);
  }

  public void updateChannels(List<YouTubeData> channels) {
    mChannels = channels;

    clear();
    for (YouTubeData data : mChannels)
      add(data.mTitle);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return super.getView(position, convertView, parent);
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    View result = super.getDropDownView(position, convertView, parent);

    ImageView imageView = (ImageView) result.findViewById(android.R.id.icon1);
    CheckedTextView textView = (CheckedTextView) result.findViewById(android.R.id.text1);
    final YouTubeData data = mChannels.get(position);

    // is this right?  seems crazy
    if (((ListView)parent).isItemChecked(position)) {

      if (sCheckDrawable == null) {
        sCheckDrawable = ToolbarIcons.icon(mContext, ToolbarIcons.IconID.CHECK, 0xff000000, 30);
        sCheckDrawable.setAlpha(80);
      }

      textView.setCheckMarkDrawable(sCheckDrawable);
    }
    else
      textView.setCheckMarkDrawable(null);


    Bitmap bitmap = ImageLoader.instance(mContext).bitmap(data);
    if (bitmap != null)
      imageView.setImageBitmap(bitmap);
    else {
      ImageLoader.instance(mContext).requestBitmap(data, new ImageLoader.GetBitmapCallback() {

        @Override
        public void onLoaded() {
          ChannelSpinnerAdapter.this.notifyDataSetChanged();
        }

      });
    }

    return result;
  }
}
