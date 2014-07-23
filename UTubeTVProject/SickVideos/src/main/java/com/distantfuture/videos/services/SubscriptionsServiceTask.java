package com.distantfuture.videos.services;

import android.content.Context;
import android.content.Intent;

import com.distantfuture.videos.activities.AuthActivity;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.BusEvents;
import com.distantfuture.videos.youtube.YouTubeAPI;

import java.util.List;

import de.greenrobot.event.EventBus;

public class SubscriptionsServiceTask {

  public SubscriptionsServiceTask(final Context context, final SubscriptionsServiceRequest request) {
    YouTubeAPI helper = new YouTubeAPI(context, true, false, new YouTubeAPI.YouTubeAPIListener() {
      @Override
      public void handleAuthIntent(final Intent authIntent) {
        AuthActivity.show(context, authIntent, request.toBundle());
      }
    });

    YouTubeAPI.SubscriptionListResults results = helper.subscriptionListResults(true);

    List<YouTubeData> items = results.getItems(0);

    List<String> channelIds = YouTubeData.contentIdsList(items);

    // notify that we handled an intent so pull to refresh can stop it's animation and other stuff
    EventBus.getDefault().post(new BusEvents.SubscriptionServiceResult(channelIds));
  }

}
