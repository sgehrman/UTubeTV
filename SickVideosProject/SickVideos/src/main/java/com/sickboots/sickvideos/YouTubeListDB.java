package com.sickboots.sickvideos;

import java.util.Map;

/**
 * Created by sgehrman on 11/4/13.
 */
public class YouTubeListDB extends YouTubeList {

  public YouTubeListDB(YouTubeListSpec s, UIAccess a) {
    super(s, a);
  }

  @Override
  public void updateHighestDisplayedIndex(int position) {
    // doi nothing - not used in DB list
  }

  @Override
  public void refresh() {
    // empty DB and refetch
  }

  @Override
  protected void loadData(boolean askUser) {
    YouTubeHelper helper = youTubeHelper(askUser);

    if (helper != null) {
    }
  }

  @Override
  public void handleClick(Map itemMap, boolean clickedIcon) {
    String movieID = (String) itemMap.get("video");

    if (movieID != null) {
      YouTubeHelper.playMovie(getActivity(), movieID);
    }

  }

}
