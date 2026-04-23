package com.messageschallenge.notifications.queue;

import java.time.Duration;

public interface NotificationQueueProducer {

  void enqueue(NotificationJob job);

  void enqueueDelayed(NotificationJob job, Duration delay);

  void deadLetter(DeadLetterRecord record);
}
