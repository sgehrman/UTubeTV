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
import com.sickboots.sickvideos.misc.ColorPickerFragment;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.ToolbarIcons;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by sgehrman on 1/2/14.
 */
public class Content extends Observable {
  public static final String CONTENT_UPDATED_NOTIFICATION = "CONTENT_UPDATED";
  private ChannelList.ProductCode mProductCode;
  private YouTubeData mChannelInfo;
  private Context mContext;
  public ChannelList mChannelList;

  public Content(Context context, ChannelList.ProductCode code) {
    super();

    mProductCode = code;
    mChannelList = new ChannelList(mProductCode);

    mContext = context.getApplicationContext();

    askYouTubeForChannelInfo(false);
  }

  public ArrayList<Map> drawerTitles() {
    ArrayList<Map> result = new ArrayList<Map>();

    switch (mProductCode) {
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

    switch (mProductCode) {
      default:
        switch (index) {
          case 0:
            fragment = new ChannelAboutFragment();
            break;
          case 1:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.playlistsRequest(mChannelList.channelID(), "Playlists", null, 250));
            break;
          case 2:
            fragment = YouTubeGridFragment.newInstance(YouTubeServiceRequest.relatedRequest(YouTubeAPI.RelatedPlaylistType.UPLOADS, mChannelList.channelID(), "Videos", "Recent Uploads", 50));
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
          database.deleteAllRows(mChannelList.channelID());
        } else {
          List<YouTubeData> items = database.getItems(0, mChannelList.channelID(), 1);

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

          final Map fromYouTubeMap = helper.channelInfo(mChannelList.channelID());

          // save in the db if we got results
          if (fromYouTubeMap.size() > 0) {
            channelInfo = new YouTubeData();
            channelInfo.mThumbnail = (String) fromYouTubeMap.get("thumbnail");
            channelInfo.mTitle = (String) fromYouTubeMap.get("title");
            channelInfo.mDescription = (String) fromYouTubeMap.get("description");
            channelInfo.mChannel = mChannelList.channelID();

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
