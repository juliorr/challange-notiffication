package com.messageschallenge.notifications.queue;

import com.messageschallenge.notifications.config.AppProperties;
import com.messageschallenge.notifications.domain.NotificationStatus;
import com.messageschallenge.notifications.observability.NotificationMetrics;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.service.NotificationDispatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(
    prefix = "app.sweeper",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class PendingNotificationSweeper {

  private static final Logger log = LoggerFactory.getLogger(PendingNotificationSweeper.class);

  private final NotificationRepository repo;
  private final NotificationDispatcher dispatcher;
  private final NotificationMetrics metrics;
  private final AppProperties.Sweeper props;

  public PendingNotificationSweeper(
      NotificationRepository repo,
      NotificationDispatcher dispatcher,
      NotificationMetrics metrics,
      AppProperties appProps) {
    this.repo = repo;
    this.dispatcher = dispatcher;
    this.metrics = metrics;
    this.props = appProps.sweeper();
  }

  @Scheduled(
      fixedDelayString = "${app.sweeper.interval-ms}",
      initialDelayString = "${app.sweeper.interval-ms}")
  public void sweep() {
    int requeued = runOnce();
    if (requeued > 0) {
      log.warn("sweeper_requeued_pending count={}", requeued);
    }
  }

  public int runOnce() {
    Instant threshold = Instant.now().minus(Duration.ofMillis(props.minAgeMs()));
    List<Long> ids =
        repo.findStaleIdsByStatus(
            NotificationStatus.PENDING, threshold, PageRequest.of(0, props.batchSize()));
    if (ids.isEmpty()) {
      return 0;
    }
    dispatcher.dispatchAll(ids);
    metrics.recordRequeued(ids.size());
    return ids.size();
  }
}
