package com.messageschallenge.notifications.queue;

import static org.assertj.core.api.Assertions.assertThat;

import com.messageschallenge.notifications.AbstractIntegrationTest;
import com.messageschallenge.notifications.domain.Channel;
import com.messageschallenge.notifications.domain.Message;
import com.messageschallenge.notifications.domain.Notification;
import com.messageschallenge.notifications.domain.NotificationStatus;
import com.messageschallenge.notifications.domain.User;
import com.messageschallenge.notifications.notifications.ChannelSender;
import com.messageschallenge.notifications.repository.CategoryRepository;
import com.messageschallenge.notifications.repository.ChannelRepository;
import com.messageschallenge.notifications.repository.MessageRepository;
import com.messageschallenge.notifications.repository.NotificationRepository;
import com.messageschallenge.notifications.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@Import(NotificationProcessorIT.ControllableSmsConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationProcessorIT extends AbstractIntegrationTest {

  @Autowired NotificationProcessor processor;
  @Autowired NotificationRepository notifications;
  @Autowired MessageRepository messages;
  @Autowired UserRepository users;
  @Autowired CategoryRepository categories;
  @Autowired ChannelRepository channels;
  @Autowired ControllableSmsSender sms;

  @BeforeEach
  void reset() {
    sms.failNext.set(false);
    sms.alwaysFail.set(false);
    sms.sends.set(0);
  }

  @Test
  @Transactional
  void happyPath_marksSent() {
    sms.failNext.set(false);
    Long id = insertPendingSmsNotification();

    var r = processor.process(new NotificationJob(id, 1));

    assertThat(r.outcome()).isEqualTo(NotificationProcessor.Outcome.SUCCESS);
    Notification persisted = notifications.findById(id).orElseThrow();
    assertThat(persisted.getStatus()).isEqualTo(NotificationStatus.SENT);
    assertThat(persisted.getProviderId()).isEqualTo("mock-sms-test");
    assertThat(persisted.getFirstSentAt()).isNotNull();
    assertThat(persisted.getAttempts()).isEqualTo(1);
    assertThat(sms.sends.get()).isEqualTo(1);
  }

  @Test
  @Transactional
  void failureWithinBudget_returnsRetryAndMarksFailed() {
    sms.alwaysFail.set(true);
    Long id = insertPendingSmsNotification();

    var r = processor.process(new NotificationJob(id, 1));

    assertThat(r.outcome()).isEqualTo(NotificationProcessor.Outcome.RETRY);
    assertThat(r.retryDelay().toMillis()).isPositive();
    assertThat(notifications.findById(id).orElseThrow().getStatus())
        .isEqualTo(NotificationStatus.FAILED);
    assertThat(notifications.findById(id).orElseThrow().getAttempts()).isEqualTo(1);
  }

  @Test
  @Transactional
  void failureAtLastAttempt_returnsDeadLetter() {
    sms.alwaysFail.set(true);
    Long id = insertPendingSmsNotification();

    var r = processor.process(new NotificationJob(id, 5));

    assertThat(r.outcome()).isEqualTo(NotificationProcessor.Outcome.DEAD_LETTER);
    assertThat(r.channel()).isEqualTo("SMS");
    assertThat(r.error()).isNotBlank();
    Notification persisted = notifications.findById(id).orElseThrow();
    assertThat(persisted.getStatus()).isEqualTo(NotificationStatus.DEAD_LETTER);
    assertThat(persisted.getProviderId()).isNull();
    assertThat(persisted.getLastError()).isNotBlank();
  }

  @Test
  @Transactional
  void idempotent_whenAlreadySent_noSecondSendHappens() {
    Long id = insertPendingSmsNotification();
    processor.process(new NotificationJob(id, 1));
    int sendsAfterFirst = sms.sends.get();

    var r = processor.process(new NotificationJob(id, 2));

    assertThat(r.outcome()).isEqualTo(NotificationProcessor.Outcome.SUCCESS);
    assertThat(sms.sends.get()).isEqualTo(sendsAfterFirst);
  }

  @Test
  @Transactional
  void missingNotification_returnsMissing() {
    var r = processor.process(new NotificationJob(9_999_999L, 1));
    assertThat(r.outcome()).isEqualTo(NotificationProcessor.Outcome.MISSING);
  }

  @Test
  @Transactional
  void transientFailure_thenSuccess_marksSent() {
    sms.failNext.set(true);
    Long id = insertPendingSmsNotification();

    var r1 = processor.process(new NotificationJob(id, 1));
    assertThat(r1.outcome()).isEqualTo(NotificationProcessor.Outcome.RETRY);
    assertThat(notifications.findById(id).orElseThrow().getStatus())
        .isEqualTo(NotificationStatus.FAILED);
    assertThat(notifications.findById(id).orElseThrow().getAttempts()).isEqualTo(1);

    var r2 = processor.process(new NotificationJob(id, 2));
    assertThat(r2.outcome()).isEqualTo(NotificationProcessor.Outcome.SUCCESS);
    assertThat(notifications.findById(id).orElseThrow().getStatus())
        .isEqualTo(NotificationStatus.SENT);
    assertThat(sms.sends.get()).isEqualTo(2);
  }

  private Long insertPendingSmsNotification() {
    Channel sms = channels.findByCode("SMS").orElseThrow();
    User ana =
        users.findSubscribersWithChannelsByCategoryCode("SPORTS").stream()
            .filter(u -> u.getEmail().equals("ana@example.com"))
            .findFirst()
            .orElseGet(() -> users.findAll().get(0));
    Message m = messages.save(new Message(categories.findByCode("SPORTS").orElseThrow(), "body"));
    Map<String, Object> payload = new HashMap<>();
    payload.put("to", ana.getPhoneNumber());
    payload.put("body", "body");
    return notifications.save(new Notification(m, ana, sms, payload)).getId();
  }

  static class ControllableSmsSender implements ChannelSender {
    final AtomicBoolean failNext = new AtomicBoolean(false);
    final AtomicBoolean alwaysFail = new AtomicBoolean(false);
    final AtomicInteger sends = new AtomicInteger(0);

    @Override
    public SendResult send(Notification notification) {
      sends.incrementAndGet();
      if (alwaysFail.get() || failNext.getAndSet(false)) {
        return SendResult.failure("simulated");
      }
      return SendResult.success("mock-sms-test");
    }

    @Override
    public String channelCode() {
      return "SMS";
    }

    @Override
    public java.util.Set<String> requiredPayloadKeys() {
      return java.util.Set.of("to", "body");
    }
  }

  @TestConfiguration
  static class ControllableSmsConfig {

    @Bean(name = "smsMockSender")
    ControllableSmsSender smsMockSender() {
      return new ControllableSmsSender();
    }
  }
}
