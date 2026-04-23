package com.messageschallenge.notifications.queue;

import static org.assertj.core.api.Assertions.assertThat;

import com.messageschallenge.notifications.AbstractIntegrationTest;
import com.messageschallenge.notifications.config.AppProperties;
import com.messageschallenge.notifications.domain.Channel;
import com.messageschallenge.notifications.domain.Message;
import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.domain.User;
import com.messageschallenge.notifications.observability.NotificationMetrics;
import com.messageschallenge.notifications.repository.CategoryRepository;
import com.messageschallenge.notifications.repository.ChannelRepository;
import com.messageschallenge.notifications.repository.MessageRepository;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.repository.UserRepository;
import com.messageschallenge.notifications.service.NotificationDispatcher;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class PendingNotificationSweeperIT extends AbstractIntegrationTest {

  @Autowired NotificationRepository notifications;
  @Autowired MessageRepository messages;
  @Autowired UserRepository users;
  @Autowired CategoryRepository categories;
  @Autowired ChannelRepository channels;
  @Autowired AppProperties props;
  @Autowired EntityManager em;

  @Test
  @Transactional
  void sweepsStalePendingAndReenqueuesThroughDispatcher() {
    Long stale = insertPendingSms("stale body");
    Long fresh = insertPendingSms("fresh body");
    backdateCreatedAt(stale, 3600_000L);

    RecordingQueue queue = new RecordingQueue();
    NotificationDispatcher dispatcher = new NotificationDispatcher(queue);
    NotificationMetrics metrics = new NotificationMetrics(new SimpleMeterRegistry());
    PendingNotificationSweeper sweeper =
        new PendingNotificationSweeper(notifications, dispatcher, metrics, props);

    int requeued = sweeper.runOnce();

    assertThat(requeued).isEqualTo(1);
    assertThat(queue.jobs).extracting(NotificationJob::notificationId).containsExactly(stale);
    assertThat(queue.jobs).extracting(NotificationJob::notificationId).doesNotContain(fresh);
  }

  @Test
  @Transactional
  void doesNotTouchTerminalOrNonPendingRows() {
    Long sent = insertPendingSms("sent body");
    backdateCreatedAt(sent, 3600_000L);
    markSent(sent);

    RecordingQueue queue = new RecordingQueue();
    NotificationMetrics metrics = new NotificationMetrics(new SimpleMeterRegistry());
    PendingNotificationSweeper sweeper =
        new PendingNotificationSweeper(
            notifications, new NotificationDispatcher(queue), metrics, props);

    assertThat(sweeper.runOnce()).isZero();
    assertThat(queue.jobs).isEmpty();
  }

  private Long insertPendingSms(String body) {
    Channel sms = channels.findByCode("SMS").orElseThrow();
    User ana =
        users.findSubscribersWithChannelsByCategoryCode("SPORTS").stream()
            .filter(u -> u.getEmail().equals("ana@example.com"))
            .findFirst()
            .orElseThrow();
    Message m = messages.save(new Message(categories.findByCode("SPORTS").orElseThrow(), body));
    Map<String, Object> payload = new HashMap<>();
    payload.put("to", ana.getPhoneNumber());
    payload.put("body", body);
    return notifications.save(new Notification(m, ana, sms, payload)).getId();
  }

  private void backdateCreatedAt(Long id, long millisAgo) {
    String sql =
        "UPDATE notifications SET created_at = now() - (:ms || ' milliseconds')::interval"
            + " WHERE id = :id";
    em.createNativeQuery(sql).setParameter("ms", millisAgo).setParameter("id", id).executeUpdate();
    em.flush();
    em.clear();
  }

  private void markSent(Long id) {
    em.createNativeQuery("UPDATE notifications SET status = 'SENT' WHERE id = :id")
        .setParameter("id", id)
        .executeUpdate();
    em.flush();
    em.clear();
  }

  static class RecordingQueue implements NotificationQueueProducer {
    final List<NotificationJob> jobs = new CopyOnWriteArrayList<>();

    @Override
    public void enqueue(NotificationJob job) {
      jobs.add(job);
    }

    @Override
    public void enqueueDelayed(NotificationJob job, java.time.Duration delay) {
      jobs.add(job);
    }

    @Override
    public void deadLetter(DeadLetterRecord record) {}
  }
}
