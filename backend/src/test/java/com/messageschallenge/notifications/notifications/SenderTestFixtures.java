package com.messageschallenge.notifications.notifications;

import com.messageschallenge.notifications.config.AppProperties;
import java.util.List;

final class SenderTestFixtures {

  private SenderTestFixtures() {}

  static AppProperties appPropertiesWithDefaults() {
    return new AppProperties(
        new AppProperties.Queue(5, List.of(500L, 2000L), 0.2, 4, "q", "dlq", 1000L, 5000L),
        new AppProperties.Sweeper(30_000L, 60_000L, 500),
        new AppProperties.Senders(
            new AppProperties.Senders.Sms("+10000000000"),
            new AppProperties.Senders.Email("no-reply@test"),
            new AppProperties.Senders.Push("app-test")),
        new AppProperties.Sse(1_800_000L),
        new AppProperties.Pagination(20, 100),
        new AppProperties.Errors("https://messages-challenge/errors/"));
  }
}
