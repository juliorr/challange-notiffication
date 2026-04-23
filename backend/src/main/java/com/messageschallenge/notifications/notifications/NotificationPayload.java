package com.messageschallenge.notifications.notifications;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record NotificationPayload(Map<String, Object> data) {

  public NotificationPayload {

    data = data == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(data));
  }

  public String getString(String key) {
    Object v = data.get(key);
    return v == null ? null : v.toString();
  }
}
