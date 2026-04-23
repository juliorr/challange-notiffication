package com.messageschallenge.notifications.queue;

import java.time.Instant;
import java.util.List;

public interface NotificationQueueInspector {

  int size();

  int dlqSize();

  int delayedSize();

  List<JobSummary> peek(int n);

  List<DlqEntrySummary> peekDlq(int n);

  record JobSummary(Long notificationId, int attempt) {
    public static JobSummary from(NotificationJob job) {
      return new JobSummary(job.notificationId(), job.attempt());
    }
  }

  record DlqEntrySummary(
      Long notificationId, int attempt, String channel, String error, Instant failedAt) {
    public static DlqEntrySummary from(DeadLetterRecord record) {
      NotificationJob job = record.job();
      return new DlqEntrySummary(
          job.notificationId(), job.attempt(), record.channel(), record.error(), record.failedAt());
    }
  }
}
