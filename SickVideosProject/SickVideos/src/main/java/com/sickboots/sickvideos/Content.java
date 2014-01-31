package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.Context;

import com.google.common.collect.ImmutableMap;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.ChannelAboutFragment;
import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;

/**
 * Created by sgehrman on 1/2/14.
 */
public class Content extends Observable {
  public static final String CONTENT_UPDATED_NOTIFICATION = "CONTENT_UPDATED";
  private ChannelList.ChannelCode mChannelCode;
  private Context mContext;
  public ChannelList mChannelList;

  public Content(Context context, ChannelList.ChannelCode code) {
    super();

    mChannelCode = code;
    mChannelList = new ChannelList(context, mChannelCode, new ChannelList.OnChannelListUpdateListener() {
      @Override
      public void onUpdate() {
        notifyForDataUpdate();
      }
    });

    mContext = context.getApplicationContext();
  }

  public ArrayList<Map> drawerTitles() {
    ArrayList<Map> result = new ArrayList<Map>();

    switch (mChannelCode) {
      default:
        result.add(ImmutableMap.of("title", "About", "icon", ToolbarIcons.IconID.ABOUT));
        result.add(ImmutableMap.of("title", "Playlists", "icon", ToolbarIcons.IconID.PLAYLISTS));
        result.add(ImmutableMap.of("title", "Recent Uploads", "icon", ToolbarIcons.IconID.UPLOADS));
        break;
    }

    return result;
  }

  public void changeChannel(int index) {
    mChannelList.changeChannel(index);

    notifyForDataUpdate();
  }

  public Fragment fragmentForIndex(int index) {
    Fragment fragment = null;

    switch (mChannelCode) {
      default:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment();
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(mChannelList.currentChannelId(), null, 250));
            break;
          case 2:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, mChannelList.currentChannelId(), null, 50));
            break;
        }
        break;
    }

    return fragment;
  }

  public boolean needsChannelSwitcher() {
    return mChannelList.needsChannelSwitcher();
  }

  private void notifyForDataUpdate() {
    setChanged();
    notifyObservers(Content.CONTENT_UPDATED_NOTIFICATION);
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

}
