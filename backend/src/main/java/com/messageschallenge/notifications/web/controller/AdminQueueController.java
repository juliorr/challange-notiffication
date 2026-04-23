package com.messageschallenge.notifications.web.controller;

import com.messageschallenge.notifications.queue.NotificationQueueInspector;
import com.messageschallenge.notifications.queue.NotificationQueueWorker;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/queue")
@ConditionalOnProperty(prefix = "app.admin", name = "enabled", havingValue = "true")
public class AdminQueueController {

  private final NotificationQueueInspector queue;
  private final ObjectProvider<NotificationQueueWorker> workerProvider;

  public AdminQueueController(
      NotificationQueueInspector queue, ObjectProvider<NotificationQueueWorker> workerProvider) {
    this.queue = queue;
    this.workerProvider = workerProvider;
  }

  @PostMapping("/pause")
  public Map<String, Object> pause() {
    NotificationQueueWorker worker = workerProvider.getIfAvailable();
    if (worker == null) {
      return Map.of("running", false, "note", "worker bean not present");
    }
    worker.pause();
    return Map.of("running", worker.isRunning());
  }

  @PostMapping("/resume")
  public Map<String, Object> resume() {
    NotificationQueueWorker worker = workerProvider.getIfAvailable();
    if (worker == null) {
      return Map.of("running", false, "note", "worker bean not present");
    }
    worker.resume();
    return Map.of("running", worker.isRunning());
  }

  @GetMapping("/status")
  public Map<String, Object> status() {
    NotificationQueueWorker worker = workerProvider.getIfAvailable();
    boolean running = worker != null && worker.isRunning();
    int threads = worker != null ? worker.threads() : 0;
    return Map.of(
        "running", running,
        "threads", threads,
        "queueSize", queue.size(),
        "delayedSize", queue.delayedSize(),
        "dlqSize", queue.dlqSize());
  }

  @GetMapping("/peek")
  public Map<String, List<?>> peek(@RequestParam(defaultValue = "20") int n) {
    return Map.of(
        "queue", queue.peek(n),
        "dlq", queue.peekDlq(n));
  }
}
