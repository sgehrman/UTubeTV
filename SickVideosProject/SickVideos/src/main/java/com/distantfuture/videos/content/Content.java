package com.distantfuture.videos.content;

import android.app.Fragment;
import android.content.Context;

import com.distantfuture.videos.R;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.imageutils.ToolbarIcons;
import com.distantfuture.videos.mainactivity.ChannelAboutFragment;
import com.distantfuture.videos.mainactivity.YouTubeGridFragment;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.Events;
import com.distantfuture.videos.services.YouTubeServiceRequest;
import com.distantfuture.videos.youtube.YouTubeAPI;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class Content {
  private static Content instance;
  private Context mContext;
  private ChannelList mChannelList;

  private Content(Context context) {
    super();

    String[] channels = context.getResources().getStringArray(R.array.content_array);

    List<ChannelList.ChannelCode> channelCodes = new ArrayList<ChannelList.ChannelCode>();
    for (String c : channels) {
      channelCodes.add(ChannelList.ChannelCode.valueOf(c));
    }

    mChannelList = new ChannelList(context, channelCodes, new ChannelList.OnChannelListUpdateListener() {
      @Override
      public void onUpdate() {
        notifyForDataUpdate();
      }
    });

    mContext = context.getApplicationContext();
  }

  // called once early in main activity, but activity could get recreated, so checking for null
  public static Content instance(Context context) {
    if (instance == null)
      instance = new Content(context);

    return instance;
  }

  public static Content instance() {
    if (instance == null)
      Debug.log("Content instance null");

    return instance;
  }

  public ArrayList<Map> drawerTitles() {
    ArrayList<Map> result = new ArrayList<Map>();

    result.add(ImmutableMap.of("title", "About", "icon", ToolbarIcons.IconID.ABOUT));
    result.add(ImmutableMap.of("title", "Playlists", "icon", ToolbarIcons.IconID.PLAYLISTS));
    result.add(ImmutableMap.of("title", "Recent Uploads", "icon", ToolbarIcons.IconID.UPLOADS));

    return result;
  }

  // returns false if that channel is already current
  public boolean changeChannel(int index) {
    return mChannelList.changeChannel(index);
  }

  public Fragment fragmentForIndex(int index) {
    Fragment fragment = null;

    switch (index) {
      case 0:
        fragment = new ChannelAboutFragment();
        break;
      case 1:
        fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(mChannelList
            .currentChannelId(), null, 250));
        break;
      case 2:
        fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, mChannelList
            .currentChannelId(), null, 50));
        break;
    }

    if (fragment != null)
      AppUtils.instance(mContext).saveSectionIndex(index, mChannelList.currentChannelId());

    return fragment;
  }

  public boolean needsChannelSwitcher() {
    return mChannelList.needsChannelSwitcher();
  }

  public List<YouTubeData> channels() {
    return mChannelList.channels();
  }

  public int currentChannelIndex() {
    return mChannelList.currentChannelIndex();
  }

  public int savedSectionIndex() {
    return AppUtils.instance(mContext).savedSectionIndex(mChannelList.currentChannelId());
  }

  public YouTubeData channelInfo() {
    return mChannelList.currentChannelInfo();
  }

  public String channelName() {
    String result = null;

    YouTubeData data = channelInfo();
    if (data != null)
      result = data.mTitle;

    return result;
  }

  public void refreshChannelInfo() {
    mChannelList.refresh();
  }

  private void notifyForDataUpdate() {
    // this only happens once, so make it sticky
    EventBus.getDefault().post(new Events.ContentEvent());
  }

}
