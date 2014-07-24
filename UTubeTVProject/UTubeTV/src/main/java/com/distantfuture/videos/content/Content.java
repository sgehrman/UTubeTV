package com.distantfuture.videos.content;

import android.app.Fragment;
import android.content.Context;

import com.distantfuture.videos.R;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.imageutils.ToolbarIcons;
import com.distantfuture.videos.mainactivity.ChannelAboutFragment;
import com.distantfuture.videos.mainactivity.YouTubeGridFragment;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.DUtils;
import com.distantfuture.videos.services.ListServiceRequest;
import com.distantfuture.videos.youtube.YouTubeAPI;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Content {
  private static Content instance;
  private Context mContext;
  private ChannelList mChannelList;

  private Content(Context context) {
    super();

    mChannelList = new ChannelList(context, R.array.content_array);

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
      DUtils.log("Content instance null");

    return instance;
  }

  public ArrayList<Map> drawerTitles() {
    ArrayList<Map> result = new ArrayList<Map>();

    result.add(ImmutableMap.of("title", "About Channel", "icon", ToolbarIcons.IconID.ABOUT));
    result.add(ImmutableMap.of("title", "Recent Uploads", "icon", ToolbarIcons.IconID.UPLOADS));
    result.add(ImmutableMap.of("title", "Playlists", "icon", ToolbarIcons.IconID.PLAYLISTS));
    result.add(ImmutableMap.of("title", "Liked by Channel", "icon", ToolbarIcons.IconID.THUMBS_UP));

    return result;
  }

  public void resetToDefaults() {
    mChannelList.resetToDefaults();
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
        fragment = YouTubeGridFragment.newInstance(ListServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, mChannelList
            .currentChannelId(), null, 50));
        break;
      case 2:
        fragment = YouTubeGridFragment.newInstance(ListServiceRequest.playlistsRequest(mChannelList
            .currentChannelId(), null, 150));
        break;
      case 3:
        fragment = YouTubeGridFragment.newInstance(ListServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.LIKES, mChannelList
            .currentChannelId(), null, 50));
        break;
    }

    if (fragment != null)
      AppUtils.instance(mContext).saveSectionIndexForChannel(index, mChannelList.currentChannelId());

    return fragment;
  }

  public boolean needsChannelSwitcher() {
    return mChannelList.needsChannelSwitcher();
  }

  public boolean supportsDonate() {
    String string = mContext.getText(R.string.supports_donate).toString();
    return !string.isEmpty();
  }

  public boolean supportsChannelEditing() {
    String string = mContext.getText(R.string.supports_channel_editing).toString();
    return !string.isEmpty();
  }

  public List<YouTubeData> channels() {
    return mChannelList.channels();
  }

  public int currentChannelIndex() {
    return mChannelList.currentChannelIndex();
  }

  public int savedSectionIndex() {
    return AppUtils.instance(mContext).savedSectionIndexForChannel(mChannelList.currentChannelId());
  }

  public YouTubeData currentChannelInfo() {
    return mChannelList.currentChannelInfo();
  }

  public String channelName() {
    String result = null;

    YouTubeData data = currentChannelInfo();
    if (data != null)
      result = data.mTitle;

    return result;
  }

  public void refreshChannelInfo() {
    mChannelList.refresh();
  }

  public boolean hasChannel(String channelId) {
    return mChannelList.hasChannel(channelId);
  }

  public boolean addChannel(String channelId) {
    return mChannelList.editChannel(channelId, true);
  }

  public boolean removeChannel(String channelId) {
    return mChannelList.editChannel(channelId, false);
  }

  public void replaceChannels(List<String> channels) {
    mChannelList.replaceChannels(channels);
  }
}
