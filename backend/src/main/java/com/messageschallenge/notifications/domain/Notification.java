package com.messageschallenge.notifications.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(nullable = false, columnDefinition = "notification_status")
  private NotificationStatus status = NotificationStatus.PENDING;

  @Column(nullable = false)
  private int attempts = 0;

  @Column(name = "last_error")
  private String lastError;

  @Column(name = "provider_id", length = 128)
  private String providerId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> payload = new HashMap<>();

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "first_sent_at")
  private Instant firstSentAt;

  @Column(name = "last_attempt_at")
  private Instant lastAttemptAt;

  protected Notification() {}

  public Notification(Message message, User user, Channel channel) {
    this.message = message;
    this.user = user;
    this.channel = channel;
  }

  public Notification(Message message, User user, Channel channel, Map<String, Object> payload) {
    this(message, user, channel);
    this.payload = payload;
  }

  private static final Map<NotificationStatus, Set<NotificationStatus>> ALLOWED_TRANSITIONS;

  static {
    Map<NotificationStatus, Set<NotificationStatus>> m = new EnumMap<>(NotificationStatus.class);
    m.put(NotificationStatus.PENDING, EnumSet.of(NotificationStatus.SENDING));
    m.put(
        NotificationStatus.SENDING,
        EnumSet.of(
            NotificationStatus.SENT, NotificationStatus.FAILED, NotificationStatus.DEAD_LETTER));
    m.put(
        NotificationStatus.FAILED,
        EnumSet.of(NotificationStatus.SENDING, NotificationStatus.DEAD_LETTER));
    m.put(NotificationStatus.SENT, EnumSet.noneOf(NotificationStatus.class));
    m.put(NotificationStatus.DEAD_LETTER, EnumSet.noneOf(NotificationStatus.class));
    ALLOWED_TRANSITIONS = Map.copyOf(m);
  }

  private void transitionTo(NotificationStatus next) {
    Set<NotificationStatus> allowed =
        ALLOWED_TRANSITIONS.getOrDefault(this.status, EnumSet.noneOf(NotificationStatus.class));
    if (!allowed.contains(next)) {
      throw new IllegalStateException(
          "Illegal notification transition: " + this.status + " -> " + next);
    }
    this.status = next;
    this.lastAttemptAt = Instant.now();
  }

  public void markSending() {
    transitionTo(NotificationStatus.SENDING);
  }

  public void markSent(String providerId) {
    transitionTo(NotificationStatus.SENT);
    this.attempts += 1;
    if (this.firstSentAt == null) {
      this.firstSentAt = this.lastAttemptAt;
    }
    this.lastError = null;
    this.providerId = providerId;
  }

  public void markFailed(String error) {
    transitionTo(NotificationStatus.FAILED);
    this.attempts += 1;
    this.lastError = error;
  }

  public void markDeadLetter(String error) {
    transitionTo(NotificationStatus.DEAD_LETTER);
    this.lastError = error;
  }

  public boolean isTerminal() {
    return status == NotificationStatus.SENT || status == NotificationStatus.DEAD_LETTER;
  }

  public Long getId() {
    return id;
  }

  public Message getMessage() {
    return message;
  }

  public User getUser() {
    return user;
  }

  public Channel getChannel() {
    return channel;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public int getAttempts() {
    return attempts;
  }

  public String getLastError() {
    return lastError;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public String getProviderId() {
    return providerId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getFirstSentAt() {
    return firstSentAt;
  }
}
