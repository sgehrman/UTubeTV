package com.sickboots.sickvideos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgehrman on 1/30/14.
 */
public class ChannelList {
  private List<Channel> mChannels;
  private Map<ProductCode, String> mChannelIDMap;

  public ChannelList(ChannelList.ProductCode code) {
    super();

    mChannels = new ArrayList<Channel>();

    mChannels.add(new Channel(code.toString(), channelIDForCode(code), ""));
    mChannels.add(new Channel(ProductCode.VICE.toString(), channelIDForCode(ProductCode.VICE), ""));
    mChannels.add(new Channel(ProductCode.ROGAN.toString(), channelIDForCode(ProductCode.ROGAN), ""));

  }

  String[] titles() {
    List<String> result = new ArrayList<String>();

    for (Channel channel : mChannels) {
      result.add(channel.mTitle);
    }

    return result.toArray(new String[0]);
  }

  public String channelID() {
    return mChannels.get(0).mChannelId;
  }

  private String channelIDForCode(ProductCode code) {
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

    return mChannelIDMap.get(code);
  }

  public static enum ProductCode {NEURO_SOUP, KHAN_ACADEMY, YOUNG_TURKS, XDA, CONNECTIONS, CODE_ORG, JUSTIN_BIEBER, THE_VERGE, REASON_TV, BIG_THINK, ANDROID_DEVELOPERS, PEWDIEPIE, YOUTUBE, VICE, TOP_GEAR, COLLEGE_HUMOR, ROGAN, LUKITSCH, NERDIST, RT, JET_DAISUKE, MAX_KEISER, GATES_FOUNDATION}

  public static class Channel {
    String mTitle;
    String mChannelId;
    String mThumbnail;

    public Channel(String title, String channelId, String thumbnail) {
      super();

      mTitle = title;
      mChannelId = channelId;
      mThumbnail = thumbnail;

    }
  }


}
