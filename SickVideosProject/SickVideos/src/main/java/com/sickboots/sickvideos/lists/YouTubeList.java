package com.sickboots.sickvideos.lists;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.youtube.GoogleAccount;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class YouTubeList implements GoogleAccount.GoogleAccountDelegate, YouTubeAPI.YouTubeHelperListener {

  // subclasses must implement
  abstract public void updateHighestDisplayedIndex(int position);

  abstract public void refresh();

  abstract protected void loadData(boolean askUser);

  abstract public void handleClick(Map itemMap, boolean clickedIcon);

  protected UIAccess access;
  protected GoogleAccount account;
  protected YouTubeListSpec listSpec;
  protected static final int REQUEST_AUTHORIZATION = 444;
  protected List<Map> items = new ArrayList<Map>();

  // use accessor in subclasses
  private YouTubeAPI youTubeHelper;

  public YouTubeList(YouTubeListSpec s, UIAccess a) {
    super();

    listSpec = s;
    access = a;
    account = GoogleAccount.newYouTube(this);
  }

  public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
    boolean handled = false;

    if (!handled) {
      switch (requestCode) {
        case REQUEST_AUTHORIZATION:
          if (resultCode != Activity.RESULT_OK) {
            loadData(true);
          }

          handled = true;
          break;
      }
    }

    return handled;
  }

  public List<Map> getItems() {
    return items;
  }

  public YouTubeListSpec.ListType type() {
    return listSpec.type;
  }

  public String name() {
    return listSpec.name();
  }

  protected YouTubeAPI youTubeHelper(boolean askUser) {
    if (youTubeHelper == null) {
      GoogleAccountCredential credential = account.credential(askUser);

      if (credential != null) {
        youTubeHelper = new YouTubeAPI(credential, this);
      }
    }

    return youTubeHelper;
  }

  // =================================================================================
  // YouTubeHelperListener

  @Override
  public void handleAuthIntent(Intent intent) {
    Util.toast(getActivity(), "Need Authorization");

    Fragment f = access.fragment();

    // start intent asking the user to authorize the app for google api
    f.startActivityForResult(intent, REQUEST_AUTHORIZATION);
  }

  @Override
  public void handleExceptionMessage(String message) {
    Util.toast(getActivity(), message);
  }

  // =================================================================================

  @Override   // in GoogleAccountDelegate
  public void credentialIsReady() {
    loadData(false);
  }

  @Override
  public Activity getActivity() {
    return access.fragment().getActivity();
  }

}
