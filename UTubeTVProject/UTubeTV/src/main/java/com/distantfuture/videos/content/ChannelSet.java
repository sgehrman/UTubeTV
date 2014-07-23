package com.distantfuture.videos.content;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ChannelSet {
  private List<String> channelIds;
  private String name;  // null is default set

  public ChannelSet(String name, List<String> channelIds) {
    super();

    setName(name);
    setChannelIds(channelIds);
  }

  public boolean hasChannel(String channelId) {
    return channelIds.contains(channelId);
  }

  public boolean editChannel(Context context, String channelId, boolean addChannel) {
    boolean modifiedList = false;

    if (addChannel) {
      if (!channelIds.contains(channelId)) {
        channelIds.add(channelId);
        modifiedList = true;
      }
    } else {
      // don't allow removing the last channel
      if (channelIds.size() > 1) {
        if (channelIds.contains(channelId)) {

          channelIds.remove(channelId);
          modifiedList = true;
        }
      }
    }

    if (modifiedList) {
      ChannelSetManager.saveChannelSet(context, this);
    }

    return modifiedList;
  }

  public String get(int index) {
    return channelIds.get(index);
  }

  public List<String> getChannelIds() {
    return channelIds;
  }

  private void setChannelIds(List<String> channelIds) {
    // copy the array so we don't modify the original, could be default list
    channelIds = new ArrayList<String>(channelIds);

    this.channelIds = channelIds;
  }

  public String getName() {
    return name;
  }

  private void setName(String name) {
    this.name = name;
  }
}

