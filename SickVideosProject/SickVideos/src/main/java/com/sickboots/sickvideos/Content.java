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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by sgehrman on 1/2/14.
 */
public class Content extends Observable {
  public static final String CONTENT_UPDATED_NOTIFICATION = "CONTENT_UPDATED";
  private ProductCode mProductCode;
  private YouTubeData mChannelInfo;
  private Context mContext;
  private Map<ProductCode, String> mChannelIDMap;

  public Content(Context context, ProductCode code) {
    super();

    mProductCode = code;

    mContext = context.getApplicationContext();

    askYouTubeForChannelInfo(false);
  }

  public ArrayList<Map> drawerTitles() {
    ArrayList<Map> result = new ArrayList<Map>();

    switch (mProductCode) {
      default:
        result.add(ImmutableMap.of("title", "About", "icon", ToolbarIcons.IconID.ABOUT));
        result.add(ImmutableMap.of("title", "Playlists", "icon", ToolbarIcons.IconID.PLAYLISTS));
        result.add(ImmutableMap.of("title", "Recent Uploads", "icon", ToolbarIcons.IconID.UPLOADS));
        result.add(ImmutableMap.of("title", "Channels", "icon", ToolbarIcons.IconID.YOUTUBE));
        break;
    }

    return result;
  }

  public Fragment fragmentForIndex(int index) {
    Fragment fragment = null;

    switch (mProductCode) {
      default:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment();
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(channelID(), "Playlists", null, 250));
            break;
          case 2:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, channelID(), "Videos", "Recent Uploads", 50));
            break;
          case 3:
            fragment = new ChannelAboutFragment();
            break;
        }
        break;
    }

    return fragment;
  }

  private void notifyForDataUpdate() {
    setChanged();
    notifyObservers(Content.CONTENT_UPDATED_NOTIFICATION);
  }

  public String channelID() {
    if (mChannelIDMap == null) {
      mChannelIDMap = new HashMap<ProductCode, String>();

      mChannelIDMap.put(ProductCode.CONNECTIONS, "UC07XXQh04ukEX68loZFgnVw");
      mChannelIDMap.put(ProductCode.NEURO_SOUP, "UCf--Le-Ssa_R5ERoM7PbdcA");
      mChannelIDMap.put(ProductCode.VICE, "UCn8zNIfYAQNdrFRrr8oibKw");
      mChannelIDMap.put(ProductCode.ROGAN, "UCzQUP1qoWDoEbmsQxvdjxgQ");
      mChannelIDMap.put(ProductCode.LUKITSCH, "UCULJH9kW-UdTBCDu27P0BoA");
      mChannelIDMap.put(ProductCode.KHAN_ACADEMY, "UC4a-Gbdw7vOaccHmFo40b9g");
      mChannelIDMap.put(ProductCode.TOP_GEAR, "UCjOl2AUblVmg2rA_cRgZkFg");
      mChannelIDMap.put(ProductCode.ANDROID_DEVELOPERS, "UCVHFbqXqoYvEWM1Ddxl0QDg");
      mChannelIDMap.put(ProductCode.NERDIST, "UCTAgbu2l6_rBKdbTvEodEDw");
      mChannelIDMap.put(ProductCode.CODE_ORG, "UCJyEBMU1xVP2be1-AoGS1BA");
      mChannelIDMap.put(ProductCode.MAX_KEISER, "UCBIwq18tUFrujiPd3HLPaGw");
      mChannelIDMap.put(ProductCode.RT, "UCpwvZwUam-URkxB7g4USKpg");
      mChannelIDMap.put(ProductCode.PEWDIEPIE, "UC-lHJZR3Gqxm24_Vd_AJ5Yw");
      mChannelIDMap.put(ProductCode.BIG_THINK, "UCvQECJukTDE2i6aCoMnS-Vg");
      mChannelIDMap.put(ProductCode.REASON_TV, "UC0uVZd8N7FfIZnPu0y7o95A");
      mChannelIDMap.put(ProductCode.JET_DAISUKE, "UC6wKgAlOeFNqmXV167KERhQ");
      mChannelIDMap.put(ProductCode.THE_VERGE, "UCddiUEpeqJcYeBxX1IVBKvQ");
      mChannelIDMap.put(ProductCode.XDA, "UCk1SpWNzOs4MYmr0uICEntg");
      mChannelIDMap.put(ProductCode.YOUNG_TURKS, "UC1yBKRuGpC1tSM73A0ZjYjQ");
      mChannelIDMap.put(ProductCode.GATES_FOUNDATION, "UCRi8JQTnKQilJW15uzo7bRQ");
      mChannelIDMap.put(ProductCode.JUSTIN_BIEBER, "UCHkj014U2CQ2Nv0UZeYpE_A");
      mChannelIDMap.put(ProductCode.COLLEGE_HUMOR, "UCPDXXXJj9nax0fr0Wfc048g");
      mChannelIDMap.put(ProductCode.YOUTUBE, "UCBR8-60-B28hp2BmDPdntcQ");
    }

    return mChannelIDMap.get(mProductCode);
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

  public static enum ProductCode {NEURO_SOUP, KHAN_ACADEMY, YOUNG_TURKS, XDA, CONNECTIONS, CODE_ORG, JUSTIN_BIEBER, THE_VERGE, REASON_TV, BIG_THINK, ANDROID_DEVELOPERS, PEWDIEPIE, YOUTUBE, VICE, TOP_GEAR, COLLEGE_HUMOR, ROGAN, LUKITSCH, NERDIST, RT, JET_DAISUKE, MAX_KEISER, GATES_FOUNDATION}

}
