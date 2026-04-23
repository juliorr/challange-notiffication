package com.messageschallenge.notifications.web.dto;

import com.messageschallenge.notifications.domain.Notification;
import java.time.Instant;

public record NotificationView(
    Long id,
    Long messageId,
    String messageBody,
    UserSummary user,
    String channel,
    String category,
    String status,
    int attempts,
    String lastError,
    Instant createdAt,
    Instant firstSentAt) {
  public record UserSummary(Integer id, String name, String email) {}

  public static NotificationView from(Notification n) {
    return new NotificationView(
        n.getId(),
        n.getMessage().getId(),
        n.getMessage().getBody(),
        new UserSummary(n.getUser().getId(), n.getUser().getName(), n.getUser().getEmail()),
        n.getChannel().getCode(),
        n.getMessage().getCategory().getCode(),
        n.getStatus().name(),
        n.getAttempts(),
        n.getLastError(),
        n.getCreatedAt(),
        n.getFirstSentAt());
  }
}
