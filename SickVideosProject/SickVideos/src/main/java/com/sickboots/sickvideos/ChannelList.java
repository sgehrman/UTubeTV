package com.sickboots.sickvideos;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgehrman on 1/30/14.
 */
public class ChannelList {

  public interface OnChannelListUpdateListener {
    public void onUpdate();
  }

  private List<YouTubeData> mChannels;
  private List<String> mChannelIds;
  private Map<ChannelCode, String> mChannelIDMap;
  private int mCurrentChannelIndex = 0;
  private Context mContext;
  private OnChannelListUpdateListener mListener;

  public ChannelList(Context context, ChannelCode code, OnChannelListUpdateListener listener) {
    super();

    mContext = context.getApplicationContext();
    mListener = listener;

    mChannelIds = new ArrayList<String>();
    mChannelIds.add(channelIDForCode(code));
    mChannelIds.add(channelIDForCode(ChannelCode.VICE));
    mChannelIds.add(channelIDForCode(ChannelCode.ROGAN));

    requestChannelInfo(false);
  }

  public void refresh() {
    requestChannelInfo(true);
  }

  String[] titles() {
    List<String> result = new ArrayList<String>();

    if (mChannels != null) {
      for (YouTubeData channel : mChannels) {
        result.add(channel.mTitle);
      }
    }

    return result.toArray(new String[0]);
  }

  public String currentChannelId() {
    return channelIdForIndex(mCurrentChannelIndex);
  }

  public boolean needsChannelSwitcher() {
    return mChannelIds.size() > 1;
  }

  public YouTubeData currentChannelInfo() {
    YouTubeData result = null;

    if (mChannels != null)
      result = mChannels.get(mCurrentChannelIndex);

    return result;
  }

  public String channelIdForIndex(int index) {
    return mChannelIds.get(index);
  }

  public YouTubeData channelInfoForIndex(int index) {
    return mChannels.get(index);
  }

  // called on main thread
  private void updateChannels(List<YouTubeData> channels) {
    mChannels = channels;
    mListener.onUpdate();
  }

  public void changeChannel(int index) {
    mCurrentChannelIndex = index;
  }

  private void requestChannelInfo(final boolean refresh) {
    (new Thread(new Runnable() {
      public void run() {
        final List<YouTubeData> channels = new ArrayList<YouTubeData>();

        for (String channelId : mChannelIds) {
          YouTubeData data = requestChannelInfoForChannel(channelId, refresh);

          if (data != null)
            channels.add(data);
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
          @Override
          public void run() {
            updateChannels(channels);
          }
        });

      }
    })).start();
  }

  private YouTubeData requestChannelInfoForChannel(final String channelID, final boolean refresh) {
    YouTubeData result = null;
    DatabaseAccess database = new DatabaseAccess(mContext, DatabaseTables.channelTable());

    // if refreshing, don't get from database (need to remove existing data?)
    if (refresh) {
      database.deleteAllRows(channelID);
    } else {
      List<YouTubeData> items = database.getItems(0, channelID, 1);

      if (items.size() > 0)
        result = items.get(0);
    }

    if (result == null) {
      YouTubeAPI helper = new YouTubeAPI(mContext, new YouTubeAPI.YouTubeAPIListener() {
        @Override
        public void handleAuthIntent(final Intent authIntent) {
          Debug.log("handleAuthIntent inside update Service.  not handled here");
        }
      });

      final Map fromYouTubeMap = helper.channelInfo(channelID);

      // save in the db if we got results
      if (fromYouTubeMap.size() > 0) {
        result = new YouTubeData();
        result.mThumbnail = (String) fromYouTubeMap.get("thumbnail");
        result.mTitle = (String) fromYouTubeMap.get("title");
        result.mDescription = (String) fromYouTubeMap.get("description");
        result.mChannel = channelID;

        database.insertItems(Arrays.asList(result));
      }
    }

    return result;
  }


  private String channelIDForCode(ChannelCode code) {
    if (mChannelIDMap == null) {
      mChannelIDMap = new HashMap<ChannelCode, String>();

      mChannelIDMap.put(ChannelCode.CONNECTIONS, "UC07XXQh04ukEX68loZFgnVw");
      mChannelIDMap.put(ChannelCode.NEURO_SOUP, "UCf--Le-Ssa_R5ERoM7PbdcA");
      mChannelIDMap.put(ChannelCode.VICE, "UCn8zNIfYAQNdrFRrr8oibKw");
      mChannelIDMap.put(ChannelCode.ROGAN, "UCzQUP1qoWDoEbmsQxvdjxgQ");
      mChannelIDMap.put(ChannelCode.LUKITSCH, "UCULJH9kW-UdTBCDu27P0BoA");
      mChannelIDMap.put(ChannelCode.KHAN_ACADEMY, "UC4a-Gbdw7vOaccHmFo40b9g");
      mChannelIDMap.put(ChannelCode.TOP_GEAR, "UCjOl2AUblVmg2rA_cRgZkFg");
      mChannelIDMap.put(ChannelCode.ANDROID_DEVELOPERS, "UCVHFbqXqoYvEWM1Ddxl0QDg");
      mChannelIDMap.put(ChannelCode.NERDIST, "UCTAgbu2l6_rBKdbTvEodEDw");
      mChannelIDMap.put(ChannelCode.CODE_ORG, "UCJyEBMU1xVP2be1-AoGS1BA");
      mChannelIDMap.put(ChannelCode.MAX_KEISER, "UCBIwq18tUFrujiPd3HLPaGw");
      mChannelIDMap.put(ChannelCode.RT, "UCpwvZwUam-URkxB7g4USKpg");
      mChannelIDMap.put(ChannelCode.PEWDIEPIE, "UC-lHJZR3Gqxm24_Vd_AJ5Yw");
      mChannelIDMap.put(ChannelCode.BIG_THINK, "UCvQECJukTDE2i6aCoMnS-Vg");
      mChannelIDMap.put(ChannelCode.REASON_TV, "UC0uVZd8N7FfIZnPu0y7o95A");
      mChannelIDMap.put(ChannelCode.JET_DAISUKE, "UC6wKgAlOeFNqmXV167KERhQ");
      mChannelIDMap.put(ChannelCode.THE_VERGE, "UCddiUEpeqJcYeBxX1IVBKvQ");
      mChannelIDMap.put(ChannelCode.XDA, "UCk1SpWNzOs4MYmr0uICEntg");
      mChannelIDMap.put(ChannelCode.YOUNG_TURKS, "UC1yBKRuGpC1tSM73A0ZjYjQ");
      mChannelIDMap.put(ChannelCode.GATES_FOUNDATION, "UCRi8JQTnKQilJW15uzo7bRQ");
      mChannelIDMap.put(ChannelCode.JUSTIN_BIEBER, "UCHkj014U2CQ2Nv0UZeYpE_A");
      mChannelIDMap.put(ChannelCode.COLLEGE_HUMOR, "UCPDXXXJj9nax0fr0Wfc048g");
      mChannelIDMap.put(ChannelCode.YOUTUBE, "UCBR8-60-B28hp2BmDPdntcQ");
    }

    return mChannelIDMap.get(code);
  }

  public static enum ChannelCode {NEURO_SOUP, KHAN_ACADEMY, YOUNG_TURKS, XDA, CONNECTIONS, CODE_ORG, JUSTIN_BIEBER, THE_VERGE, REASON_TV, BIG_THINK, ANDROID_DEVELOPERS, PEWDIEPIE, YOUTUBE, VICE, TOP_GEAR, COLLEGE_HUMOR, ROGAN, LUKITSCH, NERDIST, RT, JET_DAISUKE, MAX_KEISER, GATES_FOUNDATION}

}
