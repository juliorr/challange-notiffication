package com.messageschallenge.notifications.queue;

import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.notifications.ChannelSender;
import com.messageschallenge.notifications.notifications.ChannelSenderRegistry;
import com.messageschallenge.notifications.observability.NotificationMetrics;
import com.messageschallenge.notifications.repository.NotificationRepository;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationProcessor {

  private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

  private final NotificationRepository repo;
  private final ChannelSenderRegistry registry;
  private final RetryPolicy retryPolicy;
  private final NotificationMetrics metrics;

  public NotificationProcessor(
      NotificationRepository repo,
      ChannelSenderRegistry registry,
      RetryPolicy retryPolicy,
      NotificationMetrics metrics) {
    this.repo = repo;
    this.registry = registry;
    this.retryPolicy = retryPolicy;
    this.metrics = metrics;
  }

  @Transactional
  public Result process(NotificationJob job) {
    MDC.put("notificationId", String.valueOf(job.notificationId()));
    MDC.put("attempt", String.valueOf(job.attempt()));
    String channel = null;
    try {
      Notification n = repo.findById(job.notificationId()).orElse(null);
      if (n == null) {
        log.warn("Notification missing; ignoring job");
        return Result.missing();
      }
      MDC.put("messageId", String.valueOf(n.getMessage().getId()));
      if (n.isTerminal()) {
        log.info("Notification already terminal (status={}); idempotent skip", n.getStatus());
        return Result.success();
      }

      channel = n.getChannel().getCode();
      MDC.put("channel", channel);
      ChannelSender sender = registry.require(channel);

      n.markSending();
      long startNanos = System.nanoTime();
      ChannelSender.SendResult res = sender.send(n);
      metrics.recordDuration(channel, Duration.ofNanos(System.nanoTime() - startNanos));

      if (res.ok()) {
        n.markSent(res.providerId());
        metrics.recordSent(channel);
        log.info("Sent successfully providerId={}", res.providerId());
        return Result.success();
      }

      if (retryPolicy.isExhausted(job.attempt())) {
        n.markDeadLetter(res.error());
        metrics.recordDeadLetter(channel);
        log.warn("Retry budget exhausted at attempt {}; moved to DLQ", job.attempt());
        return Result.deadLetter(channel, res.error(), null);
      }

      n.markFailed(res.error());
      metrics.recordFailed(channel);
      Duration delay = retryPolicy.delayFor(job.attempt());
      log.warn("Send failed: {} — retrying in {}ms", res.error(), delay.toMillis());
      return Result.retry(delay);

    } catch (RuntimeException ex) {
      log.error("Processor caught exception", ex);
      if (retryPolicy.isExhausted(job.attempt())) {
        return Result.deadLetter(channel, ex.getMessage(), ex);
      }
      return Result.retry(retryPolicy.delayFor(job.attempt()));
    } finally {
      MDC.remove("notificationId");
      MDC.remove("attempt");
      MDC.remove("channel");
      MDC.remove("messageId");
    }
  }

  public enum Outcome {
    SUCCESS,
    RETRY,
    DEAD_LETTER,
    MISSING
  }

  public record Result(
      Outcome outcome, Duration retryDelay, String channel, String error, Throwable cause) {
    public static Result success() {
      return new Result(Outcome.SUCCESS, Duration.ZERO, null, null, null);
    }

    public static Result retry(Duration delay) {
      return new Result(Outcome.RETRY, delay, null, null, null);
    }

    public static Result deadLetter(String channel, String error, Throwable cause) {
      return new Result(Outcome.DEAD_LETTER, Duration.ZERO, channel, error, cause);
    }

    public static Result missing() {
      return new Result(Outcome.MISSING, Duration.ZERO, null, null, null);
    }
  }
}
