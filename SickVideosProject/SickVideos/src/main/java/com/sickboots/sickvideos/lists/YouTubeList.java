package com.sickboots.sickvideos.lists;

import android.app.Activity;
import android.content.Intent;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.Auth;
import com.sickboots.sickvideos.youtube.YouTubeAPI;
import com.sickboots.sickvideos.youtube.YouTubeServiceRequest;

import java.util.ArrayList;
import java.util.List;

public abstract class YouTubeList {
  protected enum TaskType {USER_REFRESH, REFETCH, FIRSTLOAD}

  ;

  // subclasses must implement
  abstract public void refresh();

  abstract public void refetch();

  abstract protected void loadData(TaskType taskType);

  abstract public void updateItem(YouTubeData itemMap);

  protected UIAccess access;
  protected YouTubeServiceRequest listSpec;
  protected static final int REQUEST_AUTHORIZATION = 444;
  protected List<YouTubeData> items = new ArrayList<YouTubeData>();

  // use accessor in subclasses
  private YouTubeAPI youTubeHelper;

  public YouTubeList(YouTubeServiceRequest s, UIAccess a) {
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

  public YouTubeServiceRequest.RequestType type() {
    return listSpec.type();
  }

  public String name() {
    return listSpec.name();
  }

  protected YouTubeAPI youTubeHelper() {
    if (youTubeHelper == null) {
      GoogleAccountCredential credential = Auth.getCredentials(access.getContext());

      if (credential != null) {
        youTubeHelper = new YouTubeAPI(access.getContext());
      }
    }

    return youTubeHelper;
  }

}
