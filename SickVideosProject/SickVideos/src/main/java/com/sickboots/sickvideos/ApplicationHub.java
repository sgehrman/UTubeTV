package com.sickboots.sickvideos;

import java.util.HashMap;

public class ApplicationHub {
  private static ApplicationHub instance = null;
  private HashMap data;

  private ApplicationHub() {
    data = new HashMap();
  }

  public static ApplicationHub getInstance() {
    if (instance == null) {
      instance = new ApplicationHub();
    }
    return instance;
  }

  public Object getData(String key) {
    return data.get(key);
  }

  public void setData(String key, Object value) {
    data.put(key, value);
  }
}