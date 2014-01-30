package com.sickboots.sickvideos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sgehrman on 1/30/14.
 */
public class ChannelList {
  private List<Channel> mChannels;

  public ChannelList() {
    super();

    mChannels = new ArrayList<Channel>();

    mChannels.add(new Channel("title", "channelId", "thumbnail"));
  }

  String[] titles() {
    List<String> result = new ArrayList<String>();

    for (Channel channel : mChannels) {
      result.add(channel.mTitle);
    }

    return (String[]) result.toArray();
  }

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
