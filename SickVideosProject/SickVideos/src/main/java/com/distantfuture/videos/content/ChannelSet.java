package com.distantfuture.videos.content;

import android.content.Context;
import android.text.TextUtils;

import com.distantfuture.videos.misc.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      // saved list in prefs
      AppUtils.instance(context).saveChannelIds(name, channelIds);
    }

    return modifiedList;
  }

  public String get(int index) {
    return channelIds.get(index);
  }

  public List<String> getChannelIds() {
    return channelIds;
  }

  public void setChannelIds(List<String> channelIds) {
    this.channelIds = channelIds;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

