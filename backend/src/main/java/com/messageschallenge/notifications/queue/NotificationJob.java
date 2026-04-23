package com.messageschallenge.notifications.queue;

import java.io.Serializable;

public record NotificationJob(Long notificationId, int attempt) implements Serializable {
  public NotificationJob next() {
    return new NotificationJob(notificationId, attempt + 1);
  }
}
