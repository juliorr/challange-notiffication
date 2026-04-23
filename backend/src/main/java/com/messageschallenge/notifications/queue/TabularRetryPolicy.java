package com.messageschallenge.notifications.queue;

import com.messageschallenge.notifications.config.AppProperties;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class TabularRetryPolicy implements RetryPolicy {

  private final List<Long> baseDelaysMs;
  private final double jitterPct;
  private final int maxAttempts;

  public TabularRetryPolicy(AppProperties props) {
    this.baseDelaysMs = props.queue().retryDelaysMs();
    this.jitterPct = props.queue().jitterPct();
    this.maxAttempts = props.queue().maxAttempts();
  }

  @Override
  public Duration delayFor(int attempt) {
    long base = baseDelaysMs.get(Math.min(attempt - 1, baseDelaysMs.size() - 1));
    double factor = 1.0 + ThreadLocalRandom.current().nextDouble(-jitterPct, jitterPct);
    return Duration.ofMillis(Math.max(1, (long) (base * factor)));
  }

  @Override
  public boolean isExhausted(int attempt) {
    return attempt >= maxAttempts;
  }
}
