package com.messageschallenge.notifications.queue;

import java.time.Duration;

public interface NotificationQueueConsumer {

  NotificationJob poll(Duration timeout) throws InterruptedException;
}
