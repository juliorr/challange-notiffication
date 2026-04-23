package com.messageschallenge.notifications.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Queue queue, Sweeper sweeper, Senders senders, Sse sse, Pagination pagination, Errors errors) {

  public record Queue(
      int maxAttempts,
      List<Long> retryDelaysMs,
      double jitterPct,
      int workerThreads,
      String queueName,
      String dlqName,
      long pollTimeoutMs,
      long shutdownTimeoutMs) {}

  public record Sweeper(long intervalMs, long minAgeMs, int batchSize) {}

  public record Senders(Sms sms, Email email, Push push) {
    public record Sms(String from) {}

    public record Email(String from) {}

    public record Push(String appId) {}
  }

  public record Sse(long timeoutMs) {}

  public record Pagination(int defaultSize, int maxSize) {}

  public record Errors(String detailsBaseUrl) {}
}
