package com.distantfuture.videos.content;

import android.content.Context;

import com.distantfuture.videos.misc.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelSetStore {
  private Context context;
  private List<String> defaultChannelIds;
  private Map<ChannelCode, String> mChannelIDMap;

  public ChannelSetStore(Context context, int channels_array_resource) {
    super();

    this.context = context.getApplicationContext();

    defaultChannelIds = defaultChannelIds(context, channels_array_resource);
  }

  private List<String> defaultChannelIds(Context context, int channels_array_resource) {
    List<String> result = new ArrayList<String>();

    // get the hard coded defaults if nothing found in prefs above
    String[] defaultChannels = context.getResources().getStringArray(channels_array_resource);
    List<ChannelCode> channelCodes = new ArrayList<ChannelCode>();
    for (String c : defaultChannels) {
      channelCodes.add(ChannelCode.valueOf(c));
    }

    for (ChannelCode code : channelCodes)
      result.add(channelIDForCode(code));

    return result;
  }

  public boolean needsChannelSwitcher() {
    // using the default ids since a user could edit his list to one item
    // then relaunch and be unable to switch
    return defaultChannelIds.size() > 1;
  }

  public ChannelSet channelSet(String name) {
    List<String> result = AppUtils.instance(context).channelIds(name);

    if (result != null && result.size() > 0)
      return new ChannelSet(name, result);

    return new ChannelSet(null, defaultChannelIds);
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

