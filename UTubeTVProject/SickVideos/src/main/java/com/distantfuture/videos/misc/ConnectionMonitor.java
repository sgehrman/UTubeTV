package com.distantfuture.videos.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import de.greenrobot.event.EventBus;

public class ConnectionMonitor {
  Context mContext;
  ConnectivityManager mConnectivityManager;
  boolean mConnected = true;  // assume we have a connection, send event if not connected

  public ConnectionMonitor(Context context) {
    mContext = context.getApplicationContext();

    mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

    BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        boolean debug = false;

        if (debug) {
          boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
          String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
          boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

          DUtils.log("noConnectivity: " + (noConnectivity ? "true" : "false"));
          DUtils.log("reason: " + reason);
          DUtils.log("isFailover: " + (isFailover ? "true" : "false"));
        }

        boolean isConnected = hasNetworkConnection();
        if (mConnected != isConnected) {
          mConnected = isConnected;

          EventBus.getDefault().post(new BusEvents.ConnectionChanged());
        }
      }
    };

    mContext.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }

  public boolean hasNetworkConnection() {
    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    // could add this later (from dev sample)
    //    if (isConnected) {
    //      boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    //
    //      Debug.log("got wifi");
    //    }

    return isConnected;
  }
}
