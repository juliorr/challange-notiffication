package com.messageschallenge.notifications.queue;

import com.messageschallenge.notifications.config.AppProperties;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.web.dto.NotificationView;
import com.messageschallenge.notifications.web.sse.SseBroadcaster;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@ConditionalOnProperty(
    prefix = "app.queue",
    name = "worker-enabled",
    havingValue = "true",
    matchIfMissing = true)
@Component
public class NotificationQueueWorker {

  private static final Logger log = LoggerFactory.getLogger(NotificationQueueWorker.class);

  private final NotificationQueueProducer queue;
  private final NotificationQueueConsumer consumer;
  private final NotificationProcessor processor;
  private final NotificationRepository repo;
  private final SseBroadcaster sse;
  private final int threads;
  private final Duration pollTimeout;
  private final long shutdownTimeoutMs;
  private final AtomicBoolean running = new AtomicBoolean(false);

  private ExecutorService executor;

  public NotificationQueueWorker(
      NotificationQueueProducer queue,
      NotificationQueueConsumer consumer,
      NotificationProcessor processor,
      NotificationRepository repo,
      SseBroadcaster sse,
      AppProperties props) {
    this.queue = queue;
    this.consumer = consumer;
    this.processor = processor;
    this.repo = repo;
    this.sse = sse;
    this.threads = props.queue().workerThreads();
    this.pollTimeout = Duration.ofMillis(props.queue().pollTimeoutMs());
    this.shutdownTimeoutMs = props.queue().shutdownTimeoutMs();
  }

  @PostConstruct
  public void start() {
    startInternal();
  }

  private synchronized void startInternal() {
    if (running.get()) {
      return;
    }
    running.set(true);
    executor =
        Executors.newFixedThreadPool(
            threads,
            r -> {
              Thread t = new Thread(r, "notif-worker");
              t.setDaemon(true);
              return t;
            });
    for (int i = 0; i < threads; i++) {
      executor.submit(this::consumeLoop);
    }
    log.info("NotificationQueueWorker started with {} threads", threads);
  }

  @PreDestroy
  public void stop() {
    stopInternal();
  }

  private synchronized void stopInternal() {
    if (!running.get() && executor == null) {
      return;
    }
    running.set(false);
    if (executor != null) {
      executor.shutdownNow();
      try {
        executor.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      executor = null;
    }
    log.info("NotificationQueueWorker stopped");
  }

  public synchronized void pause() {
    stopInternal();
  }

  public synchronized void resume() {
    startInternal();
  }

  public boolean isRunning() {
    return running.get();
  }

  public int threads() {
    return threads;
  }

  private void consumeLoop() {
    while (running.get() && !Thread.currentThread().isInterrupted()) {
      try {
        NotificationJob job = consumer.poll(pollTimeout);
        if (job == null) continue;
        handle(job);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        return;
      } catch (RuntimeException ex) {
        log.error("Worker loop error (continuing)", ex);
      }
    }
  }

  void handle(NotificationJob job) {
    NotificationProcessor.Result r = processor.process(job);
    switch (r.outcome()) {
      case RETRY -> queue.enqueueDelayed(job.next(), r.retryDelay());
      case DEAD_LETTER ->
          queue.deadLetter(
              DeadLetterRecord.of(job, r.channel(), r.error(), r.cause(), Instant.now()));
      case MISSING -> {}
      case SUCCESS -> {}
    }
    if (r.outcome() != NotificationProcessor.Outcome.MISSING) {
      publishToSse(job.notificationId());
    }
  }

  private void publishToSse(Long notificationId) {
    repo.findByIdWithAssociations(notificationId)
        .ifPresent(n -> sse.publish(NotificationView.from(n)));
  }
}
