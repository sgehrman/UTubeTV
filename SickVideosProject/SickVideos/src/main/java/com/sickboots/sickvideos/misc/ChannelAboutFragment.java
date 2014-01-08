package com.sickboots.sickvideos.misc;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.Content;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.YouTubeGridFragment;
import com.sickboots.sickvideos.database.YouTubeData;

import java.util.Observable;
import java.util.Observer;

public class ChannelAboutFragment extends Fragment implements Observer {
  TextView mTitle;
  TextView mDescription;
  ImageView mImage;
  Content mContent;

  public ChannelAboutFragment(Content content) {
    super();

    mContent = content;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_channel_about, container, false);

    mTitle = (TextView) rootView.findViewById(R.id.text_view);
    mDescription = (TextView) rootView.findViewById(R.id.description_view);
    mImage = (ImageView) rootView.findViewById(R.id.image);
    Button button = (Button) rootView.findViewById(R.id.watch_button);

    mImage.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showPlaylistsFragment();
      }
    });
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showPlaylistsFragment();
      }
    });


    updateUI();

    return rootView;
  }

  private void showPlaylistsFragment() {
    YouTubeGridFragment.HostActivitySupport provider = (YouTubeGridFragment.HostActivitySupport) getActivity();

    provider.showPlaylistsFragment();
  }

  @Override
  public void update(Observable observable, Object data) {

    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(Content.CONTENT_UPDATED_NOTIFICATION)) {

        updateUI();

        // only need this called once
        mContent.deleteObserver(this);
      }
    }
  }

  private void updateUI() {
    YouTubeData data = mContent.channelInfo();
    if (data == null) {
      mContent.addObserver(this);
    } else {
      mTitle.setText(data.mTitle);
      mDescription.setText(data.mDescription);

      int defaultImageResID = 0;
      final ImageView image = mImage;
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

    // update the action bar title
    Utils.setActionBarTitle(getActivity(), mContent.actionBarTitle(0));
  }

}
