package com.sickboots.sickvideos;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.common.collect.ImmutableMap;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.DatabaseTables;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.ChannelAboutFragment;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by sgehrman on 1/2/14.
 */
public class Content extends Observable {
  public static final String CONTENT_UPDATED_NOTIFICATION = "CONTENT_UPDATED";
  private ChannelList.ChannelCode mChannelCode;
  private YouTubeData mChannelInfo;
  private Context mContext;
  public ChannelList mChannelList;

  public Content(Context context, ChannelList.ChannelCode code) {
    super();

    mChannelCode = code;
    mChannelList = new ChannelList(context, mChannelCode);

    mContext = context.getApplicationContext();

    askYouTubeForChannelInfo(false);
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

  public Fragment fragmentForIndex(int index) {
    Fragment fragment = null;

    switch (mChannelCode) {
      default:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment();
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(mChannelList.currentChannel(), "Playlists", null, 250));
            break;
          case 2:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, mChannelList.currentChannel(), "Videos", "Recent Uploads", 50));
            break;
        }
        break;
    }

    return fragment;
  }

  private void notifyForDataUpdate() {
    setChanged();
    notifyObservers(Content.CONTENT_UPDATED_NOTIFICATION);
  }

  public YouTubeData channelInfo() {
    return mChannelInfo;
  }

  public void refreshChannelInfo() {
    askYouTubeForChannelInfo(true);
  }

  private void askYouTubeForChannelInfo(final boolean refresh) {
    (new Thread(new Runnable() {
      public void run() {
        YouTubeData channelInfo = null;
        DatabaseAccess database = new DatabaseAccess(mContext, DatabaseTables.channelTable());

        // if refreshing, don't get from database (need to remove existing data?)
        if (refresh) {
          database.deleteAllRows(mChannelList.currentChannel());
        } else {
          List<YouTubeData> items = database.getItems(0, mChannelList.currentChannel(), 1);

          if (items.size() > 0)
            channelInfo = items.get(0);
        }

        if (channelInfo == null) {
          YouTubeAPI helper = new YouTubeAPI(mContext, new YouTubeAPI.YouTubeAPIListener() {
            @Override
            public void handleAuthIntent(final Intent authIntent) {
              Debug.log("handleAuthIntent inside update Service.  not handled here");
            }
          });

          final Map fromYouTubeMap = helper.channelInfo(mChannelList.currentChannel());

          // save in the db if we got results
          if (fromYouTubeMap.size() > 0) {
            channelInfo = new YouTubeData();
            channelInfo.mThumbnail = (String) fromYouTubeMap.get("thumbnail");
            channelInfo.mTitle = (String) fromYouTubeMap.get("title");
            channelInfo.mDescription = (String) fromYouTubeMap.get("description");
            channelInfo.mChannel = mChannelList.currentChannel();

            database.insertItems(Arrays.asList(channelInfo));
          }
        }

        final YouTubeData newChannelInfo = channelInfo;

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
          @Override
          public void run() {
            // we are on the main thread, set the new data and send out notifications
            mChannelInfo = newChannelInfo;
            notifyForDataUpdate();
          }
        });
      }
    })).start();
  }


}
