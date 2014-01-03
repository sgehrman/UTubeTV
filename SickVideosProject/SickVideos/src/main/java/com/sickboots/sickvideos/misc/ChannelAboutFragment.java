package com.sickboots.sickvideos.misc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.Content;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChannelAboutFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_channel_about, container, false);

    return rootView;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    askYouTubeForAboutInfo();

    Utils.setActionBarTitle(activity, "About");
  }

  private void updateUI(View rootView, YouTubeData data) {
    TextView title = (TextView) rootView.findViewById(R.id.text_view);
    TextView description = (TextView) rootView.findViewById(R.id.description_view);
    final ImageView image = (ImageView) rootView.findViewById(R.id.image);

    title.setText(data.mTitle);
    description.setText(data.mDescription);


    int defaultImageResID = 0;

    UrlImageViewHelper.setUrlDrawable(image, data.mThumbnail, defaultImageResID, new UrlImageViewCallback() {

      @Override
      public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
        if (!loadedFromCache) {
          image.setAlpha(.5f);
          image.animate().setDuration(200).alpha(1);
        } else
          image.setAlpha(1f);
      }

    });

  }

  private void askYouTubeForAboutInfo() {
    (new Thread(new Runnable() {
      public void run() {
        YouTubeData channelInfo = null;

        DatabaseAccess database = new DatabaseAccess(getActivity(), DatabaseTables.channelTable());

        List<YouTubeData> items = database.getItems(0, Content.channelID(), 1);

        if (items.size() > 0)
          channelInfo = items.get(0);

        if (channelInfo == null) {
          YouTubeAPI helper = new YouTubeAPI(getActivity(), new YouTubeAPI.YouTubeAPIListener() {
            @Override
            public void handleAuthIntent(final Intent authIntent) {
              Debug.log("handleAuthIntent inside update Service.  not handled here");
            }
          });

          final Map fromYouTubeMap = helper.channelInfo(Content.channelID());

          // save in the db
          channelInfo = new YouTubeData();
          channelInfo.mThumbnail = (String) fromYouTubeMap.get("thumbnail");
          channelInfo.mTitle = (String) fromYouTubeMap.get("title");
          channelInfo.mDescription = (String) fromYouTubeMap.get("description");
          channelInfo.mChannel = Content.channelID();

          database.insertItems(Arrays.asList(channelInfo));
        }

        final YouTubeData newChannelInfo = channelInfo;

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
          @Override
          public void run() {
            updateUI(getView(), newChannelInfo);
          }
        });
      }
    })).start();
  }

}
