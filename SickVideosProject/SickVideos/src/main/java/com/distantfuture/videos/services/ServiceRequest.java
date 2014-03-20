package com.distantfuture.videos.services;

import android.os.Bundle;

import com.distantfuture.videos.misc.DUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceRequest {
  public static final String REQUEST_CLASS_TYPE_KEY = "rct_key";
  private HashMap<String, Object> map;

  protected ServiceRequest() {
    super();

    map = new HashMap();
  }

  public static Bundle toBundle(ServiceRequest request) {
    Bundle result = new Bundle();

    for (Map.Entry<String, Object> entry : request.map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (value instanceof String)
        result.putString(key, (String) value);
      else if (value instanceof Integer)
        result.putInt(key, (Integer) value);
      else if (value instanceof Long)
        result.putLong(key, (Long) value);
      else if (value != null)  // null is OK, so don't flag that
        DUtils.log("you fd up : " + value.getClass().toString());
    }

    return result;
  }

  public static ServiceRequest fromBundle(Bundle bundle) {
    ServiceRequest result = new ServiceRequest();

    Set<String> keySet = bundle.keySet();
    for (String key : keySet) {
      Object value = bundle.get(key);

      result.map.put(key, value);
    }

    return result;
  }

  protected Object getData(String key) {
    Object result = null;

    if (map.containsKey(key))
      result = map.get(key);

    return result;
  }

  protected void putString(String key, String value) {
    map.put(key, value);
  }

  protected void putInt(String key, int value) {
    map.put(key, value);
  }
}
