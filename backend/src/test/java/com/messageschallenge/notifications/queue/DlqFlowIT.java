package com.messageschallenge.notifications.queue;

import static org.assertj.core.api.Assertions.assertThat;

import com.messageschallenge.notifications.AbstractIntegrationTest;
import com.messageschallenge.notifications.domain.Channel;
import com.messageschallenge.notifications.domain.Message;
import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.domain.NotificationStatus;
import com.messageschallenge.notifications.domain.User;
import com.messageschallenge.notifications.notifications.EmailMockSender;
import com.messageschallenge.notifications.repository.CategoryRepository;
import com.messageschallenge.notifications.repository.ChannelRepository;
import com.messageschallenge.notifications.repository.MessageRepository;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end sanity check that the EmailMockSender FAILURE_MARKER pattern drives a notification
 * through the real retry/DLQ path: RETRY for attempts 1..N-1, then DEAD_LETTER at max-attempts.
 * Complements NotificationProcessorIT (which uses a ControllableSmsSender) by exercising the real
 * Spring-wired EmailMockSender.
 */
class DlqFlowIT extends AbstractIntegrationTest {

  @Autowired NotificationProcessor processor;
  @Autowired NotificationRepository notifications;
  @Autowired MessageRepository messages;
  @Autowired UserRepository users;
  @Autowired CategoryRepository categories;
  @Autowired ChannelRepository channels;

  @Test
  @Transactional
  void emailWithFailureMarker_retriesThenLandsInDlq() {
    Long id = insertPendingEmailNotification("recipient" + EmailMockSender.FAILURE_MARKER + "x.io");

    // Attempts 1..4 should RETRY (default max-attempts = 5).
    for (int attempt = 1; attempt <= 4; attempt++) {
      var r = processor.process(new NotificationJob(id, attempt));
      assertThat(r.outcome()).isEqualTo(NotificationProcessor.Outcome.RETRY);
      assertThat(notifications.findById(id).orElseThrow().getStatus())
          .isEqualTo(NotificationStatus.FAILED);
    }

    // Attempt 5 exhausts the budget → DEAD_LETTER.
    var last = processor.process(new NotificationJob(id, 5));
    assertThat(last.outcome()).isEqualTo(NotificationProcessor.Outcome.DEAD_LETTER);
    assertThat(last.channel()).isEqualTo("EMAIL");
    assertThat(last.error()).contains("SIMULATED_FAILURE");

    Notification persisted = notifications.findById(id).orElseThrow();
    assertThat(persisted.getStatus()).isEqualTo(NotificationStatus.DEAD_LETTER);
    assertThat(persisted.getProviderId()).isNull();
    assertThat(persisted.getLastError()).contains("SIMULATED_FAILURE");
  }

  @Test
  @Transactional
  void emailWithoutFailureMarker_sendsSuccessfullyOnFirstAttempt() {
    Long id = insertPendingEmailNotification("plain@test.local");

    var r = processor.process(new NotificationJob(id, 1));

    assertThat(r.outcome()).isEqualTo(NotificationProcessor.Outcome.SUCCESS);
    assertThat(notifications.findById(id).orElseThrow().getStatus())
        .isEqualTo(NotificationStatus.SENT);
  }

  private Long insertPendingEmailNotification(String email) {
    Channel email_ = channels.findByCode("EMAIL").orElseThrow();
    // Unique user per test to avoid UNIQUE(email) collisions across runs within the shared
    // Testcontainers Postgres (withReuse=true).
    User user = users.save(new User("DLQ IT " + System.nanoTime(), email, "+525500000000"));
    Message m = messages.save(new Message(categories.findByCode("SPORTS").orElseThrow(), "body"));
    Map<String, Object> payload = new HashMap<>();
    payload.put("to", email);
    payload.put("subject", "Notification");
    payload.put("body", "body");
    return notifications.save(new Notification(m, user, email_, payload)).getId();
  }
}
