package com.distantfuture.videos.content;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.distantfuture.videos.database.DatabaseAccess;
import com.distantfuture.videos.database.DatabaseTables;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.BusEvents;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class ChannelList {

  private List<YouTubeData> mChannels;
  private List<String> mChannelIds;
  private Map<ChannelCode, String> mChannelIDMap;
  private String mCurrentChannelID;
  private Context mContext;

  public ChannelList(Context context, int channels_array_resource) {
    super();

    mContext = context.getApplicationContext();

    mChannelIds = channelIds(context, channels_array_resource);
    mCurrentChannelID = AppUtils.instance(mContext).defaultChannelID(mChannelIds.get(0));

    requestChannelInfo(false);
  }

  public void refresh() {
    requestChannelInfo(true);
  }

  public List<YouTubeData> channels() {
    return mChannels;
  }

  // this is just used to set the initial value of the action bars spinner
  public int currentChannelIndex() {
    int i = 0;
    for (YouTubeData data : mChannels) {
      if (data.mChannel.equals(mCurrentChannelID))
        return i;

      i++;
    }

    Debug.log("should not get here: " + Debug.currentMethod());
    return 0;
  }

  public String currentChannelId() {
    return mCurrentChannelID;
  }

  public boolean needsChannelSwitcher() {
    return mChannelIds.size() > 1;
  }

  public YouTubeData currentChannelInfo() {
    YouTubeData result = null;

    if (mChannels != null)
      result = mChannels.get(currentChannelIndex());

    return result;
  }

  // called on main thread
  private void updateChannels(List<YouTubeData> channels) {
    // keep mChannels null if no results
    if (channels.size() > 0) {
      mChannels = channels;

      // notify anyone who cares
      EventBus.getDefault().post(new BusEvents.ContentEvent());
    }
  }

  // returns false if that channel is already current
  public boolean changeChannel(int index) {
    if (currentChannelIndex() != index) {
      mCurrentChannelID = mChannels.get(index).mChannel;

      AppUtils.instance(mContext).saveDefaultChannelID(mCurrentChannelID);

      return true;
    }

    return false;
  }

  public void editChannel(String channelId, boolean addChannel) {
    if (addChannel) {
      mChannelIds.add(channelId);
    } else {
      mChannelIds.remove(channelId);
    }

    // refresh data
    requestChannelInfo(false);
  }

  public boolean hasChannel(String channelId) {
    return mChannelIds.contains(channelId);
  }

  private void requestChannelInfo(final boolean refresh) {
    (new Thread(new Runnable() {
      public void run() {
        List<YouTubeData> channels = new ArrayList<YouTubeData>();
        final List<String> needToAskYouTube = new ArrayList<String>();
        DatabaseAccess database = new DatabaseAccess(mContext, DatabaseTables.channelTable());

        for (String channelId : mChannelIds) {
          YouTubeData data = requestChannelInfoFromDB(channelId, database, refresh);

          if (data != null)
            channels.add(data);
          else
            needToAskYouTube.add(channelId);
        }

        // ask youtube for channel info
        if (needToAskYouTube.size() > 0) {
          List<YouTubeData> fromYT = requestChannelInfoFromYT(needToAskYouTube, database);

          channels.addAll(fromYT);
        }

        // sort by title
        final List<YouTubeData> result = YouTubeData.sortByTitle(channels);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
          @Override
          public void run() {
            updateChannels(result);
          }
        });

      }
    })).start();
  }

  private YouTubeData requestChannelInfoFromDB(final String channelID, final DatabaseAccess database, final boolean refresh) {
    YouTubeData result = null;

    // if refreshing, don't get from database (need to remove existing data?)
    if (refresh) {
      database.deleteAllRows(channelID);
    } else {
      List<YouTubeData> items = database.getItems(0, channelID, 1);

      if (items.size() > 0)
        result = items.get(0);
    }

    return result;
  }

  private List<YouTubeData> requestChannelInfoFromYT(final List<String> channelIDs, final DatabaseAccess database) {
    List<YouTubeData> result = null;

    if (result == null) {
      YouTubeAPI helper = new YouTubeAPI(mContext, new YouTubeAPI.YouTubeAPIListener() {
        @Override
        public void handleAuthIntent(final Intent authIntent) {
          Debug.log("handleAuthIntent inside update Service.  not handled here");
        }
      });

      result = helper.channelInfo(channelIDs);

      // save in the db if we got results
      if (result.size() > 0) {
        database.insertItems(result);
      }
    }

    return result;
  }

  private List<String> channelIds(Context context, int channels_array_resource) {
    String[] channels = context.getResources().getStringArray(channels_array_resource);
    List<ChannelList.ChannelCode> channelCodes = new ArrayList<ChannelList.ChannelCode>();
    for (String c : channels) {
      channelCodes.add(ChannelList.ChannelCode.valueOf(c));
    }

    ArrayList<String> result = new ArrayList<String>();
    for (ChannelList.ChannelCode code : channelCodes)
      result.add(channelIDForCode(code));

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
      mChannelIDMap.put(ChannelCode.TECH_CRUNCH, "UCCjyq_K1Xwfg8Lndy7lKMpA");
      mChannelIDMap.put(ChannelCode.TWIT, "UCwY9B5_8QDGP8niZhBtTh8w");
      mChannelIDMap.put(ChannelCode.ENGADGET, "UC-6OW5aJYBFM33zXQlBKPNA");
      mChannelIDMap.put(ChannelCode.VSAUCE, "UC6nSFpj9HTCZ5t-N3Rm3-HA");
      mChannelIDMap.put(ChannelCode.SVB, "UCJLo-ihNo6sVMPvRzGVPRoQ");
    }

    return mChannelIDMap.get(code);
  }

  private static enum ChannelCode {NEURO_SOUP, KHAN_ACADEMY, VSAUCE, SVB, ENGADGET, TWIT, TECH_CRUNCH, YOUNG_TURKS, XDA, CONNECTIONS, CODE_ORG, JUSTIN_BIEBER, THE_VERGE, REASON_TV, BIG_THINK, ANDROID_DEVELOPERS, PEWDIEPIE, YOUTUBE, VICE, TOP_GEAR, COLLEGE_HUMOR, ROGAN, LUKITSCH, NERDIST, RT, JET_DAISUKE, MAX_KEISER, GATES_FOUNDATION}

}
