package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.common.collect.ImmutableMap;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.ChannelAboutFragment;
import com.sickboots.sickvideos.misc.ColorPickerFragment;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by sgehrman on 1/2/14.
 */
public class Content extends Observable {
  private ProductCode mProductCode;
  private YouTubeData mChannelInfo;
  private Context mContext;

  public static enum ProductCode {NEURO_SOUP, KHAN_ACADEMY, CONNECTIONS, JUSTIN_BIEBER, ANDROID_DEVELOPER, VICE, TOP_GEAR, COLLEGE_HUMOR, ROGAN, BEARD_CLUB, LUKITSCH, USER}

  public static enum ThemeCode {DARK, LIGHT}

  public static final String CONTENT_UPDATED_NOTIFICATION = "CONTENT_UPDATED";

  public Content(Context context, ProductCode code) {
    super();

    mProductCode = code;

    mContext = context.getApplicationContext();

    askYouTubeForChannelInfo(false);
  }

  public ArrayList<Map> drawerTitles() {
    ArrayList<Map> result = new ArrayList<Map>();

    switch (mProductCode) {
      case NEURO_SOUP:
      case CONNECTIONS:
      case ROGAN:
      case TOP_GEAR:
      case VICE:
      case BEARD_CLUB:
      case COLLEGE_HUMOR:
      case ANDROID_DEVELOPER:
      case JUSTIN_BIEBER:
      case KHAN_ACADEMY:
      case LUKITSCH:

        result.add(ImmutableMap.of("title", "About", "icon", ToolbarIcons.IconID.ABOUT));
        result.add(ImmutableMap.of("title", "Playlists", "icon", ToolbarIcons.IconID.PLAYLISTS));
        break;
      case USER:
        String[] titles = new String[]{"About", "Favorites", "Likes", "History", "Uploads", "Watch Later", "Color Picker", "Connections", "Connections Intent"};
        for (String title : titles)
          result.add(ImmutableMap.of("title", title, "icon", ToolbarIcons.IconID.VIDEO_PLAY));
        break;
    }

    return result;
  }

  public String actionBarTitle(int flag) {
    String title = null;

    switch (flag) {
      case 0:
        title = "About";
        break;
      case 1:
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
      case JUSTIN_BIEBER:
      case KHAN_ACADEMY:
      case COLLEGE_HUMOR:
      case ANDROID_DEVELOPER:
      case TOP_GEAR:
      case LUKITSCH:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment(this);
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID(), null));
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
      case LUKITSCH:
        return "UCULJH9kW-UdTBCDu27P0BoA";
      case KHAN_ACADEMY:
        return "UC4a-Gbdw7vOaccHmFo40b9g";
      case COLLEGE_HUMOR:
        return "UCPDXXXJj9nax0fr0Wfc048g";
      case TOP_GEAR:
        return "UCjOl2AUblVmg2rA_cRgZkFg";
      case JUSTIN_BIEBER:
        return "UCHkj014U2CQ2Nv0UZeYpE_A";
      case ANDROID_DEVELOPER:
        return "UCVHFbqXqoYvEWM1Ddxl0QDg";
    }

    return null;
  }

  public YouTubeData channelInfo() {
    return mChannelInfo;
  }

  public void refreshChannelInfo() {
    askYouTubeForChannelInfo(true);
  }

  private void askYouTubeForChannelInfo(final boolean refresh) {
    (new Thread(new Runnable() {
      public void run() {
        YouTubeData channelInfo = null;
        DatabaseAccess database = new DatabaseAccess(mContext, DatabaseTables.channelTable());

        // if refreshing, don't get from database (need to remove existing data?)
        if (refresh) {
          database.deleteAllRows(channelID());
        } else {
          List<YouTubeData> items = database.getItems(0, channelID(), 1);

          if (items.size() > 0)
            channelInfo = items.get(0);
        }

        if (channelInfo == null) {
          YouTubeAPI helper = new YouTubeAPI(mContext, new YouTubeAPI.YouTubeAPIListener() {
            @Override
            public void handleAuthIntent(final Intent authIntent) {
              Debug.log("handleAuthIntent inside update Service.  not handled here");
            }
          });

          final Map fromYouTubeMap = helper.channelInfo(channelID());

          // save in the db if we got results
          if (fromYouTubeMap.size() > 0) {
            channelInfo = new YouTubeData();
            channelInfo.mThumbnail = (String) fromYouTubeMap.get("thumbnail");
            channelInfo.mTitle = (String) fromYouTubeMap.get("title");
            channelInfo.mDescription = (String) fromYouTubeMap.get("description");
            channelInfo.mChannel = channelID();

            database.insertItems(Arrays.asList(channelInfo));
          }
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
