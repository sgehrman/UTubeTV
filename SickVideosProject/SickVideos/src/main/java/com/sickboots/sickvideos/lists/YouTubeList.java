package com.sickboots.sickvideos.lists;

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
  protected YouTubeServiceRequest mRequest;
  protected List<YouTubeData> items = new ArrayList<YouTubeData>();

  // use accessor in subclasses
  private YouTubeAPI youTubeHelper;

  public YouTubeList(YouTubeServiceRequest request, UIAccess a) {
    super();

    mRequest = request;
    access = a;
  }

  public List<YouTubeData> getItems() {
    return items;
  }

  public YouTubeServiceRequest.RequestType type() {
    return mRequest.type();
  }

  public String name() {
    return mRequest.name();
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
