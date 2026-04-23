package com.messageschallenge.notifications.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {

  private final MeterRegistry registry;
  private final ConcurrentMap<String, Counter> sent = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Counter> failed = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Counter> dlq = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Timer> duration = new ConcurrentHashMap<>();
  private final Counter requeued;

  public NotificationMetrics(MeterRegistry registry) {
    this.registry = registry;
    this.requeued =
        Counter.builder("notifications.requeued")
            .description("PENDING notifications re-enqueued by the sweeper")
            .register(registry);
  }

  public void recordSent(String channel) {
    sent.computeIfAbsent(channel, this::buildSent).increment();
  }

  public void recordFailed(String channel) {
    failed.computeIfAbsent(channel, this::buildFailed).increment();
  }

  public void recordDeadLetter(String channel) {
    dlq.computeIfAbsent(channel, this::buildDlq).increment();
  }

  public void recordDuration(String channel, Duration d) {
    duration.computeIfAbsent(channel, this::buildDuration).record(d);
  }

  public void recordRequeued(int count) {
    if (count > 0) {
      requeued.increment(count);
    }
  }

  private Counter buildSent(String channel) {
    return Counter.builder("notifications.sent")
        .description("Notifications successfully sent")
        .tag("channel", channel)
        .register(registry);
  }

  private Counter buildFailed(String channel) {
    return Counter.builder("notifications.failed")
        .description("Notification send attempts that failed (still retriable)")
        .tag("channel", channel)
        .register(registry);
  }

  private Counter buildDlq(String channel) {
    return Counter.builder("notifications.dlq")
        .description("Notifications moved to the dead-letter queue")
        .tag("channel", channel)
        .register(registry);
  }

  private Timer buildDuration(String channel) {
    return Timer.builder("notifications.send.duration")
        .description("Time spent in ChannelSender.send()")
        .tag("channel", channel)
        .register(registry);
  }
}
