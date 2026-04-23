package com.messageschallenge.notifications.web.sse;

import com.messageschallenge.notifications.config.AppProperties;
import com.messageschallenge.notifications.web.dto.NotificationView;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class NotificationSseBroadcaster implements SseBroadcaster {

  private static final Logger log = LoggerFactory.getLogger(NotificationSseBroadcaster.class);

  private final long timeoutMs;
  private final Map<String, SseEmitter> subscribers = new ConcurrentHashMap<>();

  public NotificationSseBroadcaster(AppProperties props) {
    this.timeoutMs = props.sse().timeoutMs();
  }

  @Override
  public SseEmitter subscribe() {
    String id = UUID.randomUUID().toString();
    SseEmitter emitter = new SseEmitter(timeoutMs);
    subscribers.put(id, emitter);
    emitter.onCompletion(() -> subscribers.remove(id));
    emitter.onTimeout(() -> subscribers.remove(id));
    emitter.onError(e -> subscribers.remove(id));
    try {
      emitter.send(SseEmitter.event().name("connected").data(Map.of("id", id)));
    } catch (IOException e) {
      subscribers.remove(id);
    }
    return emitter;
  }

  @Override
  public void publish(NotificationView view) {
    for (Map.Entry<String, SseEmitter> e : subscribers.entrySet()) {
      try {
        e.getValue().send(SseEmitter.event().name("notification").data(view));
      } catch (IOException ex) {
        log.debug("SSE subscriber {} dropped: {}", e.getKey(), ex.getMessage());
        subscribers.remove(e.getKey());
      }
    }
  }
}
