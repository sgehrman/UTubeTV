package com.distantfuture.videos.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.content.Content;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.imageutils.CircleImageTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.greenrobot.event.EventBus;

public class ActionBarSpinnerAdapter extends ArrayAdapter<CharSequence> {
  Content mContent;
  private List<YouTubeData> mChannels;  // we save this to get thumbnails in getView()
  private Context mContext;
  private CharSequence mTitle;
  private CharSequence mSubtitle;
  private CircleImageTransformation mCircleTransform;

  public ActionBarSpinnerAdapter(Context context, Content content) {
    super(context, R.layout.view_ab_spinner, android.R.id.text1);

    mContext = context.getApplicationContext();
    mContent = content;
    mCircleTransform = new CircleImageTransformation();

    EventBus.getDefault().register(this);

    setDropDownViewResource(R.layout.view_ab_spinner_item);

    updateChannels();
  }

  @Override
  protected void finalize() throws Throwable {
    EventBus.getDefault().unregister(this);

    super.finalize();
  }

  public void updateChannels() {
    clear();

    mChannels = mContent.channels();

    if (mChannels != null) {
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
  public View getView(int position, View view, ViewGroup parent) {
    ViewHolder2 holder;
    if (view == null) {
      view = LayoutInflater.from(mContext).inflate(R.layout.view_ab_spinner, parent, false);
      holder = new ViewHolder2();
      holder.subtitle = (TextView) view.findViewById(R.id.action_bar_subtitle);
      holder.title = (TextView) view.findViewById(android.R.id.text1);
      view.setTag(holder);
    } else {
      holder = (ViewHolder2) view.getTag();
    }

    holder.title.setText(mTitle);
    holder.subtitle.setText(mSubtitle);

    return view;
  }

  // eventbus event
  public void onEventMainThread(BusEvents.ContentEvent event) {
    updateChannels();
  }

  @Override
  public View getDropDownView(int position, View view, ViewGroup parent) {
    if (mChannels == null)
      return super.getDropDownView(position, view, parent);

    ViewHolder holder;
    if (view == null) {
      view = LayoutInflater.from(mContext).inflate(R.layout.view_ab_spinner_item, parent, false);
      holder = new ViewHolder();
      holder.imageView = (ImageView) view.findViewById(android.R.id.icon1);
      holder.textView = (CheckedTextView) view.findViewById(android.R.id.text1);
      view.setTag(holder);
    } else {
      holder = (ViewHolder) view.getTag();
    }

    final YouTubeData data = mChannels.get(position);

    holder.textView.setText(getItem(position));

    Picasso.with(mContext).load(data.mThumbnail)
        .fit()
        .transform(mCircleTransform)

            //          .noFade()
            //          .resize(250, 250) // put into dimens for dp values
        .into(holder.imageView);

    if ((position % 2) != 0)
      view.setBackgroundColor(0x05ffffff);
    else
      view.setBackgroundColor(0);

    return view;
  }

  private static class ViewHolder {
    ImageView imageView;
    CheckedTextView textView;
  }

  private static class ViewHolder2 {
    TextView title;
    TextView subtitle;
  }
}
