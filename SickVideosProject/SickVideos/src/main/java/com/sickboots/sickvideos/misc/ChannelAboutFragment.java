package com.sickboots.sickvideos.misc;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.billing.IabException;
import com.sickboots.sickvideos.billing.IabResult;
import com.sickboots.sickvideos.billing.Purchase;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChannelAboutFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_channel_about, container, false);

    askYouTubeForAboutInfo();

    return rootView;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  private void updateUI(View rootView, Map info) {
    TextView title = (TextView) rootView.findViewById(R.id.text_view);
    TextView description = (TextView) rootView.findViewById(R.id.description_view);
    final ImageView image = (ImageView) rootView.findViewById(R.id.image);

    title.setText((String) info.get("title"));
    description.setText((String) info.get("description"));


    int defaultImageResID = 0;

    UrlImageViewHelper.setUrlDrawable(image, (String) info.get("thumbnail"), defaultImageResID, new UrlImageViewCallback() {

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

        YouTubeAPI helper = new YouTubeAPI(getActivity(), new YouTubeAPI.YouTubeAPIListener() {
          @Override
          public void handleAuthIntent(final Intent authIntent) {
            Debug.log("handleAuthIntent inside update Service.  not handled here");
          }
        });

        final Map result = helper.channelInfo("UC07XXQh04ukEX68loZFgnVw");
        Debug.log("result here.");

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
          @Override
          public void run() {
            updateUI(getView(), result);
          }
        });

      }
    })).start();
  }

}
