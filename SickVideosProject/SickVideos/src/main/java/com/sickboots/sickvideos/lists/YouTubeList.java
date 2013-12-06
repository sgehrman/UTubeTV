package com.sickboots.sickvideos.lists;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.sickboots.sickvideos.DrawerActivity;
import com.sickboots.sickvideos.MainActivity;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Auth;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.List;

public abstract class YouTubeList implements YouTubeAPI.YouTubeHelperListener {
  protected enum TaskType {USER_REFRESH, REFETCH, FIRSTLOAD}

  ;

  // subclasses must implement
  abstract public void updateHighestDisplayedIndex(int position);

  abstract public void refresh();

  abstract protected void loadData(TaskType taskType);

  abstract public void updateItem(YouTubeData itemMap);

  protected UIAccess access;
  protected YouTubeListSpec listSpec;
  protected static final int REQUEST_AUTHORIZATION = 444;
  protected List<YouTubeData> items = new ArrayList<YouTubeData>();

  // use accessor in subclasses
  private YouTubeAPI youTubeHelper;

  public YouTubeList(YouTubeListSpec s, UIAccess a) {
    super();

    listSpec = s;
    access = a;
  }

  // owning fragment calls this
  public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
    boolean handled = false;

    if (!handled) {
      switch (requestCode) {
        case REQUEST_AUTHORIZATION:
          if (resultCode != Activity.RESULT_OK) {
            loadData(TaskType.FIRSTLOAD);
          }

          handled = true;
          break;
      }
    }

    return handled;
  }

  public List<YouTubeData> getItems() {
    return items;
  }

  public YouTubeListSpec.ListType type() {
    return listSpec.type;
  }

  public String name() {
    return listSpec.name();
  }

  protected YouTubeAPI youTubeHelper() {
    if (youTubeHelper == null) {
      GoogleAccountCredential credential = Auth.getCredentials(access.getContext());

      if (credential != null) {
        youTubeHelper = new YouTubeAPI(credential, this);
      }
    }

    return youTubeHelper;
  }

  // =================================================================================
  // YouTubeHelperListener

  private static void requestAuth(Context context, Intent authIntent) {
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
    Intent runReqAuthIntent = new Intent(MainActivity.REQUEST_AUTHORIZATION_INTENT);
    runReqAuthIntent.putExtra(MainActivity.REQUEST_AUTHORIZATION_INTENT_PARAM, authIntent);
    manager.sendBroadcast(runReqAuthIntent);
    Util.log(String.format("Sent broadcast %s", MainActivity.REQUEST_AUTHORIZATION_INTENT));
  }


  @Override
  public void handleAuthIntent(Intent intent) {
    Util.toast(access.getContext(), "Need Authorization");

    Fragment f = access.fragment();
    if (f != null) {
      requestAuth(access.getContext(), intent);
    } else
      Util.log("Activity is null in handleAuthIntent");
  }

  @Override
  public void handleExceptionMessage(String message) {
    Util.toast(access.getContext(), message);
  }

}
