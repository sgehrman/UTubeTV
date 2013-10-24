package com.sickboots.sickvideos;

import java.util.HashMap;

public class YouTubeListCache {
  private static YouTubeListCache instance = null;
  private HashMap data;

  private YouTubeListCache(){
    data = new HashMap();
  }

  public static YouTubeListCache getInstance(){
    if(instance == null) {
      instance = new YouTubeListCache();
    }
    return instance;
  }

  public Object getData(String key) {
    return data.get(key);
  }

  public void setData(String key, Object value){
    data.put(key, value);
  }
}