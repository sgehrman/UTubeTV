package com.distantfuture.videos.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.distantfuture.videos.misc.DUtils;

import java.util.HashSet;
import java.util.Set;

public class YouTubeService extends IntentService {
  private Set mHasFetchedDataMap = new HashSet<String>();

  public YouTubeService() {
    super("YouTubeListService");
  }

  public static void startListRequest(Context context, ListServiceRequest request, boolean refresh) {
    startRequest(context, request.toBundle(), refresh);
  }

  public static void startSubscriptionRequest(Context context, SubscriptionsServiceRequest request) {
    startRequest(context, request.toBundle(), false);
  }

  public static void startRequest(Context context, Bundle requestBundle, boolean refresh) {
    context = context.getApplicationContext();

    Intent i = new Intent(context, YouTubeService.class);
    i.putExtra("request", requestBundle);
    i.putExtra("refresh", refresh);
    context.startService(i);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      ListServiceRequest listServiceRequest = ListServiceRequest.fromBundle(intent.getBundleExtra("request"));
      if (listServiceRequest != null) {
        boolean refresh = intent.getBooleanExtra("refresh", false);

        boolean hasFetchedData = mHasFetchedDataMap.contains(listServiceRequest.requestIdentifier());
        mHasFetchedDataMap.add(listServiceRequest.requestIdentifier());

        listServiceRequest.runTask(this, hasFetchedData, refresh);
      } else {
        SubscriptionsServiceRequest subscriptionsServiceRequest = SubscriptionsServiceRequest.fromBundle(intent.getBundleExtra("request"));

        if (subscriptionsServiceRequest != null) {
          subscriptionsServiceRequest.runTask(this);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      DUtils.log(String.format("%s exception: %s", DUtils.currentMethod(), e.getMessage()));
    }
  }
}