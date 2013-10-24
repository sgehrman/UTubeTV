package com.sickboots.sickvideos;

import java.util.HashMap;

public class AppData {
  private static AppData instance = null;
  private HashMap data;

  private AppData(){
    data = new HashMap();
  }

  public static AppData getInstance(){
    if(instance == null) {
      instance = new AppData();
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