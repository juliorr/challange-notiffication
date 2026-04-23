package com.messageschallenge.notifications.queue;

import java.io.Serializable;
import java.time.Instant;

public record DeadLetterRecord(
    NotificationJob job, String channel, String error, String stackTrace, Instant failedAt)
    implements Serializable {

  private static final int MAX_STACK_TRACE_LENGTH = 4096;

  public static DeadLetterRecord of(
      NotificationJob job, String channel, String error, Throwable cause, Instant failedAt) {
    return new DeadLetterRecord(job, channel, error, truncateStackTrace(cause), failedAt);
  }

  private static String truncateStackTrace(Throwable cause) {
    if (cause == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(cause.getClass().getName());
    if (cause.getMessage() != null) {
      sb.append(": ").append(cause.getMessage());
    }
    for (StackTraceElement el : cause.getStackTrace()) {
      sb.append("\n\tat ").append(el);
      if (sb.length() >= MAX_STACK_TRACE_LENGTH) {
        break;
      }
    }
    if (sb.length() > MAX_STACK_TRACE_LENGTH) {
      return sb.substring(0, MAX_STACK_TRACE_LENGTH);
    }
    return sb.toString();
  }
}
