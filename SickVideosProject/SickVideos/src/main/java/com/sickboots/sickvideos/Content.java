package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.AppUtils;
import com.sickboots.sickvideos.misc.ChannelAboutFragment;
import com.sickboots.sickvideos.misc.ColorPickerFragment;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by sgehrman on 1/2/14.
 */
public class Content extends Observable {
  private ProductCode mProductCode = ProductCode.KHAN_ACADEMY;
  private YouTubeData mChannelInfo;
  private Context mContext;

  public static final String CONTENT_UPDATED_NOTIFICATION = "CONTENT_UPDATED";

  public Content(Context context) {
    super();

    mContext = context.getApplicationContext();

    askYouTubeForChannelInfo();
  }

  public String[] drawerTitles() {
    switch (mProductCode) {
      case NEURO_SOUP:
      case CONNECTIONS:
      case ROGAN:
      case VICE:
      case BEARD_CLUB:
      case KHAN_ACADEMY:
        return new String[]{actionBarTitle(0), actionBarTitle(1)};
      case USER:
        return new String[]{"About", "Favorites", "Likes", "History", "Uploads", "Watch Later", "Color Picker", "Connections", "Connections Intent"};
    }

    return null;
  }

  public String actionBarTitle(int flag) {
    String title = null;

    switch (flag) {
      case 0:
        if (mChannelInfo != null)
          title = mChannelInfo.mTitle;

        if (title == null)
          title = "About";

        break;
      case 1:

        if (mChannelInfo != null) {
            title = mChannelInfo.mTitle;
          if (title != null)
            title = "Playlists on " + title;
        }

        if (title == null)
          title = "Playlists";

        break;
    }

    return title;
  }

  public Fragment fragmentForIndex(int index) {
    Fragment fragment = null;

    switch (mProductCode) {
      case NEURO_SOUP:
      case CONNECTIONS:
      case ROGAN:
      case VICE:
      case BEARD_CLUB:
      case KHAN_ACADEMY:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment(this);
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID(), actionBarTitle(1)));
            break;
        }
        break;
      case USER:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment(this);
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.FAVORITES, null));
            break;
          case 2:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.LIKES, null));
            break;
          case 3:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.WATCHED, null));
            break;
          case 4:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, null));
            break;
          case 5:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.WATCHLATER, null));
            break;
          case 6:
            fragment = new ColorPickerFragment();
            break;
          case 7:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID(), null));
            break;
          case 8:
//            YouTubeAPI.openPlaylistUsingIntent(this, "PLC5CD4355724A28FC");
            break;
        }
    }

    return fragment;
  }

  private void notifyForDataUpdate() {
    setChanged();
    notifyObservers(Content.CONTENT_UPDATED_NOTIFICATION);
  }

  public String channelID() {
    switch (mProductCode) {
      case CONNECTIONS:
        return "UC07XXQh04ukEX68loZFgnVw";
      case USER:
        return "UC07XXQh04ukEX68loZFgnVw";
      case NEURO_SOUP:
        return "UCf--Le-Ssa_R5ERoM7PbdcA";
      case VICE:
        return "UCn8zNIfYAQNdrFRrr8oibKw";
      case ROGAN:
        return "UCzQUP1qoWDoEbmsQxvdjxgQ";
      case BEARD_CLUB:
        return "UCULJH9kW-UdTBCDu27P0BoA";
      case KHAN_ACADEMY:
        return "UC4a-Gbdw7vOaccHmFo40b9g";
    }

    return null;
  }

  public static enum ProductCode {NEURO_SOUP, KHAN_ACADEMY, CONNECTIONS, VICE, ROGAN, BEARD_CLUB, USER}


  public YouTubeData channelInfo() {
    return mChannelInfo;
  }

  private void askYouTubeForChannelInfo() {
    (new Thread(new Runnable() {
      public void run() {
        YouTubeData channelInfo = null;

        DatabaseAccess database = new DatabaseAccess(mContext, DatabaseTables.channelTable());

        List<YouTubeData> items = database.getItems(0, channelID(), 1);

        if (items.size() > 0)
          channelInfo = items.get(0);

        if (channelInfo == null) {
          YouTubeAPI helper = new YouTubeAPI(mContext, new YouTubeAPI.YouTubeAPIListener() {
            @Override
            public void handleAuthIntent(final Intent authIntent) {
              Debug.log("handleAuthIntent inside update Service.  not handled here");
            }
          });

          final Map fromYouTubeMap = helper.channelInfo(channelID());

          // save in the db
          channelInfo = new YouTubeData();
          channelInfo.mThumbnail = (String) fromYouTubeMap.get("thumbnail");
          channelInfo.mTitle = (String) fromYouTubeMap.get("title");
          channelInfo.mDescription = (String) fromYouTubeMap.get("description");
          channelInfo.mChannel = channelID();

          database.insertItems(Arrays.asList(channelInfo));
        }

        final YouTubeData newChannelInfo = channelInfo;

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
          @Override
          public void run() {
            // we are on the main thread, set the new data and send out notifications
            mChannelInfo = newChannelInfo;
            notifyForDataUpdate();
          }
        });
      }
    })).start();
  }


}
