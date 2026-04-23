package com.messageschallenge.notifications.queue;

import java.time.Duration;

public interface RetryPolicy {

  Duration delayFor(int attempt);

  boolean isExhausted(int attempt);
}
