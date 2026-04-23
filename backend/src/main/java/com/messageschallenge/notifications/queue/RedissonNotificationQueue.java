package com.messageschallenge.notifications.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.messageschallenge.notifications.config.AppProperties;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RList;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.stereotype.Component;

@Component
public class RedissonNotificationQueue
    implements NotificationQueueProducer, NotificationQueueInspector, NotificationQueueConsumer {

  private final RBlockingQueue<NotificationJob> queue;
  private final RDelayedQueue<NotificationJob> delayed;
  private final RList<DeadLetterRecord> dlq;
  private final RScoredSortedSet<NotificationJob> delayedTimeoutSet;

  public RedissonNotificationQueue(RedissonClient redisson, AppProperties props) {
    this.queue = redisson.getBlockingQueue(props.queue().queueName());
    this.delayed = redisson.getDelayedQueue(queue);
    this.dlq = redisson.getList(props.queue().dlqName(), dlqCodec());

    this.delayedTimeoutSet =
        redisson.getScoredSortedSet(
            "redisson_delay_queue_timeout:{" + props.queue().queueName() + "}");
  }

  private static Codec dlqCodec() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.findAndRegisterModules();
    return new TypedJsonJacksonCodec(DeadLetterRecord.class, mapper);
  }

  @Override
  public void enqueue(NotificationJob job) {
    queue.offer(job);
  }

  @Override
  public void enqueueDelayed(NotificationJob job, Duration delay) {
    delayed.offer(job, delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void deadLetter(DeadLetterRecord record) {
    dlq.add(record);
  }

  @Override
  public NotificationJob poll(Duration timeout) throws InterruptedException {
    return queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public int size() {
    return queue.size();
  }

  @Override
  public int dlqSize() {
    return dlq.size();
  }

  @Override
  public int delayedSize() {
    try {
      return delayedTimeoutSet.size();
    } catch (RuntimeException ex) {
      return 0;
    }
  }

  @Override
  public List<JobSummary> peek(int n) {
    if (n <= 0 || queue.isEmpty()) {
      return List.of();
    }
    int to = Math.min(n, queue.size()) - 1;
    List<NotificationJob> range = queue.readAll().subList(0, to + 1);
    return range.stream().map(JobSummary::from).toList();
  }

  @Override
  public List<DlqEntrySummary> peekDlq(int n) {
    if (n <= 0 || dlq.isEmpty()) {
      return List.of();
    }
    int to = Math.min(n, dlq.size()) - 1;
    List<DeadLetterRecord> range = dlq.range(0, to);
    return range.stream().map(DlqEntrySummary::from).toList();
  }
}
