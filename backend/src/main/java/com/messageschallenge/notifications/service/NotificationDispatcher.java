package com.messageschallenge.notifications.service;

import com.messageschallenge.notifications.queue.NotificationJob;
import com.messageschallenge.notifications.queue.NotificationQueueProducer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher {

  private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

  private final NotificationQueueProducer queue;

  public NotificationDispatcher(NotificationQueueProducer queue) {
    this.queue = queue;
  }

  public void dispatchAll(List<Long> notificationIds) {
    for (Long id : notificationIds) {
      queue.enqueue(new NotificationJob(id, 1));
    }
    log.info("Dispatched {} notifications to the queue", notificationIds.size());
  }
}
