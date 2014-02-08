package com.sickboots.sickvideos.mainactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.content.Content;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.imageutils.BitmapLoader;
import com.sickboots.sickvideos.imageutils.ToolbarIcons;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ActionBarSpinnerAdapter extends ArrayAdapter<CharSequence> implements Observer {
  private List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()
  private Context mContext;
  private Drawable mCheckDrawable;
  private BitmapLoader mBitmapLoader;
  Content mContent;
  private CharSequence mTitle;
  private CharSequence mSubtitle;

  public ActionBarSpinnerAdapter(Context context, Content content) {
    super(context, R.layout.action_bar_spinner_item, android.R.id.text1);

    mContext = context.getApplicationContext();
    mContent = content;

    mBitmapLoader = new BitmapLoader(context);

    setDropDownViewResource(R.layout.channel_spinner_ab_item);

    updateChannels();
  }

  public void updateChannels() {
    clear();

    if (mContent.channelInfo() == null)
      mContent.addObserver(this);
    else {
      mChannels = mContent.mChannelList.channels();

      for (YouTubeData data : mChannels)
        add(data.mTitle);
    }
  }

  public void setTitleAndSubtitle(CharSequence title, CharSequence subtitle) {
    mTitle = title;
    mSubtitle = subtitle;
    notifyDataSetChanged();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View result = super.getView(position, convertView, parent);

    TextView title = (TextView) result.findViewById(android.R.id.text1);
    TextView subtitle = (TextView) result.findViewById(R.id.action_bar_subtitle);

    title.setText(mTitle);
    subtitle.setText(mSubtitle);

    return result;
  }

  @Override
  public void update(Observable observable, Object data) {
    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(Content.CONTENT_UPDATED_NOTIFICATION)) {

        updateChannels();

        // only need this called once
        mContent.deleteObserver(this);
      }
    }
  }

  @Override
  public View getDropDownView(int position, View view, ViewGroup parent) {
    if (mChannels == null)
      return super.getDropDownView(position, view, parent);

    ViewHolder holder;
    if (view == null) {
      view = LayoutInflater.from(mContext).inflate(R.layout.channel_spinner_item, parent, false);
      holder = new ViewHolder();
      holder.imageView = (ImageView) view.findViewById(android.R.id.icon1);
      holder.textView = (CheckedTextView) view.findViewById(android.R.id.text1);
      view.setTag(holder);
    } else {
      holder = (ViewHolder) view.getTag();
    }

    final YouTubeData data = mChannels.get(position);

    holder.textView.setText(getItem(position));

    final int thumbnailSize = 64;

    Bitmap bitmap = mBitmapLoader.bitmap(data, thumbnailSize);
    if (bitmap != null)
      holder.imageView.setImageBitmap(bitmap);
    else {
      mBitmapLoader.requestBitmap(data, thumbnailSize, new BitmapLoader.GetBitmapCallback() {
        @Override
        public void onLoaded(Bitmap bitmap) {
          if (bitmap != null)  // avoid and endless loop update if bitmap is null, don't refresh
            ActionBarSpinnerAdapter.this.notifyDataSetChanged();
        }
      });
    }

    return view;
  }

  private static class ViewHolder {
    ImageView imageView;
    CheckedTextView textView;
  }
}
