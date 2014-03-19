package com.distantfuture.videos.content;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.distantfuture.videos.database.DatabaseAccess;
import com.distantfuture.videos.database.DatabaseTables;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.BusEvents;
import com.distantfuture.videos.misc.DUtils;
import com.distantfuture.videos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ChannelList {

  private List<YouTubeData> mChannels;
  private ChannelSetManager mChannelSetStore;
  private ChannelSet mChannelSet;
  private String mCurrentChannelID;
  private Context mContext;

  public ChannelList(Context context, int channels_array_resource) {
    super();

    mContext = context.getApplicationContext();

    mChannelSetStore = new ChannelSetManager(context, channels_array_resource);
    mChannelSet = mChannelSetStore.channelSet();
    mCurrentChannelID = AppUtils.instance(mContext).defaultChannelID(mChannelSet.get(0));

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

    DUtils.log("should not get here: " + DUtils.currentMethod());
    return 0;
  }

  public void resetToDefaults() {
    mChannelSetStore.resetToDefaults();
    mChannelSet = mChannelSetStore.channelSet();

    requestChannelInfo(false);
  }

  public String currentChannelId() {
    return mCurrentChannelID;
  }

  private void setCurrentChannelId(String channelId) {
    mCurrentChannelID = channelId;
    AppUtils.instance(mContext).saveDefaultChannelID(mCurrentChannelID);
  }

  public boolean needsChannelSwitcher() {
    return mChannelSetStore.needsChannelSwitcher();
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
      setCurrentChannelId(mChannels.get(index).mChannel);

      return true;
    }

    return false;
  }

  public boolean editChannel(String channelId, boolean addChannel) {
    boolean modifiedList = mChannelSet.editChannel(mContext, channelId, addChannel);

    if (modifiedList) {
      if (TextUtils.equals(mCurrentChannelID, channelId))
        setCurrentChannelId(mChannelSet.get(0));

      // refresh data
      requestChannelInfo(false);
    }

    return modifiedList;
  }

  public void replaceChannels(List<String> channels) {
    mChannelSet = mChannelSetStore.channelSet(channels);

    setCurrentChannelId(mChannelSet.get(0));

    // refresh data
    requestChannelInfo(false);
  }

  public boolean hasChannel(String channelId) {
    return mChannelSet.hasChannel(channelId);
  }

  private void requestChannelInfo(final boolean refresh) {
    (new Thread(new Runnable() {
      public void run() {
        List<YouTubeData> channels = new ArrayList<YouTubeData>();
        final List<String> needToAskYouTube = new ArrayList<String>();
        DatabaseAccess database = new DatabaseAccess(mContext, DatabaseTables.channelTable());

        List<String> channelIds = mChannelSet.getChannelIds();
        for (String channelId : channelIds) {
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
          DUtils.log("handleAuthIntent inside update Service.  not handled here");
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

}
