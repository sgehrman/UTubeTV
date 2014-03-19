package com.distantfuture.videos.services;

import android.os.Bundle;

/**
 * Created by sgehrman on 3/19/14.
 */
public class SubscriptionsServiceRequest {
  ServiceRequest serviceRequest;
  private static final int CLASS_TYPE_KEY = 5544;

  private SubscriptionsServiceRequest(ServiceRequest serviceRequest) {
    super();

    this.serviceRequest = serviceRequest;
  }

  public SubscriptionsServiceRequest() {
    super();

    serviceRequest = new ServiceRequest();
    serviceRequest.putInt(ServiceRequest.REQUEST_CLASS_TYPE_KEY, CLASS_TYPE_KEY);
  }

  public Bundle toBundle() {
    return ServiceRequest.toBundle(serviceRequest);
  }

  public static SubscriptionsServiceRequest fromServiceRequest(ServiceRequest request) {
    SubscriptionsServiceRequest result = null;

    Integer intValue = (Integer) request.getData(ServiceRequest.REQUEST_CLASS_TYPE_KEY);

    if (intValue != null) {
      if (intValue == CLASS_TYPE_KEY)
        result = new SubscriptionsServiceRequest(request);
    }

    return result;
  }
}
